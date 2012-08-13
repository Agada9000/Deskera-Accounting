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

package com.krawler.spring.accounting.discount;

import com.krawler.hql.accounting.Discount;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONObject;
import java.util.HashMap;
import java.util.Map;
import org.hibernate.SessionFactory;
import org.springframework.test.AbstractTransactionalDataSourceSpringContextTests;

/**
 *
 * @author sagar
 */
public class accDiscountImplTest extends AbstractTransactionalDataSourceSpringContextTests {
    private SessionFactory sessionFactory = null;
    //company id for demo
    private static String companyid = "a4792363-b0e1-4b67-992b-2851234d5ea6";
    private static String discountID = "";

    public accDiscountImplTest(String testName) {
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
     * Test of addDiscount method, of class accDiscountImpl.
     */
    public void testAddDiscount() throws Exception {
        System.out.println("addDiscount");
        String jsonStr = "{\"companyid\":\""+companyid+"\",\"originalamount\":10,\"inpercent\":true,\"discount\":5}";
        JSONObject json = new JSONObject(jsonStr);
        accDiscountImpl instance = new accDiscountImpl();
        instance.setSessionFactory(sessionFactory);
        KwlReturnObject result = instance.addDiscount(json);
        //commit the transaction to verify result in database.
        transactionManager.commit(transactionStatus);
        if(result.isSuccessFlag()) {
            discountID = ((Discount)result.getEntityList().get(0)).getID();
        }
        assertTrue("Success : ", result.isSuccessFlag());
        assertEquals("Object returned on add : ", 1, result.getEntityList().size());
    }

    /**
     * Test of updateDiscount method, of class accDiscountImpl.
     */
    public void testUpdateDiscount() throws Exception {
        System.out.println("updateDiscount");
        Map requestParam = new HashMap();
        requestParam.put("discountid", discountID);
        double discount = 2;
        double originalamount = 10;
        requestParam.put("discount", discount);
        requestParam.put("inpercent", true);
        requestParam.put("originalamount", originalamount);
        requestParam.put("companyid", companyid);
        accDiscountImpl instance = new accDiscountImpl();
        instance.setSessionFactory(sessionFactory);
        KwlReturnObject result = instance.updateDiscount(requestParam);
        //commit the transaction to verify result in database.
//        transactionManager.commit(transactionStatus);
        assertTrue("Success : ", result.isSuccessFlag());
        assertEquals(1, result.getEntityList().size());
    }

    /**
     * Test of deleteDiscount method, of class accDiscountImpl.
     */
    public void testDeleteDiscount() throws Exception {
        System.out.println("deleteDiscount");
        accDiscountImpl instance = new accDiscountImpl();
        instance.setSessionFactory(sessionFactory);
        KwlReturnObject result = instance.deleteDiscount(discountID, companyid);
        //commit the transaction to verify result in database.
//        transactionManager.commit(transactionStatus);
        System.out.println("No. of records deleted : "+result.getRecordTotalCount());
        assertTrue("Deletion failed ", (result.getRecordTotalCount() == 1));
    }

    /**
     * Test of deleteDiscountEntry method, of class accDiscountImpl.
     */
    public void testDeleteDiscountEntry() throws Exception {
        System.out.println("deleteDiscountEntry");
        accDiscountImpl instance = new accDiscountImpl();
        instance.setSessionFactory(sessionFactory);
        KwlReturnObject result = instance.deleteDiscountEntry(discountID, companyid);
        //commit the transaction to verify result in database.
//        transactionManager.commit(transactionStatus);
        System.out.println("No. of records deleted : "+result.getRecordTotalCount());
        assertTrue("Deletion failed ", (result.getRecordTotalCount() == 1));
    }
}
