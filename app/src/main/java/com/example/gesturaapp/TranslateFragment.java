package com.example.gesturaapp;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import java.util.Locale;

public class TranslateFragment extends Fragment {

    private TextView textConnectionStatus;
    private TextView textEnglish;
    private TextView textFilipino;
    private Button btnBluetoothConnect;
    private Button btnTextToSpeech;
    private ImageView iconConnection;

    private boolean isConnected = false;
    private TextToSpeech textToSpeech;

    public TranslateFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_translate, container, false);

        // Initialize views
        textConnectionStatus = view.findViewById(R.id.textConnectionStatus);
        textEnglish = view.findViewById(R.id.textEnglish);
        textFilipino = view.findViewById(R.id.textTranslation);
        btnBluetoothConnect = view.findViewById(R.id.btnBluetoothConnect);
        btnTextToSpeech = view.findViewById(R.id.btnTextToSpeech);
        iconConnection = view.findViewById(R.id.iconConnection);

        // Set up TTS
        textToSpeech = new TextToSpeech(requireContext(), status -> {
            if (status != TextToSpeech.ERROR) {
                textToSpeech.setLanguage(new Locale("fil", "PH"));
            }
        });

        // Button listeners
        btnBluetoothConnect.setOnClickListener(v -> toggleBluetooth());
        btnTextToSpeech.setOnClickListener(v -> speakFilipino());

        return view;
    }

    private void toggleBluetooth() {
        isConnected = !isConnected;
        animateButton(btnBluetoothConnect);

        if (isConnected) {
            textConnectionStatus.setText("Connected");
            textConnectionStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            btnBluetoothConnect.setText("Disconnect");
            btnBluetoothConnect.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(android.R.color.holo_green_dark)));
            iconConnection.setImageResource(R.drawable.bluetoothconnected);
            Toast.makeText(requireContext(), "Bluetooth Connected", Toast.LENGTH_SHORT).show();
        } else {
            textConnectionStatus.setText("Disconnected");
            textConnectionStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            btnBluetoothConnect.setText("Connect");
            btnBluetoothConnect.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(android.R.color.holo_red_dark)));
            iconConnection.setImageResource(R.drawable.bluetoothdisconnectedsymbolic);
            Toast.makeText(requireContext(), "Bluetooth Disconnected", Toast.LENGTH_SHORT).show();
        }
    }

    private void animateButton(Button button) {
        AlphaAnimation animation = new AlphaAnimation(0.5f, 1.0f);
        animation.setDuration(300);
        button.startAnimation(animation);
    }

    private void speakFilipino() {
        if (!isConnected) {
            Toast.makeText(requireContext(), "Please connect to Bluetooth first.", Toast.LENGTH_SHORT).show();
            return;
        }

        String text = textFilipino.getText().toString();
        if (!text.isEmpty()) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    @Override
    public void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }
}
