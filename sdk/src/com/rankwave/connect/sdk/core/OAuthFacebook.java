package com.rankwave.connect.sdk.core;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;
import com.rankwave.connect.sdk.Connect;
import com.rankwave.connect.sdk.ConnectCallback;
import com.rankwave.connect.sdk.SnsType;

public class OAuthFacebook {

	private ConnectCallback<com.rankwave.connect.sdk.Session> connectCallback = null;

	private static OAuthFacebook instance = null;

	public static OAuthFacebook getInstance() {

		if (instance == null) {

			synchronized (OAuthFacebook.class) {
				instance = new OAuthFacebook();
			}
		}

		return instance;
	}

	public OAuthFacebook() {

	}

	public static com.facebook.Session.StatusCallback fb_session_callback = new com.facebook.Session.StatusCallback() {

		@Override
		public void call(com.facebook.Session session,
				com.facebook.SessionState state, Exception exception) {

			if (state == SessionState.OPENED
					|| state == SessionState.OPENED_TOKEN_UPDATED) {

				Log.i(Connect.TAG, session.getAccessToken());

				final String access_token = session.getAccessToken();
				
				com.facebook.Request request = com.facebook.Request.newMeRequest(session,
						new com.facebook.Request.GraphUserCallback() {
							@Override
							public void onCompleted(GraphUser user, com.facebook.Response response) {
								// If the response is successful
								
								if (user != null) {
									String sns_id = user.getId();
									String name = user.getName();
									String profile_url = "https://graph.facebook.com/" + sns_id + "/picture?type=large";
									String birthday = user.getBirthday();
									if(birthday != null && !birthday.equals("")){
										birthday = birthday.replaceAll("/", "");
										birthday = birthday.substring(4, 8) + birthday.substring(0, 2) + birthday.substring(2, 4);
									}
									
									Connect.getActiveSession().getUser().getSnsInfo().clearInfo();
									
									Connect.getActiveSession().getUser().getSnsInfo().setSnsId(sns_id);
									Connect.getActiveSession().getUser().getSnsInfo().setName(name);
									Connect.getActiveSession().getUser().getSnsInfo().setProfileUrl(profile_url);
									Connect.getActiveSession().getUser().getSnsInfo().setSnsType(SnsType.SNS_TYPE_FACEBOOK);
									Connect.getActiveSession().getUser().getSnsInfo().setAccessToken(access_token);
									Connect.getActiveSession().getUser().getSnsInfo().setTokenSecret("");
									Connect.getActiveSession().getUser().getSnsInfo().setBirthday(birthday);
								}
								
								if (response.getError() != null) {
									// Handle errors, will do so later.
								}
								
								Connect.setFacebookToken(access_token, getInstance().connectCallback);
							}
						});
				request.executeAsync();
				
				
				//Connect.setFacebookToken(access_token, getInstance().connectCallback);
				
			} else if (state == SessionState.CLOSED_LOGIN_FAILED) {
				
				if (getInstance().connectCallback != null) {
					getInstance().connectCallback.onFail(ConnectCallback.FuncResult.E_FAIL,
							new Exception("failed to login to facebook"));
				}
			} 
		}
	};

	public void connecnt(Activity activity, List<String> permissions,
			ConnectCallback<com.rankwave.connect.sdk.Session> callback) {
        
		getInstance().connectCallback = callback;

		if (permissions == null) {
			com.facebook.Session.openActiveSession(activity, true,
					fb_session_callback);
		} else {

			com.facebook.Session session = com.facebook.Session
					.getActiveSession();
			if (session == null) {
				session = new com.facebook.Session(activity);
				com.facebook.Session.setActiveSession(session);

				session.openForRead(new com.facebook.Session.OpenRequest(
						activity).setCallback(fb_session_callback)
						.setPermissions(permissions));
			} else {

				if (session.isOpened() == false && session.isClosed() == false) {
					session.openForRead(new com.facebook.Session.OpenRequest(
							activity).setCallback(fb_session_callback)
							.setPermissions(permissions));
				} else {
					com.facebook.Session.openActiveSession(activity, true,
							fb_session_callback);
				}
			}
		}
	}

	public void getToken(Activity currentActivity, int requestCode,
			int resultCode, Intent data) {

		if (com.facebook.Session.getActiveSession() != null)
			com.facebook.Session.getActiveSession().onActivityResult(
					currentActivity, requestCode, resultCode, data);
	}
	
	public void close() {
		
		Session session = Session.getActiveSession();
		
        if (session != null && !session.isClosed()) 
        {
            session.closeAndClearTokenInformation();
        }
	}

}