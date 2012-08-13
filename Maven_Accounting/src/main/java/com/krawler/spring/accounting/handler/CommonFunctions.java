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
import com.krawler.common.admin.KWLCurrency;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.StringUtil;
import com.krawler.esp.handlers.APICallHandler;
import com.krawler.esp.handlers.AuthHandler;
import com.krawler.esp.handlers.SendMailHandler;
import com.krawler.esp.servlets.ProfileImageServlet;

import com.krawler.hql.accounting.CompanyAccountPreferences;
import com.krawler.spring.accounting.currency.accCurrencyController;
import com.krawler.spring.accounting.product.accProductController;
import com.krawler.spring.accounting.tax.accTaxController;
import com.krawler.spring.accounting.term.accTermController;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.authHandler.authHandlerDAO;
import com.krawler.spring.common.CommonFnController;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.exportFuctionality.ExportRecord;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.spring.storageHandler.storageHandlerImpl;
import com.krawler.utils.json.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.lowagie.text.DocumentException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.String;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.MessagingException;
import com.krawler.utils.json.base.JSONObject;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 *
 * @author krawler
 */
public class CommonFunctions  extends MultiActionController implements MessageSourceAware{
    private authHandlerDAO authHandlerDAOObj;
    private ExportRecord ExportrecordObj;
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;
    private accTaxController AccTaxcontrollerObj;
    private accProductController AccProductcontrollerObj;
    private accTermController AccTermcontrollerObj;
    private accCurrencyController AccCurrencycontrollerObj;
    private MessageSource messageSource;
    
	@Override
	public void setMessageSource(MessageSource ms) {
		this.messageSource=ms;
	}

