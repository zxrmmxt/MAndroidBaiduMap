package com.xt.m_baidu_map;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.baidu.trace.model.CoordType;
import com.baidu.trace.model.TraceLocation;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by baidu on 17/1/23.
 */

public class ABaiduCommonUtil {

    private static DecimalFormat df = new DecimalFormat("######0.00");

    public static final double DISTANCE = 0.0001;

    public static String getCurProcessName(Context context) {
        int pid = android.os.Process.myPid();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo appProcess : activityManager.getRunningAppProcesses()) {
            if (appProcess.pid == pid) {
                return appProcess.processName;
            }
        }
        return "";
    }

    /**
     * 获取当前时间戳(单位：秒)
     *
     * @return
     */
    public static long getCurrentTime() {
        return System.currentTimeMillis() / 1000;
    }

    /**
     * 校验double数值是否为0
     *
     * @param value
     * @return
     */
    public static boolean isEqualToZero(double value) {
        return Math.abs(value - 0.0) < 0.01 ? true : false;
    }

    /**
     * 经纬度是否为(0,0)点
     *
     * @return
     */
    public static boolean isZeroPoint(double latitude, double longitude) {
        return isEqualToZero(latitude) && isEqualToZero(longitude);
    }

    /**
     * 将字符串转为时间戳
     */
    public static long toTimeStamp(String time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                Locale.CHINA);
        Date date;
        try {
            date = sdf.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
        return date.getTime() / 1000;
    }

    /**
     * 获取时分秒
     *
     * @param timestamp 时间戳（单位：毫秒）
     * @return
     */
    public static String getHMS(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        try {
            return sdf.format(new Timestamp(timestamp));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return String.valueOf(timestamp);
    }

    /**
     * 获取年月日 时分秒
     *
     * @param timestamp 时间戳（单位：毫秒）
     * @return
     */
    public static String formatTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            return sdf.format(new Timestamp(timestamp));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return String.valueOf(timestamp);
    }

    public static String formatSecond(int second) {
        String format = "%1$,02d:%2$,02d:%3$,02d";
        Integer hours = second / (60 * 60);
        Integer minutes = second / 60 - hours * 60;
        Integer seconds = second - minutes * 60 - hours * 60 * 60;
        Object[] array = new Object[]{hours, minutes, seconds};
        return String.format(format, array);
    }

    public static final String formatDouble(double doubleValue) {
        return df.format(doubleValue);
    }

    /**
     * 计算x方向每次移动的距离
     */
    public static double getXMoveDistance(double slope) {
        if (slope == Double.MAX_VALUE) {
            return DISTANCE;
        }
        return Math.abs((DISTANCE * slope) / Math.sqrt(1 + slope * slope));
    }

    /**
     * 根据点和斜率算取截距
     */
    public static double getInterception(double slope, LatLng startPoint) {
        return startPoint.latitude - slope * startPoint.longitude;
    }

    /**
     * 算斜率
     */
    public static double getSlope(LatLng fromPoint, LatLng toPoint) {
        if (toPoint.longitude == fromPoint.longitude) {
            return Double.MAX_VALUE;
        }
        return (toPoint.latitude - fromPoint.latitude) / (toPoint.longitude - fromPoint.longitude);
    }

    /**
     * 根据两点算取图标转的角度
     */
    public static double getAngle(LatLng fromPoint, LatLng toPoint) {
        double slope = getSlope(fromPoint, toPoint);
        if (slope == Double.MAX_VALUE) {
            if (toPoint.latitude > fromPoint.latitude) {
                return 0;
            } else {
                return 180;
            }
        }
        float deltAngle = 0;
        if ((toPoint.latitude - fromPoint.latitude) * slope < 0) {
            deltAngle = 180;
        }
        double radio = Math.atan(slope);
        return 180 * (radio / Math.PI) + deltAngle - 90;
    }

    /**
     * 保存当前定位点
     */
//    public static void saveCurrentLocation(TrackApplication trackApp) {
//        SharedPreferences.Editor editor = trackApp.trackConf.edit();
//        StringBuffer locationInfo = new StringBuffer();
//        locationInfo.append(CurrentLocation.locTime);
//        locationInfo.append(";");
//        locationInfo.append(CurrentLocation.latitude);
//        locationInfo.append(";");
//        locationInfo.append(CurrentLocation.longitude);
//        editor.putString(Constants.LAST_LOCATION, locationInfo.toString());
//        editor.apply();
//    }

    /**
     * 获取设备IMEI码
     *
     * @param context
     * @return
     */
    public static String getImei(Context context) {
        String imei;
        try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return null;
            }
            imei = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
        } catch (Exception e) {
            imei = "myTrace";
        }
        return imei;
    }

    /****************************************地图坐标和轨迹坐标之间转换*********************************************/
    /**
     * 将地图坐标转换轨迹坐标
     *
     * @param latLng
     * @return
     */
    public static com.baidu.trace.model.LatLng convertMap2Trace(LatLng latLng) {
        return new com.baidu.trace.model.LatLng(latLng.latitude, latLng.longitude);
    }

    /**
     * 将轨迹坐标对象转换为地图坐标对象
     *
     * @param traceLatLng
     * @return
     */
    public static LatLng convertTrace2Map(com.baidu.trace.model.LatLng traceLatLng) {
        return new LatLng(traceLatLng.latitude, traceLatLng.longitude);
    }

    /**
     * 将轨迹实时定位点转换为地图坐标
     *
     * @param location
     *
     * @return
     */
    public static LatLng convertTraceLocation2Map(TraceLocation location) {
        if (null == location) {
            return null;
        }
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        if (Math.abs(latitude - 0.0) < 0.000001 && Math.abs(longitude - 0.0) < 0.000001) {
            return null;
        }
        LatLng currentLatLng = new LatLng(latitude, longitude);
        if (CoordType.wgs84 == location.getCoordType()) {
            LatLng sourceLatLng = currentLatLng;
            CoordinateConverter converter = new CoordinateConverter();
            converter.from(CoordinateConverter.CoordType.GPS);
            converter.coord(sourceLatLng);
            currentLatLng = converter.convert();
        }
        return currentLatLng;
    }

    /****************************************地图坐标和轨迹坐标之间转换*********************************************/

}
