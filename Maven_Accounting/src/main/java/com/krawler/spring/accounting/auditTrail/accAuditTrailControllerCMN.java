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
package com.krawler.spring.accounting.auditTrail;

import com.krawler.common.admin.AuditGroup;
import com.krawler.common.admin.AuditTrail;
import com.krawler.common.admin.User;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.StringUtil;
import com.krawler.esp.handlers.AuthHandler;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.spring.storageHandler.storageHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

/**
 *
 * @author Karthik
 */
public class accAuditTrailControllerCMN extends MultiActionController {

    private auditTrailDAO auditTrailDAOObj;
    private sessionHandlerImpl sessionHandlerImplObj;
    private String successView;

    

    public void setAuditTrailDAO(auditTrailDAO auditTrailDAOObj1) {
        this.auditTrailDAOObj = auditTrailDAOObj1;
    }

    public String getSuccessView() {
        return successView;
    }

    public void setSuccessView(String successView) {
        this.successView = successView;
    }

    public void setsessionHandlerImpl(sessionHandlerImpl sessionHandlerImplObj1) {
        this.sessionHandlerImplObj = sessionHandlerImplObj1;
    }


    public JSONObject getAuditJSONData(List ll, HttpServletRequest request, int totalSize) throws ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        try {
            Iterator itr = ll.iterator();
            JSONArray jArr = new JSONArray();
            while (itr.hasNext()) {
                AuditTrail auditTrail = (AuditTrail) itr.next();
                JSONObject obj = new JSONObject();
                obj.put("id", auditTrail.getID());
                obj.put("username", auditTrail.getUser().getUserLogin().getUserName() + " [ " +  sessionHandlerImpl.getUserFullName(request) + " ]");
                obj.put("ipaddr", auditTrail.getIPAddress());
                obj.put("details", auditTrail.getDetails());
                obj.put("timestamp", authHandler.getDateFormatter(request).format(auditTrail.getAuditTime()));
                jArr.put(obj);
            }
            jobj.put("data", jArr);
            jobj.put("count", totalSize);

        } catch (JSONException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return jobj;
    }

    public JSONObject getAuditGroupJsonData(List ll, HttpServletRequest request, int totalSize) throws ServiceException {
        JSONObject jobj = new JSONObject();
        JSONArray jArr = new JSONArray();
        try {
            JSONObject objN = new JSONObject();
            objN.put("groupid", "");
            objN.put("groupname", "--All--");
            jArr.put(objN);
            Iterator itr = ll.iterator();
            while (itr.hasNext()) {
                AuditGroup auditGroup = (AuditGroup) itr.next();
                JSONObject obj = new JSONObject();
                obj.put("groupid", auditGroup.getID());
                obj.put("groupname", auditGroup.getGroupName());
                jArr.put(obj);
            }
            jobj.put("data", jArr);
            jobj.put("count", totalSize);

        } catch (JSONException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return jobj;
    }

    public ModelAndView getAuditData(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {
        JSONObject jobj = new JSONObject();
        KwlReturnObject kmsg = null;
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("start", StringUtil.checkForNull(request.getParameter("start")));
            requestParams.put("limit", StringUtil.checkForNull(request.getParameter("limit")));
            requestParams.put("groupid", StringUtil.checkForNull(request.getParameter("groupid")));
            requestParams.put("search", StringUtil.checkForNull(request.getParameter("search")));
            requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            Date stDate=null,endDate=null;
            DateFormat df=AuthHandler.getDateFormatter(request);
            try{
                if(!StringUtil.isNullOrEmpty(request.getParameter("startdate")))stDate = df.parse(request.getParameter("startdate"));
                if(!StringUtil.isNullOrEmpty(request.getParameter("enddate")))endDate = df.parse(request.getParameter("enddate"));
            } catch (ParseException ex) {
                System.out.println(ex.getMessage());
            }
            requestParams.put("stDate", stDate);
            requestParams.put("eDate", endDate);

            kmsg = auditTrailDAOObj.getAuditData(requestParams);
            jobj = getAuditJSONData(kmsg.getEntityList(), request, kmsg.getRecordTotalCount());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getAuditGroupData(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {
        JSONObject jobj = new JSONObject();
        KwlReturnObject kmsg = null;
        try {
            kmsg = auditTrailDAOObj.getAuditGroupData();
            jobj = getAuditGroupJsonData(kmsg.getEntityList(), request, kmsg.getRecordTotalCount());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView reloadLuceneIndex(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {
        JSONObject jobj = new JSONObject();
        KwlReturnObject kmsg = null;
        try {

              kmsg = auditTrailDAOObj.reloadLuceneIndex();
              Iterator itr = kmsg.getEntityList().iterator();
              while (itr.hasNext()) {
                   AuditTrail auditTrail = (AuditTrail) itr.next();
                   ArrayList<Object> indexFieldDetails = new ArrayList<Object>();
                   ArrayList<String> indexFieldName = new ArrayList<String>();
                   indexFieldDetails.add(auditTrail.getDetails());
                   indexFieldName.add("details");
                   indexFieldDetails.add(auditTrail.getID());
                   indexFieldName.add("transactionid");
                   indexFieldDetails.add(auditTrail.getAction().getID());
                   indexFieldName.add("actionid");
                   indexFieldDetails.add(auditTrail.getIPAddress());
                   indexFieldName.add("ipaddr");
                   User user = auditTrail.getUser();
                   String userName = user.getUserLogin().getUserName()+" "+user.getFirstName()+" "+user.getLastName();
                   indexFieldDetails.add(userName);
                   indexFieldName.add("username");
                   indexFieldDetails.add(auditTrail.getAuditTime());
                   indexFieldName.add("timestamp");
                   String indexPath = storageHandlerImpl.GetAuditTrailIndexPath();
                   com.krawler.esp.indexer.KrawlerIndexCreator kwlIndex = new com.krawler.esp.indexer.KrawlerIndexCreator();
                   kwlIndex.setIndexPath(indexPath);
                   com.krawler.esp.indexer.CreateIndex  cIndex = new com.krawler.esp.indexer.CreateIndex();
                   cIndex.indexAlert(kwlIndex, indexFieldDetails, indexFieldName);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return new ModelAndView("jsonView", "model", "Realoading lucene completed");
    }
}
