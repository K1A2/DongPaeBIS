package com.k1a2.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.k1a2.myapplication.http.BISRequestTask;
import com.k1a2.myapplication.service.OnService;
import com.k1a2.myapplication.view.recyclerview.BISRecyclerAdapter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recycler_bus1 = null;
    private TextView text_min1 = null;
    private RecyclerView recycler_bus2 = null;
    private TextView text_min2 = null;
    private LinearLayout layout_not = null;
    private LinearLayout layout_yes = null;

    private BISRecyclerAdapter bisRecyclerAdapter = null;
    private BISRecyclerAdapter bisRecyclerAdapter2 = null;

    private int currentApiVersion = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentApiVersion = android.os.Build.VERSION.SDK_INT;
        final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

        // This work only for android 4.4+
        if(currentApiVersion >= Build.VERSION_CODES.KITKAT) {
            getWindow().getDecorView().setSystemUiVisibility(flags);
            final View decorView = getWindow().getDecorView();
            decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {

                        @Override
                        public void onSystemUiVisibilityChange(int visibility) {
                            if((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                                decorView.setSystemUiVisibility(flags);
                            }
                        }
                    });
        }
        setContentView(R.layout.activity_main);

        layout_not = findViewById(R.id.main_layout_not);
        layout_yes = findViewById(R.id.main_layout_on);

        bisRecyclerAdapter = new BISRecyclerAdapter(this, 0);
        bisRecyclerAdapter2 = new BISRecyclerAdapter(this, 1);

        recycler_bus1 = findViewById(R.id.main_recycler_bus1);
        text_min1 =findViewById(R.id.main_text_min);

        recycler_bus2 = findViewById(R.id.main_recycler_bus2);
        text_min2 =findViewById(R.id.main_text_min2);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recycler_bus1.setLayoutManager(layoutManager);
        recycler_bus1.setAdapter(bisRecyclerAdapter);

        LinearLayoutManager layoutManager2 = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recycler_bus2.setLayoutManager(layoutManager2);
        recycler_bus2.setAdapter(bisRecyclerAdapter2);

