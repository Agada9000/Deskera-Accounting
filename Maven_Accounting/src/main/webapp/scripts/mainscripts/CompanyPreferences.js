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
Wtf.account.CompanyAccountPreferences=function(config){
    Wtf.account.CompanyAccountPreferences.superclass.constructor.call(this, config);
};

Wtf.extend(Wtf.account.CompanyAccountPreferences, Wtf.account.ClosablePanel,{
    val:false,
    currentyear:-1,
    ffyear:null,
    onRender:function(config){
        WtfComMsgBox(29,4,true);
        this.setCurrentYear();
        this.val=Wtf.account.companyAccountPref.withoutinventory; 
        this.accRec = Wtf.data.Record.create ([
            {name:'accountname',mapping:'accname'},
            {name:'accountid',mapping:'accid'}
//            {name:'level', type:'int'}
        ]);

        this.dgStore = new Wtf.data.Store({
//            url: Wtf.req.account+'CompanyManager.jsp',
            url : "ACCAccount/getAccountsForCombo.do",
            baseParams:{
                mode:2,
                group:[16,17],
                ignore:true,
                nondeleted:true
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.accRec)
        });
        
               
         this.exStore = new Wtf.data.Store({
//            url: Wtf.req.account+'CompanyManager.jsp',
            url : "ACCAccount/getAccountsForCombo.do",
            baseParams:{
                mode:2,
                group:[8],
                nondeleted:true
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.accRec)
        });
        this.createForm();
        this.exStore.on('load',function(){ Wtf.MessageBox.hide();this.setAccounts();},this);
        this.dgStore.on('load',function(){this.exStore.load();},this);
        this.dgStore.load();
        
        Wtf.account.CompanyAccountPreferences.superclass.onRender.call(this, config);
    },

    createForm:function(){
        this.autojournalentry= new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.autoJE"),  //'Journal Entry*',
            name:'autojournalentry',
            allowBlank:false,
            value:Wtf.account.companyAccountPref.autojournalentry
        });
        this.autoinvoice= new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.autoInvoice"),  //'Invoice',
            name:'autoinvoice',
            value:Wtf.account.companyAccountPref.autoinvoice
        });
        this.autocreditmemo= new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.autoCN"),  //'Credit Note',
            name:'autocreditmemo',
            value:Wtf.account.companyAccountPref.autocreditmemo
        });
        this.autoreceipt= new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.autoRP"),  //'Receive Payment',
            name:'autoreceipt',
            value:Wtf.account.companyAccountPref.autoreceipt
        });
        this.autogoodsreceipt= new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.autoVI"),  //'Vendor Invoice',
            name:'autogoodsreceipt',
            value:Wtf.account.companyAccountPref.autogoodsreceipt
        });
        this.autodebitnote= new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.autoDN"),  //'Debit Note',
            name:'autodebitnote',
            value:Wtf.account.companyAccountPref.autodebitnote
        });
        this.autopayment= new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.autoMP"),  //'Make Payment',
            name:'autopayment',
            value:Wtf.account.companyAccountPref.autopayment
        });
        this.autoso= new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.autoSO"),  //'Sales Order',
            name:'autoso',
            value:Wtf.account.companyAccountPref.autoso
        });
        this.autopo= new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.autoPO"),  //'Purchase Order',
            name:'autopo',
            value:Wtf.account.companyAccountPref.autopo
        });
        this.autocashsales= new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.autoCS"),  //'Cash Sales',
            name:'autocashsales',
            value:Wtf.account.companyAccountPref.autocashsales
        });
        this.autocashpurchase= new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.autoCP"),  //'Cash Purchase',
            name:'autocashpurchase',
            value:Wtf.account.companyAccountPref.autocashpurchase
        });
         this.autobillinginvoice= new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.autoInvoice"),  //'Invoice',
            name:'autobillinginvoice',
            value:Wtf.account.companyAccountPref.autobillinginvoice
        });
         this.autobillingreceipt= new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.autoRP"),  //'Receive Payment',
            name:'autobillingreceipt',
            value:Wtf.account.companyAccountPref.autobillingreceipt
        });
        this.autobillingcashsales= new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.autoCS"),  //'Cash Sales',
            name:'autobillingcashsales',
            value:Wtf.account.companyAccountPref.autobillingcashsales
        });
        this.autobillinggoodsreceipt= new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.autoVI"),  //'Vendor Invoice',
            name:'autobillinggoodsreceipt',
            value:Wtf.account.companyAccountPref.autobillinggoodsreceipt
        });
        this.autobillingdebitnote= new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.autoDN"),  //'Debit Note',
            name:'autobillingdebitnote',
            value:Wtf.account.companyAccountPref.autobillingdebitnote
        });
        this.autobillingcreditmemo= new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.autoCN"),  //'Credit Note',
            name:'autobillingcreditmemo',
            value:Wtf.account.companyAccountPref.autobillingcreditmemo
        });
        this.autobillingpayment= new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.autoMP"),  //'Make Payment',
            name:'autobillingpayment',
            value:Wtf.account.companyAccountPref.autobillingpayment
        });
        this.autobillingso= new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.autoSO"),  //'Sales Order',
            name:'autobillingso',
            value:Wtf.account.companyAccountPref.autobillingso
        });
        this.autobillingpo= new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.autoPO"),  //'Purchase Order',
            name:'autobillingpo',
            value:Wtf.account.companyAccountPref.autobillingpo
        });
        this.autobillingcashpurchase= new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.autoCP"),  //'Cash Purchase',
            name:'autobillingcashpurchase',
            value:Wtf.account.companyAccountPref.autobillingcashpurchase
        });
        this.autoquotation= new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.autoQN"),  //'Quotation',
            name:'autoquotation',
            value:Wtf.account.companyAccountPref.autoquotation
        });
        this.daysStore = new Wtf.data.SimpleStore({
            fields: [{name:'daysid',type:'int'}, 'name'],
            data :[[1,'1'],[2,'2'],[3,'3'],[4,'4'],[5,'5'],[6,'6'],[7,'7'],[8,'8'],[9,'9'],[10,'10'],
                [11,'11'],[12,'12'],[13,'13'],[14,'14'],[15,'15'],[16,'16'],[17,'17'],[18,'18'],[19,'19'],[20,'20'],
                [21,'21'],[22,'22'],[23,'23'],[24,'24'],[25,'25'],[26,'26'],[27,'27'],[28,'28'],[29,'29'],[30,'30'],[31,'31']]
        });
        this.monthStore = new Wtf.data.SimpleStore({
            fields: [{name:'monthid',type:'int'}, 'name'],
            data :[[0,'January'],[1,'February'],[2,'March'],[3,'April'],[4,'May'],[5,'June'],[6,'July'],[7,'August'],[8,'September'],[9,'October'],
                [10,'November'],[11,'December']]
        });
      
        this.lockEditor= new Wtf.form.Checkbox({
            boxLabel:" ",
            name:'rectype',
            inputValue:'2'
        });
        this.fdays = new Wtf.form.ComboBox({
            store:  this.daysStore,
            name:'daysid',
            displayField:'name',
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.financialYearDate"),  //'Financial Year Date',
            forceSelection: true,
            valueField:'daysid',
            mode: 'local',
            anchor:'95%',
            triggerAction: 'all',
            selectOnFocus:true
        });
        this.fmonth = new Wtf.form.ComboBox({
            store: this.monthStore,
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.month"),  //'Month',
            name:'monthid',
            displayField:'name',
            forceSelection: true,
            anchor:'95%',
            valueField:'monthid',
            mode: 'local',
            triggerAction: 'all',
            selectOnFocus:true
        });
        this.bdays = new Wtf.form.ComboBox({
            store:  this.daysStore,
            name:'daysid',
            displayField:'name',
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.bookBeginingDate"),  //'Book Beginning Date',
            valueField:'daysid',
            mode: 'local',
            forceSelection: true,
            anchor:'95%',
            triggerAction: 'all',
            selectOnFocus:true
        });
        this.bmonth = new Wtf.form.ComboBox({
            store: this.monthStore,
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.month"),  //'Month',
            name:'monthid',
            displayField:'name',
            anchor:'95%',
            forceSelection: true,
            valueField:'monthid',
            mode: 'local',
            triggerAction: 'all',
            selectOnFocus:true
        });
          var data=this.getBookBeginningYear(true);
            this.yearStore= new Wtf.data.SimpleStore({
            fields: [{name:'id',type:'int'}, 'yearid'],
            data :data
        });
        this.byear = new Wtf.form.ComboBox({
            store: this.yearStore,
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.year"),  //'Year',
            name:'yearid',
            displayField:'yearid',
            anchor:'95%',
            valueField:'yearid',
            forceSelection: true,
            mode: 'local',
            triggerAction: 'all',
            selectOnFocus:true
        });
        this.fmonth.on('select',this.getBeginningYear,this);
        this.fdays.on('select',this.getBeginningYear,this);
        this.fmonth.on('select',this.getBookBeginningYear.createDelegate(this,[false]),this);
        this.fdays.on('select',this.getBookBeginningYear.createDelegate(this,[false]),this);
        var btnArr=[];
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.accpref, Wtf.Perm.accpref.edit))
        btnArr.push(new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.common.saveBtn"),  //'Save',
            handler:this.savePreferences,
            iconCls :getButtonIconCls(Wtf.etype.save),
            scope:this
        }));
        btnArr.push("->");
        btnArr.push(getHelpButton(this,this.helpmodeid));

        this.addGrid();
        this.form=new Wtf.form.FormPanel({
            border:false,
            buttonAlign:'left',
            autoScroll:true,
            defaults:{labelWidth:200,border:false},
            items:[{
                layout:'column',
                defaults:{border:false,bodyStyle:'padding:10px'},
                items:[{
                    columnWidth:.49,
                    layout:'form',
                    items:[{
                        xtype:'fieldset',
                        autoHeight:true,
                        id:"financialYearSettings"+this.helpmodeid,
                        title:WtfGlobal.getLocaleText("acc.accPref.FYsettings"),  //'Financial Year Settings',
                        title:"<span wtf:qtip= '"+WtfGlobal.getLocaleText("acc.accPref.FYsettingsTip")+"'>"+WtfGlobal.getLocaleText("acc.accPref.FYsettings")+"</span>",
                        defaults:{format:WtfGlobal.getOnlyDateFormat(),allowBlank:false},
                        items:[{
                            layout:'column',
                            border:false,
                            labelWidth:130,
                            defaults:{border:false}, 
                            items:[{
                                layout:'form',
                                columnWidth:0.42,
                                items:this.fdays
                            },{
                                columnWidth:0.30,
                                labelWidth:40,
                                layout:'form',
                                items:this.fmonth
                            },{ //Show Financial Start YEAR as read only[SK]
                                columnWidth:0.27,
                                labelWidth:35,
                                layout:'form',
                                items:new Wtf.form.Field({
                                            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.year"),  //'Year',
                                            cls:"clearStyle",
                                            anchor:'95%',
                                            readOnly:true,
                                            value:this.currentyear
                                        })
                            }]
                        },{
                            layout:'column',
                            border:false,
                            labelWidth:130,
                            defaults:{border:false},
                            items:[{
                                layout:'form',
                                columnWidth:0.42,
                                items:this.bdays
                            },{
                                columnWidth:0.30,
                                labelWidth:40,
                                layout:'form',
                                items:this.bmonth
                            },{
                                columnWidth:0.27,
                                labelWidth:35,
                                layout:'form',
                                items:this.byear
                            }]
                        }]
                    },{
                        columnWidth:.49,
                        layout:'fit',
                        items:this.grid

                    },{
                        xtype:'fieldset',
                        autoHeight:true,
                        title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.accPref.accSettingsTip")+"'>"+WtfGlobal.getLocaleText("acc.accPref.accSettings")+"</span>",
                        id:'accountsettings'+this.helpmodeid,
                        defaults:{forceSelection: true,allowBlank:false,anchor:'80%'},
                        items:[
                        /*this.Cash= new Wtf.form.FnComboBox({
                            addNewFn:this.addNewAccount.createDelegate(this,[false,null,"coaWin",false],true),
                            fieldLabel:'Cash goes to',
                            name:'cashaccount',
                            store:this.dgStore,
                            hiddenName:'cashaccount',
                            displayField:'accountname',
                            disabled:true,
                            valueField:'accountid',
                            value:Wtf.account.companyAccountPref.cashaccount,
                            emptyText:WtfGlobal.getLocaleText("acc.accPref.emptyText")
                            
                        }),*/
						this.Cash= new Wtf.form.Field({
                            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.cashGoesTo"),  //'Cash goes to',
                            name:'cashaccount',
                            hiddenName:'cashaccount',
                            cls:"clearStyle",
                            readOnly:true
                        }),this.DiscountGiven= new Wtf.form.FnComboBox({
//                            addNewFn:this.addNewAccount.createDelegate(this,[false,null,"coaWin",false],true),
                            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.disGivenGoesTo"),  //'Discount Given goes to',
                            name:'discountgiven',
                            store:this.dgStore,
                            hiddenName:'discountgiven',
                            displayField:'accountname',
                            valueField:'accountid',
                     //       value:Wtf.account.companyAccountPref.discountgiven,
                            emptyText:WtfGlobal.getLocaleText("acc.accPref.emptyText")
                        }),this.DiscountReceived= new Wtf.form.FnComboBox({
//                            addNewFn:this.addNewAccount.createDelegate(this,[false,null,"coaWin",false],true),
                            hirarchical:true,
                            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.disReceivedGoesTo"),  //'Discount Received goes to',
                            name:'discountreceived',
                            store:this.dgStore,
                            hiddenName:'discountreceived',
                            displayField:'accountname',
                            valueField:'accountid',
                      //      value:Wtf.account.companyAccountPref.discountreceived,
                            emptyText:WtfGlobal.getLocaleText("acc.accPref.emptyText")
                        }),this.Depreciation= new Wtf.form.FnComboBox({
                      //      addNewFn:this.addNewAccount.createDelegate(this,[false,null,"depcoaWin",true],true),
                            hirarchical:true,
                            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.assetDrepGoesTo"),  //'Asset Depreciation goes to',
                            name:'depreciationaccount',
                            store:this.exStore,//this.depStore,
                            hiddenName:'depreciationaccount',
                            displayField:'accountname',
                            valueField:'accountid',
                      //      value:Wtf.account.companyAccountPref.depreciationaccount,
                            emptyText:WtfGlobal.getLocaleText("acc.accPref.emptyText")
                         }),this.ForeignExchange= new Wtf.form.FnComboBox({
                      //      addNewFn:this.addNewAccount.createDelegate(this,[false,null,"fxcoaWin",true],true),
                            hirarchical:true,
                            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.foreignExchangeGoesTo"),  //'Foreign Exchange goes to',
                            name:'foreginexchange',
                            store:this.exStore,
                            hiddenName:'foreignexchange',
                            displayField:'accountname',
                            valueField:'accountid',
                        //    value:Wtf.account.companyAccountPref.foreignexchange,
                            emptyText:WtfGlobal.getLocaleText("acc.accPref.emptyText")
//                        }),
//                        this.ShippingCharges= new Wtf.form.FnComboBox({
//                            addNewFn:this.addNewAccount.createDelegate(this,[false,null,"coaWin",false],true),
//                            hirarchical:true,
//                            fieldLabel:'Shipping Charges go to',
//                            name:'shippingcharges',
//                            store:this.dgStore,
//                            hiddenName:'shippingcharges',
//                            displayField:'accountname',
//                            valueField:'accountid',
//                            value:Wtf.account.companyAccountPref.shippingcharges,
//                            emptyText:WtfGlobal.getLocaleText("acc.accPref.emptyText"),
//                            hidden:true,
//                            hideLabel:true
                        }),this.OtherCharges= new Wtf.form.FnComboBox({
                       //     addNewFn:this.addNewAccount.createDelegate(this,[false,null,"coaWin",false],true),
                            hirarchical:true,
                            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.otherCharges"),
                            name:'othercharges',
                            store:this.dgStore,
                            hiddenName:'othercharges',
                            displayField:'accountname',
                            valueField:'accountid',
                      //      value:Wtf.account.companyAccountPref.othercharges,
                            emptyText:WtfGlobal.getLocaleText("acc.accPref.emptyText"),
                            hidden:true,
                            hideLabel:true
                       
                        })]
                     }] 
                },{
                    columnWidth:.49,
                    layout:'form',
                    items:[{
//                        xtype:'fieldset',
//                        autoHeight:true,
//                        title:'Email Settings',
//                        id:'emailSettings'+this.helpmodeid,
//                        defaults:{xtype:'checkbox',anchor:'80%',maxLength:50,validator:this.validateFormat},
//                        items:[this.sendInvMail=new Wtf.form.Checkbox({
//                            fieldLabel:'Send email notification on invoice creation',
//                            name:'emailinvoice',
//                            checked:Wtf.account.companyAccountPref.emailinvoice
//                        })]
//                    },{
                        xtype:'fieldset',
                        autoHeight:true,
                        title:WtfGlobal.getLocaleText("acc.accPref.invSettings"),  //'Inventory Settings',
                        title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.accPref.invSettingsTip")+"'>"+WtfGlobal.getLocaleText("acc.accPref.invSettings")+"</span>",
                        defaults:{anchor:'80%',maxLength:50,validator:this.validateFormat},
                        items:[this.withInv=new Wtf.form.Checkbox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.withoutInv"),  //'Transactions without inventory',
                            name:'withoutinventory',
                            checked:this.val
                        })]
                    },{
                        xtype:'fieldset',
                        autoHeight:true,
                        title:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.accPref.autoNoDescription") +"'>"+WtfGlobal.getLocaleText("acc.accPref.autoNoGeneration")+" </span>",
                        id:"automaticNumberGeneration"+this.helpmodeid,
                        defaults:{xtype:'textfield',anchor:'80%',maxLength:50,validator:this.validateFormat},
                        items:[{
                            border:false,
                            xtype:'panel',
                            width:350,
                            bodyStyle:'padding:0px 0px 20px 40px;',
                            html:'<font color="#555555"><ul><li> '+ WtfGlobal.getLocaleText("acc.accPref.msg1")+
                                '<li>'+ WtfGlobal.getLocaleText("acc.accPref.msg2") +'</ul></font>'
                        },this.autojournalentry,this.autoso,this.autoinvoice,this.autocreditmemo,this.autoreceipt,this.autogoodsreceipt,this.autodebitnote,this.autopayment,this.autopo,this.autocashsales,this.autocashpurchase,this.autobillingso,this.autobillinginvoice,this.autobillingcreditmemo,this.autobillingreceipt,this.autobillinggoodsreceipt,this.autobillingdebitnote,this.autobillingpayment,this.autobillingpo,this.autobillingcashsales,this.autobillingcashpurchase,this.autoquotation]
                    }]
                }]
            }],
            bbar:btnArr
        });
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.coa, Wtf.Perm.coa.create)){
            this.DiscountGiven.addNewFn=this.addNewAccount.createDelegate(this,[false,null,"coaWin",false],true);
            this.DiscountReceived.addNewFn=this.addNewAccount.createDelegate(this,[false,null,"coaWin",false],true);
            this.Depreciation.addNewFn=this.addNewAccount.createDelegate(this,[false,null,"depcoaWin",true],true);
            this.ForeignExchange.addNewFn=this.addNewAccount.createDelegate(this,[false,null,"fxcoaWin",true],true);
            this.OtherCharges.addNewFn=this.addNewAccount.createDelegate(this,[false,null,"coaWin",false],true);
        }
        this.add(this.form);
