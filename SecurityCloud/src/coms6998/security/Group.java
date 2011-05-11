package coms6998.security;

import java.util.List;

public class Group {

	private List<User> users;
	private String key;
	public Group(List<User> users, String key)
	{
		this.users = users;
		this.key = key;
	}
	String getKey()
	{
		return key;
	}
	List<User> getUsers()
	{
		return users;
	}
	
	
	
}
