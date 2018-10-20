package example.lichong.recordvideo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import android.view.MotionEvent;


import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class CustomVideoCaptureActivity extends Activity  implements SensorEventListener{

    private  String DB_FILENAME = "MyTest.db";
    public  String TABLE_NAME = "VideoAndSensor";
    private  Copy_File mFileCopy;
    private  String mDBCopyOutputDir=" ";
    private boolean DB_Initstate=false;//数据库初始化状态
    private MyDatabaseDeal mSensorDB;
    private Date mDBUpdateTime;
    private long mDBStartTIme=0;
    Timer SensorTimer=null;//传感器采集定时任务
    Timer WrDBTimer=null;//写入数据定时任务\
    private boolean SensorTimer_State=true;


    private SensorManager mSensorManager;
    private  Sensor mSensorAccelerometer;
    private  Sensor mSensorGravity;
    private  Sensor mSensorGyroscope;
    private  Sensor mSensorLinearAcceleration;
    private  Sensor mSensorMagneticField;
    private  Sensor mSensorRotationvector;

    private  static  final int  TYPE_ORIENTATION =  999;


    private static  String gFileName_time=" ";
    TextView mTimerInfo;
    SurfaceView mPreview;
    SurfaceHolder mPreviewHolder;
    Camera mCamera;
    GestureDetector mGestureDetector;
    MediaRecorder mrec;
    File mOutputFile;

    long second = 0;
    boolean mCameraConfigured = false;
    boolean mInPreview = false;
    boolean mRecording = false;
    static int MEDIA_TYPE_VIDEO=1;
    static int MEDIA_TYPE_IMAGE=0;

    float[]  mAccelerometer;
    float[]  mGravity;
    float[]  mGyroscope;
    float[]  mLinearAcceleration;
    float[]  mMagneticField;
    float[]  mOrientation;
    float[]  mRotationVeCtor;

    ArrayList<Long> TimeList=new ArrayList<Long>();//采集时间
    ArrayList<Float> AccList=new ArrayList<Float>();
    ArrayList<Float> GraList=new ArrayList<Float>();
    ArrayList <Float> GyrList=new ArrayList<Float>();
    ArrayList <Float> LinearAcceList=new ArrayList<Float>();
    ArrayList <Float> MagList=new ArrayList<Float>();
    ArrayList <Float> OrienList=new ArrayList<Float>();
    ArrayList <Float> RotationVeCtorList=new ArrayList<Float>();

    long tt=0;


    private void initPreview() {
        if (mCamera != null && mPreviewHolder.getSurface() != null) {
            try {
                mCamera.setPreviewDisplay(mPreviewHolder);
            } catch (IOException e) {
                Toast.makeText(CustomVideoCaptureActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }

            if (!mCameraConfigured) {
                Camera.Parameters parameters = mCamera.getParameters();
                parameters.setPreviewFpsRange(30000, 30000);
                parameters.setPreviewSize(640, 360);
                mCamera.setParameters(parameters);
                mCameraConfigured = true;
            }
        }
    }

    private void startPreview() {
        if (mCameraConfigured && mCamera != null) {
            mCamera.startPreview();
            mInPreview = true;
        }
    }

    SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            initPreview();
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            startPreview();
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            releaseCamera();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preview);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        mTimerInfo = (TextView) findViewById(R.id.timer);
        mCamera = getCameraInstance();
        if(mCamera==null)
            mTimerInfo.setText("摄像头打开失败");
        else
            mTimerInfo.setText("单击选择开始录像");

        mSensorManager=(SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorAccelerometer=mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorGravity=mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        mSensorGyroscope=mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mSensorLinearAcceleration=mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mSensorMagneticField=mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mSensorRotationvector=mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        //采样时间20ms
//        mSensorManager.registerListener(this,mSensorAccelerometer,SensorManager.SENSOR_DELAY_GAME);
//        mSensorManager.registerListener(this,mSensorGravity,SensorManager.SENSOR_DELAY_GAME);
//        mSensorManager.registerListener(this,mSensorGyroscope,SensorManager.SENSOR_DELAY_GAME);
//        mSensorManager.registerListener(this,mSensorLinearAcceleration,SensorManager.SENSOR_DELAY_GAME);
//        mSensorManager.registerListener(this,mSensorMagneticField,SensorManager.SENSOR_DELAY_GAME);
//        mSensorManager.registerListener(this,mSensorRotationvector,SensorManager.SENSOR_DELAY_GAME);

        mPreview = (SurfaceView) findViewById(R.id.preview);
        mPreviewHolder = mPreview.getHolder();
        mPreviewHolder.addCallback(surfaceCallback);

        mFileCopy=new Copy_File();
        mGestureDetector=new GestureDetector(this);
        mGestureDetector.setBaseListener(new GestureDetector.BaseListener()
        {
            @Override
            public boolean onGesture(Gesture gesture){
                if(gesture == Gesture.TAP){
                    openOptionsMenu();
                    return true;
                }
                return  false;
            }
        });
    }


    private final Handler mHandler = new Handler();
    private final Runnable mUpdateTextRunnable = new Runnable() {
        @Override
        public void run() {
            if (mRecording) {
                mTimerInfo.setText(String.format("%02d:%02d:%02d",
                        TimeUnit.MILLISECONDS.toHours(second * 1000),
                        TimeUnit.MILLISECONDS.toMinutes(second * 1000) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(second * 1000)),
                        TimeUnit.MILLISECONDS.toSeconds(second * 1000) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(second * 1000))));
                second++;
            }
            mHandler.postDelayed(mUpdateTextRunnable, 1000);
        }
    };

    private final Runnable mSQLiteInsetRunnable = new Runnable() {
        @Override
        public void run() {
            if  (DB_Initstate&&!TimeList.isEmpty()) {
                mSensorDB.insertSensorDate(TimeList, AccList, GraList, GyrList, LinearAcceList, MagList, OrienList, RotationVeCtorList);
                TimeList.clear();
                AccList.clear();
                GraList.clear();
                GyrList.clear();
                LinearAcceList.clear();
                MagList.clear();
                OrienList.clear();
                RotationVeCtorList.clear();

                Log.d("mSQLiteInsetRunnable","定时器SQL写入完成");
            }
            mHandler.postDelayed(mSQLiteInsetRunnable, 20*1000);
        }
    };

    public boolean onGenericMotionEvent(MotionEvent event){
        if(mGestureDetector!=null){
            return mGestureDetector.onMotionEvent(event);
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.videocapture,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.vidonoff:
                if(item.getTitle().equals("开始录像")){//点击开始录像按钮
                    if (mRecording)
                        return true;
                    if (prepareVideoRecorder()) {
                        mrec.start();
                        mRecording = true;
                        second = 0;
                        mHandler.post(mUpdateTextRunnable);
                    } else {
                        mRecording=false;
                        releaseMediaRecorder();
                        return false;
                    }

                    //数据库初始化
                    DB_FILENAME=getDBFileName();
                    mDBCopyOutputDir=getDBFileDir();
                    mSensorDB=new MyDatabaseDeal(CustomVideoCaptureActivity.this,"v_"+DB_FILENAME,TABLE_NAME);
                    if(!mSensorDB.isDataExist()){ //表中没有数据则初始化
                        if(mSensorDB.initTable()){
                            Toast.makeText(CustomVideoCaptureActivity.this, "数据库初始化成功"+DB_FILENAME, Toast.LENGTH_LONG).show();
                            DB_Initstate=true;
                        }else {
                            Toast.makeText(CustomVideoCaptureActivity.this, "数据库初始化失败" + DB_FILENAME, Toast.LENGTH_LONG).show();
                            DB_Initstate = false;
                        }
                    }else {
                        Toast.makeText(CustomVideoCaptureActivity.this, "数据库已存在", Toast.LENGTH_LONG).show();
                        DB_Initstate=true;
                    }
                    mDBUpdateTime = new Date();
                    mHandler.post(mSQLiteInsetRunnable);
                    //定时任务1  将数据加入链表
                    if (SensorTimer == null) {SensorTimer = new Timer();}
                    SensorTimer.schedule(new TimerTask()
                    {
                        @Override
                        public void run() {
                            if(DB_Initstate)
                            {
                                if(mAccelerometer!=null&&mGravity!=null&&mGyroscope!=null&&mLinearAcceleration!=null&&mMagneticField!=null&&mOrientation!=null&&mRotationVeCtor!=null){
                                    if (mDBStartTIme == 0)
                                         mDBStartTIme = new Date().getTime();
                                    TimeList.add(new Date().getTime()-mDBStartTIme);
                                    AccList.add(mAccelerometer[0]);AccList.add(mAccelerometer[1]);AccList.add(mAccelerometer[2]);
                                    GraList.add(mGravity[0]);GraList.add(mGravity[1]);GraList.add(mGravity[2]);
                                    GyrList.add(mGyroscope[0]);GyrList.add(mGyroscope[1]);GyrList.add(mGyroscope[2]);
                                    LinearAcceList.add(mLinearAcceleration[0]);LinearAcceList.add(mLinearAcceleration[1]);LinearAcceList.add(mLinearAcceleration[2]);
                                    MagList.add(mMagneticField[0]);MagList.add(mMagneticField[1]);MagList.add(mMagneticField[2]);
                                    OrienList.add(mOrientation[0]);OrienList.add(mOrientation[1]);OrienList.add(mOrientation[2]);
                                    RotationVeCtorList.add(mRotationVeCtor[0]);RotationVeCtorList.add(mRotationVeCtor[1]);RotationVeCtorList.add(mRotationVeCtor[2]);
                                }
                            }
                        }
                    },100,100);   //50ms后开始采集，每隔50ms采集一次

  /*                   //数据库定时写入，2分钟一次
                    if (WrDBTimer == null) {WrDBTimer = new Timer();}
                    WrDBTimer.schedule(new TimerTask()
                    {
                        @Override
                        public void run() {
                            if  (DB_Initstate&&!TimeList.isEmpty())
                            {
                                //SensorTimer_State=false;//暂停采集
                                mSensorDB.insertSensorDate(TimeList,AccList,GraList,GyrList,LinearAcceList,MagList,OrienList,RotationVeCtorList);
                                TimeList.clear();
                                AccList.clear();
                                GraList.clear();
                                GyrList.clear();
                                LinearAcceList.clear();
                                MagList.clear();
                                OrienList.clear();
                                RotationVeCtorList.clear();

                            }
                        }
                    },2*60*1000,2*60*1000);   //每隔2分钟写入一次
*/
                    item.setTitle("停止录像");
                    item.setIcon(R.drawable.ic_stop);
                }
                else{//点击停止录像按钮
                    if(SensorTimer!=null)
                        SensorTimer.cancel();//取消采集任务
                    SensorTimer=null;
                    if(WrDBTimer!=null)
                        WrDBTimer.cancel();//取消写入任务
                    WrDBTimer=null;
                    if(!TimeList.isEmpty()){//将剩余数据写入
                        mDBUpdateTime = new Date();
                        mSensorDB.insertSensorDate(TimeList,AccList,GraList,GyrList,LinearAcceList,MagList,OrienList,RotationVeCtorList);
                        TimeList.clear();
                        AccList.clear();
                        GraList.clear();
                        GyrList.clear();
                        LinearAcceList.clear();
                        MagList.clear();
                        OrienList.clear();
                        RotationVeCtorList.clear();
                        tt=new Date().getTime()-mDBUpdateTime.getTime();
                        Log.d("Time","="+tt);
                    }
                    mDBStartTIme=0;
                    stopRec();
                    if(mFileCopy.CopyFile("/data/data/example.lichong.GlassUIElements/databases/"+"v_"+DB_FILENAME,mDBCopyOutputDir+DB_FILENAME)){
                        Toast.makeText(CustomVideoCaptureActivity.this, "数据库复制完成:"+mDBCopyOutputDir+DB_FILENAME, Toast.LENGTH_LONG).show();
                    }else
                        Toast.makeText(CustomVideoCaptureActivity.this, "数据库复制失败:"+mDBCopyOutputDir+DB_FILENAME, Toast.LENGTH_LONG).show();

                    item.setTitle("开始录像");
                    item.setIcon(R.drawable.ic_start);
                   // PlayVideo();

                }
                return  true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    void stopRec() {
        stopRecording();
        mTimerInfo.setText("录制完成");
        Toast.makeText(CustomVideoCaptureActivity.this, "Video 保存到" + mOutputFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
    }

    void PlayVideo(){
        Intent i = new Intent();
        i.setAction("com.google.glass.action.VIDEOPLAYER");
        i.putExtra("video_url", mOutputFile.getAbsolutePath());
        startActivity(i);
    }

    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open();
        } catch (Exception e) {
            Log.e("Camera.open", e.getMessage());
        }
        return c;
    }


    //获得视频文件名
    private static File getOutputMediaFile(int type){
        String dateStamp=new SimpleDateFormat("yyyy_MM_dd").format(new Date());
        File mediaStorageDir=new File("/mnt/sdcard/DCIM","MyVideoCapture/"+dateStamp);

        if(!mediaStorageDir.exists()){
            if(!mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp","failed to create directory");
                return null;
            }
        }

        gFileName_time=new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        File mediaFile;
        if(type==MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath(),gFileName_time+".jpg");
        }
        else if(type==MEDIA_TYPE_VIDEO){
            mediaFile=new File(mediaStorageDir.getPath(),gFileName_time+".mp4");
        }else return null;

        return mediaFile;
    }

    //获得DB文件名
    private static String getDBFileName(){

        return gFileName_time+".db";
    }

    //获得DB文件路径
    private static String getDBFileDir(){
        String dateStamp=new SimpleDateFormat("yyyy_MM_dd").format(new Date());
        File mediaStorageDir=new File("/mnt/sdcard/DCIM","MyVideoCapture/"+dateStamp);

        if(!mediaStorageDir.exists()){
            if(!mediaStorageDir.mkdirs()){
                Log.d("OutputDBDri","failed to create directory");
                return null;
            }
        }
        return mediaStorageDir.getPath()+"/";
    }


    public void onAccuracyChanged(Sensor sensor, int accuracy){
        //传感器精度变化
    }


    //int DB_Write_Time=50;//数据库写入时间间隔100ms
    @Override
    public final void onSensorChanged(SensorEvent event){
        if(event.sensor.getType()==Sensor.TYPE_ACCELEROMETER){
            mAccelerometer=event.values.clone();
        }

        if(event.sensor.getType()==Sensor.TYPE_GRAVITY){
            mGravity=event.values.clone();
        }

        if(event.sensor.getType()==Sensor.TYPE_GYROSCOPE){
            mGyroscope=event.values.clone();
        }


        if(event.sensor.getType()==Sensor.TYPE_LINEAR_ACCELERATION){
            mLinearAcceleration=event.values.clone();
        }

        if(event.sensor.getType()==Sensor.TYPE_MAGNETIC_FIELD){
            mMagneticField=event.values.clone();
        }

        if(event.sensor.getType()==Sensor.TYPE_ROTATION_VECTOR){
            mRotationVeCtor=event.values.clone();
        }

        //方向感应器需要特殊处理
        if(mAccelerometer!=null && mMagneticField!=null){
            float rotation[]=new float[16];
            float orientation[]=new float[3];
            boolean success=SensorManager.getRotationMatrix(rotation,orientation,mAccelerometer,mMagneticField);

            if(success){
                mOrientation=new float[3];
                SensorManager.getOrientation(rotation,mOrientation);
                mOrientation[0]=180+(float)Math.toDegrees(mOrientation[0]);
                mOrientation[1]=90+(float)Math.toDegrees(mOrientation[1]);
                mOrientation[2]=(float)Math.toDegrees(mOrientation[2]);
            }
        }


//        if(DB_Initstate)
//        {
//            if(new Date().getTime()-mDBUpdateTime.getTime()>DB_Write_Time) {
//                if (mDBStartTIme == 0)
//                    mDBStartTIme = new Date().getTime();
//
//                mSensorDB.insertSensorDate((new Date().getTime()) - mDBStartTIme, mAccelerometer, mGravity, mGyroscope, mLinearAcceleration, mMagneticField, mRotationVeCtor, mOrientation);
//                mDBUpdateTime = new Date();
//            }
//        }

    }

    private boolean prepareVideoRecorder(){
        if(mCamera!=null){
            mCamera.release();
        }
        mCamera=getCameraInstance();
        if(mCamera==null) return false;

        mrec=new MediaRecorder();
        mCamera.unlock();
        mrec.setCamera(mCamera);
        mrec.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mrec.setVideoSource(MediaRecorder.VideoSource.CAMERA);
/*
        // Set output file format
        mrec.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);

        // 这两项需要放在setOutputFormat之后
        mrec.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mrec.setVideoEncoder(MediaRecorder.VideoEncoder.H263);

        //mrec.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
        mrec.setVideoSize(320, 240);
        mrec.setVideoFrameRate(20);
*/
        mrec.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
        mrec.setPreviewDisplay(mPreviewHolder.getSurface());

        mOutputFile=getOutputMediaFile(MEDIA_TYPE_VIDEO);
        mrec.setOutputFile(mOutputFile.toString());

        try{
            mrec.prepare();
        }catch (Exception e){
            Log.e("mrec.prepare",e.getMessage());
            return  false;
        }
        return  true;
    }

    @Override
    protected void onPause(){
        if(SensorTimer!=null)
            SensorTimer.cancel();//取消采集任务
        SensorTimer=null;
        if(WrDBTimer!=null)
            WrDBTimer.cancel();//取消写入任务
        WrDBTimer=null;
        if(TimeList!=null&&!TimeList.isEmpty()){//将剩余数据写入
            mSensorDB.insertSensorDate(TimeList,AccList,GraList,GyrList,LinearAcceList,MagList,OrienList,RotationVeCtorList);
            TimeList.clear();
            AccList.clear();
            GraList.clear();
            GyrList.clear();
            LinearAcceList.clear();
            MagList.clear();
            OrienList.clear();
            RotationVeCtorList.clear();
        }
        mFileCopy.CopyFile("/data/data/example.lichong.GlassUIElements/databases/"+"v_"+DB_FILENAME,mDBCopyOutputDir+DB_FILENAME);
        super.onPause();
        releaseMediaRecorder();
        releaseCamera();
    }


    @Override
    protected void onResume(){
        super.onResume();
        //采样时间20ms
        mSensorManager.registerListener(this,mSensorAccelerometer,SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this,mSensorGravity,SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this,mSensorGyroscope,SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this,mSensorLinearAcceleration,SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this,mSensorMagneticField,SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this,mSensorRotationvector,SensorManager.SENSOR_DELAY_GAME);

    }

    @Override
    protected void onDestroy(){
        if(mRecording) {
            stopRec();
            PlayVideo();
        }
        mRecording=false;
        super.onDestroy();
    }

    private void releaseMediaRecorder(){
        if(mrec!=null){
            mrec.reset();
            mrec.release();
            mrec=null;
        }
    }

    private void releaseCamera(){
        if(mCamera!=null){
            mCamera.release();
            mCamera=null;
        }
    }

    protected void stopRecording(){
        if(mrec!=null){
            mrec.stop();
            mrec.release();
            mrec=null;
            releaseCamera();
            mRecording=false;
        }
    }
}
