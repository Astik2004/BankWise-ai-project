package com.bankwise.common.exception;

public class RagProcessingException extends RuntimeException {

    public RagProcessingException(String message) {
        super(message);
    }

    public RagProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
