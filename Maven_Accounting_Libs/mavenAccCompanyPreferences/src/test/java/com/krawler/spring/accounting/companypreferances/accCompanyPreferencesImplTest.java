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

package com.krawler.spring.accounting.companypreferances;

import com.krawler.spring.common.KwlReturnObject;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import org.hibernate.SessionFactory;
import org.springframework.test.AbstractTransactionalDataSourceSpringContextTests;

/**
 *
 * @author sagar
 */
public class accCompanyPreferencesImplTest extends AbstractTransactionalDataSourceSpringContextTests {
    private SessionFactory sessionFactory = null;
    //company id for demo
    private static String companyid = "a4792363-b0e1-4b67-992b-2851234d5ea6";
    private static String accountID = "";

    public accCompanyPreferencesImplTest(String testName) {
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
     * Test of getNextAutoNumber method, of class accCompanyPreferencesImpl.
     */
//    public void testGetNextAutoNumber() throws Exception {
//        System.out.println("getNextAutoNumber");
//        String companyid = "";
//        int from = 0;
//        accCompanyPreferencesImpl instance = new accCompanyPreferencesImpl();
//        String expResult = "";
//        String result = instance.getNextAutoNumber(companyid, from);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    /**
     * Test of getPreferencesFromAccount method, of class accCompanyPreferencesImpl.
     */
    public void testGetPreferencesFromAccount() throws Exception {
        System.out.println("getPreferencesFromAccount");
        accCompanyPreferencesImpl instance = new accCompanyPreferencesImpl();
        instance.setSessionFactory(sessionFactory);
        KwlReturnObject result = instance.getPreferencesFromAccount(accountID, companyid);
        //To Check success flag in KWLReturnObject
        assertTrue("Success Flag false : ", result.isSuccessFlag());
        //To Check if fetched list is not null
        assertNotNull("Result Entrylist is NULL : ", result.getEntityList());

        System.out.println("No. of records fetched : "+result.getRecordTotalCount());
    }

    /**
     * Test of getCompanyPreferences method, of class accCompanyPreferencesImpl.
     */
    public void testGetCompanyPreferences() throws Exception {
        System.out.println("getCompanyPreferences");
        Map<String, Object> filterParams = new HashMap<String, Object>();
        accCompanyPreferencesImpl instance = new accCompanyPreferencesImpl();
        instance.setSessionFactory(sessionFactory);
        KwlReturnObject result = instance.getCompanyPreferences(filterParams);
        //To Check success flag in KWLReturnObject
        assertTrue("Success Flag false : ", result.isSuccessFlag());
        //To Check if fetched list is not null
        assertNotNull("Result Entrylist is NULL : ", result.getEntityList());

        System.out.println("No. of records fetched : "+result.getRecordTotalCount());
    }

    public void testGetCompanyPreferences_withID() throws Exception {
        System.out.println("getCompanyPreferences");
        Map<String, Object> filterParams = new HashMap<String, Object>();
        filterParams.put("id", companyid);
        accCompanyPreferencesImpl instance = new accCompanyPreferencesImpl();
        instance.setSessionFactory(sessionFactory);
        KwlReturnObject result = instance.getCompanyPreferences(filterParams);
        //To Check success flag in KWLReturnObject
        assertTrue("Success Flag false : ", result.isSuccessFlag());
        //To Check if fetched list is not null
        assertNotNull("Result Entrylist is NULL : ", result.getEntityList());

        System.out.println("No. of records fetched : "+result.getRecordTotalCount());
    }

    /**
     * Test of addPreferences method, of class accCompanyPreferencesImpl.
     */
    public void testAddPreferences() throws Exception {
        System.out.println("addPreferences");
        HashMap<String, Object> prefMap = new HashMap<String, Object>();
        SimpleDateFormat sdf=new SimpleDateFormat("MMMM d, yyyy hh:mm:ss aa");
        String tzdiff = "-07:00";
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"+tzdiff));

        prefMap.put("fyfrom", sdf.parse("Dec 31, 2010 12:00:00 AM"));
        prefMap.put("bbfrom", sdf.parse("Dec 31, 2009 12:00:00 AM"));
        prefMap.put("firstfyfrom", sdf.parse("Dec 31, 2010 12:00:00 AM"));
        prefMap.put("withoutinventory", false);
//        if(!StringUtil.isNullOrEmpty(request.getParameter("withouttax1099")))
//            prefMap.put("withouttax1099", request.getParameter("withouttax1099"));
        prefMap.put("emailinvoice", false);
        prefMap.put("companyid", companyid);

        prefMap.put("autobillingcashpurchase", "CP000000");
        prefMap.put("autobillingcashsales", "CS000000");
        prefMap.put("autobillingcreditmemo", "CN000000");
        prefMap.put("autobillingdebitnote", "DN00000011");
        prefMap.put("autobillinggoodsreceipt", "VINV000000");
        prefMap.put("autobillinginvoice", "Deskera/000000");
        prefMap.put("autobillingpayment", "MP000000");
        prefMap.put("autobillingpo", "PO000000");
        prefMap.put("autobillingreceipt", "RP000000");
        prefMap.put("autobillingso", "SO000000");
        prefMap.put("autocashpurchase", "CP000000");
        prefMap.put("autocashsales", "CS000000");
        prefMap.put("autocreditmemo", "CN000000");
        prefMap.put("autodebitnote", "DN000000");
        prefMap.put("autogoodsreceipt", "VINV000000");
        prefMap.put("autoinvoice", "KWL/00000INV0");
        prefMap.put("autojournalentry", "JE000000");
        prefMap.put("autopayment", "MP000000");
        prefMap.put("autopo", "PO/000/KWL/000");
        prefMap.put("autoreceipt", "A0B00C000");
        prefMap.put("autoso", "SO/N000000");
        prefMap.put("cashaccount", "c340667e23abce0b0123ae49d2d60122");
//        String data = "[{id:\"c340667e268ec5f401268ecbc50c0008\",name:\"2011\",islock:\"false\",modified:true},{id:\"c340667e268ec5f401268ecbc50c0009\",name:\"2012\",islock:\"false\",modified:true},{id:\"c340667e268ec5f401268ecbc50c000a\",name:\"2013\",islock:\"false\",modified:true},{id:\"c340667e268ec5f401268ecbc50c000b\",name:\"2014\",islock:\"false\",modified:true},{id:\"c340667e268ec5f401268ecbc50c000c\",name:\"2015\",islock:\"false\",modified:true},{id:\"c340667e268ec5f401268ecbc50d000d\",name:\"2009\",islock:\"true\",modified:true},{id:\"c340667e268ec5f401268ecbc50d000e\",name:\"2008\",islock:\"false\",modified:true},{id:\"c340667e268ec5f401268ecbc50d000f\",name:\"2007\",islock:\"false\",modified:true},{id:\"c340667e268ec5f401268ecbc50d0010\",name:\"2006\",islock:\"false\",modified:true},{id:\"c340667e268ec5f401268ecbc50d0011\",name:\"2005\",islock:\"false\",modified:true}]";
//        prefMap.put("data", data);
//        prefMap.put("daysid", "31");
//        prefMap.put("monthid", "December");
//        prefMap.put("yearid", "2009");
        prefMap.put("depreciationaccount", "c340667e270a192501270a7e9aab0057");
        prefMap.put("discountgiven", "c340667e27f5e71d0127f67cdad60011");
        prefMap.put("discountreceived", "c340667e27f5e71d0127f67d432e0013");
        prefMap.put("emailinvoice", false);
        prefMap.put("foreignexchange", "c340667e268ec5f401268ecafd680006");
        prefMap.put("mode", "82");
        prefMap.put("othercharges", "c340667e24fb25300124fc4744dd002c");
//        prefMap.put("id", companyid);

        accCompanyPreferencesImpl instance = new accCompanyPreferencesImpl();
        instance.setSessionFactory(sessionFactory);
        KwlReturnObject result = instance.addPreferences(prefMap);
        //commit the transaction to verify result in database.
//                transactionManager.commit(transactionStatus);
        assertTrue("Success : ", result.isSuccessFlag());
        assertEquals("Object returned on add : ", 1, result.getEntityList().size());
    }

    /**
     * Test of updatePreferences method, of class accCompanyPreferencesImpl.
     */
    public void testUpdatePreferences() throws Exception {
        System.out.println("updatePreferences");
        HashMap<String, Object> prefMap = new HashMap<String, Object>();
        SimpleDateFormat sdf=new SimpleDateFormat("MMMM d, yyyy hh:mm:ss aa");
        String tzdiff = "-07:00";
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"+tzdiff));

        prefMap.put("fyfrom", sdf.parse("Dec 31, 2010 12:00:00 AM"));
        prefMap.put("bbfrom", sdf.parse("Dec 31, 2009 12:00:00 AM"));
        prefMap.put("firstfyfrom", sdf.parse("Dec 31, 2010 12:00:00 AM"));
        prefMap.put("withoutinventory", false);
//        if(!StringUtil.isNullOrEmpty(request.getParameter("withouttax1099")))
//            prefMap.put("withouttax1099", request.getParameter("withouttax1099"));
        prefMap.put("emailinvoice", false);
        prefMap.put("companyid", companyid);

        prefMap.put("autobillingcashpurchase", "CP000000");
        prefMap.put("autobillingcashsales", "CS000000");
        prefMap.put("autobillingcreditmemo", "CN000000");
        prefMap.put("autobillingdebitnote", "DN00000011");
        prefMap.put("autobillinggoodsreceipt", "VINV000000");
        prefMap.put("autobillinginvoice", "Deskera/000000");
        prefMap.put("autobillingpayment", "MP000000");
        prefMap.put("autobillingpo", "PO000000");
        prefMap.put("autobillingreceipt", "RP000000");
        prefMap.put("autobillingso", "SO000000");
        prefMap.put("autocashpurchase", "CP000000");
        prefMap.put("autocashsales", "CS000000");
        prefMap.put("autocreditmemo", "CN000000");
        prefMap.put("autodebitnote", "DN000000");
        prefMap.put("autogoodsreceipt", "VINV000000");
        prefMap.put("autoinvoice", "KWL/00000INV0");
        prefMap.put("autojournalentry", "JE000000");
        prefMap.put("autopayment", "MP000000");
        prefMap.put("autopo", "PO/000/KWL/000");
        prefMap.put("autoreceipt", "A0B00C000");
        prefMap.put("autoso", "SO/N000000");
        prefMap.put("cashaccount", "c340667e23abce0b0123ae49d2d60122");
//        String data = "[{id:\"c340667e268ec5f401268ecbc50c0008\",name:\"2011\",islock:\"false\",modified:true},{id:\"c340667e268ec5f401268ecbc50c0009\",name:\"2012\",islock:\"false\",modified:true},{id:\"c340667e268ec5f401268ecbc50c000a\",name:\"2013\",islock:\"false\",modified:true},{id:\"c340667e268ec5f401268ecbc50c000b\",name:\"2014\",islock:\"false\",modified:true},{id:\"c340667e268ec5f401268ecbc50c000c\",name:\"2015\",islock:\"false\",modified:true},{id:\"c340667e268ec5f401268ecbc50d000d\",name:\"2009\",islock:\"true\",modified:true},{id:\"c340667e268ec5f401268ecbc50d000e\",name:\"2008\",islock:\"false\",modified:true},{id:\"c340667e268ec5f401268ecbc50d000f\",name:\"2007\",islock:\"false\",modified:true},{id:\"c340667e268ec5f401268ecbc50d0010\",name:\"2006\",islock:\"false\",modified:true},{id:\"c340667e268ec5f401268ecbc50d0011\",name:\"2005\",islock:\"false\",modified:true}]";
//        prefMap.put("data", data);
//        prefMap.put("daysid", "31");
//        prefMap.put("monthid", "December");
//        prefMap.put("yearid", "2009");
        prefMap.put("depreciationaccount", "c340667e270a192501270a7e9aab0057");
        prefMap.put("discountgiven", "c340667e27f5e71d0127f67cdad60011");
        prefMap.put("discountreceived", "c340667e27f5e71d0127f67d432e0013");
        prefMap.put("emailinvoice", false);
        prefMap.put("foreignexchange", "c340667e268ec5f401268ecafd680006");
        prefMap.put("mode", "82");
        prefMap.put("othercharges", "c340667e24fb25300124fc4744dd002c");
        prefMap.put("id", companyid);

        accCompanyPreferencesImpl instance = new accCompanyPreferencesImpl();
        instance.setSessionFactory(sessionFactory);
        KwlReturnObject result = instance.updatePreferences(prefMap);
        //commit the transaction to verify result in database.
//                transactionManager.commit(transactionStatus);
        assertTrue("Success : ", result.isSuccessFlag());
        assertEquals("Object returned on add : ", 1, result.getEntityList().size());
    }

    /**
     * Test of addYearLock method, of class accCompanyPreferencesImpl.
     */
    public void testAddYearLock() throws Exception {
        System.out.println("addYearLock");

        HashMap<String, Object> yearLockMap = new HashMap<String, Object>();
        yearLockMap.put("yearid", Integer.parseInt("2011"));
        yearLockMap.put("islock", "true".equalsIgnoreCase("false"));
        yearLockMap.put("companyid", companyid);

        accCompanyPreferencesImpl instance = new accCompanyPreferencesImpl();
        instance.setSessionFactory(sessionFactory);
        KwlReturnObject result = instance.addYearLock(yearLockMap);
        //commit the transaction to verify result in database.
//                transactionManager.commit(transactionStatus);
        assertTrue("Success : ", result.isSuccessFlag());
        assertEquals("Object returned on add : ", 1, result.getEntityList().size());
    }

    /**
     * Test of updateYearLock method, of class accCompanyPreferencesImpl.
     */
    public void testUpdateYearLock() throws Exception {
        System.out.println("updateYearLock");
        HashMap<String, Object> yearLockMap = new HashMap<String, Object>();
        yearLockMap.put("yearid", Integer.parseInt("2011"));
        yearLockMap.put("islock", "true".equalsIgnoreCase("false"));
        yearLockMap.put("companyid", companyid);
        String yearLockid = "c340667e268ec5f401268ecbc50c0008";
        yearLockMap.put("id", yearLockid);

        accCompanyPreferencesImpl instance = new accCompanyPreferencesImpl();
        instance.setSessionFactory(sessionFactory);
        KwlReturnObject result = instance.updateYearLock(yearLockMap);
        //commit the transaction to verify result in database.
//                transactionManager.commit(transactionStatus);
        assertTrue("Success : ", result.isSuccessFlag());
        assertEquals("Object returned on add : ", 1, result.getEntityList().size());
    }

    /**
     * Test of getYearLock method, of class accCompanyPreferencesImpl.
     */
//    public void testGetYearLock() throws Exception {
//        System.out.println("getYearLock");
//        HashMap<String, Object> filterParams = new HashMap<String, Object>();
//        filterParams.put("companyid", companyid);
//        accCompanyPreferencesImpl instance = new accCompanyPreferencesImpl();
//        instance.setSessionFactory(sessionFactory);
//        KwlReturnObject result = instance.getYearLock(filterParams);
//        //To Check success flag in KWLReturnObject
//        assertTrue("Success Flag false : ", result.isSuccessFlag());
//        //To Check if fetched list is not null
//        assertNotNull("Result Entrylist is NULL : ", result.getEntityList());
//
//        System.out.println("No. of records fetched : "+result.getRecordTotalCount());
//    }

//    /**
//     * Test of setAccountPreferences method, of class accCompanyPreferencesImpl.
//     */
//    public void testSetAccountPreferences() throws Exception {
//        System.out.println("setAccountPreferences");
//        String companyid = "";
//        HashMap hm = null;
//        Date curDate = null;
//        accCompanyPreferencesImpl instance = new accCompanyPreferencesImpl();
//        instance.setAccountPreferences(companyid, hm, curDate);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

}
