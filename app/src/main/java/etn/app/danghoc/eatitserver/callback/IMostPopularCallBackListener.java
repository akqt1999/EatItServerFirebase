package etn.app.danghoc.eatitserver.callback;

import java.util.List;

import etn.app.danghoc.eatitserver.model.MostPopularModel;

public interface IMostPopularCallBackListener {
    void onListMostPopularLoadSuccess(List<MostPopularModel>mostPopularModelList);
    void onListMostPopularLoadFail(String message);
}
