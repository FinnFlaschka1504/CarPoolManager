package finn_daniel.carpoolmanager;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SimpleAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar;
import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
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
    FloatingActionButton group_addTrip;


    ViewPager_GroupOverview thisGroupOverview = new ViewPager_GroupOverview();
    ViewPager_GroupCalender thisGroupCalender = new ViewPager_GroupCalender();


    Map<String, User> groupPassengerMap = new HashMap<>();
    Map<String, Trip> groupTripsMap = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        group_addTrip = findViewById(R.id.group_addTrip);

        loggedInUser = gson.fromJson(getIntent().getStringExtra(EXTRA_USER), User.class);
        thisGroup = gson.fromJson(getIntent().getStringExtra(EXTRA_GROUP), Group.class);
        groupPassengerMap = gson.fromJson(
                getIntent().getStringExtra(EXTRA_PASSENGERMAP), new TypeToken<HashMap<String, User>>() {
                }.getType()
        );
        groupTripsMap = gson.fromJson(
                getIntent().getStringExtra(EXTRA_TRIPMAP), new TypeToken<Map<String, Trip>>() {
                }.getType()
        );

        thisGroupOverview.setData(thisGroupCalender, loggedInUser, thisGroup, groupPassengerMap, groupTripsMap, group_addTrip);
        thisGroupCalender.setData(loggedInUser, thisGroup, groupPassengerMap, groupTripsMap);
        SharedPreferences mySPR = getSharedPreferences("CarPoolManager_Settings", 0);
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
                intent.putExtra(EXTRA_TRIPMAP, gson.toJson(groupTripsMap));
                startActivityForResult(intent, NEWTRIP);
            }
        });

