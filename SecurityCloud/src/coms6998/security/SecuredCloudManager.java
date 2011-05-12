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
		
		//final String BUCKET_NAME = "edu.columbia.cloud.test";
		
		// create a single bucket for all the users		
		S3 s3 = S3.getInstance();
		S3Bucket bucket = s3.createBucket("jla2164");
		
		// create 3 users
		User user1 = User.getInstance("user1", "password");
		User user2 = User.getInstance("user2", "password");
		User user3 = User.getInstance("user3", "password");
		
		// create 3 groups
		

		
		
		
		// create a file object and set permissions on it
		FileObject file = new FileObject("TestFile", "this is a sample file");
		file.setPermission(FilePermission.Private);
		
		try {
			user1.uploadFile(file);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			user1.downloadFile("TestFile");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}

}
