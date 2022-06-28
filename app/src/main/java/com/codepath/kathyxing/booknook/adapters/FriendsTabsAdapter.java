package com.codepath.kathyxing.booknook.adapters;

import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.codepath.kathyxing.booknook.fragments.FindUserFragment;
import com.codepath.kathyxing.booknook.fragments.HomeFragment;
import com.codepath.kathyxing.booknook.fragments.IncomingFriendRequestsFragment;
import com.codepath.kathyxing.booknook.fragments.MyFriendsFragment;

public class FriendsTabsAdapter extends FragmentPagerAdapter {
    private Context myContext;
    int totalTabs;

    public FriendsTabsAdapter(Context context, FragmentManager fm, int totalTabs) {
        super(fm);
        myContext = context;
        this.totalTabs = totalTabs;
    }
    // this is for fragment tabs
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new MyFriendsFragment();
            case 1:
                return new FindUserFragment();
            case 2:
                return new IncomingFriendRequestsFragment();
            default:
                return null;
        }
    }
    // this counts total number of tabs
    @Override
    public int getCount() {
        return totalTabs;
    }
}
