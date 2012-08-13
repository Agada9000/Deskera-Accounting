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
package com.krawler.spring.common;

import com.krawler.common.admin.CompanyType;
import com.krawler.common.admin.User;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.StringUtil;
import com.krawler.esp.handlers.FileUploadHandler;
import com.krawler.esp.handlers.StorageHandler;
import com.krawler.esp.servlets.ProfileImageServlet;
import com.krawler.esp.utils.ConfigReader;
import com.krawler.spring.profileHandler.profileHandlerDAO;

import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItem;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 *
 * @author krawler
 */
public class CommonFnController extends MultiActionController implements MessageSourceAware{

    private profileHandlerDAO profileHandlerDAOObj;
    private HibernateTransactionManager txnManager;
    private AccCommonTablesDAO accCommonTablesDAO;
    private MessageSource messageSource;
    
	@Override
	public void setMessageSource(MessageSource ms) {
		this.messageSource=ms;
	}
    public void setprofileHandlerDAO(profileHandlerDAO profileHandlerDAOObj1) {
        this.profileHandlerDAOObj = profileHandlerDAOObj1;
    }

    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }
    public void setaccCommonTablesDAO(AccCommonTablesDAO accCommonTablesDAO) {
        this.accCommonTablesDAO = accCommonTablesDAO;
    }

    public ModelAndView saveUsers(HttpServletRequest request, HttpServletResponse response) {
        HashMap hm = null;
        String msg = "";
        Boolean success = false;
        JSONObject jobj = new JSONObject();
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("CF_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            hm = new FileUploadHandler().getItems(request);
            HashMap<String, Object> requestMap = generateMap(hm);
            KwlReturnObject rtObj = profileHandlerDAOObj.saveUser(requestMap);
            User usr = (User) rtObj.getEntityList().get(0);
            String imageName = ((FileItem) (hm.get("userimage"))).getName();
            if (StringUtil.isNullOrEmpty(imageName) == false) {
                String fileName = usr.getUserID() + FileUploadHandler.getImageExt();
                usr.setImage(ProfileImageServlet.ImgBasePath + fileName);
                new FileUploadHandler().uploadImage((FileItem) hm.get("userimage"),
                        fileName,
                        StorageHandler.GetProfileImgStorePath(), 100, 100, false, false);
            }
            success = true;
            msg = messageSource.getMessage("acc.rem.189", null, RequestContextUtils.getLocale(request));
            txnManager.commit(status);
        } catch (ServiceException ex) {
            success = false;
            msg = ex.getMessage();
            txnManager.rollback(status);
            Logger.getLogger(CommonFnController.class.getName()).log(Level.SEVERE, "saveUsers", ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            success = false;
            txnManager.rollback(status);
            Logger.getLogger(CommonFnController.class.getName()).log(Level.SEVERE, "saveUsers", ex);
        } finally {
            try {
                jobj.put("success", success);
                jobj.put("msg", msg);
            } catch (com.krawler.utils.json.base.JSONException ex) {
                Logger.getLogger(CommonFnController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        // }
        return new ModelAndView("jsonView_ex", "model", jobj.toString());
    }

    public HashMap<String, Object> generateMap(HashMap requestMap) throws UnsupportedEncodingException {
        HashMap<String, Object> params = new HashMap();
        params.put("userid", getUTFString(requestMap.get("userid")));
        params.put("firstName", getUTFString(requestMap.get("fname")));
        params.put("lastName", getUTFString(requestMap.get("lname")));
        params.put("emailID", getUTFString(requestMap.get("emailid")));
        params.put("address", getUTFString(requestMap.get("address")));
        params.put("contactNumber", getUTFString(requestMap.get("contactno")));
        params.put("aboutUser", getUTFString(requestMap.get("aboutuser")));
        params.put("dateformat", getUTFString(getFKvalue(requestMap.get("formatid"))));
        params.put("timeZone", getUTFString(getFKvalue(requestMap.get("tzid"))));
        return params;
    }

    public String getFKvalue(Object str) {
        return str==null?null:StringUtil.isNullOrEmpty(str.toString())?null:str.toString();
    }

    public String getUTFString(Object str) throws UnsupportedEncodingException {
        return str==null?null:(new String(str.toString().getBytes("ISO-8859-1"),"UTF-8"));
    }

    public ModelAndView changeUserPassword(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        KwlReturnObject kmsg = null;
        //Create transaction
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("CF_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String platformURL = this.getServletContext().getInitParameter("platformURL");

            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("currentpassword", StringUtil.checkForNull(request.getParameter("currentpassword")));
            requestParams.put("changepassword", StringUtil.checkForNull(request.getParameter("changepassword")));
            requestParams.put("userid", sessionHandlerImpl.getUserid(request));
            requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            requestParams.put("remoteapikey", ConfigReader.getinstance().get("remoteapikey"));


            kmsg = profileHandlerDAOObj.changeUserPassword(platformURL, requestParams);
            jobj = (JSONObject) kmsg.getEntityList().get(0);
            txnManager.commit(status);
            jobj.put("success", true);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            txnManager.rollback(status);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getCompanyTypes(HttpServletRequest request, HttpServletResponse response){
        JSONObject jobj=new JSONObject();
        boolean issuccess = true;
        String msg = "";
		try {
            KwlReturnObject result = accCommonTablesDAO.getCompanyTypes();
            List ll = result.getEntityList();
            Iterator itr = ll.iterator();

            JSONArray jArr=new JSONArray();
            while (itr.hasNext()) {
                CompanyType cType = (CompanyType) itr.next();
                JSONObject obj = new JSONObject();
                obj.put("id", cType.getID());
                obj.put("name", messageSource.getMessage("acc.ct."+cType.getID(), null, RequestContextUtils.getLocale(request)));
                obj.put("details", cType.getDetails());
                jArr.put(obj);
            }
            jobj.put("data", jArr);
            jobj.put("totalCount", result.getRecordTotalCount());
        } catch(Exception ex) {
            Logger.getLogger(CommonFnController.class.getName()).log(Level.SEVERE, null, ex);
            issuccess = false;
            msg = "CommonFnController.getCompanyType : "+ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(CommonFnController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
}
