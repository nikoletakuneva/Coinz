package com.example.nikoleta.coinz;

public class Coin {
    String id;
    String currency;
    double value;
    Integer image;
    Coin(String curr, double val, String id) {
        this.currency = curr;
        this.value = val;
        this.id = id;
        switch(currency) {
            case "QUID": this.image = R.drawable.quid;
                break;
            case "DOLR": this.image = R.drawable.dollar;
                break;
            case "PENY": this.image = R.drawable.penny;
                break;
            case "SHIL": this.image = R.drawable.shilling;
                break;
        }
    }

    public String getId() {
        return id;
    }

    public double getValue() {
        return value;
    }

    public Integer getImage() {
        return image;
    }

    public String getCurrency() {
        return currency;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
