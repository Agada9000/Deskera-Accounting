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
Wtf.account.BusinessContactWindow = function(config){
    this.perAccID=null;
    this.isClosable=true;
    this.businessPerson=(config.isCustomer?"Customer":"Vendor");
    this.uPermType=config.isCustomer?Wtf.UPerm.customer:Wtf.UPerm.vendor;
    this.permType=config.isCustomer?Wtf.Perm.customer:Wtf.Perm.vendor;
    this.currencyExchangeWinId = "SetCurrencyExchangeWin";
    this.requestTaxIDBttn=new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.common.requestTaxIDBtn"),  //'Request Tax ID from Vendor.',
        scope: this,
        hidden:config.isCustomer||Wtf.account.companyAccountPref.withouttax1099,
        handler: this.sendMail.createDelegate(this),
        disabled :!config.isEdit
    });
     Wtf.apply(this,{
        constrainHeader :true,		// 19991
//        title:(!config.isEdit)?"Add New "+this.businessPerson:"Edit "+this.businessPerson,			// 20141
        title:(config.isEdit)?(this.isCustomer?WtfGlobal.getLocaleText("acc.cust.tabTitleEdit"):WtfGlobal.getLocaleText("acc.ven.tabTitleEdit")):(this.isCustomer?WtfGlobal.getLocaleText("acc.cust.tabTitleAdd"):WtfGlobal.getLocaleText("acc.ven.tabTitleAdd")),
        buttons: [this.requestTaxIDBttn,this.saveBttn=new Wtf.Toolbar.Button({
            text: (!config.isEdit)?WtfGlobal.getLocaleText("acc.common.saveBtn"):WtfGlobal.getLocaleText("acc.common.editBtn"),  //'Save':'Edit',				//	20029
            minWidth: 50,
            scope: this,
            handler: this.saveForm.createDelegate(this)
        }),this.cancelBttn=new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.cancelBtn"),  //'Cancel',
            minWidth: 50,
            scope: this,
            handler:this.closeForm.createDelegate(this)
        })]
    },config);
    Wtf.account.BusinessContactWindow.superclass.constructor.call(this, config);
    this.addEvents({
        'update':true,
        'cancel':true,
        'loadingcomplete':true
    });
}
Wtf.extend( Wtf.account.BusinessContactWindow, Wtf.Window, {
    loadRecord:function(){
        if(this.record!=null){
            if(this.record.data['parentid']){
                this.issubFieldset.toggleCollapse();
            }
            this.CustomerInfoForm.getForm().loadRecord(this.record);
            this.creationdate.setValue(this.record.data.creationDate);
            if(this.creationdate.getValue() == null || this.creationdate.getValue() == undefined || this.creationdate.getValue() == ""){
            	this.creationdate.setValue(Wtf.serverDate);
            }
            this.Category.setValue(this.record.data.categoryid);
            this.requestTaxIDBttn.setDisabled(!this.record.data.taxeligible);
            var bal=this.record.data.openbalance;
            this.balTypeEditor.setValue(bal>0);
            if(bal!==0){
                this.openingBal.setValue(Math.abs(bal));
                this.balTypeEditor.enable();
            }
            if(bal==0 && this.isCustomer)
            	this.balTypeEditor.setValue(true);
        }
                    
    },
    onRender: function(config){
        var image="../../images/accounting_image/"+this.businessPerson+".gif";
        Wtf.account.BusinessContactWindow.superclass.onRender.call(this, config);
        this.createStore();
        this.createFields();
        this.createForm();
//        if(this.isEdit)
//            this.requestTaxIDBttn.enable()
//        else
//            this.requestTaxIDBttn.disable()
        if(this.isCustomer)
            this.SAddress.show();        
        else
            this.SAddress.hide();
        
        this.add({
            region: 'north',
            height:75,
            border: false,
            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
            html:getTopHtml(this.isCustomer?WtfGlobal.getLocaleText("acc.cust.cusInfo"):WtfGlobal.getLocaleText("acc.ven.venInfo"),(this.isEdit?(this.isCustomer?WtfGlobal.getLocaleText("acc.cust.cusInfoEdit"):WtfGlobal.getLocaleText("acc.cust.venInfoEdit")):(this.isCustomer?WtfGlobal.getLocaleText("acc.cust.cusInfoAdd"):WtfGlobal.getLocaleText("acc.cust.venInfoAdd"))),image)
//            html:getTopHtml(this.businessPerson+' Information',(this.isEdit?"Edit ":"Add new ")+this.businessPerson+' Information',image)
        }, this.centerPanel=new Wtf.Panel({
                border: false,
                region: 'center',
                autoScroll:true,
                bodyStyle: 'background:#f1f1f1;font-size:10px;padding:10px',
                baseCls:'bckgroundcolor',
                layout: 'border',
                items:[this.CustomerInfoForm,this.pan
                ]
            })
        );
        this.CustomerInfoForm.cascade(function(comp){
            if(comp.isXType('field')){
                comp.on('change', function(){this.isClosable=false;},this);
            }
        },this);
        this.name.on('change',this.checkDuplicateName ,this);
        this.email.on('change',this.checkDuplicateEmail ,this);
        this.copyAddress.on('check',this.setAddress ,this);        
   },
   createStore:function(){
        this.parentRec = Wtf.data.Record.create ([
            {name:'parentid',mapping:'accid'},
            {name:'parentname',mapping:'accname'},
//            {name:'leaf',type:'int'},
            {name:'email'},
            {name:'deleted'}
//            {name:'level',type:'int'}
        ]);
        this.parentStore = new Wtf.data.Store({
//            url:Wtf.req.account+this.businessPerson+'Manager.jsp',
            url:"ACC"+this.businessPerson+"/get"+this.businessPerson+"sForCombo.do",
            baseParams:{
                mode:2,
                accountid:(this.record==null?null:this.record.data['accid']),
                group:(this.isCustomer?[10]:[13])                
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.parentRec)
        });
//        Wtf.termRec = new Wtf.data.Record.create([
//                {name: 'termid'},
//                {name: 'termname'},
//                {name: 'termdays'}
//        ]);
//        Wtf.termds = new Wtf.data.Store({
//            reader: new Wtf.data.KwlJsonReader({
//                root: "data"
//            },Wtf.termRec),
////            url: Wtf.req.account + 'CompanyManager.jsp',
//            url : "ACCTerm/getTerm.do",
//            baseParams:{
//                mode:91
//            }
//        });


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
//            url:Wtf.req.account+'CompanyManager.jsp'
            url:"ACCCurrency/getCurrencyExchange.do"
        });
        this.currencyStore.on("load",function(store){
            if(store.getCount()==0){
                callCurrencyExchangeWindow(this.currencyExchangeWinId);
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.common.currency")],2);     //'Alert',"Please set Currency Exchange Rates"
                Wtf.getCmp(this.currencyExchangeWinId).on("update", function(){
                    this.currencyStore.reload();
                },this);
            }
        },this)
        this.hideLoading(true);

        this.parentStore.load();
        this.currencyStore.load({params:{mode:201,transactiondate:WtfGlobal.convertToGenericDate(new Date())}});
        chktermload();
        chktitleload();
        if(this.businessPerson=="Customer"){
            chkCustomerCategoryload();
        } else {
            chkVendorCategoryload();
        }
        
        this.parentStore.on('load',this.loadRecord,this);
        Wtf.TitleStore.on('load',this.setPersonTitle,this);
        Wtf.termds.on('load',this.setPDM,this);
        this.currencyStore.on('load', function(){
            if(this.isEdit) this.Currency.setValue(this.record.data.currencyid);
            this.hideLoading(false);
        },this);
        Wtf.CustomerCategoryStore.on("load", this.setCategory, this);
        Wtf.VendorCategoryStore.on("load", this.setCategory, this);

        Wtf.termds.on('loadexception',this.hideLoading.createDelegate(this,[false]),this);
        this.currencyStore.on('loadexception',this.hideLoading.createDelegate(this,[false]),this);
        this.parentStore.on('loadexception',this.hideLoading.createDelegate(this,[false]),this);

//        this.CategoryStore=new Wtf.data.Store({
//            url:"ACCMaster/getMasterItems.do",
//            baseParams:{
//                mode:112,
//                groupid:(this.businessPerson=="Customer"?7:8)
//             },
//            reader: new Wtf.data.KwlJsonReader({
//                root: "data"
//            },Wtf.CategoryRec)
//        });
//        this.CategoryStore.load();
//        if(this.isEdit){
//            this.CategoryStore.on("load", function(){
//                if(this.CategoryStore.getCount()>0){
//                    this.Category.setValue(this.record.data.categoryid);
//                }
//            },this);
//        }


    },
    createFields:function(){ 
        this.Title= new Wtf.form.FnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.cust.title"),  //'Title',
            name:'title',
            forceSelection: true,
            store:Wtf.TitleStore,
            valueField:'id',
            displayField:'name'//,
           // addNewFn:this.addMaster.createDelegate(this,[6,Wtf.TitleStore])
        });
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.masterconfig, Wtf.Perm.masterconfig.create))
            this.Title.addNewFn=this.addMaster.createDelegate(this,[6,Wtf.TitleStore])
        
        this.Category= new Wtf.form.FnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.cust.category")+WtfGlobal.addLabelHelp((this.isCustomer?WtfGlobal.getLocaleText("acc.cust.msg1"):WtfGlobal.getLocaleText("acc.ven.msg1"))),
            name:'category',
            forceSelection: true,
            store: (this.businessPerson=="Customer")?Wtf.CustomerCategoryStore:Wtf.VendorCategoryStore,
