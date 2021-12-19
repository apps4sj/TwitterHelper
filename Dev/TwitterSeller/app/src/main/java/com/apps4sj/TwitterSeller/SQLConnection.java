package com.apps4sj.TwitterSeller;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SQLConnection extends SQLiteOpenHelper { // https://www.javatpoint.com/android-sqlite-tutorial

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "TwitterSeller";
    private static final String TABLE_LISTINGS = "listings";
    private static final String KEY_ID = "id";
    private static final String KEY_PROD_NAME = "product_name";
    private static final String KEY_PROD_DESC = "product_description";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_LOCATION = "location";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_PROD_PRICE = "price";
    private static final String KEY_IMAGE1 = "image1";
    private static final String KEY_IMAGE2 = "image2";
    private static final String KEY_IMAGE3 = "image3";
    private static final String KEY_DATE_POST = "date_posted";


    public SQLConnection(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        //3rd argument to be passed is CursorFactory instance
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_LISTINGS_TABLE = "CREATE TABLE " + TABLE_LISTINGS + "("
                + KEY_ID + " TEXT PRIMARY KEY," + KEY_PROD_NAME + " TEXT,"
                + KEY_PROD_DESC + " TEXT," + KEY_EMAIL + " TEXT," + KEY_LOCATION
                + " TEXT," + KEY_PHONE + " TEXT," + KEY_PROD_PRICE + " TEXT," +
                KEY_IMAGE1 + " TEXT," + KEY_IMAGE2 + " TEXT," + KEY_IMAGE3 + " TEXT," +
                KEY_DATE_POST + " TEXT" + ")";
        db.execSQL(CREATE_LISTINGS_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LISTINGS);

        // Create tables again
        onCreate(db);
    }

    public void createTable() {
        SQLiteDatabase db = this.getWritableDatabase();

        String CREATE_LISTINGS_TABLE = "CREATE TABLE " + TABLE_LISTINGS + "("
                + KEY_ID + " TEXT PRIMARY KEY," + KEY_PROD_NAME + " TEXT,"
                + KEY_PROD_DESC + " TEXT," + KEY_EMAIL + " TEXT," + KEY_LOCATION
                + " TEXT," + KEY_PHONE + " TEXT," + KEY_PROD_PRICE + " TEXT," +
                KEY_IMAGE1 + " TEXT," + KEY_IMAGE2 + " TEXT," + KEY_IMAGE3 + " TEXT," +
                KEY_DATE_POST + " TEXT" + ")";
        db.execSQL(CREATE_LISTINGS_TABLE);
    }

    // code to add the new contact
    public void addListing(Listing l) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ID, l.getId());
        values.put(KEY_PROD_NAME, l.getProductName());
        values.put(KEY_PROD_DESC, l.getProductDescription());
        values.put(KEY_EMAIL, l.getEmail());
        values.put(KEY_LOCATION, l.getLocation());
        values.put(KEY_PHONE, l.getPhone());
        values.put(KEY_PROD_PRICE, l.getPrice());
        values.put(KEY_IMAGE1, l.getImage1());
        values.put(KEY_IMAGE2, l.getImage2());
        values.put(KEY_IMAGE3, l.getImage3());
        values.put(KEY_DATE_POST, l.getDatePosted());

        // Inserting Row
        db.insert(TABLE_LISTINGS, null, values);
        //2nd argument is String containing nullColumnHack
        db.close(); // Closing database connection
    }

    public Listing getListing(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_LISTINGS, new String[] { KEY_ID,
                        KEY_PROD_NAME, KEY_PROD_DESC, KEY_EMAIL, KEY_LOCATION, KEY_PHONE,
                        KEY_PROD_PRICE, KEY_IMAGE1, KEY_IMAGE2, KEY_IMAGE3, KEY_DATE_POST }, KEY_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        Listing listing = new Listing(cursor.getString(0), cursor.getString(1),
                cursor.getString(2), cursor.getString(3),
                cursor.getString(4), cursor.getString(5),
                cursor.getString(6), cursor.getString(7),
                cursor.getString(8), cursor.getString(9), cursor.getString(10));
        return listing;
    }

    public String getListingJSON(int id) {
        Listing l = getListing(id);
        try {
            JSONObject saveInstance = new JSONObject();
            saveInstance.put("itemName", l.getProductName());
            saveInstance.put("price", l.getPrice());
            saveInstance.put("description", l.getProductDescription());
            saveInstance.put("location", l.getLocation());

            JSONObject contact = new JSONObject();
            contact.put("email", l.getEmail());
            contact.put("phoneNum", l.getPhone());
            saveInstance.put("contact", contact);

            if (!l.getImage1().equals("")) {
                saveInstance.put("image0", l.getImage1());
            }
            if (!l.getImage2().equals("")) {
                saveInstance.put("image1", l.getImage2());
            }
            if (!l.getImage3().equals("")) {
                saveInstance.put("image2", l.getImage3());
            }
            return saveInstance.toString();
        } catch (Exception e){
            return "";
        }

    }

    // code to get all contacts in a list view
    public List<Listing> getAllListings() {
        List<Listing> listingList = new ArrayList<Listing>();

        String selectQuery = "SELECT  * FROM " + TABLE_LISTINGS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Listing listing = new Listing(cursor.getString(0), cursor.getString(1),
                        cursor.getString(2), cursor.getString(3),
                        cursor.getString(4), cursor.getString(5),
                        cursor.getString(6), cursor.getString(7),
                        cursor.getString(8), cursor.getString(9), cursor.getString(10));
                listingList.add(listing);
            } while (cursor.moveToNext());
        }

        return listingList;
    }

    public boolean idExists(String id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_LISTINGS, new String[] { KEY_ID,
                        KEY_PROD_NAME, KEY_PROD_PRICE, KEY_DATE_POST }, KEY_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);

        return cursor != null;
    }

    public void deleteListing(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_LISTINGS, KEY_ID + " = ?",
                new String[] { id });

        db.close();
    }

    public void printAllListings() {
        List<Listing> listings = getAllListings();
        for (Listing l : listings) {
            System.out.println(l);
        }
    }

    public void dropTable() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LISTINGS);
        db.close();
    }

}
