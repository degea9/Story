package com.atat.models;

import com.atat.exceptions.RegisterUserException;

public class User {
	private String username;
	private String password;
	private String confirmPassword;
	
	public User(String username,String password,String confirmPassword) throws RegisterUserException{
		filter(username, password,confirmPassword);
		this.username = username;
		this.password = password;
		this.confirmPassword = confirmPassword;
	}
	
	public static void filter(String username,String password,String confirmPassword) throws RegisterUserException{
		if(username.length()<8) throw new RegisterUserException("username must be greater than or equals to 8 characters");
		if(password.length()<8) throw new RegisterUserException("password must be greater than or equals to 8 characters");
		if(!password.equals(confirmPassword)) throw new RegisterUserException("Password does not match the confirm password");
	}
	
	public String getUserName(){
		
		return username;
	}
	
	public String getPassword(){
		return password;
	}

}
