package com.example.ivsmirnov.keyregistrator.async_tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.example.ivsmirnov.keyregistrator.databases.DataBaseShedule;
import com.example.ivsmirnov.keyregistrator.databases.DataBases;
import com.example.ivsmirnov.keyregistrator.interfaces.Shedule_Load;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by IVSmirnov on 08.09.2015.
 */
public class Load_shedule extends AsyncTask<Void, ArrayList<String>, Integer> {

    HttpURLConnection urlConnection = null;
    BufferedReader bufferedReader = null;
    String resultJSON = "";
    String resultGroups = "";
    String resultShedule = "";

    private Context mContext;
    private Shedule_Load shedule_load;

    private int result;

    private DataBases db;
    private DataBaseShedule dbShedule;

    public Load_shedule (Context c, Shedule_Load s){
        this.mContext = c;
        this.shedule_load = s;
    }

    @Override
    protected Integer doInBackground(Void... params) {

        db = new DataBases(mContext);
        dbShedule = new DataBaseShedule(mContext);

        JSONObject jsonObject = null;
        String facultyName = "";
        String facultyId = "";
        JSONArray faculties = null;
        ArrayList<String> facultyIDs = new ArrayList<>();
        ArrayList<String> dataFromJson = new ArrayList<>();
        ArrayList<String> groupsNames = new ArrayList<>();
        ArrayList<String> groupsIDs = new ArrayList<>();
        ArrayList<String> auditroomsList = db.readAudirtoomsFromDB();
        result = 0;

        Calendar calendar = Calendar.getInstance();
        int today = calendar.get(Calendar.DAY_OF_WEEK)-1;

        Date todayDate = calendar.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        String todayString = sdf.format(todayDate);

        try {
            Date todayDay = sdf.parse(todayString);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Calendar weekCalendar = Calendar.getInstance();
        weekCalendar.set(Calendar.DAY_OF_MONTH,1);
        weekCalendar.set(Calendar.MONTH,Calendar.SEPTEMBER);
        weekCalendar.set(Calendar.YEAR,2015);
        int weekOf1stSeptember = weekCalendar.get(Calendar.WEEK_OF_YEAR);
        int todayWeek = calendar.get(Calendar.WEEK_OF_YEAR);
        int weekFromStartStudy = todayWeek - weekOf1stSeptember + 1;
        Log.d("week",String.valueOf(weekFromStartStudy));

        dbShedule.clearBaseShedule();


        //список факультетов и их ID
        /*try {
            URL url = new URL("http://www.campus-card.fa.ru/Sched2Json.asmx/get_faculties");

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                buffer.append(line);
            }
            resultJSON = buffer.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            jsonObject = new JSONObject(resultJSON);
            faculties = jsonObject.getJSONArray("faculties");

            for (int i = 0; i < faculties.length(); i++) {
                JSONObject jsonbjectFacult = faculties.getJSONObject(i);
                facultyName = jsonbjectFacult.getString("faculty_name");
                facultyId = jsonbjectFacult.getString("faculty_id");
                //dataFromJson.add(facultyName);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }*/

        facultyIDs.add(String.valueOf(92));
        facultyIDs.add(String.valueOf(170));

        for (String f : facultyIDs){
            Log.d("facID",f);
        }

        //список групп с их ID
        for (String id : facultyIDs) {
            try {
                URL jsonURL = new URL("http://www.campus-card.fa.ru/Sched2Json.asmx/get_groups?faculty_id=" + id);

                urlConnection = (HttpURLConnection) jsonURL.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    buffer.append(line);
                }
                resultGroups = buffer.toString();

            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                JSONObject jsonGroups = new JSONObject(resultGroups);
                JSONArray jsonArrayGroups = jsonGroups.getJSONArray("groups");
                for (int i=0;i<jsonArrayGroups.length();i++){
                    JSONObject jsonObjectGroup = jsonArrayGroups.getJSONObject(i);
                    String groupName = jsonObjectGroup.getString("group_name");
                    String groupID = jsonObjectGroup.getString("group_id");
                    groupsNames.add(groupName);
                    groupsIDs.add(groupID);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        for (String gr : groupsIDs){
            Log.d("id",gr);
        }

        for (String group_id : groupsIDs){
            try {
                URL jsonURL = new URL("http://www.campus-card.fa.ru/Sched2Json.asmx/get_schedule?group_id="+group_id);

                urlConnection = (HttpURLConnection) jsonURL.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    buffer.append(line);
                }
                resultShedule = buffer.toString();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try{
                JSONObject jsonSchedule = new JSONObject(resultShedule);
                JSONArray jsonDays = jsonSchedule.getJSONArray("days");
                for (int i=0;i<jsonDays.length();i++){
                    JSONObject jsonObjectDay = jsonDays.getJSONObject(i);
                    String weekday = jsonObjectDay.getString("weekday");
                    JSONArray jsonArrayLessons = jsonObjectDay.getJSONArray("lessons");
                    if (today==Integer.parseInt(weekday)){
                        for (int j=0;j<jsonArrayLessons.length();j++){
                            JSONObject jsonObjectLessons = jsonArrayLessons.getJSONObject(j);
                            JSONArray jsonArrayAuditories = jsonObjectLessons.getJSONArray("auditories");

                            ArrayList<String> aud_names = new ArrayList<>();
                            for (int k=0;k<jsonArrayAuditories.length();k++){
                                JSONObject jsonObjectAuditories = jsonArrayAuditories.getJSONObject(k);
                                String aud_name = jsonObjectAuditories.getString("auditory_name");
                                aud_names.add(aud_name);
                            }

                            for (String audn : aud_names){
                                for (String aud : auditroomsList){
                                    if (audn.contains(aud)){

                                        String subject = jsonObjectLessons.getString("subject");

                                        JSONArray jsonArrayTeachers = jsonObjectLessons.getJSONArray("teachers");
                                        String teacher = "";
                                        for (int l=0;l<jsonArrayTeachers.length();l++){
                                            JSONObject jsonObjectTeachers = jsonArrayTeachers.getJSONObject(l);
                                            teacher += jsonObjectTeachers.getString("teacher_name")+"\n";
                                        }

                                        String timeStart = jsonObjectLessons.getString("time_start");
                                        if (timeStart.length()==4){
                                            timeStart = "0"+timeStart;
                                        }
                                        String timeEnd = jsonObjectLessons.getString("time_end");
                                        String group_name = jsonSchedule.getString("group_name");

                                        int parity = jsonObjectLessons.getInt("parity");
                                        String dateStart = jsonObjectLessons.getString("date_start");
                                        String dateEnd = jsonObjectLessons.getString("date_end");

                                        Date dayOfLessonStart = null;
                                        Date dayOfLessonEnd = null;
                                        try{
                                            dayOfLessonStart = sdf.parse(dateStart);
                                            dayOfLessonEnd = sdf.parse(dateEnd);
                                        }catch (Exception e){
                                            e.printStackTrace();
                                        }

                                        if (dayOfLessonStart.before(todayDate)&&dayOfLessonEnd.after(todayDate)){
                                            if (parity!=0&&weekFromStartStudy%2==0){ //неделя четная
                                                if (parity == 2 || parity ==0){
                                                    dbShedule.writeInBaseShedule(timeStart,timeEnd,group_name,teacher,audn,subject,String.valueOf(parity));
                                                }
                                            } else if (parity==0){
                                                dbShedule.writeInBaseShedule(timeStart, timeEnd, group_name, teacher, audn, subject, String.valueOf(parity));
                                            }else{//неделя нечетная
                                                if (parity == 1 || parity == 0){
                                                    dbShedule.writeInBaseShedule(timeStart, timeEnd, group_name, teacher, audn, subject, String.valueOf(parity));
                                                }

                                            }
                                            result = 1;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }catch (JSONException e){
                e.printStackTrace();
            }
        }

        return result;
    }

        @Override
        protected void onPostExecute (Integer r){
            super.onPostExecute(r);
            shedule_load.onFinish(r);
        }

        @Override
        protected void onPreExecute () {
            super.onPreExecute();
        }


}
