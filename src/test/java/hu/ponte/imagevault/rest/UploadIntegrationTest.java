package hu.ponte.imagevault.rest;

import hu.ponte.imagevault.TestUtil;
import hu.ponte.imagevault.model.File;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.ResourceAccessException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UploadIntegrationTest extends IntegrationTestBase {

    @Test
    void uploadASingleFile() {
        var file = readFileFromResource("just_ok.jpg");
        var response = uploadFile(file);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertJsonEquals(response.getBody(), """
                {"just_ok.jpg":"OK"}""");
    }

    @Test
    void uploadMultipleFiles() {
        var files = readFilesFromResources("just_ok.jpg", "just_ok.png", "just_ok.tiff", "no_ext", "empty.png", "corrupted.jpg");
        var response = uploadFiles(files);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertJsonEquals(response.getBody(), """
                {
                "just_ok.jpg":"OK",
                "just_ok.png":"OK",
                "just_ok.tiff":"UNSUPPORTED_EXTENSION",
                "no_ext":"MISSING_EXTENSION",
                "empty.png":"EMPTY_FILE",
                "corrupted.jpg":"WRONG_CONTENT"
                }""");
    }

    @Test
    void overwriteFileSuccessfully() {
        var file = readFileFromResource("too_wide.jpg");
        uploadFile(file);
        file.setContent(readFileFromResource("just_ok.jpg").getContent());
        var response = uploadFile(file);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("""
                {"too_wide.jpg":"OK"}""");
        assertThat(downloadSingleFile("too_wide.jpg").getBody()).isEqualTo(file.getContent());
    }

    @Test
    void overwriteSkippedBecauseNewFileIsWrong() {
        var file = readFileFromResource("just_ok.jpg");
        uploadFile(file);
        var originalContent = file.getContent();
        file.setContent(readFileFromResource("corrupted.jpg").getContent());
        var response = uploadFile(file);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("""
                {"just_ok.jpg":"WRONG_CONTENT"}""");
        assertThat(downloadSingleFile("just_ok.jpg").getBody()).isEqualTo(originalContent);
    }

    @Test
    void uploadMaximumSizedSingleFile() {
        var response = uploadFile(new File("maximum_size", TestUtil.generateRandomByteArray(20)));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void uploadTooLargeSingleFile() {
        assertThatThrownBy(() -> uploadFile(new File("too_large", TestUtil.generateRandomByteArray(21))))
                .isInstanceOf(ResourceAccessException.class)
                .hasMessageContaining("Error writing request body to server");
    }

    @Test
    void uploadFilesIncludingTooLargeFile() {
        assertThatThrownBy(() -> uploadFiles(List.of(
                new File("maximum_size", TestUtil.generateRandomByteArray(20)),
                new File("too_large", TestUtil.generateRandomByteArray(21))
        )))
                .isInstanceOf(ResourceAccessException.class)
                .hasMessageContaining("Error writing request body to server");
    }

    @Test
    void uploadFilesMaximumSizeInTotal() {
        var response = uploadFiles(TestUtil.generateRandomFilesWithCountAndSize(99, 1));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void uploadFilesTooLargeInTotal() {
        var response = uploadFiles(TestUtil.generateRandomFilesWithCountAndSize(100, 1));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.PAYLOAD_TOO_LARGE);
    }

}
