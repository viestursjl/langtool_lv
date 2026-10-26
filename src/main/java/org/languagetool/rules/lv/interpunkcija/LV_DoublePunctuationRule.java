package org.languagetool.rules.lv.interpunkcija;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.*;
import org.languagetool.rules.lv.LatvianErrorCategories;
import org.languagetool.rules.lv.RuleDescriptions;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/* Šī noteikuma loģika ņemta no – base DoublePunctuationRule
Pievienots vienīgi, ka divpunkte tiek saglabāta citātu izlaidumos – [..]
* */

public class LV_DoublePunctuationRule extends Rule {

    public LV_DoublePunctuationRule(ResourceBundle messages) {
        super(messages);
        super.setCategory(LatvianErrorCategories.PUNKT);
        setLocQualityIssueType(ITSIssueType.Typographical);
    }

    public LV_DoublePunctuationRule(ResourceBundle messages, URL url) {
        super(messages);
        super.setCategory(LatvianErrorCategories.PUNKT);
        setLocQualityIssueType(ITSIssueType.Typographical);
        if (url != null) {
            setUrl(url);
        }
    }

    @Override
    public String getId() {
        return "DOUBLE_PUNCTUATION";
    }

    @Override
    public final String getDescription() {
        return RuleDescriptions.doublePunctuationMessage;
    }

    public String getCommaCharacter() {
        return ",";
    }

    @Override
    public final RuleMatch[] match(AnalyzedSentence sentence) {
        List<RuleMatch> ruleMatches = new ArrayList<>();
        AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();
        int startPos = 0;
        int dotCount = 0;
        int commaCount = 0;
        for (int i = 1; i < tokens.length; i++) {
            String token = tokens[i].getToken();
            String nextToken = null;
            String prevToken = null;
            String prevPrevToken = null;
            if (i < tokens.length - 1) {
                nextToken = tokens[i + 1].getToken();
            }
            if (i > 1) {
                prevToken = tokens[i-1].getToken();
                prevPrevToken = tokens[i - 2].getToken();
            }
            if (".".equals(token)) {
                dotCount++;
                commaCount = 0;
                startPos = tokens[i].getStartPos();
            } else if (getCommaCharacter().equals(token)) {
                commaCount++;
                dotCount = 0;
                startPos = tokens[i].getStartPos();
            }

            Boolean quoteCollapse = ("[".equals(prevPrevToken) && "]".equals(nextToken));
            if (dotCount == 2 && !".".equals(nextToken) && !"…".equals(nextToken) &&
                    !"/".equals(token) && !"/".equals(nextToken) &&  /* Unix path */
                    !"\\".equals(token) && !"\\".equals(nextToken) &&  /* Windows path */
                    !"?".equals(prevPrevToken) && !"!".equals(prevPrevToken) &&
                    !"…".equals(prevPrevToken) && !".".equals(prevPrevToken) && !quoteCollapse) {
                int fromPos = Math.max(0, startPos - 1);
                RuleMatch ruleMatch = new RuleMatch(this, sentence, fromPos, startPos + 1,
                        getDotMessage(), messages.getString("double_dots_short"));
                ruleMatch.addSuggestedReplacement(".");
                ruleMatch.addSuggestedReplacement("…");
                ruleMatches.add(ruleMatch);
                dotCount = 0;
            } else if (commaCount == 2 && !getCommaCharacter().equals(nextToken)) {
                int fromPos = Math.max(0, startPos - 1);
                RuleMatch ruleMatch = new RuleMatch(this, sentence, fromPos, startPos + 1,
                        getCommaMessage(), messages.getString("double_commas_short"));
                ruleMatch.setSuggestedReplacement(getCommaCharacter());
                ruleMatches.add(ruleMatch);
                commaCount = 0;
            }
            if (!".".equals(token) && !getCommaCharacter().equals(token)) {
                dotCount = 0;
                commaCount = 0;
            }
        }

        return toRuleMatchArray(ruleMatches);
    }


    protected String getDotMessage() {
        return messages.getString("two_dots");
    }

    protected String getCommaMessage() {
        return messages.getString("two_commas");
    }
}
