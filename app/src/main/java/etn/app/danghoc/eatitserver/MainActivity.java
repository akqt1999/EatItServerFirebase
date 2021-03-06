package etn.app.danghoc.eatitserver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.List;

import dmax.dialog.SpotsDialog;
import etn.app.danghoc.eatitserver.common.Common;
import etn.app.danghoc.eatitserver.model.ServerUserModel;

public class MainActivity extends AppCompatActivity {

    private static int APP_REQUEST_CODE = 7171;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener listener;
    private AlertDialog dialog;
    private DatabaseReference serverRef;
    private List<AuthUI.IdpConfig> providers;

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(listener);
    }

    @Override
    protected void onStop() {
        if (listener != null)
            firebaseAuth.removeAuthStateListener(listener);
        super.onStop();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        providers = Arrays.asList(new AuthUI.IdpConfig.PhoneBuilder().build()
                ,new AuthUI.IdpConfig.EmailBuilder().build()

        );

        serverRef = FirebaseDatabase.getInstance().getReference(Common.SERVER_REF);
        firebaseAuth = FirebaseAuth.getInstance();
        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        listener = firebaseAuthLocal -> {
            FirebaseUser user = firebaseAuthLocal.getCurrentUser();
            if (user != null) {
                CheckServerUserFirebase(user);
            } else {
                phoneLogin();
            }
        };

    }

    private void CheckServerUserFirebase(FirebaseUser user) {
        dialog.show();
        serverRef.child(user.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (snapshot.exists()) {
                            ServerUserModel userModel=snapshot.getValue(ServerUserModel.class);
                            if(userModel.isActive())
                            {
                                gotoHomeActivity(userModel);
                            }
                            else
                            {
                                Toast.makeText(MainActivity.this, "You must be allowed from Admin to access", Toast.LENGTH_SHORT).show();
                            }

                        } else {
                            dialog.dismiss();
                            showRegisterDialog(user);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        dialog.dismiss();
                        Toast.makeText(MainActivity.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showRegisterDialog(FirebaseUser user) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Register");
        builder.setMessage("Please fill information \n Admin will accept your account late");

        //set data
        View itemView = LayoutInflater.from(this).inflate(R.layout.layout_register, null);
        TextInputLayout  phone_input_layout=itemView.findViewById(R.id.phone_input_layout);

        EditText edt_name = itemView.findViewById(R.id.edt_name);
        EditText edt_phone = itemView.findViewById(R.id.edt_phone);

        if(user.getPhoneNumber()==null&&TextUtils.isEmpty(user.getPhoneNumber()))
        {
            phone_input_layout.setHint("Email");
            edt_phone.setText(user.getEmail());

        }
        else
        edt_phone.setText(user.getPhoneNumber());



        builder.setView(itemView);

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss()).setPositiveButton("Register", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                if (TextUtils.isEmpty(edt_name.getText().toString())) {
                    Toast.makeText(MainActivity.this, "Please enter your name", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(edt_phone.getText().toString())) {
                    Toast.makeText(MainActivity.this, "Please enter your number phone", Toast.LENGTH_SHORT).show();
                    return;
                }

                ServerUserModel serverUserModel=new ServerUserModel();
                serverUserModel.setName(edt_name.getText().toString());
                serverUserModel.setPhone(edt_phone.getText().toString());
                serverUserModel.setUid(user.getUid());
                serverUserModel.setActive(false);

                dialog.show();

                serverRef.child(serverUserModel.getUid())
                        .setValue(serverUserModel)
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                dialog.dismiss();
                                Toast.makeText(MainActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        dialog.dismiss();
                        Toast.makeText(MainActivity.this, "Congratulation ! Register success ! Admin will check and active you soon", Toast.LENGTH_SHORT).show();
                 //       gotoHomeActivity(serverUserModel);
                    }
                });
            }
        });

        androidx.appcompat.app.AlertDialog registerDialog=builder.create();
        registerDialog.show();
    }

    private void gotoHomeActivity(ServerUserModel serverUserModel) {
        Common.currentServerUser=serverUserModel;
        dialog.dismiss();

        Intent intent=new Intent(this,HomeActivity.class);
        intent.putExtra(Common.IS_OPEN_ACTIVITY_NEW_ORDER,getIntent().getBooleanExtra(Common.IS_OPEN_ACTIVITY_NEW_ORDER,false));
        startActivity(new Intent(intent));

        finish();

    }

    private void phoneLogin() {
        startActivityForResult(AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build(), APP_REQUEST_CODE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == APP_REQUEST_CODE) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            } else {
                Toast.makeText(this, "Faile to sign in", Toast.LENGTH_SHORT).show();
            }
        }
    }
}







