package com.kirana.store.data.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Date;

import org.junit.Test;

/**
 * Unit tests for the {@link DateConverter} Room TypeConverter.
 * Pure JVM – no Android runtime required.
 */
public class DateConverterTest {

    @Test
    public void dateToTimestamp_roundTripsThroughFromTimestamp() {
        Date original = new Date(1_718_700_000_000L); // mid-2024
        Long ts = DateConverter.dateToTimestamp(original);
        Date recovered = DateConverter.fromTimestamp(ts);

        assertEquals(original.getTime(), ts.longValue());
        assertEquals(original, recovered);
    }

    @Test
    public void fromTimestamp_returnsNullForNullInput() {
        assertNull(DateConverter.fromTimestamp(null));
    }

    @Test
    public void dateToTimestamp_returnsNullForNullInput() {
        assertNull(DateConverter.dateToTimestamp(null));
    }

    @Test
    public void fromTimestamp_reconstructsExactMillis() {
        long epoch = System.currentTimeMillis();
        Date d = DateConverter.fromTimestamp(epoch);
        assertEquals(epoch, d.getTime());
    }
}
