// Файл: src/client/java/com/multicmd/start/client/parser/rule/impl/NumericRangeRule.java
package com.multicmd.start.client.parser.rule.impl;

import com.multicmd.start.client.parser.CommandParser;
import com.multicmd.start.client.parser.rule.ExpansionRule;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Алгоритм обработки числовых диапазонов.
 * Поддерживает как прямую[1-5], так и обратную [5-1] генерацию.
 */
public class NumericRangeRule implements ExpansionRule {
    // Регулярное выражение с предкомпиляцией (оптимизация CPU)
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

                // Защита алгоритма от OutOfMemoryError (OOM)
                if (Math.abs(start - end) > 2000) {
                    throw new CommandParser.ParserSecurityException("Отказ в обслуживании: Числовой диапазон превышает безопасный предел (2000 итераций).");
                }

                String prefix = currentCmd.substring(0, matcher.start());
                String suffix = currentCmd.substring(matcher.end());

                for (int i = start; i != end + step; i += step) {
                    // Используем конкатенацию для быстрой сборки дочерней ветви
                    context.expandRecursive(prefix + i + suffix, results, depth + 1);
                }
            } catch (NumberFormatException ex) {
                throw new CommandParser.ParserSecurityException("Критическое переполнение разрядности числа в диапазоне.");
            }
        }
    }
}