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

Wtf.account.COA = function(config){
    this.cashbank=false;
    this.incomenature=config.incomenature;
    this.ispurchase=config.ispurchase;
    this.islibility=config.islibility;
    this.isFixedAsset=config.isFixedAsset;
    this.isotherexpense=config.isotherexpense;
    this.isexpense=config.isexpense;
    this.issales=config.issales;
    this.currencyExchangeWinId = "SetCurrencyExchangeWin";
    Wtf.apply(this,{
        constrainHeader :true,		// 19991
        buttons: [{
            text: WtfGlobal.getLocaleText("acc.common.saveBtn"),  //'Save',
            scope: this,
            handler:this.saveForm.createDelegate(this)
        }, {
            text: WtfGlobal.getLocaleText("acc.common.cancelBtn"),  //'Cancel',
            scope: this,
            handler:this.closeForm.createDelegate(this)
        }]
    },config);
    Wtf.account.COA.superclass.constructor.call(this, config);
    this.addEvents({
        'update':true,
        'cancel':true,
        'loadingcomplete':true
    });
}

Wtf.extend(Wtf.account.COA, Wtf.Window, {
    loadRecord:function(){
      if(!this.isFixedAsset)
        this.openingBal.setValue(0);
        if(this.record!=null){
            if(this.record.data['parentid'])
                this.subAccount.toggleCollapse();
            this.coaForm.getForm().loadRecord(this.record);
            var bal=this.openingBal.getValue();
            if(bal!==0 || this.isFixedAsset){		// Neeraj Opening balance not used anymore for asset value
                this.openingBal.setValue(Math.abs(bal));
                this.balTypeEditor.setValue(bal>0);
            }else{
                var rec=this.accGroup.store.getAt(this.accGroup.store.find("groupid",this.accGroup.getValue()))
                if(rec) {
                    this.setOBType(rec.data["nature"]);
                }
            }
            if(this.isFixedAsset){
            	this.openingBal.setValue(this.record.data.presentbalance);
            }
            this.creationdate.setValue(this.record.data.creationDate);
            this.salvage.setValue(this.record.data.salvage);
            this.life.setValue(this.record.data.life);
            this.Depreciation.setValue(this.record.data.depreciationaccount);
            this.Category.setValue(this.record.data.categoryid);
            this.CostCenter.setValue(this.record.data.costcenterid);
        }
        else
            this.Depreciation.setValue(Wtf.account.companyAccountPref.depreciationaccount);
         if(this.issales){
            this.accGroup.setValue('5');
            this.balTypeEditor.setValue(false);
            this.accGroup.setDisabled(true);
        }
        else if(this.incomenature) {  
   //         this.accGroup.setValue('5');
            this.balTypeEditor.setValue(false);
        }
        else if(this.ispurchase){
            this.accGroup.setValue('6');
            this.accGroup.setDisabled(true);
            this.balTypeEditor.setValue(true);
        }
        else if(this.islibility){ 
            this.accGroup.setValue('3');
            this.balTypeEditor.setValue(false);
        }
         else if(this.isotherexpense){
            this.accGroup.setValue('8');
            this.accGroup.setDisabled(true);
            this.balTypeEditor.setValue(true);
        }
        else if(this.isexpense){
            this.accGroup.setValue('7');
            this.accGroup.setDisabled(true);
            this.balTypeEditor.setValue(true);
        }
         else if(this.isFixedAsset){
            this.accGroup.setValue('12');
            this.accGroup.setDisabled(true);
            this.balTypeEditor.setValue(true);
        }
        this.hideLoading(false);
    },
    
    onRender: function(config) {
        Wtf.account.COA.superclass.onRender.call(this, config);
        this.createBank();
        this.createFields();
        this.createForm();
        this.createPanel();
        var msg=(this.isFixedAsset?WtfGlobal.getLocaleText("acc.fixedAssetList.tabTitle"):WtfGlobal.getLocaleText("acc.bankBook.gridAccount"));  //(this.record==null? "Add New ":"Edit ")+(this.isFixedAsset?'Fixed Asset':'Account');
        if(this.isEdit && this.isFixedAsset)
        	this.getID();
        if(this.isFixedAsset){
        }
        this.add({
            region: 'north',
            height: 75,
            border: false,
            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
            html: getTopHtml(this.title,msg,"../../images/accounting_image/Chart-of-Accounts.gif")
        }, this.centerPanel=new Wtf.Panel({
            region: 'center',
            border: false,
            autoScroll:true,
            bodyStyle: 'background:#f1f1f1;font-size:10px;padding:10px',
            baseCls:'bckgroundcolor',
            layout: 'border',
            items:[this.coaForm,{
                region: 'east',
                border: false,
                width:170,
                layout: 'fit',
                bodyStyle: 'background:#f1f1f1;font-size:10px;',
                items:[this.pan]
            }]  
        }));
    },

    hideLoading:function(val){
        if(!val){
            this.fireEvent("loadingcomplete",this);
            this.AccName.focus();      //To set focus on Name on form load.
        }
    },

    createFields:function(){
    	                                     
         var groupRec=new Wtf.data.Record.create([
            {name: 'groupid'},
            {name: 'groupname'},
            {name: 'nature'},
            {name: 'leaf',type:'boolean'},
            {name: 'level', type:'int'}
        ]);

        var groupStore=new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },groupRec),
//            url: Wtf.req.account+'CompanyManager.jsp',
            url : "ACCAccount/getGroups.do",
            baseParams:{
                mode:1,
                ignorevendors:true,
                ignorecustomers:true
            }
        });

        this.accRec = Wtf.data.Record.create ([
            {name:'accountname',mapping:'accname'},
            {name:'accountid',mapping:'accid'}
//            {name:'level', type:'int'}
        ]);
         this.depStore = new Wtf.data.Store({
//            url: Wtf.req.account+'CompanyManager.jsp',
            url:"ACCAccount/getAccountsForCombo.do",
            baseParams:{
                mode:2,
                includeparentid:Wtf.account.companyAccountPref.depreciationaccount,
                group:[8]
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.accRec)
        });
        var parentRec=new Wtf.data.Record.create([
            {name: 'parentid',mapping:'accid'},
            {name: 'groupid'},
            {name: 'nature'},
            {name: 'parentname',mapping:'accname'},
            {name: 'deleted'}
//            {name: 'leaf',type:'boolean'},
//            {name: 'level',type:'int'}
        ]);

        this.parentStore=new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },parentRec),
