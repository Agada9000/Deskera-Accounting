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

package com.krawler.spring.accounting.goodsreceipt;

import com.krawler.spring.common.KwlReturnObject;
import java.util.HashMap;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.TimeZone;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.test.AbstractTransactionalDataSourceSpringContextTests;

/**
 *
 * @author krawler
 */
public class accGoodsReceiptImplTest extends AbstractTransactionalDataSourceSpringContextTests{
   private SessionFactory sessionFactory = null;
       private HibernateTemplate hibernateTemplate;

    //company id for demo
    private static String companyid = "a4792363-b0e1-4b67-992b-2851234d5ea6";
    private static String accountID = "ff8080812be6edcc012be6fcc6860002";

    public accGoodsReceiptImplTest(String testName) {
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
     * Test of getGoodsReceipts method, of class accGoodsReceiptImpl.
     */
    public void testGetGoodsReceipts() throws Exception {
        System.out.println("getGoodsReceipts");
        SimpleDateFormat sdf=new SimpleDateFormat("MMMM d, yyyy hh:mm:ss aa");
        String tzdiff = "-07:00";
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"+tzdiff));
        Map<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put("companyid", companyid);
        requestParams.put("gcurrencyid", "1");
        requestParams.put("dateformat", sdf);
        requestParams.put("start", "0");
        requestParams.put("limit", "15");
        requestParams.put("ss", null);
        requestParams.put("accid", null);
        requestParams.put("cashonly", null);
        requestParams.put("creditonly", "false");
        requestParams.put("ignorezero", null);
        requestParams.put("curdate", "Nov 20, 2010 02:49:09 PM");
        requestParams.put("persongroup", null);
        requestParams.put("isagedgraph", null);
        requestParams.put("vendorid", null);
        requestParams.put("deleted", null);
        requestParams.put("nondeleted", "true");
        requestParams.put("billid", null);
        requestParams.put("onlyamountdue",null);
        requestParams.put("only1099Vend", "true");
        requestParams.put("only1099Acc", "true");
        requestParams.put("onlyexpenseinv", "false");
        requestParams.put("for1099Report", "true");
        accGoodsReceiptImpl instance = new accGoodsReceiptImpl();
        instance.setSessionFactory(sessionFactory);
        KwlReturnObject result = instance.getGoodsReceipts(requestParams);
        assertTrue("Success flag : ", result.isSuccessFlag());

    }
}
