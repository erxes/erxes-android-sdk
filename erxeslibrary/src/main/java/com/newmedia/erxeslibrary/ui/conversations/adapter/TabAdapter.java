package com.newmedia.erxeslibrary.ui.conversations.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.newmedia.erxeslibrary.R;
import com.newmedia.erxeslibrary.ui.conversations.fragments.FaqFragment;
import com.newmedia.erxeslibrary.ui.conversations.fragments.SupportFragment;

public class TabAdapter extends FragmentPagerAdapter {
    private Context context;
    public TabAdapter(FragmentManager fm,Context context) {
        super(fm);
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
        if(position == 0)
            return SupportFragment.newInstance("1","2");
        return new FaqFragment();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if(position == 0 )
            return context.getString(R.string.SUPPORT);
        else
            return context.getString(R.string.FAQ);
    }

    @Override
    public int getCount() {
        return 2;
    }
}
