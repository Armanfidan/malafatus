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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.NoSuchAlgorithmException;

public class EnterPinActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    DatabaseReference databaseReference;

    PinEntryEditText pinField;
    ImageView digit1, digit2, digit3, digit4;
    Button button1, button2, button3, button4, button5, button6,
            button7, button8, button9, button0, buttonDelete;

    String pinHash;
    private String newPin;
    String newPinHash;

    Boolean alreadySignedIn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_pin);

        pinField = findViewById(R.id.editPin);

        button0 = findViewById(R.id.button0);
        button1 = findViewById(R.id.button1);
        button2 = findViewById(R.id.button2);
        button3 = findViewById(R.id.button3);
        button4 = findViewById(R.id.button4);
        button5 = findViewById(R.id.button5);
        button6 = findViewById(R.id.button6);
        button7 = findViewById(R.id.button7);
        button8 = findViewById(R.id.button8);
        button9 = findViewById(R.id.button9);
        buttonDelete = findViewById(R.id.buttonDelete);

        digit1 = findViewById(R.id.digit1);
        digit2 = findViewById(R.id.digit2);
        digit3 = findViewById(R.id.digit3);
        digit4 = findViewById(R.id.digit4);

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
                String currentPin = pinField.getText().toString();
                if (currentPin.length() > 0) currentPin = currentPin.substring(0, pinField.getText().length() - 1);
                pinField.setText(currentPin);
            }
        });

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        final DatabaseReference userReference;

//         Don't forget to change this!
//         Value Event Listeners are asynchronous - it's not a good idea to write anything to a global variable
//         as we don't know when the function will return its data. This needs to be in a callback function, which
//         itself needs to be in an interface.
        if (user != null) {
            databaseReference = FirebaseDatabase.getInstance().getReference();
            userReference = databaseReference.child(user.getUid());
            ValueEventListener listener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    pinHash = dataSnapshot.getValue(String.class);
                    
                    if (user != null && pinHash == null) {
                        startActivity(new Intent(EnterPinActivity.this, CreatePinActivity.class));
                    } else if (user == null)
                        startActivity(new Intent(EnterPinActivity.this, SignUpActivity.class));
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    System.out.println("The read failed: " + databaseError.getCode());
                }
            };
            userReference.child("pinHash").addListenerForSingleValueEvent(listener);
        }

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
                        if (pinHash != null && !alreadySignedIn) {
                            try {
                                newPinHash = HashGenerator.sha256(newPin);
                            } catch (NoSuchAlgorithmException e) {
                                e.printStackTrace();
                            }

                            if (newPinHash.equals(pinHash)) {
                                alreadySignedIn = true;
                                finish();
                                MainActivity.profileJustCreated = true;
                                startActivity(new Intent(EnterPinActivity.this, MainActivity.class));
                            } else {
                                Toast.makeText(EnterPinActivity.this, "Incorrect PIN!", Toast.LENGTH_SHORT).show();
                                pinField.getText().clear();
                            }
                        }
                    }
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
    }
}