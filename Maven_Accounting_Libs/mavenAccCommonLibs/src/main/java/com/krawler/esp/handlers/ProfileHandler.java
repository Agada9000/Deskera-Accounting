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

import com.krawler.accounting.utils.AuditAction;
import com.krawler.common.admin.AuditGroup;
import com.krawler.common.admin.AuditTrail;
import com.krawler.common.admin.Company;
import com.krawler.common.admin.CompanyHoliday;
import com.krawler.common.admin.Country;
import com.krawler.common.admin.KWLCurrency;
import com.krawler.common.admin.KWLDateFormat;
import com.krawler.common.admin.KWLTimeZone;
import com.krawler.common.admin.ProjectFeature;
import com.krawler.common.admin.Role;
import com.krawler.common.admin.User;
import com.krawler.common.admin.UserLogin;
import com.krawler.common.admin.UserPermission;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.StringUtil;
import com.krawler.common.util.URLUtil;
import com.krawler.esp.hibernate.impl.HibernateUtil;
import com.krawler.esp.servlets.ProfileImageServlet;
import com.krawler.esp.web.resource.Links;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.spring.common.KwlReturnObject;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.fileupload.FileItem;
import com.krawler.utils.json.base.*;
import java.text.DecimalFormat;
import org.hibernate.HibernateException;
import org.hibernate.Session;


public class ProfileHandler {
    public static String getUserFullName(Session session, String userid) throws ServiceException{
        String name=null;
        String SELECT_USER_INFO="select u.firstName, u.lastName from User as u " +
            "where u.userID = ?";
        List list = HibernateUtil.executeQuery(session, SELECT_USER_INFO, userid);
        Iterator ite = list.iterator();
        if( ite.hasNext() ) {
            Object[] row = (Object[]) ite.next();
            name=(StringUtil.isNullOrEmpty((String)row[0])?"":row[0])+" "+(StringUtil.isNullOrEmpty((String)row[1])?"":row[1]);
        }

        return name;
    }
    public static JSONObject getVendorDetails(Session session, HttpServletRequest request) throws ServiceException, JSONException, SessionExpiredException{
        JSONObject obj = new JSONObject();
        long timediff=0;
        boolean isexpired=false;
        String vendorid=request.getParameter("personid");
        String SELECT_USER_INFO="select v.name, v.email,v.mailOn,v.taxIDNumber from Vendor as v " +
            "where v.ID = ?";
        List list = HibernateUtil.executeQuery(session, SELECT_USER_INFO, vendorid);
        Iterator ite = list.iterator();
        if( ite.hasNext() ) {
            Object[] row = (Object[]) ite.next();
                obj.put("personname", StringUtil.isNullOrEmpty((String)row[0])?"":row[0]);
                obj.put("email", StringUtil.isNullOrEmpty((String)row[1])?"":row[1]);
                obj.put("taxidnumber", StringUtil.isNullOrEmpty((String)row[3])?"":row[3]);
                Date mailedOn= (Date)row[2];
                Date currentDate = new Date();
                 if(mailedOn!=null){
                    timediff=currentDate.getTime()-mailedOn.getTime();
                    timediff=timediff/(1000*60*60);
                    if(timediff>48)//||timediff<0[Add after time jone issue]
                        isexpired=true;
                }
                obj.put("isexpired",isexpired);
        }
        return obj;
    }
    public static String saveVendorTaxID(Session session, HttpServletRequest request) throws ServiceException {
        String msg="";
        try {
            String personid = request.getParameter("personid");
            String taxid = request.getParameter("taxid");
            ArrayList params=new ArrayList();
            params.add(taxid);
            params.add(personid);
            String SELECT_USER_INFO="update Vendor v set v.taxIDNumber=? " +
                "where v.ID = ?";
            int count = HibernateUtil.executeUpdate(session, SELECT_USER_INFO, params.toArray());
            if(count>0)
                msg="Information has been updated successfully";
            else
                msg="Error";
        } catch (Exception e) {
            msg="Error";
            throw ServiceException.FAILURE("ProfileHandler.saveVendorTaxID", e);
        }
        return msg;
    }
    public static JSONObject getAllUserDetails(Session session, HttpServletRequest request) throws ServiceException{
        JSONObject jobj=new JSONObject();
		try {
            String lid=request.getParameter("lid");
            String start=request.getParameter("start");
            String limit=request.getParameter("limit");
            String ss=request.getParameter("ss");
            String filterStr="";
            ArrayList params=new ArrayList();
            String SELECT_USER_INFO=" from User  where company.companyID = ?  and deleted=?";
            params.add(AuthHandler.getCompanyid(request));
            params.add(false);
            if(StringUtil.isNullOrEmpty(lid)==false){
                filterStr=" and userID = ? ";
                params.add(lid);
            }
            if(StringUtil.isNullOrEmpty(ss)==false){
                filterStr=" and userLogin.userName like ? ";
                params.add(ss+"%");
            }

            SELECT_USER_INFO+=filterStr;

            List list=list = HibernateUtil.executeQuery(session, SELECT_USER_INFO,params.toArray());
            int count=list.size();
            if(StringUtil.isNullOrEmpty(start)==false&&StringUtil.isNullOrEmpty(limit)==false){
                list=HibernateUtil.executeQueryPaging(session, SELECT_USER_INFO,params.toArray(), new Integer[]{Integer.parseInt(start),Integer.parseInt(limit)});
            }
                
            Iterator itr = list.iterator();
            JSONArray jArr=new JSONArray();
            while(itr.hasNext()) {
                User user = (User) itr.next();
                UserLogin ul=user.getUserLogin();
                JSONObject obj = new JSONObject();
                obj.put("userid", user.getUserID());
                obj.put("username", ul.getUserName());
                obj.put("fname", user.getFirstName());
                obj.put("lname", user.getLastName());
                obj.put("image", user.getImage());
                obj.put("emailid", user.getEmailID());
                obj.put("lastlogin",(ul.getLastActivityDate()==null?"":AuthHandler.getDateFormatter(request).format(ul.getLastActivityDate())));
                obj.put("aboutuser", user.getAboutUser());
                obj.put("address", user.getAddress());
                obj.put("contactno", user.getContactNumber());
                obj.put("formatid", (user.getDateFormat()==null?"":user.getDateFormat().getFormatID()));
                obj.put("tzid", (user.getTimeZone()==null?"":user.getTimeZone().getTimeZoneID()));
//@@@                obj.put("roleid", user.getRole().getID());
//                obj.put("rolename", user.getRole().getName());
                jArr.put(obj);
            }
            jobj.put("data", jArr);
            jobj.put("count", count);
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE("ProfileHandler.getAllUserDetails", ex);
        } catch (JSONException e) {
            throw ServiceException.FAILURE("ProfileHandler.getAllUserDetails", e);
        }

        return jobj;
    }
    
