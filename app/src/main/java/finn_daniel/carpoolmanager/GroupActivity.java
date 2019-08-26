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
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SearchView;
import android.widget.SimpleAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar;
import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

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
    String standardView_group;
    Gson gson = new Gson();
    public static String EXTRA_GROUP_ID = "EXTRA_GROUP_ID";
    String EXTRA_PASSENGERMAP = "EXTRA_PASSENGERMAP";
    String EXTRA_TRIPMAP = "EXTRA_TRIPMAP";
    int NEWTRIP = 001;
    String thisGroup_Id;
    FloatingActionButton group_addTrip;
    DatabaseReference databaseReference;

    ViewPager_GroupOverview thisGroupOverview = new ViewPager_GroupOverview();
    ViewPager_GroupCalender thisGroupCalender = new ViewPager_GroupCalender();

    Database database = Database.getInstance();
    Database.OnChangeListener onGroupChangeListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        group_addTrip = findViewById(R.id.group_addTrip);
        databaseReference = FirebaseDatabase.getInstance().getReference();

        thisGroup_Id = getIntent().getStringExtra(EXTRA_GROUP_ID);

        thisGroupOverview.setData(thisGroupCalender, thisGroup_Id, group_addTrip);
        thisGroupCalender.setData(thisGroup_Id);
        SharedPreferences mySPR = getSharedPreferences("CarPoolManager_Settings", 0);
        standardView_group = mySPR.getString("standardView_group", "Übersicht");

        mPager = findViewById(R.id.group_pager);
        pagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(pagerAdapter);
        mPager.setCurrentItem(standardView_group.equals("Übersicht") ? 0 : 1);

        TabLayout tabLayout = findViewById(R.id.group_tabLayout);
        tabLayout.setupWithViewPager(mPager);
        tabLayout.getTabAt(0).setText("Übersicht");
        tabLayout.getTabAt(1).setText("Kalender");

        findViewById(R.id.group_addTrip).setOnClickListener(view -> {
            if (!Utility.isOnline(GroupActivity.this))
                return;

            Intent intent = new Intent(GroupActivity.this, AddTripActivity.class);
            intent.putExtra(EXTRA_GROUP_ID, thisGroup_Id);
//            intent.putExtra(EXTRA_PASSENGERMAP, gson.toJson(database.groupPassengerMap));
//            intent.putExtra(EXTRA_TRIPMAP, gson.toJson(database.groupTripMap.get(thisGroup_Id)));
            startActivityForResult(intent, NEWTRIP);
        });

        Toolbar toolbar = findViewById(R.id.group_toolbar);
        toolbar.setTitle(database.groupsMap.get(thisGroup_Id).getName());
//        ((TextView) findViewById(R.id.group_toolbar_title)).setText(database.groupsMap.get(thisGroup_Id).getName());
        toolbar.setNavigationIcon(ContextCompat.getDrawable(this, R.drawable.ic_arrow_back));
        toolbar.setNavigationOnClickListener(v -> {
            database.removeOnGroupChangeListener(onGroupChangeListener);
            thisGroupOverview.removeListeners();
            thisGroupCalender.removeListeners();
            finish();
        });
        toolbar.inflateMenu(R.menu.group_edit);
        toolbar.setOnMenuItemClickListener(item -> {
            if (!Utility.isOnline(GroupActivity.this))
                return true;

            int buttonId = View.generateViewId();
            CustomDialog.Builder(this)
                    .setTitle("Gruppen-Namen Ändern")
                    .setButtonType(CustomDialog.ButtonType.OK_CANCEL)
                    .setEdit(new CustomDialog.EditBuilder()
                            .setShowKeyboard(true)
                            .setSelectAll(true)
                            .setText(database.groupsMap.get(thisGroup_Id).getName())
                            .setHint("Neuer Gruppen-Name")
                            .setDiableButtonWhenEmpty(buttonId))
                    .addButton(CustomDialog.OK_BUTTON, dialog -> {
                        String name = CustomDialog.getEditText(dialog);
                        toolbar.setTitle(name);
                        database.groupsMap.get(thisGroup_Id).setName(name);
                        databaseReference.child("Groups").child(database.groupsMap.get(thisGroup_Id).getGroup_id()).setValue(database.groupsMap.get(thisGroup_Id));
                        // ToDo: namen ändern
                        dialog.dismiss();
                    }, buttonId,false)
                    .show();
            return true;
        });
        onGroupChangeListener = database.addOnGroupChangeListener(() ->
                toolbar.setTitle(database.groupsMap.get(thisGroup_Id).getName()));
        // ToDo: wegen layout_gravity bescheid sagen
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


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == NEWTRIP && resultCode == RESULT_OK) {
            thisGroupOverview.reLoadContent();
            thisGroupCalender.reLoadContent();
        }
    }

    @Override
    public void onBackPressed() {
        if ((mPager.getCurrentItem() == 0 && standardView_group.equals("Übersicht")) || (mPager.getCurrentItem() == 1 && standardView_group.equals("Kalender"))) {
            database.removeOnGroupChangeListener(onGroupChangeListener);
            thisGroupOverview.removeListeners();
            thisGroupCalender.removeListeners();
            super.onBackPressed();
        } else {
            mPager.setCurrentItem(mPager.getCurrentItem() == 0 ? 1 : 0);
        }
    }
}

class ViewPager_GroupOverview extends Fragment {

