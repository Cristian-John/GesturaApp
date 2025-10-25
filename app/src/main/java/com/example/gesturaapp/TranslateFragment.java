package com.example.gesturaapp;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtSession;

public class TranslateFragment extends Fragment {

    private TextView textConnectionStatus;
    private TextView textEnglish;
    private TextView textFilipino;
    private Button btnBluetoothConnect;
    private Button btnTextToSpeech;
    private ImageView iconConnection;

    private boolean isConnected = false;
    private TextToSpeech textToSpeech;

    // Bluetooth
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private InputStream inputStream;
    private Thread workerThread;
    private boolean stopWorker;
    private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothDevice selectedDevice;
    private static final int REQUEST_BLUETOOTH_PERMISSIONS = 1001;

    // ONNX
    private OrtEnvironment ortEnv;
    private OrtSession ortSession;
    private String inputName;

    // Track last prediction & cooldown
    private String lastPrediction = "";
    private long lastPredictionTime = 0;
    private static final long PREDICTION_COOLDOWN = 3000; // 3 seconds

    // Prediction smoothing
    private static final int SMOOTHING_WINDOW = 15; // how many past predictions to keep
    private static final double MAJORITY_THRESHOLD = 0.6; // e.g. 9/15 votes needed
    private final ArrayDeque<String> predictionWindow = new ArrayDeque<>();

