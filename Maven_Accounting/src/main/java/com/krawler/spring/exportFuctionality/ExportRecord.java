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
package com.krawler.spring.exportFuctionality;

import com.krawler.accounting.fontsetting.FontContext;
import com.krawler.accounting.fontsetting.FontFamily;
import com.krawler.accounting.fontsetting.FontFamilySelector;
import com.krawler.common.admin.Company;
import com.krawler.common.admin.KWLCurrency;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.hql.accounting.Account;
import com.krawler.hql.accounting.BillingGoodsReceipt;
import com.krawler.hql.accounting.BillingGoodsReceiptDetail;
import com.krawler.hql.accounting.BillingInvoice;
import com.krawler.hql.accounting.BillingInvoiceDetail;
import com.krawler.hql.accounting.BillingPayment;
import com.krawler.hql.accounting.BillingReceipt;
import com.krawler.hql.accounting.CompanyAccountPreferences;
import com.krawler.hql.accounting.Discount;
import com.krawler.hql.accounting.GoodsReceipt;
import com.krawler.hql.accounting.GoodsReceiptDetail;
import com.krawler.hql.accounting.Invoice;
import com.krawler.hql.accounting.InvoiceDetail;
import com.krawler.hql.accounting.JournalEntryDetail;
import com.krawler.hql.accounting.Payment;
import com.krawler.hql.accounting.Receipt;
import com.krawler.hql.accounting.StaticValues;
import com.krawler.hql.accounting.BillingSalesOrder;
import com.krawler.spring.accounting.invoice.accInvoiceDAO;
import com.krawler.hql.accounting.BillingSalesOrderDetail;
import com.krawler.hql.accounting.BillingPurchaseOrder;
import com.krawler.hql.accounting.BillingPurchaseOrderDetail;
import com.krawler.hql.accounting.BillingCreditNote;
import com.krawler.hql.accounting.BillingCreditNoteDetail;
import com.krawler.hql.accounting.BillingDebitNote;
import com.krawler.hql.accounting.BillingDebitNoteDetail;
import com.krawler.hql.accounting.Group;
import com.krawler.hql.accounting.Quotation;
import com.krawler.hql.accounting.QuotationDetail;
import com.krawler.hql.accounting.SalesOrder;
import com.krawler.hql.accounting.SalesOrderDetail;
import com.krawler.hql.accounting.PurchaseOrder;
import com.krawler.hql.accounting.PurchaseOrderDetail;
import com.krawler.hql.accounting.Vendor;
import com.krawler.hql.accounting.Customer;
import com.krawler.hql.accounting.DebitNote;
import com.krawler.hql.accounting.DebitNoteDetail;
import com.krawler.hql.accounting.CreditNote;
import com.krawler.hql.accounting.CreditNoteDetail;
import com.krawler.hql.accounting.ExpenseGRDetail;
import com.krawler.hql.accounting.Tax;
import com.krawler.spring.accounting.creditnote.accCreditNoteDAO;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.debitnote.accDebitNoteDAO;
import com.krawler.spring.accounting.goodsreceipt.accGoodsReceiptDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.purchaseorder.accPurchaseOrderDAO;
import com.krawler.spring.accounting.salesorder.accSalesOrderDAO;
import com.krawler.spring.accounting.tax.accTaxDAO;
import com.krawler.spring.authHandler.authHandlerDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.exportFunctionality.exportMPXDAOImpl;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.ExceptionConverter;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import javax.servlet.http.HttpServletResponse;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.web.servlet.support.RequestContextUtils;


public class ExportRecord implements MessageSourceAware{

    private static final long serialVersionUID = -763555229410947890L;
    private static Font fontSmallRegular = FontFactory.getFont("Times New Roman", 10, Font.NORMAL, Color.BLACK);
    private static Font fontSmallBold = FontFactory.getFont("Times New Roman", 10, Font.BOLD, Color.BLACK);
    private static Font fontMediumRegular = FontFactory.getFont("Times New Roman", 12, Font.NORMAL, Color.BLACK);
    private static Font fontMediumBold = FontFactory.getFont("Times New Roman", 12, Font.BOLD, Color.BLACK);
    private static Font fontTblMediumBold = FontFactory.getFont("Times New Roman", 10, Font.NORMAL, Color.GRAY);
    private static Font fontTbl = FontFactory.getFont("Times New Roman", 20, Font.NORMAL, Color.GRAY);
    private static String imgPath = "";
    private EnglishNumberToWords EnglishNumberToWordsOjb = new EnglishNumberToWords();
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;
    private authHandlerDAO authHandlerDAOObj;
    private accInvoiceDAO accInvoiceDAOobj;
//    private accTaxDAO accTaxObj;
    private accSalesOrderDAO accSalesOrderDAOobj;
    private accPurchaseOrderDAO accPurchaseOrderobj;
    private accCreditNoteDAO accCreditNoteDAOobj;
    private accDebitNoteDAO accDebitNoteobj;
    private accGoodsReceiptDAO accGoodsReceiptobj;
    private accCurrencyDAO accCurrencyobj;
    private exportMPXDAOImpl exportDaoObj;
    private accTaxDAO accTaxObj;
    private MessageSource messageSource;
    
	@Override
	public void setMessageSource(MessageSource ms) {
		this.messageSource=ms;
	}

