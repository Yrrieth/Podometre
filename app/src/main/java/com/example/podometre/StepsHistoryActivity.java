package com.example.podometre;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import java.util.ArrayList;

public class StepsHistoryActivity extends Activity {
    private ListView sensorListView;
    private ListAdapter listAdapter;
    private StepsDBHelper stepsDBHelper;
    private ArrayList<DateStepsModel> stepCountList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //sensorListView = (ListView)findViewById(R.id.steps_list);

        getDataForList();

        listAdapter = new ListAdapter(stepCountList, this);
        sensorListView.setAdapter(listAdapter);

        Intent stepsIntent = new Intent(getApplicationContext(), StepsService.class);
        startService(stepsIntent);
    }

    public void getDataForList() {
        stepsDBHelper = new StepsDBHelper(this);
        stepCountList = stepsDBHelper.readStepsEntries();
    }

}
