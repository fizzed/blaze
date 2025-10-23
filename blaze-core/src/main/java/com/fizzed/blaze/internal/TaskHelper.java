package com.fizzed.blaze.internal;

import java.util.regex.Pattern;

public class TaskHelper {

    // Regex to check the start and subsequent characters.
    // ^[a-zA-Z_$]     - Starts with a letter (a-z, A-Z), underscore, or dollar sign.
    // [a-zA-Z0-9_$]* - Followed by zero or more letters, digits, underscores, or dollar signs.
    // $               - End of the string.
    private static final Pattern VALID_METHOD_NAME_PATTERN = Pattern.compile("^[a-zA-Z_$][a-zA-Z0-9_$]*$");

    /**
     * An alternative validation method using a pre-compiled Regular Expression.
     * This version is simpler but less precise as it doesn't account for all
     * Unicode characters allowed by the JLS (e.g., 'é', 'ü').
     * It's good for identifiers restricted to ASCII.
     *
     * @param name The string to validate.
     * @return {@code true} if the string matches the regex and is not a keyword.
     */
    static public boolean isValidName(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }

        /*if (JAVA_KEYWORDS_AND_LITERALS.contains(name)) {
            return false;
        }*/

        return VALID_METHOD_NAME_PATTERN.matcher(name).matches();
    }

    static public boolean isValidConfigKey(String key) {
        // key must be non-empty, and start with a letter or number
        return key != null && key.length() > 0 && Character.isLetterOrDigit(key.charAt(0));
    }

}