    public void setkwlCommonTablesDAO(kwlCommonTablesDAO kwlCommonTablesDAOObj1) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj1;
    }

    public void setauthHandlerDAO(authHandlerDAO authHandlerDAOObj1) {
        this.authHandlerDAOObj = authHandlerDAOObj1;
    }
    public void setaccInvoiceDAO(accInvoiceDAO accInvoiceDAOobj) {
        this.accInvoiceDAOobj = accInvoiceDAOobj;
    }
    public void setaccSalesOrderDAO(accSalesOrderDAO accSalesOrderDAOobj) {
        this.accSalesOrderDAOobj = accSalesOrderDAOobj;
    }
    public void setaccPurchaseOrderDAO(accPurchaseOrderDAO accPurchaseOrderobj) {
        this.accPurchaseOrderobj = accPurchaseOrderobj;
    }
    public void setaccCreditNoteDAO(accCreditNoteDAO accCreditNoteDAOobj) {
        this.accCreditNoteDAOobj = accCreditNoteDAOobj;
    }
    public void setaccDebitNoteDAO(accDebitNoteDAO accDebitNoteobj) {
        this.accDebitNoteobj = accDebitNoteobj;
    }
    public void setaccGoodsReceiptDAO(accGoodsReceiptDAO accGoodsReceiptobj) {
        this.accGoodsReceiptobj = accGoodsReceiptobj;
    }
    public void setaccCurrencyDAO(accCurrencyDAO accCurrencyobj) {
        this.accCurrencyobj = accCurrencyobj;
    }
    public void setexportMPXDAOImpl(exportMPXDAOImpl exportDaoObj) {
        this.exportDaoObj = exportDaoObj;
    }
    public void setaccTaxDAO (accTaxDAO accTaxObj) {
        this.accTaxObj = accTaxObj;
    }

    
    private static FontFamilySelector fontFamilySelector=new FontFamilySelector();
    static{
    	FontFamily fontFamily=new FontFamily();
    	fontFamily.addFont(FontContext.HEADER_NOTE, FontFactory.getFont("Helvetica", 10, Font.BOLD, Color.GRAY));
    	fontFamily.addFont(FontContext.FOOTER_NOTE, FontFactory.getFont("Helvetica", 8, Font.BOLD, Color.BLACK));
    	fontFamily.addFont(FontContext.LOGO_TEXT, FontFactory.getFont("Times New Roman", 14, Font.NORMAL, Color.BLACK));
    	fontFamily.addFont(FontContext.REPORT_TITLE, FontFactory.getFont("Helvetica", 12, Font.BOLD, Color.BLACK));
    	fontFamily.addFont(FontContext.SMALL_TEXT, fontSmallBold);
    	fontFamily.addFont(FontContext.TABLE_HEADER, fontMediumBold);
    	fontFamily.addFont(FontContext.TABLE_DATA, fontSmallRegular);
    	fontFamilySelector.addFontFamily(fontFamily);
    	
    	File[] files;
		try {
			File f = new File(exportMPXDAOImpl.class.getClassLoader().getResource("fonts").toURI());
			files = f.listFiles(new FilenameFilter() {				
					@Override
					public boolean accept(File dir, String name) {
						return name.endsWith(".ttf");
					}
				});
		} catch (Exception e1) {
			Logger.getLogger(exportMPXDAOImpl.class.getName()).log(Level.SEVERE, null, e1);
			files = new File[]{};
		}
	for(File file:files){
		try {
				BaseFont bfnt = BaseFont.createFont(file.getAbsolutePath(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
				fontFamily=new FontFamily();
				fontFamily.addFont(FontContext.HEADER_NOTE, new Font(bfnt, 10, Font.BOLD, Color.GRAY));
		    	fontFamily.addFont(FontContext.FOOTER_NOTE, new Font(bfnt, 12, Font.BOLD, Color.GRAY));
		    	fontFamily.addFont(FontContext.LOGO_TEXT, new Font(bfnt, 14, Font.NORMAL, Color.BLACK));
		    	fontFamily.addFont(FontContext.REPORT_TITLE, new Font(bfnt, 20, Font.BOLD, Color.BLACK));
		    	fontFamily.addFont(FontContext.SMALL_TEXT, new Font(bfnt, 12, Font.NORMAL, Color.BLACK));
		    	fontFamily.addFont(FontContext.TABLE_HEADER, new Font(bfnt, 14, Font.BOLD, Color.BLACK));
		    	fontFamily.addFont(FontContext.TABLE_DATA, new Font(bfnt, 12, Font.NORMAL, Color.BLACK));
		    	fontFamilySelector.addFontFamily(fontFamily);
			} catch (Exception e) {
				Logger.getLogger(exportMPXDAOImpl.class.getName()).log(Level.SEVERE, null, e);
			}
	}  	
   
  }
    
    
    
    public ByteArrayOutputStream exportRatioAnalysis(HttpServletRequest request,JSONObject jobj,String logoPath,String comName)  throws DocumentException, ServiceException, IOException {
        ByteArrayOutputStream baos = null;
        double total = 0;
        Document document = null;
        PdfWriter writer = null;
        try {
            //flag 1 = BalanceSheet , 2 = P&L

            baos = new ByteArrayOutputStream();
            document = new Document(PageSize.A4, 15, 15, 15, 15);
            writer = PdfWriter.getInstance(document, baos);
            document.open();
            PdfPTable mainTable = new PdfPTable(1);
            mainTable.setWidthPercentage(100);

            PdfPTable tab1 = null;
            Rectangle page = document.getPageSize();

            int bmargin = 15;  //border margin
            PdfContentByte cb = writer.getDirectContent();
            cb.rectangle(bmargin, bmargin, page.getWidth() - bmargin * 2, page.getHeight() - bmargin * 2);
            cb.setColorStroke(Color.WHITE);
            cb.stroke();

//            addHeaderFooter(document, writer);

            PdfPTable table1 = new PdfPTable(4);
            table1.setWidthPercentage(100);
            table1.setWidths(new float[]{25,25,25,25});


            PdfPCell blankCell = new PdfPCell();
            blankCell.setBorder(0);
            tab1 = addCompanyLogo(logoPath, comName);
            PdfPCell cell1 = new PdfPCell(tab1);
            cell1.setBorder(0);
            table1.addCell(cell1);
            table1.addCell(blankCell);
            table1.addCell(blankCell);
            table1.addCell(blankCell);
            
            PdfPCell cell2 = new PdfPCell(new Paragraph(comName, fontSmallRegular));
            cell2.setBorder(0);
            table1.addCell(cell2);
            table1.addCell(blankCell);
            table1.addCell(blankCell);
            table1.addCell(blankCell);
            
            PdfPCell headerCell =  createCell(messageSource.getMessage("acc.ra.tabTT", null, RequestContextUtils.getLocale(request)), FontContext.TABLE_HEADER, Element.ALIGN_LEFT, 0, 5);
            headerCell.setBorder(0);
            table1.addCell(headerCell);
            table1.addCell(blankCell);
            table1.addCell(blankCell);
            table1.addCell(blankCell);

            

            table1.addCell(blankCell);
            table1.addCell(blankCell);
            table1.addCell(blankCell);
            table1.addCell(blankCell);



            
            JSONArray ObjArr = jobj.getJSONArray("data");
            
            PdfPCell HeaderCell1 = createCell(messageSource.getMessage("acc.ra.principalGroups", null, RequestContextUtils.getLocale(request)), FontContext.SMALL_TEXT, Element.ALIGN_LEFT, 0, 5);
            HeaderCell1.setBorderWidthLeft(1);
            HeaderCell1.setBorderWidthBottom(1);
            HeaderCell1.setBorderWidthTop(1);
            PdfPCell HeaderCell2 = createCell(messageSource.getMessage("acc.ra.value", null, RequestContextUtils.getLocale(request)), FontContext.SMALL_TEXT, Element.ALIGN_RIGHT, 0, 5);
            HeaderCell2.setBorderWidthBottom(1);
            HeaderCell2.setBorderWidthTop(1);
            
            PdfPCell HeaderCell3 = createCell(messageSource.getMessage("acc.ra.principalRatios", null, RequestContextUtils.getLocale(request)), FontContext.SMALL_TEXT, Element.ALIGN_LEFT, 0, 5);
            HeaderCell3.setBorderWidthBottom(1);
            HeaderCell3.setBorderWidthTop(1);
            HeaderCell3.setBorderWidthLeft(1);
            PdfPCell HeaderCell4 = createCell(messageSource.getMessage("acc.ra.value", null, RequestContextUtils.getLocale(request)), FontContext.SMALL_TEXT, Element.ALIGN_RIGHT, 0, 5);
            HeaderCell4.setBorderWidthBottom(1);
            HeaderCell4.setBorderWidthRight(1);
            HeaderCell4.setBorderWidthTop(1);
            table1.addCell(HeaderCell1);
            table1.addCell(HeaderCell2);
            table1.addCell(HeaderCell3);
            table1.addCell(HeaderCell4);
            PdfPCell objCell1 = null;
            PdfPCell objCell2 = null;
            PdfPCell objCell3 = null;
            PdfPCell objCell4 = null;
            int objArrLength = ObjArr.length();
            for(int i=0;i<objArrLength;i++){
                JSONObject leftObj = ObjArr.getJSONObject(i);
                objCell1 = createBalanceSheetCell(leftObj.getString("lname"), FontContext.TABLE_DATA, Element.ALIGN_LEFT, 0, 5,0);
                objCell2 = createBalanceSheetCell(leftObj.getString("lvalue"), FontContext.TABLE_DATA, Element.ALIGN_RIGHT, 0, 0,0);
                objCell3 = createBalanceSheetCell(leftObj.getString("rname"), FontContext.TABLE_DATA, Element.ALIGN_LEFT, 0, 5,0);
                objCell4 = createBalanceSheetCell(leftObj.getString("rvalue"), FontContext.TABLE_DATA, Element.ALIGN_RIGHT, 0, 0,0);
                objCell1.setBorderWidthLeft(1);
                objCell3.setBorderWidthLeft(1);
                objCell4.setBorderWidthRight(1);
                if(i!=(objArrLength-1)){
                    table1.addCell(objCell1);
                    table1.addCell(objCell2);
                    table1.addCell(objCell3);
                    table1.addCell(objCell4);
                }
            }
            objCell1.setBorderWidthBottom(1);
            objCell2.setBorderWidthBottom(1);
            objCell3.setBorderWidthBottom(1);
            objCell4.setBorderWidthBottom(1);
            table1.addCell(objCell1);
            table1.addCell(objCell2);
            table1.addCell(objCell3);
            table1.addCell(objCell4);
            
            PdfPCell mainCell11 = new PdfPCell(table1);
            mainCell11.setBorder(0);
            mainCell11.setPadding(10);
            mainTable.addCell(mainCell11);





            document.add(mainTable);
        } catch (Exception ex) {
            return null;
        } finally {
            if (document != null) {
                document.close();
            }
            if (writer != null) {
                writer.close();
            }
            if (baos != null) {
                baos.close();
            }
        }
           return baos;
    }

    public ByteArrayOutputStream exportBalanceSheetPdf(HttpServletRequest request, String currencyid,DateFormat formatter, String logoPath,String comName,JSONObject jobj,Date startDate,Date endDate,int flag, int toggle) throws DocumentException, ServiceException, IOException {
       ByteArrayOutputStream baos = null;
        double total = 0;
        Document document = null;
        PdfWriter writer = null;
        try {
            //flag 1 = BalanceSheet , 2 = P&L

            String headingString = "";
            String DateheadingString   = "";
            String value = "";
            String subHeading1 = "";
            String subHeading2 = "";
            if(flag==1){
                headingString = messageSource.getMessage("acc.rem.123", null, RequestContextUtils.getLocale(request)); //"Balance Sheet For : ";
                DateheadingString =messageSource.getMessage("acc.rem.124", null, RequestContextUtils.getLocale(request)); //"Balance Sheet Till :";
                value = formatter.format(endDate);
                subHeading1 = messageSource.getMessage("acc.balanceSheet.Amount(asset)", null, RequestContextUtils.getLocale(request));   //"Asset";
                subHeading2 = messageSource.getMessage("acc.balanceSheet.Amount(liability)", null, RequestContextUtils.getLocale(request));   //"Liability";
                if(toggle == 1){
                	subHeading1 = messageSource.getMessage("acc.balanceSheet.Amount(liability)", null, RequestContextUtils.getLocale(request));   //"Liability";
                    subHeading2 = messageSource.getMessage("acc.balanceSheet.Amount(asset)", null, RequestContextUtils.getLocale(request));   //"Asset";
                }
            }else{
                headingString = messageSource.getMessage("acc.rem.125", null, RequestContextUtils.getLocale(request)); // "P&L Statement For : ";
                DateheadingString = messageSource.getMessage("acc.rem.126", null, RequestContextUtils.getLocale(request)); // "P&L Statement From-To :";
                value = formatter.format(startDate)+" "+messageSource.getMessage("acc.common.to", null, RequestContextUtils.getLocale(request))+" "+formatter.format(endDate);
                subHeading1 = messageSource.getMessage("acc.P&L.Amount(Debit)", null, RequestContextUtils.getLocale(request));   //"Debit";
                subHeading2 = messageSource.getMessage("acc.P&L.Amount(Credit)", null, RequestContextUtils.getLocale(request));   //"Credit";
            }
            baos = new ByteArrayOutputStream();
            document = new Document(PageSize.A4, 15, 15, 15, 15);
            writer = PdfWriter.getInstance(document, baos);
            document.open();
            PdfPTable mainTable = new PdfPTable(1);
            mainTable.setWidthPercentage(100);

            PdfPTable tab1 = null;
            Rectangle page = document.getPageSize();

            int bmargin = 15;  //border margin
            PdfContentByte cb = writer.getDirectContent();
            cb.rectangle(bmargin, bmargin, page.getWidth() - bmargin * 2, page.getHeight() - bmargin * 2);
            cb.setColorStroke(Color.WHITE);
            cb.stroke();

//            addHeaderFooter(document, writer);

            PdfPTable table1 = new PdfPTable(3);
            table1.setWidthPercentage(100);
            table1.setWidths(new float[]{30,30,40});

            
            PdfPCell blankCell = new PdfPCell();
            blankCell.setBorder(0);
            tab1 = addCompanyLogo(logoPath, comName);
            PdfPCell cell1 = new PdfPCell(tab1);
            cell1.setBorder(0);
            table1.addCell(cell1);
            table1.addCell(blankCell);
            table1.addCell(blankCell);

            PdfPCell headerCell =  createCell(headingString, FontContext.TABLE_DATA, Element.ALIGN_LEFT, 0, 5);
            headerCell.setBorder(0);
            table1.addCell(headerCell);
            PdfPCell headerNameCell = createCell(comName, FontContext.TABLE_DATA, Element.ALIGN_LEFT, 0, 5);
            headerNameCell.setBorder(0);
            table1.addCell(headerNameCell);
            table1.addCell(blankCell);

            headerCell =  createCell(DateheadingString, FontContext.TABLE_DATA, Element.ALIGN_LEFT, 0, 5);
            headerCell.setBorder(0);
            table1.addCell(headerCell);
            headerNameCell = createCell(value, FontContext.TABLE_DATA, Element.ALIGN_LEFT, 0, 5);
            headerNameCell.setBorder(0);
            table1.addCell(headerNameCell);
            table1.addCell(blankCell);

            table1.addCell(blankCell);
            table1.addCell(blankCell);
            table1.addCell(blankCell);

            

            jobj = jobj.getJSONObject("data");
            JSONArray rightObjArr = flag==1?jobj.getJSONArray("right"):jobj.getJSONArray("left");
            JSONArray leftObjArr = flag==1?jobj.getJSONArray("left"):jobj.getJSONArray("right");
            if(toggle == 1){
            	rightObjArr = jobj.getJSONArray("left");
                leftObjArr = jobj.getJSONArray("right");
            }
            JSONArray finalValArr = jobj.getJSONArray("total");
            PdfPCell HeaderCell1 = createCell(messageSource.getMessage("acc.P&L.particulars", null, RequestContextUtils.getLocale(request)), FontContext.TABLE_DATA, Element.ALIGN_LEFT, 0, 5);
            HeaderCell1.setBorderWidthBottom(1);
            PdfPCell HeaderCell2 = createCell(subHeading1, FontContext.TABLE_DATA, Element.ALIGN_LEFT, 0, 5);
            HeaderCell2.setBorderWidthBottom(1);
            table1.addCell(HeaderCell1);
            table1.addCell(HeaderCell2);
            table1.addCell(blankCell);
            double totalAsset = Double.parseDouble(finalValArr.getString(0));
            double totalLibility = Double.parseDouble(finalValArr.getString(1));
            for(int i=0;i<rightObjArr.length();i++){
                JSONObject leftObj = rightObjArr.getJSONObject(i);
                addBalanceSheetCell(leftObj,table1,currencyid);
//                PdfPCell cell3 = createCell(leftObj.get("accountname").toString(), fontSmallBold, Element.ALIGN_LEFT, 0, 5);
//                PdfPCell cell4 = createCell(com.krawler.common.util.StringUtil.serverHTMLStripper(leftObj.get("amount").toString()), fontSmallBold, Element.ALIGN_LEFT, 0, 5);
//                cell3.setBorder(0);
//                table1.addCell(cell3);
//                cell4.setBorder(0);
//                table1.addCell(cell4);
            }
            PdfPCell totalAsscell = createBalanceSheetCell(messageSource.getMessage("acc.common.total", null, RequestContextUtils.getLocale(request))+" "+subHeading1, FontContext.TABLE_DATA, Element.ALIGN_LEFT, 0, 0,0);
            table1.addCell(totalAsscell);
            PdfPCell totalAssValcell = createBalanceSheetCell(exportDaoObj.currencyRender(Double.toString(totalAsset), currencyid), fontSmallBold, Element.ALIGN_RIGHT, 0, 0,0);
            table1.addCell(totalAssValcell);
            for(int i = 0; i < 16; i++)
            	table1.addCell(blankCell);
            
            HeaderCell1 = createCell(messageSource.getMessage("acc.balanceSheet.particulars", null, RequestContextUtils.getLocale(request)), FontContext.TABLE_DATA, Element.ALIGN_LEFT, 0, 5);
            HeaderCell1.setBorderWidthBottom(1);
            HeaderCell2 = createCell(subHeading2, FontContext.TABLE_DATA, Element.ALIGN_LEFT, 0, 5);
            HeaderCell2.setBorderWidthBottom(1);
            table1.addCell(HeaderCell1);
            table1.addCell(HeaderCell2);
            table1.addCell(blankCell);

            for(int i=0;i< leftObjArr.length();i++){
                JSONObject leftObj = leftObjArr.getJSONObject(i);
                 addBalanceSheetCell(leftObj,table1,currencyid);
//                PdfPCell cell3 = createCell(leftObj.get("accountname").toString(), fontSmallBold, Element.ALIGN_LEFT, 0, 5);
//                PdfPCell cell4 = createCell(com.krawler.common.util.StringUtil.serverHTMLStripper(leftObj.get("amount").toString()), fontSmallBold, Element.ALIGN_LEFT, 0, 5);
//                cell3.setBorder(0);
//                table1.addCell(cell3);
//                cell4.setBorder(0);
//                table1.addCell(cell4);
            }
            PdfPCell totalLibcell = createBalanceSheetCell(messageSource.getMessage("acc.common.total", null, RequestContextUtils.getLocale(request))+" "+subHeading2, FontContext.TABLE_DATA, Element.ALIGN_LEFT, 0, 0,0);
            table1.addCell(totalLibcell);
            PdfPCell totalLibValcell = createBalanceSheetCell(exportDaoObj.currencyRender(Double.toString(totalLibility), currencyid), FontContext.TABLE_DATA, Element.ALIGN_RIGHT, 0, 0,0);
            table1.addCell(totalLibValcell);
            table1.addCell(blankCell);
            




            PdfPCell mainCell11 = new PdfPCell(table1);
            mainCell11.setBorder(0);
            mainCell11.setPadding(10);
            mainTable.addCell(mainCell11);




//            document.add(mainTable);
            document.add(mainTable);
        } catch (Exception ex) {
            return null;
        } finally {
            if (document != null) {
                document.close();
            }
            if (writer != null) {
                writer.close();
            }
            if (baos != null) {
                baos.close();
            }
        }
        return baos;
    }

    public Double addBalanceSheetCell(JSONObject jobj,PdfPTable table,String currencyid) throws JSONException, SessionExpiredException{
        String val = "";
        double retnum = 0 ;
        PdfPCell cell3 = null;
        PdfPCell cell4 = null;
        PdfPCell cell5 = createBalanceSheetCell(val, FontContext.SMALL_TEXT, Element.ALIGN_RIGHT, 0, 0,0);
        if(!jobj.toString().equalsIgnoreCase("{}")){
            String accName = jobj.get("accountname").toString();
            double amount = 0;
            int padding = Integer.parseInt(jobj.get("level").toString())*10;
            try{
                amount = Double.parseDouble(jobj.get("amount").toString());
                val = exportDaoObj.currencyRender(Double.toString(amount), currencyid);
                 java.text.DecimalFormat obj = new java.text.DecimalFormat("###0.00");
                 if(padding == 0 &&  !accName.equals("")){
                    retnum = Double.parseDouble(obj.format(amount));
                 }
            }catch(NumberFormatException ex){
                val = com.krawler.common.util.StringUtil.serverHTMLStripper(jobj.get("amount").toString());
            }
            if(jobj.has("fmt")){
                cell3 = createBalanceSheetCell(accName, FontContext.TABLE_HEADER, Element.ALIGN_LEFT, 0, padding,0);
                cell4 = createBalanceSheetCell(val, FontContext.TABLE_HEADER, Element.ALIGN_RIGHT, 0, 0,0);
            }else if(padding == 0 &&  !accName.equals("")){
                cell3 = createBalanceSheetCell(accName, FontContext.SMALL_TEXT, Element.ALIGN_LEFT, 0, padding,0);
                cell4 = createBalanceSheetCell(val, FontContext.SMALL_TEXT, Element.ALIGN_RIGHT, 0, 0,0);
                if(!val.equals("")){
                    cell3.setBorderWidthBottom(1);
                    cell3.setBorderColor(Color.GRAY);
                    cell4.setBorderWidthBottom(1);
                    cell4.setBorderColor(Color.gray);
                }
            }else{
                cell3 = createBalanceSheetCell(accName, FontContext.TABLE_DATA, Element.ALIGN_LEFT, 0, padding,0);
                cell4 = createBalanceSheetCell(val, FontContext.TABLE_DATA, Element.ALIGN_RIGHT, 0, 0,0);
            }
            table.addCell(cell3);
            table.addCell(cell4);
            table.addCell(cell5);
        }/*else{
            cell3 = createBalanceSheetCell(val, fontSmallBold, Element.ALIGN_RIGHT, 0, 0,0);
            cell4 = createBalanceSheetCell(val, fontSmallBold, Element.ALIGN_RIGHT, 0, 0,0);
        }*/

        
        return  retnum;

    }

    private PdfPCell createBalanceSheetCell(String string, Font fontTbl, int ALIGN_RIGHT, int i, int paddLeft,int paddRight) {
        PdfPCell cell = new PdfPCell(new Paragraph(string, fontTbl));
        cell.setHorizontalAlignment(ALIGN_RIGHT);
        cell.setBorder(i);
        cell.setPaddingLeft(paddLeft);
        if(paddRight!=0){
            cell.setPaddingRight(paddRight);
        }
        return cell;
    }
    
    private PdfPCell createBalanceSheetCell(String string, FontContext context,int ALIGN_RIGHT, int i, int paddLeft,int paddRight) {
        PdfPCell cell = new PdfPCell(new Paragraph(fontFamilySelector.process(string, context)));
        cell.setHorizontalAlignment(ALIGN_RIGHT);
        cell.setBorder(i);
        cell.setPaddingLeft(paddLeft);
        if(paddRight!=0){
            cell.setPaddingRight(paddRight);
        }
        return cell;
    }

    public ByteArrayOutputStream createPdf(HttpServletRequest request, String currencyid, String billid, DateFormat formatter, int mode, double amount, String logoPath, String customer, String accname, String address,boolean isExpenseInv) throws DocumentException, ServiceException, IOException {
        ByteArrayOutputStream baos = null;
        double total = 0;
        Document document = null;
        PdfWriter writer = null;
        try {
            String poRefno ="";
            baos = new ByteArrayOutputStream();
            document = new Document(PageSize.A4, 15, 15, 15, 15);
            writer = PdfWriter.getInstance(document, baos);
            document.open();
            PdfPTable mainTable = new PdfPTable(1);
            mainTable.setWidthPercentage(100);

            PdfPTable blankTable = null;
            PdfPCell blankCell = null;
            PdfPTable tab1 = null;
            PdfPTable tab2 = null;
            PdfPTable tab3 = null;
            Rectangle page = document.getPageSize();

            int bmargin = 15;  //border margin
            PdfContentByte cb = writer.getDirectContent();
            cb.rectangle(bmargin, bmargin, page.getWidth() - bmargin * 2, page.getHeight() - bmargin * 2);
            cb.setColorStroke(Color.WHITE);
            cb.stroke();

            if (mode == StaticValues.AUTONUM_INVOICE || mode == StaticValues.AUTONUM_BILLINGINVOICE||mode == StaticValues.AUTONUM_BILLINGSALESORDER||mode==StaticValues.AUTONUM_BILLINGPURCHASEORDER||mode==StaticValues.AUTONUM_SALESORDER||mode==StaticValues.AUTONUM_PURCHASEORDER||mode==StaticValues.AUTONUM_QUOTATION) {
                addHeaderFooter(document, writer);
                Invoice inv = null;
                BillingInvoice inv1 = null;
                BillingSalesOrder so = null;
                Company com  = null;
                Account cEntry;
                String invno = "";
                Date entryDate = null;
                BillingPurchaseOrder po=null;
                SalesOrder sOrder = null;
                PurchaseOrder pOrder = null;
                Tax mainTax = null;
                Quotation quotation = null;
                if (mode == StaticValues.AUTONUM_INVOICE) {
                    KwlReturnObject cap = kwlCommonTablesDAOObj.getObject(Invoice.class.getName(), billid);
                    inv = (Invoice) cap.getEntityList().get(0);
                    com = inv.getCompany();
                    cEntry = inv.getCustomerEntry().getAccount();
                    invno = inv.getInvoiceNumber();
                    entryDate = inv.getJournalEntry().getEntryDate();
                //inv = (Invoice) session.get(Invoice.class, billid);
                }else if(mode == StaticValues.AUTONUM_BILLINGSALESORDER){
                    so = (BillingSalesOrder) kwlCommonTablesDAOObj.getClassObject(BillingSalesOrder.class.getName(), billid);
                    com = so.getCompany();
                    cEntry = so.getCustomer().getAccount();
                    invno = so.getSalesOrderNumber();
                    entryDate = so.getOrderDate();
                    mainTax = so.getTax();
                } else if(mode==StaticValues.AUTONUM_BILLINGPURCHASEORDER){
                    po = (BillingPurchaseOrder) kwlCommonTablesDAOObj.getClassObject(BillingPurchaseOrder.class.getName(), billid);
                    com = po.getCompany();
                    cEntry = po.getVendor().getAccount();
                    invno = po.getPurchaseOrderNumber();
                    entryDate = po.getOrderDate();
                    mainTax = po.getTax();
                }else if(mode==StaticValues.AUTONUM_SALESORDER){
                    sOrder = (SalesOrder) kwlCommonTablesDAOObj.getClassObject(SalesOrder.class.getName(), billid);
                    com = sOrder.getCompany();
                    cEntry = sOrder.getCustomer().getAccount();
                    invno = sOrder.getSalesOrderNumber();
                    entryDate = sOrder.getOrderDate();
                    mainTax = sOrder.getTax();

                }else if(mode==StaticValues.AUTONUM_PURCHASEORDER){
                    pOrder = (PurchaseOrder) kwlCommonTablesDAOObj.getClassObject(PurchaseOrder.class.getName(), billid);
                    com = pOrder.getCompany();
                    cEntry = pOrder.getVendor().getAccount();
                    invno = pOrder.getPurchaseOrderNumber();
                    entryDate = pOrder.getOrderDate();
                    mainTax = pOrder.getTax();
                    
                }else if(mode==StaticValues.AUTONUM_QUOTATION){
                	quotation = (Quotation) kwlCommonTablesDAOObj.getClassObject(Quotation.class.getName(), billid);
                    com = quotation.getCompany();
                    cEntry = quotation.getCustomer().getAccount();
                    invno = quotation.getquotationNumber();
                    entryDate = quotation.getQuotationDate();
                    mainTax = quotation.getTax();

                }else {
                    KwlReturnObject cap = kwlCommonTablesDAOObj.getObject(BillingInvoice.class.getName(), billid);
                    inv1 = (BillingInvoice) cap.getEntityList().get(0);
                    com = inv1.getCompany();
                    cEntry = inv1.getCustomerEntry().getAccount();
                    invno = inv1.getBillingInvoiceNumber();
                    entryDate = inv1.getJournalEntry().getEntryDate();
                    poRefno = inv1.getPoRefNumber()==null?"":inv1.getPoRefNumber();
                    mainTax = inv1.getTax();
                //inv1=(BillingInvoice)session.get(BillingInvoice.class,billid);
                }


//                Company com = mode != StaticValues.AUTONUM_BILLINGINVOICE ? inv.getCompany() : inv1.getCompany();
                String company[] = new String[4];
                company[0] = com.getCompanyName();
                company[1] = com.getAddress();
                company[2] = com.getEmailID();
                company[3] = com.getPhoneNumber();

                PdfPTable table1 = new PdfPTable(2);
                table1.setWidthPercentage(100);
                table1.setWidths(new float[]{50, 50});

                tab1 = addCompanyLogo(logoPath, com);
                tab2 = new PdfPTable(1);
                PdfPCell invCell = null;

                CompanyAccountPreferences pref = (CompanyAccountPreferences) kwlCommonTablesDAOObj.getClassObject(CompanyAccountPreferences.class.getName(), com.getCompanyID());
                Account cash = pref.getCashAccount();
//
//                if (mode != StaticValues.AUTONUM_BILLINGINVOICE) {
//                    cEntry = inv.getCustomerEntry().getAccount();
//                } else {
//                    cEntry = inv1.getCustomerEntry().getAccount();
//                }
                String theader = cEntry==cash?messageSource.getMessage("acc.accPref.autoCS", null, RequestContextUtils.getLocale(request)):messageSource.getMessage("acc.accPref.autoInvoice", null, RequestContextUtils.getLocale(request));
                if(mode == StaticValues.AUTONUM_BILLINGSALESORDER||mode==StaticValues.AUTONUM_SALESORDER){
                    theader = messageSource.getMessage("acc.accPref.autoSO", null, RequestContextUtils.getLocale(request));
                }else if(mode == StaticValues.AUTONUM_BILLINGPURCHASEORDER||mode==StaticValues.AUTONUM_PURCHASEORDER){
                     theader = messageSource.getMessage("acc.accPref.autoPO", null, RequestContextUtils.getLocale(request));
                }else if(mode == StaticValues.AUTONUM_QUOTATION){
                    theader = messageSource.getMessage("acc.accPref.autoQN", null, RequestContextUtils.getLocale(request));
                }
                invCell=createCell(theader,fontTbl,Element.ALIGN_RIGHT,0,5);
                tab2.addCell(invCell);

                PdfPCell cell1 = new PdfPCell(tab1);
                cell1.setBorder(0);
                table1.addCell(cell1);
                PdfPCell cel2 = new PdfPCell(tab2);
                cel2.setBorder(0);
                table1.addCell(cel2);

                PdfPCell mainCell11 = new PdfPCell(table1);
                mainCell11.setBorder(0);
                mainCell11.setPadding(10);
                mainTable.addCell(mainCell11);


                PdfPTable userTable2 = new PdfPTable(2);
                userTable2.setWidthPercentage(100);
                userTable2.setWidths(new float[]{60, 40});

                tab3 = getCompanyInfo(company);

                PdfPTable tab4 = new PdfPTable(2);
                tab4.setWidthPercentage(100);
                tab4.setWidths(new float[]{50, 50});

                PdfPCell cell2=createCell(theader+"# :",fontSmallBold,Element.ALIGN_RIGHT,0,5);
                if(mode == StaticValues.AUTONUM_QUOTATION){
                    cell2=createCell(theader+"# :",fontSmallBold,Element.ALIGN_RIGHT,0,5);
                }
                tab4.addCell(cell2);
//                String invno = mode != StaticValues.AUTONUM_BILLINGINVOICE ? inv.getInvoiceNumber() : inv1.getBillingInvoiceNumber();
                cell2 = createCell(invno, fontSmallRegular, Element.ALIGN_LEFT, 0, 5);
                tab4.addCell(cell2);
                if(mode != StaticValues.AUTONUM_QUOTATION && mode != StaticValues.AUTONUM_PURCHASEORDER && mode != StaticValues.AUTONUM_SALESORDER){
                	cell2 = createCell(messageSource.getMessage("acc.numb.43", null, RequestContextUtils.getLocale(request))+" # :", fontSmallBold, Element.ALIGN_RIGHT, 0, 5);
                	tab4.addCell(cell2);
                	cell2 = createCell(poRefno, fontSmallRegular, Element.ALIGN_LEFT, 0, 5);
                	tab4.addCell(cell2);
                }	
                	
                cell2 = createCell(messageSource.getMessage("acc.rem.198", null, RequestContextUtils.getLocale(request))+" :", fontSmallBold, Element.ALIGN_RIGHT, 0, 5);
                tab4.addCell(cell2);
                cell2 = createCell(formatter.format(entryDate), fontSmallRegular, Element.ALIGN_LEFT, 0, 5);
                tab4.addCell(cell2);

                cell1 = new PdfPCell(tab3);
                cell1.setBorder(0);
                userTable2.addCell(cell1);
                cel2 = new PdfPCell(tab4);
                cel2.setBorder(0);
                userTable2.addCell(cel2);

                PdfPCell mainCell12 = new PdfPCell(userTable2);
                mainCell12.setBorder(0);
                mainCell12.setPadding(10);
                mainTable.addCell(mainCell12);


                PdfPTable tab5 = new PdfPTable(2);
                tab5.setWidthPercentage(100);
                tab5.setWidths(new float[]{10, 90});
                PdfPCell cell3 = createCell(messageSource.getMessage("acc.common.to", null, RequestContextUtils.getLocale(request))+" , ", fontSmallBold, Element.ALIGN_LEFT, 0, 5);
                tab5.addCell(cell3);
                cell3 = createCell("", fontSmallBold, Element.ALIGN_LEFT, 0, 0);
                tab5.addCell(cell3);
                cell3 = createCell("", fontSmallBold, Element.ALIGN_LEFT, 0, 0);
                tab5.addCell(cell3);

                HashMap<String, Object> invRequestParams = new HashMap<String, Object>();
                ArrayList filter_names = new ArrayList(),filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
                order_by.add("srno");
                order_type.add("asc");
                invRequestParams.put("order_by", order_by);
                invRequestParams.put("order_type", order_type);
                KwlReturnObject idresult = null;

                String customerName = "";
                String shipTo = "";
                String memo = "";
                Iterator itr = null;
                if(mode==StaticValues.AUTONUM_INVOICE){
                    filter_names.add("invoice.ID");
                    filter_params.add(inv.getID());
                    invRequestParams.put("filter_names", filter_names);
                    invRequestParams.put("filter_params", filter_params);
                    idresult = accInvoiceDAOobj.getInvoiceDetails(invRequestParams);
                    customerName = inv.getCustomer()==null?inv.getCustomerEntry().getAccount().getName():inv.getCustomer().getName();
                    shipTo = inv.getShipTo();
                    itr = idresult.getEntityList().iterator();
                    memo =inv.getMemo();
                } else if(mode == StaticValues.AUTONUM_BILLINGSALESORDER){
                    customerName = so.getCustomer().getName();
                    shipTo = so.getCustomer().getShippingAddress();
                    filter_names.add("salesOrder.ID");
                    filter_params.add(so.getID());
                    invRequestParams.put("filter_names", filter_names);
                    invRequestParams.put("filter_params", filter_params);
                    idresult = accSalesOrderDAOobj.getBillingSalesOrderDetails(invRequestParams);
                    itr = idresult.getEntityList().iterator();
                    memo = so.getMemo();
                } else if(mode == StaticValues.AUTONUM_BILLINGPURCHASEORDER){
                    customerName = po.getVendor().getName();
                    shipTo = po.getVendor().getAddress();
                    filter_names.add("purchaseOrder.ID");
                    filter_params.add(po.getID());
                    invRequestParams.put("filter_names", filter_names);
                    invRequestParams.put("filter_params", filter_params);
                    idresult = accPurchaseOrderobj.getBillingPurchaseOrderDetails(invRequestParams);
                    itr = idresult.getEntityList().iterator();
                    memo = po.getMemo();
                }else if(mode==StaticValues.AUTONUM_SALESORDER){
                    customerName = sOrder.getCustomer().getName();
                    shipTo = sOrder.getCustomer().getShippingAddress();
                    filter_names.add("salesOrder.ID");
                    filter_params.add(sOrder.getID());
                    invRequestParams.put("filter_names", filter_names);
                    invRequestParams.put("filter_params", filter_params);
                    idresult = accSalesOrderDAOobj.getSalesOrderDetails(invRequestParams);
                    itr = idresult.getEntityList().iterator();
                    memo = sOrder.getMemo();
                }else if(mode==StaticValues.AUTONUM_PURCHASEORDER){
                    customerName = pOrder.getVendor().getName();
                    shipTo = pOrder.getVendor().getAddress();
                    filter_names.add("purchaseOrder.ID");
                    filter_params.add(pOrder.getID());
                    invRequestParams.put("filter_names", filter_names);
                    invRequestParams.put("filter_params", filter_params);
                    idresult = accPurchaseOrderobj.getPurchaseOrderDetails(invRequestParams);
                    itr = idresult.getEntityList().iterator();
                    memo = pOrder.getMemo();
                }else if(mode==StaticValues.AUTONUM_QUOTATION){
                    customerName = quotation.getCustomer().getName();
                    shipTo = quotation.getCustomer().getShippingAddress();
                    filter_names.add("quotation.ID");
                    filter_params.add(quotation.getID());
                    invRequestParams.put("filter_names", filter_names);
                    invRequestParams.put("filter_params", filter_params);
                    idresult = accSalesOrderDAOobj.getQuotationDetails(invRequestParams);
                    itr = idresult.getEntityList().iterator();
                    memo = quotation.getMemo();
                } else {
                    filter_names.add("billingInvoice.ID");
                    filter_params.add(inv1.getID());
                    invRequestParams.put("filter_names", filter_names);
                    invRequestParams.put("filter_params", filter_params);
                    idresult = accInvoiceDAOobj.getBillingInvoiceDetails(invRequestParams);
                    customerName = inv1.getCustomer()==null?inv1.getCustomerEntry().getAccount().getName():inv1.getCustomer().getName();
                    shipTo = inv1.getShipTo();
                    itr = idresult.getEntityList().iterator();
                    memo = inv1.getMemo();
                }
                cell3=createCell(customerName, fontSmallRegular,Element.ALIGN_LEFT,0,5);
                tab5.addCell(cell3);
                cell3 = createCell("", fontSmallBold, Element.ALIGN_LEFT, 0, 0);
                tab5.addCell(cell3);
                cell3 = createCell(shipTo, fontSmallRegular, Element.ALIGN_LEFT, 0, 5);
                tab5.addCell(cell3);

                PdfPCell mainCell14 = new PdfPCell(tab5);
                mainCell14.setBorder(0);
                mainCell14.setPadding(10);
                mainTable.addCell(mainCell14);

//                if(mode == StaticValues.AUTONUM_QUOTATION)
//                	String[] header = {"S.No.","PRODUCT DESCRIPTION", "QUANTITY", "UNIT PRICE", "TAX", "AMOUNT"};
                
                	String[] header = {messageSource.getMessage("acc.setupWizard.sno", null, RequestContextUtils.getLocale(request)),messageSource.getMessage("acc.rem.176", null, RequestContextUtils.getLocale(request)), messageSource.getMessage("acc.rem.187", null, RequestContextUtils.getLocale(request)), messageSource.getMessage("acc.rem.188", null, RequestContextUtils.getLocale(request)), messageSource.getMessage("acc.rem.191", null, RequestContextUtils.getLocale(request)),messageSource.getMessage("acc.rem.192", null, RequestContextUtils.getLocale(request)), messageSource.getMessage("acc.rem.193", null, RequestContextUtils.getLocale(request))};
                PdfPTable table = getBlankTable();
                PdfPCell invcell = null;
                for (int i = 0; i < header.length; i++) {
                    invcell = new PdfPCell(new Paragraph(header[i], fontSmallBold));
                    invcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    invcell.setBackgroundColor(Color.LIGHT_GRAY);
                    invCell.setBorder(0);
                    invcell.setPadding(3);
                    table.addCell(invcell);
                }

                addTableRow(mainTable, table); //Break table after adding header row
                table = getBlankTable();
                
                HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
                KwlReturnObject bAmt = null;
                InvoiceDetail row = null;
                BillingInvoiceDetail row1 = null;
                BillingSalesOrderDetail row3 = null;
                BillingPurchaseOrderDetail row4 = null;
                SalesOrderDetail row5 = null;
                PurchaseOrderDetail row6 = null;
                QuotationDetail row7 = null;
                int index=0;
                while (itr.hasNext()) {
                    String prodName = "";
                    double quantity = 0, discountQuotation = 0;
                    double rate = 0;
                    Discount discount = null;
                    String uom = "";
                    double amount1 = 0;
                    if (mode == StaticValues.AUTONUM_INVOICE) {
                        row = (InvoiceDetail) itr.next();
                        prodName = row.getInventory().getProduct().getName();
                        quantity =  row.getInventory().getQuantity();
                        rate = row.getRate() ;
                        discount = row.getDiscount();
                        uom = row.getInventory().getProduct().getUnitOfMeasure()==null?"":row.getInventory().getProduct().getUnitOfMeasure().getName();
                    }  else if(mode == StaticValues.AUTONUM_BILLINGSALESORDER){
                        row3 = (BillingSalesOrderDetail) itr.next();
                        prodName = row3.getProductDetail();
                        quantity =  row3.getQuantity();
                        rate = row3.getRate() ;
                    }else if(mode == StaticValues.AUTONUM_BILLINGPURCHASEORDER){
                        row4 = (BillingPurchaseOrderDetail) itr.next();
                        prodName = row4.getProductDetail();
                        quantity = row4.getQuantity();
                        rate = row4.getRate();
                    } else if(mode==StaticValues.AUTONUM_SALESORDER){
                        row5 = (SalesOrderDetail) itr.next();
                        prodName = row5.getProduct().getName();
                        quantity =  row5.getQuantity();
                        rate = row5.getRate() ;
                        uom = row5.getProduct().getUnitOfMeasure()==null?"":row5.getProduct().getUnitOfMeasure().getName();
                    }else if(mode==StaticValues.AUTONUM_PURCHASEORDER){
                        row6 = (PurchaseOrderDetail) itr.next();
                        prodName = row6.getProduct().getName();
                        quantity = row6.getQuantity();
                        rate = row6.getRate();
                        uom = row6.getProduct().getUnitOfMeasure()==null?"":row6.getProduct().getUnitOfMeasure().getName();
                    }else if(mode==StaticValues.AUTONUM_QUOTATION){
                        row7 = (QuotationDetail) itr.next();
                        prodName = row7.getProduct().getName();
                        quantity =  row7.getQuantity();
                        rate = row7.getRate() ;
                        discountQuotation = rate*quantity *row7.getDiscount()/100;
                        uom = row7.getProduct().getUnitOfMeasure()==null?"":row7.getProduct().getUnitOfMeasure().getName();
                    }else {
                        row1 = (BillingInvoiceDetail) itr.next();
                        prodName = row1.getProductDetail();
                        quantity =  row1.getQuantity();
                        rate = row1.getRate() ;
                        discount = row1.getDiscount()!=null?row1.getDiscount():null;
//                        uom = row1.getInventory().getProduct().getUnitOfMeasure()==null?"":row1.getInventory().getProduct().getUnitOfMeasure().getName();
                    }
                    invcell = createCell((++index)+".", fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                    table.addCell(invcell);
                    invcell = createCell(prodName, fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                    table.addCell(invcell);

                    String qtyStr = Double.toString(quantity);
                    if (mode == StaticValues.AUTONUM_INVOICE || mode==StaticValues.AUTONUM_SALESORDER || mode==StaticValues.AUTONUM_PURCHASEORDER || mode==StaticValues.AUTONUM_QUOTATION) {
                        qtyStr = Integer.toString((int)quantity); //For with-Inventory flow, Don't show decimal point as inventory has integer value
                    }
                    invcell = createCell(qtyStr+" "+uom, fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
                    table.addCell(invcell);
                    if( mode==StaticValues.AUTONUM_PURCHASEORDER || mode==StaticValues.AUTONUM_SALESORDER || mode==StaticValues.AUTONUM_QUOTATION){
                        bAmt = accCurrencyobj.getBaseToCurrencyAmount(requestParams, rate, cEntry.getCurrency().getCurrencyID(), entryDate, 0);
                        double rateInBase = (Double) bAmt.getEntityList().get(0);
                       rate = rateInBase;
                    }
                    invcell = createCell(authHandlerDAOObj.getFormattedCurrency(rate, currencyid), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                    table.addCell(invcell);
                    if(mode==StaticValues.AUTONUM_QUOTATION)
                    	invcell = calculateDiscount(discountQuotation, currencyid);
                    else
                    	invcell = calculateDiscount(discount, currencyid);
                    invcell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    invcell.setPadding(5);
                    invcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
                    table.addCell(invcell);

                    amount1 = rate*quantity;
                    if (discount != null) {
                        amount1 -= mode != StaticValues.AUTONUM_BILLINGINVOICE ? (row.getDiscount().getDiscountValue()) : (row1.getDiscount().getDiscountValue());
                    }
                    if (discountQuotation != 0){
                    	amount1 -= discountQuotation; 
                    }
                    double rowTaxPercent=0;
                    if (row!= null&&row.getTax() != null) {
                        requestParams.put("transactiondate", entryDate);
                        requestParams.put("taxid", row.getTax().getID());
                        KwlReturnObject result = accTaxObj.getTax(requestParams);
                        List taxList = result.getEntityList();
                        Object[] taxObj=(Object[]) taxList.get(0);
                        rowTaxPercent=taxObj[1]==null?0:(Double) taxObj[1];
                    }
                    else if (row1!= null&&row1.getTax() != null) {
                        requestParams.put("transactiondate", entryDate);
                        requestParams.put("taxid", row1.getTax().getID());
                        KwlReturnObject result = accTaxObj.getTax(requestParams);
                        List taxList = result.getEntityList();
                        Object[] taxObj=(Object[]) taxList.get(0);
                        rowTaxPercent=taxObj[1]==null?0:(Double) taxObj[1];
                    }
                    else if (row3!= null&&row3.getTax() != null) {
                        requestParams.put("transactiondate", entryDate);
                        requestParams.put("taxid", row3.getTax().getID());
                        KwlReturnObject result = accTaxObj.getTax(requestParams);
                        List taxList = result.getEntityList();
                        Object[] taxObj=(Object[]) taxList.get(0);
                        rowTaxPercent=taxObj[1]==null?0:(Double) taxObj[1];
                    }  else if (row4!= null&&row4.getTax() != null) {
                        requestParams.put("transactiondate", entryDate);
                        requestParams.put("taxid", row4.getTax().getID());
                        KwlReturnObject result = accTaxObj.getTax(requestParams);
                        List taxList = result.getEntityList();
                        Object[] taxObj=(Object[]) taxList.get(0);
                        rowTaxPercent=taxObj[1]==null?0:(Double) taxObj[1];
                    }  else if (row5!= null&&row5.getTax() != null) {
                        requestParams.put("transactiondate", entryDate);
                        requestParams.put("taxid", row5.getTax().getID());
                        KwlReturnObject result = accTaxObj.getTax(requestParams);
                        List taxList = result.getEntityList();
                        Object[] taxObj=(Object[]) taxList.get(0);
                        rowTaxPercent=taxObj[1]==null?0:(Double) taxObj[1];
                    }  else if (row6!= null&&row6.getTax() != null) {
                        requestParams.put("transactiondate", entryDate);
                        requestParams.put("taxid", row6.getTax().getID());
                        KwlReturnObject result = accTaxObj.getTax(requestParams);
                        List taxList = result.getEntityList();
                        Object[] taxObj=(Object[]) taxList.get(0);
                        rowTaxPercent=taxObj[1]==null?0:(Double) taxObj[1];
                    }  else if (row7!= null&&row7.getTax() != null){
                    	requestParams.put("transactiondate", entryDate);
                        requestParams.put("taxid", row7.getTax().getID());
                        KwlReturnObject result = accTaxObj.getTax(requestParams);
                        List taxList = result.getEntityList();
                        Object[] taxObj=(Object[]) taxList.get(0);
                        rowTaxPercent=taxObj[1]==null?0:(Double) taxObj[1];
                    }
                    invcell = createCell(authHandlerDAOObj.getFormattedCurrency(amount1*rowTaxPercent/100, currencyid), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                    table.addCell(invcell);
                    amount1+=amount1*rowTaxPercent/100;
                    invcell = createCell(authHandlerDAOObj.getFormattedCurrency(amount1, currencyid), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                    table.addCell(invcell);
                    total += amount1;

                    addTableRow(mainTable, table); //Break table after adding detail's row
                    table = getBlankTable();
                }
                for (int j = 0; j < 98; j++) {
                    invcell = new PdfPCell(new Paragraph("", fontSmallRegular));
                    invcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    invcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
                    table.addCell(invcell);
                }
                addTableRow(mainTable, table); //Break table after adding extra space
                table = getBlankTable();
//                for (int i = 0; i < 5; i++) {
//                    invcell = new PdfPCell(new Paragraph("", fontSmallRegular));
//                    invcell.setHorizontalAlignment(Element.ALIGN_CENTER);
//                    invcell.setBorder(Rectangle.TOP);
//                    table.addCell(invcell);
//                }
                cell3 = createCell(messageSource.getMessage("acc.rem.194", null, RequestContextUtils.getLocale(request)), fontSmallBold, Element.ALIGN_RIGHT, Rectangle.TOP, 5);
                cell3.setColspan(6);
                table.addCell(cell3);
//                if(mode == StaticValues.AUTONUM_INVOICE || mode==StaticValues.AUTONUM_PURCHASEORDER || mode==StaticValues.AUTONUM_SALESORDER){
//                    bAmt = accCurrencyobj.getBaseToCurrencyAmount(requestParams, total, cEntry.getCurrency().getCurrencyID(), entryDate, 0);
//                    double baseTotalAmount = (Double) bAmt.getEntityList().get(0);
//                    total = baseTotalAmount;
//                }
                cell3 = createCell(authHandlerDAOObj.getFormattedCurrency(total, currencyid), fontSmallRegular, Element.ALIGN_RIGHT, 15, 5);
                table.addCell(cell3);
//                for (int i = 0; i < 5; i++) {
//                    invcell = new PdfPCell(new Paragraph("", fontSmallRegular));
//                    invcell.setHorizontalAlignment(Element.ALIGN_CENTER);
//                    invcell.setPadding(5);
//                    invcell.setBorder(0);
//                    table.addCell(invcell);
//                }
                Discount totalDiscount = null;
                double totaltax = 0, discountTotalQuotation = 0;
                double totalAmount = 0;
                double taxPercent = 0;
                if(mainTax!=null){ //Get tax percent
                    requestParams.put("transactiondate", entryDate);
                    requestParams.put("taxid", mainTax.getID());
                    KwlReturnObject result = accTaxObj.getTax(requestParams);
                    List taxList = result.getEntityList();
                    Object[] taxObj=(Object[]) taxList.get(0);
                    taxPercent =taxObj[1]==null?0:(Double) taxObj[1];
                }

                if(mode==StaticValues.AUTONUM_INVOICE){
                    totalDiscount = inv.getDiscount();
                    totaltax = inv.getTaxEntry() != null ? inv.getTaxEntry().getAmount() : 0;
                    totalAmount = inv.getCustomerEntry().getAmount();
                } else if(mode == StaticValues.AUTONUM_BILLINGSALESORDER || mode == StaticValues.AUTONUM_BILLINGPURCHASEORDER||mode==StaticValues.AUTONUM_PURCHASEORDER||mode==StaticValues.AUTONUM_SALESORDER||mode==StaticValues.AUTONUM_QUOTATION){
                    totalAmount = total;
                    if(mode==StaticValues.AUTONUM_QUOTATION && quotation.getDiscount() != 0){
                    	if(!quotation.isPerDiscount()){
	                    	discountTotalQuotation = quotation.getDiscount();
	                    	total = total - quotation.getDiscount();
	                    	totalAmount = total;
                    	} else {
                    		discountTotalQuotation = total * quotation.getDiscount()/100;
                    		total -= (total * quotation.getDiscount()/100);
                    		totalAmount = total;
                    	}
                    }
                    totaltax = (taxPercent==0?0:totalAmount*taxPercent/100);
                    totalAmount = total + totaltax;
                }else {
                    totalDiscount = inv1.getDiscount();
                    totaltax = inv1.getTaxEntry() != null ? inv1.getTaxEntry().getAmount() : 0;
                    totalAmount =  (inv1.getCustomerEntry().getAmount());
                }
                if(mode!=StaticValues.AUTONUM_PURCHASEORDER || mode!=StaticValues.AUTONUM_SALESORDER){
	                cell3 = createCell(messageSource.getMessage("acc.rem.195", null, RequestContextUtils.getLocale(request)), fontSmallBold, Element.ALIGN_RIGHT, 0, 5);
	                cell3.setColspan(6);
	                table.addCell(cell3);
	                if(mode==StaticValues.AUTONUM_QUOTATION)
	                	invcell = calculateDiscount(discountTotalQuotation, currencyid); 
                    else
                    	invcell = calculateDiscount(totalDiscount, currencyid);
	                invcell.setHorizontalAlignment(Element.ALIGN_RIGHT);
	                invcell.setPadding(5);
	                table.addCell(invcell);
                }
//                for (int i = 0; i < 5; i++) {
//                    invcell = new PdfPCell(new Paragraph("", fontSmallRegular));
//                    invcell.setHorizontalAlignment(Element.ALIGN_CENTER);
//                    invcell.setPadding(5);
//                    invcell.setBorder(0);
//                    table.addCell(invcell);
//                }
                StringBuffer taxNameStr = new StringBuffer();
                if(mainTax != null){
                    taxNameStr.append(mainTax.getName());
                    taxNameStr.append(" ");
                    taxNameStr.append(taxPercent);
                    taxNameStr.append("% (+)");
                } else {
                    taxNameStr.append(messageSource.getMessage("acc.rem.196", null, RequestContextUtils.getLocale(request)));
                }
                cell3 = createCell(taxNameStr.toString(), fontSmallBold, Element.ALIGN_RIGHT, 0, 5);
                cell3.setColspan(6);
                table.addCell(cell3);
                cell3 = createCell(authHandlerDAOObj.getFormattedCurrency(totaltax, currencyid), fontSmallRegular, Element.ALIGN_RIGHT, 15, 5);
                table.addCell(cell3);
//                for (int i = 0; i < 5; i++) {
//                    invcell = new PdfPCell(new Paragraph("", fontSmallRegular));
//                    invcell.setHorizontalAlignment(Element.ALIGN_CENTER);
//                    invcell.setPadding(5);
//                    invcell.setBorder(0);
//                    table.addCell(invcell);
//                }
                cell3 = createCell(messageSource.getMessage("acc.rem.197", null, RequestContextUtils.getLocale(request)), fontSmallBold, Element.ALIGN_RIGHT, 0, 5);
                cell3.setColspan(6);
                table.addCell(cell3);
                cell3 = createCell(authHandlerDAOObj.getFormattedCurrency(totalAmount, currencyid), fontSmallRegular, Element.ALIGN_RIGHT, 15, 5);
                table.addCell(cell3);

                addTableRow(mainTable, table);
                
                KWLCurrency currency = (KWLCurrency) kwlCommonTablesDAOObj.getClassObject(KWLCurrency.class.getName(), currencyid);
                String netinword = EnglishNumberToWordsOjb.convert(Double.parseDouble(String.valueOf(totalAmount)), currency);
                String currencyname = currency.getName();
                cell3 = createCell(messageSource.getMessage("acc.rem.177", null, RequestContextUtils.getLocale(request))+" : " + currencyname + " " + netinword + " Only.", fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT + Rectangle.BOTTOM + Rectangle.TOP, 5);
                PdfPTable table2 = new PdfPTable(1);
                table2.addCell(cell3);
                PdfPCell mainCell62 = new PdfPCell(table2);
                mainCell62.setBorder(0);
                mainCell62.setPadding(10);
                mainTable.addCell(mainCell62);
                PdfPTable helpTable = new PdfPTable(new float[]{8, 92});
                helpTable.setWidthPercentage(100);
                Phrase phrase1 = new Phrase(messageSource.getMessage("acc.common.memo", null, RequestContextUtils.getLocale(request))+":  ",fontSmallBold);
                Phrase phrase2 = new Phrase(memo,fontSmallRegular);
                PdfPCell pcell1 = new PdfPCell(phrase1);
                PdfPCell pcell2 = new PdfPCell(phrase2);
                pcell1.setBorder(0);
                pcell1.setPadding(10);
                pcell1.setPaddingRight(0);
                pcell2.setBorder(0);
                pcell2.setPadding(10);
                helpTable.addCell(pcell1);
                helpTable.addCell(pcell2);

                PdfPCell mainCell61 = new PdfPCell(helpTable);
                mainCell61.setBorder(0);
                mainTable.addCell(mainCell61);
  
            } else if (mode == StaticValues.AUTONUM_DEBITNOTE || mode == StaticValues.AUTONUM_CREDITNOTE || mode == StaticValues.AUTONUM_BILLINGCREDITNOTE || mode == StaticValues.AUTONUM_BILLINGDEBITNOTE) {
                addHeaderFooter(document, writer);
               
                CreditNote creNote = null;
                DebitNote dbNote = null;
                BillingCreditNote biCreNote = null;
                BillingDebitNote biDeNote = null;
                Company com  = null;
                Account cEntry = null;
                String invno = "";
                Date entryDate = null;
                Customer customerObj = null;
                Vendor vendorObj = null;
                double taxMain = 0;
                
                if(mode == StaticValues.AUTONUM_BILLINGCREDITNOTE){
                    KwlReturnObject cap = kwlCommonTablesDAOObj.getObject(BillingCreditNote.class.getName(), billid);
                    biCreNote = (BillingCreditNote) cap.getEntityList().get(0);
                    Set<JournalEntryDetail> entryset=biCreNote.getJournalEntry().getDetails();
                    customerObj = new Customer();
                    Iterator itr=entryset.iterator();
                    while(itr.hasNext()){
                        cEntry=((JournalEntryDetail)itr.next()).getAccount();
    //                    customer=(Customer)session.get(Customer.class,acc.getID());
                        customerObj=(Customer)kwlCommonTablesDAOObj.getClassObject(Customer.class.getName(),cEntry.getID());
                        if(customerObj!=null)
                                break;
                    }
                    com = biCreNote.getCompany();
                    invno = biCreNote.getCreditNoteNumber();
                    entryDate = biCreNote.getJournalEntry().getEntryDate();
                }else if(mode == StaticValues.AUTONUM_BILLINGDEBITNOTE){
                    KwlReturnObject cap = kwlCommonTablesDAOObj.getObject(BillingDebitNote.class.getName(), billid);
                    biDeNote = (BillingDebitNote) cap.getEntityList().get(0);
//                    dbNote = (BillingDebitNote) kwlCommonTablesDAOObj.getClassObject(BillingDebitNote.class.getName(), billid);
                    com = biDeNote.getCompany();
                    Set<JournalEntryDetail> entryset=biDeNote.getJournalEntry().getDetails();
                    vendorObj=new Vendor();
                    Iterator itr=entryset.iterator();
                    while(itr.hasNext()){
                        cEntry=((JournalEntryDetail)itr.next()).getAccount();
    //                    vendor=(Vendor)session.get(Vendor.class,acc.getID());
                        vendorObj=(Vendor)kwlCommonTablesDAOObj.getClassObject(Vendor.class.getName(),cEntry.getID());
                        if(vendorObj!=null)
                            break;
                    }
                    invno = biDeNote.getDebitNoteNumber();
                    entryDate = biDeNote.getJournalEntry().getEntryDate();
                }
                if (mode == StaticValues.AUTONUM_CREDITNOTE) {
                    KwlReturnObject cap = kwlCommonTablesDAOObj.getObject(CreditNote.class.getName(), billid);
                    creNote = (CreditNote) cap.getEntityList().get(0);
                    Set<JournalEntryDetail> entryset=creNote.getJournalEntry().getDetails();
                    customerObj = new Customer();
                    Iterator itr=entryset.iterator();
                    while(itr.hasNext()){
                        cEntry=((JournalEntryDetail)itr.next()).getAccount();
    //                    customer=(Customer)session.get(Customer.class,acc.getID());
                        customerObj=(Customer)kwlCommonTablesDAOObj.getClassObject(Customer.class.getName(),cEntry.getID());
                        if(customerObj!=null)
                                break;
                    }
                    com = creNote.getCompany();
                    invno = creNote.getCreditNoteNumber();
                    entryDate = creNote.getJournalEntry().getEntryDate();
                //inv = (Invoice) session.get(Invoice.class, billid);
                }else if(mode == StaticValues.AUTONUM_DEBITNOTE ){
                    KwlReturnObject cap = kwlCommonTablesDAOObj.getObject(DebitNote.class.getName(), billid);
                    dbNote = (DebitNote) cap.getEntityList().get(0);
//                    dbNote = (BillingDebitNote) kwlCommonTablesDAOObj.getClassObject(BillingDebitNote.class.getName(), billid);
                    com = dbNote.getCompany();
                    Set<JournalEntryDetail> entryset=dbNote.getJournalEntry().getDetails();
                    vendorObj=new Vendor();
                    Iterator itr=entryset.iterator();
                    while(itr.hasNext()){
                        cEntry=((JournalEntryDetail)itr.next()).getAccount();
    //                    vendor=(Vendor)session.get(Vendor.class,acc.getID());
                        vendorObj=(Vendor)kwlCommonTablesDAOObj.getClassObject(Vendor.class.getName(),cEntry.getID());
                        if(vendorObj!=null)
                            break;
                    }
                    invno = dbNote.getDebitNoteNumber();
                    entryDate = dbNote.getJournalEntry().getEntryDate();
                }


//                Company com = mode != StaticValues.AUTONUM_BILLINGINVOICE ? inv.getCompany() : inv1.getCompany();
                String company[] = new String[4];
                company[0] = com.getCompanyName();
                company[1] = com.getAddress();
                company[2] = com.getEmailID();
                company[3] = com.getPhoneNumber();

                PdfPTable table1 = new PdfPTable(2);
                table1.setWidthPercentage(100);
                table1.setWidths(new float[]{50, 50});

                tab1 = addCompanyLogo(logoPath, com);
                tab2 = new PdfPTable(1);
                PdfPCell invCell = null;

                CompanyAccountPreferences pref = (CompanyAccountPreferences) kwlCommonTablesDAOObj.getClassObject(CompanyAccountPreferences.class.getName(), com.getCompanyID());
                Account cash = pref.getCashAccount();
//
//                if (mode != StaticValues.AUTONUM_BILLINGINVOICE) {
//                    cEntry = inv.getCustomerEntry().getAccount();
//                } else {
//                    cEntry = inv1.getCustomerEntry().getAccount();
//                }
                String theader = "";
                if(mode == StaticValues.AUTONUM_CREDITNOTE||mode == StaticValues.AUTONUM_BILLINGCREDITNOTE){
                    theader = messageSource.getMessage("acc.accPref.autoCN", null, RequestContextUtils.getLocale(request));
                }else if(mode == StaticValues.AUTONUM_DEBITNOTE||mode == StaticValues.AUTONUM_BILLINGDEBITNOTE){
                     theader = messageSource.getMessage("acc.accPref.autoDN", null, RequestContextUtils.getLocale(request));
                }
                invCell=createCell(theader,fontTbl,Element.ALIGN_RIGHT,0,5);
                tab2.addCell(invCell);

                PdfPCell cell1 = new PdfPCell(tab1);
                cell1.setBorder(0);
                table1.addCell(cell1);
                PdfPCell cel2 = new PdfPCell(tab2);
                cel2.setBorder(0);
                table1.addCell(cel2);

                PdfPCell mainCell11 = new PdfPCell(table1);
                mainCell11.setBorder(0);
                mainCell11.setPadding(10);
                mainTable.addCell(mainCell11);


                PdfPTable userTable2 = new PdfPTable(2);
                userTable2.setWidthPercentage(100);
                userTable2.setWidths(new float[]{60, 40});

                tab3 = getCompanyInfo(company);

                PdfPTable tab4 = new PdfPTable(2);
                tab4.setWidthPercentage(100);
                tab4.setWidths(new float[]{30, 70});

                PdfPCell cell2=createCell(theader+" #",fontSmallBold,Element.ALIGN_LEFT,0,5);
                tab4.addCell(cell2);
//                String invno = mode != StaticValues.AUTONUM_BILLINGINVOICE ? inv.getInvoiceNumber() : inv1.getBillingInvoiceNumber();
                cell2 = createCell(": " + invno, fontSmallRegular, Element.ALIGN_LEFT, 0, 5);
                tab4.addCell(cell2);
                cell2 = createCell(messageSource.getMessage("acc.rem.198", null, RequestContextUtils.getLocale(request))+" ", fontSmallBold, Element.ALIGN_LEFT, 0, 5);
                tab4.addCell(cell2);
                cell2 = createCell(": " + formatter.format(entryDate), fontSmallRegular, Element.ALIGN_LEFT, 0, 5);
                tab4.addCell(cell2);

                cell1 = new PdfPCell(tab3);
                cell1.setBorder(0);
                userTable2.addCell(cell1);
                cel2 = new PdfPCell(tab4);
                cel2.setBorder(0);
                userTable2.addCell(cel2);

                PdfPCell mainCell12 = new PdfPCell(userTable2);
                mainCell12.setBorder(0);
                mainCell12.setPadding(10);
                mainTable.addCell(mainCell12);


                PdfPTable tab5 = new PdfPTable(2);
                tab5.setWidthPercentage(100);
                tab5.setWidths(new float[]{10, 90});
                PdfPCell cell3 = createCell(messageSource.getMessage("acc.common.to", null, RequestContextUtils.getLocale(request))+" , ", fontSmallBold, Element.ALIGN_LEFT, 0, 5);
                tab5.addCell(cell3);
                cell3 = createCell("", fontSmallBold, Element.ALIGN_LEFT, 0, 0);
                tab5.addCell(cell3);
                cell3 = createCell("", fontSmallBold, Element.ALIGN_LEFT, 0, 0);
                tab5.addCell(cell3);

                String customerName = "";
                String shipTo = "";
                String memo = "";
                Iterator itr = null;

                HashMap<String, Object> invRequestParams = new HashMap<String, Object>();
                ArrayList filter_names = new ArrayList(),filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
                order_by.add("srno");
                order_type.add("asc");
                invRequestParams.put("order_by", order_by);
                invRequestParams.put("order_type", order_type);
                KwlReturnObject idresult = null;
                
                if(mode==StaticValues.AUTONUM_BILLINGCREDITNOTE){
                    customerName = customerObj.getName();//inv.getCustomer()==null?inv.getCustomerEntry().getAccount().getName():inv.getCustomer().getName();
                    shipTo = customerObj.getBillingAddress();//inv.getShipTo();
                    filter_names.add("creditNote.ID");
                    filter_params.add(biCreNote.getID());
                    invRequestParams.put("filter_names", filter_names);
                    invRequestParams.put("filter_params", filter_params);
                    idresult = accCreditNoteDAOobj.getBillingCreditNoteDetails(invRequestParams);
                    itr = idresult.getEntityList().iterator();
                    memo =biCreNote.getMemo();
                }else if(mode == StaticValues.AUTONUM_BILLINGDEBITNOTE){
                    customerName = vendorObj.getName();//dbNote.getCustomer().getName();
                    shipTo = vendorObj.getAddress();
                    filter_names.add("debitNote.ID");
                    filter_params.add(biDeNote.getID());
                    invRequestParams.put("filter_names", filter_names);
                    invRequestParams.put("filter_params", filter_params);
                    idresult = accDebitNoteobj.getBillingDebitNoteDetails(invRequestParams);
                    itr = idresult.getEntityList().iterator();
                    memo = biDeNote.getMemo();
                }else if(mode==StaticValues.AUTONUM_CREDITNOTE){
                    customerName = customerObj.getName();//inv.getCustomer()==null?inv.getCustomerEntry().getAccount().getName():inv.getCustomer().getName();
                    shipTo = customerObj.getBillingAddress();//inv.getShipTo();
                    filter_names.add("creditNote.ID");
                    filter_params.add(creNote.getID());
                    invRequestParams.put("filter_names", filter_names);
                    invRequestParams.put("filter_params", filter_params);
                    idresult = accCreditNoteDAOobj.getCreditNoteDetails(invRequestParams);
                    itr = idresult.getEntityList().iterator();
                    memo =creNote.getMemo();
                } else if(mode == StaticValues.AUTONUM_DEBITNOTE){
                    customerName = vendorObj.getName();//dbNote.getCustomer().getName();
                    shipTo = vendorObj.getAddress();
                    filter_names.add("debitNote.ID");
                    filter_params.add(dbNote.getID());
                    invRequestParams.put("filter_names", filter_names);
                    invRequestParams.put("filter_params", filter_params);
                    idresult = accDebitNoteobj.getDebitNoteDetails(invRequestParams);
                    itr = idresult.getEntityList().iterator();
                    memo = dbNote.getMemo();
                } 
                cell3=createCell(customerName, fontSmallRegular,Element.ALIGN_LEFT,0,5);
                tab5.addCell(cell3);
                cell3 = createCell("", fontSmallBold, Element.ALIGN_LEFT, 0, 0);
                tab5.addCell(cell3);
                cell3 = createCell(shipTo, fontSmallRegular, Element.ALIGN_LEFT, 0, 5);
                tab5.addCell(cell3);

                PdfPCell mainCell14 = new PdfPCell(tab5);
                mainCell14.setBorder(0);
                mainCell14.setPadding(10);
                mainTable.addCell(mainCell14);


                String[] header = {messageSource.getMessage("acc.cnList.Sno", null, RequestContextUtils.getLocale(request)),messageSource.getMessage("acc.rem.190", null, RequestContextUtils.getLocale(request)), messageSource.getMessage("acc.rem.187", null, RequestContextUtils.getLocale(request)), messageSource.getMessage("acc.rem.193", null, RequestContextUtils.getLocale(request))};
                PdfPTable table = new PdfPTable(4);
                table.setWidthPercentage(100);
                table.setWidths(new float[]{7, 35, 29, 29});
                PdfPCell invcell = null;
                for (int i = 0; i < header.length; i++) {
                    invcell = new PdfPCell(new Paragraph(header[i], fontSmallBold));
                    invcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    invcell.setBackgroundColor(Color.LIGHT_GRAY);
                    invCell.setBorder(0);
                    invcell.setPadding(3);
                    table.addCell(invcell);
                }

//                Iterator itr = mode != StaticValues.AUTONUM_BILLINGINVOICE ? inv.getRows().iterator() : inv1.getRows().iterator();
                CreditNoteDetail row = null;
                DebitNoteDetail row1 = null;
                BillingCreditNoteDetail row2 = null;
                BillingDebitNoteDetail row3 = null;
                int index = 0;
                while (itr.hasNext()) {
                    String prodName = "";
                    double quantity = 0;
                    Discount discount = null;
                    String uom = "";
                    double amount1 = 0;
                    if (mode == StaticValues.AUTONUM_CREDITNOTE) {
                        row = (CreditNoteDetail) itr.next();
                        prodName = row.getInvoiceRow().getInventory().getProduct().getName();
                        quantity =  row.getQuantity();
                        discount = row.getDiscount();
                        taxMain = taxMain + row.getTaxAmount();
                        total = total + row.getTaxAmount();
                        try{
                            uom = row.getInvoiceRow().getInventory().getProduct().getUnitOfMeasure()==null?"":row.getInvoiceRow().getInventory().getProduct().getUnitOfMeasure().getName();
                        } catch(Exception ex){//In case of exception use uom="";
                        }
                    }else if(mode == StaticValues.AUTONUM_DEBITNOTE){
                        row1 = (DebitNoteDetail) itr.next();
                        prodName = row1.getGoodsReceiptRow().getInventory().getProduct().getName();
                        quantity =  row1.getQuantity();
                        discount = row1.getDiscount();
                        taxMain = taxMain + row1.getTaxAmount();
                        total = total + row1.getTaxAmount();
                        try{
                            uom = row1.getGoodsReceiptRow().getInventory().getProduct().getUnitOfMeasure()==null?"":row1.getGoodsReceiptRow().getInventory().getProduct().getUnitOfMeasure().getName();
                        } catch(Exception ex){//In case of exception use uom="";
                        }
                    }else if(mode == StaticValues.AUTONUM_BILLINGCREDITNOTE){
                        row2 = (BillingCreditNoteDetail) itr.next();
                        prodName = row2.getInvoiceRow().getProductDetail();
                        quantity =  row2.getQuantity();
                        discount = row2.getDiscount();
                        taxMain = taxMain + row2.getTaxAmount();
                        total = total + row2.getTaxAmount();
                    }else if(mode == StaticValues.AUTONUM_BILLINGDEBITNOTE){
                        row3 = (BillingDebitNoteDetail) itr.next();
                        prodName = row3.getGoodsReceiptRow().getProductDetail();
                        quantity =  row3.getQuantity();
                        discount = row3.getDiscount();
                        taxMain = taxMain + row3.getTaxAmount();
                        total = total + row3.getTaxAmount();
                    }

                    invcell = createCell((++index)+".", fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                    table.addCell(invcell);
                    invcell = createCell(prodName, fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                    table.addCell(invcell);
                    invcell = createCell((int)quantity+" "+uom, fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
                    table.addCell(invcell);
                    invcell = calculateDiscount(discount, currencyid);
                    invcell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    invcell.setPadding(5);
                    invcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
                    table.addCell(invcell);

                    total += discount.getDiscountValue();
//                    invcell = createCell(authHandlerDAOObj.getFormattedCurrency(amount1, currencyid), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
//                    table.addCell(invcell);

                }
//                for (int j = 0; j < 70; j++) {
//                    invcell = new PdfPCell(new Paragraph("", fontSmallRegular));
//                    invcell.setHorizontalAlignment(Element.ALIGN_CENTER);
//                    invcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
//                    table.addCell(invcell);
//                }
//                for (int i = 0; i < 3; i++) {
//                    invcell = new PdfPCell(new Paragraph("", fontSmallRegular));
//                    invcell.setHorizontalAlignment(Element.ALIGN_CENTER);
//                    invcell.setBorder(Rectangle.TOP);
//                    table.addCell(invcell);
//                }

                for (int i = 0; i < 2; i++) {
                    invcell = new PdfPCell(new Paragraph("", fontSmallRegular));
                    invcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    invcell.setBorder(Rectangle.TOP);
                    table.addCell(invcell);
                }
                
                
                cell3 = createCell(messageSource.getMessage("acc.rem.192", null, RequestContextUtils.getLocale(request)), fontSmallBold, Element.ALIGN_CENTER, Rectangle.TOP, 5);
                table.addCell(cell3);
                cell3 = createCell(authHandlerDAOObj.getFormattedCurrency(taxMain, currencyid), fontSmallRegular, Element.ALIGN_RIGHT, 15, 5);
                table.addCell(cell3);
                
                
                for (int i = 0; i < 2; i++) {
                    invcell = new PdfPCell(new Paragraph("", fontSmallRegular));
                    invcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    invcell.setBorder(0);
                    table.addCell(invcell);
                }
                
                cell3 = createCell(messageSource.getMessage("acc.rem.197", null, RequestContextUtils.getLocale(request)), fontSmallBold, Element.ALIGN_CENTER, 0, 5);
                table.addCell(cell3);
                cell3 = createCell(authHandlerDAOObj.getFormattedCurrency(total, currencyid), fontSmallRegular, Element.ALIGN_RIGHT, 15, 5);
                table.addCell(cell3);


                PdfPCell mainCell5 = new PdfPCell(table);
                mainCell5.setBorder(0);
                mainCell5.setPadding(10);
                mainTable.addCell(mainCell5);

                PdfPTable helpTable = new PdfPTable(new float[]{8, 92});
                helpTable.setWidthPercentage(100);
                Phrase phrase1 = new Phrase(messageSource.getMessage("acc.common.memo", null, RequestContextUtils.getLocale(request))+" : ",fontSmallBold);
                Phrase phrase2 = new Phrase(memo,fontSmallRegular);
                PdfPCell pcell1 = new PdfPCell(phrase1);
                PdfPCell pcell2 = new PdfPCell(phrase2);
                pcell1.setBorder(0);
                pcell1.setPadding(10);
                pcell1.setPaddingRight(0);
                pcell2.setBorder(0);
                pcell2.setPadding(10);
                helpTable.addCell(pcell1);
                helpTable.addCell(pcell2);

                PdfPCell mainCell61 = new PdfPCell(helpTable);
                mainCell61.setBorder(0);
                mainTable.addCell(mainCell61);

            } else if (mode == StaticValues.AUTONUM_GOODSRECEIPT|| mode == StaticValues.AUTONUM_BILLINGGOODSRECEIPT) {
                addHeaderFooter(document,writer);
                GoodsReceipt gr=null;
                BillingGoodsReceipt gr1=null;
                if (mode == StaticValues.AUTONUM_GOODSRECEIPT) {
                    gr = (GoodsReceipt) kwlCommonTablesDAOObj.getClassObject(GoodsReceipt.class.getName(), billid);
                } else {
                    gr1 = (BillingGoodsReceipt) kwlCommonTablesDAOObj.getClassObject(BillingGoodsReceipt.class.getName(), billid);
                }

                Company com =mode==StaticValues.AUTONUM_GOODSRECEIPT?gr.getCompany():gr1.getCompany();
                String company[] = new String[4];
                company[0] = com.getCompanyName();
                company[1] = com.getAddress();
                company[2] = com.getEmailID();
                company[3] = com.getPhoneNumber();

                KWLCurrency rowCurrency = (mode==StaticValues.AUTONUM_GOODSRECEIPT?gr.getCurrency():gr1.getCurrency());
                String rowCurrenctID = rowCurrency==null?currencyid:rowCurrency.getCurrencyID();

                PdfPTable table1 = new PdfPTable(2);
                table1.setWidthPercentage(100);
                table1.setWidths(new float[]{50, 50});

                tab1 = addCompanyLogo(logoPath, com);
                tab2 = new PdfPTable(1);
                PdfPCell invCell = null;

                CompanyAccountPreferences pref=(CompanyAccountPreferences)kwlCommonTablesDAOObj.getClassObject(CompanyAccountPreferences.class.getName(),com.getCompanyID());
                Account cash=pref.getCashAccount();
                Account vEntry;
                if (mode == StaticValues.AUTONUM_GOODSRECEIPT) {
                    vEntry = gr.getVendorEntry().getAccount();
                } else {
                    vEntry = gr1.getVendorEntry().getAccount();
                }
                String theader = vEntry==cash?messageSource.getMessage("acc.accPref.autoCP", null, RequestContextUtils.getLocale(request)):messageSource.getMessage("acc.accPref.autoVI", null, RequestContextUtils.getLocale(request));
                invCell=createCell(theader,fontTbl,Element.ALIGN_RIGHT,0,5);
                tab2.addCell(invCell);

                PdfPCell cell1 = new PdfPCell(tab1);
                cell1.setBorder(0);
                table1.addCell(cell1);
                PdfPCell cel2 = new PdfPCell(tab2);
                cel2.setBorder(0);
                table1.addCell(cel2);

                PdfPCell mainCell11 = new PdfPCell(table1);
                mainCell11.setBorder(0);
                mainTable.addCell(mainCell11);

                blankTable = addBlankLine(3);
                blankCell = new PdfPCell(blankTable);
                blankCell.setBorder(0);
                mainTable.addCell(blankCell);

                PdfPTable userTable2 = new PdfPTable(2);
                userTable2.setWidthPercentage(100);
                userTable2.setWidths(new float[]{60, 40});

                tab3 = getCompanyInfo(company);

                PdfPTable tab4 = new PdfPTable(2);
                tab4.setWidthPercentage(100);
                tab4.setWidths(new float[]{30, 70});

                PdfPCell cell2=createCell(theader+" #",fontSmallBold,Element.ALIGN_LEFT,0,5);
                tab4.addCell(cell2);
                String grno=mode==StaticValues.AUTONUM_GOODSRECEIPT?gr.getGoodsReceiptNumber():gr1.getBillingGoodsReceiptNumber();
                cell2=createCell(": "+grno,fontSmallRegular,Element.ALIGN_LEFT,0,5);
                tab4.addCell(cell2);
                cell2=createCell(messageSource.getMessage("acc.rem.198", null, RequestContextUtils.getLocale(request))+"  ",fontSmallBold,Element.ALIGN_LEFT,0,5);
                tab4.addCell(cell2);
                cell2=createCell(": "+formatter.format(mode==StaticValues.AUTONUM_GOODSRECEIPT?gr.getJournalEntry().getEntryDate():gr1.getJournalEntry().getEntryDate()),fontSmallRegular,Element.ALIGN_LEFT,0,5);
                tab4.addCell(cell2);

                cell1 = new PdfPCell(tab3);
                cell1.setBorder(0);
                userTable2.addCell(cell1);
                cel2 = new PdfPCell(tab4);
                cel2.setBorder(0);
                userTable2.addCell(cel2);

                PdfPCell mainCell12 = new PdfPCell(userTable2);
                mainCell12.setBorder(0);
                mainTable.addCell(mainCell12);

                blankTable = addBlankLine(3);
                blankCell = new PdfPCell(blankTable);
                blankCell.setBorder(0);
                mainTable.addCell(blankCell);

                PdfPTable tab5 = new PdfPTable(2);
                tab5.setWidthPercentage(100);
                tab5.setWidths(new float[]{10, 90});
                PdfPCell cell3 = createCell(messageSource.getMessage("acc.common.from", null, RequestContextUtils.getLocale(request))+" , ", fontSmallBold, Element.ALIGN_LEFT, 0, 5);
                tab5.addCell(cell3);
                cell3 = createCell("", fontSmallBold, Element.ALIGN_LEFT, 0, 0);
                tab5.addCell(cell3);
                cell3 = createCell("", fontSmallBold, Element.ALIGN_LEFT, 0, 0);
                tab5.addCell(cell3);

                String vendorName = "";
                if(mode==StaticValues.AUTONUM_GOODSRECEIPT){
                    vendorName = gr.getVendor()==null?gr.getVendorEntry().getAccount().getName():gr.getVendor().getName();
                } else {
                    vendorName = gr1.getVendor()==null?gr1.getVendorEntry().getAccount().getName():gr1.getVendor().getName();
                }
                cell3=createCell(vendorName, fontSmallRegular,Element.ALIGN_LEFT,0,5);
                tab5.addCell(cell3);
                cell3 = createCell("", fontSmallBold, Element.ALIGN_LEFT, 0, 0);
                tab5.addCell(cell3);
                cell3 = createCell(mode==StaticValues.AUTONUM_GOODSRECEIPT?gr.getBillFrom():gr1.getBillFrom(), fontSmallRegular,Element.ALIGN_LEFT,0,5);
                tab5.addCell(cell3);

                PdfPCell mainCell14 = new PdfPCell(tab5);
                mainCell14.setBorder(0);
                mainTable.addCell(mainCell14);

                blankTable = addBlankLine(3);
                blankCell = new PdfPCell(blankTable);
                blankCell.setBorder(0);
                mainTable.addCell(blankCell);
                PdfPTable table;
                PdfPCell grcell = null;
                if(isExpenseInv){
                    String[] header  = {"S.No.","Account",  "PRICE", "DISCOUNT","TAX", "LINE TOTAL"};
                     table = new PdfPTable(6);
                    table.setWidthPercentage(100);
                    table.setWidths(new float[]{7, 23, 18, 18, 18,16});
                    for (int i = 0; i < header.length; i++) {
                        grcell = new PdfPCell(new Paragraph(header[i], fontSmallBold));
                        grcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        grcell.setBackgroundColor(Color.LIGHT_GRAY);
                        grcell.setBorder(0);
                        grcell.setPadding(3);
                        table.addCell(grcell);
                    }
                }
                else{

                    String[] header  = {messageSource.getMessage("acc.cnList.Sno", null, RequestContextUtils.getLocale(request)),messageSource.getMessage("acc.rem.190", null, RequestContextUtils.getLocale(request)), messageSource.getMessage("acc.rem.187", null, RequestContextUtils.getLocale(request)), messageSource.getMessage("acc.rem.188", null, RequestContextUtils.getLocale(request)), messageSource.getMessage("acc.rem.191", null, RequestContextUtils.getLocale(request)),messageSource.getMessage("acc.rem.192", null, RequestContextUtils.getLocale(request)), messageSource.getMessage("acc.rem.212", null, RequestContextUtils.getLocale(request))};
                    table = new PdfPTable(7);
                    table.setWidthPercentage(100);
                    table.setWidths(new float[]{5, 20, 15, 15, 15,13, 17});                    
                    for (int i = 0; i < header.length; i++) {
                        grcell = new PdfPCell(new Paragraph(header[i], fontSmallBold));
                        grcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        grcell.setBackgroundColor(Color.LIGHT_GRAY);
                        grcell.setBorder(0);
                        grcell.setPadding(3);
                        table.addCell(grcell);
                    }
                }
//                Iterator itr =mode==StaticValues.AUTONUM_GOODSRECEIPT?gr.getRows().iterator():gr1.getRows().iterator();
                HashMap<String, Object> grRequestParams = new HashMap<String, Object>();
                ArrayList filter_names = new ArrayList(),filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
                order_by.add("srno");
                order_type.add("asc");
                grRequestParams.put("order_by", order_by);
                grRequestParams.put("order_type", order_type);

                KwlReturnObject idresult = null;
                if(mode != StaticValues.AUTONUM_BILLINGINVOICE||mode != StaticValues.AUTONUM_BILLINGGOODSRECEIPT) {
                    if(mode == StaticValues.AUTONUM_BILLINGGOODSRECEIPT){
                        filter_names.add("billingGoodsReceipt.ID");
                        filter_params.add(gr1.getID());
                        grRequestParams.put("filter_names", filter_names);
                        grRequestParams.put("filter_params", filter_params);
                        idresult = accGoodsReceiptobj.getBillingGoodsReceiptDetails(grRequestParams);
                    }
                    else{
                    filter_names.add("goodsReceipt.ID");
                    filter_params.add(gr.getID());
                    grRequestParams.put("filter_names", filter_names);
                    grRequestParams.put("filter_params", filter_params);
                    if(isExpenseInv){
                         idresult = accGoodsReceiptobj.getExpenseGRDetails(grRequestParams);
                    }
                    else
                        idresult = accGoodsReceiptobj.getGoodsReceiptDetails(grRequestParams);}
            } else {
                    filter_names.add("billingGoodsReceipt.ID");
                    filter_params.add(gr.getID());
                    grRequestParams.put("filter_names", filter_names);
                    grRequestParams.put("filter_params", filter_params);
                    idresult = accGoodsReceiptobj.getBillingGoodsReceiptDetails(grRequestParams);
                }
                Iterator itr = idresult.getEntityList().iterator();
                
                GoodsReceiptDetail row=null;
                BillingGoodsReceiptDetail row1=null;
                ExpenseGRDetail exprow=null;
                int index = 0;
                while (itr.hasNext()) {
                    if (mode == StaticValues.AUTONUM_GOODSRECEIPT) {
                        if(isExpenseInv)
                             exprow = (ExpenseGRDetail) itr.next();
                        else
                            row = (GoodsReceiptDetail) itr.next();
                    } else {
                        row1 = (BillingGoodsReceiptDetail) itr.next();
                     }
                    if(isExpenseInv){
                        grcell = createCell((++index)+".", fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                        table.addCell(grcell);
                        grcell = createCell(exprow.getAccount().getName(), fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                        table.addCell(grcell);
                        grcell = createCell(authHandlerDAOObj.getFormattedCurrency(exprow.getRate(), rowCurrenctID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                        table.addCell(grcell);
                        grcell = calculateDiscount(exprow.getDiscount(), rowCurrenctID);
                        grcell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        grcell.setPadding(5);
                        grcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
                        table.addCell(grcell);
                        double amount1 = exprow.getRate();
                        Discount disc = exprow.getDiscount();
                        if (disc != null) {
                            amount1 -=exprow.getDiscount().getDiscountValue();
                        }

                        double rowTaxPercent=0;
                        HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
                        if (exprow!= null&&exprow.getTax() != null) {
                            requestParams.put("transactiondate", exprow.getGoodsReceipt().getJournalEntry().getEntryDate());
                            requestParams.put("taxid", exprow.getTax().getID());
                            KwlReturnObject result = accTaxObj.getTax(requestParams);
                            List taxList = result.getEntityList();
                            Object[] taxObj=(Object[]) taxList.get(0);
                            rowTaxPercent=taxObj[1]==null?0:(Double) taxObj[1];
                        }
                        grcell = createCell(authHandlerDAOObj.getFormattedCurrency(amount1*rowTaxPercent/100, rowCurrenctID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                        table.addCell(grcell);
                        amount1+=amount1*rowTaxPercent/100;
                        grcell = createCell(authHandlerDAOObj.getFormattedCurrency(amount1, rowCurrenctID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                        table.addCell(grcell);
                        total += amount1;
                        for (int j = 0; j < 84; j++) {
                            grcell = new PdfPCell(new Paragraph("", fontSmallRegular));
                            grcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                            grcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
                            table.addCell(grcell);
                        }

                    }else{
                        grcell = createCell((++index)+".", fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                        table.addCell(grcell);
                        grcell = createCell(mode == StaticValues.AUTONUM_GOODSRECEIPT ? row.getInventory().getProduct().getName() : row1.getProductDetail(), fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                        table.addCell(grcell);
                        grcell = createCell(Double.toString(mode == StaticValues.AUTONUM_GOODSRECEIPT ? row.getInventory().getQuantity() : row1.getQuantity()) + " " + (mode != StaticValues.AUTONUM_BILLINGGOODSRECEIPT ? (row.getInventory().getProduct().getUnitOfMeasure()==null?"":row.getInventory().getProduct().getUnitOfMeasure().getName()) : ""), fontSmallRegular, Element.ALIGN_CENTER, Rectangle.LEFT + Rectangle.RIGHT, 5);
                        table.addCell(grcell);
                        grcell = createCell(authHandlerDAOObj.getFormattedCurrency(mode == StaticValues.AUTONUM_GOODSRECEIPT ? row.getRate() : row1.getRate(), rowCurrenctID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                        table.addCell(grcell);
                        grcell = calculateDiscount(mode == StaticValues.AUTONUM_GOODSRECEIPT ? row.getDiscount() : row1.getDiscount(), rowCurrenctID);
                        grcell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        grcell.setPadding(5);
                        grcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
                        table.addCell(grcell);
                        double amount1 = mode == StaticValues.AUTONUM_GOODSRECEIPT ? row.getRate() * row.getInventory().getQuantity() : row1.getRate() * row1.getQuantity();
                        Discount disc = mode == StaticValues.AUTONUM_GOODSRECEIPT ? row.getDiscount() : row1.getDiscount();
                        if (disc != null) {
                            amount1 -= mode == StaticValues.AUTONUM_GOODSRECEIPT ? row.getDiscount().getDiscountValue() : row1.getDiscount().getDiscountValue();
                        }

                        double rowTaxPercent=0;
                        HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
                        if (row!= null&&row.getTax() != null) {
                            requestParams.put("transactiondate", row.getGoodsReceipt().getJournalEntry().getEntryDate());
                            requestParams.put("taxid", row.getTax().getID());
                            KwlReturnObject result = accTaxObj.getTax(requestParams);
                            List taxList = result.getEntityList();
                            Object[] taxObj=(Object[]) taxList.get(0);
                            rowTaxPercent=taxObj[1]==null?0:(Double) taxObj[1];
                        }
                        else if (row1!= null&&row1.getTax() != null) {
                            requestParams.put("transactiondate", row1.getBillingGoodsReceipt().getJournalEntry().getEntryDate());
                            requestParams.put("taxid", row1.getTax().getID());
                            KwlReturnObject result = accTaxObj.getTax(requestParams);
                            List taxList = result.getEntityList();
                            Object[] taxObj=(Object[]) taxList.get(0);
                            rowTaxPercent=taxObj[1]==null?0:(Double) taxObj[1];
                        }
                        grcell = createCell(authHandlerDAOObj.getFormattedCurrency(amount1*rowTaxPercent/100, rowCurrenctID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                        table.addCell(grcell);
                        amount1+=amount1*rowTaxPercent/100;
                        grcell = createCell(authHandlerDAOObj.getFormattedCurrency(amount1, rowCurrenctID), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                        table.addCell(grcell);
                        total += amount1;
                         for (int j = 0; j < 98; j++) {
                            grcell = new PdfPCell(new Paragraph("", fontSmallRegular));
                            grcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                            grcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
                            table.addCell(grcell);
                         }
                   
                    }
                }
                int length=isExpenseInv?4:5;
                for (int i = 0; i < length; i++) {
                    grcell = new PdfPCell(new Paragraph("", fontSmallRegular));
                    grcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    grcell.setBorder(Rectangle.TOP);
                    table.addCell(grcell);
                }
                cell3 = createCell(messageSource.getMessage("acc.rem.194", null, RequestContextUtils.getLocale(request)), fontSmallBold, Element.ALIGN_LEFT, Rectangle.TOP, 5);
                table.addCell(cell3);
                cell3 = createCell(authHandlerDAOObj.getFormattedCurrency(total, rowCurrenctID), fontSmallRegular, Element.ALIGN_RIGHT, 15, 5);
                table.addCell(cell3);
                for (int i = 0; i < length; i++) {
                    grcell = new PdfPCell(new Paragraph("", fontSmallRegular));
                    grcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    grcell.setPadding(5);
                    grcell.setBorder(0);
                    table.addCell(grcell);
                }
                cell3 = createCell(messageSource.getMessage("acc.rem.195", null, RequestContextUtils.getLocale(request)), fontSmallBold, Element.ALIGN_LEFT, 0, 5);
                table.addCell(cell3);
                grcell = calculateDiscount(mode==StaticValues.AUTONUM_GOODSRECEIPT?gr.getDiscount():gr1.getDiscount(), rowCurrenctID);
                grcell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                grcell.setPadding(5);
                table.addCell(grcell);
                for (int i = 0; i < length; i++) {
                    grcell = new PdfPCell(new Paragraph("", fontSmallRegular));
                    grcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    grcell.setPadding(5);
                    grcell.setBorder(0);
                    table.addCell(grcell);
                }
                cell3 = createCell(messageSource.getMessage("acc.rem.196", null, RequestContextUtils.getLocale(request)), fontSmallBold, Element.ALIGN_LEFT, 0, 5);
                table.addCell(cell3);
                cell3 = createCell(authHandlerDAOObj.getFormattedCurrency(mode==StaticValues.AUTONUM_GOODSRECEIPT?(gr.getTaxEntry()!=null?gr.getTaxEntry().getAmount():0):(gr1.getTaxEntry()!=null?gr1.getTaxEntry().getAmount():0),rowCurrenctID), fontSmallRegular,Element.ALIGN_RIGHT,15,5);
                table.addCell(cell3);
                for (int i = 0; i < length; i++) {
                    grcell = new PdfPCell(new Paragraph("", fontSmallRegular));
                    grcell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    grcell.setPadding(5);
                    grcell.setBorder(0);
                    table.addCell(grcell);
                }
                cell3 = createCell(messageSource.getMessage("acc.rem.197", null, RequestContextUtils.getLocale(request)), fontSmallBold, Element.ALIGN_LEFT, 0, 5);
                table.addCell(cell3);
                cell3 = createCell(authHandlerDAOObj.getFormattedCurrency(mode==StaticValues.AUTONUM_GOODSRECEIPT?(gr.getVendorEntry().getAmount()):(gr1.getVendorEntry().getAmount()), rowCurrenctID), fontSmallRegular,Element.ALIGN_RIGHT,15,5);
                table.addCell(cell3);

                PdfPCell mainCell5 = new PdfPCell(table);
                mainCell5.setBorder(0);
                mainTable.addCell(mainCell5);
                KWLCurrency currency = (KWLCurrency) kwlCommonTablesDAOObj.getClassObject(KWLCurrency.class.getName(), currencyid);
                double totalamount=0;
                if(gr!=null)
                    totalamount=gr.getVendorEntry().getAmount();
                else if(gr1!=null)
                    totalamount=gr1.getVendorEntry().getAmount();
                String netinword = EnglishNumberToWordsOjb.convert(Double.parseDouble(String.valueOf(totalamount)), currency);
                String currencyname = currency.getName();
                cell3 = createCell(messageSource.getMessage("acc.rem.177", null, RequestContextUtils.getLocale(request))+" : " + currencyname + " " + netinword + " Only.", fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT + Rectangle.BOTTOM + Rectangle.TOP, 5);
                PdfPTable table2 = new PdfPTable(1);
                table2.addCell(cell3);
                PdfPCell mainCell62 = new PdfPCell(table2);
                mainCell62.setBorder(0);
                mainTable.addCell(mainCell62);
                PdfPTable helpTable = new PdfPTable(new float[]{8, 92});
                helpTable.setWidthPercentage(100);
                Phrase phrase1 = new Phrase(messageSource.getMessage("acc.common.memo", null, RequestContextUtils.getLocale(request))+":  ",fontSmallBold);
                Phrase phrase2 = new Phrase(mode==StaticValues.AUTONUM_GOODSRECEIPT?gr.getMemo():gr1.getMemo(),fontSmallRegular);
                PdfPCell pcell1 = new PdfPCell(phrase1);
                PdfPCell pcell2 = new PdfPCell(phrase2);
                pcell1.setBorder(0);
                pcell1.setPadding(10);
                pcell1.setPaddingRight(0);
                pcell2.setBorder(0);
                pcell2.setPadding(10);
                helpTable.addCell(pcell1);
                helpTable.addCell(pcell2);

                PdfPCell mainCell61 = new PdfPCell(helpTable);
                mainCell61.setBorder(0);
                mainTable.addCell(mainCell61);

            } else if (mode == StaticValues.AUTONUM_RECEIPT || mode == StaticValues.AUTONUM_BILLINGRECEIPT) {

                Receipt rc = null;
                BillingReceipt rc1 = null;
                if (mode != StaticValues.AUTONUM_BILLINGRECEIPT) {
                    rc = (Receipt) kwlCommonTablesDAOObj.getClassObject(Receipt.class.getName(), billid);
                } else {
                    rc1 = (BillingReceipt) kwlCommonTablesDAOObj.getClassObject(BillingReceipt.class.getName(), billid);
                }

                KWLCurrency currency = (KWLCurrency) kwlCommonTablesDAOObj.getClassObject(KWLCurrency.class.getName(), currencyid);
                Company com = mode != StaticValues.AUTONUM_BILLINGRECEIPT ? rc.getCompany() : rc1.getCompany();
                String company[] = new String[3];
                company[0] = com.getCompanyName();
                company[1] = com.getAddress();
                company[2] = com.getEmailID();


                PdfPTable table1 = new PdfPTable(2);
                table1.setWidthPercentage(100);
                table1.setWidths(new float[]{25, 75});

                tab1 = addCompanyLogo(logoPath, com);

                tab3 = new PdfPTable(1);
                tab3.setWidthPercentage(100);
                PdfPCell mainCell1 = null;
                mainCell1 = createCell(com.getCompanyName(), fontMediumBold, Element.ALIGN_LEFT, 0, 0);
                mainCell1.setPaddingLeft(125);
                tab3.addCell(mainCell1);
                mainCell1 = createCell(com.getAddress(), fontSmallRegular, Element.ALIGN_LEFT, 0, 0);
                mainCell1.setPaddingLeft(75);
                tab3.addCell(mainCell1);
                mainCell1 = createCell(com.getEmailID(), fontSmallRegular, Element.ALIGN_LEFT, 0, 0);
                mainCell1.setPaddingLeft(75);
                tab3.addCell(mainCell1);
                mainCell1 = createCell(com.getPhoneNumber(), fontSmallRegular, Element.ALIGN_LEFT, 0, 0);
                mainCell1.setPaddingLeft(75);
                tab3.addCell(mainCell1);
                mainCell1 = new PdfPCell(tab1);
                mainCell1.setBorder(0);
                table1.addCell(mainCell1);
                mainCell1 = new PdfPCell(tab3);
                mainCell1.setBorder(0);
                table1.addCell(mainCell1);

                mainCell1 = new PdfPCell(table1);
                mainCell1.setBorder(0);
                mainTable.addCell(mainCell1);


                blankTable = addBlankLine(5);
                blankCell = new PdfPCell(blankTable);
                blankCell.setBorder(0);
                mainTable.addCell(blankCell);

                PdfPCell mainCell2 = createCell(messageSource.getMessage("acc.numb.37", null, RequestContextUtils.getLocale(request)), fontMediumBold, Element.ALIGN_CENTER, 0, 0);
                mainTable.addCell(mainCell2);

                blankTable = addBlankLine(2);
                blankCell = new PdfPCell(blankTable);
                blankCell.setBorder(0);
                mainTable.addCell(blankCell);

                tab3 = new PdfPTable(2);
                tab3.setWidthPercentage(100);
                tab3.setWidths(new float[]{60, 40});

                tab1 = new PdfPTable(2);
                tab1.setWidthPercentage(100);
                tab1.setWidths(new float[]{10, 90});

                PdfPCell cell3 = createCell(messageSource.getMessage("acc.msgbox.no", null, RequestContextUtils.getLocale(request))+" : ", fontSmallRegular, Element.ALIGN_LEFT, 0, 0);
                new PdfPCell(new Paragraph());
                tab1.addCell(cell3);
                cell3 = createCell(mode!=StaticValues.AUTONUM_BILLINGRECEIPT?rc.getReceiptNumber():rc1.getBillingReceiptNumber(), fontSmallBold,Element.ALIGN_LEFT,0,0);
                tab1.addCell(cell3);
                tab2 = new PdfPTable(2);
                tab2.setWidthPercentage(100);
                tab2.setWidths(new float[]{30, 70});
                cell3 = createCell(messageSource.getMessage("acc.numb.38", null, RequestContextUtils.getLocale(request)), fontSmallRegular, Element.ALIGN_RIGHT, 0, 0);
                tab2.addCell(cell3);
                cell3 = createCell(formatter.format(mode!=StaticValues.AUTONUM_BILLINGRECEIPT?rc.getJournalEntry().getEntryDate():rc1.getJournalEntry().getEntryDate()), fontSmallBold,Element.ALIGN_LEFT,0,0);
                tab2.addCell(cell3);

                PdfPCell mainCell3 = new PdfPCell(tab1);
                mainCell3.setBorder(0);
                tab3.addCell(mainCell3);
                mainCell3 = new PdfPCell(tab2);
                mainCell3.setBorder(0);
                tab3.addCell(mainCell3);

                PdfPCell mainCell4 = new PdfPCell(tab3);
                mainCell4.setBorder(0);
                mainTable.addCell(mainCell4);

                blankTable = addBlankLine(5);
                blankCell = new PdfPCell(blankTable);
                blankCell.setBorder(0);
                mainTable.addCell(blankCell);
                tab2 = new PdfPTable(2);
                tab2.setWidthPercentage(100);
                tab2.setWidths(new float[]{75, 25});
                cell3 = new PdfPCell(new Paragraph(messageSource.getMessage("acc.report.2", null, RequestContextUtils.getLocale(request)), fontSmallBold));
                cell3.setHorizontalAlignment(Element.ALIGN_LEFT);
                cell3.setBackgroundColor(Color.lightGray);
                cell3.setBorder(Rectangle.LEFT + Rectangle.BOTTOM + Rectangle.RIGHT);
                tab2.addCell(cell3);
                cell3 = new PdfPCell(new Paragraph(messageSource.getMessage("acc.rem.193", null, RequestContextUtils.getLocale(request)), fontSmallBold));
                cell3.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell3.setBackgroundColor(Color.lightGray);
                cell3.setBorder(Rectangle.RIGHT + Rectangle.BOTTOM);
                tab2.addCell(cell3);
                cell3 = createCell(messageSource.getMessage("acc.je.acc", null, RequestContextUtils.getLocale(request))+" : ", fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                tab2.addCell(cell3);
                cell3 = createCell("", fontSmallBold, Element.ALIGN_LEFT, Rectangle.RIGHT, 0);
                tab2.addCell(cell3);
                cell3 = createCell(accname, fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT, 0);
                cell3.setPaddingLeft(50);
                tab2.addCell(cell3);
                cell3 = createCell(authHandlerDAOObj.getFormattedCurrency(amount, currencyid), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.RIGHT, 15);
                tab2.addCell(cell3);
                cell3 = createCell(address, fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT, 0);
                cell3.setPaddingLeft(50);
                tab2.addCell(cell3);
                cell3 = createCell("", fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.RIGHT, 0);
                tab2.addCell(cell3);

                for (int i = 0; i < 30; i++) {
                    cell3 = new PdfPCell(new Paragraph("", fontSmallRegular));
                    cell3.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
                    tab2.addCell(cell3);
                }
                cell3 = createCell(messageSource.getMessage("acc.numb.42", null, RequestContextUtils.getLocale(request)), fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                tab2.addCell(cell3);
                cell3 = createCell("", fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.RIGHT, 0);
                tab2.addCell(cell3);
                cell3 = createCell(mode!=StaticValues.AUTONUM_BILLINGRECEIPT?(rc.getPayDetail()==null?"Cash":rc.getPayDetail().getPaymentMethod().getMethodName()):(rc1.getPayDetail()==null?"Cash":rc1.getPayDetail().getPaymentMethod().getMethodName()), fontSmallRegular,Element.ALIGN_LEFT,Rectangle.LEFT + Rectangle.RIGHT,5);
                cell3.setPaddingLeft(50);
                tab2.addCell(cell3);
                cell3 = createCell("", fontSmallRegular, Element.ALIGN_CENTER, Rectangle.RIGHT, 0);
                tab2.addCell(cell3);
                cell3 = createCell(mode!=StaticValues.AUTONUM_BILLINGRECEIPT?(rc.getPayDetail()!=null?(rc.getPayDetail().getPaymentMethod().getMethodName().equals("Cheque")?"Cheque No : "+rc.getPayDetail().getCheque().getChequeNo()+" and Bank Name : "+rc.getPayDetail().getCheque().getBankName():""):""):
                    (rc1.getPayDetail()!=null?(rc1.getPayDetail().getPaymentMethod().getMethodName().equals("Cheque")?"Cheque No : "+rc1.getPayDetail().getCheque().getChequeNo()+" and Bank Name : "+rc1.getPayDetail().getCheque().getBankName():""):""), fontSmallRegular,Element.ALIGN_LEFT,Rectangle.LEFT + Rectangle.RIGHT,5);
                cell3.setPaddingLeft(50);
                tab2.addCell(cell3);
                cell3 = createCell("", fontSmallRegular, Element.ALIGN_CENTER, Rectangle.RIGHT, 0);
                tab2.addCell(cell3);
                String str="";
                                if(mode!=StaticValues.AUTONUM_BILLINGRECEIPT){
                  if( rc.getPayDetail()!=null)
                      if(rc.getPayDetail().getPaymentMethod().getDetailType()==2||rc.getPayDetail().getPaymentMethod().getDetailType()==1)
                          if(rc.getPayDetail().getCard()!=null)
                              str="Card No : "+(rc.getPayDetail().getCard().getCardNo())+" and Card Holder : "+rc.getPayDetail().getCard().getCardHolder();
                            else if(rc.getPayDetail().getCheque()!=null)
                                 str="Bank Name : "+(rc.getPayDetail().getCheque().getBankName())+"and Ref. No : "+(rc.getPayDetail().getCheque().getChequeNo());
                }else
                   if( rc1.getPayDetail()!=null)
                         if(rc1.getPayDetail().getPaymentMethod().getDetailType()==2||rc1.getPayDetail().getPaymentMethod().getDetailType()==1)
                            if(rc1.getPayDetail().getCard()!=null)
                                str="Card No : "+(rc1.getPayDetail().getCard().getCardNo())+" and Card Holder : "+rc1.getPayDetail().getCard().getCardHolder();
                            else if(rc1.getPayDetail().getCheque()!=null)
                                 str="Bank Name : "+(rc1.getPayDetail().getCheque().getBankName())+"and Ref. No : "+(rc1.getPayDetail().getCheque().getChequeNo());


//                mode!=StaticValues.AUTONUM_BILLINGRECEIPT?(rc.getPayDetail()!=null?(rc.getPayDetail().getPaymentMethod().getMethodName().equals("Credit Card")||customer.equals("Debit Card")?
//                        rc.getPayDetail().getCard()!=null?"Card No : "+(rc.getPayDetail().getCard().getCardNo()+" and Card Holder : "+rc.getPayDetail().getCard().getCardHolder()):"":""):""):
//                        (rc1.getPayDetail()!=null?(rc1.getPayDetail().getPaymentMethod().getMethodName().equals("Credit Card")||customer.equals("Debit Card")?
//                        rc1.getPayDetail().getCard()!=null?"Card No : "+(rc1.getPayDetail().getCard().getCardNo()+" and Card Holder : "+rc1.getPayDetail().getCard().getCardHolder()):"":""):"")

                cell3 = createCell(str,fontSmallRegular,Element.ALIGN_LEFT,Rectangle.LEFT + Rectangle.RIGHT,5);
                cell3.setPaddingLeft(50);
                tab2.addCell(cell3);
                cell3 = createCell("", fontSmallRegular, Element.ALIGN_CENTER, Rectangle.RIGHT, 0);
                tab2.addCell(cell3);
                cell3 = createCell(messageSource.getMessage("acc.numb.41", null, RequestContextUtils.getLocale(request)), fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                tab2.addCell(cell3);
                cell3 = createCell("", fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.RIGHT, 5);
                tab2.addCell(cell3);
                cell3 = createCell(mode!=StaticValues.AUTONUM_BILLINGRECEIPT?rc.getMemo():rc1.getMemo(), fontSmallRegular,Element.ALIGN_LEFT,Rectangle.LEFT + Rectangle.RIGHT,5);
                cell3.setPaddingLeft(50);
                tab2.addCell(cell3);
                cell3 = createCell("", fontSmallRegular, Element.ALIGN_CENTER, Rectangle.RIGHT, 5);
                tab2.addCell(cell3);
                String netinword = EnglishNumberToWordsOjb.convert(Double.parseDouble(String.valueOf(amount)), currency);
                String currencyname = currency.getName();
                cell3 = createCell(messageSource.getMessage("acc.rem.177", null, RequestContextUtils.getLocale(request))+" : " + currencyname + " " + netinword + " Only.", fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT + Rectangle.BOTTOM + Rectangle.TOP, 5);
                tab2.addCell(cell3);
                cell3 = createCell(authHandlerDAOObj.getFormattedCurrency(amount, currencyid), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT + Rectangle.BOTTOM + Rectangle.TOP, 5);
                tab2.addCell(cell3);
                PdfPCell mainCell5 = new PdfPCell(tab2);
                mainCell5.setBorder(1);
                mainTable.addCell(mainCell5);

                blankTable = addBlankLine(25);
                blankCell = new PdfPCell(blankTable);
                blankCell.setBorder(0);
                mainTable.addCell(blankCell);

                tab1 = new PdfPTable(2);
                tab1.setWidthPercentage(100);
                tab1.setWidths(new float[]{50, 50});
                cell3 = createCell(messageSource.getMessage("acc.numb.35", null, RequestContextUtils.getLocale(request)), fontTblMediumBold, Element.ALIGN_CENTER, 0, 0);
                tab1.addCell(cell3);
                cell3 = createCell(messageSource.getMessage("acc.numb.36", null, RequestContextUtils.getLocale(request)), fontTblMediumBold, Element.ALIGN_CENTER, 0, 0);
                tab1.addCell(cell3);
                PdfPCell mainCell6 = new PdfPCell(tab1);
                mainCell6.setPadding(5);
                mainCell6.setBorder(0);
                mainTable.addCell(mainCell6);

                blankTable = addBlankLine(15);
                blankCell = new PdfPCell(blankTable);
                blankCell.setBorder(0);
                mainTable.addCell(blankCell);

                tab2 = new PdfPTable(2);
                tab2.setWidthPercentage(100);
                tab2.setWidths(new float[]{50, 50});
                cell3 = createCell(messageSource.getMessage("acc.numb.39", null, RequestContextUtils.getLocale(request)), fontTblMediumBold, Element.ALIGN_CENTER, 0, 0);
                tab2.addCell(cell3);
                cell3 = createCell(messageSource.getMessage("acc.numb.40", null, RequestContextUtils.getLocale(request)), fontTblMediumBold, Element.ALIGN_CENTER, 0, 0);
                tab2.addCell(cell3);
                PdfPCell mainCell7 = new PdfPCell(tab2);
                mainCell7.setPadding(5);
                mainCell7.setBorder(0);
                mainTable.addCell(mainCell7);
                blankTable = addBlankLine(5);
                blankCell = new PdfPCell(blankTable);
                blankCell.setBorder(0);
                mainTable.addCell(blankCell);

            } else if (mode == StaticValues.AUTONUM_PAYMENT||mode == StaticValues.AUTONUM_BILLINGPAYMENT) {
                Payment pc = null;
                BillingPayment pc1= null;
                if (mode == StaticValues.AUTONUM_PAYMENT) {
                    pc = (Payment) kwlCommonTablesDAOObj.getClassObject(Payment.class.getName(), billid);
                } else {
                    pc1 = (BillingPayment) kwlCommonTablesDAOObj.getClassObject(BillingPayment.class.getName(), billid);
                }
                KWLCurrency currency = (KWLCurrency) kwlCommonTablesDAOObj.getClassObject(KWLCurrency.class.getName(), currencyid);
                Company com =mode==StaticValues.AUTONUM_PAYMENT?pc.getCompany():pc1.getCompany();
                String company[] = new String[3];
                company[0] = com.getCompanyName();
                company[1] = com.getAddress();
                company[2] = com.getEmailID();


                PdfPTable table1 = new PdfPTable(2);
                table1.setWidthPercentage(100);
                table1.setWidths(new float[]{25, 75});

                tab1 = addCompanyLogo(logoPath, com);

                tab3 = new PdfPTable(1);
                tab3.setWidthPercentage(100);
                PdfPCell mainCell1 = null;
                mainCell1 = createCell(com.getCompanyName(), fontMediumBold, Element.ALIGN_LEFT, 0, 0);
                mainCell1.setPaddingLeft(125);
                tab3.addCell(mainCell1);
                mainCell1 = createCell(com.getAddress(), fontSmallRegular, Element.ALIGN_LEFT, 0, 0);
                mainCell1.setPaddingLeft(75);
                tab3.addCell(mainCell1);
                mainCell1 = createCell(com.getEmailID(), fontSmallRegular, Element.ALIGN_LEFT, 0, 0);
                mainCell1.setPaddingLeft(75);
                tab3.addCell(mainCell1);
                mainCell1 = createCell(com.getPhoneNumber(), fontSmallRegular, Element.ALIGN_LEFT, 0, 0);
                mainCell1.setPaddingLeft(75);
                tab3.addCell(mainCell1);
                mainCell1 = new PdfPCell(tab1);
                mainCell1.setBorder(0);
                table1.addCell(mainCell1);
                mainCell1 = new PdfPCell(tab3);
                mainCell1.setBorder(0);
                table1.addCell(mainCell1);

                mainCell1 = new PdfPCell(table1);
                mainCell1.setBorder(0);
                mainTable.addCell(mainCell1);


                blankTable = addBlankLine(5);
                blankCell = new PdfPCell(blankTable);
                blankCell.setBorder(0);
                mainTable.addCell(blankCell);

                PdfPCell mainCell2 = createCell(messageSource.getMessage("acc.numb.37", null, RequestContextUtils.getLocale(request)), fontMediumBold, Element.ALIGN_CENTER, 0, 0);
                mainTable.addCell(mainCell2);

                blankTable = addBlankLine(2);
                blankCell = new PdfPCell(blankTable);
                blankCell.setBorder(0);
                mainTable.addCell(blankCell);

                tab3 = new PdfPTable(2);
                tab3.setWidthPercentage(100);
                tab3.setWidths(new float[]{60, 40});

                tab1 = new PdfPTable(2);
                tab1.setWidthPercentage(100);
                tab1.setWidths(new float[]{10, 90});

                PdfPCell cell3 = createCell(messageSource.getMessage("acc.msgbox.no", null, RequestContextUtils.getLocale(request))+" : ", fontSmallRegular, Element.ALIGN_LEFT, 0, 0);
                new PdfPCell(new Paragraph());
                tab1.addCell(cell3);
                cell3 = createCell(mode==StaticValues.AUTONUM_PAYMENT?pc.getPaymentNumber():pc1.getBillingPaymentNumber(), fontSmallBold,Element.ALIGN_LEFT,0,0);
                tab1.addCell(cell3);
                tab2 = new PdfPTable(2);
                tab2.setWidthPercentage(100);
                tab2.setWidths(new float[]{30, 70});
                cell3 = createCell(messageSource.getMessage("acc.numb.38", null, RequestContextUtils.getLocale(request)), fontSmallRegular, Element.ALIGN_RIGHT, 0, 0);
                tab2.addCell(cell3);
                cell3 = createCell(formatter.format(mode==StaticValues.AUTONUM_PAYMENT?pc.getJournalEntry().getEntryDate():pc1.getJournalEntry().getEntryDate()), fontSmallBold,Element.ALIGN_LEFT,0,0);
                tab2.addCell(cell3);

                PdfPCell mainCell3 = new PdfPCell(tab1);
                mainCell3.setBorder(0);
                tab3.addCell(mainCell3);
                mainCell3 = new PdfPCell(tab2);
                mainCell3.setBorder(0);
                tab3.addCell(mainCell3);

                PdfPCell mainCell4 = new PdfPCell(tab3);
                mainCell4.setBorder(0);
                mainTable.addCell(mainCell4);

                blankTable = addBlankLine(5);
                blankCell = new PdfPCell(blankTable);
                blankCell.setBorder(0);
                mainTable.addCell(blankCell);
                tab2 = new PdfPTable(2);
                tab2.setWidthPercentage(100);
                tab2.setWidths(new float[]{75, 25});
                cell3 = new PdfPCell(new Paragraph(messageSource.getMessage("acc.report.2", null, RequestContextUtils.getLocale(request)), fontSmallBold));
                cell3.setHorizontalAlignment(Element.ALIGN_LEFT);
                cell3.setBackgroundColor(Color.lightGray);
                cell3.setBorder(Rectangle.LEFT + Rectangle.BOTTOM + Rectangle.RIGHT);
                tab2.addCell(cell3);
                cell3 = new PdfPCell(new Paragraph(messageSource.getMessage("acc.rem.193", null, RequestContextUtils.getLocale(request)), fontSmallBold));
                cell3.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell3.setBackgroundColor(Color.lightGray);
                cell3.setBorder(Rectangle.RIGHT + Rectangle.BOTTOM);
                tab2.addCell(cell3);
                cell3 = createCell(messageSource.getMessage("acc.rem.193", null, RequestContextUtils.getLocale(request))+" : ", fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                tab2.addCell(cell3);
                cell3 = createCell("", fontSmallBold, Element.ALIGN_LEFT, Rectangle.RIGHT, 0);
                tab2.addCell(cell3);
                cell3 = createCell(accname, fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT, 0);
                cell3.setPaddingLeft(50);
                tab2.addCell(cell3);
                cell3 = createCell(authHandlerDAOObj.getFormattedCurrency(amount, currencyid), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.RIGHT, 5);
                tab2.addCell(cell3);
                cell3 = createCell(address, fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT, 0);
                cell3.setPaddingLeft(50);
                tab2.addCell(cell3);
                cell3 = createCell("", fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.RIGHT, 0);
                tab2.addCell(cell3);

                for (int i = 0; i < 30; i++) {
                    cell3 = new PdfPCell(new Paragraph("", fontSmallRegular));
                    cell3.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
                    tab2.addCell(cell3);
                }
                cell3 = createCell(messageSource.getMessage("acc.numb.42", null, RequestContextUtils.getLocale(request)), fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                tab2.addCell(cell3);
                cell3 = createCell("", fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.RIGHT, 0);
                tab2.addCell(cell3);
                cell3 = createCell(mode==StaticValues.AUTONUM_PAYMENT?(pc.getPayDetail()==null?"Cash":pc.getPayDetail().getPaymentMethod().getMethodName()):(pc1.getPayDetail()==null?"Cash":pc1.getPayDetail().getPaymentMethod().getMethodName()), fontSmallRegular,Element.ALIGN_LEFT,Rectangle.LEFT + Rectangle.RIGHT,5);
                cell3.setPaddingLeft(50);
                tab2.addCell(cell3);
                cell3 = createCell("", fontSmallRegular, Element.ALIGN_CENTER, Rectangle.RIGHT, 0);
                tab2.addCell(cell3);
                cell3 = createCell(mode==StaticValues.AUTONUM_PAYMENT?(pc.getPayDetail()!=null?(pc.getPayDetail().getPaymentMethod().getMethodName().equals("Cheque")?"Cheque No : "+pc.getPayDetail().getCheque().getChequeNo()+" and Bank Name : "+pc.getPayDetail().getCheque().getBankName():""):""):(pc1.getPayDetail()!=null?(pc1.getPayDetail().getPaymentMethod().getMethodName().equals("Cheque")?"Cheque No : "+pc1.getPayDetail().getCheque().getChequeNo()+" and Bank Name : "+pc1.getPayDetail().getCheque().getBankName():""):""), fontSmallRegular,Element.ALIGN_LEFT,Rectangle.LEFT + Rectangle.RIGHT,5);
                cell3.setPaddingLeft(50);
                tab2.addCell(cell3);
                cell3 = createCell("", fontSmallRegular, Element.ALIGN_CENTER, Rectangle.RIGHT, 0);
                tab2.addCell(cell3);
                String str="";

                if(mode==StaticValues.AUTONUM_PAYMENT){
                  if( pc.getPayDetail()!=null)
                      if(pc.getPayDetail().getPaymentMethod().getDetailType()==2||pc.getPayDetail().getPaymentMethod().getDetailType()==1)
                          if(pc.getPayDetail().getCard()!=null)
                              str="Card No : "+(pc.getPayDetail().getCard().getCardNo())+" and Card Holder : "+pc.getPayDetail().getCard().getCardHolder();
                            else if(pc.getPayDetail().getCheque()!=null)
                                 str="Bank Name : "+(pc.getPayDetail().getCheque().getBankName())+"and Ref. No : "+(pc.getPayDetail().getCheque().getChequeNo());
                }else
                   if( pc1.getPayDetail()!=null)
                         if(pc1.getPayDetail().getPaymentMethod().getDetailType()==2||pc1.getPayDetail().getPaymentMethod().getDetailType()==1)
                            if(pc1.getPayDetail().getCard()!=null)
                                str="Card No : "+(pc1.getPayDetail().getCard().getCardNo())+" and Card Holder : "+pc1.getPayDetail().getCard().getCardHolder();
                            else if(pc1.getPayDetail().getCheque()!=null)
                                 str="Bank Name : "+(pc1.getPayDetail().getCheque().getBankName())+"and Ref. No : "+(pc1.getPayDetail().getCheque().getChequeNo());
                cell3 = createCell(str,fontSmallRegular,Element.ALIGN_LEFT,Rectangle.LEFT + Rectangle.RIGHT,5);
                cell3.setPaddingLeft(50);
                tab2.addCell(cell3);
                cell3 = createCell("", fontSmallRegular, Element.ALIGN_CENTER, Rectangle.RIGHT, 0);
                tab2.addCell(cell3);
                cell3 = createCell(messageSource.getMessage("acc.numb.41", null, RequestContextUtils.getLocale(request)), fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT, 5);
                tab2.addCell(cell3);
                cell3 = createCell("", fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.RIGHT, 5);
                tab2.addCell(cell3);
                cell3 = createCell(mode==StaticValues.AUTONUM_PAYMENT?pc.getMemo():pc1.getMemo(), fontSmallRegular,Element.ALIGN_LEFT,Rectangle.LEFT + Rectangle.RIGHT,5);
                cell3.setPaddingLeft(50);
                tab2.addCell(cell3);
                cell3 = createCell("", fontSmallRegular, Element.ALIGN_CENTER, Rectangle.RIGHT, 5);
                tab2.addCell(cell3);
                String netinword = EnglishNumberToWordsOjb.convert(Double.parseDouble(String.valueOf(amount)), currency);
                String currencyname = currency.getName();
                cell3 = createCell(messageSource.getMessage("acc.rem.177", null, RequestContextUtils.getLocale(request))+" " + currencyname + " " + netinword + " Only.", fontSmallRegular, Element.ALIGN_LEFT, Rectangle.LEFT + Rectangle.RIGHT + Rectangle.BOTTOM + Rectangle.TOP, 5);
                tab2.addCell(cell3);
                cell3 = createCell(authHandlerDAOObj.getFormattedCurrency(amount, currencyid), fontSmallRegular, Element.ALIGN_RIGHT, Rectangle.LEFT + Rectangle.RIGHT + Rectangle.BOTTOM + Rectangle.TOP, 5);
                tab2.addCell(cell3);
                PdfPCell mainCell5 = new PdfPCell(tab2);
                mainCell5.setBorder(1);
                mainTable.addCell(mainCell5);

                blankTable = addBlankLine(25);
                blankCell = new PdfPCell(blankTable);
                blankCell.setBorder(0);
                mainTable.addCell(blankCell);

                tab1 = new PdfPTable(2);
                tab1.setWidthPercentage(100);
                tab1.setWidths(new float[]{50, 50});
                cell3 = createCell(messageSource.getMessage("acc.numb.35", null, RequestContextUtils.getLocale(request)), fontTblMediumBold, Element.ALIGN_CENTER, 0, 0);
                tab1.addCell(cell3);
                cell3 = createCell(messageSource.getMessage("acc.numb.36", null, RequestContextUtils.getLocale(request)), fontTblMediumBold, Element.ALIGN_CENTER, 0, 0);
                tab1.addCell(cell3);
                PdfPCell mainCell6 = new PdfPCell(tab1);
                mainCell6.setPadding(5);
                mainCell6.setBorder(0);
                mainTable.addCell(mainCell6);

                blankTable = addBlankLine(15);
                blankCell = new PdfPCell(blankTable);
                blankCell.setBorder(0);
                mainTable.addCell(blankCell);

                tab2 = new PdfPTable(2);
                tab2.setWidthPercentage(100);
                tab2.setWidths(new float[]{50, 50});
                cell3 = createCell(messageSource.getMessage("acc.numb.39", null, RequestContextUtils.getLocale(request)), fontTblMediumBold, Element.ALIGN_CENTER, 0, 0);
                tab2.addCell(cell3);
                cell3 = createCell(messageSource.getMessage("acc.numb.40", null, RequestContextUtils.getLocale(request)), fontTblMediumBold, Element.ALIGN_CENTER, 0, 0);
                tab2.addCell(cell3);
                PdfPCell mainCell7 = new PdfPCell(tab2);
                mainCell7.setPadding(5);
                mainCell7.setBorder(0);
                mainTable.addCell(mainCell7);
                blankTable = addBlankLine(5);
                blankCell = new PdfPCell(blankTable);
                blankCell.setBorder(0);
                mainTable.addCell(blankCell);

            }

            document.add(mainTable);
            return baos;
        } catch (Exception ex) {
        	throw ServiceException.FAILURE("Export:"+ex.getMessage(), ex);
        } finally {
            if (document != null) {
                document.close();
            }
            if (writer != null) {
                writer.close();
            }
            if (baos != null) {
                baos.close();
            }
        }

    }

    private PdfPCell createCell(String string, Font fontTbl, int ALIGN_RIGHT, int i, int padd) {
        PdfPCell cell = new PdfPCell(new Paragraph(string, fontTbl));
        cell.setHorizontalAlignment(ALIGN_RIGHT);
        cell.setBorder(i);
        cell.setPadding(padd);
        return cell;
    }
    
    private PdfPCell createCell(String string, FontContext context, int ALIGN_RIGHT, int i, int padd) {
        PdfPCell cell = new PdfPCell(new Paragraph(fontFamilySelector.process(string, context)));
        cell.setHorizontalAlignment(ALIGN_RIGHT);
        cell.setBorder(i);
        cell.setPadding(padd);
        return cell;
    }

    private PdfPTable addCompanyLogo(String logoPath, Company com) {
        PdfPTable tab1 = new PdfPTable(1);
        imgPath = logoPath;
        PdfPCell imgCell = null;
        try {
            Image img = Image.getInstance(imgPath);
            imgCell = new PdfPCell(img);
        } catch (Exception e) {
            imgCell = new PdfPCell(new Paragraph(com.getCompanyName(), fontSmallRegular));
        }
        imgCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        imgCell.setBorder(0);
        tab1.addCell(imgCell);
        return tab1;
    }
     private PdfPTable addCompanyLogo(String logoPath, String comName) {
        PdfPTable tab1 = new PdfPTable(1);
        imgPath = logoPath;
        PdfPCell imgCell = null;
        try {
            Image img = Image.getInstance(imgPath);
            imgCell = new PdfPCell(img);
        } catch (Exception e) {
            imgCell = new PdfPCell(new Paragraph(comName, fontSmallRegular));
        }
        imgCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        imgCell.setBorder(0);
        tab1.addCell(imgCell);
        return tab1;
    }

    private void addHeaderFooter(Document document, PdfWriter writer) throws DocumentException, ServiceException {
        PdfPTable footer = new PdfPTable(1);
        PdfPCell footerSeparator = new PdfPCell(new Paragraph("THANK YOU FOR YOUR BUSINESS!", fontTblMediumBold));
        footerSeparator.setHorizontalAlignment(Element.ALIGN_CENTER);
        footerSeparator.setBorder(0);
        footer.addCell(footerSeparator);

        try {
            Rectangle page = document.getPageSize();
            footer.setTotalWidth(page.getWidth() - document.leftMargin());
            footer.writeSelectedRows(0, -1, document.leftMargin(), document.bottomMargin(), writer.getDirectContent());
        } catch (Exception e) {
            throw new ExceptionConverter(e);
        }
    }

    public PdfPTable getCompanyInfo(String com[]) {
        PdfPTable tab1 = new PdfPTable(1);
        tab1.setHorizontalAlignment(Element.ALIGN_CENTER);
        PdfPCell cell = new PdfPCell(new Paragraph(com[0], fontMediumBold));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setBorder(0);
        tab1.addCell(cell);
        for (int i = 1; i < com.length; i++) {
            cell = new PdfPCell(new Paragraph(com[i], fontTblMediumBold));
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell.setBorder(0);
            tab1.addCell(cell);
        }
        return tab1;
    }

    private PdfPTable addBlankLine(int count) {
        PdfPTable table = new PdfPTable(1);
        PdfPCell cell = null;
        for (int i = 0; i < count; i++) {
            cell = new PdfPCell(new Paragraph("", fontTblMediumBold));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(3);
            cell.setBorder(0);
            table.addCell(cell);
        }
        return table;
    }

    private PdfPCell calculateDiscount(Double disc, String currencyid) {
        PdfPCell cell = null;
        if (disc == null) {
            cell = new PdfPCell(new Paragraph("--", fontSmallRegular));
        } else {
            cell = new PdfPCell(new Paragraph(authHandlerDAOObj.getFormattedCurrency(disc, currencyid), fontSmallRegular));
        }
        return cell;
    }
    
    private PdfPCell calculateDiscount(Discount disc, String currencyid) {
        PdfPCell cell = null;
        if (disc == null) {
            cell = new PdfPCell(new Paragraph("--", fontSmallRegular));
        } else if (disc.isInPercent()) {
            cell = new PdfPCell(new Paragraph(authHandlerDAOObj.getFormattedCurrency(disc.getDiscountValue(), currencyid), fontSmallRegular));
        } else {
            cell = new PdfPCell(new Paragraph(authHandlerDAOObj.getFormattedCurrency(disc.getDiscountValue(), currencyid), fontSmallRegular));
        }
        return cell;
    }

    private PdfPCell getCharges(JournalEntryDetail jEntry, String currencyid) {
        PdfPCell cell = null;
        if (jEntry == null) {
            cell = new PdfPCell(new Paragraph("--", fontSmallBold));
        } else {
            cell = new PdfPCell(new Paragraph(authHandlerDAOObj.getFormattedCurrency(jEntry.getAmount(), currencyid), fontSmallRegular));
        }
        return cell;
    }

    public String numberFormatter(double values, String compSymbol) {
        NumberFormat numberFormatter;
        java.util.Locale currentLocale = java.util.Locale.US;
        numberFormatter = NumberFormat.getNumberInstance(currentLocale);
        numberFormatter.setMinimumFractionDigits(2);
        numberFormatter.setMaximumFractionDigits(2);
        return (compSymbol + numberFormatter.format(values));
    }

    public void writeDataToFile(String filename, ByteArrayOutputStream baos,
            HttpServletResponse response) throws IOException {

        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        response.setContentType("application/octet-stream");
        response.setContentLength(baos.size());
        response.getOutputStream().write(baos.toByteArray());
        response.getOutputStream().flush();
        response.getOutputStream().close();
    }

    public PdfPTable getBlankTable() throws DocumentException{
        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{5, 20, 15, 15, 15,13, 17});
        return table;
    }

    public void addTableRow(PdfPTable container, PdfPTable table) {
        PdfPCell tableRow = new PdfPCell(table);
        tableRow.setBorder(0);
        tableRow.setPaddingRight(10);
        tableRow.setPaddingLeft(10);
        container.addCell(tableRow);
    }
    public class EnglishNumberToWords {

        private final String[] tensNames = {
            "", " Ten", " Twenty", " Thirty", " Forty", " Fifty", " Sixty", " Seventy", " Eighty", " Ninety"
        };
        private final String[] numNames = {
            "", " One", " Two", " Three", " Four", " Five", " Six", " Seven", " Eight", " Nine", " Ten", " Eleven", " Twelve",
            " Thirteen", " Fourteen", " Fifteen", " Sixteen", " Seventeen", " Eighteen", " Nineteen"
        };

        private String convertLessThanOneThousand(int number) {
            String soFar;
            if (number % 100 < 20) {
                soFar = numNames[number % 100];
                number /= 100;
            } else {
                soFar = numNames[number % 10];
                number /= 10;
                soFar = tensNames[number % 10] + soFar;
                number /= 10;
            }
            if (number == 0) {
                return soFar;
            }
            return numNames[number] + " Hundred" + soFar;
        }

        private String convertLessOne(int number, KWLCurrency currency) {
            String soFar;
            String val = currency.getAfterDecimalName();
            if (number % 100 < 20) {
                soFar = numNames[number % 100];
                number /= 100;
            } else {
                soFar = numNames[number % 10];
                number /= 10;
                soFar = tensNames[number % 10] + soFar;
                number /= 10;
            }
            if (number == 0) {
                return " And " + soFar +" "+ val;
            }
            return " And " + numNames[number] +" "+ val + soFar;
        }

        public String convert(Double number, KWLCurrency currency) {
            if (number == 0) {
                return "Zero";
            }
            String snumber = Double.toString(number);
            String mask = "000000000000.00";
            DecimalFormat df = new DecimalFormat(mask);
            snumber = df.format(number);
            int billions = Integer.parseInt(snumber.substring(0, 3));
            int millions = Integer.parseInt(snumber.substring(3, 6));
            int hundredThousands = Integer.parseInt(snumber.substring(6, 9));
            int thousands = Integer.parseInt(snumber.substring(9, 12));
            int fractions = Integer.parseInt(snumber.substring(13, 15));
            String tradBillions;
            switch (billions) {
                case 0:
                    tradBillions = "";
                    break;
                case 1:
                    tradBillions = convertLessThanOneThousand(billions) + " Billion ";
                    break;
                default:
                    tradBillions = convertLessThanOneThousand(billions) + " Billion ";
            }
            String result = tradBillions;

            String tradMillions;
            switch (millions) {
                case 0:
                    tradMillions = "";
                    break;
                case 1:
                    tradMillions = convertLessThanOneThousand(millions) + " Million ";
                    break;
                default:
                    tradMillions = convertLessThanOneThousand(millions) + " Million ";
            }
            result = result + tradMillions;

            String tradHundredThousands;
            switch (hundredThousands) {
                case 0:
                    tradHundredThousands = "";
                    break;
                case 1:
                    tradHundredThousands = "One Thousand ";
                    break;
                default:
                    tradHundredThousands = convertLessThanOneThousand(hundredThousands) + " Thousand ";
            }
            result = result + tradHundredThousands;
            String tradThousand;
            tradThousand = convertLessThanOneThousand(thousands);
            result = result + tradThousand;
            String paises;
            switch (fractions) {
                case 0:
                    paises = "";
                    break;
                default:
                    paises = convertLessOne(fractions, currency);
            }
            result = result + paises; //to be done later
            result = result.replaceAll("^\\s+", "").replaceAll("\\b\\s{2,}\\b", " ");
//            result = result.substring(0, 1).toUpperCase() + result.substring(1).toLowerCase(); // Make first letter of operand capital.
            return result;
        }
    }
    
    public ByteArrayOutputStream exportCashFlow(JSONObject jobj,String logoPath,String comName, HttpServletRequest request)  throws DocumentException, ServiceException, IOException {
        ByteArrayOutputStream baos = null;
        double total = 0;
        Document document = null;
        PdfWriter writer = null;
        try {

            baos = new ByteArrayOutputStream();
            document = new Document(PageSize.A4, 15, 15, 15, 15);
            writer = PdfWriter.getInstance(document, baos);
            document.open();
            PdfPTable mainTable = new PdfPTable(1);
            mainTable.setWidthPercentage(100);

            PdfPTable tab1 = null;
            Rectangle page = document.getPageSize();

            int bmargin = 15;  //border margin
            PdfContentByte cb = writer.getDirectContent();
            cb.rectangle(bmargin, bmargin, page.getWidth() - bmargin * 2, page.getHeight() - bmargin * 2);
            cb.setColorStroke(Color.WHITE);
            cb.stroke();

            PdfPTable table1 = new PdfPTable(2);
            table1.setWidthPercentage(100);
            table1.setWidths(new float[]{30,20});

            PdfPCell blankCell = new PdfPCell();
            blankCell.setBorder(0);
            tab1 = addCompanyLogo(logoPath, comName);
            PdfPCell cell1 = new PdfPCell(tab1);
            cell1.setBorder(0);
            table1.addCell(cell1);
            table1.addCell(blankCell);
            table1.addCell(blankCell);
            table1.addCell(blankCell);
            
            PdfPCell cell2 = new PdfPCell(new Paragraph(comName, fontSmallRegular));
            cell2.setBorder(0);
            table1.addCell(cell2);
            table1.addCell(blankCell);
            table1.addCell(blankCell);
            table1.addCell(blankCell);

            PdfPCell headerCell =  createCell(messageSource.getMessage("acc.dashboard.cashFlowStatement", null, RequestContextUtils.getLocale(request)), fontMediumBold, Element.ALIGN_LEFT, 0, 5);
            headerCell.setBorder(0);
            table1.addCell(headerCell);
            table1.addCell(blankCell);
            table1.addCell(blankCell);
            table1.addCell(blankCell);

            table1.addCell(blankCell);
            table1.addCell(blankCell);
            table1.addCell(blankCell);
            table1.addCell(blankCell);
            
            JSONArray ObjArr = jobj.getJSONArray("data");
            
            PdfPCell HeaderCell1 = createCell(messageSource.getMessage("acc.report.2", null, RequestContextUtils.getLocale(request)), fontSmallBold, Element.ALIGN_LEFT, 0, 5);
            HeaderCell1.setBorderWidthLeft(1);
            HeaderCell1.setBorderWidthBottom(1);
            HeaderCell1.setBorderWidthTop(1);
            HeaderCell1.setBorderWidthRight(1);
            PdfPCell HeaderCell2 = createCell(messageSource.getMessage("acc.ra.value", null, RequestContextUtils.getLocale(request)), fontSmallBold, Element.ALIGN_RIGHT, 0, 5);
            HeaderCell2.setBorderWidthBottom(1);
            HeaderCell2.setBorderWidthTop(1);
            HeaderCell2.setBorderWidthRight(1);
            
            table1.addCell(HeaderCell1);
            table1.addCell(HeaderCell2);
            PdfPCell objCell1 = null;
            PdfPCell objCell2 = null;
            int objArrLength = ObjArr.length();
            for(int i=0;i<objArrLength;i++){
                JSONObject leftObj = ObjArr.getJSONObject(i);
                if(leftObj.has("lfmt")  &&  leftObj.getString("lfmt").equals("title"))
                	objCell1 = createBalanceSheetCell(leftObj.getString("lname"), fontSmallBold, Element.ALIGN_CENTER, 0, 5,0);
                else
                	objCell1 = createBalanceSheetCell(leftObj.getString("lname"), fontSmallRegular, Element.ALIGN_LEFT, 0, 5,0);
                objCell2 = createBalanceSheetCell(leftObj.getString("lvalue"), fontSmallRegular, Element.ALIGN_RIGHT, 0, 0,0);
                objCell1.setBorderWidthLeft(1);
                objCell2.setBorderWidthRight(1);
                objCell1.setBorderWidthRight(1);
                objCell1.setBorderWidthBottom(1);
                objCell2.setBorderWidthBottom(1);
                if(i!=(objArrLength-1)){
                    table1.addCell(objCell1);
                    table1.addCell(objCell2);
                }
            }
            objCell1.setBorderWidthBottom(1);
            objCell2.setBorderWidthBottom(1);
            table1.addCell(objCell1);
            table1.addCell(objCell2);
            
            PdfPCell mainCell11 = new PdfPCell(table1);
            mainCell11.setBorder(0);
            mainCell11.setPadding(10);
            mainTable.addCell(mainCell11);

            document.add(mainTable);
        } catch (Exception ex) {
            return null;
        } finally {
            if (document != null) {
                document.close();
            }
            if (writer != null) {
                writer.close();
            }
            if (baos != null) {
                baos.close();
            }
        }
           return baos;
    }
}
