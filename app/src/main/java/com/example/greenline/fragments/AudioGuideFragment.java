package com.example.greenline.fragments;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.greenline.R;
import com.example.greenline.models.Place;

import java.util.ArrayList;
import java.util.List;

public class AudioGuideFragment extends Fragment {

    private static final String TAG = "AudioGuideFragment";
    private Spinner spinnerPlaces;
    private Button btnPlay, btnPause, btnStop;
    private SeekBar seekBar;
    private TextView tvCurrentTime, tvDuration;

    private MediaPlayer mediaPlayer;
    private List<Place> placeList;
    private Handler handler = new Handler();
    private boolean isPlaying = false;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Log.d(TAG, "onAttach");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        return inflater.inflate(R.layout.fragment_audioguide, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        loadPlaces();
        setupSpinner();
        setupMediaControls();
    }

    private void initViews(View view) {
        spinnerPlaces = view.findViewById(R.id.spinner_places);
        btnPlay = view.findViewById(R.id.btn_play);
        btnPause = view.findViewById(R.id.btn_pause);
        btnStop = view.findViewById(R.id.btn_stop);
        seekBar = view.findViewById(R.id.seek_bar);
        tvCurrentTime = view.findViewById(R.id.tv_current_time);
        tvDuration = view.findViewById(R.id.tv_duration);
    }

    private void loadPlaces() {
        placeList = new ArrayList<>();
        placeList.add(new Place("Дом Мешкова", "", 0, 0, 0, "", R.raw.dom_meshkova));
        placeList.add(new Place("Пермский театр оперы и балета имени П. И. Чайковского", "", 0, 0, 0, "", R.raw.teatr));
        placeList.add(new Place("Стела трудовой доблести", "", 0, 0, 0, "", R.raw.stela));
        placeList.add(new Place("Счастье не за горами", "", 0, 0, 0, "", R.raw.schastie));
        placeList.add(new Place("Пермяк соленые уши", "", 0, 0, 0, "", R.raw.ushi));
        placeList.add(new Place("Пермская художественная галерея", "", 0, 0, 0, "", R.raw.galereya));
    }

    private void setupSpinner() {
        List<String> titles = new ArrayList<>();
        for (Place p : placeList) titles.add(p.title);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, titles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPlaces.setAdapter(adapter);

        spinnerPlaces.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                stopAudio();
                loadAudio(position);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadAudio(int position) {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        int audioRes = placeList.get(position).audioRes;
        if (audioRes != 0) {
            mediaPlayer = MediaPlayer.create(getContext(), audioRes);
            if (mediaPlayer != null) {
                seekBar.setMax(mediaPlayer.getDuration());
                tvDuration.setText(formatTime(mediaPlayer.getDuration()));
                Toast.makeText(getContext(), "Загружено: " + placeList.get(position).title, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setupMediaControls() {
        btnPlay.setOnClickListener(v -> playAudio());
        btnPause.setOnClickListener(v -> pauseAudio());
        btnStop.setOnClickListener(v -> stopAudio());

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null) mediaPlayer.seekTo(progress);
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });
    }

    private void playAudio() {
        if (mediaPlayer != null && !isPlaying) {
            mediaPlayer.start();
            isPlaying = true;
            startUpdater();
            Toast.makeText(getContext(), "Воспроизведение", Toast.LENGTH_SHORT).show();
        }
    }

    private void pauseAudio() {
        if (mediaPlayer != null && isPlaying) {
            mediaPlayer.pause();
            isPlaying = false;
            Toast.makeText(getContext(), "Пауза", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopAudio() {
        if (mediaPlayer != null) {
            if (isPlaying) {
                mediaPlayer.pause();
                isPlaying = false;
            }
            mediaPlayer.seekTo(0);
            seekBar.setProgress(0);
            tvCurrentTime.setText("00:00");
            Toast.makeText(getContext(), "Остановлено", Toast.LENGTH_SHORT).show();
        }
    }

    private void startUpdater() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && isPlaying) {
                    seekBar.setProgress(mediaPlayer.getCurrentPosition());
                    tvCurrentTime.setText(formatTime(mediaPlayer.getCurrentPosition()));
                    handler.postDelayed(this, 500);
                }
            }
        }, 500);
    }

    private String formatTime(int ms) {
        int sec = ms / 1000;
        int min = sec / 60;
        sec = sec % 60;
        return String.format("%02d:%02d", min, sec);
    }

    @Override public void onStop() {
        super.onStop();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        Log.d(TAG, "onStop - MediaPlayer released");
    }

    @Override public void onStart() { super.onStart(); Log.d(TAG, "onStart"); }
    @Override public void onResume() { super.onResume(); Log.d(TAG, "onResume"); }
    @Override public void onPause() { super.onPause(); Log.d(TAG, "onPause"); }
}