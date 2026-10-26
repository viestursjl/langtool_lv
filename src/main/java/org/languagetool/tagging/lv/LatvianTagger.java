/* LanguageTool, a natural language style checker 
 * Copyright (C) 2006 Daniel Naber (http://www.danielnaber.de)
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 * USA
 */
package org.languagetool.tagging.lv;

import lv.semti.morphology.analyzer.Analyzer;
import lv.semti.morphology.analyzer.Word;
import lv.semti.morphology.analyzer.Wordform;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.tagging.Tagger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Latvian morphological tagger.
 * Based PeterisP/morphology.
 */
public class LatvianTagger implements Tagger {

  private Analyzer analyzer;

  public LatvianTagger(Analyzer analyzer) {
    this.analyzer = analyzer;
  }

  @Override
  public List<AnalyzedTokenReadings> tag(List<String> sentenceTokens) throws IOException {
    List<AnalyzedTokenReadings> tokenReadings = new ArrayList<>();
    for (String word: sentenceTokens) {
      List<AnalyzedToken> l = new ArrayList<>();
      Word w = analyzer.analyze(word);
      for (Wordform wf: w.wordforms) {
        l.add(new AnalyzedToken(word, wf.getTag(), wf.getValue("Pamatforma")));
      }
      if(w.wordforms.size() == 0) {
        l.add(new AnalyzedToken(word, null, null));
      }
//      System.out.println(word);
//      System.out.println(l.toString());
      tokenReadings.add(new AnalyzedTokenReadings(l, 0));
    }
    return tokenReadings;
  }

  @Override
  public AnalyzedTokenReadings createNullToken(String token, int startPos) {
    return new AnalyzedTokenReadings(new AnalyzedToken(token, null, null), startPos);
  }

  @Override
  public AnalyzedToken createToken(String token, String posTag) {
    return new AnalyzedToken(token, posTag, null);
  }
}
