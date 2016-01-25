package com.example.ivsmirnov.keyregistrator.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.activities.Launcher;
import com.example.ivsmirnov.keyregistrator.adapters.adapter_persons_grid;
import com.example.ivsmirnov.keyregistrator.async_tasks.Find_User_in_SQL_Server;
import com.example.ivsmirnov.keyregistrator.custom_views.PersonItem;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseFavorite;
import com.example.ivsmirnov.keyregistrator.interfaces.Find_User_in_SQL_Server_Interface;
import com.example.ivsmirnov.keyregistrator.interfaces.RecycleItemClickListener;
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
public class Search_Fragment extends Fragment implements Find_User_in_SQL_Server_Interface, RecycleItemClickListener{

    private Context mContext;
    private SharedPreferences mPreferences;
    private Connection conn;
    private Find_User_in_SQL_Server_Interface mListener;

    private ArrayList<PersonItem> mPersonItems;

    private ProgressBar mProgressBar;
    private RecyclerView mPersonsRecycler;
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

        mPersonsRecycler = (RecyclerView)rootView.findViewById(R.id.recycler_view_for_search_persons);
        mPersonsRecycler.setLayoutManager(new GridLayoutManager(mContext,3));

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


        final TextInputLayout mInputLayout = (TextInputLayout)rootView.findViewById(R.id.layout_add_new_staff_input);
        final AppCompatEditText mInputText = (AppCompatEditText) rootView.findViewById(R.id.layout_add_new_staff_input_text);
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
                        Find_User_in_SQL_Server find_user_in_sql_server = new Find_User_in_SQL_Server(mContext, mListener);
                        find_user_in_sql_server.execute(resultSet);

                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });


        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String source = mInputText.getText().toString();
                if (source.length()==0){
                    mInputLayout.setError(getResources().getString(R.string.input_empty_error));
                }else{
                    String[] split = source.split("\\s+");
                    String lastname = " ";
                    String firstname = " ";
                    String midname = " ";
                    String div = " ";
                    String gender = "М";
                    String tag = "null";
                    String photo = Find_User_in_SQL_Server.getBase64DefaultPhotoFromResources(mContext);
                    try {
                        lastname = split[0];
                        firstname = split[1];
                        midname = split[2];
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    addUserInFavorite(mContext, new PersonItem(
                            lastname,
                            firstname,
                            midname,
                            div,
                            gender,
                            DataBaseFavorite.getPhotoPreview(photo),
                            photo,
                            tag));
                }
            }
        });
        return rootView;
    }

    @Override
    public void changeProgressBar(int visibility) {
        mProgressBar.setVisibility(visibility);
        if (visibility==View.VISIBLE){
            mPersonsRecycler.setVisibility(View.INVISIBLE);
        }else{
            mPersonsRecycler.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void updateGrid(ArrayList<PersonItem> items) {
        if (!items.isEmpty()){
            mPersonItems = items;
            mPersonsRecycler.setAdapter(new adapter_persons_grid(mContext,
                    mPersonItems,
                    Values.SHOW_ALL_PERSONS,
                    this));
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (((Launcher)getActivity()).getSupportActionBar() !=null){
            ((Launcher)getActivity()).getSupportActionBar().setTitle(getResources().getString(R.string.title_add_new_staff));
        }
    }

    @Override
    public void onItemClick(View v, int position) {
        addUserInFavorite(mContext, mPersonItems.get(position));
    }

    @Override
    public void onItemLongClick(View v, int position, long timeIn) {
    }

    private void addUserInFavorite(final Context context, final PersonItem personItem){
        DataBaseFavorite dbFavorite = new DataBaseFavorite(context);
        dbFavorite.writeInDBTeachers(personItem);
        dbFavorite.closeDB();

        if (getView()!=null){
            Snackbar.make(getView(),R.string.snack_user_added,Snackbar.LENGTH_LONG)
                    .setAction(R.string.snack_cancel, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            DataBaseFavorite dataBaseFavorite = new DataBaseFavorite(context);
                            dataBaseFavorite.deleteFromTeachersDB(personItem);
                            dataBaseFavorite.closeDB();
                            Snackbar.make(getView(),R.string.snack_cancelled,Snackbar.LENGTH_LONG).show();
                        }
                    })
                    .show();
        }

    }

}
