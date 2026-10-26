package org.languagetool.rules.lv.tehniskais;

import org.languagetool.*;
import org.languagetool.language.Latvian;
import org.languagetool.rules.*;
import org.languagetool.rules.lv.LatvianErrorCategories;
import org.languagetool.rules.lv.RuleDescriptions;
import org.languagetool.tagging.Tagger;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateTimeFormatRule extends Rule {

    public DateTimeFormatRule(ResourceBundle messages, Latvian language) {
        super(messages);
        super.setCategory(LatvianErrorCategories.TEHN);
        setLocQualityIssueType(ITSIssueType.Whitespace);
    }

    @Override
    public String getId() {
        return "DATUMU_FORMATĒJUMS";
    }

    @Override
    public String getDescription() {
        return RuleDescriptions.dateFormatMessage;
    }

    @Override
    public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
        return new RuleMatch[0];
    }
}
