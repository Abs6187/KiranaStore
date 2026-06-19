package com.kirana.store.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Application-wide preferences stored in {@link SharedPreferences}.
 * <p>
 * Holds runtime-tunable settings (Gemini key override, voice/Scanner toggles) so
 * they take effect immediately without rebuilding. The Gemini key override is
 * stored in plaintext — noted for future AES encryption if the project matures.
 * <p>
 * Singleton via {@link #get(Context)}; initialised lazily on first access.
 */
public final class AppPreferences {

    private static final String PREFS_NAME = "kirana_store_prefs";

    private static volatile AppPreferences instance;

    private final SharedPreferences prefs;

    // ── Keys ──────────────────────────────────────────────────────────────────
    private static final String KEY_GEMINI_OVERRIDE   = "gemini_key_override";
    private static final String KEY_VOICE_AI_ENABLED   = "voice_ai_enabled";
    private static final String KEY_SCANNER_ENABLED     = "scanner_enabled";

    // ── Defaults ───────────────────────────────────────────────────────────────
    private static final boolean DEFAULT_VOICE_AI   = true;
    private static final boolean DEFAULT_SCANNER    = true;

    private AppPreferences(@NonNull Context context) {
        prefs = context.getApplicationContext()
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /** Returns the singleton; prefers the application context to avoid leaks. */
    @NonNull
    public static AppPreferences get(@NonNull Context context) {
        if (instance == null) {
            synchronized (AppPreferences.class) {
                if (instance == null) {
                    instance = new AppPreferences(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    /** Convenience overload for callers that already have the singleton (e.g. GeminiConfig). */
    @NonNull
    public static AppPreferences get() {
        if (instance == null) {
            throw new IllegalStateException(
                "AppPreferences not initialised. Call get(Context) first.");
        }
        return instance;
    }

    // ── Gemini key override ──────────────────────────────────────────────────

    /** The runtime Gemini key override, or {@code null} if none set. */
    @Nullable
    public String getGeminiKeyOverride() {
        return prefs.getString(KEY_GEMINI_OVERRIDE, null);
    }

    /** Persist a Gemini key override. Pass {@code null} to clear. */
    public void setGeminiKeyOverride(@Nullable String key) {
        if (key == null) {
            prefs.edit().remove(KEY_GEMINI_OVERRIDE).apply();
        } else {
            prefs.edit().putString(KEY_GEMINI_OVERRIDE, key).apply();
        }
    }

    // ── Feature toggles ──────────────────────────────────────────────────────

    public boolean isVoiceAiEnabled() {
        return prefs.getBoolean(KEY_VOICE_AI_ENABLED, DEFAULT_VOICE_AI);
    }

    public void setVoiceAiEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_VOICE_AI_ENABLED, enabled).apply();
    }

    public boolean isScannerEnabled() {
        return prefs.getBoolean(KEY_SCANNER_ENABLED, DEFAULT_SCANNER);
    }

    public void setScannerEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_SCANNER_ENABLED, enabled).apply();
    }

    // ── Bulk operations ──────────────────────────────────────────────────────

    /** Clear all preferences (used for diagnostics / reset). */
    public void clearAll() {
        prefs.edit().clear().apply();
    }
}
