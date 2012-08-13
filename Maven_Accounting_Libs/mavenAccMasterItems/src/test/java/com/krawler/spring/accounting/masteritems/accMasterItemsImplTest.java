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

package com.krawler.spring.accounting.masteritems;

import com.krawler.hql.accounting.MasterGroup;
import com.krawler.hql.accounting.MasterItem;
import com.krawler.spring.common.KwlReturnObject;
import com.mchange.util.AssertException;
import java.util.ArrayList;
import java.util.HashMap;
import org.hibernate.SessionFactory;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.AbstractTransactionalDataSourceSpringContextTests;

/**
 *
 * @author sagar
 */
public class accMasterItemsImplTest extends AbstractTransactionalDataSourceSpringContextTests {
    private SessionFactory sessionFactory = null;
    //company id for demo
    private static String companyid = "a4792363-b0e1-4b67-992b-2851234d5ea6";
    private static String groupID = "7";
    private static String masterID = "ff8080812aa24c0a012aa2b4be580004";

    public accMasterItemsImplTest(String testName) {
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
     * Test of setSessionFactory method, of class accPaymentImpl.
     */
    public void testSetSessionFactory() {
        System.out.println("setSessionFactory");
        assertNotNull(sessionFactory);
    }

    /**
     * Test of addMasterGroup method, of class accMasterItemsImpl.
     */
    public void testAddMasterGroup() throws Exception {
        System.out.println("addMasterGroup");
        try{
            HashMap<String, Object> groupmap = new HashMap();
            groupmap.put("name", "test-data11");
            accMasterItemsImpl instance = new accMasterItemsImpl();
            instance.setSessionFactory(sessionFactory);
            KwlReturnObject result = instance.addMasterGroup(groupmap);
            //commit the transaction to verify result in database.
//            transactionManager.commit(transactionStatus);
//            if(result.isSuccessFlag()) {
//                groupID = ((MasterGroup)result.getEntityList().get(0)).getID();
//            }
            assertTrue("Success flag : ", result.isSuccessFlag());
            assertEquals("Object returned on add : ", 1, result.getEntityList().size());
        } catch(ConstraintViolationException e) {
            throw new AssertException("Constraint Violation Exception - Duplicate name for the group name.");
        } catch(DataIntegrityViolationException e) {
            throw new AssertException("DataIntegrityViolation Exception - Duplicate name for the group name.");
        }
    }

    /**
     * Test of updateMasterGroup method, of class accMasterItemsImpl.
     */
    public void testUpdateMasterGroup() throws Exception {
        System.out.println("updateMasterGroup");
        HashMap<String, Object> groupmap = new HashMap();
        groupmap.put("id", groupID);
        groupmap.put("name", "test-data1");
        accMasterItemsImpl instance = new accMasterItemsImpl();
        instance.setSessionFactory(sessionFactory);
        KwlReturnObject result = instance.updateMasterGroup(groupmap);
        //commit the transaction to verify result in database.
//        transactionManager.commit(transactionStatus);
        assertTrue("Success flag : ", result.isSuccessFlag());
        assertNotNull("Result Object is NULL : ", result.getEntityList().get(0));
    }

    /**
     * Test of getMasterGroups method, of class accMasterItemsImpl.
     */
    public void testGetMasterGroups() throws Exception {
        System.out.println("getMasterGroups");
        accMasterItemsImpl instance = new accMasterItemsImpl();
        instance.setSessionFactory(sessionFactory);
        KwlReturnObject result = instance.getMasterGroups();
        //To Check success flag in KWLReturnObject
        assertTrue("Success Flag false : ", result.isSuccessFlag());
        //To Check if fetched list is not null
        assertNotNull("Result Entrylist is NULL : ", result.getEntityList());

        System.out.println("No. of records fetched : "+result.getRecordTotalCount());
    }

//    /**
//     * Test of deleteMasterGroup method, of class accMasterItemsImpl.
//     */
//    public void testDeleteMasterGroup() throws Exception {
//        System.out.println("deleteMasterGroup");
//        String groupid = groupID;
//        accMasterItemsImpl instance = new accMasterItemsImpl();
//        instance.setSessionFactory(sessionFactory);
//        KwlReturnObject result = instance.deleteMasterGroup(groupid);
//        //commit the transaction to verify result in database.
////        transactionManager.commit(transactionStatus);
//        System.out.println("No. of records deleted : "+result.getRecordTotalCount());
//        assertTrue("Deletion failed ", (result.getRecordTotalCount() == 1));
//    }

//    public void testDeleteMasterGroup_withDummyID() throws Exception {
//        System.out.println("deleteMasterGroup");
//        String groupid = "abcd_xyz_dummyid";
//        accMasterItemsImpl instance = new accMasterItemsImpl();
//        instance.setSessionFactory(sessionFactory);
//        KwlReturnObject result = instance.deleteMasterGroup(groupid);
//        //commit the transaction to verify result in database.
////        transactionManager.commit(transactionStatus);
//        assertEquals("Function shows affected records with dummyid : ",0,result.getRecordTotalCount());
//    }

    /**
     * Test of addMasterItem method, of class accMasterItemsImpl.
     */
    public void testAddMasterItem() throws Exception {
        System.out.println("addMasterItem");
        HashMap itemmap = new HashMap();
        itemmap.put("name", "test-data");
        itemmap.put("groupid", groupID);
        itemmap.put("companyid", companyid);

        accMasterItemsImpl instance = new accMasterItemsImpl();
        instance.setSessionFactory(sessionFactory);
        KwlReturnObject result = instance.addMasterItem(itemmap);
        //commit the transaction to verify result in database.
//        transactionManager.commit(transactionStatus);
//        if(result.isSuccessFlag()) {
//            masterID = ((MasterItem)result.getEntityList().get(0)).getID();
//        }
        assertTrue("Success flag : ", result.isSuccessFlag());
        assertEquals("Cheque object returned on add : ", 1, result.getEntityList().size());
    }

    /**
     * Test of updateMasterItem method, of class accMasterItemsImpl.
     */
    public void testUpdateMasterItem() throws Exception {
        System.out.println("updateMasterItem");

        HashMap itemmap = new HashMap();
        itemmap.put("id", masterID);
        itemmap.put("name", "test-data1");
        itemmap.put("groupid", groupID);
        itemmap.put("companyid", companyid);

        accMasterItemsImpl instance = new accMasterItemsImpl();
        instance.setSessionFactory(sessionFactory);
        KwlReturnObject result = instance.updateMasterItem(itemmap);
        //commit the transaction to verify result in database.
//        transactionManager.commit(transactionStatus);
        assertTrue("Success flag : ", result.isSuccessFlag());
        assertNotNull("Result Object is NULL : ", result.getEntityList().get(0));
    }

    /**
     * Test of daleteMasterItem method, of class accMasterItemsImpl.
     */
    public void testDaleteMasterItem() throws Exception {
        System.out.println("daleteMasterItem");
        accMasterItemsImpl instance = new accMasterItemsImpl();
        instance.setSessionFactory(sessionFactory);
        KwlReturnObject result = instance.daleteMasterItem(masterID);
        //commit the transaction to verify result in database.
//        transactionManager.commit(transactionStatus);
        assertTrue("Deletion failed ", result.isSuccessFlag());
    }

    public void testDaleteMasterItem_withDummyId() throws Exception {
        System.out.println("daleteMasterItemDummy");
        String itemid = "abcd_xyz_dummyid";
        accMasterItemsImpl instance = new accMasterItemsImpl();
        instance.setSessionFactory(sessionFactory);
        KwlReturnObject result = instance.daleteMasterItem(itemid);
        //commit the transaction to verify result in database.
//        transactionManager.commit(transactionStatus);
        assertFalse("Deletion failed with dummyid ", result.isSuccessFlag());
    }

    /**
     * Test of getMasterItems method, of class accMasterItemsImpl.
     */
    public void testGetMasterItems() throws Exception {
        System.out.println("getMasterItems");
        HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
        ArrayList filter_names = new ArrayList(),filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
        filter_names.add("masterGroup.ID");
        filter_params.add(groupID);
        filter_names.add("company.companyID");
        filter_params.add(companyid);
        order_by.add("value");
        order_type.add("asc");
        filterRequestParams.put("filter_names", filter_names);
        filterRequestParams.put("filter_params", filter_params);
        filterRequestParams.put("order_by", order_by);
        filterRequestParams.put("order_type", order_type);

        accMasterItemsImpl instance = new accMasterItemsImpl();
        instance.setSessionFactory(sessionFactory);
        KwlReturnObject result = instance.getMasterItems(filterRequestParams);
        //To Check success flag in KWLReturnObject
        assertTrue("Success Flag false : ", result.isSuccessFlag());
        //To Check if fetched list is not null
        assertNotNull("Result Entrylist is NULL : ", result.getEntityList());

        System.out.println("No. of records fetched : "+result.getRecordTotalCount());
    }    

    /**
     * Test of copyMasterItems method, of class accMasterItemsImpl.
     */
    public void testCopyMasterItems() throws Exception {
        System.out.println("copyMasterItems");
        accMasterItemsImpl instance = new accMasterItemsImpl();
        instance.setSessionFactory(sessionFactory);
        instance.copyMasterItems(companyid);
        //commit the transaction to verify result in database.
//        transactionManager.commit(transactionStatus);
    }

}
