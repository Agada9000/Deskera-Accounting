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

package com.krawler.spring.accounting.account;

import com.krawler.common.admin.KWLCurrency;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.StringUtil;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.hql.accounting.Account;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author krawler
 */
public class accAccountHandler {
    public static HashMap<String, Object> getRequestMap(HttpServletRequest request) throws SessionExpiredException {
        HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
        requestParams.put("group", request.getParameterValues("group"));
        requestParams.put("ignore", request.getParameter("ignore"));
        requestParams.put("ignorecustomers", request.getParameter("ignorecustomers"));
        requestParams.put("ignorevendors", request.getParameter("ignorevendors"));
        requestParams.put("ignoreAssets", request.getParameter("ignoreAssets"));
        requestParams.put("accountid", request.getParameter("accountid"));
        requestParams.put("includeaccountid", request.getParameter("includeaccountid"));
        requestParams.put("includeparentid", request.getParameter("includeparentid"));
        requestParams.put("deleted", request.getParameter("deleted"));
        requestParams.put("nondeleted", request.getParameter("nondeleted"));
        requestParams.put("nature", request.getParameterValues("nature"));
        requestParams.put("costCenterId",request.getParameter("costCenterId"));
        requestParams.put("currencyid", sessionHandlerImpl.getCurrencyID(request));
        if(request.getParameter("ss") != null && !StringUtil.isNullOrEmpty(request.getParameter("ss"))) {
            requestParams.put("ss", request.getParameter("ss"));
        }
        if(request.getParameter("start")!= null) {
            requestParams.put("start", request.getParameter("start"));
        }
        if(request.getParameter("limit")!= null) {
            requestParams.put("limit", request.getParameter("limit"));
        }
        return requestParams;
    }

    public static JSONObject getAccountJson(HttpServletRequest request, List list, accCurrencyDAO accCurrencyDAOobj) throws SessionExpiredException, ServiceException {
        JSONObject jobj = new JSONObject();
        JSONArray jArr=new JSONArray();
        try{
            Iterator itr = list.iterator();
            KwlReturnObject bAmt, presentBaseAmount;
            String currencyid = "";
            double openbalanceInbase = 0, presentbalanceInBase = 0,openbalanceSummary = 0,presentbalanceSummary = 0;
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            while (itr.hasNext()) {
                openbalanceInbase = 0; 
                presentbalanceInBase = 0;
                Object[] row = (Object[]) itr.next();
                Account account = (Account) row[0];
                JSONObject obj = new JSONObject();
                obj.put("accid", account.getID());
                obj.put("accname", account.getName());
                obj.put("groupid", account.getGroup().getID());
                obj.put("groupname", account.getGroup().getName());
                obj.put("nature", account.getGroup().getNature());
                obj.put("openbalance", account.getOpeningBalance());
                KWLCurrency currency = (KWLCurrency) row[5];
                currencyid = account.getCurrency()==null?currency.getCurrencyID(): account.getCurrency().getCurrencyID();
                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, account.getOpeningBalance(), currencyid, account.getCreationDate(), 0);
                openbalanceInbase = authHandler.round((Double) bAmt.getEntityList().get(0),2);
                obj.put("openbalanceinbase", openbalanceInbase);
                presentBaseAmount = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, account.getPresentValue(), currencyid, account.getCreationDate(), 0);
                presentbalanceInBase = authHandler.round((Double) presentBaseAmount.getEntityList().get(0),2);
                obj.put("presentbalanceInBase", presentbalanceInBase);
                obj.put("depreciationaccount", account.getDepreciationAccont()==null?"":account.getDepreciationAccont().getID());
                Account parentAccount = (Account) row[6];
                if(parentAccount!=null){
                    obj.put("parentid", parentAccount.getID());
                    obj.put("parentname", parentAccount.getName());
                }
                obj.put("currencyid", currencyid);
                obj.put("currencysymbol",(account.getCurrency()==null?currency.getCurrencyID(): account.getCurrency().getSymbol()));
                obj.put("currencyname",(account.getCurrency()==null?currency.getName(): account.getCurrency().getName()));
                obj.put("level", row[3]);
                obj.put("leaf", row[4]);
                obj.put("presentbalance", account.getPresentValue());
                obj.put("life", account.getLife());
                obj.put("salvage", account.getSalvage());
                obj.put("deleted", account.isDeleted());
                obj.put("posted", row[7]);
                obj.put("creationDate", authHandler.getDateFormatter(request).format(account.getCreationDate()));
                obj.put("categoryid", account.getCategory()==null?"":account.getCategory().getID());
                obj.put("costcenterid", account.getCostcenter()==null?"":account.getCostcenter().getID());
                obj.put("costcenterName", account.getCostcenter()==null?"":account.getCostcenter().getName());
                if(!account.isDeleted()){
                /*    openbalanceSummary += presentbalanceInBase;    */ openbalanceSummary += openbalanceInbase;
                	presentbalanceSummary += presentbalanceInBase;
                }
                jArr.put(obj);
            }
            jobj.put("data", jArr);
            jobj.put("openbalanceSummary", openbalanceSummary);
            jobj.put("presentbalanceSummary", presentbalanceSummary);
        } catch (JSONException ex) {
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("getAccountJson : "+ex.getMessage(), ex);
        }
        return jobj;
    }
}
