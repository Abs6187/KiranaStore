package com.kirana.store.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.kirana.store.data.db.DateConverter;

import java.util.Date;

/**
 * Core entity for a Kirana product.
 * <p>
 * Design decision: {@code currentPrice} is the live price displayed on the dashboard.
 * All previous prices are stored in {@link PriceHistory}. This keeps the main table
 * lean while preserving full audit history for market-fluctuation tracking.
 */
@Entity(tableName = "products")
@TypeConverters(DateConverter.class)
public class Product {

    @PrimaryKey(autoGenerate = true)
    public int id;

    /** Human-readable product name (e.g., "Mustard Oil", "Basmati Rice 5kg") */
    public String name;

    /** Current selling price in ₹ (Rupees) */
    public double currentPrice;

    /** Optional unit of measure (e.g., "kg", "litre", "packet") */
    public String unit;

    /** Optional category tag for future grouping */
    public String category;

    /** Timestamp of last price update */
    @TypeConverters(DateConverter.class)
    public Date lastUpdated;

    /** Whether the item is marked as favourite/pinned to top of dashboard */
    public boolean isPinned;

    /** Timestamp the product was first added */
    @TypeConverters(DateConverter.class)
    public Date createdAt;

    public Product() {
        this.lastUpdated = new Date();
        this.createdAt = new Date();
        this.isPinned = false;
        this.unit = "";
        this.category = "General";
    }

    public Product(String name, double currentPrice, String unit) {
        this.name = name;
        this.currentPrice = currentPrice;
        this.unit = unit;
        this.category = "General";
        this.lastUpdated = new Date();
        this.createdAt = new Date();
        this.isPinned = false;
    }
}
