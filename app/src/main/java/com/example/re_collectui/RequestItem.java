package com.example.re_collectui;

import java.io.Serializable;

public class RequestItem implements Serializable {
    public enum RequestType { ACTIVITY, COMMUNITY }
    private int id;
    private RequestType type;
    private int patientID;
    private int careGiverID;
    private String status;
    private String author;
    private String name;
    private String actType;
    private String actDescription;
    private String commType;
    private String commFirstName;
    private String commLastName;
    private String commDescription;
    private String commCuteMessage;
    private String commImage;

    public RequestItem(int id, RequestType type, int patientID, int careGiverID, String status,
                       String author, String name, String actType, String actDescription,
                       String commType, String commFirstName, String commLastName,
                       String commDescription, String commCuteMessage, String commImage) {
        this.id = id;
        this.type = type;
        this.patientID = patientID;
        this.careGiverID = careGiverID;
        this.status = status;
        this.author = author;
        this.name = name;
        this.actType = actType;
        this.actDescription = actDescription;
        this.commType = commType;
        this.commFirstName = commFirstName;
        this.commLastName = commLastName;
        this.commDescription = commDescription;
        this.commCuteMessage = commCuteMessage;
        this.commImage = commImage;
    }
    public int getId() { return id; }
    public RequestType getType() { return type; }
    public int getPatientID() { return patientID; }
    public int getCareGiverID() { return careGiverID; }
    public String getStatus() { return status; }

    public String getName() {
        return type == RequestType.ACTIVITY ? actType : commFirstName + " " + commLastName;
    }

    public String getAuthor() { return author; }

    public String getActType() { return actType; }
    public String getActDescription() { return actDescription; }

    public String getCommType() { return commType; }
    public String getCommFirstName() { return commFirstName; }
    public String getCommLastName() { return commLastName; }
    public String getCommDescription() { return commDescription; }
    public String getCommCuteMessage() { return commCuteMessage; }
    public String getCommImage() { return commImage; }
}
