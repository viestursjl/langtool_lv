package org.languagetool.rules.lv.tehniskais.refactor.dash;

import java.io.IOException;
import java.util.Optional;

import org.languagetool.AnalyzedSentence;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.lv.tehniskais.refactor.DashFormatRule;

public interface DashRule {

    Optional<RuleMatch> apply(
            DashFormatRule owner,
            AnalyzedSentence sentence,
            DashContext context
    ) throws IOException;
}
