package com.example.minimaltravel.model;


public class Task {

    private Long taskId; // Primary key
    private String description;
    private String creationDate;
    private String status; // Valores aceptados: "Pendiente", "Completado", "Eliminado"
    private Long assignedUserId;
    private String assignedUserName;

    // Getters and setters
    public Long getTaskId() { return taskId; }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getAssignedUserId() { return assignedUserId; }

    public void setAssignedUserId(Long assignedUserId) { this.assignedUserId = assignedUserId; }
    public String getAssignedUserName() { return assignedUserName; }

    public void setAssignedUserName(String assignedUserName) { this.assignedUserName = assignedUserName; }
}