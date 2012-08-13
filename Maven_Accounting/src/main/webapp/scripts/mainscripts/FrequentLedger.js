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

Wtf.account.FrequentLedger=function(config){
    this.uPermType=Wtf.UPerm.fstatement;
    this.permType=Wtf.Perm.fstatement;
     this.exportPermType=(this.cash?this.permType.exportdatacashbook:this.permType.exportdatabankbook);
     this.printPermType=(this.cash?this.permType.printcashbook:this.permType.printbankbook);
        this.group=config.group;
        this.accountID=(config.cash?Wtf.account.companyAccountPref.cashaccount:'');
        this.summary = new Wtf.ux.grid.GridSummary();
        this.accRec = Wtf.data.Record.create ([
            {name:'accountname',mapping:'accname'},
            {name:'accountid',mapping:'accid'}
        ]);

        this.accStore = new Wtf.data.Store({
//            url: Wtf.req.account+'CompanyManager.jsp',
            url : "ACCAccount/getAccountsForCombo.do",
            baseParams:{
                 mode:2,
                 group:config.group,
                 nondeleted: true
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.accRec)
        });

           this.Record = new Wtf.data.Record.create([
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

            this.Store = new Wtf.data.Store({
                reader: new Wtf.data.KwlJsonReader({
                    root: "data"
                },this.Record),
//                url: Wtf.req.account+'CompanyManager.jsp',
                url:"ACCReports/getLedger.do",
                baseParams:{
                    mode:61
                }
            });

            this.rowNo=new Wtf.KWLRowNumberer();
            this.gridcm= new Wtf.grid.ColumnModel([this.rowNo,{
                header: WtfGlobal.getLocaleText("acc.bankBook.date"),  //"Date",
                dataIndex: 'c_date',
                pdfwidth:100,
                align:'center',
                renderer:WtfGlobal.onlyDateRenderer
            },{
                header: WtfGlobal.getLocaleText("acc.bankBook.acc"),  //"Account",
                dataIndex: 'c_accountname',
                pdfwidth:100,
                renderer:this.formatAccountName
            },{
                header: WtfGlobal.getLocaleText("acc.bankBook.JF"),  //"Journal Folio (J/F)",
                dataIndex: 'c_entryno',
                pdfwidth:100,
                renderer:WtfGlobal.linkRenderer
            },{
                header: WtfGlobal.getLocaleText("acc.bankBook.debitAmt"),  //"Debit Amount",
                dataIndex: 'd_amount',
                align:'right',
                pdfwidth:100,
                renderer:WtfGlobal.currencyRenderer
            },{
                header: WtfGlobal.getLocaleText("acc.bankBook.crebitAmt"),  //"Credit Amount",
                dataIndex: 'c_amount',
                align:'right',
                pdfwidth:100,
                renderer:WtfGlobal.currencyRenderer
            }]);
            this.grid = new Wtf.grid.GridPanel({
                stripeRows :true,
                store: this.Store,
                cm: this.gridcm,
                title:(this.group==9?"<center>"+WtfGlobal.getLocaleText("acc.bankBook.msg1")+"</center>":""),
                border : false,
                loadMask : true,
                viewConfig: {
                    forceFit:true,
                    emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
                }

            });
            this.cmbAccount=new Wtf.form.ComboBox({
                fieldLabel:WtfGlobal.getLocaleText("acc.bankBook.bankAccName"),  //'Bank Account Name',
                name:'accountid',
                store:this.accStore,
                width:150,
                valueField:'accountid',
                displayField:'accountname',
                mode: 'local',
                hiddenName:'accountid',
                allowBlank:false,
                emptyText:WtfGlobal.getLocaleText("acc.bankBook.sel"),  //'Select Bank Account',
                typeAhead: true,
                forceSelection:true,
                triggerAction:'all'
               
            });
            this.startDate=new Wtf.form.DateField({
                fieldLabel:WtfGlobal.getLocaleText("acc.common.from"),  //'From',
                name:'stdate',
                format:WtfGlobal.getOnlyDateFormat(),
                value:this.getDates(true)
            });
            this.endDate=new Wtf.form.DateField({
                fieldLabel:WtfGlobal.getLocaleText("acc.common.to"),  //'To',
                format:WtfGlobal.getOnlyDateFormat(),
                name:'enddate',
                value:this.getDates(false)
            });
    var tabTitle = config.title;
    var btnArr = [];
    if(config.cash==false){
        btnArr.push(WtfGlobal.getLocaleText("acc.bankBook.bankAccName"));
        btnArr.push(this.cmbAccount,'-');
    }
    btnArr.push(WtfGlobal.getLocaleText("acc.common.from"),this.startDate,WtfGlobal.getLocaleText("acc.common.to"),this.endDate,'-',{
        xtype:'button',
        text:WtfGlobal.getLocaleText("acc.common.fetch"),  //'Fetch',
        tooltip:config.cash?WtfGlobal.getLocaleText("acc.bankBook.fetchTT"):WtfGlobal.getLocaleText("acc.bankBook.fetchTT1"),
        iconCls:'accountingbase fetch',
        scope:this,
        handler:this.onClick
    });
//    if(config.cash==true){   
    if(!WtfGlobal.EnableDisable(this.uPermType, this.exportPermType)){
      btnArr.push("-",this.expButton=new Wtf.exportButton({
        obj:this,
        id:"exportReports"+config.helpmodeid,
        text:WtfGlobal.getLocaleText("acc.common.export"),
        tooltip :WtfGlobal.getLocaleText("acc.common.exportTT"), //'Export report details',
        disabled :true,
        params:{ stdate:WtfGlobal.convertToGenericDate(this.getDates(true)),
                 enddate:WtfGlobal.convertToGenericDate(this.getDates(false)),
                 accountid:this.accountID||config.accountID
        },
        menuItem:{csv:true,pdf:true,rowPdf:false},
        get:115
      }));
    }

    if(!WtfGlobal.EnableDisable(this.uPermType, this.printPermType)){
      btnArr.push("-",this.printButton=new Wtf.exportButton({
        text:WtfGlobal.getLocaleText("acc.bankBook.print"), //"Print",
        obj:this,
        tooltip :WtfGlobal.getLocaleText("acc.bankBook.printTT"), //'Print report details',
        disabled :true,
        params:{ stdate:WtfGlobal.convertToGenericDate(this.getDates(true)),
                 enddate:WtfGlobal.convertToGenericDate(this.getDates(false)),
                 accountid:this.accountID||config.accountID,
                 name: tabTitle
        },
        label: tabTitle,
        menuItem:{print:true},
        get:115 
      }));
    }
    btnArr.push("->");
    btnArr.push(getHelpButton(this,config.helpmodeid));
//    }
    Wtf.apply(this,{
        items:[{
            layout:'border',
            border:false,
            scope:this,
            items:[{
                region:'center',
                layout:'fit',
                border:false,
                items:this.grid,
                tbar:btnArr
            }]
        }]

    },config)

    Wtf.account.FrequentLedger.superclass.constructor.call(this,config);
    this.addEvents({
       'journalentry':true
    });
    this.grid.on('rowclick',this.onRowClick, this);
    this.accStore.on('load',function(){this.showLedger(config.accountID);},this);
    this.accStore.load();
    this.Store.on('load',this.callOnLoad,this);
}

Wtf.extend( Wtf.account.FrequentLedger,Wtf.Panel,{
    formatAccountName:function(val,m,rec){
         if(val=="Total"){return "<b>"+val+"</b>";}
         else{return val}
    },
    
    onClick:function(){
        if(this.cash==false){
            this.accountID=this.cmbAccount.getValue();
        }
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
            var newFilterParams = {
                accountid:this.accountID,
                stdate:WtfGlobal.convertToGenericDate(this.startDate.getValue()),
                enddate:WtfGlobal.convertToGenericDate(this.endDate.getValue())
            };

            this.Store.load({
                params: newFilterParams
            });
//            if(this.cash==true)
            if(this.expButton) {
                this.expButton.setParams(newFilterParams);
            }
            if(this.printButton) {
                this.printButton.setParams(newFilterParams);
            }
        }
    },

    callOnLoad:function(){
        if(this.Store.getCount()==0){
            this.grid.getView().emptyText=WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
            this.grid.getView().refresh();
            if(this.expButton)this.expButton.disable();
            if(this.printButton)this.printButton.disable();
        }else{
            if(this.expButton)this.expButton.enable();
            if(this.printButton)this.printButton.enable();
        }
        if(this.group==9){
            var i=this.accStore.find("accountid",this.accountID);
            if(i>=0)
                this.grid.setTitle(WtfGlobal.getLocaleText("acc.bankBook.bankBookOf")+" "+this.accStore.getAt(i).data["accountname"]);
        }
        this.Store.each(function(record){
            if(record.data['d_accountname']!=''){
                record.set('c_date', record.data['d_date']);
                record.set('c_accountname',record.data['d_accountname']);
                record.set('c_entryno', record.data['d_entryno']);
                record.set('c_journalentryid', record.data['d_journalentryid']);
            }
        }, this);
        this.Store.commitChanges();
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
         Wtf.account.FrequentLedger.superclass.onRender.call(this,config);
         this.fetchLedger();
    },

    onRowClick:function(g,i,e){
        e.stopEvent();
        var el=e.getTarget("a");
        if(el==null)return;
        var rec=this.Store.getAt(i);
        var jid=rec.data['c_journalentryid'];
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
    }

});
