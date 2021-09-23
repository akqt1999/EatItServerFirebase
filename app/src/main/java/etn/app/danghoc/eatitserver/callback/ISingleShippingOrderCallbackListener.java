package etn.app.danghoc.eatitserver.callback;

import etn.app.danghoc.eatitserver.model.ShippingOrderModel;

public interface ISingleShippingOrderCallbackListener {
    void onSingleShippingOrderLoadSuccess(ShippingOrderModel shippingOrderModel);
}
