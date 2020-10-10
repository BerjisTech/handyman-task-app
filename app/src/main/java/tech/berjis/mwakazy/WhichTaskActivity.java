package tech.berjis.mwakazy;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class WhichTaskActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    DatabaseReference dbRef;
    String UID;

    List<Categories> categoriesList;
    CategoriesAdapter CategoriesAdapter;

    RecyclerView categoryRecycler;
    String category_name, location;
    SearchView searchCategory;
    ImageView profile, services, orders, settings;
    ConstraintLayout root_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_which_task);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        initVars();
    }

    private void initVars() {
        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.keepSynced(true);

        categoriesList = new ArrayList<>();

        categoryRecycler = findViewById(R.id.categoryRecycler);
        profile = findViewById(R.id.profile);
        orders = findViewById(R.id.orders);
        services = findViewById(R.id.services);
        settings = findViewById(R.id.settings);
        searchCategory = findViewById(R.id.searchCategory);
        root_view = findViewById(R.id.root_view);

        loadcategories();
        staticOnClicks();
        loaduserdata();
    }

    private void staticOnClicks() {

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(WhichTaskActivity.this, ProfileActivity.class));
            }
        });

        orders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(WhichTaskActivity.this, MyOrdersActivity.class));
            }
        });

        services.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(WhichTaskActivity.this, MyServicesActivity.class));
            }
        });

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(WhichTaskActivity.this, SettingsActivity.class));
            }
        });
    }

    private void search() {
        final Intent c_intent = new Intent(WhichTaskActivity.this, ByCategoryActivity.class);
        Bundle c_bundle = new Bundle();
        c_bundle.putString("category", category_name);
        c_intent.putExtras(c_bundle);

        final ImageView preload = findViewById(R.id.preload);
        preload.setVisibility(View.VISIBLE);

        Glide.with(WhichTaskActivity.this).asGif().load(R.drawable.preloader).into(preload);
        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        startActivity(c_intent);
                    }
                },
                500);
        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        preload.setVisibility(View.GONE);
                    }
                },
                1000);
    }


    private void loadcategories() {
        categoriesList.clear();
        categoriesList.add(new Categories("All Categories", "drawable://" + R.drawable.plus));
        categoryRecycler.setLayoutManager(new GridLayoutManager(this, 3, RecyclerView.VERTICAL, false));
        dbRef.child("Categories").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        Categories l = snap.getValue(Categories.class);
                        categoriesList.add(l);
                    }
                }
                CategoriesAdapter = new CategoriesAdapter(WhichTaskActivity.this, categoriesList, "categories");
                categoryRecycler.setAdapter(CategoriesAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loaduserdata() {
        UID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        dbRef.child("Users")
                .child(UID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String user_type = snapshot.child("user_type").getValue().toString();
                        if (snapshot.child("subscription").exists()) {
                            long today = System.currentTimeMillis() / 1000L;
                            long nextMonth = Long.parseLong(Objects.requireNonNull(snapshot.child("next_month").getValue()).toString());

                            if (today > nextMonth) {
                                Intent mainActivity = new Intent(getApplicationContext(), RenewSubscriptionActivity.class);
                                startActivity(mainActivity);
                                finish();
                            } else {
                                if (user_type.equals("tasker")) {
                                    services.setVisibility(View.VISIBLE);
                                }
                            }
                        } else {
                            Intent mainActivity = new Intent(getApplicationContext(), ChooseSubscriptionActivity.class);
                            startActivity(mainActivity);
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

}

