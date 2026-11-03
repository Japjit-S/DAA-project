package com.campus.validation;

/**
 * Thrown when critical data validation errors are detected on startup or dataset reload.
 */
public class DataValidationException extends RuntimeException {

    private final ValidationReport report;

    public DataValidationException(String message, ValidationReport report) {
        super(message);
        this.report = report;
    }

    public ValidationReport getReport() {
        return report;
    }
}
