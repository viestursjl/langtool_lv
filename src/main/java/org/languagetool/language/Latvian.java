/* LanguageTool, a natural language style checker 
 * Copyright (C) 2007 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.language;

import lv.semti.morphology.analyzer.Analyzer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.languagetool.Language;
import org.languagetool.LanguageMaintainedState;
import org.languagetool.UserConfig;
import org.languagetool.rules.*;
//import org.languagetool.rules.lv.CFGRuleFactory;
//import org.languagetool.rules.lv.ExampleCFGRule;
import org.languagetool.rules.lv.MorfologikLatvianSpellerRule;
import org.languagetool.rules.lv.interpunkcija.*;
import org.languagetool.rules.lv.stils.VaratRule;
import org.languagetool.rules.lv.tehniskais.*;
import org.languagetool.rules.spelling.SpellingCheckRule;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.synthesis.lv.LatvianSynthesizer;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.lv.LatvianSentenceTokenizer;
import org.languagetool.tagging.lv.LatvianTagger;
import org.languagetool.tokenizers.SRXSentenceTokenizer;
import org.languagetool.tokenizers.SentenceTokenizer;

import java.io.IOException;
import java.util.*;

/**
 * Actively maintained since v6.2+
 */
public class Latvian extends Language {

  private static Analyzer analyzer = null;

  private static Analyzer getAnalyzer() {
    if(analyzer == null) {
      try {
        analyzer = new Analyzer();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    return analyzer;
  }

  @Override
  public String getName() {
    return "Latvian";
  }

  @Override
  public String getShortCode() {
    return "lv";
  }

  @Override
  public String[] getCountries() {
    return new String[]{"LV"};
  }

  @Override
  @NotNull
  public Tagger createDefaultTagger() {
    return new LatvianTagger(getAnalyzer());
  }

  @Override
  public SentenceTokenizer createDefaultSentenceTokenizer() {
//    return new LatvianSentenceTokenizer(getAnalyzer());
    return new SRXSentenceTokenizer(this);
  }


  @Override
  public Contributor[] getMaintainers() {
    return new Contributor[] {new Contributor("Viesturs Jūlijs Lasmanis")};
  }

  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, Language motherTongue, List<Language> altLanguages) throws IOException {
    return Arrays.asList(
            new CommaWhitespaceRule(messages, true),
            // Latviešu valodai custom noteikumi
            new DecadeNotationRule(messages, this),
            new ConnectingWhitespaceRule(messages, this),

            // FIXME: Testing DashFormatRule refactoring
//            new DashFormatRule(messages, this),
//            new org.languagetool.rules.lv.tehniskais.refactor.DashFormatRule(messages, this),

            new DefiseFormatRule(messages, this),
            new VaratRule(messages, this),
            new UnnecessaryWhitespaceRule(messages, this),

            // Built-in noteikumi
            // TODO: Ko darīt ar kļūdas tipu???
            new LV_DoublePunctuationRule(messages),
            new LV_GenericUnpairedBracketsRule(messages, Arrays.asList("[", "(", "{"), Arrays.asList("]", ")", "}")),
            // NB: recommended quote style has to be first listed
            new LV_GenericUnpairedQuotesRule(messages, Arrays.asList("“", "\"",  "„", "«", "‘", "‘", ",,", "‘’"), Arrays.asList("”", "\"", "”", "»", "”", "’", "”", "”")),
            new UppercaseSentenceStartRule(messages, this),
            new LongSentenceRule(messages, userConfig, 40),
            new LV_WordRepeatRule(messages, this),
            new LV_MultipleWhitespaceRule(messages, this)
    );
  }

  @Override
  public LanguageMaintainedState getMaintainedState() {
    return LanguageMaintainedState.ActivelyMaintained;
  }

  @Override
  @NotNull
  public Synthesizer createDefaultSynthesizer() {
    return new LatvianSynthesizer(getAnalyzer());
  }

  @Nullable
  @Override
  protected SpellingCheckRule createDefaultSpellingRule(ResourceBundle messages) throws IOException {
    return new MorfologikLatvianSpellerRule(messages, this, null, null);
  }
}
