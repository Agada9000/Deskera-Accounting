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
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONArray;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hibernate.SessionFactory;
import org.springframework.test.AbstractTransactionalDataSourceSpringContextTests;
/**
 *
 * @author krawler
 */
public class ImportImplTest extends AbstractTransactionalDataSourceSpringContextTests {
    private SessionFactory sessionFactory =  null;
    private ImportImpl ImportImplInstance = new ImportImpl();
    
    private static String companyid = "a4792363-b0e1-4b67-992b-2851234d5ea6"; //company id for demo
    private static String userid = "99f1eb77-cac9-41bb-977b-c8bc17fb3daa"; //user id for demo
    private static String tablename = "IL_TEST_TABLE_NAME";
    private static String moduleName = "Accounts"; //Any module name from Modules table
    private static String moduleId = "e1e72896-bf85-102d-b644-001e58a64cb6"; //Module id for assigned modulename i.e. Accounts from Modules Table
    
    public ImportImplTest(String testName) {
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
        ImportImplInstance.setSessionFactory(sessionFactory);
    }

    /**
     * Test of setSessionFactory method, of class ImportImpl.
     */
    public void testSetSessionFactory() {
        System.out.println("setSessionFactory");
        assertNotNull(sessionFactory);
    }

    /**
     * Test of saveRecord method, of class ImportImpl.
     */
    public void testSaveRecord() throws Exception {
        System.out.println("saveRecord");
        //Tested by saving entry in Import Log Table

        HashMap<String, Object> requestParams = null;
        HashMap<String, Object> logDataMap = new HashMap<String, Object>();
        logDataMap.put("FileName", "JUNIT_TEST_FILE_ENTRY.xls");
        logDataMap.put("StorageName", "JUNIT_TEST_FILE_ENTRY.xls");
        logDataMap.put("Log", "Junit Test Result");
        logDataMap.put("Type", ".xls");
        logDataMap.put("TotalRecs", 10);
        logDataMap.put("Rejected", 0);
        logDataMap.put("Module", moduleId);
        logDataMap.put("ImportDate", new Date());
        logDataMap.put("User", userid);
        logDataMap.put("Company", companyid);
        
        Object csvReader = null;
        String modeName = "JUNIT_TEST";
        String classPath = "com.krawler.common.admin.ImportLog";
        String primaryKey = "Id";
        Object extraObj = null;
        JSONArray customfield = null;

        Object result = ImportImplInstance.saveRecord(requestParams, logDataMap, csvReader, modeName, classPath, primaryKey, extraObj, customfield);
        assertNotNull(result);
    }

