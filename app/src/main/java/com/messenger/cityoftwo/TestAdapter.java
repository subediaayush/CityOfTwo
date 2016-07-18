package com.messenger.cityoftwo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

//import com.squareup.picasso.Picasso;

/**
 * Created by Aayush on 2/6/2016.
 */
public class TestAdapter extends PagerAdapter {

    Context context;
    ArrayList<Test> Tests;

    StringBuilder answer;

    public TestAdapter(Context context) {
        this(context, new ArrayList<Test>());

    }

    public TestAdapter(Context context, ArrayList<Test> tests) {
        this.context = context;
        Tests = tests;
        answer = new StringBuilder();
    }

    @Override
    public int getCount() {
        return Tests.size();
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.layout_test, container, false);

        TextView question = (TextView) view.findViewById(R.id.test_question);
        final ListView answers_listview = (ListView) view.findViewById(R.id.test_answers);

        Test t = Tests.get(position);

        question.setText(t.getQuestion());

        AnswersAdapter a = new AnswersAdapter(context, t.getAnswers(), position);

        answers_listview.setAdapter(a);

        answers_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (answer.length() - 1 > position) answer.append(String.valueOf(i));
                else answer.replace(position, position + 1, String.valueOf(i));

                LocalBroadcastManager b = LocalBroadcastManager.getInstance(context);
                Intent intent = new Intent();
                intent.setAction(CityOfTwo.KEY_TEST);

                if (position == Tests.size() - 1)
                    intent.putExtra(CityOfTwo.KEY_SELECTED_ANSWER, answer.toString());
                else
                    intent.putExtra(CityOfTwo.KEY_CURRENT_ANSWER, position);

                b.sendBroadcast(intent);
            }
        });

        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }


}
