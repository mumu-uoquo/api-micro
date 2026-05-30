package com.uoquo.platform.main.config;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.uoquo.utils.json.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import jakarta.annotation.PostConstruct;
import java.io.IOException;

@Configuration
@Import(ApplicationDeveloperConfig.class)
@Order(0)
public class ApplicationConfig {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @PostConstruct
    public void setProperties() {
        log.debug("ApplicationConfig init ...");
    }

//    /**
//     * HTTP的转换器
//     */
//    @Bean
//    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
//        log.debug("加载BEAN：MappingJackson2HttpMessageConverter");
//        ObjectMapper mapper = Jackson2ObjectMapperBuilder.json().build();
//        // 分页序列化（此处不处理，防止在ServiceConfig中重复注册）
//        mapper = JsonUtil.initialJackson(mapper);
//        // 枚举序列化
//        SimpleModule simpleModule = new SimpleModule();
//        simpleModule.addDeserializer(SseMessageTypeEnum.class, new JsonDeserializer<SseMessageTypeEnum>() {
//            @Override
//            public SseMessageTypeEnum deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
//                String name = p.getValueAsString();
//                return SseMessageTypeEnum.fromName(name); // 使用自定义方法处理未知值
//            }
//        });
//        mapper.registerModule(simpleModule);
//        return new MappingJackson2HttpMessageConverter(mapper);
//    }

}
