package com.example.projektandroid;

public class NBP {
    String currency;
    String code;
    double bid;
    double ask;

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public double getBid() {
        return bid;
    }

    public void setBid(double bid) {
        this.bid = bid;
    }

    public double getask() {
        return ask;
    }

    public void setask(double ask) {
        this.ask = ask;
    }

    public NBP(String currency, String code, double bid, double ask){
        this.currency = currency;
        this.ask = ask;
        this.code = code;
        this.bid = bid;
    }

}

