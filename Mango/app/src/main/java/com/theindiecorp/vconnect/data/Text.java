package com.theindiecorp.vconnect.data;

public class Text {
    String content,date,time,sentBy,postId;
    Boolean read, isPost;

    public Boolean getPost() {
        return isPost;
    }

    public void setPost(Boolean post) {
        isPost = post;
    }

    public Boolean getRead() {
        return read;
    }

    public void setRead(Boolean read) {
        this.read = read;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getSentBy() {
        return sentBy;
    }

    public void setSentBy(String sentBy) {
        this.sentBy = sentBy;
    }
}
