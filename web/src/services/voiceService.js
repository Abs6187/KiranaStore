/**
 * Web Speech API Service for Voice STT & TTS
 */

const getSpeechRecognition = () => window.SpeechRecognition || window.webkitSpeechRecognition;

export const isSpeechSupported = () => {
  return Boolean(getSpeechRecognition());
};

let activeRecognition = null;
let activeSessionId = 0;

export const startListening = ({ onResult, onError, onStart, onEnd, lang = "hi-IN" }) => {
  const SpeechRecognition = getSpeechRecognition();
  if (!SpeechRecognition) {
    if (onError) onError("Browser speech recognition not supported. Use Chrome or Edge.");
    return null;
  }

  // Cancel any previous recognition and bump session id so stale callbacks are ignored
  const thisSessionId = ++activeSessionId;
  if (activeRecognition) {
    try { activeRecognition.abort(); } catch (e) {}
    activeRecognition = null;
  }

  const recognition = new SpeechRecognition();
  recognition.continuous = false;
  recognition.interimResults = false;
  recognition.lang = lang;

  recognition.onstart = () => {
    if (activeSessionId !== thisSessionId) return;
    if (onStart) onStart();
  };

  recognition.onresult = (event) => {
    if (activeSessionId !== thisSessionId) return;
    const result = event.results?.[event.results.length - 1]?.[0]?.transcript;
    if (result && onResult) onResult(result);
  };

  recognition.onerror = (event) => {
    if (activeSessionId !== thisSessionId) return;
    console.warn("Speech recognition error:", event.error);
    activeRecognition = null;
    if (onError && event.error !== "aborted") onError(event.error);
  };

  recognition.onend = () => {
    if (activeSessionId !== thisSessionId) return;
    if (onEnd) onEnd();
    activeRecognition = null;
  };

  try {
    recognition.start();
    activeRecognition = recognition;
  } catch (err) {
    console.warn("Speech recognition start failed:", err);
    activeRecognition = null;
    if (onError) onError(err.message);
  }

  return recognition;
};

export const stopListening = () => {
  if (activeRecognition) {
    try { activeRecognition.stop(); } catch (e) {}
  }
};

/**
 * Text-To-Speech Readback Feedback
 */
export const speakText = (text, lang = "hi-IN") => {
  if (!window.speechSynthesis) return;

  window.speechSynthesis.cancel(); // Stop any ongoing speech
  const utterance = new SpeechSynthesisUtterance(text);
  utterance.lang = lang;
  utterance.rate = 0.95; // Slightly slower, more natural speed for Indian accents
  utterance.pitch = 1.0;

  const selectBestVoice = () => {
    const voices = window.speechSynthesis.getVoices();
    if (!voices || voices.length === 0) return;

    // Priority 1: Google Hindi or Microsoft Hindi natural voice
    const hindiVoice = voices.find(v => 
      (v.lang.startsWith("hi") || v.lang.includes("hi-IN")) && 
      (v.name.includes("Google") || v.name.includes("Natural") || v.name.includes("Online"))
    ) || voices.find(v => v.lang.startsWith("hi") || v.lang.includes("hi-IN"));

    // Priority 2: Google / Microsoft Indian English natural voice
    const indianEngVoice = voices.find(v => 
      v.lang.includes("IN") || v.lang.includes("en-IN")
    );

    const chosenVoice = hindiVoice || indianEngVoice || voices[0];
    if (chosenVoice) {
      utterance.voice = chosenVoice;
    }
  };

  selectBestVoice();
  if (window.speechSynthesis.getVoices().length === 0) {
    window.speechSynthesis.addEventListener("voiceschanged", selectBestVoice, { once: true });
  }

  window.speechSynthesis.speak(utterance);
};

/**
 * Announce an action aloud when Simple Mode is enabled.
 */
export const announce = (message, enabled = false) => {
  if (enabled && message) {
    speakText(message, "hi-IN");
  }
};
