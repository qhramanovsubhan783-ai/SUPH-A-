// SUPH AI - Web Client Logic

const personalityPrompts = {
  balanced: "Sən SUPH AI - güclü, nəzakətli və çox bacarıqlı süni intellekt köməkçisisən. Əsas dilin Azərbaycan dilidir. İstifadəçilərin suallarına aydın, dəqiq və faydalı cavab ver.",
  coder: "Sən SUPH AI proqram təminatı və proqramlaşdırma üzrə baş mühəndisisən. Kodları təmiz, optimal və izahatlı şəkildə təqdim et. Ən yaxşı mühəndislik təcrübələrinə əməl et.",
  creative: "Sən SUPH AI - enerjili, ilhamverici və yaradıcı bir yol yoldaşısan. İstifadəçiyə yeni ideyalar, mətnlər, hekayələr və orijinal təkliflər yaratmaqda kömək et.",
  expert: "Sən SUPH AI elmi tədqiqatçısan. Mürəkkəb mövzuları analitik şəkildə, elmi faktlara və məntiqə əsaslanaraq addım-addım izah et."
};

let currentSessionId = generateId();
let messages = [];
let sessions = JSON.parse(localStorage.getItem('suph_sessions') || '[]');
let settings = JSON.parse(localStorage.getItem('suph_settings') || JSON.stringify({
  personality: 'balanced',
  customInstructions: '',
  temperature: 0.7
}));

// DOM Elements
const chatContainer = document.getElementById('chatContainer');
const messagesList = document.getElementById('messagesList');
const welcomeCard = document.getElementById('welcomeCard');
const typingIndicator = document.getElementById('typingIndicator');
const messageInput = document.getElementById('messageInput');
const sendBtn = document.getElementById('sendBtn');
const chatForm = document.getElementById('chatForm');

const sidebar = document.getElementById('sidebar');
const sidebarOverlay = document.getElementById('sidebarOverlay');
const menuToggleBtn = document.getElementById('menuToggleBtn');
const closeSidebarBtn = document.getElementById('closeSidebarBtn');
const sessionsList = document.getElementById('sessionsList');
const newChatSidebarBtn = document.getElementById('newChatSidebarBtn');
const newChatHeaderBtn = document.getElementById('newChatHeaderBtn');

const settingsModal = document.getElementById('settingsModal');
const settingsBtn = document.getElementById('settingsBtn');
const closeSettingsBtn = document.getElementById('closeSettingsBtn');
const saveSettingsBtn = document.getElementById('saveSettingsBtn');
const customInstructionsInput = document.getElementById('customInstructions');
const tempSlider = document.getElementById('tempSlider');
const tempValue = document.getElementById('tempValue');
const clearHistoryBtn = document.getElementById('clearHistoryBtn');

function generateId() {
  return 'sess_' + Math.random().toString(36).substring(2, 9) + Date.now();
}

// Initialize
function init() {
  renderSessions();
  applySettingsToUI();
  setupEventListeners();
  autoResizeTextarea();
}

