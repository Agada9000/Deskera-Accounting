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
package com.krawler.esp.servlets;
import com.krawler.common.admin.Company;
import com.krawler.common.admin.KWLCurrency;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.lowagie.text.DocumentException;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.hibernate.Session;
import com.krawler.esp.handlers.AuthHandler;
import com.krawler.esp.handlers.ProfileHandler;
import com.krawler.esp.hibernate.impl.HibernateUtil;
import com.krawler.hql.accounting.Account;
import com.krawler.hql.accounting.BillingInvoice;
import com.krawler.hql.accounting.BillingInvoiceDetail;
import com.krawler.hql.accounting.BillingReceipt;
import com.krawler.hql.accounting.Discount;
import com.krawler.hql.accounting.GoodsReceipt;
import com.krawler.hql.accounting.GoodsReceiptDetail;
import com.krawler.hql.accounting.Invoice;
import com.krawler.hql.accounting.InvoiceDetail;
import com.krawler.hql.accounting.JournalEntryDetail;
import com.krawler.hql.accounting.Payment;
import com.krawler.hql.accounting.Receipt;
import com.krawler.hql.accounting.StaticValues;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.ExceptionConverter;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Iterator;
import org.hibernate.HibernateException;

public class ExportServlet extends HttpServlet {

//    private static final long serialVersionUID = -763555229410947890L;
//    private static Font fontSmallRegular = FontFactory.getFont("Times New Roman", 10, Font.NORMAL, Color.BLACK);
//    private static Font fontSmallBold = FontFactory.getFont("Times New Roman", 10, Font.BOLD, Color.BLACK);
//    private static Font fontMediumBold = FontFactory.getFont("Times New Roman", 12, Font.BOLD, Color.BLACK);
//    private static Font fontTblMediumBold = FontFactory.getFont("Times New Roman", 10,Font.NORMAL, Color.GRAY);
//    private static Font fontTbl = FontFactory.getFont("Times New Roman", 20, Font.NORMAL, Color.GRAY);
//    private static String imgPath = "";
//
//    protected void processRequest(HttpServletRequest request,
//            HttpServletResponse response) throws ServletException, IOException {
//        response.setContentType("text/html;charset=UTF-8");
//        Session session = HibernateUtil.getCurrentSession();
//        try {
//            String filename = request.getParameter("filename")+".pdf";
//            ByteArrayOutputStream baos = null;
//            double amount = Double.parseDouble(request.getParameter("amount"));
//            int mode = Integer.parseInt(request.getParameter("mode"));
//            String billid = request.getParameter("bills");
//            String cust = request.getParameter("customer");
//            String accname = request.getParameter("accname");
//            String address = request.getParameter("address");
//            String logoPath = ProfileImageServlet.getProfileImagePath(request, true, null);
//            Account account=(Account)session.get(Account.class, request.getParameter("personid"));
//            String currencyid = account==null?AuthHandler.getCurrencyID(request):account.getCurrency().getCurrencyID();
//            DateFormat formatter = AuthHandler.getUserDateFormatter(session, AuthHandler.getDateFormatID(request), AuthHandler.getTimeZoneDifference(request), true);
//            baos = createPdf(session, currencyid, billid, formatter, mode, amount, logoPath, cust,accname,address);
//            if (baos != null) {
//                writeDataToFile(filename, baos, response);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            HibernateUtil.closeSession(session);
//        }
//    }
//
//    public static ByteArrayOutputStream createPdf(Session session, String currencyid, String billid, DateFormat formatter, int mode, double amount, String logoPath, String customer,String accname,String address) throws DocumentException, ServiceException, IOException {
//        ByteArrayOutputStream baos = null;
//        double total = 0;
//        Document document = null;
//        PdfWriter writer = null;
//        try {
//            baos = new ByteArrayOutputStream();
//            document = new Document(PageSize.A4, 15, 15, 15, 15);
//            writer = PdfWriter.getInstance(document, baos);
//            document.open();
//            PdfPTable mainTable = new PdfPTable(1);
//            mainTable.setWidthPercentage(100);
//
//            PdfPTable blankTable = null;
//            PdfPCell blankCell = null;
//            PdfPTable tab1 = null;
//            PdfPTable tab2 = null;
//            PdfPTable tab3 = null;
//            Rectangle page = document.getPageSize();
//
//            int bmargin = 15;  //border margin
//            PdfContentByte cb = writer.getDirectContent();
//            cb.rectangle(bmargin, bmargin, page.getWidth() - bmargin * 2, page.getHeight() - bmargin * 2);
//            cb.setColorStroke(Color.WHITE);
//            cb.stroke();
//
//            if (mode == StaticValues.AUTONUM_INVOICE||mode==StaticValues.AUTONUM_BILLINGINVOICE) {
//                addHeaderFooter(document,writer);
//                Invoice inv=null;
//                BillingInvoice inv1=null;
//                if(mode!=StaticValues.AUTONUM_BILLINGINVOICE)
//                     inv = (Invoice) session.get(Invoice.class, billid);
//                else
//                    inv1=(BillingInvoice)session.get(BillingInvoice.class,billid);
//
//                Company com = mode!=StaticValues.AUTONUM_BILLINGINVOICE?inv.getCompany():inv1.getCompany();
//                String company[] = new String[4];
//                company[0] = com.getCompanyName();
//                company[1] = com.getAddress();
//                company[2] = com.getEmailID();
//                company[3] = com.getPhoneNumber();
//
//                PdfPTable table1 = new PdfPTable(2);
//                table1.setWidthPercentage(100);
//                table1.setWidths(new float[]{50, 50});
//
//                tab1 = addCompanyLogo(logoPath,com);
//                tab2 = new PdfPTable(1);
//                PdfPCell invCell=null;
//
//                invCell=createCell("INVOICE",fontTbl,Element.ALIGN_RIGHT,0,5);
//                tab2.addCell(invCell);
//
//                PdfPCell cell1 = new PdfPCell(tab1);
//                cell1.setBorder(0);
//                table1.addCell(cell1);
//                PdfPCell cel2 = new PdfPCell(tab2);
//                cel2.setBorder(0);
//                table1.addCell(cel2);
//
//                PdfPCell mainCell11 = new PdfPCell(table1);
//                mainCell11.setBorder(0);
//                mainCell11.setPadding(10);
//                mainTable.addCell(mainCell11);
//
//
//                PdfPTable userTable2 = new PdfPTable(2);
//                userTable2.setWidthPercentage(100);
//                userTable2.setWidths(new float[]{60, 40});
//
//                tab3 = getCompanyInfo(company);
//
//                PdfPTable tab4 = new PdfPTable(2);
//                tab4.setWidthPercentage(100);
//                tab4.setWidths(new float[]{30, 70});
//
//                PdfPCell cell2=createCell("INVOICE #",fontSmallBold,Element.ALIGN_LEFT,0,5);
//                tab4.addCell(cell2);
//                String invno=mode!=StaticValues.AUTONUM_BILLINGINVOICE?inv.getInvoiceNumber() : inv1.getBillingInvoiceNumber();
//                cell2=createCell(": "+invno,fontSmallRegular,Element.ALIGN_LEFT,0,5);
//                tab4.addCell(cell2);
//                cell2=createCell("DATE  ",fontSmallBold,Element.ALIGN_LEFT,0,5);
//                tab4.addCell(cell2);
//                cell2=createCell(": "+formatter.format(mode!=StaticValues.AUTONUM_BILLINGINVOICE ?inv.getJournalEntry().getEntryDate():inv1.getJournalEntry().getEntryDate()),fontSmallRegular,Element.ALIGN_LEFT,0,5);
//                tab4.addCell(cell2);
//
//                cell1 = new PdfPCell(tab3);
//                cell1.setBorder(0);
//                userTable2.addCell(cell1);
//                cel2 = new PdfPCell(tab4);
//                cel2.setBorder(0);
//                userTable2.addCell(cel2);
//
//                PdfPCell mainCell12 = new PdfPCell(userTable2);
//                mainCell12.setBorder(0);
//                mainCell12.setPadding(10);
//                mainTable.addCell(mainCell12);
//
//
//                PdfPTable tab5 = new PdfPTable(2);
//                tab5.setWidthPercentage(100);
//                tab5.setWidths(new float[]{10, 90});
//                PdfPCell cell3 = createCell("To , ",fontSmallBold,Element.ALIGN_LEFT,0,5);
//                tab5.addCell(cell3);
//                cell3 = createCell("",fontSmallBold,Element.ALIGN_LEFT,0,0);
//                tab5.addCell(cell3);
//                cell3 = createCell("",fontSmallBold,Element.ALIGN_LEFT,0,0);
//                tab5.addCell(cell3);
//                cell3=createCell(mode!=StaticValues.AUTONUM_BILLINGINVOICE?inv.getCustomerEntry().getAccount().getName():inv1.getCustomerEntry().getAccount().getName(), fontSmallRegular,Element.ALIGN_LEFT,0,5);
//                tab5.addCell(cell3);
//                cell3 = createCell("",fontSmallBold,Element.ALIGN_LEFT,0,0);
//                tab5.addCell(cell3);
//                cell3 = createCell(mode!=StaticValues.AUTONUM_BILLINGINVOICE?inv.getShipTo():inv1.getShipTo(), fontSmallRegular,Element.ALIGN_LEFT,0,5);
//                tab5.addCell(cell3);
//
//                PdfPCell mainCell14 = new PdfPCell(tab5);
//                mainCell14.setBorder(0);
//                mainCell14.setPadding(10);
//                mainTable.addCell(mainCell14);
//
//
//                String[] header = {"PRODUCT DESCRIPTION","QUANTITY", "UNIT PRICE", "DISCOUNT", "LINE TOTAL"};
//                PdfPTable table = new PdfPTable(5);
//                table.setWidthPercentage(100);
//                table.setWidths(new float[]{ 30, 15, 15, 20, 20});
//                PdfPCell invcell = null;
//                for (int i = 0; i < header.length; i++) {
//                    invcell = new PdfPCell(new Paragraph(header[i], fontSmallBold));
//                    invcell.setHorizontalAlignment(Element.ALIGN_CENTER);
//                    invcell.setBackgroundColor(Color.LIGHT_GRAY);
//                    invCell.setBorder(0);
//                    invcell.setPadding(3);
//                    table.addCell(invcell);
//                }
//
//                Iterator itr = mode!=StaticValues.AUTONUM_BILLINGINVOICE?inv.getRows().iterator():inv1.getRows().iterator();
//                InvoiceDetail row=null;
//                BillingInvoiceDetail row1=null;
//                while (itr.hasNext()) {
//                    if(mode!=StaticValues.AUTONUM_BILLINGINVOICE)
//                        row = (InvoiceDetail) itr.next();
//                    else
//                        row1=(BillingInvoiceDetail) itr.next();
//                    invcell= createCell(mode!=StaticValues.AUTONUM_BILLINGINVOICE?row.getInventory().getProduct().getName():row1.getProductDetail(), fontSmallRegular,Element.ALIGN_LEFT,Rectangle.LEFT + Rectangle.RIGHT,5);
//                    table.addCell(invcell);
//                    invcell= createCell(Integer.toString(mode!=StaticValues.AUTONUM_BILLINGINVOICE?row.getInventory().getQuantity():row1.getQuantity()) + " " +(mode!=StaticValues.AUTONUM_BILLINGINVOICE?row.getInventory().getProduct().getUnitOfMeasure().getName():""), fontSmallRegular,Element.ALIGN_CENTER,Rectangle.LEFT + Rectangle.RIGHT,5);
//                    table.addCell(invcell);
//                    invcell= createCell(ProfileHandler.getFormattedCurrency(mode!=StaticValues.AUTONUM_BILLINGINVOICE?row.getRate():row1.getRate(), currencyid, session), fontSmallRegular,Element.ALIGN_RIGHT,Rectangle.LEFT + Rectangle.RIGHT,5);
//                    table.addCell(invcell);
//                    invcell = calculateDiscount(mode!=StaticValues.AUTONUM_BILLINGINVOICE?row.getDiscount():row1.getDiscount(), currencyid, session);
//                    invcell.setHorizontalAlignment(Element.ALIGN_RIGHT);
//                    invcell.setPadding(5);
//                    invcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
//                    table.addCell(invcell);
//                    double amount1 = mode!=StaticValues.AUTONUM_BILLINGINVOICE?(row.getRate() * row.getInventory().getQuantity()):(row1.getRate() * row1.getQuantity());
//                    Discount disc=mode!=StaticValues.AUTONUM_BILLINGINVOICE?row.getDiscount():row1.getDiscount();
//                    if (disc!=null) {
//                        amount1 -= mode!=StaticValues.AUTONUM_BILLINGINVOICE?(row.getDiscount().getDiscountValue()):(row1.getDiscount().getDiscountValue());
//                    }
//                    total += amount1;
//                    invcell= createCell(ProfileHandler.getFormattedCurrency(amount1, currencyid, session), fontSmallRegular,Element.ALIGN_RIGHT,Rectangle.LEFT + Rectangle.RIGHT,5);
//                    table.addCell(invcell);
//                }
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
//                cell3 = createCell("SUB TOTAL", fontSmallBold,Element.ALIGN_LEFT,Rectangle.TOP,5);
//                table.addCell(cell3);
//                cell3 = createCell(ProfileHandler.getFormattedCurrency(total, currencyid, session), fontSmallRegular,Element.ALIGN_RIGHT,15,5);
//                table.addCell(cell3);
//                for (int i = 0; i < 3; i++) {
//                    invcell = new PdfPCell(new Paragraph("", fontSmallRegular));
//                    invcell.setHorizontalAlignment(Element.ALIGN_CENTER);
//                    invcell.setPadding(5);
//                    invcell.setBorder(0);
//                    table.addCell(invcell);
//                }
//                cell3 = createCell("DISCOUNT (-)", fontSmallBold,Element.ALIGN_LEFT,0,5);
//                table.addCell(cell3);
//                invcell = calculateDiscount(mode!=StaticValues.AUTONUM_BILLINGINVOICE?inv.getDiscount():inv1.getDiscount(), currencyid, session);
//                invcell.setHorizontalAlignment(Element.ALIGN_RIGHT);
//                invcell.setPadding(5);
//                table.addCell(invcell);
//                for (int i = 0; i < 3; i++) {
//                    invcell = new PdfPCell(new Paragraph("", fontSmallRegular));
//                    invcell.setHorizontalAlignment(Element.ALIGN_CENTER);
//                    invcell.setPadding(5);
//                    invcell.setBorder(0);
//                    table.addCell(invcell);
//                }
//                cell3 = createCell("TAX (+)", fontSmallBold,Element.ALIGN_LEFT,0,5);
//                table.addCell(cell3);
//                cell3 = createCell(ProfileHandler.getFormattedCurrency(mode!=StaticValues.AUTONUM_BILLINGINVOICE?(inv.getTaxEntry()!=null?inv.getTaxEntry().getAmount():0):(inv1.getTaxEntry()!=null?inv1.getTaxEntry().getAmount():0),currencyid,session), fontSmallRegular,Element.ALIGN_RIGHT,15,5);
//                table.addCell(cell3);
//                for (int i = 0; i < 3; i++) {
//                    invcell = new PdfPCell(new Paragraph("", fontSmallRegular));
//                    invcell.setHorizontalAlignment(Element.ALIGN_CENTER);
//                    invcell.setPadding(5);
//                    invcell.setBorder(0);
//                    table.addCell(invcell);
//                }
//                cell3 = createCell("TOTAL", fontSmallBold,Element.ALIGN_LEFT,0,5);
//                table.addCell(cell3);
//                cell3 = createCell(ProfileHandler.getFormattedCurrency(mode!=StaticValues.AUTONUM_BILLINGINVOICE?(inv.getCustomerEntry().getAmount()):(inv1.getCustomerEntry().getAmount()), currencyid, session), fontSmallRegular,Element.ALIGN_RIGHT,15,5);
//                table.addCell(cell3);
//
//                PdfPCell mainCell5 = new PdfPCell(table);
//                mainCell5.setBorder(0);
//                mainCell5.setPadding(10);
//                mainTable.addCell(mainCell5);
//
//                PdfPTable helpTable = new PdfPTable(1);
//                helpTable.setWidthPercentage(100);
//                Chunk chunk1 = new Chunk("Memo:  ",fontSmallBold);
//                Chunk chunk2 = new Chunk(mode!=StaticValues.AUTONUM_BILLINGINVOICE?inv.getMemo():inv1.getMemo(),fontSmallRegular);
//                Phrase phrase1 = new Phrase();
//                phrase1.add(chunk1);
//                phrase1.add(chunk2);
//                Paragraph p = new Paragraph();
//                p.add(phrase1);
//                PdfPCell pcell = new PdfPCell(p);
//                pcell.setBorder(0);
//                pcell.setPadding(10);
//                helpTable.addCell(pcell);
//
//                PdfPCell mainCell61 = new PdfPCell(helpTable);
//                mainCell61.setBorder(0);
//                mainTable.addCell(mainCell61);
//
//             } if (mode == StaticValues.AUTONUM_GOODSRECEIPT) {
//                addHeaderFooter(document,writer);
//                GoodsReceipt gr=null;
//                gr=(GoodsReceipt)session.get(GoodsReceipt.class,billid);
//
//                Company com =gr.getCompany();
//                String company[] = new String[4];
//                company[0] = com.getCompanyName();
//                company[1] = com.getAddress();
//                company[2] = com.getEmailID();
//                company[3] = com.getPhoneNumber();
//
//                PdfPTable table1 = new PdfPTable(2);
//                table1.setWidthPercentage(100);
//                table1.setWidths(new float[]{50, 50});
//
//                tab1 = addCompanyLogo(logoPath,com);
//                tab2 = new PdfPTable(1);
//                PdfPCell invCell=null;
//
//                invCell=createCell("GOODS RECEIPT",fontTbl,Element.ALIGN_RIGHT,0,5);
//                tab2.addCell(invCell);
//
//                PdfPCell cell1 = new PdfPCell(tab1);
//                cell1.setBorder(0);
//                table1.addCell(cell1);
//                PdfPCell cel2 = new PdfPCell(tab2);
//                cel2.setBorder(0);
//                table1.addCell(cel2);
//
//                PdfPCell mainCell11 = new PdfPCell(table1);
//                mainCell11.setBorder(0);
//                mainTable.addCell(mainCell11);
//
//                blankTable = addBlankLine(3);
//                blankCell = new PdfPCell(blankTable);
//                blankCell.setBorder(0);
//                mainTable.addCell(blankCell);
//
//                PdfPTable userTable2 = new PdfPTable(2);
//                userTable2.setWidthPercentage(100);
//                userTable2.setWidths(new float[]{60, 40});
//
//                tab3 = getCompanyInfo(company);
//
//                PdfPTable tab4 = new PdfPTable(2);
//                tab4.setWidthPercentage(100);
//                tab4.setWidths(new float[]{30, 70});
//
//                PdfPCell cell2=createCell("GOODS RECEIPT #",fontSmallBold,Element.ALIGN_LEFT,0,5);
//                tab4.addCell(cell2);
//                String grno=gr.getGoodsReceiptNumber() ;
//                cell2=createCell(": "+grno,fontSmallRegular,Element.ALIGN_LEFT,0,5);
//                tab4.addCell(cell2);
//                cell2=createCell("DATE  ",fontSmallBold,Element.ALIGN_LEFT,0,5);
//                tab4.addCell(cell2);
//                cell2=createCell(": "+formatter.format(gr.getJournalEntry().getEntryDate()),fontSmallRegular,Element.ALIGN_LEFT,0,5);
//                tab4.addCell(cell2);
//
//                cell1 = new PdfPCell(tab3);
//                cell1.setBorder(0);
//                userTable2.addCell(cell1);
//                cel2 = new PdfPCell(tab4);
//                cel2.setBorder(0);
//                userTable2.addCell(cel2);
//
//                PdfPCell mainCell12 = new PdfPCell(userTable2);
//                mainCell12.setBorder(0);
//                mainTable.addCell(mainCell12);
//
//                blankTable = addBlankLine(3);
//                blankCell = new PdfPCell(blankTable);
//                blankCell.setBorder(0);
//                mainTable.addCell(blankCell);
//
//                PdfPTable tab5 = new PdfPTable(2);
//                tab5.setWidthPercentage(100);
//                tab5.setWidths(new float[]{10, 90});
//                PdfPCell cell3 = createCell("FROM , ",fontSmallBold,Element.ALIGN_LEFT,0,5);
//                tab5.addCell(cell3);
//                cell3 = createCell("",fontSmallBold,Element.ALIGN_LEFT,0,0);
//                tab5.addCell(cell3);
//                cell3 = createCell("",fontSmallBold,Element.ALIGN_LEFT,0,0);
//                tab5.addCell(cell3);
//                cell3=createCell(gr.getVendorEntry().getAccount().getName(), fontSmallRegular,Element.ALIGN_LEFT,0,5);
//                tab5.addCell(cell3);
//                cell3 = createCell("",fontSmallBold,Element.ALIGN_LEFT,0,0);
//                tab5.addCell(cell3);
//                cell3 = createCell(gr.getBillFrom(), fontSmallRegular,Element.ALIGN_LEFT,0,5);
//                tab5.addCell(cell3);
//
//                PdfPCell mainCell14 = new PdfPCell(tab5);
//                mainCell14.setBorder(0);
//                mainTable.addCell(mainCell14);
//
//                blankTable = addBlankLine(3);
//                blankCell = new PdfPCell(blankTable);
//                blankCell.setBorder(0);
//                mainTable.addCell(blankCell);
//
//                String[] header = {"PRODUCT DESCRIPTION","QUANTITY", "UNIT PRICE", "DISCOUNT", "LINE TOTAL"};
//                PdfPTable table = new PdfPTable(5);
//                table.setWidthPercentage(100);
//                table.setWidths(new float[]{ 30, 15, 15, 20, 20});
//                PdfPCell grcell = null;
//                for (int i = 0; i < header.length; i++) {
//                    grcell = new PdfPCell(new Paragraph(header[i], fontSmallBold));
//                    grcell.setHorizontalAlignment(Element.ALIGN_CENTER);
//                    grcell.setBackgroundColor(Color.LIGHT_GRAY);
//                    grcell.setBorder(0);
//                    grcell.setPadding(3);
//                    table.addCell(grcell);
//                }
//
//                Iterator itr =gr.getRows().iterator();
//                GoodsReceiptDetail row=null;
//                while (itr.hasNext()) {
//                        row=(GoodsReceiptDetail) itr.next();
//                    grcell= createCell(row.getInventory().getProduct().getName(), fontSmallRegular,Element.ALIGN_LEFT,Rectangle.LEFT + Rectangle.RIGHT,5);
//                    table.addCell(grcell);
//                    grcell= createCell(Integer.toString(row.getInventory().getQuantity()) + " " +(mode!=StaticValues.AUTONUM_BILLINGINVOICE?row.getInventory().getProduct().getUnitOfMeasure().getName():""), fontSmallRegular,Element.ALIGN_CENTER,Rectangle.LEFT + Rectangle.RIGHT,5);
//                    table.addCell(grcell);
//                    grcell= createCell(ProfileHandler.getFormattedCurrency(row.getRate(), currencyid, session), fontSmallRegular,Element.ALIGN_RIGHT,Rectangle.LEFT + Rectangle.RIGHT,5);
//                    table.addCell(grcell);
//                    grcell = calculateDiscount(row.getDiscount(), currencyid, session);
//                    grcell.setHorizontalAlignment(Element.ALIGN_RIGHT);
//                    grcell.setPadding(5);
//                    grcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
//                    table.addCell(grcell);
//                    double amount1 = row.getRate() * row.getInventory().getQuantity();
//                    Discount disc=row.getDiscount();
//                    if (disc!=null) {
//                        amount1 -=row.getDiscount().getDiscountValue();
//                    }
//                    total += amount1;
//                    grcell= createCell(ProfileHandler.getFormattedCurrency(amount1, currencyid, session), fontSmallRegular,Element.ALIGN_RIGHT,Rectangle.LEFT + Rectangle.RIGHT,5);
//                    table.addCell(grcell);
//                }
//                for (int j = 0; j < 70; j++) {
//                    grcell = new PdfPCell(new Paragraph("", fontSmallRegular));
//                    grcell.setHorizontalAlignment(Element.ALIGN_CENTER);
//                    grcell.setBorder(Rectangle.LEFT + Rectangle.RIGHT);
//                    table.addCell(grcell);
//                }
//                for (int i = 0; i < 3; i++) {
//                    grcell = new PdfPCell(new Paragraph("", fontSmallRegular));
//                    grcell.setHorizontalAlignment(Element.ALIGN_CENTER);
//                    grcell.setBorder(Rectangle.TOP);
//                    table.addCell(grcell);
//                }
//                cell3 = createCell("SUB TOTAL", fontSmallBold,Element.ALIGN_LEFT,Rectangle.TOP,5);
//                table.addCell(cell3);
//                cell3 = createCell(ProfileHandler.getFormattedCurrency(total, currencyid, session), fontSmallRegular,Element.ALIGN_RIGHT,15,5);
//                table.addCell(cell3);
//                for (int i = 0; i < 3; i++) {
//                    grcell = new PdfPCell(new Paragraph("", fontSmallRegular));
//                    grcell.setHorizontalAlignment(Element.ALIGN_CENTER);
//                    grcell.setPadding(5);
//                    grcell.setBorder(0);
//                    table.addCell(grcell);
//                }
//                cell3 = createCell("DISCOUNT (-)", fontSmallBold,Element.ALIGN_LEFT,0,5);
//                table.addCell(cell3);
//                grcell = calculateDiscount(gr.getDiscount(), currencyid, session);
//                grcell.setHorizontalAlignment(Element.ALIGN_RIGHT);
//                grcell.setPadding(5);
//                table.addCell(grcell);
//                for (int i = 0; i < 3; i++) {
//                    grcell = new PdfPCell(new Paragraph("", fontSmallRegular));
//                    grcell.setHorizontalAlignment(Element.ALIGN_CENTER);
//                    grcell.setPadding(5);
//                    grcell.setBorder(0);
//                    table.addCell(grcell);
//                }
//                cell3 = createCell("TAX (+)", fontSmallBold,Element.ALIGN_LEFT,0,5);
//                table.addCell(cell3);
//                cell3 = createCell(ProfileHandler.getFormattedCurrency((gr.getTaxEntry()!=null?gr.getTaxEntry().getAmount():0),currencyid,session), fontSmallRegular,Element.ALIGN_RIGHT,15,5);
//                table.addCell(cell3);
//                for (int i = 0; i < 3; i++) {
//                    grcell = new PdfPCell(new Paragraph("", fontSmallRegular));
//                    grcell.setHorizontalAlignment(Element.ALIGN_CENTER);
//                    grcell.setPadding(5);
//                    grcell.setBorder(0);
//                    table.addCell(grcell);
//                }
//                cell3 = createCell("TOTAL", fontSmallBold,Element.ALIGN_LEFT,0,5);
//                table.addCell(cell3);
//                cell3 = createCell(ProfileHandler.getFormattedCurrency((gr.getVendorEntry().getAmount()), currencyid, session), fontSmallRegular,Element.ALIGN_RIGHT,15,5);
//                table.addCell(cell3);
//
//                PdfPCell mainCell5 = new PdfPCell(table);
//                mainCell5.setBorder(0);
//                mainTable.addCell(mainCell5);
//
//                PdfPTable helpTable = new PdfPTable(1);
//                helpTable.setWidthPercentage(100);
//                Chunk chunk1 = new Chunk("Memo:  ",fontSmallBold);
//                Chunk chunk2 = new Chunk(gr.getMemo(),fontSmallRegular);
//                Phrase phrase1 = new Phrase();
//                phrase1.add(chunk1);
//                phrase1.add(chunk2);
//                Paragraph p = new Paragraph();
//                p.add(phrase1);
//                PdfPCell pcell = new PdfPCell(p);
//                pcell.setBorder(0);
//                pcell.setPadding(10);
//                helpTable.addCell(pcell);
//
//                PdfPCell mainCell61 = new PdfPCell(helpTable);
//                mainCell61.setBorder(0);
//                mainTable.addCell(mainCell61);
//
//             } else if (mode == StaticValues.AUTONUM_RECEIPT||mode==45) {
//
//                Receipt rc = null;
//                BillingReceipt rc1=null;
//                if(mode!=45)
//                    rc=(Receipt) session.get(Receipt.class, billid);
//                else
//                    rc1=(BillingReceipt) session.get(BillingReceipt.class, billid);
//
//                KWLCurrency currency=(KWLCurrency)session.get(KWLCurrency.class, currencyid);
//                Company com = mode!=45?rc.getCompany():rc1.getCompany();
//                String company[] = new String[3];
//                company[0] = com.getCompanyName();
//                company[1] = com.getAddress();
//                company[2] = com.getEmailID();
//
//
//                PdfPTable table1 = new PdfPTable(2);
//                table1.setWidthPercentage(100);
//                table1.setWidths(new float[]{25, 75});
//
//                tab1 = addCompanyLogo(logoPath,com);
//
//                tab3 = new PdfPTable(1);
//                tab3.setWidthPercentage(100);
//                PdfPCell mainCell1 = null;
//                mainCell1 = createCell(com.getCompanyName(), fontMediumBold,Element.ALIGN_LEFT,0,0);
//                mainCell1.setPaddingLeft(125);
//                tab3.addCell(mainCell1);
//                mainCell1 = createCell(com.getAddress(), fontSmallRegular,Element.ALIGN_LEFT,0,0);
//                mainCell1.setPaddingLeft(75);
//                tab3.addCell(mainCell1);
//                mainCell1 = createCell(com.getEmailID(), fontSmallRegular,Element.ALIGN_LEFT,0,0);
//                mainCell1.setPaddingLeft(75);
//                tab3.addCell(mainCell1);
//                mainCell1 = createCell(com.getPhoneNumber(), fontSmallRegular,Element.ALIGN_LEFT,0,0);
//                mainCell1.setPaddingLeft(75);
//                tab3.addCell(mainCell1);
//                mainCell1 = new PdfPCell(tab1);
//                mainCell1.setBorder(0);
//                table1.addCell(mainCell1);
//                mainCell1 = new PdfPCell(tab3);
//                mainCell1.setBorder(0);
//                table1.addCell(mainCell1);
//
//                mainCell1 = new PdfPCell(table1);
//                mainCell1.setBorder(0);
//                mainTable.addCell(mainCell1);
//
//
//                blankTable = addBlankLine(5);
//                blankCell = new PdfPCell(blankTable);
//                blankCell.setBorder(0);
//                mainTable.addCell(blankCell);
//
//                PdfPCell mainCell2 = createCell("Payment Voucher", fontMediumBold,Element.ALIGN_CENTER,0,0);
//                mainTable.addCell(mainCell2);
//
//                blankTable = addBlankLine(2);
//                blankCell = new PdfPCell(blankTable);
//                blankCell.setBorder(0);
//                mainTable.addCell(blankCell);
//
//                tab3 = new PdfPTable(2);
//                tab3.setWidthPercentage(100);
//                tab3.setWidths(new float[]{60, 40});
//
//                tab1 = new PdfPTable(2);
//                tab1.setWidthPercentage(100);
//                tab1.setWidths(new float[]{10, 90});
//
//                PdfPCell cell3 = createCell("No : ", fontSmallRegular,Element.ALIGN_LEFT,0,0);new PdfPCell(new Paragraph());
//                tab1.addCell(cell3);
//                cell3 = createCell(mode!=45?rc.getReceiptNumber():rc1.getBillingReceiptNumber(), fontSmallBold,Element.ALIGN_LEFT,0,0);
//                tab1.addCell(cell3);
//                tab2 = new PdfPTable(2);
//                tab2.setWidthPercentage(100);
//                tab2.setWidths(new float[]{30, 70});
//                cell3 = createCell("Dated :", fontSmallRegular,Element.ALIGN_RIGHT,0,0);
//                tab2.addCell(cell3);
//                cell3 = createCell(formatter.format(mode!=45?rc.getJournalEntry().getEntryDate():rc1.getJournalEntry().getEntryDate()), fontSmallBold,Element.ALIGN_LEFT,0,0);
//                tab2.addCell(cell3);
//
//                PdfPCell mainCell3 = new PdfPCell(tab1);
//                mainCell3.setBorder(0);
//                tab3.addCell(mainCell3);
//                mainCell3 = new PdfPCell(tab2);
//                mainCell3.setBorder(0);
//                tab3.addCell(mainCell3);
//
//                PdfPCell mainCell4 = new PdfPCell(tab3);
//                mainCell4.setBorder(0);
//                mainTable.addCell(mainCell4);
//
//                blankTable = addBlankLine(5);
//                blankCell = new PdfPCell(blankTable);
//                blankCell.setBorder(0);
//                mainTable.addCell(blankCell);
//                tab2 = new PdfPTable(2);
//                tab2.setWidthPercentage(100);
//                tab2.setWidths(new float[]{75, 25});
//                cell3 = new PdfPCell(new Paragraph("Particulars", fontSmallBold));
//                cell3.setHorizontalAlignment(Element.ALIGN_LEFT);
//                cell3.setBackgroundColor(Color.lightGray);
//                cell3.setBorder(Rectangle.LEFT+Rectangle.BOTTOM+Rectangle.RIGHT);
//                tab2.addCell(cell3);
//                cell3 = new PdfPCell(new Paragraph("Amount", fontSmallBold));
//                cell3.setHorizontalAlignment(Element.ALIGN_CENTER);
//                cell3.setBackgroundColor(Color.lightGray);
//                cell3.setBorder(Rectangle.RIGHT+Rectangle.BOTTOM);
//                tab2.addCell(cell3);
//                cell3 = createCell("Account : ", fontSmallRegular,Element.ALIGN_LEFT,Rectangle.LEFT + Rectangle.RIGHT,5);
//                tab2.addCell(cell3);
//                cell3 = createCell("", fontSmallBold,Element.ALIGN_LEFT,Rectangle.RIGHT,0);
//                tab2.addCell(cell3);
//                cell3 = createCell(accname, fontSmallRegular,Element.ALIGN_LEFT,Rectangle.LEFT + Rectangle.RIGHT,0);
//                cell3.setPaddingLeft(50);
//                tab2.addCell(cell3);
//                cell3 = createCell(ProfileHandler.getFormattedCurrency(amount, currencyid, session), fontSmallRegular,Element.ALIGN_RIGHT,Rectangle.RIGHT,15);
//                tab2.addCell(cell3);
//                cell3 = createCell(address, fontSmallRegular,Element.ALIGN_LEFT,Rectangle.LEFT + Rectangle.RIGHT,0);
//                cell3.setPaddingLeft(50);
//                tab2.addCell(cell3);
//                cell3 = createCell("", fontSmallRegular,Element.ALIGN_RIGHT,Rectangle.RIGHT,0);
//                tab2.addCell(cell3);
//
//                for (int i = 0; i < 30; i++) {
//                    cell3 = new PdfPCell(new Paragraph("", fontSmallRegular));
//                    cell3.setBorder(Rectangle.LEFT+Rectangle.RIGHT);
//                    tab2.addCell(cell3);
//                }
//                cell3 = createCell("Through :", fontSmallRegular,Element.ALIGN_LEFT,Rectangle.LEFT + Rectangle.RIGHT,5);
//                tab2.addCell(cell3);
//                cell3 = createCell("", fontSmallRegular,Element.ALIGN_RIGHT,Rectangle.RIGHT,0);
//                tab2.addCell(cell3);
//                cell3 = createCell(mode!=45?(rc.getPayDetail()==null?"Cash":rc.getPayDetail().getPaymentMethod().getMethodName()):(rc1.getPayDetail()==null?"Cash":rc1.getPayDetail().getPaymentMethod().getMethodName()), fontSmallRegular,Element.ALIGN_LEFT,Rectangle.LEFT + Rectangle.RIGHT,5);
//                cell3.setPaddingLeft(50);
//                tab2.addCell(cell3);
//                cell3 = createCell("", fontSmallRegular,Element.ALIGN_CENTER,Rectangle.RIGHT,0);
//                tab2.addCell(cell3);
//                cell3 = createCell(mode!=45?(rc.getPayDetail()!=null?(rc.getPayDetail().getPaymentMethod().getMethodName().equals("Cheque")?"Cheque No : "+rc.getPayDetail().getCheque().getChequeNo()+" and Bank Name : "+rc.getPayDetail().getCheque().getBankName():""):""):
//                    (rc1.getPayDetail()!=null?(rc1.getPayDetail().getPaymentMethod().getMethodName().equals("Cheque")?"Cheque No : "+rc1.getPayDetail().getCheque().getChequeNo()+" and Bank Name : "+rc1.getPayDetail().getCheque().getBankName():""):""), fontSmallRegular,Element.ALIGN_LEFT,Rectangle.LEFT + Rectangle.RIGHT,5);
//                cell3.setPaddingLeft(50);
//                tab2.addCell(cell3);
//                cell3 = createCell("", fontSmallRegular,Element.ALIGN_CENTER,Rectangle.RIGHT,0);
//                tab2.addCell(cell3);
//                cell3 = createCell(mode!=45?(rc.getPayDetail()!=null?(rc.getPayDetail().getPaymentMethod().getMethodName().equals("Credit Card")||customer.equals("Debit Card")?
//                        rc.getPayDetail().getCard()!=null?"Card No : "+(rc.getPayDetail().getCard().getCardNo()+" and Card Holder : "+rc.getPayDetail().getCard().getCardHolder()):"":""):""):
//                        (rc1.getPayDetail()!=null?(rc1.getPayDetail().getPaymentMethod().getMethodName().equals("Credit Card")||customer.equals("Debit Card")?
//                        rc1.getPayDetail().getCard()!=null?"Card No : "+(rc1.getPayDetail().getCard().getCardNo()+" and Card Holder : "+rc1.getPayDetail().getCard().getCardHolder()):"":""):""),fontSmallRegular,Element.ALIGN_LEFT,Rectangle.LEFT + Rectangle.RIGHT,5);
//                cell3.setPaddingLeft(50);
//                tab2.addCell(cell3);
//                cell3 = createCell("", fontSmallRegular,Element.ALIGN_CENTER,Rectangle.RIGHT,0);
//                tab2.addCell(cell3);
//                cell3 = createCell("On Account of :", fontSmallRegular,Element.ALIGN_LEFT,Rectangle.LEFT + Rectangle.RIGHT,5);
//                tab2.addCell(cell3);
//                cell3 = createCell("", fontSmallRegular,Element.ALIGN_RIGHT,Rectangle.RIGHT,5);
//                tab2.addCell(cell3);
//                cell3 = createCell(mode!=45?rc.getMemo():rc1.getMemo(), fontSmallRegular,Element.ALIGN_LEFT,Rectangle.LEFT + Rectangle.RIGHT,5);
//                cell3.setPaddingLeft(50);
//                tab2.addCell(cell3);
//                cell3 = createCell("", fontSmallRegular,Element.ALIGN_CENTER,Rectangle.RIGHT,5);
//                tab2.addCell(cell3);
//                String netinword = EnglishNumberToWords.convert(Double.parseDouble(String.valueOf(amount)),currency);
//                 String currencyname=currency.getName();
//                cell3 = createCell("Amount (in words) : " +currencyname+" "+ netinword + " only.", fontSmallRegular,Element.ALIGN_LEFT,Rectangle.LEFT + Rectangle.RIGHT + Rectangle.BOTTOM + Rectangle.TOP,5);
//                tab2.addCell(cell3);
//                cell3 = createCell(ProfileHandler.getFormattedCurrency(amount, currencyid, session), fontSmallRegular,Element.ALIGN_RIGHT,Rectangle.LEFT + Rectangle.RIGHT + Rectangle.BOTTOM + Rectangle.TOP,5);
//                tab2.addCell(cell3);
//                PdfPCell mainCell5 = new PdfPCell(tab2);
//                mainCell5.setBorder(1);
//                mainTable.addCell(mainCell5);
//
//                blankTable = addBlankLine(25);
//                blankCell = new PdfPCell(blankTable);
//                blankCell.setBorder(0);
//                mainTable.addCell(blankCell);
//
//                tab1 = new PdfPTable(2);
//                tab1.setWidthPercentage(100);
//                tab1.setWidths(new float[]{50, 50});
//                cell3 = createCell("Receiver's Signature ", fontTblMediumBold,Element.ALIGN_CENTER,0,0);
//                tab1.addCell(cell3);
//                cell3 = createCell("Authorised Signature", fontTblMediumBold,Element.ALIGN_CENTER,0,0);
//                tab1.addCell(cell3);
//                PdfPCell mainCell6 = new PdfPCell(tab1);
//                mainCell6.setPadding(5);
//                mainCell6.setBorder(0);
//                mainTable.addCell(mainCell6);
//
//                blankTable = addBlankLine(15);
//                blankCell = new PdfPCell(blankTable);
//                blankCell.setBorder(0);
//                mainTable.addCell(blankCell);
//
//                tab2 = new PdfPTable(2);
//                tab2.setWidthPercentage(100);
//                tab2.setWidths(new float[]{50, 50});
//                cell3 = createCell("Checked by ", fontTblMediumBold,Element.ALIGN_CENTER,0,0);
//                tab2.addCell(cell3);
//                cell3 = createCell("Verified by ", fontTblMediumBold,Element.ALIGN_CENTER,0,0);
//                tab2.addCell(cell3);
//                PdfPCell mainCell7 = new PdfPCell(tab2);
//                mainCell7.setPadding(5);
//                mainCell7.setBorder(0);
//                mainTable.addCell(mainCell7);
//                blankTable = addBlankLine(5);
//                blankCell = new PdfPCell(blankTable);
//                blankCell.setBorder(0);
//                mainTable.addCell(blankCell);
//
//            }
//             else if (mode == StaticValues.AUTONUM_PAYMENT) {
//                Payment pc = null;
//                pc=(Payment) session.get(Payment.class, billid);
//                KWLCurrency currency=(KWLCurrency)session.get(KWLCurrency.class, currencyid);
//                Company com =pc.getCompany();
//                String company[] = new String[3];
//                company[0] = com.getCompanyName();
//                company[1] = com.getAddress();
//                company[2] = com.getEmailID();
//
//
//                PdfPTable table1 = new PdfPTable(2);
//                table1.setWidthPercentage(100);
//                table1.setWidths(new float[]{25, 75});
//
//                tab1 = addCompanyLogo(logoPath,com);
//
//                tab3 = new PdfPTable(1);
//                tab3.setWidthPercentage(100);
//                PdfPCell mainCell1 = null;
//                mainCell1 = createCell(com.getCompanyName(), fontMediumBold,Element.ALIGN_LEFT,0,0);
//                mainCell1.setPaddingLeft(125);
//                tab3.addCell(mainCell1);
//                mainCell1 = createCell(com.getAddress(), fontSmallRegular,Element.ALIGN_LEFT,0,0);
//                mainCell1.setPaddingLeft(75);
//                tab3.addCell(mainCell1);
//                mainCell1 = createCell(com.getEmailID(), fontSmallRegular,Element.ALIGN_LEFT,0,0);
//                mainCell1.setPaddingLeft(75);
//                tab3.addCell(mainCell1);
//                mainCell1 = createCell(com.getPhoneNumber(), fontSmallRegular,Element.ALIGN_LEFT,0,0);
//                mainCell1.setPaddingLeft(75);
//                tab3.addCell(mainCell1);
//                mainCell1 = new PdfPCell(tab1);
//                mainCell1.setBorder(0);
//                table1.addCell(mainCell1);
//                mainCell1 = new PdfPCell(tab3);
//                mainCell1.setBorder(0);
//                table1.addCell(mainCell1);
//
//                mainCell1 = new PdfPCell(table1);
//                mainCell1.setBorder(0);
//                mainTable.addCell(mainCell1);
//
//
//                blankTable = addBlankLine(5);
//                blankCell = new PdfPCell(blankTable);
//                blankCell.setBorder(0);
//                mainTable.addCell(blankCell);
//
//                PdfPCell mainCell2 = createCell("Payment Voucher", fontMediumBold,Element.ALIGN_CENTER,0,0);
//                mainTable.addCell(mainCell2);
//
//                blankTable = addBlankLine(2);
//                blankCell = new PdfPCell(blankTable);
//                blankCell.setBorder(0);
//                mainTable.addCell(blankCell);
//
//                tab3 = new PdfPTable(2);
//                tab3.setWidthPercentage(100);
//                tab3.setWidths(new float[]{60, 40});
//
//                tab1 = new PdfPTable(2);
//                tab1.setWidthPercentage(100);
//                tab1.setWidths(new float[]{10, 90});
//
//                PdfPCell cell3 = createCell("No : ", fontSmallRegular,Element.ALIGN_LEFT,0,0);new PdfPCell(new Paragraph());
//                tab1.addCell(cell3);
//                cell3 = createCell(pc.getPaymentNumber(), fontSmallBold,Element.ALIGN_LEFT,0,0);
//                tab1.addCell(cell3);
//                tab2 = new PdfPTable(2);
//                tab2.setWidthPercentage(100);
//                tab2.setWidths(new float[]{30, 70});
//                cell3 = createCell("Dated :", fontSmallRegular,Element.ALIGN_RIGHT,0,0);
//                tab2.addCell(cell3);
//                cell3 = createCell(formatter.format(pc.getJournalEntry().getEntryDate()), fontSmallBold,Element.ALIGN_LEFT,0,0);
//                tab2.addCell(cell3);
//
//                PdfPCell mainCell3 = new PdfPCell(tab1);
//                mainCell3.setBorder(0);
//                tab3.addCell(mainCell3);
//                mainCell3 = new PdfPCell(tab2);
//                mainCell3.setBorder(0);
//                tab3.addCell(mainCell3);
//
//                PdfPCell mainCell4 = new PdfPCell(tab3);
//                mainCell4.setBorder(0);
//                mainTable.addCell(mainCell4);
//
//                blankTable = addBlankLine(5);
//                blankCell = new PdfPCell(blankTable);
//                blankCell.setBorder(0);
//                mainTable.addCell(blankCell);
//                tab2 = new PdfPTable(2);
//                tab2.setWidthPercentage(100);
//                tab2.setWidths(new float[]{75, 25});
//                cell3 = new PdfPCell(new Paragraph("Particulars", fontSmallBold));
//                cell3.setHorizontalAlignment(Element.ALIGN_LEFT);
//                cell3.setBackgroundColor(Color.lightGray);
//                cell3.setBorder(Rectangle.LEFT+Rectangle.BOTTOM+Rectangle.RIGHT);
//                tab2.addCell(cell3);
//                cell3 = new PdfPCell(new Paragraph("Amount", fontSmallBold));
//                cell3.setHorizontalAlignment(Element.ALIGN_CENTER);
//                cell3.setBackgroundColor(Color.lightGray);
//                cell3.setBorder(Rectangle.RIGHT+Rectangle.BOTTOM);
//                tab2.addCell(cell3);
//                cell3 = createCell("Account : ", fontSmallRegular,Element.ALIGN_LEFT,Rectangle.LEFT + Rectangle.RIGHT,5);
//                tab2.addCell(cell3);
//                cell3 = createCell("", fontSmallBold,Element.ALIGN_LEFT,Rectangle.RIGHT,0);
//                tab2.addCell(cell3);
//                cell3 = createCell(accname, fontSmallRegular,Element.ALIGN_LEFT,Rectangle.LEFT + Rectangle.RIGHT,0);
//                cell3.setPaddingLeft(50);
//                tab2.addCell(cell3);
//                cell3 = createCell(ProfileHandler.getFormattedCurrency(amount, currencyid, session), fontSmallRegular,Element.ALIGN_RIGHT,Rectangle.RIGHT,5);
//                tab2.addCell(cell3);
//                cell3 = createCell(address, fontSmallRegular,Element.ALIGN_LEFT,Rectangle.LEFT + Rectangle.RIGHT,0);
//                cell3.setPaddingLeft(50);
//                tab2.addCell(cell3);
//                cell3 = createCell("", fontSmallRegular,Element.ALIGN_RIGHT,Rectangle.RIGHT,0);
//                tab2.addCell(cell3);
//
//                for (int i = 0; i < 30; i++) {
//                    cell3 = new PdfPCell(new Paragraph("", fontSmallRegular));
//                    cell3.setBorder(Rectangle.LEFT+Rectangle.RIGHT);
//                    tab2.addCell(cell3);
//                }
//                cell3 = createCell("Through :", fontSmallRegular,Element.ALIGN_LEFT,Rectangle.LEFT + Rectangle.RIGHT,5);
//                tab2.addCell(cell3);
//                cell3 = createCell("", fontSmallRegular,Element.ALIGN_RIGHT,Rectangle.RIGHT,0);
//                tab2.addCell(cell3);
//                cell3 = createCell((pc.getPayDetail()==null?"Cash":pc.getPayDetail().getPaymentMethod().getMethodName()), fontSmallRegular,Element.ALIGN_LEFT,Rectangle.LEFT + Rectangle.RIGHT,5);
//                cell3.setPaddingLeft(50);
//                tab2.addCell(cell3);
//                cell3 = createCell("", fontSmallRegular,Element.ALIGN_CENTER,Rectangle.RIGHT,0);
//                tab2.addCell(cell3);
//                cell3 = createCell((pc.getPayDetail()!=null?(pc.getPayDetail().getPaymentMethod().getMethodName().equals("Cheque")?"Cheque No : "+pc.getPayDetail().getCheque().getChequeNo()+" and Bank Name : "+pc.getPayDetail().getCheque().getBankName():""):""), fontSmallRegular,Element.ALIGN_LEFT,Rectangle.LEFT + Rectangle.RIGHT,5);
//                cell3.setPaddingLeft(50);
//                tab2.addCell(cell3);
//                cell3 = createCell("", fontSmallRegular,Element.ALIGN_CENTER,Rectangle.RIGHT,0);
//                tab2.addCell(cell3);
//                cell3 = createCell((pc.getPayDetail()!=null?(pc.getPayDetail().getPaymentMethod().getMethodName().equals("Credit Card")||customer.equals("Debit Card")?
//                        pc.getPayDetail().getCard()!=null?"Card No : "+(pc.getPayDetail().getCard().getCardNo()+" and Card Holder : "+pc.getPayDetail().getCard().getCardHolder()):"":""):""),fontSmallRegular,Element.ALIGN_LEFT,Rectangle.LEFT + Rectangle.RIGHT,5);
//                cell3.setPaddingLeft(50);
//                tab2.addCell(cell3);
//                cell3 = createCell("", fontSmallRegular,Element.ALIGN_CENTER,Rectangle.RIGHT,0);
//                tab2.addCell(cell3);
//                cell3 = createCell("On Account of :", fontSmallRegular,Element.ALIGN_LEFT,Rectangle.LEFT + Rectangle.RIGHT,5);
//                tab2.addCell(cell3);
//                cell3 = createCell("", fontSmallRegular,Element.ALIGN_RIGHT,Rectangle.RIGHT,5);
//                tab2.addCell(cell3);
//                cell3 = createCell(pc.getMemo(), fontSmallRegular,Element.ALIGN_LEFT,Rectangle.LEFT + Rectangle.RIGHT,5);
//                cell3.setPaddingLeft(50);
//                tab2.addCell(cell3);
//                cell3 = createCell("", fontSmallRegular,Element.ALIGN_CENTER,Rectangle.RIGHT,5);
//                tab2.addCell(cell3);
//                String netinword = EnglishNumberToWords.convert(Double.parseDouble(String.valueOf(amount)),currency);
//                 String currencyname=currency.getName();
//                cell3 = createCell("Amount (in words) : " +currencyname+" "+ netinword + " only.", fontSmallRegular,Element.ALIGN_LEFT,Rectangle.LEFT + Rectangle.RIGHT + Rectangle.BOTTOM + Rectangle.TOP,5);
//                tab2.addCell(cell3);
//                cell3 = createCell(ProfileHandler.getFormattedCurrency(amount, currencyid, session), fontSmallRegular,Element.ALIGN_RIGHT,Rectangle.LEFT + Rectangle.RIGHT + Rectangle.BOTTOM + Rectangle.TOP,5);
//                tab2.addCell(cell3);
//                PdfPCell mainCell5 = new PdfPCell(tab2);
//                mainCell5.setBorder(1);
//                mainTable.addCell(mainCell5);
//
//                blankTable = addBlankLine(25);
//                blankCell = new PdfPCell(blankTable);
//                blankCell.setBorder(0);
//                mainTable.addCell(blankCell);
//
//                tab1 = new PdfPTable(2);
//                tab1.setWidthPercentage(100);
//                tab1.setWidths(new float[]{50, 50});
//                cell3 = createCell("Receiver's Signature ", fontTblMediumBold,Element.ALIGN_CENTER,0,0);
//                tab1.addCell(cell3);
//                cell3 = createCell("Authorised Signature", fontTblMediumBold,Element.ALIGN_CENTER,0,0);
//                tab1.addCell(cell3);
//                PdfPCell mainCell6 = new PdfPCell(tab1);
//                mainCell6.setPadding(5);
//                mainCell6.setBorder(0);
//                mainTable.addCell(mainCell6);
//
//                blankTable = addBlankLine(15);
//                blankCell = new PdfPCell(blankTable);
//                blankCell.setBorder(0);
//                mainTable.addCell(blankCell);
//
//                tab2 = new PdfPTable(2);
//                tab2.setWidthPercentage(100);
//                tab2.setWidths(new float[]{50, 50});
//                cell3 = createCell("Checked by ", fontTblMediumBold,Element.ALIGN_CENTER,0,0);
//                tab2.addCell(cell3);
//                cell3 = createCell("Verified by ", fontTblMediumBold,Element.ALIGN_CENTER,0,0);
//                tab2.addCell(cell3);
//                PdfPCell mainCell7 = new PdfPCell(tab2);
//                mainCell7.setPadding(5);
//                mainCell7.setBorder(0);
//                mainTable.addCell(mainCell7);
//                blankTable = addBlankLine(5);
//                blankCell = new PdfPCell(blankTable);
//                blankCell.setBorder(0);
//                mainTable.addCell(blankCell);
//
//            }
//
//            document.add(mainTable);
//            return baos;
//        } catch (Exception ex) {
//            return null;
//        } finally {
//            if(document!=null)
//                document.close();
//            if(writer!=null)
//                writer.close();
//             if(baos!=null)
//                baos.close();
//        }
//
//    }
//
//    private static PdfPCell createCell(String string, Font fontTbl, int ALIGN_RIGHT, int i,int padd) {
//        PdfPCell cell = new PdfPCell(new Paragraph(string, fontTbl));
//        cell.setHorizontalAlignment(ALIGN_RIGHT);
//        cell.setBorder(i);
//        cell.setPadding(padd);
//        return cell;
//    }
//
//    private static PdfPTable addCompanyLogo(String logoPath, Company com) {
//        PdfPTable tab1 = new PdfPTable(1);
//        imgPath = logoPath;
//        PdfPCell imgCell = null;
//        try {
//           Image img = Image.getInstance(imgPath);
//           imgCell = new PdfPCell(img);
//        } catch (Exception e) {
//           imgCell = new PdfPCell(new Paragraph(com.getCompanyName(), fontSmallRegular));
//        }
//        imgCell.setHorizontalAlignment(Element.ALIGN_LEFT);
//        imgCell.setBorder(0);
//        tab1.addCell(imgCell);
//        return tab1;
//    }
//     private static void addHeaderFooter(Document document,PdfWriter writer) throws DocumentException,ServiceException{
//        PdfPTable footer = new PdfPTable(1);
//        PdfPCell footerSeparator = new PdfPCell(new Paragraph("THANK YOU FOR YOUR BUSINESS!", fontTblMediumBold));
//        footerSeparator.setHorizontalAlignment(Element.ALIGN_CENTER);
//        footerSeparator.setBorder(0);
//        footer.addCell(footerSeparator);
//
//     try {
//        Rectangle page = document.getPageSize();
//        footer.setTotalWidth(page.getWidth()-document.leftMargin());
//        footer.writeSelectedRows(0, -1, document.leftMargin(), document.bottomMargin() ,writer.getDirectContent());
//    } catch (Exception e) {
//        throw new ExceptionConverter(e);
//    }
//    }
//
//
//    public static PdfPTable getCompanyInfo(String com[]) {
//        PdfPTable tab1 = new PdfPTable(1);
//        tab1.setHorizontalAlignment(Element.ALIGN_CENTER);
//        PdfPCell cell = new PdfPCell(new Paragraph(com[0], fontMediumBold));
//        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
//        cell.setBorder(0);
//        tab1.addCell(cell);
//        for (int i = 1; i < com.length; i++) {
//            cell = new PdfPCell(new Paragraph(com[i], fontTblMediumBold));
//            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
//            cell.setBorder(0);
//            tab1.addCell(cell);
//        }
//        return tab1;
//    }
//
//    private static PdfPTable addBlankLine(int count) {
//        PdfPTable table = new PdfPTable(1);
//        PdfPCell cell = null;
//        for (int i = 0; i < count; i++) {
//            cell = new PdfPCell(new Paragraph("", fontTblMediumBold));
//            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
//            cell.setPadding(3);
//            cell.setBorder(0);
//            table.addCell(cell);
//        }
//        return table;
//    }
//
//    private static PdfPCell calculateDiscount(Discount disc, String currencyid, Session session) throws HibernateException, SessionExpiredException {
//        PdfPCell cell = null;
//        if (disc == null) {
//            cell = new PdfPCell(new Paragraph("--", fontSmallRegular));
//        } else if (disc.isInPercent()) {
//            cell = new PdfPCell(new Paragraph(ProfileHandler.getFormattedCurrency(disc.getDiscountValue(), currencyid, session), fontSmallRegular));
//        } else {
//            cell = new PdfPCell(new Paragraph(ProfileHandler.getFormattedCurrency(disc.getDiscountValue(), currencyid, session), fontSmallRegular));
//        }
//        return cell;
//    }
//
//    private static PdfPCell getCharges(JournalEntryDetail jEntry, String currencyid, Session session) throws HibernateException, SessionExpiredException {
//        PdfPCell cell = null;
//        if (jEntry == null) {
//            cell = new PdfPCell(new Paragraph("--", fontSmallBold));
//        } else {
//            cell = new PdfPCell(new Paragraph(ProfileHandler.getFormattedCurrency(jEntry.getAmount(), currencyid, session), fontSmallRegular));
//        }
//        return cell;
//    }
//
//    public static String numberFormatter(double values, String compSymbol) {
//        NumberFormat numberFormatter;
//        java.util.Locale currentLocale = java.util.Locale.US;
//        numberFormatter = NumberFormat.getNumberInstance(currentLocale);
//        numberFormatter.setMinimumFractionDigits(2);
//        numberFormatter.setMaximumFractionDigits(2);
//        return (compSymbol + numberFormatter.format(values));
//    }
//
//    private void writeDataToFile(String filename, ByteArrayOutputStream baos,
//        HttpServletResponse response) throws IOException {
//
//        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
//        response.setContentType("application/octet-stream");
//        response.setContentLength(baos.size());
//        response.getOutputStream().write(baos.toByteArray());
//        response.getOutputStream().flush();
//        response.getOutputStream().close();
//    }
//
//    public static class EnglishNumberToWords {
//
//        private static final String[] tensNames = {
//            "", " ten", " twenty", " thirty", " forty", " fifty", " sixty", " seventy", " eighty", " ninety"
//        };
//        private static final String[] numNames = {
//            "", " one", " two", " three", " four", " five", " six", " seven", " eight", " nine", " ten", " eleven", " twelve",
//            " thirteen", " fourteen", " fifteen", " sixteen", " seventeen", " eighteen", " nineteen"
//        };
//
//        private static String convertLessThanOneThousand(int number) {
//            String soFar;
//            if (number % 100 < 20) {
//                soFar = numNames[number % 100];
//                number /= 100;
//            } else {
//                soFar = numNames[number % 10];
//                number /= 10;
//                soFar = tensNames[number % 10] + soFar;
//                number /= 10;
//            }
//            if (number == 0) {
//                return soFar;
//            }
//            return numNames[number] + " hundred" + soFar;
//        }
//        private static String convertLessOne(int number,KWLCurrency currency) {
//            String soFar;
//            String val= currency.getName().equalsIgnoreCase(" US Dollar") ?" Cents":currency.getName().equalsIgnoreCase("Euro")?" Euro Cents":" Paise";
//            if (number % 100 < 20) {
//                soFar = numNames[number % 100];
//                number /= 100;
//            } else {
//                soFar = numNames[number % 10];
//                number /= 10;
//                soFar = tensNames[number % 10] + soFar;
//                number /= 10;
//            }
//            if (number == 0) {
//                return " and "+soFar +val;
//            }
//            return " and " +numNames[number] + val + soFar;
//        }
//
//        public static String convert(Double number,KWLCurrency currency) {
//            if (number == 0) {
//                return "zero";
//            }
//            String snumber = Double.toString(number);
//            String mask = "000000000000.00";
//            DecimalFormat df = new DecimalFormat(mask);
//            snumber = df.format(number);
//            int billions = Integer.parseInt(snumber.substring(0, 3));
//            int millions = Integer.parseInt(snumber.substring(3, 6));
//            int hundredThousands = Integer.parseInt(snumber.substring(6, 9));
//            int thousands = Integer.parseInt(snumber.substring(9, 12));
//            int fractions = Integer.parseInt(snumber.substring(13, 15));
//            String tradBillions;
//            switch (billions) {
//                case 0:
//                    tradBillions = "";
//                    break;
//                case 1:
//                    tradBillions = convertLessThanOneThousand(billions) + " billion ";
//                    break;
//                default:
//                    tradBillions = convertLessThanOneThousand(billions) + " billion ";
//            }
//            String result = tradBillions;
//
//            String tradMillions;
//            switch (millions) {
//                case 0:
//                    tradMillions = "";
//                    break;
//                case 1:
//                    tradMillions = convertLessThanOneThousand(millions) + " million ";
//                    break;
//                default:
//                    tradMillions = convertLessThanOneThousand(millions) + " million ";
//            }
//            result = result + tradMillions;
//
//            String tradHundredThousands;
//            switch (hundredThousands) {
//                case 0:
//                    tradHundredThousands = "";
//                    break;
//                case 1:
//                    tradHundredThousands = "one thousand ";
//                    break;
//                default:
//                    tradHundredThousands = convertLessThanOneThousand(hundredThousands) + " thousand ";
//            }
//            result = result + tradHundredThousands;
//            String tradThousand;
//            tradThousand = convertLessThanOneThousand(thousands);
//            result = result + tradThousand;
//            String paises;
//            switch (fractions) {
//                case 0:
//                    paises = "";
//                    break;
//               default:
//                    paises = convertLessOne(fractions,currency);
//            }
//            result = result + paises;
//            return result.replaceAll("^\\s+", "").replaceAll("\\b\\s{2,}\\b", " ");
//        }
//    }
//
//    @Override
//    protected void doGet(HttpServletRequest request,
//            HttpServletResponse response) throws ServletException, IOException {
//        processRequest(request, response);
//    }
//
//    @Override
//    protected void doPost(HttpServletRequest request,
//            HttpServletResponse response) throws ServletException, IOException {
//        processRequest(request, response);
//    }

    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
