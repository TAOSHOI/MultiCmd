// Файл: src/test/java/com/multicmd/start/ParserOOPTest.java
package com.multicmd.start;

import com.multicmd.start.client.parser.CommandParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CI/CD Модульные тесты для движка компиляции
 */
public class ParserOOPTest {

    private CommandParser parser;
    private Map<String, String> envVars;
    private Map<String, String> groups;
    private Map<String, String> macros;

    @BeforeEach
    public void setup() {
        parser = new CommandParser();
        envVars = new HashMap<>();
        groups = new HashMap<>();
        macros = new HashMap<>();

        envVars.put("%me%", "TAOSHOI");
        envVars.put("%x%", "100");
        envVars.put("%y%", "64");
        envVars.put("%z%", "-100");
    }

    @Test
    public void testEmptyInput() {
        Assertions.assertTrue(parser.parse("", envVars, groups, macros).isEmpty());
    }

    @Test
    public void testStrategyNumericAscending() {
        List<String> result = parser.parse("say[1-3]", envVars, groups, macros);
        Assertions.assertEquals(3, result.size());
        Assertions.assertEquals("say 1", result.get(0));
    }

    @Test
    public void testStrategyRandomList() {
        List<String> result = parser.parse("give ?{Steve,Alex} apple", envVars, groups, macros);
        Assertions.assertEquals(1, result.size());
    }

    @Test
    public void testEnvironmentVariablesInjection() {
        List<String> result = parser.parse("tp %me% %x% %y% %z%", envVars, groups, macros);
        Assertions.assertEquals("tp TAOSHOI 100 64 -100", result.get(0));
    }

    @Test
    public void testMacroPreprocessing() {
        macros.put("claim", "rg addmember Base_[1-2] @Builders");
        groups.put("Builders", "Jeb,Notch");

        List<String> result = parser.parse("#claim", envVars, groups, macros);
        Assertions.assertEquals(4, result.size());
    }

    @Test
    public void testCombinatorialExplosionProtection() {
        Assertions.assertThrows(CommandParser.ParserSecurityException.class, () -> {
            parser.parse("test[1-500] [1-5]", envVars, groups, macros); // 2500 комбинаций > лимита 1000
        });
    }
}