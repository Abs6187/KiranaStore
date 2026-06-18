package com.kirana.store.data.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.kirana.store.data.model.PriceHistory;

import java.util.List;

/**
 * DAO for the {@code price_history} table.
 * History is append-only – entries are never updated or deleted manually;
 * CASCADE handles cleanup when the parent product is removed.
 */
@Dao
public interface PriceHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertHistory(PriceHistory history);

    @Query("SELECT * FROM price_history WHERE productId = :productId ORDER BY timestamp DESC")
    LiveData<List<PriceHistory>> getHistoryForProduct(int productId);

    @Query("SELECT * FROM price_history ORDER BY timestamp DESC LIMIT :limit")
    LiveData<List<PriceHistory>> getRecentHistory(int limit);

    @Query("SELECT * FROM price_history WHERE productId = :productId ORDER BY timestamp DESC LIMIT 1")
    PriceHistory getLatestHistoryEntry(int productId);

    @Query("DELETE FROM price_history WHERE productId = :productId")
    void clearHistoryForProduct(int productId);
}
