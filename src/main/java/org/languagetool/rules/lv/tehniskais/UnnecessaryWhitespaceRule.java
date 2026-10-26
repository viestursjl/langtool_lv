package org.languagetool.rules.lv.tehniskais;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.language.Latvian;
import org.languagetool.rules.ITSIssueType;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.lv.LatvianErrorCategories;
import org.languagetool.rules.lv.RuleDescriptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UnnecessaryWhitespaceRule extends Rule {

    public UnnecessaryWhitespaceRule(ResourceBundle messages, Latvian language) {
        super(messages);
        super.setCategory(LatvianErrorCategories.TEHN);
        setLocQualityIssueType(ITSIssueType.Whitespace);
    }

    @Override
    public String getId() {
        return "UNNECESSARY_WHITESPACE";
    }

    @Override
    public String getDescription() {
        return RuleDescriptions.whitespaceMessage;
    }

    @Override
    public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
        List<RuleMatch> ruleMatches = new ArrayList<>();

        // TODO: Te laikam ir nepieciešams dalījums teikumos, lai varētu papildus normāli apstrādāt punkta lietojumu
        //  Nezinu kādos gadījumos punktu vajag speciāli atdalīt bet varbūt tādi gadījumi ir ＼（〇_ｏ）／
        Pattern pattern = Pattern.compile("\\s+(?=[,!?);”])|(?<=[\\[(“„])\\s+");
        Matcher matcher = pattern.matcher(sentence.getText());

        while (matcher.find()) {
            int fromPos = matcher.start();
            int toPos =  matcher.end();

            String msg = getDescription();
            RuleMatch match = new RuleMatch(this, sentence, fromPos, toPos, msg);
            match.setSuggestedReplacement("");
            ruleMatches.add(match);
        }

        return toRuleMatchArray(ruleMatches);
    }
}
