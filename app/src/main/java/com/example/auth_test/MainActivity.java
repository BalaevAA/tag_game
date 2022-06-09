package com.example.auth_test;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private int emptyX = 3;
    private int emptyY = 3;
    private RelativeLayout group;
    private Button[][] buttons;
    private int[] titles;
    private Button buttonShuffle;
    private Timer timer;
    private TextView time;
    private int timeCounter = 0;
    private TextView record;
    private int seconds = 0;
    private int minutes = 0;
    private User user = new User();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logoutUser();
            }
        });

        TextView helloUser = findViewById(R.id.HelloUser);
        record = findViewById(R.id.record_time);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("users").child(currentUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                user = snapshot.getValue(User.class);
                if (user != null) {
                    helloUser.setText("Игрок: " + user.firstName + " " + user.lastName);
                    record.setText(String.format("Рекорд: %02d:%02d", user.minute, user.second));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        Button btnAbout = findViewById(R.id.btnAbout);
        btnAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                aboutApp();
            }
        });




        loadViews();
        loadNumbers();
        generateNumbers();
        loadDataToViews();

    }

    private void aboutApp() {
        Dialog dialog = new Dialog(MainActivity.this);
        dialog.setContentView(R.layout.dialog_layout);

        Button button = (Button) dialog.findViewById(R.id.backToGame);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.setCancelable(false);
        dialog.setTitle("Как играть?");
        dialog.show();
    }

    private void logoutUser() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void loadDataToViews() {

        emptyX = 3;
        emptyY = 3;

        for (int i = 0; i < group.getChildCount() - 1; i++) {
            buttons[i / 4][i % 4].setText(String.valueOf(titles[i]));
            buttons[i / 4][i % 4].setBackgroundResource(R.drawable.circle_button);
        }

        buttons[emptyX][emptyY].setText("");
        buttons[emptyX][emptyY].setBackgroundColor(ContextCompat.getColor(this, R.color.emptyButton));

    }

    private void generateNumbers() {

        int n = 15;
        Random random = new Random();

        while (n > 1) {
            int randomNum = random.nextInt(n--);
            int temp = titles[randomNum];
            titles[randomNum] = titles[n];
            titles[n] = temp;
        }

        if(!isSolved()) {
            generateNumbers();
        }

    }

    private boolean isSolved() {

        int countInversions = 0;
        for (int i = 0; i < 15; i++) {
            for(int j = 0; j < i; j++) {
                if (titles[j] > titles[i]) {
                    countInversions += 1;
                }
            }
        }
        return countInversions % 2 == 0;
    }

    private void loadNumbers() {
        titles = new int[16];
        for (int i = 0; i < group.getChildCount() - 1; i++) {
            titles[i] = i + 1;
        }
    }

    private void loadViews() {
        group = findViewById(R.id.group);
        buttons = new Button[4][4];
        buttonShuffle = findViewById(R.id.buttonShuffle);
        time = findViewById(R.id.time);
        startTimer();
        for (int i = 0; i < group.getChildCount(); i++) {
            buttons[i / 4][i % 4] = (Button) group.getChildAt(i);
        }

//        Button forDebug = findViewById(R.id.forDebug);
//        forDebug.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                loadNumbers();
//                loadDataToViews();
//            }
//        });


        buttonShuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                generateNumbers();
                loadDataToViews();
                timeCounter = 0;
                for (int i = 0; i < group.getChildCount(); i++) {
                    buttons[i / 4][i % 4].setClickable(true);
                }
            }
        });
    }

    private void startTimer() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                timeCounter += 1;
                seconds = timeCounter % 60;
                minutes = timeCounter / 60;
                time.setText(String.format("Время: %02d:%02d", minutes, seconds));
            }
        }, 1000, 1000);
    }

    public void buttonClick(View view) {

        Button button = (Button) view;
        int x = button.getTag().toString().charAt(0)-'0';
        int y = button.getTag().toString().charAt(1)-'0';

        if ((Math.abs(emptyX-x) == 1 && emptyY == y) || (Math.abs(emptyY - y) == 1 && emptyX == x)) {
            buttons[emptyX][emptyY].setText(button.getText().toString());
            buttons[emptyX][emptyY].setBackgroundResource(R.drawable.circle_button);
            button.setText("");
            button.setBackgroundColor(ContextCompat.getColor(this, R.color.emptyButton));
            emptyX = x;
            emptyY = y;
            Check();
        }

    }

    private void setRecordTime(int minute, int second) {
        FirebaseDatabase.getInstance().getReference("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .setValue(user);
    }

    private void Check() {
        boolean isOkey = false;
        if(emptyX == 3 && emptyY == 3) {
            for (int i = 0; i < group.getChildCount() - 1; i++) {
                if (buttons[i / 4][i % 4].getText().toString().equals(String.valueOf(i + 1))) {
                    isOkey = true;
                } else {
                    isOkey = false;
                    break;
                }
            }
        }
        if(isOkey == true) {
            Toast.makeText(this, "Вы победили", Toast.LENGTH_SHORT).show();
            for (int i = 0; i < group.getChildCount(); i++) {
                buttons[i / 4][i % 4].setClickable(false);
            }
            timer.cancel();
            if((user.getMinute() > minutes) || (user.getMinute() == minutes && user.getSecond() > seconds)) {
                user.setMinute(minutes);
                user.setSecond(seconds);
                record.setText(String.format("Рекорд: %02d:%02d", minutes, seconds));
                setRecordTime(minutes, seconds);
            }
        }
    }
}