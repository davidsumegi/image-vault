package hu.ponte.imagevault.util;

import hu.ponte.imagevault.TestUtil;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ZipUtilTest {

    @Test
    void zipLargeFiles() {
        var files = TestUtil.generateRandomFilesWithCountAndSize(2, 20);
        var zipped = ZipUtil.zipFiles(files);
        var unzipped = ZipUtil.unzipFiles(zipped);
        assertThat(unzipped).containsExactlyElementsOf(files);
    }

    @Test
    void zipManyFiles() {
        var files = TestUtil.generateRandomFilesWithCountAndSize(100, 1);
        var zipped = ZipUtil.zipFiles(files);
        var unzipped = ZipUtil.unzipFiles(zipped);
        assertThat(unzipped).containsExactlyInAnyOrderElementsOf(files);
    }
}
