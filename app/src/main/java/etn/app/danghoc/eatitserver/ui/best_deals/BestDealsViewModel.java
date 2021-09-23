package etn.app.danghoc.eatitserver.ui.best_deals;

import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import etn.app.danghoc.eatitserver.callback.IBestDealsCallBackListener;
import etn.app.danghoc.eatitserver.common.Common;
import etn.app.danghoc.eatitserver.model.BestDealModel;

public class BestDealsViewModel extends ViewModel implements IBestDealsCallBackListener {
    private MutableLiveData<String>messagesError=new MutableLiveData<>();
    private MutableLiveData<List<BestDealModel>>bestDealsListMutable;

    private IBestDealsCallBackListener bestDealsCallBackListener;

    public BestDealsViewModel()
    {
        bestDealsCallBackListener=this;
    }


    public MutableLiveData<List<BestDealModel>> getBestDealsListMutable() {
        if(bestDealsListMutable==null)
            bestDealsListMutable=new MutableLiveData<>();
        loadBestDeals();
        return bestDealsListMutable;
    }

    public void loadBestDeals() {
        List<BestDealModel>temp=new ArrayList<>();
        DatabaseReference bestDealsRef= FirebaseDatabase.getInstance()
                .getReference(Common.BEST_DEALS);
        bestDealsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for(DataSnapshot bestDealSnapshot:snapshot.getChildren())
                {
                    BestDealModel dealModel=bestDealSnapshot.getValue(BestDealModel.class);
                    dealModel.setKey(bestDealSnapshot.getKey());
                    temp.add(dealModel);
                }
                bestDealsCallBackListener.onListBestDealsLoadSuccess(temp);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                bestDealsCallBackListener.onListBestDealLoadFailed(error.getMessage());
            }
        });
    }


    public MutableLiveData<String> getMessagesError() {
        return messagesError;
    }


    @Override
    public void onListBestDealsLoadSuccess(List<BestDealModel> bestDealModels) {
            bestDealsListMutable.setValue(bestDealModels);
    }


    @Override
    public void onListBestDealLoadFailed(String message) {
        messagesError.setValue(message);
    }
}