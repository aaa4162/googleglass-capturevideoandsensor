package example.lichong.recordvideo;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MyDatabaseHelper extends SQLiteOpenHelper {
    private static final int DB_VERSION = 1;
    //public static final String TABLE_NAME = "Orders";
    public  final String TABLE_NAME;

    public MyDatabaseHelper(Context context,String DBname,String TABNAME) {
        super(context, DBname, null, DB_VERSION);
        TABLE_NAME=new String(TABNAME);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // create table Orders(Id integer primary key, CustomName text, OrderPrice integer, Country text);
        Log.i(".onCreate", "my database helper create");
        //数据依次为：时间（ms）、加速度计、重力传感器、陀螺仪、线性加速度计、磁场传感器、旋转矢量传感器、软件方向计算
        String sql = "CREATE TABLE " + TABLE_NAME + " (ID INTEGER PRIMARY KEY,Time_ms INTEGER,AccelerometerX REAL,AccelerometerY REAL,AccelerometerZ REAL," +
                "GravityX REAL,GravityY REAL,GravityZ REAL,GyroscopeX REAL,GyroscopeY REAL,GyroscopeZ REAL," +
                "Linear_AccelerationX REAL,Linear_AccelerationY REAL,Linear_AccelerationZ REAL,MagneticX REAL, MagneticY REAL, MagneticZ REAL," +
                "Rotation_VectorX REAL,Rotation_VectorY REAL,Rotation_VectorZ REAL,Yaw REAL,Pitch REAL,Roll REAL)";
        sqLiteDatabase.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        String sql = "DROP TABLE IF EXISTS " + TABLE_NAME;
        sqLiteDatabase.execSQL(sql);
        onCreate(sqLiteDatabase);
    }
}
