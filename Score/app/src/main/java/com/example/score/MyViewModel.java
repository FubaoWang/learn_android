package com.example.score;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;

public class MyViewModel extends AndroidViewModel {
    private int aBack, bBack;
    private SavedStateHandle handle;
    private String Adata = getApplication().getString(R.string.Adata);
    private String Bdata = getApplication().getString(R.string.Bdata);
    private String shpname = getApplication().getString(R.string.shpname);

    public MyViewModel(@NonNull Application application, SavedStateHandle handle) {
        super(application);
        this.handle = handle;
        if (!handle.contains(Adata))
            load();
    }

    void load() {
        SharedPreferences shp = getApplication().getSharedPreferences(shpname, Context.MODE_PRIVATE);
        int a = shp.getInt(Adata, 0);
        int b = shp.getInt(Bdata, 0);

        handle.set(Adata, a);
        handle.set(Bdata, b);
    }

    void save() {
        SharedPreferences shp = getApplication().getSharedPreferences(shpname, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = shp.edit();
        editor.putInt(Adata, getaTeamScore().getValue());
        editor.putInt(Bdata, getbTeamScore().getValue());
        editor.apply();
    }

    public LiveData<Integer> getaTeamScore() {
        return handle.getLiveData(Adata);
    }

    public LiveData<Integer> getbTeamScore() {
        return handle.getLiveData(Bdata);
    }

    public void aTeamAdd(int p) {
        aBack = getaTeamScore().getValue();
        bBack = getbTeamScore().getValue();
        handle.set(Adata, getaTeamScore().getValue() + p);
    }

    public void bTeamAdd(int p) {
        aBack = getaTeamScore().getValue();
        bBack = getbTeamScore().getValue();
        handle.set(Bdata, getbTeamScore().getValue() + p);
    }

    public void reset() {
        aBack = getaTeamScore().getValue();
        bBack = getaTeamScore().getValue();
        handle.set(Adata, 0);
        handle.set(Bdata, 0);
    }

    public void undo() {
        handle.set(Adata, aBack);
        handle.set(Bdata, bBack);
    }
}
