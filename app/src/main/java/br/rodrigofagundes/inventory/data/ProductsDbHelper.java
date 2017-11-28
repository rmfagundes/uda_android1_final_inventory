package br.rodrigofagundes.inventory.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by rmfagundes on 21/11/2017.
 */

public class ProductsDbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "inventory.db";

    private static final String TEXT_TYPE = " TEXT";
    private static final String INTEGER_TYPE = " INTEGER";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + ProductContract.ProductEntry.TABLE_NAME + " (" +
                    ProductContract.ProductEntry._ID + " INTEGER PRIMARY KEY," +
                    ProductContract.ProductEntry.COLUMN_PRODUCT_NAME + TEXT_TYPE + COMMA_SEP +
                    ProductContract.ProductEntry.COLUMN_PRODUCT_SUPPLIER + TEXT_TYPE + COMMA_SEP +
                    ProductContract.ProductEntry.COLUMN_PRODUCT_SUPPLIER_EMAIL + TEXT_TYPE + COMMA_SEP +
                    ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY + INTEGER_TYPE + COMMA_SEP +
                    ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE + INTEGER_TYPE + COMMA_SEP +
                    ProductContract.ProductEntry.COLUMN_PRODUCT_FOLLOW + INTEGER_TYPE + " )";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + ProductContract.ProductEntry.TABLE_NAME;

    public ProductsDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
