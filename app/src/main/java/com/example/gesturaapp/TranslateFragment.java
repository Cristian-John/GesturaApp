package com.example.gesturaapp;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import com.google.android.material.button.MaterialButton;
import java.util.Locale;

public class TranslateFragment extends Fragment {

    private TextView textConnectionStatus;
    private TextView textTranslation;
    private MaterialButton btnBluetoothConnect;
    private MaterialButton btnBluetoothDisconnect;
    private MaterialButton btnTextToSpeech;

    private TextToSpeech textToSpeech;
    private boolean isConnected = false;

    public TranslateFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_translate, container, false);

        // Initialize views
        textConnectionStatus = view.findViewById(R.id.textConnectionStatus);
        textTranslation = view.findViewById(R.id.textTranslation);
        btnBluetoothConnect = view.findViewById(R.id.btnBluetoothConnect);
        btnBluetoothDisconnect = view.findViewById(R.id.btnBluetoothDisconnect);
        btnTextToSpeech = view.findViewById(R.id.btnTextToSpeech);

        btnBluetoothConnect.setOnClickListener(v -> connectBluetooth());
        btnBluetoothDisconnect.setOnClickListener(v -> disconnectBluetooth());
        btnTextToSpeech.setOnClickListener(v -> speakTranslation());

        // Initialize Text-to-Speech
        textToSpeech = new TextToSpeech(requireContext(), status -> {
            if (status != TextToSpeech.ERROR) {
                textToSpeech.setLanguage(new Locale("fil", "PH"));
            }
        });

        return view;
    }

    private void connectBluetooth() {
        isConnected = true;
        showConnectedAnimation();
        Toast.makeText(requireContext(), "Bluetooth Connected!", Toast.LENGTH_SHORT).show();
    }

    private void disconnectBluetooth() {
        isConnected = false;
        textConnectionStatus.setText("Disconnected");
        textConnectionStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        Toast.makeText(requireContext(), "Bluetooth Disconnected", Toast.LENGTH_SHORT).show();
    }

    private void speakTranslation() {
        if (!isConnected) {
            Toast.makeText(requireContext(), "Please connect to Bluetooth first.", Toast.LENGTH_SHORT).show();
            return;
        }

        String text = textTranslation.getText().toString();
        if (!text.isEmpty()) {
            animateMicButton();
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    private void showConnectedAnimation() {
        textConnectionStatus.setText("Connected");
        textConnectionStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));

        Animation fadeInOut = new AlphaAnimation(0.0f, 1.0f);
        fadeInOut.setDuration(800);
        fadeInOut.setRepeatCount(3);
        fadeInOut.setRepeatMode(Animation.REVERSE);
        textConnectionStatus.startAnimation(fadeInOut);
    }

    private void animateMicButton() {
        Animation pulse = new AlphaAnimation(0.3f, 1.0f);
        pulse.setDuration(500);
        pulse.setRepeatCount(3);
        pulse.setRepeatMode(Animation.REVERSE);
        btnTextToSpeech.startAnimation(pulse);
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
