package bse.Data;

import bse.Commands.TestCreate;
import bse.Utility.JSONUtility;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
public class Test {
    private String[][] questions;
    private long userID;
    private String name;

    public Test (String name, long userID, ArrayList <String[]> questions) {
        this.name = name;
        this.userID = userID;
        this.questions = new String[questions.size ()][2];
        for (int index = 0; index < this.questions.length; index++) {
            this.questions[index] = questions.get (index);
        }
    }

    public String name () {
        return name;
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

    public static Test get (final String id) {
        JSONObject jsonObject = JSONUtility.load (JSONUtility.JSONFile.Tests);
        Iterator <String> keys = jsonObject.keys ();
        while (keys.hasNext ()) {
            final String key = keys.next ();
            JSONObject testJSONObject = jsonObject.getJSONObject (key);
            final String testID = testJSONObject.getString ("id");
            if (testID.equals (id)) {
                String name = key;
                long userID = testJSONObject.getLong ("userID");
                JSONArray questionsJSONArray = testJSONObject.getJSONArray ("questions");
                ArrayList <String[]> questionsArrayList = new ArrayList <String[]> ();
                for (int index = 0; index < questionsJSONArray.length (); index++) {
                    JSONArray questionJSONArray = questionsJSONArray.getJSONArray (index);
                    String[] question = {
                        questionJSONArray.getString (0),
                        questionJSONArray.getString (1)
                    };
                    questionsArrayList.add (question);
                }
                return new Test (name, userID, questionsArrayList);
            }
        }
        return null;
    }

    public String[] problems () {
        String[] problems = new String[questions.length];
        for (int index = 0; index < questions.length; index++) {
            problems[index] = questions[index][0];
        }
        return problems;
    }

    public int[] grade (String[] responses) {
        int score = 0;
        for (int index = 0; index < responses.length; index++) {
            String response = responses[index];
            response = TestCreate.simplifyAnswer (response);
            if (response.contains (questions[index][1])) {
                score += 1;
            }
        }
        return new int[] {score, responses.length};
    }
}
