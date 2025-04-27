package com.example.gesturaapp;

import android.os.Bundle;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.VideoView;
import android.widget.Toast;  // For displaying toast messages in case of errors
import androidx.fragment.app.Fragment;

public class VideoLessonFragment extends Fragment {

    private VideoView videoView;
    private Button playButton, pauseButton, stopButton;
    private SeekBar seekBar;

    public VideoLessonFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_video_lesson, container, false);

        // Initialize UI elements
        videoView = view.findViewById(R.id.video_view);
        playButton = view.findViewById(R.id.play_button);
        pauseButton = view.findViewById(R.id.pause_button);
        stopButton = view.findViewById(R.id.stop_button);
        seekBar = view.findViewById(R.id.seek_bar);

        // Set up MediaController for video controls
        MediaController mediaController = new MediaController(getContext());
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);

        // Get the category title from arguments and handle null case
        String categoryTitle = getArguments() != null ? getArguments().getString("category_title") : null;

        if (categoryTitle == null) {
            // If categoryTitle is null, display a message and load a default video
            Toast.makeText(getContext(), "Category not found. Loading default video.", Toast.LENGTH_SHORT).show();
            categoryTitle = "Default";  // Use a default category for this case
        }

        // Set video URL based on category (this is an example, you should set actual video URLs)
        String videoUrl = getVideoUrlForCategory(categoryTitle);
        Uri videoUri = Uri.parse(videoUrl);
        videoView.setVideoURI(videoUri);

        // Play button click listener
        playButton.setOnClickListener(v -> videoView.start());

        // Pause button click listener
        pauseButton.setOnClickListener(v -> videoView.pause());

        // Stop button click listener
        stopButton.setOnClickListener(v -> {
            videoView.stopPlayback();
            videoView.seekTo(0); // Reset to the beginning of the video
        });

        // Seek bar listener to track video progress
        videoView.setOnPreparedListener(mp -> {
            int duration = videoView.getDuration();
            seekBar.setMax(duration);
            updateSeekBar();
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    videoView.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        return view;
    }

    // Method to get the video URL based on the category
    private String getVideoUrlForCategory(String categoryTitle) {
        switch (categoryTitle) {
            case "Alphabet":
                return "https://www.example.com/alphabet_video.mp4";
            case "Animals":
                return "https://www.example.com/animals_video.mp4";
            // Add more cases for other categories
            default:
                return "https://www.example.com/default_video.mp4";
        }
    }

    // Method to update the SeekBar as the video plays
    private void updateSeekBar() {
        seekBar.postDelayed(new Runnable() {
            @Override
            public void run() {
                int currentPosition = videoView.getCurrentPosition();
                seekBar.setProgress(currentPosition);
                updateSeekBar();
            }
        }, 1000);
    }
}
