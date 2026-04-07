// Файл: src/client/java/com/multicmd/start/client/parser/rule/impl/LetterRangeRule.java
package com.multicmd.start.client.parser.rule.impl;

import com.multicmd.start.client.parser.CommandParser;
import com.multicmd.start.client.parser.rule.ExpansionRule;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Улучшение алгоритма: Обработка буквенных диапазонов.
 * Пример: /say [a-d] -> /say a, /say b, /say c, /say d
 */
public class LetterRangeRule implements ExpansionRule {
    private static final Pattern PATTERN = Pattern.compile("\\[([a-zA-Z])-([a-zA-Z])\\]");

    @Override
    public boolean matches(String currentCmd) {
        return PATTERN.matcher(currentCmd).find();
    }

    @Override
    public void expand(String currentCmd, List<String> results, int depth, CommandParser context) {
        Matcher matcher = PATTERN.matcher(currentCmd);
        if (matcher.find()) {
            char start = matcher.group(1).charAt(0);
            char end = matcher.group(2).charAt(0);

            // Защита от смешивания регистров (например [a-Z])
            if (Character.isUpperCase(start) != Character.isUpperCase(end)) {
                throw new CommandParser.ParserSecurityException("Буквенный диапазон должен использовать один регистр (a-z или A-Z).");
            }

            int step = start <= end ? 1 : -1;
            String prefix = currentCmd.substring(0, matcher.start());
            String suffix = currentCmd.substring(matcher.end());

            for (char c = start; c != end + (char) step; c += step) {
                context.expandRecursive(prefix + c + suffix, results, depth + 1);
            }
        }
    }
}