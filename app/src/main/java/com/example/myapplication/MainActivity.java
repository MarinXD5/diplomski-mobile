package com.example.myapplication;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;

import com.example.myapplication.consts.consts;

public class MainActivity extends BaseActivity{
    private int selectedColor = Color.RED;
    private GestureDetector gestureDetector;
    private Map<Integer, String> coloredDots = new HashMap<>();
    private final boolean isLive = false; // promijeni ovo u true ako želiš testirat aplikaciju na lampicama


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setChildLayout(R.layout.activity_main);

        if(isLive){
            resolveHostname("raspberrypi", consts.SERVER_PORT,
                    () -> {
                        Toast.makeText(this, "Connected to Raspberry Pi!", Toast.LENGTH_SHORT).show();
                        initializeApp();
                    },
                    () -> showErrorPopup("Failed to resolve Raspberry Pi hostname. Please check the network.")
            );
        }
        else{
            initializeApp();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initializeApp() {
        LinearLayout mainContainer = findViewById(R.id.main_container);
        createTreeLayout(mainContainer, 200);
        setupColorPalette();

        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onScroll(MotionEvent e1, @NonNull MotionEvent e2, float distanceX, float distanceY) {
                colorDotUnderTouch(mainContainer, e2);
                return true;
            }
        });

        mainContainer.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_MOVE || event.getAction() == MotionEvent.ACTION_DOWN) {
                gestureDetector.onTouchEvent(event);
                return true;
            }
            return false;
        });

        Button resetButton = findViewById(R.id.resetBTN);
        resetButton.setOnClickListener(v -> resetDotsAndLeds());
    }

    private void setupColorPalette() {
        LinearLayout colorPalette = findViewById(R.id.color_palette);
        int[] colors = {
                Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.CYAN,
                Color.MAGENTA, Color.BLACK, Color.GRAY, Color.LTGRAY, Color.DKGRAY,
                Color.rgb(255, 165, 0),
                Color.rgb(128, 0, 128)
        };

        for (int color : colors) {
            View colorView = getColorView(color);
            colorPalette.addView(colorView);
        }
    }

    private @NonNull View getColorView(int color) {
        View colorView = new View(this);
        colorView.setLayoutParams(new LinearLayout.LayoutParams(80, 80));
        colorView.setBackgroundColor(color);
        colorView.setClickable(true);
        colorView.setFocusable(true);

        colorView.setOnClickListener(v -> selectedColor = color);

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) colorView.getLayoutParams();
        params.setMargins(8, 8, 8, 8);
        colorView.setLayoutParams(params);
        return colorView;
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
                dot.setTag(dotCount);
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

    private void colorDotUnderTouch(ViewGroup container, MotionEvent event) {
        int[] location = new int[2];
        int action = event.getActionMasked();
        boolean shouldSendCommand = false;
        int r = Color.red(selectedColor);
        int g = Color.green(selectedColor);
        int b = Color.blue(selectedColor);

        for (int i = 0; i < container.getChildCount(); i++) {
            LinearLayout row = (LinearLayout) container.getChildAt(i);
            for (int j = 0; j < row.getChildCount(); j++) {
                View dot = row.getChildAt(j);
                dot.getLocationOnScreen(location);

                int dotX = location[0];
                int dotY = location[1];

                if (event.getRawX() >= dotX && event.getRawX() <= (dotX + dot.getWidth()) &&
                        event.getRawY() >= dotY && event.getRawY() <= (dotY + dot.getHeight())) {

                    if (action == MotionEvent.ACTION_MOVE || action == MotionEvent.ACTION_DOWN) {
                        dot.setBackgroundTintList(android.content.res.ColorStateList.valueOf(selectedColor));

                        int ledIndex = (int) dot.getTag();

                        if (!dot.getTag().equals(dot.getContentDescription())) {
                            sendLedCommand(ledIndex, r, g, b);
                            dot.setContentDescription(ledIndex + "_" + r + "_" + g + "_" + b);
                        }
                    }
                }
            }
        }
    }

    private void sendCommandToServer(int ledIndex, String command) throws Exception {
        Socket socket = new Socket(consts.SERVER_IP, consts.SERVER_PORT);

        String fullCommand = ledIndex + ":" + command;

        OutputStream outputStream = socket.getOutputStream();
        PrintWriter writer = new PrintWriter(outputStream, true);
        writer.println(fullCommand);

        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String response = reader.readLine();

        writer.close();
        reader.close();
        socket.close();

        if (!"OK".equals(response)) {
            throw new Exception("Server did not acknowledge the command");
        }
    }

    private void sendLedCommand(int ledIndex, int r, int g, int b) {
        new Thread(() -> {
            try {
                String newCommand = r + "," + g + "," + b;

                if (coloredDots.containsKey(ledIndex)) {
                    String currentCommand = coloredDots.get(ledIndex);

                    if (!Objects.equals(currentCommand, newCommand)) {
                        sendCommandToServer(ledIndex, newCommand);
                        coloredDots.put(ledIndex, newCommand);
                    }
                } else {
                    sendCommandToServer(ledIndex, newCommand);
                    coloredDots.put(ledIndex, newCommand);
                }

            } catch (Exception e) {
                showErrorPopup("Failed to send command. Please try again.");
            }
        }).start();
    }

    private void resetDotsAndLeds() {
        LinearLayout mainContainer = findViewById(R.id.main_container);

        for (int i = 0; i < mainContainer.getChildCount(); i++) {
            LinearLayout row = (LinearLayout) mainContainer.getChildAt(i);
            for (int j = 0; j < row.getChildCount(); j++) {
                View dot = row.getChildAt(j);
                coloredDots.clear();
                dot.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.WHITE));
            }
        }

        sendResetCommand();
    }

    private void resolveHostname(String hostname, int port, Runnable onSuccess, Runnable onFailure) {
        new Thread(() -> {
            try {
                InetAddress address = InetAddress.getByName(hostname);

                if (address != null) {
                    consts.SERVER_IP = address.getHostAddress();

                    Socket socket = new Socket(consts.SERVER_IP, port);
                    socket.close();

                    runOnUiThread(onSuccess);
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            runOnUiThread(onFailure);
        }).start();
    }

    private void sendResetCommand() {
        new Thread(() -> {
            try {
                Socket socket = new Socket(consts.SERVER_IP, consts.SERVER_PORT);

                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                writer.println("RESET");

                writer.close();
                socket.close();
            } catch (Exception e) {
                showErrorPopup("Failed to reset LEDs. Please try again.");
            }
        }).start();
    }

    private void showErrorPopup(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}