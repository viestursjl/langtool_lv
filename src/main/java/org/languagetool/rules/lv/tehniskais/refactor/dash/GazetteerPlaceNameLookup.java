package org.languagetool.rules.lv.tehniskais.refactor.dash;

import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.tagging.Tagger;

import java.io.IOException;
import java.util.*;


public class GazetteerPlaceNameLookup implements PlaceNameLookup {

    private final Set<String> places;
    private final Tagger tagger;

    public GazetteerPlaceNameLookup(Set<String> places, Tagger tagger) {
        this.places = places;
        this.tagger = tagger;
    }

    @Override
    public boolean isRoute(List<AnalyzedTokenReadings> words) throws IOException {
        if (isInList(words)) {
            return false;
        }

        return buildRouteCorrection(words) != null;
    }

    @Override
    public boolean isInList(
            List<AnalyzedTokenReadings> words)
            throws IOException {

        StringBuilder concatenatedText = new StringBuilder();

        for (int i = 0; i < words.size(); i++) {
            concatenatedText.append(words.get(i).getToken());

            if (i + 1 != words.size()) {
                concatenatedText.append("–");
            }
        }

        List<AnalyzedTokenReadings> tagged =
                tagger.tag(List.of(concatenatedText.toString()));

        for (AnalyzedTokenReadings token : tagged) {
            for (AnalyzedToken reading : token.getReadings()) {

                String lemma = reading.getLemma();

                if (lemma != null && places.contains(lemma)) {
                    return true;
                }

                if (places.contains(token.getToken())) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public RouteResult checkRoute(
            List<AnalyzedTokenReadings> routeParts)
            throws IOException {

        if (isInList(routeParts)) {
            return RouteResult.notRoute();
        }

        String correction = buildRouteCorrection(routeParts);

        return RouteResult.route(correction);
    }


    private String buildRouteCorrection( List<AnalyzedTokenReadings> routeParts) {
        StringBuilder correction = new StringBuilder();

        for (int j = 0; j < routeParts.size(); j++) {
            boolean existsMatch = false;

            for (AnalyzedToken reading : routeParts.get(j).getReadings()) {
                String lemma = reading.getLemma();

                if (lemma != null && places.contains(lemma)) {
                    existsMatch = true;
                    correction.append(reading.getToken());
                    if (j + 1 != routeParts.size()) {
                        correction.append("–");
                    }
                    break;
                }

                // TODO: Handle hyphenated place names here.
            }
            if (!existsMatch) {
                return null;
            }
        }

        return correction.toString();
    }
}
