package com.example.demo.job.jdbcCursorItemReader;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
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
public class JdbcCursorItemReaderJobConfiguration {

	/*
	 * 주의!
	 * 1. Cursor는 하나의 커넥션을 배치가 끝날 때까지 사용하기 때문에, Timeout을 충분히 큰 값으로 설정해야 한다.
	 * 2. 그래서 배치 수행 시간이 오래 걸리는 경우에는 CursorItemReader보다 PagingItemReader를 사용하는게 낫다.
	 * 3. Jpa에는 CursorItemReader가 없다.
	 */

	private final JobBuilderFactory jobBuilderFactory;
	private final StepBuilderFactory stepBuilderFactory;
	private final DataSource dataSource; // DataSource DI

	private static final int chunkSize = 10;

	@Bean
	public Job jdbcCursorItemReaderJob() {
		return jobBuilderFactory.get("jdbcCursorItemReaderJob")
			.start(jdbcCursorItemReaderStep())
			.build();
	}

	@Bean
	public Step jdbcCursorItemReaderStep() {
		return stepBuilderFactory.get("jdbcCursorItemReaderStep")
			.<Pay, Pay>chunk(chunkSize) //첫 번째 Pay는 Reader에서 반환할 타입, 두 번째 Pay는 Writer에 파라미터로 넘어갈 타입
			.reader(jdbcCursorItemReader())
			.writer(jdbcCursorItemWriter())
			.build();
	}

	@Bean
	public JdbcCursorItemReader<Pay> jdbcCursorItemReader() {
		return new JdbcCursorItemReaderBuilder<Pay>()
			.fetchSize(chunkSize) //DB에서 한 번에 가져올 데이터의 양
			.dataSource(dataSource) //DB 접근할 때 사용할 DataSource 객체 할당
			.rowMapper(new BeanPropertyRowMapper<>(Pay.class)) //쿼리 결과 매퍼. BeanPropertyRowMapper는 스프링 공식 지원함
			.sql("SELECT id, amount, tx_name, tx_date_time FROM pay") //Reader로 사용할 쿼리문
			.name("jdbcCursorItemReader") //Reader의 이름. Bean의 이름이 아니라 Spring Batch의 ExecutionContext에 저장되는 이름
			.build();
	}

	private ItemWriter<Pay> jdbcCursorItemWriter() {
		return list -> {
			for (Pay pay: list) {
				log.info("Current Pay={}", pay);
			}
		};
	}
}
