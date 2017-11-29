package br.rodrigofagundes.inventory;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.text.NumberFormat;
import java.util.Locale;

import br.rodrigofagundes.inventory.data.ProductContract.ProductEntry;
import br.rodrigofagundes.inventory.data.ProductProvider;

/**
 * Created by rmfagundes on 23/11/2017.
 */

public class ProductCursorAdapter extends CursorAdapter {
    /**
     * Constructs a new {@link ProductCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public ProductCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the product data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current product can be set on the name
     * TextView in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        String currentName = cursor.getString(cursor.getColumnIndex(
                ProductEntry.COLUMN_PRODUCT_NAME));
        final String currentAmount = String.valueOf(cursor.getInt(cursor.getColumnIndex(
                ProductEntry.COLUMN_PRODUCT_QUANTITY)));

        double currentPrice = cursor.getInt(cursor.getColumnIndex(
                ProductEntry.COLUMN_PRODUCT_PRICE)) / 100;

        final int currentId = cursor.getInt(cursor.getColumnIndex(ProductEntry._ID));

        Locale ptBr = new Locale("pt", "BR");
        String price = NumberFormat.getCurrencyInstance(ptBr).format(currentPrice);

        ((TextView)view.findViewById(R.id.name)).setText(currentName);
        ((TextView)view.findViewById(R.id.quantity)).setText(currentAmount);
        ((TextView)view.findViewById(R.id.price)).setText(price);

        Button btnSell = (Button)view.findViewById(R.id.sell);
        btnSell.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int amnt = Integer.valueOf(currentAmount) - 1;
                    ((MainActivity)view.getContext()).subtractOne(currentId, amnt);
                }
            }
        );
    }
}
