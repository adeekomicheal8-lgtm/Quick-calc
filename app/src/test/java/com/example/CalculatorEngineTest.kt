package com.example

import com.example.domain.AngleUnit
import com.example.domain.CalculatorEngine
import com.example.domain.EvaluationResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculatorEngineTest {

  private val engine = CalculatorEngine()

  @Test
  fun `exact decimal addition avoids floating point drift`() {
    val res = engine.evaluate("0.1+0.2", AngleUnit.DEG)
    assertEquals(EvaluationResult.Success("0.3", "0.3"), res)
  }

  @Test
  fun `operator precedence and grouping commas`() {
    val res = engine.evaluate("1000+250×4", AngleUnit.DEG)
    assertEquals(EvaluationResult.Success("2,000", "2000"), res)
  }

  @Test
  fun `implicit multiplication with parentheses and constants`() {
    val res = engine.evaluate("2(3+4)", AngleUnit.DEG)
    assertEquals(EvaluationResult.Success("14", "14"), res)
  }

  @Test
  fun `percentage and factorial evaluation`() {
    assertEquals(
      EvaluationResult.Success("30", "30"),
      engine.evaluate("200×15%", AngleUnit.DEG),
    )
    assertEquals(
      EvaluationResult.Success("120", "120"),
      engine.evaluate("5!", AngleUnit.DEG),
    )
  }

  @Test
  fun `scientific trig functions in degrees and radians`() {
    assertEquals(
      EvaluationResult.Success("0.5", "0.5"),
      engine.evaluate("sin(30)", AngleUnit.DEG),
    )
    assertEquals(
      EvaluationResult.Success("0", "0"),
      engine.evaluate("cos(90)", AngleUnit.DEG),
    )
    assertEquals(
      EvaluationResult.Error("Undefined"),
      engine.evaluate("tan(90)", AngleUnit.DEG),
    )
    assertEquals(
      EvaluationResult.Success("1", "1"),
      engine.evaluate("sin(π÷2)", AngleUnit.RAD),
    )
  }

  @Test
  fun `logarithms square root and right associative power`() {
    assertEquals(
      EvaluationResult.Success("1", "1"),
      engine.evaluate("ln(e)", AngleUnit.DEG),
    )
    assertEquals(
      EvaluationResult.Success("2", "2"),
      engine.evaluate("log(100)", AngleUnit.DEG),
    )
    assertEquals(
      EvaluationResult.Success("12", "12"),
      engine.evaluate("√(144)", AngleUnit.DEG),
    )
    assertEquals(
      EvaluationResult.Success("512", "512"),
      engine.evaluate("2^3^2", AngleUnit.DEG),
    )
  }

  @Test
  fun `division by zero and domain errors`() {
    val divZero = engine.evaluate("8÷0", AngleUnit.DEG)
    assertTrue(divZero is EvaluationResult.Error)
    assertEquals("Can't divide by zero", (divZero as EvaluationResult.Error).message)

    val sqrtNeg = engine.evaluate("√(−4)", AngleUnit.DEG)
    assertTrue(sqrtNeg is EvaluationResult.Error)
    assertEquals("Domain error", (sqrtNeg as EvaluationResult.Error).message)
  }

  @Test
  fun `live preview ignores single number or trailing operator`() {
    assertEquals(EvaluationResult.Empty, engine.evaluatePreview("42", AngleUnit.DEG))
    assertEquals(EvaluationResult.Empty, engine.evaluatePreview("42+", AngleUnit.DEG))
    assertEquals(
      EvaluationResult.Success("50", "50"),
      engine.evaluatePreview("42+8", AngleUnit.DEG),
    )
  }
}
