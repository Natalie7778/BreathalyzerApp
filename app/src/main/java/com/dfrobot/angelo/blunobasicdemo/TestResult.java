package com.dfrobot.angelo.blunobasicdemo;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class TestResult {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String username;
    public String bacValue;
    public String estimateTime;
    public String locationInfo;
    public String timestamp;
}
