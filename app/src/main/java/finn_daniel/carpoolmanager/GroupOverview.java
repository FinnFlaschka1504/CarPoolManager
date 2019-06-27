package finn_daniel.carpoolmanager;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

public class GroupOverview extends FragmentActivity {
    private static final int NUM_PAGES = 2;
    private ViewPager mPager;
    private PagerAdapter pagerAdapter;
    String standardView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        // Instantiate a ViewPager and a PagerAdapter.

        SharedPreferences mySPR = getSharedPreferences("Settings",0);
        standardView = mySPR.getString("standardView", "Übersicht");

        mPager = (ViewPager) findViewById(R.id.pager);
        pagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(pagerAdapter);
        mPager.setCurrentItem(standardView.equals("Übersicht") ? 0 : 1);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout_group);
        tabLayout.setupWithViewPager(mPager);
        tabLayout.getTabAt(0).setText("Übersicht");
        tabLayout.getTabAt(1).setText("Kalender");


    }

    @Override
    public void onBackPressed() {
        if ((mPager.getCurrentItem() == 0 && standardView.equals("Übersicht")) || (mPager.getCurrentItem() == 1 && standardView.equals("Kalender"))) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            mPager.setCurrentItem(mPager.getCurrentItem() == 0 ? 1 : 0);
        }
    }

    /**
     * A simple pager adapter that represents 5 ViewPager_GroupOverview objects, in
     * sequence.
     */
    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case 0: return new ViewPager_GroupOverview();
                case 1: return new ViewPager_GroupCalender();
                default: return new ViewPager_GroupOverview();

            }
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }



}

class ViewPager_GroupOverview extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.group_overview, container, false);

        return rootView;

    }
}

class ViewPager_GroupCalender extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.group_calender, container, false);

        return rootView;

    }
}