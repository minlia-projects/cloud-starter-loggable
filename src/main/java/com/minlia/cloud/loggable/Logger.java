package com.minlia.cloud.loggable;

import java.util.Objects;
import org.slf4j.LoggerFactory;
import org.springframework.boot.logging.LogLevel;

/**
 * Helper method for logger.
 *
 * @author will
 */
public final class Logger {

  private void log(org.slf4j.Logger logger, LogLevel level, String message) {
    Objects.requireNonNull(level, "LogLevel must not be null.");
    switch (level) {
      case TRACE:
        logger.trace(message);
        break;
      case DEBUG:
        logger.debug(message);
        break;
      case INFO:
        logger.info(message);
        break;
      case WARN:
        logger.warn(message);
        break;
      case ERROR:
      case FATAL:
        logger.error(message);
        break;
      default:
        break;
    }
  }

  private void log(org.slf4j.Logger logger, LogLevel level, String message, Throwable err) {
    Objects.requireNonNull(level, "LogLevel must not be null.");
    switch (level) {
      case TRACE:
        logger.trace(message, err);
        break;
      case DEBUG:
        logger.debug(message, err);
        break;
      case INFO:
        logger.info(message, err);
        break;
      case WARN:
        logger.warn(message, err);
        break;
      case ERROR:
      case FATAL:
        logger.error(message, err);
        break;
      default:
        break;
    }
  }

  public void log(LogLevel level, Class<?> clazz, String message) {
    log(LoggerFactory.getLogger(clazz), level, message);
  }

  public void log(LogLevel level, String name, String message) {
    log(LoggerFactory.getLogger(name), level, message);
  }

  public void log(Class<?> clazz, String message, Throwable err) {
    log(LoggerFactory.getLogger(clazz), LogLevel.ERROR, message, err);
  }

  public void log(LogLevel level, String name, String message, Throwable err) {
    log(LoggerFactory.getLogger(name), level, message, err);
  }

  private boolean isEnabled(org.slf4j.Logger logger, LogLevel level) {
    Objects.requireNonNull(level, "LogLevel must not be null.");
    switch (level) {
      case TRACE:
        return logger.isTraceEnabled();
      case DEBUG:
        return logger.isDebugEnabled();
      case INFO:
        return logger.isInfoEnabled();
      case WARN:
        return logger.isWarnEnabled();
      case ERROR:
      case FATAL:
        return logger.isErrorEnabled();
      default:
        throw new IllegalArgumentException("LogLevel must be one of the enabled levels.");
    }
  }

  public boolean isEnabled(LogLevel level, Class<?> clazz) {
    return isEnabled(LoggerFactory.getLogger(clazz), level);
  }

  public boolean isEnabled(LogLevel level, String name) {
    return isEnabled(LoggerFactory.getLogger(name), level);
  }
}
