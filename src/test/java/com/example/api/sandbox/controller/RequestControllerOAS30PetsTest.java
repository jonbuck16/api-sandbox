package com.example.api.sandbox.controller;

import org.apache.commons.io.FileUtils;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.comparator.ArraySizeComparator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.stream.Stream;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RequestControllerOAS30PetsTest {
    public static final String PET = "pet";
    public static final String PETS = "pets";
    public static final String HTTP_LOCALHOST = "http://localhost:";
    public static final String SYSTEM_CLEAR = "/system/clear";
    @ClassRule
    public static final EnvironmentVariables envVars = new EnvironmentVariables();
    public static String OLLIE;
    public static String ERNIE;
    public static String FIDO;
    public static String HONEY;
    private static HttpHeaders httpHeaders;
    private UriComponentsBuilder builder;
    @Autowired
    private RequestController requestController;
    @LocalServerPort
    private int port;
    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeClass
    public static void beforeClass() throws Exception {
        envVars.set("specifications.directory", Paths.get("src/test/resources/oas3/specifications").toFile().getAbsolutePath());
        httpHeaders = new HttpHeaders();
        httpHeaders.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        httpHeaders.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        OLLIE = FileUtils.readFileToString(Paths.get("src/test/resources/data/pets/ollie.json").toFile(), StandardCharsets.UTF_8);
        ERNIE = FileUtils.readFileToString(Paths.get("src/test/resources/data/pets/ernie.json").toFile(), StandardCharsets.UTF_8);
        FIDO = FileUtils.readFileToString(Paths.get("src/test/resources/data/pets/fido.json").toFile(), StandardCharsets.UTF_8);
        HONEY = FileUtils.readFileToString(Paths.get("src/test/resources/data/pets/honey.json").toFile(), StandardCharsets.UTF_8);
    }

    @Before
    public void beforeEachTest() {
        builder = UriComponentsBuilder.fromHttpUrl(HTTP_LOCALHOST + port);
        Assertions.assertThat(restTemplate.postForEntity(builder.replacePath(SYSTEM_CLEAR).toUriString(),
                new HttpEntity<>(httpHeaders), String.class).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void getErnie() throws Exception {
        addPets(ERNIE);
        ResponseEntity<String> response = restTemplate.exchange(builder.replacePath(PET).pathSegment("Ernie").toUriString(),
                HttpMethod.GET, new HttpEntity<>(httpHeaders), String.class);
        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JSONAssert.assertEquals("{\"name\":\"Ernie\"}", response.getBody(), JSONCompareMode.LENIENT);
    }

    @Test
    public void findBySingleStatus() throws Exception {
        addPets(OLLIE, ERNIE, FIDO, HONEY);
        ResponseEntity<String> response = restTemplate.exchange(builder.replacePath(PET).pathSegment("findByStatus").queryParam(
                "status", "available").toUriString(), HttpMethod.GET, new HttpEntity<>(httpHeaders), String.class);
        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JSONAssert.assertEquals("[2]", Objects.requireNonNull(response.getBody()),
                new ArraySizeComparator(JSONCompareMode.LENIENT));
    }

    @Test
    public void findByMultipleStatus() throws Exception {
        addPets(OLLIE, ERNIE, FIDO, HONEY);
        ResponseEntity<String> response = restTemplate.exchange(builder.replacePath(PET).pathSegment("findByStatus").queryParam(
                "status", "available").queryParam("status", "pending").toUriString(), HttpMethod.GET,
                new HttpEntity<>(httpHeaders), String.class);
        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JSONAssert.assertEquals("[3]", Objects.requireNonNull(response.getBody()),
                new ArraySizeComparator(JSONCompareMode.LENIENT));
    }

    @Test
    public void findByTags() throws Exception {
        addPets(OLLIE, ERNIE, FIDO, HONEY);
        ResponseEntity<String> response = restTemplate.exchange(builder.replacePath(PET).pathSegment("findByTags").queryParam(
                "tags", "dog").toUriString(), HttpMethod.GET,
                new HttpEntity<>(httpHeaders), String.class);
        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JSONAssert.assertEquals("[3]", Objects.requireNonNull(response.getBody()),
                new ArraySizeComparator(JSONCompareMode.LENIENT));
    }

    @Test
    public void getAllPets() throws Exception {
        addPets(OLLIE, ERNIE, FIDO, HONEY);
        ResponseEntity<String> response = restTemplate.exchange(builder.replacePath(PETS).toUriString(), HttpMethod.GET,
                new HttpEntity<>(httpHeaders), String.class);
        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JSONAssert.assertEquals("[4]", Objects.requireNonNull(response.getBody()),
                new ArraySizeComparator(JSONCompareMode.LENIENT));
    }

    /**
     * Adds pets into the petstore
     *
     * @param pets the list of pets to add
     */
    private void addPets(String... pets) {
        Stream.of(pets).forEach(s -> Assertions.assertThat(restTemplate.postForEntity(builder.replacePath(PET).toUriString(),
                new HttpEntity<>(s, httpHeaders), String.class).getStatusCode()).isEqualTo(HttpStatus.CREATED));
    }

}
