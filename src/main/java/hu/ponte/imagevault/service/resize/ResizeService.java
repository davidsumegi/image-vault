package hu.ponte.imagevault.service.resize;

import hu.ponte.imagevault.exception.UploadException;

public interface ResizeService {
    byte[] resizeImageIfNeeded(String extension, byte[] originalContent, int maxWidth, int maxHeight) throws UploadException;
}
