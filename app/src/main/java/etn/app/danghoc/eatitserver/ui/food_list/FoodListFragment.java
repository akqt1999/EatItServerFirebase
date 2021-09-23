package etn.app.danghoc.eatitserver.ui.food_list;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.EditText;
import android.widget.ImageView;

import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;
import etn.app.danghoc.eatitserver.EventBus.AddonSizeEditEvent;
import etn.app.danghoc.eatitserver.EventBus.ChangeMenuClick;
import etn.app.danghoc.eatitserver.EventBus.ToastEvent;
import etn.app.danghoc.eatitserver.R;
import etn.app.danghoc.eatitserver.SideAddonEditActivity;
import etn.app.danghoc.eatitserver.adapter.MyFoodListAdapter;
import etn.app.danghoc.eatitserver.common.Common;
import etn.app.danghoc.eatitserver.common.MySwiperHelper;
import etn.app.danghoc.eatitserver.model.AddonModel;
import etn.app.danghoc.eatitserver.model.FoodModel;

public class FoodListFragment extends Fragment {

    //image update
    private static final int PICK_IMAGE_REQUEST = 123;
    private ImageView img_food;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private android.app.AlertDialog dialog;

    private FoodListViewModel foodListViewModel;

    private List<FoodModel> foodModelList;

    Unbinder unbinder;
    LayoutAnimationController layoutAnimationController;
    MyFoodListAdapter adapter;

