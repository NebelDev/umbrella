package com.foo.umbrella.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.foo.umbrella.R;
import com.foo.umbrella.RecyclerViewAdapter;
import com.foo.umbrella.RecyclerViewAdapterTomorrow;
import com.foo.umbrella.data.ApiServicesProvider;
import com.foo.umbrella.data.model.CurrentObservation;
import com.foo.umbrella.data.model.ForecastCondition;
import com.foo.umbrella.data.model.WeatherData;
import com.foo.umbrella.settings.SettingsActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

  TextView temp;
  TextView degree;
  ApiServicesProvider apiServicesProvider;
  String zipCode;
  String isCelsius;
  View toolbarView;
  RecyclerView recyclerView;
  RecyclerView recyclerViewTomorrow;
  int numberOfColumns = 4;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    temp = (TextView)findViewById(R.id.weather_temp);
    degree = (TextView) findViewById(R.id.weather_degree);
    toolbarView = findViewById(R.id.relativeToolbar);
    recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
    recyclerViewTomorrow = (RecyclerView) findViewById(R.id.recyclerView_tomorrow);
    recyclerView.setHasFixedSize(true);
    recyclerView.setLayoutManager(new GridLayoutManager(this, numberOfColumns));

    recyclerViewTomorrow.setHasFixedSize(true);
    recyclerViewTomorrow.setLayoutManager(new GridLayoutManager(this, numberOfColumns));

    //Load data from SharePreferences
    loadCelciusPreferences();

    apiServicesProvider = new ApiServicesProvider(getApplication());
    AsyncWeatherData asyncData = new AsyncWeatherData();
    asyncData.execute();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.options, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.mn_settings:
                  startSettingsActivity();
                  return true;
      default:
            return super.onOptionsItemSelected(item);
    }
  }

  private void startSettingsActivity(){
    Intent settingsActivity = new Intent(this, SettingsActivity.class);
    startActivity(settingsActivity);
  }

  private void setTitleActionBar(String state, String temp, String degree){
    getSupportActionBar().setTitle(state);
    Double round = Double.valueOf(temp);
    this.temp.setText(round.intValue()+"°");
    this.degree.setText(degree);
  }

  private boolean isCold(String temperature){
    return (Double.valueOf(temperature).intValue() < 60);
  }

  private void loadCelciusPreferences(){
    SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
    zipCode = SP.getString("example_text", "95116");
    isCelsius = SP.getString("example_list", "0");
  }

  private List<ForecastCondition> getForecast(int today, List<ForecastCondition> forecastConditionList, int dayBy) {
    List<ForecastCondition> forecast = new ArrayList<>();

    for(ForecastCondition f: forecastConditionList){
      if(f.getDateTime().getDayOfMonth() == (today + dayBy)){
        forecast.add(f);
      }
    }
    return  forecast;
  }

  public class  AsyncWeatherData extends AsyncTask<Void,Void, Void> {

    CurrentObservation currentObservation;

    List<ForecastCondition> todayForecast;
    List<ForecastCondition> tomorrow;
    List<ForecastCondition> theDayAfterTomorrow;

    @Override
    protected Void doInBackground(Void... voids) {
      try {
        Call<WeatherData> weather = apiServicesProvider.getWeatherService().forecastForZipCallable(zipCode);
        Response<WeatherData> response = weather.execute();
        currentObservation = response.body().getCurrentObservation();

        List<ForecastCondition> forecastConditionList = response.body().getForecast();

        //Get the forecast for different days
        Calendar calendar = Calendar.getInstance();
        int today = calendar.get(Calendar.DAY_OF_MONTH);
        todayForecast = getForecast(today, forecastConditionList, 0);
        tomorrow = getForecast(today, forecastConditionList, 1);
        theDayAfterTomorrow = getForecast(today, forecastConditionList, 2);

      }catch (Exception e) { }
      return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
      super.onPostExecute(aVoid);
      //Show the temperature/ 1 is Celsius in SharedPreferences
      if(isCelsius.equals("1")) {
        setTitleActionBar(currentObservation.getDisplayLocation().getFullName(), currentObservation.getTempCelsius(), currentObservation.getWeatherDescription());
      }else {
        setTitleActionBar(currentObservation.getDisplayLocation().getFullName(), currentObservation.getTempFahrenheit(), currentObservation.getWeatherDescription());
      }

      //if the temperature is cold ( < 60 degree) the background changes in azure else,
      if(isCold(currentObservation.getTempFahrenheit())) {
        toolbarView.setBackgroundResource(R.color.weather_cool);
      }
      else
        toolbarView.setBackgroundResource(R.color.weather_warm);

      //Load the adapter and pass it to the RecyclerView
      RecyclerViewAdapter recyclerViewAdapter = new RecyclerViewAdapter(getApplicationContext(), todayForecast);
      recyclerView.setAdapter(recyclerViewAdapter);
      //Load the adapter with forecast of tomorrow
      RecyclerViewAdapterTomorrow recyTomorrow = new RecyclerViewAdapterTomorrow(getApplicationContext(), tomorrow);
      recyclerViewTomorrow.setAdapter(recyTomorrow);
    }
  }
}
