package com.kirana.store.ui.dashboard;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.kirana.store.R;
import com.kirana.store.data.model.Product;
import com.kirana.store.databinding.FragmentDashboardBinding;
import com.kirana.store.databinding.DialogAddProductBinding;
import com.kirana.store.ui.prices.PriceListAdapter;

import java.util.ArrayList;

/**
 * Dashboard screen – the primary screen for the shopkeeper.
 * <p>
 * Shows: live search, pinned items at top, grid of all products with ₹ prices,
 * and a FAB-driven quick-add dialog. Voice events from MainActivity are also
 * handled here through the shared ViewModel.
 */
public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private DashboardViewModel viewModel;
    private PriceListAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(DashboardViewModel.class);
        setupRecyclerView();
        setupSearch();
        setupObservers();
        setupAddButton();
    }

    private void setupRecyclerView() {
        adapter = new PriceListAdapter(
            // On price tap – inline edit
            (product, newPrice) -> viewModel.updatePrice(product.id, newPrice, "manual", null),
            // On item long-press – options
            product -> showProductOptions(product)
        );
        binding.recyclerProducts.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        binding.recyclerProducts.setAdapter(adapter);
        binding.recyclerProducts.setHasFixedSize(false);
    }

    private void setupSearch() {
        binding.editSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                String q = s.toString().trim();
                if (q.isEmpty()) {
                    viewModel.allProducts.observe(getViewLifecycleOwner(),
                        products -> adapter.submitList(products != null ? products : new ArrayList<>()));
                } else {
                    viewModel.searchProducts(q).observe(getViewLifecycleOwner(),
                        products -> adapter.submitList(products != null ? products : new ArrayList<>()));
                }
            }
        });
    }

    private void setupObservers() {
        viewModel.allProducts.observe(getViewLifecycleOwner(), products -> {
            if (products != null) {
                adapter.submitList(products);
                binding.textEmptyState.setVisibility(
                    products.isEmpty() ? View.VISIBLE : View.GONE);
                binding.textProductCount.setText(products.size() + " items");
            }
        });

        // Voice-triggered add-product event
        viewModel.getAddProductTrigger().observe(getViewLifecycleOwner(), event -> {
            if (event != null && !event.consumed) {
                event.consumed = true;
                showAddProductDialog(event.name, event.price, event.unit);
            }
        });

        viewModel.getStatusMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty() && getView() != null) {
                Snackbar.make(requireView(), msg, Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void setupAddButton() {
        binding.fabAddProduct.setOnClickListener(v -> showAddProductDialog("", 0, ""));

        // Gear icon → navigate to Settings screen
        binding.btnSettings.setOnClickListener(v ->
            androidx.navigation.Navigation.findNavController(v)
                .navigate(R.id.navigation_settings));
    }

    private void showAddProductDialog(String prefillName, double prefillPrice, String prefillUnit) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext(),
            R.style.ThemeOverlay_KiranaStore_BottomSheet);
        DialogAddProductBinding dialogBinding =
            DialogAddProductBinding.inflate(getLayoutInflater());
        dialog.setContentView(dialogBinding.getRoot());

        if (!prefillName.isEmpty()) dialogBinding.editProductName.setText(prefillName);
        if (prefillPrice > 0) dialogBinding.editProductPrice.setText(String.valueOf((int)prefillPrice));
        if (prefillUnit != null && !prefillUnit.isEmpty())
            dialogBinding.editProductUnit.setText(prefillUnit);

        dialogBinding.btnSaveProduct.setOnClickListener(v -> {
            String name = dialogBinding.editProductName.getText().toString().trim();
            String priceStr = dialogBinding.editProductPrice.getText().toString().trim();
            String unit = dialogBinding.editProductUnit.getText().toString().trim();

            if (name.isEmpty()) {
                dialogBinding.editProductName.setError("Product name required");
                return;
            }
            if (priceStr.isEmpty()) {
                dialogBinding.editProductPrice.setError("Price required");
                return;
            }

            try {
                double price = Double.parseDouble(priceStr);
                viewModel.addProduct(name, price, unit);
                dialog.dismiss();
                Snackbar.make(requireView(), "✅ " + name + " added at ₹" + (int)price,
                    Snackbar.LENGTH_SHORT).show();
            } catch (NumberFormatException e) {
                dialogBinding.editProductPrice.setError("Invalid price");
            }
        });

        dialogBinding.btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showProductOptions(Product product) {
        // Simple bottom sheet: Edit Price / Delete / Pin
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext(),
            R.style.ThemeOverlay_KiranaStore_BottomSheet);
        // For brevity – can be expanded with a dedicated layout
        // Currently opens the add dialog pre-filled for price edit
        dialog.dismiss();
        showEditPriceDialog(product);
    }

    private void showEditPriceDialog(Product product) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext(),
            R.style.ThemeOverlay_KiranaStore_BottomSheet);
        DialogAddProductBinding dialogBinding =
            DialogAddProductBinding.inflate(getLayoutInflater());
        dialog.setContentView(dialogBinding.getRoot());

        dialogBinding.textDialogTitle.setText("Edit Price: " + product.name);
        dialogBinding.editProductName.setText(product.name);
        dialogBinding.editProductName.setEnabled(false);
        dialogBinding.editProductPrice.setText(String.valueOf((int) product.currentPrice));
        dialogBinding.editProductUnit.setText(product.unit);

        dialogBinding.btnSaveProduct.setText("Update Price");
        dialogBinding.btnSaveProduct.setOnClickListener(v -> {
            String priceStr = dialogBinding.editProductPrice.getText().toString().trim();
            if (priceStr.isEmpty()) { dialogBinding.editProductPrice.setError("Required"); return; }
            try {
                double newPrice = Double.parseDouble(priceStr);
                viewModel.updatePrice(product.id, newPrice, "manual", null);
                dialog.dismiss();
                Snackbar.make(requireView(),
                    "✅ " + product.name + " → ₹" + (int) newPrice, Snackbar.LENGTH_SHORT).show();
            } catch (NumberFormatException e) {
                dialogBinding.editProductPrice.setError("Invalid price");
            }
        });
        dialogBinding.btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
