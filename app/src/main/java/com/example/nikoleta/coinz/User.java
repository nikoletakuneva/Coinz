package com.example.nikoleta.coinz;

public class User {
    private String email, username, password;
    private double money;

    User(String email, String password, double money) {
        this.email = email;
        this.password = password;
        this.money = money;
    }

    public String getEmail() {

        return email;
    }

    public String getPassword() {
        return password;
    }

    public double getMoney() {
        return money;
    }
}