//        LocalBroadcastManager
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == NEWTRIP && resultCode == RESULT_OK) {
            ArrayList<Trip> newTrips = gson.fromJson(
                    data.getStringExtra(AddTripActivity.EXTRA_REPLY_TRIPS), new TypeToken<ArrayList<Trip>>() {
                    }.getType()
            );

            for (Trip newTrip : newTrips) {
                groupTripsMap.put(newTrip.getTrip_id(), newTrip);
                thisGroup.getTripIdList().add(newTrip.getTrip_id());
            }
            thisGroupOverview.setData(thisGroupCalender, loggedInUser, thisGroup, groupPassengerMap, groupTripsMap, group_addTrip);
            thisGroupOverview.reLoadContent();
            thisGroupCalender.setData(loggedInUser, thisGroup, groupPassengerMap, groupTripsMap);
            thisGroupCalender.reLoadContent();
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
                case 0:
                    return thisGroupOverview;
                case 1:
                    return thisGroupCalender;
                default:
                    return thisGroupOverview;
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
    double colorMargin = 0.1;
    DatabaseReference databaseReference;
    boolean isDriver;
    Snackbar noDriverSnackbar;

    Button overview_showAllTrips;
    Button overview_showMyTrips;
    Button overview_calculateCosts;
    Button overview_editPassengers;
    TextView overview_groupName;
    Switch overview_isDriverSwitch;
    ListView dialogTripList_list;
    TextView overview_calculationType;
    TextView overview_calculationMethod;
    RoundCornerProgressBar overview_progressBar;
    TextView overview_progress;
    TextView overview_budget;
    Button overview_changeCostCalculation;
    Button overview_save_isDriver;
    FloatingActionButton group_addTrip;
    TextView overview_noDriverText;
    ViewPager_GroupCalender thisGroupCalender;

    List<String> listviewTitle = new ArrayList<String>();
    List<Boolean> listviewIsDriver = new ArrayList<>();
    List<java.io.Serializable> listviewOwnDrivenAmount = new ArrayList<>();
    List<User> sortedUserList = new ArrayList<>();
    Map<String, User> groupPassengerMap = new HashMap<>();
    Map<String, Trip> groupTripsMap = new HashMap<>();
    List<Trip> userTripList = new ArrayList<>();
    Map<String, Map<String, Trip>> userTripMap = new HashMap<>();
    List<Trip> tripList;



    @SuppressLint("RestrictedApi")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.group_overview, container, false);
        databaseReference = FirebaseDatabase.getInstance().getReference();
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
        overview_changeCostCalculation = view.findViewById(R.id.overview_changeCostCalculation);
        overview_save_isDriver = view.findViewById(R.id.overview_save_isDriver);
        overview_noDriverText = view.findViewById(R.id.overview_noDriverText);
        overview_editPassengers = view.findViewById(R.id.overview_editPassengers);

        overview_editPassengers.setOnClickListener(view1 -> {
            Dialog dialog = CustomDialog.Builder(getContext())
                    .setTitle("Mitfahrer bearbeiten")
                    .setText("Was möchtest du tun?")
                    .setButtonType(CustomDialog.buttonType_Enum.CUSTOM)
                    .addButton("Gruppe Verlassen", () -> {
                        // ToDo: Gruppe verlassen implementieren
                        Toast.makeText(getContext(), "Tschüss", Toast.LENGTH_SHORT).show();
                    })
                    .addButton("Mitfahrer hinzufügen", () ->
                            // ToDo: nutzer hinzufügen implementieren
                            CustomDialog.Builder(getContext())
                                    .setTitle("Mitfahrer hinzufügen")
                                    .setButtonType(CustomDialog.buttonType_Enum.BACK)
                                    .setView(R.layout.dialog_add_passenger)
                                    .show(),
                            false)
                    .addButton("Test1", () -> {}, false)
                    .addButton("Test2", () -> {}, false)
                    .addButton("Test3", () -> {}, false)
                    .addButton("Test4", () -> {}, false)
                    .addButton("Test5", () -> {}, false)
                    .show();
        });

        // ToDo: dialoge durch neuen Custom Dialog ersetzen
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
                overview_save_isDriver.setVisibility(overview_isDriverSwitch.isChecked() == isDriver ?
                        View.INVISIBLE : View.VISIBLE);
            }
        });

        overview_save_isDriver.setOnClickListener(view -> {
            if (overview_isDriverSwitch.isChecked())
                thisGroup.getDriverIdList().add(loggedInUser.getUser_id());
            else
                thisGroup.getDriverIdList().remove(loggedInUser.getUser_id());
            isDriver = overview_isDriverSwitch.isChecked();
            overview_save_isDriver.setVisibility(View.INVISIBLE);
            reLoadContent();
            databaseReference.child("Groups").child(thisGroup.getGroup_id()).child("driverIdList").setValue(thisGroup.getDriverIdList());
            if (thisGroup.getDriverIdList().size() <= 0) {
                group_addTrip.setVisibility(View.INVISIBLE);
                overview_noDriverText.setVisibility(View.VISIBLE);
                overview_noDriverText.setSelected(true);
            } else {
                group_addTrip.setVisibility(View.VISIBLE);
                overview_noDriverText.setVisibility(View.INVISIBLE);
            }
            Toast.makeText(getContext(), "Gespeichert", Toast.LENGTH_SHORT).show();
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

        group_addTrip.setVisibility(thisGroup.getDriverIdList().size() > 0 ?
                View.VISIBLE : View.INVISIBLE);
        overview_noDriverText.setVisibility(thisGroup.getDriverIdList().size() <= 0 ?
                View.VISIBLE : View.INVISIBLE);
        overview_noDriverText.setSelected(thisGroup.getDriverIdList().size() <= 0);
        overview_isDriverSwitch.setChecked(thisGroup.getDriverIdList().contains(loggedInUser.getUser_id()));
        isDriver = thisGroup.getDriverIdList().contains(loggedInUser.getUser_id());
        overview_save_isDriver.setVisibility(View.INVISIBLE);
        reLoadContent();

        for (Map.Entry<String, Trip> entry : groupTripsMap.entrySet()) {
            Trip trip = entry.getValue();

            Map<String, Trip> map = userTripMap.get(trip.getDriverId());
            if (map == null) {
                Map<String, Trip> newMap = new HashMap<>();
                newMap.put(trip.getTrip_id(), trip);
                userTripMap.put(trip.getDriverId(), newMap);
            } else {
                map.put(trip.getTrip_id(), trip);
                userTripMap.replace(trip.getDriverId(), map);
            }
        }

        return view;

        // ToDo: Lade Daten aus der Cloud und passe an bei Änderungen
    }

    public void setData(ViewPager_GroupCalender thisGroupCalender, User pLoggedInUser, Group pThisGroup, Map<String,User> pGroupPassengerMap, Map<String,Trip> pGroupTripsMap, FloatingActionButton pGroup_addTrip) {
        loggedInUser = pLoggedInUser;
        thisGroup = pThisGroup;
        groupPassengerMap = pGroupPassengerMap;
        groupTripsMap = pGroupTripsMap;
        group_addTrip = pGroup_addTrip;
        this.thisGroupCalender = thisGroupCalender;
    }

    public void reLoadContent() {
        overview_groupName.setText(thisGroup.getName());
        overview_showAllTrips.setText(String.valueOf(calculateDrivenAmount(null)));
        overview_showMyTrips.setText(String.valueOf(calculateDrivenAmount(loggedInUser.getUser_id())));
        listeLaden();
        setCalculationTexts();
    }

    private void setCalculationTexts() {
        String typeText;
        String methodText;
        switch (thisGroup.getCalculationType()) {
            case COST:
                typeText = "nach Kosten";
                break;
            case BUDGET:
                typeText = "nach Budget";
                break;
            default:
                typeText = "-- nicht festgelegt -- ";
                break;
        }
        overview_calculationType.setText(typeText);
        switch (thisGroup.getCalculationMethod()) {
            case ACTUAL_COST:
                methodText = "nach tatsächlichen Kosten";
                break;
            case KIKOMETER_ALLOWANCE:
                methodText = "nach Kilometerpauschale";
                break;
            case TRIP:
                methodText = "nach Fahrten";
                break;
            default:
                methodText = "-- nicht festgelegt -- ";
                break;
        }
        overview_calculationMethod.setText(methodText);

        setProgressBar(overview_progressBar, loggedInUser);

    }

    private void setProgressBar(RoundCornerProgressBar progressBar, User user) {
        if (thisGroup.getCalculationType() == Group.costCalculationType.BUDGET) {
            double budget = thisGroup.getBudget();
            if (thisGroup.isBudgetPerUser()) {
                budget *= thisGroup.getUserIdList().size();
            }
            progressBar.setMax((float) budget);
            double ownProgress = 0;
            double allProgress = 0;
            if (thisGroup.getCalculationMethod() == Group.costCalculationMethod.ACTUAL_COST) {
                for (Map.Entry<String, Trip> entry : groupTripsMap.entrySet()) {
                    if (entry.getValue().getDriverId().equals(user.getUser_id()))
                        ownProgress += entry.getValue().getCost();
                    allProgress += entry.getValue().getCost();
                }
                progressBar.setSecondaryProgress((float) allProgress);
            }
            if (thisGroup.getCalculationMethod() == Group.costCalculationMethod.KIKOMETER_ALLOWANCE) {
                for (Map.Entry<String, Trip> entry : groupTripsMap.entrySet()) {
                    Trip trip = entry.getValue();
                    if (trip.getDriverId().equals(user.getUser_id()))
                        ownProgress += Double.valueOf(trip.getDistance().split(" ")[0].replaceAll(",", ".")) * thisGroup.getKilometerAllowance();
                    allProgress += Double.valueOf(trip.getDistance().split(" ")[0].replaceAll(",", ".")) * thisGroup.getKilometerAllowance();
                }
                progressBar.setSecondaryProgress((float) allProgress);
            }
            if (thisGroup.getCalculationMethod() == Group.costCalculationMethod.TRIP) {
                for (Map.Entry<String, Trip> entry : groupTripsMap.entrySet()) {
                    Trip trip = entry.getValue();
                    if (trip.getDriverId().equals(user.getUser_id()))
                        ownProgress += trip.isTwoWay() ? 2 : 1;
                    allProgress += trip.isTwoWay() ? 2 : 1;
                }
                if (mySPR_settings.getString("tripCount", "Pro Weg").equals("Pro Fahrt")) {
                    allProgress = allProgress / 2;
                    ownProgress = ownProgress / 2;
                }

                progressBar.setMax((float) allProgress);
                progressBar.setSecondaryProgress((float) allProgress);
            }
            progressBar.setProgress((float) ownProgress);

            if (thisGroup.getCalculationMethod() != Group.costCalculationMethod.TRIP) {
                overview_progress.setText(convertToEuro(allProgress));
                overview_budget.setText(convertToEuro(budget) + "€");
                if (ownProgress >= budget)
                    progressBar.setProgressColor(Color.RED);
                else
                    setColorBasedOnRatio(overview_progressBar, ownProgress, allProgress, thisGroup.getDriverIdList().size(), colorMargin, user);
                if (allProgress >= budget)
                    progressBar.setSecondaryProgressColor(Color.RED);
                else
                    progressBar.setSecondaryProgressColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));

            } else {
                overview_progress.setText(convertToEuro(ownProgress));
                overview_budget.setText(convertToEuro(allProgress));
                setColorBasedOnRatio(overview_progressBar, ownProgress, allProgress, thisGroup.getDriverIdList().size(), colorMargin, user);
                progressBar.setSecondaryProgressColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
            }
        }
        if (thisGroup.getCalculationType() == Group.costCalculationType.COST) {
            double ownProgress = 0;
            double allProgress = 0;
            if (thisGroup.getCalculationMethod() == Group.costCalculationMethod.ACTUAL_COST) {
                for (Map.Entry<String, Trip> entry : groupTripsMap.entrySet()) {
                    if (entry.getValue().getDriverId().equals(user.getUser_id()))
                        ownProgress += entry.getValue().getCost();
                    allProgress += entry.getValue().getCost();
                }
            }
            if (thisGroup.getCalculationMethod() == Group.costCalculationMethod.KIKOMETER_ALLOWANCE) {
                for (Map.Entry<String, Trip> entry : groupTripsMap.entrySet()) {
                    Trip trip = entry.getValue();
                    if (trip.getDriverId().equals(user.getUser_id()))
                        ownProgress += Double.valueOf(trip.getDistance().split(" ")[0].replaceAll(",", ".")) * thisGroup.getKilometerAllowance();
                    allProgress += Double.valueOf(trip.getDistance().split(" ")[0].replaceAll(",", ".")) * thisGroup.getKilometerAllowance();
                }
            }
            progressBar.setMax((float) allProgress);
            progressBar.setProgress((float) ownProgress);
            progressBar.setSecondaryProgress((float) allProgress);

            overview_progress.setText(convertToEuro(ownProgress));
            overview_budget.setText(convertToEuro(allProgress) + "€");
            if (ownProgress >= allProgress)
                progressBar.setProgressColor(Color.RED);
            else
                setColorBasedOnRatio(overview_progressBar, ownProgress, allProgress, thisGroup.getDriverIdList().size(), colorMargin, user);
            progressBar.setSecondaryProgressColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));

        }
    }


    private void setColorBasedOnRatio(RoundCornerProgressBar progressBar, double ownProgress, double allProgress, int size, double margin, User user) {
        if (!thisGroup.getDriverIdList().contains(user.getUser_id())) {
//            progressBar.setProgressColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryProgressBar_toMutch));
            progressBar.setProgressColor(Color.RED);
            return;
        }

        switch (isInRatio(ownProgress, allProgress, size, margin)) {
            case 0:
                progressBar.setProgressColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryProgressBar_tooLittle));
                break;
            case 1:
                progressBar.setProgressColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryProgressBar));
                break;
            case 2:
                progressBar.setProgressColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryProgressBar_tooMutch));
                break;
        }
    }


    int isInRatio(double small, double big, int count, double margin) {
        double ratio = small / big;
        double optimalRatio = (double) 1 / count;
        if (ratio < optimalRatio - margin)
            return 0;
        else if (ratio < optimalRatio + margin && ratio > optimalRatio - margin)
            return 1;
        else if (ratio > optimalRatio + margin)
            return 2;
        else return -1;
    }

    String convertToEuro(double count) {
        DecimalFormat df = new DecimalFormat("#.00");
        count = Double.valueOf(df.format(count).replace(",", "."));
        if (count % 1 == 0) {
            return String.valueOf(count).split("\\.")[0];
        } else {
            return String.valueOf(count);
        }
    }

    private void showChangeCostCalculation() {
        final Dialog dialog_changeCostCalculation = new Dialog(getContext());
        dialog_changeCostCalculation.setContentView(R.layout.dialog_changecost_calculation);

        final RadioGroup dialogChangeCostCalculation_typeGroup;
        final RadioGroup dialogChangeCostCalculation_methodGroup;
        final EditText dialogChangeCostCalculation_budget;
        final CheckBox dialogChangeCostCalculation_perPerson;
        final EditText dialogChangeCostCalculation_kilometerAllowance;

        dialogChangeCostCalculation_typeGroup = dialog_changeCostCalculation.findViewById(R.id.dialogChangeCostCalculation_typeGroup);
        dialogChangeCostCalculation_methodGroup = dialog_changeCostCalculation.findViewById(R.id.dialogChangeCostCalculation_methodGroup);
        dialogChangeCostCalculation_budget = dialog_changeCostCalculation.findViewById(R.id.dialogChangeCostCalculation_budget);
        dialogChangeCostCalculation_perPerson = dialog_changeCostCalculation.findViewById(R.id.dialogChangeCostCalculation_perPerson);
        dialogChangeCostCalculation_kilometerAllowance = dialog_changeCostCalculation.findViewById(R.id.dialogChangeCostCalculation_kilometerAllowance);

        setChangeCostListener(dialog_changeCostCalculation, dialogChangeCostCalculation_typeGroup, dialogChangeCostCalculation_methodGroup);

        switch (thisGroup.getCalculationType()) {
            default:
            case COST:
                dialogChangeCostCalculation_typeGroup.check(R.id.dialogChangeCostCalculation_costRadio);
                break;
            case BUDGET:
                dialogChangeCostCalculation_typeGroup.check(R.id.dialogChangeCostCalculation_budgetRadio);
                break;
        }
        switch (thisGroup.getCalculationMethod()) {
            default:
            case ACTUAL_COST:
                dialogChangeCostCalculation_methodGroup.check(R.id.dialogChangeCostCalculation_realCostRadio);
                break;
            case KIKOMETER_ALLOWANCE:
                dialogChangeCostCalculation_methodGroup.check(R.id.dialogChangeCostCalculation_kilometerAllowanceRadio);
                break;
            case TRIP:
                dialogChangeCostCalculation_methodGroup.check(R.id.dialogChangeCostCalculation_tripRadio);
                break;
        }
        dialogChangeCostCalculation_budget.setText(String.valueOf(thisGroup.getBudget()));
        dialogChangeCostCalculation_perPerson.setChecked(thisGroup.isBudgetPerUser());
        dialogChangeCostCalculation_kilometerAllowance.setText(String.valueOf(thisGroup.getKilometerAllowance()));

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog_changeCostCalculation.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        dialog_changeCostCalculation.show();
        dialog_changeCostCalculation.getWindow().setAttributes(lp);


        dialog_changeCostCalculation.findViewById(R.id.dialogChangeCostCalculation_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (dialogChangeCostCalculation_typeGroup.getCheckedRadioButtonId()) {
                    case R.id.dialogChangeCostCalculation_budgetRadio:
                        thisGroup.setCalculationType(Group.costCalculationType.BUDGET);
                        break;
                    case R.id.dialogChangeCostCalculation_costRadio:
                        thisGroup.setCalculationType(Group.costCalculationType.COST);
                        break;
                }
                switch (dialogChangeCostCalculation_methodGroup.getCheckedRadioButtonId()) {
                    case R.id.dialogChangeCostCalculation_realCostRadio:
                        thisGroup.setCalculationMethod(Group.costCalculationMethod.ACTUAL_COST);
                        break;
                    case R.id.dialogChangeCostCalculation_kilometerAllowanceRadio:
                        thisGroup.setCalculationMethod(Group.costCalculationMethod.KIKOMETER_ALLOWANCE);
                        break;
                    case R.id.dialogChangeCostCalculation_tripRadio:
                        thisGroup.setCalculationMethod(Group.costCalculationMethod.TRIP);
                        break;
                }
                if (dialogChangeCostCalculation_budget.isEnabled()) {
                    if (!dialogChangeCostCalculation_budget.getText().toString().equals("")) {
                        thisGroup.setBudget(Double.parseDouble(dialogChangeCostCalculation_budget.getText().toString()));
                    } else {
                        Toast.makeText(getContext(), "Ein Budget angeben", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                if (dialogChangeCostCalculation_kilometerAllowance.isEnabled()) {
                    if (!dialogChangeCostCalculation_kilometerAllowance.getText().toString().equals("")) {
                        thisGroup.setKilometerAllowance(Double.parseDouble(dialogChangeCostCalculation_kilometerAllowance.getText().toString()));
                    } else {
                        Toast.makeText(getContext(), "Eine Pauschale angeben", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                thisGroup.setBudgetPerUser(dialogChangeCostCalculation_perPerson.isChecked());
                setCalculationTexts();
                // ToDo: gruppe in Firebase aktualisieren
                databaseReference.child("Groups").child(thisGroup.getGroup_id()).setValue(thisGroup);
                dialog_changeCostCalculation.dismiss();
            }
        });
        dialog_changeCostCalculation.findViewById(R.id.dialogChangeCostCalculation_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog_changeCostCalculation.dismiss();
            }
        });
    }

    private void setChangeCostListener(final Dialog dialog_changeCostCalculation, RadioGroup dialogChangeCostCalculation_typeGroup, RadioGroup dialogChangeCostCalculation_methodGroup) {
        dialogChangeCostCalculation_typeGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                switch (checkedId) {
                    case R.id.dialogChangeCostCalculation_budgetRadio:
                        dialog_changeCostCalculation.findViewById(R.id.dialogChangeCostCalculation_budget).setEnabled(true);
                        dialog_changeCostCalculation.findViewById(R.id.dialogChangeCostCalculation_perPerson).setEnabled(true);
                        dialog_changeCostCalculation.findViewById(R.id.dialogChangeCostCalculation_tripRadio).setEnabled(true);
                        break;
                    case R.id.dialogChangeCostCalculation_costRadio:
                        dialog_changeCostCalculation.findViewById(R.id.dialogChangeCostCalculation_budget).setEnabled(false);
                        dialog_changeCostCalculation.findViewById(R.id.dialogChangeCostCalculation_perPerson).setEnabled(false);
                        RadioButton radioButton = dialog_changeCostCalculation.findViewById(R.id.dialogChangeCostCalculation_tripRadio);
                        radioButton.setEnabled(false);
                        if (radioButton.isChecked()) {
                            radioButton.setChecked(false);
                            dialog_changeCostCalculation.findViewById(R.id.dialogChangeCostCalculation_save).setEnabled(false);
                        }
                        break;
                }
            }
        });
        dialogChangeCostCalculation_methodGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                dialog_changeCostCalculation.findViewById(R.id.dialogChangeCostCalculation_save).setEnabled(true);

                dialog_changeCostCalculation.findViewById(R.id.dialogChangeCostCalculation_kilometerAllowance)
                        .setEnabled(checkedId == R.id.dialogChangeCostCalculation_kilometerAllowanceRadio);
            }
        });
    }

    View.OnClickListener costCalculationClickListener = view -> {
        String test = null;
        // ((LinearLayout) view.getParent()).indexOfChild(view) / 2
    };


    private void showCostCalculation() {
        final Dialog dialog_costCalculation = new Dialog(getContext());
        dialog_costCalculation.setContentView(R.layout.dialog_calculate_costs);
        final LinearLayout dialogCostList_list = dialog_costCalculation.findViewById(R.id.dialogCostList_list);
        final Map<String, Double> userTripMap_count = new HashMap<>();


        for (User user : sortedUserList) {
            userTripMap_count.put(user.getUser_id(), Double.valueOf(calculateDrivenAmount(user.getUser_id())));
        }

        List<User> driverList = new ArrayList<>(sortedUserList);
        Collections.sort(driverList, new Comparator() {
            public int compare(User obj1, User obj2) {
                return userTripMap_count.get(obj1.getUser_id()).compareTo(userTripMap_count.get(obj2.getUser_id()));
            }

            public int compare(Object var1, Object var2) {
                return this.compare((User) var1, (User) var2);
            }
        });
        Collections.reverse(driverList);

        double allTripCount = 0;
        for (Double value : userTripMap_count.values()) {
            allTripCount += value;
        }

        double allCost = calculateUserTripCost(null);

        for (User thisUser : driverList) {
//            List<Trip> trips = new ArrayList<>(userTripMap.get(thisUser.getUser_id()).values());
            if (dialogCostList_list.getChildCount() != 0) {
                View divider = new View(getContext());
                divider.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (getResources().getDisplayMetrics().density * 1)));
                divider.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorDdivider));
                dialogCostList_list.addView(divider);
            }

            LayoutInflater li = LayoutInflater.from(getContext());
            View listItem = li.inflate(R.layout.list_item_cost, null);

            listItem.setOnClickListener(costCalculationClickListener);

            TextView costList_name = listItem.findViewById(R.id.costList_name);
            costList_name.setText(thisUser.getUserName());

            TextView costList_tripsOrCost = listItem.findViewById(R.id.costList_tripsOrCost);
            TextView tripList_percentage = listItem.findViewById(R.id.tripList_percentage);
            TextView costList_budgetShare = listItem.findViewById(R.id.costList_budgetShare);
            TextView costList_methodLabel = listItem.findViewById(R.id.costList_methodLabel);
            TextView costList_share = listItem.findViewById(R.id.costList_share);
            TextView costList_costDifference = listItem.findViewById(R.id.costList_costDifference);
            RoundCornerProgressBar progressBar = listItem.findViewById(R.id.costList_progressBar);


            if (thisGroup.getCalculationType() == Group.costCalculationType.BUDGET) {
                if (thisGroup.getCalculationMethod() != Group.costCalculationMethod.TRIP) {
                    costList_methodLabel.setText("Kosten:");

                    double thisCost = calculateUserTripCost(thisUser.getUser_id());
                    costList_tripsOrCost.setText(convertToEuro(thisCost) + "€");

                    progressBar.setMax((float) allCost);
                    progressBar.setProgress((float) thisCost);
                    setColorBasedOnRatio(progressBar, thisCost, allCost, thisGroup.getDriverIdList().size(), colorMargin, thisUser);

                    tripList_percentage.setText((int) (thisCost / allCost * 100) + "%");
                    costList_budgetShare.setText(convertToEuro(thisCost / allCost *
                            (thisGroup.getBudget() * (thisGroup.isBudgetPerUser() ? thisGroup.getUserIdList().size() : 1)
                            )) + "€");
                    // ToDo: check ob weniger als budget - ???
                } else {
                    String drivenAmorunt = calculateDrivenAmount(thisUser.getUser_id());

                    costList_tripsOrCost.setText(String.valueOf(drivenAmorunt));

                    double percentage = Double.valueOf(drivenAmorunt) / allTripCount * 100;

                    tripList_percentage.setText((int) percentage + "%");
                    costList_budgetShare.setText(convertToEuro(Double.valueOf(drivenAmorunt) / allTripCount *
                            (thisGroup.getBudget() * (thisGroup.isBudgetPerUser() ? thisGroup.getUserIdList().size() : 1)
                            )) + "€");

                    progressBar.setMax((float) allTripCount);
                    progressBar.setProgress(Float.valueOf(drivenAmorunt));
                    setColorBasedOnRatio(progressBar, Double.valueOf(drivenAmorunt), allTripCount, thisGroup.getDriverIdList().size(), colorMargin, thisUser);
                }
            } else if (thisGroup.getCalculationType() == Group.costCalculationType.COST) {
                costList_methodLabel.setText("Kosten:");
                costList_share.setText("Anteil an Kosten:");

                double thisCost = calculateUserTripCost(thisUser.getUser_id());
                costList_tripsOrCost.setText(convertToEuro(thisCost) + "€");

                progressBar.setMax((float) allCost);
                progressBar.setProgress((float) thisCost);
                setColorBasedOnRatio(progressBar, thisCost, allCost, thisGroup.getDriverIdList().size(), colorMargin, thisUser);

                tripList_percentage.setText((int) (thisCost / allCost * 100) + "%");

                costList_budgetShare.setText(convertToEuro(allCost / thisGroup.getUserIdList().size()) + "€");

                double costDifference = thisCost - (allCost / thisGroup.getUserIdList().size());

                if (costDifference < 0) {
                    costList_costDifference.setTextColor(Color.RED);
                    costList_costDifference.setText("(" + convertToEuro(Math.abs(costDifference)) + "€)");
                } else if (costDifference > 0) {
                    costList_costDifference.setText("(" + convertToEuro(costDifference) + "€)");
                    costList_costDifference.setTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryProgressBar));
                }


                // ToDo: (optional) Aufschlüsseln wer wem wie viel geld schuldet - Schulden durch Anzahl Schuldner - ???
            }

            dialogCostList_list.addView(listItem);
        }

        dialog_costCalculation.findViewById(R.id.dialogCostList_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog_costCalculation.dismiss();
            }
        });

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog_costCalculation.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        dialog_costCalculation.show();
        dialog_costCalculation.getWindow().setAttributes(lp);
    }

    private void showTripList(boolean showAll) {


        dialog_tripList = CustomDialog.Builder(getContext())
                .setTitle("Trip Liste")
                .setView(R.layout.dialog_trip_list)
                .setButtonType(CustomDialog.buttonType_Enum.BACK)
                .show();

        dialogTripList_list = dialog_tripList.findViewById(R.id.dialogTripList_list);

        dialogTripList_list.setOnItemClickListener((adapterView, view, i, l) -> {

            Dialog dialog_deleteTrip = CustomDialog.Builder(getContext())
                    .setTitle("Den Trip Löschen?")
                    .setView(R.layout.dialog_delete_trip)
                    .addButton(CustomDialog.YES_BUTTON, () -> {
                        Trip trip = tripList.get(i);

                        thisGroup.getTripIdList().remove(trip.getTrip_id());
                        groupTripsMap.remove(trip.getTrip_id());
                        databaseReference.child("Groups").child(thisGroup.getGroup_id()).child("tripIdList").setValue(thisGroup.getTripIdList());
                        databaseReference.child("Trips").child(thisGroup.getGroup_id()).child(trip.getTrip_id()).removeValue();

                        tripListLaden(showAll);
                        reLoadContent();
                        thisGroupCalender.setData(loggedInUser, thisGroup, groupPassengerMap,groupTripsMap);
                        thisGroupCalender.reLoadContent();
                    })
                    .addButton(CustomDialog.NO_BUTTON, () -> {})
                    .show();
//            Dialog dialog_deleteTrip = new Dialog(getContext());
//            dialog_deleteTrip.setContentView(R.layout.dialog_delete_trip);

            LinearLayout dialogDeleteTrip_layout = dialog_deleteTrip.findViewById(R.id.dialogDeleteTrip_layout);
            View newView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_trip, null);
            dialogDeleteTrip_layout.addView(newView);

            ((TextView) newView.findViewById(R.id.tripList_date)).setText(((TextView) view.findViewById(R.id.tripList_date)).getText());
            ((TextView) newView.findViewById(R.id.tripList_driver)).setText(((TextView) view.findViewById(R.id.tripList_driver)).getText());
            ((TextView) newView.findViewById(R.id.tripList_fromTo)).setText(((TextView) view.findViewById(R.id.tripList_fromTo)).getText());
            ((TextView) newView.findViewById(R.id.tripList_distance)).setText(((TextView) view.findViewById(R.id.tripList_distance)).getText());
            ((TextView) newView.findViewById(R.id.tripList_twoWay)).setText(((TextView) view.findViewById(R.id.tripList_twoWay)).getText());
            ((TextView) newView.findViewById(R.id.tripList_cost)).setText(((TextView) view.findViewById(R.id.tripList_cost)).getText());

        });


        tripListLaden(showAll);

        // ToDo: Fahrt per Klick löschen (erst abfrage per Menü, oder Dialog)

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog_tripList.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        dialog_tripList.show();
        dialog_tripList.getWindow().setAttributes(lp);
    }

    private void tripListLaden(boolean showAll) {
        List<String> tripList_date_list = new ArrayList<>();
        List<String> tripList_driver_list = new ArrayList<>();
        List<String> tripList_fromTo_list = new ArrayList<>();
        List<String> tripList_distance_list = new ArrayList<>();
        List<String> tripList_twoWay_list = new ArrayList<>();
        List<String> tripList_cost_list = new ArrayList<>();

        tripList = new ArrayList<>();
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

        for (Trip trip : tripList) {
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

        for (int i = 0; i < tripList.size(); ++i) {
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
    }

    void setDialogLayoutParameters(Dialog dialog, boolean width, boolean height) {
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        if (width)
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        if (height)
            lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }

    double calculateUserTripCost(String userId) {
        double count = 0;
        if (userId == null) {
            for (Map<String, Trip> tripMap : userTripMap.values()) {
                for (Trip trip : tripMap.values()) {
                    if (thisGroup.getCalculationMethod() == Group.costCalculationMethod.ACTUAL_COST)
                        count += trip.getCost();
                    else
                        count += Double.valueOf(trip.getDistance().split(" ")[0].replace(",", ".")) * thisGroup.getKilometerAllowance();
                }
            }
            return count;
        }
        if (userTripMap.get(userId) == null)
            return count;

        for (Trip trip : userTripMap.get(userId).values()) {
            if (thisGroup.getCalculationMethod() == Group.costCalculationMethod.ACTUAL_COST)
                count += trip.getCost();
            else
                count += Double.valueOf(trip.getDistance().split(" ")[0].replace(",", ".")) * thisGroup.getKilometerAllowance();
        }
        return count;
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

        for (int i = 0; i < sortedUserList.size(); ++i) {
            HashMap<String, Serializable> hm = new HashMap<String, Serializable>();
            (hm).put("listview_title", listviewTitle.get(i));
            (hm).put("listview_isDriver", listviewIsDriver.get(i) ? R.drawable.ic_lenkrad : R.drawable.ic_leer);
            (hm).put("listview_discription_ownAmount", listviewOwnDrivenAmount.get(i));
            aList.add(hm);
        }

        String[] from = new String[]{"listview_title", "listview_isDriver", "listview_discription_ownAmount"};
        int[] to = new int[]{R.id.userList_name, R.id.userList_image, R.id.userList_ownAmount};
        SimpleAdapter simpleAdapter = new SimpleAdapter(this.getContext(), aList, R.layout.list_item_passenger, from, to);
        userList.setAdapter(simpleAdapter);

        // ToDo: fahrten nach anteil farblich markieren

        ViewTreeObserver.OnPreDrawListener mOnPreDrawListener = new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                ListAdapter listAdapter = userList.getAdapter();

                for (int i = 0; i < listAdapter.getCount(); i++) {
                    View rowView = userList.getChildAt(i);//The item number in the List View
                    if (rowView != null) {
                        TextView userList_color = rowView.findViewById(R.id.userList_color);
                        userList_color.setTextColor(Color.parseColor(sortedUserList.get(i).getUserColor()));
                    }
                }
                userList.getViewTreeObserver().removeOnPreDrawListener(this);

                return true;
            }
        };
        userList.getViewTreeObserver().addOnPreDrawListener(mOnPreDrawListener);
    }

}

