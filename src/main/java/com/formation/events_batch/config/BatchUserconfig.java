package com.formation.events_batch.config;

import java.io.IOException;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import com.formation.events_batch.batch.users.UserItemProcessor;
import com.formation.events_batch.batch.users.UserItemReader;
import com.formation.events_batch.batch.users.UserItemWriter;
import com.formation.events_batch.dto.UserCsvDTO;
import com.formation.events_batch.entities.UserEntity;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class BatchUserconfig {

  private final UserItemProcessor userItemProcessor;
  private final UserItemWriter userItemWriter;
  private final JobExecutionListenerImpl jobExecutionListenerImpl;

  @Bean
  FlatFileItemReader<UserCsvDTO> userItemReader() {
    return UserItemReader.read(new ClassPathResource("data/users.csv"));
  }

  @Bean
  TaskExecutor userTaskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(4);
    executor.setMaxPoolSize(8);
    executor.setQueueCapacity(100);
    executor.setThreadNamePrefix("user-batch-");
    executor.setWaitForTasksToCompleteOnShutdown(true);
    executor.setAwaitTerminationSeconds(60);
    executor.initialize();
    return executor;
  }

  @Bean
  Step userItemStep(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager) {
    return new StepBuilder("importUserStep", jobRepository)
        .<UserCsvDTO, UserEntity>chunk(100, platformTransactionManager)
        .reader(userItemReader())
        .processor(userItemProcessor)
        .writer(userItemWriter)
        .taskExecutor(userTaskExecutor())
        .faultTolerant()
        .skip(DataIntegrityViolationException.class)
        .skip(ConstraintViolationException.class)
        // .retry(DataIntegrityViolationException.class)
        // .retryLimit(3)
        .noSkip(IOException.class)
        .build();
  }

  @Bean
  Job importUserJob(JobRepository jobRepository, Step userItemStep) {
    return new JobBuilder("importUserJob", jobRepository)
        .start(userItemStep)
        .listener(jobExecutionListenerImpl)
        .build();

  }

}
