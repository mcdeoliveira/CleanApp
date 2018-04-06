package org.emstrack.ambulance.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.emstrack.ambulance.dialogs.AlertSnackbar;
import org.emstrack.ambulance.services.AmbulanceForegroundService;
import org.emstrack.ambulance.R;
import org.emstrack.ambulance.services.OnServiceComplete;
import org.emstrack.models.Ambulance;
import org.emstrack.models.Hospital;
import org.emstrack.models.Location;
import org.emstrack.mqtt.MqttProfileClient;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static android.content.ContentValues.TAG;

// TODO: Implement listener to ambulance changes

public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnCameraIdleListener {

    View rootView;
    private Map<String, String> ambulanceStatus;
    private Map<Integer, Marker> ambulanceMarkers;
    private HashMap<Integer, Marker> hospitalMarkers;

    private ImageView showLocationButton;
    private boolean centerAmbulances = false;

    private ImageView showAmbulancesButton;
    private boolean showAmbulances = false;

    private ImageView showHospitalsButton;
    private boolean showHospitals = false;

    private boolean myLocationEnabled;
    private boolean useMyLocation = false;

    private float defaultZoom = 15;
    private int defaultPadding = 50;

    private float zoomLevel = defaultZoom;

    private GoogleMap googleMap;
    private AmbulancesUpdateBroadcastReceiver receiver;
    private int screenOrientation;
    private OrientationEventListener orientationListener;

    static float ROTATIONS[] = { 0.f, 90.f, 180.f, 270.f };
    static int degreesToRotation(int degrees) {
        if (degrees > 315 || degrees < 45)
            return Surface.ROTATION_0;
        else if (degrees > 45 && degrees < 135)
            return Surface.ROTATION_90;
        else if (degrees > 135 && degrees < 225)
            return Surface.ROTATION_180;
        else // if (degrees > 225 && degrees < 315)
            return Surface.ROTATION_270;
    };

    public class AmbulancesUpdateBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent ) {
            if (intent != null) {
                final String action = intent.getAction();
                assert action != null;
                if (action.equals(AmbulanceForegroundService.BroadcastActions.AMBULANCE_UPDATE)) {

                    if (centerAmbulances) {

                        Log.i(TAG, "AMBULANCE_UPDATE");

                        // center ambulances
                        centerAmbulances();

                    }

                } else if (action.equals(AmbulanceForegroundService.BroadcastActions.AMBULANCES_UPDATE)) {

                    Log.i(TAG, "AMBULANCES_UPDATE");

                    // update markers without centering
                    updateMarkers();

                }
            }
        }
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_map, container, false);

        // Retrieve location button
        showLocationButton = (ImageView) rootView.findViewById(R.id.showLocationButton);
        showLocationButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                // toggle show ambulances
                centerAmbulances = !centerAmbulances;

                // Switch color
                if (centerAmbulances)
                    showLocationButton.setBackgroundColor(getResources().getColor(R.color.mapButtonOn));
                else
                    showLocationButton.setBackgroundColor(getResources().getColor(R.color.mapButtonOff));

                Log.i(TAG, "Toggle center ambulances: " + (centerAmbulances ? "ON" : "OFF"));

                return true;

            }
        });
        showLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (googleMap != null) {

                    // center ambulances
                    centerAmbulances();

                }

            }
        });

        // Retrieve ambulance button
        showAmbulancesButton = (ImageView) rootView.findViewById(R.id.showAmbulancesButton);
        showAmbulancesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // toggle show ambulances
                showAmbulances = !showAmbulances;

                // Switch color
                if (showAmbulances)
                    showAmbulancesButton.setBackgroundColor(getResources().getColor(R.color.mapButtonOn));
                else
                    showAmbulancesButton.setBackgroundColor(getResources().getColor(R.color.mapButtonOff));

                Log.i(TAG, "Toggle show ambulances: " + (showAmbulances ? "ON" : "OFF"));

                if (googleMap != null) {

                    if (showAmbulances)

                        // retrieve ambulances
                        retrieveAmbulances();

                    else {

                        // forget ambulances
                        forgetAmbulances();

                        // update markers without centering
                        updateMarkers();

                    }

                }


            }
        });

        // Retrieve hospitals button
        showHospitalsButton = (ImageView) rootView.findViewById(R.id.showHospitalsButton);
        showHospitalsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // toggle show hospitals
                showHospitals = !showHospitals;

                // Switch color
                if (showHospitals)
                    showHospitalsButton.setBackgroundColor(getResources().getColor(R.color.mapButtonOn));
                else
                    showHospitalsButton.setBackgroundColor(getResources().getColor(R.color.mapButtonOff));

                Log.i(TAG, "Toggle show hospitals: " + (showHospitals ? "ON" : "OFF"));

                if (googleMap != null) {

                    if (!showHospitals) {

                        // Clear markers
                        Iterator<Map.Entry<Integer,Marker>> iter = hospitalMarkers.entrySet().iterator();
                        while (iter.hasNext())
                        {
                            // get entry
                            Map.Entry<Integer,Marker> entry = iter.next();

                            // remove from map
                            entry.getValue().remove();

                            // remove from collection
                            iter.remove();

                        }

                    }

                    // update markers without centering
                    updateMarkers();

                }

            }
        });
        
        // Initialize markers maps
        ambulanceMarkers = new HashMap<>();
        hospitalMarkers = new HashMap<>();

        // Get settings, status and capabilities
        final MqttProfileClient profileClient = AmbulanceForegroundService.getProfileClient(getContext());

        ambulanceStatus = profileClient.getSettings().getAmbulanceStatus();

        // Initialize screen rotation
        if (rootView.getDisplay() != null)
            screenOrientation = rootView.getDisplay().getRotation();
        else
            screenOrientation = Surface.ROTATION_0;

        // Initialize map
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Orientation listener
        orientationListener = new OrientationEventListener(getContext(), SensorManager.SENSOR_DELAY_NORMAL) {

            @Override
            public void onOrientationChanged(int rotation) {

                Activity activity = getActivity();
                if (activity != null) {

                    //Log.d(TAG,"onOrientationChanged");
                    if (rotation != ORIENTATION_UNKNOWN) {
                        screenOrientation = degreesToRotation(rotation);
                        //Log.d(TAG, "rotation = " + rotation + ", screenOrientation = " + screenOrientation);
                    }
                }

            }

        };

        // Enable sensor
        if (orientationListener.canDetectOrientation())
            orientationListener.enable();
        else
            orientationListener.disable();

        return rootView;

    }

    public void retrieveAmbulances() {

        // Get ambulances
        Map<Integer, Ambulance> ambulances = AmbulanceForegroundService.getOtherAmbulances();

        if (ambulances == null) {

            Log.i(TAG,"No ambulances.");

            if (showAmbulances) {

                Log.i(TAG,"Retrieving ambulances...");

                // Disable button
                showAmbulancesButton.setEnabled(false);

                // Retrieve ambulances first
                Intent ambulancesIntent = new Intent(getContext(), AmbulanceForegroundService.class);
                ambulancesIntent.setAction(AmbulanceForegroundService.Actions.GET_AMBULANCES);

                // What to do when GET_AMBULANCES service completes?
                new OnServiceComplete(getContext(),
                        AmbulanceForegroundService.BroadcastActions.SUCCESS,
                        AmbulanceForegroundService.BroadcastActions.FAILURE,
                        ambulancesIntent) {

                    @Override
                    public void onSuccess(Bundle extras) {

                        Log.i(TAG,"Got them all. Updating markers.");

                        // update markers and center bounds
                        centerMap(updateMarkers());

                        // Enable button
                        showAmbulancesButton.setEnabled(true);

                    }
                }
                        .setFailureMessage(getString(R.string.couldNotRetrieveAmbulances))
                        .setAlert(new AlertSnackbar(getActivity()));

            }

        } else if (showAmbulances) {

            Log.i(TAG,"Already have ambulances. Updating markers.");

            // Already have ambulances

            // update markers and center bounds
            centerMap(updateMarkers());

        }

    }

    public void forgetAmbulances() {

        // disable button
        showAmbulancesButton.setEnabled(false);

        // Clear markers
        Iterator<Map.Entry<Integer,Marker>> iter = ambulanceMarkers.entrySet().iterator();
        while (iter.hasNext())
        {
            // get entry
            Map.Entry<Integer,Marker> entry = iter.next();

            // remove from map
            entry.getValue().remove();

            // remove from collection
            iter.remove();

        }

        // Unsubscribe to ambulances
        Intent intent = new Intent(getActivity(), AmbulanceForegroundService.class);
        intent.setAction(AmbulanceForegroundService.Actions.STOP_AMBULANCES);

        // What to do when service completes?
        new OnServiceComplete(getContext(),
                AmbulanceForegroundService.BroadcastActions.SUCCESS,
                AmbulanceForegroundService.BroadcastActions.FAILURE,
                intent) {

            @Override
            public void onSuccess(Bundle extras) {
                Log.i(TAG, "onSuccess");

                showAmbulancesButton.setEnabled(true);

            }

        }
                .setFailureMessage(getString(R.string.couldNotUnsubscribeToAmbulances))
                .setAlert(new AlertSnackbar(getActivity()));

    }

     @Override
    public void onResume() {
         super.onResume();

         // Enable orientation listener
         if (orientationListener.canDetectOrientation())
             orientationListener.enable();
         else
             orientationListener.disable();

         // Register receiver
         IntentFilter filter = new IntentFilter();
         filter.addAction(AmbulanceForegroundService.BroadcastActions.AMBULANCES_UPDATE);
         filter.addAction(AmbulanceForegroundService.BroadcastActions.AMBULANCE_UPDATE);
         receiver = new AmbulancesUpdateBroadcastReceiver();
         getLocalBroadcastManager().registerReceiver(receiver, filter);

         // Switch colors
         if (showAmbulances)
             showAmbulancesButton.setBackgroundColor(getResources().getColor(R.color.mapButtonOn));
         else
             showAmbulancesButton.setBackgroundColor(getResources().getColor(R.color.mapButtonOff));

         if (showHospitals)
             showHospitalsButton.setBackgroundColor(getResources().getColor(R.color.mapButtonOn));
         else
             showHospitalsButton.setBackgroundColor(getResources().getColor(R.color.mapButtonOff));
         
         if (centerAmbulances)
             showLocationButton.setBackgroundColor(getResources().getColor(R.color.mapButtonOn));
         else
             showLocationButton.setBackgroundColor(getResources().getColor(R.color.mapButtonOff));

     }

    @Override
    public void onPause() {
        super.onPause();

        // Unregister receiver
        if (receiver != null) {
            getLocalBroadcastManager().unregisterReceiver(receiver);
            receiver = null;
        }

        // disable orientation listener
        orientationListener.disable();

    }

    @Override
    public void onCameraIdle() {
        zoomLevel = googleMap.getCameraPosition().zoom;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        // save map
        this.googleMap = googleMap;

        myLocationEnabled = false;
        if (AmbulanceForegroundService.canUpdateLocation()) {

            if (useMyLocation) {

                // Use google map's my location

                try {

                    Log.i(TAG, "Enable my location on google map.");
                    googleMap.setMyLocationEnabled(true);
                    myLocationEnabled = true;

                } catch (SecurityException e) {
                    Log.i(TAG, "Could not enable my location on google map.");
                }

            }

        }

        // Add listener to track zoom
        googleMap.setOnCameraIdleListener(this);

        if (showAmbulances) {

            // retrieve ambulances
            retrieveAmbulances();

        } else {

            // Update markers and center map
            centerMap(updateMarkers());

        }

    }



    public void centerMap(LatLngBounds bounds) {

        if (ambulanceMarkers.size() > 0) {

            // Move camera
            if (ambulanceMarkers.size() == 1) {

                Ambulance ambulance = AmbulanceForegroundService.getAmbulance();
                if (ambulance != null) {

                    Location location = ambulance.getLocation();
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel));

                    return;
                }

            } else if (bounds != null) {

                // move camera
                googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, defaultPadding));

                return;

            }

        }

        // Otherwise center at default location
        Location location = AmbulanceForegroundService.getProfileClient(getContext())
                .getSettings().getDefaults().getLocation();
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel));

    }

    public LatLngBounds updateMarkers() {

        // fast return
        if (googleMap == null)
            return null;

        // Assemble marker bounds
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        // Update hospitals
        if (showHospitals) {

            // Get hospitals
            Map<Integer, Hospital> hospitals = AmbulanceForegroundService.getHospitals();

            if (hospitals != null) {

                // Loop over all hospitals
                for (Map.Entry<Integer, Hospital> entry : hospitals.entrySet()) {

                    // Get hospital
                    Hospital hospital = entry.getValue();

                    // Add marker for hospital
                    Marker marker = addMarkerForHospital(hospital);

                    // Add to bound builder
                    builder.include(marker.getPosition());

                }
            }

        }
        
        // Update ambulances
        if (showAmbulances) {

            // Get ambulances
            Map<Integer, Ambulance> ambulances = AmbulanceForegroundService.getOtherAmbulances();

            if (ambulances != null) {

                // Loop over all ambulances
                for (Map.Entry<Integer, Ambulance> entry : ambulances.entrySet()) {

                    // Get ambulance
                    Ambulance ambulance = entry.getValue();

                    // Add marker for ambulance
                    Marker marker = addMarkerForAmbulance(ambulance);

                    // Add to bound builder
                    builder.include(marker.getPosition());

                }
            }

        }

        // Handle my location?
        if (!useMyLocation) {

            Ambulance ambulance = AmbulanceForegroundService.getAmbulance();
            if (ambulance != null) {

                // Add marker for ambulance
                Marker marker = addMarkerForAmbulance(ambulance);

                // Add to bound builder
                builder.include(marker.getPosition());

            }

        }

        // Calculate bounds and return
        try {
            return builder.build();
        } catch (IllegalStateException e) {
            return null;
        }

    }

    public Marker addMarkerForAmbulance(Ambulance ambulance) {

        Log.d(TAG,"Adding marker for ambulance " + ambulance.getIdentifier());

        // Find new location
        Location location = ambulance.getLocation();
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        // Marker exist?
        Marker marker;
        if (ambulanceMarkers.containsKey(ambulance.getId())) {

            // Update marker
            marker = ambulanceMarkers.get(ambulance.getId());
            marker.setPosition(latLng);
            marker.setRotation((float) ambulance.getOrientation());
            marker.setSnippet(ambulanceStatus.get(ambulance.getStatus()));

        } else {

            // Create marker
            marker = googleMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .icon(bitmapDescriptorFromVector(getActivity(), R.drawable.ambulance_blue, 0.1))
                    .anchor(0.5F,0.5F)
                    .rotation((float) ambulance.getOrientation())
                    .flat(true)
                    .title(ambulance.getIdentifier())
                    .snippet(ambulanceStatus.get(ambulance.getStatus())));

            // Save marker
            ambulanceMarkers.put(ambulance.getId(), marker);

        }

        return marker;
    }

    public Marker addMarkerForHospital(Hospital hospital) {

        Log.d(TAG,"Adding marker for hospital " + hospital.getName());

        // Find new location
        Location location = hospital.getLocation();
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        // Marker exist?
        Marker marker;
        if (hospitalMarkers.containsKey(hospital.getId())) {

            // Update marker
            marker = hospitalMarkers.get(hospital.getId());
            marker.setPosition(latLng);;

        } else {

            // Create marker
            marker = googleMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .icon(bitmapDescriptorFromVector(getActivity(), R.drawable.ic_hospital_15, 1))
                    .anchor(0.5F,0.5F)
                    .flat(true)
                    .title(hospital.getName()));

            // Save marker
            hospitalMarkers.put(hospital.getId(), marker);

        }

        return marker;
    }
    
    public void centerAmbulances() {

        Ambulance ambulance = AmbulanceForegroundService.getAmbulance();
        if (ambulance != null) {

            Location location = ambulance.getLocation();
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

            // Add marker for ambulance
            Marker marker = addMarkerForAmbulance(ambulance);

            // Center and orient map
            CameraPosition currentPlace = new CameraPosition.Builder()
                    .target(latLng)
                    .bearing((float) ambulance.getOrientation() + ROTATIONS[screenOrientation])
                    .zoom(zoomLevel)
                    .build();
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(currentPlace));

        }

    }

    /*
     * This is from
     * https://stackoverflow.com/questions/42365658/custom-marker-in-google-maps-in-android-with-vector-asset-icon
     */
    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId, double scale) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        int width = Math.round((float) Math.ceil(vectorDrawable.getIntrinsicWidth()*scale));
        int height = Math.round((float) Math.ceil(vectorDrawable.getIntrinsicHeight()*scale));
        vectorDrawable.setBounds(0, 0, width, height);
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

   /**
     * Get LocalBroadcastManager
     *
     * @return the LocalBroadcastManager
     */
    private LocalBroadcastManager getLocalBroadcastManager() {
        return LocalBroadcastManager.getInstance(getContext());
    }

}
