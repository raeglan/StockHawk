package com.udacity.stockhawk.ui;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.udacity.stockhawk.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Rafael
 * @since 16.05.2017
 */

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    @BindView(R.id.symbol)
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

    @BindView(R.id.rv_history)
    RecyclerView mHistoryRecyclerView;

    /**
     * The stock uri that should be loaded.
     */
    Uri mStockUri;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);

        mStockUri = getIntent().getData();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, mStockUri, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

    }

    /**
     * We aren't doing anything here because the action of reading from the database is not going to
     * take long.
     * @param loader
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }
}
