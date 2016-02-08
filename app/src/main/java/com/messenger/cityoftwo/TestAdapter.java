package com.messenger.cityoftwo;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Aayush on 2/6/2016.
 */
public class TestAdapter extends PagerAdapter {

    TestActivity context;
    ArrayList<Test> Tests;

    public TestAdapter(TestActivity context) {
        this.context = context;
        Tests = new ArrayList<>();
    }

    public TestAdapter(TestActivity context, ArrayList<Test> tests) {
        this.context = context;
        Tests = tests;
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
        ListView answers_listview = (ListView) view.findViewById(R.id.test_answers);

        AnswersAdapter a = new AnswersAdapter(context, Tests.get(position).getAnswers());
        answers_listview.setAdapter(a);

        answers_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                context.setAnswer(i, position);
            }
        });

        container.addView(view);
        return view;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return false;
    }

    class AnswersAdapter extends ArrayAdapter<String> {

        Context context;
        List<String> answers;

        public AnswersAdapter(Context context, List<String> answers) {
            super(context, R.layout.layout_test_option, answers);

            this.context = context;
            this.answers = answers;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null){
                LayoutInflater li = LayoutInflater.from(context);
                convertView = li.inflate(R.layout.layout_test_option, null);
            }

            ImageButton Option = (ImageButton) convertView.findViewById(R.id.option_image);

            Picasso.with(context)
                    .load(answers.get(position))
                    .into(Option);

            return convertView;
        }
    }
}
