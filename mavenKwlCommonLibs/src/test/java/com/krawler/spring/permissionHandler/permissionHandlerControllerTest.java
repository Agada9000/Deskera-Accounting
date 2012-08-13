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

import com.krawler.common.service.ServiceException;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import junit.framework.TestCase;
import org.hibernate.SessionFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.test.AbstractTransactionalDataSourceSpringContextTests;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 * @author krawler
 */
public class permissionHandlerControllerTest extends AbstractTransactionalDataSourceSpringContextTests{
    private permissionHandlerDAO permissionHandlerDAOObj;
    private sessionHandlerImpl sessionHandlerImplObj;
    private auditTrailDAO auditTrailDAOObj;
    private HibernateTransactionManager txnManager;
    private String successView;

    public void setpermissionHandlerDAO(permissionHandlerDAO permissionHandlerDAOObj1) {
        this.permissionHandlerDAOObj = permissionHandlerDAOObj1;
    }

    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }

    public void setsessionHandlerImpl(sessionHandlerImpl sessionHandlerImplObj1) {
        this.sessionHandlerImplObj = sessionHandlerImplObj1;
    }

    public void setAuditTrailDAO(auditTrailDAO auditTrailDAOObj1) {
        this.auditTrailDAOObj = auditTrailDAOObj1;
    }
    //company id for demo
    private static String companyid = "a4792363-b0e1-4b67-992b-2851234d5ea6";
    private static String sessionParams = "{\"tzdiff\":\"-07:00\",\"timezoneid\":\"23\",\"roleid\":\"ff8080812b99b47f012b99ba5e8d0003\"," +
            "\"callwith\":1,\"companyPreferences\":\"\",\"dateformatid\":\"1\",\"lid\":\"99f1eb77-cac9-41bb-977b-c8bc17fb3daa\",\"companyid\":\"a4792363-b0e1-4b67-992b-2851234d5ea6\"," +
            "\"username\":\"demo\",\"currencyid\":\"1\",\"company\":\"Demo\",\"success\":true,\"superuser\":\"ff8080812b99b47f012b99ba5e8d0003\",\"timeformat\":2,\"userfullname\":\"Deskera \"," +
            "\"perms\":[{\"creditterm\":3},{\"paymentmethod\":3},{\"customer\":255},{\"vendor\":255},{\"coa\":4095},{\"invoice\":4294967295},{\"fstatement\":262143},{\"audittrail\":1},{\"useradmin\":7},{\"accpref\":3},{\"groups\":3},{\"product\":4095},{\"uom\":3},{\"bankreconciliation\":1},{\"currencyexchange\":3},{\"salesbyitem\":1},{\"vendorinvoice\":4294967295},{\"qanalysis\":3},{\"fixedasset\":1023},{\"masterconfig\":15},{\"importlog\":1},{\"tax\":3}]}";

    
    public permissionHandlerControllerTest(String testName) {
        super(testName);
    }
        public MockHttpSession createUserMockSession(JSONObject jObj) throws ServiceException, JSONException {
        MockHttpSession session = new MockHttpSession();
        try {
            session.setAttribute("username", jObj.getString("username"));
        	session.setAttribute("userid", jObj.getString("lid"));
        	session.setAttribute("companyid", jObj.getString("companyid"));
        	session.setAttribute("company", jObj.getString("company"));
        	session.setAttribute("timezoneid", jObj.getString("timezoneid"));
        	session.setAttribute("tzdiff", jObj.getString("tzdiff"));
        	session.setAttribute("dateformatid", jObj.getString("dateformatid"));
        	session.setAttribute("currencyid", jObj.getString("currencyid"));
        	session.setAttribute("callwith", jObj.getString("callwith"));
            session.setAttribute("timeformat", jObj.getString("timeformat"));
            session.setAttribute("companyPreferences", jObj.getString("companyPreferences"));
            session.setAttribute("roleid", jObj.getString("roleid"));
        	session.setAttribute("initialized", "true");
        	session.setAttribute("userfullname", jObj.getString("userfullname"));
			JSONArray jarr = jObj.getJSONArray("perms");
        	for (int l = 0; l < jarr.length(); l++) {
				String keyName = jarr.getJSONObject(l).names().get(0)
						.toString();
				session.setAttribute(keyName, jarr.getJSONObject(l)
						.get(keyName));
			}
            return session;
        } catch (JSONException e) {
            throw ServiceException.FAILURE("sessionHandlerImpl.createUserMockSession", e);
        }
    }
    protected String[] getConfigLocations() {
        return new String[]{"test-applicationContext.xml"};
    }

    /**
     * Test of setpermissionHandlerDAO method, of class permissionHandlerController.
     */
