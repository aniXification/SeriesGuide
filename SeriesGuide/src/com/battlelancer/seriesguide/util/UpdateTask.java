
package com.battlelancer.seriesguide.util;

import com.battlelancer.seriesguide.x.R;
import com.battlelancer.seriesguide.provider.SeriesContract;
import com.battlelancer.seriesguide.provider.SeriesContract.Episodes;
import com.battlelancer.seriesguide.provider.SeriesContract.Shows;
import com.battlelancer.seriesguide.ui.SeriesGuidePreferences;
import com.battlelancer.seriesguide.ui.ShowsActivity;
import com.battlelancer.thetvdbapi.TheTVDB;
import com.jakewharton.apibuilder.ApiException;
import com.jakewharton.trakt.ServiceManager;
import com.jakewharton.trakt.TraktException;
import com.jakewharton.trakt.entities.Activity;
import com.jakewharton.trakt.entities.ActivityItem;
import com.jakewharton.trakt.entities.TvShowEpisode;
import com.jakewharton.trakt.enumerations.ActivityAction;
import com.jakewharton.trakt.enumerations.ActivityType;

import org.xml.sax.SAXException;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class UpdateTask extends AsyncTask<Void, Integer, Integer> {

    private static final String TAG = "UpdateTask";

    private static final int UPDATE_SUCCESS = 100;

    private static final int UPDATE_ERROR = 102;

    private static final int UPDATE_OFFLINE = 103;

    private static final int UPDATE_CANCELLED = 104;

    public String[] mShows = null;

    public String mFailedShows = "";

    private Context mAppContext;

    public final AtomicInteger mUpdateCount = new AtomicInteger();

    private boolean mIsFullUpdate = false;

    private String mCurrentShowName;

    private NotificationManager mNotificationManager;

    private Notification mNotification;

    private static final int UPDATE_NOTIFICATION_ID = 1;

    public UpdateTask(boolean isFullUpdate, Context context) {
        mAppContext = context.getApplicationContext();
        mIsFullUpdate = isFullUpdate;
    }

    public UpdateTask(String[] shows, int index, String failedShows, Context context) {
        mAppContext = context.getApplicationContext();
        mShows = shows;
        mUpdateCount.set(index);
        mFailedShows = failedShows;
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onPreExecute() {
        // create a notification (holy crap is that a lot of code)
        String ns = Context.NOTIFICATION_SERVICE;
        mNotificationManager = (NotificationManager) mAppContext.getSystemService(ns);

        CharSequence tickerText = mAppContext.getString(R.string.update_notification);
        long when = System.currentTimeMillis();
        final int icon = R.drawable.ic_notification;

        mNotification = new Notification(icon, tickerText, when);
        mNotification.flags |= Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR
                | Notification.FLAG_ONLY_ALERT_ONCE;

        // content view
        RemoteViews contentView = new RemoteViews(mAppContext.getPackageName(),
                R.layout.update_notification);
        contentView.setImageViewResource(R.id.image, icon);
        contentView.setTextViewText(R.id.text, mAppContext.getString(R.string.update_notification));
        contentView.setProgressBar(R.id.progressbar, 0, 0, true);
        mNotification.contentView = contentView;

        // content intent
        Intent notificationIntent = new Intent(mAppContext, ShowsActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(mAppContext, 0, notificationIntent,
                0);
        mNotification.contentIntent = contentIntent;

        mNotificationManager.notify(UPDATE_NOTIFICATION_ID, mNotification);
    }

    @Override
    protected Integer doInBackground(Void... params) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mAppContext);
        final ContentResolver resolver = mAppContext.getContentResolver();
        final AtomicInteger updateCount = mUpdateCount;
        final boolean isAutoUpdateWlanOnly = prefs.getBoolean(
                SeriesGuidePreferences.KEY_AUTOUPDATEWLANONLY, true);
        long currentTime = 0;

        // build a list of shows to update
        if (mShows == null) {

            currentTime = System.currentTimeMillis();

            if (mIsFullUpdate) {

                // get all show IDs for a full update
                final Cursor shows = resolver.query(Shows.CONTENT_URI, new String[] {
                    Shows._ID
                }, null, null, null);

                mShows = new String[shows.getCount()];
                int i = 0;
                while (shows.moveToNext()) {
                    mShows[i] = shows.getString(0);
                    i++;
                }

                shows.close();

            } else {
                // get only shows which have not been updated for a certain time
                mShows = TheTVDB.deltaUpdateShows(currentTime, prefs, mAppContext);
            }
        }

        final int maxProgress = mShows.length + 2;
        int resultCode = UPDATE_SUCCESS;
        String id;

        // actually update the shows
        for (int i = updateCount.get(); i < mShows.length; i++) {
            // skip ahead if we get cancelled or connectivity is
            // lost/forbidden
            if (isCancelled()) {
                resultCode = UPDATE_CANCELLED;
                break;
            }
            if (!Utils.isNetworkConnected(mAppContext)
                    || (isAutoUpdateWlanOnly && !Utils.isWifiConnected(mAppContext))) {
                resultCode = UPDATE_OFFLINE;
                break;
            }

            id = mShows[i];
            setCurrentShowName(resolver, id);

            publishProgress(i, maxProgress);

            for (int itry = 0; itry < 2; itry++) {
                // skip ahead if we get cancelled or connectivity is
                // lost/forbidden
                if (isCancelled()) {
                    resultCode = UPDATE_CANCELLED;
                    break;
                }
                if (!Utils.isNetworkConnected(mAppContext)
                        || (isAutoUpdateWlanOnly && !Utils.isWifiConnected(mAppContext))) {
                    resultCode = UPDATE_OFFLINE;
                    break;
                }

                try {
                    TheTVDB.updateShow(id, mAppContext);
                    break;
                } catch (SAXException e) {
                    if (itry == 1) {
                        // failed twice, give up
                        fireTrackerEvent(e.getMessage());
                        resultCode = UPDATE_ERROR;
                        addFailedShow(mCurrentShowName);
                    }
                }
            }

            updateCount.incrementAndGet();
        }

        // try to avoid renewing the search table as it is time consuming
        if (updateCount.get() != 0 && mShows.length != 0) {
            publishProgress(mShows.length, maxProgress);
            TheTVDB.onRenewFTSTable(mAppContext);
        }

        // mark episodes based on trakt activity
        final int traktResult = getTraktActivity(prefs, maxProgress, currentTime,
                isAutoUpdateWlanOnly);
        // do not overwrite earlier failure codes
        if (resultCode == UPDATE_SUCCESS) {
            resultCode = traktResult;
        }

        publishProgress(maxProgress, maxProgress);

        // update the latest episodes
        Utils.updateLatestEpisodes(mAppContext);

        // store time of update if it was successful
        if (currentTime != 0 && resultCode == UPDATE_SUCCESS) {
            prefs.edit().putLong(SeriesGuidePreferences.KEY_LASTUPDATE, currentTime).commit();
        }

        return resultCode;
    }

    private int getTraktActivity(SharedPreferences prefs, int maxProgress, long currentTime,
            boolean isAutoUpdateWlanOnly) {
        if (Utils.isTraktCredentialsValid(mAppContext)) {
            // return if we get cancelled or connectivity is lost/forbidden
            if (isCancelled()) {
                return UPDATE_CANCELLED;
            }
            if (!Utils.isNetworkConnected(mAppContext)
                    || (isAutoUpdateWlanOnly && !Utils.isWifiConnected(mAppContext))) {
                return UPDATE_OFFLINE;
            }

            publishProgress(maxProgress - 1, maxProgress);

            // get last trakt update timestamp
            final long startTimeTrakt = prefs.getLong(SeriesGuidePreferences.KEY_LASTTRAKTUPDATE,
                    currentTime) / 1000;

            ServiceManager manager;
            try {
                manager = Utils.getServiceManagerWithAuth(mAppContext, false);
            } catch (Exception e) {
                fireTrackerEvent(e.getMessage());
                Log.w(TAG, e);
                return UPDATE_ERROR;
            }

            // get watched episodes from trakt
            Activity activity;
            try {
                activity = manager
                        .activityService()
                        .user(Utils.getTraktUsername(mAppContext))
                        .types(ActivityType.Episode)
                        .actions(ActivityAction.Checkin, ActivityAction.Seen,
                                ActivityAction.Scrobble, ActivityAction.Collection)
                        .timestamp(startTimeTrakt).fire();
            } catch (TraktException e) {
                fireTrackerEvent(e.getMessage());
                Log.w(TAG, e);
                return UPDATE_ERROR;
            } catch (ApiException e) {
                fireTrackerEvent(e.getMessage());
                Log.w(TAG, e);
                return UPDATE_ERROR;
            }

            // build an update batch
            final ArrayList<ContentProviderOperation> batch = Lists.newArrayList();
            for (ActivityItem item : activity.activity) {
                // check for null (potential fix for reported crash)
                if (item.action != null && item.show != null) {
                    switch (item.action) {
                        case Seen: {
                            // seen uses an array of episodes
                            List<TvShowEpisode> episodes = item.episodes;
                            for (TvShowEpisode episode : episodes) {
                                addEpisodeSeenOp(batch, episode, item.show.tvdbId);
                            }
                            break;
                        }
                        case Checkin:
                        case Scrobble: {
                            // checkin and scrobble use a single episode
                            TvShowEpisode episode = item.episode;
                            addEpisodeSeenOp(batch, episode, item.show.tvdbId);
                            break;
                        }
                        case Collection: {
                            // collection uses an array of episodes
                            List<TvShowEpisode> episodes = item.episodes;
                            for (TvShowEpisode episode : episodes) {
                                addEpisodeCollectedOp(batch, episode, item.show.tvdbId);
                            }
                            break;
                        }
                        default:
                            break;
                    }
                }
            }

            // execute the batch
            try {
                mAppContext.getContentResolver()
                        .applyBatch(SeriesContract.CONTENT_AUTHORITY, batch);
            } catch (RemoteException e) {
                // Failed binder transactions aren't recoverable
                fireTrackerEvent(e.getMessage());
                throw new RuntimeException("Problem applying batch operation", e);
            } catch (OperationApplicationException e) {
                // Failures like constraint violation aren't
                // recoverable
                fireTrackerEvent(e.getMessage());
                throw new RuntimeException("Problem applying batch operation", e);
            }

            // store time of this update as seen by the trakt server
            prefs.edit()
                    .putLong(SeriesGuidePreferences.KEY_LASTTRAKTUPDATE,
                            activity.timestamps.current.getTime()).commit();

        }

        return UPDATE_SUCCESS;
    }

    private void setCurrentShowName(final ContentResolver resolver, String id) {
        Cursor show = resolver.query(Shows.buildShowUri(id), new String[] {
            Shows.TITLE
        }, null, null, null);
        if (show.moveToFirst()) {
            mCurrentShowName = show.getString(0);
        }
        show.close();
    }

    @Override
    protected void onPostExecute(Integer result) {
        String message = null;
        int length = 0;
        switch (result) {
            case UPDATE_SUCCESS:
                fireTrackerEvent("Success");

                message = mAppContext.getString(R.string.update_success);
                length = Toast.LENGTH_SHORT;

                break;
            case UPDATE_ERROR:
                message = mAppContext.getString(R.string.update_saxerror);
                length = Toast.LENGTH_LONG;
                break;
            case UPDATE_OFFLINE:
                fireTrackerEvent("Offline");

                message = mAppContext.getString(R.string.update_offline);
                length = Toast.LENGTH_LONG;
                break;
        }

        if (message != null) {
            // add a list of failed shows
            if (mFailedShows.length() != 0) {
                message += "(" + mFailedShows + ")";
            }
            Toast.makeText(mAppContext, message, length).show();
        }
        mNotificationManager.cancel(UPDATE_NOTIFICATION_ID);
        TaskManager.getInstance(mAppContext).onTaskCompleted();
    }

    @Override
    protected void onCancelled() {
        mNotificationManager.cancel(UPDATE_NOTIFICATION_ID);
        TaskManager.getInstance(mAppContext).onTaskCompleted();
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        String text;
        if (values[0] == values[1]) {
            // clear the text field if we are finishing up
            text = "";
        } else if (values[0] + 1 == values[1]) {
            // if we're one before completion, we're looking on trakt for user
            // activity
            text = mAppContext.getString(R.string.update_traktactivity);
        } else if (values[0] + 2 == values[1]) {
            // if we're two before completion, we're rebuilding the search index
            text = mAppContext.getString(R.string.update_rebuildsearch);
        } else {
            text = mCurrentShowName + "...";
        }

        mNotification.contentView.setTextViewText(R.id.text, text);
        mNotification.contentView.setProgressBar(R.id.progressbar, values[1], values[0], false);
        mNotificationManager.notify(UPDATE_NOTIFICATION_ID, mNotification);
    }

    private void addFailedShow(String seriesName) {
        if (mFailedShows.length() != 0) {
            mFailedShows += ", ";
        }
        mFailedShows += seriesName;
    }

    private static void addEpisodeSeenOp(final ArrayList<ContentProviderOperation> batch,
            TvShowEpisode episode, String showTvdbId) {
        batch.add(ContentProviderOperation.newUpdate(Episodes.buildEpisodesOfShowUri(showTvdbId))
                .withSelection(Episodes.NUMBER + "=? AND " + Episodes.SEASON + "=?", new String[] {
                        String.valueOf(episode.number), String.valueOf(episode.season)
                }).withValue(Episodes.WATCHED, true).build());
    }

    private static void addEpisodeCollectedOp(ArrayList<ContentProviderOperation> batch,
            TvShowEpisode episode, String showTvdbId) {
        batch.add(ContentProviderOperation.newUpdate(Episodes.buildEpisodesOfShowUri(showTvdbId))
                .withSelection(Episodes.NUMBER + "=? AND " + Episodes.SEASON + "=?", new String[] {
                        String.valueOf(episode.number), String.valueOf(episode.season)
                }).withValue(Episodes.COLLECTED, true).build());
    }

    private void fireTrackerEvent(String message) {
        AnalyticsUtils.getInstance(mAppContext).trackEvent(TAG, "Update result", message, 0);
    }

}