function setupEventListeners() {
  chatForm.addEventListener('submit', (e) => {
    e.preventDefault();
    handleSend();
  });

  messageInput.addEventListener('keydown', (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  });

  messageInput.addEventListener('input', () => {
    autoResizeTextarea();
    sendBtn.disabled = messageInput.value.trim().length === 0;
  });

  // Suggestion chips
  document.querySelectorAll('.suggestion-chip').forEach(chip => {
    chip.addEventListener('click', () => {
      const prompt = chip.getAttribute('data-prompt');
      messageInput.value = prompt;
      handleSend();
    });
  });

  // Sidebar toggling
  menuToggleBtn?.addEventListener('click', () => {
    sidebar.classList.add('active');
    sidebarOverlay.classList.add('active');
  });

  const closeSidebar = () => {
    sidebar.classList.remove('active');
    sidebarOverlay.classList.remove('active');
  };

  closeSidebarBtn?.addEventListener('click', closeSidebar);
  sidebarOverlay?.addEventListener('click', closeSidebar);

  // New Chat buttons
  newChatSidebarBtn?.addEventListener('click', () => {
    startNewChat();
    closeSidebar();
  });
  newChatHeaderBtn?.addEventListener('click', startNewChat);

  // Settings Modal
  settingsBtn?.addEventListener('click', () => {
    applySettingsToUI();
    settingsModal.classList.add('active');
  });

  closeSettingsBtn?.addEventListener('click', () => {
    settingsModal.classList.remove('active');
  });

  tempSlider.addEventListener('input', () => {
    tempValue.textContent = tempSlider.value;
  });

  saveSettingsBtn.addEventListener('click', () => {
    const selectedPersonality = document.querySelector('input[name="personality"]:checked')?.value || 'balanced';
    settings = {
      personality: selectedPersonality,
      customInstructions: customInstructionsInput.value.trim(),
      temperature: parseFloat(tempSlider.value)
    };
    localStorage.setItem('suph_settings', JSON.stringify(settings));
    settingsModal.classList.remove('active');
  });

  clearHistoryBtn.addEventListener('click', () => {
    if (confirm('Bütün söhbət tarixçəsini silmək istədiyinizdən əminsiniz?')) {
      sessions = [];
      localStorage.removeItem('suph_sessions');
      renderSessions();
      startNewChat();
      settingsModal.classList.remove('active');
    }
  });
}

function autoResizeTextarea() {
  messageInput.style.height = 'auto';
  messageInput.style.height = (messageInput.scrollHeight) + 'px';
}

function startNewChat() {
  saveCurrentSession();
  currentSessionId = generateId();
  messages = [];
  messagesList.innerHTML = '';
  welcomeCard.style.display = 'block';
  renderSessions();
}

async function handleSend() {
  const text = messageInput.value.trim();
  if (!text) return;

  welcomeCard.style.display = 'none';

  // Add user message to UI
  addMessage('user', text);
  messages.push({ role: 'user', text });

  messageInput.value = '';
  autoResizeTextarea();
  sendBtn.disabled = true;

  // Show typing indicator
  typingIndicator.style.display = 'flex';
  scrollToBottom();

  // Prepare system instruction
  let effectiveInstruction = personalityPrompts[settings.personality] || personalityPrompts.balanced;
  if (settings.customInstructions) {
    effectiveInstruction += `\n\nƏlavə Təlimat: ${settings.customInstructions}`;
  }

  try {
    const res = await fetch('/api/chat', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        messages: messages, // Send full conversation context
        systemInstruction: effectiveInstruction,
        temperature: settings.temperature
      })
    });

    const data = await res.json();
    typingIndicator.style.display = 'none';

    if (!res.ok) {
      addMessage('ai', data.error || 'Əlaqə xətası baş verdi.', true);
      return;
    }

    addMessage('ai', data.reply);
    messages.push({ role: 'model', text: data.reply });
    saveCurrentSession();
  } catch (err) {
    typingIndicator.style.display = 'none';
    addMessage('ai', 'İnternet və ya server bağlantısı tapılmadı. Zəhmət olmasa yenidən cəhd edin.', true);
  } finally {
    scrollToBottom();
  }
}

