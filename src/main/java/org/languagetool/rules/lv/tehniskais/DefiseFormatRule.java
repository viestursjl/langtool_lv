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
        AnalyzedTokenReadings[] tokens = sentence.getTokens();

        ruleMatches = yearSpanRule(ruleMatches, sentence);
        Pattern dash = Pattern.compile("[-–—]");
        for (int i = 0; i < tokens.length; i++) {
//            System.out.println("{"+tokens[i].getToken()+"}");
            this.dashCorrected = new StringBuilder();
            String token = tokens[i].getToken();
//            System.out.println("{"+tokens[i]+"}");
            Matcher dash_matcher = dash.matcher(token);
            // Vai te atstarpes skaitās kā atsevišķs tokens? Diemžēl izskatās, ka jā!
            //  Ja noņemtu atstarpes tas neļautu izķert kļūdas ar saistītās atstarpes lietojumu
            if (token.matches(dash.pattern())) {
                // Ja svītra ievada teikumu varam pieņemt, ka tā ir dialogu ievadoša domuzīme
                // Teikuma tokenu sarakstam tiek pievienots tukšs [/SENT_START] tokens
                if (i+1 == tokens.length) {
                    // Vai vispār var beigties teikums ar svītru???
                    continue;
                } else {
                    // Pārbaudām, vai tā ir domuzīme
                    // Sākam ar gadījumu, kad simbols ir defise
                    // Ņemam vērā, ka šajā gadījumā defise ir tokenizēta atsevišķi no pārējiem vārdiem
                    // Tas notiek gadījumos, ja tai kādā pusē ir atstarpe vai cits punktuācijas simbols
                    if (token.equals(defise.toString())) {
                        String a = tokens[i].getWhitespaceBefore();
                        List<AnalyzedToken> previous = tokens[i-1].getReadings();
                        List<String> previousTags = previous.stream().map(AnalyzedToken::getPOSTag).collect(Collectors.toList());
//                        System.out.println(previousTags);
                        if (previousTags.contains("SENT_START")) {
                            // TODO: Pielikt klāt labojumu, ka tiešo runu, dialogu ievada ar domuzīmi
                            String correction = domuzīme.toString();
                            // Ja nav atstarpes pēc tam tad pieliekam arī to klāt
                            if (!tokens[i+1].isWhitespaceBefore()) correction += " ";
                            String msg = RuleDescriptions.quoteDashMessage;
                            RuleMatch match = new RuleMatch(this, sentence, tokens[i].getStartPos(), tokens[i].getEndPos(), msg);
                            match.setSuggestedReplacement(correction);
                            ruleMatches.add(match);
                        }
                        // TODO: Šo droši vien vajadzēs papildināt ar likumiem, kas pārbauda vai gadījumā nav izlaista atstarpe
                        //  aka, vai iepriekšējais vārds beidzas ar defisi "audio-"; nākošais vārds sākas ar defisi "-šana", u.c. instances
                        else if (tokens[i-1] != null && tokens[i+1] != null) {
                            if (tokens[i-1].getToken().matches("\\s") && tokens[i+1].getToken().matches("\\s|,")) {
                                String msg = RuleDescriptions.dashMessage;
                                if (!tokens[i-1].getToken().equals(nonBreakingSpace.toString())) {
                                    int h = i;
                                    while (tokens[h-1].getToken().matches("\\s")) h--;
                                    msg += " "+RuleDescriptions.NBSPDashMessage;
                                    String correction = nonBreakingSpace.toString()+ domuzīme;
                                    RuleMatch match = new RuleMatch(this, sentence, tokens[h].getStartPos(), tokens[i].getEndPos(), msg);
                                    match.setSuggestedReplacement(correction);
                                    ruleMatches.add(match);
                                }
                                else {
                                    String correction = domuzīme.toString();
                                    RuleMatch match = new RuleMatch(this, sentence, tokens[i].getStartPos(), tokens[i].getEndPos(), msg);
                                    match.setSuggestedReplacement(correction);
                                    ruleMatches.add(match);
                                }
                            }
                        }
                    }
                    // Ja tokens nav defise, tad tas ir domuzīme
                    else {
                        // TODO: Atlasām iepriekšējo un nākošo tokenu, ieliekam sarakstā un palaižam checkRoute funkciju
                        int sublistStart = i-1;
                        int sublistEnd = i+2;
                        if (tokens[i-1].getToken().trim().isEmpty() && i-2>=0) sublistStart = i-2;
                        if (tokens[i+1].getToken().trim().isEmpty() && i+2<=tokens.length) sublistEnd = i+3;
                        // Man šķiet, ka jau sāku rakstīt pilnīgu sviestu (。・ω・。)
                        // Atlasu arī iepriekšējos elementus un izņemu whitespace tokenus no list
                        List<AnalyzedTokenReadings> full = Arrays.asList(tokens).subList(sublistStart, sublistEnd);
                        String dfks = full.stream().map(tok -> tok.getToken()).collect(Collectors.toList()).stream()
                                .reduce("", (str1, str2) -> str1 + str2);
                        List<AnalyzedTokenReadings> a = full.stream()
                                .filter(element -> !element.getToken().trim().isEmpty())
                                .collect(Collectors.toList());
                        if (a.size() != 3) continue;
                        // Nav vērts turpināt pārbaudi, ja neatrod divus vārdus...
                        Pattern īpašvārds = Pattern.compile("\\p{Lu}\\p{Ll}+");
                        // Basically pārbaudām, ka abi savienotie vārdi sākas ar lielo burtu, un nav vienburtīgi
                        if (a.get(0).getToken().matches(īpašvārds.pattern()) && a.get(2).getToken().matches(īpašvārds.pattern())) {
                            // Checkroute funkciju ir vērts palaist tikai iekš šī if'a
                            if (checkGazetteer(a, PLACENAME_FILENAME)) {
                                // Piemērs: Rīga–Viļņa
                                if (dashCorrected.length() != 0 && !dashCorrected.toString().equals(dfks)) {
                                    String msg = RuleDescriptions.doublePlacenameMessage;
                                    RuleMatch match = new RuleMatch(this, sentence, tokens[sublistStart].getStartPos(), tokens[sublistEnd-1].getEndPos(), msg);
                                    System.out.println(dashCorrected);
                                    match.setSuggestedReplacement(dashCorrected.toString());
                                    ruleMatches.add(match);
                                }
                            } else {
                                // Piemērs: Vīķe–Freiberga
                                // TODO: Pārbaudām vai kombinācija ir INVENTORS sarakstā
//                                System.out.println("___"+checkInList(a, INVENTORS_FILENAME).toString());
                                if (checkInList(a, INVENTORS_FILENAME)) {
                                    // Piemērs: Molotova–Ribentropa
                                    if (a.get(1).isWhitespaceBefore() && a.get(2).isWhitespaceBefore()) {
                                        // FIXME: Magic numbers i+3, i+2
                                        String msg = getSpacingIssue1();
                                        StringBuilder correction = concat(Arrays.stream(tokens, i-1, i+3).collect(Collectors.toList()));
                                        RuleMatch match = new RuleMatch(this, sentence, tokens[i-1].getStartPos(), tokens[i+1].getEndPos(), msg);
                                        match.setSuggestedReplacement(correction.toString());
                                        ruleMatches.add(match);
                                    } else if (a.get(1).isWhitespaceBefore() || a.get(2).isWhitespaceBefore()) {
                                        String msg = getSpacingIssue1();
                                        StringBuilder correction = concat(Arrays.stream(tokens, i-1, i+2).collect(Collectors.toList()));
                                        RuleMatch match = new RuleMatch(this, sentence, tokens[i-1].getStartPos(), tokens[i+1].getEndPos(), msg);
                                        match.setSuggestedReplacement(correction.toString());
                                        ruleMatches.add(match);
                                    }
                                } else {
                                    // Pieņemam, ka ja ir rakstīts bez atstarpēm, tad dubultuzvārds -> Vīķe-Freiberga
                                    // Ja ar atstarpēm tad iespējams divi atšķirīgi vārdi -> Jānis – Pēterim
                                    // FIXME: Te vēl var pielikt klāt locījuma pārbaudi, ja sakrīt dzimta, locījums, tad dubultuzvārds, ja nē tad divi atšķirīgi vārdi
                                    if (!a.get(1).isWhitespaceBefore() && !a.get(2).isWhitespaceBefore()) {
                                        if (true) {
                                            // FIXME: Ja locījums sakrīt
                                            String msg = RuleDescriptions.doubleSurnameMessage;
                                            StringBuilder correction = concat(Arrays.stream(tokens, i-1, i+2).collect(Collectors.toList()), defise);
                                            RuleMatch match = new RuleMatch(this, sentence, tokens[i-1].getStartPos(), tokens[i+1].getEndPos(), msg);
                                            match.setSuggestedReplacement(correction.toString());
                                            ruleMatches.add(match);
                                        }
                                    }
                                }
                            }
                        } else {
                            // Šeit ir basic gadījumi, kas arī nemaz nav tik viegli filtrējami
                            // FIXME: Pārbaudām vai tokeni ir savienoti, t.i. bez atstarpēm pa vidu
                            if (true) {
//                            if (!a.get(1).isWhitespaceBefore() && !a.get(2).isWhitespaceBefore()) {
                                if (checkInList(a, COMPOUND_FILENAME)) {
                                    // Te ir jābūt tādiem salikteņiem kā "diode-triode", "e-pasts",
                                    // kuros nepareizi izmantota domuzīme
                                    String msg = getDescription();
                                    List<AnalyzedTokenReadings> apple = Arrays.stream(tokens, sublistStart, sublistEnd).collect(Collectors.toList());
                                    String correction = apple.get(0).getToken()+defise+apple.get(apple.size()-1).getToken();
                                    RuleMatch match = new RuleMatch(this, sentence, tokens[sublistStart].getStartPos(), tokens[sublistEnd-1].getEndPos(), msg);
                                    System.out.println(correction);
                                    match.setSuggestedReplacement(correction.toString());
                                    ruleMatches.add(match);
                                } else if (true) {

                                }
                            } else {

                            }
                        }


//                        if (!checkGazetteer(a, PLACENAME_FILENAME)) {
//                            // Ja netiek atrasts maršruts
//                            System.out.println("Amogus "+a.toString());
//                        }
                    }

                    // Citādi mēs varam aplūkot iepriekšējo un nākamo tokenu.
                    // Ja Pattern.compile("\\p{Lu}\\p{Ll}+[-–—]\\p{Lu}\\p{Ll}+");
                    //      Ja maršruts, tad pārbaudām, ka nav atstarpju
                    //      Ja vietvārds ar defisi, labojam uz defisi
                    //      Citādi laikam dubultuzvārds un ir jālabo uz defisi
                    // Citādi pārbaudām vai abās pusēs ir divi atpazīti vārdi
                    //      Ja tie veido saistītu terminu, tad labojam uz defisi
                    // Citādi pārbaudām pārējos defises lietojuma tipus
                }
            }
            // TODO: Nepieciešams atsevišķi apstrādāt situācijas, kad defise ir daļa no cita tokena.
            //  it kā kkas šāds ir apskatīts DashFormatRule bet tikai maršrutiem
            //  Šis arī attiecas uz dubultuzvārdiem un visiem citiem labojumiem
            // Pašreizējā metode vnk neatrod nekādus labojumus, ja defise ir daļa no vārda
            else if (token.matches("e-")) {
                System.out.println(tokens[i].getToken()+tokens[i+1].getToken()+tokens[i+2].getToken());
                if (tokens.length > i+2 && tokens[i+1].getToken().matches("\\s") && tokens[i+2].getToken().matches("\\p{Ll}+")) {
                    String msg = getDescription();
                    String correction = token.substring(1);
                    RuleMatch match = new RuleMatch(this, sentence, tokens[i].getStartPos()+1, tokens[i].getEndPos()+1, msg);
                    System.out.println(sentence.getText().substring(tokens[i].getStartPos()+1, tokens[i].getEndPos()+1));
                    System.out.println(correction);
                    match.setSuggestedReplacement(correction);
                    ruleMatches.add(match);
                }
            }
        }

        // TODO: Visos gadījumos, kuros nav kļūdaini lietota pārgarā domuzīme, nepieciešams to pārlabot uz parasto domuzīmi
        //  it kā vajadzētu būt, ka visos iepriekše'jos gadījumos kur ir kaut kāds kļūdains lietojums, tiek aizstāts ar parasto domuzīmi
        String text = sentence.getText();
        int index = text.indexOf(longDomuzīme);
        while (index != -1) {
            // TBD: do something with index
            System.out.println("Found '—' at position: " + index);
            String msg = getDescription();
            String correction = domuzīme.toString();
            RuleMatch match = new RuleMatch(this, sentence, index, index+1, msg);
            System.out.println(correction);
            match.setSuggestedReplacement(correction.toString());
            ruleMatches.add(match);

            index = text.indexOf(domuzīme, index + 1);
        }

        return toRuleMatchArray(ruleMatches);
    }



    public RuleMatch[] match2(AnalyzedSentence sentence) throws IOException {
        List<RuleMatch> ruleMatches = new ArrayList<>();
        String text = sentence.getText();
        AnalyzedTokenReadings[] tokens = sentence.getTokens();

        // Vispirms apskatām specifiskākos gadījumus
        ruleMatches = yearSpanRule(ruleMatches, sentence);

        // Apskatām situāciju, kur svītras simbols ir teikuma sākumā
        // Tas nav saistīts ar pārējiem apskatāmajiem gadījumiem, kas ir starp vārdiem
        // TODO: Pārliecināties vai šis matcher strādā pareizi
        Pattern pattern = Pattern.compile("^\\s*[-–—]", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            System.out.println("Match: " + matcher.group());
            int start = matcher.start();
            int end = matcher.end();

            String msg = RuleDescriptions.quoteDashMessage;
            String correction = domuzīme.toString();
            RuleMatch match = new RuleMatch(this, sentence, start, end+1, msg);
            match.setSuggestedReplacement(correction);
            ruleMatches.add(match);
        }


        // Vispirms apskatām situāciju ar 2 vārdiem ar Lielajiem sākuma burtiem
        // e.g. Ribetropa–Molotova, Vaira Vīķe-Freiberga, Kohtla-Jerve
        pattern = Pattern.compile("\\p{Lu}\\p{Ll}+\\s*([-–—])\\s*\\p{Lu}\\p{Ll}+(\\s*[-–—]\\s*\\p{Lu}\\p{Ll}+)*");
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
                    .filter(element -> !element.getToken().matches("[\\s-–—]*"))
                    .toList();
            List<String> filteredText = filteredTokens.stream().map(AnalyzedTokenReadings::getToken).toList();
            List<AnalyzedTokenReadings> taggedFilteredTokens = this.tagger.tag(filteredText);

            // TODO: Pārliecionāties ka šīs funkcijas strādā gan priekš tokenizēta, gan netokinizēta ievada
            //  ACTUALLY: Ja jau strādātu pie netokinizēta vai vispār ir jēga sadalīt jau šeit???
            // Efektīvi pašlaik loģika šim gabalam ir sadalīta pa DefiseFormatRule un DashFormatRule
            //  apvienot varētu būt sarežģīti :<
            // Vai arī tā kā DashFormatRule faila defises loģika strādā uz vnk String tad es varu ņemt to?
            int start = matcher.start();
            int end = matcher.end();

            String hyphenConcat = concat(taggedFilteredTokens, '-').toString();
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
                if (!potentialRoute.equals(matcher.group())) {
                    String msg = RuleDescriptions.dashFormatMessage;
                    String correction = potentialRoute;
                    RuleMatch match = new RuleMatch(this, sentence, start, end, msg);
                    match.setSuggestedReplacement(correction);
                    ruleMatches.add(match);
                }
                else if (checkInList(matchingTokens, INVENTORS_FILENAME)) {
                    // Divu dažādu izgudrotāju uzvārdus atdala ar domuzīmi, e.g. Bozes–Einšteina
                    String correction = concat(taggedFilteredTokens).toString();
                    if (!correction.equals(matcher.group())) {
                        String msg = RuleDescriptions.dashFormatMessage;
                        RuleMatch match = new RuleMatch(this, sentence, start, end, msg);
                        match.setSuggestedReplacement(correction);
                        ruleMatches.add(match);
                    }
                }
                else {
                    // Ja nav neviens no specgadījumiem, tad ir dažas alternatīvas
                    // Dubultuzvārdi, vai parasta domuzīme
                    // e.g. Vīķe-Freiberga, Jānis – Pēterim

                    if (matcher.group().matches("\\p{Lu}\\p{Ll}+\\s+([-–—])\\s+\\p{Lu}\\p{Ll}+")) {
                        // If whitespaces from both sides, assume dash
                        String correction = matcher.group().replace("-", "–");
                        if (!correction.equals(matcher.group())) {
                            String msg = RuleDescriptions.dashFormatMessage;
                            RuleMatch match = new RuleMatch(this, sentence, start, end, msg);
                            match.setSuggestedReplacement(correction);
                            ruleMatches.add(match);
                        }
                    }
                    else {
                        // If no whitespaces, assume hyphen
                        String correction = concat(taggedFilteredTokens, '-').toString();
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



        // Citādi apskatām gadījumus, kad vismaz viens no vārdiem ir ar mazo burtu?


        // Aplūkojam pārējos gadījumus, abi ir ar mazajiem
        // FIXME:
        pattern = Pattern.compile("\\b\\p{Ll}+\\s*[-–—]\\s*\\p{Ll}+\\b");
        matcher = pattern.matcher(text);

        while (matcher.find()) {
            int matchStart = matcher.start();
            int matchEnd = matcher.end();
            List<AnalyzedTokenReadings> matchingTokens = new ArrayList<>();

            if (checkInList(matchingTokens, COMPOUND_FILENAME)) {
                // FIXME: This never matches take this to the section where small letter words are looked at
                String correction = concat(taggedFilteredTokens, '-').toString();
                if (!correction.equals(matcher.group())) {
                    String msg = RuleDescriptions.hyphenMessage;
                    RuleMatch match = new RuleMatch(this, sentence, start, end, msg);
                    match.setSuggestedReplacement(correction);
                    ruleMatches.add(match);
                }
            }
        }

        // FIXME: Pārvietot šo funckiju kā sekundāru izsaukumu gadījumos, kad citi likumi satur pārgaro domuzīmi
        //  Ja šis ir vienīgais labojums, message tekstam jābūt "", vai arī jāspēj izsaut bez String message
        // Aizvietojam visas pārgarās domuzīmes ar parastajām domuzīmēm
        int index = text.indexOf(longDomuzīme);
        while (index != -1) {
            String msg = getDescription();
            String correction = domuzīme.toString();
            RuleMatch match = new RuleMatch(this, sentence, index, index+1, msg);
            match.setSuggestedReplacement(correction.toString());
            ruleMatches.add(match);

            index = text.indexOf(domuzīme, index + 1);
        }

        return toRuleMatchArray(ruleMatches);
    }







    // TODO: Varbūt būtu labāk pārlikt maršruta pārbaudīšanu atsevišķā funkcijā
    public String returnRoute(List<AnalyzedTokenReadings> route_parts) throws IOException {
        Set<String> places = loadGazetteer(PLACENAME_FILENAME);
        Character domuzīme = '–';

        boolean isRoute = true;

        // Ar StringBuilder izveidojam kopējo "maršrutu" vienā String elementā
        StringBuilder concatenatedText = new StringBuilder();
        for (int i = 0; i < route_parts.size(); i++) {
            concatenatedText.append(route_parts.get(i).getToken());
            if (i+1 != route_parts.size()) concatenatedText.append("–");
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
            boolean existsMatch = false;
            for (AnalyzedToken reading: route_parts.get(j).getReadings()) {
                if (places.contains(reading.getLemma())) {
                    existsMatch = true;
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
                        int div = place.indexOf('-');
                        if (place.indexOf('-') != -1) {
                            if (place.substring(0,div).contains(reading.getToken()) && j+1 != route_parts.size() && route_parts.size() > 2) {
                                for (AnalyzedToken reading2: route_parts.get(j+1).getReadings()) {
                                    String lemma = "";
                                    if (reading2.getLemma() != null) lemma = reading2.getLemma();
                                    else lemma = reading2.getToken();
                                    if (place.substring(div+1).contains(lemma)) {
                                        existsMatch = true;
                                        correction.append(reading.getToken()).append("-").append(lemma);
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
            if (!existsMatch) isRoute = false;
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
        Pattern dash = Pattern.compile("[-–—]");
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
        System.out.println(concatenatedText+"+");

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
//            for (String a: places) {
//                if (a.contains(String.valueOf(defise))) {
//                    String[] terms = a.split(defise.toString());
//                    Boolean good = true;
//                    for (int i = 0; i < terms.length; i++) {
//                        if (tagged_whole_word.get(i).getReadings().size() > 0) {
//                            for (int j = 0; j < terms.length; j++)
//                        }
//                        if (tagged_whole_word.get(i).getReadings().get(0).equals(terms[0])) {}
//                    }
//                }
//            }
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
        return correction.replace("—", "–");
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
        Pattern dash = Pattern.compile("[-–—]");
        // Ar StringBuilder izveidojam kopējo "maršrutu" vienā String elementā
        StringBuilder concatenatedText = new StringBuilder();
        for (int i = 0; i < parts.size(); i++) {
            // Sakonkatinējam tos tokenus, kas nav svītras ar domuzīmēm
            if (!parts.get(i).getToken().matches(dash.pattern())) {
                concatenatedText.append(parts.get(i).getToken());
                if (i+1 != parts.size()) concatenatedText.append("–");
            }
        }
        replaceLongDomuzime(concatenatedText);
        return concatenatedText;
    }

    public static StringBuilder concat(List<AnalyzedTokenReadings> parts, Character joiner) {
        Pattern dash = Pattern.compile("[-–—]");
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

        String years_format = "\\b(\\d{4})\\s?[-–—]\\s?(\\d{4})\\b";
        Pattern yearsPattern = Pattern.compile(years_format);
        Matcher yearsMatcher = yearsPattern.matcher(sentence.getText());

        while (yearsMatcher.find()) {
            int startPos = yearsMatcher.start();
            int endPos = yearsMatcher.end();
            String corrected_text;

            String year_1 = yearsMatcher.group(1);
            String year_2 = yearsMatcher.group(2);
            corrected_text = year_1+defise+year_2;

            System.out.println(corrected_text);

            RuleMatch ruleMatch = new RuleMatch(this, sentence, startPos, endPos, message);
            ruleMatch.setSuggestedReplacement(corrected_text);
            ruleMatches.add(ruleMatch);
        }
        return ruleMatches;
    }
}
