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
Wtf.namespace("Wtf","Wtf.common","Wtf.account","Wtf.reportBuilder");
Wtf.req = {
    base: "../../jspfiles/",
    json: "../../json/",
    account: "../../jspfiles/"
};
/* Product Type Master
+--------------------------------------+--------------------+
| id                                   | name               |
+--------------------------------------+--------------------+
| e4611696-515c-102d-8de6-001cc0794cfa | Inventory Assembly |
| d8a50d12-515c-102d-8de6-001cc0794cfa | Inventory Part     |
| f071cf84-515c-102d-8de6-001cc0794cfa | Non-Inventory Part |
| 4efb0286-5627-102d-8de6-001cc0794cfa | Service            |
+--------------------------------------+--------------------+
 * */

Wtf.producttype = {
    assembly: "e4611696-515c-102d-8de6-001cc0794cfa",
    invpart: "d8a50d12-515c-102d-8de6-001cc0794cfa",
    noninvpart: "f071cf84-515c-102d-8de6-001cc0794cfa",
    service: "4efb0286-5627-102d-8de6-001cc0794cfa",
    inventoryNonSale: "ff8080812f5c78bb012f5cfe7edb000c9cfa"	
};

Wtf.dirtyStore = {
    title: false,
    customerCategory: false,
    vendorCategory: false,
    assetCategory: false,
    inventory: false,
    product: false
};



Wtf.TAB_TITLE_LENGTH = 19;
Wtf.BLANK_IMAGE_URL = "../../lib/resources/images/default/s.gif";
Wtf.DEFAULT_USER_URL = "../../images/defaultuser.png";
Wtf.ValidateMailPatt = /^([a-zA-Z0-9_\-\.+]+)@(([0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.)|(([a-zA-Z0-9\-]+\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})$/;
Wtf.ValidateUserid = /^\w+$/;
Wtf.ValidateUserName = /^[\w\s\'\"\.\-]+$/;
Wtf.validateHeadTitle = /^[\w\s\'\"\.\-\,\~\!\@\$\^\*\(\)\{\}\[\])]+$/;
Wtf.DomainPatt = /[ab]\/([^\/]*)\/(.*)/;
Wtf.PhoneRegex= /^([^-])(\(?\+?[0-9]*\)?)?[\.0-9_\- \(\)]*$/;
Wtf.specialCharacters = /[\[\\\^\$\.\|\?\*\+\(\)\{\}]/g;
Wtf.specialChar = "";  ///^[a-z A-Z 0-9 ]{1,50}$/;
Wtf.etype = {
    user: 0,
    comm: 1,
    proj: 2,
    home: 3,
    docs: 4,
    cal: 5,
    forum: 6,
    pmessage: 7,
    pplan: 8,
    adminpanel: 9,
    todo: 10,
    search: 11,
    deskera:12,
    exportfile:13,
    exportcsv:14,
    exportpdf:15,
    save:16,
    resetbutton:17,
    add:18,
    edit:19,
    deletebutton:20,
    customer:21,
    audittrail:22,
    permission:23,
    deletegridrow:24,
    menuadd:25,
    menuedit:26,
    menudelete:27,
    addproduct:28,
    buildassemly:29,
    inventoryval:30,
    cyclecount:31,
    addcyclecount:32,
    approvecyclecount:33,
    countcyclecount:34,
    cyclecountreport:35,
    reorderreport:36,
    ratioreport:37,
    addcyclecounttab:38,
    approvecyclecounttab:39,
    countcyclecounttab:40,
    cyclecountreporttab:41,
    salesbyitem:42,
    salesbyitemsummary:43,
    salesbyitemdetil:44,
    copy:45,
    sync:46,
    menuclone:47
};

Wtf.autoNum={
    JournalEntry:0,
    SalesOrder:1,
    Invoice:2,
    CreditNote:3,
    Receipt:4,
    PurchaseOrder:5,
    GoodsReceipt:6,
    DebitNote:7,
    Payment:8,
    CashSale:9,
    CashPurchase:10,
    BillingInvoice:11,
    BillingReceipt:12,
    BillingCashSale:13,
    BillingCashPurchase:14,
    BillingGoodsReceipt:15,
    BillingPayment:16,
    BillingSalesOrder:17,
    BillingPurchaseOrder:18,
    BillingCreditNote:19,
    BillingDebitNote:20,
    AgedPayableWithInv:21,
    AgedPayableWithOutInv:22,
    VendorAgedPayable:23,
    ExportInvoices:24,//Aged Receivable with inventory
    getBillingInvoices:25,//Aged Receivable with inventory
    CustomerAgedReceivable:26,
    BalanceSheet:27,
    TradingPnl:28,
    RatioAnalysis:29,
    Quotation:50
};

Wtf.account.nature={
    Liability:0,
    Asset:1,
    Expences:2,
    Income:3
};
var bHasChanged = false;
Wtf.Perm = {};
Wtf.UPerm = {};
this.countryRec = new Wtf.data.Record.create([
    {name: 'id'},
    {name: 'name'}
]);
this.timezoneRec = new Wtf.data.Record.create([
    {name: 'id'},
    {name: 'name'}
]);

Wtf.currencyRec = new Wtf.data.Record.create([
    {name: 'currencyid',mapping:'tocurrencyid'},
    {name: 'symbol'},
    {name: 'currencyname',mapping:'tocurrency'},
    {name: 'exchangerate'},
    {name: 'htmlcode'}
]);
Wtf.countryStore = new Wtf.data.Store({
//    url:Wtf.req.base+"UserManager.jsp",
    url : "kwlCommonTables/getAllCountries.do",
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    },this.countryRec),
    baseParams:{
        mode:20,
        common:'1'
    }
});
Wtf.currencyStore = new Wtf.data.Store({
//    url:Wtf.req.base+"CompanyManager.jsp",
    url:"ACCCurrency/getCurrencyExchange.do",
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    },Wtf.currencyRec),
    baseParams:{
        mode:201,
        common:'1'
    },
    autoLoad:false
});
Wtf.timezoneStore = new Wtf.data.Store({
//    url:Wtf.req.base+"UserManager.jsp",
    url:"kwlCommonTables/getAllTimeZones.do",
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    },this.timezoneRec),
    baseParams:{
        mode:16,
        common:'1'
    },
    autoLoad:false
});

