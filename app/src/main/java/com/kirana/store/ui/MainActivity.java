package com.kirana.store.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.kirana.store.BuildConfig;
import com.kirana.store.R;
import com.kirana.store.ai.KiranaAiAgent;
import com.kirana.store.data.repository.ProductRepository;
import com.kirana.store.databinding.ActivityMainBinding;
import com.kirana.store.ui.dashboard.DashboardViewModel;
import com.kirana.store.util.FuzzyMatcher;
import com.kirana.store.voice.VoiceManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Single-Activity host for the Navigation Component.
 * <p>
 * Owns the global {@link VoiceManager} and {@link KiranaAiAgent} so they persist
 * across fragment navigation without lifecycle issues.
 * The floating voice button is hosted here and usable from any screen.
 */
public class MainActivity extends AppCompatActivity implements VoiceManager.VoiceCallback {

    private static final String TAG = "MainActivity";
    private static final String[] REQUIRED_PERMISSIONS = {
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO
    };

    private ActivityMainBinding binding;
    private VoiceManager voiceManager;
    private KiranaAiAgent aiAgent;
    private ProductRepository productRepository;
    private DashboardViewModel dashboardViewModel;
    private boolean isListening = false;
    private Vibrator vibrator;

    private final ActivityResultLauncher<String[]> permissionLauncher =
        registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
            boolean allGranted = true;
            for (Boolean granted : result.values()) {
                if (!granted) { allGranted = false; break; }
            }
            if (!allGranted) {
                Snackbar.make(binding.getRoot(),
                    "Camera & Microphone permissions are required for full functionality.",
                    Snackbar.LENGTH_LONG).show();
            }
        });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupNavigation();
        requestPermissionsIfNeeded();
        initVoiceAndAI();
        setupVoiceFab();

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        productRepository = new ProductRepository(getApplication());
        dashboardViewModel = new ViewModelProvider(this).get(DashboardViewModel.class);
    }

    private void setupNavigation() {
        NavHostFragment navHostFragment = (NavHostFragment)
            getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();

        BottomNavigationView bottomNav = binding.bottomNavigation;
        AppBarConfiguration config = new AppBarConfiguration.Builder(
            R.id.navigation_dashboard,
            R.id.navigation_prices,
            R.id.navigation_scanner,
            R.id.navigation_history
        ).build();

        NavigationUI.setupWithNavController(bottomNav, navController);

        // Hide bottom nav on scanner screen (full-screen camera)
        navController.addOnDestinationChangedListener((ctrl, dest, args) -> {
            if (dest.getId() == R.id.navigation_scanner) {
                binding.bottomNavigation.setVisibility(View.GONE);
                binding.fabVoice.hide();
            } else {
                binding.bottomNavigation.setVisibility(View.VISIBLE);
                binding.fabVoice.show();
            }
        });
    }

    private void initVoiceAndAI() {
        voiceManager = new VoiceManager(this);
        voiceManager.init(this);

        String apiKey = BuildConfig.GEMINI_API_KEY;
        if (apiKey != null && !apiKey.isEmpty() && !apiKey.equals("YOUR_GEMINI_API_KEY")) {
            aiAgent = new KiranaAiAgent(apiKey);
        } else {
            Log.w(TAG, "Gemini API key not configured. AI voice parsing disabled.");
        }
    }

    private void setupVoiceFab() {
        binding.fabVoice.setOnClickListener(v -> {
            if (isListening) {
                stopVoiceListening();
            } else {
                startVoiceListening();
            }
        });
    }

    private void startVoiceListening() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            Snackbar.make(binding.getRoot(), "Microphone permission needed for voice commands.",
                Snackbar.LENGTH_SHORT).show();
            return;
        }
        isListening = true;
        binding.fabVoice.setImageResource(R.drawable.ic_mic_active);
        vibrateShort();
        voiceManager.startListening();
        Snackbar.make(binding.getRoot(), "🎤 Listening... speak now", Snackbar.LENGTH_SHORT).show();
    }

    private void stopVoiceListening() {
        isListening = false;
        binding.fabVoice.setImageResource(R.drawable.ic_mic);
        voiceManager.stopListening();
    }

    // ── VoiceManager.VoiceCallback ────────────────────────────────────────────

    @Override
    public void onSpeechResult(String text, boolean isFinal) {
        if (!isFinal) return; // Only process final results
        Log.d(TAG, "Voice result: " + text);
        isListening = false;
        binding.fabVoice.setImageResource(R.drawable.ic_mic);

        if (aiAgent == null) {
            // Fallback: show raw transcript
            Snackbar.make(binding.getRoot(), "Heard: " + text, Snackbar.LENGTH_LONG).show();
            return;
        }

        // Show processing indicator
        Snackbar.make(binding.getRoot(), "🤖 Processing: \"" + text + "\"",
            Snackbar.LENGTH_SHORT).show();

        // Fetch product names for AI context, then call AI
        new Thread(() -> {
            try {
                List<String> names = productRepository.getAllProductNamesSync();
                aiAgent.parseVoiceCommand(text, names, new KiranaAiAgent.ParseCallback() {
                    @Override
                    public void onSuccess(KiranaAiAgent.ParsedCommand cmd) {
                        runOnUiThread(() -> handleParsedCommand(cmd));
                    }
                    @Override
                    public void onError(Exception e) {
                        runOnUiThread(() ->
                            Snackbar.make(binding.getRoot(),
                                "Couldn't understand: " + e.getMessage(),
                                Snackbar.LENGTH_LONG).show()
                        );
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error fetching product names: " + e.getMessage());
            }
        }).start();
    }

    @Override
    public void onListeningStarted() {
        runOnUiThread(() -> binding.fabVoice.setImageResource(R.drawable.ic_mic_active));
    }

    @Override
    public void onListeningStopped() {
        runOnUiThread(() -> {
            isListening = false;
            binding.fabVoice.setImageResource(R.drawable.ic_mic);
        });
    }

    @Override
    public void onError(String errorMessage) {
        runOnUiThread(() -> {
            isListening = false;
            binding.fabVoice.setImageResource(R.drawable.ic_mic);
            Snackbar.make(binding.getRoot(), "Voice error: " + errorMessage,
                Snackbar.LENGTH_SHORT).show();
        });
    }

    // ── AI Command Handler ────────────────────────────────────────────────────

    private void handleParsedCommand(KiranaAiAgent.ParsedCommand cmd) {
        if ("update_price".equals(cmd.action) && !cmd.product.isEmpty() && cmd.price > 0) {
            // Find closest matching product and update price
            productRepository.getAllProductNamesSync_Async(names -> {
                // Use string contains first, then Levenshtein for best match
                String bestMatch = FuzzyMatcher.findBestMatch(cmd.product, names);
                if (bestMatch != null) {
                    updatePriceByName(bestMatch, cmd.price, cmd.rawTranscript);
                    voiceManager.speak(cmd.acknowledgement);
                    Snackbar.make(binding.getRoot(),
                        "✅ " + cmd.acknowledgement, Snackbar.LENGTH_SHORT).show();
                } else {
                    voiceManager.speak("Product not found. Please add it manually.");
                    Snackbar.make(binding.getRoot(),
                        "Product \"" + cmd.product + "\" not found",
                        Snackbar.LENGTH_LONG).show();
                }
            });
        } else if ("add_product".equals(cmd.action) && !cmd.product.isEmpty()) {
            dashboardViewModel.triggerAddProduct(cmd.product,
                cmd.price > 0 ? cmd.price : 0, cmd.unit != null ? cmd.unit : "");
            voiceManager.speak(cmd.acknowledgement);
            Snackbar.make(binding.getRoot(), "✅ " + cmd.acknowledgement,
                Snackbar.LENGTH_SHORT).show();
        } else {
            voiceManager.speak("Sorry, I didn't understand that command.");
            Snackbar.make(binding.getRoot(), "Unrecognised command: " + cmd.action,
                Snackbar.LENGTH_SHORT).show();
        }
    }

    private void updatePriceByName(String productName, double price, String note) {
        dashboardViewModel.updatePriceByName(productName, price, "voice", note);
    }

    private void vibrateShort() {
        if (vibrator != null && vibrator.hasVibrator()) {
            vibrator.vibrate(50);
        }
    }

    private void requestPermissionsIfNeeded() {
        boolean allGranted = true;
        for (String perm : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
        }
        if (!allGranted) {
            permissionLauncher.launch(REQUIRED_PERMISSIONS);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        voiceManager.release();
    }
}
