package com.example.sjors.sjors_witteveen_pset3;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {

    Context context = this;

    EditText search_input;

    private ListView movie_list;
    private String[][] searchResults = new String[10][5];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        search_input = (EditText) findViewById(R.id.search_input);
        movie_list = (ListView) findViewById(R.id.movie_list);

        movie_list.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent,
                                    View view, int position, long id) {

                Intent moreInfoActivity = new Intent(context, MoreInfoActivity.class);
                moreInfoActivity.putExtra("imdbID", searchResults[position][4]);
                startActivity(moreInfoActivity);
            }
        });
    }

    public void onSearch(View view) throws IOException, JSONException {

        // build a URL
        String OMDb_API = "http://www.omdbapi.com/?";
        String title_search = "s=" +
                URLEncoder.encode(search_input.getText().toString(), "UTF-8");
        URL url = new URL(OMDb_API + title_search);

        new readJsonSearchFromURL(url).execute();
    }

    private class readJsonSearchFromURL extends AsyncTask<Void, Void, Void> {

        URL url;
        JSONArray search;
        String toast_string;

        private readJsonSearchFromURL(URL url) {
            this.url = url;
        }

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
                for (int i = 0; i < search.length(); i++) {
                    try {
                        JSONObject searchResult = search.getJSONObject(i);
                        searchResults[i][0] = searchResult.getString("Title");
                        searchResults[i][1] = searchResult.getString("Type");
                        searchResults[i][2] = searchResult.getString("Year");
                        searchResults[1][3] = searchResult.getString("Poster");
                        searchResults[i][4] = searchResult.getString("imdbID");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                ListAdapter adapter = new MyAdapter(context, searchResults);
                movie_list.setAdapter(adapter);

            } else {
                    Toast.makeText(getApplicationContext(), toast_string,
                            Toast.LENGTH_SHORT).show();
            }
            super.onPostExecute(result);
        }

    }

}
