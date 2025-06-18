package com.formation.events_batch.config;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.stereotype.Component;

@Component
public class JobExecutionListenerImpl implements JobExecutionListener {

  private static final Logger logger = LoggerFactory.getLogger(JobExecutionListenerImpl.class);

  @Override
  public void beforeJob(JobExecution jobExecution) {
    logger.info("=== DÉBUT DU JOB ===");
    logger.info("Job Name: {}", jobExecution.getJobInstance().getJobName());
    logger.info("Job ID: {}", jobExecution.getJobId());
    logger.info("Start Time: {}", jobExecution.getStartTime());
    logger.info("Job Parameters: {}", jobExecution.getJobParameters());
  }

  @Override
  public void afterJob(JobExecution jobExecution) {
    logger.info("=== FIN DU JOB ===");
    logger.info("Job Name: {}", jobExecution.getJobInstance().getJobName());
    logger.info("Job Status: {}", jobExecution.getStatus());
    logger.info("End Time: {}", jobExecution.getEndTime());

    if (jobExecution.getEndTime() != null && jobExecution.getStartTime() != null) {
      LocalDateTime startTime = jobExecution.getStartTime().toInstant(ZoneOffset.UTC)
          .atZone(ZoneId.systemDefault()).toLocalDateTime();
      LocalDateTime endTime = jobExecution.getEndTime().toInstant(ZoneOffset.UTC)
          .atZone(ZoneId.systemDefault()).toLocalDateTime();
      Duration duration = Duration.between(startTime, endTime);
      logger.info("Duration: {} ms", duration.toMillis());
    } else {
      logger.info("Duration: N/A");
    }

    if (jobExecution.getStatus().isUnsuccessful()) {
      logger.error("Job failed with exit description: {}", jobExecution.getExitStatus().getExitDescription());
    } else {
      logger.info("Job completed successfully!");
    }

    logger.info("Read Count: {}", jobExecution.getStepExecutions().stream()
        .mapToLong(StepExecution::getReadCount).sum());
    logger.info("Write Count: {}", jobExecution.getStepExecutions().stream()
        .mapToLong(StepExecution::getWriteCount).sum());
    logger.info("Skip Count: {}", jobExecution.getStepExecutions().stream()
        .mapToLong(StepExecution::getSkipCount).sum());
  }
}
