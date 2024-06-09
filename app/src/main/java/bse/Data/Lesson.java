package bse.Data;

import bse.Utility.FileUtility;
import bse.Utility.JSONUtility;
import org.json.JSONArray;
import org.json.JSONObject;

import java.security.Key;
import java.util.ArrayList;
import java.util.Iterator;
public class Lesson {
    private long userID;
    private String name;
    private String[] pages;

    public String[] pages () {
        return pages;
    }

    public String id () {
        String id = "";
        id += name.toLowerCase ();
        id += userID;
        String[] words = id.split (" ");
        id = "";
        for (final String word : words) {
            id += word;
        }
        return id;
    }

    public String name () {
        return name;
    }

    public Lesson (final long userID, final String name, final String[] pages) {
        this.userID = userID;
        this.name = name;
        this.pages = pages;
    }

    public Lesson (final long userID, final String name, final ArrayList <String> pages) {
        this.userID = userID;
        this.name = name;
        this.pages = new String[pages.size ()];
        for (int index = 0; index < this.pages.length; index++) {
            final String page = pages.get (index);
            this.pages[index] = page;
        }
    }

    public Lesson (final long userID, final String name, final JSONArray pages) {
        this.userID = userID;
        this.name = name;
        this.pages = new String[pages.length ()];
        for (int index = 0; index < this.pages.length; index++) {
            final String page = pages.getString (index);
            this.pages[index] = page;
        }
    }

    public static Lesson get (String id) {
        JSONObject lessonsJSONObject = JSONUtility.load (JSONUtility.JSONFile.Lessons);
        Iterator <String> keys = lessonsJSONObject.keys ();
        while (keys.hasNext ()) {
            final String key = keys.next ();
            final JSONObject lessonJSONObject = lessonsJSONObject.getJSONObject (key);
            final String lessonID = lessonJSONObject.getString ("id");
            if (lessonID.equals (id)) {
                String name = key;
                long userID = lessonJSONObject.getLong ("userID");
                JSONArray jsonArray = lessonJSONObject.getJSONArray ("pages");
                return new Lesson (userID, name, jsonArray);
            }
        }
        return null;
    }
}
