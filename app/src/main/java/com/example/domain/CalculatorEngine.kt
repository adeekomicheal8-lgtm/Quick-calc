package com.example.domain

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.roundToLong
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

enum class AngleUnit {
  DEG,
  RAD,
}

sealed interface EvaluationResult {
  data class Success(val formatted: String, val raw: String) : EvaluationResult

  data class Error(val message: String) : EvaluationResult

  data object Empty : EvaluationResult
}

class CalculatorEngine {

  private sealed interface Token {
    data class Number(val text: String) : Token

    data class Constant(val symbol: String) : Token

    data class Operator(val symbol: Char) : Token

    data class Function(val name: String) : Token

    data object LeftParen : Token

    data object RightParen : Token

    data object Percent : Token

    data object Factorial : Token
  }

  private sealed interface CalcValue {
    fun toDouble(): Double

    data class Exact(val value: BigDecimal) : CalcValue {
      override fun toDouble(): Double = value.toDouble()
    }

    data class Approx(val value: Double) : CalcValue {
      override fun toDouble(): Double = value
    }
  }

  private class CalcException(message: String) : RuntimeException(message)

  fun evaluate(expression: String, angleUnit: AngleUnit): EvaluationResult {
    val trimmed = expression.trim()
    if (trimmed.isEmpty()) return EvaluationResult.Empty

    return try {
      val tokens = tokenize(trimmed, autoCloseParens = true)
      if (tokens.isEmpty()) return EvaluationResult.Empty
      val value = Parser(tokens, angleUnit).parseExpression()
      formatCalcValue(value)
    } catch (e: CalcException) {
      EvaluationResult.Error(e.message ?: "Error")
    } catch (_: Exception) {
      EvaluationResult.Error("Syntax error")
    }
  }

  fun evaluatePreview(expression: String, angleUnit: AngleUnit): EvaluationResult {
    val trimmed = expression.trim()
    if (trimmed.isEmpty()) return EvaluationResult.Empty

    // Don't preview if expression ends with an incomplete binary operator or open paren
    val lastChar = trimmed.last()
    if (lastChar in listOf('+', '-', '−', '×', '*', '÷', '/', '^', '(')) {
      return EvaluationResult.Empty
    }

    return try {
      val tokens = tokenize(trimmed, autoCloseParens = true)
      if (tokens.isEmpty()) return EvaluationResult.Empty

      // Don't show redundant preview if it's just a single literal number
      if (tokens.size == 1 && tokens.first() is Token.Number) {
        return EvaluationResult.Empty
      }
      if (
        tokens.size == 2 &&
          tokens[0] == Token.Operator('-') &&
          tokens[1] is Token.Number
      ) {
        return EvaluationResult.Empty
      }

      val value = Parser(tokens, angleUnit).parseExpression()
      formatCalcValue(value)
    } catch (e: CalcException) {
      // Only surface clear math domain errors like divide by zero in preview if helpful,
      // otherwise stay silent while typing.
      val msg = e.message
      if (msg == "Can't divide by zero") {
        EvaluationResult.Error(msg)
      } else {
        EvaluationResult.Empty
      }
    } catch (_: Exception) {
      EvaluationResult.Empty
    }
  }

