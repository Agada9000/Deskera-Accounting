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
package com.krawler.spring.accounting.customer;

import com.krawler.common.admin.Company;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.StringUtil;
import com.krawler.esp.hibernate.impl.HibernateUtil;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.hql.accounting.Account;
import com.krawler.hql.accounting.Customer;
import com.krawler.hql.accounting.StaticValues;
import com.krawler.hql.accounting.Term;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.HibernateTemplate;

/**
 *
 * @author krawler
 */
    public class accCustomerDAOImpl implements accCustomerDAO{
    private HibernateTemplate hibernateTemplate;
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.hibernateTemplate = new HibernateTemplate(sessionFactory);
	}

    public KwlReturnObject addCustomer(HashMap request) {
        KwlReturnObject result;
        try{
            Customer customer = new Customer();
            customer.setCreatedOn(new Date());
            if(request.containsKey("accountid")){
                customer.setAccount((Account) hibernateTemplate.get(Account.class, (String)request.get("accountid")));
            }
            if(request.containsKey("title")){
                customer.setTitle((String)request.get("title"));
            }
            if(request.containsKey("accname")){
                customer.setName((String)request.get("accname"));
            }
            if(request.containsKey("address")){
                customer.setBillingAddress((String)request.get("address"));
            }
            if(request.containsKey("bankaccountno")){
                customer.setBankaccountno((String)request.get("bankaccountno"));
            }
            if(request.containsKey("email")){
                customer.setEmail((String)request.get("email"));
            }
            if(request.containsKey("contactno")){
                customer.setContactNumber((String)request.get("contactno"));
            }
            if(request.containsKey("contactno2")){
                customer.setAltContactNumber((String)request.get("contactno2"));
            }
            if(request.containsKey("fax")){
                customer.setFax((String)request.get("fax"));
            }
            if(request.containsKey("shippingaddress")){
                customer.setShippingAddress((String)request.get("shippingaddress"));
            }
            if(request.containsKey("other")){
                customer.setOther((String)request.get("other"));
            }
            if(request.containsKey("taxno")){
                customer.setTaxNo((String)request.get("taxno"));
            }
            if(request.containsKey("termid")){
                customer.setCreditTerm((Term) hibernateTemplate.get(Term.class, (String)request.get("termid")));
            }
            if(request.containsKey("companyid")){
                customer.setCompany((Company) hibernateTemplate.get(Company.class, (String)request.get("companyid")));
            }
            customer.setPreferedDeliveryMode(StaticValues.PDM_EMAIL);
//            if (Integer.parseInt(request.getParameter("pdm")) == StaticValues.PDM_PRINT) {
//                customer.setPreferedDeliveryMode(StaticValues.PDM_PRINT);
//            }
            
            hibernateTemplate.saveOrUpdate(customer);

            List resultlist = new ArrayList();
            resultlist.add(customer);
            result = new KwlReturnObject(true, null, null, resultlist, resultlist.size());
        } catch(Exception ex){
            result = new KwlReturnObject(false, "accCustomerDAOImpl.addCustomer:" + ex.getMessage(), null, null, 0);
        }
        return result;
    }

    public KwlReturnObject updateCustomer(HashMap request) {
        KwlReturnObject result;
        try{
            String customerid = (String)request.get("accid");
            Customer customer = (Customer) hibernateTemplate.get(Customer.class, customerid);
            customer.setModifiedOn(new Date());
            if(request.containsKey("accountid")){
                customer.setAccount((Account) hibernateTemplate.get(Account.class, (String)request.get("accountid")));
            }
            if(request.containsKey("title")){
                customer.setTitle((String)request.get("title"));
            }
            if(request.containsKey("accname")){
                customer.setName((String)request.get("accname"));
            }
            if(request.containsKey("address")){
                customer.setBillingAddress((String)request.get("address"));
            }
            if(request.containsKey("bankaccountno")){
                customer.setBankaccountno((String)request.get("bankaccountno"));
            }
            if(request.containsKey("email")){
                customer.setEmail((String)request.get("email"));
            }
            if(request.containsKey("contactno")){
                customer.setContactNumber((String)request.get("contactno"));
            }
            if(request.containsKey("contactno2")){
                customer.setAltContactNumber((String)request.get("contactno2"));
            }
            if(request.containsKey("fax")){
                customer.setFax((String)request.get("fax"));
            }
            if(request.containsKey("shippingaddress")){
                customer.setShippingAddress((String)request.get("shippingaddress"));
            }
            if(request.containsKey("other")){
                customer.setOther((String)request.get("other"));
            }
            if(request.containsKey("taxno")){
                customer.setTaxNo((String)request.get("taxno"));
            }
            if(request.containsKey("termid")){
                customer.setCreditTerm((Term) hibernateTemplate.get(Term.class, (String)request.get("termid")));
            }
            if(request.containsKey("companyid")){
                customer.setCompany((Company) hibernateTemplate.get(Company.class, (String)request.get("companyid")));
            }
            customer.setPreferedDeliveryMode(StaticValues.PDM_EMAIL);
//            if (Integer.parseInt(request.getParameter("pdm")) == StaticValues.PDM_PRINT) {
//                customer.setPreferedDeliveryMode(StaticValues.PDM_PRINT);
//            }
            hibernateTemplate.saveOrUpdate(customer);

            List resultlist = new ArrayList();
            resultlist.add(customer);
            result = new KwlReturnObject(true, null, null, resultlist, resultlist.size());
        } catch(Exception ex){
            result = new KwlReturnObject(false, "accCustomerDAOImpl.updateCustomer:" + ex.getMessage(), null, null, 0);
        }
        return result;
    }

