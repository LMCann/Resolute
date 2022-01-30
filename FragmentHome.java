package com.resolute.Fragments;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;

import com.resolute.MainActivity;
import com.example.resolute.R;
import com.google.gson.Gson;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import static com.resolute.Base.CHANNEL_1_ID;
import static com.resolute.Base.CHANNEL_2_ID;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentHome#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentHome extends Fragment {

    NumberPicker numPickerHour;
    NumberPicker numPickerMin;

    TextView workCountdown;
    TextView restCountdown;
    Button startTimerButton;
    Button resetTimerButton;
    long START_TIME_MINUTES = 1500000;
    long START_TIME_HOURS;
    boolean workMode;
    long workTimeLeft;
    long restTimeLeft;
    public static boolean disableNavbar;
    public static boolean workTimerRunning;
    public static boolean restTimerRunning;
    boolean restTimerPaused;
    CountDownTimer mCountDownTimer;
    TextView textView;
    TextView taskNameTV;
    TextView remainingTV;
    TextView targetTimeTV;
    TextView deadlineTV;
    LinearLayout timeLayout;
    private NotificationManagerCompat notificationManager;
    public static boolean notificationProgressBarRunning=false;
    public static boolean workNotificationProgressBarCancelled=false;
    public static boolean restNotificationProgressBarCancelled=false;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public FragmentHome() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FragmentHome.
     */
    // TODO: Rename and change types and number of parameters
    public static FragmentHome newInstance(String param1, String param2) {
        FragmentHome fragment = new FragmentHome();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        notificationManager = NotificationManagerCompat.from(this.getContext());

        Date currentDate = new Date();
        DateFormat sdf = android.text.format.DateFormat.getDateFormat(getContext());
        String currentDateString = sdf.format(currentDate);
        if(currentDateString!=MainActivity.dateLastUsed){
            for (int i=0; i<MainActivity.mTaskList.size(); i++){
                updateTaskTime(i);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        textView = view.findViewById(R.id.textView);

        numPickerHour = view.findViewById(R.id.numPickerHour);
        numPickerMin = view.findViewById(R.id.numPickerMin);

        disableNavbar = false;

        numPickerMin.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                START_TIME_MINUTES = i1*60000;
                START_TIME_HOURS = numPickerHour.getValue()*3600000;
                resetTimer();
            }
        });

        numPickerHour.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                if(i1==0){
                    numPickerMin.setMinValue(1);
                } else{
                    numPickerMin.setMinValue(0);
                }
                if(MainActivity.listItemPos!=null){
                    if(MainActivity.mTaskList.get(MainActivity.listItemPos).getHours()==numPickerHour.getValue()){
                        numPickerMin.setMaxValue(MainActivity.mTaskList.get(MainActivity.listItemPos).getMinutes());
                    } else{
                        numPickerMin.setMaxValue(59);
                    }
                }
                START_TIME_MINUTES = numPickerMin.getValue()*60000;
                START_TIME_HOURS = i1*3600000;
                resetTimer();
            }
        });

        workCountdown = view.findViewById(R.id.workTimer);
        restCountdown = view.findViewById(R.id.restTimer);

        startTimerButton = view.findViewById(R.id.startTimerButton);
        resetTimerButton = view.findViewById(R.id.resetTimerButton);

        taskNameTV = view.findViewById(R.id.taskNameTV);
        remainingTV = view.findViewById(R.id.remainingTV);
        timeLayout = view.findViewById(R.id.timeLayout);
        targetTimeTV = view.findViewById(R.id.targetTimeTV);
        deadlineTV = view.findViewById(R.id.deadlineTV);

        if(MainActivity.listItemPos==null){
            textView.setVisibility(View.VISIBLE);
            taskNameTV.setVisibility(View.INVISIBLE);
            remainingTV.setVisibility(View.INVISIBLE);
            timeLayout.setVisibility(View.INVISIBLE);
            targetTimeTV.setVisibility(View.INVISIBLE);
            deadlineTV.setVisibility(View.INVISIBLE);
        } else{
            DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getContext());
            updateTaskTimeText();
