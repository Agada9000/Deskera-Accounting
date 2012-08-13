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

package com.krawler.spring.accounting.vendor;

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
public class accVendorDAOImplTest extends AbstractTransactionalDataSourceSpringContextTests {
    private SessionFactory sessionFactory = null;
    //company id for demo
    private static String companyid = "a4792363-b0e1-4b67-992b-2851234d5ea6";
    private static String accountID = "ff8080812be6edcc012be6fcc6860002";

    public accVendorDAOImplTest(String testName) {
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
     * Test of addVendor method, of class accVendorDAOImpl.
     */
    public void testAddVendor() {
        System.out.println("addVendor");
        try{
            SimpleDateFormat sdf=new SimpleDateFormat("MMMM d, yyyy hh:mm:ss aa");
            String tzdiff = "-07:00";
            sdf.setTimeZone(TimeZone.getTimeZone("GMT"+tzdiff));
            String currencyid="5";
            String customerid = "";//request.getParameter("accid");

            boolean issub = true;
            boolean debitType = false;
            double openBalance = 0;
            openBalance = debitType ? openBalance : -openBalance;
            String parentid = "c340667e254308e70125448edf450090";
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
            accjson.put("groupid", Group.ACCOUNTS_PAYABLE);
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
            requestParams.put("debitType", false);
            requestParams.put("openbalance", "10");
            requestParams.put("title", "Mr.");
            requestParams.put("address", "Test Address1");
            requestParams.put("bankaccountno", "32434234");
            requestParams.put("email", "sagar@mailinator.com");
            requestParams.put("contactno", "");
            requestParams.put("contactno2", "");
            requestParams.put("fax", "");
            requestParams.put("shippingaddress", "Test Address1");
            requestParams.put("termid", "c340667e25dfadf80125e32383f9003b");
            requestParams.put("other", "Test Information");
            requestParams.put("taxno", "");
            requestParams.put("taxidnumber", "Test TaxID");
            requestParams.put("taxeligible", true);
            requestParams.put("companyid", companyid);

            accAccountDAOImpl instance = new accAccountDAOImpl();
            instance.setSessionFactory(sessionFactory);
            KwlReturnObject accresult = instance.addAccount(accjson);
            Account account = (Account) accresult.getEntityList().get(0);
            requestParams.put("accountid", account.getID());

            accVendorDAOImpl instance1 = new accVendorDAOImpl();
            instance1.setSessionFactory(sessionFactory);
            KwlReturnObject result = instance1.addVendor(requestParams);
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
     * Test of updateVendor method, of class accVendorDAOImpl.
     */
    public void testUpdateVendor() {
        System.out.println("updateVendor");
        try{
            SimpleDateFormat sdf=new SimpleDateFormat("MMMM d, yyyy hh:mm:ss aa");
            String tzdiff = "-07:00";
            sdf.setTimeZone(TimeZone.getTimeZone("GMT"+tzdiff));
            String currencyid="5";
            String customerid = accountID;//request.getParameter("accid");

            boolean issub = true;
            boolean debitType = false;
            double openBalance = 0;
            openBalance = debitType ? openBalance : -openBalance;
            String parentid = "c340667e254308e70125448edf450090";
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
            accjson.put("groupid", Group.ACCOUNTS_PAYABLE);
            accjson.put("companyid", companyid);
            accjson.put("currencyid", currencyid);
            accjson.put("life", life);
            accjson.put("salvage", salvage);
            accjson.put("creationdate", creationDate);
            accjson.put("category", "");

            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("accid", customerid);
            requestParams.put("accname", "Test-Data1");
            requestParams.put("parentid", parentid);
            requestParams.put("issub", "on");
            requestParams.put("debitType", false);
            requestParams.put("openbalance", "10");
            requestParams.put("title", "Mr.");
            requestParams.put("address", "Test Address1");
            requestParams.put("bankaccountno", "32434234");
            requestParams.put("email", "sagar@mailinator.com");
            requestParams.put("contactno", "");
            requestParams.put("contactno2", "");
            requestParams.put("fax", "");
            requestParams.put("shippingaddress", "Test Address1");
            requestParams.put("termid", "c340667e25dfadf80125e32383f9003b");
            requestParams.put("other", "Test Information1");
            requestParams.put("taxno", "");
            requestParams.put("taxidnumber", "Test TaxID1");
            requestParams.put("taxeligible", true);
            requestParams.put("companyid", companyid);

            accAccountDAOImpl instance = new accAccountDAOImpl();
            instance.setSessionFactory(sessionFactory);
            KwlReturnObject accresult = instance.updateAccount(accjson);
            Account account = (Account) accresult.getEntityList().get(0);
            requestParams.put("accountid", account.getID());

            accVendorDAOImpl instance1 = new accVendorDAOImpl();
            instance1.setSessionFactory(sessionFactory);
            KwlReturnObject result = instance1.updateVendor(requestParams);
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
     * Test of getVendor method, of class accVendorDAOImpl.
     */
    public void testGetVendor() throws Exception {
        System.out.println("getVendor");
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        String[] groups = new String[] {"13"};
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

        accVendorDAOImpl instance = new accVendorDAOImpl();
        instance.setSessionFactory(sessionFactory);
        KwlReturnObject result = instance.getVendor(requestParams);
        //To Check success flag in KWLReturnObject
        assertTrue("Success Flag false : ", result.isSuccessFlag());
        //To Check if fetched list is not null
        assertNotNull("Result Entrylist is NULL : ", result.getEntityList());

        System.out.println("No. of records fetched : "+result.getRecordTotalCount());
    }

    /**
     * Test of getVendorForAgedPayable method, of class accVendorDAOImpl.
     */
    public void testGetVendorForAgedPayable() throws Exception {
        System.out.println("getVendorForAgedPayable");
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put("companyid", companyid);

        accVendorDAOImpl instance = new accVendorDAOImpl();
        instance.setSessionFactory(sessionFactory);
        KwlReturnObject result = instance.getVendorForAgedPayable(requestParams);
        //To Check success flag in KWLReturnObject
        assertTrue("Success Flag false : ", result.isSuccessFlag());
        //To Check if fetched list is not null
        assertNotNull("Result Entrylist is NULL : ", result.getEntityList());

        System.out.println("No. of records fetched : "+result.getRecordTotalCount());
    }

    /**
     * Test of deleteVendor method, of class accVendorDAOImpl.
     */
    public void testDeleteVendor() throws Exception {
        System.out.println("deleteVendor");
        accVendorDAOImpl instance = new accVendorDAOImpl();
        instance.setSessionFactory(sessionFactory);
        KwlReturnObject result = instance.deleteVendor(accountID, companyid);
        //commit the transaction to verify result in database.
//        transactionManager.commit(transactionStatus);
        System.out.println("No. of records deleted : "+result.getRecordTotalCount());
        assertTrue("Deletion failed ", (result.getRecordTotalCount() == 1));
    }

    /**
     * Test of getVendor_Dashboard method, of class accVendorDAOImpl.
     */
    public void testGetVendor_Dashboard() throws Exception {
        System.out.println("getVendor_Dashboard");
        boolean isnull = true;
        String orderby = "createdOn";
        int start = 0;
        int limit = 2;

        accVendorDAOImpl instance = new accVendorDAOImpl();
        instance.setSessionFactory(sessionFactory);
        KwlReturnObject result = instance.getVendor_Dashboard(companyid, isnull, orderby, start, limit);
        //To Check success flag in KWLReturnObject
        assertTrue("Success Flag false : ", result.isSuccessFlag());
        //To Check if fetched list is not null
        assertNotNull("Result Entrylist is NULL : ", result.getEntityList());

        System.out.println("No. of records fetched : "+result.getRecordTotalCount());

        isnull = false;
        orderby = "modifiedOn";
        result = instance.getVendor_Dashboard(companyid, isnull, orderby, start, limit);
        //To Check success flag in KWLReturnObject
        assertTrue("Success Flag false : ", result.isSuccessFlag());
        //To Check if fetched list is not null
        assertNotNull("Result Entrylist is NULL : ", result.getEntityList());

        System.out.println("No. of records fetched : "+result.getRecordTotalCount());
    }

    /**
     * Test of getVendorList method, of class accVendorDAOImpl.
     */
    public void testGetVendorList() throws Exception {
        System.out.println("getVendorList");
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

        accVendorDAOImpl instance = new accVendorDAOImpl();
        instance.setSessionFactory(sessionFactory);
        KwlReturnObject result = instance.getVendorList(requestParams);
        //To Check success flag in KWLReturnObject
        assertTrue("Success Flag false : ", result.isSuccessFlag());
        //To Check if fetched list is not null
        assertNotNull("Result Entrylist is NULL : ", result.getEntityList());

        System.out.println("No. of records fetched : "+result.getRecordTotalCount());
    }

    public void testGet1099EligibleVendor_withcompany() throws Exception {
        System.out.println("get1099EligibleVendor");
        accVendorDAOImpl instance = new accVendorDAOImpl();
        instance.setSessionFactory(sessionFactory);
        KwlReturnObject result = instance.get1099EligibleVendor(companyid,null);
        //To Check success flag in KWLReturnObject
        assertTrue("Success Flag false : ", result.isSuccessFlag());
        //To Check if fetched list is not null
        assertNotNull("Result Entrylist is NULL : ", result.getEntityList());

        System.out.println("No. of records fetched : "+result.getRecordTotalCount());
    }
    public void testGet1099EligibleVendor_withoutcompany() throws Exception {
        System.out.println("get1099EligibleVendor");
        accVendorDAOImpl instance = new accVendorDAOImpl();
        instance.setSessionFactory(sessionFactory);
        KwlReturnObject result = instance.get1099EligibleVendor("","abc");
        //To Check success flag in KWLReturnObject
        assertTrue("Success Flag false : ", result.isSuccessFlag());
        //To Check if fetched list is not null
        assertNotNull("Result Entrylist is NULL : ", result.getEntityList());

        System.out.println("No. of records fetched : "+result.getRecordTotalCount());
    }

}
