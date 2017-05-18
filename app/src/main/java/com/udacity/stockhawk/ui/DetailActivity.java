package com.udacity.stockhawk.ui;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Displays the detailed information about a selected stock.
 *
 * @author Rafael
 * @since 16.05.2017
 */
public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = DetailActivity.class.getSimpleName();

    @BindView(R.id.tv_symbol)
    TextView mSymbolTextView;

    /**
     * Displays the price, maybe null if there is displaying error view.
     */
    @BindView(R.id.price)
    TextView mPriceTextView;

    /**
     * The change since last quote, maybe null if displaying no stock view.
     */
    @BindView(R.id.change)
    TextView mChangeTextView;

    @BindView(R.id.graph)
    GraphView mHistoryGraphView;

    /**
     * The stock uri that should be loaded.
     */
    Uri mStockUri;

    private static final String KEY_STOCK_URI = "STOCK_URI";

    /**
     * So that we can swap between absolute and percentage even here!
     */
    private String mChange, mPercentage;

    private DecimalFormat dollarFormatWithPlus;
    private DecimalFormat dollarFormat;
    private DecimalFormat percentageFormat;
    private DateFormat dateFormat;

    /**
     * Our  elite loader id.
     */
    private static final int LOADER_ID = 1337;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);

        // getting data
        mStockUri = getIntent().getData();
        String symbol = getIntent().getAction();

        // home as up
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        // checking if everything is here
        if ((mStockUri == null || symbol == null) && savedInstanceState == null) {
            Log.e(TAG, "Detail activity started without data!");
            Toast.makeText(this, R.string.error_no_detail, Toast.LENGTH_SHORT).show();
            finish();
        } else {

            if (savedInstanceState != null) {
                mStockUri = Uri.parse(savedInstanceState.getString(KEY_STOCK_URI));
                symbol = mStockUri.getLastPathSegment();
            }

            // just setting the symbol
            mSymbolTextView.setText(symbol);

            // making formatting looks beautiful!
            dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
            dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
            dollarFormatWithPlus.setPositivePrefix(getString(R.string.dollar_with_plus));
            percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
            percentageFormat.setMaximumFractionDigits(2);
            percentageFormat.setMinimumFractionDigits(2);
            percentageFormat.setPositivePrefix(getString(R.string.plus_symbol));
            dateFormat = android.text.format.DateFormat.getDateFormat(this);

            // last but not least, starting the loader
            getSupportLoaderManager().initLoader(LOADER_ID, null, this);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_STOCK_URI, mStockUri.toString());
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, mStockUri, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data == null || data.getCount() <= 0) {
            Log.e(TAG, "The cursor with detailed data returned null for: " + mStockUri);
            Toast.makeText(this, R.string.error_no_detail, Toast.LENGTH_SHORT).show();
            finish();
        } else {

            data.moveToPosition(0);

            //noinspection ConstantConditions
            mPriceTextView.setText(dollarFormat.format(data.getFloat(Contract.Quote.POSITION_PRICE)));

            float rawAbsoluteChange = data.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
            float percentageChange = data.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE);

            if (rawAbsoluteChange > 0) {
                //noinspection ConstantConditions
                mChangeTextView.setBackgroundResource(R.drawable.percent_change_pill_green);
            } else {
                //noinspection ConstantConditions
                mChangeTextView.setBackgroundResource(R.drawable.percent_change_pill_red);
            }

            mChange = dollarFormatWithPlus.format(rawAbsoluteChange);
            mPercentage = percentageFormat.format(percentageChange / 100);

            if (PrefUtils.getDisplayMode(this)
                    .equals(this.getString(R.string.pref_display_mode_absolute_key))) {
                mChangeTextView.setText(mChange);
            } else {
                mChangeTextView.setText(mPercentage);
            }

            // setting the  contents for the graph.
            String[] history = data.getString(Contract.Quote.POSITION_HISTORY).split("\n");

            if (history.length > 0) {

                // sort the list, so that no matter what Yahoo decides to do it should always work.
                Arrays.sort(history, new Comparator<String>() {
                    @Override
                    public int compare(String o1, String o2) {
                        long t1 = parseFirstLong(o1);
                        long t2 = parseFirstLong(o2);
                        return Long.compare(t1, t2);
                    }

                });

                LineGraphSeries<DataPoint> series = new LineGraphSeries<>();

                long minDate = parseFirstLong(history[0]);
                long maxDate = parseFirstLong(history[history.length - 1]);

                /// I invert the order so that the first X value comes in first place.
                for (String aHistory : history) {
                    String[] components = aHistory.split(",");
                    long timeInMs = Long.parseLong(components[0].trim());
                    double value = Double.parseDouble(components[1].trim());
                    series.appendData(new DataPoint(new Date(timeInMs), value),
                            true, history.length + 1);
                }

                // making the graph
                mHistoryGraphView.addSeries(series);

                // set date label formatter
                mHistoryGraphView.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(this));
                mHistoryGraphView.getGridLabelRenderer().setNumHorizontalLabels(3);

                // set manual x bounds to have nice steps
                mHistoryGraphView.getViewport().setMinX(minDate);
                mHistoryGraphView.getViewport().setMaxX(maxDate);
                mHistoryGraphView.getViewport().setXAxisBoundsManual(true);

                // as we use dates as labels, the human rounding to nice readable numbers
                // is not necessary
                mHistoryGraphView.getGridLabelRenderer().setHumanRounding(false);
            }
        }
    }

    /**
     * Parses the time in millis from our String.
     *
     * @param norbert the string, which should contain a long before the comma.
     * @return the long.
     */
    private long parseFirstLong(String norbert) {
        String[] components = norbert.split(",");
        return Long.parseLong(components[0].trim());
    }

    /**
     * We aren't doing anything here because the action of reading from the database is not going to
     * take long.
     *
     * @param loader the loader, duh.
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    private void setDisplayModeMenuItemIcon(MenuItem item) {
        if (PrefUtils.getDisplayMode(this)
                .equals(getString(R.string.pref_display_mode_absolute_key))) {
            item.setIcon(R.drawable.ic_percentage);
            item.setTitle(R.string.action_change_units_percentage);
        } else {
            item.setIcon(R.drawable.ic_dollar);
            item.setTitle(R.string.action_change_units_absolute);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_settings, menu);
        MenuItem item = menu.findItem(R.id.action_change_units);
        setDisplayModeMenuItemIcon(item);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                supportFinishAfterTransition();
                return true;
            case R.id.action_change_units:
                PrefUtils.toggleDisplayMode(this);
                setDisplayModeMenuItemIcon(item);
                if (mChange != null && mPercentage != null) {
                    if (PrefUtils.getDisplayMode(this)
                            .equals(this.getString(R.string.pref_display_mode_absolute_key))) {
                        mChangeTextView.setText(mChange);
                    } else {
                        mChangeTextView.setText(mPercentage);
                    }
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
