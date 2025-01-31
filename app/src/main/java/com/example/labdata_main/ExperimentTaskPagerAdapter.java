package com.example.labdata_main;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.HashMap;
import java.util.Map;

public class ExperimentTaskPagerAdapter extends FragmentStateAdapter {
    private static final int NUM_PAGES = 4;
    private final Map<Integer, Fragment> fragments = new HashMap<>();

    public ExperimentTaskPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Fragment fragment = fragments.get(position);
        if (fragment == null) {
            switch (position) {
                case 0:
                    fragment = new SelectProjectFragment();
                    break;
                case 1:
                    fragment = new SelectMixRatioFragment();
                    break;
                case 2:
                    fragment = new SelectMoldingMethodFragment();
                    break;
                case 3:
                    fragment = new ExperimentAssignmentFragment();
                    break;
                default:
                    throw new IllegalArgumentException("Invalid position: " + position);
            }
            fragments.put(position, fragment);
        }
        return fragment;
    }

    @Override
    public int getItemCount() {
        return NUM_PAGES;
    }

    public SelectMixRatioFragment getMixRatioFragment() {
        return (SelectMixRatioFragment) fragments.get(1);
    }

    public SelectMoldingMethodFragment getMoldingMethodFragment() {
        return (SelectMoldingMethodFragment) fragments.get(2);
    }

    public ExperimentAssignmentFragment getExperimentAssignmentFragment() {
        return (ExperimentAssignmentFragment) fragments.get(3);
    }
}
