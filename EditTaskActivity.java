package com.resolute.Fragments;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
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
import com.google.gson.Gson;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class EditTaskActivity extends AppCompatActivity {

    Button goBackButton;
    NumberPicker numPickerHour;
    NumberPicker numPickerMin;

    EditText taskNameInput;
    EditText dateEditText;
    DatePickerDialog datePicker;

    String taskName;
    Integer dailyTargetTime;
    Date deadlineDate;

    Integer targetHours;
    Integer targetMins;

    Long newDailyStartTime;

    CheckBox checkbox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_task);

        goBackButton    = findViewById(R.id.goBackButton);
        taskNameInput   = findViewById(R.id.taskNameInput);
        dateEditText    = findViewById(R.id.dateEditText);
        numPickerHour   = findViewById(R.id.numPickerHour);
        numPickerMin    = findViewById(R.id.numPickerMin);
        checkbox        = findViewById(R.id.checkbox);

//        #####################
//        SET NUM PICKER VALUES
//        #####################

        numPickerHour.setMinValue(0);
        numPickerHour.setMaxValue(7);

        numPickerMin.setMinValue(0);
        numPickerMin.setMaxValue(59);

        numPickerHour.setValue((int) (MainActivity.mTaskList.get(FragmentList.editingItem).getDailyStartTime()/3600000));
        numPickerMin.setValue((int) (MainActivity.mTaskList.get(FragmentList.editingItem).getDailyStartTime()/60000));

//        numPickerHour.setValue(MainActivity.mTaskList.get(FragmentList.editingItem).getHours());
//        numPickerMin.setValue(MainActivity.mTaskList.get(FragmentList.editingItem).getMinutes());

//        #####################
//        SET TASK NAME TEXT
//        #####################

        taskNameInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        taskNameInput.setText(MainActivity.mTaskList.get(FragmentList.editingItem).getTaskName());

//        #####################
//        SET DATE TEXT
//        #####################

        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        if(MainActivity.mTaskList.get(FragmentList.editingItem).getDeadlineDate()!=null){
            dateEditText.setText(dateFormat.format(MainActivity.mTaskList.get(FragmentList.editingItem).getDeadlineDate()));
        }

        if(MainActivity.mTaskList.get(FragmentList.editingItem).getDeadlineDate()!=null){
            checkbox.setChecked(true);
            dateEditText.setEnabled(true);
        } else{
            checkbox.setChecked(false);
            dateEditText.setEnabled(false);
        }

        goBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(dateEditText.getText().toString().isEmpty() && checkbox.isChecked() || taskNameInput.getText().toString().equals("")){

                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(EditTaskActivity.this);
                    alertDialogBuilder.setTitle("Looks like you missed something");
                    alertDialogBuilder.setMessage("Please fill in all fields");
                    alertDialogBuilder.setPositiveButton("Exit without saving", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    startActivity(new Intent(EditTaskActivity.this, MainActivity.class));
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

                } else {
                    taskName = taskNameInput.getText().toString();
//                  MainActivity.mTaskName = taskNameInput.getText().toString();

//                  #####################
//                          TIME
//                  #####################
                    targetHours = numPickerHour.getValue();
                    targetMins = numPickerMin.getValue();
                    dailyTargetTime = (targetHours * 3600000) + (targetMins * 60000);
//                  MainActivity.mTargetTime = dailyTargetTime.longValue();

                    newDailyStartTime = (long) (targetHours * 3600000) + (targetMins * 60000);

//                  #####################
//                          DATE
//                  #####################
                    try {
                        deadlineDate = new SimpleDateFormat("dd/MM/yyyy").parse(dateEditText.getText().toString());
                    } catch (ParseException e) {
                        Log.i("Deadline Date", "Null");
                        e.printStackTrace();
                    }

                    Long timeWorked = MainActivity.mTaskList.get(FragmentList.editingItem).getDailyStartTime() - MainActivity.mTaskList.get(FragmentList.editingItem).getTargetTime();

                    if (checkbox.isChecked()) {
                        MainActivity.mTaskList.get(FragmentList.editingItem).setDeadlineDate(deadlineDate);
                    } else {
                        MainActivity.mTaskList.get(FragmentList.editingItem).setDeadlineDate(null);
                    }
                    MainActivity.mTaskList.get(FragmentList.editingItem).setTaskName(taskName);

                    Log.i("item daily start", String.valueOf(MainActivity.mTaskList.get(FragmentList.editingItem).getDailyStartTime()));
                    Log.i("new daily start", newDailyStartTime.toString());

                    if(timeWorked==0){
                        MainActivity.mTaskList.get(FragmentList.editingItem).setTargetTime(Long.valueOf(dailyTargetTime));
                        MainActivity.mTaskList.get(FragmentList.editingItem).setDailyStartTime(Long.valueOf(dailyTargetTime));
                        MainActivity.mTaskList.get(FragmentList.editingItem).setHours(targetHours);
                        MainActivity.mTaskList.get(FragmentList.editingItem).setMins(targetMins);
                        saveData();
                        startActivity(new Intent(EditTaskActivity.this, MainActivity.class));
                    } else if(MainActivity.mTaskList.get(FragmentList.editingItem).getDailyStartTime().toString().equals(newDailyStartTime.toString())){
                        saveData();
                        startActivity(new Intent(EditTaskActivity.this, MainActivity.class));
                    } else {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(EditTaskActivity.this);
                        alertDialogBuilder.setTitle("Reset the target time?");
                        alertDialogBuilder.setMessage("Are you sure you want to reset the target time? You will lose any progress made so far.");
                        alertDialogBuilder.setPositiveButton("Reset", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        MainActivity.mTaskList.get(FragmentList.editingItem).setTargetTime(Long.valueOf(dailyTargetTime));
                                        MainActivity.mTaskList.get(FragmentList.editingItem).setDailyStartTime(Long.valueOf(dailyTargetTime));
                                        MainActivity.mTaskList.get(FragmentList.editingItem).setHours(targetHours);
                                        MainActivity.mTaskList.get(FragmentList.editingItem).setMins(targetMins);
                                        saveData();
                                        startActivity(new Intent(EditTaskActivity.this, MainActivity.class));
                                    }
                                }
                        );
                        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        });
                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                      }
                }
            }
        });

        dateEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                dateEditText.setEnabled(true);
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
                datePicker = new DatePickerDialog(EditTaskActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                            dateEditText.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year); }
                            }, year, month, day);
                datePicker.getDatePicker().setMinDate(System.currentTimeMillis());
                datePicker.show();
            }
        });
    }

    private void saveData() {
        MainActivity.sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = MainActivity.sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(MainActivity.mTaskList);
        editor.putString("task list", json);
        editor.apply();
    }

}