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
import com.krawler.common.admin.KWLTimeZone;
import com.krawler.common.admin.User;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.esp.handlers.StorageHandler;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.masteritems.accMasterItemsDAO;
import com.krawler.spring.accounting.payment.accPaymentDAO;
import com.krawler.spring.accounting.tax.accTaxDAO;
import com.krawler.spring.accounting.term.accTermDAO;
import com.krawler.spring.accounting.uom.accUomDAO;
import com.krawler.spring.common.KwlReturnObject;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author krawler
 */
public class NewCompanyHandler {
    private accTermDAO accTermObj;
    private accUomDAO accUomObj;
    private accAccountDAO accAccountDAOobj;
    private accPaymentDAO accPaymentDAOobj;
    private accMasterItemsDAO accMasterItemsDAOobj;
    private accCompanyPreferencesDAO accCompanyPreferencesObj;
        private accTaxDAO accTaxObj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    
    public void setaccTermDAO(accTermDAO accTermObj) {
        this.accTermObj = accTermObj;
    }
    public void setaccUomDAO (accUomDAO accUomObj) {
        this.accUomObj = accUomObj;
    }
    public void setaccAccountDAO(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }
    public void setaccPaymentDAO(accPaymentDAO accPaymentDAOobj) {
        this.accPaymentDAOobj = accPaymentDAOobj;
    }
    public void setaccMasterItemsDAO(accMasterItemsDAO accMasterItemsDAOobj) {
        this.accMasterItemsDAOobj = accMasterItemsDAOobj;
    }
    public void setaccCompanyPreferencesDAO(accCompanyPreferencesDAO accCompanyPreferencesObj) {
        this.accCompanyPreferencesObj = accCompanyPreferencesObj;
    }
    public void setaccTaxDAO(accTaxDAO accTaxObj) {
        this.accTaxObj = accTaxObj;
    }
    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }

    public void setupNewCompany(HttpServletRequest request, Company company, User companyAdmin) throws SessionExpiredException {
		try {
            String companyid = company.getCompanyID();
            String currencyid = StorageHandler.getDefaultCurrencyID();

//            copyTerms(session,company);
//            copyUOM(session,company);
//            HashMap hmAcc=copyAccounts(session,request,company);
//            copyPaymentMethods(session, company, hmAcc);
//            copyMasterItems(session, company);
//            setAccountPreferences(session, company, hmAcc);
            accTermObj.copyTerms(companyid);
            accUomObj.copyUOM(companyid);
            KwlReturnObject kresult = accAccountDAOobj.copyAccounts(companyid, currencyid, null);
            HashMap hmAcc = (HashMap) kresult.getEntityList().get(0);
            accPaymentDAOobj.copyPaymentMethods(companyid, hmAcc);
            accMasterItemsDAOobj.copyMasterItems(companyid);
            accCompanyPreferencesObj.setAccountPreferences(companyid, hmAcc, getCurrentDate());
            accTaxObj.copyTax1099Category(companyid);
            
        } catch (ServiceException e) {
            Logger.getLogger(NewCompanyHandler.class.getName()).log(Level.WARNING, e.getMessage());
        } catch (Exception ex) {
            Logger.getLogger(NewCompanyHandler.class.getName()).log(Level.WARNING, ex.getMessage(), ex);
        }
    }

    public Date getCurrentDate() throws ServiceException {
        DateFormat sdf = new SimpleDateFormat("MMMM d, yyyy");
        String timezoneid = StorageHandler.getDefaultTimeZoneID();
//            KWLTimeZone tz = (KWLTimeZone) session.get(KWLTimeZone.class, StorageHandler.getDefaultTimeZoneID());
        KwlReturnObject kresult = accountingHandlerDAOobj.getObject(KWLTimeZone.class.getName(), timezoneid);
        KWLTimeZone tz = (KWLTimeZone) kresult.getEntityList().get(0);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT" + tz.getDifference()));
        Date curDate = new Date();
        try {
            curDate = sdf.parse(sdf.format(curDate));
        } catch (ParseException ex) {
            Logger.getLogger(NewCompanyHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return curDate;
    }
}
