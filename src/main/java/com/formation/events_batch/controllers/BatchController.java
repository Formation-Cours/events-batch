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

  private final JobLauncher jobLauncher;

  @Qualifier("importEventJob")
  private final Job importEventJob;

  @Qualifier("optimizedImportEventJob")
  private final Job optimizedImportEventJob;

  @Qualifier("importUserJob")
  private final Job importUserJob;

  // public BatchController(JobLauncher jobLauncher,
  // @Qualifier("importEventJob") Job importEventJob,
  // @Qualifier("optimizedImportEventJob") Job optimizedImportEventJob,
  // @Qualifier("importUserJob") Job importUserJob,
  // JobExplorer jobExplorer) {
  // this.jobLauncher = jobLauncher;
  // this.importEventJob = importEventJob;
  // this.optimizedImportEventJob = optimizedImportEventJob;
  // this.importUserJob = importUserJob;
  // this.jobExplorer = jobExplorer;
  // }

  private final JobExplorer jobExplorer;

  /*
    
    
    
    
   */

  @GetMapping("/import-events")
  public ResponseEntity<Map<String, Object>> importEvents() {
    return getResponse(importEventJob);
  }

  @GetMapping("/import-events-fast")
  public ResponseEntity<Map<String, Object>> optimizedImportEvents() {
    return getResponse(optimizedImportEventJob);
  }

  @GetMapping("/import-users")
  public ResponseEntity<Map<String, Object>> importUsers() {
    return getResponse(importUserJob);
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

  private ResponseEntity<Map<String, Object>> getResponse(Job job) {
    Map<String, Object> response = new HashMap<>();

    try {
      JobParameters jobParameters = new JobParametersBuilder().addLong("startAt", System.currentTimeMillis())
          .toJobParameters();

      JobExecution jobExecution = jobLauncher.run(job, jobParameters);
      if (jobExecution.getStatus() == BatchStatus.FAILED) {
        response.put("Status", "ERROR");
        response.put("message", "Failed to start import job: " + jobExecution.getJobInstance());
        return ResponseEntity.internalServerError().body(response);
      }

      response.put("Status", "SUCCESS");
      response.put("message", "Import job started successfully.");
      return ResponseEntity.ok(response);

    } catch (Exception e) {
      response.put("Status", "ERROR");
      response.put("message", "Failed to start import job: " + e.getMessage());
      return ResponseEntity.internalServerError().body(response);
    }
  }

}