//            targetTimeTV.setText(String.valueOf(MainActivity.mTaskList.get(MainActivity.listItemPos).getHours())+" hrs "+String.valueOf(MainActivity.mTaskList.get(MainActivity.listItemPos).getMinutes())+" mins");
            taskNameTV.setText(String.valueOf(MainActivity.mTaskList.get(MainActivity.listItemPos).getTaskName()));

            if(MainActivity.mTaskList.get(MainActivity.listItemPos).getDeadlineDate()!=null){
                deadlineTV.setVisibility(View.VISIBLE);
                deadlineTV.setText((dateFormat.format(MainActivity.mTaskList.get(MainActivity.listItemPos).getDeadlineDate())));
                if(MainActivity.mTaskList.get(MainActivity.listItemPos).getDeadlineDate().getTime()<=System.currentTimeMillis()){
                    deadlineTV.setTextColor(getResources().getColor(R.color.red));
                } else{
                    deadlineTV.setTextColor(getResources().getColor(R.color.colorPrimary));
                }
            }

            textView.setVisibility(View.GONE);
            taskNameTV.setVisibility(View.VISIBLE);
            remainingTV.setVisibility(View.VISIBLE);
            timeLayout.setVisibility(View.VISIBLE);
            targetTimeTV.setVisibility(View.VISIBLE);

        }

        startTimerButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if(workTimerRunning){
                    pauseTimer();
                } else if(restTimerRunning){
                    pauseTimer();
                } else{
                    if(workMode){
                        startWorkTimer();
                    } else{
                        startRestTimer();
                    }
                }
            }
        });

        resetTimerButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                resetTimer();
            }
        });

        setMaxAndMinNumPickers();

        workTimeLeft= START_TIME_HOURS+START_TIME_MINUTES;
        restTimeLeft=workTimeLeft/5;
        updateWorkCountdownText();
        updateRestCountdownText();
        resetTimerButton.setEnabled(false);
        workMode = true;

        // Inflate the layout for this fragment
        return view;
    }

    // #############################################################################################
    //      TIMER MANAGEMENT
    // #############################################################################################

    private void resetTimer() {
        restTimerPaused = false;
        disableNavbar = false;
        cancelNotificationProgressBar();
        workTimeLeft = START_TIME_HOURS+START_TIME_MINUTES;
        restTimeLeft=workTimeLeft/5;
        updateWorkCountdownText();
        updateRestCountdownText();
        numPickerHour.setEnabled(true);
        numPickerMin.setEnabled(true);
        startTimerButton.setEnabled(true);
        resetTimerButton.setEnabled(false);
    }

    private void startWorkTimer() {
        workNotificationProgressBarCancelled = false;
        if(!notificationProgressBarRunning){
            startWorkNotificationProgress(workTimeLeft);
        }

        MainActivity.lastUsedDate = new Date();
        DateFormat sdf = android.text.format.DateFormat.getDateFormat(getContext());
        MainActivity.dateLastUsed = sdf.format(MainActivity.lastUsedDate);

        MainActivity.sharedPreferences2 = this.getActivity().getSharedPreferences("shared preferences", getContext().MODE_PRIVATE);
        SharedPreferences.Editor editor2 = MainActivity.sharedPreferences.edit();
        Gson gson2 = new Gson();
        String json2 = gson2.toJson(MainActivity.dateLastUsed);
        editor2.putString("last used date", json2);
        editor2.apply();

        numPickerHour.setEnabled(false);
        numPickerMin.setEnabled(false);

        mCountDownTimer = new CountDownTimer(workTimeLeft, 1000) {
            @Override
            public void onTick(long timeUntilFinished) {
                if(MainActivity.listItemPos!=null){
                    MainActivity.mTaskList.get(MainActivity.listItemPos).setTargetTime(MainActivity.mTaskList.get(MainActivity.listItemPos).getTargetTime()-1000);
                    updateTaskTime(MainActivity.listItemPos);
                    updateTaskTimeText();
                    if(MainActivity.mTaskList.get(MainActivity.listItemPos).getTargetTime()<1000){
                        taskComplete();
                    }
                }
                workTimeLeft = timeUntilFinished;
                updateWorkCountdownText();
            }

            @Override
            public void onFinish() {
                Toast.makeText(getActivity(), "Rest time!", Toast.LENGTH_LONG).show();
                workTimerRunning = false;
                resetTimer();
                workMode = false;
                startRestTimer();
            }
        }.start();

        disableNavbar = true;
        workTimerRunning =true;
        startTimerButton.setText("Pause");
        resetTimerButton.setEnabled(false);
    }

    private void taskComplete() {
        pauseTimer();
        resetTimer();
        sendTaskCompleteNotification();
        MainActivity.listItemPos=null;
        getFragmentManager().beginTransaction().detach(FragmentHome.this).attach(FragmentHome.this).commit();
    }

    private void sendTaskCompleteNotification() {
        String titleText = MainActivity.mTaskList.get(MainActivity.listItemPos).getTaskName();
        String messageText = "Task Complete!";

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this.getContext(), CHANNEL_1_ID)
                .setSmallIcon(R.drawable.ic_baseline_timer_24)
                .setContentTitle(titleText)
                .setContentText(messageText)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setColor(Color.GREEN)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setOngoing(false);

        notificationManager.notify(3,notificationBuilder.build());
    }

    private void startRestTimer() {

        if(!restTimerPaused){
            restTimeLeft = workTimeLeft/5;
        }

        restNotificationProgressBarCancelled = false;
        if(!notificationProgressBarRunning){
            startRestNotificationProgress(restTimeLeft);
        }

        mCountDownTimer = new CountDownTimer((restTimeLeft), 1000) {
            @Override
            public void onTick(long timeUntilFinished) {
                restTimeLeft = timeUntilFinished;
                updateRestCountdownText();
            }

            @Override
            public void onFinish() {
                Toast.makeText(getActivity(), "Rest time over! Time to carry on working", Toast.LENGTH_LONG).show();
                restTimerRunning = false;
                restTimerPaused=false;
                resetTimer();
                workMode = true;
                startWorkTimer();
            }
        }.start();
        disableNavbar = true;
        restTimerRunning =true;
        startTimerButton.setText("Pause");
        resetTimerButton.setEnabled(false);
    }

    private void updateWorkCountdownText() {

        String formattedTimeLeft;
        int hours = (int) (workTimeLeft/3600000);
        int minutes = (int) ((workTimeLeft/1000)/60)%60;
        int seconds = (int) (workTimeLeft/1000)%60;

        if(hours == 0){
            formattedTimeLeft = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        } else{
            formattedTimeLeft = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
        }

        workCountdown.setText(formattedTimeLeft);
    }

    private void updateTaskTime(int i) {
        MainActivity.mTaskList.get(i).setHours((int) (MainActivity.mTaskList.get(i).getTargetTime()/3600000));
        MainActivity.mTaskList.get(i).setMins((int) ((MainActivity.mTaskList.get(i).getTargetTime()/1000)/60)%60);

        MainActivity.mTaskList.get(i).setMins(MainActivity.mTaskList.get(i).getMinutes());
        MainActivity.mTaskList.get(i).setHours(MainActivity.mTaskList.get(i).getHours());
        MainActivity.mTaskList.get(i).setTargetTime(MainActivity.mTaskList.get(i).getTargetTime());

        MainActivity.sharedPreferences = this.getActivity().getSharedPreferences("shared preferences", getContext().MODE_PRIVATE);
        SharedPreferences.Editor editor = MainActivity.sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(MainActivity.mTaskList.get(i));
        editor.putString("activeTaskTime", json);
        editor.apply();

        saveData();
    }

    private void updateTaskTimeText(){
        if (MainActivity.listItemPos!=null){
            if(MainActivity.mTaskList.get(MainActivity.listItemPos).getHours()==0){
                if(MainActivity.mTaskList.get(MainActivity.listItemPos).getMinutes()==1){
                    targetTimeTV.setText(String.valueOf(MainActivity.mTaskList.get(MainActivity.listItemPos).getMinutes())+" min ");
                } else{
                    targetTimeTV.setText(String.valueOf(MainActivity.mTaskList.get(MainActivity.listItemPos).getMinutes())+" mins ");
                }
            } else if(MainActivity.mTaskList.get(MainActivity.listItemPos).getMinutes()==0){
                if(MainActivity.mTaskList.get(MainActivity.listItemPos).getHours()==1){
                    targetTimeTV.setText(String.valueOf(MainActivity.mTaskList.get(MainActivity.listItemPos).getHours())+" hr ");
                } else{
                    targetTimeTV.setText(String.valueOf(MainActivity.mTaskList.get(MainActivity.listItemPos).getHours())+" hr ");
                }
            } else{
                if(MainActivity.mTaskList.get(MainActivity.listItemPos).getHours()==1 && MainActivity.mTaskList.get(MainActivity.listItemPos).getMinutes()==1){
                    targetTimeTV.setText(String.valueOf(MainActivity.mTaskList.get(MainActivity.listItemPos).getHours())+" hr "+String.valueOf(MainActivity.mTaskList.get(MainActivity.listItemPos).getMinutes())+" min");
                } else if(MainActivity.mTaskList.get(MainActivity.listItemPos).getHours()==1){
                    targetTimeTV.setText(String.valueOf(MainActivity.mTaskList.get(MainActivity.listItemPos).getHours())+" hr "+String.valueOf(MainActivity.mTaskList.get(MainActivity.listItemPos).getMinutes())+" mins");
                } else if(MainActivity.mTaskList.get(MainActivity.listItemPos).getMinutes()==1){
                    targetTimeTV.setText(String.valueOf(MainActivity.mTaskList.get(MainActivity.listItemPos).getHours())+" hrs "+String.valueOf(MainActivity.mTaskList.get(MainActivity.listItemPos).getMinutes())+" min");
                } else{
                    targetTimeTV.setText(String.valueOf(MainActivity.mTaskList.get(MainActivity.listItemPos).getHours())+" hrs "+String.valueOf(MainActivity.mTaskList.get(MainActivity.listItemPos).getMinutes())+" mins");
                }
            }

            saveData();
        }
    }

    private void updateRestCountdownText() {
        int minutes = (int) (restTimeLeft/1000)/60;
        int seconds = (int) (restTimeLeft/1000)%60;

        String formattedTimeLeft = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        restCountdown.setText(formattedTimeLeft);
    }

    private void pauseTimer() {
        updateTaskTimeText();
        mCountDownTimer.cancel();
        workTimerRunning = false;
        restTimerRunning = false;
        restTimerPaused=true;
        startTimerButton.setText("Start");
        resetTimerButton.setEnabled(true);
    }

    private void saveData() {
        MainActivity.sharedPreferences = this.getActivity().getSharedPreferences("shared preferences", getContext().MODE_PRIVATE);
        SharedPreferences.Editor editor = MainActivity.sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(MainActivity.mTaskList);
        editor.putString("task list", json);
        editor.apply();
    }

    private void setMaxAndMinNumPickers(){
        if(MainActivity.listItemPos==null){
            numPickerHour.setMinValue(0);
            numPickerHour.setMaxValue(2);
            numPickerHour.setValue(0);

            numPickerMin.setMaxValue(59);
            numPickerMin.setValue(25);
            if(numPickerHour.getValue()==0){
                numPickerMin.setMinValue(1);
            } else{
                numPickerMin.setMinValue(0);
            }
        }
        //MORE THAN 1 HOUR
        else if(MainActivity.mTaskList.get(MainActivity.listItemPos).getTargetTime()>=3600000){
            numPickerHour.setMinValue(0);
            numPickerHour.setMaxValue(MainActivity.mTaskList.get(MainActivity.listItemPos).getHours());
            numPickerHour.setValue(0);

            numPickerMin.setMinValue(0);
            numPickerMin.setMaxValue(59);
            numPickerMin.setValue(25);
        }
        //LESS THAN 1 HOUR MORE THAN 25 MINS
        else if(MainActivity.mTaskList.get(MainActivity.listItemPos).getMinutes()>=25){
            numPickerHour.setMinValue(0);
            numPickerHour.setMaxValue(0);
            numPickerHour.setValue(0);

            numPickerMin.setMinValue(1);
            numPickerMin.setMaxValue(MainActivity.mTaskList.get(MainActivity.listItemPos).getMinutes());
            numPickerMin.setValue(25);
        }
        //LESS THAN 25 MINS MORE THAN 1 MIN
        else if(MainActivity.mTaskList.get(MainActivity.listItemPos).getMinutes()>1){
            numPickerHour.setMinValue(0);
            numPickerHour.setMaxValue(0);
            numPickerHour.setValue(0);

            numPickerMin.setMinValue(1);
            numPickerMin.setMaxValue(MainActivity.mTaskList.get(MainActivity.listItemPos).getMinutes());
            numPickerMin.setValue(MainActivity.mTaskList.get(MainActivity.listItemPos).getMinutes());
        }
        //LESS THAN 1 MIN
        else{
            numPickerHour.setMinValue(0);
            numPickerHour.setMaxValue(0);
            numPickerHour.setValue(0);

            numPickerMin.setMinValue(1);
            numPickerMin.setMaxValue(1);
            numPickerMin.setValue(1);
        }

        START_TIME_MINUTES = numPickerMin.getValue()*60000;
        START_TIME_HOURS = numPickerHour.getValue()*3600000;
        resetTimer();

    }

    public void startWorkNotificationProgress(Long fullWorkTime){

        notificationProgressBarRunning=true;
        workNotificationProgressBarCancelled=false;

        final int maxProgress = Integer.parseInt(fullWorkTime.toString());
        final int[] progress = new int[1];
        progress[0] = (int) (maxProgress-workTimeLeft);
        String titleText = "Work time";

        final NotificationCompat.Builder notification = new NotificationCompat.Builder(this.getContext(), CHANNEL_1_ID)
                .setSmallIcon(R.drawable.ic_baseline_timer_24)
                .setContentTitle(titleText)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setProgress(maxProgress,progress[0],false);

//        Intent activityIntent = new Intent(this.getContext(), MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this.getContext());
//        stackBuilder.addNextIntentWithParentStack(activityIntent);
//        PendingIntent contentIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
//
//        notification.setContentIntent(contentIntent);

        notificationManager.notify(1,notification.build());

        new Thread(new Runnable() {
            @Override
            public void run() {
                SystemClock.sleep(2000);
                for(int i=0;i<=maxProgress;i++){
                    if(!workNotificationProgressBarCancelled){

                        String formattedWorkTimeLeftUpdate;
                        int hours = (int) (workTimeLeft/3600000);
                        int minutes = (int) ((workTimeLeft/1000)/60)%60;
                        int seconds = (int) (workTimeLeft/1000)%60;

                        if(hours == 0){
                            formattedWorkTimeLeftUpdate = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
                        } else{
                            formattedWorkTimeLeftUpdate = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
                        }

                        progress[0] = (int) (maxProgress-workTimeLeft);
                        notification.setProgress(maxProgress,progress[0],false);
                        notification.setContentText(formattedWorkTimeLeftUpdate);
                        notificationManager.notify(1,notification.build());
                        SystemClock.sleep(1000);
                    }
                }
                notification.setContentText("Done").setProgress(0,0,false).setOngoing(false);
                notificationManager.notify(1,notification.build());
                if (getContext()!=null){
                    NotificationManager cancelMgr = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
                    cancelMgr.cancel(1);
                } else{
                    SystemClock.sleep(1500);
                    NotificationManager cancelMgr = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
                    cancelMgr.cancel(1);
                }

            }
        }).start();
    }

    public void startRestNotificationProgress(Long fullRestTime){

        notificationProgressBarRunning=true;
        restNotificationProgressBarCancelled=false;

        final int maxProgress = Integer.parseInt(fullRestTime.toString());
        final int[] progress = new int[1];
        progress[0] = (int) (maxProgress-restTimeLeft);
        String messageText = String.valueOf(restTimeLeft)+"Time Remaining";
        String titleText = "Rest time";

        final NotificationCompat.Builder notification = new NotificationCompat.Builder(this.getContext(), CHANNEL_2_ID)
                .setSmallIcon(R.drawable.ic_baseline_timer_24)
                .setContentTitle(titleText)
                .setContentText(messageText)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setProgress(maxProgress,progress[0],false);

//        Intent activityIntent = new Intent(this.getContext(), MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this.getContext());
//        stackBuilder.addNextIntentWithParentStack(activityIntent);
//        PendingIntent contentIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
//
//        notification.setContentIntent(contentIntent);

        notificationManager.notify(2,notification.build());

        new Thread(new Runnable() {
            @Override
            public void run() {
                SystemClock.sleep(2000);
                for(int i=0;i<=maxProgress;i++){
                    if(!restNotificationProgressBarCancelled){

                        String formattedRestTimeLeftUpdate;
                        int hours = (int) (restTimeLeft/3600000);
                        int minutes = (int) ((restTimeLeft/1000)/60)%60;
                        int seconds = (int) (restTimeLeft/1000)%60;

                        if(hours == 0){
                            formattedRestTimeLeftUpdate = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
                        } else{
                            formattedRestTimeLeftUpdate = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
                        }

                        progress[0] = (int) (maxProgress-restTimeLeft);
                        notification.setProgress(maxProgress,progress[0],false);
                        notification.setContentText(formattedRestTimeLeftUpdate);
                        notificationManager.notify(2,notification.build());
                        SystemClock.sleep(2000);
                    }
                }
                notification.setContentText("Done").setProgress(0,0,false).setOngoing(false);
                notificationManager.notify(2,notification.build());
                NotificationManager cancelMgr = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
                cancelMgr.cancel(2);
            }
        }).start();
    }

    public void cancelNotificationProgressBar(){
        workNotificationProgressBarCancelled = true;
        restNotificationProgressBarCancelled = true;
        notificationProgressBarRunning=false;
        NotificationManager cancelMgr = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        cancelMgr.cancel(2);
        cancelMgr.cancel(1);
    }

}