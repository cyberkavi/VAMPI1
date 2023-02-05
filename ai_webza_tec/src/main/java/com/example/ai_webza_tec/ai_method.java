package com.example.ai_webza_tec;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;

import static android.content.Context.MODE_PRIVATE;

public class ai_method {
    public static String callTo_Key = "CONTACT";
    public static String sp_callTo_Name = "TEMP_CONTACT_LIST";
    public static Gson gson = new Gson();

    public static HashMap<String, String> getContactList(Activity activity, String names) {
        HashMap<String, String> c_no_name = new HashMap<String, String>();
        Uri lkup = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_FILTER_URI, names);
        ContentResolver cr = activity.getContentResolver();
        Cursor cur = cr.query(lkup, null, null, null, null);

        if ((cur != null ? cur.getCount() : 0) > 0) {
            while (cur != null && cur.moveToNext()) {
                String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                if (name.toLowerCase().contains(names.trim())) {

                    if (cur.getInt(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                        Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                new String[]{id}, null);
                        while (pCur.moveToNext()) {
                            String phoneNo = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            c_no_name.put(name.toLowerCase(), phoneNo);
                        }
                        pCur.close();
                    }
                }
            }
        }
        if(cur!=null){
            cur.close();
        }

        Gson gson = new Gson();
        String hashMapString = gson.toJson(c_no_name);

        //save in shared prefs
        SharedPreferences prefs = activity.getSharedPreferences(sp_callTo_Name, MODE_PRIVATE);
        prefs.edit().putString(callTo_Key, hashMapString).apply();

        //get from shared prefs
        //String storedHashMapString = prefs.getString(callTo_Key, "oopsDintWork");
        //java.lang.reflect.Type type = new TypeToken<HashMap<String, String>>(){}.getType();
        //HashMap<String, String> testHashMap2 = gson.fromJson(hashMapString, type);

        //use values
        //for (String i : testHashMap2.keySet()) {
        //    Log.d("cno_list",i);
        //}

        return c_no_name;
    }

    public static void getContactFromArray(int i,Activity activity){

        SharedPreferences prefs = activity.getSharedPreferences(sp_callTo_Name, MODE_PRIVATE);
        String storedHashMapString = prefs.getString(callTo_Key, "oopsDintWork");
        java.lang.reflect.Type type = new TypeToken<HashMap<String, String>>(){}.getType();
        HashMap<String, String> testHashMap2 = gson.fromJson(storedHashMapString, type);
        Object firstKey = testHashMap2.keySet().toArray()[i];
        String num = getValueByKey(testHashMap2,firstKey);
        makeCall(activity,num);
        clearContactListSavedData(activity);
    }

    public static String getValueByKey(HashMap<String, String> hashMap, Object firstKey){
        if (String.valueOf(firstKey.toString().charAt(0)).equals(" ")){
            firstKey = firstKey.toString().substring(1);
        }
        String value = hashMap.get(firstKey);
        return value;
    }

    public static Boolean checkForPreviousCallList(Activity activity){
        Boolean b = false;
        SharedPreferences prefs = activity.getSharedPreferences(sp_callTo_Name, MODE_PRIVATE);
        String data = prefs.getString(callTo_Key, "oopsDintWork");
        if (!data.equals("oopsDintWork")){
            b = true;
        }
        return b;
    }

    public static String makeCallFromSavedContactList(Activity activity,String name){
        //get from shared prefs
        String response = "No contact found";
        SharedPreferences prefs = activity.getSharedPreferences(sp_callTo_Name, MODE_PRIVATE);
        String storedHashMapString = prefs.getString(callTo_Key, "oopsDintWork");
        java.lang.reflect.Type type = new TypeToken<HashMap<String, String>>(){}.getType();
        HashMap<String, String> testHashMap2 = gson.fromJson(storedHashMapString, type);

        String num = getValueByKey(testHashMap2,name);
        if (num != null) {
            makeCall(activity, num);
            response = "calling "+name;
        }
        clearContactListSavedData(activity);
        //use values
        /*for (String i : testHashMap2.keySet()) {
            Log.d("cno_list",i);
        }*/
        return response;
    }

    public static void clearContactListSavedData(Activity activity){
        SharedPreferences prefs = activity.getSharedPreferences(sp_callTo_Name, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(callTo_Key);
        editor.commit();
    }

    public static void makeCall(Activity activity,String number){
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + number));
        activity.startActivity(intent);
    }
}