//    public void testSetpermissionHandlerDAO() {
//        System.out.println("setpermissionHandlerDAO");
//        permissionHandlerDAO permissionHandlerDAOObj1 = null;
//        permissionHandlerController instance = new permissionHandlerController();
//        instance.setpermissionHandlerDAO(permissionHandlerDAOObj1);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setTxnManager method, of class permissionHandlerController.
//     */
//    public void testSetTxnManager() {
//        System.out.println("setTxnManager");
//        HibernateTransactionManager txManager = null;
//        permissionHandlerController instance = new permissionHandlerController();
//        instance.setTxnManager(txManager);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setsessionHandlerImpl method, of class permissionHandlerController.
//     */
//    public void testSetsessionHandlerImpl() {
//        System.out.println("setsessionHandlerImpl");
//        sessionHandlerImpl sessionHandlerImplObj1 = null;
//        permissionHandlerController instance = new permissionHandlerController();
//        instance.setsessionHandlerImpl(sessionHandlerImplObj1);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getSuccessView method, of class permissionHandlerController.
//     */
//    public void testGetSuccessView() {
//        System.out.println("getSuccessView");
//        permissionHandlerController instance = new permissionHandlerController();
//        String expResult = "";
//        String result = instance.getSuccessView();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setSuccessView method, of class permissionHandlerController.
//     */
//    public void testSetSuccessView() {
//        System.out.println("setSuccessView");
//        String successView = "";
//        permissionHandlerController instance = new permissionHandlerController();
//        instance.setSuccessView(successView);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setAuditTrailDAO method, of class permissionHandlerController.
//     */
//    public void testSetAuditTrailDAO() {
//        System.out.println("setAuditTrailDAO");
//        auditTrailDAO auditTrailDAOObj1 = null;
//        permissionHandlerController instance = new permissionHandlerController();
//        instance.setAuditTrailDAO(auditTrailDAOObj1);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    /**
     * Test of getActivityJson method, of class permissionHandlerController.
     */
//    public void testGetActivityJson() throws ServiceException, JSONException, ServletException {
//        MockHttpServletRequest request = new MockHttpServletRequest();
//        MockHttpSession session = createUserMockSession(new JSONObject(sessionParams));
//        request.setSession(session);
//        MockHttpServletResponse response = new MockHttpServletResponse();
//        permissionHandlerController instance = new permissionHandlerController();
//        instance.setpermissionHandlerDAO(permissionHandlerDAOObj);
//        instance.setAuditTrailDAO(auditTrailDAOObj);
//        instance.setTxnManager(txnManager);
//        instance.setsessionHandlerImpl(sessionHandlerImplObj);
//        instance.setSuccessView("jsonView");
//        JSONObject expResult = null;
//        KwlReturnObject kmsg = permissionHandlerDAOObj.getActivityList();
//        JSONObject result = instance.getActivityJson(kmsg.getEntityList(), request, kmsg.getRecordTotalCount());
//         assertTrue("Success Flag false ", kmsg.getRecordTotalCount()==result.getInt("count"));
//        // TODO review the generated test code and remove the default call to fail.
//        //fail("The test case is a prototype.");
//    }

    /**
     * Test of getActivityList method, of class permissionHandlerController.
     */
