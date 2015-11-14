package com.example.vasanthreddy.connectme;

/**
 * Created by vasanthreddy on 13/11/15.
 */
public class UserData {

    public UserData()
    {
        lat=0;
        lng=0;
        uid=null;
        isalive=false;
    }
    public UserData(double lat , double lng , String uid , String name , long Unixtime , boolean isalive)
    {
        this.lat = lat;
        this.lng = lng;
        this.uid = uid;
        this.name = name;
        this.Unixtime = Unixtime;
        this.isalive = isalive;
    }

    public double lat;
    public double lng;
    public String uid;
    public String name;
    public long Unixtime;
    public boolean isalive;
}
