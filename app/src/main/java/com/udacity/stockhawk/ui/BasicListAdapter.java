package com.udacity.stockhawk.ui;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * As I didn't want to use the normal list view I had to implement the most boiler plate of codes,
 * for a String list... oh well, at least let's make this good.
 *
 * And it ended up not being needed, oh well...
 *
 * @author Rafael
 * @since 18.05.2017
 */
public class BasicListAdapter extends RecyclerView.Adapter<BasicListAdapter.ListViewHolder> {

    private List<String> mItems;

    @Override
    public ListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ListViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ListViewHolder holder, int position) {
        holder.mText1.setText(mItems.get(position));
    }

    @Override
    public int getItemCount() {
        return mItems == null ? 0 : mItems.size();
    }

    /**
     * Swaps all the items of the list.
     *
     * @param items the new list which should be displayed.
     */
    public void swapItems(List<String> items) {
        mItems = items;
        notifyDataSetChanged();
    }

    class ListViewHolder extends RecyclerView.ViewHolder {

        @BindView(android.R.id.text1)
        TextView mText1;

        ListViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
