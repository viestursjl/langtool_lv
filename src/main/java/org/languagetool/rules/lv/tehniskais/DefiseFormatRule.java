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
import java.util.stream.Collectors;


public class DefiseFormatRule extends Rule {
    private static final Character nonBreakingSpace = ' ';
    private static final Character longDomuzīme = '—';
    private static final Character domuzīme = '–';
    private static final Character defise = '-';
    private static final String dashes = "[-‒–—]";

    private static final String PLACENAME_FILENAME = "/lv/gazetteer.txt";
    private static final String INVENTORS_FILENAME = "/lv/inventors.txt";
    private static final String COMPOUND_FILENAME = "/lv/compounds.txt";
    private Tagger tagger;
    private StringBuilder dashCorrected;

    public DefiseFormatRule(ResourceBundle messages, Latvian language) {
        super(messages);
        super.setCategory(LatvianErrorCategories.TEHN);
        setLocQualityIssueType(ITSIssueType.Whitespace);
        this.tagger = language.getTagger();
    }

    @Override
    public String getId() {
        return "DEFISES_LIETOJUMS";
    }

    @Override
    public String getDescription() {
        return RuleDescriptions.hyphenMessage;
    }

    public String getSpacingIssue1() {
        return RuleDescriptions.dashFormatMessage;
    }

    public String getDomuzimeIssue() {
        return RuleDescriptions.dashMessage;
    }


    /*
    Šajā failā paredzēts apskatīt tās kļūdas, kur tekstā svītras (domuzīmes jeb defises) ir atsevišķi tokeni,
    kas ir neatbilstoši kontekstam pielietoti. Domuzīmes vienmēr ir atsevišķi tokeni, kamēr defises, tikai gadījumos,
    kad ap tām ir atstarpes.
    * */


    public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
        List<RuleMatch> ruleMatches = new ArrayList<>();
        String text = sentence.getText();
        AnalyzedTokenReadings[] tokens = sentence.getTokens();

        // Vispirms apskatām specifiskākos gadījumus
        // TODO: Līdzīgs likums ir arī ConnectingWhitespace failā, pārliecināties par nepieciešamību
        ruleMatches = yearSpanRule(ruleMatches, sentence);

        // Apskatām situāciju, kur svītras simbols ir teikuma sākumā
        // Tas nav saistīts ar pārējiem apskatāmajiem gadījumiem, kas ir starp vārdiem
        // TODO: Pārliecināties vai šis matcher strādā pareizi, ja seko cipars drīzāk lietota kā mīnus zīme
        Pattern pattern = Pattern.compile("(?U)^\\s*"+dashes+"(?!\\d)", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();

            String correction = domuzīme.toString();
            if (!correction.matches(matcher.group())) {
                String msg = RuleDescriptions.quoteDashMessage;
                RuleMatch match = new RuleMatch(this, sentence, start, end+1, msg);
                match.setSuggestedReplacement(correction);
                ruleMatches.add(match);
            }
        }


        // Vispirms apskatām situāciju ar 2 vārdiem ar Lielajiem sākuma burtiem
        // e.g. Ribetropa–Molotova, Vaira Vīķe-Freiberga, Kohtla-Jerve
        pattern = Pattern.compile("(?U)(\\p{Lu}\\p{Ll}+)((?:\\s*"+dashes+"\\s*\\p{Lu}\\p{Ll}+)+)");
        matcher = pattern.matcher(text);

