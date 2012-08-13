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
function activateWizardCard(card){
    var imgLink = document.getElementById("accWizardLinkImage"+card);
    if(imgLink.className.indexOf("img-visited") != -1) {
        var wizard = Wtf.getCmp("SetUpWizard");
        if(!wizard.isLinksClosed) {
            var lay = wizard.cardPanel.getLayout();
            var i = lay.activeItem.id.split('card-')[1];
            var current = parseInt(i);
            wizard.navigate(current, parseInt(card));
        }
    }
}
function aWizardAddCustomer(){
    var wizard = Wtf.getCmp("SetUpWizard");
    if(wizard){
        wizard.close();
    }
    callCustomerDetails(null,true,true);
}
function aWizardAddVendor(){
    var wizard = Wtf.getCmp("SetUpWizard");
    if(wizard){
        wizard.close();
    }
    callVendorDetails(null,true,true);
}
function copyDefaultExchangeRates(id){
    Wtf.getCmp(id).getStore().proxy.conn.url = "ACCCurrency/getDefaultCurrencyExchange.do";
    Wtf.getCmp(id).getStore().load({params:{currencyid:Wtf.getCmp('currencyrecid').getValue()}});
}
Wtf.account.setUpWizard = function (config){
    this.hide1099=(WtfGlobal.getCurrencyID()!=usCurrencyID);
    this.buttons = [
                {
                    id: 'exit-setup',
                    text: WtfGlobal.getLocaleText("acc.setupWizard.msg2"),  //'Skip Setup',
                    scope:this,
                    handler: function(){
                        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.setupWizard.tabTitle"), WtfGlobal.getLocaleText("acc.setupWizard.msg1"),function(btn){
                            if(btn=="yes") {
                                var setUpData = "companyTypeId:\"defaultaccount\",addDefaultAccount:\"Yes\",withInventory:\"Yes\",";//withTax1099:\"Yes\",";
                                setUpData += "yearStartDate:\""+WtfGlobal.convertToGenericDate(this.FinancialStartDate)+"\",";
                                setUpData += "bookStartDate:\""+WtfGlobal.convertToGenericDate(this.BookStartDate)+"\",";
                                setUpData += "countryid:\""+Wtf.account.companyAccountPref.countryid+"\",";
                                setUpData += "currencyid:\""+Wtf.pref.Currencyid+"\",";
                                setUpData += "currencyDetails:[],taxDetails:[],bankDetails:[],lockDetails:[],withTax1099:\"No\"";
                                setUpData = "{"+setUpData+"}";
//                                alert(setUpData);

                                Wtf.Ajax.timeout = 600000;
                                Wtf.Ajax.requestEx({
                                    url: "ACCCompanySetup/SetupCompany.do",
                                    params:{
                                        data:setUpData
                                    }
                                },
                                this,
                                function(response){
                                    if(response.success){
                                        this.fireEvent("setup");
                                    }
                                    Wtf.Ajax.timeout = 30000;
                                },
                                function(response){
                                    Wtf.Ajax.timeout = 30000;
                                });

                                this.close();
                            }
                        },this);
                    }
                },
