package com.formation.events_batch.controllers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class BatchController {

  private final JobLauncher jobLauncher;

  @Qualifier("importEventJob")
  private final Job importEventJob;

  @Qualifier("optimizedTaskExecutor")
  private final Job optimizedImportEventJob;

  @GetMapping("/import-events")
  public ResponseEntity<Map<String, Object>> importEvents() {
    Map<String, Object> response = new HashMap<>();

    try {
      JobParameters jobParameters = new JobParametersBuilder().addLong("startAt", System.currentTimeMillis())
          .toJobParameters();

      JobExecution jobExecution = jobLauncher.run(importEventJob, jobParameters);
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

  @GetMapping("/import-events-fast")
  public ResponseEntity<Map<String, Object>> optimizedImportEvents() {
    Map<String, Object> response = new HashMap<>();

    try {
      JobParameters jobParameters = new JobParametersBuilder().addLong("startAt", System.currentTimeMillis())
          .toJobParameters();

      JobExecution jobExecution = jobLauncher.run(optimizedImportEventJob, jobParameters);
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
