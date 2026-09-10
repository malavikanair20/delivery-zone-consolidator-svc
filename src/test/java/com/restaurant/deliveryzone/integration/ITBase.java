package com.restaurant.deliveryzone.integration;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class ITBase {

    @LocalServerPort
    protected int port;

    @BeforeEach
    protected void initialSetup() {
        RestAssured.port = port;
    }
}