//                '->',
                {
                    id: 'move-prev',
                    text: WtfGlobal.getLocaleText("acc.setupWizard.previous"),  //'<< Previous',
                    handler: this.navHandler.createDelegate(this, [-1]),
                    hidden: true
                },
                {
                    id: 'move-next',
                    text: WtfGlobal.getLocaleText("acc.setupWizard.next"),  //'Next >>',
                    handler: this.navHandler.createDelegate(this, [1])
                }
            ]
    Wtf.apply(this,config);
    Wtf.account.setUpWizard.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.account.setUpWizard, Wtf.Window,{
    initComponent:function (){
        this.addEvents({
            'setup':true
        });
        Wtf.account.setUpWizard.superclass.initComponent.call(this);

        this.companyTypeID = "defaultaccount";      // Default Company type id
        this.isLinksClosed = false;                 // Enable/Disable processbar image link ==>false:enable, true:disable
        this.FinancialStartDate = new Date();       // Default financial start date
        this.BookStartDate = new Date();            // Default Book begining date
        this.getAllCards();

        this.cardsArr = [
					this.card0 = new Wtf.Panel({
					    id: 'card-0',
					    linkName: WtfGlobal.getLocaleText("acc.nee.16"),  //"Set Country, Currency & Financial Year",  "Select Industry Type",
					    border: false,
					    layout:"fit",
					    bodyStyle: 'padding:20px 100px 20px 100px',
					    items: this.countryCurrencyBody
					}),     
                    this.card1 = new Wtf.Panel({
                        id: 'card-1',
                        linkName: WtfGlobal.getLocaleText("acc.setupWizard.step1"),  //"Select Industry Type",
                        border: false,
                        layout:"fit",
                        bodyStyle: 'padding:20px 100px 20px 100px',
                        items: this.companyTypeBody
                    }),
                    this.card2 = new Wtf.Panel({
                        id: 'card-2',
                        linkName: WtfGlobal.getLocaleText("acc.setupWizard.step2"),  //"View Account list",
                        border: false,
                        layout: "fit",
                        bodyStyle: 'padding:10px 100px',
                        items: this.defaultAccountBody
                    }),
                    this.card3 = new Wtf.Panel({
                        id: 'card-3',
                        linkName: WtfGlobal.getLocaleText("acc.setupWizard.step3"),  //"Set Currency Exchange Rate",
                        border: false,
                        bodyStyle: 'padding:10px 100px',
                        layout:"fit",
//                        items: this.currencyGridPanel = new Wtf.Panel({
//                            layout: "fit",
//                            border: true,
//                            height: 250,
                            items: this.currencyBody
//                        })
                    }),
//                    this.card3 = new Wtf.Panel({
//                        id: 'card-4',
//                        border: false,
//                        layout:"fit",
//                        linkName: WtfGlobal.getLocaleText("acc.setupWizard.step4"),  //"Set Financial Year",
//                        bodyStyle: 'padding:50px 80px 50px 80px',
//                        items : this.financialYearBody
//                    }),
                    this.card4 = new Wtf.Panel({
                        id: 'card-4',
                        border: false,
                        linkName: WtfGlobal.getLocaleText("acc.setupWizard.step5"),  //"Set Tax Details",
                        layout: "fit",
                        bodyStyle: 'padding:10px 50px 30px 50px',
                        items: this.taxBody
                    }),
                    this.card5 = new Wtf.Panel({
                        id: 'card-5',
                        border: false,
                        linkName: WtfGlobal.getLocaleText("acc.setupWizard.step6"),  //"Set Bank Details",
                        layout: "fit",
                        bodyStyle: 'padding:30px 50px 30px 50px',
                        items: this.bankBody

                    }),
                    this.card6 = new Wtf.Panel({
                        id: 'card-6',
                        border: false,
                        linkName: WtfGlobal.getLocaleText("acc.setupWizard.step7"),  //"Set Inventory Preferences",
                        bodyStyle: 'padding:100px 150px 0 150px',
                        items: this.inventoryPreferencesForm
                    }),
                     this.card7 = new Wtf.Panel({
                        id: 'card-7',
                        border: false,
                        hidden: (Wtf.account.companyAccountPref.countryid != '244')?true:false,   //this.hide1099,
                        linkName: WtfGlobal.getLocaleText("acc.setupWizard.step8"),  //"Set Tax 1099 Preferences",
                        bodyStyle: 'padding:100px 150px 0 150px',
                        items: this.tax1099PreferencesForm
                    }),
                    this.card8 = new Wtf.Panel({
                        id: 'card-8',

                        border: false,
                        linkName: WtfGlobal.getLocaleText("acc.setupWizard.step9"),  //"Confirm Setup",
                        layout: "fit",
                        autoScroll: true,
                        html: this.previewTemplate=new Wtf.XTemplate(
                                 "<div>"+WtfGlobal.getLocaleText("acc.setupWizard.note13")+"</div><br/>"
                            )
                    }),
                    this.card9 = new Wtf.Panel({
                        id: 'card-9',
//                    this.card8 = new Wtf.Panel({
//                        id: 'card-8',
                        border: false,
                        layout: "fit",
                        html: "<div style='padding-left:50px;font-size:12px'><br/><br/>"+
                                "<h1>"+WtfGlobal.getLocaleText("acc.setupWizard.note19")+"</h1><br/><br/>" +
                                "<p>"+WtfGlobal.getLocaleText("acc.setupWizard.note18")+"</p><br/><br/>" +
                                "<p>"+WtfGlobal.getLocaleText("acc.setupWizard.note20")+"</p>" +
                                "<p class='listpanelcontent' style='padding:0' >"+WtfGlobal.getLocaleText("acc.setupWizard.or") +
                                    "<br/><a wtf:qtip='Create Customer' href='#' onclick='aWizardAddCustomer()'>"+WtfGlobal.getLocaleText("acc.setupWizard.note21")+"</a>" +
                                    "<br/><a wtf:qtip='Create Vendor' href='#' onclick='aWizardAddVendor()'>"+WtfGlobal.getLocaleText("acc.setupWizard.note22")+"</a>" +
                                "</p>" +

                                "</div>"
                    })];
        this.cardPanel = new Wtf.Panel({
            region: "center",
            bodyStyle: 'background:#f1f1f1;font-size:12px;padding:10px',
            layout: "card",
            border: false,
            activeItem: 0,
            items: this.cardsArr
        });

        this.topLinkPanelHTML = this.createLinkPanel();
        this.northPanel = new Wtf.Panel({
            region: "north",
            height: 100,
            border: false,
            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
            html: this.topLinkPanelHTML
        });

        this.wrapperPanel = new Wtf.Panel({
           layout: "border",
           items: [
               this.northPanel,
               this.cardPanel
           ]
        });

    },

    onRender: function(config){
        this.add(this.wrapperPanel);
        Wtf.account.setUpWizard.superclass.onRender.call(this, config);
    },

    navHandler: function(direction) {
    	var tax = (Wtf.getCmp('countryreccombo').getValue() != '244')?true:false;
       var lay = this.cardPanel.getLayout();
       var i = lay.activeItem.id.split('card-')[1];
       var current = parseInt(i);
       var next = current + direction;
       if(tax&&next==7){next+=direction}
       this.navigate(current, next);
    },

    navigate: function(current, next){
        if(this.beforeActivateCard(next)) {
            var lay = this.cardPanel.getLayout();
            var last = this.cardsArr.length-1;

            Wtf.getCmp('move-prev').setVisible(!next==0);
            Wtf.getCmp('move-next').setDisabled(false);
            this.isLinksClosed = false;
            this.updateLinkState(current,"visited");
            if(next<last) {
               lay.setActiveItem(next);
               this.onActivateCard(next);
               Wtf.getCmp('move-next').setText(next==last-1?WtfGlobal.getLocaleText("acc.common.saveBtn"):WtfGlobal.getLocaleText("acc.setupWizard.next"));
            } else if(next==last){ //Save
                Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.confirm"), WtfGlobal.getLocaleText("acc.setupWizard.note23"),function(btn){
                    if(btn=="yes") {
                        this.saveSetup(lay, next);
                    }
                },this);
            } else if(next>last){ //Finish
               this.close();
            }

            if((next==0 && !this.typesm.hasSelection())
                || (next==6 && this.inventoryPreferencesForm.getForm().getValues().withInventory==undefined)){
                    Wtf.getCmp('move-next').setDisabled(true);
                    this.isLinksClosed = true;
            }
             if((next==0 && !this.typesm.hasSelection())
                || (next==7 && this.tax1099PreferencesForm.getForm().getValues().withTax1099==undefined)){
                    Wtf.getCmp('move-next').setDisabled(true);
                    this.isLinksClosed = true;
            }
        }
    },

    saveSetup: function(lay, next){
        this.isLinksClosed = true;
        Wtf.getCmp('move-prev').setDisabled(true);
        Wtf.getCmp('move-next').setDisabled(true);

        Wtf.Ajax.timeout = 600000;
        Wtf.Ajax.requestEx({
            url: "ACCCompanySetup/SetupCompany.do",
            params:{
                data:this.getSetUpData()
            }
        },
        this,
        function(response){
            if(response.success){
                lay.setActiveItem(next);
                this.onActivateCard(next);
                Wtf.getCmp('move-prev').setVisible(false);
                Wtf.getCmp('exit-setup').setVisible(false);
                Wtf.getCmp('move-next').setText(WtfGlobal.getLocaleText("acc.setupWizard.finish"));
                Wtf.getCmp('move-next').setDisabled(false);
                this.isLinksClosed = true;
                Wtf.Ajax.timeout = 30000;
                this.fireEvent("setup");
                
                Wtf.pref.Currencyid = response.currency.Currencyid;
                Wtf.pref.CurrencyName = response.currency.CurrencyName;
                Wtf.pref.CurrencySymbol = response.currency.CurrencySymbol;
                Wtf.pref.Currency = response.currency.Currency;
            } else {
                this.isLinksClosed = false;
                Wtf.getCmp('move-prev').setDisabled(false);
                Wtf.getCmp('move-next').setDisabled(false);
                var msg=WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
                if(response.msg)msg="Failed to save company setup<br/>"+response.msg;
                this.showMessage(WtfGlobal.getLocaleText("acc.common.alert"),msg,Wtf.MessageBox.ERROR);
                Wtf.Ajax.timeout = 30000;
            }
        },
        function(response){
            this.isLinksClosed = false;
            Wtf.getCmp('move-prev').setDisabled(false);
            Wtf.getCmp('move-next').setDisabled(false);
            var msg=WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
            if(response.msg)msg=response.msg;
            this.showMessage(WtfGlobal.getLocaleText("acc.common.alert"),msg,Wtf.MessageBox.ERROR);
            Wtf.Ajax.timeout = 30000;
        });
    },

    createLinkPanel: function(){
        var links = new Array();
        for(var i=0; i<this.cardsArr.length; i++){
            if(this.cardsArr[i].linkName!=undefined && this.cardsArr[i].linkName!=""){
                links.push(this.cardsArr[i].linkName);
            }
        }
        var topLinkPanelHTML = "<div style=\"padding:5px\">&nbsp;</div>";
        var totalLinks = links.length;
        var totalWidth = this.width-15;
        var linkWidth = Math.floor(totalWidth/totalLinks);
        for(i=0; i<totalLinks; i++){
            topLinkPanelHTML += "<div id='accWizardLink"+i+"' class='accWizardLink' style=\"width:"+linkWidth+"px;\">" +
                                    "<div class='awl-image-bg'>" +
                                       "<div id='accWizardLinkImage"+i+"' class='awl-image"+(i==0?' img-current':'')+"' onclick=\"activateWizardCard('"+i+"');\">&nbsp;</div>" +
                                    "</div>" +
                                    "<div style=\"height:5px\"></div>" +
                                    "<div id='accWizardLinkText"+i+"' class='awl-text"+(i==0?' text-current':'')+"'>" +links[i] + "</div>" +
                                "</div>";
        }
        return topLinkPanelHTML;
    },

    beforeActivateCard: function(card){
        var process = true;
        if(card==1){ // set default company type as Others/None
            if(!this.typesm.hasSelection()){
//                this.showMessage("Alert","Please select <b>Others/None</b> as your Industry Type");
//                process = false;
                var defaultIndex = this.typeStore.find("id", "defaultaccount");
                this.typesm.selectRow(defaultIndex);
            }
        } else if(card==4){
            var fyval= this.getBeginingYearDate();
            var bbval= this.getBookBeginingDate();
            process = fyval & bbval;
        }
        return process;
    },

    onActivateCard: function(card){
        this.updateLinkState(card,"current");
        Wtf.getCmp("card-"+card).doLayout();
        if(card==1){
            if(this.typesm.hasSelection()){
                var rec = this.typesm.getSelected();
                if(this.companyTypeID != rec.data.id){
                    this.companyTypeID = rec.data.id;
                    this.defaultAccountStore.load({params:{companyType:this.companyTypeID}});
                }
            } else {
                this.defaultAccountStore.load({params:{companyType:this.companyTypeID}});
            }
        } else if (card==2){
//            this.card2.doLayout();
//            alert(this.defaultAccountForm.getForm().getValues());
        } else if (card==3){

        } else if (card==4){
            this.taxApplyDate.setValue(this.FinancialStartDate);
        } else if (card==5){
            this.bankApplyDate.setValue(this.FinancialStartDate);
        } else if (card==6){
//            alert(this.getBankDetails());
        } else if (card==7){
////            alert(this.getBankDetails());
        }else if (card==8){
//            alert(this.getPreviewTemplate());
            if(this.typesm.hasSelection()){
                rec = this.typesm.getSelected();
                if(this.companyTypeID != rec.data.id){
                    this.companyTypeID = rec.data.id;
                }
            }
            this.card8.body.dom.innerHTML = this.getPreviewTemplate();
        }
    }, 

    updateLinkState: function(card, state) {
        var ImgLink = document.getElementById("accWizardLinkImage"+card);
        if(ImgLink){
            var ImgLinkClass = ImgLink.className;
            var textLink = document.getElementById("accWizardLinkText"+card);
            var textLinkClass = textLink.className;

            if(state=="visited"){
                ImgLinkClass = ImgLinkClass.replace("img-current", "");
                ImgLinkClass += " img-visited";
                textLinkClass = textLinkClass.replace("text-current", "");
                textLinkClass += " text-visited";
            } else if(state=="current") {
                ImgLinkClass = ImgLinkClass.replace("img-visited", "");
                ImgLinkClass += " img-current";
                textLinkClass = textLinkClass.replace("text-visited", "");
                textLinkClass += " text-current";
            } else {
                ImgLinkClass = ImgLinkClass.replace("img-visited", "");
                ImgLinkClass = ImgLinkClass.replace("img-current", "");
                textLinkClass = textLinkClass.replace("text-visited", "");
                textLinkClass = textLinkClass.replace("text-current", "");
            }
            ImgLink.className = ImgLinkClass;
            textLink.className = textLinkClass;
        }
    },


    showMessage: function(title, message, icon) {
        Wtf.MessageBox.show({
            title: title,
            msg: message,
            buttons: Wtf.MessageBox.OK,
            icon: icon==undefined ? Wtf.MessageBox.INFO : icon
        });
    },

    getAllCards: function() {

//------------------- Country and currency ---------------------------//    	

    	this.currencystorerec = Wtf.data.Record.create ([
    	                                        {name: 'currencyid'},
    	                                        {name: 'name'},
    	                                ]);
    	                                this.currencyrecStore=new Wtf.data.Store({
    	                                    url: "ACCCurrency/getCurrency.do",
    	                                    reader: new Wtf.data.KwlJsonReader({
    	                                        root: "data"
    	                                    },this.currencystorerec)
    	                                });
    	this.currencyrecStore.load();
    	Wtf.countryStore.load();
    	
    	this.country = new Wtf.form.ComboBox({
            store: Wtf.countryStore,
//            name:'taxStoreid',
            width:160,
            id:'countryreccombo',
            listWidth:150,
            labelWidth:80,
            fieldLabel:WtfGlobal.getLocaleText("acc.rem.200"),
            displayField:'name',
            valueField:'id',
            value:Wtf.account.companyAccountPref.countryid,
            triggerAction: 'all',
            mode: 'local',
            typeAhead:true,
            emptyText: "Select Country",
            selectOnFocus:true,
            forceSelection: true
        });

    	
    	this.currency = new Wtf.form.ComboBox({
            store: this.currencyrecStore,
            id:'currencyrecid',
            width:160,
            listWidth:150,
            labelWidth:80,
            fieldLabel:WtfGlobal.getLocaleText("acc.rem.201"),
            displayField:'name',
            valueField:'currencyid',
            value:Wtf.pref.Currencyid,
            triggerAction: 'all',
            mode: 'local',
            typeAhead:true,
            emptyText: "Select Currency",
            selectOnFocus:true,
            forceSelection: true
        });
    	
    	this.currencyrecStore.on('load',function(){this.currency.setValue(Wtf.pref.Currencyid);},this);         
    	Wtf.countryStore.on('load',function(){this.country.setValue(Wtf.account.companyAccountPref.countryid);},this);
    	this.currency.on('select',function(){
    		if(Wtf.getCmp('wizardCurrencyGrid').getStore().getCount() != 0) {
    			copyDefaultExchangeRates('wizardCurrencyGrid');
    		}
    	},this);
    	
    	this.country.on('select',function(){
    		if(Wtf.getCmp('countryreccombo').getValue() != '244') {
    			this.updateLinkState("7","none");
    		}
    	},this);

//    	this.countryCurrencyForm = new Wtf.form.FormPanel({
//            border: false,
//            region: "center",
//            layout: "form",
//            autoScroll: true,
//            style: "background:#f1f1f1;",
//            defaults: {labelWidth:180,border:false},
//            items : [
//                new Wtf.form.FieldSet({
//                        cls: "wiz-card3-fieldset",
//                        width: 400,
//                        autoHeight:true,
//                        title:"Country and Currency Settings",  //'Financial Year Settings',
//                        items:[this.country, this.currency]
//                            
//
//                ]
//        });
    	
    	
    	
    	
//    	this.countryCurrencyBody = new Wtf.Panel({
//            border: false,
//            layout: "border",
//            items: [
//                this.countryCurrencyForm,
//                new Wtf.Panel({
//                    region:"south",
//                    heigth:20,
//                    border:false,
//                    style: "background:#f1f1f1;padding-top:2px",
//                    html:"<div style='font-size: 12px; padding-top:2px;'>"+
//                    "Note: The following 2 points will be applicable when you set Country and Currency from Setup Wizard <br/>"+
//                    " - Country cannot be changed for Accounting application through apps <br/>"+
//                    " - Currency cannot be changed for Accounting application apps <br/>"+
//                        "</div>"
//                }),
//                this.financialYearBody
//                ]
//        });
    	

    	
    	
    	
    	
    	
    	
    	
//------------------- Country and currency ---------------------------//    	
    	
//------------------- Card 0 ---------------------------//
        this.typeRec = Wtf.data.Record.create ([
                {name: 'id'},
                {name: 'name'},
                {name: 'details'}
        ]);
        this.typeStore=new Wtf.data.Store({
            url: "ACCCommon/getCompanyTypes.do",
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.typeRec)
        });
        this.typeStore.load();
        this.typeStore.on("load",function(store, recs, option){
            if(this.typeStore.getCount()>0){
                var defaultIndex = this.typeStore.find("id", this.companyTypeID);
                this.typesm.selectRow(defaultIndex);
//                Wtf.getCmp('move-next').setDisabled(true);
//                this.isLinksClosed = true;
            }
        },this);

        this.typesm = new Wtf.grid.RadioSelectionModel();//{singleSelect:true});
        this.typecm = new Wtf.grid.ColumnModel([
            this.typesm,
            {
                header: WtfGlobal.getLocaleText("acc.setupWizard.indType"),  //"Industry Type",
                sortable:true,
                dataIndex: 'name'
            }
        ]);
        this.typesm.on("selectionchange",function(sm){
            if(this.typesm.hasSelection()){
//                this.companyTypeID = this.typesm.getSelected().data.id;
                Wtf.getCmp('move-next').setDisabled(false);
                this.isLinksClosed = false;
            } else {
                Wtf.getCmp('move-next').setDisabled(true);
                this.isLinksClosed = true;
            }
        },this);
        this.companyTypeGrid = new Wtf.grid.GridPanel({
            region:"center",
            cm:this.typecm,
            store:this.typeStore,
//            height:350,
            sm:this.typesm,
            view: new Wtf.grid.GridView({
                forceFit: true,
                emptyText:WtfGlobal.getLocaleText("acc.setupWizard.note24")  //"Industry types are not available"
            }),
            loadMask:true
        });


        this.companyTypeBody = new Wtf.Panel({
            border: false,
            layout: "border",
            items: [
                this.companyTypeGrid,
                new Wtf.Panel({
                    region:"south",
                    heigth:20,
                    border:false,
                    style: "background:#f1f1f1;padding-top:2px",
                    html:"<div style='color: red; font-size: 12px; padding-top:2px;'>"+WtfGlobal.getLocaleText("acc.setupWizard.text3")+"</div>"
                })
                ]
        });

