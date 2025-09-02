package com.example.re_collectui;

public class Activity {
    private int activityId, patientId;
    private String StartTime, EndTime, actDate;

    public Activity(int activityId, int patientId, String startTime, String endTime, String actDate) {
        this.activityId = activityId;
        this.patientId = patientId;
        StartTime = startTime;
        EndTime = endTime;
        this.actDate = actDate;
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
}