       public void setkwlCommonTablesDAO(kwlCommonTablesDAO kwlCommonTablesDAOObj1) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj1;
    }
    public void setauthHandlerDAO(authHandlerDAO authHandlerDAOObj1) {
        this.authHandlerDAOObj = authHandlerDAOObj1;
    }
    public void setexportRecord(ExportRecord ExportrecordObj) {
        this.ExportrecordObj = ExportrecordObj;
    }

    public void setaccTaxcontroller(accTaxController accTaxControllerObj) {
        this.AccTaxcontrollerObj = accTaxControllerObj;
    }

    public void setaccProductcontroller(accProductController accProductControllerObj) {
        this.AccProductcontrollerObj = accProductControllerObj;
    }

    public void setaccTermcontroller(accTermController accTermControllerObj) {
        this.AccTermcontrollerObj = accTermControllerObj;
    }

    public void setaccCurrencycontroller(accCurrencyController accCurrencyControllerObj) {
        this.AccCurrencycontrollerObj = accCurrencyControllerObj;
    }

    public ModelAndView sendMail(HttpServletRequest request, HttpServletResponse response ) throws FileNotFoundException, IOException, DocumentException, ServiceException, JSONException {
       java.io.OutputStream os = null;
       JSONObject jobj = new JSONObject();
        {
        
            ByteArrayOutputStream baos = null;
            boolean issuccess = false;
            try {
                
                String[] emails=request.getParameter("emailid").split(";");
                String personid=request.getParameter("personid");
                String plainMsg =request.getParameter("message");
                String subject = request.getParameter("subject");
                boolean sendPdf = Boolean.parseBoolean((String)request.getParameter("sendpdf"));
                CompanyAccountPreferences preferences = (CompanyAccountPreferences) kwlCommonTablesDAOObj.getClassObject(CompanyAccountPreferences.class.getName(), sessionHandlerImpl.getCompanyid(request));
                Company company = preferences.getCompany();
                KWLCurrency currency = (KWLCurrency) kwlCommonTablesDAOObj.getClassObject(KWLCurrency.class.getName(), sessionHandlerImpl.getCurrencyID(request));
               // double amount = 0;
                Date invDate = new Date();
                String fromID = StringUtil.isNullOrEmpty(company.getEmailID())?authHandlerDAOObj.getSysEmailIdByCompanyID(company.getCompanyID()):company.getEmailID();
                File destDir=new File("");
                String[] path = new String[]{};
                if(sendPdf){
                    String billid=request.getParameter("billid");
                    double amount=Double.parseDouble((String)request.getParameter("amount"));
                    int mode=Integer.parseInt(request.getParameter("mode"));
                    DateFormat formatter = authHandlerDAOObj.getUserDateFormatter(sessionHandlerImpl.getDateFormatID(request), sessionHandlerImpl.getTimeZoneDifference(request), true);
                    String logoPath = ProfileImageServlet.getProfileImagePath(request, true, null);
                    String currencyid = request.getParameter("currencyid")==null?currency.getCurrencyID():request.getParameter("currencyid");
                    String dateStr = "";
                    try {
                        DateFormat df = authHandler.getDateFormatter(request);
                        dateStr = df.format(invDate);
                    } catch(Exception ex) {
                    }
                    baos = ExportrecordObj.createPdf(request, currencyid, billid, formatter, mode, amount, logoPath, null, null, null,false);
                    destDir = new File(storageHandlerImpl.GetProfileImgStorePath(), "Transaction"+dateStr+".pdf");
                    FileOutputStream oss = new FileOutputStream(destDir);
                    baos.writeTo(oss);
                    path= new String[]{destDir.getAbsolutePath()};
                    //baos.close();
                }
                try {
                    if (emails.length > 0) {
                        SendMailHandler.postMail(emails, subject, plainMsg, plainMsg, fromID, path);
                        issuccess=true;
                    }
                } catch (MessagingException e) {
                    issuccess=false;
                    Logger.getLogger(CommonFunctions.class.getName()).log(Level.SEVERE, null, e);
                }
               
                } catch (SessionExpiredException ex) {
                    Logger.getLogger(CommonFnController.class.getName()).log(Level.SEVERE, null, ex);
                }   catch (Exception e) {
                      issuccess=false;
                 } finally {
                    try{
                    if(baos!=null)
                        baos.close();
                     if(os!=null)
                        os.close();
                         } catch (IOException ex) {
                    Logger.getLogger(AopAdvisor.class.getName()).log(Level.SEVERE, null, ex);
                }
                        jobj.put("success", issuccess);
                        jobj.put("msg", messageSource.getMessage("acc.rem.165", null, RequestContextUtils.getLocale(request)));
                }
        }
       return new ModelAndView("jsonView", "model", jobj.toString());
    }
     public ModelAndView getInvoiceCreationJson(HttpServletRequest request, HttpServletResponse response ) throws FileNotFoundException, IOException, DocumentException, ServiceException, JSONException {
       JSONObject jobj = new JSONObject();
        {
            ByteArrayOutputStream baos = null;
            boolean issuccess = true;
            try {
                boolean loadTaxStore= Boolean.parseBoolean((String)request.getParameter("loadtaxstore"));
                boolean loadPriceStore = Boolean.parseBoolean((String)request.getParameter("loadpricestore"));
                boolean loadCurrencyStore =  Boolean.parseBoolean((String)request.getParameter("loadcurrencystore"));
                boolean loadTermStore =  Boolean.parseBoolean((String)request.getParameter("loadtermstore"));
                ModelAndView model=null;
                Map map=null;
                String modelStr;
                JSONObject obj;
                if(loadTaxStore){
                    model=AccTaxcontrollerObj.getTax(request,response);
                    map = model.getModel();
                    modelStr = (String) map.get("model");
                    obj = new JSONObject(modelStr);
                     jobj.put("taxdata", obj);
                }
                if(loadPriceStore){
                     model=AccProductcontrollerObj.getProducts(request,response);
                     map = model.getModel();
                     modelStr = (String) map.get("model");
                     obj = new JSONObject(modelStr);
                     jobj.put("productdata", obj);
                }
                if(loadTermStore){
                     model=AccTermcontrollerObj.getTerm(request,response);
                     map = model.getModel();
                     modelStr = (String) map.get("model");
                     obj = new JSONObject(modelStr);
                     jobj.put("termdata", obj);
                }
                if(loadCurrencyStore){
                     model=AccCurrencycontrollerObj.getCurrencyExchange(request,response);
                     map = model.getModel();
                     modelStr = (String) map.get("model");
                     obj = new JSONObject(modelStr);
                     jobj.put("currencydata", obj);
                }
            }   catch (Exception e) {
                  issuccess=false;
             } finally {
             
                    jobj.put("success", issuccess);
                    jobj.put("msg", "Json Created");
            }
        }
       return new ModelAndView("jsonView", "model", jobj.toString());
    }
}
