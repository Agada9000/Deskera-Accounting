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

package com.krawler.spring.accounting.depreciation;

import com.krawler.common.service.ServiceException;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.journalentry.accJournalEntryDAO;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.exportFunctionality.exportMPXDAOImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import com.mchange.util.AssertException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
public class accDepreciationControllerTest extends AbstractTransactionalDataSourceSpringContextTests {
    private SessionFactory sessionFactory = null;
    //company id for demo
    private static String companyid = "a4792363-b0e1-4b67-992b-2851234d5ea6";
    private static String accountID = "c340667e294f2b8501294f44a5350002";// Change this id from the DB
    private accDepreciationDAO accDepreciationObj;
    private accAccountDAO accAccountDAOobj;
    private accJournalEntryDAO accJournalEntryobj;
    private accCompanyPreferencesDAO accCompanyPreferencesObj;
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;
    private exportMPXDAOImpl exportDaoObj;
    
    private static String sessionParams = "{\"tzdiff\":\"-07:00\",\"timezoneid\":\"23\",\"roleid\":\"ff8080812b99b47f012b99ba5e8d0003\"," +
            "\"callwith\":1,\"companyPreferences\":\"\",\"dateformatid\":\"1\",\"lid\":\"99f1eb77-cac9-41bb-977b-c8bc17fb3daa\",\"companyid\":\"a4792363-b0e1-4b67-992b-2851234d5ea6\"," +
            "\"username\":\"demo\",\"currencyid\":\"1\",\"company\":\"Demo\",\"success\":true,\"superuser\":\"ff8080812b99b47f012b99ba5e8d0003\",\"timeformat\":2,\"userfullname\":\"Deskera \"," +
            "\"perms\":[{\"creditterm\":3},{\"paymentmethod\":3},{\"customer\":255},{\"vendor\":255},{\"coa\":4095},{\"invoice\":4294967295},{\"fstatement\":262143},{\"audittrail\":1},{\"useradmin\":7},{\"accpref\":3},{\"groups\":3},{\"product\":4095},{\"uom\":3},{\"bankreconciliation\":1},{\"currencyexchange\":3},{\"salesbyitem\":1},{\"vendorinvoice\":4294967295},{\"qanalysis\":3},{\"fixedasset\":1023},{\"masterconfig\":15},{\"importlog\":1},{\"tax\":3}]}";


    public accDepreciationControllerTest(String testName) {
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

    /**
     * Spring will automatically inject the Hibernate session factory on startup
     * @param sessionFactory
     */
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /**
     * Test of setaccDepreciationDAO method, of class accDepreciationController.
     */
    public void setaccDepreciationDAO(accDepreciationDAO accDepreciationObj) {
        this.accDepreciationObj = accDepreciationObj;
    }

    /**
     * Test of setaccAccountDAO method, of class accDepreciationController.
     */
    public void setaccAccountDAO(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }

    /**
     * Test of setaccJournalEntryDAO method, of class accDepreciationController.
     */
    public void setaccJournalEntryDAO(accJournalEntryDAO accJournalEntryobj) {
        this.accJournalEntryobj = accJournalEntryobj;
    }

    /**
     * Test of setaccCompanyPreferencesDAO method, of class accDepreciationController.
     */
    public void setaccCompanyPreferencesDAO(accCompanyPreferencesDAO accCompanyPreferencesObj) {
        this.accCompanyPreferencesObj = accCompanyPreferencesObj;
    }

    /**
     * Test of setkwlCommonTablesDAO method, of class accDepreciationController.
     */
    public void setkwlCommonTablesDAO(kwlCommonTablesDAO kwlCommonTablesDAOObj1) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj1;
    }

    /**
     * Test of setexportMPXDAOImpl method, of class accDepreciationController.
     */
    public void setexportMPXDAOImpl(exportMPXDAOImpl exportDaoObj) {
        this.exportDaoObj = exportDaoObj;
    }

