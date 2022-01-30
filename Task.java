package com.resolute;

import java.util.Date;

public class Task {

    int taskID;

    public String getTaskName(){
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public Date getDeadlineDate(){
        return deadlineDate;
    }

    public void setDeadlineDate(Date deadlineDate){
        this.deadlineDate = deadlineDate;
    }

    public Long getTargetTime(){
        return targetTime;
    }

    public void setTargetTime(Long targetTime){
        this.targetTime = targetTime;
    }

    public void setHours(int Hs){
        this.hours = Hs;
    }

    public void setMins(int mins){
//        this.minutes = (int) (((getTargetTime()/1000)/60)%60);
        this.minutes = mins;
    }

    public int getHours(){
        return hours;
    }

    public int getMinutes(){
        return minutes;
    }

    public Long getDailyStartTime(){
        return dailyStartTime;
    }

    public void setDailyStartTime(Long targetTime){
        this.dailyStartTime = targetTime;
    }

    String taskName;
    Date deadlineDate;
    Long dailyStartTime;
    Long targetTime;
    int hours;
    int minutes;

    public Task(String taskName, Date deadlineDate, Long targetTime, Long dailyStartTime) {
        this.taskName = taskName;
        this.deadlineDate = deadlineDate;
        this.targetTime = targetTime;
        this.dailyStartTime = dailyStartTime;
    }

}