//        this.CompanyTypeGridBody = new Wtf.Panel({
//            border: false,
//            layout: "border",
//            items: [
//                this.companyTypeGrid,
//                new Wtf.Panel({
//                    region: "south",
//                    heigth: 100,
//                    border: false,
//                    layout: "form",
//                    style: "background:#f1f1f1;padding:15px 0 0 0",
//                    title: "Note",
//                    items: new Wtf.form.FieldSet({
//                            border: false,
//                            title: "Note",
//                            autoHeight: true,
//
//                            items: new Wtf.Panel({
//                                border: false,
//                                html: "If your industry type is different then above shown list, select \"Others/None\""
//                            })
//                        })
//                })
//                ]
//        });

//------------------- Card 1 <View Account List>---------------------------//

        this.defaultAccountRec = Wtf.data.Record.create ([
                {name: 'id'},
                {name: 'name'},
                {name: 'groupname'},
                {name: 'companytype'}
        ]);
        this.defaultAccountStore=new Wtf.data.Store({
            url: "ACCAccount/getDefaultAccount.do",
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.defaultAccountRec)
        });
        this.defaultAccountStore.load({params:{companyType:this.companyTypeID}});
        this.defaultAccountcm = new Wtf.grid.ColumnModel([
            new Wtf.grid.RowNumberer(),
            {
                header: WtfGlobal.getLocaleText("acc.setupWizard.accName"),  //"Account Name",
                sortable:true,
                dataIndex: 'name',
                regex:Wtf.specialChar
            },
            {
                header: WtfGlobal.getLocaleText("acc.setupWizard.accType"),  //"Account Type",
                sortable:true,
                dataIndex: 'groupname'
            }
        ]);
        this.defaultAccountsGrid = new Wtf.grid.GridPanel({
            region: "center",
            border: true,
            cm:this.defaultAccountcm,
            store:this.defaultAccountStore,
            view: new Wtf.grid.GridView({
                forceFit: true,
                emptyText:WtfGlobal.getLocaleText("acc.setupWizard.note25")  //"Account list is not available"
            }),
            loadMask:true
        });

        this.defaultAccountForm = new Wtf.form.FormPanel({
            region: "south",
            heigth: 120,
            border: false,
            style: "background:#f1f1f1;padding:15px 0 0 0",
            labelWidth:10,
            items : new Wtf.form.FieldSet({
                title: WtfGlobal.getLocaleText("acc.setupWizard.question1"),  //'Do you want to use Account List shown above?',
                autoHeight: true,
                border: false,
                cls: "wiz-card6-fieldset",
                defaultType: 'radio',
                items: [
                {
                    checked: true,
                    fieldLabel: '',
                    labelSeparator: '',
                    boxLabel: WtfGlobal.getLocaleText("acc.setupWizard.listSelect1"),  //'Yes, I will start with these accounts and will edit them later.',
                    name: 'addDefaultAccount',
                    inputValue: "Yes"
                }, {
                    ctCls:"fieldset-item",
                    fieldLabel: '',
                    labelSeparator: '',
                    boxLabel: WtfGlobal.getLocaleText("acc.setupWizard.listSelect2"),  //'No, I will create my own accounts later.',
                    name: 'addDefaultAccount',
                    inputValue: "No"
                }]
            })
        });

        this.defaultAccountBody = new Wtf.Panel({
            border: false,
            layout: "border",
            items: [
                this.defaultAccountsGrid,
                this.defaultAccountForm
                ]
        });


