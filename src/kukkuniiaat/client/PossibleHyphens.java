package kukkuniiaat.client;

import com.sun.star.lang.Locale;
import com.sun.star.linguistic2.XPossibleHyphens;

public class PossibleHyphens implements XPossibleHyphens {
    private String word;
    private String lcword;
    private Locale locale;

    public PossibleHyphens(String word, Locale locale) {
        this.word = word;
        this.lcword = word.toLowerCase();
        this.locale = locale;
    }

    public short[] getHyphenationPositions() {
        /*
        - split between double consonants, but beware that "ng" is 1 consonant if followed by a vowel
        - split between different vowels, except "ai" in last syllable
        - split before single consonant if there are no double consonants
        - split between r and any later consonant
        //*/
        int num = 0;
        for(short i = 0 ; i < lcword.length()-2 ; ++i) {
            if (lcword.charAt(i) == lcword.charAt(i+1) && Hyphenator.is_consonant(lcword.charAt(i))) {
                ++num;
            }
            else if (lcword.charAt(i) != lcword.charAt(i+1) && Hyphenator.is_vowel(lcword.charAt(i)) && Hyphenator.is_vowel(lcword.charAt(i+1))) {
                ++num;
            }
            else if (Hyphenator.is_vowel(lcword.charAt(i)) && Hyphenator.is_consonant(lcword.charAt(i+1)) && Hyphenator.is_vowel(lcword.charAt(i+2))) {
                ++num;
            }
            else if (i < lcword.length()-3 && Hyphenator.is_vowel(lcword.charAt(i)) && lcword.charAt(i+1) == 'n' && lcword.charAt(i+2) == 'g' && Hyphenator.is_vowel(lcword.charAt(i+3))) {
                ++num;
            }
            else if (lcword.charAt(i) == 'r' && Hyphenator.is_consonant(lcword.charAt(i+1))) {
                ++num;
            }
            else if (lcword.charAt(i) == 't' && lcword.charAt(i+1) == 's') {
                ++num;
            }
        }

        short[] ps = new short[num];
        num = 0;
        for(short i = 0 ; i < lcword.length()-2 ; ++i) {
            if (lcword.charAt(i) == lcword.charAt(i+1) && Hyphenator.is_consonant(lcword.charAt(i))) {
                ps[num++] = i;
            }
            else if (lcword.charAt(i) != lcword.charAt(i+1) && Hyphenator.is_vowel(lcword.charAt(i)) && Hyphenator.is_vowel(lcword.charAt(i+1))) {
                ps[num++] = i;
            }
            else if (Hyphenator.is_vowel(lcword.charAt(i)) && Hyphenator.is_consonant(lcword.charAt(i+1)) && Hyphenator.is_vowel(lcword.charAt(i+2))) {
                ps[num++] = i;
            }
            else if (i < lcword.length()-3 && Hyphenator.is_vowel(lcword.charAt(i)) && lcword.charAt(i+1) == 'n' && lcword.charAt(i+2) == 'g' && Hyphenator.is_vowel(lcword.charAt(i+3))) {
                ps[num++] = i;
            }
            else if (lcword.charAt(i) == 'r' && Hyphenator.is_consonant(lcword.charAt(i+1))) {
                ps[num++] = i;
            }
            else if (lcword.charAt(i) == 't' && lcword.charAt(i+1) == 's') {
                ps[num++] = i;
            }
        }
        return ps;
    }

    public Locale getLocale() {
        return locale;
    }

    public String getPossibleHyphens() {
        short[] ps = getHyphenationPositions();
        StringBuilder sb = new StringBuilder(word);
        for (int i=ps.length ; i>0 ; --i) {
            sb.insert(ps[i-1]+1, '=');
        }
        return sb.toString();
    }

    public String getWord() {
        return word;
    }
}
