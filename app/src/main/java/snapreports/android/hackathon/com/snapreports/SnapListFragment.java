package snapreports.android.hackathon.com.snapreports;


import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class SnapListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final String TAG = "snapreports.android.hackathon.com.snapreports";


    public SnapListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new SQLiteCursorLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        SnapListAdapter adapter = new SnapListAdapter(getActivity(), data);
        setListAdapter(adapter);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        setListAdapter(null);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Intent intent = new Intent(getActivity(), SnapDetailsActivity.class);
        intent.putExtra(SnapDetailsActivity.SNAP_ID, id);
        startActivity(intent);
    }

    private class SnapListAdapter extends CursorAdapter {

        public SnapListAdapter(Context context, Cursor cursor) {
            super(context, cursor, 0);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            return inflater.inflate(R.layout.fragment_snap_list, parent, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {

            TextView tvDate = (TextView) view.findViewById(R.id.snap_list_date);
            ImageView ivImage = (ImageView) view.findViewById(R.id.snap_list_image);
            TextView tvCaption = (TextView) view.findViewById(R.id.snap_list_caption);
            TextView tvAddress = (TextView) view.findViewById(R.id.snap_list_address);

            long date = cursor.getLong(cursor.getColumnIndex(SnapDatabaseHelper.COLUMN_DATE));
            tvDate.setText(new SimpleDateFormat("MM-dd-yyyy").format(new Date(date)));

            String path = cursor.getString(cursor.getColumnIndex(SnapDatabaseHelper.COLUMN_IMAGEPATH));
            try {
                Bitmap bitmap = decodeSampleBitmapFromPath(path, 350, 300);
                Matrix matrix = new Matrix();
                matrix.postRotate(getBitmapOrientation(path));

                Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                ivImage.setImageBitmap(rotatedBitmap);
            } catch (IOException e) {
                Log.e(TAG, e.toString());
            }

            String caption = cursor.getString(cursor.getColumnIndex(SnapDatabaseHelper.COLUMN_CAPTION));
            tvCaption.setText(caption);

            String address = cursor.getString(cursor.getColumnIndex(SnapDatabaseHelper.COLUMN_ADDRESS));
            tvAddress.setText(address);
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
}
