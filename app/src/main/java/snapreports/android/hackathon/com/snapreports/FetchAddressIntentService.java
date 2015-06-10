package snapreports.android.hackathon.com.snapreports;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by shang on 2/20/2015.
 */
public class FetchAddressIntentService extends IntentService {

    private static final String TAG = "snapreports.android.hackathon.com.snapreports.FetchAddressIntentService";

    ResultReceiver mReceiver;

    public FetchAddressIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mReceiver = intent.getParcelableExtra(SnapPostFragment.RECEIVER);
        if (mReceiver == null) {
            Log.e(TAG, "No receiver received.");
            return;
        }

        Location location = intent.getParcelableExtra(SnapPostFragment.LOCATION_EXTRA);
        if (location == null) {
            Log.e(TAG, "No location passed");
            return;
        }

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = null;
        try {
            String address = intent.getStringExtra(SnapPostFragment.ADDRESS_EXTRA);
            if (address.equals("")) {
                addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            } else {
                addresses = geocoder.getFromLocationName(address, 1);
            }

            if (addresses == null || addresses.size() == 0) {
                Log.e(TAG, "No address found");
                deliverResultToReceiver(SnapPostFragment.FAILURE_RESULT, "No address found");
            } else {
                Address mAddress = addresses.get(0);
                ArrayList<String> addressFragments = new ArrayList<String>();

                for (int i = 0; i < mAddress.getMaxAddressLineIndex(); i++) {
                    addressFragments.add(mAddress.getAddressLine(i));
                }
                SnapPostFragment.mCurrentLocation.setLatitude(mAddress.getLatitude());
                SnapPostFragment.mCurrentLocation.setLongitude(mAddress.getLongitude());

                deliverResultToReceiver(SnapPostFragment.SUCCESS_RESULT, TextUtils.join(", ", addressFragments));
            }
        } catch (IOException e) {
            Log.e(TAG, "No service available");
            deliverResultToReceiver(SnapPostFragment.FAILURE_RESULT, "No receiver available");
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Invalid latitude or longitude");
            deliverResultToReceiver(SnapPostFragment.FAILURE_RESULT, "Invalid latitude or longitude");
        }
    }

    private void deliverResultToReceiver(int resultCode, String message) {
        Bundle bundle = new Bundle();
        bundle.putString(SnapPostFragment.RESULT_DATA_KEY, message);
        mReceiver.send(resultCode, bundle);
    }
}
