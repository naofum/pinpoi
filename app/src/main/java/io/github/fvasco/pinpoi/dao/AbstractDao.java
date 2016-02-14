package io.github.fvasco.pinpoi.dao;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.design.BuildConfig;

/**
 * Generic Dao.
 * Init with {@linkplain #setSqLiteOpenHelper(SQLiteOpenHelper)}.
 * {@linkplain #close()} to free resources
 *
 * @author Francesco Vasco
 */
public abstract class AbstractDao<T extends AbstractDao> implements AutoCloseable {
    SQLiteDatabase database;
    private SQLiteOpenHelper sqLiteOpenHelper;
    private int openCount = 0;

    protected void setSqLiteOpenHelper(SQLiteOpenHelper sqLiteOpenHelper) {
        if (this.sqLiteOpenHelper != null) {
            throw new IllegalStateException("sqLiteOpenHelper already defined");
        }
        this.sqLiteOpenHelper = sqLiteOpenHelper;
    }

    public synchronized T open() throws SQLException {
        //noinspection PointlessBooleanExpression
        if (BuildConfig.DEBUG && openCount < 0) {
            throw new AssertionError(openCount);
        }
        if (openCount == 0) {
            //noinspection PointlessBooleanExpression
            if (BuildConfig.DEBUG && database != null) {
                throw new AssertionError();
            }
            database = sqLiteOpenHelper.getWritableDatabase();
        }
        //noinspection PointlessBooleanExpression
        if (BuildConfig.DEBUG && database == null) {
            throw new AssertionError(openCount);
        }
        ++openCount;
        return (T) this;
    }

    public synchronized void close() {
        //noinspection PointlessBooleanExpression
        if (BuildConfig.DEBUG && openCount <= 0) {
            throw new AssertionError(openCount);
        }
        --openCount;
        if (openCount == 0) {
            database.close();
            database = null;
        } else //noinspection PointlessBooleanExpression
            if (BuildConfig.DEBUG && database == null) {
                throw new AssertionError(openCount);
            }
    }

    public SQLiteDatabase getDatabase() {
        return database;
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            close();
        } finally {
            super.finalize();
        }
    }
}
