package com.example.template.common;

public final class TemplateSettings {
    private TemplateSettings() {
    }

    public static final int TICKS_PER_SECOND = 20;

    /** Number of letters shown while spinning. */
    public static final int ROLL_COUNT = 15;
    /** Ticks between two spinning letters. */
    public static final int ROLL_INTERVAL_TICKS = 2;

    private static volatile TemplateConfig config = TemplateConfig.DEFAULTS;

    public static TemplateConfig config() {
        return config;
    }

    public static void apply(TemplateConfig newConfig) {
        config = newConfig;
    }
}
