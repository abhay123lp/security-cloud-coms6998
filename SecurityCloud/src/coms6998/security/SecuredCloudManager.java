/**
 * 
 */
package coms6998.security;

import coms6998.security.s3.S3;
import coms6998.security.s3.S3Bucket;
import coms6998.security.FileObject.FilePermission;

/**
 * @author Jaya
 *
 */
public class SecuredCloudManager {

    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub

        final String BUCKET_NAME = "edu.columbia.cloud.test";

        String username = "user";
        String password = "passwordpassword";

        // create a single bucket for all the users		
        S3 s3 = S3.getInstance();
        S3Bucket bucket = s3.createBucket("jla2164");

        // create a user
        User user = User.getInstance(username, password);

        // create a file object and set permissions on it
        FileObject file = new FileObject("TestFile", "this is sample file");
        file.setPermission(FilePermission.Private);

        try {
            user.uploadFile(file);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            user.downloadFile("TestFile");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}