//            url: Wtf.req.account+'CompanyManager.jsp',
            url:"ACCAccount/getAccountsForCombo.do",
            baseParams:{
                mode:2,
                ignorecustomers:true,
                ignorevendors:true,
                accountid:(this.record!=null?this.record.data['accid']:null)
            }
        });
        this.parentStore.on('load',this.loadRecord,this);
        groupStore.on('load',function(){
            if(this.issales)
                this.parentStore.load({params:{group:[5]}});
            else if(this.ispurchase)
                this.parentStore.load({params:{group:[6]}});
            else if(this.isotherexpense)
                this.parentStore.load({params:{group:[8]}});
             else if(this.isexpense)
                this.parentStore.load({params:{group:[7]}});
            else if(this.isFixedAsset)
                this.parentStore.load({params:{group:[12]}});
            else if(this.cashbank)
                this.parentStore.load({params:{group:[9,18]}});
            else if(this.incomenature)
                this.parentStore.load({params:{nature:[Wtf.account.nature.Income]}});
            else
                this.parentStore.load({params:{group:[12],ignore:true}});
        },this);        
        this.depStore.on('load',function(){
            if(this.isFixedAsset)
                groupStore.load({params:{group:[12]}});
            else if(this.cashbank)
                groupStore.load({params:{group:[9,18]}});
            else if(this.incomenature)
                groupStore.load({params:{nature:[Wtf.account.nature.Income]}});
            else
                groupStore.load({params:{group:[12],ignore:true}});
        },this);
        this.depStore.on('load',function(){this.Depreciation.setValue(Wtf.account.companyAccountPref.depreciationaccount);},this)
        this.depStore.load();
        chkcurrencyload();

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
         if(store.getCount()==0){
            callCurrencyExchangeWindow(this.currencyExchangeWinId);
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),"Please set Currency Exchange Rates"],2);
            Wtf.getCmp(this.currencyExchangeWinId).on("update", function(){
                this.currencyStore.reload();
            },this);
        }
     },this);
     this.currencyStore.load({params:{mode:201,transactiondate:WtfGlobal.convertToGenericDate(new Date())}});
     this.depStore.on('loadexception',this.hideLoading.createDelegate(this,[false]),this);
     groupStore.on('loadexception',this.hideLoading.createDelegate(this,[false]),this);
     this.currencyStore.on('loadexception',this.hideLoading.createDelegate(this,[false]),this);
     this.parentStore.on('loadexception',this.hideLoading.createDelegate(this,[false]),this);

        this.Currency= new Wtf.form.FnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.coa.gridCurrency") + "*",  //'Currency*',
            hiddenName:'currencyid',
            disabled:this.isEdit,
            //hidden:this.isFixedAsset,
            //hideLabel:this.isFixedAsset,
