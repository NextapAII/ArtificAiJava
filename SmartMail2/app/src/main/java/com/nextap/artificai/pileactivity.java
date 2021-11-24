package com.nextap.artificai;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static com.nextap.artificai.CustomPage.shareApp;

public class pileactivity extends AppCompatActivity {

    ArrayList<String> list = new ArrayList<>();
    ArrayList<String> list_descript = new ArrayList<>();
    ArrayList<String> list_dates = new ArrayList<>();
    customAdapter ca = new customAdapter();
    OverscrollListView listView;
    ConfirmDialog d;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pileactivity);
        getSupportActionBar().hide();
        d = new ConfirmDialog(pileactivity.this);

        //list.add("Titanic");
        //list.add("Avengers");
        if(getListFromLocal("dates")!=null){
            list = getListFromLocal("save_titles");
            list_descript = getListFromLocal("save_titledescript");
            list_dates = getListFromLocal("dates");}


        //when the item is clicked,start a new activity and send the title name to it
        listView = findViewById(R.id.listView);
        ca.setList(list);
        listView.setAdapter(ca);
        //make a search section to search for a particular title
        EditText searchView = findViewById(R.id.searchView);
        final Intent intent = new Intent(this, MainActivity.class);
        searchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                final List<String> filteredList = new ArrayList<>();
                if(s.length() != 0){
                    //if the text is not empty, filter it.
                    for (String each : list) {
                        if (each.toLowerCase().contains(s.toString().toLowerCase())) {
                            filteredList.add(each);
                        }
                    }
                    //replace the array adapter with the filtered one
                    ca.setList(filteredList);
                    listView.setAdapter(ca);
                }
                else {
                    //if the text is empty, reload the whole list
                    ca.setList(list);
                    listView.setAdapter(ca);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }

        });

        findViewById(R.id.back_arrow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(intent);
                finish();
            }
        });





    }

   public class customAdapter extends BaseAdapter {

        private List<String> list_;

       @Override
        public int getCount() { return list_.size();}

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        public void setList(List<String> list) {
            this.list_ = list;
            notifyDataSetChanged();
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            //View view = getLayoutInflater().inflate(R.layout.task_tab, null);
            final View view = getLayoutInflater().inflate(R.layout.item_tab,null);
            TextView to = view.findViewById(R.id.to);
            TextView descript = view.findViewById(R.id.title_);
            TextView date = view.findViewById(R.id.date);
            to.setText(list.get(position));
            descript.setText(list_descript.get(position));
            date.setText(list_dates.get(position));

            view.findViewById(R.id.item_rl).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    descript.setHeight(descript.getHeight() + 1000);
                }
            });

            view.findViewById(R.id.item_rl).setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    d.getPosition(position);
                    d.show();
                    Log.e("","-=-=-=-=-=-=-=");
                    return false;
                }
            });
            view.findViewById(R.id.share).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!descript.getText().toString().equals("")) {
                        shareApp(getApplicationContext(), descript.getText().toString());
                    } else {
                        Toast.makeText(getApplicationContext(), "mail is Empty", Toast.LENGTH_LONG).show();
                    }
                }
            });
            return view;
        }

    }

    public ArrayList<String> getListFromLocal(String key){
        SharedPreferences prefs = getSharedPreferences("AppName", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = prefs.getString(key, null);
        Type type = new TypeToken<ArrayList<String>>() {}.getType();
        return gson.fromJson(json, type);

    }





    public class ConfirmDialog extends Dialog {

        int i;
        public void getPosition(Integer m){
            i = m;
        }

        public ConfirmDialog(Context context) {
            super(context);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.confirm_dialog);

            TextView confirmText = (TextView) findViewById(R.id.confirmText);
            confirmText.setText("Are you sure you want to proceed?");

            Button okButton = (Button) findViewById(R.id.okButton);
            okButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    //User clicked OK button
                    d.hide();
                    list.remove(i);
                    list_descript.remove(i);
                    list_dates.remove(i);
                    ca.notifyDataSetChanged();
                    saveListInLocal(list,"save_titles");
                    saveListInLocal(list_descript,"save_titledescript");
                    saveListInLocal(list_dates,"dates");
                    //Perform action here

                }
            });

            Button cancelButton = (Button) findViewById(R.id.cancelButton);
            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    //User clicked OK button
                    d.hide();
                    //Perform action here

                }
            });
        }
    }

    public void saveListInLocal(ArrayList<String> list, String key) {
        SharedPreferences prefs = getSharedPreferences("AppName", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(list);
        editor.putString(key, json);
        editor.apply();     // This line is IMPORTANT !!!

    }


}

//to preserve the layouts as they are as its as it is as they are