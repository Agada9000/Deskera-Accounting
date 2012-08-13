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
package com.krawler.spring.accounting.handler;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.User;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.StringUtil;
import com.krawler.common.util.URLUtil;
import com.krawler.esp.handlers.APICallHandler;
import com.krawler.esp.handlers.StorageHandler;
import com.krawler.esp.hibernate.impl.HibernateUtil;
import com.krawler.esp.utils.ConfigReader;
import com.krawler.hql.accounting.CompanyAccountPreferences;
import com.krawler.hql.accounting.Customer;
import com.krawler.hql.accounting.Vendor;
import com.krawler.spring.accounting.customer.accCustomerDAO;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.accounting.product.productHandler;
import com.krawler.spring.accounting.vendor.accVendorDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.permissionHandler.permissionHandlerDAO;
import com.krawler.spring.permissionHandler.permissionHandler;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.hibernate.Session;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 *
 * @author krawler
 */
public class accDashboardController extends MultiActionController implements MessageSourceAware{
    private accCustomerDAO accCustomerDAOobj;
    private accVendorDAO accVendorDAOobj;
    private accProductDAO accProductObj;
    private permissionHandlerDAO permissionHandlerDAOObj;
    private sessionHandlerImpl sessionHandlerImplObj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private MessageSource messageSource;
    
