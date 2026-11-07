/*
 * DeluxeCoinflip Plugin
 * Copyright (c) 2021 - 2025 Zithium Studios. All rights reserved.
 */

package net.zithium.deluxecoinflip.utility;

import net.zithium.library.utils.ColorUtil;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TextUtil {

    private static final String[] SUFFIXES = {"", "k", "M", "B", "T"};
    private static final int SHORT_MAX_LEN = 5;

    private static final DecimalFormat SHORT_DF = new DecimalFormat("##0E0");
    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getNumberInstance(Locale.US);

    private static final Pattern COLOR_SIMPLE      = Pattern.compile("(?i)(?<!&r|§r)([&§][0-9a-f])");
    private static final Pattern COLOR_HEX_HASH    = Pattern.compile("(?i)(?<!&r|§r)&#([0-9a-f]{6})");
    private static final Pattern COLOR_HEX_SECTION = Pattern.compile("(?i)(?<!&r|§r)([&§]x(?:[&§][0-9a-f]){6})");

    private static final Pattern EXPONENT_SUFFIX      = Pattern.compile("E\\d");
    private static final Pattern DECIMAL_TRAIL_MATCH  = Pattern.compile("\\d+\\.[a-zA-Z]");
    private static final Pattern RESET_DUPES          = Pattern.compile("(?i)(?:[&§]r){2,}");
    private static final Pattern STRIP_LEGACY_SIMPLE  = Pattern.compile("(?i)[§&][0-9A-FK-OR]");
    private static final Pattern STRIP_HEX_HASH       = Pattern.compile("(?i)&#[0-9A-F]{6}");
    private static final Pattern STRIP_HEX_SECTION    = Pattern.compile("(?i)[§&]x(?:[§&][0-9A-F]){6}");

    private static final BigDecimal THOUSAND = new BigDecimal(1000);
    private static final BigDecimal MILLION  = new BigDecimal(1_000_000);
    private static final BigDecimal BILLION  = new BigDecimal(1_000_000_000);
    private static final BigDecimal TRILLION = new BigDecimal(1_000_000_000_000L);

    private static final Pattern AMOUNT_INPUT = Pattern.compile("([0-9.,]*[0-9][0-9.,]*)\\s*([kKmMbBtT])?");

    private static final char NBSP = '\u00A0';

    public static String format(double number) {
        String repr = SHORT_DF.format(number);
        int expDigit = Character.getNumericValue(repr.charAt(repr.length() - 1));
        int suffixIndex = Math.max(0, Math.min(expDigit / 3, SUFFIXES.length - 1));
        repr = EXPONENT_SUFFIX.matcher(repr).replaceAll(SUFFIXES[suffixIndex]);

        while (repr.length() > SHORT_MAX_LEN || DECIMAL_TRAIL_MATCH.matcher(repr).matches()) {
            repr = repr.substring(0, repr.length() - 2) + repr.substring(repr.length() - 1);
        }

        return repr;
    }

    public static String numberFormat(long amount) {
        return NUMBER_FORMAT.format(amount);
    }

    public static String color(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        String normalized = enforceColorResets(preserveLeadingIndent(input));
        return ColorUtil.color(normalized);
    }

    public static List<String> color(List<String> lines) {
        if (lines == null || lines.isEmpty()) {
            return lines;
        }

        for (int i = 0; i < lines.size(); i++) {
            String original = Objects.toString(lines.get(i), "");
            String stripped = stripLegacyColors(original);

            if (stripped.isEmpty()) {
                lines.set(i, ColorUtil.color("&r" + NBSP));
            } else {
                String normalized = enforceColorResets(preserveLeadingIndent(original));
                lines.set(i, ColorUtil.color(normalized));
            }
        }

        return lines;
    }

    /**
     * Join a list of lines for chat: preserves "middle"/top/bottom blank lines
     * (exactly one line each), keeps leading spaces/tabs, and prevents
     * style bleed by injecting resets before color changes.
     */
    public static String fromList(List<?> lines) {
        if (lines == null || lines.isEmpty()) {
            return null;
        }

        List<String> rendered = new ArrayList<>(lines.size());
        for (Object o : lines) {
            String original = Objects.toString(o, "");
            String stripped = stripLegacyColors(original);

            if (stripped.isEmpty()) {
                rendered.add("&r" + NBSP);
            } else {
                rendered.add(enforceColorResets(preserveLeadingIndent(original)));
            }
        }

        return String.join("\n", rendered);
    }

    public static boolean isBuiltByBit() {
        final String token = new String("%__FILEHASH__%".toCharArray());
        return !token.equals("%%__FILEHASH__%%");
    }

    public static boolean isValidDownload() {
        final String token = new String("%__USER__%".toCharArray());
        return !token.equals("%%__USER__%%");
    }

    public static String enforceColorResets(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        String out = COLOR_SIMPLE.matcher(input).replaceAll("&r$1");
        out = COLOR_HEX_HASH.matcher(out).replaceAll("&r&#$1");
        out = COLOR_HEX_SECTION.matcher(out).replaceAll("&r$1");
        return RESET_DUPES.matcher(out).replaceAll("&r");
    }

    private static String stripLegacyColors(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        String out = STRIP_LEGACY_SIMPLE.matcher(input).replaceAll("");
        out = STRIP_HEX_HASH.matcher(out).replaceAll("");
        out = STRIP_HEX_SECTION.matcher(out).replaceAll("");
        return out;
    }

    private static String preserveLeadingIndent(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }

        int i = 0;
        StringBuilder lead = new StringBuilder();
        while (i < s.length()) {
            char c = s.charAt(i);
            if (c == ' ') {
                lead.append(NBSP);
            } else if (c == '\t') {
                lead.append(NBSP).append(NBSP).append(NBSP).append(NBSP);
            } else {
                break;
            }

            i++;
        }

        if (lead.isEmpty()) {
            return s;
        }

        return lead.append(s.substring(i)).toString();
    }

    public static Long parseAmountToLong(String input) {
        if (input == null) {
            return null;
        }

        final String rawInput = input.trim();
        if (rawInput.isEmpty()) {
            return null;
        }

        final Matcher matcher = AMOUNT_INPUT.matcher(rawInput);
        if (!matcher.matches()) {
            return null;
        }

        final String numberPart = normalizeSeparators(matcher.group(1));
        if (numberPart == null) {
            return null;
        }

        final String suffix = matcher.group(2) == null ? "" : matcher.group(2).toUpperCase(Locale.ROOT);
        BigDecimal multiplier = switch (suffix) {
            case "K" -> THOUSAND;
            case "M" -> MILLION;
            case "B" -> BILLION;
            case "T" -> TRILLION;
            default -> null;
        };

        try {
            BigDecimal amount = new BigDecimal(numberPart);
            if (multiplier != null) {
                amount = amount.multiply(multiplier);
            }

            amount = amount.setScale(0, RoundingMode.FLOOR);
            return amount.longValueExact();
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Converts a number using either ',' or '.' as decimal/grouping separator
     * into a plain BigDecimal-compatible string (e.g. "1234.5").
     * <ul>
     *   <li>Both separators present: the last one is the decimal separator ("1.234,5", "1,234.5").</li>
     *   <li>One separator used multiple times: grouping ("1.000.000", "1,000,000").</li>
     *   <li>One separator used once: grouping if followed by exactly 3 digits ("1,500", "1.500"),
     *       otherwise decimal ("1,5", "1.25").</li>
     * </ul>
     * Returns null if the input is malformed.
     */
    private static String normalizeSeparators(String number) {
        final int lastComma = number.lastIndexOf(',');
        final int lastDot = number.lastIndexOf('.');

        String integerPart;
        String fractionPart = "";

        if (lastComma >= 0 && lastDot >= 0) {
            final int decimalIndex = Math.max(lastComma, lastDot);
            final char groupingChar = decimalIndex == lastComma ? '.' : ',';
            integerPart = number.substring(0, decimalIndex);
            fractionPart = number.substring(decimalIndex + 1);
            // The grouping separator must only appear before the decimal separator.
            if (fractionPart.indexOf(groupingChar) >= 0 || !isValidGrouping(integerPart, groupingChar)) {
                return null;
            }
            integerPart = integerPart.replace(String.valueOf(groupingChar), "");
        } else if (lastComma >= 0 || lastDot >= 0) {
            final char sep = lastComma >= 0 ? ',' : '.';
            final int first = number.indexOf(sep);
            final int last = number.lastIndexOf(sep);
            final int digitsAfter = number.length() - last - 1;

            if (first != last || (digitsAfter == 3 && first > 0)) {
                if (!isValidGrouping(number, sep)) {
                    return null;
                }
                integerPart = number.replace(String.valueOf(sep), "");
            } else {
                integerPart = number.substring(0, last);
                fractionPart = number.substring(last + 1);
            }
        } else {
            integerPart = number;
        }

        if (integerPart.isEmpty()) {
            integerPart = "0";
        }
        if (fractionPart.isEmpty()) {
            return integerPart;
        }
        return integerPart + "." + fractionPart;
    }

    /** Checks that groups after the first are exactly 3 digits, e.g. "1,234,567". */
    private static boolean isValidGrouping(String number, char groupingChar) {
        final String[] groups = number.split(Pattern.quote(String.valueOf(groupingChar)), -1);
        if (groups[0].isEmpty() && groups.length > 1) {
            return false;
        }
        for (int i = 1; i < groups.length; i++) {
            if (groups[i].length() != 3) {
                return false;
            }
        }
        return true;
    }
}
