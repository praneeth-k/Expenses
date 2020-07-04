package com.example.expenses;

import java.util.Map;

public class GroupDataModel {
    String key;
    String userId;
    String userName;
    String desc;
    String value;
    String timestamp;
    public GroupDataModel()
    {

    }
    public GroupDataModel(String key, String userId, String userName, String desc, String value, String timestamp)
    {
        this.key = key;
        this.userId = userId;
        this.userName = userName;
        this.desc = desc;
        this.value = value;
        this.timestamp = timestamp;
    }
    public String getKey() {return  key;}
    public String getUserId()
    {
        return userId;
    }
    public String getUserName()
    {
        return userName;
    }
    public String getDesc()
    {
        return desc;
    }
    public String getValue()
    {
        return value;
    }
    public String getTimestamp()
    {
        return  timestamp;
    }
}
