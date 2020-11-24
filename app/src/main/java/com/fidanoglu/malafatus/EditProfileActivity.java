package com.fidanoglu.malafatus;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.w3c.dom.Text;

import java.io.IOException;

public class EditProfileActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = EditProfileActivity.class.getSimpleName();
    Button saveButton;
    private FirebaseAuth auth;
    private TextView displayEmail;
    private EditText nameField, surnameField, dateOfBirthField;
    private ImageView avatarField;
    private FirebaseStorage firebaseStorage;

    private static int PICK_IMAGE = 123;

    public static Uri imagePath;
    private StorageReference storageReference;
    private DatabaseReference databaseReference;

    public EditProfileActivity() {
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data.getData() != null) {
            imagePath = data.getData();
            try {
                ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), imagePath);
                avatarField.setImageBitmap(ImageDecoder.decodeBitmap(source));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            finish();
            startActivity(new Intent(getApplicationContext(), SignUpActivity.class));
        }

        databaseReference = FirebaseDatabase.getInstance().getReference();

        nameField = findViewById(R.id.first_name_field);
        surnameField = findViewById(R.id.surname_field);
        dateOfBirthField = findViewById(R.id.date_of_birth_field);
        saveButton = findViewById(R.id.saveUserButton);
        displayEmail = findViewById(R.id.display_email_field);
        avatarField = findViewById(R.id.avatarView);

        FirebaseUser user = auth.getCurrentUser();
        displayEmail.setText(user.getEmail());

        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        saveButton.setOnClickListener(this);

        avatarField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent profileIntent = new Intent();
                profileIntent.setType("image/*");
                profileIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(profileIntent, "Select Avatar"), PICK_IMAGE);
            }
        });
    }

    private void userInformation() {
        String name = nameField.getText().toString().trim();
        String surname = surnameField.getText().toString().trim();
        String dateOfBirth = dateOfBirthField.getText().toString().trim();
        if (TextUtils.isEmpty(name))
            Toast.makeText(EditProfileActivity.this, "Please enter your name.", Toast.LENGTH_SHORT).show();
        else if (TextUtils.isEmpty(surname))
            Toast.makeText(EditProfileActivity.this, "Please enter your surname.", Toast.LENGTH_SHORT).show();
        else if (TextUtils.isEmpty(dateOfBirth))
            Toast.makeText(EditProfileActivity.this, "Please enter your date of birth.", Toast.LENGTH_SHORT).show();
        else {
            FirebaseUser user = auth.getCurrentUser();
            UserInformation userInformation = SignUpActivity.userInformation;
            userInformation.setFirstName(name);
            userInformation.setSurname(surname);
            userInformation.setDateOfBirth(dateOfBirth);
            databaseReference.child(user.getUid()).setValue(SignUpActivity.userInformation);
            Toast.makeText(EditProfileActivity.this, "Your profile has been updated.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View view) {
        if (view == saveButton) {
            if (imagePath == null) {
                Drawable drawable = this.getResources().getDrawable(R.drawable.account_icon, null);
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.modern_button_red);
//                openSelectProfilePictureDialog();
                userInformation();
//                sendUserData();
                finish();
                startActivity(new Intent(EditProfileActivity.this, CreatePinActivity.class));
            } else {
                userInformation();
                sendUserData();
                finish();
                startActivity(new Intent(EditProfileActivity.this, CreatePinActivity.class));
            }
        }
    }

    private void sendUserData() {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        // Get "User UID" from Firebase -> Authentification -> Users
        DatabaseReference databaseReference = firebaseDatabase.getReference(auth.getUid());
        StorageReference imageReference = storageReference.child(auth.getUid()).child("Images").child("Avatar");
        UploadTask uploadTask = imageReference.putFile(imagePath);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(EditProfileActivity.this, "Error: uploading avatar", Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(EditProfileActivity.this, "Profile picture uploaded", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void openSelectProfilePictureDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        TextView title = new TextView(this);
        title.setText("Profile Picture");
        title.setPadding(10, 10, 10, 10);   // Set Position
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.BLACK);
        title.setTextSize(20);
        alertDialog.setCustomTitle(title);
        TextView msg = new TextView(this);
        msg.setText("Please select a profile picture \n Tap the sample avatar logo");
        msg.setGravity(Gravity.CENTER_HORIZONTAL);
        msg.setTextColor(Color.BLACK);
        alertDialog.setView(msg);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // Perform Action on Button
            }
        });
        new Dialog(getApplicationContext());
        alertDialog.show();
        final Button okBT = alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL);
        LinearLayout.LayoutParams neutralBtnLP = (LinearLayout.LayoutParams) okBT.getLayoutParams();
        neutralBtnLP.gravity = Gravity.FILL_HORIZONTAL;
        okBT.setPadding(50, 10, 10, 10);   // Set Position
        okBT.setTextColor(Color.BLUE);
        okBT.setLayoutParams(neutralBtnLP);
    }
}
