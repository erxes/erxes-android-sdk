package com.newmedia.erxeslibrary.ui.conversations.adapter;

import android.content.Context;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.newmedia.erxeslibrary.R;
import com.newmedia.erxeslibrary.ui.conversations.fragments.FaqFragment;
import com.newmedia.erxeslibrary.ui.conversations.fragments.SupportFragment;

public class TabAdapter extends FragmentPagerAdapter {
    private Context context;
    private SupportFragment supportFragment = new SupportFragment();
    private FaqFragment faqFragment = new FaqFragment();

    public TabAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
        if(position == 0)
            return supportFragment;
        return faqFragment;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position){
            case 0: return context.getResources().getString(R.string.Support);
            case 1: return context.getResources().getString(R.string.Faq);
            default:
                return context.getResources().getString(R.string.Faq);
        }
    }


    @Override
    public int getCount() {
        return 2;
    }
}
