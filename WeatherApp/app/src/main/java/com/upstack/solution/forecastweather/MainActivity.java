package com.upstack.solution.forecastweather;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.upstack.solution.forecastweather.Adapter.WeatherListAdapter;
import com.upstack.solution.forecastweather.Utils.AppMethods;
import com.upstack.solution.forecastweather.YWeather.WeatherInfo;
import com.upstack.solution.forecastweather.YWeather.YahooWeather;
import com.upstack.solution.forecastweather.YWeather.YahooWeatherExceptionListener;
import com.upstack.solution.forecastweather.Utils.AppPermissions;
import com.upstack.solution.forecastweather.Utils.Constants;
import com.upstack.solution.forecastweather.YWeather.YahooWeatherInfoListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;


public class MainActivity extends AppCompatActivity implements YahooWeatherExceptionListener, YahooWeatherInfoListener, WeatherListAdapter.OnItemRecycleViewClickListener {

    TextView todayTemperature;
    TextView todayDescription;
    TextView todayWind;
    TextView todayPressure;
    TextView todayHumidity;
    TextView todaySunrise;
    TextView todaySunset;
    TextView lastUpdate;
    ImageView todayIcon;
    Toolbar toolbar;
    WeatherInfo DefaultWeatherInfo;
    RecyclerView WeatherRecyclerView;
    YahooWeather mYahooWeather = YahooWeather.getInstance(5000, 5000, true);
    WeatherListAdapter mWeatherListAdapter;
    private List<WeatherInfo.ForecastInfo> mForecastInfos;
    ProgressDialog progressDialog;
    String recentCity = "";
    View appView;
    private static final String[] ALL_PERMISSIONS = {
            Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    private static final int ALL_REQUEST_CODE = 0;
    private AppPermissions mRuntimePermission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mYahooWeather.setExceptionListener(MainActivity.this);

        initHeader();
        initView();
    }

