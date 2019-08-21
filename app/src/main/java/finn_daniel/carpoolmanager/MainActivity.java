package finn_daniel.carpoolmanager;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, NetworkStateReceiver.NetworkStateReceiverListener {

//    List<String> listviewTitle = new ArrayList<String>();
//    List<Boolean> listviewisDriver = new ArrayList<>();
//    List<String> listviewPassengers = new ArrayList<>();
//    List<java.io.Serializable> listviewOwnDrivenAmount = new ArrayList<>();
//    List<java.io.Serializable> listviewAllDrivenAmount = new ArrayList<>();

//    ListView listView_groupList;
    CustomRecycler customRecycler_groupList;
    View contentView_groups;
    View contentView_account;
    ViewStub stub_groups;
    ViewStub stub_account;
    NavigationView navigationView;

    List<String> createData_gruppenNamen;
    List<String> createData_emailAddressen;
    List<Group.costCalculationType> createData_gruppenCostCalcType;
    List<Group.costCalculationMethod> createData_gruppenCostCalcMethod;
    List<String> createData_userNamen;
    ArrayList<ArrayList<String>> createData_mitfahrer;
    ArrayList<ArrayList<String>> createData_fahrer;
    ArrayList<ArrayList<Integer>> createData_fahrten;

    Map<String, User> createData_userMap = new HashMap<>();
    Map<String, List<String>> createData_userGroupMap = new HashMap<>();
    Map<String, String> createData_userIdMap = new HashMap<>();
    Map<String, String> createData_groupIdMap = new HashMap<>();

    Gson gson = new Gson();
    DatabaseReference databaseReference;
    private NetworkStateReceiver networkStateReceiver;
    boolean wizzardManuellAktiviert = false;
    SharedPreferences mySPR_daten;
    SharedPreferences mySPR_settings;
    int shownContent;



    int SETTINGS_INTENT = 001;
    String EXTRA_USER = "EXTRA_USER";
    private String EXTRA_GROUP = "EXTRA_GROUP";
    private String EXTRA_PASSENGERMAP = "EXTRA_PASSENGERMAP";
    private String EXTRA_TRIPMAP = "EXTRA_TRIPMAP";
    int loggedInUser_passengerCount = 0;


    Map<String, Boolean> hasGroupChangeListener = new HashMap<>();
    User loggedInUser;
    String loggedInUser_Name = "FinnF";
    List<String> loggedInUser_groupsIdList = new ArrayList<>(); //<---
    Map<String, Group> loggedInUser_groupsMap = new HashMap<>(); //<---
    Map<String, User> loggedInUser_groupPassengerMap = new HashMap<>(); //<---
    List<Group> sortedGroupList;
    Map<String, Map<String, Trip>> loggedInUser_groupTripMap = new HashMap<>(); //<---

    ValueEventListener groupChangeListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if (dataSnapshot.getValue() == null) {
                // ToDo: Gruppe aus speicherungen der nutzer Löschen
                final String removedGroup = dataSnapshot.getKey();
                for (String user : loggedInUser_groupsMap.get(removedGroup).getUserIdList()) {
                    databaseReference.child("Users").child(user).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.getValue() == null)
                                return;
                            User foundUser = dataSnapshot.getValue(User.class);
                            foundUser.getGroupIdList().remove(removedGroup);
                            databaseReference.child("Users").child(foundUser.getUser_id()).setValue(foundUser);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
                }

                loggedInUser_groupsIdList.remove(removedGroup);
                loggedInUser_groupsMap.remove(removedGroup);
                listeLaden();
                return;
            }
            // ToDo: was passiert wenn gelöscht? eventuell per cloud function?
            Group foundGroup = dataSnapshot.getValue(Group.class);
            if (!hasGroupChangeListener.get(foundGroup.getGroup_id())) {  // neue Gruppe?
                hasGroupChangeListener.put(foundGroup.getGroup_id(), true);
                return;
            }

//            Findet heraus, ob ein Nutzer eingetreten, oder ausgetreten ist
            if (!foundGroup.getUserIdList().equals(loggedInUser_groupsMap.get(foundGroup.getGroup_id()).getUserIdList())) {
                onChangedUsers(foundGroup);
                return;
            }

