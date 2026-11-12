package com.kirill.physiognomy2.data

import java.util.Locale

object ResultComposer {

    data class Answered(
        val step: Step,
        val question: Question,
        val option: AnswerOption,
    )

    fun answered(answers: Map<String, String>): List<Answered> =
        PhysiognomyCatalog.steps.flatMap { step ->
            step.questions.mapNotNull { question ->
                val optionId = answers[question.id] ?: return@mapNotNull null
                val option = question.options.firstOrNull { it.id == optionId } ?: return@mapNotNull null
                Answered(step, question, option)
            }
        }

    fun missing(answers: Map<String, String>): List<Pair<Step, Question>> =
        PhysiognomyCatalog.steps.flatMap { step ->
            step.questions
                .filter { !answers.containsKey(it.id) }
                .map { step to it }
        }

    fun profileTitle(answers: Map<String, String>): String {
        val values = answered(answers).flatMap { it.option.traits }
        val countable = values
            .map { it.lowercase(Locale("ru")).trim() }
            .filter { it.length in 4..40 }
        if (countable.isEmpty()) return "Портрет ещё не составлен"

        val dominant = countable
            .groupingBy { it }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .take(1)
            .firstOrNull()

        return if (dominant == null) "Ваш физиогномический портрет"
        else "Ведущая черта — ${dominant.key}"
    }

    fun buildText(
        answers: Map<String, String>,
        name: String? = null,
    ): String {
        val answered = answered(answers)
        val missing = missing(answers)
        val builder = StringBuilder()

        builder.appendLine("ФИЗИОГНОМИЧЕСКИЙ ПОРТРЕТ")
        if (!name.isNullOrBlank()) builder.appendLine(name.trim())
        builder.appendLine()

        if (answered.isEmpty()) {
            builder.appendLine("Пока не отмечено ни одной черты. Пройдите шаги чек-листа, чтобы получить описание.")
            return builder.toString().trimEnd()
        }

        builder.appendLine("Отмечено ${answered.size} из ${PhysiognomyCatalog.totalQuestions} черт.")
        builder.appendLine()

        builder.appendLine(profileTitle(answers))
        builder.appendLine()

        val allTraits = answered.flatMap { it.option.traits }
        val grouped = allTraits
            .groupingBy { it }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
        if (grouped.isNotEmpty()) {
            builder.appendLine("КЛЮЧЕВЫЕ КАЧЕСТВА")
            builder.appendLine(grouped.joinToString(" · ") { it.key })
            builder.appendLine()
        }

        builder.appendLine("ПО ЧЕРТАМ")
        var currentStep = -1
        answered.forEach { entry ->
            if (entry.step.number != currentStep) {
                currentStep = entry.step.number
                builder.appendLine()
                builder.appendLine("${entry.step.number}. ${entry.step.title}")
            }
            builder.appendLine("• ${entry.option.label}")
            if (entry.option.traits.isNotEmpty()) {
                builder.appendLine("  ${entry.option.traits.joinToString(" · ")}")
            }
            builder.appendLine("  ${entry.option.description}")
        }

        builder.appendLine()
        builder.appendLine("ОБЩИЙ ВЫВОД")
        builder.appendLine(buildConclusion(answered))

        if (missing.isNotEmpty()) {
            builder.appendLine()
            val steps = missing.map { it.first.number }.distinct()
            builder.appendLine(
                "Не отмечено черт: ${missing.size}. Пропущенные шаги: " +
                    steps.joinToString(", ") { PhysiognomyCatalog.step(it).title.lowercase(Locale("ru")) } + ".",
            )
        }

        builder.appendLine()
        builder.append("Составлено в приложении «Физиогномика» по мотивам книги Мэк Фулфер «Искусство чтения по лицу».")
        return builder.toString().trimEnd()
    }

