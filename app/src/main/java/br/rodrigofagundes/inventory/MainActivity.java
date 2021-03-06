package br.rodrigofagundes.inventory;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import br.rodrigofagundes.inventory.data.ProductContract.ProductEntry;

public class MainActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int PRODUCT_LOADER = 0;
    private ProductCursorAdapter mProductCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // Encontre o ListView que será preenchido com os dados do product
        ListView productListView = (ListView) findViewById(R.id.list_view_product);

        // Localizar e definir view vazia no ListView, para que ele só mostre quando a lista conter 0 itens.
        View emptyView = findViewById(R.id.empty_view);
        productListView.setEmptyView(emptyView);

        mProductCursorAdapter = new ProductCursorAdapter(this, null);
        productListView.setAdapter(mProductCursorAdapter);

        getLoaderManager().initLoader(PRODUCT_LOADER, null, this);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertProduct();
                // Do nothing for now
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                showDeleteConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void insertProduct() {
        // Create a ContentValues object where column names are the keys,
        // and Toto's product attributes are the values.
        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_NAME, "Imaginary Product");
        values.put(ProductEntry.COLUMN_PRODUCT_PRICE, 24900);
        values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, 200);
        values.put(ProductEntry.COLUMN_PRODUCT_FOLLOW, 1);

        getContentResolver().insert(ProductEntry.CONTENT_URI, values);
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_all_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the product.
                deleteProducts();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the product.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the product in the database.
     */
    private void deleteProducts() {
        int rowsAffected = getContentResolver().delete(ProductEntry.CONTENT_URI, null, null);
        if (rowsAffected != 0) {
            Toast.makeText(this, getString(R.string.list_delete_products_successful), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.list_delete_products_failed), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_PRODUCT_IMAGE};

        return new CursorLoader(this, ProductEntry.CONTENT_URI, projection,
                null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mProductCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mProductCursorAdapter.swapCursor(null);
    }

    public void subtractOne(int currentId, int amnt) {
        if (amnt >= 0) {
            ((TextView)findViewById(R.id.quantity)).setText(String.valueOf(amnt));

            // Loads the complete cursor
            String[] projection = {
                    ProductEntry._ID,
                    ProductEntry.COLUMN_PRODUCT_NAME,
                    ProductEntry.COLUMN_PRODUCT_SUPPLIER,
                    ProductEntry.COLUMN_PRODUCT_SUPPLIER_EMAIL,
                    ProductEntry.COLUMN_PRODUCT_PRICE,
                    ProductEntry.COLUMN_PRODUCT_QUANTITY,
                    ProductEntry.COLUMN_PRODUCT_FOLLOW
            };

            Cursor data = getContentResolver().query(ContentUris.withAppendedId(
                    ProductEntry.CONTENT_URI, currentId), projection, null,
                    null, null);

            if (data.moveToFirst()) {
                ContentValues values = new ContentValues();
                values.put(ProductEntry.COLUMN_PRODUCT_NAME,
                        data.getString(data.getColumnIndex(
                                ProductEntry.COLUMN_PRODUCT_NAME)));
                values.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER,
                        data.getString(data.getColumnIndex(
                                ProductEntry.COLUMN_PRODUCT_SUPPLIER)));
                values.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER_EMAIL,
                        data.getString(data.getColumnIndex(
                                ProductEntry.COLUMN_PRODUCT_SUPPLIER_EMAIL)));
                values.put(ProductEntry.COLUMN_PRODUCT_PRICE,
                        data.getInt(data.getColumnIndex(
                                ProductEntry.COLUMN_PRODUCT_PRICE)));
                values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, amnt);
                values.put(ProductEntry.COLUMN_PRODUCT_FOLLOW,
                        data.getInt(data.getColumnIndex(
                                ProductEntry.COLUMN_PRODUCT_FOLLOW)));

                int rowsAffected = getContentResolver().update(
                        ContentUris.withAppendedId(ProductEntry.CONTENT_URI, currentId),
                        values, null,null);
                // Mostra uma mensagem toast dependendo se ou não a atualização foi bem-sucedida.
                if (rowsAffected == 0) {
                    // Se nenhuma linha foi afetada, então houve um erro com o update.
                    Toast.makeText(this, getString(R.string.editor_update_product_failed),
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Caso contrário, o update foi bem sucedido e podemos mostrar um toast.
                    Toast.makeText(this, getString(R.string.editor_update_product_successful),
                            Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(this, getString(R.string.editor_stock_short),
                    Toast.LENGTH_SHORT).show();
        }
    }
}
