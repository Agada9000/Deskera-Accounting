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
function invRecLink(val,isReceipt,isCustBill){
if(isReceipt=="true")
        isCustBill=="true"?callBillingInvoiceList(val,true):callInvoiceList(val,true);
    else
        isCustBill=="true"?callBillingGoodsReceiptList(val,true):callGoodsReceiptList(val,true);
}
function openRec(isRec,isCustBill){
    if(isRec=="true")
        isCustBill=="true"?callBillingReceipt():callReceipt();
    else
        isCustBill=="true"?callBillingPayment():callPayment();
}

/* <COMPONENT USED FOR>
 * 1.Receive Payment Report
 *      ReceiptReport() --- < Payment Received >
 *      [isReceipt:true]
 * 2.Receive Payment Report
 *      BillingReceiptReport() --- <>
 *      [isReceipt:true, isCustBill:true]
 * 3.Payment Report
 *      callPaymentReport() --- <>
 *      [isReceipt:false]
 *
 */
Wtf.account.ReceiptReport=function(config){
    this.isReceipt=config.isReceipt||false; 
    this.isCustBill=config.isCustBill||false;
    this.nondeleted=false;
    this.deleted=false;
     this.label=(this.isReceipt?'Receipt':'Payment');
    this.uPermType=(this.isReceipt?Wtf.UPerm.invoice:Wtf.UPerm.vendorinvoice);
    this.permType=(this.isReceipt?Wtf.Perm.invoice:Wtf.Perm.vendorinvoice);
    this.exportPermType=(this.isReceipt?this.permType.exportdatareceipt:this.permType.exportdatapayment);
    this.printPermType=(this.isReceipt?this.permType.printreceipt:this.permType.printpayment);
    this.removePermType=(this.isReceipt?this.permType.removereceipt:this.permType.removepayment);
    this.editPermType=(this.isReceipt?this.permType.editreceipt:this.permType.editpayment);
    this.emailPermType=(this.isReceipt?this.permType.emailreceipt:this.permType.emailpayment);
    this.typeEditor = new Wtf.form.ComboBox({
        store: Wtf.delTypeStore,
        name:'typeid',
        displayField:'name',
        id:'view'+config.helpmodeid,
        valueField:'typeid',
        mode: 'local',
        value:0,
        triggerAction: 'all',
        typeAhead:true,
        selectOnFocus:true
    });
    this.expandRec = Wtf.data.Record.create ([
        {name:'transectionno'},
        {name:'creationdate',type:'date'},
        {name:'duedate',type:'date'},
        {name:'quantity'},
        {name:'currencysymbol'},
        {name:'amount'},
        {name:'totalamount'},
        {name:'rowid'},
        {name:'transectionid'},
        {name:'billid'},
        {name:'journalentryid'},
        {name:'personid'},
        {name:'entryno'},
        {name:'billno'},
        {name:'transectionno'},
        {name:'date',type:'date',mapping:'creationdate'},
        {name:'currencyid'},
        {name:'oldcurrencyrate'},
        {name:'currencyname'},
        {name:'oldcurrencysymbol'},
        {name:'vendorid'},
        {name:'vendorname'},
        {name:'personname'},

        {name: 'externalcurrencyrate'},
        {name:'amountdue', mapping:'amountduenonnegative'},
        {name:'taxpercent'},
        {name:'discount'},
        {name:'memo'},
        {name:'payment'},
        {name:'accountid'},
        {name:'accountname'},
        {name:'description'},
        {name:'dramount'}
    ]);
    this.expandStoreUrl = "";
    if(this.isReceipt){
        this.expandStoreUrl =  (this.isCustBill?"ACCReceipt/getBillingReceiptRows":"ACCReceiptCMN/getReceiptRows") + ".do";
    }else{
        this.expandStoreUrl =  (this.isCustBill?"ACCVendorPayment/getBillingPaymentRows":"ACCVendorPaymentCMN/getPaymentRows") + ".do";
    }

    this.expandStore = new Wtf.data.Store({
        url : this.expandStoreUrl,
//        url:Wtf.req.account+(this.isReceipt?'CustomerManager.jsp':'VendorManager.jsp'),
        baseParams:{
            mode:(this.isCustBill?36:33)
        },
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        },this.expandRec)
    });
    this.usersRec = new Wtf.data.Record.create([
        {name: 'billid'},
        {name: 'refid'},
        {name: 'personid'},
        {name: 'billno'},
        {name: 'refno'},
        {name: 'refname'},
        {name: 'refdetail'},
        {name:'personemail'},
        {name: 'detailtype'},
        {name: 'expirydate'},
        {name: 'journalentryid'},
        {name: 'entryno'},
        {name: 'currencysymbol'},
        {name: 'externalcurrencyrate'},
        {name: 'personname'},
        {name: 'address'},
        {name: 'deleted'},
        {name: 'billdate',type:'date'},
        {name: 'paymentmethod'},
        {name: 'memo'},
        {name: 'amount'},
        {name: 'methodid'},
        {name: 'receiptamount'},
        {name: 'currencyid'}
    ]);

    this.userdsUrl = "";
    if(this.isReceipt){
        this.userdsUrl = "ACCReceipt/" + (this.isCustBill?"getBillingReceipts":"getReceipts") + ".do";
    }else{
        this.userdsUrl = "ACCVendorPayment/" + (this.isCustBill?"getBillingPayments":"getPayments") + ".do";
    }
    this.userds = new Wtf.data.Store({
        reader: new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty:"count"
        },this.usersRec),
        url : this.userdsUrl,
