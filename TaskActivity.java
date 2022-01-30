package com.resolute.Fragments;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.NumberPicker;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.resolute.MainActivity;
import com.example.resolute.R;
import com.resolute.Task;
import com.google.gson.Gson;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TaskActivity extends AppCompatActivity {

    Button goBackButton;
    NumberPicker numPickerHour;
    NumberPicker numPickerMin;

    EditText taskNameInput;
    EditText dateEditText;
    DatePickerDialog picker;

    String taskName;
    Integer dailyTargetTime;
    Date deadlineDate;

    Integer targetHours;
    Integer targetMins;

    CheckBox checkbox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        goBackButton    = findViewById(R.id.goBackButton);
        taskNameInput   = findViewById(R.id.taskNameInput);
        dateEditText    = findViewById(R.id.dateEditText);
        checkbox        = findViewById(R.id.checkbox);
        numPickerHour   = findViewById(R.id.numPickerHour);
        numPickerMin    = findViewById(R.id.numPickerMin);
        dateEditText    = findViewById(R.id.dateEditText);

        checkbox.setChecked(true);

        taskNameInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

        goBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(TaskActivity.this);

                if(dateEditText.getText().toString().isEmpty() && checkbox.isChecked() || taskNameInput.getText().toString().equals("")){

                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(TaskActivity.this);
                    alertDialogBuilder.setTitle("Looks like you missed something");
                    alertDialogBuilder.setMessage("Please fill in all fields");
                    alertDialogBuilder.setPositiveButton("Exit without saving", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    startActivity(new Intent(TaskActivity.this, MainActivity.class));
                                }
                            }
                    );
                    alertDialogBuilder.setNegativeButton("Go Back", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();

                }
                else{
//                TASK NAME
                    taskName = taskNameInput.getText().toString();

//                TARGET TIME
                    targetHours = numPickerHour.getValue();
                    targetMins = numPickerMin.getValue();
                    dailyTargetTime = (targetHours*3600000)+(targetMins*60000);

//                DEADLINE DATE
                    try {
                        deadlineDate = new SimpleDateFormat("dd/MM/yyyy").parse(dateEditText.getText().toString());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    MainActivity.newTask = new Task(taskName, deadlineDate, MainActivity.mTargetTime, MainActivity.mTargetTime);
                    MainActivity.mTaskList.add(MainActivity.newTask);

                    if(MainActivity.mTaskList.size()==0){
                        if(checkbox.isChecked()){
                            MainActivity.mTaskList.get(MainActivity.mTaskList.size()).setDeadlineDate(deadlineDate);
                        } else{
                            MainActivity.mTaskList.get(MainActivity.mTaskList.size()).setDeadlineDate(null);
                        }
                        MainActivity.mTaskList.get(MainActivity.mTaskList.size()).setTargetTime(Long.valueOf(dailyTargetTime));
                        MainActivity.mTaskList.get(MainActivity.mTaskList.size()).setDailyStartTime(Long.valueOf(dailyTargetTime));
                        MainActivity.mTaskList.get(MainActivity.mTaskList.size()).setTaskName(taskName);
                        MainActivity.mTaskList.get(MainActivity.mTaskList.size()).setHours(targetHours);
                        MainActivity.mTaskList.get(MainActivity.mTaskList.size()).setMins(targetMins);
                    }
                    else{
                        if(checkbox.isChecked()){
                            MainActivity.mTaskList.get(MainActivity.mTaskList.size()-1).setDeadlineDate(deadlineDate);
                        } else{
                            MainActivity.mTaskList.get(MainActivity.mTaskList.size()-1).setDeadlineDate(null);
                        }
                        MainActivity.mTaskList.get(MainActivity.mTaskList.size()-1).setTargetTime(Long.valueOf(dailyTargetTime));
                        MainActivity.mTaskList.get(MainActivity.mTaskList.size()-1).setDailyStartTime(Long.valueOf(dailyTargetTime));
                        MainActivity.mTaskList.get(MainActivity.mTaskList.size()-1).setTaskName(taskName);
                        MainActivity.mTaskList.get(MainActivity.mTaskList.size()-1).setHours(targetHours);
                        MainActivity.mTaskList.get(MainActivity.mTaskList.size()-1).setMins(targetMins);
                    }

                    MainActivity.sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE);
                    SharedPreferences.Editor editor = MainActivity.sharedPreferences.edit();
                    Gson gson = new Gson();
                    String json = gson.toJson(MainActivity.mTaskList);
                    editor.putString("task list", json);
                    editor.apply();

                    startActivity(new Intent(TaskActivity.this, MainActivity.class));
                }
            }
        });

        numPickerHour.setMinValue(0);
        numPickerHour.setMaxValue(7);

        numPickerMin.setMinValue(0);
        numPickerMin.setMaxValue(59);
        numPickerMin.setValue(25);

        dateEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        break;
                    case MotionEvent.ACTION_UP:
                        view.performClick();
                        break;
                    default:
                        break;
                }
                return true;
            }
        });

        checkbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkbox.isChecked()){
                    dateEditText.setEnabled(true);
                } else{
                    dateEditText.setEnabled(false);
                }
            }
        });

        dateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final Calendar cldr = Calendar.getInstance();
                int day = cldr.get(Calendar.DAY_OF_MONTH);
                int month = cldr.get(Calendar.MONTH);
                int year = cldr.get(Calendar.YEAR);

                // date picker dialog
                picker = new DatePickerDialog(TaskActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                dateEditText.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
                            }
                        }, year, month, day);
                picker.getDatePicker().setMinDate(System.currentTimeMillis());
                picker.show();

            }
        });

    }

}