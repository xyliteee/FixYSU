package com.Xyliteee.fixysu;
import android.graphics.*;
import java.util.Arrays;
import java.util.Objects;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.client5.http.cookie.*;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.*;
import java.io.InputStream;


public class LoginFunctions {
    private static String UserName;

    public static String cookieValue;
    private static LoginFunctions instance;
    private final static  CookieStore cookieStore = new BasicCookieStore();
    private static final CloseableHttpClient httpClient = HttpClients.custom ().setDefaultCookieStore(cookieStore).build();

    private static final String onlineDevicesUrl = "https://serv.ysu.edu.cn/selfservice/module/webcontent/web/onlinedevice_list.jsf";

    public static Bitmap GetVerifyCode(){
        Bitmap verifyCodeImage = null;
        String codeUrl = "https://serv.ysu.edu.cn/selfservice/common/web/verifycode.jsp";
        HttpGet httpGet = new HttpGet(codeUrl);
        httpGet.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36");
        try {
            CloseableHttpResponse response = httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                InputStream inputStream = entity.getContent();
                verifyCodeImage = BitmapFactory.decodeStream(inputStream);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return verifyCodeImage;
    }
    public static String JudgeLogin(String userName, String encryptedPassword, String verifyCode) {
        String result = null;
        UserName = userName;
        String judgeUrl = "https://serv.ysu.edu.cn/selfservice/module/scgroup/web/login_judge.jsf";
        HttpPost httpPost = new HttpPost(judgeUrl);
        httpPost.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36");
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("from", "rsa"));
        params.add(new BasicNameValuePair("name", userName));
        params.add(new BasicNameValuePair("password", encryptedPassword));
        params.add(new BasicNameValuePair("verify", verifyCode));
        params.add(new BasicNameValuePair("verifyMsg", ""));
        httpPost.setEntity(new UrlEncodedFormEntity(params));
        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            HttpEntity entity = response.getEntity();
            result = EntityUtils.toString(entity);
            EntityUtils.consume(entity);
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static void GetCookie(){
        cookieValue = "init";
        for(Cookie cookie:cookieStore.getCookies()){
            cookieValue = cookie.getValue();
        }
        cookieValue = "JSESSIONID="+cookieValue;
    }

    public static Device[] GetDevicesList(){
        Device[] devices = new Device[10];
        int index = 0;
        String devicesHtml = null;
        HttpGet httpGet = new HttpGet(onlineDevicesUrl);
        httpGet.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36");
        try {
            CloseableHttpResponse response = httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            devicesHtml = EntityUtils.toString(entity);
            EntityUtils.consume(entity);
            String regex = "id=\"([^\"]*)\"";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(devicesHtml);
            while (matcher.find()) {
                String single = matcher.group(1);
                if (single.contains("divdiv")){
                    String deviceID = single.replace("divdiv", "");
                    devices[index] = new Device(); // 创建并实例化Device对象
                    devices[index].deviceID = deviceID;
                    index++;
                }
            }

            devices = Arrays.stream(devices).filter(Objects::nonNull).toArray(Device[]::new);
            if (devices.length == 0){
                devices = new Device[1];
                devices[0] = new Device();
                devices[0].deviceName ="无设备";
            }
            else{
                for (Device device :devices){
                    String name = Re("<input id=\"inputId" + device.deviceID + "\" type=\"hidden\" value=\"(.*?)\">",devicesHtml);
                    String IP = Re("<input id=\"userIp4" + device.deviceID + "\" type=\"hidden\" value=\"(.*?)\">",devicesHtml);
                    String MAC = Re("<input id=\"usermac" + device.deviceID + "\" type=\"hidden\" value=\"(.*?)\">",devicesHtml);
                    String time = Re("<input id=\"createTimeStr" + device.deviceID + "\" type=\"hidden\" value=\"(.*?)\">",devicesHtml);
                    device.deviceName = name;device.ipAddress = IP;device.macAddress = MAC;device.lastOnlineTime = time;
                }
            }
            GetCookie();
        }catch (IOException |ParseException e){
            e.printStackTrace();
            devices = new Device[1];
            devices[0] = new Device();
            devices[0].deviceName ="登陆失败";
        }

        return devices;
    }

    public static void KickDevice(String IP){
        String url = "https://serv.ysu.edu.cn/selfservice/module/userself/web/userself_ajax.jsf?methodName=indexBean.kickUserBySelfForAjax";
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36");
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("key", UserName+":"+IP));
        httpPost.setEntity(new UrlEncodedFormEntity(params));
        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            HttpEntity entity = response.getEntity();
            EntityUtils.consume(entity);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static String Re(String regex,String input){
        String result = "null";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            result = matcher.group(1);
        }
        return result;
    }

    public static Device[] AutoGetDevicesList(String inputCookieValue){
        Device[] devices = new Device[10];
        int index = 0;
        String devicesHtml = null;
        HttpGet httpGet = new HttpGet(onlineDevicesUrl);
        httpGet.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36");
        httpGet.addHeader("Cookie",inputCookieValue);
        System.out.println(inputCookieValue);
        try {
            CloseableHttpResponse response = httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            devicesHtml = EntityUtils.toString(entity);
            EntityUtils.consume(entity);
            if(devicesHtml.contains("您还未登录或会话过期")){
                devices = new Device[1];
                devices[0] = new Device();
                devices[0].deviceName ="登陆失败";
            }
            else{
                String regex = "id=\"([^\"]*)\"";
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(devicesHtml);
                while (matcher.find()) {
                    String single = matcher.group(1);
                    if (single.contains("divdiv")){
                        String deviceID = single.replace("divdiv", "");
                        devices[index] = new Device(); // 创建并实例化Device对象
                        devices[index].deviceID = deviceID;
                        index++;
                    }
                }

                devices = Arrays.stream(devices).filter(Objects::nonNull).toArray(Device[]::new);
                if (devices.length == 0){
                    devices = new Device[1];
                    devices[0] = new Device();
                    devices[0].deviceName ="无设备";
                }
                else{
                    for (Device device :devices){
                        String name = Re("<input id=\"inputId" + device.deviceID + "\" type=\"hidden\" value=\"(.*?)\">",devicesHtml);
                        String IP = Re("<input id=\"userIp4" + device.deviceID + "\" type=\"hidden\" value=\"(.*?)\">",devicesHtml);
                        String MAC = Re("<input id=\"usermac" + device.deviceID + "\" type=\"hidden\" value=\"(.*?)\">",devicesHtml);
                        String time = Re("<input id=\"createTimeStr" + device.deviceID + "\" type=\"hidden\" value=\"(.*?)\">",devicesHtml);
                        device.deviceName = name;device.ipAddress = IP;device.macAddress = MAC;device.lastOnlineTime = time;
                    }
                }
                GetCookie();
            }
        }catch (IOException |ParseException e){
            e.printStackTrace();
            devices = new Device[1];
            devices[0] = new Device();
            devices[0].deviceName ="登陆失败";
        }
        return devices;
    }

}
