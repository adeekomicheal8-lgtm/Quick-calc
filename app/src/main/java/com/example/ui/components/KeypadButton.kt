package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.SpaceGroteskFontFamily

enum class KeyStyle {
  NUMBER,
  UTILITY,
  OPERATOR,
  EQUALS,
  SCIENTIFIC,
}

@Composable
fun KeypadButton(
  label: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  style: KeyStyle = KeyStyle.NUMBER,
  icon: ImageVector? = null,
  contentDescriptionText: String = label,
  testTag: String,
  isActive: Boolean = false,
  fontSize: TextUnit = TextUnit.Unspecified,
  shape: Shape = RoundedCornerShape(22.dp),
) {
  val haptic = LocalHapticFeedback.current
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()

  val scale by
    animateFloatAsState(
      targetValue = if (isPressed) 0.93f else 1f,
      animationSpec =
        spring(
          dampingRatio = Spring.DampingRatioMediumBouncy,
          stiffness = Spring.StiffnessMedium,
        ),
      label = "key_scale",
    )

  val containerColor =
    when {
      isActive -> MaterialTheme.colorScheme.secondary
      style == KeyStyle.EQUALS -> MaterialTheme.colorScheme.primary
      style == KeyStyle.OPERATOR -> MaterialTheme.colorScheme.primaryContainer
      style == KeyStyle.UTILITY -> MaterialTheme.colorScheme.surfaceContainerHigh
      style == KeyStyle.SCIENTIFIC -> MaterialTheme.colorScheme.secondaryContainer
      else -> MaterialTheme.colorScheme.surfaceContainer
    }

  val contentColor =
    when {
      isActive -> MaterialTheme.colorScheme.onSecondary
      style == KeyStyle.EQUALS -> MaterialTheme.colorScheme.onPrimary
      style == KeyStyle.OPERATOR -> MaterialTheme.colorScheme.onPrimaryContainer
      style == KeyStyle.UTILITY -> MaterialTheme.colorScheme.onSurface
      style == KeyStyle.SCIENTIFIC -> MaterialTheme.colorScheme.onSecondaryContainer
      else -> MaterialTheme.colorScheme.onSurface
    }

  val border =
    if (style == KeyStyle.NUMBER) {
      BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))
    } else {
      null
    }

  val resolvedFontSize =
    if (fontSize != TextUnit.Unspecified) {
      fontSize
    } else {
      when (style) {
        KeyStyle.EQUALS,
        KeyStyle.OPERATOR -> 26.sp
        KeyStyle.UTILITY -> 20.sp
        KeyStyle.SCIENTIFIC -> 15.sp
        KeyStyle.NUMBER -> 24.sp
      }
    }

  val fontFamily =
    if (style == KeyStyle.SCIENTIFIC) {
      JetBrainsMonoFontFamily
    } else {
      SpaceGroteskFontFamily
    }

  val fontWeight =
    when (style) {
      KeyStyle.EQUALS,
      KeyStyle.OPERATOR -> FontWeight.SemiBold
      KeyStyle.SCIENTIFIC -> FontWeight.Medium
      else -> FontWeight.Medium
    }

  Surface(
    onClick = {
      haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
      onClick()
    },
    modifier =
      modifier
        .minimumInteractiveComponentSize()
        .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
        .graphicsLayer {
          scaleX = scale
          scaleY = scale
        }
        .testTag(testTag)
        .semantics { contentDescription = contentDescriptionText },
    shape = shape,
    color = containerColor,
    contentColor = contentColor,
    border = border,
    tonalElevation = if (style == KeyStyle.NUMBER) 1.dp else 0.dp,
    interactionSource = interactionSource,
  ) {
    Box(
      modifier = Modifier.fillMaxSize(),
      contentAlignment = Alignment.Center,
    ) {
      if (icon != null) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          modifier = Modifier.size(24.dp),
        )
      } else {
        Text(
          text = label,
          fontFamily = fontFamily,
          fontWeight = fontWeight,
          fontSize = resolvedFontSize,
          maxLines = 1,
          softWrap = false,
          overflow = TextOverflow.Clip,
        )
      }
    }
  }
}
