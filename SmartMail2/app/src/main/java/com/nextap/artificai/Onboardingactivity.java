package com.nextap.artificai;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

public class Onboardingactivity extends Activity {
    private ViewPager viewPager;
    private ViewPagerAdapter viewPagerAdapter;
    private Button button;
    private Button skipbutton;
    String[] titles = {"Welcome to SmartMail","Auto AI","Mail Easy"};
    String[] descs = {"SmartMail is a product developed by Nextap.org.",
            "Create Super Fast in seconds, Smartest Mail creating App on the Playstore",
            "Mails can be for your boss or your long met friend, SmartMail got you all covered!"};

    Integer[] imgs = {R.drawable.intro_1,R.drawable.intro_2,R.drawable.intro_3};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.onboardingactivity);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_id))
                .requestEmail()
                .build();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

        if(account!=null){Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(myIntent);
            finish();}
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPagerAdapter = new ViewPagerAdapter(Onboardingactivity.this);
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.setOffscreenPageLimit(2);
        button =  findViewById(R.id.button12);
        skipbutton = findViewById(R.id.skip_button);
        skipbutton.setVisibility(View.GONE);
        Intent intent = new Intent(this, Registration.class);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
                if(button.getText().equals("Done")){startActivity(intent);finish();}
                if(viewPager.getCurrentItem()==2){
                    button.setText("Done");
                    skipbutton.setVisibility(View.INVISIBLE);
                }
            }
        });
        skipbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
                startActivity(intent);finish();
            }
        });
    }

    class ViewPagerAdapter extends PagerAdapter {
        private LayoutInflater layoutInflater;

        public ViewPagerAdapter(Onboardingactivity mainActivity) {
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = layoutInflater.inflate(R.layout.page, null);
            TextView title = (TextView) view.findViewById(R.id.title);
            TextView desc = (TextView) view.findViewById(R.id.desc);
            ImageView imageView = (ImageView) view.findViewById(R.id.imageView2);
            imageView.setImageResource(imgs[position]);
            //title.setText(titles[position]);
            //desc.setText(descs[position]);
            container.addView(view);
            return view;
        }

        @Override
        public int getCount() {
            return titles.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }
}