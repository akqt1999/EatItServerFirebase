package etn.app.danghoc.eatitserver.ui.category;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.print.PageRange;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.greenrobot.eventbus.EventBus;

import java.sql.Struct;
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
import etn.app.danghoc.eatitserver.adapter.MyCategoryAdapter;
import etn.app.danghoc.eatitserver.common.Common;
import etn.app.danghoc.eatitserver.common.MySwiperHelper;
import etn.app.danghoc.eatitserver.common.SpacesItemDecoration;
import etn.app.danghoc.eatitserver.model.CategoryModel;

public class CategoryFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 123;
    private CategoryViewModel menuViewModel;

    Unbinder unbinder;
    @BindView(R.id.recycler_menu)
    RecyclerView recyclerViewMenu;

    AlertDialog dialog;
    LayoutAnimationController layoutAnimationController;
    MyCategoryAdapter adapter;

    List<CategoryModel> categoryModels;

    ImageView img_category;
    private Uri imaUri = null;

    FirebaseStorage storage;
    StorageReference storageReference;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        menuViewModel =
                new ViewModelProvider(this).get(CategoryViewModel.class);
        View root = inflater.inflate(R.layout.fragment_category, container, false);

        unbinder = ButterKnife.bind(this, root);

        menuViewModel.getMessageError().observe(getViewLifecycleOwner(), s -> {
            //
            Toast.makeText(getContext(), s + "", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
        menuViewModel.getCategoryListMultable().observe(getViewLifecycleOwner(), categoryModelList -> {
            dialog.dismiss();
            categoryModels = categoryModelList;
            adapter = new MyCategoryAdapter(getContext(), categoryModels);
            recyclerViewMenu.setAdapter(adapter);
            adapter.notifyDataSetChanged();
            //          recyclerViewMenu.setLayoutAnimation(layoutAnimationController);
        });

        init();
        return root;
    }

    private void init() {

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        dialog = new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).build();
//        dialog.show();
        //    layoutAnimationController = AnimationUtils.loadLayoutAnimation(getContext(), R.anim.layout_item_from_left);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());

        recyclerViewMenu.setLayoutManager(layoutManager);
        recyclerViewMenu.addItemDecoration(new DividerItemDecoration(getContext(), layoutManager.getOrientation()));


        MySwiperHelper mySwiperHelper = new MySwiperHelper(getContext(), recyclerViewMenu, 200) {
            @Override
            public void instantiateMyButton(RecyclerView.ViewHolder viewHolder, List<MyButton> buf) {
                buf.add(new MyButton(getContext(), "Update", 30, 0, Color.parseColor("#560027"),
                        pos -> {

                            Common.categorySelected = categoryModels.get(pos);
                            showUpdateDialog();
                        }));
            }
        };
    }

    private void showUpdateDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
        builder.setTitle("Update");
        builder.setMessage("Please fill information");

        View itemView = LayoutInflater.from(getContext()).inflate(R.layout.layout_update_category, null);
        EditText edt_category_name = itemView.findViewById(R.id.edt_category_name);
        img_category = itemView.findViewById(R.id.img_category);

        //set data
        edt_category_name.setText(new StringBuilder("").append(Common.categorySelected.getName()));
        Glide.with(getContext()).load(Common.categorySelected.getImage()).into(img_category);

        //set event
        img_category.setOnClickListener(v -> {
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
                Log.d("loiii", ""+1);
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
                        updateCategory(updateData);

                    });
                }).addOnProgressListener(snapshot -> {
                    double progress = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                    dialog.setMessage("Uploading:" + progress + "%");

                });
            } else {
                updateCategory(updateData);
                Log.d("loiii", ""+2);
            }
        });
        builder.setView(itemView);
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void updateCategory(Map<String, Object> updateData) {
        Log.d("loiii", ""+3);
        FirebaseDatabase.getInstance()
                .getReference(Common.CATEGORY_REF)
                .child(Common.categorySelected.getMenuId())
                .updateChildren(updateData)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                }).addOnCompleteListener(task -> {
            menuViewModel.loadCategories();
            Toast.makeText(getContext(), "update success", Toast.LENGTH_SHORT).show();
            EventBus.getDefault().postSticky(new ToastEvent(true,false) );

        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data != null && data.getData() != null) {
                imaUri = data.getData();
                img_category.setImageURI(imaUri);

            }
        }
    }


}