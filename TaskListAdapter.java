package com.resolute;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.resolute.R;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

public class TaskListAdapter extends ArrayAdapter<Task> {

    private Context mContext;
    int mResource;

    public TaskListAdapter(@NonNull Context context, int resource, @NonNull ArrayList<Task> objects) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        String taskName = getItem(position).getTaskName();
        Date deadlineDate = getItem(position).getDeadlineDate();
        Long targetTime = getItem(position).getTargetTime();
        Long dailyStartTime = getItem(position).getDailyStartTime();
        int hours = getItem(position).getHours();
        int minutes = getItem(position).getMinutes();

        DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getContext());
//        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Task task = new Task(taskName, deadlineDate, targetTime, dailyStartTime);

        LayoutInflater inflater = LayoutInflater.from(mContext);

        convertView = inflater.inflate(mResource, parent, false);

        TextView listTaskName = convertView.findViewById(R.id.listTaskName);
        TextView listDeadlineDate = convertView.findViewById(R.id.listDeadlineDate);
        TextView listTargetHours = convertView.findViewById(R.id.listTargetHours);
        TextView listTargetMinutes = convertView.findViewById(R.id.listTargetMinutes);

        LinearLayout linearLayout = convertView.findViewById(R.id.listLinearLayout5);

        listTaskName.setText(String.valueOf(taskName));

        if(deadlineDate!=null){
            listDeadlineDate.setVisibility(View.VISIBLE);
            listDeadlineDate.setText(String.valueOf(dateFormat.format(deadlineDate)));
        } else{
            listDeadlineDate.setVisibility(View.GONE);
        }

        if(hours==0){
            listTargetMinutes.setVisibility(View.VISIBLE);
            listTargetHours.setVisibility(View.GONE);
            listTargetMinutes.setGravity(Gravity.CENTER);
            if(minutes==1){
                listTargetMinutes.setText(String.valueOf(minutes)+" min");
            } else{
                listTargetMinutes.setText(String.valueOf(minutes)+" mins");
            }
        } else if(minutes==0){
            listTargetHours.setVisibility(View.VISIBLE);
            listTargetMinutes.setVisibility(View.GONE);
            if(hours==1){
                listTargetHours.setText(String.valueOf(hours)+" hr");
            } else{
                listTargetHours.setText(String.valueOf(hours)+" hrs");
            }
        } else{
            listTargetHours.setVisibility(View.VISIBLE);
            listTargetMinutes.setVisibility(View.VISIBLE);

            if(hours==1){
                listTargetHours.setText(String.valueOf(hours)+" hr");
            } else{
                listTargetHours.setText(String.valueOf(hours)+" hrs");
            }
            if(minutes==1){
                listTargetMinutes.setText(String.valueOf(minutes)+" min");
            } else{
                listTargetMinutes.setText(String.valueOf(minutes)+" mins");
            }
        }

//        listTargetHours.setText(String.valueOf(hours)+ " hrs");
//        listTargetMinutes.setText(String.valueOf(minutes)+ " mins");

        return convertView;
    }
}
