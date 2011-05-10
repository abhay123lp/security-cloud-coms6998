package coms6998.security;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.s3.AmazonS3Client;


public class User {

	private int id;
	private AmazonEC2 ec2;
	private AmazonS3Client s3;
	private String username;
	private String password;
	private List<Group> groups;
	private String oneTimePassword;
	private AwsProvision awsProvision;

	
	// HashMap to create/retrieve unique instances of the user
	private static final Map<Object, User> instances = new HashMap<Object, User>();

	public static User getInstance(String username, String password) {
		
		String key = username + password;
		User instance = instances.get(key);
		
		if (instance == null) {

			synchronized (instances) {

				// check again after synchronization
				instance = instances.get(key);
				if (instance == null) {
					instance = new User(username, password);

					// add it to the map
					instances.put(key, instance);
				}

			} // end of synchronized

		}// end of if loop
		return instance;

	}
	
	private User(String username, String password) {
		this.username = username;
		this.password = password;
		
		// create a unique instance of Amazon EC2
		awsProvision = AwsProvision.getInstance(username, username, "default", username);
		ec2 = awsProvision.getEC2Client();
		s3 = awsProvision.getS3Client();
	}
	
	public String getUsername() {
		return username;
	}
	
	public String getPassword() {
		return password;
	}
	
	public AmazonEC2 getEC2() {
		return ec2;
	}
	
	public AmazonS3Client getS3() {
		return s3;
	}
 	
	
	

}