        int tokenIndex = 0;
        while (matcher.find()) {
            // FIXME: Pārejot uz RegEx rodas problēma, ka vairs vārdlietojumi nav tokenizēti
            //  vai nu jānotokenizē, vai jāpārtaisa check funkcijas
            // Analyzed token readings ietver minēto lemmu un locījumu, kas ir svarīgi check funkcijām
            // Ideja — tokeniem AnalyzedTokens sarakstā ir zināmi start end punkti
            // Varam atlasīt tikai tos tokenus, kas ir matcher intervālā
            // TODO: Pārbaudīt vai strādā???
            int matchStart = matcher.start();
            int matchEnd = matcher.end();
            List<AnalyzedTokenReadings> matchingTokens = new ArrayList<>();

            // Skip tokens before current match
            // Process tokens inside match
            while (tokenIndex < tokens.length && tokens[tokenIndex].getEndPos() <= matchStart) tokenIndex++;
            int i = tokenIndex;
            while (i < tokens.length && tokens[i].getStartPos() < matchEnd) {
                AnalyzedTokenReadings t = tokens[i];
                if (t.getStartPos() >= matchStart && t.getEndPos() <= matchEnd) {
                    matchingTokens.add(tokens[i]);
                } else if (t.getEndPos() > matchEnd) {
                    break;
                }
                i++;
            }

            // FIXME: Vai gadījumā šeit es nevaru vnk izmantot RegEx grupas pa taisno nevis šitādu filtrēšanu???
            // This line filters out all non-text tokens from the regex match, i.e. whitespaces, dashes, hyphens
            List<AnalyzedTokenReadings> filteredTokens = matchingTokens.stream()
                    .filter(element -> !element.getToken().matches("(?U)(\\s|"+dashes+")*"))
                    .toList();
            List<String> filteredText = filteredTokens.stream()
                    .map(AnalyzedTokenReadings::getToken)
                    .flatMap(token -> Arrays.stream(token.split(defise.toString())))
                    .map(String::trim)
                    .filter(token -> !token.isEmpty())
                    .toList();
            List<AnalyzedTokenReadings> taggedFilteredTokens = this.tagger.tag(filteredText);

            // TODO: Pārliecionāties ka šīs funkcijas strādā gan priekš tokenizēta, gan netokinizēta ievada
            //  ACTUALLY: Ja jau strādātu pie netokinizēta vai vispār ir jēga sadalīt jau šeit???
            // Efektīvi pašlaik loģika šim gabalam ir sadalīta pa DefiseFormatRule un DashFormatRule
            //  apvienot varētu būt sarežģīti :<
            // Vai arī tā kā DashFormatRule faila defises loģika strādā uz vnk String tad es varu ņemt to?
            int start = matcher.start();
            int end = matcher.end();

            String hyphenConcat = concat(taggedFilteredTokens, defise).toString();
            if (checkGazetteer(hyphenConcat, PLACENAME_FILENAME)) {
                if (!hyphenConcat.equals(matcher.group())) {
                    // e.g. Kohtla–Jerve, Kohtla- Jerve
                    String msg = RuleDescriptions.doublePlacenameMessage;
                    String correction = hyphenConcat;
                    RuleMatch match = new RuleMatch(this, sentence, start, end, msg);
                    match.setSuggestedReplacement(correction);
                    ruleMatches.add(match);
                }
            }
            else {
                // Maršrutus atdala ar domuzīmi, e.g. Rīgas–Viļņas, Rīga–Roja–Kolka
                String potentialRoute = returnRoute(taggedFilteredTokens);
                if (!potentialRoute.isEmpty()) {
                    if (!potentialRoute.equals(matcher.group())) {
                        String msg = RuleDescriptions.dashFormatMessage;
                        String correction = potentialRoute;
                        RuleMatch match = new RuleMatch(this, sentence, start, end, msg);
                        match.setSuggestedReplacement(correction);
                        ruleMatches.add(match);
                    }
                }
                else {
                     if (checkInList(taggedFilteredTokens, INVENTORS_FILENAME)) {
                        // Divu dažādu izgudrotāju uzvārdus atdala ar domuzīmi, e.g. Bozes–Einšteina
                        String correction = concat(taggedFilteredTokens).toString();
                        if (!correction.equals(matcher.group())) {
                            String msg = RuleDescriptions.NBSPDashMessage;
                            RuleMatch match = new RuleMatch(this, sentence, start, end, msg);
                            match.setSuggestedReplacement(correction);
                            ruleMatches.add(match);
                        }
                    }
                    else {
                        // Ja nav neviens no specgadījumiem, tad ir dažas alternatīvas
                        // Dubultuzvārdi, vai parasta domuzīme
                        // e.g. Vīķe-Freiberga, Jānis – Pēterim

                        if (matcher.group().matches("(?U)(\\p{Lu}\\p{Ll}+)(\\s+"+dashes+"\\s+(\\p{Lu}\\p{Ll}+))+")) {
                            // If whitespaces from both sides, assume dash
                            StringBuilder correction = new StringBuilder(matcher.group(1));
                            List<String> others = List.of((matcher.group(2).split("(?U)\\s"+dashes+"\\s+")));
                            others = others.stream().filter(token -> !token.isEmpty()).toList();
                            for (String other: others) {
                                correction.append(nonBreakingSpace.toString() + domuzīme.toString() + " "+ other);
                            }
                            if (!correction.toString().equals(matcher.group())) {
                                String msg = RuleDescriptions.NBSPDashMessage;
                                if (correction.toString().contains(defise.toString())) {msg += " "+RuleDescriptions.dashMessage;}
                                if (correction.toString().contains(longDomuzīme.toString())) {msg += " "+RuleDescriptions.mDashMessage;}
                                RuleMatch match = new RuleMatch(this, sentence, start, end, msg);
                                match.setSuggestedReplacement(correction.toString());
                                ruleMatches.add(match);
                            }
                        }
                        else {
                            // If no whitespaces, assume hyphen
                            String correction = concat(taggedFilteredTokens, defise).toString();
                            if (!correction.equals(matcher.group())) {
                                // FIXME: Could possibly be a generic RuleDescriptions.hyphenMessage situation as well
                                String msg = RuleDescriptions.doubleSurnameMessage;
                                RuleMatch match = new RuleMatch(this, sentence, start, end, msg);
                                match.setSuggestedReplacement(correction);
                                ruleMatches.add(match);
                            }
                        }
                    }
                }
            }
        }

