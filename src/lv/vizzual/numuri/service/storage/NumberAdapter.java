/**
 * This software is released under GPLv3 licence. For
 * further information regarding GPL licence, please visit
 * http://www.gnu.org/licenses/gpl.html
 */
package lv.vizzual.numuri.service.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.util.Date;
import lv.vizzual.numuri.model.PhoneNumber;

public class NumberAdapter {
    private static final String TAG = "NumberAdapter";
    private MessageOpenHelper helper;
    private SQLiteDatabase db;
    
    private static final String DATABASE_NAME = "numuri.db";
    private static final String DATABASE_TABLE = "numbers";
    private static final int DATABASE_VERSION = 2;

    private static final String KEY_ID = "_id";
    private static final String KEY_PHONE_NUMBER = "phone_number";
    private static final String KEY_NETWORK_PROVIDER = "provider";
    private static final String KEY_DATE_UPDATED = "date_created";

    private static final int KEY_ID_COLUMN = 0;
    private static final int KEY_PHONE_NUMBER_COLUMN = 1;
    private static final int KEY_PROVIDER_COLUMN = 2;
    private static final int KEY_DATE_UPDATED_COLUMN = 4;
    
    public NumberAdapter(Context context) {
        helper = new MessageOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void open() throws SQLiteException {
        try {
            db = helper.getWritableDatabase();
        } catch (SQLiteException ex) {
            db = helper.getReadableDatabase();
        }
    }

    public void close() {
        db.close();
    }

    public long replace(PhoneNumber number) {
        String nr = number.getPhoneNumber();

        // just in case it exists
        deleteNumber(nr);

        Log.d(TAG, "saving/replacing provider for number " + number.getPhoneNumber());

        // Create a new row of values to insert.
        ContentValues values = new ContentValues();
        // Assign values for each row.
        values.put(KEY_PHONE_NUMBER, number.getPhoneNumber());
        values.put(KEY_NETWORK_PROVIDER, number.getNetworkProvider());
        values.put(KEY_DATE_UPDATED, number.getLastUpdated().getTime());

        // Insert the row.
        return db.insert(DATABASE_TABLE, null, values);
    }

   public void deleteNumber(String number) {
        db.delete(DATABASE_TABLE, KEY_PHONE_NUMBER + "='" + number + "'", null);
    }
   
    public void deleteAll() {
        db.delete(DATABASE_TABLE, null, null);
    }

    public boolean hasDataForNumber(String number) {
        boolean hasData = false;

        long dateLimit = new Date().getTime() - 1000 * 60 * 60 * 24 * 7;

        String criteria = KEY_PHONE_NUMBER + "='" + number + "' AND "
                        + KEY_DATE_UPDATED + ">" + dateLimit;
        
        Cursor cursor = db.query(true, DATABASE_TABLE,
            new String[] {KEY_PHONE_NUMBER},
            criteria, null, null, null,
            null, null);

        if ((cursor.getCount() > 0)) {
            hasData = true;
        }
        cursor.close();

        return hasData;
    }
    
    public String getNetworkProvider(String number) {
        String provider = null;
        
        Cursor cursor = db.query(true, DATABASE_TABLE,
            new String[] {KEY_NETWORK_PROVIDER},
            KEY_PHONE_NUMBER + "='" + number + "'", null, null, null,
            null, null);

        if ((cursor.getCount() > 0)) {
            cursor.moveToFirst();
            provider = cursor.getString(0);
        }

        cursor.close();

        return provider;
    }

    static class MessageOpenHelper extends SQLiteOpenHelper {
        public MessageOpenHelper(Context context, String name, CursorFactory
            factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String sql = "CREATE TABLE " + DATABASE_TABLE + " (" +
                KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                KEY_PHONE_NUMBER + " TEXT NOT NULL UNIQUE," +
                KEY_NETWORK_PROVIDER + " TEXT," +
                KEY_DATE_UPDATED + " INTEGER NOT NULL)";

            db.execSQL(sql);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
            onCreate(db);
        }
    }
}
