package com.example.ivsmirnov.keyregistrator.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.activities.Launcher;
import com.example.ivsmirnov.keyregistrator.adapters.adapter_persons_grid;
import com.example.ivsmirnov.keyregistrator.async_tasks.Find_User_in_SQL_Server;
import com.example.ivsmirnov.keyregistrator.async_tasks.Loader_Image;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseFavorite;
import com.example.ivsmirnov.keyregistrator.interfaces.Find_User_in_SQL_Server_Interface;
import com.example.ivsmirnov.keyregistrator.others.Values;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * Created by ivsmirnov on 04.12.2015.
 */
public class Search_Fragment extends Fragment implements Find_User_in_SQL_Server_Interface{

    private Context mContext;
    private SharedPreferences mPreferences;
    private Connection conn;
    private Find_User_in_SQL_Server_Interface mListener;

    private ProgressBar mProgressBar;
    private GridView mGridPersons;
    private Button mAddButton;

    public static Search_Fragment new_Instance(){
        return new Search_Fragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.layout_add_new_staff_fragment,container,false);
        mContext = rootView.getContext();
        mPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        mProgressBar = (ProgressBar)rootView.findViewById(R.id.layout_add_new_staff_progress);
        mProgressBar.setVisibility(View.INVISIBLE);

        mListener = this;
        mGridPersons = (GridView)rootView.findViewById(R.id.layout_add_new_staff_grid);
        mAddButton = (Button)rootView.findViewById(R.id.layout_add_new_staff_input_button);

        String ip = mPreferences.getString(Values.SQL_SERVER,"");
        String classs = "net.sourceforge.jtds.jdbc.Driver";
        final String db = "KeyRegistratorBase";
        String user = mPreferences.getString(Values.SQL_USER,"");
        String password = mPreferences.getString(Values.SQL_PASSWORD,"");

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);

        conn = null;
        String ConnURL = null;

        try {
            Class.forName(classs);
            ConnURL = "jdbc:jtds:sqlserver://" + ip + ";"
                    + "database=" + db +";user=" + user + ";password="
                    + password + ";";
            conn = DriverManager.getConnection(ConnURL);

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }


        final EditText mInputText = (EditText)rootView.findViewById(R.id.layout_add_new_staff_input_text);
        if (mInputText.requestFocus()){
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
        mInputText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length()>=3){
                    try {

                        Statement statement = conn.createStatement();

                        ResultSet resultSet = statement.executeQuery("select * from STAFF where [LASTNAME] like '"+s+"%'");

                        Find_User_in_SQL_Server find_user_in_sql_server = new Find_User_in_SQL_Server(mListener);

                        find_user_in_sql_server.execute(resultSet);

                    } catch (SQLException e) {
                        //Toast.makeText(mContext,e.toString(),Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                }
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mGridPersons.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int selectedPosition = position - parent.getFirstVisiblePosition();
                View gridItem = parent.getChildAt(selectedPosition);
                String [] items = new String[]{gridItem.getTag(R.string.grid_item_tag_lastname).toString(),
                        gridItem.getTag(R.string.grid_item_tag_firstname).toString(),
                        gridItem.getTag(R.string.grid_item_tag_midname).toString(),
                        gridItem.getTag(R.string.grid_item_tag_division).toString(),
                        gridItem.getTag(R.string.grid_item_tag_sex).toString(),
                        gridItem.getTag(R.string.grid_item_tag_radio_label).toString()};
                Loader_Image loader_image = new Loader_Image(mContext,items,Search_Fragment.this);
                loader_image.execute();
            }
        });

        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String source = mInputText.getText().toString();
                String[] split = source.split("\\s+");
                String surname = "-";
                String name = "-";
                String lastname = "-";
                String div = "-";
                String gender = "М";
                String tag = "null";
                try {
                    surname = split[0];
                    name = split[1];
                    lastname = split[2];
                } catch (Exception e) {
                    e.printStackTrace();
                }
                String [] items = new String[]{surname,name,lastname,div,gender,tag};
                Loader_Image loader_image = new Loader_Image(mContext,items,Search_Fragment.this);
                loader_image.execute();
            }
        });
        return rootView;
    }

    @Override
    public void changeProgressBar(int visibility) {
        mProgressBar.setVisibility(visibility);
        if (visibility==View.VISIBLE){
            mGridPersons.setVisibility(View.INVISIBLE);
        }else{
            mGridPersons.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void updateGrid(ArrayList<SparseArray> items) {
        if (!items.isEmpty()){
            mGridPersons.setAdapter(new adapter_persons_grid(mContext,items,2));
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (((Launcher)getActivity()).getSupportActionBar() !=null){
            ((Launcher)getActivity()).getSupportActionBar().setTitle(getResources().getString(R.string.title_add_new_staff));
        }
    }
}
