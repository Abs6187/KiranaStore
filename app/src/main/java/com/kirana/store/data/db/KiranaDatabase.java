package com.kirana.store.data.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.kirana.store.data.model.PriceHistory;
import com.kirana.store.data.model.Product;

/**
 * Singleton Room database for Kirana Store Manager.
 * <p>
 * Migration strategy: exportSchema = true so that schema files are checked in
 * to version control, enabling safe migration scripts between versions.
 * <p>
 * Version history:
 *   v1 – initial schema (products + price_history)
 */
@Database(
    entities = {Product.class, PriceHistory.class},
    version = 1,
    exportSchema = true
)
@TypeConverters(DateConverter.class)
public abstract class KiranaDatabase extends RoomDatabase {

    private static volatile KiranaDatabase INSTANCE;
    private static final String DB_NAME = "kirana_store.db";

    public abstract ProductDao productDao();
    public abstract PriceHistoryDao priceHistoryDao();

    public static KiranaDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (KiranaDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            KiranaDatabase.class,
                            DB_NAME
                        )
                        .fallbackToDestructiveMigration()
                        .build();
                }
            }
        }
        return INSTANCE;
    }
}
