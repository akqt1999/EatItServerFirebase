package etn.app.danghoc.eatitserver.callback;

import java.util.List;

import etn.app.danghoc.eatitserver.model.OrderModel;
import etn.app.danghoc.eatitserver.ui.order.OrderViewModel;

public interface IOrderCallbackListener {
    void onOrderLoadSuccess(List<OrderModel>orderModelList);
    void onOrderLoadFailed(String message);
}
