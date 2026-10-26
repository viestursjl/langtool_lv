package org.languagetool.rules.lv.interpunkcija;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.*;
import org.languagetool.rules.lv.LatvianErrorCategories;
import org.languagetool.rules.lv.RuleDescriptions;

import java.util.*;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Pattern;


/**
// like omg, why is this like so ass
// like it cannot for the life of it detect any recursive quotes, what...
// ummm, just like iter through all of them and find how many quote symbols there are.
// check if any of them match in formating, ig ┗|｀O′|┛
// assign directionality if easy to determine, but like if can't then idk, guess
 // if not matching count probably mark the farthest one as a mistake??
// */


public class LV_GenericUnpairedQuotesRule extends TextLevelRule {
    private static final Pattern POSSIBLE_APOSTROPHE = Pattern.compile("[‘’']");
    private static final Pattern INCH_PATTERN = Pattern.compile(".*\\d\".*", Pattern.DOTALL);
    private static final Pattern PUNCTUATION = Pattern.compile("[\\p{Punct}…–—&&[^\"'_]]");
    private static final Pattern PUNCT_MARKS = Pattern.compile("[\\?\\.!,]");

    private final QuoteStyle recommendedQuoteStyle;
    private final List<QuoteStyle> styles;

    private final List<String> startSymbols;
    private final List<String> endSymbols;
    private final String ruleId;

    public LV_GenericUnpairedQuotesRule(ResourceBundle messages, List<String> startSymbols, List<String> endSymbols) {
        super(messages);
        this.ruleId = "QUOTES";
        super.setCategory(LatvianErrorCategories.TEHN);
        if (startSymbols.size() != endSymbols.size()) {
            throw new IllegalArgumentException("Different number of start and end symbols: " + startSymbols + " vs. " + endSymbols);
        }
        this.startSymbols = startSymbols;
        this.endSymbols = endSymbols;

        List<QuoteStyle> styles = new ArrayList<>();
        for (int j=0; j<this.startSymbols.size(); j++) {
          styles.add(new QuoteStyle(this.startSymbols.get(j), this.endSymbols.get(j)));
        }
        this.styles = styles;
        this.recommendedQuoteStyle = styles.get(0); // Magic number, currently just first list element
//        setLocQualityIssueType(ITSIssueType.Typographical);
    }



  @Override
  public String getId() {
    return ruleId;
  }

  @Override
  public String getDescription() {
    return messages.getString("desc_unpaired_quotes");
  }

  public String getMissingCloseQuote() {
        return RuleDescriptions.quoteClosedMissingMessage;
  }

  public String getMissingOpenQuote() {
        return RuleDescriptions.quoteOpenMissingMessage;
  }

  public String getDescriptionQuoteType() {
        return RuleDescriptions.quoteStyleMessage;
  }

  @Override
  public final RuleMatch[] match(List<AnalyzedSentence> sentences) {
    List<RuleMatch> ruleMatches = new ArrayList<>();

    List<QuoteStyle> styles = new ArrayList<>();
    for (int j=0; j<this.startSymbols.size(); j++) {
      styles.add(new QuoteStyle(this.startSymbols.get(j), this.endSymbols.get(j)));
    }

    int sentStartPos = 0;
    for (AnalyzedSentence sentence : sentences) {
      AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();
      validate(tokens, sentence, sentStartPos, ruleMatches);
      sentStartPos += sentence.getText().length();
    }
    // FIXME: If there are two closing quotes in a row the correction ruleMatches are here
    //  BUT! In the main ruleMatches output the second one is missing, could be due to almost overlapping positions?
//    System.out.println(">"+Arrays.toString(Arrays.stream(toRuleMatchArray(ruleMatches)).toArray()));
    return toRuleMatchArray(ruleMatches);
  }

  @Override
  public int minToCheckParagraph() {
    return -1;
  }


  private boolean isNotQuote (AnalyzedTokenReadings[] tokens, int i, int j) {
    if ((tokens[i - 1].isSentenceStart() || tokens[i].isWhitespaceBefore())
      && (i >= tokens.length -1 || tokens[i + 1].isWhitespaceBefore())) {
      return true;
    }
    if (endSymbols.get(j).equals(startSymbols.get(j))) {
      if (i < tokens.length - 1
        && !tokens[i].isWhitespaceBefore()
        && !tokens[i + 1].isWhitespaceBefore()
        && PUNCTUATION.matcher(tokens[i - 1].getToken()).matches()
        && !".".equals(tokens[i + 1].getToken())
        && PUNCTUATION.matcher(tokens[i + 1].getToken()).matches()) {
        return true;
      }
    }
    return false;
  }


  private void addMatch(OpenQuote openingQuote, AnalyzedSentence sentence, AnalyzedTokenReadings[] tokens, List<RuleMatch> ruleMatches, int sentStartPos) {
    String message = getMissingCloseQuote();
    int quoteStartPos = sentStartPos+tokens[openingQuote.position].getStartPos();
    RuleMatch match = new RuleMatch(this, sentence, quoteStartPos,
            quoteStartPos + openingQuote.style.open.length(), message);
    ruleMatches.add(match);
  }

