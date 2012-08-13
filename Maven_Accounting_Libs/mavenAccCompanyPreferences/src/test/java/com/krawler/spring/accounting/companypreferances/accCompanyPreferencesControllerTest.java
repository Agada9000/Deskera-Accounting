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

package com.krawler.spring.accounting.companypreferances;

import com.krawler.common.service.ServiceException;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import com.mchange.util.AssertException;
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
 * @author sagar
 */
public class accCompanyPreferencesControllerTest extends AbstractTransactionalDataSourceSpringContextTests {
    private SessionFactory sessionFactory = null;
    private accCompanyPreferencesDAO accCompanyPreferencesDAOObj;
    private auditTrailDAO auditTrailDAOObj;
    
    //company id for demo
    private static String companyid = "a4792363-b0e1-4b67-992b-2851234d5ea6";
    private static String sessionParams = "{\"tzdiff\":\"-07:00\",\"timezoneid\":\"23\",\"roleid\":\"ff8080812b99b47f012b99ba5e8d0003\"," +
            "\"callwith\":1,\"companyPreferences\":\"\",\"dateformatid\":\"1\",\"lid\":\"99f1eb77-cac9-41bb-977b-c8bc17fb3daa\",\"companyid\":\"a4792363-b0e1-4b67-992b-2851234d5ea6\"," +
            "\"username\":\"demo\",\"currencyid\":\"1\",\"company\":\"Demo\",\"success\":true,\"superuser\":\"ff8080812b99b47f012b99ba5e8d0003\",\"timeformat\":2,\"userfullname\":\"Deskera \"," +
            "\"perms\":[{\"creditterm\":3},{\"paymentmethod\":3},{\"customer\":255},{\"vendor\":255},{\"coa\":4095},{\"invoice\":4294967295},{\"fstatement\":262143},{\"audittrail\":1},{\"useradmin\":7},{\"accpref\":3},{\"groups\":3},{\"product\":4095},{\"uom\":3},{\"bankreconciliation\":1},{\"currencyexchange\":3},{\"salesbyitem\":1},{\"vendorinvoice\":4294967295},{\"qanalysis\":3},{\"fixedasset\":1023},{\"masterconfig\":15},{\"importlog\":1},{\"tax\":3}]}";

