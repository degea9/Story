package com.atat.story;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.atat.exceptions.RegisterUserException;
import com.atat.json.JSONParser;
import com.atat.models.DatabaseHandler;
import com.atat.models.User;
import com.atat.models.UserFunctions;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import android.widget.Toast;

@SuppressLint("ValidFragment")
public class RegisterDialog extends DialogFragment {

	private User user;
	private View inflator;
	// Progress Dialog
	private ProgressDialog pDialog;
	// url to get all chapters list
	private static String url_register_user = "http://192.168.22.1/story/slim/v1/register";
	//private static String url_register_user = "http://tuandang.esy.es/story/android_connect/register_user.php";
	// JSON Node names
	private static final String TAG_ERROR = "error";

	// Creating JSON Parser object
	JSONParser jParser ;
	JSONArray user_details;
	private String error;
	private String username;
	private String api_key;
	private String type;
	public RegisterDialog() {

	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		jParser = new JSONParser(getActivity().getApplicationContext());
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		// Get the layout inflater
		LayoutInflater inflater = getActivity().getLayoutInflater();
		inflator = inflater.inflate(R.layout.register_dialog, null);

		// Inflate and set the layout for the dialog
		// Pass null as the parent view because its going in the dialog layout
		builder.setView(inflator)
				// Add action buttons
				.setPositiveButton("Register",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {

							}
						})
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								RegisterDialog.this.getDialog().cancel();
							}
						});
		return builder.create();
	}

	@Override
	public void onStart() {
		super.onStart();
		AlertDialog d = (AlertDialog) getDialog();
		final EditText edt_Username = (EditText) inflator
				.findViewById(R.id.register_username);
		
		final EditText edt_Password = (EditText) inflator
				.findViewById(R.id.register_password);
		final EditText edt_rePassword = (EditText) inflator
				.findViewById(R.id.re_enter_password);
		if (d != null) {
			Button positiveButton = (Button) d
					.getButton(Dialog.BUTTON_POSITIVE);
			positiveButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					boolean wantToCloseDialog = false;
					try {
						username = edt_Username.getText().toString();
						user = new User(edt_Username.getText().toString(),
								edt_Password.getText().toString(),
								edt_rePassword.getText().toString());

						wantToCloseDialog = true;
					} catch (RegisterUserException e) {
						Toast.makeText(getActivity().getApplicationContext(),
								e.getMessageErros(), Toast.LENGTH_LONG).show();
					}
					Toast.makeText(getActivity().getApplicationContext(),
							"Register click", Toast.LENGTH_SHORT).show();
					if (wantToCloseDialog) {
						// dismiss();
						new RegisterUser(getActivity().getApplicationContext())
								.execute();
					}
					// else dialog stays open. Make sure you have an obvious way
					// to close the dialog especially if you set cancellable to
					// false.
				}
			});
		}
	}

	class RegisterUser extends AsyncTask<String, String, String> {
		private Context context;

		public RegisterUser(Context context) {
			this.context = context;
		}

		/**
		 * Before starting background thread Show Progress Dialog
		 * */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(getActivity());
			pDialog.setMessage("Loading . Please wait...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
		}

		/**
		 * getting All chapters from url
		 * */
		protected String doInBackground(String... args) {
			// Building Parameters
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("name", user.getUserName()));
			params.add(new BasicNameValuePair("password", user.getPassword()));
			// getting JSON string from URL
			JSONObject json = jParser.makeHttpRequest(url_register_user,
					"POST", params);

			// Check your log cat for JSON reponse
			Log.d("json: ", json.toString());
			try {
				// Checking for SUCCESS TAG
				error = json.getString(TAG_ERROR);
				if(error.equals("false")){
					api_key = json.getString("apiKey");
					type = json.getString("type");
				}
				

			} catch (JSONException e) {
				e.printStackTrace();
			}

			return null;
		}

		/**
		 * After completing background task Dismiss the progress dialog
		 * **/
		protected void onPostExecute(String file_url) {
			// dismiss the dialog after getting all products
			pDialog.dismiss();
			if (error.equals("false")) {
				RegisterDialog.this.dismiss();
				 DatabaseHandler db = new DatabaseHandler(context);
				 UserFunctions userFunction = new UserFunctions();
				 // Clear all previous data in database
                 userFunction.logoutUser(context);
                 db.addUser(username,api_key,type);  
				Toast.makeText(context,
						"Congratulations! You Have Successfully Registered",
						Toast.LENGTH_SHORT).show();

			} else {
				Toast.makeText(context,
						"Registration Failed - Username already exists",
						Toast.LENGTH_SHORT).show();
			}
		}

	}

}
