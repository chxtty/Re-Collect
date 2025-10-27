package com.example.re_collectui;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class Community_Member implements Serializable {

    private int commID;
    private int patientID;
    private String commFirstName;
    private String commLastName;
    private String commType;
    private String commDescription;
    private String commCuteMessage;
    private String commImage;

    public Community_Member() {
    }

    public Community_Member(int commID, int patientID, String commFirstName, String commLastName,
                            String commType, String commDescription, String commCuteMessage, String commImage) {
        this.commID = commID;
        this.patientID = patientID;
        this.commFirstName = commFirstName;
        this.commLastName = commLastName;
        this.commType = commType;
        this.commDescription = commDescription;
        this.commCuteMessage = commCuteMessage;
        this.commImage = commImage;
    }

    public int getCommID() {
        return commID;
    }

    public void setCommID(int commID) {
        this.commID = commID;
    }

    public int getPatientID() {
        return patientID;
    }

    public void setPatientID(int patientID) {
        this.patientID = patientID;
    }

    public String getCommFirstName() {
        return commFirstName;
    }

    public void setCommFirstName(String commFirstName) {
        this.commFirstName = commFirstName;
    }

    public String getCommLastName() {
        return commLastName;
    }

    public void setCommLastName(String commLastName) {
        this.commLastName = commLastName;
    }

    public String getCommType() {
        return commType;
    }

    public void setCommType(String commType) {
        this.commType = commType;
    }

    public String getCommDescription() {
        return commDescription;
    }

    public void setCommDescription(String commDescription) {
        this.commDescription = commDescription;
    }

    public String getCommCuteMessage() {
        return commCuteMessage;
    }

    public void setCommCuteMessage(String commCuteMessage) {
        this.commCuteMessage = commCuteMessage;
    }

    public String getCommImage() {
        return commImage;
    }

    public void setCommImage(String commImage) {
        this.commImage = commImage;
    }

    @Override
    public String toString() {
        return "CommunityMember{" +
                "commID=" + commID +
                ", patientID=" + patientID +
                ", commFirstName='" + commFirstName + '\'' +
                ", commLastName='" + commLastName + '\'' +
                ", commType='" + commType + '\'' +
                '}';
    }

    public static Community_Member fromJson(JSONObject jsonObject) throws JSONException {
        Community_Member member = new Community_Member();

        member.commID = jsonObject.getInt("commID");
        member.patientID = jsonObject.getInt("patientID");
        member.commFirstName = jsonObject.getString("commFirstName");
        member.commType = jsonObject.getString("commType");
        member.commDescription = jsonObject.getString("commDescription");
        member.commCuteMessage = jsonObject.getString("commCuteMessage");

        member.commLastName = jsonObject.isNull("commLastName") ? null : jsonObject.getString("commLastName");
        member.commImage = jsonObject.isNull("commImage") ? null : jsonObject.getString("commImage");

        return member;
    }
}