package com.example.ivsmirnov.keyregistrator.others;


import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.StrictMode;
import android.preference.PreferenceManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class Values{

    public static final String ALARM_SET = "alarm_set";
    public static final String COLUMNS_AUD_COUNT = "columns_count";
    public static final String COLUMNS_PER_COUNT = "columns_persons_count";
    public static final String AUDITROOM = "auditroom";
    public static final String POSITION_IN_BASE_FOR_ROOM = "position_in_base_for_room";
    public static final String POSITION_IN_LIST_FOR_ROOM = "position_in_list_for_room";
    public static final String POSITION_IN_ROOMS_BASE_FOR_ROOM = "position_in_rooms_base_for_room";
    public static final String CURSOR_POSITION = "cursor_position";
    public static final String DATE = "date";
    public static final String TODAY = "today";
    public static final int WRITE_JOURNAL = 100;
    public static final int WRITE_TEACHERS = 101;
    public static final int WRITE_ROOMS = 123;
    public static final String AUTO_CLOSED_COUNT = "auto_closed_count";
    public static final String PATH_FOR_COPY_ON_PC_FOR_JOURNAL = "path_for_copy_on_pc_for_journal";
    public static final String PATH_FOR_COPY_ON_PC_FOR_TEACHERS = "path_for_copy_on_pc_for_teachers";
    public static final String DISCLAIMER_SIZE = "disclaimer_size";
    public static final String GRID_SIZE = "grid_size";
    public static final String JOURNAL_SIZE = "journal_size";
    public static final int DIALOG_SEEKBAR = 102;
    public static final int DIALOG_LOADING = 103;
    public static final int DIALOG_CLEAR_JOURNAL = 104;
    public static final int DIALOG_CLEAR_TEACHERS = 105;
    public static final int DIALOG_CLEAR_ROOMS = 120;
    public static final int LOAD_JOURNAL = 106;
    public static final int LOAD_TEACHERS = 107;
    public static final int LOAD_ROOMS = 119;
    public static final int SELECT_LOCATION_JOURNAL = 121;
    public static final int SELECT_LOCATION_TEACHERS = 122;
    public static final int LOAD_FROM_SQL_SERVER = 124;
    public static final int DIALOG_SQL_CONNECT = 125;
    public static final String LINES_COUNT_IN_FILE = "lines_count";
    public static final String DIALOG_TYPE = "dialog_type";
    public static final int DIALOG_EDIT = 108;
    public static final int INPUT_DIALOG = 109;
    public static final int DELETE_ROOM_DIALOG = 110;
    public static final int ADD_ROOM_DIALOG = 111;
    public static final int SELECT_COLUMNS_DIALOG = 112;
    public static final int DIALOG_SEEKBARS = 113;
    public static final String PERSONS_FRAGMENT_TYPE = "persons_fragment_type";
    public static final String PERSONS_FRAGMENT_HEAD = "persons_fragment_head";
    public static final int PERSONS_FRAGMENT_EDITOR = 115;
    public static final int PERSONS_FRAGMENT_SELECTOR = 116;
    public static final int PERSONS_FRAGMENT_HEAD_NOT_FOUND_USER = 117;
    public static final String EMAIL = "email";
    public static final String PASSWORD = "password";
    public static final String RECIPIENTS = "recipients";
    public static final String BODY = "body";
    public static final String THEME = "theme";
    public static final String CHECK_JOURNAL = "check_journal";
    public static final String CHECK_TEACHERS = "check_teachers";
    public static final String DATE_SHEDULE_UPDATE = "date_shedule_update";
    public static final String SQL_SERVER = "sql_server";
    public static final String SQL_USER = "sql_user";
    public static final String SQL_PASSWORD = "sql_password";
    public static final String SQL_STATUS = "sql_status";
    public static final int SQL_STATUS_CONNECT = 126;
    public static final int SQL_STATUS_DISCONNECT = 127;
    public static final int DIALOG_CLOSE_ROOM = 118;
    public static final String DIALOG_CLOSE_ROOM_TYPE = "close_room_type";
    public static final int DIALOG_CLOSE_ROOM_TYPE_PERSONS = 127;
    public static final int DIALOG_CLOSE_ROOM_TYPE_ROOMS = 128;
    public static final int TYPE_GRID_PERSONS = 129;
    public static final int TAG_LASTNAME= 129;

    public static void copyfile(Context context, String srFile, String dtFile){
        try{
            File f1 = new File(srFile);
            File f2 = new File(dtFile);
            InputStream in = new FileInputStream(f1);
            OutputStream out = new FileOutputStream(f2);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0){
                out.write(buf, 0, len);
            }
            in.close();
            out.close();

        }
        catch(FileNotFoundException ex){
            System.out.println(ex.getMessage() + " in the specified directory.");
        }
        catch(IOException e){
            System.out.println(e.getMessage());
        }
    }

    public static String showDate() {
        Date currentDate =  new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM",new Locale("ru"));
        return String.valueOf(dateFormat.format(currentDate));
    }

    public static ArrayList<String> getListStaffForSearchFromServer(Context context){
        SharedPreferences mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        ArrayList<String> items = new ArrayList<>();
        String ip = mPreferences.getString(Values.SQL_SERVER,"");
        String classs = "net.sourceforge.jtds.jdbc.Driver";
        String db = "KeyRegistratorBase";
        String user = mPreferences.getString(Values.SQL_USER,"");
        String password = mPreferences.getString(Values.SQL_PASSWORD,"");

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Connection conn = null;
        String ConnURL = null;

        try {
            Class.forName(classs);
            ConnURL = "jdbc:jtds:sqlserver://" + ip + ";"
                    + "database=" + db +";user=" + user + ";password="
                    + password + ";";
            conn = DriverManager.getConnection(ConnURL);
            Statement statement = conn.createStatement();
            ResultSet resultSet = statement.executeQuery("select [LASTNAME],[FIRSTNAME],[MIDNAME],[NAME_DIVISION],[SEX],[RADIO_LABEL] from STAFF");
            while (resultSet.next()){
                String nameDivision = resultSet.getString("NAME_DIVISION");
                String lastname = resultSet.getString("LASTNAME");
                String firstname = resultSet.getString("FIRSTNAME");
                String midname = resultSet.getString("MIDNAME");
                String sex = resultSet.getString("SEX");
                String radioLabel = resultSet.getString("RADIO_LABEL");
                items.add(lastname+";"+firstname+";"+midname+";"+nameDivision+";"+sex+";"+resultSet.getRow()+";"+radioLabel);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();

        }
        return items;
    }

}
