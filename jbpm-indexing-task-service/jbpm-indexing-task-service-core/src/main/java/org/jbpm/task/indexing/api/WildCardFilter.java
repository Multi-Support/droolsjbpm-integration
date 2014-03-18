package org.jbpm.task.indexing.api;

import java.util.ArrayList;
import java.util.List;

/**
 * The wildcard filter provides matches on wildcard single char or sub strings.
 * <p/>
 * The default special chars defined in the filter is:
 * <pre>
 *  <code>?</code> for single char match
 *  <code>*</code> for string match
 *  <code>\</code> default escape char
 * </pre>
 * <p/>
 * The filter provides functionality for changing special chars for an other set
 * of special chars, making it easy to deploy the filter towards any match
 * engine.
 *
 */
public class WildCardFilter<K> extends TermFilter<K> {

    /**
     * The default single char wildcard is ?.
     */
    public static final char DEFAULT_SINGLE_CHAR_WILDCARD = '?';
    /**
     * The default 0-many char wildcard is *.
     */
    public static final char DEFAULT_GENERIC_WILDCARD = '*';
    /**
     * The default escape char (for exact match on special chars) is \.
     */
    public static final char DEFAULT_ESCAPE_CHAR = '\\';
    private char characterWildCard;
    private char stringWildCard;
    private char escapeChar;

    /**
     * Creates a new WildCharFilter with default special chars.
     *
     * @param occurs the type of filter
     * @param field  the field to filter on.
     * @param terms  the match terms.
     */
    public WildCardFilter(Occurs occurs, String field, String... terms) {
        super(occurs, field, terms);
        this.characterWildCard = DEFAULT_SINGLE_CHAR_WILDCARD;
        this.stringWildCard = DEFAULT_GENERIC_WILDCARD;
        this.escapeChar = DEFAULT_ESCAPE_CHAR;
    }

    /**
     * Creates a WildCardFilter.
     *
     * @param occurs  the type of filter
     * @param field   the field to filter on.
     * @param single  the single char wildcard char used in the input terms.
     * @param generic the generic wildcard char used in the input terms.
     * @param escape  the escape char used in the input terms.
     * @param terms   the match terms.
     */
    public WildCardFilter(Occurs occurs, String field, char single,
        char generic, char escape, String... terms) {
        super(occurs, field, terms);
        this.characterWildCard = single;
        this.stringWildCard = generic;
        this.escapeChar = escape;
    }

    /**
     * {@inheritDoc}
     */
    public boolean add(String match) {
        return matches.add(match);
    }

    /**
     * Retrieves the single char wildcard char.
     *
     * @return single character wildcard char
     */
    public char getCharacterWildCard() {
        return characterWildCard;
    }

    /**
     * Retrieves the String wildcard char.
     *
     * @return string wildcard char.
     */
    public char getStringWildCard() {
        return stringWildCard;
    }

    /**
     * Retrieves the escape char.
     *
     * @return escape char.
     */
    public char getEscapeChar() {
        return escapeChar;
    }

    /**
     * Helper method to escape a strings special charaters.
     *
     * @param argument the original string
     * @return the escaped string.
     */
    public String escape(String argument) {
        StringBuilder builder = new StringBuilder();
        char[] org = argument.toCharArray();
        for (char c : org) {
            if (isSpecial(c)) {
                builder.append(escapeChar);
            }
            builder.append(c);
        }
        return builder.toString();
    }

    /**
     * Translates all Terms to the provided set of special chars.
     *
     * @param single  the special char for single char sub.
     * @param generic the special char for generic match
     * @param escape  the new escape char.
     * @return All Terms in the special char set provided.
     */
    public String[] getFormattedTerms(char single, char generic, char escape) {

        StringBuilder builder = new StringBuilder();
        List<String> formatted = new ArrayList<String>();
        for (String term : getMatches()) {
            char[] org = term.toCharArray();
            int i = 0;
            while (i < org.length) {
                char next = org[i];
                boolean isSpecial = isSpecial(next);
                boolean isNewSpecial =
                    isContainedIn(next, single, generic, escape);

                if (isSpecial) {
                    char readAhead = org[i];
                    if (i < org.length - 1) {
                        readAhead = org[i + 1];
                    }
                    if (next == escapeChar) {
                        //can forget escape?
                        if (isSpecial(readAhead) && !isContainedIn(readAhead,
                            single, generic, escape)) {
                            builder.append(readAhead);
                            i += 2;
                        } else if (isSpecial(readAhead)) {
                            //in both
                            builder.append(escape).append(
                                newChar(readAhead, single, generic, escape));
                            i += 2;
                        } else if (!isSpecial(readAhead)) {
                            //not escape role.
                            if (isNewSpecial) {
                                builder.append(escape);
                            }
                            builder.append(next);
                            i += 1;
                        }
                    } else {
                        //print new special
                        builder.append(newChar(next, single, generic, escape));
                        i += 1;
                    }
                } else if (isNewSpecial) {
                    //not with special meaning
                    builder.append(escape).append(next);
                    i += 1;
                } else {
                    builder.append(next);
                    i += 1;
                }
            }
            formatted.add(builder.toString());
            builder.setLength(0);
        }
        return formatted.toArray(new String[formatted.size()]);
    }

    public boolean matches(Object value) {
        for (String pattern : getMatches()) {
            value = occurs == Occurs.SHOULD ? ((String) value).toLowerCase()
                : value;
            pattern = occurs == Occurs.SHOULD ? pattern.toLowerCase() : pattern;
            if (match((String) value, pattern)) {
                return true;
            }

        }
        return false;
    }

    private boolean match(String string, String pattern) {
        return match(string, pattern, 0, 0);
    }

    private boolean match(String s, String pattern, int offset, int poffset) {

        char patternChar, stringChar;
        int sLen = s.length(), pLen = pattern.length();

        while (offset < sLen && poffset < pLen) {
            patternChar = pattern.charAt(poffset);
            stringChar = s.charAt(offset);

            //single wildcard
            if (patternChar == characterWildCard) {
                ++offset;
                ++poffset;
                continue;
            }

            //match string wildcard
            // - by recursive call 'eating' a char of data.
            if (patternChar == stringWildCard) {
                if (poffset == pattern.length() - 1) {
                    return true;
                }
                while (offset < s.length()) {
                    if (match(s, pattern, offset, poffset + 1)) {
                        return true;
                    }
                    ++offset;
                }
                return false;
            }

            //pattern escape skip it read next.
            if (patternChar == escapeChar) {
                ++poffset;
                if (poffset == pattern.length()) {
                    return false;
                }
                patternChar = pattern.charAt(poffset);
            }

            // the first non-matching character means the string doesn't match
            if (patternChar != stringChar) {
                return false;
            }

            // advance to the next character
            ++poffset;
            ++offset;
        }

        // skip any multi-char wildcards in the pattern
        while (poffset < pattern.length()
            && pattern.charAt(poffset) == stringWildCard) {
            ++poffset;
        }
        //
        return (offset == s.length()) && (poffset == pattern.length());
    }

    private char newChar(char old, char single, char generic, char escape) {
        if (old == escapeChar) {
            return escape;
        }
        if (old == characterWildCard) {
            return single;
        }
        if (old == stringWildCard) {
            return generic;
        }
        return old;
    }

    private boolean isContainedIn(char needle, char... haystack) {
        for (char hay : haystack) {
            if (needle == hay) {
                return true;
            }
        }
        return false;
    }

    private boolean isSpecial(char c) {
        return c == escapeChar || c == stringWildCard || c == characterWildCard;
    }
}
