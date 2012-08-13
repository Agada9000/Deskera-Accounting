/*
 * Copyright (C) 2012  Krawler Information Systems Pvt Ltd
 * All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package com.krawler.spring.sessionHandler;

import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.StringUtil;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author karthik
 */
public class sessionHandlerImpl {

    private sessionHandlerImpl sessionHandlerImplObj;

    public sessionHandlerImpl(){
	}

    public void setsessionHandlerImpl(sessionHandlerImpl sessionHandlerImplObj1) {
        this.sessionHandlerImplObj = sessionHandlerImplObj1;
    }

    public static boolean isValidSession(HttpServletRequest request,
            HttpServletResponse response) {
        boolean bSuccess = false;
        try {
            if (request.getSession().getAttribute("initialized") != null) {
                bSuccess = true;
            }
        } catch (Exception ex) {
        }
        return bSuccess;
    }

    public void updatePreferences(HttpServletRequest request,
            String currencyid, String dateformatid, String timezoneid,
            String tzdiff) {
        if (currencyid != null) {
            request.getSession().setAttribute("currencyid", currencyid);
        }
        if (timezoneid != null) {
            request.getSession().setAttribute("timezoneid", timezoneid);
            request.getSession().setAttribute("tzdiff", tzdiff);
        }
        if (dateformatid != null) {
            request.getSession().setAttribute("dateformatid", dateformatid);
        }
    }

    /* Update date preference only. */
    public void updateDatePreferences(HttpServletRequest request, String dateformatid) {
        if (dateformatid != null) {
            request.getSession().setAttribute("dateformatid", dateformatid);
        }
    }

    /* Time Format included here. */
    public void updatePreferences(HttpServletRequest request,
            String currencyid, String dateformatid, String timezoneid,
            String tzdiff, String timeformat) {
        if (currencyid != null) {
            request.getSession().setAttribute("currencyid", currencyid);
        }
        if (timezoneid != null) {
            request.getSession().setAttribute("timezoneid", timezoneid);
            request.getSession().setAttribute("tzdiff", tzdiff);
        }
        if (dateformatid != null) {
            request.getSession().setAttribute("dateformatid", dateformatid);
        }
        if (timeformat != null) {
            request.getSession().setAttribute("timeformat", timeformat);
        }
    }

    public boolean validateSession(HttpServletRequest request,
            HttpServletResponse response) {
        return sessionHandlerImpl.isValidSession(request, response);
    }

    public void createUserSession(HttpServletRequest request, JSONObject jObj) throws ServiceException {
        HttpSession session = request.getSession(true);
        try {
            session.setAttribute("username", jObj.getString("username"));
        	session.setAttribute("userid", jObj.getString("lid"));
        	session.setAttribute("companyid", jObj.getString("companyid"));
        	session.setAttribute("company", jObj.getString("company"));
        	session.setAttribute("timezoneid", jObj.getString("timezoneid"));
        	session.setAttribute("tzdiff", jObj.getString("tzdiff"));
        	session.setAttribute("dateformatid", jObj.getString("dateformatid"));
        	session.setAttribute("currencyid", jObj.getString("currencyid"));
        	session.setAttribute("callwith", jObj.getString("callwith"));
            session.setAttribute("timeformat", jObj.getString("timeformat"));
            session.setAttribute("companyPreferences", jObj.getString("companyPreferences"));
            session.setAttribute("roleid", jObj.getString("roleid"));
        	session.setAttribute("initialized", "true");
        	session.setAttribute("userfullname", jObj.getString("userfullname"));
			JSONArray jarr = jObj.getJSONArray("perms");
        	for (int l = 0; l < jarr.length(); l++) {
				String keyName = jarr.getJSONObject(l).names().get(0)
						.toString();
				session.setAttribute(keyName, jarr.getJSONObject(l)
						.get(keyName));
			}
        } catch (JSONException e) {
            throw ServiceException.FAILURE("sessionHandlerImpl.createUserSession", e);
        }
    }