Wtf.personRec = new Wtf.data.Record.create ([
        {name:'accid'},
        {name:'accname'},
//        {name: 'level'},
        {name: 'termdays'},
        {name: 'billto'},
        {name: 'currencysymbol'},
        {name: 'currencyname'},
        {name: 'currencyid'},
        {name:'deleted'}
]);
Wtf.customerAccStore =  new Wtf.data.Store({
//    url:Wtf.req.account+'CustomerManager.jsp',
    url:"ACCCustomer/getCustomersForCombo.do",
    baseParams:{
         mode:2,
         group:10,
         deleted:false,
         nondeleted:true,
        common:'1'
    },
    reader: new  Wtf.data.KwlJsonReader({
        root: "data",
        autoLoad:false
    },Wtf.personRec)
});

Wtf.vendorAccStore =  new Wtf.data.Store({
//    url:Wtf.req.account+'VendorManager.jsp',
    url:"ACCVendor/getVendorsForCombo.do",
    baseParams:{
         mode:2,
         group:13,
         deleted:false,
         nondeleted:true,
        common:'1'
    },
    reader: new  Wtf.data.KwlJsonReader({
        root: "data",
        autoLoad:false
    },Wtf.personRec)
});

Wtf.uomRec = Wtf.data.Record.create ([
    {name:'uomid'},
    {name:'uomname'},
    {name: 'precision'}
]);
Wtf.uomStore=new Wtf.data.Store({
    url: "ACCUoM/getUnitOfMeasure.do",
    baseParams:{
        mode:31,
        common:'1'
    },
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    },Wtf.uomRec)
});

Wtf.productTypeRec = Wtf.data.Record.create ([
    {name: 'id'},
    {name: 'name'}
]);
Wtf.productTypeStore=new Wtf.data.Store({
    url: "ACCProduct/getProductTypes.do",
    baseParams:{
        mode:24,
        common:'1'
    },
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    },Wtf.productTypeRec)
});

Wtf.salesAccRec = Wtf.data.Record.create ([
    {name: 'accid'},
    {name: 'accname'}
]);
Wtf.salesAccStore=new Wtf.data.Store({
    url:"ACCAccount/getAccountsForCombo.do",
    baseParams:{
         mode:2,
         nature:[Wtf.account.nature.Income],
         common:'1',
         nondeleted:true
     },
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    },Wtf.salesAccRec)
});

