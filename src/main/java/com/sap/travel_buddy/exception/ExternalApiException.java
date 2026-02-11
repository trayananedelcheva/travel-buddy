package com.sap.travel_buddy.exception;

/**
 * Exception за грешки при извикване на външни API-та
 */
public class ExternalApiException extends RuntimeException {
    
    private final String apiName;
    
    public ExternalApiException(String apiName, String message) {
        super(String.format("Error calling %s API: %s", apiName, message));
        this.apiName = apiName;
    }
    
    public ExternalApiException(String apiName, String message, Throwable cause) {
        super(String.format("Error calling %s API: %s", apiName, message), cause);
        this.apiName = apiName;
    }
    
    public String getApiName() {
        return apiName;
    }
}
