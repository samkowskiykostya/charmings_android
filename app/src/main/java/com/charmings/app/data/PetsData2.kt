package com.charmings.app.data

import com.charmings.app.data.model.Pet
import com.charmings.app.data.model.Requirement

// Pets 21-45
val pets2: List<Pet> = listOf(
    Pet(21, "Омелько", "Злісний наклепник.", "Насправді це я повинен був бути вашим провідником...", "image_21", "Ніхто не знає коли можна зустріти.", 0.3, listOf(Requirement(steps = 2000))),
    Pet(22, "Угуся", "Привітна і товариська. Сова.", "Привіт! Я Угуся!", "image_22", "Прогулюйтесь протягом 15 хв вночі.", 0.25, listOf(Requirement(steps = 1000, time = listOf(23, 5)))),
    Pet(23, "Часик", "Корисний, але не дасть розслабитись.", "Ви запізнилися знову?", "image_23", "Прогулюйтесь протягом 30 хв вдень.", 0.15, listOf(Requirement(steps = 2000, time = listOf(11, 17)))),
    Pet(24, "Вечорничка", "Любить пригоди.", "Добрий вечір!", "image_24", "Прогулюйтесь протягом 30 хв увечері.", 0.15, listOf(Requirement(steps = 2000, time = listOf(17, 23)))),
    Pet(25, "Жуланчик", "Дає заряд бадьорості на день.", "Доброго ранку!", "image_25", "Прогулюйтесь протягом 30 хв зранку.", 0.15, listOf(Requirement(steps = 2000, time = listOf(5, 11)))),
    Pet(26, "Нюта", "Музика - її життя і доля.", "Привіт! Ви любите класичну музику?", "image_26", "Ніхто не знає коли можна зустріти.", 0.15, listOf(Requirement(steps = 2000))),
    Pet(27, "Піона", "Невиправна любителька романтики.", "Втомилися від сірого буття?", "image_27", "Прогулюйтесь протягом 20 хв весною.", 0.15, listOf(Requirement(steps = 1400, month = listOf("march", "april", "may")))),
    Pet(28, "Нюк", "Загадкове створіння.", "Угрм.. Угрм-угрм-угрм!", "image_28", "Прогулюйтесь протягом 30 хв влітку.", 0.15, listOf(Requirement(steps = 2000, month = listOf("june", "july", "august")))),
    Pet(29, "Жовтанчик", "Моторошне чарівнятко.", "Осінь, це краса руйнації...", "image_29", "Прогулюйтесь протягом 20 хв восени.", 0.15, listOf(Requirement(steps = 1400, month = listOf("september", "october", "november")))),
    Pet(30, "Сніжинка", "Ще одне патетичне чарівнятко.", "Привіт. Ти знаєш, що сніжинки унікальні?", "image_30", "Прогулюйтесь протягом 15 хв взимку.", 0.15, listOf(Requirement(steps = 1000, month = listOf("december", "january", "february")))),
    Pet(31, "Ангелятко", "Найнатхненніша захисниця людей.", "Запам'ятай! Світло в серці твоєму...", "image_31", "Прогулюйтесь протягом 30 хв в ясну погоду.", 1.0, listOf(Requirement(steps = 2000, weather = listOf("sunny")))),
    Pet(32, "Хмареня", "Хмари плану не мають!", "Слухай, мандрівник...", "image_32", "Прогулюйтесь протягом 30 хв в хмарну погоду.", 1.0, listOf(Requirement(steps = 2000, weather = listOf("partly cloudy", "cloudy", "overcast")))),
    Pet(33, "Дощовинка", "Дружелюбна і допитлива.", "О, небо високо десь там!", "image_33", "Прогулюйтесь протягом 15 хв в дощову погоду.", 1.0, listOf(Requirement(steps = 1000, weather = listOf("patchy light drizzle", "light drizzle", "freezing drizzle", "heavy freezing drizzle", "patchy light rain", "light rain", "moderate rain at times", "moderate rain", "heavy rain at times", "heavy rain")))),
    Pet(34, "Туманюсик", "Пухнаста бубочка.", "Привіт. Туман надає загадковості...", "image_34", "Прогулюйтесь протягом 15 хв в туманну погоду.", 1.0, listOf(Requirement(steps = 1000, weather = listOf("fog")))),
    Pet(35, "Шурхотун", "Еталон грації та легкості.", "Дозволь, щоб шепіт вітру...", "image_35", "Прогулюйтесь протягом 15 хв в вітряну погоду влітку.", 1.0, listOf(Requirement(steps = 1000, month = listOf("june", "july", "august"), windSpeed = listOf("15", "_")))),
    Pet(36, "Сніжничок", "Не рекомендується не ліпити сніговика!", "Здрастуй! Я Сніжничок.", "image_36", "Прогулюйтесь протягом 15 хв в сніжну погоду.", 1.0, listOf(Requirement(steps = 1000, weather = listOf("light sleet", "moderate or heavy sleet", "patchy moderate snow", "moderate snow", "patchy heavy snow", "heavy snow", "light snow showers", "moderate or heavy snow showers", "blowing snow", "blizzard")))),
    Pet(37, "Ням-нямка", "Енергійне, люб'язне. Смачне…", "Привіт-привіт!", "image_37", "Прогулюйтесь протягом 30 хв в теплу погоду.", 1.0, listOf(Requirement(steps = 2000, temperature = listOf("15", "26")))),
    Pet(38, "Кактусеня", "З розумінням ставиться до спеки.", "Доброго дня!", "image_38", "Прогулюйтесь протягом 15 хв в спекотну погоду.", 1.0, listOf(Requirement(steps = 1000, temperature = listOf("26", "_")))),
    Pet(39, "Котон", "Інтроверт, домосід.", "Привіт!", "image_39", "Прогулюйтесь протягом 15 хв в прохолодну погоду.", 1.0, listOf(Requirement(steps = 1000, temperature = listOf("0", "15")))),
    Pet(40, "Паморозь", "Вважає себе головною в країні іроній.", "Ох, знову ти з'явився?", "image_40", "Прогулюйтесь протягом 15 хв в морозну погоду.", 1.0, listOf(Requirement(steps = 1000, temperature = listOf("_", "-1")))),
    Pet(41, "Дюдя", "Замкнена істота.", "Як ви помітили мене?", "image_41", "Прогулюйтесь протягом 15 хв зранку за морозної погоди.", 1.0, listOf(Requirement(steps = 1000, time = listOf(5, 9), weather = listOf("sunny"), temperature = listOf("_", "-1")))),
    Pet(42, "Тон", "Ентузіаст і екоактивіст.", "Вітаю тебе, подорожній.", "image_42", "Прогулюйтесь 15 хв в холодну дощову погоду.", 1.0, listOf(Requirement(steps = 1000, weather = listOf("patchy light drizzle", "light drizzle", "freezing drizzle", "heavy freezing drizzle", "patchy light rain", "light rain", "moderate rain at times", "moderate rain", "heavy rain at times", "heavy rain"), temperature = listOf("-5", "15")))),
    Pet(43, "Ропавк", "Миле чарівнятко.", "Ква-ква!", "image_43", "Прогулюйтесь 15 хв дощовим ранком.", 1.0, listOf(Requirement(steps = 1000, weather = listOf("patchy light drizzle", "light drizzle", "freezing drizzle", "heavy freezing drizzle", "patchy light rain", "light rain", "moderate rain at times", "moderate rain", "heavy rain at times", "heavy rain"), time = listOf(5, 11)))),
    Pet(44, "Сніжниця", "Майстер снігових шедеврів.", "Привіт! Що за несподівана зустріч!", "image_44", "Прогулюйтесь 5 хв в хуртовину.", 1.0, listOf(Requirement(steps = 400, weather = listOf("blowing snow", "blizzard")))),
    Pet(45, "Мур-Мряка", "Зверхнє створіння.", "Так так так... погляньмо, хто тут.", "image_45", "Прогулюйтесь в п'ятницю 13 протягом 15 хв.", 1.0, listOf(Requirement(steps = 1000, day = "friday", dateday = 13)))
)
