package com.example.Task_Management_Programs;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import java.util.Arrays;

@SpringBootApplication
@ComponentScan(basePackages = "com.example")
public class TaskManagementProgramsApplication {

	public static void main(String[] args) {
		SpringApplication.run(TaskManagementProgramsApplication.class, args);
	}

//		@Bean
//		public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
//
//			return args -> {
//				System.out.println("Beans, загруженные в Spring-контексте:");
//				String[] beanNames = ctx.getBeanDefinitionNames();
//				Arrays.sort(beanNames);
//				for (String beanName : beanNames) {
//					System.out.println(beanName);
//				}
//			};
//		}
	}

