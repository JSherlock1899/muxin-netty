package com.sherlock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import tk.mybatis.spring.annotation.MapperScan;

// 扫描 所有需要的包, 包含一些自用的工具类包 所在的路径
@SpringBootApplication(scanBasePackages = {"com.sherlock", "org.n3r.idworker"})
// 扫描mybatis mapper包路径
@MapperScan(basePackages="com.sherlock.mapper")
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	public SpringUtil getSpringUtil() {
		return new SpringUtil();
	}
}
