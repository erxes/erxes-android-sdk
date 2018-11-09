package com.newmedia.erxeslibrary.ui.conversations.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.newmedia.erxeslibrary.ui.conversations.fragments.FaqFragment;
import com.newmedia.erxeslibrary.ui.conversations.fragments.SupportFragment;

public class TabAdapter extends FragmentPagerAdapter {

    public TabAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        if(position == 0)
            return SupportFragment.newInstance("1","2");
        return new FaqFragment();
    }

    @Override
    public int getCount() {
        return 2;
    }
}
