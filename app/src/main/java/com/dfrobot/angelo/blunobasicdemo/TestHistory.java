// === TestHistory.java ===
package com.dfrobot.angelo.blunobasicdemo;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class TestHistory {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String username;
    public String bacValue;
    public String location;
    public String timestamp;
}
