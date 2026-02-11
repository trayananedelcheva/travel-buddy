package com.sap.travel_buddy.exception;

/**
 * Exception за ситуации, когато ресурс не е намерен
 */
public class ResourceNotFoundException extends RuntimeException {
    
    public ResourceNotFoundException(String message) {
        super(message);
    }
    
    public ResourceNotFoundException(String resourceName, Long id) {
        super(String.format("%s with id %d not found", resourceName, id));
    }
    
    public ResourceNotFoundException(String resourceName, String identifier, String value) {
        super(String.format("%s with %s '%s' not found", resourceName, identifier, value));
    }
}
