package com.example.lab5_starter;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.DocumentReference;

import com.google.firebase.firestore.DocumentReference;

public class MainActivity extends AppCompatActivity implements CityDialogFragment.CityDialogListener {

    private Button addCityButton;
    private ListView cityListView;

    private ArrayList<City> cityArrayList;
    private ArrayAdapter<City> cityArrayAdapter;
    private FirebaseFirestore db;
    private CollectionReference citiesRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Set views
        addCityButton = findViewById(R.id.buttonAddCity);
        cityListView = findViewById(R.id.listviewCities);

        // create city array + adapter
        cityArrayList = new ArrayList<>();
        cityArrayAdapter = new CityArrayAdapter(this, cityArrayList);
        cityListView.setAdapter(cityArrayAdapter);

        // Firestore
        db = FirebaseFirestore.getInstance();
        citiesRef = db.collection("cities");

        // Listen to Firestore (loads list)
        citiesRef.addSnapshotListener((value, error) -> {
            if (error != null) return;

            cityArrayList.clear();
            if (value != null) {
                for (QueryDocumentSnapshot doc : value) {
                    String name = doc.getString("name");
                    String province = doc.getString("province");
                    cityArrayList.add(new City(name, province));
                }
            }
            cityArrayAdapter.notifyDataSetChanged();
        });

        // Long-press to delete (deletes from Firestore)
        cityListView.setOnItemLongClickListener((parent, view, position, id) -> {
            City cityToDelete = cityArrayAdapter.getItem(position);
            if (cityToDelete != null) {
                citiesRef.document(cityToDelete.getName()).delete();
            }
            return true;
        });

        // Add button
        addCityButton.setOnClickListener(view -> {
            CityDialogFragment cityDialogFragment = new CityDialogFragment();
            cityDialogFragment.show(getSupportFragmentManager(), "Add City");
        });

        // Tap to view/edit
        cityListView.setOnItemClickListener((adapterView, view, i, l) -> {
            City city = cityArrayAdapter.getItem(i);
            CityDialogFragment cityDialogFragment = CityDialogFragment.newInstance(city);
            cityDialogFragment.show(getSupportFragmentManager(), "City Details");
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }


    @Override
    public void updateCity(City city, String title, String year) {
        city.setName(title);
        city.setProvince(year);
        cityArrayAdapter.notifyDataSetChanged();

        // Updating the database using delete + addition
    }

    @Override
    public void addCity(City city) {
        DocumentReference docRef = citiesRef.document(city.getName());
        docRef.set(city);
    }


    public void addDummyData(){
        City m1 = new City("Edmonton", "AB");
        City m2 = new City("Vancouver", "BC");
        cityArrayList.add(m1);
        cityArrayList.add(m2);
        cityArrayAdapter.notifyDataSetChanged();
    }
}