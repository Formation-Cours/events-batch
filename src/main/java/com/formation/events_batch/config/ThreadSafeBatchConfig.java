package com.formation.events_batch.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import com.formation.events_batch.batch.ThreadSafeJsonItemReader;
import com.formation.events_batch.dto.OpenAgendaDTO;
import com.formation.events_batch.entities.EventEntity;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class ThreadSafeBatchConfig {

  @Bean
  ThreadSafeJsonItemReader optimizedJsonItemReader() {
    return new ThreadSafeJsonItemReader(new ClassPathResource("data/events.json"));
  }

  @Bean("optimizedTaskExecutor")
  TaskExecutor optimizedTaskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(4);
    executor.setMaxPoolSize(8);
    executor.setQueueCapacity(100);
    executor.setThreadNamePrefix("batch-");
    executor.setWaitForTasksToCompleteOnShutdown(true);
    executor.setAwaitTerminationSeconds(60);

    executor.initialize();

    return executor;
  }

  @Bean
  Step optimizedImportEventStep(
      JobRepository jobRepository,
      PlatformTransactionManager platformTransactionManager,
      ItemProcessor<OpenAgendaDTO, EventEntity> openAgendaEventProcessor,
      ItemWriter<EventEntity> writer,
      @Qualifier("optimizedTaskExecutor") TaskExecutor taskExecutor) {
    return new StepBuilder("optimizedImportEventStep", jobRepository)
        .<OpenAgendaDTO, EventEntity>chunk(100, platformTransactionManager)
        .reader(optimizedJsonItemReader())
        .processor(openAgendaEventProcessor)
        .writer(writer)
        .taskExecutor(taskExecutor)
        .build();
  }

  @Bean
  Job optimizedImportEventJob(JobRepository jobRepository, Step optimizedImportEventStep,
      JobExecutionListener jobExecutionListenerImpl) {
    return new JobBuilder("optimizedImportEventJob", jobRepository)
        .start(optimizedImportEventStep)
        .listener(jobExecutionListenerImpl)
        .build();
  }
}
