package org.languagetool.rules.lv.tehniskais.refactor;

import org.languagetool.*;
import org.languagetool.language.Latvian;
import org.languagetool.rules.*;
import org.languagetool.rules.lv.LatvianErrorCategories;
import org.languagetool.rules.lv.tehniskais.refactor.dash.*;
import org.languagetool.tagging.Tagger;

import java.io.IOException;
import java.util.*;


public class DashFormatRule extends Rule {
    private static final String PLACENAME_FILENAME = "/lv/gazetteer.txt";
    private static final String INVENTORS_FILENAME = "/lv/inventors.txt";
    private final Tagger tagger;
    private final List<DashRule> rules;

    public DashFormatRule(ResourceBundle messages, Latvian language) throws IOException {
        super(messages);
        super.setCategory(LatvianErrorCategories.TEHN);
        setLocQualityIssueType(ITSIssueType.Whitespace);

        this.tagger = language.getTagger();

        Set<String> placeNames = loadGazetteer(PLACENAME_FILENAME);

        PlaceNameLookup placeNameLookup = new GazetteerPlaceNameLookup(placeNames, tagger);

        this.rules = List.of(
                new RouteDashRule(placeNameLookup),
                new LongDashRule()
        );
    }

    @Override
    public String getId() {
        return "";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
        List<RuleMatch> matches = new ArrayList<>();

//        matches.addAll(yearSpanRule(...));

        AnalyzedTokenReadings[] tokens = sentence.getTokens();

        for (int i = 0; i < tokens.length; i++) {
            if (!DashContext.containsDash(tokens[i])) {
                continue;
            }

            DashContext context = DashContext.from(tokens, i);

            for (DashRule rule : rules) {
                Optional<RuleMatch> match = rule.apply(this, sentence, context);

                if (match.isPresent()) {
                    matches.add(match.get());
                    break;
                }
            }
        }

        return toRuleMatchArray(matches);
    }


    public RuleMatch createMatch(
            AnalyzedSentence sentence,
            int start,
            int end,
            String message,
            String replacement) {

        RuleMatch match = new RuleMatch(
                this,
                sentence,
                start,
                end,
                message
        );

        match.setSuggestedReplacement(replacement);

        return match;
    }


    public static HashSet<String> loadGazetteer(String path) {
        HashSet<String> l = new HashSet<>();
        List<String> lines = JLanguageTool.getDataBroker().getFromResourceDirAsLines(path);
        for (String line : lines) {
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            l.add(line.trim());
        }
        return l;
    }

    public String getSpacingIssue1() {
        return "";
    }
}