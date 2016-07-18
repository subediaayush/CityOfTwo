package com.messenger.cityoftwo;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Aayush on 7/18/2016.
 */
class AnswersAdapter extends BaseAdapter {

    Context context;
    ArrayList<AnswerPair> answers;
    int currentQuestion;

    OnSelectedListener onSelectedListener;

    public AnswersAdapter(Context context, ArrayList<AnswerPair> answers, int currentQuestion) {
        this.context = context;
        this.answers = answers;
        this.currentQuestion = currentQuestion;
    }

    @Override
    public int getCount() {
        return answers.size();
    }

    @Override
    public AnswerPair getItem(int position) {
        return answers.get(position);
    }

    @Override
    public long getItemId(int position) {
        return -1;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater li = LayoutInflater.from(context);
            convertView = li.inflate(R.layout.layout_test_option, parent, false);
        }

        if (position % 2 == 1) {
            LinearLayout parentLayout = (LinearLayout) convertView;
            ArrayList<View> views = new ArrayList<View>();

            for (int x = 0; x < parentLayout.getChildCount(); x++) {
                views.add(parentLayout.getChildAt(x));
            }
            parentLayout.removeAllViews();
            for (int x = views.size() - 1; x >= 0; x--) {
                parentLayout.addView(views.get(x));
            }
        }

        ImageView Option = (ImageView) convertView.findViewById(R.id.option_image);
        TextView Description = (TextView) convertView.findViewById(R.id.option_description);

        Bitmap answerBitmap = CityOfTwo.answerBitmapList.get(
                String.valueOf(currentQuestion) + String.valueOf(position)
        );

        if (answerBitmap == null)
            Picasso.with(context)
                    .load(answers.get(position).second)
                    .into(Option);
        else
            Option.setImageBitmap(answerBitmap);

        Description.setText(answers.get(position).first);

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onSelectedListener != null) onSelectedListener.OnSelected(
                        answers.get(position),
                        position
                );
            }
        });

        return convertView;
    }

    public void setOnSelectedListener(OnSelectedListener onSelectedListener) {
        this.onSelectedListener = onSelectedListener;
    }

    public interface OnSelectedListener {
        void OnSelected(AnswerPair answer, int position);
    }
}