package com.charmings.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.charmings.app.data.model.Pet
import com.charmings.app.data.repository.WalkingHistoryEntry
import com.charmings.app.domain.HolidayCalculator
import com.charmings.app.domain.Texts
import com.charmings.app.domain.WalkState
import com.charmings.app.graph
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class TrackingStatus { ACTIVE, PAUSED, NO_PERMISSION }

data class DashboardState(
    val walk: WalkState = WalkState(),
    val message: String = Texts.START.first(),
    val newCatches: List<Int> = emptyList(),
    val dateLabel: String = "",
    val holiday: String? = null,
    val status: TrackingStatus = TrackingStatus.ACTIVE,
    val history: List<WalkingHistoryEntry> = emptyList()
)

data class CollectionState(
    val caught: List<Pet> = emptyList(),
    val hiding: List<Pet> = emptyList(),
    val newIds: Set<Int> = emptySet(),
    val total: Int = 0,
    val isLoading: Boolean = true
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val graph = application.graph
    private val petRepository = graph.petRepository

    private val hasPermission = MutableStateFlow(false)
    private val trackingEnabled = MutableStateFlow(graph.settings.trackingEnabled)
    private val today = MutableStateFlow(LocalDate.now())
    private val startMessage = Texts.START.random()

    private val status = combine(hasPermission, trackingEnabled, graph.trackingActive) { perm, enabled, active ->
        when {
            !perm -> TrackingStatus.NO_PERMISSION
            enabled && active -> TrackingStatus.ACTIVE
            else -> TrackingStatus.PAUSED
        }
    }

    val dashboardState: StateFlow<DashboardState> = combine(
        graph.tracker.state,
        graph.walkRepository.encouragementFlow,
        combine(petRepository.newCatchesFlow, graph.walkRepository.historyFlow, ::Pair),
        status,
        today
    ) { walk, message, (newCatches, history), status, date ->
        DashboardState(
            walk = walk,
            message = when (status) {
                TrackingStatus.NO_PERMISSION -> Texts.NO_PERMISSION
                TrackingStatus.PAUSED -> Texts.PAUSED
                TrackingStatus.ACTIVE -> message ?: startMessage
            },
            newCatches = newCatches.filter { it != 0 }.reversed(),
            dateLabel = formatDate(date),
            holiday = HolidayCalculator.todayHolidayName(date),
            status = status,
            history = history
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardState())

    val collectionState: StateFlow<CollectionState> = combine(
        petRepository.caughtPetIdsFlow,
        petRepository.newCatchesFlow
    ) { caughtIds, newIds ->
        val caughtSet = caughtIds.toSet()
        val all = petRepository.allPets
        CollectionState(
            // Newest finds first: it's the order people care about.
            caught = caughtIds.asReversed().mapNotNull { petRepository.getPetById(it) },
            hiding = all.filter { it.id !in caughtSet },
            newIds = newIds.toSet(),
            total = all.size,
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CollectionState())

    fun getPetById(id: Int): Pet? = petRepository.getPetById(id)

    fun markSeen(petId: Int) {
        viewModelScope.launch { petRepository.removeFromNewCatches(petId) }
    }

    /** Called from onResume: permissions may have changed in system settings, the day may have rolled. */
    fun refresh(permissionGranted: Boolean) {
        hasPermission.value = permissionGranted
        trackingEnabled.value = graph.settings.trackingEnabled
        today.value = LocalDate.now()
    }

    fun setTrackingEnabled(enabled: Boolean) {
        graph.settings.trackingEnabled = enabled
        trackingEnabled.value = enabled
    }

    private fun formatDate(date: LocalDate): String =
        date.format(DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale("uk")))
            .replaceFirstChar { it.titlecase(Locale("uk")) }
}
