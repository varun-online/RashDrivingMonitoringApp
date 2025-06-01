package app.rashdriving.saferide;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.os.Handler;
import android.os.Looper;


public class dashboard extends AppCompatActivity implements OnMapReadyCallback {

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef, vehicleRef;
    private TextView userName, userphone, speedTextView;
    private FusedLocationProviderClient fusedLocationClient;
    private GoogleMap mMap;
    private static final int LOCATION_REQUEST_CODE = 1001;
    private LatLng previousDeviceLocation = null;
    private LatLng userLocation, deviceLocation;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable vehicleLocationRunnable,markerUpdateRunnable;

    private Marker userMarker, vehicleMarker;

    private static final String VEHICLE_NUMBER = "TN-10-AB-1234";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Initialize Firebase Auth and Database
        //ImageButton btnMyLocation = findViewById(R.id.dash_btnMyLocation);

       // getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE  | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        ImageButton btnVehicleLocation = findViewById(R.id.dash_zoom_vechicle_location);
        ImageButton btnDirections = findViewById(R.id.dash_directions_btn);
        speedTextView = findViewById(R.id.dash_speedTextView);
        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");
        vehicleRef = FirebaseDatabase.getInstance().getReference("vehicles");
        FirebaseUser user = mAuth.getCurrentUser();
        ImageButton menuIcon = findViewById(R.id.menu_item_home);


        // Initialize Location Client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Get map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.dash_map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }


        //btnMyLocation.setOnClickListener(v -> {
        //  if (userLocation != null) {
        //    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 16f));
        // } else {
        //   Toast.makeText(this, "User location not available yet!", Toast.LENGTH_SHORT).show();
        //}
        //});

