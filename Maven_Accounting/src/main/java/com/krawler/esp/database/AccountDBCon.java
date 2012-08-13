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
package com.krawler.esp.database;

import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.esp.handlers.AuthHandler;
import com.krawler.esp.handlers.CompanyHandler;
import com.krawler.esp.handlers.CustomerHandler;
import com.krawler.esp.handlers.VendorHandler;
import com.krawler.esp.hibernate.impl.HibernateUtil;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.CompanyAccountPreferences;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.lowagie.text.DocumentException;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.exception.ConstraintViolationException;
import com.krawler.utils.json.base.JSONObject;
import java.util.ArrayList;
import java.util.Date;
import org.hibernate.HibernateException;

public class AccountDBCon {

        /*public static void saveCompanyAccountPreferences(HttpServletRequest request) throws ServiceException, AccountingException {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getCurrentSession();
            tx = session.beginTransaction();
            CompanyHandler.saveCompanyAccountPreferences(session, request);
            tx.commit();
        } catch (ServiceException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
    }*/

    /*public static JSONObject getNextAutoNumber(HttpServletRequest request) throws ServiceException, AccountingException{
        JSONObject jObj = new JSONObject();
        Session session = null;
        try {
            session = HibernateUtil.getCurrentSession();
            CompanyAccountPreferences pref=(CompanyAccountPreferences)session.get(CompanyAccountPreferences.class,AuthHandler.getCompanyid(request));
            int from=Integer.parseInt(request.getParameter("from"));
            String autoNum = CompanyHandler.getNextAutoNumber(session, pref, from);
            jObj.put("data", autoNum);
        } catch (ServiceException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (NumberFormatException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (JSONException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
        return jObj;
    }*/

    /*public static JSONObject getCompanyAccountPreferences(HttpServletRequest request) throws ServiceException {
        JSONObject jObj = new JSONObject();
        Session session = null;
        try {
            session = HibernateUtil.getCurrentSession();
            jObj = CompanyHandler.getCompanyAccountPreferences(session, request);
        } catch (ServiceException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
        return jObj;
    }*/
    /*public static boolean checkApplyTax(HttpServletRequest request) throws ServiceException, JSONException, SessionExpiredException, ParseException {

        Session session = null;
        boolean flag=false;
        try {
            session = HibernateUtil.getCurrentSession();
            flag = CompanyHandler.checkApplyTax(session, request);
        } catch (ServiceException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
        return flag;
    }*/

    /*public static String saveAccount(HttpServletRequest request) throws ServiceException, SessionExpiredException, AccountingException, ParseException {
        Session session = null;
        Transaction tx = null;
        String accID=null;
        try {
            session = HibernateUtil.getCurrentSession();
            tx = session.beginTransaction();
            accID=CompanyHandler.saveAccount(session, request);
            tx.commit();
        } catch (ServiceException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
        return accID;
    }*/

    /*public static void deleteAccount(HttpServletRequest request) throws ServiceException, JSONException, AccountingException, SessionExpiredException {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getCurrentSession();
            tx = session.beginTransaction();
            CompanyHandler.deleteAccount(session, request);
            tx.commit();
        } catch (ServiceException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (ConstraintViolationException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("Cannot delete account, Dependencies exist.", ex);
        } catch (HibernateException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
    }*/

  /*  public static JSONObject getAccounts(HttpServletRequest request) throws ServiceException, AccountingException {
        JSONObject jObj = new JSONObject();
        Session session = null;
        try {
            session = HibernateUtil.getCurrentSession();
            jObj = CompanyHandler.getAccounts(session, request);
        } catch (ServiceException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
        return jObj;
    }

    public static JSONObject getAccountDepreciation(HttpServletRequest request) throws ServiceException, JSONException, ParseException {
        JSONObject jObj = new JSONObject();
        Session session = null;
        try {
            session = HibernateUtil.getCurrentSession();
            jObj = CompanyHandler.getAccountDepreciation(session, request);
        } catch (ServiceException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
        return jObj;
    }
*/
     /*public static void saveAccountDepreciation(HttpServletRequest request) throws ServiceException, SessionExpiredException, ParseException, AccountingException {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getCurrentSession();
            tx = session.beginTransaction();
            CompanyHandler.saveAccountDepreciation(session, request);
            tx.commit();
        } catch (ServiceException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }

    }*/
    /*public static JSONObject getGroups(HttpServletRequest request) throws ServiceException {
        JSONObject jObj = new JSONObject();
        Session session = null;
        try {
            session = HibernateUtil.getCurrentSession();
            jObj = CompanyHandler.getGroups(session, request.getParameter("groupid"),CompanyHandler.getBoolean(request, "ignorecustomers"),CompanyHandler.getBoolean(request, "ignorevendors"),AuthHandler.getCompanyid(request));
        } catch (ServiceException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
        return jObj;
    }*/

    /*public static JSONObject getCustomerAddress(HttpServletRequest request) throws ServiceException, SessionExpiredException, AccountingException {
        JSONObject jObj = new JSONObject();
        Session session = null;
        try {
            session = HibernateUtil.getCurrentSession();
            jObj = CustomerHandler.getAddress(session, request);
        } catch (ServiceException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
        return jObj;
    }*/

    /*public static JSONObject getVendorAddress(HttpServletRequest request) throws ServiceException, SessionExpiredException, AccountingException {
        JSONObject jObj = new JSONObject();
        Session session = null;
        try {
            session = HibernateUtil.getCurrentSession();
            jObj = VendorHandler.getAddress(session, request);
        } catch (ServiceException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
        return jObj;
    }*/

    /*public static void setNewPrice(HttpServletRequest request) throws ServiceException {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getCurrentSession();
            tx = session.beginTransaction();
            CompanyHandler.setNewPrice(session, request);
            tx.commit();
        } catch (ServiceException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
    }*/

    /*public static JSONObject getProductPrice(HttpServletRequest request) throws ServiceException {
        JSONObject jObj = new JSONObject();
        Session session = null;
        try {
            session = HibernateUtil.getCurrentSession();
            jObj = CompanyHandler.getPrice(session, request, request.getParameter("productid"));
        } catch (ServiceException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
        return jObj;
    }*/

    /*public static String saveProduct(HttpServletRequest request) throws ServiceException, AccountingException {
        Session session = null;
        Transaction tx = null;
        String productID=null;
        try {
            session = HibernateUtil.getCurrentSession();
            tx = session.beginTransaction();
            productID=CompanyHandler.saveProduct(session, request);
            tx.commit();
        } catch (AccountingException ex) {
            if (tx != null) {
                tx.rollback();
            }
            throw ex;
        } catch (ServiceException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
        return productID;
    }*/

