package finn_daniel.carpoolmanager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class User {
    private String user_id;
    private String userName;
    private List<String> groupIdList = new ArrayList<>();
    private List<String> carIdList = new ArrayList<>();

    public User() {
        user_id = "user_" + UUID.randomUUID().toString();
    }

    public void addGroup(String user_id) {
        groupIdList.add(user_id);
    }

    public void removeGroup(String user_id) {
        groupIdList.remove(user_id);
    }

    public void addCar(String car_id) {
        carIdList.add(car_id);
    }

    public void removeCar(String car_id) {
        carIdList.remove(car_id);
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public List<String> getGroupIdList() {
        return groupIdList;
    }

    public void setGroupIdList(List<String> groupIdList) {
        this.groupIdList = groupIdList;
    }

    public List<String> getCarIdList() {
        return carIdList;
    }

    public void setCarIdList(List<String> carIdList) {
        this.carIdList = carIdList;
    }
}
