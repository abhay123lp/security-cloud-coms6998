package coms6998.security;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import coms6998.security.s3.S3;
import coms6998.security.s3.S3Bucket;
import coms6998.security.FileObject.FilePermission;

/**
 * @author Jaya
 *
 */
public class SecuredCloudManager {

    public static Map<String, User> userInfo;
    public static Set<Group> groupInfo;

    {
        init();
    }

    public static User getUser(String username) {
        return userInfo.get(username);
    }

    /**
     * @param args
     */
    public static void init() {
        // TODO Auto-generated method stub

        //final String BUCKET_NAME = "edu.columbia.cloud.test";

        // Create the User objects
        User user1 = User.getInstance("jaya.allamsetty@gmail.com", "password");
        User user2 = User.getInstance("meetmerohitsonu@gmail.com", "password");
        User user3 = User.getInstance("mohankrishna10@gmail.com", "password");
        User user4 = User.getInstance("asthamalik8@gmail.com", "password");
        User user5 = User.getInstance("bk2409@columbia.edu", "password");

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

            groupInfo = new HashSet<Group>();
            groupInfo.add(group1);
            groupInfo.add(group2);
            groupInfo.add(group3);

        } catch (NoSuchAlgorithmException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        // create a single bucket for all the users		
        S3 s3 = S3.getInstance();
        S3Bucket bucket = s3.createBucket("jla2164");

        // create a file object and set permissions on it
        FileObject file = new FileObject("TestFile", "this is a sample file");
        file.setPermission(FilePermission.Group);


        try {
            user1.downloadFile("TestFile");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}
