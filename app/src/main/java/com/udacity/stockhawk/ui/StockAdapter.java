package com.udacity.stockhawk.ui;


import android.content.Context;
import android.database.Cursor;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

class StockAdapter extends RecyclerView.Adapter<StockAdapter.StockViewHolder> {

    private final Context context;
    private final DecimalFormat dollarFormatWithPlus;
    private final DecimalFormat dollarFormat;
    private final DecimalFormat percentageFormat;
    private Cursor cursor;
    private final StockAdapterOnClickHandler clickHandler;

    private final static int TYPE_STOCK = 0;
    private final static int TYPE_NO_STOCK = 1;

    /**
     * Just so that the user doesn't see a cascade of toasts while clicking invalid stock.
     */
    Toast mFeedbackToast;

    StockAdapter(Context context, StockAdapterOnClickHandler clickHandler) {
        this.context = context;
        this.clickHandler = clickHandler;

        dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        dollarFormatWithPlus.setPositivePrefix("+$");
        percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
        percentageFormat.setMaximumFractionDigits(2);
        percentageFormat.setMinimumFractionDigits(2);
        percentageFormat.setPositivePrefix("+");
    }

    void setCursor(Cursor cursor) {
        this.cursor = cursor;
        notifyDataSetChanged();
    }

    String getSymbolAtPosition(int position) {
        cursor.moveToPosition(position);
        return cursor.getString(Contract.Quote.POSITION_SYMBOL);
    }

    @Override
    public StockViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View item;

        switch (viewType) {
            case TYPE_STOCK:
                item = LayoutInflater.from(context).inflate(R.layout.list_item_quote, parent, false);
                break;
            case TYPE_NO_STOCK:
                item = LayoutInflater.from(context).inflate(R.layout.list_item_no_quote, parent,
                        false);
                break;
            default:
                throw new UnsupportedOperationException("Unknown view type!");
        }

        return new StockViewHolder(item);
    }

    @Override
    public void onBindViewHolder(StockViewHolder holder, int position) {

        cursor.moveToPosition(position);
        int type = getItemViewType(position);

        holder.symbol.setText(cursor.getString(Contract.Quote.POSITION_SYMBOL));

        if (type == TYPE_STOCK) {
            //noinspection ConstantConditions
            holder.price.setText(dollarFormat.format(cursor.getFloat(Contract.Quote.POSITION_PRICE)));

            float rawAbsoluteChange = cursor.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
            float percentageChange = cursor.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE);

            if (rawAbsoluteChange > 0) {
                //noinspection ConstantConditions
                holder.change.setBackgroundResource(R.drawable.percent_change_pill_green);
            } else {
                //noinspection ConstantConditions
                holder.change.setBackgroundResource(R.drawable.percent_change_pill_red);
            }

            String change = dollarFormatWithPlus.format(rawAbsoluteChange);
            String percentage = percentageFormat.format(percentageChange / 100);

            if (PrefUtils.getDisplayMode(context)
                    .equals(context.getString(R.string.pref_display_mode_absolute_key))) {
                holder.change.setText(change);
            } else {
                holder.change.setText(percentage);
            }
        }
    }

    @Override
    public int getItemCount() {
        int count = 0;
        if (cursor != null) {
            count = cursor.getCount();
        }
        return count;
    }


    interface StockAdapterOnClickHandler {
        void onClick(String symbol, View sharedSymbolElement);
    }

    /**
     * returns which type of view should be displayed, error or no error.
     *
     * @param position which position on the cursor we are on.
     * @return the type
     */
    @Override
    public int getItemViewType(int position) {
        cursor.moveToPosition(position);
        if (cursor.getInt(Contract.Quote.POSITION_IS_REAL) == 1)
            return TYPE_STOCK;
        else
            return TYPE_NO_STOCK;
    }

    class StockViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.symbol)
        TextView symbol;

        /**
         * Displays the price, maybe null if there is displaying error view.
         */
        @Nullable
        @BindView(R.id.price)
        TextView price;

        /**
         * The change since last quote, maybe null if displaying no stock view.
         */
        @Nullable
        @BindView(R.id.change)
        TextView change;

        StockViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            int type = StockAdapter.this.getItemViewType(adapterPosition);
            if (type == TYPE_NO_STOCK) {
                if (mFeedbackToast != null)
                    mFeedbackToast.cancel();
                mFeedbackToast = Toast.makeText(context, R.string.hint_not_a_real_stock,
                        Toast.LENGTH_SHORT);
                mFeedbackToast.show();
            } else {
                cursor.moveToPosition(adapterPosition);
                int symbolColumn = cursor.getColumnIndex(Contract.Quote.COLUMN_SYMBOL);
                clickHandler.onClick(cursor.getString(symbolColumn), symbol);
            }
        }
    }
}
