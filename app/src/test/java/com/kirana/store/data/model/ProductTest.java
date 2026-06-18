package com.kirana.store.data.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

/**
 * Unit tests for the {@link Product} entity.
 * All monetary values are expressed in Indian Rupees (₹).
 */
public class ProductTest {

    @Test
    public void defaultConstructor_setsSensibleDefaults() {
        Product p = new Product();

        assertEquals("General", p.category);
        assertEquals("", p.unit);
        assertFalse("New product should not be pinned", p.isPinned);
        assertNotNull("lastUpdated must be stamped", p.lastUpdated);
        assertNotNull("createdAt must be stamped", p.createdAt);
    }

    @Test
    public void paramConstructor_assignsFieldsAndDefaultsCategory() {
        Product p = new Product("Mustard Oil", 175.0, "litre");

        assertEquals("Mustard Oil", p.name);
        assertEquals(175.0, p.currentPrice, 0.001);
        assertEquals("litre", p.unit);
        assertEquals("General", p.category);
        assertFalse(p.isPinned);
        assertNotNull(p.lastUpdated);
        assertNotNull(p.createdAt);
    }

    @Test
    public void paramConstructor_acceptsWholeRupeeIntegerPrice() {
        // Typical kirana price: whole-rupee amount
        Product rice = new Product("Basmati Rice 5kg", 540, "5kg");
        assertEquals(540.0, rice.currentPrice, 0.001);
    }

    @Test
    public void paramConstructor_acceptsFractionalRupeePrice() {
        Product sugar = new Product("Sugar", 48.50, "kg");
        assertEquals(48.50, sugar.currentPrice, 0.001);
    }
}