//            store: this.CategoryStore,
            valueField:'id',
            displayField:'name',
            addNoneRecord: true//,
//            addNewFn: this.addMaster.createDelegate(this,[(this.businessPerson=="Customer"?7:8),this.CategoryStore])
          //  addNewFn: (this.businessPerson=="Customer")?this.addMaster.createDelegate(this,[7,Wtf.CustomerCategoryStore]):this.addMaster.createDelegate(this,[8,Wtf.VendorCategoryStore])
        });
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.masterconfig, Wtf.Perm.masterconfig.create))
            this.Category.addNewFn= (this.businessPerson=="Customer")?this.addMaster.createDelegate(this,[7,Wtf.CustomerCategoryStore]):this.addMaster.createDelegate(this,[8,Wtf.VendorCategoryStore])
        this.name= new Wtf.form.ExtendedTextField({
            fieldLabel:this.isCustomer?WtfGlobal.getLocaleText("acc.cust.name"):WtfGlobal.getLocaleText("acc.ven.name"),  //this.businessPerson+" Name *",
            name: 'accname',
            id:'accname',
            allowBlank:false,
            maxLength:50,
//            regex:Wtf.specialChar,
            listeners:{
                scope:this,
                focus:function(){
                    this.searchText(this.parentStore,this.pan);
                }
            }
        });
        this.creationdate= new Wtf.form.DateField({
            fieldLabel:WtfGlobal.getLocaleText("acc.cust.creationDate"),  //'Creation Date',
            name: 'creationDate',
            width:150,
            format:WtfGlobal.getOnlyDateFormat(),
            disabled:this.isEdit,
            value: Wtf.serverDate,
            allowBlank:false
        });
        this.balTypeStore = new Wtf.data.SimpleStore({
            fields: [{name:'typeid',type:'boolean'}, 'name'],
            data :[[true,'Debit'],[false,'Credit']]
        });
        this.balTypeEditor = new Wtf.form.ComboBox({
            store: this.balTypeStore,
            fieldLabel:WtfGlobal.getLocaleText("acc.cust.openType"),  //'Opening Balance Type',
            name:'debitType',
            displayField:'name',
            disabled:true,
            forceSelection: true,
            allowBlank:false,
            width:150,
            valueField:'typeid',
            mode: 'local',
            value:this.isCustomer,
            triggerAction: 'all',
            typeAhead:true,
            selectOnFocus:true
        });
        this.openingBal= new Wtf.form.NumberField({
            fieldLabel: this.isCustomer?WtfGlobal.getLocaleText("acc.cust.open"):WtfGlobal.getLocaleText("acc.ven.open"),  //   //"Opening Balance in "+WtfGlobal.getCurrencySymbolForForm(),		
            name: 'openbalance',
            width:150,
            maxLength:15,
            decimalPrecision:2,
            value:0,
            allowBlank:false,
            allowNegative:false,
            xtype:'numberfield'
        });
        
        this.openingBal.on('blur',function(field){if(field.getValue() == ""){field.setValue(0);}},this);
        this.openingBal.on('focus',function(field){if(field.getValue() == 0){field.setValue("");}},this);
        
        this.address= new Wtf.form.TextArea({
            fieldLabel: WtfGlobal.getLocaleText("acc.cust.add") +" *",  //'Address *',
            name: 'address',
            allowBlank:false,
            maxLength:200
        });
        this.email= new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.cust.email"),  //'Email',
            name: 'email',
            allowBlank:true,
            maxLength:45,
            vtype:'email'
        });
        this.conNo= new Wtf.form.TextField({
           fieldLabel: WtfGlobal.getLocaleText("acc.cust.no"),  //'Contact No',
           name: 'contactno',
           maxLength:15//,
          // regex:Wtf.PhoneRegex
        });
        this.taxIDNo= new Wtf.form.TextField({
           fieldLabel: WtfGlobal.getLocaleText("acc.cust.taxID"),  //'Tax ID Number',
           name: 'taxidnumber',
           maxLength:20,
           hideLabel:this.isCustomer||Wtf.account.companyAccountPref.withouttax1099,
           hidden:this.isCustomer||Wtf.account.companyAccountPref.withouttax1099
        });
        this.Eligible1099= new Wtf.form.Checkbox({
            name:'taxeligible',
            fieldLabel:WtfGlobal.getLocaleText("acc.ven.1099"),  //'Eligible for 1099',
            checked:false,
            hideLabel:this.isCustomer||Wtf.account.companyAccountPref.withouttax1099,
            hidden:this.isCustomer||Wtf.account.companyAccountPref.withouttax1099,
            style: 'padding:0px 0px 10px 0px;',
            width: 10
        })
        this.altPhNo=new Wtf.form.TextField({
           fieldLabel: WtfGlobal.getLocaleText("acc.cust.Altno"),  //'Alternate Phone No',
           name: 'contactno2',
           maxLength:15//,
          // regex:Wtf.PhoneRegex
        });
        this.fax=new Wtf.form.TextField({
           fieldLabel: WtfGlobal.getLocaleText("acc.cust.fax"),  //'Fax',
           name: 'fax',
           maxLength:15//,
          // regex:Wtf.PhoneRegex
        });
        this.SAddress= new Wtf.form.TextArea({
           fieldLabel: WtfGlobal.getLocaleText("acc.cust.shipAdd"),  //'Shipping Address',
           name: 'shippingaddress',
           maxLength:200,
           hideLabel:(!this.isCustomer),
           xtype:'textarea'
        });
        this.bankaccountno = new Wtf.form.TextField({
           fieldLabel: WtfGlobal.getLocaleText("acc.cust.bankAcc"),  //'Bank Account No',
           maxLength:30,
           name: 'bankaccountno'
        });
        this.other= new Wtf.form.TextArea({
            fieldLabel:WtfGlobal.getLocaleText("acc.cust.otherInfo"),  //'Other Information',
            name:'other',
            id:'other',
            width:158,
            height:40,
            xtype:'textarea',
            maxLength:200
        });
        this.parentAccount= new Wtf.form.FnComboBox({
            fieldLabel:this.isCustomer?WtfGlobal.getLocaleText("acc.cust.parent"):WtfGlobal.getLocaleText("acc.ven.parent"),  //'Parent '+this.businessPerson,
            hiddenName:'parentid',
            store:this.parentStore,
            valueField:'parentid',
            displayField:'parentname',
            emptyText:WtfGlobal.getLocaleText("acc.cust.sel"),  //'Select Parent...',
            typeAhead: true,
            disableOnField: "deleted",
            forceSelection: true,
            mode: 'local',
            width:160,
            hirarchical:true,
            triggerAction: 'all'
        });

        this.Currency= new Wtf.form.FnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.cust.currency") +" *",  //'Currency*',
            hiddenName:'currencyid',
            name:'currencyid',
            disabled:this.isEdit,
            width:158,
            allowBlank:false,
            store: this.currencyStore,
            valueField:'currencyid',
            emptyText:WtfGlobal.getLocaleText("acc.cust.currencyTT"),  //'Please select Currency...',
            forceSelection: true,
            displayField:'currencyname',
            scope:this,
            selectOnFocus:true
