package finn_daniel.carpoolmanager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.applikeysolutions.cosmocalendar.dialog.CalendarDialog;
import com.applikeysolutions.cosmocalendar.dialog.OnDaysSelectionListener;
import com.applikeysolutions.cosmocalendar.model.Day;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupActivity extends FragmentActivity {
    static final int NUM_PAGES = 2;
    ViewPager mPager;
    PagerAdapter pagerAdapter;
    String standardView;
    Gson gson = new Gson();
    Group thisGroup;
    String EXTRA_USER = "EXTRA_USER";
    String EXTRA_GROUP = "EXTRA_GROUP";
    String EXTRA_PASSENGERMAP = "EXTRA_PASSENGERMAP";
    String EXTRA_TRIPMAP = "EXTRA_TRIPMAP";
    User loggedInUser;

    // ToDo: gruppe und Trips aus Firebase

    ViewPager_GroupOverview thisGroupOverview = new ViewPager_GroupOverview();
    ViewPager_GroupCalender thisGroupCalender = new ViewPager_GroupCalender();


    Map<String, User> groupPassengerMap = new HashMap<>();
    Map<String, Trip> groupTripsMap = new HashMap<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        loggedInUser = gson.fromJson(getIntent().getStringExtra(EXTRA_USER), User.class);
        thisGroup = gson.fromJson(getIntent().getStringExtra(EXTRA_GROUP), Group.class);
        groupPassengerMap = gson.fromJson(
                getIntent().getStringExtra(EXTRA_PASSENGERMAP), new TypeToken<HashMap<String, User>>() {}.getType()
        );
        groupTripsMap = gson.fromJson(
                getIntent().getStringExtra(EXTRA_TRIPMAP), new TypeToken<Map<String, Trip>>() {}.getType()
        );

        thisGroupOverview.setData(loggedInUser, thisGroup, groupPassengerMap, groupTripsMap);
        // ToDo: reload Group, PassengerMap und TripsMap methode

        SharedPreferences mySPR = getSharedPreferences("Settings",0);
        standardView = mySPR.getString("standardView", "Übersicht");

        mPager = findViewById(R.id.pager);
        pagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(pagerAdapter);
        mPager.setCurrentItem(standardView.equals("Übersicht") ? 0 : 1);

        TabLayout tabLayout = findViewById(R.id.tabLayout_group);
        tabLayout.setupWithViewPager(mPager);
        tabLayout.getTabAt(0).setText("Übersicht");
        tabLayout.getTabAt(1).setText("Kalender");
    }

    @Override
    public void onBackPressed() {
        if ((mPager.getCurrentItem() == 0 && standardView.equals("Übersicht")) || (mPager.getCurrentItem() == 1 && standardView.equals("Kalender"))) {
            super.onBackPressed();
        } else {
            mPager.setCurrentItem(mPager.getCurrentItem() == 0 ? 1 : 0);
        }
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0: return thisGroupOverview;
                case 1: return thisGroupCalender;
                default: return thisGroupOverview;
            }
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }
}

class ViewPager_GroupOverview extends Fragment {

    User loggedInUser;
    Group thisGroup;
    View view;
    ListView userList;
    Gson gson = new Gson();
    String EXTRA_GROUP = "EXTRA_GROUP";
    String EXTRA_PASSENGERMAP = "EXTRA_PASSENGERMAP";

    Button overview_showAllTrips;
    Button overview_showMyTrips;
    TextView overview_groupName;


