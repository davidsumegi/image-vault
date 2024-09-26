package hu.ponte.imagevault.service.resize;

import hu.ponte.imagevault.exception.ResizeException;
import hu.ponte.imagevault.exception.ErrorType;
import hu.ponte.imagevault.exception.UploadException;
import org.im4java.core.ConvertCmd;
import org.im4java.core.IMOperation;
import org.im4java.core.IdentifyCmd;
import org.im4java.process.ArrayListOutputConsumer;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

@Service
public class ImageMagickResizeServiceImpl implements ResizeService {

    private static String[] getDimensions(File inputFile) throws UploadException {
        var arrayListOutputConsumer = new ArrayListOutputConsumer();
        var identifyCmd = new IdentifyCmd();
        identifyCmd.setOutputConsumer(arrayListOutputConsumer);

        var imOperation = new IMOperation();
        imOperation.format("%w %h");
        imOperation.addImage(inputFile.getAbsolutePath());

        try {
            identifyCmd.run(imOperation);
        } catch (Exception e) {
            throw new UploadException(ErrorType.WRONG_CONTENT, e);
        }

        return ((ArrayListOutputConsumer) arrayListOutputConsumer).getOutput().get(0).split(" ");
    }

    @Override
    public byte[] resizeImageIfNeeded(String extension, byte[] originalContent, int maxWidth, int maxHeight) throws UploadException {
        try {
            if (fitsMaxSize(extension, originalContent, maxWidth, maxHeight)) {
                return originalContent;
            }
            var inputFile = Files.createTempFile("inputImage", extension).toFile();
            var outputFile = Files.createTempFile("outputImage", extension).toFile();
            try (var fileOutputStream = new FileOutputStream(inputFile)) {
                fileOutputStream.write(originalContent);
            }

            var cmd = new ConvertCmd();
            var op = new IMOperation();
            op.addImage(inputFile.getAbsolutePath());
            op.resize(maxWidth, maxHeight);
            op.addImage(outputFile.getAbsolutePath());
            try {
                cmd.run(op);
            } catch (Exception e) {
                throw new UploadException(ErrorType.WRONG_CONTENT, e);
            }
            return Files.readAllBytes(outputFile.toPath());
        } catch (IOException e) {
            throw new ResizeException("Error during resizing image", e);
        }
    }

    private boolean fitsMaxSize(String extension, byte[] originalContent, int maxWidth, int maxHeight) throws UploadException {
        try {
            var inputFile = Files.createTempFile("inputImage", extension).toFile();
            try (var fileOutputStream = new FileOutputStream(inputFile)) {
                fileOutputStream.write(originalContent);
            }
            var dimensions = getDimensions(inputFile);
            var width = Integer.parseInt(dimensions[0]);
            var height = Integer.parseInt(dimensions[1]);
            return width <= maxWidth && height <= maxHeight;
        } catch (IOException e) {
            throw new ResizeException("Error during reading file", e);
        }
    }

}
