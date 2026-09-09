package com.restaurant.deliveryzone.util;

import lombok.experimental.UtilityClass;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.io.File;

@UtilityClass
public class TestDataFactory {

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    public <T> T retreiveObject(Class<T> clazz, String filename) {
        try {
            return jsonMapper.readValue(new File(TestDataFactory.class.getClassLoader().getResource(filename).getFile()),
                    clazz);
        } catch (JacksonException ex) {
            throw new RuntimeException(ex);
        }
    }
}

