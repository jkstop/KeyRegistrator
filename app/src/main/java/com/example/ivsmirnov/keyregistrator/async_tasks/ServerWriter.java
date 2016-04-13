package com.example.ivsmirnov.keyregistrator.async_tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.example.ivsmirnov.keyregistrator.databases.FavoriteDB;
import com.example.ivsmirnov.keyregistrator.databases.JournalDB;
import com.example.ivsmirnov.keyregistrator.databases.RoomDB;
import com.example.ivsmirnov.keyregistrator.items.JournalItem;
import com.example.ivsmirnov.keyregistrator.items.PersonItem;
import com.example.ivsmirnov.keyregistrator.items.RoomItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * Запись на сервер
 */
public class ServerWriter extends AsyncTask<Integer,Void,Void> {

   // public static final int JOURNAL_NEW = 100; //для добавления новой записи
   // public static final int JOURNAL_ALL = 101; //для добавления всех не синхронизированных записей
    public static final int JOURNAL_UPDATE = 104; //обновление журнала
    public static final int JOURNAL_DELETE_ONE = 102; //для удаления 1 записи
    public static final int JOURNAL_DELETE_ALL = 103; //для очистки все журнала
   // public static final int PERSON_NEW = 200; //добавление 1 нового пользователя
   // public static final int PERSON_ALL = 201; //добавление всех не синхронизированных пользователей
    public static final int PERSON_UPDATE = 204; //обновление пользователей
    public static final int PERSON_DELETE_ONE = 202; //удаление 1 пользователя
    public static final int PERSON_DELETE_ALL = 203; //удаление всех пользователей на сервере
    public static final int ROOMS_DELETE_ONE = 302; //удаление 1 записи
    public static final int ROOMS_DELETE_ALL = 303; //удаление всех записей
    public static final int ROOMS_UPDATE = 304; //обновление помещений

    private ProgressDialog mProgressDialog;

    private Long mJournalItemTag;
    private String mTag;
    private JournalItem mJournalItem;
    private PersonItem mPersonItem;
    private RoomItem mRoomItem;

    public ServerWriter (JournalItem journalItem){
        mJournalItem = journalItem;
    }

    public ServerWriter (Long journalItemTag){
        mJournalItemTag = journalItemTag;
    }

    public ServerWriter (String tag){
        mTag = tag;
    }

    public ServerWriter (PersonItem personItem){
        mPersonItem = personItem;
    }

    public ServerWriter (RoomItem roomItem){
        mRoomItem = roomItem;
    }

    public ServerWriter (){
    }

    public ServerWriter (Context context, boolean isShowProgress){
        if (isShowProgress) mProgressDialog = new ProgressDialog(context);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        System.out.println("server writer ***************************");
        if (mProgressDialog!=null){
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setMessage("Синхронизация...");
            mProgressDialog.show();
        }
    }

