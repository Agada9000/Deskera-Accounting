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

import com.krawler.common.service.ServiceException;
import com.krawler.hql.accounting.Account;
import com.krawler.hql.accounting.Group;
import com.krawler.spring.accounting.account.accAccountDAOImpl;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import com.mchange.util.AssertException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;
import org.hibernate.SessionFactory;
import org.springframework.test.AbstractTransactionalDataSourceSpringContextTests;

/**
 *
 * @author sagar
 */
public class accCustomerDAOImplTest extends AbstractTransactionalDataSourceSpringContextTests {
    private SessionFactory sessionFactory = null;
    //company id for demo
    private static String companyid = "a4792363-b0e1-4b67-992b-2851234d5ea6";
    private static String accountID = "c340667e266e177d01266f823c130145";

    public accCustomerDAOImplTest(String testName) {
        super(testName);
    }

    /*
     *** spring version 2.5.6 and above when extending from AbstractJUnit38SpringContextTests
     *
     *
    protected AbstractXmlApplicationContext createApplicationContext() {
    return new ClassPathXmlApplicationContext("test-applicationContext.xml");
    }*/
    protected String[] getConfigLocations() {
        return new String[]{"test-applicationContext.xml"};
    }

    /**
     * Spring will automatically inject the Hibernate session factory on startup
     * @param sessionFactory
     */
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /**
     * Test of setSessionFactory method, of class accUomImpl.
     */
    public void testSetSessionFactory() {
        System.out.println("setSessionFactory");
        assertNotNull(sessionFactory);
    }

    /**
     * Test of addCustomer method, of class accCustomerDAOImpl.
     */
    public void testAddCustomer() {
        System.out.println("addCustomer");
        try{
            SimpleDateFormat sdf=new SimpleDateFormat("MMMM d, yyyy hh:mm:ss aa");
            String tzdiff = "-07:00";
            sdf.setTimeZone(TimeZone.getTimeZone("GMT"+tzdiff));
            String currencyid="1";
            String customerid = "";//request.getParameter("accid");

            boolean issub = true;
            boolean debitType = false;
            double openBalance = 0;
            openBalance = debitType ? openBalance : -openBalance;
            String parentid = "c340667e277b61e001277b91b553005d";
            if (!issub) {
                parentid = null;
            }

            double life = 0;
            double salvage = 0;
            Date creationDate = sdf.parse("Oct 20, 2010 12:00:00 AM");
            if (creationDate == null) {
                creationDate = new Date();
            }

            JSONObject accjson = new JSONObject();
            accjson.put("accountid", customerid);
            accjson.put("name", "Test-Data");
            accjson.put("balance", openBalance);
            accjson.put("parentid", parentid);
            accjson.put("groupid", Group.ACCOUNTS_RECEIVABLE);
            accjson.put("companyid", companyid);
            accjson.put("currencyid", currencyid);
            accjson.put("life", life);
            accjson.put("salvage", salvage);
            accjson.put("creationdate", creationDate);
            accjson.put("category", "");

            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("accid", customerid);
            requestParams.put("accname", "Test-Data");
            requestParams.put("parentid", parentid);
            requestParams.put("issub", "on");
            requestParams.put("debitType", "false");
            requestParams.put("openbalance", "10");
            requestParams.put("title", "Mr.");
            requestParams.put("address", "Test Address1");
            requestParams.put("bankaccountno", "32434234");
            requestParams.put("email", "sagar.ahire@mailinator.com");
            requestParams.put("contactno", "");
            requestParams.put("contactno2", "");
            requestParams.put("fax", "");
            requestParams.put("shippingaddress", "Test Address1");
            requestParams.put("termid", "c340667e26bbb6290126bbc4a7a90001");
            requestParams.put("other", "Test Information");
            requestParams.put("taxno", "Test TaxID");
            requestParams.put("companyid", companyid);

            accAccountDAOImpl instance = new accAccountDAOImpl();
            instance.setSessionFactory(sessionFactory);
            KwlReturnObject accresult = instance.addAccount(accjson);
            Account account = (Account) accresult.getEntityList().get(0);
            requestParams.put("accountid", account.getID());

            accCustomerDAOImpl instance1 = new accCustomerDAOImpl();
            instance1.setSessionFactory(sessionFactory);
            KwlReturnObject result = instance1.addCustomer(requestParams);
            //commit the transaction to verify result in database.
    //        transactionManager.commit(transactionStatus);
            assertTrue("Success flag : ", result.isSuccessFlag());
            assertEquals("Object returned on add : ", 1, result.getEntityList().size());
        } catch (ServiceException ex) {
            throw new AssertException("Service Exception - error creating user session - "+ ex.getMessage());
        } catch (JSONException ex) {
            throw new AssertException("JSON Exception - error parsing model json - "+ ex.getMessage());
        } catch (Exception ex) {
            throw new AssertException("Exception - error parsing model json - "+ ex.getMessage());
        }
    }

