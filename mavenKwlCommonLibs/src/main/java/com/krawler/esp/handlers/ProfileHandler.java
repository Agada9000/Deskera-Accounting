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

import com.krawler.common.session.SessionExpiredException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.hibernate.Session;

import com.krawler.common.admin.Assignmanager;
import com.krawler.common.admin.AuditAction;
import com.krawler.common.admin.AuditGroup;
import com.krawler.common.admin.AuditTrail;
import com.krawler.common.admin.Company;
import com.krawler.common.admin.CompanyHoliday;
import com.krawler.common.admin.Country;
import com.krawler.common.admin.KWLCurrency;
import com.krawler.common.admin.KWLDateFormat;
import com.krawler.common.admin.KWLTimeZone;
import com.krawler.common.admin.ProjectFeature;
import com.krawler.common.admin.RoleUserMapping;
import com.krawler.common.admin.Rolelist;
import com.krawler.common.admin.User;
import com.krawler.common.admin.UserLogin;
import com.krawler.common.admin.UserPermission;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.KWLErrorMsgs;
import com.krawler.common.util.StringUtil;
import com.krawler.common.util.URLUtil;
//import com.krawler.crm.dbhandler.UserManagement;
import com.krawler.esp.Search.SearchBean;
import com.krawler.esp.hibernate.impl.HibernateUtil;
import com.krawler.esp.utils.ConfigReader;
import com.krawler.esp.web.resource.Links;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import javax.servlet.http.HttpSession;
import org.apache.lucene.search.Hit;
import org.apache.lucene.search.Hits;
import org.hibernate.Transaction;

public class ProfileHandler {
    public static String getUserFullName(Session session, String userid) throws ServiceException{
        String name=null;
        try {
            String SELECT_USER_INFO="select u.firstName, u.lastName from User as u " +
                    "where u.userID = ?  and u.deleteflag=0 ";
            List list = HibernateUtil.executeQuery(session, SELECT_USER_INFO, userid);
            Iterator ite = list.iterator();
            if( ite.hasNext() ) {
                Object[] row = (Object[]) ite.next();
                name=(StringUtil.isNullOrEmpty((String)row[0])?"":row[0])+" "+(StringUtil.isNullOrEmpty((String)row[1])?"":row[1]);
            }
        } catch (Exception e) {
            throw ServiceException.FAILURE("ProfileHandler.getUserFullName", e);
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
                    if(timediff>48)//||timediff<0
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
            String SELECT_USER_INFO="update Vendor set taxIDNumber=? " +
                "where v.id = ?";
            int count = HibernateUtil.executeUpdate(session, SELECT_USER_INFO, params.toArray());
            if(count>0)
                msg="Your information has been updated successfully.";
            else
                msg="Error";
        } catch (Exception e) {
            msg="Error";
            throw ServiceException.FAILURE("ProfileHandler.setPassword", e);
        }
        return msg;
    }
    
    public static JSONObject getAllUserDetails(Session session, HttpServletRequest request) throws ServiceException{
        JSONObject jobj=new JSONObject();
		try {
            String lid=request.getParameter("lid");
            String start=request.getParameter("start");
            String limit=request.getParameter("limit");
            String serverSearch = request.getParameter("ss");
            String filterStr="";
            Object[] params={AuthHandler.getCompanyid(request)};
            if(StringUtil.isNullOrEmpty(lid)==false){
                filterStr=" and u.userID=?";
                params=new Object[]{AuthHandler.getCompanyid(request),lid};
            }
            String SELECT_USER_INFO="from User u where u.company.companyID=? and u.deleteflag=0 "+filterStr;
            if(!StringUtil.isNullOrEmpty(serverSearch)) {
                SELECT_USER_INFO+=" and (u.firstName like '%"+serverSearch+"%'";
                SELECT_USER_INFO+=" or u.lastName like '%"+serverSearch+"%')";
            }
            List list=list = HibernateUtil.executeQuery(session, SELECT_USER_INFO,params);
            int count=list.size();
            if(StringUtil.isNullOrEmpty(start)==false&&StringUtil.isNullOrEmpty(limit)==false){
                list=HibernateUtil.executeQueryPaging(session, SELECT_USER_INFO,params, new Integer[]{Integer.parseInt(start),Integer.parseInt(limit)});
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
                obj.put("tzid", (user.getTimeZone()==null?"23":user.getTimeZone().getTimeZoneID())); // 23 is id of New York Time Zone. [default]
                obj.put("callwithid", user.getCallwith());
                obj.put("timeformat",(user.getTimeformat()!=1 && user.getTimeformat()!=2)?2:user.getTimeformat()); // 2 is id for '24 hour timeformat'. [default]
                String roleStr = "";
                obj.put("roles", roleStr);
                jArr.put(obj);
            }
            jobj.put("data", jArr);
            jobj.put("count", count);
        } catch (Exception e) {
                throw ServiceException.FAILURE("ProfileHandler.getAllUserDetails", e);
        }

        return jobj;
    }

