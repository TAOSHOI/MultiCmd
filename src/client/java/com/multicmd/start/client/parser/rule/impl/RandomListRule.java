// Файл: src/client/java/com/multicmd/start/client/parser/rule/impl/RandomListRule.java
package com.multicmd.start.client.parser.rule.impl;

import com.multicmd.start.client.parser.CommandParser;
import com.multicmd.start.client.parser.rule.ExpansionRule;

import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RandomListRule implements ExpansionRule {
    private static final Pattern PATTERN = Pattern.compile("\\?\\{([^}]+)\\}");
    private static final Random RANDOM = new Random();

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
                String chosen = items[RANDOM.nextInt(items.length)].trim();
                String next = currentCmd.substring(0, matcher.start()) + chosen + currentCmd.substring(matcher.end());
                context.expandRecursive(next, results, depth + 1);
            }
        }
    }
}