    /**
     * Test of updateCustomer method, of class accCustomerDAOImpl.
     */
    public void testUpdateCustomer() {
        System.out.println("updateCustomer");
        try{
            SimpleDateFormat sdf=new SimpleDateFormat("MMMM d, yyyy hh:mm:ss aa");
            String tzdiff = "-07:00";
            sdf.setTimeZone(TimeZone.getTimeZone("GMT"+tzdiff));
            String currencyid="1";
            String customerid = accountID;//request.getParameter("accid");

            boolean issub = true;
            boolean debitType = false;
            double openBalance = 0;
            openBalance = debitType ? openBalance : -openBalance;
            String parentid = "c340667e277b61e001277b91b553005d";
            if (!issub) {
                parentid = null;
            }

            double life = 0;
            double salvage = 0;
            Date creationDate = sdf.parse("Oct 20, 2010 12:00:00 AM");
            if (creationDate == null) {
                creationDate = new Date();
            }

            JSONObject accjson = new JSONObject();
            accjson.put("accountid", customerid);
            accjson.put("name", "Test-Data");
            accjson.put("balance", openBalance);
            accjson.put("parentid", parentid);
            accjson.put("groupid", Group.ACCOUNTS_RECEIVABLE);
            accjson.put("companyid", companyid);
            accjson.put("currencyid", currencyid);
            accjson.put("life", life);
            accjson.put("salvage", salvage);
            accjson.put("creationdate", creationDate);
            accjson.put("category", "");

            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("accid", customerid);
            requestParams.put("accname", "Test-Data");
            requestParams.put("parentid", parentid);
            requestParams.put("issub", "on");
            requestParams.put("debitType", "false");
            requestParams.put("openbalance", "10");
            requestParams.put("title", "Mr.");
            requestParams.put("address", "Test Address1");
            requestParams.put("bankaccountno", "32434234");
            requestParams.put("email", "sagar.ahire@mailinator.com");
            requestParams.put("contactno", "");
            requestParams.put("contactno2", "");
            requestParams.put("fax", "");
            requestParams.put("shippingaddress", "Test Address1");
            requestParams.put("termid", "c340667e26bbb6290126bbc4a7a90001");
            requestParams.put("other", "Test Information");
            requestParams.put("taxno", "Test TaxID");
            requestParams.put("companyid", companyid);

            accAccountDAOImpl instance = new accAccountDAOImpl();
            instance.setSessionFactory(sessionFactory);
            KwlReturnObject accresult = instance.updateAccount(accjson);
            Account account = (Account) accresult.getEntityList().get(0);
            requestParams.put("accountid", account.getID());

            accCustomerDAOImpl instance1 = new accCustomerDAOImpl();
            instance1.setSessionFactory(sessionFactory);
            KwlReturnObject result = instance1.updateCustomer(requestParams);
            //commit the transaction to verify result in database.
    //        transactionManager.commit(transactionStatus);
            assertTrue("Success flag : ", result.isSuccessFlag());
            assertEquals("Object returned on add : ", 1, result.getEntityList().size());
        } catch (ServiceException ex) {
            throw new AssertException("Service Exception - error creating user session - "+ ex.getMessage());
        } catch (JSONException ex) {
            throw new AssertException("JSON Exception - error parsing model json - "+ ex.getMessage());
        } catch (Exception ex) {
            throw new AssertException("Exception - error parsing model json - "+ ex.getMessage());
        }
    }

    /**
     * Test of getCustomer_Dashboard method, of class accCustomerDAOImpl.
     */
    public void testGetCustomer_Dashboard() throws Exception {
        System.out.println("getCustomer_Dashboard");
        boolean isnull = true;
        String orderby = "createdOn";
        int start = 0;
        int limit = 2;

        accCustomerDAOImpl instance = new accCustomerDAOImpl();
        instance.setSessionFactory(sessionFactory);
        KwlReturnObject result = instance.getCustomer_Dashboard(companyid, isnull, orderby, start, limit);
        //To Check success flag in KWLReturnObject
        assertTrue("Success Flag false : ", result.isSuccessFlag());
        //To Check if fetched list is not null
        assertNotNull("Result Entrylist is NULL : ", result.getEntityList());

        System.out.println("No. of records fetched : "+result.getRecordTotalCount());

        isnull = false;
        orderby = "modifiedOn";
        result = instance.getCustomer_Dashboard(companyid, isnull, orderby, start, limit);
        //To Check success flag in KWLReturnObject
        assertTrue("Success Flag false : ", result.isSuccessFlag());
        //To Check if fetched list is not null
        assertNotNull("Result Entrylist is NULL : ", result.getEntityList());

        System.out.println("No. of records fetched : "+result.getRecordTotalCount());
    }

