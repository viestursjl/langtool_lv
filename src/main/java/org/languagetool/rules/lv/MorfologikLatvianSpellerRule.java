package org.languagetool.rules.lv;

import java.io.IOException;
import java.util.List;
import java.util.ResourceBundle;

import org.languagetool.Language;
import org.languagetool.UserConfig;
import org.languagetool.rules.spelling.morfologik.MorfologikSpellerRule;

public final class MorfologikLatvianSpellerRule extends MorfologikSpellerRule {

  private static final String RESOURCE_FILENAME = "/lv/hunspell/lv_LV.dict";

  public MorfologikLatvianSpellerRule(ResourceBundle messages,
                                      Language language, UserConfig userConfig, List<Language> altLanguages) throws IOException {
    super(messages, language, userConfig, altLanguages);
  }

  @Override
  public String getFileName() {
    return RESOURCE_FILENAME;
  }

  @Override
  public String getId() {
    return "MORFOLOGIK_RULE_LV_LV";
  }
}
