package com.example.faiflytest;

import android.app.Fragment;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.StringReader;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = "MainActivity";
    private static final String URL_DESCRIPTION = "http://api.geonames.org/wikipediaSearch?maxRows=1&username=bogs&q=";
    private Spinner country;
    private Spinner city;
    private EditText editText;
    private DBInfo dbInfo;
    private SaveSelectSpinner saveSelectSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        country = findViewById(R.id.country);
        city = findViewById(R.id.city);
        editText = findViewById(R.id.editText);
        dbInfo = new DBInfo(this, null, 1);

        FragmentManager fm = getFragmentManager();
        saveSelectSpinner = (SaveSelectSpinner) fm.findFragmentByTag("data");
        // create the fragment and data the first time
        if (saveSelectSpinner == null) {

            // add the fragment
            saveSelectSpinner = new SaveSelectSpinner();
            fm.beginTransaction().add(saveSelectSpinner, "data").commit();
            // load the data from the web
        } else{

        }
        writeInCountry();


        country.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                writeInCity();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        city.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                description();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }
    private void writeInCountry(){
        final ArrayAdapter<String> arrayAdapterCountry = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);


        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase db = dbInfo.getWritableDatabase();
                Cursor cursorCounry = db.query("country", null, null,null,null,null,null);
                if(cursorCounry.moveToFirst()){
                    int nameColIndex = cursorCounry.getColumnIndex("name");
                    arrayAdapterCountry.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                    do {
                        arrayAdapterCountry.add(cursorCounry.getString(nameColIndex));

                    } while (cursorCounry.moveToNext());

                } else{
                }
                cursorCounry.close();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        country.setAdapter(arrayAdapterCountry);
                        if (saveSelectSpinner != null&& country.getSelectedItemId() ==0) {
                            country.setSelection((int)saveSelectSpinner.getCountrySelect(), true);
                            saveSelectSpinner.setCountrySelect(0);


                        }
                    }
                });
            }
        });
        thread.start();


    }
    private void writeInCity(){
        final ArrayAdapter<String> arrayAdapterCity = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase db = dbInfo.getWritableDatabase();
                Cursor cursorCounry = db.query("city", null, "idCountry="+(country.getSelectedItemId()+1),null,null,null,null);
                if(cursorCounry.moveToFirst()){
                    int nameColIndex = cursorCounry.getColumnIndex("name");
                    arrayAdapterCity.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                    do {
                        arrayAdapterCity.add(cursorCounry.getString(nameColIndex));

                    } while (cursorCounry.moveToNext());

                } else{
                }
                cursorCounry.close();
            }
        });
        thread.start();
        try{
            thread.join();
        } catch (InterruptedException e){
            Log.e(LOG_TAG, e.getMessage());
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                city.setAdapter(arrayAdapterCity);
                if (saveSelectSpinner != null && city.getSelectedItemId() ==0) {

                    city.setSelection((int)saveSelectSpinner.getCitySelect(), true);
                    saveSelectSpinner.setCitySelect(0);

                }
            }
        });
    }
    private void description(){

        new Thread(new Runnable() {
            String descriptionText ="";

            @Override
            public void run() {
                HttpHendler httpHendler = new HttpHendler();
                String xmlText = httpHendler.makeServiceCall(URL_DESCRIPTION
                        + city.getSelectedItem().toString().replace(" ", "%20"));
                if(xmlText != null){
                    try{
                        XmlPullParser xmlPullParser = Xml.newPullParser();
                        xmlPullParser.setInput(new StringReader(xmlText));
                        while (xmlPullParser.next() != XmlPullParser.END_DOCUMENT){
                            if(xmlPullParser.getEventType()== XmlPullParser.START_TAG && xmlPullParser.getName().equals("summary")){
                                xmlPullParser.next();
                                descriptionText=xmlPullParser.getText();
                                break;
                            }

                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                editText.setText(descriptionText);
                            }
                        });
                    } catch (XmlPullParserException e){
                        Log.e(LOG_TAG, e.getMessage());
                    } catch (IOException e){
                        Log.e(LOG_TAG, e.getMessage());
                    }
                }
            }
        }).start();
    }
    public static class SaveSelectSpinner extends Fragment{
        private long countrySelect;
        private long citySelect;
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
        }

        public long getCountrySelect() {
            return countrySelect;
        }

        public void setCountrySelect(long countrySelect) {
            this.countrySelect = countrySelect;
        }

        public long getCitySelect() {
            return citySelect;
        }

        public void setCitySelect(long citySelect) {
            this.citySelect = citySelect;
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        saveSelectSpinner.setCitySelect(city.getSelectedItemId());
        saveSelectSpinner.setCountrySelect(country.getSelectedItemId());
    }
}
