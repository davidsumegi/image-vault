package hu.ponte.imagevault.rest;

import hu.ponte.imagevault.model.File;
import hu.ponte.imagevault.util.FileUtil;
import hu.ponte.imagevault.util.ZipUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;

class DownloadIntegrationTest extends IntegrationTestBase {

    @ParameterizedTest
    @ValueSource(strings = {"just_ok.jpg", "just_ok.png"})
    void downloadSingleFileUnresized(String fileName) {
        var file = readFileFromResource(fileName);
        uploadFile(file);
        var response = downloadSingleFile(fileName);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(FileUtil.typeOf(fileName));
        assertThat(response.getBody()).isEqualTo(file.getContent());
    }

    @ParameterizedTest
    @ValueSource(strings = {"too_wide.jpg", "too_high.jpg", "too_wide.png", "too_high.png"})
    void downloadSingleFileResized(String fileName) {
        var file = readFileFromResource(fileName);
        uploadFile(file);
        var response = downloadSingleFile(fileName);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(FileUtil.typeOf(fileName));
        assertThat(response.getBody()).isNotEqualTo(file.getContent());
    }

    @Test
    void downloadAllFiles() {
        var files = readFilesFromResources("just_ok.jpg", "just_ok.png", "just_ok.tiff");
        uploadFiles(files);
        var response = downloadAllFilesAsZip();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_OCTET_STREAM);
        assertThat(response.getHeaders().getContentDisposition().getFilename()).isEqualTo("images.zip");
        assertThat(ZipUtil.unzipFiles(response.getBody())).containsExactlyInAnyOrderElementsOf(
                files.subList(0, 2));
    }

    @Test
    void downloadWithOffset() {
        var files = readFilesFromResources("just_ok.jpg", "just_ok.png", "too_high.jpg", "too_high.png", "too_wide.jpg", "too_wide.png");
        uploadFiles(files);
        assertThat(ZipUtil.unzipFiles(downloadAllFilesAsZip().getBody()))
                .extracting(File::getName)
                .containsExactly("just_ok.jpg", "just_ok.png", "too_high.jpg", "too_high.png", "too_wide.jpg");
        assertThat(ZipUtil.unzipFiles(downloadAllFilesAsZipWithPage(0).getBody()))
                .extracting(File::getName)
                .containsExactly("just_ok.jpg", "just_ok.png", "too_high.jpg", "too_high.png", "too_wide.jpg");
        assertThat(ZipUtil.unzipFiles(downloadAllFilesAsZipWithPage(1).getBody()))
                .extracting(File::getName)
                .containsExactly("too_wide.png");
    }

    @Test
    void downloadSingleFile_NotFound() {
        var response = downloadSingleFile("image.jpg");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void downloadAllFilesAsZip_NothingFound() {
        var response = downloadAllFilesAsZip();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

}
