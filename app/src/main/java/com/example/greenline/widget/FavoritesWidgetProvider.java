package com.example.greenline.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.example.greenline.MainActivity;
import com.example.greenline.R;
import com.example.greenline.models.Place;

import java.util.ArrayList;
import java.util.List;

public class FavoritesWidgetProvider extends AppWidgetProvider {

    private static final String PREFS_NAME = "GreenLinePrefs";
    private static final String ACTION_OPEN_APP = "OPEN_APP";
    private static final String ACTION_OPEN_ROUTE = "OPEN_ROUTE";
    private static final String EXTRA_LAT = "extra_lat";
    private static final String EXTRA_LNG = "extra_lng";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_favorites);

        // Загружаем избранные места
        List<Place> favorites = getFavorites(context);

        // Показываем до 3 мест
        showFavorite(context, views, favorites, 0, R.id.fav_item_1, R.id.fav_title_1, R.id.fav_route_1);
        showFavorite(context, views, favorites, 1, R.id.fav_item_2, R.id.fav_title_2, R.id.fav_route_2);
        showFavorite(context, views, favorites, 2, R.id.fav_item_3, R.id.fav_title_3, R.id.fav_route_3);

        // Кнопка обновления
        views.setOnClickPendingIntent(R.id.tv_update, getUpdatePendingIntent(context, appWidgetId));

        // Открыть приложение
        views.setOnClickPendingIntent(R.id.tv_open_app, getOpenAppPendingIntent(context));

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private static void showFavorite(Context context, RemoteViews views, List<Place> favorites,
                                     int index, int itemId, int titleId, int routeId) {
        if (index < favorites.size()) {
            Place place = favorites.get(index);
            views.setViewVisibility(itemId, android.view.View.VISIBLE);
            views.setTextViewText(titleId, place.title);
            views.setOnClickPendingIntent(routeId, getRoutePendingIntent(context, place.lat, place.lng, place.title));
            views.setOnClickPendingIntent(itemId, getOpenAppPendingIntent(context));
        } else {
            views.setViewVisibility(itemId, android.view.View.GONE);
        }
    }

    private static List<Place> getFavorites(Context context) {
        List<Place> favorites = new ArrayList<>();
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        String saved = prefs.getString("favorites", "");
        if (saved.isEmpty()) return favorites;

        String[] titles = saved.split("\\|");
        for (String title : titles) {
            String address = prefs.getString(title + "_address", "");
            float lat = prefs.getFloat(title + "_lat", 0);
            float lng = prefs.getFloat(title + "_lng", 0);
            float rating = prefs.getFloat(title + "_rating", 0);
            if (!address.isEmpty()) {
                favorites.add(new Place(title, address, lat, lng, rating));
            }
        }
        return favorites;
    }

    private static PendingIntent getUpdatePendingIntent(Context context, int appWidgetId) {
        Intent intent = new Intent(context, FavoritesWidgetProvider.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[]{appWidgetId});
        return PendingIntent.getBroadcast(context, appWidgetId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private static PendingIntent getOpenAppPendingIntent(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private static PendingIntent getRoutePendingIntent(Context context, double lat, double lng, String title) {
        String uri = "yandexmaps://maps.yandex.ru/?rtext=~" + lat + "," + lng;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return PendingIntent.getActivity(context, (int) (lat * 1000), intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }
}