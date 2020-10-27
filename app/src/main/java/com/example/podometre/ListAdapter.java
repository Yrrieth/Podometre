package com.example.podometre;

import android.content.Context;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

class DateStepsModel {
    public String date;
    public int stepCount;
}

// Créer une liste personnalisé
public class ListAdapter extends BaseAdapter {
    TextView dateStepCountText;
    ArrayList<DateStepsModel> stepCountList;
    Context context;
    LayoutInflater layoutInflater; // Pour faire un custom view avec ListView

    public ListAdapter(ArrayList<DateStepsModel> stepCountList, Context context) {
        this.stepCountList = stepCountList;
        this.context = context;
        this.layoutInflater = (LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount () {
        return stepCountList.size();
    }

    @Override
    public Object getItem(int position) {
        return stepCountList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.list_rows, parent, false);
        }

        dateStepCountText = (TextView)convertView.findViewById(R.id.sensor_name);
        String text = stepCountList.get(position).date + " - Total Steps: " + String.valueOf(stepCountList.get(position).stepCount);
        dateStepCountText.setText(text);
        return convertView;
    }
}
