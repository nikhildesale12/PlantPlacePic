package com.ibin.plantplacepic.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Address;
import android.location.Geocoder;
import android.text.TextUtils;
import android.util.Log;

import com.ibin.plantplacepic.bean.Information;
import com.ibin.plantplacepic.bean.SubmitRequest;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private final Context myContext;
    private static DatabaseHelper databaseInstance;
    private static SQLiteDatabase sqLiteDatabase;
    /*Database variables starts*/
    public static final String DATABASE_NAME = "plantplacepic.sqlite";
    public static final int DATABASE_VERSION = 3;
    /*Tables Name*/
    private static final String TABLE_INFORMATION_SAVE_TO_LATER = "information";
    private static final String TABLE_INFORMATION_SAVE_DATA = "information_save";
    private static final String TABLE_ALL_INFORMATION_SAVE_DATA = "all_information_save";

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
    private static final String COLUMN_INFO_UPDATE_INFO = "update_info";
    private static final String COLUMN_INFO_UPLOAD_FROM = "upload_from";

    //private static final String COLUMN_SUBMIT_REQUEST= "SubmitRequest";
    /*Database variables end*/
    /*create Query starts*/
    //To save dta which we hv to upload
    private static final String CREATE_TABLE_INFORMATION_SAVE_TO_LATER = "CREATE TABLE " + TABLE_INFORMATION_SAVE_TO_LATER + "(" + COLUMN_INFO_USERID + " TEXT," + COLUMN_INFO_IMAGES_URL + " TEXT," + COLUMN_INFO_IMAGES + " TEXT,"
            + COLUMN_INFO_SPECIES + " TEXT," + COLUMN_INFO_REMATK + " TEXT," + COLUMN_INFO_TAG + " TEXT," + COLUMN_INFO_STATUS + " TEXT," +
            COLUMN_INFO_TITLE + " TEXT," + COLUMN_INFO_LAT + " TEXT," + COLUMN_INFO_LNG + " TEXT," + COLUMN_INFO_ADDRESS + " TEXT," +
            COLUMN_INFO_CROP + " TEXT," + COLUMN_INFO_TIME + " TEXT," + COLUMN_INFO_UPDATE_INFO + " TEXT,"+ COLUMN_INFO_UPLOAD_FROM + " TEXT);";
    //To save all uploaded data
    private static final String CREATE_TABLE_INFORMATION_SAVE = "CREATE TABLE " + TABLE_INFORMATION_SAVE_DATA + "(" + COLUMN_INFO_USERID + " TEXT," + COLUMN_INFO_IMAGES_URL + " TEXT," + COLUMN_INFO_IMAGES + " TEXT,"
            + COLUMN_INFO_SPECIES + " TEXT," + COLUMN_INFO_REMATK + " TEXT," + COLUMN_INFO_TAG + " TEXT," + COLUMN_INFO_STATUS + " TEXT," +
            COLUMN_INFO_TITLE + " TEXT," + COLUMN_INFO_LAT + " TEXT," + COLUMN_INFO_LNG + " TEXT," + COLUMN_INFO_ADDRESS + " TEXT," +
            COLUMN_INFO_CROP + " TEXT," + COLUMN_INFO_TIME + " TEXT," + COLUMN_INFO_UPDATE_INFO + " TEXT," + COLUMN_INFO_UPLOAD_FROM + " TEXT);";

    private static final String CREATE_TABLE_ALL_INFORMATION_SAVE = "CREATE TABLE " + TABLE_ALL_INFORMATION_SAVE_DATA + "(" + COLUMN_INFO_USERID + " TEXT," + COLUMN_INFO_IMAGES_URL + " TEXT," + COLUMN_INFO_IMAGES + " TEXT,"
            + COLUMN_INFO_SPECIES + " TEXT," + COLUMN_INFO_REMATK + " TEXT," + COLUMN_INFO_TAG + " TEXT," + COLUMN_INFO_STATUS + " TEXT," +
            COLUMN_INFO_TITLE + " TEXT," + COLUMN_INFO_LAT + " TEXT," + COLUMN_INFO_LNG + " TEXT," + COLUMN_INFO_ADDRESS + " TEXT," +
            COLUMN_INFO_CROP + " TEXT," + COLUMN_INFO_TIME + " TEXT," + COLUMN_INFO_UPDATE_INFO + " TEXT," + COLUMN_INFO_UPLOAD_FROM + " TEXT);";
    /*create Query end*/

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
        sqLiteDatabase.execSQL(CREATE_TABLE_INFORMATION_SAVE_TO_LATER);
        sqLiteDatabase.execSQL(CREATE_TABLE_INFORMATION_SAVE);
        sqLiteDatabase.execSQL(CREATE_TABLE_ALL_INFORMATION_SAVE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // on upgrade drop older tables
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_INFORMATION_SAVE_TO_LATER);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_INFORMATION_SAVE_DATA);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_ALL_INFORMATION_SAVE_DATA);
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
                initialValues.put(COLUMN_INFO_UPDATE_INFO, request.getUpdateInfo());
                initialValues.put(COLUMN_INFO_UPLOAD_FROM, request.getUploadedFrom());
                lastInsert = sqLiteDatabase.insert(TABLE_INFORMATION_SAVE_TO_LATER, null, initialValues);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        close();
        return lastInsert;
    }
    /*Insert values TO SAVe Uploaded data table*/
    public long insertDataInTableInformationToSave(Information information) {
        openDatabase();
        long lastInsert = -1;
        if (information != null) {
            try {
                ContentValues initialValues = new ContentValues();
                initialValues.put(COLUMN_INFO_USERID, information.getUserId());
                initialValues.put(COLUMN_INFO_IMAGES, information.getImages());
                initialValues.put(COLUMN_INFO_SPECIES, information.getSpecies());
                initialValues.put(COLUMN_INFO_REMATK, information.getRemark());
                initialValues.put(COLUMN_INFO_TAG, information.getTag());
                initialValues.put(COLUMN_INFO_STATUS, information.getStatus());
                initialValues.put(COLUMN_INFO_TITLE, information.getTitle());
                initialValues.put(COLUMN_INFO_LAT, information.getLat());
                initialValues.put(COLUMN_INFO_LNG, information.getLng());
                initialValues.put(COLUMN_INFO_ADDRESS, information.getAddress());
                initialValues.put(COLUMN_INFO_CROP, information.getCrop());
                initialValues.put(COLUMN_INFO_TIME, information.getTime());
                initialValues.put(COLUMN_INFO_UPDATE_INFO, information.getUpdateinfo());
                initialValues.put(COLUMN_INFO_UPLOAD_FROM, information.getUploadFrom());
                lastInsert = sqLiteDatabase.insert(TABLE_INFORMATION_SAVE_DATA, null, initialValues);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        close();
        return lastInsert;
    }

    public long insertDataInTableAllInformationToSave(Information information) {
        openDatabase();
        long lastInsert = -1;
        if (information != null) {
            try {
                ContentValues initialValues = new ContentValues();
                initialValues.put(COLUMN_INFO_USERID, information.getUserId());
                initialValues.put(COLUMN_INFO_IMAGES, information.getImages());
                initialValues.put(COLUMN_INFO_SPECIES, information.getSpecies());
                initialValues.put(COLUMN_INFO_REMATK, information.getRemark());
                initialValues.put(COLUMN_INFO_TAG, information.getTag());
                initialValues.put(COLUMN_INFO_STATUS, information.getStatus());
                initialValues.put(COLUMN_INFO_TITLE, information.getTitle());
                initialValues.put(COLUMN_INFO_LAT, information.getLat());
                initialValues.put(COLUMN_INFO_LNG, information.getLng());
                initialValues.put(COLUMN_INFO_ADDRESS, information.getAddress());
                initialValues.put(COLUMN_INFO_CROP, information.getCrop());
                initialValues.put(COLUMN_INFO_TIME, information.getTime());
                initialValues.put(COLUMN_INFO_UPDATE_INFO, information.getUpdateinfo());
                initialValues.put(COLUMN_INFO_UPLOAD_FROM, information.getUploadFrom());
                lastInsert = sqLiteDatabase.insert(TABLE_ALL_INFORMATION_SAVE_DATA, null, initialValues);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        close();
        return lastInsert;
    }
    //get info to upload data
    public List<SubmitRequest> getImageInfoToUpload(String userId) {
        openDatabase();
        List<SubmitRequest> dataList = null;
        try {
            dataList = new ArrayList<>();
            String selectQuery = "SELECT * FROM " + TABLE_INFORMATION_SAVE_TO_LATER + " WHERE " + COLUMN_INFO_USERID + "='" + userId + "'";
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
                    sr.setUploadedFrom(cursor.getString(cursor.getColumnIndex(COLUMN_INFO_UPLOAD_FROM)));
                    dataList.add(sr);
                } while (cursor.moveToNext());
            }
            close();
        }catch(Exception e){
            e.printStackTrace();
        }
        return dataList;
    }
    /*get info of uploaded data starts */
    public ArrayList<Information> getImageUploadedInfo(String userId) {
        openDatabase();
        ArrayList<Information> dataList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_INFORMATION_SAVE_DATA  + " WHERE " + COLUMN_INFO_USERID + "='" + userId + "'";
        Cursor cursor = sqLiteDatabase.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                Information sr = new Information();
                sr.setTime(cursor.getString(cursor.getColumnIndex(COLUMN_INFO_TIME)));
                sr.setTag(cursor.getString(cursor.getColumnIndex(COLUMN_INFO_TAG)));
                sr.setTitle(cursor.getString(cursor.getColumnIndex(COLUMN_INFO_TITLE)));
                sr.setImages(cursor.getString(cursor.getColumnIndex(COLUMN_INFO_IMAGES)));
                sr.setAddress(cursor.getString(cursor.getColumnIndex(COLUMN_INFO_ADDRESS)));
                sr.setLat(cursor.getString(cursor.getColumnIndex(COLUMN_INFO_LAT)));
                sr.setLng(cursor.getString(cursor.getColumnIndex(COLUMN_INFO_LNG)));
                sr.setRemark(cursor.getString(cursor.getColumnIndex(COLUMN_INFO_REMATK)));
                sr.setSpecies(cursor.getString(cursor.getColumnIndex(COLUMN_INFO_SPECIES)));
                sr.setUserId(cursor.getString(cursor.getColumnIndex(COLUMN_INFO_USERID)));
                sr.setUpdateinfo(cursor.getString(cursor.getColumnIndex(COLUMN_INFO_UPLOAD_FROM)));
                sr.setUploadFrom(cursor.getString(cursor.getColumnIndex(COLUMN_INFO_UPLOAD_FROM)));
                dataList.add(sr);
            } while (cursor.moveToNext());
        }
        close();
        return dataList;
    }
    /*end*/
    /*starts*/
    public ArrayList<Information> getAllImageUploadedInfo() {
        openDatabase();
        ArrayList<Information> dataList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_ALL_INFORMATION_SAVE_DATA ;
        Cursor cursor = sqLiteDatabase.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                Information sr = new Information();
                //sr.setImageUrl(cursor.getString(cursor.getColumnIndex(COLUMN_INFO_IMAGES_URL)));
                sr.setTime(cursor.getString(cursor.getColumnIndex(COLUMN_INFO_TIME)));
                sr.setTag(cursor.getString(cursor.getColumnIndex(COLUMN_INFO_TAG)));
                sr.setTitle(cursor.getString(cursor.getColumnIndex(COLUMN_INFO_TITLE)));
                sr.setImages(cursor.getString(cursor.getColumnIndex(COLUMN_INFO_IMAGES)));
                sr.setAddress(cursor.getString(cursor.getColumnIndex(COLUMN_INFO_ADDRESS)));
                sr.setLat(cursor.getString(cursor.getColumnIndex(COLUMN_INFO_LAT)));
                sr.setLng(cursor.getString(cursor.getColumnIndex(COLUMN_INFO_LNG)));
                sr.setRemark(cursor.getString(cursor.getColumnIndex(COLUMN_INFO_REMATK)));
                sr.setSpecies(cursor.getString(cursor.getColumnIndex(COLUMN_INFO_SPECIES)));
                sr.setUserId(cursor.getString(cursor.getColumnIndex(COLUMN_INFO_USERID)));
                sr.setUpdateinfo(cursor.getString(cursor.getColumnIndex(COLUMN_INFO_UPLOAD_FROM)));
                sr.setUploadFrom(cursor.getString(cursor.getColumnIndex(COLUMN_INFO_UPLOAD_FROM)));
                dataList.add(sr);
            } while (cursor.moveToNext());
        }
        close();
        return dataList;
    }
    /*end*/
    //update information in local
    public int updateInfoInLocal(String userId,String  IMAGE, String SPECIES,String  REMARK,String  TAG, String TITLE,String  serverFolderPath,String  serverFolderPathFrom,String  ADDRESS){
        int updateResult = 0;
        openDatabase();
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues values=new ContentValues();
        values.put(COLUMN_INFO_SPECIES,SPECIES);
        values.put(COLUMN_INFO_REMATK,REMARK);
        values.put(COLUMN_INFO_TAG,TAG);
        values.put(COLUMN_INFO_TITLE,TITLE);
        values.put(COLUMN_INFO_ADDRESS,ADDRESS);
        updateResult=db.update(TABLE_INFORMATION_SAVE_DATA,values,"user_id='"+userId+"' and images='"+IMAGE+"'",null);
        close();
        return updateResult;
    }

    //move folder
    public int moveFolderInLocal(String userId, String imageName, String fromSpecies, String toSpecies){
        int updateResult = 0;
        openDatabase();
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues values=new ContentValues();

        values.put(COLUMN_INFO_SPECIES,toSpecies);
        values.put(COLUMN_INFO_UPDATE_INFO,"From "+fromSpecies+" To "+toSpecies);

        updateResult=db.update(TABLE_INFORMATION_SAVE_DATA,values,"user_id='"+userId+"' and images='"+imageName+"' and species='"+fromSpecies+"'",null);
        close();
        return updateResult;
    }

    public int renameSpeciesLocal(String imageName, String fromSpecies, String toSpecies){
        int updateResult = 0;
        openDatabase();
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues values=new ContentValues();

        values.put(COLUMN_INFO_SPECIES,toSpecies);
        values.put(COLUMN_INFO_UPDATE_INFO,"From "+fromSpecies+" To "+toSpecies);

        updateResult=db.update(TABLE_INFORMATION_SAVE_DATA,values,"images='"+imageName+"' and species='"+fromSpecies+"'",null);
        close();
        return updateResult;
    }

    //get total uploaded info count
    public int getTotalUploadedData(String userId){
        String countQuery = "SELECT * FROM " + TABLE_INFORMATION_SAVE_DATA +" WHERE "+ COLUMN_INFO_USERID + "='" + userId + "'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int cnt = cursor.getCount();
        cursor.close();
        return cnt;
    }

    public int getTotalALLUploadedData(){
        String countQuery = "SELECT * FROM " + TABLE_ALL_INFORMATION_SAVE_DATA;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int cnt = cursor.getCount();
        cursor.close();
        Log.d("Total upload in local : ","Total upload in local : "+cnt);
        return cnt;
    }

    public int deleteFromLocal(String userId,String imageName){
        openDatabase();
        int i = -1;
        i = sqLiteDatabase.delete(TABLE_INFORMATION_SAVE_TO_LATER,COLUMN_INFO_USERID  + "=? AND " +COLUMN_INFO_IMAGES + "=?",new String[] {userId,imageName});
        close();
        return i;
    }

    public int deleteSaveDataFromLocal(String userId,String imageName){
        openDatabase();
        int i = -1;
        i = sqLiteDatabase.delete(TABLE_INFORMATION_SAVE_DATA,COLUMN_INFO_USERID  + "=? AND " +COLUMN_INFO_IMAGES + "=?",new String[] {userId,imageName});
        close();
        return i;
    }

   public ArrayList<String> getSpeciesNames() {
        openDatabase();
        ArrayList<String> speciesList = new ArrayList<>();
        String selectQuery = "SELECT " + COLUMN_INFO_SPECIES + " FROM " + TABLE_INFORMATION_SAVE_DATA;
        Cursor cursor = sqLiteDatabase.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                speciesList.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        close();
        return speciesList;
    }
    /*Delete all table*/
    public void removeAllTableSaveToLater() {
        openDatabase();
        sqLiteDatabase.delete(TABLE_INFORMATION_SAVE_TO_LATER, null, null);
        close();
    }
    public void removeSaveDataFromTable() {
        openDatabase();
        sqLiteDatabase.delete(TABLE_INFORMATION_SAVE_DATA, null, null);
        close();
    }
    public void removeAllSaveDataFromTable() {
        openDatabase();
        sqLiteDatabase.delete(TABLE_ALL_INFORMATION_SAVE_DATA, null, null);
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

    public boolean isDataAvialableInLocalDb(SubmitRequest submitRequest){
        List<SubmitRequest> submitRequestList = getImageInfoToUpload(submitRequest.getUserId());
        if(submitRequestList != null && submitRequestList.size() > 0){
            for (int i =0;i<submitRequestList.size();i++){
                if(submitRequestList.get(i) != null){
                    if(  submitRequestList.get(i).getImageName().equals(submitRequest.getImageName())
                            &&  submitRequestList.get(i).getUserId().equals(submitRequest.getUserId())
                            &&  submitRequestList.get(i).getImageUrl().equals(submitRequest.getImageUrl())
                            &&  submitRequestList.get(i).getSpecies().equals(submitRequest.getSpecies())
                            &&  submitRequestList.get(i).getTime().equals(submitRequest.getTime()))
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    //get image from species name
    public List<String> getImagesFromSpecies(String speciesImage) {
        openDatabase();
        List<String> imagesArray = new ArrayList<>();
        String selectQuery = "SELECT " + COLUMN_INFO_IMAGES_URL + " FROM " + TABLE_INFORMATION_SAVE_TO_LATER + " WHERE " + COLUMN_INFO_IMAGES + "='" + speciesImage + "'";
        Cursor cursor = sqLiteDatabase.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                imagesArray.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        close();
        return imagesArray;
    }
}