  private void addMatch(AnalyzedTokenReadings closingQuote, AnalyzedSentence sentence, List<RuleMatch> ruleMatches, int sentStartPos) {
    String message = getMissingOpenQuote();
    int quoteStartPos = sentStartPos+closingQuote.getStartPos();
    RuleMatch match = new RuleMatch(this, sentence, quoteStartPos,
            quoteStartPos + closingQuote.getToken().length(), message);
    ruleMatches.add(match);
  }

  private void addTypeMatch(QuotePair quotePair, AnalyzedSentence sentence, AnalyzedTokenReadings[] tokens, List<RuleMatch> ruleMatches, int sentStartPos) {
      String message = getDescriptionQuoteType();
      int quoteStartPos = sentStartPos+tokens[quotePair.openPos].getStartPos();
      System.out.printf("%d %d\n", quoteStartPos, quoteStartPos+quotePair.style.open.length());
      RuleMatch ruleMatchStart = new RuleMatch(this, sentence, quoteStartPos, quoteStartPos+quotePair.style.open.length(), message);
      ruleMatchStart.setSuggestedReplacement("“");
      ruleMatches.add(ruleMatchStart);

      if (!tokens[quotePair.closePos].getToken().equals(recommendedQuoteStyle.close)) {
          int quoteClosePos = sentStartPos+tokens[quotePair.closePos].getStartPos();
          System.out.printf("%d %d\n", quoteClosePos, quoteClosePos+quotePair.style.close.length());
          RuleMatch ruleMatchClose = new RuleMatch(this, sentence, quoteClosePos, quoteClosePos+quotePair.style.close.length(), message);
          ruleMatchClose.setSuggestedReplacement(recommendedQuoteStyle.close);
          ruleMatches.add(ruleMatchClose);
      }
  }


  static class QuoteStyle {
    final String open;
    final String close;

    QuoteStyle(String open, String close) {
      this.open = open;
      this.close = close;
    }

    boolean isSymmetric() {
      return open.equals(close);
    }
  }

  static class QuotePair {
    final QuoteStyle style;
    final int openPos;
    final int closePos;

    QuotePair(QuoteStyle style, int openPos, int closePos) {
      this.style = style;
      this.openPos = openPos;
      this.closePos = closePos;
    }

    @Override
    public String toString() {
      return style.open + " @ " + openPos + " -> " + style.close + " @ " + closePos;
    }
  }

  static class OpenQuote {
    final QuoteStyle style;
    final int position;

    OpenQuote(QuoteStyle style, int position) {
      this.style = style;
      this.position = position;
    }
  }

  public void validate(
          AnalyzedTokenReadings[] tokens, AnalyzedSentence sentence, int sentStartPos, List<RuleMatch> ruleMatches
  ) {
    Deque<OpenQuote> stack = new ArrayDeque<>();
    List<QuotePair> result = new ArrayList<>();

    for (int i = 0; i < tokens.length; i++) {
      for (QuoteStyle style : this.styles) {
        // TODO: Pie simetriskajiem gadījumiem vajag pielikt šādus papildus likumus:
        //  ja whitespaceBefore -> visticamāk openQuote
        //  ja whitespaceAfter -> visticamāk closeQuote
        //  ja openQuote tieši pirms -> visticmāk openQuote
        //  ja closeQuote tieši aiz -> visticmāk closeQuote
        if (style.isSymmetric() && tokens[i].getToken().matches(style.open)) {
          if (!stack.isEmpty() && stack.peek().style == style) {
            if (tokens[i].isWhitespaceBefore() || (stack.peek().position == i-1)) {
              stack.push(new OpenQuote(style, i));
            }
            else {
              OpenQuote open = stack.pop();
              result.add(new QuotePair(style, open.position, i));
            }
          } else {
            stack.push(new OpenQuote(style, i));
          }
        }

        // FIXME: Problēma rodas, ja identificēts ir stils “Cukurs”, bet patiesībā lietots „Cukurs”
        //  te tad vajadzētu atļaut iterēt cauri visiem un, ja neatrodas match tad mest MismatchError
        else if (!style.isSymmetric()) {
          if (tokens[i].getToken().matches(style.open)) {
            stack.push(new OpenQuote(style, i));
          } else if (tokens[i].getToken().matches(style.close)) {
            if (stack.isEmpty()) {
              addMatch(tokens[i], sentence, ruleMatches, sentStartPos);
            }
            else if (stack.peek().style.equals(style)) {
                OpenQuote open = stack.pop();
                result.add(new QuotePair(style, open.position, i));
                break;
            }
          }
        }
      }
    }

    // TODO: Iterate through all quote symbols and check if stack is empty otherwise suggest edits
    //  If pair amount of quotes, especially symmetrical ones, suggest removing the whitespace before
    //  Suggest whitespace after certain punctuation marks

    // verify all stacks are empty
    for (OpenQuote openQuote: stack) {
      addMatch(openQuote, sentence, tokens, ruleMatches, sentStartPos);
    }

    // verify all pairQuotes are recommended style
    for (QuotePair quotePair: result) {
      if (!quotePair.style.equals(this.recommendedQuoteStyle)) {
        addTypeMatch(quotePair, sentence, tokens, ruleMatches, sentStartPos);
      }
    }

//    System.out.println(ruleMatches.toString());
  }
}
