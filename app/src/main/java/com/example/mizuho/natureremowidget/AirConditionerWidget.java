package com.example.mizuho.natureremowidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


/**
 * Implementation of App Widget functionality.
 */
public class AirConditionerWidget extends BaseWidget {

    final static String TAG = "AIRCON_WIDGET";
    final static String TOKEN_FILE_NAME = "token.txt";

    private final static String plusBtnAction   = "android.appwidget.action.PLUS_BUTTON_ACTION";
    private final static String minusBtnAction  = "android.appwidget.action.MINUS_BUTTON_ACTION";
    private final static String airVolumeAction = "android.appwidget.action.AIR_VOLUME_BUTTON_ACTION";
    private final static String onOffAction     = "android.appwidget.action.ON_OFF_BUTTON_ACTION";
    private final static String modeAction      = "android.appwidget.action.MODE_BUTTON_ACTION";

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        Log.d(TAG, "AirConditionerWidget updateAppWidget. appWidgetId: " + String.valueOf(appWidgetId));

        Init init = new Init(context, appWidgetId);
        Thread thread = new Thread(init);
        thread.start();
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

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // Enter relevant functionality for when the last widget is disabled

        // 設定ファイルを消す
        String fileName = "ac" + String.valueOf(appWidgetIds[0])+ ".txt";
        FileRW fileRW = new FileRW(context);
        fileRW.clearFile(fileName);
    }


    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        Log.d("TEST_WIDGET", "onReceive");

        int appWidgetId = getWidgetId(intent);

        // どのボタンが押されたのか
        String action = intent.getAction();
        Log.d(TAG, "ACTION: " + action);

        if(!action.equals("android.appwidget.action.APPWIDGET_UPDATE")) {
            SentRequest sentRequest = new SentRequest(context, appWidgetId, action);
            Thread thread = new Thread(sentRequest);
            thread.start();
        }
    }


    // ウィジェットのUIの設定
    public static void SetWidget(Context context, int appWidgetId, SettingsInfo settingsInfo) {

        Log.d(TAG, "SetWidget");

        String mode  = settingsInfo.mode;
        String temp  = settingsInfo.temp;
        String dir   = settingsInfo.dir;
        String vol   = settingsInfo.vol;
        String power = settingsInfo.power;

        RemoteViews views = new RemoteViews(context.getPackageName(),
                R.layout.air_conditioner_widget);


        // airVolumeボタンの設定
        Intent aieVolumeBtnIntent = new Intent(context, AirConditionerWidget.class);
        aieVolumeBtnIntent.setAction(airVolumeAction);
        aieVolumeBtnIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent airVolumeBtnPendingIntent = PendingIntent.getBroadcast(context, appWidgetId, aieVolumeBtnIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        views.setOnClickPendingIntent(R.id.airVolumeBtn, airVolumeBtnPendingIntent);
        views.setTextViewText(R.id.airVolumeTextView, vol);
        views.setTextColor(R.id.airVolumeTextView, Color.BLACK);


        // +,-ボタンの設定
        Intent plusBtnIntent = new Intent(context, AirConditionerWidget.class);
        plusBtnIntent.setAction(plusBtnAction);
        plusBtnIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent plusBtnPendingIntent = PendingIntent.getBroadcast(context, appWidgetId, plusBtnIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        views.setOnClickPendingIntent(R.id.plusBtn, plusBtnPendingIntent);
        views.setImageViewResource(R.id.plusBtn, R.drawable.button_icon_pm_puls);

        Intent minusBtnIntent = new Intent(context, AirConditionerWidget.class);
        minusBtnIntent.setAction(minusBtnAction);
        minusBtnIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent minusBtnPendingIntent = PendingIntent.getBroadcast(context, appWidgetId, minusBtnIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        views.setOnClickPendingIntent(R.id.minusBtn, minusBtnPendingIntent);
        views.setImageViewResource(R.id.minusBtn, R.drawable.button_icon_pm_minus);


        // 温度表示のTextViewの設定
        views.setTextViewText(R.id.textView, temp);
        views.setTextColor(R.id.textView, Color.BLACK);

        // モード表示のTextViewの設定
        views.setTextViewText(R.id.modeTextView, mode);
        views.setTextColor(R.id.modeTextView, Color.WHITE);


        // on/offボタンの設定
        Intent onOffBtnIntent = new Intent(context, AirConditionerWidget.class);
        onOffBtnIntent.setAction(onOffAction);
        onOffBtnIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent onOffBtnPendingIntent = PendingIntent.getBroadcast(context, appWidgetId, onOffBtnIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        views.setOnClickPendingIntent(R.id.onOffBtn, onOffBtnPendingIntent);
        if (power.equals("")) {
            views.setTextViewText(R.id.onOffBtn, "ON");
        } else {
            views.setTextViewText(R.id.onOffBtn, "OFF");
        }
        views.setTextColor(R.id.onOffBtn, Color.BLACK);


        // モード変更ボタン（アイコンが表示されているimageButton）の設定
        Intent modeBtnIntent = new Intent(context, AirConditionerWidget.class);
        modeBtnIntent.setAction(modeAction);
        modeBtnIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent modeBtnPendingIntent = PendingIntent.getBroadcast(context, appWidgetId, modeBtnIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        views.setOnClickPendingIntent(R.id.iconBtn, modeBtnPendingIntent);
        views.setImageViewResource(R.id.iconBtn, R.drawable.device_icon_airconditioner);


        views.setViewVisibility(R.id.widget_background_cool, View.INVISIBLE);
        views.setViewVisibility(R.id.widget_background_warm, View.INVISIBLE);
        views.setViewVisibility(R.id.widget_background_auto, View.INVISIBLE);
        views.setViewVisibility(R.id.widget_background_blow, View.INVISIBLE);
        views.setViewVisibility(R.id.widget_background_dry, View.INVISIBLE);

        // 背景色の変更
        if (power.equals("")) {
            switch (mode) {
                case "cool":
                    views.setViewVisibility(R.id.widget_background_cool, View.VISIBLE);
                    break;
                case "warm":
                    views.setViewVisibility(R.id.widget_background_warm, View.VISIBLE);
                    break;
                case "auto":
                    views.setViewVisibility(R.id.widget_background_auto, View.VISIBLE);
                    break;
                case "blow":
                    views.setViewVisibility(R.id.widget_background_blow, View.VISIBLE);
                    break;
                case "dry":
                    views.setViewVisibility(R.id.widget_background_dry, View.VISIBLE);
                    break;
            }
        }

        // ウェジェットの更新
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        manager.updateAppWidget(appWidgetId, views);

    }

    public class SentAcHttpRequest implements Runnable {
        URL url;
        String TOKEN;
        Context context;
        String mode;
        String temp;
        String dir;
        String vol;
        String power;
        String data;  // 送信するデータ

        SentAcHttpRequest(URL url, String mode, String temp, String dir, String vol, String power, Context context){
            this.url = url;
            this.context = context;

            FileRW fileRW = new FileRW(context);
            TOKEN = fileRW.readFile(TOKEN_FILE_NAME);

            this.mode  = mode;
            this.dir   = dir;
            this.temp  = temp;
            this.vol   = vol;
            this.power = power;

            if (power.equals("power-off")) {
                data = "button=power-off";
            } else {
                data = "operation_mode="    + mode
                        + "&temperature="   + temp
                        + "&air_volume="    + vol
                        + "&air_direction=" + dir;
            }
        }

        @Override
        public void run() {
            HttpURLConnection urlConnection = null;

            try {
                // URLへのコネクションを取得する。
                urlConnection = (HttpURLConnection) url.openConnection();
                Log.d(TAG, "connection get");

                urlConnection.setConnectTimeout(100000);
                urlConnection.setReadTimeout(100000);
                urlConnection.setRequestProperty("Authorization", "Bearer " + TOKEN);
                urlConnection.setRequestProperty("accept", "application/json");
                urlConnection.addRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                urlConnection.setInstanceFollowRedirects(true);

                // 送信するデータの設定
                // データを送信するためにはbyte配列に変換する必要がある
                byte[] sendData = data.getBytes("UTF-8");
                urlConnection.getOutputStream().write(sendData);
                urlConnection.getOutputStream().flush();
                urlConnection.getOutputStream().close();

                urlConnection.connect();
                Log.d(TAG, "connection start");
                Log.d(TAG, url.toString());
                Log.d(TAG, data);

                int responseCode = urlConnection.getResponseCode();
                Log.d(TAG, "HttpStatusCode:" + responseCode);
                //ret = convertToString(urlConnection.getInputStream());
                //Log.d(TAG, "ResponseData:" + ret);

                if (responseCode != 200) {
                    // toastで結果を表示
                    displayToast(context, "failed (AirConditionerWidget)");
                } else {
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
        }
    }

    public class SentRequest implements Runnable {
        Context context;
        int appWidgetId;
        String id;
        String action;

        SentRequest(Context context, int appWidgetId, String action) {
            this.context = context;
            this.appWidgetId = appWidgetId;
            this.action = action;

            // 設定ファイルからidを取り出す
            String saveFileName = "ac" + String.valueOf(appWidgetId)+ ".txt";
            FileRW fileRW = new FileRW(context);
            String data = fileRW.readFile(saveFileName);
            JSONObject dataJson;

            try {
                dataJson = new JSONObject(data);
                Log.d(TAG, dataJson.toString());

                this.id = dataJson.getString("id");
            } catch (JSONException e) {
                Log.d(TAG, "JSONException");
                e.printStackTrace();
            }

        }

        @Override
        public void run() {
            String response;

            String newMode  = "";
            String newTemp  = "";
            String newDir   = "";
            String newVol   = "";
            String newPower = "";

            JSONObject modesJson = new JSONObject();

            if (action.equals("android.appwidget.action.APPWIDGET_UPDATE_OPTIONS")) {
                return;
            }

            try {
                // エアコンの情報を取得
                response = GetACData(context);
                //Log.d(TAG, response);

                if (response.equals("failed")) {
                    Log.d(TAG, "response is failed");
                    return;
                }

                // 必要なエアコンの情報を取り出す
                JSONArray jsonArray = new JSONArray(response);
                JSONObject acInfoJsonArray;
                JSONObject settingsJson = new JSONObject();

                Log.d(TAG, "id: " + id);

                for (int i = 0; i < jsonArray.length(); i++) {

                    if (jsonArray.getJSONObject(i).getString("id").equals(id) ) {
                        acInfoJsonArray = jsonArray.getJSONObject(i);
                        settingsJson    = acInfoJsonArray.getJSONObject("settings");
                        modesJson       = acInfoJsonArray.getJSONObject("aircon").getJSONObject("range").getJSONObject("modes");
                        break;
                    }
                }

                // 現在の設定情報を取り出す
                newMode  = settingsJson.getString("mode");
                newTemp  = settingsJson.getString("temp");
                newDir   = settingsJson.getString("dir");
                newVol   = settingsJson.getString("vol");
                newPower = settingsJson.getString("button");

            } catch (JSONException e) {
                Log.d(TAG, "JSONException");
                e.printStackTrace();
            }


            // ボタンが押された時の処理
            try {

                if (airVolumeAction.equals(action)) {
                    Log.d(TAG, "ACTION: " + action + " !!!");

                    // 設定できる風量を取得
                    JSONArray volJsonArray = modesJson.getJSONObject(newMode).getJSONArray("vol");

                    Log.d(TAG, volJsonArray.toString(2));

                    if (newVol.equals("")) {
                        newVol = volJsonArray.getString(0);
                    } else {
                        for (int i = 0; i < volJsonArray.length(); i++) {
                            if (volJsonArray.getString(i).equals(newVol)) {
                                newVol = volJsonArray.getString((i + 1) % volJsonArray.length());
                                break;
                            }
                        }
                    }
                    newPower = "";

                } else if (minusBtnAction.equals(action)) {
                    Log.d(TAG, "ACTION: " + action + " !!!");

                    // 設定できる温度を取得
                    JSONArray tempJsonArray = modesJson.getJSONObject(newMode).getJSONArray("temp");

                    if (newTemp.equals("")) {
                        newTemp = tempJsonArray.getString(0);
                    } else {
                        for (int i = 0; i < tempJsonArray.length(); i++) {
                            if (tempJsonArray.getString(i).equals(newTemp) && (i - 1) >= 0) {
                                newTemp = tempJsonArray.getString(i - 1);
                                break;
                            }
                        }
                    }
                    newPower = "";

                } else if (plusBtnAction.equals(action)) {
                    Log.d(TAG, "ACTION: " + action + " !!!");

                    // 設定できる温度を取得
                    JSONArray tempJsonArray = modesJson.getJSONObject(newMode).getJSONArray("temp");

                    if (newTemp.equals("")) {
                        newTemp = tempJsonArray.getString(0);
                    } else {
                        for (int i = 0; i < tempJsonArray.length(); i++) {
                            if (tempJsonArray.getString(i).equals(newTemp) && (i + 1) < tempJsonArray.length()) {
                                newTemp = tempJsonArray.getString(i + 1);
                                break;
                            }
                        }
                    }
                    newPower = "";

                } else if (onOffAction.equals(action)) {
                    Log.d(TAG, "ACTION: " + action + " !!!");

                    // 電源のON/OFF
                    if (newPower.equals("")) {
                        newPower = "power-off";
                    } else {
                        newPower = "";
                    }

                } else if (modeAction.equals(action)) {
                    Log.d(TAG, "ACTION: " + action + " !!!");

                    // エアコンのモードを変更
                    String modes[] = {"auto", "cool", "warm", "dry", "blow"};
                    List<String> hasMode = new ArrayList<>();

                    // 設定できるモードをリスト化
                    for (String str : modes) {
                        if (modesJson.has(str)) {
                            hasMode.add(str);
                        }
                    }

                    if (newMode.equals("")) {
                        newMode = "";
                    } else {
                        for (int i = 0; i < hasMode.size(); i++) {
                            if (hasMode.get(i).equals(newMode)) {
                                newMode = hasMode.get( (i + 1) % hasMode.size() );
                                break;
                            }
                        }
                    }

                    // 温度、風量、風向の設定
                    JSONArray tempJsonArray = modesJson.getJSONObject(newMode).getJSONArray("temp");
                    newTemp = tempJsonArray.getString(tempJsonArray.length() / 2);

                    newDir = "";
                    newVol = "auto";
                    newPower = "";
                } else {
                    return;
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }



            // 送信するURLの設定
            URL url = null;
            try {
                url = new URL("https://api.nature.global/1/appliances/" + id + "/aircon_settings");
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }


            // nature remo で信号の送信
            if (url != null) {
                SentAcHttpRequest runnable = new SentAcHttpRequest(url, newMode, newTemp, newDir, newVol, newPower, context);
                Thread thread = new Thread(runnable);
                thread.start();
            } else {
                Log.d(TAG,  "id is \"non\". not sent request");
            }

            // uiの設定
            SettingsInfo settingsInfo = new SettingsInfo(newMode, newTemp, newDir, newVol, newPower);
            SetWidget(context, appWidgetId, settingsInfo);
        }
    }

    static public class Init implements Runnable {
        Context context;
        int appWidgetId;

        Init(Context context, int appWidgetId) {
            this.context = context;
            this.appWidgetId = appWidgetId;
        }

        @Override
        public void run() {
            String response;
            SettingsInfo settingsInfo = new SettingsInfo();

            try {
                // エアコンの情報を取得
                response = GetACData(context);
                //Log.d(TAG, response);

                if (response.equals("failed")) {
                    return;
                }

                // 必要なエアコンの情報を取り出す
                JSONArray jsonArray = new JSONArray(response);
                JSONObject acInfoJsonArray;
                JSONObject settingsJson = new JSONObject();

                // 設定ファイルからidを取り出す
                String saveFileName = "ac" + String.valueOf(appWidgetId)+ ".txt";
                FileRW fileRW = new FileRW(context);
                String data = fileRW.readFile(saveFileName);
                JSONObject dataJson = new JSONObject(data);
                String id = dataJson.getString("id");

                for (int i = 0; i < jsonArray.length(); i++) {
                    if (jsonArray.getJSONObject(i).getString("id").equals(id)) {
                        acInfoJsonArray = jsonArray.getJSONObject(i);
                        settingsJson = acInfoJsonArray.getJSONObject("settings");
                        break;
                    }
                }

                // 現在の設定情報を取り出す
                settingsInfo.mode  = settingsJson.getString("mode");
                settingsInfo.temp  = settingsJson.getString("temp");
                settingsInfo.dir   = settingsJson.getString("dir");
                settingsInfo.vol   = settingsJson.getString("vol");
                settingsInfo.power = settingsJson.getString("button");

            } catch (JSONException e) {
                Log.d(TAG, "JSONException");
                e.printStackTrace();
            }

            // ウィジェットの更新
            SetWidget(context, appWidgetId, settingsInfo);
        }
    }


    // エアコンの情報の取得
    // 別スレッドで実行する必要あり
    static private String GetACData(Context context) {

        String TOKEN;

        FileRW fileRW = new FileRW(context);
        TOKEN = fileRW.readFile(TOKEN_FILE_NAME);

        // 以下エアコンの情報を取得
        // URLへのコネクションを取得
        HttpURLConnection urlConnection = null;
        String response = "";

        try {
            URL url = new URL("https://api.nature.global/1/appliances");

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

            // レスポンスのコードが200(正常)でなければTOKENを取得させるように促すactivityを開く
            if (responseCode != 200) {
                response = "failed";
            } else {
                response = convertToString(urlConnection.getInputStream());
                //Log.d(TAG, response);
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

