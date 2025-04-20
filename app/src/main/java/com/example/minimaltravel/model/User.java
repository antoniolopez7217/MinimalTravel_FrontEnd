package com.example.minimaltravel.model;

public class User {

    private Long userId; // Primary key
    private String userName;
    private String mail;
    private String creationDate;

    public Long getUserId() {
        return userId;
    }

    public String getuserName() {
        return userName;
    }

    public void setuserName(String userName) {
        this.userName = userName;
    }

    public String getmail() {
        return mail;
    }

    public void setmail(String mail) {
        this.mail = mail;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }
}
