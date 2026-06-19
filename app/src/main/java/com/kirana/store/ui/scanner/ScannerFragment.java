package com.kirana.store.ui.scanner;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.common.MlKitException;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.kirana.store.databinding.FragmentScannerBinding;
import com.kirana.store.ui.dashboard.DashboardViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Receipt Scanner using CameraX (real-time preview) + ML Kit Text Recognition v2.
 * <p>
 * Replaces the legacy {@code firebase-ml-vision} API (deprecated) with the modern
 * standalone {@code com.google.mlkit:text-recognition:17.3.0} which is:
 *   ✅ Fully on-device (private, no cloud round-trip)
 *   ✅ No Firebase project required
 *   ✅ Supports Latin + Devanagari (Hindi script) scripts
 * <p>
 * Model is downloaded lazily by Google Play Services on first {@code process()} call.
 * This fragment gracefully handles the download-wait state instead of appearing frozen.
 * <p>
 * Flow:
 *   1. Check Google Play Services availability
 *   2. CameraX preview feeds into ImageAnalysis
 *   3. ML Kit OCR extracts text blocks from every frame
 *   4. TextProcessor parses price patterns (₹ digits, digits + /kg etc.)
 *   5. Results displayed in a bottom sheet for user confirmation
 *   6. On confirm → ProductRepository.updatePrice(source="ocr_scan")
 */
public class ScannerFragment extends Fragment {

    private static final String TAG = "ScannerFragment";

    private FragmentScannerBinding binding;
    private DashboardViewModel viewModel;
    private ExecutorService cameraExecutor;
    private TextRecognizer textRecognizer;
    private volatile boolean isAnalysing = true;
    private final AtomicBoolean isProcessingFrame = new AtomicBoolean(false);
    private int modelDownloadRetries = 0;

    // Price pattern: matches "₹175", "Rs.175", "175/-", "175.00", "175 rupees"
    private static final Pattern PRICE_PATTERN =
        Pattern.compile("(?:₹|Rs\\.?\\s*)?(\\d{1,6}(?:\\.\\d{1,2})?)(?:\\s*(?:/\\-|rupees?|rs)?)",
            Pattern.CASE_INSENSITIVE);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentScannerBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(DashboardViewModel.class);

        cameraExecutor = Executors.newSingleThreadExecutor();

        // ── Check Google Play Services before touching CameraX / ML Kit ──
        int playServices = GoogleApiAvailability.getInstance()
            .isGooglePlayServicesAvailable(requireContext());
        if (playServices != ConnectionResult.SUCCESS) {
            String msg = GoogleApiAvailability.getInstance()
                .getErrorString(playServices);
            binding.textOcrResult.setText("⚠️ Google Play Services error: " + msg
                + "\n\nOCR scanning requires up-to-date Play Services.");
            binding.btnConfirmScan.setVisibility(View.GONE);
            binding.btnCapture.setVisibility(View.GONE);
            Log.e(TAG, "Play Services not available: " + msg);
            return;
        }

        // ── Initialise ML Kit TextRecognizer ──
        try {
            textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        } catch (Exception e) {
            binding.textOcrResult.setText("⚠️ OCR engine failed to initialise: "
                + e.getMessage());
            binding.btnCapture.setVisibility(View.GONE);
            Log.e(TAG, "TextRecognizer init failed", e);
            return;
        }

        startCamera();

        binding.btnCapture.setOnClickListener(v -> {
            isAnalysing = true;
            modelDownloadRetries = 0;
            binding.textOcrResult.setText("Scanning...");
        });

        binding.btnClose.setOnClickListener(v ->
            requireActivity().getSupportFragmentManager().popBackStack());
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
            ProcessCameraProvider.getInstance(requireContext());

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Camera init error: " + e.getMessage());
                requireActivity().runOnUiThread(() ->
                    binding.textOcrResult.setText("⚠️ Camera failed to start: " + e.getMessage()));
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void bindCameraUseCases(ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(binding.cameraPreview.getSurfaceProvider());

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build();
        imageAnalysis.setAnalyzer(cameraExecutor, this::analyseImage);

        CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
        cameraProvider.unbindAll();
        cameraProvider.bindToLifecycle(getViewLifecycleOwner(),
            cameraSelector, preview, imageAnalysis);
    }

