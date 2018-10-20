package example.lichong.recordvideo;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.google.android.glass.timeline.LiveCard;

public class AppService extends Service {
    private static  final  String TAG="AppService";
    private static  final  String LIVE_CARD_ID="hello_Glass";

    private AppDrawer mCallback;
    private LiveCard mLiveCard;

    @Override
    public void onCreate(){
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent){
        return null;
    }

    @Override
    public int onStartCommand(Intent intent,int flags,int startId){
        if(mLiveCard==null){
            mLiveCard=new LiveCard(this,LIVE_CARD_ID);
            mCallback=new AppDrawer(this);
            mLiveCard.setDirectRenderingEnabled(true).getSurfaceHolder().addCallback(mCallback);
            Intent menuIntent = new Intent(this,MenuActivity.class);
            mLiveCard.setAction(PendingIntent.getActivity(this,0,menuIntent,0));
            mLiveCard.publish(LiveCard.PublishMode.REVEAL);
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy(){
        if(mLiveCard!=null&&mLiveCard.isPublished()){
            if(mCallback!=null){
                mLiveCard.getSurfaceHolder().removeCallback(mCallback);
            }
            mLiveCard.unpublish();
            mLiveCard=null;
        }
        super.onDestroy();
    }

}
