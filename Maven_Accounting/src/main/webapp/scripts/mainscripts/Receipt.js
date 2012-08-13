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
function editReceiptExchangeRates(winid,basecurrency,foreigncurrency,exchangerate){
    function showReceiptExternalExchangeRate(btn,txt){
        if(btn == 'ok'){
             if(txt.indexOf('.')!=-1)
                 var decLength=(txt.substring(txt.indexOf('.'),txt.length-1)).length;
            if(isNaN(txt)||txt.length>15||decLength>7||txt==0){
                Wtf.MessageBox.show({
                    title: 'Exchange Rate',
                    msg: "You have entered an incorrect exchange rate. Please note:"+
                    "<br>* Only seven decimal places are allowed"+
                    "<br>* Alpha-Numeric and Special character are not allowed",
                    buttons: Wtf.MessageBox.OK,
                    icon: Wtf.MessageBox.WARNING,
//                    width: 300,
                    scope: this,
                    fn: function(){
                        if(btn=="ok"){
                            editReceiptExchangeRates(winid,basecurrency,foreigncurrency,exchangerate);
                        }
                    }
                });
            } else {
                Wtf.getCmp(winid).externalcurrencyrate=txt;
                Wtf.getCmp(winid).ApplyCurrencySymbol();
            }
        }
    }
    Wtf.MessageBox.prompt('Exchange Rate','<b>Present Exchange Rate:</b> 1 '+basecurrency+' = '+exchangerate+' '+foreigncurrency +
        '<br><b>Input New Exchange Rate :</b>', showReceiptExternalExchangeRate);
}

function openInv(isTran,isCustBill,vendor){
//	vendorid: Wtf.getCmp("account"+config.helpmodeid+this.id).getValue();			//Neeraj
	
    if(isTran && isCustBill)
        callBillingInvoice(false,null);		// Without Inventory
    else if(isTran)
        callInvoice(false,null);			// Customer Invoice
    else
        callGoodsReceipt(false,null,null,vendor);		// Vendor Invoice	
}
/*< COMPONENT USED FOR >
 *      1.Receive Payments
 *          callReceipt() --- < Receive Payments >
 *          [isReceipt:true]
 *      2.Receive Payments
 *          callBillingReceipt() --- < >
 *          [isReceipt:true, isBillReceipt:true]
 *      3.Make Payment
 *          callPayment() --- < Make Payment >
 *          [isReceipt:false]
 *
 *      4.this.appendId --- It is used when this.id is appended in the id of component. This is useful for displaying help.
 */

Wtf.account.OSDetailPanel=function(config){
	this.isReceipt=config.isReceipt;
    this.isMultiDebit=false;
    this.val="2";
    this.directPayment=config.directPayment?config.directPayment:false;
    this.invoiceRecord=config.invoiceRecord;
    this.isEdit=config.isEdit;
    this.setEditableData=false;
    this.record=config.record;
    this.id=config.id;
    this.personwin=false;
    this.symbol=null;
    this.currencyid=null;
    this.isCustBill=config.isCustBill;
    this.businessPerson=(config.isReceipt?"Customer":"Vendor");
    this.transectionName=config.isReceipt?"Receipt":"Payment";
    this.label=config.isReceipt?"Invoice":"Vendor Invoice";
    this.masterGroup=config.isReceipt?12:10;
    this.uPermType=config.isReceipt?Wtf.UPerm.customer:Wtf.UPerm.vendor;
    this.permType=config.isReceipt?Wtf.Perm.customer:Wtf.Perm.vendor;
    this.amtDue=0;
    this.pmtRec = new Wtf.data.Record.create([
        {name: 'methodid'},
        {name: 'methodname'},
        {name: 'accountid'},
        {name: 'detailtype',type:'int'}
    ]);
    this.pmtStore = new Wtf.data.Store({
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        },this.pmtRec),
//        url:Wtf.req.account+'CompanyManager.jsp',
        url : "ACCPaymentMethods/getPaymentMethods.do",
        baseParams:{
            mode:51
        }
    });
    this.pmtStore.load({params:{grouper:'paymentTrans'}});
    if(this.isEdit)
        this.pmtStore.on('load',this.setPMData,this);
    config.isReceipt?chkcustaccload():chkvenaccload();
    this.personRec = new Wtf.data.Record.create ([
        {name:'accid'},
        {name:'accname'},
        {name:'currencyid'}
//        {name: 'level'}
    ]);


    this.personAccStore =  new Wtf.data.Store({
//        url:Wtf.req.account+'CompanyManager.jsp',
        url : "ACCAccount/getAccountsForCombo.do",
     // baseParams:{mode:2},
        reader: new  Wtf.data.KwlJsonReader({
            root: "data",
            autoLoad:false
        },this.personRec)
    });

    this.creationDate=new Wtf.form.DateField({
        name:"creationdate",
        format:WtfGlobal.getOnlyDateFormat(),
        value:Wtf.serverDate,
        id:"date"+config.helpmodeid+this.id,
        fieldLabel:WtfGlobal.getLocaleText("acc.mp.date"), //"Date*",
        anchor:'85%'
//        disabled: true
    });
     this.currencyRec = new Wtf.data.Record.create([
        {name: 'currencyid',mapping:'tocurrencyid'},
        {name: 'symbol'},
        {name: 'currencyname',mapping:'tocurrency'},
        {name: 'exchangerate'},
        {name: 'htmlcode'}
     ]);
     this.currencyStore = new Wtf.data.Store({
        reader: new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty:"count"
        },this.currencyRec),
