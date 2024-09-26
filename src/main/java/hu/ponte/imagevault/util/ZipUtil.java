package hu.ponte.imagevault.util;

import hu.ponte.imagevault.exception.ZipException;
import hu.ponte.imagevault.model.File;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public final class ZipUtil {

    private ZipUtil() {
    }

    public static byte[] zipFiles(List<File> files) {
        try (var byteArrayOutputStream = new ByteArrayOutputStream();
             var zipOutputStream = new ZipOutputStream(byteArrayOutputStream)) {
            for (var file : files) {
                var zipEntry = new ZipEntry(file.getName());
                zipOutputStream.putNextEntry(zipEntry);
                zipOutputStream.write(file.getContent());
                zipOutputStream.closeEntry();
            }
            zipOutputStream.finish();
            return byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            throw new ZipException("Failed to zip files", e);
        }
    }

    public static List<File> unzipFiles(byte[] zipBytes) {
        var files = new ArrayList<File>();
        try (var byteArrayInputStream = new ByteArrayInputStream(zipBytes);
             var zipInputStream = new ZipInputStream(byteArrayInputStream)) {
            ZipEntry zipEntry;
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                try (var byteArrayOutputStream = new ByteArrayOutputStream()) {
                    zipInputStream.transferTo(byteArrayOutputStream);
                    files.add(new File(zipEntry.getName(), byteArrayOutputStream.toByteArray()));
                }
                zipInputStream.closeEntry();
            }
        } catch (Exception e) {
            throw new ZipException("Failed to unzip files", e);
        }
        return files;
    }
}
