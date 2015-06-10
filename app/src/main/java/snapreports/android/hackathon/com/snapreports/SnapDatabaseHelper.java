package snapreports.android.hackathon.com.snapreports;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by shang on 2/20/2015.
 */
public class SnapDatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "SnapDatabaseHelper";
    private static final String DB_NAME = "snap.db";
    private static final int VERSION = 1;

    public static final String TABLE_NAME = "snaps";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_CAPTION = "caption";
    public static final String COLUMN_IMAGEPATH = "imagepath";
    public static final String COLUMN_ADDRESS = "address";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_LATITUDE = "latitude";
    public static final String COLUMN_LONGITUDE = "longitude";

    public SnapDatabaseHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME + "(_id integer primary key,"
        + COLUMN_CAPTION + " varchar(100),"
        + COLUMN_IMAGEPATH + " varchar(255),"
        + COLUMN_ADDRESS + " varchar(255),"
        + COLUMN_DATE + " integer,"
        + COLUMN_LATITUDE + " real,"
        + COLUMN_LONGITUDE + " real)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public long insertSnap(Snap snap) {
        Log.e(TAG, snap.getAddress() + ", " + snap.getLatitude() + ", " + snap.getLongitude());
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_CAPTION, snap.getCaption());
        cv.put(COLUMN_IMAGEPATH, snap.getImagePath());
        cv.put(COLUMN_ADDRESS, snap.getAddress());
        cv.put(COLUMN_DATE, snap.getDate().getTime());
        cv.put(COLUMN_LATITUDE, snap.getLatitude());
        cv.put(COLUMN_LONGITUDE, snap.getLongitude());
        return getWritableDatabase().insert(TABLE_NAME, null, cv);
    }

    public Cursor querySnaps() {
        return getWritableDatabase().query(TABLE_NAME, null, null, null, null, null, COLUMN_DATE + " desc");
    }

    public Cursor querySnap(long id) {
        return getWritableDatabase().query(TABLE_NAME, null, "_id = ?", new String[]{String.valueOf(id)}, null, null, null);
    }

    public void deleteReports() {
        getWritableDatabase().delete(TABLE_NAME, null, null);
    }
}
