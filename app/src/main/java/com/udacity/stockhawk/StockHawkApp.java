package com.udacity.stockhawk;

import android.app.Application;

import timber.log.Timber;

public class StockHawkApp extends Application {

    // DONE: 06.05.2017 "Right now I can't use this app with my screen reader. My friends love it, so I would love to download it, but the buttons don't tell my screen reader what they do."
    // DONE: 06.05.2017 "We need to prepare Stock Hawk for the Egypt release. Make sure our translators know what to change and make sure the Arabic script will format nicely."
    // DONE: 06.05.2017 "Stock Hawk allows me to track the current price of stocks, but to track their prices over time, I need to use an external program. It would be wonderful if you could show more detail on a stock, including its price over time."
    // DONE: 06.05.2017 "I use a lot of widgets on my Android device, and I would love to have a widget that displays my stock quotes on my home screen."
    // DONE: 06.05.2017 "I found a bug in your app. Right now when I search for a stock quote that doesn't exist, the app crashes."
    // DONE: 06.05.2017 "When I opened this app for the first time without a network connection, it was a confusing blank screen. I would love a message that tells me why the screen is blank or whether my stock quotes are out of date."

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.uprootAll();
            Timber.plant(new Timber.DebugTree());
        }
    }
}
