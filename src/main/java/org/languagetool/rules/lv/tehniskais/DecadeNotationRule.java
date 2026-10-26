package org.languagetool.rules.lv.tehniskais;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.language.Latvian;
import org.languagetool.rules.ITSIssueType;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.lv.LatvianErrorCategories;
import org.languagetool.rules.lv.RuleDescriptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DecadeNotationRule extends Rule {
    public DecadeNotationRule(ResourceBundle messages, Latvian language) {
        super(messages);
        super.setCategory(LatvianErrorCategories.TEHN);
        setLocQualityIssueType(ITSIssueType.Whitespace);
    }

    @Override
    public String getId() {
        return "DESMITGAŽU PIERAKSTS";
    }

    @Override
    public String getDescription() {
        return RuleDescriptions.decadeFormatMessage;
    }

    @Override
    public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
        List<RuleMatch> ruleMatches = new ArrayList<>();
        AnalyzedTokenReadings[] tokens = sentence.getTokens();
        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i].getToken();
//            System.out.println(token);
            if (!token.isEmpty()) {
                if (Character.isDigit(token.charAt(0))) {
                    if (token.matches("(1[0-9]|20)?[0-9]0-?t(ie|o|ajiem|os|ajos)")) {
                        Pattern p = Pattern.compile("(^\\d+)");
                        Matcher matcher = p.matcher(token);
                        if (matcher.find())
                        {
                            String number_part = matcher.group(1);
                            String correction = getCentury(number_part);

                            int expr_len = 0;

                            // Laikam neskaitās labi izlaist vārdu "gadi" ja par tiem runā
                            // "90-tie" -> "90. gadi"
                            int g = 2;
                            if (i+g < tokens.length) {
                                String next_token = tokens[i+g].getToken();
                                if (next_token.startsWith("gad")) {
                                    expr_len += g;
                                    correction += next_token;
                                } else {
                                    Pattern p2 = Pattern.compile("(\\p{Ll}+)");
                                    Matcher matcher2 = p2.matcher(token);
                                    String letter_part = "";
                                    if (matcher2.find()) {
                                        letter_part = matcher2.group(1);
                                    }
                                    correction += "gad"+getYearEnding(letter_part);
                                }
                            }

                            String msg = getDescription();
                            RuleMatch match = new RuleMatch(this, sentence, tokens[i].getStartPos(), tokens[i+expr_len].getEndPos(), msg);
                            StringBuilder builder = new StringBuilder();
                            for (int j = i; j <= i+expr_len; j++) {
                                builder.append(tokens[j].getToken());
                            }
                            System.out.println(builder);
                            System.out.println(correction);
                            match.setSuggestedReplacement(correction);
                            ruleMatches.add(match);
                        }
                    }
                    // Punkts pa vidu sadala tokenus: "90.-tie" -> "90", ".", "-tie"
                    else if (token.matches("(1[0-9]|20)?[0-9]0")) {
                        if (i+2 < tokens.length) {
                            if (tokens[i+1].getToken().equals(".") && tokens[i+2].getToken().matches("-?t(ie|o|ajiem|os|ajos)")) {
                                int expr_len = 2;
                                String correction = getCentury(tokens[i].getToken());

                                // "90", ".", "-tie", " ", "gadi"
                                if (i+4 < tokens.length) {
                                    StringBuilder builder = new StringBuilder();
                                    for (int j = i; j <= i+4; j++) {
                                        builder.append(tokens[j].getToken()+"|");
                                    }
                                    System.out.println(builder);
                                    String next_token = tokens[i+4].getToken();
                                    if (next_token.startsWith("gad")) {
                                        expr_len = 4;
                                        correction += next_token;
                                    } else {
                                        Pattern p2 = Pattern.compile("(\\p{Ll}+)");
                                        Matcher matcher2 = p2.matcher(token);
                                        String letter_part = "";
                                        if (matcher2.find()) {
                                            letter_part = matcher2.group(1);
                                        }
                                        correction += "gad"+getYearEnding(letter_part);
                                    }
                                }

                                String msg = getDescription();
                                RuleMatch match = new RuleMatch(this, sentence, tokens[i].getStartPos(), tokens[i+expr_len].getEndPos(), msg);
                                StringBuilder builder = new StringBuilder();
                                for (int j = i; j <= i+expr_len; j++) {
                                    builder.append(tokens[j].getToken());
                                }
                                System.out.println(builder);
                                System.out.println(correction);
                                match.setSuggestedReplacement(correction);
                                ruleMatches.add(match);
                            }
                        }
                    }
                }
            }
        }

        return toRuleMatchArray(ruleMatches);
    }


    public String getCentury(String number_part) {
        // Pielietojam sekojošo formātu: XX. gs. XX. gadi
        // Tādēļ ka saīsinājumam gs. nav nepieciešams noteikt locījumu.
        String correction = "";
        if (number_part.length() == 4) {
            Integer century = Integer.parseInt(number_part.substring(0,2))+1;
            correction += String.valueOf(century)+". gs. ";
            number_part = number_part.substring(2,4);
        }
        if (!number_part.startsWith("0")) {
            // FIXME: Nevar būt 00. gadu
            correction += number_part.substring(0,2)+". ";
        } else {
            // TODO: Nezinu ko maz šajā situācijā piedāvāt
        }
        return correction;
    }

    public String getYearEnding(String end) {
        switch (end) {
            case "tie": return "i";
            case "to": return "u";
            case "tajiem": return "iem";
            case "tos": return "us";
            case "tajos": return "os";
        }
        // Gadījumā, ja kkāda cita dīvaina galotne piekabināta.
        return "i";
    }
}