//------------------- Card 2 <Set Currency Exchange Rate>---------------------------//
        this.currencyRec = new Wtf.data.Record.create([
            {
                name: 'id'
            },{
                name: 'applydate', type:'date'
            },{
                name: 'fromcurrency'
            },{
                name: 'tocurrency'
            },{
                name: 'exchangerate', type:'float'
            },{
                name: 'tocurrencyid'
            },{
                name: 'fromcurrencyid'
            },{
                name: 'companyid'
            }
        ]);
        this.currencyStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:"count"
            },this.currencyRec),
            url: "ACCCurrency/getCurrencyExchangeList.do"
        });
        this.currencyStore.load();
        this.currencyStore.on("load",function(store){
            this.currencyStore.filterBy(function(rec){
                rec.data.applydate = ((rec.data.applydate)).add(Date.DAY,-1);    
                if(rec.data.tocurrencyid==rec.data.fromcurrencyid)
                    return false
                else
                    return true
            },this);
        },this);
        this.currencyCm= new Wtf.grid.ColumnModel([
            new Wtf.grid.RowNumberer(),
            {
                header:WtfGlobal.getLocaleText("acc.setupWizard.gridForCurrency"),  //"Foreign Currency",
                dataIndex:'tocurrency',
                autoWidth : true
            },{
                header:WtfGlobal.getLocaleText("acc.setupWizard.gridExcRate"),  //"Enter Exchange Rate",
                dataIndex:'exchangerate',
                width:110,
                align:'right',
                editor:this.exchangeRate=new Wtf.form.NumberField({
                    allowBlank: false,
                    decimalPrecision:7,
                    allowNegative: false,
                    minValue:0
                })
            },{
                header:WtfGlobal.getLocaleText("acc.setupWizard.gridViewConv"),  //"View Conversion",
                width:200,
                dataIndex:'fromcurrency',
                renderer: function(a,b,c){
                    return "1 "+a+" = "+c.data.exchangerate+" "+c.data.tocurrency;
                }
            },{
                header: WtfGlobal.getLocaleText("acc.setupWizard.applyDate"),  //"Applied Date",
                dataIndex: 'applydate',
                width:100,
                renderer:(WtfGlobal.onlyDateRenderer),
                minValue:new Date().clearTime(true),
                editor:new Wtf.form.DateField({
                    name:'applydate',
                    format:WtfGlobal.getOnlyDateFormat()
                })
            }
        ]);
        this.currencyGrid = new Wtf.grid.EditorGridPanel({
            id:"wizardCurrencyGrid",
            region:"center",
            autoScroll:true,
            clicksToEdit:1,
            store: this.currencyStore,
            cm: this.currencyCm,
            loadMask : true,
            border: true,
            view: new Wtf.grid.GridView({
                forceFit: true,
                emptyText:"<div style=\"text-align:center\"><a style=\"font-size:12px;font-weight:normal;\" onclick=\"javascript: copyDefaultExchangeRates('wizardCurrencyGrid')\" href='#' class='tbar-link-text'>"+WtfGlobal.getLocaleText("acc.setupWizard.note2")+"</a></div>"
            })
        });

        this.currencyBody = new Wtf.Panel({
            border: false,
            layout: "border",
            items: [
                new Wtf.Panel({
                    region:"north",
                    heigth:50,
                    border:false,
                    style: "background:#f1f1f1;padding-top:2px;padding-bottom:5px",
                    html:"<div style='font-size: 12px;'>"+
//                            "<b>"+WtfGlobal.getLocaleText("acc.setupWizard.homeCurrency")+" "+WtfGlobal.getCurrencyName()+".</b>&nbsp;"+
//                            "<a onclick=\"javascript: copyDefaultExchangeRates('wizardCurrencyGrid')\" href='#' class='tbar-link-text'>Download Latest Exchange Rates</a><br/>"+
                            "</div>"
                }),
                this.currencyGrid,
                new Wtf.Panel({
                    region:"south",
                    heigth:20,
                    border:false,
                    style: "background:#f1f1f1;padding-top:2px",
                    html:"<div style='color: red; font-size: 12px; padding-top:2px;'>"+WtfGlobal.getLocaleText("acc.setupWizard.note1")+"</div>"
                })
                ]
        });
//------------------- Card 3 <Set Financial Year>---------------------------//
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
        this.fdays = new Wtf.form.ComboBox({
            store:  this.daysStore,
            width: 150,
            listWidth: 150,
            name:'daysid',
            displayField:'name',
            fieldLabel:WtfGlobal.getLocaleText("acc.setupWizard.FinYrDate"),  //'Financial Year Date',
            valueField:'daysid',
            mode: 'local',
            anchor:'95%',
            triggerAction: 'all',
            forceSelection: true,
            selectOnFocus:true,
            allowBlank:false,
            value:1
        });
        this.fmonth = new Wtf.form.ComboBox({
            store: this.monthStore,
            width: 150,
            listWidth: 150,
            fieldLabel:WtfGlobal.getLocaleText("acc.setupWizard.month"),  //'Month',
            name:'monthid',
            displayField:'name',
            anchor:'95%',
            valueField:'monthid',
            mode: 'local',
            triggerAction: 'all',
            forceSelection: true,
            selectOnFocus:true,
            allowBlank:false,
            value:0
        });
        this.bdays = new Wtf.form.ComboBox({
            store:  this.daysStore,
            width: 150,
            listWidth: 150,
            name:'daysid',
            displayField:'name',
            fieldLabel:WtfGlobal.getLocaleText("acc.setupWizard.BookBeginingDate"),  //'Book Beginning Date',
            valueField:'daysid',
            mode: 'local',
            anchor:'95%',
            triggerAction: 'all',
            forceSelection: true,
            selectOnFocus:true,
            allowBlank:false,
            value:1
        });
        this.bmonth = new Wtf.form.ComboBox({
            store: this.monthStore,
            width: 150,
            listWidth: 150,
            fieldLabel:WtfGlobal.getLocaleText("acc.setupWizard.month"),  //'Month',
            name:'monthid',
            displayField:'name',
            anchor:'95%',
            valueField:'monthid',
            mode: 'local',
            triggerAction: 'all',
            forceSelection: true,
            selectOnFocus:true,
            allowBlank:false,
            value:0
        });

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
            url : "ACCCompanyPref/getYearLock.do",
            baseParams:{
                mode:94
            }
        });
        this.lockds.load();
//        this.lockds.on('load',this.setLockYearValues,this);

        this.financialYearForm = new Wtf.form.FormPanel({
            border: false,
            region: "center",
            layout: "form",
            autoScroll: true,
            style: "background:#f1f1f1; padding-top:3.5cm;",
            defaults: {labelWidth:200,border:false},
            items : [
                new Wtf.form.FieldSet({
//                        xtype:'fieldset',
                        cls: "wiz-card3-fieldset",
                        autoHeight:true,
                        title:WtfGlobal.getLocaleText("acc.setupWizard.FYsetting"),  //'Financial Year Settings',
//                        defaults:{
//                            format:WtfGlobal.getOnlyDateFormat(),
//                            allowBlank:false,
//                            border:false
//                        },
                        items:[{
                            layout:"column",
                            border:false,
                            labelWidth:130,
                            defaults:{border:false},
                            items:[{
//                                columnWidth: .5,
                                width:"54%",
                                layout:'form',
                                items:this.fdays
                            },{
//                                columnWidth: .5,
                                width:"38%",
                                cls:"right-column",
                                labelWidth:50,
                                layout:'form',
                                items:this.fmonth
                            }]
                        },{
                            layout:'column',
                            cls:"fieldset3-item",
                            border:false,
                            labelWidth:130,
                            defaults:{border:false},
                            items:[{
                                layout:'form',
//                                columnWidth:0.5,
                                width:"54%",
                                items:this.bdays
                            },{
//                                columnWidth:0.5,
                                width:"38%",
                                cls:"right-column",
                                labelWidth:50,
                                layout:'form',
                                items:this.bmonth
                            }]
                        }]
                    })
                ]
        });
//        this.fmonth.on('beforeselect',this.getBeginingYearDate,this);
//        this.fdays.on('beforeselect',this.getBeginingYearDate,this);
//        this.bmonth.on('beforeselect',this.getBookBeginingDate,this);
//        this.bdays.on('beforeselect',this.getBookBeginingDate,this);
        this.fmonth.on('select',this.getBeginingYearDate,this);
        this.fdays.on('select',this.getBeginingYearDate,this);
        this.bmonth.on('select',this.getBookBeginingDate,this);
        this.bdays.on('select',this.getBookBeginingDate,this);
        this.getBeginingYearDate();
        this.getBookBeginingDate();


        this.financialYearBody = new Wtf.Panel({
            border: false,
            layout: "border",
            items: [
                this.financialYearForm,
                new Wtf.Panel({
                    region:"south",
                    heigth:20,
                    border:false,
                    style: "background:#f1f1f1;padding-top:2px",
                    html:"<div style='font-size: 12px; padding-top:2px;'>"+
                    WtfGlobal.getLocaleText("acc.setupWizard.note3")+
                    WtfGlobal.getLocaleText("acc.setupWizard.note4")+
                    WtfGlobal.getLocaleText("acc.setupWizard.note5")+
                        "</div>"
                })
                ]
        });
        
        
        this.countryCurrencyForm = new Wtf.form.FormPanel({
            border: false,
            region: "center",
            layout: "form",
            autoScroll: true,
            style: "background:#f1f1f1;padding-top:2px",
            defaults: {labelWidth:130,border:false},
            items : [
                new Wtf.form.FieldSet({
                        cls: "wiz-card0-fieldset",
                        width: 350,
                        autoHeight:true,
                        title:WtfGlobal.getLocaleText("acc.nee.17"),  //"Country and Currency Settings",  //'Financial Year Settings',
                        items:[this.country, this.currency]
                }),
                               
                new Wtf.form.FieldSet({
                  cls: "wiz-card3-fieldset",
                  autoHeight:true,
                  style: "padding-top:10px",
                  title:WtfGlobal.getLocaleText("acc.setupWizard.FYsetting"),  //'Financial Year Settings',
                  items:[{
                      layout:"column",
                      border:false,
                      labelWidth:130,
                      defaults:{border:false},
                      items:[{
                          width:"54%",
                          layout:'form',
                          items:this.fdays
                      },{
                          width:"38%",
                          cls:"right-column",
                          labelWidth:50,
                          layout:'form',
                          items:this.fmonth
                      }]
                  },{
                      layout:'column',
                      cls:"fieldset3-item",
                      border:false,
                      labelWidth:130,
                      defaults:{border:false},
                      items:[{
                          layout:'form',
                          width:"54%",
                          items:this.bdays
                      },{
                          width:"38%",
                          cls:"right-column",
                          labelWidth:50,
                          layout:'form',
                          items:this.bmonth
                      }]
                  }]
              })               
                            

                ]
        });
                

        this.countryCurrencyBody = new Wtf.Panel({
            border: false,
            layout: "border",
            items: [
                this.countryCurrencyForm,
//                this.financialYearForm,
                new Wtf.Panel({
                    region:"south",
                    heigth:20,
                    border:false,
                    style: "background:#f1f1f1;padding-top:2px",
                    html:"<div style='font-size: 12px; padding-top:2px;'>"+
                    WtfGlobal.getLocaleText("acc.rem.205")+"<br>"+
                    WtfGlobal.getLocaleText("acc.rem.206")+"<br>"+
                    WtfGlobal.getLocaleText("acc.rem.207")+"<br>"+"<br>"+
                    WtfGlobal.getLocaleText("acc.rem.211")+"<br>"+"<br>"+
                    WtfGlobal.getLocaleText("acc.setupWizard.note3")+
                    WtfGlobal.getLocaleText("acc.setupWizard.note4")+
                    WtfGlobal.getLocaleText("acc.setupWizard.note5")+
                        "</div>"
                }),
                ]
        });

