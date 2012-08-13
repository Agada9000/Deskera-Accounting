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

package com.krawler.spring.accounting.currency;

import com.krawler.common.service.ServiceException;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import com.krawler.utils.json.base.JSONArray;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
public class accCurrencyControllerTest extends AbstractTransactionalDataSourceSpringContextTests{
     private HibernateTransactionManager txnManager;
    private accCurrencyDAO accCurrencyDAOobj;
    private String successView;

    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }
    public void setaccCurrencyDAO(accCurrencyDAO accCurrencyDAOobj) {
        this.accCurrencyDAOobj = accCurrencyDAOobj;
    }
    public void setSuccessView(String successView) {
        this.successView = successView;
    }

        //company id for demo
    private static String companyid = "a4792363-b0e1-4b67-992b-2851234d5ea6";
    private static String sessionParams = "{\"tzdiff\":\"-07:00\",\"timezoneid\":\"23\",\"roleid\":\"ff8080812b99b47f012b99ba5e8d0003\"," +
            "\"callwith\":1,\"companyPreferences\":\"\",\"dateformatid\":\"1\",\"lid\":\"99f1eb77-cac9-41bb-977b-c8bc17fb3daa\",\"companyid\":\"a4792363-b0e1-4b67-992b-2851234d5ea6\"," +
            "\"username\":\"demo\",\"currencyid\":\"1\",\"company\":\"Demo\",\"success\":true,\"superuser\":\"ff8080812b99b47f012b99ba5e8d0003\",\"timeformat\":2,\"userfullname\":\"Deskera \"," +
            "\"perms\":[{\"creditterm\":3},{\"paymentmethod\":3},{\"customer\":255},{\"vendor\":255},{\"coa\":4095},{\"invoice\":4294967295},{\"fstatement\":262143},{\"audittrail\":1},{\"useradmin\":7},{\"accpref\":3},{\"groups\":3},{\"product\":4095},{\"uom\":3},{\"bankreconciliation\":1},{\"currencyexchange\":3},{\"salesbyitem\":1},{\"vendorinvoice\":4294967295},{\"qanalysis\":3},{\"fixedasset\":1023},{\"masterconfig\":15},{\"importlog\":1},{\"tax\":3}]}";



    public accCurrencyControllerTest(String testName) {
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
     * Test of saveCurrencyExchange method, of class accCurrencyController.
     */
//    public void testSaveCurrencyExchange_HttpServletRequest_HttpServletResponse() {
//        System.out.println("saveCurrencyExchange");
//        HttpServletRequest request = null;
//        HttpServletResponse response = null;
//        accCurrencyController instance = new accCurrencyController();
//        ModelAndView expResult = null;
//        ModelAndView result = instance.saveCurrencyExchange(request, response);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of saveCurrencyExchange method, of class accCurrencyController.
//     */
//    public void testSaveCurrencyExchange_HttpServletRequest() throws Exception {
//        System.out.println("saveCurrencyExchange");
//        HttpServletRequest request = null;
//        accCurrencyController instance = new accCurrencyController();
//        boolean expResult = false;
//        boolean result = instance.saveCurrencyExchange(request);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    /**
     * Test of getCurrencyExchange method, of class accCurrencyController.
     */
    public void testGetCurrencyExchange() throws ServiceException, JSONException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpSession session = createUserMockSession(new JSONObject(sessionParams));
        request.setParameter("transactiondate", "Oct 22, 2010 11:25:09 AM");
        request.setParameter("fromcurrencyid", "1");//base currency
        request.setParameter("tocurrencyid", "5");
        request.setParameter("companyid", companyid);
        request.setSession(session);
        MockHttpServletResponse response = new MockHttpServletResponse();
        accCurrencyController instance = new accCurrencyController();
        instance.setaccCurrencyDAO(accCurrencyDAOobj);
        instance.setTxnManager(txnManager);
        ModelAndView result = instance.getCurrencyExchange(request, response);
        JSONObject job = new JSONObject(result.getModel().get("model").toString()  );
        assertTrue("Success Flag false ", job.getInt("count")>0);
    }

     public void testGetCurrencyExchange_withoutToCurrency() throws ServiceException, JSONException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpSession session = createUserMockSession(new JSONObject(sessionParams));
        request.setParameter("transactiondate", "Oct 22, 2010 11:25:09 AM");
        request.setParameter("fromcurrencyid", "1");//base currency
        request.setParameter("companyid", companyid);
        request.setSession(session);
        MockHttpServletResponse response = new MockHttpServletResponse();
        accCurrencyController instance = new accCurrencyController();
        instance.setaccCurrencyDAO(accCurrencyDAOobj);
        instance.setTxnManager(txnManager);
        ModelAndView result = instance.getCurrencyExchange(request, response);
        JSONObject job = new JSONObject(result.getModel().get("model").toString()  );
        assertTrue("Success Flag false ", job.getInt("count")>0);
    }

//    /**
//     * Test of getCurrencyExchangeJson method, of class accCurrencyController.
//     */
//    public void testGetCurrencyExchangeJson() throws Exception {
//        System.out.println("getCurrencyExchangeJson");
//        HttpServletRequest request = null;
//        List list = null;
//        accCurrencyController instance = new accCurrencyController();
//        JSONArray expResult = null;
//        JSONArray result = instance.getCurrencyExchangeJson(request, list);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getCurrencyExchangeList method, of class accCurrencyController.
//     */
//    public void testGetCurrencyExchangeList() {
//        System.out.println("getCurrencyExchangeList");
//        HttpServletRequest request = null;
//        HttpServletResponse response = null;
//        accCurrencyController instance = new accCurrencyController();
//        ModelAndView expResult = null;
//        ModelAndView result = instance.getCurrencyExchangeList(request, response);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getCurrencyExchangeListJson method, of class accCurrencyController.
//     */
//    public void testGetCurrencyExchangeListJson() throws Exception {
//        System.out.println("getCurrencyExchangeListJson");
//        HttpServletRequest request = null;
//        List list = null;
//        accCurrencyController instance = new accCurrencyController();
//        JSONArray expResult = null;
//        JSONArray result = instance.getCurrencyExchangeListJson(request, list);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getDefaultCurrencyExchange method, of class accCurrencyController.
//     */
//    public void testGetDefaultCurrencyExchange() {
//        System.out.println("getDefaultCurrencyExchange");
//        HttpServletRequest request = null;
//        HttpServletResponse response = null;
//        accCurrencyController instance = new accCurrencyController();
//        ModelAndView expResult = null;
//        ModelAndView result = instance.getDefaultCurrencyExchange(request, response);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getDefaultCurrencyExchangeJson method, of class accCurrencyController.
//     */
//    public void testGetDefaultCurrencyExchangeJson() throws Exception {
//        System.out.println("getDefaultCurrencyExchangeJson");
//        HttpServletRequest request = null;
//        List list = null;
//        accCurrencyController instance = new accCurrencyController();
//        JSONArray expResult = null;
//        JSONArray result = instance.getDefaultCurrencyExchangeJson(request, list);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

}
