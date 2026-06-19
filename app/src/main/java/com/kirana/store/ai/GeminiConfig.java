package com.kirana.store.ai;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.kirana.store.BuildConfig;
import com.kirana.store.util.AppPreferences;

/**
 * Wires the Gemini Developer API key into the Firebase AI Logic SDK.
 * <p>
 * <b>Why this exists.</b> The default {@code FirebaseAI.getInstance()} call resolves to the
 * <i>Vertex AI</i> backend, which requires a Firebase project on the Blaze (paid) plan plus
 * App Check — it does <b>not</b> use a free Gemini API key. The free-tier path is the
 * <i>googleAI</i> backend, and that backend reads its key from the configured Firebase
 * {@link FirebaseApp}.
 * <p>
 * The checked-in {@code google-services.json} contains a Firebase Web API key (for
 * Analytics/Firestore) but no Gemini key. Per the Firebase multi-app pattern, we therefore
 * initialise a <b>secondary {@link FirebaseApp}</b> named {@value #GEMINI_APP_NAME} whose
 * {@link FirebaseOptions#getApiKey()} is the Gemini key. This lets the user override the key
 * at runtime from the Settings screen without touching {@code google-services.json}.
 *
 * @see <a href="https://firebase.google.com/docs/ai-logic/get-started">Firebase AI Logic get-started</a>
 */
public final class GeminiConfig {

    /** Name of the secondary FirebaseApp used purely for the Gemini key. */
    public static final String GEMINI_APP_NAME = "gemini";

    /** Placeholder values that should be treated as "no key configured". */
    private static final String PLACEHOLDER_DEFAULT = "YOUR_GEMINI_API_KEY";
    private static final String PLACEHOLDER_LOCAL = "YOUR_GEMINI_API_KEY_HERE";

    private GeminiConfig() { /* utility */ }

    /**
     * Returns the secondary Gemini {@link FirebaseApp}, creating it on first call.
     * The key is resolved from {@link AppPreferences} (runtime override) first, falling back
     * to {@link BuildConfig#GEMINI_API_KEY} (from {@code local.properties}).
     * <p>
     * If the runtime key has changed since the app was created, the old instance is deleted
     * and rebuilt so the new key takes effect.
     *
     * @throws IllegalStateException if no usable key is configured
     */
    @NonNull
    public static synchronized FirebaseApp ensureGeminiApp(@NonNull Context context) {
        String key = resolveKey(context);

        // Look for an existing secondary app.
        for (FirebaseApp app : FirebaseApp.getApps(context)) {
            if (GEMINI_APP_NAME.equals(app.getName())) {
                FirebaseOptions opts = app.getOptions();
                if (opts != null && key.equals(opts.getApiKey())) {
                    return app; // unchanged key → reuse
                }
                app.delete(); // key changed → rebuild below
                break;
            }
        }
        return createGeminiApp(context, key);
    }

    /** True if a non-placeholder Gemini key is available (runtime override or BuildConfig). */
    public static boolean isConfigured(@NonNull Context context) {
        return isUsableKey(AppPreferences.get(context).getGeminiKeyOverride())
            || isUsableKey(BuildConfig.GEMINI_API_KEY);
    }

    /** The key that would actually be used right now, or {@code null} if none. */
    @Nullable
    public static String effectiveKey(@NonNull Context context) {
        String override = AppPreferences.get(context).getGeminiKeyOverride();
        if (isUsableKey(override)) return override;
        if (isUsableKey(BuildConfig.GEMINI_API_KEY)) return BuildConfig.GEMINI_API_KEY;
        return null;
    }

    /** Mask the key for display, e.g. {@code "AQ.Ab…svA"}. Returns "not set" if absent. */
    @NonNull
    public static String maskedKey(@NonNull Context context) {
        String key = effectiveKey(context);
        if (key == null) return "not set";
        if (key.length() <= 8) return "••••";
        return key.substring(0, 5) + "…" + key.substring(key.length() - 3);
    }

    /**
     * Returns the source of the effective key, for the Settings "About" section.
     * @return "runtime override", "BuildConfig (local.properties)", or "not set"
     */
    @NonNull
    public static String keySource(@NonNull Context context) {
        if (isUsableKey(AppPreferences.get(context).getGeminiKeyOverride())) return "runtime override";
        if (isUsableKey(BuildConfig.GEMINI_API_KEY)) return "BuildConfig (local.properties)";
        return "not set";
    }

    // ── internals ─────────────────────────────────────────────────────────────

    @NonNull
    private static String resolveKey(@NonNull Context context) {
        String override = AppPreferences.get(context).getGeminiKeyOverride();
        if (isUsableKey(override)) return override;
        if (isUsableKey(BuildConfig.GEMINI_API_KEY)) return BuildConfig.GEMINI_API_KEY;
        throw new IllegalStateException(
            "No Gemini API key configured. Add one in Settings or local.properties.");
    }

    @NonNull
    private static FirebaseApp createGeminiApp(@NonNull Context context, @NonNull String key) {
        FirebaseOptions options = new FirebaseOptions.Builder()
            .setApiKey(key)
            .setApplicationId(BuildConfig.APPLICATION_ID)
            .setProjectId("kirana-gemini") // arbitrary; the googleAI backend only needs the key
            .build();
        return FirebaseApp.initializeApp(context.getApplicationContext(), options, GEMINI_APP_NAME);
    }

    private static boolean isUsableKey(@Nullable String key) {
        if (TextUtils.isEmpty(key)) return false;
        return !PLACEHOLDER_DEFAULT.equals(key) && !PLACEHOLDER_LOCAL.equals(key);
    }
}
