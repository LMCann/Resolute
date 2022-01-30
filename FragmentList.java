package com.resolute.Fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.resolute.MainActivity;
import com.example.resolute.R;
import com.google.gson.Gson;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentList#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentList extends Fragment {

    Button button;
    public static Integer editingItem;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private FragmentActivity myContext;

    public FragmentList() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FragmentList.
     */
    // TODO: Rename and change types and number of parameters
    public static FragmentList newInstance(String param1, String param2) {
        FragmentList fragment = new FragmentList();
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_list, container, false);

        final ListView listView = view.findViewById(R.id.listView);

        //UPDATE SIMPLE LIST ITEM TO SPECIAL TASK ITEM
        listView.setAdapter(MainActivity.adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(MainActivity.mTaskList.get(i).getTargetTime()>1000){
                    MainActivity.listItemPos = i;
                    Fragment fragment = new FragmentHome();
                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.fragmentContainer, fragment);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                } else{
                    Toast.makeText(getContext(), "Task Complete!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                openItemOptions(i);
                return true;
            }
        });

        button = view.findViewById(R.id.addNewTaskButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addNewTask();
            }
        });

        // Inflate the layout for this fragment
        return view;
    }

    private void openItemOptions(final int pos) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
        alertDialogBuilder.setTitle("Task Settings");
        alertDialogBuilder.setMessage("What would you like to do?");
        alertDialogBuilder.setCancelable(true);

        alertDialogBuilder.setNegativeButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                MainActivity.listItemPos=null;
                Toast.makeText(getContext(),"Task Deleted",Toast.LENGTH_SHORT).show();
                MainActivity.mTaskList.remove(pos);
                saveData();
                MainActivity.adapter.notifyDataSetChanged();
            }
        });

        alertDialogBuilder.setPositiveButton("Edit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                if(!MainActivity.mTaskList.get(pos).getTargetTime().toString().equals(MainActivity.mTaskList.get(pos).getDailyStartTime().toString())){
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
                    alertDialogBuilder.setTitle("Are you sure you would like to edit this task?");
                    alertDialogBuilder.setMessage("You may lose the progress you have made so far");
                    alertDialogBuilder.setCancelable(true);

                    alertDialogBuilder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });

                    alertDialogBuilder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent editTaskIntent = new Intent(getContext(), EditTaskActivity.class);
                            editingItem = pos;
                            startActivity(editTaskIntent);
                        }
                    });

                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();

                } else{
                    Intent editTaskIntent = new Intent(getContext(), EditTaskActivity.class);
                    editingItem = pos;
                    startActivity(editTaskIntent);
                }
            }
        });

        alertDialogBuilder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void saveData() {
        MainActivity.sharedPreferences = this.getActivity().getSharedPreferences("shared preferences", getContext().MODE_PRIVATE);
        SharedPreferences.Editor editor = MainActivity.sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(MainActivity.mTaskList);
        editor.putString("task list", json);
        editor.apply();
    }

    public void addNewTask() {
        startActivity(new Intent(getActivity(), TaskActivity.class));
    }
}