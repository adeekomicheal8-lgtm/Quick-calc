package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.example.ui.CalculatorRoute
import com.example.ui.CalculatorViewModel
import com.example.ui.theme.CalculatorTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Calculator", appName)
  }

  @Test
  fun `standard arithmetic flow with live preview and equals`() {
    val viewModel = CalculatorViewModel()
    composeTestRule.setContent {
      CalculatorTheme(darkTheme = false) {
        CalculatorRoute(viewModel = viewModel)
      }
    }

    composeTestRule.onNodeWithTag("key_2").performClick()
    composeTestRule.onNodeWithTag("key_add").performClick()
    composeTestRule.onNodeWithTag("key_3").performClick()
    composeTestRule.onNodeWithTag("key_multiply").performClick()
    composeTestRule.onNodeWithTag("key_4").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("display_preview_text").assertTextEquals("= 14")

    composeTestRule.onNodeWithTag("key_equals").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("display_expression_text").assertTextEquals("14")
    composeTestRule.onNodeWithTag("completed_expression_text").assertTextEquals("2+3×4 =")
  }

  @Test
  fun `scientific mode toggle and calculation history`() {
    val viewModel = CalculatorViewModel()
    composeTestRule.setContent {
      CalculatorTheme(darkTheme = true) {
        CalculatorRoute(viewModel = viewModel)
      }
    }

    composeTestRule.onNodeWithTag("toggle_scientific_button").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("key_sqrt").assertIsDisplayed().performClick()
    composeTestRule.onNodeWithTag("key_9").performClick()
    composeTestRule.onNodeWithTag("key_equals").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("display_expression_text").assertTextEquals("3")

    composeTestRule.onNodeWithTag("history_button").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("history_list").assertIsDisplayed()
    composeTestRule.onNodeWithText("√(9)").assertIsDisplayed()
  }
}
