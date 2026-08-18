package io.github.kloping.spt.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MatcherUtils {
    private MatcherUtils() { }

    public static List<String> matcherAll(String value, String regex) {
        List<String> result = new ArrayList<>();
        Matcher matcher = Pattern.compile(regex).matcher(value);
        while (matcher.find()) result.add(matcher.group());
        return result;
    }
}