Wtf.termRec = new Wtf.data.Record.create([
        {name: 'termid'},
        {name: 'termname'},
        {name: 'termdays'}
]);

Wtf.termds = new Wtf.data.Store({
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    },Wtf.termRec),
//    url: Wtf.req.account + 'CompanyManager.jsp',
    url : "ACCTerm/getTerm.do",
    baseParams:{
        mode:91,
        common:'1'
    }
 });

Wtf.GridRecTitle = Wtf.data.Record.create ([
    {name:'id'},
    {name:'name'}
]);

Wtf.TitleStore=new Wtf.data.Store({
//   url:Wtf.req.account+'CompanyManager.jsp',
   url:"ACCMaster/getMasterItems.do",
    baseParams:{
        mode:112,
        groupid:6,
        common:'1'
     },
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    },Wtf.GridRecTitle)
});
Wtf.custCategoryRec = Wtf.data.Record.create ([
    {name:'id'},
    {name:'name'}
]);

Wtf.CustomerCategoryStore=new Wtf.data.Store({
   url:"ACCMaster/getMasterItems.do",
    baseParams:{
        mode:112,
        groupid:7,
        common:'1'
     },
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    },Wtf.custCategoryRec)
});

Wtf.vedCategoryRec = Wtf.data.Record.create ([
    {name:'id'},
    {name:'name'}
]);
Wtf.VendorCategoryStore=new Wtf.data.Store({
   url:"ACCMaster/getMasterItems.do",
    baseParams:{
        mode:112,
        groupid:8,
        common:'1'
     },
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    },Wtf.vedCategoryRec)
});

Wtf.assetCategoryRec = Wtf.data.Record.create ([
    {name:'id'},
    {name:'name'}
]);
Wtf.AssetCategoryStore=new Wtf.data.Store({
   url:"ACCMaster/getMasterItems.do",
    baseParams:{
        mode:112,
        groupid:9,
        common:'1'
     },
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    },Wtf.assetCategoryRec)
});
Wtf.taxRec = new Wtf.data.Record.create([
   {name: 'taxid'},
   {name: 'taxname'},
   {name: 'percent',type:'float'},
   {name: 'taxcode'},
   {name: 'accountid'},
   {name: 'accountname'},
   {name: 'applydate', type:'date'}

]);

Wtf.taxStore = new Wtf.data.Store({
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    },Wtf.taxRec),
//    url: Wtf.req.account + 'CompanyManager.jsp',
    url : "ACCTax/getTax.do",
    baseParams:{
        mode:33,
        common:'1'
    }
});

Wtf.CostCenterRec = Wtf.data.Record.create ([
    {name: 'id'},
    {name: 'ccid'},
    {name: 'name'},
    {name: 'description'}
]);
Wtf.CostCenterStore=new Wtf.data.Store({
    url: "CostCenter/getCostCenter.do?forCombo=report",
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    },Wtf.CostCenterRec)
});
Wtf.FormCostCenterStore=new Wtf.data.Store({
    url: "CostCenter/getCostCenter.do?forCombo=form",
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    },Wtf.CostCenterRec),
    baseParams:{
        common:'1'
    }
});
Wtf.productRec = Wtf.data.Record.create ([
    {name:'productid'},
    {name:'productname'},
    {name:'desc'},
    {name:'uomid'},
    {name:'uomname'},
    {name:'parentid'},
    {name:'parentname'},
    {name:'purchaseaccountid'},
    {name:'salesaccountid'},
    {name:'purchaseretaccountid'},
    {name:'salesretaccountid'},
    {name:'reorderquantity'},
    {name:'quantity'},
    {name:'reorderlevel'},
    {name:'leadtime'},
    {name:'purchaseprice'},
    {name:'saleprice'},
    {name: 'leaf'},
    {name: 'currencysymbol'},
    {name: 'currencyrate'},
    {name: 'producttype'},
    {name: 'type'},
    {name: 'syncable'},
    {name:'initialsalesprice'},
    {name: 'initialquantity',mapping:'initialquantity'},
    {name: 'initialprice'},
    {name: 'ccountinterval'},
    {name: 'ccounttolerance'},
    {name: 'vendor'},
    {name: 'pid'},
    {name: 'level'}
]);
Wtf.productStore = new Wtf.data.Store({
//    url:Wtf.req.account+'CompanyManager.jsp',
    url:"ACCProduct/getProducts.do",
    baseParams:{mode:22,common:'1'},
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    },Wtf.productRec)
});
Wtf.productStoreSales = new Wtf.data.Store({
        url:"ACCProduct/getProducts.do",
        baseParams:{
        	loadInventory:true
            },
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        },Wtf.productRec)
    });


