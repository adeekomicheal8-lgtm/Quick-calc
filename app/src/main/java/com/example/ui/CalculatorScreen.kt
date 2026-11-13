package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.domain.AngleUnit
import com.example.ui.components.DisplayPanel
import com.example.ui.components.HistoryPanelContent
import com.example.ui.components.KeyStyle
import com.example.ui.components.KeypadButton
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorRoute(
  viewModel: CalculatorViewModel = viewModel(),
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val clipboardManager = LocalClipboardManager.current
  val snackbarHostState = remember { SnackbarHostState() }
  val coroutineScope = rememberCoroutineScope()
  val copiedLabel = stringResource(R.string.label_copied)

  CalculatorScreen(
    uiState = uiState,
    snackbarHostState = snackbarHostState,
    onDigit = viewModel::onDigit,
    onDecimal = viewModel::onDecimal,
    onOperator = viewModel::onOperator,
    onFunction = viewModel::onFunction,
    onConstant = viewModel::onConstant,
    onSmartParenthesis = viewModel::onSmartParenthesis,
    onPercent = viewModel::onPercent,
    onFactorial = viewModel::onFactorial,
    onToggleSign = viewModel::onToggleSign,
    onBackspace = viewModel::onBackspace,
    onClear = viewModel::onClear,
    onEquals = viewModel::onEquals,
    onToggleScientific = viewModel::onToggleScientificMode,
    onToggleInverse = viewModel::onToggleInverseMode,
    onToggleAngleUnit = viewModel::onToggleAngleUnit,
    onCycleTheme = viewModel::onCycleThemePreference,
    onUseHistoryExpression = viewModel::onUseHistoryExpression,
    onUseHistoryResult = viewModel::onUseHistoryResult,
    onClearHistory = viewModel::onClearHistory,
    onCopyResult = { valueToCopy ->
      clipboardManager.setText(AnnotatedString(valueToCopy))
      coroutineScope.launch {
        snackbarHostState.currentSnackbarData?.dismiss()
        snackbarHostState.showSnackbar(
          message = copiedLabel,
          duration = SnackbarDuration.Short,
        )
      }
    },
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
  uiState: CalculatorUiState,
  snackbarHostState: SnackbarHostState,
  onDigit: (String) -> Unit,
  onDecimal: () -> Unit,
  onOperator: (String) -> Unit,
  onFunction: (String) -> Unit,
  onConstant: (String) -> Unit,
  onSmartParenthesis: () -> Unit,
  onPercent: () -> Unit,
  onFactorial: () -> Unit,
  onToggleSign: () -> Unit,
  onBackspace: () -> Unit,
  onClear: () -> Unit,
  onEquals: () -> Unit,
  onToggleScientific: () -> Unit,
  onToggleInverse: () -> Unit,
  onToggleAngleUnit: () -> Unit,
  onCycleTheme: () -> Unit,
  onUseHistoryExpression: (CalculationHistoryItem) -> Unit,
  onUseHistoryResult: (CalculationHistoryItem) -> Unit,
  onClearHistory: () -> Unit,
  onCopyResult: (String) -> Unit,
) {
  var showHistorySheet by rememberSaveable { mutableStateOf(false) }

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    containerColor = MaterialTheme.colorScheme.background,
    contentWindowInsets = WindowInsets.safeDrawing,
    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
  ) { innerPadding ->
    BoxWithConstraints(
      modifier =
        Modifier
          .fillMaxSize()
          .padding(innerPadding)
          .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
      val isExpandedWidth = maxWidth >= 840.dp
      val isLandscapeCompact = !isExpandedWidth && maxWidth > maxHeight && maxHeight < 560.dp

      when {
        isExpandedWidth -> {
          // Canonical Supporting Pane layout for tablets & unfolded foldables
          Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
          ) {
            Box(
              modifier = Modifier.weight(1.45f).fillMaxHeight(),
              contentAlignment = Alignment.Center,
            ) {
              Column(
                modifier = Modifier.fillMaxSize().widthIn(max = 680.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
              ) {
                DisplayPanel(
                  uiState = uiState,
                  showHistoryButton = false,
                  showScientificToggle = false,
                  isCompactHeight = false,
                  onToggleScientific = onToggleScientific,
                  onToggleAngleUnit = onToggleAngleUnit,
                  onOpenHistory = {},
                  onCycleTheme = onCycleTheme,
                  onCopyResult = onCopyResult,
                  modifier = Modifier.weight(0.34f),
                )

                ScientificKeypadGrid(
                  isInverseMode = uiState.isInverseMode,
                  angleUnit = uiState.angleUnit,
                  onToggleInverse = onToggleInverse,
                  onToggleAngleUnit = onToggleAngleUnit,
                  onConstant = onConstant,
                  onOperator = onOperator,
                  onFunction = onFunction,
                  onFactorial = onFactorial,
                  onToggleSign = onToggleSign,
                  keySpacing = 8.dp,
                  modifier = Modifier.weight(0.22f),
                )

                StandardKeypadGrid(
                  onDigit = onDigit,
                  onDecimal = onDecimal,
                  onOperator = onOperator,
                  onSmartParenthesis = onSmartParenthesis,
                  onPercent = onPercent,
                  onBackspace = onBackspace,
                  onClear = onClear,
                  onEquals = onEquals,
                  keySpacing = 10.dp,
                  modifier = Modifier.weight(0.44f),
                )
              }
            }

            Surface(
              modifier = Modifier.weight(0.95f).fillMaxHeight(),
              shape = RoundedCornerShape(28.dp),
              color = MaterialTheme.colorScheme.surface,
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            ) {
              HistoryPanelContent(
                history = uiState.history,
                onUseExpression = onUseHistoryExpression,
                onUseResult = onUseHistoryResult,
                onCopyResult = onCopyResult,
                onClearHistory = onClearHistory,
              )
            }
          }
        }

        isLandscapeCompact -> {
          // Side-by-side landscape phone layout: Display left, unified Scientific + Standard right
          Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            DisplayPanel(
              uiState = uiState,
              showHistoryButton = true,
              showScientificToggle = false,
              isCompactHeight = true,
              onToggleScientific = onToggleScientific,
              onToggleAngleUnit = onToggleAngleUnit,
              onOpenHistory = { showHistorySheet = true },
              onCycleTheme = onCycleTheme,
              onCopyResult = onCopyResult,
              modifier = Modifier.weight(0.40f).fillMaxHeight(),
            )

            Row(
              modifier = Modifier.weight(0.60f).fillMaxHeight(),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              LandscapeScientificColumn(
                isInverseMode = uiState.isInverseMode,
                onToggleInverse = onToggleInverse,
                onConstant = onConstant,
                onOperator = onOperator,
                onFunction = onFunction,
                onFactorial = onFactorial,
                onToggleSign = onToggleSign,
                modifier = Modifier.weight(0.42f).fillMaxHeight(),
              )
              StandardKeypadGrid(
                onDigit = onDigit,
                onDecimal = onDecimal,
                onOperator = onOperator,
                onSmartParenthesis = onSmartParenthesis,
                onPercent = onPercent,
                onBackspace = onBackspace,
                onClear = onClear,
                onEquals = onEquals,
                keySpacing = 6.dp,
                buttonCornerRadius = 16.dp,
                modifier = Modifier.weight(0.58f).fillMaxHeight(),
              )
            }
          }
        }

        else -> {
          // Standard Portrait / Medium Screen Layout
          Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
          ) {
            Column(
              modifier = Modifier.fillMaxSize().widthIn(max = 600.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              DisplayPanel(
                uiState = uiState,
                showHistoryButton = true,
                showScientificToggle = true,
                isCompactHeight = false,
                onToggleScientific = onToggleScientific,
                onToggleAngleUnit = onToggleAngleUnit,
                onOpenHistory = { showHistorySheet = true },
                onCycleTheme = onCycleTheme,
                onCopyResult = onCopyResult,
                modifier =
                  Modifier.weight(if (uiState.isScientificMode) 0.31f else 0.39f),
              )

              AnimatedVisibility(
                visible = uiState.isScientificMode,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
                modifier = Modifier.weight(0.22f, fill = false),
              ) {
                ScientificKeypadGrid(
                  isInverseMode = uiState.isInverseMode,
                  angleUnit = uiState.angleUnit,
                  onToggleInverse = onToggleInverse,
                  onToggleAngleUnit = onToggleAngleUnit,
                  onConstant = onConstant,
                  onOperator = onOperator,
                  onFunction = onFunction,
                  onFactorial = onFactorial,
                  onToggleSign = onToggleSign,
                  keySpacing = 8.dp,
                  modifier = Modifier.fillMaxWidth().height(172.dp),
                )
              }

              StandardKeypadGrid(
                onDigit = onDigit,
                onDecimal = onDecimal,
                onOperator = onOperator,
                onSmartParenthesis = onSmartParenthesis,
                onPercent = onPercent,
                onBackspace = onBackspace,
                onClear = onClear,
                onEquals = onEquals,
                keySpacing = 10.dp,
                modifier =
                  Modifier.weight(if (uiState.isScientificMode) 0.47f else 0.61f),
              )
            }
          }
        }
      }

      if (showHistorySheet && !isExpandedWidth) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
          onDismissRequest = { showHistorySheet = false },
          sheetState = sheetState,
          containerColor = MaterialTheme.colorScheme.surface,
        ) {
          Box(modifier = Modifier.fillMaxWidth().height(480.dp)) {
            HistoryPanelContent(
              history = uiState.history,
              onUseExpression = { item ->
                onUseHistoryExpression(item)
                showHistorySheet = false
              },
              onUseResult = { item ->
                onUseHistoryResult(item)
                showHistorySheet = false
              },
              onCopyResult = onCopyResult,
              onClearHistory = onClearHistory,
              onClose = { showHistorySheet = false },
            )
          }
        }
      }
    }
  }
}

