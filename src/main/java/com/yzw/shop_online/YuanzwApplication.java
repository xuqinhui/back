package com.yzw.shop_online;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.RequestBody;

@MapperScan("com.yzw.shop_online.mapper")
@SpringBootApplication
@EnableAsync
public class YuanzwApplication {

    public static void main(String[] args) {
        SpringApplication.run(YuanzwApplication.class, args);
    }

}