//            anchor: '90%',
            width:150,
            allowBlank:false,
            store:this.currencyStore,
            valueField:'currencyid',
            emptyText:WtfGlobal.getLocaleText("acc.cust.currencyTT"),  //'Please select Currency...',
            forceSelection: true,
            displayField:'currencyname',
            scope:this,
            selectOnFocus:true

        });
        this.cmbParent= new Wtf.form.FnComboBox({
            fieldLabel:this.isFixedAsset?WtfGlobal.getLocaleText("acc.rem.21"):WtfGlobal.getLocaleText("acc.rem.20"),  //'Fixed Asset Parent Name*':'Parent Account',
            hiddenName:'parentid',
            name:'parentid',
            store:this.parentStore,
            valueField:'parentid',
            displayField:'parentname',
            disableOnField: "deleted",
            width:150,
            mode: 'local',
            disableKeyFilter:true,
            triggerAction:'all',
            typeAhead: true,
            hirarchical:true, 
            forceSelection:true
        });

        this.AccName=new Wtf.form.ExtendedTextField({
            fieldLabel:this.isFixedAsset?WtfGlobal.getLocaleText("acc.rem.22"):WtfGlobal.getLocaleText("acc.rem.23"),  //'Fixed Asset Name*': 'Account Name*',
            name: 'accname',
            width:150,
//            regex:Wtf.specialChar,
            maxLength:50,
            id:'accname',
            allowBlank:false,
            listeners:{
                scope:this,
                focus:function(){
                    this.searchText(this.parentStore,this.pan);
                }
            }
        });
        
        this.creationdate= new Wtf.form.DateField({
            fieldLabel: this.isFixedAsset?WtfGlobal.getLocaleText("acc.rem.24"):WtfGlobal.getLocaleText("acc.rem.25"),  //'Creation Date':'As On',
            name: 'creationDate',
            width:150,
            format:WtfGlobal.getOnlyDateFormat(),
            disabled:this.isEdit,
            value: Wtf.serverDate.clearTime(),
            allowBlank:false
        });

        this.subAccount=new Wtf.form.FieldSet({
            title: this.isFixedAsset?WtfGlobal.getLocaleText("acc.rem.26"):WtfGlobal.getLocaleText("acc.rem.19"),  //'Is a Sub Asset':'Is a subaccount?',
            checkboxToggle: true,
            autoHeight: true,
            width:325,
         //   hidden:this.isFixedAsset,
            border:false,
            checkboxName: 'subaccount',
            style: 'margin-left:-10px',
            collapsed: true,
            items:[this.cmbParent]
        });

//        this.AssetCategoryStore=new Wtf.data.Store({
//            url:"ACCMaster/getMasterItems.do",
//            baseParams:{
//                mode:112,
//                groupid:9
//            },
//            reader: new Wtf.data.KwlJsonReader({
//                root: "data"
//            },Wtf.CategoryRec)
//        });
//        this.AssetCategoryStore.load();

        this.Category= new Wtf.form.FnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.cust.category"),  // 'Category',
            name: 'category',
            hidden: !this.isFixedAsset,
            hideLabel: !this.isFixedAsset,
            forceSelection: true,
            store: Wtf.AssetCategoryStore,
//            store: this.AssetCategoryStore,
            valueField: 'id',
            displayField: 'name',
            width:150,
            addNoneRecord: true
