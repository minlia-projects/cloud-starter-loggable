package com.minlia.cloud.loggable;

import com.minlia.cloud.loggable.annotation.Loggable;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.logging.LogLevel;

/**
 * AspectJ to intercept {@link Loggable} methods or classes.
 *
 * @author will
 */
@Aspect
public class LoggerInterceptor {

  private Logger logger;

  private LoggerMsgFormatter formatter;

  private Set<WarnPoint> warnPoints;
  private ScheduledExecutorService warnService;

  @Autowired
  public LoggerInterceptor(Logger logger, LoggerFormats formats) {
    this.formatter = new LoggerMsgFormatter(formats);
    this.logger = logger;
  }

  @PostConstruct
  protected void construct() {
    warnPoints = new ConcurrentSkipListSet<>();
    warnService = Executors.newSingleThreadScheduledExecutor();
    warnService.scheduleAtFixedRate(() -> {
      for (WarnPoint wp : warnPoints) {
        long duration = System.nanoTime() - wp.getStart();
        if (isOver(duration, wp.getLoggable())) {
          log(LogLevel.WARN, formatter.warnBefore(wp.getPoint(), wp.getLoggable(), duration),
              wp.getPoint(),
              wp.getLoggable());
          warnPoints.remove(wp);
        }
      }
    }, 1L, 1L, TimeUnit.SECONDS);
  }

  @Pointcut("execution(public * *(..))"
      + " && !execution(String *.toString())"
      + " && !execution(int *.hashCode())"
      + " && !execution(boolean *.canEqual(Object))"
      + " && !execution(boolean *.equals(Object))")
  protected void publicMethod() {
  }

  @Pointcut("@annotation(loggable)")
  protected void loggableMethod(Loggable loggable) {
  }

  @Pointcut("@within(loggable)")
  protected void loggableClass(Loggable loggable) {
  }

  @Around(value = "publicMethod() && loggableMethod(loggable)", argNames = "joinPoint,loggable")
  public Object logExecutionMethod(ProceedingJoinPoint joinPoint, Loggable loggable)
      throws Throwable {
    return logMethod(joinPoint, loggable);
  }

  @Around(value = "publicMethod() && loggableClass(loggable) && !loggableMethod(com.minlia.cloud.loggable.annotation.Loggable)", argNames = "joinPoint,loggable")
  public Object logExecutionClass(ProceedingJoinPoint joinPoint, Loggable loggable)
      throws Throwable {
    return logMethod(joinPoint, loggable);
  }

  public Object logMethod(ProceedingJoinPoint joinPoint, Loggable loggable) throws Throwable {
    long start = System.nanoTime();
    WarnPoint warnPoint = null;
    Object returnVal = null;

    if (isLevelEnabled(joinPoint, loggable) && loggable.warnOver() >= 0) {
      warnPoint = new WarnPoint(joinPoint, loggable, start);
      warnPoints.add(warnPoint);
    }

    if (loggable.entered()) {
      log(loggable.value(), formatter.enter(joinPoint, loggable), joinPoint, loggable);
    }

    try {
      returnVal = joinPoint.proceed();

      long nano = System.nanoTime() - start;
      if (isOver(nano, loggable)) {
        log(LogLevel.WARN, formatter.warnAfter(joinPoint, loggable, returnVal, nano), joinPoint,
            loggable);
      } else {
        log(loggable.value(), formatter.after(joinPoint, loggable, returnVal, nano), joinPoint,
            loggable);
      }
      return returnVal;
    } catch (Throwable ex) {
      if (contains(loggable.ignore(), ex)) {
        log(LogLevel.ERROR,
            formatter.error(joinPoint, loggable, returnVal, System.nanoTime() - start, ex),
            joinPoint, loggable);
      } else {
        log(formatter.error(joinPoint, loggable, returnVal, System.nanoTime() - start, ex),
            joinPoint, loggable, ex);
      }
      throw ex;
    } finally {
      if (warnPoint != null) {
        warnPoints.remove(warnPoint);
      }
    }
  }

  private void log(LogLevel level, String message, ProceedingJoinPoint joinPoint,
      Loggable loggable) {
    if (loggable.name().isEmpty()) {
      logger.log(level,
          MethodSignature.class.cast(joinPoint.getSignature()).getMethod().getDeclaringClass(),
          message);
    } else {
      logger.log(level, loggable.name(), message);
    }
  }

  private void log(String message, ProceedingJoinPoint joinPoint, Loggable loggable, Throwable ex) {
    if (loggable.name().isEmpty()) {
      logger.log(
          MethodSignature.class.cast(joinPoint.getSignature()).getMethod().getDeclaringClass(),
          message, ex);
    } else {
      logger.log(LogLevel.ERROR, loggable.name(), message, ex);
    }
  }

  private boolean isLevelEnabled(ProceedingJoinPoint joinPoint, Loggable loggable) {
    return loggable.name().isEmpty()
        ? logger.isEnabled(LogLevel.WARN,
        MethodSignature.class.cast(joinPoint.getSignature()).getMethod().getDeclaringClass())
        : logger.isEnabled(LogLevel.WARN, loggable.name());
  }

  private boolean isOver(long nano, Loggable loggable) {
    return loggable.warnOver() >= 0
        && TimeUnit.NANOSECONDS.toMillis(nano) > loggable.warnUnit().toMillis(loggable.warnOver());
  }

  private boolean contains(Class<? extends Throwable>[] array, Throwable exp) {
    boolean contains = false;
    for (final Class<? extends Throwable> type : array) {
      if (instanceOf(exp.getClass(), type)) {
        contains = true;
        break;
      }
    }
    return contains;
  }

  private boolean instanceOf(Class<?> child, Class<?> parent) {
    boolean instance = child.equals(parent)
        || child.getSuperclass() != null && instanceOf(child.getSuperclass(), parent);
    if (!instance) {
      for (final Class<?> iface : child.getInterfaces()) {
        instance = instanceOf(iface, parent);
        if (instance) {
          break;
        }
      }
    }
    return instance;
  }

  @EqualsAndHashCode(of = "point")
  @AllArgsConstructor
  @Getter
  protected static class WarnPoint implements Comparable<WarnPoint> {

    private ProceedingJoinPoint point;
    private Loggable loggable;
    private long start;

    @Override
    public int compareTo(WarnPoint obj) {
      return Long.compare(obj.getStart(), start);
    }
  }

}
