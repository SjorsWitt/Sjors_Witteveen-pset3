package com.example.sjors.sjors_witteveen_pset3;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

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
    TextView text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        search_input = (EditText) findViewById(R.id.search_input);
        text = (TextView) findViewById(R.id.textView);
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

        String Title;

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
                JSONArray search = obj.getJSONArray("Search");
                JSONObject first_search = search.getJSONObject(0);
                Title = first_search.getString("Title");
            } catch (JSONException e) {
                Title = "No results found";
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            text.setText(Title);
            super.onPostExecute(result);
        }
    }
}
