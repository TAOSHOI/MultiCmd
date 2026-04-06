// Файл: src/client/java/com/multicmd/start/client/integration/ModMenuIntegration.java
package com.multicmd.start.client.integration;

import com.multicmd.start.client.gui.MultiCmdScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/**
 * Интеграция с ModMenu для возможности открытия настроек из списка модов.
 */
public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return MultiCmdScreen::new;
    }
}