    private void initHeader() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void initView() {
        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("Loading Data...");
        progressDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                ProgressBar v = (ProgressBar) progressDialog.findViewById(android.R.id.progress);
                v.getIndeterminateDrawable().setColorFilter(0xffFCDF55,
                        android.graphics.PorterDuff.Mode.MULTIPLY);
            }
        });
        todayTemperature = (TextView) findViewById(R.id.todayTemperature);
        todayDescription = (TextView) findViewById(R.id.todayDescription);
        todayWind = (TextView) findViewById(R.id.todayWind);
        todayPressure = (TextView) findViewById(R.id.todayPressure);
        todayHumidity = (TextView) findViewById(R.id.todayHumidity);
        todaySunrise = (TextView) findViewById(R.id.todaySunrise);
        todaySunset = (TextView) findViewById(R.id.todaySunset);
        lastUpdate = (TextView) findViewById(R.id.lastUpdate);
        todayIcon = (ImageView) findViewById(R.id.todayIcon);
        appView = findViewById(R.id.viewApp);
        mRuntimePermission = new AppPermissions(this);

        LinearLayoutManager layoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);

        WeatherRecyclerView = (RecyclerView) findViewById(R.id.WeatherRecyclerView);
        WeatherRecyclerView.setLayoutManager(layoutManager);

        mWeatherListAdapter = new WeatherListAdapter(MainActivity.this, mForecastInfos, MainActivity.this);
        WeatherRecyclerView.setAdapter(mWeatherListAdapter);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        recentCity = preferences.getString("city", Constants.DEFAULT_CITY);
        String recentData = preferences.getString(Constants.DEFAULT_WEATHER, "");
        Log.d("mytag", "recentData::>>" + recentData);
        Gson gson = new Gson();
        DefaultWeatherInfo = gson.fromJson(recentData, WeatherInfo.class);
        setDefaultData(DefaultWeatherInfo);
        if (mRuntimePermission.hasPermission(ALL_PERMISSIONS)) {
            Log.d("mytag", "All permission already given");
            searchByPlaceName(recentCity);
        } else {
            mRuntimePermission.requestPermission(this, ALL_PERMISSIONS, ALL_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case ALL_REQUEST_CODE:
                List<Integer> permissionResults = new ArrayList<>();
                for (int grantResult : grantResults) {
                    permissionResults.add(grantResult);
                }
                if (permissionResults.contains(PackageManager.PERMISSION_DENIED)) {
                    Toast.makeText(this, "All Permissions not granted", Toast.LENGTH_SHORT).show();
                    searchByPlaceName(recentCity);
                } else {
                    Toast.makeText(this, "All Permissions granted", Toast.LENGTH_SHORT).show();
                    searchByPlaceName(recentCity);
                }
                break;
        }
    }

    private void setDefaultData(WeatherInfo weatherInfo) {
        try {
            if (weatherInfo != null) {
                mForecastInfos = new ArrayList<>();
                for (int i = 0; i < YahooWeather.FORECAST_INFO_MAX_SIZE; i++) {
                    final WeatherInfo.ForecastInfo forecastInfo = weatherInfo.getForecastInfoList().get(i);
                    mForecastInfos.add(forecastInfo);
                }

                String city = weatherInfo.getLocationCity();
                String country = weatherInfo.getLocationCountry();
                getSupportActionBar().setTitle(city + (country.isEmpty() ? "" : ", " + country));
                float temperature = weatherInfo.getCurrentTemp();

                todayTemperature.setText(temperature + " °" + "C");
                todayDescription.setText(weatherInfo.getCurrentText());
                todayWind.setText("Wind: " + weatherInfo.getWindSpeed() + " km/h");
                todayPressure.setText("Pressure: " + weatherInfo.getAtmospherePressure() + " in");
                todayHumidity.setText("Humidity: " + weatherInfo.getAtmosphereHumidity() + " %");
                todaySunrise.setText("Sunrise: " + weatherInfo.getAstronomySunrise());
                todaySunset.setText("Sunset: " + weatherInfo.getAstronomySunset());

                Log.d("mytag", "CurrentConditionIcon::" + weatherInfo.getCurrentCode());
                if (weatherInfo.getCurrentConditionIcon() != null) {
                    String name = "icon_" + weatherInfo.getCurrentCode();
                    int resID = getResources().getIdentifier(name, "drawable", this.getPackageName());
                    todayIcon.setImageResource(resID);
                }

                lastUpdate.setText("Update: " + formatTimeWithDayIfNotToday(MainActivity.this, System.currentTimeMillis()).replace(".", ""));
                mWeatherListAdapter.SetWeatherData(mForecastInfos);
            } else {

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    MenuItem action_myLocation;
    private Menu mMenu;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_scrolling, menu);
        //updatePlayStatus();
        return true;
    }

    public void updatePlayStatus() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                String Location = preferences.getString("Location", "");
                if (mMenu != null) {
                    action_myLocation = mMenu.findItem(R.id.action_myLocation);
                    if (Location.equals("MyLocation")) {
                        action_myLocation.setIcon(R.mipmap.ic_mylocation_selected);
                    } else {
                        action_myLocation.setIcon(R.mipmap.ic_mylocation);
                    }
                }
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
            String Location = preferences.getString("Location", "");
            if (Location.equals("MyLocation")) {
                if (AppMethods.isNetworkAvailable(MainActivity.this)) {
                    getCityByLocation();
                } else {
                    Snackbar.make(appView, getString(R.string.msg_connection_problem), Snackbar.LENGTH_LONG).show();
                }
            } else {
                if (AppMethods.isNetworkAvailable(MainActivity.this)) {
                    searchByPlaceName(recentCity);
                } else {
                    Snackbar.make(appView, getString(R.string.msg_connection_problem), Snackbar.LENGTH_LONG).show();
                }
            }
            return true;
        }
        if (id == R.id.action_myLocation) {
            getCityByLocation();
        }
        if (id == R.id.action_search) {
            searchCities();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        this.mMenu = menu;
        updatePlayStatus();
        return super.onPrepareOptionsMenu(menu);
    }

    private void getCityByLocation() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("Location", "MyLocation");
        editor.commit();
        updatePlayStatus();

        ShowProgress(true);
        mYahooWeather.setNeedDownloadIcons(true);
        mYahooWeather.setUnit(YahooWeather.UNIT.CELSIUS);
        mYahooWeather.setSearchMode(YahooWeather.SEARCH_MODE.PLACE_NAME);
        mYahooWeather.queryYahooWeatherByGPS(MainActivity.this, MainActivity.this);
    }

    private void searchCities() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(this.getString(R.string.search_title));
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setMaxLines(1);
        input.setSingleLine(true);
        alert.setView(input, 32, 0, 32, 0);
        alert.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String result = input.getText().toString();
                if (!result.isEmpty()) {
                    saveLocation(result);
                }
            }
        });
        alert.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Cancelled
            }
        });
        alert.show();
    }

    private void saveLocation(String result) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        recentCity = preferences.getString("city", Constants.DEFAULT_CITY);

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("city", result);
        editor.putString("Location", "");
        editor.commit();
        updatePlayStatus();

        if (!recentCity.equals(result)) {
            recentCity = result;
            searchByPlaceName(result);
        }
    }

    private void ShowProgress(boolean b) {
        if (b) {
            progressDialog.show();
        } else {
            progressDialog.dismiss();
        }
    }

    private void searchByPlaceName(String location) {
        ShowProgress(true);
        mYahooWeather.setNeedDownloadIcons(true);
        mYahooWeather.setUnit(YahooWeather.UNIT.CELSIUS);
        mYahooWeather.setSearchMode(YahooWeather.SEARCH_MODE.PLACE_NAME);
        mYahooWeather.queryYahooWeatherByPlaceName(getApplicationContext(), location, MainActivity.this);
    }

    @Override
    public void onFailConnection(Exception e) {
        ShowProgress(false);
        Snackbar.make(appView, getString(R.string.msg_connection_problem), Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onFailParsing(Exception e) {

    }

    @Override
    public void onFailFindLocation(Exception e) {
        ShowProgress(false);
        Snackbar.make(appView, "There is a problem to find location.", Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void gotWeatherInfo(WeatherInfo weatherInfo) {
        if (weatherInfo != null) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
            Gson gson = new Gson();
            String JsonData = gson.toJson(weatherInfo, WeatherInfo.class);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(Constants.DEFAULT_WEATHER, JsonData);
            editor.commit();

            ShowProgress(false);
            mForecastInfos = new ArrayList<>();
            for (int i = 0; i < YahooWeather.FORECAST_INFO_MAX_SIZE; i++) {
                final WeatherInfo.ForecastInfo forecastInfo = weatherInfo.getForecastInfoList().get(i);
                mForecastInfos.add(forecastInfo);
            }

            String city = weatherInfo.getLocationCity();
            String country = weatherInfo.getLocationCountry();
            getSupportActionBar().setTitle(city + (country.isEmpty() ? "" : ", " + country));
            editor.putString("city", city);
            editor.putString("Location", "");
            editor.commit();

            float temperature = weatherInfo.getCurrentTemp();

            todayTemperature.setText(temperature + " °" + "C");
            todayDescription.setText(weatherInfo.getCurrentText());
            todayWind.setText("Wind: " + weatherInfo.getWindSpeed() + " km/h");
            todayPressure.setText("Pressure: " + weatherInfo.getAtmospherePressure() + " in");
            todayHumidity.setText("Humidity: " + weatherInfo.getAtmosphereHumidity() + " %");
            todaySunrise.setText("Sunrise: " + weatherInfo.getAstronomySunrise());
            todaySunset.setText("Sunset: " + weatherInfo.getAstronomySunset());
            // todayIcon.setImageBitmap(weatherInfo.getCurrentConditionIcon());

            Log.d("mytag", "CurrentConditionIcon::" + weatherInfo.getCurrentCode());
            if (weatherInfo.getCurrentConditionIcon() != null) {
                String name = "icon_" + weatherInfo.getCurrentCode();
                int resID = getResources().getIdentifier(name, "drawable", this.getPackageName());
                todayIcon.setImageResource(resID);
            }

            lastUpdate.setText("Update: " + formatTimeWithDayIfNotToday(MainActivity.this, System.currentTimeMillis()).replace(".", ""));
            mWeatherListAdapter.SetWeatherData(mForecastInfos);
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
    public void onItemClicked(int position, WeatherListAdapter mAdapter) {

    }
}