  private fun tokenize(input: String, autoCloseParens: Boolean): List<Token> {
    val rawTokens = mutableListOf<Token>()
    var i = 0
    val n = input.length

    while (i < n) {
      val ch = input[i]
      when {
        ch.isWhitespace() || ch == ',' -> {
          i++
        }
        ch.isDigit() || ch == '.' -> {
          val start = i
          var hasDot = ch == '.'
          i++
          while (i < n && (input[i].isDigit() || (input[i] == '.' && !hasDot))) {
            if (input[i] == '.') hasDot = true
            i++
          }
          // Support scientific notation like 1.5E10 or 1.5E-5
          if (i < n && input[i] == 'E') {
            val expStart = i
            var j = i + 1
            if (j < n && (input[j] == '+' || input[j] == '-' || input[j] == '−')) {
              j++
            }
            val digitStart = j
            while (j < n && input[j].isDigit()) {
              j++
            }
            if (j > digitStart) {
              i = j
            } else {
              i = expStart
            }
          }
          val numStr = input.substring(start, i).replace('−', '-')
          if (numStr == "." || numStr == "-.") throw CalcException("Syntax error")
          rawTokens.add(Token.Number(numStr))
        }
        ch == 'π' -> {
          rawTokens.add(Token.Constant("π"))
          i++
        }
        ch == 'e' -> {
          rawTokens.add(Token.Constant("e"))
          i++
        }
        ch == '√' -> {
          rawTokens.add(Token.Function("sqrt"))
          i++
        }
        ch == '+' -> {
          rawTokens.add(Token.Operator('+'))
          i++
        }
        ch == '-' || ch == '−' -> {
          rawTokens.add(Token.Operator('-'))
          i++
        }
        ch == '×' || ch == '*' -> {
          rawTokens.add(Token.Operator('*'))
          i++
        }
        ch == '÷' || ch == '/' -> {
          rawTokens.add(Token.Operator('/'))
          i++
        }
        ch == '^' -> {
          rawTokens.add(Token.Operator('^'))
          i++
        }
        ch == '%' -> {
          rawTokens.add(Token.Percent)
          i++
        }
        ch == '!' -> {
          rawTokens.add(Token.Factorial)
          i++
        }
        ch == '(' -> {
          rawTokens.add(Token.LeftParen)
          i++
        }
        ch == ')' -> {
          rawTokens.add(Token.RightParen)
          i++
        }
        else -> {
          // Match named functions
          val remaining = input.substring(i)
          val matchedFunc =
            FUNCTION_NAMES.firstOrNull { fn -> remaining.startsWith(fn) }
              ?: throw CalcException("Syntax error")
          rawTokens.add(Token.Function(normalizeFuncName(matchedFunc)))
          i += matchedFunc.length
        }
      }
    }

    // Insert implicit multiplication where appropriate
    val tokens = mutableListOf<Token>()
    for (token in rawTokens) {
      val prev = tokens.lastOrNull()
      if (prev != null && needsImplicitMultiply(prev, token)) {
        tokens.add(Token.Operator('*'))
      }
      tokens.add(token)
    }

    if (autoCloseParens) {
      val openCount = tokens.count { it == Token.LeftParen }
      val closeCount = tokens.count { it == Token.RightParen }
      repeat((openCount - closeCount).coerceAtLeast(0)) {
        tokens.add(Token.RightParen)
      }
    }

    return tokens
  }

  private fun needsImplicitMultiply(prev: Token, curr: Token): Boolean {
    val prevCanMultiply =
      prev is Token.Number ||
        prev is Token.Constant ||
        prev == Token.RightParen ||
        prev == Token.Percent ||
        prev == Token.Factorial
    val currCanMultiply =
      curr is Token.Number ||
        curr is Token.Constant ||
        curr == Token.LeftParen ||
        curr is Token.Function
    return prevCanMultiply && currCanMultiply
  }

  private fun normalizeFuncName(name: String): String =
    when (name) {
      "sin⁻¹" -> "asin"
      "cos⁻¹" -> "acos"
      "tan⁻¹" -> "atan"
      else -> name
    }

