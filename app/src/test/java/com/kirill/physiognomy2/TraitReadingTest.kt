package com.kirill.physiognomy2

import com.kirill.physiognomy2.data.PhysiognomyCatalog
import com.kirill.physiognomy2.data.ReadingArea
import com.kirill.physiognomy2.data.TraitReading
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TraitReadingTest {

    @Test
    fun `every step has reading terms`() {
        PhysiognomyCatalog.steps.forEach { step ->
            val terms = TraitReading.termsFor(step.number)
            assertTrue("Шаг ${step.number} без описания", terms.isNotEmpty())
            terms.forEach { term ->
                assertTrue("Пустой термин в шаге ${step.number}", term.isNotBlank())
            }
        }
    }

    @Test
    fun `step terms cover exactly the catalog steps`() {
        val catalogSteps = PhysiognomyCatalog.steps.map { it.number }.toSet()
        assertEquals(catalogSteps, TraitReading.stepTerms.keys)
    }

    @Test
    fun `areas cover every step exactly once`() {
        val covered = ReadingArea.entries.flatMap { it.stepNumbers }
        assertEquals(covered.size, covered.distinct().size)
        assertEquals(PhysiognomyCatalog.steps.map { it.number }.sorted(), covered.sorted())
    }

    @Test
    fun `every step belongs to an area`() {
        PhysiognomyCatalog.steps.forEach { step ->
            assertNotNull(
                "Шаг ${step.number} не входит ни в одну смысловую группу",
                TraitReading.areaFor(step.number),
            )
        }
    }

    @Test
    fun `question overrides reference existing questions`() {
        TraitReading.questionTerms.keys.forEach { questionId ->
            assertNotNull(
                "Вопрос $questionId отсутствует в каталоге",
                PhysiognomyCatalog.question(questionId),
            )
        }
    }

    @Test
    fun `question override belongs to same step`() {
        TraitReading.questionTerms.forEach { (questionId, terms) ->
            val step = PhysiognomyCatalog.stepOf(questionId)
            assertNotNull("Вопрос $questionId не привязан к шагу", step)
            assertTrue("Пустое переопределение для $questionId", terms.isNotEmpty())
        }
    }

    @Test
    fun `verification marks reference existing questions`() {
        TraitReading.unverifiedQuestions.forEach { questionId ->
            assertNotNull("Непроверенный вопрос $questionId отсутствует", PhysiognomyCatalog.question(questionId))
        }
    }

    @Test
    fun `reading joins terms and short form truncates`() {
        val full = TraitReading.readingFor(3)
        assertTrue(full.contains("мировоззрение"))
        assertTrue(full.contains(", "))

        val short = TraitReading.shortReadingFor(3, limit = 2)
        assertTrue(short.endsWith("…"))
        assertEquals(2, short.removeSuffix("…").split(", ").size)
    }

    @Test
    fun `question override wins over step terms`() {
        val nostrils = TraitReading.termsFor(7, "s7q5")
        assertEquals(listOf("щедрость", "уровень энергии"), nostrils)
        assertTrue(TraitReading.readingFor(7).contains("стиль работы"))
    }

    @Test
    fun `type and position questions have own values`() {
        assertEquals(listOf("властность", "оптимизм", "бесстрашие", "осторожность"), TraitReading.termsFor(26, "s26q1"))
        assertTrue(TraitReading.termsFor(2, "s2q2").isNotEmpty())
        assertTrue(TraitReading.termsFor(8, "s8q3").isNotEmpty())
        assertTrue(TraitReading.termsFor(23, "s23q1").isNotEmpty())
    }

    @Test
    fun `unknown question falls back to step terms`() {
        assertEquals(TraitReading.termsFor(7), TraitReading.termsFor(7, "s7q99"))
    }
}
