package com.atat.exceptions;

import java.util.ArrayList;

public class RegisterUserException extends Exception{
	private ArrayList<String> messageError=new ArrayList<String>();
	
	public RegisterUserException(String messageError){
		this.messageError.add(messageError) ;
	}
	
	public ArrayList<String> getMessageErrors(){
		return messageError;
	}
	
	public String getMessageErros(){
		StringBuilder messageErrorsString = new StringBuilder();
		for(int i=0;i<messageError.size();i++)
			messageErrorsString.append(messageError.get(i));
		return messageErrorsString.toString();
	}

}
