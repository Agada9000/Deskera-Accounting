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
package com.krawler.esp.handlers;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.RandomStringUtils;
import org.hibernate.Session;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.KWLCurrency;
import com.krawler.common.admin.KWLDateFormat;
import com.krawler.common.admin.KWLTimeZone;
import com.krawler.common.admin.Role;
import com.krawler.common.admin.User;
import com.krawler.common.admin.UserLogin;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.StringUtil;
import com.krawler.esp.hibernate.impl.HibernateUtil;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import org.hibernate.HibernateException;

public class AuthHandler {
    public static void main(String[] str){
    }

    public static JSONObject verifyLogin(Session  session, String username, String passwd, String subdomain) throws ServiceException, HibernateException {
		JSONObject jobj = new JSONObject();
		try {
            String SELECT_USER_INFO="select u, u.userLogin, u.company from User as u where u.userLogin.userName = ? and u.userLogin.password = ? and u.company.deleted=false  and u.deleted=false and u.company.subDomain=?";
            List list = HibernateUtil.executeQuery(session, SELECT_USER_INFO, new Object[] {username, passwd ,subdomain});
            Iterator ite = list.iterator();
            if( ite.hasNext() ) {
                Object[] row = (Object[]) ite.next();
                User user = (User) row[0];
                UserLogin userLogin = (UserLogin) row[1];
                Company company = (Company) row[2];
                jobj.put("success", true);
                jobj.put("lid", userLogin.getUserID());
                jobj.put("username", userLogin.getUserName());
                jobj.put("companyid", company.getCompanyID());
                jobj.put("company", company.getCompanyName());
                KWLTimeZone timeZone= user.getTimeZone();
                if(timeZone==null)timeZone=company.getTimeZone();
                if(timeZone==null)timeZone=(KWLTimeZone)session.get(KWLTimeZone.class, StorageHandler.getDefaultTimeZoneID());
                jobj.put("timezoneid", timeZone.getTimeZoneID());
                jobj.put("tzdiff", timeZone.getDifference());
                KWLDateFormat dateFormat= user.getDateFormat();
                if(dateFormat==null)dateFormat=(KWLDateFormat)session.get(KWLDateFormat.class, StorageHandler.getDefaultDateFormatID());
                jobj.put("dateformatid", dateFormat.getFormatID());
                KWLCurrency currency= company.getCurrency();
                if(currency==null)currency=(KWLCurrency)session.get(KWLCurrency.class, StorageHandler.getDefaultCurrencyID());
                jobj.put("currencyid", currency.getCurrencyID());
//@@@               jobj.put("superuser", user.getRole().getID());
            } else {
                jobj.put("failure", true);
            }
        } catch (JSONException e) {
                throw ServiceException.FAILURE("Auth.verifyLogin", e);
        } 
        return jobj;
    }
    
    public static JSONObject verifyLogin(Session  session, String username, String subdomain) throws ServiceException, HibernateException {
		JSONObject jobj = new JSONObject();
		try {
            String SELECT_USER_INFO="select u, u.userLogin, u.company from User as u where u.userLogin.userName = ? and u.company.deleted=false  and u.deleted=false and u.company.subDomain=?";
            List list = HibernateUtil.executeQuery(session, SELECT_USER_INFO, new Object[] {username, subdomain});
            Iterator ite = list.iterator();
            if( ite.hasNext() ) {
                Object[] row = (Object[]) ite.next();
                User user = (User) row[0];
                UserLogin userLogin = (UserLogin) row[1];
                Company company = (Company) row[2];
                jobj.put("success", true);
                jobj.put("lid", userLogin.getUserID());
                jobj.put("username", userLogin.getUserName());
                jobj.put("companyid", company.getCompanyID());
                jobj.put("company", company.getCompanyName());
                KWLTimeZone timeZone= user.getTimeZone();
                if(timeZone==null)timeZone=company.getTimeZone();
                if(timeZone==null)timeZone=(KWLTimeZone)session.get(KWLTimeZone.class, StorageHandler.getDefaultTimeZoneID());
                jobj.put("timezoneid", timeZone.getTimeZoneID());
                jobj.put("tzdiff", timeZone.getDifference());
                KWLDateFormat dateFormat= user.getDateFormat();
                if(dateFormat==null)dateFormat=(KWLDateFormat)session.get(KWLDateFormat.class, StorageHandler.getDefaultDateFormatID());
                jobj.put("dateformatid", dateFormat.getFormatID());
                KWLCurrency currency= company.getCurrency();
                if(currency==null)currency=(KWLCurrency)session.get(KWLCurrency.class, StorageHandler.getDefaultCurrencyID());
                jobj.put("currencyid", currency.getCurrencyID());
//@@@                jobj.put("superuser", user.getRole().getID());
            } else {
                jobj.put("failure", true);
            }
        } catch (JSONException e) {
                throw ServiceException.FAILURE("Auth.verifyLogin", e);
        } 
        return jobj;
    }

