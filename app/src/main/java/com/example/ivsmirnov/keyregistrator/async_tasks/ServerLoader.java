package com.example.ivsmirnov.keyregistrator.async_tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.example.ivsmirnov.keyregistrator.databases.FavoriteDB;
import com.example.ivsmirnov.keyregistrator.items.JournalItem;
import com.example.ivsmirnov.keyregistrator.databases.JournalDB;
import com.example.ivsmirnov.keyregistrator.interfaces.UpdateInterface;
import com.example.ivsmirnov.keyregistrator.items.PersonItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * Загрузка с сервера
 */
public class ServerLoader extends AsyncTask<Integer,Void,Void> {

    public static final int LOAD_JOURNAL = 100;
    public static final int LOAD_TEACHERS = 200;

    private long taskDurationStart, taskDurationEnd;

    private ProgressDialog mProgressDialog;
    private UpdateInterface mListener;

    public ServerLoader(Context context, UpdateInterface updateInterface){
        this.mListener = updateInterface;
        mProgressDialog = new ProgressDialog(context);

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        taskDurationStart = System.currentTimeMillis();
        System.out.println("start server loader");
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage("Загрузка с сервера...");
        mProgressDialog.show();
    }

    @Override
    protected Void doInBackground(Integer... params) {

        try {
            Connection connection = SQL_Connection.SQLconnect;
            if (connection!=null){

                Statement mStatement = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                ResultSet mResult;
                switch (params[0]){
                    case LOAD_JOURNAL:
                        ArrayList<Long> mJournalTags = JournalDB.getJournalItemTags(null);

                        //выбираем записи, которые есть на сервере, но нет в устройстве. пишем в устройство отсутствующие
                        mResult = mStatement.executeQuery("SELECT * FROM JOURNAL WHERE TIME_IN NOT IN (" + getInClause(mJournalTags) + ")");
                        while (mResult.next()){
                            JournalDB.writeInDBJournal(new JournalItem()
                                    .setAccountID(mResult.getString("ACCOUNT_ID"))
                                    .setAuditroom(mResult.getString("AUDITROOM"))
                                    .setTimeIn(mResult.getLong("TIME_IN"))
                                    .setTimeOut(mResult.getLong("TIME_OUT"))
                                    .setAccessType(mResult.getInt("ACCESS"))
                                    .setPersonLastname(mResult.getString("PERSON_LASTNAME"))
                                    .setPersonFirstname(mResult.getString("PERSON_FIRSTNAME"))
                                    .setPersonMidname(mResult.getString("PERSON_MIDNAME"))
                                    .setPersonPhoto(mResult.getString("PERSON_PHOTO")));
                        }

                        //проверяем открытые помещения. Если на сервере они закрыты, то обновляем локальный журнал.
                        //сначала получаем тэги открытых помещений  в журнале (они же время входа)
                        ArrayList<Long> mOpenTags = JournalDB.getOpenRoomsTags();

                        //для каждого помещения проверяем, закрылось ли на сервере
                        mResult = mStatement.executeQuery("SELECT TIME_OUT FROM JOURNAL WHERE TIME_IN IN (" + getInClause(mOpenTags) + ")");
                        long timeOut;
                        while (mResult.next()){
                            timeOut = mResult.getLong("TIME_OUT");
                            if (timeOut!=0) JournalDB.updateDB(mOpenTags.get(mResult.getRow()-1), timeOut);
                        }

                        break;
                    case LOAD_TEACHERS:
                        //отправляем запрос, получаем радиометки всех пользователей с сервера
                        ResultSet personsTagsResult = connection.prepareStatement("SELECT RADIO_LABEL FROM TEACHERS").executeQuery();
                        ResultSet personsItemResult;

                        while (personsTagsResult.next()){
                            String tag = personsTagsResult.getString("RADIO_LABEL"); //тэг пользователя
                            //если пользователя нет в базе на устройстве, то загружаем его с сервера и пишем в базу на устройство
                            if (!FavoriteDB.isUserInBase(tag)){
                                personsItemResult = mStatement.executeQuery("SELECT * FROM TEACHERS WHERE RADIO_LABEL ='" + tag + "'");
                                personsItemResult.first();
                                FavoriteDB.writeInDBTeachers(new PersonItem()
                                        .setLastname(personsItemResult.getString("LASTNAME"))
                                        .setFirstname(personsItemResult.getString("FIRSTNAME"))
                                        .setMidname(personsItemResult.getString("MIDNAME"))
                                        .setDivision(personsItemResult.getString("DIVISION"))
                                        .setRadioLabel(tag)
                                        .setSex(personsItemResult.getString("SEX"))
                                        .setPhotoPreview(personsItemResult.getString("PHOTO_PREVIEW"))
                                        .setPhotoOriginal(personsItemResult.getString("PHOTO_ORIGINAL")));
                                System.out.println("write in local " + tag);
                            }

                        }
                        break;
                    default:
                        break;
                }


            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getInClause(ArrayList<Long> items){
        StringBuilder inClause = new StringBuilder();
        for (int i=0; i < items.size(); i++) {
            inClause.append(items.get(i));
            inClause.append(',');
        }
        if (inClause.length() == 0){
            inClause.append(0);
        } else {
            inClause.delete(inClause.length()-1,inClause.length());
        }
        return inClause.toString();
    }



    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        taskDurationEnd = System.currentTimeMillis();
        System.out.println("LOAD TASK DURATION " + (taskDurationEnd - taskDurationStart));
        if (mProgressDialog.isShowing()){
            mProgressDialog.cancel();
        }
        if (mListener!=null) mListener.updateInformation();
    }
}
