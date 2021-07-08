package com.example.demo.job.jdbcBatchItemWriter;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import com.example.demo.domain.Pay;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class JdbcBatchItemWriterJobConfiguration {

	private final JobBuilderFactory jobBuilderFactory;
	private final StepBuilderFactory stepBuilderFactory;
	private final DataSource dataSource; // DataSource DI

	private static final int chunkSize = 10;

	@Bean
	public Job jdbcBatchItemWriterJob() {
		return jobBuilderFactory.get("jdbcBatchItemWriterJob")
			.start(jdbcBatchItemWriterStep())
			.build();
	}

	@Bean
	public Step jdbcBatchItemWriterStep() {
		return stepBuilderFactory.get("jdbcBatchItemWriterStep")
			.<Pay, Pay>chunk(chunkSize)
			.reader(jdbcBatchItemWriterReader())
			.writer(jdbcBatchItemWriter())
			.build();
	}

	@Bean
	public JdbcCursorItemReader<Pay> jdbcBatchItemWriterReader() {
		return new JdbcCursorItemReaderBuilder<Pay>()
			.fetchSize(chunkSize)
			.dataSource(dataSource)
			.rowMapper(new BeanPropertyRowMapper<>(Pay.class))
			.sql("SELECT id, amount, tx_name, tx_date_time FROM pay")
			.name("jdbcBatchItemWriter")
			.build();
	}

	/*
	 * 주의!
	 * JdbcBatchItemWriterBuilder가 아닌 JdbcBatchItemWriter의 설정에서,
	 * JdbcBatchItemWriter의 제네릭 타입은 Reader에서 넘겨주는 값의 타입이다.
	 */
	@Bean // beanMapped()을 사용할때는 필수
	public JdbcBatchItemWriter<Pay> jdbcBatchItemWriter() {
		return new JdbcBatchItemWriterBuilder<Pay>() //여기 제네릭 타입은 Reader에서 넘겨주는 값의 타입
			.dataSource(dataSource)
			.sql("insert into pay2(amount, tx_name, tx_date_time) values (:amount, :txName, :txDateTime)")
			.beanMapped() //POJO 기반으로 Insert SQL의 Values 매핑
			.build();
	}
}