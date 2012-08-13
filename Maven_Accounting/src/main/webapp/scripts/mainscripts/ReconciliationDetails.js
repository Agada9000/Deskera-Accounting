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
Wtf.account.ReconciliationDetails=function(config){
    this.total=[0,0];
    this.openingbalance=[0];
    this.clearingBalance=0;
    this.summaryL = new Wtf.ux.grid.GridSummary();
    this.lsm=new Wtf.grid.CheckboxSelectionModel();
    this.rsm=new Wtf.grid.CheckboxSelectionModel();
    this.summaryR = new Wtf.ux.grid.GridSummary();
    this.reconRec=config.reconRec;
    this.accRec = Wtf.data.Record.create ([
        {name:'accountname',mapping:'accname'},
        {name:'accountid',mapping:'accid'}
    ]);

    this.accStore = new Wtf.data.Store({
//        url: Wtf.req.account+'CompanyManager.jsp',
        url : "ACCAccount/getAccountsForCombo.do",
        baseParams:{
        	nondeleted:true,
            mode:2,
            group:"9"
        },
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        },this.accRec)
    });

   this.LedgerRec = new Wtf.data.Record.create([
        {name: 'd_date',type:'date'},
        {name: 'd_accountname'},
        {name: 'd_entryno'},
        {name: 'd_journalentryid'},
        {name: 'd_amount'},
        {name: 'c_date',type:'date'},
        {name: 'c_accountname'},
        {name: 'c_entryno'},
        {name: 'c_journalentryid'},
        {name: 'c_amount'}
    ]);

    this.LStore = new Wtf.data.Store({
        reader: new Wtf.data.KwlJsonReader({
            root: "left"
        },this.LedgerRec),
//        url: Wtf.req.account+'CompanyManager.jsp',
        url:"ACCReports/getReconciliationData.do",
        baseParams:{
            mode:67
        }
    });

    this.RStore = new Wtf.data.Store({
        reader: new Wtf.data.KwlJsonReader({
            root: "right"
        },this.LedgerRec),
//        url: Wtf.req.account+'CompanyManager.jsp',
        url:"ACCReports/getReconciliationData.do",
        baseParams:{
            mode:67
        }
    });

    this.rowNo=new Wtf.grid.RowNumberer();
    this.rgridcm= new Wtf.grid.ColumnModel([this.lsm,{ 
        header: WtfGlobal.getLocaleText("acc.bankReconcile.gridDate"),  //"Date",
        dataIndex: 'c_date',
        align:'center',
        width:40,
        renderer:WtfGlobal.onlyDateRenderer
    },{
        header: WtfGlobal.getLocaleText("acc.bankReconcile.gridAccountPayee"),  //"Account Receivable",
        dataIndex: 'c_accountname',
        renderer:this.formatAccountName,
        summaryRenderer:function(){return '<div class="grid-summary-common">'+WtfGlobal.getLocaleText("acc.common.total")+'</div>';}  //Total  
    },{
        header: WtfGlobal.getLocaleText("acc.bankReconcile.gridJournalFolio"),  //"Journal Folio (J/F)",
        dataIndex: 'c_entryno',
        width:50,
        renderer:WtfGlobal.linkRenderer
    },{
        header: WtfGlobal.getLocaleText("acc.bankReconcile.gridCreditAmount"),  //"Credit Amount",
        align:'right',
        dataIndex: 'c_amount',
        renderer:WtfGlobal.currencyRenderer,
        width:50,
        summaryType:'sum',
        pdfwidth:125,
        summaryRenderer:WtfGlobal.currencySummaryRenderer
    }]);

    this.lgridcm= new Wtf.grid.ColumnModel([this.rsm,{ 
        header: WtfGlobal.getLocaleText("acc.bankReconcile.gridDate"),  //"Date",
        dataIndex: 'd_date',
        align:'center',
        width:40,
        renderer:WtfGlobal.onlyDateRenderer
    },{
        header: WtfGlobal.getLocaleText("acc.bankReconcile.gridAccountPayee"),  //"Account Payee",
        dataIndex: 'd_accountname',
        renderer:this.formatAccountName,
        summaryRenderer:function(){return '<div class="grid-summary-common">'+ WtfGlobal.getLocaleText("acc.common.total") +'</div>';}
    },{
        header: WtfGlobal.getLocaleText("acc.bankReconcile.gridJournalFolio"),  //"Journal Folio (J/F)",
        dataIndex: 'd_entryno',
        width:50,
        renderer:WtfGlobal.linkRenderer
    },{
        header:WtfGlobal.getLocaleText("acc.bankReconcile.gridDebitAmount"),  // "Debit Amount",
        align:'right',
        dataIndex: 'd_amount',
        renderer:WtfGlobal.currencyRenderer,
        width:50,
        summaryType:'sum',
        pdfwidth:125,
        summaryRenderer:WtfGlobal.currencySummaryRenderer
    }]);
    this.lgrid = new Wtf.grid.GridPanel({
        stripeRows :true,
        store: this.LStore,
        height:500,
        style:'padding:2px',
        cm: this.lgridcm,
        sm:this.lsm,
        loadMask : true,
        plugins:[this.summaryL],
        viewConfig: {
            forceFit:true,
            emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
        },
        tbar:['<b>'+WtfGlobal.getLocaleText("acc.bankReconcile.deposit&otherCredits")+'</b>']  //Deposits and Other Credits</b>']
    });

    this.rgrid = new Wtf.grid.GridPanel({
        stripeRows :true,
        store: this.RStore,
        height:500,
        style:'padding:2px',
        cm: this.rgridcm,
        sm:this.rsm,
        loadMask : true,
        plugins:[this.summaryR],
        viewConfig: {
            forceFit:true,
            emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
        },
        tbar:['<b>'+WtfGlobal.getLocaleText("acc.bankReconcile.checks&payments")+'</b>']  //Checks and Payments</b>']
    });

    this.LStore.on('load', function() {
        if(this.LStore.getCount()<1) {
            this.lgrid.getView().emptyText=WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
            this.lgrid.getView().refresh();
        }
    }, this);
    this.RStore.on('load', function() {
        if(this.RStore.getCount()<1) {
            this.rgrid.getView().emptyText=WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
            this.rgrid.getView().refresh();
        }
    }, this);

    this.cmbAccount=new Wtf.form.ComboBox({
        fieldLabel:WtfGlobal.getLocaleText("acc.bankReconcile.account"),  //'Account Name',
        name:'accountid',
        store:this.accStore,
        valueField:'accountid',
        displayField:'accountname',
        mode: 'local',
        width:150,
        hiddenName:'accountid',
        emptyText:'Select Account',
        allowBlank:false,
        forceSelection:true,
        triggerAction:'all'
    });
    this.startDate=new Wtf.form.DateField({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.from"),  //'From',
        name:'stdate',
        format:WtfGlobal.getOnlyDateFormat(),
        value:(this.reconRec==""?this.getDates(true):this.reconRec.startdate)
    });
    this.endDate=new Wtf.form.DateField({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.to"),  //'To',
        format:WtfGlobal.getOnlyDateFormat(),
        name:'enddate',
        value:(this.reconRec==""?this.getDates(false):this.reconRec.statementdate)
    });

    this.tplSummary=new Wtf.XTemplate(
        '<div class="currency-view">',
        '<table width="100%">',
        '<tpl if="'+(!this.isOrder)+'">',
        '<tr><td><b>'+ WtfGlobal.getLocaleText("acc.bankReconcile.endingBalance") +'</b></td><td text-align=right>{endingbalance}</td></tr>',
        '<tr><td><b>'+ WtfGlobal.getLocaleText("acc.bankReconcile.clearingBalance") +'</b></td><td align=right>{clearingbalance}</td></tr>',
        '</table>',
        '<table width="100%">',
        '<tr><td><b>'+ WtfGlobal.getLocaleText("acc.bankReconcile.difference") +'</b></td><td align=right>{difference}</td></tr>',
        '</table>',
        '</tpl>',    
        '</div>'
    );

    this.southEastCalTemp=new Wtf.Panel({
        border:false,
        baseCls:'tempbackgroundview',
        html:this.tplSummary.apply({endingbalance:WtfGlobal.currencyRenderer(0),clearingbalance:WtfGlobal.currencyRenderer(0),difference:WtfGlobal.currencyRenderer(0)})
    });
    this.centerPanel=new Wtf.Panel({
        region:'south',
        height:150,
        border:false,
        style:'padding:10px',
        layout:'border',
        items:[{
            region:'center',
            border:false,
            layout:'border',
            items:[{
                region:'north',
                border:false,
                buttons: [{
                    text: WtfGlobal.getLocaleText("acc.bankReconcile.modifyEndBalance"),  //'Modify Ending Balance    ',
                    scope: this,
                    handler: this.editReconciliationWindow.createDelegate(this)
                }]
            },{
                region:'center',
                border:false,
                buttons: [{
                    text: WtfGlobal.getLocaleText("acc.bankReconcile.clearData"),  //'Clear Data',
                    scope: this,
                    handler: this.clearData.createDelegate(this)
                }]
            },{
                region:'south',
                border:false,
                buttons: [{
                    text: WtfGlobal.getLocaleText("acc.bankReconcile.reconcile&save"),  //'Reconcile and Save',			// 20177
                    scope: this,
                    handler: this.saveData.createDelegate(this)
                }]
           
            }]
        },{
            region:'east',
            width:350,
            layout:'form',
            cls:'bckgroundcolor',
            bodyStyle:'padding:10px',
            labelWidth:70,
            items:this.southEastCalTemp
        },{
            region:'west',
            width:350,
            border:false,
            html:"<b>"+ WtfGlobal.getLocaleText("acc.bankReconcile.reconcileDesc") +"</b>" //*Selected transactions will be reconciled<b>"
        }]
    });

   Wtf.apply(this,{
        items:[{
            layout:'border',
            border:false,
            scope:this,
            autoScroll:true,
            items:[{
                region:'center',
                autoScroll:true,
                border:false,
                style:'padding:10px',
                defaults:{layout:'fit',split:true, border:false},
                layout:'column',
                items:[{
                    layout:'fit',
                    columnWidth:0.5,
                    defaults:{border:false},
                    items:this.lgrid
                },{
                    layout:'fit',
                    columnWidth:0.5,
                   defaults:{border:false},
                    items:this.rgrid
                }]
            }, this.centerPanel],
            tbar:[WtfGlobal.getLocaleText("acc.bankReconcile.account"),this.cmbAccount,WtfGlobal.getLocaleText("acc.common.from"),this.startDate,WtfGlobal.getLocaleText("acc.common.to"),this.endDate,'-',{
                xtype:'button',
                text:WtfGlobal.getLocaleText("acc.common.fetch"),  //'Fetch',
                tooltip:WtfGlobal.getLocaleText("acc.bankReconcile.fetchTT"),  //"Select a time period to view corresponding ledger records.",
                iconCls:'accountingbase fetch',
                scope:this,
                handler:this.onClick
            },'->',{
                xtype:'button',
                text:WtfGlobal.getLocaleText("acc.bankReconcile.viewPrevious"),  //'View Previous Reconcile(s)',
                tooltip:WtfGlobal.getLocaleText("acc.bankReconcile.viewPreviousTT"),  //"Previous Reconcile",
                iconCls:'accountingbase fetch',
                scope:this,
                handler:this.viewReport
            }]
        }]
    },config)

    Wtf.account.ReconciliationDetails.superclass.constructor.call(this,config);
    this.addEvents({
       'journalentry':true
    });
    this.lgrid.on('rowclick',this.onLRowClick, this);
    this.rgrid.on('rowclick',this.onRRowClick, this);
    this.lgrid.getSelectionModel().on('selectionchange',this.getSelectedRowAmount,this);
    this.rgrid.getSelectionModel().on('selectionchange',this.getSelectedRowAmount,this);
    this.rgrid.on('rowclick',this.onRRowClick, this);
    this.accStore.on('load',function(){this.showLedger(config.accountID);},this);
    this.accStore.load();
}