function loadGlobalStores(){
    
	Wtf.delTypeStore = new Wtf.data.SimpleStore({
        fields: [{name:'typeid',type:'int'}, 'name'],
        data :[[0,WtfGlobal.getLocaleText("acc.rem.105")],[1,WtfGlobal.getLocaleText("acc.rem.106")],[2,WtfGlobal.getLocaleText("acc.rem.107")]]
    });

	Wtf.intervalTypeStore = new Wtf.data.SimpleStore({
	    fields: ["id", "name"], //Dont't Change id field values [SK]
	    data :[["day",WtfGlobal.getLocaleText("acc.rem.108")],["week",WtfGlobal.getLocaleText("acc.rem.109")],["month",WtfGlobal.getLocaleText("acc.fixedAssetList.month")]]
	});
	
}


Wtf.grid.CheckColumn = function(config){
    Wtf.apply(this, config);
    if(!this.id)
        this.id = Wtf.id();
    this.renderer = this.renderer.createDelegate(this);
};
Wtf.grid.CheckColumn.prototype ={
    fyear:0,
    byear:0,
    fdate:0,
    bdate:0,
    init : function(grid){
        this.grid = grid;
        this.grid.on('render', function(){
            var view = this.grid.getView();
            view.mainBody.on('mousedown', this.onMouseDown, this);
        }, this);
    },
    onMouseDown : function(e, t){
        if(t.className && t.className.indexOf('x-grid3-cc-'+this.id) != -1){
            e.stopEvent();
            var index = this.grid.getView().findRowIndex(t);
            var record = this.grid.store.getAt(index);
            record.set(this.dataIndex, !record.data[this.dataIndex]);
            this.grid.fireEvent("afteredit",{
                grid:this.grid,
                record:record,
                field:this.dataIndex,
                value:!record.data[this.dataIndex],
                originalValue:record.data[this.dataIndex],
                row:index,
                column:0//Not known
            });
        }
    },
    renderer : function(v, p, record){
        p.css += ' x-grid3-check-col-td';
        return '<div class="x-grid3-check-col'+(v?'-on':'')+' x-grid3-cc-'+this.id+'">&#160;</div>';
    }
};

function getTopHtml(text, body,img,isgrid,margin){
    if(isgrid===undefined)isgrid=false;
    if(margin===undefined)margin='15px 0px 10px 10px';
     if(img===undefined||img==null) {
        img = '../../images/createuser.png';
    }
     var str =  "<div style = 'width:100%;height:100%;position:relative;float:left;'>"
                    +"<div style='float:left;height:100%;width:auto;position:relative;'>"
                    +"<img src = "+img+"  class = 'adminWinImg'></img>"
                    +"</div>"
                    +"<div style='float:left;height:100%;width:80%;position:relative;'>"
                    +"<div style='font-size:12px;font-style:bold;float:left;margin:15px 0px 0px 10px;width:100%;position:relative;'><b>"+text+"</b></div>"
                    +"<div style='font-size:10px;float:left;margin:15px 0px 10px 10px;width:100%;position:relative;'>"+body+"</div>"
                        +(isgrid?"":"<div class='medatory-msg'>"+WtfGlobal.getLocaleText("acc.changePass.reqFields")+"</div>")
                        +"</div>"
                    +"</div>" ;
     return str;
}

function deleteHoliday(obj, admin){
    Wtf.MessageBox.confirm('Confirm', 'Are you sure you would like to delete the holiday?', function(btn){
        if(btn == "yes")
            Wtf.getCmp(admin).deleteHoliday(obj.id.substring(4));
        },
    this);
}

function cancelHoliday(){
    Wtf.get("addHoliday").dom.style.display = 'none';
}
function addHoliday(admin){
    Wtf.getCmp(admin).addHoliday();
}