    public accCompanyPreferencesControllerTest(String testName) {
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

    public void setaccCompanyPreferencesDAO(accCompanyPreferencesDAO accCompanyPreferencesDAOObj) {
        this.accCompanyPreferencesDAOObj = accCompanyPreferencesDAOObj;
    }

    public void setauditTrailDAO(auditTrailDAO auditTrailDAOObj) {
        this.auditTrailDAOObj = auditTrailDAOObj;
    }

    /**
     * Spring will automatically inject the Hibernate session factory on startup
     * @param sessionFactory
     */
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /**
     * Test of saveCompanyAccountPreferences method, of class accCompanyPreferencesController.
     */
    public void testSaveCompanyAccountPreferences() {
        System.out.println("saveCompanyAccountPreferences");
        try {
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpSession session = createUserMockSession(new JSONObject(sessionParams));
            request.setParameter("autobillingcashpurchase", "CP000000");
            request.setParameter("autobillingcashsales", "CS000000");
            request.setParameter("autobillingcreditmemo", "CN000000");
            request.setParameter("autobillingdebitnote", "DN00000011");
            request.setParameter("autobillinggoodsreceipt", "VINV000000");
            request.setParameter("autobillinginvoice", "Deskera/000000");
            request.setParameter("autobillingpayment", "MP000000");
            request.setParameter("autobillingpo", "PO000000");
            request.setParameter("autobillingreceipt", "RP000000");
            request.setParameter("autobillingso", "SO000000");
            request.setParameter("autocashpurchase", "CP000000");
            request.setParameter("autocashsales", "CS000000");
            request.setParameter("autocreditmemo", "CN000000");
            request.setParameter("autodebitnote", "DN000000");
            request.setParameter("autogoodsreceipt", "VINV000000");
            request.setParameter("autoinvoice", "KWL/00000INV0");
            request.setParameter("autojournalentry", "JE000000");
            request.setParameter("autopayment", "MP000000");
            request.setParameter("autopo", "PO/000/KWL/000");
            request.setParameter("autoreceipt", "A0B00C000");
            request.setParameter("autoso", "SO/N000000");
            request.setParameter("bbfrom", "Dec 31, 2009 12:00:00 AM");
            request.setParameter("cashaccount", "c340667e23abce0b0123ae49d2d60122");
            String data = "[{id:\"c340667e268ec5f401268ecbc50c0008\",name:\"2011\",islock:\"false\",modified:true},{id:\"c340667e268ec5f401268ecbc50c0009\",name:\"2012\",islock:\"false\",modified:true},{id:\"c340667e268ec5f401268ecbc50c000a\",name:\"2013\",islock:\"false\",modified:true},{id:\"c340667e268ec5f401268ecbc50c000b\",name:\"2014\",islock:\"false\",modified:true},{id:\"c340667e268ec5f401268ecbc50c000c\",name:\"2015\",islock:\"false\",modified:true},{id:\"c340667e268ec5f401268ecbc50d000d\",name:\"2009\",islock:\"true\",modified:true},{id:\"c340667e268ec5f401268ecbc50d000e\",name:\"2008\",islock:\"false\",modified:true},{id:\"c340667e268ec5f401268ecbc50d000f\",name:\"2007\",islock:\"false\",modified:true},{id:\"c340667e268ec5f401268ecbc50d0010\",name:\"2006\",islock:\"false\",modified:true},{id:\"c340667e268ec5f401268ecbc50d0011\",name:\"2005\",islock:\"false\",modified:true}]";
            request.setParameter("data", data);
            request.setParameter("daysid", "31");
            request.setParameter("depreciationaccount", "c340667e270a192501270a7e9aab0057");
            request.setParameter("discountgiven", "c340667e27f5e71d0127f67cdad60011");
            request.setParameter("discountreceived", "c340667e27f5e71d0127f67d432e0013");
            request.setParameter("emailinvoice", "false");
            request.setParameter("foreignexchange", "c340667e268ec5f401268ecafd680006");
            request.setParameter("fyfrom", "Dec 31, 2010 12:00:00 AM");
            request.setParameter("mode", "82");
            request.setParameter("monthid", "December");
            request.setParameter("monthid", "December");
            request.setParameter("othercharges", "c340667e24fb25300124fc4744dd002c");
            request.setParameter("yearid", "2009");

            request.setSession(session);
            MockHttpServletResponse response = new MockHttpServletResponse();
            accCompanyPreferencesController instance = new accCompanyPreferencesController();
            instance.setaccCompanyPreferencesDAO(accCompanyPreferencesDAOObj);
            instance.setauditTrailDAO(auditTrailDAOObj);
            instance.setSuccessView("jsonView");
            instance.setTxnManager((HibernateTransactionManager) transactionManager);
            ModelAndView result = instance.saveCompanyAccountPreferences(request, response);
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
     * Test of getCompanyAccountPreferences method, of class accCompanyPreferencesController.
     */
    public void testGetCompanyAccountPreferences() {
        System.out.println("getCompanyAccountPreferences");
        try {
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpSession session = createUserMockSession(new JSONObject(sessionParams));
            request.setSession(session);
            MockHttpServletResponse response = new MockHttpServletResponse();
            accCompanyPreferencesController instance = new accCompanyPreferencesController();
            instance.setaccCompanyPreferencesDAO(accCompanyPreferencesDAOObj);
            instance.setSuccessView("jsonView");
            ModelAndView result = instance.getCompanyAccountPreferences(request, response);
            JSONObject job = new JSONObject(result.getModel().get("model").toString());
            assertTrue(job.getBoolean("success"));
        } catch (ServiceException ex) {
            throw new AssertException("Service Exception - error creating user session");
        } catch (JSONException ex) {
            throw new AssertException("JSON Exception - error parsing model json");
        }
    }

    /**
     * Test of getNextAutoNumber method, of class accCompanyPreferencesController.
     */
//    public void testGetNextAutoNumber() {
//        System.out.println("getNextAutoNumber");
//        try {
//            MockHttpServletRequest request = new MockHttpServletRequest();
//            MockHttpSession session = createUserMockSession(new JSONObject(sessionParams));
//            request.setSession(session);
//            request.setParameter("from", "5"); //Hardcoded id for vendor purchase order.
//            MockHttpServletResponse response = new MockHttpServletResponse();
//            accCompanyPreferencesController instance = new accCompanyPreferencesController();
//            instance.setaccCompanyPreferencesDAO(accCompanyPreferencesDAOObj);
//            instance.setSuccessView("jsonView");
//            ModelAndView result = instance.getNextAutoNumber(request, response);
//            JSONObject job = new JSONObject(result.getModel().get("model").toString());
//            assertTrue(job.getBoolean("success"));
//        } catch (ServiceException ex) {
//            throw new AssertException("Service Exception - error creating user session");
//        } catch (JSONException ex) {
//            throw new AssertException("JSON Exception - error parsing model json");
//        }
//    }

    /**
     * Test of getYearLock method, of class accCompanyPreferencesController.
     */
//    public void testGetYearLock() {
//        System.out.println("getYearLock");
//        try {
//            MockHttpServletRequest request = new MockHttpServletRequest();
//            MockHttpSession session = createUserMockSession(new JSONObject(sessionParams));
//            request.setSession(session);
//            MockHttpServletResponse response = new MockHttpServletResponse();
//            accCompanyPreferencesController instance = new accCompanyPreferencesController();
//            instance.setaccCompanyPreferencesDAO(accCompanyPreferencesDAOObj);
//            instance.setSuccessView("jsonView");
//            ModelAndView result = instance.getYearLock(request, response);
//            JSONObject job = new JSONObject(result.getModel().get("model").toString());
//            assertTrue(job.getBoolean("success"));
//        } catch (ServiceException ex) {
//            throw new AssertException("Service Exception - error creating user session");
//        } catch (JSONException ex) {
//            throw new AssertException("JSON Exception - error parsing model json");
//        }
//    }

}
