package snapreports.android.hackathon.com.snapreports;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by shang on 2/20/2015.
 */
public class SnapMapFragment extends SupportMapFragment implements LoaderManager.LoaderCallbacks<Cursor>{

    GoogleMap mGoogleMap;

    SnapDatabaseHelper mHelper;

    private long mId = -1;

    Cursor mCursor;

    public static SnapMapFragment newInstance(long id) {
        Bundle args = new Bundle();
        args.putLong(SnapDetailsActivity.SNAP_ID, id);
        SnapMapFragment fragment = new SnapMapFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public SnapMapFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLoaderManager().initLoader(1, null, this);
        mHelper = new SnapDatabaseHelper(getActivity());
        Bundle args = getArguments();
        if (args != null) {
            mId = args.getLong(SnapDetailsActivity.SNAP_ID, -1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new SQLiteCursorLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        mGoogleMap = getMap();
        LatLng latLng = null;

        while (data.moveToNext()) {
            latLng = new LatLng(data.getDouble(data.getColumnIndex(SnapDatabaseHelper.COLUMN_LATITUDE)),
                    data.getDouble(data.getColumnIndex(SnapDatabaseHelper.COLUMN_LONGITUDE)));

            MarkerOptions options = new MarkerOptions().position(latLng)
                    .title(data.getString(data.getColumnIndex(SnapDatabaseHelper.COLUMN_CAPTION)))
                    .snippet(data.getString(data.getColumnIndex(SnapDatabaseHelper.COLUMN_ADDRESS)));

            mGoogleMap.addMarker(options);
        }

        if (mId != -1) {
            mCursor = mHelper.querySnap(mId);
            if (mCursor != null && mCursor.moveToFirst()) {
                LatLng coordinates = new LatLng(mCursor.getDouble(mCursor.getColumnIndex(SnapDatabaseHelper.COLUMN_LATITUDE)),
                        mCursor.getDouble(mCursor.getColumnIndex(SnapDatabaseHelper.COLUMN_LONGITUDE)));
                CameraUpdate movement = CameraUpdateFactory.newLatLngZoom(coordinates, 18);
                mGoogleMap.moveCamera(movement);
            }
        }
        else if (latLng != null){
            CameraUpdate movement = CameraUpdateFactory.newLatLngZoom(latLng, 15);
            mGoogleMap.moveCamera(movement);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
