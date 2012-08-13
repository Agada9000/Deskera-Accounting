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

package com.krawler.spring.permissionHandler;

import com.krawler.spring.common.KwlReturnObject;
import java.util.HashMap;
import org.hibernate.SessionFactory;
import org.springframework.test.AbstractTransactionalDataSourceSpringContextTests;

/**
 *
 * @author krawler
 */
public class permissionHandlerDAOImplTest extends AbstractTransactionalDataSourceSpringContextTests {
    private SessionFactory sessionFactory = null;
    private static String companyid = "a4792363-b0e1-4b67-992b-2851234d5ea6";
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

    public permissionHandlerDAOImplTest(String testName) {
        super(testName);
    }


    /**
     * Test of setSessionFactory method, of class permissionHandlerDAOImpl.
     */
    public void testSetSessionFactory() {

        System.out.println("setSessionFactory");

        assertNotNull(sessionFactory);

        //accUomImpl instance = new accUomImpl();
        //instance.setSessionFactory(sessionFactory);
        // TODO review the generated test code and remove the default call to fail.

    }
    /**
     * Test of getFeatureList method, of class permissionHandlerDAOImpl.
     */
    public void testGetFeatureList() throws Exception {
        System.out.println("getFeatureList");
        permissionHandlerDAOImpl instance = new permissionHandlerDAOImpl();
        instance.setSessionFactory(sessionFactory);
        KwlReturnObject result = instance.getFeatureList();
        assertTrue("Success Flag false ", result.isSuccessFlag());
        // TODO review the generated test code and remove the default call to fail.
        assertNotNull("Result Entrylist is NULL", result.getEntityList());
        System.out.println("No. of records fetched : "+result.getRecordTotalCount());
    }

    /**
     * Test of getRoleList method, of class permissionHandlerDAOImpl.
     */
    public void testGetRoleList() throws Exception {
        System.out.println("getRoleList");
        String companyid = "";
        permissionHandlerDAOImpl instance = new permissionHandlerDAOImpl();
        instance.setSessionFactory(sessionFactory);
        KwlReturnObject result = instance.getRoleList(companyid);
         assertTrue("Success Flag false ", result.isSuccessFlag());

        //To Check if fetched list is not null
        assertNotNull("Result Entrylist is NULL", result.getEntityList());
        System.out.println("No. of records fetched : "+result.getRecordTotalCount());
    }
    public void testGetRoleList_withCompanyIdFilter() throws Exception {
        System.out.println("getRoleList_withCompanyIdFilter");
        permissionHandlerDAOImpl instance = new permissionHandlerDAOImpl();
        KwlReturnObject expResult = null;
        instance.setSessionFactory(sessionFactory);
        KwlReturnObject result = instance.getRoleList(companyid);
         assertTrue("Success Flag false ", result.isSuccessFlag());

        //To Check if fetched list is not null
        assertNotNull("Result Entrylist is NULL", result.getEntityList());
        System.out.println("No. of records fetched : "+result.getRecordTotalCount());
    }
    /**
     * Test of getRoleofUser method, of class permissionHandlerDAOImpl.
     */
//    public void testGetRoleofUser() throws Exception {
//        System.out.println("getRoleofUser");
//        String userid = "";
//        permissionHandlerDAOImpl instance = new permissionHandlerDAOImpl();
//        KwlReturnObject expResult = null;
//        KwlReturnObject result = instance.getRoleofUser(userid);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }


    /**
     * Test of getActivityList method, of class permissionHandlerDAOImpl.
     */
    public void testGetActivityList() throws Exception {
        System.out.println("getActivityList");
        permissionHandlerDAOImpl instance = new permissionHandlerDAOImpl();
        instance.setSessionFactory(sessionFactory);
        KwlReturnObject result = instance.getActivityList();
        assertTrue("Success Flag false ", result.isSuccessFlag());

        //To Check if fetched list is not null
        assertNotNull("Result Entrylist is NULL", result.getEntityList());
        System.out.println("No. of records fetched : "+result.getRecordTotalCount());
    }

