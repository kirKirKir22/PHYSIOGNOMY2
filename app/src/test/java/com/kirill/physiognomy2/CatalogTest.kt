package com.kirill.physiognomy2

import com.kirill.physiognomy2.data.PhysiognomyCatalog
import com.kirill.physiognomy2.data.ResultComposer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CatalogTest {

    @Test
    fun `catalog has expected size`() {
        assertEquals(26, PhysiognomyCatalog.steps.size)
        assertEquals(44, PhysiognomyCatalog.totalQuestions)
    }

    @Test
    fun `step numbers are sequential`() {
        val numbers = PhysiognomyCatalog.steps.map { it.number }
        assertEquals((1..PhysiognomyCatalog.steps.size).toList(), numbers)
    }

    @Test
    fun `every step has at least one question`() {
        PhysiognomyCatalog.steps.forEach { step ->
            assertTrue("Шаг ${step.number} без вопросов", step.questions.isNotEmpty())
        }
    }

    @Test
    fun `every question has at least one option`() {
        PhysiognomyCatalog.steps.forEach { step ->
            step.questions.forEach { question ->
                assertTrue(
                    "Вопрос ${question.id} без вариантов",
                    question.options.isNotEmpty(),
                )
            }
        }
    }

    @Test
    fun `question ids are unique`() {
        val ids = PhysiognomyCatalog.steps.flatMap { step -> step.questions.map { it.id } }
        assertEquals(ids.size, ids.distinct().size)
    }

    @Test
    fun `option ids are unique`() {
        val ids = PhysiognomyCatalog.steps.flatMap { step ->
            step.questions.flatMap { question -> question.options.map { it.id } }
        }
        assertEquals(ids.size, ids.distinct().size)
    }

    @Test
    fun `option ids start with question id`() {
        PhysiognomyCatalog.steps.forEach { step ->
            step.questions.forEach { question ->
                question.options.forEach { option ->
                    assertTrue(
                        "Вариант ${option.id} не относится к вопросу ${question.id}",
                        option.id.startsWith("${question.id}-"),
                    )
                }
            }
        }
    }

    @Test
    fun `option has non blank label and description`() {
        PhysiognomyCatalog.steps.forEach { step ->
            step.questions.forEach { question ->
                question.options.forEach { option ->
                    assertTrue("Пустая подпись в ${option.id}", option.label.isNotBlank())
                    assertTrue("Пустое описание в ${option.id}", option.description.isNotBlank())
                }
            }
        }
    }

    @Test
    fun `option lookup by id works`() {
        PhysiognomyCatalog.steps.forEach { step ->
            step.questions.forEach { question ->
                question.options.forEach { option ->
                    assertEquals(option, PhysiognomyCatalog.option(option.id))
                    assertEquals(
                        option.label,
                        PhysiognomyCatalog.optionLabel(question.id, option.id),
                    )
                    assertEquals(step, PhysiognomyCatalog.stepOf(question.id))
                }
            }
        }
    }

    @Test
    fun `step lookup falls back to first step`() {
        assertEquals(1, PhysiognomyCatalog.step(0).number)
        assertEquals(1, PhysiognomyCatalog.step(999).number)
    }

    @Test
    fun `sections cover all steps`() {
        val covered = com.kirill.physiognomy2.data.Section.entries.flatMap { section ->
            section.firstStep..section.lastStep
        }
        assertEquals(PhysiognomyCatalog.steps.map { it.number }, covered)
    }
}

class ResultComposerTest {

    @Test
    fun `empty answers produce placeholder`() {
        val text = ResultComposer.buildText(emptyMap())
        assertTrue(text.contains("Пока не отмечено"))
        assertEquals("Портрет ещё не составлен", ResultComposer.profileTitle(emptyMap()))
    }

    @Test
    fun `unknown option ids are ignored`() {
        val text = ResultComposer.buildText(mapOf("s1q1" to "missing-option"))
        assertTrue(text.contains("Пока не отмечено"))
    }

    @Test
    fun `answered entries match selected options`() {
        val step = PhysiognomyCatalog.step(1)
        val question = step.questions.first()
        val option = question.options.first()
        val answered = ResultComposer.answered(mapOf(question.id to option.id))

        assertEquals(1, answered.size)
        assertEquals(step.number, answered.first().step.number)
        assertEquals(option, answered.first().option)
    }

    @Test
    fun `missing counts every unanswered question`() {
        val step = PhysiognomyCatalog.step(1)
        val question = step.questions.first()
        val option = question.options.first()
        val missing = ResultComposer.missing(mapOf(question.id to option.id))

        assertEquals(PhysiognomyCatalog.totalQuestions - 1, missing.size)
        assertTrue(missing.none { it.second.id == question.id })
    }

    @Test
    fun `full answers produce complete document`() {
        val answers = PhysiognomyCatalog.steps.flatMap { step ->
            step.questions.map { question -> question.id to question.options.first().id }
        }.toMap()

        val text = ResultComposer.buildText(answers)

        assertEquals(PhysiognomyCatalog.totalQuestions, ResultComposer.answered(answers).size)
        assertTrue(ResultComposer.missing(answers).isEmpty())
        assertTrue(text.contains("ФИЗИОГНОМИЧЕСКИЙ ПОРТРЕТ"))
        assertTrue(text.contains("ОБЩИЙ ВЫВОД"))
        assertTrue(text.contains("Отмечено ${PhysiognomyCatalog.totalQuestions}"))
    }

    @Test
    fun `text includes profile name when provided`() {
        val step = PhysiognomyCatalog.step(1)
        val question = step.questions.first()
        val answers = mapOf(question.id to question.options.first().id)
        val text = ResultComposer.buildText(answers, name = "Иван")

        assertTrue(text.contains("Иван"))
    }

    @Test
    fun `title is stable for same answers`() {
        val answers = PhysiognomyCatalog.steps.take(3).flatMap { step ->
            step.questions.map { question -> question.id to question.options.first().id }
        }.toMap()

        assertEquals(ResultComposer.profileTitle(answers), ResultComposer.profileTitle(answers))
    }
}
