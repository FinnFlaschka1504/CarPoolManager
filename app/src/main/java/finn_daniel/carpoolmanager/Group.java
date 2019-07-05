package finn_daniel.carpoolmanager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Group {
    private String group_id;
    private String name;
    private List<String> userIdList = new ArrayList<>();
    private List<String> tripIdList = new ArrayList<>();
    private List<String> driverIdList = new ArrayList<>();
    private List<String> bookmarkIdList = new ArrayList<>();
    public enum priceCalculationType
    {
        KIKOMETERALLOWANCE, BUDGET
    }

    public Group() {
        group_id = "group_" + UUID.randomUUID().toString();
    }

    List<List<String>> getChangedUserLists(Group oldVersion) {
        List<String> newList = new ArrayList<>(userIdList) ;
        List<String> oldList = oldVersion.getUserIdList();
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
}
