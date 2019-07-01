package com.example.faiflytest;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DBInfo extends SQLiteOpenHelper {
    private static final String LOG_TAG = "DB_INFO";
    private static final String NAME_DB = "Info";
    private JSONObject jsonObject;
    private final String URL_JSON = "https://raw.githubusercontent.com/David-Haim/CountriesToCitiesJSON/master/countriesToCities.json";
    public DBInfo(Context context, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, NAME_DB, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table country ("
                + "id integer primary key autoincrement,"
                + "name text" + ");");
        db.execSQL("create table city ("
                + "id integer primary key autoincrement,"
                + "idCountry integer,"
                + "name text" + ");");
        HttpHendler sh = new HttpHendler();
        String jsonStr = sh.makeServiceCall(URL_JSON);
        ContentValues cv;
        if(jsonStr != null){
            try {
                jsonObject = new JSONObject(jsonStr);
                JSONArray country = jsonObject.names();
                for(int i=0; i<country.length(); i++){
                    JSONArray city = jsonObject.getJSONArray(country.getString(i));
                    cv = new ContentValues();
                    cv.put("name", country.getString(i));
                    long indexCountry = db.insert("country", null, cv);
                    for (int y=0; y<city.length();y++){
                        cv = new ContentValues();
                        cv.put("idCountry", indexCountry);
                        cv.put("name", city.getString(y));
                        db.insert("city", null, cv);
                    }
                }

            } catch (JSONException e){
                Log.e(LOG_TAG, e.getMessage());
            }

        } else{
            Log.e(LOG_TAG, "Erorr connect to JSON country-city");
        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

}
