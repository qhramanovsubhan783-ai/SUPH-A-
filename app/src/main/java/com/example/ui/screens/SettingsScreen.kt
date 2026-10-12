package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.data.ChatSettings
import com.example.data.PersonalityPreset
import com.example.ui.theme.CoralError
import com.example.ui.theme.EmeraldOnline
import com.example.ui.theme.Navy700
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy850
import com.example.ui.theme.Navy900
import com.example.ui.theme.Navy950
import com.example.ui.theme.PurpleLight
import com.example.ui.theme.PurpleNeon
import com.example.ui.theme.PurpleVivid
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletSoft

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentSettings: ChatSettings,
    onSaveSettings: (ChatSettings) -> Unit,
    onClearAllHistory: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val context = LocalContext.current

    var selectedPersonality by remember { mutableStateOf(currentSettings.personality) }
    var customInstructions by remember { mutableStateOf(currentSettings.customSystemInstruction) }
    var temperature by remember { mutableFloatStateOf(currentSettings.temperature) }
    var apiKeyInput by remember { mutableStateOf(currentSettings.customApiKey) }
    var showApiKey by remember { mutableStateOf(false) }
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    val hasGlobalKey = BuildConfig.GEMINI_API_KEY.isNotBlank() && BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Tənzimləmələr",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("settings_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Geri",
                            tint = VioletSoft
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val updated = ChatSettings(
                                personality = selectedPersonality,
                                customSystemInstruction = customInstructions,
                                temperature = temperature,
                                customApiKey = apiKeyInput
                            )
                            onSaveSettings(updated)
                            Toast.makeText(context, "Tənzimləmələr yadda saxlanıldı", Toast.LENGTH_SHORT).show()
                            onBack()
                        },
                        modifier = Modifier.testTag("settings_save_icon_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Yadda saxla",
                            tint = PurpleNeon
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Navy900
                )
            )
        },
        containerColor = Navy950,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 680.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Section 1: Personality Preset
                SectionCard(
                    title = "AI Xarakteri & Rolu",
                    icon = Icons.Default.Psychology
                ) {
                    Text(
                        text = "SUPH AI-nin cavab tərzini və əsas davranışını seçin:",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    PersonalityPreset.values().forEach { preset ->
                        val isSelected = selectedPersonality == preset
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) Navy800 else Navy900,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) PurpleNeon else Navy700,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { selectedPersonality = preset }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { selectedPersonality = preset },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = PurpleNeon,
                                        unselectedColor = TextMuted
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = preset.titleAz,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = preset.descAz,
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                // Section 2: Custom System Instruction
                SectionCard(
                    title = "Fərdi Sistem Təlimatı",
                    icon = Icons.Default.Tune
                ) {
                    Text(
                        text = "Modelə xüsusi göstərişlər verin (məsələn: 'Həmişə qısa cavab ver', 'Mənə Subhan deyə müraciət et', 'Kodları Kotlin ilə yaz'):",
                        fontSize = 12.5.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = customInstructions,
                        onValueChange = { customInstructions = it },
                        placeholder = {
                            Text(
                                text = "Xüsusi göstərişlərinizi buraya yazın...",
                                color = TextMuted,
                                fontSize = 13.sp
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .testTag("custom_instructions_field"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PurpleNeon,
                            unfocusedBorderColor = Navy700,
                            focusedContainerColor = Navy900,
                            unfocusedContainerColor = Navy900,
                            cursorColor = PurpleLight,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }

                // Section 3: Creativity / Temperature Slider
                SectionCard(
                    title = "Kreativlik Səviyyəsi (Temperature)",
                    icon = Icons.Default.Info
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Model Yaradıcılığı:",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = String.format("%.2f", temperature),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = PurpleNeon
                        )
                    }

                    Slider(
                        value = temperature,
                        onValueChange = { temperature = it },
                        valueRange = 0.0f..1.0f,
                        steps = 9,
                        colors = SliderDefaults.colors(
                            thumbColor = PurpleNeon,
                            activeTrackColor = PurpleVivid,
                            inactiveTrackColor = Navy700
                        ),
                        modifier = Modifier.testTag("temperature_slider")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "0.0 (Daha Dəqiq)", fontSize = 11.sp, color = TextMuted)
                        Text(text = "0.7 (Balanslı)", fontSize = 11.sp, color = TextMuted)
                        Text(text = "1.0 (Yaradıcı)", fontSize = 11.sp, color = TextMuted)
                    }
                }

                // Section 4: API Key Management & Security
                SectionCard(
                    title = "Google Gemini API Açarı & Təhlükəsizlik",
                    icon = Icons.Default.Security
                ) {
                    // Security Guidance Card
                    Surface(
                        color = Navy900,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Navy700, RoundedCornerShape(10.dp))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (hasGlobalKey || apiKeyInput.isNotBlank()) Icons.Default.CheckCircle else Icons.Default.Key,
                                    contentDescription = null,
                                    tint = if (hasGlobalKey || apiKeyInput.isNotBlank()) EmeraldOnline else PurpleNeon,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (hasGlobalKey || apiKeyInput.isNotBlank()) "Status: Aktiv və Hazır" else "Status: Açar Əlavə Edilməyib",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (hasGlobalKey || apiKeyInput.isNotBlank()) EmeraldOnline else PurpleLight
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Açarı harada və necə əlavə etməli?\n" +
                                    "1. AI Studio Secrets panelində: 'GEMINI_API_KEY' adlı açar yaradın və qiymətini təyin edin.\n" +
                                    "2. Aşağıdakı xanaya birbaşa daxil edin: Əgər dərhal test etmək istəyirsinizsə, açarınızı bura yaza bilərsiniz.\n" +
                                    "3. Təhlükəsizlik: Açarınız heç vaxt veb brauzerdə ictimaiyyətə açılmır, server konfiqurasiyası və BuildConfig vasitəsilə qorunur.",
                                fontSize = 12.sp,
                                lineHeight = 18.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = { apiKeyInput = it },
                        label = { Text("Fərdi Gemini API Açarı (Könüllü)") },
                        placeholder = { Text("AIzaSy...") },
                        visualTransformation = if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showApiKey = !showApiKey }) {
                                Icon(
                                    imageVector = if (showApiKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (showApiKey) "Gizlət" else "Göstər",
                                    tint = VioletSoft
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("api_key_input_field"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PurpleNeon,
                            unfocusedBorderColor = Navy700,
                            focusedContainerColor = Navy900,
                            unfocusedContainerColor = Navy900,
                            cursorColor = PurpleLight,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true
                    )
                }

                // Section 5: Clear History
                SectionCard(
                    title = "Məlumatları Təmizlə",
                    icon = Icons.Default.DeleteSweep
                ) {
                    Text(
                        text = "Bütün keçmiş söhbətləri və mesajları sıfırlayın:",
                        fontSize = 12.5.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Button(
                        onClick = { showClearConfirmDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CoralError.copy(alpha = 0.2f),
                            contentColor = CoralError
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("clear_history_button")
                    ) {
                        Text(
                            text = "Bütün Söhbət Tarixçəsini Sil",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Save Changes Action Button
                Button(
                    onClick = {
                        val updated = ChatSettings(
                            personality = selectedPersonality,
                            customSystemInstruction = customInstructions,
                            temperature = temperature,
                            customApiKey = apiKeyInput
                        )
                        onSaveSettings(updated)
                        Toast.makeText(context, "Tənzimləmələr uğurla yadda saxlanıldı", Toast.LENGTH_SHORT).show()
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PurpleVivid,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("save_settings_bottom_button")
                ) {
                    Text(
                        text = "Yadda Saxla və Tətbiq Et",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        if (showClearConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showClearConfirmDialog = false },
                containerColor = Navy850,
                title = {
                    Text(
                        text = "Tarixçəni silmək istəyirsiniz?",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                text = {
                    Text(
                        text = "Bu əməliyyat bütün saxlanılmış söhbətləri və mesajları həmişəlik siləcək.",
                        color = TextSecondary
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onClearAllHistory()
                            showClearConfirmDialog = false
                            Toast.makeText(context, "Tarixçə təmizləndi", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CoralError)
                    ) {
                        Text("Bəli, Sil", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearConfirmDialog = false }) {
                        Text("Ləğv et", color = VioletSoft)
                    }
                }
            )
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Surface(
        color = Navy850,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Navy700, RoundedCornerShape(14.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(PurpleNeon.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = PurpleNeon,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
            Divider(color = Navy700, modifier = Modifier.padding(bottom = 12.dp))
            content()
        }
    }
}
