package com.example.re_collectui;

public class Event {
    public String title;
    public int eventID;
    public String startDate;
    public String endDate;
    public String description;
    public String location;

    public boolean allDay;

    public Event(int eventID, String title, String startDate, String endDate, String description, String location, boolean allDay) {
        this.eventID = eventID;
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.description = description;
        this.location = location;
        this.allDay = allDay;

    }
    public boolean getAllDay() {
        return allDay;
    }

    public String getTitle() {
        return title;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public String getDescription() {
        return description;
    }

    public String getLocation() {
        return location;
    }

    //for the ui
    private boolean isExpanded = false;

    public int getEventID() {
        return eventID;
    }


    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }
}
