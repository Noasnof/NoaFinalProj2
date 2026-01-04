package com.example.noafinalproj2;

public class MyDetailsInFb {
     private String type;
    private int price;

    public MyDetailsInFb(String type, int price) {
        this.type = type;
        this.price = price;

    }

    // MUST have the constructor  for the FireBase
    public MyDetailsInFb() {
    }

    // MUST generate getters and setters for the FireBase

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "MyDetailsInFb{" +
                ", type='" + type + '\'' +
                ", price=" + price +
                '}';
    }
}
