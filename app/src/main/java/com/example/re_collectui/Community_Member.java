package com.example.re_collectui;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class Community_Member implements Serializable {

    // --- Fields ---
    private int commID;
    private int patientID;
    private String commFirstName;
    private String commLastName;
    private String commType;
    private String commDescription;
    private String commCuteMessage;
    private String commImage; // Stores the path or URL to the image

    // --- Constructors ---

    /**
     * Default constructor.
     */
    public Community_Member() {
    }

    /**
     * Constructor to initialize all fields.
     * @param commID Unique identifier for the community member.
     * @param patientID Identifier for the related patient.
     * @param commFirstName First name of the community member.
     * @param commLastName Last name of the community member.
     * @param commType The type or role of the member (e.g., "Friend", "Family").
     * @param commDescription A description of the member.
     * @param commCuteMessage A special message from the member.
     * @param commImage A string representing the image URL or file path.
     */
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


    // --- Getters and Setters ---

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

    // --- toString Method ---

    /**
     * Returns a string representation of the CommunityMember object, useful for logging and debugging.
     */
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

        // Assign values from the JSONObject to the object's fields
        member.commID = jsonObject.getInt("commID");
        member.patientID = jsonObject.getInt("patientID");
        member.commFirstName = jsonObject.getString("commFirstName");
        member.commType = jsonObject.getString("commType");
        member.commDescription = jsonObject.getString("commDescription");
        member.commCuteMessage = jsonObject.getString("commCuteMessage");

        // Handle potentially null fields
        member.commLastName = jsonObject.isNull("commLastName") ? null : jsonObject.getString("commLastName");
        member.commImage = jsonObject.isNull("commImage") ? null : jsonObject.getString("commImage");

        return member;
    }
}
