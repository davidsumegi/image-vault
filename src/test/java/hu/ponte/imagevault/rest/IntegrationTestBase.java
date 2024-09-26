package hu.ponte.imagevault.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import hu.ponte.imagevault.model.File;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.LinkedMultiValueMap;

import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class IntegrationTestBase {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @LocalServerPort
    int localPort;

    TestRestTemplate template;

    @Autowired
    private ResourceLoader resourceLoader;

    @BeforeEach
    void setUp() {
        template = new TestRestTemplate(new RestTemplateBuilder().rootUri("http://localhost:%s".formatted(localPort)));
    }

    @SneakyThrows
    static void assertJsonEquals(String actual, String expected) {
        assertThat(OBJECT_MAPPER.readTree(actual)).isEqualTo(OBJECT_MAPPER.readTree(expected));
    }

    ResponseEntity<byte[]> downloadSingleFile(String fileName) {
        return template.getForEntity("/api/file/%s".formatted(fileName), byte[].class);
    }

    ResponseEntity<byte[]> downloadAllFilesAsZip() {
        return template.getForEntity("/api/files", byte[].class);
    }

    ResponseEntity<byte[]> downloadAllFilesAsZipWithPage(int page) {
        return template.getForEntity("/api/files?page={page}", byte[].class, page);
    }

    List<File> readFilesFromResources(String... fileNames) {
        return Arrays.asList(fileNames).stream()
                .map(fileName -> readFileFromResource(fileName))
                .toList();
    }

    ResponseEntity<String> uploadFiles(List<File> files) {
        var body = new LinkedMultiValueMap<String, Object>();
        for (var file : files) {
            body.add("files", makeMultipartFile(file.getName(), file.getContent()));
        }
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        return template.postForEntity("/api/files", new HttpEntity<>(body, headers), String.class);
    }

    ResponseEntity<String> uploadFile(File file) {
        var body = new LinkedMultiValueMap<String, Object>();
        body.add("file", makeMultipartFile(file.getName(), file.getContent()));
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        return template.postForEntity("/api/file", new HttpEntity<>(body, headers), String.class);
    }

    private ByteArrayResource makeMultipartFile(String fileName, byte[] content) {
        return new ByteArrayResource(content) {
            @Override
            public String getFilename() {
                return fileName;
            }
        };
    }

    @SneakyThrows
    File readFileFromResource(String fileName) {
        return new File(fileName, Files.readAllBytes(resourceLoader.getResource("classpath:" + fileName).getFile().toPath()));
    }

}
