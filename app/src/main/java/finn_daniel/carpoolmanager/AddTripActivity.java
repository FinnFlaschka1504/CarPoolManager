package finn_daniel.carpoolmanager;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.slybeaver.slycalendarview.SlyCalendarDialog;


public class AddTripActivity extends AppCompatActivity implements OnMapReadyCallback {

    Button dialogSelectRoute_save;
    Button addTrip_from;
    Button addTrip_to;
    Button addTrip_distance;
    Button addTrip_selectDate;
    Button addTrip_addBookmark;
    Button addTrip_cancel;
    Button addTrip_save;
    Button addTrip_addCar;
    Button dialogAddCar_cancel;
    Button dialogAddCar_save;
    TextView addTrip_consumption;
    TextView addTrip_wear;
    TextView addTrip_cost;
    TextView dialogSelectRoute_from;
    TextView dialogSelectRoute_to;
    TextView dialogSelectRoute_distance;
    LinearLayout dialogSelectRoute_distanceLayout;
    TextView dialogRenamePoints_from;
    TextView dialogRenamePoints_to;
    TextView dialogAddCar_name;
    TextView dialogAddCar_consumption;
    TextView dialogAddCar_wear;
    Spinner addTrip_selectCar;

    Map<String, Car> carIdMap = new HashMap<>();
    Map<String, String> carNameToIdMap = new HashMap<>();
    List<Car> carList = new ArrayList<>();
//    Map<String, User> loggedinUser_map = new HashMap<>();


    User loggedInUser;
    SharedPreferences mySPR;
    Marker markerFrom;
    Marker markerTo;
    GoogleMap googleMap;
    Group thisGroup;
    Dialog dialog_selectRoute;
    Dialog dialog_renamePoints;
    Dialog dialog_addCar;
    Gson gson = new Gson();
    private String EXTRA_GROUP = "EXTRA_GROUP";
    private static final String TAG = "AddTripActivity";
    Date[] date = new Date[2];
    String[] dateString = new String[2];
    String from;
    String to;
    Marker neuerMarker;
    Car newCar;
    DatabaseReference databaseReference;
    Map<String, Double> fuelCostMap = new HashMap<>();



