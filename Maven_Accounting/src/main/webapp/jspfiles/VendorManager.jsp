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
                     String perAccID=AccountDBCon.saveVendor(request);
                     obj.put("msg", "Vendor information has been saved successfully");
                     obj.put("perAccID",perAccID);
               break;*/
               /*case 2 :
                     obj=AccountDBCon.getVendors(request);
               break;*/
               /*case 3:
                    AccountDBCon.deleteAccount(request);
                    obj.put("msg", "Vendor has been deleted successfully.");
               break;*/
               /*case 4:
                    obj=AccountDBCon.getVendorAddress(request);
               break;*/
               /*case 11 :
                    AccountDBCon.saveGoodsReceipt(request);
                    Boolean cash=Boolean.parseBoolean(request.getParameter("incash"));                      
                    obj.put("msg", (cash?"Purchase Receipt":"Vendor Invoice")+" has been saved successfully");
               break;*/
               /*case 12 :
                     obj=AccountDBCon.getGoodsReceipts(request);
               break;*/
               /*case 14 :
                    obj=AccountDBCon.getGoodsReceiptRows(request);
               break;*/
               /*case 15 :
                     AccountDBCon.deleteGoodsReceipt(request);
                     obj.put("msg", "Vendor Invoice(s) has been deleted successfully");
                break;*/
                /*case 18 :
                     obj=AccountDBCon.getVendorAgedPayable(request);
                break;*/
                /*case 22 :
                     AccountDBCon.deletePayment(request);
                     obj.put("msg", "Payment(s) has been deleted successfully");
                break;*/
               /*case 27 :
                    AccountDBCon.saveDebitNote(request);
                    obj.put("msg", "Debit note has been saved successfully");
               break;*/
               /*case 28 :
                    obj=AccountDBCon.getDebitNotes(request);                    
               break;*/
               /*case 29 :
                    obj=AccountDBCon.getDebitNoteRows(request);                    
               break;*/
               /*case 31 :
                     AccountDBCon.savePayment(request);
                     obj.put("msg","Payment information has been saved successfully");
               break;*/
               /*case 32 :
                     obj=AccountDBCon.getPayments(request);
               break;*/
               /*case 33 :
                     obj=AccountDBCon.getPaymentRows(request);
                break;*/
               /*case 41 :
                     AccountDBCon.savePurchaseOrder(request);
                     obj.put("msg","Purchase order has been saved successfully");
               break;*/
               /*case 42 :
                     obj=AccountDBCon.getPurchaseOrders(request);
               break;*/
               /*case 43 :
                     obj=AccountDBCon.getPurchaseOrderRows(request);
               break;*/
               /*case 44 :
                     AccountDBCon.deletePurchaseOrders(request);
                     obj.put("msg","Purchase order has been deleted successfully");
               break;*/
               /*case 45 :
                     AccountDBCon.deleteDebitNotes(request);
                     obj.put("msg","Debit note has been deleted successfully");
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
