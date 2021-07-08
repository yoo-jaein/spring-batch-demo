package com.example.demo.job.customItemWriter;

import javax.persistence.EntityManagerFactory;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
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
public class CustomItemWriterJobConfiguration {

	private final JobBuilderFactory jobBuilderFactory;
	private final StepBuilderFactory stepBuilderFactory;
	private final EntityManagerFactory entityManagerFactory;

	private static final int chunkSize = 10;

	/*
	 * Custom Writer를 구현해야 하는 경우
	 * 1. Reader에서 읽어온 데이터를 RestTemplate으로 외부 API로 전달해야할때
	 * 2. 임시저장을 하고 비교하기 위해 싱글톤 객체에 값을 넣어야 할때
	 * 3. 여러 Entity를 동시에 save 해야할때
	 */

	@Bean
	public Job customItemWriterJob() {
		return jobBuilderFactory.get("customItemWriterJob")
			.start(customItemWriterStep())
			.build();
	}

	@Bean
	public Step customItemWriterStep() {
		return stepBuilderFactory.get("customItemWriterStep")
			.<Pay, Pay2>chunk(chunkSize)
			.reader(customItemWriterReader())
			.processor(customItemWriterProcessor())
			.writer(customItemWriter())
			.build();
	}

	@Bean
	public JpaPagingItemReader<Pay> customItemWriterReader() {
		return new JpaPagingItemReaderBuilder<Pay>()
			.name("customItemWriterReader")
			.entityManagerFactory(entityManagerFactory)
			.pageSize(chunkSize)
			.queryString("SELECT p FROM Pay p")
			.build();
	}

	@Bean
	public ItemProcessor<Pay, Pay2> customItemWriterProcessor() {
		return pay -> new Pay2(pay.getAmount(), pay.getTxName(), pay.getTxDateTime().toString());
	}

	@Bean
	public ItemWriter<Pay2> customItemWriter() {
		/*
		return new ItemWriter<Pay2>() {
            @Override //write()만 오버라이드하면 구현체 생성은 끝난다.
            public void write(List<? extends Pay2> items) throws Exception {
                for (Pay2 item : items) {
                    System.out.println(item);
                }
            }
        };
		 */

		return items -> { //Java 8 lambda
			for (Pay2 item : items) {
				System.out.println(item); //processor에서 넘어온 데이터를 출력하는 Writer
			}
		};
	}
}