    View.OnFocusChangeListener onFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View view, boolean focus) {
            if (!dialogSelectRoute_from.isFocused() && !dialogSelectRoute_to.isFocused()) {
                dialog_selectRoute.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                InputMethodManager imm = (InputMethodManager) AddTripActivity.this.getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
            if (dialogSelectRoute_from.isFocused() || dialogSelectRoute_to.isFocused())
                dialog_selectRoute.findViewById(R.id.dialogSelectRoute_fromToLayout).setAlpha(1f);
            else
                dialog_selectRoute.findViewById(R.id.dialogSelectRoute_fromToLayout).setAlpha(0.7f);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_trip);

        mySPR = getSharedPreferences("CarPoolManager_Daten",0);
        String loggedinUser_map_string = mySPR.getString("loggedInUser", "--Leer--");
        if (!loggedinUser_map_string.equals("--Leer--")) {
            loggedInUser = gson.fromJson(loggedinUser_map_string, User.class);
        }


        thisGroup = gson.fromJson(getIntent().getStringExtra(EXTRA_GROUP), Group.class);
        databaseReference = FirebaseDatabase.getInstance().getReference();

        dialog_selectRoute = new Dialog(AddTripActivity.this);
        dialog_selectRoute.setContentView(R.layout.dialog_select_route);

        addTrip_from = findViewById(R.id.addTrip_from);
        addTrip_to = findViewById(R.id.addTrip_to);
        addTrip_distance = findViewById(R.id.addTrip_distance);
        dialogSelectRoute_from = dialog_selectRoute.findViewById(R.id.dialogSelectRoute_from);
        dialogSelectRoute_to = dialog_selectRoute.findViewById(R.id.dialogSelectRoute_to);
        dialogSelectRoute_distance = dialog_selectRoute.findViewById(R.id.dialogSelectRoute_distance);
        dialogSelectRoute_distanceLayout = dialog_selectRoute.findViewById(R.id.dialogSelectRoute_distanceLayout);
        addTrip_selectDate = findViewById(R.id.addTrip_selectDate);
        addTrip_addBookmark = findViewById(R.id.addTrip_addBookmark);
        addTrip_save = findViewById(R.id.addTrip_save);
        addTrip_cancel = findViewById(R.id.addTrip_cancel);
        addTrip_consumption = findViewById(R.id.addTrip_consumption);
        addTrip_wear = findViewById(R.id.addTrip_wear);
        addTrip_cost = findViewById(R.id.addTrip_cost);
        addTrip_addCar = findViewById(R.id.addTrip_addCar);
        addTrip_selectCar = findViewById(R.id.addTrip_selectCar);

        loadCarSpinner(false);


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        dialogSelectRoute_from.setOnFocusChangeListener(onFocusChangeListener);
        dialogSelectRoute_to.setOnFocusChangeListener(onFocusChangeListener);

        addTrip_from.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startMapDialog();
                dialogSelectRoute_from.requestFocus();
                dialog_selectRoute.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            }
        });
        addTrip_to.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startMapDialog();
                dialogSelectRoute_to.requestFocus();
                dialog_selectRoute.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            }
        });

        Date heute = new Date();
        String pattern = "dd.MM.yyyy E";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        String buttonText = simpleDateFormat.format(heute.getTime()).replace(" ", " (") + ")";
        addTrip_selectDate.setText(buttonText);
        date[0] = heute;

        addTrip_selectCar.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long arg3) {
//                String city = "The city is " + parent.getItemAtPosition(position).toString();
//                Toast.makeText(parent.getContext(), city, Toast.LENGTH_LONG).show();
                Car selectedCar = carList.get(position);
                addTrip_consumption.setText(selectedCar.getConsumption().toString().replace(".",",") + " €/100km");
                addTrip_wear.setText(selectedCar.getWear().toString().replace(".",",") + " €/100km");
                if (addTrip_distance.getText().toString().contains("km")) {
                    calculateCost();
                }
            }

            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        addTrip_addCar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddCarDialog();
            }
        });

    }

    void calculateCost() {
        double distance = Double.valueOf(addTrip_distance.getText().toString().split(" ")[0].replace(",","."));
        DecimalFormat df = new DecimalFormat("#.00");
        Car selectedCar = carList.get(addTrip_selectCar.getSelectedItemPosition());
        Double costs = distance * (selectedCar.getConsumption() + selectedCar.getWear()) / 100;
        addTrip_cost.setText( df.format(costs)+ " €");
        // ToDo: wenn kosten stehen kann gespeicert werden
        addTrip_save.setEnabled(true);
        addTrip_addBookmark.setBackgroundTintList(this.getResources().getColorStateList(R.color.add_bookmar_color));
        addTrip_addBookmark.setEnabled(true);
    }

    private void showAddCarDialog() {
        dialog_addCar = new Dialog(AddTripActivity.this);
        dialog_addCar.setContentView(R.layout.dialog_add_car);

        dialogAddCar_name = dialog_addCar.findViewById(R.id.dialogAddCar_name);
        dialogAddCar_consumption = dialog_addCar.findViewById(R.id.dialogAddCar_consumption);
        dialogAddCar_wear = dialog_addCar.findViewById(R.id.dialogAddCar_wear);
        dialogAddCar_cancel = dialog_addCar.findViewById(R.id.dialogAddCar_cancel);
        dialogAddCar_save = dialog_addCar.findViewById(R.id.dialogAddCar_save);


        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog_addCar.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        dialog_addCar.show();
        dialog_addCar.getWindow().setAttributes(lp);

        dialog_addCar.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        dialogAddCar_name.requestFocus();

        dialogAddCar_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog_addCar.dismiss();
            }
        });

        dialogAddCar_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dialogAddCar_name.getText().toString().trim().equals("") || dialogAddCar_consumption.getText().toString().equals("") || dialogAddCar_wear.getText().toString().equals("")) {
                    Toast.makeText(AddTripActivity.this, "Alle Felder müssen gefüllt sein", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (carNameToIdMap.containsKey(dialogAddCar_name.getText().toString().trim())) {
                    Toast.makeText(AddTripActivity.this, "Auto mit dem Namen existiert bereits", Toast.LENGTH_SHORT).show();
                    return;
                }

                newCar = new Car();
                newCar.setName(dialogAddCar_name.getText().toString().trim());
                newCar.setConsumption(Double.valueOf(dialogAddCar_consumption.getText().toString()));
                newCar.setWear(Double.valueOf(dialogAddCar_wear.getText().toString()));
                databaseReference.child("Cars").child(newCar.getCar_id()).setValue(newCar);
                dialog_addCar.dismiss();

                databaseReference.child("Users").child(loggedInUser.getUser_id()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue() == null)
                            return;
                        User foundUser = dataSnapshot.getValue(User.class);
                        foundUser.addCar(newCar.getCar_id());
                        databaseReference.child("Users").child(foundUser.getUser_id()).setValue(foundUser);
                        loggedInUser = foundUser;
                        loadCarSpinner(true);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            }
        });
    }

    void loadCarSpinner(boolean newCar) {
        carIdMap.clear();
        carNameToIdMap.clear();
        for (String carId : loggedInUser.getCarIdList()) {
            databaseReference.child("Cars").child(carId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() == null)
                        return;
                    Car foundCar = dataSnapshot.getValue(Car.class);
                    carIdMap.put(foundCar.getCar_id(), foundCar);
                    carNameToIdMap.put(foundCar.getName(), foundCar.getCar_id());
                    if (carIdMap.size() >= loggedInUser.getCarIdList().size()) {
                        ArrayList<String> arrayList = new ArrayList<>();
                        for (String carId : loggedInUser.getCarIdList()) {
                            arrayList.add(carIdMap.get(carId).getName());
                            carList.add(carIdMap.get(carId));
                        }
                        ArrayAdapter<String> adp = new ArrayAdapter<>(AddTripActivity.this, android.R.layout.simple_spinner_dropdown_item, arrayList);
                        addTrip_selectCar.setAdapter(adp);
                        // ToDo: wenn neues auto dann das automatisch auswählen

                        //        addTrip_selectCar.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }
    }

    void startMapDialog() {
        dialog_selectRoute.show();

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog_selectRoute.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        dialog_selectRoute.show();
        dialog_selectRoute.getWindow().setAttributes(lp);


        dialog_selectRoute.findViewById(R.id.dialogSelectRoute_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog_selectRoute.dismiss();
            }
        });

        dialogSelectRoute_save = dialog_selectRoute.findViewById(R.id.dialogSelectRoute_continue);
        dialogSelectRoute_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Toast.makeText(AddTripActivity.this, markerFrom.getPosition().toString() + "\n" + markerTo.getPosition().toString(), Toast.LENGTH_SHORT).show();
                showRenamePointsDialog();
            }
        });
    }

    private void showRenamePointsDialog() {
        dialog_renamePoints = new Dialog(AddTripActivity.this);
        dialog_renamePoints.setContentView(R.layout.dialog_rename_points);

        dialogRenamePoints_from = dialog_renamePoints.findViewById(R.id.dialogRenamePoints_from);
        dialogRenamePoints_to = dialog_renamePoints.findViewById(R.id.dialogRenamePoints_to);

        dialogRenamePoints_from.setText(dialogSelectRoute_from.getText());
        dialogRenamePoints_to.setText(dialogSelectRoute_to.getText());

        TextView dialogRenamePoints_fromText = dialog_renamePoints.findViewById(R.id.dialogRenamePoints_fromText);
        ;
        TextView dialogRenamePoints_toText = dialog_renamePoints.findViewById(R.id.dialogRenamePoints_toText);


        if (dialogRenamePoints_to.getText().toString().matches("Breite: .*; Länge: .*")) {
            dialogRenamePoints_toText.setTextColor(Color.RED);
            dialogRenamePoints_to.requestFocus();
            dialog_renamePoints.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
        if (dialogSelectRoute_from.getText().toString().matches("Breite: .*; Länge: .*")) {
            dialogRenamePoints_fromText.setTextColor(Color.RED);
            dialogRenamePoints_from.requestFocus();
            dialog_renamePoints.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog_renamePoints.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        dialog_renamePoints.show();
        dialog_renamePoints.getWindow().setAttributes(lp);

        dialogRenamePoints_to.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO || actionId == EditorInfo.IME_ACTION_DONE || event.getAction() == KeyEvent.ACTION_DOWN || event.getAction() == KeyEvent.KEYCODE_ENTER) {
                    dialog_renamePoints.findViewById(R.id.dialogRenamePoints_save).callOnClick();
                }
                return true;
            }
        });


        dialog_renamePoints.findViewById(R.id.dialogRenamePoints_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog_renamePoints.dismiss();
            }
        });
        dialog_renamePoints.findViewById(R.id.dialogRenamePoints_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addTrip_from.setText(dialogRenamePoints_from.getText());
                addTrip_to.setText(dialogRenamePoints_to.getText());
                addTrip_distance.setText(dialogSelectRoute_distance.getText());
                dialog_renamePoints.dismiss();
                dialog_selectRoute.dismiss();
                if (addTrip_selectCar.getAdapter() != null)
                    calculateCost();
            }
        });


    }

    public boolean onSupportNavigateUp() {
        this.finish();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_trip, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_bookmark) {
            Toast.makeText(this, "Lesezeichen", Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void addTrip_chooseDate(View view) {
        new SlyCalendarDialog()
                .setSingle(false)
                .setCallback(callback)
                .setStartDate(date[0])
                .setEndDate(date[1])
                .setHeaderColor(Color.parseColor("#009688"))
                .setSelectedTextColor(Color.parseColor("#ffffff"))
                .setSelectedColor(Color.parseColor("#009688"))
                .show(getSupportFragmentManager(), "TAG_SLYCALENDAR");
    }


    SlyCalendarDialog.Callback callback = new SlyCalendarDialog.Callback() {
        @Override
        public void onCancelled() {

        }

        @Override
        public void onDataSelected(Calendar firstDate, Calendar secondDate, int hours, int minutes) {

            String pattern = "dd.MM.yyyy E";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
            date[0] = firstDate.getTime();
            if (secondDate != null)
                date[1] = secondDate.getTime();
            else
                date[1] = null;
            dateString[0] = simpleDateFormat.format(firstDate.getTime()).replace(" ", " (") + ")";
            if (secondDate != null)
                dateString[1] = simpleDateFormat.format(secondDate.getTime()).replace(" ", " (") + ")";
            else
                dateString[1] = null;

            String buttonText = dateString[0];
            if (dateString[1] != null)
                buttonText = buttonText.concat("  -  " + dateString[1]);
            addTrip_selectDate.setText(buttonText);

        }
    };

    @Override
    public void onMapReady(GoogleMap pGoogleMap) {

        googleMap = pGoogleMap;

        googleMap.setPadding(0, 300, 0, 150);

        dialogSelectRoute_from.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE || event.getAction() == KeyEvent.ACTION_DOWN || event.getAction() == KeyEvent.KEYCODE_ENTER) {
                    if (!dialogSelectRoute_from.getText().toString().equals("") && dialogSelectRoute_to.getText().toString().equals("")) {
                        LatLng foundLocation = geoLocate(dialogSelectRoute_from);
                        if (foundLocation == null)
                            return true;
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(foundLocation, 14f));
                        googleMap.addMarker(new MarkerOptions().position(foundLocation).title("Von"));
                    } else
                        routing();
                }
                return true;
            }
        });
        dialogSelectRoute_to.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE || event.getAction() == KeyEvent.ACTION_DOWN || event.getAction() == KeyEvent.KEYCODE_ENTER) {
                    if (dialogSelectRoute_from.getText().toString().equals("") && !dialogSelectRoute_to.getText().toString().equals("")) {
                        LatLng foundLocation = geoLocate(dialogSelectRoute_to);
                        if (foundLocation == null)
                            return true;
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(foundLocation, 14f));
                        googleMap.addMarker(new MarkerOptions().position(foundLocation).title("Nach"));
                    } else
                        routing();
                }

                return true;
            }
        });

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(50.857150, 10.266292), 5f));
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                dialogSelectRoute_from.clearFocus();
                dialogSelectRoute_to.clearFocus();

                if (neuerMarker != null)
                    neuerMarker.remove();

                String infoText;
                if (dialogSelectRoute_from.getText().toString().equals("") && !dialogSelectRoute_to.getText().toString().equals(""))
                    infoText = "+ Von";
                else if (!dialogSelectRoute_from.getText().toString().equals("") && dialogSelectRoute_to.getText().toString().equals(""))
                    infoText = "+ Nach";
                else
                    infoText = "+ Von/Nach";
                neuerMarker = googleMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(infoText)
                );
                neuerMarker.showInfoWindow();
                googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                    @Override
                    public void onInfoWindowClick(Marker marker) {
                        if (dialogSelectRoute_from.getText().toString().equals("") && !dialogSelectRoute_to.getText().toString().equals("")) {
                            dialogSelectRoute_from.setText(FormatCoordinats(neuerMarker.getPosition()));
                            googleMap.addMarker(new MarkerOptions().position(neuerMarker.getPosition()).title("Von"));
                            neuerMarker.remove();
                            if (!dialogSelectRoute_from.getText().toString().equals("") && !dialogSelectRoute_to.getText().toString().equals(""))
                                routing();
                        } else if (!dialogSelectRoute_from.getText().toString().equals("") && dialogSelectRoute_to.getText().toString().equals("")) {
                            dialogSelectRoute_to.setText(FormatCoordinats(neuerMarker.getPosition()));
                            googleMap.addMarker(new MarkerOptions().position(neuerMarker.getPosition()).title("Nach"));
                            neuerMarker.remove();
                            if (!dialogSelectRoute_from.getText().toString().equals("") && !dialogSelectRoute_to.getText().toString().equals(""))
                                routing();
                        } else {
                            new AlertDialog.Builder(AddTripActivity.this)
                                    .setTitle("Möchtest du diesen Punkt als 'von', oder 'nach' verwenden")
//                                    .setMessage("Are you sure you want to delete this entry?")
                                    .setPositiveButton("Nach", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialogSelectRoute_to.setText(FormatCoordinats(neuerMarker.getPosition()));
                                            googleMap.addMarker(new MarkerOptions().position(neuerMarker.getPosition()).title("Nach"));
                                            neuerMarker.remove();
                                            if (!dialogSelectRoute_from.getText().toString().equals("") && !dialogSelectRoute_to.getText().toString().equals(""))
                                                routing();
                                        }
                                    })
                                    .setNegativeButton("Von", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialogSelectRoute_from.setText(FormatCoordinats(neuerMarker.getPosition()));
                                            googleMap.addMarker(new MarkerOptions().position(neuerMarker.getPosition()).title("Von"));
                                            neuerMarker.remove();
                                            if (!dialogSelectRoute_from.getText().toString().equals("") && !dialogSelectRoute_to.getText().toString().equals(""))
                                                routing();
                                        }
                                    })
                                    .show();

                        }

                    }
                });
            }
        });


        googleMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                dialogSelectRoute_from.clearFocus();
                dialogSelectRoute_to.clearFocus();
            }
        });
    }

    String FormatCoordinats(LatLng latLng) {
        DecimalFormat df = new DecimalFormat("#.0000");
        String lat = df.format(latLng.latitude);
        String lng = df.format(latLng.longitude);
        return "Breite: " + lat + "; Länge: " + lng;
    }

    private void routing() {
        if (dialogSelectRoute_from.getText().toString().equals("") || dialogSelectRoute_to.toString().toString().equals(""))
            return;

        // ToDo: api key aus Git entfernen und nur lokal speichern
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        from = dialogSelectRoute_from.getText().toString();
        to = dialogSelectRoute_to.getText().toString();
        String mJSONURLString = "https://maps.googleapis.com/maps/api/directions/json?origin=" +
                (!from.matches("Breite: .*; Länge: .*") ? from : getCoordsFromString(from)) +
                "&destination=" +
                (!to.matches("Breite: .*; Länge: .*") ? to : getCoordsFromString(to)) +
                "&key=AIzaSyA_G5o9xK0fZ24NXQUMnxGR_HGJzvcEw9M&language=de&mode=driving";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                mJSONURLString,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.get("status").equals("ZERO_RESULTS")) {
                                Toast.makeText(AddTripActivity.this, "Keine Verbindung gefunden", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        String distance = null;
                        try {
                            distance = (String) response.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONObject("distance").get("text");

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        dialogSelectRoute_distance.setText(distance);
                        dialogSelectRoute_distanceLayout.setVisibility(View.VISIBLE);
                        JSONArray stepsList = null;
                        try {
                            stepsList = (JSONArray) response.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONArray("steps");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        List<LatLng> latLngList = new ArrayList<>();
                        for (int i = 0; i < stepsList.length(); i++) {
                            try {
                                latLngList.addAll(PolyUtil.decode((String) stepsList.getJSONObject(i).getJSONObject("polyline").get("points")));

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        googleMap.clear();
                        googleMap.addPolyline(new PolylineOptions()
                                .addAll(latLngList)
                                .width(12)
                                .color(Color.BLUE));
                        List<LatLng> fromTo = new ArrayList<>();
                        fromTo.add(latLngList.get(0));
                        fromTo.add(latLngList.get(latLngList.size() - 1));

                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                        builder.include(fromTo.get(0));
                        builder.include(fromTo.get(1));
                        LatLngBounds bounds = builder.build();

                        markerFrom = googleMap.addMarker(new MarkerOptions()
                                .title(dialogSelectRoute_from.getText().toString())
                                .snippet("Breite: " + String.valueOf(fromTo.get(0).latitude).substring(0, 6) + "\n" + "Länge: " + String.valueOf(fromTo.get(0).longitude).substring(0, 6))
                                .position(fromTo.get(0)).icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("von", 119, 100))));
                        markerTo = googleMap.addMarker(new MarkerOptions()
                                .title(dialogSelectRoute_to.getText().toString())
                                .snippet("Breite: " + String.valueOf(fromTo.get(1).latitude).substring(0, 6) + "\nLänge: " + String.valueOf(fromTo.get(1).longitude).substring(0, 6))
                                .position(fromTo.get(1)).icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("nach", 137, 100))));

                        int padding = 75; // offset from edges of the map in pixels
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));

                        dialogSelectRoute_save.setEnabled(true);

                        loadGasPrice();

                    }
                },
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error){
                        // Do something when error occurred
//                        Snackbar.make(this,
//                                "Error.",
//                                Snackbar.LENGTH_LONG
//                        ).show();
                        Toast.makeText(AddTripActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // Add JsonObjectRequest to the RequestQueue
        requestQueue.add(jsonObjectRequest);
    }

    private void loadGasPrice() {
        LatLng location = markerFrom.getPosition();
        String request = "https://creativecommons.tankerkoenig.de/json/list.php?lat=" +
                location.latitude +
                "&lng=" +
                location.longitude +
                "&rad=5&sort=dist&type=" +
                "all" + // TODO: 04.07.2019 ändern
                "&apikey=" +
                "f9852054-1931-01c3-76b0-03d00aefcd7b"; // TODO: 04.07.2019 ändern



        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                request,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (!response.get("status").equals("ok")){
                                Toast.makeText(AddTripActivity.this, "Fehler", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        JSONObject data = null;
                        try {
                            data = response.getJSONArray("stations").getJSONObject(0);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        try {
                            fuelCostMap.put("diesel" , (Double) data.get("diesel"));
                            fuelCostMap.put("e5" , (Double) data.get("e5"));
                            fuelCostMap.put("e10" , (Double) data.get("e10"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        int i = 0;

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Do something when error occurred
//                        Snackbar.make(this,
//                                "Error.",
//                                Snackbar.LENGTH_LONG
//                        ).show();
                        Toast.makeText(AddTripActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // Add JsonObjectRequest to the RequestQueue
        requestQueue.add(jsonObjectRequest);
    }

    String getCoordsFromString(String coords) {
        return custSubString(coords.split(";")[0], 8).replace(",", ".") + "," +
                custSubString(coords.split(";")[1], 8).replace(",", ".");
    }

    public Bitmap resizeMapIcons(String iconName, int width, int height) {
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier(iconName, "drawable", getPackageName()));
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        return resizedBitmap;
    }


    private LatLng geoLocate(TextView view) {
        Log.d(TAG, "geoLocate: geolocating");

        String searchString = view.getText().toString();
        if (searchString.matches("Breite: .*; Länge: .*")) {
            return new LatLng(Double.valueOf(searchString.split(";")[0].substring(8).replace(",", ".")),
                    Double.valueOf(searchString.split(";")[1].substring(8).replace(",", ".")));
        }

        Geocoder geocoder = new Geocoder(AddTripActivity.this);
        List<Address> list = new ArrayList<>();
        try {
            list = geocoder.getFromLocationName(searchString, 1);
        } catch (IOException e) {
            Log.d(TAG, "geoLocate: IOExeption: " + e.getMessage());
        }

        LatLng foundPosition = null;
        if (!list.isEmpty()) {
            Address address = list.get(0);
            Log.d(TAG, "geoLocate: found a Location: " + address.toString());
//            msgBox(address.toString());
            foundPosition = new LatLng(address.getLatitude(), address.getLongitude());

            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(foundPosition, 15));
        } else
            Toast.makeText(this, searchString + " nicht gefunden", Toast.LENGTH_SHORT).show();
        return foundPosition;
    }

    String custSubString(String string, int start, int end) {
        if (start < 0)
            start = string.length() + start;
        if (end < 0)
            end = string.length() + end;
        return string.substring(start, end);
    }

    String custSubString(String string, int start) {
        return custSubString(string, start, string.length());
    }


}

