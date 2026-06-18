package com.kirana.store.data.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

/**
 * Unit tests for the {@link PriceHistory} audit entity.
 * Prices are in Indian Rupees (₹).
 */
public class PriceHistoryTest {

    @Test
    public void defaultConstructor_stampsTimestampNow() {
        PriceHistory h = new PriceHistory();
        assertNotNull(h.timestamp);
    }

    @Test
    public void paramConstructor_assignsAllFields() {
        PriceHistory h = new PriceHistory(7, 220.0, "voice", "Atta 175 rupees kar do");

        assertEquals(7, h.productId);
        assertEquals(220.0, h.price, 0.001);
        assertEquals("voice", h.source);
        assertEquals("Atta 175 rupees kar do", h.note);
        assertNotNull(h.timestamp);
    }

    @Test
    public void paramConstructor_acceptsAllThreeSources() {
        // The repository's updatePrice() is the canonical path for all three sources.
        assertNotNull(new PriceHistory(1, 90.0, "manual", null));
        assertNotNull(new PriceHistory(1, 92.0, "voice", "dal 92"));
        assertNotNull(new PriceHistory(1, 95.0, "ocr_scan", "receipt row"));
    }
}