//------------------- Card 4 <Set Tax Details>---------------------------//

        this.taxRec = new Wtf.data.Record.create([
            {name: 'taxid'},
            {name: 'taxname'},
            {name: 'taxcode'},
            {name: 'percent', type:'float'},
            {name: 'applydate', type:'date'},
            {name: 'isEditing'}
        ]);
        this.taxStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:"count"
            },this.taxRec),
            url:"ACCTax/getTax.do"
        });
//        this.taxStore.load({
//            params:{
//                group:9
//            }
//        });
        this.taxCm= new Wtf.grid.ColumnModel([
//            new Wtf.grid.RowNumberer(),
            {
                header:WtfGlobal.getLocaleText("acc.setupWizard.gridTaxName"),  //"Tax Name",
                dataIndex:'taxname'
            },{
                header:WtfGlobal.getLocaleText("acc.setupWizard.gridTaxCode"),  //"Tax Code",
                dataIndex:'taxcode'
            },{
                header:WtfGlobal.getLocaleText("acc.setupWizard.gridPercent"),  //"Percent",
                dataIndex:'percent',
                align: "left",
                renderer:function(val){
                    if(typeof val != "number") return "";
                    return val+'%';
                }
            },{
                header: WtfGlobal.getLocaleText("acc.setupWizard.gridTaxApplicable"),  //"Tax Applicable Date",
                dataIndex: 'applydate',
                renderer:WtfGlobal.onlyDateRenderer
            },{
                width:45,
                header:WtfGlobal.getLocaleText("acc.setupWizard.gridEdit"),  //'Edit',
                dataIndex: 'action',
                align: "center",
                renderer: function(){
                    return "<div class='"+getButtonIconCls(Wtf.etype.edit)+"' style='height: 20px; width: 16px; cursor:pointer; float:left;'></div>";
                }
            },{
                width:50,
                header:WtfGlobal.getLocaleText("acc.setupWizard.gridDelete"),  //'Delete',
                dataIndex: 'action',
                align: "center",
                renderer: function(){
                    return "<div class='"+getButtonIconCls(Wtf.etype.deletegridrow)+"' style='height: 16px; width: 16px; cursor:pointer; float:left; margin-left:5px'></div>";
                }
            }
        ]);
        this.taxGrid = new Wtf.grid.GridPanel({
            region:"center",
            autoScroll:true,
            clicksToEdit:1,
            sm: new Wtf.grid.RowSelectionModel(),
            store: this.taxStore,
            cm: this.taxCm,
            loadMask : true,
            border: false,
            view: new Wtf.grid.GridView({
                forceFit: true,
                emptyText:WtfGlobal.getLocaleText("acc.setupWizard.note26")  //"Fill the form to add Tax Details"
            })
        });
        this.taxGrid.on('rowclick',function(grid,rowindex,e){
            if(e.getTarget(".delete-gridrow")){
                Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.setupWizard.note27"), function(btn){
                    if(btn!="yes") return;
                    var store=grid.getStore();
                    store.remove(store.getAt(rowindex));
                }, this);
            } else if(e.getTarget(".edit")){
                var store=grid.getStore();
                this.taxForm.getForm().loadRecord(store.getAt(rowindex));
//                store.remove(store.getAt(rowindex));
                store.each(function(rec){
                    rec.data.isEditing = 0;
                },this);
                store.getAt(rowindex).data.isEditing = 1;
            }
        },this);

        this.taxForm = new Wtf.form.FormPanel({
            region: "north",
            border: false,
            style: "background:#f1f1f1;padding:0 15px 15px 15px",
            height: 185,
            labelWidth:160,
            items : [
                new Wtf.Panel({
                    border: false,
                    html : "<div style='font-size: 12px;padding-bottom:10px'>"+WtfGlobal.getLocaleText("acc.setupWizard.note7")+"<br/></div>"
                }),
                this.taxId = new Wtf.form.Hidden({
                    hidden:true,
                    name:"taxid"
                }),
                this.taxName = new Wtf.form.TextField({
                    fieldLabel:WtfGlobal.getLocaleText("acc.setupWizard.gridTaxName"),  //"Tax Name*",
                    width:200,
                    maxLength:50,
                    allowBlank: false,
                    emptyText: WtfGlobal.getLocaleText("acc.setupWizard.enterTaxName"),  //"Enter Tax name here",
                    name:"taxname",
                    regex:Wtf.specialChar
                }),
                this.taxCode = new Wtf.form.TextField({
                    fieldLabel:WtfGlobal.getLocaleText("acc.setupWizard.gridTaxCode"),  //"Tax Code*",
                    width:200,
                    maxLength:50,
                    allowBlank: false,
                    emptyText: WtfGlobal.getLocaleText("acc.setupWizard.enterTaxCode"),  //"Enter Tax code here",
                    name:"taxcode",
                    regex:Wtf.specialChar
                }),
                this.taxPercent = new Wtf.form.NumberField({
                    fieldLabel:WtfGlobal.getLocaleText("acc.setupWizard.gridPercent"),  //"Percent*",
                    width:200,
                    allowBlank: false,
                    maxValue:100,
                    allowNegative:false,
                    emptyText: WtfGlobal.getLocaleText("acc.setupWizard.enterPercent"),  //"Enter Percent here",
                    maxLength:50,
                    name:"percent"
                }),
                this.taxApplyDate = new Wtf.form.DateField({
                    fieldLabel:WtfGlobal.getLocaleText("acc.setupWizard.gridTaxApplicable"),  //"Tax Applicable Date*",
                    width:183,
                    allowNegative:false,
                    name:'applydate',
                    readOnly:true,
                    value: this.FinancialStartDate,
                    format:WtfGlobal.getOnlyDateFormat()
                }),
                new Wtf.Button({
                    text:WtfGlobal.getLocaleText("acc.setupWizard.addToListBtn"),  //"Add to list",
                    scope:this,
                    handler:function() {
                        if(!this.taxForm.getForm().isValid()) {
                            this.showMessage(WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.setupWizard.note15"));
                        } else {
                            var FIND_TAX = this.taxName.getValue();
                            var editIndex = this.taxStore.find("isEditing","1");
                            var index = this.taxStore.findBy(function(rec){
                                                                var taxname=rec.data['taxname'].trim();
//                                                                taxname=taxname.replace(/\s+/g, '');
                                                                if(taxname==FIND_TAX)// && rec.data.isEditing!=1)
                                                                    return true;
                                                                else
                                                                    return false
                                                            });
                            if(index!=-1 && ((editIndex ==-1) || (editIndex!=-1 && index!=editIndex))){
                                this.showMessage("Alert","Tax entry for <b>"+this.taxName.getValue()+"</b> is available in tax details");
                            } else {
                                if(index==-1 && editIndex==-1){
                                    var newrec = new this.taxRec({
                                        taxid:this.taxStore.getCount(),
                                        taxname:this.taxName.getValue(),
                                        taxcode:this.taxCode.getValue(),
                                        percent:this.taxPercent.getValue(),
                                        applydate:this.taxApplyDate.getValue()
                                    });
                                    this.taxStore.add(newrec);
                                } else {
                                    var rec = this.taxStore.getAt(editIndex);
                                    rec.data.taxname=this.taxName.getValue();
                                    rec.data.taxcode=this.taxCode.getValue();
                                    rec.data.percent=this.taxPercent.getValue();
                                    rec.data.applydate=this.taxApplyDate.getValue();
                                    rec.data.isEditing = 0;
                                    this.taxStore.commitChanges();
                                    this.taxGrid.getView().refresh();
                                }
                                this.taxForm.getForm().setValues({
                                    taxid:"",
                                    taxname:"",
                                    taxcode:"",
                                    percent:"",
                                    applydate:this.FinancialStartDate
                                });
                                this.taxForm.getForm().clearInvalid();
                            }
                        }
                    }
                })
            ]
        });

        this.taxBody = new Wtf.Panel({
            border: false,
            layout: "border",
            items: [
                this.taxForm,
                this.taxGrid,
                new Wtf.Panel({
                    region:"south",
                    heigth:20,
                    border:false,
                    style: "background:#f1f1f1;padding-top:2px",
                    html:"<div style='color: red; font-size: 12px; padding-top:2px;'>"+WtfGlobal.getLocaleText("acc.setupWizard.note6")+"</div>"
                })
                ]
        });

//------------------- Card 5 <Set Bank Details>---------------------------//

        this.bankRec = new Wtf.data.Record.create([
            {name: 'bankid'},
            {name: 'bankname'},
            {name: 'accountname'},
            {name: 'accountno'},
            {name: 'balance'},
            {name: 'applydate', type:'date'},
            {name: 'isEditing'}
        ]);
        this.bankStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:"count"
            },this.bankRec),
            url:"ACCAccount/getAccounts.do"
        });
        this.bankCm= new Wtf.grid.ColumnModel([
//            new Wtf.grid.RowNumberer(),
            {
                header:WtfGlobal.getLocaleText("acc.setupWizard.BankName"),  //"Bank Name",
                dataIndex:'bankname'
            },{
                header:WtfGlobal.getLocaleText("acc.setupWizard.AccountName"),  //"Account Name",
                dataIndex:'accountname'
            },{
                header:WtfGlobal.getLocaleText("acc.setupWizard.AccountNo"),  //"Account No.",
                dataIndex:'accountno'
            },{
                header:WtfGlobal.getLocaleText("acc.setupWizard.openBal"),  //"Opening Balance",
                dataIndex:'balance',
                autoWidth : true,
                align: "right"
            },{
                header: WtfGlobal.getLocaleText("acc.setupWizard.appDate"),  //"Applicable Date",
                dataIndex: 'applydate',
                renderer:WtfGlobal.onlyDateRenderer
            },{
                width:45,
                header:WtfGlobal.getLocaleText("acc.setupWizard.gridEdit"),  //'Edit',
                dataIndex: '',
                align: "center",
                renderer: function(){
                    return "<div class='"+getButtonIconCls(Wtf.etype.edit)+"' style='height: 20px; width: 16px; cursor:pointer; float:left;'></div>";
                }
            },{
                width:55,
                header:WtfGlobal.getLocaleText("acc.setupWizard.gridDelete"),  //'Delete',
                dataIndex: '',
                align: "center",
                renderer: function(){
                    return "<div class='"+getButtonIconCls(Wtf.etype.deletegridrow)+"' style='height: 16px; width: 16px; cursor:pointer; float:left;'></div>"
                }
            }
        ]);
        this.bankGrid = new Wtf.grid.GridPanel({
            region:"center",
            autoScroll:true,
            store: this.bankStore,
            cm: this.bankCm,
            loadMask : true,
            border: false,
            view: new Wtf.grid.GridView({
                forceFit: true,
                emptyText:WtfGlobal.getLocaleText("acc.setupWizard.note28")  //"Fill the form to add Bank Details"
            })
        });
        this.bankGrid.on('rowclick',function(grid,rowindex,e){
            if(e.getTarget(".delete-gridrow")){
                Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.setupWizard.note27"), function(btn){
                    if(btn!="yes") return;
                    var store=grid.getStore();
                    store.remove(store.getAt(rowindex));
                }, this);
            } else if(e.getTarget(".edit")){
                var store=grid.getStore();
                this.bankForm.getForm().loadRecord(store.getAt(rowindex));
//                store.remove(store.getAt(rowindex));
                store.each(function(rec){
                    rec.data.isEditing = 0;
                },this);
                store.getAt(rowindex).data.isEditing = 1;
            }
        },this);

        this.bankForm = new Wtf.form.FormPanel({
            region: "north",
            heigth: 370,
            border: false,
            style: "background:#f1f1f1;padding:0 15px 15px 15px",
            labelWidth:120,
            items : [
			new Wtf.Panel({
			    border: false,
			    html : "<div style='font-size: 12px;padding-bottom:10px'>"+WtfGlobal.getLocaleText("acc.nee.18")+"<br/></div>"
			}),
                this.bankId = new Wtf.form.Hidden({
                    hidden:true,
                    name:"bankid"
                }),
                this.bankName = new Wtf.form.TextField({
                    fieldLabel:WtfGlobal.getLocaleText("acc.setupWizard.BankName"),  //"Bank Name*",
                    width:200,
                    maxLength:50,
                    allowBlank: false,
                    emptyText:WtfGlobal.getLocaleText("acc.setupWizard.enterBankName"),  //"Enter Bank name here",
                    name:"bankname",
                    regex:Wtf.specialChar
                }),
                this.accountName = new Wtf.form.TextField({
                    fieldLabel:WtfGlobal.getLocaleText("acc.setupWizard.AccountName"),  //"Account Name*",
                    width:200,
                    maxLength:50,
                    allowBlank: false,
                    emptyText: WtfGlobal.getLocaleText("acc.setupWizard.enterAccountName"),  //"Enter Account name here",
                    name:"accountname",
                    regex:Wtf.specialChar
                }),
                this.bankAccountNo = new Wtf.form.TextField({
                    fieldLabel:WtfGlobal.getLocaleText("acc.setupWizard.AccountNo"),  //"Account No",
                    width:200,
                    maxLength:30,
                    emptyText: WtfGlobal.getLocaleText("acc.setupWizard.enterAccountNo"),  //"Enter Account no. here",
//                    allowBlank: false,
                    name:"accountno",
                    regex:Wtf.specialChar
                }),
                this.bankBalance= new Wtf.form.NumberField({
                    fieldLabel:WtfGlobal.getLocaleText("acc.setupWizard.openBal"),  //"Opening Balance",
                    width:200,
                    allowBlank: false,
                    minValue:0,
                    allowNegative:false,
                    value:0,
                    maxLength:11,
                    name:"balance"
                }),
                this.bankApplyDate = new Wtf.form.DateField({
                    fieldLabel:WtfGlobal.getLocaleText("acc.setupWizard.appDate"),  //"Applicable Date*",
                    width:183,
                    allowNegative:false,
                    name:'applydate',
                    readOnly:true,
                    value: this.FinancialStartDate,
                    format:WtfGlobal.getOnlyDateFormat()
                }),
                new Wtf.Button({
                    text:WtfGlobal.getLocaleText("acc.setupWizard.addToListBtn"),  //"Add to list",
                    scope:this,
                    handler:function() {
                                if(!this.bankForm.getForm().isValid()) {
                                    this.showMessage("Alert",WtfGlobal.getLocaleText("acc.setupWizard.note15"));
                                } else {
                                    var FIND_BANK = this.bankName.getValue();
                                    var editIndex = this.bankStore.find("isEditing","1");
                                    var index = this.bankStore.findBy(function(rec){
                                                                        var bankname=rec.data['bankname'].trim();
//                                                                        bankname=bankname.replace(/\s+/g, '');
                                                                        if(bankname==FIND_BANK)
                                                                            return true;
                                                                        else
                                                                            return false
                                                                    });
                                    if(index!=-1 && ((editIndex ==-1) || (editIndex!=-1 && index!=editIndex))){
                                        this.showMessage("Alert","Bank entry for <b>"+this.bankName.getValue()+"</b> is available in bank details");
                                    } else {
                                        if(index==-1 && editIndex==-1){
                                            var newrec = new this.bankRec({
                                                bankid:this.bankStore.getCount(),
                                                bankname:this.bankName.getValue(),
                                                accountname:this.accountName.getValue(),
                                                accountno:this.bankAccountNo.getValue(),
                                                balance:this.bankBalance.getValue(),
                                                applydate:this.bankApplyDate.getValue()
                                            });
                                            this.bankStore.add(newrec);
                                        } else {
                                            var rec = this.bankStore.getAt(editIndex);
                                            rec.data.bankname=this.bankName.getValue();
                                            rec.data.accountname=this.accountName.getValue();
                                            rec.data.accountno=this.bankAccountNo.getValue();
                                            rec.data.balance=this.bankBalance.getValue();
                                            rec.data.applydate=this.bankApplyDate.getValue();
                                            rec.data.isEditing = 0;
                                            this.bankStore.commitChanges();
                                            this.bankGrid.getView().refresh();
                                        }

                                        this.bankForm.getForm().setValues({
                                            bankid:"",
                                            bankname:"",
                                            accountname:"",
                                            accountno:"",
                                            balance:0,
                                            applydate:this.FinancialStartDate
                                        });
                                        this.bankForm.getForm().clearInvalid();
                                    }
                                }
                            }
                })
            ]
        });
        this.bankName.on("change",function(){
            if(this.accountName.getValue()==""){
                this.accountName.setValue(this.bankName.getValue());
            }
        },this);
        this.bankBody = new Wtf.Panel({
            border: false,
            layout: "border",
            items: [
                this.bankForm,
                this.bankGrid,
                new Wtf.Panel({
                    region:"south",
                    heigth:20,
                    border:false,
                    style: "background:#f1f1f1;padding-top:2px",
                    html:"<div style='color: red; font-size: 12px; padding-top:2px;'>"+WtfGlobal.getLocaleText("acc.setupWizard.note8")+"</div>"
                })
                ]
        });

