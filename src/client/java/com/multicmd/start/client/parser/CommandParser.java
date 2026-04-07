// Файл: src/client/java/com/multicmd/start/client/parser/CommandParser.java
package com.multicmd.start.client.parser;

import com.multicmd.start.client.parser.rule.ExpansionRule;
import com.multicmd.start.client.parser.rule.impl.LetterRangeRule;
import com.multicmd.start.client.parser.rule.impl.NumericRangeRule;
import com.multicmd.start.client.parser.rule.impl.RandomListRule;
import com.multicmd.start.client.parser.rule.impl.StandardListRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Ядро синтаксического анализа и генерации AST-эквивалента.
 * Отвязано от потоков Minecraft для возможности параллельных вычислений.
 */
public class CommandParser {

    private static final Logger LOGGER = LoggerFactory.getLogger("MultiCmd-Parser");

    // Инженерные лимиты безопасности
    private static final int MAX_RECURSION_DEPTH = 15;
    private static final int MAX_STRING_LENGTH = 5000;
    private static final int MAX_RESULTS_LIMIT = 2500; // Повышено для мощных ПК

    private final List<ExpansionRule> rules;

    public CommandParser() {
        this.rules = new ArrayList<>();
        // ПРИОРИТЕТ ВАЖЕН! Специфичные правила применяются первыми.
        this.rules.add(new RandomListRule());
        this.rules.add(new NumericRangeRule());
        this.rules.add(new LetterRangeRule()); // Интегрировано новое буквенное правило
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
            throw new ParserSecurityException("Строка команды слишком велика. Сработала защита буфера.");
        }

        // 1. Препроцессинг: Алиасы макросов
        if (rawCmd.startsWith("#")) {
            String macroName = rawCmd.substring(1).trim();
            if (macros.containsKey(macroName)) {
                rawCmd = macros.get(macroName);
            } else {
                throw new IllegalArgumentException("Ошибка компиляции: Макрос #" + macroName + " не зарегистрирован.");
            }
        }

        // 2. Препроцессинг: Переменные среды окружения
        for (Map.Entry<String, String> env : envVariables.entrySet()) {
            rawCmd = rawCmd.replace(env.getKey(), env.getValue());
        }

        // 3. Препроцессинг: Распаковка ссылок на группы
        for (Map.Entry<String, String> group : groups.entrySet()) {
            String groupRef = "@" + group.getKey();
            if (rawCmd.contains(groupRef)) {
                rawCmd = rawCmd.replace(groupRef, "{" + group.getValue() + "}");
            }
        }

        // 4. Глубокий рекурсивный анализ
        List<String> results = new ArrayList<>();
        expandRecursive(rawCmd, results, 0);

        if (results.size() > MAX_RESULTS_LIMIT) {
            throw new ParserSecurityException("Блокировка комбинаторного взрыва. Сгенерировано " + results.size() +
                    " команд (Максимум: " + MAX_RESULTS_LIMIT + ").");
        }

        return results;
    }

    public void expandRecursive(String currentCmd, List<String> results, int depth) {
        if (depth > MAX_RECURSION_DEPTH) {
            LOGGER.warn("Защита StackOverflow Shield (Уровень: {}). Ветвь обрезана.", depth);
            results.add(currentCmd);
            return;
        }

        for (ExpansionRule rule : rules) {
            if (rule.matches(currentCmd)) {
                rule.expand(currentCmd, results, depth, this);
                return;
            }
        }

        // Атомарная ветвь достигнута
        results.add(currentCmd);
    }
}