	public static JSONObject getValidUserOptions(Session session, String userid) throws ServiceException {
		return new JSONObject();
	}


    public static void saveUser(Session session, HttpServletRequest request, HashMap hm)  throws ServiceException, AccountingException{
		try {
            String id=(String)hm.get("userid");
            UserLogin userLogin;
            String auditMessage="";
            String auditID="";
            User user;
            User creater= (User)session.get(User.class, AuthHandler.getUserid(request));
            String fullnameCreator = AuthHandler.getFullName(creater);
            String pwd=null;
            if(StringUtil.isNullOrEmpty(id)==false){
                user = (User)session.get(User.class, id);
                userLogin=user.getUserLogin();
                if(user.getUserID().equals(AuthHandler.getUserid(request))){
                    auditMessage="User "+fullnameCreator+" has modified his profile";
                    auditID=AuditAction.PROFILE_CHANGED;
                }else{
                    auditMessage="Profile of user "+AuthHandler.getFullName(user)+" has been modified by "+fullnameCreator;
                    auditID=AuditAction.USER_MODIFIED;
                }
            }else{
                String uuid=UUID.randomUUID().toString();
                user = new User();
                user.setUserID(uuid);
                userLogin=new UserLogin();
                user.setUserLogin(userLogin);
                userLogin.setUser(user);
                String q="from User where userLogin.userName=?";// and company.companyID=?";
                if(HibernateUtil.executeQuery(session, q, new Object[]{hm.get("username")/*,AuthHandler.getCompanyid(request)*/}).isEmpty()==false&&hm.get("username").equals(userLogin.getUserName())==false)
                    throw new AccountingException("User name not available",null);
                userLogin.setUserName((String)hm.get("username"));
                pwd=AuthHandler.generateNewPassword();
                userLogin.setPassword(AuthHandler.getSHA1(pwd));
                user.setCompany((Company)session.get(Company.class,AuthHandler.getCompanyid(request)));
            }

            user.setFirstName((String)hm.get("fname"));
            user.setLastName((String)hm.get("lname"));
            user.setEmailID((String)hm.get("emailid"));
            user.setAddress((String)hm.get("address"));
            user.setContactNumber((String)hm.get("contactno"));
//@@@            if(StringUtil.isNullOrEmpty((String)hm.get("roleid"))==false)
//                user.setRole((Role)session.get(Role.class, (String)hm.get("roleid")));
            if(StringUtil.isNullOrEmpty((String)hm.get("formatid"))==false)
                user.setDateFormat((KWLDateFormat)session.get(KWLDateFormat.class, (String)hm.get("formatid")));
            String diff=null;
            if(StringUtil.isNullOrEmpty((String)hm.get("tzid"))==false){
                KWLTimeZone timeZone=(KWLTimeZone)session.get(KWLTimeZone.class, (String)hm.get("tzid"));
                diff=timeZone.getDifference();
                user.setTimeZone(timeZone);
            }
            if(StringUtil.isNullOrEmpty((String)hm.get("aboutuser"))==false)user.setAboutUser((String)hm.get("aboutuser"));
            String imageName=((FileItem)(hm.get("userimage"))).getName();
            if(StringUtil.isNullOrEmpty(imageName)==false){
                session.saveOrUpdate(user);
                String fileName=user.getUserID()+FileUploadHandler.getImageExt();
                user.setImage(ProfileImageServlet.ImgBasePath+fileName);
                new FileUploadHandler().uploadImage((FileItem)hm.get("userimage"),
                        fileName,
                        StorageHandler.GetProfileImgStorePath(),100,100,false,false);
            }

            session.saveOrUpdate(user);
            session.saveOrUpdate(userLogin);
            SessionHandler.updatePreferences(request, null, (StringUtil.isNullOrEmpty((String)hm.get("formatid"))?null:(String)hm.get("formatid")), (StringUtil.isNullOrEmpty((String)hm.get("tzid"))?null:(String)hm.get("tzid")),diff);
            if(StringUtil.isNullOrEmpty(id)){
                String uri = URLUtil.getPageURL(request, Links.loginpageFull);
                String pmsg = String.format(com.krawler.accounting.utils.KWLErrorMsgs.msgMailInvite,user.getFirstName(),fullnameCreator, userLogin.getUserName(), pwd, uri,fullnameCreator);
                String htmlmsg = String.format(com.krawler.accounting.utils.KWLErrorMsgs.msgMailInviteUsernamePassword, user.getFirstName(),fullnameCreator, AuthHandler.getCompanyName(request), userLogin.getUserName(),
                            pwd, uri,uri,fullnameCreator);
                try {
                    SendMailHandler.postMail(new String[] { user.getEmailID() },"[Deskera] Welcome to Deskera Accounting", htmlmsg, pmsg, creater.getEmailID());
                } catch (MessagingException e) {
                    e.printStackTrace();
                }

                auditMessage="A new account for user "+AuthHandler.getFullName(user)+" has been created by "+fullnameCreator;
                auditID=AuditAction.USER_CREATED;
            }
            insertAuditLog(session, auditID, auditMessage, request);
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }

    public static void deleteUser(Session session, HttpServletRequest request)  throws ServiceException, AccountingException{
        try {
            String[] ids=request.getParameterValues("userids");
            String users="";
            for(int i=0;i<ids.length;i++){
                User user=(User)session.get(User.class, ids[i]);
//                if(AuthHandler.isCompanyAdmin(user))throw new AccountingException("Cannot delete Administrator");
                users+=AuthHandler.getFullName(user)+", ";
//@@@                user.setDeleted(true);
                user.setDeleteflag(1);
            }
//            insertAuditLog(session, AuditAction.USER_DELETED, "Account for user(s) "+users+"has been deleted by "+AuthHandler.getFullName(session,AuthHandler.getUserid(request)), request);
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }

//    public static void changePassword(Session session, HttpServletRequest request, String password) throws ServiceException {
//        try {
//            String newpass=AuthHandler.getSHA1(password);
//            User user=(User)session.get(User.class, request.getParameter("userid"));
//            UserLogin userLogin=user.getUserLogin();
//            userLogin.setPassword(newpass);
//            session.saveOrUpdate(userLogin);
//            String uri = URLUtil.getPageURL(request, Links.loginpageFull);
//            String fname = user.getFirstName();
//			if (StringUtil.isNullOrEmpty(fname))
//				fname = user.getUserLogin().getUserName();
//            String pmsg = String
//                    .format(
//                            KWLErrorMsgs.msgChangePassword,
//                            fname, AuthHandler.getUserName(request), password, uri,uri);
//            String htmlmsg = String
//                    .format(
//                            KWLErrorMsgs.msgMailChangePassword,
//                            fname, AuthHandler.getUserName(request), password, uri,uri);
//            try {
//                SendMailHandler.postMail(new String[] { user.getEmailID() },
//                        KWLErrorMsgs.subjectChangePassword, htmlmsg,
//                        pmsg, KWLErrorMsgs.adminEmailId);
//            } catch (MessagingException e) {
//                e.printStackTrace();
//            }
//            insertAuditLog(session, AuditAction.PASSWORD_CHANGED, "Password for user "+AuthHandler.getFullName(user)+" has been changed by "+AuthHandler.getFullName(session, AuthHandler.getUserid(request)), request);
//        } catch (SessionExpiredException ex) {
//            throw ServiceException.FAILURE("ProfileHandler.changePassword", ex);
//        }
//    }
      public static JSONObject changePassword(String platformURL,Session session, HttpServletRequest request) throws ServiceException {
        JSONObject jobj = new JSONObject();
        String msg="";
        try {
            String password = request.getParameter("currentpassword");
            String pwd = request.getParameter("changepassword").toString();
            String uid = AuthHandler.getUserid(request);
            String companyid = AuthHandler.getCompanyid(request);

            if (password == null || password.length() <= 0) {
                msg="Invalid Password";
            } else {                
                if (!StringUtil.isNullOrEmpty(platformURL)) {
                    JSONObject userData = new JSONObject();
                    userData.put("pwd", pwd);
                    userData.put("oldpwd", password);
                    userData.put("userid", uid);
                    String action = "3";
                    JSONObject resObj = APICallHandler.callApp(session, platformURL, userData, companyid, action);
                    if (!resObj.isNull("success") && resObj.getBoolean("success")) {
                        User user = (User) session.load(User.class, uid);
                        UserLogin userLogin = user.getUserLogin();
                            userLogin.setPassword(pwd);
                            session.saveOrUpdate(userLogin);
                            msg="New Password has been successfully set";


                    } else {
                        msg="Error in changing Password";
                    }
                } else {
                    User user = (User) session.load(User.class, uid);
                    UserLogin userLogin = user.getUserLogin();
                    String currentpass = userLogin.getPassword();
                    if (StringUtil.equal(password, currentpass)) {
                        userLogin.setPassword(pwd);
                        session.saveOrUpdate(userLogin);
                        msg = "New Password has been successfully set";
                    } else {
                        msg = "Old password is incorrect. Please try again.";
                    }
                }
            }
            jobj.put("msg", msg);
        } catch (Exception e) {
            throw ServiceException.FAILURE("ProfileHandler.setPassword", e);
        }
        return jobj;
    }


    public static void setPermissions(Session session, HttpServletRequest request) throws ServiceException {
        try {
            String id=request.getParameter("roleid");
            String[] features=request.getParameterValues("features");
            String[] permissions=request.getParameterValues("permissions");
            String sql="delete from UserPermission where role.ID=?";
            HibernateUtil.executeUpdate(session, sql, id);
            Role role = (Role)session.get(Role.class, id);
            for(int i=0;i<features.length;i++){
                if(permissions[i].equals("0"))continue;
                UserPermission permission = new UserPermission();
//@@@                permission.setRole(role);
                permission.setFeature((ProjectFeature)session.get(ProjectFeature.class, features[i]));
                permission.setPermissionCode(Long.parseLong(permissions[i]));
                session.save(permission);
            }
//            insertAuditLog(session, AuditAction.PERMISSIONS_MODIFIED, AuthHandler.getFullName(session, AuthHandler.getUserid(request))+" has changed the permissions of role "+role.getName(), request);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("ProfileHandler.setPermissions", ex);
        }
    }

    public static void updateLastLogin(Session session, HttpServletRequest request) throws ServiceException {
        try {
            UserLogin userLogin=(UserLogin)session.get(UserLogin.class, AuthHandler.getUserid(request));
            userLogin.setLastActivityDate(new Date());
            session.update(userLogin);
//            insertAuditLog(session, AuditAction.LOG_IN_SUCCESS, "User "+AuthHandler.getFullName(session, AuthHandler.getUserid(request)) + " has logged in", request);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("ProfileHandler.updateLastLogin", ex);
        }
    }

    public static JSONObject getAllTimeZones(Session session, HttpServletRequest request) throws ServiceException{
        JSONObject jobj=new JSONObject();
		try {
            String query="from KWLTimeZone";
            List list = HibernateUtil.executeQuery(session, query);
            Iterator itr = list.iterator();
            JSONArray jArr=new JSONArray();
            while(itr.hasNext()) {
                KWLTimeZone timeZone = (KWLTimeZone) itr.next();
                JSONObject obj = new JSONObject();
                obj.put("id", timeZone.getTimeZoneID());
                obj.put("name", timeZone.getName());
                obj.put("difference", timeZone.getDifference());
                jArr.put(obj);
            }
            jobj.put("data", jArr);
        } catch (JSONException e) {
                throw ServiceException.FAILURE("ProfileHandler.getAllTimeZones", e);
        }

        return jobj;
    }

    public static JSONObject getAllCurrencies(Session session, HttpServletRequest request) throws ServiceException{
        JSONObject jobj=new JSONObject();
		try {
            String query="from KWLCurrency";
            List list = HibernateUtil.executeQuery(session, query);
            Iterator itr = list.iterator();
            JSONArray jArr=new JSONArray();
            while(itr.hasNext()) {
                KWLCurrency currency = (KWLCurrency) itr.next();
                JSONObject obj = new JSONObject();
                obj.put("currencyid", currency.getCurrencyID());
                obj.put("symbol", currency.getSymbol());
                obj.put("currencyname", currency.getName());
                obj.put("htmlcode", currency.getHtmlcode());
                jArr.put(obj);
            }
            jobj.put("data", jArr);
        } catch (JSONException e) {
            throw ServiceException.FAILURE("ProfileHandler.getAllCurrencies", e);
        }

        return jobj;
    }

    public static JSONObject getCompanyInformation(Session session, HttpServletRequest request) throws ServiceException{
        JSONObject jobj=new JSONObject();
		try {
            String query="from Company where companyID=?";
            List list = HibernateUtil.executeQuery(session, query, AuthHandler.getCompanyid(request));
            Iterator itr = list.iterator();
            JSONArray jArr=new JSONArray();
            while(itr.hasNext()) {
                Company company = (Company) itr.next();
                JSONObject obj = new JSONObject();
                obj.put("phone", company.getPhoneNumber());
                obj.put("state", company.getState());
                obj.put("currency", (company.getCurrency()==null?"1":company.getCurrency().getCurrencyID()));
                obj.put("city", company.getCity());
                obj.put("emailid", company.getEmailID());
                obj.put("companyid", company.getCompanyID());
                obj.put("timezone", (company.getTimeZone()==null?"1":company.getTimeZone().getTimeZoneID()));
                obj.put("zip", company.getZipCode());
                obj.put("fax", company.getFaxNumber());
                obj.put("website", company.getWebsite());
                obj.put("image", company.getCompanyLogo());
                obj.put("modifiedon", (company.getModifiedOn()==null?"":AuthHandler.getDateFormatter(request).format(company.getModifiedOn())));
                obj.put("createdon", (company.getCreatedOn()==null?"":AuthHandler.getDateFormatter(request).format(company.getCreatedOn())));
                obj.put("companyname", company.getCompanyName());
                obj.put("country", (company.getCountry()==null?"":company.getCountry().getID()));
                obj.put("address", company.getAddress());
                obj.put("subdomain", company.getSubDomain());
                jArr.put(obj);
            }
            jobj.put("data", jArr);
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE("ProfileHandler.getCompanyInformation", ex);
        } catch (JSONException e) {
            throw ServiceException.FAILURE("ProfileHandler.getCompanyInformation", e);
        }

        return jobj;
    }

    private static String getFormattedDate(Date curDate, String javaForm) {
        SimpleDateFormat sdf=new SimpleDateFormat(javaForm);
        return sdf.format(curDate);
    }

    public static JSONObject getAllDateFormats(Session session, HttpServletRequest request) throws ServiceException{
        JSONObject jobj=new JSONObject();
		try {
            String query="from KWLDateFormat";
            List list = HibernateUtil.executeQuery(session, query);
            Iterator itr = list.iterator();
            JSONArray jArr=new JSONArray();
            Date curDate=new Date();
            while(itr.hasNext()) {
                KWLDateFormat dateFormat = (KWLDateFormat) itr.next();
                JSONObject obj = new JSONObject();
                obj.put("formatid", dateFormat.getFormatID());
                obj.put("formalname", dateFormat.getName());
                obj.put("name", getFormattedDate(curDate,dateFormat.getJavaForm()));
                obj.put("javaform", dateFormat.getJavaForm());
                obj.put("scriptform", dateFormat.getScriptForm());
                jArr.put(obj);
            }
            jobj.put("data", jArr);
        } catch (JSONException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }

        return jobj;
    }

    public static JSONObject getCompanyHolidays(Session session, HttpServletRequest request) throws ServiceException{
        JSONObject jobj=new JSONObject();
		try {
            String query="from CompanyHoliday where company.companyID=? order by holidayDate";
            List list = HibernateUtil.executeQuery(session, query, AuthHandler.getCompanyid(request));
            Iterator itr = list.iterator();
            JSONArray jArr=new JSONArray();
            while(itr.hasNext()) {
                CompanyHoliday holiday = (CompanyHoliday) itr.next();
                JSONObject obj = new JSONObject();
                obj.put("holiday", AuthHandler.getDateFormatter(request).format(holiday.getHolidayDate()));
                obj.put("description", holiday.getDescription());
                jArr.put(obj);
            }
            jobj.put("data", jArr);
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE("ProfileHandler.getCompanyHolidays", ex);
        } catch (JSONException e) {
            throw ServiceException.FAILURE("ProfileHandler.getCompanyHolidays", e);
        }

        return jobj;
    }

    public static JSONObject getAllCountries(Session session, HttpServletRequest request) throws ServiceException{
        JSONObject jobj=new JSONObject();
		try {
            String query="from Country";
            List list = HibernateUtil.executeQuery(session, query);
            Iterator itr = list.iterator();
            JSONArray jArr=new JSONArray();
            while(itr.hasNext()) {
                Country country = (Country) itr.next();
                JSONObject obj = new JSONObject();
                obj.put("id", country.getID());
                obj.put("name", country.getCountryName());
                jArr.put(obj);
            }
            jobj.put("data", jArr);
        } catch (JSONException e) {
            throw ServiceException.FAILURE("ProfileHandler.getAllCountries", e);
        }

        return jobj;
    }

    public static void updateCompany(Session session, HttpServletRequest request, HashMap hm) throws ServiceException {
        try {
            Company company=(Company)session.get(Company.class, AuthHandler.getCompanyid(request));
            company.setCompanyName((String)hm.get("companyname"));
            company.setAddress((String)hm.get("address"));
            company.setCity((String)hm.get("city"));
            company.setState((String)hm.get("state"));
            company.setZipCode((String)hm.get("zip"));
            company.setPhoneNumber((String)hm.get("phone"));
            company.setFaxNumber((String)hm.get("fax"));
            company.setWebsite((String)hm.get("website"));
            company.setEmailID((String)hm.get("mail"));
            company.setSubDomain((String)hm.get("domainname"));
            company.setCountry((Country)session.get(Country.class, (String)hm.get("country")));
            company.setCurrency((KWLCurrency)session.get(KWLCurrency.class, (String)hm.get("currency")));
            KWLTimeZone timeZone=(KWLTimeZone)session.get(KWLTimeZone.class, (String)hm.get("timezone"));
            company.setTimeZone(timeZone);
            company.setModifiedOn(new Date());
            JSONArray jArr=new JSONArray((String)hm.get("holidays"));
            Set<CompanyHoliday> holidays=company.getHolidays();
            holidays.clear();
            for(int i=0; i<jArr.length();i++){
                CompanyHoliday day=new CompanyHoliday();
                JSONObject obj=jArr.getJSONObject(i);
                day.setDescription(obj.getString("description"));
                day.setHolidayDate(AuthHandler.getDateFormatter(request).parse(obj.getString("day")));
                day.setCompany(company);
                holidays.add(day);
            }
            String imageName=((FileItem)(hm.get("logo"))).getName();
            if(StringUtil.isNullOrEmpty(imageName)==false){
                String fileName= AuthHandler.getCompanyid(request)+FileUploadHandler.getCompanyImageExt();
                company.setCompanyLogo(ProfileImageServlet.ImgBasePath+fileName);
                new FileUploadHandler().uploadImage((FileItem)hm.get("logo"),
                        fileName,
                        StorageHandler.GetProfileImgStorePath(),130,25,true,false);
            }
            session.update(company);
            SessionHandler.updatePreferences(request,(String)hm.get("currency"),null,(String)hm.get("timezone"),timeZone.getDifference());
            insertAuditLog(session, AuditAction.COMPANY_UPDATION, "User "+AuthHandler.getUserName(request) + " changed company details", request);
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("ProfileHandler.updateCompany", ex);
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE("ProfileHandler.updateCompany", ex);
        } catch (JSONException ex){
            throw ServiceException.FAILURE("ProfileHandler.updateCompany", ex);
        }
    }

    public static void deleteCompany(Session session, HttpServletRequest request)  throws ServiceException{
        try {
                Company company=(Company)session.get(Company.class, request.getParameter("companyid"));
//@@@                company.setDeleted(true);
                company.setDeleted(1);
                session.update(company);
                insertAuditLog(session, AuditAction.COMPANY_DELETION, "User "+AuthHandler.getUserName(request) + " deleted company "+company.getCompanyName(), request);
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }

    public static void insertAuditLog(Session session,String actionid, String details, HttpServletRequest request)  throws ServiceException{
            AuditAction action=(AuditAction)session.get(AuditAction.class, actionid);
            insertAuditLog(session, action, details, request);
    }

    public static void insertAuditLog(Session session,String actionid, String details, String ipAddress, String userid)  throws ServiceException{
            AuditAction action=(AuditAction)session.get(AuditAction.class, actionid);
            User user=(User)session.get(User.class, userid);
            insertAuditLog(session, action, details, ipAddress, user);
    }

    public static void insertAuditLog(Session session,AuditAction action, String details, HttpServletRequest request)  throws ServiceException{
        try {
            User user = (User) session.get(User.class, AuthHandler.getUserid(request));
            String ipaddr = null;
            if (StringUtil.isNullOrEmpty(request.getHeader("x-real-ip"))) {
                ipaddr = request.getRemoteAddr();
            } else {
                ipaddr = request.getHeader("x-real-ip");
            }
            insertAuditLog(session, action, details, ipaddr, user);
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
     }

    public static void insertAuditLog(Session session,AuditAction action, String details, String ipAddress, User user)  throws ServiceException{
//            AuditTrail auditTrail=new AuditTrail();
//            auditTrail.setAction(action);
//            auditTrail.setAuditTime(new Date());
//            auditTrail.setDetails(details);
//            auditTrail.setIPAddress(ipAddress);
//            auditTrail.setUser(user);
//            session.save(auditTrail);
    }

    public static JSONObject getAuditTrail(Session session, HttpServletRequest request) throws ServiceException{
        JSONObject jobj=new JSONObject();
		try {
            String start=request.getParameter("start");
            String limit=request.getParameter("limit");
            String ss=request.getParameter("ss");
            String gid=request.getParameter("groupid");
            ArrayList params=new ArrayList();
            String condition="";
            params.add(AuthHandler.getCompanyid(request));
            condition=" where user.company.companyID=?";
            if(StringUtil.isNullOrEmpty(ss)==false){
                params.add(ss+"%");
                params.add(ss+"%");
                if(condition.length()>0)
                    condition+=" and";
                else
                    condition+=" where";
                condition+= " (user.firstName like ? or user.lastName like ?)";
            }
            if(StringUtil.isNullOrEmpty(gid)==false){
                params.add(gid);
                if(condition.length()>0)
                    condition+=" and";
                else
                    condition+=" where";
                condition+= " action.auditGroup.ID like ?";
            }
            String query="from AuditTrail"+condition +" order by auditTime desc";

            List list=list = HibernateUtil.executeQuery(session, query,params.toArray());
            int count=list.size();
            if(StringUtil.isNullOrEmpty(start)==false&&StringUtil.isNullOrEmpty(limit)==false){
                list=HibernateUtil.executeQueryPaging(session, query,params.toArray(), new Integer[]{Integer.parseInt(start),Integer.parseInt(limit)});
            }

            Iterator itr = list.iterator();
            JSONArray jArr=new JSONArray();
            while(itr.hasNext()) {
                AuditTrail auditTrail = (AuditTrail) itr.next();
                JSONObject obj = new JSONObject();
                obj.put("id", auditTrail.getID());
                obj.put("username", AuthHandler.getFullName(auditTrail.getUser()));
                obj.put("ipaddr", auditTrail.getIPAddress());
                obj.put("details", auditTrail.getDetails());
                obj.put("timestamp", AuthHandler.getDateFormatter(request).format(auditTrail.getAuditTime()));
                jArr.put(obj);
            }
            jobj.put("data", jArr);
            jobj.put("count", count);
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (JSONException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }

        return jobj;
    }

    public static JSONObject getAuditGroups(Session session, HttpServletRequest request) throws ServiceException{
        JSONObject jobj=new JSONObject();
		try {
            String start=request.getParameter("start");
            String limit=request.getParameter("limit");
            String query="from AuditGroup";

            List list=list = HibernateUtil.executeQuery(session, query);
            int count=list.size();
            if(StringUtil.isNullOrEmpty(start)==false&&StringUtil.isNullOrEmpty(limit)==false){
                list=HibernateUtil.executeQueryPaging(session, query, new Integer[]{Integer.parseInt(start),Integer.parseInt(limit)});
            }

            Iterator itr = list.iterator();
            JSONArray jArr=new JSONArray();
            while(itr.hasNext()) {
                AuditGroup auditGroup = (AuditGroup) itr.next();
                JSONObject obj = new JSONObject();
                obj.put("groupid", auditGroup.getID());
                obj.put("groupname", auditGroup.getGroupName());
                jArr.put(obj);
            }
            jobj.put("data", jArr);
            jobj.put("count", count);
        } catch (JSONException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }

        return jobj;
    }

    public static String getFormattedCurrency(double value, String currencyid,Session session) throws HibernateException, SessionExpiredException{
        DecimalFormat df=new DecimalFormat("#,##0.00");
        KWLCurrency c=(KWLCurrency)session.get(KWLCurrency.class, currencyid);
        //Temp fix need to find out the html code of INR
          //Temp fix need to find out the html code of INR
//                if(c.getHtmlcode().equals("20A8"))
//                    return "Rs. " +df.format(value);
        String sym;
        try{
            sym=new Character((char)Integer.parseInt(c.getHtmlcode(),16)).toString();
        }catch(Exception e){
            sym=c.getHtmlcode();
        }
        return sym+" "+df.format(value);
    }
}
