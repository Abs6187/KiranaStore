package com.kirana.store.data.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.kirana.store.data.db.DateConverter;

import java.util.Date;

/**
 * Immutable price-change log entry tied to a {@link Product}.
 * <p>
 * Every time a product's price changes (manual edit, voice command, OCR scan),
 * a new PriceHistory row is inserted. The timestamp auto-tags the change with
 * the exact moment, enabling market-fluctuation analysis.
 */
@Entity(
    tableName = "price_history",
    foreignKeys = @ForeignKey(
        entity = Product.class,
        parentColumns = "id",
        childColumns = "productId",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {@Index("productId")}
)
@TypeConverters(DateConverter.class)
public class PriceHistory {

    @PrimaryKey(autoGenerate = true)
    public int id;

    /** FK → products.id */
    public int productId;

    /** Price at the time of this entry */
    public double price;

    /** How the price was changed: "manual", "voice", "ocr_scan" */
    public String source;

    /** Optional note (e.g., parsed voice command text) */
    public String note;

    /** When this price entry was recorded */
    @TypeConverters(DateConverter.class)
    public Date timestamp;

    public PriceHistory() {
        this.timestamp = new Date();
    }

    public PriceHistory(int productId, double price, String source, String note) {
        this.productId = productId;
        this.price = price;
        this.source = source;
        this.note = note;
        this.timestamp = new Date();
    }
}
