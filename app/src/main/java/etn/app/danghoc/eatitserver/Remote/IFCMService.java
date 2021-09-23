package etn.app.danghoc.eatitserver.Remote;

import etn.app.danghoc.eatitserver.model.FCMResponse;
import etn.app.danghoc.eatitserver.model.FCMSendData;
import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAPz4KPeM:APA91bFs3sz0Um_BRPKp6jbetUs8LICHLViGMOFTTnrAyXk1WEyip7_u2u0C7u2nszOMeTWmasL_A18n7ZdO0jyygrmEh6hVmV1go2MWB9Bly65ym5jZEqHVhDOIzEWPDDGj23jAcdrg"



    })
    @POST("fcm/send")
    Observable<FCMResponse> sendNotification(@Body FCMSendData body);
}
