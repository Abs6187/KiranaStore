package com.kirana.store.data.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.kirana.store.data.model.Product;

import java.util.List;

/**
 * Data-access object for the {@code products} table.
 * LiveData queries are observed by ViewModels; Room automatically delivers
 * updates on the main thread when the underlying data changes.
 */
@Dao
public interface ProductDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertProduct(Product product);

    @Update
    void updateProduct(Product product);

    @Delete
    void deleteProduct(Product product);

    @Query("SELECT * FROM products ORDER BY isPinned DESC, name ASC")
    LiveData<List<Product>> getAllProducts();

    @Query("SELECT * FROM products WHERE id = :id")
    LiveData<Product> getProductById(int id);

    @Query("SELECT * FROM products WHERE name LIKE '%' || :query || '%' ORDER BY isPinned DESC, name ASC")
    LiveData<List<Product>> searchProducts(String query);

    @Query("SELECT * FROM products ORDER BY lastUpdated DESC LIMIT 10")
    LiveData<List<Product>> getRecentlyUpdatedProducts();

    @Query("UPDATE products SET currentPrice = :price, lastUpdated = :timestamp WHERE id = :id")
    void updatePrice(int id, double price, long timestamp);

    @Query("SELECT COUNT(*) FROM products")
    int getProductCount();

    /** For Levenshtein-based fuzzy matching in OCR flow – returns all names at once */
    @Query("SELECT name FROM products")
    List<String> getAllProductNames();
}
