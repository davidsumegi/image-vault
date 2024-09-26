package hu.ponte.imagevault.util;

import org.springframework.http.MediaType;

public final class FileUtil {


    private FileUtil() {
    }

    public static MediaType typeOf(String fileName) {
        switch (fileName.substring(fileName.lastIndexOf('.'))) {
            case ".jpg":
                return MediaType.IMAGE_JPEG;
            case ".png":
                return MediaType.IMAGE_PNG;
            default:
                return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

}