//    public void testGetActivityList() throws Exception {
//        System.out.println("getActivityList");
//        MockHttpServletRequest request = new MockHttpServletRequest();
//        MockHttpSession session = createUserMockSession(new JSONObject(sessionParams));
//        request.setSession(session);
//        MockHttpServletResponse response = new MockHttpServletResponse();
//        permissionHandlerController instance = new permissionHandlerController();
//        ModelAndView expResult = null;
//        ModelAndView result = instance.getActivityList(request, response);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    /**
     * Test of getRoleJson method, of class permissionHandlerController.
     */
//    public void testGetRoleJson() {
//        System.out.println("getRoleJson");
//        List ll = null;
//        HttpServletRequest request = null;
//        int totalSize = 0;
//        permissionHandlerController instance = new permissionHandlerController();
//        JSONObject expResult = null;
//        JSONObject result = instance.getRoleJson(ll, request, totalSize);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    /**
     * Test of getRoleList method, of class permissionHandlerController.
     */
    public void testGetRoleList() throws Exception {
        System.out.println("getRoleList");


        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpSession session = createUserMockSession(new JSONObject(sessionParams));
        request.setSession(session);
        MockHttpServletResponse response = new MockHttpServletResponse();
        permissionHandlerController instance = new permissionHandlerController();
        instance.setpermissionHandlerDAO(permissionHandlerDAOObj);
        instance.setAuditTrailDAO(auditTrailDAOObj);
        instance.setTxnManager(txnManager);
        instance.setsessionHandlerImpl(sessionHandlerImplObj);
        instance.setSuccessView("jsonView");
        ModelAndView result = instance.getRoleList(request, response);
        JSONObject job = new JSONObject(result.getModel().get("model").toString()  );
        assertTrue("Success Flag false ", job.getInt("count")>0);
    }

    /**
     * Test of getFeatureJson method, of class permissionHandlerController.
     */
//    public void testGetFeatureJson() {
//        System.out.println("getFeatureJson");
//        List ll = null;
//        HttpServletRequest request = null;
//        int totalSize = 0;
//        permissionHandlerController instance = new permissionHandlerController();
//        JSONObject expResult = null;
//        JSONObject result = instance.getFeatureJson(ll, request, totalSize);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    /**
     * Test of getFeatureList method, of class permissionHandlerController.
     */
//    public void testGetFeatureList() throws Exception {
//        System.out.println("getFeatureList");
//        HttpServletRequest request = null;
//        HttpServletResponse response = null;
//        permissionHandlerController instance = new permissionHandlerController();
//        ModelAndView expResult = null;
//        ModelAndView result = instance.getFeatureList(request, response);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    /**
     * Test of getPermissions method, of class permissionHandlerController.
     */
//    public void testGetPermissions() throws Exception {
//        System.out.println("getPermissions");
//        HttpServletRequest request = null;
//        HttpServletResponse response = null;
//        permissionHandlerController instance = new permissionHandlerController();
//        ModelAndView expResult = null;
//        ModelAndView result = instance.getPermissions(request, response);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    /**
     * Test of getRolePermissionJson method, of class permissionHandlerController.
     */
//    public void testGetRolePermissionJson() {
//        System.out.println("getRolePermissionJson");
//        List ll = null;
//        HttpServletRequest request = null;
//        int totalSize = 0;
//        permissionHandlerController instance = new permissionHandlerController();
//        JSONObject expResult = null;
//        JSONObject result = instance.getRolePermissionJson(ll, request, totalSize);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    /**
     * Test of getRolePermissions method, of class permissionHandlerController.
     */
