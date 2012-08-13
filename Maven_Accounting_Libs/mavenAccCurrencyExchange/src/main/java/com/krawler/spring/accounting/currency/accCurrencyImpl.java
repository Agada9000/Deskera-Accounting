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
package com.krawler.spring.accounting.currency;

import com.krawler.common.admin.Company;
import com.krawler.common.service.ServiceException;
import com.krawler.esp.hibernate.impl.HibernateUtil;
import com.krawler.hql.accounting.ExchangeRate;
import com.krawler.hql.accounting.ExchangeRateDetails;
import com.krawler.spring.common.KwlReturnObject;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.HibernateTemplate;

/**
 *
 * @author krawler
 */
public class accCurrencyImpl implements accCurrencyDAO{
    private HibernateTemplate hibernateTemplate;
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.hibernateTemplate = new HibernateTemplate(sessionFactory);
	}

    public KwlReturnObject getExcDetailID(Map request, String currencyid, Date transactiondate, String erid) throws ServiceException {
        List list = new ArrayList();
        ExchangeRateDetails erd = null;
        try {
            String condition = "";
            ArrayList params = new ArrayList();
//            params.add(AuthHandler.getCurrencyID(request));
            params.add(request.get("gcurrencyid"));
            params.add(currencyid);
            if (erid == null) {
                String erIDQuery = "select ID from ExchangeRate where fromCurrency.currencyID=? and toCurrency.currencyID=? ";
                List erIDList = HibernateUtil.executeQuery(hibernateTemplate, erIDQuery, params.toArray());
                Iterator erIDitr = erIDList.iterator();
                erid = (String) erIDitr.next();
            }
            params = new ArrayList();
//            params.add(AuthHandler.getCompanyid(request));
            params.add(request.get("companyid"));
            params.add(erid);
            if (transactiondate != null) {
                params.add(transactiondate);
                condition += " and applyDate <= ?  ";
            }
            String applyDateQuery = "select max(erd.applyDate) from ExchangeRateDetails erd where erd.company.companyID=? and  erd.exchangeratelink.ID = ? " + condition;
            List applyDateList = HibernateUtil.executeQuery(hibernateTemplate, applyDateQuery, params.toArray());
            Iterator itr = applyDateList.iterator();

            Date maxDate = (Date) itr.next();
            params = new ArrayList();
            params.add(maxDate);
            params.add(erid);
//            params.add(AuthHandler.getCompanyid(request));
            params.add(request.get("companyid"));

            String erdIDQuery = "from ExchangeRateDetails erd where erd.applyDate=? and erd.exchangeratelink.ID=? and erd.company.companyID=?";
            List erdIDList = HibernateUtil.executeQuery(hibernateTemplate, erdIDQuery, params.toArray());
            Iterator erdIDItr = erdIDList.iterator();
            erd = (ExchangeRateDetails) erdIDItr.next();
        } catch (ServiceException ex) {
            throw ServiceException.FAILURE("accCurrencyImpl.getExcDetailID : " + ex.getMessage(), ex);
        } finally {
            list.add(erd);
            return new KwlReturnObject(true, null, null, list, list.size());
        }
    }

    public KwlReturnObject getCurrencyToBaseAmount(Map request, Double Amount, String currencyid, Date transactiondate, double rate) throws ServiceException {
        List list = new ArrayList();
        try{
            if (Amount != 0) {
                if (rate == 0) {
                    KwlReturnObject result = getExcDetailID(request, currencyid, transactiondate, null);
                    List li = result.getEntityList();
                    if (!li.isEmpty()) {
                        Iterator itr = li.iterator();
                        ExchangeRateDetails erd = (ExchangeRateDetails) itr.next();
                        rate = erd.getExchangeRate();
                    }
                }
                Amount = Amount / rate;
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accCurrencyImpl.getCurrencyToBaseAmount : "+ex.getMessage(), ex);
        } finally {
            list.add(Amount);
            return new KwlReturnObject(true, null, null, list, list.size());
        }
    }

    public KwlReturnObject getBaseToCurrencyAmount(Map request, Double Amount, String newcurrencyid, Date transactiondate, double rate) throws ServiceException {
        List list = new ArrayList();
        try{
            if (Amount != 0) {
                if (rate == 0) {
                    KwlReturnObject result = getExcDetailID(request, newcurrencyid, transactiondate, null);
                    List li = result.getEntityList();
                    if (!li.isEmpty()) {
                        Iterator itr = li.iterator();
                        ExchangeRateDetails erd = (ExchangeRateDetails) itr.next();
                        rate = erd.getExchangeRate();
                    }
                }
                Amount = Amount * rate;
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accCurrencyImpl.getBaseToCurrencyAmount : "+ex.getMessage(), ex);
        } finally {
            list.add(Amount);
            return new KwlReturnObject(true, null, null, list, list.size());
        }
    }
    
    public KwlReturnObject getOneCurrencyToOther(Map request, Double Amount, String oldcurrencyid, String newcurrencyid, Date transactiondate, double rate) throws ServiceException {
        List list = new ArrayList();
        Double currencyAmount=0.0;
        try{
            if(Amount != 0) {
                KwlReturnObject bAmt = getCurrencyToBaseAmount(request, Amount, oldcurrencyid, transactiondate, rate);
                Double baseAmount = (Double) bAmt.getEntityList().get(0);
                bAmt = getBaseToCurrencyAmount(request, baseAmount, newcurrencyid, transactiondate, rate);
                currencyAmount = (Double) bAmt.getEntityList().get(0);
            }
            list.add(currencyAmount);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accCurrencyImpl.getOneCurrencyToOther : "+ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    public KwlReturnObject getCurrencyExchange(Map<String, Object> filterParams) throws ServiceException {
		List returnList = new ArrayList();
        ArrayList params=new ArrayList();
        String condition = "";
        String query="from ExchangeRate ";

        if(filterParams.containsKey("fromcurrencyid")){
            condition += (condition.length()==0?" where ":" and ") + "fromCurrency.currencyID=?";
            params.add(filterParams.get("fromcurrencyid"));
        }
        if(filterParams.containsKey("tocurrencyid")){
            condition += (condition.length()==0?" where ":" and ") + "toCurrency.currencyID=?";
            params.add(filterParams.get("tocurrencyid"));
        }
        query += condition;
//        query="select ID from ExchangeRate where fromCurrency.currencyID=?";
        returnList = HibernateUtil.executeQuery(hibernateTemplate, query, params.toArray());
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }

    public KwlReturnObject getExchangeRateDetails(Map<String, Object> filterParams, boolean doSort) throws ServiceException {
		List returnList = new ArrayList();
        ArrayList params=new ArrayList();
        String condition = "";
        String query="from ExchangeRateDetails ";

        if(filterParams.containsKey("applydate")){
            condition += (condition.length()==0?" where ":" and ") + "applyDate=?";
            params.add(filterParams.get("applydate"));
        }
        if(filterParams.containsKey("erid")){
            condition += (condition.length()==0?" where ":" and ") + "exchangeratelink.ID=?";
            params.add(filterParams.get("erid"));
        }
        if(filterParams.containsKey("companyid")){
            condition += (condition.length()==0?" where ":" and ") + "company.companyID=?";
            params.add(filterParams.get("companyid"));
        }
        query += condition;
        if (doSort) {
            query += " order by applyDate asc";
        }
//        query="from ExchangeRateDetails where applyDate=? and exchangeratelink.ID=? and company.companyID=?";
//        query="from ExchangeRateDetails where exchangeratelink.ID=? and company.companyID=? order by applyDate asc";
        returnList = HibernateUtil.executeQuery(hibernateTemplate, query, params.toArray());
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }

    public KwlReturnObject getDefaultCurrencyExchange(Map<String, Object> requestParams) throws ServiceException {
        List returnList = new ArrayList();
        String currencyid = (String) requestParams.get("gcurrencyid");
        Date transactiondate = (Date) requestParams.get("transactiondate");
        ArrayList params = new ArrayList();
        String condition = "";
        if (transactiondate != null) {
            params.add(transactiondate);
            condition += " and applyDate <= ?  ";
        }
        params.add(currencyid);
        String query = "select er,erd from DefaultExchangeRateDetails erd, DefaultExchangeRate er where erd.exchangeratelink.ID=er.ID and" +
                " applyDate in (select max(applyDate) from DefaultExchangeRateDetails where exchangeratelink.ID=erd.exchangeratelink.ID " + condition + " group by exchangeratelink )" +
                " and fromCurrency=? order by toCurrency desc";
        returnList = HibernateUtil.executeQuery(hibernateTemplate, query, params.toArray());
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }


    public KwlReturnObject addExchangeRateDetails(Map<String, Object> erdMap) throws ServiceException {
        List list = new ArrayList();
        try {
            ExchangeRateDetails erd = new ExchangeRateDetails();
            erd = buildExchangeRateDetails(erd, erdMap);
            hibernateTemplate.save(erd);
            list.add(erd);
        } catch (Exception e) {
            throw ServiceException.FAILURE("addExchangeRateDetails : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "Exchange Rate Details has been added successfully", null, list, list.size());
    }

    public KwlReturnObject updateExchangeRateDetails(Map<String, Object> erdMap) throws ServiceException {
        List list = new ArrayList();
        try {
            String erdid = (String) erdMap.get("erdid");
            ExchangeRateDetails erd = (ExchangeRateDetails) hibernateTemplate.get(ExchangeRateDetails.class, erdid);
            if(erd != null) {
                erd = buildExchangeRateDetails(erd, erdMap);
            }
            hibernateTemplate.save(erd);
            list.add(erd);
        } catch (Exception e) {
            throw ServiceException.FAILURE("updateExchangeRateDetails : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "Exchange Rate Details has been updated successfully", null, list, list.size());
    }

    public ExchangeRateDetails buildExchangeRateDetails(ExchangeRateDetails erd, Map<String, Object> erdMap) {
        if (erdMap.containsKey("exchangerate")) {
            erd.setExchangeRate((Double) erdMap.get("exchangerate"));
        }
        if (erdMap.containsKey("applydate")) {
            erd.setApplyDate((Date) erdMap.get("applydate"));
        }
        if (erdMap.containsKey("erid")) {
            ExchangeRate er = erdMap.get("erid")==null?null:(ExchangeRate)hibernateTemplate.get(ExchangeRate.class, (String) erdMap.get("erid"));
            erd.setExchangeratelink(er);
        }
        if (erdMap.containsKey("companyid")) {
            Company company = erdMap.get("companyid")==null?null:(Company)hibernateTemplate.get(Company.class, (String) erdMap.get("companyid"));
            erd.setCompany(company);
        }
        return erd;
    }

	@Override
	public KwlReturnObject getCurrencies() throws ServiceException {
		String query = "from KWLCurrency";
		List list = HibernateUtil.executeQuery(query);
		return new KwlReturnObject(true, "", "", list, list.size());
	}
    
    
}
