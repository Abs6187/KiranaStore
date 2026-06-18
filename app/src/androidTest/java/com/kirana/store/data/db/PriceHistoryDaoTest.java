package com.kirana.store.data.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.kirana.store.data.model.PriceHistory;
import com.kirana.store.data.model.Product;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumented tests for {@link PriceHistoryDao} against an in-memory Room DB.
 * Validates the append-only audit log, the "latest entry" query, and the
 * {@code ON DELETE CASCADE} foreign-key behaviour.
 * <p>
 * Prices in Indian Rupees (₹).
 */
@RunWith(AndroidJUnit4.class)
public class PriceHistoryDaoTest {

    private KiranaDatabase db;
    private ProductDao productDao;
    private PriceHistoryDao historyDao;

    @Before
    public void createDb() {
        db = Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                KiranaDatabase.class)
            .allowMainThreadQueries()
            .build();
        productDao = db.productDao();
        historyDao = db.priceHistoryDao();
    }

    @After
    public void closeDb() {
        db.close();
    }

    @Test
    public void getLatestHistoryEntry_returnsNewestForProduct() throws Exception {
        long productId = productDao.insertProduct(new Product("Mustard Oil", 175.0, "litre"));

        PriceHistory first = new PriceHistory((int) productId, 175.0, "manual", "initial");
        first.timestamp = new Date(System.currentTimeMillis() - 60_000);
        historyDao.insertHistory(first);

        PriceHistory second = new PriceHistory((int) productId, 180.0, "voice", "180 rupee");
        second.timestamp = new Date(System.currentTimeMillis());
        historyDao.insertHistory(second);

        PriceHistory latest = historyDao.getLatestHistoryEntry((int) productId);
        assertNotNull(latest);
        assertEquals(180.0, latest.price, 0.001);
        assertEquals("voice", latest.source);
    }

    @Test
    public void getLatestHistoryEntry_returnsNullWhenNoHistory() {
        long productId = productDao.insertProduct(new Product("Sugar", 48.0, "kg"));
        PriceHistory latest = historyDao.getLatestHistoryEntry((int) productId);
        assertNull(latest);
    }

    @Test
    public void deletingProduct_cascadesToDeleteItsHistory() {
        Product oil = new Product("Sunflower Oil", 130.0, "litre");
        long productId = productDao.insertProduct(oil);
        oil.id = (int) productId;

        historyDao.insertHistory(new PriceHistory((int) productId, 130.0, "manual", null));
        historyDao.insertHistory(new PriceHistory((int) productId, 135.0, "ocr_scan", "receipt"));

        // Sanity: history exists
        assertNotNull(historyDao.getLatestHistoryEntry((int) productId));

        // Delete the parent product → cascade should wipe its history
        productDao.deleteProduct(oil);
        assertNull(historyDao.getLatestHistoryEntry((int) productId));
    }

    @Test
    public void clearHistoryForProduct_removesOnlyThatProductsRows() {
        long id1 = productDao.insertProduct(new Product("Toor Dal", 140.0, "kg"));
        long id2 = productDao.insertProduct(new Product("Sugar", 48.0, "kg"));

        historyDao.insertHistory(new PriceHistory((int) id1, 140.0, "manual", null));
        historyDao.insertHistory(new PriceHistory((int) id2, 48.0, "manual", null));

        historyDao.clearHistoryForProduct((int) id1);

        assertNull(historyDao.getLatestHistoryEntry((int) id1));
        assertNotNull("Other product's history must survive",
            historyDao.getLatestHistoryEntry((int) id2));
    }
}
