package com.example.gesturaapp;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Locale;

public class VideoPlayerFragment extends Fragment implements TextToSpeech.OnInitListener {

    private static final String ARG_TITLE = "title";
    private static final String ARG_VIDEO_URI = "video_uri";

    private String title;
    private String videoUriString;

    private VideoView videoView;
    private ImageButton btnPlayPause;
    private ImageButton btnLoop;
    private SeekBar videoSeekBar;
    private boolean isLooping = false;

    private final Handler handler = new Handler();

    private TextToSpeech tts;
    private boolean ttsReady = false;
    private boolean videoReady = false;

    public static VideoPlayerFragment newInstance(String title, String videoUriString) {
        VideoPlayerFragment fragment = new VideoPlayerFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_VIDEO_URI, videoUriString);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_video_player, container, false);

        if (getArguments() != null) {
            title = getArguments().getString(ARG_TITLE);
            videoUriString = getArguments().getString(ARG_VIDEO_URI);
        }

        TextView videoTitle = view.findViewById(R.id.videoTitle);
        videoTitle.setText(title);

        videoView = view.findViewById(R.id.videoView);
        btnPlayPause = view.findViewById(R.id.btnPlayPause);
        btnLoop = view.findViewById(R.id.btnLoop);
        videoSeekBar = view.findViewById(R.id.videoSeekBar);

        // Initialize TTS
        tts = new TextToSpeech(getContext(), this);

        setupVideo();

        return view;
    }

    private void setupVideo() {
        if (videoUriString == null || videoUriString.isEmpty()) {
            Toast.makeText(getContext(), "Video URI is missing.", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            Uri videoUri = Uri.parse(videoUriString);
            videoView.setVideoURI(videoUri);
            videoView.requestFocus();

            videoView.setOnPreparedListener(mp -> {
                // Adjust VideoView size to maintain aspect ratio
                int videoWidth = mp.getVideoWidth();
                int videoHeight = mp.getVideoHeight();
                float videoProportion = (float) videoWidth / (float) videoHeight;

                int screenWidth = getResources().getDisplayMetrics().widthPixels;
                int newHeight = (int) (screenWidth / videoProportion);

                ViewGroup.LayoutParams layoutParams = videoView.getLayoutParams();
                layoutParams.width = screenWidth;
                layoutParams.height = newHeight;
                videoView.setLayoutParams(layoutParams);

                videoSeekBar.setMax(videoView.getDuration());
                updateSeekBar();
                videoView.start();
                btnPlayPause.setImageResource(android.R.drawable.ic_media_pause);

                videoReady = true;
                if (ttsReady) {
                    speakFilipinoPart();
                }
            });

            videoView.setOnCompletionListener(mp -> {
                if (isLooping) {
                    videoView.seekTo(0);
                    videoView.start();

                    // 🔊 Safely trigger TTS on loop replay
                    if (tts != null && ttsReady) {
                        speakFilipinoPart();
                    }
                } else {
                    btnPlayPause.setImageResource(android.R.drawable.ic_media_play);
                }
            });


            videoView.setOnErrorListener((mp, what, extra) -> {
                Toast.makeText(getContext(), "Error playing video", Toast.LENGTH_SHORT).show();
                return true;
            });

        } catch (Exception e) {
            Toast.makeText(getContext(), "Failed to load video", Toast.LENGTH_SHORT).show();
            Log.e("VideoPlayerFragment", "Error: ", e);
        }

        btnPlayPause.setOnClickListener(v -> {
            if (videoView.isPlaying()) {
                videoView.pause();
                btnPlayPause.setImageResource(android.R.drawable.ic_media_play);
            } else {
                videoView.start();
                btnPlayPause.setImageResource(android.R.drawable.ic_media_pause);

                // 🔊 Safely trigger TTS when playing
                if (tts != null && ttsReady) {
                    speakFilipinoPart();
                }
            }
        });


        btnLoop.setOnClickListener(v -> {
            isLooping = !isLooping;
            Toast.makeText(getContext(), "Looping: " + (isLooping ? "On" : "Off"), Toast.LENGTH_SHORT).show();
        });

        videoSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            boolean wasPlaying = false;

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (videoView.isPlaying()) {
                    wasPlaying = true;
                    videoView.pause();
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                videoView.seekTo(seekBar.getProgress());
                if (wasPlaying) {
                    videoView.start();
                    wasPlaying = false;
                }
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // no-op
            }
        });
    }

    private void updateSeekBar() {
        if (videoView != null && videoView.isPlaying()) {
            videoSeekBar.setProgress(videoView.getCurrentPosition());
        }
        handler.postDelayed(this::updateSeekBar, 500);
    }

    private void speakFilipinoPart() {
        if (title == null || !ttsReady) {
            Log.w("TTS", "speakFilipinoPart skipped: ttsReady=" + ttsReady);
            return;
        }

        String[] parts = title.split("/");
        String filipinoPart = parts.length > 1 ? parts[1].trim() : parts[0].trim();

        Log.i("TTS", "Speaking: " + filipinoPart);
        tts.speak(filipinoPart, TextToSpeech.QUEUE_FLUSH, null, "tts1");
    }


    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            Locale filipino = new Locale("fil", "PH");
            int result = tts.setLanguage(filipino);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "Filipino language not supported");
            } else {
                ttsReady = true;
                Log.i("TTS", "TTS initialized and ready");

                if (videoReady) {
                    speakFilipinoPart();
                }
            }
        } else {
            Log.e("TTS", "Initialization failed");
        }
    }

    @Override
    public void onDestroyView() {
        handler.removeCallbacksAndMessages(null);
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroyView();
    }
}
