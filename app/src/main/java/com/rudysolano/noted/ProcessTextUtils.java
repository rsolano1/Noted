package com.rudysolano.noted;

import android.content.Context;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.TextAppearanceSpan;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.text.TextUtils.isEmpty;

/**
 * A helper class to process the text provided by the user.
 *
 * This class can be used to validate a string, remove a hash symbol from a string (useful for
 * removing the hash symbol from a hashtag), getting the first hashtag from a string, and applying
 * special formatting to a substring within a string (useful for applying special formatting to a
 * string's hashtag).
 */
public class ProcessTextUtils {

    //Values that are returned after attempting to validate a string.
    public static final int ENTRY_EMPTY = 0;
    public static final int ENTRY_MISSING_HASH = 1;
    public static final int ENTRY_HAS_MULTIPLE_HASHES = 2;
    public static final int ENTRY_VALID = 3;
    public static final int INVALID_HASHTAG = 4;

    /**
     * Validates the provided string, verifying that it is not null or empty, it contains
     * exactly one "#," and that the character after the # is a letter or number.
     * @param string the provided string
     * @return an integer value representing the validity of the string
     */
    public static int validateEntry(String string) {
        if (isEmpty(string)) {
            return ENTRY_EMPTY;
        } else if (!string.contains("#")) {
            return ENTRY_MISSING_HASH;
        } else if (string.indexOf("#") != string.lastIndexOf("#")) {
            return ENTRY_HAS_MULTIPLE_HASHES;
        }

        //Verify that the character after the hash is valid (it needs to be a letter or a number).
        try {
            int indexAfterHash = string.indexOf("#") + 1;

            if (!Character.isLetterOrDigit(string.charAt(indexAfterHash))) {
                //The character following the hash is not a letter or number, return error.
                return INVALID_HASHTAG;
            }
        } catch (StringIndexOutOfBoundsException e) {
            //Could not get the index after the hash, so hash was the last character in the string.
            // Return error.
            return INVALID_HASHTAG;
        }

        //Entry passed all tests, so it is valid.
        return ENTRY_VALID;
    }

    /**
     * Removes any occurrences of "#" from the provided string.
     * @param string the provided string
     * @return the string after removing all instances of "#"
     */
    public static String removeHash(String string) {
        return string.replace("#", "");
    }

    /**
     * Returns the first hashtag from the provided string. Finds the hashtag by using the Pattern
     * and Matcher classes. Returns an empty string if no hashtag found.
     * @param string the provided string
     */
    public static String getHashtag(String string) {
        //Look for pattern where a hash is followed by a word character (a-zA-Z_0-9) and can contain
        // apostrophe's either in the middle or the end of the string.
        Pattern pattern = Pattern.compile("#\\w+'?\\w+'?");

        Matcher matcher = pattern.matcher(string);

        String hashtag = "";

        if (matcher.find()) {
            //If a pattern match is found, the variable hashtag is set to the match; otherwise, it
            // will remain empty.
            hashtag = matcher.group();
        }

        return hashtag;
    }

    /**
     * Apply special formatting to a substring, if the substring exists.
     * @param string The entire string.
     * @param substring The substring to be formatted.
     * @param context Context of the caller.
     * @return a SpannableString object, which contains the entire string with the formatted
     * substring, or simply the string if the substring is not found
     */
    public static SpannableString formatSubstring(String string, String substring, Context context) {
        //A SpannableString object allows you to apply special formatting to a substring.
        SpannableString spannableString = new SpannableString(string);

        //Get the starting and ending position of where to apply formatting. The start position is
        // the same as the substring start position, while the end position is the end position of
        // the substring + 1 (the same is achieved with substring.length()), since the end position
        // in setSpan() is exclusive). Note that if the the substring is not found, formatting will
        // not be applied.
        int formatStartPos = string.indexOf(substring);

        if (formatStartPos != -1) {
            int formatEndPos = formatStartPos + substring.length();

            spannableString.setSpan(
                    new TextAppearanceSpan(context, R.style.tag),
                    formatStartPos, formatEndPos, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return spannableString;
    }
}
