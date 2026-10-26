package org.languagetool.rules.lv.tehniskais.refactor.dash;

import org.languagetool.AnalyzedTokenReadings;

public final class DashContext {

    final int dashTokenIndex;
    final int dashCharIndex;
    final String dash;

    final AnalyzedTokenReadings[] tokens;
    final AnalyzedTokenReadings containingToken;

    /**
     * Logical text immediately before/after this dash.
     *
     * For an embedded dash:
     *   Rīgas-Viļņas
     *   beforeText = "Rīgas"
     *   afterText  = "Viļņas"
     *
     * For a standalone dash:
     *   Rīgas – Viļņas
     *   beforeText = "Rīgas"
     *   afterText  = "Viļņas"
     */
    final String beforeText;
    final String afterText;

    /**
     * Physical neighboring tokens.
     *
     * These are null for an embedded dash because the dash is part
     * of containingToken.
     */
    final AnalyzedTokenReadings before;
    final AnalyzedTokenReadings after;

    final boolean embedded;
    final boolean whitespaceBefore;
    final boolean whitespaceAfter;

    private DashContext(
            int dashTokenIndex,
            int dashCharIndex,
            String dash,
            AnalyzedTokenReadings[] tokens,
            AnalyzedTokenReadings containingToken,
            String beforeText,
            String afterText,
            AnalyzedTokenReadings before,
            AnalyzedTokenReadings after,
            boolean embedded,
            boolean whitespaceBefore,
            boolean whitespaceAfter) {

        this.dashTokenIndex = dashTokenIndex;
        this.dashCharIndex = dashCharIndex;
        this.dash = dash;
        this.tokens = tokens;
        this.containingToken = containingToken;

        this.beforeText = beforeText;
        this.afterText = afterText;

        this.before = before;
        this.after = after;

        this.embedded = embedded;
        this.whitespaceBefore = whitespaceBefore;
        this.whitespaceAfter = whitespaceAfter;
    }

    public static DashContext from(
            AnalyzedTokenReadings[] tokens,
            int i) {

        AnalyzedTokenReadings tokenReading = tokens[i];
        String token = tokenReading.getToken();

        int dashCharIndex = findDashIndex(token);

        if (dashCharIndex == -1) {
            throw new IllegalArgumentException(
                    "Token does not contain a dash: " + token);
        }

        String dash = String.valueOf(token.charAt(dashCharIndex));

        boolean embedded =
                dashCharIndex > 0
                        || dashCharIndex + 1 < token.length();

        if (embedded) {
            return fromEmbeddedDash(
                    tokens,
                    i,
                    tokenReading,
                    dashCharIndex,
                    dash);
        }

        return fromStandaloneDash(
                tokens,
                i,
                tokenReading,
                dash);
    }

    private static DashContext fromEmbeddedDash(
            AnalyzedTokenReadings[] tokens,
            int i,
            AnalyzedTokenReadings tokenReading,
            int dashCharIndex,
            String dash) {

        String token = tokenReading.getToken();

        String beforeText = token.substring(0, dashCharIndex);
        String afterText = token.substring(dashCharIndex + 1);

        return new DashContext(
                i,
                dashCharIndex,
                dash,
                tokens,
                tokenReading,
                beforeText,
                afterText,
                null,
                null,
                true,
                false,
                false
        );
    }

    private static DashContext fromStandaloneDash(
            AnalyzedTokenReadings[] tokens,
            int i,
            AnalyzedTokenReadings tokenReading,
            String dash) {

        AnalyzedTokenReadings before =
                previousNonWhitespace(tokens, i);

        AnalyzedTokenReadings after =
                nextNonWhitespace(tokens, i);

        String beforeText =
                before == null ? "" : before.getToken();

        String afterText =
                after == null ? "" : after.getToken();

        boolean whitespaceBefore =
                tokenReading.isWhitespaceBefore();

        boolean whitespaceAfter =
                i + 1 < tokens.length
                        && tokens[i + 1].isWhitespaceBefore();

        return new DashContext(
                i,
                0,
                dash,
                tokens,
                tokenReading,
                beforeText,
                afterText,
                before,
                after,
                false,
                whitespaceBefore,
                whitespaceAfter
        );
    }

    private static int findDashIndex(String token) {
        for (int i = 0; i < token.length(); i++) {
            char c = token.charAt(i);

            if (isDash(c)) {
                return i;
            }
        }

        return -1;
    }

    private static boolean isDash(char c) {
        return c == '-'
                || c == '–'
                || c == '—';
    }

    public static boolean containsDash(
            AnalyzedTokenReadings token) {

        return findDashIndex(token.getToken()) != -1;
    }

    private static AnalyzedTokenReadings previousNonWhitespace(
            AnalyzedTokenReadings[] tokens,
            int i) {

        for (int j = i - 1; j >= 0; j--) {
            if (!tokens[j].getToken().trim().isEmpty()) {
                return tokens[j];
            }
        }

        return null;
    }

    private static AnalyzedTokenReadings nextNonWhitespace(
            AnalyzedTokenReadings[] tokens,
            int i) {

        for (int j = i + 1; j < tokens.length; j++) {
            if (!tokens[j].getToken().trim().isEmpty()) {
                return tokens[j];
            }
        }

        return null;
    }

    int dashStart() {
        return containingToken.getStartPos() + dashCharIndex;
    }

    int dashEnd() {
        return dashStart() + 1;
    }

    /**
     * Returns the complete token containing the dash.
     *
     * This is particularly important for route recognition:
     *
     *   "Latvija-Papua-Jaungvineja"
     *
     * must be examined as a whole so that the lookup can distinguish
     * the route separator from the internal hyphen in
     * "Papua-Jaungvineja".
     */
    String containingText() {
        return containingToken.getToken();
    }
}