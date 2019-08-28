package finn_daniel.carpoolmanager;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Database {
    public static final String SUCCSESS = "SUCCSESS";
    public static final String FAILED = "FAILED";
    public static final String GROUPS = "Groups";
    public static final String TRIPS = "Trips";
    public static final String USERS = "Users";
    public static final String CARS = "Cars";

    private static Database database;
    private static DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    private OnInstanceFinishedLoading onInstanceFinishedLoading;

    public User loggedInUser;
    public String loggedInUser_Id;
    public Map<String, Group> groupsMap = new HashMap<>();
    public Map<String, User> groupPassengerMap = new HashMap<>();
    public Map<String, Map<String, Trip>> groupTripMap = new HashMap<>();

    private List<OnChangeListener> onGroupChangeListenerList = new ArrayList<>();
    public Map<String, Boolean> hasGroupChangeListener = new HashMap<>();


    public static final Database getInstance() {
        return database;
    }

    public static final Database getInstance(String userId, OnInstanceFinishedLoading onInstanceFinishedLoading) {
        return new Database(userId, onInstanceFinishedLoading);
    }

    private Database(String loggedInUser_Id, OnInstanceFinishedLoading onInstanceFinishedLoading) {
        Database.database = Database.this;
        this.loggedInUser_Id = loggedInUser_Id;
        this.onInstanceFinishedLoading = onInstanceFinishedLoading;
        reloadLoggedInUser();
    }
    public Database(String loggedInUser_Id) {
        Database.database = Database.this;
        this.loggedInUser_Id = loggedInUser_Id;
    }

//  ----- Get data from database ----->
    public interface OnInstanceFinishedLoading {
        void onFinishedLoading(Database database);
    }

    private void reloadLoggedInUser() {
        Database.databaseCall_read(Arrays.asList(Database.USERS, loggedInUser_Id), dataSnapshot -> {
            if (dataSnapshot.getValue() == null)
                return;
            loggedInUser = Database.getFromData_user(dataSnapshot);
            getGroupsfromUser();
        });
    }

    private void getGroupsfromUser() {
        List<String> loggedInUser_groupsIdList = loggedInUser.getGroupIdList();
        if (loggedInUser_groupsIdList.size() == 0) {
            return;
        }
        for (String groupId : loggedInUser_groupsIdList) {
            Database.databaseCall_read(Arrays.asList(Database.GROUPS, groupId), dataSnapshot -> {
                if (dataSnapshot.getValue() == null) {
                    return;
                }
                Group foundGroup = Database.getFromData_group(dataSnapshot);
                groupsMap.put(foundGroup.getGroup_id(), foundGroup);
                if (groupsMap.size() == loggedInUser_groupsIdList.size()) {
                    getGroupPassengers();
                }
            });
            hasGroupChangeListener.put(groupId, false);
            addOnGroupChangeListener_database(groupId);
        }
    }

    private void getGroupPassengers() {
        final int[] loggedInUser_passengerCount = {0};
        groupsMap.values().forEach(group -> loggedInUser_passengerCount[0] += group.getUserIdList().size());

        for (Group group : groupsMap.values()) {
            for (String user : group.getUserIdList()) {
                Database.databaseCall_read(Arrays.asList(Database.USERS, user), dataSnapshot -> {
                    if (dataSnapshot.getValue() == null)
                        return;
                    User foundUser = Database.getFromData_user(dataSnapshot);
                    groupPassengerMap.put(foundUser.getUser_id(), foundUser);
                    loggedInUser_passengerCount[0]--;
                    if (loggedInUser_passengerCount[0] == 0) {
                        getGroupTrips();
                    }
                });
            }
        }
    }

    private void getGroupTrips() {
        groupTripMap.clear();
        for (final String groupId : loggedInUser.getGroupIdList()) {
            if (groupsMap.get(groupId).getTripIdList().size() == 0) {
                groupTripMap.put(groupId, new HashMap<>());
                if (groupTripMap.size() >= loggedInUser.getGroupIdList().size()) {
                    onInstanceFinishedLoading.onFinishedLoading(Database.this); // --> fertig
                    return;
                }
                else
                    continue;
            }
            Database.databaseCall_read(Arrays.asList(Database.TRIPS, groupId), dataSnapshot -> {
                if (dataSnapshot.getValue() == null)
                    return;
                groupTripMap.put(groupId, Database.getFromData_tripMap(dataSnapshot));
                if (groupTripMap.size() >= loggedInUser.getGroupIdList().size()) {
                    onInstanceFinishedLoading.onFinishedLoading(Database.this); // --> fertig
                }
            });
        }
    }
//  <----- Get data from database -----


//  ----- Get data from datSnapshot ----->
    private static User getFromData_user(DataSnapshot dataSnapshot) {
    return dataSnapshot.getValue(User.class);
}

    private static Group getFromData_group(DataSnapshot dataSnapshot) {
        return dataSnapshot.getValue(Group.class);
    }

    private static Map<String, Trip> getFromData_tripMap(DataSnapshot dataSnapshot) {
        Map<String, Trip> newMap = new HashMap<>();
        for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
            Trip foundTrip = messageSnapshot.getValue(Trip.class);
            newMap.put(foundTrip.getTrip_id(), foundTrip);
        }
        return newMap;
    }
