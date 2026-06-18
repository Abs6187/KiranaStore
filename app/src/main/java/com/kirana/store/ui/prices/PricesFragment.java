package com.kirana.store.ui.prices;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.kirana.store.databinding.FragmentPricesBinding;
import com.kirana.store.ui.dashboard.DashboardViewModel;

import java.util.ArrayList;

/**
 * Prices screen – linear list view with all products sorted by last-updated,
 * with inline price editing. Shares ViewModel with Dashboard.
 */
public class PricesFragment extends Fragment {

    private FragmentPricesBinding binding;
    private DashboardViewModel viewModel;
    private PriceListAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentPricesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(DashboardViewModel.class);

        adapter = new PriceListAdapter(
            (product, newPrice) -> viewModel.updatePrice(product.id, newPrice, "manual", null),
            product -> { /* long press handled via bottom sheet in DashboardFragment */ }
        );
        binding.recyclerPrices.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerPrices.setAdapter(adapter);

        viewModel.allProducts.observe(getViewLifecycleOwner(), products ->
            adapter.submitList(products != null ? products : new ArrayList<>()));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
