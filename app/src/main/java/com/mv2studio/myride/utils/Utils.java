/*
 * Copyright (c) 2016 Localhost s.r.o. - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package com.mv2studio.myride.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.mv2studio.myride.App;
import com.mv2studio.myride.utils.permissions.OnPermissionsUpdatedListener;
import com.mv2studio.myride.utils.permissions.PermissionKind;
import com.mv2studio.myride.utils.permissions.PermissionsUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.Normalizer;
import java.util.Collection;
import java.util.List;
import java.util.Locale;


/**
 * Created by matej on 05/09/15.
 */
public class Utils {

    public static boolean isApiBelow(int api) {
        return Build.VERSION.SDK_INT < api;
    }

    public static boolean isApiAtleast(int api) {
        return !isApiBelow(api);
    }

    public static boolean isApiBelowEqual(int api) {
        return Build.VERSION.SDK_INT <= api;
    }

    public static boolean isPhoneNumberValid(String phoneNumber) {
        return ((phoneNumber.length() == 13 && phoneNumber.matches("\\+[0-9]{12}")) || // +421901234567
                (phoneNumber.length() == 10 && phoneNumber.matches("[0-9]{10}"))); //0901 234 567
    }

    public static boolean isEmpty(Collection collection) {
        return collection == null || collection.size() == 0;
    }

    public static boolean isEmpty(CharSequence text) {
        return TextUtils.isEmpty(text);
    }

    public static boolean isEmptyOrNull(CharSequence text) {
        return TextUtils.isEmpty(text) || "null".equals(text) || "NULL".equals(text);
    }

    public static String normalizeString(String text) {
        if (TextUtils.isEmpty(text)) return text;
        return Normalizer
                .normalize(text, Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "");
    }

    public static int compareNormalizedStrings(String left, String right) {
        if (TextUtils.isEmpty(left) && TextUtils.isEmpty(right)) return 0;
        else if (TextUtils.isEmpty(left)) return 1;
        else if (TextUtils.isEmpty(right)) return -1;
        return normalizeString(left).toLowerCase().compareTo(normalizeString(right).toLowerCase());
    }

    public static int dpToPx(int dp) {
        DisplayMetrics displayMetrics = App.getAppContext().getResources().getDisplayMetrics();
        return (int)((dp * displayMetrics.density) + 0.5);
    }

    public static int[] getScreenDimsne() {
        Display display = ((WindowManager) App.getAppContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        return new int[]{width, height};
    }

    public static int pxToDp(int px) {
        DisplayMetrics displayMetrics = App.getAppContext().getResources().getDisplayMetrics();
        return (int) ((px/displayMetrics.density) + 0.5);
    }

    public static void startCallIntent(Activity activity, String phoneNumber) {
        if (Utils.isEmpty(phoneNumber)) return;
        String tel = phoneNumber.startsWith("tel:") ? phoneNumber : "tel:" + phoneNumber;
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(tel));
        activity.startActivity(intent);
    }

    public static boolean makeACallSafely(Activity context, String phoneNumber, OnPermissionsUpdatedListener listener) {
        if (PermissionsUtils.getInstance().requestPermission(context, PermissionKind.PERMISSIONS_REQUEST_PHONE, listener)) {
            startCallIntent(context, phoneNumber);
            return true;
        }
        return false;
    }

    public static void openLocationIntent(String address, Activity activity) {
        Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + address);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        activity.startActivity(mapIntent);
    }

    public static void hideKeyboard(Activity activity) {
        if(activity == null) return;
        View view = activity.getCurrentFocus();
        if (view != null) {
            view.clearFocus();
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static void showKeyboard(EditText editText) {
        InputMethodManager imm = (InputMethodManager) App.getAppContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
    }

    public static void hideKeyboardOnTouchOutside(View view, final Activity activity) {
        if(!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    hideKeyboard(activity);
                    return false;
                }
            });
        }
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                hideKeyboardOnTouchOutside(innerView, activity);
            }
        }
    }

    public static String inputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder total = new StringBuilder();
        String line;
        while ((line = r.readLine()) != null) {
            total.append(line);
        }
        return total.toString();
    }

    public static String updateUrlParam(String queryString, String name, String value) {
        if (queryString != null) {
            queryString = queryString.replaceAll(name + "=.*?($|&)", "").replaceFirst("&$", "");
        }
        return TextUtils.isEmpty(queryString) ? (name + "=" + value) : (queryString + "&" + name + "=" + value);
    }

    /**
     * Returns
     * @param attrColor color you want to obtain - for example R.attr.colorAccent
     * @return
     */
    public static int getStyleColor(Activity activity, int attrColor) {
        TypedValue typedValue = new TypedValue();
        TypedArray a = activity.obtainStyledAttributes(typedValue.data, new int[] { attrColor });
        int color = a.getColor(0, 0);
        a.recycle();
        return color;
    }

    /**
     * Builds string containing items from <code>items</code> array, with spaces between each item and linebreaks
     * defined in <code>lineBreaks</code> list.
     * @param lineBreaks List of indexes after which has to be linebreak. indexes > items.length are ignored.
     * @param items items to concatenate
     * @return concatenated string or empty string if <code>items</code> is null or empty.
     */
    @NonNull
    public static String concatenateStrings(@Nullable List<Integer> lineBreaks, String... items) {
        StringBuilder builder = new StringBuilder();
        if (items == null) return "";

        for (int i = 0; i < items.length; i++) {
            boolean isLineBreakLast = builder.length() > 0 && builder.charAt(builder.length() - 1) == '\n';
            if (!Utils.isEmpty(items[i])) {
                if (builder.length() > 0 && !isLineBreakLast) builder.append(" ");
                builder.append(items[i]);
            }

            if (lineBreaks != null && lineBreaks.contains(i)
                    && !isLineBreakLast
                    && hasValidItemsAfterIndex(i, items)) builder.append("\n");
        }
        return builder.toString();
    }

    public static String concatenateStrings(String divider, String... items) {
        StringBuilder builder = new StringBuilder();
        if (items == null) return "";

        for (int i = 0; i < items.length; i++) {
            if (isEmpty(items[i])) continue;

            if (builder.length() > 0) builder.append(divider);
            builder.append(items[i]);
        }

        return builder.toString();
    }

    private static boolean hasValidItemsAfterIndex(int index, String... items) {
    for (int i = index + 1; i < items.length; i++) {
        if (!Utils.isEmpty(items[i])) return true;
    }
    return false;
    }

    public static String colorAsHex(int color) {
        return String.format("#%06X", (0xFFFFFF & color));
    }

    /**
     * Get ISO 3166-1 alpha-2 country code for this device (or null if not available)
     * @return country code or null
     */
    public static String getUserCountry() {
        try {
            final TelephonyManager tm = (TelephonyManager) App.getAppContext().getSystemService(Context.TELEPHONY_SERVICE);
            final String simCountry = tm.getSimCountryIso();
            if (simCountry != null && simCountry.length() == 2) { // SIM country code is available
                return simCountry.toLowerCase(Locale.getDefault());
            }
            else if (tm.getPhoneType() != TelephonyManager.PHONE_TYPE_CDMA) { // device is not 3G (would be unreliable)
                String networkCountry = tm.getNetworkCountryIso();
                if (networkCountry != null && networkCountry.length() == 2) { // network country code is available
                    return networkCountry.toLowerCase(Locale.getDefault());
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}
