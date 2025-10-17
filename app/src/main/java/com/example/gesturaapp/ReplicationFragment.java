package com.example.gesturaapp;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtSession;

public class ReplicationFragment extends Fragment {

    // UI
    private VideoView videoView;
    private TextView tvScore, tvResult, tvBluetoothData, tvQuestionLabel, textConnectionStatus;
    private Button btnRetry, btnNext, btnConnect, btnPrepare;

    // Quiz state
    private int currentQuestion = 0;
    private int score = 0;
    private List<String> expectedSigns;
    private List<String> videoFiles;

    // Bluetooth
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private InputStream inputStream;
    private Thread workerThread;
    private volatile boolean stopWorker = false;
    private Handler handler = new Handler();
    private boolean isConnected = false;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final int REQUEST_BLUETOOTH_PERMISSIONS = 1001;

    // ONNX
    private OrtEnvironment ortEnv;
    private OrtSession ortSession;
    private String inputName;

    // === Smoothing & voting (UPDATED) ===
    // Use 15-sample window and require 13/15 agreement (~0.8667)
    private final ArrayDeque<String> smoothingWindow = new ArrayDeque<>();
    private static final int SMOOTHING_WINDOW = 15;
    private static final double MAJORITY_THRESHOLD = 13.0 / 15.0; // 13/15 agreement
    // Cooldown for updating displayed gesture (5 seconds)
    private static final long PREDICTION_COOLDOWN = 5000;
    private long lastPredictionTime = 0;
    private String lastStablePrediction = "";

    // For replication voting over a question (kept but evaluate uses displayed gesture)
    private final List<String> questionPredictionBuffer = new ArrayList<>();
    private static final int VOTING_WINDOW_MS = 4000;

    // Model labels fallback (keep consistent with translate)
    private final String[] knownLabels = {"isa", "dalawa", "tatlo", "apat", "lima", "A", "B"};

