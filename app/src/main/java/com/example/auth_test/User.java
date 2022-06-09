package com.example.auth_test;

public class User {
    public String firstName;
    public String lastName;
    public String email;
    public int minute = 0;
    public int second = 0;

    public User() {
    }

    public User (String firstName, String lastName, String email, int minute, int second) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.minute = minute;
        this.second = second;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public int getSecond() {
        return second;
    }

    public void setSecond(int second) {
        this.second = second;
    }
}