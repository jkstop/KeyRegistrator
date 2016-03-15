package com.example.ivsmirnov.keyregistrator.others;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.activities.Launcher;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseFavorite;
import com.example.ivsmirnov.keyregistrator.items.JournalItem;
import com.example.ivsmirnov.keyregistrator.items.PersonItem;
import com.example.ivsmirnov.keyregistrator.items.RoomItem;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseJournal;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseRooms;
import com.example.ivsmirnov.keyregistrator.fragments.Persons_Fragment;

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

    //public static final String ALARM_SET = "alarm_set";
    public static final String COLUMNS_AUD_COUNT = "columns_count";
    //public static final String COLUMNS_PER_COUNT = "columns_persons_count";
    public static final String AUDITROOM = "auditroom";
    public static final String POSITION_IN_BASE_FOR_ROOM = "position_in_base_for_room";
    //public static final String POSITION_IN_LIST_FOR_ROOM = "position_in_list_for_room";
    //public static final String POSITION_IN_ROOMS_BASE_FOR_ROOM = "position_in_rooms_base_for_room";
    //public static final String CURSOR_POSITION = "cursor_position";
    //public static final String DATE = "date";
    public static final String TODAY = "today";
    public static final int WRITE_JOURNAL = 100;
    public static final int WRITE_TEACHERS = 101;
    public static final int WRITE_ROOMS = 123;
    public static final String AUTO_CLOSED_COUNT = "auto_closed_count";
    public static final String TOTAL_JOURNAL_COUNT = "total_journal_count";
    public static final String PATH_FOR_COPY_ON_PC_FOR_JOURNAL = "path_for_copy_on_pc_for_journal";
    public static final String PATH_FOR_COPY_ON_PC_FOR_TEACHERS = "path_for_copy_on_pc_for_teachers";
    public static final String DISCLAIMER_SIZE = "disclaimer_size";
    //public static final String GRID_SIZE = "grid_size";
    //public static final String JOURNAL_SIZE = "journal_size";
    //public static final int DIALOG_SEEKBAR = 102;
    //public static final int DIALOG_LOADING = 103;


    //public static final int LOAD_FROM_SQL_SERVER = 124;

    //public static final String LINES_COUNT_IN_FILE = "lines_count";

    //public static final int INPUT_DIALOG = 109;


    //public static final int DIALOG_SEEKBARS = 113;
    public static final String PERSONS_FRAGMENT_TYPE = "persons_fragment_type";
    //public static final String PERSONS_FRAGMENT_HEAD = "persons_fragment_head";
    public static final int PERSONS_FRAGMENT_EDITOR = 115;
    public static final int PERSONS_FRAGMENT_SELECTOR = 116;
    //public static final int PERSONS_FRAGMENT_HEAD_NOT_FOUND_USER = 117;
    //public static final String EMAIL = "email";
    //public static final String AUTH_TOKEN = "auth_token";
    //public static final String PASSWORD = "password";
    //public static final String RECIPIENTS = "recipients";
    //public static final String BODY = "body";
    //public static final String THEME = "theme";
    //public static final String CHECK_JOURNAL = "check_journal";
    //public static final String CHECK_TEACHERS = "check_teachers";
    //public static final String DATE_SHEDULE_UPDATE = "date_shedule_update";
    public static final String SQL_SERVER = "sql_server";
    public static final String SQL_USER = "sql_user";
    public static final String SQL_PASSWORD = "sql_password";
    public static final String SQL_STATUS = "sql_status";

    //public static final int SQL_STATUS_CONNECT = 126;
    //public static final int SQL_STATUS_DISCONNECT = 127;
    //public static final int DIALOG_DELETE_JOURNAL_ITEM = 129;
    //public static final int DIALOG_EMAIL = 132;

    public static final String ACTIVE_ACCOUNT_ID = "active_account_id";
    public static final String MAIL_RECEPIENTS = "mail_recepients";
    public static final String MAIL_ATTACHMENTS = "mail_attachments";
    public static final String MAIL_THEME = "mail_theme";
    public static final String MAIL_BODY = "mail_body";

    public static final int ROOM_IS_BUSY = 0;
    public static final int ROOM_IS_FREE = 1;

    public static final int SHOW_FAVORITE_PERSONS = 0;
    public static final int SHOW_ALL_PERSONS = 1;

    //request codes
    public static final int REQUEST_CODE_LOAD_FAVORITE_STAFF = 200;
    public static final int REQUEST_CODE_LOAD_JOURNAL = 201;
    public static final int REQUEST_CODE_LOAD_ROOMS = 202;
    public static final int REQUEST_CODE_SELECT_BACKUP_JOURNAL_LOCATION = 203;
    public static final int REQUEST_CODE_SELECT_BACKUP_FAVORITE_STAFF_LOCATION = 204;
    public static final int REQUEST_CODE_LOG_ON = 205;
    public static final int REQUEST_CODE_SELECT_EMAIL_ATTACHMENT = 206;

    //public static final String KEY_VALUES_FOR_DIALOG_PERSON_INFORMATION = "values_for_dialog_person_information";
    //public static final String KEY_USER_FOR_DIALOG_PERSON_INFORMATION = "values_for_dialog_person_information";
    //public static final int DIALOG_PERSON_INFORMATION_KEY_LASTNAME = 0;
    //public static final int DIALOG_PERSON_INFORMATION_KEY_FIRSTNAME = 1;
    //public static final int DIALOG_PERSON_INFORMATION_KEY_MIDNAME = 2;
    //public static final int DIALOG_PERSON_INFORMATION_KEY_DIVISION = 3;
    //public static final int DIALOG_PERSON_INFORMATION_KEY_PHOTO_ORIGINAL = 4;
    public static final String DIALOG_PERSON_INFORMATION_KEY_TAG = "DIALOG_PERSON_INFORMATION_KEY_TAG";
    //public static final int DIALOG_PERSON_INFORMATION_KEY_SEX = 5;

    public static final String EMPTY = " ";

    public static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

    public static final int TOAST_NEGATIVE = 0;
    public static final int TOAST_POSITIVE = 1;

    public static void copyfile(String srFile, String dtFile){
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
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy",new Locale("ru"));
        return String.valueOf(dateFormat.format(new Date())) + " г.";
    }

    public static void showFullscreenToast(Context context, String message, int type){
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View toastView = inflater.inflate(R.layout.layout_toast_fullscreen,null);
        TextView toastText = (TextView)toastView.findViewById(R.id.toast_fullscreen_text);
        toastText.setText(message);

        if (type==Values.TOAST_NEGATIVE){
            toastView.setBackgroundResource(R.color.colorAccent);
        }else{
            toastView.setBackgroundResource(R.color.primary);
        }

        Toast toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.FILL,0,0);
        toast.setView(toastView);
        toast.show();
    }

    public static void writeRoom (JournalItem journalItem, String personTag, long positionInBase){

        DataBaseRooms.updateRoom(new RoomItem().setAuditroom(journalItem.getAuditroom())
                .setStatus(Values.ROOM_IS_BUSY)
                .setAccessType(journalItem.getAccessType())
                .setPositionInBase(positionInBase)
                .setLastVisiter(Persons_Fragment.getPersonInitials(journalItem.getPersonLastname(),journalItem.getPersonFirstname(),journalItem.getPersonMidname()))
                .setTag(personTag));
    }

}