    public static String getUserid(HttpServletRequest request)
			throws SessionExpiredException {
		String userId = NullCheckAndThrow(request.getSession().getAttribute(
				"userid"), SessionExpiredException.USERID_NULL);
		return userId;
	}

	public static String getUserName(HttpServletRequest request)
			throws SessionExpiredException {
		String userName = NullCheckAndThrow(request.getSession().getAttribute(
				"username"), SessionExpiredException.USERNAME_NULL);
		return userName;
	}

	public static String getFullName(Session session, String userid) throws HibernateException {
        return getFullName((User)session.get(User.class, userid));
	}

	public static String getFullName(User user) {
        String fullname = user.getFirstName();
        if(fullname!=null && user.getLastName()!=null) fullname+=" "+user.getLastName();
        if (StringUtil.isNullOrEmpty(user.getFirstName()) && StringUtil.isNullOrEmpty(user.getLastName())) {
            fullname = user.getUserLogin().getUserName();
        }
        return fullname;
	}

    public static String getCompanyid(HttpServletRequest request)
			throws SessionExpiredException {
		String userId = NullCheckAndThrow(request.getSession().getAttribute(
				"companyid"), SessionExpiredException.USERID_NULL);
		return userId;
	}

    public static String getCompanyid(Session session, String domain) {
        String companyID=null;
		try {
            String query="select companyID from Company where subDomain = ?";
            List list = HibernateUtil.executeQuery(session, query, domain);
            Iterator ite = list.iterator();
            if( ite.hasNext() ) {
                companyID = (String)ite.next();
            }
        } catch (Exception e) {}
        return companyID;
	}

	public static String getCompanyName(HttpServletRequest request)
			throws SessionExpiredException {
		String userName = NullCheckAndThrow(request.getSession().getAttribute(
				"company"), SessionExpiredException.USERNAME_NULL);
		return userName;
	}
    private static String NullCheckAndThrow(Object objToCheck, String errorCode)
			throws SessionExpiredException {
		if (objToCheck != null) {
			String oStr = objToCheck.toString();
			if (!StringUtil.isNullOrEmpty(oStr))
				return oStr;
		}
		throw new SessionExpiredException("Session Invalidated", errorCode);
	}

    public static String getTimeZoneID(HttpServletRequest request)
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

    public static String getDateFormatID(HttpServletRequest request)
			throws SessionExpiredException {
		String userId = NullCheckAndThrow(request.getSession().getAttribute(
				"dateformatid"), SessionExpiredException.USERID_NULL);
		return userId;
	}

    public static DateFormat getUserDateFormatter(Session session, String formatid, String diff, boolean onlydate)
			throws SessionExpiredException, HibernateException {
        KWLDateFormat df=(KWLDateFormat)session.get(KWLDateFormat.class, formatid);
        String format=df.getJavaForm();
        int pos=format.length();
        if(onlydate)
            pos=df.getJavaSeperatorPosition();

        SimpleDateFormat sdf=new SimpleDateFormat(format.substring(0, pos));
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"+diff));
        return sdf;
    }

    public static DateFormat getDateFormatter(HttpServletRequest request)
			throws SessionExpiredException {
        SimpleDateFormat sdf=new SimpleDateFormat("MMMM d, yyyy hh:mm:ss aa");

        sdf.setTimeZone(TimeZone.getTimeZone("GMT"+getTimeZoneDifference(request)));
        return sdf;
    }

