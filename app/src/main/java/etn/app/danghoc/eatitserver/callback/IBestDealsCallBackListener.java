package etn.app.danghoc.eatitserver.callback;

import java.util.List;

import etn.app.danghoc.eatitserver.model.BestDealModel;


public interface IBestDealsCallBackListener {
    void onListBestDealsLoadSuccess(List<BestDealModel> bestDealModels);
    void onListBestDealLoadFailed(String message);
}
