package com.smartgaon.ai.smartgaon_api.shikshaquiz.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashSet;
import java.util.Set;

/**
 * Duplicate-detection helpers (Section 3.3) — all free, no paid AI services.
 * Layer 1: normalization + SHA-256 hash (exact/near-exact, at insert time).
 * Layer 2: Jaccard word-overlap + Levenshtein (paraphrased, on-demand).
 */
public final class TextSimilarityUtil {

    private TextSimilarityUtil() {}

    /** lowercase, trim, strip punctuation and extra spaces */
    public static String normalize(String text) {
        if (text == null) return "";
        return text.toLowerCase()
                .replaceAll("[^a-z0-9\\u0900-\\u097F\\s]", "") // keep alphanumerics + Devanagari
                .replaceAll("\\s+", " ")
                .trim();
    }

    /** questionHash = SHA-256(normalizedText + subject + classOrCompetition) */
    public static String questionHash(String questionText, String subject, String classOrCompetition) {
        String input = normalize(questionText)
                + "|" + normalize(subject)
                + "|" + normalize(classOrCompetition == null ? "" : classOrCompetition);
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }

    /** Word-overlap similarity in [0,1]. Cheap; good for paraphrase detection. */
    public static double jaccardSimilarity(String a, String b) {
        Set<String> wa = new HashSet<>();
        Set<String> wb = new HashSet<>();
        for (String w : normalize(a).split(" ")) if (!w.isBlank()) wa.add(w);
        for (String w : normalize(b).split(" ")) if (!w.isBlank()) wb.add(w);
        if (wa.isEmpty() && wb.isEmpty()) return 1.0;
        Set<String> intersection = new HashSet<>(wa);
        intersection.retainAll(wb);
        Set<String> union = new HashSet<>(wa);
        union.addAll(wb);
        return (double) intersection.size() / union.size();
    }

    /** Levenshtein-based similarity in [0,1] (1 = identical). */
    public static double levenshteinSimilarity(String a, String b) {
        String na = normalize(a);
        String nb = normalize(b);
        int maxLen = Math.max(na.length(), nb.length());
        if (maxLen == 0) return 1.0;
        return 1.0 - ((double) levenshtein(na, nb) / maxLen);
    }

    private static int levenshtein(String a, String b) {
        int[] prev = new int[b.length() + 1];
        int[] curr = new int[b.length() + 1];
        for (int j = 0; j <= b.length(); j++) prev[j] = j;
        for (int i = 1; i <= a.length(); i++) {
            curr[0] = i;
            for (int j = 1; j <= b.length(); j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                curr[j] = Math.min(Math.min(curr[j - 1] + 1, prev[j] + 1), prev[j - 1] + cost);
            }
            int[] tmp = prev; prev = curr; curr = tmp;
        }
        return prev[b.length()];
    }

    /** Combined score used for duplicate grouping. */
    public static double similarity(String a, String b) {
        return Math.max(jaccardSimilarity(a, b), levenshteinSimilarity(a, b));
    }
}
