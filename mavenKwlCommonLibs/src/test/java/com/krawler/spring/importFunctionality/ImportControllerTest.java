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

package com.krawler.spring.importFunctionality;

import com.krawler.common.service.ServiceException;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import com.mchange.util.AssertException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.servlet.ModelAndView;

import org.springframework.test.AbstractTransactionalDataSourceSpringContextTests;
/**
 *
 * @author krawler
 */
public class ImportControllerTest extends AbstractTransactionalDataSourceSpringContextTests {
    private ImportDAO importDao;
    public ImportHandler importHandler;

    private static String companyid = "a4792363-b0e1-4b67-992b-2851234d5ea6"; //company id for demo
    private static String userid = "99f1eb77-cac9-41bb-977b-c8bc17fb3daa"; //user id for demo
    private static String sessionParams = "{\"tzdiff\":\"-07:00\",\"timezoneid\":\"23\",\"roleid\":\"ff8080812b99b47f012b99ba5e8d0003\"," +
            "\"callwith\":1,\"companyPreferences\":\"\",\"dateformatid\":\"1\",\"lid\":\""+userid+"\",\"companyid\":\""+companyid+"\"," +
            "\"username\":\"demo\",\"currencyid\":\"1\",\"company\":\"Demo\",\"success\":true,\"superuser\":\"ff8080812b99b47f012b99ba5e8d0003\",\"timeformat\":2,\"userfullname\":\"Deskera \"," +
            "\"perms\":[{\"creditterm\":3},{\"paymentmethod\":3},{\"customer\":255},{\"vendor\":255},{\"coa\":4095},{\"invoice\":4294967295},{\"fstatement\":262143},{\"audittrail\":1},{\"useradmin\":7},{\"accpref\":3},{\"groups\":3},{\"product\":4095},{\"uom\":3},{\"bankreconciliation\":1},{\"currencyexchange\":3},{\"salesbyitem\":1},{\"vendorinvoice\":4294967295},{\"qanalysis\":3},{\"fixedasset\":1023},{\"masterconfig\":15},{\"importlog\":1},{\"tax\":3}]}";
    private static String moduleName = "Accounts"; //Any module name from Modules table
    private static String moduleId = "e1e72896-bf85-102d-b644-001e58a64cb6"; //Module id for assigned modulename i.e. Accounts from Modules Table

    public ImportControllerTest(String testName) {
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
    public void setimportDAO(ImportDAO importDao) {
        this.importDao = importDao;
    }
    public void setimportHandler(ImportHandler importHandler) {
        this.importHandler = importHandler;
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

    /**
     * Test of setimportDAO method, of class ImportController.
     */
    public void testSetimportDAO() {
        System.out.println("setimportDAO");
        assertNotNull(importDao);
//        ImportDAO importDao = null;
//        ImportController instance = new ImportController();
//        instance.setimportDAO(importDao);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }

    /**
     * Test of setimportHandler method, of class ImportController.
     */
    public void testSetimportHandler() {
        System.out.println("setimportHandler");
        assertNotNull(importHandler);
//        ImportHandler importHandler = null;
//        ImportController instance = new ImportController();
//        instance.setimportHandler(importHandler);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }

    /**
     * Test of getColumnConfig method, of class ImportController.
     */
    public void testGetColumnConfig() {
        System.out.println("getColumnConfig");
        try{
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();

            MockHttpSession session = createUserMockSession(new JSONObject(sessionParams));
            request.setSession(session);
            request.setParameter("module","Accounts");

            ImportController instance = new ImportController();
            instance.setimportDAO(importDao);
            instance.setimportHandler(importHandler);

            ModelAndView result = instance.getColumnConfig(request, response);
            JSONObject job = new JSONObject(result.getModel().get("model").toString());
            assertTrue(job.getBoolean("success"));
        } catch (ServiceException ex) {
            throw new AssertException("Service Exception - error creating user session");
        } catch (JSONException ex) {
            throw new AssertException("JSON Exception - error parsing model json");
        }
    }

//    /**
//     * Test of importRecords method, of class ImportController.
//     */
//    public void testImportRecords() {
//        System.out.println("importRecords");
//        HttpServletRequest request = null;
//        HttpServletResponse response = null;
//        ImportController instance = new ImportController();
//        ModelAndView expResult = null;
//        ModelAndView result = instance.importRecords(request, response);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

//    /**
//     * Test of fileUploadXLS method, of class ImportController.
//     */
//    public void testFileUploadXLS() {
//        System.out.println("fileUploadXLS");
//        HttpServletRequest request = null;
//        HttpServletResponse response = null;
//        ImportController instance = new ImportController();
//        ModelAndView expResult = null;
//        ModelAndView result = instance.fileUploadXLS(request, response);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    /**
     * Test of getImportLog method, of class ImportController.
     */
    public void testGetImportLog() {
        System.out.println("getImportLog");
        try{
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();

            MockHttpSession session = createUserMockSession(new JSONObject(sessionParams));
            request.setSession(session);
            request.setParameter("start","0");
            request.setParameter("limit","15");
            request.setParameter("startdate", "2010-10-10 12:00:00");
            request.setParameter("enddate", "2010-10-11 12:00:00");

            ImportController instance = new ImportController();
            instance.setimportDAO(importDao);
            instance.setimportHandler(importHandler);

            ModelAndView result = instance.getImportLog(request, response);
            JSONObject job = new JSONObject(result.getModel().get("model").toString());
            assertTrue(job.getBoolean("success"));
        } catch (ServiceException ex) {
            throw new AssertException("Service Exception - error creating user session");
        } catch (JSONException ex) {
            throw new AssertException("JSON Exception - error parsing model json");
        }
    }

//    /**
//     * Test of downloadFileData method, of class ImportController.
//     */
//    public void testDownloadFileData() {
//        System.out.println("downloadFileData");
//        HttpServletRequest request = null;
//        HttpServletResponse response = null;
//        ImportController instance = new ImportController();
//        instance.downloadFileData(request, response);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

}
