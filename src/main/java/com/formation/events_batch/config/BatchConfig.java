package com.formation.events_batch.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import com.formation.events_batch.batch.EventItemWriter;
import com.formation.events_batch.batch.JsonItemReader;
import com.formation.events_batch.batch.OpenAgendaEventProcessor;
import com.formation.events_batch.dto.OpenAgendaDTO;
import com.formation.events_batch.entities.EventEntity;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class BatchConfig {

  private final OpenAgendaEventProcessor openAgendaEventProcessor;
  private final EventItemWriter eventItemWriter;
  private final JobExecutionListenerImpl jobExecutionListenerImpl;

  @Bean
  JsonItemReader jsonItemReader() {
    return new JsonItemReader(new ClassPathResource("data/events.json"));
  }

  @Bean("eventBatchExecutor")
  TaskExecutor taskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(1);
    executor.setMaxPoolSize(1);
    executor.setQueueCapacity(1000);
    executor.setThreadNamePrefix("batch-");
    executor.setWaitForTasksToCompleteOnShutdown(true);
    executor.setAwaitTerminationSeconds(60);

    executor.initialize();

    return executor;
  }

  @Bean
  Step importEventStep(
      JobRepository jobRepository,
      PlatformTransactionManager platformTransactionManager,
      @Qualifier("eventBatchExecutor") TaskExecutor taskExecutor) {
    return new StepBuilder("importEventStep", jobRepository)
        .<OpenAgendaDTO, EventEntity>chunk(100, platformTransactionManager)
        .reader(jsonItemReader())
        .processor(openAgendaEventProcessor)
        .writer(eventItemWriter)
        // .exceptionHandler(new MyCustomExceptionHandler())
        .taskExecutor(taskExecutor)
        .build();
  }

  @Bean
  Job importEventJob(JobRepository jobRepository, Step importEventStep) {
    return new JobBuilder("importEventJob", jobRepository)
        .start(importEventStep)
        .listener(jobExecutionListenerImpl)
        .build();
  }
}
