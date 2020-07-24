package com.zjx.cardhelper;

import android.os.Bundle;
import android.widget.TextView;

import com.ryx.card_annotation.model.CardMeta;
import com.ryx.card_api.core.CardManager;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
      CardMeta cardMeta= CardManager.getInstance().getCardMetaByTypeName("abc");
        TextView textView=findViewById(R.id.text);
        textView.setText(cardMeta.getTypeName());
    }
}