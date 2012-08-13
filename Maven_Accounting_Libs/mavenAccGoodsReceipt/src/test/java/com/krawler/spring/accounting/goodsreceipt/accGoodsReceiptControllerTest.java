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

package com.krawler.spring.accounting.goodsreceipt;

import com.krawler.common.service.ServiceException;
import com.krawler.common.util.StringUtil;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.discount.accDiscountDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.journalentry.accJournalEntryDAO;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import com.mchange.util.AssertException;
import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.test.AbstractTransactionalDataSourceSpringContextTests;

/**
 *
 * @author krawler
 */
public class accGoodsReceiptControllerTest extends AbstractTransactionalDataSourceSpringContextTests{
    private SessionFactory sessionFactory = null;
    private HibernateTransactionManager txnManager;
    private accGoodsReceiptDAO accGoodsReceiptobj;
    private accJournalEntryDAO accJournalEntryobj;
    private accProductDAO accProductObj;
    private accDiscountDAO accDiscountobj;
    private accCurrencyDAO accCurrencyDAOobj;
    private accCompanyPreferencesDAO accCompanyPreferencesObj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private static String sessionParams = "{\"tzdiff\":\"-07:00\",\"timezoneid\":\"23\",\"roleid\":\"ff8080812b99b47f012b99ba5e8d0003\"," +
            "\"callwith\":1,\"companyPreferences\":\"\",\"dateformatid\":\"1\",\"lid\":\"99f1eb77-cac9-41bb-977b-c8bc17fb3daa\",\"companyid\":\"a4792363-b0e1-4b67-992b-2851234d5ea6\"," +
            "\"username\":\"demo\",\"currencyid\":\"1\",\"company\":\"Demo\",\"success\":true,\"superuser\":\"ff8080812b99b47f012b99ba5e8d0003\",\"timeformat\":2,\"userfullname\":\"Deskera \"," +
            "\"perms\":[{\"creditterm\":3},{\"paymentmethod\":3},{\"customer\":255},{\"vendor\":255},{\"coa\":4095},{\"invoice\":4294967295},{\"fstatement\":262143},{\"audittrail\":1},{\"useradmin\":7},{\"accpref\":3},{\"groups\":3},{\"product\":4095},{\"uom\":3},{\"bankreconciliation\":1},{\"currencyexchange\":3},{\"salesbyitem\":1},{\"vendorinvoice\":4294967295},{\"qanalysis\":3},{\"fixedasset\":1023},{\"masterconfig\":15},{\"importlog\":1},{\"tax\":3}]}";
    
    public accGoodsReceiptControllerTest(String testName) {
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

  public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }
    public void setaccGoodsReceiptDAO(accGoodsReceiptDAO accGoodsReceiptobj) {
        this.accGoodsReceiptobj = accGoodsReceiptobj;
    }
    public void setaccJournalEntryDAO(accJournalEntryDAO accJournalEntryobj) {
        this.accJournalEntryobj = accJournalEntryobj;
    }
    public void setaccProductDAO(accProductDAO accProductObj) {
        this.accProductObj = accProductObj;
    }
    public void setaccDiscountDAO(accDiscountDAO accDiscountobj){
        this.accDiscountobj = accDiscountobj;
    }
    public void setaccCurrencyDAO(accCurrencyDAO accCurrencyDAOobj) {
        this.accCurrencyDAOobj = accCurrencyDAOobj;
    }
    public void setaccCompanyPreferencesDAO(accCompanyPreferencesDAO accCompanyPreferencesObj) {
        this.accCompanyPreferencesObj = accCompanyPreferencesObj;
    }
    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }
    /**
     * Spring will automatically inject the Hibernate session factory on startup
     * @param sessionFactory
     */
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
    public void testSetSessionFactory() {
        System.out.println("setSessionFactory");
        assertNotNull(sessionFactory);
    }
    /**
     * Test of saveGoodsReceipt method, of class accGoodsReceiptController.
     */
