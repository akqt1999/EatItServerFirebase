package etn.app.danghoc.eatitserver.EventBus;

import java.util.List;

import etn.app.danghoc.eatitserver.model.SizeModel;

public class UpdateSizeModel {

    private List<SizeModel>sizeModelList;

    public UpdateSizeModel(List<SizeModel> sizeModelList) {
        this.sizeModelList = sizeModelList;
    }

    public UpdateSizeModel() {
    }

    public List<SizeModel> getSizeModelList() {
        return sizeModelList;
    }

    public void setSizeModelList(List<SizeModel> sizeModelList) {
        this.sizeModelList = sizeModelList;
    }
}
