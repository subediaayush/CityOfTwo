package com.messenger.cityoftwo;

import java.util.ArrayList;

/**
 * Created by Aayush on 2/6/2016.
 */
public class Test {

    String Question;
    ArrayList<AnswerPair> Answers;

    public Test(String question, ArrayList<AnswerPair> answers) {
        Question = question;
        Answers = answers;
    }

    public String getQuestion() {
        return Question;
    }

    public ArrayList<AnswerPair> getAnswers() {
        return Answers;
    }

    public AnswerPair getAnswer(int index){
        return Answers.get(index);
    }
}
