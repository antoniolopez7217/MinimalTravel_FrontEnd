package com.example.minimaltravel.model;

import java.util.List;

public class Transaction {
    private Long transactionId;
    private String description;
    private Double amount;
    private String category; // Puedes usar String o el mismo Enum si lo tienes en el frontend
    private String creationDate;
    private Long creditorUserId;
    private String creditorUserName;
    private List<Participant> participants;

    // Clase interna para los participantes
    public static class Participant {
        private Long userId;
        private String userName;
        private Double amount;

        // Getters y setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }

        public Double getAmount() { return amount; }
        public void setAmount(Double amount) { this.amount = amount; }
    }

    // Getters y setters para Transaction
    public Long getTransactionId() { return transactionId; }
    public void setTransactionId(Long transactionId) { this.transactionId = transactionId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getCreationDate() { return creationDate; }
    public void setCreationDate(String creationDate) { this.creationDate = creationDate; }

    public Long getCreditorUserId() { return creditorUserId; }
    public void setCreditorUserId(Long creditorUserId) { this.creditorUserId = creditorUserId; }

    public String getCreditorUserName() { return creditorUserName; }
    public void setCreditorUserName(String creditorUserName) { this.creditorUserName = creditorUserName; }

    public List<Participant> getParticipants() { return participants; }
    public void setParticipants(List<Participant> participants) { this.participants = participants; }
}
