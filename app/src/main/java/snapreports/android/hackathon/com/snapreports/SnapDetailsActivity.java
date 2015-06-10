package snapreports.android.hackathon.com.snapreports;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


public class SnapDetailsActivity extends ActionBarActivity implements ActionBar.TabListener {

    private static final String TAG = "SnapDetailsActivity";

    public static final String SNAP_ID = "SNAP_ID";

    ActionBar actionBar;
    ViewPager viewPager;

    private long mId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snap_details);

        mId = getIntent().getLongExtra(SNAP_ID, -1);
        Log.e(TAG, "The id " + mId);
        viewPager = (ViewPager) findViewById(R.id.snapDetailsFragmentContainer);
        viewPager.setAdapter(new SnapDetailsAdapter(getSupportFragmentManager()));
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        ActionBar.Tab detailsTab = actionBar.newTab();
        detailsTab.setText("Details");
        detailsTab.setTabListener(this);

        ActionBar.Tab mapTab = actionBar.newTab();
        mapTab.setText("Map");
        mapTab.setTabListener(this);

        actionBar.addTab(detailsTab);
        actionBar.addTab(mapTab);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    private class SnapDetailsAdapter extends FragmentPagerAdapter {


        public SnapDetailsAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;
            if (position == 0) {
                fragment = SnapDetailsFragment.newInstance(mId);
            } else if (position == 1) {
                fragment = SnapMapFragment.newInstance(mId);
            }
            return fragment;
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}
