package com.fidanoglu.malafatus;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.time.ZonedDateTime;


@Entity
public class Session {
    @PrimaryKey(autoGenerate = true)
    private long id;

    private String startTimeValue;

    private long duration;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getStartYear() {
        return ZonedDateTime.parse(startTimeValue).getYear();
    }

    public int getStartMonth() {
        return ZonedDateTime.parse(startTimeValue).getMonthValue();
    }

    public int getStartDay() {
        return ZonedDateTime.parse(startTimeValue).getDayOfMonth();
    }

    public int getStartHour() {
        return ZonedDateTime.parse(startTimeValue).getHour();
    }

    public int getStartMinute() {
        return ZonedDateTime.parse(startTimeValue).getMinute();
    }

    public String getStartTimeValue() {
        return startTimeValue;
    }

    public String getFullStartTime() {
        return startTimeValue;
    }

    public long getDuration() {
        return duration;
    }

    public void setStartTimeValue(String startTimeValue) {
        this.startTimeValue = startTimeValue;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    @Override
    public String toString() {
        return "Session{" + "startTimeValue=" + startTimeValue + ", duration=" + duration + "}";
    }
}
