package com.example.mizuho.natureremowidget;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Locale;


public class FileRW {
    private Context context;
    private StringBuffer stringBuffer;

    FileRW(Context context){
        this.context = context;
        stringBuffer = new StringBuffer();
        stringBuffer.setLength(0);
    }

    // ファイルの削除
    void clearFile(String file_name){
        // ファイル削除
        context.deleteFile(file_name);
        // StringBuffer clear
        stringBuffer.setLength(0);
    }


    // 時間とともにmagをファイルを保存
    void writeLog(String file_name, String msg) {

        stringBuffer = new StringBuffer();

        long currentTime = System.currentTimeMillis();
        SimpleDateFormat dataFormat;
        dataFormat = new SimpleDateFormat("yyyy/MM/dd/HH:mm:ss", Locale.US);
        String cTime = dataFormat.format(currentTime);
        Log.d("debug", cTime);

        stringBuffer.append(cTime + " " + msg);
        stringBuffer.append(System.getProperty("line.separator"));// 改行

        // try-with-resources
        try (FileOutputStream fileOutputstream =
                     context.openFileOutput(file_name,
                             Context.MODE_APPEND)){

            fileOutputstream.write(stringBuffer.toString().getBytes());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 文字列をファイルに保存
    void writeFileMessage(String file_name, String msg) {

        stringBuffer = new StringBuffer();

        stringBuffer.append(msg);

        // try-with-resources
        try (FileOutputStream fileOutputstream =
                     context.openFileOutput(file_name,
                             Context.MODE_APPEND)){

            fileOutputstream.write(stringBuffer.toString().getBytes());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ファイルを読み出し
    String readFile(String file_name) {

        stringBuffer = new StringBuffer();

        // try-with-resources
        try (FileInputStream fileInputStream = context.openFileInput(file_name);
             BufferedReader reader= new BufferedReader(
                     new InputStreamReader(fileInputStream,"UTF-8"))
        ) {

            String lineBuffer;

            while( (lineBuffer = reader.readLine()) != null ) {
                stringBuffer.append(lineBuffer);
                stringBuffer.append(System.getProperty("line.separator"));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return stringBuffer.toString();
    }



    //
    // 外部ストレージにアクセス
    //

    // 外部ストレージに書き込み
    void writeFileLogToExternalStorage(String fileName, String str) {
        // 現在ストレージが書き込みできるかチェック
        if(isExternalStorageWritable()) {
            String filePath =
                    Environment.getExternalStorageDirectory().getPath()
                            + "/"+fileName;

            long currentTime = System.currentTimeMillis();
            SimpleDateFormat dataFormat;
            dataFormat = new SimpleDateFormat("yyyy/MM/dd/HH:mm:ss", Locale.US);
            String cTime = dataFormat.format(currentTime);
            //Log.d("debug", cTime);

            File file = new File(filePath);

            try(FileOutputStream fileOutputStream =
                        new FileOutputStream(file, true); // 追記したくない場合は第二引数のtrueを消す
                OutputStreamWriter outputStreamWriter =
                        new OutputStreamWriter(fileOutputStream, "UTF-8");
                BufferedWriter bw =
                        new BufferedWriter(outputStreamWriter)
            ) {

                bw.write(cTime + "," + str);
                bw.write(System.getProperty("line.separator")); // 改行
                bw.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    void writeFileToExternalStorage(String fileName, String str) {
        // 現在ストレージが書き込みできるかチェック
        if(isExternalStorageWritable()) {
            String filePath =
                    Environment.getExternalStorageDirectory().getPath()
                            + "/"+fileName;

            File file = new File(filePath);

            try(FileOutputStream fileOutputStream =
                        new FileOutputStream(file, true); // 追記したくない場合は第二引数のtrueを消す
                OutputStreamWriter outputStreamWriter =
                        new OutputStreamWriter(fileOutputStream, "UTF-8");
                BufferedWriter bw =
                        new BufferedWriter(outputStreamWriter)
            ) {

                bw.write(str);
                bw.write(System.getProperty("line.separator")); // 改行
                bw.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // 外部ストレージのファイルを読み出し
    String readFileToExternalStorage(String fileName) {

        String str = null;

        stringBuffer = new StringBuffer();

        // 現在ストレージが読出しできるかチェック
        if(isExternalStorageReadable()){
            String filePath =
                    Environment.getExternalStorageDirectory().getPath()
                            + "/" + fileName;
            //Log.d("debug", "filePath="+filePath);

            try(FileInputStream fileInputStream =
                        new FileInputStream(filePath);
                InputStreamReader inputStreamReader =
                        new InputStreamReader(fileInputStream, "UTF8");
                BufferedReader reader=
                        new BufferedReader(inputStreamReader) ) {

                String lineBuffer;

                while( (lineBuffer = reader.readLine()) != null ) {
                    stringBuffer.append(lineBuffer);
                    stringBuffer.append(System.getProperty("line.separator"));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //return str;
        return stringBuffer.toString();
    }

    // 外部ストレージが読み書き可能か確認
    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    // 外部ストレージが読み込み可能か確認
    private boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state));
    }

}


/*
* マニュフェストファイルに
*
* <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
* <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
*
* を追加
*
*
*
*
* 以下のコードをアクティビティに追加

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
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION);

    } else {
        Toast toast =
                Toast.makeText(this, "アプリ実行に許可が必要です", Toast.LENGTH_SHORT);
        toast.show();

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
            Toast toast =
                    Toast.makeText(this, "何もできません", Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}

*
*
* アプリからのSDカードへの書き込みは、アプリ固有ディレクトリ配下に限定
* /storage/sdcard/Android/data/{プロジェクトパッケージ}
*
* 外部ストレージへのアクセスは
* String filePath = Environment.getExternalStorageDirectory().getPath();
* でパスを取得し、
* filePath + {プロジェクトパッケージ名} + FILENAME
* のパスに対してしかできない
*
* */
