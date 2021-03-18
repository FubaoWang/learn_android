package com.example.math_game;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;

import java.util.Random;

public class MyViewModel extends AndroidViewModel {

    private static String KEY_HIGH_SCORE = "key_high_score";
    private static String KEY_LEFT_NUMBER = "key_left_number";
    private static String KEY_RIGHT_NUMBER = "key_right_number";
    private static String KEY_OPERATOR = "key_operator";
    private static String KEY_ANSWER = "key_answer";
    private static String SHP_NAME = "save_shp_data";
    private static String KEY_CURRENT_NAME = "key_current_name";

    boolean win_flag = false;
    SavedStateHandle handle;

    public MyViewModel(@NonNull Application application, SavedStateHandle handle) {
        super(application);

        if(!handle.contains(KEY_HIGH_SCORE)){
            SharedPreferences shp = getApplication().getSharedPreferences(SHP_NAME, Context.MODE_PRIVATE);
            handle.set(KEY_HIGH_SCORE,shp.getInt(KEY_HIGH_SCORE,0));
            handle.set(KEY_LEFT_NUMBER,0);
            handle.set(KEY_RIGHT_NUMBER,0);
            handle.set(KEY_OPERATOR,"+");
            handle.set(KEY_ANSWER,0);
            handle.set(KEY_CURRENT_NAME,0);
        }
        this.handle = handle;
    }

    public MutableLiveData<Integer> getLeftNumber(){
        return handle.getLiveData(KEY_LEFT_NUMBER);
    }
    public MutableLiveData<Integer> getRightNumber(){
        return handle.getLiveData(KEY_RIGHT_NUMBER);
    }
    public MutableLiveData<String> getOperator(){
        return handle.getLiveData(KEY_OPERATOR);
    }
    public MutableLiveData<Integer> getHighScore(){
        return handle.getLiveData(KEY_HIGH_SCORE);
    }
    public MutableLiveData<Integer> getCurrentScore(){
        return handle.getLiveData(KEY_CURRENT_NAME);
    }
    public MutableLiveData<Integer> getAnswer(){
        return handle.getLiveData(KEY_ANSWER);
    }

    void generator(){
        int LEVEL = 20;
        Random random = new Random();
        int x, y, z;
        x = random.nextInt(LEVEL) + 1;
        y = random.nextInt(LEVEL) + 1;
        z = random.nextInt(LEVEL) + 1;
        if (z%2==0){
            getOperator().setValue("+");
            getLeftNumber().setValue(x);
            getRightNumber().setValue(y);
            getAnswer().setValue(x+y);
        }else{
            getOperator().setValue("-");
            if(x < y) {
                x = x + y;
            }
            getLeftNumber().setValue(x);
            getRightNumber().setValue(y);
            getAnswer().setValue(x-y);
        }

    }

    void save(){
        SharedPreferences shp = getApplication().getSharedPreferences(SHP_NAME,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = shp.edit();
        editor.putInt(KEY_HIGH_SCORE, getHighScore().getValue());
        editor.apply();
    }

    void answerCorrect(){
        getCurrentScore().setValue(getCurrentScore().getValue() + 1);
        if (getCurrentScore().getValue() > getHighScore().getValue()){
            getHighScore().setValue(getCurrentScore().getValue());
            win_flag = true;
        }
        generator();
    }

}
