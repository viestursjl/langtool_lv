package org.languagetool.rules.lv.stils;

import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.language.Latvian;
import org.languagetool.rules.ITSIssueType;
import org.languagetool.rules.Rule;
import org.languagetool.rules.lv.LatvianErrorCategories;
import org.languagetool.AnalyzedSentence;
import org.languagetool.rules.*;
import org.languagetool.rules.lv.RuleDescriptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class VaratRule extends Rule {

    public VaratRule(ResourceBundle messages, Latvian language) {
        super(messages);
        super.setCategory(LatvianErrorCategories.TEHN);
        setLocQualityIssueType(ITSIssueType.Whitespace);
    }

    @Override
    public String getId() {
        return "VARAT_GRIBAT";
    }

    @Override
    public String getDescription() {
        return RuleDescriptions.varatMessage;
    }

    @Override
    public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
        List<RuleMatch> ruleMatches = new ArrayList<>();
        AnalyzedTokenReadings[] tokens = sentence.getTokens();

        Pattern pattern = Pattern.compile("(var|grib)iet", Pattern.CASE_INSENSITIVE);

        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i].getToken();
            Matcher matcher = pattern.matcher(token);
            if (matcher.matches()) {
                String msg = getDescription();
                RuleMatch match = new RuleMatch(this, sentence, tokens[i].getStartPos(), tokens[i].getEndPos(), msg);
                match.setSuggestedReplacement(matcher.group(1)+"at");
                ruleMatches.add(match);
            }
        }

        return toRuleMatchArray(ruleMatches);
    }
}