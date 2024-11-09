package com.example.myapplication;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        LinearLayout mainContainer = findViewById(R.id.main_container);


        int numDots = 200;
        createTreeLayout(mainContainer, numDots);

        for (int i = 1; i <= numDots; i++) {
            View dot = findViewById(i);
            if (dot != null) {
                dot.setOnTouchListener((v, event) -> {
                    System.out.println("Log");
                    if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
                        v.setBackgroundColor(Color.RED);
                        return true;
                    }
                    return false;
                });
            }
        }
    }

    private void createTreeLayout(LinearLayout container, int numDots) {
        int dotCount = 0;
        int rowNumber = 1;

        while (dotCount < numDots) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            row.setGravity(android.view.Gravity.CENTER);

            for (int i = 0; i < rowNumber; i++) {
                if (dotCount >= numDots) break;

                View dot = new View(this);
                dot.setId(View.generateViewId());
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(24, 24);
                params.setMargins(6, 6, 6, 6);
                dot.setLayoutParams(params);
                dot.setBackgroundResource(R.drawable.circle_shape);

                row.addView(dot);
                dotCount++;
            }

            container.addView(row);
            rowNumber++;
        }
    }
}