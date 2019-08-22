package finn_daniel.carpoolmanager;

public class Database {
    // ToDo: supress updates
    public static final String SUCCSESS = "SUCCSESS";
    public static final String FAILED = "FAILED";
    public static final String GROUPS = "Groups";
    public static final String TRIPS = "Trips";
    public static final String USERS = "Users";
    private static Database database = new Database();

    public static final Database getInstance() {
        return database;
    }

    public String updateGroup(Group group) {
        return FAILED;
    }
}
