package com.example.myapplication;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

import com.example.myapplication.consts.consts;
import com.example.myapplication.utils.Utils.TemplateUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends BaseActivity{
    private int selectedColor = Color.RED;
    private GestureDetector gestureDetector;
    private Map<Integer, String> coloredDots = new HashMap<>();
    private Map<Integer, View> dotMap = new HashMap<>();
    private final boolean isLive = false; // promijeni ovo u true ako želiš testirat aplikaciju na lampicama


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setChildLayout(R.layout.activity_main);
        applyThemeFromPreferences();

        Intent intent = getIntent();

        if (intent != null && intent.hasExtra("templateFilePath")) {
            String templateFilePath = intent.getStringExtra("templateFilePath");
            String fileContents = null;
            System.out.println("Template file path: " + templateFilePath);
            if (templateFilePath.startsWith("/content:/")) {
                try (InputStream is = getContentResolver().openInputStream(Uri.parse(templateFilePath));
                     BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                    fileContents = sb.toString();
                    System.out.println("File contents: " + fileContents);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                File file = new File(templateFilePath);
                System.out.println("Does file exist: " + file.exists());
                if (file.exists()) {
                    try (FileReader reader = new FileReader(file);
                         BufferedReader br = new BufferedReader(reader)) {
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) {
                            sb.append(line);
                        }
                        fileContents = sb.toString();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            if (fileContents != null) {
                try {
                    JSONObject templateData = new JSONObject(fileContents);
                    System.out.println("Template Data: " + templateData);
                    if (templateData.has("dotColors")) {
                        JSONObject dotColors = templateData.getJSONObject("dotColors");
                        for (Iterator<String> it = dotColors.keys(); it.hasNext(); ) {
                            String key = it.next();
                            int dotIndex = Integer.parseInt(key);
                            String colorStr = dotColors.getString(key);
                            View dot = dotMap.get(dotIndex);
                            if (dot != null) {
                                dot.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor(colorStr)));
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

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
        Button saveTemplateButton = findViewById(R.id.saveTemplate);
        resetButton.setOnClickListener(v -> resetDotsAndLeds());
        saveTemplateButton.setOnClickListener(v->onSaveTemplateClick());
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

                dotMap.put(dotCount, dot);

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

                        //Uncomment this when ready to send to raspberry
                        /*
                        if (!dot.getTag().equals(dot.getContentDescription())) {
                            sendLedCommand(ledIndex, r, g, b);
                            dot.setContentDescription(ledIndex + "_" + r + "_" + g + "_" + b);
                        }

                         */
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
                dot.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.argb(255,211,211,211)));
            }
        }

        //sendResetCommand();
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

    private void applyThemeFromPreferences() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String themePref = prefs.getString("pref_theme", "system");

        switch (themePref) {
            case "light":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "dark":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }

    public void onSaveTemplateClick() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Save Template");

        final EditText input = new EditText(this);
        input.setHint("Enter template name");
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String templateName = input.getText().toString().trim();
            if (templateName.isEmpty()) {
                Toast.makeText(this, "Template name cannot be empty.", Toast.LENGTH_SHORT).show();
                return;
            }
            JSONObject templateData = new JSONObject();
            try {
                templateData.put("color", String.format("#%06X", (0xFFFFFF & selectedColor)));
                templateData.put("pattern", "blink");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            boolean result = TemplateUtils.saveTemplate(this, templateName, templateData);
            if (result) {
                Toast.makeText(this, "Template saved!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to save template.", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }
}