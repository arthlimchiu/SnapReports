package snapreports.android.hackathon.com.snapreports;


import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.ResultReceiver;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * A simple {@link Fragment} subclass.
 */
public class SnapPostFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener{

    private static final String TAG = "snapreports.android.hackathon.com.snapreports";

    public static final int SUCCESS_RESULT = 1;
    public static final int FAILURE_RESULT = 0;
    public static final String RECEIVER = TAG + ".RECEIVER";
    public static final String LOCATION_EXTRA = TAG + ".LOCATION_EXTRA";
    public static final String ADDRESS_EXTRA = TAG + ".ADDRESS_EXTRA";
    public static final String RESULT_DATA_KEY = TAG + ".RESULT_DATA_KEY";

    public static final int TAKE_PICTURE = 3;
    public static final int OPEN_GALLERY = 4;

    TextView tvDate;
    ImageView ivImage;
    EditText etCaption, etAddress;
    ImageButton ibSearch;

    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    AddressResultReceiver mReceiver;
    public static Location mCurrentLocation;
    SnapDatabaseHelper mHelper;

    String mImagePath;
    Uri mImageUri;

    public SnapPostFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mReceiver = new AddressResultReceiver(new Handler());
        mHelper = new SnapDatabaseHelper(getActivity());
        buildGoogleApiClient();
    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected()) {
            requestLocationUpdates();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(300000);
        mLocationRequest.setFastestInterval(180000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void requestLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_snap_post, container, false);
        tvDate = (TextView) v.findViewById(R.id.snap_post_date);
        ivImage = (ImageView) v.findViewById(R.id.snap_post_image);
        etCaption = (EditText) v.findViewById(R.id.snap_post_caption);
        etAddress = (EditText) v.findViewById(R.id.snap_post_addres);
        ibSearch = (ImageButton) v.findViewById(R.id.snap_post_search);
        ibSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String address = etAddress.getText().toString();
                startIntentService(address);
            }
        });

        tvDate.setText(new SimpleDateFormat("MM-dd-yyyy").format(new Date()));
        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_snap_post, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.snap_picture:
                takePhoto();
                return true;
            case R.id.snap_gallery:
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_PICK);
                startActivityForResult(intent, OPEN_GALLERY);
                return true;
            case R.id.snap_post:
                Intent i = new Intent(getActivity(), SnapActivity.class);
                Snap snap = new Snap();
                snap.setCaption(etCaption.getText().toString());
                snap.setAddress(etAddress.getText().toString());
                snap.setDate(new Date());
                snap.setImagePath(mImagePath);
                snap.setLatitude(mCurrentLocation.getLatitude());
                snap.setLongitude(mCurrentLocation.getLongitude());
                Log.e(TAG, mHelper.insertSnap(snap) + "");
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case TAKE_PICTURE:
                Uri selectImage = mImageUri;
                getActivity().getContentResolver().notifyChange(selectImage, null);
                Bitmap bitmap;

                try {
                    bitmap = decodeSampleBitmapFromPath(mImagePath, 350, 250);

                    Matrix matrix = new Matrix();
                    matrix.postRotate(getBitmapOrientation(mImagePath));

                    Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                    ivImage.setImageBitmap(rotatedBitmap);

                } catch (IOException e) {
                    Log.e(TAG, e.toString());
                }
                galleryAddPic();
                break;
            case OPEN_GALLERY:
                Uri imageUri = data.getData();
                mImagePath = getPathFromGallery(imageUri);
                Bitmap b;

                try {
                    b = decodeSampleBitmapFromPath(mImagePath, 350, 250);
                    Matrix matrix = new Matrix();
                    matrix.postRotate(getBitmapOrientation(mImagePath));

                    Bitmap rotatedBitmap = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, true);
                    ivImage.setImageBitmap(rotatedBitmap);
                } catch (IOException e) {
                    Log.e(TAG, e.toString());
                }
                break;
        }
    }

    private String getPathFromGallery(Uri uri) {
        String[] columnPaths = {MediaStore.Images.Media.DATA};
        Cursor cursor = getActivity().getContentResolver().query(uri, columnPaths, null, null, null);
        int columnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();

        return cursor.getString(columnIndex);
    }

    private void takePhoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File photo = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), imageFileName + ".jpg");
        mImageUri = Uri.fromFile(photo);
        mImagePath = photo.getAbsolutePath();

        intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
        startActivityForResult(intent, TAKE_PICTURE);
    }

    private Bitmap decodeSampleBitmapFromPath(String path, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        options.inSampleSize = createInSampleSize(options, reqWidth, reqHeight);

        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(path, options);
    }

    private int createInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    private int getBitmapOrientation(String path) throws IOException {
        ExifInterface exif = new ExifInterface(path);
        int rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        if (rotation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        } else if (rotation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if (rotation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        }
        return 0;
    }

    private void galleryAddPic() {
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mImagePath);
        intent.setData(Uri.fromFile(f));
        getActivity().sendBroadcast(intent);
    }

    @Override
    public void onConnected(Bundle bundle) {
        requestLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        startIntentService("");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    private void startIntentService(String address) {
        Intent intent = new Intent(getActivity(), FetchAddressIntentService.class);
        intent.putExtra(LOCATION_EXTRA, mCurrentLocation);
        intent.putExtra(RECEIVER, mReceiver);
        intent.putExtra(ADDRESS_EXTRA, address);
        getActivity().startService(intent);
    }

    private class AddressResultReceiver extends ResultReceiver {

        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            if (resultCode == SUCCESS_RESULT) {
                etAddress.setText(resultData.getString(RESULT_DATA_KEY));
            }
        }
    }
}
