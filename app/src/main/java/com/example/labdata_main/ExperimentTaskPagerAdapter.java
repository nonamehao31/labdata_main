package com.example.labdata_main;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ExperimentTaskPagerAdapter extends FragmentStateAdapter {
    private static final int NUM_PAGES = 4;

    public ExperimentTaskPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new SelectProjectFragment();
            case 1:
                return new SelectMixRatioFragment();
            case 2:
                return new SelectMoldingMethodFragment();
            case 3:
                return new ExperimentAssignmentFragment();
            default:
                throw new IllegalArgumentException("Invalid position: " + position);
        }
    }

    @Override
    public int getItemCount() {
        return NUM_PAGES;
    }
}
