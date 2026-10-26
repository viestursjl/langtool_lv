package org.languagetool.rules.lv;

import org.languagetool.rules.Categories;
import org.languagetool.rules.Category;
import org.languagetool.rules.CategoryId;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;


public final class LatvianErrorCategories {

    public static final Category TEHN = make("TEHN", "Tehniskais noformējums");
    public static final Category PARE = make("PARE", "Pareizrakstība");
    public static final Category FORM = make("FORM", "Formveidošana");
    public static final Category SAIST = make("SAIST", "Saistāmība");
    public static final Category STILS = make("STILS", "Stils");
    public static final Category PUNKT = make("PUNKT", "Interpunkcija");
    public static final Category SINT = make("SINT", "Sintakse");
    public static final Category KONST = make("KONST", "Vārdu un konstrukciju izvēle");

    // *** NOTE: keep in sync when making changes above ***
    public static final List<Category> ALL = Arrays.asList(TEHN, PARE, FORM, SAIST, STILS, PUNKT, SINT, KONST);

    private static Category make(String id, String message) {
        return new Category(new CategoryId(id), message);
    }

    private final String id;
    private final String messageKey;

    private LatvianErrorCategories(String id, String messageKey) {
        this.id = Objects.requireNonNull(id);
        this.messageKey = Objects.requireNonNull(messageKey);
    }

    public CategoryId getId() {
        return new CategoryId(id);
    }

    public Category getCategory(ResourceBundle messages) {
        return new Category(new CategoryId(id), messages.getString(messageKey));
    }
}
