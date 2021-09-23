package etn.app.danghoc.eatitserver.services;

import android.content.Intent;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;
import java.util.Random;

import etn.app.danghoc.eatitserver.MainActivity;
import etn.app.danghoc.eatitserver.common.Common;

public class MyFCMServices  extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        Map<String,String> dataRecv=remoteMessage.getData();
        if(dataRecv!=null)
        {
//ao
            if(dataRecv.get(Common.NOTI_TITILE).equals("New Order"))
            {
                    Intent intent   =new Intent(this, MainActivity.class);
                    intent.putExtra(Common.IS_OPEN_ACTIVITY_NEW_ORDER,true);

                Common.showNotifiCation(this,new Random().nextInt(),
                        dataRecv.get(Common.NOTI_TITILE),
                        dataRecv.get(Common.NOTI_CONTENT),
                        intent);//intent la khi nhan vao se vao trang cai activity cua intent do

            }else
            {
                Common.showNotifiCation(this,new Random().nextInt(),
                        dataRecv.get(Common.NOTI_TITILE),
                        dataRecv.get(Common.NOTI_CONTENT),
                        null);
            }

        }
    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        Common.updateToken(this,s,true,false); /// dang la server app nen isServer =true
    }
}
