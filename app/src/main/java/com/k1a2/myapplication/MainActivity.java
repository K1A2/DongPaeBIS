package com.k1a2.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.k1a2.myapplication.http.BISRequestTask;
import com.k1a2.myapplication.http.RequestWeather;
import com.k1a2.myapplication.service.OnService;
import com.k1a2.myapplication.view.recyclerview.BISRecyclerAdapter;

import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recycler_bus1 = null; //왼쪽 버스 정보 (힌울공원 방면)
    private TextView text_min1 = null; //왼쪽 잠시후 도착 (힌울공원 방면)
    private RecyclerView recycler_bus2 = null; //오른쪽 버스 정보 (동패중 방면)
    private TextView text_min2 = null; //오른쪽 잠시후 도착 (동패중 방면)

    private BISRecyclerAdapter bisRecyclerAdapter = null; //왼쪽 버스 정보 리사이클러 어댑터
    private BISRecyclerAdapter bisRecyclerAdapter2 = null; //오른쪽 버스 정보 리사이클러 어댑터

    private PowerManager powerManager;

    private PowerManager.WakeLock wakeLock;

    private int currentApiVersion = 0;

    @SuppressLint("InvalidWakeLockTag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 전체 화면 처리
        currentApiVersion = android.os.Build.VERSION.SDK_INT;
        final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
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

        setContentView(R.layout.layout_main_v2);

        //화면 꺼지지 않도록 처리
        powerManager = (PowerManager)getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "WAKELOCK");
        wakeLock.acquire();

        //각각 리사이클러뷰에 어댑터 생성 및 연결
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

        //BIS 업데이트 시작
        startBis();

        //날씨 정부 10분마다 업데이트
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                new RequestWeather(MainActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        },0, 1000*60*10);
    }

    @Override
    protected void onPause() {
        super.onPause();
        wakeLock.release();
    }

    public void setWeatherInfo(Bitmap bitmap, Double currentTemp, Double currentLike, Double humidity, Double wind) {
        ImageView imageView = findViewById(R.id.main_img_weather);
        TextView textTemp = findViewById(R.id.main_text_temp);
        TextView textTemplike = findViewById(R.id.main_text_templike);
        TextView textHum = findViewById(R.id.main_text_hmid);
        TextView textWind = findViewById(R.id.main_text_wind);
        if (bitmap != null) imageView.setImageBitmap(bitmap);
        textTemp.setText(String.format("%.2f", currentTemp)+ "°C");
        textWind.setText(String.format("%.2f", wind) + "m/s");
        textTemplike.setText(String.format("%.2f", currentLike)+ "°C");
        textHum.setText(String.format("%.2f", humidity)+ "%");
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

                DisplayMetrics metrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(metrics);
                bisRecyclerAdapter.setItemHeight(height, metrics);

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

                DisplayMetrics metrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(metrics);
                bisRecyclerAdapter2.setItemHeight(height, metrics);

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
                        periodTime = 10000/page;
                    } else {
                        page += 1;
                        periodTime = 10000/page;
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
                        periodTime = 10000/page2;
                    } else {
                        page2 += 1;
                        periodTime = 10000/page2;
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
        repeatBis = new Timer();
        final Context context = this;
        repeatBis.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
                try {
                    //TODO: 리스트 형태 확인\

                    Date startTime = dateFormat.parse("00:00:00");
                    Date endTime = dateFormat.parse("24:00:00");
                    Date now = new Date(System.currentTimeMillis());
                    Date convert = dateFormat.parse(dateFormat.format(now));

                    if (convert.after(startTime) && convert.before(endTime)) {
                        ((MainActivity)context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
//                                layout_yes.setVisibility(View.VISIBLE);
//                                layout_not.setVisibility(View.GONE);
                                new BISRequestTask(MainActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//                                setBIS(test_list);
//                                setBIS2(test_list);
                            }
                        });
                        Log.d("Changed", "바뀜 ");
                    } else {
                        ((MainActivity)context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
//                                layout_yes.setVisibility(View.GONE);
//                                layout_not.setVisibility(View.VISIBLE);
                                text_min1.setText("");
                                text_min2.setText("");
                            }
                        });
                        Log.d("Changed", "표시시간이 아님 ");
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }, 0, 1000*10);
    }

    public void setBIS(String[][] bis) {
        TextView textView = findViewById(R.id.main_text1_no);
        text_min1.setText("");
        if (bis != null) {
            if (bis.length == 0) {
                textView.setVisibility(View.VISIBLE);
                recycler_bus1.setVisibility(View.GONE);
                return;
            }
            textView.setVisibility(View.GONE);
            recycler_bus1.setVisibility(View.VISIBLE);
            ArrayList<String[]> content = new ArrayList<String[]>();

            int c0 = 0;
            for (int i = 0;i < bis.length;i++) {
                content.add(bis[i]);
                int min = Integer.parseInt(bis[i][1].split(",")[0]);
                String num = bis[i][0];

                if (min <= 4) {
                    c0+=1;
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
            if (c0 == 0) {
                text_min1.setText("잠시후 도착하는 버스가 없습니다.");
            }

            bisRecyclerAdapter.setBISList(content);
//        isLoading = false;
        } else {
            text_min1.setText("");
            textView.setVisibility(View.VISIBLE);
            recycler_bus1.setVisibility(View.GONE);
        }
    }

    public void setBIS2(String[][] bis) {
        TextView textView = findViewById(R.id.main_text2_no);
        text_min2.setText("");
        if (bis != null) {
            if (bis.length == 0) {
                textView.setVisibility(View.VISIBLE);
                recycler_bus2.setVisibility(View.GONE);
                return;
            }
            textView.setVisibility(View.GONE);
            recycler_bus2.setVisibility(View.VISIBLE);
            ArrayList<String[]> content = new ArrayList<String[]>();

            int c0 = 0;
            for (int i = 0;i < bis.length;i++) {
                content.add(bis[i]);
                int min = Integer.parseInt(bis[i][1].split(",")[0]);
                String num = bis[i][0];

                if (min <= 4) {
                    c0 += 1;
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
            if (c0 == 0) {
                text_min2.setText("잠시후 도착하는 버스가 없습니다.");
            }

            bisRecyclerAdapter2.setBISList(content);
//        isLoading = false;
        } else {
            text_min2.setText("");
            textView.setVisibility(View.VISIBLE);
            recycler_bus2.setVisibility(View.GONE);
        }
    }
}
