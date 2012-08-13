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
import com.krawler.spring.common.KwlReturnObject;
import java.util.HashMap;
import org.hibernate.SessionFactory;
import java.util.ArrayList;
import java.util.HashMap;
import org.hibernate.SessionFactory;
import org.springframework.test.AbstractTransactionalDataSourceSpringContextTests;

/**
 *
 * @author krawler
 */
public class accTaxImplTest extends AbstractTransactionalDataSourceSpringContextTests {
     private SessionFactory sessionFactory = null;
    //company id for demo
    private static String companyid = "a4792363-b0e1-4b67-992b-2851234d5ea6";
    private static String accountID = "ff8080812be6edcc012be6fcc6860002";
    public accTaxImplTest(String testName) {
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
     * Test of getTax1099Category method, of class accTaxImpl.
     */
    public void testGetTax1099Category() throws Exception {
        System.out.println("getTax1099Category");
        HashMap<String, Object> requestParams = new HashMap();
        accTaxImpl instance = new accTaxImpl();
        instance.setSessionFactory(sessionFactory);
        KwlReturnObject result = instance.getTax1099Category(requestParams);
        assertTrue(result.isSuccessFlag());
    }

    /**
     * Test of getTax1099AccCategory method, of class accTaxImpl.
     */
    public void testGetTax1099AccCategory() throws Exception {
        System.out.println("getTax1099AccCategory");
        HashMap<String, Object> requestParams =  new HashMap();
        requestParams.put("companyid", companyid);
        requestParams.put("accountid", "c357c1042c5de6ab012c5e264da50002");
        accTaxImpl instance = new accTaxImpl();
        instance.setSessionFactory(sessionFactory);
        KwlReturnObject result = instance.getTax1099AccCategory(requestParams);
        assertTrue(result.isSuccessFlag());
    }

    /**
     * Test of belongsTo1099 method, of class accTaxImpl.
     */
    public void testBelongsTo1099() throws Exception {
        System.out.println("belongsTo1099");
        ArrayList accIDArr = new ArrayList();
        accIDArr.add(0, "c357c1042c5de6ab012c5e264da50002");
        accTaxImpl instance = new accTaxImpl();
        instance.setSessionFactory(sessionFactory);
        KwlReturnObject result = instance.belongsTo1099(companyid, accIDArr);
        assertTrue(result.isSuccessFlag());
    }
        public void testCopyTax1099Category() throws Exception {

        System.out.println("copyTax1099Category");
        accTaxImpl instance = new accTaxImpl();
        
        instance.setSessionFactory(sessionFactory);
        instance.copyTax1099Category(companyid);
        transactionManager.commit(transactionStatus);
        assertTrue(true);


    }
}