//  <----- Get data from datSnapshot -----


//  ----- Database Call ----->
    interface OnDatabaseCallFinished {
        void onFinished(DataSnapshot dataSnapshot);
    }

    interface OnDatabaseCallFailed {
        void onFailed(DatabaseError databaseError);
    }

    public static void databaseCall_read(List<String> stepList, OnDatabaseCallFinished onDatabaseCallFinished){
        accessChilds(databaseReference, stepList).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                onDatabaseCallFinished.onFinished(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                String test = null;
            }
        });
    }

    public static void databaseCall_read(List<String> stepList, OnDatabaseCallFinished onDatabaseCallFinished,
                                         OnDatabaseCallFailed onDatabaseCallFailed){
        accessChilds(databaseReference, stepList).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                onDatabaseCallFinished.onFinished(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                onDatabaseCallFailed.onFailed(databaseError);
            }
        });
    }

    public static void databaseCall_write(List<String> stepList, Object object){
        accessChilds(databaseReference, stepList).setValue(object);
    }

    public static void databaseCall_delete(List<String> stepList){
        accessChilds(databaseReference, stepList).removeValue();
    }

    public static DatabaseReference accessChilds(DatabaseReference databaseReference, List<String> steps) {
        List<String> newSteps = new ArrayList<>(steps);
        if (newSteps.size() > 0) {
            DatabaseReference newDatabaseReference = databaseReference.child(newSteps.remove(0));
            return accessChilds(newDatabaseReference, newSteps);
        }
        return databaseReference;
    }

    public static Database.OnDatabaseCallFailed getStandardFail(Context context) {
        return databaseError -> Toast.makeText(context, "Datenbankabfrage gescheitert", Toast.LENGTH_SHORT).show();
    }
//  <----- Database Call -----


//  <----- Updater -----
    public static void updateGroup(Group group) {
        databaseCall_write(Arrays.asList(GROUPS, group.getGroup_id()), group);
    }

    public static void updateUser(User user) {
        databaseCall_write(Arrays.asList(USERS, user.getUser_id()), user);
    }

    public static void removeTrip(Group group, Trip trip) {
        databaseCall_delete(Arrays.asList(GROUPS, group.getGroup_id(), trip.getTrip_id()));
    }

    public static void removeCar(User user, Car car) {
        databaseCall_delete(Arrays.asList(CARS, user.getUser_id(), car.getCar_id()));
    }
//  ----- Updater ----->


