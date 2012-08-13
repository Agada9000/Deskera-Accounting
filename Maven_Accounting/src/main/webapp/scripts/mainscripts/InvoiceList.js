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




function openCashTransTab(isCustomer,isCustBill){
    if(isCustBill)
        callBillingSalesReceipt(false,null);
   else if(isCustomer)
        callSalesReceipt(false,null);
    else
        callPurchaseReceipt(false,null);
    }

function openInvTab(isTran,isOrder,isCustBill){
    if(isCustBill){        
        if(isTran && isOrder)
            callBillingSalesOrder(false,null);
        else if(isTran)
            callBillingInvoice(false,null);
        else if(!isTran && isOrder)
            callBillingPurchaseOrder(false,null);
        else
            callBillingGoodsReceipt(false,null);
    }else{
        if(isTran && isOrder)
            callSalesOrder(false,null);
        else if(isTran)
            callInvoice(false,null);
        else if(!isTran && isOrder)
            callPurchaseOrder(false,null);
        else
            callGoodsReceipt(false,null);
    }
}

function openQuotationTab(){
	callQuotation();
}
/*< COMPONENT USED FOR >
 *      1.Invoice and Cash Sales Report
 *          callInvoiceList(id,check,isCash) --- < Invoice and Cash Sales Report >
 *          [isCash:isCash, isCustomer:true, isOrder:false]
 *      2.Invoice Report
 *          callBillingInvoiceList(id,check,isCash) --- < Invoice and Cash Sales Report >
 *          [isCash:isCash, isCustBill:true, isCustomer:true, isOrder:false]
 *      3.Credit/Cash Purchase Report
 *          callGoodsReceiptList(id,check)
 *          [isOrder:false, isCustomer:false]
 *      4.Purchase Order Report
 *          callPurchaseOrderList()
 *          [isOrder:true, isCustomer:false]
 *      5.Sales Order Report
 *          callSalesOrderList() -- < Sales Order Report >
 *          [isOrder:true, isCustomer:true]
 *
 *      this.appendID:This is useful for displaying help , TRUE => It is used when this.id is appended in the id of component.
 */