Wtf.extend( Wtf.account.ReconciliationDetails,Wtf.Panel,{
    getSelectedRowAmount:function(){
        this.clearingBalance=0;
       var larr=this.lgrid.getSelectionModel().getSelections();
       for(var i=0;i<this.lsm.getCount();i++){
           this.clearingBalance+=larr[i].data["d_amount"];
       }
       var rarr=this.rgrid.getSelectionModel().getSelections();
       for(i=0;i<this.rsm.getCount();i++){
           this.clearingBalance-=rarr[i].data["c_amount"];
       }
       this.clearingBalance+=this.openingbalance[0];
       this.tplSummary.overwrite(this.southEastCalTemp.body,{endingbalance:WtfGlobal.currencyRenderer(this.reconRec.endingbalance),clearingbalance:WtfGlobal.currencyRenderer(this.clearingBalance),difference:WtfGlobal.currencyRenderer(this.reconRec.endingbalance-this.clearingBalance)});
    },
    clearData:function(){
        this.startDate.setValue(this.getDates(true));
        this.endDate.setValue(this.getDates(false));
        this.lgrid.getStore().removeAll();
        this.rgrid.getStore().removeAll();
        this.tplSummary.overwrite(this.southEastCalTemp.body,{endingbalance:WtfGlobal.currencyRenderer(0),clearingbalance:WtfGlobal.currencyRenderer(0),difference:WtfGlobal.currencyRenderer(0)});
    }, 
    editReconciliationWindow:function(){
        callReconciliationWindow(null,this.reconRec)
    },
    viewReport:function(){
        callBankReconciliationReport('recwin'+this.cmbAccount.getValue(),this.cmbAccount.getValue())
    }, 
    updateData:function(accid,startdate,enddate,endingbalance){
        this.reconRec.endingbalance=endingbalance;
        this.cmbAccount.setValue(accid);
        this.startDate.setValue(startdate);
        this.endDate.setValue(enddate); 
        this.accStore.on('load',function(){this.showLedger(accid);},this);
        this.accStore.load();
    }, 
    formatAccountName:function(val,m,rec){
         if(val=="Total"){return "<b>"+val+"</b>";}
         else{return val}
    },
    onClick:function(){
        this.accountID=this.cmbAccount.getValue();
        this.fetchLedger();
    },
    fetchLedger:function(){
        var sDate=this.startDate.getValue();
        var eDate=this.endDate.getValue();

        if(sDate>eDate){
            WtfComMsgBox(1,2);
            return;
        }
        if(this.accountID&&this.accountID.length>0){
          var  params={
              mode:67,
                    accountid:this.accountID,
                    stdate:WtfGlobal.convertToGenericDate(this.startDate.getValue()),
                    enddate:WtfGlobal.convertToGenericDate(this.endDate.getValue())
                }

           Wtf.Ajax.requestEx({
               //url:Wtf.req.account+'CompanyManager.jsp',
                url:"ACCReports/getReconciliationData.do", 
                params:params
            }, this, this.successCallback, this.failureCallback);
        }
    },
    successCallback:function(response){
        if(response.success){
            this.total=response.data.total;
            this.openingbalance=response.data.openingbalance;
            this.lgrid.store.loadData(response.data);
            this.rgrid.store.loadData(response.data);
           this.lsm.selectAll();
           this.rsm.selectAll();
        }
    },

    failureCallback:function(response){

    },
    getDates:function(start){
        var d=Wtf.serverDate;
        var monthDateStr=d.format('M d');
        if(Wtf.account.companyAccountPref.fyfrom)
            monthDateStr=Wtf.account.companyAccountPref.fyfrom.format('M d');
        var fd=new Date(monthDateStr+', '+d.getFullYear()+' 12:00:00 AM');
        if(d<fd)
            fd=new Date(monthDateStr+', '+(d.getFullYear()-1)+' 12:00:00 AM');
        if(start)
            return fd;
        return fd.add(Date.YEAR, 1).add(Date.DAY, -1);
    },

    showLedger:function(accid){
        var i=this.accStore.find("accountid",accid);
        if(i>=0){
            this.cmbAccount.setValue(accid);
            this.accountID=accid;
            this.fetchLedger();
        }
    },

    onRender:function(config){
         Wtf.account.ReconciliationDetails.superclass.onRender.call(this,config);
    },

    onLRowClick:function(g,i,e){
        e.stopEvent();
        var el=e.getTarget("a");
        if(el==null)return;
        var rec=this.LStore.getAt(i);
        var jid=rec.data['d_journalentryid'];        
        this.fireEvent('journalentry',jid,true);
    },
    onRRowClick:function(g,i,e){
        e.stopEvent();
        var el=e.getTarget("a");
        if(el==null)return;
        var rec=this.RStore.getAt(i);
         var   jid=rec.data['c_journalentryid'];
        this.fireEvent('journalentry',jid,true);
    },    

    dateRenderer:function(v){
        if(v) return v.format('Y M d');
        return "";
    },
    getReconciliationDetails:function(isleft){
         var arr=[];
        var len=(isleft?this.lgrid.getStore().getCount():this.rgrid.getStore().getCount());
        for(var i=0;i<len;i++){
            var isSelected=(isleft?this.lsm.isSelected(i):this.rsm.isSelected(i));
            if(isSelected)
                arr.push(i);
            }
            var grid=(isleft?this.lgrid:this.rgrid);
        return WtfGlobal.getJSONArray(grid,true,arr);
      
    },
    saveData:function(){
        var ldetails=this.getReconciliationDetails(true);
        var rdetails=this.getReconciliationDetails(false);
        if(ldetails=="[]"&&rdetails=="[]"){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.bankReconcile.msg1")], 2);
            return;
        }
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.bankReconcile.save"),WtfGlobal.getLocaleText("acc.bankReconcile.msg2"),function(btn){
            if(btn!="yes") { return; }
            this.msg= WtfComMsgBox(27,4,true);
            var rec=[];
            rec.d_details=ldetails;
            rec.c_details=rdetails;
            rec.mode=56;
            rec.startdate=WtfGlobal.convertToGenericDate(this.startDate.getValue());
            rec.enddate=WtfGlobal.convertToGenericDate(this.endDate.getValue());
            rec.endingbalance=this.reconRec.endingbalance;
            rec.clearingbalance=this.clearingBalance;
            rec.accid=this.cmbAccount.getValue();
            Wtf.Ajax.requestEx({
//                url: Wtf.req.account+'CompanyManager.jsp',
                url: "ACCReconciliation/saveBankReconciliation.do",
                params: rec
            },this,this.genSuccessResponse,this.genFailureResponse);
        },this);
    },
     genSuccessResponse:function(response){
         if(response.success)
             WtfComMsgBox([WtfGlobal.getLocaleText("acc.dashboard.bankReconciliation"),response.msg],response.success*2+1);
    },

    genFailureResponse:function(response){
        var msg= WtfGlobal.getLocaleText("acc.common.msg1");  //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    }  
});
