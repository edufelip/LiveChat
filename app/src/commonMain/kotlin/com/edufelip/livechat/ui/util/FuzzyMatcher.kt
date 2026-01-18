package com.edufelip.livechat.ui.util

/**
 * Fuzzy string matcher using Levenshtein distance algorithm.
 *
 * This matcher tolerates small typos and variations in search queries
 * to improve user experience in search functionality.
 *
 * Design decisions:
 * - Uses Levenshtein distance for typo tolerance
 * - Configurable threshold for maximum allowed edit distance
 * - Case-insensitive matching by default
 * - Optimized for short strings (typical search queries)
 *
 * Complexity: O(n*m) where n and m are string lengths
 */
object FuzzyMatcher {
    private const val DEFAULT_THRESHOLD = 2

    /**
     * Checks if query fuzzy matches the target string.
     *
     * @param query The search query string
     * @param target The target string to match against
     * @param threshold Maximum allowed edit distance (default: 2)
     * @return true if strings match within threshold, false otherwise
     */
    fun matches(
        query: String,
        target: String,
        threshold: Int = DEFAULT_THRESHOLD,
    ): Boolean {
        val normalizedQuery = query.trim().lowercase()
        val normalizedTarget = target.trim().lowercase()

        // Handle empty strings
        if (normalizedQuery.isEmpty() && normalizedTarget.isEmpty()) return true
        if (normalizedQuery.isEmpty() || normalizedTarget.isEmpty()) return false

        // Early exit: if query is substring of target, it's a match
        if (normalizedTarget.contains(normalizedQuery)) return true

        // Calculate Levenshtein distance
        val distance = levenshteinDistance(normalizedQuery, normalizedTarget)

        // Match if distance is within threshold
        return distance <= threshold
    }

    /**
     * Calculates the Levenshtein distance between two strings.
     *
     * The Levenshtein distance is the minimum number of single-character
     * edits (insertions, deletions, or substitutions) required to change
     * one string into another.
     *
     * Implementation uses dynamic programming with space optimization.
     *
     * @param source Source string
     * @param target Target string
     * @return Minimum edit distance
     */
    private fun levenshteinDistance(
        source: String,
        target: String,
    ): Int {
        val sourceLength = source.length
        val targetLength = target.length

        // Handle edge cases
        if (sourceLength == 0) return targetLength
        if (targetLength == 0) return sourceLength

        // Create distance matrix (only need current and previous row for space optimization)
        var previousRow = IntArray(targetLength + 1) { it }
        var currentRow = IntArray(targetLength + 1)

        // Fill the matrix
        for (i in 1..sourceLength) {
            currentRow[0] = i

            for (j in 1..targetLength) {
                val cost = if (source[i - 1] == target[j - 1]) 0 else 1

                // deletion, insertion, substitution
                currentRow[j] =
                    minOf(
                        previousRow[j] + 1,
                        currentRow[j - 1] + 1,
                        previousRow[j - 1] + cost,
                    )
            }

            // Swap rows
            val temp = previousRow
            previousRow = currentRow
            currentRow = temp
        }

        return previousRow[targetLength]
    }
}
