package com.kirana.store.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.kirana.store.data.db.KiranaDatabase;
import com.kirana.store.data.db.PriceHistoryDao;
import com.kirana.store.data.db.ProductDao;
import com.kirana.store.data.model.PriceHistory;
import com.kirana.store.data.model.Product;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Single source of truth for all product/price data.
 * <p>
 * Uses a fixed thread pool so all DB writes are serialised off the main thread.
 * ViewModels observe {@link LiveData} returned here; Room pushes updates automatically.
 */
public class ProductRepository {

    private final ProductDao productDao;
    private final PriceHistoryDao priceHistoryDao;
    private final FirebaseFirestore firestore;
    private final ExecutorService executor = Executors.newFixedThreadPool(3);

    public ProductRepository(Application application) {
        KiranaDatabase db = KiranaDatabase.getInstance(application);
        productDao = db.productDao();
        priceHistoryDao = db.priceHistoryDao();
        firestore = FirebaseFirestore.getInstance();
    }

    // ── Product CRUD ──────────────────────────────────────────────────────────

    public void insert(Product product, OnResultCallback<Long> callback) {
        executor.execute(() -> {
            long id = productDao.insertProduct(product);
            product.id = (int) id;
            firestore.collection("products").document(String.valueOf(id)).set(product);
            if (callback != null) callback.onResult(id);
        });
    }

    public void update(Product product) {
        executor.execute(() -> {
            productDao.updateProduct(product);
            firestore.collection("products").document(String.valueOf(product.id)).set(product);
        });
    }

    public void delete(Product product) {
        executor.execute(() -> {
            productDao.deleteProduct(product);
            firestore.collection("products").document(String.valueOf(product.id)).delete();
        });
    }

    // ── Price Updates ─────────────────────────────────────────────────────────

    /**
     * Atomically updates the current price and appends a PriceHistory entry.
     * This is the canonical way to change a price – from manual edit, voice, or OCR.
     *
     * @param productId target product
     * @param newPrice  new price in ₹
     * @param source    "manual" | "voice" | "ocr_scan"
     * @param note      optional note (e.g., raw voice transcript)
     */
    public void updatePrice(int productId, double salesPrice, double purchasePrice, String source, String note) {
        executor.execute(() -> {
            long now = new Date().getTime();
            productDao.updatePrice(productId, salesPrice, purchasePrice, now);
            PriceHistory history = new PriceHistory(productId, salesPrice, purchasePrice, source, note);
            priceHistoryDao.insertHistory(history);
            
            Map<String, Object> updates = new HashMap<>();
            updates.put("salesPrice", salesPrice);
            updates.put("purchasePrice", purchasePrice);
            updates.put("lastUpdated", new Date(now));
            firestore.collection("products").document(String.valueOf(productId)).update(updates);
            firestore.collection("price_history").add(history);
        });
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    public LiveData<List<Product>> getAllProducts() {
        return productDao.getAllProducts();
    }

    public LiveData<Product> getProductById(int id) {
        return productDao.getProductById(id);
    }

    public LiveData<List<Product>> searchProducts(String query) {
        return productDao.searchProducts(query);
    }

    public LiveData<List<Product>> getRecentlyUpdatedProducts() {
        return productDao.getRecentlyUpdatedProducts();
    }

    public LiveData<List<PriceHistory>> getHistoryForProduct(int productId) {
        return priceHistoryDao.getHistoryForProduct(productId);
    }

    public LiveData<List<PriceHistory>> getRecentHistory(int limit) {
        return priceHistoryDao.getRecentHistory(limit);
    }

    /** Returns all product names synchronously for Levenshtein fuzzy matching in OCR/voice flow. */
    public List<String> getAllProductNamesSync() throws Exception {
        return executor.submit(() -> productDao.getAllProductNames()).get();
    }

    /** Async variant – runs on executor then posts callback on the calling thread's context. */
    public void getAllProductNamesSync_Async(OnResultCallback<List<String>> callback) {
        executor.execute(() -> {
            try {
                List<String> names = productDao.getAllProductNames();
                if (callback != null) callback.onResult(names);
            } catch (Exception e) {
                if (callback != null) callback.onResult(new java.util.ArrayList<>());
            }
        });
    }

    // ── Callback Interface ────────────────────────────────────────────────────

    public interface OnResultCallback<T> {
        void onResult(T result);
    }
}
