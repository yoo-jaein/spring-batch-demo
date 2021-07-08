package com.example.demo.job.jdbcPagingItemReader;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import com.example.demo.domain.Pay;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class JdbcPagingItemReaderJobConfiguration {

	private final JobBuilderFactory jobBuilderFactory;
	private final StepBuilderFactory stepBuilderFactory;
	private final DataSource dataSource; // DataSource DI

	private static final int chunkSize = 10;

	@Bean
	public Job jdbcPagingItemReaderJob() throws Exception {
		return jobBuilderFactory.get("jdbcPagingItemReaderJob")
			.start(jdbcPagingItemReaderStep())
			.build();
	}

	@Bean
	public Step jdbcPagingItemReaderStep() throws Exception {
		return stepBuilderFactory.get("jdbcPagingItemReaderStep")
			.<Pay, Pay>chunk(chunkSize)
			.reader(jdbcPagingItemReader())
			.writer(jdbcPagingItemWriter())
			.build();
	}

	@Bean
	public JdbcPagingItemReader<Pay> jdbcPagingItemReader() throws Exception {
		Map<String, Object> parameterValues = new HashMap<>();
		parameterValues.put("amount", 2000);

		return new JdbcPagingItemReaderBuilder<Pay>()
			.pageSize(chunkSize)
			.fetchSize(chunkSize)
			.dataSource(dataSource)
			.rowMapper(new BeanPropertyRowMapper<>(Pay.class))
			.queryProvider(createQueryProvider())
			.parameterValues(parameterValues) //쿼리 파라미터 값으로 Map 할당(Using parameterMap)
			.name("jdbcPagingItemReader")
			.build();
	}

	private ItemWriter<Pay> jdbcPagingItemWriter() {
		return list -> {
			for (Pay pay: list) {
				log.info("Current Pay={}", pay);
			}
		};
	}

	/*
	 * PagingItemReader에서는 PagingQueryProvider를 통해 쿼리를 생성한다.
	 * 각 DB에는 Paging을 지원하는 자체적인 전략이 있다. 따라서 Spring Batch에서는 DB에 맞는 전략으로 구현되어야 한다.
	 * Spring Batch는 DB 전략에 해당되는 Provider를 연결해주기 위해, SqlPagingQueryProviderFactoryBean을 통해 DataSource 설정 값을 확인한다.
	 */
	@Bean
	public PagingQueryProvider createQueryProvider() throws Exception {
		SqlPagingQueryProviderFactoryBean queryProvider = new SqlPagingQueryProviderFactoryBean();
		queryProvider.setDataSource(dataSource); // Database에 맞는 PagingQueryProvider를 선택하기 위해
		queryProvider.setSelectClause("id, amount, tx_name, tx_date_time");
		queryProvider.setFromClause("from pay");
		queryProvider.setWhereClause("where amount >= :amount");

		Map<String, Order> sortKeys = new HashMap<>(1);
		sortKeys.put("id", Order.ASCENDING); //정렬

		queryProvider.setSortKeys(sortKeys);

		return queryProvider.getObject();
	}
}