//    public void testGetRolePermissions() throws Exception {
//        System.out.println("getRolePermissions");
//        HttpServletRequest request = null;
//        HttpServletResponse response = null;
//        permissionHandlerController instance = new permissionHandlerController();
//        ModelAndView expResult = null;
//        ModelAndView result = instance.getRolePermissions(request, response);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    /**
     * Test of setPermissions method, of class permissionHandlerController.
     */
//    public void testSetPermissions() throws Exception {
//        System.out.println("setPermissions");
//        HttpServletRequest request = null;
//        HttpServletResponse response = null;
//        permissionHandlerController instance = new permissionHandlerController();
//        ModelAndView expResult = null;
//        ModelAndView result = instance.setPermissions(request, response);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    /**
     * Test of saveFeatureList method, of class permissionHandlerController.
     */
//    public void testSaveFeatureList() throws Exception {
//        System.out.println("saveFeatureList");
//        HttpServletRequest request = null;
//        HttpServletResponse response = null;
//        permissionHandlerController instance = new permissionHandlerController();
//        ModelAndView expResult = null;
//        ModelAndView result = instance.saveFeatureList(request, response);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    /**
     * Test of saveRoleList method, of class permissionHandlerController.
     */
    public void testSaveRoleList() throws Exception {
        System.out.println("saveRoleList");
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpSession session = createUserMockSession(new JSONObject(sessionParams));
        request.setParameter("displayrolename", "Role1");
        request.setParameter("mode", "9");
        request.setParameter("rolename", "role1");
        request.setParameter("userid", "9552b857-1b70-4083-8588-d603df005261");
        request.setSession(session);
        MockHttpServletResponse response = new MockHttpServletResponse();
        permissionHandlerController instance = new permissionHandlerController();
        ModelAndView expResult = null;
        instance.setpermissionHandlerDAO(permissionHandlerDAOObj);
        instance.setAuditTrailDAO(auditTrailDAOObj);
        instance.setTxnManager(txnManager);
        instance.setsessionHandlerImpl(sessionHandlerImplObj);
        instance.setSuccessView("jsonView");
        ModelAndView result = instance.saveRoleList(request, response);
        JSONObject job = new JSONObject(result.getModel().get("model").toString());
        //commit the transaction to verify result in database."msg",
        //transactionManager.commit(transactionStatus);
        assertTrue("Success Flag false ", job.getString("msg").equals("Role saved successfully"));
    }

    /**
     * Test of saveActivityList method, of class permissionHandlerController.
     */
//    public void testSaveActivityList() throws Exception {
//        System.out.println("saveActivityList");
//        HttpServletRequest request = null;
//        HttpServletResponse response = null;
//        permissionHandlerController instance = new permissionHandlerController();
//        ModelAndView expResult = null;
//        ModelAndView result = instance.saveActivityList(request, response);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    /**
     * Test of deleteFeature method, of class permissionHandlerController.
     */
//    public void testDeleteFeature() throws Exception {
//        System.out.println("deleteFeature");
//        HttpServletRequest request = null;
//        HttpServletResponse response = null;
//        permissionHandlerController instance = new permissionHandlerController();
//        ModelAndView expResult = null;
//        ModelAndView result = instance.deleteFeature(request, response);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    /**
     * Test of deleteRole method, of class permissionHandlerController.
     */
//    public void testDeleteRole() throws Exception {
//        System.out.println("deleteRole");
//        HttpServletRequest request = null;
//        HttpServletResponse response = null;
//        permissionHandlerController instance = new permissionHandlerController();
//        ModelAndView expResult = null;
//        ModelAndView result = instance.deleteRole(request, response);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    /**
     * Test of deleteActivity method, of class permissionHandlerController.
     */
//    public void testDeleteActivity() throws Exception {
//        System.out.println("deleteActivity");
//        HttpServletRequest request = null;
//        HttpServletResponse response = null;
//        permissionHandlerController instance = new permissionHandlerController();
//        ModelAndView expResult = null;
//        ModelAndView result = instance.deleteActivity(request, response);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

}
