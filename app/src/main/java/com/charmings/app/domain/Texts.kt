package com.charmings.app.domain

/** Short app-voice lines shown in the companion's speech bubble. */
object Texts {
    val START = listOf(
        "Ходімо на пошуки!",
        "Ходімо шукати пухнастиків",
        "Час шукати друзів!"
    )
    val WALKING = listOf(
        "Тепліше... ще трошки!",
        "Хтось визирнув з-за кущів!",
        "Ще кілька кроків — і хтось наважиться привітатися.",
        "Чуєш шурхіт? Ти на правильному шляху!",
        "Чарівнятка люблять тих, хто не зупиняється."
    )
    val CAUGHT = listOf(
        "Вітаємо з новим другом!",
        "Сьогодні Вам щастить!",
        "Ого! Яка чудова знахідка!",
        "Є! Спіймали пустунця!",
        "Ура! Новий друг з нами!",
        "Ось він, наш скарб!"
    )
    val MISSED = listOf(
        "Тут щойно хтось був! Але втік..",
        "Тільки якась тінь майнула..",
        "Ой, лапки промайнули за рогом!",
        "Хвостик майнув і зник!"
    )
    val WALK_ENDED = listOf(
        "Схоже ви призупинили пошуки, але ще не всі звірята знайдені!",
        "Наші пухнасті друзі все ще десь ховаються!",
        "Ой, перерва? А хвостаті ще чекають на зустріч!",
        "Десь тут бігають самотні звірятка, час їх знайти!",
        "Ще стільки хвостиків можна знайти!"
    )
    val PAUSED = "Пошуки на паузі. Натисни «Гуляти», щоб продовжити."
    val NO_PERMISSION = "Дозволь рахувати кроки — і я поведу тебе до чарівняток."
}

/** Ukrainian plural forms: 1 крок, 2 кроки, 5 кроків, 21 крок, 11 кроків. */
fun pluralUk(n: Int, one: String, few: String, many: String): String {
    val n100 = n % 100
    val n10 = n % 10
    return when {
        n100 in 11..14 -> many
        n10 == 1 -> one
        n10 in 2..4 -> few
        else -> many
    }
}

fun stepsWord(n: Int) = pluralUk(n, "крок", "кроки", "кроків")
fun minutesWord(n: Int) = pluralUk(n, "хвилина", "хвилини", "хвилин")

/** 12345 → "12 345" (narrow no-break space keeps the number on one line). */
fun formatSteps(n: Int): String =
    String.format(java.util.Locale.US, "%,d", n).replace(',', ' ')