Wtf.account.TransactionListPanel=function(config){
    this.appendID = true;
    this.invID=null;
    this.exponly=null;
    this.recArr=[];
    this.isCash=true;
    this.businessPerson=(config.isCustomer?'Customer':'Vendor');
    this.costCenterId = "";
    this.extraFilters = config.extraFilters;
    if(config.extraFilters != undefined){//Cost Center Report View
        this.costCenterId = config.extraFilters.costcenter?config.extraFilters.costcenter:"";
    }
    this.label = config.label;
    this.isOrder=config.isOrder;
    this.isCustBill=config.isCustBill;
    this.isCash=config.cash?true:false;
    this.isexpenseinv=false;
    this.nondeleted=false;
    this.deleted=false;
    this.uPermType=(config.isCustomer?Wtf.UPerm.invoice:Wtf.UPerm.vendorinvoice);
    this.permType=(config.isCustomer?Wtf.Perm.invoice:Wtf.Perm.vendorinvoice);
    this.uPaymentPermType=(config.isCustomer?Wtf.UPerm.invoice:Wtf.UPerm.vendorinvoice);
    this.createPaymentPermType=(config.isCustomer?Wtf.Perm.invoice.createreceipt:Wtf.Perm.vendorinvoice.createpayment);
    this.exportPermType=(config.isCustomer?(this.isOrder?this.permType.exportdataso:this.permType.exportdatainvoice):(this.isOrder?this.permType.exportdatapo:this.permType.exportdatavendorinvoice));
    this.printPermType=(config.isCustomer?(this.isOrder?this.permType.printso:this.permType.printinvoice):(this.isOrder?this.permType.printpo:this.permType.printvendorinvoice));
    this.removePermType=(config.isCustomer?(this.isOrder?this.permType.removeso:this.permType.removeinvoice):(this.isOrder?this.permType.removepo:this.permType.removevendorinvoice));
    this.editPermType=(config.isCustomer?(this.isOrder?this.permType.editso:this.permType.editinvoice):(this.isOrder?this.permType.editpo:this.permType.editvendorinvoice));
    this.copyPermType=(config.isCustomer?this.permType.copyinvoice:this.permType.copyvendorinvoice);
    this.emailPermType=(config.isCustomer?this.permType.emailinvoice:this.permType.emailvendorinvoice);
    this.recurringPermType=this.permType.recurringinvoice;
    this.isQuotation = config.isQuotation;
    if(this.isQuotation == undefined || this.isQuotation == null){
    	this.isQuotation = false;
    }
    this.expandRec = Wtf.data.Record.create ([
        {name:'productname'},
        {name:'productdetail'},
        {name:'prdiscount'},
        {name:'amount'},
        {name:'productid'},
        {name:'accountid'},
        {name:'accountname'},
        {name:'quantity'},
        {name:'unitname'},
        {name:'rate'},
        {name:'rateinbase'},
        {name:'externalcurrencyrate'},
        {name:'prtaxpercent'},
        {name:'orderrate'},
        {name:'desc', convert:WtfGlobal.shortString},
        {name:'productmoved'},
        {name:'currencysymbol'},
        {name:'currencyrate'},
        {name: 'type'},
        {name: 'pid'},
        {name:'carryin'}
    ]);
    this.expandStoreUrl = "";
    if(this.businessPerson=="Customer"){
        //mode:this.isOrder?43:(this.isCustBill?17:14)
        this.expandStoreUrl = "ACC" + (this.isOrder?(this.isCustBill?"SalesOrderCMN/getBillingSalesOrderRows":"SalesOrderCMN/getSalesOrderRows"):(this.isCustBill?"InvoiceCMN/getBillingInvoiceRows":"InvoiceCMN/getInvoiceRows")) + ".do";
    }else if(this.businessPerson=="Vendor"){
        this.expandStoreUrl = "ACC" + (this.isOrder?(this.isCustBill?"PurchaseOrderCMN/getBillingPurchaseOrderRows":"PurchaseOrderCMN/getPurchaseOrderRows"):(this.isCustBill?"GoodsReceiptCMN/getBillingGoodsReceiptRows":"GoodsReceiptCMN/getGoodsReceiptRows")) + ".do";
    }
    if(this.isQuotation){
    	this.expandStoreUrl = "ACCSalesOrderCMN/getQuotationRows.do";
    }
    this.expandStore = new Wtf.data.Store({
        url:this.expandStoreUrl,
//        url:Wtf.req.account+this.businessPerson+'Manager.jsp',
        baseParams:{
            mode:this.isOrder?(this.isCustBill?53:43):(this.isCustBill?17:14)
        },
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        },this.expandRec)
    });
    this.GridRec = Wtf.data.Record.create ([
        {name:'billid'},
        {name:'journalentryid'},
        {name:'entryno'},
        {name:'billto'},
        {name:'discount'},
        {name:'currencysymbol'},
        {name:'orderamount'},
        {name:'isexpenseinv'},
        {name:'currencyid'},
        {name:'shipto'},
        {name:'mode'},
        {name:'billno'},
        {name:'date', type:'date'},
        {name:'duedate', type:'date'},
        {name:'shipdate', type:'date'},
        {name:'personname'},
        {name:'personemail'},
        {name:'personid'},
        {name:'shipping'},
        {name:'othercharges'},
        {name:'amount'},
        {name:'amountdue'},
        {name:'termdays'},
        {name:'termname'},
        {name:'incash'},
        {name:'taxamount'},
        {name:'taxid'},
        {name:'orderamountwithTax'},
        {name:'taxincluded',type:'boolean'},
        {name:'taxname'},
        {name:'deleted'},
        {name:'amountinbase'},
        {name:'memo'},
        {name:'externalcurrencyrate'},
        {name:'ispercentdiscount'},
        {name:'discountval'},
        {name:'crdraccid'},
        {name:'creditDays'},
        {name:'isRepeated'},
        {name:'porefno'},
        {name:'costcenterid'},
        {name:'costcenterName'},
        {name:'interval'},
        {name:'intervalType'},
        {name:'startDate', type:'date'},
        {name:'nextDate', type:'date'},
        {name:'expireDate', type:'date'},
        {name:'repeateid'},
        {name:'status'}
    ]);
    this.StoreUrl = "";
    if(this.businessPerson=="Customer"){
        //mode:this.isOrder?42:(this.isCustBill?16:12)
        this.StoreUrl = "ACC" + (this.isOrder?(this.isCustBill?"SalesOrderCMN/getBillingSalesOrders":"SalesOrderCMN/getSalesOrders"):(this.isCustBill?"InvoiceCMN/getBillingInvoices":"InvoiceCMN/getInvoices")) + ".do";
    }else if(this.businessPerson=="Vendor"){
        this.StoreUrl = "ACC" + (this.isOrder?(this.isCustBill?"PurchaseOrderCMN/getBillingPurchaseOrders":"PurchaseOrderCMN/getPurchaseOrders"):(this.isCustBill?"GoodsReceiptCMN/getBillingGoodsReceipts":"GoodsReceiptCMN/getGoodsReceipts")) + ".do";
    }
    if(this.isQuotation){
    	this.StoreUrl = "ACCSalesOrderCMN/getQuotations.do";
    }
    this.Store = new Wtf.data.Store({
        url:this.StoreUrl,
//        url: Wtf.req.account+this.businessPerson+'Manager.jsp',
        baseParams:{
            mode:this.isOrder?(this.isCustBill?52:42):(this.isCustBill?16:12),
            costCenterId: this.costCenterId,
            deleted:false,
            nondeleted:false,
            cashonly:false,
            creditonly:false
        },
        reader: new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty:'count'
        },this.GridRec)
    });

    if(this.extraFilters != undefined){//Cost Center Report View
        var currentBaseParams = this.Store.baseParams;
        currentBaseParams.startdate = this.extraFilters.startdate;
        currentBaseParams.enddate = this.extraFilters.enddate;
        this.Store.baseParams=currentBaseParams;
    }
    var cashType=config.isCustomer?WtfGlobal.getLocaleText("acc.accPref.autoCS"):WtfGlobal.getLocaleText("acc.accPref.autoCP");    //"Cash Sales":"Cash Purchase";
    var creditType=config.isCustomer?WtfGlobal.getLocaleText("acc.accPref.autoInvoice"):WtfGlobal.getLocaleText("acc.accPref.autoVI");   //"Invoice":"Vendor Invoice";
    this.typeStore = new Wtf.data.SimpleStore({
        fields: [{name:'typeid',type:'int'}, 'name'],
        data :[[0,WtfGlobal.getLocaleText("acc.rem.105")],[1,cashType],[2,creditType],[3,WtfGlobal.getLocaleText("acc.rem.106")],[4,WtfGlobal.getLocaleText("acc.rem.107")]]
    });
    this.typeEditor = new Wtf.form.ComboBox({
        store: this.typeStore,
        name:'typeid',
        displayField:'name',
        id:'view'+config.helpmodeid+config.id,
        valueField:'typeid',
        mode: 'local',
        defaultValue:0,
        width:160,
        listWidth:160,
        triggerAction: 'all',
        typeAhead:true,
        selectOnFocus:true
    });

    chkCostCenterload();
    if(Wtf.CostCenterStore.getCount()==0) Wtf.CostCenterStore.on("load", this.setCostCenter, this);
    this.costCenter = new Wtf.form.ComboBox({
        store: Wtf.CostCenterStore,
        name:'costCenterId',
        width:150,
        listWidth:150,
        displayField:'name',
        valueField:'id',
        triggerAction: 'all',
        mode: 'local',
        typeAhead:true,
        value: this.costCenterId,
        selectOnFocus:true,
        forceSelection: true
    });

    this.costCenter.on("select", function(cmb, rec, ind){
        this.costCenterId = rec.data.id;

        var currentBaseParams = this.Store.baseParams;
        currentBaseParams.costCenterId = this.costCenterId;
        this.Store.baseParams=currentBaseParams;

        this.loadStore();
    },this);

    this.tbar2 = "";
    if(config.extraFilters == undefined){//Cost Center Report View - Don't show 'cost center' filter
        if(!config.isOrder&&!this.isQuotation){// For invoice & Vendor Invoice show 'cost center' and 'view' filters in 2nd tbar applied for grid
            this.tbar2 = new Array();
            this.tbar2.push(WtfGlobal.getLocaleText("acc.common.costCenter"), this.costCenter);
            this.tbar2.push("&nbsp;View", this.typeEditor);
        }
    }
    this.emptytext1=WtfGlobal.emptyGridRenderer("<a class='grid-link-text' href='#' onClick='javascript: openInvTab("+config.isCustomer+","+config.isOrder+","+config.isCustBill+","+config.isQuotation+")'>"+" "+WtfGlobal.getLocaleText("acc.rem.147")+" "+this.label+" "+WtfGlobal.getLocaleText("acc.rem.148")+"</a>")    ;
    this.emptytext2=WtfGlobal.emptyGridRenderer("<a class='grid-link-text' href='#' onClick='javascript: openCashTransTab("+config.isCustomer+","+config.isCustBill+")'>"+WtfGlobal.getLocaleText("acc.rem.147")+" "+cashType+" "+WtfGlobal.getLocaleText("acc.rem.148")+" </a>")    ;
    this.emptytext3=WtfGlobal.emptyGridRenderer("<a class='grid-link-text' href='#' onClick='javascript: openQuotationTab()'>"+WtfGlobal.getLocaleText("acc.rem.147")+" "+WtfGlobal.getLocaleText("acc.accPref.autoQN")+" "+WtfGlobal.getLocaleText("acc.rem.148")+"</a>")    ;
    this.deletedRecordsEmptyTxt = WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
    this.expander = new Wtf.grid.RowExpander({});
    this.sm = new Wtf.grid.CheckboxSelectionModel();
    this.grid = new Wtf.grid.GridPanel({
        stripeRows :true,
        store:this.Store,
        id:"gridmsg"+config.helpmodeid+config.id,
        border:false,
        sm:this.sm,
        tbar: this.tbar2,
        layout:'fit',
       // loadMask:true,
        plugins: this.expander,
        viewConfig:{forceFit:true,emptyText:(config.isQuotation?this.emptytext3 : (this.emptytext1+(config.isOrder?"":"<br>"+this.emptytext2)))},
        forceFit:true,
        columns:[this.sm,this.expander,{
            hidden:true,
            dataIndex:'billid'
        },{
            header:this.label+" "+WtfGlobal.getLocaleText("acc.cn.9"),
            dataIndex:'billno',
            pdfwidth:75,
            renderer:(config.isQuotation||config.isOrder)?"":WtfGlobal.linkDeletedRenderer
        },{
            header:WtfGlobal.getLocaleText("acc.invoiceList.jeno"),  //"Journal Entry No",
            dataIndex:'entryno',
            hidden:this.isOrder||this.isQuotation,
            pdfwidth:75,
            renderer:WtfGlobal.linkDeletedRenderer
        }/*,{
            header:"Vendor Invoice No",
            dataIndex:'this.isOrder?'orderamount':vendorinvoice',
            hidden:this.isOrder,
            pdfwidth:75
        }*/,{
            header:this.label+" "+WtfGlobal.getLocaleText("acc.inventoryList.date"),
            dataIndex:'date',
            align:'center',
            pdfwidth:80,
            renderer:WtfGlobal.onlyDateDeletedRenderer
        },{
            header:WtfGlobal.getLocaleText("acc.invoiceList.due"),  //"Due Date",
            dataIndex:'duedate',
            align:'center',
            pdfwidth:80,
            renderer:WtfGlobal.onlyDateDeletedRenderer,
            hidden:this.isQuotation
        },{
            dataIndex:'shipdate',
            hidden:true            
        },{
            header:(config.isCustomer?WtfGlobal.getLocaleText("acc.invoiceList.cust"):WtfGlobal.getLocaleText("acc.invoiceList.ven")),  //this.businessPerson,
            pdfwidth:75,
            renderer:WtfGlobal.deletedRenderer,
            dataIndex:'personname'
        },{
            header:WtfGlobal.getLocaleText("acc.invoiceList.discount"),  //"Discount",
            dataIndex:'discount',
            align:'right',
            pdfwidth:75,
            renderer:((this.isOrder||this.isQuotation)?WtfGlobal.currencyRendererDeletedSymbol:WtfGlobal.withoutRateCurrencyDeletedSymbol),
            hidden:this.isOrder && !this.isQuotation
        },{
            header:WtfGlobal.getLocaleText("acc.invoiceList.taxName"),  //"Tax Name",
            dataIndex:'taxname',
            pdfwidth:75,
            renderer:WtfGlobal.deletedRenderer,
            hidden:!this.isOrder
        },{
            header:WtfGlobal.getLocaleText("acc.invoiceList.taxAmt"),  //"Tax Amount",
            dataIndex:'taxamount',
            align:'right',
            pdfwidth:75,
            renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol
        },{
            header:WtfGlobal.getLocaleText("acc.invoiceList.totAmt"),  //"Total Amount",
            align:'right',
            dataIndex:((this.isOrder||this.isQuotation)?'orderamountwithTax':'amount'),
            pdfwidth:75,
            renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol
        },{
            header:WtfGlobal.getLocaleText("acc.invoiceList.totAmtHome"),  //"Total Amount (In Home Currency)",
            align:'right',
            hidden:(this.isQuotation?false:this.isOrder), //this.quotation?false:this.isOrder,
            dataIndex:'amountinbase',
            pdfwidth:75,
            renderer:WtfGlobal.currencyDeletedRenderer
        },{
            header:WtfGlobal.getLocaleText("acc.invoiceList.amtDue"),  //"Amount Due",
            dataIndex:'amountdue',
            align:'right',
            hidden:this.isOrder||this.isQuotation,
            pdfwidth:75,
            renderer:(this.isOrder?WtfGlobal.currencyRendererDeletedSymbol:WtfGlobal.withoutRateCurrencyDeletedSymbol)
        },{
            header:WtfGlobal.getLocaleText("acc.invoiceList.memo"),  //"Memo",
            dataIndex:'memo',
            renderer:function(value){
                var res = "<span class='gridRow' style='width:200px;'  wtf:qtip='"+value+"'>"+Wtf.util.Format.ellipsis(value,20)+"</span>";
                return res;
            },
            pdfwidth:100
        },{
            header:WtfGlobal.getLocaleText("acc.invoiceList.status"),  //"Status",
            dataIndex:'status',
             hidden:!this.isOrder || this.isQuotation,
             renderer:WtfGlobal.deletedRenderer,
            pdfwidth:100
//         },{
//            header:"Expense Type",
//            dataIndex:'isexpenseinv',
//            hidden:this.isOrder,
//            pdfwidth:100
        }]
    });
    this.resetBttn=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.common.reset"),  //'Reset',
        tooltip :WtfGlobal.getLocaleText("acc.common.resetTT"),  //'Allows you to add a new search term by clearing existing search terms.',
        id: 'btnRec' + this.id,
        scope: this,
        iconCls :getButtonIconCls(Wtf.etype.resetbutton),
        disabled :false 
    });
    this.resetBttn.on('click',this.handleResetClick,this);

    var btnArr=[];
    var tranType=null;
    if(this.isCustBill)
        tranType=config.isCustomer?(config.isOrder?Wtf.autoNum.BillingSalesOrder:Wtf.autoNum.BillingInvoice):(config.isOrder?Wtf.autoNum.BillingPurchaseOrder:Wtf.autoNum.BillingGoodsReceipt);
    else
        tranType=config.isCustomer?(config.isOrder?Wtf.autoNum.SalesOrder:Wtf.autoNum.Invoice):(config.isOrder?Wtf.autoNum.PurchaseOrder:Wtf.autoNum.GoodsReceipt);
    if(this.isQuotation)
    	tranType=Wtf.autoNum.Quotation;
    
    var singlePDFtext = null;
    if(this.isQuotation)
    	singlePDFtext = WtfGlobal.getLocaleText("acc.accPref.autoQN");
    else
    	singlePDFtext = config.isCustomer?(config.isOrder?WtfGlobal.getLocaleText("acc.accPref.autoSO"):WtfGlobal.getLocaleText("acc.accPref.autoInvoice")):(config.isOrder?WtfGlobal.getLocaleText("acc.accPref.autoPO"):WtfGlobal.getLocaleText("acc.accPref.autoVI"));

