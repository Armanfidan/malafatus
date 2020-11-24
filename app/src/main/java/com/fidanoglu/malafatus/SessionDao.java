package com.fidanoglu.malafatus;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.time.ZonedDateTime;
import java.util.List;

@Dao
public interface SessionDao {
    @Insert
    void insert(Session... sessions);

    @Query("SELECT * FROM Session")
    List<Session> fetchAllData();

    @Query("SELECT * FROM Session WHERE startTimeValue =:date")
    Session getSessionsFromDate(String date);

    @Update
    void update(Session... sessions);

    @Delete
    void delete(Session... sessions);

    @Query("DELETE FROM Session")
    public void clear();


}
