package com.kirana.store.ui.prices;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.kirana.store.R;
import com.kirana.store.data.model.Product;
import com.kirana.store.databinding.ItemProductCardBinding;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * RecyclerView adapter for price cards.
 * Uses ListAdapter with DiffUtil for efficient updates when prices change.
 */
public class PriceListAdapter extends ListAdapter<Product, PriceListAdapter.ProductViewHolder> {

    private static final SimpleDateFormat DATE_FMT =
        new SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault());

    private final OnPriceEditListener priceEditListener;
    private final OnProductLongPressListener longPressListener;

    public interface OnPriceEditListener {
        void onPriceEdit(Product product, double newPrice);
    }

    public interface OnProductLongPressListener {
        void onLongPress(Product product);
    }

    public PriceListAdapter(OnPriceEditListener editListener,
                            OnProductLongPressListener longPressListener) {
        super(DIFF_CALLBACK);
        this.priceEditListener = editListener;
        this.longPressListener = longPressListener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemProductCardBinding binding = ItemProductCardBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
        return new ProductViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class ProductViewHolder extends RecyclerView.ViewHolder {
        private final ItemProductCardBinding binding;

        ProductViewHolder(ItemProductCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Product product) {
            binding.textProductName.setText(product.name);
            binding.textPrice.setText("₹" + formatPrice(product.currentPrice));
            binding.textUnit.setText(
                TextUtils.isEmpty(product.unit) ? "" : "/ " + product.unit);
            binding.textLastUpdated.setText(
                product.lastUpdated != null ? DATE_FMT.format(product.lastUpdated) : "");
            binding.iconPinned.setVisibility(product.isPinned ? View.VISIBLE : View.GONE);

            // Tap → inline quick-price edit (shows edit text in-card)
            binding.btnEditPrice.setOnClickListener(v -> showInlineEdit(product));
            binding.getRoot().setOnLongClickListener(v -> {
                if (longPressListener != null) longPressListener.onLongPress(product);
                return true;
            });

            // Colour accent based on category (future extensibility)
            binding.cardAccent.setBackgroundColor(
                getCategoryColor(product.category, binding.getRoot()));
        }

        private void showInlineEdit(Product product) {
            // Toggle inline edit panel
            boolean visible = binding.layoutInlineEdit.getVisibility() == View.VISIBLE;
            if (visible) {
                binding.layoutInlineEdit.setVisibility(View.GONE);
            } else {
                binding.editNewPrice.setText(String.valueOf((int) product.currentPrice));
                binding.layoutInlineEdit.setVisibility(View.VISIBLE);
                binding.editNewPrice.requestFocus();

                binding.btnConfirmPrice.setOnClickListener(v -> {
                    String val = binding.editNewPrice.getText().toString().trim();
                    if (!val.isEmpty()) {
                        try {
                            double newPrice = Double.parseDouble(val);
                            if (priceEditListener != null) {
                                priceEditListener.onPriceEdit(product, newPrice);
                            }
                            binding.textPrice.setText("₹" + formatPrice(newPrice));
                            binding.layoutInlineEdit.setVisibility(View.GONE);
                        } catch (NumberFormatException ignored) {}
                    }
                });
                binding.btnCancelEdit.setOnClickListener(v ->
                    binding.layoutInlineEdit.setVisibility(View.GONE));
            }
        }
    }

    private static String formatPrice(double price) {
        if (price == Math.floor(price)) return String.valueOf((int) price);
        return String.format(Locale.getDefault(), "%.2f", price);
    }

    private int getCategoryColor(String category, View v) {
        if (category == null) return v.getContext().getColor(R.color.accent_default);
        return v.getContext().getColor(R.color.accent_default);
    }

    private static final DiffUtil.ItemCallback<Product> DIFF_CALLBACK =
        new DiffUtil.ItemCallback<Product>() {
            @Override
            public boolean areItemsTheSame(@NonNull Product a, @NonNull Product b) {
                return a.id == b.id;
            }
            @Override
            public boolean areContentsTheSame(@NonNull Product a, @NonNull Product b) {
                return a.currentPrice == b.currentPrice &&
                    a.name.equals(b.name) &&
                    a.isPinned == b.isPinned;
            }
        };
}
