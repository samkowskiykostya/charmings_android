package com.charmings.app.data

import com.charmings.app.data.model.Pet
import com.charmings.app.data.model.Requirement

// Pets 46-66
val pets3: List<Pet> = listOf(
    Pet(46, "Зая", "Любителька екзотичних схованок з яйцями!", "Привіт-привіт!", "image_46", "Прогулюйтесь на Великдень протягом 15 хв.", 1.0, listOf(Requirement(steps = 1000, holiday = "Великдень"))),
    Pet(47, "Рябка", "Планування від 'А' до 'Я'.", "Привіт! А чи підготував ти плани?", "image_47", "Прогулюйтесь 30 хв в перший день місяця.", 1.0, listOf(Requirement(steps = 2000, dateday = 1))),
    Pet(48, "Гууля", "Хочете відпочити? Забудьте про це…", "Кожен день я знаю, що робити...", "image_48", "Прогулюйтесь протягом 15 хв в суботу або п'ятницю.", 1.0, listOf(Requirement(steps = 1000, day = "saturday,friday"))),
    Pet(49, "Ціпа", "Позитивна і мила.", "Привіт. Любиш гратися у піжмурки?", "image_49", "Прогулюйтесь на Великдень протягом 30 хв.", 1.0, listOf(Requirement(steps = 1000, holiday = "Великдень"))),
    Pet(50, "Пряничок", "Справжній оптиміст.", "Привіт. Різдво - найкращий період!", "image_50", "Прогулюйтесь на Різдво протягом 15 хв.", 1.0, listOf(Requirement(steps = 1000, holiday = "Різдво"))),
    Pet(51, "Сніжуня", "Має різкий соціальний характер.", "Сніг може бути таким красивим...", "image_51", "Прогулюйтесь 15 хв у зимовий вікенд коли сніг.", 1.0, listOf(Requirement(steps = 2000, day = "saturday,sunday", weather = listOf("light sleet", "moderate or heavy sleet", "patchy moderate snow", "moderate snow", "patchy heavy snow", "heavy snow", "light snow showers", "moderate or heavy snow showers", "blowing snow", "blizzard"), month = listOf("december", "january", "february")))),
    Pet(52, "Зоряник", "Спокійний, схильний до самопізнання.", "Ніч - найкращий час для роздумів.", "image_52", "Прогулюйтесь протягом 15 хв ясними ночами взимку.", 1.0, listOf(Requirement(steps = 1000, time = listOf(23, 5), weather = listOf("clear"), month = listOf("december", "january", "february")))),
    Pet(53, "Вандрик", "Любовний! Жук!", "Привіт, дорогий друже! Я Вандрик!", "image_53", "Прогулюйтесь протягом 15 хв на день Валентина.", 1.0, listOf(Requirement(steps = 1000, holiday = "день святого валентина"))),
    Pet(54, "Різдвян", "Приносить атмосферу різдва.", "Вітаю! Я радий бачити вас!", "image_54", "Прогулюйтесь протягом 15 хв на Різдво.", 1.0, listOf(Requirement(steps = 1000, holiday = "Різдво"))),
    Pet(55, "Гарбузеня", "Безтурботне і жартівливе.", "Буууу! Ти злякався мене?", "image_55", "Прогулюйтесь протягом 15 хв на Хелловін.", 1.0, listOf(Requirement(steps = 1000, holiday = "хелловін"))),
    Pet(56, "Папоротеня", "Хитре!", "Але ж кого ми тут маємо!", "image_56", "Прогулюйтесь протягом 15 хв на Свято Купала.", 1.0, listOf(Requirement(steps = 1000, holiday = "івана купала"))),
    Pet(57, "Ельзимка", "Магічні здібності найпотужніші!", "З Новим Роком!", "image_57", "Прогулюйтесь протягом 15 хв на Новий рік.", 1.0, listOf(Requirement(steps = 1000, holiday = "новий рік"))),
    Pet(58, "Часомір", "Цілеспрямований і амбітний.", "Ти довго йшов до своєї мети...", "image_58", "Ніхто не знає коли можна зустріти.", 0.5, listOf(Requirement(steps = 4000))),
    Pet(59, "Чичюк", "Можливо, якби він не одягався як бандит...", "Вітаю… Чим можу допомогти?", "image_59", "Прогулюйтесь протягом 20 хв увечері восени.", 1.0, listOf(Requirement(steps = 1400, month = listOf("september", "october", "november"), time = listOf(17, 23)))),
    Pet(60, "Дрімка", "Маленьке ніжне створіння.", "Вітаю. Рада бачити вас!", "image_60", "Прогулюйтесь щонайменше 1 км.", 0.15, listOf(Requirement(distance = 1000))),
    Pet(61, "Боровичок", "Гриб-гуморист!", "Привіт. Знаєш як відрізнити гриба?", "image_61", "Прогулюйтесь протягом 30 хв восени в дощ.", 1.0, listOf(Requirement(steps = 2000, month = listOf("september", "october", "november"), time = listOf(17, 23)))),
    Pet(62, "Дзвінка", "Надихає до навчання.", "Вітаю! Навчання - ключ до успіху!", "image_62", "Прогулюйтесь протягом 20 хв на День Знань.", 1.0, listOf(Requirement(steps = 1400, holiday = "день знань"))),
    Pet(63, "Темногляд", "Уособлення непроглядної темноти.", "Спробуємо... зрозуміти... разом...", "image_63", "Прогулюйтесь протягом 10 хв коли нічне небо хмарне.", 1.0, listOf(Requirement(steps = 700, time = listOf(23, 5), weather = listOf("partly cloudy", "cloudy", "overcast")))),
    Pet(64, "Лаврик", "Усі жарти трохи старомодні.", "Привіт, я довго чекав!", "image_64", "Прогулюйтесь щонайменше 2 км.", 0.15, listOf(Requirement(steps = 4000))),
    Pet(65, "Зефірка", "Справжня оптимістка.", "Вітаю тебе на початку нового дня!", "image_65", "Прогулюйтесь щонайменше 1 км рано вранці.", 0.6, listOf(Requirement(steps = 1500, time = listOf(5, 8)))),
    Pet(66, "Чорничка", "Любителька літератури.", "Що ти бачиш, коли дивишся на світ?", "image_66", "Прогулюйтесь щонайменше 1 км пізно увечері.", 0.3, listOf(Requirement(steps = 1500, time = listOf(20, 24))))
)

// Combined list of all pets
object PetsData {
    val pets: List<Pet> = pets1 + pets2 + pets3
    
    fun getPetById(id: Int): Pet? = pets.find { it.id == id }
}
