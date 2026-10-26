package org.languagetool.tagging.lv;

import lv.semti.morphology.analyzer.Analyzer;
import lv.semti.morphology.analyzer.Splitting;
import lv.semti.morphology.analyzer.Word;
import org.languagetool.tokenizers.SentenceTokenizer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class LatvianSentenceTokenizer implements SentenceTokenizer {

    private Analyzer analyzer;

    public LatvianSentenceTokenizer(Analyzer analyzer) {
        this.analyzer = analyzer;
    }


    public LinkedList<String> restoreSentences(String fulltext, LinkedList<LinkedList<Word>> grouped) {
        System.out.println(">>>"+fulltext);
        LinkedList<String> sentences = new LinkedList<>();
        int pos = 0;

        for (LinkedList<Word> sentenceWords : grouped) {
            int start = pos;
            for (Word w : sentenceWords) {
                String value = w.getToken();
                // Find the next occurrence of this word starting at pos
                int idx = fulltext.indexOf(value, pos);
                if (idx == -1) {
                    throw new IllegalStateException("Word not found in original text: " + value);
                }
                // Move cursor to just after this word
                pos = idx + value.length();
            }
            // 'pos' now points to the character right after the last word of this sentence.
            // Capture the full substring from start to pos.
            sentences.add(fulltext.substring(start, pos));
        }
        return sentences;

    }



    @Override
    public List<String> tokenize(String text) {
        LinkedList<LinkedList<Word>> nested = Splitting.tokenizeSentences(analyzer, text);
        // Result list of sentences
        LinkedList<String> sentences = restoreSentences(text, nested);

//        for (LinkedList<Word> inner : nested) {
//            StringBuilder sb = new StringBuilder();
//            for (Word w : inner) {
//                String whitespace = " ";
//                if (!w.wordforms.isEmpty()) {
//                    if (w.hasAttribute("Atstarpes pirms", "")) {
//                        whitespace = "";
//                    }
//
////                    whitespace = w.wordforms.get(0).getValue("Atstarpes pirms");
//                }
//                if (sb.length() > 0) sb.append(whitespace);
//                sb.append(w.getToken());
//            }
//            sentences.add(sb.toString());
//        }
        return sentences;
    }

    @Override
    public void setSingleLineBreaksMarksParagraph(boolean lineBreakParagraphs) {

    }

    @Override
    public boolean singleLineBreaksMarksPara() {
        return false;
    }
}
