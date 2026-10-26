package org.languagetool.rules.lv.tehniskais.refactor.dash;

public record RouteResult(
        boolean route,
        String correction
) {

    public static RouteResult notRoute() {
        return new RouteResult(false, null);
    }

    public static RouteResult route(String correction) {
        return new RouteResult(true, correction);
    }
}
