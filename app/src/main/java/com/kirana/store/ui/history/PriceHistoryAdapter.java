package com.kirana.store.ui.history;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.kirana.store.data.model.PriceHistory;
import com.kirana.store.databinding.ItemHistoryBinding;

import java.text.SimpleDateFormat;
import java.util.Locale;

/** Adapter for the price history feed. */
public class PriceHistoryAdapter extends ListAdapter<PriceHistory, PriceHistoryAdapter.VH> {

    private static final SimpleDateFormat DATE_FMT =
        new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());

    public PriceHistoryAdapter() {
        super(DIFF);
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemHistoryBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.bind(getItem(position));
    }

    static class VH extends RecyclerView.ViewHolder {
        final ItemHistoryBinding b;
        VH(ItemHistoryBinding b) { super(b.getRoot()); this.b = b; }

        void bind(PriceHistory h) {
            b.textHistoryPrice.setText("₹" + formatPrice(h.price));
            b.textHistorySource.setText(sourceLabel(h.source));
            b.textHistoryDate.setText(h.timestamp != null ? DATE_FMT.format(h.timestamp) : "");
            b.textHistoryNote.setText(h.note != null ? h.note : "");
        }
    }

    private static String formatPrice(double p) {
        return p == Math.floor(p) ? String.valueOf((int) p)
            : String.format(Locale.getDefault(), "%.2f", p);
    }

    private static String sourceLabel(String source) {
        if (source == null) return "Manual";
        switch (source) {
            case "voice":    return "🎤 Voice";
            case "ocr_scan": return "📷 OCR Scan";
            default:         return "✏️ Manual";
        }
    }

    private static final DiffUtil.ItemCallback<PriceHistory> DIFF =
        new DiffUtil.ItemCallback<PriceHistory>() {
            @Override public boolean areItemsTheSame(@NonNull PriceHistory a, @NonNull PriceHistory b) {
                return a.id == b.id;
            }
            @Override public boolean areContentsTheSame(@NonNull PriceHistory a, @NonNull PriceHistory b) {
                return a.price == b.price && a.productId == b.productId;
            }
        };
}
