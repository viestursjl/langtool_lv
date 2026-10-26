package org.languagetool.rules.lv.tehniskais.refactor.dash;

import org.languagetool.*;
import org.languagetool.rules.*;
import org.languagetool.rules.lv.tehniskais.refactor.DashFormatRule;

import java.io.IOException;
import java.util.*;

public final class RouteDashRule implements DashRule {

    private final PlaceNameLookup placeNameLookup;

    public RouteDashRule(PlaceNameLookup placeNameLookup) {
        this.placeNameLookup = placeNameLookup;
    }

    @Override
    public Optional<RuleMatch> apply(
            DashFormatRule owner,
            AnalyzedSentence sentence,
            DashContext context) throws IOException {


        if (context.before == null || context.after == null) {
            return Optional.empty();
        }



        List<AnalyzedTokenReadings> routeParts =
                List.of(context.before, context.after);

        RouteResult result =
                placeNameLookup.checkRoute(routeParts);

        if (!result.route()) {
            return Optional.empty();
        }

        if (result.correction().equals(
                sentence.getText().substring(
                        context.before.getStartPos(),
                        context.after.getEndPos()))) {
            return Optional.empty();
        }

        return Optional.of(owner.createMatch(
                sentence,
                context.before.getStartPos(),
                context.after.getEndPos(),
                owner.getSpacingIssue1(),
                result.correction()
        ));
    }
}