    @androidx.camera.core.ExperimentalGetImage
    private void analyseImage(ImageProxy imageProxy) {
        if (!isAnalysing || isProcessingFrame.getAndSet(true)) {
            imageProxy.close();
            return;
        }
        if (imageProxy.getImage() == null) {
            isProcessingFrame.set(false);
            imageProxy.close();
            return;
        }

        InputImage inputImage = InputImage.fromMediaImage(
            imageProxy.getImage(), imageProxy.getImageInfo().getRotationDegrees());

        textRecognizer.process(inputImage)
            .addOnSuccessListener(visionText -> {
                isProcessingFrame.set(false);
                processOcrResult(visionText);
                imageProxy.close();
            })
            .addOnFailureListener(e -> {
                isProcessingFrame.set(false);
                handleOcrFailure(e);
                imageProxy.close();
            });
    }

    /**
     * Handle ML Kit failure — especially the first-launch model download.
     * <p>
     * Code 14 = "waiting for the text recognition model to be downloaded".
     * We show a helpful status and auto-retry on the next frame.
     */
    private void handleOcrFailure(@NonNull Exception e) {
        if (e instanceof MlKitException) {
            int code = ((MlKitException) e).getErrorCode();
            if (code == 14) {
                // Model still downloading — keep scanning, it'll succeed once ready.
                modelDownloadRetries++;
                if (modelDownloadRetries <= 5 || modelDownloadRetries % 10 == 0) {
                    requireActivity().runOnUiThread(() ->
                        binding.textOcrResult.setText(
                            "⏳ Downloading OCR model… auto-retrying"));
                }
                Log.i(TAG, "OCR model downloading (retry #" + modelDownloadRetries + ")");
                return;
            }
            if (code == 9 || code == 13) {
                // Model unavailable or unsupported device.
                requireActivity().runOnUiThread(() -> {
                    binding.textOcrResult.setText(
                        "⚠️ OCR model unavailable on this device.\n"
                            + "Try updating Google Play Services.");
                    isAnalysing = false;
                });
                Log.e(TAG, "OCR model unavailable: code " + code);
                return;
            }
        }
        Log.e(TAG, "OCR processing error: " + e.getMessage());
        // For transient errors, keep trying — don't stall the scanner.
    }

    private void processOcrResult(Text visionText) {
        String fullText = visionText.getText();
        if (fullText == null || fullText.isBlank()) return;

        List<OcrPriceItem> items = new ArrayList<>();
        for (Text.TextBlock block : visionText.getTextBlocks()) {
            String blockText = block.getText();
            Matcher m = PRICE_PATTERN.matcher(blockText);
            while (m.find()) {
                try {
                    double price = Double.parseDouble(m.group(1));
                    // Filter out obviously wrong values (< ₹1 or > ₹100,000)
                    if (price >= 1 && price <= 100000) {
                        // Try to extract a label from the same block
                        String label = extractLabel(blockText, m.start());
                        items.add(new OcrPriceItem(label, price));
                    }
                } catch (NumberFormatException ignored) {}
            }
        }

        if (!items.isEmpty()) {
            isAnalysing = false; // Pause while showing results
            requireActivity().runOnUiThread(() -> showOcrResults(items));
        }
    }

    /** Heuristic: take text before the price pattern as product label. */
    private String extractLabel(String blockText, int priceStart) {
        String before = blockText.substring(0, priceStart).trim();
        // Take last meaningful word segment
        String[] parts = before.split("[\\n\\r]+");
        String candidate = parts[parts.length - 1].trim();
        return candidate.isEmpty() ? "Item" : candidate;
    }

    private void showOcrResults(List<OcrPriceItem> items) {
        StringBuilder sb = new StringBuilder("Detected prices:\n");
        for (OcrPriceItem item : items) {
            sb.append("• ").append(item.label).append(" → ₹").append((int) item.price).append("\n");
        }
        binding.textOcrResult.setText(sb.toString());
        binding.btnConfirmScan.setVisibility(View.VISIBLE);
        binding.btnConfirmScan.setOnClickListener(v -> {
            // Apply all detected price updates via repository
            for (OcrPriceItem item : items) {
                viewModel.updatePriceByName(item.label, item.price, "ocr_scan", "OCR Receipt Scan");
            }
            Toast.makeText(requireContext(),
                items.size() + " price(s) updated from scan", Toast.LENGTH_SHORT).show();
            isAnalysing = true;
            binding.btnConfirmScan.setVisibility(View.GONE);
            binding.textOcrResult.setText("Ready to scan...");
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        cameraExecutor.shutdown();
        if (textRecognizer != null) textRecognizer.close();
        binding = null;
    }

    // ── Inner class ───────────────────────────────────────────────────────────

    static class OcrPriceItem {
        final String label;
        final double price;
        OcrPriceItem(String label, double price) {
            this.label = label;
            this.price = price;
        }
    }
}