//            Findet heraus, ob sich bei den Trips was verändert hat --> loggedInUser_groupTripMap muss aktuallisiert werden
            if (!foundGroup.getTripIdList().equals(loggedInUser_groupsMap.get(foundGroup.getGroup_id()).getTripIdList())) {
                onChangedTrip(foundGroup);
                return;
            }

            loggedInUser_groupsMap.replace(foundGroup.getGroup_id(), foundGroup); // Gruppe wird aktuallisiert

            listeLaden();

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
        }

        void onChangedTrip(final Group foundGroup) {
            final List<List<String>> changeList = foundGroup.getChangedTripsLists(loggedInUser_groupTripMap.get(foundGroup.getGroup_id()).keySet());

            if (changeList.get(0) != null) {
                for (String trip : new ArrayList<>(changeList.get(0))) {
                    databaseReference.child("Trips").child(foundGroup.getGroup_id()).child(trip).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.getValue() == null)
                                return;
                            Trip foundTrip = dataSnapshot.getValue(Trip.class);
                            changeList.get(0).remove(foundTrip.getTrip_id());
                            loggedInUser_groupTripMap.get(foundGroup.getGroup_id()).put(foundTrip.getTrip_id(), foundTrip);
                            loggedInUser_groupsMap.get(foundGroup.getGroup_id()).getTripIdList().add(foundTrip.getTrip_id());
//                        loggedInUser_groupPassengerMap.put(foundTrip.getUser_id(), foundTrip);
                            if (changeList.get(0).size() <= 0)
                                listeLaden();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
                }
            }
            if (changeList.get(1) != null) {
                for (String tripId : changeList.get(1)) {
                    loggedInUser_groupTripMap.get(foundGroup.getGroup_id()).remove(tripId);
                    loggedInUser_groupsMap.get(foundGroup.getGroup_id()).getTripIdList().remove(tripId);
                }
                listeLaden();
            }
        }
        void onChangedUsers(Group foundGroup) {
            final List<List<String>> changeList = foundGroup.getChangedUserLists(loggedInUser_groupsMap.get(foundGroup.getGroup_id()));

            for (String user : changeList.get(1)) {
                loggedInUser_groupPassengerMap.remove(user);
            }

            for (String user : new ArrayList<>(changeList.get(0))) {
                changeList.get(0).remove(user);
                databaseReference.child("Users").child(user).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue() == null)
                            return;
                        User foundUser = dataSnapshot.getValue(User.class);
                        loggedInUser_groupPassengerMap.put(foundUser.getUser_id(), foundUser);
                        loggedInUser_groupsMap.get(foundGroup.getGroup_id()).getUserIdList().add(foundUser.getUser_id());
                        if (changeList.get(0).size() <= 0)
                            listeLaden();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mySPR_daten = getSharedPreferences("CarPoolManager_Daten", 0);
        mySPR_settings = getSharedPreferences("CarPoolManager_Settings", 0);
        navigationView = findViewById(R.id.nav_view);
        databaseReference = FirebaseDatabase.getInstance().getReference();

        String loggedInUser_string = mySPR_daten.getString("loggedInUser", "--Leer--");
        if (!loggedInUser_string.equals("--Leer--")) {
            loggedInUser = gson.fromJson(loggedInUser_string, User.class);
            // ToDo: Handle nicht angemeldet
        }

        stub_groups = findViewById(R.id.layout_stub_groups);
        stub_groups.setLayoutResource(R.layout.main_content_groups);
        contentView_groups = stub_groups.inflate();
        navigationView.setCheckedItem(R.id.navigation_menu_groups);
        shownContent = R.id.navigation_menu_groups;

        if (mySPR_settings.getInt("standardView_main", R.id.navigation_menu_groups) != R.id.navigation_menu_groups
                    && loggedInUser != null) {
            stub_account = MainActivity.this.findViewById(R.id.layout_stub_account);
            stub_account.setLayoutResource(R.layout.main_content_account);
            contentView_account = stub_account.inflate();
            contentView_groups.setVisibility(View.GONE);
            setAccountView();
            navigationView.setCheckedItem(R.id.navigation_menu_account);
            shownContent = R.id.navigation_menu_account;
        }

//        mySPR_daten.edit().putString("loggedInUserId", "user_2a48b5ec-bc70-4b5a-8c1d-b76384cec163").commit();

        networkStateReceiver = new NetworkStateReceiver();
        networkStateReceiver.addListener(this);
        this.registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.main_groups_addGroup);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int saveButtonId = View.generateViewId();
                Dialog dialog_newGroup = CustomDialog.Builder(MainActivity.this)
                        .setTitle("Neue Gruppe Erstellen")
                        .setView(R.layout.dialog_new_group)
                        .setButtonType(CustomDialog.ButtonType.SAVE_CANCEL)
                        .addButton(CustomDialog.SAVE_BUTTON, dialog -> {
                            Group newGroup = new Group();

                            newGroup.setName(((EditText) dialog.findViewById(R.id.dialogNewGroup_name)).getText().toString().trim());
                            if (newGroup.getName().equals("")) {
                                Toast.makeText(MainActivity.this, "Einen Gruppen-Namen angeben", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            switch (((RadioGroup)(dialog.findViewById(R.id.dialogNewGroup_typeGroup))).getCheckedRadioButtonId()) {
                                case R.id.dialogNewGroup_budgetRadio:
                                    newGroup.setCalculationType(Group.costCalculationType.BUDGET);
                                    break;
                                case R.id.dialogNewGroup_costRadio:
                                    newGroup.setCalculationType(Group.costCalculationType.COST);
                                    break;
                            }
                            switch (((RadioGroup)(dialog.findViewById(R.id.dialogNewGroup_methodGroup))).getCheckedRadioButtonId()) {
                                case R.id.dialogNewGroup_realCostRadio:
                                    newGroup.setCalculationMethod(Group.costCalculationMethod.ACTUAL_COST);
                                    break;
                                case R.id.dialogNewGroup_kilometerAllowanceRadio:
                                    newGroup.setCalculationMethod(Group.costCalculationMethod.KIKOMETER_ALLOWANCE);
                                    break;
                                case R.id.dialogNewGroup_tripRadio:
                                    newGroup.setCalculationMethod(Group.costCalculationMethod.TRIP);
                                    break;
                            }
                            EditText dialogNewGroup_budget = dialog.findViewById(R.id.dialogNewGroup_budget);
                            if (dialog.findViewById(R.id.dialogNewGroup_budget).isEnabled()) {
                                if (!dialogNewGroup_budget.getText().toString().equals("")) {
                                    newGroup.setBudget(Double.parseDouble(dialogNewGroup_budget.getText().toString()));
                                } else {
                                    Toast.makeText(MainActivity.this, "Ein Budget angeben", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            }

                            EditText dialogNewGroup_kilometerAllowance = dialog.findViewById(R.id.dialogNewGroup_kilometerAllowance);
                            if (dialogNewGroup_kilometerAllowance.isEnabled()) {
                                if (!dialogNewGroup_kilometerAllowance.getText().toString().equals("")) {
                                    newGroup.setKilometerAllowance(Double.parseDouble(dialogNewGroup_kilometerAllowance.getText().toString()));
                                } else {
                                    Toast.makeText(MainActivity.this, "Eine Pauschale angeben", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            }

                            newGroup.setBudgetPerUser(((CheckBox) dialog.findViewById(R.id.dialogNewGroup_perPerson)).isChecked());
                            newGroup.getUserIdList().add(loggedInUser.getUser_id());
                            newGroup.getDriverIdList().add(loggedInUser.getUser_id());

                            databaseReference.child("Groups").child(newGroup.getGroup_id()).setValue(newGroup);

                            dialog.dismiss();

                            loggedInUser.getGroupIdList().add(newGroup.getGroup_id());
                            loggedInUser_groupsMap.put(newGroup.getGroup_id(), newGroup);
                            loggedInUser_groupTripMap.put(newGroup.getGroup_id(), new HashMap<>());
                            databaseReference.child("Users").child(loggedInUser.getUser_id()).child("groupIdList").setValue(loggedInUser.getGroupIdList());

                            hasGroupChangeListener.put(newGroup.getGroup_id(), false);
                            databaseReference.child("Groups").child(newGroup.getGroup_id()).addValueEventListener(groupChangeListener);

                            listeLaden();
                        }, saveButtonId, false)
                        .show();
                dialog_newGroup.findViewById(saveButtonId).setEnabled(false);
                setChangeRadioButtonListener(dialog_newGroup, dialog_newGroup.findViewById(R.id.dialogNewGroup_typeGroup),
                        dialog_newGroup.findViewById(R.id.dialogNewGroup_methodGroup), saveButtonId);
            }
        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);


//        listView_groupList = findViewById(R.id.listView_groupList);

        if (!Utility.isOnline()) {

            String loggedInUser_groupsMap_string = mySPR_daten.getString("loggedInUser_groupsMap", "--Leer--");
            if (!loggedInUser_groupsMap_string.equals("--Leer--")) {
                loggedInUser_groupsMap = gson.fromJson(
                        loggedInUser_groupsMap_string, new TypeToken<HashMap<String, Group>>() {
                        }.getType()
                );
            } else {
                Toast.makeText(this, "Fehler beim Laden der Offline Daten", Toast.LENGTH_SHORT).show();
                return;
            }

            String loggedInUser_groupPassengerMap_string = mySPR_daten.getString("loggedInUser_groupPassengerMap", "--Leer--");
            if (!loggedInUser_groupPassengerMap_string.equals("--Leer--")) {
                loggedInUser_groupPassengerMap = gson.fromJson(
                        loggedInUser_groupPassengerMap_string, new TypeToken<HashMap<String, User>>() {
                        }.getType()
                );
            } else {
                Toast.makeText(this, "Fehler beim Laden der Offline Daten", Toast.LENGTH_SHORT).show();
                return;
            }

            String loggedInUser_groupTripMap_string = mySPR_daten.getString("loggedInUser_groupTripMap", "--Leer--");
            if (!loggedInUser_groupTripMap_string.equals("--Leer--")) {
                loggedInUser_groupTripMap = gson.fromJson(
                        loggedInUser_groupTripMap_string, new TypeToken<Map<String, Map<String, Trip>>>() {
                        }.getType()
                );
            } else {
                Toast.makeText(this, "Fehler beim Laden der Offline Daten", Toast.LENGTH_SHORT).show();
                return;
            }

            listeLaden();
            return;
        }

    }

    private void getUserFromUserId(String loggedInUserId_string) {
        databaseReference.child("Users").child(loggedInUserId_string).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null)
                    return;
                loggedInUser = dataSnapshot.getValue(User.class);

                reloadLoggedInUser();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }

    private void setChangeRadioButtonListener(final Dialog dialog_newGroup, RadioGroup dialogNewGroup_typeGroup, RadioGroup dialogNewGroup_methodGroup, int saveButtonId) {
        dialogNewGroup_typeGroup.setOnCheckedChangeListener((radioGroup, checkedId) -> {
            switch (checkedId) {
                case R.id.dialogNewGroup_budgetRadio:
                    dialog_newGroup.findViewById(R.id.dialogNewGroup_budget).setEnabled(true);
                    dialog_newGroup.findViewById(R.id.dialogNewGroup_perPerson).setEnabled(true);
                    dialog_newGroup.findViewById(R.id.dialogNewGroup_tripRadio).setEnabled(true);
                    break;
                case R.id.dialogNewGroup_costRadio:
                    dialog_newGroup.findViewById(R.id.dialogNewGroup_budget).setEnabled(false);
                    dialog_newGroup.findViewById(R.id.dialogNewGroup_perPerson).setEnabled(false);
                    RadioButton radioButton = dialog_newGroup.findViewById(R.id.dialogNewGroup_tripRadio);
                    radioButton.setEnabled(false);
                    if (radioButton.isChecked()) {
                        dialogNewGroup_methodGroup.clearCheck();
                        dialog_newGroup.findViewById(saveButtonId).setEnabled(false);
                    }
                    break;
            }
            if(dialogNewGroup_methodGroup.getCheckedRadioButtonId() != -1)
                dialog_newGroup.findViewById(saveButtonId).setEnabled(true);

        });
        dialogNewGroup_methodGroup.setOnCheckedChangeListener((radioGroup, checkedId) -> {
            if(dialogNewGroup_typeGroup.getCheckedRadioButtonId() != -1)
                dialog_newGroup.findViewById(saveButtonId).setEnabled(true);

            dialog_newGroup.findViewById(R.id.dialogNewGroup_kilometerAllowance)
                    .setEnabled(checkedId == R.id.dialogNewGroup_kilometerAllowanceRadio);
        });
    }


    private void showNoInternetSnackBar() {
        Snackbar.make(contentView_groups, "Es gibt keine verbindung zum Internet!", Snackbar.LENGTH_LONG)
                .setAction("Aktivieren", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showActivateInternetDialog();
                    }
                })
                .show();
    }

    private void showActivateInternetDialog() {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Was möchtest du aktivieren?")
//                                    .setMessage("Are you sure you want to delete this entry?")
                .setPositiveButton("W-Lan", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                        wifi.setWifiEnabled(true);
                        msgBox("W-Lan aktiviert");
                    }
                })
                .setNegativeButton("Mobiel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.setComponent(new ComponentName("com.android.settings",
                                "com.android.settings.Settings$DataUsageSummaryActivity"));
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                })
                .show();
    }


    private void reloadLoggedInUser() {
        if (loggedInUser == null) {
            String loggedInUserId_string = mySPR_daten.getString("loggedInUserId", "--Leer--");
            if (!loggedInUserId_string.equals("--Leer--")) {
                getUserFromUserId(loggedInUserId_string);
            }
            else
                Toast.makeText(this, "Fehler", Toast.LENGTH_SHORT).show();
            return;
        }
        View view = navigationView.getHeaderView(0);
        ((TextView) view.findViewById(R.id.main_navView_name)).setText(loggedInUser.getUserName());
        ((TextView) view.findViewById(R.id.main_navView_eMail)).setText(loggedInUser.getEmailAddress());
        databaseReference.child("Users").child(loggedInUser.getUser_id()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null)
                    return;

                loggedInUser = dataSnapshot.getValue(User.class);
                getGroupsfromUser();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void getGroupsfromUser() {
        loggedInUser_groupsIdList = loggedInUser.getGroupIdList();
        if (loggedInUser_groupsIdList.size() == 0) {
            TextView main_groupInfo = findViewById(R.id.main_groups_groupInfo);
            main_groupInfo.setText("Du bist aktuell in keiner Fahrgemeinschaft");
            findViewById(R.id.main_groups_loadData).setVisibility(View.GONE);
            return;
        }
        for (String groupId : loggedInUser_groupsIdList) {
            databaseReference.child("Groups").child(groupId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() == null)
                        return;
                    Group foundGroup = dataSnapshot.getValue(Group.class);
                    loggedInUser_groupsMap.put(foundGroup.getGroup_id(), foundGroup);
                    if (loggedInUser_groupsMap.size() == loggedInUser_groupsIdList.size()) {
                        getGroupPassengers();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
            hasGroupChangeListener.put(groupId, false);
            addGroupChangeListener(groupId);
        }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//            }
//        });
    }

    private void getGroupPassengers() {
        for (Map.Entry<String, Group> entry : loggedInUser_groupsMap.entrySet()) {
            loggedInUser_passengerCount += entry.getValue().getUserIdList().size();
        }
        for (Map.Entry<String, Group> entry : loggedInUser_groupsMap.entrySet()) {
            for (String user : entry.getValue().getUserIdList()) {
                databaseReference.child("Users").child(user).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue() == null)
                            return;
                        User foundUser = dataSnapshot.getValue(User.class);
                        loggedInUser_groupPassengerMap.put(foundUser.getUser_id(), foundUser);
                        loggedInUser_passengerCount--;
                        if (loggedInUser_passengerCount == 0) {
                            getGroupTrips();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });

            }
        }
    }

    private void getGroupTrips() {
        loggedInUser_groupTripMap.clear();
        for (final String groupId : loggedInUser.getGroupIdList()) {
            if (loggedInUser_groupsMap.get(groupId).getTripIdList().size() == 0) {
                loggedInUser_groupTripMap.put(groupId, new HashMap<String, Trip>());
                if (loggedInUser_groupTripMap.size() >= loggedInUser.getGroupIdList().size()) {
                    listeLaden();
//                    listeClickListener();   //<==
                    return;
                }
                else
                    continue;
            }
            databaseReference.child("Trips").child(groupId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() == null)
                        return;
                    Map<String, Trip> newMap = new HashMap<>();
                    for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
                        Trip foundTrip = messageSnapshot.getValue(Trip.class);
                        newMap.put(foundTrip.getTrip_id(), foundTrip);
                    }
                    loggedInUser_groupTripMap.put(groupId, newMap);
                    if (loggedInUser_groupTripMap.size() >= loggedInUser.getGroupIdList().size()) {
                        listeLaden();
//                        listeClickListener();    //<==
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    String  test = null;
                }
            });
        }
    }

    private void addGroupChangeListener(final String groupId) {
        databaseReference.child("Groups").child(groupId).removeEventListener( groupChangeListener);
        databaseReference.child("Groups").child(groupId).addValueEventListener(groupChangeListener);
    }

    @Override
    protected void onStop() {
        SharedPreferences mySPR = getSharedPreferences("CarPoolManager_Daten", 0);
        SharedPreferences.Editor editor = mySPR.edit();
        editor.clear();

        editor.putString("loggedInUser_groupsMap", gson.toJson(loggedInUser_groupsMap));
        editor.putString("loggedInUser_groupPassengerMap", gson.toJson(loggedInUser_groupPassengerMap));
        editor.putString("loggedInUser_groupTripMap", gson.toJson(loggedInUser_groupTripMap));

//        for (Map.Entry<String, Group> entry : loggedInUser_groupsMap.entrySet()) {
//            editor.putString("loggedInUser_groupsList_" + count, gson.toJson(entry.getValue()));
//            count++;
//        }
//        for (Map.Entry<String, User> entry : loggedInUser_groupPassengerMap.entrySet()) {
//            editor.putString("loggedInUser_groupPassengerMap_" + count, gson.toJson(entry.getValue()));
//            count++;
//        }
//        editor.putString("loggedInUser_Id", loggedInUser_Id);

        editor.putString("loggedInUser", gson.toJson(loggedInUser));
        editor.commit();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        networkStateReceiver.removeListener(this);
        this.unregisterReceiver(networkStateReceiver);
    }

    @Override
    public void networkAvailable() {
        findViewById(R.id.main_groups_noInternet).setVisibility(View.GONE);
        findViewById(R.id.main_groups_loadData).setVisibility(View.VISIBLE);
//        createListData();
        reloadLoggedInUser();
//        getGroupsfromUser();
    }

    @Override
    public void networkUnavailable() {
        showNoInternetSnackBar();
        TextView main_noInternet = findViewById(R.id.main_groups_noInternet);
        main_noInternet.setVisibility(View.VISIBLE);
        main_noInternet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!Utility.isOnline()) {
                    showActivateInternetDialog();
                } else {
                    findViewById(R.id.main_groups_noInternet).setVisibility(View.GONE);
//                    getGroupsfromUser();
                    reloadLoggedInUser();
                }

            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (shownContent != mySPR_settings.getInt("standardView_main", R.id.navigation_menu_groups)) {
                contentView_groups.setVisibility(shownContent == R.id.navigation_menu_groups ? View.GONE : View.VISIBLE);
                contentView_account.setVisibility(shownContent == R.id.navigation_menu_groups ? View.VISIBLE : View.GONE);
                shownContent = shownContent == R.id.navigation_menu_groups ? R.id.navigation_menu_account : R.id.navigation_menu_groups;

                navigationView.setCheckedItem(shownContent);
            } else {
                super.onBackPressed();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {
            case R.id.navigation_menu_groups:
                if (contentView_groups == null) {
                    stub_groups = MainActivity.this.findViewById(R.id.layout_stub_groups);
                    stub_groups.setLayoutResource(R.layout.main_content_groups);
                    contentView_groups = stub_groups.inflate();
                    contentView_account.setVisibility(View.GONE);
                }
                contentView_groups.setVisibility(View.VISIBLE);
                contentView_account.setVisibility(View.GONE);
                shownContent = id;
                break;
            case R.id.navigation_menu_account:
                if (contentView_account == null) {
                    stub_account = MainActivity.this.findViewById(R.id.layout_stub_account);
                    stub_account.setLayoutResource(R.layout.main_content_account);
                    contentView_account = stub_account.inflate();
                    contentView_groups.setVisibility(View.GONE);
                    setAccountView();
                }
                contentView_groups.setVisibility(View.GONE);
                contentView_account.setVisibility(View.VISIBLE);
                shownContent = id;
                break;
            case R.id.navigation_menu_settings:
                Intent intent = new Intent(MainActivity.this, Settings.class);
                startActivityForResult(intent, SETTINGS_INTENT);
                break;
        }


        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setAccountView() {
        ((TextView) contentView_account.findViewById(R.id.main_account_name)).setText(loggedInUser.getUserName());
        databaseReference.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null)
                    return;

                List<User> userList = new ArrayList<>();
                for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
                    User foundUser = messageSnapshot.getValue(User.class);
                    userList.add(foundUser);
                }

                CustomRecycler.Builder(MainActivity.this, MainActivity.this.findViewById(R.id.main_account_groupList))
                        .setItemView(R.layout.list_item_select_user)
                        .setObjectList(userList)
                        .setViewList(viewIdList -> {
                            viewIdList.add(R.id.selectUserList_name);
                            viewIdList.add(R.id.selectUserList_email);
                            viewIdList.add(R.id.selectUserList_selected);
                            return viewIdList;
                        })
                        .setSetItemContent((viewHolder, ViewIdMap, object) -> {
                            User user = (User) object;
                            ((TextView) ViewIdMap.get(R.id.selectUserList_name)).setText(user.getUserName());
                            ((TextView) ViewIdMap.get(R.id.selectUserList_email)).setText(user.getEmailAddress());
                            if (user.equals(loggedInUser))
                                ((CheckBox) ViewIdMap.get(R.id.selectUserList_selected)).setChecked(true);
                        })
                        .setOnClickListener((recycler, view, object, index) -> {
//                            CheckBox checkBox = view.findViewById(R.id.selectUserList_selected);
//                            checkBox.setChecked(!checkBox.isChecked());
                            CustomDialog.Builder(MainActivity.this)
                                    .setTitle("Benutzer Wechseln")
                                    .setText("Du wirst als '" + ((User) object).getUserName() + "' angemeldet?")
                                    .setButtonType(CustomDialog.ButtonType.OK_CANCEL)
                                    .addButton(CustomDialog.OK_BUTTON, dialog -> {
                                        mySPR_daten.edit().clear().commit();
                                        mySPR_daten.edit().putString("loggedInUserId", ((User) object).getUser_id()).commit();
                                        Utility.restartApp(MainActivity.this);
//                                        reloadLoggedInUser();
//                                        navigationView.setCheckedItem(R.id.groups);
                                        return;
                                    })
                                    .show();
                        })
                        .generate();


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if (requestCode == SETTINGS_INTENT && resultCode == RESULT_OK) {
            listeLaden();
        }
    }

    String calculateDrivenAmount(String userId, String groupId) {
        double count = 0;
        for (String tripId : loggedInUser_groupsMap.get(groupId).getTripIdList()) {
            Trip trip = loggedInUser_groupTripMap.get(groupId).get(tripId);
            if (trip.getDriverId().equals(userId) || userId == null) {
                count++;
                if (trip.isTwoWay())
                    count++;
            }
        }
        if (mySPR_settings.getString("tripCount", "Pro Weg").equals("Pro Fahrt"))
            count /= 2;

        if (count % 1 == 0)
            return String.valueOf(count).split("\\.")[0];
        else
            return String.valueOf(count);
    }

    public void listeLaden() {

        TextView main_groupInfo = findViewById(R.id.main_groups_groupInfo);
        main_groupInfo.setVisibility(View.GONE);

        sortedGroupList = new ArrayList<>(loggedInUser_groupsMap.values());
        Collections.sort(sortedGroupList, new Comparator() {
            public int compare(Group obj1, Group obj2) {
                return obj1.getName().compareTo(obj2.getName());
            }

            public int compare(Object var1, Object var2) {
                return this.compare((Group) var1, (Group) var2);
            }
        });

        if (sortedGroupList.size() == 0) {
            main_groupInfo.setVisibility(View.VISIBLE);
            main_groupInfo.setText("Du bist aktuell in keiner Fahrgemeinschaft");
        }

        customRecycler_groupList = CustomRecycler.Builder(this, findViewById(R.id.main_groups_groupList))
                .setItemView(R.layout.list_item_group)
                .setObjectList(sortedGroupList)
                .setViewList(viewIdList -> {
                    viewIdList.add(R.id.groupList_image);
                    viewIdList.add(R.id.groupList_name);
                    viewIdList.add(R.id.groupList_passengers);
                    viewIdList.add(R.id.groupList_ownAmount);
                    viewIdList.add(R.id.groupList_allAmount);
                    return viewIdList;
                })
                .setMultipleClickDelay(1000)
                .setSetItemContent((viewHolder, ViewIdMap, object) -> {
                    Group group = (Group) object;

                    ((ImageView) ViewIdMap.get(R.id.groupList_image)).setImageResource(
                            group.getDriverIdList().contains(loggedInUser.getUser_id())
                                    ? R.drawable.ic_lenkrad : R.drawable.ic_leer);
                    ((TextView) ViewIdMap.get(R.id.groupList_name)).setText(group.getName());
                    String passengers = "";
                    for (int n = 0; n < group.getUserIdList().size(); n++) {
                        passengers = passengers.concat(loggedInUser_groupPassengerMap.get(group.getUserIdList().get(n)).getUserName());
                        if (n < group.getUserIdList().size() - 1)
                            passengers = passengers.concat(", ");
                    }
                    ((TextView) ViewIdMap.get(R.id.groupList_passengers)).setText(passengers);
                    ((TextView) ViewIdMap.get(R.id.groupList_ownAmount)).setText(calculateDrivenAmount(loggedInUser.getUser_id(), group.getGroup_id()));
                    ((TextView) ViewIdMap.get(R.id.groupList_allAmount)).setText(calculateDrivenAmount(null, group.getGroup_id()));
                })
                .setOnClickListener((recycler, view, object, index) -> {
                    Group selectedGroup = sortedGroupList.get(index);
                    Intent intent = new Intent(MainActivity.this, GroupActivity.class);
                    intent.putExtra(EXTRA_USER, gson.toJson(loggedInUser));
                    intent.putExtra(EXTRA_GROUP, gson.toJson(selectedGroup));
                    Map<String, User> passengerMap = new HashMap<>();
                    for (String userId : selectedGroup.getUserIdList()) {
                        passengerMap.put(userId, loggedInUser_groupPassengerMap.get(userId));
                    }
                    intent.putExtra(EXTRA_PASSENGERMAP, gson.toJson(passengerMap));
                    intent.putExtra(EXTRA_TRIPMAP, gson.toJson(loggedInUser_groupTripMap.get(selectedGroup.getGroup_id())));
                    startActivity(intent);
                })
                .generateCustomRecycler();
        // ToDo: änder neuladen der liste von komplette methode neuladen zu .reload()

        findViewById(R.id.main_groups_loadData).setVisibility(View.GONE);
    }

    public void msgBox(String nachricht) {
        Toast.makeText(this, nachricht, Toast.LENGTH_SHORT).show();
    }

    public void startWizzard(View view) {
        wizzardManuellAktiviert = true;
        createListData();
    }

    public void createListData() {
        createData_gruppenNamen = new ArrayList<>(Arrays.asList("Auf zur Arbeit", "FHDW-Gruppe", "FußballTeam"));
        createData_gruppenCostCalcType = new ArrayList<>(Arrays.asList(Group.costCalculationType.BUDGET, Group.costCalculationType.BUDGET ,Group.costCalculationType.COST ));
        createData_gruppenCostCalcMethod = new ArrayList<>(Arrays.asList(Group.costCalculationMethod.ACTUAL_COST, Group.costCalculationMethod.TRIP ,Group.costCalculationMethod.KIKOMETER_ALLOWANCE ));
        createData_userNamen = new ArrayList<>(Arrays.asList("HansP", "DanielP", "FinnF", "GünterM", "HansD", "JohnL", "MarkusG", "TomN", "KarlF"));
        createData_emailAddressen = new ArrayList<>(Arrays.asList("Hans.P@gmail.com", "Daniel.P@gmail.com", "Finn.Flaschka@gmail.com", "Günter.M@gmail.com", "Hans.D@gmail.com", "John.L@gmail.com", "Markus.G@gmail.com", "Tom.N@gmail.com", "Karl.F@gmail.com"));

        createData_userGroupMap.put("HansP", new ArrayList<>(Arrays.asList("Auf zur Arbeit")));
        createData_userGroupMap.put("DanielP", new ArrayList<>(Arrays.asList("Auf zur Arbeit", "FHDW-Gruppe")));
        createData_userGroupMap.put("FinnF", new ArrayList<>(Arrays.asList("Auf zur Arbeit", "FHDW-Gruppe", "FußballTeam")));
        createData_userGroupMap.put("GünterM", new ArrayList<>(Arrays.asList("FHDW-Gruppe")));
        createData_userGroupMap.put("HansD", new ArrayList<>(Arrays.asList("FHDW-Gruppe")));
        createData_userGroupMap.put("JohnL", new ArrayList<>(Arrays.asList("FHDW-Gruppe")));
        createData_userGroupMap.put("MarkusG", new ArrayList<>(Arrays.asList("FußballTeam")));
        createData_userGroupMap.put("TomN", new ArrayList<>(Arrays.asList("FußballTeam")));
        createData_userGroupMap.put("KarlF", new ArrayList<>(Arrays.asList("FußballTeam")));

        createData_mitfahrer = new ArrayList<>();
        createData_mitfahrer.add(new ArrayList<>(Arrays.asList("HansP", "DanielP", "FinnF")));
        createData_mitfahrer.add(new ArrayList<>(Arrays.asList("GünterM", "HansD", "DanielP", "JohnL", "FinnF")));
        createData_mitfahrer.add(new ArrayList<>(Arrays.asList("MarkusG", "TomN", "KarlF", "FinnF")));

        createData_fahrer = new ArrayList<>();
        createData_fahrer.add(new ArrayList<>(Arrays.asList("HansP", "DanielP", "FinnF")));
        createData_fahrer.add(new ArrayList<>(Arrays.asList("GünterM", "HansD", "JohnL")));
        createData_fahrer.add(new ArrayList<>(Arrays.asList("TomN", "FinnF")));

        createData_fahrten = new ArrayList<>();
        createData_fahrten.add(new ArrayList<>(Arrays.asList(7, 31)));
        createData_fahrten.add(new ArrayList<>(Arrays.asList(0, 568)));
        createData_fahrten.add(new ArrayList<>(Arrays.asList(9, 10)));

        databaseReference.child("Groups").removeValue();
        databaseReference.child("Users").removeValue();
        databaseReference.child("Cars").removeValue();
        databaseReference.child("Trips").removeValue();

        for (String name : createData_gruppenNamen) {
            int i = createData_gruppenNamen.indexOf(name);
            Group newGroup = new Group();
            newGroup.setName(name);
            newGroup.setCalculationType(createData_gruppenCostCalcType.get(i));
            newGroup.setCalculationMethod(createData_gruppenCostCalcMethod.get(i));
            createData_groupIdMap.put(name, newGroup.getGroup_id());
            databaseReference.child("Groups").child(newGroup.getGroup_id()).setValue(newGroup);
        }

        loggedInUser = new User();
        int count = 0;
        for (String name : createData_userNamen) {
            User newUser = new User();
//            newUser.generateId();
            newUser.setUserName(name);
            newUser.setEmailAddress(createData_emailAddressen.get(count));
            Random random = new Random();
            newUser.setUserColor("#" + Integer.toHexString(Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256))).toUpperCase());
            createData_userIdMap.put(name, newUser.getUser_id());
            for (String gruppe : createData_userGroupMap.get(name)) {
                newUser.addGroup(createData_groupIdMap.get(gruppe));
            }

            if (name.equals(loggedInUser_Name)) {
                loggedInUser = newUser;
                SharedPreferences.Editor editor = mySPR_daten.edit();
                editor.putString("loggedInUser", gson.toJson(loggedInUser));
                editor.commit();
            }

            databaseReference.child("Users").child(newUser.getUser_id()).setValue(newUser);
            count++;
        }

        databaseReference.child("Groups").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null)
                    return;


                for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
                    Group foundGroup = messageSnapshot.getValue(Group.class);
                    for (String user : createData_mitfahrer.get(createData_gruppenNamen.indexOf(foundGroup.getName()))) {
                        foundGroup.addUser(createData_userIdMap.get(user));
                    }
                    for (String user : createData_fahrer.get(createData_gruppenNamen.indexOf(foundGroup.getName()))) {
                        foundGroup.addDriver(createData_userIdMap.get(user));
                    }

                    try {
                        databaseReference.child("Groups").child(foundGroup.getGroup_id()).setValue(foundGroup);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (wizzardManuellAktiviert) {
                    wizzardManuellAktiviert = false;
                    Utility.restartApp(MainActivity.this);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

    private String getGroupIdByName(final String name) {
        databaseReference.child("Group").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null)
                    return;


                for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
                    Group foundGroup = messageSnapshot.getValue(Group.class);
                    if (foundGroup.getName().equals(name))
                        return;
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        return null;
    }
}
