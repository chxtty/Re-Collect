package com.example.re_collectui;
import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.Date;
public class Patient implements Serializable {


        // --- Private Fields ---
        private int patientID;           // AUTO_INCREMENT, Primary Key
        private int careGiverID;         // Foreign key, must exist in Caregiver table
        private String firstName;
        private String lastName;
        private String DoB;                 // Date of Birth
        private String contactNumber;
        private String diagnosis;
        private String emergencyContact;
        private String userImage;        // Can be null
        private String email;
        private String patientPassword;

        // --- Constructors ---
        public Patient() {
            // Empty constructor (needed for frameworks like Firebase or Retrofit)
        }

    public static Patient fromJson(org.json.JSONObject o) throws org.json.JSONException {
        Patient p = new Patient();
        p.patientID = o.getInt("patientID");
        p.careGiverID = o.getInt("careGiverID");
        p.firstName = o.getString("firstName");
        p.lastName = o.getString("lastName");
        p.DoB = o.getString("DoB");
        p.contactNumber = o.getString("contactNumber");
        p.diagnosis = o.getString("diagnosis");
        p.emergencyContact = o.getString("emergencyContact");
        p.userImage = o.isNull("userImage") ? null : o.getString("userImage");
        p.email = o.getString("email");
        p.patientPassword = o.getString("patientPassword");
        return p;
    }

//        public Patient(int careGiverID, String firstName, String lastName, String DoB,
//                       String contactNumber, String diagnosis, String emergencyContact,
//                       String userImage, String email, String patientPassword) {
//            this.careGiverID = careGiverID;
//            this.firstName = firstName;
//            this.lastName = lastName;
//            this.DoB = DoB;
//            this.contactNumber = contactNumber;
//            this.diagnosis = diagnosis;
//            this.emergencyContact = emergencyContact;
//            this.userImage = userImage;
//            this.email = email;
//            this.patientPassword = patientPassword;
//        }

    public Patient(int patientID, int careGiverID, String firstName, String lastName, String DoB,
                   String contactNumber, String diagnosis, String emergencyContact,
                   String userImage, String email, String patientPassword) {
            this.patientID=patientID;
        this.careGiverID = careGiverID;
        this.firstName = firstName;
        this.lastName = lastName;
        this.DoB = DoB;
        this.contactNumber = contactNumber;
        this.diagnosis = diagnosis;
        this.emergencyContact = emergencyContact;
        this.userImage = userImage;
        this.email = email;
        this.patientPassword = patientPassword;
    }

        // --- Getters and Setters ---

        public int getPatientID() {
            return patientID;
        }

        public void setPatientID(int patientID) {
            this.patientID = patientID;
        }

        public int getCareGiverID() {
            return careGiverID;
        }

        public void setCareGiverID(int careGiverID) {
            this.careGiverID = careGiverID;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getDoB() {
            return DoB;
        }

        public void setDoB(String DoB) {
            this.DoB = DoB;
        }

        public String getContactNumber() {
            return contactNumber;
        }

        public void setContactNumber(String contactNumber) {
            this.contactNumber = contactNumber;
        }

        public String getDiagnosis() {
            return diagnosis;
        }

        public void setDiagnosis(String diagnosis) {
            this.diagnosis = diagnosis;
        }

        public String getEmergencyContact() {
            return emergencyContact;
        }

        public void setEmergencyContact(String emergencyContact) {
            this.emergencyContact = emergencyContact;
        }

        public String getImage() {
            return userImage;
        }

        public void setImage(String userImage) {
            this.userImage = userImage;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPatientPassword() {
            return patientPassword;
        }

        public void setPatientPassword(String patientPassword) {
            this.patientPassword = patientPassword;
        }

        // --- ToString (optional for debugging) ---
        @NonNull
        @Override
        public String toString() {
            return "Patient{" +
                    "patientID=" + patientID +
                    ", careGiverID=" + careGiverID +
                    ", firstName='" + firstName + '\'' +
                    ", lastName='" + lastName + '\'' +
                    ", DoB=" + DoB +
                    ", contactNumber='" + contactNumber + '\'' +
                    ", diagnosis='" + diagnosis + '\'' +
                    ", emergencyContact='" + emergencyContact + '\'' +
                    ", userImage='" + userImage + '\'' +
                    ", email='" + email + '\'' +
                    ", patientPassword='" + patientPassword + '\'' +
                    '}';
        }
    }