//        url:Wtf.req.account+'CompanyManager.jsp'
        url:"ACCCurrency/getCurrencyExchange.do"
     });
     this.currencyStore.on("load",function(store){
         if(this.isEdit) {
            this.Currency.setValue(this.record.data.currencyid);
         } else if(this.directPayment) {
            this.Currency.setValue(this.invoiceRecord.data.currencyid);
         }
        if(store.getCount()<=1){
                callCurrencyExchangeWindow();
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.mp.10")],2);
        } else {
            var index = 3;
            if(this.Currency.getValue()!=""){
                var FIND = this.Currency.getValue();
                index = this.currencyStore.findBy( function(rec){
                        var parentname=rec.data['currencyid'];
                        if(parentname==FIND)
                            return true;
                        else
                            return false
                    })
            }
            this.applyTemplate(this.currencyStore, index);
        }
     },this)
     this.currencyStore.load({params:{grouper:'paymentTrans',mode:201,transactiondate:WtfGlobal.convertToGenericDate(new Date())}});
     this.Currency= new Wtf.form.FnComboBox({
        fieldLabel:WtfGlobal.getLocaleText("acc.mp.cur"),  //'Currency*',
        hiddenName:'currencyid',
        id:"currency"+config.helpmodeid+this.id,
        anchor: '85%',
        allowBlank:false,
        store:this.currencyStore,
        disabled:true,
        valueField:'currencyid',
//        emptyText:'Please select Currency...',
        forceSelection: true,
        displayField:'currencyname',
        scope:this,
        selectOnFocus:true
    });
   this.Name=new Wtf.form.FnComboBox({
        fieldLabel: (config.isReceipt?WtfGlobal.getLocaleText("acc.mp.cus"):WtfGlobal.getLocaleText("acc.mp.ven")),//
        id:"account"+config.helpmodeid+this.id,
        hiddenName:'accid',
        store:this.personAccStore,
        disabled:this.isEdit || this.directPayment,
        valueField:'accid',
        emptyText:WtfGlobal.getLocaleText("acc.mp.selAcc"),  //'Please select an Account...',
        allowBlank:false,
        anchor:'85%',
        displayField:'accname',
        forceSelection:true,
        hirarchical:true//,
//        addNewFn:this.addBusinessPerson.createDelegate(this,[false, null,'custwin',this.isReceipt],true)
    });

