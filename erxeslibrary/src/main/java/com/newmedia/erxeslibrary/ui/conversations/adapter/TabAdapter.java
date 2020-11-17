package com.newmedia.erxeslibrary.ui.conversations.adapter;

import android.content.Context;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.newmedia.erxeslibrary.R;
import com.newmedia.erxeslibrary.helper.ErxesHelper;
import com.newmedia.erxeslibrary.ui.conversations.ConversationListActivity;
import com.newmedia.erxeslibrary.ui.conversations.fragments.FaqFragment;
import com.newmedia.erxeslibrary.ui.conversations.fragments.SupportFragment;

public class TabAdapter extends FragmentPagerAdapter {
    private final ConversationListActivity context;
    private final SupportFragment supportFragment = new SupportFragment();
    private final FaqFragment faqFragment = new FaqFragment();

    public TabAdapter(FragmentManager fm, ConversationListActivity context) {
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
            case 0: return ErxesHelper.getLocalizedResources(context,context.config.language).getString(R.string.Support);
            case 1: return ErxesHelper.getLocalizedResources(context,context.config.language).getString(R.string.Faq);
            default:
                return ErxesHelper.getLocalizedResources(context,context.config.language).getString(R.string.Faq);
        }
    }


    @Override
    public int getCount() {
        return 2;
    }
}
