package com.minlia.cloud.loggable.starter.configuration;

import com.minlia.cloud.loggable.Logger;
import com.minlia.cloud.loggable.LoggerFormats;
import com.minlia.cloud.loggable.LoggerInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StopWatch;

/**
 * @author will
 */
@Configuration
@Slf4j
@ConditionalOnProperty(prefix = "system.mvc.loggable", name = "enabled", havingValue = "true")
public class LoggableAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean
  public Logger logger() {
    return new Logger();
  }

  @Bean
  @ConditionalOnMissingBean
  public LoggerInterceptor loggerInterceptor() {
    return new LoggerInterceptor(logger(), loggerFormats());
  }

  @Bean
  @ConditionalOnMissingBean
  public LoggerFormats loggerFormats() {
    log.debug("Starting Minlia Loggable");
    StopWatch watch = new StopWatch();
    watch.start();

    LoggerFormats loggerFormats = LoggerFormats.builder()
        .enter("#${method.name}(${method.args}): "
            + "entered")
        .warnBefore("#${method.name}(${method.args}): "
            + "in ${method.duration} and still running (max ${method.warn.duration})")
        .warnAfter("#${method.name}(${method.args}): "
            + "${method.result} in ${method.duration} (max ${method.warn.duration})")
        .after("#${method.name}(${method.args}): "
            + "${method.result} in ${method.duration}")
        .error("#${method.name}(${method.args}): "
            + "thrown ${error.class.name}(${error.message}) "
            + "from ${error.source.class.name}[${error.source.line}] in ${method.duration}")
        .build();
    watch.stop();
    log.debug("Started Minlia Loggable in {} ms", watch.getTotalTimeMillis());
    return loggerFormats;
  }
}