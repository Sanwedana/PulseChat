package com.example.pulsechat;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.text.format.DateUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Utils {

    private static final int[] AVATARS = {
            R.drawable.avatar_0, R.drawable.avatar_1, R.drawable.avatar_2,
            R.drawable.avatar_3, R.drawable.sakura_avatar
    };

    public static int avatarRes(int id) {
        return (id < 0 || id >= AVATARS.length) ? AVATARS[0] : AVATARS[id];
    }

    /** Same two users always produce the same chat id. */
    public static String chatId(String a, String b) {
        return a.compareTo(b) < 0 ? a + "_" + b : b + "_" + a;
    }

    public static String time(Date d) {
        if (d == null) return "";
        return new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(d);
    }

    public static String listTime(Date d) {
        if (d == null) return "";
        if (DateUtils.isToday(d.getTime())) return time(d);
        return new SimpleDateFormat("dd MMM", Locale.getDefault()).format(d);
    }

    public static String dayLabel(Date d) {
        if (d == null) return "";
        if (DateUtils.isToday(d.getTime())) return "Today";
        if (DateUtils.isToday(d.getTime() + DateUtils.DAY_IN_MILLIS)) return "Yesterday";
        return new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(d);
    }

    public static boolean sameDay(Date a, Date b) {
        if (a == null || b == null) return true;
        Calendar x = Calendar.getInstance(), y = Calendar.getInstance();
        x.setTime(a);
        y.setTime(b);
        return x.get(Calendar.YEAR) == y.get(Calendar.YEAR)
                && x.get(Calendar.DAY_OF_YEAR) == y.get(Calendar.DAY_OF_YEAR);
    }

    public static boolean isOnline(Context c) {
        ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        NetworkCapabilities caps = cm.getNetworkCapabilities(cm.getActiveNetwork());
        return caps != null && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }
}
