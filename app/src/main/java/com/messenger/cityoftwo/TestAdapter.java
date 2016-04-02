package com.messenger.cityoftwo;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

//import com.squareup.picasso.Picasso;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

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

        Test t = Tests.get(position);

        question.setText(t.getQuestion());

        AnswersAdapter a = new AnswersAdapter(context, t.getAnswers());

        answers_listview.setAdapter(a);

        answers_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                context.setAnswer(i, position);
                Toast.makeText(context, "Answer " + i + " selected", Toast.LENGTH_SHORT).show();
            }
        });

        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ((NonSwipeableViewPager) container).removeView((View) object);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }

    class AnswersAdapter extends ArrayAdapter<String> {

        Context context;
        ArrayList<AnswerPair> answers;

        public AnswersAdapter(Context context, ArrayList<AnswerPair> answers) {
            super(context, R.layout.layout_test_option);
            this.context = context;
            this.answers = answers;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null){
                LayoutInflater li = LayoutInflater.from(context);
                convertView = li.inflate(R.layout.layout_test_option, null);
            }

            ImageView Option = (ImageView) convertView.findViewById(R.id.option_image);
            TextView Description = (TextView) convertView.findViewById(R.id.option_description);

            Glide.with(context)
                    .load(answers.get(position).second)
                    .into(Option);

            Description.setText(answers.get(position).first);

            return convertView;
        }
    }
}
