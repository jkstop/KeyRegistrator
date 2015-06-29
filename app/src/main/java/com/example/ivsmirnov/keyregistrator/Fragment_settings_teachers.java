package com.example.ivsmirnov.keyregistrator;

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

/**
 * Created by ivsmirnov on 18.06.2015.
 */
public class Fragment_settings_teachers extends Fragment {

    private ListView listView;
    private Context context;
    private static String mPath;
    private SharedPreferences preferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_settings_teachers,container,false);
        context = rootView.getContext();
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        mPath = Environment.getExternalStorageDirectory().getPath();
        listView = (ListView)rootView.findViewById(R.id.fragment_list_teachers);
        listView.setAdapter(new ArrayAdapter<String>(context,android.R.layout.simple_list_item_1,context.getResources().getStringArray(R.array.db_teacher_items)){
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
                        db.writeFile(Values.WRITE_TEACHERS);
                        db.closeDBconnection();
                        break;
                    case 1:
                        String path = preferences.getString("pathPC", Environment.getExternalStorageDirectory().getPath());
                        String srFileTeachers = mPath + "/Teachers.txt";
                        String dtFileTeachers = path + "/Teachers.txt";
                        DataBases.copyfile(context, srFileTeachers, dtFileTeachers);
                        break;
                    case 2:
                        startActivity( new Intent(context, FileManager.class).putExtra("what",11));
                        break;
                    case 3:
                        Dialog_Fragment dialog = new Dialog_Fragment(context,Values.DIALOG_CLEAR_TEACHERS);
                        dialog.show(getFragmentManager(),"clearTeachers");
                        break;
                    default:
                        break;
                }
            }
        });
        return rootView;
    }
}
