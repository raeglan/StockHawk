package com.udacity.stockhawk.widget;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.widget.RemoteViewsService;

import static android.appwidget.AppWidgetManager.*;

/**
 * @author Rafael
 * @since 18.05.2017
 */

public class StockWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return (new StockRemoteViewFactory(this.getApplicationContext()));
    }
}
