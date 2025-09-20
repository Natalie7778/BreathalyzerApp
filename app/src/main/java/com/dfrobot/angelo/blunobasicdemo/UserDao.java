package com.dfrobot.angelo.blunobasicdemo;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface UserDao {
    @Insert
    void insert(User user);

    @Query("SELECT * FROM User WHERE username = :username LIMIT 1")
    User getUserByUsername(String username);

    @Query("SELECT * FROM User WHERE username = :username AND password = :password LIMIT 1")
    User login(String username, String password);

    @Query("UPDATE User SET rank = :newRank WHERE username = :username")
    void updateRank(String username, int newRank);

    @Query("SELECT * FROM User WHERE id = :id LIMIT 1")
    User getUserById(int id);
}
