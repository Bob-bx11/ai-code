package com.aicode.config;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ser.std.ToStringSerializer;

/**
 * Spring MVC Json 配置
 */
@Configuration
public class JsonConfig {

    /**
     * 将 Long / long 序列化为字符串，避免前端 JS 精度丢失
     */
    @Bean
    public JsonMapperBuilderCustomizer longToStringJsonMapperBuilderCustomizer() {
        return builder -> {
            SimpleModule module = new SimpleModule("LongToStringModule");
            module.addSerializer(Long.class, ToStringSerializer.instance);
            module.addSerializer(Long.TYPE, ToStringSerializer.instance);
            builder.addModule(module);
        };
    }
}
