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

import com.krawler.spring.accounting.masteritems.accMasterItemsDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import com.mchange.util.AssertException;
import org.hibernate.SessionFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.test.AbstractTransactionalDataSourceSpringContextTests;
import org.springframework.web.servlet.ModelAndView;
/**
 *
 * @author sagar
 */
public class accMasterItemsControllerTest extends AbstractTransactionalDataSourceSpringContextTests {
    private SessionFactory sessionFactory = null;
    private accMasterItemsDAO accMasterItemsDAOObj;
    //company id for demo
    private static String companyid = "a4792363-b0e1-4b67-992b-2851234d5ea6";
    private static String groupID = "2"; // Change this id from the DB
    private static String masterId_update = "c340667e2734190401273ce8a6af002e"; // Change this id from the DB
    private static String masterId_delete = "c340667e2734190401273ce8a6af002e"; // Change this id from the DB
    private static String sessionParams = "{\"tzdiff\":\"-07:00\",\"timezoneid\":\"23\",\"roleid\":\"ff8080812b99b47f012b99ba5e8d0003\"," +
            "\"callwith\":1,\"companyPreferences\":\"\",\"dateformatid\":\"1\",\"lid\":\"99f1eb77-cac9-41bb-977b-c8bc17fb3daa\",\"companyid\":\"a4792363-b0e1-4b67-992b-2851234d5ea6\"," +
            "\"username\":\"demo\",\"currencyid\":\"1\",\"company\":\"Demo\",\"success\":true,\"superuser\":\"ff8080812b99b47f012b99ba5e8d0003\",\"timeformat\":2,\"userfullname\":\"Deskera \"," +
            "\"perms\":[{\"creditterm\":3},{\"paymentmethod\":3},{\"customer\":255},{\"vendor\":255},{\"coa\":4095},{\"invoice\":4294967295},{\"fstatement\":262143},{\"audittrail\":1},{\"useradmin\":7},{\"accpref\":3},{\"groups\":3},{\"product\":4095},{\"uom\":3},{\"bankreconciliation\":1},{\"currencyexchange\":3},{\"salesbyitem\":1},{\"vendorinvoice\":4294967295},{\"qanalysis\":3},{\"fixedasset\":1023},{\"masterconfig\":15},{\"importlog\":1},{\"tax\":3}]}";

    public accMasterItemsControllerTest(String testName) {
        super(testName);
    }

