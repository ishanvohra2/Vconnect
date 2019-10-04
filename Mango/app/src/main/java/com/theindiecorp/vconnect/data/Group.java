package com.theindiecorp.vconnect.data;

import java.util.ArrayList;

public class Group {
    private String name;
    private String url;
    private String groupDescription;
    private String adminId;
    private String id;
    private ArrayList<String> members;
    private int maximumNumberOfMembers;

    public int getMaximumNumberOfMembers() {
        return maximumNumberOfMembers;
    }

    public void setMaximumNumberOfMembers(int maximumNumberOfMembers) {
        this.maximumNumberOfMembers = maximumNumberOfMembers;
    }

    public ArrayList<String> getMembers() {
        return members;
    }

    public void setMembers(ArrayList<String> members) {
        this.members = members;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getGroupDescription() {
        return groupDescription;
    }

    public void setGroupDescription(String groupDescription) {
        this.groupDescription = groupDescription;
    }

    public String getAdminId() {
        return adminId;
    }

    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }
}
