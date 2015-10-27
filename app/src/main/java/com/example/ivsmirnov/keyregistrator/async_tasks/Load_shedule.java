package com.example.ivsmirnov.keyregistrator.async_tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

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
import java.util.ArrayList;

/**
 * Created by IVSmirnov on 08.09.2015.
 */
public class Load_shedule extends AsyncTask<Void, ArrayList<String>, ArrayList<String>> {

    HttpURLConnection urlConnection = null;
    BufferedReader bufferedReader = null;
    String resultJSON = "";
    String resultGroups = "";
    String resultShedule = "";

    private Context mContext;
    private Shedule_Load shedule_load;

    public Load_shedule (Context c, Shedule_Load s){
        this.mContext = c;
        this.shedule_load = s;
    }

    @Override
    protected ArrayList<String> doInBackground(Void... params) {

        JSONObject jsonObject = null;
        String facultyName = "";
        String facultyId = "";
        JSONArray faculties = null;
        ArrayList<String> facultyIDs = new ArrayList<>();
        ArrayList<String> dataFromJson = new ArrayList<>();
        ArrayList<String> groupsNames = new ArrayList<>();
        ArrayList<String> groupsIDs = new ArrayList<>();

        //список факультетов и их ID
        try {
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
                facultyIDs.add(String.valueOf(92));
                facultyIDs.add(String.valueOf(170));
                //dataFromJson.add(facultyName);
            }
        } catch (JSONException e) {
            e.printStackTrace();
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
                    for (int j=0;j<jsonArrayLessons.length();j++){
                        JSONObject jsonObjectLessons = jsonArrayLessons.getJSONObject(j);
                        JSONArray jsonArrayAuditories = jsonObjectLessons.getJSONArray("auditories");

                        for (int k=0;k<jsonArrayAuditories.length();k++){
                            JSONObject jsonObjectAuditories = jsonArrayAuditories.getJSONObject(k);
                            String aud_name = jsonObjectAuditories.getString("auditory_name");
                            if (aud_name.contains(String.valueOf(510))){
                                String subject = jsonObjectLessons.getString("subject");

                                JSONArray jsonArrayTeachers = jsonObjectLessons.getJSONArray("teachers");
                                String teacher = "null";
                                for (int l=0;l<jsonArrayTeachers.length();l++){
                                    JSONObject jsonObjectTeachers = jsonArrayTeachers.getJSONObject(l);
                                    teacher = jsonObjectTeachers.getString("teacher_name");
                                }

                                String timeStart = jsonObjectLessons.getString("time_start");
                                String timeEnd = jsonObjectLessons.getString("time_end");

                                String group_name = jsonSchedule.getString("group_name");
                                Log.d("510",timeStart+"/"+timeEnd+" "+group_name+" "+ subject+" "+teacher);

                            }
                        }
                    }
                }
            }catch (JSONException e){
                e.printStackTrace();
            }
        }

        return groupsNames;
    }

        @Override
        protected void onPostExecute (ArrayList<String> s){
            super.onPostExecute(s);

        /*JSONObject jsonObject = null;
        String facultyName = "";
        String facultyId = "";
        JSONArray faculties = null;
        ArrayList <String> facultyIDs = new ArrayList<>();
        ArrayList <String> dataFromJson = new ArrayList<>();
        ArrayList <String> groups = new ArrayList<>();

        try {
            jsonObject = new JSONObject(s);
            faculties = jsonObject.getJSONArray("faculties");

            for (int i=0;i<faculties.length();i++){
                JSONObject jsonbjectFacult = faculties.getJSONObject(i);
                facultyName = jsonbjectFacult.getString("faculty_name");
                facultyId = jsonbjectFacult.getString("faculty_id");
                facultyIDs.add(facultyId);
                //dataFromJson.add(facultyName);
            }

            for (String id : facultyIDs){
                try {
                    URL jsonURL = new URL("http://www.campus-card.fa.ru/Sched2Json.asmx/get_groups?faculty_id="+id);

                    urlConnection = (HttpURLConnection)jsonURL.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();

                    InputStream inputStream = urlConnection.getInputStream();
                    StringBuffer buffer = new StringBuffer();
                    bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                    String line;
                    while ((line = bufferedReader.readLine())!=null){
                        buffer.append(line);
                    }
                    resultGroups = buffer.toString();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.d("result",resultGroups);
                JSONObject jsonGroups = new JSONObject(resultGroups);
                JSONArray jsonArrayGroups = jsonGroups.getJSONArray("groups");
                for (int i=0;i<jsonArrayGroups.length();i++){
                    JSONObject jsonObjectGroup = jsonArrayGroups.getJSONObject(i);
                    String groupName = jsonObjectGroup.getString("group_name");
                    groups.add(groupName);
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }



        shedule_load.onFinish(groups);*/
            shedule_load.onFinish(s);
        }

        @Override
        protected void onPreExecute () {
            super.onPreExecute();
        }


}
