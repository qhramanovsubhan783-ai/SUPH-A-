package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.ChatSettings
import com.example.data.MessageSender
import com.example.data.PersonalityPreset
import com.example.viewmodel.ChatViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("SUPH AI", appName)
  }

  @Test
  fun `test chat viewmodel initial welcome state`() {
    val viewModel = ChatViewModel()
    val messages = viewModel.messages.value
    assertTrue("Should have initial welcome message", messages.isNotEmpty())
    assertEquals(MessageSender.AI, messages.first().sender)
    assertTrue(messages.first().text.contains("SUPH AI"))
  }

  @Test
  fun `test chat viewmodel update settings`() {
    val viewModel = ChatViewModel()
    val newSettings = ChatSettings(
      personality = PersonalityPreset.CODER,
      customSystemInstruction = "Cavabları yalnız Kotlin ilə yaz",
      temperature = 0.3f,
      customApiKey = "TEST_KEY_123"
    )
    viewModel.updateSettings(newSettings)
    assertEquals(PersonalityPreset.CODER, viewModel.settings.value.personality)
    assertEquals("Cavabları yalnız Kotlin ilə yaz", viewModel.settings.value.customSystemInstruction)
    assertEquals(0.3f, viewModel.settings.value.temperature)
    assertEquals("TEST_KEY_123", viewModel.settings.value.customApiKey)
    assertTrue("Should indicate API key is configured", viewModel.isApiKeyConfigured())
  }
}
