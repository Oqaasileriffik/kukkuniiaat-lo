package kukkuniiaat.client;

import com.sun.star.lang.Locale;
import com.sun.star.linguistic2.XSpellAlternatives;

public class SpellAlternatives implements XSpellAlternatives {
    private String word;
    private Locale locale;

    public SpellAlternatives(String word, Locale locale) {
        this.word = word;
        this.locale = locale;
    }

    public String getWord() {
        return word;
    }

    public Locale getLocale() {
        return locale;
    }

    public String[] getAlternatives() {
        String[] rv = new String[0];
        return rv;
    }

    public short getAlternativesCount() {
        return 0;
    }

    public short getFailureType() {
        return 1;
    }
}