    /**
     * Test of saveFeatureList method, of class permissionHandlerDAOImpl.
     */
//    public void testSaveFeatureList() throws Exception {
//        System.out.println("saveFeatureList");
//        HashMap<String, Object> requestParams = null;
//        permissionHandlerDAOImpl instance = new permissionHandlerDAOImpl();
//        KwlReturnObject expResult = null;
//        KwlReturnObject result = instance.saveFeatureList(requestParams);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    /**
     * Test of saveRoleList method, of class permissionHandlerDAOImpl.
     */
    public void testSaveRoleList() throws Exception {
        System.out.println("saveRoleList");
        HashMap<String, Object> requestParams = new HashMap();
        requestParams.put("userid", "99f1eb77-cac9-41bb-977b-c8bc17fb3daa");
        requestParams.put("rolename", "role1");
        requestParams.put("displayrolename", "Role1");
        requestParams.put("companyid", companyid);
        permissionHandlerDAOImpl instance = new permissionHandlerDAOImpl();
        instance.setSessionFactory(sessionFactory);
        KwlReturnObject result = instance.saveRoleList(requestParams);
        transactionManager.commit(transactionStatus);
        assertTrue(result.isSuccessFlag());
    }

    /**
     * Test of saveActivityList method, of class permissionHandlerDAOImpl.
     */
//    public void testSaveActivityList() throws Exception {
//        System.out.println("saveActivityList");
//        HashMap<String, Object> requestParams = null;
//        permissionHandlerDAOImpl instance = new permissionHandlerDAOImpl();
//        KwlReturnObject expResult = null;
//        KwlReturnObject result = instance.saveActivityList(requestParams);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    /**
     * Test of deleteFeature method, of class permissionHandlerDAOImpl.
     */
//    public void testDeleteFeature() throws Exception {
//        System.out.println("deleteFeature");
//        HashMap<String, Object> requestParams = null;
//        permissionHandlerDAOImpl instance = new permissionHandlerDAOImpl();
//        KwlReturnObject expResult = null;
//        KwlReturnObject result = instance.deleteFeature(requestParams);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    /**
     * Test of deleteRole method, of class permissionHandlerDAOImpl.
     */
//    public void testDeleteRole() throws Exception {
//        System.out.println("deleteRole");
//        HashMap<String, Object> requestParams = null;
//        permissionHandlerDAOImpl instance = new permissionHandlerDAOImpl();
//        KwlReturnObject expResult = null;
//        KwlReturnObject result = instance.deleteRole(requestParams);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    /**
     * Test of deleteActivity method, of class permissionHandlerDAOImpl.
     */
//    public void testDeleteActivity() throws Exception {
//        System.out.println("deleteActivity");
//        HashMap<String, Object> requestParams = null;
//        permissionHandlerDAOImpl instance = new permissionHandlerDAOImpl();
//        KwlReturnObject expResult = null;
//        KwlReturnObject result = instance.deleteActivity(requestParams);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    /**
     * Test of getActivityFeature method, of class permissionHandlerDAOImpl.
     */
//    public void testGetActivityFeature() throws Exception {
//        System.out.println("getActivityFeature");
//        permissionHandlerDAOImpl instance = new permissionHandlerDAOImpl();
//        KwlReturnObject expResult = null;
//        instance.setSessionFactory(sessionFactory);
//        KwlReturnObject result = instance.getActivityFeature();
//        assertTrue("Success Flag false ", result.isSuccessFlag());
//
//        //To Check if fetched list is not null
//        assertNotNull("Result Entrylist is NULL", result.getEntityList());
//        System.out.println("No. of records fetched : "+result.getRecordTotalCount());
//    }

    /**
     * Test of getUserPermission method, of class permissionHandlerDAOImpl.
     */
//    public void testGetUserPermission() throws Exception {
//        System.out.println("getUserPermission");
//        HashMap<String, Object> requestParams = null;
//        permissionHandlerDAOImpl instance = new permissionHandlerDAOImpl();
//        KwlReturnObject expResult = null;
//        KwlReturnObject result = instance.getUserPermission(requestParams);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    /**
     * Test of isSuperAdmin method, of class permissionHandlerDAOImpl.
     */
//    public void testIsSuperAdmin() throws Exception {
//        System.out.println("isSuperAdmin");
//        String userid = "";
//        String companyid = "";
//        permissionHandlerDAOImpl instance = new permissionHandlerDAOImpl();
//        boolean expResult = false;
//        boolean result = instance.isSuperAdmin(userid, companyid);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    /**
     * Test of setPermissions method, of class permissionHandlerDAOImpl.
     */
//    public void testSetPermissions() throws Exception {
//        System.out.println("setPermissions");
//        HashMap<String, Object> requestParams = null;
//        String[] features = null;
//        String[] permissions = null;
//        permissionHandlerDAOImpl instance = new permissionHandlerDAOImpl();
//        KwlReturnObject expResult = null;
//        KwlReturnObject result = instance.setPermissions(requestParams, features, permissions);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

}
