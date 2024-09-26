package hu.ponte.imagevault;

import hu.ponte.imagevault.model.File;

import java.security.SecureRandom;
import java.util.List;
import java.util.stream.IntStream;

public class TestUtil {

    public static byte[] generateRandomByteArray(int megaBytes) {
        byte[] byteArray = new byte[1024 * 1024 * megaBytes];
        new SecureRandom().nextBytes(byteArray);
        return byteArray;
    }

    public static List<File> generateRandomFilesWithCountAndSize(int fileCount, int megabytes) {
        var files = IntStream.range(0, fileCount).boxed()
                .map(i -> new File("Café" + i + ".jpg", generateRandomByteArray(megabytes)))
                .toList();
        return files;
    }

}
