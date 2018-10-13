package com.example.mizuho.natureremowidget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class BaseWidget extends AppWidgetProvider {
    final String TAG = "TEST_BWIDGET";
    final String FILE_NAME = "token.txt";

    public class SentHttpRequest implements Runnable {
        URL url;
        String TOKEN;
        Context context;
        FileRW fileRW;

        SentHttpRequest(URL url, Context context){
            this.url = url;
            this.context = context;
            fileRW = new FileRW(context);
            TOKEN = fileRW.readFile(FILE_NAME);
        }

        @Override
        public void run() {
            // URLへのコネクションを取得する。
            HttpURLConnection urlConnection = null;

            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                Log.d(TAG, "connection get");

                urlConnection.setConnectTimeout(100000);
                urlConnection.setReadTimeout(100000);
                urlConnection.setRequestProperty("Authorization", "Bearer " + TOKEN);
                urlConnection.addRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                urlConnection.setInstanceFollowRedirects(true);

                urlConnection.connect();
                Log.d(TAG, "connection start");

                int responseCode = urlConnection.getResponseCode();
                Log.d(TAG, "HttpStatusCode:" + responseCode);
                //ret = convertToString(urlConnection.getInputStream());
                //Log.d(TEST, "ResponseData:" + ret);

                if (responseCode != 200) {
                    // toastで結果を表示
                    displayToast(context, "failed" + String.valueOf(responseCode));
                } else {
                    // toastで結果を表示
                    displayToast(context, "success");
                }


            } catch (IOException e) {
                Log.d(TAG, "SentHttpRequest: IOException");
                e.printStackTrace();
            } finally {
                // 7.コネクションを閉じる。
                if (urlConnection != null) {
                    urlConnection.disconnect();
                    Log.d(TAG, "SentHttpRequest: connection closed");
                }
            }
        }
    }

    public static String convertToString(InputStream stream) throws IOException {
        StringBuffer sb = new StringBuffer();
        String line = "";
        BufferedReader br = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        try {
            stream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    // ウィジェットIDを取得する関数
    public static int getWidgetId(Intent intent) {
        Bundle extras = intent.getExtras();
        int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
        if (extras != null) {
            appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }
        return appWidgetId;
    }

    // toastでメッセージを表示する関数
    public void displayToast(final Context context, final String message) {
        final Handler h = new Handler(Looper.getMainLooper());
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("MainActivity", "thread run() called. thread name:" + Thread.currentThread().getName());
                h.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                    }
                });
            }
        }, "ToastThread").start();
    }

}
