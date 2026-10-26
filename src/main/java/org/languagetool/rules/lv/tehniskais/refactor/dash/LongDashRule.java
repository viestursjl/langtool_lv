package org.languagetool.rules.lv.tehniskais.refactor.dash;

import java.io.IOException;
import java.util.Optional;

import org.languagetool.AnalyzedSentence;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.lv.tehniskais.refactor.DashFormatRule;

public final class LongDashRule implements DashRule {

    @Override
    public Optional<RuleMatch> apply(
            DashFormatRule owner,
            AnalyzedSentence sentence,
            DashContext context
    ) throws IOException {

        if (!context.dash.equals("—")) {
            return Optional.empty();
        }

        RuleMatch match = owner.createMatch(
                sentence,
                context.dashStart(),
                context.dashEnd(),
                owner.getDescription(),
                "–"
        );

        return Optional.of(match);
    }
}
