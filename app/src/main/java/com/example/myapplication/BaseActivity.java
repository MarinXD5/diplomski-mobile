package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

public abstract class BaseActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    protected DrawerLayout drawerLayout;
    protected NavigationView navigationView;
    protected Toolbar toolbar;

    Button loginButton, logoutButton;

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);
        loginButton = findViewById(R.id.loginRedirectButton);
        logoutButton = findViewById(R.id.logoutButton);
        mAuth = FirebaseAuth.getInstance();

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);

        loginButton.setOnClickListener(view -> startActivity(new Intent(BaseActivity.this, LoginActivity.class)));

        logoutButton.setOnClickListener(view ->{
            mAuth.signOut();
            this.checkLoginStatus();
        });
        toggle.syncState();
        checkLoginStatus();
    }

    private void checkLoginStatus() {
        if(mAuth.getCurrentUser() != null){
            loginButton.setVisibility(View.GONE);
            logoutButton.setVisibility(View.VISIBLE);
            navigationView.getMenu().findItem(R.id.nav_account).setVisible(true);
        } else {
            logoutButton.setVisibility(View.GONE);
            loginButton.setVisibility(View.VISIBLE);
            navigationView.getMenu().findItem(R.id.nav_account).setVisible(false);
        }
    }

    protected void setChildLayout(int layoutResID) {
        FrameLayout contentFrame = findViewById(R.id.content_frame);
        View childView = getLayoutInflater().inflate(layoutResID, contentFrame, false);
        contentFrame.addView(childView);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        Intent intent = null;
        if (id == R.id.nav_home) {
            if (!(this instanceof MainActivity)) {
                intent = new Intent(this, MainActivity.class);
            }
        } else if (id == R.id.nav_settings) {
            intent = new Intent(this, SettingsActivity.class);
        } else if (id == R.id.nav_templates) {
            intent = new Intent(this, TemplatesActivity.class);
        } else if (id == R.id.nav_liked) {
            intent = new Intent(this, LikedActivity.class);
        } else if (id == R.id.nav_marketplace) {
            intent = new Intent(this, MarketplaceActivity.class);
        } else if (id == R.id.nav_account) {
            // Redirect to an AccountActivity (or a fragment) that shows account details
            intent = new Intent(this, AccountActivity.class);
        }
        if (intent != null) {
            startActivity(intent);
        }
        drawerLayout.closeDrawers();
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(navigationView)) {
            drawerLayout.closeDrawers();
        } else {
            super.onBackPressed();
        }
    }
}
