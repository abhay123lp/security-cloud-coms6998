/**
 * 
 */
package coms6998.security;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

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
		HashMap<Object, String> passwords = new HashMap<Object, String>();
		passwords.put("jaya.allamsetty@gmail.com", "password");
		passwords.put("meetmerohitsonu@gmail.com", "password");
		passwords.put("mohankrishna10@gmail.com", "password");
		passwords.put("asthamalik8@gmail.com", "password");
		passwords.put("bk2409@columbia.edu", "password");
		
		// Create the User objects
		User user1 = User.getInstance("jaya.allamsetty@gmail.com", "password");
		User user2 = User.getInstance("meetmerohitsonu@gmail.com", "password");
		User user3 = User.getInstance("mohankrishna10@gmail.com", "password");
		User user4 = User.getInstance("asthamalik8@gmail.com", "password");
		User user5 = User.getInstance("bk2409@columbia.edu", "password");

		try {
			Group group1 = new Group("Group1");
			group1.addUserToGroup(user1);
			group1.addUserToGroup(user2);
			
			Group group2 = new Group("Group2");
			group2.addUserToGroup(user3);
			group2.addUserToGroup(user4);
			
			Group group3 = new Group("Group3");
			group3.addUserToGroup(user5);
			
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
