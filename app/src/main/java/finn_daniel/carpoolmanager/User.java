package finn_daniel.carpoolmanager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class User {
    String user_id;
    String userName;
    List<String> groupIdList = new ArrayList<>();


    public User() {
        user_id = "user_" + UUID.randomUUID().toString();
    }

    public void addGroup(String user_id) {
        groupIdList.add(user_id);
    }

    public void removeGroup(String user_id) {
        groupIdList.remove(user_id);
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
}
