package com.example.demo.job;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class StepNextConditionalJobConfiguration {

	private final JobBuilderFactory jobBuilderFactory;
	private final StepBuilderFactory stepBuilderFactory;

	/*
	 * step1 실패 시나리오: step1 -> step3
	 * step1 성공 시나리오: step1 -> step2 -> step3
	 */
	@Bean
	public Job stepNextConditionalJob() {
		return jobBuilderFactory.get("stepNextConditionalJob")
			.start(conditionalJobStep1()) 
				.on("FAILED") //FAILED일 경우 
				.to(conditionalJobStep3()) //step3으로 이동
				.on("*") //step3의 결과와 관계 없이 
				.end() //step3으로 이동하면 flow 종료
			.from(conditionalJobStep1()) //step1로부터
				.on("*") //FAILED 외에 모든 경우
				.to(conditionalJobStep2()) //step2로 이동
				.next(conditionalJobStep3()) //step2가 정상 종료되면 step3으로 이동
				.on("*") //step3의 결과와 관계 없이
				.end() //step3으로 이동하면 flow 종료
			.end() //job 종료
			.build();
	}

	@Bean
	public Step conditionalJobStep1() {
		return stepBuilderFactory.get("step1")
			.tasklet((contribution, chunkContext) -> {
				log.info(">>>>> This is stepNextConditionalJob Step1");

				/*
				 * on이 캐치하는 상태 값은 Step의 실행 후 상태인 ExitStatus이다.
				 * 그래서 분기 처리를 위해 ExitStatus를 FAILED로 조정한다.
				 * 해당 status를 보고 flow가 진행된다.
				 */
				//contribution.setExitStatus(ExitStatus.FAILED);

				return RepeatStatus.FINISHED;
			})
			.build();
	}

	@Bean
	public Step conditionalJobStep2() {
		return stepBuilderFactory.get("conditionalJobStep2")
			.tasklet((contribution, chunkContext) -> {
				log.info(">>>>> This is stepNextConditionalJob Step2");
				return RepeatStatus.FINISHED;
			})
			.build();
	}

	@Bean
	public Step conditionalJobStep3() {
		return stepBuilderFactory.get("conditionalJobStep3")
			.tasklet((contribution, chunkContext) -> {
				log.info(">>>>> This is stepNextConditionalJob Step3");
				return RepeatStatus.FINISHED;
			})
			.build();
	}
}
