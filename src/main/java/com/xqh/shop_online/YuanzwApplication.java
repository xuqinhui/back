package com.yzw.shop_online;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@MapperScan("com.yzw.shop_online.mapper")
@SpringBootApplication
public class YuanzwApplication {

    public static void main(String[] args) {
        SpringApplication.run(YuanzwApplication.class, args);
    }

}