//        public void testSaveGoodsReceipt_HttpServletRequest_HttpServletResponse() {
//        System.out.println("saveGoodsReceipt");
//            try {
//                MockHttpServletRequest request = new MockHttpServletRequest();
//                MockHttpSession session = createUserMockSession(new JSONObject(sessionParams));
//                request.setParameter("billdate", "Nov 20, 2010 12:00:00 AM");
//                request.setParameter("billto", "Omega Paradise, Near IIFD, Church square, Sydney");
//                request.setParameter("creditoraccount", "");
//                request.setParameter("currencyid", "1");
//                request.setParameter("discount", "0");
//                request.setParameter("duedate", "Nov 23, 2010 12:00:00 AM");
//                request.setParameter("expensedetail", "[{rowid:\"undefined\",accountid:\"c340667e2a6568d4012a6582587a0005\",billid:\"undefined\",billno:\"undefined\",rate:\"800\",discamount:\"undefined\",discount:\"undefined\",prdiscount:\"50\",prtaxid:\"\",prtaxname:\"\",prtaxpercent:\"undefined\",taxamount:\"0\",calamount:\"400\",discountamount:\"400\",currencysymbol:\"&#36;\",taxpercent:\"0\",accountname:\"undefined\",totalamount:\"0\",amount:\"400\",transectionno:\"undefined\",orignalamount:\"undefined\",typeid:\"0\",isNewRecord:\"1\",modified:true}]");
//                request.setParameter("externalcurrencyrate", "0");
//                request.setParameter("incash", "0");
//                request.setParameter("includepropax", "false");
//                request.setParameter("isExpenseInv", "true");
//                request.setParameter("memo", "");
//                request.setParameter("mode", "11");
//                request.setParameter("number", "VINV089075");
//                request.setParameter("perdiscount", "false");
//                request.setParameter("porefno", "");
//                request.setParameter("prdiscount", "false");
//                request.setParameter("shipaddress", "3, Omega Paradise, Near IIFD, Church square, Sydney");
//                request.setParameter("shipdate", "Nov 20, 2010 12:00:00 AM");
//                request.setParameter("subTotal", "400");
//                request.setParameter("taxamount", "160");
//                request.setParameter("taxid", "c340667e2742dd9d01274686cd1a0029");
//                request.setParameter("term", "3");
//                request.setParameter("vendor", "c340667e254308e70125448edf450090");
//                request.setSession(session);
//                MockHttpServletResponse response = new MockHttpServletResponse();
//                accGoodsReceiptController instance = new accGoodsReceiptController();
//                instance.setaccCompanyPreferencesDAO(accCompanyPreferencesObj);
//                instance.setaccJournalEntryDAO(accJournalEntryobj);
//                instance.setaccGoodsReceiptDAO(accGoodsReceiptobj);
//                instance.setaccCurrencyDAO(accCurrencyDAOobj);
//                instance.setaccProductDAO(accProductObj);
//                instance.setaccountingHandlerDAO(accountingHandlerDAOobj);
//                instance.setSuccessView("jsonView");
//                instance.setaccDiscountDAO(accDiscountobj);
//                instance.setTxnManager((HibernateTransactionManager) transactionManager);
//                ModelAndView result = instance.saveGoodsReceipt(request, response);
//                JSONObject job = new JSONObject(result.getModel().get("model").toString());
//                //commit the transaction to verify result in database.
//    //            transactionManager.commit(transactionStatus);
//                assertTrue(job.getBoolean("success"));
//        } catch (ServiceException ex) {
//            throw new AssertException("Service Exception - error creating user session");
//        } catch (JSONException ex) {
//            throw new AssertException("JSON Exception - error parsing model json");
//        }
//    }


//    public void testSaveGoodsReceipt_HttpServletRequest() throws Exception {
//        System.out.println("saveGoodsReceipt");
//                MockHttpServletRequest request = new MockHttpServletRequest();
//                MockHttpSession session = createUserMockSession(new JSONObject(sessionParams));
//                request.setParameter("billdate", "Nov 20, 2010 12:00:00 AM");
//                request.setParameter("billto", "Omega Paradise, Near IIFD, Church square, Sydney");
//                request.setParameter("creditoraccount", "");
//                request.setParameter("currencyid", "1");
//                request.setParameter("discount", "0");
//                request.setParameter("duedate", "Nov 23, 2010 12:00:00 AM");
//                request.setParameter("expensedetail", "[{rowid:\"undefined\",accountid:\"c340667e2a6568d4012a6582587a0005\",billid:\"undefined\",billno:\"undefined\",rate:\"800\",discamount:\"undefined\",discount:\"undefined\",prdiscount:\"50\",prtaxid:\"\",prtaxname:\"\",prtaxpercent:\"undefined\",taxamount:\"0\",calamount:\"400\",discountamount:\"400\",currencysymbol:\"&#36;\",taxpercent:\"0\",accountname:\"undefined\",totalamount:\"0\",amount:\"400\",transectionno:\"undefined\",orignalamount:\"undefined\",typeid:\"0\",isNewRecord:\"1\",modified:true}]");
//                request.setParameter("externalcurrencyrate", "0");
//                request.setParameter("incash", "0");
//                request.setParameter("includepropax", "false");
//                request.setParameter("isExpenseInv", "true");
//                request.setParameter("memo", "");
//                request.setParameter("mode", "11");
//                request.setParameter("number", "VINV089075");
//                request.setParameter("perdiscount", "false");
//                request.setParameter("porefno", "");
//                request.setParameter("prdiscount", "false");
//                request.setParameter("shipaddress", "3, Omega Paradise, Near IIFD, Church square, Sydney");
//                request.setParameter("shipdate", "Nov 20, 2010 12:00:00 AM");
//                request.setParameter("subTotal", "400");
//                request.setParameter("taxamount", "160");
//                request.setParameter("taxid", "c340667e2742dd9d01274686cd1a0029");
//                request.setParameter("term", "3");
//                request.setParameter("vendor", "c340667e254308e70125448edf450090");
//                request.setSession(session);
//                accGoodsReceiptController instance = new accGoodsReceiptController();
//                instance.setaccCompanyPreferencesDAO(accCompanyPreferencesObj);
//                instance.setaccJournalEntryDAO(accJournalEntryobj);
//                instance.setaccGoodsReceiptDAO(accGoodsReceiptobj);
//                instance.setaccCurrencyDAO(accCurrencyDAOobj);
//                instance.setaccProductDAO(accProductObj);
//                instance.setaccountingHandlerDAO(accountingHandlerDAOobj);
//                instance.setSuccessView("jsonView");
//                instance.setaccDiscountDAO(accDiscountobj);
//                instance.setTxnManager((HibernateTransactionManager) transactionManager);
//                List result = instance.saveGoodsReceipt(request);
//                String[] id = (String[]) result.get(0);
//                assertTrue(!StringUtil.isNullOrEmpty(id[0]));
//    }
}
