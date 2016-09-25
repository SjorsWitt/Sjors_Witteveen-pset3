package com.example.sjors.sjors_witteveen_pset3;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class MoreInfoActivity extends AppCompatActivity {

    private SharedPreferences pref;

    private ImageView movie_poster;
    private TextView movie_title;
    private TextView movie_type_year;
    private TextView movie_released;
    private TextView movie_genre;
    private TextView movie_runtime;
    private TextView movie_imdb_rating;
    private TextView movie_plot;
    private TextView movie_director;
    private TextView movie_writer;
    private TextView movie_actors;
    private TextView movie_awards;

    // IMDb id of the item opened in this activity
    private String imdbID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.more_info_activity);

        pref = getApplicationContext().getSharedPreferences("imdbIDs",
                MODE_PRIVATE);

        // display up navigation button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        movie_poster = (ImageView) findViewById(R.id.movie_poster);
        movie_title = (TextView) findViewById(R.id.movie_title);
        movie_type_year = (TextView) findViewById(R.id.movie_type_year);
        movie_released = (TextView) findViewById(R.id.movie_released);
        movie_genre = (TextView) findViewById(R.id.movie_genre);
        movie_runtime = (TextView) findViewById(R.id.movie_runtime);
        movie_imdb_rating = (TextView) findViewById(R.id.movie_imdb_rating);
        movie_plot = (TextView) findViewById(R.id.movie_plot);
        movie_director = (TextView) findViewById(R.id.movie_director);
        movie_writer = (TextView) findViewById(R.id.movie_writer);
        movie_actors = (TextView) findViewById(R.id.movie_actors);
        movie_awards = (TextView) findViewById(R.id.movie_awards);

        // get IMDb ID String from previous activity
        Intent intent = getIntent();
        imdbID = intent.getExtras().getString("imdbID");

        // look up more info on IMDb item
        URL url = null;
        try {
            url = new URL("http://www.omdbapi.com/?i=" + imdbID);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        new readJsonItemInfo(url).execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // add favorite button (star icon) to action bar
        getMenuInflater().inflate(R.menu.favorite_button, menu);

        MenuItem favorite_button = menu.findItem(R.id.favorite_button);

        // if item is already added to watch list, display filled star icon
        if (pref.getBoolean(imdbID, false)) {
            favorite_button.setIcon(ResourcesCompat.getDrawable(
                    getResources(), R.drawable.ic_favorite_clicked, null));
            favorite_button.setTitle("Remove from your watch list.");
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // if favorite button is pressed
        if (item.getItemId() == R.id.favorite_button) {
            SharedPreferences.Editor editor = pref.edit();

            // if item is already on watch list
            if (pref.getBoolean(imdbID, false)) {
                editor.remove(imdbID);

                item.setIcon(ResourcesCompat.getDrawable(getResources(),
                        R.drawable.ic_favorite_unclicked, null));
                Toast.makeText(getApplicationContext(),
                        "Removed from your watch list!", Toast.LENGTH_SHORT)
                        .show();

                item.setTitle("Add to your watch list.");

            // if item is not on watch list
            } else {
                editor.putBoolean(imdbID, true);

                item.setIcon(ResourcesCompat.getDrawable(
                        getResources(), R.drawable.ic_favorite_clicked, null));
                Toast.makeText(getApplicationContext(),
                        "Added to your watch list!", Toast.LENGTH_SHORT)
                        .show();

                item.setTitle("Remove from your watch list.");
            }
            editor.apply();

        // if back button is pressed
        } else {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private class readJsonItemInfo extends AsyncTask<Void, Void, Void> {

        URL url;
        JSONObject jsonMovieInfo;

        private readJsonItemInfo(URL url) {
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
                String urlString = "";
                try {
                    urlString = jsonMovieInfo.getString("Poster");

                    movie_title.setText(jsonMovieInfo.getString("Title"));

                    String typeAndYear = jsonMovieInfo.getString("Type") +
                            " (" + jsonMovieInfo.getString("Year") + ")";
                    movie_type_year.setText(typeAndYear);
                    movie_released.setText(
                            jsonMovieInfo.getString("Released"));
                    movie_genre.setText(jsonMovieInfo.getString("Genre"));
                    movie_runtime.setText(jsonMovieInfo.getString("Runtime"));

                    String imdbRating = "IMDb Rating: " +
                            jsonMovieInfo.getString("imdbRating");
                    movie_imdb_rating.setText(imdbRating);

                    movie_plot.setText(jsonMovieInfo.getString("Plot"));

                    String director = "Director: " +
                            jsonMovieInfo.getString("Director");
                    movie_director.setText(director);

                    String writer = "Writer: " +
                            jsonMovieInfo.getString("Writer");
                    movie_writer.setText(writer);

                    String actors = "Actors: " +
                            jsonMovieInfo.getString("Actors");
                    movie_actors.setText(actors);

                    String awards = "Awards: " +
                            jsonMovieInfo.getString("Awards");
                    movie_awards.setText(awards);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (urlString.equals("N/A")) {
                    movie_poster.setVisibility(View.GONE);
                } else {
                    new DownloadImageTask(movie_poster).execute(urlString);
                }
            }
            super.onPostExecute(result);
        }

    }

    // Image download task
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }


}
