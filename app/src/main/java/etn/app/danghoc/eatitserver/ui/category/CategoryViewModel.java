package etn.app.danghoc.eatitserver.ui.category;

import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import etn.app.danghoc.eatitserver.callback.ICategoryCallbackListener;
import etn.app.danghoc.eatitserver.common.Common;
import etn.app.danghoc.eatitserver.model.CategoryModel;

public class CategoryViewModel extends ViewModel implements ICategoryCallbackListener {

    private MutableLiveData<List<CategoryModel>>categoryListMultable;
    private MutableLiveData<String>messageError=new MutableLiveData<>();
    private ICategoryCallbackListener categoryCallbackListener;

    public CategoryViewModel() {
        categoryCallbackListener=this;
    }

    public MutableLiveData<List<CategoryModel>> getCategoryListMultable() {
        if(categoryListMultable==null)
        {
            categoryListMultable=new MutableLiveData<>();
            messageError=new MutableLiveData<>();
            loadCategories();
            Log.d("aaaa","get");
        }
        return categoryListMultable;
    }

    public void loadCategories() {
        List<CategoryModel>tempList=new ArrayList<>();
        DatabaseReference categoryRef= FirebaseDatabase.getInstance().getReference(Common.CATEGORY_REF);
        categoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot itemSnapShot:snapshot.getChildren())
                {
                    CategoryModel model=itemSnapShot.getValue(CategoryModel.class);
                    model.setMenuId(itemSnapShot.getKey());
                    tempList.add(model);
                }
                categoryCallbackListener.onCategoryLoadSuccess(tempList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                categoryCallbackListener.onCategoryLoadFail(error.getMessage());

            }
        });


    }

    public MutableLiveData<String> getMessageError() {
        return messageError;
    }

    @Override
    public void onCategoryLoadSuccess(List<CategoryModel> categoryModelList) {
        categoryListMultable.setValue(categoryModelList);
    }

    @Override
    public void onCategoryLoadFail(String message) {
        messageError.setValue(message);
    }
}