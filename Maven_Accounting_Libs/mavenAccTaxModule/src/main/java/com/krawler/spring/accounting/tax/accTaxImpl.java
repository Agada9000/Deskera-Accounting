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
package com.krawler.spring.accounting.tax;

import com.krawler.common.admin.Company;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.StringUtil;
import com.krawler.esp.hibernate.impl.HibernateUtil;
import com.krawler.hql.accounting.Account;
import com.krawler.hql.accounting.DefaultTax1099Category;
import com.krawler.hql.accounting.Tax;
import com.krawler.hql.accounting.Tax1099Accounts;
import com.krawler.hql.accounting.Tax1099Category;
import com.krawler.hql.accounting.TaxList;
import com.krawler.spring.common.KwlReturnObject;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.HibernateTemplate;

/**
 *
 * @author krawler
 */
public class accTaxImpl implements accTaxDAO,TaxConstants {

    private HibernateTemplate hibernateTemplate;
    public void setSessionFactory(SessionFactory sessionFactory){
        this.hibernateTemplate = new HibernateTemplate(sessionFactory);
    }


    public KwlReturnObject getTax(Map<String, Object> requestParams) throws ServiceException {
        String condition = "";
        String maincondition = "";
        ArrayList params = new ArrayList();
        Date transactiondate = (Date) requestParams.get(TRANSACTIONDATE);
        String taxID = (String) requestParams.get(TAXID);
        if (transactiondate != null) {
            condition += " and tl1.applyDate <= ?  ";
            params.add(transactiondate);
            params.add(transactiondate);
        }
        params.add((String) requestParams.get(COMPANYID));
         if (taxID != null) {
            maincondition += " and t.ID= ?  ";
            params.add(taxID);

        }
        String query = "select t," +
                "(select tl1.percent from TaxList tl1 where tax.ID=t.ID  and applyDate in (select max(applyDate) as ld from TaxList where tax.ID=tl1.tax.ID " + condition + " group by tax))," +
                "(select max(tl1.applyDate) from TaxList tl1 where tax.ID=t.ID  and applyDate in (select max(applyDate) as ld from TaxList where tax.ID=tl1.tax.ID " + condition + " group by tax))" +
                "from Tax t where company.companyID=? and t.deleted=false "+maincondition+"  order by t.name ";
        List list = HibernateUtil.executeQuery(hibernateTemplate, query, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }
   public KwlReturnObject  getTax1099Category (Map<String, Object> requestParams) throws ServiceException {
       String query = "from Tax1099Category t where t.deleted=false and company.companyID=? order by t.srno ";
        List list = HibernateUtil.executeQuery(hibernateTemplate, query, requestParams.get(COMPANYID));
        return new KwlReturnObject(true, "", null, list, list.size());
    }
   
    public void copyTax1099Category(String companyid) throws ServiceException {
        try {
            String query = "from DefaultTax1099Category";
            List<DefaultTax1099Category> list = HibernateUtil.executeQuery(hibernateTemplate, query);
            Company company = (Company)hibernateTemplate.get(Company.class, companyid);
            if (list != null){
                for ( DefaultTax1099Category defaulttax: list){
                    Tax1099Category tax = new Tax1099Category();
                    tax.setCompany(company);
                    tax.setCategory(defaulttax.getCategory());
                    tax.setDeleted(defaulttax.isDeleted());
                    tax.setSrno(defaulttax.getSrno());
                    tax.setThresholdValue(defaulttax.getThresholdValue());
                    hibernateTemplate.save(tax);
                }
            }
        }  catch (Exception ex) {
            throw ServiceException.FAILURE("copyTax1099Category : "+ex.getMessage(), ex);
        }
    }
      public KwlReturnObject  getTax1099AccCategory (Map<String, Object> requestParams) throws ServiceException {
        List list = new ArrayList();
       try {
            ArrayList params = new ArrayList();
            params.add(requestParams.get(COMPANYID));
            String condition ="";
            if(requestParams.containsKey(ACCOUNTID)){
                params.add(requestParams.get(ACCOUNTID));
                condition = " and ta.account.ID =? ";
            }

        String query = "select t from Tax1099Accounts  ta inner join  ta.tax1099Category t where ta.company.companyID=? and t.deleted=false "+condition;

        list = HibernateUtil.executeQuery(hibernateTemplate, query, params.toArray());
         } catch (Exception ex) {
            throw ServiceException.FAILURE("accVendorPaymentImpl.getTax1099AccCategory : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

   public KwlReturnObject belongsTo1099(String companyid,ArrayList accIDArr) throws ServiceException {
		List list = new ArrayList();
        int count = 0;
        try {
            String condition = "";
            ArrayList params=new ArrayList();
            params.add(companyid);
            if(!accIDArr.isEmpty()){
                condition = " and t.account.ID in (";
                for (int i = 0; i < accIDArr.size(); i++) {
                    params.add(accIDArr.get(i));
                    condition += " ?,";
                }
                condition=condition.substring(0,Math.max(0, condition.length()-1));
                condition += " )";
            }
            String query = "from Tax1099Accounts  t where t.company.companyID=? " + condition;
            list = HibernateUtil.executeQuery(hibernateTemplate, query, params.toArray());
            count = list.size();
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accVendorPaymentImpl.belongsTo1099 : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, count);
   }


   public KwlReturnObject getTaxCategoryAccount(String companyid,String taxid) throws ServiceException {
        List list = new ArrayList();
        int count = 0;
        try {
            ArrayList params = new ArrayList();
            params.add(companyid);
            params.add(taxid);
            String condition = " where t.company.companyID=? and t.tax1099Category.ID=? ";
            String query = "select account from Tax1099Accounts  t " + condition;
            list = HibernateUtil.executeQuery(hibernateTemplate, query, params.toArray());
            count = list.size();
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accVendorPaymentImpl.getTaxCategoryAccount : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, count);
    }

    public KwlReturnObject addTax(Map<String, Object> taxMap) throws ServiceException {
        List list = new ArrayList();
        try {
            Tax tax = new Tax();
            tax.setDeleted(false);
            tax = buildTax(tax, taxMap);
            hibernateTemplate.save(tax);
            list.add(tax);
        } catch (Exception e) {
            throw ServiceException.FAILURE("addTax : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "Tax has been added successfully", null, list, list.size());
    }

    public KwlReturnObject updateTax(Map<String, Object> taxMap) throws ServiceException {
        List list = new ArrayList();
        try {
            String taxid = (String) taxMap.get(TAXID);
            Tax tax = (Tax) hibernateTemplate.get(Tax.class, taxid);
            if(tax != null) {
                tax = buildTax(tax, taxMap);
                hibernateTemplate.save(tax);
            }
            list.add(tax);
        } catch (Exception e) {
            throw ServiceException.FAILURE("updateTax : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "Tax has been updated successfully", null, list, list.size());
    }

    public Tax buildTax(Tax tax, Map<String, Object> taxMap) throws ServiceException {
        try {
            if (taxMap.containsKey("taxname")) {
                tax.setName((String) taxMap.get("taxname"));
            }
            if (taxMap.containsKey("taxcode")) {
                tax.setTaxCode((String) taxMap.get("taxcode"));
            }
            if (taxMap.containsKey(ACCOUNTID)) {
                Account account = taxMap.get(ACCOUNTID)==null?null:(Account)hibernateTemplate.get(Account.class, (String) taxMap.get(ACCOUNTID));
                tax.setAccount(account);
            }
            if (taxMap.containsKey(COMPANYID)) {
                Company company = taxMap.get(COMPANYID)==null?null:(Company)hibernateTemplate.get(Company.class, (String) taxMap.get(COMPANYID));
                tax.setCompany(company);
            }
        } catch (Exception e) {
            throw ServiceException.FAILURE("buildTax : " + e.getMessage(), e);
        }
        return tax;
    }

    public KwlReturnObject deleteTax(String taxid, String companyid) throws ServiceException {
        String delQuery = "delete from Tax  where ID=? and company.companyID=?";
        int numRows = HibernateUtil.executeUpdate(hibernateTemplate, delQuery,new Object[]{taxid, companyid});
        return new KwlReturnObject(true, "Tax has been deleted successfully.", null, null, numRows);
    }


    public KwlReturnObject addTaxList(Map<String, Object> taxListMap) throws ServiceException {
        List list = new ArrayList();
        try {
            TaxList taxlist = new TaxList();
            taxlist = buildTaxList(taxlist, taxListMap);
            hibernateTemplate.save(taxlist);
            list.add(taxlist);
        } catch (Exception e) {
            throw ServiceException.FAILURE("addTaxList : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "Tax List has been added successfully", null, list, list.size());
    }

    public KwlReturnObject updateTaxList(Map<String, Object> taxListMap) throws ServiceException {
       List list = new ArrayList();
        try {
            String taxlistid = (String) taxListMap.get("taxlistid");
            TaxList taxlist = (TaxList) hibernateTemplate.get(TaxList.class, taxlistid);
            if(taxlist != null) {
                taxlist = buildTaxList(taxlist, taxListMap);
                hibernateTemplate.save(taxlist);
            }
            list.add(taxlist);
        } catch (Exception e) {
            throw ServiceException.FAILURE("updateTaxList : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "Tax List has been updated successfully", null, list, list.size());
    }

      public TaxList buildTaxList(TaxList taxlist, Map<String, Object> taxListMap) throws ServiceException {
        try {
            if (taxListMap.containsKey(APPLYDATE)) {
                taxlist.setApplyDate((Date) taxListMap.get(APPLYDATE));
            }
            if (taxListMap.containsKey(PERCENT)) {
                taxlist.setPercent((Double) taxListMap.get(PERCENT));
            }
            if (taxListMap.containsKey(TAXID)) {
                Tax tax = taxListMap.get(TAXID)==null?null:(Tax)hibernateTemplate.get(Tax.class, (String) taxListMap.get(TAXID));
                taxlist.setTax(tax);
            }
            if (taxListMap.containsKey(COMPANYID)) {
                Company company = taxListMap.get(COMPANYID)==null?null:(Company)hibernateTemplate.get(Company.class, (String) taxListMap.get(COMPANYID));
                taxlist.setCompany(company);
            }
        } catch (Exception e) {
            throw ServiceException.FAILURE("buildTaxList : " + e.getMessage(), e);
        }
        return taxlist;
    }
    public KwlReturnObject updateTax1099Category(Map<String, Object> taxCategoryMap) throws ServiceException {
        List list = new ArrayList();
        try {
            String categoryid = (String) taxCategoryMap.get(CATEGORYID);
            Tax1099Category taxcategorylist = (Tax1099Category) hibernateTemplate.get(Tax1099Category.class, categoryid);
            if(taxcategorylist != null) {
                taxcategorylist = buildTax1099CategoryList(taxcategorylist, taxCategoryMap);
                hibernateTemplate.save(taxcategorylist);
            }
            list.add(taxcategorylist);
        } catch (Exception e) {
            throw ServiceException.FAILURE("updateTaxList : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "Tax List has been updated successfully", null, list, list.size());
    }



    public Tax1099Category buildTax1099CategoryList(Tax1099Category taxcategory, Map<String, Object> taxCategoryMap) throws ServiceException {
        try {
            if (taxCategoryMap.containsKey("thresholdvalue")) {
                taxcategory.setThresholdValue((Double) taxCategoryMap.get("thresholdvalue"));
            }
        } catch (Exception e) {
            throw ServiceException.FAILURE("buildTaxList : " + e.getMessage(), e);
        }
        return taxcategory;
    }

        public KwlReturnObject updateTax1099Account(Map<String, Object> taxCategoryAccountMap) throws ServiceException {
        List list = new ArrayList();
        try {
            String categoryid = (String) taxCategoryAccountMap.get(CATEGORYID);
            String companyid = (String) taxCategoryAccountMap.get(COMPANYID);
            Tax1099Accounts taxcategoryacc =null;
            if(categoryid != null) {
              //  deleteTax1099AccountList(categoryid, companyid);
                taxcategoryacc = new Tax1099Accounts();
                taxcategoryacc = buildTax1099AccountList(taxcategoryacc, taxCategoryAccountMap);
                hibernateTemplate.save(taxcategoryacc);
            }
            list.add(taxcategoryacc);
        } catch (Exception e) {
            throw ServiceException.FAILURE("updateTaxList : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "Tax List has been updated successfully", null, list, list.size());
    }

    public KwlReturnObject deleteTax1099AccountList(String categoryid, String companyid) throws ServiceException {
        String delQuery = "delete from Tax1099Accounts  where tax1099Category.ID=? and company.companyID=?";
        int numRows = HibernateUtil.executeUpdate(hibernateTemplate, delQuery,new Object[]{categoryid, companyid});
        return new KwlReturnObject(true, "Tax Category Accounts has been deleted successfully.", null, null, numRows);
    }


    public Tax1099Accounts buildTax1099AccountList(Tax1099Accounts taxaccountscategory, Map<String, Object> taxCategoryAccMap) throws ServiceException {
        try {
            if (taxCategoryAccMap.containsKey(COMPANYID)) {
                Company company = taxCategoryAccMap.get(COMPANYID)==null?null:(Company)hibernateTemplate.get(Company.class, (String) taxCategoryAccMap.get(COMPANYID));
                taxaccountscategory.setCompany(company);
            }
            if (taxCategoryAccMap.containsKey(ACCOUNTID)) {
                Account taxAccount = taxCategoryAccMap.get(ACCOUNTID)==null?null:(Account)hibernateTemplate.get(Account.class, (String) taxCategoryAccMap.get(ACCOUNTID));
                taxaccountscategory.setAccount(taxAccount);
            }
            if (taxCategoryAccMap.containsKey(CATEGORYID)) {
                Tax1099Category taxCategory = taxCategoryAccMap.get(CATEGORYID)==null?null:(Tax1099Category)hibernateTemplate.get(Tax1099Category.class, (String) taxCategoryAccMap.get(CATEGORYID));
                taxaccountscategory.setTax1099Category(taxCategory);
            }
        } catch (Exception e) {
            throw ServiceException.FAILURE("buildTaxList : " + e.getMessage(), e);
        }
        return taxaccountscategory;
    }

    public KwlReturnObject deleteTaxList(String taxid, String companyid) throws ServiceException {
        String delQuery = "delete from TaxList  where tax.ID=? and company.companyID=?";
        int numRows = HibernateUtil.executeUpdate(hibernateTemplate, delQuery,new Object[]{taxid, companyid});
        return new KwlReturnObject(true, "Tax Details has been deleted successfully.", null, null, numRows);
    }

    public KwlReturnObject getTaxList(Map<String, Object> filterParams) throws ServiceException {
		List returnList = new ArrayList();
        ArrayList params=new ArrayList();
        String condition = "";
        String query="from TaxList ";

        if(filterParams.containsKey(TAXID)){
            condition += (condition.length()==0?" where ":" and ") + "tax.ID=?";
            params.add(filterParams.get(TAXID));
        }
        if(filterParams.containsKey(APPLYDATE)){
            condition += (condition.length()==0?" where ":" and ") + "applyDate=?";
            params.add(filterParams.get(APPLYDATE));
        }
        if(filterParams.containsKey(COMPANYID)){
            condition += (condition.length()==0?" where ":" and ") + "company.companyID=?";
            params.add(filterParams.get(COMPANYID));
        }
        query += condition;
//        query="from TaxList where applyDate=? and tax.ID=?  and company.companyID=?";
        returnList = HibernateUtil.executeQuery(hibernateTemplate, query, params.toArray());
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }


    public KwlReturnObject getTaxFromAccount(String accountid, String companyid) throws ServiceException {
        List list = new ArrayList();
        String q = "from Tax t where account.ID=? and t.company.companyID=?";
        list = HibernateUtil.executeQuery(hibernateTemplate, q, new Object[]{accountid, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    public KwlReturnObject getCalculatedTax(Map<String, Object> filterParams) throws ServiceException {
        List returnlist = new ArrayList();
        ArrayList params=new ArrayList();
        String condition="";
        params.add((String) filterParams.get(COMPANYID));
        String ss = (String) filterParams.get(SS);
        if(!StringUtil.isNullOrEmpty(ss)){
            for (int i=0;i<2;i++){
                params.add(ss + "%");
            }
            condition+=" and ( t.taxCode like ? or t.name like ? )";
        }
        String query = "select t," +
                    " (select tl1 from TaxList tl1 where tax.ID=t.ID  and applyDate in (select max(applyDate) as ld from TaxList where tax.ID=tl1.tax.ID  group by tax)) "+
                    " from Tax t where company.companyID=? and t.deleted=false "+condition+" order by t.name ";
        returnlist = HibernateUtil.executeQuery(hibernateTemplate, query, params.toArray());
        return new KwlReturnObject(true, "", null, returnlist, returnlist.size());
    }

    public KwlReturnObject getTaxPercent(String companyid, Date transactiondate, String taxid) throws ServiceException {
        List list = new ArrayList();
        double percent = 0;
        ArrayList params = new ArrayList();
        params.add(taxid);
        params.add(taxid);
        params.add(transactiondate);
        params.add(companyid);
        String query = "select tl1.percent from TaxList tl1 where tl1.tax.ID=?  and applyDate in (select max(applyDate) from TaxList where tax.ID=? and tl1.applyDate <= ? ) and company.companyID=?";
        Iterator itr = HibernateUtil.executeQuery(hibernateTemplate, query, params.toArray()).iterator();
        while (itr.hasNext()) {
            percent = Double.parseDouble(itr.next().toString());
        }
        list.add(percent);
        return new KwlReturnObject(true, "", null, list, list.size());
    }
}
