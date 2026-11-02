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


public class DashFormatRule extends Rule {

    private static final String PLACENAME_FILENAME = "/lv/gazetteer.txt";
    private static final String INVENTORS_FILENAME = "/lv/inventors.txt";
    private Tagger tagger;


    public DashFormatRule(ResourceBundle messages, Latvian language) {
      super(messages);
      super.setCategory(LatvianErrorCategories.TEHN);
      setLocQualityIssueType(ITSIssueType.Whitespace);
      this.tagger = language.getTagger();
    }

    @Override
    public String getId() {
      return "DOMUZĪMES_LIETOJUMS_PRETPOLIEM";
    }

    @Override
    public String getDescription() {
      return RuleDescriptions.dashUsageMessage;
    }

    @Override
    public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
        List<RuleMatch> ruleMatches = new ArrayList<>();
        AnalyzedTokenReadings[] tokens = sentence.getTokens();

        Character domuzīme = '–';

        // Šeit tiek pieņemts, ka rakstītājs ir izmantojis tikai defises tekstā (rakstot vienā vārdā bez atstarpēm)
        // Attiecīgi jānosaka vai nevajag lietot domuzīmi un ja jā tad kādā veidā (atdalot ar atstarpēm vai nē).

        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i].getToken();
            // Ignore non-capitalized words or full-caps wods
            // Ignore if doesn't contain defise/domuzīme