@Composable
private fun StandardKeypadGrid(
  onDigit: (String) -> Unit,
  onDecimal: () -> Unit,
  onOperator: (String) -> Unit,
  onSmartParenthesis: () -> Unit,
  onPercent: () -> Unit,
  onBackspace: () -> Unit,
  onClear: () -> Unit,
  onEquals: () -> Unit,
  keySpacing: Dp,
  buttonCornerRadius: Dp = 22.dp,
  modifier: Modifier = Modifier,
) {
  val shape = RoundedCornerShape(buttonCornerRadius)
  val backspaceDesc = stringResource(R.string.cd_backspace)

  Column(
    modifier = modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(keySpacing),
  ) {
    // Row 1: AC, ( ), %, ÷
    Row(
      modifier = Modifier.weight(1f).fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(keySpacing),
    ) {
      KeypadButton(
        label = "AC",
        onClick = onClear,
        style = KeyStyle.UTILITY,
        testTag = "key_clear",
        contentDescriptionText = "All clear",
        shape = shape,
        modifier = Modifier.weight(1f).fillMaxHeight(),
      )
      KeypadButton(
        label = "( )",
        onClick = onSmartParenthesis,
        style = KeyStyle.UTILITY,
        testTag = "key_parens",
        contentDescriptionText = "Parentheses",
        shape = shape,
        modifier = Modifier.weight(1f).fillMaxHeight(),
      )
      KeypadButton(
        label = "%",
        onClick = onPercent,
        style = KeyStyle.UTILITY,
        testTag = "key_percent",
        contentDescriptionText = "Percent",
        shape = shape,
        modifier = Modifier.weight(1f).fillMaxHeight(),
      )
      KeypadButton(
        label = "÷",
        onClick = { onOperator("÷") },
        style = KeyStyle.OPERATOR,
        testTag = "key_divide",
        contentDescriptionText = "Divide",
        shape = shape,
        modifier = Modifier.weight(1f).fillMaxHeight(),
      )
    }

    // Row 2: 7, 8, 9, ×
    Row(
      modifier = Modifier.weight(1f).fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(keySpacing),
    ) {
      KeypadButton(
        label = "7",
        onClick = { onDigit("7") },
        testTag = "key_7",
        shape = shape,
        modifier = Modifier.weight(1f).fillMaxHeight(),
      )
      KeypadButton(
        label = "8",
        onClick = { onDigit("8") },
        testTag = "key_8",
        shape = shape,
        modifier = Modifier.weight(1f).fillMaxHeight(),
      )
      KeypadButton(
        label = "9",
        onClick = { onDigit("9") },
        testTag = "key_9",
        shape = shape,
        modifier = Modifier.weight(1f).fillMaxHeight(),
      )
      KeypadButton(
        label = "×",
        onClick = { onOperator("×") },
        style = KeyStyle.OPERATOR,
        testTag = "key_multiply",
        contentDescriptionText = "Multiply",
        shape = shape,
        modifier = Modifier.weight(1f).fillMaxHeight(),
      )
    }

    // Row 3: 4, 5, 6, −
    Row(
      modifier = Modifier.weight(1f).fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(keySpacing),
    ) {
      KeypadButton(
        label = "4",
        onClick = { onDigit("4") },
        testTag = "key_4",
        shape = shape,
        modifier = Modifier.weight(1f).fillMaxHeight(),
      )
      KeypadButton(
        label = "5",
        onClick = { onDigit("5") },
        testTag = "key_5",
        shape = shape,
        modifier = Modifier.weight(1f).fillMaxHeight(),
      )
      KeypadButton(
        label = "6",
        onClick = { onDigit("6") },
        testTag = "key_6",
        shape = shape,
        modifier = Modifier.weight(1f).fillMaxHeight(),
      )
      KeypadButton(
        label = "−",
        onClick = { onOperator("−") },
        style = KeyStyle.OPERATOR,
        testTag = "key_subtract",
        contentDescriptionText = "Subtract",
        shape = shape,
        modifier = Modifier.weight(1f).fillMaxHeight(),
      )
    }

    // Row 4: 1, 2, 3, +
    Row(
      modifier = Modifier.weight(1f).fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(keySpacing),
    ) {
      KeypadButton(
        label = "1",
        onClick = { onDigit("1") },
        testTag = "key_1",
        shape = shape,
        modifier = Modifier.weight(1f).fillMaxHeight(),
      )
      KeypadButton(
        label = "2",
        onClick = { onDigit("2") },
        testTag = "key_2",
        shape = shape,
        modifier = Modifier.weight(1f).fillMaxHeight(),
      )
      KeypadButton(
        label = "3",
        onClick = { onDigit("3") },
        testTag = "key_3",
        shape = shape,
        modifier = Modifier.weight(1f).fillMaxHeight(),
      )
      KeypadButton(
        label = "+",
        onClick = { onOperator("+") },
        style = KeyStyle.OPERATOR,
        testTag = "key_add",
        contentDescriptionText = "Add",
        shape = shape,
        modifier = Modifier.weight(1f).fillMaxHeight(),
      )
    }

    // Row 5: 0, ., ⌫, =
    Row(
      modifier = Modifier.weight(1f).fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(keySpacing),
    ) {
      KeypadButton(
        label = "0",
        onClick = { onDigit("0") },
        testTag = "key_0",
        shape = shape,
        modifier = Modifier.weight(1f).fillMaxHeight(),
      )
      KeypadButton(
        label = ".",
        onClick = onDecimal,
        testTag = "key_decimal",
        contentDescriptionText = "Decimal point",
        shape = shape,
        modifier = Modifier.weight(1f).fillMaxHeight(),
      )
      KeypadButton(
        label = "⌫",
        icon = Icons.AutoMirrored.Outlined.Backspace,
        onClick = onBackspace,
        testTag = "key_backspace",
        contentDescriptionText = backspaceDesc,
        shape = shape,
        modifier = Modifier.weight(1f).fillMaxHeight(),
      )
      KeypadButton(
        label = "=",
        onClick = onEquals,
        style = KeyStyle.EQUALS,
        testTag = "key_equals",
        contentDescriptionText = "Equals",
        shape = shape,
        modifier = Modifier.weight(1f).fillMaxHeight(),
      )
    }
  }
}

