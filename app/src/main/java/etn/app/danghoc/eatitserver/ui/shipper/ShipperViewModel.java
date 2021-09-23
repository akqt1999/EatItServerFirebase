package etn.app.danghoc.eatitserver.ui.shipper;

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

import etn.app.danghoc.eatitserver.callback.IShipperLocalCallbackListener;
import etn.app.danghoc.eatitserver.common.Common;
import etn.app.danghoc.eatitserver.model.ShipperModel;

public abstract class ShipperViewModel extends ViewModel implements IShipperLocalCallbackListener {
    private MutableLiveData<String> messageError = new MutableLiveData<>();
    private MutableLiveData<List<ShipperModel>> shipperMutableList;
    private IShipperLocalCallbackListener shipperLocalCallbackListener;

    public ShipperViewModel() {
        shipperLocalCallbackListener = this;
    }

    public MutableLiveData<String> getMessageError() {
        return messageError;
    }

    public MutableLiveData<List<ShipperModel>> getShipperMutableList() {
        if (shipperMutableList == null) {
            shipperMutableList = new MutableLiveData<>();
            loadShipper();
        }
        return shipperMutableList;
    }

    private void loadShipper() {
        List<ShipperModel> tempList = new ArrayList<>();
        DatabaseReference shipperRef = FirebaseDatabase.getInstance().getReference(Common.SHIPPER);
        shipperRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot shipperSnapShot : snapshot.getChildren()) {
                    ShipperModel shipperModel = shipperSnapShot.getValue(ShipperModel.class);
                    shipperModel.setKey(shipperSnapShot.getKey());
                    tempList.add(shipperModel);
                }

                shipperLocalCallbackListener.onShipperLoadSuccess(tempList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                shipperLocalCallbackListener.onShipperLoadFailed(error.getMessage());
            }
        });
    }

    @Override
    public void onShipperLoadSuccess(List<ShipperModel> shipperModelList) {
        if (shipperMutableList != null)
            shipperMutableList.setValue(shipperModelList);
    }

    @Override
    public void onShipperLoadFailed(String message) {
        messageError.setValue(message);
    }
}
