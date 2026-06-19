package com.kirana.store.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

/**
 * Unit tests for {@link FuzzyMatcher} – the voice/OCR product-name matcher
 * extracted from MainActivity. Uses kirana product names.
 */
public class FuzzyMatcherTest {

    private static final List<String> STOCK = Arrays.asList(
        "Mustard Oil", "Basmati Rice", "Atta 5kg", "Sugar", "Toor Dal", "Sunflower Oil"
    );

    // ── Substring / exact matching (priority 1) ───────────────────────────────

    @Test
    public void exactContainsMatch_returnsCandidateAsStored() {
        // Spoken name is substring of a stored name → returns the stored name verbatim
        String match = FuzzyMatcher.findBestMatch("basmati", STOCK);
        assertEquals("Basmati Rice", match);
    }

    @Test
    public void reverseContainsMatch_returnsCandidate() {
        // Stored name is substring of the spoken query → returns the stored name
        String match = FuzzyMatcher.findBestMatch("Sunflower Oil 1 Litre", STOCK);
        assertEquals("Sunflower Oil", match);
    }

    @Test
    public void containsMatch_isCaseInsensitive() {
        assertEquals("Sugar", FuzzyMatcher.findBestMatch("sugar", STOCK));
        assertEquals("Sugar", FuzzyMatcher.findBestMatch("SUGAR", STOCK));
    }

    // ── Levenshtein fallback (priority 2) ─────────────────────────────────────

    @Test
    public void smallTypo_matchesWithinThreshold() {
        // "Toor Dal" vs "toor dal" distance 0; a near miss like "Toor Daal"
        // is within the ≤5 threshold via Levenshtein.
        assertEquals("Toor Dal", FuzzyMatcher.findBestMatch("Toor Daal", STOCK));
    }

    @Test
    public void levenshtein_returnsNullWhenNothingCloseEnough() {
        // Completely unrelated, long word → edit distance > threshold
        assertNull(FuzzyMatcher.findBestMatch("Detergent Powder 2kg Refill", STOCK));
    }

    // ── Edge cases ────────────────────────────────────────────────────────────

    @Test
    public void emptyCandidateList_returnsNull() {
        assertNull(FuzzyMatcher.findBestMatch("Mustard Oil", Collections.emptyList()));
    }

    @Test
    public void nullCandidateList_returnsNull() {
        assertNull(FuzzyMatcher.findBestMatch("Mustard Oil", null));
    }

    @Test
    public void nullQuery_returnsNull() {
        assertNull(FuzzyMatcher.findBestMatch(null, STOCK));
    }

    // ── Direct levenshtein() checks ───────────────────────────────────────────

    @Test
    public void levenshtein_identicalStringsIsZero() {
        assertEquals(0, FuzzyMatcher.levenshtein("mustard oil", "mustard oil"));
    }

    @Test
    public void levenshtein_singleSubstitutionIsOne() {
        assertEquals(1, FuzzyMatcher.levenshtein("dal", "dal".replace('a', 'e')));
        // one substitution: 'dal' -> 'dal' is 0; 'dal' -> 'pal' is 1
        assertEquals(1, FuzzyMatcher.levenshtein("dal", "pal"));
    }

    @Test
    public void levenshtein_handlesNullAsEmpty() {
        assertEquals(5, FuzzyMatcher.levenshtein(null, "sugar"));
        assertEquals(0, FuzzyMatcher.levenshtein(null, null));
    }
}
