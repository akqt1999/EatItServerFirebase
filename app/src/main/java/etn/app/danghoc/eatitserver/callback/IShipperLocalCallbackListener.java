package etn.app.danghoc.eatitserver.callback;

import android.widget.Button;
import android.widget.RadioButton;

import androidx.appcompat.app.AlertDialog;

import java.util.List;

import etn.app.danghoc.eatitserver.model.OrderModel;
import etn.app.danghoc.eatitserver.model.ShipperModel;

public interface IShipperLocalCallbackListener {
    void onShipperLoadSuccess(List<ShipperModel>shipperModelList);
    void onShipperLoadSuccess12(int pos, OrderModel orderModel, List<ShipperModel>shipperModels
                                , AlertDialog dialog,
                              Button btn_ok, Button btn_cancel,
                              RadioButton rdi_shipping,RadioButton rdi_shipper,
                              RadioButton rdi_canceled,RadioButton rdi_delete,RadioButton rdi_restore_place);

    void onShipperLoadFailed(String message);
}
/*
chap nhan thoi tri oi khong tuoc gi dau
 */