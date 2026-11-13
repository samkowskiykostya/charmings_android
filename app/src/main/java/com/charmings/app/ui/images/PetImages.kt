package com.charmings.app.ui.images

import com.charmings.app.BuildConfig
import com.charmings.app.R

/**
 * Only tiny (24 px) thumbnails ship in the APK — used for "mystery" pets and as placeholders.
 * Full images live in /pet-images and are fetched + disk-cached by Coil on demand (pet 0 is bundled).
 * Explicit R references (not getIdentifier) keep resource shrinking safe.
 */
object PetImages {
    private val thumbs = intArrayOf(
        R.drawable.thumb_00,
        R.drawable.thumb_01,
        R.drawable.thumb_02,
        R.drawable.thumb_03,
        R.drawable.thumb_04,
        R.drawable.thumb_05,
        R.drawable.thumb_06,
        R.drawable.thumb_07,
        R.drawable.thumb_08,
        R.drawable.thumb_09,
        R.drawable.thumb_10,
        R.drawable.thumb_11,
        R.drawable.thumb_12,
        R.drawable.thumb_13,
        R.drawable.thumb_14,
        R.drawable.thumb_15,
        R.drawable.thumb_16,
        R.drawable.thumb_17,
        R.drawable.thumb_18,
        R.drawable.thumb_19,
        R.drawable.thumb_20,
        R.drawable.thumb_21,
        R.drawable.thumb_22,
        R.drawable.thumb_23,
        R.drawable.thumb_24,
        R.drawable.thumb_25,
        R.drawable.thumb_26,
        R.drawable.thumb_27,
        R.drawable.thumb_28,
        R.drawable.thumb_29,
        R.drawable.thumb_30,
        R.drawable.thumb_31,
        R.drawable.thumb_32,
        R.drawable.thumb_33,
        R.drawable.thumb_34,
        R.drawable.thumb_35,
        R.drawable.thumb_36,
        R.drawable.thumb_37,
        R.drawable.thumb_38,
        R.drawable.thumb_39,
        R.drawable.thumb_40,
        R.drawable.thumb_41,
        R.drawable.thumb_42,
        R.drawable.thumb_43,
        R.drawable.thumb_44,
        R.drawable.thumb_45,
        R.drawable.thumb_46,
        R.drawable.thumb_47,
        R.drawable.thumb_48,
        R.drawable.thumb_49,
        R.drawable.thumb_50,
        R.drawable.thumb_51,
        R.drawable.thumb_52,
        R.drawable.thumb_53,
        R.drawable.thumb_54,
        R.drawable.thumb_55,
        R.drawable.thumb_56,
        R.drawable.thumb_57,
        R.drawable.thumb_58,
        R.drawable.thumb_59,
        R.drawable.thumb_60,
        R.drawable.thumb_61,
        R.drawable.thumb_62,
        R.drawable.thumb_63,
        R.drawable.thumb_64,
        R.drawable.thumb_65,
        R.drawable.thumb_66,
    )

    fun thumb(petId: Int): Int = thumbs.getOrElse(petId) { R.drawable.thumb_00 }

    /** Bundled full image, if any. */
    fun bundled(petId: Int): Int? = if (petId == 0) R.drawable.pet_00_full else null

    fun url(petId: Int): String = BuildConfig.PET_IMAGE_BASE_URL + String.format(java.util.Locale.US, "pet_%02d.webp", petId)
}
