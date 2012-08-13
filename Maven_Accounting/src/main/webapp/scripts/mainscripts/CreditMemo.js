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
Wtf.account.TrNotePanel=function(config){
    this.masterGroup=config.isCN?5:1;
    this.currencyid=null;
    this.externalcurrencyrate=0;
    this.noteType=config.isCN?'Credit':'Debit';
    this.businessPerson=config.isCN?"Customer":"Vendor";
    this.custPermType=config.isCN?Wtf.Perm.customer:Wtf.Perm.vendor;
    this.soUPermType=(config.isCN?Wtf.UPerm.invoice:Wtf.UPerm.vendorinvoice);
    this.isCustomer=config.isCN;
    Wtf.account.TrNotePanel.superclass.constructor.call(this,config);
}
Wtf.extend(Wtf.account.TrNotePanel,Wtf.Panel,{
    onRender:function(config){
        this.createFields();
        this.extendComponent();
        this.add({
            layout:'border',
            border:false,
            items:[{
                 region:'north',
                 height:220,
                 border:false,
                layout:'border',
                items:[this.custForm,this.invGrGrid] 
            },
                {
                cls:"gridFormat",
                bodyStyle:"background:#eeeeee;",
                region:'center', 
                layout:'border',
                id:"creditNoteDetails"+this.helpmodeid,
                items:[this.northForm,this.pdGrid]
            }],
            bbar: [{
                text:WtfGlobal.getLocaleText("acc.common.saveBtn"),  //'Save',
                scope:this,
                iconCls :getButtonIconCls(Wtf.etype.save),  
                handler: this.save
            },"->",getHelpButton(this,this.helpmodeid)]
        });
        this.name.on('select',this.loadGridData,this);
        this.invGrGrid.getSelectionModel().on('rowselect',this.getSelectedRow,this);
        this.pdGrid.getStore().on('load',this.setType,this);
        this.pdGrid.getSelectionModel().on('rowdeselect',this.checkAmountDue,this);
        this.creationDate.setValue(Wtf.serverDate);
        Wtf.account.TrNotePanel.superclass.onRender.call(this,config);
    },     

    createFields:function(){
        this.costCenterId = "";
        this.no=new Wtf.form.TextField({
            fieldLabel:(this.isCustomer?WtfGlobal.getLocaleText("acc.accPref.autoCN"):WtfGlobal.getLocaleText("acc.accPref.autoDN")) +" "+ WtfGlobal.getLocaleText("acc.cn.9") + "*",  //this.noteType+' Note No*',
            name: 'number',
            scope:this,
            maxLength:45,
            anchor:'90%',
            allowBlank:false
        });
        this.setTrNoteNumber();
        this.isCN?chkcustaccload():chkvenaccload();
        this.name=new Wtf.form.FnComboBox({
            fieldLabel:(this.isCustomer?WtfGlobal.getLocaleText("acc.invoiceList.cust"):WtfGlobal.getLocaleText("acc.invoiceList.ven"))+"*",  //this.businessPerson +'*',
            id:"selectCustomer"+this.helpmodeid,
            hiddenName:'accid',
            store:this.isCN?Wtf.customerAccStore:Wtf.vendorAccStore,
            valueField:'accid',
            scope:this,
            anchor:'90%',
            displayField:'accname',
//            allowBlank:false, //Checked at a time of saving
            emptyText:this.isCustomer?WtfGlobal.getLocaleText("acc.inv.cus"):WtfGlobal.getLocaleText("acc.inv.ven"),  //'Please Select a '+this.businessPerson+'...',
            forceSelection: true,
            hirarchical:true
//            addNewFn:this.callCustomer.createDelegate(this,[false, null,'custwin',this.isCN])
        });
       if(!WtfGlobal.EnableDisable(this.custUPermType,this.custPermType.create))
            this.name.addNewFn=this.callCustomer.createDelegate(this,[false, null,'custwin',this.isCN]);
        this.creationDate=new Wtf.form.DateField({
             xtype:'datefield',
             name:'creationdate',
             allowBlank:false,
             anchor:'90%',
             format:WtfGlobal.getOnlyDateFormat(),
             fieldLabel:WtfGlobal.getLocaleText("acc.customer.date") + "*"  //'Creation Date*'
        });
        this.textBoxName=new Wtf.form.TextField({
            fieldLabel:this.businessPerson,
            name:'textname',
            anchor:'90%',
            allowBlank:false,
            readOnly:true
        });
        this.memo=new Wtf.form.TextArea({
            fieldLabel:WtfGlobal.getLocaleText("acc.common.memo"),  //'Memo',
            name:'memo',
            height:35,
            anchor:'90%',
            maxLength:200
        });
        chkcurrencyload();
        Wtf.currencyStore.on("load",function(store){
            if(store.getCount()<=1){
                callCurrencyExchangeWindow();
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),"Please set Currency Exchange Rates"],2);
            }
         },this)
         this.Currency= new Wtf.form.FnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.cust.currency"),  //'Currency',
            hiddenName:'currencyid',
            anchor: '90%',
            allowBlank:false,
            store:Wtf.currencyStore,
            disabled:true,
            valueField:'currencyid',
         //   emptyText:'Please select Currency...',
            forceSelection: true,
            displayField:'currencyname',
            scope:this,
            selectOnFocus:true,
            listeners:{
                'select':{
                    fn:this.getCurrencySymbol,
                    scope:this
                }
            }
        });
    },
   extendComponent:function(){
    this.ExternalCurrencyRate=new Wtf.form.NumberField({
        allowNegative:false,
        hidden:this.isOrder,
        hideLabel:this.isOrder,
        readOnly:true,
        emptyText:'System generated Rate Applied',
        maxLength: 10,
        anchor:'70%',
        fieldLabel:'External Currency Rate',
        name:'externalcurrencyrate',
         listeners:{
            'change':{
                fn:this.updateFormCurrency,
                scope:this
            }
        }
    });
       this.custForm=new Wtf.form.FormPanel({
            region : "north",
            height:70,
            border:false,
            style:'padding:10px 0px 0px 10px',
            layout:'form',          
            labelWidth:90,
               items:[{
                layout:'column',
                border:false,
                defaults:{border:false,layout:'form',columnWidth:0.25},
                items:[{layout:'form',
                            columnWidth:0.43,
                    items:[this.name]
                },{layout:'form',
                            columnWidth:0.43,
                    items:[this.Currency]
//                },{layout:'form',
//                            columnWidth:0.33,
//                    items:[this.ExternalCurrencyRate]

                }]
            }]
       });
        this.invGrGrid = new Wtf.account.OSDetailGrid({
            region:'center',
            layout : "fit",
            style:'padding:0px 10px 0px 10px',
            isNote:true,
            sm:new Wtf.grid.CheckboxSelectionModel({singleSelect:true}),
            isReceipt:this.isCN,
            viewConfig:{forceFit:true, emptyText:"<div class='grid-empty-text'>"+(this.isCN?WtfGlobal.getLocaleText("acc.rem.121"):WtfGlobal.getLocaleText("acc.rem.122"))+"</div>"},
            closable: true
        });
        if(this.isCustBill){
            this.pdGrid=new Wtf.account.BillingProductDetailsGrid({
                region:'center',
                layout:'fit',
                title:WtfGlobal.getLocaleText("acc.cnList.prodList"),  //"Product List",
                autoScroll:true,
                border:false,
                viewConfig:{forceFit:true},
                isCustomer:this.isCustomer,
                isCustBill:this.isCustBill,
                sm:new Wtf.grid.CheckboxSelectionModel(),
                isNote:true,
                isCN:this.isCN,
                currencyid:this.Currency.getValue(),
                forceFit:true,
                loadMask : true
            });
        }else{
            this.pdGrid=new Wtf.account.ProductDetailsGrid({
                region:'center',
                title:WtfGlobal.getLocaleText("acc.cnList.prodList"),  //"Product List",
                autoScroll:true,
                border:false,
                sm:new Wtf.grid.CheckboxSelectionModel(),
                isNote:true,
                isCN:this.isCN,
                layout:'fit',
                viewConfig:{forceFit:true},
                loadMask : true
            });
        }

        this.northForm=new Wtf.form.FormPanel({
            region : "north",
            height:60,
            border:false,
            layout:'form',
            style: "padding:10px",
            labelWidth:90,
            items:[{
                layout:'column',
                border:false,
                defaults:{border:false,layout:'form',columnWidth:0.32},
                items:[{
                    items:[this.no]
                },{
                    items:[this.creationDate]
//                },{
//                    items:[this.textBoxName]
                },{
                    items:[this.memo]
                }]
            }]
        });

    },
    callCustomer:function(isEdit,rec,winid,isCustomer){
         callBusinessContactWindow(isEdit, rec, winid, isCustomer);
        Wtf.getCmp(winid).on('update', function(){
            this.isCN?Wtf.customerAccStore.reload():Wtf.vendorAccStore.reload();
        }, this);
    },

    updateData:function(){
        var customer=this.name.getValue();
        Wtf.Ajax.requestEx({
            url:"ACC"+this.businessPerson+"/getAddress.do",
//            url:Wtf.req.account+this.businessPerson+'Manager.jsp',
            params:{
                mode:4,
                customerid:customer
            }
        }, this,this.setCurrency);
    },
    setCurrency:function(response){
        if(response.success){
            this.Currency.setValue(response.currencyid);
            this.setInvoiceCurrencySymbol() ;
        }
    },
   
    getCurrencySymbol:function(){
        Wtf.currencyStore.clearFilter(true)
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

 //      var rec =Wtf.currencyStore.find('currencyid',this.Currency.getValue());
       this.currencyid=this.Currency.getValue();
       if(index>=0)
            var symbol=  Wtf.currencyStore.getAt(rec).data['symbol'];
       
       return symbol;
    },
    setInvoiceCurrencySymbol:function(){
        this.pdGrid.getStore().removeAll();
        var rec =Wtf.currencyStore.find('currencyid',this.Currency.getValue());
        if(rec>=0){
            var symbol=  Wtf.currencyStore.getAt(rec).data['symbol'];
            this.invGrGrid.setCurrencyid(this.Currency.getValue(),symbol,rec);
       }
    },
    setProductCurrencySymbol:function(store){
       var rec =Wtf.currencyStore.find('currencyid',this.Currency.getValue());
       if(rec>=0){
            var rate=  Wtf.currencyStore.getAt(rec).data['exchangerate'];
            var symbol=  Wtf.currencyStore.getAt(rec).data['symbol'];
            this.pdGrid.setCurrencyid(this.currencyid,rate,symbol,rec);
       }
    },

    setType:function(store){
        store.each(function(rec){
            rec.set('typeid',0);
            rec.set('discamount',0);
        },this);
        this.setProductCurrencySymbol(store);
    },
    loadGridData:function(a,rec){
        var recData = rec.data;
        this.pdGrid.getStore().removeAll();
//        this.invGrGrid.getStore().load({params:{accid:recData.accid,mode:(this.isCustBill?16:12)}});

        this.invGrGrid.getStore().proxy.conn.url = this.isCN ? (this.isCustBill?"ACCInvoiceCMN/getBillingInvoices.do":"ACCInvoiceCMN/getInvoices.do") : (this.isCustBill?"ACCGoodsReceiptCMN/getBillingGoodsReceipts.do":"ACCGoodsReceiptCMN/getGoodsReceipts.do");
        this.invGrGrid.getStore().load({params:{accid:recData.accid,mode:(this.isCustBill?16:12)}});
        this.invGrGrid.getStore().on('load',this.updateData,this) 
        var val
        if(this.isCN)
            val=(Wtf.customerAccStore.getAt(Wtf.customerAccStore.find('accid',recData.accid))).data['accname'];
        else
            val=(Wtf.vendorAccStore.getAt(Wtf.vendorAccStore.find('accid',recData.accid))).data['accname'];
        this.textBoxName.setValue(val);
    },
     checkAmountDue:function(){
        var  pdamountdue=0;
        var selModel=this.invGrGrid.getSelectionModel();
        var len=this.invGrGrid.getStore().getCount();
        for(var i=0;i<len;i++){
            pdamountdue=0;
            if(selModel.isSelected(i)){
                var invid=this.invGrGrid.getStore().getAt(i).data['billid'];
                var invamountdue= this.invGrGrid.getStore().getAt(i).data['amountdue'];
                var FIND =invid;
               
                var pdindex=this.pdGrid.getStore().findBy( function(rec){
                    var billid=rec.data['billid'];
               
                     if(billid==FIND){
                        pdamountdue=+rec.data['discamount'];
                        return true;
                    }
                     else
                       return false

                })
                if(invamountdue<pdamountdue)
                    return true;
            }
        }

        return false;
    },
    getSelectedRow:function(a,index,rec){
        var arr=[];
        this.costCenterId = "";
        var selModel=this.invGrGrid.getSelectionModel();
        if(rec.data.externalcurrencyrate!=undefined)
            this.externalcurrencyrate=rec.data.externalcurrencyrate;
     //   this.ExternalCurrencyRate.setValue(this.externalcurrencyrate==0?"":this.externalcurrencyrate);
        var len=this.invGrGrid.getStore().getCount();
        for(var i=0;i<len;i++){
            if(selModel.isSelected(i)){
                arr.push(this.invGrGrid.getStore().getAt(i).data['billid']);
                this.costCenterId = this.invGrGrid.getStore().getAt(i).data['costcenterid'];
            }
        }
        if(arr.length==0){
            this.pdGrid.getStore().removeAll();
            return;
        }
        var url = ((this.isCN)?(this.isCustBill?"ACCInvoiceCMN/getBillingInvoiceRows.do":'ACCInvoiceCMN/getInvoiceRows.do'):(this.isCustBill?"ACCGoodsReceiptCMN/getBillingGoodsReceiptRows.do":'ACCGoodsReceiptCMN/getGoodsReceiptRows.do'))
        this.pdGrid.getStore().proxy.conn.url = url;
        this.pdGrid.getStore().load({params:{
           bills:arr,
           mode:(this.isCustBill?17:14)
       }})
    },
    save:function(){
        this.no.setValue(this.no.getValue().trim());
        if(this.name.getValue()==""){
            this.name.markInvalid(WtfGlobal.getLocaleText("acc.product.msg1"));
            return;
        }
        if(this.northForm.getForm().isValid()  && this.name.getValue()!=""){
            var details=this.pdGrid.getCMProductDetails();
            if(details=="[]"){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.cn.8")], 2);
                return;
            }
            if(details=="Error"){               
                return;
            }
            var store=this.pdGrid.getStore();
            var checkamountdue=this.checkAmountDue();
             if(checkamountdue){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.cn.3")], 2);
                return;
            }
            var iszero=this.pdGrid.isAmountzero(store);
            if(iszero){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), (this.isCN?WtfGlobal.getLocaleText("acc.cn.6"):WtfGlobal.getLocaleText("acc.cn.7"))], 2);
                return;
            }
            if(this.invGrGrid.getSelectionModel().getSelected().data.date>this.creationDate.getValue()){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), (this.isCN?WtfGlobal.getLocaleText("acc.cn.5"):WtfGlobal.getLocaleText("acc.cn.4"))], 2);
                return;
            }

            var includetax=false;
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.cn.at"),WtfGlobal.getLocaleText("acc.cn.1"),function(btn){
                if(btn=="yes") { includetax=true; }

            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.savdat"),WtfGlobal.getLocaleText("acc.cn.2"),function(btn){
                if(btn!="yes") { return; }
                this.msg= WtfComMsgBox(27,4,true);
                var rec=this.northForm.getForm().getValues();
                rec.productdetails=details;
                rec.includetax=includetax;
                rec.externalcurrencyrate=this.externalcurrencyrate;
                rec.currencyid=this.Currency.getValue()
                rec.mode=this.isCN?(this.isCustBill?61:26):(this.isCustBill?61:27);
                rec.creationdate=WtfGlobal.convertToGenericDate(this.creationDate.getValue());
                rec.accid=this.name.getValue();
                rec.costCenterId=this.costCenterId;
                this.ajxUrl = "";
                if(this.isCN){
                    this.ajxUrl = this.isCustBill?"ACCCreditNote/saveBillingCreditNote.do":"ACCCreditNote/saveCreditNote.do";
                }else{
                    this.ajxUrl = this.isCustBill?"ACCDebitNote/saveBillingDebitNote.do":"ACCDebitNote/saveDebitNote.do";
                }
                Wtf.Ajax.requestEx({
                    url:this.ajxUrl,
//                    url: Wtf.req.account+(this.isCN?'CustomerManager.jsp':'VendorManager.jsp'),
                    params: rec
                },this,this.genSuccessResponse,this.genFailureResponse);
            },this);
             },this);
        }else
                 WtfComMsgBox(2,2);
    },
    genSuccessResponse:function(response){
        WtfComMsgBox([this.title,response.msg],response.success*2+1);
        if(response.success){
            this.northForm.getForm().reset();
            this.invGrGrid.getStore().removeAll();
            this.pdGrid.getStore().removeAll();
            this.name.setValue("");
//            this.invGrGrid.getView().emptyText="<div class='grid-empty-text'>Please select the account name.</div>"; //BUG FIXED:16231
            this.invGrGrid.getView().refresh();
            Wtf.dirtyStore.product = true; //To reload product list on activate Product List.
        }
        if(response.reloadInventory){
            Wtf.productStore.reload();
            Wtf.productStoreSales.reload();
        }
        this.setTrNoteNumber();
    },
    genFailureResponse:function(response){
        var msg=WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    },
    setTrNoteNumber:function(){
        var format="";var temp2="";
        var val=this.isCN*1+this.isCustBill*10;
        switch(val){
            case 0:format=Wtf.account.companyAccountPref.autodebitnote;
                temp2=Wtf.autoNum.DebitNote;
                break;
            case 1:format=Wtf.account.companyAccountPref.autocreditmemo;
                temp2=Wtf.autoNum.CreditNote;
                break;
            case 10:format=Wtf.account.companyAccountPref.autobillingdebitnote;
                temp2=Wtf.autoNum.BillingDebitNote;
                break;
            case 11:format=Wtf.account.companyAccountPref.autobillingcreditmemo;
                temp2=Wtf.autoNum.BillingCreditNote;
                break;
        }
        if(format&&format.length>0){
            WtfGlobal.fetchAutoNumber(temp2, function(resp){this.no.setValue(resp.data)}, this);
        }
    }
}); 