    /*public static JSONObject getProducts(HttpServletRequest request) throws ServiceException {
        JSONObject jObj = new JSONObject();
        Session session = null;
        try {
            session = HibernateUtil.getCurrentSession();
            jObj = CompanyHandler.getProducts(session, request.getParameter("productid"), AuthHandler.getCompanyid(request));
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE("AccountDBCon.getProducts", ex);
        } catch (ServiceException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
        return jObj;
    }*/

    /*public static void deleteProducts(HttpServletRequest request) throws ServiceException, JSONException, SessionExpiredException, AccountingException {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getCurrentSession();
            tx = session.beginTransaction();
            CompanyHandler.deleteProducts(session, request);
            tx.commit();
        } catch (HibernateException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
    }*/
    /*public static void deleteBankReconciliation(HttpServletRequest request) throws ServiceException, JSONException, SessionExpiredException, AccountingException {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getCurrentSession();
            tx = session.beginTransaction();
            CompanyHandler.deleteBankReconciliation(session, request);
            tx.commit();
        } catch (HibernateException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
    }*/

    /*public static JSONObject getUnitOfMeasure(HttpServletRequest request) throws ServiceException {
        JSONObject jObj = new JSONObject();
        Session session = null;
        try {
            session = HibernateUtil.getCurrentSession();
            jObj = CompanyHandler.getUnitOfMeasure(session, request);
        } catch (ServiceException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
        return jObj;
    }*/

    /*public static void saveUnitOfMeasure(HttpServletRequest request) throws ServiceException, AccountingException {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getCurrentSession();
            tx = session.beginTransaction();
            CompanyHandler.saveUnitOfMeasure(session, request);
            tx.commit();
        } catch (ServiceException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
    }*/
    /*public static JSONObject getTax(HttpServletRequest request) throws ServiceException {
        JSONObject jObj = new JSONObject();
        Session session = null;
        try {
            session = HibernateUtil.getCurrentSession();
            jObj = CompanyHandler.getTax(session, request,null);
        } catch (ServiceException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
        return jObj;
    }*/

    /*public static void saveTax(HttpServletRequest request) throws ServiceException, AccountingException {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getCurrentSession();
            tx = session.beginTransaction();
            CompanyHandler.saveTax(session, request);
            tx.commit();
        } catch (ServiceException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
    }*/
    /*public static String saveCustomer(HttpServletRequest request) throws ServiceException, AccountingException, ParseException {
        Session session = null;
        Transaction tx = null;
        String perAccID=null;
        try {
            session = HibernateUtil.getCurrentSession();
            tx = session.beginTransaction();
            perAccID=CustomerHandler.saveCustomer(session, request);
            tx.commit();

        } catch (ServiceException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
        return perAccID;
    }*/

    /*public static String saveVendor(HttpServletRequest request) throws ServiceException, AccountingException, ParseException {
        Session session = null;
        Transaction tx = null;
        String perAccID=null;
        try {
            session = HibernateUtil.getCurrentSession();
            tx = session.beginTransaction();
            perAccID=VendorHandler.saveVendor(session, request);
            tx.commit();
        } catch (ServiceException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
        return perAccID;
    }*/

    /*public static void savePayment(HttpServletRequest request) throws ServiceException, AccountingException {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getCurrentSession();
            tx = session.beginTransaction();
            VendorHandler.savePayment(session, request);
            tx.commit();
        } catch (AccountingException ex) {
            if (tx != null) {
                tx.rollback();
            }
            throw ex;
        } catch (ServiceException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
    }*/

    /*public static JSONObject getInventory(HttpServletRequest request) throws ServiceException {
        JSONObject jObj = new JSONObject();
        Session session = null;
        try {
            session = HibernateUtil.getCurrentSession();
            jObj = CompanyHandler.getInventory(session, request);
        } catch (ServiceException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
        return jObj;
    }*/

    /*public static JSONObject getCustomers(HttpServletRequest request) throws ServiceException, SessionExpiredException, AccountingException {
        JSONObject jObj = new JSONObject();
        Session session = null;
        try {
            session = HibernateUtil.getCurrentSession();
            jObj = CustomerHandler.getCustomers(session, request);
        } catch (ServiceException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
        return jObj;
    }*/

    /*public static String saveInvoice(HttpServletRequest request) throws ServiceException, AccountingException {
        Session session = null;
        Transaction tx = null;
        String id=null;
        try {
            session = HibernateUtil.getCurrentSession();
            tx = session.beginTransaction();
            id=CustomerHandler.saveInvoice(session, request);
            tx.commit();
        } catch (AccountingException ex) {
            if (tx != null) {
                tx.rollback();
            }
            throw ex;
        } catch (ServiceException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
        return id;
    }*/
    /*public static String saveBillingInvoice(HttpServletRequest request) throws ServiceException, AccountingException {
        Session session = null;
        Transaction tx = null;
        String id=null;
        try {
            session = HibernateUtil.getCurrentSession();
            tx = session.beginTransaction();
            id=CustomerHandler.saveBillingInvoice(session, request);
            tx.commit();
        } catch (AccountingException ex) {
            if (tx != null) {
                tx.rollback();
            }
            throw ex;
        } catch (ServiceException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
        return id;
    }*/

    // Fetch invoices of all customers
    /*public static JSONObject getInvoices(HttpServletRequest request) throws ServiceException, AccountingException {
        JSONObject jObj = new JSONObject();
        Session session = null;
        try {
            session = HibernateUtil.getCurrentSession();
            jObj = CustomerHandler.getInvoices(session, request, request.getParameter("start"), request.getParameter("limit"),null);
        } catch (ServiceException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
        return jObj;
    }*/