    public static DateFormat getPrefDateFormatter(HttpServletRequest request,String pref)
        throws SessionExpiredException {
        String dateformat="";
        String timeformat=AuthHandler.getUserTimeFormat(request);
        if(timeformat.equals("1")) {
            dateformat=pref.replace('H', 'h');
            if(!dateformat.equals(pref))
                dateformat+=" a";
        } else
            dateformat=pref;
        SimpleDateFormat sdf=new SimpleDateFormat(dateformat);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"+getTimeZoneDifference(request)));
        return sdf;
    }

    public static String getUserTimeFormat(HttpServletRequest request)
			throws SessionExpiredException {
		String timeformat = NullCheckAndThrow(request.getSession().getAttribute(
				"timeformat"), SessionExpiredException.USERID_NULL);
		return timeformat;
	}
    
   public static String getCurrencyID(HttpServletRequest request)
			throws SessionExpiredException {
		String userId = NullCheckAndThrow(request.getSession().getAttribute(
				"currencyid"), SessionExpiredException.USERID_NULL);
		return userId;
	}

    public static String generateNewPassword() {
            return RandomStringUtils.random(8, true, true);
    }

	public static String getSHA1(String inStr) throws ServiceException {
		String outStr = inStr;
		try {
			byte[] theTextToDigestAsBytes = inStr.getBytes("utf-8");

			MessageDigest sha = MessageDigest.getInstance("SHA-1");
			byte[] digest = sha.digest(theTextToDigestAsBytes);

			StringBuffer sb = new StringBuffer();
			for (byte b : digest) {
				String h = Integer.toHexString(b & 0xff);
				if (h.length() == 1) {
					sb.append("0" + h);
				} else {
					sb.append(h);
				}
			}
			outStr = sb.toString();
		} catch (UnsupportedEncodingException e) {
			throw ServiceException.FAILURE("Auth.getSHA1", e);
		} catch (NoSuchAlgorithmException e) {
			throw ServiceException.FAILURE("Auth.getSHA1", e);
		}
		return outStr;
	}



	public static JSONArray getPreferences(Session session, HttpServletRequest request)
			throws ServiceException, HibernateException {
		JSONArray preferences = new JSONArray();
		try {
				JSONObject j = new JSONObject();
                KWLTimeZone timeZone = (KWLTimeZone)session.get(KWLTimeZone.class, getTimeZoneID(request));
                KWLDateFormat dateFormat = (KWLDateFormat)session.get(KWLDateFormat.class, getDateFormatID(request));
                KWLCurrency currency = (KWLCurrency)session.get(KWLCurrency.class, getCurrencyID(request));
                j.put("Timezone", timeZone.getName());
				j.put("Timezoneid", timeZone.getTimeZoneID());
				j.put("Timezonediff", timeZone.getDifference());
				j.put("DateFormat", dateFormat.getScriptForm());
                j.put("DateFormatid",dateFormat.getFormatID());
                j.put("seperatorpos",dateFormat.getScriptSeperatorPosition());
				j.put("Currency", currency.getHtmlcode());
				j.put("CurrencyName", currency.getName());
				j.put("CurrencySymbol", currency.getSymbol());
                j.put("Currencyid",currency.getCurrencyID());
				preferences.put(j);
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE("Auth.getPreferences", ex);
        } catch (JSONException e) {
            throw ServiceException.FAILURE("Auth.getPreferences", e);
        }
		return preferences;
	}

    /*public static boolean isCompanyAdmin(User user){
        if(user!=null) {
            return user.getRoleID().equals(Role.COMPANY_ADMIN);
        }
//@@@            return user.getRole().getID().equals(Role.COMPANY_ADMIN);
        return false;
    }*/

    public static String getTZID(HttpServletRequest request)
			throws SessionExpiredException {
		String userId = NullCheckAndThrow(request.getSession().getAttribute(
				"tzid"), SessionExpiredException.USERID_NULL);
		return userId;
	}
}
