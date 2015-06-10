package snapreports.android.hackathon.com.snapreports;


import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class SnapDetailsFragment extends Fragment {

    private long mId;

    SnapDatabaseHelper mHelper;

    Cursor mCursor;

    public static SnapDetailsFragment newInstance(long id) {
        Bundle args = new Bundle();
        args.putLong(SnapDetailsActivity.SNAP_ID, id);
        SnapDetailsFragment fragment = new SnapDetailsFragment();
        fragment.setArguments(args);
        return fragment;
    }


    public SnapDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHelper = new SnapDatabaseHelper(getActivity());
        Bundle args = getArguments();
        if (args != null) {
            mId = args.getLong(SnapDetailsActivity.SNAP_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_snap_details, container, false);
        TextView tvDate = (TextView) v.findViewById(R.id.snap_details_date);
        ImageView ivImage = (ImageView) v.findViewById(R.id.snap_details_image);
        TextView tvCaption = (TextView) v.findViewById(R.id.snap_details_caption);
        TextView tvAddress = (TextView) v.findViewById(R.id.snap_details_address);

        mCursor = mHelper.querySnap(mId);
        if (mCursor != null && mCursor.moveToFirst()) {
            long date = mCursor.getLong(mCursor.getColumnIndex(SnapDatabaseHelper.COLUMN_DATE));
            tvDate.setText(new SimpleDateFormat("MM-dd-yyyy").format(new Date(date)));

            String path = mCursor.getString(mCursor.getColumnIndex(SnapDatabaseHelper.COLUMN_IMAGEPATH));
            try {
                Bitmap bitmap = decodeSampleBitmapFromPath(path, 350, 250);
                Matrix matrix = new Matrix();
                matrix.postRotate(getBitmapOrientation(path));

                Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                ivImage.setImageBitmap(rotatedBitmap);
            } catch (IOException e) {
                Log.e("SnapDetailsFragment", e.toString());
            }

            String caption = mCursor.getString(mCursor.getColumnIndex(SnapDatabaseHelper.COLUMN_CAPTION));
            tvCaption.setText(caption);

            String address = mCursor.getString(mCursor.getColumnIndex(SnapDatabaseHelper.COLUMN_ADDRESS));
            tvAddress.setText(address);
            mCursor.close();
        }

        return v;
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


}
