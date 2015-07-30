package com.example.ivsmirnov.keyregistrator.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.ivsmirnov.keyregistrator.databases.DataBases;
import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.others.Values;
import com.example.ivsmirnov.keyregistrator.activities.FileManager;


public class Fragment_settings_journal extends Fragment {

    private ListView listView;
    private Context context;
    private static String mPath;
    private SharedPreferences preferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_settings_journal,container,false);
        mPath = Environment.getExternalStorageDirectory().getPath();
        context = rootView.getContext();
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        listView = (ListView)rootView.findViewById(R.id.fragment_list_journal);
        listView.setAdapter(new ArrayAdapter<String>(context,android.R.layout.simple_list_item_1,context.getResources().getStringArray(R.array.db_journal_items)){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View root = super.getView(position, convertView, parent);
                TextView text = (TextView)root.findViewById(android.R.id.text1);
                text.setGravity(Gravity.CENTER);
                text.setTextColor(Color.BLACK);
                return root;
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        DataBases db = new DataBases(context);
                        db.writeFile(Values.WRITE_JOURNAL);
                        db.closeDBconnection();
                        break;
                    case 1:
                        String path = preferences.getString(Values.PATH_FOR_COPY_ON_PC, Environment.getExternalStorageDirectory().getPath());
                        String srFileJournal = mPath + "/Journal.txt";
                        String dtFileJournal = path + "/Journal.txt";
                        DataBases.copyfile(context, srFileJournal, dtFileJournal);
                        break;
                    case 2:
                        startActivity(new Intent(context,FileManager.class).putExtra("what",10));
                        break;
                    case 3:
                        Dialog_Fragment dialog = new Dialog_Fragment();
                        Bundle bundle = new Bundle();
                        bundle.putInt(Values.DIALOG_TYPE,Values.DIALOG_CLEAR_JOURNAL);
                        dialog.setArguments(bundle);
                        dialog.show(getFragmentManager(),"clearJournal");
                        break;
                    default:
                        break;
                }
            }
        });
        return rootView;
    }
}
