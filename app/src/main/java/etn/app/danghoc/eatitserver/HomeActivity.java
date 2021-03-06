package etn.app.danghoc.eatitserver;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import butterknife.OnClick;
import etn.app.danghoc.eatitserver.EventBus.CategoryClick;
import etn.app.danghoc.eatitserver.EventBus.ChangeMenuClick;
import etn.app.danghoc.eatitserver.EventBus.ToastEvent;
import etn.app.danghoc.eatitserver.Remote.IFCMService;
import etn.app.danghoc.eatitserver.Remote.RetrofitFCMClient;
import etn.app.danghoc.eatitserver.common.Common;
import etn.app.danghoc.eatitserver.model.FCMResponse;
import etn.app.danghoc.eatitserver.model.FCMSendData;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private AppBarConfiguration mAppBarConfiguration;

    private DrawerLayout drawer;
    NavigationView navigationView;
    NavController navController;
    private int menuClick = -1;

    //send news system
    ImageView img_upload;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private IFCMService ifcmService;
    private Uri imgUri = null;

    private FirebaseStorage storage;
    private StorageReference storageReference;
    private final int PICK_IMAGE_REQUEST = 4432;
    //-----------------------------------------


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ifcmService = RetrofitFCMClient.getInstance().create(IFCMService.class);
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();


        updateTokent();

        subscribeToTopic(Common.createTopicOrder());

        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_category, R.id.nav_food_list, R.id.nav_order, R.id.nav_shipper, R.id.nav_order2)
                .setDrawerLayout(drawer)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        navigationView.setNavigationItemSelectedListener(this);
        navigationView.bringToFront();

        View headerView = navigationView.getHeaderView(0);
        TextView txt_user = headerView.findViewById(R.id.txt_user);
        Common.setSpanString("Hey ", Common.currentServerUser.getName(), txt_user);
        menuClick = R.id.nav_category;

        checkIsOpenFromActivity();


    }

    private void checkIsOpenFromActivity() {
        boolean isOpenFromNewOrder = getIntent().getBooleanExtra(Common.IS_OPEN_ACTIVITY_NEW_ORDER, false);
        if (isOpenFromNewOrder) {
            navController.popBackStack();
            navController.navigate(R.id.nav_order);
            menuClick = R.id.nav_order;

        }

    }

    private void updateTokent() {
        FirebaseInstanceId.getInstance()
                .getInstanceId()
                .addOnFailureListener(e -> Toast.makeText(HomeActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show())
                .addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
                    @Override
                    public void onSuccess(InstanceIdResult instanceIdResult) {
                        Common.updateToken(HomeActivity.this, instanceIdResult.getToken(), true, false);
                    }
                });
    }

    private void subscribeToTopic(String topicOrder) {
        FirebaseMessaging.getInstance()
                .subscribeToTopic(topicOrder)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(HomeActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (!task.isSuccessful()) {
                    Toast.makeText(HomeActivity.this, "Failed" + task.isSuccessful(), Toast.LENGTH_SHORT).show();

                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }


    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        compositeDisposable.clear();
        super.onStop();

    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onCategoryClick(CategoryClick event) {
        if (event.isSuccess()) {
            if (menuClick != R.id.nav_food_list) {
                navController.navigate(R.id.nav_food_list);
                menuClick = R.id.nav_food_list;
            }

        }


    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onChangeMenuClick(ChangeMenuClick event) {
        if (event.isFromFoodList()) {
            //clear
            Toast.makeText(this, "truee", Toast.LENGTH_SHORT).show();
            navController.popBackStack(R.id.nav_category, true);
            navController.navigate(R.id.nav_category);
        } else {
            Toast.makeText(this, "falsee", Toast.LENGTH_SHORT).show();
            navController.popBackStack(R.id.nav_food_list, true);
            navController.navigate(R.id.nav_food_list);
        }
        menuClick = -1;
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onToastEvent(ToastEvent event) {
        if (event.isUpdate()) {
            Toast.makeText(this, "Update", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Delete success", Toast.LENGTH_SHORT).show();
        }

        EventBus.getDefault().postSticky(new ChangeMenuClick(event.isFromFoodList()));
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        item.setChecked(true);
        drawer.closeDrawers();
        switch (item.getItemId()) {
            case R.id.nav_category:
                if (item.getItemId() != menuClick) {
                    navController.navigate(R.id.nav_category);
                    navController.popBackStack(); //remove all back stack

                }

                break;
            case R.id.nav_order:
                if (item.getItemId() != menuClick) {
                    navController.navigate(R.id.nav_order);
                    //  navController.popBackStack(); //remove all back stack

                }

                break;
            case R.id.nav_shipper:
                if (item.getItemId() != menuClick) {
                    navController.popBackStack(); //remove all back stack
                    navController.navigate(R.id.nav_shipper);

                }
            case R.id.nav_order2:
                if (item.getItemId() != menuClick) {
                    navController.popBackStack(); //remove all back stack
                    navController.navigate(R.id.nav_order2);

                }
                break;

            case R.id.nav_best_deals:
                if (item.getItemId() != menuClick) {
                    navController.popBackStack(); //remove all back stack
                    navController.navigate(R.id.nav_best_deals);

                }
                break;

            case R.id.nav_most_popular:
                if (item.getItemId() != menuClick) {
                    navController.popBackStack(); //remove all back stack
                    navController.navigate(R.id.nav_most_popular);

                }
                break;

            case R.id.nav_send_news:
                if (item.getItemId() != menuClick) {
                    showNewsDialog();
                }
                break;


            case R.id.nav_sign_out:
                signOut();
                menuClick = -1;
                break;


            default:
                menuClick = -1;
                break;

        }

        menuClick = item.getItemId();
        return false;
    }

    private void showNewsDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("News System");
        builder.setMessage("send news notification to all client");

        View itemView = LayoutInflater.from(this).inflate(R.layout.layout_new_system, null);

        //views
        EditText edt_title = itemView.findViewById(R.id.edt_title);
        EditText edt_content = itemView.findViewById(R.id.edt_content);

        EditText edt_link = itemView.findViewById(R.id.edt_link);
        img_upload = itemView.findViewById(R.id.img_upload);

        RadioButton rdi_none = itemView.findViewById(R.id.rdi_none);
        RadioButton rdi_link = itemView.findViewById(R.id.rdi_link);
        RadioButton rdi_upload = itemView.findViewById(R.id.rdi_image);

        //event

        rdi_none.setOnClickListener(v -> {
            edt_link.setVisibility(View.GONE);
            img_upload.setVisibility(View.GONE);
        });

        rdi_link.setOnClickListener(v -> {
            edt_link.setVisibility(View.VISIBLE);
            img_upload.setVisibility(View.GONE);

        });

        rdi_upload.setOnClickListener(v -> {
            edt_link.setVisibility(View.GONE);
            img_upload.setVisibility(View.VISIBLE);

        });

        img_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
            }
        });

        builder.setView(itemView);

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.setPositiveButton("Send", (dialog, which) -> {
            if (rdi_none.isChecked()) {
                sendNews(edt_title.getText().toString(), edt_content.getText().toString());
            } else if (rdi_link.isChecked()) {
                sendNews(edt_title.getText().toString(), edt_content.getText().toString(), edt_link.getText().toString());
            } else if (rdi_upload.isChecked()) {
                if (imgUri != null) {
                    AlertDialog dialog1 = new AlertDialog.Builder(this).setMessage("Uploading...").create();
                    dialog1.show();

                    String file_name = UUID.randomUUID().toString();
                    StorageReference newsImage = storageReference.child("news/" + file_name);
                    newsImage.putFile(imgUri)
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    dialog.dismiss();
                                    Toast.makeText(HomeActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnSuccessListener(taskSnapshot -> {
                                dialog.dismiss();
                                dialog1.dismiss();
                                Toast.makeText(this, "upload success ok", Toast.LENGTH_SHORT).show();
                                newsImage.getDownloadUrl().addOnSuccessListener(uri ->
                                        sendNews(edt_title.getText().toString(), edt_content.getText().toString(), uri.toString()));
                            })
                            .addOnProgressListener(snapshot -> {
                                double process = Math.round((100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount());
                                dialog1.setMessage(new StringBuilder("Uploading: ").append(process).append("%"));
                            });

                }

            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void sendNews(String title, String content, String url) {
        Map<String, String> notificationData = new HashMap<>();
        notificationData.put(Common.NOTI_TITILE, title);
        notificationData.put(Common.NOTI_CONTENT, content);
        notificationData.put(Common.IS_SEND_IMAGE, "true");
        notificationData.put(Common.IMAGE_URL, url);

        FCMSendData fcmSendData = new FCMSendData(Common.getNewsTopic(), notificationData);
        AlertDialog dialog = new AlertDialog.Builder(this).setMessage("Waiting...").create();

        dialog.show();

        compositeDisposable.add(ifcmService.sendNotification(fcmSendData)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(fcmResponse -> {
                            dialog.dismiss();
                            if (fcmResponse.getMessage_id() != 0)
                                Toast.makeText(this, "News has been sent", Toast.LENGTH_SHORT).show();
                            else
                                Toast.makeText(this, "New send fail", Toast.LENGTH_SHORT).show();

                        }, throwable -> {
                            dialog.dismiss();
                            Toast.makeText(this, "New send fail", Toast.LENGTH_SHORT).show();
                            Toast.makeText(this, "" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                ));


    }

    private void sendNews(String title, String content) {
        Map<String, String> notificationData = new HashMap<>();
        notificationData.put(Common.NOTI_TITILE, title);
        notificationData.put(Common.NOTI_CONTENT, content);
        notificationData.put(Common.IS_SEND_IMAGE, "false");

        FCMSendData fcmSendData = new FCMSendData(Common.getNewsTopic(), notificationData);
        AlertDialog dialog = new AlertDialog.Builder(this).setMessage("Waiting...").create();

        dialog.show();

        compositeDisposable.add(ifcmService.sendNotification(fcmSendData)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(fcmResponse -> {
                            dialog.dismiss();
                            if (fcmResponse.getMessage_id() != 0)
                                Toast.makeText(this, "cai nay khong co gi", Toast.LENGTH_SHORT).show();
                            else
                                Toast.makeText(this, "New send fail", Toast.LENGTH_SHORT).show();

                        }, throwable -> {
                            dialog.dismiss();
                            Toast.makeText(this, "" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                ));


    }


    private void signOut() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sign Out")
                .setMessage("do you  really  want to sign out")
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Common.selectedFood = null;
                        Common.categorySelected = null;
                        Common.currentServerUser = null;
                        FirebaseAuth.getInstance().signOut();
                        Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                });

        AlertDialog dialog;
        dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data != null && data.getData() != null) {
                imgUri = data.getData();
                img_upload.setImageURI(imgUri);

            }
        }
    }


}