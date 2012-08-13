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

Wtf.account.Ledger=function(config){
    this.summary = new Wtf.ux.grid.GridSummary();
    this.accRec = Wtf.data.Record.create ([
        {name:'accountname',mapping:'accname'},
        {name:'accountid',mapping:'accid'}
    ]);

    this.uPermType=Wtf.UPerm.fstatement;
    this.permType=Wtf.Perm.fstatement;
    this.exportPermType=this.permType.exportdataledger;
     this.printPermType=this.permType.printledger
    this.accStore = new Wtf.data.Store({
//        url: Wtf.req.account+'CompanyManager.jsp',
        url : "ACCAccount/getAccountsForCombo.do",
        baseParams:{
            mode:2,
             nondeleted:true
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

            this.LedgerStore = new Wtf.data.Store({
                reader: new Wtf.data.KwlJsonReader({
                    root: "data"
                },this.LedgerRec),
//                url: Wtf.req.account+'CompanyManager.jsp',
                url:"ACCReports/getLedger.do",
                baseParams:{
                    mode:61
                }
            });
            
            this.rowNo=new Wtf.grid.RowNumberer();
            this.gridcm= new Wtf.grid.ColumnModel([this.rowNo,
            {
                header: WtfGlobal.getLocaleText("acc.inventoryList.date"),  //"Date",
                dataIndex: 'd_date',
                align:'center',
                width:40,
                pdfwidth:110,
                renderer:WtfGlobal.onlyDateRenderer
            },{
                header: WtfGlobal.getLocaleText("acc.ledger.gridAccount"),  //"Account",
                dataIndex: 'd_accountname',
                renderer:this.formatAccountName,
                pdfwidth:100,
                summaryRenderer:function(){return '<div class="grid-summary-common">'+WtfGlobal.getLocaleText("acc.common.total")+'</div>'}
            },{
                header: WtfGlobal.getLocaleText("acc.ledger.gridJournalFolio"),  //"Journal Folio (J/F)",
                dataIndex: 'd_entryno',
                width:50,
                pdfwidth:100,
                renderer:WtfGlobal.linkRenderer
            },{
                header: WtfGlobal.getLocaleText("acc.ledger.gridDebitAmount"),  //"Debit Amount",
                dataIndex: 'd_amount',
                align:'right',
                renderer:WtfGlobal.currencyRenderer,
                width:50,
                summaryType:'sum',
                pdfwidth:125,
                summaryRenderer:WtfGlobal.currencySummaryRenderer
            },{
                width:1,
                renderer:this.showSeperator
            },{
                header: WtfGlobal.getLocaleText("acc.inventoryList.date"),  //"Date",
                dataIndex: 'c_date',
                align:'center',
                width:40,
                renderer:WtfGlobal.onlyDateRenderer
            },{
                header: WtfGlobal.getLocaleText("acc.ledger.gridAccount"),  //"Account",
                dataIndex: 'c_accountname',
                renderer:this.formatAccountName,
                summaryRenderer:function(){return '<div class="grid-summary-common">'+WtfGlobal.getLocaleText("acc.common.total")+'</div>'}
            },{
                header: WtfGlobal.getLocaleText("acc.ledger.gridJournalFolio"),  //"Journal Folio (J/F)",
                dataIndex: 'c_entryno',
                width:50,
                renderer:WtfGlobal.linkRenderer
            },{
                header: WtfGlobal.getLocaleText("acc.ledger.gridCreditAmount"),  //"Credit Amount",
                align:'right',
                dataIndex: 'c_amount',
                renderer:WtfGlobal.currencyRenderer,
                width:50,
                summaryType:'sum',
                pdfwidth:125,
                summaryRenderer:WtfGlobal.currencySummaryRenderer
            }]);
            this.grid = new Wtf.grid.GridPanel({
                stripeRows :true,
                store: this.LedgerStore,
                cm: this.gridcm,
                border : false,
                loadMask : true,
                plugins:[this.summary],
                viewConfig: {
                    forceFit:true,
                    emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
                },
                tbar:[WtfGlobal.getLocaleText("acc.trial.debit"),'->',WtfGlobal.getLocaleText("acc.trial.credit")]
            });
            this.LedgerStore.on("load", function(store){
                if(store.getCount()==0){
                    this.grid.getView().emptyText=WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
                    this.grid.getView().refresh();

                            if(this.expButton)this.expButton.disable();
                    if(this.printButton)this.printButton.disable();
                }else{

                //    if(!WtfGlobal.EnableDisable(this.uPermType, this.exportPermTypee)){
                    if(this.expButton)this.expButton.enable();//}
                   // if(!WtfGlobal.EnableDisable(this.uPermType, this.printPermType)){
                    if(this.printButton)this.printButton.enable();//}
                }
            },this);
            this.cmbAccount=new Wtf.form.ComboBox({
                fieldLabel:WtfGlobal.getLocaleText("acc.ledger.accName"),  //'Account Name',
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
                readOnly:true,
                value:this.getDates(true)
            });
            this.endDate=new Wtf.form.DateField({
                fieldLabel:WtfGlobal.getLocaleText("acc.common.to"),  //'To',
                format:WtfGlobal.getOnlyDateFormat(),
                readOnly:true,
                name:'enddate',
                value:this.getDates(false)
            });
            var btnArr=[];

            btnArr.push(
            		WtfGlobal.getLocaleText("acc.ledger.accName"),this.cmbAccount,
            		WtfGlobal.getLocaleText("acc.common.from"),this.startDate,
            		WtfGlobal.getLocaleText("acc.common.to"),this.endDate,
                '-',{
                    xtype:'button',
                    text:WtfGlobal.getLocaleText("acc.common.fetch"),  //'Fetch',
                    tooltip:WtfGlobal.getLocaleText("acc.bankReconcile.fetchTT"),  //"Select a time period to view corresponding ledger records.",
                    iconCls:'accountingbase fetch',
                    scope:this,
                    handler:this.onClick
                }
            );
            if(!WtfGlobal.EnableDisable(this.uPermType, this.exportPermType)){
                btnArr.push("-",this.expButton=new Wtf.exportButton({
                	text:WtfGlobal.getLocaleText("acc.common.export"),
                    obj:this,
                    tooltip :WtfGlobal.getLocaleText("acc.common.exportTT"),
                    disabled :true,
                    params:{ stdate:WtfGlobal.convertToGenericDate(this.getDates(true)),
                             enddate:WtfGlobal.convertToGenericDate(this.getDates(false)),
                             accountid:this.accountID||config.accountID,
                             name: "Ledger"
                    },
                    menuItem:{csv:true,pdf:true,rowPdf:false},
                    get:117
                }));
            }

            if(!WtfGlobal.EnableDisable(this.uPermType, this.printPermType)){
                btnArr.push("-",this.printButton=new Wtf.exportButton({ 
                    obj:this,
                    text:WtfGlobal.getLocaleText("acc.common.print"),
                    tooltip :WtfGlobal.getLocaleText("acc.common.printTT"),
                    disabled :true,
                    params:{ stdate:WtfGlobal.convertToGenericDate(this.getDates(true)),
                             enddate:WtfGlobal.convertToGenericDate(this.getDates(false)),
                             accountid:this.accountID||config.accountID,
                             name: WtfGlobal.getLocaleText("acc.ledger.tabTitle")
                    },
                    label:"Ledger",
                    menuItem:{print:true},
                    get:117
                }));
            }
    Wtf.apply(this,{
        items:[{
            layout:'border',
            border:false,
            scope:this,
            items:[{
                region:'center',
                layout:'fit',
                border:false,
                items:this.grid
            }],
            tbar:btnArr
        }]
    },config)

    Wtf.account.Ledger.superclass.constructor.call(this,config);
    this.addEvents({
       'journalentry':true
    });
    this.grid.on('rowclick',this.onRowClick, this);
    this.accStore.on('load',function(){this.showLedger(config.accountID);},this);
    this.accStore.load();
}

Wtf.extend( Wtf.account.Ledger,Wtf.Panel,{
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
            this.LedgerStore.load({
                params:{
                    accountid:this.accountID,
                    stdate:WtfGlobal.convertToGenericDate(this.startDate.getValue()),
                    enddate:WtfGlobal.convertToGenericDate(this.endDate.getValue())
                }
            });
            if(!WtfGlobal.EnableDisable(this.uPermType, this.exportPermType)){
                this.expButton.setParams({
                    accountid:this.accountID,
                    stdate:WtfGlobal.convertToGenericDate(this.startDate.getValue()),
                    enddate:WtfGlobal.convertToGenericDate(this.endDate.getValue()),
                    name: WtfGlobal.getLocaleText("acc.ledger.tabTitle")+"-"+this.cmbAccount.getRawValue()
                });
            }
            if(!WtfGlobal.EnableDisable(this.uPermType, this.printPermType)){
                this.printButton.setParams({
                    accountid:this.accountID,
                    stdate:WtfGlobal.convertToGenericDate(this.startDate.getValue()),
                    enddate:WtfGlobal.convertToGenericDate(this.endDate.getValue()),
                    name: WtfGlobal.getLocaleText("acc.ledger.tabTitle")+"-"+this.cmbAccount.getRawValue()
                });
            }
        }
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
         Wtf.account.Ledger.superclass.onRender.call(this,config);
    },

    onRowClick:function(g,i,e){
        e.stopEvent();
        var el=e.getTarget("a");
        if(el==null)return;
        var rec=this.LedgerStore.getAt(i);
        var jid=rec.data['d_journalentryid'];
        if(!jid||jid.length<=0)
            jid=rec.data['c_journalentryid'];
        this.fireEvent('journalentry',jid,true);
    },

    getDates:function(start){
        var d=new Date();
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

    dateRenderer:function(v){
        if(v) return v.format('Y M d');
        return "";
    },
    
    showSeperator:function(){
        return '<div style="margin:-5px"><img src="../../images/header.gif"></div>';
    }
});