//    public KwlReturnObject getCustomerObj(String customerid) {
//        List list = new ArrayList();
//        Customer customer = (Customer) hibernateTemplate.get(Customer.class, customerid);
//        list.add(customer);
//        return new KwlReturnObject(true, null, null, list, list.size());
//    }

    public KwlReturnObject getCustomer_Dashboard(String companyid, boolean isnull, String orderby, int start, int limit) throws ServiceException {
        List list = new ArrayList();
        String condition = isnull ? "modifiedOn is null" : "modifiedOn is not null";

//        query="from Customer where company.companyID=? and modifiedOn is null order by createdOn";
//        query="from Customer where company.companyID=? and modifiedOn is not null order by modifiedOn";

        String query="from Customer where company.companyID=? and "+condition+" order by "+orderby+" desc";
        list = HibernateUtil.executeQueryPaging(hibernateTemplate, query, new Object[]{companyid}, new Integer[]{start, limit});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject deleteCustomer(String accountid, String companyid) throws ServiceException {
        String delQuery = "delete from Customer c where c.ID=? and c.company.companyID=?";
        int numRows = HibernateUtil.executeUpdate(hibernateTemplate, delQuery, new Object[]{accountid,companyid});
        return new KwlReturnObject(true, "Customer has been deleted successfully.", null, null, numRows);
    }

    public KwlReturnObject getCustomerForAgedReceivable(HashMap request) throws ServiceException {
        List list = new ArrayList();
        ArrayList params = new ArrayList();
        params.add(request.get("companyid"));
        String condition = "";
        String customerid = (String) request.get("accid");
        if (!StringUtil.isNullOrEmpty(customerid)) {
            params.add(customerid);
            condition += " and c.account.ID=? ";
        }
        String query = "select ID from Customer c where company.companyID= ?" + condition;
        list = HibernateUtil.executeQuery(hibernateTemplate, query, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }


    public KwlReturnObject getCustomer(HashMap<String, Object> requestParams) throws ServiceException {
        KwlReturnObject result;
        try{
            int start = 0;
            int limit = 15;
            boolean pagingFlag = false;
            if (requestParams.containsKey("start") && requestParams.containsKey("limit") && !StringUtil.isNullOrEmpty(requestParams.get("start").toString())) {
                start = Integer.parseInt(requestParams.get("start").toString());
                limit = Integer.parseInt(requestParams.get("limit").toString());
                pagingFlag = true;
            }

            ArrayList params=new ArrayList();
            String[] groups=(String[]) requestParams.get("group");

            String condition=(requestParams.get("ignore")==null?"":" not ");
            String[] nature=(String[])requestParams.get("nature");
            String companyid=(String)requestParams.get("companyid");
            boolean deleted =Boolean.parseBoolean((String)requestParams.get("deleted"));
            boolean nondeleted =Boolean.parseBoolean((String)requestParams.get("nondeleted"));

            if(groups!=null){
                String qMarks="?";
                params.add("null");
                 for(int i=0;i<groups.length;i++){
                    qMarks+=",?";
                    params.add(groups[i]);
                }
                condition=" and ct.account.group.ID "+condition+" in ("+qMarks+") ";
            }
            else if(nature!=null){
                String qMarks="?";
                params.add(5);//not a nature
                 for(int i=0;i<nature.length;i++){
                    qMarks+=",?";
                    params.add(Integer.parseInt(nature[i]));
                }
                condition+=" and ct.account.group.nature "+condition+" in ("+qMarks+") ";
            }
            if(requestParams.containsKey("ss") && requestParams.get("ss") != null) {
                String ss=requestParams.get("ss").toString();
                ss = ss.replaceAll("%", "////");		// issue for search
                ss = ss.replaceAll("_", "////");
                if(!StringUtil.isNullOrEmpty(ss)){
                    String[] searchcol = new String[]{"ct.name"};
                    StringUtil.insertParamSearchString(params, ss, 1);
                    String searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                    condition +=searchQuery;
                }
            }
            if (nondeleted)
                condition += " and ct.account.deleted=false ";
            else if (deleted)
                condition += " and ct.account.deleted=true ";
            params.add(companyid);

            String query="select ct.account from Customer ct where ct.account.parent is null "+condition+" and ct.company.companyID=? order by ct.name";
            List list = HibernateUtil.executeQuery(hibernateTemplate, query,params.toArray());
            int totalCount = list.size();
            if(pagingFlag) {
                list = HibernateUtil.executeQueryPaging(hibernateTemplate, query, params.toArray(), new Integer[]{start, limit});
            }
            result = new KwlReturnObject(true, null, null, list, totalCount);
        }catch(Exception ex){
            result = new KwlReturnObject(false,"accAccountDAOImpl.getAccounts:"+ex.getMessage(), null, null ,0);
        }
        return result;
    }

    public KwlReturnObject getCustomersForCombo(HashMap<String, Object> requestParams) throws ServiceException {
        KwlReturnObject result;
        try{
            ArrayList params=new ArrayList();
            String[] groups=(String[]) requestParams.get("group");

            String condition=(requestParams.get("ignore")==null?"":" not ");
            String[] nature=(String[])requestParams.get("nature");
            String companyid=(String)requestParams.get("companyid");
            boolean deleted =Boolean.parseBoolean((String)requestParams.get("deleted"));
            boolean nondeleted =Boolean.parseBoolean((String)requestParams.get("nondeleted"));
            params.add(companyid);
            if(groups!=null){
                String qMarks="?";
                params.add("null");
                 for(int i=0;i<groups.length;i++){
                    qMarks+=",?";
                    params.add(groups[i]);
                }
                condition=" and ct.account.group.ID "+condition+" in ("+qMarks+") ";
            }
            else if(nature!=null){
                String qMarks="?";
                params.add(5);//not a nature
                 for(int i=0;i<nature.length;i++){
                    qMarks+=",?";
                    params.add(Integer.parseInt(nature[i]));
                }
                condition+=" and ct.account.group.nature "+condition+" in ("+qMarks+") ";
            }
            if (nondeleted)
                condition += " and ct.account.deleted=false ";
            else if (deleted)
                condition += " and ct.account.deleted=true ";
            

            String query="select ct.account, ct from Customer ct where ct.company.companyID=? "+condition+" order by ct.name";
            List list = HibernateUtil.executeQuery(hibernateTemplate, query,params.toArray());
            int totalCount = list.size();
            result = new KwlReturnObject(true, null, null, list, totalCount);
        }catch(Exception ex){
            result = new KwlReturnObject(false,"accAccountDAOImpl.getAccounts:"+ex.getMessage(), null, null ,0);
        }
        return result;
    }

    @Override
    public KwlReturnObject getCustomerList(HashMap<String, Object> requestParams) throws ServiceException{
        String initialQuery = "from Customer";
        return StringUtil.buildNExecuteQuery(hibernateTemplate, initialQuery, requestParams);
    }
    }
