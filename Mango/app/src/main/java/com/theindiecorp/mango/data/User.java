package com.theindiecorp.mango.data;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.Date;

// [START blog_user_class]
@IgnoreExtraProperties

public class User {

    public enum Sex {MALE, FEMALE, OTHERS}

    private Sex sex;
    private String displayName;
    private String username;
    private String email;
    private String userId;
    private String bio;
    private String number;
    String joinedOn;
    private Date birthdate;
    private ArrayList<String> eventIds;

    public Date getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(Date birthdate) {
        this.birthdate = birthdate;
    }

    public void setSex(Sex sex) {
        this.sex = sex;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public Sex getSex() {
        return sex;
    }

    public String getJoinedOn() {
        return joinedOn;
    }

    public void setJoinedOn(String joinedOn) {
        this.joinedOn = joinedOn;
    }

    public ArrayList<String> getEventIds() {
        return eventIds;
    }

    public void setEventIds(ArrayList<String> eventIds) {
        this.eventIds = eventIds;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }


}
