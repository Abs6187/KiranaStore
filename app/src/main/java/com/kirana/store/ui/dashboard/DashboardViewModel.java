package com.kirana.store.ui.dashboard;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.kirana.store.data.model.Product;
import com.kirana.store.data.repository.ProductRepository;

import java.util.List;

/**
 * ViewModel for the Dashboard and shared price-update operations.
 * Survives configuration changes. Exposed to MainActivity for voice-command routing.
 */
public class DashboardViewModel extends AndroidViewModel {

    private final ProductRepository repository;

    // Observed by DashboardFragment and PricesFragment
    public final LiveData<List<Product>> allProducts;
    public final LiveData<List<Product>> recentProducts;

    // Single-event triggers for cross-fragment actions (voice commands)
    private final MutableLiveData<AddProductEvent> addProductTrigger = new MutableLiveData<>();
    private final MutableLiveData<String> statusMessage = new MutableLiveData<>();

    public DashboardViewModel(Application application) {
        super(application);
        repository = new ProductRepository(application);
        allProducts = repository.getAllProducts();
        recentProducts = repository.getRecentlyUpdatedProducts();
    }

    // ── Product operations ────────────────────────────────────────────────────

    public void addProduct(String name, double price, String unit) {
        Product p = new Product(name, price, unit);
        repository.insert(p, id -> {
            if (id > 0) {
                new Handler(Looper.getMainLooper()).post(() ->
                    statusMessage.setValue("Added: " + name));
            }
        });
    }

    public void deleteProduct(Product product) {
        repository.delete(product);
    }

    public void updateProduct(Product product) {
        repository.update(product);
    }

    /** Update price by product ID – direct API for price cards. */
    public void updatePrice(int productId, double newPrice, String source, String note) {
        repository.updatePrice(productId, newPrice, source, note);
    }

    /** Update price by product name – used by voice command routing from MainActivity. */
    public void updatePriceByName(String productName, double price, String source, String note) {
        if (allProducts.getValue() == null) return;
        for (Product p : allProducts.getValue()) {
            if (p.name.equalsIgnoreCase(productName)) {
                repository.updatePrice(p.id, price, source, note);
                statusMessage.setValue("Updated " + productName + " → ₹" + price);
                return;
            }
        }
    }

    /** Trigger add-product dialog from voice command (consumed by DashboardFragment). */
    public void triggerAddProduct(String name, double price, String unit) {
        addProductTrigger.setValue(new AddProductEvent(name, price, unit));
    }

    public LiveData<AddProductEvent> getAddProductTrigger() { return addProductTrigger; }
    public LiveData<String> getStatusMessage() { return statusMessage; }

    public LiveData<List<Product>> searchProducts(String query) {
        return repository.searchProducts(query);
    }

    // ── Inner classes ─────────────────────────────────────────────────────────

    public static class AddProductEvent {
        public final String name;
        public final double price;
        public final String unit;
        public boolean consumed = false;

        public AddProductEvent(String name, double price, String unit) {
            this.name = name;
            this.price = price;
            this.unit = unit;
        }
    }
}
