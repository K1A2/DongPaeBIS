package com.k1a2.myapplication.view.recyclerview;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.k1a2.myapplication.MainActivity;
import com.k1a2.myapplication.R;
import com.k1a2.schoolcalculator.view.recyclerview.BISRecyclerItem;
import com.uni.unitor.unitorm2.view.recycler.listener.RecyclerItemClickListener;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**성적 추가/삭제 리스트 아이탬 관리 어댑터
 * 절대 수정금지**/

public class BISRecyclerAdapter extends RecyclerView.Adapter<BISRecyclerAdapter.ViewHolder> implements RecyclerItemClickListener.OnItemClickListener {

    private ArrayList<BISRecyclerItem> listViewList = new  ArrayList<BISRecyclerItem>();
    private int itemHeight = 0;
    private Context context;

    private final int maule = 30;
    private final int general = 13;

    private int vv = 0;

    public BISRecyclerAdapter(Context context, int vv) {
        this.context = context;
        this.vv = vv;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_list_bus_one, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.linearLayout.getLayoutParams().height = itemHeight;
        BISRecyclerItem item = listViewList.get(position);
        holder.textNum.setText(item.getNum());
        TextView r = holder.textTime;
        int min = Integer.parseInt(item.getMin());
        if (min <= 4) {
            r.setText("잠시 후 도착");
        } else {
            r.setText(item.getMin()  + "분 후 도착");
        }
        holder.textPos.setText(item.getPos());
        switch (Integer.parseInt(item.getType())) {
            case 13:{
//                int[] location = new int[2];
//                r.getLocationOnScreen(location);
//                Shader textShader=new LinearGradient(location[0], location[1], r.getWidth(), r.getHeight(),
//                        new int[]{Color.rgb(8, 174, 234),Color.rgb(42, 245, 152)},
//                        new float[]{0, 1}, Shader.TileMode.CLAMP);
//                holder.textNum.getPaint().setShader(textShader);
                holder.textNum.setTextColor(Color.rgb(8, 174, 234));
                break;
            }

            case 30:{
//                Shader textShader=new LinearGradient(0, 0, 0, 20,
//                        new int[]{Color.rgb(250, 217, 97),Color.rgb(247, 107, 28)},
//                        new float[]{0, 1}, Shader.TileMode.CLAMP);
//                holder.textNum.getPaint().setShader(textShader);
                holder.textNum.setTextColor(Color.rgb(250, 217, 97));
                break;
            }

            default:{
                holder.textNum.setTextColor(Color.WHITE);
                break;
            }
        }
        //TODO: UI에 뿌려주는 부분
    }

    @Override
    public int getItemCount() {
        return listViewList.size();
    }

