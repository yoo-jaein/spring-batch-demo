package com.example.demo.job.simple;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j //Log 사용을 위한 Lombok 어노테이션
@RequiredArgsConstructor //생성자 DI를 위한 Lombok 어노테이션
@Configuration //Spring Batch의 모든 Job은 @Configuration으로 등록해서 사용한다
public class SimpleJobConfiguration {

	private final JobBuilderFactory jobBuilderFactory; //생성자 DI
	private final StepBuilderFactory stepBuilderFactory; //생성자 DI

	@Bean
	public Job simpleJob() {
		return jobBuilderFactory.get("simpleJob")
			.start(simpleStep1(null))
			.next(simpleStep2(null))
			.build();
	}

	/*
	 * Step은 실제 Batch 작업을 수행하는 역할
	 * jobParameter는 SpringBatch가 실행될 때 외부에서 받을 수 있는 파라미터
	 */
	@Bean
	@JobScope
	public Step simpleStep1(@Value("#{jobParameters[requestDate]}") String requestDate) {
		return stepBuilderFactory.get("simpleStep1")
			.tasklet((contribution, chunkContext) -> {
				log.info(">>>>> This is Step1");
				log.info(">>>>> requestDate = {}", requestDate);
				return RepeatStatus.FINISHED;
			})
			.build();
	}

	@Bean
	@JobScope
	public Step simpleStep2(@Value("#{jobParameters[requestDate]}") String requestDate) {
		return stepBuilderFactory.get("simpleStep2")
			.tasklet((contribution, chunkContext) -> {
				log.info(">>>>> This is Step2");
				log.info(">>>>> requestDate = {}", requestDate);
				return RepeatStatus.FINISHED;
			})
			.build();
	}
}
