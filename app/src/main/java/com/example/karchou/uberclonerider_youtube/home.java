package com.example.karchou.uberclonerider_youtube;

import android.Manifest;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Response;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


import com.example.karchou.uberclonerider_youtube.Common.Common;
import com.example.karchou.uberclonerider_youtube.Remote.iGoogleAPI;

import retrofit2.Call;
import retrofit2.Callback;


public class home extends FragmentActivity implements OnMapReadyCallback,NavigationDrawerFragment.NavigationDrawerCallbacks{

    GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;
    LatLng userlocation;
    public FirebaseDatabase database;
    double currentlat,currentlong;
    private LatLng startposition,endposition,currentposition;
    private Location mlocation;
    private LocationRequest mLocationRequest;
    DatabaseReference riders,dbrequest;
    private GeoFire geoFire,mgeoFire;
    private GoogleApiClient mGoogleAPIclinet;
    Marker mCurrent,carMarker,mUsermarker;
    private static final int MY_PERMISSION_REQUEST_CODE= 7000;
    private static final int PLAY_SERVICE_RES_REQUEST=7001;
    private Button btnpickuprequest;
    private static int UPDATE_INTERVAL=5000;
    private static int FASTES_INTERVAL=3000;
    private static int DISPLACEMENT=10;

    private List<LatLng> polylinelist;
    private float v;
    private double lat,lng;
    private Handler handler;
    private int index,next;
    private String pickupoint,destination;
    private PolylineOptions polylineOptions, blackPolylineOptions;
    private Polyline blackPolyline, greyPolyline;
    private iGoogleAPI mService;

    FragmentTransaction transaction1,transaction2,transaction3;
    MapFragment mapFragment;
    PlaceAutocompleteFragment framentplacespickup,fragmentplacesdrop;

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;


