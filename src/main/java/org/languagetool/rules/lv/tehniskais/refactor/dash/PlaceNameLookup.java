package org.languagetool.rules.lv.tehniskais.refactor.dash;

import org.languagetool.AnalyzedTokenReadings;
import java.io.IOException;
import java.util.List;


public interface PlaceNameLookup {
    boolean isRoute(List<AnalyzedTokenReadings> words) throws IOException;
    boolean isInList(List<AnalyzedTokenReadings> words) throws IOException;
    RouteResult checkRoute(List<AnalyzedTokenReadings> routeParts) throws IOException;
}
