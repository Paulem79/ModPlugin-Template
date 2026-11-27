package com.example.template.common.scheduler;

import com.example.template.common.TemplateConfig;
import com.example.template.common.TemplateSettings;

/** What the players are told alongside a letter. */
public enum LetterDisplay {
    SPINNING,
    FINAL;

    public String subtitle() {
        TemplateConfig config = TemplateSettings.config();
        return switch (this) {
            case SPINNING -> config.spinningText();
            case FINAL -> config.finalText()
                    .replace(TemplateConfig.SECONDS_PLACEHOLDER, String.valueOf(config.destructionDelaySeconds()));
        };
    }
}