function showChart(id1,dataflag,swf,xmlpath,persongroup,isagedgraph,withinventory,nondeleted,deleted,year){
	var comp  =Wtf.getCmp(id1);
	if(comp){
		if(comp.rendered){				// Send year field selected in the year combo      Neeraj
            var pid = comp.body.dom.id;
            var personlimit=1;
//            var data= "ACCChart/getTopCustomerChart.do";//&personlimit="+personlimit+"&creditonly=true&persongroup="+persongroup+"&isagedgraph="+isagedgraph+"&withinventory="+withinventory;
            var data= dataflag+".do?personlimit="+personlimit+"&creditonly=true&persongroup="+persongroup+"&isagedgraph="+isagedgraph+"&withinventory="+withinventory+"&nondeleted="+nondeleted+"&deleted="+deleted+"&year="+year;
            createNewChart(swf,'krwpie', '100%', '100%', '8', '#FFFFFF', xmlpath,data, pid);			
		}else{
	        comp.on("render", function(){		 
	            var pid = Wtf.getCmp(id1).body.dom.id;
	            var personlimit=1;
	//            var data= "ACCChart/getTopCustomerChart.do";//&personlimit="+personlimit+"&creditonly=true&persongroup="+persongroup+"&isagedgraph="+isagedgraph+"&withinventory="+withinventory;
	            var data= dataflag+".do?personlimit="+personlimit+"&creditonly=true&persongroup="+persongroup+"&isagedgraph="+isagedgraph+"&withinventory="+withinventory+"&nondeleted="+nondeleted+"&deleted="+deleted;
	            createNewChart(swf,'krwpie', '100%', '100%', '8', '#FFFFFF', xmlpath,data, pid);
	        }, this);
		}
    }
}
function globalChart(id,id1,swf,dataflag,mainid,xmlpath,withinventory){
    var reportPanel =Wtf.getCmp(id);
    if(reportPanel==null){
        reportPanel = new Wtf.Panel({
            id: id,
            border : false,
            title : WtfGlobal.getLocaleText("acc.rem.166"),  //"Chart View",
            autoScroll:true,
            layout:'border',
            closable: true,
            style:'padding:50px',
            defaults:{border:false},
            iconCls:(Wtf.isChrome?'accountingbase chartChrome':'accountingbase chart'),
            items:[new Wtf.Panel({
                id:"msgid",
                region:"south",
                width:10,
                baseCls:"chartmsg",
                html:"Note: Amount in <span class='currency-view'>"+WtfGlobal.getCurrencySymbol()+"</span>",
                border : false,
                frame:false
            }),new Wtf.Panel({
                id:id1,
                region:"center",
                defaults:{border:false},
                border : false,
                frame:false
            })]
        });
        showChart(id1,dataflag,swf,xmlpath,false,false,withinventory);
        Wtf.getCmp(mainid).add(reportPanel);
    }
    Wtf.getCmp(mainid).setActiveTab(reportPanel);
    Wtf.getCmp(mainid).doLayout();
}

function getBookBeginningYear(){ 
	var cfYear=new Date(Wtf.account.companyAccountPref.fyfrom)
    var ffyear=new Date(Wtf.account.companyAccountPref.firstfyfrom)            
    ffyear=new Date( ffyear.getFullYear(),cfYear.getMonth(),cfYear.getDate()).clearTime()
    
    var data=[];
    var newrec;
    if(ffyear==null||ffyear=="NaN"){ ffyear=new Date(Wtf.account.companyAccountPref.fyfrom)}
    var year=ffyear.getFullYear();
    data.push([0,year]) 
    if(!(ffyear.getMonth()==0&&ffyear.getDate()==1)){
        data.push([1,year+1]);
        newrec = new Wtf.data.Record({id:1,yearid:year+1});
    }
    return data;
}

