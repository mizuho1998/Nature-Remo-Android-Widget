package com.example.mizuho.natureremowidget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class AirConditionerWidgetSetting extends Activity {

    private final String TAG = "AC_WIDGET_SETTING";
    private final String TOKEN_FILE_NAME = "token.txt";
    private static final int REQUEST_CODE = 100; // リクエストコード（呼び出しActivity識別用）

    int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    int select = 0;
    JSONArray acInfoJsonArray;
    SettingsInfo settingsInfo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_air_conditioner_widget_setting);

        Log.d(TAG, "onCreate");

        // appWidgetIdを取得
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            appWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }
        Log.d(TAG, "StandardWidgetSetting sppWidgetId " + String.valueOf(appWidgetId));

        settingsInfo = new SettingsInfo();

        // spinnerの設定
        InitRunnable initRunnable = new InitRunnable(getApplicationContext());
        Thread thread = new Thread(initRunnable);
        thread.start();

        Spinner spinner = findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            //　アイテムが選択された時
            @Override
            public void onItemSelected(AdapterView<?> parent,
                                       View view, int position, long id) {
                select = position;
            }

            //　アイテムが選択されなかった
            public void onNothingSelected(AdapterView<?> parent) {
                //
            }
        });


        // 設定事項の確定ボタン
        // クリックするとウィジェットを作成する
        final Button btn = findViewById(R.id.button);
        btn.setText("OK");
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // 設定項目の保存
                // 設定項目をjsonにする
                JSONObject json=null;
                try {
                    json = acInfoJsonArray.getJSONObject(select);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // 設定ファイルの保存
                if (json !=null) {
                    String saveFileName = "ac" + String.valueOf(appWidgetId)+ ".txt";
                    FileRW fileRW = new FileRW(getApplicationContext());
                    fileRW.clearFile(saveFileName);
                    fileRW.writeFileMessage(saveFileName, json.toString());

                    Log.d(TAG, json.toString());
                }

                // ウィジェットのUIの設定
                AirConditionerWidget.SetWidget(getApplicationContext(), appWidgetId, settingsInfo);

                // 結果を返す
                Intent resultValue = new Intent();
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                setResult(RESULT_OK, resultValue);
                finish();

            }
        });


        final Button setTokenBtn = findViewById(R.id.setTokenBtn);
        setTokenBtn.setText("change token");
        setTokenBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AirConditionerWidgetSetting.this, SetTokenActivity.class);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });

    }


    // 中でGetACData()とsetSpinnerRunnableを呼ぶ
    public class InitRunnable implements Runnable {
        String TOKEN;
        Context context;
        Handler handler;

        InitRunnable(Context context) {
            this.context = context;
            FileRW fileRW = new FileRW(context);
            TOKEN = fileRW.readFile(TOKEN_FILE_NAME);
            handler = new Handler();
        }

        @Override
        public void run() {

            String acData = GetACData(context);

            if(acData.equals("failed")) {
                return;
            }

            // 必要なデータだけ取り出す
            try {
                // responseから必要な情報を取得して保存
                // エアコンの情報を取り出す
                JSONArray jsonArray = new JSONArray(acData);
                acInfoJsonArray = new JSONArray();
                //Log.d(TAG, "jsonArray length: " + String.valueOf(jsonArray.length()));

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject applianceDataJsonObject = jsonArray.getJSONObject(i);

                    if (applianceDataJsonObject.getString("type").equals("AC")) {
                        JSONObject newJson = new JSONObject();

                        String id = applianceDataJsonObject.getString("id");
                        newJson.put("id", id);

                        String nickname = applianceDataJsonObject.getString("nickname");
                        newJson.put("nickname", nickname);

                        JSONObject aircon = applianceDataJsonObject.getJSONObject("aircon");
                        JSONObject range  = aircon.getJSONObject("range");
                        JSONObject modes  = range.getJSONObject("modes");

                        // エアコンの初期設定
                        if (modes.has("auto")) {
                            JSONArray temp = modes.getJSONObject("auto").getJSONArray("temp");
                            settingsInfo.mode = "auto";
                            settingsInfo.temp = temp.getString(temp.length()/2);
                        } else if (modes.has("cool")) {
                            JSONArray temp = modes.getJSONObject("cool").getJSONArray("temp");
                            settingsInfo.mode = "cool";
                            settingsInfo.temp = temp.getString(temp.length()/2);
                        } else if (modes.has("warm")) {
                            JSONArray temp = modes.getJSONObject("warm").getJSONArray("temp");
                            settingsInfo.mode = "warm";
                            settingsInfo.temp = temp.getString(temp.length()/2);
                        } else if (modes.has("blow")) {
                            JSONArray temp = modes.getJSONObject("blow").getJSONArray("temp");
                            settingsInfo.mode = "blow";
                            settingsInfo.temp = temp.getString(temp.length()/2);
                        } else if (modes.has("dry")) {
                            JSONArray temp = modes.getJSONObject("dry").getJSONArray("temp");
                            settingsInfo.mode = "dry";
                            settingsInfo.temp = temp.getString(temp.length()/2);
                        } else {
                            settingsInfo.mode = "";
                            settingsInfo.temp = "";
                        }

                        settingsInfo.dir   = "";
                        settingsInfo.vol   = "";
                        settingsInfo.power = "power-off";


                        acInfoJsonArray.put(newJson);
                    }
                }

                //Log.d(TAG, acInfoJsonArray.toString(2));

                SetSpinnerRunnable setSpinnerRunnable = new SetSpinnerRunnable(acInfoJsonArray);
                handler.post(setSpinnerRunnable);

            } catch (JSONException e) {
                Log.d(TAG, "JSONException");
                e.printStackTrace();
            }
        }
    }

    // エアコンの情報の取得
    // 別スレッドで実行する必要あり
    private String GetACData(Context context) {

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
            Log.d(TAG, "ResponseCode: " + String.valueOf(responseCode) + "  " + String.valueOf(responseCode / 100));

            // レスポンスのコードが200(正常)でなければ(TOKENが間違っている可能性があるので)TOKENを取得させるように促す
            if (responseCode != 200) {
                Toast.makeText(context, "TOKE may be incorrect. Reset the TOKEN", Toast.LENGTH_LONG).show();
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


    // spinnerの設定を行うためのRunnable
    public class SetSpinnerRunnable implements Runnable {
        Spinner spinner;
        JSONArray jsonArray;

        SetSpinnerRunnable(JSONArray jsonArray) {
            spinner = findViewById(R.id.spinner);
            this.jsonArray = jsonArray;
        }

        @Override
        public void run() {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(AirConditionerWidgetSetting.this, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            for (int i = 0; i < jsonArray.length(); i++) {
                try {
                    String nickname = jsonArray.getJSONObject(i).getString("nickname");
                    adapter.add(nickname);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            // アダプターを設定
            spinner.setAdapter(adapter);
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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        // 注意：superメソッドは呼ぶようにする
        // Activity側のonActivityResultで呼ばないとFragmentのonActivityResultが呼ばれない
        super.onActivityResult(requestCode,resultCode,data);

        switch(requestCode){
            case(REQUEST_CODE):
                // 呼び出し先のActivityから結果を受け取る
                // spinnerの初期化
                // nature remoのサーバからリモコンの情報を取得してそれらを洗濯肢に追加する
                // spinnerの設定
                InitRunnable initRunnable = new InitRunnable(getApplicationContext());
                Thread thread = new Thread(initRunnable);
                thread.start();
                break;
            default:
                break;
        }
    }

}
