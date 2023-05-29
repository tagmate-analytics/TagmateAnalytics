package com.tagmate;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.appcompat.widget.AppCompatButton;

import com.google.firebase.analytics.FirebaseAnalytics;

public class CustomButton extends AppCompatButton implements View.OnClickListener {

    private FirebaseAnalytics mFirebaseAnalytics;

    private CustomOnClickListener event;
    private Context mContext;

    public CustomButton(Context context) {
        super(context);
    }

    public CustomButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(mContext);
        setOnClickListener(this);
    }

    public CustomButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    public void onClick(View v) {
        String buttonText = getText().toString();
        Log.d("MyCustomButton", "Button text: " + buttonText);

        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "123");
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "btn_click");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "btn");
        mFirebaseAnalytics.logEvent(buttonText, bundle);

        event.onClick(v);
    }

    public void setCustomClickListener(CustomOnClickListener event) {
        this.event = event;
    }

    public interface CustomOnClickListener {
        public void onClick(View var1);
    }
}