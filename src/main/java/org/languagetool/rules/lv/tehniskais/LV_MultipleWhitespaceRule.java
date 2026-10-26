package org.languagetool.rules.lv.tehniskais;

import org.languagetool.Language;
import org.languagetool.rules.Categories;
import org.languagetool.rules.ITSIssueType;
import org.languagetool.rules.MultipleWhitespaceRule;
import org.languagetool.rules.lv.LatvianErrorCategories;
import org.languagetool.rules.lv.RuleDescriptions;

import java.util.ResourceBundle;

public class LV_MultipleWhitespaceRule extends MultipleWhitespaceRule {
    public LV_MultipleWhitespaceRule(ResourceBundle messages, Language language) {
        super(messages, language);
        super.setCategory(LatvianErrorCategories.TEHN);
        setLocQualityIssueType(ITSIssueType.Whitespace);
    }

    @Override
    public String getDescription() {
        return RuleDescriptions.whitespaceMessage;
    }
}
