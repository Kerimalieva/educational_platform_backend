package com.onlinelearning.exception;

public class AccessDeniedException extends BusinessException {
    public AccessDeniedException(String message) {
        super(message, "ACCESS_DENIED");
    }
}