    public ReplicationFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_replication, container, false);

        // UI references
        videoView = view.findViewById(R.id.videoView);
        tvScore = view.findViewById(R.id.tvScore);
        tvResult = view.findViewById(R.id.tvResult);
        tvBluetoothData = view.findViewById(R.id.tvBluetoothData);
        tvQuestionLabel = view.findViewById(R.id.tvQuestionLabel);
        btnRetry = view.findViewById(R.id.btnRetry);
        btnNext = view.findViewById(R.id.btnNext);
        btnConnect = view.findViewById(R.id.btnConnect);
        btnPrepare = view.findViewById(R.id.btnStart);
        textConnectionStatus = view.findViewById(R.id.tvConnectionStatus);

        // Default visibility
        btnRetry.setVisibility(View.GONE);
        btnNext.setVisibility(View.GONE);
        btnPrepare.setVisibility(View.GONE);

        btnConnect.setOnClickListener(v -> {
            if (checkAndRequestBluetoothPermissions()) selectBluetoothDevice();
        });

        btnPrepare.setOnClickListener(v -> showPrepareDialog());
        btnNext.setOnClickListener(v -> nextQuestion());
        btnRetry.setOnClickListener(v -> resetQuiz());

        // Load ONNX model
        initOnnxModel();

        // Load questions
        setupQuestions();

        tvScore.setText("Score: 0/" + expectedSigns.size());
        tvResult.setText("");

        // Immediately show video & perform label
        showCurrentVideo();

        // Show Connect button initially
        btnConnect.setVisibility(View.VISIBLE);

        return view;
    }

    // ================= Subject & Videos =================
    private void setupQuestions() {
        String subject = getArguments() != null ? getArguments().getString("subject", "Alphabet") : "Alphabet";

        expectedSigns = new ArrayList<>();
        videoFiles = new ArrayList<>();

        switch (subject) {
            case "Alphabet":
                Collections.addAll(expectedSigns, "A","B","C","D","E");
                Collections.addAll(videoFiles, "aletter","bletter","cletter","dletter","eletter");
                break;
            case "Numbers":
                Collections.addAll(expectedSigns, "isa","dalawa","tatlo","apat","lima");
                Collections.addAll(videoFiles, "isa","dalawa","tatlo","apat","lima");
                break;
            case "Random":
            default:
                List<String> poolSigns = new ArrayList<>();
                List<String> poolVideos = new ArrayList<>();
                Collections.addAll(poolSigns, "A","B","C","D","E","isa","dalawa","tatlo","apat","lima");
                Collections.addAll(poolVideos, "aletter","bletter","cletter","dletter","eletter","isa","dalawa","tatlo","apat","lima");
                List<Integer> idx = new ArrayList<>();
                for (int i=0;i<poolSigns.size();i++) idx.add(i);
                Collections.shuffle(idx);
                for (int k=0;k<5;k++) {
                    int i = idx.get(k);
                    expectedSigns.add(poolSigns.get(i));
                    videoFiles.add(poolVideos.get(i));
                }
                break;
        }
    }

    // ================= Video Display =================
    private void showCurrentVideo() {
        if (currentQuestion >= videoFiles.size()) return;

        String name = videoFiles.get(currentQuestion);
        tvQuestionLabel.setText("Perform: " + expectedSigns.get(currentQuestion));
        tvResult.setText("");

        int resId = getResources().getIdentifier(name, "raw", requireContext().getPackageName());
        if (resId == 0) {
            Toast.makeText(getContext(), "Video not found: " + name, Toast.LENGTH_SHORT).show();
            return;
        }

        videoView.setVideoURI(Uri.parse("android.resource://" + requireContext().getPackageName() + "/" + resId));
        videoView.setOnPreparedListener(mp -> {
            mp.setLooping(true);
            videoView.start();
        });
    }

    // ================= Prepare / Start =================
    private void showPrepareDialog() {
        if (!isConnected) {
            Toast.makeText(requireContext(), "Please connect to Bluetooth first!", Toast.LENGTH_SHORT).show();
            return;
        }

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Prepare Sign")
                .setMessage("Click Yes when you are ready to perform the sign")
                .setPositiveButton("Yes", (d, w) -> {
                    // Clear buffer and schedule evaluation after VOTING_WINDOW_MS
                    questionPredictionBuffer.clear();
                    handler.postDelayed(this::evaluateGestureForCurrentQuestion, VOTING_WINDOW_MS);
                    Toast.makeText(getContext(), "Collecting gesture...", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .setCancelable(false)
                .show();
    }

    /**
     * EVALUATION UPDATED:
     * Compare the displayed gesture (tvBluetoothData) with expected sign.
     * This uses the stable gesture shown to the user after smoothing + cooldown.
     */
    private void evaluateGestureForCurrentQuestion() {
        String displayed = tvBluetoothData.getText() != null ? tvBluetoothData.getText().toString().trim() : "";
        if (displayed.isEmpty()) {
            tvResult.setText("Incorrect! (no stable gesture detected)");
            Toast.makeText(getContext(), "Invalid gesture!", Toast.LENGTH_SHORT).show();
        } else {
            String expected = expectedSigns.get(currentQuestion);
            if (displayed.equalsIgnoreCase(expected)) {
                score++;
                tvScore.setText("Score: " + score + "/" + expectedSigns.size());
                tvResult.setText("Correct!");
            } else {
                tvResult.setText("Incorrect! Expected: " + expected);
            }
        }

        // show next button so user advances when ready
        btnNext.setVisibility(View.VISIBLE);
    }

    private String majorityVote(List<String> arr) {
        if (arr.isEmpty()) return null;
        HashMap<String, Integer> cnt = new HashMap<>();
        for (String s : arr) {
            String k = s.trim().toLowerCase(Locale.ROOT);
            cnt.put(k, cnt.getOrDefault(k, 0) + 1);
        }
        return Collections.max(cnt.entrySet(), Map.Entry.comparingByValue()).getKey();
    }

    private void nextQuestion() {
        currentQuestion++;
        btnNext.setVisibility(View.GONE);
        if (currentQuestion < expectedSigns.size()) {
            showCurrentVideo();
            btnPrepare.setVisibility(View.VISIBLE);
        } else {
            videoView.stopPlayback();
            tvResult.setText(score >= Math.max(1, (int)Math.ceil(expectedSigns.size()*0.6)) ? "Passed!" : "Failed!");
            btnRetry.setVisibility(View.VISIBLE);
            btnPrepare.setVisibility(View.GONE);
        }
    }

    private void resetQuiz() {
        currentQuestion = 0;
        score = 0;
        tvScore.setText("Score: 0/" + expectedSigns.size());
        tvResult.setText("");
        btnRetry.setVisibility(View.GONE);
        btnNext.setVisibility(View.GONE);
        showCurrentVideo();
        btnPrepare.setVisibility(isConnected ? View.VISIBLE : View.GONE);
    }

    // ================= Bluetooth =================
    private boolean checkAndRequestBluetoothPermissions() {
        List<String> missing = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED)
                missing.add(Manifest.permission.BLUETOOTH_CONNECT);
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED)
                missing.add(Manifest.permission.BLUETOOTH_SCAN);
        } else {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                missing.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (!missing.isEmpty()) {
            requestPermissions(missing.toArray(new String[0]), REQUEST_BLUETOOTH_PERMISSIONS);
            return false;
        }
        return true;
    }

    private void selectBluetoothDevice() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Toast.makeText(requireContext(), "Enable Bluetooth first!", Toast.LENGTH_SHORT).show();
            return;
        }

        Set<BluetoothDevice> paired = bluetoothAdapter.getBondedDevices();
        if (paired == null || paired.isEmpty()) {
            Toast.makeText(requireContext(), "No paired devices found!", Toast.LENGTH_SHORT).show();
            return;
        }

        final BluetoothDevice[] devices = paired.toArray(new BluetoothDevice[0]);
        String[] deviceNames = new String[devices.length];
        for (int i = 0; i < devices.length; i++) deviceNames[i] = devices[i].getName() + "\n" + devices[i].getAddress();

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Choose Bluetooth Device")
                .setItems(deviceNames, (dialog, which) -> connectToDevice(devices[which]))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void connectToDevice(BluetoothDevice device) {
        new Thread(() -> {
            try {
                bluetoothSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
                bluetoothAdapter.cancelDiscovery();
                bluetoothSocket.connect();
                inputStream = bluetoothSocket.getInputStream();
                isConnected = true;
                handler.post(() -> {
                    Toast.makeText(requireContext(), "Connected to " + device.getName(), Toast.LENGTH_SHORT).show();
                    textConnectionStatus.setText("Connected");
                    textConnectionStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    btnPrepare.setVisibility(View.VISIBLE);
                    btnConnect.setVisibility(View.GONE);
                });
                startListeningForData();
            } catch (IOException e) {
                e.printStackTrace();
                handler.post(() -> {
                    Toast.makeText(requireContext(), "Connection failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    textConnectionStatus.setText("Disconnected");
                    textConnectionStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                });
            }
        }).start();
    }

    // ================= Data listener =================
    private void startListeningForData() {
        stopWorker = false;
        workerThread = new Thread(() -> {
            byte delimiter = '\n';
            byte[] readBuffer = new byte[2048];
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
                                handler.post(() -> handleSensorData(data));
                            } else {
                                if (readBufferPosition < readBuffer.length) readBuffer[readBufferPosition++] = b;
                                else readBufferPosition = 0;
                            }
                        }
                    } else Thread.sleep(8);
                } catch (IOException | InterruptedException ex) {
                    stopWorker = true;
                    handler.post(() -> Toast.makeText(requireContext(), "Connection lost", Toast.LENGTH_SHORT).show());
                }
            }
        });
        workerThread.start();
    }

    private void handleSensorData(String data) {
        // parse raw CSV -> float[]
        String[] parts = data.split(",");
        if (parts.length < 8) return;

        try {
            float[] inputValues = new float[8];
            for (int i = 0; i < 8; i++) inputValues[i] = Float.parseFloat(parts[i].trim());

            String pred = runOnnxPrediction(inputValues);
            if (pred == null) pred = "";

            if (!pred.isEmpty()) {
                // smoothing window (we changed window size above)
                smoothingWindow.addLast(pred);
                if (smoothingWindow.size() > SMOOTHING_WINDOW) smoothingWindow.removeFirst();

                String stable = getStablePrediction();
                long now = System.currentTimeMillis();
                // use the 5s cooldown and 13/15 threshold (getStablePrediction enforces threshold)
                if (stable != null && !stable.equals(lastStablePrediction) && (now - lastPredictionTime >= PREDICTION_COOLDOWN)) {
                    lastStablePrediction = stable;
                    lastPredictionTime = now;
                    // update displayed gesture (this is what the checker will compare against)
                    tvBluetoothData.setText(stable);

                    // keep question buffer (optional)
                    questionPredictionBuffer.add(stable);
                }
            }

        } catch (Exception ignored) {}
    }

    private String getStablePrediction() {
        if (smoothingWindow.isEmpty()) return null;
        HashMap<String, Integer> freq = new HashMap<>();
        for (String s : smoothingWindow) freq.put(s.toLowerCase(Locale.ROOT), freq.getOrDefault(s.toLowerCase(Locale.ROOT), 0)+1);
        String best = null; int bestCount=0;
        for (Map.Entry<String,Integer> e: freq.entrySet()) {
            if (e.getValue()>bestCount){bestCount=e.getValue(); best=e.getKey();}
        }
        // require at least 13/15 (MAJORITY_THRESHOLD)
        if (bestCount >= (int)(SMOOTHING_WINDOW * MAJORITY_THRESHOLD)) return best;
        return null;
    }

    // ================= ONNX =================
    private void initOnnxModel() {
        try {
            ortEnv = OrtEnvironment.getEnvironment();
            File modelFile = loadModelFile("gesture_model.onnx");
            ortSession = ortEnv.createSession(modelFile.getAbsolutePath(), new OrtSession.SessionOptions());
            inputName = ortSession.getInputNames().iterator().next();
        } catch (Exception e) {
            ortSession = null; ortEnv=null;
            Log.e("REPL_ONNX","Model load failed: "+e.getMessage());
        }
    }

    private File loadModelFile(String modelName) throws IOException {
        File file = new File(requireContext().getFilesDir(), modelName);
        if (!file.exists()) {
            try (InputStream in = requireContext().getAssets().open(modelName);
                 FileOutputStream out = new FileOutputStream(file)) {
                byte[] buf = new byte[1024]; int r;
                while((r=in.read(buf))!=-1) out.write(buf,0,r);
            }
        }
        return file;
    }

    private String runOnnxPrediction(float[] input) {
        if (ortSession == null || ortEnv == null) return null;
        try {
            long[] shape = new long[]{1, input.length};
            FloatBuffer fb = FloatBuffer.wrap(input);
            OnnxTensor tensor = OnnxTensor.createTensor(ortEnv, fb, shape);

            try (OrtSession.Result result = ortSession.run(Collections.singletonMap(inputName, tensor))) {
                if (result.size() >= 1) {
                    Object out0 = result.get(0).getValue();

                    // Case 1: string array
                    if (out0 instanceof String[]) {
                        String[] labels = (String[]) out0;
                        if (labels.length > 0) return labels[0];
                    }

                    // Case 2: object array
                    if (out0 instanceof Object[]) {
                        Object[] arr = (Object[]) out0;
                        if (arr.length > 0) return arr[0].toString();
                    }

                    // Case 3: float 2D array
                    if (out0 instanceof float[][]) {
                        float[][] probs = (float[][]) out0;
                        if (probs.length > 0) return labelFromProbArray(probs[0]);
                    }

                    // Case 4: double 2D array
                    if (out0 instanceof double[][]) {
                        double[][] probs = (double[][]) out0;
                        if (probs.length > 0) {
                            float[] tmp = new float[probs[0].length];
                            for (int i = 0; i < probs[0].length; i++) tmp[i] = (float) probs[0][i];
                            return labelFromProbArray(tmp);
                        }
                    }
                }

                // Case 5: second output is map
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
        } catch (Exception e) {
            Log.e("REPL_ONNX", "Prediction failed: " + e.getMessage());
        }
        return null;
    }


    private String labelFromProbArray(float[] probs){
        if(probs==null || probs.length==0) return null;
        int maxIdx=0; float maxVal=probs[0];
        for(int i=1;i<probs.length;i++){ if(probs[i]>maxVal){maxVal=probs[i]; maxIdx=i;} }
        if(maxIdx>=0 && maxIdx<knownLabels.length) return knownLabels[maxIdx];
        return null;
    }

    private String labelFromProbArray(float[][] probs){
        if(probs.length==0 || probs[0].length==0) return null;
        return labelFromProbArray(probs[0]);
    }

    @Override
    public void onDestroyView() {
        stopWorker = true;
        if (workerThread!=null) workerThread.interrupt();
        try { if(inputStream!=null) inputStream.close(); } catch(Exception ignored){}
        try { if(bluetoothSocket!=null) bluetoothSocket.close(); } catch(Exception ignored){}
        super.onDestroyView();
    }
}
