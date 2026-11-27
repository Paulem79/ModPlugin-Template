package com.example.template.paper.logger;

import com.example.template.common.wrapper.LoggerWrapper;

import java.util.logging.Logger;

public record PaperLoggerWrapper(Logger logger) implements LoggerWrapper {
    @Override
    public void info(String msg) {
        logger.info(msg);
    }

    @Override
    public void warn(String msg) {
        logger.warning(msg);
    }

    @Override
    public void error(String msg) {
        logger.severe(msg);
    }
}
