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
package com.krawler.spring.accounting.product;

import com.krawler.common.admin.Company;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.StringUtil;
import com.krawler.esp.hibernate.impl.HibernateUtil;
import com.krawler.hql.accounting.Account;
import com.krawler.hql.accounting.Cyclecount;
import com.krawler.hql.accounting.Inventory;
import com.krawler.hql.accounting.PriceList;
import com.krawler.hql.accounting.Product;
import com.krawler.hql.accounting.ProductAssembly;
import com.krawler.hql.accounting.ProductBuild;
import com.krawler.hql.accounting.ProductBuildDetails;
import com.krawler.hql.accounting.ProductCyclecount;
import com.krawler.hql.accounting.Producttype;
import com.krawler.hql.accounting.StaticValues;
import com.krawler.hql.accounting.UnitOfMeasure;
import com.krawler.hql.accounting.Vendor;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.HibernateTemplate;
/**
 *
 * @author krawler
 */
public class accProductImpl implements accProductDAO {
    private HibernateTemplate hibernateTemplate;
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.hibernateTemplate = new HibernateTemplate(sessionFactory);
	}


    public KwlReturnObject getInventoryWOdetails(HashMap<String, Object> requestParams) throws ServiceException {
        String ss = (String) requestParams.get("ss");
        ArrayList params = new ArrayList();
        params.add((String) requestParams.get("productid"));
        params.add((String) requestParams.get("companyid"));
        String condition = " and inven.deleted=false and inven.company.companyID=?";
        if (StringUtil.isNullOrEmpty(ss) == false) {
            params.add(ss + "%");
            condition += " and inven.product.name like ?";
        }
        String query = "select inven from " +
                "Inventory inven " +
                "where " +
                "    inven not in (select inventory from InvoiceDetail invd) " +
                "and inven not in (select inventory from CreditNoteDetail cnd)" +
                "and inven not in (select inventory from GoodsReceiptDetail cnd)" +
                "and inven not in (select inventory from DebitNoteDetail cnd)" +
                " and inven.product.ID = ? " + condition +
                " group by inven.ID";
        List list = HibernateUtil.executeQuery(hibernateTemplate, query, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    public KwlReturnObject getInventoryWithDetails(HashMap<String, Object> requestParams) throws ServiceException {
        String start = (String) requestParams.get("start");
        String limit = (String) requestParams.get("limit");
        String ss = (String) requestParams.get("ss");
        ArrayList params = new ArrayList();
        params.add((String) requestParams.get("productid"));
        params.add((String) requestParams.get("companyid"));
        String condition = " and inven.deleted=false and inven.company.companyID=?";
        if (StringUtil.isNullOrEmpty(ss) == false) {
            params.add(ss + "%");
            condition += " and inven.product.name like ?";
        }
        String query = "select inven, je from " +
                "Inventory inven, JournalEntry je " +
                "where (" +
                "    inven.ID in (select ID from InvoiceDetail where invoice.journalEntry=je)" +
                " or inven.ID in (select inventory.ID from CreditNoteDetail where creditNote.journalEntry=je)" +
                " or inven.ID in (select ID from GoodsReceiptDetail where goodsReceipt.journalEntry=je)" +
                " or inven.ID in (select inventory.ID from DebitNoteDetail where debitNote.journalEntry=je)" +
                ") and je.deleted=false and inven.product.ID = ? " + condition +
                " order by je.entryNumber ASC";
        List list = HibernateUtil.executeQuery(hibernateTemplate, query, params.toArray());
        int count = list.size();
//        if (StringUtil.isNullOrEmpty(start) == false && StringUtil.isNullOrEmpty(limit) == false) {
//            list = HibernateUtil.executeQueryPaging(hibernateTemplate, query, params.toArray(), new Integer[]{Integer.parseInt(start), Integer.parseInt(limit)});
//        }
        return new KwlReturnObject(true, "", null, list, count);
    }

    public KwlReturnObject getProducts(HashMap<String, Object> requestParams) throws ServiceException {
        List returnList = new ArrayList();
        String condition="";
        ArrayList params=new ArrayList();

        String productid = (String) requestParams.get("productid");
        String companyid = (String) requestParams.get("companyid");
        String ids[] =  (String[]) requestParams.get("ids");

        String date = (String) requestParams.get("transactiondate");
        String producttype = (String) requestParams.get("type");
        DateFormat df = (DateFormat) requestParams.get("df");

        Date transactionDate = null;
        try {
            transactionDate = (date == null ? null : df.parse(date));
        } catch (ParseException ex) {
            Logger.getLogger(accProductImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("getProducts : "+ex.getMessage(), ex);
        }
        params.add(companyid);
        if(ids!=null){
            condition+=" and p.ID in(? ";
            params.add(null);
            for (int i = 0; i < ids.length; i++) {
                if (!StringUtil.isNullOrEmpty(ids[i])) {
                    condition+=" ,?";
                    params.add(ids[i]);
                }
            }
             condition+=" )";
        }               
        if(!StringUtil.isNullOrEmpty(producttype)){
            condition+=" and p.producttype.ID=? ";
            params.add(producttype);
        }
        String query="select p from Product p where p.parent is null and p.deleted=false and p.company.companyID=? "+condition+" order by p.producttype, p.name ";
        List list = HibernateUtil.executeQuery(hibernateTemplate, query, params.toArray());
        Iterator itr = list.iterator();
        int level=0;
        while(itr.hasNext()) {
            Product product = (Product) itr.next();
            if(product.getID().equals(productid)) continue;
            Object tmplist[] = new Object[13]; //0:Product, 1:level, 2:leaf
            tmplist[0]=product;
            tmplist[1]=level;
            returnList.add(tmplist);
            tmplist[2]=getChildProducts(product, returnList, level, productid, transactionDate);

            tmplist = getProductsDetails(tmplist, product, transactionDate);
        }
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }

      public KwlReturnObject getProductByID(String productid,String companyid) throws ServiceException {
        ArrayList params=new ArrayList();
        params.add(productid);
        params.add(companyid);
        List returnList = new ArrayList();
        String query="from Product p where p.ID=? and p.company.companyID=? ";
        returnList = HibernateUtil.executeQuery(hibernateTemplate, query, params.toArray());
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }

    private Object getChildProducts(Product product, List returnList, int level, String productid, Date transactionDate) throws ServiceException {
        boolean leaf=true;
        Iterator<Product> itr = new TreeSet(product.getChildren()).iterator();
        level++;
        while(itr.hasNext()) {
            Product child = itr.next();
            if(child.getID().equals(productid)||child.isDeleted()) continue;
            leaf=false;
            Object tmplist[] = new Object[13]; //0:Product, 1:level, 2:leaf
            tmplist[0]=child;
            tmplist[1]=level;
            returnList.add(tmplist);
            tmplist[2]=getChildProducts(child, returnList, level, productid, transactionDate);
            tmplist = getProductsDetails(tmplist, child, transactionDate);
        }
        return leaf;
    }

    private Object[] getProductsDetails(Object[] detailsArray, Product product, Date transactionDate) throws ServiceException {
            KwlReturnObject result = getProductPrice(product.getID(), true, null);
            detailsArray[3] = result.getEntityList().get(0);
//            obj.put("saleprice",row[2]);
            result = getProductPrice(product.getID(), false, null);
            detailsArray[4] = result.getEntityList().get(0);
//            obj.put("quantity",(row[3]==null?0:row[3]));
            result = getQuantity(product.getID());
            detailsArray[5] = (result.getEntityList().get(0));
//            obj.put("initialquantity",(row[4]==null?0:row[4]));
            result = getInitialQuantity(product.getID());
            detailsArray[6] = (result.getEntityList().get(0));
//            obj.put("initialprice",(row[5]==null?0:row[5]));
            result = getInitialPrice(product.getID(), true);
            detailsArray[7] = result.getEntityList().get(0);

            //Cycle count Object
            HashMap<String, Object> ccfilterParams = new HashMap<String, Object>();
            ccfilterParams.put("productid", product.getID());
            detailsArray[8] = getCycleCountEntryObject(ccfilterParams);

            //salespricedatewise
            result = getProductPrice(product.getID(), false, transactionDate);
            detailsArray[9] = result.getEntityList().get(0);

            //purchasepricedatewise
            result = getProductPrice(product.getID(), true, transactionDate);
            detailsArray[10] = result.getEntityList().get(0);

            //initialsalesprice
            result = getInitialPrice(product.getID(), false);
            detailsArray[11] = result.getEntityList().get(0);
 
            result = getProCreationDate(product.getID(), product.getCompany().getCompanyID());
            detailsArray[12] = result.getEntityList().get(0);

            return detailsArray;
    }

    public KwlReturnObject getSuggestedReorderProducts(HashMap<String, Object> requestParams) throws ServiceException {
        List returnList = new ArrayList();
        String condition="";
        ArrayList params=new ArrayList();

        String productid = (String) requestParams.get("productid");
        String companyid = (String) requestParams.get("companyid");
        String start = (String) requestParams.get("start");
        String limit = (String) requestParams.get("limit");
        String date = (String) requestParams.get("transactiondate");
        String producttype = (String) requestParams.get("type");
        DateFormat df = (DateFormat) requestParams.get("df");

        Date transactionDate = null;
        try {
            transactionDate = (date == null ? null : df.parse(date));
        } catch (ParseException ex) {
            Logger.getLogger(accProductImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("getSuggestedReorderProducts : "+ex.getMessage(), ex);
        }
        params.add(Producttype.SERVICE);
        params.add(companyid);
        if(!StringUtil.isNullOrEmpty(producttype)){
            condition=" and p.producttype.ID=? ";
            params.add(producttype);
        }
        String query="select p, (select sum((case when carryIn=true then quantity else -quantity end)) from Inventory inv3  where product.ID=p.ID and inv3.deleted=false group by product.ID) from Product p" +
                " where p.deleted=false and p.producttype.ID <> ?  and p.company.companyID=? "+condition+
                " order by p.producttype, p.name ";
        List<Object[]> list = HibernateUtil.executeQuery(hibernateTemplate, query, params.toArray());
        int count = list.size();
        if(StringUtil.isNullOrEmpty(start)==false&&StringUtil.isNullOrEmpty(limit)==false){
            list=HibernateUtil.executeQueryPaging(hibernateTemplate, query,params.toArray(), new Integer[]{Integer.parseInt(start),Integer.parseInt(limit)});
        }
        int level=0;
        Product product;
        int quantity;
        if (list != null && !list.isEmpty()) {
            for(Object[] result : list){
                product = (Product) result[0];
                quantity = result[1]==null?0:Integer.valueOf(result[1].toString());
                if(quantity>product.getReorderLevel()){continue;}
                if(product.getID().equals(productid)) continue;
                Object tmplist[] = new Object[13]; //0:Product, 1:level, 2:leaf
                tmplist[0]=product;
                tmplist[1]=level;
                returnList.add(tmplist);
                tmplist[2]= true;//getChildProducts(product, returnList, level, productid, transactionDate);
                tmplist = getProductsDetails(tmplist, product, transactionDate);
            }
        }
        return new KwlReturnObject(true, "", null, returnList, count);
    }

    public KwlReturnObject getProductfromAccount(String accountid, String companyid) throws ServiceException {
        List list = new ArrayList();
        String q = "from Product pr where (purchaseAccount.ID=? or salesAccount.ID=?) and pr.company.companyID=?";
        list = HibernateUtil.executeQuery(hibernateTemplate, q, new Object[]{accountid, accountid, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject addProduct(HashMap<String, Object> productMap) throws ServiceException {
        List list = new ArrayList();
        try {
            Product product = new Product();
            product.setDeleted(false);
            product = buildProduct(product, productMap);
            hibernateTemplate.save(product);
            list.add(product);
        } catch (Exception e) {
            throw ServiceException.FAILURE("addProduct : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "Product has been added successfully", null, list, list.size());
    }

    public KwlReturnObject updateProduct(HashMap<String, Object> productMap) throws ServiceException {
        List list = new ArrayList();
        try {
            String productid = (String) productMap.get("id");
            Product product = (Product) hibernateTemplate.get(Product.class, productid);
            if(product != null) {
                product = buildProduct(product, productMap);
                hibernateTemplate.save(product);
            }
            list.add(product);
        } catch (Exception e) {
            throw ServiceException.FAILURE("updateProduct : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "Product has been updated successfully", null, list, list.size());
    }

    public Product buildProduct(Product product, HashMap<String, Object> productMap) throws ServiceException {
        if (productMap.containsKey("producttype")) {
            Producttype ptype = productMap.get("producttype")==null?null:(Producttype) hibernateTemplate.get(Producttype.class, (String) productMap.get("producttype"));
            product.setProducttype(ptype);
        }
        if (productMap.containsKey("parentid")) {
            Product pproduct = productMap.get("parentid")==null?null:(Product) hibernateTemplate.get(Product.class, (String) productMap.get("parentid"));
            product.setParent(pproduct);
        }
        if (productMap.containsKey("name")) {
            product.setName((String) productMap.get("name"));
        }
        if (productMap.containsKey("productid")) {
            product.setProductid((String) productMap.get("productid"));
        }
        if (productMap.containsKey("desc")) {
            product.setDescription((String) productMap.get("desc"));
        }
        if (productMap.containsKey("syncable")) {
            product.setSyncable((Boolean)productMap.get("syncable"));
        }
        if (productMap.containsKey("uomid")) {
            UnitOfMeasure uom = productMap.get("uomid")==null?null:(UnitOfMeasure)hibernateTemplate.get(UnitOfMeasure.class, (String) productMap.get("uomid"));
            product.setUnitOfMeasure(uom);
        }
        if (productMap.containsKey("purchaseaccountid")) {
            Account paccount = productMap.get("purchaseaccountid")==null?null:(Account)hibernateTemplate.get(Account.class, (String) productMap.get("purchaseaccountid"));
            product.setPurchaseAccount(paccount);
        }
        if (productMap.containsKey("salesaccountid")) {
            Account saccount = productMap.get("salesaccountid")==null?null:(Account)hibernateTemplate.get(Account.class, (String) productMap.get("salesaccountid"));
            product.setSalesAccount(saccount);
        }
        if (productMap.containsKey("purchaseretaccountid")) {
            Account praccount = productMap.get("purchaseretaccountid")==null?null:(Account)hibernateTemplate.get(Account.class, (String) productMap.get("purchaseretaccountid"));
            product.setPurchaseReturnAccount(praccount);
        }
        if (productMap.containsKey("salesretaccountid")) {
            Account sraccount = productMap.get("salesretaccountid")==null?null:(Account)hibernateTemplate.get(Account.class, (String) productMap.get("salesretaccountid"));
            product.setSalesReturnAccount(sraccount);
        }
        if (productMap.containsKey("leadtime")) {
            product.setLeadTimeInDays((Integer) productMap.get("leadtime"));
        }
        if (productMap.containsKey("reorderlevel")) {
            product.setReorderLevel((Integer) productMap.get("reorderlevel"));
        }
        if (productMap.containsKey("reorderquantity")) {
            product.setReorderQuantity((Integer) productMap.get("reorderquantity"));
        }
        if (productMap.containsKey("deletedflag")) {
            product.setDeleted((Boolean) productMap.get("deletedflag"));
        }
        if (productMap.containsKey("companyid")) {
            Company company = productMap.get("companyid")==null?null:(Company)hibernateTemplate.get(Company.class, (String) productMap.get("companyid"));
            product.setCompany(company);
        }
        if (productMap.containsKey("vendorid")) {
            Vendor vendor = productMap.get("vendorid")==null?null:(Vendor)hibernateTemplate.get(Vendor.class, (String) productMap.get("vendorid"));
            product.setVendor(vendor);
        }
        return product;
    }

    public KwlReturnObject deleteProduct(String productid) throws ServiceException {
        List list = new ArrayList();
        Product product = (Product) hibernateTemplate.get(Product.class, productid);
        product.setDeleted(true);
        hibernateTemplate.save(product);
        list.add(product);
        return new KwlReturnObject(true, "Product has been deleted successfully", null, list, 1);
    }

    public KwlReturnObject deleteProductPermanently(String productid,String companyid) throws ServiceException {
        String delQuery = "delete from Product p where p.ID=? and p.company.companyID=?";
        int numRows = HibernateUtil.executeUpdate(hibernateTemplate, delQuery, new Object[]{productid,companyid});
        return new KwlReturnObject(true, "Product Type has been deleted successfully.", null, null, numRows);

    }

    public KwlReturnObject deleteProductCycleCountPermanently(String productid,String companyid) throws ServiceException {
        String delQuery = "delete from ProductCyclecount p where p.product.ID=? ";
        int numRows = HibernateUtil.executeUpdate(hibernateTemplate, delQuery, new Object[]{productid});
        return new KwlReturnObject(true, "Product Type has been deleted successfully.", null, null, numRows);

    }

    public KwlReturnObject deleteCyclecountPermanently(String productid,String companyid) throws ServiceException {
        String delQuery = "delete from Cyclecount c where c.product.ID=? ";
        int numRows = HibernateUtil.executeUpdate(hibernateTemplate, delQuery, new Object[]{productid});
        return new KwlReturnObject(true, "Product Type has been deleted successfully.", null, null, numRows);

    }

    public KwlReturnObject deleteProPricePermanently(String productid,String companyid) throws ServiceException {
        String delQuery = "delete from PriceList p where p.product.ID=? and p.company.companyID=?";
        int numRows = HibernateUtil.executeUpdate(hibernateTemplate, delQuery, new Object[]{productid,companyid});
        return new KwlReturnObject(true, "Product Type has been deleted successfully.", null, null, numRows);
    }

    public KwlReturnObject saveProductTypes(HashMap<String, Object> dataMap) throws ServiceException {
        List list = new ArrayList();
        try {
            Producttype ptype = new Producttype();
            if (dataMap.containsKey("id")) {
                ptype = dataMap.get("id")==null?null:(Producttype) hibernateTemplate.get(Producttype.class, (String) dataMap.get("id"));
            }
            if (dataMap.containsKey("name")) {
                ptype.setName((String) dataMap.get("name"));
            }
            hibernateTemplate.save(ptype);
            list.add(ptype);
        } catch (Exception e) {
            throw ServiceException.FAILURE("saveProductTypes : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "Product Type has been added successfully", null, list, list.size());
    }

    public KwlReturnObject getProductTypes(HashMap<String, Object> requestParams) throws ServiceException {
        List returnList = new ArrayList();
        String query="from Producttype";
        returnList = HibernateUtil.executeQuery(hibernateTemplate, query);
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }

    public KwlReturnObject deleteProductType(String typeid) throws ServiceException {
        String delQuery = "delete from Producttype u where u.ID=?";
        int numRows = HibernateUtil.executeUpdate(hibernateTemplate, delQuery, new Object[]{typeid});
        return new KwlReturnObject(true, "Product Type has been deleted successfully.", null, null, numRows);
    }

    public KwlReturnObject getProductsByType(HashMap<String, Object> requestParams) throws ServiceException {
        List returnList = new ArrayList();
        ArrayList params = new ArrayList();
        params.add(Producttype.SERVICE);
        params.add((String) requestParams.get("companyid"));
        String type = (String) requestParams.get("type");
        String condition = "";
        if (!StringUtil.isNullOrEmpty(type)) {
            condition = " and p.producttype.ID=? ";
            params.add(type);
        }
        String query = "select p," +
                "(select sum((case when carryIn=true then quantity else -quantity end)) from Inventory inv where product.ID=p.ID and inv.deleted=false  and product.producttype.ID <> ? group by product.ID) " +
                "from Product p where p.parent is null and p.deleted=false and p.company.companyID=? " + condition + " order by p.name ";
        returnList = HibernateUtil.executeQuery(hibernateTemplate, query, params.toArray());
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }


    //Product Assembly
    public KwlReturnObject deleteProductAssembly(String parentProductId) throws ServiceException {
        String delQuery = "delete from ProductAssembly where product.ID = ?";
        int numRows = HibernateUtil.executeUpdate(hibernateTemplate, delQuery, new Object[]{parentProductId});

        return new KwlReturnObject(true, "", null, null, numRows);
    }
    
    public KwlReturnObject deleteSubProductFromAssembly(String subProductId) throws ServiceException {
        String delQuery = "delete from ProductAssembly where subproducts.ID = ?";
        int numRows = HibernateUtil.executeUpdate(hibernateTemplate, delQuery, new Object[]{subProductId});
        return new KwlReturnObject(true, "", null, null, numRows);
    }

    public KwlReturnObject saveProductAssembly(HashMap<String, Object> assemblyMap) throws ServiceException {
        List list = new ArrayList();
        try {
            ProductAssembly assembly = new ProductAssembly();
            if (assemblyMap.containsKey("id")) {
                assembly = assemblyMap.get("id")==null?null:(ProductAssembly) hibernateTemplate.get(ProductAssembly.class, (String) assemblyMap.get("id"));
            }
            if (assemblyMap.containsKey("productid")) {
                Product Pproduct = assemblyMap.get("productid")==null?null:(Product) hibernateTemplate.get(Product.class, (String) assemblyMap.get("productid"));
                assembly.setProduct(Pproduct);
            }
            if (assemblyMap.containsKey("subproductid")) {
                Product Sproduct = assemblyMap.get("subproductid")==null?null:(Product) hibernateTemplate.get(Product.class, (String) assemblyMap.get("subproductid"));
                assembly.setSubproducts(Sproduct);
            }
            if (assemblyMap.containsKey("quantity")) {
                assembly.setQuantity((Integer) assemblyMap.get("quantity"));
            }
            hibernateTemplate.save(assembly);
            list.add(assembly);
        } catch (Exception e) {
            throw ServiceException.FAILURE("saveProductAssembly : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "Product Assembly has been added successfully", null, list, list.size());
    }

    public void updateAssemblyInventory(HashMap<String, Object> requestParams) throws ServiceException {
		try {
            int buildquantity = (Integer) requestParams.get("quantity");
            String refno = (String) requestParams.get("refno");
            String buildproductid = (String) requestParams.get("buildproductid");
            String applydate = (String) requestParams.get("applydate");
            String memo = (String) requestParams.get("memo");
            String companyid = (String) requestParams.get("companyid");
            DateFormat df = (DateFormat) requestParams.get("df");
            String jsondata = (String) requestParams.get("assembly");

            JSONArray jarr = new JSONArray("[" + jsondata + "]");
            if(jarr.length()>0){ //Bug Fixed #16851
                Date appDate=null;
                try{
                    appDate=df.parse(applydate);
                }catch (Exception e){
                    appDate=new Date();
                }
                Product buildproduct = (Product) hibernateTemplate.get(Product.class, buildproductid);
                ProductBuild build = new ProductBuild();
                build.setQuantity(buildquantity);
                build.setRefno(refno);
                build.setMemo(memo);
                build.setEntryDate(appDate);
                build.setProduct(buildproduct);
                build.setCompany((Company)hibernateTemplate.get(Company.class,companyid));
                hibernateTemplate.saveOrUpdate(build);

                int aquantity=0, deductqty=0;
                double rate=0;
                for (int i = 0; i < jarr.length(); i++) {
                    JSONObject jobj = jarr.getJSONObject(i);
                    ProductBuildDetails pbd = new ProductBuildDetails();
                    pbd.setBuild(build);
                    rate = Double.parseDouble(jobj.get("rate").toString());
                    pbd.setRate(rate);
                    Product aproduct = (Product)hibernateTemplate.get(Product.class,jobj.get("product").toString());
                    pbd.setAproduct(aproduct);
                    aquantity = Integer.parseInt(jobj.get("quantity").toString());
                    deductqty = aquantity * buildquantity;
                    pbd.setAquantity(aquantity);
                    hibernateTemplate.save(pbd);
//                    makeInventory(session, request, aproduct, deductqty, "Build Product Assembly for "+buildproduct.getName(), false, false,null);
                    JSONObject inventoryjson = new JSONObject();
                    inventoryjson.put("productid", aproduct.getID());
                    inventoryjson.put("quantity", deductqty);
                    inventoryjson.put("description", "Build Product Assembly for "+buildproduct.getName());
                    inventoryjson.put("carryin", false);
                    inventoryjson.put("defective", false);
                    inventoryjson.put("newinventory", false);
                    inventoryjson.put("companyid", companyid);
                    KwlReturnObject invresult = addInventory(inventoryjson);
                    Inventory inventory = (Inventory) invresult.getEntityList().get(0);
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("updateAssemblyInventory : "+ex.getMessage(), ex);
        }
    }

    public KwlReturnObject getAssemblyItems(HashMap<String, Object> requestParams) throws ServiceException {
        String productid = (String) requestParams.get("productid");
        List returnList = new ArrayList();
        String query="select pa," +
                        " (select pl1.price from PriceList pl1 where product.ID=pa.subproducts.ID and carryIn=true and applyDate in (select max(applyDate) as ld from PriceList where product.ID=pl1.product.ID and carryIn=pl1.carryIn group by product))," +
                        " (select pl2.price from PriceList pl2 where product.ID=pa.subproducts.ID and carryIn=false and applyDate in (select max(applyDate) as ld from PriceList where product.ID=pl2.product.ID and carryIn=pl2.carryIn group by product))," +
                        " (select sum((case when carryIn=true then quantity else -quantity end)) from Inventory inv  where product.ID=pa.subproducts.ID and inv.deleted=false group by product.ID) " +
                        " from ProductAssembly pa where pa.product.ID=?";
        returnList = HibernateUtil.executeQuery(hibernateTemplate, query, productid);
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }


//PRICE LIST
    public KwlReturnObject addPriceList(HashMap<String, Object> priceMap) throws ServiceException {
        List list = new ArrayList();
        try {
            PriceList price = new PriceList();
            if (priceMap.containsKey("productid")) {
                Product product = priceMap.get("productid")==null?null:(Product) hibernateTemplate.get(Product.class, (String) priceMap.get("productid"));
                price.setProduct(product);
            }
            if (priceMap.containsKey("carryin")) {
                price.setCarryIn((Boolean) priceMap.get("carryin"));
            }
            if (priceMap.containsKey("applydate")) {
                price.setApplyDate((Date) priceMap.get("applydate"));
            }
            if (priceMap.containsKey("price")) {
                price.setPrice((Double) priceMap.get("price"));
            }
            if (priceMap.containsKey("companyid")) {
                Company company = priceMap.get("companyid")==null?null:(Company)hibernateTemplate.get(Company.class, (String) priceMap.get("companyid"));
                price.setCompany(company);
            }
            hibernateTemplate.save(price);
            list.add(price);
        } catch (Exception e) {
            throw ServiceException.FAILURE("addPriceList : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "Price has been added successfully", null, list, list.size());
    }

    public KwlReturnObject updatePriceList(HashMap<String, Object> priceMap) throws ServiceException {
        List list = new ArrayList();
        try {
            String priceid = (String) priceMap.get("priceid");
            PriceList price = (PriceList) hibernateTemplate.get(PriceList.class, priceid);
            if(price!=null) {
                if (priceMap.containsKey("productid")) {
                    Product product = priceMap.get("productid")==null?null:(Product) hibernateTemplate.get(Product.class, (String) priceMap.get("productid"));
                    price.setProduct(product);
                }
                if (priceMap.containsKey("carryin")) {
                    price.setCarryIn((Boolean) priceMap.get("carryin"));
                }
                if (priceMap.containsKey("applydate")) {
                    price.setApplyDate((Date) priceMap.get("applydate"));
                }
                if (priceMap.containsKey("price")) {
                    price.setPrice((Double) priceMap.get("price"));
                }
                if (priceMap.containsKey("companyid")) {
                    Company company = priceMap.get("companyid")==null?null:(Company)hibernateTemplate.get(Company.class, (String) priceMap.get("companyid"));
                    price.setCompany(company);
                }
                hibernateTemplate.save(price);
                list.add(price);
            }
        } catch (Exception e) {
            throw ServiceException.FAILURE("updatePriceList : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "Price has been updated successfully", null, list, list.size());
    }

    public KwlReturnObject getPrice(HashMap<String, Object> request) throws ServiceException {
        List list = new ArrayList();
        int count = 0;
        try {
            String query = "from PriceList";
            String productid = (String) request.get("productid");
            String companyid = (String) request.get("companyid");
            String start = (String) request.get("start");
            String limit = (String) request.get("limit");
            String ss = (String) request.get("ss");
            String condition = " where company.companyID=? and product.ID=? ";
            ArrayList params = new ArrayList();
            params.add(companyid);
            params.add(productid);
            if (StringUtil.isNullOrEmpty(ss) == false) {
                params.add(ss + "%");
                if (condition.length() > 0) {
                    condition += " and";
                } else {
                    condition += " where";
                }
                condition += " product.name like ?";
            }
            condition += " order by applyDate desc";
            query += condition;
            list = HibernateUtil.executeQuery(hibernateTemplate, query, params.toArray());
            count = list.size();
            if (StringUtil.isNullOrEmpty(start) == false && StringUtil.isNullOrEmpty(limit) == false) {
                list = HibernateUtil.executeQueryPaging(hibernateTemplate, query, params.toArray(), new Integer[]{Integer.parseInt(start), Integer.parseInt(limit)});
            }
        } catch (Exception e) {
            throw ServiceException.FAILURE("getPrice : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "", null, list, count);
    }

    public KwlReturnObject getProductPrice(String productid, boolean isPurchase, Date transactiondate) throws ServiceException {
        List returnList = new ArrayList();
        try {
            ArrayList params=new ArrayList();
            params.add(productid);
            params.add(isPurchase);
            String condition = "";
            if (transactiondate != null) {
                 params.add(transactiondate);
                 condition += " and applyDate<=? ";
            }
            String query = "select pl1.price from PriceList pl1 where product.ID=? and carryIn=? and applyDate in (select max(applyDate) as ld from PriceList where product.ID=pl1.product.ID and carryIn=pl1.carryIn "+condition+" group by product)";

            List list = HibernateUtil.executeQuery(hibernateTemplate, query, params.toArray());
            Iterator itr = list.iterator();
            if(itr.hasNext()) {
                returnList.add((Double) itr.next());
            } else {
                returnList.add(null);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getProductPrice : "+ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }
    
    public KwlReturnObject getInitialPrice(String productid, boolean isPurchase) throws ServiceException {
        List returnList = new ArrayList();
        try {
            ArrayList params=new ArrayList();
            params.add(productid);
            params.add(isPurchase);
            String query="select pl1.price from PriceList pl1 where product.ID=? and carryIn=? and" +
                    " applyDate in (select min(applyDate) as ld from PriceList where product.ID=pl1.product.ID and carryIn=pl1.carryIn group by product)";
            List list = HibernateUtil.executeQuery(hibernateTemplate, query, params.toArray());
            Iterator itr = list.iterator();
            if(itr.hasNext()) {
                returnList.add((Double) itr.next());
            } else {
                returnList.add(null);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getInitialPrice : "+ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }

    public KwlReturnObject getProCreationDate(String productid, String companyid) throws ServiceException {
        List list = new ArrayList();
        try {
            ArrayList params=new ArrayList();
            params.add(companyid);
            params.add(productid);
             String query="select min(updateDate) from Inventory where deleted=false and company.companyID=? and product.ID=? ";
            list = HibernateUtil.executeQuery(hibernateTemplate, query, params.toArray());          
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getInitialPrice : "+ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject getPriceListEntry(HashMap<String, Object> filterParams) throws ServiceException {
		List returnList = new ArrayList();
        ArrayList params=new ArrayList();
        String condition = "";
        String query="from PriceList ";

        if(filterParams.containsKey("applydate")){
            condition += (condition.length()==0?" where ":" and ") + "applyDate=?";
            params.add(filterParams.get("applydate"));
        }
        if(filterParams.containsKey("productid")){
            condition += (condition.length()==0?" where ":" and ") + "product.ID=?";
            params.add(filterParams.get("productid"));
        }
        if(filterParams.containsKey("carryin")){
            condition += (condition.length()==0?" where ":" and ") + "carryIn=?";
            params.add(filterParams.get("carryin"));
        }
        if(filterParams.containsKey("companyid")){
            condition += (condition.length()==0?" where ":" and ") + "company.companyID=?";
            params.add(filterParams.get("companyid"));
        }
        query += condition;
//        query="from PriceList where applyDate=? and product.ID=? and carryIn=? and company.companyID=?";
        returnList = HibernateUtil.executeQuery(hibernateTemplate, query, params.toArray());
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }

//Cycle Count
    public KwlReturnObject getCycleCountForApproval(HashMap<String, Object> requestParams) throws ServiceException {
        List returnList = new ArrayList();
        try {
            ArrayList params = new ArrayList();
            String query = "";
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
            Date countDate = sdf.parse((String) requestParams.get("countdate"));
            query = "from Cyclecount cc where cc.status != 2 and cc.countDate = ? and cc.product.company.companyID=?";
            params.add(countDate);
            params.add((String) requestParams.get("companyid"));
            returnList = HibernateUtil.executeQuery(hibernateTemplate, query, params.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getCycleCountForApproval : "+ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }

    public KwlReturnObject getCycleCountWorkSheet(HashMap<String, Object> requestParams, boolean isExpotrt) throws ServiceException {
        List returnList = new ArrayList();
        try {
            ArrayList params = new ArrayList();
            String query = "";
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
            Date fromDate = null;
            Date toDate = null;
            String companyID = (String) requestParams.get("companyid");
            if (isExpotrt) {
                fromDate = sdf.parse((String) requestParams.get("stdate"));
                toDate = sdf.parse((String) requestParams.get("enddate"));
//                includeCycleCount = request.getParameter("config");
            } else {
                fromDate = sdf.parse((String) requestParams.get("ctdatefr"));
                toDate = sdf.parse((String) requestParams.get("ctdateto"));
//                includeCycleCount = request.getParameter("includerecount");
            }

            String filter = "";
            if (Integer.parseInt((String) requestParams.get("includerecount")) == 0) {
                query = "select cp, " +
                        " (select sum((case when carryIn=true then quantity else -quantity end)) from Inventory where deleted=false and product.ID=cp.product.ID  and product.producttype.ID <> ? group by product.ID) " +
                        " from ProductCyclecount cp where ((cp.nextDate >= ? and cp.nextDate <= ?) or cp.nextDate = ?) and " +
                        " cp.product.deleted=false and cp.product.company.companyID=? order by cp.product.name ";
                params.add( Producttype.SERVICE);
                params.add(fromDate);
                params.add(toDate);
                params.add(new Date(0, 0, 1));
                params.add(companyID);
                returnList = HibernateUtil.executeQuery(hibernateTemplate, query, params.toArray());
            } else {
                filter = "(cc.countDate >= ? and cc.countDate <= ?) and ";
                query = "select cc," +
                        " (select cp from ProductCyclecount cp where cp.product=cc.product )," +
                        " (select sum((case when carryIn=true then quantity else -quantity end)) from Inventory where deleted=false and product.ID=cc.product.ID  and product.producttype.ID <> ? group by product.ID) " +
                        " from Cyclecount cc where " + filter +
                        " cc.status = 1 and cc.product.deleted=false and cc.product.company.companyID=? order by cc.product.name ";
                params.add( Producttype.SERVICE);
                params.add(fromDate);
                params.add(toDate);
                params.add(companyID);
                returnList = HibernateUtil.executeQuery(hibernateTemplate, query, params.toArray());
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getCycleCountWorkSheet : "+ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }

    public KwlReturnObject getCycleCountProduct(HashMap<String, Object> filterParams) throws ServiceException {
        List returnList = new ArrayList();
        ArrayList params = new ArrayList();
        params.add(Producttype.SERVICE);
        String query = "";
        if (Integer.parseInt((String) filterParams.get("type")) == 0) {//type = 0 include initialcount ; type =1 include recount items
            params.add(new Date(new Date().getYear(), new Date().getMonth(), new Date().getDate()));
            params.add(new Date(0, 0, 1));
            query = "select cp.product, " +
                    " (select sum((case when carryIn=true then quantity else -quantity end)) from Inventory where deleted=false and product.ID=cp.product.ID and product.producttype.ID <> ? group by product.ID), " +
                    " cp from ProductCyclecount cp where (cp.nextDate <= ? or cp.nextDate = ? ) and " +
                    " cp.product.deleted=false and cp.product.company.companyID=? order by cp.product.name ";
        } else {
            query = "select cp.product, " +
                    " (select sum((case when carryIn=true then quantity else -quantity end)) from Inventory where deleted=false and product.ID=cp.product.ID and product.producttype.ID <> ? group by product.ID), " +
                    " (select cp1 from ProductCyclecount cp1 where cp1.product = cp.product),cp.ID " +
                    " from Cyclecount cp where cp.status = 1  and " +
                    " cp.product.deleted=false and cp.product.company.companyID=? order by cp.product.name ";
        }
        params.add((String) filterParams.get("companyid"));
        returnList = HibernateUtil.executeQuery(hibernateTemplate, query, params.toArray());
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }

    public KwlReturnObject cycleCountReport(HashMap<String, Object> filterParams, boolean isExport) throws ServiceException {
        List returnList = new ArrayList();
        int count = 0;
        try {
            ArrayList params = new ArrayList();
            String query = "";
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
            Date countStDate = sdf.parse((String) filterParams.get("stdate"));
            Date countEndDate = sdf.parse((String) filterParams.get("enddate"));
            countStDate = AccountingManager.setFilterTime(countStDate, true);
            countEndDate = AccountingManager.setFilterTime(countEndDate, false);
            String start = (String) filterParams.get("start");
            String limit = (String) filterParams.get("limit");

            query = "from Cyclecount cc where (cc.countDate >= ? and cc.countDate <= ? )and cc.product.company.companyID=?";
            params.add(countStDate);
            params.add(countEndDate);
            params.add((String) filterParams.get("companyid"));
            returnList = HibernateUtil.executeQuery(hibernateTemplate, query, params.toArray());
            count = returnList.size();
            if (StringUtil.isNullOrEmpty(start) == false && StringUtil.isNullOrEmpty(limit) == false && !isExport) {
                returnList = HibernateUtil.executeQueryPaging(hibernateTemplate, query, params.toArray(), new Integer[]{Integer.parseInt(start), Integer.parseInt(limit)});
            }
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("cycleCountReport : "+ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, returnList, count);
    }

    public KwlReturnObject saveCycleCount(HashMap<String, Object> dataMap) throws ServiceException {
        List list = new ArrayList();
        try {
            Cyclecount cc = new Cyclecount();
            if (dataMap.containsKey("id")) {
                cc = (Cyclecount) hibernateTemplate.get(Cyclecount.class, (String) dataMap.get("id"));
            }
            if (dataMap.containsKey("productid")) {
                Product product = dataMap.get("productid") == null ? null : (Product) hibernateTemplate.get(Product.class, (String) dataMap.get("productid"));
                cc.setProduct(product);
            }
            if (dataMap.containsKey("initquantity")) {
                cc.setIniquantity((Integer) dataMap.get("initquantity"));
            }
            if (dataMap.containsKey("countquantity")) {
                cc.setCountedquantity((Integer) dataMap.get("countquantity"));
            }
            if (dataMap.containsKey("status")) {
                cc.setStatus((Integer) dataMap.get("status"));
            }
            if (dataMap.containsKey("reason")) {
                cc.setReason((String) dataMap.get("reason"));
            }
            if (dataMap.containsKey("countdate")) {
                cc.setCountDate((Date) dataMap.get("countdate"));
            }
            hibernateTemplate.save(cc);
            list.add(cc);
        } catch (Exception e) {
            throw ServiceException.FAILURE("saveCycleCount : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "Cycle count has been added successfully", null, list, list.size());
    }

    public KwlReturnObject getCycleCountEntry(HashMap<String, Object> filterParams) throws ServiceException {
		List returnList = new ArrayList();
        ArrayList params=new ArrayList();
        String condition = "";
        String query="from ProductCyclecount ";

        if(filterParams.containsKey("productid")){
            condition += (condition.length()==0?" where ":" and ") + "product.ID=?";
            params.add(filterParams.get("productid"));
        }
        query += condition;
//        "select pc from ProductCyclecount pc where pc.product = ? ?"
        returnList = HibernateUtil.executeQuery(hibernateTemplate, query, params.toArray());
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }

    public ProductCyclecount getCycleCountEntryObject(HashMap<String, Object> filterParams) throws ServiceException {
		KwlReturnObject retObj = getCycleCountEntry(filterParams);
        ProductCyclecount pc = null;
        Iterator itr = retObj.getEntityList().iterator();
        while (itr.hasNext()) {
            pc = (ProductCyclecount) itr.next();
        }
        return pc;
    }

    public KwlReturnObject saveProductCycleCount(HashMap<String, Object> dataMap) throws ServiceException {
        List list = new ArrayList();
        try {
            ProductCyclecount pc = null;
            String productid = (String) dataMap.get("productid");

            if(!StringUtil.isNullOrEmpty(productid)){
                HashMap<String, Object> ccfilterParams = new HashMap<String, Object>();
                ccfilterParams.put("productid", productid);
                pc = getCycleCountEntryObject(ccfilterParams);
            }

            if (dataMap.containsKey("id")) {
                pc = (ProductCyclecount) hibernateTemplate.get(ProductCyclecount.class, (String) dataMap.get("id"));
            }

            if(pc==null){
                pc = new ProductCyclecount();
                pc.setStatus(0);
            }

            if (dataMap.containsKey("productid")) {
                Product product = productid==null?null:(Product) hibernateTemplate.get(Product.class, productid);
                pc.setProduct(product);
            }
            if (dataMap.containsKey("tolerance")) {
                pc.setTolerance((Integer) dataMap.get("tolerance"));
            }
            if (dataMap.containsKey("interval")) {
                pc.setCountInterval((Integer) dataMap.get("interval"));
            }
            if (dataMap.containsKey("prevdate")) {
                pc.setPrevDate((Date) dataMap.get("prevdate"));
            }
            if (dataMap.containsKey("nextdate")) {
                pc.setNextDate((Date) dataMap.get("nextdate"));
            }
            hibernateTemplate.save(pc);
            list.add(pc);
        } catch (Exception e) {
            throw ServiceException.FAILURE("saveProductCycleCount : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "Product cycle count has been added successfully", null, list, list.size());
    }

//Inventory
    public KwlReturnObject deleteInventory(String inventoryid, String companyid) throws ServiceException {
        String delQuery = "delete from Inventory where ID=? and company.companyID=?";
        int numRows = HibernateUtil.executeUpdate(hibernateTemplate, delQuery, new Object[]{inventoryid, companyid});

        return new KwlReturnObject(true, "Inventory entry has been deleted successfully.", null, null, numRows);
    }
     public KwlReturnObject deleteInventoryByProduct(String productid, String companyid) throws ServiceException {
        String delQuery = "delete from Inventory i where i.product.ID=? and i.company.companyID=?";
        int numRows = HibernateUtil.executeUpdate(hibernateTemplate, delQuery, new Object[]{productid, companyid});

        return new KwlReturnObject(true, "Inventory entry has been deleted successfully.", null, null, numRows);
    }

    public KwlReturnObject deleteInventoryEntry(String inventoryid, String companyid) throws ServiceException {
        String query = "update Inventory inv set inv.deleted=true where inv.ID=? and inv.company.companyID=?";
        int numRows = HibernateUtil.executeUpdate(hibernateTemplate, query, new Object[]{inventoryid, companyid});
        return new KwlReturnObject(true, "Inventory has been deleted successfully.", null, null, numRows);
    }

    public KwlReturnObject addInventory(JSONObject json) throws ServiceException {
        KwlReturnObject result;
        List list = new ArrayList();
        Inventory invetory=new Inventory();
        try{
            invetory = buildInventory(invetory, json);
            list.add(invetory);
            result = new KwlReturnObject(true, null, null, list, list.size());
        }catch(Exception ex){
//            result = new KwlReturnObject(true, "addInventory :"+ex.getMessage(), null, list, list.size());
            throw ServiceException.FAILURE("addInventory:"+ex.getMessage(), ex);
        }
        return result;
    }
    
    public KwlReturnObject updateInventory(JSONObject json) throws ServiceException {
        KwlReturnObject result;
        List list = new ArrayList();
        try{
        String inventoryid = json.getString("inventoryid");
            Inventory invetory = (Inventory) hibernateTemplate.get(Inventory.class, inventoryid);
            if(invetory != null) {
                invetory = buildInventory(invetory, json);
            }
            list.add(invetory);
            result = new KwlReturnObject(true, null, null, list, list.size());
        }catch(Exception ex){
            throw ServiceException.FAILURE("updateInventory:"+ex.getMessage(), ex);
        }
        return result;
    }

    public KwlReturnObject updateInitialInventory(JSONObject json) throws ServiceException {
        KwlReturnObject result;
        List list = new ArrayList();
        Inventory invetory;
        try{
        	String productid = json.getString("productid");
        	List Inventoryid = searchInventoryId(productid, true);
            if(Inventoryid.size() == 0){
            	invetory=new Inventory();
            }else{
            	invetory = (Inventory) Inventoryid.get(0);
            }
                invetory = buildInventory(invetory, json);
            
            list.add(invetory);
            result = new KwlReturnObject(true, null, null, list, list.size());
        }catch(Exception ex){
            throw ServiceException.FAILURE("updateInitialInventory:"+ex.getMessage(), ex);
        }
        return result;
    }
    
    public List searchInventoryId(String productid,boolean flag) throws ServiceException {
    	List resultList;
    	try{
			String selQuery = "from Inventory i where i.product.ID=? and i.newInv=?";
			resultList = HibernateUtil.executeQuery(hibernateTemplate, selQuery, new Object[]{productid, flag});
		}catch(Exception ex){
            throw ServiceException.FAILURE("searchInventoryId:"+ex.getMessage(), ex);
		}
			return resultList;
    }

    public Inventory buildInventory(Inventory invetory, JSONObject json) throws JSONException, UnsupportedEncodingException {
        Product product = (Product)hibernateTemplate.get(Product.class,json.getString("productid"));
       // if(!product.getProducttype().getID().equals(Producttype.SERVICE)){//service products
            if(json.has("productid")){
                invetory.setProduct((Product)hibernateTemplate.get(Product.class,json.getString("productid")));
            }
            if(json.has("quantity")){
                invetory.setQuantity(json.getInt("quantity"));
            }
            if(json.has("description")){
                invetory.setDescription(URLDecoder.decode(json.optString("description"), StaticValues.ENCODING));
            }
            if(json.has("carryin")){
                invetory.setCarryIn(json.getBoolean("carryin"));
            }
            if(json.has("defective")){
                invetory.setDefective(json.getBoolean("defective"));
            }
            if(json.has("newinventory")){
                invetory.setNewInv(json.getBoolean("newinventory"));
            }
            if(json.has("companyid")){
                invetory.setCompany((Company)hibernateTemplate.get(Company.class,json.getString("companyid")));
            }
            Date updateDate = new Date();
            if(json.has("updatedate")){
                updateDate = json.get("updatedate")!=null?(Date) json.get("updatedate"):new Date();
            }
            invetory.setUpdateDate(updateDate);
            hibernateTemplate.saveOrUpdate(invetory);
      //  }
        return invetory;
    }

    public KwlReturnObject getInventoryEntry(HashMap<String, Object> filterParams) throws ServiceException {
		List returnList = new ArrayList();
        ArrayList params=new ArrayList();
        String condition = "";
        String query="from Inventory ";

        if(filterParams.containsKey("newinv")){
            condition += (condition.length()==0?" where ":" and ") + "newinv=?";
            params.add(filterParams.get("newinv"));
        }
        if(filterParams.containsKey("productid")){
            condition += (condition.length()==0?" where ":" and ") + "product.ID=?";
            params.add(filterParams.get("productid"));
        }
        if(filterParams.containsKey("companyid")){
            condition += (condition.length()==0?" where ":" and ") + "company.companyID=?";
            params.add(filterParams.get("companyid"));
        }
        query += condition;
//        "from Inventory   where product.ID =? and newinv='T' and company.companyID=?"
        returnList = HibernateUtil.executeQuery(hibernateTemplate, query, params.toArray());
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }

    public KwlReturnObject getQuantity(String productid) throws ServiceException {
        List returnList = new ArrayList();
        try {
            ArrayList params=new ArrayList();
            params.add(Producttype.SERVICE);
            params.add(productid);            
            String query="select sum((case when carryIn=true then quantity else -quantity end)) from Inventory where deleted=false  and product.producttype.ID <> ? and product.ID=? group by product.ID";
            List list = HibernateUtil.executeQuery(hibernateTemplate, query, params.toArray());
            Iterator itr = list.iterator();
            if(itr.hasNext()) {
                returnList.add(itr.next());
            } else {
                returnList.add(null);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getQuantity : "+ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }

    public KwlReturnObject getInitialQuantity(String productid) throws ServiceException {
        List returnList = new ArrayList();
        try {
            ArrayList params=new ArrayList();
            params.add(Producttype.SERVICE);
            params.add(productid);
            String query="select quantity from Inventory where deleted=false  and product.producttype.ID <> ? and product.ID=?  and newinv='T' group by product.ID";
            List list = HibernateUtil.executeQuery(hibernateTemplate, query, params.toArray());
            Iterator itr = list.iterator();
            if(itr.hasNext()) {
                returnList.add(itr.next());
            } else {
                returnList.add(null);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getInitialQuantity : "+ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }

    public KwlReturnObject getProValuation(HashMap requestParam) throws ServiceException {
        ArrayList params = new ArrayList();
        List returnList = new ArrayList();
        String condition = "";
        Calendar endcal = Calendar.getInstance();

        String companyid = (String) requestParam.get("companyid");
        Date stDate = (Date) requestParam.get("stDate");
        Date endDate = (Date) requestParam.get("endDate");

        if (stDate != null) {
            params.add(AccountingManager.setFilterTime(stDate, true));
            condition += " and updateDate>= ? ";
        }
        if (endDate != null) {
            endcal.setTime(endDate);
            endcal.add(Calendar.DAY_OF_MONTH, -1);
            params.add(AccountingManager.setFilterTime(endcal.getTime(), false));
            condition += " and updateDate<= ? ";
        }
        params.add(companyid);
        params.add(Producttype.INVENTORY_PART);
        params.add(Producttype.ASSEMBLY);
        String query = "select p," +
                " (select sum(inventory.quantity*grd.rate)/sum(inventory.quantity) as avgamount from GoodsReceiptDetail grd where inventory.product.ID=p.ID group by inventory.product.ID), " + // not in use[PS]
                " (select pl1.price from PriceList pl1 where product.ID=p.ID and carryIn=true and applyDate in (select max(applyDate) as ld from PriceList where product.ID=pl1.product.ID and carryIn=pl1.carryIn group by product))," +
                " (select sum((case when carryIn=true then quantity else -quantity end)) from Inventory where deleted=false and product.ID=p.ID" + condition + " group by product.ID) " +
                " from Product p where p.deleted=false and p.company.companyID=?" +//p.parent is null and
                " and (p.producttype.ID=? or p.producttype.ID=?) " +//Inventory Part or inventory assembly
                " order by p.name ";

        returnList = HibernateUtil.executeQuery(hibernateTemplate, query, params.toArray());
        int count = returnList.size();
        return new KwlReturnObject(true, "", null, returnList, count);
    }

    public KwlReturnObject getQuantityPurchaseOrSales(String productid, boolean isPurchase) throws ServiceException {
        List returnList = new ArrayList();
        try {
            ArrayList params=new ArrayList();
            params.add(isPurchase);
            params.add(productid);
            String query="select sum((case when carryIn=? then quantity else  0 end)) from Inventory where product.ID= ? group by product.ID";
            returnList = HibernateUtil.executeQuery(hibernateTemplate, query, params.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getPurchaseOrSalesQuantity : "+ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }
        public KwlReturnObject getQuantityPurchaseOrSalesDetails(String productid, boolean isPurchase) throws ServiceException {
        List returnList = new ArrayList();
        try {
            ArrayList params=new ArrayList();
            params.add(productid);
            params.add(isPurchase);            
            String query="from Inventory where product.ID= ? and carryIn=? and deleted = false";
            returnList = HibernateUtil.executeQuery(hibernateTemplate, query, params.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getPurchaseOrSalesQuantity : "+ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }

    public KwlReturnObject getInitialCost(String productid) throws ServiceException{
        String query = "select price from PriceList where carryin = 'T' and product.ID = ? and " +
                    " applyDate = (select min(applyDate) as ld from PriceList where product.ID=? and carryIn='T') ";

        List returnList = HibernateUtil.executeQuery(hibernateTemplate, query, new Object[] {productid,productid});

        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }

    public KwlReturnObject getAvgcostAssemblyProduct(String productid, Date stDate, Date endDate) throws ServiceException {
        double avgCost = 0;
        List returnList = new ArrayList();
        ArrayList params=new ArrayList();
        params.add(productid);
        String condition="";
        if(stDate!=null){
            params.add(stDate);
            condition+= "  and inv.updateDate>= ?";
        }
        if(endDate!=null){
            params.add(endDate);
            condition+= "  and inv.updateDate<= ?";
        }
        String query = "select inv, " +
                " (select pl1.price from PriceList pl1 where product.ID=inv.product.ID and carryIn=true and applyDate in (select max(applyDate) as ld from PriceList where product.ID=inv.product.ID and carryIn=true and date(applyDate)<=date(inv.updateDate)  group by product))" +
                "from Inventory inv " +
                " where inv.deleted=false and product.ID=? and carryIn = true "+condition+" group by product.ID";

        List resultList = HibernateUtil.executeQuery(hibernateTemplate, query, params.toArray());
        Iterator itr = resultList.iterator();
        double totalCost = 0;
        double totalQuantity = 0;
        while (itr.hasNext()) {
            Object[] row = (Object[]) itr.next();
            Inventory invObj = (Inventory) row[0];
            double qua = invObj.getQuantity();
            double amount =  (row[1]==null?0.0:(Double)row[1]);
            totalQuantity += qua;
            totalCost += (qua * amount);
        }
        if (totalQuantity != 0) {
            avgCost = (totalCost / totalQuantity);
        }

        returnList.add(avgCost);

        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }

    public KwlReturnObject getInventoryOpeningBalanceDate(String companyid) throws ServiceException{
        String query = "select min(updateDate) from Inventory where product.company.companyID = ?";
        List returnList = HibernateUtil.executeQuery(hibernateTemplate, query, companyid);
        
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }

    public boolean isChild(String ParentID, String childID) throws ServiceException{
        String query = "select id from product where parent=? and id=?";
        List list = HibernateUtil.executeSQLQuery(hibernateTemplate.getSessionFactory().getCurrentSession(), query, new Object[]{ParentID, childID});
        return !list.isEmpty();
    }
    
    public KwlReturnObject getProductList(Map<String, Object> requestParams) throws ServiceException {
        List returnList = new ArrayList();
        int count = 0;
        try {
            String companyId = (String) requestParams.get("companyid");
            String start = (String) requestParams.get("start");
            String limit = (String) requestParams.get("limit");
            String ss=(String)requestParams.get("ss");
            String condition = "";
            ArrayList   params=new ArrayList();
            params.add(companyId);
            if(!StringUtil.isNullOrEmpty(ss)){
                condition = StringUtil.getSearchString(ss, "and", new String[]{"p.name"});
                params.add(ss);
                params.add(ss+"%");
            }
            String query="select p from Product p where  p.deleted=false and p.company.companyID=? "+condition ;
            returnList = HibernateUtil.executeQuery(hibernateTemplate, query,params.toArray());
            count = returnList.size();
            if (StringUtil.isNullOrEmpty(start) == false && StringUtil.isNullOrEmpty(limit) == false) {
                returnList = HibernateUtil.executeQueryPaging(hibernateTemplate, query,params.toArray(), new Integer[]{Integer.parseInt(start), Integer.parseInt(limit)});
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getSalesByItem : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, returnList, count);
    }

    // Done by Neeraj
    public KwlReturnObject selectInventoryByProduct(String productid, String companyid) throws ServiceException {
        String selectQuery = "from Inventory i where i.product.ID=? and i.company.companyID=?";
        List Records = HibernateUtil.executeQuery(hibernateTemplate, selectQuery, new Object[]{productid, companyid});
        return new KwlReturnObject(true, "", null, null, Records.size());
    }
    
    public KwlReturnObject selectSubProductFromAssembly(String subProductId) throws ServiceException {
        String selectQuery = "from ProductAssembly p where p.subproducts.ID = ?";
        List Records = HibernateUtil.executeQuery(hibernateTemplate, selectQuery, new Object[]{subProductId});
        return new KwlReturnObject(true, "", null, null, Records.size());
    }
    
    public KwlReturnObject selectProductCycleCountPermanently(String productid,String companyid) throws ServiceException {
        String selectQuery = "from ProductCyclecount p where p.product.ID=? ";
        List Records = HibernateUtil.executeQuery(hibernateTemplate, selectQuery, new Object[]{productid});
        return new KwlReturnObject(true, "", null, null, Records.size());
    }

    public KwlReturnObject selectCyclecountPermanently(String productid,String companyid) throws ServiceException {
        String selectQuery = "from Cyclecount c where c.product.ID=? ";
        List Records = HibernateUtil.executeQuery(hibernateTemplate, selectQuery, new Object[]{productid});
        return new KwlReturnObject(true, "", null, null, Records.size());
    }
    
    public KwlReturnObject selectProPricePermanently(String productid,String companyid) throws ServiceException {
        String selectQuery = "from PriceList p where p.product.ID=? and p.company.companyID=?";
        List Records = HibernateUtil.executeQuery(hibernateTemplate, selectQuery, new Object[]{productid,companyid});
        return new KwlReturnObject(true, "", null, null, Records.size());
    }
    
    @Override
    public List isChildorGrandChild(String childID) throws ServiceException{
        String query = "from Product where ID=? and parent != null";
        List Result = HibernateUtil.executeQuery(hibernateTemplate, query, new Object[]{childID});
        return Result;
    }

    public KwlReturnObject getQtyandUnitCost(String productid, Date endDate) throws ServiceException {
    	try{
    	    String selQuery = "select quantity, updateDate from Inventory inv where inv.product.ID=? and inv.updateDate<=? and inv.carryIn=true and inv.newInv=false";
    	    List list = HibernateUtil.executeQuery(hibernateTemplate, selQuery, new Object[]{productid, endDate});
    	    return new KwlReturnObject(true, "Rate and Quantity for the product", null, list, list.size());
    	}catch(Exception ex){
    		System.out.print(ex);
    		throw ServiceException.FAILURE(ex.getMessage(), ex);
    	}
    }


	@Override
	public KwlReturnObject getAssemblyProductBillofMaterials(String productid) throws ServiceException {
		try{
			String selectQuery = "from ProductAssembly ap where ap.product.ID=?";
	        List Records = HibernateUtil.executeQuery(hibernateTemplate, selectQuery, new Object[]{productid});
	        return new KwlReturnObject(true, "Bill of Materials", null, Records, Records.size());
		}catch(Exception ex){
			 Logger.getLogger(accProductImpl.class.getName()).log(Level.SEVERE, null, ex);
			 throw ServiceException.FAILURE("getAssemblyProductBillofMaterials : "+ex.getMessage(), ex);
    	}
	}
	
    public String getNextAutoProductIdNumber(String companyid) throws ServiceException {
        try{
	    	String autoNumber="";
	        String table="Product", field="productid", pattern="PD000000";
	                 
	        String query="select max("+field+") from "+table+" where "+field+" like ? and company.companyID=?";
	        List list=HibernateUtil.executeQuery(hibernateTemplate, query, new Object[]{pattern.replace('0', '_'), companyid});
	        if(list.isEmpty()==false)autoNumber=(String)list.get(0);
	
	        while(!pattern.equals(autoNumber)){
	            autoNumber = AccountingManager.generateNextAutoNumber(pattern, autoNumber);
	            query="select "+field+" from "+table+" where "+field+" = ? and company.companyID=?";
	            list=HibernateUtil.executeQuery(hibernateTemplate, query, new Object[]{autoNumber, companyid});
	            if(list.isEmpty()) return autoNumber;
	        }

	        return autoNumber;
        } catch(Exception ex){
        	throw ServiceException.FAILURE("getNextAutoProductIdNumber : "+ex.getMessage(), ex);
        }
    }

    public KwlReturnObject getSoldorPurchaseQty(String productid, boolean sold) throws ServiceException {
    	try{
    		String query="from Inventory i where i.product.id=? and i.carryIn=? and i.deleted=false";
    		List list=HibernateUtil.executeQuery(hibernateTemplate, query, new Object[]{productid,sold});
    		return new KwlReturnObject(true, "Sold or Purchased Quantity for the product", null, list, list.size());
    	} catch(Exception ex){
        	throw ServiceException.FAILURE("getSoldorPurchaseQty : "+ex.getMessage(), ex);
        }
    }
    
    public KwlReturnObject getRateandQtyfromInvoice(String productid) throws ServiceException {
    	try{
    		String query="from GoodsReceiptDetail grd where grd.inventory.product.id=? and grd.goodsReceipt.deleted = false";
    		List list=HibernateUtil.executeQuery(hibernateTemplate, query, new Object[]{productid});
    		return new KwlReturnObject(true, "Rate and qty of product from invoice", null, list, list.size());
    	}catch(Exception ex){
    		ex.printStackTrace();
        	throw ServiceException.FAILURE("getRateandQtyfromInvoice : "+ex.getMessage(), ex);
    	}
    }
    
    public KwlReturnObject checkSubProductforAssembly(String productid) throws ServiceException {
    	try{
    		String query = "from ProductAssembly p where p.subproducts.ID=? and p.product.deleted=false";
    		List list=HibernateUtil.executeQuery(hibernateTemplate, query, new Object[]{productid});
    		return new KwlReturnObject(true, "", null, list, list.size());
    	} catch(Exception ex){
        	throw ServiceException.FAILURE("checkSubProductforAssembly : "+ex.getMessage(), ex);
        }
    }
    
    public KwlReturnObject checkIfParentProduct(String productid) throws ServiceException {
    	try {
    		String query = "from Product p where p.parent.ID=?";
    		List list=HibernateUtil.executeQuery(hibernateTemplate, query, new Object[]{productid});
    		return new KwlReturnObject(true, "", null, list, list.size());
    	} catch(Exception ex){
    		throw ServiceException.FAILURE("checkIfParentProduct : "+ex.getMessage(), ex);
    	}
    }
}
