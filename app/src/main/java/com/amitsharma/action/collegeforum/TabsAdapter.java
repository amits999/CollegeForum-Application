package com.amitsharma.action.collegeforum;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class TabsAdapter extends FragmentPagerAdapter {

    public TabsAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        switch (position){
            case 0:
                AllQuestionFragment allQuestionFragment=new AllQuestionFragment();
                return allQuestionFragment;

            case 1:
                ImpQuestionFragment impQuestionFragment=new ImpQuestionFragment();
                return impQuestionFragment;

            default:
                return null;

        }
    }

    @Override
    public int getCount() {
        return 2;
    }
}