//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                String[][] test_list2 = {{"66", "1", "change", "13"}};
//                setBIS(test_list2);
//            }
//        }, 4000);
//
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                String[][] test_list2 = {{"80", "2", "1번전", "13"}, {"66", "1", "9999", "13"}, {"080", "30", "302", "30"}, {"081", "29", "00049", "30"}, {"086", "12", ",,", "30"}, {"999", "8", "00049", "3"}};
//                setBIS(test_list2);
//            }
//        }, 6000);
//
//        setBIS(test_list);
//        setBIS2(test_list);
        startBis();
    }

    @SuppressWarnings("NewApi")
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(currentApiVersion >= Build.VERSION_CODES.KITKAT && hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        recycler_bus1.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout()  {
                recycler_bus1.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int height = recycler_bus1.getHeight();
                bisRecyclerAdapter.setItemHeight(height);

                for (int i = 0;i < bisRecyclerAdapter.getItemCount();i++) {
                    View viewItem = recycler_bus1.getLayoutManager().findViewByPosition(i);
                    viewItem.findViewById(R.id.list_content_main).getLayoutParams().height = (height - 5)/4;
                }
//                isDraw = true;
            }
        });
        recycler_bus2.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout()  {
                recycler_bus2.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int height = recycler_bus2.getHeight();
                bisRecyclerAdapter2.setItemHeight(height);

                for (int i = 0;i < bisRecyclerAdapter2.getItemCount();i++) {
                    View viewItem = recycler_bus2.getLayoutManager().findViewByPosition(i);
                    viewItem.findViewById(R.id.list_content_main).getLayoutParams().height = (height - 5)/4;
                }
//                isDraw = true;
            }
        });
    }

    private Timer timer = null;
    private boolean isRun = false;
    int p = 0;
    int page = 0;

    private Timer timer2 = null;
    private boolean isRun2 = false;
    int p2 = 0;
    int page2 = 0;

    public void pauseAutoScroll(int vv) {
        switch (vv) {
            case 0:{
                isRun = false;
                if (timer != null) {
                    timer.cancel();
                }
                break;
            }

            case 1:{
                isRun2 = false;
                if (timer2 != null) {
                    timer2.cancel();
                }
                break;
            }
        }
    }

    public void startAutoScroll(int vv) {
        switch (vv) {
            case 0:{
                isRun = true;
                int count = bisRecyclerAdapter.getItemCount();
                p = 1;
                page = count/4;
                final int rest = count%4;
                int periodTime = 0;
                if (page == 0) {

                } else {
                    if (rest == 0) {
                        periodTime = 30000/page;
                    } else {
                        page += 1;
                        periodTime = 30000/page;
                    }
                    timer = new Timer(true);
                    timer.scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            if (isRun) {
                                if (p > page) {
                                    p = 1;
                                }
                                if (p == 1) {
                                    recycler_bus1.smoothScrollToPosition(0);
                                    p++;
                                } else {
                                    recycler_bus1.smoothScrollToPosition(p * 4 - 1);
                                    p++;
                                }
                                Log.d("Timer", "pos" + String.valueOf(p));
                            }
                        }
                    }, 0, periodTime);
                }
                break;
            }

            case 1:{
                isRun2 = true;
                int count = bisRecyclerAdapter2.getItemCount();
                p2 = 1;
                page2 = count/4;
                final int rest = count%4;
                int periodTime = 0;
                if (page2 == 0) {

                } else {
                    if (rest == 0) {
                        periodTime = 30000/page2;
                    } else {
                        page2 += 1;
                        periodTime = 30000/page2;
                    }
                    timer2 = new Timer(true);
                    timer2.scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            if (isRun2) {
                                if (p2 > page2) {
                                    p2 = 1;
                                }
                                if (p2 == 1) {
                                    recycler_bus2.smoothScrollToPosition(0);
                                    p2++;
                                } else {
                                    recycler_bus2.smoothScrollToPosition(p2 * 4 - 1);
                                    p2++;
                                }
                                Log.d("Timer", "pos" + String.valueOf(p2));
                            }
                        }
                    }, 0, periodTime);
                }
                break;
            }
        }
    }


    private Timer repeatBis = null;

    public void startBis() {
//        alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
//        Intent intent = new Intent(this, OnService.class);
//        PendingIntent pIntent = PendingIntent.getBroadcast(this, 0,intent, 0);
//
//        Calendar calendar = Calendar.getInstance();
//        calendar.set(Calendar.HOUR_OF_DAY, 0);
//        calendar.set(Calendar.MINUTE, 36);
//        calendar.set(Calendar.SECOND, 0);
//        calendar.set(Calendar.MILLISECOND, 0);
//
//        // 지정한 시간에 매일 알림
//        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),  1000*60, pIntent);
        repeatBis = new Timer();
        final Context context = this;
        repeatBis.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
                try {
                    final String[][] test_list = {{"66", "3", "2번전", "13"}, {"80", "4", "1번전", "30"}, {"150", "1", "1번전", "13"}, {"080", "25", "9번전", "30"}};

                    Date startTime = dateFormat.parse("00:00:00");
                    Date endTime = dateFormat.parse("24:00:00");
                    Date now = new Date(System.currentTimeMillis());
                    Date convert = dateFormat.parse(dateFormat.format(now));

                    if (convert.after(startTime) && convert.before(endTime)) {
                        ((MainActivity)context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                layout_yes.setVisibility(View.VISIBLE);
                                layout_not.setVisibility(View.GONE);
//                              new BISRequestTask(MainActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR); aysnctask 실
                                setBIS(test_list);
                                setBIS2(test_list);
                            }
                        });
                        Log.d("Changed", "바뀜 ");
                    } else {
                        ((MainActivity)context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                layout_yes.setVisibility(View.GONE);
                                layout_not.setVisibility(View.VISIBLE);
                            }
                        });
                        Log.d("Changed", "표시시간이 아님 ");
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }, 0, 1000*30);
    }

    public void setBIS(String[][] bis) {
        ArrayList<String[]> content = new ArrayList<String[]>();
        text_min1.setText("");
        for (int i = 0;i < bis.length;i++) {
            content.add(bis[i]);
            int min = Integer.parseInt(bis[i][1]);
            String num = bis[i][0];

            if (min <= 4) {
                switch (Integer.parseInt(bis[i][3])) {
                    case 13:{
                        text_min1.append(num + "번 ");
                        break;
                    }

                    case 30:{
                        text_min1.append(num + "번 ");
                        break;
                    }

                    default:{
                        text_min1.append(num + "번 ");
                        break;
                    }
                }
            }
        }

        bisRecyclerAdapter.setBISList(content);
//        isLoading = false;
    }

    public void setBIS2(String[][] bis) {
        ArrayList<String[]> content = new ArrayList<String[]>();
        text_min2.setText("");
        for (int i = 0;i < bis.length;i++) {
            content.add(bis[i]);
            int min = Integer.parseInt(bis[i][1]);
            String num = bis[i][0];

            if (min <= 4) {
                switch (Integer.parseInt(bis[i][3])) {
                    case 13:{
                        text_min2.append(num + "번 ");
                        break;
                    }

                    case 30:{
                        text_min2.append(num + "번 ");
                        break;
                    }

                    default:{
                        text_min2.append(num + "번 ");
                        break;
                    }
                }
            }
        }

        bisRecyclerAdapter2.setBISList(content);
//        isLoading = false;
    }
}
