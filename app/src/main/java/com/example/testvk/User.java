package com.example.testvk;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by 95tox on 28.02.2016.
 */
public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    public long uid;




    public static User parse(JSONObject o) throws JSONException {
        User u = new User();
        u.uid = Long.parseLong(o.getString("id"));

        return u;
    }


    public static ArrayList<User> parseUsers(JSONArray array) throws JSONException {
        ArrayList<User> users=new ArrayList<User>();
        //it may be null if no users returned
        //no users may be returned if we request users that are already removed
        if(array==null)
            return users;
        int category_count=array.length();
        for(int i=0; i<category_count; ++i){
            JSONObject o = (JSONObject)array.get(i);
            User u = User.parse(o);
            users.add(u);
        }
        return users;
    }
}