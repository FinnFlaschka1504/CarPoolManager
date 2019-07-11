package finn_daniel.carpoolmanager;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;

public class Settings extends AppCompatActivity {

    DatabaseReference databaseReference;
    Boolean isReadingActivated = false;
    SharedPreferences mySPR;


    Spinner settings_spinnerStandardView;
    Spinner settings_spinnerTripCount;


    ValueEventListener postListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if (dataSnapshot.getValue() == null)
                return;
            testDatenAuslesen((Map<String, Object>) dataSnapshot.getValue());
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        settings_spinnerStandardView = findViewById(R.id.settings_spinnerStandardView);
        settings_spinnerTripCount = findViewById(R.id.settings_spinnerTripCount);
        mySPR = getSharedPreferences("CarPoolManager_Settings",0);
        mySPR.getString("standardView", "Übersicht");
        databaseReference = FirebaseDatabase.getInstance().getReference();
        setSpinners();
    }

    private void setSpinners() {
        settings_spinnerStandardView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                SharedPreferences.Editor editor = mySPR.edit();
                editor.putString("standardView",((AppCompatTextView) view).getText().toString());
                editor.commit();
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });
        settings_spinnerStandardView.setSelection(mySPR.getString("standardView", "Übersicht").equals("Übersicht") ? 0 : 1);

        settings_spinnerTripCount.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                SharedPreferences.Editor editor = mySPR.edit();
                editor.putString("tripCount",((AppCompatTextView) view).getText().toString());
                editor.commit();

            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });
        settings_spinnerTripCount.setSelection(mySPR.getString("tripCount", "Pro Weg").equals("Pro Weg") ? 0 : 1);

    }

    public void firebaseAddText(View view) {
        EditText editText = findViewById(R.id.editText_firebase_add);

        if (editText.getText().toString().equals("")) {
            Toast.makeText(Settings.this, "Erst einen Text eingeben!", Toast.LENGTH_SHORT).show();
            return;
        }
        FireBase_test newTest = new FireBase_test();
        newTest.setNachricht(editText.getText().toString());
        editText.setText("");

        databaseReference.child("Tests").child(newTest.test_id).setValue(newTest);

        Toast.makeText(Settings.this, "Text Hinzugefügt", Toast.LENGTH_SHORT).show();
    }

    public void firebaseReadText(View view) {

        Button settings_activateRead = (Button)findViewById(R.id.settings_activateRead);

        if (!isReadingActivated) {
            isReadingActivated = true;
            databaseReference.child("Tests").addValueEventListener(postListener);
            settings_activateRead.setText("Deaktivieren");
        }
        else {
            isReadingActivated = false;
            databaseReference.child("Tests").removeEventListener(postListener);
            settings_activateRead.setText("Aktivieren");
        }

    }

    private void testDatenAuslesen(Map<String,Object> users) {

        ArrayList<String> nachrichten = new ArrayList<>();

        //iterate through each user, ignoring their UID
        for (Map.Entry<String, Object> entry : users.entrySet()){

            //Get user map
            Map nachricht = (Map) entry.getValue();
            //Get phone field and append to list
            nachrichten.add((String) nachricht.get("nachricht"));
        }

        System.out.println(nachrichten.toString());
        Toast.makeText(Settings.this, nachrichten.toString(), Toast.LENGTH_SHORT).show();
    }
}
