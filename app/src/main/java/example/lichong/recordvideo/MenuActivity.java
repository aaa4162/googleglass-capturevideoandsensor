package example.lichong.recordvideo;

import android.app.Activity;
import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.os.Bundle;
import android.speech.tts.UtteranceProgressListener;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.util.HashMap;
import java.util.Locale;

public class MenuActivity extends Activity implements TextToSpeech.OnInitListener {

    private TextToSpeech tts;
    private boolean mAttachedToWindow;
    private boolean mTTSSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_menu);
        mTTSSelected=false;
    }

    @Override
    public void onAttachedToWindow(){
        super.onAttachedToWindow();
        mAttachedToWindow=true;
        openOptionsMenu();
    }

    @Override
    public void onDetachedFromWindow(){
        super.onDetachedFromWindow();
        mAttachedToWindow=false;
    }

    @Override
    public void openOptionsMenu(){
        if(mAttachedToWindow){
            super.openOptionsMenu();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){

            case R.id.tts:
                mTTSSelected=true;
                tts=new TextToSpeech(this,this);
                tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String utteranceId) { }

                    @Override
                    public void onDone(String utteranceId) {
                        if(tts!=null){
                            tts.stop();
                            tts.shutdown();
                        }
                    }

                    @Override
                    public void onError(String utteranceId) { }
                });
                return  true;

            case R.id.sensor:
                Intent intent_se=new Intent(MenuActivity.this,SensorActivity.class);
                startActivity(intent_se);
                return true;

            case R.id.customvideocapture:
                Intent intent_rv=new Intent(MenuActivity.this,CustomVideoCaptureActivity.class);
                startActivity(intent_rv);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onOptionsMenuClosed(Menu menu){
        if(!mTTSSelected)
            finish();
    }

    @Override
    public void onInit(int status){
        if(status==TextToSpeech.SUCCESS){
            int result=tts.setLanguage(Locale.US);
            if(result==TextToSpeech.LANG_MISSING_DATA || result==TextToSpeech.LANG_NOT_SUPPORTED){

            }else{
                HashMap<String,String>map=new HashMap<String, String>();
                map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID,"helloID");
                tts.speak("hello Glass，my name is lichong!",TextToSpeech.QUEUE_FLUSH,map);
            }
        }
    }
}
