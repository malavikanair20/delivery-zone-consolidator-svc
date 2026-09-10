package com.restaurant.deliveryzone.util;

import lombok.experimental.UtilityClass;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.io.File;
import java.util.Objects;

@UtilityClass
public class TestDataFactory {

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    public <T> T retreiveObject(Class<T> clazz, String filename) {
        try {
            var resource = Objects.requireNonNull(
                    TestDataFactory.class.getClassLoader().getResource(filename),
                    "Test resource not found: " + filename
            );
            return jsonMapper.readValue(new File(resource.getFile()),
                    clazz);
        } catch (JacksonException ex) {
            throw new RuntimeException(ex);
        }
    }

}

