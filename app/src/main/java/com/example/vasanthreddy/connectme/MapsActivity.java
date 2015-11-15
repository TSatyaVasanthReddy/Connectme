package com.example.vasanthreddy.connectme;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

public class MapsActivity extends FragmentActivity {
    SharedPreferences sharedpreferences;
    static GoogleMap mMap; // Might be null if Google Play services APK is not available.
    int x=0;
    int y;
    static private LatLng mypos;
    private int colind=0;
    static LinearLayout l;
    static LinearLayout.LayoutParams lp;
    static  Map<String, Marker> cur_markers ;
    static  Map<String, LinkedList> new_markers;
    static  Map<String , LinkedList> servHash;
    static private  BitmapDescriptor[] markcol;
    static Marker marker;
    static String UID;
    static String name;
    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        MapsInitializer.initialize(getApplicationContext());
        markcol= new BitmapDescriptor[]{BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE),
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE),
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN),
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN),
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA),
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE),
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED),
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE),
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET),
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)};
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }


    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }



    private void setUpMap() {
        sharedpreferences= getSharedPreferences("my_pref", MODE_PRIVATE);
       // UID=sharedpreferences.getString("UID", "123456");
        UID="Tej";

        l=(LinearLayout)findViewById(R.id.lin_sc);
        servHash=new HashMap<String, LinkedList>();
        new_markers = new HashMap<String,LinkedList>();
        cur_markers = new HashMap<String, Marker>();
        lp=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        name="Tej";
       // name=sharedpreferences.getString("name","vasanth");
        mypos=new LatLng(0,0);
        mMap.setMyLocationEnabled(true);
        LocationManager lmanage=(LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        Criteria criteria = new Criteria();
        String provider = lmanage.getBestProvider(criteria, true);
        Log.e("Test", "Test logline") ;
        Thread t=new Thread(new communicate("172.16.4.19",8080));
        t.start();
        //Timer t2=new Timer();
        //t2.schedule(new MarkUpdate(),5000,5000);
        Log.d("TTTTTT", "THANK GODD");
        lmanage.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, new LocationListener()
        {
            @Override
            public void onLocationChanged(Location location) {
                // TODO Auto-generated method stub
                updatemarkers();
                mypos = new LatLng(location.getLatitude(), location.getLongitude());
                Toast.makeText(MapsActivity.this, "Your Location changed to (" + mypos.latitude + "," + mypos.longitude + ")", Toast.LENGTH_LONG).show();
                mMap.moveCamera(CameraUpdateFactory.newLatLng(mypos));

                if (x == 0) {
                    marker = mMap.addMarker(new MarkerOptions().position(mypos).title("My updated Location"));
                    x++;
                } else {
                    marker.setPosition(mypos);
                }
            }

            @Override
            public void onProviderDisabled(String provider) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onProviderEnabled(String provider) {
                // TODO Auto-generated method stub
                Log.d("gfghgjgjgjgj ", provider + " provider enabled");
            }

            @Override
            public void onStatusChanged(String provider, int status,
                                        Bundle extras) {
                // TODO Auto-generated method stub
            }
        });

        Log.d("TTTTTT","THANK GODD");

    }
    public void updatemarkers()
    {
        Log.e("SERVHASH","UPDATING MARKERS");
        for (String uid : new_markers.keySet()) {
            Log.e("Markers", "UID:" + uid);
            if (!uid.equals(UID)) {
                LinkedList ud = new_markers.get(uid);
                LatLng pos = new LatLng((double) ud.get(2), (double) ud.get(3));
                MarkerOptions m = new MarkerOptions().title((String) ud.get(0))
                        .position(pos)
                        .icon(markcol[getColid((String) ud.get(1))]);
                Log.e("Markers", "Created new options");

                if (m == null)
                    Log.d("Markers", "Null Marker");
                else
                    Log.d("Markers", "Title:" + m.getTitle() + ";Lat:" + m.getPosition().latitude + ";Long:" + m.getPosition().longitude);

                if (cur_markers.containsKey(uid)) {
                    Log.d("Markers", "Already Here.So update marker");
                    cur_markers.get(uid).setPosition(pos);
                } else {
                    Log.d("Markers", "Adding new Marker");

                    Marker nm = mMap.addMarker(new MarkerOptions().title((String) ud.get(0))
                                    .position(pos)
                    );
                    cur_markers.put(uid, nm);
                }
            } else {
                Log.e("Markers", "Thats me");
            }
        }
        for (String uid : cur_markers.keySet()) {
            if (!new_markers.containsKey(uid)) {
                cur_markers.get(uid).remove();
                cur_markers.remove(uid);
            }
        }
    }
    public void updateHash()
    {
        Log.e("SERVHASH","UPDATING HASH");
        for (String uid:servHash.keySet())
        {
            Log.e("SERVHASH","ID"+servHash.get(uid).get(0)+";location lat="+servHash.get(uid).get(2));
        }
        new_markers=new HashMap<String,LinkedList>();
        for(String uid :servHash.keySet())
        {
            LinkedList ud=servHash.get(uid);
            if((boolean)ud.get(5))
            {
                Log.e("SERVHASH",servHash.get(uid).get(0)+" ALIVE");
                LatLng pos = new LatLng((double)ud.get(2),(double) ud.get(3));

                new_markers.put(uid,ud);
            }
            else
                Log.e("SERVHASH",servHash.get(uid).get(0)+"DEAD");
        }

    }


    public int getColid(String x)
    {
        int sum =0 ;
        for(int i=0;i<x.length();i++)
            sum+=x.charAt(i);
        return  sum%9;

    }

    public class communicate implements Runnable
    {

        InetAddress ip;
        int port;
        public  communicate(String servip,int servport)
        {
            try {
                ip=InetAddress.getByName(servip);
                port=servport;
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }
        @Override
        public void run() {

            Socket client = null;
            try {
                client = new Socket(ip, port);
                client.setKeepAlive(true);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Timer t=new Timer();
            t.schedule(new sendmylocationtoserver(client),0,5000);
            Looper.prepare();
            //Toast.makeText(MapsActivity.this, "Waiting for server", Toast.LENGTH_LONG).show();
            Log.e("msg","Waiting for server") ;

            int i=10;
            while(i-->0)
            {
                InputStream iStream;
                ObjectInputStream oiStream;
                if(client!=null)
                    {
                        Log.e("Hey", "Still waiting") ;
                        Log.e("client", "client ip " + client.getInetAddress().toString());
                        try {
                            iStream = client.getInputStream();
                            oiStream= new ObjectInputStream(iStream);
                            try {

                                if(oiStream==null)
                                    Log.e("oistream","null") ;
                                else {
                                    servHash = (HashMap<String, LinkedList>) oiStream.readObject();
                                    updateHash();
                                    //updatemarkers();
                                    System.out.println( "Got from server***************");

                                }
                            } catch (ClassNotFoundException e) {
                                Log.e("CnotF","Exception") ;
                                e.printStackTrace();
                            }

                        } catch (IOException e) {
                            Log.e("IOE","IO exception ") ;
                            e.printStackTrace();
                        }

}
                    else
                    {
                        Log.e("err","failed failed");
                    }


            }

        }
    }
    public class sendmylocationtoserver extends TimerTask
    {
        Socket sock;
        sendmylocationtoserver(Socket x)
        {
            sock=x;
        }
        public void run() {
            Log.e("Hey", "Entered timer task thread") ;
            LinkedList ud=new LinkedList();
            ud.add(name);
            ud.add(UID);
            ud.add(mypos.latitude);
            ud.add(mypos.longitude);
            ud.add(System.currentTimeMillis()/1000);

            OutputStream oStream;
            try {
                Log.e("Hey","preparing the outstream") ;
                if(sock!=null) {
                    oStream = sock.getOutputStream();
                    ObjectOutputStream ooStream = new ObjectOutputStream(oStream);
                    Log.e("Hey", "Writing ");

                    ooStream.writeObject(ud);  // send serilized payload
                    ooStream.flush();
                    Log.e("Hey", "yoo done");

                    //ooStream.close();
                }
                else
                {
                    Log.e("Sending", "Socket null to send");

                }

            } catch (IOException e) {
                e.printStackTrace();
            }
         }
    }

}
