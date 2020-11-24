package com.fidanoglu.malafatus;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.NoSuchAlgorithmException;

public class SignUpActivity extends AppCompatActivity {

    EditText emailField, passwordField;
    Button signUpButton, forgotButton;
    private SignInButton googleButton;
    private GoogleSignInClient googleSignInClient;
    private String TAG = "SignUpActivity";
    private FirebaseAuth auth;
    private int RC_SIGN_IN = 1;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private FirebaseStorage firebaseStorage;
    public String hash;

    public static UserInformation userInformation;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        emailField = findViewById(R.id.email_sign_up);
        passwordField = findViewById(R.id.password_sign_up);
        auth = FirebaseAuth.getInstance();
        signUpButton = findViewById(R.id.signUpButton);
        googleButton = findViewById(R.id.sign_in_google);

        forgotButton = findViewById(R.id.forgotButton);
        forgotButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignUpActivity.this, ForgotPasswordActivity.class));
            }
        });

        googleButton.setBackgroundResource(R.drawable.modern_button_blue);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        googleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailField.getText().toString();
                final String password = passwordField.getText().toString();

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplicationContext(), "Please enter your e-mail address.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(password))
                    Toast.makeText(getApplicationContext(), "Please enter your password.", Toast.LENGTH_SHORT).show();
                if (password.length() == 0)
                    Toast.makeText(getApplicationContext(), "Please enter your password.", Toast.LENGTH_SHORT).show();
                if (password.length() < 8)
                    Toast.makeText(getApplicationContext(), "Password must be at least 8 characters long.", Toast.LENGTH_SHORT).show();
                else {

                    auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (!task.isSuccessful()) {
                                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                            auth.signInWithEmailAndPassword(emailField.getText().toString(), passwordField.getText().toString())
                                                    .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                                            if (!task.isSuccessful())
                                                                Toast.makeText(SignUpActivity.this, "Error! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                            else {
                                                                startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                                                                Toast.makeText(SignUpActivity.this, "Welcome Back!", Toast.LENGTH_SHORT).show();
                                                                finish();
                                                            }
                                                        }
                                                    });
                                        } else {
                                            Toast.makeText(SignUpActivity.this, "Error! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        try {
                                            hash = HashGenerator.sha256(passwordField.getText().toString().trim());
                                        } catch (NoSuchAlgorithmException e) {
                                            e.printStackTrace();
                                        }

                                        // We create a new user object that we will later send to the database.
                                        userInformation = new UserInformation(
                                                null, null, null, emailField.getText().toString().trim(), hash, null);
                                        startActivity(new Intent(SignUpActivity.this, EditProfileActivity.class));
                                        finish();
                                    }
                                }
                            });
                }
            }
        });
    }

    private void signIn() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> task) {
        try {
            GoogleSignInAccount acc = task.getResult(ApiException.class);
            Toast.makeText(SignUpActivity.this, "Signed in successfully.", Toast.LENGTH_SHORT).show();
            firebaseGoogleAuth(acc);
        } catch (ApiException e) {
            Toast.makeText(SignUpActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            firebaseGoogleAuth(null);
        }
    }

    private void firebaseGoogleAuth(GoogleSignInAccount acc) {
        final AuthCredential authCredential = GoogleAuthProvider.getCredential(acc.getIdToken(), null);
        auth.signInWithCredential(authCredential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (!task.isSuccessful())
                    Toast.makeText(SignUpActivity.this, "Error! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                else {
                    FirebaseUser user = auth.getCurrentUser();
                    userInformationGoogle(user);
                    startActivity(new Intent(SignUpActivity.this, CreatePinActivity.class));
                    Toast.makeText(SignUpActivity.this, "Welcome!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });
    }

    // If the user has signed in with Google, they will not be taken to the EditProfileActivity. Thus, their details should be saved now.
    private void userInformationGoogle(FirebaseUser user) {
        String[] names = user.getDisplayName().split(" ");
        String surname = names[names.length - 1].trim();
        String name = "";
        for (int i = 0; i < names.length - 1; i++) {
            name += names[i] + " ";
        }
        name.trim();
        String email = user.getEmail();
        UserInformation userInformation = new UserInformation(name, surname, null, email, null, null);
        databaseReference.child(user.getUid()).setValue(userInformation);
        StorageReference imageReference = storageReference.child(user.getUid()).child("Images").child("Avatar");
        Bitmap avatar;
        Uri path;
        try {
            URL url = new URL(user.getPhotoUrl().getPath());
            avatar = getBitmapFromURL(url);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            avatar.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();
//            path = Uri.parse(saveInCache(avatar).getPath());
            UploadTask uploadTask = imageReference.putBytes(data);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(SignUpActivity.this, "Error: uploading avatar", Toast.LENGTH_SHORT).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(SignUpActivity.this, "Profile picture uploaded", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Bitmap getBitmapFromURL(URL url) {
        try {
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);

            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public File saveInCache(Bitmap pic) {
        File cacheDir = getBaseContext().getCacheDir();
        File f = new File(cacheDir, "pic");

        try {
            FileOutputStream out = new FileOutputStream(
                    f);
            pic.compress(
                    Bitmap.CompressFormat.JPEG,
                    100, out);
            out.flush();
            out.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cacheDir;
    }
}
