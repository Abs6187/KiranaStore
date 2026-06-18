package com.kirana.store.ai;

import android.util.Log;

import com.google.firebase.Firebase;
import com.google.firebase.ai.FirebaseAI;
import com.google.firebase.ai.GenerativeModel;
import com.google.firebase.ai.java.GenerativeModelFutures;
import com.google.firebase.ai.type.Content;
import com.google.firebase.ai.type.GenerateContentResponse;
import com.google.firebase.ai.type.GenerationConfig;
import com.google.firebase.ai.type.RequestOptions;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Kirana AI Agent powered by Google Gemini 2.0 Flash via Firebase AI Logic SDK.
 * <p>
 * ⚠️ Migration notice:
 *   The legacy {@code com.google.ai.client.generativeai:0.9.0} SDK reached
 *   end-of-life on November 30, 2025. This class now uses the official
 *   replacement: {@code com.google.firebase:firebase-ai} (Firebase AI Logic),
 *   managed by Firebase BOM 34.15.0.
 * <p>
 * The SDK is configured to use the "googleai" developer provider (free-tier
 * Gemini API key from AI Studio) rather than Vertex AI, keeping costs at ₹0.
 * <p>
 * Primary use-case: parse a natural-language voice command into a structured
 * product name + price update command.
 * <p>
 * Example input : "Mustard oil ki price 175 rupees kar do"
 * Example output: {"action":"update_price","product":"Mustard Oil","price":175.0}
 * <p>
 * API key is stored in local.properties as {@code GEMINI_API_KEY=...} and
 * injected via BuildConfig through the secrets-gradle-plugin.
 */
public class KiranaAiAgent {

    private static final String TAG = "KiranaAiAgent";
    private static final String MODEL_NAME = "gemini-2.0-flash";

    private final GenerativeModelFutures model;
    private final Executor executor = Executors.newSingleThreadExecutor();

    private static final String SYSTEM_PROMPT =
        "You are an assistant for a Kirana (Indian grocery) store manager. " +
        "Your job is to parse voice commands and return ONLY a valid JSON object. " +
        "The JSON must have these fields:\n" +
        "  action: \"update_price\" | \"add_product\" | \"query_price\" | \"unknown\"\n" +
        "  product: the product name (capitalised, in English)\n" +
        "  price: a number (price in Indian Rupees) – null if not applicable\n" +
        "  unit: unit of measure (e.g. kg, litre, packet) – null if not mentioned\n" +
        "  acknowledgement: a SHORT, friendly confirmation sentence in English (max 10 words)\n" +
        "\n" +
        "Examples:\n" +
        "Input: 'mustard oil ka price 175 rupee karo'\n" +
        "Output: {\"action\":\"update_price\",\"product\":\"Mustard Oil\",\"price\":175,\"unit\":null," +
        "\"acknowledgement\":\"Updated Mustard Oil to ₹175.\"}\n" +
        "\n" +
        "Input: 'atta 5 kg 280 rupees add karo'\n" +
        "Output: {\"action\":\"add_product\",\"product\":\"Atta 5kg\",\"price\":280,\"unit\":\"5kg\"," +
        "\"acknowledgement\":\"Added Atta 5kg at ₹280.\"}\n" +
        "\n" +
        "Respond ONLY with the JSON object. No markdown. No extra text.";

    /**
     * Initialise the agent using Firebase AI Logic (googleai provider = free Gemini API key).
     * The {@code apiKey} parameter is kept for interface compatibility but the Firebase SDK
     * secures it internally via App Check when configured.
     */
    public KiranaAiAgent(String apiKey) {
        // Firebase AI Logic – googleai provider (free tier, API key secured via App Check)
        GenerativeModel generativeModel = FirebaseAI.getInstance()
            .generativeModel(MODEL_NAME);
        model = GenerativeModelFutures.from(generativeModel);
    }

    /**
     * Parse a raw voice transcript into a structured {@link ParsedCommand}.
     *
     * @param voiceTranscript raw STT text (Hindi/Hinglish/English supported)
     * @param productNames    list of existing product names for context
     * @param callback        result callback
     */
    public void parseVoiceCommand(
        String voiceTranscript,
        List<String> productNames,
        ParseCallback callback
    ) {
        String productContext = productNames.isEmpty()
            ? ""
            : "\nKnown products in the store: " + String.join(", ", productNames) + ".";

        String userPrompt = SYSTEM_PROMPT + productContext +
            "\n\nVoice command: \"" + voiceTranscript + "\"";

        Content content = new Content.Builder()
            .addText(userPrompt)
            .build();

        ListenableFuture<GenerateContentResponse> future = model.generateContent(content);

        Futures.addCallback(future, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                try {
                    String rawJson = result.getText();
                    if (rawJson == null || rawJson.isBlank()) {
                        callback.onError(new Exception("Empty AI response"));
                        return;
                    }
                    ParsedCommand cmd = parseResponseJson(rawJson, voiceTranscript);
                    callback.onSuccess(cmd);
                } catch (Exception e) {
                    Log.e(TAG, "JSON parse error: " + e.getMessage());
                    callback.onError(e);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e(TAG, "AI call failed: " + t.getMessage());
                callback.onError(new Exception(t));
            }
        }, executor);
    }

    // ── JSON parsing (pure logic, unit-testable) ──────────────────────────────

    /**
     * Parse the raw text returned by Gemini into a {@link ParsedCommand}.
     * <p>
     * Strips any Markdown JSON code-fencing and tolerates a missing {@code price}
     * field (resolved to {@code -1}). Exposed at package visibility for testing.
     *
     * @param rawJson      the raw model output
     * @param transcript   the original voice transcript (stored on the result)
     * @return the parsed command
     * @throws org.json.JSONException if the text is not valid JSON
     * @throws IllegalArgumentException if the text is null/blank
     */
    static ParsedCommand parseResponseJson(String rawJson, String transcript) throws org.json.JSONException {
        if (rawJson == null || rawJson.isBlank()) {
            throw new IllegalArgumentException("Empty AI response");
        }
        // Strip potential markdown fencing
        rawJson = rawJson.trim()
            .replaceAll("^```json\\s*", "")
            .replaceAll("^```\\s*", "")
            .replaceAll("\\s*```$", "");

        JSONObject json = new JSONObject(rawJson);
        ParsedCommand cmd = new ParsedCommand();
        cmd.action = json.optString("action", "unknown");
        cmd.product = json.optString("product", "");
        cmd.price = json.isNull("price") ? -1 : json.optDouble("price", -1);
        cmd.unit = json.optString("unit", "");
        cmd.acknowledgement = json.optString("acknowledgement",
            "Done. " + cmd.product + " updated.");
        cmd.rawTranscript = transcript;
        return cmd;
    }

    // ── Data classes ─────────────────────────────────────────────────────────

    public static class ParsedCommand {
        public String action;           // "update_price", "add_product", "query_price", "unknown"
        public String product;          // canonical product name
        public double price;            // ₹ value, -1 if not specified
        public String unit;             // unit of measure
        public String acknowledgement;  // TTS readback sentence
        public String rawTranscript;    // original voice text
    }

    public interface ParseCallback {
        void onSuccess(ParsedCommand command);
        void onError(Exception e);
    }
}