//  ----- Change Listener ----->
    // ToDo: onLoggedInUserChange
    interface OnChangeListener {
        void onChangeListener();
    }

    private void fireOnGroupChangeListeners(){
            onGroupChangeListenerList.forEach(OnChangeListener::onChangeListener);
    }
    public OnChangeListener addOnGroupChangeListener(OnChangeListener onChangeListener) {
        onGroupChangeListenerList.add(onChangeListener);
        return onChangeListener;
    }
    public boolean removeOnGroupChangeListener(OnChangeListener onChangeListener) {
        return onGroupChangeListenerList.remove(onChangeListener);
    }
    public void addOnGroupChangeListener_database(final String groupId) {
        databaseReference.child(Database.GROUPS).child(groupId).addValueEventListener(onGroupChangeListener);
    }
    private ValueEventListener onGroupChangeListener = new ValueEventListener() {
    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        if (dataSnapshot.getValue() == null) {
            final String removedGroup = dataSnapshot.getKey();
//                for (String user : groupsMap.get(removedGroup).getUserIdList()) {
//                    databaseReference.child("Users").child(user).addListenerForSingleValueEvent(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(DataSnapshot dataSnapshot) {
//                            if (dataSnapshot.getValue() == null)
//                                return;
//                            User foundUser = dataSnapshot.getValue(User.class);
//                            foundUser.getGroupIdList().remove(removedGroup);
//                            databaseReference.child("Users").child(foundUser.getUser_id()).setValue(foundUser);
//                        }
//
//                        @Override
//                        public void onCancelled(DatabaseError databaseError) {
//                        }
//                    });
//                }

            groupsMap.remove(removedGroup);
            fireOnGroupChangeListeners();
            return;
        }
        Group foundGroup = dataSnapshot.getValue(Group.class);
        if (!hasGroupChangeListener.get(foundGroup.getGroup_id())) {  // neue Gruppe?
            hasGroupChangeListener.replace(foundGroup.getGroup_id(), true);
            return;
        }

//            Findet heraus, ob ein Nutzer eingetreten, oder ausgetreten ist
        if (!foundGroup.getUserIdList().equals(groupsMap.get(foundGroup.getGroup_id()).getUserIdList())) {
            onChangedUsers(foundGroup);
            return;
        }

//            Findet heraus, ob sich bei den Lesezeichen was verändert hat
        if (!foundGroup.getBookmarkList().equals(groupsMap.get(foundGroup.getGroup_id()).getBookmarkList()))
            groupsMap.get(foundGroup.getGroup_id()).setBookmarkList(foundGroup.getBookmarkList());


//            Findet heraus, ob sich bei den Trips was verändert hat
        if (!foundGroup.getTripIdList().equals(groupsMap.get(foundGroup.getGroup_id()).getTripIdList())) {
            onChangedTrip(foundGroup);
            return;
        }

        groupsMap.replace(foundGroup.getGroup_id(), foundGroup); // Gruppe wird aktuallisiert

        fireOnGroupChangeListeners();
        // ToDo: group trip map updaten

    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
    }

    void onChangedTrip(final Group foundGroup) {
        final List<List<String>> changeList = foundGroup.getChangedTripsLists(groupTripMap.get(foundGroup.getGroup_id()).keySet());

        if (changeList.get(0) != null) {
            for (String trip : new ArrayList<>(changeList.get(0))) {
                databaseReference.child("Trips").child(foundGroup.getGroup_id()).child(trip).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue() == null)
                            return;
                        Trip foundTrip = dataSnapshot.getValue(Trip.class);
                        changeList.get(0).remove(foundTrip.getTrip_id());
                        groupTripMap.get(foundGroup.getGroup_id()).put(foundTrip.getTrip_id(), foundTrip);
                        groupsMap.get(foundGroup.getGroup_id()).getTripIdList().add(foundTrip.getTrip_id());
//                        groupPassengerMap.put(foundTrip.getUser_id(), foundTrip);
                        if (changeList.get(0).size() <= 0)
                            fireOnGroupChangeListeners();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            }
        }
        if (changeList.get(1) != null) {
            for (String tripId : changeList.get(1)) {
                groupTripMap.get(foundGroup.getGroup_id()).remove(tripId);
                groupsMap.get(foundGroup.getGroup_id()).getTripIdList().remove(tripId);
            }
            fireOnGroupChangeListeners();
        }
    }
    void onChangedUsers(Group foundGroup) {
        final List<List<String>> changeList = foundGroup.getChangedUserLists(groupsMap.get(foundGroup.getGroup_id()));

        for (String user : changeList.get(1)) {
            groupPassengerMap.remove(user);
        }

        for (String user : new ArrayList<>(changeList.get(0))) {
            changeList.get(0).remove(user);
            databaseReference.child("Users").child(user).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() == null)
                        return;
                    User foundUser = dataSnapshot.getValue(User.class);
                    groupPassengerMap.put(foundUser.getUser_id(), foundUser);
                    groupsMap.get(foundGroup.getGroup_id()).getUserIdList().add(foundUser.getUser_id());
                    if (changeList.get(0).size() <= 0)
                        fireOnGroupChangeListeners();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }
    }
};
//  <----- Change Listener -----

}
