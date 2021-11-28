package com.apps4sj.TwitterSeller;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SQLConnection extends SQLiteOpenHelper { // https://www.javatpoint.com/android-sqlite-tutorial

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "TwitterSeller";
    private static final String TABLE_LISTINGS = "listings";
    private static final String KEY_ID = "id";
    private static final String KEY_PROD_NAME = "product_name";
    private static final String KEY_PROD_PRICE = "price";
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
                + KEY_PROD_PRICE + " TEXT," + KEY_DATE_POST + " TEXT" + ")";
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

    // code to add the new contact
    public void addListing(Listing l) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ID, l.getId());
        values.put(KEY_PROD_NAME, l.getProductName());
        values.put(KEY_PROD_PRICE, l.getProductPrice());
        values.put(KEY_DATE_POST, l.getDatePosted());

        // Inserting Row
        db.insert(TABLE_LISTINGS, null, values);
        //2nd argument is String containing nullColumnHack
        db.close(); // Closing database connection
    }

    public Listing getListing(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_LISTINGS, new String[] { KEY_ID,
                        KEY_PROD_NAME, KEY_PROD_PRICE, KEY_DATE_POST }, KEY_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        Listing listing = new Listing(cursor.getString(0), cursor.getString(1),
                cursor.getString(2), cursor.getString(3));
        return listing;
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
                        cursor.getString(2), cursor.getString(3));
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
