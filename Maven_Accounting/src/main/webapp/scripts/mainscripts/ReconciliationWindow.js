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
Wtf.account.ReconciliationWindow = function(config){
    this.reconRec=(config.reconRec==undefined?"":config.reconRec);
     Wtf.apply(this,{
        title:WtfGlobal.getLocaleText("acc.dashboard.bankReconciliation"),  //"Bank Reconciliation",
        buttons: [{
            text: WtfGlobal.getLocaleText("acc.common.continueBtn"),  //'Continue',
            scope: this,
            handler: this.saveData.createDelegate(this)
        },{
            text: WtfGlobal.getLocaleText("acc.common.cancelBtn"),  //'Cancel',
            scope: this,
            handler:this.closeWin.createDelegate(this)
        }]
    },config);
    Wtf.account.ReconciliationWindow.superclass.constructor.call(this, config);
    this.addEvents({
        'update':true
    });
}
Wtf.extend( Wtf.account.ReconciliationWindow, Wtf.Window, {

    onRender: function(config){
        var image="../../images/accounting_image/bank-reconciliation.jpg";
        Wtf.account.ReconciliationWindow.superclass.onRender.call(this, config);
        this.createStore();
        this.createForm();
        this.add({
            region: 'north',
            height:75,
            border: false,
            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
            html:getTopHtml(WtfGlobal.getLocaleText("acc.bankReconcile.tab"),WtfGlobal.getLocaleText("acc.bankReconcile.BRinfo"),image,false)
        },{
            region: 'center',
            border: false,
            baseCls:'bckgroundcolor',
//            layout: 'fit',
            height:220,
            items:this.Form
        });
   },
   createStore:function(){
       this.allAccountRec = new Wtf.data.Record.create([
            {name: 'accid'},
            {name: 'accname'},
            {name: 'groupid'},
            {name: 'groupname'}
//            {name: 'level'},
//            {name: 'leaf'},
//            {name: 'openbalance'},
//            {name: 'parentid'},
//            {name: 'parentname'}
        ]);
        this.allAccountStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                totalProperty:'count',
                root: "data"
            },this.allAccountRec),
//            url: Wtf.req.account +'CompanyManager.jsp',
            url : "ACCAccount/getAccountsForCombo.do",
            baseParams:{
                mode:2,
                group:"9",
                nondeleted:true
            }
        });
        this.allAccountStore.on('load',this.setAccount,this);        
        this.allAccountStore.load();
   },
   setAccount:function(){
       var value=(this.reconRec==""?"":this.reconRec.accountid)
       this.Account.setValue(value)
   },
    createForm:function(){
        this.Account= new Wtf.form.FnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.bankReconcile.account")+"*",
            hiddenName:"accountid",
            anchor:"70%",
            store: this.allAccountStore,
            valueField:'accid',
            displayField:'accname',
            allowBlank:false,
            hirarchical:true,
            emptyText:WtfGlobal.getLocaleText("acc.mp.selAcc"),  //'Please select an Account...',
            mode: 'local',
            typeAhead: true,
            forceSelection: true,
            selectOnFocus:true,
            triggerAction:'all',
            scope:this
        });
       this.startDate= new Wtf.form.DateField({
            fieldLabel:WtfGlobal.getLocaleText("acc.bankReconcile.startDate")+'*',
            format:WtfGlobal.getOnlyDateFormat(),
            value:(this.reconRec==""?this.getDates(true):this.reconRec.startdate),
            anchor:'70%',
            name:"startdate",
            allowBlank:false
        });
        this.statementDate= new Wtf.form.DateField({
            fieldLabel:WtfGlobal.getLocaleText("acc.bankReconcile.statementDate")+'*',
            format:WtfGlobal.getOnlyDateFormat(),
            value:(this.reconRec==""?this.getDates(false):this.reconRec.statementdate),
            anchor:'70%',
            name:"statementdate",
            allowBlank:false
        });
        this.Account.on('select',this.setOpeningBalance,this)
        this.startDate.on('change',this.setOpeningBalance,this)
        this.beginingBalance=new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.bankReconcile.openBalance")+' '+WtfGlobal.getCurrencySymbolForForm(),
            name: 'openingbalance',
            anchor:'70%',
            maxLength:45,
            value:(this.reconRec==""?0:this.reconRec.openingbalance),
            scope:this,
            readOnly:true,
            disabled: true	// Bug No 19950 Fixed 
        });
        this.endingBalance=new Wtf.form.NumberField({
            name:"endingbalance",
            allowBlank:false,
            fieldLabel:WtfGlobal.getLocaleText("acc.bankReconcile.endingBalance")+" "+WtfGlobal.getCurrencySymbolForForm()+"*",
            maxLength:15,
            decimalPrecision:2,
            value:(this.reconRec==""?0:this.reconRec.endingbalance),
            anchor:'70%'
        });
         this.Form=new Wtf.form.FormPanel({
            region:'north',
            height:(Wtf.isIE)?220:190,
            border:false,
            bodyStyle: 'overflow: hidden',
            items:[{
                layout:'form',
                bodyStyle: "background: transparent; padding: 20px;",
                labelWidth:150,
                border:false,
                height:300,
                items:[this.Account,this.startDate,this.statementDate,this.beginingBalance,this.endingBalance]
           }]
        });
    },
    setOpeningBalance:function(a,rec){
        Wtf.Ajax.requestEx({
//            url:Wtf.req.account+'CompanyManager.jsp',
            url:"ACCReports/getAccountOpeningBalance.do",
            params:{
                mode:68,
                stdate:WtfGlobal.convertToGenericDate(this.startDate.getValue()),
                accountid:this.Account.getValue()

            }
        }, this,this.setbalance);
    },

    setbalance:function(response){
        if(response.success)
            this.beginingBalance.setValue(response.data.openingbalance);
    },

    closeWin:function(){
         this.fireEvent('cancel',this)
         this.close();
     },

    saveData:function(){
        if(!this.Form.getForm().isValid())
                WtfComMsgBox(2,2);
        else{
            var rec=this.Form.getForm().getValues();
            callReconciliationLedger(rec)
            this.close();
        }
    },

    genSuccessResponse:function(response){
         WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.info"),response.msg],response.success*2+1);
         if(response.success) this.fireEvent('update');
        this.store.load();
    },

    genFailureResponse:function(response){
        var msg=WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    },

    getDates:function(start){
        var d=new Date();
        if(this.statementType=='BalanceSheet'){
            if(start)
                return new Date('January 1, 1970 00:00:00 AM');
            else
                return d;
        }
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
