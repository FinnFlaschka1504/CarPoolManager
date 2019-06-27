package finn_daniel.carpoolmanager;

import java.util.UUID;

public class FireBase_test {
    String nachricht;
    String test_id;

    public FireBase_test() {
        test_id = "test_" + UUID.randomUUID().toString();
    }

    public String getNachricht() {
        return nachricht;
    }

    public void setNachricht(String nachricht) {
        this.nachricht = nachricht;
    }

    public String getTest_id() {
        return test_id;
    }

    public void setTest_id(String test_id) {
        this.test_id = test_id;
    }
}
