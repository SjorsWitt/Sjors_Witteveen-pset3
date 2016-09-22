package com.example.sjors.sjors_witteveen_pset3;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {

    URL url;

    EditText search_input;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        search_input = (EditText) findViewById(R.id.search_input);
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        //mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new MyAdapter(new String[0]);
        mRecyclerView.setAdapter(mAdapter);
    }

    public void onSearch(View view) throws IOException, JSONException {

        // build a URL
        String OMDb_API = "http://www.omdbapi.com/?";
        String title_search = "s=" +
                URLEncoder.encode(search_input.getText().toString(), "UTF-8");
        url = new URL(OMDb_API + title_search);

        new readJsonFromURL().execute();
    }

    private class readJsonFromURL extends AsyncTask<Void, Void, Void> {

        JSONArray search;
        String toast_string;

        @Override
        protected Void doInBackground(Void... params) {
            // read from the URL
            Scanner scan = null;
            try {
                scan = new Scanner(url.openStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
            String str = "";
            while (scan.hasNext())
                str += scan.nextLine();
            scan.close();

            // build a JSON object
            try {
                JSONObject obj = new JSONObject(str);
                if (obj.getString("Response").equals("True")) {
                    search = obj.getJSONArray("Search");
                } else if (obj.getString("Response").equals("False")) {
                    toast_string = obj.getString("Error");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            if (search != null) {
                String[] titles = new String[10];
                for (int i = 0; i < search.length(); i++) {
                    try {
                        JSONObject search_result = search.getJSONObject(i);
                        titles[i] = search_result.getString("Title");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                mAdapter = new MyAdapter(titles);
                mRecyclerView.setAdapter(mAdapter);

            } else {
                    Toast.makeText(getApplicationContext(), toast_string,
                            Toast.LENGTH_SHORT).show();
            }
            super.onPostExecute(result);
        }


    }
}
