// Файл: src/client/java/com/multicmd/start/client/parser/rule/impl/NumericRangeRule.java
package com.multicmd.start.client.parser.rule.impl;

import com.multicmd.start.client.parser.CommandParser;
import com.multicmd.start.client.parser.rule.ExpansionRule;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NumericRangeRule implements ExpansionRule {
    private static final Pattern PATTERN = Pattern.compile("\\[(-?\\d+)-(-?\\d+)\\]");

    @Override
    public boolean matches(String currentCmd) {
        return PATTERN.matcher(currentCmd).find();
    }

    @Override
    public void expand(String currentCmd, List<String> results, int depth, CommandParser context) {
        Matcher matcher = PATTERN.matcher(currentCmd);
        if (matcher.find()) {
            try {
                int start = Integer.parseInt(matcher.group(1));
                int end = Integer.parseInt(matcher.group(2));
                int step = start <= end ? 1 : -1;

                if (Math.abs(start - end) > 1000) {
                    throw new CommandParser.ParserSecurityException("Диапазон чисел превышает безопасный предел в 1000 итераций.");
                }

                for (int i = start; i != end + step; i += step) {
                    String next = currentCmd.substring(0, matcher.start()) + i + currentCmd.substring(matcher.end());
                    context.expandRecursive(next, results, depth + 1);
                }
            } catch (NumberFormatException ex) {
                throw new CommandParser.ParserSecurityException("Критическое переполнение числа в диапазоне.");
            }
        }
    }
}