  private inner class Parser(
    private val tokens: List<Token>,
    private val angleUnit: AngleUnit,
  ) {
    private var pos = 0

    fun parseExpression(): CalcValue {
      val result = parseAddSub()
      if (pos < tokens.size) {
        throw CalcException("Syntax error")
      }
      return result
    }

    private fun peek(): Token? = tokens.getOrNull(pos)

    private fun consume(): Token = tokens[pos++]

    private fun parseAddSub(): CalcValue {
      var left = parseMulDiv()
      while (true) {
        val op = (peek() as? Token.Operator)?.symbol
        if (op == '+' || op == '-') {
          consume()
          val right = parseMulDiv()
          left = if (op == '+') add(left, right) else subtract(left, right)
        } else {
          break
        }
      }
      return left
    }

    private fun parseMulDiv(): CalcValue {
      var left = parseUnary()
      while (true) {
        val op = (peek() as? Token.Operator)?.symbol
        if (op == '*' || op == '/') {
          consume()
          val right = parseUnary()
          left = if (op == '*') multiply(left, right) else divide(left, right)
        } else {
          break
        }
      }
      return left
    }

    private fun parseUnary(): CalcValue {
      val op = (peek() as? Token.Operator)?.symbol
      return if (op == '+' || op == '-') {
        consume()
        val operand = parseUnary()
        if (op == '-') negate(operand) else operand
      } else {
        parsePower()
      }
    }

    // Exponentiation is right-associative: 2^3^2 = 2^(3^2) = 512
    private fun parsePower(): CalcValue {
      val base = parsePostfix()
      val op = (peek() as? Token.Operator)?.symbol
      return if (op == '^') {
        consume()
        val exp = parseUnary()
        power(base, exp)
      } else {
        base
      }
    }

    private fun parsePostfix(): CalcValue {
      var value = parsePrimary()
      while (true) {
        when (peek()) {
          Token.Percent -> {
            consume()
            value = divide(value, CalcValue.Exact(BigDecimal("100")))
          }
          Token.Factorial -> {
            consume()
            value = factorial(value)
          }
          else -> break
        }
      }
      return value
    }

    private fun parsePrimary(): CalcValue {
      val token = peek() ?: throw CalcException("Syntax error")
      return when (token) {
        is Token.Number -> {
          consume()
          val bd =
            token.text.toBigDecimalOrNull() ?: throw CalcException("Invalid number")
          CalcValue.Exact(bd)
        }
        is Token.Constant -> {
          consume()
          when (token.symbol) {
            "π" -> CalcValue.Approx(PI)
            "e" -> CalcValue.Approx(Math.E)
            else -> throw CalcException("Syntax error")
          }
        }
        Token.LeftParen -> {
          consume()
          val inner = parseAddSub()
          if (peek() != Token.RightParen) {
            throw CalcException("Syntax error")
          }
          consume()
          inner
        }
        is Token.Function -> {
          consume()
          val arg =
            if (peek() == Token.LeftParen) {
              consume()
              val inner = parseAddSub()
              if (peek() != Token.RightParen) {
                throw CalcException("Syntax error")
              }
              consume()
              inner
            } else {
              // Allow shorthand like √9 or sin30 if typed without parens
              parseUnary()
            }
          applyFunction(token.name, arg)
        }
        else -> throw CalcException("Syntax error")
      }
    }

    private fun add(a: CalcValue, b: CalcValue): CalcValue =
      if (a is CalcValue.Exact && b is CalcValue.Exact) {
        CalcValue.Exact(a.value.add(b.value, MATH_CONTEXT))
      } else {
        CalcValue.Approx(a.toDouble() + b.toDouble())
      }

    private fun subtract(a: CalcValue, b: CalcValue): CalcValue =
      if (a is CalcValue.Exact && b is CalcValue.Exact) {
        CalcValue.Exact(a.value.subtract(b.value, MATH_CONTEXT))
      } else {
        CalcValue.Approx(a.toDouble() - b.toDouble())
      }

    private fun multiply(a: CalcValue, b: CalcValue): CalcValue =
      if (a is CalcValue.Exact && b is CalcValue.Exact) {
        CalcValue.Exact(a.value.multiply(b.value, MATH_CONTEXT))
      } else {
        CalcValue.Approx(a.toDouble() * b.toDouble())
      }

    private fun divide(a: CalcValue, b: CalcValue): CalcValue {
      if (a is CalcValue.Exact && b is CalcValue.Exact) {
        if (b.value.compareTo(BigDecimal.ZERO) == 0) {
          throw CalcException("Can't divide by zero")
        }
        return CalcValue.Exact(a.value.divide(b.value, MATH_CONTEXT))
      }
      val denom = b.toDouble()
      if (denom == 0.0) throw CalcException("Can't divide by zero")
      return CalcValue.Approx(a.toDouble() / denom)
    }

    private fun negate(a: CalcValue): CalcValue =
      when (a) {
        is CalcValue.Exact -> CalcValue.Exact(a.value.negate())
        is CalcValue.Approx -> CalcValue.Approx(-a.value)
      }

    private fun power(base: CalcValue, exp: CalcValue): CalcValue {
      if (base is CalcValue.Exact && exp is CalcValue.Exact) {
        val expStripped = exp.value.stripTrailingZeros()
        if (expStripped.scale() <= 0) {
          val intExp =
            try {
              expStripped.intValueExact()
            } catch (_: ArithmeticException) {
              null
            }
          if (intExp != null && abs(intExp) <= 500) {
            if (intExp < 0 && base.value.compareTo(BigDecimal.ZERO) == 0) {
              throw CalcException("Can't divide by zero")
            }
            return CalcValue.Exact(base.value.pow(intExp, MATH_CONTEXT))
          }
        }
      }
      val b = base.toDouble()
      val e = exp.toDouble()
      val res = b.pow(e)
      if (res.isNaN()) throw CalcException("Domain error")
      if (res.isInfinite()) throw CalcException("Overflow")
      return CalcValue.Approx(res)
    }

    private fun factorial(v: CalcValue): CalcValue {
      val d = v.toDouble()
      if (d < 0 || d.isNaN() || d.isInfinite()) throw CalcException("Domain error")
      val rounded = d.roundToLong()
      if (abs(d - rounded.toDouble()) > 1e-10) throw CalcException("Domain error")
      if (rounded > 170) throw CalcException("Overflow")
      var acc = 1.0
      for (k in 2..rounded.toInt()) {
        acc *= k.toDouble()
      }
      return CalcValue.Approx(acc)
    }

    private fun applyFunction(name: String, arg: CalcValue): CalcValue {
      val x = arg.toDouble()
      val res =
        when (name) {
          "sin" -> {
            if (angleUnit == AngleUnit.DEG) {
              val norm = ((x % 360.0) + 360.0) % 360.0
              if (abs(norm - 0.0) < 1e-12 || abs(norm - 180.0) < 1e-12 || abs(norm - 360.0) < 1e-12) {
                0.0
              } else {
                sin(Math.toRadians(x))
              }
            } else {
              sin(x)
            }
          }
          "cos" -> {
            if (angleUnit == AngleUnit.DEG) {
              val norm = ((x % 360.0) + 360.0) % 360.0
              if (abs(norm - 90.0) < 1e-12 || abs(norm - 270.0) < 1e-12) {
                0.0
              } else {
                cos(Math.toRadians(x))
              }
            } else {
              cos(x)
            }
          }
          "tan" -> {
            if (angleUnit == AngleUnit.DEG) {
              val norm = ((x % 180.0) + 180.0) % 180.0
              if (abs(norm - 90.0) < 1e-11) throw CalcException("Undefined")
              if (abs(norm - 0.0) < 1e-12 || abs(norm - 180.0) < 1e-12) {
                0.0
              } else {
                tan(Math.toRadians(x))
              }
            } else {
              val c = cos(x)
              if (abs(c) < 1e-12) throw CalcException("Undefined")
              tan(x)
            }
          }
          "asin" -> {
            val clamped = clampTrigDomain(x)
            val rad = asin(clamped)
            if (angleUnit == AngleUnit.DEG) Math.toDegrees(rad) else rad
          }
          "acos" -> {
            val clamped = clampTrigDomain(x)
            val rad = acos(clamped)
            if (angleUnit == AngleUnit.DEG) Math.toDegrees(rad) else rad
          }
          "atan" -> {
            val rad = atan(x)
            if (angleUnit == AngleUnit.DEG) Math.toDegrees(rad) else rad
          }
          "ln" -> {
            if (x <= 0.0) throw CalcException("Domain error")
            ln(x)
          }
          "log" -> {
            if (x <= 0.0) throw CalcException("Domain error")
            log10(x)
          }
          "sqrt" -> {
            if (x < 0.0) throw CalcException("Domain error")
            sqrt(x)
          }
          "abs" -> abs(x)
          else -> throw CalcException("Unknown function")
        }
      if (res.isNaN()) throw CalcException("Domain error")
      if (res.isInfinite()) throw CalcException("Overflow")
      return CalcValue.Approx(res)
    }

    private fun clampTrigDomain(x: Double): Double {
      if (x in -1.0..1.0) return x
      if (abs(x - 1.0) < 1e-12) return 1.0
      if (abs(x + 1.0) < 1e-12) return -1.0
      throw CalcException("Domain error")
    }
  }

