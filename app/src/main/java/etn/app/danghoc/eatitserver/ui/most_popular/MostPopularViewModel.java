package etn.app.danghoc.eatitserver.ui.most_popular;

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

import etn.app.danghoc.eatitserver.callback.IMostPopularCallBackListener;
import etn.app.danghoc.eatitserver.common.Common;
import etn.app.danghoc.eatitserver.model.MostPopularModel;

public class MostPopularViewModel extends ViewModel implements IMostPopularCallBackListener {
    private MutableLiveData<String>messageError=new MutableLiveData<>();
    private MutableLiveData<List<MostPopularModel>>mostPopularListMutable;
    private IMostPopularCallBackListener mostPopularCallBackListener;

    public MostPopularViewModel( ) {
        this.mostPopularCallBackListener = this;
    }


    public MutableLiveData<List<MostPopularModel>> getMostPopularListMutable() {
        if(mostPopularListMutable==null)
            mostPopularListMutable=new MutableLiveData<>();
        loadMostPopular();
        return mostPopularListMutable;
    }

    private void loadMostPopular() {
        List<MostPopularModel>mostPopularModels=new ArrayList<>();
        DatabaseReference mostPopularRef= FirebaseDatabase.getInstance()
                .getReference(Common.MOST_POPULAR);
        mostPopularRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot mostPopularSnapshot:snapshot.getChildren())
                {
                    MostPopularModel mostPopularModel=mostPopularSnapshot.getValue(MostPopularModel.class);
                    mostPopularModel.setKey(mostPopularSnapshot.getKey());
                    mostPopularModels.add(mostPopularModel);
                }
                mostPopularCallBackListener.onListMostPopularLoadSuccess(mostPopularModels);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                    mostPopularCallBackListener.onListMostPopularLoadFail(error.getMessage());
            }
        });
    }

    public MutableLiveData<String> getMessageError() {
        return messageError;
    }

    @Override
    public void onListMostPopularLoadSuccess(List<MostPopularModel> mostPopularModelList) {
        mostPopularListMutable.setValue(mostPopularModelList);
    }

    @Override
    public void onListMostPopularLoadFail(String message) {
        messageError.setValue(message);
    }
}
/*
van vat phu du

 */