package com.example.vasanthreddy.connectme;

/**
 * Created by vasanthreddy on 13/11/15.
 */

import java.util.HashMap;
import java.util.Map;

public class ServData {
    public ServData()
    {
        hashlist=new HashMap<String ,UserData>();
        unixTime = System.currentTimeMillis() / 1000L;
    }
    long unixTime;
    Map<String ,UserData> hashlist;
}
