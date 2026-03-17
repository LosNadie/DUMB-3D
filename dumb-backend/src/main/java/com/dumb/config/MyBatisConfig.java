package com.dumb.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.dumb.mapper")
public class MyBatisConfig {
}
