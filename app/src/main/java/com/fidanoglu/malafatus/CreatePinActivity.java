package com.fidanoglu.malafatus;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.security.NoSuchAlgorithmException;

public class CreatePinActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    StorageReference storageReference;
    DatabaseReference databaseReference;
    DatabaseReference userReference;
    FirebaseStorage firebaseStorage;

    PinEntryEditText pinField;
    ImageView digit1, digit2, digit3, digit4;
    Button button1, button2, button3, button4, button5, button6,
            button7, button8, button9, button0, buttonDelete;

    private String newPin;
    String newPinHash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_pin);

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        pinField = findViewById(R.id.createPin);

        button0 = findViewById(R.id.button0Create);
        button1 = findViewById(R.id.button1Create);
        button2 = findViewById(R.id.button2Create);
        button3 = findViewById(R.id.button3Create);
        button4 = findViewById(R.id.button4Create);
        button5 = findViewById(R.id.button5Create);
        button6 = findViewById(R.id.button6Create);
        button7 = findViewById(R.id.button7Create);
        button8 = findViewById(R.id.button8Create);
        button9 = findViewById(R.id.button9Create);
        buttonDelete = findViewById(R.id.buttonDeleteCreate);

        digit1 = findViewById(R.id.digit1Create);
        digit2 = findViewById(R.id.digit2Create);
        digit3 = findViewById(R.id.digit3Create);
        digit4 = findViewById(R.id.digit4Create);

        // Button functions
        button0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pinField.append("0");
            }
        });
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pinField.append("1");
            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pinField.append("2");
            }
        });
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pinField.append("3");
            }
        });
        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pinField.append("4");
            }
        });
        button5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pinField.append("5");
            }
        });
        button6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pinField.append("6");
            }
        });
        button7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pinField.append("7");
            }
        });
        button8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pinField.append("8");
            }
        });
        button9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pinField.append("9");
            }
        });
        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentPin = pinField.getText().toString().substring(0, pinField.getText().length() - 1);
                pinField.setText(currentPin);
            }
        });

        if(user == null)
        startActivity(new Intent(CreatePinActivity.this, SignUpActivity.class));

        pinField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                newPin = pinField.getText().toString().trim();
                if (newPin.length() == 0) {
                    digit1.setImageResource(R.drawable.pin_display_digit);
                    digit2.setImageResource(R.drawable.pin_display_digit);
                    digit3.setImageResource(R.drawable.pin_display_digit);
                    digit4.setImageResource(R.drawable.pin_display_digit);
                }
                if (newPin.length() == 1) {
                    digit1.setImageResource(R.drawable.pin_display_digit_entered);
                    digit2.setImageResource(R.drawable.pin_display_digit);
                    digit3.setImageResource(R.drawable.pin_display_digit);
                    digit4.setImageResource(R.drawable.pin_display_digit);
                }
                if (newPin.length() == 2) {
                    digit1.setImageResource(R.drawable.pin_display_digit_entered);
                    digit2.setImageResource(R.drawable.pin_display_digit_entered);
                    digit3.setImageResource(R.drawable.pin_display_digit);
                    digit4.setImageResource(R.drawable.pin_display_digit);
                }
                if (newPin.length() == 3) {
                    digit1.setImageResource(R.drawable.pin_display_digit_entered);
                    digit2.setImageResource(R.drawable.pin_display_digit_entered);
                    digit3.setImageResource(R.drawable.pin_display_digit_entered);
                    digit4.setImageResource(R.drawable.pin_display_digit);
                }
                if (newPin.length() == 4) {
                    digit1.setImageResource(R.drawable.pin_display_digit_entered);
                    digit2.setImageResource(R.drawable.pin_display_digit_entered);
                    digit3.setImageResource(R.drawable.pin_display_digit_entered);
                    digit4.setImageResource(R.drawable.pin_display_digit_entered);
                    if (user != null) {
                        databaseReference = FirebaseDatabase.getInstance().getReference();
                        firebaseStorage = FirebaseStorage.getInstance();
                        storageReference = firebaseStorage.getReference();

                        newPin = pinField.getText().toString().trim();
                        try {
                            newPinHash = HashGenerator.sha256(newPin);
                        } catch (NoSuchAlgorithmException e) {
                            e.printStackTrace();
                        }
                        userReference = databaseReference.child(user.getUid());
                        userReference.child("pinHash").setValue(newPinHash);

                        MainActivity.profileJustCreated = true;
                        Toast.makeText(CreatePinActivity.this, "Your profile has been created.", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(CreatePinActivity.this, MainActivity.class));
                    }
                }
            }
        });
    }
}
