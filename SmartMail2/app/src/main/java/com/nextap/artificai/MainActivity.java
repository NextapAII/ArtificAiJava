package com.nextap.artificai;

import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.*;

public class MainActivity extends AppCompatActivity {
    private static final String CHANNEL_ID = "Artific AI";
    private static final int NOTIFICATION_ID = 1;
    GoogleSignInClient mGoogleSignInClient;
    ConfirmDialog d;
    Firebase mref;
    String msg;
    String recentversion; //example : 1.7
    Boolean demo=false;String user;
    String demo_user;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        Firebase.setAndroidContext(this);
        String root_fb_url = "https://artific-ai-default-rtdb.asia-southeast1.firebasedatabase.app/";
        mref = new Firebase(root_fb_url);
        mref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                msg = dataSnapshot.child("notification").getValue().toString();
                recentversion = dataSnapshot.child("version").getValue().toString();
                String user = readFromFile(MainActivity.this).trim().toLowerCase();
                String demo_user = dataSnapshot.child("demo-account").getValue().toString();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });


        //////
        //CallNotification();
        //NotificationUtils.showNotification(this, "Welcome to Artific AI", "Start making quick mails",
            //    R.drawable.app_logo, 1);
        PackageManager manager = this.getPackageManager();
        new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                sendNotification();
                mref = new Firebase(root_fb_url);
                mref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String user = readFromFile(MainActivity.this).trim().toLowerCase();
                        String demo_user = dataSnapshot.child("demo-account").getValue().toString();
                        PackageInfo info = null;
                        try {

                            info = manager.getPackageInfo(getApplicationContext().getPackageName(), PackageManager.GET_ACTIVITIES);
                            Log.e("VC",String.valueOf(info.versionCode));
                            if(info.versionCode!=Integer.parseInt(recentversion)
                                    || user.equals(demo_user)){
                                Intent i = new Intent(MainActivity.this, updatetheapp.class);
                                startActivity(i);finish();}

                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });
                return false;
            }
        }).sendEmptyMessageDelayed(0, 2000);
        //////

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_id))
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        final RelativeLayout button1 = findViewById(R.id.tab_1);
        final RelativeLayout button2 = findViewById(R.id.tab_2);
        final RelativeLayout button3 = findViewById(R.id.tab_3);
        final RelativeLayout button4 = findViewById(R.id.tab_4);


        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, CustomPage.class);
                i.putExtra("key1", R.id.tab_1);
                startActivity(i);
            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, CustomPage.class);
                i.putExtra("key2", R.id.tab_2);
                startActivity(i);
            }
        });
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, CustomPage.class);
                i.putExtra("key3", R.id.tab_3);
                startActivity(i);
            }
        });
        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, CustomPage.class);
                i.putExtra("key4", R.id.tab_4);
                startActivity(i);
            }
        });

        findViewById(R.id.favourite_textview).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, pileactivity.class);
                startActivity(i);
            }
        });

        findViewById(R.id.signout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d = new ConfirmDialog(MainActivity.this);
                d.show();
            }
        });


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


    public class ConfirmDialog extends Dialog {

        int i;

        public void getPosition(Integer m) {
            i = m;
        }

        public ConfirmDialog(Context context) {
            super(context);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.confirm_dialog);

            TextView confirmText = findViewById(R.id.confirmText);
            confirmText.setText("Are you sure you want to proceed?");

            Button okButton = findViewById(R.id.okButton);
            okButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    //User clicked OK button
                    signOut();
                    Toast.makeText(MainActivity.this, "Sign out", Toast.LENGTH_LONG).show();
                    //Perform action here
                    Intent myIntent = new Intent(getApplicationContext(), Registration.class);
                    startActivity(myIntent);
                    finish();

                }
            });

            Button cancelButton = findViewById(R.id.cancelButton);
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

    private void sendNotification() {
        createNotificationChannel();

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.real_company_logo)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.updated_app_logo))
                .setContentTitle("Artific AI")
                .setContentText(String.format("%s", msg))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(String.format("%s", msg)))
                .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "channel_01";
            String description = "channel_01";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

}