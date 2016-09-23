package com.example.sjors.sjors_witteveen_pset3;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class MyAdapter extends ArrayAdapter<String[]> {

    public MyAdapter(Context context, String[][] searchResults) {
        super(context, R.layout.list_item, searchResults);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.list_item, parent, false);

        String[] searchResult = getItem(position);
        String typeAndYearString = searchResult[1] + " ("
                + searchResult[2] + ")";

        TextView title = (TextView) view.findViewById(R.id.movie_title);
        TextView typeAndYear = (TextView)
                view.findViewById(R.id.movie_type_year);

        title.setText(searchResult[0]);
        typeAndYear.setText(typeAndYearString);

        return view;
    }

}