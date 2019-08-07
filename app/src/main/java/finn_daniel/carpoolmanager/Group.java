package finn_daniel.carpoolmanager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class Group {
    private String group_id;
    private String name;
    private costCalculationType calculationType;
    private costCalculationMethod calculationMethod;
    private double budget;
    private boolean budgetPerUser;
    private double kilometerAllowance = 0.3;
    private List<String> userIdList = new ArrayList<>();
    private List<String> tripIdList = new ArrayList<>();
    private List<String> driverIdList = new ArrayList<>();
    private List<String> bookmarkIdList = new ArrayList<>();
    public enum costCalculationType
    {
        BUDGET, COST
    }
    public enum costCalculationMethod
    {
        ACTUAL_COST, KIKOMETER_ALLOWANCE, TRIP
    }

    public Group() {
        group_id = "group_" + UUID.randomUUID().toString();
    }

    List<List<String>> getChangedUserLists(Group oldVersion) {
        List<String> newList = new ArrayList<>(userIdList) ;
        List<String> oldList = new ArrayList<>(oldVersion.getUserIdList());
        List<String> newList_clone = new ArrayList<>(newList);
        for (String userId : newList_clone ) {
            if (oldList.contains(userId)) {
                newList.remove(userId);
                oldList.remove(userId);
            }
        }
        List<List<String>> returnList = new ArrayList<>();
        returnList.add(newList);
        returnList.add(oldList);
        return returnList;
    }

    List<List<String>> getChangedTripsLists(Set<String> pOldVersion) {
        List<List<String>> returnList = new ArrayList(Arrays.asList(null,null));
        Set<String> newVersion = new HashSet<>(tripIdList);
        Set<String> oldVersion = new HashSet<>(pOldVersion);

        if (newVersion.containsAll(oldVersion)) {
            newVersion.removeAll(oldVersion);
            returnList.set(0, new ArrayList<>(newVersion));
        } else {
            oldVersion.removeAll(newVersion);
            returnList.set(1, new ArrayList<>(oldVersion));
            // ToDo: wenn trip gelöscht
        }


//        returnList.add(newList);
//        returnList.add(oldList);
        return returnList;
    }

    public void addUser(String user_id) {
        userIdList.add(user_id);
    }

    public void removeUser(String user_id) {
        userIdList.remove(user_id);
    }

    public void addDriver(String user_id) {
        driverIdList.add(user_id);
    }

    public void removeDriver(String user_id) {
        driverIdList.remove(user_id);
    }

    public String getGroup_id() {
        return group_id;
    }

    public void setGroup_id(String group_id) {
        this.group_id = group_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getUserIdList() {
        return userIdList;
    }

    public void setUserIdList(List<String> userIdList) {
        this.userIdList = userIdList;
    }

    public List<String> getTripIdList() {
        return tripIdList;
    }

    public void setTripIdList(List<String> tripIdList) {
        this.tripIdList = tripIdList;
    }

    public List<String> getDriverIdList() {
        return driverIdList;
    }

    public void setDriverIdList(List<String> driverIdList) {
        this.driverIdList = driverIdList;
    }

    public List<String> getBookmarkIdList() {
        return bookmarkIdList;
    }

    public void setBookmarkIdList(List<String> bookmarkIdList) {
        this.bookmarkIdList = bookmarkIdList;
    }

    public costCalculationType getCalculationType() {
        return calculationType;
    }

    public void setCalculationType(costCalculationType calculationType) {
        this.calculationType = calculationType;
    }

    public costCalculationMethod getCalculationMethod() {
        return calculationMethod;
    }

    public void setCalculationMethod(costCalculationMethod calculationMethod) {
        this.calculationMethod = calculationMethod;
    }

    public double getBudget() {
        return budget;
    }

    public void setBudget(double budget) {
        this.budget = budget;
    }

    public boolean isBudgetPerUser() {
        return budgetPerUser;
    }

    public void setBudgetPerUser(boolean budgetPerUser) {
        this.budgetPerUser = budgetPerUser;
    }

    public double getKilometerAllowance() {
        return kilometerAllowance;
    }

    public void setKilometerAllowance(double kilometerAllowance) {
        this.kilometerAllowance = kilometerAllowance;
    }
}
