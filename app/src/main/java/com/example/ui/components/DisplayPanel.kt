package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BrightnessAuto
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Functions
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.domain.AngleUnit
import com.example.ui.CalculatorUiState
import com.example.ui.ThemePreference
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.SpaceGroteskFontFamily

@Composable
fun DisplayPanel(
  uiState: CalculatorUiState,
  showHistoryButton: Boolean,
  showScientificToggle: Boolean,
  isCompactHeight: Boolean,
  onToggleScientific: () -> Unit,
  onToggleAngleUnit: () -> Unit,
  onOpenHistory: () -> Unit,
  onCycleTheme: () -> Unit,
  onCopyResult: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val primaryAccent = MaterialTheme.colorScheme.primary
  val secondaryAccent = MaterialTheme.colorScheme.secondary
  val surfaceColor = MaterialTheme.colorScheme.surface
  val surfaceLow = MaterialTheme.colorScheme.surfaceContainerLow

  val expressionScrollState = rememberScrollState()
  LaunchedEffect(uiState.expression) {
    expressionScrollState.animateScrollTo(expressionScrollState.maxValue)
  }

  val copyableText =
    when {
      uiState.justEvaluated && uiState.rawResult.isNotEmpty() -> uiState.rawResult
      uiState.livePreview.isNotEmpty() -> uiState.livePreview.replace(",", "")
      else -> ""
    }

  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp, topStart = 24.dp, topEnd = 24.dp),
    color = surfaceColor,
    tonalElevation = 1.dp,
  ) {
    Box(
      modifier =
        Modifier
          .fillMaxWidth()
          .drawBehind {
            drawRect(
              brush =
                Brush.radialGradient(
                  colors =
                    listOf(
                      primaryAccent.copy(alpha = 0.08f),
                      Color.Transparent,
                    ),
                  center = Offset(size.width * 0.88f, size.height * 0.85f),
                  radius = size.maxDimension * 0.75f,
                )
            )
            drawRect(
              brush =
                Brush.linearGradient(
                  colors =
                    listOf(
                      surfaceLow.copy(alpha = 0.35f),
                      Color.Transparent,
                    ),
                  start = Offset.Zero,
                  end = Offset(0f, size.height),
                )
            )
          }
          .padding(horizontal = 16.dp, vertical = if (isCompactHeight) 10.dp else 14.dp),
    ) {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.SpaceBetween,
      ) {
        // Top Utility Bar
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            if (showScientificToggle) {
              Surface(
                onClick = onToggleScientific,
                modifier =
                  Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("toggle_scientific_button"),
                shape = CircleShape,
                color =
                  if (uiState.isScientificMode) {
                    MaterialTheme.colorScheme.secondaryContainer
                  } else {
                    MaterialTheme.colorScheme.surfaceContainerHigh
                  },
                contentColor =
                  if (uiState.isScientificMode) {
                    MaterialTheme.colorScheme.onSecondaryContainer
                  } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                  },
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                  Icon(
                    imageVector = Icons.Rounded.Functions,
                    contentDescription = stringResource(R.string.cd_toggle_scientific),
                    modifier = Modifier.size(16.dp),
                  )
                  Text(
                    text =
                      if (uiState.isScientificMode) {
                        stringResource(R.string.label_scientific)
                      } else {
                        stringResource(R.string.label_standard)
                      },
                    style = MaterialTheme.typography.labelMedium,
                  )
                }
              }
            }

            if (uiState.isScientificMode || !showScientificToggle) {
              Surface(
                onClick = onToggleAngleUnit,
                modifier =
                  Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("angle_unit_badge"),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = MaterialTheme.colorScheme.secondary,
              ) {
                Text(
                  text =
                    if (uiState.angleUnit == AngleUnit.DEG) {
                      stringResource(R.string.label_deg)
                    } else {
                      stringResource(R.string.label_rad)
                    },
                  style = MaterialTheme.typography.labelMedium,
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                )
              }
            }
          }

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
          ) {
            AnimatedVisibility(
              visible = copyableText.isNotEmpty(),
              enter = fadeIn(),
              exit = fadeOut(),
            ) {
              IconButton(
                onClick = { onCopyResult(copyableText) },
                modifier = Modifier.testTag("copy_result_button"),
              ) {
                Icon(
                  imageVector = Icons.Rounded.ContentCopy,
                  contentDescription = stringResource(R.string.cd_copy_result),
                  tint = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.size(20.dp),
                )
              }
            }

            IconButton(
              onClick = onCycleTheme,
              modifier = Modifier.testTag("theme_toggle_button"),
            ) {
              val themeIcon =
                when (uiState.themePreference) {
                  ThemePreference.SYSTEM -> Icons.Outlined.BrightnessAuto
                  ThemePreference.DARK -> Icons.Outlined.DarkMode
                  ThemePreference.LIGHT -> Icons.Outlined.LightMode
                }
              Icon(
                imageVector = themeIcon,
                contentDescription = stringResource(R.string.cd_toggle_theme),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(21.dp),
              )
            }

            if (showHistoryButton) {
              IconButton(
                onClick = onOpenHistory,
                modifier = Modifier.testTag("history_button"),
              ) {
                BadgedBox(
                  badge = {
                    if (uiState.history.isNotEmpty()) {
                      Badge(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                      ) {
                        Text(
                          text = uiState.history.size.coerceAtMost(99).toString(),
                          style = MaterialTheme.typography.labelSmall,
                        )
                      }
                    }
                  }
                ) {
                  Icon(
                    imageVector = Icons.Outlined.History,
                    contentDescription = stringResource(R.string.cd_open_history),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp),
                  )
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(if (isCompactHeight) 6.dp else 14.dp))

        // Completed Expression (shown above result after pressing '=')
        Box(
          modifier = Modifier.fillMaxWidth().height(26.dp),
          contentAlignment = Alignment.CenterEnd,
        ) {
          val completed = uiState.completedExpression
          if (!completed.isNullOrEmpty()) {
            Text(
              text = "$completed =",
              style = MaterialTheme.typography.bodyLarge,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier.testTag("completed_expression_text"),
            )
          }
        }

        // Primary Expression / Result Readout
        val displayFontSize =
          when {
            isCompactHeight && uiState.expression.length > 14 -> 26.sp
            isCompactHeight -> 34.sp
            uiState.expression.length > 18 -> 30.sp
            uiState.expression.length > 12 -> 38.sp
            uiState.expression.length > 8 -> 46.sp
            else -> 54.sp
          }

        Box(
          modifier =
            Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .clickable(enabled = copyableText.isNotEmpty()) {
                if (copyableText.isNotEmpty()) onCopyResult(copyableText)
              }
              .padding(vertical = 4.dp)
              .testTag("display_expression_text"),
          contentAlignment = Alignment.CenterEnd,
        ) {
          if (uiState.expression.isEmpty()) {
            Text(
              text = "0",
              fontFamily = SpaceGroteskFontFamily,
              fontWeight = FontWeight.Medium,
              fontSize = displayFontSize,
              color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
              textAlign = TextAlign.End,
              maxLines = 1,
            )
          } else {
            val highlighted =
              highlightExpression(
                expression = uiState.expression,
                operatorColor = primaryAccent,
                functionColor = secondaryAccent,
              )
            Text(
              text = highlighted,
              fontFamily = SpaceGroteskFontFamily,
              fontWeight = if (uiState.justEvaluated) FontWeight.SemiBold else FontWeight.Medium,
              fontSize = displayFontSize,
              lineHeight = displayFontSize * 1.12f,
              color = MaterialTheme.colorScheme.onSurface,
              maxLines = 1,
              softWrap = false,
              modifier = Modifier.horizontalScroll(expressionScrollState),
            )
          }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Live Preview or Error Message
        Box(
          modifier = Modifier.fillMaxWidth().height(30.dp),
          contentAlignment = Alignment.CenterEnd,
        ) {
          when {
            uiState.errorMessage != null -> {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.testTag("display_error_text"),
              ) {
                Icon(
                  imageVector = Icons.Rounded.ErrorOutline,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.error,
                  modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = uiState.errorMessage,
                  fontFamily = JetBrainsMonoFontFamily,
                  fontSize = 15.sp,
                  fontWeight = FontWeight.Medium,
                  color = MaterialTheme.colorScheme.error,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
              }
            }
            uiState.livePreview.isNotEmpty() -> {
              Text(
                text = "= ${uiState.livePreview}",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.testTag("display_preview_text"),
              )
            }
          }
        }
      }
    }
  }
}

private fun highlightExpression(
  expression: String,
  operatorColor: Color,
  functionColor: Color,
): AnnotatedString {
  val operators = setOf('+', '−', '×', '÷', '^', '%')
  val parens = setOf('(', ')')
  return buildAnnotatedString {
    var i = 0
    while (i < expression.length) {
      val ch = expression[i]
      when {
        ch in operators -> {
          withStyle(SpanStyle(color = operatorColor)) { append(ch) }
          i++
        }
        ch in parens || ch == '√' || ch == 'π' || ch == 'e' || ch == '!' -> {
          withStyle(SpanStyle(color = functionColor)) { append(ch) }
          i++
        }
        ch.isLetter() -> {
          val start = i
          while (i < expression.length && (expression[i].isLetter() || expression[i] == '⁻' || expression[i] == '¹')) {
            i++
          }
          withStyle(SpanStyle(color = functionColor)) {
            append(expression.substring(start, i))
          }
        }
        else -> {
          append(ch)
          i++
        }
      }
    }
  }
}