        // Citādi apskatām gadījumus, kad vismaz viens no vārdiem ir ar mazo burtu?

        // Aplūkojam gadījumus, kad pirmais vārds ir potenciāli ar lielo burtu
        // Aplūkojam pārējos gadījumus, abi ir ar mazajiem
        // FIXME: E-pasts vs C-vitamīns -> E-pasts, bet C vitamīns
        pattern = Pattern.compile("(?U)\\b\\p{L}?\\p{Ll}+(\\s*"+dashes+"\\s*)\\p{Ll}+\\b");
        matcher = pattern.matcher(text);

        tokenIndex = 0;
        while (matcher.find()) {
            int matchStart = matcher.start();
            int matchEnd = matcher.end();
            List<AnalyzedTokenReadings> matchingTokens = new ArrayList<>();

            // Skip tokens before current match
            // Process tokens inside match
            while (tokenIndex < tokens.length && tokens[tokenIndex].getEndPos() <= matchStart) tokenIndex++;
            int i = tokenIndex;
            while (i < tokens.length && tokens[i].getStartPos() < matchEnd) {
                AnalyzedTokenReadings t = tokens[i];
                if (t.getStartPos() >= matchStart && t.getEndPos() <= matchEnd) {
                    matchingTokens.add(tokens[i]);
                } else if (t.getEndPos() > matchEnd) {
                    break;
                }
                i++;
            }

            // FIXME: Vai gadījumā šeit es nevaru vnk izmantot RegEx grupas pa taisno nevis šitādu filtrēšanu???
            // This line filters out all non-text tokens from the regex match, i.e. whitespaces, dashes, hyphens
            List<AnalyzedTokenReadings> filteredTokens = matchingTokens.stream()
                    .filter(element -> !element.getToken().matches("(\\s|"+dashes+")*"))
                    .toList();
            List<String> filteredText = filteredTokens.stream()
                    .map(AnalyzedTokenReadings::getToken)
                    .flatMap(token -> Arrays.stream(token.split(defise.toString())))
                    .filter(token -> !token.isEmpty())
                    .toList();
            List<AnalyzedTokenReadings> taggedFilteredTokens = this.tagger.tag(filteredText);

            if (checkInList(taggedFilteredTokens, COMPOUND_FILENAME)) {
                String correction = concat(taggedFilteredTokens, defise).toString();
                if (!correction.equals(matcher.group())) {
                    String msg = RuleDescriptions.hyphenMessage;
                    RuleMatch match = new RuleMatch(this, sentence, matchStart, matchEnd, msg);
                    match.setSuggestedReplacement(correction);
                    ruleMatches.add(match);
                }
            }
            else {
                // FIXME: I feel like this should be reworked, but this is fine for now
                boolean genetive_construct = true;
                boolean month_construct = true;
                boolean weekday_construct = true;
                for (AnalyzedTokenReadings readings: taggedFilteredTokens) {
                    boolean gen_dsk = false;
                    boolean month_form = false;
                    boolean weekday_form = false;
                    for (AnalyzedToken reading: readings.getReadings()) {
                        if (!reading.hasNoTag()) {
                            if (reading.getPOSTag().matches("n.*[pd]g.")) gen_dsk = true;
                            if (reading.getLemma().matches("(janvā|februā|((septe|nove|dece)m|okto)b)(ris)|marts|aprīlis|maijs|jū[nl]ijs|augusts")) month_form = true;
                            if (reading.getLemma().matches("(pirm|otr|treš|ceturt|piekt|sest|svēt)diena")) weekday_form = true;
                        }
                    }
                    if (!gen_dsk) genetive_construct = false;
                    if (!month_form) month_construct = false;
                    if (!weekday_form) weekday_construct = false;
                }
                if (!taggedFilteredTokens.isEmpty() && (genetive_construct || month_construct || weekday_construct)) {
                    String correction = concat(taggedFilteredTokens).toString();
                    if (!correction.equals(matcher.group())) {
                        String msg = RuleDescriptions.dashFormatMessage;
                        RuleMatch match = new RuleMatch(this, sentence, matchStart, matchEnd, msg);
                        match.setSuggestedReplacement(correction);
                        ruleMatches.add(match);
                    }
                } else if (matcher.group(1).matches("(?U)\\s?[‒–—]\\s?")) {
                    String correction = nonBreakingSpace.toString()+domuzīme.toString()+" ";
                    if (!matcher.group(1).equals(correction)) {
                        String msg = RuleDescriptions.NBSPDashMessage;
                        RuleMatch match = new RuleMatch(this, sentence, matcher.start(1), matcher.end(1), msg);
                        match.setSuggestedReplacement(correction);
                        ruleMatches.add(match);
                    }
                }
            }
        }

