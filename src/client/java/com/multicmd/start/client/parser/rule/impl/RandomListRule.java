// Файл: src/client/java/com/multicmd/start/client/parser/rule/impl/RandomListRule.java
package com.multicmd.start.client.parser.rule.impl;

import com.multicmd.start.client.parser.CommandParser;
import com.multicmd.start.client.parser.rule.ExpansionRule;

import java.security.SecureRandom;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Алгоритм случайного выбора (Рулетка).
 * Улучшено: Использование SecureRandom для криптографически стойкой энтропии.
 */
public class RandomListRule implements ExpansionRule {
    private static final Pattern PATTERN = Pattern.compile("\\?\\{([^}]+)\\}");
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Override
    public boolean matches(String currentCmd) {
        return PATTERN.matcher(currentCmd).find();
    }

    @Override
    public void expand(String currentCmd, List<String> results, int depth, CommandParser context) {
        Matcher matcher = PATTERN.matcher(currentCmd);
        if (matcher.find()) {
            String[] items = matcher.group(1).split(",");
            if (items.length > 0) {
                // Выбор одного случайного элемента с отсечением лишних пробелов
                String chosen = items[SECURE_RANDOM.nextInt(items.length)].trim();

                String next = currentCmd.substring(0, matcher.start()) + chosen + currentCmd.substring(matcher.end());
                context.expandRecursive(next, results, depth + 1);
            }
        }
    }
}