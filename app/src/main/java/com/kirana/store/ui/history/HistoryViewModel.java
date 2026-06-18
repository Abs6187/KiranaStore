package com.kirana.store.ui.history;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.kirana.store.data.model.PriceHistory;
import com.kirana.store.data.repository.ProductRepository;

import java.util.List;

public class HistoryViewModel extends AndroidViewModel {
    private final ProductRepository repository;
    public final LiveData<List<PriceHistory>> recentHistory;

    public HistoryViewModel(Application application) {
        super(application);
        repository = new ProductRepository(application);
        recentHistory = repository.getRecentHistory(50);
    }
}
