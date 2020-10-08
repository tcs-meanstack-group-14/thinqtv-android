package com.thinqtv.thinqtv_android;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.thinqtv.thinqtv_android.data.UserRepository;

public class MainActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setOnNavigationItemSelectedListener(navigationItemSelectedListener);

        if (UserRepository.getInstance().isLoggedIn())
        {
            openFragment(conversations_fragment.newInstance());
            bottomNavigation.setSelectedItemId(R.id.action_conversations);
        }
        else
        {
            openFragment(welcome_fragment.newInstance());
            bottomNavigation.setSelectedItemId(R.id.action_profile);
        }
    }

    public void openFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener = item ->
    {
        switch (item.getItemId()) {
            case R.id.action_dropin:
                openFragment(dropin_fragment.newInstance());
                return true;
            case R.id.action_conversations:
                openFragment(conversations_fragment.newInstance());
                return true;
            case R.id.action_aboutus:
                openFragment(aboutus_fragment.newInstance("",""));
                return true;
            case R.id.action_profile:
                if (UserRepository.getInstance().isLoggedIn())
                    openFragment(profile_fragment.newInstance());
                else
                    openFragment(welcome_fragment.newInstance());

                return true;
        }
        return false;
    };
}