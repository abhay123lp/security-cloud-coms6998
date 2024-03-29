package coms6998.security;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import coms6998.security.s3.S3;

/**
 * @author Jaya
 *
 */
public class SecuredCloudManager {

    public static final String BUCKET_NAME = "jla2164";
    
    public static Map<String, User> userInfo;
    public static Map<String, Group> groupInfo;
    
    static {
        init();
    }

    public static User getUser(String username) {
        return userInfo.get(username);
    }

    public static Group getGroup(String groupname) {
        return groupInfo.get(groupname);
    }
    
    /**
     * @param args
     */
    public static void init() {
        // TODO Auto-generated method stub

        //final String BUCKET_NAME = "edu.columbia.cloud.test";

        // Create the User objects
        User user1 = User.getInstance("a@gmail.com", "password1");
        User user2 = User.getInstance("ab@gmail.com", "password2");
        User user3 = User.getInstance("abc@gmail.com", "password3");
        User user4 = User.getInstance("abcd@gmail.com", "password4");
        User user5 = User.getInstance("abcde@columbia.edu", "password5");

        userInfo = new HashMap<String, User>();
        userInfo.put(user1.getUsername(), user1);
        userInfo.put(user2.getUsername(), user2);
        userInfo.put(user3.getUsername(), user3);
        userInfo.put(user4.getUsername(), user4);
        userInfo.put(user5.getUsername(), user5);

        try {
            Group group1 = new Group("Group1");
            group1.addUserToGroup(user1);
            group1.addUserToGroup(user2);
            user1.addToGroup(group1);
            user2.addToGroup(group1);

            Group group2 = new Group("Group2");
            group2.addUserToGroup(user3);
            group2.addUserToGroup(user4);
            user3.addToGroup(group2);
            user4.addToGroup(group2);

            Group group3 = new Group("Group3");
            group3.addUserToGroup(user5);
            user5.addToGroup(group3);

            groupInfo = new HashMap<String, Group>();
            groupInfo.put(group1.getName(), group1);
            groupInfo.put(group2.getName(), group2);
            groupInfo.put(group3.getName(), group3);

        } catch (NoSuchAlgorithmException e1) {
            e1.printStackTrace();
        }
        
        S3 s3 = S3.getInstance();
        if(s3.getBucket(BUCKET_NAME) == null) {
            s3.createBucket(BUCKET_NAME);
        }
    }

}
