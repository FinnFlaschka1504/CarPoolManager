package finn_daniel.carpoolmanager;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.SimpleAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar;
import com.applikeysolutions.cosmocalendar.dialog.CalendarDialog;
import com.applikeysolutions.cosmocalendar.dialog.OnDaysSelectionListener;
import com.applikeysolutions.cosmocalendar.model.Day;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
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
    int NEWTRIP = 001;
    User loggedInUser;
    Button group_addTrip;


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

        SharedPreferences mySPR = getSharedPreferences("CarPoolManager_Settings",0);
        standardView = mySPR.getString("standardView", "Übersicht");

        mPager = findViewById(R.id.group_pager);
        pagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(pagerAdapter);
        mPager.setCurrentItem(standardView.equals("Übersicht") ? 0 : 1);

        TabLayout tabLayout = findViewById(R.id.group_tabLayout);
        tabLayout.setupWithViewPager(mPager);
        tabLayout.getTabAt(0).setText("Übersicht");
        tabLayout.getTabAt(1).setText("Kalender");

        findViewById(R.id.group_addTrip).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GroupActivity.this, AddTripActivity.class);
                intent.putExtra(EXTRA_GROUP, gson.toJson(thisGroup));
                intent.putExtra(EXTRA_PASSENGERMAP, gson.toJson(groupPassengerMap));
                startActivityForResult(intent, NEWTRIP);
            }
        });