function addMessage(role, text, isError = false) {
  const row = document.createElement('div');
  row.className = `message-row ${role}`;

  const time = new Date().toLocaleTimeString('az-AZ', { hour: '2-digit', minute: '2-digit' });

  if (role === 'ai') {
    const avatar = document.createElement('div');
    avatar.className = 'ai-avatar';
    avatar.textContent = '✨';
    row.appendChild(avatar);
  }

  const bubbleWrap = document.createElement('div');
  bubbleWrap.className = 'bubble-wrap';

  const bubble = document.createElement('div');
  bubble.className = `message-bubble ${isError ? 'error-bubble' : ''}`;
  bubble.innerHTML = formatMarkdown(text);

  const footer = document.createElement('div');
  footer.className = 'message-footer';
  footer.innerHTML = `<span>${time}</span>`;

  if (role === 'ai' && !isError) {
    const copyBtn = document.createElement('button');
    copyBtn.className = 'copy-btn';
    copyBtn.textContent = 'Kopyala';
    copyBtn.addEventListener('click', () => {
      navigator.clipboard.writeText(text);
      copyBtn.textContent = 'Kopyalandı! ✓';
      setTimeout(() => { copyBtn.textContent = 'Kopyala'; }, 2000);
    });
    footer.appendChild(copyBtn);
  }

  bubbleWrap.appendChild(bubble);
  bubbleWrap.appendChild(footer);
  row.appendChild(bubbleWrap);

  messagesList.appendChild(row);
  scrollToBottom();
}

function formatMarkdown(text) {
  let escaped = text
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;");

  // Bold **text**
  escaped = escaped.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>');

  // Code blocks ```code```
  escaped = escaped.replace(/```([\s\S]*?)```/g, '<pre><code>$1</code></pre>');

  // Inline code `code`
  escaped = escaped.replace(/`([^`]+)`/g, '<code>$1</code>');

  // Line breaks
  escaped = escaped.replace(/\n/g, '<br>');

  return escaped;
}

function scrollToBottom() {
  chatContainer.scrollTop = chatContainer.scrollHeight;
}

function saveCurrentSession() {
  if (messages.length === 0) return;

  const firstUserMsg = messages.find(m => m.role === 'user')?.text || 'Yeni Söhbət';
  const title = firstUserMsg.length > 28 ? firstUserMsg.substring(0, 28) + '...' : firstUserMsg;

  const existingIndex = sessions.findIndex(s => s.id === currentSessionId);
  const sessionData = {
    id: currentSessionId,
    title,
    messages,
    updatedAt: Date.now()
  };

  if (existingIndex >= 0) {
    sessions[existingIndex] = sessionData;
  } else {
    sessions.unshift(sessionData);
  }

  localStorage.setItem('suph_sessions', JSON.stringify(sessions));
  renderSessions();
}

function renderSessions() {
  if (!sessionsList) return;
  sessionsList.innerHTML = '';

  if (sessions.length === 0) {
    sessionsList.innerHTML = '<div style="font-size: 0.8rem; color: var(--text-muted); padding: 8px;">Saxlanılmış söhbət yoxdur</div>';
    return;
  }

  sessions.forEach(sess => {
    const item = document.createElement('div');
    item.className = `session-item ${sess.id === currentSessionId ? 'active' : ''}`;
    item.innerHTML = `
      <span class="session-title">${sess.title}</span>
      <button class="delete-session-btn" title="Sil">✕</button>
    `;

    item.addEventListener('click', (e) => {
      if (e.target.classList.contains('delete-session-btn')) {
        e.stopPropagation();
        deleteSession(sess.id);
        return;
      }
      loadSession(sess);
    });

    sessionsList.appendChild(item);
  });
}

function loadSession(session) {
  saveCurrentSession();
  currentSessionId = session.id;
  messages = session.messages || [];

  messagesList.innerHTML = '';
  welcomeCard.style.display = 'none';

  messages.forEach(msg => {
    addMessage(msg.role === 'user' ? 'user' : 'ai', msg.text);
  });

  renderSessions();
}

function deleteSession(id) {
  sessions = sessions.filter(s => s.id !== id);
  localStorage.setItem('suph_sessions', JSON.stringify(sessions));
  if (currentSessionId === id) {
    startNewChat();
  } else {
    renderSessions();
  }
}

function applySettingsToUI() {
  const radio = document.querySelector(`input[name="personality"][value="${settings.personality}"]`);
  if (radio) radio.checked = true;

  customInstructionsInput.value = settings.customInstructions || '';
  tempSlider.value = settings.temperature || 0.7;
  tempValue.textContent = settings.temperature || 0.7;
}

// Start
init();
