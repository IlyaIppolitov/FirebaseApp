package com.example.firebaseapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthSettings;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private EditText phoneNumberEditText, codeEditText;
    private Button sendCodeButton, verifyCodeButton;
    private FirebaseAuth mAuth;
    private String verificationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        phoneNumberEditText = findViewById(R.id.phoneNumberEditText);
        codeEditText = findViewById(R.id.codeEditText);
        sendCodeButton = findViewById(R.id.sendCodeButton);
        verifyCodeButton = findViewById(R.id.verifyCodeButton);

        mAuth = FirebaseAuth.getInstance();

        sendCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = phoneNumberEditText.getText().toString().trim();
                if (TextUtils.isEmpty(phoneNumber)) {
                    phoneNumberEditText.setError("Введите номер телефона");
                    return;
                }
                sendVerificationCode(phoneNumber);
            }
        });

        verifyCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code = codeEditText.getText().toString().trim();
                if (TextUtils.isEmpty(code)) {
                    codeEditText.setError("Введите код");
                    return;
                }
                verifyCode(code);
            }
        });
    }

    private void sendVerificationCode(String phoneNumber) {

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                    @Override
                    public void onCodeSent(@NonNull String verificationId,
                                           @NonNull PhoneAuthProvider.ForceResendingToken token) {
                        MainActivity.this.verificationId = verificationId;
                        Toast.makeText(MainActivity.this, "Код отправлен", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                        signInWithPhoneAuthCredential(phoneAuthCredential);
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }).build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void verifyCode(String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            String phoneNumber = task.getResult().getUser().getPhoneNumber();
                            Toast.makeText(MainActivity.this, "Успех: " + phoneNumber, Toast.LENGTH_LONG).show();
                        } else {
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                Toast.makeText(MainActivity.this, "Провал! данные не совпали!", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(MainActivity.this, "Провал: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
    }
}
