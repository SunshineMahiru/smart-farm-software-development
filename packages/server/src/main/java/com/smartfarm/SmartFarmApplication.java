package com.smartfarm;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // 开启定时任务支持 (为成员2和3铺路)
@MapperScan("com.smartfarm.modules.**.mapper") // 扫描所有模块的 Mapper
public class SmartFarmApplication {
    public static void main(String[] args) {
        SpringApplication.run(SmartFarmApplication.class, args);
        System.out.println("===============================================");
        System.out.println("🚀 智慧农场数字孪生管控平台 - 后端引擎启动成功！");
        System.out.println("===============================================");
    }
}