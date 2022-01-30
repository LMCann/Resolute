package com.resolute;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.resolute.Fragments.FragmentHome;
import com.resolute.Fragments.FragmentList;
import com.example.resolute.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    public static BottomNavigationView navbar;
//    public static Date mTargetDate;
//    public static String mTaskName;
    public static Long mTargetTime;
    public static ArrayList<Task> mTaskList;
    public static Task newTask;
    public static TaskListAdapter adapter;
    public static Integer listItemPos;
    public static SharedPreferences sharedPreferences;
    public static SharedPreferences sharedPreferences2;
    public static Date lastUsedDate;
    public static String dateLastUsed;
    public static boolean newDate;
    public static Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadData();
        loadLastUsedDate();

        //  update the target time if its a new day
        Date currentDate = new Date();
        DateFormat sdf = android.text.format.DateFormat.getDateFormat(MainActivity.this);
        String currentDateString = sdf.format(currentDate);
        if(dateLastUsed!=null){
            if (!dateLastUsed.equals(currentDateString)){
                newDate = true;
            } else{
                newDate = false;
            }
        } else{
            newDate=false;
        }

        if(newDate){
            for(int i=0;i<mTaskList.size(); i++){
                mTaskList.get(i).setTargetTime(mTaskList.get(i).dailyStartTime);
            }
        }

        adapter = new TaskListAdapter(this, R.layout.tasklistitem, mTaskList);

        //  navbar setup
        navbar =findViewById(R.id.navbar);
        navbar.setOnNavigationItemSelectedListener(navListener);

        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, new FragmentHome()).commit();
    }

    // #############################################################################################
    //      BOTTOM NAVIGATION
    // #############################################################################################

    BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    if(!FragmentHome.disableNavbar){
                        Fragment selectedFragment = null;
                        switch (item.getItemId()){
                            case R.id.timerNav:
                                selectedFragment = new FragmentHome();
                                break;
                            case R.id.tasksNav:
                                selectedFragment = new FragmentList();
                                break;
                        }
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, selectedFragment).commit();
                    }
                    else{
                        Toast.makeText(MainActivity.this, "Cannot leave whiile task in progress", Toast.LENGTH_LONG).show();
                    }
                    return true;
                }
            };

    // #############################################################################################
    //      DATA METHODS
    // #############################################################################################

    public void saveData() {
        sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(mTaskList);
        editor.putString("task list", json);
        editor.apply();
    }

    public void loadData(){
        sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("task list", null);
        Type type = new TypeToken<ArrayList<Task>>() {}.getType();
        mTaskList = gson.fromJson(json, type);

        if(mTaskList == null){
            mTaskList = new ArrayList<>();
        }
    }

    public void loadLastUsedDate(){
        sharedPreferences2 = getSharedPreferences("shared preferences2", MODE_PRIVATE);
        Gson gson2 = new Gson();
        String json2 = sharedPreferences.getString("last used date", null);
        Type type2 = new TypeToken<String>() {}.getType();
        dateLastUsed = gson2.fromJson(json2, type2);
    }

    public void deleteAllItems(){
        for(int i=0; i<mTaskList.size(); i++){
            mTaskList.clear();
            listItemPos = null;
            saveData();
            adapter.notifyDataSetChanged();
        }
    }

    // #############################################################################################
    //      MENU
    // #############################################################################################

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.clearButton){
            if (!FragmentHome.workTimerRunning){
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                alertDialogBuilder.setTitle("Clear list");
                alertDialogBuilder.setMessage("Are you sure you want to delete all tasks?");
                alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deleteAllItems();
                    }
                });
                alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            } else{
                Toast.makeText(this, "Task Currently in Progress", Toast.LENGTH_SHORT).show();
            }
        } else{
            if (!FragmentHome.workTimerRunning){
                if(listItemPos==null){
                    Toast.makeText(this, "No Active Task", Toast.LENGTH_LONG).show();
                } else{
                    removeActiveTask();
                }
            } else{
                Toast.makeText(this, "Task Currently in Progress", Toast.LENGTH_SHORT).show();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public void removeActiveTask(){
        listItemPos=null;
        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, new FragmentHome()).commit();
    }

}