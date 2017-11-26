/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package br.rodrigofagundes.inventory;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import br.rodrigofagundes.inventory.data.ProductContract.ProductEntry;

/**
 * Allows user to create a new product or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {
    private CheckBox mCheckFollow;

    private boolean mProductHasChanged = false;

    private static final int EXISTING_PRODUCT_LOADER = 0;
    //private ProductCursorAdapter pca;
    private Uri currentUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        currentUri = getIntent().getData();

        if (currentUri == null) {
            setTitle(getString(R.string.editor_activity_title_new_product));

            invalidateOptionsMenu();
        } else {
            setTitle(getString(R.string.editor_activity_title_edit_product));
            //pca = new ProductCursorAdapter(this, null);
            getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null,
                    this);
        }

        mCheckFollow = (CheckBox)findViewById(R.id.edit_product_follow);

        ((EditText)findViewById(R.id.edit_product_name)).setOnTouchListener(mTouchListener);
        ((EditText)findViewById(R.id.edit_product_price)).setOnTouchListener(mTouchListener);
        ((EditText)findViewById(R.id.edit_product_quantity)).setOnTouchListener(mTouchListener);
        mCheckFollow.setOnTouchListener(mTouchListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                saveProduct();
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // Se o product não mudou, continua navegando pra cima para a activity pai
                // que é a {@link MainActivity}.
                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Se há alterações não salvas, configura um dialog para alertar o usuário.
                // Cria um click listener para lidar com o usuário confirmando que
                // mudanças devem ser descartadas.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // Usuário clidou no botão "Discard", e navegou para a activity pai.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Mostra um dialog que notifica o usuário que eles tem alterações não salvas
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveProduct() {
        if (currentUri == null && isBlank()) return;

        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String nameString = ((EditText) findViewById(R.id.edit_product_name)).getText()
                .toString().trim();
        String priceString = ((EditText) findViewById(R.id.edit_product_price)).getText()
                .toString().trim();
        String quantityString = ((EditText) findViewById(R.id.edit_product_quantity)).getText()
                .toString().trim();
        Boolean isFollowing = ((CheckBox) findViewById(R.id.edit_product_follow)).isChecked();

        double price = Integer.parseInt(priceString) / 100;
        int quantity = Integer.parseInt(quantityString);
        int following = isFollowing ? 1 : 0;

        // Create a ContentValues object where column names are the keys,
        // and product attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_NAME, nameString);
        values.put(ProductEntry.COLUMN_PRODUCT_PRICE, price);
        values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, quantity);
        values.put(ProductEntry.COLUMN_PRODUCT_FOLLOW, following);

        // Insert a new row for product in the database, returning the ID of that new row.
        if (currentUri == null) {
            Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            int rowsAffected = getContentResolver().update(currentUri, values, null,
                    null);
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
    }

    private boolean isBlank() {
        if (TextUtils.isEmpty(((EditText) findViewById(R.id.edit_product_name)).getText()) &&
                TextUtils.isEmpty(((EditText) findViewById(R.id.edit_product_price)).getText()) &&
                TextUtils.isEmpty(((EditText) findViewById(R.id.edit_product_quantity)).getText()) &&
                !mCheckFollow.isChecked()) {
            return true;
        }
        return false;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductEntry.COLUMN_PRODUCT_FOLLOW
        };

        return new CursorLoader(this, currentUri, projection,
                null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data == null || data.getCount() < 1) {
            return;
        }

        if (data.moveToFirst()) {
            ((EditText) findViewById(R.id.edit_product_name)).setText(data.getString(data
                    .getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME)));
            ((EditText) findViewById(R.id.edit_product_price)).setText(String.valueOf(data.
                    getInt(data.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE))));
            ((EditText) findViewById(R.id.edit_product_quantity)).setText(String.valueOf(data.
                    getInt(data.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY))));
            mCheckFollow.setChecked(data.getInt(data.getColumnIndex(ProductEntry
                    .COLUMN_PRODUCT_FOLLOW)) == 1);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        ((EditText) findViewById(R.id.edit_product_name)).setText("");
        ((EditText) findViewById(R.id.edit_product_price)).setText("");
        ((EditText) findViewById(R.id.edit_product_quantity)).setText("");
        ((CheckBox) findViewById(R.id.edit_product_follow)).setChecked(false);
    }

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductHasChanged = true;
            return false;
        }
    };

    @Override
    public void onBackPressed() {
        // Se o product não mudou, continue lidando com clique do botão back
        if (!mProductHasChanged) {
            super.onBackPressed();
            return;
        }

        // Caso contrário se há alterações não salvas, configure um dialog para alertar o usuário.
        // Crie um click listener para lidar com o usuário confirmando que mudanças devem ser descartadas.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicou no botão "Discard", fecha a activity atual.
                        finish();
                    }
                };

        // Mostra dialog dizendo que há mudanças não salvas
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        // Cria um AlertDialog.Builder e seta a mensagem, e click listeners
        // para os botões positivos e negativos do dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicou no botão "Continuar editando", então feche o dialog
                // e continue editando o product.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Cria e mostra o AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new product, hide the "Delete" menu item.
        if (currentUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the product.
                deleteProduct();
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
    private void deleteProduct() {
        int rowsAffected = getContentResolver().delete(currentUri, null, null);
        if (rowsAffected != 0) {
            Toast.makeText(this, getString(R.string.editor_delete_product_successful), Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, getString(R.string.editor_delete_product_failed), Toast.LENGTH_SHORT).show();
        }
    }
}