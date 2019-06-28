package finn_daniel.carpoolmanager;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import android.view.View;

import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;

import android.view.MenuItem;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private List listviewTitle = new ArrayList();
    private List listviewisDriver = new ArrayList();
    private List listviewPassengers = new ArrayList();
    private List listviewOwnDrivenAmount = new ArrayList();
    private List listviewAllDrivenAmount = new ArrayList();
    ListView listView_groupList;

    List<String> createData_gruppenNamen;
    List<String> createData_userNamen;
    ArrayList<ArrayList<String>> createData_mitfahrer;
    ArrayList<ArrayList<String>> createData_fahrer;
    ArrayList<ArrayList<Integer>> createData_fahrten;

    Map<String, User> createData_userMap = new HashMap<>();
    Map<String, List<String>> createData_userGroupMap = new HashMap<>();
    Map<String, String > createData_userIdMap = new HashMap<>();
    Map<String, String > createData_groupIdMap = new HashMap<>();

    Gson gson = new Gson();
    DatabaseReference databaseReference;

    int aktuell;
    Map<String, Boolean> hasGroupChangeListener = new HashMap<>();
    String loggedInUser_Name = "DeineMudda";
    String loggedInUser_Id;
    List<String> loggedInUser_groupsIdList = new ArrayList<>();
    Map<String , Group> loggedInUser_groupsMap = new HashMap<>();
    Map<String , User> loggedInUser_groupPassengerMap = new HashMap<>();
    int loggedInUser_passengerCount = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Eine Neue Gruppe hinzufügen, oder erstellen", Snackbar.LENGTH_SHORT)
                        .setAction("Aktion", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                msgBox("test");
                            }
                        }).show();
            }
        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        databaseReference = FirebaseDatabase.getInstance().getReference();

        listView_groupList = findViewById(R.id.listView_groupList);

        if (!isOnline()) {
            showNoInternetSnackBar();
            TextView main_noInternet = findViewById(R.id.main_noInternet);
            main_noInternet.setVisibility(View.VISIBLE);
            main_noInternet.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!isOnline()) {
                        showActivateInternetDialog();
                    } else {
                        findViewById(R.id.main_noInternet).setVisibility(View.GONE);
                        getUserIdfromUserName();
                    }

                }
            });
            SharedPreferences mySPR = getSharedPreferences("OfflineDaten",0);
            int count = 0;
            while (true) {
                String loggedInUser_groupsList_string = mySPR.getString("loggedInUser_groupsList_" + count, "--Leer--");
                if (!loggedInUser_groupsList_string.equals("--Leer--")) {
                    Group newGroup = gson.fromJson(loggedInUser_groupsList_string, Group.class);
                    loggedInUser_groupsMap.put(newGroup.getGroup_id(), newGroup);
                }
                else
                    break;
                count++;
            }
            count = 0;
            User foundUser;
            while (true) {
                String loggedInUser_groupPassengerMap_string = mySPR.getString("loggedInUser_groupPassengerMap_" + count, "--Leer--");
                if (!loggedInUser_groupPassengerMap_string.equals("--Leer--")) {
                    foundUser = gson.fromJson(loggedInUser_groupPassengerMap_string, User.class);
                }
                else
                    break;
                count++;
                loggedInUser_groupPassengerMap.put(foundUser.getUser_id(), foundUser);
            }
            String loggedInUser_Id_string = mySPR.getString("loggedInUser_Id", "--Leer--");
            if (!loggedInUser_Id_string.equals("--Leer--")) {
                loggedInUser_Id = loggedInUser_Id_string;
            }
            listeLaden();
            listeClickListener();
            return;
        }



        getUserIdfromUserName();

    }

    private void showNoInternetSnackBar() {
        Snackbar.make(findViewById(R.id.include), "Es gibt keine verbindung zum Internet!", Snackbar.LENGTH_LONG)
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


    private void getUserIdfromUserName() {
        databaseReference.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null)
                    return;

                for (DataSnapshot messageSnapshot: dataSnapshot.getChildren()) {
                    User foundUser = messageSnapshot.getValue(User.class);
                    if (foundUser.getUserName().equals(loggedInUser_Name)) {
                        loggedInUser_Id = foundUser.getUser_id();
                        System.out.println(loggedInUser_Id);
                    }
                }

                getGroupsfromUser();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void getGroupsfromUser() {
        // ToDo: App stürtzt beim aktuallisieren ab
        databaseReference.child("Users").child(loggedInUser_Id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null)
                    return;
                User foundUser = dataSnapshot.getValue(User.class);
                loggedInUser_groupsIdList = foundUser.getGroupIdList();
                // addValueEventListener
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
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void addGroupChangeListener(final String groupId) {
        databaseReference.child("Groups").child(groupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null)
                    return;
                // ToDo: was passiert wenn gelöscht?
                Group foundGroup = dataSnapshot.getValue(Group.class);
                if (!hasGroupChangeListener.get(foundGroup.getGroup_id())) {
                    hasGroupChangeListener.put(foundGroup.getGroup_id(), true);
                    return;
                }
                int i = 0;

                if (!loggedInUser_groupsMap.containsKey(foundGroup.getGroup_id()))
                    return;
                // ToDo: Was passiert wenn neue Gruppe

                foundGroup.getUserIdList().equals(loggedInUser_groupsMap.get(foundGroup.getGroup_id()).getUserIdList());


                List<List<String>> changeList = foundGroup.getChangedUserLists(loggedInUser_groupsMap.get(foundGroup.getGroup_id()));

                for (String user : changeList.get(1)) {
                    loggedInUser_groupPassengerMap.remove(user);
                }
                for (String user : changeList.get(0)) {
                    databaseReference.child("Users").child(user).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.getValue() == null)
                                return;
                            User foundUser = dataSnapshot.getValue(User.class);
                            loggedInUser_groupPassengerMap.put(foundUser.getUser_id(), foundUser);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
                }

                loggedInUser_groupsMap.put(foundGroup.getGroup_id(), foundGroup);

                listeLaden();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
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
                            listeLaden();
                            listeClickListener();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });

            }
        }
    }

    @Override
    protected void onStop() {
        SharedPreferences mySPR = getSharedPreferences("OfflineDaten", 0);
        SharedPreferences.Editor editor = mySPR.edit();
        editor.clear();

        //ToDo: Alle einträge vorher löschen - delete shared preferences

        int count = 0;
        for (Map.Entry<String, Group> entry : loggedInUser_groupsMap.entrySet()) {
            editor.putString("loggedInUser_groupsList_" + count, gson.toJson(entry.getValue()));
            count++;
        }
        count = 0;
        for (Map.Entry<String, User> entry : loggedInUser_groupPassengerMap.entrySet()) {
            editor.putString("loggedInUser_groupPassengerMap_" + count, gson.toJson(entry.getValue()));
            count++;
        }
        editor.putString("loggedInUser_Id", loggedInUser_Id);

        editor.commit();
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
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

        if (id == R.id.groups) {
            msgBox("groups");
        }
        else if (id == R.id.settings) {
            Intent intent = new Intent(MainActivity.this, Settings.class);
            startActivity(intent);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private final void listeLaden() {

        TextView main_groupInfo = findViewById(R.id.main_groupInfo);
        main_groupInfo.setVisibility(View.GONE);

        ArrayList driver1 = new ArrayList();
        driver1.add("Die Bekloppten");
        driver1.add(true);
        driver1.add("Arsch 1, Derda31, DeineMudda");
        driver1.add(7);
        driver1.add(31);

        ArrayList driver2 = new ArrayList();
        driver2.add("Deine Mudda");
        driver2.add(false);
        driver2.add("Pudding, Die Olle, Niemand");
        driver2.add(0);
        driver2.add(568);

        ArrayList driver3 = new ArrayList();
        driver3.add("Mip");
        driver3.add(true);
        driver3.add("Noch Einer, und Noch Einer, und Noch Einer");
        driver3.add(9);
        driver3.add(10);

        ArrayList<ArrayList> driverTest = new ArrayList();
        driverTest.add(driver1);
        driverTest.add(driver2);
        driverTest.add(driver3);

        // ---------------------------------------------------------------------

        this.listviewTitle.clear();
        this.listviewisDriver.clear();
        this.listviewPassengers.clear();
        this.listviewOwnDrivenAmount.clear();
        this.listviewAllDrivenAmount.clear();

        int count = 0;
        for (Map.Entry<String, Group> entry : loggedInUser_groupsMap.entrySet()) {
            listviewTitle.add(entry.getValue().getName());
            listviewisDriver.add(entry.getValue().getDriverIdList().contains(loggedInUser_Id));

            String passengers = "";
            for (int n = 0; n < entry.getValue().getUserIdList().size(); n++) {
                passengers = passengers.concat(loggedInUser_groupPassengerMap.get(entry.getValue().getUserIdList().get(n)).getUserName());
                if (n < entry.getValue().getUserIdList().size() - 1)
                    passengers = passengers.concat(", ");
            }

            listviewPassengers.add(passengers);
            listviewOwnDrivenAmount.add(driverTest.get(count).get(3));
            listviewAllDrivenAmount.add(driverTest.get(count).get(4));
            count++;
        }

//        TextView var11;
//        if (this.listviewTitle.isEmpty()) {
//            var11 = (TextView)this._$_findCachedViewById(id.textView_keine_reminder);
//            Intrinsics.checkExpressionValueIsNotNull(var11, "textView_keine_reminder");
//            var11.setVisibility(0);
//        } else {
//            var11 = (TextView)this._$_findCachedViewById(id.textView_keine_reminder);
//            Intrinsics.checkExpressionValueIsNotNull(var11, "textView_keine_reminder");
//            var11.setVisibility(4);
//        }
        // ToDo: Mitfahrer farblich kennzeichnen

        ArrayList aList = new ArrayList<>();

        for(int i = 0; i < loggedInUser_groupsMap.size(); ++i) {
            HashMap hm = new HashMap();
            (hm).put("listview_title", listviewTitle.get(i));
            (hm).put("listview_isDriver", (Boolean)listviewisDriver.get(i) ? R.drawable.ic_lenkrad : R.drawable.ic_leer );
            (hm).put("listview_discription_passengers", listviewPassengers.get(i));
            (hm).put("listview_discription_ownAmount", listviewOwnDrivenAmount.get(i));
            (hm).put("listview_discription_allAmount", listviewAllDrivenAmount.get(i));
            aList.add(hm);
        }

        String[] from = new String[]{"listview_title", "listview_isDriver", "listview_discription_passengers", "listview_discription_ownAmount", "listview_discription_allAmount"};
        int[] to = new int[]{R.id.groupList_name, R.id.listview_image, R.id.passengers, R.id.groupList_ownAmount, R.id.groupList_allAmount};
        SimpleAdapter simpleAdapter = new SimpleAdapter(this.getBaseContext(), aList, R.layout.group_list_item, from, to);
        listView_groupList.setAdapter(simpleAdapter);

//      <undefinedtype> mOnPreDrawListener = new OnPreDrawListener() {
//            public boolean onPreDraw() {
//                ListView var10000 = (ListView)ReminderListe.this._$_findCachedViewById(id.listView_reminder);
//                Intrinsics.checkExpressionValueIsNotNull(var10000, "listView_reminder");
//                ListAdapter listAdapter = var10000.getAdapter();
//                int i = 0;
//                Intrinsics.checkExpressionValueIsNotNull(listAdapter, "listAdapter");
//
//                for(int var3 = listAdapter.getCount(); i < var3; ++i) {
//                    ReminderListe var6 = ReminderListe.this;
//                    ListView var10002 = (ListView)ReminderListe.this._$_findCachedViewById(id.listView_reminder);
//                    Intrinsics.checkExpressionValueIsNotNull(var10002, "listView_reminder");
//                    View listItem = var6.getViewByPosition(i, var10002);
//                    View var7 = listItem.findViewById(-1000011);
//                    if (var7 == null) {
//                        throw new TypeCastException("null cannot be cast to non-null type android.widget.TextView");
//                    }
//
//                    TextView status_description_view = (TextView)var7;
//                    if (Intrinsics.areEqual(status_description_view.getText(), ReminderListe.this.getDEAKTIVIERT$app_debug())) {
//                        status_description_view.setTextColor(-65536);
//                    }
//                }
//
//                return true;
//            }
//        };
//        listView_groupList = (ListView)this._$_findCachedViewById(id.listView_reminder);
//        listView_groupList.getViewTreeObserver().addOnPreDrawListener((OnPreDrawListener)mOnPreDrawListener);
//        this.listeClickListener();
    }


    void listeClickListener(){
        listView_groupList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int index, long l) {
                aktuell = index;
                Intent intent = new Intent(MainActivity.this, GroupOverview.class);

//        intent.putExtra(EXTRA_MESSAGE, message);
                startActivity(intent);            }
        });
    }



    public void msgBox(String nachricht) {
        Toast.makeText(this, nachricht, Toast.LENGTH_SHORT).show();
    }

    public void bildClickTest(View view) {
        msgBox("Konto Informationen Öffnen");
    }

    public void createListData(View view) {
        createData_gruppenNamen = new ArrayList<String>(Arrays.asList("Die Bekloppten", "Deine Mudda", "Mip"));
        createData_userNamen = new ArrayList<String>(Arrays.asList("Arsch 1", "Derda31", "DeineMudda", "Pudding", "Die Olle", "Niemand", "Noch Einer", "und Noch Einer", "und Noch Noch Einer"));

        createData_userGroupMap.put("Arsch 1", new ArrayList<String>(Arrays.asList("Die Bekloppten")));
        createData_userGroupMap.put("Derda31", new ArrayList<String>(Arrays.asList("Die Bekloppten", "Deine Mudda")));
        createData_userGroupMap.put("DeineMudda", new ArrayList<String>(Arrays.asList("Die Bekloppten", "Deine Mudda", "Mip")));
        createData_userGroupMap.put("Pudding", new ArrayList<String>(Arrays.asList("Deine Mudda")));
        createData_userGroupMap.put("Die Olle", new ArrayList<String>(Arrays.asList("Deine Mudda")));
        createData_userGroupMap.put("Niemand", new ArrayList<String>(Arrays.asList("Deine Mudda")));
        createData_userGroupMap.put("Noch Einer", new ArrayList<String>(Arrays.asList("Mip")));
        createData_userGroupMap.put("und Noch Einer", new ArrayList<String>(Arrays.asList("Mip")));
        createData_userGroupMap.put("und Noch Noch Einer", new ArrayList<String>(Arrays.asList("Mip")));

        createData_mitfahrer = new ArrayList<ArrayList<String>>();
        createData_mitfahrer.add(new ArrayList<>(Arrays.asList("Arsch 1", "Derda31", "DeineMudda")));
        createData_mitfahrer.add(new ArrayList<>(Arrays.asList("Pudding", "Die Olle", "Derda31", "Niemand", "DeineMudda")));
        createData_mitfahrer.add(new ArrayList<>(Arrays.asList("Noch Einer", "und Noch Einer", "und Noch Noch Einer", "DeineMudda")));

        createData_fahrer = new ArrayList<ArrayList<String>>();
        createData_fahrer.add(new ArrayList<>(Arrays.asList("Arsch 1", "Derda31", "DeineMudda")));
        createData_fahrer.add(new ArrayList<>(Arrays.asList("Pudding", "Die Olle", "Niemand")));
        createData_fahrer.add(new ArrayList<>(Arrays.asList("und Noch Einer", "DeineMudda")));

        createData_fahrten = new ArrayList<>();
        createData_fahrten.add(new ArrayList<>(Arrays.asList(7,31)));
        createData_fahrten.add(new ArrayList<>(Arrays.asList(0,568)));
        createData_fahrten.add(new ArrayList<Integer>(Arrays.asList(9,10)));

        databaseReference.child("Groups").removeValue();
        databaseReference.child("Users").removeValue();

        for (String name : createData_gruppenNamen) {
            Group newGroup = new Group();
            newGroup.setName(name);
            createData_groupIdMap.put(name, newGroup.getGroup_id());
            databaseReference.child("Groups").child(newGroup.group_id).setValue(newGroup);
        }

        for (String name : createData_userNamen) {
            User newUser = new User();
//            newUser.generateId();
            newUser.setUserName(name);
            createData_userIdMap.put(name, newUser.getUser_id());
            for (String gruppe : createData_userGroupMap.get(name)) {
//                String gruppenID = getGroupIdByName(name);
                newUser.addGroup(createData_groupIdMap.get(gruppe));
                // ToDo
            }

            databaseReference.child("Users").child(newUser.user_id).setValue(newUser);
        }

        databaseReference.child("Groups").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null)
                    return;


                for (DataSnapshot messageSnapshot: dataSnapshot.getChildren()) {
                    Group foundGroup = messageSnapshot.getValue(Group.class);
                    for (String user : createData_mitfahrer.get(createData_gruppenNamen.indexOf(foundGroup.getName()))) {
                        foundGroup.addUser(createData_userIdMap.get(user));
                    }
                    for (String user : createData_fahrer.get(createData_gruppenNamen.indexOf(foundGroup.getName()))) {
                        foundGroup.addDriver(createData_userIdMap.get(user));
                    }

                    try {
                        databaseReference.child("Groups").child(foundGroup.group_id).setValue(foundGroup);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

    public boolean isOnline() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int     exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        }
        catch (IOException e)          { e.printStackTrace(); }
        catch (InterruptedException e) { e.printStackTrace(); }

        return false;
    }

    private String getGroupIdByName(final String name) {
        databaseReference.child("Group").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null)
                    return;


                for (DataSnapshot messageSnapshot: dataSnapshot.getChildren()) {
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