    @Override
    public void onItemClicked(@NotNull View view, int position) {
        Toast.makeText(view.getContext(), position, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLongItemClicked(@Nullable View view, int position) {

    }

    public int getItemHeight() {
        return itemHeight;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public CardView linearLayout = null;
        public TextView textNum = null;
        public TextView textPos = null;
        public TextView textTime = null;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            context = itemView.getContext();
            //TODO: UI 아이템 가져오는 부분
            linearLayout = itemView.findViewById(R.id.list_content_main);
            textNum = itemView.findViewById(R.id.text_bus_num);
            textTime = itemView.findViewById(R.id.text_bus_time);
            textPos = itemView.findViewById(R.id.text_bus_now);
        }
    }

    public void setBISList(ArrayList<String[]> bis) {
        ((MainActivity)context).pauseAutoScroll(vv);

        Map<BISRecyclerItem, Boolean> ht = new HashMap<>();
        for (int b = 0;b < bis.size();b++) {
            BISRecyclerItem bisRecyclerItem = new BISRecyclerItem();
            String[] s = bis.get(b);
            bisRecyclerItem.setNum(s[0]);
            bisRecyclerItem.setMin(s[1]);
            bisRecyclerItem.setPos(s[2]);
            bisRecyclerItem.setType(s[3]);
            ht.put(bisRecyclerItem, false);
        }

        ArrayList<BISRecyclerItem> biss = new ArrayList<>();
        for (int b = 0;b < listViewList.size();b++) {
            BISRecyclerItem s = listViewList.get(b);
            String num = s.getNum();
            boolean isIn = false;
            for (int i = 0;i < bis.size();i++) {
                if (num.equals(bis.get(i)[0])) {
//                    ht.put(num, true);
                    isIn = true;
                    break;
                }
            }
            if (!isIn) {
                biss.add(s);
            }
        }
        for (BISRecyclerItem b : biss) {
            int pos = listViewList.indexOf(b);
            listViewList.remove(b);
            notifyItemRemoved(pos);
        }

        Iterator<Map.Entry<BISRecyclerItem,Boolean>> iter = ht.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<BISRecyclerItem,Boolean> entry = iter.next();
            BISRecyclerItem s = entry.getKey();
            String num = s.getNum();
            String pos = s.getPos();
            String min = s.getMin();
            String type = s.getType();
            for (int i = 0;i < listViewList.size();i++) {
                BISRecyclerItem bbbbb = listViewList.get(i);
                if (num.equals(bbbbb.getNum())) {
                    final BISRecyclerItem bisRecyclerItem = new BISRecyclerItem();
                    bisRecyclerItem.setNum(num);
                    bisRecyclerItem.setMin(min);
                    bisRecyclerItem.setPos(pos);
                    bisRecyclerItem.setType(type);
                    listViewList.set(i, bisRecyclerItem);
                    notifyItemChanged(i);
                    iter.remove();
                    break;
                }
            }
        }

        Set set = ht.entrySet();
        Iterator iterator = set.iterator();

        while(iterator.hasNext()){
            Map.Entry entry = (Map.Entry)iterator.next();
            BISRecyclerItem key = (BISRecyclerItem) entry.getKey();
            Boolean value = (Boolean) entry.getValue();
            if (!value) {
                listViewList.add(key);
                notifyItemInserted(listViewList.size() - 1);
            }
        }

        Collections.sort(listViewList, new Comparator<BISRecyclerItem>() {
            @Override
            public int compare(BISRecyclerItem o1, BISRecyclerItem o2) {
                if ((Integer.parseInt(o1.getMin())) > (Integer.parseInt(o2.getMin()))) {
                    return 1;
                } else if ((Integer.parseInt(o1.getMin())) < (Integer.parseInt(o2.getMin()))) {
                    return -1;
                } else if (((Integer.parseInt(o1.getMin())) == (Integer.parseInt(o2.getMin())))) {
                    return 0;
                }
                return 0;
            }
        });
        notifyDataSetChanged();

        ((MainActivity)context).startAutoScroll(vv);
    }

    public void setItemHeight(int height) {
        this.itemHeight = (height - 5)/4;
    }

    public void addItem(BISRecyclerItem gradeRecyclerItem) {
        listViewList.add(gradeRecyclerItem);
        notifyItemInserted(listViewList.size());
    }

    public void removeItem(int position) {
        if (position != -1) {
            listViewList.remove(position);
            notifyItemRemoved(position);
        }
    }

    public BISRecyclerItem getItem(int position) {
        return listViewList.get(position);
    }

    public void clearItem() {
        final int count = listViewList.size();
        listViewList.clear();
        notifyItemRangeRemoved(0, count);
    }

//    public static List<BISRecyclerItem> sortByValue(final Map<BISRecyclerItem, Boolean> map) {
//        List<BISRecyclerItem> list = new ArrayList<BISRecyclerItem>();
//        list.addAll(map.keySet());
//
//        Collections.sort(list, new Comparator<BISRecyclerItem>() {
//            public int compare(BISRecyclerItem o1, BISRecyclerItem o2) {
//                int v1 = map.get(o1);
//                int v2 = map.get(o2);
//                return ((Comparable<Integer>) v2).compareTo(v1);
//            }
//        });
//
//        return list;
//    }
}
