package com.tal.kisen.slidecardpager;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private SlideCardPager touchCardView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        touchCardView = findViewById(R.id.touch_view);
        touchCardView.setAdapter(new SlideCardPager.CardAdapter<CardData>(R.layout.item_card, createList()) {
            @Override
            protected void convert(SlideCardPager.CardHolder cardView, CardData data) {
                View card = cardView.getContentView();
                TextView title = card.findViewById(R.id.title);
                TextView detail = card.findViewById(R.id.detail);
                title.setText(data.name);
                detail.setText(data.detail);
            }
        });
    }

    private List<CardData> createList() {
        List<CardData> list = new ArrayList<>();
        for (int i = 1; i < 11; i++) {
            list.add(new CardData(i + "Card" + i, "Detail" + i));
        }
        return list;
    }

    public void preClick(View view) {
        touchCardView.pre();
    }

    public void nextClick(View view) {
        touchCardView.next();
    }


    private class CardData {
        public String name;
        public String detail;

        CardData(String name, String detail) {
            this.name = name;
            this.detail = detail;
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Log.e("MainActivity", "dispatch"+ev.getAction());
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.e("MainActivity", "onTouchEvent"+event.getAction());
        return super.onTouchEvent(event);
    }
}
