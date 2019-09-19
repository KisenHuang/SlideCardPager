package com.tal.kisen.slidecardpager;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.kisen.slidecard.SlideCardPager;
import com.kisen.slidecard.transforms.BoomTransforms;
import com.kisen.slidecard.transforms.FanPageTransforms;
import com.kisen.slidecard.transforms.ZoomTransforms;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private SlideCardPager touchCardView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        touchCardView = findViewById(R.id.touch_view);
        touchCardView.setTransforms(new ZoomTransforms());
        SlideCardPager.CardAdapter<CardData> adapter = new SlideCardPager.CardAdapter<CardData>(R.layout.item_card, createList()) {
            @Override
            protected void convert(SlideCardPager.CardHolder cardView, CardData data) {
                View card = cardView.getContentView();
                TextView title = card.findViewById(R.id.title);
                TextView detail = card.findViewById(R.id.detail);
                title.setText(data.name);
                detail.setText(data.detail);
            }
        };
        touchCardView.setAdapter(adapter);
        adapter.setOnItemClickListener(new SlideCardPager.OnItemClickListener<CardData>() {
            @Override
            public void onItemClick(SlideCardPager.CardAdapter<CardData> adapter, View view, int state, int position) {
                CardData itemData = adapter.getItemData(position);
                Log.e("MainActivity", itemData != null?itemData.toString():"null");
            }
        });
    }

    private List<CardData> createList() {
        List<CardData> list = new ArrayList<>();
        for (int i = 1; i < 11; i++) {
            list.add(new CardData("Card" + i, "Detail"));
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

        @NonNull
        @Override
        public String toString() {
            return "{ name=" + name + " detail=" + detail + "}";
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_type1:
                touchCardView.setTransforms(new ZoomTransforms());
                break;
            case R.id.action_type2:
                touchCardView.setTransforms(new BoomTransforms());
                break;
            case R.id.action_type3:
                touchCardView.setTransforms(new FanPageTransforms());
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
