package com.campus.validation;

/**
 * Represents an individual validation issue detected in campus datasets.
 */
public class ValidationIssue {

    public enum Severity {
        ERROR,
        WARNING,
        INFO
    }

    public enum EntityType {
        NODE,
        EDGE,
        BUILDING,
        GRAPH
    }

    private final Severity severity;
    private final EntityType entityType;
    private final String entityId;
    private final String field;
    private final String message;
    private final Object invalidValue;

    public ValidationIssue(
            Severity severity,
            EntityType entityType,
            String entityId,
            String field,
            String message,
            Object invalidValue) {
        this.severity = severity;
        this.entityType = entityType;
        this.entityId = entityId;
        this.field = field;
        this.message = message;
        this.invalidValue = invalidValue;
    }

    public static ValidationIssue error(EntityType entityType, String entityId, String field, String message, Object invalidValue) {
        return new ValidationIssue(Severity.ERROR, entityType, entityId, field, message, invalidValue);
    }

    public static ValidationIssue warning(EntityType entityType, String entityId, String field, String message, Object invalidValue) {
        return new ValidationIssue(Severity.WARNING, entityType, entityId, field, message, invalidValue);
    }

    public static ValidationIssue info(EntityType entityType, String entityId, String field, String message, Object invalidValue) {
        return new ValidationIssue(Severity.INFO, entityType, entityId, field, message, invalidValue);
    }

    public Severity getSeverity() {
        return severity;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public String getEntityId() {
        return entityId;
    }

    public String getField() {
        return field;
    }

    public String getMessage() {
        return message;
    }

    public Object getInvalidValue() {
        return invalidValue;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s %s: field '%s' -> %s (value: %s)",
                severity, entityType, entityId, field, message, invalidValue);
    }
}
