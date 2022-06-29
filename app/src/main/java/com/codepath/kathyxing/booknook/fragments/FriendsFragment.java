package com.codepath.kathyxing.booknook.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.codepath.kathyxing.booknook.R;
import com.codepath.kathyxing.booknook.adapters.FriendsTabsAdapter;
import com.google.android.material.tabs.TabLayout;

/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {

    // fragment parameters
    public static final String TAG = "FriendsFragment";
    private TabLayout tlTabs;
    private ViewPager viewPager;

    // Required empty public constructor
    public FriendsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_friends, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Set the toolbar text
        Activity activity = getActivity();
        if (activity != null) {
            activity.setTitle(getString(R.string.friends));
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // initialize views
        tlTabs = view.findViewById(R.id.tlTabs);
        viewPager = view.findViewById(R.id.viewPager);
        tlTabs.addTab(tlTabs.newTab().setText("My Friends"));
        tlTabs.addTab(tlTabs.newTab().setText("Find User"));
        tlTabs.addTab(tlTabs.newTab().setText("Incoming Requests"));
        tlTabs.setTabGravity(TabLayout.GRAVITY_FILL);

        final FriendsTabsAdapter adapter = new FriendsTabsAdapter(getContext(),
                getChildFragmentManager(), tlTabs.getTabCount());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tlTabs));
        tlTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Log.i(TAG, "switched to tab at position " + tab.getPosition());
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }
}