if(config.extraFilters == undefined){//Cost Center Report View - Don't show Buttons
   if(!WtfGlobal.EnableDisable(this.uPermType, this.editPermType)){				//!this.isOrder&&
        btnArr.push(this.editBttn=new Wtf.Toolbar.Button({
                text:WtfGlobal.getLocaleText("acc.common.edit"),  //'Edit',
                tooltip :(this.isOrder)?WtfGlobal.getLocaleText("acc.invoiceList.editO"):WtfGlobal.getLocaleText("acc.invoiceList.editI"),  //'Allows you to edit Order.':'Allows you to edit Invoice.',
                id: 'btnEdit' + this.id,
                scope: this,
                hidden:this.isQuotation,
                iconCls :getButtonIconCls(Wtf.etype.edit),
                disabled :true
        }));
        this.editBttn.on('click',this.isOrder?this.editOrderTransaction:this.editTransaction.createDelegate(this,[false]),this);
//        this.editBttn.on('click',this.editTransaction.createDelegate(this,[false]),this);
    }
   if(!this.isQuotation&&!this.isOrder&&!WtfGlobal.EnableDisable(this.uPermType, this.copyPermType)){
        btnArr.push(this.copyInvBttn=new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.invoiceList.copyInv"),  //'Copy Invoice',
            tooltip :WtfGlobal.getLocaleText("acc.invoiceList.copyInvTT"),  //'Allows you to Copy Invoice.',
            id: 'btnCopyInv' + this.id,
            scope: this,
            hidden:this.isOrder,//this.isCustBill||
            iconCls :getButtonIconCls(Wtf.etype.copy),
            disabled :true
        }));
        this.copyInvBttn.on('click',this.editTransaction.createDelegate(this,[true]),this);
    }
   if(!WtfGlobal.EnableDisable(this.uPermType, this.removePermType)){
        btnArr.push(this.deleteTrans=new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.rem.7")+' '+this.label,
            scope: this,
            //hidden:config.isOrder,
            tooltip:WtfGlobal.getLocaleText("acc.rem.6"),  //{text:"Select a "+this.label+" to delete.",dtext:"Select a "+this.label+" to delete.", etext:"Delete selected "+this.label+" details."},
            iconCls:getButtonIconCls(Wtf.etype.deletebutton),
            disabled :true,
            handler:this.handleDelete.createDelegate(this)
       }))
   }
    this.operationType = tranType;
    if(this.operationType==Wtf.autoNum.Invoice || this.operationType==Wtf.autoNum.GoodsReceipt || this.operationType==Wtf.autoNum.BillingInvoice || this.operationType==Wtf.autoNum.BillingGoodsReceipt || this.operationType==Wtf.autoNum.Quotation) {
        var bText = "";
        var bToolTip = "";
        if(this.operationType==Wtf.autoNum.Invoice || this.operationType==Wtf.autoNum.BillingInvoice){
            bText = WtfGlobal.getLocaleText("acc.invoiceList.recPay");  //"Receive Payment";
            bToolTip = WtfGlobal.getLocaleText("acc.invoiceList.recPayTT");  //"Allows you to Receive Payment for invoice.";
        }else if(this.operationType==Wtf.autoNum.GoodsReceipt || this.operationType==Wtf.autoNum.BillingGoodsReceipt){
            bText = WtfGlobal.getLocaleText("acc.invoiceList.mP");  //"Make Payment";
            bToolTip = WtfGlobal.getLocaleText("acc.invoiceList.mPTT");  //"Allows you to Make Payment for vendor invoice.";
        }
     if(!this.isQuotation&&!this.isOrder&&!WtfGlobal.EnableDisable(this.uPaymentPermType, this.createPaymentPermType)){
            btnArr.push(this.paymentButton=new Wtf.Toolbar.Button({
                text: bText,
                tooltip : bToolTip,
                scope: this,
                iconCls : "accountingbase financialreport",
                disabled : true,
                handler : this.makePayment
            }));
        }
   if(!this.isOrder&&!WtfGlobal.EnableDisable(this.uPermType, this.emailPermType)){
         btnArr.push(this.email=new Wtf.Toolbar.Button({
                text:WtfGlobal.getLocaleText("acc.common.email"),  // "Email",
                tooltip : WtfGlobal.getLocaleText("acc.common.emailTT"),  //"Email",
                scope: this,
                iconCls : "accountingbase financialreport",
                disabled : true,
                handler : this.sendMail
        }));
    }
    }
     if(this.operationType==Wtf.autoNum.Invoice || this.operationType==Wtf.autoNum.BillingInvoice) {
        if(!WtfGlobal.EnableDisable(this.uPermType, this.recurringPermType)){
            btnArr.push(this.RepeateInvoice=new Wtf.Toolbar.Button({
                text:WtfGlobal.getLocaleText("acc.invoiceList.recInv"),  // "Recurring Invoice",
                tooltip :WtfGlobal.getLocaleText("acc.invoiceList.recInv"),  // "Recurring Invoice",
                scope: this,
                iconCls : getButtonIconCls(Wtf.etype.copy),
                disabled : true,
                handler : this.repeateInvoiceHandler
            }));
        }
    }
}
    if(!WtfGlobal.EnableDisable(this.uPermType, this.exportPermType)){
        btnArr.push(this.exportButton=new Wtf.exportButton({
            obj:this,
            id:"exportReports"+config.helpmodeid+config.id,
            text: WtfGlobal.getLocaleText("acc.common.export"),
            tooltip :WtfGlobal.getLocaleText("acc.common.exportTT"),  //'Export report details',
            disabled :true,
            menuItem:{csv:true,pdf:true,rowPdf:true,rowPdfTitle:WtfGlobal.getLocaleText("acc.rem.39") + " "+ singlePDFtext},
            get:tranType
          }));
       }
     if(!WtfGlobal.EnableDisable(this.uPermType, this.printPermType)){
          btnArr.push(this.printButton=new Wtf.exportButton({
            text:WtfGlobal.getLocaleText("acc.common.print"),  //"Print",
            obj:this,
            tooltip :WtfGlobal.getLocaleText("acc.common.printTT"),  //'Print report details',
            disabled :true,
            label:config.isCustomer?(config.isOrder?"Sales Order":"Invoice"):(config.isOrder?"Purchase Order":"Vendor Invoice"),
            params:{isexpenseinv:this.isexpenseinv,name:config.isQuotation?WtfGlobal.getLocaleText("acc.qnList.tabTitle"):(config.isCustomer?(config.isOrder?WtfGlobal.getLocaleText("acc.soList.tabTitle"):WtfGlobal.getLocaleText("acc.invoiceList.tabtitle")):(config.isOrder?WtfGlobal.getLocaleText("acc.poList.tabTitle"):WtfGlobal.getLocaleText("acc.grList.tabTitle")))},
            menuItem:{print:true}, 
            get:tranType
          }));
     }
    this.quickPanelSearch = new Wtf.KWLTagSearch({
        emptyText:WtfGlobal.getLocaleText("acc.rem.5")+" "+this.label,
        width: 150,
  //      id:"quickSearch"+config.helpmodeid+config.id,
        field: 'billno',
        Store:this.Store
    })

    this.tbar1 = new Array();
    this.tbar1.push(this.quickPanelSearch, this.resetBttn, btnArr);
    if(config.extraFilters == undefined){//Cost Center Report View - Don't show 'cost center' filter
        if(config.isOrder && !this.isQuotation){ // For sales/purchase order show 'cost center' filter in main tbar applied for panel
            this.tbar1.push("-", WtfGlobal.getLocaleText("acc.common.costCenter"), this.costCenter);
        }
    }else if(config.extraFilters != undefined){//Invoice Report View - Add type filter in main tbar
        if(!config.isOrder){this.tbar1.push("&nbsp;View", this.typeEditor);}
    }
    this.tbar1.push("->", getHelpButton(this,config.helpmodeid));

    Wtf.apply(this,{
        border:false,
        layout : "fit",
        tbar: this.tbar1,//this.quickPanelSearch,
//            this.resetBttn,btnArr,'->',(config.isCustomer && !config.isOrder)?'View':'',(!config.isOrder)?this.typeEditor:'',getHelpButton(this,config.helpmodeid)],
        items:[this.grid],
        bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
            pageSize: 30,
            id: "pagingtoolbar" + this.id,
            store: this.Store,
            searchField: this.quickPanelSearch,
            displayInfo: true,
//            displayMsg: 'Displaying records {0} - {1} of {2}',
            emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"),  //"No results to display",
            plugins: this.pP = new Wtf.common.pPageSize({id : "pPageSize_"+this.id})
        })
    });
    this.Store.on('beforeload',function(s,o){
        if(!o.params)o.params={};
        o.params.cashonly=this.cashonly;
        o.params.creditonly=this.creditonly;
        var currentBaseParams = this.Store.baseParams;
        currentBaseParams.deleted=this.deleted;
        currentBaseParams.nondeleted=this.nondeleted;
        currentBaseParams.cashonly= this.cashonly;
        currentBaseParams.creditonly=this.creditonly;
        this.Store.baseParams=currentBaseParams;

    },this);
    this.loadParmStore();
    this.typeEditor.on('select',this.loadTypeStore,this);
    this.expandStore.on('load',this.fillExpanderBody,this);
    this.expander.on("expand",this.onRowexpand,this);
    //this.grid.on('render',this.loadParmStore,this);
    this.Store.on('load',this.expandRow, this);
    this.Store.on('load',this.hideLoading, this);
    this.Store.on('loadexception',this.hideLoading, this);
    Wtf.account.TransactionListPanel.superclass.constructor.call(this,config);
    this.addEvents({
        'journalentry':true
    });
   this.sm.on("selectionchange",this.enableDisableButtons.createDelegate(this),this);
   this.grid.on('cellclick',this.onCellClick, this);
}
Wtf.extend(Wtf.account.TransactionListPanel,Wtf.Panel,{
  hideLoading:function(){  Wtf.MessageBox.hide(); },
    loadTypeStore:function(a,rec){         
        this.cashonly=false;
        this.creditonly=false;
        this.deleted=false;
        this.nondeleted=false;
        var index=rec.data.typeid;
        if(index==1)
            this.cashonly=true;
        else if(index==2)
            this.creditonly=true;
        if(index==4)
            this.nondeleted=true;
        if(index==3){
           this.deleted=true;
           if(!WtfGlobal.EnableDisable(this.uPermType, this.removePermType)){
                if(this.deleteTrans){this.deleteTrans.disable();}
           }
        } else{
            if(!WtfGlobal.EnableDisable(this.uPermType, this.removePermType)){
                if(this.deleteTrans){this.deleteTrans.enable();}
            }
        }
        this.Store.on('load',this.storeloaded,this);
        this.loadStore();
       WtfComMsgBox(29,4,true);

    },
    setCostCenter: function(){
        this.costCenter.setValue(this.costCenterId);//Select Default Cost Center
        Wtf.CostCenterStore.un("load", this.setCostCenter, this);
    },
    enableDisableButtons:function(){
        if(!WtfGlobal.EnableDisable(this.uPermType, this.removePermType)){
            if(this.deleteTrans){this.deleteTrans.enable();}
        }
        var arr=this.grid.getSelectionModel().getSelections();
        if(arr.length==0&&!WtfGlobal.EnableDisable(this.uPermType,this.removePermType)){
            if(this.deleteTrans){this.deleteTrans.disable();}
        }
        if(!WtfGlobal.EnableDisable(this.uPermType, this.removePermType)){
            for(var i=0;i<arr.length;arr++){
                if(arr[i]&&arr[i].data.deleted)
                    if(this.deleteTrans){this.deleteTrans.disable();}
            }
        }

        var rec = this.sm.getSelected();
        if((this.sm.getCount()==1 && rec.data.deleted != true)&&!rec.data.isexpenseinv){ 
            if(this.email && !rec.data.incash)this.email.enable();
            if(this.editBttn)this.editBttn.enable();
            if(this.copyInvBttn)this.copyInvBttn.enable();
        }else{
            if(this.email)this.email.disable();
            if(this.editBttn)this.editBttn.disable();
            if(this.copyInvBttn)this.copyInvBttn.disable();
        }
        if(this.operationType==Wtf.autoNum.Invoice || this.operationType==Wtf.autoNum.GoodsReceipt || this.operationType==Wtf.autoNum.BillingInvoice || this.operationType==Wtf.autoNum.BillingGoodsReceipt) {
            if(this.paymentButton != undefined) {
                if(this.sm.getCount()==1 && rec.data.amountdue!=0 && rec.data.incash != true && rec.data.deleted != true){
                    this.paymentButton.enable();
                } else {
                    this.paymentButton.disable();
                }
            }
        }

        if(this.operationType==Wtf.autoNum.Invoice || this.operationType==Wtf.autoNum.BillingInvoice) {
            if(this.RepeateInvoice != undefined) {
                if(this.sm.getCount()==1 && rec.data.incash != true && rec.data.deleted != true){
                    this.RepeateInvoice.enable();
                } else {
                    this.RepeateInvoice.disable();
                }
            }
        }
    },
    loadParmStore:function(){
        this.typeEditor.setValue(0);
        this.Store.on('load',this.expandRow, this);
        if(this.invID==null)
            this.Store.load({params:{start:0,limit:30}});
        this.Store.on('datachanged', function() {
            if(this.invID==null){
                    var p = this.pP.combo.value;
                    this.quickPanelSearch.setPage(p);
            }
        }, this);
        WtfComMsgBox(29,4,true);
    },
    handleResetClick:function(){
        if(this.quickPanelSearch.getValue()){
            this.quickPanelSearch.reset();
            this.loadStore();
            this.Store.on('load',this.storeloaded,this);
        }
    },
    storeloaded:function(store){
  //      this.hideLoading();
        this.quickPanelSearch.StorageChanged(store);
    },
    viewTransection:function(){
        var formrec=null;
        if(this.grid.getSelectionModel().hasSelection()==false||this.grid.getSelectionModel().getCount()>1){
                WtfComMsgBox(15,2);
                return;
        }
        formrec = this.grid.getSelectionModel().getSelected();
        var incash=formrec.get("incash");
        if(this.isCustomer)
            if(incash &&!this.isCustBill)
                callViewCashReceipt(formrec, 'ViewInvoice');
            else if(incash)
                callViewBillingCashReceipt(formrec,null, 'ViewBillingCSInvoice',true);
            else if(this.isCustBill)
                callViewBillingInvoice(formrec,null, 'ViewBillingInvoice',false);
            else
                callViewInvoice(formrec, 'ViewCashReceipt');
        else
            if(incash &&!this.isCustBill)
                callViewPaymentReceipt(formrec, 'ViewPaymentReceipt');
            else if(incash)
                callViewBillingPaymentReceipt(formrec,null, 'ViewBillingCSInvoice',true);
            else if(this.isCustBill)
                callViewBillingGoodsReceipt(formrec,null, 'ViewBillingInvoice',false);
            else
                callViewGoodsReceipt(formrec, 'ViewGoodsReceipt');
    },
     editTransaction:function(copyInv){

        var formrec=null;
        if(this.grid.getSelectionModel().hasSelection()==false||this.grid.getSelectionModel().getCount()>1){
                WtfComMsgBox(15,2);
                return;
        }
        formrec = this.grid.getSelectionModel().getSelected();
        var label=copyInv?"Copy":"Edit";
        var incash=formrec.get("incash");
        var billid=formrec.get("billid");
        label=label+billid;
        if(this.isCustomer){
            if(this.isCustBill){ 
                if(incash)
                    callEditBillingSalesReceipt(formrec, label+'BillingCSInvoice',copyInv);
                else
                    callEditBillingInvoice(formrec, label+'BillingInvoice',copyInv);
            }
            else{
                if(incash)
                    callEditCashReceipt(formrec, label+'CashSales',copyInv);
                else
                    callEditInvoice(formrec, label+'Invoice',copyInv);
           }                
        }else{
            if(this.isCustBill){
                if(incash)
                    callEditBillingPurchaseReceipt(formrec, label+'BillingCSInvoice',copyInv);
                else
                    callEditBillingGoodsReceipt(formrec,  label+'BillingInvoice',copyInv);
            }
            else{
                if(incash)
                    callEdiCashPurchase(formrec, label+'PaymentReceipt',copyInv);
                else
                    callEditGoodsReceipt(formrec, label+'GoodsReceipt',copyInv);
            }
        }
    },
    
    editOrderTransaction:function(){			// Editing Sales and Purchase Order with Inventory and Without Inventory
    	var formRecord = null;
    	if(this.grid.getSelectionModel().hasSelection()==false||this.grid.getSelectionModel().getCount()>1){
            WtfComMsgBox(15,2);
            return;
    	}
    	formRecord = this.grid.getSelectionModel().getSelected();
    	var billid=formRecord.get("billid");
    	if(!this.isCustomer){													
    		if(!this.isCustBill){				// Without Inventory
    			callEditPurchaseOrder(true,formRecord,billid);
    		}else{								// With Inventory
    			callBillingPurchaseOrder(true,formRecord,billid);
    		}
    	}
    	else{
    		if(!this.isCustBill){				// Without Inventory
    			callSalesOrder(true,formRecord,billid);
    		}else{								// With Inventory
    			callBillingSalesOrder(true,formRecord,billid);
    		}
    	}
    },
    
    sendMail:function(){
        var formrec=null;
        if(this.grid.getSelectionModel().hasSelection()==false||this.grid.getSelectionModel().getCount()>1){
                WtfComMsgBox(15,2);
                return;
        }
        formrec = this.grid.getSelectionModel().getSelected();   
        var incash=formrec.get("incash");
        if(this.isQuotation){
        	callEmailWin("emailwin",formrec,this.label,50,false,true);
        }else{
        if(this.isCustomer){
            if(this.isCustBill){
//                if(incash)
//                     callEmailWin("editwin",formrec,this.label,13);
//                else
                    callEmailWin("emailwin",formrec,this.label,11,true);
            }
            else{
//                if(incash)
//                    callEmailWin("editwin",formrec,this.label,2);
//                else
                    callEmailWin("emailwin",formrec,this.label,2,true);
           }
        }else{
            if(this.isCustBill){
//                if(incash)
//                     callEmailWin("editwin",formrec,this.label,11);
//                else
                   callEmailWin("emailwin",formrec,this.label,15,true);
            }
            else{
//                if(incash)
//                    callEmailWin("editwin",formrec,this.label,15);
//                else
                   callEmailWin("emailwin",formrec,this.label,6,true);
            }
        }
        }
       
    },
    repeateInvoiceHandler:function(){
        var formrec=null;
        if(this.grid.getSelectionModel().hasSelection()==false||this.grid.getSelectionModel().getCount()>1){
            WtfComMsgBox(15,2);
            return;
        }
        formrec = this.grid.getSelectionModel().getSelected();
        if(this.isCustomer){
            callRepeatedInvoicesWindow(this.isCustBill, formrec);
        }
    },
    onRowexpand:function(scope, record, body){
        this.expanderBody=body;
        this.isexpenseinv=!this.isCustomer&&record.data.isexpenseinv;
        this.expandStore.load({params:{bills:record.data.billid,isexpenseinv:(!this.isCustomer&&record.data.isexpenseinv)}});
    },
    fillExpanderBody:function(){
        var disHtml = "";
        var arr=[];
        if(this.isexpenseinv){//for vendor expense invoice[PS]
//        arr=['Account Name' ,'Amount','Discount','Tax Percent','Total Amount','                  '];//(this.isCustBill?'':'Remark'),
        arr=[WtfGlobal.getLocaleText("acc.invoiceList.accName") ,WtfGlobal.getLocaleText("acc.invoiceList.expand.amt"),WtfGlobal.getLocaleText("acc.invoiceList.expand.dsc"),WtfGlobal.getLocaleText("acc.invoiceList.expand.tax"),WtfGlobal.getLocaleText("acc.invoiceList.totAmt"),'                  '];
        var header = "<span class='gridHeader'>"+WtfGlobal.getLocaleText("acc.invoiceList.expand.accList")+"</span>";   //Account List
        header += "<span class='gridNo' style='font-weight:bold;'>S.No.</span>";
        for(var i=0;i<arr.length;i++){
            header += "<span class='headerRow'>" + arr[i] + "</span>";
        }
        header += "<span class='gridLine'></span>";
        for(i=0;i<this.expandStore.getCount();i++){
            var rec=this.expandStore.getAt(i);
            var accountname= rec.data['accountname'];
            header += "<span class='gridNo'>"+(i+1)+".</span>";
            header += "<span class='gridRow'  wtf:qtip='"+accountname+"'>"+Wtf.util.Format.ellipsis(accountname,15)+"</span>";
            header += "<span class='gridRow'>"+WtfGlobal.addCurrencySymbolOnly(rec.data.rate,rec.data['currencysymbol'],[true])+"</span>";
            header += "<span class='gridRow'>"+rec.data.prdiscount+"% "+"&nbsp;</span>";
            header += "<span class='gridRow'>"+rec.data.prtaxpercent+"% "+"&nbsp;</span>";
            var amount=rec.data.rate-(rec.data.rate*rec.data.prdiscount)/100;
            amount+=(amount*rec.data.prtaxpercent/100);
            header += "<span class='gridRow'>"+WtfGlobal.addCurrencySymbolOnly(amount,rec.data['currencysymbol'],[true])+"</span>";
            header +="<br>";
        }
        disHtml += "<div class='expanderContainer' style='width:100%'>" + header + "</div>";
        }else{
//            arr=[(this.isCustBill?'':'Product ID'),(this.isCustBill?'Product Details':'Product Name' ),(this.isCustBill?'':'Product Type'),'Quantity','Unit Price',(this.isOrder||this.isQuotation)?'':'Discount','Tax Percent','Amount',"                  "];//(this.isCustBill?'':'Remark'),
        	arr=[(this.isCustBill?'':WtfGlobal.getLocaleText("acc.invoiceList.expand.PID")),(this.isCustBill?WtfGlobal.getLocaleText("acc.invoiceList.expand.pDetails"):WtfGlobal.getLocaleText("acc.invoiceList.expand.pName")),(this.isCustBill?'':WtfGlobal.getLocaleText("acc.invoiceList.expand.pType")),WtfGlobal.getLocaleText("acc.invoiceList.expand.qty"),WtfGlobal.getLocaleText("acc.invoiceList.expand.unitPrice"),(this.isOrder && !this.isQuotation)?'':WtfGlobal.getLocaleText("acc.invoiceList.expand.dsc"),WtfGlobal.getLocaleText("acc.invoiceList.expand.tax"),WtfGlobal.getLocaleText("acc.invoiceList.expand.amt"),"                  "];
        	header = "<span class='gridHeader'>"+WtfGlobal.getLocaleText("acc.invoiceList.expand.pList")+"</span>";   //Product List
            header += "<span class='gridNo' style='font-weight:bold;'>"+WtfGlobal.getLocaleText("acc.cnList.Sno")+"</span>";
            for(i=0;i<arr.length;i++){
                header += "<span class='headerRow'>" + arr[i] + "</span>";
            }
            header += "<span class='gridLine'></span>";
            for(i=0;i<this.expandStore.getCount();i++){
                rec=this.expandStore.getAt(i);
                var productname=this.isCustBill?rec.data['productdetail']: rec.data['productname'];
                header += "<span class='gridNo'>"+(i+1)+".</span>";
                if(!this.isCustBill)
                    header += "<span class='gridRow'>"+rec.data['pid']+"&nbsp;</span>";
                header += "<span class='gridRow'  wtf:qtip='"+productname+"'>"+Wtf.util.Format.ellipsis(productname,15)+"</span>";
                if(!this.isCustBill)
                    header += "<span class='gridRow'>"+rec.data['type']+"</span>";

                header += "<span class='gridRow'>"+rec.data['quantity']+" "+rec.data['unitname']+"</span>";
                var rate=this.isQuotation||this.isOrder&&!this.isCustBill?rec.data.orderrate:rec.data.rate;
                header += "<span class='gridRow'>"+WtfGlobal.addCurrencySymbolOnly(rate,rec.data['currencysymbol'],[true])+"</span>";
                if(!this.isOrder)
                    header += "<span class='gridRow'>"+rec.data['prdiscount']+"% "+"&nbsp;</span>";
                header += "<span class='gridRow'>"+rec.data['prtaxpercent']+"% "+"&nbsp;</span>";
                amount=0;
                if(this.isOrder && !this.isQuotation){
                    amount=rec.data['quantity']*rate;
                    amount+=(amount*rec.data['prtaxpercent']/100);
                }else{
                    amount=rec.data['quantity']*rate-(rec.data['quantity']*rate*rec.data['prdiscount'])/100;
                    amount+=(amount*rec.data['prtaxpercent']/100);
                }
                    header += "<span class='gridRow'>"+WtfGlobal.addCurrencySymbolOnly(amount,rec.data['currencysymbol'],[true])+"</span>";
               if(!this.isCustBill)
                    header += "<span class='gridRow'>"+rec.data['productmoved']+"</span>";
    //           if(!this.isCustBill)
    //                header += "<span class='gridRow'>"+rec.data['desc']+"&nbsp;</span>";
               header +="<br>";
        }
        disHtml += "<div class='expanderContainer' style='width:100%'>" + header + "</div>";}
        this.expanderBody.innerHTML = disHtml;
    }, 
    onCellClick:function(g,i,j,e){
        e.stopEvent();
        var el=e.getTarget("a");
        if(el==null)return;
        var header=g.getColumnModel().getDataIndex(j);
        if(header=="entryno"){
            var accid=this.Store.getAt(i).data['journalentryid'];
            this.fireEvent('journalentry',accid,true);
        }
        if(header=="billno"){
            this.viewTransection(g,i,e)
        }
    },
    expandInvoice:function(id,exponly){
        this.invID=id
        if(exponly){
            this.pagingToolbar.hide();
            this.exponly=exponly;
            this.Store.load();
            this.Store.on('load',this.expandRow, this);
        }
    }, 
    expandRow:function(){
       
        if(this.Store.getCount()==0){
            if(this.exportButton)this.exportButton.disable();
            if(this.printButton)this.printButton.disable();
            var selTypeVal = this.typeEditor.getValue();
            var emptyTxt = "";
            if(selTypeVal == 3) {//deleted
                emptyTxt = this.deletedRecordsEmptyTxt;
            } else if(selTypeVal == 0 || selTypeVal == 4) {//All or Exclude deleted
                emptyTxt = this.emptytext1+(this.isOrder?"":"<br>"+this.emptytext2);
            } else if(selTypeVal == 1) {//Cash Sales
                emptyTxt = this.isOrder?"":"<br>"+this.emptytext2;
            } else if(selTypeVal == 2) {//Invoice
                emptyTxt = this.emptytext1;
            } 
            if(this.isQuotation){
            	emptyTxt = this.emptytext3;
            }
            this.grid.getView().emptyText=emptyTxt;
            this.grid.getView().refresh();
        }else{
            if(this.exportButton)this.exportButton.enable();
            if(this.printButton)this.printButton.enable();
        }
        this.Store.filter('billid',this.invID);
        if(this.exponly)
            this.expander.toggleRow(0);
     },
    loadStore:function(){
       this.Store.load({
           params : {
               start : 0,
               limit : this.pP.combo.value,
               ss : this.quickPanelSearch.getValue()
           }
       });
       this.Store.on('load',this.storeloaded,this);
    },
    handleDelete:function(){
        if(this.grid.getSelectionModel().hasSelection()==false){
            WtfComMsgBox(34,2);
            return;
        }
        var data=[];
        var arr=[];
        this.recArr = this.grid.getSelectionModel().getSelections();
        this.grid.getSelectionModel().clearSelections();
        WtfGlobal.highLightRowColor(this.grid,this.recArr,true,0,2);
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.rem.146")+" "+this.label+"?",function(btn){
        if(btn!="yes") { 
            for(var i=0;i<this.recArr.length;i++){
                var ind=this.Store.indexOf(this.recArr[i])
                var num= ind%2;
                WtfGlobal.highLightRowColor(this.grid,this.recArr[i],false,num,2,true);
            }
            return; 
        }
        for(i=0;i<this.recArr.length;i++){ 
                arr.push(this.Store.indexOf(this.recArr[i])); 
        }
        var mode=(this.isCustBill?23:15);
        if(this.isOrder){
            mode=(this.isCustBill?54:44);
        }
        data= WtfGlobal.getJSONArray(this.grid,true,arr);
        this.ajxUrl = "";
        //this.isCustBill?23:15
        if(this.businessPerson=="Customer"){
            this.ajxUrl = "ACCInvoiceCMN/"+(this.isCustBill?"deleteBillingInvoices":"deleteInvoice")+".do";
            if(this.isOrder){
                this.ajxUrl = this.isCustBill?"ACCSalesOrder/deleteBillingSalesOrders.do":"ACCSalesOrder/deleteSalesOrders.do"
            }
        }else if((this.businessPerson=="Vendor")){
            this.ajxUrl = "ACCGoodsReceiptCMN/"+(this.isCustBill?"deleteBillingGoodsReceipt":"deleteGoodsReceipt")+".do";
            if(this.isOrder){
                this.ajxUrl = this.isCustBill?"ACCPurchaseOrder/deleteBillingPurchaseOrders.do":"ACCPurchaseOrder/deletePurchaseOrders.do"
            }
        }
        if(this.isQuotation){
        	this.ajxUrl = "ACCSalesOrder/deleteQuotations.do";
        }
            Wtf.Ajax.requestEx({
                url:this.ajxUrl,
//                url: Wtf.req.account+this.businessPerson+'Manager.jsp',
                params:{
                   data:data,
                    mode:mode
                }
            },this,this.genSuccessResponse,this.genFailureResponse);
        },this);
    },
    genSuccessResponse:function(response){
         WtfComMsgBox([this.label,response.msg],response.success*2+1);
        for(var i=0;i<this.recArr.length;i++){
             var ind=this.Store.indexOf(this.recArr[i])
             var num= ind%2;
             WtfGlobal.highLightRowColor(this.grid,this.recArr[i],false,num,2,true);
        }
        if(response.success){
        (function(){
        this.loadStore();
        }).defer(WtfGlobal.gridReloadDelay(),this);
        Wtf.productStore.reload();
        Wtf.productStoreSales.reload();
        }
    },
    genFailureResponse:function(response){
         for(var i=0;i<this.recArr.length;i++){
             var ind=this.Store.indexOf(this.recArr[i])
             var num= ind%2;
             WtfGlobal.highLightRowColor(this.grid,this.recArr[i],false,num,2,true);
        }
        var msg=WtfGlobal.getLocaleText("acc.common.msg1");  //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    },
    makePayment:function(){
        if(this.sm.getCount()==1){
            var invoiceRecord = this.sm.getSelected();
            if(this.operationType==Wtf.autoNum.Invoice) {
                callReceipt(true, invoiceRecord);
            } else if(this.operationType==Wtf.autoNum.BillingInvoice) {
                callBillingReceipt(true, invoiceRecord);
            } else if(this.operationType==Wtf.autoNum.GoodsReceipt) {
                callPayment(true, invoiceRecord);
            } else if(this.operationType==Wtf.autoNum.BillingGoodsReceipt) {
                callBillingPayment(true, invoiceRecord);
            }
        }
    },
    getTransName:function(type){
       switch(type){
           case Wtf.autoNum.SalesOrder:return "Sales Order";
           case Wtf.autoNum.Invoice:return "Invoice";
           case Wtf.autoNum.PurchaseOrder:return "Purchase Order";
           case Wtf.autoNum.GoodsReceipt:return "Vendor Invoice";
           case Wtf.autoNum.BillingSalesOrder:return "Sales Order";
           case Wtf.autoNum.BillingInvoice:return "Invoice";
           case Wtf.autoNum.BillingPurchaseOrder:return "Purchase Order";
           case Wtf.autoNum.BillingGoodsReceipt:return "Vendor Invoice";
           case Wtf.autoNum.Quotation:return "Quotation";
       }
    }
});
