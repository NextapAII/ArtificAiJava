package com.nextap.artificai;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.*;
import java.util.ArrayList;

public class Registration extends AppCompatActivity {
    private static final int RC_SIGN_IN = 123;
    Firebase mref;
    String root_fb_url = "https://artific-ai-default-rtdb.asia-southeast1.firebasedatabase.app";
    String User_id = "user-1";
    Boolean first = true;
    String Email_native;
    Integer Subscription_tokens = 0;
    boolean Auth = false;
    GoogleSignInClient mGoogleSignInClient;
    public Boolean found=false;
    ArrayList<String> user_list = new ArrayList<>();

    private String readFromFile(Context context) {

        String ret = "";

        try {
            InputStream inputStream = context.openFileInput("config.txt");

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append("\n").append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return ret;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        getSupportActionBar().hide();
        Firebase.setAndroidContext(this);
        mref = new Firebase(root_fb_url);

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        preSignIn();

        findViewById(R.id.pp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(getApplicationContext(), privacypolicy.class);
                startActivity(myIntent);
            }
        });

    }

    public void preSignIn(){
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_id))
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        //signOut();

        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

        try{
            SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
            String mail = sharedPref.getString(getString(R.string.startmain1),null);
            //sharedPref.edit().remove(getString(R.string.startmain1));
            sharedPref.edit().commit();
            Log.e(">>>",mail );
            //mail = "-=";
            if(mail != null){updateUI(account,false);
                Log.e("","Not First" );}
        }
        catch (Exception e){
            Log.e("Error : ","Trying google account");e.printStackTrace();
        }


        // Set the dimensions of the sign-in button.
        SignInButton signInButton = findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);


        findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.sign_in_button:
                        signIn();
                        break;
                    // ...
                }
            }
        });
    }

    private void writeToFile(String data,Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("config.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }





    private void updateUI(GoogleSignInAccount account,boolean first_) {


        ArrayList<String> CheckList = new ArrayList<>();
        String[] temp = String.valueOf(account.getEmail()).split("@");
        Log.e("General Message","Entering theUIUPDATE" );
        User_id = temp[0].replaceAll("\\.","");
        //Log.e("General Message","Entering theUIUPDATE" );

        int timeload = 1000;
        if(first_) timeload = 5000;

        getData(account.getEmail());
        CustomLoadingDialog dialog = CustomLoadingDialog.getInstance();
        dialog.ShowProgress(Registration.this,"Registering...",false);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                // yourMethod();

                Log.e("Err",String.valueOf(user_list.size()));
                Log.e("Found",String.valueOf(found));
                if(!found && first_){
                    Firebase mreff = new Firebase(root_fb_url+ String.format("/Users/%s/",User_id));
                    mreff.child("Name").setValue(account.getDisplayName());
                    mreff.child("TokenUsage").setValue(Subscription_tokens);
                    mreff.child("Email-id").setValue(account.getEmail());
                    mreff.child("access").setValue("true");
                    mreff.child("ad").setValue("false");
                    Log.e("","New User setup....Success");

                }
                writeToFile(User_id,getApplicationContext());
                Intent myIntent;
                if(first_){myIntent = new Intent(getApplicationContext(), MainActivity.class);}
                else{myIntent = new Intent(getApplicationContext(), MainActivity.class);}

                Log.e("Logged in",User_id);
                SharedPreferences sharedPref = Registration.this.getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString(getString(R.string.startmain1), "Ashwin");
                editor.apply();
                //overridePendingTransition(R.anim.fade_in,R.anim.slide_down);
                dialog.hideProgress();
                startActivity(myIntent);
                finish();




            }
        }, timeload);






    }

    public void getData(String m){

        Log.e("General Message","Entering theUIUPDATE" );
        mref.child("/Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    if((postSnapshot.getKey().trim().equals(User_id.trim()))){
                        found=true;

                        Email_native = (String) dataSnapshot.child(String.format("/%s/Email-id", User_id)).getValue();
                        System.out.println(User_id);

                        if(Email_native.equals(m)){
                            Auth = true;
                        }
                        break;
                    }

                    Log.e("",postSnapshot.getKey());
                }
            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            GoogleSignInAccount act = GoogleSignIn.getLastSignedInAccount(this);
            Log.e("","Name process...");
            Log.e("",account.getDisplayName());

            if (act!=null){

                Log.e("","Name process...");
                Log.e("",account.getDisplayName());
                updateUI(account,true);
            }


            // Signed in successfully, show authenticated UI.

        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.e("Error failure", "signInResult:failed code=" + e.getStatusCode());
            e.printStackTrace();
        }
    }

    protected void setGooglePlusButtonText(SignInButton signInButton, String buttonText) {
        // Find the TextView that is inside of the SignInButton and set its text
        for (int i = 0; i < signInButton.getChildCount(); i++) {
            View v = signInButton.getChildAt(i);

            if (v instanceof TextView) {
                TextView tv = (TextView) v;
                tv.setText(buttonText);
                return;
            }
        }
    }

    private void signOut() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        // ...
                    }
                });
    }
}