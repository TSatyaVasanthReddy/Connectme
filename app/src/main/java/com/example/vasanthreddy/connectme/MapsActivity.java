package com.example.vasanthreddy.connectme;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.widget.TextView;
import com.facebook.*;
import android.content.*;
import com.example.vasanthreddy.connectme.UserData;
import android.util.*;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.*;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.CameraUpdateFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

public class MapsActivity extends FragmentActivity {
    SharedPreferences sharedpreferences;
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    int x=0;
    private LatLng mypos;
    private int colind=0;
    private  Map<String, Marker> cur_markers = new ConcurrentHashMap<String, Marker>();
    private  Map<String, MarkerOptions> new_markers = new ConcurrentHashMap<String, MarkerOptions>();
    private  Map<String , UserData> servHash=new ConcurrentHashMap<String, UserData>();
    private  BitmapDescriptor[] markcol;
    Marker marker;
    String UID;
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

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
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

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
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
    public String getbestLocationProvider() {
        LocationManager mLocationManager=(LocationManager) getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = mLocationManager.getProviders(true);
        Location bestLocation = null;
        String bestprovider=null;
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
                bestprovider=provider;
            }
        }
        if (bestLocation == null) {
            return null;
        }
        return bestprovider;
    }
    private void setUpMap() {

        mMap.setMyLocationEnabled(true);
        LocationManager lmanage=(LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = lmanage.getBestProvider(criteria, true);
        lmanage.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                // TODO Auto-generated method stub
                mypos = new LatLng(location.getLatitude(), location.getLongitude());
                Toast.makeText(MapsActivity.this, "Your Location changed to ("+mypos.latitude+","+mypos.longitude+")", Toast.LENGTH_LONG).show();
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
    public void sendLocation()
    {
        sharedpreferences=getSharedPreferences("my_pref",Context.MODE_PRIVATE);
        UID=sharedpreferences.getString("UID",null);

    }
    public void updateHash()
    {
        new_markers=new ConcurrentHashMap<>();
        for(String uid :servHash.keySet())
        {
            UserData ud=servHash.get(uid);
            LatLng pos=new LatLng(ud.lat,ud.lng);
            MarkerOptions mop=new MarkerOptions()
                    .position(pos).title(ud.name)
                    .icon(markcol[getColid(uid)]);
            new_markers.put(uid,mop);

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
                } else {
                    Marker nm = mMap.addMarker(new_markers.get(uid));
                    cur_markers.put(uid, nm);
                }
            }
        }
        for(String uid:cur_markers.keySet())
        {
            if(!new_markers.containsKey(uid))
            {
                cur_markers.get(uid).setVisible(false);
            }
        }

    }
    public int getColid(String x)
    {
        int sum =0 ;
        for(int i=0;i<x.length();i++)
            sum+=x.charAt(i);
        return  sum%9;

    }
    public void del_marker(String UID)
    {

    }
    public class communicate implements Runnable{

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
            } catch (IOException e) {
                e.printStackTrace();
            }
            Timer t=new Timer();
            t.schedule(new sendmylocationtoserver(client),2000,5000);
            while(true)
            {
                InputStream iStream;
                try {
                    iStream = client.getInputStream();
                    ObjectInputStream oiStream = new ObjectInputStream(iStream);
                    ServData sd=(ServData)oiStream.readObject();
                    servHash=sd.hashlist;
                    updateHash();
                    updatemarkers();

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
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
            UserData ud=new UserData(mypos.latitude,mypos.longitude,"name",UID,System.currentTimeMillis()/1000,true);
            OutputStream oStream;
            try {
                oStream = sock.getOutputStream();
                ObjectOutputStream ooStream = new ObjectOutputStream(oStream);
                ooStream.writeObject(ud);  // send serilized payload
                ooStream.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
         }
    }
}
