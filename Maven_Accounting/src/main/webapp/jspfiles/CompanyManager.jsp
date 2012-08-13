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
<%@page import="com.krawler.esp.database.*" %>  
<%@page import="com.krawler.utils.json.base.*" %>
<%@page import="java.util.*" %>
<%@page import="org.apache.commons.fileupload.servlet.ServletFileUpload" %>
<%@page import="com.krawler.esp.handlers.*" %>
<jsp:useBean id="sessionbean" scope="session" class="com.krawler.esp.handlers.SessionHandler" />
<%

    JSONObject jobj=new JSONObject();
    JSONObject obj=new JSONObject();
   boolean isFormSubmit = false;
    if(sessionbean.validateSession(request, response)){
       try {
            int mode;
            HashMap hm=null;
            if(ServletFileUpload.isMultipartContent(request)){
                hm=new FileUploadHandler().getItems(request);
                mode=Integer.parseInt(hm.get("mode").toString());
            }else
                mode = Integer.parseInt(request.getParameter("mode"));

            boolean isLoggable = false;
            int actiontype=0;
            String actiontext="";
            switch (mode) {
                /*case 1 :
                     obj=AccountDBCon.getGroups(request);
                break;*/
                /*case 2 :
                     obj=AccountDBCon.getAccounts(request);
                break;*/
                /*case 3 :
                     String accID=AccountDBCon.saveAccount(request);
                     obj.put("msg", "Account has been saved successfully");
                     obj.put("accID",accID);
                break;*/
                /*case 4:
                     obj=AccountDBCon.getAccountDepreciation(request); 
                break;*/
                /*case 5:
                     AccountDBCon.saveAccountDepreciation(request);
                     obj.put("msg", "Depreciation has been done successfully");
                break;*/
                /*case 6 :
                     AccountDBCon.deleteAccount(request);
                     obj.put("msg", "Account has been deleted successfully");
                break;*/
                /*case 9 :
                     AccountDBCon.deleteGroup(request);
                     obj.put("msg", "Group has been deleted successfully");
                break;*/
                /*case 10 :
                     String groupID=AccountDBCon.saveGroup(request);
                     obj.put("msg", "Group has been saved successfully");
                     obj.put("groupID",groupID);
                break;*/
                /*case 11 :
                     AccountDBCon.setNewPrice(request);
                     obj.put("msg", "New Price has been applied successfully");
                break;*/
                /*case 12 :
                     obj=AccountDBCon.getProductPrice(request);
                break;*/
                /*case 21 :
                     String productID=AccountDBCon.saveProduct(request);
                     obj.put("msg", "Product has been saved successfully");
                     obj.put("productID",productID);
                break;*/
                /*case 22 :
                     obj=AccountDBCon.getProducts(request);
                break;*/
                /*case 23:
                     AccountDBCon.deleteProducts(request);
                     obj.put("msg", "Product has been deleted successfully");
                break;*/
                /*case 31 :
                     obj=AccountDBCon.getUnitOfMeasure(request);
                break;*/
                /*case 32 :
                     AccountDBCon.saveUnitOfMeasure(request);
                     obj.put("msg", "Unit of measure has been updated successfully");
                break;*/
                /*case 33:
                     obj=AccountDBCon.getTax(request);
                break;*/
                /*case 34:
                     AccountDBCon.saveTax(request);
                     obj.put("msg", "Tax details has been updated successfully");
                break;*/
                /*case 35:
                     boolean flag=AccountDBCon.checkApplyTax(request);
                     obj.put("msg", flag);
                break;*/

                /*case 42 :
                     obj=AccountDBCon.getInventory(request);
                break;*/
                /*case 51 :
                     obj=AccountDBCon.getPaymentMethods(request);
                break;*/
                /*case 52 :
                     AccountDBCon.savePaymentMethod(request);
                     obj.put("msg", "Payment method has been Updated successfully");
                break;*/
                /*case 53 :
                     AccountDBCon.saveJournalEntry(request);
                     obj.put("msg", "Journal Entry has been saved successfully");
                break;*/
                /*case 54 :
                     obj=AccountDBCon.getJournalEntry(request);
                break;*/
                /*case 55 :
                     obj=AccountDBCon.getJournalEntryDetails(request);
                break;*/
               /*case 56 :
                     AccountDBCon.saveBankReconciliation(request);
                     obj.put("msg", "Bank Reconciliation Entry has been saved successfully");
                break;*/
                /*case 57 :
                     obj=AccountDBCon.getBankReconciliation(request);
                break;
                */
                /*case 58:
                     AccountDBCon.deleteBankReconciliation(request);
                     obj.put("msg", "Bank Reconciliation has been deleted successfully");
                break;*/
                /*case 61 :
                     obj=AccountDBCon.getLedger(request);
                break;*/
                /*case 62 :
                     obj=AccountDBCon.getTrialBalance(request);
                break;*/
                /*case 63 :
                     obj=AccountDBCon.getTrading(request);
                break;*/
                /*case 64 :
                     obj=AccountDBCon.getProfitLoss(request);
                break;*/
                /*case 65 :
                     obj=AccountDBCon.getTradingAndProfitLoss(request);
                break;*/
                /*case 66 :
                     obj=AccountDBCon.getBalanceSheet(request);
                break;*/
                /* case 67 :
                     obj=AccountDBCon.getReconciliationData(request);
                break;*/
                /*case 68 :
                     obj=AccountDBCon.getAccountOpeningBalance(request);
                break;*/
                /*case 81 :
                     obj=AccountDBCon.getCompanyAccountPreferences(request);
                break;*/
                /*case 82 :
                     AccountDBCon.saveCompanyAccountPreferences(request);
                     obj.put("msg", "Account Preferences has been saved successfully");
                break;*/
                /*case 83 :
                     obj=AccountDBCon.getNextAutoNumber(request);
                break;*/
                /*case 91 :
                     obj=AccountDBCon.getTerm(request);
                break;*/
                /*case 92 :
                     AccountDBCon.saveTerm(request);
                     obj.put("msg", "Term has been Updated successfully");
                break;*/
                /*case 93 :
                     AccountDBCon.saveYearLock(request);
                     obj.put("msg", "Lock has been Updated successfully");
                break;*/
                /*case 94 :
                     obj=AccountDBCon.getYearLock(request);
                break;*/
                /*case 111:
                     obj=AccountDBCon.getMasterGroups(request);
                break;*/
                /*case 112:
                     obj=AccountDBCon.getMasterItems(request);
                break;*/
                /*case 113:
                     AccountDBCon.saveMasterGroup(request);
                     obj.put("msg", "Master group has been saved successfully");
                break;*/
                /*case 114:
                     AccountDBCon.saveMasterItem(request);
                     obj.put("msg", "Master item has been saved successfully");
                break;*/
                /*case 115:
                     AccountDBCon.deleteMasterGroup(request);
                     obj.put("msg", "Master group has been deleted successfully");
                break;*/
                /*case 116:
                     AccountDBCon.deleteMasterItem(request);
                     obj.put("msg", "Master item has been deleted successfully");
                break;*/
                /*case 201:
                     obj=AccountDBCon.getCurrencyExchange(request);
                break;*/
                /*case 202:
                     AccountDBCon.saveCurrencyExchange(request);
                     obj.put("msg", "Currency Exchange Rate has been updated successfully");
                break;*/
                /* case 203:
                     obj=AccountDBCon.getCurrencyExchangeList(request);
                break;*/
                /*case 204:
                     obj=AccountDBCon.getDefaultCurrencyExchange(request);
                break;*/
                // TODO case 204:
                //     AccountDBCon.saveCurrencyExchangeList(request);
                //     obj.put("msg", "Master item has been deleted successfully");
                //break;
                /*case 205:
                     AccountDBCon.deleteJournalEntries(request);
                     obj.put("msg", "Journal Entry has been deleted successfully");
                break;*/
            }
            obj.put("success",true);
         } catch (Exception e) {
            obj.put("success", false);
            obj.put("msg", e.getMessage());
        } finally {
            jobj.put("valid", true);
            if(!isFormSubmit){
                jobj.put("data", obj);
            }else{
                jobj=obj;
            }
        }
    } else {
        sessionbean.destroyUserSession(request, response);
         jobj.put("valid", false);
    }

    out.println(jobj);
%>
