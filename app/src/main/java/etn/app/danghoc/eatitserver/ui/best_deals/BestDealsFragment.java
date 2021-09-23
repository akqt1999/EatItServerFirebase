package etn.app.danghoc.eatitserver.ui.best_deals;

import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LayoutAnimationController;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;
import etn.app.danghoc.eatitserver.EventBus.ToastEvent;
import etn.app.danghoc.eatitserver.R;
import etn.app.danghoc.eatitserver.adapter.MyBestDealsAdapter;
import etn.app.danghoc.eatitserver.adapter.MyCategoryAdapter;
import etn.app.danghoc.eatitserver.common.Common;
import etn.app.danghoc.eatitserver.common.MySwiperHelper;
import etn.app.danghoc.eatitserver.model.BestDealModel;
import etn.app.danghoc.eatitserver.model.CategoryModel;
import etn.app.danghoc.eatitserver.ui.category.CategoryViewModel;

public class BestDealsFragment extends Fragment {

    private BestDealsViewModel mViewModel;

    private static final int PICK_IMAGE_REQUEST = 123;

    //update image
    ImageView img_best_deals;
    private Uri imaUri = null;

    FirebaseStorage storage;
    StorageReference storageReference;
//---------------------------------------------


    Unbinder unbinder;
    @BindView(R.id.recycler_best_deal)
    RecyclerView recycler_best_deal;

    AlertDialog dialog;
    LayoutAnimationController layoutAnimationController;
    MyBestDealsAdapter adapter;

    List<BestDealModel> bestDealModels;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        mViewModel =
                new ViewModelProvider(this).get(BestDealsViewModel.class);

        View root = inflater.inflate(R.layout.best_deals_fragment, container, false);

        unbinder = ButterKnife.bind(this, root);

        init();

        mViewModel.getMessagesError().observe(getViewLifecycleOwner(), s -> {
            //
            Toast.makeText(getContext(), s + "", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
        mViewModel.getBestDealsListMutable().observe(getViewLifecycleOwner(), bestDealModelList -> {
            dialog.dismiss();
            bestDealModels = bestDealModelList;
            adapter = new MyBestDealsAdapter(getContext(), bestDealModelList);
            recycler_best_deal.setAdapter(adapter);
            adapter.notifyDataSetChanged();
            //          recyclerViewMenu.setLayoutAnimation(layoutAnimationController);
        });


        return root;

    }

    private void init() {

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();


        dialog = new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).build();
//        dialog.show();
        //    layoutAnimationController = AnimationUtils.loadLayoutAnimation(getContext(), R.anim.layout_item_from_left);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());

        recycler_best_deal.setLayoutManager(layoutManager);
        recycler_best_deal.addItemDecoration(new DividerItemDecoration(getContext(), layoutManager.getOrientation()));


        MySwiperHelper mySwiperHelper = new MySwiperHelper(getContext(), recycler_best_deal, 200) {
            @Override
            public void instantiateMyButton(RecyclerView.ViewHolder viewHolder, List<MyButton> buf) {
                buf.add(new MyButton(getContext(), "Update", 30, 0, Color.parseColor("#560027"),
                        pos -> {

                            Common.bestDealsSelected = bestDealModels.get(pos);
                            showUpdateDialog();
                        }));

                buf.add(new MyButton(getContext(), "Delete", 30, 0, Color.parseColor("#333639"),
                        pos -> {

                            Common.bestDealsSelected = bestDealModels.get(pos);
                            showDeleteDialog();
                        }));

            }
        };


    }

    private void showDeleteDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
        builder.setTitle("Delete");
        builder.setMessage("Do you want to delete this item");
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteBestDeals();
            }
        });


        androidx.appcompat.app.AlertDialog dialog=builder.create();
        dialog.show();


    }

    private void deleteBestDeals() {
        FirebaseDatabase.getInstance()
                .getReference(Common.BEST_DEALS)
                .child(Common.bestDealsSelected.getKey())
                .removeValue()
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                }).addOnCompleteListener(task -> {
            mViewModel.loadBestDeals();
            Toast.makeText(getContext(), "delete success", Toast.LENGTH_SHORT).show();
            //  EventBus.getDefault().postSticky(new ToastEvent(true,false) );
        });
    }


    private void showUpdateDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
        builder.setTitle("Update");
        builder.setMessage("Please fill information");

        View itemView = LayoutInflater.from(getContext()).inflate(R.layout.layout_update_category, null);
        EditText edt_category_name = itemView.findViewById(R.id.edt_category_name);
        img_best_deals = itemView.findViewById(R.id.img_category);

        //set data
        edt_category_name.setText(new StringBuilder("").append(Common.bestDealsSelected.getName()));
        Glide.with(getContext()).load(Common.bestDealsSelected.getImage()).into(img_best_deals);

        //set event
        img_best_deals.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);

        });

        builder.setNegativeButton("CANCEL", (dialog, which) -> dialog.dismiss());
        builder.setPositiveButton("UPDATE", (dialog1, which) -> {
            Map<String, Object> updateData = new HashMap<>();
            updateData.put("name", edt_category_name.getText().toString());

            if (imaUri != null) {
                dialog.setMessage("Uploading...");
                dialog.show();
                Log.d("loiii", "" + 1);
                String unique_name = UUID.randomUUID().toString();
                StorageReference imageFolder = storageReference.child("images/" + unique_name);

                imageFolder.putFile(imaUri)
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                dialog.dismiss();

                                Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }).addOnCompleteListener(task -> {
                    dialog.dismiss();
                    imageFolder.getDownloadUrl().addOnSuccessListener(task1 -> {
                        updateData.put("image", task1.toString());
                        updateBestDeals(updateData);

                    });
                }).addOnProgressListener(snapshot -> {
                    double progress = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                    dialog.setMessage("Uploading:" + progress + "%");

                });
            } else {
                updateBestDeals(updateData);
                Log.d("loiii", "" + 2);
            }
        });
        builder.setView(itemView);
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void updateBestDeals(Map<String, Object> updateData) {

        FirebaseDatabase.getInstance()
                .getReference(Common.BEST_DEALS)
                .child(Common.bestDealsSelected.getKey())
                .updateChildren(updateData)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                }).addOnCompleteListener(task -> {
            mViewModel.loadBestDeals();
            Toast.makeText(getContext(), "update success", Toast.LENGTH_SHORT).show();
            //  EventBus.getDefault().postSticky(new ToastEvent(true,false) );
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data != null && data.getData() != null) {
                imaUri = data.getData();
                img_best_deals.setImageURI(imaUri);

            }
        }
    }


}
/*
m co buon gi trong long noi ra het di tri
thu nhat di xe keu keu la do cho do co cac chu k phai gi het ,
tien chuyen xong roi
ok ,


 */