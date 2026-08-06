/**
 * Web Speech API Service for Voice STT & TTS
 */

const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;

export const isSpeechSupported = () => {
  return Boolean(SpeechRecognition);
};

let recognitionInstance = null;

export const startListening = ({ onResult, onError, onStart, onEnd, lang = "hi-IN" }) => {
  if (!SpeechRecognition) {
    if (onError) onError("Browser speech recognition not supported. Use Chrome or Edge.");
    return null;
  }

  if (recognitionInstance) {
    try { recognitionInstance.stop(); } catch (e) {}
  }

  recognitionInstance = new SpeechRecognition();
  recognitionInstance.continuous = false;
  recognitionInstance.interimResults = false;
  recognitionInstance.lang = lang;

  recognitionInstance.onstart = () => {
    if (onStart) onStart();
  };

  recognitionInstance.onresult = (event) => {
    const transcript = event.results[0][0].transcript;
    if (onResult) onResult(transcript);
  };

  recognitionInstance.onerror = (event) => {
    console.warn("Speech recognition error:", event.error);
    if (onError) onError(event.error);
  };

  recognitionInstance.onend = () => {
    if (onEnd) onEnd();
  };

  recognitionInstance.start();
  return recognitionInstance;
};

export const stopListening = () => {
  if (recognitionInstance) {
    try { recognitionInstance.stop(); } catch (e) {}
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
  utterance.rate = 1.0;
  utterance.pitch = 1.0;

  // Try to pick an Indian English or Hindi voice if available
  const voices = window.speechSynthesis.getVoices();
  const indianVoice = voices.find(v => v.lang.includes("hi") || v.lang.includes("IN"));
  if (indianVoice) {
    utterance.voice = indianVoice;
  }

  window.speechSynthesis.speak(utterance);
};
