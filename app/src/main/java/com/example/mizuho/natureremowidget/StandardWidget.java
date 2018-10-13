package com.example.mizuho.natureremowidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.util.Log;
import android.widget.RemoteViews;
import org.json.JSONException;
import org.json.JSONObject;
import java.net.MalformedURLException;
import java.net.URL;


public class StandardWidget extends BaseWidget {

    static final String TAG = "STANDARD";
    private static final String BUTTON_PUSHED_ACTION[] =
            {"android.appwidget.action.BUTTON1_PUSHED", "android.appwidget.action.BUTTON2_PUSHED",
            "android.appwidget.action.BUTTON3_PUSHED", "android.appwidget.action.BUTTON4_PUSHED"};


    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        Log.d(TAG, "updateAppWidget");
        SetWidget(context, appWidgetId);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            Log.d(TAG, "onUpdate appid: " + String.valueOf(appWidgetId));
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

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        Log.d(TAG, "onDelete " + String.valueOf(appWidgetIds[0]));

        // 削除されたウィジェットの設定を削除
        FileRW fileRW = new FileRW(context);
        fileRW.clearFile("setting" + appWidgetIds[0] + ".txt");
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        Log.d("TEST_WIDGET", "onReceive");

        // ボタンが押された場合のみ処理を実行
        String action = intent.getAction();
        Log.d(TAG,  "ACTION: " + action);


        // ボタンが押された時の処理
        for (int i = 0; i < 4; i++) {
            if (BUTTON_PUSHED_ACTION[i].equals(action)) {

                int appWidgetId = getWidgetId(intent);
                String signalId=""; // ボタンのsignalのid

                // ボタンの設定データの読み込み
                FileRW fileRW = new FileRW(context);
                String data = fileRW.readFile("setting" + appWidgetId + ".txt");

                try {
                    JSONObject dataJson = new JSONObject(data);
                    JSONObject btnData = dataJson.getJSONObject("btn"+i);
                    signalId = btnData.getString("id");

                } catch (JSONException e) {
                    Log.d(TAG,  "JSONException at onReceive");
                    e.printStackTrace();
                }

                // nature remo で信号の送信
                try {
                    if ( !(signalId.equals("non")) ) {
                        URL url = new URL("https://api.nature.global/1/signals/" + signalId + "/send");
                        SentHttpRequest runnable = new SentHttpRequest(url, context);
                        Thread thread = new Thread(runnable);
                        thread.start();
                    } else {
                        Log.d(TAG,  "id is \"non\". not sent request");
                    }

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }

                // ボタンの再設定
                SetWidget(context, appWidgetId);

            }
        }

    }

    private static void SetWidget(Context context, int appWidgetId) {

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.standard_widget);

        // ボタンの設定データの読み込み
        FileRW fileRW = new FileRW(context);
        String data = fileRW.readFile("setting" + appWidgetId + ".txt");
        JSONObject dataJson = null;

        if (data == null || data.length() == 0) {
            return;
        }

        // アイコンの再設定
        String icon="";
        String iconName="";

        try {
            dataJson = new JSONObject(data);
            JSONObject iconJson = dataJson.getJSONObject("icon");
            icon = iconJson.getString("icon");
            iconName = iconJson.getString("name");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Resources res = context.getResources();
        int iconResourceId = res.getIdentifier(icon,
                "drawable", context.getPackageName());
        views.setImageViewResource(R.id.imageView, iconResourceId);
        views.setTextViewText(R.id.appwidget_text, iconName);

        // paddingの設定
        final float paddingDp = 10.0f;
        final float scale = context.getResources().getDisplayMetrics().density;
        int paddingPx = (int) (paddingDp * scale + 0.5f);
        views.setViewPadding(R.id.imageView, paddingPx, paddingPx, paddingPx, paddingPx);


        // ボタンの再設定
        for (int i=0; i<4; i++) {

            String iconData=""; // ボタンの画像のファイル名

            try {
                assert dataJson != null;
                JSONObject btnData = dataJson.getJSONObject("btn"+i);
                iconData = btnData.getString("icon");

            } catch (JSONException e) {
                Log.d(TAG,  "JSONException at onReceive");
                e.printStackTrace();
            }

            // ボタンの再設定
            Intent btnIntent = new Intent(context, StandardWidget.class);
            btnIntent.setAction(BUTTON_PUSHED_ACTION[i]);
            btnIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, appWidgetId, btnIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            // ボタンIDを取得
            int buttonId = res.getIdentifier("button" + (i + 1), "id", context.getPackageName());
            views.setOnClickPendingIntent(buttonId, pendingIntent);

            // ボタンに画像をセット
            int resourceId = res.getIdentifier(iconData, "drawable", context.getPackageName());
            views.setImageViewResource(buttonId, resourceId);

            // ボタンの画像のpaddingの設定
            views.setViewPadding(buttonId, paddingPx, paddingPx, paddingPx, paddingPx);
        }

        // ウェジェットの更新
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        manager.updateAppWidget(appWidgetId, views);

    }
}

