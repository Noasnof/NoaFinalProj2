package com.example.noafinalproj2;

public class Record {
    private String Answer;


    public Record(String Answer) {
        this.Answer = Answer;

    }
    public String getAnswer() {
        return Answer;
    }

    public void setAnswer(String answer) {
        Answer = answer;
    }



    // MUST have the constructor  for the FireBase
    public Record() {
    }

    // MUST generate getters and setters for the FireBase



}