    public TranslateFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_translate, container, false);

        textConnectionStatus = view.findViewById(R.id.textConnectionStatus);
        textEnglish = view.findViewById(R.id.textEnglish);
        textFilipino = view.findViewById(R.id.textTranslation);
        btnBluetoothConnect = view.findViewById(R.id.btnBluetoothConnect);
        btnTextToSpeech = view.findViewById(R.id.btnTextToSpeech);
        iconConnection = view.findViewById(R.id.iconConnection);

        // TTS in Filipino
        textToSpeech = new TextToSpeech(requireContext(), status -> {
            if (status != TextToSpeech.ERROR) {
                textToSpeech.setLanguage(new Locale("fil", "PH"));
            }
        });

        // Load ONNX model
        initOnnxModel();

        btnBluetoothConnect.setOnClickListener(v -> {
            if (checkAndRequestBluetoothPermissions()) {
                toggleBluetooth();
            }
        });

        btnTextToSpeech.setOnClickListener(v -> speakFilipino());

        return view;
    }

    /** ================== ONNX LOADING ================== **/
    private void initOnnxModel() {
        try {
            ortEnv = OrtEnvironment.getEnvironment();
            File modelFile = loadModelFile("gesture_model.onnx");
            ortSession = ortEnv.createSession(modelFile.getAbsolutePath(),
                    new OrtSession.SessionOptions());
            inputName = ortSession.getInputNames().iterator().next();
            Log.d("ONNX", "Model loaded. Input name: " + inputName);
        } catch (Exception e) {
            Log.e("ONNX", "Failed to load model: " + e.getMessage());
        }
    }

    private File loadModelFile(String modelName) throws IOException {
        File file = new File(requireContext().getFilesDir(), modelName);
        if (!file.exists()) {
            try (InputStream in = requireContext().getAssets().open(modelName);
                 FileOutputStream out = new FileOutputStream(file)) {
                byte[] buffer = new byte[1024];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
            }
        }
        return file;
    }

    /** ================== BLUETOOTH ================== **/
    private boolean checkAndRequestBluetoothPermissions() {
        List<String> missingPermissions = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT)
                    != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(Manifest.permission.BLUETOOTH_CONNECT);
            }
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_SCAN)
                    != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(Manifest.permission.BLUETOOTH_SCAN);
            }
        } else {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }
        }

        if (!missingPermissions.isEmpty()) {
            requestPermissions(missingPermissions.toArray(new String[0]), REQUEST_BLUETOOTH_PERMISSIONS);
            return false;
        }
        return true;
    }

    private void toggleBluetooth() {
        if (!isConnected) {
            connectToBluetoothDevice();
        } else {
            disconnectBluetooth();
        }
    }

    private void connectToBluetoothDevice() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Toast.makeText(requireContext(), "Please enable Bluetooth first", Toast.LENGTH_SHORT).show();
            return;
        }

        Set<BluetoothDevice> pairedDevices;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT)
                        != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(requireContext(), "Bluetooth permission required", Toast.LENGTH_SHORT).show();
            return;
        } else {
            pairedDevices = bluetoothAdapter.getBondedDevices();
        }

        if (pairedDevices.isEmpty()) {
            Toast.makeText(requireContext(), "No paired devices found", Toast.LENGTH_SHORT).show();
            return;
        }

        final BluetoothDevice[] devices = pairedDevices.toArray(new BluetoothDevice[0]);
        String[] deviceNames = new String[devices.length];
        for (int i = 0; i < devices.length; i++) {
            deviceNames[i] = devices[i].getName() + "\n" + devices[i].getAddress();
        }

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Choose Bluetooth Device")
                .setItems(deviceNames, (dialog, which) -> {
                    selectedDevice = devices[which];
                    try {
                        bluetoothSocket = selectedDevice.createRfcommSocketToServiceRecord(MY_UUID);
                        bluetoothAdapter.cancelDiscovery();
                        bluetoothSocket.connect();
                        inputStream = bluetoothSocket.getInputStream();
                        isConnected = true;
                        updateUIConnected(true);
                        startListeningForData();
                        Toast.makeText(requireContext(), "Connected to " + selectedDevice.getName(), Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(requireContext(), "Connection failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        updateUIConnected(false);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void disconnectBluetooth() {
        stopWorker = true;
        try {
            if (inputStream != null) inputStream.close();
            if (bluetoothSocket != null) bluetoothSocket.close();
        } catch (IOException e) { e.printStackTrace(); }
        isConnected = false;
        updateUIConnected(false);
        Toast.makeText(requireContext(), "Bluetooth Disconnected", Toast.LENGTH_SHORT).show();
    }

    private void updateUIConnected(boolean connected) {
        isConnected = connected;
        if (connected) {
            textConnectionStatus.setText("Connected");
            textConnectionStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            btnBluetoothConnect.setText("Disconnect");
            btnBluetoothConnect.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(android.R.color.holo_green_dark)));
            iconConnection.setImageResource(R.drawable.bluetoothconnected);
        } else {
            textConnectionStatus.setText("Disconnected");
            textConnectionStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            btnBluetoothConnect.setText("Connect");
            btnBluetoothConnect.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(android.R.color.holo_red_dark)));
            iconConnection.setImageResource(R.drawable.bluetoothdisconnectedsymbolic);
        }
        AlphaAnimation animation = new AlphaAnimation(0.5f, 1.0f);
        animation.setDuration(300);
        btnBluetoothConnect.startAnimation(animation);
    }

    /** ================== DATA LISTENER ================== **/
    private void startListeningForData() {
        stopWorker = false;
        workerThread = new Thread(() -> {
            byte delimiter = '\n';
            byte[] readBuffer = new byte[1024];
            int readBufferPosition = 0;

            while (!Thread.currentThread().isInterrupted() && !stopWorker) {
                try {
                    int bytesAvailable = inputStream.available();
                    if (bytesAvailable > 0) {
                        byte[] packetBytes = new byte[bytesAvailable];
                        inputStream.read(packetBytes);
                        for (int i = 0; i < bytesAvailable; i++) {
                            byte b = packetBytes[i];
                            if (b == delimiter) {
                                byte[] encodedBytes = new byte[readBufferPosition];
                                System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                final String data = new String(encodedBytes, StandardCharsets.UTF_8).trim();
                                readBufferPosition = 0;

                                requireActivity().runOnUiThread(() -> handleRawSensorData(data));
                            } else {
                                readBuffer[readBufferPosition++] = b;
                            }
                        }
                    }
                } catch (IOException ex) {
                    stopWorker = true;
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Connection lost", Toast.LENGTH_SHORT).show();
                        disconnectBluetooth();
                    });
                }
            }
        });
        workerThread.start();
    }

    private void handleRawSensorData(String data) {
        textEnglish.setText(data);

        String[] parts = data.split(",");
        if (parts.length < 8) return; // Expect 8 values (5 flex + 3 accel)

        try {
            float[] inputValues = new float[8];
            for (int i = 0; i < 8; i++) {
                inputValues[i] = Float.parseFloat(parts[i].trim());
            }

            String prediction = runOnnxPrediction(inputValues);
            if (prediction != null) {
                // add to smoothing window
                predictionWindow.addLast(prediction);
                if (predictionWindow.size() > SMOOTHING_WINDOW) {
                    predictionWindow.removeFirst();
                }

                String stablePrediction = getStablePrediction();
                long currentTime = System.currentTimeMillis();

                if (stablePrediction != null &&
                        !stablePrediction.equals(lastPrediction) &&
                        (currentTime - lastPredictionTime >= PREDICTION_COOLDOWN)) {

                    lastPrediction = stablePrediction;
                    lastPredictionTime = currentTime;

                    textFilipino.setText(stablePrediction);
                    textToSpeech.speak(stablePrediction, TextToSpeech.QUEUE_FLUSH, null, null);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getStablePrediction() {
        if (predictionWindow.isEmpty()) return null;

        // Count frequencies
        java.util.Map<String, Integer> freq = new java.util.HashMap<>();
        for (String p : predictionWindow) {
            freq.put(p, freq.getOrDefault(p, 0) + 1);
        }

        String bestLabel = null;
        int bestCount = 0;
        for (java.util.Map.Entry<String, Integer> entry : freq.entrySet()) {
            if (entry.getValue() > bestCount) {
                bestCount = entry.getValue();
                bestLabel = entry.getKey();
            }
        }

        // apply majority threshold
        if (bestCount >= (int)(SMOOTHING_WINDOW * MAJORITY_THRESHOLD)) {
            return bestLabel;
        }
        return null;
    }

    /** ================== ONNX INFERENCE ================== **/
    private String runOnnxPrediction(float[] input) {
        if (ortSession == null || ortEnv == null) {
            Log.e("ONNX", "Session or Env not initialized");
            return null;
        }

        try {
            long[] shape = new long[]{1, input.length};
            FloatBuffer fb = FloatBuffer.wrap(input);
            OnnxTensor tensor = OnnxTensor.createTensor(ortEnv, fb, shape);

            try (OrtSession.Result result = ortSession.run(Collections.singletonMap(inputName, tensor))) {
                if (result.size() >= 1) {
                    Object out0 = result.get(0).getValue();
                    if (out0 instanceof String[]) {
                        String[] labels = (String[]) out0;
                        if (labels.length > 0) return labels[0];
                    }
                    if (out0 instanceof Object[]) {
                        Object[] arr = (Object[]) out0;
                        if (arr.length > 0) return arr[0].toString();
                    }
                    if (out0 instanceof float[][]) {
                        float[][] probs = (float[][]) out0;
                        return labelFromProbArray(probs[0]);
                    }
                    if (out0 instanceof double[][]) {
                        double[][] probs = (double[][]) out0;
                        float[] tmp = new float[probs[0].length];
                        for (int i = 0; i < probs[0].length; i++) tmp[i] = (float) probs[0][i];
                        return labelFromProbArray(tmp);
                    }
                }

                if (result.size() >= 2) {
                    Object out1 = result.get(1).getValue();
                    if (out1 instanceof Map) {
                        Map<?, ?> probsMap = (Map<?, ?>) out1;
                        String bestLabel = null;
                        double bestProb = -Double.MAX_VALUE;
                        for (Map.Entry<?, ?> e : probsMap.entrySet()) {
                            Object key = e.getKey();
                            Object val = e.getValue();
                            double p = 0.0;
                            if (val instanceof Number) p = ((Number) val).doubleValue();
                            else {
                                try { p = Double.parseDouble(val.toString()); } catch (Exception ignored) {}
                            }
                            if (p > bestProb) {
                                bestProb = p;
                                bestLabel = key.toString();
                            }
                        }
                        if (bestLabel != null) return bestLabel;
                    }
                }
            }

            Log.e("ONNX", "No usable outputs from model");
            return null;

        } catch (Exception e) {
            Log.e("ONNX", "Prediction failed: " + e.getMessage());
            return null;
        }
    }

    private String labelFromProbArray(float[] probs) {
        if (probs == null || probs.length == 0) return null;
        int maxIdx = 0;
        float maxVal = probs[0];
        for (int i = 1; i < probs.length; i++) {
            if (probs[i] > maxVal) {
                maxVal = probs[i];
                maxIdx = i;
            }
        }
        String[] labels = {"Paalam", "Ano", "Itlog","Malakas", "Mahal Kita"};
        if (maxIdx < labels.length) return labels[maxIdx];
        return "label_" + maxIdx;
    }

    private void speakFilipino() {
        String text = textFilipino.getText().toString();
        if (!text.isEmpty() && textToSpeech != null) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    @Override
    public void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        stopWorker = true;
        try {
            if (inputStream != null) inputStream.close();
            if (bluetoothSocket != null) bluetoothSocket.close();
        } catch (IOException ignored) {}
        super.onDestroy();
    }
}
