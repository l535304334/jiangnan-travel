package com.jiangnan.travel.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Knife4jConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("江南出行智慧服务平台 API")
                .description("江南出行智慧服务平台后端接口文档，提供出租车/网约车预约、AI推荐、文旅融合等功能")
                .version("v1.0")
                .contact(new Contact().name("江南出行开发团队")));
    }
}