    String thisGroup_Id;
    View view;
    RecyclerView userList;
    Dialog dialog_tripList;
    SharedPreferences mySPR_settings;
    double colorMargin = 0.1;
    DatabaseReference databaseReference;
    boolean isDriver;
    Database.OnChangeListener onGroupChangeListener;

    Button overview_showAllTrips;
    Button overview_showMyTrips;
    Button overview_calculateCosts;
    Button overview_editPassengers;
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

    List<String> listviewTitle = new ArrayList<>();
    List<Boolean> listviewIsDriver = new ArrayList<>();
    List<java.io.Serializable> listviewOwnDrivenAmount = new ArrayList<>();
    List<User> sortedUserList = new ArrayList<>();
    List<Trip> userTripList = new ArrayList<>();
    Map<String, Map<String, Trip>> userTripMap = new HashMap<>();
    List<Trip> tripList;
    Database database = Database.getInstance();
    
    public void setData(ViewPager_GroupCalender thisGroupCalender, String thisGroup_Id, FloatingActionButton pGroup_addTrip) {
        this.thisGroup_Id = thisGroup_Id;
        group_addTrip = pGroup_addTrip;
        this.thisGroupCalender = thisGroupCalender;
    }

    public void reLoadContent() {
        overview_showAllTrips.setText(String.valueOf(calculateDrivenAmount(null)));
        overview_showMyTrips.setText(String.valueOf(calculateDrivenAmount(database.loggedInUser.getUser_id())));
        listeLaden();
        setCalculationTexts();
    }

    public void removeListeners() {
        database.removeOnGroupChangeListener(onGroupChangeListener);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.group_overview, container, false);
        databaseReference = FirebaseDatabase.getInstance().getReference();
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
            if (!Utility.isOnline(getContext()))
                return;

