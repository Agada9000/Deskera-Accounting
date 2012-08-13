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
<%@page import="com.krawler.hql.accounting.StaticValues" %>
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
                     String perAccID=AccountDBCon.saveCustomer(request);
                     obj.put("msg","Customer information has been saved successfully");
                     obj.put("perAccID",perAccID);
                break;*/
                /*case 2 :
                     obj=AccountDBCon.getCustomers(request);
                break;*/
                /*case 3:
                     AccountDBCon.deleteAccount(request);
                     obj.put("msg", "Customer has been deleted successfully");
                break;*/
                /*case 4:
                     obj=AccountDBCon.getCustomerAddress(request);
                break;*/
               /*case 11 :
                     String id=AccountDBCon.saveInvoice(request);
                     Boolean cash=Boolean.parseBoolean(request.getParameter("incash"));
                     if(!cash){
                        try{
                            AccountDBCon.sendMailToCustomer(request, StaticValues.AUTONUM_INVOICE, id);
                        }catch(Exception ex){
                        }
                     }
                     obj.put("msg", (cash?"Sales Receipt":"Invoice")+" has been saved successfully");
                break;*/
                /*case 12 :
                     obj=AccountDBCon.getInvoices(request);
                break;*/
                /* case 13 :
                     id=AccountDBCon.saveBillingInvoice(request);
                     cash=Boolean.parseBoolean(request.getParameter("incash"));
                     if(!cash)
                        AccountDBCon.sendMailToCustomer(request, StaticValues.AUTONUM_BILLINGINVOICE, id);
                     obj.put("msg", (cash?"Sales Receipt":"Invoice")+" has been saved successfully");
                break;*/
                /*case 14 :
                     obj=AccountDBCon.getInvoiceRows(request);
                break;*/
                /*case 15 :
                     AccountDBCon.deleteInvoices(request);
                     obj.put("msg", "Invoice(s) has been deleted successfully");
                break;*/
                /*case 16 :
                     obj=AccountDBCon.getBillingInvoices(request);
                break;*/
                 /*case 17 :
                     obj=AccountDBCon.getBillingInvoiceRows(request);
                break;*/
                /*case 18 :
                     obj=AccountDBCon.getCustomerAgedReceivable(request);
                break;*/
                 /*case 19 :
                     obj=AccountDBCon.getAgedReceivable(request);
                break;*/
                /* case 20 :
                     obj=AccountDBCon.getInvoicesCurrencyRate(request);
                break;*/
                /*case 21 :
                     AccountDBCon.deleteBillingReceipt(request);
                     obj.put("msg", "Billing Receipt(s) has been deleted successfully");
                break;*/
                /*case 22 :
                      AccountDBCon.deleteReceipt(request);
                     obj.put("msg", "Receipt(s) has been deleted successfully");
                break;*/
                /*case 23 :
                     AccountDBCon.deleteBillingInvoices(request);
                     obj.put("msg", "Invoice(s) has been deleted successfully");
                break;*/
                /*case 26 :
                     AccountDBCon.saveCreditNote(request);
                      obj.put("msg", "Credit note has been saved successfully");
                break;*/
                /*case 27 :
                     obj=AccountDBCon.getCreditNote(request);
                break;*/
                /*case 28 :
                     obj=AccountDBCon.getCreditNoteRows(request);
                break;*/
                /*case 31 :
                     AccountDBCon.saveReceipt(request);
                     obj.put("msg", "Receipt has been saved successfully");
                break;*/
                /*case 32 :
                     obj=AccountDBCon.getReceipts(request);
                break;*/
                /*case 33 :
                     obj=AccountDBCon.getReceiptRows(request);
                break;*/
                /*case 34 :
                     AccountDBCon.saveBillingReceipt(request);
                     obj.put("msg", "Receipt has been saved successfully");
                break;*/
                /*case 35 :
                     obj=AccountDBCon.getBillingReceipts(request);
                break;*/
                /*case 36 :
                     obj=AccountDBCon.getBillingReceiptRows(request);
                break;*/
                /*case 41 :
                     AccountDBCon.saveSalesOrder(request);
                     obj.put("msg","Sales order has been saved successfully");
               break;*/
               /*case 42 :
                     obj=AccountDBCon.getSalesOrders(request);
               break;*/
               /*case 43 :
                     obj=AccountDBCon.getSalesOrderRows(request);
               break;*/
               /*case 44 :
                     AccountDBCon.deleteSalesOrders(request);
                     obj.put("msg","Sales order has been deleted successfully");
               break;*/
               /*case 45 :
                     AccountDBCon.deleteCreditNotes(request);
                     obj.put("msg","Credit note has been deleted successfully");
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
