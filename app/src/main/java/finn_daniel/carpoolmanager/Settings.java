package finn_daniel.carpoolmanager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Settings extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    DatabaseReference databaseReference;
    Boolean isReadingActivated = false;

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
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(this);
        SharedPreferences mySPR = getSharedPreferences("Settings",0);
        mySPR.getString("standardView", "Übersicht");
        spinner.setSelection(mySPR.getString("standardView", "Übersicht").equals("Übersicht") ? 0 : 1);

        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        SharedPreferences mySPR = getSharedPreferences("Settings", 0);
        SharedPreferences.Editor editor = mySPR.edit();

        editor.putString("standardView",((AppCompatTextView) view).getText().toString());
        editor.commit();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
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
