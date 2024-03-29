package com.raisa.update1;

import static android.content.Context.MODE_PRIVATE;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.raisa.update1.Constants.GlobalVariable;
import com.raisa.update1.adapter.EventListAdapter;
import com.raisa.update1.object.Event;

import java.util.ArrayList;

public class CalenderFragment extends Fragment {
    private DatabaseReference rootRef;
    TextView addEvent;
    ImageView calender;
    EventListAdapter adapter;
    ArrayList<Event> list;
    RecyclerView recyclerView;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calender_nav,container,false);
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);
        set_var();

        rootRef = FirebaseDatabase.getInstance().getReference().child("careNeeder").child(GlobalVariable.Uid).child("Events");

        addEvent = getView().findViewById(R.id.addTask);
        calender = getView().findViewById(R.id.calendar);
        recyclerView = getView().findViewById(R.id.eventRecycler);

        recyclerView = getView().findViewById(R.id.eventRecycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        list = new ArrayList<>();

        rootRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren())
                {
                    Event task = dataSnapshot.getValue(Event.class);
                    list.add(task);
                }
                adapter.notifyDataSetChanged();
                if(list.size()==0){

                }
                else{

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        adapter = new EventListAdapter(getContext(), list);// rootref
        recyclerView.setAdapter(adapter);
        calender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCalender();
            }
        });
        addEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCreateTaskeDialog();
            }
        });
    }

    private void showCreateTaskeDialog() {


        final Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.create_event);

        EditText title = dialog.findViewById(R.id.idEdtTask);
        EditText description = dialog.findViewById(R.id.idEdtTaskDescription);
        Button add = dialog.findViewById(R.id.addTask);

        TimePicker timePicker = dialog.findViewById(R.id.SelectTime);
        final int[] hour = new int[1];
        final int[] min = new int[1];
        final int[] year = new int[1];
        final int[] month = new int[1];
        final int[] day = new int[1];

        DatePicker datePicker = dialog.findViewById(R.id.SelectDate);

        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker timePicker, int i, int i1) {

                hour[0] = i;
                min[0] = i1;


            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            datePicker.setOnDateChangedListener(new DatePicker.OnDateChangedListener() {
                @Override
                public void onDateChanged(DatePicker datePicker, int i, int i1, int i2) {
                    year[0]=i;
                    month[0] = i1+1;
                    day[0] = i2;
                }
            });
        }


        dialog.show();
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String t = title.getText().toString().trim();
                String d = description.getText().toString().trim();

                String y = Integer.toString(year[0]);
                String m = Integer.toString(month[0]);
                String da = Integer.toString(day[0]);

                String h = Integer.toString(hour[0]);
                String mi = Integer.toString(min[0]);

                if(!TextUtils.isEmpty(t)||!TextUtils.isEmpty(d) )
                {

                    addInfo(t, d, da,m,y, h, m);
                }
                else {
                    Toast.makeText(getContext(), "Please fill all the fields...", Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
            }
        });



        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);

    }
    private void showCalender()
    {
        final Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.fragment_calendar_view);

        dialog.show();


        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation2;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    public void addInfo(String title, String description, String dd, String mm, String yy, String hour, String min)
    {
        String id = rootRef.push().getKey();
        Event event = new Event(id, title, description, hour, min, dd, mm, yy);
        rootRef.child(id).setValue(event);
    }

    void set_var()
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore fStore;
        fStore = FirebaseFirestore.getInstance();
        GlobalVariable.Uid = user.getUid();


        DocumentReference df = fStore.collection("Users").document(GlobalVariable.Uid);

        df.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Log.d("TAG","onSuccess: " + documentSnapshot.getData());
                GlobalVariable.UserName = documentSnapshot.getString("Name");
                GlobalVariable.Email = documentSnapshot.getString("UserEmail");

            }
        });

    }
}
