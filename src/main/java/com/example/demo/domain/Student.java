package com.example.demo.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "student")
public class Student {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "student_name")
	private String studentName;
	@Column(name = "student_id")
	private String studentId;

	private int korean;
	private int english;
	private int math;
	private double avg;

	public Student(String studentName, String studentId, int korean, int english, int math, double avg) {
		this.studentName = studentName;
		this.studentId = studentId;
		this.korean = korean;
		this.english = english;
		this.math = math;
		this.avg = avg;
	}
}
