package com.petadoption.petadoption.exception;

public class ResourceNotFoundException extends BaseException {
    public ResourceNotFoundException(String resourceName, Long id) {
        super(404, resourceName + " not found with id: " + id);
    }
}