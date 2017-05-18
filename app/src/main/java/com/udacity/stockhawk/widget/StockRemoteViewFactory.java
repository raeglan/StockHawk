package com.udacity.stockhawk.widget;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * @author Rafael
 * @since 18.05.2017
 */
class StockRemoteViewFactory implements RemoteViewsService.RemoteViewsFactory {

    private Cursor mCursor;

    private Context mContext;

    private final DecimalFormat dollarFormatWithPlus;
    private final DecimalFormat dollarFormat;
    private final DecimalFormat percentageFormat;


    StockRemoteViewFactory(Context context) {
        mContext = context;
        dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        dollarFormatWithPlus.setPositivePrefix("+$");
        percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
        percentageFormat.setMaximumFractionDigits(2);
        percentageFormat.setMinimumFractionDigits(2);
        percentageFormat.setPositivePrefix("+");
    }

    @Override
    public void onCreate() {
        ContentResolver contentResolver = mContext.getContentResolver();
        String selection = Contract.Quote.COLUMN_IS_REAL + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(1)};
        mCursor = contentResolver.query(Contract.Quote.URI, null, selection, selectionArgs, null);
    }

    @Override
    public void onDataSetChanged() {
    }

    @Override
    public void onDestroy() {
        mContext = null;
        mCursor.close();
    }

    @Override
    public int getCount() {
        return mCursor == null ? 0 : mCursor.getCount();
    }

    /**
     * Mimicking the adapter it sets all the stock values.
     * @param position the position to move the adapter
     * @return the remote views to be updated
     */
    @Override
    public RemoteViews getViewAt(int position) {
        mCursor.moveToPosition(position);

        // like the inflater, but worse
        RemoteViews rView = new RemoteViews(mContext.getPackageName(), R.layout.list_item_widget_stock);

        // the symbol
        rView.setTextViewText(R.id.symbol, mCursor.getString(Contract.Quote.POSITION_SYMBOL));

        rView.setTextViewText(R.id.price,
                dollarFormat.format(mCursor.getFloat(Contract.Quote.POSITION_PRICE)));

        float rawAbsoluteChange = mCursor.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
        float percentageChange = mCursor.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE);

        if (rawAbsoluteChange > 0) {
            rView.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_green);
        } else {
            rView.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_red);
        }

        String change = dollarFormatWithPlus.format(rawAbsoluteChange);
        String percentage = percentageFormat.format(percentageChange / 100);

        if (PrefUtils.getDisplayMode(mContext)
                .equals(mContext.getString(R.string.pref_display_mode_absolute_key))) {
            rView.setTextViewText(R.id.change, change);
        } else {
            rView.setTextViewText(R.id.change, percentage);
        }
        return rView;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        mCursor.moveToPosition(position);
        int idIndex = mCursor.getColumnIndex(Contract.Quote._ID);
        return mCursor.getInt(idIndex);
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
