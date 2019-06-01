package com.theindiecorp.mango.data;

import java.util.ArrayList;

public class Comment {
    private String userId;
    private  String message;
    private ArrayList<Comment> replies;

    public Comment() {
    }

    public Comment(String userId, String message, ArrayList<Comment> replies) {
        this.userId = userId;
        this.message = message;
        this.replies = replies;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ArrayList<Comment> getReplies() {
        return replies;
    }

    public void setReplies(ArrayList<Comment> replies) {
        this.replies = replies;
    }
}
