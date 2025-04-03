package com.subaiqiao.yupicture;

import org.apache.shardingsphere.spring.boot.ShardingSphereAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication(exclude = {ShardingSphereAutoConfiguration.class})
@MapperScan("com.subaiqiao.yupicture.infrastructure.manager")
// 开启AOP代理
@EnableAspectJAutoProxy(exposeProxy = true)
public class YuPictureApplication {

    public static void main(String[] args) {
        SpringApplication.run(YuPictureApplication.class, args);
    }

}
