package snapreports.android.hackathon.com.snapreports;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.AsyncTaskLoader;

/**
 * Created by shang on 2/20/2015.
 */
public class SQLiteCursorLoader extends AsyncTaskLoader<Cursor> {

    SnapDatabaseHelper mHelper;

    Cursor mCursor;

    public SQLiteCursorLoader(Context context) {
        super(context);
        mHelper = new SnapDatabaseHelper(context);
    }

    @Override
    public Cursor loadInBackground() {
        mCursor = mHelper.querySnaps();
        if (mCursor != null)    {
            mCursor.getCount();
        }
        return mCursor;
    }

    @Override
    protected void onStartLoading() {
        if (mCursor != null) {
            deliverResult(mCursor);
        }

        if (takeContentChanged() || mCursor == null) {
            forceLoad();
        }
    }

    @Override
    protected void onReset() {
        super.onReset();
        onStopLoading();
        if (mCursor != null && !mCursor.isClosed()) {
            mCursor.close();
        }
        mCursor = null;
    }

    @Override
    public void onCanceled(Cursor data) {
        if (data != null && !data.isClosed()) {
            data.close();
        }
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    public void deliverResult(Cursor data) {
        Cursor oldCursor = mCursor;
        mCursor = data;

        if (isStarted()) {
            super.deliverResult(data);
        }

        if (oldCursor != null && oldCursor != data && !oldCursor.isClosed()) {
            oldCursor.close();
        }
    }
}
