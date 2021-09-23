package etn.app.danghoc.eatitserver.callback;

import java.util.List;

import etn.app.danghoc.eatitserver.model.CategoryModel;

public interface ICategoryCallbackListener {
    void onCategoryLoadSuccess(List<CategoryModel> categoryModelList);
    void onCategoryLoadFail(String message);
}
