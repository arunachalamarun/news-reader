package com.example.newsreader;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {


    ArrayList<String> urls = new ArrayList<>();
    ArrayList<String> headLines = new ArrayList<>();
    ListView news;
    SQLiteDatabase newsDb;
    int size = 20;


    public void update() {
        //  try {
        headLines.clear();
        urls.clear();
        Cursor c = newsDb.rawQuery("SELECT * FROM content", null);
        int headLinesIndex = c.getColumnIndex("title");
        int urlIndex = c.getColumnIndex("url");
        int primaryIndex = c.getColumnIndex("Id");
        int i = 0;
        //if (c.moveToFirst()) {
        c.moveToFirst();
        do {
            // Log.i("ifo is ", c.getString(headLinesIndex));
            headLines.add(c.getString(headLinesIndex));
            urls.add(c.getString(urlIndex));
            Log.i("info iss " + i, urls.toString());
            i++;
        } while (c.moveToNext());

        //  }
        Log.i("handled", "handled");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, headLines);
        news.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    } //catch (Exception e) {

    // }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        download dn = new download();
        news = findViewById(R.id.listView);
        newsDb = openOrCreateDatabase("title", MODE_PRIVATE, null);


        // update();


        if (headLines.isEmpty() && urls.isEmpty()) {
            Log.i("ENtered IF", "IFFFFF");
            newsDb.execSQL("DROP TABLE IF EXISTS content");
            newsDb.execSQL("CREATE TABLE IF NOT EXISTS content(Id INTEGER(3) PRIMARY KEY,title VARCHAR,url VARCHAR)");

            try {
                dn.execute("https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty").get();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
                // }
            }
        }

        news.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, Main2Activity.class);
                intent.putExtra("args", urls.get(position).toString());
                startActivity(intent);
            }
        });

        news.setOnScrollListener(new AbsListView.OnScrollListener() {

            public void onScrollStateChanged(AbsListView view, int scrollState) {


            }

            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
            //   update();

                Log.i("end of list", "end ");
            }
        });


    }


    public class download extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {

            URL url;
            String content = "";
            HttpsURLConnection urlConnection;

            try {
                url = new URL(strings[0]);
                urlConnection = (HttpsURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();
                while (data != -1) {
                    char c = (char) data;
                    content += c;
                    data = reader.read();
                }

                JSONArray jsonArray = new JSONArray(content);
               /* for (int i = 0; i < jsonArray.length(); i++) {
                    Log.i("data", jsonArray.get(i).toString());
                }*/

                if (jsonArray.length() != 0) {

                    for (int i = 0; i < jsonArray.length(); i++) {
                        //int i = 4;
                        String wholeContent = "";
                        url = new URL("https://hacker-news.firebaseio.com/v0/item/" + jsonArray.getInt(i) + ".json?print=pretty");
                        urlConnection = (HttpsURLConnection) url.openConnection();
                        in = urlConnection.getInputStream();
                        reader = new InputStreamReader(in);
                        int da = reader.read();
                        while (da != -1) {
                            char c1 = (char) da;
                            wholeContent += c1;
                            da = reader.read();
                        }

                        JSONObject separteDatas = new JSONObject(wholeContent);
                        if (separteDatas.isNull("title") && separteDatas.isNull("url")) {

                        } else {
                            // String newsContent = "";

                      /*  url = new URL(dataConnect);
                        urlConnection = (HttpsURLConnection) url.openConnection();
                        in = urlConnection.getInputStream();
                        reader = new InputStreamReader(in);
                        int dat = reader.read();
                        while (dat != -1) {
                            char c1 = (char) da;
                            newsContent += c1;
                            dat = reader.read();
                        }*/
                            String dataConnect = separteDatas.getString("url");
                            String heading = separteDatas.getString("title");
                            Log.i("Start of bind", "bind");

                            String sql = "INSERT INTO content(id,title,url) VALUES(? ,?, ?)";
                            SQLiteStatement statement = newsDb.compileStatement(sql);

                            statement.bindString(1, String.valueOf(i));
                            statement.bindString(2, heading);
                            statement.bindString(3, dataConnect);

                            statement.execute();
                            Log.i("End of bind", "bind");

                        }
                    }
                }
                return content;
                //Log.i("data is ",content.toString());

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(getApplicationContext(), "Pleae wait", Toast.LENGTH_SHORT).show();
            Log.i("Done", "Wait");

        }

        @Override
        protected void onPostExecute(String s) {

            update();
            Log.i("Done", "Completed");
            // Log.i("arrays are", urls.toString());
            //Log.i("arrays are", headLines.toString());
            super.onPostExecute(s);
        }
    }


}