//            addNewFn: this.addMaster.createDelegate(this,[9,this.AssetCategoryStore])
//            addNewFn: this.addMaster.createDelegate(this,[9,Wtf.AssetCategoryStore])
        });
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.masterconfig, Wtf.Perm.masterconfig.create))
            this.Category.addNewFn= this.addMaster.createDelegate(this,[9,Wtf.AssetCategoryStore])
        chkAssetCategoryload();
        Wtf.AssetCategoryStore.on("load", this.setCategory, this);

        this.accGroup=new Wtf.form.FnComboBox({
            fieldLabel:this.isFixedAsset?WtfGlobal.getLocaleText("acc.rem.17"):WtfGlobal.getLocaleText("acc.rem.18")+'*',
            hiddenName:'groupid',
            name:'groupid',
            store:groupStore,
            valueField:'groupid',
            displayField:'groupname',
            typeAhead: true,
            forceSelection: true,
            width:150,
            allowBlank: false,
            mode: 'local',
            disableKeyFilter:true,
            hirarchical:true,
            triggerAction:'all'
//            addNewFn:this.showGroupWindow.createDelegate(this)
        });

        if(!WtfGlobal.EnableDisable(Wtf.UPerm.groups, Wtf.Perm.groups.create))
            this.accGroup.addNewFn= this.showGroupWindow.createDelegate(this)
        this.AccName.on('change',this.checkDuplicateName ,this);
        this.accGroup.on('select',function(c,rec){this.setOBType(rec.data["nature"])},this);
        this.accGroup.on('select',this.checkGroup,this);
        this.cmbParent.on('select',this.checkGroup,this);
        this.subAccount.on('collapse',this.enableDisableCombo.createDelegate(this,[false]),this);
        this.subAccount.on('expand',this.enableDisableCombo.createDelegate(this,[true]),this);


        this.balTypeStore = new Wtf.data.SimpleStore({
            fields: [{name:'typeid',type:'boolean'}, 'name'],
            data :[[true,'Debit'],[false,'Credit']]
        });
        this.balTypeEditor = new Wtf.form.ComboBox({
            store: this.balTypeStore,
            fieldLabel:this.isFixedAsset?WtfGlobal.getLocaleText("acc.fixedAssetList.grid.value"):WtfGlobal.getLocaleText("acc.coa.gridOpeningBalanceType"),
            name:'debitType',
            displayField:'name',
            forceSelection: true,
            hideLabel:this.isFixedAsset,
            hidden:this.isFixedAsset,
            allowBlank:false,
            width:150,
            valueField:'typeid',
            mode: 'local',
            value:true,
            triggerAction: 'all',
            typeAhead:true,
            selectOnFocus:true
        });

        this.openingBal= new Wtf.form.NumberField({
            fieldLabel:( this.isFixedAsset?WtfGlobal.getLocaleText("acc.fixedAssetList.grid.value")+'*':WtfGlobal.getLocaleText("acc.coa.gridOpeningBalance")),       // in "+WtfGlobal.getCurrencySymbolForForm()),
            name: 'openbalance',
            width:150,
            maxLength:15,
            decimalPrecision:2,
            disabled:(this.isEdit && this.isFixedAsset),
           // value:(this.isFixedAsset?"":0),
            allowBlank:false,
            allowNegative:false,
            xtype:'numberfield'
        });
        if(!this.isFixedAsset)
            this.openingBal.setValue(0);

        this.life= new Wtf.form.NumberField({
            fieldLabel:WtfGlobal.getLocaleText("acc.fixedAssetList.grid.assetLifeinYears")+"*",  // "Asset Life(in years)*",
            name: 'life',
            width:150,
            maxLength:15,
            allowNegative:false,
            decimalPrecision:2,
            //disabled:this.isFixedAsset&&this.isEdit,
            value:10,
            allowBlank:false
        });
        this.salvage= new Wtf.form.NumberField({
            fieldLabel:WtfGlobal.getLocaleText("acc.fixedAssetList.grid.salvageValue") + "*",  // "Salvage Value*",
            name: 'salvage',
            width:150,
            allowNegative:false,
            maxLength:15,
            //disabled:this.isEdit,
            decimalPrecision:2,
            value:0,
            allowBlank:false
        });
        
        this.Depreciation= new Wtf.form.FnComboBox({
            hirarchical:true,
            width:150,
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.assetDrepGoesTo"),  //'Asset Depreciation goes to',
            name:'depreciationaccount',
            store:this.depStore,//this.depStore,
            hiddenName:'depreciationaccount',
            displayField:'accountname',
            valueField:'accountid',            
            emptyText:'Select an Account...',
            mode: 'local',
            disableKeyFilter:true,
            triggerAction:'all',
            typeAhead: true,
            forceSelection:true//,
            // addNewFn:this.showGroupWindow.createDelegate(this)
        });

        if(this.isFixedAsset){
            chkFormCostCenterload();
        }
        this.CostCenter= new Wtf.form.FnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.common.costCenter"),  //"Cost Center",
            hiddenName:"costcenter",
            store: Wtf.FormCostCenterStore,
            hidden:!this.isFixedAsset,
            hideLabel:!this.isFixedAsset,
            itemCls : !this.isFixedAsset?"hidden-from-item":"",
            valueField:'id',
            displayField:'name',
            mode: 'local',
            typeAhead: true,
            forceSelection: true,
            selectOnFocus:true,
            width:150,
            triggerAction:'all',
            addNewFn:this.addCostCenter,
            scope:this,
            disabled:this.isEdit
        });
    },
    
    createBank:function(){
        this.accRec1=new Wtf.data.Record.create([
            {name: 'accountid',mapping:'accid'},
            {name: 'accountname',mapping:'accname'}
        ]); 
        
        this.accStore1=new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.accRec1),
            url : "ACCAccount/getAccountsForCombo.do",
            baseParams:{
                mode:2,
                group:[9,18],
                nondeleted:true
            }
        });
        this.accStore1.load();
        
        this.cmbPaymentAccount= new Wtf.form.FnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.mp.payAcc"),  //'Payment Account*',
            name:'Payaccountid',
            hiddenName:'Payaccountid',
            store:this.accStore1,
            hidden:!this.isFixedAsset,
            hideLabel: !this.isFixedAsset,
            width:150,
            disabled:this.isEdit,
            valueField:'accountid',
            displayField:'accountname',
            allowBlank:!this.isFixedAsset,
            triggerAction:'all',
            forceSelection:true,
            typeAhead: true,
            hirarchical:true
        });

        if(!WtfGlobal.EnableDisable(Wtf.UPerm.coa, Wtf.Perm.coa.create)){
            this.cmbPaymentAccount.addNewFn=function(){callCOAWindow(false, null, "coaWin",false,false,false,false,true);
                Wtf.getCmp("coaWin").on("update",function(){this.accStore1.reload();},this);};
        }
        
    },  
    
    getID:function(){
        if(this.isEdit && this.isFixedAsset){
    		Wtf.Ajax.requestEx({
    	          url:"ACCDepreciation/getPurchaseAccount.do",
    	          params: {fixedAssetID : this.record.data["accid"]}
    	      },this,this.genPurchaseSucess);
        }
    },
    genPurchaseSucess:function(response){
    	this.cmbPaymentAccount.setValue(response.purchaseAccName);
    },
    
    searchText:function(cAccTypeStore,pan){
        document.getElementById("accname").onkeyup=function(){
            this.text=Wtf.getCmp("accname").getValue();
            if(this.text=="" && this.tpl != undefined){
            	this.tpl.overwrite(pan.body,"");	// 20140 fixed
//                pan.hide();
            }
            else{
//                cAccTypeStore.filter("parentname",this.text);
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
    addMaster:function(id,store){
        addMasterItemWindow(id);
        Wtf.getCmp('masterconfiguration').on('update', function(){
            store.reload();
        }, this);
    },
    addCostCenter:function(){
        callCostCenter('addCostCenterWin');
    },
    setOBType:function(nature){
        this.balTypeEditor.setValue(nature==Wtf.account.nature.Asset||nature==Wtf.account.nature.Expences);
    },
    setCategory: function(){
        if(this.isEdit && Wtf.AssetCategoryStore.getCount()>0){
            this.Category.setValue(this.record.data.categoryid);
        }
        Wtf.AssetCategoryStore.un("load", this.setCategory, this);
    },
    checkGroup:function(){   
        var temp=this.coaForm.getForm().getValues();
        if(!temp.subaccount)return;
        var rec=this.cmbParent.store.find("parentid",this.cmbParent.getValue());
        if(rec==-1)return;
        rec=this.cmbParent.store.getAt(rec);
        if(rec.data['groupid']!=this.accGroup.getValue()){
            this.accGroup.setValue(rec.data['groupid']);            
        }
        this.setOBType(rec.data["nature"]);
    }, 
    
    enableDisableCombo:function(disabled){
        if(!this.isotherexpense && !this.isFixedAsset && !this.issales && !this.ispurchase)			// issue 20495 fixed
            this.accGroup.setDisabled(disabled);
        this.cmbParent.setValue("");
        this.checkGroup();
    },

    validateCard:function(){
        var flag=true;
        var val;
        var temp=this.coaForm.getForm().getValues();
        if(temp.subaccount){
            val=this.cmbParent.getValue();
            if(val.length<=0){
                this.cmbParent.markInvalid(WtfGlobal.getLocaleText("acc.product.msg1"));
                flag=false;
            }
        }
        return flag;
    },  
    createPanel:function(){
        this.pan=new Wtf.Panel({
            height:100,
            width:150,
            style:"padding:20px 13px 30px 0px;",
            autoScroll:true,
            title:(this.isFixedAsset?WtfGlobal.getLocaleText("acc.nee.11"):WtfGlobal.getLocaleText("acc.nee.10"))
        });
        this.pan.hide();
    },
    createForm:function(){
        this.coaForm=new Wtf.form.FormPanel({
            region:'center',
            width:430,
            height:700,
            labelWidth:130,
            border:false,
            bodyStyle: "background:transparent; padding: 20px 10px 0px 10px",
            style: "background: transparent;padding-left:15px;",
            defaultType: 'textfield',
            items:[{
                xtype:'hidden',
                name:'accid'
            },this.AccName,this.subAccount,this.accGroup,this.Category, this.openingBal, this.Currency,this.creationdate,this.cmbPaymentAccount,this.balTypeEditor,this.CostCenter,this.depreciation=new Wtf.form.FieldSet({
                title:WtfGlobal.getLocaleText("acc.fixedAssetList.depDet"),  //'Depreciation Details',
                autoHeight: true,
                width:325,  
                border:false, 
                style: 'margin-left:-10px',
                hidden:!this.isFixedAsset,
                layout:'form',
                items:[this.Depreciation,this.life,this.salvage]
            })]
        }); 
    }, 
    closeForm:function(){ 
        this.fireEvent('cancel',this);
         this.close();         
    },

    checkDuplicateName:function(o,newval,oldval){
        this.AccName.setValue(this.AccName.upper(this.AccName.getValue()));
        
        this.parentStore.clearFilter(true);
        var FIND = this.AccName.getValue().trim();
        FIND =FIND.replace(/\s+/g, '');
         var index=this.parentStore.findBy( function(rec){
             var parentname=rec.data['parentname'].trim();
             parentname=parentname.replace(/\s+/g, '');
             if(parentname==FIND && !rec.data.deleted) // Add non-deleted record check
                return true;
             else
                return false
        })
        if(index>=0){
            if(this.isFixedAsset)
                WtfComMsgBox(45,2);
            else
                WtfComMsgBox(35,2);

            this.AccName.setValue(oldval)
             return;
        }
    },

    saveForm:function(){
        
        this.AccName.setValue(this.AccName.getValue().trim());
        var flag1=this.coaForm.getForm().isValid();
        var flag2=this.validateCard();
        if(this.accGroup.getValue()=='12' && this.salvage.getValue()>this.openingBal.getValue()){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.rem.15")], 2);
            return;
        }
        if(this.openingBal.getValue()==0&&this.isFixedAsset){ 
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.rem.16")], 2);
            return;
        }
         if(this.life.getValue()==0&&this.isFixedAsset){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.rem.17")], 2);
            return;
        }
        if(!flag1||!flag2){
            WtfComMsgBox(2,2);
        }else{
        	
            this.loadMask1 = new Wtf.LoadMask(this.id, {msg: WtfGlobal.getLocaleText("acc.msgbox.49"), msgCls: "x-mask-loading acc-customer-form-mask"});
            this.loadMask1.show();
            Wtf.getCmp(this.id).on("loadingcomplete",function(){this.loadMask1.msg = "Saved..." ;this.loadMask1.hide()},this);
            
            
            var rec=this.coaForm.getForm().getValues();
            rec.subaccount=(rec.subaccount=="on"?true:false);
            rec.groupid=this.accGroup.getValue();
            rec.openbalance=this.openingBal.getValue();
            rec.currencyid=this.Currency.getValue();//WtfGlobal.getCurrencyID(),
            rec.creationDate=WtfGlobal.convertToGenericDate(this.creationdate.getValue());
            rec.mode=3;
            rec.parentid=this.cmbParent.getValue();
            rec.parentname=this.cmbParent.getRawValue();
            rec.category= this.Category.getValue();
            rec.debitType=this.isFixedAsset?true:this.balTypeEditor.getValue();
            rec.isFixedAsset =this.isFixedAsset;
            Wtf.Ajax.requestEx({
                url:"ACCAccount/saveAccount.do",
//                url: Wtf.req.account+'CompanyManager.jsp',
                params: rec
            },this,this.genSuccessResponse,this.genFailureResponse);
        }
    },

    createFixedAssetJE:function(){
    	var asset = '{debit:"'+true+'",accountid:"'+this.fixedAssetID+'",description:"'+"This is a Fixed Asset"+'",amount:'+this.openingBal.getValue()+"}";
    	var cashinHand = '{debit:"'+false+'",accountid:"'+this.cmbPaymentAccount.getValue()+'",description:"'+"This is a Fixed Asset Value deducted"+'",amount:'+this.openingBal.getValue()+"}";
    	this.JERecord={costcenter:this.CostCenter.getValue(),
    					currencyid: this.Currency.getValue(),
    					entrydate: WtfGlobal.convertToGenericDate(this.creationdate.getValue()),
    					entryno: this.autoGenJEno,
    					memo: "Fixed Asset Entry",
    					detail: "["+asset+","+cashinHand+"]"
    				  };
    },
    
    createJE:function(){
			this.createFixedAssetJE();
			Wtf.Ajax.requestEx({
              url:"ACCJournal/saveJournalEntry.do",
              params: this.JERecord
          },this,this.genSucess);
	},
	
	createAssetEntry:function(){
		Wtf.Ajax.requestEx({
          url:"ACCDepreciation/saveAssetDetail.do",
          params: {purchaseId : this.purchaseId , fixedAssetID : (this.fixedAssetID==null||this.fixedAssetID==undefined)?"000001":this.fixedAssetID}
      },this);
	},
	    
	genSucess:function(response){
		this.purchaseId = response.id;
		this.createAssetEntry();
	},
	
    genSuccessResponse:function(response){
    	if(this.isFixedAsset && !this.isEdit){
    		this.fixedAssetID = response.accID;
    		WtfGlobal.fetchAutoNumber(0, function(resp){this.autoGenJEno = resp.data;this.createJE();}, this);
    	}
    	
        // Msgbox for success of adding of account moved to last line of this method from here          Neeraj
        if(response.success){
            Wtf.salesAccStore.reload();
            this.fireEvent('update',this,response.accID);
            
          //Neeraj
            var tabid = 'ledger';
            if(Wtf.getCmp(tabid)!=undefined) {
                Wtf.getCmp(tabid).accStore.reload();
            }
            
            //On updation of category reload 'by category report grid'
            if(!this.isEdit || (this.isEdit && this.record.data.categoryid!=this.Category.getValue())) {
                var panel= Wtf.getCmp("fixedAssetByCategoryReport");
                if(panel!=null || panel!=undefined){
                    panel.isCategoryUpdated=true;
                }
            }
            this.close();
        } else {
            if(response.msg) {
                var msgArr = [];
                msgArr[0] = "Error";
                msgArr[1] = response.msg;
                WtfComMsgBox(msgArr, 1);
            }
        }
        this.fireEvent("loadingcomplete",this);
//        WtfComMsgBox([(this.isFixedAsset?WtfGlobal.getLocaleText("acc.fixedAssetList.tabTitle"):WtfGlobal.getLocaleText("acc.coa.tabTitle")),(this.isFixedAsset?WtfGlobal.getLocaleText("acc.acc.saveFA"):WtfGlobal.getLocaleText("acc.acc.save"))],response.success*2+1);
    },

    genFailureResponse:function(response){
    	this.fireEvent("loadingcomplete",this);
        var msg="Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
        this.close();
    },

    showGroupWindow:function(){
        callGroupWindow(false, null, "groupWin");
        Wtf.getCmp("groupWin").alignTo(this.getEl(),"other-tl",[50,50]);
        Wtf.getCmp("groupWin").on("update",function(){this.accGroup.store.reload();},this);
    }
 
    

    
});