//        LocalBroadcastManager
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if (requestCode == NEWTRIP && resultCode == RESULT_OK) {
            ArrayList<Trip> newTrips = gson.fromJson(
                    data.getStringExtra(AddTripActivity.EXTRA_REPLY_TRIPS), new TypeToken<ArrayList<Trip>>() {}.getType()
            );

            for (Trip newTrip : newTrips) {
                groupTripsMap.put(newTrip.getTrip_id(), newTrip);
                thisGroup.getTripIdList().add(newTrip.getTrip_id());
            }
            thisGroupOverview.setData(loggedInUser, thisGroup, groupPassengerMap, groupTripsMap);
            thisGroupOverview.reLoadContent();
        }
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
    Dialog dialog_tripList;
    SharedPreferences mySPR_settings;


    Button overview_showAllTrips;
    Button overview_showMyTrips;
    Button overview_calculateCosts;
    TextView overview_groupName;
    Switch overview_isDriverSwitch;
    ListView dialogTripList_list;
    TextView overview_calculationType;
    TextView overview_calculationMethod;
    RoundCornerProgressBar overview_progressBar;
    TextView overview_progress;
    TextView overview_budget;
    Button overview_changeCostCalculation;


    List<String> listviewTitle = new ArrayList<String>();
    List<Boolean> listviewIsDriver = new ArrayList<>();
    List<java.io.Serializable> listviewOwnDrivenAmount = new ArrayList<>();
    List<User> sortedUserList = new ArrayList<>();
    Map<String , User> groupPassengerMap = new HashMap<>();
    Map<String, Trip> groupTripsMap = new HashMap<>();
    List<Trip> userTripList = new ArrayList<>();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.group_overview, container, false);
        overview_groupName = view.findViewById(R.id.overview_groupName);
        userList = view.findViewById(R.id.overview_userList);
        overview_showAllTrips = view.findViewById(R.id.overview_showAllTrips);
        overview_showMyTrips = view.findViewById(R.id.overview_showMyTrips);
        overview_isDriverSwitch = view.findViewById(R.id.overview_isDriverSwitch);
        overview_calculateCosts = view.findViewById(R.id.overview_calculateCosts);
        overview_calculationType = view.findViewById(R.id.overview_calculationType);
        overview_calculationMethod = view.findViewById(R.id.overview_calculationMethod);
        overview_progressBar = view.findViewById(R.id.overview_progressBar);
        overview_progress = view.findViewById(R.id.overview_progress);
        overview_budget = view.findViewById(R.id.overview_budget);
        overview_changeCostCalculation =  view.findViewById(R.id.overview_changeCostCalculation);
        overview_changeCostCalculation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showChangeCostCalculation();
            }
        });

        mySPR_settings = getActivity().getSharedPreferences("CarPoolManager_Settings", 0);

        view.findViewById(R.id.overview_isDriverSwitch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });


        view.findViewById(R.id.floatingActionButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CalendarDialog test = new CalendarDialog(getActivity(), new OnDaysSelectionListener() {
                    @Override
                    public void onDaysSelected(List<Day> selectedDays) {
                    }
                });
//                test.setDayTextColor(R.color.colorPrimary)
                test.show();
            }
        });

        overview_showAllTrips.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!overview_showAllTrips.getText().toString().equals("0"))
                    showTripList(true);
                else
                    Toast.makeText(getContext(), "Es gibt nix zum anzeigen", Toast.LENGTH_SHORT).show();
            }
        });
        overview_showMyTrips.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!overview_showMyTrips.getText().toString().equals("0"))
                    showTripList(false);
                else
                    Toast.makeText(getContext(), "Es gibt nix zum anzeigen", Toast.LENGTH_SHORT).show();
            }
        });
        overview_calculateCosts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (thisGroup.getTripIdList().size() != 0)
                    showCostCalculation();
                else
                    Toast.makeText(getContext(), "Es gibt nix zum anzeigen", Toast.LENGTH_SHORT).show();
            }
        });

        overview_isDriverSwitch.setChecked(thisGroup.getDriverIdList().contains(loggedInUser.getUser_id()));
        reLoadContent();
        setCalculationTexts();

        return view;

        // ToDo: Lade Daten aus der Cloud und passe an bei Änderungen
    }

    private void setCalculationTexts() {
        String typeText = "";
        String methodText = "";
        switch (thisGroup.getCalculationType()) {
            case COST: typeText = "nach Kosten"; break;
            case BUDGET: typeText = "nach Budget"; break;
            default: typeText = "-- nicht festgelegt -- "; break;
        }
        overview_calculationType.setText(typeText);
        switch (thisGroup.getCalculationMethod()) {
            case ACTUAL_COST: methodText = "nach tatsächlichen Kosten"; break;
            case KIKOMETER_ALLOWANCE: methodText = "nach Kilometerpauschale"; break;
            case TRIP: methodText = "nach Fahrten"; break;
            default: methodText = "-- nicht festgelegt -- "; break;
        }
        overview_calculationMethod.setText(methodText);

        if (thisGroup.getCalculationType() == Group.costCalculationType.BUDGET) {
            double budget = thisGroup.getBudget();
            if (thisGroup.isBudgetPerUser()) {
                budget *= thisGroup.getUserIdList().size();
            }
            overview_progressBar.setMax((float) budget);
            double ownProgress = 0;
            double allProgress = 0;
            if (thisGroup.getCalculationMethod() == Group.costCalculationMethod.ACTUAL_COST) {
                for (Map.Entry<String, Trip> entry : groupTripsMap.entrySet()) {
                    if (entry.getValue().getDriverId().equals(loggedInUser.getUser_id()))
                        ownProgress += entry.getValue().getCost();
                    allProgress += entry.getValue().getCost();
                }

//                for (Trip trip : userTripList) {
//                    ownProgress += trip.getCost();
//                }
                overview_progressBar.setSecondaryProgress((float) allProgress);

            }
            overview_progressBar.setProgress((float) ownProgress);
            if (ownProgress >= budget)
                overview_progressBar.setProgressColor(Color.RED);

            overview_progress.setText(convertToEuro(ownProgress));
            overview_budget.setText(convertToEuro(budget) + "€");
        }
        // ToDo: möglichkeiten je nach auswahl disablen

    }

    String convertToEuro(double count) {
        DecimalFormat df = new DecimalFormat("#.00");
        count = Double.valueOf(df.format(count).replace(",","."));
        if (count % 1 == 0) {
            return String.valueOf(count).split("\\.")[0];
        }
        else {
            return String.valueOf(count);
        }

    }

    private void showChangeCostCalculation() {
        final Dialog dialog_changeCostCalculation = new Dialog(getContext());
        dialog_changeCostCalculation.setContentView(R.layout.dialog_changecost_calculation);

        RadioGroup dialogChangeCostCalculation_typeGroup;
        RadioGroup dialogChangeCostCalculation_methodGroup;
        EditText dialogChangeCostCalculation_budget;
        CheckBox dialogChangeCostCalculation_perPerson;

        dialogChangeCostCalculation_typeGroup = dialog_changeCostCalculation.findViewById(R.id.dialogChangeCostCalculation_typeGroup);
        dialogChangeCostCalculation_methodGroup = dialog_changeCostCalculation.findViewById(R.id.dialogChangeCostCalculation_methodGroup);
        dialogChangeCostCalculation_budget = dialog_changeCostCalculation.findViewById(R.id.dialogChangeCostCalculation_budget);
        dialogChangeCostCalculation_perPerson = dialog_changeCostCalculation.findViewById(R.id.dialogChangeCostCalculation_perPerson);

        switch (thisGroup.getCalculationType()) {
            default:
            case COST: dialogChangeCostCalculation_typeGroup.check(R.id.dialogChangeCostCalculation_costRadio); break;
            case BUDGET: dialogChangeCostCalculation_typeGroup.check(R.id.dialogChangeCostCalculation_budgetRadio); break;
        }
        switch (thisGroup.getCalculationMethod()) {
            default:
            case ACTUAL_COST: dialogChangeCostCalculation_methodGroup.check(R.id.dialogChangeCostCalculation_realCostRadio); break;
            case KIKOMETER_ALLOWANCE: dialogChangeCostCalculation_methodGroup.check(R.id.dialogChangeCostCalculation_lumbSumRadio); break;
            case TRIP: dialogChangeCostCalculation_methodGroup.check(R.id.dialogChangeCostCalculation_tripRadio); break;
        }
        dialogChangeCostCalculation_budget.setText(String.valueOf(thisGroup.getBudget()));
        dialogChangeCostCalculation_perPerson.setChecked(thisGroup.isBudgetPerUser());

//        dialogChangeCostCalculation_typeGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(RadioGroup radioGroup, int i) {
//
//            }
//        });



        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog_changeCostCalculation.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        dialog_changeCostCalculation.show();
        dialog_changeCostCalculation.getWindow().setAttributes(lp);

        dialog_changeCostCalculation.findViewById(R.id.dialogChangeCostCalculation_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog_changeCostCalculation.dismiss();
            }
        });
    }


    private void showCostCalculation() {
        Dialog dialog_costCalculation = new Dialog(getContext());
        dialog_costCalculation.setContentView(R.layout.dialog_calculate_costs);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog_costCalculation.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        dialog_costCalculation.show();
        dialog_costCalculation.getWindow().setAttributes(lp);
    }

    private void showTripList(boolean showAll) {
        dialog_tripList = new Dialog(getContext());
        dialog_tripList.setContentView(R.layout.dialog_trip_list);

        dialogTripList_list = dialog_tripList.findViewById(R.id.dialogTripList_list);

        dialog_tripList.findViewById(R.id.dialogTripList_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog_tripList.dismiss();
            }
        });

        List<String> tripList_date_list =  new ArrayList<>();
        List<String> tripList_driver_list =  new ArrayList<>();
        List<String> tripList_fromTo_list =  new ArrayList<>();
        List<String> tripList_distance_list =  new ArrayList<>();
        List<String> tripList_twoWay_list =  new ArrayList<>();
        List<String> tripList_cost_list =  new ArrayList<>();

        List<Trip> tripList = new ArrayList<>();
        for (String tripId : thisGroup.getTripIdList()) {
            Trip trip = groupTripsMap.get(tripId);
            if (!showAll && !trip.getDriverId().equals(loggedInUser.getUser_id()))
                continue;
            tripList.add(trip);
        }

        Collections.sort(tripList, new Comparator() {
            public int compare(Trip obj1, Trip obj2) {
                return obj1.getDate().compareTo(obj2.getDate());
            }

            public int compare(Object var1, Object var2) {
                return this.compare((Trip) var1, (Trip) var2);
            }
        });

        String pattern = "dd.MM.yyyy E";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        DecimalFormat df = new DecimalFormat("#.00");

        for (Trip trip: tripList) {
            tripList_date_list.add(simpleDateFormat.format(trip.getDate()).replace(" ", " (") + ")");
            tripList_driver_list.add(showAll ?
                    "- " + groupPassengerMap.get(trip.getDriverId()).getUserName() : "");
            tripList_fromTo_list.add(trip.getLocationName().get(0) +
                    (trip.isTwoWay() ? " ⇔ " : " ⇒ ") + trip.getLocationName().get(1));
            tripList_distance_list.add(trip.getDistance());
            tripList_twoWay_list.add(trip.isTwoWay() ? "x2" : "");
            tripList_cost_list.add(df.format(trip.getCost()) + "€");
        }

        ArrayList<HashMap<String, Serializable>> aList = new ArrayList<HashMap<String, Serializable>>();

        for(int i = 0; i < tripList.size(); ++i) {
            HashMap<String, Serializable> hm = new HashMap<String, Serializable>();
            (hm).put("tripList_date_list", tripList_date_list.get(i));
            (hm).put("tripList_driver_list", tripList_driver_list.get(i));
            (hm).put("tripList_fromTo_list", tripList_fromTo_list.get(i));
            (hm).put("tripList_distance_list", tripList_distance_list.get(i));
            (hm).put("tripList_twoWay_list", tripList_twoWay_list.get(i));
            (hm).put("tripList_cost_list", tripList_cost_list.get(i));
            aList.add(hm);
        }

        String[] from = new String[]{"tripList_date_list", "tripList_driver_list", "tripList_fromTo_list", "tripList_distance_list", "tripList_twoWay_list", "tripList_cost_list"};
        int[] to = new int[]{R.id.tripList_date, R.id.tripList_driver, R.id.tripList_fromTo, R.id.tripList_distance, R.id.tripList_twoWay, R.id.tripList_cost};
        SimpleAdapter simpleAdapter = new SimpleAdapter(this.getContext(), aList, R.layout.list_item_trip, from, to);
        dialogTripList_list.setAdapter(simpleAdapter);


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






        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog_tripList.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        dialog_tripList.show();
        dialog_tripList.getWindow().setAttributes(lp);
    }

    public void setData(User pLoggedInUser, Group pThisGroup, Map<String,User> pGroupPassengerMap, Map<String,Trip> pGroupTripsMap) {
        loggedInUser = pLoggedInUser;
        thisGroup = pThisGroup;
        groupPassengerMap = pGroupPassengerMap;
        groupTripsMap = pGroupTripsMap;
    }

    public void reLoadContent() {
        overview_groupName.setText(thisGroup.getName());
        overview_showAllTrips.setText(calculateDrivenAmount(null));
        overview_showMyTrips.setText(calculateDrivenAmount(loggedInUser.getUser_id()));
        listeLaden();
    }

    String calculateDrivenAmount(String userId) {
        double count = 0;
        if (loggedInUser.getUser_id().equals(userId))
            userTripList.clear();
        for (Map.Entry<String, Trip> entry : groupTripsMap.entrySet()) {
            if (entry.getValue().getDriverId().equals(userId) || userId == null) {
                if (loggedInUser.getUser_id().equals(userId))
                    userTripList.add(entry.getValue());
                count++;
                if (entry.getValue().isTwoWay())
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


    private final void listeLaden() {

        this.listviewTitle.clear();
        this.listviewIsDriver.clear();
        this.listviewOwnDrivenAmount.clear();

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
            listviewIsDriver.add(thisGroup.getDriverIdList().contains(user.getUser_id()));
            listviewOwnDrivenAmount.add(calculateDrivenAmount(user.getUser_id()));
        }

        ArrayList<HashMap<String, Serializable>> aList = new ArrayList<HashMap<String, Serializable>>();

        for(int i = 0; i < sortedUserList.size(); ++i) {
            HashMap<String, Serializable> hm = new HashMap<String, Serializable>();
            (hm).put("listview_title", listviewTitle.get(i));
            (hm).put("listview_isDriver", listviewIsDriver.get(i) ? R.drawable.ic_lenkrad : R.drawable.ic_leer );
            (hm).put("listview_discription_ownAmount", listviewOwnDrivenAmount.get(i));
            aList.add(hm);
        }

        String[] from = new String[]{"listview_title", "listview_isDriver", "listview_discription_ownAmount"};
        int[] to = new int[]{R.id.userList_name, R.id.userList_image, R.id.userList_ownAmount};
        SimpleAdapter simpleAdapter = new SimpleAdapter(this.getContext(), aList, R.layout.list_item_passenger, from, to);
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

//    public void onActivityResult(int requestCode, int resultCode, Intent data){
//        super.onActivityResult(requestCode,resultCode,data);
//        if (requestCode == NEWTRIP && resultCode == RESULT_OK) {
//            //if (resultCode == RESULT_OK) {
//
//            ArrayList<Trip> newTrips = gson.fromJson(
//                    data.getStringExtra(AddTripActivity.EXTRA_REPLY_TRIPS), new TypeToken<ArrayList<Trip>>() {}.getType()
//            );
//
//            for (Trip newTrip : newTrips) {
//                groupTripsMap.put(newTrip.getTrip_id(), newTrip);
//                thisGroup.getTripIdList().add(newTrip.getTrip_id());
//            }
//            reLoadContent();
//
//            String test = null;
//
//            //}
//        }
//    }


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