    Runnable drawPathRunnable=new Runnable() {
        @Override
        public void run() {
            if(index<polylinelist.size()-1) {
                index++;
                next=index+1;
            }
            if (index<polylinelist.size()-1) {
                startposition=polylinelist.get(index);
                endposition=polylinelist.get(next);
            }

            final ValueAnimator valueAnimator=ValueAnimator.ofFloat(0,1);
            valueAnimator.setDuration(3000);
            valueAnimator.setInterpolator(new LinearInterpolator());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    v=valueAnimator.getAnimatedFraction();
                    lng=v*endposition.longitude+(1-v)*startposition.longitude;
                    lat=v*endposition.latitude +(1-v)*startposition.latitude;
                    LatLng newPos=new LatLng(lat,lng);
                    carMarker.setPosition(newPos);
                    carMarker.setAnchor(0.5f,0.5f);
                    carMarker.setRotation(getBearing(startposition,newPos));
                    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(newPos)
                                    .zoom(15.5f)
                                    .build()
                    ));
                }
            });
            valueAnimator.start();
            handler.postDelayed(this,3000);
        }
    };

    private float getBearing(LatLng startposition, LatLng endPosition) {
        double lat=Math.abs(startposition.latitude-endPosition.latitude);
        double lon=Math.abs(startposition.longitude-endPosition.longitude);

        if (startposition.latitude<endPosition.latitude && startposition.longitude<endPosition.longitude)
            return (float) (Math.toDegrees(Math.atan(lon/lat)));
        else if (startposition.latitude>=endPosition.latitude && startposition.longitude<endPosition.longitude)
            return (float) ((90-Math.toDegrees(Math.atan(lon/lat)))+90);
        else if (startposition.latitude>=endPosition.latitude && startposition.longitude>=endPosition.longitude)
            return (float) (Math.toDegrees(Math.atan(lon/lat))+180);
        else if (startposition.latitude<endPosition.latitude && startposition.longitude>=endPosition.longitude)
            return (float) ((90-Math.toDegrees(Math.atan(lon/lat)))+270);
        return -1;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        riders= FirebaseDatabase.getInstance().getReference("Riders");


        mapFragment = new MapFragment();
        transaction1 =getFragmentManager().beginTransaction();
        transaction1.replace(R.id.map, mapFragment).commit();
        mapFragment.getMapAsync(this);
        /*  MapFragment mapFragment=(MapFragment) getFragmentManager().findFragmentById(R.id.map); */


        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        btnpickuprequest=(Button)findViewById(R.id.btnnpickuprequest);
        btnpickuprequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             requestpickuphere(FirebaseAuth.getInstance().getCurrentUser().getUid());
            }
        });

        startlocationUpdates();
        polylinelist=new ArrayList<>();

        framentplacespickup=new PlaceAutocompleteFragment();
        transaction2 =getFragmentManager().beginTransaction();
        transaction2.replace(R.id.place_location, framentplacespickup).commit();
        //places1=(PlaceAutocompleteFragment)getFragmentManager().findFragmentById(R.id.place_location);
        framentplacespickup.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                pickupoint = place.getAddress().toString();
                pickupoint = pickupoint.replace("", "+");
                getDirection();
            }

            @Override
            public void onError(Status status) {
                Toast.makeText(home.this,"Error: "+status.toString(),Toast.LENGTH_SHORT).show();
            }
        });

        fragmentplacesdrop =new PlaceAutocompleteFragment();
        transaction3 =getFragmentManager().beginTransaction();
        transaction3.add(R.id.place_location2, fragmentplacesdrop).commit();
        fragmentplacesdrop.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                destination = place.getAddress().toString();
                destination = destination.replace("", "+");
                getDirection();
            }

            @Override
            public void onError(Status status) {
                Toast.makeText(home.this,"Error: "+status.toString(),Toast.LENGTH_SHORT).show();
            }
        });

        geoFire=new GeoFire(riders);
        mService= Common.getGoogleAPI();
    }

    private void requestpickuphere(String uid) {
        dbrequest=FirebaseDatabase.getInstance().getReference("PickupRequest");
        geoFire=new GeoFire(dbrequest);
        mgeoFire.setLocation(uid,new GeoLocation(mlocation.getLatitude(),mlocation.getLongitude()));

        if (mCurrent.isVisible())
            mCurrent.remove();
        if (carMarker.isVisible())
            carMarker.remove();

        mUsermarker= mMap.addMarker(new MarkerOptions()
                          .title("Pickup here").snippet("")
                          .position(new LatLng(mlocation.getLatitude(),mlocation.getLongitude()))
                          .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        mUsermarker.showInfoWindow();
        btnpickuprequest.setText("Getting your driver");
    }

    private void getDirection() {

            currentposition=new LatLng(mlocation.getLatitude(),mlocation.getLongitude());
            String requestAPI=null;

            try{
               requestAPI="https://maps.googleapis.com/maps/api/directions/json?"+
                        "mode=driving&"+
                        "transit_routing_preference=less_driving&"+
                        "origin="+currentposition.latitude+","+currentposition.longitude+
                        "&destination="+destination+"&"+
                        "key="+getResources().getString(R.string.google_maps_key);
                Log.d("karchouURL",requestAPI);

                mService.getPath(requestAPI).enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, retrofit2.Response<String> response) {
                        try {
                            JSONObject jsonObject=new JSONObject(response.body().toString());

                            JSONArray jsonArray=jsonObject.getJSONArray("routes");

                            for (int i=0;i<jsonArray.length();i++) {
                                JSONObject route=jsonArray.getJSONObject(i);
                                JSONObject poly=route.getJSONObject("overview_polyline");


                                String polyline=poly.getString("points");
                                polylinelist=decodePoly(polyline);

                            }

                            LatLngBounds.Builder builder=new LatLngBounds.Builder();
                            for (LatLng latLng:polylinelist)
                                builder.include(latLng);
                            LatLngBounds bounds=builder.build();
                            CameraUpdate mCameraUpdate=CameraUpdateFactory.newLatLngBounds(bounds,2);

                            mMap.animateCamera(mCameraUpdate);

                            polylineOptions=new PolylineOptions();
                            polylineOptions.color(Color.GRAY);
                            polylineOptions.width(5);
                            polylineOptions.startCap(new SquareCap());
                            polylineOptions.endCap(new SquareCap());

                            polylineOptions.jointType(JointType.ROUND);
                            polylineOptions.addAll(polylinelist);

                            greyPolyline=mMap.addPolyline(polylineOptions);


                            blackPolylineOptions=new PolylineOptions();
                            blackPolylineOptions.color(Color.BLACK);
                            blackPolylineOptions.width(5);
                            blackPolylineOptions.startCap(new SquareCap());
                            blackPolylineOptions.endCap(new SquareCap());

                            blackPolylineOptions.jointType(JointType.ROUND);
                            blackPolyline=mMap.addPolyline(blackPolylineOptions);

                            mMap.addMarker(new MarkerOptions()
                                    .position(polylinelist.get(polylinelist.size()-1))
                                    .title("Pickup Location"));


                            ValueAnimator polyLineanimator=ValueAnimator.ofInt(0,100);
                            polyLineanimator.setDuration(2000);
                            polyLineanimator.setInterpolator(new LinearInterpolator());
                            polyLineanimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                @Override
                                public void onAnimationUpdate(ValueAnimator animation) {
                                    List<LatLng> points=greyPolyline.getPoints();
                                    int Percentvalue=(int)animation.getAnimatedValue();
                                    int size=points.size();

                                    int newPoints=(int)(size*(Percentvalue/100.0f));

                                    List<LatLng> p=points.subList(0,newPoints);
                                    blackPolyline.setPoints(p);
                                }
                            });

                            polyLineanimator.start();

                            if (mCurrent.isVisible())
                                mCurrent.remove();

                            carMarker=mMap.addMarker(new MarkerOptions().position(currentposition)
                                    .flat(true)
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.carertical)));


                            handler=new Handler();
                            index=-1;
                            next=1;
                            handler.postDelayed(drawPathRunnable,3000);

                        } catch (JSONException e) {

                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        Toast.makeText(home.this,""+t.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });

            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
    }

    private void startlocationUpdates() {
            locationManager=(LocationManager)this.getSystemService(Context.LOCATION_SERVICE);

            locationListener=new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    mlocation=location;
                    UpdateMap(mlocation);
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            };
    }

    private void displaylocation() {

        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) &&
                (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {

            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSION_REQUEST_CODE);
        }
        else
        {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            mlocation= locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (mlocation !=null)
               UpdateMap(mlocation);
        }
    }


    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        return super.onCreateView(name, context, attrs);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                .commit();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
      mMap=googleMap;
      mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
      mMap.setTrafficEnabled(false);
      mMap.setIndoorEnabled(false);
      mMap.setBuildingsEnabled(false);
      mMap.getUiSettings().setZoomControlsEnabled(true);
      mMap.getUiSettings().setScrollGesturesEnabled(true);
      mMap.getUiSettings().setCompassEnabled(true);

      displaylocation();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == MY_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if ((ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) &&
                        (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
                    {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                        mlocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (mlocation != null)
                            UpdateMap(mlocation);
                    }
                }
            }
        }
    }

    private void UpdateMap(Location location) {

        final double latittude = location.getLatitude();
        final double longitude = location.getLongitude();
        userlocation = new LatLng(latittude, longitude);

        if (userlocation!=null) {

                geoFire.setLocation(FirebaseAuth.getInstance().getCurrentUser().getUid(), new GeoLocation(latittude, longitude), new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, DatabaseError error) {

                        mCurrent = mMap.addMarker(new MarkerOptions()
                                .position(userlocation)
                                .flat(true)
                                .title("Your Location"));
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userlocation,15.0f));
                        //rotateMarker(mCurrent, -360, mMap);
                      }
                });
            }
           else
            Log.d("ERROR","");
  }

    private void rotateMarker(final Marker mCurrent, final float i, GoogleMap mMap) {
        final Handler handler=new Handler();
        final long start= SystemClock.uptimeMillis();
        final float startRotation=mCurrent.getRotation();
        final long duration=1500;
        final Interpolator interpolator=new LinearInterpolator();
        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed=SystemClock.uptimeMillis()-start;
                float t=interpolator.getInterpolation((float)elapsed/duration);
                float rot=t*i+(1-t)*startRotation;
                mCurrent.setRotation(-rot>180?rot/2:rot);

                if (t<1.0) {
                    handler.postDelayed(this,16);
                }
            }
        });
    }

    private void createLocationRequest() {
        mLocationRequest=new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTES_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    private void buildGoogleApiClient() {
        mGoogleAPIclinet=new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleAPIclinet.connect();
    }

    private List decodePoly(String encoded) {

        List poly = new ArrayList();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }


    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_home, container, false);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((home) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }
}
