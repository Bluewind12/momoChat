package com.example.momonyan.newapp_chat;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import java.util.*;

public class MainActivity extends AppCompatActivity {
    public static int SAMPLE_APP = 1;
    private TextView callView, responseView; // TextView
    private int checkedId; //ラジオボタンID
    private RadioGroup radioGroup; //RadioGroup
    private String text;
    private TextToSpeech tts;
    private double lat, lon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        callView = (TextView) findViewById(R.id.mainText);
        responseView = (TextView) findViewById(R.id.responsText);
        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        Button buttonStart = (Button) findViewById(R.id.chatButton);
        Button buttonMemo = (Button) findViewById(R.id.memoButton);
        tts = new TextToSpeech(getApplicationContext(), new SampleInitListener());  //テキストトゥスピーチオブジェクトの生成と、イベントリスナーの登録
        tts.setLanguage(Locale.JAPANESE);  //読み上げる言語の設定


        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkedId = radioGroup.getCheckedRadioButtonId();
                if (checkedId != -1) {
                    // 選択されているラジオボタンの取得
                    RadioButton radioButton = (RadioButton) findViewById(checkedId);// (Fragmentの場合は「getActivity().findViewById」にする)

                    // ラジオボタンのテキストを取得
                    text = radioButton.getText().toString();
                    //textView.setText(text);
                } else {
                    // 何も選択されていない場合の処理
                }
                try {
                    Intent it = new Intent();
                    it.setAction(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);  //音声認識にアクションを設定
                    it.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);  //音声認識の設定
                    it.putExtra(RecognizerIntent.EXTRA_PROMPT, "入力してください。");  //音声認識のプロンプト文字の設定
                    startActivityForResult(it, SAMPLE_APP); //結果を取得するインテントのスタート
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "音声認識は利用できません。", Toast.LENGTH_LONG).show();
                }
            }
        });

        buttonMemo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 遷移先のActivityを指定
                // Intent intent = new　Intent(このクラスから, このクラスへ)
                Intent intent = new Intent(MainActivity.this, MemoActivity.class);
                // 遷移開始
                startActivity(intent);
            }
        });

    }

    public void onActivityResult(int reqcode, int result, Intent it) {
        if (reqcode == SAMPLE_APP && result == RESULT_OK) { //インテント先から結果が返ってきたときの処理
            ArrayList<String> list = it.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);  //インテント先からのデータ取得
            callView.setText(list.get(0));

            //ラジオボタンの選択による処理の変更
            if (text.equals("会話・メモ")) {


                if (list.get(0).contains("こんにちは") || list.get(0).contains("おはよう")) {
                    //挨拶
                    String setTextData = "こんにちは";
                    speak(setTextData);
                    responseView.setText(setTextData);

                } else if (list.get(0).contains("おみくじ") || list.get(0).contains("運勢")) {
                    //おみくじ
                    String resultOmikuzi = omikuzi();
                    String setTextData = "今日の運勢は" + resultOmikuzi;
                    speak(setTextData);
                    responseView.setText(setTextData);

                } else if (list.get(0).contains("メモ")) {
                    //メモ
                    String memo = (String) list.get(0).replaceAll("メモ", "");
                    setMemo(memo);

                } else {
                    //該当なし
                    String setTextData = "よくわかりませんでした";
                    speak(setTextData);
                    responseView.setText(setTextData);

                }
            } else if (text.equals("検索・天気")) {
                if (list.get(0).contains("検索")) {
                    String search = (String) list.get(0).replaceAll("検索", "");
                    Intent intent = new Intent(MainActivity.this, searchWeb.class);
                    intent.putExtra("Url", "https://www.google.co.jp/search?q=" + search);
                    startActivity(intent);
                } else if (list.get(0).contains("天気")) {
                    String weather = (String) list.get(0).replaceAll("天気", "");
                    Intent intent = new Intent(MainActivity.this, searchWeb.class);

                    getLatLon(weather);
                    intent.putExtra("Url", "http://weathernews.jp/onebox/" + lat + "/" + lon + "/");
                    startActivity(intent);
                } else {
                    String setTextData = "検索か天気を選択してください";
                    speak(setTextData);
                    responseView.setText(setTextData);
                }
            } else {
                Log.e("Error", "ラジオボタンセットエラー");
            }

        }
    }

    //おみくじ
    public String omikuzi() {
        Random rand = new Random();
        int randint = rand.nextInt(100);
        if (randint <= 15) {
            return "大吉\n今日はきっとついていますね";
        } else if (randint <= 35) {
            return "中吉\n今日はなにかあるかもですね";
        } else if (randint <= 55) {
            return "小吉\nおめでとうございます";
        } else if (randint <= 75) {
            return "吉\n平凡ですね";
        } else if (randint <= 90) {
            return "凶\nがんばってください";
        } else {
            return "大凶\n気を落とさないでがんばってください";
        }
    }

    //メモ登録用
    private void setMemo(final String setText) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("メモ登録");
        builder.setMessage("メモ：" + setText + "を登録しますか？");

        builder.setPositiveButton("登録", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DBAdapter dbAdapter = new DBAdapter(MainActivity.this);
                dbAdapter.openDB();                                         // DBの読み書き
                dbAdapter.saveDB(setText);   // DBに登録
                dbAdapter.closeDB();                                        // DBを閉じる
                String setTextData = "メモに[" + setText + "]を登録しました";
                speak(setTextData);
                responseView.setText(setTextData);
            }
        });

        builder.setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String setTextData = "キャンセルしました";
                speak(setTextData);
                responseView.setText(setTextData);

            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    //県名から座標取得
    public void getLatLon(String city) {
        if (city.contains("愛知県")) {
            lat = 35.18028;
            lon = 0;
        } else if (city.contains("愛知県")) {
            lat = 35.18028;
            lon = 136.90667;
        } else if (city.contains("愛媛県")) {
            lat = 33.84167;
            lon = 132.76611;
        } else if (city.contains("茨城県")) {
            lat = 36.34139;
            lon = 140.44667;
        } else if (city.contains("岡山県")) {
            lat = 34.66167;
            lon = 133.935;
        } else if (city.contains("沖縄県")) {
            lat = 26.2125;
            lon = 127.68111;
        } else if (city.contains("岩手県")) {
            lat = 39.70361;
            lon = 141.1525;
        } else if (city.contains("岐阜県")) {
            lat = 35.39111;
            lon = 136.72222;
        } else if (city.contains("宮崎県")) {
            lat = 31.91111;
            lon = 131.42389;
        } else if (city.contains("宮城県")) {
            lat = 38.26889;
            lon = 140.87194;
        } else if (city.contains("京都府")) {
            lat = 35.02139;
            lon = 135.75556;
        } else if (city.contains("熊本県")) {
            lat = 32.78972;
            lon = 130.74167;
        } else if (city.contains("群馬県")) {
            lat = 36.39111;
            lon = 139.06083;
        } else if (city.contains("広島県")) {
            lat = 34.39639;
            lon = 132.45944;
        } else if (city.contains("香川県")) {
            lat = 34.34028;
            lon = 134.04333;
        } else if (city.contains("高知県")) {
            lat = 33.55972;
            lon = 133.53111;
        } else if (city.contains("佐賀県")) {
            lat = 33.24944;
            lon = 130.29889;
        } else if (city.contains("埼玉県")) {
            lat = 35.85694;
            lon = 139.64889;
        } else if (city.contains("三重県")) {
            lat = 34.73028;
            lon = 136.50861;
        } else if (city.contains("山形県")) {
            lat = 38.24056;
            lon = 140.36333;
        } else if (city.contains("山口県")) {
            lat = 34.18583;
            lon = 131.47139;
        } else if (city.contains("山梨県")) {
            lat = 35.66389;
            lon = 138.56833;
        } else if (city.contains("滋賀県")) {
            lat = 35.00444;
            lon = 135.86833;
        } else if (city.contains("鹿児島県")) {
            lat = 31.56028;
            lon = 130.55806;
        } else if (city.contains("秋田県")) {
            lat = 39.71861;
            lon = 140.1025;
        } else if (city.contains("新潟県")) {
            lat = 37.90222;
            lon = 139.02361;
        } else if (city.contains("神奈川県")) {
            lat = 35.44778;
            lon = 139.6425;
        } else if (city.contains("青森県")) {
            lat = 40.82444;
            lon = 140.74;
        } else if (city.contains("静岡県")) {
            lat = 34.97694;
            lon = 138.38306;
        } else if (city.contains("石川県")) {
            lat = 36.59444;
            lon = 136.62556;
        } else if (city.contains("千葉県")) {
            lat = 35.60472;
            lon = 140.12333;
        } else if (city.contains("大阪府")) {
            lat = 34.68639;
            lon = 135.52;
        } else if (city.contains("大分県")) {
            lat = 33.23806;
            lon = 131.6125;
        } else if (city.contains("長崎県")) {
            lat = 32.74472;
            lon = 129.87361;
        } else if (city.contains("長野県")) {
            lat = 36.65139;
            lon = 138.18111;
        } else if (city.contains("鳥取県")) {
            lat = 35.50361;
            lon = 134.23833;
        } else if (city.contains("島根県")) {
            lat = 35.47222;
            lon = 133.05056;
        } else if (city.contains("東京都")) {
            lat = 35.68944;
            lon = 139.69167;
        } else if (city.contains("徳島県")) {
            lat = 34.06583;
            lon = 134.55944;
        } else if (city.contains("栃木県")) {
            lat = 36.56583;
            lon = 139.88361;
        } else if (city.contains("奈良県")) {
            lat = 34.68528;
            lon = 135.83278;
        } else if (city.contains("富山県")) {
            lat = 36.69528;
            lon = 137.21139;
        } else if (city.contains("福井県")) {
            lat = 36.06528;
            lon = 136.22194;
        } else if (city.contains("福岡県")) {
            lat = 33.60639;
            lon = 130.41806;
        } else if (city.contains("福島県")) {
            lat = 37.75;
            lon = 140.46778;
        } else if (city.contains("兵庫県")) {
            lat = 34.69139;
            lon = 135.18306;
        } else if (city.contains("北海道")) {
            lat = 43.06417;
            lon = 141.34694;
        } else if (city.contains("和歌山県")) {
            lat = 34.22611;
            lon = 135.1675;
        }
    }

    //読み上げ
    private void speak(String talk) {
        if (tts.isSpeaking()) {
            tts.stop();
        }
        responseView.setText(talk);
        String utteranceId = this.hashCode() + "";  //utteranceIdの取得
        tts.speak(talk, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
    }

    class SampleInitListener implements TextToSpeech.OnInitListener {
        public void onInit(int status) {
        }

    }
}
