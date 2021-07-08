package com.example.demo.job.jpaItemWriter;

import javax.persistence.EntityManagerFactory;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.demo.domain.Pay;
import com.example.demo.domain.Pay2;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
//@Configuration
public class JpaItemWriterJobConfiguration {

	private final JobBuilderFactory jobBuilderFactory;
	private final StepBuilderFactory stepBuilderFactory;
	private final EntityManagerFactory entityManagerFactory;

	private static final int chunkSize = 10;

	@Bean
	public Job jpaItemWriterJob() {
		return jobBuilderFactory.get("jpaItemWriterJob")
			.start(jpaItemWriterStep())
			.build();
	}

	@Bean
	public Step jpaItemWriterStep() {
		return stepBuilderFactory.get("jpaItemWriterStep")
			.<Pay, Pay2>chunk(chunkSize)
			.reader(jpaItemWriterReader())
			.processor(jpaItemProcessor())
			.writer(jpaItemWriter())
			.build();
	}

	@Bean
	public JpaPagingItemReader<Pay> jpaItemWriterReader() {
		return new JpaPagingItemReaderBuilder<Pay>()
			.name("jpaItemWriterReader")
			.entityManagerFactory(entityManagerFactory) //JPA를 사용하기 떄문에 영속성 관리를 위해 EntityManager를 할당해야 한다.
			.pageSize(chunkSize)
			.queryString("SELECT p FROM Pay p")
			.build();
	}

	@Bean
	public ItemProcessor<Pay, Pay2> jpaItemProcessor() {
		return pay -> new Pay2(pay.getAmount(), pay.getTxName(), pay.getTxDateTime().toString());
	}

	/*
	 * JpaItemWriter는 넘어온 Entity를 DB에 반영한다.
	 * 그래서 반드시 Entity 클래스를 제네릭 타입으로 받아야만 한다.
	 * JdbcBatchItemWriter는 DTO 클래스를 받더라도 sql로 지정된 쿼리가 실행되어 문제가 없지만,
	 * JpaItemWriter는 넘어온 아이템을 그대로 entityManager.merge()로 테이블에 반영하기 때문이다.
	 */
	@Bean
	public JpaItemWriter<Pay2> jpaItemWriter() {
		JpaItemWriter<Pay2> jpaItemWriter = new JpaItemWriter<>(); //Entity 클래스
		jpaItemWriter.setEntityManagerFactory(entityManagerFactory);
		return jpaItemWriter;
	}
}
