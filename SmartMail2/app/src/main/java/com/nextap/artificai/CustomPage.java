package com.nextap.artificai;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.ads.*;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.android.play.core.tasks.Task;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.lang.reflect.Type;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class CustomPage extends AppCompatActivity {
    static String APIKEY;
    private final String TAG = "MainActivity";
    public Firebase Mref2;
    public boolean save_stat = false;
    Integer tokens = 1;
    Firebase Mref;
    ArrayList<String> title = new ArrayList<>();
    ArrayList<String> titledescript = new ArrayList<>();
    ArrayList<String> dates = new ArrayList<>();
    EditText Subject;
    EditText To;
    String From_main;
    String To_main;
    String sentiment;
    String Output_str;
    EditText OutputBox;
    ImageButton emotebutton;
    boolean access;
    boolean ad;
    Integer totaltokens;
    Integer[] emotes = {R.drawable.emote1, R.drawable.emote2, R.drawable.emote3};
    String ENGINE_NAME;
    Integer tokenreward;
    //String Rewarded_ads_testkey = "ca-app-pub-3940256099942544/5224354917";
    String Rewarded_ads_testkey = "ca-app-pub-3510244847912471/4796493195";
    private RewardedAd mRewardedAd;
    Boolean full=false;
    Boolean firsttime = false;

    public static void shareApp(Context context, String OutputBoxtext) {
        final String appPackageName = context.getPackageName();
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, OutputBoxtext);
        sendIntent.setType("text/plain");
        context.startActivity(sendIntent);
    }

    private String sendPost(String iinput, Float temperature) throws Exception {
        // create a client
        String url = String.format("https://api.openai.com/v1/engines/%s/completions", ENGINE_NAME);
        URL obj = new URL(url);
        HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();


        //add reuqest header
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Authorization", String.format("Bearer %s", APIKEY));

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("prompt", String.format("%s", iinput));
        jsonObject.put("max_tokens", 120);
        jsonObject.put("temperature", temperature);
        jsonObject.put("top_p", 1);
        jsonObject.put("n", 1);
        jsonObject.put("stream", false);
        jsonObject.put("logprobs", null);
        jsonObject.put("stop", "####");

        String urlParameters = jsonObject.toString().replaceAll("\n", "\\\n");

        // Send post request
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(urlParameters);
        wr.flush();
        wr.close();

        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'POST' request to URL : " + url);
        System.out.println("Post parameters : " + urlParameters);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        JSONObject json = new JSONObject(response.toString());
        String text = json.getJSONArray("choices").getJSONObject(0).getString("text");
        System.out.println("Response Text : " + text);
        System.out.println("Response info : " + response.toString());

        //print result
        System.out.println(text);

        boolean Success = false;
        try {
            String[] substring = text.split("&&&&");
            Output_str = substring[0];

            Log.e("-ERROR!))", substring[1]);
            sentiment = substring[1];
            Success = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (Success) {
            Log.e("TAG", text);
            return text.replaceAll("\\\\n", "\n");
        } else {
            tokens += 420;
            Mref2.setValue(tokens);
            totaltokens += 420;
            Mref.child("total-tokens").setValue(totaltokens);
            return sendPost(iinput, 0.8f);
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_page_updatev1);
        getSupportActionBar().hide();
        Firebase.setAndroidContext(this);



        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });



        Mref = new Firebase("https://artific-ai-default-rtdb.asia-southeast1.firebasedatabase.app/");
        String user = readFromFile(this).trim().toLowerCase();
        Mref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                APIKEY = dataSnapshot.child("api-key").getValue().toString();
                Log.e("APIKEY :", APIKEY);
                ENGINE_NAME = dataSnapshot.child("Server").getValue().toString();
                access = Boolean.parseBoolean(dataSnapshot.child(String.format("Users/%s/access", user)).getValue().toString());
                ad = Boolean.parseBoolean(dataSnapshot.child(String.format("Users/%s/ad", user)).getValue().toString());

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                // yourMethod();
                if (ad){LoadRewardedVideoAd();}
            }
        }, 3000);

        Mref2 = new Firebase(String.format("https://artific-ai-default-rtdb.asia-southeast1.firebasedatabase.app/Users/%s/TokenUsage", user));


        Subject = findViewById(R.id.subject);
        To = findViewById(R.id.TO);
        OutputBox = findViewById(R.id.outputbox_text);
        emotebutton = findViewById(R.id.emote);
        final Intent intent = new Intent(this, MainActivity.class);
        if (getListFromLocal("dates") != null) {
            title = getListFromLocal("save_titles");
            titledescript = getListFromLocal("save_titledescript");
            dates = getListFromLocal("dates");
        }
        OutputBox.setHint("Mail be shown here...");


        OutputBox.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                view.getParent().requestDisallowInterceptTouchEvent(true);
                switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_SCROLL:
                        view.getParent().requestDisallowInterceptTouchEvent(false);
                        return true;
                    case MotionEvent.ACTION_BUTTON_PRESS:

                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

                        imm.showSoftInput(OutputBox, InputMethodManager.SHOW_IMPLICIT);
                }
                return false;
            }
        });

        findViewById(R.id.back_arrow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(intent);
                finish();
            }
        });
        Mref2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                tokens = Integer.parseInt(dataSnapshot.getValue().toString());
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
        findViewById(R.id.create_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //OutputBox.setText("Try Regenerating.....:)");
                Boolean CanComplete=true;
                String obt = OutputBox.getText().toString();
                try{
                    if(!String.valueOf((obt.charAt(obt.length()-4))).equals("!")){CanComplete=true;}
                    else{CanComplete=false;}
                }catch (Exception e){e.printStackTrace();}


                if (access) {
                    if(CanComplete)
                    {
                        if (!Subject.getText().toString().equals("") && !To.getText().toString().equals("")) {
                            if (ad) {
                                //Calling a rewarded video-ad
                                CallRewardedVideoAd();
                            } else {
                                {
                                    CustomLoadingDialog dialog = CustomLoadingDialog.getInstance();
                                    dialog.ShowProgress(CustomPage.this, "Generating...", false);
                                    Thread thread = new Thread(new Runnable() {
                                        final String PromptInput = Prompt_request(Subject.getText().toString(), To.getText().toString(),OutputBox.getText().toString());

                                        @Override
                                        public void run() {
                                            try {
                                                Log.e("Calling...", "OPEN_AI");
                                                if (access) {
                                                    String tag = ResponseVerification(Subject.getText().toString() + To.getText().toString());
                                                    if (tag.equals("2")) {
                                                        Toast.makeText(CustomPage.this, "Subject/Keywords Inappropriate", Toast.LENGTH_LONG).show();
                                                    } else {
                                                        sendPost(PromptInput, 0.6f);
                                                    }
                                                    Log.e("output_label", tag);

                                                }

                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });

                                    thread.start();
                                    Mref.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            totaltokens = Integer.parseInt(dataSnapshot.child("total-tokens").getValue().toString());
                                        }

                                        @Override
                                        public void onCancelled(FirebaseError firebaseError) {

                                        }
                                    });
                                    Handler handler = new Handler();
                                    handler.postDelayed(new Runnable() {
                                        public void run() {
                                            // yourMethod();
                                            try {
                                                OutputBox.setText(OutputBox.getText().toString()+Output_str+"!!!!");
                                                sentiment = sentiment.replaceAll(" ", "").replaceAll("Mail", "").replaceAll("Sentiment:", "").replaceAll("&", "");
                                                Log.e("Sentiment button: ", sentiment);
                                                if (sentiment.equals("Positive")) {
                                                    emotebutton.setImageDrawable(getDrawable(R.drawable.emote1));
                                                } else if (sentiment.equals("Negative")) {
                                                    emotebutton.setImageDrawable(getDrawable(R.drawable.emote2));
                                                } else if (sentiment.equals("Neutral")) {
                                                    emotebutton.setImageDrawable(getDrawable(R.drawable.emote3));
                                                }
                                                Log.e(">||>>", OutputBox.getText().toString());
                                                //ImageView save = findViewById(R.id.imageView);
                                                //save.setImageDrawable(getDrawable(R.drawable.ic_baseline_favorite_border_24));
                                                save_stat = false;
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            dialog.hideProgress();
                                            tokens += 420;
                                            totaltokens += 420;
                                            Mref.child("total-tokens").setValue(totaltokens);
                                            Mref2.setValue(tokens);

                                        }
                                    }, 8000);   //5 seconds
                                }
                                if(tokens<800){firsttime=true;}
                                //getreview(firsttime);
                            }

                        } else {
                            Toast.makeText(CustomPage.this, "Subject/Something related can't be empty", 5).show();
                        }
                    }else {
                        Toast.makeText(CustomPage.this, "Refresh or Delete some words in mail body", 3).show();}


                } else {
                    Toast.makeText(CustomPage.this, "Access Denied, Contact Admin", 5).show();
                }



            }
        });

        findViewById(R.id.imageView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //ImageView save = findViewById(R.id.imageView);
                if (!save_stat) {
                    //save.setImageDrawable(getDrawable(R.drawable.ic_baseline_favorite_24));
                    title.add(Subject.getText().toString());
                    titledescript.add(OutputBox.getText().toString());
                    dates.add(SystemDate());
                    save_stat = true;
                    Toast.makeText(CustomPage.this,"Drafted",Toast.LENGTH_LONG).show();
                } else {
                    //save.setImageDrawable(getDrawable(R.drawable.ic_baseline_favorite_border_24));
                    save_stat = false;
                    title.remove(Subject.getText().toString());
                    titledescript.remove(OutputBox.getText().toString());
                    Toast.makeText(CustomPage.this,"Undrafted",Toast.LENGTH_LONG).show();
                }
                //when the button is clicked add the to subject text to titledescript list and edittext edittext text to title list

                //save both lists in internal storage of device
                saveListInLocal(title, "save_titles");
                saveListInLocal(titledescript, "save_titledescript");
                saveListInLocal(dates, "dates");
            }
        });

        EditText From_str = findViewById(R.id.To_edit);
        EditText To_str = findViewById(R.id.To_edit2);
        findViewById(R.id.click).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                From_main = From_str.getText().toString();
                findViewById(R.id.from_lay).setVisibility(View.GONE);
                findViewById(R.id.to_lay).setVisibility(View.VISIBLE);
            }
        });

        findViewById(R.id.click2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                To_main = To_str.getText().toString();
                findViewById(R.id.to_lay).setVisibility(View.GONE);
                findViewById(R.id.rl_full).setVisibility(View.VISIBLE);
                InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
            }
        });

        findViewById(R.id.share).setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
        if (!OutputBox.getText().toString().equals("")) {
        shareApp(CustomPage.this, OutputBox.getText().toString());
        } else {
        Toast.makeText(CustomPage.this, "mail is Empty", Toast.LENGTH_LONG).show();
        }
        }
        });
         findViewById(R.id.copy).setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
        if (!OutputBox.getText().toString().equals("")) {
        setClipboard(CustomPage.this, OutputBox.getText().toString());
        Toast.makeText(CustomPage.this, "Copyied to clipboard", Toast.LENGTH_LONG).show();
        } else {
        Toast.makeText(CustomPage.this, "mail is Empty", Toast.LENGTH_LONG).show();
        }
        }
        });

         findViewById(R.id.refresh).setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 OutputBox.setText("");Toast.makeText(CustomPage.this, "Mail body cleaned", Toast.LENGTH_LONG).show();
             }
         });
    }

    public static List<Character>
    convertStringToCharList(String str)
    {

        // Create an empty List of character
        List<Character> chars = new ArrayList<>();

        // For each character in the String
        // add it to the List
        for (char ch : str.toCharArray()) {

            chars.add(ch);
        }

        // return the List
        return chars;
    }

    public void Typewritereffect(String input){

        for(Character c : convertStringToCharList(input)){

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    // yourMethod();
                    OutputBox.append(c.toString());
                }
            }, 550);
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

    public String SystemDate() {
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Date date = new Date();
        return dateFormat.format(date);
    }

    private String readFromFile(Context context) {

        String ret = "";

        try {
            InputStream inputStream = context.openFileInput("config.txt");

            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ((receiveString = bufferedReader.readLine()) != null) {
                    stringBuilder.append("\n").append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        } catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return ret;
    }

    Integer CalculateTokens(String s) {
        return tokens;
    }

    public String Prompt_request(String sub, String keywords, String Outputboxtext) {
        String mail_Request;
        {
            mail_Request = "This is a mail generator\n" +
                    "1.Subject: Vacation request for September, 10-15\n" +
                    "keywords: Current Projects & Pending Tasks, Vacation Request\n" +
                    "\n" +
                    String.format("Dear %s,\n", To_main) +
                    "\n" +
                    "I would like to request vacation from Monday, September 9th till Friday, September 13th.\n" +
                    "I will make sure to complete all my current projects and pending tasks in advance before the vacation. Ravi and John will cover my responsibilities during my absence.\n" +
                    "\n" +
                    "\n" +
                    "Looking forward to your approval.\n" +
                    "\n" +
                    "Sincerely,\n" +
                    String.format("[%s]\n", From_main) +
                    "[Job title]\n" +
                    "&&&&Mail Sentiment: Neutral&&&&\n" +
                    "####\n" +
                    "\n" +
                    "2.\n" +
                    "Subject: Do you have student discounts for the Annual Coding Conference?\n" +
                    "keywords: discount ,education,student ,coding,annual\n" +
                    "\n" +
                    String.format("Greetings,%s\n", To_main) +
                    "\n" +
                    "I would like to ask if you provide student discounts for tickets to the Annual Coding Conference.\n" +
                    "A full-time student at the University of Texas and very excited about your event, but unfortunately, the ticket price is too high for me. I would appreciate if you could offer me an educational discount.\n" +
                    "Looking forward to hearing from you!\n" +
                    "\n" +
                    "Best,\n" +
                    String.format("[%s]\n", From_main) +
                    "&&&&Mail Sentiment: Negative&&&&\n" +
                    "####\n" +
                    "\n" +
                    String.format("3.Subject: %s\n", sub) +
                    String.format("keywords: %s,\n\n", keywords) +
                    Outputboxtext;
        }
        return mail_Request;
    }

    private String ResponseVerification(String iinput) throws Exception {
        // create a client
        String url = String.format("https://api.openai.com/v1/engines/content-filter-alpha-c4/completions", ENGINE_NAME);
        URL obj = new URL(url);
        HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
        //add reuqest header
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Authorization", String.format("Bearer %s", APIKEY));

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("prompt", String.format("<|endoftext|>%s\\\n--\\\nLabel:", iinput));
        jsonObject.put("max_tokens", 1);
        jsonObject.put("temperature", 1);
        jsonObject.put("top_p", 0);
        jsonObject.put("logprobs", 10);
        String urlParameters = jsonObject.toString().replaceAll("\n", "\\\n");

        // Send post request
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(urlParameters);
        wr.flush();
        wr.close();

        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'POST' request to URL : " + url);
        System.out.println("Post parameters : " + urlParameters);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        JSONObject json = new JSONObject(response.toString());
        String output_label = json.getJSONArray("choices").getJSONObject(0).getString("text");
        Float toxic_threshold = -0.355f;

        if (output_label.equals("2")) {
            // If the model returns "2", return its confidence in 2 or other output-labels
            JSONObject logprobs = json.getJSONArray("choices").getJSONObject(0).getJSONObject("logprobs").getJSONArray("top_logprobs").getJSONObject(0);

            // If the model is not sufficiently confident in "2",
            // choose the most probable of "0" or "1"
            // Guaranteed to have a confidence for 2 since this was the selected token.
            if (logprobs.getDouble("2") < toxic_threshold) {
                Double logprob_0 = logprobs.getDouble("0");
                Double logprob_1 = logprobs.getDouble("1");

                // If both "0" and "1" have probabilities, set the output label
                // to whichever is most probable
                if (logprob_0 != null && logprob_1 != null) {
                    if (logprob_0 >= logprob_1) {
                        output_label = "0";
                    } else {
                        output_label = "1";
                    }
                }
                // If only one of them is found, set output label to that one
                else if (logprob_0 != null) {
                    output_label = "0";
                } else if (logprob_1 != null) {
                    output_label = "1";
                }

                // If neither "0" or "1" are available, stick with "2"
                // by leaving output_label unchanged.
            }
        }

        // if the most probable token is none of "0", "1", or "2"
        // this should be set as unsafe
        if (output_label.equals("2")) {
            output_label = "2";
        }

        return output_label;
    }

    public ArrayList<String> getListFromLocal(String key) {
        SharedPreferences prefs = getSharedPreferences("AppName", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = prefs.getString(key, null);
        Type type = new TypeToken<ArrayList<String>>() {
        }.getType();
        return gson.fromJson(json, type);

    }

    public void CallRewardedVideoAd() {
        try {
            if (mRewardedAd != null) {
                mRewardedAd.show(CustomPage.this, new OnUserEarnedRewardListener() {
                    @Override
                    public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                        // Handle the reward.
                        Log.d(TAG, "The user earned the reward.");
                        tokenreward = rewardItem.getAmount();
                        {
                            CustomLoadingDialog dialog = CustomLoadingDialog.getInstance();
                            dialog.ShowProgress(CustomPage.this, "Generating..", false);
                            Thread thread = new Thread(new Runnable() {
                                final String PromptInput = Prompt_request(Subject.getText().toString(), To.getText().toString(),OutputBox.getText().toString());

                                @Override
                                public void run() {
                                    try {
                                        Log.e("Calling...", "OPEN_AI");
                                        if (access) {
                                            String tag = ResponseVerification(Subject.getText().toString() + To.getText().toString());
                                            if (tag.equals("2")) {
                                                Toast.makeText(CustomPage.this, "Subject/Keywords Inappropriate", Toast.LENGTH_LONG).show();
                                            } else {
                                                sendPost(PromptInput, 0.6f);
                                            }
                                            Log.e("output_label", tag);

                                        }

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });

                            thread.start();
                            Mref.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    totaltokens = Integer.parseInt(dataSnapshot.child("total-tokens").getValue().toString());
                                }

                                @Override
                                public void onCancelled(FirebaseError firebaseError) {

                                }
                            });
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                public void run() {
                                    // yourMethod();
                                    try {
                                        OutputBox.setText(Output_str);

                                        sentiment = sentiment.replaceAll(" ", "").replaceAll("Mail", "").replaceAll("Sentiment:", "").replaceAll("&", "");
                                        Log.e("Sentiment button: ", sentiment);
                                        if (sentiment.equals("Positive")) {
                                            emotebutton.setImageDrawable(getDrawable(R.drawable.emote1));
                                        } else if (sentiment.equals("Negative")) {
                                            emotebutton.setImageDrawable(getDrawable(R.drawable.emote2));
                                        } else if (sentiment.equals("Neutral")) {
                                            emotebutton.setImageDrawable(getDrawable(R.drawable.emote3));
                                        }
                                        Log.e(">||>>", OutputBox.getText().toString());
                                        ImageView save = findViewById(R.id.imageView);
                                        save.setImageDrawable(getDrawable(R.drawable.ic_baseline_favorite_border_24));
                                        save_stat = false;
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    dialog.hideProgress();
                                    tokens += 420;
                                    totaltokens += 420;
                                    Mref.child("total-tokens").setValue(totaltokens);
                                    Mref2.setValue(tokens);

                                }
                            }, 8000);   //5 seconds
                        }
                        mRewardedAd = null;
                        LoadRewardedVideoAd();
                    }
                });
            } else {
                Log.d(TAG, "The rewarded ad wasn't ready yet.");
            }
            mRewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdShowedFullScreenContent() {
                    // Called when ad is shown.
                    Log.e(TAG, "Ad was shown.");
                }

                @Override
                public void onAdFailedToShowFullScreenContent(AdError adError) {
                    // Called when ad fails to show.
                    Log.e(TAG, "Ad failed to show.");
                }

                @Override
                public void onAdDismissedFullScreenContent() {
                    // Called when ad is dismissed.
                    // Set the ad reference to null so you don't show the ad a second time.
                    Log.e(TAG, "Ad was dismissed.");
                    mRewardedAd = null;
                    LoadRewardedVideoAd();

                }
            });
        } catch (Exception e) {
            Log.e("IS", "The ad failed to show.");
            e.printStackTrace();
        }
    }

    public void LoadRewardedVideoAd() {
        AdRequest adRequest = new AdRequest.Builder().build();

        RewardedAd.load(CustomPage.this, Rewarded_ads_testkey,
                adRequest, new RewardedAdLoadCallback() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error.
                        Log.d(TAG, loadAdError.getMessage());
                        mRewardedAd = null;
                        Toast.makeText(getApplicationContext(), "Try again after some time", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                        mRewardedAd = rewardedAd;
                        Log.e(TAG, "Ad was loaded.");
                    }
                });


    }

    private void setClipboard(Context context, String text) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(text);
        } else {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", text);
            clipboard.setPrimaryClip(clip);
        }
    }

    public void getreview(Boolean first){
        ReviewManager reviewManager = ReviewManagerFactory.create(this);
        if (!first){
            ReviewManager manager = ReviewManagerFactory.create(this);
            Task<ReviewInfo> request = manager.requestReviewFlow();
            request.addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // We can get the ReviewInfo object
                    ReviewInfo reviewInfo = task.getResult();

                } else {
                    // There was some problem, log or handle the error code.
                }
            });



        }
    }
}