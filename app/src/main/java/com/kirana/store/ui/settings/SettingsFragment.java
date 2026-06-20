package com.kirana.store.ui.settings;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.mlkit.common.MlKitException;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.kirana.store.BuildConfig;
import com.kirana.store.R;
import com.kirana.store.ai.GeminiConfig;
import com.kirana.store.ai.KiranaAiAgent;
import com.kirana.store.databinding.FragmentSettingsBinding;
import com.kirana.store.util.AppPreferences;

import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Settings screen — accessed via the gear icon on the Dashboard header.
 * <p>
 * Provides runtime configuration for the Gemini API key, voice/Scanner toggles,
 * connection diagnostics, and about info. Changes take effect immediately.
 */
public class SettingsFragment extends Fragment {

    private static final String TAG = "SettingsFragment";

    private FragmentSettingsBinding binding;
    private AppPreferences prefs;
    private final ExecutorService diagExecutor = Executors.newSingleThreadExecutor();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        prefs = AppPreferences.get(requireContext());

        refreshKeyStatus();
        setupKeyControls();
        setupToggles();
        setupDiagnostics();
        showAbout();
    }

    // ── Key status display ───────────────────────────────────────────────────

    private void refreshKeyStatus() {
        String masked = GeminiConfig.maskedKey(requireContext());
        String source = GeminiConfig.keySource(requireContext());
        binding.textKeyStatus.setText("Effective key: " + masked + "\nSource: " + source);
        binding.editApiKey.setText(prefs.getGeminiKeyOverride() != null
            ? prefs.getGeminiKeyOverride() : "");
    }

    // ── Key controls ────────────────────────────────────────────────────────

    private void setupKeyControls() {
        binding.btnSaveKey.setOnClickListener(v -> {
            String key = binding.editApiKey.getText().toString().trim();
            if (key.isEmpty()) {
                binding.editApiKey.setError("Key cannot be empty");
                return;
            }
            if (!key.startsWith("AIza") && !key.startsWith("AQ.")) {
                binding.editApiKey.setError("Key should start with AIza or AQ.");
                return;
            }
            prefs.setGeminiKeyOverride(key);
            // Rebuild the Gemini FirebaseApp so the new key takes effect immediately.
            try {
                GeminiConfig.ensureGeminiApp(requireContext());
            } catch (IllegalStateException e) {
                Log.e(TAG, "Rebuild after key save failed", e);
            }
            refreshKeyStatus();
            showAbout();
            Toast.makeText(requireContext(), "✅ Gemini key saved", Toast.LENGTH_SHORT).show();
        });

        binding.btnClearKey.setOnClickListener(v -> {
            prefs.setGeminiKeyOverride(null);
            refreshKeyStatus();
            showAbout();
            Toast.makeText(requireContext(), "Key override cleared — using BuildConfig",
                Toast.LENGTH_SHORT).show();
        });

        binding.btnTestConnection.setOnClickListener(v -> testGeminiConnection());
    }

    private void testGeminiConnection() {
        binding.textConnectionResult.setText("Testing…");
        binding.textConnectionResult.setTextColor(
            requireContext().getColor(R.color.text_secondary));

        if (!GeminiConfig.isConfigured(requireContext())) {
            binding.textConnectionResult.setText("❌ No Gemini key configured");
            binding.textConnectionResult.setTextColor(
                requireContext().getColor(R.color.color_error));
            return;
        }

        try {
            KiranaAiAgent agent = new KiranaAiAgent(requireContext());
            agent.parseVoiceCommand("ping", Collections.emptyList(),
                new KiranaAiAgent.ParseCallback() {
                    @Override
                    public void onSuccess(KiranaAiAgent.ParsedCommand cmd) {
                        requireActivity().runOnUiThread(() -> {
                            binding.textConnectionResult.setText(
                                "✅ Gemini reachable — model responded");
                            binding.textConnectionResult.setTextColor(
                                requireContext().getColor(R.color.color_success));
                        });
                    }
                    @Override
                    public void onError(Exception e) {
                        requireActivity().runOnUiThread(() -> {
                            binding.textConnectionResult.setText(
                                "❌ Gemini error: " + e.getMessage());
                            binding.textConnectionResult.setTextColor(
                                requireContext().getColor(R.color.color_error));
                        });
                    }
                });
        } catch (Exception e) {
            binding.textConnectionResult.setText("❌ Init failed: " + e.getMessage());
            binding.textConnectionResult.setTextColor(
                requireContext().getColor(R.color.color_error));
        }
    }

    // ── Feature toggles ──────────────────────────────────────────────────────

    private void setupToggles() {
        binding.switchVoiceAi.setChecked(prefs.isVoiceAiEnabled());
        binding.switchScanner.setChecked(prefs.isScannerEnabled());

        binding.switchVoiceAi.setOnCheckedChangeListener((btn, checked) -> {
            prefs.setVoiceAiEnabled(checked);
            Toast.makeText(requireContext(),
                checked ? "Voice AI enabled" : "Voice AI disabled",
                Toast.LENGTH_SHORT).show();
        });

        binding.switchScanner.setOnCheckedChangeListener((btn, checked) -> {
            prefs.setScannerEnabled(checked);
            Toast.makeText(requireContext(),
                checked ? "Scanner enabled" : "Scanner disabled",
                Toast.LENGTH_SHORT).show();
        });
    }

    // ── Diagnostics ─────────────────────────────────────────────────────────

    private void setupDiagnostics() {
        // Play Services status
        int psStatus = GoogleApiAvailability.getInstance()
            .isGooglePlayServicesAvailable(requireContext());
        if (psStatus == ConnectionResult.SUCCESS) {
            binding.textPlayServices.setText("Google Play Services: ✅ Available");
        } else {
            String err = GoogleApiAvailability.getInstance().getErrorString(psStatus);
            binding.textPlayServices.setText("Google Play Services: ❌ " + err);
        }

        // OCR model check
        binding.btnCheckOcr.setOnClickListener(v -> checkOcrModel());
    }

    private void checkOcrModel() {
        binding.textOcrStatus.setText("Checking OCR model…");
        binding.textOcrStatus.setTextColor(
            requireContext().getColor(R.color.text_secondary));

        diagExecutor.execute(() -> {
            try {
                TextRecognizer recognizer =
                    TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

                // Create a tiny 1×1 white bitmap to test model availability.
                android.graphics.Bitmap bmp = android.graphics.Bitmap.createBitmap(
                    1, 1, android.graphics.Bitmap.Config.ARGB_8888);
                InputImage img = InputImage.fromBitmap(bmp, 0);

                recognizer.process(img)
                    .addOnSuccessListener(text -> requireActivity().runOnUiThread(() -> {
                        binding.textOcrStatus.setText("✅ OCR model ready");
                        binding.textOcrStatus.setTextColor(
                            requireContext().getColor(R.color.color_success));
                        recognizer.close();
                    }))
                    .addOnFailureListener(e -> requireActivity().runOnUiThread(() -> {
                        if (e instanceof MlKitException) {
                            int code = ((MlKitException) e).getErrorCode();
                            if (code == 14) {
                                binding.textOcrStatus.setText(
                                    "⏳ OCR model downloading… try again in a minute");
                            } else {
                                binding.textOcrStatus.setText(
                                    "❌ OCR model error (code " + code + "): " + e.getMessage());
                            }
                        } else {
                            binding.textOcrStatus.setText("❌ " + e.getMessage());
                        }
                        binding.textOcrStatus.setTextColor(
                            requireContext().getColor(R.color.color_error));
                    }));
            } catch (Exception e) {
                requireActivity().runOnUiThread(() -> {
                    binding.textOcrStatus.setText("❌ Check failed: " + e.getMessage());
                    binding.textOcrStatus.setTextColor(
                        requireContext().getColor(R.color.status_error));
                });
            }
        });
    }

    // ── About ───────────────────────────────────────────────────────────────

    private void showAbout() {
        binding.textAboutVersion.setText(
            "Kirana Store Manager v" + BuildConfig.VERSION_NAME);
        binding.textAboutBackend.setText(
            "AI Backend: Gemini Developer API (googleAI)");
        binding.textAboutKeySource.setText(
            "Key source: " + GeminiConfig.keySource(requireContext()));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        diagExecutor.shutdown();
        binding = null;
    }
}
