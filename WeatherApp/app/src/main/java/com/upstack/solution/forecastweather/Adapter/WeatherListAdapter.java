package com.upstack.solution.forecastweather.Adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.upstack.solution.forecastweather.R;
import com.upstack.solution.forecastweather.YWeather.WeatherInfo;

import java.util.List;


public class WeatherListAdapter extends RecyclerView.Adapter<WeatherListAdapter.MyViewHolder> {


    private Activity context;
    OnItemRecycleViewClickListener mOnItemRecycleViewClickListener;
    private List<WeatherInfo.ForecastInfo> mForecastInfos;

    public WeatherListAdapter(Activity context, List<WeatherInfo.ForecastInfo> mForecastInfos, OnItemRecycleViewClickListener mOnItemRecycleViewClickListener) {
        this.context = context;
        this.mForecastInfos = mForecastInfos;
        this.mOnItemRecycleViewClickListener = mOnItemRecycleViewClickListener;
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_row, parent, false);
        MyViewHolder myViewHolder = new MyViewHolder(itemView);

        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        WeatherInfo.ForecastInfo mInfo = mForecastInfos.get(position);

        holder.itemDate.setText("" + mInfo.getForecastDay());
        holder.itemDescription.setText("" + mInfo.getForecastText());
        float temperature = mInfo.getForecastTempHigh();
        holder.itemTemperature.setText(temperature + " °" + "C");
        //holder.itemIcon.setImageBitmap(mInfo.getForecastConditionIcon());

        Log.d("mytag", "CurrentConditionIcon::" + mInfo.getForecastCode());
        if (mInfo.getForecastConditionIcon() != null) {
            String name = "icon_" + mInfo.getForecastCode();
            int resID = context.getResources().getIdentifier(name, "drawable", context.getPackageName());
            holder.itemIcon.setImageResource(resID);
        }
    }

    @Override
    public int getItemCount() {
        return mForecastInfos != null ? mForecastInfos.size() : 0;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView itemDate, itemDescription, itemTemperature;
        ImageView itemIcon;

        public MyViewHolder(View itemView) {
            super(itemView);
            itemIcon = (ImageView) itemView.findViewById(R.id.itemIcon);
            itemDate = (TextView) itemView.findViewById(R.id.itemDate);
            itemDescription = (TextView) itemView.findViewById(R.id.itemDescription);
            itemTemperature = (TextView) itemView.findViewById(R.id.itemTemperature);
        }
    }

    public void SetWeatherData(List<WeatherInfo.ForecastInfo> mForecastInfos) {
        this.mForecastInfos = mForecastInfos;
        notifyDataSetChanged();
    }

    public interface OnItemRecycleViewClickListener {
        void onItemClicked(int position, WeatherListAdapter mAdapter);
    }

}
