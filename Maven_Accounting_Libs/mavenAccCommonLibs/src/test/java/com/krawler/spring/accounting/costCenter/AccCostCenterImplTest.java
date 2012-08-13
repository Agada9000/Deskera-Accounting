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

package com.krawler.spring.accounting.costCenter;

import com.krawler.common.admin.CostCenter;
import com.krawler.spring.common.KwlReturnObject;
import java.util.HashMap;
import org.hibernate.SessionFactory;
import org.springframework.test.AbstractTransactionalDataSourceSpringContextTests;

/**
 *
 * @author krawler
 */
public class AccCostCenterImplTest extends AbstractTransactionalDataSourceSpringContextTests {
    private SessionFactory sessionFactory =  null;
    private AccCostCenterImpl accCostCenterImplObj = new AccCostCenterImpl();
    private static String companyid = "a4792363-b0e1-4b67-992b-2851234d5ea6"; //company id of "demo"
    private static String ccid = ""; //costcenter id for test
    
    public AccCostCenterImplTest(String testName) {
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
        accCostCenterImplObj.setSessionFactory(sessionFactory);
    }

    /**
     * Test of setSessionFactory method, of class ImportImpl.
     */
    public void testSetSessionFactory() {
        System.out.println("setSessionFactory");
        assertNotNull(sessionFactory);
    }



    /**
     * Test of saveCostCenter method, of class accCostCenterImpl.
     */
    public void testSaveCostCenter_Add() throws Exception {
        System.out.println("saveCostCenter_Add");
        HashMap<String, Object> dataMap = new HashMap<String, Object>();
        dataMap.put("Ccid", "CC-001");
        dataMap.put("Name", "Marketing");
        dataMap.put("Description", "Marketing Department");
        dataMap.put("Company", companyid);
        CostCenter ccobj = (CostCenter) accCostCenterImplObj.saveCostCenter(dataMap);
        ccid = ccobj.getID();
        transactionManager.commit(transactionStatus);
        assertEquals(ccobj.getName(), "Marketing");
    }

    /**
     * Test of getCostCenter method, of class accCostCenterImpl.
     */
    public void testGetCostCenter() throws Exception {
        System.out.println("getCostCenter");
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        KwlReturnObject result = accCostCenterImplObj.getCostCenter(requestParams);
        assertTrue(result.getRecordTotalCount()>0);
    }

        /**
     * Test of saveCostCenter method, of class accCostCenterImpl.
     */
    public void testSaveCostCenter_Update() throws Exception {
        System.out.println("saveCostCenter_Update");
        HashMap<String, Object> dataMap = new HashMap<String, Object>();
        dataMap.put("ID", ccid);
        dataMap.put("Name", "Marketing Dept.");
        CostCenter ccobj = (CostCenter) accCostCenterImplObj.saveCostCenter(dataMap);
        transactionManager.commit(transactionStatus);
        assertEquals("Marketing Dept.", ccobj.getName());
    }

    /**
     * Test of checkUniqueCostCenter method, of class accCostCenterImpl.
     */
    public void testCheckUniqueCostCenter() throws Exception {
        System.out.println("checkUniqueCostCenter");
        String id = "";
        String cccid = "CC-001";
        String name = "Marketing Dept.";
        KwlReturnObject result = accCostCenterImplObj.checkUniqueCostCenter(id, cccid, name, companyid);
        assertTrue(result.getRecordTotalCount()>0);
    }

    /**
     * Test of deleteCostCenter method, of class accCostCenterImpl.
     */
    public void testDeleteCostCenter() throws Exception {
        System.out.println("deleteCostCenter");
        KwlReturnObject result = accCostCenterImplObj.deleteCostCenter(ccid, companyid);
        transactionManager.commit(transactionStatus);
        assertEquals(true, result.isSuccessFlag());
        assertEquals(1, result.getRecordTotalCount());
    }
}
