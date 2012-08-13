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

package com.krawler.spring.accounting.payment;

import com.krawler.common.admin.Company;
import com.krawler.hql.accounting.Account;
import com.krawler.hql.accounting.Card;
import com.krawler.hql.accounting.Cheque;
import com.krawler.hql.accounting.PayDetail;
import com.krawler.hql.accounting.PaymentMethod;
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
public class accPaymentImplTest extends AbstractTransactionalDataSourceSpringContextTests {
    private SessionFactory sessionFactory = null;
    //company id for demo
    private static String companyid = "a4792363-b0e1-4b67-992b-2851234d5ea6";
    private static String payDetailIDCash = "";
    private static String payDetailIDCheque = "";
    private static String payDetailIDCard = "";

    private static String payMethodCashID = "";
    private static String payMethodCardID = "";
    private static String payMethodBankID = "";
    private static String checkID = "";
    private static String cardID = "";

    private static String bankAccountID = "c340667e24dc5b090124dd8acd31002c";
    public accPaymentImplTest(String testName) {
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
     * Test of setSessionFactory method, of class accPaymentImpl.
     */
    public void testSetSessionFactory() {
        System.out.println("setSessionFactory");
        assertNotNull(sessionFactory);
    }
    

    /**
     * Test of addCheque method, of class accPaymentImpl.
     */
    public void testAddCheque() throws Exception {
        System.out.println("addCheque");
        HashMap hm = new HashMap();
        hm.put("chequeno", "test-CH0002");
        hm.put("description", "testdescription");
        hm.put("bankname", "testbank");

        accPaymentImpl instance = new accPaymentImpl();
        instance.setSessionFactory(sessionFactory);
        KwlReturnObject result = instance.addCheque(hm);
        //commit the transaction to verify result in database.
        transactionManager.commit(transactionStatus);
        if(result.isSuccessFlag()) {
            checkID = ((Cheque)result.getEntityList().get(0)).getID();
        }
        assertTrue("Success flag : ", result.isSuccessFlag());
        assertEquals("Cheque object returned on add : ", 1, result.getEntityList().size());
    }

    /**
     * Test of updateCheque method, of class accPaymentImpl.
     */
    public void testUpdateCheque() throws Exception {
        System.out.println("updateCheque");
        HashMap hm = new HashMap();
        hm.put("chequeid", checkID);
        hm.put("chequeno", "test-CH00011");
        hm.put("description", "testdescription1");
        hm.put("bankname", "testbank1");

        accPaymentImpl instance = new accPaymentImpl();
        instance.setSessionFactory(sessionFactory);
        KwlReturnObject result = instance.updateCheque(hm);
        //commit the transaction to verify result in database.
        transactionManager.commit(transactionStatus);
        assertTrue("Success flag : ", result.isSuccessFlag());
        assertNotNull("Result Cheque is NULL : ", result.getEntityList().get(0));
    }

    /**
     * Test of deleteCheque method, of class accPaymentImpl.
     */
    public void testDeleteCheque() throws Exception {
        System.out.println("deleteCheque");
        accPaymentImpl instance = new accPaymentImpl();
        instance.setSessionFactory(sessionFactory);
        KwlReturnObject result = instance.deleteCheque(checkID, companyid);
        //commit the transaction to verify result in database.
//        transactionManager.commit(transactionStatus);
        System.out.println("No. of records deleted : "+result.getRecordTotalCount());
        assertTrue("Deletion failed ", (result.getRecordTotalCount() == 1));
    }

    public void testDeleteCheque_withDummyChequeId() throws Exception {
        System.out.println("deleteChequeDummy");
        String chequeid = "abcd_xyz_dummyid";
        accPaymentImpl instance = new accPaymentImpl();
        instance.setSessionFactory(sessionFactory);
        KwlReturnObject result = instance.deleteCheque(chequeid, companyid);
        //commit the transaction to verify result in database.
//        transactionManager.commit(transactionStatus);
        assertEquals("Function shows affected records with dummyid : ",0,result.getRecordTotalCount());
    }

    /**
     * Test of addCard method, of class accPaymentImpl.
     */
    public void testAddCard() throws Exception {
        System.out.println("addCard");
        HashMap hm = new HashMap();
        hm.put("cardno", "test-CR0001");
        hm.put("nameoncard", "test-name");
        hm.put("expirydate", "2020-10-10");
        hm.put("cardtype", "test-CardType");
        hm.put("refno", "test-REF0001");

        accPaymentImpl instance = new accPaymentImpl();
        instance.setSessionFactory(sessionFactory);
        KwlReturnObject result = instance.addCard(hm);
        //commit the transaction to verify result in database.
        transactionManager.commit(transactionStatus);
        if(result.isSuccessFlag()) {
            cardID = ((Card)result.getEntityList().get(0)).getID();
        }
        assertTrue("Success flag : ", result.isSuccessFlag());
        assertEquals("Object returned on add : ", 1, result.getEntityList().size());
    }

    /**
     * Test of updateCard method, of class accPaymentImpl.
     */
    public void testUpdateCard() throws Exception {
        System.out.println("updateCard");
        HashMap hm = new HashMap();
        hm.put("cardid", cardID);
        hm.put("cardno", "test-CR00011");
        hm.put("nameoncard", "test-name1");
        hm.put("expirydate", "2020-10-10");
        hm.put("cardtype", "test-CardType1");
        hm.put("refno", "test-REF00011");
        accPaymentImpl instance = new accPaymentImpl();
        instance.setSessionFactory(sessionFactory);
        KwlReturnObject result = instance.updateCard(hm);
        //commit the transaction to verify result in database.
        transactionManager.commit(transactionStatus);
        assertTrue("Success flag : ", result.isSuccessFlag());
        assertNotNull("Result Object is NULL : ", result.getEntityList().get(0));
    }

    /**
     * Test of deleteCard method, of class accPaymentImpl.
     */
    public void testDeleteCard() throws Exception {
        System.out.println("deleteCard");
        accPaymentImpl instance = new accPaymentImpl();
        instance.setSessionFactory(sessionFactory);
        KwlReturnObject result = instance.deleteCard(cardID, companyid);
        //commit the transaction to verify result in database.
//        transactionManager.commit(transactionStatus);
        System.out.println("No. of records deleted : "+result.getRecordTotalCount());
        assertTrue("Deletion failed ", (result.getRecordTotalCount() == 1));
    }

    public void testDeleteCard_withDummyCardId() throws Exception {
        System.out.println("deleteCardDummy");
        String cardid = "abcd_xyz_dummyid";
        accPaymentImpl instance = new accPaymentImpl();
        instance.setSessionFactory(sessionFactory);
        KwlReturnObject result = instance.deleteCard(cardid, companyid);
        //commit the transaction to verify result in database.
//        transactionManager.commit(transactionStatus);
        assertEquals("Function shows affected records with dummyid : ",0,result.getRecordTotalCount());
    }

    /**
     * Test of addPaymentMethod method, of class accPaymentImpl.
     */
    public void testAddPaymentMethod() throws Exception {
        System.out.println("addPaymentMethod");
        //Cash Type
        HashMap hm = new HashMap();
        hm.put("methodname", "test-cash");
        hm.put("detailtype", 0);//change type with any other types from the db.
        hm.put("accountid", bankAccountID);
        hm.put("companyid", companyid);

        accPaymentImpl instance = new accPaymentImpl();
        instance.setSessionFactory(sessionFactory);
        KwlReturnObject result = instance.addPaymentMethod(hm);
        if(result.isSuccessFlag()) {
            payMethodCashID = ((PaymentMethod)result.getEntityList().get(0)).getID();
        }
        assertTrue("Success flag : ", result.isSuccessFlag());
        assertEquals("Object returned on add : ", 1, result.getEntityList().size());

        //Card Type
        hm = new HashMap();
        hm.put("methodname", "test-card");
        hm.put("detailtype", 1);//change type with any other types from the db.
        hm.put("accountid", bankAccountID);
        hm.put("companyid", companyid);

        instance = new accPaymentImpl();
        instance.setSessionFactory(sessionFactory);
        result = instance.addPaymentMethod(hm);
        if(result.isSuccessFlag()) {
            payMethodCardID = ((PaymentMethod)result.getEntityList().get(0)).getID();
        }
        assertTrue("Success flag : ", result.isSuccessFlag());
        assertEquals("Object returned on add : ", 1, result.getEntityList().size());

        //Bank/Cheque type
        hm = new HashMap();
        hm.put("methodname", "test-bank");
        hm.put("detailtype", 2);//change type with any other types from the db.
        hm.put("accountid", bankAccountID);
        hm.put("companyid", companyid);

        instance = new accPaymentImpl();
        instance.setSessionFactory(sessionFactory);
        result = instance.addPaymentMethod(hm);
        if(result.isSuccessFlag()) {
            payMethodBankID = ((PaymentMethod)result.getEntityList().get(0)).getID();
        }
        assertTrue("Success flag : ", result.isSuccessFlag());
        assertEquals("Object returned on add : ", 1, result.getEntityList().size());

        //commit the transaction to verify result in database.
        transactionManager.commit(transactionStatus);
    }

    /**
     * Test of updatePaymentMethod method, of class accPaymentImpl.
     */
    public void testUpdatePaymentMethod() throws Exception {
        System.out.println("updatePaymentMethod");

        //Cash Type
        HashMap hm = new HashMap();
        hm.put("methodid", payMethodCashID);
        hm.put("methodname", "test-cash1");
        hm.put("detailtype", 0);
        hm.put("accountid", bankAccountID);
        hm.put("companyid", companyid);
        accPaymentImpl instance = new accPaymentImpl();
        instance.setSessionFactory(sessionFactory);
        KwlReturnObject result = instance.updatePaymentMethod(hm);
        assertTrue("Success flag : ", result.isSuccessFlag());
        assertNotNull("Result Object is NULL : ", result.getEntityList().get(0));

        //Card Type
        hm = new HashMap();
        hm.put("methodid", payMethodCardID);
        hm.put("methodname", "test-card1");
        hm.put("detailtype", 1);
        hm.put("accountid", bankAccountID);
        hm.put("companyid", companyid);
        instance = new accPaymentImpl();
        instance.setSessionFactory(sessionFactory);
        result = instance.updatePaymentMethod(hm);
        assertTrue("Success flag : ", result.isSuccessFlag());
        assertNotNull("Result Object is NULL : ", result.getEntityList().get(0));

        //Bank/Cheque Type
        hm = new HashMap();
        hm.put("methodid", payMethodBankID);
        hm.put("methodname", "test-bank1");
        hm.put("detailtype", 2);
        hm.put("accountid", bankAccountID);
        hm.put("companyid", companyid);
        instance = new accPaymentImpl();
        instance.setSessionFactory(sessionFactory);
        result = instance.updatePaymentMethod(hm);        
        assertTrue("Success flag : ", result.isSuccessFlag());
        assertNotNull("Result Object is NULL : ", result.getEntityList().get(0));

        //commit the transaction to verify result in database.
        transactionManager.commit(transactionStatus);
    }

    /**
     * Test of buildPaymentMethod method, of class accPaymentImpl.
     */
    public void testBuildPaymentMethod() {
        System.out.println("buildPaymentMethod");

        PaymentMethod pm = new PaymentMethod();
        HashMap<String, Object> pmMap = new HashMap();
        pmMap.put("methodname", "test-data");
        pmMap.put("detailtype", 0);
        pmMap.put("accountid", bankAccountID);
        pmMap.put("companyid", companyid);

        accPaymentImpl instance = new accPaymentImpl();
        instance.setSessionFactory(sessionFactory);
        PaymentMethod result = instance.buildPaymentMethod(pm, pmMap);
        assertEquals("test-data", result.getMethodName());
        assertEquals(0, result.getDetailType());
        assertEquals(companyid, ((Company) result.getCompany()).getCompanyID());
        assertEquals(bankAccountID, ((Account) result.getAccount()).getID());
    }

    /**
     * Test of deletePaymentMethod method, of class accPaymentImpl.
     */
    public void testDeletePaymentMethod() throws Exception {
        System.out.println("deletePaymentMethod");
        accPaymentImpl instance = new accPaymentImpl();
        instance.setSessionFactory(sessionFactory);
        KwlReturnObject result = instance.deletePaymentMethod(payMethodCashID, companyid);
        System.out.println("No. of records deleted for cash : "+result.getRecordTotalCount());
        assertTrue("Cash Deletion failed ", (result.getRecordTotalCount() == 1));

        result = instance.deletePaymentMethod(payMethodCardID, companyid);
        System.out.println("No. of records deleted for card : "+result.getRecordTotalCount());
        assertTrue("Card Deletion failed ", (result.getRecordTotalCount() == 1));

        result = instance.deletePaymentMethod(payMethodBankID, companyid);
        System.out.println("No. of records deleted for Bank : "+result.getRecordTotalCount());
        assertTrue("Bank Deletion failed ", (result.getRecordTotalCount() == 1));

        //commit the transaction to verify result in database.
//        transactionManager.commit(transactionStatus);
    }

    public void testDeletePaymentMethod_withDummyMethodId() throws Exception {
        System.out.println("deletePaymentMethodDummy");
        String methodid = "abcd_xyz_dummyid";
        accPaymentImpl instance = new accPaymentImpl();
        instance.setSessionFactory(sessionFactory);
        KwlReturnObject result = instance.deletePaymentMethod(methodid, companyid);
        //commit the transaction to verify result in database.
//        transactionManager.commit(transactionStatus);
        assertEquals("Function shows affected records with dummyid : ",0,result.getRecordTotalCount());
    }

    /**
     * Test of addPayDetail method, of class accPaymentImpl.
     */
    public void testAddPayDetail_cash() throws Exception {
        System.out.println("addPayDetail");

        HashMap hm = new HashMap();
        //select * from paymentmethod where company = 'a4792363-b0e1-4b67-992b-2851234d5ea6' and detailtype = 0;
        hm.put("paymethodid", payMethodCashID); //change the id with any other id from the db. Current ID for Cash In Hand method for demo company.
        hm.put("companyid", companyid);

        accPaymentImpl instance = new accPaymentImpl();
        instance.setSessionFactory(sessionFactory);
        KwlReturnObject result = instance.addPayDetail(hm);
        //commit the transaction to verify result in database.
        transactionManager.commit(transactionStatus);
        if(result.isSuccessFlag()) {
            payDetailIDCash = ((PayDetail)result.getEntityList().get(0)).getID();
        }
        assertTrue("Success flag : ", result.isSuccessFlag());
        assertEquals("PayDetail object returned on add : ", 1, result.getEntityList().size());
    }

    public void testAddPayDetail_cheque() throws Exception {
        System.out.println("addPayDetail");

        HashMap hm = new HashMap();
        //select * from paymentmethod where company = 'a4792363-b0e1-4b67-992b-2851234d5ea6' and detailtype = 2;
        hm.put("paymethodid", payMethodBankID); //change the id with any other id from the db. Current ID for Cheque method for demo company.
        hm.put("companyid", companyid);
        //select * from cheque where BankMasterItem in (select id from masteritem where company = 'a4792363-b0e1-4b67-992b-2851234d5ea6' );
        hm.put("chequeid", checkID); //change the id with any other id from the db.
        
        accPaymentImpl instance = new accPaymentImpl();
        instance.setSessionFactory(sessionFactory);
        KwlReturnObject result = instance.addPayDetail(hm);
        //commit the transaction to verify result in database.
        transactionManager.commit(transactionStatus);
        if(result.isSuccessFlag()) {
            payDetailIDCheque = ((PayDetail)result.getEntityList().get(0)).getID();
        }
        assertTrue("Success flag : ", result.isSuccessFlag());
        assertEquals("PayDetail object returned on add : ", 1, result.getEntityList().size());
    }

    public void testAddPayDetail_card() throws Exception {
        System.out.println("addPayDetail");

        HashMap hm = new HashMap();
        //select * from paymentmethod where company = 'a4792363-b0e1-4b67-992b-2851234d5ea6' and detailtype = 1;
        hm.put("paymethodid", payMethodCardID); //change the id with any other id from the db. Current ID for Cash In Hand method for demo company.
        hm.put("companyid", companyid);
        //select * from card;
        hm.put("cardid", cardID); //change the id with any other id from the db.

        accPaymentImpl instance = new accPaymentImpl();
        instance.setSessionFactory(sessionFactory);
        KwlReturnObject result = instance.addPayDetail(hm);
        //commit the transaction to verify result in database.
        transactionManager.commit(transactionStatus);
        if(result.isSuccessFlag()) {
            payDetailIDCard = ((PayDetail)result.getEntityList().get(0)).getID();
        }
        assertTrue("Success flag : ", result.isSuccessFlag());
        assertEquals("PayDetail object returned on add : ", 1, result.getEntityList().size());
    }
    
    /**
     * Test of updatePayDetail method, of class accPaymentImpl.
     *
     * This function is not called from the code.
     */
    public void testUpdatePayDetail_cash() throws Exception {
        System.out.println("addPayDetail");

        HashMap hm = new HashMap();
        hm.put("paydetailid", payDetailIDCash);
        //select * from paymentmethod where company = 'a4792363-b0e1-4b67-992b-2851234d5ea6' and detailtype = 0;
        hm.put("paymethodid", payMethodCashID); //change the id with any other id from the db. Current ID for Cash In Hand method for demo company.
        hm.put("companyid", companyid);

        accPaymentImpl instance = new accPaymentImpl();
        instance.setSessionFactory(sessionFactory);
        KwlReturnObject result = instance.updatePayDetail(hm);
        //commit the transaction to verify result in database.
        transactionManager.commit(transactionStatus);
        assertTrue("Success flag : ", result.isSuccessFlag());
        assertNotNull("Result PayDetail is NULL : ", result.getEntityList().get(0));
    }

    public void testUpdatePayDetail_cheque() throws Exception {
        System.out.println("addPayDetail");

        HashMap hm = new HashMap();
        hm.put("paydetailid", payDetailIDCheque);
        //select * from paymentmethod where company = 'a4792363-b0e1-4b67-992b-2851234d5ea6' and detailtype = 2;
        hm.put("paymethodid", payMethodBankID); //change the id with any other id from the db. Current ID for Cheque method for demo company.
        hm.put("companyid", companyid);
        //select * from cheque where BankMasterItem in (select id from masteritem where company = 'a4792363-b0e1-4b67-992b-2851234d5ea6' );
        hm.put("chequeid", checkID); //change the id with any other id from the db.

        accPaymentImpl instance = new accPaymentImpl();
        instance.setSessionFactory(sessionFactory);
        KwlReturnObject result = instance.updatePayDetail(hm);
        //commit the transaction to verify result in database.
        transactionManager.commit(transactionStatus);
        assertTrue("Success flag : ", result.isSuccessFlag());
        assertNotNull("Result PayDetail is NULL : ", result.getEntityList().get(0));
    }

    public void testUpdatePayDetail_card() throws Exception {
        System.out.println("addPayDetail");

        HashMap hm = new HashMap();
        hm.put("paydetailid", payDetailIDCard);
        //select * from paymentmethod where company = 'a4792363-b0e1-4b67-992b-2851234d5ea6' and detailtype = 1;
        hm.put("paymethodid", payMethodCardID); //change the id with any other id from the db. Current ID for Cash In Hand method for demo company.
        hm.put("companyid", companyid);
        //select * from card;
        hm.put("cardid", cardID); //change the id with any other id from the db.

        accPaymentImpl instance = new accPaymentImpl();
        instance.setSessionFactory(sessionFactory);
        KwlReturnObject result = instance.updatePayDetail(hm);
        //commit the transaction to verify result in database.
        transactionManager.commit(transactionStatus);
        assertTrue("Success flag : ", result.isSuccessFlag());
        assertNotNull("Result PayDetail is NULL : ", result.getEntityList().get(0));
    }

    /**
     * Test of getPaymentMethodFromAccount method, of class accPaymentImpl.
     */
    public void testGetPaymentMethodFromAccount() throws Exception {
        System.out.println("getPaymentMethodFromAccount");
        accPaymentImpl instance = new accPaymentImpl();
        instance.setSessionFactory(sessionFactory);
        KwlReturnObject result = instance.getPaymentMethodFromAccount(bankAccountID, companyid);
        //To Check success flag in KWLReturnObject
        assertTrue("Success Flag false : ", result.isSuccessFlag());
        //To Check if fetched list is not null
        assertNotNull("Result Entrylist is NULL : ", result.getEntityList());

        System.out.println("No. of records fetched : "+result.getRecordTotalCount());
    }

    /**
     * Test of getPaymentMethod method, of class accPaymentImpl.
     */
    public void testGetPaymentMethod() throws Exception {
        System.out.println("getPaymentMethod");
        HashMap<String, Object> filterParams = new HashMap();
        accPaymentImpl instance = new accPaymentImpl();
        instance.setSessionFactory(sessionFactory);
        KwlReturnObject result = instance.getPaymentMethod(filterParams);
        //To Check success flag in KWLReturnObject
        assertTrue("Success Flag false : ", result.isSuccessFlag());
        //To Check if fetched list is not null
        assertNotNull("Result Entrylist is NULL : ", result.getEntityList());

        System.out.println("No. of records fetched : "+result.getRecordTotalCount());
    }

    public void testGetPaymentMethod_withCompanyIdFilter() throws Exception {
        System.out.println("getPaymentMethod");
        HashMap<String, Object> filterParams = new HashMap();
        filterParams.put("companyid", companyid);
        accPaymentImpl instance = new accPaymentImpl();
        instance.setSessionFactory(sessionFactory);
        KwlReturnObject result = instance.getPaymentMethod(filterParams);
        //To Check success flag in KWLReturnObject
        assertTrue("Success Flag false : ", result.isSuccessFlag());
        //To Check if fetched list is not null
        assertNotNull("Result Entrylist is NULL : ", result.getEntityList());

        System.out.println("No. of records fetched : "+result.getRecordTotalCount());
    }

    /**
     * Test of copyPaymentMethods method, of class accPaymentImpl.
     */
    public void testCopyPaymentMethods() throws Exception {
        System.out.println("copyPaymentMethods");
        //This function is called on new company creation. It requires HashMap of new accounts created.
//        HashMap hm = new HashMap();
//        accPaymentImpl instance = new accPaymentImpl();
//        instance.copyPaymentMethods(companyid, hm);
    }

}
