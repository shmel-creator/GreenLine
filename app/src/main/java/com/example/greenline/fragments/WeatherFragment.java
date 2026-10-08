package com.example.greenline.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.greenline.R;

public class WeatherFragment extends Fragment {

    private static final String TAG = "WeatherFragment";

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Log.d(TAG, "onAttach");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        return inflater.inflate(R.layout.fragment_weather, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        WebView webView = view.findViewById(R.id.webview_weather);

        // Настройки WebView для производительности
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        settings.setDomStorageEnabled(true);  // включить DOM storage
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);

        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl("https://yandex.ru/pogoda/perm");
    }

    @Override public void onStart() { super.onStart(); Log.d(TAG, "onStart"); }
    @Override public void onResume() { super.onResume(); Log.d(TAG, "onResume"); }
    @Override public void onPause() { super.onPause(); Log.d(TAG, "onPause"); }
    @Override public void onStop() { super.onStop(); Log.d(TAG, "onStop"); }
    @Override public void onDestroyView() { super.onDestroyView(); Log.d(TAG, "onDestroyView"); }
    @Override public void onDestroy() { super.onDestroy(); Log.d(TAG, "onDestroy"); }
    @Override public void onDetach() { super.onDetach(); Log.d(TAG, "onDetach"); }
}