    public MockHttpSession createUserMockSession(JSONObject jObj) throws ServiceException {
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

    public void setaccMasterItemsDAO(accMasterItemsDAO accMasterItemsDAOObj) {
        this.accMasterItemsDAOObj = accMasterItemsDAOObj;
    }

    /**
     * Spring will automatically inject the Hibernate session factory on startup
     * @param sessionFactory
     */
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /**
     * Test of saveMasterItem method, of class accMasterItemsController.
     */
//    public void testSaveMasterItem_add() {
//        System.out.println("saveMasterItem");
//        try {
//            MockHttpServletRequest request = new MockHttpServletRequest();
//            MockHttpSession session = createUserMockSession(new JSONObject(sessionParams));
//            request.setParameter("id", "");
//            request.setParameter("groupid", groupID);
//            request.setParameter("name", "test-data");
//
//            request.setSession(session);
//            MockHttpServletResponse response = new MockHttpServletResponse();
//            accMasterItemsController instance = new accMasterItemsController();
//            instance.setaccMasterItemsDAO(accMasterItemsDAOObj);
//            instance.setSuccessView("jsonView");
//            instance.setTxnManager((HibernateTransactionManager) transactionManager);
//            ModelAndView result = instance.saveMasterItem(request, response);
//            JSONObject job = new JSONObject(result.getModel().get("model").toString());
//            //commit the transaction to verify result in database.
////            transactionManager.commit(transactionStatus);
//            assertTrue(job.getBoolean("success"));
//        } catch (ServiceException ex) {
//            throw new AssertException("Service Exception - error creating user session");
//        } catch (JSONException ex) {
//            throw new AssertException("JSON Exception - error parsing model json");
//        }
//    }
//
//    public void testSaveMasterItem_edit() {
//        System.out.println("saveMasterItem");
//        try {
//            MockHttpServletRequest request = new MockHttpServletRequest();
//            MockHttpSession session = createUserMockSession(new JSONObject(sessionParams));
//            request.setParameter("id", masterId_update);
//            request.setParameter("groupid", groupID);
//            request.setParameter("name", "	test-data1");
//
//            request.setSession(session);
//            MockHttpServletResponse response = new MockHttpServletResponse();
//            accMasterItemsController instance = new accMasterItemsController();
//            instance.setaccMasterItemsDAO(accMasterItemsDAOObj);
//            instance.setSuccessView("jsonView");
//            instance.setTxnManager((HibernateTransactionManager) transactionManager);
//            ModelAndView result = instance.saveMasterItem(request, response);
//            JSONObject job = new JSONObject(result.getModel().get("model").toString());
//            //commit the transaction to verify result in database.
////            transactionManager.commit(transactionStatus);
//            assertTrue(job.getBoolean("success"));
//        } catch (ServiceException ex) {
//            throw new AssertException("Service Exception - error creating user session");
//        } catch (JSONException ex) {
//            throw new AssertException("JSON Exception - error parsing model json");
//        }
//    }

    /**
     * Test of deleteMasterItem method, of class accMasterItemsController.
     */
    public void testDeleteMasterItem_delete() {
        System.out.println("deleteMasterItem");
        try {
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpSession session = createUserMockSession(new JSONObject(sessionParams));
            request.setParameter("ids", masterId_delete);

            request.setSession(session);
            MockHttpServletResponse response = new MockHttpServletResponse();
            accMasterItemsController instance = new accMasterItemsController();
            instance.setaccMasterItemsDAO(accMasterItemsDAOObj);
            instance.setSuccessView("jsonView");
            instance.setTxnManager((HibernateTransactionManager) transactionManager);
            ModelAndView result = instance.deleteMasterItem(request, response);
            JSONObject job = new JSONObject(result.getModel().get("model").toString());
            //commit the transaction to verify result in database.
//            transactionManager.commit(transactionStatus);
            assertTrue(job.getBoolean("success"));
        } catch (ServiceException ex) {
            throw new AssertException("Service Exception - error creating user session");
        } catch (JSONException ex) {
            throw new AssertException("JSON Exception - error parsing model json");
        }
    }

    /**
     * Test of getMasterItems method, of class accMasterItemsController.
     */
    public void testGetMasterItems() {
        System.out.println("getMasterItems");
        try {
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpSession session = createUserMockSession(new JSONObject(sessionParams));
            request.setSession(session);
            request.setParameter("groupid", groupID);
            MockHttpServletResponse response = new MockHttpServletResponse();
            accMasterItemsController instance = new accMasterItemsController();
            instance.setaccMasterItemsDAO(accMasterItemsDAOObj);
            instance.setSuccessView("jsonView");
            ModelAndView result = instance.getMasterItems(request, response);
            JSONObject job = new JSONObject(result.getModel().get("model").toString());
            assertTrue(job.getBoolean("success"));
        } catch (ServiceException ex) {
            throw new AssertException("Service Exception - error creating user session");
        } catch (JSONException ex) {
            throw new AssertException("JSON Exception - error parsing model json");
        }
    }

//    /**
//     * Test of saveMasterGroup method, of class accMasterItemsController.
//     */
//    public void testSaveMasterGroup() {
//        System.out.println("saveMasterGroup");
//        HttpServletRequest request = null;
//        HttpServletResponse response = null;
//        accMasterItemsController instance = new accMasterItemsController();
//        ModelAndView expResult = null;
//        ModelAndView result = instance.saveMasterGroup(request, response);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    /**
     * Test of getMasterGroups method, of class accMasterItemsController.
     */
//    public void testGetMasterGroups() {
//        System.out.println("getMasterGroups");
//        try {
//            MockHttpServletRequest request = new MockHttpServletRequest();
//            MockHttpSession session = createUserMockSession(new JSONObject(sessionParams));
//            request.setSession(session);
//            MockHttpServletResponse response = new MockHttpServletResponse();
//            accMasterItemsController instance = new accMasterItemsController();
//            instance.setaccMasterItemsDAO(accMasterItemsDAOObj);
//            instance.setSuccessView("jsonView");
//            ModelAndView result = instance.getMasterGroups(request, response);
//            JSONObject job = new JSONObject(result.getModel().get("model").toString());
//            assertTrue(job.getBoolean("success"));
//        } catch (ServiceException ex) {
//            throw new AssertException("Service Exception - error creating user session");
//        } catch (JSONException ex) {
//            throw new AssertException("JSON Exception - error parsing model json");
//        }
//    }

}
