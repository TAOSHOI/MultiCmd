// Файл: src/client/java/com/multicmd/start/client/parser/CommandParser.java
package com.multicmd.start.client.parser;

import com.multicmd.start.client.parser.rule.ExpansionRule;
import com.multicmd.start.client.parser.rule.impl.NumericRangeRule;
import com.multicmd.start.client.parser.rule.impl.RandomListRule;
import com.multicmd.start.client.parser.rule.impl.StandardListRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Ядро парсинга и лексического анализа синтаксиса.
 * Не содержит зависимостей от Minecraft, что делает его полностью покрываемым Unit-тестами.
 */
public class CommandParser {

    private static final Logger LOGGER = LoggerFactory.getLogger("MultiCmd-Engine");

    // Hard-Limits защиты движка от эксплоитов (Catastrophic Backtracking)
    private static final int MAX_RECURSION_DEPTH = 15;
    private static final int MAX_STRING_LENGTH = 5000;
    private static final int MAX_RESULTS_LIMIT = 1000;

    private final List<ExpansionRule> rules;

    public CommandParser() {
        this.rules = new ArrayList<>();
        this.rules.add(new RandomListRule());
        this.rules.add(new NumericRangeRule());
        this.rules.add(new StandardListRule());
    }

    public static class ParserSecurityException extends RuntimeException {
        public ParserSecurityException(String message) {
            super(message);
        }
    }

    public List<String> parse(String rawCmd, Map<String, String> envVariables,
                              Map<String, String> groups, Map<String, String> macros) {
        if (rawCmd == null || rawCmd.trim().isEmpty()) {
            return new ArrayList<>();
        }

        if (rawCmd.length() > MAX_STRING_LENGTH) {
            throw new ParserSecurityException("Размер строки команды превышает безопасный лимит памяти.");
        }

        // Разрешение Макросов
        if (rawCmd.startsWith("#")) {
            String macroName = rawCmd.substring(1).trim();
            if (macros.containsKey(macroName)) {
                rawCmd = macros.get(macroName);
            } else {
                throw new IllegalArgumentException("Не удалось найти зарегистрированный макрос: #" + macroName);
            }
        }

        // Инъекция переменных среды (%me%, %x%)
        for (Map.Entry<String, String> env : envVariables.entrySet()) {
            rawCmd = rawCmd.replace(env.getKey(), env.getValue());
        }

        // Разрешение Групп (@Builders -> {Steve,Alex})
        for (Map.Entry<String, String> group : groups.entrySet()) {
            String groupRef = "@" + group.getKey();
            if (rawCmd.contains(groupRef)) {
                rawCmd = rawCmd.replace(groupRef, "{" + group.getValue() + "}");
            }
        }

        List<String> results = new ArrayList<>();
        expandRecursive(rawCmd, results, 0);

        if (results.size() > MAX_RESULTS_LIMIT) {
            throw new ParserSecurityException("Защита от взрыва комбинаторики: Пул команд > " + MAX_RESULTS_LIMIT);
        }

        return results;
    }

    public void expandRecursive(String currentCmd, List<String> results, int depth) {
        if (depth > MAX_RECURSION_DEPTH) {
            LOGGER.warn("Сработала защита StackOverflow Shield (Глубина: {}). Остановка ветви.", depth);
            results.add(currentCmd);
            return;
        }

        for (ExpansionRule rule : rules) {
            if (rule.matches(currentCmd)) {
                rule.expand(currentCmd, results, depth, this);
                return;
            }
        }

        results.add(currentCmd);
    }
}