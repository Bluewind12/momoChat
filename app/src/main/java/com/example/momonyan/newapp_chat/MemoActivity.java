package com.example.momonyan.newapp_chat;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import java.util.ArrayList;

public class MemoActivity extends Activity {
    private DBAdapter dbAdapter;             // DBAdapter
    private ArrayAdapter<String> adapter;    // ArrayAdapter
    private ArrayList<String> items;         // ArrayList
    private ListView list;                   // ListView
    private Button deleteButton;            // 全削除ボタン

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.memo_layout);
        dbAdapter = new DBAdapter(this);
        dbAdapter.openDB();     // DBの読み込み(読み書きの方)
        findViews();            // 各部品の結び付け
        // ArrayListを生成
        items = new ArrayList<>();
        // DBのデータを取得
        String[] columns = {DBAdapter.COL_Memo};     // DBのカラム：品名
        Cursor c = dbAdapter.getDB(columns);

        if (c.moveToFirst()) {
            do {
                items.add(c.getString(0));
            } while (c.moveToNext());
        }
        c.close();
        dbAdapter.closeDB();    // DBを閉じる

        adapter = new ArrayAdapter<>
                (this, android.R.layout.simple_list_item_1, items);

        list.setAdapter(adapter);     //ListViewにアダプターをセット(=表示)

        // ArrayAdapterに対してListViewのリスト(items)の更新
        adapter.notifyDataSetChanged();
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!items.isEmpty()) {
                    dbAdapter.openDB();     // DBの読み込み(読み書きの方)
                    dbAdapter.allDelete();  // DBのレコードを全削除
                    dbAdapter.closeDB();    // DBを閉じる

                    //ArrayAdapterに対してListViewのリスト(items)の更新
                    adapter.clear();
                    adapter.addAll(items);
                    adapter.notifyDataSetChanged(); // // Viewの更新

                } else {
                    Toast.makeText(MemoActivity.this, "登録されているデータがありません。", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void findViews() {
        list = (ListView) findViewById(R.id.list);       // 品名一覧用のListView
        deleteButton = (Button) findViewById(R.id.deleteButton);
    }
}