@Composable
private fun ScientificKeypadGrid(
  isInverseMode: Boolean,
  angleUnit: AngleUnit,
  onToggleInverse: () -> Unit,
  onToggleAngleUnit: () -> Unit,
  onConstant: (String) -> Unit,
  onOperator: (String) -> Unit,
  onFunction: (String) -> Unit,
  onFactorial: () -> Unit,
  onToggleSign: () -> Unit,
  keySpacing: Dp,
  modifier: Modifier = Modifier,
) {
  val shape = RoundedCornerShape(16.dp)
  Column(
    modifier = modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(keySpacing),
  ) {
    // Row 1: INV, sin/sin⁻¹, cos/cos⁻¹, tan/tan⁻¹, DEG/RAD
    Row(
      modifier = Modifier.weight(1f).fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(keySpacing),
    ) {
      KeypadButton(
        label = "INV",
        onClick = onToggleInverse,
        style = KeyStyle.SCIENTIFIC,
        isActive = isInverseMode,
        testTag = "key_inv",
        contentDescriptionText = stringResource(R.string.cd_inverse_functions),
        shape = shape,
        fontSize = 13.sp,
        modifier = Modifier.weight(1f).fillMaxHeight(),
      )
      KeypadButton(
        label = if (isInverseMode) "sin⁻¹" else "sin",
        onClick = { onFunction(if (isInverseMode) "sin⁻¹(" else "sin(") },
        style = KeyStyle.SCIENTIFIC,
        testTag = "key_sin",
        shape = shape,
        fontSize = 14.sp,
        modifier = Modifier.weight(1f).fillMaxHeight(),
      )
      KeypadButton(
        label = if (isInverseMode) "cos⁻¹" else "cos",
        onClick = { onFunction(if (isInverseMode) "cos⁻¹(" else "cos(") },
        style = KeyStyle.SCIENTIFIC,
        testTag = "key_cos",
        shape = shape,
        fontSize = 14.sp,
        modifier = Modifier.weight(1f).fillMaxHeight(),
      )
      KeypadButton(
        label = if (isInverseMode) "tan⁻¹" else "tan",
        onClick = { onFunction(if (isInverseMode) "tan⁻¹(" else "tan(") },
        style = KeyStyle.SCIENTIFIC,
        testTag = "key_tan",
        shape = shape,
        fontSize = 14.sp,
        modifier = Modifier.weight(1f).fillMaxHeight(),
      )
      KeypadButton(
        label = if (angleUnit == AngleUnit.DEG) "DEG" else "RAD",
        onClick = onToggleAngleUnit,
        style = KeyStyle.SCIENTIFIC,
        testTag = "key_angle_unit",
        contentDescriptionText = stringResource(R.string.cd_angle_unit),
        shape = shape,
        fontSize = 13.sp,
        modifier = Modifier.weight(1f).fillMaxHeight(),
      )
    }

    // Row 2: √ / x², ln / eˣ, log / 10ˣ, xʸ, x!
    Row(
      modifier = Modifier.weight(1f).fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(keySpacing),
    ) {
      KeypadButton(
        label = if (isInverseMode) "x²" else "√",
        onClick = {
          if (isInverseMode) onOperator("^") else onFunction("√(")
        },
        style = KeyStyle.SCIENTIFIC,
        testTag = "key_sqrt",
        contentDescriptionText = if (isInverseMode) "Power" else "Square root",
        shape = shape,
        modifier = Modifier.weight(1f).fillMaxHeight(),
      )
      KeypadButton(
        label = if (isInverseMode) "eˣ" else "ln",
        onClick = {
          if (isInverseMode) onFunction("e^(") else onFunction("ln(")
        },
        style = KeyStyle.SCIENTIFIC,
        testTag = "key_ln",
        shape = shape,
        modifier = Modifier.weight(1f).fillMaxHeight(),
      )
      KeypadButton(
        label = if (isInverseMode) "10ˣ" else "log",
        onClick = {
          if (isInverseMode) onFunction("10^(") else onFunction("log(")
        },
        style = KeyStyle.SCIENTIFIC,
        testTag = "key_log",
        shape = shape,
        modifier = Modifier.weight(1f).fillMaxHeight(),
      )
      KeypadButton(
        label = "xʸ",
        onClick = { onOperator("^") },
        style = KeyStyle.SCIENTIFIC,
        testTag = "key_power",
        contentDescriptionText = "Exponent",
        shape = shape,
        modifier = Modifier.weight(1f).fillMaxHeight(),
      )
      KeypadButton(
        label = "x!",
        onClick = onFactorial,
        style = KeyStyle.SCIENTIFIC,
        testTag = "key_factorial",
        contentDescriptionText = "Factorial",
        shape = shape,
        modifier = Modifier.weight(1f).fillMaxHeight(),
      )
    }

    // Row 3: π, e, |x|, 1/x, ±
    Row(
      modifier = Modifier.weight(1f).fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(keySpacing),
    ) {
      KeypadButton(
        label = "π",
        onClick = { onConstant("π") },
        style = KeyStyle.SCIENTIFIC,
        testTag = "key_pi",
        contentDescriptionText = "Pi",
        shape = shape,
        modifier = Modifier.weight(1f).fillMaxHeight(),
      )
      KeypadButton(
        label = "e",
        onClick = { onConstant("e") },
        style = KeyStyle.SCIENTIFIC,
        testTag = "key_e",
        contentDescriptionText = "Euler's number",
        shape = shape,
        modifier = Modifier.weight(1f).fillMaxHeight(),
      )
      KeypadButton(
        label = "|x|",
        onClick = { onFunction("abs(") },
        style = KeyStyle.SCIENTIFIC,
        testTag = "key_abs",
        contentDescriptionText = "Absolute value",
        shape = shape,
        modifier = Modifier.weight(1f).fillMaxHeight(),
      )
      KeypadButton(
        label = "1/x",
        onClick = { onFunction("1÷(") },
        style = KeyStyle.SCIENTIFIC,
        testTag = "key_reciprocal",
        contentDescriptionText = "Reciprocal",
        shape = shape,
        modifier = Modifier.weight(1f).fillMaxHeight(),
      )
      KeypadButton(
        label = "±",
        onClick = onToggleSign,
        style = KeyStyle.SCIENTIFIC,
        testTag = "key_sign",
        contentDescriptionText = "Toggle sign",
        shape = shape,
        modifier = Modifier.weight(1f).fillMaxHeight(),
      )
    }
  }
}

