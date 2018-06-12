package com.example.momonyan.newapp_chat;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;

public class searchWeb extends AppCompatActivity{
    Button[] bt = new Button[5];
    WebView wv;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
       String url = intent.getStringExtra("Url");
        TableLayout tl = new TableLayout(this);
        setContentView(tl);

        wv = new WebView(this);  //Webサイト出力のためのビューの生成
        wv.setWebViewClient(new WebViewClient());  //Webビュークライアントの設定
        wv.loadUrl(url);  //URLの設定

        TableRow tr = new TableRow(this);

        for(int i=0; i<bt.length; i++){
            bt[i] = new Button(this);
            tr.addView(bt[i]);
        }

        bt[0].setText("←");  //戻るボタンの設定
        bt[1].setText("→");  //進むボタンの設定
        bt[2].setText("+");  //ズームインボタンの設定
        bt[3].setText("-");  //ズームアウトボタンの設定
        bt[4].setText("R");  //再度読み込みボタンの設定

        for(int i=0; i<bt.length; i++)
            bt[i].setOnClickListener(new SampleClickListener());

        tl.addView(tr);
        tl.addView(wv);
    }

    class SampleClickListener implements View.OnClickListener {
        public void onClick(View v){
            if(v == bt[0]){  //進むボタンの処理
                if(wv.canGoBack())
                    wv.goBack();
            }
            else if(v == bt[1]){  //戻るボタンの処理
                if(wv.canGoForward())
                    wv.goForward();
            }
            else if(v == bt[2])
                wv.zoomIn();  //ズームイン
            else if(v == bt[3])
                wv.zoomOut();  //ズームアウト
            else if(v == bt[4])
                wv.reload();  //再度読み込み
        }
    }
}
