// Файл: src/client/java/com/multicmd/start/client/parser/rule/ExpansionRule.java
package com.multicmd.start.client.parser.rule;

import com.multicmd.start.client.parser.CommandParser;
import java.util.List;

/**
 *[Архитектура SOLID: Open/Closed Principle]
 * Базовый контракт для всех синтаксических правил.
 * Позволяет добавлять новые паттерны без модификации основного ядра парсера.
 */
public interface ExpansionRule {
    /**
     * @param currentCmd Анализируемая строка.
     * @return true, если строка содержит паттерн, обрабатываемый этим правилом.
     */
    boolean matches(String currentCmd);

    /**
     * @param currentCmd Исходная строка для распаковки.
     * @param results Потокобезопасная или локальная коллекция для сбора результатов.
     * @param depth Текущая глубина рекурсии (защита от StackOverflow).
     * @param context Ссылка на ядро парсера для обработки вложенных паттернов.
     */
    void expand(String currentCmd, List<String> results, int depth, CommandParser context);
}