//    if(!WtfGlobal.EnableDisable(this.uPermType,this.permType.edit))
//            this.Name.addNewFn=this.addBusinessPerson.createDelegate(this,[false, null,'custwin',this.isReceipt],true)


    this.pmtMethod= new Wtf.form.FnComboBox({
        fieldLabel:(config.isReceipt?WtfGlobal.getLocaleText("acc.mp.payAcc"):WtfGlobal.getLocaleText("acc.mp.payAcc")),
        name:"pmtmethod",
        store:this.pmtStore,
        id:"paymentMethod"+config.helpmodeid+this.id,
        valueField:'methodid',
        displayField:'methodname',
        allowBlank:false,
        emptyText:(config.isReceipt?WtfGlobal.getLocaleText("acc.rp.recaacc"):WtfGlobal.getLocaleText("acc.mp.selpayacc")),
        anchor:'90%',
        mode: 'local',
        triggerAction: 'all',
        typeAhead: true,
        forceSelection: true//,
 //       addNewFn:this.addPaymentMethod.createDelegate(this)
    });
    if(!WtfGlobal.EnableDisable(Wtf.UPerm.paymentmethod, Wtf.Perm.paymentmethod.edit))
        this.pmtMethod.addNewFn=this.addPaymentMethod.createDelegate(this)
    this.pmtMethod.on('select',this.ShowCheckDetails.createDelegate(this),this);
    this.No=new Wtf.form.TextField({
        fieldLabel:config.isReceipt?WtfGlobal.getLocaleText("acc.rp.RecNO"):WtfGlobal.getLocaleText("acc.mp.payNo"),  //this.transectionName+' No*',
        id:"receiptNo"+config.helpmodeid+this.id,
        name: 'no',
        disabled:this.isEdit,
        anchor:'85%',
        maxLength:45,
        allowBlank:false
    });
    this.setOSDetailNumber();
    this.Memo=new Wtf.form.TextArea({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.memo"),  // 'Memo',
        name: 'memo',
        height:40,
        anchor:'90%',
        maxLength:200
    });
    this.Amount=new Wtf.form.NumberField({
        name:"amount",
        allowBlank:false,
        fieldLabel:WtfGlobal.getLocaleText("acc.mp.11"),  //"Amount*",//in "+WtfGlobal.getCurrencySymbolForForm()+"*",			
        id:"amount"+config.helpmodeid+this.id,
        maxLength:15,
        decimalPrecision:2,
        disabled:true,
        value:0,
        anchor:'85%'
//        disabled: true,
//        hidden: true
//        hideLabel: true						// Amount Field and Label made Hidden			Neeraj
    });
    
    this.Amount.on('blur',function(field){if(field.getValue() == ""){field.setValue(0);this.updateSouthTemp();}},this);
    this.Amount.on('focus',function(field){if(field.getValue() == 0){field.setValue("");}},this);

    this.SouthForm=new Wtf.account.PayMethodPanel({
        region : "center",
        hideMode:'display',
         baseCls:'bodyFormat',
        isReceipt:this.isReceipt,
        height:90,
        hidden:true,
        style:'margin:10px 10px;',
        id:this.id+'southform',
        border:false
    });
    this.NorthForm=new Wtf.form.FormPanel({
        region : "north",
        height:90,
        border:false,
        defaults:{border:false},
        split:true,        
        layout:'form',
        baseCls:'northFormFormat',
        hideMode:'display',
        id:this.id+'Northform',
        cls:"visibleDisabled",
        labelWidth:140,
        items:[{
            layout:'column',
            defaults:{border:false},
            items:[{
                layout:'form',
                columnWidth:0.34,
                items:[this.No,this.creationDate]
           },{
                layout:'form',
                columnWidth:0.33,
                items:[this.Name, this.Currency, this.Amount]
            },{
                layout:'form',
                columnWidth:0.32,
                items:[this.pmtMethod,this.Memo]
            }]
        }]
    });
     this.createGrid();
     this.tplSummary=new Wtf.XTemplate(
        '<div class="currency-view">',
        '<table width="100%">',
        '<tr><td><b>'+WtfGlobal.getLocaleText("acc.mp.amtDue")+': </b></td><td text-align=right>{due}</td></tr>',
        '<tr><td><b>'+(this.isReceipt?WtfGlobal.getLocaleText("acc.mp.12"):WtfGlobal.getLocaleText("acc.mp.13"))+ '</b></td><td text-align=right>{received}</td></tr>',
        '</table>',
        '<hr class="templineview">',
        '<table width="100%">',
        '<tr><td><b>'+(this.isReceipt?WtfGlobal.getLocaleText("acc.rp.resce"):WtfGlobal.getLocaleText("acc.mp.paya"))+ ' </b></td><td align=right>{receivable}</td></tr>',
        '</table>',
        '<hr class="templineview">',
        '<hr class="templineview">',
        '</div>'
    );
    this.southCalTemp=new Wtf.Panel({
        border:false,
        boaseCls:'tempbackgroundview',
        html:this.tplSummary.apply({receivable:WtfGlobal.currencyRenderer(0),received:WtfGlobal.currencyRenderer(0),due:WtfGlobal.currencyRenderer(0)})
    });
   this.southCenterTplSummary=new Wtf.XTemplate(
         "<div style='line-height:18px;'><b>"+WtfGlobal.getLocaleText("acc.inv.cur")+"</b> {basecurrency}</div>",
         '<tpl if="editable==true">',
         "<b>Applied Exchange Rate for the current transaction:</b>",
         "<div style='line-height:18px;padding-left:30px;'> 1 {basecurrency} (Home Currency) = {exchangerate} {foreigncurrency} (Foreign Currency) </div>",
         "<div style='line-height:18px;padding-left:30px;padding-bottom:5px;'>1 {foreigncurrency} (Foreign Currency) = {revexchangerate} {basecurrency} (Home Currency) </div>",
//         "<br>",
         "If you want to change the Exchange Rate for the current transaction only, then please <a class='tbar-link-text' href='#' onClick='javascript: editReceiptExchangeRates(\""+this.id+"\",\"{basecurrency}\",\"{foreigncurrency}\",\"{exchangerate}\")'wtf:qtip=''>Click Here</a>",
         '</tpl>'

    );
   this.southCenterTpl=new Wtf.Panel({
        border:false,
        html:this.southCenterTplSummary.apply({basecurrency:WtfGlobal.getCurrencyName(),exchangerate:'x',foreigncurrency:"Foreign Currency",editable:false})
    });
    this.southPanel=new Wtf.Panel({
        region:'south',
        border:false,
        style:'padding:0px 10px 10px 10px',
        layout:'border',
        height:110,
        items:[{
            region:'center',
            border:false,
            items:[this.southCenterTpl]
        },{
            region:'east',
            id: this.id + 'southEastPanel',
            cls:'bckgroundcolor',
            bodyStyle:'padding:10px',
            width:280,
            items:[this.southCalTemp]
        }]
    });
    this.appendID = true;
    Wtf.apply(this,{
        items:[
          this.wrapperNorth = new Wtf.Panel({
              region:"north",
              height:105,
              style:'padding:0px 10px 0px 0px',
              id:this.id+"wrapperPanelNorth",
              border:false,
              layout:'border',
              defaults:{border:false},
              items:[this.NorthForm,this.SouthForm]
           }),this.grid,this.southPanel
        ],
        bbar:[this.saveBttn=new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.common.saveBtn"),  //'Save',
            scope:this,
            id:"save"+config.helpmodeid+this.id,
            iconCls :getButtonIconCls(Wtf.etype.save),
            handler: this.save.createDelegate(this)
        }),"->",getHelpButton(this,config.helpmodeid)]
    });
    this.Amount.on('change',this.checkAmount,this);
    this.grid.on('datachanged',this.updateSouthTemp,this);
    this.creationDate.on('change',this.changeCurrencyStore,this);
    this.grid.getStore().on('load',this.updateAmount,this);
    this.Name.on('select',this.loadGrid,this);
    this.Currency.on('select',function(a,rec){this.grid.setCurrencyid(rec.data.currencyid,rec.data.symbol);this.updateSouthTemp()},this);
    this.Amount.on('change',function(a,b){if(this.val!="2")this.grid.updateAmount(b);},this);
    Wtf.account.OSDetailPanel.superclass.constructor.call(this,config);
    this.addEvents({
        update:true
    });
}
Wtf.extend(Wtf.account.OSDetailPanel,Wtf.Panel,{
    setPMData:function(){
        this.pmtMethod.setValue(this.record.data.methodid);
        this.ShowCheckDetails(null,this.record);
     //   this.pmtMethod.setValue(this.record.data.methodid);
        var type=this.record.data.detailtype;
          if(type==2){
            this.SouthForm.checkNo.setValue(this.record.data.refno);
            this.SouthForm.description.setValue(this.record.data.refdetail);
  //          this.SouthForm.refNo.setValue(this.record.data.refno);
            this.SouthForm.bankTypeStore.on('load',function(){this.SouthForm.bank.setValue(this.record.data.refname);},this)
       // }else if(type==1){
          //  this.SouthForm.refNo.setValue(this.record.data.refno);
           // this.SouthForm.cardType.setValue(this.record.data.refdetail);
           // this.SouthForm.expDate.setValue(this.record.data.expirydate);
           // this.SouthForm.nameOnCard.setValue(this.record.data.refname);
        }
        this.doLayout();
    },
    loadRecord:function(){
        if(this.record!=null){
            var data=this.record.data;
            this.NorthForm.getForm().loadRecord(this.record);
            this.setEditableData=true;
            this.Currency.setValue(data.currencyid);
            this.Memo.setValue(data.memo);
            this.creationDate.setValue(data.billdate);
        }
    },


    changeCurrencyStore:function(a,val,oldval){
        this.currencyStore.load({params:{mode:201,transactiondate:WtfGlobal.convertToGenericDate(this.creationDate.getValue())}});
        this.currencyStore.on('load',this.changeTemplateSymbol.createDelegate(this,[oldval]),this);
    },
    changeTemplateSymbol:function(oldval){
        if(this.currencyStore.getCount()==0){
            this.currencyStore.purgeListeners();
             WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.mp.10")], 2);
             this.creationDate.setValue(oldval);
             this.changeCurrencyStore();
        }
        else
            this.getCurrencySymbol();
    },
    onRender:function(config){
    	if(this.isEdit){
           this.checkWinVal(null,"1");
           this.loadRecord();
           this.loadGrid(this.record);
        } else if(this.directPayment){
           this.checkWinVal(null,this.isCustBill?"3":"1");
           this.loadGrid();
        } else{
            var winid=this.isCustBill?"billrecwin":"recWin";
            callReceiptWindow(winid,this.isReceipt,false,this.isCustBill);
            Wtf.getCmp(winid).on('update',this.checkWinVal,this);
        }
        Wtf.account.OSDetailPanel.superclass.onRender.call(this,config);
    },
    checkWinVal:function(c,val){
        this.val=val;
        var params={mode:2,grouper:'paymentTrans'};
        if(this.val=="1" && !this.isEdit){								//Neeraj
        	if(this.invoiceRecord == undefined){
        		WtfGlobal.hideFormElement(this.Amount);
        	}else{
            	this.Amount.hideLabel = true;
            	this.Amount.hidden = true;
        	}
        }
        if(this.val=="2"&&!this.isEdit){
        	this.Name.allowBlank=false;
            if(this.isReceipt){
                this.Amount.enable();
            	this.grid.getView().emptyText="";
                this.grid.getStore().removeAll();
                this.grid.hide();
                this.southPanel.hide();
                WtfGlobal.updateFormLabel(this.Name, WtfGlobal.getLocaleText("acc.mp.11"));
                if(!this.isEdit)
                    params.ignorevendors=true;
            }
            else{
                this.isMultiDebit=true;
                WtfGlobal.hideFormElement(this.Name);					
                this.Name.allowBlank=true;
                this.grid.reconfigureGrid(this.isMultiDebit);
                this.Currency.enable();
                this.Amount.enable();										//Neeraj
                if(!this.isEdit)
                    params.ignorecustomers=true;
            }
            this.Amount.setValue(0);
            this.SouthForm.getForm().reset();

        }

        else if(this.isReceipt&&!this.isEdit) {
            params.group=10;
            params.nondeleted=true;
        }
        else if(!this.isEdit) {
            params.group=13;
            params.nondeleted=true;
        }
        if(val=="3"){
        	this.isCustBill=true;
        	WtfGlobal.hideFormElement(this.Amount);				//Neeraj
        }

        this.personAccStore.load({params:params});
        if(this.isEdit){
            this.personAccStore.on("load",function(){this.Name.setValue(this.record.data.personid);},this);
        }else if(this.directPayment) {
            this.personAccStore.on("load",function(){this.Name.setValue(this.invoiceRecord.data.personid);},this);
        }

    },
    loadGrid:function(a,b){
    	var type= this.isReceipt?'invoice':'Vendor invoice';  		
    	this.grid.getView().emptyText = "<div class='grid-empty-text'>No "+type+" is made against this "+this.businessPerson+"</div><br><br>"+WtfGlobal.emptyGridRenderer("<a class='grid-link-text' href='#' onClick=\"javascript: openInv("+this.isReceipt+","+this.isCustBill+",'"+this.Name.getValue()+"')\">Get Started by adding a "+this.label+" now...</a>");
        if(this.val!="2"){
            if(this.isEdit){
                if(this.isReceipt){//Customer
                    this.grid.getStore().proxy.conn.url =(this.isCustBill?"ACCReceipt/getBillingReceiptRows":"ACCReceiptCMN/getReceiptRows") + ".do";
                }else{//vendor
                    this.grid.getStore().proxy.conn.url =  (this.isCustBill?"ACCVendorPayment/getBillingPaymentRows":"ACCVendorPaymentCMN/getPaymentRows") + ".do";
                }
                this.grid.getStore().load({params:{grouper:'paymentTrans',bills:this.record.data['billid'],mode:(this.isCustBill?36:33),isReceiptEdit:true}});
                this.grid.getStore().on('load',function(store){
                    if(store.getCount()==0){
                        if(this.isEdit&&!this.isReceipt){
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.mp.9")],2);
                            this.saveBttn.hide();
                        }
                        else{
                            this.saveBttn.show();
                            this.southCalTemp.hide();
                            this.grid.hide(); this.val="2";
                        }
                    }else{
                    	WtfGlobal.hideFormElement(this.Amount);		// Neeraj
                    }
                },this);
                
            } else {
                    if(this.isReceipt){//Customer
                        this.grid.getStore().proxy.conn.url = (this.val=="3")? "ACCInvoiceCMN/getBillingInvoices.do" : "ACCInvoiceCMN/getInvoices.do";
                    }else{//vendor
                        this.grid.getStore().proxy.conn.url = (this.val=="3")? "ACCGoodsReceiptCMN/getBillingGoodsReceipts.do" : "ACCGoodsReceiptCMN/getGoodsReceipts.do";
                    }
                    
                    if(this.directPayment) {
                        this.grid.getStore().load({params:{billid:this.invoiceRecord.data.billid,deleted:false, 
                        nondeleted:true}});
                        this.grid.getStore().on("load",function(store){
                            if(store.getCount()>1){
                                store.filter("billid",this.invoiceRecord.data.billid);
                                this.updateAmount();
                                this.updateSouthTemp();
                            }
                        },this);
                    } else {
                        this.grid.getStore().load({params:{onlyAmountDue:true,accid:b.data['accid'],mode:((this.val=="3")?16:12),deleted:false,nondeleted:true}});
                        this.setCurrency(b.data['currencyid']);
                        this.grid.amount=this.Amount.getValue();
                    }
            }
       }else
        this.setCurrency(b.data['currencyid'])
    }, 
    applyTemplate:function(store,index){
        var isedit=this.Currency.getValue()!=WtfGlobal.getCurrencyID()&&this.Currency.getValue()!="";
        var exchangeRate = store.getAt(index).data['exchangerate'];
        if(this.externalcurrencyrate>0) {
            exchangeRate = this.externalcurrencyrate;
        } else if(this.isEdit && this.record.data.externalcurrencyrate){
            var externalCurrencyRate = this.record.data.externalcurrencyrate-0;
            if(externalCurrencyRate>0){
                exchangeRate = externalCurrencyRate;
            }
        }
        var revExchangeRate = 1/(exchangeRate-0);
        revExchangeRate = (Math.round(revExchangeRate*10000000))/10000000;
       this.southCenterTplSummary.overwrite(this.southCenterTpl.body,{foreigncurrency:store.getAt(index).data['currencyname'],exchangerate:exchangeRate,basecurrency:WtfGlobal.getCurrencyName(),editable:isedit,revexchangerate:revExchangeRate
            });
    },
    setCurrency:function(currencyid){
        if(currencyid==undefined){
            this.Currency.setValue(WtfGlobal.getCurrencyID());
            this.currencyid=WtfGlobal.getCurrencyID();
        }
        this.Currency.setValue(currencyid);
        this.currencyid=currencyid;
        this.getCurrencySymbol() ;
        this.updateSouthTemp();
        this.ApplyCurrencySymbol();

    },
    updateAmount:function(){
        this.amtdue=(this.isMultiDebit?this.Amount.getValue():this.grid.getAmount(true));
         if(!this.isMultiDebit){
       //     if(!this.directPayment)
                this.Amount.enable();
            if(this.setEditableData){
                this.Amount.setValue(this.record.data.amount);
                this.grid.amount=this.Amount.getValue();
                this.grid.updateAmount(this.Amount.getValue());
            }
            else{
//              this.Amount.setValue(this.grid.getAmount(true));			// Amount NumberField used in make or recieve payment logic
//            this.grid.amount=this.Amount.getValue();
//            this.grid.updateAmount(this.grid.getAmount(true));
            
            this.Amount.setValue(this.grid.getAmount(true));				// Amount NumberField not used anymore in make or recieve payment logic     Neeraj
        	this.grid.amount=0;
        	this.grid.updateAmount(0);
            
            }
        }
    },
    checkAmount:function(a,b){
        if(this.isMultiDebit)
            this.updateSouthTemp();
        if(this.val=="1"||this.val=="3")
            if(b>this.amtdue){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.mp.8")],2);
            this.Amount.setValue(this.amtdue);
        }
    },
    updateSouthTemp:function(){
        if(this.isMultiDebit){
            this.getCurrencySymbol();
           this.tplSummary.overwrite(this.southCalTemp.body,{received:WtfGlobal.addCurrencySymbolOnly(this.grid.getMultiDebitAmount(),this.symbol),due:WtfGlobal.addCurrencySymbolOnly(this.Amount.getValue(),this.symbol),receivable:WtfGlobal.addCurrencySymbolOnly(this.grid.getMultiDebitAmount()-this.Amount.getValue(),this.symbol)});
        }
        else{
            if(this.directPayment)
                this.getCurrencySymbol();
                this.tplSummary.overwrite(this.southCalTemp.body,{received:WtfGlobal.addCurrencySymbolOnly(this.grid.getAmount(false),this.symbol),due:WtfGlobal.addCurrencySymbolOnly(this.grid.getAmount(true),this.symbol),receivable:WtfGlobal.addCurrencySymbolOnly(this.grid.getAmount(true)-this.grid.getAmount(false),this.symbol)});
        }
    },
    addBusinessPerson:function(isEdit,rec,winid,isCustomer){
        if(this.val!="1" ||(this.val!="2"&& this.personwin))
            this.checkperson(isEdit,rec,"perwindow",isCustomer)
       else{
            callBusinessContactWindow(isEdit, rec, winid, isCustomer);
            Wtf.getCmp(winid).on('update', function(){
                this.isReceipt?Wtf.customerAccStore.reload():Wtf.vendorAccStore.reload();
                this.personAccStore.reload();
            }, this);
       }
    },
    checkperson:function(){
           callAccountTypeWindow('perwindow',this.isReceipt,true);
           Wtf.getCmp('perwindow').on('update',this.addwin,this);
    },
    addwin:function(scope,val){
        var win='custwin'
        if(val=="1")
         callBusinessContactWindow(false, null, win, this.isReceipt);
        else{
            win='coaWin'
            callCOAWindow(false, null, "coaWin");
            }
         Wtf.getCmp(win).on('update', function(){
            this.isReceipt?Wtf.customerAccStore.reload():Wtf.vendorAccStore.reload();
            this.personAccStore.reload();
        }, this);
    },
    addPaymentMethod:function(){
      PaymentMethod('PaymentMethodWin');
      Wtf.getCmp('PaymentMethodWin').on('update', function(){
            this.pmtStore.reload();
      }, this);
    },
    getCurrencySymbol:function(){
       var index=null;
       this.currencyStore.clearFilter(true)
       var FIND = this.Currency.getValue();
       index=this.currencyStore.findBy( function(rec){
       var parentname=rec.data['currencyid'];
       if(parentname==FIND)
            return true;
         else
            return false
       })
        if(index>=0)
            this.symbol=  this.currencyStore.getAt(index).data['symbol'];
         return index;
    },

    ApplyCurrencySymbol:function(){
       var index=this.getCurrencySymbol();
       if(index>=0){
            this.grid.setCurrencyid(this.currencyid,this.symbol,index);
            this.applyTemplate(this.currencyStore,index);
       }
    },
    createGrid:function(){
         var type= this.isReceipt?'invoice':'Vendor invoice';

         this.grid = new Wtf.account.OSDetailGrid({
           region:'center',
           border:true,
           currencyid:this.currencyid,
           isEdit:this.isEdit,
           cls:'gridFormat',
           viewConfig:{forceFit:true, emptyText:"<div class='grid-empty-text'>No "+type+" is made against this "+this.businessPerson+"</div><br><br>"+WtfGlobal.emptyGridRenderer("<a class='grid-link-text' href='#' onClick=\"javascript: openInv("+this.isReceipt+","+this.isCustBill+",'"+this.Name.getValue()+"')\">Get Started by adding a "+this.label+" now...</a>")},
           isReceipt:this.isReceipt,
           isMultiDebit:this.isMultiDebit,
           amount:0,
           id : this.id+(this.isReceipt ? 'customergrid' : 'vendorgrid'),
           closable: true//,
    });
    },
    
    ShowCheckDetails:function(combo,rec){
        this.SouthForm.ShowCheckDetails(rec.data['detailtype']);
        if(rec.data['detailtype']==2) {
            this.wrapperNorth.setHeight(235);
            this.SouthForm.show();
	    this.SouthForm.getForm().items.items[1].disabled = false; 	
        }else {
            this.wrapperNorth.setHeight(105);
            this.SouthForm.hide();
            this.SouthForm.getForm().items.items[1].disabled = true;
        }
        this.wrapperNorth.doLayout();
        this.southPanel.doLayout();
        this.grid.doLayout();
        this.doLayout();
        if(!this.isReceipt && rec.data.methodname != undefined){
            this.SouthForm.setBankName(rec.data.methodname);
        }
    },

    save:function(a,b){
    	if(!this.isMultiDebit && !this.grid.hidden){
    		this.Amount.disable();
    	}
        var valid=this.NorthForm.getForm().isValid();
        if(this.SouthForm.getForm().isValid()&&valid){
            if(this.isMultiDebit){
                var amt=this.Amount.getValue();
                var due=this.grid.getMultiDebitAmount();
                if(amt==0 || amt==""||due==0){
                  WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.mp.7")],2);
                     return;
                }
                if(amt!=due){
                  WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.mp.6")],2);
                     return;
                }
                if(this.grid.getData()=='[]'){
                  WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.mp.5")],2);
                     return;
                }
            }
            else{
                 amt=this.grid.getAmount(false);
                due=this.grid.getAmount(true);
                if(amt==0 && due==0 && (this.val=="1"||this.val=="3") && this.Amount.getValue()!=0){
                    if(this.isReceipt)
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.mp.4")],2);
                     else
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.mp.3")],2);
                     return;
                }
                if(this.Amount.getValue()==0 || this.Amount.getValue()==""||(amt==0 && due!=0)){
                  WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.mp.2")],2);
                     return;
                }
            }
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.savdat"),WtfGlobal.getLocaleText("acc.je.msg1"),function(btn){
                if(btn!="yes") return;
                WtfComMsgBox(27,4,true);
                var rec=this.NorthForm.getForm().getValues();
                rec.mode=(this.isCustBill?34:31);
                rec.externalcurrencyrate=this.externalcurrencyrate;
                rec.pmtmethod=this.pmtMethod.getValue();
                if(this.val!="2"||this.isMultiDebit)
                    rec.detail=this.grid.getData();
                rec.isReceiptEdit=this.isEdit;
                if(this.isEdit)
                    rec.billid=this.record.data.billid;
                rec.accid=this.Name.getValue();
                rec.ismultidebit=this.isMultiDebit;
                    rec.paydetail = this.SouthForm.GetPaymentFormData();
                rec.currencyid=this.Currency.getValue();
                rec.creationdate=WtfGlobal.convertToGenericDate(this.creationDate.getValue());
                this.ajxUrl = "";
                //(this.isCustBill?34:31)
                if(this.businessPerson=="Customer"){
                    this.ajxUrl = "ACCReceipt/" + (this.isCustBill?"saveBillingReceipt":"saveReceipt") + ".do";
                }else if(this.businessPerson=="Vendor"){
                    this.ajxUrl = "ACCVendorPayment/" + (this.isCustBill?"saveBillingPayment":"savePayment") + ".do";
                }
                Wtf.Ajax.requestEx({
                    url:this.ajxUrl,
//                    url: Wtf.req.account+this.businessPerson+'Manager.jsp',
                    params: rec
                    },this,this.genSuccessResponse,this.genFailureResponse);
                },this);
        }
        else
            WtfComMsgBox(2, 2);
    },
    genSuccessResponse:function(response){ 
        Wtf.MessageBox.hide();
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"),response.msg],response.success*2+1);
        this.fireEvent('update',this);
        if(!this.directPayment&&!this.isEdit){
            if(response.success){
               this.NorthForm.getForm().reset();
               this.SouthForm.getForm().reset();
               this.Currency.setValue(WtfGlobal.getCurrencyID());
               this.tplSummary.overwrite(this.southCalTemp.body,{received:WtfGlobal.addCurrencySymbolOnly(0,this.symbol),due:WtfGlobal.addCurrencySymbolOnly(0,this.symbol),receivable:WtfGlobal.addCurrencySymbolOnly(0,this.symbol)});
                   this.grid.getStore().removeAll();
                   this.grid.getView().emptyText="<div class='grid-empty-text'>"+WtfGlobal.getLocaleText("acc.mp.1")+"</div>";
                   this.grid.getView().refresh();
                   this.externalcurrencyrate=0; //Reset external exchange rate for new Transaction.
                   this.ApplyCurrencySymbol();
               if(this.isMultiDebit)
                    this.grid.addNewRow();
            }
            this.setOSDetailNumber();
        } else {
            if(response.success){
                Wtf.getCmp('as').remove(this);
            }
        }
    }, 
    genFailureResponse:function(response){
      Wtf.MessageBox.hide();
        var msg=WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
//        if(this.directPayment){
//             Wtf.getCmp('as').remove(this);
//        }
    },
    setOSDetailNumber:function(){
        if(this.isEdit)
            this.No.setValue(this.record.data.billno);
        else{
            var temp=0;
            if(this.isCustBill)
                temp=this.isCustBill*10
            temp+=this.isReceipt*1;
            var format="";var temp2="";
            switch(temp){
                case 0:format=Wtf.account.companyAccountPref.autopayment;
                    temp2=Wtf.autoNum.Payment;
                    break;
                case 1:format=Wtf.account.companyAccountPref.autoreceipt;
                    temp2=Wtf.autoNum.Receipt;
                    break;
                case 10:format=Wtf.account.companyAccountPref.autobillingpayment;
                    temp2=Wtf.autoNum.BillingPayment;
                    break;
                case 11:format=Wtf.account.companyAccountPref.autobillingreceipt;
                    temp2=Wtf.autoNum.BillingReceipt;
                    break;
            }
            if(format&&format.length>0){
                WtfGlobal.fetchAutoNumber(temp2, function(resp){this.No.setValue(resp.data)}, this);
            }
        }
    }
});