        // FIXME: Pārvietot šo funckiju kā sekundāru izsaukumu gadījumos, kad citi likumi satur pārgaro domuzīmi
        //  Ja šis ir vienīgais labojums, message tekstam jābūt "", vai arī jāspēj izsaut bez String message
        // Aizvietojam visas pārgarās domuzīmes ar parastajām domuzīmēm
        int index = text.indexOf(longDomuzīme);
        while (index != -1) {
            String msg = RuleDescriptions.mDashMessage;
            String correction = domuzīme.toString();
            RuleMatch match = new RuleMatch(this, sentence, index, index+1, msg);
            match.setSuggestedReplacement(correction.toString());
            ruleMatches.add(match);

            index = text.indexOf(longDomuzīme, index + 1);
        }


        // TODO: Šeit pielietot parastos noteikumus
        pattern = Pattern.compile("(?U)\\s+"+dashes+"\\s+");
        matcher = pattern.matcher(text);
        while (matcher.find()) {
            String correction = nonBreakingSpace.toString()+domuzīme.toString()+" ";
            int matchStart = matcher.start();
            int matchEnd = matcher.end();

            if (!correction.equals(matcher.group())) {
                String msg = RuleDescriptions.dashMessage;
                RuleMatch match = new RuleMatch(this, sentence, matchStart, matchEnd, msg);
                match.setSuggestedReplacement(correction);
                ruleMatches.add(match);
            }
        }


