package com.example.data

import java.util.UUID

enum class MessageSender {
    USER,
    AI
}

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isError: Boolean = false,
    val errorMessage: String? = null
)

enum class PersonalityPreset(
    val id: String,
    val titleAz: String,
    val descAz: String,
    val systemPrompt: String
) {
    BALANCED(
        id = "balanced",
        titleAz = "🤖 Standart Köməkçi",
        descAz = "Dəqiq, nəzakətli və hər sahədə faydalı ümumi köməkçi",
        systemPrompt = "Sən SUPH AI - güclü, nəzakətli və çox bacarıqlı süni intellekt köməkçisisən. Əsas dilin Azərbaycan dilidir. İstifadəçilərin suallarına aydın, dəqiq, məntiqli və faydalı cavab ver. Formatlaşdırma üçün lazım gəldikdə maddələr və başlıqlardan istifadə et."
    ),
    CODER(
        id = "coder",
        titleAz = "💻 Proqramlaşdırma Mütəxəssisi",
        descAz = "Təmiz kod, arxitektura və texniki həllər üzrə ekspert",
        systemPrompt = "Sən SUPH AI proqram təminatı və proqramlaşdırma üzrə baş mühəndisisən. Kodları təmiz, optimal və izahatlı şəkildə təqdim et. Ən yaxşı mühəndislik təcrübələrinə əməl et və texniki sualları dəqiqliklə cavablandır."
    ),
    CREATIVE(
        id = "creative",
        titleAz = "🎨 Yaradıcı & İdeyalar",
        descAz = "Şeir, hekayə, marketinq və yaradıcı beyin həmləsi",
        systemPrompt = "Sən SUPH AI - enerjili, ilhamverici və yaradıcı bir yol yoldaşısan. İstifadəçiyə yeni ideyalar, mətnlər, hekayələr və orijinal təkliflər yaratmaqda kömək et."
    ),
    EXPERT(
        id = "expert",
        titleAz = "🎓 Elmi & Tədqiqatçı",
        descAz = "Dərin elmi izahatlar, faktlar və analitik düşüncə",
        systemPrompt = "Sən SUPH AI elmi tədqiqatçısan. Mürəkkəb mövzuları analitik şəkildə, elmi faktlara və məntiqə əsaslanaraq addım-addım izah et."
    ),
    CUSTOM(
        id = "custom",
        titleAz = "⚙️ Fərdi Təlimat",
        descAz = "Öz şəxsi təlimatınızı və xarakterinizi daxil edin",
        systemPrompt = ""
    )
}

data class ChatSettings(
    val personality: PersonalityPreset = PersonalityPreset.BALANCED,
    val customSystemInstruction: String = "",
    val temperature: Float = 0.7f,
    val customApiKey: String = ""
)

data class ChatSession(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val messages: List<ChatMessage>,
    val updatedAt: Long = System.currentTimeMillis()
)
