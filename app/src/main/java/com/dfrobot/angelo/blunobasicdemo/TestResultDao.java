package com.dfrobot.angelo.blunobasicdemo;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface TestResultDao {
    @Insert
    void insert(TestResult result);

    @Query("SELECT * FROM TestResult WHERE username = :username ORDER BY id DESC")
    List<TestResult> getAllResultsForUser(String username);
}
