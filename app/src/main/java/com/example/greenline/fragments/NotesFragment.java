package com.example.greenline.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.greenline.R;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class NotesFragment extends Fragment {

    private static final String TAG = "NotesFragment";
    private EditText editNote;
    private Button btnSave, btnLoad;
    private static final String FILE_NAME = "notes.txt";

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Log.d(TAG, "onAttach");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        return inflater.inflate(R.layout.fragment_notes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        editNote = view.findViewById(R.id.edit_note);
        btnSave = view.findViewById(R.id.btn_save);
        btnLoad = view.findViewById(R.id.btn_load);

        btnSave.setOnClickListener(v -> saveNote());
        btnLoad.setOnClickListener(v -> loadNote());

        loadNote();
    }

    private void saveNote() {
        if (getActivity() == null) return;
        String text = editNote.getText().toString();
        if (text.isEmpty()) {
            Toast.makeText(getContext(), "Нет текста для сохранения", Toast.LENGTH_SHORT).show();
            return;
        }
        try (FileOutputStream fos = getActivity().openFileOutput(FILE_NAME, Context.MODE_PRIVATE)) {
            fos.write(text.getBytes());
            Toast.makeText(getContext(), "Сохранено", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(getContext(), "Ошибка сохранения", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadNote() {
        if (getActivity() == null) return;
        try (FileInputStream fis = getActivity().openFileInput(FILE_NAME)) {
            byte[] data = new byte[fis.available()];
            fis.read(data);
            editNote.setText(new String(data));
            Toast.makeText(getContext(), "Загружено", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            editNote.setText("");
            Toast.makeText(getContext(), "Нет сохранений", Toast.LENGTH_SHORT).show();
        }
    }

    @Override public void onStart() { super.onStart(); Log.d(TAG, "onStart"); }
    @Override public void onResume() { super.onResume(); Log.d(TAG, "onResume"); }
    @Override public void onPause() { super.onPause(); Log.d(TAG, "onPause"); }
    @Override public void onStop() { super.onStop(); Log.d(TAG, "onStop"); }
    @Override public void onDestroyView() { super.onDestroyView(); Log.d(TAG, "onDestroyView"); }
    @Override public void onDestroy() { super.onDestroy(); Log.d(TAG, "onDestroy"); }
    @Override public void onDetach() { super.onDetach(); Log.d(TAG, "onDetach"); }
}