    /**
     * Test of saveImportLog method, of class ImportImpl.
     */
    public void testSaveImportLog() {
        try {
            System.out.println("saveImportLog");
            HashMap<String, Object> logDataMap = new HashMap<String, Object>();
            logDataMap.put("FileName", "JUNIT_TEST_FILE_ENTRY.xls");
            logDataMap.put("StorageName", "JUNIT_TEST_FILE_ENTRY.xls");
            logDataMap.put("Log", "Junit Test Result");
            logDataMap.put("Type", ".xls");
            logDataMap.put("TotalRecs", 10);
            logDataMap.put("Rejected", 0);
            logDataMap.put("Module", moduleId);
            logDataMap.put("ImportDate", new Date());
            logDataMap.put("User", userid);
            logDataMap.put("Company", companyid);
            Object result = ImportImplInstance.saveImportLog(logDataMap);
            assertNotNull(result);
        } catch (ServiceException ex) {
            Logger.getLogger(ImportImplTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Test of getImportLog method, of class ImportImpl.
     */
    public void testGetImportLog() throws Exception {
        System.out.println("getImportLog");
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put("startdate", new Date());
        requestParams.put("enddate", new Date());
        requestParams.put("companyid", companyid);
        requestParams.put("start", 0);
        requestParams.put("limit", 15);

        KwlReturnObject result = ImportImplInstance.getImportLog(requestParams);
        assertNotNull(result);
    }

    /**
     * Test of getRefModuleData method, of class ImportImpl.
     */
    public void testGetRefModuleData() throws Exception {
        System.out.println("getRefModuleData");
        HashMap<String, Object> requestParams = null;
        String module = "Modules";
        String fetchColumn = "id";
        String comboConfigid = "";
        ArrayList<String> filterNames = new ArrayList<String>();
        ArrayList<Object> filterValues = new ArrayList<Object>();
        filterNames.add("modulename");
        filterValues.add(moduleName);

        List result = ImportImplInstance.getRefModuleData(requestParams, module, fetchColumn, comboConfigid, filterNames, filterValues);
        assertNotNull(result);
        assertTrue(result.size()>0);
        assertEquals(moduleId, result.get(0).toString());
    }

    /**
     * Test of getCustomComboID method, of class ImportImpl.
     */
//    public void testGetCustomComboID() throws Exception {
//        System.out.println("getCustomComboID");
//        String fetchColumn = "";
//        ArrayList filterNames = null;
//        ArrayList filterValues = null;
//        ImportImpl instance = new ImportImpl();
//        List expResult = null;
//        List result = instance.getCustomComboID(fetchColumn, filterNames, filterValues);
//        assertEquals(expResult, result);
//    }

    /**
     * Test of getModuleColumnConfig method, of class ImportImpl.
     */
    public void testGetModuleColumnConfig() throws Exception {
        System.out.println("getModuleColumnConfig");
        List result = ImportImplInstance.getModuleColumnConfig(moduleId, companyid);
        assertNotNull(result);
        assertTrue(result.size()>=1);
    }

    /**
     * Test of getModuleObject method, of class ImportImpl.
     */
    public void testGetModuleObject() throws Exception {
        System.out.println("getModuleObject");
        List result = ImportImplInstance.getModuleObject(moduleName);
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    /**
     * Test of getTableName method, of class ImportImpl.
     */
    public void testGetTableName() {
        System.out.println("getTableName");
        String fileName = "QB-Customer500_c38f8307fdde4d118cab13c413b52578.xls";
        ImportImpl instance = new ImportImpl();
        String expResult = "IL_QB-Customer500_c38f8307fdde4d118cab13c413b52578";
        String result = instance.getTableName(fileName);
        assertEquals(expResult, result);
    }

    /**
     * Test of createFileTable method, of class ImportImpl.
     */
    public void testCreateFileTable() throws Exception {
        System.out.println("createFileTable");
        int cols = 4;
        int expResult = 0;
        int result = ImportImplInstance.createFileTable(tablename, cols);
        assertEquals(expResult, result);
    }

    /**
     * Test of dumpFileRow method, of class ImportImpl.
     */
    public void testDumpFileRow() throws Exception {
        System.out.println("dumpFileRow");
        Object[] dataArray = {"col1_data", "col2_data", "col3_data", "col4_data"};
        int expResult = 1;
        int result = ImportImplInstance.dumpFileRow(tablename, dataArray);
        transactionManager.commit(transactionStatus);
        assertEquals(expResult, result);
    }

    /**
     * Test of makeUploadedFileEntry method, of class ImportImpl.
     */
    public void testMakeUploadedFileEntry() throws Exception {
        System.out.println("makeUploadedFileEntry");
//        String filename = "TEST_UPLOAD_c38f8307fdde4d118cab13c413b52578.xls";
//        String onlyfilename = "TEST_UPLOAD.xls";
//        String tablename = "IL_TEST_UPLOAD_c38f8307fdde4d118cab13c413b52578";
//        int expResult = 1;
//        int result = ImportImplInstance.makeUploadedFileEntry(filename, onlyfilename, tablename, companyid);
//        assertEquals(expResult, result);
    }

    /**
     * Test of markRecordValidation method, of class ImportImpl.
     */
    public void testMarkRecordValidation() throws Exception {
        System.out.println("markRecordValidation- single record");
        int id = 1;
        int isvalid = 0;
        String validateLog = "Test for markRecordInvalid";
        String invalidColumns = "";
        int expResult = 1;
        int result = ImportImplInstance.markRecordValidation(tablename, id, isvalid, validateLog, invalidColumns);
        transactionManager.commit(transactionStatus);
        assertEquals(expResult, result);
    }

    public void testMarkRecordValidation_AllRecords() throws Exception {
        System.out.println("markRecordValidation- all records");
        int id = -1;
        int isvalid = 0;
        String validateLog = "";
        String invalidColumns = "";
        int result = ImportImplInstance.markRecordValidation(tablename, id, isvalid, validateLog, invalidColumns);
        transactionManager.commit(transactionStatus);
        assertTrue(result>=1);
    }

    /**
     * Test of getFileData method, of class ImportImpl.
     */
    public void testGetFileData() throws Exception {
        System.out.println("getFileData");
        HashMap<String, Object> filterParams = new HashMap<String, Object>();
        KwlReturnObject result = ImportImplInstance.getFileData(tablename, filterParams);
        assertTrue(result.getRecordTotalCount()>=1);
        assertNotNull(result.getEntityList());
    }

    public void testGetFileData_WithFilter() throws Exception {
        System.out.println("getFileData");
        HashMap<String, Object> filterParams = new HashMap<String, Object>();
        filterParams.put("isvalid", false);
        KwlReturnObject result = ImportImplInstance.getFileData(tablename, filterParams);
        assertTrue(result.getRecordTotalCount()>=1);
        assertNotNull(result.getEntityList());
    }

}
