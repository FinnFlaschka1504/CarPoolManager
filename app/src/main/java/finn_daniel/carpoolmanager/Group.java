package finn_daniel.carpoolmanager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Group {
    String group_id;
    String name;
    List<String> userIdList = new ArrayList<>();
    List<String> tripIdList = new ArrayList<>();
    List<String> driverIdList = new ArrayList<>();
    public enum priceCalculationType
    {
        KIKOMETERALLOWANCE, BUDGET
    }

    public Group() {
        group_id = "group_" + UUID.randomUUID().toString();
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
}
