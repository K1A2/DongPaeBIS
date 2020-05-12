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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class BISRequestTask extends AsyncTask<String, String, String[][]> {

    private MainActivity context = null;

    public BISRequestTask(Context context) {
        this.context = (MainActivity) context;
    }

    @Override
    protected String[][] doInBackground(String... strings) {
        URL url;
        Document doc = null; //정류장 도착정보

        try {
            url = new URL("http://openapi.gbis.go.kr/ws/rest/busarrivalservice/station?serviceKey=1234567890&stationId=229001658&");
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            doc = db.parse(new InputSource(url.openStream()));
            doc.getDocumentElement().normalize();

            ArrayList<String> routeID = new ArrayList<String>();
            ArrayList<String> Location = new ArrayList<String>();
            ArrayList<String> PredictTime = new ArrayList<String>();

            NodeList nodeList = doc.getElementsByTagName("busArrivalList");
            int length = nodeList.getLength();

            for (int i = 0; i < length; i++) {
                Node node = nodeList.item(i);
                Element fstElmnt = (Element) node;

                NodeList locationNo1 = fstElmnt.getElementsByTagName("locationNo1");
                String Value = locationNo1.item(0).getChildNodes().item(0).getNodeValue();
                if (Value == null){
                    Location.add(" ");
                }else {
                    Location.add(locationNo1.item(0).getChildNodes().item(0).getNodeValue());
                }

                NodeList locationNo2 = fstElmnt.getElementsByTagName("locationNo2");
                String Value2 = locationNo2.item(0).getChildNodes().item(0).getNodeValue();
                if (Value2 == null){
                    Location.add(" ");
                }else {
                    Location.add(locationNo2.item(0).getChildNodes().item(0).getNodeValue());
                }

                NodeList predictTime1 = fstElmnt.getElementsByTagName("predictTime1");
                String Value3 = predictTime1.item(0).getChildNodes().item(0).getNodeValue();
                if (Value3 == null){
                    PredictTime.add(" ");
                }else {
                    PredictTime.add(predictTime1.item(0).getChildNodes().item(0).getNodeValue());
                }

                NodeList predictTime2 = fstElmnt.getElementsByTagName("predictTime2");
                String Value4 = predictTime2.item(0).getChildNodes().item(0).getNodeValue();
                if (Value4 == null){
                    PredictTime.add(" ");
                }else {
                    PredictTime.add(predictTime2.item(0).getChildNodes().item(0).getNodeValue());
                }

                NodeList routeId = fstElmnt.getElementsByTagName("routeId");
                String Value5 = routeId.item(0).getChildNodes().item(0).getNodeValue();
                if (Value5 == null){
                    routeID.add(" ");
                }else {
                    routeID.add(routeId.item(0).getChildNodes().item(0).getNodeValue());
                }

                System.out.println("루트ID:" + routeID);
                System.out.println("도착시간:" + PredictTime);
                System.out.println("위치:" + Location);

//                NodeList staOrder = fstElmnt.getElementsByTagName("staOrder");
//                s += "staOrder = " + staOrder.item(0).getChildNodes().item(0).getNodeValue() + "\n";
//
//                NodeList stationId = fstElmnt.getElementsByTagName("stationId");
//                s += "stationId = " + stationId.item(0).getChildNodes().item(0).getNodeValue() + "\n";
            }

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

                ArrayList<String> BusNum = new ArrayList<String>();
                ArrayList<String> BusType = new ArrayList<String>();

                for (int i = 0; i < length2; i++) {
                    Node node = nodeList2.item(i);
                    Element fstElmnt2 = (Element) node;

                    NodeList routeName = fstElmnt2.getElementsByTagName("routeName");
                    BusNum.add(routeName.item(0).getChildNodes().item(0).getNodeValue());

                    NodeList routeTypeCd = fstElmnt2.getElementsByTagName("routeTypeCd");
                    BusType.add(routeTypeCd.item(0).getChildNodes().item(0).getNodeValue());

                    System.out.println("버스번호:" + BusNum);
                    System.out.println("버스타입:" + BusType);
                }
            }
        } catch (Exception e) {
            Log.e("ss", e.getMessage());
        }

        //TODO: 여기다가 파싱 코드 작성
        return new String[0][];
    }

    @Override
    protected void onPostExecute(String[][] strings) {
//        context.setBIS(); 한울공원 방면 설정
//        context.setBIS2(); 동패중 방면 설정
    }
}