//------------------- Card 6 <Set Inventory Preferences>---------------------------//
        this.inventoryPreferencesForm = new Wtf.form.FormPanel({
            border: false,
            labelWidth:20,
            items : new Wtf.form.FieldSet({
                title: WtfGlobal.getLocaleText("acc.setupWizard.question2"),  //'Do you want to track Inventory in Deskera Accounting?',
                autoHeight: true,
                cls: "wiz-card6-fieldset",
                border: false,
                defaultType: 'radio',
                items: [
                this.withInventory= new Wtf.form.Radio({
                    checked: true,
                    fieldLabel: '',
                    labelSeparator: '',
                    boxLabel: WtfGlobal.getLocaleText("acc.setupWizard.note29"),  //"Yes, I want to use Deskera Accounting <b>with</b> Inventory.",
                    name: 'withInventory',
                    inputValue: "Yes"
                }),
                this.withOutInventory= new Wtf.form.Radio({
                    ctCls:"fieldset-item",
                    fieldLabel: '',
                    labelSeparator: '',
                    boxLabel: WtfGlobal.getLocaleText("acc.setupWizard.note30"),  //"No, I want to use Deskera Accounting <b>without</b> Inventory.",
                    name: 'withInventory',
                    inputValue: "No"
                })]
            })
        });
        this.withInventory.on("check",function(){
            this.isLinksClosed = this.inventoryPreferencesForm.getForm().getValues().withInventory==undefined;
            Wtf.getCmp('move-next').setDisabled(this.isLinksClosed);
        },this);
        this.withOutInventory.on("check",function(){
            this.isLinksClosed = this.inventoryPreferencesForm.getForm().getValues().withInventory==undefined;
            Wtf.getCmp('move-next').setDisabled(this.isLinksClosed);
        },this);


        //------------------- Card 7 <Set Tax1099 Preferences>---------------------------//
        this.tax1099PreferencesForm = new Wtf.form.FormPanel({
            border: false,
            labelWidth:20,
            items : new Wtf.form.FieldSet({
                title: WtfGlobal.getLocaleText("acc.setupWizard.question3"),  //'Do you want to track Tax 1099 in Deskera Accounting',
                autoHeight: true,
                cls: "wiz-card6-fieldset",
                border: false,
                defaultType: 'radio',
                items: [
                this.withTax1099= new Wtf.form.Radio({
//                    checked: true,
                    fieldLabel: '',
                    labelSeparator: '',
                    checked: true,
                    boxLabel: WtfGlobal.getLocaleText("acc.setupWizard.note16"),  //"Yes, I want to use Deskera Accounting <b>with</b> Tax 1099.",
                    name: 'withTax1099',
                    inputValue: "Yes"
                }),
                this.withOutTax1099= new Wtf.form.Radio({
                    ctCls:"fieldset-item",
                    fieldLabel: '',
//                    checked: true,
                    labelSeparator: '',
                    boxLabel: WtfGlobal.getLocaleText("acc.setupWizard.note17"),  //"No, I want to use Deskera Accounting <b>without</b> Tax 1099.",
                    name: 'withTax1099',
                    inputValue: "No"
                })]
            })
        });
        this.withTax1099.on("check",function(){
            this.isLinksClosed = this.tax1099PreferencesForm.getForm().getValues().withTax1099==undefined;
            Wtf.getCmp('move-next').setDisabled(this.isLinksClosed);
        },this);
        this.withOutTax1099.on("check",function(){
            this.isLinksClosed = this.tax1099PreferencesForm.getForm().getValues().withTax1099==undefined;
            Wtf.getCmp('move-next').setDisabled(this.isLinksClosed);
        },this);
    },

    getBeginingYearDate: function(v,m,rec){
        var year =  Wtf.serverDate?Wtf.serverDate.getFullYear():new Date().getFullYear();
        var fmonth = (this.fmonth.getValue()==0 || this.fmonth.getValue()!="")?this.fmonth.getValue():new Date().getMonth();
        var fday = (this.fdays.getValue()==0 || this.fdays.getValue()!="")?this.fdays.getValue():new Date().getDay();
        var isvaliddate= this.checkdate(fday, fmonth, year);
        if(!isvaliddate){
            this.showMessage(WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.setupWizard.note31"));
            return false;
        }
        this.FinancialStartDate = new Date(year,fmonth,fday);
        return true;
    },

    getBookBeginingDate: function(v,m,rec){
        var year = Wtf.serverDate?Wtf.serverDate.getFullYear():new Date().getFullYear();
        if(this.bmonth.getValue()<this.fmonth.getValue()
                ||(this.bmonth.getValue()==this.fmonth.getValue()&&this.bdays.getValue()<this.fdays.getValue())){
             year++;
        }

        var bmonth = (this.bmonth.getValue()==0 || this.bmonth.getValue()!="")?this.bmonth.getValue():new Date().getMonth();
        var bday = (this.bdays.getValue()==0 || this.bdays.getValue()!="")?this.bdays.getValue():new Date().getDay();
        var isvaliddate= this.checkdate(bday, bmonth, year);
        if(!isvaliddate){
            this.showMessage(WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.setupWizard.note32"));
            return false;
        }
        this.BookStartDate = new Date(year,bmonth,bday);
        return true;
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

    getCurrencyDetails: function(){
        var currencyDatails = "";
        this.currencyStore.clearFilter();
        for(var i=0; i<this.currencyStore.getCount(); i++){
            var rec = this.currencyStore.getAt(i);
            currencyDatails += "{erid:\""+rec.data.id+"\"," +
                            "tocurrency:\""+rec.data.tocurrency+"\"," +
                            "tocurrencyid:\""+rec.data.tocurrencyid+"\","+
                            "fromcurrencyid:\""+rec.data.fromcurrencyid+"\","+
                            "exchangerate:\""+encodeURI(rec.data.exchangerate)+"\","+
                            "applydate:\""+WtfGlobal.convertToGenericDate(rec.data.applydate.clearTime())+"\"},";
        }
        if(currencyDatails.length>0) {
            currencyDatails = currencyDatails.substring(0, currencyDatails.length-1);
        }
        currencyDatails = "["+currencyDatails+"]"
        // Filter
        this.currencyStore.filterBy(function(rec){
            if(rec.data.tocurrencyid==rec.data.fromcurrencyid)
                return false
            else
                return true
        },this);
        return currencyDatails;
    },

    getTaxDetails: function(){
        var taxDatails = "";
        for(var i=0; i<this.taxStore.getCount(); i++){
            var rec = this.taxStore.getAt(i);
            taxDatails += "{name:\""+encodeURI(rec.data.taxname)+"\"," +
                            "code:\""+encodeURI(rec.data.taxcode)+"\"," +
                            "percent:\""+rec.data.percent+"\","+
                            "applydate:\""+WtfGlobal.convertToGenericDate(rec.data.applydate.clearTime())+"\"},";
        }
        if(taxDatails.length>0) {
            taxDatails = taxDatails.substring(0, taxDatails.length-1);
        }
        taxDatails = "["+taxDatails+"]"
        return taxDatails;
    },

    getBankDetails: function(){
        var bankDatails = "";
        for(var i=0; i<this.bankStore.getCount(); i++){
            var rec = this.bankStore.getAt(i);
            bankDatails += "{name:\""+encodeURI(rec.data.bankname)+"\"," +
                            "accountname:\""+encodeURI(rec.data.accountname)+"\"," +
                            "no:\""+rec.data.accountno+"\"," +
                            "balance:\""+encodeURI(rec.data.balance)+"\","+
                            "applydate:\""+WtfGlobal.convertToGenericDate(rec.data.applydate.clearTime())+"\"},";
        }
        if(bankDatails.length>0) {
            bankDatails = bankDatails.substring(0, bankDatails.length-1);
        }
        bankDatails = "["+bankDatails+"]"
        return bankDatails;
    },

    setLockYearValues:function(){
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

    getLockYearDetails: function(){
//        this.setLockYearValues();
        var lockDatails = "";
        for(var i=0; i<this.lockds.getCount(); i++){
            var rec = this.lockds.getAt(i);
            lockDatails += "{id:\""+rec.data.id+"\"," +
                            "islock:\""+rec.data.islock+"\","+
                            "name:\""+rec.data.name+"\"},";
        }
        if(lockDatails.length>0) {
            lockDatails = lockDatails.substring(0, lockDatails.length-1);
        }
        lockDatails = "["+lockDatails+"]"
        return lockDatails;
    },

    getSetUpData: function() {
        var setUpData = "";
        var defaultIndex = this.typeStore.find("id", this.companyTypeID);
        
        setUpData += "countryid:\""+this.country.getValue()+"\",";
        setUpData += "currencyid:\""+this.currency.getValue()+"\",";
        setUpData += "companyTypeId:\""+this.companyTypeID+"\",";
        setUpData += "companyTypeId:\""+this.companyTypeID+"\",";
        setUpData += "companyType:\""+this.typeStore.getAt(defaultIndex).data.name+"\",";
        setUpData += "addDefaultAccount:\""+this.defaultAccountForm.getForm().getValues().addDefaultAccount+"\",";
        setUpData += "currencyDetails:"+this.getCurrencyDetails()+",";

        setUpData += "yearStartDate:\""+WtfGlobal.convertToGenericDate(this.FinancialStartDate)+"\",";
        setUpData += "bookStartDate:\""+WtfGlobal.convertToGenericDate(this.BookStartDate)+"\",";

        setUpData += "lockDetails:"+this.getLockYearDetails()+",";
        setUpData += "taxDetails:"+this.getTaxDetails()+",";
        setUpData += "bankDetails:"+this.getBankDetails()+",";
        setUpData += "withInventory:\""+this.inventoryPreferencesForm.getForm().getValues().withInventory+"\",";
        
        if(document.getElementById("accWizardLinkImage7").className.indexOf("img-visited") != "-1")
        	setUpData += "withTax1099:"+this.tax1099PreferencesForm.getForm().getValues().withTax1099;
        else
        	setUpData += "withTax1099:false";

        setUpData = "{"+setUpData+"}";
        return setUpData;
    },

    getPreviewTemplate: function() {
        var defaultIndex = this.typeStore.find("id", this.companyTypeID);
        var previwHTML =
            "<div style='padding:10px;font-size:11px;'>"+
                "<div>"+WtfGlobal.getLocaleText("acc.setupWizard.note13")+"</div><br/>"+
                "<div>"+WtfGlobal.getLocaleText("acc.setupWizard.note14")+"</div><br/>"+ "</div>"

                previwHTML += "<div class='accSWdata'><span style='float:left;width:170px'><b>"+WtfGlobal.getLocaleText("acc.rem.213")+"</b></span>: "+Wtf.countryStore.getAt(Wtf.countryStore.find("id", this.country.getValue())).data.name+"</div>";
                previwHTML += "<div class='accSWdata'><span style='float:left;width:170px'><b>"+WtfGlobal.getLocaleText("acc.rem.214")+"</b></span>: "+this.currencyrecStore.getAt(this.currencyrecStore.find("currencyid", this.currency.getValue())).data.name+"</div>";

                previwHTML += "<div class='accSWdata'><span style='float:left;width:170px'><b>"+WtfGlobal.getLocaleText("acc.setupWizard.FinYrDate")+"</b></span>: "+this.FinancialStartDate.format(WtfGlobal.getOnlyDateFormat())+"</div>";
                previwHTML += "<div class='accSWdata'><span style='float:left;width:170px'><b>"+WtfGlobal.getLocaleText("acc.setupWizard.BookBeginingDate")+"</b></span>: "+this.BookStartDate.format(WtfGlobal.getOnlyDateFormat())+"</div>";
                
                previwHTML += "<div class='accSWdata'><span style='float:left;width:170px'><b>"+WtfGlobal.getLocaleText("acc.setupWizard.indType")+"</b></span>: "+this.typeStore.getAt(defaultIndex).data.name+"</div>";

                previwHTML += "<div class='accSWdata'><span style='float:left;width:170px'><b>"+WtfGlobal.getLocaleText("acc.setupWizard.note33")+"</b></span>: ";
                if(this.defaultAccountForm.getForm().getValues().addDefaultAccount=="Yes"){
                    previwHTML += WtfGlobal.getLocaleText("acc.setupWizard.added");  //"Added";
                } else {
                    previwHTML += WtfGlobal.getLocaleText("acc.setupWizard.notAdded");  //"Not Added";
                }
                previwHTML += "</div>";

                previwHTML += "<div class='accSWdata'><span style='float:left;width:170px'><b>"+WtfGlobal.getLocaleText("acc.setupWizard.invPref")+"</b></span>: ";
                    if(this.inventoryPreferencesForm.getForm().getValues().withInventory=="Yes"){
                        previwHTML += WtfGlobal.getLocaleText("acc.setupWizard.note34");  //"With Inventory";
                    } else {
                        previwHTML += WtfGlobal.getLocaleText("acc.setupWizard.note35");  //"Without Inventory";
                    }
                    previwHTML += "</div>";
                    
                if(document.getElementById("accWizardLinkImage7").className.indexOf("img-visited") != "-1") {    
                    previwHTML += "<div class='accSWdata'><span style='float:left;width:170px'><b>"+WtfGlobal.getLocaleText("acc.setupWizard.tax1099Pref")+"</b></span>: ";
                    if(this.tax1099PreferencesForm.getForm().getValues().withTax1099=="Yes"){
                        previwHTML += WtfGlobal.getLocaleText("acc.setupWizard.note36");  //"With Tax 1099";
                    } else {
                        previwHTML += WtfGlobal.getLocaleText("acc.setupWizard.note37");  //"Without Tax 1099";
                    }
                    previwHTML += "</div>";
                }

var rowStyle = "width: 20%; display: block; float: left; overflow:hidden; padding-left:3px;";
var rowHeaderStyle = rowStyle+" border-bottom: 1px solid #E8E8E8;";

                var currencyDatails = "";
                if(this.currencyStore.getCount()>0) {
                    currencyDatails = "<span style='"+rowHeaderStyle+" width: 10px;'>&nbsp;</span>" +
                                    "<span style='"+rowHeaderStyle+" width: 40px;'>"+WtfGlobal.getLocaleText("acc.setupWizard.sno")+"</span>" +
                                    "<span style='"+rowHeaderStyle+"'>"+WtfGlobal.getLocaleText("acc.setupWizard.cur")+"</span>"+
                                    "<span style='"+rowHeaderStyle+"'>"+WtfGlobal.getLocaleText("acc.setupWizard.curEx")+"</span>"+
                                    "<span style='"+rowHeaderStyle+" width:30%'>"+WtfGlobal.getLocaleText("acc.setupWizard.conv")+"</span>"+
                                    "<span style='"+rowHeaderStyle+" width:15%'>"+WtfGlobal.getLocaleText("acc.setupWizard.appDate")+"</span>"+
                                    "<br style='clear:both'/>";

                    for(var i=0; i<this.currencyStore.getCount(); i++){
                        var rec = this.currencyStore.getAt(i);
                        currencyDatails += "<span style='"+rowStyle+" width: 10px;'>&nbsp;</span>" +
                                        "<span style='"+rowStyle+" width: 40px;'>"+(i+1)+".&nbsp;</span>" +
                                        "<span style='"+rowStyle+"'>"+rec.data.tocurrency+"&nbsp;</span>" +
                                        "<span style='"+rowStyle+"'>"+rec.data.exchangerate+"&nbsp;</span>" +
                                        "<span style='"+rowStyle+" width:30%'>"+"1 "+rec.data.fromcurrency+" = "+rec.data.exchangerate+" "+rec.data.tocurrency+"&nbsp;</span>" +
                                        "<span style='"+rowStyle+" width:15%'>"+(rec.data.applydate).format(WtfGlobal.getOnlyDateFormat())+"&nbsp;</span>"+
                                        "<br style='clear:both'/>" ;
                    }
                    currencyDatails = "<br/><div style='padding-left:40px'>"+currencyDatails+"</div>";
                } else {
                    currencyDatails = " "+WtfGlobal.getLocaleText("acc.setupWizard.note12");
                }
                previwHTML += "<div class='accSWdata'><span style='float:left;width:170px'><b>"+WtfGlobal.getLocaleText("acc.setupWizard.surrexrates")+"</b></span>: "+currencyDatails+"</div>";

                var taxDatails = "";
                if(this.taxStore.getCount()>0) {
                    taxDatails = "<span style='"+rowHeaderStyle+" width: 10px;'>&nbsp;</span>" +
                                    "<span style='"+rowHeaderStyle+" width: 40px;'>"+WtfGlobal.getLocaleText("acc.setupWizard.sno")+"</span>" +
                                    "<span style='"+rowHeaderStyle+"'>"+WtfGlobal.getLocaleText("acc.setupWizard.taxnam")+"</span>"+
                                    "<span style='"+rowHeaderStyle+"'>"+WtfGlobal.getLocaleText("acc.setupWizard.taxcod")+"</span>"+
                                    "<span style='"+rowHeaderStyle+"'>"+WtfGlobal.getLocaleText("acc.setupWizard.per")+"</span>"+
                                    "<span style='"+rowHeaderStyle+"'>"+WtfGlobal.getLocaleText("acc.setupWizard.appDate")+"</span>"+
                                    "<br style='clear:both'/>";

                    for(i=0; i<this.taxStore.getCount(); i++){
                        rec = this.taxStore.getAt(i);
                        taxDatails += "<span style='"+rowStyle+" width: 10px;'>&nbsp;</span>" +
                                        "<span style='"+rowStyle+" width: 40px;'>"+(i+1)+".&nbsp;</span>" +
                                        "<span style='"+rowStyle+"'>"+rec.data.taxname+"&nbsp;</span>" +
                                        "<span style='"+rowStyle+"'>"+rec.data.taxcode+"&nbsp;</span>" +
                                        "<span style='"+rowStyle+"'>"+rec.data.percent+"%&nbsp;</span>" +
                                        "<span style='"+rowStyle+"'>"+(rec.data.applydate).format(WtfGlobal.getOnlyDateFormat())+"&nbsp;</span>"+
                                        "<br style='clear:both'/>" ;
                    }
                    taxDatails = "<br/><div style='padding-left:40px'>"+taxDatails+"</div>";
                } else {
                    taxDatails = WtfGlobal.getLocaleText("acc.setupWizard.note12");  //" Details not provided, can be entered later.";
                }
                previwHTML += "<div class='accSWdata'><span style='float:left;width:170px'><b>"+WtfGlobal.getLocaleText("acc.setupWizard.taxDetails")+"</b></span>: "+taxDatails+"</div>";

                var bankDatails = "";
                if(this.bankStore.getCount()>0) {
                    bankDatails = "<span style='"+rowHeaderStyle+" width: 10px;'>&nbsp;</span>" +
                                    "<span style='"+rowHeaderStyle+" width: 40px;'>"+WtfGlobal.getLocaleText("acc.cnList.Sno")+"</span>" +
                                    "<span style='"+rowHeaderStyle+"'>"+WtfGlobal.getLocaleText("acc.setupWizard.BankNam")+"</span>"+
                                    "<span style='"+rowHeaderStyle+"'>"+WtfGlobal.getLocaleText("acc.setupWizard.AccountNam")+"</span>"+
                                    "<span style='"+rowHeaderStyle+"'>"+WtfGlobal.getLocaleText("acc.setupWizard.openBal")+"</span>"+
                                    "<span style='"+rowHeaderStyle+"'>"+WtfGlobal.getLocaleText("acc.setupWizard.appDate")+"</span>"+
                                    "<br style='clear:both'/>";

                    for(i=0; i<this.bankStore.getCount(); i++){
                        rec = this.bankStore.getAt(i);
                        bankDatails += "<span style='"+rowStyle+" width: 10px;'>&nbsp;</span>" +
                                        "<span style='"+rowStyle+" width: 40px;'>"+(i+1)+".&nbsp;</span>" +
                                        "<span style='"+rowStyle+"'>"+rec.data.bankname+"&nbsp;</span>" +
                                        "<span style='"+rowStyle+"'>"+rec.data.accountno+"&nbsp;</span>" +
                                        "<span style='"+rowStyle+"'>"+rec.data.balance+"&nbsp;</span>" +
                                        "<span style='"+rowStyle+"'>"+(rec.data.applydate).format(WtfGlobal.getOnlyDateFormat())+"&nbsp;</span>"+
                                        "<br style='clear:both'/>" ;
                    }
                    bankDatails = "<br/><div style='padding-left:40px'>"+bankDatails+"</div>";
                } else {
                    bankDatails = WtfGlobal.getLocaleText("acc.setupWizard.note12");  //" Details not provided, can be entered later.";
                }
                previwHTML += "<div class='accSWdata'><span style='float:left;width:170px'><b>"+WtfGlobal.getLocaleText("acc.setupWizard.bankDetails")+"</b></span>: "+bankDatails+"</div>";
        previwHTML += "</div>";

        return previwHTML;
    }    	

});
