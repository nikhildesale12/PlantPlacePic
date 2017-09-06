package com.ibin.plantplacepic.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.ibin.plantplacepic.bean.SubmitRequest;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private final Context myContext;
    private static DatabaseHelper databaseInstance;
    private static SQLiteDatabase sqLiteDatabase;
    /*Database variables starts*/
    public static final String DATABASE_NAME = "plantplacepic.sqlite";
    public static final int DATABASE_VERSION = 1;
    /*Tables Name*/
    private static final String TABLE_INFORMATION = "information";
    private static final String TABLE_INFORMATION_SAVE_DATA = "informatio_save";

    /*Field Name - species_structure */
    private static final String COLUMN_INFO_USERID = "user_id";
    private static final String COLUMN_INFO_IMAGES_URL = "imageurl";
    private static final String COLUMN_INFO_IMAGES = "images";
    private static final String COLUMN_INFO_SPECIES = "species";
    private static final String COLUMN_INFO_REMATK = "remark";
    private static final String COLUMN_INFO_TAG = "tag";
    private static final String COLUMN_INFO_STATUS = "status";
    private static final String COLUMN_INFO_TITLE = "title";
    private static final String COLUMN_INFO_LAT = "lat";
    private static final String COLUMN_INFO_LNG = "lng";
    private static final String COLUMN_INFO_ADDRESS = "address";
    private static final String COLUMN_INFO_CROP = "crop";
    private static final String COLUMN_INFO_TIME = "time";
    private static final String COLUMN_INFO_UPLOAD_FROM = "upload_from";

    //private static final String COLUMN_SUBMIT_REQUEST= "SubmitRequest";
    /*Database variables end*/
    /*create Query starts*/
    private static final String CREATE_TABLE_INFORMATION = "CREATE TABLE " + TABLE_INFORMATION + "(" + COLUMN_INFO_USERID + " TEXT," + COLUMN_INFO_IMAGES_URL + " TEXT," + COLUMN_INFO_IMAGES + " TEXT,"
            + COLUMN_INFO_SPECIES + " TEXT," + COLUMN_INFO_REMATK + " TEXT," + COLUMN_INFO_TAG + " TEXT," + COLUMN_INFO_STATUS + " TEXT," +
            COLUMN_INFO_TITLE + " TEXT," + COLUMN_INFO_LAT + " TEXT," + COLUMN_INFO_LNG + " TEXT," + COLUMN_INFO_ADDRESS + " TEXT," +
            COLUMN_INFO_CROP + " TEXT," + COLUMN_INFO_TIME + " TEXT," + COLUMN_INFO_UPLOAD_FROM + " TEXT);";
    /*create Query end*/
    /*/data/user/0/com.ibin.pathangasuchya/databases/butterfly*/
    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.myContext = context;
    }

    public static synchronized DatabaseHelper getDatabaseInstance(Context context) {
        if (databaseInstance == null) {
            databaseInstance = new DatabaseHelper(context.getApplicationContext());
        }
        return databaseInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_TABLE_INFORMATION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // on upgrade drop older tables
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + CREATE_TABLE_INFORMATION);
        // create new tables
        onCreate(sqLiteDatabase);
    }

    /*Insert values in Species_structure table*/
    public long insertDataInTableInformation(SubmitRequest request) {
        openDatabase();
        long lastInsert = -1;
        if (request != null) {
            try {
                ContentValues initialValues = new ContentValues();
                initialValues.put(COLUMN_INFO_USERID, request.getUserId());
                initialValues.put(COLUMN_INFO_IMAGES, request.getImageName());
                initialValues.put(COLUMN_INFO_IMAGES_URL, request.getImageUrl());
                initialValues.put(COLUMN_INFO_SPECIES, request.getSpecies());
                initialValues.put(COLUMN_INFO_REMATK, request.getRemark());
                initialValues.put(COLUMN_INFO_TAG, request.getTag());
                initialValues.put(COLUMN_INFO_STATUS, request.getStatus());
                initialValues.put(COLUMN_INFO_TITLE, request.getTitle());
                initialValues.put(COLUMN_INFO_LAT, request.getLatitude());
                initialValues.put(COLUMN_INFO_LNG, request.getLongitude());
                initialValues.put(COLUMN_INFO_ADDRESS, request.getAddress());
                initialValues.put(COLUMN_INFO_CROP, request.getCrop());
                initialValues.put(COLUMN_INFO_TIME, request.getTime());
                initialValues.put(COLUMN_INFO_UPLOAD_FROM, request.getUploadedFrom());
                lastInsert = sqLiteDatabase.insert(TABLE_INFORMATION, null, initialValues);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        close();
        return lastInsert;
    }


    public List<SubmitRequest> getImageInfoToUpload(String userId) {
        openDatabase();
        List<SubmitRequest> dataList = new ArrayList<SubmitRequest>();
        String selectQuery = "SELECT * FROM " + TABLE_INFORMATION  + " WHERE " + COLUMN_INFO_USERID + "='" + userId + "' AND "+COLUMN_INFO_STATUS + "='false'" ;
        Cursor cursor = sqLiteDatabase.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                SubmitRequest sr = new SubmitRequest();
                sr.setImageUrl(cursor.getString(cursor.getColumnIndex(COLUMN_INFO_IMAGES_URL)));
                sr.setTime(cursor.getString(cursor.getColumnIndex(COLUMN_INFO_TIME)));
                sr.setTag(cursor.getString(cursor.getColumnIndex(COLUMN_INFO_TAG)));
                sr.setTitle(cursor.getString(cursor.getColumnIndex(COLUMN_INFO_TITLE)));
                sr.setImageName(cursor.getString(cursor.getColumnIndex(COLUMN_INFO_IMAGES)));
                sr.setAddress(cursor.getString(cursor.getColumnIndex(COLUMN_INFO_ADDRESS)));
                sr.setLongitude(cursor.getString(cursor.getColumnIndex(COLUMN_INFO_LAT)));
                sr.setLongitude(cursor.getString(cursor.getColumnIndex(COLUMN_INFO_LNG)));
                sr.setRemark(cursor.getString(cursor.getColumnIndex(COLUMN_INFO_REMATK)));
                sr.setSpecies(cursor.getString(cursor.getColumnIndex(COLUMN_INFO_SPECIES)));
                sr.setUserId(cursor.getString(cursor.getColumnIndex(COLUMN_INFO_USERID)));
                dataList.add(sr);
            } while (cursor.moveToNext());
        }
        close();
        return dataList;
    }

    public int deleteFromLocal(String userId,String imageName){
        openDatabase();
        int i = -1;
        i = sqLiteDatabase.delete(TABLE_INFORMATION,COLUMN_INFO_USERID  + "=? AND " +COLUMN_INFO_IMAGES + "=?",new String[] {userId,imageName});
        close();
        return i;
    }
    /*Delete all table*/
    public void removeAllTable() {
        openDatabase();
        sqLiteDatabase.delete(TABLE_INFORMATION, null, null);
        close();
    }

    public SQLiteDatabase openDatabase() throws SQLException {
        if ((sqLiteDatabase == null) || (!sqLiteDatabase.isOpen())) {
            sqLiteDatabase = this.getWritableDatabase();
        }
        return sqLiteDatabase;
    }

    @Override
    public void close() throws SQLException {
        super.close();
        if (sqLiteDatabase != null && sqLiteDatabase.isOpen()) {
            sqLiteDatabase.close();
            sqLiteDatabase = null;
        }
    }
}