//            listeners:{
//                'select':{
//                    fn:this.getCurrencySymbol,
//                    scope:this
//                }
//            }
        });
// TODO       this.modeStore=new Wtf.data.SimpleStore({
//            fields:[{name:"id"},{name:"name"}],
//            data:[[1,"Email"],[2,"Print"]]
//        });
//        this.pdm=new Wtf.form.ComboBox({
//            fieldLabel:'Preferred Delivery Mode*',
//            hiddenName:'pdm',
//            store:this.modeStore,
//            valueField:'id',
//            displayField:'name',
//            mode: 'local',
//            typeAhead: true,
//            allowBlank:false,
//            forceSelection:true,
//            triggerAction:'all'
//       });
       this.CreditTerm= new Wtf.form.FnComboBox({
           fieldLabel:this.isCustomer?WtfGlobal.getLocaleText("acc.cust.term") +" *":WtfGlobal.getLocaleText("acc.ven.term") +" *",  //'Credit Term*':'Debit Term*',
           hiddenName:'termid',
           allowBlank:false,
           store:Wtf.termds,
           valueField:'termid',
//           addNewFn:this.callCreditTerm.createDelegate(this),
           displayField:'termname',
           selectOnFocus:true,
           forceSelection:true
        });

        if(!WtfGlobal.EnableDisable(Wtf.UPerm.creditterm, Wtf.Perm.creditterm.edit))
            this.CreditTerm.addNewFn= this.callCreditTerm.createDelegate(this)
        
        this.issubFieldset=new Wtf.form.FieldSet({
            border:false,
            xtype:'fieldset',
            autoWidth:true,
            autoHeight:true,
            checkboxName:"issub",
            checkboxToggle:true,
            title:this.isCustomer?WtfGlobal.getLocaleText("acc.cust.sub"):WtfGlobal.getLocaleText("acc.ven.sub"),  //'Is a sub'+this.businessPerson.toLowerCase()+"?",
            collapsed:true,
            defaults:{border:false},
            items:[this.parentAccount]
        });
        this.copyAddress= new Wtf.form.Checkbox({
            name:'copyadress',
            fieldLabel:WtfGlobal.getLocaleText("acc.cust.copyAdd"),  //'Copy Address',
            checked:false,
            hideLabel:!this.isCustomer,
            hidden:!this.isCustomer,
            style: 'padding:0px 0px 10px 0px;',
            width: 10
        });
        this.taxNo=new Wtf.form.TextField({
           fieldLabel: WtfGlobal.getLocaleText("acc.cust.taxID"),  //'Tax Id',
           name: 'taxno',
           hideLabel:this.isCustomer,
           hidden:this.isCustomer,
           maxLength:255
        });        
        this.issubFieldset.on("beforeexpand",this.checkParent,this)
        this.openingBal.on('change',function(a,newval){if(newval==0)this.balTypeEditor.disable();else this.balTypeEditor.enable();},this)
    },  
    createForm:function(){
        this.CustomerInfoForm=new Wtf.form.FormPanel({ 
            region:'center', 
            autoHeight:true,
            labelWidth:150,
            border:false,
            bodyStyle: "background: transparent; padding: 20px;",
            defaultType: 'textfield',
            defaults:{
                width:200
            },
            items:[{
                xtype:'hidden',
                name:'accid'
            },
            this.Title,this.name,this.openingBal,this.balTypeEditor,this.creationdate,this.Currency,this.issubFieldset,this.address,this.email,this.conNo,this.altPhNo,this.taxIDNo,this.Eligible1099,this.fax,this.copyAddress,this.SAddress,this.other,this.bankaccountno,this.CreditTerm,this.Category
        ]
        });
         this.pan=new Wtf.Panel({
            width:150,
            layout:'fit',
            region:'east',
            style:"padding:20px 13px 30px 0px;",
            //autoScroll:true,
            title:this.isCustomer?WtfGlobal.getLocaleText(""):WtfGlobal.getLocaleText("")  //"Existing "+(this.isCustomer?"Customers":"Vendors")
        });
       // this.pan.hide();

    },
    searchText:function(cAccTypeStore,pan){ 
        document.getElementById("accname").onkeyup=function(){
            this.text=Wtf.getCmp("accname").getValue();
            if(this.text=="" && this.tpl != undefined){
            	this.tpl.overwrite(pan.body,"");	// 20140 fixed
//                pan.hide();						
            }
            else{
                cAccTypeStore.filterBy(function(rec){
                    var FIND = String(this.text);
                    FIND = new RegExp('^'+Wtf.escapeRe(FIND), 'i');
                    return rec && FIND.test(rec.data.parentname) && !rec.data.deleted;
                },this);

                this.len=cAccTypeStore.getCount();
                if(this.len==0 && this.tpl != undefined){
                	this.tpl.overwrite(pan.body,"");	// 20140 fixed
//                    pan.hide();
                }
                for(var i=0;i<this.len;i++){
                    this.presentAcc=cAccTypeStore.getAt(i).get("parentname");
                    this.tdata={
                        paname:this.presentAcc
                    }
                    this.tpl=new Wtf.Template('<font size=2> <p>{paname}</p></font>');
                    if(i==0)
                        this.tpl.overwrite(pan.body,this.tdata);
                    else
                        this.tpl.append(pan.body,this.tdata);

                    pan.show();
                }
            }
        };        
    },
    sendMail:function(){
        if(!this.isCustomer){
            if(this.record.data.taxidnumber!=""){
                 Wtf.MessageBox.show({
                    title: WtfGlobal.getLocaleText("acc.common.warning"),  //'Warning',
                    msg: WtfGlobal.getLocaleText("acc.ven.msg2") +   //"You have already filled TAX ID NUMBER for this Vendor."+
                    WtfGlobal.getLocaleText("acc.ven.msg3") +   //"Requesting TAX ID NUMBER from Vendor will result in overriding the existing TAX ID NUMBER by the one filled by Vendor."
                    WtfGlobal.getLocaleText("acc.ven.msg4"),   //+"Do You want to continue?",
                    buttons: Wtf.MessageBox.YESNO,
                    animEl: 'mb9',
                    fn:function(btn){
                        if(btn!="yes")return;
                        callTaxEmailWin("vendoremailwin",this.record,true);
                    },
                    scope:this,
                    icon: Wtf.MessageBox.QUESTION
                },this);
            }else{
                callTaxEmailWin("vendoremailwin",this.record,true);
            }
        }
    },
    setAddress:function(o,newval,oldval){
            var val=newval?this.address.getValue():"";
            if(this.isCustomer)
                this.SAddress.setValue(val);
    },
    checkParent:function(){
        if(this.parentStore.getCount()==0)
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),this.isCustomer?WtfGlobal.getLocaleText("acc.cust.msg3"):WtfGlobal.getLocaleText("acc.ven.msg5")],2);     //"Alert","No Parent "+this.businessPerson+" exist"
    },
    setPersonTitle:function(){
        if(Wtf.TitleStore.getCount()>0){
            if(this.isEdit){
                this.Title.setValue(this.record.data.title);
            } else {
//               this.Title.setValue(Wtf.TitleStore.getAt(Wtf.TitleStore.getCount()-1).get('id')); //Don't set default title
            }
        }
        Wtf.TitleStore.un("load", this.setPersonTitle, this);
    },
    setCategory: function(){
        if(this.isEdit){
            if(this.businessPerson=="Customer") {
                if(Wtf.CustomerCategoryStore.getCount()>0){
                    this.Category.setValue(this.record.data.categoryid);
                }
            } else {
                if(Wtf.VendorCategoryStore.getCount()>0){
                    this.Category.setValue(this.record.data.categoryid);
                }
            }
        }
        Wtf.CustomerCategoryStore.un("load", this.setCategory, this);
        Wtf.VendorCategoryStore.un("load", this.setCategory, this);
    },

    hideLoading:function(val){
//        this.saveBttn.setDisabled(val);
//        this.cancelBttn.setDisabled(val);
        if(!val){
            this.fireEvent("loadingcomplete",this);
            this.name.focus();      //To set focus on Name on form load.
        }
    },
    setPDM:function(){
//        this.hideLoading(false);
        if(Wtf.termds.getCount()>0){
            if(this.isEdit){
                this.CreditTerm.setValue(this.record.data.termid);
            } else {
                this.CreditTerm.setValue(Wtf.termds.getAt(Wtf.termds.getCount()-1).get('termid'));
            }
        }
        Wtf.termds.un("load", this.setPDM, this);
    },
    addMaster:function(id,store){
        addMasterItemWindow(id);
        Wtf.getCmp('masterconfiguration').on('update', function(){
            store.reload();
        }, this);
    },
    closeForm:function(){
        if(this.isClosable!==true){
            Wtf.MessageBox.show({
                title: WtfGlobal.getLocaleText("acc.common.warning"),  //'Warning',
                msg: WtfGlobal.getLocaleText("acc.common.msg2")+"<br>"+WtfGlobal.getLocaleText("acc.common.msg3")+"</br>",  //"The data you have entered is unsaved.<br>Are you sure you want to close the window?",
                buttons: Wtf.MessageBox.YESNO,
                animEl: 'mb9',
                fn:function(btn){
                    if(btn!="yes")return;

                     this.fireEvent('cancel',this);
                     this.close();
                },
                scope:this,
                icon: Wtf.MessageBox.QUESTION
            });
        } else {
            this.fireEvent('cancel',this);
            this.close();
        }
    },
    checkDuplicateName:function(o,newval,oldval){
        this.name.setValue(this.name.upper(this.name.getValue()));
        this.isClosable=false;

        this.parentStore.clearFilter(true)
        var FIND = this.name.getValue().trim().toLowerCase();
        FIND =FIND.replace(/\s+/g, '');
        var index=this.parentStore.findBy( function(rec){
             var parentname=rec.data['parentname'].trim().toLowerCase();
             parentname=parentname.replace(/\s+/g, '');
            if(parentname===FIND && !rec.data.deleted) // Add non-deleted record check
                return true;
             else
                return false
        })
        if(index>=0){
            if(this.isCustomer)
                WtfComMsgBox(37,2);
            else
                WtfComMsgBox(38,2);
            this.name.setValue(oldval)
            //return;
        }
    },
    checkDuplicateEmail:function(o,newval,oldval){
      if(this.email.getValue() != ""){
    	this.parentStore.clearFilter(true)
	    var FIND = this.email.getValue().trim().toLowerCase();
	    FIND =FIND.replace(/\s+/g, '');
	    var index=this.parentStore.findBy( function(rec){
	    var parentname=rec.data['email'].trim().toLowerCase();
	    parentname=parentname.replace(/\s+/g, '');
	    if(parentname===FIND)
	        return true;
	    else
	        return false
	    })
	    if(index>=0){
	        if(this.isCustomer)
	            WtfComMsgBox(43,2);
	        else
	            WtfComMsgBox(44,2);
	        this.email.setValue(oldval)
	     }
      }
    },
    saveForm:function(){
        this.name.setValue(this.name.getValue().trim());
        var isValid = this.CustomerInfoForm.getForm().isValid();
        var tmp = this.conNo.getValue().trim();
        if(tmp.length>0 && tmp.length<10){
            this.conNo.markInvalid(WtfGlobal.getLocaleText("acc.cust.msg2"));
            isValid = false;
        }
        tmp = this.altPhNo.getValue().trim();
        if(tmp.length>0 && tmp.length<10){
            this.altPhNo.markInvalid(WtfGlobal.getLocaleText("acc.cust.msg2"));
            isValid = false;
        }
        tmp = this.fax.getValue().trim();
        if(tmp.length>0 && tmp.length<10){
            this.fax.markInvalid(WtfGlobal.getLocaleText("acc.cust.msg2"));
            isValid = false;
        }

        if(!isValid){   //if(!this.CustomerInfoForm.getForm().isValid()){
            WtfComMsgBox(2,2);
        }else{
            this.msg= WtfComMsgBox(27,4,true);
            var rec=this.CustomerInfoForm.getForm().getValues();
            rec.currencyid=this.Currency.getValue();
            rec.creationDate=WtfGlobal.convertToGenericDate(this.creationdate.getValue());
            rec.category= this.Category.getValue();
            rec.mode=1;
            rec.parentname=this.parentAccount.getRawValue();
            rec.taxeligible=this.Eligible1099.getValue()//=='on'?true:false);
             rec.debitType=this.balTypeEditor.getValue();
            Wtf.Ajax.requestEx({
                url:"ACC"+this.businessPerson+"/save"+this.businessPerson+".do",
//                url:Wtf.req.account+this.businessPerson+'Manager.jsp',
                params: rec
            },this,this.genSuccessResponse,this.genFailureResponse);
       }
    },
    callCreditTerm:function(){
        callCreditTerm('creditTermReportWin');
//        Wtf.getCmp('creditTermReportWin').on('update', function(){ // Already Reloaded on success in UpdateWindow.js(line:282) [SK]
//            Wtf.termds.reload();
//        }, this);
    },
    genSuccessResponse:function(response){
         WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.info"),response.msg],response.success*2+1);
        if(response.success){
          this.fireEvent('update',this,response.perAccID);
          
        //Neeraj
          var tabid = 'ledger';
          if(Wtf.getCmp(tabid)!=undefined) {
              Wtf.getCmp(tabid).accStore.reload();
          }
          
            //On updation of category reload 'by category report grid'
            if(!this.isEdit || (this.isEdit && this.record.data.categoryid!=this.Category.getValue())) {
                var panel= (this.businessPerson=="Customer")?Wtf.getCmp("CustomerByCategoryDetails"):Wtf.getCmp("VendorByCategoryDetails");
                if(panel!=null || panel!=undefined){
                    panel.isCategoryUpdated=true;
                }
            }
            WtfGlobal.loadpersonacc(this.isCustomer);
            this.close();
            
        }
// TO DO           if(!this.isEdit&&this.taxIDNo.getValue()==""){
//                Wtf.MessageBox.show({
//                    title: 'Warning',
//                    msg: "Do you want to send 1099 Tax ID request?",
//                    buttons: Wtf.MessageBox.YESNO,
//                    animEl: 'mb3',
//                    fn:function(btn){
//                        if(btn!="yes"){this.close();}
//                        this.sendMail();
//            this.close();
//    },
//                    scope:this,
//                    icon: Wtf.MessageBox.QUESTION
//                });
//            }
//                    if(response.success){
//           if(!this.isEdit&&this.taxIDNo.getValue()==""&&!this.isCustomer&&this.Eligible1099.getValue()){
//                Wtf.MessageBox.show({
//                    title: this.businessPerson+' Info',
//                    msg: response.msg+" Do you want to send 1099 Tax ID request?",
//                    buttons: Wtf.MessageBox.YESNO,
//                    animEl: 'mb3',
//                    scope:this,
//                    fn:function(btn){
//                        if(btn!="yes"){this.onSucess(response);}
//                        var rec=this.CustomerInfoForm.getForm().getValues();
//                         callTaxEmailWin("vendoremailwin",rec,true);
//                        this.onSucess(response);
//                    },
//
//                    icon: Wtf.MessageBox.QUESTION
//                });
//            }else{
//                WtfComMsgBox([this.businessPerson+' Info',response.msg],response.success*2+1);
//                this.onSucess(response)
//            }
//        }
  //          if(this.isCustomer)

    },
    genFailureResponse:function(response){
        var msg=WtfGlobal.getLocaleText("acc.common.msg1");  //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
        this.close();
    }
 });