    List<String> listviewTitle = new ArrayList<String>();
    List<Boolean> listviewisDriver = new ArrayList<>();
    List<java.io.Serializable> listviewOwnDrivenAmount = new ArrayList<>();
    List<User> sortedUserList = new ArrayList<>();
    Map<String , User> groupPassengerMap = new HashMap<>();
    Map<String, Trip> groupTripsMap = new HashMap<>();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.group_overview, container, false);
        overview_groupName = view.findViewById(R.id.overview_groupName);
        userList = view.findViewById(R.id.overview_userList);
        overview_showAllTrips = view.findViewById(R.id.overview_showAllTrips);
        overview_showMyTrips = view.findViewById(R.id.overview_showMyTrips);

        view.findViewById(R.id.overview_isDriverSwitch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });

        view.findViewById(R.id.overview_addTrip).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), AddTripActivity.class);
                intent.putExtra(EXTRA_GROUP, gson.toJson(thisGroup));
                intent.putExtra(EXTRA_PASSENGERMAP, gson.toJson(groupPassengerMap));
                startActivity(intent);
            }
        });

        view.findViewById(R.id.floatingActionButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CalendarDialog test = new CalendarDialog(getActivity(), new OnDaysSelectionListener() {
                    @Override
                    public void onDaysSelected(List<Day> selectedDays) {
                        String  test = null;
                    }
                });
//                test.setDayTextColor(R.color.colorPrimary)
                test.show();
            }
        });

        reLoadContent();

        return view;

        // ToDo: Lade Daten aus der Cloud und passe an bei Änderungen
    }

    public void setData(User pLoggedInUser, Group pThisGroup, Map<String,User> pGroupPassengerMap, Map<String,Trip> pGroupTripsMap) {
        loggedInUser = pLoggedInUser;
        thisGroup = pThisGroup;
        groupPassengerMap = pGroupPassengerMap;
        groupTripsMap = pGroupTripsMap;
    }

    private void reLoadContent() {
        overview_groupName.setText(thisGroup.getName());
        overview_showAllTrips.setText(String.valueOf(thisGroup.getTripIdList().size()));
        overview_showMyTrips.setText(String.valueOf(calculateDrivenAmount(loggedInUser.getUser_id())));
        listeLaden();
    }

    int calculateDrivenAmount(String userId) {
        int count = 0;
        for (Map.Entry<String, Trip> entry : groupTripsMap.entrySet()) {
            if (entry.getValue().getDriverId().equals(userId))
                count++;
        }
        return count;
    }


    private final void listeLaden() {

        this.listviewTitle.clear();
        this.listviewisDriver.clear();
        this.listviewOwnDrivenAmount.clear();

        // ToDo: Einträge sortieren (Mapp zu List und dann sortieren)
        sortedUserList = new ArrayList<>();
        for (String userId : thisGroup.getUserIdList()) {
            sortedUserList.add(groupPassengerMap.get(userId));
        }
        Collections.sort(sortedUserList, new Comparator() {
            public int compare(User obj1, User obj2) {
                return obj1.getUserName().compareTo(obj2.getUserName());
            }

            public int compare(Object var1, Object var2) {
                return this.compare((User) var1, (User) var2);
            }
        });

        for (User user : sortedUserList) {
            listviewTitle.add(user.getUserName());
            listviewisDriver.add(thisGroup.getDriverIdList().contains(user.getUser_id()));
            listviewOwnDrivenAmount.add(calculateDrivenAmount(user.getUser_id()));
        }

        ArrayList<HashMap<String, Serializable>> aList = new ArrayList<HashMap<String, Serializable>>();

        for(int i = 0; i < sortedUserList.size(); ++i) {
            HashMap<String, Serializable> hm = new HashMap<String, Serializable>();
            (hm).put("listview_title", listviewTitle.get(i));
            (hm).put("listview_isDriver", listviewisDriver.get(i) ? R.drawable.ic_lenkrad : R.drawable.ic_leer );
            (hm).put("listview_discription_ownAmount", listviewOwnDrivenAmount.get(i));
            aList.add(hm);
        }

        String[] from = new String[]{"listview_title", "listview_isDriver", "listview_discription_ownAmount"};
        int[] to = new int[]{R.id.userList_name, R.id.userList_image, R.id.userList_ownAmount};
        SimpleAdapter simpleAdapter = new SimpleAdapter(this.getContext(), aList, R.layout.passenger_list_item, from, to);
        userList.setAdapter(simpleAdapter);

        // ToDo: fahrten nach anteil farblich markieren

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

//        ListAdapter listadp = userList.getAdapter();
//        if (listadp != null) {
//            int totalHeight = 0;
//            for (int i = 0; i < listadp.getCount(); i++) {
//                View listItem = listadp.getView(i, null, userList);
//                listItem.measure(0, 0);
//                totalHeight += listItem.getMeasuredHeight();
//            }
//
////            int[] test = new int[2];
////            userList.getLocationOnScreen(test);
//
//            ViewGroup.LayoutParams params = userList.getLayoutParams();
//            params.height = totalHeight + (userList.getDividerHeight() * (listadp.getCount() - 1));
//            userList.setLayoutParams(params);
//            userList.requestLayout();
//        }
    }


}

class ViewPager_GroupCalender extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.group_calender, container, false);

        return rootView;

    }
}