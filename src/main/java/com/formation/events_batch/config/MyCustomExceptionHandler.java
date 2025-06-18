package com.formation.events_batch.config;

import java.io.FileNotFoundException;

import org.springframework.batch.repeat.RepeatContext;
import org.springframework.batch.repeat.exception.ExceptionHandler;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MyCustomExceptionHandler implements ExceptionHandler {

  @Override
  public void handleException(RepeatContext context, Throwable throwable) throws Throwable {
    if (throwable instanceof FileNotFoundException) {
      log.error("file error");
    } else {
      throw throwable;
    }
  }

}
