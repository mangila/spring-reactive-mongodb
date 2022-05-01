package com.github.mangila.springreactivemongodb;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@SpringBootTest
@AutoConfigureWebTestClient
@Slf4j
class ApplicationControllerTest {

    @Container
    private static final MongoDBContainer container = new MongoDBContainer(DockerImageName.parse("mongo"))
            .withLogConsumer(new Slf4jLogConsumer(log));

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", container::getReplicaSetUrl);
    }

    @Autowired
    private WebTestClient http;

    @Test
    void findUserByUsername() {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("username", "mangila");
        builder.part("files", new FileSystemResource("src/test/resources/test.txt"));
        builder.part("files", new FileSystemResource("src/test/resources/test.txt"));

        http.post()
                .uri("/api/v1/upload")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .exchange()
                .expectStatus()
                .is2xxSuccessful();

        http.get()
                .uri("/api/v1/{username}", "mangila")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody()
                .jsonPath("$.username").isEqualTo("mangila")
                .jsonPath("$.fileUuids.length()").isEqualTo(2);
    }

    @Test
    void downloadFileByUuid() {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("username", "alignam");
        builder.part("files", new FileSystemResource("src/test/resources/test.txt"));
        builder.part("files", new FileSystemResource("src/test/resources/test.txt"));

        http.post()
                .uri("/api/v1/upload")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .exchange()
                .expectStatus()
                .is2xxSuccessful();

        var userDto = http.get()
                .uri("/api/v1/{username}", "alignam")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .returnResult(UserDto.class)
                .getResponseBody()
                .blockFirst();

        String fileUuid = userDto.getFileUuids().get(0);

        http.get()
                .uri("/api/v1/{username}/{uuid}", "alignam", fileUuid)
                .accept(MediaType.APPLICATION_OCTET_STREAM)
                .exchange()
                .expectStatus()
                .is2xxSuccessful();
    }

}