    private fun buildConclusion(answered: List<Answered>): String {
        val traits = answered.flatMap { it.option.traits }.map { it.lowercase(Locale("ru")) }
        val text = traits.joinToString(" ")
        val sentences = mutableListOf<String>()

        val mind = score(
            text,
            "логик", "факт", "аналитик", "ум", "мышлен", "расчёт", "рассуд", "интеллект", "точн", "систем",
        )
        val feeling = score(
            text,
            "чувств", "эмоци", "сочувств", "довер", "открыт", "тепл", "забот", "интуиц", "чутк", "серд",
        )
        val will = score(
            text,
            "воля", "решит", "упор", "цел", "настойчив", "лидер", "власт", "контрол", "дисциплин", "доминир",
        )
        val social = score(
            text,
            "общит", "экстраверт", "контакт", "коллектив", "люд", "переговор", "дипломат", "команд",
        )
        val caution = score(
            text,
            "осторож", "сдержан", "подозр", "закрыт", "скептиц", "замкнут", "защит", "насторож",
        )
        val creative = score(
            text,
            "творч", "воображ", "иде", "оригинальн", "мечтат", "художеств", "нестандарт",
        )

        fun strongest(vararg pairs: Pair<String, Int>): Pair<String, Int> =
            pairs.maxByOrNull { it.second } ?: ("" to 0)

        val leading = strongest(
            "ум и логика" to mind,
            "чувства и интуиция" to feeling,
            "воля и целеустремлённость" to will,
            "общительность" to social,
            "осторожность" to caution,
            "творческое начало" to creative,
        )

        sentences += when (leading.first) {
            "ум и логика" -> "В вашем лице сильнее всего читается аналитический склад: вы опираетесь на факты и выстраиваете решения последовательно."
            "чувства и интуиция" -> "Ведущая линия вашего лица — эмоциональная: вы принимаете мир через переживания и тонко чувствуете людей."
            "воля и целеустремлённость" -> "Ярче всего в вашем лице выражена воля: вы умеете ставить цель и идти к ней, не сворачивая."
            "общительность" -> "Ваше лицо выдаёт человека, ориентированного на людей: вам важен контакт, разговор и общее дело."
            "осторожность" -> "В вашем лице читается осмотрительность: вы держите дистанцию и сначала проверяете, потом доверяете."
            "творческое начало" -> "В вашем лице заметнее всего творческое начало: вы ищете собственный путь и не любите жёстких рамок."
            else -> "Черты вашего лица складываются в спокойный, ровный рисунок без резко доминирующей линии."
        }

        val secondary = strongest(
            "ум и логика" to mind,
            "чувства и интуиция" to feeling,
            "воля и целеустремлённость" to will,
            "общительность" to social,
            "осторожность" to caution,
            "творческое начало" to creative,
        )
        if (secondary.first != leading.first && secondary.second > 0) {
            sentences += "Дополняет портрет ${secondary.first}: эта сторона проявляется в ваших реакциях и решениях."
        }

        val stepsWithAnswers = answered.map { it.step.number }.distinct()
        val zones = when {
            1 in stepsWithAnswers && 15 in stepsWithAnswers && 7 in stepsWithAnswers ->
                "Судя по сочетанию лба, носа и подбородка, вы соединяете мыслительную работу с практической хваткой."
            1 in stepsWithAnswers && 3 in stepsWithAnswers ->
                "Верхняя часть лица и глаза говорят о том, как вы воспринимаете информацию и как её обрабатываете."
            else -> null
        }
        if (zones != null) sentences += zones

        if (answered.size >= 20) {
            sentences += "Портрет получился подробным: чем больше черт отмечено, тем точнее читается общий рисунок характера."
        } else if (answered.size >= 8) {
            sentences += "Портрет уже достаточно выразителен, но его можно уточнить, отметив оставшиеся черты."
        } else {
            sentences += "Это набросок: отметьте больше черт, чтобы описание стало полнее."
        }

        sentences += "Помните, что физиогномика — язык наблюдений, а не приговор: лицо меняется вместе с опытом и привычками."

        return sentences.joinToString(" ")
    }

    private fun score(text: String, vararg roots: String): Int =
        roots.sumOf { root -> Regex(Regex.escape(root)).findAll(text).count() }
}
