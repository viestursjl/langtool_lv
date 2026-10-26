package org.languagetool.rules.lv.interpunkcija;

import org.languagetool.rules.Categories;
import org.languagetool.rules.GenericUnpairedBracketsRule;
import org.languagetool.rules.ITSIssueType;
import org.languagetool.rules.lv.LatvianErrorCategories;
import org.languagetool.rules.lv.RuleDescriptions;

import java.util.*;
import java.util.regex.Pattern;

public class LV_GenericUnpairedBracketsRule extends GenericUnpairedBracketsRule {
    private static final Pattern NUMERALS_EN =
            Pattern.compile("(?i)\\d{1,2}?[a-z']*|M*(D?C{0,3}|C[DM])(L?X{0,3}|X[LC])(V?I{0,3}|I[VX])$");
    private static final Pattern PUNCTUATION = Pattern.compile("[\\p{Punct}…–—]");
    private static final Pattern PUNCTUATION_NO_DOT =
            Pattern.compile("[ldmnstLDMNST]'|[–—\\p{Punct}&&[^.]]");
    // "[ldmnst]'" allows dealing with apostrophed words in Catalan (i.e. l'home)

    private final List<String> startSymbols;
    private final List<String> endSymbols;
    private final Map<String,Boolean> uniqueMap;
    private final String ruleId;
    private final Pattern numerals;

    public LV_GenericUnpairedBracketsRule(ResourceBundle messages, List<String> startSymbols, List<String> endSymbols) {
        super(messages);
        this.ruleId = "UNPAIRED_BRACKETS";
        super.setCategory(LatvianErrorCategories.PUNKT);
        if (startSymbols.size() != endSymbols.size()) {
            throw new IllegalArgumentException("Different number of start and end symbols: " + startSymbols + " vs. " + endSymbols);
        }
        this.startSymbols = startSymbols;
        this.endSymbols = endSymbols;
        this.numerals = Objects.requireNonNull(NUMERALS_EN);
        this.uniqueMap = uniqueMapInit();
        setLocQualityIssueType(ITSIssueType.Typographical);
    }

    @Override
    public String getDescription() {
        if (startSymbols.size() < endSymbols.size()) {
            return RuleDescriptions.bracketsOpenMissingMessage;
        } else {
            return RuleDescriptions.bracketsClosedMissingMessage;
        }
    }

    private Map<String, Boolean> uniqueMapInit() {
        Map<String,Boolean> uniqueMap = new HashMap<>();
        for (String endSymbol : endSymbols) {
            int found = 0;
            for (String endSymbol1 : endSymbols) {
                if (endSymbol1.equals(endSymbol)) {
                    found++;
                }
            }
            uniqueMap.put(endSymbol, found == 1);
        }
        return Collections.unmodifiableMap(uniqueMap);
    }
}
