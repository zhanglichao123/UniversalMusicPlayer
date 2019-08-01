package com.example.myapplication;

import java.io.Serializable;

public class Bean implements Serializable {
    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }

    private   String string;
}
