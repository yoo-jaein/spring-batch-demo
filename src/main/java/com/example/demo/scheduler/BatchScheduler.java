package com.example.demo.scheduler;

import java.time.LocalDateTime;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchScheduler {

	private final Job deciderJob;
	private final JobLauncher jobLauncher;

	//5초마다 실행
	@Scheduled(fixedDelay = 5 * 1000L)
	public void executeJob() {
		try {
			jobLauncher.run(
				deciderJob,
				new JobParametersBuilder()
				.addString("datetime", LocalDateTime.now().toString())
				.toJobParameters() //jobParamter 설정
			);
		} catch (JobExecutionException e) {
			log.error(e.getMessage());
			e.printStackTrace();
		}
	}
}