            Dialog dialog1 = CustomDialog.Builder(getContext())
                    .setTitle("Mitfahrer bearbeiten")
                    .setText("Was möchtest du tun?")
//                    .setDividerVisibility(false)
                    .setButtonType(CustomDialog.ButtonType.CUSTOM)
                    .addButton("Gruppe Verlassen", dialog -> {

                        CustomDialog.Builder(getContext())
                                .setTitle("Gruppe Verlassen")
                                .setText("Möchtest du wirklich die Gruppe verlassen?")
                                .setButtonType(CustomDialog.ButtonType.YES_NO)
                                .addButton(CustomDialog.YES_BUTTON, dialog2 -> {

                                    if (leaveGroup(database.loggedInUser, database.groupsMap.get(thisGroup_Id)).equals(Database.SUCCSESS)) {
                                        Toast.makeText(getContext(), "Gruppe Verlasen"
                                                + (database.groupsMap.get(thisGroup_Id).getUserIdList().size() == 0 ? " und gelöscht" : ""), Toast.LENGTH_SHORT).show();
                                        getActivity().finish();
                                    } else {
                                        Toast.makeText(getContext(), "Failed", Toast.LENGTH_SHORT).show();
                                    }


                                    dialog.dismiss();
                                })
                                .show();

                    }, false)
                    .addButton("Mitfahrer hinzufügen", dialog ->
                            {
                                // ToDo: wegen Datenschutz gedanken machen
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

                                        showAddPassengerDialog(userList, dialog);

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                    }
                                });

                            },
                            false)
                    .show();


        });

        overview_changeCostCalculation.setOnClickListener(view -> showChangeCostCalculation());

        mySPR_settings = getActivity().getSharedPreferences("CarPoolManager_Settings", 0);

        view.findViewById(R.id.overview_isDriverSwitch).setOnClickListener(view ->
                overview_save_isDriver.setVisibility(overview_isDriverSwitch.isChecked() == isDriver ?
                View.INVISIBLE : View.VISIBLE));

        overview_save_isDriver.setOnClickListener(view -> {
            if (!Utility.isOnline(getContext()))
                return;

            if (overview_isDriverSwitch.isChecked())
                database.groupsMap.get(thisGroup_Id).getDriverIdList().add(database.loggedInUser.getUser_id());
            else
                database.groupsMap.get(thisGroup_Id).getDriverIdList().remove(database.loggedInUser.getUser_id());
            isDriver = overview_isDriverSwitch.isChecked();
            overview_save_isDriver.setVisibility(View.INVISIBLE);
//            reLoadContent();
            databaseReference.child("Groups").child(database.groupsMap.get(thisGroup_Id).getGroup_id()).child("driverIdList").setValue(database.groupsMap.get(thisGroup_Id).getDriverIdList());
//            if (database.groupsMap.get(thisGroup_Id).getDriverIdList().size() <= 0) {
//                group_addTrip.setVisibility(View.INVISIBLE);
//                overview_noDriverText.setVisibility(View.VISIBLE);
//                overview_noDriverText.setSelected(true);
//            } else {
//                group_addTrip.setVisibility(View.VISIBLE);
//                overview_noDriverText.setVisibility(View.INVISIBLE);
//            }
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
                if (database.groupsMap.get(thisGroup_Id).getTripIdList().size() != 0)
                    showCostCalculation();
                else
                    Toast.makeText(getContext(), "Es gibt nix zum anzeigen", Toast.LENGTH_SHORT).show();
            }
        });

        group_addTrip.setVisibility(database.groupsMap.get(thisGroup_Id).getDriverIdList().size() > 0 ?
                View.VISIBLE : View.INVISIBLE);
        overview_noDriverText.setVisibility(database.groupsMap.get(thisGroup_Id).getDriverIdList().size() <= 0 ?
                View.VISIBLE : View.INVISIBLE);
        overview_noDriverText.setSelected(database.groupsMap.get(thisGroup_Id).getDriverIdList().size() <= 0);
        overview_isDriverSwitch.setChecked(database.groupsMap.get(thisGroup_Id).getDriverIdList().contains(database.loggedInUser.getUser_id()));
        isDriver = database.groupsMap.get(thisGroup_Id).getDriverIdList().contains(database.loggedInUser.getUser_id());
        overview_save_isDriver.setVisibility(View.INVISIBLE);
        reLoadContent();

        for (Map.Entry<String, Trip> entry : database.groupTripMap.get(thisGroup_Id).entrySet()) {
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

        onGroupChangeListener = database.addOnGroupChangeListener(() -> {
            if (database.groupsMap.get(thisGroup_Id).getDriverIdList().size() <= 0) {
                group_addTrip.setVisibility(View.INVISIBLE);
                overview_noDriverText.setVisibility(View.VISIBLE);
                overview_noDriverText.setSelected(true);
            } else {
                group_addTrip.setVisibility(View.VISIBLE);
                overview_noDriverText.setVisibility(View.INVISIBLE);
            }
            reLoadContent();
        });

        return view;
    }

    private String leaveGroup(User user, Group group) {

        List<Trip> tripList = new ArrayList<>();
        for (Trip trip : database.groupTripMap.get(thisGroup_Id).values()) {
            if (trip.getDriverId().equals(database.loggedInUser.getUser_id()))
                tripList.add(trip);
        }

        database.groupsMap.get(thisGroup_Id).getUserIdList().remove(database.loggedInUser.getUser_id());
        database.groupsMap.get(thisGroup_Id).getDriverIdList().remove(database.loggedInUser.getUser_id());
        List<Trip> lesezeichenList = new ArrayList<>();
        database.groupsMap.get(thisGroup_Id).getBookmarkList().forEach(trip -> {
            if (trip.getDriverId().equals(database.loggedInUser.getUser_id()))
                lesezeichenList.add(trip);
        });
        database.groupsMap.get(thisGroup_Id).getBookmarkList().removeAll(lesezeichenList);
        tripList.stream().forEach(trip -> {
            database.groupsMap.get(thisGroup_Id).getTripIdList().remove(trip.getTrip_id());
            databaseReference.child(Database.TRIPS).child(database.groupsMap.get(thisGroup_Id).getGroup_id()).child(trip.getTrip_id()).removeValue();
        });
        database.loggedInUser.getGroupIdList().remove(database.groupsMap.get(thisGroup_Id).getGroup_id());

        if (database.groupsMap.get(thisGroup_Id).getUserIdList().size() == 0)
            databaseReference.child(Database.GROUPS).child(database.groupsMap.get(thisGroup_Id).getGroup_id()).removeValue();
        else
            databaseReference.child(Database.GROUPS).child(database.groupsMap.get(thisGroup_Id).getGroup_id()).setValue(database.groupsMap.get(thisGroup_Id));

        databaseReference.child(Database.USERS).child(database.loggedInUser.getUser_id()).setValue(database.loggedInUser);

        return Database.SUCCSESS;
    }

    private void showAddPassengerDialog(List<User> userList, Dialog editPassengersDialog) {

        userList.removeAll(sortedUserList);

        List<User> selectedUserList = new  ArrayList<>();
        List<User> filterdUserList = new ArrayList<>(userList);
        int saveButtonId = View.generateViewId();

        Dialog dialog_AddPassenger = CustomDialog.Builder(getContext())
                .setTitle("Mitfahrer hinzufügen")
                .setButtonType(CustomDialog.ButtonType.SAVE_CANCEL)
                .setView(R.layout.dialog_add_passenger)
                .setDimensions(true, true)
                .addButton(CustomDialog.SAVE_BUTTON, dialog -> {
                    for (User user : selectedUserList) {
                        user.getGroupIdList().add(database.groupsMap.get(thisGroup_Id).getGroup_id());
                        database.groupPassengerMap.put(user.getUser_id(), user);
                        database.groupsMap.get(thisGroup_Id).getUserIdList().add(user.getUser_id());
                        listeLaden();
                        editPassengersDialog.dismiss();
                        databaseReference.child("Users").child(user.getUser_id()).setValue(user);
                        databaseReference.child("Groups").child(database.groupsMap.get(thisGroup_Id).getGroup_id()).setValue(database.groupsMap.get(thisGroup_Id));

                    }
                }, saveButtonId)
                .show();

        Button saveButton = dialog_AddPassenger.findViewById(saveButtonId);
        saveButton.setEnabled(false);

        CustomRecycler customRecycler_selectList = CustomRecycler.Builder(getContext(), dialog_AddPassenger.findViewById(R.id.dialogAddPassenger_selectPassengers));

        CustomRecycler customRecycler_selectedList = CustomRecycler.Builder(getContext(), dialog_AddPassenger.findViewById(R.id.dialogAddPassenger_selectedPassengers))
                .setItemView(R.layout.list_item_user_bubble)
                .setObjectList(selectedUserList)
                .setShowDivider(false)
                .setViewList(viewIdList -> {
                    viewIdList.add(R.id.userList_bubble_name);
                    viewIdList.add(R.id.userList_bubble_email);
                    return viewIdList;
                })
                .setSetItemContent((viewHolder, ViewIdMap, object) -> {
                    User user = (User) object;
                    ((TextView) ViewIdMap.get(R.id.userList_bubble_name)).setText(user.getUserName());
                    ((TextView) ViewIdMap.get(R.id.userList_bubble_email)).setText(user.getEmailAddress());
                    ViewIdMap.get(R.id.userList_bubble_email).setSelected(true);
                })
                .setOrientation(CustomRecycler.ORIENTATION.HORIZONTAL)
                .setOnClickListener((recycler, view, object, index) -> {
                    Toast.makeText(getContext(),
                            "Halten zum abwählen" , Toast.LENGTH_SHORT).show();
                })
                .setOnLongClickListener((recycler, view, object, index) -> {
                    ((CustomRecycler.MyAdapter) recycler.getAdapter()).removeItemAt(index);
                    selectedUserList.remove(object);

                    if (selectedUserList.size() <= 0) {
                        dialog_AddPassenger.findViewById(R.id.dialogAddPassenger_nothingSelected).setVisibility(View.VISIBLE);
                        dialog_AddPassenger.findViewById(saveButtonId).setEnabled(false);
                    } else {
                        dialog_AddPassenger.findViewById(R.id.dialogAddPassenger_nothingSelected).setVisibility(View.GONE);
                        dialog_AddPassenger.findViewById(saveButtonId).setEnabled(true);
                    }

                    // ToDo: wieder probleme weil entladen
                    customRecycler_selectList.setObjectList(filterdUserList).reload();
//                    ((CheckBox) customRecycler_selectList.getRecycler().getLayoutManager().findViewByPosition(
//                            filterdUserList.indexOf(object)
//                    ).findViewById(R.id.selectUserList_selected)).setChecked(false);
//                    ((CheckBox) ((CustomRecycler.MyAdapter) recycler.getAdapter()).getViewHolders().get(filterdUserList.indexOf(object))
//                            .viewMap.get(R.id.selectUserList_selected)).setChecked(false);

                })
                .setUseCustomRipple(true)
                .generateCustomRecycler();

        customRecycler_selectList
                .setItemView(R.layout.list_item_select_user)
                .setMultiClickEnabled(true)
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
                    if (selectedUserList.contains(user))
                        ((CheckBox) ViewIdMap.get(R.id.selectUserList_selected)).setChecked(true);
                })
                .setOnClickListener((recycler, view, object, index) -> {
                    User user = ((User) object);
                    CheckBox checkBox = view.findViewById(R.id.selectUserList_selected);
                    checkBox.setChecked(!checkBox.isChecked());
                    if (selectedUserList.contains(user))
                        selectedUserList.remove(user);
                    else
                        selectedUserList.add(user);

                    if (selectedUserList.size() <= 0) {
                        dialog_AddPassenger.findViewById(R.id.dialogAddPassenger_nothingSelected).setVisibility(View.VISIBLE);
                        dialog_AddPassenger.findViewById(saveButtonId).setEnabled(false);
                    } else {
                        dialog_AddPassenger.findViewById(R.id.dialogAddPassenger_nothingSelected).setVisibility(View.GONE);
                        dialog_AddPassenger.findViewById(saveButtonId).setEnabled(true);
                    }

                    customRecycler_selectedList.setObjectList(selectedUserList).reload();
                })
                .generateCustomRecycler();

        SearchView searchView = dialog_AddPassenger.findViewById(R.id.dialogAddPassenger_search);
        searchView.requestFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {

                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                s = s.trim();
                if (s.equals(""))
                    customRecycler_selectList.setObjectList(userList).reload();

                filterdUserList.clear();
                for (User user : userList) {
                    if (user.getUserName().toLowerCase().contains(s.toLowerCase()) ||
                            user.getEmailAddress().toLowerCase().contains(s.toLowerCase()))
                        filterdUserList.add(user);
                }

                customRecycler_selectList.setObjectList(filterdUserList).reload();

                return true;
            }
        });


    }

    private void setCalculationTexts() {
        String typeText;
        String methodText;
        switch (database.groupsMap.get(thisGroup_Id).getCalculationType()) {
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
        switch (database.groupsMap.get(thisGroup_Id).getCalculationMethod()) {
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

        setProgressBar(overview_progressBar, database.loggedInUser);

    }

    private void setProgressBar(RoundCornerProgressBar progressBar, User user) {
        if (database.groupsMap.get(thisGroup_Id).getCalculationType() == Group.costCalculationType.BUDGET) {
            double budget = database.groupsMap.get(thisGroup_Id).getBudget();
            if (database.groupsMap.get(thisGroup_Id).isBudgetPerUser()) {
                budget *= database.groupsMap.get(thisGroup_Id).getUserIdList().size();
            }
            progressBar.setMax((float) budget);
            double ownProgress = 0;
            double allProgress = 0;
            if (database.groupsMap.get(thisGroup_Id).getCalculationMethod() == Group.costCalculationMethod.ACTUAL_COST) {
                for (Map.Entry<String, Trip> entry : database.groupTripMap.get(thisGroup_Id).entrySet()) {
                    if (entry.getValue().getDriverId().equals(user.getUser_id()))
                        ownProgress += entry.getValue().getCost();
                    allProgress += entry.getValue().getCost();
                }
                progressBar.setSecondaryProgress((float) allProgress);
            }
            if (database.groupsMap.get(thisGroup_Id).getCalculationMethod() == Group.costCalculationMethod.KIKOMETER_ALLOWANCE) {
                for (Map.Entry<String, Trip> entry : database.groupTripMap.get(thisGroup_Id).entrySet()) {
                    Trip trip = entry.getValue();
                    if (trip.getDriverId().equals(user.getUser_id()))
                        ownProgress += Double.valueOf(trip.getDistance().split(" ")[0].replaceAll(",", ".")) * database.groupsMap.get(thisGroup_Id).getKilometerAllowance();
                    allProgress += Double.valueOf(trip.getDistance().split(" ")[0].replaceAll(",", ".")) * database.groupsMap.get(thisGroup_Id).getKilometerAllowance();
                }
                progressBar.setSecondaryProgress((float) allProgress);
            }
            if (database.groupsMap.get(thisGroup_Id).getCalculationMethod() == Group.costCalculationMethod.TRIP) {
                for (Map.Entry<String, Trip> entry : database.groupTripMap.get(thisGroup_Id).entrySet()) {
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

            if (database.groupsMap.get(thisGroup_Id).getCalculationMethod() != Group.costCalculationMethod.TRIP) {
                overview_progress.setText(convertToEuro(allProgress));
                overview_budget.setText(convertToEuro(budget) + "€");
                if (ownProgress >= budget)
                    progressBar.setProgressColor(Color.RED);
                else
                    setColorBasedOnRatio(overview_progressBar, ownProgress, allProgress, database.groupsMap.get(thisGroup_Id).getDriverIdList().size(), colorMargin, user);
                if (allProgress >= budget)
                    progressBar.setSecondaryProgressColor(Color.RED);
                else
                    progressBar.setSecondaryProgressColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));

            } else {
                overview_progress.setText(convertToEuro(ownProgress));
                overview_budget.setText(convertToEuro(allProgress));
                setColorBasedOnRatio(overview_progressBar, ownProgress, allProgress, database.groupsMap.get(thisGroup_Id).getDriverIdList().size(), colorMargin, user);
                progressBar.setSecondaryProgressColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
            }
        }
        if (database.groupsMap.get(thisGroup_Id).getCalculationType() == Group.costCalculationType.COST) {
            double ownProgress = 0;
            double allProgress = 0;
            if (database.groupsMap.get(thisGroup_Id).getCalculationMethod() == Group.costCalculationMethod.ACTUAL_COST) {
                for (Map.Entry<String, Trip> entry : database.groupTripMap.get(thisGroup_Id).entrySet()) {
                    if (entry.getValue().getDriverId().equals(user.getUser_id()))
                        ownProgress += entry.getValue().getCost();
                    allProgress += entry.getValue().getCost();
                }
            }
            if (database.groupsMap.get(thisGroup_Id).getCalculationMethod() == Group.costCalculationMethod.KIKOMETER_ALLOWANCE) {
                for (Map.Entry<String, Trip> entry : database.groupTripMap.get(thisGroup_Id).entrySet()) {
                    Trip trip = entry.getValue();
                    if (trip.getDriverId().equals(user.getUser_id()))
                        ownProgress += Double.valueOf(trip.getDistance().split(" ")[0].replaceAll(",", ".")) * database.groupsMap.get(thisGroup_Id).getKilometerAllowance();
                    allProgress += Double.valueOf(trip.getDistance().split(" ")[0].replaceAll(",", ".")) * database.groupsMap.get(thisGroup_Id).getKilometerAllowance();
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
                setColorBasedOnRatio(overview_progressBar, ownProgress, allProgress, database.groupsMap.get(thisGroup_Id).getDriverIdList().size(), colorMargin, user);
            progressBar.setSecondaryProgressColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));

        }
    }


    private void setColorBasedOnRatio(RoundCornerProgressBar progressBar, double ownProgress, double allProgress, int size, double margin, User user) {
        if (!database.groupsMap.get(thisGroup_Id).getDriverIdList().contains(user.getUser_id())) {
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

        int saveButtonId = View.generateViewId();
        Dialog dialog_changeCostCalculation = CustomDialog.Builder(getContext())
                .setTitle("Kostenberechnung ändern")
                .setView(R.layout.dialog_change_cost_calculation)
                .setButtonType(CustomDialog.ButtonType.SAVE_CANCEL)
                .addButton(CustomDialog.SAVE_BUTTON, dialog -> {
                    if (!Utility.isOnline(getContext()))
                        return;

                    RadioGroup dialogChangeCostCalculation_typeGroup = dialog.findViewById(R.id.dialogChangeCostCalculation_typeGroup);
                    RadioGroup dialogChangeCostCalculation_methodGroup = dialog.findViewById(R.id.dialogChangeCostCalculation_methodGroup);
                    EditText dialogChangeCostCalculation_budget = dialog.findViewById(R.id.dialogChangeCostCalculation_budget);
                    CheckBox dialogChangeCostCalculation_perPerson = dialog.findViewById(R.id.dialogChangeCostCalculation_perPerson);
                    EditText dialogChangeCostCalculation_kilometerAllowance = dialog.findViewById(R.id.dialogChangeCostCalculation_kilometerAllowance);

                    switch (dialogChangeCostCalculation_typeGroup.getCheckedRadioButtonId()) {
                        case R.id.dialogChangeCostCalculation_budgetRadio:
                            database.groupsMap.get(thisGroup_Id).setCalculationType(Group.costCalculationType.BUDGET);
                            break;
                        case R.id.dialogChangeCostCalculation_costRadio:
                            database.groupsMap.get(thisGroup_Id).setCalculationType(Group.costCalculationType.COST);
                            break;
                    }
                    switch (dialogChangeCostCalculation_methodGroup.getCheckedRadioButtonId()) {
                        case R.id.dialogChangeCostCalculation_realCostRadio:
                            database.groupsMap.get(thisGroup_Id).setCalculationMethod(Group.costCalculationMethod.ACTUAL_COST);
                            break;
                        case R.id.dialogChangeCostCalculation_kilometerAllowanceRadio:
                            database.groupsMap.get(thisGroup_Id).setCalculationMethod(Group.costCalculationMethod.KIKOMETER_ALLOWANCE);
                            break;
                        case R.id.dialogChangeCostCalculation_tripRadio:
                            database.groupsMap.get(thisGroup_Id).setCalculationMethod(Group.costCalculationMethod.TRIP);
                            break;
                    }
                    if (dialogChangeCostCalculation_budget.isEnabled()) {
                        if (!dialogChangeCostCalculation_budget.getText().toString().equals("")) {
                            database.groupsMap.get(thisGroup_Id).setBudget(Double.parseDouble(dialogChangeCostCalculation_budget.getText().toString()));
                        } else {
                            Toast.makeText(getContext(), "Ein Budget angeben", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    if (dialogChangeCostCalculation_kilometerAllowance.isEnabled()) {
                        if (!dialogChangeCostCalculation_kilometerAllowance.getText().toString().equals("")) {
                            database.groupsMap.get(thisGroup_Id).setKilometerAllowance(Double.parseDouble(dialogChangeCostCalculation_kilometerAllowance.getText().toString()));
                        } else {
                            Toast.makeText(getContext(), "Eine Pauschale angeben", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    database.groupsMap.get(thisGroup_Id).setBudgetPerUser(dialogChangeCostCalculation_perPerson.isChecked());
                    setCalculationTexts();
                    databaseReference.child("Groups").child(database.groupsMap.get(thisGroup_Id).getGroup_id()).setValue(database.groupsMap.get(thisGroup_Id));
                    dialog.dismiss();

                }, saveButtonId, false)
                .show();

        final RadioGroup dialogChangeCostCalculation_typeGroup = dialog_changeCostCalculation.findViewById(R.id.dialogChangeCostCalculation_typeGroup);
        final RadioGroup dialogChangeCostCalculation_methodGroup = dialog_changeCostCalculation.findViewById(R.id.dialogChangeCostCalculation_methodGroup);
        final EditText dialogChangeCostCalculation_budget = dialog_changeCostCalculation.findViewById(R.id.dialogChangeCostCalculation_budget);
        final CheckBox dialogChangeCostCalculation_perPerson = dialog_changeCostCalculation.findViewById(R.id.dialogChangeCostCalculation_perPerson);
        final EditText dialogChangeCostCalculation_kilometerAllowance = dialog_changeCostCalculation.findViewById(R.id.dialogChangeCostCalculation_kilometerAllowance);

        setChangeCostListener(dialog_changeCostCalculation, dialogChangeCostCalculation_typeGroup, dialogChangeCostCalculation_methodGroup, saveButtonId);

        switch (database.groupsMap.get(thisGroup_Id).getCalculationType()) {
            default:
            case COST:
                dialogChangeCostCalculation_typeGroup.check(R.id.dialogChangeCostCalculation_costRadio);
                break;
            case BUDGET:
                dialogChangeCostCalculation_typeGroup.check(R.id.dialogChangeCostCalculation_budgetRadio);
                break;
        }
        switch (database.groupsMap.get(thisGroup_Id).getCalculationMethod()) {
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
        dialogChangeCostCalculation_budget.setText(String.valueOf(database.groupsMap.get(thisGroup_Id).getBudget()));
        dialogChangeCostCalculation_perPerson.setChecked(database.groupsMap.get(thisGroup_Id).isBudgetPerUser());
        dialogChangeCostCalculation_kilometerAllowance.setText(String.valueOf(database.groupsMap.get(thisGroup_Id).getKilometerAllowance()));
    }

    private void setChangeCostListener(final Dialog dialog_changeCostCalculation, RadioGroup dialogChangeCostCalculation_typeGroup, RadioGroup dialogChangeCostCalculation_methodGroup, int saveButtonId) {
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
                            dialog_changeCostCalculation.findViewById(saveButtonId).setEnabled(false);
                        }
                        break;
                }
            }
        });
        dialogChangeCostCalculation_methodGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                dialog_changeCostCalculation.findViewById(saveButtonId).setEnabled(true);

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
        final Dialog dialog_costCalculation = CustomDialog.Builder(getContext())
                .setTitle("Kosten-Kalkulation")
                .setView(R.layout.dialog_calculate_costs)
                .show();

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


            if (database.groupsMap.get(thisGroup_Id).getCalculationType() == Group.costCalculationType.BUDGET) {
                if (database.groupsMap.get(thisGroup_Id).getCalculationMethod() != Group.costCalculationMethod.TRIP) {
                    costList_methodLabel.setText("Kosten:");

                    double thisCost = calculateUserTripCost(thisUser.getUser_id());
                    costList_tripsOrCost.setText(convertToEuro(thisCost) + "€");

                    progressBar.setMax((float) allCost);
                    progressBar.setProgress((float) thisCost);
                    setColorBasedOnRatio(progressBar, thisCost, allCost, database.groupsMap.get(thisGroup_Id).getDriverIdList().size(), colorMargin, thisUser);

                    tripList_percentage.setText((int) (thisCost / allCost * 100) + "%");
                    costList_budgetShare.setText(convertToEuro(thisCost / allCost *
                            (database.groupsMap.get(thisGroup_Id).getBudget() * (database.groupsMap.get(thisGroup_Id).isBudgetPerUser() ? database.groupsMap.get(thisGroup_Id).getUserIdList().size() : 1)
                            )) + "€");
                    // ToDo: check ob weniger als budget - ???
                } else {
                    String drivenAmorunt = calculateDrivenAmount(thisUser.getUser_id());

                    costList_tripsOrCost.setText(String.valueOf(drivenAmorunt));

                    double percentage = Double.valueOf(drivenAmorunt) / allTripCount * 100;

                    tripList_percentage.setText((int) percentage + "%");
                    costList_budgetShare.setText(convertToEuro(Double.valueOf(drivenAmorunt) / allTripCount *
                            (database.groupsMap.get(thisGroup_Id).getBudget() * (database.groupsMap.get(thisGroup_Id).isBudgetPerUser() ? database.groupsMap.get(thisGroup_Id).getUserIdList().size() : 1)
                            )) + "€");

                    progressBar.setMax((float) allTripCount);
                    progressBar.setProgress(Float.valueOf(drivenAmorunt));
                    setColorBasedOnRatio(progressBar, Double.valueOf(drivenAmorunt), allTripCount, database.groupsMap.get(thisGroup_Id).getDriverIdList().size(), colorMargin, thisUser);
                }
            } else if (database.groupsMap.get(thisGroup_Id).getCalculationType() == Group.costCalculationType.COST) {
                costList_methodLabel.setText("Kosten:");
                costList_share.setText("Anteil an Kosten:");

                double thisCost = calculateUserTripCost(thisUser.getUser_id());
                costList_tripsOrCost.setText(convertToEuro(thisCost) + "€");

                progressBar.setMax((float) allCost);
                progressBar.setProgress((float) thisCost);
                setColorBasedOnRatio(progressBar, thisCost, allCost, database.groupsMap.get(thisGroup_Id).getDriverIdList().size(), colorMargin, thisUser);

                tripList_percentage.setText((int) (thisCost / allCost * 100) + "%");

                costList_budgetShare.setText(convertToEuro(allCost / database.groupsMap.get(thisGroup_Id).getUserIdList().size()) + "€");

                double costDifference = thisCost - (allCost / database.groupsMap.get(thisGroup_Id).getUserIdList().size());

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

    }

    private void showTripList(boolean showAll) {
        // ToDo: trips werden doppelt angezeigt
        dialog_tripList = CustomDialog.Builder(getContext())
                .setTitle((showAll ? "Alle" : "Deine") + "Trips")
//                .setText("Das sind " + (showAll ? "alle" : "deine") + " Trips")
                .setView(R.layout.dialog_trip_list)
                .setButtonType(CustomDialog.ButtonType.BACK)
                .show();

        dialogTripList_list = dialog_tripList.findViewById(R.id.dialogTripList_list);

        dialogTripList_list.setOnItemClickListener((adapterView, view, i, l) -> {

            Dialog dialog_deleteTrip = CustomDialog.Builder(getContext())
                    .setTitle("Den Trip Löschen?")
                    .setView(R.layout.dialog_delete_trip)
                    .setButtonType(CustomDialog.ButtonType.YES_NO)
                    .addButton(CustomDialog.YES_BUTTON, dialog -> {
                        Trip trip = tripList.get(i);

                        database.groupsMap.get(thisGroup_Id).getTripIdList().remove(trip.getTrip_id());
                        database.groupTripMap.get(thisGroup_Id).remove(trip.getTrip_id());
                        databaseReference.child("Groups").child(database.groupsMap.get(thisGroup_Id).getGroup_id()).child("tripIdList").setValue(database.groupsMap.get(thisGroup_Id).getTripIdList());
                        databaseReference.child("Trips").child(database.groupsMap.get(thisGroup_Id).getGroup_id()).child(trip.getTrip_id()).removeValue();

                        tripListLaden(showAll);
                        reLoadContent();
                        thisGroupCalender.setData(thisGroup_Id);
                        thisGroupCalender.reLoadContent();
                    })
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
        for (String tripId : database.groupsMap.get(thisGroup_Id).getTripIdList()) {
            Trip trip = database.groupTripMap.get(thisGroup_Id).get(tripId);
            if (!showAll && !trip.getDriverId().equals(database.loggedInUser.getUser_id()))
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
                    "- " + database.groupPassengerMap.get(trip.getDriverId()).getUserName() : "");
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
                    if (database.groupsMap.get(thisGroup_Id).getCalculationMethod() == Group.costCalculationMethod.ACTUAL_COST)
                        count += trip.getCost();
                    else
                        count += Double.valueOf(trip.getDistance().split(" ")[0].replace(",", ".")) * database.groupsMap.get(thisGroup_Id).getKilometerAllowance();
                }
            }
            return count;
        }
        if (userTripMap.get(userId) == null)
            return count;

        for (Trip trip : userTripMap.get(userId).values()) {
            if (database.groupsMap.get(thisGroup_Id).getCalculationMethod() == Group.costCalculationMethod.ACTUAL_COST)
                count += trip.getCost();
            else
                count += Double.valueOf(trip.getDistance().split(" ")[0].replace(",", ".")) * database.groupsMap.get(thisGroup_Id).getKilometerAllowance();
        }
        return count;
    }

    String calculateDrivenAmount(String userId) {
        double count = 0;
        if (database.loggedInUser.getUser_id().equals(userId))
            userTripList.clear();
        for (Map.Entry<String, Trip> entry : database.groupTripMap.get(thisGroup_Id).entrySet()) {
            if (entry.getValue().getDriverId().equals(userId) || userId == null) {
                if (database.loggedInUser.getUser_id().equals(userId))
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
        for (String userId : database.groupsMap.get(thisGroup_Id).getUserIdList()) {
            sortedUserList.add(database.groupPassengerMap.get(userId));
        }
        Collections.sort(sortedUserList, new Comparator() {
            public int compare(User obj1, User obj2) {
                return obj1.getUserName().compareTo(obj2.getUserName());
            }

            public int compare(Object var1, Object var2) {
                return this.compare((User) var1, (User) var2);
            }
        });


        CustomRecycler.Builder(getContext(), userList)
                .setItemView(R.layout.list_item_passenger)
                .setObjectList(sortedUserList)
                .setViewList(viewIdList -> {
                    viewIdList.add(R.id.userList_name);
                    viewIdList.add(R.id.userList_image);
                    viewIdList.add(R.id.userList_ownAmount);
                    viewIdList.add(R.id.userList_color);
                    return viewIdList;
                })
                .setSetItemContent((viewHolder, ViewIdMap, object) -> {
                    User user = (User) object;

                    ((TextView) ViewIdMap.get(R.id.userList_name)).setText(user.getUserName());
                    ((ImageView) ViewIdMap.get(R.id.userList_image)).setImageResource(
                            database.groupsMap.get(thisGroup_Id).getDriverIdList().contains(user.getUser_id()) ?
                                    R.drawable.ic_lenkrad : R.drawable.ic_leer);
                    ((TextView) ViewIdMap.get(R.id.userList_ownAmount)).setText(calculateDrivenAmount(user.getUser_id()));
                    ((TextView) ViewIdMap.get(R.id.userList_color)).setTextColor(Color.parseColor(user.getUserColor()));
                })
                .addSubOnClickListener(R.id.userList_image, (recycler, view1, object, index) ->
                        Toast.makeText(getContext(), "Image: " + ((User) object).getUserName(), Toast.LENGTH_SHORT).show())
                .addSubOnClickListener(R.id.userList_color, (recycler, view1, object, index) ->
                        Toast.makeText(getContext(), "Dot: " + ((User) object).getUserName(), Toast.LENGTH_SHORT).show(),
                        false)
                .generate();

        // ToDo: fahrten nach anteil farblich markieren
    }
}

class  ViewPager_GroupCalender extends Fragment {

    View view;
    String thisGroup_Id;
    CompactCalendarView calendarView;
    TextView calender_month;
    ImageView calender_previousMonth;
    ImageView calender_nextMonth;
    TextView calender_noTrips;
    LinearLayout calender_tripList_Layout;


    Database database = Database.getInstance();
    Database.OnChangeListener onGroupChangeListener;

    List<String> listviewTitle = new ArrayList<String>();
    List<Boolean> listviewIsDriver = new ArrayList<>();
    List<java.io.Serializable> listviewOwnDrivenAmount = new ArrayList<>();
    List<User> sortedUserList = new ArrayList<>();
    List<Trip> userTripList = new ArrayList<>();
    Map<String, Map<String, Trip>> userTripMap = new HashMap<>();


    public void setData(String thisGroup_Id) {
        this.thisGroup_Id = thisGroup_Id;
    }

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

        onGroupChangeListener = database.addOnGroupChangeListener(() -> reLoadContent());
        return view;
    }

    public void reLoadContent() {
        loadCalender();
    }

    public void removeListeners() {
        database.removeOnGroupChangeListener(onGroupChangeListener);
    }

    private void loadCalender() {
        // Set first day of week to Monday, defaults to Monday so calling setFirstDayOfWeek is not necessary
        // Use constants provided by Java Calendar class
        calendarView.removeAllEvents();
        calendarView.setFirstDayOfWeek(Calendar.MONDAY);
        calendarView.shouldSelectFirstDayOfMonthOnScroll(false);
//        calendarView.displayOtherMonthDays(true);

        for (String tripId : database.groupsMap.get(thisGroup_Id).getTripIdList()) {
            Trip trip = database.groupTripMap.get(thisGroup_Id).get(tripId);
            Event ev1 = new Event(Color.parseColor(database.groupPassengerMap.get(trip.getDriverId()).getUserColor())
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
            Trip trip = database.groupTripMap.get(thisGroup_Id).get(event.getData().toString());
            User user = database.groupPassengerMap.get(trip.getDriverId());
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