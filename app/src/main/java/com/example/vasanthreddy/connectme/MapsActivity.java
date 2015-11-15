package com.example.vasanthreddy.connectme;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
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
import java.io.Serializable;
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
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    int x=0;
    int y;
    private LatLng mypos;
    private int colind=0;
    private  Map<String, Marker> cur_markers = new ConcurrentHashMap<String, Marker>();
    private  Map<String, MarkerOptions> new_markers = new ConcurrentHashMap<String, MarkerOptions>();
    private  Map<String , UserData> servHash=new ConcurrentHashMap<String, UserData>();
    private  BitmapDescriptor[] markcol;
    Marker marker;
    String UID;
    String name;
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


    public Location getLastKnownLocation() {
        LocationManager mLocationManager=(LocationManager) getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = mLocationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            Location l = mLocationManager.getLastKnownLocation(provider);
            //Log.d("last known location, provider: %s, location: ", provider);

            if (l == null) {
                continue;
            }
            if (bestLocation == null
                    || l.getAccuracy() < bestLocation.getAccuracy()) {
              //  Log.d("found best last known location: ","n ");
                bestLocation = l;
            }
        }
        if (bestLocation == null) {
            return null;
        }
        return bestLocation;
    }

    private void setUpMap() {
        sharedpreferences= PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        UID=sharedpreferences.getString("UID", null);
        name=sharedpreferences.getString("name",null);
        mypos=new LatLng(0,0);
        mMap.setMyLocationEnabled(true);
        LocationManager lmanage=(LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = lmanage.getBestProvider(criteria, true);
        Log.e("Test","Test logline") ;
        Thread t=new Thread(new communicate("172.16.11.241",8080));
        t.start();
        lmanage.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                // TODO Auto-generated method stub
                mypos = new LatLng(location.getLatitude(), location.getLongitude());
               // Toast.makeText(MapsActivity.this, "Your Location changed to ("+mypos.latitude+","+mypos.longitude+")", Toast.LENGTH_LONG).show();
                mMap.moveCamera(CameraUpdateFactory.newLatLng(mypos));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(10));
                if(x==0){
                marker=mMap.addMarker(new MarkerOptions().position(mypos).title("My updated Location"));
                x++;}
                else {
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

    }
    public void updateHash()
    {
        new_markers=new ConcurrentHashMap<>();
        for(String uid :servHash.keySet())
        {
            UserData ud=servHash.get(uid);
            if(ud.isalive)
            {
                LatLng pos = new LatLng(ud.lat, ud.lng);
                MarkerOptions mop = new MarkerOptions()
                        .position(pos).title(ud.name)
                        .icon(markcol[getColid(uid)])
                        .title(name);
                new_markers.put(uid, mop);
            }

        }

    }

    public void updatemarkers()
    {
        for(String uid :new_markers.keySet())
        {
            if(!uid.equals(UID)) {
                if (cur_markers.containsKey(uid)) {
                    LatLng newpos = new_markers.get(uid).getPosition();
                    cur_markers.get(uid).setPosition(newpos);
                }
                else {
                    Marker nm = mMap.addMarker(new_markers.get(uid));
                    cur_markers.put(uid, nm);
                }
            }
        }
        for(String uid:cur_markers.keySet())
        {
            if(!new_markers.containsKey(uid))
            {
                cur_markers.get(uid).remove();
                cur_markers.remove(uid);
            }
        }

    }

    public static class ServData implements Serializable{
        public ServData(HashMap<String ,UserData> x)
        {
            System.out.println("Created object");
            hashlist=x;
            unixTime = System.currentTimeMillis() / 1000L;
        }
        long unixTime;
        Map<String ,UserData> hashlist;
    }

    public class UserData implements Serializable {

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
                try {
                    if(client!=null)
                    {
                        Log.e("Hey","Still waiting") ;
                        Log.e("client","client ip "+client.getInetAddress().toString());
                        iStream = client.getInputStream();
                    ObjectInputStream oiStream = new ObjectInputStream(iStream);

                    servHash=(HashMap<String,UserData>)oiStream.readObject();
                    updateHash();
                    updatemarkers();
                    System.out.println( "Got from server***************");

                    Log.e("Hey","Got msg from server") ;
                    }
                    else
                    {
                        Log.e("err","failed failed");
                    }

                } catch (Exception e)
                {
                    Log.e("Hey","Exception found vasanth");
                    Log.e("Exc", e.toString());
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