   public static JSONObject getAgedReceivable(HttpServletRequest request) throws ServiceException, JSONException, AccountingException {
        JSONObject jObj = new JSONObject();
        Session session = null;
        try {
            session = HibernateUtil.getCurrentSession();
            jObj = CustomerHandler.getAgedReceivable(session, request, request.getParameter("start"), request.getParameter("limit"));
        } catch (ServiceException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
        return jObj;
    }
    /*public static JSONObject getCustomerAgedReceivable(HttpServletRequest request) throws ServiceException, JSONException, AccountingException, SessionExpiredException, ParseException {
        JSONObject jObj = new JSONObject();
        Session session = null;
        try {
            session = HibernateUtil.getCurrentSession();
            jObj = CustomerHandler.getCustomerAgedReceivable(session, request, request.getParameter("start"), request.getParameter("limit"));
        } catch (ServiceException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
        return jObj;
    }*/
    /*public static JSONObject getBillingInvoices(HttpServletRequest request) throws ServiceException, AccountingException {
        JSONObject jObj = new JSONObject();
        Session session = null;
        try {
            session = HibernateUtil.getCurrentSession();
            jObj = CustomerHandler.getBillingInvoices(session, request, request.getParameter("start"), request.getParameter("limit"),null);
        } catch (ServiceException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
        return jObj;
    }*/

    /*public static JSONObject getBillingInvoiceRows(HttpServletRequest request) throws ServiceException, SessionExpiredException, AccountingException {
        JSONObject jObj = new JSONObject();
        Session session = null;
        try {
            session = HibernateUtil.getCurrentSession();
            jObj = CustomerHandler.getBillingInvoiceRows(session, request);
        } catch (ServiceException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
        return jObj;
    }*/
    /*public static JSONObject getInvoiceRows(HttpServletRequest request) throws ServiceException, SessionExpiredException, AccountingException {
        JSONObject jObj = new JSONObject();
        Session session = null;
        try {
            session = HibernateUtil.getCurrentSession();
            jObj = CustomerHandler.getInvoiceRows(session, request);
        } catch (ServiceException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
        return jObj;
    }*/
    public static void deleteInvoices(HttpServletRequest request) throws ServiceException, AccountingException, JSONException, SessionExpiredException {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getCurrentSession();
            tx = session.beginTransaction();
            CustomerHandler.deleteInvoices(session, request);
            tx.commit();
        } catch (AccountingException ex) {
            if (tx != null) {
                tx.rollback();
            }
            throw ex;
        } catch (ServiceException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
    }
    /*public static void deleteBillingInvoices(HttpServletRequest request) throws ServiceException, AccountingException, JSONException, SessionExpiredException {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getCurrentSession();
            tx = session.beginTransaction();
            CustomerHandler.deleteBillingInvoices(session, request);
            tx.commit();
        } catch (AccountingException ex) {
            if (tx != null) {
                tx.rollback();
            }
            throw ex;
        } catch (ServiceException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
    }*/
    /*public static void deleteBillingReceipt(HttpServletRequest request) throws ServiceException, AccountingException, JSONException, SessionExpiredException {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getCurrentSession();
            tx = session.beginTransaction();
            CustomerHandler.deleteBillingReceipt(session, request);
            tx.commit();
        } catch (AccountingException ex) {
            if (tx != null) {
                tx.rollback();
            }
            throw ex;
        } catch (ServiceException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
    }*/

     /*public static void deleteReceipt(HttpServletRequest request) throws ServiceException, AccountingException, JSONException, SessionExpiredException {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getCurrentSession();
            tx = session.beginTransaction();
            CustomerHandler.deleteReceipt(session, request);
            tx.commit();
        } catch (AccountingException ex) {
            if (tx != null) {
                tx.rollback();
            }
            throw ex;
        } catch (ServiceException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
    }*/

    /*public static void deletePayment(HttpServletRequest request) throws ServiceException, AccountingException, JSONException, SessionExpiredException {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getCurrentSession();
            tx = session.beginTransaction();
            VendorHandler.deletePayment(session, request);
            tx.commit();
        } catch (AccountingException ex) {
            if (tx != null) {
                tx.rollback();
            }
            throw ex;
        } catch (ServiceException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
    }*/

       /* public static void deleteGoodsReceipt(HttpServletRequest request) throws ServiceException, AccountingException, JSONException, SessionExpiredException {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getCurrentSession();
            tx = session.beginTransaction();
            VendorHandler.deleteGoodsReceipt(session, request);
            tx.commit();
        } catch (AccountingException ex) {
            if (tx != null) {
                tx.rollback();
            }
            throw ex;
        } catch (ServiceException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
    }*/

    /*public static void saveCreditNote(HttpServletRequest request) throws ServiceException, AccountingException, SessionExpiredException {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getCurrentSession();
            tx = session.beginTransaction();
            CustomerHandler.saveCreditNote(session, request);
            tx.commit();
        } catch (AccountingException ex) {
            if (tx != null) {
                tx.rollback();
            }
            throw ex;
        } catch (ServiceException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
    }*/

    /*public static JSONObject getCreditNote(HttpServletRequest request) throws ServiceException {
        JSONObject jObj = new JSONObject();
        Session session = null;
        try {
            session = HibernateUtil.getCurrentSession();
            jObj = CustomerHandler.getCreditNote(session, request);
        } catch (ServiceException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
        return jObj;
    }*/

    /*public static JSONObject getCreditNoteRows(HttpServletRequest request) throws ServiceException, SessionExpiredException, AccountingException {
        JSONObject jObj = new JSONObject();
        Session session = null;
        try {
            session = HibernateUtil.getCurrentSession();
            jObj = CustomerHandler.getCreditNoteRows(session, request);
        } catch (ServiceException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
        return jObj;
    }*/

    /*public static void saveReceipt(HttpServletRequest request) throws ServiceException, AccountingException {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getCurrentSession();
            tx = session.beginTransaction();
            CustomerHandler.saveReceipt(session, request);
            tx.commit();
        } catch (AccountingException ex) {
            if (tx != null) {
                tx.rollback();
            }
            throw ex;
        } catch (ServiceException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
    }*/

    /*public static JSONObject getReceipts(HttpServletRequest request) throws ServiceException {
        JSONObject jObj = new JSONObject();
        Session session = null;
        try {
            session = HibernateUtil.getCurrentSession();
            jObj = CustomerHandler.getReceipts(session, request);
        } catch (ServiceException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
        return jObj;
    }*/

    /*public static JSONObject getReceiptRows(HttpServletRequest request) throws ServiceException {
        JSONObject jObj = new JSONObject();
        Session session = null;
        try {
            session = HibernateUtil.getCurrentSession();
            jObj = CustomerHandler.getReceiptRows(session, request);
        } catch (ServiceException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
        return jObj;
    }*/

     /*public static void saveBillingReceipt(HttpServletRequest request) throws ServiceException, AccountingException {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getCurrentSession();
            tx = session.beginTransaction();
            CustomerHandler.saveBillingReceipt(session, request);
            tx.commit();
        } catch (AccountingException ex) {
            if (tx != null) {
                tx.rollback();
            }
            throw ex;
        } catch (ServiceException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
    }*/

    /*public static JSONObject getBillingReceipts(HttpServletRequest request) throws ServiceException, AccountingException {
        JSONObject jObj = new JSONObject();
        Session session = null;
        try {
            session = HibernateUtil.getCurrentSession();
            jObj = CustomerHandler.getBillingReceipts(session, request);
        } catch (ServiceException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
        return jObj;
    }*/

    /*public static JSONObject getBillingReceiptRows(HttpServletRequest request) throws ServiceException, AccountingException {
        JSONObject jObj = new JSONObject();
        Session session = null;
        try {
            session = HibernateUtil.getCurrentSession();
            jObj = CustomerHandler.getBillingReceiptRows(session, request);
        } catch (ServiceException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
        return jObj;
    }*/

    /*public static JSONObject getPayments(HttpServletRequest request) throws ServiceException, AccountingException {
        JSONObject jObj = new JSONObject();
        Session session = null;
        try {
            session = HibernateUtil.getCurrentSession();
            jObj = VendorHandler.getPayments(session, request);
        } catch (ServiceException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
        return jObj;
    }*/

    /*public static JSONObject getPaymentRows(HttpServletRequest request) throws ServiceException, AccountingException {
        JSONObject jObj = new JSONObject();
        Session session = null;
        try {
            session = HibernateUtil.getCurrentSession();
            jObj = VendorHandler.getPaymentRows(session, request);
        } catch (ServiceException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
        return jObj;
    }*/

   /* public static JSONObject getVendors(HttpServletRequest request) throws ServiceException, SessionExpiredException, AccountingException {
        JSONObject jObj = new JSONObject();
        Session session = null;
        try {
            session = HibernateUtil.getCurrentSession();
            jObj = VendorHandler.getVendors(session, request);
        } catch (ServiceException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
        return jObj;
    }*/

    /*public static JSONObject getPaymentMethods(HttpServletRequest request) throws ServiceException {
        JSONObject jObj = new JSONObject();
        Session session = null;
        try {
            session = HibernateUtil.getCurrentSession();
            jObj = CompanyHandler.getPaymentMethods(session, request);
        } catch (ServiceException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
        return jObj;
    }*/

    /*public static void savePaymentMethod(HttpServletRequest request) throws ServiceException, AccountingException {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getCurrentSession();
            tx = session.beginTransaction();
            CompanyHandler.savePaymentMethod(session, request);
            tx.commit();
        } catch (ServiceException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
    }*/

  /*  public static void saveJournalEntry(HttpServletRequest request) throws ServiceException, AccountingException {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getCurrentSession();
            tx = session.beginTransaction();
            CompanyHandler.saveJournalEntry(session, request);
            tx.commit();
        } catch (AccountingException ex) {
            if (tx != null) {
                tx.rollback();
            }
            throw ex;
        } catch (ServiceException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
    }
*/
   /* public static void saveBankReconciliation(HttpServletRequest request) throws ServiceException, AccountingException, ParseException {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getCurrentSession();
            tx = session.beginTransaction();
            CompanyHandler.saveBankReconciliation(session, request);
            tx.commit();
        } catch (AccountingException ex) {
            if (tx != null) {
                tx.rollback();
            }
            throw ex;
        } catch (ServiceException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
    }*/

    /*public static JSONObject getLedger(HttpServletRequest request) throws ServiceException, AccountingException {
        JSONObject jObj = new JSONObject();
        Session session = null;
        try {
            session = HibernateUtil.getCurrentSession();
            jObj = CompanyHandler.getLedger(session, request);
        } catch (ServiceException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
        return jObj;
    }*/

    /*public static JSONObject getReconciliationData(HttpServletRequest request) throws ServiceException, SessionExpiredException, AccountingException {
        JSONObject jObj = new JSONObject();
        Session session = null;
        try {
            session = HibernateUtil.getCurrentSession();
            jObj = CompanyHandler.getReconciliationData(session, request);
        } catch (ServiceException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
        return jObj;
    }*/

    /*public static JSONObject getTrialBalance(HttpServletRequest request) throws ServiceException, AccountingException {
        JSONObject jObj = new JSONObject();
        Session session = null;
        try {
            session = HibernateUtil.getCurrentSession();
            jObj = CompanyHandler.getTrialBalance(session, request);
        } catch (ServiceException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
//            HibernateUtil.closeSession(session);
        }
        return jObj;
    }*/

    /*public static JSONObject getTrading(HttpServletRequest request) throws ServiceException, AccountingException {
        JSONObject jObj = new JSONObject();
        Session session = null;
        try {
            session = HibernateUtil.getCurrentSession();
            jObj = CompanyHandler.getTrading(session, request);
        } catch (ServiceException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
        return jObj;
    }*/

    /*public static JSONObject getProfitLoss(HttpServletRequest request) throws ServiceException, AccountingException {
        JSONObject jObj = new JSONObject();
        Session session = null;
        try {
            session = HibernateUtil.getCurrentSession();
            jObj = CompanyHandler.getProfitLoss(session, request);
        } catch (ServiceException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
        return jObj;
    }*/

    /*public static JSONObject getTradingAndProfitLoss(HttpServletRequest request) throws ServiceException, AccountingException {
        JSONObject jObj = new JSONObject();
        Session session = null;
        try {
            session = HibernateUtil.getCurrentSession();
            jObj = CompanyHandler.getTradingAndProfitLoss(session, request);
        } catch (ServiceException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
        return jObj;
    }*/

    /*public static JSONObject getBalanceSheet(HttpServletRequest request) throws ServiceException, AccountingException {
        JSONObject jObj = new JSONObject();
        Session session = null;
        try {
            session = HibernateUtil.getCurrentSession();
            jObj = CompanyHandler.getBalanceSheet(session, request);
        } catch (ServiceException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
        return jObj;
    }*/
     /*public static JSONObject getAccountOpeningBalance(HttpServletRequest request) throws ServiceException, SessionExpiredException, ParseException, JSONException, AccountingException {
        JSONObject jObj = new JSONObject();
         Double openingbalance=0.0;
        Session session = null;
        try {
            session = HibernateUtil.getCurrentSession();
            Date startDate = AuthHandler.getDateFormatter(request).parse(request.getParameter("stdate"));
            openingbalance = CompanyHandler.getAccountBalance(session,request, request.getParameter("accountid"), null, startDate);
            JSONObject fobj=new JSONObject();
            fobj.put("openingbalance", new JSONArray("["+openingbalance+"]"));
            jObj.put("data", fobj);

        } catch (ServiceException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
        return jObj;
    }*/
    /*public static JSONObject getBankReconciliation(HttpServletRequest request) throws ServiceException {
        JSONObject jObj = new JSONObject();
        Session session = null;
        try {
            session = HibernateUtil.getCurrentSession();
            jObj = CompanyHandler.getBankReconciliation(session, request);
        } catch (ServiceException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
        return jObj;
    }*/
    /*public static JSONObject getJournalEntry(HttpServletRequest request) throws ServiceException {
        JSONObject jObj = new JSONObject();
        Session session = null;
        try {
            session = HibernateUtil.getCurrentSession();
            jObj = CompanyHandler.getJournalEntry(session, request);///getJournalEntry
        } catch (ServiceException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
        return jObj;
    }*/

    /*public static JSONObject getJournalEntryDetails(HttpServletRequest request) throws ServiceException, SessionExpiredException, AccountingException {
        JSONObject jObj = new JSONObject();
        Session session = null;
        try {
            session = HibernateUtil.getCurrentSession();
            jObj = CompanyHandler.getJournalEntryDetails(session, request);
        } catch (ServiceException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
        return jObj;
    }*/

    /*public static JSONObject getTerm(HttpServletRequest request) throws ServiceException {
        JSONObject jObj = new JSONObject();
        Session session = null;
        try {
            session = HibernateUtil.getCurrentSession();
            jObj = CompanyHandler.getTerm(session, request);
        } catch (ServiceException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
        return jObj;
    }*/

    /*public static void saveTerm(HttpServletRequest request) throws ServiceException, AccountingException {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getCurrentSession();
            tx = session.beginTransaction();
            CompanyHandler.saveTerm(session, request);
            tx.commit();
        } catch (ServiceException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
    }*/
    /*public static JSONObject getCurrencyExchangeList(HttpServletRequest request) throws ServiceException, SessionExpiredException {
        JSONObject jObj = new JSONObject();
        Session session = null;
        try {
            session = HibernateUtil.getCurrentSession();
            jObj = CompanyHandler.getCurrencyExchangeList(session,request);
        } catch (ServiceException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
        return jObj;
    }*/
//    public static JSONObject getCurrencyExchange(HttpServletRequest request) throws ServiceException, SessionExpiredException {
//        JSONObject jObj = new JSONObject();
//        Session session = null;
//        try {
//            session = HibernateUtil.getCurrentSession();
//            jObj = CompanyHandler.getCurrencyExchange(session,request);
//        } catch (ServiceException ex) {
//            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
//            throw ex;
//        } catch (HibernateException ex) {
//            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
//            throw ServiceException.FAILURE(ex.getMessage(), ex);
//        } finally {
//            HibernateUtil.closeSession(session);
//        }
//        return jObj;
//    }
    /*public static JSONObject getDefaultCurrencyExchange(HttpServletRequest request) throws ServiceException, SessionExpiredException, AccountingException {
        JSONObject jObj = new JSONObject();
        Session session = null;
        try {
            session = HibernateUtil.getCurrentSession();
            jObj = CompanyHandler.getDefaultCurrencyExchange(session,request,null);
        } catch (ServiceException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
        return jObj;
    }*/
    /*public static JSONObject getInvoicesCurrencyRate(HttpServletRequest request) throws ServiceException, SessionExpiredException, AccountingException {
        JSONObject jObj = new JSONObject();
        Session session = null;
        try {
            session = HibernateUtil.getCurrentSession();
            jObj = CustomerHandler.getInvoicesCurrencyRate(session,request);
        } catch (ServiceException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
        return jObj;
    }*/

    /*public static JSONObject getCurrencyExchange(HttpServletRequest request) throws ServiceException, SessionExpiredException, ParseException, AccountingException {
        JSONObject jObj = new JSONObject();
        Session session = null;
        try {
            Date transactiondate=null;
            String date=request.getParameter("transactiondate")==null?null:request.getParameter("transactiondate");
            if(date!=null)
                   transactiondate=AuthHandler.getDateFormatter(request).parse(date);

            session = HibernateUtil.getCurrentSession();
            jObj = CompanyHandler.getCurrencyExchange(session,request,transactiondate);
        } catch (ServiceException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
        return jObj;
    }*/
    /*public static void saveCurrencyExchange(HttpServletRequest request) throws ServiceException, AccountingException, JSONException {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getCurrentSession();
            tx = session.beginTransaction();
            CompanyHandler.saveCurrencyExchange(session, request);
            tx.commit();
        } catch (ServiceException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
    }*/


    /*public static JSONObject getYearLock(HttpServletRequest request) throws ServiceException, SessionExpiredException {
        JSONObject jObj = new JSONObject();
        Session session = null;
        try {
            session = HibernateUtil.getCurrentSession();
            jObj = CompanyHandler.getYearLock(session,request);
        } catch (ServiceException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
        return jObj;
    }*/

    /*public static void saveYearLock(HttpServletRequest request) throws ServiceException, AccountingException {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getCurrentSession();
            tx = session.beginTransaction();
            CompanyHandler.saveYearLock(session, request);
            tx.commit();
        } catch (ServiceException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
    }*/

    /*public static JSONObject getMasterGroups(HttpServletRequest request) throws ServiceException {
        JSONObject jObj = new JSONObject();
        Session session = null;
        try {
            session = HibernateUtil.getCurrentSession();
            jObj = CompanyHandler.getMasterGroups(session, request);
        } catch (ServiceException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
        return jObj;
    }*/

    /*public static JSONObject getMasterItems(HttpServletRequest request) throws ServiceException {
        JSONObject jObj = new JSONObject();
        Session session = null;
        try {
            session = HibernateUtil.getCurrentSession();
            jObj = CompanyHandler.getMasterItems(session, request);
        } catch (ServiceException ex) {
            throw ex;
        } catch (HibernateException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
        return jObj;
    }*/

    /*public static JSONObject saveMasterGroup(HttpServletRequest request) throws ServiceException {
        JSONObject jObj = new JSONObject();
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getCurrentSession();
            tx = session.beginTransaction();
            jObj = CompanyHandler.saveMasterGroup(session, request);
            tx.commit();
        } catch (ServiceException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
        return jObj;
    }*/

    /*public static JSONObject deleteMasterGroup(HttpServletRequest request) throws ServiceException {
        JSONObject jObj = new JSONObject();
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getCurrentSession();
            tx = session.beginTransaction();
            jObj = CompanyHandler.deleteMasterGroup(session, request);
            tx.commit();
        } catch (ServiceException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
        return jObj;
    }*/

    /*public static JSONObject saveMasterItem(HttpServletRequest request) throws ServiceException {
        JSONObject jObj = new JSONObject();
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getCurrentSession();
            tx = session.beginTransaction();
            jObj = CompanyHandler.saveMasterItem(session, request);
            tx.commit();
        } catch (ServiceException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
        return jObj;
    }*/

    /*public static JSONObject deleteMasterItem(HttpServletRequest request) throws ServiceException {
        JSONObject jObj = new JSONObject();
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getCurrentSession();
            tx = session.beginTransaction();
            jObj = CompanyHandler.deleteMasterItem(session, request);
            tx.commit();
        } catch (HibernateException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
        return jObj;
    }*/

    /*public static void saveGoodsReceipt(HttpServletRequest request) throws ServiceException, AccountingException {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getCurrentSession();
            tx = session.beginTransaction();
            VendorHandler.saveGoodsReceipt(session, request);
            tx.commit();
        } catch (AccountingException ex) {
            if (tx != null) {
                tx.rollback();
            }
            throw ex;
        } catch (ServiceException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
    }*/

    /*public static JSONObject getGoodsReceipts(HttpServletRequest request) throws ServiceException, AccountingException {
        JSONObject jObj = new JSONObject();
        Session session = null;
        try {
            session = HibernateUtil.getCurrentSession();
            jObj = VendorHandler.getGoodsReceipts(session, request, request.getParameter("start"), request.getParameter("limit"),null);
        } catch (ServiceException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
        return jObj;
    }*/
    /*public static JSONObject getVendorAgedPayable(HttpServletRequest request) throws ServiceException, AccountingException, JSONException, SessionExpiredException, ParseException {
        JSONObject jObj = new JSONObject();
        Session session = null;
        try {
            session = HibernateUtil.getCurrentSession();
            jObj = VendorHandler.getVendorAgedPayable(session, request, request.getParameter("start"), request.getParameter("limit"));
        } catch (ServiceException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
        return jObj;
    }*/

    /*public static void saveDebitNote(HttpServletRequest request) throws ServiceException, AccountingException {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getCurrentSession();
            tx = session.beginTransaction();
            VendorHandler.saveDebitNote(session, request);
            tx.commit();
        } catch (AccountingException ex) {
            if (tx != null) {
                tx.rollback();
            }
            throw ex;
        } catch (ServiceException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
    }*/

    /*public static JSONObject getDebitNotes(HttpServletRequest request) throws ServiceException, AccountingException {
        JSONObject jObj = new JSONObject();
        Session session = null;
        try {
            session = HibernateUtil.getCurrentSession();
            jObj = VendorHandler.getDebitNotes(session, request);
        } catch (ServiceException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
        return jObj;
    }*/

    /*public static JSONObject getDebitNoteRows(HttpServletRequest request) throws ServiceException, SessionExpiredException, AccountingException {
        JSONObject jObj = new JSONObject();
        Session session = null;
        try {
            session = HibernateUtil.getCurrentSession();
            jObj = VendorHandler.getDebitNoteRows(session, request);
        } catch (ServiceException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
        return jObj;
    }*/

    /*public static JSONObject getGoodsReceiptRows(HttpServletRequest request) throws ServiceException, SessionExpiredException, AccountingException {
        JSONObject jObj = new JSONObject();
        Session session = null;
        try {
            session = HibernateUtil.getCurrentSession();
            jObj = VendorHandler.getGoodsReceiptRows(session, request);
        } catch (ServiceException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
        return jObj;
    }*/

    /*public static void savePurchaseOrder(HttpServletRequest request) throws ServiceException, AccountingException {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getCurrentSession();
            tx = session.beginTransaction();
            VendorHandler.savePurchaseOrder(session, request);
            tx.commit();
        } catch (AccountingException ex) {
            if (tx != null) {
                tx.rollback();
            }
            throw ex;
        } catch (ServiceException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
    }*/

    /*public static JSONObject getPurchaseOrders(HttpServletRequest request) throws ServiceException, AccountingException {
        JSONObject jObj = new JSONObject();
        Session session = null;
        try {
            session = HibernateUtil.getCurrentSession();
            jObj = VendorHandler.getPurchaseOrders(session, request);
        } catch (ServiceException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
        return jObj;
    }*/

    /*public static JSONObject getPurchaseOrderRows(HttpServletRequest request) throws ServiceException, SessionExpiredException, AccountingException {
        JSONObject jObj = new JSONObject();
        Session session = null;
        try {
            session = HibernateUtil.getCurrentSession();
            jObj = VendorHandler.getPurchaseOrderRows(session, request);
        } catch (ServiceException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
        return jObj;
    }*/

    /*public static void saveSalesOrder(HttpServletRequest request) throws ServiceException, AccountingException {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getCurrentSession();
            tx = session.beginTransaction();
            CustomerHandler.saveSalesOrder(session, request);
            tx.commit();
        } catch (AccountingException ex) {
            if (tx != null) {
                tx.rollback();
            }
            throw ex;
        } catch (ServiceException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
    }*/

    /*public static JSONObject getSalesOrders(HttpServletRequest request) throws ServiceException {
        JSONObject jObj = new JSONObject();
        Session session = null;
        try {
            session = HibernateUtil.getCurrentSession();
            jObj = CustomerHandler.getSalesOrders(session, request);
        } catch (ServiceException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
        return jObj;
    }*/

    /*public static JSONObject getSalesOrderRows(HttpServletRequest request) throws ServiceException, SessionExpiredException {
        JSONObject jObj = new JSONObject();
        Session session = null;
        try {
            session = HibernateUtil.getCurrentSession();
            jObj = CustomerHandler.getSalesOrderRows(session, request);
        } catch (ServiceException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
        return jObj;
    }

    public static JSONObject saveReportTemplate(HttpServletRequest request) throws ServiceException, JSONException, com.krawler.utils.json.base.JSONException{
        Session session = null;
        Transaction tx = null;
        JSONObject result=new JSONObject();
        try {
            result.put("success",false);
            session = HibernateUtil.getCurrentSession();
            tx = session.beginTransaction();
            result = CustomerHandler.saveReportTemplate(session,request);
            tx.commit();
        } catch (ServiceException ex) {
            if (tx != null) {
               tx.rollback();
            }
        } catch (JSONException ex) {
            if (tx != null) {
               tx.rollback();
            }
        } finally {
            HibernateUtil.closeSession(session);
        }
        return result;
    }

    public static JSONObject getAllReportTemplate(HttpServletRequest request) throws ServiceException{
        Session session = null;
        JSONObject result=new JSONObject();
        try {
            session = HibernateUtil.getCurrentSession();
            result = CustomerHandler.getAllReportTemplate(session,request);
        } catch (ServiceException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
        return result;
    }

     public static JSONObject deleteTemplate(HttpServletRequest request) throws ServiceException{
        Session session = null;
        Transaction tx = null;
        JSONObject result=new JSONObject();
        try {
            session = HibernateUtil.getCurrentSession();
            tx = session.beginTransaction();
            result = CompanyHandler.deleteTemplate(session,request);
            tx.commit();
        } catch (ServiceException ex) {
            if (tx != null) {
               tx.rollback();
            }
        } catch (HibernateException e) {
            if (tx != null) {
               tx.rollback();
            }
            throw ServiceException.FAILURE(e.getMessage(), e);
        } finally {
            HibernateUtil.closeSession(session);
        }
        return result;
    }

    public static JSONObject editTemplate(HttpServletRequest request) throws ServiceException{
        Session session = null;
        Transaction tx = null;
        JSONObject result=new JSONObject();
        try {
            session = HibernateUtil.getCurrentSession();
            tx = session.beginTransaction();
            result = CompanyHandler.editTemplate(session,request);
            tx.commit();
        } catch (ServiceException ex) {
            if (tx != null) {
               tx.rollback();
            }
        } catch (HibernateException e) {
            if (tx != null) {
               tx.rollback();
            }
            throw ServiceException.FAILURE(e.getMessage(), e);
        } finally {
            HibernateUtil.closeSession(session);
        }
        return result;
    }*/
    public static void sendMailToCustomer(HttpServletRequest request, int mode, String billid) throws ServiceException, AccountingException{
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getCurrentSession();
            tx = session.beginTransaction();
            CustomerHandler.sendMail(session, request, mode,billid);
            tx.commit();
        } catch (DocumentException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (ServiceException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
    }

     public static String getAccountReceivableChart(HttpServletRequest request) throws ServiceException, JSONException, AccountingException {
       Session session = null;
        JSONObject jObjX = new JSONObject();
        String result = "";
         try {
             session = HibernateUtil.getCurrentSession();
             jObjX = CustomerHandler.getMonthWiseReceivable(session,request);
             JSONArray jarr = jObjX.getJSONArray("data");
             double amountreceived=0;
             double amountdue=0;
             for (int j = 0; j < jarr.length(); j++) {
                 amountreceived= jarr.getJSONObject(j).getDouble("amountreceived");
                 amountdue= jarr.getJSONObject(j).getDouble("amountdue");
                 result += jarr.getJSONObject(j).get("monthname").toString()+";"+amountreceived+";"+amountdue+"\n";
             }
        } catch (HibernateException ex) {
             throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (NumberFormatException ne) {
             throw ServiceException.FAILURE(ne.getMessage(), ne);
        } catch (JSONException je) {
             throw ServiceException.FAILURE(je.getMessage(), je);
        } catch (ServiceException se) {
             throw ServiceException.FAILURE(se.getMessage(), se);
        } finally {
            HibernateUtil.closeSession(session);
        }
        return result;
    }

   public static String getAccountPayableChart(HttpServletRequest request) throws ServiceException, JSONException, AccountingException {
       Session session = null;
        JSONObject jObjX = new JSONObject();
        String result = "";
         try {
             session = HibernateUtil.getCurrentSession();
             jObjX = VendorHandler.getMonthWisePayable(session,request);
             JSONArray jarr = jObjX.getJSONArray("data");
             double amountreceived=0;
             double amountdue=0;
             for (int j = 0; j < jarr.length(); j++) {
                 amountreceived= jarr.getJSONObject(j).getDouble("amountpayed");
                 amountdue= jarr.getJSONObject(j).getDouble("amountdue");
                 result += jarr.getJSONObject(j).get("monthname").toString()+";"+amountreceived+";"+amountdue+"\n";
             }

         } catch (HibernateException ex) {
             throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (NumberFormatException ne) {
             throw ServiceException.FAILURE(ne.getMessage(), ne);
        } catch (JSONException je) {
             throw ServiceException.FAILURE(je.getMessage(), je);
        } catch (ServiceException se) {
             throw ServiceException.FAILURE(se.getMessage(), se);
        } finally {
            HibernateUtil.closeSession(session);
        }
        return result;
    }
   public static String getAccountPayablePieChart(HttpServletRequest request) throws ServiceException, JSONException, SessionExpiredException, ParseException, AccountingException {
       Session session = null;
        JSONObject jObjX = new JSONObject();
        String result = "";
         try {
             ArrayList arr = new ArrayList();
             boolean flag=true;
             session = HibernateUtil.getCurrentSession();
             jObjX = VendorHandler.getGoodsReceipts(session,request,null);
             JSONArray jarr = jObjX.getJSONArray("data");
             String personname="";
             double amountdue=0;
             result+= "<pie>";
             for (int j = 0; j < jarr.length(); j++) {
                 flag=true;
                    for (int i=0;i<arr.size();i++)
                        if(arr.get(i).equals(jarr.getJSONObject(j).getString("personid"))){
                            flag=false;
                            break;
                        }
                    if(flag){
                        amountdue=0;
                            for(int k = 0;k < jarr.length();k++){
                                 if(jarr.getJSONObject(j).getString("personid").equals(jarr.getJSONObject(k).getString("personid")))
                                     amountdue+=jarr.getJSONObject(k).getDouble("amountdueinbase");
                            }
                    arr.add(jarr.getJSONObject(j).getString("personid"));
                    if (amountdue>0) {
                        personname= jarr.getJSONObject(j).getString("personname");
                        result += "<slice title=\"" + personname + "\" >" + amountdue  + "</slice>";
                    }
                    }
             }
             result +="</pie>";

         } catch (HibernateException ex) {
             throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (NumberFormatException ne) {
             throw ServiceException.FAILURE(ne.getMessage(), ne);
        } catch (JSONException je) {
             throw ServiceException.FAILURE(je.getMessage(), je);
        } catch (ServiceException se) {
             throw ServiceException.FAILURE(se.getMessage(), se);
        } finally {
            HibernateUtil.closeSession(session);
        }
        return result;
    }
   public static String getAccountReceivablePieChart(HttpServletRequest request) throws ServiceException, JSONException, SessionExpiredException, ParseException, AccountingException {
       Session session = null;
        JSONObject jObjX = new JSONObject();
        String result = "";
         try {
             ArrayList arr = new ArrayList();
             boolean flag=true;
             session = HibernateUtil.getCurrentSession();
             jObjX = CustomerHandler.getAgedReceivable(session,request);
             JSONArray jarr = jObjX.getJSONArray("data");
             String personname="";
             double amountdue=0;
             result+= "<pie>";
             for (int j = 0; j < jarr.length(); j++) {
                 flag=true;
                    for (int i=0;i<arr.size();i++)
                        if(arr.get(i).equals(jarr.getJSONObject(j).getString("personid"))){
                            flag=false;
                            break;
                        }
                    if(flag){
                        amountdue=0;
                            for(int k = 0;k < jarr.length();k++){
                                 if(jarr.getJSONObject(j).getString("personid").equals(jarr.getJSONObject(k).getString("personid")))
                                     amountdue+=jarr.getJSONObject(k).getDouble("amountdueinbase");
                            }
                    arr.add(jarr.getJSONObject(j).getString("personid"));
                    if (amountdue>0) {
                        personname= jarr.getJSONObject(j).getString("personname");
                        result += "<slice title=\"" + personname + "\" >" + amountdue  + "</slice>";
                    }
                    }
             }
             result +="</pie>";

         } catch (HibernateException ex) {
             throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (NumberFormatException ne) {
             throw ServiceException.FAILURE(ne.getMessage(), ne);
        } catch (JSONException je) {
             throw ServiceException.FAILURE(je.getMessage(), je);
        } catch (ServiceException se) {
             throw ServiceException.FAILURE(se.getMessage(), se);
        } finally {
            HibernateUtil.closeSession(session);
        }
        return result;
    }
    public static String getTopCustomerChart(HttpServletRequest request) throws ServiceException, JSONException, AccountingException {
       Session session = null;
        JSONObject jObjX = new JSONObject();
        String result = "";
         try {
             int personlimit=Integer.parseInt(request.getParameter("personlimit"));
             session = HibernateUtil.getCurrentSession();
             jObjX = CustomerHandler.getTopCustomersChart(session,request,personlimit);
             JSONArray jarr = jObjX.getJSONArray("data");
              double amount=0;
               String personname="";
             result = "<chart><series>";
             for (int j = 0; j < jarr.length(); j++) {
                  personname= jarr.getJSONObject(j).getString("personname");
                 result += "<value xid=\"" + j + "\" >" + personname  + "</value>";
             }
             result += "</series><graphs><graph gid=\"0\">";
             for (int k = 0; k < jarr.length(); k++) {
                  amount= jarr.getJSONObject(k).getDouble("amount");
                 result += "<value xid=\"" + k + "\" >" + amount + "</value>";
             }
             result += "</graph>";
              result += "</graphs></chart>";
        } catch (HibernateException ex) {
             throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (NumberFormatException ne) {
             throw ServiceException.FAILURE(ne.getMessage(), ne);
        } catch (JSONException je) {
             throw ServiceException.FAILURE(je.getMessage(), je);
        } catch (ServiceException se) {
             throw ServiceException.FAILURE(se.getMessage(), se);
        } finally {
            HibernateUtil.closeSession(session);
        }
        return result;
    }
     public static String getTopVendorsChart(HttpServletRequest request) throws ServiceException, JSONException, AccountingException, SessionExpiredException {
       Session session = null;
        JSONObject jObjX = new JSONObject();
        String result = "";
         try {
             int personlimit=Integer.parseInt(request.getParameter("personlimit"));
             session = HibernateUtil.getCurrentSession();
             jObjX = VendorHandler.getTopVendorsChart(session,request,personlimit);
             JSONArray jarr = jObjX.getJSONArray("data");
              double amount=0;
               String personname="";
             result = "<chart><series>";
             for (int j = 0; j < jarr.length(); j++) {
                  personname= jarr.getJSONObject(j).getString("personname");
                 result += "<value xid=\"" + j + "\" >" + personname  + "</value>";
             }
             result += "</series><graphs><graph gid=\"0\">";
             for (int k = 0; k < jarr.length(); k++) {
                  amount= jarr.getJSONObject(k).getDouble("amount");
                 result += "<value xid=\"" + k + "\" >" + amount + "</value>";
             }
             result += "</graph>";
              result += "</graphs></chart>";
        } catch (HibernateException ex) {
             throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (NumberFormatException ne) {
             throw ServiceException.FAILURE(ne.getMessage(), ne);
        } catch (JSONException je) {
             throw ServiceException.FAILURE(je.getMessage(), je);
        } catch (ServiceException se) {
             throw ServiceException.FAILURE(se.getMessage(), se);
        } finally {
            HibernateUtil.closeSession(session);
        }
        return result;
    }

    /*public static String saveGroup(HttpServletRequest request) throws ServiceException, AccountingException {
        Session session = null;
        Transaction tx = null;
        String groupID=null;
        try {
            session = HibernateUtil.getCurrentSession();
            tx = session.beginTransaction();
            groupID=CompanyHandler.saveGroup(session, request);
            tx.commit();
        } catch (AccountingException ex) {
            if (tx != null) {
                tx.rollback();
            }
            throw ex;
        } catch (ServiceException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (HibernateException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
        return groupID;
    }*/

    /*public static void deleteGroup(HttpServletRequest request) throws ServiceException {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getCurrentSession();
            tx = session.beginTransaction();
            CompanyHandler.deleteGroup(session, request);
            tx.commit();
        } catch (ServiceException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (ConstraintViolationException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("Cannot delete group, Dependencies exist.", ex);
        } catch (HibernateException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
    }*/

    /*public static void deleteJournalEntries(HttpServletRequest request) throws ServiceException, JSONException, AccountingException, SessionExpiredException {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getCurrentSession();
            tx = session.beginTransaction();
            CompanyHandler.deleteJournalEntries(session, request);
            tx.commit();
        } catch (ServiceException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (ConstraintViolationException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("Cannot delete account, Dependencies exist.", ex);
        } catch (HibernateException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
    }*/

    /*public static void deletePurchaseOrders(HttpServletRequest request) throws ServiceException, JSONException, AccountingException, SessionExpiredException {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getCurrentSession();
            tx = session.beginTransaction();
            VendorHandler.deletePurchaseOrders(session, request);
            tx.commit();
        } catch (ServiceException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (ConstraintViolationException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("Cannot delete account, Dependencies exist.", ex);
        } catch (HibernateException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
    }*/

    /*public static void deleteSalesOrders(HttpServletRequest request) throws ServiceException, JSONException, AccountingException, SessionExpiredException {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getCurrentSession();
            tx = session.beginTransaction();
            CustomerHandler.deleteSalesOrders(session, request);
            tx.commit();
        } catch (ServiceException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (ConstraintViolationException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("Cannot delete account, Dependencies exist.", ex);
        } catch (HibernateException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
    }*/

    /*public static void deleteCreditNotes(HttpServletRequest request) throws ServiceException, JSONException, AccountingException, SessionExpiredException {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getCurrentSession();
            tx = session.beginTransaction();
            CustomerHandler.deleteCreditNotes(session, request);
            tx.commit();
        } catch (ServiceException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (ConstraintViolationException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("Cannot delete account, Dependencies exist.", ex);
        } catch (HibernateException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
    }*/

    /*public static void deleteDebitNotes(HttpServletRequest request) throws ServiceException, JSONException, AccountingException, SessionExpiredException {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getCurrentSession();
            tx = session.beginTransaction();
            VendorHandler.deleteDebitNotes(session, request);
            tx.commit();
        } catch (ServiceException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (ConstraintViolationException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("Cannot delete account, Dependencies exist.", ex);
        } catch (HibernateException ex) {
            if (tx != null) {
                tx.rollback();
            }
            Logger.getLogger(AccountDBCon.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
    }*/
}

