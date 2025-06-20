package com.formation.events_batch.controllers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ResponseController {

  private final JobLauncher jobLauncher;

  public ResponseEntity<Map<String, Object>> getResponse(Job job) {
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
