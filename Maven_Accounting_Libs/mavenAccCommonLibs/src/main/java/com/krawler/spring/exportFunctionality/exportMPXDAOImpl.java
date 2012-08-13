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
package com.krawler.spring.exportFunctionality;

import com.krawler.accounting.fontsetting.FontContext;
import com.krawler.accounting.fontsetting.FontFamily;
import com.krawler.accounting.fontsetting.FontFamilySelector;
import com.krawler.common.admin.KWLCurrency;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.StringUtil;
import com.krawler.common.util.URLUtil;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.authHandler.authHandlerDAO;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.spring.storageHandler.storageHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.ExceptionConverter;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.HeaderFooter;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfCell;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import org.hibernate.SessionFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.web.servlet.support.RequestContextUtils;

public class exportMPXDAOImpl implements MessageSourceAware{

    private HibernateTemplate hibernateTemplate;
    private storageHandlerImpl storageHandlerImplObj;
    private sessionHandlerImpl sessionHandlerImplObj;
    private authHandlerDAO authHandlerDAOObj;
    private MessageSource messageSource;

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.hibernateTemplate = new HibernateTemplate(sessionFactory);
    }
    public void setstorageHandlerImpl(storageHandlerImpl storageHandlerImplObj1) {
        this.storageHandlerImplObj = storageHandlerImplObj1;
    }
    public void setsessionHandlerImpl(sessionHandlerImpl sessionHandlerImplObj1) {
        this.sessionHandlerImplObj = sessionHandlerImplObj1;
    }
    public void setauthHandlerDAO(authHandlerDAO authHandlerDAOObj1) {
        this.authHandlerDAOObj = authHandlerDAOObj1;
    }
    
	@Override
	public void setMessageSource(MessageSource ms) {
		this.messageSource=ms;
	}
    
    
    private static FontFamilySelector fontFamilySelector=new FontFamilySelector();
    static{
    	FontFamily fontFamily=new FontFamily();
    	fontFamily.addFont(FontContext.HEADER_NOTE, FontFactory.getFont("Helvetica", 10, Font.BOLD, Color.GRAY));
    	fontFamily.addFont(FontContext.FOOTER_NOTE, FontFactory.getFont("Helvetica", 8, Font.BOLD, Color.BLACK));
    	fontFamily.addFont(FontContext.LOGO_TEXT, FontFactory.getFont("Times New Roman", 14, Font.NORMAL, Color.BLACK));
    	fontFamily.addFont(FontContext.REPORT_TITLE, FontFactory.getFont("Helvetica", 12, Font.BOLD, Color.BLACK));
    	fontFamily.addFont(FontContext.SMALL_TEXT, FontFactory.getFont("Times New Roman", 12, Font.NORMAL, Color.BLACK));
    	fontFamily.addFont(FontContext.TABLE_HEADER, FontFactory.getFont("Helvetica", 12, Font.NORMAL, Color.BLACK));
    	fontFamily.addFont(FontContext.TABLE_DATA, FontFactory.getFont("Helvetica", 12, Font.NORMAL, Color.BLACK));
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
    
    
//    private static Font fontSmallRegular = FontFactory.getFont("Helvetica", 8, Font.BOLD, Color.BLACK);
//    private static Font fontSmallBold = FontFactory.getFont("Helvetica", 8, Font.BOLD, Color.BLACK);
//    private static Font fontMediumRegular = FontFactory.getFont("Helvetica", 12, Font.NORMAL, Color.BLACK);
//    private static Font fontRegular = FontFactory.getFont("Helvetica", 12, Font.NORMAL, Color.BLACK);
//    private static Font fontBold = FontFactory.getFont("Helvetica", 12, Font.BOLD, Color.BLACK);
//    private static Font fontBig = FontFactory.getFont("Helvetica", 24, Font.NORMAL, Color.BLACK);

    private static String imgPath = "";
    private static String companyName = "";
    private static com.krawler.utils.json.base.JSONObject config = null;
    private PdfPTable header = null;
    private PdfPTable footer = null;
    private static final long serialVersionUID = -8401651817881523209L;
    static SimpleDateFormat df = new SimpleDateFormat("yyyy-M-dd");
    private static String errorMsg = "";
    
    public class EndPage extends PdfPageEventHelper {

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            try {
                Rectangle page = document.getPageSize();
                try {
                    getHeaderFooter(document);
                } catch (ServiceException ex) {
                    Logger.getLogger(exportDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
                // Add page header
                header.setTotalWidth(page.getWidth() - document.leftMargin() - document.rightMargin());
                header.writeSelectedRows(0, -1, document.leftMargin(), page.getHeight() - 10, writer.getDirectContent());

                // Add page footer
                footer.setTotalWidth(page.getWidth() - document.leftMargin() - document.rightMargin());
                footer.writeSelectedRows(0, -1, document.leftMargin(), document.bottomMargin() - 5, writer.getDirectContent());

                // Add page border
                if (config.getBoolean("pageBorder")) {
                    int bmargin = 8;  //border margin
                    PdfContentByte cb = writer.getDirectContent();
                    cb.rectangle(bmargin, bmargin, page.getWidth() - bmargin * 2, page.getHeight() - bmargin * 2);
                    cb.setColorStroke(Color.LIGHT_GRAY);
                    cb.stroke();
                }

            } catch (JSONException e) {
                throw new ExceptionConverter(e);
            }
        }
    }

    public void processRequest(HttpServletRequest request, HttpServletResponse response, JSONObject jobj) throws ServiceException, IOException {
        ByteArrayOutputStream baos = null;
        String filename = request.getParameter("name")!=null?request.getParameter("name"):request.getParameter("filename");
        String fileType = null;
        JSONObject grid = null;
        try {
            fileType = request.getParameter("filetype");
            if (request.getParameter("gridconfig") != null) {
                String gridconfig = request.getParameter("gridconfig");
                String get = request.getParameter("get")==null?"":request.getParameter("get");
                if(get.equalsIgnoreCase("24") || get.equalsIgnoreCase("25")) { //Aged Receivable
                    gridconfig = "{" +
                            "data:[{'header':'personname','title':'"+messageSource.getMessage("acc.agedPay.gridCustomer/AccName", null, RequestContextUtils.getLocale(request))+"','width':'150','align':''},{'header':'billno','title':'"+messageSource.getMessage("acc.agedPay.gridIno", null, RequestContextUtils.getLocale(request))+"','width':'180','align':''},{'header':'date','title':'"+messageSource.getMessage("acc.agedPay.gridDate", null, RequestContextUtils.getLocale(request))+"','width':'150','align':'date'},{'header':'duedate','title':'"+messageSource.getMessage("acc.agedPay.gridDueDate", null, RequestContextUtils.getLocale(request))+"','width':'150','align':'date'},{'header':'amountdue','title':'"+messageSource.getMessage("acc.agedPay.gridAmtDue", null, RequestContextUtils.getLocale(request))+"','width':'200','align':'currency'}]," +
                            "groupdata:{'groupBy':'personname','groupSummaryField':'amountdue','groupSummaryText':'"+messageSource.getMessage("acc.nee.2", null, RequestContextUtils.getLocale(request))+" ','reportSummaryField':'amountdueinbase','reportSummaryText':'"+messageSource.getMessage("acc.nee.3", null, RequestContextUtils.getLocale(request))+" '}" +
                            "}";
                } else if(get.equalsIgnoreCase("21") || get.equalsIgnoreCase("22")) { //Aged Payable
                    gridconfig = "{" +
                            "data:[{'header':'personname','title':'"+messageSource.getMessage("acc.agedPay.gridVendor/AccName", null, RequestContextUtils.getLocale(request))+"','width':'150','align':''},{'header':'billno','title':'"+messageSource.getMessage("acc.agedPay.gridVIno", null, RequestContextUtils.getLocale(request))+"','width':'180','align':''},{'header':'date','title':'"+messageSource.getMessage("acc.agedPay.gridDate", null, RequestContextUtils.getLocale(request))+"','width':'150','align':'date'},{'header':'duedate','title':'"+messageSource.getMessage("acc.agedPay.gridDueDate", null, RequestContextUtils.getLocale(request))+"','width':'150','align':'date'},{'header':'amountdue','title':'"+messageSource.getMessage("acc.agedPay.gridAmtDue", null, RequestContextUtils.getLocale(request))+"','width':'200','align':'currency'}]," +
                            "groupdata:{'groupBy':'personname','groupSummaryField':'amountdue','groupSummaryText':'"+messageSource.getMessage("acc.nee.2", null, RequestContextUtils.getLocale(request))+" ','reportSummaryField':'amountdueinbase','reportSummaryText':'"+messageSource.getMessage("acc.nee.4", null, RequestContextUtils.getLocale(request))+" '}" +
                            "}";
                } else if(get.equalsIgnoreCase("914")) {
                	gridconfig = "{" +
                			"data:[{'header':'productname','title':'"+messageSource.getMessage("acc.saleByItem.gridProduct", null, RequestContextUtils.getLocale(request))+"','width':'150','align':''},{'header':'billno','title':'"+messageSource.getMessage("acc.saleByItem.gridInvoice", null, RequestContextUtils.getLocale(request))+"','width':'100','align':''},{'header':'date','title':'"+messageSource.getMessage("acc.saleByItem.gridDate", null, RequestContextUtils.getLocale(request))+"','width':'100','align':'date'},{'header':'memo','title':'"+messageSource.getMessage("acc.saleByItem.gridMemo", null, RequestContextUtils.getLocale(request))+"','width':'100','align':''},{'header':'personname','title':'"+messageSource.getMessage("acc.saleByItem.gridCustName", null, RequestContextUtils.getLocale(request))+"','width':'100','align':''},{'header':'quantity','title':'"+messageSource.getMessage("acc.saleByItem.gridQty", null, RequestContextUtils.getLocale(request))+"','width':'100','align':''},{'header':'rateinbase','title':'"+messageSource.getMessage("acc.saleByItem.gridSalesPrice", null, RequestContextUtils.getLocale(request))+"','width':'100','align':'rowcurrency'},{'header':'amount','title':'"+messageSource.getMessage("acc.saleByItem.gridAmount", null, RequestContextUtils.getLocale(request))+"','width':'100','align':'rowcurrency'},{'header':'totalsales','title':'"+messageSource.getMessage("acc.saleByItem.gridBalance", null, RequestContextUtils.getLocale(request))+"','width':'100','align':'rowcurrency'}],"+
                			"groupdata:{'groupBy':'productname','groupSummaryField':'amount','groupSummaryText':'"+messageSource.getMessage("acc.nee.5", null, RequestContextUtils.getLocale(request))+" ','reportSummaryField':'amount','reportSummaryText':'"+messageSource.getMessage("acc.nee.6", null, RequestContextUtils.getLocale(request))+" '}" +
                			"}";
                }
                grid = new JSONObject(gridconfig);
            }
            if (StringUtil.equal(fileType, "csv")) {
                createCsvFile(request, response, jobj);
            } else if (StringUtil.equal(fileType, "pdf")) {
                baos = getPdfData(grid, request, jobj);
                writeDataToFile(filename, fileType, baos, response);
            } else if (StringUtil.equal(fileType, "print")) {
                createPrinPriviewFile(request, response, jobj);
            }
        } catch (ServiceException ex) {
            PrintWriter out = response.getWriter();
            out.println("<script type='text/javascript'>alert('Failed to Download Document. "+errorMsg+"');</script>");
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (Exception ex) {
            PrintWriter out = response.getWriter();
            out.println("<script type='text/javascript'>alert('Failed to Download Document. "+errorMsg+"');</script>");
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }

    public void createPrinPriviewFile(HttpServletRequest request, HttpServletResponse response, JSONObject obj) throws ServiceException {

        try {
            DateFormat formatter = authHandlerDAOObj.getUserDateFormatter(sessionHandlerImpl.getDateFormatID(request), sessionHandlerImpl.getTimeZoneDifference(request), true);
            DateFormat frmt = authHandler.getDateFormatter(request);
            String headers[] = null;
            String titles[] = null;
            String align[] = null;
            JSONArray repArr = new JSONArray();
            String searchjson = request.getParameter("searchJson");
            JSONObject json = null;
            JSONArray advSearch=null;
            String htmlCode="";
            String advStr="<ol>";
//            User userid = (User) session.load(User.class, AuthHandler.getUserid(request));
//            String  startdate = remoteapi.getUserDateFormatter1(userid, session, KWLDateFormat.DATE_PART).format(new Date());
            int report = Integer.parseInt(request.getParameter("get"));
            double totalCre = 0, totalDeb = 0;
            String startdate = obj.getString("GenerateDate");
            String currencyid = sessionHandlerImpl.getCurrencyID(request);
            if(!StringUtil.isNullOrEmpty(searchjson) && !StringUtil.equal(searchjson, "undefined")){
                json = new JSONObject(request.getParameter("searchJson"));
                advSearch=json.getJSONArray("root");
                for(int i=0;i<advSearch.length();i++) {
                    JSONObject key =advSearch.getJSONObject(i);
                    String value="";
                    String name = key.getString("columnheader");
                    name=URLDecoder.decode(name);
                    name.trim();
                    if(name.contains("*"))
                        name=name.substring(0,name.indexOf("*")-1);
                    if(name.contains("(") && name.charAt(name.indexOf("(")+1)=='&') {
                        htmlCode= name.substring(name.indexOf("(")+3,name.length()-2);
                        char temp=  (char) Integer.parseInt(htmlCode,10);
                        htmlCode=Character.toString(temp);
                        if(htmlCode.equals("$")) {
                            String currency = currencyRender(key.getString("combosearch"), currencyid);
                            name=name.substring(0, name.indexOf("(")-1);
                            name=name+"("+htmlCode+")";
                            value = currency;
                        } else {
                            name=name.substring(0, name.indexOf("(")-1);
                            value=name+" "+htmlCode;
                        }
                    } else
                        value=key.getString("combosearch");
                     advStr+="<li><font size=\"2\">"+name+" : "+value+"</font></li>";
                }
                advStr+="</ol>";
            }
            String ashtmlString = "<html> " +
                                        "<head>" +"<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />"+
                                        "<title>"+request.getParameter("name")+"</title>" +
                                        "<style type=\"text/css\">@media print {button#print {display: none;}}</style>"+
                                        "</head>" +
//                                        "<body style = \"font-family: Arial, Helvetica, sans-sarif;\">" +

                                            "<center><div style='padding-bottom: 5px; padding-right: 5px;'>" +
                                                "<h3> "+request.getParameter("name")+" </h3>" +
                                            "</div></center>";

            ashtmlString += "<div>" +
						    "<b><font size=\"2\">"+messageSource.getMessage("acc.nee.1", null, RequestContextUtils.getLocale(request))+" : </b>"+startdate+"</font>"+
                            "</div></br>";
            if(!StringUtil.isNullOrEmpty(searchjson) && !StringUtil.equal(searchjson, "undefined")){
                ashtmlString += "<div>" +
								"<b><font size=\"2\">Selection Criteria : </b></font>"+advStr+
                                "</div>";
            }

            String atempstr = "<DIV style='page-break-after:always'></DIV>";

            if (request.getParameter("header") != null) {
                String head = request.getParameter("header");
                String tit = request.getParameter("title");
                String alignstr = request.getParameter("align");
                tit=URLDecoder.decode(tit);
                headers = (String[]) head.split(",");
                titles = (String[]) tit.split(",");
                align = (String[]) alignstr.split(",");
            } else {
                headers = (String[]) obj.get("header");
                titles = (String[]) obj.get("title");
                align = (String[]) obj.get("align");
            }
            StringBuilder reportSB = new StringBuilder();

            if (obj.isNull("coldata")) {
                if(obj.has("data"))
                   repArr = obj.getJSONArray("data");
            } else {
                repArr = obj.getJSONArray("coldata");
            }

            for (int t = 0; t < repArr.length(); t++) {
                if(t!=0){
                    ashtmlString+="</br></br>";
                }
                ashtmlString+="<center>";
                ashtmlString += "<table cellspacing=0 border=1 cellpadding=2 width='100%' style='font-size:9pt'>";
                ashtmlString +="<tr>";
                for (int hCnt = -1; hCnt < titles.length; hCnt++) {
                    if(hCnt==-1)
                        ashtmlString +="<th>"+messageSource.getMessage("acc.cnList.Sno", null, RequestContextUtils.getLocale(request))+"</th>";
                    else
                        ashtmlString +="<th>"+titles[hCnt]+"</th>";
                }
                ashtmlString +="</tr>";
               for (int h = 0; h < 15; h++) {
                    if(repArr.length() - t != 0) {
                        String recordData = "<tr><td align=\"center\">"+(t+1)+"</td>";
                        JSONObject temp = repArr.getJSONObject(t);
                        if (report == 116) { //116:Trial Balance
                            totalCre = totalCre + Double.parseDouble(!StringUtil.isNullOrEmpty(temp.getString("c_amount")) ? temp.getString("c_amount") : "0");
                            totalDeb = totalDeb + Double.parseDouble(!StringUtil.isNullOrEmpty(temp.getString("d_amount")) ? temp.getString("d_amount") : "0");
                        }
                        for (int hCnt = 0; hCnt < headers.length; hCnt++) {
                            if(temp.has(headers[hCnt].toString())) {
                                String cellData = temp.getString(headers[hCnt]);
                                if (align[hCnt].equals("currency") && !cellData.equals("")) {
                                    cellData = htmlCurrencyRender(cellData, currencyid);
                                    recordData +="<td align=\"right\">"+cellData+"&nbsp;</td>";
                                } else if (align[hCnt].equals("rowcurrency") && !cellData.equals("")) {
                                    String rowCurrencyId = temp.has("currencyid")?temp.getString("currencyid"):currencyid;
                                    cellData = htmlCurrencyRender(cellData, rowCurrencyId);
                                    recordData +="<td align=\"right\">"+cellData+"&nbsp;</td>";
                                } else if (align[hCnt].equals("date") && !cellData.equals("")) {
                                    try {
                                        cellData = formatter.format(frmt.parse(cellData));
                                        recordData +="<td>"+cellData+"&nbsp;</td>";
                                    } catch (Exception ex) {
                                        recordData +="<td>"+cellData+"&nbsp;</td>";
                                    }
                                } else if (headers[hCnt].equals("taxrate") || headers[hCnt].equals("permargin") && !cellData.equals("")){
                                	recordData += htmlPercentageRender(cellData, false);
                            	} else {
                                    if (headers[hCnt].equals("invoiceno")) {
                                        cellData = temp.getString("no");
                                        recordData +="<td>"+cellData+"&nbsp;</td>";
                                    } else if (headers[hCnt].equals("invoicedate")) {
                                        cellData = formatter.format(frmt.parse(temp.getString("date")));
                                        recordData +="<td>"+cellData+"&nbsp;</td>";
                                    } else if (headers[hCnt].equals("c_date")) {
                                        cellData = formatter.format(frmt.parse((StringUtil.isNullOrEmpty(temp.getString("c_date")) ? temp.getString("d_date") : temp.getString("c_date"))));
                                        recordData +="<td>"+cellData+"&nbsp;</td>";
                                    } else if (headers[hCnt].equals("c_accountname")) {
                                        cellData = (StringUtil.isNullOrEmpty(temp.getString("c_accountname")) ? temp.getString("d_accountname") : temp.getString("c_accountname"));
                                        recordData +="<td>"+cellData+"&nbsp;</td>";
                                    } else if (headers[hCnt].equals("c_entryno") && !(temp.isNull(headers[hCnt])) && report != 117) {
                                        cellData = (StringUtil.isNullOrEmpty(temp.getString("c_entryno")) ? temp.getString("d_entryno") : temp.getString("c_entryno"));
                                        recordData +="<td>"+cellData+"&nbsp;</td>";
                                    } else if (headers[hCnt].equals("d_date")) {
                                        cellData = formatter.format(frmt.parse((StringUtil.isNullOrEmpty(temp.getString("d_date")) ? temp.getString("c_date") : temp.getString("c_date"))));
                                        recordData +="<td>"+cellData+"&nbsp;</td>";
                                    } else if (headers[hCnt].equals("d_accountname")) {
                                        cellData = (StringUtil.isNullOrEmpty(temp.getString("d_accountname")) ? temp.getString("c_accountname") : temp.getString("d_accountname"));
                                        recordData +="<td>"+cellData+"&nbsp;</td>";
                                    } else if (headers[hCnt].equals("d_entryno") && !(temp.isNull(headers[hCnt]))) {
                                        cellData = (StringUtil.isNullOrEmpty(temp.getString("d_entryno")) ? temp.getString("c_entryno") : temp.getString("d_entryno"));
                                        recordData +="<td>"+cellData+"&nbsp;</td>";
                                    } else if ((temp.isNull(headers[hCnt])) && !(headers[hCnt].equals("invoiceno")) && !(headers[hCnt].equals("invoicedate"))) {
                                        recordData +="<td>&nbsp;</td>";
                                    } else if (!(temp.isNull(headers[hCnt])) && headers[hCnt].equals("perioddepreciation")) {
                                        double adj = temp.getDouble("perioddepreciation") - temp.getDouble("firstperiodamt");
                                        String currency = currencyRender("" + adj, currencyid);
                                        if (adj < 0.0001) {
                                            cellData = "";
                                        } else {
                                            cellData = currency;
                                        }
                                        recordData +="<td>"+cellData+"&nbsp;</td>";
                                    } else if (titles[hCnt].equals("Opening Balance") || titles[hCnt].equals("Asset Value")) {
                                        String currency = currencyRender("" + Math.abs(temp.getDouble("openbalance")), currencyid);
                                        cellData = currency;
                                        recordData +="<td align=\"right\">"+cellData+"&nbsp;</td>";
                                    } else {
                                        if (titles[hCnt].equals("Opening Balance Type")) {
                                            try {
                                                double bal = Double.parseDouble(temp.getString(headers[hCnt]));
                                                String str1 = bal == 0 ? "" : (bal < 0 ? "Credit" : "Debit");
                                                if(str1.equals(""))
                                                	str1 = "N/A";
                                                cellData = str1;
                                            }catch(Exception ex){
                                                System.out.print(ex.getMessage());
                                            }

                                            recordData +="<td>"+cellData+"&nbsp;</td>";
                                        } else {
                                            recordData +="<td>"+cellData+"&nbsp;</td>";
                                        }
                                    }
                                }
                            } else {
                                recordData +="<td>&nbsp;</td>";
                            }
                        }
                        ashtmlString += recordData + "</tr>";
                        t++;
                    } else {
                        atempstr="";
                    }
                }
                if (report == 116) { //116:Trial Balance
                    String recordData = "<tr><td align=\"center\">&nbsp;</td><td>Total</td>";
                    for (int h = 1; h < headers.length; h++) {
                        if (headers[h].equals("c_amount")) {
                            recordData +="<td align=\"right\">"+htmlCurrencyRender(String.valueOf(totalCre), currencyid)+"&nbsp;</td>";
                        } else if (headers[h].equals("d_amount")) {
                            recordData +="<td align=\"right\">"+htmlCurrencyRender(String.valueOf(totalDeb), currencyid)+"&nbsp;</td>";
                        } else {
                            recordData +="<td>&nbsp;</td>";
                        }
                    }
                    ashtmlString += recordData + "</tr>";
                }
                ashtmlString += "</table>";
                ashtmlString += "</center>";
                if(t!=repArr.length()-1) {
                    ashtmlString += atempstr;
                }
                t--;
            }
            ashtmlString +="<div style='float: left; padding-top: 3px; padding-right: 5px;'>" +
                                    "<button id = 'print' title='Print Invoice' onclick='window.print();' style='color: rgb(8, 55, 114);' href='#'>"+messageSource.getMessage("acc.common.print", null, RequestContextUtils.getLocale(request)) +"</button>" +
                                "</div>" ;
            ashtmlString +="</body>" +
            "</html>";
            String fname = request.getParameter("name");
            response.getOutputStream().write(ashtmlString.getBytes());
            response.getOutputStream().flush();
        } catch (SessionExpiredException ex) {
//            errorMsg = ex.getMessage();
            throw ServiceException.FAILURE("exportMPXDAOImpl.createPrinPriviewFile : " + ex.getMessage(), ex);
        } catch (IOException ex) {
//            errorMsg = ex.getMessage();
            throw ServiceException.FAILURE("exportMPXDAOImpl.createPrinPriviewFile : " + ex.getMessage(), ex);
        } catch (JSONException ex) {
//            errorMsg = ex.getMessage();
            throw ServiceException.FAILURE("exportMPXDAOImpl.createPrinPriviewFile : " + ex.getMessage(), ex);
        } catch (Exception ex) {
            String str;
//            errorMsg = ex.getMessage();
            throw ServiceException.FAILURE("exportMPXDAOImpl.createPrinPriviewFile : " + ex.getMessage(), ex);
        }
    }

    public void writeDataToFile(String filename, String fileType, ByteArrayOutputStream baos, HttpServletResponse response) throws ServiceException {
        try {
            response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "." + fileType + "\"");
            response.setContentType("application/octet-stream");
            response.setContentLength(baos.size());
            response.getOutputStream().write(baos.toByteArray());
            response.getOutputStream().flush();
            response.getOutputStream().close();
        } catch (Exception e) {
            try {
                response.getOutputStream().println("{\"valid\": false}");
            } catch (IOException ex) {
                Logger.getLogger(exportDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void addComponyLogo(Document d, HttpServletRequest request) throws ServiceException {
        try {
            PdfPTable table = new PdfPTable(1);
            imgPath = getImgPath(request);
            table.setHorizontalAlignment(Element.ALIGN_LEFT);
            table.setWidthPercentage(50);
            PdfPCell cell = null;
            try {
                Image img = Image.getInstance(imgPath);
                cell = new PdfPCell(img);
            } catch (Exception e) {
                companyName = sessionHandlerImplObj.getCompanyName(request);
                cell = new PdfPCell(new Paragraph(fontFamilySelector.process(companyName, FontContext.HEADER_NOTE)));
            }
            cell.setBorder(0);
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            table.addCell(cell);
            d.add(table);
        } catch (Exception e) {
            throw ServiceException.FAILURE("exportDAOImpl.addComponyLogo", e);
        }
    }

    public String getImgPath(HttpServletRequest req) throws SessionExpiredException {
        String requestedFileName = "";
        String companyId = null;
        try {
            companyId = sessionHandlerImplObj.getCompanyid(req);
        } catch (Exception ee) {
        }
        if (StringUtil.isNullOrEmpty(companyId)) {
            String domain = URLUtil.getDomainName(req);
            if (!StringUtil.isNullOrEmpty(domain)) {
                companyId = sessionHandlerImplObj.getCompanyid(req);
                requestedFileName = "/original_" + companyId + ".png";
            } else {
                requestedFileName = "logo.gif";
            }
        } else {
            requestedFileName = companyId + ".png";
        }
        String fileName = storageHandlerImplObj.GetProfileImgStorePath() + requestedFileName;
        return fileName;
    }

    public void addTitleSubtitle(Document d) throws ServiceException {
        try {
            java.awt.Color tColor = new Color(Integer.parseInt(config.getString("textColor"), 16));
//            fontBold.setColor(tColor);
//            fontRegular.setColor(tColor);
            PdfPTable table = new PdfPTable(1);
            table.setHorizontalAlignment(Element.ALIGN_CENTER);

            table.setWidthPercentage(100);
            table.setSpacingBefore(6);

            //Report Title
            PdfPCell cell = new PdfPCell(new Paragraph(fontFamilySelector.process(config.getString("title"), FontContext.REPORT_TITLE,tColor)));
            cell.setBorder(0);
            cell.setBorderWidth(0);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);

            //Report Subtitle(s)
            String[] SubTitles = config.getString("subtitles").split("~");// '~' as separator
            for (int i = 0; i < SubTitles.length; i++) {
                cell = new PdfPCell(new Paragraph((new Phrase(fontFamilySelector.process(SubTitles[i], FontContext.FOOTER_NOTE)))));
                cell.setBorder(0);
                cell.setBorderWidth(0);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }
            table.setSpacingAfter(6);
            d.add(table);

            //Separator line
            PdfPTable line = new PdfPTable(1);
            line.setWidthPercentage(100);
            PdfPCell cell1 = null;
            cell1 = new PdfPCell(new Paragraph(""));
            cell1.setBorder(PdfPCell.BOTTOM);
            line.addCell(cell1);
            d.add(line);
        } catch (Exception e) {
            throw ServiceException.FAILURE("exportDAOImpl.addTitleSubtitle", e);
        }
    }

    public int addTable(int stcol, int stpcol, int strow, int stprow, JSONArray store, String[] colwidth2, String[] colHeader, String[] widths, String[] align, Document document, HttpServletRequest request) throws ServiceException {
        try {
            DateFormat formatter = authHandlerDAOObj.getUserDateFormatter(sessionHandlerImpl.getDateFormatID(request), sessionHandlerImpl.getTimeZoneDifference(request), true);
            DateFormat frmt = authHandler.getDateFormatter(request);
            String currencyid = sessionHandlerImpl.getCurrencyID(request);
            int mode = Integer.parseInt(request.getParameter("get"));
            double totalCre = 0;
            double totalDeb = 0;
            java.awt.Color tColor = new Color(Integer.parseInt(config.getString("textColor"), 16));
//            fontSmallRegular.setColor(tColor);
            PdfPTable table;
            float[] tcol;
            tcol = new float[colHeader.length + 1];
            tcol[0] = 40;
            for (int i = 1; i < colHeader.length + 1; i++) {
                tcol[i] = Float.parseFloat(widths[i - 1]);
            }
            table = new PdfPTable(colHeader.length + 1);
            table.setWidthPercentage(tcol, document.getPageSize());
            table.setSpacingBefore(15);
            PdfPCell h2 = new PdfPCell(new Paragraph((new Phrase(fontFamilySelector.process("No.", FontContext.FOOTER_NOTE, tColor)))));
            if (config.getBoolean("gridBorder")) {
                h2.setBorder(PdfPCell.BOX);
            } else {
                h2.setBorder(0);
            }
            h2.setPadding(4);
            h2.setBorderColor(Color.GRAY);
            h2.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(h2);
            PdfPCell h1 = null;
            for (int hcol = stcol; hcol < colwidth2.length; hcol++) {
                String headerStr = StringUtil.serverHTMLStripper(colHeader[hcol]);
                if (align[hcol].equals("currency") && !colHeader[hcol].equals("")) {
                    String currency = currencyRender("", currencyid);
                    h1 = new PdfPCell(new Paragraph((new Phrase(fontFamilySelector.process(headerStr + "(" + currency + ")", FontContext.FOOTER_NOTE, tColor)))));
                } else {
                    h1 = new PdfPCell(new Paragraph((new Phrase(fontFamilySelector.process(headerStr, FontContext.FOOTER_NOTE, tColor)))));
                }
                h1.setHorizontalAlignment(Element.ALIGN_CENTER);
                if (config.getBoolean("gridBorder")) {
                    h1.setBorder(PdfPCell.BOX);
                } else {
                    h1.setBorder(0);
                }
                h1.setBorderColor(Color.GRAY);
                h1.setPadding(4);
                table.addCell(h1);
            }
            table.setHeaderRows(1);

            for (int row = strow; row < stprow; row++) {
                h2 = new PdfPCell(new Paragraph(fontFamilySelector.process(String.valueOf(row + 1), FontContext.TABLE_DATA)));
                if (config.getBoolean("gridBorder")) {
                    h2.setBorder(PdfPCell.BOTTOM | PdfPCell.LEFT | PdfPCell.RIGHT);
                } else {
                    h2.setBorder(0);
                }
                h2.setPadding(4);
                h2.setBorderColor(Color.GRAY);
                h2.setHorizontalAlignment(Element.ALIGN_CENTER);
                h2.setVerticalAlignment(Element.ALIGN_CENTER);
                table.addCell(h2);

                JSONObject temp = store.getJSONObject(row);
                if (mode == 116 || mode == 117) {
                    totalCre = totalCre + Double.parseDouble(temp.getString("c_amount") != "" ? temp.getString("c_amount") : "0");
                    totalDeb = totalDeb + Double.parseDouble(temp.getString("d_amount") != "" ? temp.getString("d_amount") : "0");
                }
                for (int col = 0; col < colwidth2.length; col++) {
                    Paragraph para = null;
                    String rowCurrencyId = temp.has("currencyid")?temp.getString("currencyid"):currencyid;
                    if (align[col].equals("currency") && !temp.getString(colwidth2[col]).equals("")) {
                        String currency = currencyRender(temp.getString(colwidth2[col]), currencyid);
                        para = new Paragraph(fontFamilySelector.process(currency, FontContext.TABLE_DATA));
                    } else if (align[col].equals("rowcurrency") && !temp.getString(colwidth2[col]).equals("")) {
                        String withCurrency = currencyRender(temp.getString(colwidth2[col]), rowCurrencyId);
                        para = new Paragraph(fontFamilySelector.process(withCurrency, FontContext.TABLE_DATA));
                    } else if (align[col].equals("date") && !temp.getString(colwidth2[col]).equals("")) {
                        try {
                            String d1 = formatter.format(frmt.parse(temp.getString(colwidth2[col])));
                            para = new Paragraph(fontFamilySelector.process(d1, FontContext.TABLE_DATA));
                        } catch (Exception ex) {
                            para = new Paragraph(fontFamilySelector.process(temp.getString(colwidth2[col]), FontContext.TABLE_DATA));
                        }
                    } else if (colwidth2[col].equals("taxrate") || colwidth2[col].equals("permargin") && !colHeader[col].equals("")){
                    	para = new Paragraph(fontFamilySelector.process(htmlPercentageRender(temp.getString(colwidth2[col]), true), FontContext.TABLE_DATA));
                    } else {
                        if (colwidth2[col].equals("invoiceno")) {
                            para = new Paragraph(fontFamilySelector.process(temp.getString("no").toString(), FontContext.TABLE_DATA));
                        } else if (colwidth2[col].equals("invoicedate")) {
                            para = new Paragraph(fontFamilySelector.process(temp.getString("date").toString(), FontContext.TABLE_DATA));
                        } else if ((temp.isNull(colwidth2[col])) && !(colwidth2[col].equals("invoiceno")) && !(colwidth2[col].equals("invoicedate"))) {
                            para = new Paragraph(fontFamilySelector.process("", FontContext.TABLE_DATA));
                        } else if (colwidth2[col].equals("c_date")) {
                            para = new Paragraph(fontFamilySelector.process(formatter.format(frmt.parse(temp.getString("c_date").toString() == "" ? temp.getString("d_date") : temp.getString("c_date"))), FontContext.TABLE_DATA));
                        } else if (colwidth2[col].equals("c_accountname")) {
                            para = new Paragraph(fontFamilySelector.process(temp.getString("c_accountname").toString() == "" ? temp.getString("d_accountname").toString() : temp.getString("c_accountname").toString(), FontContext.TABLE_DATA));
                        } else if (colwidth2[col].equals("c_entryno")) {
                            para = new Paragraph(fontFamilySelector.process(temp.getString("c_entryno").toString() == "" ? temp.getString("d_entryno").toString() : temp.getString("c_entryno").toString(), FontContext.TABLE_DATA));
                        } else if (colwidth2[col].equals("d_date")) {
                            para = new Paragraph(fontFamilySelector.process(formatter.format(frmt.parse(temp.getString("d_date").toString() == "" ? temp.getString("c_date") : temp.getString("d_date"))), FontContext.TABLE_DATA));
                        } else if (colwidth2[col].equals("d_accountname")) {
                            para = new Paragraph(fontFamilySelector.process(temp.getString("d_accountname").toString() == "" ? temp.getString("c_accountname").toString() : temp.getString("d_accountname").toString(), FontContext.TABLE_DATA));
                        } else if (colwidth2[col].equals("d_entryno")) {
                            para = new Paragraph(fontFamilySelector.process(temp.getString("d_entryno").toString() == "" ? temp.getString("c_entryno").toString() : temp.getString("d_entryno").toString(), FontContext.TABLE_DATA));
                        } else if (colwidth2[col].equals("perioddepreciation")) {
                            double adj = temp.getDouble("perioddepreciation") - temp.getDouble("firstperiodamt");
                            String currency = currencyRender("" + adj, currencyid);
                            if (adj < 0.0001) {
                                para = new Paragraph(fontFamilySelector.process("", FontContext.TABLE_DATA));
                            } else {
                                para = new Paragraph(fontFamilySelector.process(currency, FontContext.TABLE_DATA));
                            }
                        } else if (colHeader[col].equals("Opening Balance") || colHeader[col].equals("Asset Value")) {
                            String currency = currencyRender("" + Math.abs(temp.getDouble("openbalance")), currencyid);
                            para = new Paragraph(fontFamilySelector.process(currency, FontContext.TABLE_DATA));
                        } else {
                            if (colHeader[col].equals("Opening Balance Type")) {
                                double bal = Double.parseDouble(temp.getString(colwidth2[col]));
                                String str = bal == 0 ? "" : (bal < 0 ? "Credit" : "Debit");
                                if(str.equals(""))
                                	str = "N/A";
                                para = new Paragraph(fontFamilySelector.process(str, FontContext.TABLE_DATA));
                            } else {
                                para = new Paragraph(fontFamilySelector.process(temp.getString(colwidth2[col]).toString(), FontContext.TABLE_DATA));
                            }
                        }
                    }
                    h1 = new PdfPCell(para);
                    if (config.getBoolean("gridBorder")) {
                        h1.setBorder(PdfPCell.BOTTOM | PdfPCell.LEFT | PdfPCell.RIGHT);
                    } else {
                        h1.setBorder(0);
                    }
                    h1.setPadding(4);
                    h1.setBorderColor(Color.GRAY);
                    if (align[col].equals("currency") || align[col].equals("rowcurrency") || colwidth2[col].equals("taxrate") || colwidth2[col].equals("permargin")) {
                        h1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        h1.setVerticalAlignment(Element.ALIGN_RIGHT);
                    } else if (align[col].equals("date")) {
                        h1.setHorizontalAlignment(Element.ALIGN_CENTER);
                        h1.setVerticalAlignment(Element.ALIGN_CENTER);
                    } else {
                        h1.setHorizontalAlignment(Element.ALIGN_LEFT);
                        h1.setVerticalAlignment(Element.ALIGN_LEFT);
                    }
                    table.addCell(h1);
                }
            }
            if (mode == 116 || mode == 117) {
                Paragraph para1 = null;
                PdfPCell h3 = null;
                String totCr = "";
                String totDb = "";
                h3 = new PdfPCell(new Paragraph(fontFamilySelector.process("", FontContext.TABLE_DATA)));
                if (config.getBoolean("gridBorder")) {
                    h3.setBorder(PdfPCell.BOTTOM | PdfPCell.LEFT);
                } else {
                    h3.setBorder(0);
                }
                h3.setPadding(4);
                h3.setBorderColor(Color.GRAY);
                h3.setBackgroundColor(Color.lightGray);
                h3.setHorizontalAlignment(Element.ALIGN_CENTER);
                h3.setVerticalAlignment(Element.ALIGN_CENTER);
                table.addCell(h3);
                para1 = new Paragraph(fontFamilySelector.process("Total", FontContext.REPORT_TITLE));
                h3 = new PdfPCell(para1);
                if (config.getBoolean("gridBorder")) {
                    h3.setBorder(PdfPCell.BOTTOM | PdfPCell.LEFT | PdfPCell.RIGHT);
                } else {
                    h3.setBorder(0);
                }
                h3.setPadding(4);
                h3.setBorderColor(Color.GRAY);
                h3.setBackgroundColor(Color.LIGHT_GRAY);
                h3.setHorizontalAlignment(Element.ALIGN_LEFT);
                h3.setVerticalAlignment(Element.ALIGN_LEFT);
                table.addCell(h3);

                for (int col = 1; col < colwidth2.length; col++) {
                    if (colwidth2[col].equals("c_amount")) {
                        totCr = currencyRender(String.valueOf(totalCre), currencyid);
                        para1 = new Paragraph(fontFamilySelector.process(totCr, FontContext.TABLE_DATA));
                    } else if (colwidth2[col].equals("d_amount")) {
                        totDb = currencyRender(String.valueOf(totalDeb), currencyid);
                        para1 = new Paragraph(fontFamilySelector.process(totDb, FontContext.TABLE_DATA));
                    } else {
                        para1 = new Paragraph(fontFamilySelector.process("", FontContext.TABLE_DATA));
                    }

                    h3 = new PdfPCell(para1);
                    if (config.getBoolean("gridBorder")) {
                        h3.setBorder(PdfPCell.BOTTOM | PdfPCell.LEFT | PdfPCell.RIGHT);
                    } else {
                        h3.setBorder(0);
                    }
                    h3.setPadding(4);
                    h3.setBorderColor(Color.GRAY);
                    h3.setBackgroundColor(Color.LIGHT_GRAY);
                    h3.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    h3.setVerticalAlignment(Element.ALIGN_RIGHT);
                    table.addCell(h3);

                }
            }
            document.add(table);
            document.newPage();
        } catch (Exception e) {
            throw ServiceException.FAILURE("exportDAOImpl.addTable", e);
        }
        return stpcol;
    }

    public int addGroupableTable(JSONObject groupingConfig, int stcol, int stpcol, int strow, int stprow, JSONArray store, String[] dataIndexArr, String[] colHeader, String[] widths, String[] align, Document document, HttpServletRequest request) throws ServiceException {
        try {
            String groupByField = groupingConfig.getString("groupBy");
            String groupHeaderText = "";
            boolean showGroupByColumn = false;

            if(!showGroupByColumn) {
                ArrayList<String> newdataIndexs = new ArrayList<String>();
                ArrayList<String> newColHeaders = new ArrayList<String>();
                ArrayList<String> newColWidths = new ArrayList<String>();
                ArrayList<String> newColAlign = new ArrayList<String>();
                for (int i = 0; i < dataIndexArr.length; i++) {
                    if(dataIndexArr[i].equalsIgnoreCase(groupByField)) {
                        groupHeaderText = colHeader[i];
                        continue;   //Remove all groupByField column's config to hide groupByField column in table
                    }
                    newdataIndexs.add(dataIndexArr[i]);
                    newColHeaders.add(colHeader[i]);
                    newColWidths.add(widths[i]);
                    newColAlign.add(align[i]);
                }
                dataIndexArr = new String[newdataIndexs.size()];
                colHeader = new String[newdataIndexs.size()];
                widths = new String[newdataIndexs.size()];
                align = new String[newdataIndexs.size()];
                for (int i = 0; i < newdataIndexs.size(); i++) {
                    dataIndexArr[i] = newdataIndexs.get(i);
                    colHeader[i] = newColHeaders.get(i);
                    widths[i] = newColWidths.get(i);
                    align[i] = newColAlign.get(i);
                }
            }
            return addGroupableTable(groupingConfig, groupByField, groupHeaderText, stcol, stpcol, strow, stprow, store, dataIndexArr, colHeader, widths, align, document, request);
        } catch (Exception e) {
            throw ServiceException.FAILURE("exportDAOImpl.addGroupableTable", e);
        }
    }
    public int addGroupableTable(JSONObject groupingConfig, String groupByField, String groupHeaderText, int stcol, int stpcol, int strow, int stprow, JSONArray store, String[] dataIndexArr, String[] colHeader, String[] widths, String[] align, Document document, HttpServletRequest request) throws ServiceException {
        try {
            String groupSummaryField = groupingConfig.getString("groupSummaryField");
            String groupSummaryText = groupingConfig.getString("groupSummaryText");
            String reportSummaryField = groupingConfig.getString("reportSummaryField");
            String reportSummaryText = groupingConfig.getString("reportSummaryText");

            DateFormat formatter = authHandlerDAOObj.getUserDateFormatter(sessionHandlerImpl.getDateFormatID(request), sessionHandlerImpl.getTimeZoneDifference(request), true);
            DateFormat frmt = authHandler.getDateFormatter(request);
            String currencyid = sessionHandlerImpl.getCurrencyID(request);
            java.awt.Color tColor = new Color(Integer.parseInt(config.getString("textColor"), 16));
            java.awt.Color gsBGColor = new Color(Integer.parseInt("E5E5E5", 16));
            java.awt.Color rsBGColor = new Color(Integer.parseInt("808080", 16));
//            fontRegular.setColor(tColor);
            PdfPTable table;

            float[] tcol;
            tcol = new float[colHeader.length + 1];
            tcol[0] = 40;
            for (int i = 1; i < colHeader.length + 1; i++) {
                tcol[i] = Float.parseFloat(widths[i - 1]);
            }
            table = new PdfPTable(colHeader.length + 1);
            table.setWidthPercentage(tcol, document.getPageSize());
            table.setSpacingBefore(15);
            PdfPCell h2 = new PdfPCell(new Paragraph(fontFamilySelector.process("No.", FontContext.TABLE_HEADER, tColor)));
            if (config.getBoolean("gridBorder")) {
                h2.setBorder(PdfPCell.BOX);
            } else {
                h2.setBorder(0);
            }
            h2.setPadding(4);
            h2.setBorderColor(Color.GRAY);
            h2.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(h2);
            PdfPCell h1 = null;
            for (int hcol = stcol; hcol < dataIndexArr.length; hcol++) {
                if (align[hcol].equals("currency") && !colHeader[hcol].equals("")) {
                    String currency = currencyRender("", currencyid);
                    h1 = new PdfPCell(new Paragraph(fontFamilySelector.process(colHeader[hcol] + "(" + currency + ")", FontContext.TABLE_HEADER, tColor)));
                } else {
                    h1 = new PdfPCell(new Paragraph(fontFamilySelector.process(colHeader[hcol], FontContext.TABLE_HEADER, tColor)));
                }
                h1.setHorizontalAlignment(Element.ALIGN_CENTER);
                if (config.getBoolean("gridBorder")) {
                    h1.setBorder(PdfPCell.BOX);
                } else {
                    h1.setBorder(0);
                }
                h1.setBorderColor(Color.GRAY);
                h1.setPadding(4);
                table.addCell(h1);
            }
            table.setHeaderRows(1);
            String groupName = "", rowCurrency="";
            Double subTotal = 0.0;
            Double grandTotal = 0.0;
            int rowSpan = 0;
            for (int row = strow; row < stprow; row++) {
                rowSpan++;
                JSONObject rowData = store.getJSONObject(row);
                if(row==0) {
                    groupName = rowData.getString(groupByField);
                    rowCurrency = rowData.has("currencyid")?rowData.getString("currencyid"):currencyid;
                    subTotal = 0.0;
                    addGroupRow(groupHeaderText+": "+groupName, currencyid, table, dataIndexArr);
                }
                if(!groupName.equalsIgnoreCase(rowData.getString(groupByField))) {
                    addSummaryRow(groupSummaryText+groupName+" ", subTotal, rowCurrency, table, dataIndexArr, false, gsBGColor);
                    groupName = rowData.getString(groupByField);
                    rowCurrency = rowData.has("currencyid")?rowData.getString("currencyid"):currencyid;
                    addGroupRow(groupHeaderText+": "+groupName, currencyid, table, dataIndexArr);
                    subTotal = 0.0;
                    rowSpan = 1;
                }
                subTotal += Double.parseDouble(rowData.getString(groupSummaryField));
                grandTotal += Double.parseDouble(rowData.getString(reportSummaryField));
                rowCurrency = rowData.has("currencyid")?rowData.getString("currencyid"):currencyid;

                h2 = new PdfPCell(new Paragraph(fontFamilySelector.process(String.valueOf(row + 1), FontContext.TABLE_HEADER, tColor)));
                if (config.getBoolean("gridBorder")) {
                    h2.setBorder(PdfPCell.BOTTOM | PdfPCell.LEFT | PdfPCell.RIGHT);
                } else {
                    h2.setBorder(0);
                }
                h2.setPadding(4);
                h2.setBorderColor(Color.GRAY);
                h2.setHorizontalAlignment(Element.ALIGN_CENTER);
                h2.setVerticalAlignment(Element.ALIGN_CENTER);
                table.addCell(h2);

                for (int col = 0; col < dataIndexArr.length; col++) {
                    String cellData = null;
                    if (align[col].equals("currency") && !rowData.getString(dataIndexArr[col]).equals("")) {
                        cellData = currencyRender(rowData.getString(dataIndexArr[col]), rowData.getString("currencyid"));
                    } else if (align[col].equals("date") && !rowData.getString(dataIndexArr[col]).equals("")) {
                        try {
                            cellData = formatter.format(frmt.parse(rowData.getString(dataIndexArr[col])));
                        } catch (Exception ex) {
                            cellData = rowData.getString(dataIndexArr[col]);
                        }
                    } else {
                        cellData = rowData.getString(dataIndexArr[col]);
                    }


                    Paragraph para = new Paragraph(fontFamilySelector.process(cellData, FontContext.TABLE_HEADER, tColor));
                    h1 = new PdfPCell(para);
                    if (config.getBoolean("gridBorder")) {
                        h1.setBorder(PdfPCell.BOTTOM | PdfPCell.LEFT | PdfPCell.RIGHT);
                    } else {
                        h1.setBorder(0);
                    }
                    h1.setPadding(4);
                    h1.setBorderColor(Color.GRAY);

                    if (!align[col].equals("currency") && !align[col].equals("date")) {
                        h1.setHorizontalAlignment(Element.ALIGN_LEFT);
                        h1.setVerticalAlignment(Element.ALIGN_LEFT);
                    } else if (align[col].equals("currency")) {
                        h1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        h1.setVerticalAlignment(Element.ALIGN_RIGHT);
                    } else if (align[col].equals("date")) {
                        h1.setHorizontalAlignment(Element.ALIGN_CENTER);
                        h1.setVerticalAlignment(Element.ALIGN_CENTER);
                    }
                    table.addCell(h1);
                }
            }
            if(rowSpan>0) {
                addSummaryRow(groupSummaryText+groupName+" ", subTotal, rowCurrency, table, dataIndexArr, false, gsBGColor);
            }
            addSummaryRow(reportSummaryText, grandTotal, currencyid, table, dataIndexArr, false, rsBGColor);

            document.add(table);
            document.newPage();
        } catch (Exception e) {
            throw ServiceException.FAILURE("exportDAOImpl.addTable", e);
        }
        return stpcol;
    }

    public void addSummaryRow(String summeryText, double subTotal, String currencyid, PdfPTable table, String[] dataIndexArr, boolean addBlankRow, java.awt.Color bgColor) throws JSONException, SessionExpiredException {
        Paragraph para = new Paragraph(fontFamilySelector.process(summeryText, FontContext.TABLE_HEADER));
        PdfPCell h1 = new PdfPCell(para);
        if (config.getBoolean("gridBorder")) {
            h1.setBorder(PdfPCell.TOP | PdfPCell.BOTTOM | PdfPCell.LEFT | PdfPCell.RIGHT);
        } else {
            h1.setBorder(PdfPCell.TOP);
        }
        h1.setPadding(4);
        h1.setBorderColor(Color.GRAY);
        h1.setHorizontalAlignment(Element.ALIGN_RIGHT);
        h1.setVerticalAlignment(Element.ALIGN_RIGHT);
        h1.setColspan(dataIndexArr.length);
        h1.setBackgroundColor(bgColor);
        table.addCell(h1);

        String withCurrency = currencyRender(Double.toString(subTotal), currencyid);
        para = new Paragraph(fontFamilySelector.process(withCurrency, FontContext.TABLE_HEADER));
        h1 = new PdfPCell(para);
        if (config.getBoolean("gridBorder")) {
            h1.setBorder(PdfPCell.TOP | PdfPCell.BOTTOM | PdfPCell.LEFT | PdfPCell.RIGHT);
        } else {
            h1.setBorder(PdfPCell.TOP);
        }
        h1.setPadding(4);
        h1.setBorderColor(Color.GRAY);
        h1.setHorizontalAlignment(Element.ALIGN_RIGHT);
        h1.setVerticalAlignment(Element.ALIGN_RIGHT);
        h1.setBackgroundColor(bgColor);
        table.addCell(h1);

        if (addBlankRow) {
            para = new Paragraph(fontFamilySelector.process(" ", FontContext.TABLE_HEADER));
            h1 = new PdfPCell(para);
            if (config.getBoolean("gridBorder")) {
                h1.setBorder(PdfPCell.BOTTOM | PdfPCell.LEFT | PdfPCell.RIGHT);
            } else {
                h1.setBorder(0);
            }
            h1.setPadding(4);
            h1.setBorderColor(Color.GRAY);
            h1.setHorizontalAlignment(Element.ALIGN_LEFT);
            h1.setVerticalAlignment(Element.ALIGN_LEFT);
            h1.setColspan(dataIndexArr.length+1);
            table.addCell(h1);
        }
    }

    public void addGroupRow(String groupText, String currencyid, PdfPTable table, String[] dataIndexArr) throws JSONException, SessionExpiredException {
        Paragraph para = new Paragraph(fontFamilySelector.process(groupText, FontContext.REPORT_TITLE));
        PdfPCell h1 = new PdfPCell(para);
        if (config.getBoolean("gridBorder")) {
            h1.setBorder(PdfPCell.BOTTOM | PdfPCell.LEFT | PdfPCell.RIGHT);
        } else {
            h1.setBorder(PdfPCell.BOTTOM);
        }
        h1.setBorderWidthBottom(1);
        h1.setPadding(4);
        h1.setBorderColor(Color.GRAY);
        h1.setBorderColorBottom(Color.DARK_GRAY);
        h1.setHorizontalAlignment(Element.ALIGN_LEFT);
        h1.setVerticalAlignment(Element.ALIGN_LEFT);
        h1.setColspan(dataIndexArr.length+1);
        table.addCell(h1);
    }

    public void getHeaderFooter(Document document) throws ServiceException {
        try {
            java.awt.Color tColor = new Color(Integer.parseInt(config.getString("textColor"), 16));
//            fontSmallRegular.setColor(tColor);
            java.util.Date dt = new java.util.Date();
            String date = "yyyy-MM-dd";
            java.text.SimpleDateFormat dtf = new java.text.SimpleDateFormat(date);
            String DateStr = dtf.format(dt);

            // -------- header ----------------
            header = new PdfPTable(3);
            String HeadDate = "";
            if (config.getBoolean("headDate")) {
                HeadDate = DateStr;
            }
            PdfPCell headerDateCell = new PdfPCell(new Phrase(fontFamilySelector.process(HeadDate, FontContext.FOOTER_NOTE, tColor)));
            headerDateCell.setBorder(0);
            headerDateCell.setPaddingBottom(4);
            header.addCell(headerDateCell);

            PdfPCell headerNotecell = new PdfPCell(new Phrase(fontFamilySelector.process(config.getString("headNote"), FontContext.FOOTER_NOTE, tColor)));
            headerNotecell.setBorder(0);
            headerNotecell.setPaddingBottom(4);
            headerNotecell.setHorizontalAlignment(PdfCell.ALIGN_CENTER);
            header.addCell(headerNotecell);

            String HeadPager = "";
            if (config.getBoolean("headPager")) {
                HeadPager = String.valueOf(document.getPageNumber());//current page no
            }
            PdfPCell headerPageNocell = new PdfPCell(new Phrase(fontFamilySelector.process(HeadPager, FontContext.FOOTER_NOTE, tColor)));
            headerPageNocell.setBorder(0);
            headerPageNocell.setPaddingBottom(4);
            headerPageNocell.setHorizontalAlignment(PdfCell.ALIGN_RIGHT);
            header.addCell(headerPageNocell);

            PdfPCell headerSeparator = new PdfPCell(new Phrase(""));
            headerSeparator.setBorder(PdfPCell.BOX);
            headerSeparator.setPadding(0);
            headerSeparator.setColspan(3);
            header.addCell(headerSeparator);
            // -------- header end ----------------

            // -------- footer  -------------------
            footer = new PdfPTable(3);
            PdfPCell footerSeparator = new PdfPCell(new Phrase(""));
            footerSeparator.setBorder(PdfPCell.BOX);
            footerSeparator.setPadding(0);
            footerSeparator.setColspan(3);
            footer.addCell(footerSeparator);

            String PageDate = "";
            if (config.getBoolean("footDate")) {
                PageDate = DateStr;
            }
            PdfPCell pagerDateCell = new PdfPCell(new Phrase(fontFamilySelector.process(PageDate, FontContext.FOOTER_NOTE, tColor)));
            pagerDateCell.setBorder(0);
            footer.addCell(pagerDateCell);

            PdfPCell footerNotecell = new PdfPCell(new Phrase(fontFamilySelector.process(config.getString("footNote"), FontContext.FOOTER_NOTE, tColor)));
            footerNotecell.setBorder(0);
            footerNotecell.setHorizontalAlignment(PdfCell.ALIGN_CENTER);
            footer.addCell(footerNotecell);

            String FootPager = "";
            if (config.getBoolean("footPager")) {
                FootPager = String.valueOf(document.getPageNumber());//current page no
            }
            PdfPCell footerPageNocell = new PdfPCell(new Phrase(fontFamilySelector.process(FootPager, FontContext.FOOTER_NOTE, tColor)));
            footerPageNocell.setBorder(0);
            footerPageNocell.setHorizontalAlignment(PdfCell.ALIGN_RIGHT);
            footer.addCell(footerPageNocell);
        // -------- footer end   -----------
        } catch (Exception e) {
            throw ServiceException.FAILURE("exportDAOImpl.getHeaderFooter", e);
        }
    }

    public ByteArrayOutputStream getPdfData(JSONObject grid, HttpServletRequest request, JSONObject obj) throws ServiceException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = null;
        Document document = null;
        try {
            JSONArray gridmap = grid==null?null:grid.getJSONArray("data");
            String colHeader = "";
            String colHeaderFinal = "";
            String fieldListFinal = "";
            String fieldList = "";
            String width = "";
            String align = "";
            String alignFinal = "";
            String widthFinal = "";
            String colHeaderArrStr[] = null;
            String dataIndexArrStr[] = null;
            String widthArrStr[] = null;
            String alignArrStr[] = null;
            int strLength = 0;
            float totalWidth = 0;

            config = new com.krawler.utils.json.base.JSONObject(request.getParameter("config"));
            document = null;
            Rectangle rec = null;
            if (config.getBoolean("landscape")) {
                Rectangle recPage = new Rectangle(PageSize.A4.rotate());
                recPage.setBackgroundColor(new java.awt.Color(Integer.parseInt(config.getString("bgColor"), 16)));
                document = new Document(recPage, 15, 15, 30, 30);
                rec = document.getPageSize();
                totalWidth = rec.getWidth();
            } else {
                Rectangle recPage = new Rectangle(PageSize.A4);
                recPage.setBackgroundColor(new java.awt.Color(Integer.parseInt(config.getString("bgColor"), 16)));
                document = new Document(recPage, 15, 15, 30, 30);
                rec = document.getPageSize();
                totalWidth = rec.getWidth();
            }

            writer = PdfWriter.getInstance(document, baos);
            writer.setPageEvent(new EndPage());
            document.open();
            if (config.getBoolean("showLogo")) {
                addComponyLogo(document, request);
            }

            addTitleSubtitle(document);

            if (gridmap != null) {
                int givenTotalWidth = 0;
                for (int i = 0; i < gridmap.length(); i++) {
                    JSONObject temp = gridmap.getJSONObject(i);
                    givenTotalWidth += Integer.parseInt(temp.getString("width"));
                }
                double widthRatio = 1;
                if(givenTotalWidth > (totalWidth-40.00)){
                    widthRatio = (totalWidth-40.00)/givenTotalWidth; // 40.00 is left + right + table margin [15+15+10] margins of documents
                }
                for (int i = 0; i < gridmap.length(); i++) {
                    JSONObject temp = gridmap.getJSONObject(i);
                    colHeader += StringUtil.serverHTMLStripper(temp.getString("title"));
                    if (colHeader.indexOf("*") != -1) {
                        colHeader = colHeader.substring(0, colHeader.indexOf("*") - 1) + ",";
                    } else {
                        colHeader += ",";
                    }
                    fieldList += temp.getString("header") + ",";
                    if (!config.getBoolean("landscape")) {
                        int totalWidth1 = (int) ((totalWidth / gridmap.length()) - 5.00);
                        width += "" + totalWidth1 + ",";  //resize according to page view[potrait]
                    } else {
                        double adjustedWidth = (Integer.parseInt(temp.getString("width"))*widthRatio);
                        width += ((int) Math.floor(adjustedWidth)) + ",";
                    }
                    if (temp.getString("align").equals("")) {
                        align += "none" + ",";
                    } else {
                        align += temp.getString("align") + ",";
                    }
                }
                strLength = colHeader.length() - 1;
                colHeaderFinal = colHeader.substring(0, strLength);
                strLength = fieldList.length() - 1;
                fieldListFinal = fieldList.substring(0, strLength);
                strLength = width.length() - 1;
                widthFinal = width.substring(0, strLength);
                strLength = align.length() - 1;
                alignFinal = align.substring(0, strLength);
                colHeaderArrStr = colHeaderFinal.split(",");
                dataIndexArrStr = fieldListFinal.split(",");
                widthArrStr = widthFinal.split(",");
                alignArrStr = alignFinal.split(",");
            } else {
                fieldList = request.getParameter("header");
                colHeader = request.getParameter("title");
                width = request.getParameter("width");
                align = request.getParameter("align");
                colHeaderArrStr = colHeader.split(",");
                dataIndexArrStr = fieldList.split(",");
                widthArrStr = width.split(",");
                alignArrStr = align.split(",");
            }

            JSONArray store = obj.getJSONArray("data");

            if(grid!= null && grid.has("groupdata")) {
                JSONObject groupingConfig = grid.getJSONObject("groupdata");
                addGroupableTable(groupingConfig, 0, colHeaderArrStr.length, 0, store.length(), store, dataIndexArrStr, colHeaderArrStr, widthArrStr, alignArrStr, document, request);
            } else {
                addTable(0, colHeaderArrStr.length, 0, store.length(), store, dataIndexArrStr, colHeaderArrStr, widthArrStr, alignArrStr, document, request);
            }

        } catch (Exception e) {
            throw ServiceException.FAILURE("exportMPXDAOImpl.getPdfData", e);
        } finally {
            if (document != null) {
                document.close();
            }
            if (writer != null) {
                writer.close();
            }
        }
        return baos;
    }

    public void createCsvFile(HttpServletRequest request, HttpServletResponse response, JSONObject obj) throws ServiceException, SessionExpiredException {
        ByteArrayOutputStream os = null;
        DateFormat formatter = authHandlerDAOObj.getUserDateFormatter(sessionHandlerImpl.getDateFormatID(request), sessionHandlerImpl.getTimeZoneDifference(request), true);
        DateFormat frmt = authHandler.getDateFormatter(request);
        try {
            int report = Integer.parseInt(request.getParameter("get"));
            double totalCre = 0, totalDeb = 0;
            String headers[] = null;
            String titles[] = null;
            String align[] = null;
            String str = null;
            String nm = null;
            if (request.getParameter("header") != null) {
                String head = request.getParameter("header");
                String tit = request.getParameter("title");
                String algn = request.getParameter("align");
                headers = (String[]) head.split(",");
                titles = (String[]) tit.split(",");
                align = (String[]) algn.split(",");
            } else {
                headers = (String[]) obj.getString("header").split(",");
                titles = (String[]) obj.getString("title").split(",");
                align = (String[]) obj.getString("align").split(",");
            }
            String currencyid = sessionHandlerImpl.getCurrencyID(request);
            StringBuilder reportSB = new StringBuilder();
            JSONArray repArr = obj.getJSONArray("data");
            for (int h = 0; h < headers.length; h++) {
                String headerStr = StringUtil.serverHTMLStripper(titles[h]);
                if (h < headers.length - 1) {
                    if (align[h].equals("currency") && !headers[h].equals("")) {
                        String currency = currencyRender("", currencyid);
                        reportSB.append("\"" + headerStr + "(" + currency + ")" + "\",");
                    } else {
                        reportSB.append("\"" + headerStr + "\",");
                    }
                } else {
                    if (align[h].equals("currency") && !headers[h].equals("")) {
                        String currency = currencyRender("", currencyid);
                        reportSB.append("\"" + headerStr + "(" + currency + ")" + "\"\n");
                    } else {
                        reportSB.append("\"" + headerStr + "\"\n");
                    }
                }
            }
            for (int t = 0; t < repArr.length(); t++) {
                JSONObject temp = repArr.getJSONObject(t);
                if (report == 116) { //116:Trial Balance
                    totalCre = totalCre + Double.parseDouble(!StringUtil.isNullOrEmpty(temp.getString("c_amount")) ? temp.getString("c_amount") : "0");
                    totalDeb = totalDeb + Double.parseDouble(!StringUtil.isNullOrEmpty(temp.getString("d_amount")) ? temp.getString("d_amount") : "0");
                }
                String rowCurrencyId = temp.has("currencyid")?temp.getString("currencyid"):currencyid;
                for (int h = 0; h < headers.length; h++) {
                     if (h < headers.length - 1) {
                        if (align[h].equals("currency") && !temp.getString(headers[h]).equals("")) {
                            String currency = currencyRender(temp.getString(headers[h]), currencyid);
                            reportSB.append("\" " + currency + "\",");
                        } else  if (align[h].equals("rowcurrency") && !temp.getString(headers[h]).equals("")) {
                            String currency = currencyRender(temp.getString(headers[h]), rowCurrencyId);
                            reportSB.append("\" " + currency + "\",");
                        } else if (align[h].equals("date") && !temp.getString(headers[h]).equals("")) {
                            try {
                                String d1 = formatter.format(frmt.parse(temp.getString(headers[h])));
                                reportSB.append("\" " + d1 + "\",");
                            } catch (Exception ex) {
                                reportSB.append("\" " + temp.getString(headers[h]) + "\",");
                            }
                        } else if ((headers[h]).equals("taxrate") || (headers[h]).equals("permargin") && !temp.getString(headers[h]).equals("")){
                        	reportSB.append("\" " + htmlPercentageRender(temp.getString(headers[h]), true));
                    	} else {
                            if (headers[h].equals("invoiceno")) {
                                reportSB.append("\" " + temp.getString("no") + "\",");
                            } else if (headers[h].equals("invoicedate")) {
                                reportSB.append("\" " + formatter.format(frmt.parse(temp.getString("date"))) + "\",");
                            } else if (headers[h].equals("c_date")) {
                                reportSB.append("\" " + formatter.format(frmt.parse((StringUtil.isNullOrEmpty(temp.getString("c_date")) ? temp.getString("d_date") : temp.getString("c_date")))) + "\",");
                            } else if (headers[h].equals("c_accountname")) {
                                reportSB.append("\" " + (StringUtil.isNullOrEmpty(temp.getString("c_accountname")) ? temp.getString("d_accountname") : temp.getString("c_accountname")) + "\",");
                            } else if (headers[h].equals("c_entryno") && !(temp.isNull(headers[h])) && report != 117) {
                                reportSB.append("\" " + (StringUtil.isNullOrEmpty(temp.getString("c_entryno")) ? temp.getString("d_entryno") : temp.getString("c_entryno")) + "\",");
                            } else if (headers[h].equals("d_date")) {
                                reportSB.append("\" " + formatter.format(frmt.parse((StringUtil.isNullOrEmpty(temp.getString("d_date")) ? temp.getString("c_date") : temp.getString("c_date")))) + "\",");
                            } else if (headers[h].equals("d_accountname")) {
                                reportSB.append("\" " + (StringUtil.isNullOrEmpty(temp.getString("d_accountname")) ? temp.getString("c_accountname") : temp.getString("d_accountname")) + "\",");
                            } else if (headers[h].equals("d_entryno") && !(temp.isNull(headers[h]))) {
                                reportSB.append("\" " + (StringUtil.isNullOrEmpty(temp.getString("d_entryno")) ? temp.getString("c_entryno") : temp.getString("d_entryno")) + "\",");
                            } else if ((temp.isNull(headers[h])) && !(headers[h].equals("invoiceno")) && !(headers[h].equals("invoicedate"))) {
                                reportSB.append(",");
                            } else if (!(temp.isNull(headers[h])) && headers[h].equals("perioddepreciation")) {
                                double adj = temp.getDouble("perioddepreciation") - temp.getDouble("firstperiodamt");
                                String currency = currencyRender("" + adj, currencyid);
                                if (adj < 0.0001) {
                                    reportSB.append(",");
                                } else {
                                    reportSB.append("\" " + currency + "\",");
                                }
                            } else if (titles[h].equals("Opening Balance") || titles[h].equals("Asset Value")) {
                                String currency = currencyRender("" + Math.abs(temp.getDouble("openbalance")), currencyid);
                                reportSB.append("\" " + currency + "\",");
                            } else {
                                if (titles[h].equals("Opening Balance Type")) {
                                    double bal = Double.parseDouble(temp.getString(headers[h]));
                                    String str1 = bal == 0 ? "" : (bal < 0 ? "Credit" : "Debit");
                                    reportSB.append("\" " + str1 + "\",");
                                } else {
                                    reportSB.append("\" " + temp.getString(headers[h]) + "\",");
                                }
                            }
                        }
                    } else {
                        if (align[h].equals("currency") && !temp.getString(headers[h]).equals("")) {
                            String currency = currencyRender(temp.getString(headers[h]), currencyid);
                            reportSB.append("\" " + currency + "\"\n");
                        } else  if (align[h].equals("rowcurrency") && !temp.getString(headers[h]).equals("")) {
                            String currency = currencyRender(temp.getString(headers[h]), rowCurrencyId);
                            reportSB.append("\" " + currency + "\"\n");
                        } else if (align[h].equals("date") && !temp.getString(headers[h]).equals("")) {
                            String d1 = formatter.format(frmt.parse(temp.getString(headers[h])));
                            reportSB.append("\" " + d1 + "\"\n");
                        } else if ((headers[h]).equals("taxrate") || (headers[h]).equals("permargin") && !temp.getString(headers[h]).equals("")){
                        	reportSB.append("\" " + htmlPercentageRender(temp.getString(headers[h]), true));
                    	} else {
                            if (headers[h].equals("invoiceno")) {
                                reportSB.append("\" " + temp.getString("no") + "\"\n");
                            } else if (headers[h].equals("invoicedate")) {
                                reportSB.append("\" " + formatter.format(frmt.parse(temp.getString("date"))) + "\"\n");
                            } else if (headers[h].equals("c_date")) {
                                reportSB.append("\" " + formatter.format(frmt.parse((StringUtil.isNullOrEmpty(temp.getString("c_date")) ? temp.getString("d_date") : temp.getString("c_date")))) + "\"\n");
                            } else if (headers[h].equals("c_accountname")) {
                                reportSB.append("\" " + (StringUtil.isNullOrEmpty(temp.getString("c_accountname")) ? temp.getString("d_accountname") : temp.getString("c_accountname")) + "\"\n");
                            } else if (headers[h].equals("c_entryno") && !(temp.isNull(headers[h]))) {
                                reportSB.append("\" " + (StringUtil.isNullOrEmpty(temp.getString("c_entryno")) ? temp.getString("d_entryno") : temp.getString("c_entryno")) + "\"\n");
                            } else if (headers[h].equals("d_date")) {
                                reportSB.append("\" " + formatter.format(frmt.parse((StringUtil.isNullOrEmpty(temp.getString("d_date")) ? temp.getString("c_date") : temp.getString("c_date")))) + "\"\n");
                            } else if (headers[h].equals("d_accountname")) {
                                reportSB.append("\" " + (StringUtil.isNullOrEmpty(temp.getString("d_accountname")) ? temp.getString("c_accountname") : temp.getString("d_accountname")) + "\"\n");
                            } else if (headers[h].equals("d_entryno") && !(temp.isNull(headers[h]))) {
                                reportSB.append("\" " + (StringUtil.isNullOrEmpty(temp.getString("d_entryno")) ? temp.getString("c_entryno") : temp.getString("d_entryno")) + "\"\n");
                            } else if ((temp.isNull(headers[h])) && !(headers[h].equals("invoiceno")) && !(headers[h].equals("invoicedate"))) {
                                reportSB.append("\n");
                            } else if (!(temp.isNull(headers[h])) && headers[h].equals("perioddepreciation")) {
                                double adj = temp.getDouble("perioddepreciation") - temp.getDouble("firstperiodamt");
                                String currency = currencyRender("" + adj, currencyid);
                                if (adj < 0.0001) {
                                    reportSB.append(",");
                                } else {
                                    reportSB.append("\" " + currency + "\",");
                                }
                            } else if (titles[h].equals("Opening Balance") || titles[h].equals("Asset Value")) {
                                String currency = currencyRender("" + Math.abs(temp.getDouble("openbalance")), currencyid);
                                reportSB.append("\" " + currency + "\",");
                            } else {
                                if (titles[h].equals("Opening Balance Type")) {
                                    double bal = Double.parseDouble(temp.getString(headers[h]));
                                    String str1 = bal == 0 ? "" : (bal < 0 ? "Credit" : "Debit");
                                    reportSB.append("\" " + str1 + "\"\n");
                                } else {
                                    reportSB.append("\"" + temp.getString(headers[h]) + "\"\n");
                                }
                            }
                        }
                    }
                }
            }
            if (report == 116) { //116:Trial Balance
                String sep = ""; //Data separator for CSV
                reportSB.append("\"Total\",");
                for (int h = 1; h < headers.length; h++) {
                    sep = (h < headers.length - 1)?",":"\n";
                    if (headers[h].equals("c_amount")) {
                        reportSB.append("\""+currencyRender(String.valueOf(totalCre), currencyid)+"\""+sep);
                    } else if (headers[h].equals("d_amount")) {
                        reportSB.append("\""+currencyRender(String.valueOf(totalDeb), currencyid)+"\""+sep);
                    }
                }
            }
            String fname = request.getParameter("name")!=null?request.getParameter("name"):request.getParameter("filename");
            os = new ByteArrayOutputStream();
            os.write(reportSB.toString().getBytes());
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fname + ".csv\"");
            response.setContentType("application/octet-stream");
            response.setContentLength(os.size());
            response.getOutputStream().write(os.toByteArray());
            response.getOutputStream().flush();
            response.getOutputStream().close();
            if (os != null) {
                os.close();
            }
        } catch (ParseException ex) {
            Logger.getLogger(exportMPXDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (ServiceException ex) {
//            Logger.getLogger(exportMPXDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(exportMPXDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException e) {
            Logger.getLogger(exportMPXDAOImpl.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public String currencyRender(String currency, String currencyid) {
        KWLCurrency cur = (KWLCurrency) hibernateTemplate.load(KWLCurrency.class, currencyid);
        String fmt = "";
        try {
            String symbol = "";
            try {
                symbol = new Character((char) Integer.parseInt(cur.getHtmlcode(), 16)).toString();
            } catch (Exception e) {
                symbol = cur.getHtmlcode();
            }
//        char temp = (char) Integer.parseInt(symbol, 16);
//        symbol = Character.toString(temp);
//        if (cur.getHtmlcode().equals("20A8")) {
//            symbol = "Rs.";
//        }
//            float v = 0;
            DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");
            if (currency.equals("")) {
                return symbol;
            }
//            v = Float.parseFloat(currency);
//            fmt = decimalFormat.format(v);
            double amt = Double.parseDouble(currency);
            if(amt<0){
                amt = amt*-1;
                fmt = decimalFormat.format(amt);
                fmt = "("+symbol +" "+ fmt+")";
            }else{
                fmt = decimalFormat.format(amt);
                fmt = symbol +" "+ fmt;
            }
        }catch(Exception ex){
            fmt = currency;
        }
        return fmt;
    }
    public String htmlCurrencyRender(String currency, String currencyid) throws SessionExpiredException {
        KWLCurrency cur = (KWLCurrency) hibernateTemplate.load(KWLCurrency.class, currencyid);
        String fmt = "";
        try {
            String symbol = cur.getSymbol();
            DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");
            if (currency.equals("")) {
                return symbol;
            }
            double amt = Double.parseDouble(currency);
            if(amt<0){
                amt = amt*-1;
                fmt = decimalFormat.format(amt);
                fmt = "(<label style='color:red;'>"+symbol +" "+ fmt+"</label>)";
            }else{
                fmt = decimalFormat.format(amt);
                fmt = symbol +" "+ fmt;
            }
        }catch(Exception ex){
            fmt = currency;
        }
        return fmt;
    }

    public void setHeaderFooter(Document doc, String headerText) {
        HeaderFooter footer = new HeaderFooter(new Phrase("  ", FontFactory.getFont("Helvetica", 8, Font.NORMAL, Color.BLACK)), true);
        footer.setBorderWidth(0);
        footer.setBorderWidthTop(1);
        footer.setAlignment(HeaderFooter.ALIGN_RIGHT);
        doc.setFooter(footer);
        HeaderFooter header = new HeaderFooter(new Phrase(headerText, FontFactory.getFont("Helvetica", 14, Font.BOLD, Color.BLACK)), false);
        doc.setHeader(header);
    }
    
    public String htmlPercentageRender(String data, boolean isPDF) throws SessionExpiredException {
    	DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");
    	Double value = Double.parseDouble(data);
    	if(value < 0){
    		value = value*-1;
    		if(isPDF)
    			data = "("+decimalFormat.format(value)+")%";
    		else
    			data = "<td align=\"right\"> (<label style='color:red;'>"+decimalFormat.format(value)+"</label>)&#37;</td>";
    	}else{
    		if(isPDF)
    			data = decimalFormat.format(value)+"%";
    		else
    			data = "<td align=\"right\">"+data+"&#37;</td>";
    	}
    	return data;
    }
}
