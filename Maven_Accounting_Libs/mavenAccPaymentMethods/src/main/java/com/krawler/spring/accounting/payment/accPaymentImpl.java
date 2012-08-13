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
package com.krawler.spring.accounting.payment;

import com.krawler.common.admin.Company;
import com.krawler.common.service.ServiceException;
import com.krawler.esp.hibernate.impl.HibernateUtil;
import com.krawler.hql.accounting.Account;
import com.krawler.hql.accounting.Card;
import com.krawler.hql.accounting.Cheque;
import com.krawler.hql.accounting.DefaultPaymentMethod;
import com.krawler.hql.accounting.MasterItem;
import com.krawler.hql.accounting.PayDetail;
import com.krawler.hql.accounting.PaymentMethod;
import com.krawler.spring.common.KwlReturnObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.HibernateTemplate;

/**
 *
 * @author krawler
 */
public class accPaymentImpl implements accPaymentDAO {
    private HibernateTemplate hibernateTemplate;
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.hibernateTemplate = new HibernateTemplate(sessionFactory);
	}

    public KwlReturnObject addPayDetail(HashMap hm) throws ServiceException {
        List list = new ArrayList();
        try{
            PayDetail pdetail;
            if(hm.containsKey("paydetailid")){
                String paydetailid = (String) hm.get("paydetailid");
                pdetail = (PayDetail) hibernateTemplate.get(PayDetail.class, paydetailid);
            }else
                pdetail = new PayDetail();
            if(hm.containsKey("paymethodid")){
                PaymentMethod paymentMethod = (hm.get("paymethodid")==null?null:(PaymentMethod) hibernateTemplate.get(PaymentMethod.class, (String) hm.get("paymethodid")));
                pdetail.setPaymentMethod(paymentMethod);
            }
            if(hm.containsKey("companyid")){
                Company cmp = (hm.get("companyid")==null?null:(Company) hibernateTemplate.get(Company.class, (String) hm.get("companyid")));
                pdetail.setCompany(cmp);
            }
            if(hm.containsKey("chequeid")){
                Cheque chq = (hm.get("chequeid")==null?null:(Cheque) hibernateTemplate.get(Cheque.class, (String) hm.get("chequeid")));
                pdetail.setCheque(chq);
            }
            if(hm.containsKey("cardid")){
                Card card = (hm.get("cardid")==null?null:(Card) hibernateTemplate.get(Card.class, (String) hm.get("cardid")));
                pdetail.setCard(card);
            }
            hibernateTemplate.saveOrUpdate(pdetail);
            list.add(pdetail);
        } catch(Exception ex) {
            throw ServiceException.FAILURE("accPaymentImpl.addPayDetail : "+ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Pay Details has been added successfully", null, list, list.size());
    }

    public KwlReturnObject updatePayDetail(HashMap hm) throws ServiceException {
        List list = new ArrayList();
        try{
            String paydetailid = (String) hm.get("paydetailid");
            PayDetail pdetail = (PayDetail) hibernateTemplate.get(PayDetail.class, paydetailid);
            if (pdetail != null) {
                if(hm.containsKey("paymethodid")){
                    PaymentMethod paymentMethod = (hm.get("paymethodid")==null?null:(PaymentMethod) hibernateTemplate.get(PaymentMethod.class, (String) hm.get("paymethodid")));
                    pdetail.setPaymentMethod(paymentMethod);
                }
                if(hm.containsKey("companyid")){
                    Company cmp = (hm.get("companyid")==null?null:(Company) hibernateTemplate.get(Company.class, (String) hm.get("companyid")));
                    pdetail.setCompany(cmp);
                }
                if(hm.containsKey("chequeid")){
                    Cheque chq = (hm.get("chequeid")==null?null:(Cheque) hibernateTemplate.get(Cheque.class, (String) hm.get("chequeid")));
                    pdetail.setCheque(chq);
                }
                if(hm.containsKey("cardid")){
                    Card card = (hm.get("cardid")==null?null:(Card) hibernateTemplate.get(Card.class, (String) hm.get("cardid")));
                    pdetail.setCard(card);
                }
            }
            hibernateTemplate.saveOrUpdate(pdetail);
            list.add(pdetail);
        } catch(Exception ex) {
            throw ServiceException.FAILURE("accPaymentImpl.updatePayDetail : "+ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Pay Details has been updated successfully", null, list, list.size());
    }

    public KwlReturnObject addCheque(HashMap hm) throws ServiceException {
        List list = new ArrayList();
        try{
            Cheque cheque = new Cheque();
            if(hm.containsKey("chequeno")){
                cheque.setChequeNo((String) hm.get("chequeno"));
            }
            if(hm.containsKey("description")){
                cheque.setDescription((String) hm.get("description"));
            }
            if(hm.containsKey("bankname")){
                cheque.setBankName((String) hm.get("bankname"));
            }
             if(hm.containsKey("bankmasteritemid")){
                 String bankid=(String) hm.get("bankmasteritemid");
                 MasterItem bank = (MasterItem) hibernateTemplate.get(MasterItem.class, bankid);
                 cheque.setBankMasterItem(bank);
            }
            hibernateTemplate.saveOrUpdate(cheque);
            list.add(cheque);
        } catch(Exception ex) {
            throw ServiceException.FAILURE("accPaymentImpl.addCheque : "+ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Cheque has been added successfully", null, list, list.size());
    }

    public KwlReturnObject deleteCheque(String chequeid, String companyid) throws ServiceException {
        String delQuery = "delete from Cheque c where c.ID=? ";
        int numRows = HibernateUtil.executeUpdate(hibernateTemplate, delQuery, new Object[]{chequeid});
        return new KwlReturnObject(true, "Cheque has been deleted successfully.", null, null, numRows);
    }

    public KwlReturnObject updateCheque(HashMap hm) throws ServiceException {
        List list = new ArrayList();
        try{
            String chequeid = (String) hm.get("chequeid");
            Cheque cheque = (Cheque) hibernateTemplate.get(Cheque.class, chequeid);
            if (cheque != null) {
                if(hm.containsKey("chequeno")){
                    cheque.setChequeNo((String) hm.get("chequeno"));
                }
                if(hm.containsKey("description")){
                    cheque.setDescription((String) hm.get("description"));
                }
                if(hm.containsKey("bankname")){
                    cheque.setBankName((String) hm.get("bankname"));
                }
            }
            hibernateTemplate.saveOrUpdate(cheque);
            list.add(cheque);
        } catch(Exception ex) {
            throw ServiceException.FAILURE("accPaymentImpl.updateCheque : "+ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Cheque has been updated successfully", null, list, list.size());
    }

    public KwlReturnObject addCard(HashMap hm) throws ServiceException {
        List list = new ArrayList();
        try{
            Card card = new Card();
            if(hm.containsKey("cardno")){
                card.setCardNo((String) hm.get("cardno"));
            }
            if(hm.containsKey("nameoncard")){
                card.setCardHolder((String) hm.get("nameoncard"));
            }
            if(hm.containsKey("expirydate")){
                card.setExpiryDate((String) hm.get("expirydate"));
            }
            if(hm.containsKey("cardtype")){
                card.setCardType((String) hm.get("cardtype"));
            }
            if(hm.containsKey("refno")){
                card.setRefNo((String) hm.get("refno"));
            }
            hibernateTemplate.saveOrUpdate(card);
            list.add(card);
        } catch(Exception ex) {
            throw ServiceException.FAILURE("accPaymentImpl.addCard : "+ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Card has been added successfully", null, list, list.size());
    }

    public KwlReturnObject deleteCard(String cardid, String companyid) throws ServiceException {
        String delQuery = "delete from Card c where c.ID=? ";
        int numRows = HibernateUtil.executeUpdate(hibernateTemplate, delQuery, new Object[]{cardid});
        return new KwlReturnObject(true, "Card has been deleted successfully.", null, null, numRows);
    }

    public KwlReturnObject updateCard(HashMap hm) throws ServiceException {
        List list = new ArrayList();
        try{
            String cardid = (String) hm.get("cardid");
            Card card = (Card) hibernateTemplate.get(Card.class, cardid);
            if (card != null) {
                if(hm.containsKey("cardno")){
                    card.setCardNo((String) hm.get("cardno"));
                }
                if(hm.containsKey("nameoncard")){
                    card.setCardHolder((String) hm.get("nameoncard"));
                }
                if(hm.containsKey("expirydate")){
                    card.setExpiryDate((String) hm.get("expirydate"));
                }
                if(hm.containsKey("cardtype")){
                    card.setCardType((String) hm.get("cardtype"));
                }
                if(hm.containsKey("refno")){
                    card.setRefNo((String) hm.get("refno"));
                }
                hibernateTemplate.saveOrUpdate(card);
            }
            list.add(card);
        } catch(Exception ex) {
            throw ServiceException.FAILURE("accPaymentImpl.updateCard : "+ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Card has been updated successfully", null, list, list.size());
    }

    public KwlReturnObject getPaymentMethodFromAccount(String accountid, String companyid) throws ServiceException {
        List list = new ArrayList();
        String q = "from PaymentMethod pm where account.ID=? and pm.company.companyID=?";
        list = HibernateUtil.executeQuery(hibernateTemplate, q, new Object[]{accountid, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject addPaymentMethod(HashMap<String, Object> pmMap) throws ServiceException {
        List list = new ArrayList();
        try {
            PaymentMethod pm = new PaymentMethod();
            pm = buildPaymentMethod(pm, pmMap);
            hibernateTemplate.save(pm);
            list.add(pm);
        } catch (Exception e) {
            throw ServiceException.FAILURE("addPaymentMethod : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "Payment Method has been added successfully", null, list, list.size());
    }

    public KwlReturnObject updatePaymentMethod(HashMap<String, Object> pmMap) throws ServiceException {
        List list = new ArrayList();
        try {
            String methodid = (String) pmMap.get("methodid");
            PaymentMethod pm = (PaymentMethod) hibernateTemplate.get(PaymentMethod.class, methodid);
            if (pm != null) {
                pm = buildPaymentMethod(pm, pmMap);
                hibernateTemplate.saveOrUpdate(pm);
            }
            list.add(pm);
        } catch (Exception e) {
            throw ServiceException.FAILURE("updatePaymentMethod : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "Payment Method has been updated successfully", null, list, list.size());
    }

    public PaymentMethod buildPaymentMethod(PaymentMethod pm, HashMap<String, Object> pmMap) {
        if(pmMap.containsKey("methodname")){
            pm.setMethodName((String) pmMap.get("methodname"));
        }
        if(pmMap.containsKey("detailtype")){
            pm.setDetailType((Integer) pmMap.get("detailtype"));
        }
        if(pmMap.containsKey("accountid")){
            Account account = (pmMap.get("accountid")==null?null:(Account) hibernateTemplate.get(Account.class, (String) pmMap.get("accountid")));
            pm.setAccount(account);
        }
        if(pmMap.containsKey("companyid")){
            Company cmp = (pmMap.get("companyid")==null?null:(Company) hibernateTemplate.get(Company.class, (String) pmMap.get("companyid")));
            pm.setCompany(cmp);
        }
        return pm;
    }


    public KwlReturnObject deletePaymentMethod(String methodid, String companyid) throws ServiceException {
        String delQuery = "delete from PaymentMethod p where p.ID=? and p.company.companyID=?";
        int numRows = HibernateUtil.executeUpdate(hibernateTemplate, delQuery, new Object[]{methodid, companyid});
        return new KwlReturnObject(true, "Payment Method has been deleted successfully.", null, null, numRows);
    }

    public KwlReturnObject getPaymentMethod(HashMap<String, Object> filterParams) throws ServiceException {
		List returnList = new ArrayList();
        ArrayList params=new ArrayList();
        String condition = "";
        String query="from PaymentMethod ";

        if(filterParams.containsKey("companyid")){
            condition += (condition.length()==0?" where ":" and ") + "company.companyID=?";
            params.add(filterParams.get("companyid"));
        }
        query += condition;
//        query="from PaymentMethod where company.companyID=?";
        returnList = HibernateUtil.executeQuery(hibernateTemplate, query, params.toArray());
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }

    public void copyPaymentMethods(String companyid, HashMap hm) throws ServiceException {
        try {
            String query = "from DefaultPaymentMethod";
            List list = HibernateUtil.executeQuery(hibernateTemplate, query);
            Iterator iter = list.iterator();
            Company company = (Company)hibernateTemplate.get(Company.class, companyid);
            while (iter.hasNext()) {
                DefaultPaymentMethod defaultPM = (DefaultPaymentMethod) iter.next();
                PaymentMethod pm = new PaymentMethod();
                pm.setCompany(company);
                pm.setDetailType(defaultPM.getDetailType());
                pm.setMethodName(defaultPM.getMethodName());
                pm.setAccount((Account) hm.get(defaultPM.getAccount()));
                hibernateTemplate.save(pm);
            }
        }  catch (Exception ex) {
            throw ServiceException.FAILURE("copyPaymentMethods : "+ex.getMessage(), ex);
        }
    }
}
