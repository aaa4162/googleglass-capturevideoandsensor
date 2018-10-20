package example.lichong.recordvideo;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

public class SensorActivity extends Activity implements SensorEventListener {
    private  static  final String TAG =  "SensorActiVity";

    private MyDatabaseDeal mSensorDB;
    private  String DB_FILENAME = "MyTest.db";
    public  String TABLE_NAME = "OnlySenseor";
    private  Copy_File mFileCopy;
    private  String mDBCopyOutputDir=" ";
    private boolean DB_Initstate=false;//数据库初始化状态
    private Date mDBUpdateTime;
    private long mDBStartTIme=0;

    private SensorManager mSensorManager;
    private  Sensor mSensorAccelerometer;
    private  Sensor mSensorGravity;
    private  Sensor mSensorGyroscope;
    private  Sensor mSensorLight;
    private  Sensor mSensorLinearAcceleration;
    private  Sensor mSensorMagneticField;
    private  Sensor mSensorRotationvector;

    private  static  final int  TYPE_ORIENTATION =  999;
    private  int mSensorSelected;
    private  Boolean mLogOn;
    private Date mSensorDataUpdateTime;

    private TextView mTextView;
    private GestureDetector mGestureDeteCtor;


    float[]  mAccelerometer;
    float[]  mGravity;
    float[]  mGyroscope;
    float[]  mLight;
    float[]  mLinearAcceleration;
    float[]  mMagneticField;
    float[]  mOrientation;
    float[]  mRotationVeCtor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sensor);
        mTextView=(TextView) findViewById(R.id.tvSensor);
        mTextView.setText("先单击选择传感器，然后点击开始采集");

        mLogOn=false;
        mSensorSelected=-1;
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mSensorManager=(SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorAccelerometer=mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorGravity=mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        mSensorGyroscope=mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mSensorLinearAcceleration=mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mSensorLight=mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        mSensorMagneticField=mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mSensorRotationvector=mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        //mDBOutputFile=getOutputDBFile();

        mFileCopy=new Copy_File();
        mGestureDeteCtor=new GestureDetector(this);
        mGestureDeteCtor.setBaseListener(new GestureDetector.BaseListener()
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

    public boolean onGenericMotionEvent(MotionEvent event){
        if(mGestureDeteCtor!=null){
            return mGestureDeteCtor.onMotionEvent(event);
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.sensor,menu);
        return true;
    }

    String display(float[] values){
        return  "\n"+values[0]+"\n"+values[1]+"\n"+values[2];
    }

    //选择菜单
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.logon:
                if(item.getTitle().equals("开始采集")){
                    mLogOn=true;

                    //DB文件名使用动态获取
                    DB_FILENAME=getDBFileName();
                    mDBCopyOutputDir=getDBFileDir();
                    mSensorDB=new MyDatabaseDeal(this,"s_"+DB_FILENAME,TABLE_NAME);
                    if(!mSensorDB.isDataExist()){ //表中没有数据则初始化
                        if(mSensorDB.initTable()){
                            Toast.makeText(SensorActivity.this, "数据库初始化成功"+DB_FILENAME, Toast.LENGTH_LONG).show();
                            DB_Initstate=true;
                        }else {
                            Toast.makeText(SensorActivity.this, "数据库初始化失败" + DB_FILENAME, Toast.LENGTH_LONG).show();
                            DB_Initstate = false;
                        }
                    }else {
                        Toast.makeText(SensorActivity.this, "数据库已存在", Toast.LENGTH_LONG).show();
                        DB_Initstate=true;
                    }
                    mDBUpdateTime = new Date();

                    item.setTitle("停止采集");
                    item.setIcon(R.drawable.ic_stop);
                }
                else{
                    mLogOn=false;

                    if(mFileCopy.CopyFile("/data/data/example.lichong.GlassUIElements/databases/"+"s_"+DB_FILENAME,mDBCopyOutputDir+DB_FILENAME)){
                        Toast.makeText(SensorActivity.this, "数据库复制完成:"+mDBCopyOutputDir+DB_FILENAME, Toast.LENGTH_LONG).show();
                    }else
                        Toast.makeText(SensorActivity.this, "数据库复制失败:"+mDBCopyOutputDir+DB_FILENAME, Toast.LENGTH_LONG).show();

                    item.setTitle("开始采集");
                    item.setIcon(R.drawable.ic_start);
                }
                return  true;
            case R.id.sendb_copy:
                if(mFileCopy.CopyFile("/data/data/example.lichong.GlassUIElements/databases/"+"s_"+DB_FILENAME,mDBCopyOutputDir+DB_FILENAME)){
                    Toast.makeText(SensorActivity.this, "数据库复制完成:"+mDBCopyOutputDir+DB_FILENAME, Toast.LENGTH_LONG).show();
                }else
                    Toast.makeText(SensorActivity.this, "数据库复制失败:"+mDBCopyOutputDir+DB_FILENAME, Toast.LENGTH_LONG).show();



            case R.id.accelerometer:
                mSensorSelected=Sensor.TYPE_ACCELEROMETER;
                if(mAccelerometer!=null)
                    mTextView.setText(getString(R.string.accelerometer)+":"+display(mAccelerometer));
                return true;

            case R.id.gravity:
                mSensorSelected=Sensor.TYPE_GRAVITY;
                if(mGravity!=null)
                    mTextView.setText(getString(R.string.gravity)+":"+display(mGravity));
                return true;

            case R.id.gyroscope:
                mSensorSelected=Sensor.TYPE_GYROSCOPE;
                if(mGyroscope!=null)
                    mTextView.setText(getString(R.string.gyroscope)+":"+display(mGyroscope));
                return true;

            case R.id.light:
                mSensorSelected=Sensor.TYPE_LIGHT;
                if(mLight!=null)
                    mTextView.setText(getString(R.string.light)+":"+display(mLight));
                return true;

            case R.id.linearacceleration:
                mSensorSelected=Sensor.TYPE_LINEAR_ACCELERATION;
                if(mLinearAcceleration!=null)
                    mTextView.setText(getString(R.string.linearacceleration)+":"+display(mLinearAcceleration));
                return true;

            case R.id.magneticfield:
                mSensorSelected=Sensor.TYPE_MAGNETIC_FIELD;
                if(mMagneticField!=null)
                    mTextView.setText(getString(R.string.magneticfield)+":"+display(mMagneticField));
                return true;

            case R.id.orientation:
                mSensorSelected=Sensor.TYPE_ORIENTATION;
                if(mOrientation!=null)
                    mTextView.setText(getString(R.string.orientation)+":\n"+"Yaw:"+mOrientation[0]+"°\n"
                    +"Pitch:"+mOrientation[1]+"°\n"+"Roll"+mOrientation[2]+"°");
                return true;
            case R.id.rotationvector:
                mSensorSelected=Sensor.TYPE_ROTATION_VECTOR;
                if(mRotationVeCtor!=null)
                    mTextView.setText(getString(R.string.rotationvector)+":"+display(mRotationVeCtor));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStart(){
        super.onStart();
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy){
    //传感器精度变化
    }
    //  传感器刷新速度20ms
    int UI_Refinsh_Time =100;//UI刷新时间100ms
    int DB_Write_Time=50;//数据库写入时间间隔100ms
    @Override
    public final void onSensorChanged(SensorEvent event){
        if(event.sensor.getType()==Sensor.TYPE_ACCELEROMETER){
            mAccelerometer=event.values.clone();
            if(mLogOn&&mSensorSelected==Sensor.TYPE_ACCELEROMETER){
                if(new Date().getTime()-mSensorDataUpdateTime.getTime()>UI_Refinsh_Time) {
                    mTextView.setText(getString(R.string.accelerometer) + ":" + display(mAccelerometer));
                    mSensorDataUpdateTime = new Date();
                }
            }
        }

        if(event.sensor.getType()==Sensor.TYPE_GRAVITY){
            mGravity=event.values.clone();
            if(mLogOn&&mSensorSelected==Sensor.TYPE_GRAVITY){
                if(new Date().getTime()-mSensorDataUpdateTime.getTime()> UI_Refinsh_Time) {
                    mTextView.setText(getString(R.string.gravity) + ":" + display(mGravity));
                    mSensorDataUpdateTime = new Date();
                }
            }
        }

        if(event.sensor.getType()==Sensor.TYPE_GYROSCOPE){
            mGyroscope=event.values.clone();
            if(mLogOn&&mSensorSelected==Sensor.TYPE_GYROSCOPE){
                if(new Date().getTime()-mSensorDataUpdateTime.getTime()>UI_Refinsh_Time) {
                    mTextView.setText(getString(R.string.gyroscope) + ":" + display(mGyroscope));
                    mSensorDataUpdateTime = new Date();
                }
            }
        }

        if(event.sensor.getType()==Sensor.TYPE_LIGHT){
            mLight=event.values.clone();
            if(mLogOn&&mSensorSelected==Sensor.TYPE_LIGHT){
                if(new Date().getTime()-mSensorDataUpdateTime.getTime()>UI_Refinsh_Time) {
                    mTextView.setText(getString(R.string.light) + ":" + mLight[0]);
                    mSensorDataUpdateTime = new Date();
                }
            }
        }

        if(event.sensor.getType()==Sensor.TYPE_LINEAR_ACCELERATION){
            mLinearAcceleration=event.values.clone();
            if(mLogOn&&mSensorSelected==Sensor.TYPE_LINEAR_ACCELERATION){
                if(new Date().getTime()-mSensorDataUpdateTime.getTime()> UI_Refinsh_Time) {
                    mTextView.setText(getString(R.string.linearacceleration) + ":" + display(mLinearAcceleration));
                    mSensorDataUpdateTime = new Date();
                }
            }
        }

        if(event.sensor.getType()==Sensor.TYPE_MAGNETIC_FIELD){
            mMagneticField=event.values.clone();
            if(mLogOn&&mSensorSelected==Sensor.TYPE_MAGNETIC_FIELD){
                if(new Date().getTime()-mSensorDataUpdateTime.getTime()>UI_Refinsh_Time) {
                    mTextView.setText(getString(R.string.magneticfield) + ":" + display(mMagneticField));
                    mSensorDataUpdateTime = new Date();
                }
            }
        }

        if(event.sensor.getType()==Sensor.TYPE_ROTATION_VECTOR){
            mRotationVeCtor=event.values.clone();
            if(mLogOn&&mSensorSelected==Sensor.TYPE_ROTATION_VECTOR){
                if(new Date().getTime()-mSensorDataUpdateTime.getTime()> UI_Refinsh_Time) {
                    mTextView.setText(getString(R.string.rotationvector) + ":" + display(mRotationVeCtor));
                    mSensorDataUpdateTime = new Date();
                }
            }
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
                if(mLogOn && mSensorSelected==Sensor.TYPE_ORIENTATION){
                    if(new Date().getTime()-mSensorDataUpdateTime.getTime()> UI_Refinsh_Time) {
                        mTextView.setText(getString(R.string.orientation) + ":\n" + "Yaw:" + mOrientation[0] + "°\n"
                                + "Pitch:" + mOrientation[1] + "°\n" + "Roll" + mOrientation[2] + "°");
                        mSensorDataUpdateTime = new Date();
                    }
                }
            }
        }

        if(mLogOn&&DB_Initstate)
        {
            if(new Date().getTime()-mDBUpdateTime.getTime()>DB_Write_Time) {
                if (mDBStartTIme == 0)
                    mDBStartTIme = new Date().getTime();

                mSensorDB.insertSensorDate((new Date().getTime()) - mDBStartTIme, mAccelerometer, mGravity, mGyroscope, mLinearAcceleration, mMagneticField, mRotationVeCtor, mOrientation);
                mDBUpdateTime = new Date();
            }
        }
    }

    //获得DB文件名
    private static String getDBFileName(){
        String timeStamp=new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        return timeStamp+".db";
    }

    //获得DB文件路径
    private static String getDBFileDir(){
        String dateStamp=new SimpleDateFormat("yyyy_MM_dd").format(new Date());
        File mediaStorageDir=new File("/mnt/sdcard/DCIM/SensorDB",dateStamp);

        if(!mediaStorageDir.exists()){
            if(!mediaStorageDir.mkdirs()){
                Log.d("OutputDBDri","failed to create directory");
                return null;
            }
        }
        return mediaStorageDir.getPath()+"/";
    }

    @Override
    protected void onResume(){
        super.onResume();
        //采样时间20ms
        mSensorManager.registerListener(this,mSensorAccelerometer,SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this,mSensorGravity,SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this,mSensorGyroscope,SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this,mSensorLight,SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this,mSensorLinearAcceleration,SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this,mSensorMagneticField,SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this,mSensorRotationvector,SensorManager.SENSOR_DELAY_GAME);

        mSensorDataUpdateTime=new Date();
    }

    @Override
    protected void onPause(){
        mFileCopy.CopyFile("/data/data/example.lichong.GlassUIElements/databases/"+"s_"+DB_FILENAME,mDBCopyOutputDir+DB_FILENAME);
        Log.i("Pause","Copy succeed");
        super.onPause();
        mSensorManager.unregisterListener(this);
    }
}