function globalAgedChart(id,id1,swf1,dataflag1,mainid,xmlpath1,id2,swf2,dataflag2,xmlpath2,withinventory,nondeleted,deleted){

	var yearComboData = getBookBeginningYear();

	var yearStore= new Wtf.data.SimpleStore({
		fields: [{name:'id',type:'int'}, 'yearid'],
		data:yearComboData
	});

	var reportPanel =Wtf.getCmp(id);
    if(reportPanel==null){
    	var year = new Wtf.form.ComboBox({
    	    store: yearStore,
    	    fieldLabel:'Year',
    	    name:'yearid',
    	    displayField:'yearid',
    	    anchor:'40%',
//    	    valueField:'yearid',
    	    forceSelection: true,
    	    mode: 'local',
    	    triggerAction: 'all',
    	    selectOnFocus:true,
    	    emptyText: "Select a Year......."
    	});
    	year.on('select',function(c){
    		showChart(id1,dataflag1,swf1,xmlpath1,false,true,withinventory,nondeleted,deleted,c.getValue());
    	    showChart(id2,dataflag2,swf2,xmlpath2,true,true,withinventory,nondeleted,deleted,c.getValue());
    	});
        reportPanel = new Wtf.Panel({
            id: id,
            border : false,
            title : WtfGlobal.getLocaleText("acc.rem.166"),  //"Chart View",
            autoScroll:true,
            layout:'border',
            closable: true,
            bodyStyle:'padding:20px',
            defaults:{border:false},
            iconCls:(Wtf.isChrome?'accountingbase chartChrome':'accountingbase chart'),
            tbar:[new Wtf.Toolbar.Button({text:"Select Year"}),year],
            items:[new Wtf.Panel({
                id:"msgid"+id1,
                region:"south",
                width:10,
                baseCls:"chartmsg",
                html:"<b>Note:</b> Amount in <span class='currency-view'>"+WtfGlobal.getCurrencySymbol()+"  ("+WtfGlobal.getCurrencyName()+")</span>",
                border : false,
                frame:false
            }),new Wtf.Panel({
                    id:id1,
                    region:"center",
                    defaults:{border:false},
                    border : false,
                frame:false
            }),new Wtf.Panel({
                id:id2,
                region:"east",
                width:600,
                defaults:{border:false},
                border : false,
                frame:false
            })]
        });
        
        showChart(id1,dataflag1,swf1,xmlpath1,false,true,withinventory,nondeleted,deleted);
        showChart(id2,dataflag2,swf2,xmlpath2,true,true,withinventory,nondeleted,deleted);
        Wtf.getCmp(mainid).add(reportPanel);
    }
    Wtf.getCmp(mainid).setActiveTab(reportPanel);
    Wtf.getCmp(mainid).doLayout();
}

function chkcustaccload(){
    if(!Wtf.StoreMgr.containsKey("customer")){
        Wtf.customerAccStore.load();
        Wtf.StoreMgr.add("customer",Wtf.customerAccStore)
    }
}
function chkvenaccload(){
    if(!Wtf.StoreMgr.containsKey("ven")){
        Wtf.vendorAccStore.load();
        Wtf.StoreMgr.add("ven",Wtf.vendorAccStore)
    }

}
function chktaxload(){
    if(!Wtf.StoreMgr.containsKey("tax")){
        Wtf.taxStore.load();
        Wtf.StoreMgr.add("tax",Wtf.taxStore);
    }
}

function chksalesAccountload(){
    if(!Wtf.StoreMgr.containsKey("salesAccount")){
        Wtf.salesAccStore.load();
        Wtf.StoreMgr.add("salesAccount",Wtf.salesAccStore);
    }
}
function chkProductTypeload(){
    if(!Wtf.StoreMgr.containsKey("productType")){
        Wtf.productTypeStore.load();
        Wtf.StoreMgr.add("productType",Wtf.productTypeStore)
    }
}
function chkUomload(){
    if(!Wtf.StoreMgr.containsKey("uom")){
        Wtf.uomStore.load();
        Wtf.StoreMgr.add("uom",Wtf.uomStore)
    }
}
function chktermload(){
    if(!Wtf.StoreMgr.containsKey("term")){
        Wtf.termds.load();
        Wtf.StoreMgr.add("term",Wtf.termds)
    }
}

