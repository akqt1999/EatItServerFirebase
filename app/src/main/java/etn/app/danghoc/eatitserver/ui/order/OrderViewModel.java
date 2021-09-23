package etn.app.danghoc.eatitserver.ui.order;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import etn.app.danghoc.eatitserver.callback.IOrderCallbackListener;
import etn.app.danghoc.eatitserver.common.Common;
import etn.app.danghoc.eatitserver.model.OrderModel;

public class OrderViewModel extends ViewModel implements IOrderCallbackListener {

    private MutableLiveData<List<OrderModel>> orderModelMutableLiveData;
    private MutableLiveData<String> messageError;

    private IOrderCallbackListener listener;

    public OrderViewModel(MutableLiveData<List<OrderModel>> orderModelMutableLiveData, MutableLiveData<String> messageError) {
        this.orderModelMutableLiveData = orderModelMutableLiveData;
        this.messageError = messageError;
    }

    public OrderViewModel() {
        orderModelMutableLiveData=new MutableLiveData<>();
        messageError=new MutableLiveData<>();
        listener=this;

    }

    public MutableLiveData<List<OrderModel>> getOrderModelMutableLiveData() {
        loadOrderByStatus(0);
        return orderModelMutableLiveData;
    }

    public void loadOrderByStatus(int status) {
        List<OrderModel> tempList = new ArrayList<>();
        Query orderRef = FirebaseDatabase.getInstance().getReference(Common.ORDER_REF)
                .orderByChild("orderStatus")
                .equalTo(status);
        orderRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot itemSnapShot:snapshot.getChildren())
                    {//14:19
                        OrderModel orderModel=itemSnapShot.getValue(OrderModel.class);
                        orderModel.setKey(itemSnapShot.getKey()); // quan trong
                        orderModel.setOrderNumber(itemSnapShot.getKey()); // quan trong con so qua ong co oi , ong co oi giup con di , ma ong co , ong co oi ,
                        tempList.add(orderModel);
                    }
                Log.d("avvv","templist id "+tempList.size());
                    listener.onOrderLoadSuccess(tempList);
                Log.d("avvv","check load data from firebase  ");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                    listener.onOrderLoadFailed(error.getMessage());
            }
        });
    }


    public void setOrderModelMutableLiveData(MutableLiveData<List<OrderModel>> orderModelMutableLiveData) {
        this.orderModelMutableLiveData = orderModelMutableLiveData;
    }

    public MutableLiveData<String> getMessageError() {
        return messageError;
    }

    public void setMessageError(MutableLiveData<String> messageError) {
        this.messageError = messageError;
    }

    @Override
    public void onOrderLoadSuccess(List<OrderModel> orderModelList) {
        if (orderModelList.size()>0)
        {
            Collections.sort(orderModelList,(orderModel,t1)->{
                if(orderModel.getCreateDate()<t1.getCreateDate())
                    return -1;
                return orderModel.getCreateDate()== t1.getCreateDate()?0:1;
            });
        }
        orderModelMutableLiveData.setValue(orderModelList);
    }

    @Override
    public void onOrderLoadFailed(String message) {
        messageError.setValue(message);
    }
}
/*
con da vi pham diu gi a ong co ,
khong duoc khoe khang
khong duoc coi thuong nguoi khac
khong duoc coi thuong nguoi khac
con phai luoonj giu cai ban chat khong co tien cua con
luc truoc con thang thang duoc 50tr , bay gio con muon thang it nhat 90tr , con co qua tham lam khong ong co oi ,
 */