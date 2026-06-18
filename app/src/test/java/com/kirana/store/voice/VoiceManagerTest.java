package com.kirana.store.voice;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Unit tests for {@link VoiceManager#sttErrorToString(int)}.
 * <p>
 * Uses the raw {@code int} error codes from {@link android.speech.SpeechRecognizer}
 * (compile-time constants inlined by javac) rather than the symbolic references, so
 * these run on a plain JVM without an Android runtime on the classpath.
 */
public class VoiceManagerTest {

    // SpeechRecognizer.ERROR_* raw constant values (stable across Android versions)
    private static final int ERROR_AUDIO                = 3;
    private static final int ERROR_CLIENT               = 5;
    private static final int ERROR_INSUFFICIENT_PERMS   = 9;
    private static final int ERROR_NETWORK              = 2;
    private static final int ERROR_NETWORK_TIMEOUT      = 1;
    private static final int ERROR_NO_MATCH             = 7;
    private static final int ERROR_RECOGNIZER_BUSY      = 8;
    private static final int ERROR_SERVER               = 4;
    private static final int ERROR_SPEECH_TIMEOUT       = 6;

    @Test
    public void audioError_mapsToReadableMessage() {
        assertEquals("Audio recording error",
            VoiceManager.sttErrorToString(ERROR_AUDIO));
    }

    @Test
    public void clientError_mapsToReadableMessage() {
        assertEquals("Client side error",
            VoiceManager.sttErrorToString(ERROR_CLIENT));
    }

    @Test
    public void insufficientPermissions_mapsToReadableMessage() {
        assertEquals("Microphone permission denied",
            VoiceManager.sttErrorToString(ERROR_INSUFFICIENT_PERMS));
    }

    @Test
    public void networkError_mapsToReadableMessage() {
        assertEquals("Network error",
            VoiceManager.sttErrorToString(ERROR_NETWORK));
    }

    @Test
    public void networkTimeout_mapsToReadableMessage() {
        assertEquals("Network timeout",
            VoiceManager.sttErrorToString(ERROR_NETWORK_TIMEOUT));
    }

    @Test
    public void noMatch_mapsToReadableMessage() {
        assertEquals("No speech match found",
            VoiceManager.sttErrorToString(ERROR_NO_MATCH));
    }

    @Test
    public void recognizerBusy_mapsToReadableMessage() {
        assertEquals("Recogniser busy",
            VoiceManager.sttErrorToString(ERROR_RECOGNIZER_BUSY));
    }

    @Test
    public void serverError_mapsToReadableMessage() {
        assertEquals("Server error",
            VoiceManager.sttErrorToString(ERROR_SERVER));
    }

    @Test
    public void speechTimeout_mapsToReadableMessage() {
        assertEquals("No speech detected",
            VoiceManager.sttErrorToString(ERROR_SPEECH_TIMEOUT));
    }

    @Test
    public void unknownCode_mapsToGenericMessageIncludingCode() {
        String msg = VoiceManager.sttErrorToString(999);
        assertEquals("Unknown STT error (999)", msg);
    }
}
