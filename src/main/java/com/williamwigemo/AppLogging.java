package com.williamwigemo;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AppLogging {
    public static <T> Logger buildLogger(Class<T> cls) {
        Logger logger = Logger.getLogger(cls.getName());

        logger.setUseParentHandlers(false);

        logger.setLevel(Level.FINE);

        Handler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.FINE);

        logger.addHandler(consoleHandler);

        return logger;
    }
}