        btnVehicleLocation.setOnClickListener(v -> {
            if (deviceLocation != null) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(deviceLocation, 16f));
            } else {
                Toast.makeText(this, "Vehicle location not available yet!", Toast.LENGTH_SHORT).show();
            }
        });

        btnDirections.setOnClickListener(v -> {
            if (userLocation != null && deviceLocation != null) {
                showDirections(userLocation, deviceLocation);
            } else {
                Toast.makeText(dashboard.this, "Locations not available!", Toast.LENGTH_SHORT).show();
            }
        });

        // Menu icon action
        menuIcon.setOnClickListener(v -> {
            startActivity(new Intent(dashboard.this, Profile_activity.class));
        });


        // Ensure user is authenticated and verified
        if (user != null) {
            if (!user.isEmailVerified()) {
                Toast.makeText(this, "Please verify your email before accessing the dashboard.", Toast.LENGTH_LONG).show();
                logout();
            } else {
                loadUserData(user.getUid());
            }
        } else {
            logout();
        }

        requestLocationPermission();
    }

    private void showDirections(LatLng origin, LatLng destination) {
        String uri = "https://www.google.com/maps/dir/?api=1&origin=" + origin.latitude + "," + origin.longitude + "&destination=" + destination.latitude + "," + destination.longitude + "&travelmode=driving";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        intent.setPackage("com.google.android.apps.maps");
        startActivity(intent);

    }

    // Fetch user details from Firebase
    private void loadUserData(String userId) {
        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    String vehicle = snapshot.child("vehicleNo").getValue(String.class);

                    // Fetch vehicle location after loading user data
                    if (vehicle != null) {
                        getVehicleLocation(vehicle);
                    }
                } else {
                    Toast.makeText(dashboard.this, "User data not found!", Toast.LENGTH_SHORT).show();
                    logout();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(dashboard.this, "Failed to load user data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    // Fetch vehicle location from Firebase
    private void getVehicleLocation(String vehicleNumber) {
        vehicleRef.child(vehicleNumber).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    double latitude = snapshot.child("Latitude").getValue(Double.class);
                    double longitude = snapshot.child("Longitude").getValue(Double.class);
                    float speed = snapshot.child("Speed").getValue(float.class);
                    deviceLocation = new LatLng(latitude, longitude);
                    speedTextView.setText(String.format("%.2f km/h", speed));

                    if (vehicleMarker != null) {
                        vehicleMarker.remove();
                    }
                    // Add new marker
                    vehicleMarker = mMap.addMarker(new MarkerOptions().position(deviceLocation).title("Vehicle Location"));

                    // Check if the vehicle is moving
                    if (previousDeviceLocation == null || !previousDeviceLocation.equals(deviceLocation)) {
                        previousDeviceLocation = deviceLocation; // Update previous location
                    } else {
                        return;
                    }
                } else {
                    Toast.makeText(dashboard.this, "Vehicle details not found!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(dashboard.this, "Failed to retrieve vehicle location: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void startVehicleLocationUpdates(String vehicleNumber) {
        vehicleLocationRunnable = new Runnable() {
            @Override
            public void run() {
                getVehicleLocation(vehicleNumber);
                handler.postDelayed(this, 1000);
                // Repeat every 1 second
            }
        };
        markerUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                getCurrentLocation();
                handler.postDelayed(this, 2000); // Repeat every 2 seconds
            }
        };
        handler.post(vehicleLocationRunnable);
        handler.post(markerUpdateRunnable);
    }

    private void stopVehicleLocationUpdates() {
        if (vehicleLocationRunnable != null) {
            handler.removeCallbacks(vehicleLocationRunnable);
        }
        if (markerUpdateRunnable != null) {
            handler.removeCallbacks(markerUpdateRunnable);
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        startVehicleLocationUpdates(VEHICLE_NUMBER);
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopVehicleLocationUpdates();
    }

    // Logout user
    private void logout() {
        mAuth.signOut();
        Toast.makeText(this, "Signing Out...", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }



    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;


        // Enable UI controls
        mMap.getUiSettings().setCompassEnabled(true); // Compass
        mMap.getUiSettings().setMapToolbarEnabled(true); // Toolbar for navigation options
        mMap.getUiSettings().setTiltGesturesEnabled(true); // 3D tilt view

        // Enable traffic layer for real-time traffic updates
        //mMap.setTrafficEnabled(true);

        // Enable 3D buildings view
        mMap.setBuildingsEnabled(true);

        // Initialize map type selector
        initMapTypeSelector();

        // Check Permissions
        if (!hasLocationPermission()) {
            requestLocationPermission();

            // Enable the blue dot for the user's location
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mMap.setMyLocationEnabled(true);
            return;
        }

        // Fetch Locations
        getCurrentLocation();

    }

    private void initMapTypeSelector() {
        ImageButton btnMapType = findViewById(R.id.dash_map_selection);

        btnMapType.setOnClickListener(v -> {
            final String[] mapTypes = {"Default", "Satellite", "Terrain"};

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Select Map Type");
            builder.setItems(mapTypes, (dialog, which) -> {
                switch (which) {
                    case 0:
                        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        break;
                    case 1:
                        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                        break;
                    case 2:
                        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                        break;

                }
            });
            builder.show();
        });

    }


    private boolean hasLocationPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        }
       // else {
         //   startSpeedUpdates();
       // }
    }

   // private void startSpeedUpdates() {LocationRequest locationRequest = LocationRequest.create();locationRequest.setInterval(1000);locationRequest.setFastestInterval(500);locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        // Convert m/s to km/hLocationCallback locationCallback = new LocationCallback(){@Override public void onLocationResult(@NonNull LocationResult locationResult) {for (Location location : locationResult.getLocations()){if (location != null) {}}}};if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);}}

    @SuppressLint("MissingPermission")
    private void getCurrentLocation() {
        if (!hasLocationPermission()) return;

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                userLocation = new LatLng(location.getLatitude(), location.getLongitude());

                // First, zoom to vehicle location
                if (deviceLocation != null) {

                    // Remove the old marker if it exists
                    if (vehicleMarker != null) {
                        vehicleMarker.remove();
                    }

                    vehicleMarker = mMap.addMarker(new MarkerOptions()
                            .position(deviceLocation)
                            .title("Vehicle Location"));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(deviceLocation, 16f));
                }

                // Then update user location
                if (userMarker == null) {
                    mMap.setMyLocationEnabled(true); // Enable blue dot
                } else {
                    userMarker.setPosition(userLocation);
                }
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Location access failed!", Toast.LENGTH_LONG).show();
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(this, "Location permission denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private BitmapDescriptor getBitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

}