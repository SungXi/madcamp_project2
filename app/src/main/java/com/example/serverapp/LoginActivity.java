package com.example.serverapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.google.android.material.snackbar.Snackbar;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.json.JSONException;
import org.json.JSONObject;

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
    public void onBackPressed() {
        finish();
        Intent resultIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, resultIntent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Service
        Retrofit retrofitClient = RetrofitClient.getInstance();
        iAppService = retrofitClient.create(IAppService.class);

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
            private String[] data = new String[4];

            @Override
            public void onSuccess(LoginResult loginResult) {
                requestMe(loginResult.getAccessToken());
                String ownerEmail = data[0];
                Intent resultIntent = new Intent();
                resultIntent.putExtra("owner_email", ownerEmail);
                Log.e("Callback :: ", "Email : " + ownerEmail);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }

            @Override
            public void requestMe(AccessToken token) {
                GraphRequest graphRequest = GraphRequest.newMeRequest(token,
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                try {
                                    data[0] = object.getString("id");
                                    data[1] = object.getString("name");
                                    data[2] = object.getString("email");
                                    data[3] = object.getJSONObject("picture")
                                            .getJSONObject("data").getString("url");
                                } catch (JSONException ex) {
                                    ex.printStackTrace();
                                }
                                Log.e("result", object.toString());
                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,email,picture.width(200)");
                graphRequest.setParameters(parameters);
                graphRequest.executeAsync();
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

        Intent resultIntent = new Intent();
        Log.e("Callback :: ", "Email : " + savedEmail);
        resultIntent.putExtra("owner_email", savedEmail);
        setResult(Activity.RESULT_OK, resultIntent);
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
}
