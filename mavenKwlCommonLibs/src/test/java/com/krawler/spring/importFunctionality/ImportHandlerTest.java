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
import com.krawler.common.util.DataInvalidateException;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.HashMap;
import java.util.List;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.AbstractTransactionalDataSourceSpringContextTests;

/**
 *
 * @author krawler
 */
public class ImportHandlerTest extends AbstractTransactionalDataSourceSpringContextTests {
    private ImportDAO importDao = null;
    private kwlCommonTablesDAO KwlCommonTablesDAOObj = null;
    private ImportHandler ImportHandlerInstance = new ImportHandler();

    private static String companyid = "a4792363-b0e1-4b67-992b-2851234d5ea6"; //company id for demo
    private static String userid = "99f1eb77-cac9-41bb-977b-c8bc17fb3daa"; //user id for demo
    private static String sessionParams = "{\"tzdiff\":\"-07:00\",\"timezoneid\":\"23\",\"roleid\":\"ff8080812b99b47f012b99ba5e8d0003\"," +
            "\"callwith\":1,\"companyPreferences\":\"\",\"dateformatid\":\"1\",\"lid\":\""+userid+"\",\"companyid\":\""+companyid+"\"," +
            "\"username\":\"demo\",\"currencyid\":\"1\",\"company\":\"Demo\",\"success\":true,\"superuser\":\"ff8080812b99b47f012b99ba5e8d0003\",\"timeformat\":2,\"userfullname\":\"Deskera \"," +
            "\"perms\":[{\"creditterm\":3},{\"paymentmethod\":3},{\"customer\":255},{\"vendor\":255},{\"coa\":4095},{\"invoice\":4294967295},{\"fstatement\":262143},{\"audittrail\":1},{\"useradmin\":7},{\"accpref\":3},{\"groups\":3},{\"product\":4095},{\"uom\":3},{\"bankreconciliation\":1},{\"currencyexchange\":3},{\"salesbyitem\":1},{\"vendorinvoice\":4294967295},{\"qanalysis\":3},{\"fixedasset\":1023},{\"masterconfig\":15},{\"importlog\":1},{\"tax\":3}]}";
    private static String moduleName = "Accounts"; //Any module name from Modules table
    private static String moduleId = "e1e72896-bf85-102d-b644-001e58a64cb6"; //Module id for assigned modulename i.e. Accounts from Modules Table

    
    public ImportHandlerTest(String testName) {
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

    public void setKwlCommonTablesDAO(kwlCommonTablesDAO KwlCommonTablesDAOObj1) {
        this.KwlCommonTablesDAOObj = KwlCommonTablesDAOObj1;
        ImportHandlerInstance.setKwlCommonTablesDAO(KwlCommonTablesDAOObj1);
    }

    public void setimportDAO(ImportDAO importDao) {
        this.importDao = importDao;
        ImportHandlerInstance.setimportDAO(importDao);
    }


    /**
     * Test of setKwlCommonTablesDAO method, of class ImportHandler.
     */
    public void testSetKwlCommonTablesDAO() {
        System.out.println("setKwlCommonTablesDAO");
        assertNotNull(KwlCommonTablesDAOObj);
//        kwlCommonTablesDAO KwlCommonTablesDAOObj1 = null;
//        ImportHandler instance = new ImportHandler();
//        instance.setKwlCommonTablesDAO(KwlCommonTablesDAOObj1);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }

//    /**
//     * Test of setTxnManager method, of class ImportHandler.
//     */
//    public void testSetTxnManager() {
//        System.out.println("setTxnManager");
//        HibernateTransactionManager txManager = null;
//        ImportHandler instance = new ImportHandler();
//        instance.setTxnManager(txManager);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    /**
     * Test of setimportDAO method, of class ImportHandler.
     */
    public void testSetimportDAO() {
        System.out.println("setimportDAO");
        assertNotNull(importDao);
//        ImportDAO importDao = null;
//        ImportHandler instance = new ImportHandler();
//        instance.setimportDAO(importDao);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }

//    /**
//     * Test of setIsWorking method, of class ImportHandler.
//     */
//    public void testSetIsWorking() {
//        System.out.println("setIsWorking");
//        boolean isWorking = false;
//        ImportHandler instance = new ImportHandler();
//        instance.setIsWorking(isWorking);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

//    /**
//     * Test of isIsWorking method, of class ImportHandler.
//     */
//    public void testIsIsWorking() {
//        System.out.println("isIsWorking");
//        ImportHandler instance = new ImportHandler();
//        boolean expResult = false;
//        boolean result = instance.isIsWorking();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

//    /**
//     * Test of add method, of class ImportHandler.
//     */
//    public void testAdd() {
//        System.out.println("add");
//        HashMap<String, Object> requestParams = null;
//        ImportHandler instance = new ImportHandler();
//        instance.add(requestParams);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

//    /**
//     * Test of run method, of class ImportHandler.
//     */
//    public void testRun() {
//        System.out.println("run");
//        ImportHandler instance = new ImportHandler();
//        instance.run();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

//    /**
//     * Test of getMappingCSVHeader method, of class ImportHandler.
//     */
//    public void testGetMappingCSVHeader() throws Exception {
//        System.out.println("getMappingCSVHeader");
//        HttpServletRequest request = null;
//        ImportHandler instance = new ImportHandler();
//        JSONObject expResult = null;
//        JSONObject result = instance.getMappingCSVHeader(request);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    /**
     * Test of cleanHTML method, of class ImportHandler.
     */
    public void testCleanHTML() throws Exception {
        System.out.println("cleanHTML");
        String strText = "<b>Sandeep</b>";
        String expResult = "Sandeep";
        String result = ImportHandlerInstance.cleanHTML(strText);
        assertEquals(expResult, result);
    }

//    /**
//     * Test of uploadDocument method, of class ImportHandler.
//     */
//    public void testUploadDocument() throws Exception {
//        System.out.println("uploadDocument");
//        HttpServletRequest request = null;
//        String fileid = "";
//        ImportHandler instance = new ImportHandler();
//        String expResult = "";
//        String result = instance.uploadDocument(request, fileid);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of importCSVFile method, of class ImportHandler.
//     */
//    public void testImportCSVFile() throws Exception {
//        System.out.println("importCSVFile");
//        HashMap<String, Object> requestParams = null;
//        JSONObject extraParams = null;
//        Object extraObj = null;
//        ImportHandler instance = new ImportHandler();
//        JSONObject expResult = null;
//        JSONObject result = instance.importCSVFile(requestParams, extraParams, extraObj);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of doXLSImport method, of class ImportHandler.
//     */
//    public void testDoXLSImport() throws Exception {
//        System.out.println("doXLSImport");
//        HashMap<String, Object> requestParams = null;
//        JSONObject extraParams = null;
//        Object extraObj = null;
//        ImportHandler instance = new ImportHandler();
//        JSONObject expResult = null;
//        JSONObject result = instance.doXLSImport(requestParams, extraParams, extraObj);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of validateDataMAP method, of class ImportHandler.
//     */
//    public void testValidateDataMAP() throws Exception {
//        System.out.println("validateDataMAP");
//        HashMap<String, Object> requestParams = new HashMap<String, Object>();
//        HashMap<String, Object> dataMap = new HashMap<String, Object>();
//        JSONArray columnConfigArray = null;
//        JSONArray customfield = null;
//        HashMap<String, Object> columnHeaderMap = null;
//        ImportHandler instance = new ImportHandler();
//        instance.validateDataMAP(requestParams, dataMap, columnConfigArray, customfield, columnHeaderMap);
//
//    }
//
//    /**
//     * Test of createFailureFiles method, of class ImportHandler.
//     */
//    public void testCreateFailureFiles() {
//        System.out.println("createFailureFiles");
//        String filename = "";
//        StringBuilder failedRecords = null;
//        String ext = "";
//        ImportHandler instance = new ImportHandler();
//        instance.createFailureFiles(filename, failedRecords, ext);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    /**
     * Test of getModuleColumnConfig method, of class ImportHandler.
     */
    public void testGetModuleColumnConfig() throws Exception {
        System.out.println("getModuleColumnConfig");
        JSONArray result = ImportHandlerInstance.getModuleColumnConfig(moduleId, companyid);
        assertNotNull(result);
    }
//
//    /**
//     * Test of CheckUniqueRecord method, of class ImportHandler.
//     */
//    public void testCheckUniqueRecord() throws Exception {
//        System.out.println("CheckUniqueRecord");
//        HashMap<String, Object> requestParams = null;
//        HashMap<String, Object> dataMap = null;
//        String classPath = "";
//        String uniqueKeyMethodName = "";
//        String uniqueKeyHbmName = "";
//        ImportHandler instance = new ImportHandler();
//        instance.CheckUniqueRecord(requestParams, dataMap, classPath, uniqueKeyMethodName, uniqueKeyHbmName);
//    }

    /**
     * Test of validateColumnData method, of class ImportHandler.
     */

    public void testValidateColumnData_NotNull() throws Exception {
        System.out.println("validateColumnData_NotNull");
        String errorMsg = "";
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put("masterPreference", "0");
        HashMap<String, Object> dataMap = new HashMap<String, Object>();
        HashMap<String, Object> columnHeaderMap = new HashMap<String, Object>();
        columnHeaderMap.put("Email", "Email");

        JSONObject columnConfig = new JSONObject("{\"id\":\"036f5f20-d0a1-102d-af63-001e58a64cb6\",\"isNotNull\":true,\"maxLength\":50,\"configid\":\"\",\"isMandatory\":true,\"columnName\":\"Email Address\",\"customflag\":false,\"pojoName\":\"Email\",\"validatetype\":\"email\"}");
        String column = "Email";
        JSONArray customfield = new JSONArray();
        String dateFormat = "yyyy-MM-dd";

        dataMap.put("Email", "");
        boolean issuccess = true;
        try{
            ImportHandlerInstance.validateColumnData(requestParams, dataMap, columnConfig, column, customfield, columnHeaderMap, dateFormat);
        } catch(DataInvalidateException de){
            issuccess = false;//isNotNull is true so it will throw exception
            errorMsg= de.getMessage();
        }
        assertFalse(errorMsg, issuccess);
    }

    public void testValidateColumnData_MaxLength() throws Exception {
        System.out.println("validateColumnData_MaxLength");
        String errorMsg = "";
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put("masterPreference", "0");
        HashMap<String, Object> dataMap = new HashMap<String, Object>();
        HashMap<String, Object> columnHeaderMap = new HashMap<String, Object>();
        columnHeaderMap.put("Email", "Email");

        JSONObject columnConfig = new JSONObject("{\"id\":\"036f5f20-d0a1-102d-af63-001e58a64cb6\",\"isNotNull\":true,\"maxLength\":20,\"configid\":\"\",\"isMandatory\":true,\"columnName\":\"Email Address\",\"customflag\":false,\"pojoName\":\"Email\",\"validatetype\":\"email\"}");
        String column = "Email";
        JSONArray customfield = new JSONArray();
        String dateFormat = "yyyy-MM-dd";

        dataMap.put("Email", "sandeep.kamble56789@deskera.com");
        boolean issuccess = true;
        try{
            ImportHandlerInstance.validateColumnData(requestParams, dataMap, columnConfig, column, customfield, columnHeaderMap, dateFormat);
        } catch(DataInvalidateException de){
            issuccess = false;
            errorMsg= de.getMessage();
        }
        assertFalse(errorMsg, issuccess);
    }

    public void testValidateColumnData_Email() throws Exception {
        String errorMsg = "";
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put("masterPreference", "0");
        HashMap<String, Object> dataMap = new HashMap<String, Object>();
        HashMap<String, Object> columnHeaderMap = new HashMap<String, Object>();
        columnHeaderMap.put("Email", "Email");

        JSONObject columnConfig = new JSONObject("{\"id\":\"036f5f20-d0a1-102d-af63-001e58a64cb6\",\"isNotNull\":true,\"maxLength\":50,\"configid\":\"\",\"isMandatory\":true,\"columnName\":\"Email Address\",\"customflag\":false,\"pojoName\":\"Email\",\"validatetype\":\"email\"}");
        String column = "Email";
        JSONArray customfield = new JSONArray();
        String dateFormat = "yyyy-MM-dd";


        //Test for valid data
        System.out.println("validateColumnData: Email (Valid Data)");
        dataMap.put("Email", "sandeep.kamble@deskera.com");
        boolean issuccess = true;
        try{
            ImportHandlerInstance.validateColumnData(requestParams, dataMap, columnConfig, column, customfield, columnHeaderMap, dateFormat);
        } catch(DataInvalidateException de){
            issuccess = false;
            errorMsg= de.getMessage();
        }
        assertTrue(errorMsg, issuccess);

        //Test for invalid data
        System.out.println("validateColumnData: Email (InValid Data)");
        dataMap.put("Email", "sandeep.kamble.invalid.com");
        issuccess = true;
        try{
            ImportHandlerInstance.validateColumnData(requestParams, dataMap, columnConfig, column, customfield, columnHeaderMap, dateFormat);
        } catch(DataInvalidateException de){
            issuccess = false;
            errorMsg= de.getMessage();
        }
        assertFalse(errorMsg, issuccess);
    }

    public void testValidateColumnData_Date() throws Exception {
        String errorMsg = "";
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put("masterPreference", "0");
        HashMap<String, Object> dataMap = new HashMap<String, Object>();
        HashMap<String, Object> columnHeaderMap = new HashMap<String, Object>();
        columnHeaderMap.put("Date", "Date");

        JSONObject columnConfig = new JSONObject("{\"id\":\"036f5f20-d0a1-102d-af63-001e58a64cb6\",\"isNotNull\":true,\"maxLength\":50,\"configid\":\"\",\"isMandatory\":true,\"columnName\":\"Creation Date\",\"customflag\":false,\"pojoName\":\"CreationDate\",\"validatetype\":\"date\"}");
        String column = "Date";
        JSONArray customfield = new JSONArray();
        String dateFormat = "yyyy-MM-dd";


        //Test for valid data
        System.out.println("validateColumnData: Date (Valid Data)");
        dataMap.put("Date", "2010-10-20");
        boolean issuccess = true;
        try{
            ImportHandlerInstance.validateColumnData(requestParams, dataMap, columnConfig, column, customfield, columnHeaderMap, dateFormat);
        } catch(DataInvalidateException de){
            issuccess = false;
            errorMsg= de.getMessage();
        }
        assertTrue(errorMsg, issuccess);

        //Test for invalid data
        System.out.println("validateColumnData: Date (InValid Data)");
        dataMap.put("Date", "20/10/2010");
        issuccess = true;
        try{
            ImportHandlerInstance.validateColumnData(requestParams, dataMap, columnConfig, column, customfield, columnHeaderMap, dateFormat);
        } catch(DataInvalidateException de){
            issuccess = false;
            errorMsg= de.getMessage();
        }
        assertFalse(errorMsg, issuccess);
    }

    public void testValidateColumnData_Integer() throws Exception {
        String errorMsg = "";
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put("masterPreference", "0");
        HashMap<String, Object> dataMap = new HashMap<String, Object>();
        HashMap<String, Object> columnHeaderMap = new HashMap<String, Object>();
        columnHeaderMap.put("Integer", "Integer");

        JSONObject columnConfig = new JSONObject("{\"id\":\"036f5f20-d0a1-102d-af63-001e58a64cb6\",\"isNotNull\":true,\"maxLength\":50,\"configid\":\"\",\"isMandatory\":true,\"columnName\":\"Email Address\",\"customflag\":false,\"pojoName\":\"Email\",\"validatetype\":\"integer\"}");
        String column = "Integer";
        JSONArray customfield = new JSONArray();
        String dateFormat = "yyyy-MM-dd";


        //Test for valid data
        System.out.println("validateColumnData: Integer (Valid Data)");
        dataMap.put("Integer", "132");
        boolean issuccess = true;
        try{
            ImportHandlerInstance.validateColumnData(requestParams, dataMap, columnConfig, column, customfield, columnHeaderMap, dateFormat);
        } catch(DataInvalidateException de){
            issuccess = false;
            errorMsg= de.getMessage();
        }
        assertTrue(errorMsg, issuccess);

        //Test for invalid data
        System.out.println("validateColumnData: Integer (InValid Data)");
        dataMap.put("Integer", "sandeep");
        issuccess = true;
        try{
            ImportHandlerInstance.validateColumnData(requestParams, dataMap, columnConfig, column, customfield, columnHeaderMap, dateFormat);
        } catch(DataInvalidateException de){
            issuccess = false;
            errorMsg= de.getMessage();
        }
        assertFalse(errorMsg, issuccess);
    }

    public void testValidateColumnData_Double() throws Exception {
        String errorMsg = "";
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put("masterPreference", "0");
        HashMap<String, Object> dataMap = new HashMap<String, Object>();
        HashMap<String, Object> columnHeaderMap = new HashMap<String, Object>();
        columnHeaderMap.put("Double", "Double");

        JSONObject columnConfig = new JSONObject("{\"id\":\"036f5f20-d0a1-102d-af63-001e58a64cb6\",\"isNotNull\":true,\"maxLength\":50,\"configid\":\"\",\"isMandatory\":true,\"columnName\":\"Email Address\",\"customflag\":false,\"pojoName\":\"Email\",\"validatetype\":\"double\"}");
        String column = "Double";
        JSONArray customfield = new JSONArray();
        String dateFormat = "yyyy-MM-dd";


        //Test for valid data
        System.out.println("validateColumnData: Double (Valid Data)");
        dataMap.put("Double", "154.345");
        boolean issuccess = true;
        try{
            ImportHandlerInstance.validateColumnData(requestParams, dataMap, columnConfig, column, customfield, columnHeaderMap, dateFormat);
        } catch(DataInvalidateException de){
            issuccess = false;
            errorMsg= de.getMessage();
        }
        assertTrue(errorMsg, issuccess);

        //Test for invalid data
        System.out.println("validateColumnData: Double (InValid Data)");
        dataMap.put("Double", "sandeep.kamble.invalid.com");
        issuccess = true;
        try{
            ImportHandlerInstance.validateColumnData(requestParams, dataMap, columnConfig, column, customfield, columnHeaderMap, dateFormat);
        } catch(DataInvalidateException de){
            issuccess = false;
            errorMsg= de.getMessage();
        }
        assertFalse(errorMsg, issuccess);
    }

    public void testValidateColumnData_Boolean() throws Exception {
        String errorMsg = "";
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put("masterPreference", "0");
        HashMap<String, Object> dataMap = new HashMap<String, Object>();
        HashMap<String, Object> columnHeaderMap = new HashMap<String, Object>();
        columnHeaderMap.put("Boolean", "Boolean");

        JSONObject columnConfig = new JSONObject("{\"id\":\"036f5f20-d0a1-102d-af63-001e58a64cb6\",\"isNotNull\":true,\"maxLength\":50,\"configid\":\"\",\"isMandatory\":true,\"columnName\":\"Email Address\",\"customflag\":false,\"pojoName\":\"Email\",\"validatetype\":\"boolean\"}");
        String column = "Boolean";
        JSONArray customfield = new JSONArray();
        String dateFormat = "yyyy-MM-dd";


        //Test for valid data
        System.out.println("validateColumnData: Boolean (Valid Data): true");
        dataMap.put("Boolean", "true");
        boolean issuccess = true;
        try{
            ImportHandlerInstance.validateColumnData(requestParams, dataMap, columnConfig, column, customfield, columnHeaderMap, dateFormat);
        } catch(DataInvalidateException de){
            issuccess = false;
            errorMsg= de.getMessage();
        }
        assertTrue(errorMsg, issuccess);

        System.out.println("validateColumnData: Boolean (Valid Data): 1");
        dataMap.put("Boolean", "1");
        issuccess = true;
        try{
            ImportHandlerInstance.validateColumnData(requestParams, dataMap, columnConfig, column, customfield, columnHeaderMap, dateFormat);
        } catch(DataInvalidateException de){
            issuccess = false;
            errorMsg= de.getMessage();
        }
        assertTrue(errorMsg, issuccess);

        System.out.println("validateColumnData: Boolean (Valid Data): T");
        dataMap.put("Boolean", "T");
        issuccess = true;
        try{
            ImportHandlerInstance.validateColumnData(requestParams, dataMap, columnConfig, column, customfield, columnHeaderMap, dateFormat);
        } catch(DataInvalidateException de){
            issuccess = false;
            errorMsg= de.getMessage();
        }
        assertTrue(errorMsg, issuccess);

        //Test for invalid data
        System.out.println("validateColumnData: Boolean (InValid Data)");
        dataMap.put("Boolean", "sandeep");
        issuccess = true;
        try{
            ImportHandlerInstance.validateColumnData(requestParams, dataMap, columnConfig, column, customfield, columnHeaderMap, dateFormat);
        } catch(DataInvalidateException de){
            issuccess = false;
            errorMsg= de.getMessage();
        }
        assertFalse(errorMsg, issuccess);
    }
    /**
     * Test of getDefaultValue method, of class ImportHandler.
     */
    public void testGetDefaultValue() throws Exception {
        System.out.println("getDefaultValue");
        JSONObject columnConfig = new JSONObject("{\"id\":\"036f5f20-d0a1-102d-af63-001e58a64cb6\",\"isNotNull\":true,\"maxLength\":50,\"configid\":\"\",\"isMandatory\":true,\"columnName\":\"Email Address\",\"customflag\":false,\"pojoName\":\"Email\",\"validatetype\":\"integer\",\"defaultValue\":\"111\"}");
        Object expResult = 111;
        Object result = ImportHandlerInstance.getDefaultValue(columnConfig);
        assertEquals(expResult, result);
    }

    /**
     * Test of getRefData method, of class ImportHandler.
     */
    public void testGetRefData() throws Exception {
        System.out.println("getRefData");
        HashMap<String, Object> requestParams = null;
        String table = "Modules";
        String dataColumn = "modulename";
        String fetchColumn = "id";
        String comboConfigid = "";
        Object token = moduleName;

        List result = ImportHandlerInstance.getRefData(requestParams, table, dataColumn, fetchColumn, comboConfigid, token);
        assertNotNull(result);
        assertTrue(result.size()>0);
        assertEquals(moduleId, result.get(0).toString());
    }

//    /**
//     * Test of getCustomComboID method, of class ImportHandler.
//     */
//    public void testGetCustomComboID() throws Exception {
//        System.out.println("getCustomComboID");
//        String combovalue = "";
//        int fieldid = 0;
//        String fetchColumn = "";
//        ImportHandler instance = new ImportHandler();
//        List expResult = null;
//        List result = instance.getCustomComboID(combovalue, fieldid, fetchColumn);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    /**
     * Test of chkNullorEmptywithDatatruncation method, of class ImportHandler.
     */
    public void testChkNullorEmptywithDatatruncation() {
        System.out.println("chkNullorEmptywithDatatruncation");
        String cc = "chkNullorEmptywithDatatruncation";
        int dataTruncation = 4;
        String result = ImportHandlerInstance.chkNullorEmptywithDatatruncation(cc, dataTruncation);
        assertEquals(result.length(), dataTruncation);
    }

//    /**
//     * Test of parseXLS method, of class ImportHandler.
//     */
//    public void testParseXLS() throws Exception {
//        System.out.println("parseXLS");
//        String filename = "";
//        int sheetNo = 0;
//        ImportHandler instance = new ImportHandler();
//        JSONObject expResult = null;
//        JSONObject result = instance.parseXLS(filename, sheetNo);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of parseXLS1 method, of class ImportHandler.
//     */
//    public void testParseXLS1() throws Exception {
//        System.out.println("parseXLS1");
//        String filename = "";
//        int sheetNo = 0;
//        int startindex = 0;
//        ImportHandler instance = new ImportHandler();
//        JSONObject expResult = null;
//        JSONObject result = instance.parseXLS1(filename, sheetNo, startindex);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of validateFileData method, of class ImportHandler.
//     */
//    public void testValidateFileData() {
//        System.out.println("validateFileData");
//        HashMap<String, Object> requestParams = null;
//        ImportHandler instance = new ImportHandler();
//        JSONObject expResult = null;
//        JSONObject result = instance.validateFileData(requestParams);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of importFileData method, of class ImportHandler.
//     */
//    public void testImportFileData() {
//        System.out.println("importFileData");
//        HashMap<String, Object> requestParams = null;
//        ImportHandler instance = new ImportHandler();
//        JSONObject expResult = null;
//        JSONObject result = instance.importFileData(requestParams);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of validateDataMAP2 method, of class ImportHandler.
//     */
//    public void testValidateDataMAP2() throws Exception {
//        System.out.println("validateDataMAP2");
//        HashMap<String, Object> requestParams = null;
//        HashMap<String, Object> dataMap = null;
//        JSONArray columnConfigArray = null;
//        JSONArray customfield = null;
//        HashMap<String, Object> columnHeaderMap = null;
//        HashMap<String, Object> columnCSVindexMap = null;
//        String dateFormat = "";
//        ImportHandler instance = new ImportHandler();
//        instance.validateDataMAP2(requestParams, dataMap, columnConfigArray, customfield, columnHeaderMap, columnCSVindexMap, dateFormat);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of dumpXLSFileData method, of class ImportHandler.
//     */
//    public void testDumpXLSFileData() throws Exception {
//        System.out.println("dumpXLSFileData");
//        String filename = "";
//        int sheetNo = 0;
//        int startindex = 0;
//        ImportHandler instance = new ImportHandler();
//        int expResult = 0;
//        int result = instance.dumpXLSFileData(filename, sheetNo, startindex);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    /**
     * Test of dumpCSVFileData method, of class ImportHandler.
     */
//    public void testDumpCSVFileData() throws Exception {
//        System.out.println("dumpCSVFileData");
//        String filename = "";
//        String delimiterType = "";
//        int startindex = 0;
//        ImportHandler instance = new ImportHandler();
//        int expResult = 0;
//        int result = instance.dumpCSVFileData(filename, delimiterType, startindex);
//        assertEquals(expResult, result);
//    }

    /**
     * Test of createCSVrecord method, of class ImportHandler.
     */
    public void testCreateCSVrecord() {
        System.out.println("createCSVrecord");
        Object[] listArray = {"id","col1_data","col2_data","col3_data","col4_data","col",1,"Validation Log"};

        String result = ImportHandlerInstance.createCSVrecord(listArray);
        String expResult = "\"col1_data\",\"col2_data\",\"col3_data\",\"col4_data\",";
        assertTrue(result.length()>0);
        assertEquals(result, expResult);
    }

    /**
     * Test of addPendingImportLog method, of class ImportHandler.
     */
    public void testAddPendingImportLog() {
        System.out.println("addPendingImportLog");
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put("filename", "JUNIT_TEST.xls");
        requestParams.put("modName", moduleName);
        requestParams.put("companyid", companyid);
        requestParams.put("userid", userid);

        String result = ImportHandlerInstance.addPendingImportLog(requestParams);
        assertNotNull(result);
    }

    /**
     * Test of getImportRequestParams method, of class ImportHandler.
     */
    public void testGetImportRequestParams() throws Exception {
        System.out.println("getImportRequestParams");
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpSession session = createUserMockSession(new JSONObject(sessionParams));
        request.setSession(session);
        request.setParameter("filename", "JUNIT_TEST.xls");

        HashMap<String, Object> result = ImportHandlerInstance.getImportRequestParams(request);
        assertEquals(result.get("companyid"), companyid);
        assertEquals(result.get("filename"), "JUNIT_TEST.xls");
    }

}
