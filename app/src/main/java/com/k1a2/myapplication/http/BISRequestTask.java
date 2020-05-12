package com.k1a2.myapplication.http;

import android.content.Context;
import android.os.AsyncTask;

import com.k1a2.myapplication.MainActivity;

public class BISRequestTask extends AsyncTask<String, String, String[][]> {

    private MainActivity context = null;

    public BISRequestTask(Context context) {
        this.context = (MainActivity) context;
    }

    @Override
    protected String[][] doInBackground(String... strings) {
        //TODO: 여기다가 파싱 코드 작성
        return new String[0][];
    }

    @Override
    protected void onPostExecute(String[][] strings) {
//        context.setBIS(); 한울공원 방면 설정
//        context.setBIS2(); 동패중 방면 설정
    }
}
