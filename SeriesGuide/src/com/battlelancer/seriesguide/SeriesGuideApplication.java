
package com.battlelancer.seriesguide;

import com.battlelancer.seriesguide.util.Utils;
import com.battlelancer.thetvdbapi.ImageCache;

import android.app.Application;

public class SeriesGuideApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // already create an instance of ImageCache
        ImageCache.getInstance(getApplicationContext());

        Utils.runNotificationService(this);
    }

    @Override
    public void onLowMemory() {
        ImageCache.getInstance(getApplicationContext()).clear();
    }

}
