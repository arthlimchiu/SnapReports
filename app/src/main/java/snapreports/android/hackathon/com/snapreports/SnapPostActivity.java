package snapreports.android.hackathon.com.snapreports;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;


public class SnapPostActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snap_post);

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.snapPostFragmentContainer);

        if (fragment == null) {
            fragment = new SnapPostFragment();
            fm.beginTransaction().add(R.id.snapPostFragmentContainer, fragment).commit();
        }
    }
}
