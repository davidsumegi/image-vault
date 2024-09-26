package hu.ponte.imagevault.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UploadException extends Exception {
    private final ErrorType ErrorType;

    public UploadException(ErrorType ErrorType, Throwable cause) {
        super(cause);
        this.ErrorType = ErrorType;
    }
}