    /**
     * Test of saveAccountDepreciation method, of class accDepreciationController.
     */
    public void testSaveAccountDepreciation() {
        System.out.println("saveAccountDepreciation");
        try {
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpSession session = createUserMockSession(new JSONObject(sessionParams));
            request.setParameter("accountid", accountID);
            String detail = "[{accid:\"\",accname:\"\",groupid:\"\",groupname:\"\",level:\"\",leaf:\"\",openbalance:\"\",salvage:\"\",life:\"\",parentid:\"\",currencysymbol:\"Cent\",currencyname:\"US%20Dollars\",currencyid:\"1\",parentname:\"\",depreciationaccount:\"\",period:\"2\",frommonth:'Jul 19, 2010 12:00:00 AM',tomonth:'Aug 19, 2010 12:00:00 AM',firstperiodamt:\"5.416666666666667\",posted:false,perioddepreciation:\"5.416666666666667\",accdepreciation:\"10.833333333333334\",creationDate:'',netbookvalue:\"643.1666666666667\",depdetailid:\"\",deleted:\"\",isje:\"true\",categoryid:\"\",id:\"\",modified:true}]";
            request.setParameter("detail", detail);
            request.setParameter("memo", "test-data");

            request.setSession(session);
            MockHttpServletResponse response = new MockHttpServletResponse();
            accDepreciationController instance = new accDepreciationController();
            instance.setaccAccountDAO(accAccountDAOobj);
            instance.setaccCompanyPreferencesDAO(accCompanyPreferencesObj);
            instance.setSuccessView("jsonView");
            instance.setkwlCommonTablesDAO(kwlCommonTablesDAOObj);
            instance.setexportMPXDAOImpl(exportDaoObj);
            instance.setaccJournalEntryDAO(accJournalEntryobj);
            instance.setaccDepreciationDAO(accDepreciationObj);
            instance.setTxnManager((HibernateTransactionManager) transactionManager);

            ModelAndView result = instance.saveAccountDepreciation(request, response);
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
     * Test of getAccountDepreciation method, of class accDepreciationController.
     */
    public void testGetAccountDepreciation_HttpServletRequest_HttpServletResponse() {
        System.out.println("getAccountDepreciation");
        try {
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpSession session = createUserMockSession(new JSONObject(sessionParams));
            request.setSession(session);
            request.setParameter("accid", accountID);
            MockHttpServletResponse response = new MockHttpServletResponse();
            accDepreciationController instance = new accDepreciationController();
            instance.setSuccessView("jsonView");
            instance.setkwlCommonTablesDAO(kwlCommonTablesDAOObj);
            instance.setaccDepreciationDAO(accDepreciationObj);
            ModelAndView result = instance.getAccountDepreciation(request, response);
            JSONObject job = new JSONObject(result.getModel().get("model").toString());
            assertTrue(job.getBoolean("success"));
        } catch (ServiceException ex) {
            throw new AssertException("Service Exception - error creating user session");
        } catch (JSONException ex) {
            throw new AssertException("JSON Exception - error parsing model json");
        }
    }

    /**
     * Test of calMonthwiseDepreciation method, of class accDepreciationController.
     */
    public void testCalMonthwiseDepreciation() throws Exception {
        System.out.println("calMonthwiseDepreciation");
        try{
            double openingbalance = 10000.0;
            double salvage = 5000.0;
            double month = 20.0;
            accDepreciationController instance = new accDepreciationController();
            double expResult = 0.0;
            double result = instance.calMonthwiseDepreciation(openingbalance, salvage, month);
            assertNotSame(expResult, result);
        } catch (ServiceException ex) {
            throw new AssertException("Service Exception - error while calculation.");
        }
    }

    /**
     * Test of exportAccountDepreciation method, of class accDepreciationController.
     */
    public void testExportAccountDepreciation_csv() {
        System.out.println("exportAccountDepreciation");
        try {
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpSession session = createUserMockSession(new JSONObject(sessionParams));
            request.setSession(session);
            request.setParameter("accid", accountID);
            request.setParameter("align", "none,none,rowcurrency,none,rowcurrency,rowcurrency");
            request.setParameter("config", "undefined");
            request.setParameter("deleted", "false");
            request.setParameter("enddate", "");
            request.setParameter("filename", "Fixed Asset");
            request.setParameter("filetype", "csv");
            request.setParameter("header", "period,frommonth,firstperiodamt,perioddepreciation,accdepreciation,netbookvalue");
            request.setParameter("ignorecustomers", "true");
            request.setParameter("ignorevendors", "true");
            request.setParameter("name", "undefined");
            request.setParameter("stdate", "");
            request.setParameter("get", "1120");
            request.setParameter("nondeleted", "false");
            request.setParameter("title", "Period,Month,Period Depreciation,Adjustment,Accumulated Depreciation,Net Book Value");
            request.setParameter("width", "150,150,150,150,150,150");

            MockHttpServletResponse response = new MockHttpServletResponse();
            accDepreciationController instance = new accDepreciationController();
            instance.setSuccessView("jsonView");
            instance.setkwlCommonTablesDAO(kwlCommonTablesDAOObj);
            instance.setaccDepreciationDAO(accDepreciationObj);
            instance.setexportMPXDAOImpl(exportDaoObj);
            ModelAndView result = instance.exportAccountDepreciation(request, response);
            JSONObject job = new JSONObject(result.getModel().get("model").toString());
            assertTrue(job.getBoolean("success"));
        } catch (ServiceException ex) {
            throw new AssertException("Service Exception - error creating user session");
        } catch (JSONException ex) {
            throw new AssertException("JSON Exception - error parsing model json");
        }
    }

    public void testExportAccountDepreciation_pdf() {
        System.out.println("exportAccountDepreciation");
        try {
            String config =	"{\"landscape\":\"true\",\"pageBorder\":\"true\",\"gridBorder\":\"true\",\"title\":\"\",\"subtitles\":\"\",\"headNote\":\"\",\"showLogo\":\"false\",\"headDate\":\"false\",\"footDate\":\"false\",\"footPager\":\"false\",\"headPager\":\"false\",\"footNote\":\"\",\"textColor\":\"000000\",\"bgColor\":\"FFFFFF\"}";
            String gridconfig =	"{ data:[{'header':'period','title':'Period','width':'150','align':''},{'header':'frommonth','title':'Month','width':'150','align':''},{'header':'firstperiodamt','title':'Period Depreciation','width':'150','align':'rowcurrency'},{'header':'perioddepreciation','title':'Adjustment','width':'150','align':''},{'header':'accdepreciation','title':'Accumulated Depreciation','width':'150','align':'rowcurrency'},{'header':'netbookvalue','title':'Net Book Value','width':'150','align':'rowcurrency'}]}";

            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpSession session = createUserMockSession(new JSONObject(sessionParams));
            request.setSession(session);
            request.setParameter("accid", accountID);
            request.setParameter("config", config);
            request.setParameter("gridconfig", gridconfig);
            request.setParameter("enddate", "");
            request.setParameter("filename", "Fixed Asset");
            request.setParameter("filetype", "pdf");
            request.setParameter("ignorecustomers", "true");
            request.setParameter("ignorevendors", "true");
            request.setParameter("stdate", "");
            request.setParameter("get", "1120");

            MockHttpServletResponse response = new MockHttpServletResponse();
            accDepreciationController instance = new accDepreciationController();
            instance.setSuccessView("jsonView");
            instance.setkwlCommonTablesDAO(kwlCommonTablesDAOObj);
            instance.setaccDepreciationDAO(accDepreciationObj);
            instance.setexportMPXDAOImpl(exportDaoObj);
            ModelAndView result = instance.exportAccountDepreciation(request, response);
            JSONObject job = new JSONObject(result.getModel().get("model").toString());
            assertTrue(job.getBoolean("success"));
        } catch (ServiceException ex) {
            throw new AssertException("Service Exception - error creating user session");
        } catch (JSONException ex) {
            throw new AssertException("JSON Exception - error parsing model json");
        }
    }

}
