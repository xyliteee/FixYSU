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
    private String userName;
    private static LoginFunctions instance;
    CookieStore cookieStore = new BasicCookieStore();
    private CloseableHttpClient httpClient = HttpClients.custom().setDefaultCookieStore(cookieStore).build();

    private String  codeUrl = "https://serv.ysu.edu.cn/selfservice/common/web/verifycode.jsp";

    private String judgeUrl = "https://serv.ysu.edu.cn/selfservice/module/scgroup/web/login_judge.jsf";
    private String onlineDevicesUrl = "https://serv.ysu.edu.cn/selfservice/module/webcontent/web/onlinedevice_list.jsf";

    public static LoginFunctions getInstance() {
        if (instance == null) {
            instance = new LoginFunctions();
        }
        return instance;
    }
    public Bitmap GetVerifyCode(){
        Bitmap verifyCodeImage = null;
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
    public String JudgeLogin(String userName, String encryptedPassword, String verifyCode) {
        String result = null;
        this.userName = userName;
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
    public Device[] GetDevicesList(){
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
        }catch (IOException |ParseException e){
            e.printStackTrace();
        }
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
        return devices;
    }

    public void KickDevice(String IP){
        String url = "https://serv.ysu.edu.cn/selfservice/module/userself/web/userself_ajax.jsf?methodName=indexBean.kickUserBySelfForAjax";
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36");
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("key", this.userName+":"+IP));
        httpPost.setEntity(new UrlEncodedFormEntity(params));
        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            HttpEntity entity = response.getEntity();
            EntityUtils.consume(entity);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public String Re(String regex,String input){
        String result = "null";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            result = matcher.group(1);
        }
        return result;
    }
}