  private fun formatCalcValue(calcValue: CalcValue): EvaluationResult {
    val bd =
      when (calcValue) {
        is CalcValue.Exact -> calcValue.value
        is CalcValue.Approx -> {
          val d = calcValue.value
          if (d.isNaN()) return EvaluationResult.Error("Domain error")
          if (d.isInfinite()) return EvaluationResult.Error("Overflow")
          val snapped = if (abs(d) < 1e-12) 0.0 else d
          BigDecimal(snapped, DISPLAY_APPROX_CONTEXT)
        }
      }

    // Round very noisy repeating exact fractions (scale > 12) cleanly to 12 significant digits
    val cleanBd =
      if (calcValue is CalcValue.Exact && bd.scale() > 12) {
        bd.round(DISPLAY_EXACT_CONTEXT)
      } else {
        bd
      }

    val normalized =
      if (cleanBd.compareTo(BigDecimal.ZERO) == 0) {
        BigDecimal.ZERO
      } else {
        cleanBd.stripTrailingZeros()
      }

    val absVal = normalized.abs()
    val useScientific =
      absVal.compareTo(BigDecimal.ZERO) != 0 &&
        (absVal >= SCIENTIFIC_UPPER || absVal < SCIENTIFIC_LOWER)

    val raw =
      if (useScientific) {
        normalized.round(SCIENTIFIC_CONTEXT).toString().replace("+", "")
      } else {
        normalized.toPlainString()
      }

    val formatted = if (useScientific) raw else addGroupingCommas(raw)
    return EvaluationResult.Success(formatted = formatted, raw = raw)
  }

