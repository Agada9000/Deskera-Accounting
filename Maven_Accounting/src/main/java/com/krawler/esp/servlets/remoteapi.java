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
package com.krawler.esp.servlets;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.Country;
import com.krawler.common.admin.KWLCurrency;
import com.krawler.common.admin.KWLDateFormat;
import com.krawler.common.admin.KWLTimeZone;
import com.krawler.common.admin.Role;
import com.krawler.common.admin.RoleUserMapping;
import com.krawler.common.admin.Rolelist;
import com.krawler.common.admin.User;
import com.krawler.common.admin.UserLogin;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import com.krawler.common.util.StringUtil;
import com.krawler.esp.hibernate.impl.*;
import org.hibernate.*;
import java.io.*;
import java.sql.SQLException;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.URLUtil;
import com.krawler.esp.handlers.AuthHandler;
import com.krawler.esp.handlers.DashboardHandler;
import com.krawler.esp.handlers.NewCompanyHandler;
import com.krawler.esp.handlers.PermissionHandler;
import com.krawler.esp.handlers.SendMailHandler;
import com.krawler.esp.handlers.StorageHandler;
import com.krawler.esp.web.resource.Links;
import com.krawler.hql.accounting.CompanyAccountPreferences;
import com.krawler.hql.accounting.Product;
import com.krawler.hql.accounting.Producttype;
import com.krawler.spring.storageHandler.storageHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.MessagingException;

public class remoteapi extends HttpServlet {
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        Session session = null;
        String result = "";
        boolean isCommit = false;
        boolean isTestMode = false;
        int action = 0;
        String validkey = storageHandlerImpl.GetRemoteAPIKey();
        String remoteapikey = "";
        if(!StringUtil.isNullOrEmpty(request.getParameter("data"))) {

            try {
               JSONObject jobj = new JSONObject(request.getParameter("data"));
                isCommit= (jobj.has("iscommit") && jobj.getBoolean("iscommit"));
                isTestMode = (jobj.has("test") && jobj.getBoolean("test"));
                if(jobj.has("remoteapikey"))
                    remoteapikey = jobj.getString("remoteapikey");
                session = HibernateUtil.getCurrentSession();
                session.beginTransaction();
                action = Integer.parseInt(request.getParameter("action"));
                switch (action) {
                    case 0://check for companyid
                        result = CompanyidExits(session, request);
                        break;
                    case 1://Check for userid or username
                        result = UserExits(session, request);
                        break;
                    case 2://create user
                        result = isCompanyActivated(session, jobj) ? createUser(session, request) : getMessage(2, 99);
                        break;
                    case 3://create company
                        result = createCompany(session, request);
                        break;
                    case 4://delete user
                        result = isCompanyActivated(session, jobj) ? UserDelete(session, request) : getMessage(2, 99);
                        break;
                    case 5://assign Role
                        result = isCompanyActivated(session, jobj) ? assignRole(session, request) : getMessage(2, 99);
                        break;
                    case 6://Activate user
                        result = isCompanyActivated(session, jobj) ? ActivateDeactivateUser(session, request,action) : getMessage(2, 99);
                        break;
                    case 7://DeActivate user
                        result = isCompanyActivated(session, jobj) ? ActivateDeactivateUser(session, request,action) : getMessage(2, 99);
                        break;
                    case 8:
                        result = isCompanyActivated(session, jobj) ? updateCompany(session, request) : getMessage(2, 99);
                        break;
                    case 9:
                        result = isCompanyActivated(session, jobj) ? getUpdates(session, request) : getMessage(2, 99);
                        break;
                    case 10:
                        result = isCompanyActivated(session, jobj) ? editUser(session, request) : getMessage(2, 99);
                        break;
                    case 11:
                        result = isCompanyActivated(session, jobj) ? getAccProduct(session, request) : getMessage(2, 99);
                        break;
                    case 15:
                        result = deleteCompany(session, jobj);
                        break;
                    case 16:
                        result = deactivateCompany(session, request);
                        break;
                }
                 if(isCommit && validkey.equals(remoteapikey)){
                    session.getTransaction().commit();
                }
                if(isTestMode) {
                    result = result.substring(0, (result.length() - 1));
                    result += ",\"action\": " + Integer.toString(action) + "}";
                //                    result = "{success: true, action:" + Integer.toString(action) + ",data:" + getMessage(2, 2) + "}";
                }
            }catch(JSONException e){
                result = getMessage(2, 2);//"{\"success\": false, \"errorcode\": \"e02\"}";
                if(isCommit) {
                    result += ",\"action\": " + Integer.toString(action) + "}";
    //                    result = "{success: false, action:" + Integer.toString(action) + ",data:" + getMessage(2, 2) + "}";
                }
                session.getTransaction().rollback();
            } catch(ServiceException e){
                result = getMessage(2, 2);//"{\"success\": false, \"errorcode\": \"e02\"}";
                if(isCommit){
                    result += ",\"action\": " + Integer.toString(action) + "}";
    //                    result = "{success: false, action:" + Integer.toString(action) + ",data:" + getMessage(2, 2) + "}";
                }
                session.getTransaction().rollback();
            }  catch (Exception e) {
                result = getMessage(2, 2);
                session.getTransaction().rollback();
            } finally {
                HibernateUtil.closeSession(session);
                out.print(result);
            }
        } else {
            out.println(getMessage(2, 1));
        }
    }
    private static String assignRole (Session session, HttpServletRequest request) throws  SQLException,   ServiceException {
        String result ="";
        try {
            JSONObject jobj = new JSONObject(request.getParameter("data"));
            String userid = jobj.getString("userid");
            String roleStr = jobj.getString("role");
            String query = "";
            String msgStr = "";
            query="from UserLogin u where u.userID=?";
            List list = HibernateUtil.executeQuery(session, query, userid);
            int count = list.size();
            if(count > 0) {
                User user = (User) session.get(User.class,userid);

                String roleid = roleStr.equals("a0")?Role.COMPANY_ADMIN:(roleStr.equals("a1")? Role.COMPANY_ADMIN: Role.COMPANY_USER);
                user.setRoleID(roleid);
                user.setDeleteflag(0);

                String Hql = "from RoleUserMapping where userId.userID=? ";
                List ll = HibernateUtil.executeQuery(session, Hql, userid);
                Iterator itr = ll.iterator();
                if(itr.hasNext()) {
                    RoleUserMapping rmapping = (RoleUserMapping) itr.next();
                    rmapping.setRoleId((Rolelist)session.get(Rolelist.class, roleid));
                    session.update(rmapping);
                }

                if(roleStr.equals("a0")){
                	query="select company.companyID from User u where u.userID=?";
                    List result1 = HibernateUtil.executeQuery(session, query, userid);
                    if(result1 != null && result1.size() > 0){
                    	query = "update Company set creator.userID=? where companyID=?";
                    	HibernateUtil.executeUpdate(session, query, new Object[]{userid, result1.get(0)});
                    }
                }

                session.update(user);
                result = getMessage(1,8);
            } else {
                result = getMessage(2,6);
            }
        } catch (Exception e) {
            result = "{\"success\":false}";
            throw ServiceException.FAILURE("comapanyServlet.CompanyidExits:"+e.getMessage(), e);
        }
        return result;
    }
    public static String CompanyidExits(Session session, HttpServletRequest request) throws SQLException, ServiceException {
        String result = "{\"success\":false}";
        try {
            JSONObject jobj = new JSONObject(request.getParameter("data"));
            String companyid = "";
            if(!jobj.isNull("companyid")) {
                companyid = jobj.getString("companyid");
            } else {
                return getMessage(2,1);
            }
            String query="from Company c where c.companyID= ?";
            List list = HibernateUtil.executeQuery(session, query, companyid);
            int count = list.size();
            if(count > 0) {
                result = getMessage(1,1);
            } else {
                result = getMessage(1,2);
            }
        } catch (Exception e) {
            result = "{\"success\":false}";
            throw ServiceException.FAILURE("comapanyServlet.CompanyidExits:"+e.getMessage(), e);
        }
        return result;
    }

