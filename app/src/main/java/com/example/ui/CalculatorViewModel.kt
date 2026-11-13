package com.example.ui

import androidx.lifecycle.ViewModel
import com.example.domain.AngleUnit
import com.example.domain.CalculatorEngine
import com.example.domain.EvaluationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class ThemePreference {
  SYSTEM,
  LIGHT,
  DARK,
}

data class CalculationHistoryItem(
  val id: Long,
  val expression: String,
  val formattedResult: String,
  val rawResult: String,
  val angleUnit: AngleUnit,
)

data class CalculatorUiState(
  val expression: String = "",
  val completedExpression: String? = null,
  val livePreview: String = "",
  val resultText: String = "",
  val rawResult: String = "",
  val justEvaluated: Boolean = false,
  val errorMessage: String? = null,
  val isScientificMode: Boolean = false,
  val isInverseMode: Boolean = false,
  val angleUnit: AngleUnit = AngleUnit.DEG,
  val history: List<CalculationHistoryItem> = emptyList(),
  val themePreference: ThemePreference = ThemePreference.SYSTEM,
)

class CalculatorViewModel(
  private val engine: CalculatorEngine = CalculatorEngine(),
) : ViewModel() {

  private val _uiState = MutableStateFlow(CalculatorUiState())
  val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

  private var nextHistoryId = 1L

  fun onDigit(digit: String) {
    _uiState.update { state ->
      val base = if (state.justEvaluated) "" else state.expression
      val updated =
        if (base == "0" && digit != "0") {
          digit
        } else if (base == "0" && digit == "0") {
          "0"
        } else {
          base + digit
        }
      recomputeState(
        state.copy(
          expression = updated,
          completedExpression = null,
          justEvaluated = false,
          errorMessage = null,
        )
      )
    }
  }

  fun onDecimal() {
    _uiState.update { state ->
      val base = if (state.justEvaluated) "" else state.expression
      val currentNumberSegment = base.takeLastWhile { it.isDigit() || it == '.' }
      if (currentNumberSegment.contains('.')) {
        return@update state
      }
      val needsLeadingZero =
        base.isEmpty() || (!base.last().isDigit() && base.last() != '.')
      val updated = if (needsLeadingZero) "${base}0." else "$base."
      recomputeState(
        state.copy(
          expression = updated,
          completedExpression = null,
          justEvaluated = false,
          errorMessage = null,
        )
      )
    }
  }

  fun onOperator(operatorSymbol: String) {
    _uiState.update { state ->
      val base =
        if (state.justEvaluated) {
          state.rawResult.ifEmpty { state.expression }
        } else {
          state.expression
        }

      if (base.isEmpty()) {
        // Only allow unary minus on empty expression
        if (operatorSymbol == "−") {
          return@update recomputeState(
            state.copy(
              expression = "−",
              completedExpression = null,
              justEvaluated = false,
              errorMessage = null,
            )
          )
        }
        return@update state
      }

      val last = base.last()
      val updated =
        when {
          last == '(' -> {
            if (operatorSymbol == "−") "$base−" else base
          }
          last in BINARY_OPERATORS -> {
            // Allow negative exponent or negative second operand like 5×−2
            if (operatorSymbol == "−" && (last == '×' || last == '÷' || last == '^')) {
              "$base−"
            } else {
              // Avoid replacing a unary minus right after another operator/paren
              val prev = base.dropLast(1).lastOrNull()
              if (last == '−' && (prev == null || prev == '(' || prev in BINARY_OPERATORS)) {
                if (operatorSymbol == "−") base else base
              } else {
                base.dropLast(1) + operatorSymbol
              }
            }
          }
          else -> base + operatorSymbol
        }

      recomputeState(
        state.copy(
          expression = updated,
          completedExpression = null,
          justEvaluated = false,
          errorMessage = null,
        )
      )
    }
  }

  fun onFunction(functionToken: String) {
    _uiState.update { state ->
      val base = if (state.justEvaluated) "" else state.expression
      val updated = base + functionToken
      recomputeState(
        state.copy(
          expression = updated,
          completedExpression = null,
          justEvaluated = false,
          errorMessage = null,
        )
      )
    }
  }

  fun onConstant(constant: String) {
    _uiState.update { state ->
      val base = if (state.justEvaluated) "" else state.expression
      recomputeState(
        state.copy(
          expression = base + constant,
          completedExpression = null,
          justEvaluated = false,
          errorMessage = null,
        )
      )
    }
  }

  fun onSmartParenthesis() {
    _uiState.update { state ->
      val base = if (state.justEvaluated) "" else state.expression
      val openCount = base.count { it == '(' }
      val closeCount = base.count { it == ')' }
      val last = base.lastOrNull()
      val canClose =
        openCount > closeCount &&
          last != null &&
          (last.isDigit() || last == ')' || last == 'π' || last == 'e' || last == '%' || last == '!')
      val nextChar = if (canClose) ")" else "("
      recomputeState(
        state.copy(
          expression = base + nextChar,
          completedExpression = null,
          justEvaluated = false,
          errorMessage = null,
        )
      )
    }
  }

  fun onPercent() {
    _uiState.update { state ->
      val base =
        if (state.justEvaluated) {
          state.rawResult.ifEmpty { state.expression }
        } else {
          state.expression
        }
      val last = base.lastOrNull() ?: return@update state
      if (last.isDigit() || last == ')' || last == 'π' || last == 'e' || last == '!') {
        recomputeState(
          state.copy(
            expression = "$base%",
            completedExpression = null,
            justEvaluated = false,
            errorMessage = null,
          )
        )
      } else {
        state
      }
    }
  }

  fun onFactorial() {
    _uiState.update { state ->
      val base =
        if (state.justEvaluated) {
          state.rawResult.ifEmpty { state.expression }
        } else {
          state.expression
        }
      val last = base.lastOrNull() ?: return@update state
      if (last.isDigit() || last == ')' || last == 'π' || last == 'e') {
        recomputeState(
          state.copy(
            expression = "$base!",
            completedExpression = null,
            justEvaluated = false,
            errorMessage = null,
          )
        )
      } else {
        state
      }
    }
  }

  fun onToggleSign() {
    _uiState.update { state ->
      val base =
        if (state.justEvaluated) {
          state.rawResult.ifEmpty { state.expression }
        } else {
          state.expression
        }

      val updated =
        when {
          base.isEmpty() -> "(−"
          base.endsWith("(−") -> base.dropLast(2)
          base == "−" -> ""
          else -> {
            val last = base.last()
            if (last in BINARY_OPERATORS || last == '(') {
              "$base(−"
            } else {
              // Find trailing number/constant span
              var idx = base.length - 1
              while (idx >= 0 && (base[idx].isDigit() || base[idx] == '.' || base[idx] == 'E')) {
                idx--
              }
              val prefix = base.substring(0, idx + 1)
              val numPart = base.substring(idx + 1)
              if (numPart.isEmpty()) {
                "$base(−"
              } else if (prefix.endsWith("(−")) {
                prefix.dropLast(2) + numPart
              } else if (prefix == "−") {
                numPart
              } else {
                "$prefix(−$numPart"
              }
            }
          }
        }

      recomputeState(
        state.copy(
          expression = updated,
          completedExpression = null,
          justEvaluated = false,
          errorMessage = null,
        )
      )
    }
  }

  fun onBackspace() {
    _uiState.update { state ->
      if (state.expression.isEmpty()) return@update state
      val expr =
        if (state.justEvaluated && state.completedExpression != null) {
          // Allow stepping back into the expression just evaluated if desired, or editing raw result
          state.rawResult.ifEmpty { state.expression }
        } else {
          state.expression
        }
      val matchedToken = MULTI_CHAR_TOKENS.firstOrNull { expr.endsWith(it) }
      val updated =
        if (matchedToken != null) {
          expr.dropLast(matchedToken.length)
        } else {
          expr.dropLast(1)
        }
      recomputeState(
        state.copy(
          expression = updated,
          completedExpression = null,
          justEvaluated = false,
          errorMessage = null,
        )
      )
    }
  }

  fun onClear() {
    _uiState.update { state ->
      state.copy(
        expression = "",
        completedExpression = null,
        livePreview = "",
        resultText = "",
        rawResult = "",
        justEvaluated = false,
        errorMessage = null,
      )
    }
  }

  fun onEquals() {
    _uiState.update { state ->
      if (state.expression.isBlank()) return@update state
      when (val eval = engine.evaluate(state.expression, state.angleUnit)) {
        is EvaluationResult.Success -> {
          val openCount = state.expression.count { it == '(' }
          val closeCount = state.expression.count { it == ')' }
          val balancedExpression =
            if (openCount > closeCount) {
              state.expression + ")".repeat(openCount - closeCount)
            } else {
              state.expression
            }
          val normalizedRaw = eval.raw.replace('-', '−')
          val historyEntry =
            CalculationHistoryItem(
              id = nextHistoryId++,
              expression = balancedExpression,
              formattedResult = eval.formatted,
              rawResult = normalizedRaw,
              angleUnit = state.angleUnit,
            )
          // Avoid duplicate consecutive identical history items
          val updatedHistory =
            if (
              state.history.firstOrNull()?.expression == historyEntry.expression &&
                state.history.firstOrNull()?.formattedResult == historyEntry.formattedResult
            ) {
              state.history
            } else {
              listOf(historyEntry) + state.history
            }
          state.copy(
            expression = eval.formatted,
            completedExpression = balancedExpression,
            livePreview = "",
            resultText = eval.formatted,
            rawResult = normalizedRaw,
            justEvaluated = true,
            errorMessage = null,
            history = updatedHistory,
          )
        }
        is EvaluationResult.Error -> {
          state.copy(errorMessage = eval.message)
        }
        EvaluationResult.Empty -> state
      }
    }
  }

  fun onToggleScientificMode() {
    _uiState.update { it.copy(isScientificMode = !it.isScientificMode) }
  }

  fun onToggleInverseMode() {
    _uiState.update { it.copy(isInverseMode = !it.isInverseMode) }
  }

  fun onToggleAngleUnit() {
    _uiState.update { state ->
      val nextUnit =
        if (state.angleUnit == AngleUnit.DEG) AngleUnit.RAD else AngleUnit.DEG
      recomputeState(state.copy(angleUnit = nextUnit))
    }
  }

  fun onCycleThemePreference() {
    _uiState.update { state ->
      val nextTheme =
        when (state.themePreference) {
          ThemePreference.SYSTEM -> ThemePreference.DARK
          ThemePreference.DARK -> ThemePreference.LIGHT
          ThemePreference.LIGHT -> ThemePreference.SYSTEM
        }
      state.copy(themePreference = nextTheme)
    }
  }

  fun onUseHistoryExpression(item: CalculationHistoryItem) {
    _uiState.update { state ->
      recomputeState(
        state.copy(
          expression = item.expression,
          completedExpression = null,
          justEvaluated = false,
          errorMessage = null,
          angleUnit = item.angleUnit,
        )
      )
    }
  }

  fun onUseHistoryResult(item: CalculationHistoryItem) {
    _uiState.update { state ->
      val base = if (state.justEvaluated) "" else state.expression
      val updated =
        if (base.isEmpty() || base.last() in BINARY_OPERATORS || base.last() == '(') {
          base + item.rawResult
        } else {
          item.rawResult
        }
      recomputeState(
        state.copy(
          expression = updated,
          completedExpression = null,
          justEvaluated = false,
          errorMessage = null,
        )
      )
    }
  }

  fun onClearHistory() {
    _uiState.update { it.copy(history = emptyList()) }
  }

  private fun recomputeState(state: CalculatorUiState): CalculatorUiState {
    if (state.expression.isEmpty()) {
      return state.copy(livePreview = "", errorMessage = null)
    }
    return when (val preview = engine.evaluatePreview(state.expression, state.angleUnit)) {
      is EvaluationResult.Success ->
        state.copy(livePreview = preview.formatted, errorMessage = null)
      is EvaluationResult.Error ->
        state.copy(livePreview = "", errorMessage = preview.message)
      EvaluationResult.Empty ->
        state.copy(livePreview = "", errorMessage = null)
    }
  }

  companion object {
    private val BINARY_OPERATORS = setOf('+', '−', '×', '÷', '^')
    private val MULTI_CHAR_TOKENS =
      listOf("sin⁻¹(", "cos⁻¹(", "tan⁻¹(", "sin(", "cos(", "tan(", "log(", "abs(", "ln(", "√(")
  }
}
