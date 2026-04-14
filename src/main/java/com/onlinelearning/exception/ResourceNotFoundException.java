package com.onlinelearning.exception;

public class ResourceNotFoundException extends BusinessException {
    public ResourceNotFoundException(String resourceName, Long id) {
        super(resourceName + " not found with id: " + id, "NOT_FOUND");
    }

    public ResourceNotFoundException(String message) {
        super(message, "NOT_FOUND");
    }

}