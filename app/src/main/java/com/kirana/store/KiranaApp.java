package com.kirana.store;

import android.app.Application;
import com.kirana.store.data.db.KiranaDatabase;

/**
 * Application class – initialises the Room database singleton on startup.
 * All other components access data through {@link com.kirana.store.data.repository.ProductRepository}.
 */
public class KiranaApp extends Application {

    private static KiranaApp instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        // Pre-warm the database on background thread so first query is fast
        KiranaDatabase.getInstance(this);
    }

    public static KiranaApp getInstance() {
        return instance;
    }
}