    public static JSONObject getAllManagers(Session session, HttpServletRequest request) throws ServiceException {
        JSONObject jobj = new JSONObject();
        try {
            String companyid=AuthHandler.getCompanyid(request);
            String role = " and ( bitwise_and( roleID , 2 ) = 2 ) ";
            String SELECT_USER_INFO = "select userID, userLogin.userName, firstName, lastName, image, " +
                    "emailID, userLogin.lastActivityDate, aboutUser, address, contactNumber from User  where company.companyID=?  and deleteflag=0 " + role;
            List list = HibernateUtil.executeQuery(session, SELECT_USER_INFO,new Object[]{companyid});
            Iterator itr = list.iterator();
            JSONArray jArr = new JSONArray();
            while (itr.hasNext()) {
                Object[] row = (Object[]) itr.next();
                JSONObject obj = new JSONObject();
                obj.put("userid", row[0]);
                obj.put("username", row[1]);
                obj.put("fname", row[2]);
                obj.put("lname", row[3]);
                obj.put("image", row[4]);
                obj.put("emailid", row[5]);
                obj.put("lastlogin", (row[6] == null ? "" : AuthHandler.getDateFormatter(request).format(row[6])));
                obj.put("aboutuser", row[7]);
                obj.put("address", row[8]);
                obj.put("contactno", row[9]);
                jArr.put(obj);
            }
            jobj.put("data", jArr);
        } catch (Exception e) {
            throw ServiceException.FAILURE("ProfileHandler.getAllManagers", e);
        }

        return jobj;
    }

	public static JSONObject getValidUserOptions(Session session, String userid) throws ServiceException {
		return new JSONObject();
	}

    public static void saveDateFormat(Session session, HttpServletRequest request)  throws ServiceException{
		try {
            String id=request.getParameter("userid");
            String dateid=request.getParameter("newformat");
            User user=null;
            if(id!=null&&id.length()>0){
                user = (User)session.load(User.class, id);
            }
            if(StringUtil.isNullOrEmpty(dateid)==false)
                user.setDateFormat((KWLDateFormat)session.load(KWLDateFormat.class, dateid));
            session.saveOrUpdate(user);
            SessionHandler.updateDatePreferences(request, dateid);
        } catch (Exception e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
    }
      
    public static void addUserEntryForEmails(String loginid,User user,UserLogin userLogin, String pwdtext, boolean isPasswordText)  throws ServiceException{
        try {
            String baseURLFormat = StorageHandler.GetSOAPServerUrl();
            String url = baseURLFormat + "defaultUserEntry.php";
            URL u = new URL(url);
            URLConnection uc = u.openConnection();
            uc.setDoOutput(true);
            uc.setUseCaches(false);
            uc.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
            String userName = userLogin.getUserName() +"_"+user.getCompany().getSubDomain();
            String strParam = "loginid="+loginid +"&userid="+user.getUserID()+"&username="+userName
                    +"&password="+userLogin.getPassword()+"&pwdtext="+pwdtext+"&fullname="+(user.getFirstName() + user.getLastName())+"&isadmin=0&ispwdtest="+isPasswordText;
            PrintWriter pw = new PrintWriter(uc.getOutputStream());
            pw.println(strParam);
            pw.close();
            BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream()));
            String line="";
            String returnStr = "";
            while ((line = in.readLine()) != null) {
                returnStr+=line;
            }
            in.close();
            returnStr = returnStr.replaceAll("\\s+"," ").trim();
            JSONObject emailresult = new JSONObject(returnStr);
            if(Boolean.parseBoolean(emailresult.getString("success"))) {
                user.setUser_hash(emailresult.getString("pwd"));
            }
        } catch (IOException e){
            throw ServiceException.FAILURE(e.getMessage(), e);
        } catch (JSONException e){
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
    }
    
