package finn_daniel.carpoolmanager;

import android.content.Intent;
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

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

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

    DatabaseReference databaseReference;

    int aktuell;



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
        listeLaden();
        listeClickListener();
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

        this.listviewTitle.clear();
        this.listviewisDriver.clear();
        this.listviewPassengers.clear();
        this.listviewOwnDrivenAmount.clear();
        this.listviewAllDrivenAmount.clear();

        for (int i = 0; i < driverTest.size(); i++) {
            listviewTitle.add(driverTest.get(i).get(0));
            listviewisDriver.add(driverTest.get(i).get(1));
            listviewPassengers.add(driverTest.get(i).get(2));
            listviewOwnDrivenAmount.add(driverTest.get(i).get(3));
            listviewAllDrivenAmount.add(driverTest.get(i).get(4));
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

        for(int i = 0; i < driverTest.size(); ++i) {
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
        databaseReference.child("User").removeValue();

        for (String name : createData_gruppenNamen) {
            Group newGroup = new Group();
            newGroup.setName(name);
            createData_groupIdMap.put(name, newGroup.getGroup_id());
            databaseReference.child("Groups").child(newGroup.group_id).setValue(newGroup);
        }

        for (String name : createData_userNamen) {
            User newUser = new User();
            newUser.setUserName(name);
            createData_userIdMap.put(name, newUser.getUser_id());
            for (String gruppe : createData_userGroupMap.get(name)) {
//                String gruppenID = getGroupIdByName(name);
                newUser.addGroup(createData_groupIdMap.get(gruppe));
                // ToDo
            }

            databaseReference.child("User").child(newUser.user_id).setValue(newUser);
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
