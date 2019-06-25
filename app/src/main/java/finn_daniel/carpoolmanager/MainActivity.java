package finn_daniel.carpoolmanager;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import android.view.View;

import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;

import android.view.MenuItem;

import com.google.android.material.navigation.NavigationView;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private List listviewTitle = new ArrayList();
    private List listviewisDriver = new ArrayList();
    private List listviewOwnDrivenAmount = new ArrayList();
    private List listviewAllDrivenAmount = new ArrayList();

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



        listeLaden();
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
            msgBox("settings");
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private final void listeLaden() {

        ArrayList driver1 = new ArrayList();
        driver1.add("Die Bekloppten");
        driver1.add(true);
        driver1.add(7);
        driver1.add(31);

        ArrayList driver2 = new ArrayList();
        driver2.add("Deine Mudda");
        driver2.add(false);
        driver2.add(0);
        driver2.add(568);

        ArrayList driver3 = new ArrayList();
        driver3.add("Mip");
        driver3.add(true);
        driver3.add(9);
        driver3.add(10);

        ArrayList<ArrayList> driverTest = new ArrayList();
        driverTest.add(driver1);
        driverTest.add(driver2);
        driverTest.add(driver3);

        this.listviewTitle.clear();
        this.listviewisDriver.clear();
        this.listviewOwnDrivenAmount.clear();
        this.listviewAllDrivenAmount.clear();

        for (int i = 0; i < driverTest.size(); i++) {
            listviewTitle.add(driverTest.get(i).get(0));
            listviewisDriver.add(driverTest.get(i).get(1));
            listviewOwnDrivenAmount.add(driverTest.get(i).get(2));
            listviewAllDrivenAmount.add(driverTest.get(i).get(3));
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

        ArrayList aList = new ArrayList();

        for(int i = 0; i < driverTest.size(); ++i) {
            HashMap hm = new HashMap();
            ((Map)hm).put("listview_title", listviewTitle.get(i));
            ((Map)hm).put("listview_isDriver", (Boolean)listviewisDriver.get(i) ? R.drawable.ic_lenkrad : R.drawable.ic_leer );
            ((Map)hm).put("listview_discription_ownAmount", listviewOwnDrivenAmount.get(i));
            ((Map)hm).put("listview_discription_allAmount", listviewAllDrivenAmount.get(i));
            aList.add(hm);
        }

        String[] from = new String[]{"listview_title", "listview_isDriver", "listview_discription_ownAmount", "listview_discription_allAmount"};
        int[] to = new int[]{R.id.groupList_name, R.id.listview_image, R.id.groupList_ownAmount, R.id.groupList_allAmount};
        SimpleAdapter simpleAdapter = new SimpleAdapter(this.getBaseContext(), aList, R.layout.group_list_item, from, to);
        ListView listView_groupList = findViewById(R.id.listView_groupList);
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


    public void msgBox(String nachricht) {
        Toast.makeText(this, nachricht, Toast.LENGTH_SHORT).show();
    }

    public void bildClickTest(View view) {
        msgBox("Konto Informationen Öffnen");
    }
}