    public static void deleteUser(Session session, HttpServletRequest request)  throws ServiceException{
        try {
            String[] ids=request.getParameterValues("userids");
            for(int i=0;i<ids.length;i++){
                User u=(User)session.load(User.class, ids[i]);
//                if( ( u.getRoleID() & 1 ) == 1)throw new Exception("Cannot delete Administrator");
                if( u.getUserID().equals(u.getCompany().getCreator().getUserID()) )throw new Exception("Cannot delete Company Administrator");
                UserLogin userLogin=(UserLogin)session.load(UserLogin.class, ids[i]);
                session.delete(userLogin);
            }
        } catch (Exception e){
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
    }

    public static void setPassword(Session session, HttpServletRequest request) throws ServiceException {
        try {
            String password=request.getParameter("password");
            if(password==null||password.length()<=0)password=AuthHandler.generateNewPassword();
            String newpass=AuthHandler.getSHA1(password);
            User user=(User)session.load(User.class, request.getParameter("userid"));
            UserLogin userLogin=user.getUserLogin();
            userLogin.setPassword(newpass);
            session.saveOrUpdate(userLogin);
            String uri = URLUtil.getPageURL(request, Links.loginpageFull);
            String fname = user.getFirstName();
			if (StringUtil.isNullOrEmpty(fname))
				fname = user.getUserLogin().getUserName();
            String pmsg = String
                    .format(
                            KWLErrorMsgs.msgTempPassword,
                            fname, password, uri);
            String htmlmsg = String
                    .format(
                            KWLErrorMsgs.msgMailPassword,
                            fname, password, uri, uri);
            try {
                SendMailHandler.postMail(new String[] { user.getEmailID() },
                        KWLErrorMsgs.msgMailSubjectPassword, htmlmsg,
                        pmsg, KWLErrorMsgs.adminEmailId);
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        } catch (Exception e){
            throw ServiceException.FAILURE("ProfileHandler.setPassword", e);
        }
    }

    public static JSONObject setPermissions(Session session, HttpServletRequest request) throws ServiceException {
        JSONObject jobj = new JSONObject();
        List ll = null;
        try {
            String id=request.getParameter("userid");
            String roleId = request.getParameter("roleid");
            String[] features=request.getParameterValues("features");
            String[] permissions=request.getParameterValues("permissions");
            String sql="select id from RoleUserMapping where userId.userID=? and roleId.roleid=?";
            ll = HibernateUtil.executeQuery(session, sql, new Object[] {id, roleId});
            String rid = ll.get(0).toString();
            RoleUserMapping rum = (RoleUserMapping) session.load(RoleUserMapping.class, rid);
            Rolelist role = (Rolelist) session.load(Rolelist.class, roleId);
            String Hql = "delete from UserPermission where roleId=?";
            HibernateUtil.executeUpdate(session, Hql, rum);
            UserLogin userLogin = (UserLogin)session.load(UserLogin.class, id);
            for(int i=0;i<features.length;i++){
                if(permissions[i].equals("0"))continue;
                UserPermission permission = new UserPermission();
                permission.setRole(role);
                permission.setFeature((ProjectFeature)session.load(ProjectFeature.class, features[i]));
                permission.setPermissionCode(Long.parseLong(permissions[i]));
                session.save(permission);
            }
            ProfileHandler.insertAuditLog(session, AuditAction.ADMIN_Permission,
                " Permissions updated for " +userLogin.getUser().getFirstName()+" "+userLogin.getUser().getLastName(),
                request, id);
            if(id.equals(AuthHandler.getUserid(request))) {
                resetSessionPermissions(session, request, userLogin);
                jobj = PermissionHandler.getPermissionsValidate(session, userLogin.getUserID(), request);
            }

        } catch (Exception e){
            throw ServiceException.FAILURE("ProfileHandler.setPermissions", e);
        }
        return jobj;
    }
    public static void resetSessionPermissions(Session session, HttpServletRequest request, UserLogin userLogin) throws ServiceException {
        HttpSession httpsession = request.getSession(true);
        try {
            String query = "select up.feature.featureName, up.permissionCode from UserPermission up where up.roleId.userId.userID=?";
            List list = HibernateUtil.executeQuery(session, query, userLogin.getUserID());
            Iterator ite2 = list.iterator();
            while (ite2.hasNext()) {
                Object[] roww = (Object[]) ite2.next();
                httpsession.setAttribute(roww[0].toString(), roww[1]);                
            }
        } catch (Exception e) {
            throw ServiceException.FAILURE("ProfileHandler.resetSessionPermissions", e);
        }
    }

    public static void setRoles(Session session, HttpServletRequest request) throws ServiceException {
        try {
            String id = request.getParameter("userid");
            int roleid = Integer.parseInt(request.getParameter("bit"));
            // String[] roles=request.getParameterValues("roles");

            User user = (User) session.get(User.class, id);
//            user.setRoleID(roleid);
            session.save(user);
            ProfileHandler.insertAuditLog(session, AuditAction.ADMIN_Role,
                       " Roles updated for " +user.getFirstName()+" "+user.getLastName(),
                      request, id);
        } catch (Exception e) {
            throw ServiceException.FAILURE("ProfileHandler.setRoles", e);
        }
    }

    public static void updateLastLogin(Session session, HttpServletRequest request) throws ServiceException {
        try {
            UserLogin userLogin=(UserLogin)session.load(UserLogin.class, AuthHandler.getUserid(request));
            userLogin.setLastActivityDate(new Date());
            session.update(userLogin);
        } catch (Exception e){
            throw ServiceException.FAILURE("ProfileHandler.updateLastLogin", e);
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
        } catch (Exception e) {
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
        } catch (Exception e) {
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
        } catch (Exception e) {
            throw ServiceException.FAILURE("ProfileHandler.getCompanyInformation", e);
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
        } catch (Exception e) {
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
        } catch (Exception e) {
            throw ServiceException.FAILURE("ProfileHandler.getAllCountries", e);
        }

        return jobj;
    }
    
    public static void assignManager(Session session, HttpServletRequest request) throws ServiceException {

        String[] userids = request.getParameterValues("userid");
        String[] managerids = request.getParameterValues("managerid");
        try {
            for (int i = 0; i < userids.length; i++) {
                for (int j = 0; j < managerids.length; j++) {
                    Object[] obj = {userids[i], managerids[j]};
                    String hql = "delete from Assignmanager am where am.assignemp.userID=? and am.assignman.userID=?";
                    HibernateUtil.executeUpdate(session, hql, obj);
                }
            }
            for (int i = 0; i < userids.length; i++) {
                for (int j = 0; j < managerids.length; j++) {
                    if (!userids[i].equals(managerids[j])) {
                        User li1 = (User) session.load(User.class, userids[i]);
                        User li2 = (User) session.load(User.class, managerids[j]);
                        String id = java.util.UUID.randomUUID().toString();
                        Assignmanager contact = new Assignmanager();
                        contact.setId(id);
                        contact.setAssignemp(li1);
                        contact.setAssignman(li2);
                        session.save(contact);
                    }
                }
            }
        } catch (Exception e) {
            throw ServiceException.FAILURE("ProfileHandler.assignManager", e);
        }
    }

     public static JSONObject getAllCompanyDetails(Session session, HttpServletRequest request) throws ServiceException{
        JSONObject jobj=new JSONObject();
		try {
            String SelectCompany="from Company where deleted=0 order by createdOn desc";
         //   String q="select company.companyID from User where userLogin.userID=? ";
            String superCompany=null;
//            try{
//                superCompany=(String)HibernateUtil.executeQuery(session, q, "").get(0);
//            }catch(Exception ee){}
            List list = HibernateUtil.executeQuery(session,SelectCompany);
            Iterator itr = list.iterator();
            JSONArray jArr=new JSONArray();
            while(itr.hasNext()) {
                Company company=(Company) itr.next();
                JSONObject obj = new JSONObject();
                obj.put("companyid", company.getCompanyID());
                obj.put("companyname",company.getCompanyName());
//                String uq="from User where roleID=1 and company.companyID=?";
//                User u = (User)HibernateUtil.executeQuery(session, uq, company.getCompanyID()).get(0);
//                obj.put("admin_fname", u.getFirstName());
//                obj.put("admin_lname", u.getLastName());
//                obj.put("admin_uname", u.getUserLogin().getUserName());

                obj.put("admin_fname", company.getCreator().getFirstName());
                obj.put("admin_lname", company.getCreator().getLastName());
                obj.put("admin_uname", company.getCreator().getUserLogin().getUserName());

                obj.put("subdomain",company.getSubDomain());
                obj.put("address",company.getAddress());
                obj.put("city",company.getCity());
                obj.put("phoneno",company.getPhoneNumber());
                obj.put("emailid",company.getEmailID());
                obj.put("createdon",company.getCreatedOn());
                obj.put("image",company.getCompanyLogo());
                obj.put("country",(company.getCountry()==null)?"":company.getCountry().getCountryName());

                jArr.put(obj);
            }
            jobj.put("data", jArr);
        } catch (Exception e) {
                throw ServiceException.FAILURE("ProfileHandler.getAllCompanyDetails", e);
        }
        return jobj;
    }

     public static JSONObject getUserofCompany(Session session, HttpServletRequest request) throws ServiceException{
        JSONObject jobj=new JSONObject();
		try {
            String SELECT_USER="from User as u where u.company.companyID=?  and u.deleteflag=0 ";
            String cid=request.getParameter("companyid");
            List list = HibernateUtil.executeQuery(session,SELECT_USER,cid);
            Iterator itr = list.iterator();
            JSONArray jArr=new JSONArray();

            while(itr.hasNext()) {
                User user=(User) itr.next();
                JSONObject obj = new JSONObject();
                obj.put("userid",user.getUserID());
                obj.put("username",user.getUserLogin().getUserName());
                obj.put("fname",user.getFirstName());
                obj.put("lname",user.getLastName());
                obj.put("image",user.getImage());
                obj.put("emailid",user.getEmailID());
                obj.put("lastlogin",(user.getUserLogin().getLastActivityDate()==null?"":AuthHandler.getDateFormatter(request).format(user.getUserLogin().getLastActivityDate())));
                obj.put("aboutuser",user.getAboutUser());
                obj.put("address",user.getAddress());
                obj.put("contactno",user.getContactNumber());
                obj.put("image",user.getImage());
                String roleStr="";
                obj.put("roles", roleStr);
                jArr.put(obj);
            }
            jobj.put("data", jArr);
        } catch (Exception e) {
                throw ServiceException.FAILURE("ProfileHandler.getAllUserDetails", e);
        }
        return jobj;
    }///////End Users Of The Selected Company

    public static void deleteCompany(Session session, HttpServletRequest request)  throws ServiceException{
        try {
                Company company=(Company)session.load(Company.class, request.getParameter("companyid"));
                company.setDeleted(1);
                session.update(company);
        } catch (Exception e){
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
    }

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
        } catch (Exception e) {
                throw ServiceException.FAILURE(e.getMessage(), e);
        }

        return jobj;
    }


    public static void insertAuditLog(String actionid, String details, HttpServletRequest request, String recid) throws ServiceException {
        Transaction tx = null;
        Session session = null;
        try {
            session = HibernateUtil.getCurrentSession();
            tx = session.beginTransaction();
            AuditAction action = (AuditAction) session.load(AuditAction.class, actionid);
            insertAuditLog(session, action, details, request, recid,"0");
            tx.commit();
        } catch (ServiceException ex) {
            if (tx != null) {
                tx.rollback();
            }
            throw ex;
        } catch (Exception ex) {
            if (tx != null) {
                tx.rollback();
            }
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
    }

    public static void insertAuditLog(Session session,String actionid, String details, HttpServletRequest request, String recid,String extraid)  throws ServiceException{
        try {
            AuditAction action=(AuditAction)session.load(AuditAction.class, actionid);
            insertAuditLog(session, action, details, request, recid, extraid);
        } catch (Exception e){
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
    }

    public static void insertAuditLog(Session session,String actionid, String details, HttpServletRequest request, String recid)  throws ServiceException{
        try {
            AuditAction action=(AuditAction)session.load(AuditAction.class, actionid);
            insertAuditLog(session, action, details, request, recid,"0");
        } catch (Exception e){
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
    }

    public static void insertAuditLog(Session session,String actionid, String details, String ipAddress, String userid, String recid)  throws ServiceException{
        try {
            AuditAction action=(AuditAction)session.load(AuditAction.class, actionid);
            User user=(User)session.load(User.class, userid);
            insertAuditLog(session, action, details, ipAddress, user, recid,"0");
        } catch (Exception e){
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
    }

    public static void insertAuditLog(Session session,AuditAction action, String details, HttpServletRequest request, String recid, String extraid)  throws ServiceException{
        try {
            User user=(User)session.load(User.class, AuthHandler.getUserid(request));
            String ipaddr = null;
            if(StringUtil.isNullOrEmpty(request.getHeader("x-real-ip"))){
                ipaddr = request.getRemoteAddr();
            }else{
                ipaddr = request.getHeader("x-real-ip");
            }

            insertAuditLog(session, action, details, ipaddr, user, recid , extraid);
        } catch (Exception e){
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
    }

    public static void insertAuditLog(Session session,AuditAction action, String details, String ipAddress, User user, String recid , String extraid)  throws ServiceException{
        try {
            String aid = UUID.randomUUID().toString();
            AuditTrail auditTrail=new AuditTrail();
            auditTrail.setID(aid);
            auditTrail.setAction(action);
            auditTrail.setAuditTime(new Date());
            auditTrail.setDetails(details);
            auditTrail.setIPAddress(ipAddress);
            auditTrail.setRecid(recid);
            auditTrail.setUser(user);
            auditTrail.setExtraid(extraid);
            session.save(auditTrail);


            ArrayList<Object> indexFieldDetails = new ArrayList<Object>();
              ArrayList<String> indexFieldName = new ArrayList<String>();
              indexFieldDetails.add(details);
               indexFieldName.add("details");
               indexFieldDetails.add(aid);
               indexFieldName.add("transactionid");
               indexFieldDetails.add(action.getID());
               indexFieldName.add("actionid");
               indexFieldDetails.add(ipAddress);
               indexFieldName.add("ipaddr");
               String userName = user.getUserLogin().getUserName()+" "+user.getFirstName()+" "+user.getLastName();
               indexFieldDetails.add(userName);
               indexFieldName.add("username");
               indexFieldDetails.add(auditTrail.getAuditTime());
               indexFieldName.add("timestamp");
                String indexPath = com.krawler.esp.handlers.StorageHandler.GetAuditTrailIndexPath();
                com.krawler.esp.indexer.KrawlerIndexCreator kwlIndex = new com.krawler.esp.indexer.KrawlerIndexCreator();
                kwlIndex.setIndexPath(indexPath);
                com.krawler.esp.indexer.CreateIndex  cIndex = new com.krawler.esp.indexer.CreateIndex();
                cIndex.indexAlert(kwlIndex, indexFieldDetails, indexFieldName);

        } catch (Exception e){
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
    }

    public static JSONObject getAuditGroups(Session session, HttpServletRequest request) throws ServiceException {
        JSONObject jobj = new JSONObject();
        try {
            String query = "from AuditGroup";
            List list = HibernateUtil.executeQuery(session, query);
            int count = list.size();
            Iterator itr = list.iterator();
            JSONArray jArr = new JSONArray();
            JSONObject objN = new JSONObject();
            objN.put("groupid", "");
            objN.put("groupname", "--All--");
            jArr.put(objN);

            while (itr.hasNext()) {
                AuditGroup auditGroup = (AuditGroup) itr.next();
                JSONObject obj = new JSONObject();
                obj.put("groupid", auditGroup.getID());
                obj.put("groupname", auditGroup.getGroupName());
                jArr.put(obj);
            }
            jobj.put("data", jArr);
            jobj.put("count", count);
        } catch (Exception e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }

        return jobj;
    }

    public static JSONObject getAuditSearch(Session session, HttpServletRequest request) throws ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        try {
            int start = Integer.parseInt(request.getParameter("start"));
            int limit = Integer.parseInt(request.getParameter("limit"));
            String groupid = request.getParameter("groupid");
            String searchtext = request.getParameter("search");
            String companyid = AuthHandler.getCompanyid(request);

            String auditID = "";
            if (searchtext.compareTo("") != 0) {
                String query2 = searchtext + "*"; 
                SearchBean bean = new SearchBean();
                String indexPath = StorageHandler.GetAuditTrailIndexPath();
                String[] searchWithIndex = {"details", "ipaddr", "username"};
                Hits hitResult = bean.skynetsearchMulti(query2, searchWithIndex, indexPath);
                if (hitResult != null) {
                    Iterator itrH = hitResult.iterator();
                    while (itrH.hasNext()) {
                        Hit hit1 = (Hit) itrH.next();
                        org.apache.lucene.document.Document doc = hit1.getDocument();
                        auditID += "'" + doc.get("transactionid") + "',";
                    }
                    if (auditID.length() > 0) {
                        auditID = auditID.substring(0, auditID.length() - 1);
                    }
                }
            }
            List listCount = null;
            if (groupid.compareTo("") != 0 && searchtext.compareTo("") != 0) {  /* query for both gid and search  */
                String query = "from AuditTrail where user.company.companyID=? and ID in (" + auditID + ") and action.auditGroup.ID = ? order by auditTime desc";
                listCount = HibernateUtil.executeQuery(session, query, new Object[]{companyid, groupid});
                List list = HibernateUtil.executeQueryPaging(session, query, new Object[]{companyid, groupid}, new Integer[]{start, limit});
                getAuditJSONData(list, jobj, request);
            } else if (groupid.compareTo("") != 0 && searchtext.compareTo("") == 0) { /* query only for gid  */
                String query = "from AuditTrail where user.company.companyID=? and action.auditGroup.ID = ? order by auditTime desc";
                listCount = HibernateUtil.executeQuery(session, query, new Object[]{companyid, groupid});
                List list = HibernateUtil.executeQueryPaging(session, query, new Object[]{companyid, groupid}, new Integer[]{start, limit});
                getAuditJSONData(list, jobj, request);
            } else if (groupid.compareTo("") == 0 && searchtext.compareTo("") != 0) {  /* query only for search  */
                String query = "from AuditTrail where user.company.companyID=? and ID in (" + auditID + ")  order by auditTime desc";
                listCount = HibernateUtil.executeQuery(session, query, new Object[]{companyid});
                List list = HibernateUtil.executeQueryPaging(session, query, new Object[]{companyid}, new Integer[]{start, limit});
                getAuditJSONData(list, jobj, request);
            } else {        /* query for all  */
                String query = "from AuditTrail where user.company.companyID=?  order by auditTime desc";
                listCount = HibernateUtil.executeQuery(session, query, new Object[]{companyid});
                List list = HibernateUtil.executeQueryPaging(session, query, new Object[]{companyid}, new Integer[]{start, limit});
                getAuditJSONData(list, jobj, request);
            }
            jobj.put("count", listCount.size());

        } catch (IOException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (ServiceException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
        }
        return jobj;
    }

    public static JSONObject getAuditJSONData(List list, JSONObject jobj, HttpServletRequest request) throws ServiceException, SessionExpiredException {
        try {
            Iterator itr = list.iterator();
            JSONArray jArr = new JSONArray();
            while (itr.hasNext()) {
                AuditTrail auditTrail = (AuditTrail) itr.next();
                JSONObject obj = new JSONObject();
                obj.put("id", auditTrail.getID());
                obj.put("username", auditTrail.getUser().getUserLogin().getUserName() + " [ " + AuthHandler.getFullName(auditTrail.getUser()) + " ]");
                obj.put("ipaddr", auditTrail.getIPAddress());
                obj.put("details", auditTrail.getDetails());
                obj.put("actionname", auditTrail.getAction().getActionName());
                obj.put("timestamp", AuthHandler.getDateFormatter(request).format(auditTrail.getAuditTime()));
                jArr.put(obj);
            }
            jobj.put("data", jArr);
            jobj.put("count1", list.size());

        } catch (JSONException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return jobj;
    }

}