//        this.ownerCt.doLayout();
       this.setData();
        this.autoquotation.on("render", function(){//On rendering Last Auto_No_Field reArranged AutoNo fields[SK]
            this.reArrangeAutoNoFields(this.val);
            this.withInv.on("check", function(chk, checked){
                this.reArrangeAutoNoFields(checked);
            },this);
        },this);
    },
    reArrangeAutoNoFields: function(checked){
        if(checked){
            this.hideFormElement(this.autoso);
            this.hideFormElement(this.autoinvoice);
            this.hideFormElement(this.autocreditmemo);
            this.hideFormElement(this.autoreceipt);
            this.hideFormElement(this.autogoodsreceipt);
            this.hideFormElement(this.autodebitnote);
            this.hideFormElement(this.autopayment);
            this.hideFormElement(this.autopo);
            this.hideFormElement(this.autocashsales);
            this.hideFormElement(this.autocashpurchase);
            this.hideFormElement(this.autoquotation);

            this.showFormElement(this.autobillingso);
            this.showFormElement(this.autobillinginvoice);
            this.showFormElement(this.autobillingcreditmemo);
            this.showFormElement(this.autobillingreceipt);
            this.showFormElement(this.autobillingcashsales);
            this.showFormElement(this.autobillinggoodsreceipt);
            this.showFormElement(this.autobillingdebitnote);
            this.showFormElement(this.autobillingpayment);
            this.showFormElement(this.autobillingpo);
            this.showFormElement(this.autobillingcashpurchase);
        } else {
            this.showFormElement(this.autoso);
            this.showFormElement(this.autoinvoice);
            this.showFormElement(this.autocreditmemo);
            this.showFormElement(this.autoreceipt);
            this.showFormElement(this.autogoodsreceipt);
            this.showFormElement(this.autodebitnote);
            this.showFormElement(this.autopayment);
            this.showFormElement(this.autopo);
            this.showFormElement(this.autocashsales);
            this.showFormElement(this.autocashpurchase);
            this.showFormElement(this.autoquotation);

            this.hideFormElement(this.autobillingso);
            this.hideFormElement(this.autobillinginvoice);
            this.hideFormElement(this.autobillingcreditmemo);
            this.hideFormElement(this.autobillingreceipt);
            this.hideFormElement(this.autobillingcashsales);
            this.hideFormElement(this.autobillinggoodsreceipt);
            this.hideFormElement(this.autobillingdebitnote);
            this.hideFormElement(this.autobillingpayment);
            this.hideFormElement(this.autobillingpo);
            this.hideFormElement(this.autobillingcashpurchase);
        }
    },

    showFormElement:function(obj){
        var cntDiv = obj.container.up('div.x-form-item');
        cntDiv.dom.style.display='block';
        cntDiv.dom.className = WtfGlobal.replaceAll(cntDiv.dom.className, "hidden-from-item", "");
    },
    hideFormElement:function(obj){
        var cntDiv = obj.container.up('div.x-form-item');
        cntDiv.dom.style.display='none';
        cntDiv.dom.className += ' hidden-from-item';
    },
    
    getBookBeginningYear:function(isfirst){ 
        var ffyear;
        if(isfirst){
            var cfYear=new Date(Wtf.account.companyAccountPref.fyfrom)
            ffyear=new Date(Wtf.account.companyAccountPref.firstfyfrom)            
            ffyear=new Date( ffyear.getFullYear(),cfYear.getMonth(),cfYear.getDate()).clearTime()
        }
        else{
            var fyear=new Date(Wtf.account.companyAccountPref.firstfyfrom).getFullYear()
            ffyear=new Date( fyear,this.fmonth.getValue(),this.fdays.getValue()).clearTime()
        }

        var data=[];
        var newrec;
        if(ffyear==null||ffyear=="NaN"){ ffyear=new Date(Wtf.account.companyAccountPref.fyfrom)}
        var year=ffyear.getFullYear();
        data.push([0,year]) 
        if(!(ffyear.getMonth()==0&&ffyear.getDate()==1)){
            data.push([1,year+1]);
            newrec = new Wtf.data.Record({id:1,yearid:year+1})
        }
       if(!isfirst&&this.yearStore.getCount()<2){
            this.yearStore.insert(1,newrec)
         }
        return data;
    },
    addGrid:function(){ 
        this.lockRec = new Wtf.data.Record.create([
            {name: 'id'},
            {name: 'name'},
            {name: 'islock'},
        ]);
        this.lockds = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:"count"
            },this.lockRec),