    public void destroyUserSession(HttpServletRequest request,
            HttpServletResponse response) {
        request.getSession().invalidate();
    }

     public static String getUserid(HttpServletRequest request)
            throws SessionExpiredException {
        String userId = NullCheckAndThrow(request.getSession().getAttribute(
                "userid"), SessionExpiredException.USERID_NULL);
        return userId;
    }

    public String getTimeZoneID(HttpServletRequest request)
            throws SessionExpiredException {
        String userId = NullCheckAndThrow(request.getSession().getAttribute(
                "timezoneid"), SessionExpiredException.USERID_NULL);
        return userId;
    }

    public static String getTimeZoneDifference(HttpServletRequest request)
            throws SessionExpiredException {
        String userId = NullCheckAndThrow(request.getSession().getAttribute(
                "tzdiff"), SessionExpiredException.USERID_NULL);
        return userId;
    }

    public String getUserCallWith(HttpServletRequest request)
            throws SessionExpiredException {
        String callwith = NullCheckAndThrow(request.getSession().getAttribute(
                "callwith"), SessionExpiredException.USERID_NULL);
        return callwith;
    }

    public static String getUserTimeFormat(HttpServletRequest request)
            throws SessionExpiredException {
        String timeformat = NullCheckAndThrow(request.getSession().getAttribute(
                "timeformat"), SessionExpiredException.USERID_NULL);
        return timeformat;
    }

    public static String getUserName(HttpServletRequest request)
            throws SessionExpiredException {
        String userName = NullCheckAndThrow(request.getSession().getAttribute(
                "username"), SessionExpiredException.USERNAME_NULL);
        return userName;
    }

    public static String getUserFullName(HttpServletRequest request)
            throws SessionExpiredException {
        String userfullname = NullCheckAndThrow(request.getSession().getAttribute(
                "userfullname"), SessionExpiredException.USERFULLNAME_NULL);
        return userfullname;
    }
    
    public String getRole(HttpServletRequest request)
            throws SessionExpiredException {
        String roleid = NullCheckAndThrow(request.getSession().getAttribute(
                "roleid"), SessionExpiredException.USERID_NULL);
        return roleid;
    }

    public static String getDateFormatID(HttpServletRequest request)
            throws SessionExpiredException {
        String userId = NullCheckAndThrow(request.getSession().getAttribute(
                "dateformatid"), SessionExpiredException.USERID_NULL);
        return userId;
    }

    public static String getCompanyid(HttpServletRequest request)
            throws SessionExpiredException {
        String userId = NullCheckAndThrow(request.getSession().getAttribute(
                "companyid"), SessionExpiredException.USERID_NULL);
        return userId;
    }

    public static String getCompanyName(HttpServletRequest request)
            throws SessionExpiredException {
        String userName = NullCheckAndThrow(request.getSession().getAttribute(
                "company"), SessionExpiredException.USERNAME_NULL);
        return userName;
    }

    public static String getCurrencyID(HttpServletRequest request)
			throws SessionExpiredException {
		String userId = NullCheckAndThrow(request.getSession().getAttribute(
				"currencyid"), SessionExpiredException.USERID_NULL);
		return userId;
	}
    
    public Integer getPerms(HttpServletRequest request, String keyName)
            throws SessionExpiredException {
        long perl = 0;
        int per = 0;
        try {
            if (request.getSession().getAttribute(keyName) != null) {
                perl = (Long) request.getSession().getAttribute(keyName);
            }
            per = (int) perl;
        } catch (Exception e) {
            per = 0;
        }
        return per;
    }

    public static String NullCheckAndThrow(Object objToCheck, String errorCode)
            throws SessionExpiredException {
        if (objToCheck != null) {
            String oStr = objToCheck.toString();
            if (!StringUtil.isNullOrEmpty(oStr)) {
                return oStr;
            }
        }
        throw new SessionExpiredException("Session Invalidated", errorCode);
    }
}
