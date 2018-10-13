package com.example.mizuho.natureremowidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.widget.RemoteViews;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;


public class RoomMonitorWidget extends BaseWidget {

    static final String TAG = "TEST_ROOM_WID";
    static final String BUTTON_PUSHED_ACTION = "android.appwidget.action.BUTTON_PUSHED";
    final String TOKEN_FILE_NAME = "token.txt";

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        SetWidget(context, appWidgetId, 0, 0, 0);

    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }


    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        Log.d(TAG, "onReceive");

        int appWidgetId = getWidgetId(intent);

        // どのボタンが押されたのか
        String action = intent.getAction();
        Log.d(TAG, "ACTION: " + action);

        if (action.equals(BUTTON_PUSHED_ACTION)) {
            GetStateRunnable getStateRunnable = new GetStateRunnable(context, appWidgetId);
            Thread thread = new Thread(getStateRunnable);
            thread.start();
        }
    }


    // ボタンを押した時の処理
    public class GetStateRunnable implements Runnable {
        Context context;
        int appWidgetId;

        GetStateRunnable(Context context, int appWidgetId) {
            this.context = context;
            this.appWidgetId = appWidgetId;
        }

        @Override
        public void run() {
            String response = getState(context);

            try {
                // responseから必要な情報を取得して保存
                // エアコンの情報を取り出す
                JSONArray jsonArray = new JSONArray(response);
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                JSONObject newest_events = jsonObject.getJSONObject("newest_events");
                JSONObject hu = newest_events.getJSONObject("hu");
                JSONObject il = newest_events.getJSONObject("il");
                JSONObject te = newest_events.getJSONObject("te");

                double hu_val = hu.getDouble("val");
                double il_val = il.getDouble("val");
                double te_val = te.getDouble("val");

                SetWidget(context, appWidgetId, te_val, il_val, hu_val);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    // ウィジェットのUIの設定
    private static void SetWidget(Context context, int appWidgetId, double te, double il, double hu) {

        RemoteViews views = new RemoteViews(context.getPackageName(),
                R.layout.room_monitor_widget);


        // ボタンの再設定
        Intent btnIntent = new Intent(context, RoomMonitorWidget.class);
        btnIntent.setAction(BUTTON_PUSHED_ACTION);
        btnIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, appWidgetId, btnIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        views.setOnClickPendingIntent(R.id.roomMonitorWidgetBtn, pendingIntent);
        views.setTextColor(R.id.roomMonitorWidgetBtn, Color.BLACK);


        // TextViewの設定
        views.setTextViewText(R.id.textViewTm, "te: " + String.valueOf(te));
        views.setTextViewText(R.id.textViewIl, "il: " + String.valueOf(il));
        views.setTextViewText(R.id.textViewHu, "hu: " + String.valueOf(hu));


        // ウェジェットの更新
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        manager.updateAppWidget(appWidgetId, views);

    }

    // 別スレッドで実行する必要あり
    private String getState(Context context) {

        String TOKEN;

        FileRW fileRW = new FileRW(context);
        TOKEN = fileRW.readFile(TOKEN_FILE_NAME);

        // 以下エアコンの情報を取得
        // URLへのコネクションを取得
        HttpURLConnection urlConnection = null;
        String response = "";

        try {
            URL url = new URL("https://api.nature.global/1/devices");

            // URLへのコネクションを取得する。
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(100000);
            urlConnection.setReadTimeout(100000);
            urlConnection.setRequestProperty("Authorization", "Bearer " + TOKEN);
            urlConnection.setRequestProperty("accept", "application/json");
            urlConnection.addRequestProperty("Content-Type", "Raw; charset=UTF-8");
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoOutput(false);
            urlConnection.setDoInput(true);
            urlConnection.setInstanceFollowRedirects(true);

            urlConnection.connect();
            Log.d(TAG, "connection start");

            final int responseCode = urlConnection.getResponseCode();
            Log.d(TAG, "ResponseCode: " + String.valueOf(responseCode) + "  " + String.valueOf(responseCode / 100));

            // レスポンスのコードが200(正常)でなければTOKENを取得させるように促すactivityを開く
            if (responseCode != 200) {
                response = "failed";
                // toastで結果を表示
                displayToast(context, "failed");
            } else {
                response = convertToString(urlConnection.getInputStream());
                // toastで結果を表示
                displayToast(context, "success");
            }

        } catch (IOException e) {
            Log.d(TAG, "IOException");
            e.printStackTrace();
        } finally {
            // コネクションを閉じる。
            if (urlConnection != null) {
                urlConnection.disconnect();
                Log.d(TAG, "connection closed");
            }
        }

        return response;
    }

}

