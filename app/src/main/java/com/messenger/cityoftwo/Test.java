package com.messenger.cityoftwo;

import java.util.ArrayList;

/**
 * Created by Aayush on 2/6/2016.
 */
public class Test {

    String Question;
    ArrayList<String> Answers;

    public Test(String question, ArrayList<String> answers) {
        Question = question;
        Answers = answers;
    }

    public String getQuestion() {
        return Question;
    }

    public ArrayList<String> getAnswers() {
        return Answers;
    }

    public String getAnswer(int index){
        return Answers.get(index);
    }

    public Integer getAnswerIndex(String answer){
        return Answers.indexOf(answer);
    }
}
