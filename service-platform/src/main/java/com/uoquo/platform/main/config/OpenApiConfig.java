package com.uoquo.platform.main.config;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
//@ConditionalOnProperty(name = "app.swagger.enabled", havingValue = "true", matchIfMissing = false)
//@ConditionalOnExpression("${app.swagger.enabled:false} || ${springdoc.api-docs.enabled:false}")
//@ConditionalOnExpression("${app.swagger.enabled:${springdoc.api-docs.enabled:false}} == 'true'")
@ConditionalOnExpression("${app.swagger.enabled:${springdoc.api-docs.enabled:false}}")
public class OpenApiConfig {
    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("RestfulApi")
                // 接口过滤，据此增加接口扫描规则（扫描@Operation注解标注的接口）。想皮一下的话，亦可自定义注解
                .addOpenApiMethodFilter(method -> method.isAnnotationPresent(Operation.class))
//                .pathsToMatch("^(?!.*\\/admin\\/).*$")
                .pathsToExclude("/admin/**", "/**/admin/**")
                .build();
    }
    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
                .group("AdminApi")
                .addOpenApiMethodFilter(method -> method.isAnnotationPresent(Operation.class))
                .pathsToMatch("/admin/**", "/**/admin/**")
                .build();
    }

    @Bean
    public OpenAPI customOpenAPI() {
        Contact contact = new Contact()
                .name("uoquo")
                .email("dev@uoquo.com")
                .url("https://www.uoquo.com");
        License license = new License()
                .name("Apache 2.0")
                .url("http://www.apache.org/licenses/LICENSE-2.0");
        Info info = new Info()
                .title("基础服务")
                .version("1.0.0")
                .description("用户管理等基础服务。")
                .contact(contact)
                .license(license);
        return new OpenAPI()
                .info(info);
    }

}