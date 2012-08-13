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
package com.krawler.spring.accounting.uom;

import com.krawler.common.admin.Company;
import com.krawler.hql.accounting.UnitOfMeasure;
import com.krawler.spring.common.KwlReturnObject;
import java.util.HashMap;
import org.hibernate.SessionFactory;
/*
 *AbstractTransactionalDataSourceSpringContextTests is depricated but can be used for current version
 * in case of spring 2.5.66 and above use AbstractJUnit38SpringContextTests
 * */

import org.springframework.test.AbstractTransactionalDataSourceSpringContextTests;


/**
 *
 * @author krawler
 */
public class accUomImplTest extends AbstractTransactionalDataSourceSpringContextTests {

    private SessionFactory sessionFactory = null;
    //company id for demo
    private static String companyid = "a4792363-b0e1-4b67-992b-2851234d5ea6";

    public accUomImplTest(String testName) {
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

        //accUomImpl instance = new accUomImpl();
        //instance.setSessionFactory(sessionFactory);
        // TODO review the generated test code and remove the default call to fail.

    }

    /**
     * Test of getUnitOfMeasure method, of class accUomImpl.
     */
    public void testGetUnitOfMeasure() throws Exception {
        System.out.println("getUnitOfMeasure");

        HashMap<String, Object> filterParams = new HashMap();
        accUomImpl instance = new accUomImpl();
        //This is very important in all functions where session factory is required
        instance.setSessionFactory(sessionFactory);
        KwlReturnObject result = instance.getUnitOfMeasure(filterParams);
        //To Check success flag in KWLReturnObject
        assertTrue("Success Flag false ", result.isSuccessFlag());

        //To Check if fetched list is not null
        assertNotNull("Result Entrylist is NULL", result.getEntityList());
        System.out.println("No. of records fetched : "+result.getRecordTotalCount());

    }

      /**
     * Test of getUnitOfMeasure method, with companyid filter.
     */
    public void testGetUnitOfMeasure_withCompanyIdFilter() throws Exception {
        System.out.println("getUnitOfMeasure");

        HashMap<String, Object> filterParams = new HashMap();
        filterParams.put("companyid", companyid);
        accUomImpl instance = new accUomImpl();
        //This is very important in all functions where session factory is required
        instance.setSessionFactory(sessionFactory);
        KwlReturnObject result = instance.getUnitOfMeasure(filterParams);
        //To Check success flag in KWLReturnObject
        assertTrue("Success Flag false ", result.isSuccessFlag());

        //To Check if fetched list is not null
        assertNotNull("Result Entrylist is NULL", result.getEntityList());
        System.out.println(result.getRecordTotalCount());
    }

    /**
     * Test of addUoM method, of class accUomImpl.
     */
    public void testAddUoM() throws Exception {
        System.out.println("addUoM");
        HashMap<String, Object> uomMap = new HashMap();
        uomMap.put("uomname", "testdata");
        uomMap.put("uomtype", "LT");
        uomMap.put("precision", 0);
        uomMap.put("companyid", companyid);
        accUomImpl instance = new accUomImpl();
        instance.setSessionFactory(sessionFactory);
        //commit the transaction to verify result in database.
        //transactionManager.commit(transactionStatus);
        KwlReturnObject result = instance.addUoM(uomMap);
        assertTrue(result.isSuccessFlag());
        assertEquals(1, result.getEntityList().size());
    }

    /**
     * Test of deleteUoM method, of class accUomImpl.
     */
    public void testDeleteUoM() throws Exception {

        System.out.println("deleteUoM");
        String uomid = "402880842b808f53012b808f5fd20002"; // replace the id with any of the existing id
        accUomImpl instance = new accUomImpl();
        //This is very important in all functions where session factory is required
        instance.setSessionFactory(sessionFactory);
        KwlReturnObject result = instance.deleteUoM(uomid, companyid);

        assertTrue("Deletion failed ", (result.getRecordTotalCount() == 1));



    }
    /**
     * Test of deleteUoM method, with dummy UOM id to ensure records with only existing id's are deleted
     */
    public void testDeleteUoM_withDummyUOMId() throws Exception {

        System.out.println("deleteUoM");
        String uomid = "abcd_xyz_dummyid"; // replace the id with any of the existing id
        accUomImpl instance = new accUomImpl();
        //This is very important in all functions where session factory is required
        instance.setSessionFactory(sessionFactory);
        KwlReturnObject result = instance.deleteUoM(uomid, companyid);

        assertEquals("Function shows affected records with dummyid",0,result.getRecordTotalCount());



    }
    /**
     * Test of updateUoM method, of class accUomImpl.
     */
    public void testUpdateUoM() throws Exception {
        System.out.println("updateUoM");
        HashMap<String, Object> uomMap = new HashMap();
        uomMap.put("uomid", "402880842b808f53012b808f5fd20002"); //change the id with any other id from the db
        uomMap.put("uomname", "testdata1");
        uomMap.put("uomtype", "LT");
        uomMap.put("precision", 1);
        uomMap.put("companyid", companyid);
        accUomImpl instance = new accUomImpl();
        instance.setSessionFactory(sessionFactory);
        KwlReturnObject result = instance.updateUoM(uomMap);
        transactionManager.commit(transactionStatus);
        assertTrue(result.isSuccessFlag());
    }

    /**
     * Test of buildUoM method, of class accUomImpl.
     */
    public void testBuildUoM() throws Exception {
        System.out.println("buildUoM");
        UnitOfMeasure uom = new UnitOfMeasure();
        HashMap<String, Object> uomMap = new HashMap();
        accUomImpl instance = new accUomImpl();
        uomMap.put("uomname", "testdata");
        uomMap.put("uomtype", "LT");
        uomMap.put("precision", 0);
        uomMap.put("companyid", companyid);
        instance.setSessionFactory(sessionFactory);
        UnitOfMeasure result = instance.buildUoM(uom, uomMap);
        assertEquals("testdata", result.getName());
        assertEquals("LT", result.getType());
        assertEquals(0, result.getAllowedPrecision());
        assertEquals(companyid, ((Company) result.getCompany()).getCompanyID());


    }

    /**
     * Test of copyUOM method, of class accUomImpl.
     */
    public void testCopyUOM() throws Exception {
        System.out.println("copyUOM");

        accUomImpl instance = new accUomImpl();
        instance.setSessionFactory(sessionFactory);
        instance.copyUOM(companyid);

    }
}