function chktitleload(){
    if(!Wtf.StoreMgr.containsKey("title")){
        Wtf.TitleStore.load();
        Wtf.StoreMgr.add("title",Wtf.TitleStore);
    }
}
function chkCustomerCategoryload(){
    if(!Wtf.StoreMgr.containsKey("CustomerCategory")){
        Wtf.CustomerCategoryStore.load();
        Wtf.StoreMgr.add("CustomerCategory",Wtf.CustomerCategoryStore);
    }
}
function chkVendorCategoryload(){
    if(!Wtf.StoreMgr.containsKey("VendorCategory")){
        Wtf.VendorCategoryStore.load();
        Wtf.StoreMgr.add("VendorCategory",Wtf.VendorCategoryStore);
    }
}
function chkCostCenterload(){
    if(!Wtf.StoreMgr.containsKey("CostCenter")){
        Wtf.CostCenterStore.load();
        Wtf.StoreMgr.add("CostCenter",Wtf.CostCenterStore);
    }
}
function chkFormCostCenterload(){
    if(!Wtf.StoreMgr.containsKey("FormCostCenter")){
        Wtf.FormCostCenterStore.load();
        Wtf.StoreMgr.add("FormCostCenter",Wtf.FormCostCenterStore);
    }
}
function chkAssetCategoryload(){
    if(!Wtf.StoreMgr.containsKey("AssetCategory")){
        Wtf.AssetCategoryStore.load();
        Wtf.StoreMgr.add("AssetCategory",Wtf.AssetCategoryStore);
    } else if(Wtf.dirtyStore.assetCategory) {
        Wtf.AssetCategoryStore.reload();
        Wtf.dirtyStore.assetCategory=false;
    }
}
 function chktimezoneload()
 {
     if(!Wtf.StoreMgr.containsKey("timezone")){
            Wtf.timezoneStore.load();
            Wtf.StoreMgr.add("timezone",Wtf.timezoneStore);
        }
 }
 function chkcountryload()
 {
     if(!Wtf.StoreMgr.containsKey("country")){
            Wtf.countryStore.load();
            Wtf.StoreMgr.add("country",Wtf.countryStore);
        }
 }
  function chkcurrencyload(){
     if(!Wtf.StoreMgr.containsKey("currencystore")){
            Wtf.currencyStore.load();
            Wtf.StoreMgr.add("currencystore",Wtf.currencyStore);
        }
 }
 function chkproductload(){
     if(!Wtf.StoreMgr.containsKey("productstore")){
            Wtf.productStore.load();
            Wtf.StoreMgr.add("productstore",Wtf.productStore);
        }
 }
 function chkproductSalesload(){
     if(!Wtf.StoreMgr.containsKey("productstoresales")){
            Wtf.productStoreSales.load();
            Wtf.StoreMgr.add("productstoresales",Wtf.productStoreSales);
        }
 }
Wtf.apply(Wtf.form.VTypes, {
    daterange : function(val, field) {
        var date = field.parseDate(val);

        if(!date){
            return;
        }
        if (field.startDateField && (!this.dateRangeMax || (date.getTime() != this.dateRangeMax.getTime()))) {
            var start = Wtf.getCmp(field.startDateField);
            start.setMaxValue(date);
            start.validate();
            this.dateRangeMax = date;
        }
        else if (field.endDateField && (!this.dateRangeMin || (date.getTime() != this.dateRangeMin.getTime()))) {
            var end = Wtf.getCmp(field.endDateField);
            end.setMinValue(date);
            end.validate();
            this.dateRangeMin = date;
        }
        /*
         * Always return true since we're only using this vtype to set the
         * min/max allowed values (these are tested for after the vtype test)
         */
        return true;
    },

    password : function(val, field) {
        if (field.initialPassField) {
            var pwd = Wtf.getCmp(field.initialPassField);
            return (val == pwd.getValue());
        }
        return true;
    },

    passwordText : 'Passwords do not match'
});

Wtf.comboBoxRenderer = function(combo) {
    return function(value) {
        var idx = combo.store.find(combo.valueField, value);
        if(idx == -1)
            return "";
        var rec = combo.store.getAt(idx);
        return rec.data[combo.displayField];
    };
}

Wtf.commonWaitMsgBox = function(msg) {
    Wtf.MessageBox.show({
        msg: msg,
        width:290,
        wait:true,
        title:WtfGlobal.getLocaleText("acc.common.load"),  //"Processing your request. Please wait...",
        waitConfig: {interval:200}
    });
}

Wtf.updateProgress =function() {
    Wtf.MessageBox.hide();
}

function setZeroToBlank(field){
        if(field.getValue()==0){
           field.setValue("");
        }
    }