//    public static String CompanyDelete(Session session, HttpServletRequest request) throws SQLException, ServiceException {
//        String result = "{\"success\":false}";
//        try {
//            JSONObject jobj = new JSONObject(request.getParameter("data"));
//            String companyid = jobj.getString("companyid");
//            if(!StringUtil.isNullOrEmpty(companyid)) {
//                Company company=(Company)session.get(Company.class, companyid);
//                company.setDeleted(true);
//                session.update(company);
//                result = "{\"success\":true, 'msg': 'Company deleted successfully.'}";
//            } else {
//                result = "{\"success\":false, 'msg': 'Companyid could not be empty.'}";
//            }
//        } catch (Exception e) {
//            result = "{\"success\":false, 'msg': 'Following error occured while deleting company : '"+e.getMessage()+"}";
//            throw ServiceException.FAILURE("comapanyServlet.CompanyDelete:"+e.getMessage(), e);
//        }
//        return result;
//    }

    public static String UserExits(Session session, HttpServletRequest request) throws SQLException, ServiceException {
        String result = "{\"success\":false}";
        try {
            String userid = "";
            boolean flag = false;
            JSONObject jobj = new JSONObject(request.getParameter("data"));
            if(jobj.has("userid")) {
                userid = jobj.getString("userid");
            } else if(jobj.has("username")) {
                userid = jobj.getString("username");
                flag = true;
            }
            if(StringUtil.isNullOrEmpty(userid)){
                return getMessage(2,1);
            }
            String query = "";
            if(!flag) {
                query="from UserLogin u where u.userID=?";
            } else {
                query="from UserLogin u where u.userName=?";
            }
            List list = HibernateUtil.executeQuery(session, query, userid);
            int count = list.size();
            if(count > 0) {
                result = getMessage(1,3);
            } else {
                result = getMessage(1,4);
            }
        } catch (Exception e) {
            result = "{\"success\":false}";
            throw ServiceException.FAILURE("comapanyServlet.UserExits", e);
        }
        return result;
    }

    public static String UserDelete(Session session, HttpServletRequest request) throws SQLException, ServiceException {
        String result = "{\"success\":false}";
        try {
            User getuser=new User();
            String userid = "";
            boolean flag = false;
            String query = "";
            JSONObject jobj = new JSONObject(request.getParameter("data"));
            if(jobj.has("userid")) {
                userid = jobj.getString("userid");
            } else if(jobj.has("username")) {
                userid = jobj.getString("username");
                flag = true;
            }
            if(!flag) {
                String[] uArr = userid.split(",");
                for(int i =0; i < uArr.length; i++){
                    User u = (User)session.get(User.class, uArr[i]);
                    if(u!=null) {
                        u.setDeleteflag(1);
                        UserLogin ul=u.getUserLogin();
                        ul.setUserName(ul.getUserName()+"_del");
                        session.save(ul);
                        session.save(u);
                        result = getMessage(1,7);
                    }else{
                        result = getMessage(2,6);
                    }
                }
            }
//            else
//            {
//                query="from UserLogin u where u.userName=?";
//                List ls = HibernateUtil.executeQuery(session, query, userid);
//                Iterator ite = ls.iterator();
//                if(ite.hasNext()) {
//                    UserLogin userObj = (UserLogin) ite.next();
//                    userid = userObj.getUserID();
//                } else {
//                    return getMessage(2, 6);
//                }
//            }
            if(StringUtil.isNullOrEmpty(userid)) {
                return getMessage(2,1);
            }

//            getuser=(User)session.get(User.class,userid);
//            getuser.setIsDelete(true);
//            //getuser.getUserLogin().setUserName(getuser.getUserLogin().getUserName()+"-del");
//            session.saveOrUpdate(getuser);
//            result = getMessage(1,7);
         } catch (Exception e) {
            result = "{\"success\":false, \"errormsg\": \"Following error occured while deleting user : \""+e.getMessage()+"}";
            throw ServiceException.FAILURE("comapanyServlet.CompanyDelete:"+e.getMessage(), e);
        }
        return result;
    }
    public static String ActivateDeactivateUser(Session session, HttpServletRequest request,int action) throws SQLException, ServiceException {
        String result = "{\"success\":false}";
        try {
            User getuser=new User();
            String userid = "";
            boolean flag = false;
            String query = "";
            JSONObject jobj = new JSONObject(request.getParameter("data"));
            if(jobj.has("userid")) {
                userid = jobj.getString("userid");
            } else if(jobj.has("username")) {
                userid = jobj.getString("username");
                flag = true;
            }
            if (flag) {
                query = "from UserLogin u where u.userName=?";
                List ls = HibernateUtil.executeQuery(session, query, userid);
                Iterator ite = ls.iterator();
                if (ite.hasNext()) {
                    UserLogin userObj = (UserLogin) ite.next();
                    userid = userObj.getUserID();
                } else {
                    return getMessage(2, 6);
                }
            } else {
                String[] uids = userid.split(",");

                for (int i = 0; i < uids.length; i++) {
                    getuser = (User) session.get(User.class, uids[i]);
                    if (getuser != null) {
                        if (action == 6) {
                            getuser.setDeleteflag(0);
                            result = getMessage(1, 9);
                        } else {
                            getuser.setDeleteflag(1);
                            result = getMessage(1, 10);
                        }
                        //getuser.getUserLogin().setUserName(getuser.getUserLogin().getUserName()+"-Deleted");
                        session.saveOrUpdate(getuser);
                    }else{
                         result = getMessage(2,6);
                    }
                }

            }
            if(StringUtil.isNullOrEmpty(userid)) {
                return getMessage(2,1);
            }
        } catch (Exception e) {
            result = "{\"success\":false, \"errormsg\": \"Following error occured while deleting user : \""+e.getMessage()+"}";
            throw ServiceException.FAILURE("comapanyServlet.CompanyDelete:"+e.getMessage(), e);
        }
         return result;
    }
 
    private static String createUser (Session session, HttpServletRequest request) throws  SQLException,   ServiceException {
        String result = "{\"success\":false}";
        try {
            String pwdText = "";
            JSONObject jobj = new JSONObject(request.getParameter("data"));
            String userid = jobj.isNull("userid")?"" : jobj.getString("userid");
            String companyid = jobj.isNull("companyid")?"" : jobj.getString("companyid");
            String username = jobj.isNull("username")?"":jobj.getString("username");
            String pwd = jobj.isNull("password")?"":jobj.getString("password");
            String fname = jobj.isNull("fname")?"":jobj.getString("fname");
            String lname = jobj.isNull("lname")?"":jobj.getString("lname");
            String emailid = jobj.isNull("emailid")?"":jobj.getString("emailid");
            
            if(StringUtil.isNullOrEmpty(companyid) || StringUtil.isNullOrEmpty(username) || StringUtil.isNullOrEmpty(fname) || 
                    StringUtil.isNullOrEmpty(lname) || StringUtil.isNullOrEmpty(emailid)) {
                return getMessage(2,1);
            }
            //Company id Check(Company exist or not)
            String hql ="from Company where companyID=?";
            if(HibernateUtil.executeQuery(session, hql, new Object[]{companyid}).isEmpty()) {
                return getMessage(2,4);
            }
            //Email ID Check;
            /*String hql1 ="from User u where u.emailID=? and u.company.companyID=?";
            List list = HibernateUtil.executeQuery(session, hql1, new Object[]{emailid,companyid});
            if(list.size()>0) {
                return getMessage(2,9);
            }*/
            ///////////////////////////
            //UserID Check
            UserLogin userLogin;
            User user;
            try{ hql="";
                 hql ="from User where userID=?";
                 List usl=HibernateUtil.executeQuery(session, hql, new Object[]{userid});
                //user=(User)session.get(User.class, userid);
                if(usl.size()>0) return getMessage(2, 7);
            }catch(Exception e){
            }
            user = new User();

            userLogin=new UserLogin();
            user.setUserID(userid);
           // userLogin.setUserID(userid);
//            user.setCreatedon(new Date());
//            user.setUpdatedon(new Date());
          //  user.setUserLogin(userLogin);
            userLogin.setUser(user);
//            pwd=AuthHandler.generateNewPassword();
//            userLogin.setPassword(AuthHandler.getSHA1(pwd));
//            user.setCompany((Company)session.get(Company.class,AuthHandler.getCompanyid(request)));

            if(jobj.isNull("password")) {
               pwdText = AuthHandler.generateNewPassword(); 
               pwd = AuthHandler.getSHA1(pwdText);
            }
            userLogin.setPassword(pwd);
            user.setCompany((Company)session.get(Company.class,companyid));
            
            //Username Check
            String q="from User where userLogin.userName=? and company.companyID=?";////
            if(HibernateUtil.executeQuery(session, q, new Object[]{username,companyid}).isEmpty()==false
                    &&username.equals(userLogin.getUserName())==false)
//                throw new Exception("User name not available");
            {
                 result = getMessage(2, 3);
                 return result;
            }
            userLogin.setUserName(username);
            user.setFirstName(fname);
            user.setLastName(lname);
            user.setEmailID(emailid);
            user.setAddress("");
            user.setContactNumber("");
            user.setRoleID(Role.COMPANY_USER);
            user.setDateFormat((KWLDateFormat)session.get(KWLDateFormat.class, "18"));

            RoleUserMapping rmapping = new RoleUserMapping();
            rmapping.setRoleId((Rolelist)session.get(Rolelist.class, Role.COMPANY_USER));
            rmapping.setUserId(user);
            session.save(rmapping);

            session.saveOrUpdate(user);
            session.saveOrUpdate(userLogin);
            String diff=null;
            updatePreferences(request, null, (jobj.isNull("formatid")?null:jobj.getString("formatid")), (jobj.isNull("tzid")?null:jobj.getString("tzid")),diff);
            if(jobj.has("sendmail") && jobj.getBoolean("sendmail")) {
                Company companyObj = (Company) session.get(Company.class,companyid);
                User creater = (User)(companyObj.getCreator());
                String fullnameCreator = AuthHandler.getFullName(creater);
                String uri = URLUtil.getPageURL(request, Links.loginpageFull);
                String passwordString = "";
                if(jobj.isNull("password")) {
                    passwordString = "\n\nUsername: "+username+" \nPassword: "+pwdText;
                }
                String msgMailInvite = "Hi %s,\n\n%s has created an account for you at Deskera Accounting.\n\nDeskera Accounting is an Account Management Tool which you'll love using."+passwordString+"\n\nYou can log in at:\n%s\n\n\nSee you on Deskera Accounting\n\n - %s and The Deskera Acconting Team";
                String pmsg = String.format(msgMailInvite,user.getFirstName(),fullnameCreator, uri,fullnameCreator);
                if(jobj.isNull("password")) {
                    passwordString = "		<p>Username: <strong>%s</strong> </p>"
                                + "               <p>Password: <strong>%s</strong></p>";
                }
                String msgMailInviteUsernamePassword = "<html><head><title>Deskera Accounting - Your Deskera Account</title></head><style type='text/css'>"
                                + "a:link, a:visited, a:active {\n"
                                + " 	color: #03C;"
                                + "}\n"
                                + "body {\n"
                                + "	font-family: Arial, Helvetica, sans-serif;"
                                + "	color: #000;"
                                + "	font-size: 13px;"
                                + "}\n"
                                + "</style><body>"
                                + "	<div>"
                                + "		<p>Hi <strong>%s</strong>,</p>"
                                + "		<p>%s has created an account for you at %s.</p>"
                                + "             <p>Deskera Accounting is an Account Management Tool which you'll love using.</p>"
                                + passwordString
                                + "		<p>You can log in to Deskera Acconting at: <a href=%s>%s</a>.</p>"
                                + "		<br/><p>See you on Deskera Accounting!</p><p> - %s and The Deskera Accounting Team</p>"
                                + "	</div></body></html>";
                String htmlmsg = String.format(msgMailInviteUsernamePassword, user.getFirstName(),fullnameCreator, companyObj.getCompanyName(), uri,uri,fullnameCreator);
                try {
                    SendMailHandler.postMail(new String[] { user.getEmailID() },"[Deskera] Welcome to Deskera Accounting", htmlmsg, pmsg, creater.getEmailID());
                } catch (MessagingException e) {
                    e.printStackTrace();
                }
            }
            result = getMessage(1, 5);
        } catch (Exception e) {
//            result = "{\"success\":true, \"successmsg\": \"Following error occured while creating user : \""+e.getMessage()+"}";
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return result;
    }

     public static String getAccProduct(Session session, HttpServletRequest request) throws ServiceException {
        String r = getMessage(1, 11);//"{\"success\": true, \"infocode\": \"m07\"}";
        JSONObject obj = new JSONObject();
        try {
            JSONObject jobj = new JSONObject(request.getParameter("data"));
            String companyid =  jobj.getString("companyid");
            String query = "from Company c where companyID=?";
            List ll = HibernateUtil.executeQuery(session, query, new Object[]{companyid});
            int companyCount = ll.size();
            if(companyCount==0)
                return  "{\"success\": false, \"msg\": \"Company does not exist.\"}";
            obj = getProductTypes(session, request,obj);
            obj = getProducts(session,request,companyid,obj);//,start,limit);
            r = "{\"valid\": true, \"success\": true, \"data\":" + obj.toString() + "}";
        } catch (Exception e) {
            // Error Connecting to Server
            r = getMessage(2, 2);//"{\"success\": false, \"errorcode\": \"e02\"}";
            Logger.getLogger(remoteapi.class.getName()).log(Level.SEVERE, "Exception While Editing User", e);
            throw ServiceException.FAILURE(r, e);
        }
        return   r;
    }

    public static JSONObject getProducts(Session session, HttpServletRequest request, String companyID,JSONObject jobj) throws ServiceException{
        String condition1="";
		try {
              ArrayList params=new ArrayList();
            params.add(companyID);

            String query="select p," +
                "(select pl1.price from PriceList pl1 where product.ID=p.ID and carryIn=true and applyDate in (select max(applyDate) as ld from PriceList where product.ID=pl1.product.ID and carryIn=pl1.carryIn group by product))," +
                "(select pl2.price from PriceList pl2 where product.ID=p.ID and carryIn=false and applyDate in (select max(applyDate) as ld from PriceList where product.ID=pl2.product.ID and carryIn=pl2.carryIn group by product)), " +
                "(select sum((case when carryIn=true then quantity else -quantity end)) from Inventory where deleted=false and product.ID=p.ID group by product.ID), " +
                "(select quantity from Inventory where deleted=false and product.ID=p.ID and newinv='T' group by product.ID), " +
                "(select pl1.price from PriceList pl1 where product.ID=p.ID and carryIn=true and applyDate in (select min(applyDate) as ld from PriceList where product.ID=pl1.product.ID and carryIn=pl1.carryIn group by product)), " +
                "(select pl1.price from PriceList pl1 where product.ID=p.ID and carryIn=false and applyDate in (select min(applyDate) as ld from PriceList where product.ID=pl1.product.ID and carryIn=pl1.carryIn group by product)), " +
                "(select pl2.price from PriceList pl2 where product.ID=p.ID and carryIn=false and applyDate in (select max(applyDate) as ld from PriceList where product.ID=pl2.product.ID and carryIn=pl2.carryIn "+condition1+" group by product)), " +
                "(select pl1.price from PriceList pl1 where product.ID=p.ID and carryIn=true and applyDate in (select max(applyDate) as ld from PriceList where product.ID=pl1.product.ID and carryIn=pl1.carryIn "+condition1+" group by product)), " +
                "(select min(updateDate) from Inventory where deleted=false and product.ID=p.ID group by product) " +
                 "from Product p where p.deleted=false and p.company.companyID=? and p.syncable='T' order by p.producttype, p.name ";
            List list = HibernateUtil.executeQuery(session, query,params.toArray());
            int count = list.size();
//            if(StringUtil.isNullOrEmpty(start)==false&&StringUtil.isNullOrEmpty(limit)==false)
//                list=HibernateUtil.executeQueryPaging(session, query,params.toArray(), new Integer[]{Integer.parseInt(start),Integer.parseInt(limit)});

            Iterator itr = list.iterator();
            JSONArray jArr=new JSONArray();
//                        int i=0;
            while(itr.hasNext()) {
//                i++;
//                if(i==5)break;
                Object[] row = (Object[]) itr.next();
                Product product = (Product) row[0];

                JSONObject obj = new JSONObject();
                obj.put("productid", product.getID());
                obj.put("productname", product.getName());
                obj.put("description", StringUtil.isNullOrEmpty(product.getDescription())?"":product.getDescription());//desc
                obj.put("vendor", (product.getVendor()!=null?product.getVendor().getID():""));
                obj.put("vendornameid", (product.getVendor()!=null?product.getVendor().getName():""));
                obj.put("vendorphoneno", (product.getVendor()!=null?product.getVendor().getContactNumber():""));
                obj.put("vendoremail", (product.getVendor()!=null?product.getVendor().getEmail():""));
                obj.put("type", (product.getProducttype()!=null?product.getProducttype().getID():""));
                obj.put("purchaseprice", row[1]==null?0:row[1]);//purchaseprice
                obj.put("saleprice",row[2]);
              //  SimpleDateFormat sdf=new SimpleDateFormat("MMMM d, yyyy hh:mm:ss aa");
              //  DateFormat sdf= AuthHandler.getDateFormatter(request);
              // sdf.setTimeZone(TimeZone.getTimeZone("GMT"+company.getTimeZone().getDifference()));//"GMT"+AuthHandler.getTimeZoneDifference(request)));
              //  obj.put("createdon", (row[9]==null?"":sdf.format(row[9])));
                jArr.put(obj);
            }
            jobj.put("productdata", jArr);
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return jobj;
    }

    public static JSONObject getProductTypes(Session session, HttpServletRequest request,JSONObject obj) throws ServiceException {
        String r = getMessage(1, 11);//"{\"success\": true, \"infocode\": \"m07\"}";
        try {
            String query="from Producttype";
            List list = HibernateUtil.executeQuery(session, query);
            Iterator itr = list.iterator();
            JSONArray jArr=new JSONArray();
            while(itr.hasNext()) {
                Producttype ptype = (Producttype) itr.next();
                JSONObject jobj = new JSONObject();
                jobj.put("id", ptype.getID());
                jobj.put("name", ptype.getName());
                jArr.put(jobj);
            }
            obj.put("typedata", jArr);
        } catch (Exception e) {
            // Error Connecting to Server
            r = getMessage(2, 2);//"{\"success\": false, \"errorcode\": \"e02\"}";
            Logger.getLogger(remoteapi.class.getName()).log(Level. SEVERE, "Exception While Editing User", e);
            throw ServiceException.FAILURE(r, e);
        }
        return   obj;
    }
 
    static void updatePreferences(HttpServletRequest request, String currencyid, String dateformatid, String timezoneid,String tzdiff) {
        if(currencyid!=null)request.getSession().setAttribute("currencyid", currencyid);
        if(timezoneid!=null){
            request.getSession().setAttribute("timezoneid", timezoneid);
            request.getSession().setAttribute("tzdiff", tzdiff);
        }
        if(dateformatid!=null)request.getSession().setAttribute("dateformatid", dateformatid);
    }

    private static String createCompany (Session session, HttpServletRequest request) throws SQLException, ServiceException {
        String result = "{\"success\":false}";
        try {
            JSONObject jobj = new JSONObject(request.getParameter("data"));
            
            String companyid = jobj.isNull("companyid")?"":jobj.getString("companyid");
            String lname = jobj.isNull("lname")?"":jobj.getString("lname");
            String userid = jobj.isNull("userid")?"":jobj.getString("userid");
            String subdomain = jobj.isNull("subdomain")?"":jobj.getString("subdomain");
            String userid2 = jobj.isNull("username")?"":jobj.getString("username");
            String emailid2 = jobj.isNull("emailid")?"":jobj.getString("emailid");
            String password = jobj.isNull("password")?"":jobj.getString("password");
            String companyname = jobj.isNull("companyname")?"":jobj.getString("companyname");
            String fname = jobj.isNull("fname")?"":jobj.getString("fname");
            if(StringUtil.isNullOrEmpty(companyname) || StringUtil.isNullOrEmpty(userid2) ||  
                    StringUtil.isNullOrEmpty(fname) || StringUtil.isNullOrEmpty(emailid2)) {
                return getMessage(2,1);
            }
            String pwdtext = "";
            if(jobj.isNull("password")) {
               pwdtext = AuthHandler.generateNewPassword(); 
               password = AuthHandler.getSHA1(pwdtext);
            }
            if (!(StringUtil.isNullOrEmpty(userid2) || StringUtil.isNullOrEmpty(emailid2))) {
                emailid2 = emailid2.replace(" ", "+");
                result = signupCompany(session, request, companyid, userid, userid2, password, emailid2, companyname,fname,subdomain,lname);
                if(result.equals("success")) {
                    result = getMessage(1, 6);
                } else {
                    if(result.equals("failure")) {
                        result = getMessage(2, 8);
                    }
                }
            }
        }catch (Exception e) {
            result = "{\"success\":false}";
            throw ServiceException.FAILURE("comapanyServlet.createCompany:"+e.getMessage(), e);
        }
        return result;
    }

    public static String signupCompany(Session session, HttpServletRequest request,String companyid, String userid, String id, String password, String emailid, String companyname,
            String fname, String subdomain, String lname)
            throws ServiceException {
            String result = "failure";

        try {
            Company company=null;
            UserLogin userLogin=null;
            User user=null;
            KWLCurrency currency = (KWLCurrency)session.get(KWLCurrency.class, StorageHandler.getDefaultCurrencyID());
            String query="from Company c where c.subDomain= ?";
            List lst = HibernateUtil.executeQuery(session, query, subdomain);
            Iterator itr11 = lst.iterator();
            if(itr11.hasNext()) {
                Company oldcompany = (Company) itr11.next();
                oldcompany.setSubDomain("old_"+oldcompany.getSubDomain());
                session.saveOrUpdate(oldcompany);
//                return getMessage(2, 8);
            }

            if(!StringUtil.isNullOrEmpty(userid)){
                user=(User)session.get(User.class, userid);
                if(user!=null)
                    return getMessage(2, 7);
            }

            company=(Company)session.get(Company.class, companyid);
                if(company!=null)
                    return getMessage(2, 8);

            company=new Company();
            user = new User();
            userLogin=new UserLogin();
            user.setUserID(userid);
            
            company.setCompanyID(companyid);
            company.setCreator(user);
            company.setAddress("");
            company.setDeleted(0);
            Date curdate=new Date();
            company.setCreatedOn(curdate);
            company.setModifiedOn(curdate);
            company.setSubDomain(subdomain);
            company.setCompanyName(companyname);
            company.setCountry((Country)session.get(Country.class,"244"));
            company.setTimeZone((KWLTimeZone)session.get(KWLTimeZone.class, "23"));
            company.setEmailID(emailid);
            company.setCurrency(currency);
            company.setActivated(true);
            userLogin.setUser(user);
            user.setRoleID(Role.COMPANY_ADMIN);

            RoleUserMapping rmapping = new RoleUserMapping();
            rmapping.setRoleId((Rolelist)session.get(Rolelist.class, Role.COMPANY_ADMIN));
            rmapping.setUserId(user);
            session.save(rmapping);

            userLogin.setUserName(id);
            userLogin.setPassword(password);
            user.setFirstName(fname);
            user.setLastName(lname);
            user.setEmailID(emailid);
            user.setAddress("");

            user.setCompany(company);

            session.save(company);
            session.save(user);
            session.save(userLogin);
            NewCompanyHandler.setupNewCompany(session, request, company, user);
            result="success";
        } catch (Exception e) {
            result = "failure";
                throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return result;
    }

    public static String updateCompany (Session session, HttpServletRequest request) throws JSONException,ServiceException {
        String result = "{\"success\":false}";
        try {
            JSONObject jobj = new JSONObject(request.getParameter("data"));
            String companyid = jobj.isNull("companyid")?"" : jobj.getString("companyid");
            String subdomain = jobj.isNull("subdomain")?"" : jobj.getString("subdomain");
            String companyname = jobj.isNull("companyname")?"":jobj.getString("companyname");
            String address = jobj.isNull("address")?"":jobj.getString("address");
            String city = jobj.isNull("city")?"":jobj.getString("city");
            String state = jobj.isNull("state")?"":jobj.getString("state");
            String phone = jobj.isNull("phone")?"":jobj.getString("phone");
            String fax = jobj.isNull("fax")?"":jobj.getString("fax");
            String zip = jobj.isNull("zip")?"":jobj.getString("zip");
            String website = jobj.isNull("website")?"":jobj.getString("website");
            String emailid = jobj.isNull("emailid")?"":jobj.getString("emailid");
            String currency = jobj.isNull("currency")?"":jobj.getString("currency");
            String country = jobj.isNull("country")?"":jobj.getString("country");
            String timezone = jobj.isNull("timezone")?"":jobj.getString("timezone");
            String image = jobj.isNull("image")?"":jobj.getString("image");

            if(StringUtil.isNullOrEmpty(companyid) || StringUtil.isNullOrEmpty(subdomain) || StringUtil.isNullOrEmpty(currency) ||
                    StringUtil.isNullOrEmpty(timezone) || StringUtil.isNullOrEmpty(country) || StringUtil.isNullOrEmpty(companyname)) {
                return getMessage(2, 1);
            }

            Company company=(Company)session.get(Company.class, companyid);
            if(company==null)
                return getMessage(2, 4);

            String query="from Company where subDomain = ? and companyID <> ?";
            List list=HibernateUtil.executeQuery(session, query, new Object[]{subdomain,company.getCompanyID()});
            if(!list.isEmpty()){
                return getMessage(2, 10);
            }
            
            CompanyAccountPreferences companyAccountPreferences = (CompanyAccountPreferences) session.get(CompanyAccountPreferences.class, companyid);
            
            company.setCompanyName(companyname);
            company.setSubDomain(subdomain);
            if(!companyAccountPreferences.isCountryChange())
            	company.setCountry((Country)session.get(Country.class, country));
            if(!companyAccountPreferences.isCurrencyChange())
            	company.setCurrency((KWLCurrency)session.get(KWLCurrency.class, currency));
            company.setTimeZone((KWLTimeZone)session.get(KWLTimeZone.class, timezone));
            company.setCompanyLogo(image);
            company.setAddress(address);
            company.setCity(city);
            company.setState(state);
            company.setPhoneNumber(phone);
            company.setFaxNumber(fax);
            company.setZipCode(zip);
            company.setWebsite(website);
            company.setEmailID(emailid);

            company.setModifiedOn(new Date());

            session.update(company);
            result=getMessage(1, 11);
        }catch(NullPointerException e){
            ServiceException.FAILURE("remoteapi.updateCompany", e);
        }
        return result;
    }
    
    public static String getUpdates(Session session, HttpServletRequest request) throws ServiceException{
        String result = "{\"success\":false}";
        try {
            JSONObject jOutput = new JSONObject();
            JSONObject jData = new JSONObject();
            JSONObject jobj = new JSONObject(request.getParameter("data"));
            String companyid = jobj.isNull("companyid") ? "" : jobj.getString("companyid");
            String userid = jobj.isNull("userid") ? "" : jobj.getString("userid");
            int offset = jobj.isNull("offset") ? 0 : jobj.getInt("offset");
            int limit = jobj.isNull("limit") ? 5 : jobj.getInt("limit");
            if(StringUtil.isNullOrEmpty(companyid) || StringUtil.isNullOrEmpty(userid)) {
                return getMessage(2, 1);
            }

            Company company=(Company)session.get(Company.class, companyid);
            if(company==null)
                return getMessage(2, 4);

            User user=(User)session.get(User.class, userid);
            if(user==null)
                return getMessage(2, 6);

            JSONArray jArr= getUpdatesArray(session, companyid, userid);
            jData.put("head", "<div style='padding:10px 0 10px 0;font-size:13px;font-weight:bold;color:#10559a;border-bottom:solid 1px #EEEEEE;'>Updates</div>");

            jOutput.append("data", jData);
            for(int i=offset;i<offset+limit&&i<jArr.length();i++){
                JSONObject temp = new JSONObject();
                temp.put("update",jArr.getString(i));
                jOutput.append("data", temp);
            }
            jOutput.append("count", jArr.length());

            result = "{\"valid\": true, \"success\": true, \"data\":" + jOutput.toString() + "}";
        } catch (HibernateException ex) {
            Logger.getLogger(remoteapi.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException je) {
            ServiceException.FAILURE("remoteapi.updateCompany", je);
        }finally{

        }
        return result;
    }

    private static JSONArray getUpdatesArray(Session session, String companyid, String userid) throws ServiceException, JSONException {
        JSONArray jArray=new JSONArray();
        ArrayList temp;
        User user=(User)session.get(User.class, userid);
        JSONObject perms=PermissionHandler.getPermissions(session, user.getUserID());

        temp=DashboardHandler.getVendorsUpdationInfo(session, companyid, perms,false);
        for(int i=0;i<temp.size();i++){
            jArray.put(temp.get(i));
        }
        temp=DashboardHandler.getCustomersUpdationInfo(session, companyid, perms,false);
        for(int i=0;i<temp.size();i++){
            jArray.put(temp.get(i));
        }
        temp=DashboardHandler.getProductsBelowROLInfo(session, companyid, perms,false);
        for(int i=0;i<temp.size();i++){
            jArray.put(temp.get(i));
        }
        ArrayList props=new ArrayList();
        props.add("color=#10559A");
        replaceTag(jArray,"a","font", props);
        return jArray;
    }

    private static void replaceTag(JSONArray jArr, String oldTag, String newTag, ArrayList properties) throws JSONException{
        for(int i=0;i<jArr.length();i++){
            String str=jArr.getString(i);
            str=str.replaceAll("<"+oldTag+" [^>]*>", "<"+newTag+(properties!=null?" "+DashboardHandler.joinArrayList(properties, " "):"")+">");
            str=str.replaceAll("</"+oldTag+">", "</"+newTag+">");
            jArr.put(i, str);
        }
    }

    public static String getMessage(int type, int mode){
        String r = "";
        String temp = "";
        switch(type){
            case 1:     // success messages
                temp = "m" + String.format("%02d", mode);
                r = "{\"success\": true, \"infocode\": \"" + temp + "\"}";
                break;
            case 2:     // error messages
                temp = "e" + String.format("%02d", mode);

                r = "{\"success\": false, \"errorcode\": \"" + temp + "\"}";
                break;
        }
        return r;
    }

    public static String editUser(Session session, HttpServletRequest request) throws ServiceException {
        String r = getMessage(1, 11);//"{\"success\": true, \"infocode\": \"m07\"}";
        try {
            JSONObject jobj = new JSONObject(request.getParameter("data"));
            String userid = "";
            boolean flag = false;
            if (jobj.has("userid")) {
                userid = StringUtil.serverHTMLStripper(jobj.get("userid").toString());
            } else {
                flag = true;
                r = getMessage(2, 1);//"{\"success\": false, \"errorcode\": \"e01\"}";
            }
            if (!flag) {
                String emailid = jobj.has("emailid")?jobj.getString("emailid").trim().replace(" ", "+"):"";
                String fname = jobj.has("fname")?StringUtil.serverHTMLStripper(jobj.get("fname").toString()):"";
                String lname = jobj.has("lname")?StringUtil.serverHTMLStripper(jobj.get("lname").toString()):"";
                emailid = jobj.has("emailid")?StringUtil.serverHTMLStripper(emailid):"";
                String contactno = jobj.has("contactno")?StringUtil.serverHTMLStripper(jobj.get("contactno").toString()):"";
                String address = jobj.has("address")?StringUtil.serverHTMLStripper(jobj.get("address").toString()):"";

                String timezone = jobj.has("timezone")?StringUtil.serverHTMLStripper(jobj.get("timezone").toString()):"";
                KWLTimeZone ktz = (KWLTimeZone)session.get(KWLTimeZone.class, timezone);

                User u = (User)session.get(User.class, userid);
                if(u!=null) {
                    u.setFirstName(fname);
                    u.setAddress(address);
                    u.setLastName(lname);
                    u.setEmailID(emailid);
                    u.setContactNumber(contactno);
                    u.setTimeZone(ktz);
                    session.save(u);
                } else {
                    r = getMessage(2, 6);
                }
            }
        } catch (JSONException e) {
            // Error Connecting to Server
            r = getMessage(2, 2);//"{\"success\": false, \"errorcode\": \"e02\"}";
            Logger.getLogger(remoteapi.class.getName()).log(Level.SEVERE, "JSON Exception While Editing User", e);
            throw ServiceException.FAILURE(r, e);
        } catch (Exception e) {
            // Error Connecting to Server
            r = getMessage(2, 2);//"{\"success\": false, \"errorcode\": \"e02\"}";
            Logger.getLogger(remoteapi.class.getName()).log(Level.SEVERE, "Exception While Editing User", e);
            throw ServiceException.FAILURE(r, e);
        }
        return r;
    }    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    
    public static String deactivateCompany(Session session, HttpServletRequest request) throws ServiceException{
    	String result = getMessage(1, 16);
    	try{
    		JSONObject jobj = new JSONObject(request.getParameter("data"));
    		   String comp = isCompanyExists(session, jobj);
               JSONObject cj = new JSONObject(comp);
               if (cj.has("infocode") && cj.getString("infocode").equals("m01")) {

                    String companyID = jobj.getString("companyid");
                    Company company=(Company)session.get(Company.class, companyID);
                    if(company == null)
                    	return getMessage(2, 16);
                    else {
	                    company.setActivated(false);
	                    session.update(company);
                    }
               } else {
                   // Company doesn't exists or Insufficient Data
                   result = cj.toString();
               }
               
    	} catch (ServiceException ex) {
    		Logger.getLogger(remoteapi.class.getName()).log(Level.SEVERE, "Service Exception while deactivating company", ex);
            throw ServiceException.FAILURE(result, ex);
    	} catch (JSONException ex) {
    		Logger.getLogger(remoteapi.class.getName()).log(Level.SEVERE, "JSON Exception while deactivating company", ex);
            throw ServiceException.FAILURE(result, ex);
    	} 
    	
    	return result;
    }
    
    public static String isCompanyExists(Session session, JSONObject jobj) throws ServiceException{
        String r = getMessage(1, 2);//"{\"success\": true, \"infocode\": \"m02\"}";
        try{
            String sql = "";
            boolean flag = false;
            String param = "";
            if(jobj.has("companyid")){
                sql = "SELECT COUNT(companyID) AS count FROM Company WHERE companyID = ?";
                param = jobj.getString("companyid");
            } else if(jobj.has("subdomain")){
                sql = "SELECT COUNT(companyID) AS count FROM Company WHERE subDomain = ?";
                param = jobj.getString("subdomain");
            } else {
                flag = true;
                r = getMessage(2, 1);//"{\"success\": false, \"errorcode\": \"e01\"}";
            }
            if(!flag){
            	List list = HibernateUtil.executeQuery(session, sql, new Object[]{param});
                if(list.size() > 0){
                    r = getMessage(1, 1);//"{\"success\": true, \"message\": \"m01\"}";
                }
            }
        } catch(JSONException e){
            r = getMessage(2, 2);//"{\"success\": false, \"errorcode\": \"e02\"}";
            throw ServiceException.FAILURE(r, e);
        } catch(ServiceException e){
            r = getMessage(2, 2);//"{\"success\": false, \"errorcode\": \"e02\"}";
            throw ServiceException.FAILURE(r, e);
        }
        return r;
    }
    
    public static boolean isCompanyActivated(Session session, JSONObject jobj) throws ServiceException{
    	
    	boolean result = false;
    	
    	try{
    		
    		String comp = isCompanyExists(session, jobj);
            JSONObject cj = new JSONObject(comp);
            if (cj.has("infocode") && cj.getString("infocode").equals("m01")) {
            	
            	String sql = "SELECT activated FROM Company WHERE companyID = ?";
            	String param = jobj.getString("companyid");
            	List list = HibernateUtil.executeQuery(session, sql, new Object[]{param});
            	if(list.size() > 0 && list.get(0).equals(true) )
            		result = true;
            	
            }     		

    	} catch(JSONException e){
    		Logger.getLogger(remoteapi.class.getName()).log(Level.SEVERE, null, e);
    		throw ServiceException.FAILURE("JSON exception in isCompanyActivated()", e);
    	} catch(ServiceException e){
    		Logger.getLogger(remoteapi.class.getName()).log(Level.SEVERE, null, e);
    		throw ServiceException.FAILURE("Service exception in isCompanyActivated()", e);
    	}
    	
    	return result;
    	
    }
    
    public static String deleteCompany(Session session, JSONObject jobj) throws ServiceException{
    	String result = getMessage(1, 15);
    	
    	try{
    		
    		String comp = isCompanyExists(session, jobj);
            JSONObject cj = new JSONObject(comp);
            if (cj.has("infocode") && cj.getString("infocode").equals("m01")) {
            	String param = jobj.getString("companyid");
            	String[] queries = createQueryArray();
            	for(int i = 0; i < queries.length; i++){
            		HibernateUtil.executeSQLUpdate(session, queries[i], new Object[]{param});
            	}
            }     		
    	} catch(JSONException e){
    		Logger.getLogger(remoteapi.class.getName()).log(Level.SEVERE, null, e);
    		throw ServiceException.FAILURE("JSON exception in deleteCompany()", e);
    	} catch(ServiceException e){
    		Logger.getLogger(remoteapi.class.getName()).log(Level.SEVERE, null, e);
    		throw ServiceException.FAILURE("Service exception in deleteCompany()", e);
    	}
    	
    	return result;
    	
    }
    
    public static String[] createQueryArray() throws ServiceException{
    	String[] queries = {"delete from receiptdetails where company = ?",
    						"delete from receipt where company = ?",
    						"delete from cndiscount where company = ?",
    						"delete from cndetails where company = ?",
    						"delete from creditnote where company = ?",
    						"delete from invoicedetails where company = ?",
    						"update invoice set parentinvoice = null  where company = ?",
    						"delete from invoice where parentinvoice <> 'null' and  company = ?",
    						"delete from invoice where company = ?",
    						"delete from dndiscount where company = ?",
    						"delete from dndetails where company = ?",
    						"delete from debitnote where company = ?",
    						"delete from paymentdetail where company = ?",
    						"delete from payment where company = ?",
    						"delete from expenseggrdetails where company = ?",
    						"delete from grdetails where company = ?",
    						"delete from goodsreceipt where company = ?",
    						"delete from billingcndiscount where company = ?",
    						"delete from billingcndetails where company = ?",
    						"delete from billingcreditnote where company = ?",
    						"delete from billingreceiptdetails where company = ?",
    						"delete from billingreceipt where company = ?",
    						"delete from billinginvoicedetails where company = ?",
    						"update billinginvoice set parentinvoice = null  where company = ?",
    						"delete from billinginvoice where parentinvoice <> 'null' and  company = ?",
    						"delete from billinginvoice where company = ?",
    						"delete from billingdndiscount where company = ?",
    						"delete from billingdndetails where company = ?",
    						"delete from billingdebitnote where company = ?",
    						"delete from billingpaymentdetails where company = ?",
    						"delete from billingpayment where company = ?",
    						"delete from billingreceiptdetails where company = ?",
    						"delete from billingreceipt where company = ?",
    						"delete from billinggrdetails where company = ?",
    						"delete from billinggr where company = ?",
    						"delete from depriciationdetail where company = ?",
    						"delete from asset where company = ?",
    						"delete from bankreconciliationdetail where company = ?",
    						"delete from bankreconciliation where company = ?",
    						"delete from pbdetails where build in (select id from productbuild where company = ?)",
    						"delete from productbuild where company = ?",
    						"delete from jedetail where company = ?",
    						"delete from journalentry where company = ?",
    						"delete from sodetails where company = ?",
    						"delete from salesorder where company = ?",
    						"delete from podetails where company = ?",
    						"delete from purchaseorder where company = ?",
    						"delete from billingsodetails where company = ?",
    						"delete from billingsalesorder where company = ?",
    						"delete from billingpodetails where company = ?",
    						"delete from billingpurchaseorder where company = ?",
    						"delete from quotationdetails where company = ?",
    						"delete from quotation where company = ?",
    						"delete from discount where company = ?",
    						"delete from inventory where company = ?",
    						"delete from productcyclecount where product in (select id from product where company = ?)",
    						"delete from pricelist where product in (select id from product where company = ?)",
    						"delete from cyclecount where product in (select id from product where company = ?)",
    						"delete from productassembly where product in (select id from product where company = ?)",
    						"update product set parent = null  where company = ?",
    						"delete from product where company = ? and parent <> 'null'",
    						"delete from product where company = ?",
    						"update account set parent = null  where company = ?",
    						"delete from vendor where company = ?",
    						"delete from customer where company = ?",
    						"delete from taxlist where company = ?",
    						"delete from tax where company = ?",
    						"delete from paydetail where company = ?",
    						"delete from paymentmethod where company = ?",
    						"delete from compaccpreferences where id = ?",
    						"update account set depreciationaccont = null  where company = ?",
    						"delete from account where company = ?",
    						"delete from costcenter where company = ?",
    						"delete from accgroup where company = ?",
    						"delete from userlogin where userid in (select userid from users where company = ?)",
    						"update company set creator = null where companyid = ?",
    						"delete from userpermission where roleusermapping in (select id from role_user_mapping where userid in (select userid from users where company = ?))",
    						"delete from role_user_mapping where userid in (select userid from users where company = ?)",
    						"delete from role where company = ?",
    						"delete from rolelist where company = ?",
    						"delete from audittrail where user in (select userid from users where company = ?)",
    						"delete from pdfreporttemplate where user in (select userid from users where company = ?)",
    						"delete from users where company = ?",
    						"delete from uom where company = ?",
    						"delete from apiresponse where companyid = ?",
    						"delete from cheque where BankMasterItem in (select id from masteritem where company = ?)",
    						"delete from masteritem where company = ?",
    						"delete from exchangeratedetails where company = ?",
    						"delete from yearlock where company = ?",
    						"delete from creditterm where company = ?",
    						"delete from company where companyid = ?"};
    	
    	return queries;
    }

    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    } 

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
