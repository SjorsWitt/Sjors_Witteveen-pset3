package com.example.sjors.sjors_witteveen_pset3;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences pref;
    private Context context = this;

    private EditText search_bar;
    private ListView item_list;
    private String[][] listItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pref = getApplicationContext().getSharedPreferences("imdbIDs",
                MODE_PRIVATE);

        search_bar = (EditText) findViewById(R.id.search_bar);
        item_list = (ListView) findViewById(R.id.item_list);

        // load watch list on startup
        if (savedInstanceState == null) {
            displayWatchList();
        } else if (savedInstanceState.getInt("length") != 0) {
            // retrieve search results from savedInstanceState
            int arrayLength = savedInstanceState.getInt("length");
            listItems = new String[arrayLength][4];
            for (int i = 0; i < arrayLength; i++) {
                listItems[i] = savedInstanceState.getStringArray(
                        "item" + i);
            }
            updateListView();
        }

        // when search button in keyboard is clicked
        search_bar.setOnEditorActionListener(
                new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v,
                                          int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    onSearch();
                    return true;
                }
                return false;
            }
        });

        // item list OnItemClickListener
        item_list.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent,
                                    View view, int position, long id) {

                // start activity that displays more info on the item
                Intent moreInfoActivity = new Intent(context,
                        MoreInfoActivity.class);
                moreInfoActivity.putExtra("imdbID",
                        listItems[position][3]);
                startActivity(moreInfoActivity);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // add favorites button (half-filled star icon) to action bar
        getMenuInflater().inflate(R.menu.favorites_button, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // display watchlist & show toast when icon is clicked
        displayWatchList();

        Toast.makeText(getApplicationContext(), "Opened your watch list!",
                Toast.LENGTH_SHORT).show();

        return super.onOptionsItemSelected(item);
    }

    // displays watch list with added items
    private void displayWatchList() {

        // creates ArrayList with all saved IMDb IDs
        ArrayList<String> allSavedIDs = new ArrayList<>();
        Map<String, ?> allEntries = pref.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            allSavedIDs.add(entry.getKey());
        }

        listItems = new String[allSavedIDs.size()][4];

        // retrieve more info on item and put in list
        for (int i = 0; i < allSavedIDs.size(); i++) {
            try {
                URL url = new URL("http://www.omdbapi.com/?i=" +
                        allSavedIDs.get(i));
                new readJsonItemInfo(i, url).execute();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }

    // when search button is clicked
    private void onSearch() {

        // build a URL
        String OMDb_API = "http://www.omdbapi.com/?";
        URL url = null;
        try {
            String title_search = "s=" + URLEncoder.encode(
                    search_bar.getText().toString(), "UTF-8");
            url = new URL(OMDb_API + title_search);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        new readJsonSearchFromURL(url).execute();
    }

    // initialize ListAdapter and set to item_list
    private void updateListView() {
        ListAdapter adapter = new MyAdapter(context, listItems);
        item_list.setAdapter(adapter);
    }

    @Override
    protected void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);

        // save listItems
        if (listItems != null) {
            String key;
            for (int i = 0; i < listItems.length; i++) {
                String[] item = listItems[i];
                key = "item" + i;
                outState.putStringArray(key, item);
            }
            outState.putInt("length", listItems.length);
        } else {
            outState.putInt("length", 0);
        }
    }



    // read JSON search file from URL
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
            String str = "";
            try {
                Scanner scan = new Scanner(url.openStream());
                while (scan.hasNext())
                    str += scan.nextLine();
                scan.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

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

            // retrieve search results info
            if (search != null) {
                listItems = new String[search.length()][4];
                for (int i = 0; i < search.length(); i++) {
                    try {
                        JSONObject searchResult = search.getJSONObject(i);
                        listItems[i][0] = searchResult.getString("Title");
                        listItems[i][1] = searchResult.getString("Type");
                        listItems[i][2] = searchResult.getString("Year");
                        listItems[i][3] = searchResult.getString("imdbID");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                updateListView();

            } else {
                // display OMDb error message
                Toast.makeText(getApplicationContext(), toast_string,
                        Toast.LENGTH_SHORT).show();
            }
            super.onPostExecute(result);
        }

    }



    private class readJsonItemInfo extends AsyncTask<Void, Void, Void> {

        URL url;
        int position;
        JSONObject jsonMovieInfo;

        private readJsonItemInfo(int position, URL url) {
            this.url = url;
            this.position = position;
        }

        @Override
        protected Void doInBackground(Void... params) {

            // read from the URL
            String str = "";
            try {
                Scanner scan = new Scanner(url.openStream());
                while (scan.hasNext())
                    str += scan.nextLine();
                scan.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // build a JSON object
            try {
                jsonMovieInfo = new JSONObject(str);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            // retrieve JSON item info
            if (jsonMovieInfo != null) {
                try {
                    listItems[position][0] = jsonMovieInfo.getString("Title");
                    listItems[position][1] = jsonMovieInfo.getString("Type");
                    listItems[position][2] = jsonMovieInfo.getString("Year");
                    listItems[position][3] = jsonMovieInfo.getString("imdbID");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // sort listItems entry alphabetically based on title
                Arrays.sort(listItems, new Comparator<String[]>() {
                    @Override
                    public int compare(String[] o1, String[] o2) {
                        if (o1[0] != null && o2[0] != null) {
                            return o1[0].compareTo(o2[0]);
                        } else {
                            return 0;
                        }
                    }
                });

                updateListView();
            }
            super.onPostExecute(result);
        }
    }

}