//        url:Wtf.req.account+(this.isReceipt?'CustomerManager.jsp':'VendorManager.jsp'),
        baseParams:{
            mode:(this.isCustBill?35:32)
        }
    });
    this.userds.load({params:{start:0,limit:30}});
    WtfComMsgBox(29,4,true);


    this.expander = new Wtf.grid.RowExpander({});
    this.rowNo=new Wtf.grid.RowNumberer();
    this.sm = new Wtf.grid.CheckboxSelectionModel();
    var btnArr=[];
    this.gridcm= new Wtf.grid.ColumnModel([this.sm,this.expander,{
        dataIndex:'billid',
        hidden:true
    },{
        header:this.isReceipt?WtfGlobal.getLocaleText("acc.prList.gridReceiptNo"):WtfGlobal.getLocaleText("acc.pmList.gridPaymentNo"),  //"Receipt No":"Payment No",
        dataIndex:'billno',
        autoWidth : true,
        sortable: true,
        groupable: true,
        pdfwidth:100,
        renderer:WtfGlobal.linkDeletedRenderer
    },{
        header: WtfGlobal.getLocaleText("acc.pmList.accName"),  //"Account Name",
        dataIndex: 'personname',
        autoWidth : true,
        sortable: true,
        groupable: true,
        renderer:WtfGlobal.deletedRenderer,
        pdfwidth:100
    },{
        header: this.isReceipt?WtfGlobal.getLocaleText("acc.prList.Date"):WtfGlobal.getLocaleText("acc.pmList.Date"),  //"Receipt Date":"Payment Date",
        dataIndex: 'billdate',
        align:'center',
        renderer:WtfGlobal.onlyDateDeletedRenderer,
        autoWidth : true,
        sortable: true,
        groupable: true,
        pdfwidth:100
    },{ 
        header :WtfGlobal.getLocaleText("acc.pmList.JEno"),  //'Journal Entry No',
        dataIndex: 'entryno',
        autoSize : true,
        sortable: true,
        groupable: true,
        pdfwidth:100,
        renderer:WtfGlobal.linkDeletedRenderer
    },{
        header :WtfGlobal.getLocaleText("acc.prList.gridPaymentMethod"),  //'Payment Method',
        dataIndex: 'paymentmethod',
        autoSize : true,
        sortable: true,
        groupable: true,
        renderer:WtfGlobal.deletedRenderer,
        pdfwidth:100
    },{
        header :WtfGlobal.getLocaleText("acc.common.memo"),  //'Memo',
        dataIndex: 'memo',
        autoSize : true,
        sortable: true,
        groupable: true,
        renderer:WtfGlobal.deletedRenderer,
        pdfwidth:100
    },{
        header :this.isReceipt?WtfGlobal.getLocaleText("acc.prList.amtRec"):WtfGlobal.getLocaleText("acc.prList.amtPaid"),  //"Amount Received":"Amount Paid",
        dataIndex: 'amount',
        align:'right',
        pdfwidth:100,
        renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol
    }]);
    this.grid = new Wtf.grid.GridPanel({
        plugins: this.expander,
        cls:'vline-on',
        id:"gridmsg"+config.helpmodeid,
        layout:'fit',
        autoScroll:true,
        store: this.userds,
        cm: this.gridcm,
        sm :this.sm,
        border : false,
        loadMask : true,
        viewConfig: {
            forceFit:true,
            emptyText:WtfGlobal.emptyGridRenderer("<a class='grid-link-text' href='#' onClick='javascript:openRec(\""+this.isReceipt+"\",\""+this.isCustBill+"\")'>"+(this.isReceipt?WtfGlobal.getLocaleText("acc.nee.24"):WtfGlobal.getLocaleText("acc.nee.25"))+"</a>")
        }
    });
    this.quickPanelSearch = new Wtf.KWLTagSearch({
            emptyText:WtfGlobal.getLocaleText("acc.prList.search"),  //'Search by Account Name...',
                width: 200,
                id:"quickSearch"+config.helpmodeid,
                field: 'receiptnumber'
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
    if(!this.isOrder&&!WtfGlobal.EnableDisable(this.uPermType, this.editPermType)){ 
   //      btnArr.push('-');
        btnArr.push(this.editBttn=new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.common.edit"),  //'Edit', 
            tooltip :(this.isReceipt?WtfGlobal.getLocaleText("acc.prList.editTT"):WtfGlobal.getLocaleText("acc.pmList.editTT")),  //'Allows you to edit Receipt.',
            id: 'btnEdit' + this.id,
            scope: this,
    //            hidden:this.isCustBill,
            iconCls :getButtonIconCls(Wtf.etype.edit),
            disabled :true
        }));
        //btnArr.push('-');
        this.editBttn.on('click',this.editTransaction,this);
    }
    if(!this.isOrder&&!WtfGlobal.EnableDisable(this.uPermType, this.removePermType)){ 

    // btnArr.push('-');
    btnArr.push(this.deleteTrans=new Wtf.Toolbar.Button({
        text: (this.isReceipt?WtfGlobal.getLocaleText("acc.prList.delete"):WtfGlobal.getLocaleText("acc.pmList.delete")),
        scope: this,
        disabled :true,
        tooltip: (this.isReceipt?WtfGlobal.getLocaleText("acc.prList.deleteTT"):WtfGlobal.getLocaleText("acc.pmList.deleteTT")),  //{text:"Select a "+this.label+" to delete.",dtext:"Select a "+this.label+" to delete.", etext:"Delete selected "+this.label+" details."},
        iconCls:getButtonIconCls(Wtf.etype.deletebutton),
        handler:this.handleDelete.createDelegate(this)
    }));
    }
    //btnArr.push('-');
    if(!this.isOrder&&!WtfGlobal.EnableDisable(this.uPermType, this.emailPermType)){
    //    btnArr.push('-');
        btnArr.push(this.email=new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.email"),  //"Email",
            tooltip : WtfGlobal.getLocaleText("acc.common.emailTT"),  //"Email",
            scope: this,
            hidden:this.isOrder,
            disabled: true,
            iconCls : "accountingbase financialreport",
            handler : this.sendMail
        }));
    }
   // btnArr.push('-');
   if(!this.isOrder&&!WtfGlobal.EnableDisable(this.uPermType, this.exportPermType)){
   //     btnArr.push('-');
        btnArr.push(this.printButton=new Wtf.exportButton({
            obj:this,
            id:"exportReports"+config.helpmodeid,
            text:WtfGlobal.getLocaleText("acc.common.export"),
            tooltip :WtfGlobal.getLocaleText("acc.common.exportTT"),  //'Export report details',
            disabled :true,
            menuItem:{csv:true,pdf:true,rowPdf:true,rowPdfTitle:(WtfGlobal.getLocaleText("acc.rem.39")+" "+(this.isReceipt?WtfGlobal.getLocaleText("acc.receipt.1"):WtfGlobal.getLocaleText("acc.receipt.2")))},
            get:this.isCustBill?(this.isReceipt?Wtf.autoNum.BillingReceipt:Wtf.autoNum.BillingPayment):(this.isReceipt?Wtf.autoNum.Receipt:Wtf.autoNum.Payment)
        }));
   }
   // btnArr.push('-');
  if(!this.isOrder&&!WtfGlobal.EnableDisable(this.uPermType, this.printPermType)){
   //    btnArr.push('-');
        btnArr.push(this.exportButton=new Wtf.exportButton({ 
            text:WtfGlobal.getLocaleText("acc.common.print"),  //"Print", 
            obj:this,
            tooltip :WtfGlobal.getLocaleText("acc.common.printTT"),  //'Print report details',
            disabled :true,
            menuItem:{print:true},
            params:{name:this.isReceipt?WtfGlobal.getLocaleText("acc.prList.tabTitle"):WtfGlobal.getLocaleText("acc.pmList.tabTitle")},
            label:this.isReceipt?"Receipt":"Payment",
            get:this.isCustBill?(this.isReceipt?Wtf.autoNum.BillingReceipt:Wtf.autoNum.BillingPayment):(this.isReceipt?Wtf.autoNum.Receipt:Wtf.autoNum.Payment)
        }));
    
    }
   
    Wtf.apply(this,{
        layout : "fit",
        items:this.grid,
        tbar:[this.quickPanelSearch,
            this.resetBttn,btnArr,'->',this.typeEditor,'-',getHelpButton(this,config.helpmodeid)
        ],
        bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
            pageSize: 30,
            id: "pagingtoolbar" + this.id,
            store: this.userds,
            searchField:  this.quickPanelSearch ,
            displayInfo: true,
//            displayMsg: 'Displaying records {0} - {1} of {2}',
            emptyMsg: WtfGlobal.getLocaleText("acc.common.norec"), //"No results to display",
            plugins: this.pP = new Wtf.common.pPageSize({id : "pPageSize_"+this.id})
        })
    },config)     
    Wtf.account.ReceiptReport.superclass.constructor.call(this,config);
    this.addEvents({
        'invoice':true,
        'journalentry':true
    });
    this.userds.on('datachanged', function() {
        var p = this.pP.combo.value;
        this.quickPanelSearch.setPage(p);
     }, this);
    this.userds.on('load',this.storeloaded,this);
    this.expandStore.on('load',this.fillExpanderBody,this);
    this.expander.on("expand",this.onRowexpand,this);
    this.grid.on('cellclick',this.onCellClick, this);
   this.userds.on('beforeload',function(s,o){
        if(!o.params)o.params={};
        o.params.deleted=this.deleted;
        o.params.nondeleted=this.nondeleted;
    },this);
    this.typeEditor.on('select',this.loadTypeStore,this);
    this.sm.on("selectionchange",this.enableDisableButtons.createDelegate(this),this);

}
Wtf.extend(Wtf.account.ReceiptReport,Wtf.Panel,{

   loadTypeStore:function(a,rec){
        this.deleted=false;
        this.nondeleted=false;
        var index=rec.data.typeid;
        this.deleteTrans.enable();
        if(index==2)
            this.nondeleted=true;
        if(index==1){
             this.deleted=true;
            this.deleteTrans.disable();
        }
        this.userds.load({
                params: {
                    start:0,
                    limit:this.pP.combo.value,
                    ss : this.quickPanelSearch.getValue()
                }
            });
            WtfComMsgBox(29,4,true);
        this.userds.on('load',this.storeloaded,this);
    },
    enableDisableButtons:function(){
        Wtf.uncheckSelAllCheckbox(this.sm);
        this.deleteTrans.enable();
        var arr=this.grid.getSelectionModel().getSelections();
        for(var i=0;i<arr.length;arr++){
            if(arr[i]&&arr[i].data.deleted)
                this.deleteTrans.disable();
        }
        
        var rec = this.sm.getSelected();
        if(this.sm.getCount()==1 && rec.data.deleted != true){
            if(this.email)this.email.enable();
            if(this.editBttn)this.editBttn.enable();
            if(this.deleteTrans)this.deleteTrans.enable();
        }else{
            if(this.email)this.email.disable();
            if(this.editBttn)this.editBttn.disable();
            if(this.deleteTrans)this.deleteTrans.disable();
        }
    },
     handleResetClick:function(){
         if(this.quickPanelSearch.getValue()){
            this.quickPanelSearch.reset();
            this.loadStore();
         }
    },
    storeloaded:function(store){
        Wtf.uncheckSelAllCheckbox(this.sm);
        if(store.getCount()==0){
            if(this.exportButton)this.exportButton.disable();
            if(this.printButton)this.printButton.disable();
        }else{
            if(this.exportButton)this.exportButton.enable();
            if(this.printButton)this.printButton.enable();
        }
        Wtf.MessageBox.hide();
        this.quickPanelSearch.StorageChanged(store);
    },
    onCellClick:function(g,i,j,e){
        e.stopEvent();
        var el=e.getTarget("a");
        if(el==null)return;
        var header=g.getColumnModel().getDataIndex(j);
        if(header=="entryno"){
            var accid=this.userds.getAt(i).data['journalentryid'];
            this.fireEvent('journalentry',accid,true);
        }
        if(header=="billno"){
            this.viewTransection();
        }
    },
    loadStore:function(){
      this.userds.load({
                params: {
                    start:0,
                    limit:this.pP.combo.value
                }
            });
    },
    onRowexpand:function(scope, record, body){
        this.expanderBody=body;
        this.expandStore.load({params:{bills:record.data['billid']}});
    },
    fillExpanderBody:function(){
        if(this.expandStore.getCount()>0) {
        var disHtml = "";
        var arr=[];
        // arr=['Invoice No','Creation Date','Due Date','Invoice Amount','Amount Due',(this.isReceipt?'Amount Received':'Amount Paid'),"
        arr=[WtfGlobal.getLocaleText("acc.prList.invNo"),WtfGlobal.getLocaleText("acc.prList.creDate"),WtfGlobal.getLocaleText("acc.prList.dueDate"),WtfGlobal.getLocaleText("acc.prList.invAmt"),WtfGlobal.getLocaleText("acc.prList.amtDue"),(this.isReceipt?WtfGlobal.getLocaleText("acc.prList.amtRec"):WtfGlobal.getLocaleText("acc.prList.amtPaid")),"                "];
        var header = "<span class='gridHeader'>"+WtfGlobal.getLocaleText("acc.prList.prodList")+"</span>";  //Product List
        header += "<span class='gridNo' style='font-weight:bold;'>"+WtfGlobal.getLocaleText("acc.cnList.Sno")+"</span>";
        for(var i=0;i<arr.length;i++){
            header += "<span class='headerRow'>" + arr[i] + "</span>";
        }
        header += "<span class='gridLine' style='width:73%'></span>";
        for(i=0;i<this.expandStore.getCount();i++){
            var rec=this.expandStore.getAt(i);
            header += "<span class='gridNo'>"+(i+1)+".</span>";
            header += "<span class='gridRow ' >"+"<a  class='jumplink' href='#' onClick='javascript:invRecLink(\""+rec.data['transectionid']+"\",\""+this.isReceipt+"\",\""+this.isCustBill+"\")'>"+rec.data['transectionno']+"</a>"+"</span>";
            header += "<span class='gridRow'>"+WtfGlobal.onlyDateLeftRenderer(rec.data['creationdate'])+"</span>";
            header += "<span class='gridRow'>"+WtfGlobal.onlyDateLeftRenderer(rec.data['duedate'])+"</span>";
            header += "<span class='gridRow'>"+WtfGlobal.addCurrencySymbolOnly(rec.data['totalamount'],rec.data['currencysymbol'],[true])+"</span>";
            header += "<span class='gridRow'>"+WtfGlobal.addCurrencySymbolOnly(rec.data['amountdue'],rec.data['currencysymbol'],[true])+"</span>";
            header += "<span class='gridRow'>"+WtfGlobal.addCurrencySymbolOnly(rec.data['amount'],rec.data['currencysymbol'],[true])+"</span>";
          
            header +="<br>";
        }
        disHtml += "<div class='expanderContainer' style='width:100%'>" + header + "</div>";
        this.expanderBody.innerHTML = disHtml;
        }
        else
            this.expanderBody.innerHTML = "<br><b><div class='expanderContainer' style='width:100%'>"+WtfGlobal.getLocaleText("acc.prList.gridAmtReceived")+"</div></b>"      //This transaction is not linked with any invoice.

    }, 
    expandRow:function(){
        this.expandInvoice(this.invoiceID);
    },
    expandInvoice:function(id){
        this.Store.filter('billid',id);
        for(var i=0;i<this.Store.getCount();i++){
            var row = this.grid.view.getRow(i);
            if(Wtf.fly(row).hasClass('x-grid3-row-collapsed')==false) this.expander.toggleRow(row);
            if(this.Store.getAt(i).data['billid']==id){
                this.expander.toggleRow(i);
            }
        }
    },
     viewTransection:function(){
        var formrec=null;
        if(this.grid.getSelectionModel().hasSelection()==false||this.grid.getSelectionModel().getCount()>1){
                WtfComMsgBox(15,2);
                return;
        }
        formrec = this.grid.getSelectionModel().getSelected();
        if(this.isReceipt)
            this.isCustBill?callViewBillPayment(formrec, 'ViewBillingReceivePayment',true):callViewPayment(formrec, 'ViewReceivePayment',true);
        else
            this.isCustBill?callViewBillPayment(formrec, 'ViewBillingPaymentMade',false):callViewPayment(formrec, 'ViewPaymentMade',false);
    },
     editTransaction:function(){
        var formrec=null;
        if(this.grid.getSelectionModel().hasSelection()==false||this.grid.getSelectionModel().getCount()>1){
                WtfComMsgBox(15,2);
                return;
        }
        formrec = this.grid.getSelectionModel().getSelected();
        if(this.isReceipt)
            this.isCustBill?callEditBillPayment(formrec, 'EditBillingReceivePayment',true):callEditPayment(formrec, 'EditReceivePayment',true);
        else
            this.isCustBill?callEditBillPayment(formrec, 'EditBillingPaymentMade',false):callEditPayment(formrec, 'EditPaymentMade',false);
    },
    sendMail:function(){

        var formrec=null;
        if(this.grid.getSelectionModel().hasSelection()==false||this.grid.getSelectionModel().getCount()>1){
                WtfComMsgBox(15,2);
                return;
        }
        formrec = this.grid.getSelectionModel().getSelected();
        if(this.isReceipt)
            this.isCustBill? callEmailWin("emailwin",formrec,this.label,12,false): callEmailWin("emailwin",formrec,this.label,4,false);
        else
            this.isCustBill? callEmailWin("emailwin",formrec,this.label,16,false): callEmailWin("emailwin",formrec,this.label,8,false);

 


    },
    handleDelete:function(){
        if(!this.grid.getSelectionModel().hasSelection()){
            WtfComMsgBox(34,2);
            return;
        }
        var data=[];
        var arr=[];
        this.recArr = this.grid.getSelectionModel().getSelections();
        this.grid.getSelectionModel().clearSelections();
        WtfGlobal.highLightRowColor(this.grid,this.recArr,true,0,2);
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.alert"), this.isReceipt? WtfGlobal.getLocaleText("acc.nee.14"):WtfGlobal.getLocaleText("acc.nee.15") ,function(btn){
        if(btn!="yes") {
            for(var i=0;i<this.recArr.length;i++){
                var ind=this.grid.getStore().indexOf(this.recArr[i])
                var num= ind%2;
                WtfGlobal.highLightRowColor(this.grid,this.recArr[i],false,num,2,true);
            }
            return;
        }
        for(i=0;i<this.recArr.length;i++){
                arr.push(this.grid.getStore().indexOf(this.recArr[i]));
        }
        data= WtfGlobal.getJSONArray(this.grid,true,arr);
        this.deleteUrl = "";
        //(this.isCustBill?21:22)
        if(this.isReceipt){
            this.deleteUrl = "ACCReceipt/"+ (this.isCustBill?"deleteBillingReceipt":"deleteReceipt") +".do";
        }else{
            this.deleteUrl = "ACCVendorPayment/"+ (this.isCustBill?"deleteBillingPayment":"deletePayment") +".do";
        }
            Wtf.Ajax.requestEx({
                url:this.deleteUrl,
//                url: Wtf.req.account+(this.isReceipt?'CustomerManager.jsp':'VendorManager.jsp'),
                params:{
                   data:data,
                    mode:(this.isCustBill?21:22)
                }
            },this,this.genSuccessResponse,this.genFailureResponse);
        },this);
    },
    genSuccessResponse:function(response){
        WtfComMsgBox([this.label,response.msg],response.success*2+1);
        for(var i=0;i<this.recArr.length;i++){
             var ind=this.grid.getStore().indexOf(this.recArr[i])
             var num= ind%2;
             WtfGlobal.highLightRowColor(this.grid,this.recArr[i],false,num,2,true);
        }
        if(response.success){
            (function(){
                this.userds.load({params:{start:0,limit:this.pP.combo.value,ss : this.quickPanelSearch.getValue()}});
            }).defer(WtfGlobal.gridReloadDelay(),this);
        }
    },
    genFailureResponse:function(response){
         for(var i=0;i<this.recArr.length;i++){
             var ind=this.grid.getStore().indexOf(this.recArr[i])
             var num= ind%2;
             WtfGlobal.highLightRowColor(this.grid,this.recArr[i],false,num,2,true);
        }
        var msg=WtfGlobal.getLocaleText("");  //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox(['Alert',msg],2);
    }
});
 
