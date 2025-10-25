package com.example.re_collectui;

import org.json.JSONObject;
import java.io.Serializable; // Import this

public class Care_giver implements Serializable { // Implement this

    private int careGiverID;
    private String firstName;
    private String lastName;
    private String DoB;
    private String contactNumber;
    private String workNumber;
    private String employerType;
    private String userImage;
    private String email;
    private String caregiverPassword;

    public static Care_giver fromJson(JSONObject jsonObject) {
        Care_giver caregiver = new Care_giver();
        caregiver.careGiverID = jsonObject.optInt("careGiverID");
        caregiver.firstName = jsonObject.optString("firstName");
        caregiver.lastName = jsonObject.optString("lastName");
        caregiver.DoB = jsonObject.optString("DoB");
        caregiver.contactNumber = jsonObject.optString("contactNumber");
        caregiver.workNumber = jsonObject.optString("workNumber");
        caregiver.employerType = jsonObject.optString("employerType");
        caregiver.userImage = jsonObject.optString("userImage");
        caregiver.email = jsonObject.optString("email");
        caregiver.caregiverPassword = jsonObject.optString("caregiverPassword");
        return caregiver;
    }

    // --- Getters ---
    public int getCareGiverID() { return careGiverID; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getDoB() { return DoB; }
    public String getContactNumber() { return contactNumber; }
    public String getWorkNumber() { return workNumber; }
    public String getEmployerType() { return employerType; }
    public String getUserImage() { return userImage; }
    public String getEmail() { return email; }
    public String getCaregiverPassword() { return caregiverPassword; }
}