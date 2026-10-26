package org.languagetool.rules.lv.tehniskais;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;
import org.languagetool.rules.*;
import org.languagetool.rules.lv.LatvianErrorCategories;
import org.languagetool.rules.lv.RuleDescriptions;
import org.languagetool.tools.StringTools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConnectingWhitespaceRule extends Rule {


  public Character nonBreakingSpace = ' ';

  /** @since 5.9 */
  public ConnectingWhitespaceRule(ResourceBundle messages, Language language) {
    super(messages);
    super.setCategory(LatvianErrorCategories.TEHN);
    setLocQualityIssueType(ITSIssueType.Whitespace);
  }


  @Override
  public String getId() {
    return "SAISTĪTĀS_ATSTARPES_LIETOJUMS";
  }

  @Override
  public String getDescription() {
    return RuleDescriptions.NBSPMessage;
  }

  public String getCurrencyDescription() {
    return RuleDescriptions.currencyMessage;
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
    List<RuleMatch> ruleMatches = new ArrayList<>();


    // First let's fix currency usage and number within it
    ruleMatches = currencyRule(ruleMatches, sentence);
    ruleMatches = measurementRule(ruleMatches, sentence);

    // After that let's fix NBSP usage before dashes
    ruleMatches = dashRule(ruleMatches, sentence);

    // NBSP usage inbetween numbers
    ruleMatches = arbitraryRule(ruleMatches, sentence, "(?<![./])\\b[1-9]\\d{4,}\\b(?!/)", RuleDescriptions.NBSPNumbersMessage);
    ruleMatches = arbitraryRule(ruleMatches, sentence, "\\b(\\+\\d{2,3}\\s)?\\d{1,3}(\\s\\d{3})+\\b", RuleDescriptions.NBSPNumbersMessage);

    ruleMatches = arbitraryRule(ruleMatches, sentence, "\\b[1-9]\\d*\\.?\\s?(g\\.|\\p{Ll}{2,})(?=\\s|$|\\p{Punct})", RuleDescriptions.NBSPUnitsMessage);

    // NBSP usgae before initials
    ruleMatches = arbitraryRule(ruleMatches, sentence, "\\b(\\p{Lu}\\.?\\s?)+(\\p{Lu}\\p{Ll}+)\\b", RuleDescriptions.NBSPInitialMessage);

    // NBSP usage after/inbetween abbreviations
    ruleMatches = arbitraryRule(ruleMatches, sentence, "\\b(Nr|pp)\\.\\s?", RuleDescriptions.NBSPAbbrevMessage);
    ruleMatches = arbitraryRule(ruleMatches, sentence, "\\bt\\.\\s?(i|sk)\\.", RuleDescriptions.NBSPAbbrevMessage);
    ruleMatches = arbitraryRule(ruleMatches, sentence, "\\b(Ph\\.\\s?D\\.)|(Mg|Dr|Ph\\.\\s?D)\\.\\s?(habil\\.\\s?)?(philol|sc\\.\\s?comp|phys)\\.", RuleDescriptions.NBSPAbbrevMessage);
    ruleMatches = arbitraryRule(ruleMatches, sentence, "\\bu\\.\\s?(c|tml|t\\.\\s?jpr)\\.", RuleDescriptions.NBSPAbbrevMessage);
    ruleMatches = arbitraryRule(ruleMatches, sentence, "\\b(Sv|Dr)\\.\\s?\\p{Lu}\\p{Ll}+(\\b|\\s|$)", RuleDescriptions.NBSPAbbrevMessage);
    ruleMatches = arbitraryRule(ruleMatches, sentence, "\\ba\\.\\s?(k|god)\\.", RuleDescriptions.NBSPAbbrevMessage);
    ruleMatches = arbitraryRule(ruleMatches, sentence, "\\bd\\.\\s?k\\.", RuleDescriptions.NBSPAbbrevMessage);
    ruleMatches = arbitraryRule(ruleMatches, sentence, "\\bdz\\.\\s?v\\.", RuleDescriptions.NBSPAbbrevMessage);
    ruleMatches = arbitraryRule(ruleMatches, sentence, "\\bg\\.\\s?t\\.", RuleDescriptions.NBSPAbbrevMessage);
    ruleMatches = arbitraryRule(ruleMatches, sentence, "\\bīst\\.\\s?v\\.", RuleDescriptions.NBSPAbbrevMessage);
    ruleMatches = arbitraryRule(ruleMatches, sentence, "\\bļ\\.\\s?cien\\.", RuleDescriptions.NBSPAbbrevMessage);
    ruleMatches = arbitraryRule(ruleMatches, sentence, "\\bļ\\.\\s?cien\\.", RuleDescriptions.NBSPAbbrevMessage);
    ruleMatches = arbitraryRule(ruleMatches, sentence, "\\bpers\\.\\s?k\\.", RuleDescriptions.NBSPAbbrevMessage);
    ruleMatches = arbitraryRule(ruleMatches, sentence, "\\bp\\.\\s?[kni]\\.", RuleDescriptions.NBSPAbbrevMessage);
    ruleMatches = arbitraryRule(ruleMatches, sentence, "\\b(p\\.\\s?)?m\\.\\s?ē\\.", RuleDescriptions.NBSPAbbrevMessage);
    ruleMatches = arbitraryRule(ruleMatches, sentence, "\\bP\\.\\s?S\\.", RuleDescriptions.NBSPAbbrevMessage);
    ruleMatches = arbitraryRule(ruleMatches, sentence, "\\bt\\.\\s?[ps]\\.", RuleDescriptions.NBSPAbbrevMessage);
    ruleMatches = arbitraryRule(ruleMatches, sentence, "\\bu\\.\\s?g\\.", RuleDescriptions.NBSPAbbrevMessage);
    ruleMatches = arbitraryRule(ruleMatches, sentence, "\\bu\\.\\s?t\\.\\s?jpr\\.", RuleDescriptions.NBSPAbbrevMessage);
    ruleMatches = arbitraryRule(ruleMatches, sentence, "\\bv\\.\\s?tml\\.", RuleDescriptions.NBSPAbbrevMessage);
    ruleMatches = arbitraryRule(ruleMatches, sentence, "\\bz\\.\\s?v\\.", RuleDescriptions.NBSPAbbrevMessage);
    ruleMatches = arbitraryRule(ruleMatches, sentence, "\\bet\\.?\\s?(al)\\.?", RuleDescriptions.NBSPAbbrevMessage);

    return toRuleMatchArray(ruleMatches);
  }



  public List<RuleMatch> dashRule(List<RuleMatch> ruleMatches, AnalyzedSentence sentence) {
    String dashMatch = "(\\b\\s*|\\s+)([–—])(?=\\s|$)";
    Pattern dashPattern = Pattern.compile(dashMatch);
    Matcher dashMatcher = dashPattern.matcher(sentence.getText());

    while (dashMatcher.find()) {
      String message = RuleDescriptions.NBSPDashMessage;
      String match_text = dashMatcher.group();
      int startPos = dashMatcher.start();
      int endPos = dashMatcher.end();

      String corrected_text = correctWhitespace(match_text);

      corrected_text = DefiseFormatRule.replaceLongDomuzime(corrected_text);
      if (!match_text.matches("^\\s\\S+")) {
        // Dažādus simbolus atdalot jāizmanto atstarpe
        corrected_text = nonBreakingSpace + corrected_text;
      } else {
        corrected_text = corrected_text.replaceAll("\\s+", String.valueOf(nonBreakingSpace));
      }

      RuleMatch ruleMatch = new RuleMatch(this, sentence, startPos, endPos, message);
      ruleMatch.setSuggestedReplacement(corrected_text);
      ruleMatches.add(ruleMatch);
    }

    return ruleMatches;
  }


  public List<RuleMatch> arbitraryRule(List<RuleMatch> ruleMatches, AnalyzedSentence sentence, String regexRule, String message) {
    Pattern numbersPattern = Pattern.compile(regexRule);
    Matcher numbersMatcher = numbersPattern.matcher(sentence.getText());

    while (numbersMatcher.find()) {
      String match_text = numbersMatcher.group();
      int startPos = numbersMatcher.start();
      int endPos = numbersMatcher.end();

      String corrected_text = correctWhitespace(match_text);

      RuleMatch ruleMatch = new RuleMatch(this, sentence, startPos, endPos, message);
      ruleMatch.setSuggestedReplacement(corrected_text);
      ruleMatches.add(ruleMatch);
    }

    return ruleMatches;
  }


  public List<RuleMatch> currencyRule(List<RuleMatch> ruleMatches, AnalyzedSentence sentence) {
    String currencies = "([£$¥€₹¢₩₽₺₴₦₱฿₫₭₲₪]|Ls|USD|EUR|LVL|JPY|GBP|AUD|CAD|CHF|CNY|HKD|NZD|SEK|KRW|SGD|NOK|MXN|INR|RU[BR]|ZAR|TRY|BRL|TWD|DKK|PLN|THB|IDR|HUF|CZK|ILS|AED|SAR|MYR|PHP|COP|CLP|ARS|PKR|VND|EGP|NGN|KWD|DEM|FRF|ITL|ESP|NLG|BEF|GRD|PTE|ATS|IEP|FIM|LUF|SIT|SKK|CYP|MTL|YUM|ROL|LTL|EEK|HRD|BAM|TMM)";
    String currenciesFullword = "((l[ia]t|dolār|rubļ|zlot|frank|forint|riāl|dinār|cent|santīm|šiliņ)(i(em)?|[uo]s)|(mārciņ|kron|j[eē]n|juaņ|rūpij|lir|von|mark)(as|ā[ms]|u)|e[iu]ro|peso)";

    // Šeit, ja ir pareiza secība
    String currencyOrder = "([0-9]+)\\s?+("+currencies+"|"+currenciesFullword+")(?=\\s|$|\\b|\\p{P})";
    Pattern currencyOrderPattern = Pattern.compile(currencyOrder);
    Matcher currencyMatcher = currencyOrderPattern.matcher(sentence.getText());

    while (currencyMatcher.find()) {
      String match_text = currencyMatcher.group();

      int startPos = currencyMatcher.start();
      int endPos = currencyMatcher.end();
      String corrected_text = decimalRule(currencyMatcher.group(1)) + nonBreakingSpace + currencyMatcher.group(2);

      String message = RuleDescriptions.currencyMessage;
      RuleMatch ruleMatch = new RuleMatch(this, sentence, startPos, endPos, message);
      ruleMatch.setSuggestedReplacement(corrected_text);
      ruleMatches.add(ruleMatch);
    }


    // Šeit uztveram, ja ir norādīta valūta nepareizā secībā
    String wrongCurrencyOrder = "(?<=\\s|^|\\b)" + currencies+ "\\s?([0-9]+([.,][0-9]+)?(?=\\s|$|\\b|\\p{P}))";
    Pattern wrongCurrencyOrderPattern = Pattern.compile(wrongCurrencyOrder);
    Matcher wrongOrderCurrencyMatcher = wrongCurrencyOrderPattern.matcher(sentence.getText());

    while (wrongOrderCurrencyMatcher.find()) {
      String match_text = wrongOrderCurrencyMatcher.group();
      int startPos = wrongOrderCurrencyMatcher.start();
      int endPos = wrongOrderCurrencyMatcher.end();

      String corrected_text = decimalRule(wrongOrderCurrencyMatcher.group(2)) + nonBreakingSpace + wrongOrderCurrencyMatcher.group(1);

      String message = RuleDescriptions.currencyMessage;
      RuleMatch ruleMatch = new RuleMatch(this, sentence, startPos, endPos, message);
      ruleMatch.setSuggestedReplacement(corrected_text);
      ruleMatches.add(ruleMatch);
    }

    return ruleMatches;
  }


  public List<RuleMatch> measurementRule(List<RuleMatch> ruleMatches, AnalyzedSentence sentence) {
    // First let's tackle degrees
    // Be careful changing this regex, group numbers might change as well
    String degrees = "([+-])?\\s?([0-9]+(?:[.,][0-9]+)?)\\s?°(?:\\s?([CF]))?(?=\\s|$|\\b|\\p{P})";
    Pattern degreePattern = Pattern.compile(degrees);
    Matcher degreeMatcher = degreePattern.matcher(sentence.getText());

    while (degreeMatcher.find()) {
      int startPos = degreeMatcher.start();
      int endPos = degreeMatcher.end();
      String corrected_text;

      String sign = (degreeMatcher.group(1) == null) ? "" : degreeMatcher.group(1);
      String number = decimalRule(degreeMatcher.group(2));
      String measure = degreeMatcher.group(3);
      if (measure == null) {
        corrected_text = sign + number + "°";
      } else {
        corrected_text = sign + number + nonBreakingSpace + "°" + measure;
      }

      String message = RuleDescriptions.NBSPTemperatureMessage;
      RuleMatch ruleMatch = new RuleMatch(this, sentence, startPos, endPos, message);
      ruleMatch.setSuggestedReplacement(corrected_text);
      ruleMatches.add(ruleMatch);
    }


    // Next other simple measurements
    String number_pattern = "([+-])?\\s?([0-9]+(?:[.,][0-9]+)?)\\s?";
    String measures = "\\b([km]?g|[mdk]?l|[mcdk]?m(?:[²³]|\\s?/\\s?(?:[sh]|min))?|K|s|bar|psi|Pa|kPa|[kMG]?(?:[WVA]|Hz))\\b";
    Pattern measuresPattern = Pattern.compile(number_pattern + measures);
    Matcher measuresMatcher = measuresPattern.matcher(sentence.getText());

    while (measuresMatcher.find()) {
      int startPos = measuresMatcher.start();
      int endPos = measuresMatcher.end();

      String sign = (measuresMatcher.group(1) == null) ? "" : measuresMatcher.group(1);
      String number = decimalRule(measuresMatcher.group(2));
      String measure = measuresMatcher.group(3).replaceAll(" ", "");
      String corrected_text = sign + number + nonBreakingSpace + measure;

      System.out.println(corrected_text);
      String message = RuleDescriptions.NBSPUnitsMessage;
      RuleMatch ruleMatch = new RuleMatch(this, sentence, startPos, endPos, message);
      ruleMatch.setSuggestedReplacement(corrected_text);
      ruleMatches.add(ruleMatch);
    }

    return ruleMatches;
  }



  public String decimalRule(String number) {
    Pattern sep = Pattern.compile("[.,]");
    Matcher matcher = sep.matcher(number);

    if (matcher.find()) {
      if (matcher.group().equals(".")) {
        // Replace dot with comma for numerals
        number = number.replace(".", ",");
      }

      // also apply spacing for large numbers
      int decimal_sep = matcher.start();
      if (0 < decimal_sep) {
        String nonDecimal = number.substring(0, decimal_sep);
        if (nonDecimal.length() >= 5) {
          nonDecimal = bigNumberWhitespace(nonDecimal);
        }
        number = nonDecimal + number.substring(decimal_sep);
      } else {
        // Tehniski šeit var pielikt klāt 0 sākumā, bet nezinu vai tāds standarts ir?
      }
    } else {
      // If no decimals just call regular number correction function
      number = bigNumberWhitespace(number);
    }

    return number;
  }


  public String bigNumberWhitespace(String number) {
    int numberIter = number.length();
    while (numberIter > 4) {
      int insertPos = numberIter - 3;
      number = number.substring(0, insertPos) + nonBreakingSpace + number.substring(insertPos);
      numberIter -= 3;
    }
    return number;
  }


  public boolean isStandardWhitespace(AnalyzedTokenReadings token) {
    return token.isWhitespace();
  }


  public boolean isConnectedWhitespace(AnalyzedTokenReadings token) {
    return StringTools.isNonBreakingWhitespace(token.getToken());
  }

  private String correctWhitespace(String match_text) {
    String corrected_text = match_text;

    // Let's only apply the error correction at the position of the whitespace
    for (int j = 0; j < match_text.length(); j++) {
      if (Character.isWhitespace(match_text.charAt(j))) {
        corrected_text = match_text.replaceAll("\\s+", String.valueOf(nonBreakingSpace));
      }
    }

    // Add connecting whitespace if it is omitted
    Pattern dotNonWhitespace = Pattern.compile("\\.\\S");
    Matcher missingMatcher = dotNonWhitespace.matcher(match_text);

    int missed_whitespaces = 0;
    while (missingMatcher.find()) {
      int gap = missingMatcher.start()+1+missed_whitespaces;
      missed_whitespaces++;
      corrected_text = corrected_text.substring(0, gap) + nonBreakingSpace + corrected_text.substring(gap);
    }

    return corrected_text;
  }
}
