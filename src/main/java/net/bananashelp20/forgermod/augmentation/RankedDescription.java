package net.bananashelp20.forgermod.augmentation;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/** Converts legacy rank lists into one rank's text without changing gameplay. */
public final class RankedDescription {
    private static final Pattern VALUES = Pattern.compile("(?<![\\w%])(?:\\d+(?:\\.\\d+)?)(?:/\\d+(?:\\.\\d+)?)*(?:%| seconds?| minutes?| blocks?| hearts?| health points?| damage| saturation| hunger)?");
    private static final Pattern SUPER_VALUE = Pattern.compile("(\\d+(?:\\.\\d+)?(?:/\\d+(?:\\.\\d+)?)*)( blocks?| seconds?)?\\s*\\((\\d+(?:\\.\\d+)?) at super rank\\)");
    public record Text(String format, List<String> values) {}
    private RankedDescription() {}

    public static Text resolve(String source, int rank) {
        var superMatcher = SUPER_VALUE.matcher(source);
        source = superMatcher.replaceAll(match -> (rank > 4 ? match.group(3) : match.group(1)) + (match.group(2) == null ? "" : match.group(2)));
        var matcher = VALUES.matcher(source);
        var format = new StringBuilder();
        var values = new ArrayList<String>();
        while (matcher.find()) {
            String token = matcher.group();
            int end = 0;
            while (end < token.length() && (Character.isDigit(token.charAt(end)) || token.charAt(end) == '.' || token.charAt(end) == '/')) end++;
            String[] ranks = token.substring(0, end).split("/");
            String value = ranks[Math.clamp(rank - 1, 0, ranks.length - 1)] + token.substring(end);
            values.add(value);
            matcher.appendReplacement(format, java.util.regex.Matcher.quoteReplacement("%s"));
        }
        matcher.appendTail(format);
        // Literal percentages must not be interpreted as formatting directives by TranslatableContents.
        String translated = format.toString().replace("%", "%%").replace("%%s", "%s");
        return new Text(translated, List.copyOf(values));
    }
}