//            url:Wtf.req.account+'CompanyManager.jsp',
            url : "ACCCompanyPref/getYearLock.do",
            baseParams:{
                mode:94,
                CurrentFinancialYear:(Wtf.account.companyAccountPref.fyfrom).getFullYear()
            }
        });
        this.lockds.on('load',this.getBeginningYear,this)
        this.lockds.load();        
        this.gridcm= new Wtf.grid.ColumnModel([{
            header:WtfGlobal.getLocaleText("acc.accPref.gridFinancialYear"),  //"Financial Year",
            dataIndex:'name',
            align:'center',
            autoWidth : true
        },{
            header:WtfGlobal.getLocaleText("acc.accPref.gridYearBeginDate"),  //"Year Beginning Date",
            dataIndex:'sdate',
            align:'center',
            autoWidth : true
        },{
            header:WtfGlobal.getLocaleText("acc.accPref.gridYearEndDate"),  //"Year Ending Date",
            align:'center',
            dataIndex:'edate',
            autoWidth : true
        },this.checkColumn = new Wtf.grid.CheckColumn({
            header: WtfGlobal.getLocaleText("acc.accPref.gridCloseBook"),  //"Close Book",
            align:'center',
            dataIndex: 'islock',
            width: 40
        })
        ]);
        this.grid = new Wtf.grid.EditorGridPanel({
            cls:'vline-on',
            layout:'fit',
            id:'closebooks'+this.helpmodeid,
            autoScroll:true,
            height:270,
            width:580,//TODO : IE7 display problem for yearlock grid
            plugins:[this.checkColumn],
            title:WtfGlobal.getLocaleText("acc.rem.48"),  //"Close Book(s)",
            store: this.lockds,
            cm: this.gridcm,
            border : false,
            loadMask : true,
            viewConfig: {
                forceFit:true,
                emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
            }
        });

    },
    getBeginningYear:function(){
        this.lockds.each(function(rec){
            var year=rec.data.name;
            var date=new Date(year,this.fmonth.getValue(),this.fdays.getValue());
            date=date.dateFormat(WtfGlobal.getOnlyDateFormat(date));
            rec.set('sdate',date);
            date=new Date(++year,this.fmonth.getValue(),this.fdays.getValue());
            date=date.add(Date.DAY, -1);
            date=date.dateFormat(WtfGlobal.getOnlyDateFormat(date));
            rec.set('edate',date);
          },this)
    },

    getEndingYear:function(a,m,rec) {
        var year=rec.data.name;
        var date=new Date(++year,this.fmonth.getValue(),this.fdays.getValue());
        date=date.add(Date.DAY, -1);
        date=date.dateFormat(WtfGlobal.getOnlyDateFormat(date));
        return date;
    },
    setCurrentYear:function(){
    	this.currentyear = new Date(Wtf.account.companyAccountPref.fyfrom).getFullYear();
//      this.currentyear = Wtf.serverDate?Wtf.serverDate.getFullYear():new Date().getFullYear();
    },
   
    setData:function(){
        if(this.autojournalentry.getValue()=="")
            this.autojournalentry.setValue("000000");
        this.bdays.setValue(new Date(Wtf.account.companyAccountPref.bbfrom).getDate());
        this.bmonth.setValue(new Date(Wtf.account.companyAccountPref.bbfrom).getMonth());
        this.byear.setValue(new Date(Wtf.account.companyAccountPref.bbfrom).getFullYear());
        this.fdays.setValue(new Date(Wtf.account.companyAccountPref.fyfrom).getDate()); 
        this.fmonth.setValue(new Date(Wtf.account.companyAccountPref.fyfrom).getMonth());
        this.initForClose();
    }, 
    setAccounts:function(){
        this.OtherCharges.setValue(Wtf.account.companyAccountPref.othercharges)
        this.ForeignExchange.setValue(Wtf.account.companyAccountPref.foreignexchange)
        this.Depreciation.setValue(Wtf.account.companyAccountPref.depreciationaccount)
        this.DiscountReceived.setValue(Wtf.account.companyAccountPref.discountreceived)
        this.DiscountGiven.setValue(Wtf.account.companyAccountPref.discountgiven)
        var idx = this.dgStore.find("accountid", Wtf.account.companyAccountPref.cashaccount);
        if(idx != -1){
            this.Cash.setValue(this.dgStore.getAt(idx).get("accountname"));
        }
    },
    calFinancialYear:function(){
        this.fyear=this.currentyear
        return true;
    },
    validateFinancialYear:function(){
         var isvaliddate= this.calFinancialYear();
         if(!isvaliddate)return false;
         isvaliddate= this.checkdate(this.fdays.getValue(),this.fmonth.getValue(),this.fyear); 
         if(!isvaliddate){ WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.accPref.msg3") ],2);return false;}
         this.fdate=new Date(this.fyear,this.fmonth.getValue(),this.fdays.getValue());

         isvaliddate= this.checkdate(this.bdays.getValue(),this.bmonth.getValue(),this.byear.getValue());
         if(!isvaliddate){ WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.accPref.msg4") ],2);return false;}
         this.bdate=new Date(this.byear.getValue(),this.bmonth.getValue(),this.bdays.getValue())//.format(WtfGlobal.getOnlyDateFormat());
         var fyear=new Date(Wtf.account.companyAccountPref.firstfyfrom).getFullYear()
          var firstfyear=new Date( fyear,this.fmonth.getValue(),this.fdays.getValue()).clearTime()//.format(WtfGlobal.getOnlyDateFormat());
         var nxtfyear=new Date(firstfyear).add(Date.YEAR,1).clearTime();
         var bdate
         bdate=this.bdate.clearTime()
             if(new Date(bdate).between(firstfyear,nxtfyear)&&bdate.getTime()!=nxtfyear.getTime()){
                return true;
         }
            else{
                 WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.accPref.msg5") ],2);
                 return false;
            }
    }, 

    checkdate:function(d,m,y){
        var yl=1900; // least year to consider 
        var ym=2100; // most year to consider
        if (m<0 || m>11) return(false);
        if (d<1 || d>31) return(false);
        if (y<yl || y>ym) return(false);
        if (m==3 || m==5 || m==8 || m==10)
        if (d==31) return(false);
        if (m==1)
        {
        var b=parseInt(y/4);
        if (isNaN(b)) return(false);
        if (d>29) return(false);
        if (d==29 && ((y/4)!=parseInt(y/4))) return(false);
        }
        return(true);
    },
  
    getUpdatedLockDetails:function(){
        var arr=[];
         for(var i=0;i<this.lockds.getCount()-1;i++){
            var rec=this.lockds.getAt(i)
            if(rec.dirty)
                arr.push(i);
            }
        return WtfGlobal.getJSONArray(this.grid,true);
    },
  
    savePreferences:function(){         
        if(this.form.getForm().isValid()===false){WtfComMsgBox(2, 2);return};
        var isSingle=this.validateFinancialYear();
        if(!isSingle)return;
        Wtf.MessageBox.show({
            title: WtfGlobal.getLocaleText("acc.common.confirm"), //'Confirm',
            width: 600,
            msg: "<div style='padding-left:50px;'>"+WtfGlobal.getLocaleText("acc.accPref.msg6")+"</div>",
            buttons: {yes:WtfGlobal.getLocaleText("acc.accPref.msg7"),no:WtfGlobal.getLocaleText("acc.common.cancelBtn")},
            fn:function(btn){
                if(btn!="yes")return;
                this.makeRequest();
            },
            scope:this,
            icon: Wtf.MessageBox.WARNING
        });
    },

    makeRequest:function(){
        var rec=this.form.getForm().getValues();
        rec.emailinvoice=false;
        rec.mode=82;
        //rec.cashaccount=this.Cash.getValue();
        rec.cashaccount = Wtf.account.companyAccountPref.cashaccount;
        //rec.shippingcharges=this.ShippingCharges.getValue();
        rec.othercharges=this.OtherCharges.getValue();
        if(this.withInv.getValue())
            rec.withoutinventory="on";
        rec.fyfrom=WtfGlobal.convertToGenericDate(this.fdate);
        rec.bbfrom=WtfGlobal.convertToGenericDate(this.bdate);
        rec.data=this.getUpdatedLockDetails();
        Wtf.Ajax.requestEx({
//            url: Wtf.req.account+'CompanyManager.jsp',
            url : "ACCCompanyPref/saveCompanyAccountPreferences.do",
            params: rec
        },this,this.genSuccessResponse,this.genFailureResponse);
    },

    addNewAccount:function(isEdit,record,winid,isexpense){
        callCOAWindow(isEdit, record, winid,false,false,false,isexpense);
        Wtf.getCmp(winid).on('update', function(){
         if(winid=="fxcoaWin")
             this.exStore.reload();
          else if (winid=="depcoaWin")
             this.exStore.reload();
          else
            this.dgStore.reload();
        }, this);
     },
    genSuccessResponse:function(response){
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.accPref.tabTitle"),response.msg],response.success*2+1);
        if(response.success){
            getCompanyAccPref();
            this.lockds.load();
            bHasChanged=true;

            this.ownerCt.items.each(function(item){
//                if(!(item===this||item.id=="tabdashboard"))
                if(!(item.id=="tabdashboard"))
                    item.ownerCt.remove(item);
            }, this);
        }
    },
 
    genFailureResponse:function(response){
        var msg=WtfGlobal.getLocaleText("acc.common.msg1");  //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    },

    validateFormat:function(val){
        var temp=val;
        temp=temp.replace(/[0]/g, "");
        if(val.length-temp.length<6)
            return WtfGlobal.getLocaleText("acc.accPref.msg8");
        else
            return true;
    },
    
    initForClose:function(){
        this.form.cascade(function(comp){
            if(comp.isXType('field')){
                comp.on('change', function(){this.isClosable=false;},this);
            }
        },this);
        this.grid.on("afteredit", function(){this.isClosable=false;},this);
    }
});
