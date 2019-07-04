package finn_daniel.carpoolmanager;

import java.util.UUID;

public class Trip {
    private String trip_id;

    public Trip() {
        trip_id = "trip_" + UUID.randomUUID().toString();
    }
}
