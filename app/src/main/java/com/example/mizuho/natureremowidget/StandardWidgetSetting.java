package com.example.mizuho.natureremowidget;

import android.Manifest;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RemoteViews;
import android.widget.Spinner;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static com.example.mizuho.natureremowidget.BaseWidget.convertToString;


public class StandardWidgetSetting extends AppCompatActivity {
    final private String TAG = "STANDARD_CONF";
    private final int REQUEST_PERMISSION = 1000;
    private static final int REQUEST_CODE = 100; // リクエストコード（呼び出しActivity識別用）
    private final String TOKEN_FILE_NAME = "token.txt";
    private final String BUTTON_PUSHED_ACTION[] =
            {"android.appwidget.action.BUTTON1_PUSHED", "android.appwidget.action.BUTTON2_PUSHED",
            "android.appwidget.action.BUTTON3_PUSHED", "android.appwidget.action.BUTTON4_PUSHED"};

    private String spinnerDeviceIconImages[];
    private String spinnerDeviceIconName[];
    private String spinnerButtonImages[];

    private int appWidgetId   = AppWidgetManager.INVALID_APPWIDGET_ID;
    private String icon;
    private String iconName;
    private String iconData[];
    private String idData[];
    JSONArray allSignalsJsonArray;     // 全てのsignalsのid,nameをjsonで格納するためのもの

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_standard_widget_setting);

        final Spinner buttonIconSpinner[]   = new Spinner[4];
        final Spinner buttonSignalSpinner[] = new Spinner[4];
        iconData = new String[4];
        idData   = new String[4];

        // 画像リソースを取得する
        Field[] fields = R.drawable.class.getFields();
        List<String> deviceIconList = new ArrayList<>(); // デバイスのアイコンの画像のファイル名を格納する
        List<String> deviceNameList = new ArrayList<>(); // デバイスのアイコンの名前のファイル名を格納する
        List<String> buttonIconList = new ArrayList<>(); // ボタンのアイコンの画像のファイル名を格納する


        // 画像リソース読込み
        for (Field field : fields) {
            try {
                // フィールド名を取得する
                String name = field.getName();

                // "device_icon_" で始まるファイルはデバイスのアイコン画像
                // "button_icon_" で始まるファイルはボタンのアイコン画像
                // それをリストに格納
                if (name.startsWith("device_icon_")) {
                    deviceIconList.add(name);
                    deviceNameList.add(name.split("_",0)[2]);
                } else if (name.startsWith("button_icon_")) {
                    buttonIconList.add(name);
                }

            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }

        // リストから配列へ変換
        spinnerDeviceIconImages = deviceIconList.toArray(new String[deviceIconList.size()]);
        spinnerDeviceIconName   = deviceNameList.toArray(new String[deviceNameList.size()]);
        spinnerButtonImages     = buttonIconList.toArray(new String[buttonIconList.size()]);


        // appWidgetId を取得
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            appWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }
        Log.d(TAG, "StandardWidgetSetting sppWidgetId " + String.valueOf(appWidgetId));


        // 機器のアイコンの選択するspinnerの設定
        SpinnerIconAdapter deviceIconAdapter = new SpinnerIconAdapter(this.getApplicationContext(),
                R.layout.spinner_device_icon, R.layout.spinner_device_icon_list, spinnerDeviceIconName, spinnerDeviceIconImages);
        Spinner deviceIconSpinner = findViewById(R.id.spinnerIcon);
        deviceIconSpinner.setAdapter(deviceIconAdapter);
        deviceIconSpinner.setBackgroundResource(R.drawable.w_button_normal_background);
        deviceIconSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            //　アイテムが選択された時
            @Override
            public void onItemSelected(AdapterView<?> parent,
                                       View view, int position, long id) {

                icon = spinnerDeviceIconImages[position];
                iconName = spinnerDeviceIconName[position];
            }

            //　アイテムが選択されなかった
            public void onNothingSelected(AdapterView<?> parent) {
                //
            }
        });


        // ボタンのアイコンのspinnerのadapter
        ImageSpinnerAdapter buttonIconAdapter = new ImageSpinnerAdapter(this.getApplicationContext(),
                R.layout.spinner_icon_list, spinnerButtonImages);

        for (int i = 0; i < 4; i++) {
            // ボタンのアイコンを設定するspinnerの設定
            // spinnerのIDを文字列から取得する
            int buttonIconSpinnerId = getResources().getIdentifier("spinner1" + (i+1), "id", getPackageName());
            buttonIconSpinner[i] = findViewById(buttonIconSpinnerId);
            buttonIconSpinner[i].setAdapter(buttonIconAdapter);
            buttonIconSpinner[i].setBackgroundResource(R.drawable.w_button_normal_background);
            buttonIconSpinner[i].setPopupBackgroundResource(R.drawable.w_button_normal_background);
            final int j = i;
            buttonIconSpinner[i].setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                //　アイテムが選択された時
                @Override
                public void onItemSelected(AdapterView<?> parent,
                                           View view, int position, long id) {

                    iconData[j] = spinnerButtonImages[position];
                }

                //　アイテムが選択されなかった
                public void onNothingSelected(AdapterView<?> parent) {
                    //
                }
            });


            // ボタンを押した際に送信する信号の選択をするspinnerの設定
            // spinnerのIDを文字列から取得する
            int buttonSignalSpinnerId = getResources().getIdentifier("spinner2" + (i+1), "id", getPackageName());
            buttonSignalSpinner[i] = findViewById(buttonSignalSpinnerId);
            buttonSignalSpinner[i].setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                //　アイテムが選択された時
                @Override
                public void onItemSelected(AdapterView<?> parent,
                                           View view, int position, long id) {

                    String signalId = "";

                    try {
                        signalId = allSignalsJsonArray.getJSONObject(position).getString("id");
                        idData[j] = signalId;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    // buttonSignalSpinnerで”指定しない”を選択した場合
                    if (signalId.equals("non")) {

                        // 対応するbuttonIconSpinnerのアイコンを適当なものにし、設定不可にする
                        //int buttonIconSpinnerId = getResources().getIdentifier("spinner1" + (j+1), "id", getPackageName());
                        //spinner =  findViewById(buttonIconSpinnerId);
                        //spinner.setEnabled(false);
                        buttonIconSpinner[j].setSelection(0);
                        buttonIconSpinner[j].setEnabled(false);
                        buttonIconSpinner[j].setAlpha(0.5f);
                        iconData[j] = "button_icon__non";
                    } else {
                        // spinnerの選択ができるようにする
                        buttonIconSpinner[j].setEnabled(true);
                        buttonIconSpinner[j].setAlpha(1.0f);
                    }

                }

                //　アイテムが選択されなかった
                public void onNothingSelected(AdapterView<?> parent) {
                    //
                }
            });
        }


        // 設定事項の確定ボタン
        // クリックするとウィジェットを作成する
        final Button btn = findViewById(R.id.button);
        btn.setText("OK");
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // 設定項目の保存
                // 設定項目をjsonにする
                JSONObject settingData = new JSONObject();
                try {
                    JSONObject iconDataJson = new JSONObject();
                    iconDataJson.put("icon", icon);
                    iconDataJson.put("name", iconName);
                    settingData.put("icon", iconDataJson);


                    for (int i = 0; i < 4; i++) {
                        JSONObject signalData = new JSONObject();
                        signalData.put("icon", iconData[i]);
                        signalData.put("id", idData[i]);
                        settingData.put("btn"+i, signalData);
                    }

                    // 設定ファイルの保存
                    FileRW fileRW = new FileRW(getApplicationContext());
                    fileRW.clearFile("setting" + appWidgetId + ".txt");
                    fileRW.writeFileMessage("setting" + appWidgetId + ".txt", settingData.toString());

                    Log.d(TAG, "save file " + "setting" + appWidgetId + ".txt");

                } catch (JSONException e) {
                    Log.d(TAG, "JSONException");
                    e.printStackTrace();
                }


                // ウィジェットのUIの設定
                SetWidget(getApplicationContext(), appWidgetId);

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
                Intent intent = new Intent(StandardWidgetSetting.this, SetTokenActivity.class);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });


        InitSpinnerRunnable initSpinnerRunnable = new InitSpinnerRunnable(buttonSignalSpinner);
        Thread thread = new Thread(initSpinnerRunnable);
        thread.start();

        checkPermission();
    }


    // buttonSignalSpinnerの項目の設定
    // 内部でGetApplianceとSetSpinnerRunnableを呼ぶ
    public class InitSpinnerRunnable implements Runnable{
        Spinner spinners[];

        InitSpinnerRunnable (Spinner[] spinners) {
            this.spinners = spinners;
        }

        Handler handler = new Handler();

        @Override
        public void run() {
            Log.d(TAG, "InitSpinnerRunnable start");

            String response = "";
            response = GetAppliances(getApplicationContext());

            if (response.equals("failed")) {
                // TOKENを設定させるアクティビティを起動
                return;
            }
            //Log.d(TAG, "InitSpinnerRunnable appliance: " + response);

            allSignalsJsonArray = new JSONArray();     // 全てのsignalsのid,nameをjsonで格納するためのもの

            try {
                // applianceの文字列をJSONオブジェクトに変換
                JSONArray jsonArray = new JSONArray(response);

                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject jsonObject     = jsonArray.getJSONObject(i);
                    JSONArray jsonSignalArray = jsonObject.getJSONArray("signals");

                    String nickname = jsonObject.getString("nickname");
                    Log.d(TAG, nickname);

                    for (int j = 0; j < jsonSignalArray.length(); j++) {
                        JSONObject    jsonSignalObject = jsonSignalArray.getJSONObject(j);
                        String id   = jsonSignalObject.getString("id");
                        String name = jsonSignalObject.getString("name");

                        // 取り出したjsonSignalObjectの中のid,nameだけをjsonAllSignalsArrayに保存
                        JSONObject json = new JSONObject();
                        json.put("id", id);
                        json.put("name", nickname + " - " + name);

                        allSignalsJsonArray.put(json);
                    }
                }


                // ボタンに特定の信号を設定しない場合の選択肢の追加
                JSONObject json = new JSONObject();
                json.put("id", "non");
                json.put("name", "設定しない");
                allSignalsJsonArray.put(json);


            } catch (JSONException e) {
                Log.d(TAG, "InitSpinnerRunnable JSONException");
                e.printStackTrace();
            }

            Log.d(TAG, "InitSpinnerRunnable set signalJsonAllay");

            // spinnerにallSignalsJsonArrayをセット
            for (int i = 0; i < 4; i++) {
                SetSpinnerRunnable setSpinnerRunnable = new SetSpinnerRunnable(spinners[i], allSignalsJsonArray);
                handler.post(setSpinnerRunnable);
            }
            Log.d(TAG, "InitSpinnerRunnable set signalJsonAllay finished");

        }

    }

    // 別スレッドで実行する必要あり
    public String GetAppliances(Context context) {

        FileRW fileRW = new FileRW(context);
        String TOKEN = fileRW.readFile(TOKEN_FILE_NAME);

        // URLへのコネクションを取得する。
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
            Log.d(TAG, "ResponseCode: " + String.valueOf(responseCode));

            // レスポンスのコードが2XX(正常)でなければTOKENを取得させるように促すactivityを開く
            if (responseCode != 200) {
                Intent intent = new Intent(StandardWidgetSetting.this, SetTokenActivity.class);
                startActivityForResult(intent, REQUEST_CODE);
                response = "failed";
            } else {
                response = convertToString(urlConnection.getInputStream());
            }



        } catch (IOException e) {
            Log.d(TAG, "IOException");
            e.printStackTrace();
        } finally {
            // 7.コネクションを閉じる。
            if (urlConnection != null) {
                urlConnection.disconnect();
                Log.d(TAG, "connection closed");
            }
        }

        return response;
    }

    public class SetSpinnerRunnable implements Runnable {
        Spinner spinner;
        JSONArray jsonArray;

        SetSpinnerRunnable(Spinner spinner, JSONArray jsonArray) {
            this.spinner   = spinner;
            this.jsonArray = jsonArray;
        }

        @Override
        public void run() {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(StandardWidgetSetting.this, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            for (int i = 0; i < jsonArray.length(); i++) {
                try {
                    //Log.d(TAG, json.getString(i));
                    //String id   = jsonAllSignalsArray.getJSONObject(i).getString("id");
                    String name = jsonArray.getJSONObject(i).getString("name");
                    adapter.add(name);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            // アダプターを設定
            spinner.setAdapter(adapter);
        }
    }

    private void SetWidget(Context context, int appWidgetId) {

        RemoteViews views = new RemoteViews(context.getPackageName(),
                R.layout.standard_widget);

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
        views.setViewPadding(R.id.imageView,
                paddingPx,
                paddingPx,
                paddingPx,
                paddingPx);


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
            int resourceId = res.getIdentifier(iconData,
                    "drawable", context.getPackageName());
            views.setImageViewResource(buttonId, resourceId);

            // ボタンの画像のpaddingの設定
            views.setViewPadding(buttonId,
                    paddingPx,
                    paddingPx,
                    paddingPx,
                    paddingPx);
        }

        // ウェジェットの更新
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        manager.updateAppWidget(appWidgetId, views);

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
                Spinner signalSpinner[] = new Spinner[4];
                for (int i = 0; i < 4; i++) {
                    // spinnerのIDを文字列から取得する
                    int signalSpinnerId = getResources().getIdentifier("spinner2" + (i + 1), "id", getPackageName());
                    signalSpinner[i] = findViewById(signalSpinnerId);
                }
                InitSpinnerRunnable initSpinnerRunnable = new InitSpinnerRunnable(signalSpinner);
                Thread thread = new Thread(initSpinnerRunnable);
                thread.start();
                break;
            default:
                break;
        }
    }




    //
    // パーミッションの確認
    //

    // permissionの確認
    public void checkPermission() {
        // 既に許可している
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED){

            // do something

        }
        // 拒否していた場合
        else{
            requestLocationPermission();
        }
    }

    // 許可を求める
    private void requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(StandardWidgetSetting.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION);

        } else {
            Toast.makeText(this, "アプリ実行に許可が必要です", Toast.LENGTH_SHORT).show();

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,},
                    REQUEST_PERMISSION);
        }
    }

    // 結果の受け取り
    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION) {
            // 使用が許可された
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                // do something

            } else {
                // それでも拒否された時の対応
                Toast.makeText(this, "cannot do anything", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
