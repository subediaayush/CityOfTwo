package com.messenger.cityoftwo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.Profile;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Aayush on 2/5/2016.
 */
public class LobbyActivity extends AppCompatActivity {

    HttpHandler httpHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        AccessToken accessToken;
        Profile profile = null;
        try{
            accessToken = getIntent().getParcelableExtra(TesseraApplication.ACCESS_TOKEN);
            profile = getIntent().getParcelableExtra(TesseraApplication.PROFILE);

            if (accessToken == null || profile == null) throw new NullPointerException("");
        } catch (Exception e){
            setResult(RESULT_CANCELED, new Intent());
            finish();
            return;
        }

        CircleImageView ProfileImageView = (CircleImageView) findViewById(R.id.lobby_profile_image);
        TextView ProfileTextView = (TextView) findViewById(R.id.lobby_profile_name);
        ProgressBar LobbyProgressBar = (ProgressBar) findViewById(R.id.lobby_progressbar);
        TextView LobbyDescription = (TextView) findViewById(R.id.lobby_progress_description);

        int width = ProfileImageView.getLayoutParams().width,
                height = ProfileImageView.getLayoutParams().height;

        Uri uri = profile.getProfilePictureUri(width, height);

        Picasso.with(this)
                .load(uri)
                .centerCrop()
                .placeholder(R.drawable.user_placeholder)
                .error(R.drawable.user_placeholder)
                .resize(100, 100)
                .into(ProfileImageView);

        ProfileTextView.setText(profile.getName());

        httpHandler = new HttpHandler() {
            @Override
            protected void onPreRun() {

            }

            @Override
            protected void onSuccess(String response) {

            }

            @Override
            protected void onFailure(Integer status) {

            }
        };

        startActivityForResult(new Intent(this, TestActivity.class), TesseraApplication.TEST);
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED, new Intent());
        finish();
    }

}