    /**
     * Test of deleteCustomer method, of class accCustomerDAOImpl.
     */
    public void testDeleteCustomer() throws Exception {
        System.out.println("deleteCustomer");
        accCustomerDAOImpl instance = new accCustomerDAOImpl();
        instance.setSessionFactory(sessionFactory);
        KwlReturnObject result = instance.deleteCustomer(accountID, companyid);
        //commit the transaction to verify result in database.
//        transactionManager.commit(transactionStatus);
        System.out.println("No. of records deleted : "+result.getRecordTotalCount());
        assertTrue("Deletion failed ", (result.getRecordTotalCount() == 1));
    }

    /**
     * Test of getCustomerForAgedReceivable method, of class accCustomerDAOImpl.
     */
    public void testGetCustomerForAgedReceivable() throws Exception {
        System.out.println("getCustomerForAgedReceivable");
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put("companyid", companyid);

        accCustomerDAOImpl instance = new accCustomerDAOImpl();
        instance.setSessionFactory(sessionFactory);
        KwlReturnObject result = instance.getCustomerForAgedReceivable(requestParams);
        //To Check success flag in KWLReturnObject
        assertTrue("Success Flag false : ", result.isSuccessFlag());
        //To Check if fetched list is not null
        assertNotNull("Result Entrylist is NULL : ", result.getEntityList());

        System.out.println("No. of records fetched : "+result.getRecordTotalCount());
    }

    public void testGetCustomerForAgedReceivable_accidFilter() throws Exception {
        System.out.println("getCustomerForAgedReceivable");
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put("companyid", companyid);
        requestParams.put("accid", accountID);

        accCustomerDAOImpl instance = new accCustomerDAOImpl();
        instance.setSessionFactory(sessionFactory);
        KwlReturnObject result = instance.getCustomerForAgedReceivable(requestParams);
        //To Check success flag in KWLReturnObject
        assertTrue("Success Flag false : ", result.isSuccessFlag());
        //To Check if fetched list is not null
        assertNotNull("Result Entrylist is NULL : ", result.getEntityList());

        System.out.println("No. of records fetched : "+result.getRecordTotalCount());
    }

    /**
     * Test of getCustomer method, of class accCustomerDAOImpl.
     */
    public void testGetCustomer() throws Exception {
        System.out.println("getCustomer");
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        String[] groups = new String[] {"10"};
        requestParams.put("group", groups);
        requestParams.put("deleted", "false");
        requestParams.put("nondeleted", "false");
        requestParams.put("ss", "");
        requestParams.put("start", "0");
        requestParams.put("limit", "15");
        requestParams.put("companyid", companyid);
//        requestParams.put("query", request.getParameter("query"));
//        requestParams.put("ignore", request.getParameter("ignore"));
//        requestParams.put("ignorecustomers", request.getParameter("ignorecustomers"));
//        requestParams.put("ignorevendors", request.getParameter("ignorevendors"));
//        requestParams.put("accountid", request.getParameter("accountid"));
//        requestParams.put("currencyid", sessionHandlerImpl.getCurrencyID(request));

        accCustomerDAOImpl instance = new accCustomerDAOImpl();
        instance.setSessionFactory(sessionFactory);
        KwlReturnObject result = instance.getCustomer(requestParams);
        //To Check success flag in KWLReturnObject
        assertTrue("Success Flag false : ", result.isSuccessFlag());
        //To Check if fetched list is not null
        assertNotNull("Result Entrylist is NULL : ", result.getEntityList());

        System.out.println("No. of records fetched : "+result.getRecordTotalCount());
    }

    /**
     * Test of getCustomerList method, of class accCustomerDAOImpl.
     */
    public void testGetCustomerList() throws Exception {
        System.out.println("getCustomerList");
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put("start", "0");
        requestParams.put("limit", "15");

        ArrayList filter_names = new ArrayList(),filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
        filter_names.add("company.companyID");
        filter_params.add(companyid);
        filter_names.add("ISaccount.deleted");
        filter_params.add(false);
        requestParams.put("filter_names", filter_names);
        requestParams.put("filter_params", filter_params);
        order_by.add("account.category");
        order_type.add("desc");
        requestParams.put("order_by", order_by);
        requestParams.put("order_type", order_type);

        accCustomerDAOImpl instance = new accCustomerDAOImpl();
        instance.setSessionFactory(sessionFactory);
        KwlReturnObject result = instance.getCustomerList(requestParams);
        //To Check success flag in KWLReturnObject
        assertTrue("Success Flag false : ", result.isSuccessFlag());
        //To Check if fetched list is not null
        assertNotNull("Result Entrylist is NULL : ", result.getEntityList());

        System.out.println("No. of records fetched : "+result.getRecordTotalCount());
    }

}
