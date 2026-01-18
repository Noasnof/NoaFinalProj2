package com.example.noafinalproj2;

public class MyDetailsInFb {

    private int Score;

    public MyDetailsInFb() {


    }

    public MyDetailsInFb(int score) {
        this.Score = score;
    }

    // MUST have the constructor  for the FireBase


    // MUST generate getters and setters for the FireBase


    public int getScore() {
        return Score;
    }

    public void setScore(int Score) {
        this.Score = Score;
    }

    @Override
    public String toString() {
        return "MyDetailsInFb{" +
                ", Score='" + Score +
                '}';
    }
}
