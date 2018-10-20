package example.lichong.recordvideo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

public class MyDatabaseDeal  {
    private static final String TAG = "MyDatabaseDeal";
    // 列定义
    private final String[] DB_COLUMNS = new String[] {"Id", "CustomName","OrderPrice","Country"};
    private Context context;
    private MyDatabaseHelper mDBHelper;

    public MyDatabaseDeal(Context context,String DBname,String TABNAME){
        this.context=context;
        mDBHelper=new MyDatabaseHelper(context,DBname,TABNAME);
    }

    /**
     * 初始化数据
     */
    public boolean initTable(){
        SQLiteDatabase db = null;

        try {
            db = mDBHelper.getWritableDatabase();
            db.beginTransaction();

            db.execSQL("insert into " + mDBHelper.TABLE_NAME + " values (0,0, 0, 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0)");

            db.setTransactionSuccessful();
            return true;
        }catch (Exception e){
            Log.e(TAG, "", e);
        }finally {
            if (db != null) {
                db.endTransaction();
                db.close();
            }
        }
        return false;
    }

    /**
     * 新增一条数据
     * 数据依次为：时间（ms）、加速度计、重力传感器、陀螺仪、线性加速度计、磁场传感器、旋转矢量传感器、软件方向计算
     */
    public boolean insertSensorDate(long mTime,float[] Acc,float[] Gra,float[] Gyr,float[] LAcc,float[] Mag,float[] Rot,float[] Ori){
        SQLiteDatabase db = null;

        try {
            db = mDBHelper.getWritableDatabase();
            db.beginTransaction();

            // insert into Orders(Id, CustomName, OrderPrice, Country) values (7, "Jne", 700, "China");
            ContentValues contentValues = new ContentValues();
            //contentValues.put("ID", "NULL");
            contentValues.put("Time_ms", mTime);
            contentValues.put("AccelerometerX",Acc[0]);
            contentValues.put("AccelerometerY", Acc[1]);
            contentValues.put("AccelerometerZ", Acc[2]);

            contentValues.put("GravityX", Gra[0]);
            contentValues.put("GravityY", Gra[1]);
            contentValues.put("GravityZ", Gra[2]);

            contentValues.put("GyroscopeX", Gyr[0]);
            contentValues.put("GyroscopeY", Gyr[1]);
            contentValues.put("GyroscopeZ", Gyr[2]);

            contentValues.put("Linear_AccelerationX", LAcc[0]);
            contentValues.put("Linear_AccelerationY", LAcc[1]);
            contentValues.put("Linear_AccelerationZ", LAcc[2]);

            contentValues.put("MagneticX", Mag[0]);
            contentValues.put("MagneticY", Mag[1]);
            contentValues.put("MagneticZ", Mag[2]);

            contentValues.put("Rotation_VectorX", Rot[0]);
            contentValues.put("Rotation_VectorY", Rot[1]);
            contentValues.put("Rotation_VectorZ", Rot[2]);

            contentValues.put("Yaw", Ori[0]);
            contentValues.put("Pitch", Ori[1]);
            contentValues.put("Roll", Ori[2]);
            db.insertOrThrow(mDBHelper.TABLE_NAME, null, contentValues);

            db.setTransactionSuccessful();
            return true;
        }catch (SQLiteConstraintException e){
            Toast.makeText(context, "写入失败", Toast.LENGTH_SHORT).show();
        }catch (Exception e){
            Log.e(TAG, "", e);
        }finally {
            if (db != null) {
                db.endTransaction();
                db.close();
            }
        }
        return false;
    }



    /**
     * 新增一条数据  链表式
     * 数据依次为：时间（ms）、加速度计、重力传感器、陀螺仪、线性加速度计、磁场传感器、旋转矢量传感器、软件方向计算
     */
    public boolean insertSensorDate(ArrayList<Long> mTime,ArrayList<Float> Acc,ArrayList<Float> Gra,ArrayList<Float> Gyr,ArrayList<Float> LAcc,
                                    ArrayList<Float> Mag,ArrayList<Float> Rot,ArrayList<Float> Ori){
        SQLiteDatabase db = null;

        try {
            db = mDBHelper.getWritableDatabase();
            db.beginTransaction();

            // insert into Orders(Id, CustomName, OrderPrice, Country) values (7, "Jne", 700, "China");
            for(int index=0;index<mTime.size();index++)
            {
                ContentValues contentValues = new ContentValues();
                //contentValues.put("ID","NULL");
                contentValues.put("Time_ms", mTime.get(index));
                contentValues.put("AccelerometerX",Acc.get(index*3));
                contentValues.put("AccelerometerY", Acc.get(index*3+1));
                contentValues.put("AccelerometerZ", Acc.get(index*3+2));

                contentValues.put("GravityX", Gra.get(index*3));
                contentValues.put("GravityY", Gra.get(index*3+1));
                contentValues.put("GravityZ", Gra.get(index*3+2));

                contentValues.put("GyroscopeX", Gyr.get(index*3));
                contentValues.put("GyroscopeY", Gyr.get(index*3+1));
                contentValues.put("GyroscopeZ", Gyr.get(index*3+2));

                contentValues.put("Linear_AccelerationX", LAcc.get(index*3));
                contentValues.put("Linear_AccelerationY", LAcc.get(index*3+1));
                contentValues.put("Linear_AccelerationZ", LAcc.get(index*3+2));

                contentValues.put("MagneticX", Mag.get(index*3));
                contentValues.put("MagneticY", Mag.get(index*3+1));
                contentValues.put("MagneticZ", Mag.get(index*3+2));

                contentValues.put("Rotation_VectorX", Rot.get(index*3));
                contentValues.put("Rotation_VectorY", Rot.get(index*3+1));
                contentValues.put("Rotation_VectorZ", Rot.get(index*3+2));

                contentValues.put("Yaw", Ori.get(index*3));
                contentValues.put("Pitch",Ori.get(index*3+1));
                contentValues.put("Roll", Ori.get(index*3+2));

                db.insertOrThrow(mDBHelper.TABLE_NAME, null, contentValues);
            }
            Log.i(TAG,"传感器数据写入成功");
            db.setTransactionSuccessful();
            Toast.makeText(context, "传感器数据写入成功", Toast.LENGTH_SHORT).show();
            return true;
        }catch (SQLiteConstraintException e){
            Log.e(TAG,e.getMessage(), e);
            Toast.makeText(context, "写入失败", Toast.LENGTH_SHORT).show();
        }catch (Exception e){
            Log.e(TAG, e.getMessage(), e);
        }finally {
            if (db != null) {
                db.endTransaction();
                db.close();
            }
        }
        return false;
    }


    /**
     * 判断表中是否有数据
     */
    public boolean isDataExist(){
        int count = 0;

        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = mDBHelper.getReadableDatabase();
            // select count(Id) from Orders
            cursor = db.query(mDBHelper.TABLE_NAME, new String[]{"COUNT(Time_ms)"}, null, null, null, null, null);

            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
            if (count > 0) return true;
        }
        catch (Exception e) {
            Log.e(TAG, "", e);
        }
        finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return false;
    }

}
