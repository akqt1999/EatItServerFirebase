package etn.app.danghoc.eatitserver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import etn.app.danghoc.eatitserver.EventBus.AddonSizeEditEvent;
import etn.app.danghoc.eatitserver.EventBus.SelectAddonModel;
import etn.app.danghoc.eatitserver.EventBus.SelectSizeModel;
import etn.app.danghoc.eatitserver.EventBus.UpdateAddonModel;
import etn.app.danghoc.eatitserver.EventBus.UpdateSizeModel;
import etn.app.danghoc.eatitserver.adapter.MyAddonAdapter;
import etn.app.danghoc.eatitserver.adapter.MySizeAdapter;
import etn.app.danghoc.eatitserver.common.Common;
import etn.app.danghoc.eatitserver.model.AddonModel;
import etn.app.danghoc.eatitserver.model.SizeModel;

public class SideAddonEditActivity extends AppCompatActivity {

    @BindView(R.id.tool_bar)
    Toolbar toolbar;
    @BindView(R.id.edt_name)
    EditText edt_name;
    @BindView(R.id.edt_price)
    EditText edt_price;
    @BindView(R.id.btn_create)
    Button btn_create;
    @BindView(R.id.btn_edit)
    Button btn_edit;
    @BindView(R.id.recycler_addon_size)
    RecyclerView recyclerView_addon_size;

    //Variable
    MySizeAdapter adapter;
    MyAddonAdapter addonAdapter ;
    private int foodEditPosition = -1;
    private boolean needSave = false;
    private boolean isAddon=false;

    //event
    @OnClick(R.id.btn_create)
    void onCreateNew()
    {
        if(!isAddon)
        {
            if(adapter!=null)
            {
                SizeModel sizeModel=new SizeModel();
                sizeModel.setName(edt_name.getText().toString());
                sizeModel.setPrice(Long.valueOf(edt_price.getText().toString()));
                adapter.addNewSize(sizeModel);
            }
        }
        else //addon
        {
            if(addonAdapter!=null)
            {
                AddonModel addonModel=new AddonModel();
                addonModel.setName(edt_name.getText().toString());
                addonModel.setPrice(Long.valueOf(edt_price.getText().toString()));
                addonAdapter.addNewSize(addonModel);
            }
        }

    }

    @OnClick(R.id.btn_edit)
    void onEdit()
    {
        if(!isAddon) //size
        {
            if(adapter!=null)
            {
                SizeModel sizeModel=new SizeModel();
                sizeModel.setName(edt_name.getText().toString());
                sizeModel.setPrice(Long.valueOf(edt_price.getText().toString()));
                adapter.editSize(sizeModel);
            }

        }
        else // addon
        {
            if(addonAdapter!=null)
            {
                AddonModel addonModel=new AddonModel();
                addonModel.setName(edt_name.getText().toString());
                addonModel.setPrice(Long.valueOf(edt_price.getText().toString()));
                addonAdapter.addNewSize(addonModel);
            }

        }
    }

    //menu


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.addon_size_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_save:
                saveData();
                break;
            case android.R.id.home: {
                if (needSave) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Cancel?")
                            .setMessage("Do you really want close without saving ?")
                            .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            needSave = false;
                            closeActivity();
                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else {
                        closeActivity();
                }
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void saveData() {
        if(foodEditPosition!=-1)
        {
            Common.categorySelected.getFoods().set(foodEditPosition,Common.selectedFood);//save food to category

            Map<String,Object>updateData=new HashMap<>();
            updateData.put("food",Common.categorySelected.getFoods());

            FirebaseDatabase.getInstance()
                    .getReference(Common.CATEGORY_REF)
                    .child(Common.categorySelected.getMenuId())
                    .updateChildren(updateData)
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(SideAddonEditActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            Toast.makeText(SideAddonEditActivity.this, "Reload success !", Toast.LENGTH_SHORT).show();
                            needSave=false;
                            edt_name.setText("");
                            edt_price.setText("0");
                        }
                }
            });

        }
    }

    private void closeActivity() {
            edt_name.setText("");
            edt_price.setText("0");
            finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_side_addon_edit);
        init();
    }

    private void init() {
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        recyclerView_addon_size.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView_addon_size.setLayoutManager(layoutManager);

    }

    //register event


    @Override
    protected void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().removeStickyEvent(UpdateSizeModel.class);
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
        super.onStop();
    }

    //Receive event
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onAddOnSizeReceive(AddonSizeEditEvent event) {
        if (!event.isAddon()) // if event is size
        {
            if (Common.selectedFood.getSize() != null) {
                adapter = new MySizeAdapter(this, Common.selectedFood.getSize());
                foodEditPosition = event.getPos(); // save food edit update
                recyclerView_addon_size.setAdapter(adapter);

                isAddon=event.isAddon();
            }

        }
        else // is addon
        {
            if (Common.selectedFood.getAddon() != null) {
                addonAdapter = new MyAddonAdapter(this, Common.selectedFood.getAddon());
                foodEditPosition = event.getPos(); // save food edit update
                recyclerView_addon_size.setAdapter(addonAdapter);

                isAddon=event.isAddon();
            }
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void OnSizeModelUpdate(UpdateSizeModel event) {
        if (event.getSizeModelList() != null) {
            needSave = true;
            Common.selectedFood.setSize(event.getSizeModelList());
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void OnAddonModelUpdate(UpdateAddonModel event) {
        if (event.getAddonModel() != null) {
            needSave = true;
            Common.selectedFood.setAddon(event.getAddonModel());
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onSelectSizeModel(SelectSizeModel event) {

        if(event.getSizeModel()!=null)
        {
            edt_name.setText(event.getSizeModel().getName());
            edt_price.setText(event.getSizeModel().getPrice() + "");

            btn_edit.setEnabled(true);
        }
        else
        {
            btn_edit.setEnabled(false);
        }

    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onSelectAddonModel(SelectAddonModel event) {

        if(event.getAddonModel()!=null)
        {
            edt_name.setText(event.getAddonModel().getName());
            edt_price.setText(event.getAddonModel().getPrice() + "");

            btn_edit.setEnabled(true);
        }
        else
        {
            btn_edit.setEnabled(false);
        }

    }


}