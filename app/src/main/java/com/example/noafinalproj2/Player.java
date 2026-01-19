package com.example.noafinalproj2;

public class Player {
    private String name;
    private int trophies;
    /*private String id;*/

    public Player(String name/*,String id*/) {
        this.name = name;
        this.trophies = 100; // התחלה עם 100 גביעים
        /**this.id=id;*/
    }

    public String getName() { return name; }
    public int getTrophies() { return trophies; }

    // עדכון גביעים בסיום משחק
    public void addTrophiesEndGame(int amount) {
        this.trophies += amount;
        if (this.trophies < 0) this.trophies = 0;
    }
    /*public String getId(){return id;}*/
}
