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

package com.krawler.spring.accounting.term;

import com.krawler.common.admin.Company;
import com.krawler.hql.accounting.Term;
import com.krawler.spring.common.KwlReturnObject;
import java.util.HashMap;
import org.hibernate.SessionFactory;
/*
 *AbstractTransactionalDataSourceSpringContextTests is depricated but can be used for current version
 * in case of spring 2.5.66 and above use AbstractJUnit38SpringContextTests
 * */

import org.springframework.test.AbstractTransactionalDataSourceSpringContextTests;
/**
 *
 * @author sagar
 */
public class accTermImplTest extends AbstractTransactionalDataSourceSpringContextTests {
    private SessionFactory sessionFactory = null;
    //company id for demo
    private static String companyid = "a4792363-b0e1-4b67-992b-2851234d5ea6";
    private static String termID = "";

    public accTermImplTest(String testName) {
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
     * Test of setSessionFactory method, of class accUomImpl.
     */
    public void testSetSessionFactory() {
        System.out.println("setSessionFactory");
        assertNotNull(sessionFactory);
    }


    /**
     * Test of addTerm method, of class accTermImpl.
     */
    public void testAddTerm() throws Exception {
        System.out.println("addTerm");
        HashMap<String, Object> termMap = new HashMap();
        termMap.put("termdays", 0);
        termMap.put("termname", "Testdata");
        termMap.put("companyid", companyid);
        accTermImpl instance = new accTermImpl();
        instance.setSessionFactory(sessionFactory);
        KwlReturnObject result = instance.addTerm(termMap);
        //commit the transaction to verify result in database.
        transactionManager.commit(transactionStatus);
        if(result.isSuccessFlag()) {
            termID = ((Term)result.getEntityList().get(0)).getID();
        }
        assertTrue("Success : ", result.isSuccessFlag());
        assertEquals("Term object returned on add : ", 1, result.getEntityList().size());
    }

    /**
     * Test of updateTerm method, of class accTermImpl.
     */
    public void testUpdateTerm() throws Exception {
        System.out.println("updateTerm");
        HashMap<String, Object> termMap = new HashMap();
        termMap.put("termdays", 0);
        termMap.put("termname", "Testdata1");
        termMap.put("companyid", companyid);
        termMap.put("termid", termID); //change the id with any other id from the db. Current id is of term Net 30.

        accTermImpl instance = new accTermImpl();
        instance.setSessionFactory(sessionFactory);
        KwlReturnObject result = instance.updateTerm(termMap);
        //commit the transaction to verify result in database.
//        transactionManager.commit(transactionStatus);
        assertTrue("Success : ", result.isSuccessFlag());
        assertEquals("Term object returned on edit : ", 1, result.getEntityList().size());
    }

    /**
     * Test of buildTerm method, of class accTermImpl.
     */
    public void testBuildTerm() {
        System.out.println("buildTerm");
        Term term = new Term();
        HashMap<String, Object> termMap = new HashMap();
        termMap.put("termdays", 0);
        termMap.put("termname", "testdata");
        termMap.put("companyid", companyid);

        accTermImpl instance = new accTermImpl();
        instance.setSessionFactory(sessionFactory);
        Term result = instance.buildTerm(term, termMap);
        assertEquals("testdata", result.getTermname());
        assertEquals(0, result.getTermdays());
        assertEquals(companyid, ((Company) result.getCompany()).getCompanyID());
    }

    /**
     * Test of getTerm method, of class accTermImpl.
     */
    public void testGetTerm() throws Exception {
        System.out.println("getTerm");
        HashMap<String, Object> filterParams = new HashMap();
        accTermImpl instance = new accTermImpl();
        instance.setSessionFactory(sessionFactory);
        KwlReturnObject result = instance.getTerm(filterParams);
        //To Check success flag in KWLReturnObject
        assertTrue("Success Flag false : ", result.isSuccessFlag());
        //To Check if fetched list is not null
        assertNotNull("Result Entrylist is NULL : ", result.getEntityList());

        System.out.println("No. of records fetched : "+result.getRecordTotalCount());
    }

    public void testGetTerm_withCompanyIdFilter() throws Exception {
        System.out.println("getTerm");
        HashMap<String, Object> filterParams = new HashMap();
        filterParams.put("companyid", companyid);
        accTermImpl instance = new accTermImpl();
        instance.setSessionFactory(sessionFactory);
        KwlReturnObject result = instance.getTerm(filterParams);
        //To Check success flag in KWLReturnObject
        assertTrue("Success Flag false : ", result.isSuccessFlag());
        //To Check if fetched list is not null
        assertNotNull("Result Entrylist is NULL : ", result.getEntityList());

        System.out.println("No. of records fetched : "+result.getRecordTotalCount());
    }

    /**
     * Test of deleteTerm method, of class accTermImpl.
     */
    public void testDeleteTerm() throws Exception {
        System.out.println("deleteTerm");
        String termid = termID;//"6c7299cef56e102ca0d2001cc0689651"
        accTermImpl instance = new accTermImpl();
        instance.setSessionFactory(sessionFactory);
        KwlReturnObject result = instance.deleteTerm(termid, companyid);
        //commit the transaction to verify result in database.
//        transactionManager.commit(transactionStatus);
        System.out.println("No. of records deleted : "+result.getRecordTotalCount());
        assertTrue("Deletion failed ", (result.getRecordTotalCount() == 1));
    }

    public void testDeleteTerm_withDummyTermId() throws Exception {
        System.out.println("deleteTermDummy");
        String termid = "abcd_xyz_dummyid";
        accTermImpl instance = new accTermImpl();
        instance.setSessionFactory(sessionFactory);
        KwlReturnObject result = instance.deleteTerm(termid, companyid);
        //commit the transaction to verify result in database.
//        transactionManager.commit(transactionStatus);
        assertEquals("Function shows affected records with dummyid : ",0,result.getRecordTotalCount());
    }

    /**
     * Test of copyTerms method, of class accTermImpl.
     */
    public void testCopyTerms() throws Exception {
        System.out.println("copyTerms");
        accTermImpl instance = new accTermImpl();
        instance.setSessionFactory(sessionFactory);
        instance.copyTerms(companyid);
        //commit the transaction to verify result in database.
//        transactionManager.commit(transactionStatus);
    }

}