    @Override
    protected Void doInBackground(Integer... params) {
        try {
            Connection mConnection = SQL_Connection.SQLconnect;
            Statement mStatement = mConnection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ResultSet mResult;
            if (mConnection!=null){
                switch (params[0]){
                    case JOURNAL_UPDATE:
                        //если есть JournalItem, то обновляем именно эту запись. Если нет - обновляем все.
                        if (mJournalItem != null){
                            //получаем с сервера запись с указанным временм входа
                            mResult = mStatement.executeQuery("SELECT * FROM " + SQL_Connection.JOURNAL_TABLE + " WHERE " + SQL_Connection.COLUMN_JOURNAL_TIME_IN + " = " + mJournalItem.getTimeIn());
                            mResult.first();
                            if (mResult.getRow() == 0){ //такой записи нет, пишем
                                writeJournalItemToServer(mStatement, mJournalItem);
                            } else { //запись есть, обновляем время выхода
                                mResult.updateLong(SQL_Connection.COLUMN_JOURNAL_TIME_OUT,System.currentTimeMillis());
                                mResult.updateRow();
                            }
                        } else {
                            //загружаем все теги с сервера. если в журнале есть, а на сервере нет, то пишем
                            ArrayList<Long> mJournalTags = JournalDB.getJournalItemTags(null);
                            ArrayList<Long> mJournalServerTags = new ArrayList<>();

                            mResult = mConnection.prepareStatement("SELECT " + SQL_Connection.COLUMN_JOURNAL_TIME_IN + " FROM " + SQL_Connection.JOURNAL_TABLE).executeQuery();
                            while (mResult.next()){
                                mJournalServerTags.add(mResult.getLong(SQL_Connection.COLUMN_JOURNAL_TIME_IN));
                            }

                            //каждую отсутствующую запись пишем на сервер
                            for (Long unWriteTag : compareJournalTags(mJournalTags, mJournalServerTags)){
                                mJournalItem = JournalDB.getJournalItem(unWriteTag);
                                writeJournalItemToServer(mStatement, mJournalItem);
                            }

                            //если в журнале помещение закрылось, а на сервере нет, то исправляем
                            mResult = mStatement.executeQuery("SELECT " + SQL_Connection.COLUMN_JOURNAL_TIME_IN + "," + SQL_Connection.COLUMN_JOURNAL_TIME_OUT
                                    + " FROM " + SQL_Connection.JOURNAL_TABLE + " WHERE " + SQL_Connection.COLUMN_JOURNAL_TIME_OUT + " =0");
                            long timeIn;
                            while (mResult.next()){
                                timeIn = mResult.getLong(SQL_Connection.COLUMN_JOURNAL_TIME_IN);
                                if (!JournalDB.isItemOpened(timeIn)){
                                    mJournalItem = JournalDB.getJournalItem(timeIn);
                                    mResult.updateLong(SQL_Connection.COLUMN_JOURNAL_TIME_OUT, mJournalItem.getTimeOut());
                                    mResult.updateRow();
                                }
                            }
                        }
                        break;
                    case JOURNAL_DELETE_ONE:
                        if (mJournalItemTag!=null){
                            mStatement.execute("DELETE FROM " + SQL_Connection.JOURNAL_TABLE + " WHERE " + SQL_Connection.COLUMN_JOURNAL_TIME_IN + " = " + mJournalItemTag);
                        }
                        break;
                    case JOURNAL_DELETE_ALL:
                        mStatement.execute("TRUNCATE TABLE " + SQL_Connection.JOURNAL_TABLE);
                        break;
                    case PERSON_UPDATE:
                        //Если передан PersonItem, то перезаписываем его. Если нет - пишем всех.
                        if (mPersonItem != null){
                            mResult = mStatement.executeQuery("SELECT " + SQL_Connection.COLUMN_PERSONS_LASTNAME + " FROM "
                                    + SQL_Connection.PERSONS_TABLE + " WHERE " + SQL_Connection.COLUMN_PERSONS_RADIO_LABEL + " = '" + mPersonItem.getRadioLabel() + "'");
                            mResult.first();
                            if (mResult.getRow() == 1){ //если такой уже есть, то удаляем
                                mStatement.executeUpdate("DELETE FROM "
                                        + SQL_Connection.PERSONS_TABLE + " WHERE " + SQL_Connection.COLUMN_PERSONS_RADIO_LABEL + " = '" + mPersonItem.getRadioLabel() + "'");
                            }
                            //пишем нового пользователя на сервер
                            writePersonItemToServer(mStatement, mPersonItem);
                        } else {
                            //Получаем список тегов локальных пользователей и на сервере
                            ArrayList<String> localTags = FavoriteDB.getPersonsTags();
                            ArrayList<String> serverTags = new ArrayList<>();
                            mResult = mStatement.executeQuery("SELECT " + SQL_Connection.COLUMN_PERSONS_RADIO_LABEL + " FROM " + SQL_Connection.PERSONS_TABLE);
                            while (mResult.next()){
                                serverTags.add(mResult.getString(SQL_Connection.COLUMN_PERSONS_RADIO_LABEL));
                            }
                            //получаем список тэгов, которых нет на сервере. Для каждого пишем пользователя
                            for (String tag : compareStringLists(localTags, serverTags)){
                                writePersonItemToServer(mStatement, FavoriteDB.getPersonItem(tag, FavoriteDB.LOCAL_USER, FavoriteDB.ALL_PHOTO));
                            }
                        }
                        break;
                    case PERSON_DELETE_ONE:
                        if (mTag!=null){
                            mStatement.execute("DELETE FROM " + SQL_Connection.PERSONS_TABLE + " WHERE " + SQL_Connection.COLUMN_PERSONS_RADIO_LABEL + " ='" + mTag + "'");
                        }
                        break;
                    case PERSON_DELETE_ALL:
                        mStatement.execute("TRUNCATE TABLE " + SQL_Connection.PERSONS_TABLE);
                        break;
                    case ROOMS_UPDATE:
                        //если передан RoomItem, то обновляем на сервере одно помещение. Если RoomItem == null, то обновляем все помещения
                        if (mRoomItem != null){
                            mResult = mStatement.executeQuery("SELECT * FROM " +SQL_Connection.ROOMS_TABLE
                                    + " WHERE " + SQL_Connection.COLUMN_ROOMS_ROOM + " = " + mRoomItem.getAuditroom());
                            mResult.first();

                            //если помещение отсутствует на сервере,пишем его
                            if (mResult.getRow() == 0){
                                writeRoomItemToServer(mStatement, mRoomItem);
                            } else { //если такое помещение уже есть, то проверяем открыто оно или нет
                                if (mRoomItem.getStatus() != mResult.getInt(SQL_Connection.COLUMN_ROOMS_STATUS)){
                                    //статусы не совпали, надо обновить на сервере
                                    updateRoomItemToServer(mResult, mRoomItem);
                                }
                            }
                        } else {
                            //получаем локальный список помещений и список помещений на сервере
                            ArrayList<String> localRooms = RoomDB.getRoomList();
                            ArrayList<String> serverRooms = new ArrayList<>();
                            mResult = mStatement.executeQuery("SELECT * FROM " + SQL_Connection.ROOMS_TABLE);
                            String aud;
                            int status;
                            long timeLocal, timeServer;
                            while (mResult.next()){
                                serverRooms.add(mResult.getString(SQL_Connection.COLUMN_ROOMS_ROOM)); //добавляем помещение в список для последующего сравнения на наличие
                                aud = mResult.getString(SQL_Connection.COLUMN_ROOMS_ROOM);
                                status = mResult.getInt(SQL_Connection.COLUMN_ROOMS_STATUS);
                                mRoomItem = RoomDB.getRoomItem(aud);
                                //сравниваем статусы помещений
                                if (status != RoomDB.getRoomStatus(aud)){
                                    //статусы не совпали, что-то изменилось, обновляем запись на сервере
                                    updateRoomItemToServer(mResult, mRoomItem);
                                } else {
                                    //статусы совпали, но, возможно, все равно что-то изменилось. Сравниваем время входа в помещение.
                                    timeLocal = RoomDB.getRoomTimeIn(aud);
                                    timeServer = mResult.getLong(SQL_Connection.COLUMN_ROOMS_TIME);
                                    if (timeLocal != timeServer){
                                        //время входа разное. Обновляем на сервере.
                                        updateRoomItemToServer(mResult, mRoomItem);
                                    }
                                }
                            }
                            //получаем список аудиторий, которых нет на сервере. Пишем
                            for (String room : compareStringLists(localRooms,serverRooms)){
                                writeRoomItemToServer(mStatement, RoomDB.getRoomItem(room));
                            }
                        }
                        break;
                    case ROOMS_DELETE_ONE:
                        if (mTag!=null){
                            mStatement.execute("DELETE FROM " + SQL_Connection.ROOMS_TABLE + " WHERE " + SQL_Connection.COLUMN_ROOMS_ROOM + " = " + mTag);
                        }
                        break;
                    case ROOMS_DELETE_ALL:
                        mStatement.execute("TRUNCATE TABLE " + SQL_Connection.ROOMS_TABLE);
                        break;
                    default:
                        break;
                }

                if (mStatement!=null){
                    mStatement.close();
                }

            }

        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

   private ArrayList<Long> compareJournalTags(ArrayList<Long> localTags, ArrayList<Long> serverTags){
       ArrayList<Long> compareResults = new ArrayList<>();
       boolean found;
       for (Long localTag : localTags) {
           found = false;
           for (Long serverTag : serverTags) {
               if (serverTag.equals(localTag)) found = true;
           }
           if (!found) compareResults.add(localTag);
       }

       return compareResults;
   }

    private ArrayList<String> compareStringLists(ArrayList<String> localTags, ArrayList<String> serverTags){
        ArrayList<String> compareResults = new ArrayList<>();
        boolean found;
        for (String localTag : localTags){
            found = false;
            for (String serverTag : serverTags){
                if (serverTag.equals(localTag)) found = true;
            }
            if (!found) compareResults.add(localTag);
        }
        return compareResults;
    }

    private void writeJournalItemToServer(Statement statement, JournalItem journalItem){
        try {
            statement.executeUpdate("INSERT INTO " + SQL_Connection.JOURNAL_TABLE + " VALUES ('"
                    +journalItem.getAccountID()+"','"
                    +journalItem.getAuditroom()+"',"
                    +journalItem.getTimeIn()+","
                    +journalItem.getTimeOut()+","
                    +journalItem.getAccessType()+",'"
                    +journalItem.getPersonLastname()+"','"
                    +journalItem.getPersonFirstname()+"','"
                    +journalItem.getPersonMidname()+"','"
                    +journalItem.getPersonPhoto()+"')");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void writePersonItemToServer(Statement statement, PersonItem personItem){
        try {
            statement.executeUpdate("INSERT INTO " + SQL_Connection.PERSONS_TABLE + " VALUES ('"
                    +personItem.getLastname() + "','"
                    +personItem.getFirstname() + "','"
                    +personItem.getMidname() + "','"
                    +personItem.getDivision() + "','"
                    +personItem.getRadioLabel() + "','"
                    +personItem.getSex() + "','"
                    +personItem.getPhotoPreview() + "','"
                    +personItem.getPhotoOriginal() + "')");
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void writeRoomItemToServer(Statement statement, RoomItem roomItem){
        try {
            String userPhoto = null;
            //если радиометка не пустая (в помещении кто-то есть), нужно получить фото
            if (roomItem.getTag() != null) userPhoto = FavoriteDB.getPersonPhoto(roomItem.getTag(), FavoriteDB.LOCAL_PHOTO, FavoriteDB.PREVIEW_PHOTO);
            statement.executeUpdate("INSERT INTO " + SQL_Connection.ROOMS_TABLE + " VALUES ('"
            +roomItem.getAuditroom() + "',"
            +roomItem.getStatus() + ","
            +roomItem.getAccessType() + ","
            +roomItem.getTime() + ",'"
            +roomItem.getLastVisiter() + "','"
            +roomItem.getTag() + "','"
            +userPhoto + "')");
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void updateRoomItemToServer (ResultSet resultSet, RoomItem roomItem){
        try {
            String userPhoto = null;
            if (roomItem.getTag() != null) userPhoto = FavoriteDB.getPersonPhoto(roomItem.getTag(), FavoriteDB.LOCAL_PHOTO, FavoriteDB.PREVIEW_PHOTO);
            resultSet.updateInt(SQL_Connection.COLUMN_ROOMS_STATUS, roomItem.getStatus());
            resultSet.updateInt(SQL_Connection.COLUMN_ROOMS_ACCESS, roomItem.getAccessType());
            resultSet.updateLong(SQL_Connection.COLUMN_ROOMS_TIME, roomItem.getTime());
            resultSet.updateString(SQL_Connection.COLUMN_ROOMS_LAST_VISITER, roomItem.getLastVisiter());
            resultSet.updateString(SQL_Connection.COLUMN_ROOMS_RADIO_LABEL, roomItem.getTag());
            resultSet.updateString(SQL_Connection.COLUMN_ROOMS_PHOTO, userPhoto);
            resultSet.updateRow();
        } catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        System.out.println("server writer --------------------------------");
        if (mProgressDialog!=null && mProgressDialog.isShowing()) mProgressDialog.cancel();
    }
}