    @BindView(R.id.recycler_food_list)
    RecyclerView recyclerFoodList;
    private Uri imaUri = null;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        foodListViewModel =
                new ViewModelProvider(this).get(FoodListViewModel.class);
        View root = inflater.inflate(R.layout.fragment_food_list, container, false);
        unbinder = ButterKnife.bind(this, root);
        init();
        foodListViewModel.getMutableLiveDataFoodList().observe(this, foodModels -> {
            //
            if (foodModels != null) {
                foodModelList = foodModels;
                adapter = new MyFoodListAdapter(getContext(), foodModelList);
                recyclerFoodList.setAdapter(adapter);
                recyclerFoodList.setLayoutAnimation(layoutAnimationController);
            }
        });
        return root;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.food_list_menu, menu);

        MenuItem menuItem = menu.findItem(R.id.action_search);

        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));

        //event
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                startSeachFood(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        ImageView closeButton = searchView.findViewById(R.id.search_close_btn);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText ed = searchView.findViewById(R.id.search_src_text);
                //clear text
                ed.setText("");
                //clear query
                searchView.setQuery("", false);
                // the search widget
                menuItem.collapseActionView();
                //restore result to original
                foodListViewModel.getMutableLiveDataFoodList().setValue(Common.categorySelected.getFoods());

            }
        });

    }


    private void startSeachFood(String query) {
        List<FoodModel> resultFood = new ArrayList<>();
        for (int i = 0; i < Common.categorySelected.getFoods().size(); i++) {
            FoodModel foodModel = Common.categorySelected.getFoods().get(i);
            if (foodModel.getName().toLowerCase().contains(query)) {
                foodModel.setPosotionInList(i);//save index
                resultFood.add(foodModel);
            }
        }

        foodListViewModel.getMutableLiveDataFoodList().setValue(resultFood);// set search result


    }

    private void init() {

        setHasOptionsMenu(true);

        dialog = new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).build();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();


        ((AppCompatActivity) getActivity())
                .getSupportActionBar()
                .setTitle(Common.categorySelected.getName());

        recyclerFoodList.setHasFixedSize(true);
        recyclerFoodList.setLayoutManager(new LinearLayoutManager(getContext()));
        //     layoutAnimationController = AnimationUtils.loadLayoutAnimation(getContext(), R.anim.layout_item_from_left);

        // cang huy vong cang that vong

        // get size
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;

        MySwiperHelper mySwiperHelper = new MySwiperHelper(getContext(), recyclerFoodList, width / 6) {
            @Override
            public void instantiateMyButton(RecyclerView.ViewHolder viewHolder, List<MyButton> buf) {
                buf.add(new MyButton(getContext(), "Delete", 30, 0, Color.parseColor("#9b0000"),
                        pos -> {
                            if (foodModelList != null)
                                Common.selectedFood = foodModelList.get(pos);
                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                            builder.setTitle("DELETE")
                                    .setMessage("Do you want to delete this food?");
                            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                                    .setPositiveButton("Delete", (dialog, which) -> {

                                        FoodModel foodModel = adapter.getItemAtPosition(pos);//get item in adapter
                                        if (foodModel.getPosotionInList() == -1)// if ==-1 default , no thing
                                            Common.categorySelected.getFoods().remove(pos);
                                        else
                                            Common.categorySelected.getFoods().remove(foodModel.getPosotionInList()); // remove index was save
                                        updateFood(Common.categorySelected.getFoods(), true);
                                    });

                            AlertDialog deleteDialog = builder.create();
                            deleteDialog.show();


                        }));
                buf.add(new MyButton(getContext(), "Update", 30, 0, Color.parseColor("#560027"),
                        pos -> {

                    FoodModel foodModel = adapter.getItemAtPosition(pos);
                            if (foodModel.getPosotionInList() == -1)
                                showUpdateDialog(pos, foodModel);
                            else
                                showUpdateDialog(pos, foodModel);

                        }));

                buf.add(new MyButton(getContext(), "Size", 30, 0, Color.parseColor("#12005e"),
                        pos -> {


                            FoodModel foodModel=adapter.getItemAtPosition(pos);
                            if(foodModel.getPosotionInList()==-1)
                                Common.selectedFood=foodModelList.get(pos);
                            else
                                Common.selectedFood=foodModel;
                            startActivity(new Intent(getContext(), SideAddonEditActivity.class));
                            //change pos
                            if(foodModel.getPosotionInList()==-1)
                            EventBus.getDefault().postSticky(new AddonSizeEditEvent(false,pos));
                            else
                                EventBus.getDefault().postSticky(new AddonSizeEditEvent(false,foodModel.getPosotionInList()));

                        }));

                buf.add(new MyButton(getContext(), "Addon", 30, 0, Color.parseColor("#336699"),
                        pos -> {
                            FoodModel foodModel=adapter.getItemAtPosition(pos);
                            if(foodModel.getPosotionInList()==-1)
                                Common.selectedFood=foodModelList.get(pos);
                            else
                                Common.selectedFood=foodModel;
                            startActivity(new Intent(getContext(), SideAddonEditActivity.class));

                            if(foodModel.getPosotionInList()==-1)
                            EventBus.getDefault().postSticky(new AddonSizeEditEvent(true,pos)); // gửi
                            else
                                EventBus.getDefault().postSticky(new AddonSizeEditEvent(true,foodModel.getPosotionInList())); // gửi
                        }));


            }
        };


    }

    private void showUpdateDialog(int pos, FoodModel foodModel) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
        builder.setTitle("Update");
        builder.setMessage("Please fill information");

        View itemView = LayoutInflater.from(getContext()).inflate(R.layout.layout_update_food, null);

        EditText edt_food_name = itemView.findViewById(R.id.edt_food_name);
        EditText edt_food_price = itemView.findViewById(R.id.edt_food_price);
        EditText edt_food_description = itemView.findViewById(R.id.edt_food_description);
        img_food = itemView.findViewById(R.id.img_food);

        builder.setView(itemView);


        //set data
        edt_food_name.setText(new StringBuilder("")
                .append(foodModel.getName()));
        edt_food_price.setText(new StringBuilder("")
                .append(foodModel.getPrice()));
        edt_food_description.setText(new StringBuilder("")
                .append(foodModel.getDescription()));

        Glide.with(getContext()).load(foodModel.getImage()).into(img_food);

        //set event
        img_food.setOnClickListener(v -> {

            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture "), PICK_IMAGE_REQUEST);

        });

        builder.setNegativeButton("Cancel", (dialog1, which) -> dialog1.dismiss())
                .setPositiveButton("Update", (dialog1, which) -> {

                    dialog1.dismiss();

                    FoodModel updateFood = foodModel;
                    updateFood.setName(edt_food_name.getText().toString());
                    updateFood.setDescription(edt_food_description.getText().toString());
                    updateFood.setPrice(TextUtils.isEmpty(edt_food_price.getText()) ? 0 :
                            Long.parseLong(edt_food_price.getText().toString()));

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
                                updateFood.setImage(task1.toString());
                                Common.categorySelected.getFoods().set(pos, updateFood);
                                updateFood(Common.categorySelected.getFoods(), false);

                            });
                        }).addOnProgressListener(snapshot -> {
                            double progress = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                            dialog.setMessage("Uploading:" + progress + "%");

                        });

                    } else {

                        Common.categorySelected.getFoods().set(pos, updateFood);
                        updateFood(Common.categorySelected.getFoods(), false);
                    }

                });
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    private void updateFood(List<FoodModel> foods, boolean isDelete) {
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("foods", foods);

        FirebaseDatabase.getInstance()
                .getReference(Common.CATEGORY_REF)
                .child(Common.categorySelected.getMenuId())
                .updateChildren(updateData)
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT)
                                .show()).addOnCompleteListener(task -> {
            foodListViewModel.getMutableLiveDataFoodList();
            EventBus.getDefault().postSticky(new ToastEvent(!isDelete, true));

        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data != null && data.getData() != null) {
                imaUri = data.getData();
                img_food.setImageURI(imaUri);

            }
        }
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().postSticky(new ChangeMenuClick(true));
        super.onDestroy();
    }
}