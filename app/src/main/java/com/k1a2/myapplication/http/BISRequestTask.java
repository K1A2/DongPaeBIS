package com.k1a2.myapplication.http;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.k1a2.myapplication.MainActivity;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.net.URL;
import java.util.ArrayList;
import java.util.Timer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class BISRequestTask extends AsyncTask<String, String, String[][][]> {

    private MainActivity context = null;

    public BISRequestTask(Context context) {
        this.context = (MainActivity) context;
    }

    @Override
    protected String[][][] doInBackground(String... strings) {

        ArrayList<String[][]> returnList = new ArrayList<>();
        returnList.add(getBusInformation("229001394"));//공원
        returnList.add(getBusInformation("229001658"));//삽다리
//        returnList.add(getBusInformation("229001474"));//공원
//        returnList.add(getBusInformation("229001473"));//삽다리

        String[][][] result = new String[2][][];
        result[0] = returnList.get(0);
        result[1] = returnList.get(1);
//        String[][] list1 = null;
//        if (returnList.get(0) != null) {//삽다리
//            list1 = new String[returnList.get(0).length][4];
//
//            for (int i = 0;i < returnList.get(0).length;i++) {
//
//            }
//        }
//
//        String[][] list2 = null;
//        if (returnList.get(1) != null) {//공원
//            list2 = new String[returnList.get(1).length][4];
//        }
        return result;
    }

    private String[][] getBusInformation(String stationId) {
        URL url;
        Document doc = null; //정류장 도착정보
        try {
            url = new URL("http://openapi.gbis.go.kr/ws/rest/busarrivalservice/station?serviceKey=1234567890&stationId=" + stationId + "&");
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            doc = db.parse(new InputSource(url.openStream()));
            doc.getDocumentElement().normalize();

            ArrayList<String> routeID = new ArrayList<String>();
            ArrayList<String[]> Location = new ArrayList<String[]>();
            ArrayList<String[]> PredictTime = new ArrayList<String[]>();

            NodeList nodeList = doc.getElementsByTagName("busArrivalList");
            int length = nodeList.getLength();

            for (int i = 0; i < length; i++) {
                Node node = nodeList.item(i);
                Element fstElmnt = (Element) node;

                String[] locations = new String[2];
                NodeList locationNo1 = fstElmnt.getElementsByTagName("locationNo1");
                if (0 == locationNo1.item(0).getChildNodes().getLength()){
//                    Location.add(" ");
                    locations[0] = " ";
                }else {
                    locations[0] = locationNo1.item(0).getChildNodes().item(0).getNodeValue();
//                    Location.add(locationNo1.item(0).getChildNodes().item(0).getNodeValue());
                }

                NodeList locationNo2 = fstElmnt.getElementsByTagName("locationNo2");
                if (0 == locationNo2.item(0).getChildNodes().getLength()){
                    locations[1] = " ";
                }else {
                    locations[1] = locationNo2.item(0).getChildNodes().item(0).getNodeValue();
//                    Location.add(locationNo2.item(0).getChildNodes().item(0).getNodeValue());
                }
                Location.add(locations);

                String[] times = new String[2];
                NodeList predictTime1 = fstElmnt.getElementsByTagName("predictTime1");
                if (0 == predictTime1.item(0).getChildNodes().getLength()){
                    times[0] = " ";
//                    PredictTime.add(" ");
                }else {
                    times[0] = predictTime1.item(0).getChildNodes().item(0).getNodeValue();
//                    PredictTime.add(predictTime1.item(0).getChildNodes().item(0).getNodeValue());
                }

                NodeList predictTime2 = fstElmnt.getElementsByTagName("predictTime2");
                if (0 == predictTime2.item(0).getChildNodes().getLength()){
                    times[1] = " ";
//                    PredictTime.add(" ");
                }else {
                    times[1] = predictTime2.item(0).getChildNodes().item(0).getNodeValue();
//                    PredictTime.add(predictTime2.item(0).getChildNodes().item(0).getNodeValue());
                }
                PredictTime.add(times);

                NodeList routeId = fstElmnt.getElementsByTagName("routeId");
                if (0 == routeId.item(0).getChildNodes().getLength()){
                    routeID.add(" ");
                }else {
                    routeID.add(routeId.item(0).getChildNodes().item(0).getNodeValue());
                }

//                NodeList staOrder = fstElmnt.getElementsByTagName("staOrder");
//                s += "staOrder = " + staOrder.item(0).getChildNodes().item(0).getNodeValue() + "\n";
//
//                NodeList stationId = fstElmnt.getElementsByTagName("stationId");
//                s += "stationId = " + stationId.item(0).getChildNodes().item(0).getNodeValue() + "\n";
            }


//            ArrayList<String> BusNum = new ArrayList<String>();
//            ArrayList<String> BusType = new ArrayList<String>();
            ArrayList<String[]> Bus = new ArrayList<>();

            Log.d("루트ID:", String.valueOf(routeID));
            Log.d("도착시간:", String.valueOf(PredictTime));
            Log.d("위치:", String.valueOf(Location));

            for (String routeid : routeID) {
                URL url2;
                Document doc2 = null; //루트아이디->버스번호
                System.out.println("테스트:" + routeid);
                url2 = new URL("http://openapi.gbis.go.kr/ws/rest/busrouteservice/info?serviceKey=1234567890&routeId=" + routeid);
                DocumentBuilderFactory dbf2 = DocumentBuilderFactory.newInstance();
                DocumentBuilder db2 = dbf2.newDocumentBuilder();
                doc2 = db2.parse(new InputSource(url2.openStream()));
                doc2.getDocumentElement().normalize();

                NodeList nodeList2 = doc2.getElementsByTagName("busRouteInfoItem");
                int length2 = nodeList2.getLength();

                for (int i = 0; i < length2; i++) {
                    Node node = nodeList2.item(i);
                    Element fstElmnt2 = (Element) node;

                    String[] busInfo = new String[2];
                    NodeList routeName = fstElmnt2.getElementsByTagName("routeName");
                    busInfo[0] = routeName.item(0).getChildNodes().item(0).getNodeValue();
//                    BusNum.add(routeName.item(0).getChildNodes().item(0).getNodeValue());

                    NodeList routeTypeCd = fstElmnt2.getElementsByTagName("routeTypeCd");
                    busInfo[1] = routeTypeCd.item(0).getChildNodes().item(0).getNodeValue();
//                    BusType.add(routeTypeCd.item(0).getChildNodes().item(0).getNodeValue());
                    Bus.add(busInfo);

                    Log.d("버스번호:", String.valueOf(busInfo[0]));
                    Log.d("버스타입:", String.valueOf(busInfo[1]));
                }
            }

            ArrayList<String[]> allInfo = new ArrayList<>();
            for (int i = 0;i < Bus.size();i++) {
                String[] busInfo = Bus.get(i);
                String[] times = PredictTime.get(i);
                String[] locations = Location.get(i);

                allInfo.add(new String[]{busInfo[0], times[0] + "," + times[1], locations[0] + "," + locations[1], busInfo[1]});
            }

            String[][] result = new String[allInfo.size()][4];
            for (int i = 0;i < allInfo.size();i++) {
                result[i] = allInfo.get(i);
            }

            return result;
        } catch (Exception e) {
            Log.e("Error", e.getMessage() + "\n" + e.getLocalizedMessage());
            return null;
        }
    }

    @Override
    protected void onPostExecute(String[][][] strings) {
        context.setBIS(strings[0]);
        context.setBIS2(strings[1]);
//        context.setBIS2(); 동패중 방면 설정
    }
}