//            Pattern double_case = Pattern.compile("\\p{Lu}\\p{Ll}+[-–—]\\p{Lu}\\p{Ll}+");
//            Matcher matcher = double_case.matcher(token);
            if (token.matches("\\p{Lu}\\p{Ll}+[-–—]\\p{Lu}\\p{Ll}+")) {
                // FIXME: Paga, šī metode ar meklēšanu vārdā strādā tikai ar defisēm,
                //      jo domuzīmes tiek atdalītas kā atsevišķi tokeni tagošanas procesā.
                Pattern dash = Pattern.compile("[-–—]");
                Matcher dash_matcher = dash.matcher(token);
                if (dash_matcher.find()) {
                    List<String> parts = Arrays.asList(token.split(dash_matcher.group()));
                    Set<String> places = loadGazetteer(PLACENAME_FILENAME);
                    boolean isRoute = true;

                    // Pārbaudam izņēmumkombinājas, kur ir defise lokāciju nosaukumā
                    // Šis gan var pārbaudīt tikai visu kopējo vārdu, jo te nav Tagera otrreizēja izsaukšana
                    if (tokens[i].hasReading()) {
                        for (AnalyzedToken possibleLemma: tokens[i].getReadings()) {
                            if (places.contains(possibleLemma)) {
                                isRoute = false;
                            } else if (checkInList(tokens[i].getToken(), INVENTORS_FILENAME)) {
                                String msg = RuleDescriptions.dashFormatMessage;
                                RuleMatch match = new RuleMatch(this, sentence, tokens[i].getStartPos(), tokens[i].getEndPos(), msg);
                                match.setSuggestedReplacement(tokens[i].getToken().replace('-', domuzīme));
                                ruleMatches.add(match);
                                isRoute = false;
                            }
                        }
                    } else {
                        if (places.contains(token)) {
                            isRoute = false;
                        } else if (checkInList(tokens[i].getToken(), INVENTORS_FILENAME)) {
                            String msg = RuleDescriptions.dashFormatMessage;
                            RuleMatch match = new RuleMatch(this, sentence, tokens[i].getStartPos(), tokens[i].getEndPos(), msg);
                            match.setSuggestedReplacement(tokens[i].getToken().replace('-', domuzīme));
                            ruleMatches.add(match);
                            isRoute = false;
                        }
                    }
                    if (!isRoute) continue;

                    // Veicam papildus LVTagger izsaukumu individuālajiem tokenā ietvertajiem vārdiem
                    List<AnalyzedTokenReadings> tagged_parts = this.tagger.tag(parts);

                    // Check if all seperated values are placenames
                    // Iterate through each defise seperated part of the token
                    StringBuilder correction = new StringBuilder();
                    for (int j = 0; j < tagged_parts.size(); j++) {
                        boolean existsMatch = false;
                        for (AnalyzedToken reading: tagged_parts.get(j).getReadings()) {
                            if (places.contains(reading.getLemma())) {
                                existsMatch = true;
                                correction.append(reading.getToken());
                                if (j+1 != tagged_parts.size()) correction.append(domuzīme);
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
                                        if (place.substring(0,div).contains(reading.getToken()) && j+1 != tagged_parts.size() && tagged_parts.size() > 2) {
                                            for (AnalyzedToken reading2: tagged_parts.get(j+1).getReadings()) {
                                                String lemma = "";
                                                if (reading2.getLemma() != null) lemma = reading2.getLemma();
                                                else lemma = reading2.getToken();
                                                if (place.substring(div+1).contains(lemma)) {
                                                    existsMatch = true;
                                                    correction.append(reading.getToken()).append("-").append(lemma);
                                                    if (j+2 < tagged_parts.size()) correction.append(domuzīme);
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


                    if (isRoute) {
                        if (!Objects.equals(dash_matcher.group(), domuzīme.toString())) {
//                            String correction = String.join(domuzīme.toString(), parts);
//                            System.out.println(correction);
                            String msg = RuleDescriptions.dashFormatMessage;
                            RuleMatch match = new RuleMatch(this, sentence, tokens[i].getStartPos(), tokens[i].getEndPos(), msg);
                            match.setSuggestedReplacement(correction.toString());
                            ruleMatches.add(match);
                        }
                    } else {
                        // TODO: Add opposite rule
                        //  aka, otherwise use defise
                        //  Vai patiesībā varbūt pat nevajag to darīt šeit,
                        //  jo ar šo klasi var uzķert tikai vienā tokenā esošus kombo.
                    }
                }
            }
            else if (token.matches("\\p{L}\\p{Ll}+(-\\p{Ll}+)+")) {
                String basic_token = token.toLowerCase(Locale.ROOT);
                // Check
                String[] result = basic_token.split("[-–—]");

                char dash = '–';
                String concatenatedText = String.join(" ", result);
                List<String> tot = Arrays.stream(result)
                        .collect(Collectors.toCollection(ArrayList::new));
                List<AnalyzedTokenReadings> tagged_whole_word = this.tagger.tag(tot);

                // Option 1: visi vārdi ģenetīvā (un dsk.?) -> tas tā kā atlasītu visas tautības/valodas
                // Option 2: mēneši vai nedēļas dienas
                // TODO: Option 3: nedēļām–mēnešiem (vnk vienādi locījumi saistītām nozīmēm?)
                // Nezinu varbūt ir par plašu/varbūt nav
                boolean genetive_construct = true;
                boolean month_construct = true;
                boolean weekday_construct = true;
                for (AnalyzedTokenReadings readings: tagged_whole_word) {
                    if (!readings.getToken().equals(Character.toString(dash))) {
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
                }
                if (genetive_construct || month_construct || weekday_construct) {
                    String msg = RuleDescriptions.dashFormatMessage;
                    RuleMatch match = new RuleMatch(this, sentence, tokens[i].getStartPos(), tokens[i].getEndPos(), msg);
                    match.setSuggestedReplacement(tokens[i].getToken().replace('-', domuzīme));
//                    System.out.println(tokens[i].getToken().replace('-', domuzīme));
                    ruleMatches.add(match);
                }
            }
        }
        return toRuleMatchArray(ruleMatches);
    }


    // TODO: Varbūt būtu labāk pārlikt maršruta pārbaudīšanu atsevišķā funkcijā
    public Boolean checkRoute(List<AnalyzedTokenReadings> route_parts) throws IOException {
        Set<String> places = loadGazetteer(PLACENAME_FILENAME);
        Character domuzīme = '–';

        boolean isRoute = true;

        // Ar StringBuilder izveidojam kopējo "maršrutu" vienā String elementā
        StringBuilder concatenatedText = new StringBuilder();
        for (int i = 0; i < route_parts.size(); i++) {
            concatenatedText.append(route_parts.get(i).getToken());
            if (i+1 != route_parts.size()) concatenatedText.append("–");
        }
        System.out.println(route_parts);
        // Tokenizējam, lai noteiktu vai ir locījumā
        List<String> tot = Collections.singletonList(concatenatedText.toString());
        List<AnalyzedTokenReadings> tagged_whole_word = this.tagger.tag(tot);
        System.out.println();

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
        if (!isRoute) return false;


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
        return isRoute;
    }



    public static Boolean checkInList(String token, String gazetteer) {
        Set<String> li = loadGazetteer(gazetteer);
        String sub = token.replace('-', '–');
        if (li.contains(sub)) {
            return true;
        }
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
}
