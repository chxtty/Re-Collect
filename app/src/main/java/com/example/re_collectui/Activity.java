package com.example.re_collectui;

public class Activity {


    private int detailId, activityId, patientId;
    private String StartTime, EndTime, actDate, actIconBase64;
    private boolean isExpanded = false;

    public Activity(int detailId, int activityId, int patientId, String startTime, String endTime, String actDate, String actIconBase64) {
        this.detailId = detailId;
        this.activityId = activityId;
        this.patientId = patientId;
        this.StartTime = startTime;
        this.EndTime = endTime;
        this.actDate = actDate;
        this.actIconBase64 = actIconBase64;
    }

    public Activity(String actDate, int activityId){
        this.actDate = actDate;
        this.activityId = activityId;
    }

    public int getActivityId() {
        return activityId;
    }

    public int getPatientId() {
        return patientId;
    }

    public String getStartTime() {
        return StartTime;
    }

    public String getEndTime() {
        return EndTime;
    }

    public String getActDate() {
        return actDate;
    }
    public int getDetailId() {return detailId;}
    public String getActIconBase64() {return  actIconBase64;}

    public boolean isExpanded() {return isExpanded;}

    public void setActivityId(int activityId) {
        this.activityId = activityId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public void setStartTime(String startTime) {
        StartTime = startTime;
    }

    public void setEndTime(String endTime) {
        EndTime = endTime;
    }

    public void setActDate(String actDate) {
        this.actDate = actDate;
    }

    public void setDetailId(int detailId) {
        this.detailId = detailId;
    }

    public void setActIconBase64(String actIconBase64) {
        this.actIconBase64 = actIconBase64;
    }
    public void setExpanded(boolean expanded) {isExpanded = expanded;}
}
