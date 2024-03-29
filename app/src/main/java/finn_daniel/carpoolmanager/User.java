package finn_daniel.carpoolmanager;

import android.graphics.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

public class User {
    private String user_id;
    private String userName;
    private List<String> groupIdList = new ArrayList<>();
    private List<String> carIdList = new ArrayList<>();
    private String userColor;
    private String emailAddress;

    public User() {
        user_id = "user_" + UUID.randomUUID().toString();
        Random random = new Random();

        userColor = "#" + Integer.toHexString(Color.argb(255, random.nextInt(256) - 100, random.nextInt(256) - 100, random.nextInt(256) - 100)).toUpperCase();
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

    // ToDo: Fahben abhängig von der Gruppe machen (userColorMap)
    public String getUserColor() {
        return userColor;
    }

    public void setUserColor(String userColor) {
        this.userColor = userColor;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return user_id.equals(user.user_id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user_id);
    }
}