        return toRuleMatchArray(ruleMatches);
    }


    // TODO: Varbūt būtu labāk pārlikt maršruta pārbaudīšanu atsevišķā funkcijā
    public String returnRoute(List<AnalyzedTokenReadings> route_parts) throws IOException {
        Set<String> places = loadGazetteer(PLACENAME_FILENAME);
        boolean isRoute = true;

        // Ar StringBuilder izveidojam kopējo "maršrutu" vienā String elementā
        StringBuilder concatenatedText = new StringBuilder();
        for (int i = 0; i < route_parts.size(); i++) {
            concatenatedText.append(route_parts.get(i).getToken());
            if (i+1 != route_parts.size()) concatenatedText.append(domuzīme);
        }

        List<String> tot = Collections.singletonList(concatenatedText.toString());
        List<AnalyzedTokenReadings> tagged_whole_word = this.tagger.tag(tot);

        // Pārbaudām vai nav saliktais vietvārds
        for (AnalyzedTokenReadings token: tagged_whole_word) {
            for (AnalyzedToken possibleLemma: token.getReadings()) {
                if (places.contains(possibleLemma)) {
                    isRoute = false;
                } else {
                    if (places.contains(token)) {
                        isRoute = false;
                    }
                }
                if (!isRoute) continue;
            }
        }
        if (!isRoute) return "";

        StringBuilder correction = new StringBuilder();
        for (int j = 0; j < route_parts.size(); j++) {
            boolean existsMatch = true;
            for (AnalyzedToken reading: route_parts.get(j).getReadings()) {
                boolean partExistsMatch = false;
                String a = route_parts.get(j).getToken();
                if (places.contains(route_parts.get(j).getToken()) || places.contains(reading.getLemma())) {
                    partExistsMatch = true;
                    correction.append(reading.getToken());
                    if (j+1 != route_parts.size()) correction.append(domuzīme);
                    break;
                }
                else {
                    // Check for seperated placenames that contain defise within them
                    // FIXME: Piemēram gadījumā Papua-Jaungvinejas, LVTagger nespēj atsevišķi
                    //  noteikt lemmu tokenam "Jaunvinejas"... tas nobrucina pašreizējo plānu
                    //  Vajadzētu tad laikam ieslēgt minēšanu tagerī, man šķiet pēc noklusējuma tas bija izslēgts
                    //  Plus jāpārliecinās, ka ieteiktais labojums arī saglabā pareizās defises
                    for (String place: places) {
                        int div = place.indexOf(defise);
                        if (place.indexOf(defise) != -1) {
                            if (place.substring(0,div).contains(reading.getToken()) && j+1 != route_parts.size() && route_parts.size() > 2) {
                                for (AnalyzedToken reading2: route_parts.get(j+1).getReadings()) {
                                    String lemma = "";
                                    if (reading2.getLemma() != null) lemma = reading2.getLemma();
                                    else lemma = reading2.getToken();
                                    if (place.substring(div+1).contains(lemma)) {
                                        partExistsMatch = true;
                                        correction.append(reading.getToken()).append(defise).append(lemma);
                                        if (j+2 < route_parts.size()) correction.append(domuzīme);
                                        j++;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
                if (!partExistsMatch) return "";
            }
            if (!existsMatch) return "";
        }
        return correction.toString();
    }



    /***
    * Šī funkcija ir primāri paredzēta tieši ar defisēm/domuzīmēm veidotu maršrutu atpazīšanai
    * @param route_parts - padotais tokenizētais teikuma fragments
    * @param gazetteer - saraksta ievada faila nosaukums
    * @return True, ja fragments veido kādu no salikteņiem no padotā saraksta, citādi False
    * ***/
    // TODO: Varbūt būtu labāk pārlikt maršruta pārbaudīšanu atsevišķā funkcijā
    public Boolean checkGazetteer(List<AnalyzedTokenReadings> route_parts, String gazetteer) throws IOException {
        Set<String> places = loadGazetteer(gazetteer);
        Pattern dash = Pattern.compile(dashes);
        boolean isRoute = true;

        // Ar StringBuilder izveidojam kopējo "maršrutu" vienā String elementā
        StringBuilder concatenatedText = concat(route_parts);
        // Tokenizējam, lai noteiktu vai ir locījumā
        List<String> tot = Collections.singletonList(concatenatedText.toString());
        List<AnalyzedTokenReadings> tagged_whole_word = this.tagger.tag(tot);
//        System.out.println(concatenatedText);

        // Pārbaudām vai nav saliktais vietvārds
        for (AnalyzedTokenReadings token: tagged_whole_word) {
            for (AnalyzedToken possibleLemma: token.getReadings()) {
                if (places.contains(possibleLemma)) {
                    isRoute = false;
                } else {
                    if (places.contains(token.getToken())) {
                        isRoute = false;
                    }
                }
                if (!isRoute) continue;
            }
        }
        if (!isRoute) return false;

        StringBuilder correction = new StringBuilder();
        for (int j = 0; j < route_parts.size(); j++) {
            boolean existsMatch = false;
            for (AnalyzedToken reading: route_parts.get(j).getReadings()) {
//                System.out.println(":::"+reading.getToken());
                if (places.contains(reading.getLemma())) {
                    existsMatch = true;
                    correction.append(reading.getToken());
                    if (j+1 != route_parts.size()) correction.append(domuzīme);
                    break;
                } else if (reading.getToken().matches(dash.toString())) {
                    existsMatch = true;
                    continue;
                } else {
                    // Check for seperated placenames that contain defise within them
                    // FIXME: Piemēram gadījumā Papua-Jaungvinejas, LVTagger nespēj atsevišķi
                    //  noteikt lemmu tokenam "Jaunvinejas"... tas nobrucina pašreizējo plānu
                    //  Vajadzētu tad laikam ieslēgt minēšanu tagerī, man šķiet pēc noklusējuma tas bija izslēgts
                    //  Plus jāpārliecinās, ka ieteiktais labojums arī saglabā pareizās defises
                    for (String place: places) {
                        int div = place.indexOf(defise);
                        if (place.indexOf(defise) != -1) {
                            if (place.substring(0,div).contains(reading.getToken()) && j+1 != route_parts.size() && route_parts.size() > 2) {
                                for (AnalyzedToken reading2: route_parts.get(j+1).getReadings()) {
                                    String lemma = "";
                                    if (reading2.getLemma() != null) lemma = reading2.getLemma();
                                    else lemma = reading2.getToken();
                                    if (place.substring(div+1).contains(lemma)) {
                                        existsMatch = true;
                                        correction.append(reading.getToken()).append(defise).append(lemma);
                                        if (j+2 < route_parts.size()) correction.append(domuzīme);
                                        j++;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (!existsMatch) {
                isRoute = false;
            }
        }
        if (isRoute) {
            this.dashCorrected = concatenatedText;
        }
        return isRoute;
    }

    /***
    * Šī funkcija ir primāri paredzēta tieši ar defisēm/domuzīmēm apvienotu vārdu uztveršanai
    * @param parts - padotais tokenizētais teikuma fragments
    * @param list - saraksta ievada faila nosaukums
    * @return True, ja fragments veido kādu no salikteņiem no padotā saraksta, citādi False
    * ***/
    public Boolean checkInList(List<AnalyzedTokenReadings> parts, String list) throws IOException {
        Set<String> places = loadGazetteer(list);
        // Ar StringBuilder izveidojam kopējo salikteni/kombināciju vienā String elementā
        StringBuilder concatenatedText;
        if (list.equals(COMPOUND_FILENAME)) concatenatedText = concat(parts, defise);
        else concatenatedText = concat(parts);
        // Tokenizējam, lai noteiktu vai ir locījumā (vispirms izveidojam sarakstu ar 1 el, jo tagot var tikai sarakstu)
        List<String> tot = Collections.singletonList(concatenatedText.toString());
        List<AnalyzedTokenReadings> tagged_whole_word = this.tagger.tag(tot);

        // Pārbaudām vai eksistē šis saliktenis kādā no sarakstiem
        // Primāri priekš fiksētiem izteicieniem, kas vienmēr ir vienā locījumā
        // e.g. Ribentropa–Molotova, angļu-latviešu
        for (AnalyzedTokenReadings token: tagged_whole_word) {
            for (AnalyzedToken possibleLemma: token.getReadings()) {
                if (places.contains(possibleLemma)) {
                    this.dashCorrected = concatenatedText;
                    return true;
                } else {
                    if (places.contains(token.getToken())) {
                        this.dashCorrected = concatenatedText;
                        return true;
                    }
                }
            }
        }

        if (list.equals(COMPOUND_FILENAME)) {
            // Ja saliktenis, tad var būt arī locījumā
            for (String compound: places) {
                // Compound parts are by default in lemma form
                boolean whole_word_match = true;
                List<String> compound_parts = Arrays.stream(compound.split(defise.toString())).toList();
                for (int j = 0; j<compound_parts.size(); j++) {
                    boolean part_match = false;
                    AnalyzedTokenReadings part = parts.get(j);
                    for (int k = 0; k<part.getReadingsLength(); k++) {
                        String compound_part = compound_parts.get(j);
                        String part_lemma = part.getAnalyzedToken(k).getLemma();
                        if (part_lemma != null && compound_parts.get(j).equalsIgnoreCase(part_lemma)) {
                            part_match = true;
                            break;
                        }
                    }
                    if (!part_match) {
                        whole_word_match = false;
                        break;
                    }
                }
                if (whole_word_match) return true;
            }
        }

        return false;
    }


    public static Boolean checkGazetteer(String text, String path) {
        HashSet<String> data = loadGazetteer(path);
        if (data.contains(text)) {return true;};
        return false;
    }

    public static HashSet<String> loadGazetteer(String path) {
        HashSet<String> l = new HashSet<>();
        List<String> lines = JLanguageTool.getDataBroker().getFromResourceDirAsLines(path);
        for (String line : lines) {
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            l.add(line.trim());
        }
        return l;
    }

    public static String replaceLongDomuzime(String correction) {
        return correction.replace(longDomuzīme, domuzīme);
    }

    public static void replaceLongDomuzime(StringBuilder sb) {
        for (int i = 0; i < sb.length(); i++) {
            if (sb.charAt(i) == '—') {
                sb.setCharAt(i, '–');
            }
        }
    }


    /***
     * Šī funkcija sagriež gabalos un palaiž morfoanalīzi padotam vārdam ar defisi/domuzīmi
     * @param text – kautkāds analizējams vārds ar defisi/domuzīmi, e.g. Kotla-Jerve
     * @return morfoloģiski tagot'otas vārda daļas
     */
    public List<AnalyzedTokenReadings> split(String text) throws IOException {
        List<String> parts = Arrays.stream(text.split("[-–—]"))
                .map(String::strip)
                .toList();
        return this.tagger.tag(parts);
    }



    public static StringBuilder concat(List<AnalyzedTokenReadings> parts) {
        Pattern dash = Pattern.compile(dashes);
        // Ar StringBuilder izveidojam kopējo "maršrutu" vienā String elementā
        StringBuilder concatenatedText = new StringBuilder();
        for (int i = 0; i < parts.size(); i++) {
            // Sakonkatinējam tos tokenus, kas nav svītras ar domuzīmēm
            if (!parts.get(i).getToken().matches(dash.pattern())) {
                concatenatedText.append(parts.get(i).getToken());
                if (i+1 != parts.size()) concatenatedText.append(domuzīme);
            }
        }
        replaceLongDomuzime(concatenatedText);
        return concatenatedText;
    }

    public static StringBuilder concat(List<AnalyzedTokenReadings> parts, Character joiner) {
        Pattern dash = Pattern.compile(dashes);
        // Ar StringBuilder izveidojam kopējo "maršrutu" vienā String elementā
        StringBuilder concatenatedText = new StringBuilder();
        for (int i = 0; i < parts.size(); i++) {
            // Sakonkatinējam tos tokenus, kas nav svītras ar domuzīmēm
            if (!parts.get(i).getToken().matches(dash.pattern())) {
                concatenatedText.append(parts.get(i).getToken());
                if (i+1 != parts.size()) concatenatedText.append(joiner);
            }
        }
        replaceLongDomuzime(concatenatedText);
        return concatenatedText;
    }



    public List<RuleMatch> yearSpanRule(List<RuleMatch> ruleMatches, AnalyzedSentence sentence) {
        String message = getSpacingIssue1();

        String years_format = "\\b(\\d{4})\\s?"+dashes+"\\s?(\\d{4})\\b";
        Pattern yearsPattern = Pattern.compile(years_format);
        Matcher yearsMatcher = yearsPattern.matcher(sentence.getText());

        while (yearsMatcher.find()) {
            int startPos = yearsMatcher.start();
            int endPos = yearsMatcher.end();
            String corrected_text;

            String year_1 = yearsMatcher.group(1);
            String year_2 = yearsMatcher.group(2);
            corrected_text = year_1+domuzīme+year_2;

            System.out.println(corrected_text);

            RuleMatch ruleMatch = new RuleMatch(this, sentence, startPos, endPos, message);
            ruleMatch.setSuggestedReplacement(corrected_text);
            ruleMatches.add(ruleMatch);
        }
        return ruleMatches;
    }
}
