package com.application;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.support.SpringBootServletInitializer;


@SpringBootApplication
@MapperScan({"com.application.mapper", "com.application.mapper.custom"})
public class Application extends SpringBootServletInitializer implements CommandLineRunner {
	
	public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

	@Override
	public void run(String... arg0) throws Exception {
		System.out.println("系统启动完成！");
	}

}
