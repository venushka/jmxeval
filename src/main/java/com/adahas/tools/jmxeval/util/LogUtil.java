package com.adahas.tools.jmxeval.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import com.adahas.tools.jmxeval.App;

/**
 * Utility class to control logging.
 */
public class LogUtil {

  private static final Logger log = Logger.getLogger(LogUtil.class.getName());

  /**
   * Set the logging mode.
   *
   * @param mode Mode to set
   */
  public static void setLogMode(final Mode mode) {
    final LogManager logManager = LogManager.getLogManager();

    try (final InputStream configStream = App.class.getResourceAsStream(mode.logFile)) {
      logManager.readConfiguration(configStream);
    } catch (IOException e) {
      log.log(Level.WARNING, "Could not load log configuration", e);
    }
  }

  /**
   * Logging mode.
   *
   * <ul>
   *   <li>NONE - Used in production, logging to console will make the output unreadable from Nagios.</li>
   *   <li>VERBOSE - Used for identifying issues in the plugin or configuration</li>
   * </ul>
   */
  public enum Mode {
    NONE("/logging.properties"),
    VERBOSE("/logging-debug.properties");

    private final String logFile;

    private Mode(final String logFile) {
      this.logFile = logFile;
    }
  }
}

