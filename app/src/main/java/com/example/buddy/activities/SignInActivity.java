package com.example.buddy.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;


import com.example.buddy.R;
import com.example.buddy.databinding.ActivitySignInBinding;
import com.example.buddy.utilities.Constants;
import com.example.buddy.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class SignInActivity extends AppCompatActivity {

    private ActivitySignInBinding binding;
    private PreferenceManager preferenceManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferenceManager = new PreferenceManager(getApplicationContext());
        if(preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
        binding= ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();

    }
    private void setListeners(){
        binding.textCreateNewAccount.setOnClickListener(view ->
                startActivity(new Intent(getApplicationContext(), SignUpActivity.class)));
        binding.buttonSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isValidSignInDetails()){
                    signIn();
                }
            }
        });

    }

    private void signIn(){
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();

        database.collection(Constants.KEY_COLLECTION_USERS).whereEqualTo(Constants.KEY_EMAIL, binding.inputEmail.getText().toString()).whereEqualTo(Constants.KEY_PASSWORD, binding.inputPassword.getText().toString()).get().addOnCompleteListener(task -> {
            if(task.isSuccessful() && task.getResult() != null && task.getResult().size() > 0){
                DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                preferenceManager.putString(Constants.KEY_USER_ID, documentSnapshot.getId());
                preferenceManager.putString(Constants.KEY_NAME, documentSnapshot.getString(Constants.KEY_NAME));
                preferenceManager.putString(Constants.KEY_IMAGE, documentSnapshot.getString(Constants.KEY_IMAGE));
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
            else{
                loading(false);
                showToast("Unable to sign in. Make sure you've entered correct email or password");
            }
        });

    }

    private void loading(Boolean isLoading){
        if(isLoading){
            binding.buttonSignIn.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        }
        else{
            binding.buttonSignIn.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private Boolean isValidSignInDetails(){
        if(binding.inputEmail.getText().toString().trim().isEmpty()){
            showToast("Enter email");
            return false;
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()){
            showToast("Enter valid email");
            return false;
        }
        else if(binding.inputPassword.getText().toString().trim().isEmpty()){
            showToast("Enter password");
            return false;
        }
        return true;
    }


}