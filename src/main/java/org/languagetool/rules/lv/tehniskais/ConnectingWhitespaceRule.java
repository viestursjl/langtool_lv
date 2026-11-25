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
  public Character hyphen = '-';
  public Character dash = '–';
  public Character minus = '−';

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
    ruleMatches = spanRule(ruleMatches, sentence);
    ruleMatches = currencyRule(ruleMatches, sentence);
    ruleMatches = measurementRule(ruleMatches, sentence);
    ruleMatches = numbersRule(ruleMatches, sentence);
//    ruleMatches = arbitraryRule(ruleMatches, sentence, "(?U)\\b[1-9]\\d*\\.?\\s?(g\\.|\\p{Ll}{2,})(?=\\s|$|\\p{Punct})", RuleDescriptions.NBSPUnitsMessage);

    // After that let's fix NBSP usage before dashes
    ruleMatches = dashRule(ruleMatches, sentence);

    // NBSP usage inbetween numbers
//    ruleMatches = arbitraryRule(ruleMatches, sentence, "(?<![./])\\b[1-9]\\d{4,}\\b(?!/)", RuleDescriptions.NBSPNumbersMessage);
//    ruleMatches = arbitraryRule(ruleMatches, sentence, "\\b(\\+\\d{2,3}\\s)?\\d{1,3}(\\s\\d{3})+\\b", RuleDescriptions.NBSPNumbersMessage);


    // NBSP usgae before initials
    ruleMatches = arbitraryRule(ruleMatches, sentence, "(?U)\\b(\\p{Lu}\\.\\s?)+(\\p{Lu}\\p{Ll}+)\\b", RuleDescriptions.NBSPInitialMessage);

    // NBSP usage after/inbetween abbreviations
    ruleMatches = arbitraryRule(ruleMatches, sentence, "(?U)\\b(Nr|pp)\\.\\s?", RuleDescriptions.NBSPAbbrevMessage);
    ruleMatches = arbitraryRule(ruleMatches, sentence, "(?U)\\b(Sv|Dr)\\.\\s?\\p{Lu}\\p{Ll}+(\\b|\\s|$)", RuleDescriptions.NBSPAbbrevMessage);

    ruleMatches = arbitraryRule(ruleMatches, sentence, "(?U)\\bt\\.\\s?(i|sk)\\.", RuleDescriptions.NBSPAbbrevChainMessage);
    ruleMatches = arbitraryRule(ruleMatches, sentence, "(?U)\\b(Ph\\.\\s?D\\.)|(Mg|Dr|Ph\\.\\s?D)\\.\\s?(habil\\.\\s?)?(arch|biol|oec|pharm|phil(ol)?|sc\\.\\s?(comp|ing|soc|pol)|phys|geogr|med(\\.\\s?(vet))?|chem|agr|math|silv|paed|theol|iur|hist)\\.", RuleDescriptions.NBSPAbbrevChainMessage);
    ruleMatches = arbitraryRule(ruleMatches, sentence, "(?U)\\bu\\.\\s?(c|tml|t\\.\\s?jpr)\\.", RuleDescriptions.NBSPAbbrevChainMessage);
    ruleMatches = arbitraryRule(ruleMatches, sentence, "(?U)\\ba\\.\\s?(k|god)\\.", RuleDescriptions.NBSPAbbrevChainMessage);
    ruleMatches = arbitraryRule(ruleMatches, sentence, "(?U)\\bd\\.\\s?k\\.", RuleDescriptions.NBSPAbbrevChainMessage);
    ruleMatches = arbitraryRule(ruleMatches, sentence, "(?U)\\bdz\\.\\s?v\\.", RuleDescriptions.NBSPAbbrevChainMessage);
    ruleMatches = arbitraryRule(ruleMatches, sentence, "(?U)\\bg\\.\\s?t\\.", RuleDescriptions.NBSPAbbrevChainMessage);
    ruleMatches = arbitraryRule(ruleMatches, sentence, "(?U)\\bīst\\.\\s?v\\.", RuleDescriptions.NBSPAbbrevChainMessage);
    ruleMatches = arbitraryRule(ruleMatches, sentence, "(?U)\\bļ\\.\\s?cien\\.", RuleDescriptions.NBSPAbbrevChainMessage);
    ruleMatches = arbitraryRule(ruleMatches, sentence, "(?U)\\bļ\\.\\s?cien\\.", RuleDescriptions.NBSPAbbrevChainMessage);
    ruleMatches = arbitraryRule(ruleMatches, sentence, "(?U)\\bpers\\.\\s?k\\.", RuleDescriptions.NBSPAbbrevChainMessage);
    ruleMatches = arbitraryRule(ruleMatches, sentence, "(?U)\\bp\\.\\s?[kni]\\.", RuleDescriptions.NBSPAbbrevChainMessage);
    ruleMatches = arbitraryRule(ruleMatches, sentence, "(?U)\\b(p\\.\\s?)?m\\.\\s?ē\\.", RuleDescriptions.NBSPAbbrevChainMessage);
    ruleMatches = arbitraryRule(ruleMatches, sentence, "(?U)\\bP\\.\\s?S\\.", RuleDescriptions.NBSPAbbrevChainMessage);
    ruleMatches = arbitraryRule(ruleMatches, sentence, "(?U)\\bt\\.\\s?[ps]\\.", RuleDescriptions.NBSPAbbrevChainMessage);
    ruleMatches = arbitraryRule(ruleMatches, sentence, "(?U)\\bu\\.\\s?g\\.", RuleDescriptions.NBSPAbbrevChainMessage);
    ruleMatches = arbitraryRule(ruleMatches, sentence, "(?U)\\bu\\.\\s?t\\.\\s?jpr\\.", RuleDescriptions.NBSPAbbrevChainMessage);
    ruleMatches = arbitraryRule(ruleMatches, sentence, "(?U)\\bv\\.\\s?tml\\.", RuleDescriptions.NBSPAbbrevChainMessage);
    ruleMatches = arbitraryRule(ruleMatches, sentence, "(?U)\\bz\\.\\s?v\\.", RuleDescriptions.NBSPAbbrevChainMessage);
    ruleMatches = arbitraryRule(ruleMatches, sentence, "(?U)\\bet\\.?\\s?(al)\\.?", RuleDescriptions.NBSPAbbrevChainMessage);

    return toRuleMatchArray(ruleMatches);
  }



  public List<RuleMatch> dashRule(List<RuleMatch> ruleMatches, AnalyzedSentence sentence) {
    String dashMatch = "(?U)(\\b\\s*|\\s+)([‒–—])(?=\\s|$)";
    Pattern dashPattern = Pattern.compile(dashMatch);
    Matcher dashMatcher = dashPattern.matcher(sentence.getText());

    while (dashMatcher.find()) {
      String message = RuleDescriptions.NBSPDashMessage;
      String match_text = dashMatcher.group();
      int startPos = dashMatcher.start();
      int endPos = dashMatcher.end();

      String corrected_text = correctWhitespace(match_text);

      corrected_text = DefiseFormatRule.replaceLongDomuzime(corrected_text);

      if (match_text.matches("(?U)^\\S+")) {
        // Dažādus simbolus atdalot jāizmanto atstarpe
        corrected_text = nonBreakingSpace + corrected_text;
      } else {
        corrected_text = corrected_text.replaceAll("(?U)\\s+", String.valueOf(nonBreakingSpace));
      }

      if (!corrected_text.equals(match_text)) {
        RuleMatch ruleMatch = new RuleMatch(this, sentence, startPos, endPos, message);
        ruleMatch.setSuggestedReplacement(corrected_text);
        ruleMatches.add(ruleMatch);
      }
    }

    return ruleMatches;
  }

  // TODO: Generalize this regex outside, so that it can be used for other spans, e.g. (1881 – 1884) and 6-8%
  public List<RuleMatch> spanRule(List<RuleMatch> ruleMatches, AnalyzedSentence sentence) {
    // This regex is vile
    String spanMatch = "(?U)\\b([1-9](?:[0-9]*(?:[^\\S\\r\\n][0-9]+)?)?\\.?)(?:(?:\\s*[-‒–—]\\s*)([1-9](?:[0-9]*(?:[^\\S\\r\\n][0-9]+)?)?\\.?))?\\s?(g\\.|\\p{Ll}{2,})(?=\\s|$|\\p{Punct})";
    Pattern spanPattern = Pattern.compile(spanMatch);
    Matcher spanMatcher = spanPattern.matcher(sentence.getText());

    while (spanMatcher.find()) {
      String message;
      String match_text = spanMatcher.group();
      int startPos = spanMatcher.start();
      int endPos = spanMatcher.end();

      String corrected_text = "";

      if (spanMatcher.group(2) != null) {
          message = RuleDescriptions.dashFormatMessage + " " + RuleDescriptions.NBSPUnitsMessage;
          corrected_text =
                decimalRule(spanMatcher.group(1).replaceAll("(?U)\\s", ""))+
                dash.toString()+
                decimalRule(spanMatcher.group(2).replaceAll("(?U)\\s", ""))+
                nonBreakingSpace.toString()+
                spanMatcher.group(3);
      } else {
          message = RuleDescriptions.NBSPUnitsMessage;
          corrected_text =
                decimalRule(spanMatcher.group(1).replaceAll("(?U)\\s", ""))+
                nonBreakingSpace.toString()+
                spanMatcher.group(3);
      }

      if (!corrected_text.equals(match_text)) {
        RuleMatch ruleMatch = new RuleMatch(this, sentence, startPos, endPos, message);
        ruleMatch.setSuggestedReplacement(corrected_text);
        ruleMatches.add(ruleMatch);
      }
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

      if (!corrected_text.equals(match_text)) {
        RuleMatch ruleMatch = new RuleMatch(this, sentence, startPos, endPos, message);
        ruleMatch.setSuggestedReplacement(corrected_text);
        ruleMatches.add(ruleMatch);
      }
    }

    return ruleMatches;
  }


  public List<RuleMatch> currencyRule(List<RuleMatch> ruleMatches, AnalyzedSentence sentence) {
    String currencies = "([£$¥€₹¢₩₽₺₴₦₱฿₫₭₲₪]|Ls|USD|EUR|LVL|JPY|GBP|AUD|CAD|CHF|CNY|HKD|NZD|SEK|KRW|SGD|NOK|MXN|INR|RU[BR]|ZAR|TRY|BRL|TWD|DKK|PLN|THB|IDR|HUF|CZK|ILS|AED|SAR|MYR|PHP|COP|CLP|ARS|PKR|VND|EGP|NGN|KWD|DEM|FRF|ITL|ESP|NLG|BEF|GRD|PTE|ATS|IEP|FIM|LUF|SIT|SKK|CYP|MTL|YUM|ROL|LTL|EEK|HRD|BAM|TMM)";
    String currenciesFullword = "((l[ia]t|dolār|rubļ|zlot|frank|forint|riāl|dinār|cent|santīm|šiliņ)(i(em)?|[uo]s)|(mārciņ|kron|j[eē]n|juaņ|rūpij|lir|von|mark)(as|ā[ms]|u)|e[iu]ro|peso)";

    // Šeit, ja ir pareiza secība
    String currencyOrder = "(?U)([0-9]+(?:[^\\S\\r\\n][0-9]+)*)[^\\S\\r\\n]*("+currencies+"|"+currenciesFullword+")(?=\\s|$|\\b|\\p{P})";
    Pattern currencyOrderPattern = Pattern.compile(currencyOrder);
    Matcher currencyMatcher = currencyOrderPattern.matcher(sentence.getText());

    while (currencyMatcher.find()) {
      String match_text = currencyMatcher.group();

      int startPos = currencyMatcher.start();
      int endPos = currencyMatcher.end();
      String corrected_text = decimalRule(currencyMatcher.group(1)) + nonBreakingSpace + currencyMatcher.group(2);
      String message = RuleDescriptions.currencyMessage;

      if (!corrected_text.equals(match_text)) {
        RuleMatch ruleMatch = new RuleMatch(this, sentence, startPos, endPos, message);
        ruleMatch.setSuggestedReplacement(corrected_text);
        ruleMatches.add(ruleMatch);
      }
    }


    // Šeit uztveram, ja ir norādīta valūta nepareizā secībā
    String wrongCurrencyOrder = "(?U)(?<=\\s|^|\\b)" + currencies+ "[^\\S\\r\\n]?([0-9]+(?:[^\\S\\r\\n][0-9]+)*([.,][0-9]+(?:[^\\S\\r\\n][0-9]+)*)?(?=\\s|$|\\b|\\p{P}))";
    Pattern wrongCurrencyOrderPattern = Pattern.compile(wrongCurrencyOrder);
    Matcher wrongOrderCurrencyMatcher = wrongCurrencyOrderPattern.matcher(sentence.getText());

    while (wrongOrderCurrencyMatcher.find()) {
      String match_text = wrongOrderCurrencyMatcher.group();
      int startPos = wrongOrderCurrencyMatcher.start();
      int endPos = wrongOrderCurrencyMatcher.end();

      String corrected_text = decimalRule(wrongOrderCurrencyMatcher.group(2)) + nonBreakingSpace + wrongOrderCurrencyMatcher.group(1);

      if (!corrected_text.equals(wrongOrderCurrencyMatcher.group())) {
        String message = RuleDescriptions.currencyMessage;
        RuleMatch ruleMatch = new RuleMatch(this, sentence, startPos, endPos, message);
        ruleMatch.setSuggestedReplacement(corrected_text);
        ruleMatches.add(ruleMatch);
      }
    }

    return ruleMatches;
  }


  public List<RuleMatch> measurementRule(List<RuleMatch> ruleMatches, AnalyzedSentence sentence) {
    // group 1 - plus/minus sign
    // group 2 - first number
    // group 3 - second number, after dash (can be null)
    String numberis = "(?U)(?<=\\s|^|\\b)(?:([+−–—\\-])[^\\S\\r\\n]?)?([0-9]+(?:[^\\S\\r\\n][0-9]+)*(?:[.,][0-9]+(?:[^\\S\\r\\n][0-9]+)*)?)(?:\\s*[-–—]\\s*([0-9]+(?:[^\\S\\r\\n][0-9]+)*(?:[.,][0-9]+(?:[^\\S\\r\\n][0-9]+)*)?))?";
    Pattern numbers = Pattern.compile(numberis);

    // First let's tackle degrees
    // Be careful changing this regex, group numbers are important here
    String degrees = numberis + "[^\\S\\r\\n]?°(?:[^\\S\\r\\n]?([CF]))?(?=\\s|$|\\b|\\p{P})";
    Pattern degreePattern = Pattern.compile(degrees);
    Matcher degreeMatcher = degreePattern.matcher(sentence.getText());

    while (degreeMatcher.find()) {
      int startPos = degreeMatcher.start();
      int endPos = degreeMatcher.end();
      String corrected_text;

      String sign = (degreeMatcher.group(1) == null) ? "" : degreeMatcher.group(1).replaceAll("[-–‒]", "−");
      String number = decimalRule(degreeMatcher.group(2).replaceAll("(?U)[^\\S\\r\\n]", ""));
      String number2 = (degreeMatcher.group(3) == null) ? "" : "–"+decimalRule(degreeMatcher.group(3).replaceAll("(?U)[^\\S\\r\\n]", ""));
      String measure = degreeMatcher.group(4);
      String message;

      if (measure == null) {
        corrected_text = sign + number + number2 + "°";
        message = RuleDescriptions.DegreesMessage;
      } else {
        corrected_text = sign + number + number2 + nonBreakingSpace + "°" + measure;
        message = RuleDescriptions.NBSPTemperatureMessage;
      }

      if (!corrected_text.equals(degreeMatcher.group())) {
        RuleMatch ruleMatch = new RuleMatch(this, sentence, startPos, endPos, message);
        ruleMatch.setSuggestedReplacement(corrected_text);
        ruleMatches.add(ruleMatch);
      }
    }


    // Next other simple measurements
    // FIXME: A var būt gan ampēri, gan māju adresēs, i.e. Gustava Zemgala gatve 74A, Rīga
    String number_pattern = numberis + "\\s?";
    String measures = "([km]?g|ha|[mdk]?l|[mcdk]?m(?:[²³]|(?U)\\s?\\/\\s?(?:[sh]|min))?|K|s|%|bar|psi|Pa|kPa|[kMG]?(?:[WVA]|Hz))(?=(?U)\\s|$|\\b|\\p{P})";
    Pattern measuresPattern = Pattern.compile(number_pattern + measures);
    Matcher measuresMatcher = measuresPattern.matcher(sentence.getText());

    while (measuresMatcher.find()) {
      int startPos = measuresMatcher.start();
      int endPos = measuresMatcher.end();

      String sign = (measuresMatcher.group(1) == null) ? "" : measuresMatcher.group(1).replaceAll("[-–‒]", "−");
      String number = decimalRule(measuresMatcher.group(2).replaceAll("(?U)[^\\S\\r\\n]", ""));
      String number2 = (measuresMatcher.group(3) == null) ? "" : dash.toString()+decimalRule(measuresMatcher.group(3).replaceAll("(?U)[^\\S\\r\\n]", ""));
      String measure = measuresMatcher.group(4);

      String corrected_text = sign + number + number2 + nonBreakingSpace + measure;

      if (!corrected_text.equals(measuresMatcher.group())) {
        String message = RuleDescriptions.NBSPUnitsMessage;
        RuleMatch ruleMatch = new RuleMatch(this, sentence, startPos, endPos, message);
        ruleMatch.setSuggestedReplacement(corrected_text);
        ruleMatches.add(ruleMatch);
      }
    }

    return ruleMatches;
  }


  public List<RuleMatch> numbersRule(List<RuleMatch> ruleMatches, AnalyzedSentence sentence) {
    // Šeit, ja ir pareiza secība
    // FIXME: Šis tad vienkārši atlasīs jebkuru skaitli tāpatās, kas beidzas ar punktu tikai izlaižot pēdējo ciparu
    String number = "(?U)(?<=^|\\s)([0-9]+(?:[^\\S\\r\\n][0-9]+)*)([.,][0-9]+(?:[^\\S\\r\\n][0-9]+)*(?![.\\-\\/]))?(?=$|\\s)";
    Pattern numberPattern = Pattern.compile(number);
    Matcher numberMatcher = numberPattern.matcher(sentence.getText());

    while (numberMatcher.find()) {
      // Atlasīt potenciālus pulkteņa laikus (3.00) un datumus (24.08.)

      String match_text = numberMatcher.group();

      int startPos = numberMatcher.start();
      int endPos = numberMatcher.end();
      String corrected_text = decimalRule(numberMatcher.group().replaceAll("(?U)\\s",""));

      if (!corrected_text.equals(match_text)) {
        String message = RuleDescriptions.NBSPNumbersMessage;
        RuleMatch ruleMatch = new RuleMatch(this, sentence, startPos, endPos, message);
        ruleMatch.setSuggestedReplacement(corrected_text);
        ruleMatches.add(ruleMatch);
      }
    }
    return ruleMatches;
  }


  public String decimalRule(String number) {
    Pattern sep = Pattern.compile("[.,]");
    Matcher matcher = sep.matcher(number);

    if (matcher.find()) {
      if (matcher.group().equals(".") && matcher.end() != number.length()) {
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
    if (numberIter>=5) {
      while (numberIter >= 4) {
        int insertPos = numberIter - 3;
        number = number.substring(0, insertPos) + nonBreakingSpace + number.substring(insertPos);
        numberIter -= 3;
      }
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
        corrected_text = match_text.replaceAll("(?U)\\s+", String.valueOf(nonBreakingSpace));
      }
    }

    // Add connecting whitespace if it is omitted
    Pattern dotNonWhitespace = Pattern.compile("(?U)\\.\\S");
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