@Composable
private fun LandscapeScientificColumn(
  isInverseMode: Boolean,
  onToggleInverse: () -> Unit,
  onConstant: (String) -> Unit,
  onOperator: (String) -> Unit,
  onFunction: (String) -> Unit,
  onFactorial: () -> Unit,
  onToggleSign: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val spacing = 6.dp
  val shape = RoundedCornerShape(14.dp)
  Column(
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(spacing),
  ) {
    // 5 rows x 3 cols in landscape
    Row(
      modifier = Modifier.weight(1f).fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(spacing),
    ) {
      KeypadButton(
        label = "INV",
        onClick = onToggleInverse,
        style = KeyStyle.SCIENTIFIC,
        isActive = isInverseMode,
        testTag = "key_inv",
        shape = shape,
        fontSize = 12.sp,
        modifier = Modifier.weight(1f).fillMaxHeight(),
      )
      KeypadButton(
        label = "π",
        onClick = { onConstant("π") },
        style = KeyStyle.SCIENTIFIC,
        testTag = "key_pi",
        shape = shape,
        fontSize = 14.sp,
        modifier = Modifier.weight(1f).fillMaxHeight(),
      )
      KeypadButton(
        label = "e",
        onClick = { onConstant("e") },
        style = KeyStyle.SCIENTIFIC,
        testTag = "key_e",
        shape = shape,
        fontSize = 14.sp,
        modifier = Modifier.weight(1f).fillMaxHeight(),
      )
    }
    Row(
      modifier = Modifier.weight(1f).fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(spacing),
    ) {
      KeypadButton(
        label = if (isInverseMode) "sin⁻¹" else "sin",
        onClick = { onFunction(if (isInverseMode) "sin⁻¹(" else "sin(") },
        style = KeyStyle.SCIENTIFIC,
        testTag = "key_sin",
        shape = shape,
        fontSize = 13.sp,
        modifier = Modifier.weight(1f).fillMaxHeight(),
      )
      KeypadButton(
        label = if (isInverseMode) "cos⁻¹" else "cos",
        onClick = { onFunction(if (isInverseMode) "cos⁻¹(" else "cos(") },
        style = KeyStyle.SCIENTIFIC,
        testTag = "key_cos",
        shape = shape,
        fontSize = 13.sp,
        modifier = Modifier.weight(1f).fillMaxHeight(),
      )
      KeypadButton(
        label = if (isInverseMode) "tan⁻¹" else "tan",
        onClick = { onFunction(if (isInverseMode) "tan⁻¹(" else "tan(") },
        style = KeyStyle.SCIENTIFIC,
        testTag = "key_tan",
        shape = shape,
        fontSize = 13.sp,
        modifier = Modifier.weight(1f).fillMaxHeight(),
      )
    }
    Row(
      modifier = Modifier.weight(1f).fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(spacing),
    ) {
      KeypadButton(
        label = if (isInverseMode) "eˣ" else "ln",
        onClick = { onFunction(if (isInverseMode) "e^(" else "ln(") },
        style = KeyStyle.SCIENTIFIC,
        testTag = "key_ln",
        shape = shape,
        fontSize = 14.sp,
        modifier = Modifier.weight(1f).fillMaxHeight(),
      )
      KeypadButton(
        label = if (isInverseMode) "10ˣ" else "log",
        onClick = { onFunction(if (isInverseMode) "10^(" else "log(") },
        style = KeyStyle.SCIENTIFIC,
        testTag = "key_log",
        shape = shape,
        fontSize = 14.sp,
        modifier = Modifier.weight(1f).fillMaxHeight(),
      )
      KeypadButton(
        label = "√",
        onClick = { onFunction("√(") },
        style = KeyStyle.SCIENTIFIC,
        testTag = "key_sqrt",
        shape = shape,
        fontSize = 15.sp,
        modifier = Modifier.weight(1f).fillMaxHeight(),
      )
    }
    Row(
      modifier = Modifier.weight(1f).fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(spacing),
    ) {
      KeypadButton(
        label = "xʸ",
        onClick = { onOperator("^") },
        style = KeyStyle.SCIENTIFIC,
        testTag = "key_power",
        shape = shape,
        fontSize = 14.sp,
        modifier = Modifier.weight(1f).fillMaxHeight(),
      )
      KeypadButton(
        label = "x!",
        onClick = onFactorial,
        style = KeyStyle.SCIENTIFIC,
        testTag = "key_factorial",
        shape = shape,
        fontSize = 14.sp,
        modifier = Modifier.weight(1f).fillMaxHeight(),
      )
      KeypadButton(
        label = "|x|",
        onClick = { onFunction("abs(") },
        style = KeyStyle.SCIENTIFIC,
        testTag = "key_abs",
        shape = shape,
        fontSize = 14.sp,
        modifier = Modifier.weight(1f).fillMaxHeight(),
      )
    }
    Row(
      modifier = Modifier.weight(1f).fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(spacing),
    ) {
      KeypadButton(
        label = "1/x",
        onClick = { onFunction("1÷(") },
        style = KeyStyle.SCIENTIFIC,
        testTag = "key_reciprocal",
        shape = shape,
        fontSize = 14.sp,
        modifier = Modifier.weight(1f).fillMaxHeight(),
      )
      KeypadButton(
        label = "±",
        onClick = onToggleSign,
        style = KeyStyle.SCIENTIFIC,
        testTag = "key_sign",
        shape = shape,
        fontSize = 15.sp,
        modifier = Modifier.weight(1f).fillMaxHeight(),
      )
      Spacer(modifier = Modifier.weight(1f))
    }
  }
}