    public void setaccCustomerDAO(accCustomerDAO accCustomerDAOobj) {
        this.accCustomerDAOobj = accCustomerDAOobj;
    }
    public void setaccVendorDAO(accVendorDAO accVendorDAOobj) {
        this.accVendorDAOobj = accVendorDAOobj;
    }
    public void setaccProductDAO(accProductDAO accProductObj) {
        this.accProductObj = accProductObj;
    }
    public void setpermissionHandlerDAO(permissionHandlerDAO permissionHandlerDAOObj1) {
        this.permissionHandlerDAOObj = permissionHandlerDAOObj1;
    }
    public void setsessionHandlerImpl(sessionHandlerImpl sessionHandlerImplObj1) {
        this.sessionHandlerImplObj = sessionHandlerImplObj1;
    }
    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }
    
    @Override
	public void setMessageSource(MessageSource msg) {
		this.messageSource = msg;
	}
    
    public ModelAndView getDashboardData(HttpServletRequest request, HttpServletResponse response) {
        String msg = "";
        try {
            JSONObject jbj = new JSONObject();
            msg = getDashboardData(request);
            boolean refresh = true;
            //  msg += "<link rel='alternate' type='application/rss+xml' title='RSS - Global RSS Feed' href=\""+com.krawler.common.util.URLUtil.getPageURL(request,"")+"feed.rss?m=global&u="+AuthHandler.getUserName(request)+"\">";
            /*Request param must be sent from atleast one case*/
            if (StringUtil.isNullOrEmpty(request.getParameter("refresh"))) {
                refresh = true;
            } else {
                refresh = Boolean.parseBoolean(request.getParameter("refresh"));
            }
            if (refresh) {
                jbj.put("valid", true);
                jbj.put("data", msg);
                msg = jbj.toString();
            }
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accDashboardController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accDashboardController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
        }
        return new ModelAndView("jsonView_ex", "model", msg);
    }

    public String getDashboardData(HttpServletRequest request) throws ServiceException, SessionExpiredException {
		StringBuilder data=new StringBuilder();
        try {
            String userid = sessionHandlerImplObj.getUserid(request);
            String companyid = sessionHandlerImplObj.getCompanyid(request);

            data.append("<div id=\"DashboardContent\" class=\"dashboardcontent\">");
//            String userid = AuthHandler.getUserid(request);
//            User user = (User) session.get(User.class, AuthHandler.getUserid(request));
            KwlReturnObject uresult = accountingHandlerDAOobj.getObject(User.class.getName(), userid);
            User user = (User) uresult.getEntityList().get(0);

//            JSONObject perms = PermissionHandler.getPermissions(session, user.getUserID());
            JSONObject perms = new JSONObject();

            if (!permissionHandlerDAOObj.isSuperAdmin(userid, companyid)) {
                KwlReturnObject kmsg = permissionHandlerDAOObj.getActivityFeature();
                perms = permissionHandler.getAllPermissionJson(kmsg.getEntityList(), request, kmsg.getRecordTotalCount());

                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put("userid", userid);

                kmsg = permissionHandlerDAOObj.getUserPermission(requestParams);
                perms = permissionHandler.getRolePermissionJson(kmsg.getEntityList(), perms);
            } else {
                perms.put("deskeraadmin", true);
            }


            if (AccountingManager.isCompanyAdmin(user)) {
//                getCompanyAdminDashboardData(session, request, data, perms);
                getCompanyAdminDashboardData(request, data, perms);
            } else {
//                getUserDashboardData(session, request, data, perms);
                getUserDashboardData(request, data, perms);
            }
            data.append("</div>");
        } catch (Exception ex) {
            throw ServiceException.FAILURE(""+ex.getMessage(), ex);
        }
        return data.toString();
    }

    private void getUserDashboardData(HttpServletRequest request, StringBuilder data, JSONObject perms) throws ServiceException, SessionExpiredException {
//        StringBuilder temp = getUserDashboardUpdateList(session, request, perms);
        StringBuilder temp = getUserDashboardUpdateList(request, perms);
//        CompanyAccountPreferences pref = (CompanyAccountPreferences) session.get(CompanyAccountPreferences.class, AuthHandler.getCompanyid(request));
        KwlReturnObject cpresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), sessionHandlerImpl.getCompanyid(request));
        CompanyAccountPreferences pref = (CompanyAccountPreferences) cpresult.getEntityList().get(0);
        StringBuilder temp2;
        if (pref != null) {
            if (!pref.isWithoutInventory()) {
//                temp2 = getDashBoardDataFlow(session, request, perms);
                temp2 = getDashBoardDataFlow(request, perms);
            } else {
//                temp2 = getDashBoardDataFlowWithoutInv(session, request, perms);
                temp2 = getDashBoardDataFlowWithoutInv(request, perms);
            }
        } else {
//            temp2 = getDashBoardDataFlow(session, request, perms);
            temp2 = getDashBoardDataFlow(request, perms);
        }
        if (temp.length() > 0) {
            data.append(createLeftPane(messageSource.getMessage("acc.dashboard.updates", null, RequestContextUtils.getLocale(request)), temp, temp2));
        } else {
            data.append(createLeftPane(messageSource.getMessage("acc.rem.93", null, RequestContextUtils.getLocale(request)), getSetupWizard(request, sessionHandlerImpl.getCompanyid(request)), temp2));
        }
    }

     private void getCompanyAdminDashboardData(HttpServletRequest request, StringBuilder data, JSONObject perms) throws ServiceException, SessionExpiredException {
//        StringBuilder temp = getCompanyAdminDashboardUpdateList(session, request, perms);
        StringBuilder temp = getCompanyAdminDashboardUpdateList(request, perms);
//        CompanyAccountPreferences pref = (CompanyAccountPreferences) session.get(CompanyAccountPreferences.class, AuthHandler.getCompanyid(request));
        String companyID = sessionHandlerImplObj.getCompanyid(request);

        KwlReturnObject cpresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyID);
        CompanyAccountPreferences pref = (CompanyAccountPreferences) cpresult.getEntityList().get(0);

        StringBuilder temp2;
        if (pref != null) {
            if (!pref.isWithoutInventory()) {
//                temp2 = getDashBoardDataFlow(session, request, perms);
                temp2 = getDashBoardDataFlow(request, perms);
            } else {
//                temp2 = getDashBoardDataFlowWithoutInv(session, request, perms);
                temp2 = getDashBoardDataFlowWithoutInv(request, perms);
            }
        } else {
//            temp2 = getDashBoardDataFlow(session, request, perms);
            temp2 = getDashBoardDataFlow(request, perms);
        }
        if (temp.length() > 0) {
            data.append(createLeftPane(messageSource.getMessage("acc.dashboard.updates", null, RequestContextUtils.getLocale(request)), temp, temp2));
        } else {
            data.append(createLeftPane(messageSource.getMessage("acc.rem.93", null, RequestContextUtils.getLocale(request)), getSetupWizard(request, companyID), temp2));
        }
    }

    private StringBuilder getCompanyAdminDashboardUpdateList(HttpServletRequest request, JSONObject perms) throws ServiceException {
        StringBuilder finalStr=new StringBuilder();
        try {
//            CompanyAccountPreferences pref=(CompanyAccountPreferences)session.get(CompanyAccountPreferences.class,AuthHandler.getCompanyid(request));
            String companyID = sessionHandlerImplObj.getCompanyid(request);
            KwlReturnObject cpresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyID);
            CompanyAccountPreferences pref = (CompanyAccountPreferences) cpresult.getEntityList().get(0);

//            finalStr.append(joinArrayList(getVendorsUpdationInfo(session, companyID, perms,true), ""));
            finalStr.append(joinArrayList(getVendorsUpdationInfo(companyID, perms,true), ""));
//            finalStr.append(joinArrayList(getCustomersUpdationInfo(session, companyID, perms,true), ""));
            finalStr.append(joinArrayList(getCustomersUpdationInfo(companyID, perms,true), ""));
            if(pref!=null){
              if(!pref.isWithoutInventory())
//                finalStr.append(joinArrayList(getProductsBelowROLInfo(session, companyID, perms,true), ""));
                finalStr.append(joinArrayList(getProductsBelowROLInfo(request, companyID, perms,true), ""));
            }else
//                finalStr.append(joinArrayList(getProductsBelowROLInfo(session, companyID, perms,true), ""));
                finalStr.append(joinArrayList(getProductsBelowROLInfo(request, companyID, perms,true), ""));
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return finalStr;
    }

    private StringBuilder getUserDashboardUpdateList(HttpServletRequest request, JSONObject perms) throws ServiceException {
        StringBuilder finalStr = new StringBuilder();
        try {
//            CompanyAccountPreferences pref = (CompanyAccountPreferences) session.get(CompanyAccountPreferences.class, AuthHandler.getCompanyid(request));
            String companyID = sessionHandlerImplObj.getCompanyid(request);
            KwlReturnObject cpresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyID);
            CompanyAccountPreferences pref = (CompanyAccountPreferences) cpresult.getEntityList().get(0);
//            finalStr.append(joinArrayList(getVendorsUpdationInfo(session, companyID, perms, true), ""));
            finalStr.append(joinArrayList(getVendorsUpdationInfo(companyID, perms, true), ""));
//            finalStr.append(joinArrayList(getCustomersUpdationInfo(session, companyID, perms, true), ""));
            finalStr.append(joinArrayList(getCustomersUpdationInfo(companyID, perms, true), ""));
            if (pref != null) {
                if (!pref.isWithoutInventory()) {
//                    finalStr.append(joinArrayList(getProductsBelowROLInfo(session, companyID, perms, true), ""));
                    finalStr.append(joinArrayList(getProductsBelowROLInfo(request, companyID, perms, true), ""));
                }
            } else {
//                finalStr.append(joinArrayList(getProductsBelowROLInfo(session, companyID, perms, true), ""));
                finalStr.append(joinArrayList(getProductsBelowROLInfo(request, companyID, perms, true), ""));
            }
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return finalStr;
    }

    public ArrayList getProductsBelowROLInfo(HttpServletRequest request, String companyID, JSONObject perms,Boolean isDashboard) throws ServiceException {
        ArrayList jArray=new ArrayList();
        try {
//            JSONArray jArr=CompanyHandler.getProducts(session,null,companyID).getJSONArray("data");
            HashMap<String, Object> requestParams = productHandler.getProductRequestMap(request);
            KwlReturnObject result = accProductObj.getSuggestedReorderProducts(requestParams);
            JSONArray jArr = productHandler.getProductsJson(request, result.getEntityList());
            
            String link;
            String productID;
            for(int i=0;i<jArr.length();i++){
                JSONObject obj = jArr.getJSONObject(i);
                link=obj.getString("productname");
                productID=obj.getString("productid");
                if(permissionHandler.isPermitted(perms, "product", "view"))
                    link=getLink(link, "callProductDetails(\""+productID+"\")");
                if(obj.getInt("quantity")==obj.getInt("reorderlevel")){
                    jArray.add(getFormatedAlert("The Product "+link+"'s stock is equal to reorder level (Available quantity:"+obj.getInt("quantity")+" "+obj.getString("uomname")+")","accountingbase updatemsg-product",isDashboard));
                } else {
                    jArray.add(getFormatedAlert("The Product "+link+" is below reorder level (Available quantity:"+obj.getInt("quantity")+" "+obj.getString("uomname")+")","accountingbase updatemsg-product",isDashboard));
                }
            }
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (JSONException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return jArray;
    }

    public ArrayList getVendorsUpdationInfo(String companyID, JSONObject perms, Boolean isDashboard) throws ServiceException {
        ArrayList jArray=new ArrayList();
        try {
//            String query="from Vendor where company.companyID=? and modifiedOn is null order by createdOn";
//            List list = HibernateUtil.executeQueryPaging(session, query, new Object[]{companyID}, new Integer[]{0,2});
            KwlReturnObject result = accVendorDAOobj.getVendor_Dashboard(companyID, true, "createdOn", 0, 2);
            List list = result.getEntityList();
            Iterator itr=list.iterator();
            String link;
            String vendorID="";
            while(itr.hasNext()){
                Vendor vendor=(Vendor)itr.next();
                link=vendor.getAccount().getName();
                vendorID=vendor.getAccount().getID();
                if(permissionHandler.isPermitted(perms, "vendor", "view"))
                    link=getLink(link, "callVendorDetails(\""+vendorID+"\")");
                jArray.add(getFormatedAlert("New vendor "+link+" created","accountingbase updatemsg-vendor",isDashboard));
            }
//            query="from Vendor where company.companyID=? and modifiedOn is not null order by modifiedOn";
//            list = HibernateUtil.executeQueryPaging(session, query, new Object[]{companyID}, new Integer[]{0,2});
            result = accVendorDAOobj.getVendor_Dashboard(companyID, false, "modifiedOn", 0, 2);
            list = result.getEntityList();
            itr=list.iterator();
            while(itr.hasNext()){
                Vendor vendor=(Vendor)itr.next();
                link=vendor.getAccount().getName();
                vendorID=vendor.getAccount().getID();
                if(permissionHandler.isPermitted(perms, "vendor", "view"))
                    link=getLink(link, "callVendorDetails(\""+vendorID+"\")");
                jArray.add(getFormatedAlert("Vendor "+link+" modified","accountingbase updatemsg-vendor",isDashboard));
            }
        } catch (JSONException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return jArray;
    }

    public ArrayList getCustomersUpdationInfo(String companyID, JSONObject perms, Boolean isDashboard) throws ServiceException {
        ArrayList jArray=new ArrayList();
        try {
//            String query="from Customer where company.companyID=? and modifiedOn is null order by createdOn";
//            List list = HibernateUtil.executeQueryPaging(session, query, new Object[]{companyID}, new Integer[]{0,2});
            KwlReturnObject result = accCustomerDAOobj.getCustomer_Dashboard(companyID, true, "createdOn", 0, 2);
            List list = result.getEntityList();
            Iterator itr=list.iterator();
            String link;
            String customerID;
            while(itr.hasNext()){
                Customer customer=(Customer)itr.next();
                customerID=customer.getID();
                link=customer.getAccount().getName();
                if(permissionHandler.isPermitted(perms, "customer", "view"))
                    link=getLink(link, "callCustomerDetails(\""+customerID+"\")");

                jArray.add(getFormatedAlert("New customer "+link+" created","accountingbase updatemsg-customer",isDashboard));
            }
//            query="from Customer where company.companyID=? and modifiedOn is not null order by modifiedOn";
//            list = HibernateUtil.executeQueryPaging(session, query, new Object[]{companyID}, new Integer[]{0,2});
            result = accCustomerDAOobj.getCustomer_Dashboard(companyID, false, "modifiedOn", 0, 2);
            itr=list.iterator();
            while(itr.hasNext()){
                Customer customer=(Customer)itr.next();
                customerID=customer.getID();
                link=customer.getAccount().getName();
                if(permissionHandler.isPermitted(perms, "customer", "view"))
                    link=getLink(link, "callCustomerDetails(\""+customerID+"\")");
                jArray.add(getFormatedAlert("Customer "+link+" modified","accountingbase updatemsg-customer",isDashboard));
            }
        } catch (JSONException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return jArray;
    }

    public String getContentDiv(String typeStr) {
        String div = "<div  class=\""+typeStr +" statusitemimg\"></div>";
        return div;
    }

    public String getContentSpan(String textStr,Boolean isDashboard){
        String upperspacing="";
        if(isDashboard)
            upperspacing="dashboardupdate";
        String span = "<span class=\"statusitemcontent "+upperspacing+"\">" +textStr + "</span><div class=\"statusclr\"></div>";
        return span;
    }

    public String getLink(String message, String functionName) {
        return "<a href=# onclick='"+functionName+"'>"+message+"</a>";
    }

    public String getLink(String message, String functionName, String toolTip) {
        return "<a href=# onclick='"+functionName+"' wtf:qtip='"+toolTip+"'>"+message+"</a>";
    }

    public String getFormatedAlert(String message, String cssClass,Boolean isDashboard) {
        String fmtMsg="";
        if(isDashboard)
            fmtMsg=getContentDiv(cssClass);
        fmtMsg+=message;
        return getContentSpan(fmtMsg,isDashboard);
    }

    public StringBuilder getSectionHeader(String headerText) {
        StringBuilder sb=new StringBuilder();
        sb.append("<div class=\"statuspanelheader\"><span class=\"statuspanelheadertext\">");
        sb.append(headerText);
        sb.append("</span></div>");
        return sb;
    }

    private StringBuilder createNewLink(String text, String functionName, String cssClass) {
        StringBuilder newLink=new StringBuilder();
        newLink.append("<li>");
        newLink.append(getLink(text, functionName));
        newLink.append("<ul class='").append(cssClass).append("'>");
        newLink.append("</ul>");
        newLink.append("</li>");
        return newLink;
    }

    private StringBuilder createNewLink(String text, String functionName, String cssClass, String toolTip) {
        StringBuilder newLink=new StringBuilder();
        newLink.append("<li>");
        newLink.append(getLink(text, functionName, toolTip));
        newLink.append("<ul class='").append(cssClass).append("'>");
        newLink.append("</ul>");
        newLink.append("</li>");
        return newLink;
    }

    private StringBuilder createNewLink(String text, String functionName) {
        return createNewLink(text, functionName, "leadlist");
    }

    private StringBuilder createSection(String title, String sectionid, StringBuilder innerData) {
        StringBuilder data=new StringBuilder();
        data.append(getSectionHeader(title));
        data.append("<ul id='").append(sectionid).append("'>");
        data.append(innerData);
        data.append("</ul>");
        data.append("<div>&nbsp;</div>");
        return data;
    }

    private StringBuilder createLeftPane(String title, StringBuilder innerData,StringBuilder outerData) {
        StringBuilder buffer=new StringBuilder();
        buffer.append(outerData);
        buffer.append("<div class=\"statuspanelouter\"><div class=\"statuspanelinner\">");
        buffer.append(getSectionHeader("<span style='float: left;'>"+title+"</span><span style='float: right;font-weight:normal'></span>"));
        buffer.append(innerData);
        buffer.append("</div></div>");
        return buffer;
    }

    public StringBuilder getSetupWizard(HttpServletRequest request, String companyid){
        String imgPath="../../images/welcome/";
        StringBuilder buffer = new StringBuilder();
        buffer.append(createHelpSection(getLink(messageSource.getMessage("acc.nee.26", null, RequestContextUtils.getLocale(request)),"callCOA()"),
        		messageSource.getMessage("acc.nee.27", null, RequestContextUtils.getLocale(request)),
                imgPath+getImageName("coa", companyid)+".gif",""));
        buffer.append(createHelpSection(messageSource.getMessage("acc.nee.28", null, RequestContextUtils.getLocale(request))+" "+getLink(messageSource.getMessage("acc.nee.42", null, RequestContextUtils.getLocale(request)),"callCustomerDetails()")+" "+messageSource.getMessage("acc.nee.29", null, RequestContextUtils.getLocale(request))+" "+getLink(messageSource.getMessage("acc.nee.41", null, RequestContextUtils.getLocale(request)),"callVendorDetails()"),
        		messageSource.getMessage("acc.nee.30", null, RequestContextUtils.getLocale(request)),
                imgPath+getImageName("customer", companyid)+".png",""));
        buffer.append(createHelpSection(getLink(messageSource.getMessage("acc.accPref.autoInvoice", null, RequestContextUtils.getLocale(request)),"callInvoice(false,null)"),
        		messageSource.getMessage("acc.nee.31", null, RequestContextUtils.getLocale(request)),
                imgPath+getImageName("invoice", companyid)+".png",messageSource.getMessage("acc.nee.36", null, RequestContextUtils.getLocale(request))));
        buffer.append(createHelpSection(getLink(messageSource.getMessage("acc.nee.32", null, RequestContextUtils.getLocale(request)),"callJournalEntry()"),
        		messageSource.getMessage("acc.nee.33", null, RequestContextUtils.getLocale(request)),
                imgPath+getImageName("journalentry", companyid)+".png",messageSource.getMessage("acc.nee.37", null, RequestContextUtils.getLocale(request))));
        buffer.append(createHelpSection(getLink(messageSource.getMessage("acc.nee.34", null, RequestContextUtils.getLocale(request)),"callProductDetails()"),
        		messageSource.getMessage("acc.nee.35", null, RequestContextUtils.getLocale(request)),
                imgPath+getImageName("product", companyid)+".png",messageSource.getMessage("acc.nee.38", null, RequestContextUtils.getLocale(request))));
        buffer.append(createHelpSection(getLink(messageSource.getMessage("acc.nee.40", null, RequestContextUtils.getLocale(request)),"callReceipt()"),
        		messageSource.getMessage("acc.nee.39", null, RequestContextUtils.getLocale(request)),
                imgPath+getImageName("receipt", companyid)+".gif",""));
        return buffer;
    }

    private StringBuilder createHelpSection(String title, String message, String imgPath, String tipMsg) {
        StringBuilder data=new StringBuilder();
       data.append("<div>&nbsp;</div>");
         data.append("<h2 class='bullet'>"+title+"</h2>");
        data.append("<div style='padding:10px 20px'>"+message+"</div>");
        data.append("<div class='centered'><img src='"+imgPath+"' width='300px' wtf:qtip='"+tipMsg+"' wtf:qtitle='Tip'></div>");
        return data;
    }

    public String joinArrayList(ArrayList arr, String sep) {
        StringBuilder sb=new StringBuilder();
        if(!arr.isEmpty())sb.append(arr.get(0));
        for(int i=1;i<arr.size();i++){
            sb.append(sep+arr.get(i));
        }
        return sb.toString();
    }

    public StringBuilder getSysAdminLinks(HttpServletRequest request, JSONObject perms) {
        StringBuilder newLink = new StringBuilder();

        newLink.append(createNewLink("List of the companies","callSystemAdmin()"));

        return newLink;
    }

    public StringBuilder getCompanyLinks(HttpServletRequest request, JSONObject perms) throws ServiceException {
        StringBuilder finalString = new StringBuilder();
        StringBuilder newLink = new StringBuilder();

        try {
            if(permissionHandler.isPermitted(perms, "accpref", "view"))
                newLink.append(createNewLink("Account Preferences","callAccountPref()","leadlist","Maintain general settings for your organization such as financial year settings, account settings, automatic number generation and email settings."));
            if(permissionHandler.isPermitted(perms, "coa", "view"))
                newLink.append(createNewLink("Chart of Accounts","callCOA()","leadlist","Maintain all your accounts including income, expense, bank accounts and more. You can also export the account list in convenient formats as well as add sub-accounts to existing accounts."));
            if(permissionHandler.isPermitted(perms, "customer", "view"))
                newLink.append(createNewLink("Accounts Receivable/Customer(s)","callCustomerDetails()","leadlist","Maintain all information about your customers including contact information, account details, preferred delivery mode and credit term. You can also export the customer list in convenient formats as well as add sub-accounts to existing customer accounts."));
            if(permissionHandler.isPermitted(perms, "vendor", "view"))
                newLink.append(createNewLink("Accounts Payable/Vendor(s)","callVendorDetails()","leadlist","Maintain all information about your vendors including contact information, account details, preferred delivery mode and debit term. You can also export the vendor list in convenient formats as well as add sub-accounts to existing vendor accounts."));
        } catch (JSONException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        } finally {
            finalString = newLink;
        }

        return finalString;
    }

    public StringBuilder getMasterSettingLinks(HttpServletRequest request, JSONObject perms) throws ServiceException {
        StringBuilder finalString = new StringBuilder();
        StringBuilder newLink = new StringBuilder();
        try {
           newLink.append(createNewLink("Master Configuration","callMasterConfiguration()","leadlist","Define settings for payment methods, payment terms, unit of measure, bank names, preferred delivery mode and more."));
        } finally {
            finalString = newLink;
        }
        return finalString;
    }

    public StringBuilder getProductLinks(HttpServletRequest request, JSONObject perms) throws ServiceException {
        StringBuilder finalString = new StringBuilder();
        StringBuilder newLink = new StringBuilder();

        try {
            if(permissionHandler.isPermitted(perms, "product", "view"))
                newLink.append(createNewLink("Product List","callProductDetails()","leadlist","Maintain details for all products sold by your organization including product details, price, as well as inventory details. You can also add a sub-product to an existing product."));
//            if(PermissionHandler.isPermitted(perms, "uom", "view"))
//                newLink.append(createNewLink("Unit of measure","callUOM()"));
        } catch (JSONException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        } finally {
            finalString = newLink;
        }

        return finalString;
    }

     public StringBuilder getAdministrationLinks(HttpServletRequest request, JSONObject perms) throws ServiceException {
        StringBuilder finalString = new StringBuilder();
        StringBuilder newLink = new StringBuilder();
        try {
            if(permissionHandler.isPermitted(perms, "useradmin", "view"))
                newLink.append(createNewLink("User Administration","loadAdminPage(1)","leadlist","Easily manage all users in the system. Assign roles and permission to individual users in accordance to their work functions."));
        } catch (JSONException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        } finally {
            finalString = newLink;
        }
        return finalString;
    }

    public StringBuilder getPurchaseManagementLinks(HttpServletRequest request, JSONObject perms) throws ServiceException {
        StringBuilder finalString = new StringBuilder();
        StringBuilder newLink = new StringBuilder();
        try {
                newLink.append(createNewLink("Create Cash Purchase","callPurchaseReceipt(false,null)","leadlist","Create a cash purchase receipt to give to your vendors as a payment record, on paying full amount at the time of purchase."));
                newLink.append(createNewLink("Create Purchase Order","callPurchaseOrder(false, null)","leadlist","Easily create purchase order for your vendors. Include debit term and complete purchase information."));
                newLink.append(createNewLink("Create Vendor Invoice","callGoodsReceipt(false,null)","leadlist","Provide your vendors with receipt on delivery of purchased goods. Record product and payment details."));
                newLink.append(createNewLink("Create Debit Note","callDebitNote()","leadlist","Generate a debit note for your vendors for reducing your account payables in cases, such as return of damaged goods, error in billing etc."));
         } catch (Exception e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        } finally {
            finalString = newLink;
        }
        return finalString;
    }

    public StringBuilder getSalesManagementLinks(HttpServletRequest request, JSONObject perms) throws ServiceException, SessionExpiredException {
        StringBuilder finalString = new StringBuilder();
        StringBuilder newLink = new StringBuilder();
        try {
//            CompanyAccountPreferences pref=(CompanyAccountPreferences)session.get(CompanyAccountPreferences.class,AuthHandler.getCompanyid(request));
            String companyID = sessionHandlerImplObj.getCompanyid(request);
            KwlReturnObject cpresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyID);
            CompanyAccountPreferences pref = (CompanyAccountPreferences) cpresult.getEntityList().get(0);

            if(pref!=null){
                if(pref.isWithoutInventory()){
                    newLink.append(createNewLink("Create Cash Sales"," callBillingSalesReceipt(false,null)","leadlist","Create a cash sales receipt to give to your customers as a payment record, on receiving full amount at the time of sale."));
                        newLink.append(createNewLink("Create Invoice","callBillingInvoice(false,null)","leadlist","Generate Invoices for your customers. Include credit term and discounts offered on individual products as well as on the total bill amount."));
                }
                else{
                    newLink.append(createNewLink("Create Cash Sales","callSalesReceipt(false,null)","leadlist","Create a cash sales receipt to give to your customers as a payment record, on receiving full amount at the time of sale."));
                    newLink.append(createNewLink("Create Sales Order","callSalesOrder(false, null)","leadlist","Record all details related to a customer purchase order by generating an associated sales order."));
                    if(permissionHandler.isPermitted(perms, "invoice", "create"))
                        newLink.append(createNewLink("Create Invoice","callInvoice(false,null)","leadlist","Generate Invoices for your customers. Include credit term and discounts offered on individual products as well as on the total bill amount."));
                    if(permissionHandler.isPermitted(perms, "invoice", "view"))
                        newLink.append(createNewLink("Create Credit Note","callCreditNote()","leadlist","If you need to refund your customers on a credit basis i.e. in the near future, generate a credit note for the transaction. Customers can use this credit memo to get a refund in future purchases."));

                }
            }else{
                newLink.append(createNewLink("Create Cash Sales","callSalesReceipt(false,null)","leadlist","Create a cash sales receipt to give to your customers as a payment record, on receiving full amount at the time of sale."));
                newLink.append(createNewLink("Create Sales Order","callSalesOrder(false, null)","leadlist","Record all details related to a customer purchase order by generating an associated sales order."));
                if(permissionHandler.isPermitted(perms, "invoice", "create"))
                    newLink.append(createNewLink("Create Invoice","callInvoice(false,null)","leadlist","Generate Invoices for your customers. Include credit term and discounts offered on individual products as well as on the total bill amount."));
                if(permissionHandler.isPermitted(perms, "invoice", "view"))
                    newLink.append(createNewLink("Create Credit Note","callCreditNote()","leadlist","If you need to refund your customers on a credit basis i.e. in the near future, generate a credit note for the transaction. Customers can use this credit memo to get a refund in future purchases."));
            }
            //   TODO         if(PermissionHandler.isPermitted(perms, "invoice", "view"))
//            if(PermissionHandler.isPermitted(perms, "invoice", "view"))
//                newLink.append(createNewLink("Credit Note","callCreditMemo()"));
//            if(PermissionHandler.isPermitted(perms, "invoice", "view"))
//                newLink.append(createNewLink("Credit Note/Receipt","callReceipt()"));
//               newLink.append(createNewLink("Debit Note/Payment","callDebitNote()"));
//                newLink.append(createNewLink("Payment","callPayment()"));
        } catch (JSONException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        } finally {
            finalString = newLink;
        }
        return finalString;
    }

    public StringBuilder getJournalEntryLinks(HttpServletRequest request, JSONObject perms) throws ServiceException {
        StringBuilder finalString = new StringBuilder();
        StringBuilder newLink = new StringBuilder();
        try {
                if(permissionHandler.isPermitted(perms, "journalentry", "create"))
                    newLink.append(createNewLink("Make a Journal Entry","callJournalEntry()","leadlist","Record miscellaneous transactions which have not been recorded in the application through customer/vendor transactions."));
        } catch (JSONException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        } finally {
            finalString = newLink;
        }
        return finalString;
    }

    public StringBuilder getPaymentLinks(HttpServletRequest request, JSONObject perms) throws ServiceException {
        StringBuilder finalString = new StringBuilder();
        StringBuilder newLink = new StringBuilder();
        try {
//            CompanyAccountPreferences pref=(CompanyAccountPreferences)session.get(CompanyAccountPreferences.class,AuthHandler.getCompanyid(request));
            String companyID = sessionHandlerImplObj.getCompanyid(request);
            KwlReturnObject cpresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyID);
            CompanyAccountPreferences pref = (CompanyAccountPreferences) cpresult.getEntityList().get(0);

            if(pref!=null){
                if(pref.isWithoutInventory())
                    newLink.append(createNewLink("Receive Payment(s)","callBillingReceipt()","leadlist","Record all payments through multiple payment methods including cash, cheque and debit/credit card."));
                 else
                     newLink.append(createNewLink("Receive Payment(s)","callReceipt()","leadlist","Record all payments through multiple payment methods including cash, cheque and debit/credit card."));
            }
            else
                newLink.append(createNewLink("Receive Payment(s)","callReceipt()","leadlist","Record all payments through multiple payment methods including cash, cheque and debit/credit card."));
            newLink.append(createNewLink("Make Payment(s)","callPayment()","leadlist","Record all payments through multiple payment methods including cash, cheque and debit/credit card."));
            } catch (Exception e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        } finally {
            finalString = newLink;
        }
        return finalString;
    }

    public StringBuilder getReportLinks(HttpServletRequest request, JSONObject perms) throws ServiceException, SessionExpiredException {
        StringBuilder finalString = new StringBuilder();
        StringBuilder newLink = new StringBuilder();

        try {
//            CompanyAccountPreferences pref=(CompanyAccountPreferences)session.get(CompanyAccountPreferences.class,AuthHandler.getCompanyid(request));
            String companyID = sessionHandlerImplObj.getCompanyid(request);
            KwlReturnObject cpresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyID);
            CompanyAccountPreferences pref = (CompanyAccountPreferences) cpresult.getEntityList().get(0);

                //newLink.append(createNewLink("Products List","callProductList()"));
                //newLink.append(createNewLink("Customer List","callCustomerReport()"));
                //newLink.append(createNewLink("Vendor List","callVendorReport()"));
//            if(PermissionHandler.isPermitted(perms, "invoice", "view"))
//                newLink.append(createNewLink("Sales Register","callInvoiceDetails()"));
//            if(PermissionHandler.isPermitted(perms, "creditnote", "view"))
//                newLink.append(createNewLink("Credit Note","callCreditMemoDetails()"));
              newLink.append(createNewLink("<b>Financial Statements</b>","callFinalStatement()","leadlist","Track all major financial statements such as trial balance, ledger, trading and profit/loss statement and balance sheet."));
               if(pref!=null){
              if(pref.isWithoutInventory()){
                        newLink.append(createNewLink("Invoice and Cash Sales Report","callBillingInvoiceList()","leadlist","Customers can view complete list of invoices and cash sales receipts issued. Export the list in convenient formats or get a quick view by easily expanding an invoice from the given list."));
                        newLink.append(createNewLink("Received Payment","BillingReceiptReport()","leadlist","View complete details of payments received from your customers. Export the list in convenient formats or get a quick view by easily expanding a payment detail from the given list."));
                    }
                    else{
                        if(permissionHandler.isPermitted(perms, "invoice", "view"))
                            newLink.append(createNewLink("Invoice and Cash Sales Report","callInvoiceList()","leadlist","Customers can view complete list of invoices and cash sales receipts issued. Export the list in convenient formats or get a quick view by easily expanding an invoice from the given list."));
                        newLink.append(createNewLink("Purchase Order", "callPurchaseOrderList()","leadlist","View complete list of purchase orders issued to your vendors. Export the list in convenient formats or get a quick view by easily expanding a purchase order from the given list."));
                        newLink.append(createNewLink("Sales Order", "callSalesOrderList()","leadlist","View complete list of sales order associated with your customers. Export the list in convenient formats or get a quick view by easily expanding a sales order from the given list."));
                        if(permissionHandler.isPermitted(perms, "creditnote", "view"))
                            newLink.append(createNewLink("Credit Note","callCreditNoteDetails()","leadlist","View complete list of credit notes issued to your customers. Export the list in convenient formats or get a quick view by easily expanding a credit note from the given list."));
                        if(permissionHandler.isPermitted(perms, "receipt", "view"))
                            newLink.append(createNewLink("Received Payment(s)","ReceiptReport()","leadlist","View complete details of payments received from your customers. Export the list in convenient formats or get a quick view by easily expanding a payment detail from the given list."));

                        newLink.append(createNewLink("Cash and Credit Purchase Report","callGoodsReceiptList()","leadlist","View complete details of vendor invoice and cash purchase receipt(s) to your vendors. Export the list in convenient formats or get a quick view by easily expanding a vendor invoice from the given list."));
                        newLink.append(createNewLink("Debit Note Report","callDebitNoteDetails()","leadlist","View complete list of debit notes issued to your vendors. Export the list in convenient formats or get a quick view by easily expanding a debit note from the given list."));
                   }
               }
               else{
                  if(permissionHandler.isPermitted(perms, "invoice", "view"))
                            newLink.append(createNewLink("Invoice and Cash Sales Report","callInvoiceList()","leadlist","Customers can view complete list of invoices and cash sales receipts issued. Export the list in convenient formats or get a quick view by easily expanding an invoice from the given list."));
                        newLink.append(createNewLink("Purchase Order", "callPurchaseOrderList()","leadlist","View complete list of purchase orders issued to your vendors. Export the list in convenient formats or get a quick view by easily expanding a purchase order from the given list."));
                        newLink.append(createNewLink("Sales Order", "callSalesOrderList()","leadlist","View complete list of sales order associated with your customers. Export the list in convenient formats or get a quick view by easily expanding a sales order from the given list."));
                        if(permissionHandler.isPermitted(perms, "creditnote", "view"))
                            newLink.append(createNewLink("Credit Note","callCreditNoteDetails()","leadlist","View complete list of credit notes issued to your customers. Export the list in convenient formats or get a quick view by easily expanding a credit note from the given list."));
                        if(permissionHandler.isPermitted(perms, "receipt", "view"))
                            newLink.append(createNewLink("Receive Payment","ReceiptReport()","leadlist","View complete details of payments received from your customers. Export the list in convenient formats or get a quick view by easily expanding a payment detail from the given list."));

                        newLink.append(createNewLink("Vendor Invoice and Cash Purchase Report","callGoodsReceiptList()","leadlist","View complete details of vendor invoice and cash purchase receipt(s) to your vendors. Export the list in convenient formats or get a quick view by easily expanding a vendor invoice from the given list."));
                        newLink.append(createNewLink("Debit Note Report","callDebitNoteDetails()","leadlist","View complete list of debit notes issued to your vendors. Export the list in convenient formats or get a quick view by easily expanding a debit note from the given list."));
              }
//             newLink.append(createNewLink("Book Reports","callBookReport()"));
               newLink.append(createNewLink("Payment Made","callPaymentReport()","leadlist","View complete details of payments made to your vendors. Export the list in convenient formats or get a quick view by easily expanding a payment detail from the given list."));
             if(permissionHandler.isPermitted(perms, "journalentry", "view"))
                newLink.append(createNewLink("Journal Entry","callJournalEntryDetails()","leadlist","Track all journal entries transactions entered into the system."));
//            if(PermissionHandler.isPermitted(perms, "ledger", "view"))
//                newLink.append(createNewLink("Ledger","callLedger()"));
//            if(PermissionHandler.isPermitted(perms, "trialbalance", "view"))
//                newLink.append(createNewLink("Trial Balance","TrialBalance()"));
//            if(PermissionHandler.isPermitted(perms, "trading", "view"))
//                newLink.append(createNewLink("Trading","Trading()"));
//            if(PermissionHandler.isPermitted(perms, "pl", "view"))
//                newLink.append(createNewLink("Profit and Loss","ProfitandLoss()"));
//            if(PermissionHandler.isPermitted(perms, "trading", "view")&&PermissionHandler.isPermitted(perms, "pl", "view"))
//                newLink.append(createNewLink("Trading, Profit and Loss","TradingProfitLoss()"));
//            if(PermissionHandler.isPermitted(perms, "bsheet", "view"))
//                newLink.append(createNewLink("Balance Sheet","BalanceSheet()"));

            if(permissionHandler.isPermitted(perms, "cashbook", "view"))
                newLink.append(createNewLink("Cash Book","callFrequentLedger(true,\"\",\"Cash Book\",\"accountingbase cashbook\")","leadlist","Monitor all cash transactions entered into the system for any time duration."));
            if(permissionHandler.isPermitted(perms, "bankbook", "view"))
                newLink.append(createNewLink("Bank Book","callFrequentLedger(false,\"9\",\"Bank Book\",\"accountingbase bankbook\")","leadlist","Monitor all transactions for a bank account for any time duration."));
////            if(PermissionHandler.isPermitted(perms, "bankbook", "view"))
                newLink.append(createNewLink("Aged Receivable", "callAgedRecievable()","leadlist","Keep a track record of all amount receivables."));
////            if(PermissionHandler.isPermitted(perms, "bankbook", "view"))
                newLink.append(createNewLink("Aged Payable", "callAgedPayable()","leadlist","Keep a track record of all amount payables."));

            if(permissionHandler.isPermitted(perms, "audittrail", "view"))
                newLink.append(createNewLink("Audit Trail","callAuditTrail()","leadlist","Track all user activities through comprehensive Accounting system records."));
        } catch (JSONException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        } finally {
            finalString = newLink;
        }

        return finalString;
    }

    private StringBuilder getDashBoardDataFlow(HttpServletRequest request, JSONObject perms) throws ServiceException, SessionExpiredException {
        StringBuilder finalStr = new StringBuilder();
        try {
            String companyID = sessionHandlerImpl.getCompanyid(request);
            KwlReturnObject cpresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyID);
            CompanyAccountPreferences pref = (CompanyAccountPreferences) cpresult.getEntityList().get(0);
            finalStr.append("<div id='dashhelp' class='outerHelp' ><div class='helpAlert'><img src='../../images/alerticon.gif'></div><div class='helpHeader'>"+messageSource.getMessage("acc.WoutI.42", null, RequestContextUtils.getLocale(request))+"</div><div class='helpContent'><a href='#' class='helplinks' style='color:#445566;' onclick='takeTour()'>"+messageSource.getMessage("acc.WoutI.40", null, RequestContextUtils.getLocale(request))+"</a>&nbsp;&nbsp;<a class='helplinks' style='color:#445566;' href='#' onclick='noThanks()'>"+messageSource.getMessage("acc.WoutI.41", null, RequestContextUtils.getLocale(request))+"</a></div></div>");
            finalStr.append("<div class='firstflowlink' ><IMG class='thickBlackBorder' id = 'purchasemanagement1' SRC='../../images/"+getImageName("purchasemanagement",companyID)+".jpg' usemap='#AlienAreas1'><IMG class='thickBlackBorderInside1' id='customerVendorAndProductManagement1' SRC='../../images/"+getImageName("customervendorinventory",companyID)+".jpg' usemap='#AlienAreas4'></div>");
            finalStr.append("<div class='secondflowlink'><IMG class='thickBlackBorder' id='salesAndBillingManagement1' SRC='../../images/"+getImageName("salesmanagement_quotation",companyID)+".jpg' usemap='#AlienAreas2'><IMG class='thickBlackBorderInside3' id='accountManagement1' SRC='../../images/"+getImageName("accountmanagement",companyID)+".jpg' usemap='#AlienAreas5'><IMG class='thickBlackBorderInside6' id='ratioAnalysis' SRC='../../images/"+getImageName("ratio-analysis-report",companyID)+".jpg' usemap='#AlienAreas6'></div>");
            if(pref.isWithoutTax1099())
                finalStr.append("<div class='thirdflowlink'><IMG class='thickBlackBorder' id='financialReports1' SRC='../../images/"+getImageName("financialreport13",companyID)+".jpg' usemap='#AlienAreas3'></div>");
            else 
                finalStr.append("<div class='thirdflowlink'><IMG class='thickBlackBorder' id='financialReports1' SRC='../../images/"+getImageName("financialreport14",companyID)+".jpg' usemap='#AlienAreas3'></div>");
            finalStr.append("<map name='AlienAreas1'>" );
            finalStr.append("<area shape='rect' coords='50,60,150,160' href=# onclick='callVendorDetails(null,true,true)'  wtf:qtip='"+messageSource.getMessage("acc.WI.44", null, RequestContextUtils.getLocale(request))+"'>");   //Maintain all information about your vendors including contact information, account details, preferred delivery mode and debit term. You can also export the vendor list in convenient formats as well as add sub-accounts to existing vendor accounts.'>");
            finalStr.append("<area shape='rect' coords='140,60,240,149' href=# onclick='callProductDetails(null,true)' wtf:qtip='"+messageSource.getMessage("acc.WI.43", null, RequestContextUtils.getLocale(request))+"'>");   //Maintain details for all products sold by your organization including product details, price, as well as inventory details. You can also add a sub-product to an existing product.'> ");
            finalStr.append("<area shape='rect' coords='10,209,145,303' href=# onclick='callPurchaseReceipt(false,null)' wtf:qtip='"+messageSource.getMessage("acc.WI.42", null, RequestContextUtils.getLocale(request))+"'>");   //Create a cash purchase receipt to give to your vendors as a payment record, on paying full amount at the time of purchase.'> ");
            finalStr.append("<area shape='rect' coords='160,213,330,294' href=# onclick='callPurchaseOrder(false, null)' wtf:qtip='"+messageSource.getMessage("acc.WI.41", null, RequestContextUtils.getLocale(request))+"'>");   //Easily create purchase order for your vendors. Include debit term and complete purchase information.'> ");
            finalStr.append("<area shape='rect' coords='19,369,83,473' href=# onclick='callAgedPayable(true)' wtf:qtip='"+messageSource.getMessage("acc.WI.40", null, RequestContextUtils.getLocale(request))+"'>");   //Keep a track record of all amount payables.'> ");
            finalStr.append("<area shape='rect' coords='160,317,310,400' href=# onclick='callGoodsReceipt(false,null)' wtf:qtip='"+messageSource.getMessage("acc.WI.39", null, RequestContextUtils.getLocale(request))+"'>");   //Provide your vendors with receipt on delivery of purchased goods. Record product and payment details.'> ");
            finalStr.append("<area shape='rect' coords='321,318,394,419' href=# onclick='callDebitNote()' wtf:qtip='"+messageSource.getMessage("acc.WI.38", null, RequestContextUtils.getLocale(request))+"'>");   //Generate a debit note for your vendors for reducing your account payables in cases, such as return of damaged goods, error in billing etc.'>");
            finalStr.append("<area shape='rect' coords='172,422,272,524' href=# onclick='callPayment()' wtf:qtip='"+messageSource.getMessage("acc.WI.37", null, RequestContextUtils.getLocale(request))+"'>");   //Record all payments through multiple payment methods including cash, cheque and debit/credit card.'>");
            finalStr.append("</map>");

            finalStr.append("<map name='AlienAreas2'>" );
            finalStr.append("<area shape='rect' coords='34,67,138,161' href=# onclick='callCustomerDetails(null,true,true)' wtf:qtip='"+messageSource.getMessage("acc.WI.36", null, RequestContextUtils.getLocale(request))+"'>");   //Maintain all information about your customers including contact information, account details, preferred delivery mode and credit term. You can also export the customer list in convenient formats as well as add sub-accounts to existing customer accounts.'>");
            finalStr.append("<area shape='rect' coords='140,73,240,149' href=# onclick='callProductDetails(null,true)' wtf:qtip='"+messageSource.getMessage("acc.WI.35", null, RequestContextUtils.getLocale(request))+"'>");   //Maintain details for all products sold by your organization including product details, price, as well as inventory details. You can also add a sub-product to an existing product.'>");
            finalStr.append("<area shape='rect' coords='10,215,130,301' href=# onclick='callSalesReceipt(false,null)' wtf:qtip='"+messageSource.getMessage("acc.WI.34", null, RequestContextUtils.getLocale(request))+"'>");   //Create a cash sales receipt to give to your customers as a payment record, on receiving full amount at the time of sale.'>");
            finalStr.append("<area shape='rect' coords='160,213,280,294' href=# onclick='callSalesOrder(false, null)' wtf:qtip='"+messageSource.getMessage("acc.WI.33", null, RequestContextUtils.getLocale(request))+"'>");   //Record all details related to a customer purchase order by generating an associated sales order.'>");
            finalStr.append("<area shape='rect' coords='19,369,83,473' href=# onclick='callAgedRecievable(true)' wtf:qtip='"+messageSource.getMessage("acc.WI.32", null, RequestContextUtils.getLocale(request))+"'>");   //Keep a track record of all amount receivables.'>");
            finalStr.append("<area shape='rect' coords='160,317,280,400' href=# onclick='callInvoice(false,null)' wtf:qtip='"+messageSource.getMessage("acc.WI.31", null, RequestContextUtils.getLocale(request))+"'>");   //Generate Invoices for your customers. Include credit term and discounts offered on individual products as well as on the total bill amount.'>");
            finalStr.append("<area shape='rect' coords='321,318,394,419' href=# onclick='callCreditNote()' wtf:qtip='"+messageSource.getMessage("acc.WI.30", null, RequestContextUtils.getLocale(request))+"'>");   //If you need to refund your customers on a credit basis i.e. in the near future, generate a credit note for the transaction. Customers can use this credit memo to get a refund in future purchases.'>");
            finalStr.append("<area shape='rect' coords='154,425,302,511' href=# onclick='callReceipt()' wtf:qtip='"+messageSource.getMessage("acc.WI.29", null, RequestContextUtils.getLocale(request))+"'>");   //Record all payments through multiple payment methods including cash, cheque and debit/credit card.'>");
            finalStr.append("<area shape='rect' coords='320,213,385,290' href=# onclick='callQuotation()' wtf:qtip="+messageSource.getMessage("acc.WI.28", null, RequestContextUtils.getLocale(request))+"'>");   //\"Generate Quotation's related to your Customer's requirements. You can use this to give your Customer's a rough idea of financial costings for their requirements.\">");
            finalStr.append("</map>");

            finalStr.append("<map name='AlienAreas3'>" );

            finalStr.append("<area shape='rect' coords='15,34,129,139' href=# onclick='callFinalStatement()' wtf:qtip='"+messageSource.getMessage("acc.WI.27", null, RequestContextUtils.getLocale(request))+"'>");   //Track all major financial statements such as trial balance, ledger, trading and profit/loss statement and balance sheet.'> ");
            finalStr.append("<area shape='rect' coords='145,35,390,140' href=# onclick='javascript:void(0)' wtf:qtip='"+messageSource.getMessage("acc.WI.26", null, RequestContextUtils.getLocale(request))+"'>");   //Track all the Transaction Records such as Journal Entry, Cash Sales, Cash Purchases, Customer and Vendor Invoices, Credit Note and Debit Note reports among others.'> ");
            finalStr.append("<area shape='rect' coords='36,157,116,243' href=# onclick='TrialBalance()' wtf:qtip='"+messageSource.getMessage("acc.WI.25", null, RequestContextUtils.getLocale(request))+"'>");   //Track all major financial statements such as trial balance.'> ");
            finalStr.append("<area shape='rect' coords='21,254,128,322' href=# onclick='callLedger()' wtf:qtip='"+messageSource.getMessage("acc.WI.24", null, RequestContextUtils.getLocale(request))+"'>");   //Track all major financial statements such as ledger.'> ");
            finalStr.append("<area shape='rect' coords='22,336,146,426' href=# onclick='TradingProfitLoss()' wtf:qtip='"+messageSource.getMessage("acc.WI.23", null, RequestContextUtils.getLocale(request))+"'>");   //Track all major financial statements such as  trading and profit/loss statement '> ");
            finalStr.append("<area shape='rect' coords='23,429,141,501' href=# onclick='BalanceSheet()' wtf:qtip='"+messageSource.getMessage("acc.WI.22", null, RequestContextUtils.getLocale(request))+"'>");   //Track all major financial statements such as balance sheet.'> ");


            finalStr.append("<area shape='rect' coords='18,517,146,583' href=# onclick='callAgedPayable(true)' wtf:qtip='"+messageSource.getMessage("acc.WI.21", null, RequestContextUtils.getLocale(request))+"'>");   //Keep a track record of all amount payables'> ");
            finalStr.append("<area shape='rect' coords='22,598,142,683' href=# onclick='callAgedRecievable(true)' wtf:qtip='"+messageSource.getMessage("acc.WI.20", null, RequestContextUtils.getLocale(request))+"'>");   //Keep a track record of all amount receivables'>");

            finalStr.append("<area shape='rect' coords='169,146,275,257' href=# onclick='callJournalEntryDetails()' wtf:qtip='"+messageSource.getMessage("acc.WI.19", null, RequestContextUtils.getLocale(request))+"'>");   //Track all journal entries transactions entered into the system.'>");
            finalStr.append("<area shape='rect' coords='162,263,271,352' href=# onclick='callInvoiceList()'  wtf:qtip='"+messageSource.getMessage("acc.WI.18", null, RequestContextUtils.getLocale(request))+"'>");   //Customers can view complete list of invoices and cash sales receipts issued. Export the list in convenient formats or get a quick view by easily expanding an invoice from the given list.'>");
            finalStr.append("<area shape='rect' coords='158,367,273,451' href=# onclick='callPurchaseOrderList()' wtf:qtip='"+messageSource.getMessage("acc.WI.17", null, RequestContextUtils.getLocale(request))+"'>");   //View complete list of purchase orders issued to your vendors. Export the list in convenient formats or get a quick view by easily expanding a purchase order from the given list.'>");
            finalStr.append("<area shape='rect' coords='161,462,278,539' href=# onclick='callSalesOrderList()' wtf:qtip='"+messageSource.getMessage("acc.WI.16", null, RequestContextUtils.getLocale(request))+"'>");   //View complete list of sales order associated with your customers. Export the list in convenient formats or get a quick view by easily expanding a sales order from the given list.'>");
            finalStr.append("<area shape='rect' coords='162,541,267,632' href=# onclick='callCreditNoteDetails()' wtf:qtip='"+messageSource.getMessage("acc.WI.15", null, RequestContextUtils.getLocale(request))+"'>");   //View complete list of credit notes issued to your customers. Export the list in convenient formats or get a quick view by easily expanding a credit note from the given list.'>");
            finalStr.append("<area shape='rect' coords='162,643,287,723' href=# onclick='ReceiptReport()' wtf:qtip='"+messageSource.getMessage("acc.WI.14", null, RequestContextUtils.getLocale(request))+"'>");   //View complete details of payments received from your customers. Export the list in convenient formats or get a quick view by easily expanding a payment detail from the given list.'>");

            finalStr.append("<area shape='rect' coords='294,147,394,254' href=# onclick='callGoodsReceiptList()' wtf:qtip='"+messageSource.getMessage("acc.WI.13", null, RequestContextUtils.getLocale(request))+"'>");   //View complete details of vendor invoice and cash purchase receipt(s) to your vendors. Export the list in convenient formats or get a quick view by easily expanding a vendor invoice from the given list.'>");
            finalStr.append("<area shape='rect' coords='291,260,383,354' href=# onclick='callDebitNoteDetails()' wtf:qtip='"+messageSource.getMessage("acc.WI.12", null, RequestContextUtils.getLocale(request))+"'>");   //View complete list of debit notes issued to your vendors. Export the list in convenient formats or get a quick view by easily expanding a debit note from the given list.'>");
            finalStr.append("<area shape='rect' coords='290,366,387,453' href=# onclick='callPaymentReport()' wtf:qtip='"+messageSource.getMessage("acc.WI.11", null, RequestContextUtils.getLocale(request))+"'>");   //View complete details of payments made to your vendors. Export the list in convenient formats or get a quick view by easily expanding a payment detail from the given list.'>");
            finalStr.append("<area shape='rect' coords='293,461,389,534' href=# onclick='callFrequentLedger(true,\"\",\""+messageSource.getMessage("acc.WoutI.7", null, RequestContextUtils.getLocale(request))+"\",\"accountingbase cashbook\")' wtf:qtip='"+messageSource.getMessage("acc.WI.10", null, RequestContextUtils.getLocale(request))+"'>");   //Monitor all cash transactions entered into the system for any time duration.'>");
            finalStr.append("<area shape='rect' coords='291,549,389,617' href=# onclick='callFrequentLedger(false,\"9\",\""+messageSource.getMessage("acc.WoutI.8", null, RequestContextUtils.getLocale(request))+"\",\"accountingbase bankbook\")' wtf:qtip='"+messageSource.getMessage("acc.WI.9", null, RequestContextUtils.getLocale(request))+"'>");   //Monitor all transactions for a bank account for any time duration.'>");
            finalStr.append("<area shape='rect' coords='35,679,99,760' href=# onclick='callQuotationList()' wtf:qtip='"+messageSource.getMessage("acc.WI.8", null, RequestContextUtils.getLocale(request))+"'>");   //View complete list of Quotations associated with your customers.'>");
            if(!pref.isWithoutTax1099())
            {
                finalStr.append("<area shape='rect' coords='291,638,389,720' href=# onclick='call1099Report()' wtf:qtip='"+messageSource.getMessage("acc.WI.7", null, RequestContextUtils.getLocale(request))+"'>");   //Tax 1099 Accounts.'>");
            }
            finalStr.append("</map>");

            finalStr.append("<map name='AlienAreas4'>" );
            finalStr.append("<area shape='rect' coords='9,27,100,137' href=# onclick='callCustomerDetails(true)' wtf:qtip='"+messageSource.getMessage("acc.WI.6", null, RequestContextUtils.getLocale(request))+"'>");   //Maintain all information about your customers including contact information, account details, preferred delivery mode and credit term. You can also export the customer list in convenient formats as well as add sub-accounts to existing customer accounts.'>");
            finalStr.append("<area shape='rect' coords='103,35,181,136' href=# onclick='callVendorDetails(true)'  wtf:qtip='"+messageSource.getMessage("acc.WI.5", null, RequestContextUtils.getLocale(request))+"'>");   //Maintain all information about your vendors including contact information, account details, preferred delivery mode and debit term. You can also export the vendor list in convenient formats as well as add sub-accounts to existing vendor accounts.'>");
            finalStr.append("<area shape='rect' coords='5,31,289,132' href=# onclick='callProductDetails()' wtf:qtip='"+messageSource.getMessage("acc.WI.4", null, RequestContextUtils.getLocale(request))+"'>");   //Maintain details for all products sold by your organization including product details, price, as well as inventory details. You can also add a sub-product to an existing product.'> ");
            finalStr.append("</map>");


            finalStr.append("<map name='AlienAreas5'>" );
            finalStr.append("<area shape='rect' coords='10,32,97,125' href=# onclick='callJournalEntry()' wtf:qtip='"+messageSource.getMessage("acc.WI.3", null, RequestContextUtils.getLocale(request))+"'>");   //Record miscellaneous transactions which have not been recorded in the application through customer/vendor transactions.'>");
            finalStr.append("<area shape='rect' coords='109,41,190,131' href=# onclick='callCOA()'  wtf:qtip='"+messageSource.getMessage("acc.WI.2", null, RequestContextUtils.getLocale(request))+"'>");   //Maintain all your accounts including income, expense, bank accounts and more. You can also export the account list in convenient formats as well as add sub-accounts to existing accounts.'>");
            finalStr.append("</map>");

            finalStr.append("<map name='AlienAreas6'>" );
            finalStr.append("<area shape='rect' coords='5,36,160,131' href=# onclick='callRatioAnalysis()' wtf:qtip='"+messageSource.getMessage("acc.WI.1", null, RequestContextUtils.getLocale(request))+"'>");   //Gives the summary view of the effect of account on each other.'>");
            finalStr.append("</map>");
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return finalStr;
    }

    private StringBuilder getDashBoardDataFlowWithoutInv(HttpServletRequest request, JSONObject perms) throws ServiceException {
        StringBuilder finalStr = new StringBuilder();
        try {
            String companyID = sessionHandlerImpl.getCompanyid(request);
            KwlReturnObject cpresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyID);
            CompanyAccountPreferences pref = (CompanyAccountPreferences) cpresult.getEntityList().get(0);
            finalStr.append("<div id='dashhelp' class='outerHelp' ><div class='helpAlert'><img src='../../images/alerticon.gif'></div><div class='helpHeader'>"+messageSource.getMessage("acc.WoutI.42", null, RequestContextUtils.getLocale(request))+"</div><div class='helpContent'><a href='#' class='helplinks' style='color:#445566;' onclick='takeTour()'>"+messageSource.getMessage("acc.WoutI.40", null, RequestContextUtils.getLocale(request))+"</a>&nbsp;&nbsp;<a class='helplinks' style='color:#445566;' href='#' onclick='noThanks()'>"+messageSource.getMessage("acc.WoutI.41", null, RequestContextUtils.getLocale(request))+"</a></div></div>");
            finalStr.append("<div class='firstflowlink' ><IMG class='thickBlackBorder' id = 'purchasemanagement2' SRC='../../images/"+getImageName("purchasemanagement2",companyID)+".jpg' usemap='#AlienAreas1'><IMG class='thickBlackBorderInside1' id='customerVendorManagement2' SRC='../../images/"+getImageName("customervendorwithoutinventory",companyID)+".jpg' usemap='#AlienAreas4'></div>");
            finalStr.append("<div class='secondflowlink'><IMG class='thickBlackBorder' id='salesAndBillingManagement2' SRC='../../images/"+getImageName("salesmanagement2",companyID)+".jpg' usemap='#AlienAreas2'><IMG class='thickBlackBorderInside3' id='accountManagement2' SRC='../../images/"+getImageName("accountmanagement",companyID)+".jpg' usemap='#AlienAreas5'><IMG class='thickBlackBorderInside6' id='ratioAnalysis' SRC='../../images/"+getImageName("ratio-analysis-report",companyID)+".jpg' usemap='#AlienAreas6'></div>");
            if(pref.isWithoutTax1099())
                finalStr.append("<div class='thirdflowlink'><IMG class='thickBlackBorder' id='financialReports2' SRC='../../images/"+getImageName("financialreport13",companyID)+".jpg' usemap='#AlienAreas3'></div>");
            else
                finalStr.append("<div class='thirdflowlink'><IMG class='thickBlackBorder' id='financialReports2' SRC='../../images/"+getImageName("financialreport14",companyID)+".jpg' usemap='#AlienAreas3'></div>");
            finalStr.append("<map name='AlienAreas1'>" );
            finalStr.append("<area shape='rect' coords='111,74,171,154' href=# onclick='callVendorDetails(null,true,true)'  wtf:qtip='"+messageSource.getMessage("acc.WoutI.39", null, RequestContextUtils.getLocale(request))+"'>");   //Maintain all information about your vendors including contact information, account details, preferred delivery mode and debit term. You can also export the vendor list in convenient formats as well as add sub-accounts to existing vendor accounts.'>");
            finalStr.append("<area shape='rect' coords='0,208,130,301' href=# onclick='callBillingPurchaseReceipt(false,null)' wtf:qtip='"+messageSource.getMessage("acc.WoutI.38", null, RequestContextUtils.getLocale(request))+"'>");   //Create a cash purchase receipt to give to your vendors as a payment record, on paying full amount at the time of purchase.'> ");
            finalStr.append("<area shape='rect' coords='156,208,296,301' href=# onclick='callBillingPurchaseOrder(false, null)' wtf:qtip='"+messageSource.getMessage("acc.WoutI.37", null, RequestContextUtils.getLocale(request))+"'>");   //Easily create purchase order for your vendors. Include debit term and complete purchase information.'> ");
            finalStr.append("<area shape='rect' coords='14,375,84,473' href=# onclick='callAgedPayable(false)' wtf:qtip='"+messageSource.getMessage("acc.WoutI.36", null, RequestContextUtils.getLocale(request))+"'>");   //Keep a track record of all amount payables.'> ");
            finalStr.append("<area shape='rect' coords='150,319,287,408' href=# onclick='callBillingGoodsReceipt(false,null)' wtf:qtip='"+messageSource.getMessage("acc.WoutI.35", null, RequestContextUtils.getLocale(request))+"'>");   //Provide your vendors with receipt on delivery of purchased goods. Record product and payment details.'> ");
            finalStr.append("<area shape='rect' coords='326,323,386,420' href=# onclick='callBillingDebitNote()' wtf:qtip='"+messageSource.getMessage("acc.WoutI.34", null, RequestContextUtils.getLocale(request))+"'>");   //Generate a debit note for your vendors for reducing your account payables in cases, such as return of damaged goods, error in billing etc.'>");
            finalStr.append("<area shape='rect' coords='170,429,270,509' href=# onclick='callBillingPayment()' wtf:qtip='"+messageSource.getMessage("acc.WoutI.33", null, RequestContextUtils.getLocale(request))+"'>");   //Record all payments through multiple payment methods including cash, cheque and debit/credit card.'>");
            finalStr.append("</map>");

            finalStr.append("<map name='AlienAreas2'>" );
            finalStr.append("<area shape='rect' coords='84,67,201,159' href=# onclick='callCustomerDetails(null,true,true)' wtf:qtip='"+messageSource.getMessage("acc.WoutI.32", null, RequestContextUtils.getLocale(request))+"'>");   //Maintain all information about your customers including contact information, account details, preferred delivery mode and credit term. You can also export the customer list in convenient formats as well as add sub-accounts to existing customer accounts.'>");
            finalStr.append("<area shape='rect' coords='10,218,110,298' href=# onclick='callBillingSalesReceipt(false,null)' wtf:qtip='"+messageSource.getMessage("acc.WoutI.31", null, RequestContextUtils.getLocale(request))+"'>");   //Create a cash sales receipt to give to your customers as a payment record, on receiving full amount at the time of sale.'>");
            finalStr.append("<area shape='rect' coords='170,213,270,294' href=# onclick='callBillingSalesOrder(false, null)' wtf:qtip='"+messageSource.getMessage("acc.WoutI.30", null, RequestContextUtils.getLocale(request))+"'>");   //Record all details related to a customer purchase order by generating an associated sales order.'>");
            finalStr.append("<area shape='rect' coords='14,375,84,465' href=# onclick='callAgedRecievable(false)' wtf:qtip='"+messageSource.getMessage("acc.WoutI.29", null, RequestContextUtils.getLocale(request))+"'>");   //Keep a track record of all amount receivables'>");
            finalStr.append("<area shape='rect' coords='170,320,253,395' href=# onclick='callBillingInvoice(false,null)' wtf:qtip='"+messageSource.getMessage("acc.WoutI.28", null, RequestContextUtils.getLocale(request))+"'>");   //Generate Invoices for your customers. Include credit term and discounts offered on individual products as well as on the total bill amount.'>");
            finalStr.append("<area shape='rect' coords='326,323,386,413' href=# onclick='callBillingCreditNote()' wtf:qtip='"+messageSource.getMessage("acc.WoutI.27", null, RequestContextUtils.getLocale(request))+"'>");   //If you need to refund your customers on a credit basis i.e. in the near future, generate a credit note for the transaction. Customers can use this credit memo to get a refund in future purchases.'>");
            finalStr.append("<area shape='rect' coords='170,429,270,509' href=# onclick='callBillingReceipt()' wtf:qtip='"+messageSource.getMessage("acc.WoutI.26", null, RequestContextUtils.getLocale(request))+"'>");   //Record all payments through multiple payment methods including cash, cheque and debit/credit card.'>");
            finalStr.append("</map>");

            finalStr.append("<map name='AlienAreas3'>" );

            finalStr.append("<area shape='rect' coords='15,34,129,139' href=# onclick='callFinalStatement()' wtf:qtip='"+messageSource.getMessage("acc.WoutI.25", null, RequestContextUtils.getLocale(request))+"'>");   //Track all major financial statements such as trial balance, ledger, trading and profit/loss statement and balance sheet.'> ");
            finalStr.append("<area shape='rect' coords='145,35,390,140' href=# onclick='javascript:void(0)' wtf:qtip='"+messageSource.getMessage("acc.WoutI.24", null, RequestContextUtils.getLocale(request))+"'>");   //Track all the Transaction Records such as Journal Entry, Cash Sales, Cash Purchases, Customer and Vendor Invoices, Credit Note and Debit Note reports among others.'> ");
            finalStr.append("<area shape='rect' coords='36,157,116,243' href=# onclick='TrialBalance()' wtf:qtip='"+messageSource.getMessage("acc.WoutI.23", null, RequestContextUtils.getLocale(request))+"'>");   //Track all major financial statements such as trial balance.'> ");
            finalStr.append("<area shape='rect' coords='21,254,128,322' href=# onclick='callLedger()' wtf:qtip='"+messageSource.getMessage("acc.WoutI.22", null, RequestContextUtils.getLocale(request))+"'>");   //Track all major financial statements such as ledger.'> ");
            finalStr.append("<area shape='rect' coords='22,336,146,426' href=# onclick='TradingProfitLoss()' wtf:qtip='"+messageSource.getMessage("acc.WoutI.21", null, RequestContextUtils.getLocale(request))+"'>");   //rack all major financial statements such as  trading and profit/loss statement.'> ");
            finalStr.append("<area shape='rect' coords='23,429,141,501' href=# onclick='BalanceSheet()' wtf:qtip='"+messageSource.getMessage("acc.WoutI.20", null, RequestContextUtils.getLocale(request))+"'>");   //Track all major financial statements such as balance sheet.'> ");


            finalStr.append("<area shape='rect' coords='18,517,146,583' href=# onclick='callAgedPayable(false)' wtf:qtip='"+messageSource.getMessage("acc.WoutI.19", null, RequestContextUtils.getLocale(request))+"'>");   //Keep a track record of all amount payables'> ");
            finalStr.append("<area shape='rect' coords='22,598,142,683' href=# onclick='callAgedRecievable(false)' wtf:qtip='"+messageSource.getMessage("acc.WoutI.18", null, RequestContextUtils.getLocale(request))+"'>");   //Keep a track record of all amount receivables'>");

            finalStr.append("<area shape='rect' coords='169,146,275,257' href=# onclick='callJournalEntryDetails()' wtf:qtip='"+messageSource.getMessage("acc.WoutI.17", null, RequestContextUtils.getLocale(request))+"'>");   //Track all journal entries transactions entered into the system.'>");
            finalStr.append("<area shape='rect' coords='162,263,271,352' href=# onclick='callBillingInvoiceList()'  wtf:qtip='"+messageSource.getMessage("acc.WoutI.16", null, RequestContextUtils.getLocale(request))+"'>");   //Customers can view complete list of invoices and cash sales receipts issued. Export the list in convenient formats or get a quick view by easily expanding an invoice from the given list.'>");
            finalStr.append("<area shape='rect' coords='158,367,273,451' href=# onclick='callBillingPurchaseOrderList()' wtf:qtip='"+messageSource.getMessage("acc.WoutI.15", null, RequestContextUtils.getLocale(request))+"'>");   //View complete list of purchase orders issued to your vendors. Export the list in convenient formats or get a quick view by easily expanding a purchase order from the given list.'>");
            finalStr.append("<area shape='rect' coords='161,462,278,539' href=# onclick='callBillingSalesOrderList()' wtf:qtip='"+messageSource.getMessage("acc.WoutI.14", null, RequestContextUtils.getLocale(request))+"'>");   //View complete list of sales order associated with your customers. Export the list in convenient formats or get a quick view by easily expanding a sales order from the given list.'>");
            finalStr.append("<area shape='rect' coords='162,541,267,632' href=# onclick='callBillingCreditNoteDetails()' wtf:qtip='"+messageSource.getMessage("acc.WoutI.13", null, RequestContextUtils.getLocale(request))+"'>");   //View complete list of credit notes issued to your customers. Export the list in convenient formats or get a quick view by easily expanding a credit note from the given list.'>");
            finalStr.append("<area shape='rect' coords='162,643,287,723' href=# onclick='BillingReceiptReport()' wtf:qtip='"+messageSource.getMessage("acc.WoutI.12", null, RequestContextUtils.getLocale(request))+"'>");   //View complete details of payments received from your customers. Export the list in convenient formats or get a quick view by easily expanding a payment detail from the given list.'>");

            finalStr.append("<area shape='rect' coords='294,147,394,254' href=# onclick='callBillingGoodsReceiptList()' wtf:qtip='"+messageSource.getMessage("acc.WoutI.11", null, RequestContextUtils.getLocale(request))+"'>");   //View complete details of vendor invoice and cash purchase receipt(s) to your vendors. Export the list in convenient formats or get a quick view by easily expanding a vendor invoice from the given list.
            finalStr.append("<area shape='rect' coords='291,260,383,354' href=# onclick='callBillingDebitNoteDetails()' wtf:qtip='"+messageSource.getMessage("acc.WoutI.10", null, RequestContextUtils.getLocale(request))+"'>");   //View complete list of debit notes issued to your vendors. Export the list in convenient formats or get a quick view by easily expanding a debit note from the given list.
            finalStr.append("<area shape='rect' coords='290,366,387,453' href=# onclick='callBillingPaymentReport()' wtf:qtip='"+messageSource.getMessage("acc.WoutI.9", null, RequestContextUtils.getLocale(request))+"'>"); //View complete details of payments made to your vendors. Export the list in convenient formats or get a quick view by easily expanding a payment detail from the given list.
            finalStr.append("<area shape='rect' coords='293,461,389,534' href=# onclick='callFrequentLedger(true,\"\",\""+messageSource.getMessage("acc.WoutI.8", null, RequestContextUtils.getLocale(request))+"\",\"accountingbase cashbook\")' wtf:qtip='"+messageSource.getMessage("acc.WoutI.8", null, RequestContextUtils.getLocale(request))+"'>");   //Monitor all cash transactions entered into the system for any time duration.
            finalStr.append("<area shape='rect' coords='291,549,389,617' href=# onclick='callFrequentLedger(false,\"9\",\""+messageSource.getMessage("acc.WoutI.7", null, RequestContextUtils.getLocale(request))+"\",\"accountingbase bankbook\")' wtf:qtip='"+messageSource.getMessage("acc.WoutI.7", null, RequestContextUtils.getLocale(request))+"'>");   //Monitor all transactions for a bank account for any time duration.
            if(!pref.isWithoutTax1099())
            {
                finalStr.append("<area shape='rect' coords='291,638,389,720' href=# onclick='call1099Report()' wtf:qtip='"+messageSource.getMessage("acc.WoutI.6", null, RequestContextUtils.getLocale(request))+"'>");   //Tax 1099 Accounts.
            }
            finalStr.append("</map>");

            finalStr.append("<map name='AlienAreas4'>" );
            finalStr.append("<area shape='rect' coords='14,27,105,137' href=# onclick='callCustomerDetails(true)' wtf:qtip='"+messageSource.getMessage("acc.WoutI.5", null, RequestContextUtils.getLocale(request))+"'>");   //Maintain all information about your customers including contact information, account details, preferred delivery mode and credit term. You can also export the customer list in convenient formats as well as add sub-accounts to existing customer accounts.
            finalStr.append("<area shape='rect' coords='120,35,205,136' href=# onclick='callVendorDetails(true)'  wtf:qtip='"+messageSource.getMessage("acc.WoutI.4", null, RequestContextUtils.getLocale(request))+"'>");   //Maintain all information about your vendors including contact information, account details, preferred delivery mode and debit term. You can also export the vendor list in convenient formats as well as add sub-accounts to existing vendor accounts.
            finalStr.append("</map>");


            finalStr.append("<map name='AlienAreas5'>" );
            finalStr.append("<area shape='rect' coords='10,32,97,125' href=# onclick='callJournalEntry()' wtf:qtip='"+messageSource.getMessage("acc.WoutI.3", null, RequestContextUtils.getLocale(request))+"'>");   //Record miscellaneous transactions which have not been recorded in the application through customer/vendor transactions.
            finalStr.append("<area shape='rect' coords='109,41,190,131' href=# onclick='callCOA()'  wtf:qtip='"+messageSource.getMessage("acc.WoutI.2", null, RequestContextUtils.getLocale(request))+"'>");   //Maintain all your accounts including income, expense, bank accounts and more. You can also export the account list in convenient formats as well as add sub-accounts to existing accounts.
            finalStr.append("</map>");

            finalStr.append("<map name='AlienAreas6'>" );
            finalStr.append("<area shape='rect' coords='0,0,117,133' href=# onclick='callRatioAnalysis()' wtf:qtip='"+messageSource.getMessage("acc.WoutI.1", null, RequestContextUtils.getLocale(request))+"'>");  // Gives the summary view of the effect of account on each other.
            finalStr.append("</map>");
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return finalStr;
    }
    
    
    public ModelAndView getDashboardLinks(HttpServletRequest request, HttpServletResponse response) {
        String msg = "";
        try {
            Session session = HibernateUtil.getCurrentSession();
            msg = getPartnerLinks(request, session, request.getParameter("companyid"));
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accDashboardController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
        }
        return new ModelAndView("jsonView_ex", "model", msg);
    }
    
    
    public String getPartnerLinks(HttpServletRequest request, Session session, String companyid) throws ServiceException, SessionExpiredException{
        JSONObject jResult = new JSONObject();
        JSONObject appdata = new JSONObject();
        try {                       
        	String platformURL = this.getServletContext().getInitParameter("platformURL");
            JSONObject jobj = new JSONObject();
            jobj.put("companyid",companyid);
            jobj.put("userid",sessionHandlerImplObj.getUserid(request));
            jobj.put("subdomain",URLUtil.getDomainName(request));
            jobj.put("appid", "3");
            appdata = APICallHandler.callApp(session, platformURL, jobj, companyid, "14");           
            jResult.put("valid", true);
            jResult.put("success", true);
            jResult.put("data", appdata.toString());
        } catch (JSONException ex) {
            Logger.getLogger(accDashboardController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jResult.toString();
    }

    public ModelAndView getMaintainanceDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try{
            JSONArray jarr=null;
            //String platformURL=this.getServletContext().getInitParameter("platformURL");
            Session session = HibernateUtil.getCurrentSession();
            String platformURL = this.getServletContext().getInitParameter("platformURL");
            String accURL = StorageHandler.getAccURL();
            //String crmURL=this.getServletContext().getInitParameter("crmURL");
            String action = "9";
            String companyID = sessionHandlerImpl.getCompanyid(request);
            JSONObject userData = new JSONObject();
            userData.put("remoteapikey",StorageHandler.GetRemoteAPIKey());
            userData.put("companyid",sessionHandlerImpl.getCompanyid(request));
            userData.put("requesturl",accURL);
            JSONObject resObj = APICallHandler.callApp(session, platformURL, userData, companyID, action);
            if (!resObj.isNull("success") && resObj.getBoolean("success")) {
                jarr=resObj.getJSONArray("data");
            }
            if (jarr!=null&&jarr.length()>0) {
                jobj.put("data", jarr);
                msg="Data fetched successfully";
                issuccess = true;
            } else {
                msg="Error occurred while fetching data ";
            }
        } catch (Exception ex){
            logger.warn("Error occured", ex);
         } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                logger.warn("cannot create json object", ex);
            }
        }
        return new ModelAndView("jsonView","model", jobj.toString());
    }
    
    public String getImageName(String imageName, String companyid) {
        String reportFileName = imageName;
    	try {
    		KwlReturnObject companyresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) companyresult.getEntityList().get(0);
            
            if (company.getLanguage() != null && company.getLanguage().getId() != 1) {
	            if(company.getLanguage().getId() == 3)
	            	reportFileName = company.getLanguage().getId() + "/" + imageName + "_" + company.getLanguage().getId();
	            else
	            	reportFileName = imageName;
            } else 
            	reportFileName = imageName;
    	} catch (Exception ex) {
    		Logger.getLogger(accDashboardController.class.getName()).log(Level.SEVERE, null, ex);
    	}
		return reportFileName;
    }
}
