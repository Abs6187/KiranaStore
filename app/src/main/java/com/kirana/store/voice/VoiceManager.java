package com.kirana.store.voice;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Manages both Speech-to-Text (STT) and Text-to-Speech (TTS) for the Kirana voice interface.
 * <p>
 * Uses Android's native {@link SpeechRecognizer} (on-device, free, private) for STT and
 * Android's {@link TextToSpeech} engine for TTS – zero cloud cost.
 * <p>
 * Supports Hindi ("hi-IN") and English ("en-IN") recognition with auto-fallback.
 * <p>
 * Lifecycle: call {@link #init(Context)} once (Activity onCreate), {@link #release()} in onDestroy.
 */
public class VoiceManager {

    private static final String TAG = "VoiceManager";

    private SpeechRecognizer speechRecognizer;
    private TextToSpeech tts;
    private boolean ttsReady = false;

    private VoiceCallback callback;

    public interface VoiceCallback {
        /** Called when STT produces a partial or final result. */
        void onSpeechResult(String text, boolean isFinal);

        /** Called when STT starts actively listening (after beep). */
        void onListeningStarted();

        /** Called when STT finishes / times out. */
        void onListeningStopped();

        /** Called on any STT/TTS error. */
        void onError(String errorMessage);
    }

    public VoiceManager(VoiceCallback callback) {
        this.callback = callback;
    }

    /**
     * Initialise STT and TTS engines. Call from Activity/Fragment onCreate.
     */
    public void init(Context context) {
        // ── TTS Setup ──────────────────────────────────────────────────────────
        tts = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS) {
                // Prefer Indian English accent; fall back to generic English
                int result = tts.setLanguage(new Locale("en", "IN"));
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts.setLanguage(Locale.ENGLISH);
                }
                tts.setSpeechRate(0.95f);
                tts.setPitch(1.05f);
                ttsReady = true;
            } else {
                Log.e(TAG, "TTS initialisation failed: " + status);
            }
        });

        // ── STT Setup ──────────────────────────────────────────────────────────
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            Log.e(TAG, "Speech recognition not available on this device");
            return;
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override public void onReadyForSpeech(Bundle params) {
                if (callback != null) callback.onListeningStarted();
            }
            @Override public void onBeginningOfSpeech() {}
            @Override public void onRmsChanged(float rmsdB) {}
            @Override public void onBufferReceived(byte[] buffer) {}
            @Override public void onEndOfSpeech() {
                if (callback != null) callback.onListeningStopped();
            }
            @Override public void onError(int error) {
                String msg = sttErrorToString(error);
                Log.e(TAG, "STT error: " + msg);
                if (callback != null) callback.onError(msg);
            }
            @Override public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(
                    SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    if (callback != null) callback.onSpeechResult(matches.get(0), true);
                }
            }
            @Override public void onPartialResults(Bundle partialResults) {
                ArrayList<String> matches = partialResults.getStringArrayList(
                    SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    if (callback != null) callback.onSpeechResult(matches.get(0), false);
                }
            }
            @Override public void onEvent(int eventType, Bundle params) {}
        });
    }

    /**
     * Start listening for a voice command.
     * Recognises Hindi-English mixed (Hinglish) speech.
     */
    public void startListening() {
        if (speechRecognizer == null) return;
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        // Support both Hindi and Indian English
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN");
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "hi-IN,en-IN");
        intent.putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, false);
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1500L);
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 500L);
        speechRecognizer.startListening(intent);
    }

    public void stopListening() {
        if (speechRecognizer != null) speechRecognizer.stopListening();
    }

    /**
     * Speak text aloud using TTS. Interrupts any current speech.
     *
     * @param text text to speak
     */
    public void speak(String text) {
        if (ttsReady && tts != null) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "kirana_tts");
        }
    }

    public void stopSpeaking() {
        if (tts != null) tts.stop();
    }

    /** Release all resources. Call from onDestroy. */
    public void release() {
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
            speechRecognizer = null;
        }
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
        }
    }

    /** Maps an STT error code to a human-readable message. Package-private for testing. */
    static String sttErrorToString(int error) {
        switch (error) {
            case SpeechRecognizer.ERROR_AUDIO:           return "Audio recording error";
            case SpeechRecognizer.ERROR_CLIENT:          return "Client side error";
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS: return "Microphone permission denied";
            case SpeechRecognizer.ERROR_NETWORK:         return "Network error";
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT: return "Network timeout";
            case SpeechRecognizer.ERROR_NO_MATCH:        return "No speech match found";
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY: return "Recogniser busy";
            case SpeechRecognizer.ERROR_SERVER:          return "Server error";
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:  return "No speech detected";
            default:                                     return "Unknown STT error (" + error + ")";
        }
    }
}
