package com.Xyliteee.fixysu;
import android.annotation.SuppressLint;
import android.graphics.*;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.*;
import android.view.View;
import java.util.concurrent.*;
import android.app.AlertDialog;
import android.content.*;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;

public class MainActivity extends AppCompatActivity {
    private EditText usernameBox;
    private EditText passwordBox;
    private EditText verifyCodeBox;
    private WebView mWebview;
    private String encryptedPassword = "Init";
    private String password;
    private String username;
    private String verifyCode;
    private SharedPreferences sharedPreferences;
    private int loadingCodeFlag = 0;
    private long mExitTime = 0;
    private long mRefreshTime = 0;
    private ImageButton reFreshButton;
    @SuppressLint("UseSwitchCompatOrMaterialCode")

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupOnBackPressedCallback();
        sharedPreferences = this.getSharedPreferences("loginPrefs", MODE_PRIVATE);
        passwordBox = findViewById(R.id.PasswordBox);
        usernameBox = findViewById(R.id.UsernameBox);
        usernameBox.setText(sharedPreferences.getString("username", ""));
        passwordBox.setText(sharedPreferences.getString("password", ""));
        mWebview = findViewById(R.id.webView);
        verifyCodeBox = findViewById(R.id.VerificationCodeBox);
        reFreshButton = findViewById(R.id.button2);
        GetVerifyCodeImage();
    }

    private void setupOnBackPressedCallback() {
        OnBackPressedDispatcher dispatcher = getOnBackPressedDispatcher();
        dispatcher.addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if ((System.currentTimeMillis() - mExitTime) > 2000) {
                    Toast.makeText(MainActivity.this, "再次操作以返回桌面", Toast.LENGTH_SHORT).show();
                    mExitTime = System.currentTimeMillis();
                } else {
                    finish();
                    System.exit(0);
                }
            }
        });
    }

    public void Login(View view){
        password = passwordBox.getText().toString();
        username = usernameBox.getText().toString();
        verifyCode = verifyCodeBox.getText().toString();
        Toast.makeText(MainActivity.this, "登陆中......", Toast.LENGTH_SHORT).show();
        GetEncryptedPassword(password);//获取加密后的密码并且调用爬虫登录
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void GetEncryptedPassword(String password){
        mWebview.getSettings().setJavaScriptEnabled(true);
        mWebview.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                mWebview.evaluateJavascript("javascript:callTest('" + password + "')", value -> {
                    encryptedPassword = value.replace("\"","");
                    JudgeLogin();
                });
            }
        });
        mWebview.loadUrl("file:///android_asset/javascript.html");
    }

    public void RefreshCode(View view){
        if (System.currentTimeMillis() - mRefreshTime >5000){
            GetVerifyCodeImage();
        }
        else{
            Toast.makeText(MainActivity.this, "不要频繁刷新验证码！！", Toast.LENGTH_SHORT).show();
        }
    }
    public void GetVerifyCodeImage(){
        mRefreshTime = System.currentTimeMillis();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        loadingCodeFlag = 0;
        executor.execute(() -> {
            LoadingCodeCheck();
            final Bitmap bitmap = LoginFunctions.GetVerifyCode();
            handler.post(() -> {
                if(bitmap != null)
                {
                    loadingCodeFlag = 1;
                    reFreshButton.setAlpha(1.0f);
                    reFreshButton.setPadding(0, 0, 0, 0);
                    reFreshButton.setScaleType(ImageView.ScaleType.FIT_XY);
                    reFreshButton.setImageBitmap(bitmap);
                }
            });
        });
    }

    public void JudgeLogin(){
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            final String judgeHtml = LoginFunctions.JudgeLogin(username,encryptedPassword,verifyCode);
            handler.post(() -> {
                if(judgeHtml.contains("verifyError=true")){
                    ShowLoginMessage("验证码错误");
                }
                else if (judgeHtml.contains("errorMsg=用户不存在或密码错误")) {
                    ShowLoginMessage("用户不存在或密码错误");
                }
                else {
                    Toast.makeText(MainActivity.this, "跳转中", Toast.LENGTH_SHORT).show();
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("username",username);
                    editor.putString("password",password);
                    editor.apply();
                    Intent intent = new Intent(MainActivity.this, LoadingActivity.class);
                    intent.putExtra("autoLogin",false);
                    startActivity(intent);
                }
            });
        });
    }
    private void  LoadingCodeCheck(){
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try{Thread.sleep(5000);}catch (Exception ignored){}
            handler.post(() -> {
                if (loadingCodeFlag == 0){
                    Toast.makeText(MainActivity.this, "获取验证码超时，检查网络连接", Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    public void ShowMessage(View view){
        new AlertDialog.Builder(this)
                .setTitle("关于软件的使用")
                .setMessage("请在接入学校网络后再使用该软件（无需登录学校网络，但鉴于大部分手机没有登录网络后会直接走流量，因此还是建议至少先登录校园网，或者关闭流量）；\n在经历了不少优化与debug后，应该可靠性高不少了，但一定存在仍未解决的问题或者待优化，希望告之；\n荷取可爱捏😋")
                .setPositiveButton("确定", (dialog, which) -> {
                })
                .show();
    }
    public void ShowLoginMessage(String msg){
        new AlertDialog.Builder(this)
                .setTitle("消息提示")
                .setMessage(msg)
                .setPositiveButton("确定", (dialog, which) -> GetVerifyCodeImage())
                .show();

    }
}



