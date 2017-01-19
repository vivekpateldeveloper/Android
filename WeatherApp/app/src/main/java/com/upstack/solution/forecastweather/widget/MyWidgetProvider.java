package com.upstack.solution.forecastweather.widget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.upstack.solution.forecastweather.MainActivity;
import com.upstack.solution.forecastweather.R;
import com.upstack.solution.forecastweather.YWeather.WeatherInfo;
import com.upstack.solution.forecastweather.YWeather.YahooWeather;
import com.upstack.solution.forecastweather.YWeather.YahooWeatherInfoListener;
import com.upstack.solution.forecastweather.Utils.Constants;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


public class MyWidgetProvider extends AppWidgetProvider implements YahooWeatherInfoListener {

    YahooWeather mYahooWeather = YahooWeather.getInstance(5000, 5000, true);
    Context mContext;
    RemoteViews remoteViews;
    AppWidgetManager mappWidgetManager;
    int[] allWidgetIds;
    int[] appWidgetIds;
    int mWidgetId;
    public static String WIDGET_UPDATE = "com.upstack.solution.forecastweather.widget.8BITCLOCK_WIDGET_UPDATE";

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (WIDGET_UPDATE.equals(intent.getAction())) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                ComponentName thisAppWidget = new ComponentName(context.getPackageName(), MyWidgetProvider.class.getName());
                int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);
                onUpdate(context, appWidgetManager, appWidgetIds);
            }
        }
    }


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {

        this.mContext = context;
        this.mappWidgetManager = appWidgetManager;
        this.appWidgetIds = appWidgetIds;

        Log.d("mytag", "UPDate");

        // Get all ids
        ComponentName thisWidget = new ComponentName(context,
                MyWidgetProvider.class);
        allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

        for (int widgetId : allWidgetIds) {
            this.mWidgetId = widgetId;
            remoteViews = new RemoteViews(context.getPackageName(),
                    R.layout.widget_layout);

            remoteViews.setViewVisibility(R.id.widget_refresh_pb, View.VISIBLE);

            saveLocation();

        }
    }

    private void saveLocation() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        String recentCity = preferences.getString("city", Constants.DEFAULT_CITY);

        if (!recentCity.equals("")) {
            searchByPlaceName(recentCity);
        }
    }

    private void searchByPlaceName(String location) {
        mYahooWeather.setNeedDownloadIcons(true);
        mYahooWeather.setUnit(YahooWeather.UNIT.CELSIUS);
        mYahooWeather.setSearchMode(YahooWeather.SEARCH_MODE.PLACE_NAME);
        mYahooWeather.queryYahooWeatherByPlaceName(mContext, location, MyWidgetProvider.this);
    }

    @Override
    public void gotWeatherInfo(WeatherInfo weatherInfo) {
        if (weatherInfo != null) {
            String city = weatherInfo.getLocationCity();
            String country = weatherInfo.getLocationCountry();
            float temperature = weatherInfo.getCurrentTemp();
            remoteViews.setTextViewText(R.id.todayDescription, city + (country.isEmpty() ? "" : ", " + country));
            remoteViews.setTextViewText(R.id.todayTemperature, temperature + " °" + "C");
            remoteViews.setTextViewText(R.id.todayDateTime, "" + formatTimeWithDayIfNotToday(mContext, System.currentTimeMillis()).replace(".", ""));
            if (weatherInfo.getCurrentConditionIcon() != null) {
                String name = "icon_" + weatherInfo.getCurrentCode();
                int resID = mContext.getResources().getIdentifier(name, "drawable", mContext.getPackageName());
                remoteViews.setImageViewResource(R.id.todayIcon, resID);
            }
            remoteViews.setViewVisibility(R.id.widget_refresh_pb, View.GONE);

            Intent intent = new Intent(mContext, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, 0);
            remoteViews.setOnClickPendingIntent(R.id.open_app_click, pendingIntent);
            mappWidgetManager.updateAppWidget(mWidgetId, remoteViews);
        } else {

        }

    }

    public static String formatTimeWithDayIfNotToday(Context context, long timeInMillis) {
        Calendar now = Calendar.getInstance();
        Calendar lastCheckedCal = new GregorianCalendar();
        lastCheckedCal.setTimeInMillis(timeInMillis);
        Date lastCheckedDate = new Date(timeInMillis);
        String timeFormat = android.text.format.DateFormat.getTimeFormat(context).format(lastCheckedDate);
        if (now.get(Calendar.YEAR) == lastCheckedCal.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == lastCheckedCal.get(Calendar.DAY_OF_YEAR)) {
            // Same day, only show time
            return timeFormat;
        } else {
            return android.text.format.DateFormat.getDateFormat(context).format(lastCheckedDate) + " " + timeFormat;
        }
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(createIntent(context));
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), 5 * 60 * 1000, createIntent(context));
    }

    private PendingIntent createIntent(Context context) {
        Intent intent = new Intent(WIDGET_UPDATE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }
}
