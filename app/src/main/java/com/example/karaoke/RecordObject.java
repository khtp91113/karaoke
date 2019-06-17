package com.example.karaoke;

import org.json.JSONArray;

public class RecordObject {
    public int UID;
    public String Name;
    public String Artist;
    public String timeStamp;
    public String userName;

    public RecordObject() {
    }
    public void setUID(int UID){
        this.UID = UID;
    }
    public void setName(String name){
        String[] tmp = name.split("-");
        this.Artist = tmp[0];
        this.Name = tmp[1];
    }
    public void setTimeStamp(String timestamp){
        this.timeStamp = timestamp;
    }
    public void setUserName(String userName){
        this.userName = userName;
    }
}
