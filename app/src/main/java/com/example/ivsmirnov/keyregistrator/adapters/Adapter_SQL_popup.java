package com.example.ivsmirnov.keyregistrator.adapters;

import android.content.Context;
import android.support.v7.internal.widget.ActivityChooserModel;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.example.ivsmirnov.keyregistrator.R;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;

/**
 * Created by IVSmirnov on 03.07.2015.
 */
public class Adapter_SQL_popup extends ArrayAdapter <String>{

    private Context context;
    private ArrayList<String> items;
    private ArrayList<String> suggestions;
    private ArrayList<String> itemsAll;

    public Adapter_SQL_popup(Context c, ArrayList<String> i) {
        super(c, R.layout.cell_for_popup_sql,i);
        this.context = c;
        this.items = i;
        this.suggestions = new ArrayList<String>();
        this.itemsAll = (ArrayList<String>) items.clone();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rootView = inflater.inflate(R.layout.cell_for_popup_sql,parent,false);

        TextView textSurname = (TextView)rootView.findViewById(R.id.textSurname);
        TextView textName = (TextView)rootView.findViewById(R.id.textName);
        TextView textLastname = (TextView)rootView.findViewById(R.id.textLastname);
        TextView textKaf = (TextView)rootView.findViewById(R.id.textKaf);

        String item = items.get(position);
        String delims = ";";
        String [] split = item.split(delims);

        textSurname.setText(split[0]);
        textName.setText(split[1]);
        textLastname.setText(split[2]);
        textKaf.setText(split[3]);

        return rootView;
    }

    @Override
    public Filter getFilter() {
        return nameFilter;
    }

    Filter nameFilter = new Filter() {
        @Override
        public String convertResultToString(Object resultValue) {
            String str = ((String) (resultValue));
            return str;
        }
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            if(constraint != null) {
                suggestions.clear();
                for (String customer : itemsAll) {
                    if(customer.toLowerCase().startsWith(constraint.toString().toLowerCase())){
                        suggestions.add(customer);
                    }
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = suggestions;
                filterResults.count = suggestions.size();
                return filterResults;
            } else {
                return new FilterResults();
            }
        }
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            ArrayList<String> filteredList = (ArrayList<String>) results.values;
            if(results != null && results.count > 0) {
                clear();
                try {
                    for (String record : filteredList) {
                        add(record);
                    }

                }catch (ConcurrentModificationException e){
                    e.printStackTrace();
                }
                notifyDataSetChanged();
            }
        }
    };
}
