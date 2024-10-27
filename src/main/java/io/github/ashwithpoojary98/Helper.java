package io.github.ashwithpoojary98;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


public class Helper {

    public static boolean isArray(Object value) {
        if (value instanceof String) {
            return false;
        }
        if (value instanceof byte[]) {
            return false;
        }
        return value instanceof Iterable;
    }

    public static List<Object> flatten(List<Object> array) {
        List<Object> result = new ArrayList<>();
        for (Object item : array) {
            if (isArray(item)) {
                for (Object sub : (Iterable<?>) item) {
                    result.add(sub);
                }
            } else {
                result.add(item);
            }
        }
        return result;
    }


    public static List<Integer> allIndexesOf(String str, String value) {
        List<Integer> indexes = new ArrayList<>();
        if (value == null || value.isEmpty()) {
            return indexes;
        }

        int index = 0;
        while ((index = str.indexOf(value, index)) != -1) {
            indexes.add(index);
            index += value.length();
        }
        return indexes;
    }

    public static String replaceAll(String subject, String match, String escapeCharacter, Function<Integer, String> callback) {
        if (subject == null || subject.trim().isEmpty() || !subject.contains(match)) {
            return subject;
        }

        String regex = String.format("(?<!%s)[%s]", Pattern.quote(escapeCharacter), Pattern.quote(match));
        String modifiedInput = subject + '\0';

        String[] splitStrings = Pattern.compile(regex).split(modifiedInput);
        if (splitStrings.length > 0) {
            splitStrings[splitStrings.length - 1] = splitStrings[splitStrings.length - 1].replace("\0", "");
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < splitStrings.length; i++) {
            if (i > 0) {
                result.append(callback.apply(i - 1));
            }
            result.append(splitStrings[i]);
        }
        return result.toString();
    }

    public static String removeEscapeCharacter(String subject, String match, String escapeCharacter) {
        return subject.replaceAll(Pattern.quote(escapeCharacter) + Pattern.quote(match), match);
    }

    public static String joinArray(String glue, Iterable<?> array) {
        return StreamSupport.stream(array.spliterator(), false)
                .map(Object::toString)
                .collect(Collectors.joining(glue));
    }

    public static String expandParameters(String sql, String placeholder, String escapeCharacter, Object[] bindings) {
        return replaceAll(sql, placeholder, escapeCharacter, i -> {
            Object parameter = bindings[i];
            if (isArray(parameter)) {
                int count = ((List<?>) parameter).size();
                return String.join(",", Collections.nCopies(count, placeholder));
            }
            return placeholder;
        });
    }


    public static List<String> expandExpression(String expression) {
        String regex = "^(?:\\w+\\.){1,2}\\{([^}]*)}";
        Matcher matcher = Pattern.compile(regex, Pattern.MULTILINE).matcher(expression);

        if (!matcher.find()) {
            return Collections.singletonList(expression);
        }

        String table = expression.substring(0, expression.indexOf(".{"));
        String captures = matcher.group(1);
        return Arrays.stream(captures.split("\\s*,\\s*"))
                .map(x -> table + "." + x.trim())
                .collect(Collectors.toList());
    }

    public static Iterable<String> repeat(String str, int count) {
        return Collections.nCopies(count, str);
    }

    public static String replaceIdentifierUnlessEscaped(String input, String escapeCharacter, String identifier, String newIdentifier) {
        String nonEscapedReplace = input.replaceAll("(?<!\\\\\\\\)" + escapeCharacter + ")" + Pattern.quote(identifier), newIdentifier);
        return nonEscapedReplace.replaceAll(Pattern.quote(escapeCharacter) + Pattern.quote(identifier), identifier);
    }
}
