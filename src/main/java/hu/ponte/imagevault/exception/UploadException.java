package hu.ponte.imagevault.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UploadException extends Exception {
    private final ErrorType errorType;

    public UploadException(ErrorType errorType, Throwable cause) {
        super(cause);
        this.errorType = errorType;
    }
}
