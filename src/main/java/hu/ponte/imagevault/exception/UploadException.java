package hu.ponte.imagevault.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UploadException extends Exception {
    private final UploadErrorMessage uploadErrorMessage;

    public UploadException(UploadErrorMessage uploadErrorMessage, Throwable cause) {
        super(cause);
        this.uploadErrorMessage = uploadErrorMessage;
    }
}
