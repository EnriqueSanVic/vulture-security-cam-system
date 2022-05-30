package com.example.vultureapp.Views;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.vultureapp.Controllers.MainController;
import com.example.vultureapp.R;


/**
 * Vista de loggin
 */
public class MainActivity extends AppCompatActivity {

    private EditText inputEmail;
    private EditText inputPassword;
    private Button btnLogin;

    private MainController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        confAppBar();

        controller = new MainController(this);

        findElements();
    }

    private void confAppBar(){
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);
    }

    private void findElements() {

        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(controller);

        inputEmail.setText("enriquesanvic@gmail.com");
        inputPassword.setText("password");

    }


    public void goToCamListView(){

        Intent intent = new Intent(this, ListCamActivity.class);

        startActivity(intent);

    }


    public void showNoAuthMessage() {

        Toast.makeText(this, "Incorrect credentials.", Toast.LENGTH_SHORT).show();

    }

    public String getMail() {
        return inputEmail.getText().toString();
    }

    public String getPassword() {
        return inputPassword.getText().toString();
    }

    public void clearPassword() {
        inputPassword.setText("");
    }
}