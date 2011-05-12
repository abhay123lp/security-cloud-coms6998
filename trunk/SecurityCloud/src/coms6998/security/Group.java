package coms6998.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class Group {

	private List<User> users;
	private String key;
	private String name;
	
	public Group(String name) throws NoSuchAlgorithmException {
		this.name = name;
		this.generateKey();
		this.users = null;
	}
	
	public void addUserToGroup(User user) {
		users.add(user);
	}
	
	public boolean isMember(User user) {
		for (User u: users) {
			if (user.equals(u)) {
				return true;
			}
		}
		return false;
	}
	
	public void deleteUser(User user) {
		if (users.contains(user))
		this.users.remove(user);
	}
	
	// use the name of the file to create a 256-bit encryption key
	public void generateKey() throws NoSuchAlgorithmException {
		
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		byte[] hash = md.digest(this.name.getBytes());
		//System.out.println("Size of the key for the file "+this.fileName+ " is "+hash.length);
		this.key = new String(hash);
		//System.out.println("Key for the file is "+this.key);

	}
	
	public String getName() {
		return this.name;
	}
	
	public String getKey() {
		return this.key;
	}
	
}