  private fun addGroupingCommas(plain: String): String {
    val negative = plain.startsWith("-")
    val unsigned = if (negative) plain.substring(1) else plain
    val dotIndex = unsigned.indexOf('.')
    val intPart = if (dotIndex >= 0) unsigned.substring(0, dotIndex) else unsigned
    val fracPart = if (dotIndex >= 0) unsigned.substring(dotIndex) else ""

    if (intPart.length <= 3) {
      return if (negative) "−$unsigned" else unsigned
    }

    val grouped = StringBuilder()
    var count = 0
    for (idx in intPart.length - 1 downTo 0) {
      grouped.append(intPart[idx])
      count++
      if (count % 3 == 0 && idx > 0) {
        grouped.append(',')
      }
    }
    grouped.reverse()
    val prefix = if (negative) "−" else ""
    return "$prefix$grouped$fracPart"
  }

  companion object {
    private val MATH_CONTEXT = MathContext(20, RoundingMode.HALF_UP)
    private val DISPLAY_EXACT_CONTEXT = MathContext(12, RoundingMode.HALF_UP)
    private val DISPLAY_APPROX_CONTEXT = MathContext(12, RoundingMode.HALF_UP)
    private val SCIENTIFIC_CONTEXT = MathContext(10, RoundingMode.HALF_UP)
    private val SCIENTIFIC_UPPER = BigDecimal("1000000000000") // 1e12
    private val SCIENTIFIC_LOWER = BigDecimal("0.00000001") // 1e-8

    private val FUNCTION_NAMES =
      listOf("sin⁻¹", "cos⁻¹", "tan⁻¹", "asin", "acos", "atan", "sqrt", "sin", "cos", "tan", "log", "abs", "ln")
  }
}
