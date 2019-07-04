package com.example.serverapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.example.serverapp.Retrofit.IAppService;
import com.example.serverapp.Retrofit.RetrofitClient;
import com.facebook.CallbackManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.google.android.material.snackbar.Snackbar;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.security.MessageDigest;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;

public class LoginActivity extends AppCompatActivity {

    private ImageView createAccount;
    private MaterialEditText loginEmail;
    private MaterialEditText loginPassword;
    private Button loginButton;
    private LoginButton facebookLoginButton;
    private CallbackManager callbackManager;
    private Boolean saveLogin;
    private CheckBox saveLoginCheckbox;
    private SharedPreferences loginPreferences;
    private SharedPreferences.Editor loginPrefsEditor;
    private View loginView;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private IAppService iAppService;

    @Override
    protected void onStop() {
        compositeDisposable.clear();
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Service
        Retrofit retrofitClient = RetrofitClient.getInstance();
        iAppService = retrofitClient.create(IAppService.class);
//        getAppKeyHash();

        loginEmail = findViewById(R.id.emailText);
        loginPassword = findViewById(R.id.passwordText);
        loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginView = view;
                loginUser (loginEmail.getText().toString(),
                        loginPassword.getText().toString());
            }
        });

        facebookLoginButton = findViewById(R.id.facebookButton);
        callbackManager = CallbackManager.Factory.create();
        facebookLoginButton.registerCallback(callbackManager, new LoginCallback() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.e("Callback :: ", "onSuccess");
                requestMe(loginResult.getAccessToken());
                finish();
            }
        });

        saveLoginCheckbox = findViewById(R.id.loginCheckbox);
        loginPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        loginPrefsEditor = loginPreferences.edit();
        saveLogin = loginPreferences.getBoolean("saveLogin", false);
        if (saveLogin == true) {
            loginEmail.setText(loginPreferences.getString("email", ""));
            loginPassword.setText(loginPreferences.getString("password", ""));
            saveLoginCheckbox.setChecked(true);
        }

        createAccount = findViewById(R.id.registerImage);
        createAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final View registerView = LayoutInflater.from(LoginActivity.this)
                        .inflate(R.layout.register_layout, null);

                new MaterialStyledDialog.Builder(LoginActivity.this)
                        .setIcon(R.drawable.ic_register)
                        .setTitle("Registration")
                        .setDescription("Please fill all fields.")
                        .setCustomView(registerView)
                        .setNegativeText("Cancel")
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();
                            }
                        })
                        .setPositiveText("Register")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                MaterialEditText editRegisterEmail = registerView.findViewById(R.id.registerEmailText);
                                MaterialEditText editRegisterName = registerView.findViewById(R.id.registerNameText);
                                MaterialEditText editRegisterPassword = registerView.findViewById(R.id.registerPasswordText);

                                if (TextUtils.isEmpty(editRegisterEmail.getText().toString())) {
                                    Toast.makeText(LoginActivity.this, "Email cannot be null or empty.", Toast.LENGTH_LONG)
                                            .show();
                                    dialog.dismiss();
                                    return;
                                } else if (TextUtils.isEmpty(editRegisterName.getText().toString())) {
                                    Toast.makeText(LoginActivity.this, "Name cannot be null or empty.", Toast.LENGTH_LONG)
                                            .show();
                                    dialog.dismiss();
                                    return;
                                } else if (TextUtils.isEmpty(editRegisterPassword.getText().toString())) {
                                    Toast.makeText(LoginActivity.this, "Password cannot be null or empty.", Toast.LENGTH_LONG)
                                            .show();
                                    dialog.dismiss();
                                    return;
                                }

                                registerUser(editRegisterEmail.getText().toString(),
                                        editRegisterName.getText().toString(),
                                        editRegisterPassword.getText().toString());
                            }
                        }).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void loginUser(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            Snackbar.make(loginView, "Email cannot be null or empty.", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            return;
        } else if (TextUtils.isEmpty(password)) {
            Snackbar.make(loginView, "Password cannot be null or empty.", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            return;
        }

        compositeDisposable.add(iAppService.loginUser(email, password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String response) throws Exception {
                        Snackbar.make(loginView, response.replace("\"", ""), Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                }));

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(loginEmail.getWindowToken(), 0);

        String savedEmail = loginEmail.getText().toString();
        String savedPassword = loginPassword.getText().toString();

        if (saveLoginCheckbox.isChecked()) {
            loginPrefsEditor.putBoolean("saveLogin", true);
            loginPrefsEditor.putString("email", savedEmail);
            loginPrefsEditor.putString("password", savedPassword);
            loginPrefsEditor.commit();
        } else {
            loginPrefsEditor.clear();
            loginPrefsEditor.commit();
        }

        finish();
    }

    private void registerUser(String email, String name, String password) {
        compositeDisposable.add(iAppService.registerUser(email, name, password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String response) throws Exception {
                        Toast.makeText(LoginActivity.this, response.replace("\"", ""), Toast.LENGTH_LONG)
                                .show();
                    }
                }));
    }

//    private void getAppKeyHash() {
//        try {
//            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
//            for (Signature signature : info.signatures) {
//                MessageDigest md;
//                md = MessageDigest.getInstance("SHA");
//                md.update(signature.toByteArray());
//                String something = new String(Base64.encode(md.digest(), 0));
//                Log.e("Hash key", something);
//            }
//        } catch (Exception e) {
//            Log.e("Name not found", e.toString());
//        }
//    }
}

// For Logout
//btn_custom_logout = (Button) findViewById(R.id.btn_custom_logout);
//btn_custom_logout.setOnClickListener(new View.OnClickListener() {
//    @Override
//    public void onClick(View view) {
//        LoginManager.getInstance().logOut();
//    }
//});
