package com.charmings.app.domain

import com.charmings.app.data.api.WeatherApi
import com.charmings.app.data.model.Pet
import com.charmings.app.data.model.WeatherCurrent
import com.charmings.app.data.repository.PetRepository
import com.charmings.app.data.repository.WalkRepository
import java.time.LocalDateTime
import kotlin.random.Random

data class CheckOutcome(val caught: Pet?, val rolledPetIds: List<Int>)

class PetCatcher(
    private val petRepository: PetRepository,
    private val walkRepository: WalkRepository,
    private val weatherApi: WeatherApi,
    private val random: Random = Random.Default
) {

    companion object {
        /** Minimum walk steps before anything can appear. */
        const val STEP_THRESHOLD_FOR_PET_CHECK = 400
        const val PET_CHECK_INTERVAL_MS = 3 * 60 * 1000L
    }

    /**
     * Each eligible pet gets exactly one roll per walk, so `probability` means what it says
     * (rolling every 3 minutes turned a "15%" pet into ~80% over a 30-minute walk).
     * Weather pets that don't match *yet* are not rolled — the weather may still change.
     */
    suspend fun check(state: WalkState, now: LocalDateTime = LocalDateTime.now()): CheckOutcome {
        val caughtIds = petRepository.getCaughtPetIds().toSet()
        val rolled = state.rolledPetIds.toMutableSet()

        val candidates = petRepository.allPets
            .filter { it.id !in caughtIds && it.id !in rolled }
            .filter { RuleChecker.basicSatisfied(it.requirements, state.stepsSinceCatch, now) }
            .shuffled(random)
            .sortedBy { it.needsWeather } // stable: keeps the shuffle, weather pets last

        var weather: WeatherCurrent? = null
        var weatherFetched = false
        var missed = false

        for (pet in candidates) {
            if (pet.needsWeather) {
                if (!weatherFetched) {
                    weather = weatherApi.getCurrentWeather()
                    weatherFetched = true
                }
                val w = weather ?: continue
                if (!RuleChecker.weatherSatisfied(pet.requirements, w)) continue
            }
            rolled += pet.id
            if (random.nextDouble() < pet.probability) {
                petRepository.addCaughtPet(pet.id)
                walkRepository.setEncouragement(Texts.CAUGHT.random(), force = true)
                return CheckOutcome(pet, rolled.toList())
            }
            missed = true
        }
        if (missed) walkRepository.setEncouragement(Texts.MISSED.random(), force = true)
        return CheckOutcome(null, rolled.toList())
    }
}
