// Файл: src/client/java/com/multicmd/start/client/parser/rule/impl/StandardListRule.java
package com.multicmd.start.client.parser.rule.impl;

import com.multicmd.start.client.parser.CommandParser;
import com.multicmd.start.client.parser.rule.ExpansionRule;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StandardListRule implements ExpansionRule {
    private static final Pattern PATTERN = Pattern.compile("\\{([^}]+)\\}");

    @Override
    public boolean matches(String currentCmd) {
        return PATTERN.matcher(currentCmd).find();
    }

    @Override
    public void expand(String currentCmd, List<String> results, int depth, CommandParser context) {
        Matcher matcher = PATTERN.matcher(currentCmd);
        if (matcher.find()) {
            String[] items = matcher.group(1).split(",");
            for (String item : items) {
                String next = currentCmd.substring(0, matcher.start()) + item.trim() + currentCmd.substring(matcher.end());
                context.expandRecursive(next, results, depth + 1);
            }
        }
    }
}