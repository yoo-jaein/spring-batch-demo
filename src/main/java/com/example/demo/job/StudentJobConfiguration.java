package com.example.demo.job;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.PathResource;

import com.example.demo.domain.Student;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class StudentJobConfiguration {

	private final JobBuilderFactory jobBuilderFactory;
	private final StepBuilderFactory stepBuilderFactory;
	private final DataSource dataSource; // DataSource DI

	private static final int chunkSize = 10;

	@Bean
	public Job job() {
		return jobBuilderFactory.get("fileToDatabaseJob")
			.start(step())
			.build();
	}

	@Bean
	public Step step() {
		return stepBuilderFactory.get("step")
			.<Student, Student> chunk(chunkSize)
			.reader(flatFileItemReader())
			.processor(processor())
			.writer(writer())
			.build();
	}

	@Bean
	public FlatFileItemReader<Student> flatFileItemReader() {
		return new FlatFileItemReaderBuilder<Student>()
			.name("flatFileItemReader")
			.resource(new PathResource("C:\\Users\\yoojaein\\Desktop\\batch-demo\\sample.txt"))
			.delimited()
			.delimiter(" ")
			.names(new String[] {"student_name", "student_id", "korean", "english", "math"})
			.fieldSetMapper(fieldSet -> {
				String studentName = fieldSet.readString(0);
				String studentId = fieldSet.readString(1);
				int korean = fieldSet.readInt(2);
				int english = fieldSet.readInt(3);
				int math = fieldSet.readInt(4);
				System.out.println("math = " + math);
				return new Student(studentName, studentId, korean, english, math, 0);
			})
			.build();
	}

	public ItemProcessor<Student, Student> processor(){
		return student -> {
			final int korean = student.getKorean();
			final int english = student.getEnglish();
			final int math = student.getMath();

			final double avg = (double) (korean + english + math) / 3;
			return new Student(student.getStudentName(), student.getStudentId(), korean, english, math, avg);
		};
	}

	@Bean
	public JdbcBatchItemWriter<Student> writer() {
		return new JdbcBatchItemWriterBuilder<Student>()
			.itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
			.dataSource(dataSource)
			.sql("INSERT INTO STUDENT (student_name, student_id, korean, english, math, avg) VALUES (:studentName, :studentId, :korean, :english, :math, :avg)")
			.build();
	}
}
