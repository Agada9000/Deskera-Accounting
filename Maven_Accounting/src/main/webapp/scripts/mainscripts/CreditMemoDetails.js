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
function invLink(val,isCNReport, isCustBill){
    if(isCNReport=="true")
        isCustBill=="true"?callBillingInvoiceList(val,true):callInvoiceList(val,true);
    else
        isCustBill=="true"?callBillingGoodsReceiptList(val,true):callGoodsReceiptList(val,true);
}
function openNote(isNote, isCustBill){
    if(isNote=="true")
        isCustBill=="true"?callBillingCreditNote():callCreditNote();
    else
        isCustBill=="true"?callBillingDebitNote():callDebitNote();
}
/* < COMPONENT USED FOR >
 *  1.Give Refund or Details
 *      callCreditNoteDetails() --- <Credit Note Details>
 *      [isCNReport:true]
 *  2.Debit Note Report
 *      callDebitNoteDetails() --- <Debit Note Details>
 *      [isCNReport:false]
 */
Wtf.account.NoteDetailsPanel=function(config){
    this.businessPerson=(config.isCNReport?'Customer':'Vendor');
    this.costCenterId = "";
    this.extraFilters = config.extraFilters;
    if(config.extraFilters != undefined){//Cost Center Report View
        this.costCenterId = config.extraFilters.costcenter?config.extraFilters.costcenter:"";
    }
    this.transType=(config.isCNReport?'Credit':'Debit');
    this.uPermType=(config.isCNReport?Wtf.UPerm.invoice:Wtf.UPerm.vendorinvoice);
    this.permType=(config.isCNReport?Wtf.Perm.invoice:Wtf.Perm.vendorinvoice);
    this.exportPermType=(config.isCNReport?this.permType.exportdatacn:this.permType.exportdatadn);
    this.printPermType=(config.isCNReport?this.permType.printcn:this.permType.printdn);
    this.removePermType=(config.isCNReport?this.permType.removecn:this.permType.removedn);
    this.expandRec = Wtf.data.Record.create ([
        {name:'billid'},
        {name:'rowid'},
        {name:'billno'},
        {name:'transectionid'},
        {name:'transectionno'},
        {name:'productid'},
        {name:'currencysymbol'},
        {name:'productname',mapping:(config.isCustBill?"productdetail":null)},
        {name:'desc'/*,convert:this.shortString*/},
        {name:'quantity'},
        {name:'discount'},
        {name:'amount'},
        {name:'memo'/*,convert:this.shortString*/}
    ]);
    this.expandStoreUrl = "";
    //mode:config.isCNReport?(config.isCustBill?63:28):(config.isCustBill?63:29)
    if(config.isCNReport){
        this.expandStoreUrl = config.isCustBill?"ACCCreditNote/getBillingCreditNoteRows.do":"ACCCreditNote/getCreditNoteRows.do";
    }else {
        this.expandStoreUrl = config.isCustBill?"ACCDebitNote/getBillingDebitNoteRows.do":"ACCDebitNote/getDebitNoteRows.do";
    }
    this.expandStore = new Wtf.data.Store({
        url:this.expandStoreUrl,
//        url:Wtf.req.account+this.businessPerson+'Manager.jsp',
        baseParams:{
            mode:config.isCNReport?(config.isCustBill?63:28):(config.isCustBill?63:29)
        },
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        },this.expandRec)
    });
    this.expandStore.on('load',this.fillExpanderBody,this);
    this.GridRec = Wtf.data.Record.create ([
        {name:"noteid"},
        {name:"noteno"},
        {name:'journalentryid'},
        {name:'currencysymbol'},
        {name:'entryno'},
        {name:"personid"},
        {name:"personname"},
        {name:'amount'},
        {name:'costcenterid'},
        {name:'costcenterName'},
        {name:"date",type:'date'},
        {name:'memo'},
        {name:'notetax'},
        {name:'noteSubTotal'}
    ]);
    this.StoreUrl = "";
    //mode:config.isCNReport?(config.isCustBill?62:27):(config.isCustBill?62:28)
    if(config.isCNReport){
        this.StoreUrl = config.isCustBill?"ACCCreditNote/getBillingCreditNote.do":"ACCCreditNote/getCreditNote.do";
    }else {
        this.StoreUrl = config.isCustBill?"ACCDebitNote/getBillingDebitNote.do":"ACCDebitNote/getDebitNote.do";
    }
    this.Store = new Wtf.data.Store({
        url:this.StoreUrl,
//        url: Wtf.req.account+this.businessPerson+'Manager.jsp',
        baseParams:{
            mode:config.isCNReport?(config.isCustBill?62:27):(config.isCustBill?62:28),
            costCenterId: this.costCenterId
        },
        reader: new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty:"count"
        },this.GridRec)
    });
    if(this.extraFilters != undefined){//Cost Center Report View
        var currentBaseParams = this.Store.baseParams;
        currentBaseParams.startdate = this.extraFilters.startdate;
        currentBaseParams.enddate = this.extraFilters.enddate;
        this.Store.baseParams=currentBaseParams;
    }
    this.Store.load({params:{start:0,limit:30}});
     WtfComMsgBox(29,4,true);
    this.Store.on('datachanged', function(){
                var p = this.pP.combo.value;
                this.quickPanelSearch.setPage(p);
    }, this);
    this.Store.on('load',this.storeloaded,this);
    this.expander = new Wtf.grid.RowExpander();
    this.rowNo=new Wtf.grid.RowNumberer();
    this.sm = new Wtf.grid.CheckboxSelectionModel();
    this.grid = new Wtf.grid.GridPanel({
        stripeRows :true,
        id:"gridmsg"+config.helpmodeid,
        store:this.Store,
        autoScroll:true,
        sm : this.sm,
        border:false,
        layout:'fit',
        viewConfig:{forceFit:true,emptyText:WtfGlobal.emptyGridRenderer("<a class='grid-link-text' href='#' onClick='javascript:openNote(\""+config.isCNReport+"\",\""+config.isCustBill+"\")'> "+WtfGlobal.getLocaleText("acc.rem.147")+" "+(config.isCNReport?WtfGlobal.getLocaleText("acc.accPref.autoCN"):WtfGlobal.getLocaleText("acc.accPref.autoDN"))+" "+WtfGlobal.getLocaleText("acc.rem.148")+"</a>")},
        forceFit:true,
        loadMask : true,
        plugins: this.expander, 
        columns:[this.sm,this.expander,{
            dataIndex:"noteid",
            hidden:true
        },{
            header:(config.isCNReport?WtfGlobal.getLocaleText("acc.cnList.gridNoteNo"):WtfGlobal.getLocaleText("acc.dnList.gridNoteNo")),  //this.transType+" Note No",
            dataIndex:"noteno",
            sortable:true,
            pdfwidth:100,
            renderer:WtfGlobal.linkRenderer
        },{
            header:(config.isCNReport?WtfGlobal.getLocaleText("acc.cnList.gridDate"):WtfGlobal.getLocaleText("acc.dnList.gridDate")),  //this.transType+" Date",
            dataIndex:"date",
            align:'center',
            pdfwidth:100,
            renderer:WtfGlobal.onlyDateRenderer,
            sortable:true
        },{
            header:(config.isCNReport?WtfGlobal.getLocaleText("acc.cnList.gridCustomerName"):WtfGlobal.getLocaleText("acc.dnList.gridVendorName")),  //this.businessPerson+" Name",
            dataIndex:"personname",
            pdfwidth:100,
            sortable:true
        },{ 
            header:WtfGlobal.getLocaleText("acc.dnList.gridJEno"),  //"Journal Entry No",
            dataIndex:'entryno',
            sortable:true,
            pdfwidth:100,
            renderer:WtfGlobal.linkRenderer
        },{
            header:WtfGlobal.getLocaleText("acc.dnList.gridMemo"),  //"Memo",
            dataIndex:'memo',
            pdfwidth:100,
            sortable:true
        },{
            header:WtfGlobal.getLocaleText("acc.dnList.gridAmt"),  //"Amount",
            dataIndex:'amount',
            align:'right',
            pdfwidth:100,
            renderer:WtfGlobal.withoutRateCurrencySymbol
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

    this.deleteBtn=new Wtf.Toolbar.Button({
        text: ((this.transType=="Credit")?WtfGlobal.getLocaleText("acc.cnList.deleteNote"):WtfGlobal.getLocaleText("acc.dnList.deleteNote")),  //'Delete '+this.transType+' Note',
        scope: this,
        disabled :true,
        tooltip:((this.transType=="Credit")?WtfGlobal.getLocaleText("acc.cnList.deleteNoteTT"):WtfGlobal.getLocaleText("acc.dnList.deleteNoteTT")),  //{text:"Select a "+this.transType+" Note to delete.",dtext:"Select a "+this.transType+" Note to delete.", etext:"Delete selected "+this.transType+" Note details."},
        iconCls:getButtonIconCls(Wtf.etype.deletebutton),
        handler:this.performDelete.createDelegate(this)
    });

    var btnArr=[];
    btnArr.push(this.quickPanelSearch = new Wtf.KWLTagSearch({
        emptyText:(config.isCNReport?WtfGlobal.getLocaleText("acc.cnList.search"):WtfGlobal.getLocaleText("acc.dnList.search")), //'Quick Search by ' +(config.isCNReport?'Credit':'Debit')+ ' Note No',
        width: 200,
        id:"quickSearch"+config.helpmodeid,
        maxLength:50,
        field: "noteno"
    }),
    this.resetBttn);
    if(config.extraFilters == undefined){//Cost Center Report View - Don't show 'Delete' Button
        if(!WtfGlobal.EnableDisable(this.uPermType, this.removePermType)){
            btnArr.push('-',this.deleteBtn);
        }
    }
    if(!WtfGlobal.EnableDisable(this.uPermType, this.exportPermType)){
        btnArr.push('-',this.exportButton= new Wtf.exportButton({
            obj:this,
            id:"exportReports"+config.helpmodeid,
            text:WtfGlobal.getLocaleText("acc.common.export"),
            tooltip :WtfGlobal.getLocaleText("acc.common.exportTT"),  //'Export report details',
            disabled :true,
            menuItem:{csv:true,pdf:true,rowPdf:true,rowPdfTitle:WtfGlobal.getLocaleText("acc.rem.39")+" "+(config.isCNReport?WtfGlobal.getLocaleText("acc.accPref.autoCN"):WtfGlobal.getLocaleText("acc.accPref.autoDN"))},
            get:config.isCustBill?(config.isCNReport?Wtf.autoNum.BillingCreditNote:Wtf.autoNum.BillingDebitNote):(config.isCNReport?Wtf.autoNum.CreditNote:Wtf.autoNum.DebitNote)
        }));
    }
    if(!WtfGlobal.EnableDisable(this.uPermType, this.printPermType)){
        btnArr.push('-',this.printButton=new Wtf.exportButton({
            text:WtfGlobal.getLocaleText("acc.common.print"),  //"Print",
            obj:this,
            tooltip :WtfGlobal.getLocaleText("acc.common.printTT"),  //'Print report details',
            disabled :true,
            menuItem:{print:true},
            params:{name:(config.isCNReport?WtfGlobal.getLocaleText("acc.cnList.tabTitle"):WtfGlobal.getLocaleText("acc.dnList.tabTitle"))},
            label:(config.isCNReport?"Credit":"Debit")+" Note",
            get:config.isCustBill?(config.isCNReport?Wtf.autoNum.BillingCreditNote:Wtf.autoNum.BillingDebitNote):(config.isCNReport?Wtf.autoNum.CreditNote:Wtf.autoNum.DebitNote)
        }));
    }

    chkCostCenterload();
    if(Wtf.CostCenterStore.getCount()==0) Wtf.CostCenterStore.on("load", this.setCostCenter, this);
    this.costCenter = new Wtf.form.ComboBox({
        store: Wtf.CostCenterStore,
        name:'costCenterId',
        width:100,
        listWidth:100,
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

        this.loadCMStore();
    },this);
    if(config.extraFilters == undefined){//Cost Center Report View - Don't show 'cost center' filter
        btnArr.push("-",WtfGlobal.getLocaleText("acc.common.costCenter"),this.costCenter);     //"Cost Center"
    }

    btnArr.push("->");
    btnArr.push(getHelpButton(this,config.helpmodeid));
    Wtf.apply(this,{
        border:false,
        layout : "fit",
        items:[this.grid],
        tbar:btnArr,
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
    this.expander.on("expand",this.onRowexpand,this);
    Wtf.account.NoteDetailsPanel.superclass.constructor.call(this,config);
     this.addEvents({
        'invoice':true,
        'journalentry':true,
        'goodsreceipt':true
     });
//     if(Wtf.getCmp("custCreditMemo")!=null)
//        Wtf.getCmp("CreditMemo").on('update',this.loadCMStore,this);
    this.grid.on('cellclick',this.onCellClick, this);
}
Wtf.extend(Wtf.account.NoteDetailsPanel,Wtf.Panel,{
   handleResetClick:function(){
       if(this.quickPanelSearch.getValue()){
           this.quickPanelSearch.reset();
              this.Store.load({
                params: {
                    start:0,
                    limit:this.pP.combo.value
                }
            });
       }
    },
    setCostCenter: function(){
        this.costCenter.setValue(this.costCenterId);//Select Default Cost Center
        Wtf.CostCenterStore.un("load", this.setCostCenter, this);
    },
    storeloaded:function(store){
        if(store.getCount()==0){
            if(this.exportButton)this.exportButton.disable();
            if(this.printButton)this.printButton.disable();
            if(this.deleteBtn)this.deleteBtn.disable();
        }else{
            if(this.exportButton)this.exportButton.enable();
            if(this.printButton)this.printButton.enable();
            if(this.deleteBtn)this.deleteBtn.enable();
        }
        Wtf.MessageBox.hide();
        this.quickPanelSearch.StorageChanged(store);
    },
    onRowexpand:function(scope, record, body, rowIndex){
        this.expanderBody=body;       
        this.expandStore.load({params:{bills:record.data["noteid"]}});
    },
    fillExpanderBody:function(){
        if(this.expandStore.getCount()>0){
            var disHtml = "";
            var arr=[];
//            arr=['Product '+(this.isCustBill?'Description':""),this.isCustBill?"":'Description','Transaction Number','Quantity','Amount','Memo',"                "];
            arr=[WtfGlobal.getLocaleText("acc.cnList.prod")+' '+(this.isCustBill?WtfGlobal.getLocaleText("acc.cnList.Desc"):""),this.isCustBill?"":WtfGlobal.getLocaleText("acc.cnList.Desc"),WtfGlobal.getLocaleText("acc.cnList.TransNo"),WtfGlobal.getLocaleText("acc.cnList.qty"),WtfGlobal.getLocaleText("acc.cnList.gridAmt"),WtfGlobal.getLocaleText("acc.cnList.gridMemo"),"                "];
            var header = "<span class='gridHeader'>"+WtfGlobal.getLocaleText("acc.cnList.prodList")+"</span>"; //Product List
            header += "<span class='gridNo' style='font-weight:bold;'>"+WtfGlobal.getLocaleText("acc.cnList.Sno")+"</span>";    //S.No.
            for(var i=0;i<arr.length;i++){
                header += "<span class='headerRow'>" + arr[i] + "</span>";
            }
            header += "<span class='gridLine'></span>";
            for(i=0;i<this.expandStore.getCount();i++){
                var rec=this.expandStore.getAt(i);
                header += "<span class='gridNo'>"+(i+1)+".</span>";
                header += "<span class='gridRow'  wtf:qtip='"+rec.data['productname']+"'>"+Wtf.util.Format.ellipsis(rec.data['productname'],20)+"</span>";
                if(!this.isCustBill)
                    header += "<span class='gridRow' wtf:qtip='"+rec.data['productname']+"' >"+Wtf.util.Format.ellipsis(rec.data['desc'],20)+"&nbsp;</span>";
                header += "<span class='gridRow ' >"+"<a  class='jumplink' href='#' onClick='javascript:invLink(\""+rec.data['transectionid']+"\",\""+this.isCNReport+"\",\""+this.isCustBill+"\")'>"+rec.data['transectionno']+"</a>"+"</span>";
                header += "<span class='gridRow'>"+rec.data['quantity']+"</span>";
                header += "<span class='gridRow'>"+WtfGlobal.addCurrencySymbolOnly(rec.data['discount'],rec.data['currencysymbol'],[true])+"</span>";
                header += "<span class='gridRow' style='width:30%'  wtf:qtip='"+rec.data['memo']+"'>"+Wtf.util.Format.ellipsis(rec.data['memo'],80)+"</span>";
                header +="<br>";
            }
            disHtml += "<div class='expanderContainer' style='width:100%'>" + header + "</div>";
            this.expanderBody.innerHTML = disHtml;
        }
    },
    onCellClick:function(g,i,j,e){
        e.stopEvent();
        var el=e.getTarget("a");
        if(el==null)return;
        var header=g.getColumnModel().getDataIndex(j);
//        if(header=="billno"){
//            var accid=this.Store.getAt(i).data['invoiceid'];
//                   this.fireEvent('invoice',accid);
//        }
    if(header=="entryno"){
            var accid=this.Store.getAt(i).data['journalentryid'];
            this.fireEvent('journalentry',accid,true);
        }
        if(header=="noteno"){
           this.viewTransection();
        }
    },
    loadCMStore:function(){
        this.Store.load({
           params : {
               start : 0,
               limit : this.pP.combo.value,
               ss : this.quickPanelSearch.getValue()
           }
       });
        this.Store.on('load',this.storeloaded,this);
    },
    shortString:function(name){
        if(name.length > 20){
            return name.substr(0, 17) + '...';
        }
        return name;
    },
    viewTransection:function(){
        var formrec=null;
        if(this.grid.getSelectionModel().hasSelection()==false||this.grid.getSelectionModel().getCount()>1){
                WtfComMsgBox(15,2);
                return;
        }
        formrec = this.grid.getSelectionModel().getSelected();
        if(this.isCNReport)
            this.isCustBill?callViewBillingCreditNote(formrec, 'ViewcreditNote'):callViewCreditNote(formrec, 'ViewcreditNote');
        else
            this.isCustBill?callViewBillingDebitNote(formrec, 'ViewDebitNote'):callViewDebitNote(formrec, 'ViewDebitNote');
    },

    performDelete:function(){
        if(this.grid.getSelectionModel().hasSelection()==false){
            WtfComMsgBox(34,2);
            return;
        }
        var data=[];
        var arr=[];
        this.recArr = this.grid.getSelectionModel().getSelections();
        this.grid.getSelectionModel().clearSelections();
        WtfGlobal.highLightRowColor(this.grid,this.recArr,true,0,2);
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.confirm"), this.transType!="Credit"? WtfGlobal.getLocaleText("acc.rem.150"):WtfGlobal.getLocaleText("acc.rem.149") ,function(btn){           //"Confirm"
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
            data= WtfGlobal.getJSONArray(this.grid,true,arr);
            var mode=(this.isCNReport?(this.isCustBill?64:45):(this.isCustBill?64:45));
            this.deleteUrl = "";
            if(this.businessPerson=="Customer") {
                this.deleteUrl = this.isCustBill?"ACCCreditNote/deleteBillingCreditNotes.do":"ACCCreditNote/deleteCreditNotes.do";
            } else if(this.businessPerson=="Vendor") {
                this.deleteUrl = this.isCustBill?"ACCDebitNote/deleteBillingDebitNotes.do":"ACCDebitNote/deleteDebitNotes.do";
            }

            Wtf.Ajax.requestEx({
//                url: Wtf.req.account+this.businessPerson+'Manager.jsp',
                url: this.deleteUrl,
                params:{
                    data:data,
                    mode:mode
                }
            },this,this.genSuccessResponse,this.genFailureResponse);
        },this);
    },

    genSuccessResponse:function(response){
        WtfComMsgBox([(this.transType=="Credit")?WtfGlobal.getLocaleText("acc.accPref.autoCN"):WtfGlobal.getLocaleText("acc.accPref.autoDN"),response.msg],response.success*2+1);
        for(var i=0;i<this.recArr.length;i++){
            var ind=this.Store.indexOf(this.recArr[i])
            var num= ind%2;
            WtfGlobal.highLightRowColor(this.grid,this.recArr[i],false,num,2,true);
        }
        if(response.success){
            (function(){
                this.loadCMStore();
            }).defer(WtfGlobal.gridReloadDelay(),this);
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
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],1);    //'Alert'
    }
}); 
