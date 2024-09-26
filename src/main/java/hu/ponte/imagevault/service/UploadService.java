package hu.ponte.imagevault.service;

import hu.ponte.imagevault.db.FileRepository;
import hu.ponte.imagevault.exception.UploadErrorMessage;
import hu.ponte.imagevault.exception.UploadException;
import hu.ponte.imagevault.model.File;
import hu.ponte.imagevault.service.resize.ResizeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class UploadService {

    private static final int MAX_IMAGE_WIDTH = 5000;
    private static final int MAX_IMAGE_HEIGHT = 5000;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private ResizeService resizeService;

    private static byte[] checkContent(MultipartFile file) throws UploadException {
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new UploadException(UploadErrorMessage.WRONG_CONTENT);
        }
    }

    private static String checkExtension(MultipartFile file) throws UploadException {
        var fileName = file.getOriginalFilename();
        if (!fileName.contains(".")) {
            throw new UploadException(UploadErrorMessage.MISSING_EXTENSION);
        }
        var extension = fileName.substring(fileName.lastIndexOf('.')).toLowerCase();
        if (!File.SUPPORTED_EXTENSIONS.contains(extension)) {
            throw new UploadException(UploadErrorMessage.UNSUPPORTED_EXTENSION);
        }
        return extension;
    }

    public void resizeAndSaveToDb(MultipartFile file) throws UploadException {
        if (file.isEmpty()) {
            throw new UploadException(UploadErrorMessage.EMPTY_FILE);
        }
        var fileName = file.getOriginalFilename();
        var extension = checkExtension(file);
        var content = checkContent(file);
        var resized = resizeService.resizeImageIfNeeded(extension, content, MAX_IMAGE_WIDTH, MAX_IMAGE_HEIGHT);
        fileRepository.save(new File(fileName, resized));
    }

}
