// Файл: src/client/java/com/multicmd/start/client/parser/rule/ExpansionRule.java
package com.multicmd.start.client.parser.rule;

import com.multicmd.start.client.parser.CommandParser;
import java.util.List;

/**
 * Контракт (Интерфейс) для всех синтаксических правил распаковки.
 * Реализация паттерна Chain/Strategy.
 */
public interface ExpansionRule {
    boolean matches(String currentCmd);
    void expand(String currentCmd, List<String> results, int depth, CommandParser context);
}