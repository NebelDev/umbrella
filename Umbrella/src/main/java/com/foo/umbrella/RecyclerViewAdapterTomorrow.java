package com.foo.umbrella;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.foo.umbrella.data.model.ForecastCondition;

import java.util.List;

public class RecyclerViewAdapterTomorrow extends RecyclerView.Adapter<RecyclerViewAdapterTomorrow.ViewHolder> {

    List<ForecastCondition> forecast;
    Context ctx;
    int indexMin, indexMax;

    public RecyclerViewAdapterTomorrow(Context context, List<ForecastCondition> forecast) {
        this.forecast = forecast;
        ctx = context;
        indexMin = getMinTemperature();
        indexMax = getMaxTemperature();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(ctx).inflate(R.layout.grid_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if(position == indexMin) {
            holder.hour.setTextColor(ctx.getResources().getColor(R.color.weather_cool));
            holder.temperature.setTextColor(ctx.getResources().getColor(R.color.weather_cool));
      //      holder.weatherImage.setColorFilter(ctx.getResources().getColor(R.color.weather_cool));
        }
        else{
            if(position == indexMax){
                holder.hour.setTextColor(ctx.getResources().getColor(R.color.weather_warm));
                holder.temperature.setTextColor(ctx.getResources().getColor(R.color.weather_warm));
            }
        }
        holder.hour.setText(forecast.get(position).getDisplayTime());
        String image = forecast.get(position).getIcon();
   //     holder.weatherImage.setImageDrawable(ctx.getResources().getDrawable(
     //           ctx.getResources().getIdentifier("drawable/weather_" + image, "drawable", ctx.getPackageName())));

        holder.temperature.setText(forecast.get(position).getTempCelsius());
    }

    @Override
    public int getItemCount() {
        return forecast.size();
    }

    private int getMinTemperature() {
        int min = Integer.MAX_VALUE;
        int tempCast, position = -1;
        for(int i=0; i<forecast.size(); i++){
            tempCast = Integer.valueOf(forecast.get(i).getTempCelsius());
            if(tempCast < min) {
                min = tempCast;
                position = i;
            }
        }

        return position;
    }

    private int getMaxTemperature() {
        int max = Integer.MIN_VALUE;
        int tempCast, position = -1;
        for(int i=0; i<forecast.size(); i++){
            tempCast = Integer.valueOf(forecast.get(i).getTempCelsius());
            if(tempCast > max) {
                max = tempCast;
                position = i;
            }
        }

        return position;
    }

    public static final class ViewHolder extends RecyclerView.ViewHolder {

        public TextView hour;
        public ImageView weatherImage;
        public TextView temperature;

        public ViewHolder(View itemView) {
            super(itemView);
            hour = (TextView) itemView.findViewById(R.id.tb_0);
            weatherImage = (ImageView) itemView.findViewById(R.id.tb_image_0);
            temperature = (TextView) itemView.findViewById(R.id.tb_degree_0);
        }
    }
}