class ViewPager_GroupCalender extends Fragment {

    View view;
    User loggedInUser;
    Group thisGroup;
    CompactCalendarView calendarView;
    TextView calender_month;
    ImageView calender_previousMonth;
    ImageView calender_nextMonth;
    TextView calender_noTrips;
    LinearLayout calender_tripList_Layout;



    List<String> listviewTitle = new ArrayList<String>();
    List<Boolean> listviewIsDriver = new ArrayList<>();
    List<java.io.Serializable> listviewOwnDrivenAmount = new ArrayList<>();
    List<User> sortedUserList = new ArrayList<>();
    Map<String, User> groupPassengerMap = new HashMap<>();
    Map<String, Trip> groupTripsMap = new HashMap<>();
    List<Trip> userTripList = new ArrayList<>();
    Map<String, Map<String, Trip>> userTripMap = new HashMap<>();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.group_calender, container, false);
        calendarView = view.findViewById(R.id.calender_calendar);
        calender_month = view.findViewById(R.id.calender_month);
        calender_previousMonth = view.findViewById(R.id.calender_previousMonth);
        calender_nextMonth = view.findViewById(R.id.calender_nextMonth);
        calender_noTrips = view.findViewById(R.id.calender_noTrips);
        calender_tripList_Layout = view.findViewById(R.id.calender_tripList_Layout);

        calender_previousMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calendarView.scrollLeft();
            }
        });
        calender_nextMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calendarView.scrollRight();
            }
        });

