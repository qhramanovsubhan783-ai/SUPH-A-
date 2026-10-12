const express = require('express');
const path = require('path');
require('dotenv').config();

const app = express();
const PORT = process.env.PORT || 3000;

app.use(express.json());
app.use(express.static(path.join(__dirname, 'public')));

// Secure server-side Gemini API endpoint
app.post('/api/chat', async (req, res) => {
  const apiKey = process.env.GEMINI_API_KEY;

  if (!apiKey || apiKey === 'MY_GEMINI_API_KEY') {
    return res.status(400).json({
      error: '⚠️ Gemini API açarı serverdə tapılmadı! Zəhmət olmasa .env faylında GEMINI_API_KEY təyin edin.'
    });
  }

  const { messages, systemInstruction, temperature = 0.7 } = req.body;

  if (!messages || !Array.isArray(messages) || messages.length === 0) {
    return res.status(400).json({ error: 'Mesaj məzmunu boş ola bilməz.' });
  }

  // Format contents for Google Gemini API multi-turn format
  const contents = messages.map(msg => ({
    role: msg.role === 'user' ? 'user' : 'model',
    parts: [{ text: msg.text }]
  }));

  const payload = {
    contents,
    generationConfig: {
      temperature: parseFloat(temperature),
      maxOutputTokens: 4096
    }
  };

  if (systemInstruction && systemInstruction.trim().length > 0) {
    payload.systemInstruction = {
      parts: [{ text: systemInstruction.trim() }]
    };
  }

  const model = 'gemini-3.5-flash';
  const url = `https://generativelanguage.googleapis.com/v1beta/models/${model}:generateContent?key=${apiKey}`;

  try {
    const apiResponse = await fetch(url, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });

    const data = await apiResponse.json();

    if (!apiResponse.ok) {
      console.error('Gemini API Error:', data);
      let errorMsg = 'Google Gemini API xətası baş verdi.';
      if (apiResponse.status === 400) {
        errorMsg = 'API Sorğu xətası (400): Parametrlər və ya açar formatı səhvdir.';
      } else if (apiResponse.status === 403) {
        errorMsg = 'İcazə xətası (403): Gemini API açarınız etibarsızdır.';
      } else if (apiResponse.status === 429) {
        errorMsg = 'Sorğu limiti aşıldı (429): Bir az sonra yenidən cəhd edin.';
      }
      return res.status(apiResponse.status).json({
        error: errorMsg,
        details: data.error?.message
      });
    }

    const reply = data.candidates?.[0]?.content?.parts?.[0]?.text;
    if (!reply) {
      return res.status(500).json({ error: 'Gemini cavab mətni generasiya edə bilmədi.' });
    }

    res.json({ reply });
  } catch (err) {
    console.error('Server Exception:', err);
    res.status(500).json({
      error: 'Serverlə əlaqə qurularkən şəbəkə xətası baş verdi.',
      details: err.message
    });
  }
});

app.listen(PORT, () => {
  console.log(`SUPH AI veb serveri http://localhost:${PORT} ünvanında işləyir.`);
});
