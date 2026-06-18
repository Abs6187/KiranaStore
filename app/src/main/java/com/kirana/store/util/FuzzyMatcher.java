package com.kirana.store.util;

import java.util.List;

/**
 * Fuzzy product-name matching used by the voice and OCR flows.
 * <p>
 * Extracted from {@code MainActivity} so the matching strategy can be
 * unit-tested independently of the Android UI layer.
 * <p>
 * Strategy (in priority order):
 * <ol>
 *   <li>Substring contains in either direction (case-insensitive).</li>
 *   <li>Closest by Levenshtein edit distance, accepted only if ≤ {@value #THRESHOLD}.</li>
 * </ol>
 */
public final class FuzzyMatcher {

    /** Maximum Levenshtein edit distance still considered a match. */
    public static final int THRESHOLD = 5;

    private FuzzyMatcher() { /* no instances */ }

    /**
     * Find the best-matching product name from {@code candidates} for {@code query}.
     *
     * @param query     the spoken / scanned name to match (e.g. "mustard oil")
     * @param candidates known product names; may be empty or null
     * @return the best candidate, or {@code null} if nothing matches within threshold
     */
    public static String findBestMatch(String query, List<String> candidates) {
        if (candidates == null || candidates.isEmpty() || query == null) return null;
        String queryLower = query.toLowerCase();

        // 1. Exact / substring contains match (either direction)
        for (String name : candidates) {
            if (name == null) continue;
            String nameLower = name.toLowerCase();
            if (nameLower.contains(queryLower) || queryLower.contains(nameLower)) {
                return name;
            }
        }

        // 2. Levenshtein best match (threshold ≤ THRESHOLD)
        String best = null;
        int bestDist = Integer.MAX_VALUE;
        for (String name : candidates) {
            if (name == null) continue;
            int dist = levenshtein(queryLower, name.toLowerCase());
            if (dist < bestDist) {
                bestDist = dist;
                best = name;
            }
        }
        return bestDist <= THRESHOLD ? best : null;
    }

    /**
     * Classic Levenshtein edit distance between two strings.
     */
    public static int levenshtein(String a, String b) {
        if (a == null) a = "";
        if (b == null) b = "";
        int m = a.length(), n = b.length();
        int[][] dp = new int[m + 1][n + 1];
        for (int i = 0; i <= m; i++) dp[i][0] = i;
        for (int j = 0; j <= n; j++) dp[0][j] = j;
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (a.charAt(i - 1) == b.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(dp[i - 1][j - 1],
                                  Math.min(dp[i - 1][j], dp[i][j - 1]));
                }
            }
        }
        return dp[m][n];
    }
}
