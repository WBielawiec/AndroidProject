package com.example.projektandroid;

import java.util.ArrayList;
import java.util.HashMap;

public class Coordinates {
    private double x;
    private double y;
    private String code;

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public String getCode() {
        return code;
    }

    public Coordinates(String code, double x, double y){
        this.code = code;
        this.x = x;
        this.y = y;
    }
}

