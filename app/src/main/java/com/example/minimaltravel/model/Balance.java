package com.example.minimaltravel.model;


public class Balance {
    private Long creditorUserId;   // ID del usuario que debe recibir dinero
    private String creditorUserName;
    private Long debtorUserId;     // ID del usuario que debe pagar
    private String debtorUserName;
    private Double amount;         // Cantidad que el debtor debe al creditor

    public Long getCreditorUserId() { return creditorUserId; }
    public String getCreditorUserName() { return creditorUserName; }
    public Long getDebtorUserId() { return debtorUserId; }
    public String getDebtorUserName() { return debtorUserName; }
    public Double getAmount() { return amount; }
}