//        ViewGroup rootView = (ViewGroup) inflater.inflate(
//                R.layout.group_calender, container, false);
        reLoadContent();

        return view;
    }

    public void setData(User pLoggedInUser, Group pThisGroup, Map<String, User> pGroupPassengerMap, Map<String, Trip> pGroupTripsMap) {
        loggedInUser = pLoggedInUser;
        thisGroup = pThisGroup;
        groupPassengerMap = pGroupPassengerMap;
        groupTripsMap = pGroupTripsMap;
    }

    public void reLoadContent() {
        loadCalender();
    }


    private void loadCalender() {
        // Set first day of week to Monday, defaults to Monday so calling setFirstDayOfWeek is not necessary
        // Use constants provided by Java Calendar class
        calendarView.removeAllEvents();
        calendarView.setFirstDayOfWeek(Calendar.MONDAY);
        calendarView.shouldSelectFirstDayOfMonthOnScroll(false);
//        calendarView.displayOtherMonthDays(true);

        for (String tripId : thisGroup.getTripIdList()) {
            Trip trip = groupTripsMap.get(tripId);
            Event ev1 = new Event(Color.parseColor(groupPassengerMap.get(trip.getDriverId()).getUserColor())
                    , trip.getDate().getTime(), tripId);
            calendarView.addEvent(ev1);

        }
        calendarView.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {
                loadTripList(calendarView.getEvents(dateClicked));
//                Log.d(TAG, "Day was clicked: " + dateClicked + " with events " + events);
            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
//                Log.d(TAG, "Month was scrolled to: " + firstDayOfNewMonth);
                SimpleDateFormat sdfmt = new SimpleDateFormat();
                sdfmt.applyPattern( "MMMM yyyy" );
                calender_month.setText(sdfmt.format(firstDayOfNewMonth));
            }
        });

        loadTripList(calendarView.getEvents(new Date()));

    }

    void loadTripList(List<Event> eventList){
//        String pattern = "dd.MM.yyyy E";
//        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

        if (eventList.isEmpty())
            calender_noTrips.setVisibility(View.VISIBLE);
        else
            calender_noTrips.setVisibility(View.GONE);

        calender_tripList_Layout.removeAllViews();
        for (Event event : eventList) {
            Trip trip = groupTripsMap.get(event.getData().toString());
            User user = groupPassengerMap.get(trip.getDriverId());
            if (calender_tripList_Layout.getChildCount() != 0) {
                View divider = new View(getContext());
                divider.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (getResources().getDisplayMetrics().density * 1)));
                divider.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorDdivider));
                calender_tripList_Layout.addView(divider);
            }

            LayoutInflater li = LayoutInflater.from(getContext());
            View listItem = li.inflate(R.layout.list_item_trip, null);

            TextView tripList_date = listItem.findViewById(R.id.tripList_date);
            tripList_date.setText("");

            TextView tripList_driver = listItem.findViewById(R.id.tripList_driver);
            tripList_driver.setText(user.getUserName());
            tripList_driver.setTextColor(Color.parseColor(user.getUserColor()));

            TextView tripList_fromTo = listItem.findViewById(R.id.tripList_fromTo);
            tripList_fromTo.setText(trip.getLocationName().get(0) +
                    (trip.isTwoWay() ? " ⇔ " : " ⇒ ") + trip.getLocationName().get(1));

            TextView tripList_distance = listItem.findViewById(R.id.tripList_distance);
            tripList_distance.setText(trip.getDistance());

            TextView tripList_twoWay = listItem.findViewById(R.id.tripList_twoWay);
            tripList_twoWay.setText(trip.isTwoWay() ? "x2" : "");

            DecimalFormat df = new DecimalFormat("#.00");
            TextView tripList_cost = listItem.findViewById(R.id.tripList_cost);
            tripList_cost.setText(df.format(trip.getCost()) + "€");


            calender_tripList_Layout.addView(listItem);
        }

    }
}