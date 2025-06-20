package com.formation.events_batch.controllers;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class BatchController {

  @Qualifier("importEventJob")
  private final Job importEventJob;

  @Qualifier("optimizedImportEventJob")
  private final Job optimizedImportEventJob;

  @Qualifier("importUserJob")
  private final Job importUserJob;

  @Qualifier("synchronizedImportUserJob")
  private final Job synchronizedImportUserJob;

  @Qualifier("partitionedImportUserJob")
  private final Job partitionedImportUserJob;

  private final JobExplorer jobExplorer;

  private final ResponseController responseController;

  /*
    
    
    
    
   */

  @GetMapping("/import-events")
  public ResponseEntity<Map<String, Object>> importEvents() {
    return responseController.getResponse(importEventJob);
  }

  @GetMapping("/import-events-fast")
  public ResponseEntity<Map<String, Object>> optimizedImportEvents() {
    return responseController.getResponse(optimizedImportEventJob);
  }

  @GetMapping("/import-users")
  public ResponseEntity<Map<String, Object>> importUsers() {
    return responseController.getResponse(importUserJob);
  }

  @GetMapping("/import-users/synchronized")
  public ResponseEntity<Map<String, Object>> synchronizedImportUsers() {
    return responseController.getResponse(synchronizedImportUserJob);
  }

  @GetMapping("/import-users/partitioned")
  public ResponseEntity<Map<String, Object>> partitionedImportUsers() {
    return responseController.getResponse(partitionedImportUserJob);
  }

  /*
  
  
  
  
  */

  @GetMapping("/job-history/{job}")
  public ResponseEntity<List<Map<String, Object>>> getJobHistory(@PathVariable String job) {
    try {
      List<JobExecution> jobExecutions = jobExplorer.findJobInstancesByJobName(job, 0, 10)
          .stream()
          .flatMap(instance -> jobExplorer.getJobExecutions(instance).stream())
          .toList();

      List<Map<String, Object>> history = jobExecutions.stream()
          .map(execution -> {
            Map<String, Object> jobInfo = new HashMap<>();
            jobInfo.put("id", execution.getJobId());
            jobInfo.put("status", execution.getStatus().toString());
            jobInfo.put("startTime", execution.getStartTime());
            jobInfo.put("endTime", execution.getEndTime());

            ZoneOffset offset = ZoneOffset.UTC;

            if (execution.getEndTime() != null && execution.getStartTime() != null) {
              LocalDateTime startTime = execution.getStartTime().toInstant(offset)
                  .atZone(ZoneId.systemDefault()).toLocalDateTime();
              LocalDateTime endTime = execution.getEndTime().toInstant(offset)
                  .atZone(ZoneId.systemDefault()).toLocalDateTime();
              Duration duration = Duration.between(startTime, endTime);
              jobInfo.put("duration", duration.toMillis());
            } else {
              jobInfo.put("duration", null);
            }

            jobInfo.put("readCount", execution.getStepExecutions().stream()
                .mapToLong(StepExecution::getReadCount).sum());
            jobInfo.put("writeCount", execution.getStepExecutions().stream()
                .mapToLong(StepExecution::getWriteCount).sum());

            return jobInfo;
          })
          .toList();

      return ResponseEntity.ok(history);
    } catch (Exception e) {
      return ResponseEntity.internalServerError().build();
    }
  }

}
