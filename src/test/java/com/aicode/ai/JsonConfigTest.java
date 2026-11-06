package com.aicode.ai;

import com.aicode.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class JsonConfigTest {

    @Autowired
    private JsonMapper jsonMapper;

    @Test
    void longShouldSerializeAsString() {
        String json = jsonMapper.writeValueAsString(Map.of("id", 1234567890123456789L));
        System.out.println("JSON = " + json);
        assertThat(json).contains("\"id\":\"1234567890123456789\"");
    }

    @Test
    void stringShouldDeserializeToLong() {
        User user = jsonMapper.readValue("{\"id\":\"1234567890123456789\"}", User.class);
        assertThat(user.getId()).isEqualTo(1234567890123456789L);
    }
}
