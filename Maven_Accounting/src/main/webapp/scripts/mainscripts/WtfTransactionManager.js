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
function callSystemAdmin(){
    var panel = Wtf.getCmp("systemadmin");
    if(panel==null){
        panel = new Wtf.common.SystemAdmin({
            title : "Companies",
            layout : 'fit',
            id:'systemadmin',
            closable:true,
            iconCls:'systemadmin',
             border:false
        });
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
}
function callSetUpWizard(){
    new Wtf.Window({
        title: WtfGlobal.getLocaleText("acc.setupWizard.tabTitle"), //"Getting Started Wizard",
        id : 'welcomeSetUpWizard',
        closable: false,
        iconCls:'accountingbase',
        modal: true,
        width: 690,
        height: 280,
        resizable: false,
        buttonAlign: "right",
        renderTo: document.body,
        html:"<div style='font-size:12px;padding:50px;'>"+
        		WtfGlobal.getLocaleText("acc.setupWizard.dear")+" "+(_fullName.split(" ")[0])+",<br/><br/>"+
        		WtfGlobal.getLocaleText("acc.setupWizard.text1")+"<br/><br/>"+
        		WtfGlobal.getLocaleText("acc.setupWizard.text2")+
            "</div>",
        buttons: [
            {
                text: WtfGlobal.getLocaleText("acc.setupWizard.msg2"),  //"Skip Setup",
                handler: function(){
                    Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.setupWizard.tabTitle"), WtfGlobal.getLocaleText("acc.setupWizard.msg1"),function(btn){
                            if(btn=="yes") {
                                var setUpData = "companyTypeId:\"defaultaccount\",addDefaultAccount:\"Yes\",withInventory:\"Yes\",";
                                setUpData += "yearStartDate:\""+WtfGlobal.convertToGenericDate(Wtf.account.companyAccountPref.fyfrom)+"\",";
                                setUpData += "bookStartDate:\""+WtfGlobal.convertToGenericDate(Wtf.account.companyAccountPref.bbfrom)+"\",";
                                setUpData += "countryid:\""+Wtf.account.companyAccountPref.countryid+"\",";
                                setUpData += "currencyid:\""+Wtf.pref.Currencyid+"\",";
                                setUpData += "currencyDetails:[],taxDetails:[],bankDetails:[],lockDetails:[],withTax1099:\"No\"";
                                setUpData = "{"+setUpData+"}";

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
                                        Wtf.getCmp("tabdashboard").load({
                                            url:"ACCDashboard/getDashboardData.do",
                                            params:{refresh:false},
                                            scripts: true
                                        });
                                        getCompanyAccPref();
                                    }
                                    Wtf.Ajax.timeout = 30000;
                                },
                                function(response){
                                    Wtf.Ajax.timeout = 30000;
                                });

                                Wtf.getCmp("welcomeSetUpWizard").close();
                            }
                        });
                }
            },
            {
                text: WtfGlobal.getLocaleText("acc.common.continueBtn"),  //"Continue",
                handler: function(){
                    callSetUpWizardWindow();
                    Wtf.getCmp("welcomeSetUpWizard").close();
                }
            }
        ]
    }).show();
    return;
}

function callSetUpWizardWindow(){
    var panel = Wtf.getCmp("SetUpWizard");
    if(!panel){
        new Wtf.account.setUpWizard({
            id : 'SetUpWizard',
            border : false,
//            closeAction:'hide',
            layout: 'fit',
            title:WtfGlobal.getLocaleText("acc.setupWizard.tabTitle"), //'Getting Started Wizard',
            closable: false,
            iconCls:'accountingbase',
            modal: true,
            width: 800,
            height: 600,
            resizable: false,
            buttonAlign: "right",
            renderTo: document.body
        }).show();
        Wtf.getCmp("SetUpWizard").on("setup",function(win){
            Wtf.getCmp("tabdashboard").load({
                url:"ACCDashboard/getDashboardData.do",
                params:{refresh:false},
                scripts: true
            });
            getCompanyAccPref();
        },this);
    } else {
        panel.show();
    }
}
function callAccountPref(){
    var panel = Wtf.getCmp("companyAccPrefWin");
    if(panel==null){
        panel = new Wtf.account.CompanyAccountPreferences({
            id : 'companyAccPrefWin',
            border : false,
            layout: 'fit',
            title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.dashboard.accountPreferences"),Wtf.TAB_TITLE_LENGTH),
            tabTip:WtfGlobal.getLocaleText("acc.accPref.tabTitleTT"),  //'You can set your Account Preferences here related to Financial Year Settings, Close Books, Account Settings, Inventory Settings and Automatic Number Generation',
            helpmodeid:32,
            closable: true,
            iconCls:'accountingbase'
        });
        Wtf.getCmp('as').add(panel);
        panel.on("activate", function(){
            panel.doLayout();
        }, this);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
}
function callFixedAsset(){
    var panel = Wtf.getCmp("fixedAsset");
    if(panel==null){
        panel = new Wtf.TabPanel({
            id : 'fixedAsset',
            title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.fixedAssetList.tabTitle"),Wtf.TAB_TITLE_LENGTH),
            tabTip:WtfGlobal.getLocaleText("acc.fixedAssetList.tabTitleTT"),  //'You can Manage your Fixed Assets from here. like Add a Fixed Asset, Sell off, Write off and Post Depreciation etc.',
            closable: true,
            iconCls:'accountingbase coa',
            activeTab:0
        });
        Wtf.getCmp('as').add(panel);
        callFixedAssetReport();
        callFixedAssetByCategoryReport();
    }
    Wtf.getCmp('as').setActiveTab(panel);
    panel.setActiveTab(Wtf.getCmp('fixedAssetReport'));
    Wtf.getCmp('as').doLayout();
}

function callFixedAssetReport(){
     var panel = Wtf.getCmp("fixedAssetReport");
     if(panel==null){
        callFixedAsset();
        panel = new Wtf.account.COAReport({
            id : "fixedAssetReport",
            border : false,
            isFixedAsset:true,
            layout: 'fit',
            title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.fixedAssetList.faReport"),Wtf.TAB_TITLE_LENGTH),
            tabTip:WtfGlobal.getLocaleText("acc.fixedAssetList.faReportTT"),  //'Fixed Asset Report',
            iconCls:'accountingbase coa'
        });
        Wtf.getCmp("fixedAsset").add(panel);
    }
    Wtf.getCmp("fixedAsset").setActiveTab(panel);
    Wtf.getCmp("fixedAsset").doLayout();
}
function callFixedAssetByCategoryReport(){
     var panel = Wtf.getCmp("fixedAssetByCategoryReport");
     if(panel==null){
        panel = new Wtf.account.AccountListByCategory({
            id : "fixedAssetByCategoryReport",
            border : false,
            isFixedAsset:true,
            layout: 'fit',
            title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.fixedAssetList.faReportbyCategory"),Wtf.TAB_TITLE_LENGTH),
            tabTip:WtfGlobal.getLocaleText("acc.fixedAssetList.faReportbyCategoryTT"), //'Fixed Asset Report By Category',
            iconCls:'accountingbase coa'
        });
        Wtf.getCmp("fixedAsset").add(panel);
    }
    Wtf.getCmp("fixedAsset").doLayout();
}
function callCOA(){
   if(!WtfGlobal.EnableDisable(Wtf.UPerm.coa, Wtf.Perm.coa.view)) {
    var panel = Wtf.getCmp("coa");
    if(panel==null){
        panel = new Wtf.TabPanel({
            id : 'coa',
            title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.coa.tabTitle"),Wtf.TAB_TITLE_LENGTH),
            tabTip:WtfGlobal.getLocaleText("acc.coa.tabTitleTT"), //'Chart of Accounts',
            closable: true,
            iconCls:'accountingbase coa',
            activeTab:0
        });
        Wtf.getCmp('as').add(panel);
        callCOAReport();
        callGroupReport();
    }
    Wtf.getCmp('as').setActiveTab(panel);
    panel.setActiveTab(Wtf.getCmp('coaReport'));
    Wtf.getCmp('as').doLayout();
   }
   else
        WtfComMsgBox(46,0,false,"creating Chart of Accounts");
}

function callCOAReport(){
     var panel = Wtf.getCmp("coaReport");
    if(panel==null){
        callCOA();
        panel = new Wtf.account.COAReport({
            id : 'coaReport',
            border : false,
            layout: 'fit',
            title:WtfGlobal.getLocaleText("acc.coa.coaReporttabTitle"),  //'   COA Report',
            iconCls:'accountingbase coa'
        });
        Wtf.getCmp('coa').add(panel);
    }
    Wtf.getCmp('coa').setActiveTab(panel);
    Wtf.getCmp('coa').doLayout();
}

function callCOAWindow(isEdit, rec, winid,issales,ispurchase,islibility,isotherexpense,cashbank,incomenature,isexpense){
    winid=(winid==null?"coaWin":winid);
    var panel = Wtf.getCmp(winid);
    if(!panel){
        new Wtf.account.COA({
            cashbank:cashbank,
            incomenature:incomenature,
            record:rec,
            isEdit:isEdit,
            title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.coa.tabTitle"),Wtf.TAB_TITLE_LENGTH),
            tabTip:WtfGlobal.getLocaleText("acc.coa.tabTitleTT"),  //'Chart Of Accounts',
            id:winid,
            closable: true,
            islibility:islibility,
            isexpense:isexpense,
            isotherexpense:isotherexpense,
            ispurchase:ispurchase,
            issales:issales,
            modal: true,
            iconCls :getButtonIconCls(Wtf.etype.deskera),
            width: 530,
            height:400,
            resizable: false,
            layout: 'border',
            buttonAlign: 'right',
            renderTo: document.body
        }).show();
        this.loadMask1 = new Wtf.LoadMask(winid, {msg: WtfGlobal.getLocaleText("acc.msgbox.50"), msgCls: "x-mask-loading acc-customer-form-mask"});
        this.loadMask1.show();
        Wtf.getCmp(winid).on("loadingcomplete",function(){this.loadMask1.hide()},this);
    }


}
function callFixedAssetWindow(isEdit, rec, winid){
    winid=(winid==null?"fixedAssetWin":winid);
    var panel = Wtf.getCmp(winid);
    if(!panel){
        new Wtf.account.COA({
            isEdit:isEdit,
            record:rec,
            isFixedAsset:true,
            title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.fixedAssetList.tabTitle"),Wtf.TAB_TITLE_LENGTH),
            tabTip:WtfGlobal.getLocaleText("acc.fixedAssetList.tabTitleTT"),  //'Fixed Asset',
            id:winid,
            closable: true,
            modal: true,
            iconCls :getButtonIconCls(Wtf.etype.deskera),
            width: 530,
            height:570,
            resizable: false,
            layout: 'border',
            buttonAlign: 'right',
            renderTo: document.body
        }).show();
        this.loadMask1 = new Wtf.LoadMask(winid, {msg: WtfGlobal.getLocaleText("acc.msgbox.50"), msgCls: "x-mask-loading acc-customer-form-mask"});
        this.loadMask1.show();
        Wtf.getCmp(winid).on("loadingcomplete",function(){this.loadMask1.hide()},this);
    }
}
function callFixedAssetRemove(winid, rec, isWriteOff){
    winid=(winid==null?"fixedAssetRemove":winid);
    var panel = Wtf.getCmp(winid);
    if(!panel){
        new Wtf.assetremove({
            record:rec,
            isWriteOff: isWriteOff,
            title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.fixedAssetList.removeAsset"),Wtf.TAB_TITLE_LENGTH),
            tabTip:WtfGlobal.getLocaleText("acc.fixedAssetList.removeAsset"),  //'Remove Fixed Asset',
            id:winid,
            closable: true,
            modal: true,
            iconCls :getButtonIconCls(Wtf.etype.deskera),
            width: 400,
            height:350,
            resizable: false,
//            layout: 'border',
//            buttonAlign: 'right',
            renderTo: document.body
        }).show();
        this.loadMask1 = new Wtf.LoadMask(winid, {msg: WtfGlobal.getLocaleText("acc.msgbox.50"), msgCls: "x-mask-loading acc-customer-form-mask"});
//        this.loadMask1.show();
        Wtf.getCmp(winid).on("loadingcomplete",function(){alert("load mask close");this.loadMask1.hide();},this);
    }
}
function callDepreciationReport(winid,accid,rec){
    winid=(winid==null?"depereationWin":winid);
    var panel = Wtf.getCmp(winid);
    if(!panel){
        panel=new Wtf.account.COAReport({
            record:rec,
            border : false,
            accid:accid,
            isDepreciation:true,
            layout: 'fit',
            title:Wtf.util.Format.ellipsis(rec.data.accname+' '+WtfGlobal.getLocaleText("acc.fixedAssetList.depDet"),Wtf.TAB_TITLE_LENGTH),
            tabTip:rec.data.accname+' '+WtfGlobal.getLocaleText("acc.fixedAssetList.depDet"),  //Depreciation Details',
            id:winid,
            closable: true,
            iconCls :'accountingbase debitnotereport'
        });

        Wtf.getCmp("fixedAsset").add(panel);
    }
     Wtf.getCmp("fixedAsset").setActiveTab(panel);
     Wtf.getCmp("fixedAsset").doLayout();
}
function callProductType(winid){
    winid=(winid==null?"productTypeWin":winid);
    var p = Wtf.getCmp(winid);
    if(!p){
        var cm= [{
            header: "Name",
            dataIndex: 'name',
            editor: new Wtf.form.TextField({
                allowBlank: false,
                maxLength:50
            })
        }];
        this.typeRec = new Wtf.data.Record.create([
           {name: 'id'},
           {name: 'name'}
        ]);
        this.typeStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.typeRec),
//            url: Wtf.req.account + 'CompanyManager.jsp',
            url: "ACCProduct/getProductTypes.do",
            baseParams:{
                mode:24
            }
        });
        this.typeStore.load();
        new Wtf.account.GridUpdateWindow({
            cm:cm,
            headerImage:"../../images/accounting/Unit-of-measure.gif",
            store:this.typeStore,
            record:this.typeRec,
            mode:26,
            title:'Product Type',
            id:winid,
            renderTo: document.body
        }).show();
    }

}

function callUOM(winid){
    winid=(winid==null?"uomReportWin":winid);
    var p = Wtf.getCmp(winid);
    if(!p){
        var cm= [{
            header: WtfGlobal.getLocaleText("acc.masterConfig.uom.gridName"),  //"Name",
            dataIndex: 'uomname',
            editor: new Wtf.form.TextField({
                allowBlank: false,
                maxLength:50,
                regex:Wtf.specialChar
            })
        },{
            header: WtfGlobal.getLocaleText("acc.masterConfig.uom.gridAllowedPrecision"),  //"Allowed Precision",
            dataIndex: 'precision',
            renderer:WtfGlobal.numericRenderer,
            editor: new Wtf.form.NumberField({
                allowBlank: false,
                maxLength:5
            })
        },{
            header: WtfGlobal.getLocaleText("acc.masterConfig.uom.gridType"),  //"Type",
            dataIndex: 'uomtype',
            editor: new Wtf.form.TextField({
                allowBlank: false,
                maxLength:50,
                regex:Wtf.specialChar
            })
        }];
        this.uomRec = new Wtf.data.Record.create([
           {name: 'uomid'},
           {name: 'uomname'},
           {name: 'precision',type:'int'},
           {name: 'uomtype'}
        ]);
        this.uomStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.uomRec),
//            url: Wtf.req.account + 'CompanyManager.jsp',
            url: "ACCUoM/getUnitOfMeasure.do",
            baseParams:{
                mode:31
            }
        });
        this.uomStore.load();
        new Wtf.account.GridUpdateWindow({
            cm:cm,
            headerImage:"../../images/accounting_image/Unit-of-measure.gif",
            store:this.uomStore,
            record:this.uomRec,
            mode:32,
            title:WtfGlobal.getLocaleText("acc.masterConfig.uom"),  //'Unit of Measure',
            id:winid,
            renderTo: document.body
        }).show();
    }

}

function callTax(winid){
        var accRec=new Wtf.data.Record.create([
            {name: 'accountid',mapping:'accid'},
            {name: 'accountname',mapping:'accname'}
        ]);
        var accStore=new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },accRec),
//            url: Wtf.req.account+'CompanyManager.jsp',
            url : "ACCAccount/getAccountsForCombo.do",
            baseParams:{
                mode:2,
                group:[3],
                nondeleted:true
            }
        });
        accStore.load();
    winid=(winid==null?"TaxWindow":winid);
    var p = Wtf.getCmp(winid);
    if(!p){
       var record = new Wtf.data.Record.create([
           {name: 'taxid'},
           {name: 'taxname'},
           {name: 'percent',type:'float'},
           {name: 'taxcode'},
           {name: 'accountid'},
           {name: 'accountname'},
           {name: 'applydate', type:'date'}
       ]);

        var store = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },record),
//            url: Wtf.req.account + 'CompanyManager.jsp',
            url : "ACCTax/getTax.do",
            baseParams:{
                mode:33
            }
        });
        store.load();

        var cmbAccount= new Wtf.form.FnComboBox({
            hiddenName:'accountid',
            store:accStore,
            valueField:'accountid',
            displayField:'accountname',
            forceSelection:true,
            mode: 'local',
            disableKeyFilter:true,
            allowBlank:false,
            triggerAction:'all',
            hirarchical:true
//            addNewFn:function(){
//                callCOAWindow(false, null, "coaWin",false,false,true);
//                Wtf.getCmp("coaWin").on("update",function(){accStore.reload()},this);
//            }
        });
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.coa, Wtf.Perm.coa.create)){

            cmbAccount.addNewFn=function(){callCOAWindow(false, null, "coaWin",false,false,true);
            Wtf.getCmp("coaWin").on("update",function(){accStore.reload()},this);
        }
        }
         var cm;

        cm= [{
            header: WtfGlobal.getLocaleText("acc.masterConfig.taxes.gridName"),  //"Name",
            dataIndex: 'taxname',
            editor: new Wtf.form.TextField({
                allowBlank: false,
                maxLength:50,
                regex:Wtf.specialChar
            })
        },{
            header: WtfGlobal.getLocaleText("acc.masterConfig.taxes.gridPercent"),  //"Percent",
            dataIndex: 'percent',
            renderer:function(val){
                if(typeof val != "number") return "";
                return val+'%';
            },
            editor: new Wtf.form.NumberField({
                allowBlank: false,
                maxValue:100,
                allowNegative:false,
                maxLength:50
            })
        },{
            header: WtfGlobal.getLocaleText("acc.masterConfig.taxes.gridApplyDate"),  //"Apply Date",
            dataIndex: 'applydate',
            renderer:WtfGlobal.onlyDateRenderer,
            editor:new Wtf.form.DateField({
            	value: Wtf.serverDate.clearTime(true),
                name:'applydate',
                format:WtfGlobal.getOnlyDateFormat()
            })
        },{
            header: WtfGlobal.getLocaleText("acc.masterConfig.taxes.gridTaxCode"),  //"Tax Code",
            dataIndex:'taxcode',
            editor: new Wtf.form.TextField({
                allowBlank: false,
                maxLength:50,
                regex:Wtf.specialChar
            })
       },{
            header: WtfGlobal.getLocaleText("acc.masterConfig.taxes.gridAccountName"),  //"Account Name",
            dataIndex: 'accountid',
            renderer:Wtf.comboBoxRenderer(cmbAccount),
            editor:cmbAccount
        }];
      new Wtf.account.TaxWindow({
            cm:cm,
            headerImage:"../../images/accounting_image/tax.gif",
            store:store,
            record:record,
            istax:true,
            mode:34,
            title:WtfGlobal.getLocaleText("acc.tax.title"),  //'Taxes',
            id:winid,
            renderTo: document.body
        }).show();
    }

}

function callCostCenter(winid){
    winid=(winid==null?"CostCenterWin":winid);
    var p = Wtf.getCmp(winid);
    if(!p){
        var cm= [{
            header: WtfGlobal.getLocaleText("acc.masterConfig.costCenter.grid"),  //"Cost Center ID*",
            dataIndex: 'ccid',
            editor: new Wtf.form.ExtendedTextField({
                allowBlank: false,
                maxLength:50
            })
        },{
            header: WtfGlobal.getLocaleText("acc.masterConfig.costCenter.name"),  //"Name*",
            dataIndex: 'name',
            editor: new Wtf.form.ExtendedTextField({
                allowBlank: false,
                maxLength:50,
                regex:Wtf.specialChar
            })
        },{
            header: WtfGlobal.getLocaleText("acc.masterConfig.costCenter.desc1"),  //"Description*",
            dataIndex: 'description',
            editor: new Wtf.form.ExtendedTextField({
                maxLength:200
            })
        }];
        this.ccRec = new Wtf.data.Record.create([
           {name: 'id'},
           {name: 'ccid'},
           {name: 'name'},
           {name: 'description'}
        ]);
        this.ccStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.ccRec),
            url: "CostCenter/getCostCenter.do"
        });
        this.ccStore.load();
        new Wtf.account.GridUpdateWindow({
            cm:cm,
            headerImage:"../../images/accounting_image/Unit-of-measure.gif",
            store:this.ccStore,
            record:this.ccRec,
            mode:82,
            addDeleteCol: false,
            title:WtfGlobal.getLocaleText("acc.common.costCenter"),  //'Cost Center',
            id:winid,
            renderTo: document.body,
            addDeleteCol: true
        }).show();
    }
}

function callCreditTerm(winid){
    winid=(winid==null?"creditTermReportWin":winid);
    var p = Wtf.getCmp(winid);
    if(!p){
        var cm=[{
            header: WtfGlobal.getLocaleText("acc.masterConfig.payTerm.gridTermName"),  //"Term Name",
            dataIndex: 'termname',
            editor:new Wtf.form.TextField({allowBlank:false,maxLength:50,regex:Wtf.specialChar})
        },{
            header: WtfGlobal.getLocaleText("acc.masterConfig.payTerm.gridTermDays"),  //"Term Days",
            dataIndex: 'termdays',
            renderer: function(value){
                if(typeof value != "number") return value;
                if(value<=1)
                    value=value+" Day";
                else
                    value=value+" Days";
                return value;
            },
            editor:new Wtf.form.NumberField({allowBlank:false,allowNegative:false, maxValue:365})
        }];
           this.termRec = new Wtf.data.Record.create([
        {name: 'termid'},
        {name: 'termname'},
        {name: 'termdays'}
]);

    this.termds = new Wtf.data.Store({
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        },this.termRec),
//        url: Wtf.req.account + 'CompanyManager.jsp',
        url : "ACCTerm/getTerm.do",
        baseParams:{
            mode:91
        }
     });
     this.termds.load();
        new Wtf.account.GridUpdateWindow({
            mode:92,
            store:this.termds,
            headerImage:"../../images/accounting_image/Credit-Term.gif",
            record:Wtf.termRec,
            cm:cm,
            title:WtfGlobal.getLocaleText("acc.masterConfig.payTerm.details"),  //'Term Details',
            id:winid,
            renderTo: document.body
        }).show();
    }

}

function callGoodsReceipt(isEdit,rec,winid,vendorid){		// Neeraj
   if(!WtfGlobal.EnableDisable(Wtf.UPerm.vendorinvoice, Wtf.Perm.vendorinvoice.createvendorinvoice)) {
    winid=(winid==null?'goodsreceipt':winid);
    var panel = Wtf.getCmp(winid);
    if(panel==null){
        panel = new Wtf.account.TransactionPanel({
            DefaultVendor: vendorid,						// Neeraj
        	id : winid,
            isEdit:isEdit,
            isCustomer:false,
            record: rec,
            label:'Vendor Invoice',
            heplmodeid: 15,
            border : false,
//            layout: 'border',
            title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.accPref.autoVI"),Wtf.TAB_TITLE_LENGTH),
            tabTip:WtfGlobal.getLocaleText("acc.accPref.autoVI"),  //'Vendor Invoice',
            closable: true,
            iconCls:'accountingbase goodsreceipt'
        });
        panel.on("activate", function(){
            if(Wtf.isIE7) {
                var northHt=(Wtf.isIE?240:210);
                var southHt=(Wtf.isIE?210:150);
                Wtf.getCmp(winid + 'southEastPanel').setHeight(southHt);
                Wtf.getCmp(winid + 'southEastPanel').setWidth(650);
                panel.NorthForm.setHeight(northHt);
                panel.southPanel.setHeight(southHt);
                panel.on("afterlayout", function(panel, lay){if(Wtf.isIE7){panel.GridPanel.setSize(panel.getInnerWidth()-35,200);}},this);
            }              
            panel.doLayout();            
        }, this);
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
    if(Wtf.getCmp("pricewindow")!=undefined)
            Wtf.getCmp("pricewindow").on('update',function(){Wtf.getCmp(winid).Grid.loadPriceStore()},this);
   }
   else
        WtfComMsgBox(46,0,false,isEdit?WtfGlobal.getLocaleText("acc.common.editing"):WtfGlobal.getLocaleText("acc.common.creating") +" Vendor Invoice");
}


function callInvoice(isEdit,rec,winid){
   if(!WtfGlobal.EnableDisable(Wtf.UPerm.invoice, Wtf.Perm.invoice.createinvoice)) {
    winid=(winid==null?'Invoice':winid);
    var panel = Wtf.getCmp(winid);
    if(panel==null){
        panel = new Wtf.account.TransactionPanel({
            id : winid,
            isEdit:isEdit,
            isCustomer:true,
            record: rec,
            label:'Invoice',
            border : false,
            heplmodeid: 2, //This is help mode id
//            layout: 'border',
            title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.accPref.autoInvoice"),Wtf.TAB_TITLE_LENGTH),
            tabTip:WtfGlobal.getLocaleText("acc.accPref.autoInvoice"),  //'Invoice',
            closable: true,
            iconCls:'accountingbase invoice'
        });
        panel.on("activate", function(){
            if(Wtf.isIE7) {
//            var northHt=(this.isOrder?(Wtf.isIE?150:180):(Wtf.isIE?240:210));
                var northHt=(Wtf.isIE?260:230);
                var southHt=(Wtf.isIE?210:150);
                Wtf.getCmp(winid + 'southEastPanel').setHeight(southHt);
                Wtf.getCmp(winid + 'southEastPanel').setWidth(650);
                panel.NorthForm.setHeight(northHt);
                panel.southPanel.setHeight(southHt);
                panel.on("afterlayout", function(panel, lay){if(Wtf.isIE7) {panel.Grid.setSize(panel.getInnerWidth() - 18,200);}},this);
            }
            panel.doLayout();
        }, this);
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
     if(Wtf.getCmp("pricewindow")!=undefined)
            Wtf.getCmp("pricewindow").on('update',function(){Wtf.getCmp(winid).Grid.loadPriceStore()},this);
   }
   else
        WtfComMsgBox(46,0,false,isEdit?WtfGlobal.getLocaleText("acc.common.editing"):WtfGlobal.getLocaleText("acc.common.creating") +"  Invoice");
}
function callEditCashReceipt(rec,winid,copyInv){
    winid=(winid==null?'EditCashReceipt':winid);
    var panel = Wtf.getCmp(winid);
    var label="Cash Sales";
    if(panel==null){
        panel = new Wtf.account.TransactionPanel({
            id : winid,
            isCustomer:true,
            isEdit:true,
            readOnly:false,
            rec: rec,
            cash:true,
            record: rec,
            label:label,
            border : false,
            copyInv:copyInv,
//            layout: 'border',
            closable: true,
            title:Wtf.util.Format.ellipsis(((copyInv?WtfGlobal.getLocaleText("acc.wtfTrans.ccs"):WtfGlobal.getLocaleText("acc.wtfTrans.ecs"))+"-"+rec.data.billno),Wtf.TAB_TITLE_LENGTH),
            tabTip:(copyInv?WtfGlobal.getLocaleText("acc.wtfTrans.ccs"):WtfGlobal.getLocaleText("acc.wtfTrans.ecs"))+"-"+rec.data.billno,
            iconCls:'accountingbase editinvoice'
        });
        panel.on("activate", function(){
            if(Wtf.isIE7) {
                var northHt=(Wtf.isIE?240:210);
                var southHt=(Wtf.isIE?210:150);
                Wtf.getCmp(winid + 'southEastPanel').setHeight(southHt);
                Wtf.getCmp(winid + 'southEastPanel').setWidth(650);
                panel.NorthForm.setHeight(northHt);
                panel.southPanel.setHeight(southHt);
            }
            panel.doLayout();
        }, this);
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    panel.on('update',  function(){Wtf.getCmp("InvoiceList").loadStore();Wtf.getCmp('as').remove(panel);}, this);
    //panel.refreshView(rec);
   if(Wtf.getCmp("pricewindow")!=undefined)
            Wtf.getCmp("pricewindow").on('update',function(){Wtf.getCmp(winid).Grid.loadPriceStore()},this);
   Wtf.getCmp('as').doLayout();
}
function callEditInvoice(rec,winid,copyInv){
    winid=(winid==null?'EditInvoice':winid);
    var panel = Wtf.getCmp(winid);
     var label=' Invoice';
    if(panel==null){
        panel = new Wtf.account.TransactionPanel({
            id : winid,
            isCustomer:true,
            isEdit:true,
            readOnly:false,
            record: rec,
            cash:false,
            copyInv:copyInv,
            label:label,
            heplmodeid: 2, //This is help mode id
            border : false,
//            layout: 'border',
            closable: true,
            title:Wtf.util.Format.ellipsis(((copyInv?WtfGlobal.getLocaleText("acc.wtfTrans.ci"):WtfGlobal.getLocaleText("acc.wtfTrans.ei"))+"-"+rec.data.billno),Wtf.TAB_TITLE_LENGTH),
            tabTip:(copyInv?WtfGlobal.getLocaleText("acc.wtfTrans.ci"):WtfGlobal.getLocaleText("acc.wtfTrans.ei"))+"-"+rec.data.billno,
            iconCls:'accountingbase invoice'
        });
        panel.on("activate", function(){
            if(Wtf.isIE7) {
                var northHt=(Wtf.isIE?260:230);
                var southHt=(Wtf.isIE?210:150);
                Wtf.getCmp(winid + 'southEastPanel').setHeight(southHt);
                Wtf.getCmp(winid + 'southEastPanel').setWidth(650);
                panel.NorthForm.setHeight(northHt);
                panel.southPanel.setHeight(southHt);
                panel.on("afterlayout", function(panel, lay){if(Wtf.isIE7) {panel.Grid.setSize(panel.getInnerWidth() - 18,200);}},this);
            }
            panel.doLayout();
        }, this);
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    panel.on('update',  function(){Wtf.getCmp("InvoiceList").loadStore();Wtf.getCmp('as').remove(panel);}, this);
    if(Wtf.getCmp("pricewindow")!=undefined)
            Wtf.getCmp("pricewindow").on('update',function(){Wtf.getCmp(winid).Grid.loadPriceStore()},this);
    Wtf.getCmp('as').doLayout();

}

function callEdiCashPurchase(rec,winid,copyInv,isexpenseinv){
    winid=(winid==null?'EditGoodsReceipt':winid);
    var panel = Wtf.getCmp(winid);
     var label='Cash Purchase ';
    if(panel==null){
        panel = new Wtf.account.TransactionPanel({
            id : winid,
            isEdit:true,
            isCustomer:false,
            isExpenseInv:isexpenseinv,
            copyInv:copyInv,
            readOnly:false,
            record: rec,
            cash:true,
            label:label,
            border : false,
//            layout: 'border',
            closable: true,
            title:Wtf.util.Format.ellipsis(((copyInv?WtfGlobal.getLocaleText("acc.wtfTrans.ccp"):WtfGlobal.getLocaleText("acc.wtfTrans.ecp")+"-"+rec.data.billno)),Wtf.TAB_TITLE_LENGTH),
            tabTip:(copyInv?WtfGlobal.getLocaleText("acc.wtfTrans.ccp"):WtfGlobal.getLocaleText("acc.wtfTrans.ecp"))+"-"+rec.data.billno,
            iconCls:'accountingbase invoice'
        });
        panel.on("activate", function(){
            if(Wtf.isIE7) {
                var northHt=(Wtf.isIE?240:210);
                var southHt=(Wtf.isIE?210:150);
                Wtf.getCmp(winid + 'southEastPanel').setHeight(southHt);
                Wtf.getCmp(winid + 'southEastPanel').setWidth(650);
                panel.NorthForm.setHeight(northHt);
                panel.southPanel.setHeight(southHt);
            }
            panel.doLayout();
        }, this);
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    panel.on('update',  function(){Wtf.getCmp("GRList").loadStore();Wtf.getCmp('as').remove(panel);}, this);
    if(Wtf.getCmp("pricewindow")!=undefined)
        Wtf.getCmp("pricewindow").on('update',function(){Wtf.getCmp(winid).Grid.loadPriceStore()},this);
    Wtf.getCmp('as').doLayout();
}
function callEditGoodsReceipt(rec,winid,copyInv,isexpenseinv){
    winid=(winid==null?'EditGoodsReceipt':winid);
    var panel = Wtf.getCmp(winid);
     var label=' Vendor Invoice';
    if(panel==null){
        panel = new Wtf.account.TransactionPanel({
            id : winid,
            isEdit:true,
            isCustomer:false,
            readOnly:false,
            record: rec,
            cash:false,
            copyInv:copyInv,
            isExpenseInv:isexpenseinv,
            label:label,
            heplmodeid: 15,
            border : false,
//            layout: 'border',
            closable: true,
            title:Wtf.util.Format.ellipsis(((copyInv?WtfGlobal.getLocaleText("acc.wtfTrans.cvi"):WtfGlobal.getLocaleText("acc.wtfTrans.evi"))+"-"+rec.data.billno),Wtf.TAB_TITLE_LENGTH),
            tabTip:(copyInv?WtfGlobal.getLocaleText("acc.wtfTrans.cvi"):WtfGlobal.getLocaleText("acc.wtfTrans.evi"))+"-"+rec.data.billno,
            iconCls:'accountingbase invoice'
        });
        panel.on("activate", function(){
            if(Wtf.isIE7) {
                var northHt=(Wtf.isIE?240:210);
                var southHt=(Wtf.isIE?210:150);
                Wtf.getCmp(winid + 'southEastPanel').setHeight(southHt);
                Wtf.getCmp(winid + 'southEastPanel').setWidth(650);
                panel.NorthForm.setHeight(northHt);
                panel.southPanel.setHeight(southHt);
                panel.on("afterlayout", function(panel, lay){if(Wtf.isIE7) {panel.Grid.setSize(panel.getInnerWidth() - 18,200);}},this);
            }
            panel.doLayout();
        }, this);
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    if(!isexpenseinv){
       panel.on('update',  function(){Wtf.getCmp("GRList").loadStore();Wtf.getCmp('as').remove(panel);}, this);
       if(Wtf.getCmp("pricewindow")!=undefined)
          Wtf.getCmp("pricewindow").on('update',function(){Wtf.getCmp(winid).Grid.loadPriceStore()},this);
    }
    Wtf.getCmp('as').doLayout();
}
function callEditBillingInvoice(rec,winid,copyInv){
    winid=(winid==null?'EditBillingInvoice':winid);
    var panel = Wtf.getCmp(winid);
     var label=' Invoice';
    if(panel==null){
        panel = new Wtf.account.TransactionPanel({
            id : winid,
            isCustomer:true,
            isEdit:true,
            isCustBill:true,
            readOnly:false,
            record: rec,
            cash:false,
            copyInv:copyInv,
            heplmodeid: 2, //This is help mode id
            label:label,
            border : false,
//            layout: 'border',
            closable: true,
            title:Wtf.util.Format.ellipsis(((copyInv?WtfGlobal.getLocaleText("acc.wtfTrans.ci"):WtfGlobal.getLocaleText("acc.wtfTrans.ei"))+"-"+rec.data.billno),Wtf.TAB_TITLE_LENGTH),
            tabTip:(copyInv?WtfGlobal.getLocaleText("acc.wtfTrans.ci"):WtfGlobal.getLocaleText("acc.wtfTrans.ei"))+"-"+rec.data.billno,
            iconCls:'accountingbase invoice'
        });
        panel.on("activate", function(){
            if(Wtf.isIE7) {
                var northHt=(Wtf.isIE?260:230);
                var southHt=(Wtf.isIE?210:150);
                Wtf.getCmp(winid + 'southEastPanel').setHeight(southHt);
                Wtf.getCmp(winid + 'southEastPanel').setWidth(650);
                panel.NorthForm.setHeight(northHt);
                panel.southPanel.setHeight(southHt);
            }
            panel.doLayout();
        }, this);
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    panel.on('update',  function(){Wtf.getCmp("BillingInvoiceList").loadStore();Wtf.getCmp('as').remove(panel);}, this);
    Wtf.getCmp('as').doLayout();
}

function callEditBillingSalesReceipt(rec,winid,copyInv){
 //   rec=(rec==null?undefined:rec);
    winid=(winid==null?'BillingSalesReceipt':winid);
    var label='Sales Receipt';
    var panel = Wtf.getCmp(winid);
    if(panel==null){
        panel = new Wtf.account.TransactionPanel({
            id : winid,
            isEdit:true,
            isCustBill:true,
            copyInv:copyInv,
            cash:true,
            isCustomer:true,
            record: rec,
            heplmodeid: 8, //This is help mode id
            label:label,
            title:Wtf.util.Format.ellipsis(((copyInv?WtfGlobal.getLocaleText("acc.wtfTrans.bccs"):WtfGlobal.getLocaleText("acc.wtfTrans.becs"))+"-"+rec.data.billno),Wtf.TAB_TITLE_LENGTH),
            tabTip:(copyInv?WtfGlobal.getLocaleText("acc.wtfTrans.bccs"):WtfGlobal.getLocaleText("acc.wtfTrans.becs"))+"-"+rec.data.billno,
            iconCls:'accountingbase invoice'
        });
        panel.on("activate", function(){
            if(Wtf.isIE7) {
                var northHt=(Wtf.isIE?240:210);
                var southHt=(Wtf.isIE?210:150);
                Wtf.getCmp(winid + 'southEastPanel').setHeight(southHt);
                Wtf.getCmp(winid + 'southEastPanel').setWidth(650);
                panel.NorthForm.setHeight(northHt);
                panel.southPanel.setHeight(southHt);
            }
            panel.doLayout();
        }, this);
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    panel.on('update',  function(){Wtf.getCmp("BillingInvoiceList").loadStore();Wtf.getCmp('as').remove(panel);}, this);
    Wtf.getCmp('as').doLayout();
}
function callEditBillingGoodsReceipt(rec,winid,copyInv){
    winid=(winid==null?'BillingGoodsReceipt':winid);
    var label='Vendor Invoice';
    var panel = Wtf.getCmp(winid);
    if(panel==null){
        panel = new Wtf.account.TransactionPanel({
            id : winid,
            isEdit:true,
            isCustBill:true,
            copyInv:copyInv,
            isexpenseinv:rec.data.isexpenseinv,
            isCustomer:false,
            record: rec,
            heplmodeid: 15, //This is help mode id
            label:'Vendor Invoice',
            title:Wtf.util.Format.ellipsis(((copyInv?WtfGlobal.getLocaleText("acc.wtfTrans.cvi"):WtfGlobal.getLocaleText("acc.wtfTrans.evi"))+"-"+rec.data.billno),Wtf.TAB_TITLE_LENGTH),
            tabTip:(copyInv?WtfGlobal.getLocaleText("acc.wtfTrans.cvi"):WtfGlobal.getLocaleText("acc.wtfTrans.evi"))+"-"+rec.data.billno,
            closable: true,
            iconCls:'accountingbase invoice'
        });
        panel.on("activate", function(){
            if(Wtf.isIE7) {
                var northHt=(Wtf.isIE?240:210);
                var southHt=(Wtf.isIE?210:150);
                Wtf.getCmp(winid + 'southEastPanel').setHeight(southHt);
                Wtf.getCmp(winid + 'southEastPanel').setWidth(650);
                panel.NorthForm.setHeight(northHt);
                panel.southPanel.setHeight(southHt);
            }
            panel.doLayout();
        }, this);
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    panel.on('update',  function(){Wtf.getCmp("BillingGRList").loadStore();Wtf.getCmp('as').remove(panel);}, this);
    Wtf.getCmp('as').doLayout();
}
function callEditBillingPurchaseReceipt(rec,winid,copyInv){
    winid=(winid==null?'BillingPurchaseReceipt':winid);
    var label='Cash Purchase';
    var panel = Wtf.getCmp(winid);
    if(panel==null){
        panel = new Wtf.account.TransactionPanel({
            id : winid,
            isEdit:true,
            isCustBill:true,
            copyInv:copyInv,
            cash:true,
            isCustomer:false,
            record: rec,
            heplmodeid: 33, //This is help mode id
            label:'Purchase Receipt',
            title:Wtf.util.Format.ellipsis(((copyInv?WtfGlobal.getLocaleText("acc.wtfTrans.ccp"):WtfGlobal.getLocaleText("acc.wtfTrans.ecp"))+"-"+rec.data.billno),Wtf.TAB_TITLE_LENGTH),
            tabTip:(copyInv?WtfGlobal.getLocaleText("acc.wtfTrans.ccp"):WtfGlobal.getLocaleText("acc.wtfTrans.ecp"))+"-"+rec.data.billno,
            iconCls:'accountingbase invoice'
        });
        panel.on("activate", function(){
            if(Wtf.isIE7) {
                var northHt=(Wtf.isIE?240:210);
                var southHt=(Wtf.isIE?210:150);
                Wtf.getCmp(winid + 'southEastPanel').setHeight(southHt);
                Wtf.getCmp(winid + 'southEastPanel').setWidth(650);
                panel.NorthForm.setHeight(northHt);
                panel.southPanel.setHeight(southHt);
            }
            panel.doLayout();
        }, this);
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    panel.on('update',  function(){Wtf.getCmp("BillingGRList").loadStore();Wtf.getCmp('as').remove(panel);}, this);
    Wtf.getCmp('as').doLayout();
}
function callBillingInvoice(isEdit,rec,winid){
   if(!WtfGlobal.EnableDisable(Wtf.UPerm.invoice, Wtf.Perm.invoice.createinvoice)) {
    winid=(winid==null?'BillingInvoice':winid);
    var panel = Wtf.getCmp(winid);
    if(panel==null){
        panel = new Wtf.account.TransactionPanel({
            id : winid,
            isEdit:isEdit,
            isCustBill:true,
            isCustomer:true,
            record: rec,
            heplmodeid: 2, //This is help mode id
            label:' Invoice',
            title:WtfGlobal.getLocaleText("acc.accPref.autoInvoice"), //'Invoice',
            closable: true,
            iconCls:'accountingbase invoice'
        });
        panel.on("activate", function(){
            if(Wtf.isIE7) {
                var northHt=(Wtf.isIE?260:230);
                var southHt=(Wtf.isIE?210:150);
                Wtf.getCmp(winid + 'southEastPanel').setHeight(southHt);
                Wtf.getCmp(winid + 'southEastPanel').setWidth(650);
                panel.NorthForm.setHeight(northHt);
                panel.southPanel.setHeight(southHt);
            }
            panel.doLayout();
        }, this);
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
   }
   else
        WtfComMsgBox(46,0,false,isEdit?WtfGlobal.getLocaleText("acc.common.editing"):WtfGlobal.getLocaleText("acc.common.creating") +" Invoice");
}

function callViewCashReceipt(rec,type,winid){
    winid=(winid==null?'ViewCashReceipt':winid);
    var panel = Wtf.getCmp(winid);
    var label="Cash Sales";
    if(panel==null){
        panel = new Wtf.account.TransectionTemplate({
            id : winid,
            isCustomer:true,
            readOnly:true,
            rec: rec,
            label:label,
            name:WtfGlobal.getLocaleText("acc.wtfTrans.vcs"),
            title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.wtfTrans.vcs"),Wtf.TAB_TITLE_LENGTH),
            tabTip:WtfGlobal.getLocaleText("acc.wtfTrans.vcs"),  //'View '+label,
            iconCls:'accountingbase viewinvoice'
        });
        panel.on("activate", function(){
            panel.doLayout();
        }, this);
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    panel.refreshView(rec);
    Wtf.getCmp('as').doLayout();
}
function callViewInvoice(rec,type,winid,inCash){
    winid=(winid==null?'ViewInvoice':winid);
    var panel = Wtf.getCmp(winid);
    var label=inCash?"Cash Sales":"Invoice";
    if(panel==null){
        panel = new Wtf.account.TransectionTemplate({
            id : winid,
            isCustomer:true,
            readOnly:true,
            rec: rec,
            label:label,
            name:inCash?WtfGlobal.getLocaleText("acc.wtfTrans.vcs"):WtfGlobal.getLocaleText("acc.wtfTrans.vi"),
            title:Wtf.util.Format.ellipsis((inCash?WtfGlobal.getLocaleText("acc.wtfTrans.vcs"):WtfGlobal.getLocaleText("acc.wtfTrans.vi")),Wtf.TAB_TITLE_LENGTH),
            tabTip:inCash?WtfGlobal.getLocaleText("acc.wtfTrans.vcs"):WtfGlobal.getLocaleText("acc.wtfTrans.vi"),  //'View '+label,
            iconCls:'accountingbase viewinvoice'
        });
        panel.on("activate", function(){
            panel.doLayout();
        }, this);
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    panel.refreshView(rec);
    Wtf.getCmp('as').doLayout();
}
function callViewBillingInvoice(rec,type,winid,inCash){
    winid=(winid==null?'ViewBillingInvoice':winid);
    var panel = Wtf.getCmp(winid);
    var label=" Invoice";
    if(panel==null){
        panel = new Wtf.account.TransectionTemplate({
            id : winid,
            isCustomer:true,
            inCash:inCash,
            isCustBill:true,
            readOnly:true,
            rec: rec,
            label:label,
            title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.wtfTrans.vi"),Wtf.TAB_TITLE_LENGTH),
            tabTip:WtfGlobal.getLocaleText("acc.wtfTrans.vi"),  //'View '+label,
            iconCls:'accountingbase viewinvoice'
        });
        panel.on("activate", function(){
            panel.doLayout();
        }, this);
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    panel.refreshView(rec);
    Wtf.getCmp('as').doLayout();
}

function callViewBillingCashReceipt(rec,type,winid){
    winid=(winid==null?'ViewBillingInvoice':winid);
    var panel = Wtf.getCmp(winid);
    var label="Cash Sales";
    if(panel==null){
        panel = new Wtf.account.TransectionTemplate({
            id : winid,
            inCash:true,
            isCustomer:true,
            isCustBill:true,
            readOnly:true,
            rec: rec,
            label:label,
            title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.wtfTrans.vcs"),Wtf.TAB_TITLE_LENGTH),
            tabTip:WtfGlobal.getLocaleText("acc.wtfTrans.vcs"),  //'View '+label,
            iconCls:'accountingbase viewinvoice'
        });
        panel.on("activate", function(){
            panel.doLayout();
        }, this);
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    panel.refreshView(rec);
    Wtf.getCmp('as').doLayout();
}

function callViewPaymentReceipt(rec,type,winid){
    winid=(winid==null?'ViewPaymentReceipt':winid);
    var panel = Wtf.getCmp(winid);
     var label="Cash Purchase";
    if(panel==null){
        panel = new Wtf.account.TransectionTemplate({
            id : winid,
            isCustomer:false,
            readOnly:true,
            rec: rec,
            label:label,
            name:WtfGlobal.getLocaleText("acc.wtfTrans.vcp"),
            title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.wtfTrans.vcp"),Wtf.TAB_TITLE_LENGTH),
            tabTip:WtfGlobal.getLocaleText("acc.wtfTrans.vcp"),  //'View '+label,
            iconCls:'accountingbase viewgoodsreceipt'
        });
        panel.on("activate", function(){
            panel.doLayout();
        }, this);
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    panel.refreshView(rec);
    Wtf.getCmp('as').doLayout();
}
function callViewGoodsReceipt(rec,type,winid,inCash){
    winid=(winid==null?'ViewGoodsReceipt':winid);
    var panel = Wtf.getCmp(winid);
     var label=inCash?"Cash Purchase":"Vendor Receipt";
    if(panel==null){
        panel = new Wtf.account.TransectionTemplate({
            id : winid,
            isCustomer:false,
            readOnly:true,
            isexpenseinv:rec.data.isexpenseinv,
            rec: rec,
            label:label,
            name:inCash?WtfGlobal.getLocaleText("acc.wtfTrans.vcp"):WtfGlobal.getLocaleText("acc.wtfTrans.vvr"),
            title:Wtf.util.Format.ellipsis((inCash?WtfGlobal.getLocaleText("acc.wtfTrans.vcp"):WtfGlobal.getLocaleText("acc.wtfTrans.vvr")),Wtf.TAB_TITLE_LENGTH),
            tabTip:inCash?WtfGlobal.getLocaleText("acc.wtfTrans.vcp"):WtfGlobal.getLocaleText("acc.wtfTrans.vvr"),  //'View '+label,
            iconCls:'accountingbase viewgoodsreceipt'
        });
        panel.on("activate", function(){
            panel.doLayout();
        }, this);
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    panel.refreshView(rec);
    Wtf.getCmp('as').doLayout();
}

function callViewCreditNote(rec,type,winid){
    winid=(winid==null?'ViewCreditNote':winid);
    var panel = Wtf.getCmp(winid);
    if(panel==null){
        panel = new Wtf.account.TransectionTemplate({
            id : winid,
            isCustomer:true,
            readOnly:true,
            rec: rec,
            name:WtfGlobal.getLocaleText("acc.wtfTrans.vcn"),
            label:'Credit Note',
            title:WtfGlobal.getLocaleText("acc.wtfTrans.vcn"),  //'View Credit Note',
            iconCls:'accountingbase viewcreditnote'
        });
        panel.on("activate", function(){
            panel.doLayout();
        }, this);
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    panel.refreshView(rec);
    Wtf.getCmp('as').doLayout();
}
function callViewDebitNote(rec,type,winid){
    winid=(winid==null?'ViewDebitNote':winid);
    var panel = Wtf.getCmp(winid);
    if(panel==null){
        panel = new Wtf.account.TransectionTemplate({
            id : winid,
            isCustomer:false,
            readOnly:true,
            name:WtfGlobal.getLocaleText("acc.wtfTrans.vdn"),
            rec: rec,
            label:'Debit Note',
            title:WtfGlobal.getLocaleText("acc.wtfTrans.vdn"),  //'View Debit Note',
            iconCls:'accountingbase viewdebitnote'
        });
        panel.on("activate", function(){
            panel.doLayout();
        }, this);
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    panel.refreshView(rec);
    Wtf.getCmp('as').doLayout();
}

function callViewPayment(rec,winid,typeCheck){
    winid=(winid==null?(typeCheck?'ViewPaymentMade':'ViewReceivePayment'):winid);
    var panel = Wtf.getCmp(winid);
    if(panel==null){
        panel = new Wtf.account.TransectionTemplate({
            id : winid,
            isCustomer:(typeCheck?true:false),
            readOnly:true,
            rec: rec,
            name:(typeCheck?WtfGlobal.getLocaleText("acc.wtfTrans.vrp"):WtfGlobal.getLocaleText("acc.wtfTrans.vpm")),
            label:(typeCheck?'Receive Payment':'Payment Made'),
            title:Wtf.util.Format.ellipsis((typeCheck?WtfGlobal.getLocaleText("acc.wtfTrans.vrp"):WtfGlobal.getLocaleText("acc.wtfTrans.vpm")),Wtf.TAB_TITLE_LENGTH),
            tabTip:(typeCheck?WtfGlobal.getLocaleText("acc.wtfTrans.vrp"):WtfGlobal.getLocaleText("acc.wtfTrans.vpm")),
            iconCls:(typeCheck?'accountingbase viewreceivepayment':'accountingbase viewpaymentmade')
        });
        panel.on("activate", function(){
            panel.doLayout();
        }, this);
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    panel.refreshView(rec);
    Wtf.getCmp('as').doLayout();
}

function callViewBillPayment(rec,winid,typeCheck){
    winid=(winid==null?(typeCheck?'ViewBillPaymentMade':'ViewBillReceivePayment'):winid);
    var panel = Wtf.getCmp(winid);
    if(panel==null){
        panel = new Wtf.account.TransectionTemplate({
            id : winid,
            isCustomer:(typeCheck?true:false),
            readOnly:true,
            rec: rec,
            receiptTemp:true,
            isCustBill:true,
            name:(typeCheck?WtfGlobal.getLocaleText("acc.wtfTrans.vrp"):WtfGlobal.getLocaleText("acc.wtfTrans.vpm")),
            label:(typeCheck?'Receive Payment':'Payment Made'),
            title:Wtf.util.Format.ellipsis((typeCheck?WtfGlobal.getLocaleText("acc.wtfTrans.vrp"):WtfGlobal.getLocaleText("acc.wtfTrans.vpm")),Wtf.TAB_TITLE_LENGTH),
            tabTip:(typeCheck?WtfGlobal.getLocaleText("acc.wtfTrans.vrp"):WtfGlobal.getLocaleText("acc.wtfTrans.vpm")),
            iconCls:(typeCheck?'accountingbase viewreceivepayment':'accountingbase viewpaymentmade')
        });
        panel.on("activate", function(){
            panel.doLayout();
        }, this);
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    panel.refreshView(rec);
    Wtf.getCmp('as').doLayout();
}

function callEditBillPayment(rec,winid,typeCheck){
    winid=(winid==null?(typeCheck?'EditBillPaymentMade':'EditBillReceivePayment'):winid);
    var panel = Wtf.getCmp(winid);    
    if(panel==null){
        panel = new Wtf.account.OSDetailPanel({
            id : winid,
            isCustomer:(typeCheck?true:false),
            //readOnly:true,
            isEdit:true,
            isReceipt:typeCheck,
            layout: 'border',            
            readOnly:true,
            record: rec,
            //receiptTemp:true,
            closable: true,
            isCustBill:true,
            cls: 'paymentFormPayMthd',
            label:(typeCheck?'Receive Payment':'Payment Made'),
            title:Wtf.util.Format.ellipsis((typeCheck?'Edit Receive Payment':'Edit Payment Made'),Wtf.TAB_TITLE_LENGTH),
            tabTip:(typeCheck?'Edit Receive Payment':'Edit Payment Made'),
            iconCls:(typeCheck?'accountingbase viewreceivepayment':'accountingbase viewpaymentmade')
        });
        panel.on("activate", function(){
            panel.doLayout();
            Wtf.getCmp(panel.id+"wrapperPanelNorth").doLayout();
        }, this);
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    if(typeCheck)
        panel.on('update',  function(){Wtf.getCmp("receiptBillingReport").loadStore();Wtf.getCmp('as').remove(panel);}, this);
        else
        panel.on('update',  function(){Wtf.getCmp("paymentBillingReport").loadStore();Wtf.getCmp('as').remove(panel);}, this);
  //  panel.refreshView(rec);
    Wtf.getCmp('as').doLayout();
}
function callEditPayment(rec,winid,typeCheck){
       winid=(winid==null?'EditReceivePayment':winid);
        var panel = Wtf.getCmp(winid);
        if(panel!=null){
             Wtf.getCmp('as').remove(panel);
            panel.destroy();
            panel=null;
        }
        if(panel==null){
            panel = new Wtf.account.OSDetailPanel({
                id : winid,
                border : false,
                isReceipt:typeCheck,
                layout: 'border',
                readOnly:true,
                record: rec,
                cls: 'paymentFormPayMthd',
                isEdit:true,
                helpmodeid: 9, //This is help mode id
                label:(typeCheck?'Copy Receive Payment':'Edit Payment Made'),
                title:Wtf.util.Format.ellipsis((typeCheck?'Edit Receive Payment':'Edit Payment Made'),Wtf.TAB_TITLE_LENGTH),
                tabTip:(typeCheck?'Edit Receive Payment':'Edit Payment Made'),
                iconCls:'accountingbase receivepayment',
                closable: true
            });
            panel.on("activate", function(){
                panel.doLayout();
                Wtf.getCmp(panel.id+"wrapperPanelNorth").doLayout();
            }, this);
            Wtf.getCmp('as').add(panel);
        }
    //    panel.on('invoice',callInvoiceList);
        Wtf.getCmp('as').setActiveTab(panel);
        if(typeCheck)
        panel.on('update',  function(){Wtf.getCmp("receiptReport").loadStore();Wtf.getCmp('as').remove(panel);}, this);
        else
        panel.on('update',  function(){Wtf.getCmp("paymentReport").loadStore();Wtf.getCmp('as').remove(panel);}, this);
        Wtf.getCmp('as').doLayout();
}

function callBillingSalesReceipt(isEdit,rec,winid){
   if(!WtfGlobal.EnableDisable(Wtf.UPerm.invoice, Wtf.Perm.invoice.createcashsales)) {
    winid=(winid==null?'BillingSalesReceipt':winid);
    var panel = Wtf.getCmp(winid);
    if(panel==null){
        panel = new Wtf.account.TransactionPanel({
            id : winid,
            isEdit:isEdit,
            isCustBill:true,
            cash:true,
            isCustomer:true,
            record: rec,
            heplmodeid: 8, //This is help mode id
            label:'Sales Receipt',
            title:WtfGlobal.getLocaleText("acc.accPref.autoCS"),  //'Cash Sales',
            iconCls:'accountingbase invoice'
        });
        panel.on("activate", function(){
            if(Wtf.isIE7) {
                var northHt=(Wtf.isIE?240:210);
                var southHt=(Wtf.isIE?210:150);
                Wtf.getCmp(winid + 'southEastPanel').setHeight(southHt);
                Wtf.getCmp(winid + 'southEastPanel').setWidth(650);
                panel.NorthForm.setHeight(northHt);
                panel.southPanel.setHeight(southHt);
            }
            panel.doLayout();
        }, this);
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
    }
    else
        WtfComMsgBox(46,0,false,isEdit?WtfGlobal.getLocaleText("acc.common.editing"):WtfGlobal.getLocaleText("acc.common.creating") +" Cash Sales");
}

function callSalesReceipt(isEdit,rec,winid){
   if(!WtfGlobal.EnableDisable(Wtf.UPerm.invoice, Wtf.Perm.invoice.createcashsales)) {
    winid=(winid==null?'salesreceipt':winid);
    var panel = Wtf.getCmp(winid);
    if(panel==null){
        panel = new Wtf.account.TransactionPanel({
            id : winid,
            isEdit:isEdit,
            label:'Invoice',
            cash:true,
            isCustomer:true,
            record: rec,
            heplmodeid: 8, //This is help mode id
            border : false,
//            layout: 'border',
            title:WtfGlobal.getLocaleText("acc.accPref.autoCS"),  //'Cash Sales',
            closable: true,
            iconCls:'accountingbase salesreceipt'
        });
        panel.on("activate", function(){
            if(Wtf.isIE7) {
                var northHt=(Wtf.isIE?240:210);
                var southHt=(Wtf.isIE?210:150);
                Wtf.getCmp(winid + 'southEastPanel').setHeight(southHt);
                Wtf.getCmp(winid + 'southEastPanel').setWidth(650);
                panel.NorthForm.setHeight(northHt);
                panel.southPanel.setHeight(southHt);
                panel.on("afterlayout", function(panel, lay){if(Wtf.isIE7) {panel.Grid.setSize(panel.getInnerWidth()-18,200);}},this);
            }
            panel.doLayout();
        }, this);
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
    if(Wtf.getCmp("pricewindow")!=undefined)
            Wtf.getCmp("pricewindow").on('update',function(){Wtf.getCmp(winid).Grid.loadPriceStore()},this);
    }
    else
        WtfComMsgBox(46,0,false,isEdit?WtfGlobal.getLocaleText("acc.common.editing"):WtfGlobal.getLocaleText("acc.common.creating") +" Cash Sales");
}
function callPurchaseReceipt(isEdit,rec,winid){
   if(!WtfGlobal.EnableDisable(Wtf.UPerm.vendorinvoice, Wtf.Perm.vendorinvoice.createcashpurchasevendorinvoice)) {

    winid=(winid==null?'purchasereceipt':winid);
    var panel = Wtf.getCmp(winid);
    if(panel==null){
        panel = new Wtf.account.TransactionPanel({
            id : winid,
            isEdit:isEdit,
            label:'Receipt',
            cash:true,
            isCustomer:false,
            heplmodeid: 33, //This is help mode id
            record: rec,
            border : false,
//            layout: 'border',
            title:WtfGlobal.getLocaleText("acc.accPref.autoCP"),  //'Cash Purchase',
            closable: true,
            iconCls:'accountingbase salesreceipt'
        });
        panel.on("activate", function(){
            if(Wtf.isIE7) {
                var northHt=(Wtf.isIE?240:210);
                var southHt=(Wtf.isIE?210:150);
                Wtf.getCmp(winid + 'southEastPanel').setHeight(southHt);
                Wtf.getCmp(winid + 'southEastPanel').setWidth(650);
                panel.NorthForm.setHeight(northHt);
                panel.southPanel.setHeight(southHt);
                panel.on("afterlayout", function(panel, lay){if(Wtf.isIE7) {panel.GridPanel.setSize(panel.getInnerWidth()-35,200);}},this);
            }
            panel.doLayout();
        }, this);
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
    if(Wtf.getCmp("pricewindow")!=undefined)
            Wtf.getCmp("pricewindow").on('update',function(){Wtf.getCmp(winid).Grid.loadPriceStore()},this);
    }
    else
        WtfComMsgBox(46,0,false,isEdit?WtfGlobal.getLocaleText("acc.common.editing"):WtfGlobal.getLocaleText("acc.common.creating") +" Cash Purchase");
}

function callQuotation(){
	if(!WtfGlobal.EnableDisable(Wtf.UPerm.invoice, Wtf.Perm.invoice.createquotation)) {
	    winid='quotation';
	    var label = 'Quotation';
	    var panel = Wtf.getCmp(winid);
	    if(panel==null){
	        panel = new Wtf.account.TransactionPanel({
	        	quotation : true,
	            id : winid,
	            isCustomer:true,
	            isOrder:true,
	            isEdit: false,
	            label:'Quotation',
	            border : false,
	            heplmodeid: 34,
	            title:WtfGlobal.getLocaleText("acc.accPref.autoQN"),  //label, 
	            closable: true,
	            iconCls:'accountingbase purchaseorder'
	        });
	        panel.on("activate", function(){
	            if(Wtf.isIE7) {
	                var northHt=(Wtf.isIE?150:180);
	                var southHt=(Wtf.isIE?210:150);
	                Wtf.getCmp(winid + 'southEastPanel').setHeight(southHt);
	                Wtf.getCmp(winid + 'southEastPanel').setWidth(650);
	                panel.NorthForm.setHeight(northHt);
	                panel.southPanel.setHeight(southHt);
	                panel.on("afterlayout", function(panel, lay){if(Wtf.isIE7) {panel.Grid.setSize(panel.getInnerWidth() - 18,200);}},this);
	            }
	            panel.doLayout();
	        }, this);
	        Wtf.getCmp('as').add(panel);
	    }
	    Wtf.getCmp('as').setActiveTab(panel);
	    panel.on('update',  function(){if(isEdit == true)Wtf.getCmp('as').remove(panel);}, this);
	    Wtf.getCmp('as').doLayout();
	}
    else
        WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.creating") +" Quotation");
}

function callSalesOrder(isEdit,rec,winid){
    if(!WtfGlobal.EnableDisable(Wtf.UPerm.invoice, Wtf.Perm.invoice.createso)) {
    winid=(winid==null?'salesorder':winid);
    var label = 'Sales Order';
    var panel = Wtf.getCmp(winid);
    if(panel==null){
        panel = new Wtf.account.TransactionPanel({
            id : winid,
            isEdit: isEdit,
            record: rec,
            isCustomer:true,
            isOrder:true,
            label:'Order',
            border : false,
            heplmodeid: 11,
//            layout: 'border',
            closable: true,
            title:Wtf.util.Format.ellipsis(((isEdit?WtfGlobal.getLocaleText("acc.wtfTrans.eso"):WtfGlobal.getLocaleText("acc.wtfTrans.so"))+" "+((rec != null)?rec.data.billno:"")),Wtf.TAB_TITLE_LENGTH),
            iconCls:'accountingbase salesorder'
        });
        panel.on("activate", function(){
            if(Wtf.isIE7) {
                var northHt=(Wtf.isIE?150:180);
                var southHt=(Wtf.isIE?210:150);
                Wtf.getCmp(winid + 'southEastPanel').setHeight(southHt);
                Wtf.getCmp(winid + 'southEastPanel').setWidth(650);
                panel.NorthForm.setHeight(northHt);
                panel.southPanel.setHeight(southHt);
                panel.on("afterlayout", function(panel, lay){if(Wtf.isIE7) {panel.Grid.setSize(panel.getInnerWidth() - 18,200);}},this);
            }
            panel.doLayout();
        }, this);
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    panel.on('update',  function(){if(isEdit == true){Wtf.getCmp("SalesOrderList").loadStore();Wtf.getCmp('as').remove(panel);}}, this);
    Wtf.getCmp('as').doLayout();
    }
    else
        WtfComMsgBox(46,0,false,isEdit?WtfGlobal.getLocaleText("acc.common.editing"):WtfGlobal.getLocaleText("acc.common.creating") +" Sales Order");
}

function callPurchaseOrder(isEdit,rec,winid){
  if(!WtfGlobal.EnableDisable(Wtf.UPerm.vendorinvoice, Wtf.Perm.vendorinvoice.createpo)) {
    winid=(winid==null?'purchaseorder':winid);
    var label = 'Purchase Order';
    var panel = Wtf.getCmp(winid);
    if(panel==null){
        panel = new Wtf.account.TransactionPanel({
            id : winid,
            isEdit:isEdit,
            isCustomer:false,
            isOrder:true,
            label:'Order',
            border : false,
//            layout: 'border',
            heplmodeid: 12,
            title:WtfGlobal.getLocaleText("acc.accPref.autoPO"), //label, //Wtf.util.Format.ellipsis(((isEdit?'Edit ':'')+label+" "+((rec != null)?rec.data.billno:"")),Wtf.TAB_TITLE_LENGTH),
            closable: true,
            iconCls:'accountingbase purchaseorder'
        });
        panel.on("activate", function(){
            if(Wtf.isIE7) {
                var northHt=(Wtf.isIE?150:180);
                var southHt=(Wtf.isIE?210:150);
                Wtf.getCmp(winid + 'southEastPanel').setHeight(southHt);
                Wtf.getCmp(winid + 'southEastPanel').setWidth(650);
                panel.NorthForm.setHeight(northHt);
                panel.southPanel.setHeight(southHt);
                panel.on("afterlayout", function(panel, lay){if(Wtf.isIE7) {panel.Grid.setSize(panel.getInnerWidth() - 18,200);}},this);
            }
            panel.doLayout();
        }, this);
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    panel.on('update',  function(){if(isEdit == true)Wtf.getCmp('as').remove(panel);}, this);
    Wtf.getCmp('as').doLayout();
    }
    else
        WtfComMsgBox(46,0,false,isEdit?WtfGlobal.getLocaleText("acc.common.editing"):WtfGlobal.getLocaleText("acc.common.creating") +" "+WtfGlobal.getLocaleText("acc.accPref.autoPO"));
}

function callEditPurchaseOrder(isEdit,rec,winid){
	  if(!WtfGlobal.EnableDisable(Wtf.UPerm.vendorinvoice, Wtf.Perm.vendorinvoice.createpo)) {
	    var label = 'Purchase Order';
	    var panel = Wtf.getCmp(winid);
	    if(panel==null){
	        panel = new Wtf.account.TransactionPanel({
	            id : winid,
	            isEdit:isEdit,
	            record:rec,
	            isCustomer:false,
	            isOrder:true,
	            label:'Order',
	            border : false,
//	            layout: 'border',
	            heplmodeid: 12,
	            title:Wtf.util.Format.ellipsis(((isEdit?WtfGlobal.getLocaleText("acc.wtfTrans.epo"):WtfGlobal.getLocaleText("acc.wtfTrans.po"))+" "+rec.data.billno),Wtf.TAB_TITLE_LENGTH),
	            closable: true,
	            iconCls:'accountingbase purchaseorder'
	        });
	        panel.on("activate", function(){
	            if(Wtf.isIE7) {
	                var northHt=(Wtf.isIE?150:180);
	                var southHt=(Wtf.isIE?210:150);
	                Wtf.getCmp(winid + 'southEastPanel').setHeight(southHt);
	                Wtf.getCmp(winid + 'southEastPanel').setWidth(650);
	                panel.NorthForm.setHeight(northHt);
	                panel.southPanel.setHeight(southHt);
	                panel.on("afterlayout", function(panel, lay){if(Wtf.isIE7) {panel.Grid.setSize(panel.getInnerWidth() - 18,200);}},this);
	            }
	            panel.doLayout();
	        }, this);
	        Wtf.getCmp('as').add(panel);
	    }
	    Wtf.getCmp('as').setActiveTab(panel);
	    panel.on('update',  function(){if(isEdit == true){Wtf.getCmp("PurchaseOrderList").loadStore();Wtf.getCmp('as').remove(panel);}}, this);
	    Wtf.getCmp('as').doLayout();
	    }
	    else
	        WtfComMsgBox(46,0,false,isEdit?WtfGlobal.getLocaleText("acc.common.editing"):WtfGlobal.getLocaleText("acc.common.creating") +" "+WtfGlobal.getLocaleText("acc.wtfTrans.po"));
	}

function callProductDetails(productlinkid,addproductwin){
    if(!WtfGlobal.EnableDisable(Wtf.UPerm.product, Wtf.Perm.product.view)) {    
    var panel = Wtf.getCmp("ProductReport");
    if(panel==null){
         panel = new Wtf.account.ProductDetailsPanel({
            id : 'ProductReport',
            addproductwin:addproductwin,
            productlinkid:productlinkid,
            border : false,
            layout: 'fit',
            title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.productList.tabTitle"),Wtf.TAB_TITLE_LENGTH),
            tabTip:WtfGlobal.getLocaleText("acc.productList.tabTitle"),  //'Products & Services List',
            closable: true,
            iconCls:getButtonIconCls(Wtf.etype.product)
        });
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
    if(panel!=null && productlinkid!=null)
         Wtf.getCmp("ProductReport").calllinkRowColor(productlinkid);
    if(addproductwin&&!WtfGlobal.EnableDisable(Wtf.UPerm.product, Wtf.Perm.product.create)){
        callProductWindow(false,null,"productwin");
        Wtf.getCmp("productwin").on('update',panel.updateGrid,panel);
    }
    panel.on("activate",function(){
        if(Wtf.dirtyStore.product){
            panel.productStore.reload();
            Wtf.dirtyStore.product = false;
        }
    },this);
    }
    else
        WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.viewing")+" "+WtfGlobal.getLocaleText("acc.productList.tabTitle"));
}

function callCycleCount(){
    var approvalflag=0;
    var panel=null;
    var id,title;
    if(approvalflag==0){
        panel = Wtf.getCmp("CycleCount");
        id = 'CycleCount';
        title = WtfGlobal.getLocaleText("acc.productList.cc");  //'Cycle Count Entry';
    }else{
        panel = Wtf.getCmp("CycleCountApproval");
        id = 'CycleCountApproval';
        title = WtfGlobal.getLocaleText("acc.productList.cca");  //'Cycle Count Approval';
    }
    if(panel==null){
         panel = new Wtf.account.cycleCountPanel({
            id : id,
            title:Wtf.util.Format.ellipsis(title,18),
            tabTip:title,
            closable: true,
            layout:'fit',
            approve:approvalflag,
            iconCls:getButtonIconCls(Wtf.etype.addcyclecounttab)
        });
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
}

function callCycleCountApproval(){
    var approvalflag=1;
    var panel=null;
    var id,title;
    if(approvalflag==0){
        panel = Wtf.getCmp("CycleCount");
        id = 'CycleCount';
        title = WtfGlobal.getLocaleText("acc.productList.cycleCount");  //'Cycle Count';
    }else{
        panel = Wtf.getCmp("CycleCountApproval");
        id = 'CycleCountApproval';
        title = WtfGlobal.getLocaleText("acc.productList.cca");  //'Cycle Count Approval';
    }
    if(panel==null){
         panel = new Wtf.account.cycleCountPanel({
            id : id,
            title:Wtf.util.Format.ellipsis(title,18),
            tabTip:title,
            closable: true,
            layout:'fit',
            approve:approvalflag,
            iconCls:getButtonIconCls(Wtf.etype.approvecyclecounttab)
        });
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
}

function callCycleCountWorksheet(){
    var panel = Wtf.getCmp("CycleCountWorksheet");
    if(panel==null){
         panel = new Wtf.account.cycleCountWorksheet({
            id : 'CycleCountWorksheet',
            title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.productList.cycleCountWorksheet"),18),
            tabTip:WtfGlobal.getLocaleText("acc.productList.cycleCountWorksheet"),  //'Cycle Count Worksheet',
            closable: true,
            layout:'fit',
            iconCls:getButtonIconCls(Wtf.etype.countcyclecounttab)
        });
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
}

function callCycleCountReport(){
    var panel = Wtf.getCmp("CycleCountReport");
    if(panel==null){
        panel = new Wtf.account.CyclecountReport({
            id : 'CycleCountReport',
            border : false,
            layout: 'fit',
            title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.productList.cycleCountReport"),18),
            topTitle:WtfGlobal.getLocaleText("acc.productList.cycleCountReport"),  //'Cycle Count Report',
            closable: true,
            iconCls:getButtonIconCls(Wtf.etype.cyclecountreporttab)
        });

        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
}

function reorderProducts(productlinkid,addproductwin){
    var panel = Wtf.getCmp("reorderproducts");
    if(panel==null){
         panel = new Wtf.account.SuggestedReorder({
            id : 'reorderproducts',
            border : false,
            layout: 'fit',
            title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.productList.reorderProducts"),18),
            tabTip:WtfGlobal.getLocaleText("acc.productList.reorderProductsTT"),  //'List of suggested reorder product(s)',
            closable: true,
            iconCls:getButtonIconCls(Wtf.etype.reorderreport)
        });
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
    if(panel!=null && productlinkid!=null)
         Wtf.getCmp("ProductReport").calllinkRowColor(productlinkid);

}

function callProductList(){
    var panel = Wtf.getCmp("ProductList");
    if(panel==null){
         panel = new Wtf.account.ProductListPanel({
            id : 'ProductList',
            border : false,
            layout: 'fit',
            title:'Product List',
            closable: true,
            iconCls:'list'
        });
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
}


function callJournalEntryDetails(jid,check){
  if(!WtfGlobal.EnableDisable(Wtf.UPerm.coa, Wtf.Perm.coa.viewje)){
    var panelID = "JournalEntryDetails"+(jid!=undefined?jid:"");
    var panel = Wtf.getCmp(panelID);
    if(panel==null){
        panel = getJETab(panelID, WtfGlobal.getLocaleText("acc.jeList.tabTitle"), jid);
        Wtf.getCmp('as').add(panel);
        panel.expandJournalEntry(jid,check);
    }else{
        panel.expandJournalEntry(jid,check);
    }
     Wtf.getCmp('as').setActiveTab(panel);
     Wtf.getCmp('as').doLayout();
     }
    else
        WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.viewing")+" "+WtfGlobal.getLocaleText("acc.jeList.tabTitle"));
}

function AgedReceivable(){
    if(!WtfGlobal.EnableDisable(Wtf.UPerm.invoice, Wtf.Perm.invoice.viewagedreceivable)){
    var panel = Wtf.getCmp("AgedReceivable");
    if(panel==null){
        panel = new Wtf.account.AgedReceivable({
            id : 'AgedReceivable',
            border : false,
            layout: 'fit',
            title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.wtfTrans.agedr"),Wtf.TAB_TITLE_LENGTH),
            tabTip:WtfGlobal.getLocaleText("acc.wtfTrans.agedr"),  //'Aged Receivable',
            closable: true,
            iconCls:'report'
        });
        Wtf.getCmp('as').add(panel);
        panel.on('account',callLedger);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
    }
    else
        WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.viewing")+" "+" Aged Receivable Report");
}

function callFinalStatement(activetab1){
if((!WtfGlobal.EnableDisable(Wtf.UPerm.fstatement, Wtf.Perm.fstatement.viewtrialbalance))||(!WtfGlobal.EnableDisable(Wtf.UPerm.fstatement, Wtf.Perm.fstatement.viewledger))||(!WtfGlobal.EnableDisable(Wtf.UPerm.fstatement, Wtf.Perm.fstatement.viewtradingpnl))||(!WtfGlobal.EnableDisable(Wtf.UPerm.fstatement, Wtf.Perm.fstatement.viewbsheet))){
    var activetab=activetab1!=null?activetab1:0;
    var panel = Wtf.getCmp("finalStmnt");
    if(panel==null){
        panel = new Wtf.TabPanel({
            title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.financialStatements"),Wtf.TAB_TITLE_LENGTH),
            tabTip:WtfGlobal.getLocaleText("acc.financialStatements"),  //'Financial Statements',
            id:'finalStmnt',
            closable:true,
            iconCls:'accountingbase balancesheet'
        });
        Wtf.getCmp('as').add(panel);
    }
    if(activetab1 == undefined || activetab1 == null) {
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.fstatement, Wtf.Perm.fstatement.viewledger))
            callLedger();
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.fstatement, Wtf.Perm.fstatement.viewtrialbalance))
            TrialBalance();
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.fstatement, Wtf.Perm.fstatement.viewtradingpnl))
            TradingProfitLoss();
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.fstatement, Wtf.Perm.fstatement.viewbsheet))
            BalanceSheet();
    }
    Wtf.getCmp('as').setActiveTab(panel);
    panel.setActiveTab(activetab==0?Wtf.getCmp('ledger'):(activetab==1?Wtf.getCmp('TrialBalance'):(activetab==2?Wtf.getCmp('TradingProfitandLoss'):Wtf.getCmp('bsheet'))));
    Wtf.getCmp('as').doLayout();
    }
    else
        WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.viewing")+" "+WtfGlobal.getLocaleText("acc.financialStatements"));

}

function BalanceSheet(){


if(!WtfGlobal.EnableDisable(Wtf.UPerm.fstatement, Wtf.Perm.fstatement.viewbsheet)) {
    var panel = Wtf.getCmp("bsheet");
    callFinalStatement(3);
    if(panel==null){        
        panel = new Wtf.account.FinalStatement({
            id : 'bsheet',
            statementType:'BalanceSheet',
            border : false,
            layout: 'fit',
            title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.balanceSheet"),Wtf.TAB_TITLE_LENGTH),
            tabTip:WtfGlobal.getLocaleText("acc.balanceSheet"),  //'Balance Sheet',
            topTitle:'<center><font size=4>Balance Sheet</font></center>',
            closable: false,
            iconCls:'accountingbase balancesheet'
        });
        Wtf.getCmp('finalStmnt').add(panel);
    }
    Wtf.getCmp('finalStmnt').setActiveTab(panel);
    Wtf.getCmp('finalStmnt').doLayout();
    Wtf.getCmp('as').doLayout();
    panel.on("activate",function(panel){
        panel.westPanel.setWidth(panel.getInnerWidth()/2);
        panel.doLayout();
    });
   }
    else
        WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.viewing")+" "+" Balance Sheet");
}

function ProfitandLoss(){
    var panel = Wtf.getCmp("ProfitandLoss");
    callFinalStatement(2);
    if(panel==null){        
        panel = new Wtf.account.FinalStatement({
            id : 'ProfitandLoss',
            statementType:'ProfitAndLoss',
            border : false,
            layout: 'fit',
            title:'Profit & Loss',
            topTitle:'<center><font size=4>Profit and Loss Account</font></center>',
            closable: false,
            iconCls:'profitloss'
        });
        Wtf.getCmp('finalStmnt').add(panel);
    }
    Wtf.getCmp('finalStmnt').setActiveTab(panel);
    Wtf.getCmp('finalStmnt').doLayout();
    Wtf.getCmp('as').doLayout();
    panel.on("activate",function(panel){
        panel.doLayout();
    });
}

function TradingProfitLoss(){
if(!WtfGlobal.EnableDisable(Wtf.UPerm.fstatement, Wtf.Perm.fstatement.viewtradingpnl)) {
    var panel = Wtf.getCmp("TradingProfitandLoss");
    callFinalStatement(2);
    if(panel==null){        
          panel = new Wtf.account.FinalStatement({
                    id : 'TradingProfitandLoss',
                    title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.P&L.tabTitle"),Wtf.TAB_TITLE_LENGTH),
                    tabTip:WtfGlobal.getLocaleText("acc.P&L.tabTitleTT"),  //'Trading & Profit/Loss',
                    topTitle:'<center><font size=4>Trading and Profit / Loss Account</font></center>',
                    statementType:'TradingAndProfitLoss',
                    border : false,
                    closable: false,
                    layout: 'fit',
                    iconCls:'accountingbase financialreport'
          });
        Wtf.getCmp('finalStmnt').add(panel);
    }
    Wtf.getCmp('finalStmnt').setActiveTab(panel);
    Wtf.getCmp('finalStmnt').doLayout();
    Wtf.getCmp('as').doLayout();
    panel.on("activate",function(panel){
        panel.westPanel.setWidth(panel.getInnerWidth()/2);
        panel.doLayout();
    });
    }
    else
        WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.viewing")+" "+WtfGlobal.getLocaleText("acc.P&L.tabTitle"));
}

function Trading(){
    var panel = Wtf.getCmp("trading");
    callFinalStatement(2);
    if(panel==null){
        panel = new Wtf.account.FinalStatement({
            id : 'trading',
            statementType:'Trading',
            border : false,
            layout: 'fit',
            title:'Trading',
            topTitle:'<center><font size=4>Trading Account</font></center>',
            closable: true,
            iconCls:'financialreport'
        });
        Wtf.getCmp('finalStmnt').add(panel);
    }
    Wtf.getCmp('finalStmnt').setActiveTab(panel);
    Wtf.getCmp('finalStmnt').doLayout();
    Wtf.getCmp('as').doLayout();
    panel.on("activate",function(panel){
        panel.doLayout();
    });
}
function TrialBalance(){
      if(!WtfGlobal.EnableDisable(Wtf.UPerm.fstatement, Wtf.Perm.fstatement.viewtrialbalance)) {
    var panel = Wtf.getCmp("TrialBalance");
    callFinalStatement(0);
    if(panel==null){        
        panel = new Wtf.account.TrialBalance({
            id : 'TrialBalance',
            border : false,
            layout: 'fit',
            title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.trial.tabtitle"),Wtf.TAB_TITLE_LENGTH),
            tabTip:WtfGlobal.getLocaleText("acc.trial.tabtitle"),  //'Trial Balance',
            topTitle:'<center><font size=4>Trial Balance</font></center>',
            closable: false,
            iconCls:'accountingbase trialbalance'
        });
        Wtf.getCmp('finalStmnt').add(panel);
        panel.on('account',callLedger);
    }
    Wtf.getCmp('finalStmnt').setActiveTab(panel);
    Wtf.getCmp('finalStmnt').doLayout();
    Wtf.getCmp('as').doLayout();
    panel.on("activate",function(panel){
        panel.doLayout();
    });
     }
    else
        WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.viewing")+" "+" Trial Balance");
}

function callLedger(accid){
    if(!WtfGlobal.EnableDisable(Wtf.UPerm.fstatement, Wtf.Perm.fstatement.viewledger)) {
    if(!accid)accid=Wtf.account.companyAccountPref.cashaccount
    var panel = Wtf.getCmp("ledger");
    callFinalStatement(1);
    if(panel==null){
        panel = new Wtf.account.Ledger({
            id : 'ledger',
            border : false,
            layout: 'fit',
            title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.ledger.tabTitle"),Wtf.TAB_TITLE_LENGTH),
            tabTip:WtfGlobal.getLocaleText("acc.ledger.tabTitle"),  //'Ledger',
            iconCls: 'accountingbase ledger',
            accountID:accid,
            closable: false
        });
        Wtf.getCmp('finalStmnt').add(panel);
        panel.on('journalentry',callJournalEntryDetails);
    }else{        
        panel.showLedger(accid);
    }
    Wtf.getCmp('finalStmnt').setActiveTab(panel);
    Wtf.getCmp('finalStmnt').doLayout();
    Wtf.getCmp('as').doLayout();
    panel.on("activate",function(panel){
        panel.doLayout();
    });
       }
    else
        WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.viewing")+" "+WtfGlobal.getLocaleText("acc.ledger.tabTitle"));
}
function callJournalEntry(){
    if(!WtfGlobal.EnableDisable(Wtf.UPerm.coa, Wtf.Perm.coa.createje)) {
     var panel = Wtf.getCmp("JournalEntry");
            if(panel==null){
                panel = new Wtf.account.JournalEntryPanel({
                    id : 'JournalEntry',
                    border : false,
                    layout: 'border',
                    title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.je.tabTitle"),Wtf.TAB_TITLE_LENGTH),
                    tabTip:WtfGlobal.getLocaleText("acc.je.tabTitle"),  //"Journal Entry",
                    closable: true,
                    iconCls:'accountingbase journalentry'
                });
                Wtf.getCmp('as').add(panel);
            }
            Wtf.getCmp('as').setActiveTab(panel);
            Wtf.getCmp('as').doLayout();
      }
    else
        WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.creating")+" "+WtfGlobal.getLocaleText("acc.je.tabTitle"));
}

function PaymentMethod(winid){
    winid = (winid==null?"PaymentMethodReportWin":winid);
    var p = Wtf.getCmp(winid);
    if(!p){
        var accRec=new Wtf.data.Record.create([
            {name: 'accountid',mapping:'accid'},
            {name: 'accountname',mapping:'accname'}
        ]);
        var accStore=new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },accRec),
//            url: Wtf.req.account+'CompanyManager.jsp',
            url : "ACCAccount/getAccountsForCombo.do",
            baseParams:{
                mode:2,
                group:[9,18],
                nondeleted:true
            }
        });
        accStore.load();
      
        var cmbAccount= new Wtf.form.FnComboBox({
            fieldLabel:'Account',
            name:'accountid',
            hiddenName:'accountid',
            store:accStore,
            valueField:'accountid',
            displayField:'accountname',
            mode: 'local',
            disableKeyFilter:true,
            allowBlank:false,
            triggerAction:'all',
            forceSelection:true,
            typeAhead: true,
            hirarchical:true
//            addNewFn:function(){
//                callCOAWindow(false, null, "coaWin",false,false,false,false,true);
//                Wtf.getCmp("coaWin").on("update",function(){accStore.reload()},this);
//            }
        });
         if(!WtfGlobal.EnableDisable(Wtf.UPerm.coa, Wtf.Perm.coa.create)){
            cmbAccount.addNewFn=function(){callCOAWindow(false, null, "coaWin",false,false,false,false,true);
                Wtf.getCmp("coaWin").on("update",function(){accStore.reload();},this);}
        }
        var dTypeStore=new Wtf.data.SimpleStore({
            fields:[{name:"id"},{name:"name"}],
            data:[[0,"Cash"],[2,"Bank"]]//[1,"Card"],
        });
        var cmbDType= new Wtf.form.ComboBox({
            name:'detailtype',
            hiddenName:'detailtype',
            store:dTypeStore,
            valueField:'id',
            displayField:'name',
            mode: 'local',
            disableKeyFilter:true,
            allowBlank:false,
            triggerAction:'all',
            forceSelection:true,
            typeAhead: true
        });

        var cm=[{
            header: WtfGlobal.getLocaleText("acc.masterConfig.payMethod.gridBankName"),  //"Bank Name",
            dataIndex: 'methodname',
            editor:new Wtf.form.TextField({allowBlank:false,maxLength:50,regex:Wtf.specialChar})
        },{
            header: WtfGlobal.getLocaleText("acc.masterConfig.taxes.gridAccountName"),  //"Account Name",
            dataIndex: 'accountid',
            renderer:Wtf.comboBoxRenderer(cmbAccount),
            editor:cmbAccount
        },{
            header: WtfGlobal.getLocaleText("acc.masterConfig.payMethod.gridDetailType"),  //"Detail Type",
            dataIndex: 'detailtype',
            renderer:Wtf.comboBoxRenderer(cmbDType),
            editor:cmbDType
        }];
        this.payRec = new Wtf.data.Record.create([
            {name: 'methodid'},
            {name: 'methodname'},
            {name: 'accountid'},
            {name: 'detailtype',type:'int'}
        ]);
        this.payStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.payRec),
//            url:Wtf.req.account+'CompanyManager.jsp',
            url : "ACCPaymentMethods/getPaymentMethods.do",
            baseParams:{
                mode:51
            }
        });
        this.payStore.load();
        new Wtf.account.GridUpdateWindow({
            cm:cm,
            store:this.payStore,
            record:this.payRec,
            mode:52,
//            title:Wtf.util.Format.ellipsis('Payment Method Report',Wtf.TAB_TITLE_LENGTH),
            title:WtfGlobal.getLocaleText("acc.masterConfig.payMethod"),  //'Payment Method',
            tabTip:WtfGlobal.getLocaleText("acc.masterConfig.payMethod"),  //'Payment Method',
            headerImage:"../../images/accounting_image/Payment-Method.gif",
            id: winid,
            renderTo: document.body
        }).show();

    }
}

//function PurchaseOrder(){
//  var panel = Wtf.getCmp("PurchaseOrder");
//    if(panel==null){
//        panel = new Wtf.account.PurchaseOrder({
//            id : 'PurchaseOrder',
//            border : false,
//            layout: 'fit',
//            title:'Purchase Order',
//            closable: true,
//            iconCls:'report'
//        });
//        Wtf.getCmp('as').add(panel);
//    }
//    panel.on("activate", function(){
//            panel.doLayout();
//        }, this);
//    Wtf.getCmp('as').setActiveTab(panel);
//    Wtf.getCmp('as').doLayout();
//}
function callInvoiceList(id,check,isCash){
if(!WtfGlobal.EnableDisable(Wtf.UPerm.invoice, Wtf.Perm.invoice.viewinvoice)) {
    var panel = Wtf.getCmp("InvoiceList");
    var invoiceTabPanel = Wtf.getCmp("InvoiceListMainTab");
    if(panel==null){
        panel = getInvoiceTab(false, "InvoiceList", WtfGlobal.getLocaleText("acc.invoiceList.tabtitle"), undefined, isCash);
        invoiceTabPanel = new Wtf.TabPanel({
            activeTab: 0,
            border: false,
            closable: true,
            id: 'InvoiceListMainTab',
            title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.invoiceList.tabtitle"),Wtf.TAB_TITLE_LENGTH),
            tabTip:WtfGlobal.getLocaleText("acc.invoiceList.tabtitle"),  //'Invoice and Cash Sales Report',
            items: [
                panel,
                new Wtf.RepeatedInvoicesReport({
                    id : 'RepeateInvoiceList',
                    title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.invoiceList.recInvReport"),Wtf.TAB_TITLE_LENGTH),
                    tabTip:WtfGlobal.getLocaleText("acc.invoiceList.recInvReport"),  //'Recurring Invoices Report',
                    border: false,
                    closable: false,
                    layout: 'fit',
                    iconCls:'accountingbase invoicelist',
                    isCustBill:false
                })
            ]
        });
        Wtf.getCmp('as').add(invoiceTabPanel);
        panel.on('journalentry',callJournalEntryDetails);
        panel.expandInvoice(id,check);
    }else{
        panel.expandInvoice(id,check);
    }
    Wtf.getCmp('as').setActiveTab(invoiceTabPanel);
    invoiceTabPanel.setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
    }
    else
        WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.viewing")+" "+WtfGlobal.getLocaleText("acc.invoiceList.tabtitle"));
}
function callBillingInvoiceList(id,check,isCash){
   if(!WtfGlobal.EnableDisable(Wtf.UPerm.invoice, Wtf.Perm.invoice.viewinvoice)) {
    var panel = Wtf.getCmp("BillingInvoiceList");
    var invoiceTabPanel = Wtf.getCmp("InvoiceListMainTab");
    if(panel==null){
         panel = getInvoiceTab(true, "BillingInvoiceList", WtfGlobal.getLocaleText("acc.invoiceList.tabtitle"), undefined, isCash);
         invoiceTabPanel = new Wtf.TabPanel({
            activeTab: 0,
            border: false,
            closable: true,
            id: 'InvoiceListMainTab',
            title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.invoiceList.tabtitle"),Wtf.TAB_TITLE_LENGTH),
            tabTip:WtfGlobal.getLocaleText("acc.invoiceList.tabtitle"),  //'Invoice and Cash Sales Report',
            items: [
                panel,
                new Wtf.RepeatedInvoicesReport({
                    id : 'RepeateBillingInvoiceList',
                    title:Wtf.util.Format.ellipsis('Recurring Invoices Report',Wtf.TAB_TITLE_LENGTH),
                    tabTip:WtfGlobal.getLocaleText("acc.invoiceList.recInvReport"),  //'Recurring Invoices Report',
                    border: false,
                    closable: false,
                    layout: 'fit',
                    iconCls:'accountingbase invoicelist',
                    isCustBill:true
                })
            ]
        });
        Wtf.getCmp('as').add(invoiceTabPanel);
        panel.on('journalentry',callJournalEntryDetails);
        panel.expandInvoice(id,check);
    }else{
        panel.expandInvoice(id,check);
    }
    Wtf.getCmp('as').setActiveTab(invoiceTabPanel);
    invoiceTabPanel.setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
   }
    else
        WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.viewing")+" "+WtfGlobal.getLocaleText("acc.invoiceList.tabtitle"));
}

function callRepeatedInvoicesWindow(isCustBill, invoiceRec){
    var winid="RepeatedInvoicesWin";
    var panel = Wtf.getCmp(winid);
    if(!panel){
        new Wtf.RepeateInvoiceForm({
            id:winid,
            isCustBill: isCustBill,
            invoiceRec: invoiceRec,
            closable: true,
            modal: true,
            iconCls :getButtonIconCls(Wtf.etype.deskera),
            width: 500,
            height: 300,
            resizable: false,
            layout: 'border',
            buttonAlign: 'right',
            renderTo: document.body
        }).show();
    }
    Wtf.getCmp(winid).on("update",function(win){
        if(win.isCustBill){
            Wtf.getCmp("BillingInvoiceList").grid.getStore().reload();
            Wtf.getCmp("RepeateBillingInvoiceList").grid.getStore().reload();
        } else {
            Wtf.getCmp("InvoiceList").grid.getStore().reload();
            Wtf.getCmp("RepeateInvoiceList").grid.getStore().reload();
        }
    });
}

function callGoodsReceiptList(id,check){
   if(!WtfGlobal.EnableDisable(Wtf.UPerm.vendorinvoice, Wtf.Perm.vendorinvoice.viewvendorinvoice)) {
    var panel = Wtf.getCmp("GRList");
    if(panel==null){
         panel = getVendorInvoiceTab(false, "GRList", WtfGlobal.getLocaleText("acc.grList.tabTitle"));
         Wtf.getCmp('as').add(panel);
        panel.on('journalentry',callJournalEntryDetails);
        panel.expandInvoice(id,check);
    }else{
        panel.expandInvoice(id,check);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
   }
    else
        WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.viewing")+" "+WtfGlobal.getLocaleText("acc.grList.tabTitle"));
}

function callPurchaseOrderList(){
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.vendorinvoice, Wtf.Perm.vendorinvoice.viewpo)) {
    var panel = Wtf.getCmp("PurchaseOrderList");
    if(panel==null){
        panel = getPOTab(false, "PurchaseOrderList", WtfGlobal.getLocaleText("acc.poList.tabTitle"));
        Wtf.getCmp('as').add(panel);
        panel.on('journalentry',callJournalEntryDetails);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
    }
    else
        WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.viewing")+" "+WtfGlobal.getLocaleText("acc.poList.tabTitle"));
}

function callSalesOrderList(){
    if(!WtfGlobal.EnableDisable(Wtf.UPerm.invoice, Wtf.Perm.invoice.viewso)) {
    var panel = Wtf.getCmp("SalesOrderList");
    if(panel==null){
        panel = getSOTab(false, "SalesOrderList", WtfGlobal.getLocaleText("acc.soList.tabTitle"));
        Wtf.getCmp('as').add(panel);
        panel.on('journalentry',callJournalEntryDetails);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
         }
    else
        WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.viewing")+" "+WtfGlobal.getLocaleText("acc.soList.tabTitle"));
}

function callQuotationList(){
	if(!WtfGlobal.EnableDisable(Wtf.UPerm.invoice, Wtf.Perm.invoice.viewquotation)) {
    var panel = Wtf.getCmp("QuotationList");
    if(panel==null){
        panel = getQouteTab(false, "QuotationList", WtfGlobal.getLocaleText("acc.qnList.tabTitle"));
        Wtf.getCmp('as').add(panel);
        panel.on('journalentry',callJournalEntryDetails);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
    }
    else
        WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.viewing")+" "+WtfGlobal.getLocaleText("acc.qnList.tabTitle"));
}

function getQouteTab(isWithOutInventory, tabId, tabTitle, extraFilters){
    var reportPanel = new Wtf.account.TransactionListPanel({
        id : tabId,
        border : false,
//        isOrder:true,
        isQuotation:true,
        isCustomer:true,
        isCustBill: isWithOutInventory,
        title: Wtf.util.Format.ellipsis(tabTitle, Wtf.TAB_TITLE_LENGTH),
        tabTip: tabTitle,
        extraFilters: extraFilters,
        label:WtfGlobal.getLocaleText("acc.accPref.autoQN"),  //'Quotation',
        helpmodeid:35,
        layout: 'fit',
        closable: true,
        iconCls:'accountingbase salesorderlist'
    });
    return reportPanel;
}

function callSalesReceiptList(){
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.invoice, Wtf.Perm.invoice.createcashsales)) {
    var panel = Wtf.getCmp("SalesReceiptList");
    if(panel==null){
         panel = new Wtf.account.TransactionListPanel({
            id : 'SalesReceiptList',
            border : false,
            isOrder:false,
            isCustomer:true,
            cash:true,
            label:'Sales Receipt',
            layout: 'fit',
            title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.srList.tabTitle"),Wtf.TAB_TITLE_LENGTH),
            tabTip:WtfGlobal.getLocaleText("acc.srList.tabTitle"),  //'Sales Receipt Report',
            closable: true,
            iconCls:'accountingbase salesreceiptlist'
        });
        Wtf.getCmp('as').add(panel);
        panel.on('journalentry',callJournalEntryDetails);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
        }
      else
            WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.viewing")+" "+WtfGlobal.getLocaleText("acc.srList.tabTitle"));
}


function callAuditTrail(){
    var panel = Wtf.getCmp("auditTrail");
    if(panel==null){
        panel = new Wtf.common.WtfAuditTrail({
            layout : "fit",
            title:WtfGlobal.getLocaleText("acc.dashboard.auditTrail"),  //'Audit Trail',
            tabTip:WtfGlobal.getLocaleText("acc.dashboard.TT.auditTrail"),
            helpmodeid:30,
            border : false,
            id : "auditTrail",
            iconCls :getButtonIconCls(Wtf.etype.audittrail),
            closable: true
        });
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
}


function callMasterConfiguration(){
    var panel = Wtf.getCmp("masterconfiguration");
    if(panel==null){
        panel = new Wtf.account.MasterConfigurator({
            layout : "fit",
            title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.masterConfig.tabTitle"),Wtf.TAB_TITLE_LENGTH),
            tabTip:WtfGlobal.getLocaleText("acc.masterConfig.tabTitleTT"),  //'You can Add Master Items for various Master Groups from here.',
            helpmodeid:31,
            border : false,
            id : "masterconfiguration",
            iconCls:'accountingbase masterconfiguration',
            closable: true
        });
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
}

function addMasterItemWindow(id){
    var panel = Wtf.getCmp("masterconfiguration");
    var outer=false;
    if(panel==null){
        outer=true;
        panel = new Wtf.account.MasterConfigurator({
            layout : "fit",
            title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.masterConfig.tabTitle"),Wtf.TAB_TITLE_LENGTH),
            tabTip:WtfGlobal.getLocaleText("acc.masterConfig.tabTitleTT"),   //'Master Configuration',
            border : false,
            id : "masterconfiguration",
            closable: true
        });
    }
    panel.addMasterItemOuter(false,id,outer);
}

function callBusinessContactWindow(isEdit, rec, winid, isCustomer){
    winid=(winid==null?"businessContactwin":winid);
    var panel = Wtf.getCmp(winid);
    if(!panel){
        new Wtf.account.BusinessContactWindow({
            isEdit:isEdit,
            record:rec,
            id:winid,
            isCustomer:isCustomer,
//            closable: false,
            modal: true,
            iconCls :getButtonIconCls(Wtf.etype.deskera),
            width: 650,
            height: 600,
            resizable: false,
            layout: 'border',
            buttonAlign: 'right',
            renderTo: document.body
        }).show();
        this.loadMask1 = new Wtf.LoadMask(winid, {msg: WtfGlobal.getLocaleText("acc.msgbox.50"), msgCls: "x-mask-loading acc-customer-form-mask"});
        this.loadMask1.show();
        Wtf.getCmp(winid).on("loadingcomplete",function(){this.loadMask1.hide()},this);
    }
}

function callBankReconciliationReport(winid,accid){
    var panel= Wtf.getCmp('ReconciliationReport'+accid);
    if(panel==null){
        panel=new Wtf.account.BankReconciliationReport({
            title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.bankReconcile.tab"),Wtf.TAB_TITLE_LENGTH),
            tabTip:WtfGlobal.getLocaleText("acc.bankReconcile.tab"),  //'Reconciliation Report',
            id:'ReconciliationReport'+accid,
            accid:accid,
            layout:'fit',
            closable:true,
            iconCls:'accountingbase pricelistreport',
            border:false
        });
        Wtf.getCmp('as').add(panel);
    }
else
    panel.loadStore(accid)
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
}

function callReconciliationWindow(winid,reconRec){
reconRec=(reconRec==undefined?"":reconRec);
    winid=(winid==null?"reconciliationwin":winid);
    var panel = Wtf.getCmp(winid);
    if(!panel){
        new Wtf.account.ReconciliationWindow({
            id:winid,
            closable: true,
            reconRec:reconRec,
            modal: true,
            iconCls :getButtonIconCls(Wtf.etype.deskera),
            width: 450,
            height: 300,
            resizable: false,
            layout: 'border',
            buttonAlign: 'right',
            renderTo: document.body
        }).show();
    }
}
function callEmailWin(winid,rec,label,mode,isinvoice,isQuotation){
    rec=(rec==undefined?"":rec);
    winid=(winid==null?"editwin":winid);
    var panel = Wtf.getCmp(winid);
    if(!panel){
//        new Wtf.MailWin({
       new Wtf.account.MailWindow({
            id:winid,
            closable: true,
            rec:rec,
            isQuotation:isQuotation,
            isinvoice:isinvoice,
            mode:mode,
            label:label,
            modal: true,
            iconCls :getButtonIconCls(Wtf.etype.deskera),
            width: 700,
            height: (Wtf.isIE?595:557),
            resizable: false,
//            layout: 'border',
            buttonAlign: 'right',
            renderTo: document.body
        }).show();
    }
}
function callTaxEmailWin(winid,rec){
    rec=(rec==undefined?"":rec);
    winid=(winid==null?"editwin":winid);
    var panel = Wtf.getCmp(winid);
    if(!panel){
//        new Wtf.MailWin({
       new Wtf.account.MailWindow({
            id:winid,
            closable: true,
            rec:rec,
            tax1099:true,
            label:'vendorform',
            modal: true,
            iconCls :getButtonIconCls(Wtf.etype.deskera),
            width: 700,
            height: 557,
            resizable: false,
//            layout: 'border',
            buttonAlign: 'right',
            renderTo: document.body
        }).show();
    }
}


function callReconciliationLedger(reconRec){

    var panel = Wtf.getCmp("reconciliationledger");
    if(panel==null){
        panel = new Wtf.account.ReconciliationDetails({
            id : 'reconciliationledger',
            border : false,
            reconRec:reconRec,
            layout: 'fit',
            accountID:reconRec.accountid,
            tabTip:WtfGlobal.getLocaleText("acc.dashboard.TT.bankReconciliation"),
            title:WtfGlobal.getLocaleText("acc.dashboard.bankReconciliation"),  //'Bank Reconciliation',
            iconCls: 'accountingbase ledger',
            closable: true
        });
        Wtf.getCmp('as').add(panel);
        panel.on('journalentry',callJournalEntryDetails);
    }
    else{
        panel.updateData(reconRec.accountid,reconRec.startdate,reconRec.statementdate,reconRec.endingbalance);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
}

function callPriceReport(productid,product){
    var panel= Wtf.getCmp('pricereport'+product);
    if(panel==null){
        panel=new Wtf.account.PriceReport({
            title:Wtf.util.Format.ellipsis((WtfGlobal.getLocaleText("acc.productList.pricetab")+' '+product),Wtf.TAB_TITLE_LENGTH),
            tabTip:WtfGlobal.getLocaleText("acc.productList.pricetab")+' '+product,
            id:'pricereport'+product,
            productId:productid,
            productName:product,
            layout:'fit',
            closable:true,
            iconCls:'accountingbase pricelistreport',
            border:false
        });
        Wtf.getCmp('as').add(panel);
    }
    panel.on("activate",function(){
        panel.Store.reload();
    },this);

    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
}

function callPricelistWindow(rec,winid, carryIn,applyDate){
    winid=(winid==null?"pricewindow":winid);
    var panel = Wtf.getCmp(winid);
    if(!panel){
        new Wtf.account.PricelistWindow({
            title: rec.data.productname,
            id: winid,
            record: rec,
            carryIn:carryIn,
            applyDate:applyDate,
            closable: true,
            modal: true,
            iconCls :getButtonIconCls(Wtf.etype.deskera),
            width: 450,
            autoScroll:true,
            height: 325,
            resizable: false,
            layout: 'border',
            buttonAlign: 'right',
            renderTo: document.body
        }).show();
        this.loadMask1 = new Wtf.LoadMask(winid, {msg: WtfGlobal.getLocaleText("acc.msgbox.50"), msgCls: "x-mask-loading acc-pricelist-mask"});
        this.loadMask1.show();
        Wtf.getCmp(winid).on("loadingcomplete",function(){this.loadMask1.hide()},this);
    }
}
function callReceiptWindow(winid,isReceipt,personwin,isCustBill){
    winid=(winid==null?"recwindow":winid);
    var panel = Wtf.getCmp(winid);
    if(!panel){
        new Wtf.account.ReceiptWindow({
            title: isReceipt?WtfGlobal.getLocaleText("acc.mp.rtype"):WtfGlobal.getLocaleText("acc.mp.prtype"),  //"Receipt Type",
            id: winid,
            personwin:personwin,
            isCustBill:isCustBill,
            closable: false,
            isReceipt:isReceipt,
            modal: true,
            iconCls :getButtonIconCls(Wtf.etype.deskera),
            width: 450,
            autoScroll:true,
            height: 250,
            resizable: false,
            layout: 'border',
            buttonAlign: 'right',
            renderTo: document.body
        }).show();
    }
}
function callAccPrefWindow(winid){
    winid=(winid==null?"accprefwindow":winid);
    var panel = Wtf.getCmp(winid);
    if(!panel){
        new Wtf.account.ReceiptWindow({
            title: "Accounting Type",
            id: winid,
            isAccPref:true,
            closable: false,
            modal: true,
            iconCls :getButtonIconCls(Wtf.etype.deskera),
            width: 450,
            height: 250,
            resizable: false,
            layout: 'border',
            buttonAlign: 'right',
            renderTo: document.body
        }).show();
    }
}
function callAccountTypeWindow(winid,isReceipt,personwin){
    winid=(winid==null?"perwindow":winid);
    var panel = Wtf.getCmp(winid);
    if(!panel){
        new Wtf.account.ReceiptWindow({
            title: "Account Type",
            id: winid,
            personwin:personwin,
            closable: true,
            isReceipt:isReceipt,
            modal: true,
            iconCls :getButtonIconCls(Wtf.etype.deskera),
            width: 450,
            autoScroll:true,
            height: 250,
            resizable: false,
            layout: 'border',
            buttonAlign: 'right',
            renderTo: document.body
        }).show();
    }
}

function callProductWindow(isEdit, rec, winid, productname, isClone){
    winid=(winid==null?"productwindow":winid);
    productname=(productname==null?WtfGlobal.getLocaleText("acc.product.gridProduct"):productname);
    isClone=((isClone==null||isClone==undefined)?false:isClone);
    productname=(isClone?WtfGlobal.getLocaleText("acc.product.clone")+" "+productname:productname);
    if(!isClone && isEdit){
    	productname = WtfGlobal.getLocaleText("acc.common.edit")+" "+productname;
    }
    var panel = Wtf.getCmp(winid);
    if(panel==null){
        panel = new Wtf.account.ProductForm({
            title:productname,
            tabTip:productname,
            id:winid,
            isEdit:isEdit,
            isClone:isClone,
            record: rec,
            iconCls :getButtonIconCls(Wtf.etype.product),
            layout:'fit',
            closable:true,
            border:false
        });
        panel.on("update",function(panel){
            Wtf.getCmp('as').remove(panel);
            Wtf.productStore.reload();
            Wtf.productStoreSales.reload();
            Wtf.dirtyStore.product = true;
//            var ProductReportPanel = Wtf.getCmp("ProductReport");
//            if(ProductReportPanel != null){
//                Wtf.getCmp('as').setActiveTab(ProductReportPanel);
//                Wtf.getCmp('as').doLayout();
//            }
        },this);
        panel.on("activate",function(panel){panel.doLayout();},this);
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();

}

function callBuildAssemblyForm(productid){
    var buildForm = Wtf.getCmp("buildAssemblyForm");

    if(buildForm==null){
        buildForm = new Wtf.account.BuildAssemblyForm({
            title:WtfGlobal.getLocaleText("acc.productList.buildAssembly"),  //"Build Assembly",
            tabTip:WtfGlobal.getLocaleText("acc.productList.buildAssembly"),  //"Build Assembly",
            id:"buildAssemblyForm",
            productid:productid!=null?productid:"",
            iconCls :getButtonIconCls(Wtf.etype.buildassemly),
            layout:'fit',
            closable:true,
            border:false
        });
        buildForm.on("closed",function(buildForm){Wtf.getCmp('as').remove(buildForm)},this);
        buildForm.on("activate",function(panel){panel.doLayout();},this);
        Wtf.getCmp('as').add(buildForm);
    }
    Wtf.getCmp('as').setActiveTab(buildForm);
    Wtf.getCmp('as').doLayout();
}

function showProductValuationTab(){
    var productValuation = Wtf.getCmp("productValuation");
    if(productValuation==null){
        productValuation = new Wtf.account.productValuationGrid({
            title:WtfGlobal.getLocaleText("acc.productList.inventoryValuation"),  //"Inventory Valuation",
            tabTip:WtfGlobal.getLocaleText("acc.productList.inventoryValuation"),  //"Inventory Valuation",
            id:"productValuation",
            iconCls :getButtonIconCls(Wtf.etype.inventoryval),
            layout:'fit',
            closable:true,
            border:false
        });
        Wtf.getCmp('as').add(productValuation);
    }
    Wtf.getCmp('as').setActiveTab(productValuation);
    Wtf.getCmp('as').doLayout();
}

function callInventoryReport(productid,productname,rate){
    var panel = Wtf.getCmp('inventoryreport'+productname);
    if(panel==null){
        panel = new Wtf.account.InventoryReport({
            title:Wtf.util.Format.ellipsis((WtfGlobal.getLocaleText("acc.productList.invRep")+' '+productname),Wtf.TAB_TITLE_LENGTH),
            tabTip:WtfGlobal.getLocaleText("acc.productList.invRep")+' '+productname,
            id:'inventoryreport'+productname,
            productID:productid,
            productName:productname,
            rate:rate,
            layout:'fit',
            closable:true,
            iconCls:'accountingbase inventoryreport',
            border:false
        });
        Wtf.getCmp('as').add(panel);
    }
    panel.on("activate",function(){
        if(Wtf.dirtyStore.inventory){
            panel.loadStore();
            Wtf.dirtyStore.inventory = false;
        }
    },this);
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
 }

 function callFrequentLedger(cash,groupid,title,icon){
    var uPermType=Wtf.UPerm.fstatement;
    var permType=(cash?Wtf.Perm.fstatement.viewcashbook:Wtf.Perm.fstatement.viewbankbook);
    var str=(cash?WtfGlobal.getLocaleText("acc.bankBook.tabTitle1"):WtfGlobal.getLocaleText("acc.bankBook.tabTitle"));  //  //"Cash Book":"Bank Book");
   if(!WtfGlobal.EnableDisable(uPermType, permType)) { 
    var panel = Wtf.getCmp("Book"+groupid);
    if(panel==null){
        panel = new Wtf.account.FrequentLedger({
            id : "Book"+groupid,
            border : false,
            cash: cash,
            helpmodeid:(groupid==9?27:26),
            group:groupid,
            layout: 'fit',
            iconCls: icon,
            title:Wtf.util.Format.ellipsis(str,Wtf.TAB_TITLE_LENGTH) ,
            tabTip:str,
            closable: true
        });
        Wtf.getCmp('as').add(panel);
        panel.on('journalentry',callJournalEntryDetails);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
    }
    else
            WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.viewing")+" "+str);
 }
function callCreditNote(){
     if(!WtfGlobal.EnableDisable(Wtf.UPerm.invoice, Wtf.Perm.invoice.createcn)) {
     var panel = Wtf.getCmp("CreditNote");
         if(panel!=null){
             Wtf.getCmp('as').remove(panel);
            panel.destroy();
            panel=null;
        }
            if(panel==null){
                panel = new Wtf.account.TrNotePanel({
                    id : 'CreditNote',
                    border : false,
                    layout: 'fit',
                    helpmodeid:13,
                    isCN:true,
                    isCustBill:false,
                    title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.accPref.autoCN"),Wtf.TAB_TITLE_LENGTH) ,
                    tabTip:WtfGlobal.getLocaleText("acc.accPref.autoCN"),  //'Credit Note',
                    closable: true,
                    iconCls:'accountingbase creditnote'
                });
                panel.on("activate", function(){
                    panel.doLayout();
                }, this);
                Wtf.getCmp('as').add(panel);
            }
            Wtf.getCmp('as').setActiveTab(panel);
            Wtf.getCmp('as').doLayout();
    }
    else
            WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.creating")+" "+WtfGlobal.getLocaleText("acc.accPref.autoCN"));
}

function callDebitNote(){
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.vendorinvoice, Wtf.Perm.vendorinvoice.createdn)) {
     var panel = Wtf.getCmp("DebitNote");
         if(panel!=null){
             Wtf.getCmp('as').remove(panel);
            panel.destroy();
            panel=null;
        }
            if(panel==null){
                panel = new Wtf.account.TrNotePanel({
                    id : 'DebitNote',
                    border : false,
                    layout: 'fit',
                    isCN:false,
                    isCustBill:false,
                    title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.accPref.autoDN"),Wtf.TAB_TITLE_LENGTH),
                    tabTip:WtfGlobal.getLocaleText("acc.accPref.autoDN"),  //'Debit Note',
                     helpmodeid:14,
                    iconCls:'accountingbase debitnote',
                    closable: true
                });
                panel.on("activate", function(){
                    panel.doLayout();
                }, this);
                Wtf.getCmp('as').add(panel);
            }
            Wtf.getCmp('as').setActiveTab(panel);
            Wtf.getCmp('as').doLayout();
            }
    else
            WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.creating")+" "+WtfGlobal.getLocaleText("acc.accPref.autoDN"));
}

function callCreditNoteDetails(){
     if(!WtfGlobal.EnableDisable(Wtf.UPerm.invoice, Wtf.Perm.invoice.viewcn)) {
    var panel = Wtf.getCmp("CreditNoteDetails");
    if(panel==null){
        panel = getCNTab(false, "CreditNoteDetails", WtfGlobal.getLocaleText("acc.cnList.tabTitle"));
        Wtf.getCmp('as').add(panel);
        panel.on('journalentry',callJournalEntryDetails);
    }
     Wtf.getCmp('as').setActiveTab(panel);
     Wtf.getCmp('as').doLayout();
     }
    else
            WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.viewing")+" "+WtfGlobal.getLocaleText("acc.cnList.tabTitle"));
}

function callDebitNoteDetails(){
     if(!WtfGlobal.EnableDisable(Wtf.UPerm.vendorinvoice, Wtf.Perm.vendorinvoice.viewdn)) {
    var panel = Wtf.getCmp("DebitNoteDetails");
    if(panel==null){
         panel = getDNTab(false, "DebitNoteDetails", WtfGlobal.getLocaleText("acc.dnList.tabTitle"));
         Wtf.getCmp('as').add(panel);
        //panel.on('goodsreceipt', callGoodsReceiptDetails);
        panel.on('journalentry',callJournalEntryDetails);
    }
     Wtf.getCmp('as').setActiveTab(panel);
     Wtf.getCmp('as').doLayout();
    }
    else
            WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.viewing")+" "+WtfGlobal.getLocaleText("acc.dnList.tabTitle"));
}

function callReceipt(directPayment, invoiceRecord){
    if(!WtfGlobal.EnableDisable(Wtf.UPerm.invoice, Wtf.Perm.invoice.createreceipt)) {
     var panel = Wtf.getCmp("Receipt");
        if(panel!=null){
             Wtf.getCmp('as').remove(panel);
            panel.destroy();
            panel=null;
        }
        if(panel==null){
            panel = new Wtf.account.OSDetailPanel({
                id : 'Receipt',
                border : false,
                isReceipt:true,
                directPayment : directPayment,
                invoiceRecord : invoiceRecord,
                cls: 'paymentFormPayMthd',
                layout: 'border',
                helpmodeid: 9, //This is help mode id
                title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.accPref.autoRP"),Wtf.TAB_TITLE_LENGTH),
                tabTip:WtfGlobal.getLocaleText("acc.accPref.autoRP"),  //'Receive Payments',
                iconCls:'accountingbase receivepayment',
                closable: true
            });
            panel.on("activate", function(){
                panel.doLayout();
                Wtf.getCmp(panel.id+"wrapperPanelNorth").doLayout();
            }, this);
            Wtf.getCmp('as').add(panel);
        }
    //    panel.on('invoice',callInvoiceList);
        Wtf.getCmp('as').setActiveTab(panel);
        Wtf.getCmp('as').doLayout();
         }
    else
            WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.accPref.autoRP"));
}

function callBillingReceipt(directPayment, invoiceRecord){
   if(!WtfGlobal.EnableDisable(Wtf.UPerm.invoice, Wtf.Perm.invoice.createreceipt)) {
     var panel = Wtf.getCmp("BillingReceipt");
        if(panel!=null){
             Wtf.getCmp('as').remove(panel);
            panel.destroy();
            panel=null;
        }
        if(panel==null){
            panel = new Wtf.account.OSDetailPanel({
                id : 'BillingReceipt',
                border : false,
                isReceipt:true,
                isCustBill:true,
                directPayment : directPayment,
                invoiceRecord : invoiceRecord,
                cls: 'paymentFormPayMthd',
                layout: 'border',
                helpmodeid: 9, //This is help mode id
                title:Wtf.util.Format.ellipsis('Receive Payments',Wtf.TAB_TITLE_LENGTH),
                tabTip:WtfGlobal.getLocaleText("acc.accPref.autoRP"), //'Receive Payments',
                iconCls:'accountingbase receivepayment',
                closable: true
            });
            panel.on("activate", function(){
                panel.doLayout();
                Wtf.getCmp(panel.id+"wrapperPanelNorth").doLayout();
            }, this);
            Wtf.getCmp('as').add(panel);
        }
    //    panel.on('invoice',callInvoiceList);
        Wtf.getCmp('as').setActiveTab(panel);
        Wtf.getCmp('as').doLayout();
    }
    else
            WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.accPref.autoRP"));
}

function callPayment(directPayment, invoiceRecord){
     if(!WtfGlobal.EnableDisable(Wtf.UPerm.vendorinvoice, Wtf.Perm.vendorinvoice.createpayment)) {
    var panel = Wtf.getCmp("payment");
    if(panel!=null){
         Wtf.getCmp('as').remove(panel);
        panel.destroy();
        panel=null;
    }
    if(panel==null){
        panel = new Wtf.account.OSDetailPanel({
            id : 'payment',
            border : false,
            isReceipt:false,
            directPayment : directPayment,
            invoiceRecord : invoiceRecord,
            cls: 'paymentFormPayMthd',
            layout: 'border',
            helpmodeid: 10, //This is help mode id
            title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.accPref.autoMP"),Wtf.TAB_TITLE_LENGTH),
            tabTip:WtfGlobal.getLocaleText("acc.accPref.autoMP"),  //'Make Payment',
            iconCls:'accountingbase makepayment',
            closable: true
        });
        panel.on("activate", function(){            
            panel.doLayout();
            Wtf.getCmp(panel.id+"wrapperPanelNorth").doLayout();
        }, this);
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
    }
   else
            WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.accPref.autoMP"));
}
function BillingReceiptReport(){
       if(!WtfGlobal.EnableDisable(Wtf.UPerm.invoice, Wtf.Perm.invoice.viewreceipt)) {
    var panel = Wtf.getCmp("receiptBillingReport");
    if(panel==null){
        panel = new Wtf.account.ReceiptReport({
            id : 'receiptBillingReport',
            border : false,
            isCustBill:true,
            helpmodeid: 20,
            layout: 'fit',
            title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.prList.tabTitle"),Wtf.TAB_TITLE_LENGTH),
            tabTip:WtfGlobal.getLocaleText("acc.prList.tabTitle"), //'Payments Received Report',
            closable: true,
            isReceipt:true,
            iconCls:'accountingbase receivepaymentreport'
        //            activeItem : 0
        });
        Wtf.getCmp('as').add(panel);
        panel.on('journalentry',callJournalEntryDetails);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
   }
   else
            WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.viewing")+" "+WtfGlobal.getLocaleText("acc.prList.tabTitle"));
}
function ReceiptReport(){
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.invoice, Wtf.Perm.invoice.viewreceipt)) {
    var panel = Wtf.getCmp("receiptReport");
    if(panel==null){
        panel = new Wtf.account.ReceiptReport({
            id : 'receiptReport',
            border : false,
            helpmodeid: 20,
            layout: 'fit',
            title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.prList.tabTitle"),Wtf.TAB_TITLE_LENGTH),
            tabTip:WtfGlobal.getLocaleText("acc.prList.tabTitle"),  //'Payment Received Report',
            closable: true,
            isReceipt:true,
            iconCls:'accountingbase receivepaymentreport'
        //            activeItem : 0
        });
        Wtf.getCmp('as').add(panel);
        panel.on('journalentry',callJournalEntryDetails);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
         }
   else
            WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.viewing")+" "+WtfGlobal.getLocaleText("acc.prList.tabTitle"));
}

function callPaymentReport(){
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.vendorinvoice, Wtf.Perm.vendorinvoice.viewpayment)) {
    var panel = Wtf.getCmp("paymentReport");
    if(panel==null){
        panel = new Wtf.account.ReceiptReport({
            id : 'paymentReport',
            border : false,
             helpmodeid: 23,
            layout: 'fit',
            title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.pmList.tabTitle"),Wtf.TAB_TITLE_LENGTH),
            tabTip:WtfGlobal.getLocaleText("acc.pmList.tabTitle"),  //'Payment Made Report',
            closable: true,
            isReceipt:false,
            iconCls:'accountingbase makepaymentreport'
        });
        Wtf.getCmp('as').add(panel);
        panel.on('journalentry',callJournalEntryDetails);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
    }
   else
            WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.viewing")+" "+WtfGlobal.getLocaleText("acc.pmList.tabTitle"));
}

function callAgedRecievableReport(withinventory){
     var AgedRecievablepanel=Wtf.getCmp("AgedRecievable");
     if(AgedRecievablepanel==null){
         callAgedRecievable();
         AgedRecievablepanel = new Wtf.account.AgedDetail({
             id: 'AgedRecievable',
             border: false,
             helpmodeid:28,
             withinventory:withinventory,
             layout: 'fit',
             iconCls: 'accountingbase agedrecievable',
             title: WtfGlobal.getLocaleText("acc.agedPay.reportView"),  //'Report View',
             tabTip:WtfGlobal.getLocaleText("acc.rem.183"),
             receivable:true
         });
         Wtf.getCmp('mainAgedRecievable').add(AgedRecievablepanel);
         AgedRecievablepanel.on('journalentry',callJournalEntryDetails);
     }else{
     }
//     Wtf.getCmp('mainAgedRecievable').setActiveTab(AgedRecievablepanel);
     Wtf.getCmp('mainAgedRecievable').doLayout();
 }

 function callAgedRecievableSummary(withinventory){
     var AgedRecievablepanel=Wtf.getCmp("AgedRecievableSummary");
     if(AgedRecievablepanel==null){
         callAgedRecievable();
         AgedRecievablepanel = new Wtf.account.AgedDetail({
             id: 'AgedRecievableSummary',
             border: false,
             isSummary:true,
             withinventory:withinventory,
             layout: 'fit',
             iconCls: 'accountingbase agedrecievable',
             title: WtfGlobal.getLocaleText("acc.agedPay.summaryView"),  //'Summary View',
             tabTip:WtfGlobal.getLocaleText("acc.rem.182"),
             receivable:true
         });
         Wtf.getCmp('mainAgedRecievable').add(AgedRecievablepanel);
     }
     Wtf.getCmp('mainAgedRecievable').setActiveTab(AgedRecievablepanel);
     Wtf.getCmp('mainAgedRecievable').doLayout();
 }

function callAgedRecievable(withinventory){
    if(!WtfGlobal.EnableDisable(Wtf.UPerm.invoice, Wtf.Perm.invoice.viewagedreceivable)) { 
    var panel = Wtf.getCmp("mainAgedRecievable");
    if(panel==null){
        panel = new Wtf.TabPanel({
            title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.wtfTrans.agedr"),Wtf.TAB_TITLE_LENGTH),
            tabTip:WtfGlobal.getLocaleText("acc.wtfTrans.agedr"),   //'Aged Receivable',
            withinventory:withinventory,
            id:'mainAgedRecievable',
            closable:true,
            border:false,
            iconCls:'accountingbase agedrecievable',
            activeTab:0
        });
        Wtf.getCmp('as').add(panel);
        callAgedRecievableSummary(withinventory);
        callAgedRecievableReport(withinventory);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
     }
   else
            WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.viewing")+" "+WtfGlobal.getLocaleText("acc.wtfTrans.agedrr"));
}

function callAgedPayable(withinventory){
     if(!WtfGlobal.EnableDisable(Wtf.UPerm.vendorinvoice, Wtf.Perm.vendorinvoice.viewagedpayable)) {
    var panel = Wtf.getCmp("mainAgedPayable");
    if(panel==null){
        panel = new Wtf.TabPanel({
            title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.wtfTrans.agedp"),Wtf.TAB_TITLE_LENGTH),
            tabTip:WtfGlobal.getLocaleText("acc.wtfTrans.agedp"),  //'Aged Payable',
            id:'mainAgedPayable',
            withinventory:withinventory,
            closable:true,
            border:false,
            iconCls:'accountingbase balancesheet',
            activeTab:0
        });
        Wtf.getCmp('as').add(panel);
        callAgedPayableSummary(withinventory)
        callAgedPayableReport(withinventory);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
    }
   else
            WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.viewing")+" "+WtfGlobal.getLocaleText("acc.wtfTrans.agedpr"));
}

function callAgedPayableReport(withinventory){
     var AgedPayablepanel=Wtf.getCmp("AgedPayable");
     if(AgedPayablepanel==null){
         callAgedPayable();
         AgedPayablepanel = new Wtf.account.AgedDetail({
             id: 'AgedPayable',
             withinventory:withinventory,
             helpmodeid:29,
             border: false,
             layout: 'fit',
             iconCls: 'accountingbase agedpayable',
             tabTip:WtfGlobal.getLocaleText("acc.rem.181"),
             title: WtfGlobal.getLocaleText("acc.agedPay.reportView") //'Report View'
         });
         Wtf.getCmp('mainAgedPayable').add(AgedPayablepanel);
         AgedPayablepanel.on('journalentry',callJournalEntryDetails);
     }else{
     }
//     Wtf.getCmp('mainAgedPayable').setActiveTab(AgedPayablepanel);
     Wtf.getCmp('mainAgedPayable').doLayout();
 }

function callAgedPayableSummary(withinventory){
     var AgedPayablepanel=Wtf.getCmp("AgedPayableSummary");
     if(AgedPayablepanel==null){
         callAgedPayable();
         AgedPayablepanel = new Wtf.account.AgedDetail({
             id: 'AgedPayableSummary',
             border: false,
             isSummary:true,
             withinventory:withinventory,
             layout: 'fit',
             iconCls: 'accountingbase agedpayable',
             tabTip:WtfGlobal.getLocaleText("acc.rem.180"),
             title: WtfGlobal.getLocaleText("acc.agedPay.summaryView")  //'Summary View'
         });
         Wtf.getCmp('mainAgedPayable').add(AgedPayablepanel);
     }
     Wtf.getCmp('mainAgedPayable').setActiveTab(AgedPayablepanel);
     Wtf.getCmp('mainAgedPayable').doLayout();
 }

function call1099Report(){
    if(!WtfGlobal.EnableDisable(Wtf.UPerm.tax1099, Wtf.Perm.tax1099.tax1099dv) || !WtfGlobal.EnableDisable(Wtf.UPerm.tax1099, Wtf.Perm.tax1099.tax1099sv)) {
    var panel = Wtf.getCmp("main1099Report");
    if(panel==null){
        panel = new Wtf.TabPanel({
            title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.1099.tabTitle"),Wtf.TAB_TITLE_LENGTH),
            tabTip:WtfGlobal.getLocaleText("acc.1099.tabTitle"),  //'1099 Report',
            id:'main1099Report',
            withinventory:true,
            closable:true,
            border:false,
            iconCls:'accountingbase balancesheet',
            activeTab:0
        });
        Wtf.getCmp('as').add(panel);
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.tax1099, Wtf.Perm.tax1099.tax1099dv))
        	call1099DetailReport();
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.tax1099, Wtf.Perm.tax1099.tax1099sv))
        	call1099SummaryReport()
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
    }
   else
            WtfComMsgBox(46,0,false,"viewing Tax 1099 Report Report");
}


function call1099DetailReport(){
     var DetailReportpanel=Wtf.getCmp("1099DetailReport");
     if(DetailReportpanel==null){
//         call1099Report();
         DetailReportpanel = new Wtf.account.Tax1099DetailReport({
             id: '1099DetailReport',
             isexpenseinv:true,
             withinventory:true,
             helpmodeid:29,
             border: false,
             layout: 'fit',
             iconCls: 'accountingbase agedpayable',
             title: WtfGlobal.getLocaleText("acc.1099.detail"),  //'Detail View'
             tabTip:'View Details of Tax 1099 Accounts.'
         });
         Wtf.getCmp('main1099Report').add(DetailReportpanel);
         DetailReportpanel.on('journalentry',callJournalEntryDetails);
     }else{
     }
//     Wtf.getCmp('main1099Report').setActiveTab(DetailReportpanel);
     Wtf.getCmp('main1099Report').doLayout();
 }

function call1099SummaryReport(){
     var SummaryReportpanel=Wtf.getCmp("1099SummaryReport");
     if(SummaryReportpanel==null){
//         call1099Report();
         SummaryReportpanel = new Wtf.account.Tax1099SummaryReport({
             id: '1099SummaryReport',
             border: false,
             isexpenseinv:true,
             isSummary:true,
             withinventory:true,
             layout: 'fit',
             iconCls: 'accountingbase agedpayable',
             title: WtfGlobal.getLocaleText("acc.1099.summary"), //'Summary View'
             tabTip:'View Summary of Tax 1099 Accounts.'
         });
         Wtf.getCmp('main1099Report').add(SummaryReportpanel);
     }
     Wtf.getCmp('main1099Report').doLayout();
 }

function callCustomerDetails(personlinkid,openperson,withinventory){
    if(!WtfGlobal.EnableDisable(Wtf.UPerm.customer, Wtf.Perm.customer.view)) {
    var panel = Wtf.getCmp("mainCustomerDetails");
    if(panel==null){
        panel = new Wtf.TabPanel({
            title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.customerList.tabTitle"),Wtf.TAB_TITLE_LENGTH),
            tabTip:WtfGlobal.getLocaleText("acc.customerList.tabTitle"),  //'Accounts Receivable/Customer(s)',
            id:'mainCustomerDetails',
            withinventory:withinventory,
            closable:true,
            border:false,
            iconCls :getButtonIconCls(Wtf.etype.customer),
            activeTab:0
        });
        Wtf.getCmp('as').add(panel);
        callCustomerReport(personlinkid,openperson,withinventory);
        callCustomerByCategoryReport();
    } else {
        if(openperson){
            var CustomerReportpanel = Wtf.getCmp("CustomerDetails");
            callBusinessContactWindow(false, null, 'bcwin', CustomerReportpanel.isCustomer);
            Wtf.getCmp("bcwin").on('update',CustomerReportpanel.updateGrid, CustomerReportpanel);
        }
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
    if(Wtf.getCmp("CustomerDetails")!=null && personlinkid!=null)
         Wtf.getCmp("CustomerDetails").calllinkRowColor(personlinkid);
     }
   else
            WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.viewing")+" "+WtfGlobal.getLocaleText("acc.customerList.tabTitle"));
}
function callCustomerReport(personlinkid,openperson,withinventory){
    var panel = Wtf.getCmp("CustomerDetails");
    if(panel==null){
         panel = new Wtf.account.BusinessContactPanel({
            id : 'CustomerDetails',
            withinventory:withinventory,
            border : false,
            openperson:openperson,
            personlinkid:personlinkid,
            layout: 'fit',
            isCustomer:true,
            title:WtfGlobal.getLocaleText("acc.rem.14"),  //'Customer List',
            iconCls :getButtonIconCls(Wtf.etype.customer)
        });
        Wtf.getCmp('mainCustomerDetails').add(panel);
    }
    Wtf.getCmp('mainCustomerDetails').setActiveTab(panel);
    Wtf.getCmp('mainCustomerDetails').doLayout();

}
function callCustomerByCategoryReport(){
    var panel= Wtf.getCmp("CustomerByCategoryDetails");
    if(panel==null){
         panel= new Wtf.account.BusinessPersonListByCategory({
            id: 'CustomerByCategoryDetails',
            border: false,
            layout: 'fit',
            isCustomer: true,
            title: Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.customerList.tabTitleCategory"), Wtf.TAB_TITLE_LENGTH),
            tabTip: WtfGlobal.getLocaleText("acc.customerList.tabTitleCategory"),  //"Customer List By Category",
            iconCls: getButtonIconCls(Wtf.etype.customer)
        });
        Wtf.getCmp('mainCustomerDetails').add(panel);
    }
    Wtf.getCmp('mainCustomerDetails').doLayout();
}
function callVendorDetails(personlinkid,openperson,withinventory){
  // if(!WtfGlobal.EnableDisable(Wtf.UPerm.customer, Wtf.Perm.customer.view)) {
  if(!WtfGlobal.EnableDisable(Wtf.UPerm.vendor, Wtf.Perm.vendor.view)) {
    var panel = Wtf.getCmp("mainVendorDetails");
    if(panel==null){
        panel = new Wtf.TabPanel({
            title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.vendorList.tabTitle"),Wtf.TAB_TITLE_LENGTH),
            tabTip:WtfGlobal.getLocaleText("acc.vendorList.tabTitle"),  //'Accounts Payable/Vendor(s)',
            id:'mainVendorDetails',
            closable:true,
            withinventory:withinventory,
            border:false,
            iconCls:'accountingbase vendor',
            activeTab:0
        });
        Wtf.getCmp('as').add(panel);
        callVendorReport(personlinkid,openperson,withinventory);
        callVendorByCategoryReport();
    } else {
        if(openperson){
            var VendorReportpanel = Wtf.getCmp("VendorDetails");
            callBusinessContactWindow(false, null, 'bcwin', VendorReportpanel.isCustomer);
            Wtf.getCmp("bcwin").on('update',VendorReportpanel.updateGrid, VendorReportpanel);
        }
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
    if(Wtf.getCmp("VendorDetails")!=null && personlinkid!=null)
         Wtf.getCmp("VendorDetails").calllinkRowColor(personlinkid);
   }
   else
            WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.viewing")+" "+WtfGlobal.getLocaleText("acc.vendorList.tabTitle"));
}
function callVendorReport(personlinkid,openperson,withinventory){ 
    var VendorReportpanel = Wtf.getCmp("VendorDetails");
    if(VendorReportpanel==null){
         VendorReportpanel = new Wtf.account.BusinessContactPanel({
            id : 'VendorDetails',
            openperson:openperson,
            withinventory:withinventory,
            border : false,
            layout: 'fit',
            isCustomer:false,
            title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.vendorList.tab"),Wtf.TAB_TITLE_LENGTH),
            tabTip:WtfGlobal.getLocaleText("acc.vendorList.tab"), //'Vendor List',
            personlinkid:personlinkid,
           iconCls:'accountingbase vendor'
        });
        Wtf.getCmp('mainVendorDetails').add(VendorReportpanel);
    }
     Wtf.getCmp('mainVendorDetails').setActiveTab(VendorReportpanel);
     Wtf.getCmp('mainVendorDetails').doLayout();
}
function callVendorByCategoryReport(){
    var Reportpanel= Wtf.getCmp("VendorByCategoryDetails");
    if(Reportpanel==null){
         Reportpanel= new Wtf.account.BusinessPersonListByCategory({
            id: 'VendorByCategoryDetails',
            border: false,
            layout: 'fit',
            isCustomer: false,
            title: Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.vendorList.tabTitleCategory"), Wtf.TAB_TITLE_LENGTH),
            tabTip: WtfGlobal.getLocaleText("acc.vendorList.tabTitleCategory"), //"Vendor List By Category",
           iconCls:'accountingbase vendor'
        });
        Wtf.getCmp('mainVendorDetails').add(Reportpanel);
    }
     Wtf.getCmp('mainVendorDetails').doLayout();
}

function callGroupWindow(isEdit, rec, winid){
    winid=(winid==null?"groupWin":winid);
    var panel = Wtf.getCmp(winid);
    if(!panel){
        new Wtf.account.Group({
            isEdit:isEdit,
            record:rec,
            title:WtfGlobal.getLocaleText("acc.coa.coaAccountTypesTabTitle"),  //'Account Types',
            id:winid,
            closable: true,
            modal: true,
            iconCls :getButtonIconCls(Wtf.etype.deskera),
            width: 530,
            height:320,
            resizable: false,
            layout: 'border',
            buttonAlign: 'right',
            renderTo: document.body
        }).show();
    }
}

function callGroupReport(){
  if(!WtfGlobal.EnableDisable(Wtf.UPerm.groups, Wtf.Perm.groups.view)) {
     var panel = Wtf.getCmp("groupDetails");
    if(panel==null){
        callCOA();
        panel = new Wtf.account.GroupReport({
            id : 'groupDetails',
            border : false,
            layout: 'fit',
            title:WtfGlobal.getLocaleText("acc.coa.coaAccountTypesTabTitle"),  //'Account Types',
            iconCls:'accountingbase coa'
        });
        Wtf.getCmp('coa').add(panel);
    }
    Wtf.getCmp('coa').setActiveTab(panel);
    Wtf.getCmp('coa').doLayout();
      }
  else
            WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.viewing")+" "+WtfGlobal.getLocaleText("acc.coa.coaAccountTypesTabTitle"));
}

function callCurrencyExchangeWindow(winid){
    winid=(winid==null?"CurrencyExchangewin":winid);
    var panel = Wtf.getCmp(winid);
    if(!panel){
        new Wtf.account.CurrencyExchangeWindow({
            id:winid,
            closable: true,
            modal: true,
            iconCls :getButtonIconCls(Wtf.etype.deskera),
            width: 600,
            height: 400,
            resizable: false,
            layout: 'border',
            buttonAlign: 'right',
            renderTo: document.body
        }).show();
    }
}

function callCalculatorWindow(winid){
    winid=(winid==null?"Calculatorwin":winid);
    var panel = Wtf.getCmp(winid);
    if(!panel){
        new Wtf.calculatorWindow({
            id:winid,
            closable: true,
            iconCls :getButtonIconCls(Wtf.etype.deskera),
            width: 270,
            height: 210,
            resizable: true,
            layout: 'fit'
        }).show();
    }
}

function callTax1099Window(winid){
    winid=(winid==null?"tax1099win":winid);
    var panel = Wtf.getCmp(winid);
    if(!panel){
        new Wtf.account.Tax1099Window({
            id:winid,
            closable: true,
            modal: true,
            iconCls :getButtonIconCls(Wtf.etype.deskera),
            width: 600,
            height: 400,
            resizable: false,
            layout: 'border',
            buttonAlign: 'right',
            renderTo: document.body
        }).show();
    }
}
function callCurrencyExchangeDetails(winid,currencyid,title){
    winid=(winid==null?"CurrencyExchangeDetailswin":winid);
     title=(title==null?"Currency Exchange Details":title);
    var panel = Wtf.getCmp(winid);
    if(!panel){
        new Wtf.account.CurrencyExchangeWindow({
            id:winid,
            currencyhistory:true,
            currencyid:currencyid,
            closable: true,
            title:title,
            modal: true,
            iconCls :getButtonIconCls(Wtf.etype.deskera),
            width: 600,
            height: 400,
            resizable: false,
            layout: 'border',
            buttonAlign: 'right',
            renderTo: document.body
        }).show();
    }
}

function getExportUrl(mode) {
    var exportUrl = "../../export.jsp";
    switch(mode) {
        case 112:
            exportUrl = "ACCAccount/exportAccounts.do";
            break;
        case 1120:
            exportUrl = "ACCDepreciation/exportAccountDepreciation.do";
            break;
        case 113:
            exportUrl = "ACCCustomerCMN/exportCustomer.do";
            break;
        case 114:
            exportUrl = "ACCVendorCMN/exportVendor.do";
            break;
        case 115:
            exportUrl = "ACCReports/exportLedger.do";
            break;
        case 116:
            exportUrl = "ACCReports/exportTrialBalance.do";
            break;
        case 117:
            exportUrl = "ACCReports/exportLedger.do";
            break;
        case 150:
            exportUrl = "ACCProduct/exportCycleCount.do";
            break;
        case 151:
            exportUrl = "ACCProduct/exportCycleCountWorkSheet.do";
            break;
        case Wtf.autoNum.JournalEntry:
            exportUrl = "ACCJournal/exportJournalEntry.do";
            break;
        case Wtf.autoNum.Invoice:
            exportUrl = "ACCInvoiceCMN/exportInvoice.do";
            break;
        case Wtf.autoNum.PurchaseOrder:
            exportUrl = "ACCPurchaseOrderCMN/exportPurchaseOrder.do";
            break;
        case Wtf.autoNum.Payment:
            exportUrl = "ACCVendorPayment/exportPayment.do";
            break;
        case Wtf.autoNum.Receipt:
            exportUrl = "ACCReceipt/exportReceipt.do";
            break;
        case Wtf.autoNum.SalesOrder:
            exportUrl = "ACCSalesOrderCMN/exportSalesOrder.do";
            break;
        case Wtf.autoNum.GoodsReceipt:
            exportUrl = "ACCGoodsReceiptCMN/exportGoodsReceipt.do";
            break;
        case Wtf.autoNum.CreditNote:
            exportUrl = "ACCCreditNote/exportCreditNote.do";
            break;
        case Wtf.autoNum.DebitNote:
            exportUrl = "ACCDebitNote/exportDebitNote.do";
            break;
        case Wtf.autoNum.BillingSalesOrder:
            exportUrl = "ACCSalesOrderCMN/exportBillingSalesOrder.do";
            break;
        case Wtf.autoNum.BillingInvoice:
            exportUrl = "ACCInvoiceCMN/exportBillingInvoice.do";
            break;
        case Wtf.autoNum.BillingCreditNote:
            exportUrl = "ACCCreditNote/exportBillingCreditNote.do";
            break;
        case Wtf.autoNum.BillingReceipt:
            exportUrl = "ACCReceipt/exportBillingReceipt.do";
            break;
        case Wtf.autoNum.BillingPurchaseOrder:
            exportUrl = "ACCPurchaseOrderCMN/exportBillingPurchaseOrder.do";
            break;
        case Wtf.autoNum.BillingGoodsReceipt:
            exportUrl = "ACCGoodsReceiptCMN/exportBillingGoodsReceipts.do";
            break;
        case Wtf.autoNum.BillingDebitNote:
            exportUrl = "ACCDebitNote/exportBillingDebitNote.do";
            break;
        case Wtf.autoNum.BillingPayment:
            exportUrl = "ACCVendorPayment/exportBillingPayment.do";
            break;
        case Wtf.autoNum.AgedPayableWithInv:
            exportUrl = "ACCGoodsReceiptCMN/exportGoodsReceipts.do";
            break;
        case Wtf.autoNum.AgedPayableWithOutInv:
            exportUrl = "ACCGoodsReceiptCMN/exportBillingGoodsReceipts.do";
            break;
        case Wtf.autoNum.VendorAgedPayable:
            exportUrl = "ACCGoodsReceiptCMN/exportVendorAgedPayable.do";
            break;
        case Wtf.autoNum.ExportInvoices:
            exportUrl = "ACCInvoiceCMN/exportInvoices.do";
            break;
        case Wtf.autoNum.getBillingInvoices:
            exportUrl = "ACCInvoiceCMN/exportBillingInvoices.do";
            break;
        case Wtf.autoNum.CustomerAgedReceivable:
            exportUrl = "ACCInvoiceCMN/exportCustomerAgedReceivable.do";
            break;
        case Wtf.autoNum.BalanceSheet:
            exportUrl = "ACCReports/exportBalanceSheet.do";
            break;
        case Wtf.autoNum.TradingPnl:
            exportUrl = "ACCReports/exportTradingAndProfitLoss.do";
            break;
        case Wtf.autoNum.RatioAnalysis:
            exportUrl = "ACCReports/exportRatioAnalysis.do";
            break;
        case Wtf.autoNum.Quotation:
            exportUrl = "ACCSalesOrderCMN/exportQuotation.do";
            break;    
        case 911:
        	exportUrl = "ACCReports/exportCalculatedTax.do";
        	break;
        case 912:
        	exportUrl = "ACCReports/exportCostCenterSummary.do";
        	break;	
        case 913:
        	exportUrl = "ACCReports/exportSalesByItem.do";
        	break;
        case 914:
        	exportUrl = "ACCReports/exportDetailedSalesByItem.do";
        	break;
        case 915:
        	exportUrl = "ACCProduct/exportProduct.do";
        	break;	
    }
    return exportUrl;
}

function callRatioAnalysis(){
  if(!WtfGlobal.EnableDisable(Wtf.UPerm.qanalysis, Wtf.Perm.qanalysis.view)) {
    var panel = Wtf.getCmp("ratioanalysis");
    if(panel==null){
        panel = new Wtf.account.RatioAnalysis({
            id : 'ratioanalysis',
            border : false,
            layout: 'fit',
            title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.ra.tabTitle"), Wtf.TAB_TITLE_LENGTH),  //'Ratio Analysis',
            closable: true,
            iconCls:getButtonIconCls(Wtf.etype.ratioreport)
        });
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
      }
  else
            WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.viewing")+" "+" Ratio Analysis Report");
}

function callCashFlowStatement(){
	    var panel = Wtf.getCmp("cashFlow");
	    if(panel==null){
	        panel = new Wtf.account.CashFlowStatement({
	            id : 'cashFlow',
	            border : false,
	            layout: 'fit',
	            tabTip:WtfGlobal.getLocaleText("acc.dashboard.TT.cashFlowStatement"),
	            title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.dashboard.cashFlowStatement"), Wtf.TAB_TITLE_LENGTH),  //'Cash Flow Statement',
	            closable: true,
	            iconCls:'accountingbase receivepayment'
	        });
	        Wtf.getCmp('as').add(panel);
	    }
	    Wtf.getCmp('as').setActiveTab(panel);
	    Wtf.getCmp('as').doLayout();
}

function callBillingPurchaseReceipt(isEdit,rec,winid){
      if(!WtfGlobal.EnableDisable(Wtf.UPerm.invoice, Wtf.Perm.invoice.createinvoice)) {
    winid=(winid==null?'BillingPurchaseReceipt':winid);
    var panel = Wtf.getCmp(winid);
    if(panel==null){
        panel = new Wtf.account.TransactionPanel({
            id : winid,
            isEdit:isEdit,
            isCustBill:true,
            cash:true,
            isCustomer:false,
            record: rec,
            heplmodeid: 33, //This is help mode id
            label:'Purchase Receipt',
            title:WtfGlobal.getLocaleText("acc.accPref.autoCP"),  //'Cash Purchase',
            iconCls:'accountingbase invoice'
        });
        panel.on("activate", function(){
            if(Wtf.isIE7) {
                var northHt=(Wtf.isIE?240:210);
                var southHt=(Wtf.isIE?210:150);
                Wtf.getCmp(winid + 'southEastPanel').setHeight(southHt);
                Wtf.getCmp(winid + 'southEastPanel').setWidth(650);
                panel.NorthForm.setHeight(northHt);
                panel.southPanel.setHeight(southHt);
            }
            panel.doLayout();
        }, this);
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
  }
  else
             WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.creating")+" "+WtfGlobal.getLocaleText("acc.accPref.autoCP"));
}
function callBillingGoodsReceipt(isEdit,rec,winid){
    if(!WtfGlobal.EnableDisable(Wtf.UPerm.vendorinvoice, Wtf.Perm.vendorinvoice.createvendorinvoice)) {
        winid=(winid==null?'BillingGoodsReceipt':winid);
        var panel = Wtf.getCmp(winid);
        if(panel==null){
            panel = new Wtf.account.TransactionPanel({
                id : winid,
                isEdit:isEdit,
                isCustBill:true,
                isCustomer:false,
                record: rec,
                heplmodeid: 15, //This is help mode id
                label:'Vendor Invoice',
                title:WtfGlobal.getLocaleText("acc.accPref.autoVI"),  //'Vendor Invoice',
                closable: true,
                iconCls:'accountingbase invoice'
            });
            panel.on("activate", function(){
            if(Wtf.isIE7) {
                var northHt=(Wtf.isIE?240:210);
                var southHt=(Wtf.isIE?210:150);
                Wtf.getCmp(winid + 'southEastPanel').setHeight(southHt);
                Wtf.getCmp(winid + 'southEastPanel').setWidth(650);
                panel.NorthForm.setHeight(northHt);
                panel.southPanel.setHeight(southHt);
            }
                panel.doLayout();
            }, this);
            Wtf.getCmp('as').add(panel);
        }
        Wtf.getCmp('as').setActiveTab(panel);
        Wtf.getCmp('as').doLayout();
  }
  else
             WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.creating")+" "+WtfGlobal.getLocaleText("acc.accPref.autoVI"));
}

function callBillingGoodsReceiptList(id,check){
     if(!WtfGlobal.EnableDisable(Wtf.UPerm.vendorinvoice, Wtf.Perm.vendorinvoice.viewvendorinvoice)) {
    var panel = Wtf.getCmp("BillingGRList");
    if(panel==null){
        panel = getVendorInvoiceTab(true, "BillingGRList", WtfGlobal.getLocaleText("acc.grList.tabTitle"));
        Wtf.getCmp('as').add(panel);
        panel.on('journalentry',callJournalEntryDetails);
        panel.expandInvoice(id,check);
    }else{
        panel.expandInvoice(id,check);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
    }
  else
     WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.creating")+" "+WtfGlobal.getLocaleText("acc.accPref.autoVI"));
}

function callBillingPayment(directPayment, invoiceRecord){
    if(!WtfGlobal.EnableDisable(Wtf.UPerm.vendorinvoice, Wtf.Perm.vendorinvoice.createpayment)) {
    var panel = Wtf.getCmp("billingpayment");
    if(panel!=null){
         Wtf.getCmp('as').remove(panel);
        panel.destroy();
        panel=null;
    }
    if(panel==null){
        panel = new Wtf.account.OSDetailPanel({
            id : 'billingpayment',
            border : false,
            isReceipt:false,
            isCustBill:true,
            cls: 'paymentFormPayMthd',
            directPayment : directPayment,
            invoiceRecord : invoiceRecord,
            layout: 'border',
            helpmodeid: 10, //This is help mode id
            title:WtfGlobal.getLocaleText("acc.accPref.autoMP"), //'Make Payment',
            iconCls:'accountingbase makepayment',
            closable: true
//            vendorid: getCmp("account"+config.helpmodeid+this.id).getValue()       // Done by Neeraj
        });
        panel.on("activate", function(){
            panel.doLayout();
            Wtf.getCmp(panel.id+"wrapperPanelNorth").doLayout();
        }, this);
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
      }
  else
     WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.accPref.autoMP"));
}

function callBillingPaymentReport(){
if(!WtfGlobal.EnableDisable(Wtf.UPerm.vendorinvoice, Wtf.Perm.vendorinvoice.viewpayment)) {
    var panel = Wtf.getCmp("paymentBillingReport");
    if(panel==null){
        panel = new Wtf.account.ReceiptReport({
            id : 'paymentBillingReport',
            border : false,
             helpmodeid: 23,
            layout: 'fit',
            isCustBill:true,
            title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.pmList.tabTitle"),Wtf.TAB_TITLE_LENGTH),
            tabTip:WtfGlobal.getLocaleText("acc.pmList.tabTitle"),  //'Payment Made Report',
            closable: true,
            isReceipt:false,
            iconCls:'accountingbase makepaymentreport'
        });
        Wtf.getCmp('as').add(panel);
        panel.on('journalentry',callJournalEntryDetails);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
       }
  else
     WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.viewing")+" "+WtfGlobal.getLocaleText("acc.pmList.tabTitle"));
}

function callBillingSalesOrder(isEdit,rec,winid){
    winid=(winid==null?'bsalesorder':winid);
 if(!WtfGlobal.EnableDisable(Wtf.UPerm.invoice, Wtf.Perm.invoice.createso)) {
    var label='Sales Order';
    var panel = Wtf.getCmp(winid);
    if(panel==null){
        panel = new Wtf.account.TransactionPanel({
            id : winid,
            isEdit: isEdit,
            record: rec,
            isCustomer:true,
            isCustBill:true,
            isOrder:true,
            label:'Order',
            border : false,
            heplmodeid: 11,
//            layout: 'border',
            title:Wtf.util.Format.ellipsis(((isEdit?WtfGlobal.getLocaleText("acc.wtfTrans.eso"):WtfGlobal.getLocaleText("acc.wtfTrans.so"))+" "+((rec != null)?rec.data.billno:"")),Wtf.TAB_TITLE_LENGTH),
            closable: true,
            iconCls:'accountingbase salesorder'
        });
        panel.on("activate", function(){
            if(Wtf.isIE7) {
                var northHt=(Wtf.isIE?150:180);
                var southHt=(Wtf.isIE?210:150);
                Wtf.getCmp(winid + 'southEastPanel').setHeight(southHt);
                Wtf.getCmp(winid + 'southEastPanel').setWidth(650);
                panel.NorthForm.setHeight(northHt);
                panel.southPanel.setHeight(southHt);
            }
            panel.doLayout();
        }, this);
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    panel.on('update',  function(){if(isEdit == true){Wtf.getCmp("bSalesOrderList").loadStore();Wtf.getCmp('as').remove(panel);}}, this);
    Wtf.getCmp('as').doLayout();
        }
  else
     WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.creating")+" "+WtfGlobal.getLocaleText("acc.wtfTrans.so"));
}

function callBillingPurchaseOrder(isEdit,rec,winid){
    winid=(winid==null?'bpurchaseorder':winid);
 if(!WtfGlobal.EnableDisable(Wtf.UPerm.vendorinvoice, Wtf.Perm.vendorinvoice.createpo)) {
    var label='Purchase Order';
    var panel = Wtf.getCmp(winid);
    if(panel==null){
        panel = new Wtf.account.TransactionPanel({
            id : winid,
            isEdit:isEdit,
            record:(rec!=null)?rec:"",
            isCustomer:false,
            isCustBill:true,
            isOrder:true,
            label:'Order',
            border : false,
//            layout: 'border',
            title:Wtf.util.Format.ellipsis(((isEdit?WtfGlobal.getLocaleText("acc.wtfTrans.epo"):WtfGlobal.getLocaleText("acc.accPref.autoPO"))+" "+((rec != null)?rec.data.billno:"")),Wtf.TAB_TITLE_LENGTH),
            heplmodeid: 12,
            closable: true,
            iconCls:'accountingbase purchaseorder'
        });
        panel.on("activate", function(){
            if(Wtf.isIE7) {
                var northHt=(Wtf.isIE?150:180);
                var southHt=(Wtf.isIE?210:150);
                Wtf.getCmp(winid + 'southEastPanel').setHeight(southHt);
                Wtf.getCmp(winid + 'southEastPanel').setWidth(650);
                panel.NorthForm.setHeight(northHt);
                panel.southPanel.setHeight(southHt);
            }
            panel.doLayout();
        }, this);
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    panel.on('update',  function(){if(isEdit == true){Wtf.getCmp("bPurchaseOrderList").loadStore();Wtf.getCmp('as').remove(panel);}}, this);
    Wtf.getCmp('as').doLayout();
          }
  else
     WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.creating")+" "+WtfGlobal.getLocaleText("acc.accPref.autoPO"));
}

function callBillingPurchaseOrderList(){
 if(!WtfGlobal.EnableDisable(Wtf.UPerm.vendorinvoice, Wtf.Perm.vendorinvoice.viewpo)) {
    var panel = Wtf.getCmp("bPurchaseOrderList");
    if(panel==null){
        panel = getPOTab(true, "bPurchaseOrderList", WtfGlobal.getLocaleText("acc.poList.tabTitle"));
        Wtf.getCmp('as').add(panel);
        panel.on('journalentry',callJournalEntryDetails);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
            }
  else
     WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.viewing")+" "+WtfGlobal.getLocaleText("acc.poList.tabTitle"));
}

function callBillingSalesOrderList(){
     if(!WtfGlobal.EnableDisable(Wtf.UPerm.invoice, Wtf.Perm.invoice.viewso)) {
    var panel = Wtf.getCmp("bSalesOrderList");
    if(panel==null){
        panel = getSOTab(true, "bSalesOrderList", WtfGlobal.getLocaleText("acc.soList.tabTitle"));
        Wtf.getCmp('as').add(panel);
        panel.on('journalentry',callJournalEntryDetails);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
            }
  else
     WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.viewing")+" "+WtfGlobal.getLocaleText("acc.soList.tabTitle"));
}

function callBillingCreditNote(){
         if(!WtfGlobal.EnableDisable(Wtf.UPerm.invoice, Wtf.Perm.invoice.createcn)) {
     var panel = Wtf.getCmp("bCreditNote");
         if(panel!=null){
             Wtf.getCmp('as').remove(panel);
            panel.destroy();
            panel=null;
        }
            if(panel==null){
                panel = new Wtf.account.TrNotePanel({
                    id : 'bCreditNote',
                    border : false,
                    layout: 'fit',
                    helpmodeid:13,
                    isCN:true,
                    isCustBill:true,
                    title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.accPref.autoCN"),Wtf.TAB_TITLE_LENGTH) ,
                    tabTip:WtfGlobal.getLocaleText("acc.accPref.autoCN"),  //'Credit Note',
                    closable: true,
                    iconCls:'accountingbase creditnote'
                });
                panel.on("activate", function(){
                    panel.doLayout();
                }, this);
                Wtf.getCmp('as').add(panel);
            }
            Wtf.getCmp('as').setActiveTab(panel);
            Wtf.getCmp('as').doLayout();
      }
      else
     WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.creating")+" "+WtfGlobal.getLocaleText("acc.accPref.autoCN"));
}

function callBillingDebitNote(){
   if(!WtfGlobal.EnableDisable(Wtf.UPerm.vendorinvoice, Wtf.Perm.vendorinvoice.createdn)) {
     var panel = Wtf.getCmp("bDebitNote");
         if(panel!=null){
             Wtf.getCmp('as').remove(panel);
            panel.destroy();
            panel=null;
        }
            if(panel==null){
                panel = new Wtf.account.TrNotePanel({
                    id : 'bDebitNote',
                    border : false,
                    layout: 'fit',
                    isCN:false,
                    isCustBill:true,
                    title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.accPref.autoDN"),Wtf.TAB_TITLE_LENGTH),
                    tabTip:WtfGlobal.getLocaleText("acc.accPref.autoDN"),  //'Debit Note',
                     helpmodeid:14,
                    iconCls:'accountingbase debitnote',
                    closable: true
                });
                panel.on("activate", function(){
                    panel.doLayout();
                }, this);
                Wtf.getCmp('as').add(panel);
            }
            Wtf.getCmp('as').setActiveTab(panel);
            Wtf.getCmp('as').doLayout();
      }
      else
     WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.creating")+" "+WtfGlobal.getLocaleText("acc.accPref.autoDN"));
}

function callBillingCreditNoteDetails(){
         if(!WtfGlobal.EnableDisable(Wtf.UPerm.invoice, Wtf.Perm.invoice.viewcn)) {
    var panel = Wtf.getCmp("bCreditNoteDetails");
    if(panel==null){
         panel = getCNTab(true, "bCreditNoteDetails", WtfGlobal.getLocaleText("acc.cnList.tabTitle"));
         Wtf.getCmp('as').add(panel);
        panel.on('journalentry',callJournalEntryDetails);
    }
     Wtf.getCmp('as').setActiveTab(panel);
     Wtf.getCmp('as').doLayout();
      }
      else
     WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.viewing")+" "+WtfGlobal.getLocaleText("acc.cnList.tabTitle"));
}

function callBillingDebitNoteDetails(){
    if(!WtfGlobal.EnableDisable(Wtf.UPerm.vendorinvoice, Wtf.Perm.vendorinvoice.viewdn)) {
        var panel = Wtf.getCmp("bDebitNoteDetails");
        if(panel==null){
            panel = getDNTab(true, "bDebitNoteDetails", WtfGlobal.getLocaleText("acc.dnList.tabTitle"));
            Wtf.getCmp('as').add(panel);
            //panel.on('goodsreceipt', callGoodsReceiptDetails);
            panel.on('journalentry',callJournalEntryDetails);
        }
        Wtf.getCmp('as').setActiveTab(panel);
        Wtf.getCmp('as').doLayout();
   }
   else
     WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.viewing")+" "+WtfGlobal.getLocaleText("acc.dnList.tabTitle"));
}

function callViewBillingPaymentReceipt(rec,type,winid){
    winid=(winid==null?'ViewbPaymentReceipt':winid);
    var panel = Wtf.getCmp(winid);
     var label="Cash Purchase";
    if(panel==null){
        panel = new Wtf.account.TransectionTemplate({
            id : winid,
            isCustomer:false,
            readOnly:true,
            isCustBill:true,
            rec: rec,
            label:label,
            title:Wtf.util.Format.ellipsis(('View '+label),Wtf.TAB_TITLE_LENGTH),
            tabTip:'View '+label,
            iconCls:'accountingbase viewgoodsreceipt'
        });
        panel.on("activate", function(){
            panel.doLayout();
        }, this);
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    panel.refreshView(rec);
    Wtf.getCmp('as').doLayout();
}
function callViewBillingGoodsReceipt(rec,type,winid,inCash){
    winid=(winid==null?'ViewbGoodsReceipt':winid);
    var panel = Wtf.getCmp(winid);
     var label=inCash?"Cash Purchase":"Vendor Receipt";
    if(panel==null){
        panel = new Wtf.account.TransectionTemplate({
            id : winid,
            isCustomer:false,
            isCustBill:true,
            readOnly:true,
            rec: rec,
            label:label,
            title:Wtf.util.Format.ellipsis(('View '+label),Wtf.TAB_TITLE_LENGTH),
            tabTip:'View '+label,
            iconCls:'accountingbase viewgoodsreceipt'
        });
        panel.on("activate", function(){
            panel.doLayout();
        }, this);
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    panel.refreshView(rec);
    Wtf.getCmp('as').doLayout();
}

function callViewBillingCreditNote(rec,type,winid){
    winid=(winid==null?'ViewCreditNote':winid);
    var panel = Wtf.getCmp(winid);
    if(panel==null){
        panel = new Wtf.account.TransectionTemplate({
            id : winid,
            isCustomer:true,
            readOnly:true,
            rec: rec,
            isCustBill:true,
            label:'Credit Note',
            title:WtfGlobal.getLocaleText("acc.cnList.tabTitle"),  //'View Credit Note',
            iconCls:'accountingbase viewcreditnote'
        });
        panel.on("activate", function(){
            panel.doLayout();
        }, this);
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    panel.refreshView(rec);
    Wtf.getCmp('as').doLayout();
}
function callViewBillingDebitNote(rec,type,winid){
    winid=(winid==null?'ViewDebitNote':winid);
    var panel = Wtf.getCmp(winid);
    if(panel==null){
        panel = new Wtf.account.TransectionTemplate({
            id : winid,
            isCustomer:false,
            isCustBill:true,
            readOnly:true,
            rec: rec,
            label:'Debit Note',
            title:WtfGlobal.getLocaleText("acc.dnList.tabTitle"),  //'View Debit Note',
            iconCls:'accountingbase viewdebitnote'
        });
        panel.on("activate", function(){
            panel.doLayout();
        }, this);
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    panel.refreshView(rec);
    Wtf.getCmp('as').doLayout();
}

function getInvoiceTab(isWithOutInventory, tabId, tabTitle, extraFilters, isCash){
    var reportPanel = new Wtf.account.TransactionListPanel({
        id : tabId,
        border : false,
        isCustBill: isWithOutInventory,
        title: Wtf.util.Format.ellipsis(tabTitle, Wtf.TAB_TITLE_LENGTH),
        tabTip: tabTitle,
        extraFilters: extraFilters,
        layout: 'fit',
        isCash: isCash,
        isCustomer:true,
        helpmodeid: 16,
        label:WtfGlobal.getLocaleText("acc.accPref.autoInvoice"),  //"Invoice",
        isOrder:false,
        closable: false,
        iconCls:'accountingbase invoicelist'
    });
    return reportPanel;
}
function getVendorInvoiceTab(isWithOutInventory, tabId, tabTitle, extraFilters){
    var reportPanel = new Wtf.account.TransactionListPanel({
        id : tabId,
        border : false,
        isOrder:false,
        isCustBill: isWithOutInventory,
        extraFilters: extraFilters,
        title: Wtf.util.Format.ellipsis(tabTitle, Wtf.TAB_TITLE_LENGTH),
        tabTip: tabTitle,
        label:WtfGlobal.getLocaleText("acc.accPref.autoVI"),  //'Vendor Invoice',
        layout: 'fit',
        helpmodeid:21,
        isCustomer: false,
        closable: true,
        iconCls:'accountingbase invoicelist'
    });
    return reportPanel;
}
function getSOTab(isWithOutInventory, tabId, tabTitle, extraFilters){
    var reportPanel = new Wtf.account.TransactionListPanel({
        id : tabId,
        border : false,
        isOrder:true,
        isCustomer:true,
        isCustBill: isWithOutInventory,
        title: Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.soList.tabTitle"), Wtf.TAB_TITLE_LENGTH),
        tabTip: tabTitle,
        extraFilters: extraFilters,
        label:WtfGlobal.getLocaleText("acc.accPref.autoSO"),  //'Sales Order',
        helpmodeid:18,
        layout: 'fit',
        closable: true,
        iconCls:'accountingbase salesorderlist'
    });
    return reportPanel;
}
function getPOTab(isWithOutInventory, tabId, tabTitle, extraFilters){
    var reportPanel = new Wtf.account.TransactionListPanel({
        id : tabId,
        border : false,
        isOrder:true,
        isCustomer:false,
        isCustBill: isWithOutInventory,
        title: Wtf.util.Format.ellipsis(tabTitle, Wtf.TAB_TITLE_LENGTH),
        tabTip: tabTitle,
        extraFilters: extraFilters,
        label:WtfGlobal.getLocaleText("acc.accPref.autoPO"),  //'Purchase Order',
        helpmodeid:17,
        layout: 'fit',
        closable: true,
        iconCls:'accountingbase purchaseorderlist'
    });
    return reportPanel;
}
function getCNTab(isWithOutInventory, tabId, tabTitle, extraFilters){
    var reportPanel = new Wtf.account.NoteDetailsPanel({
        id : tabId,
        border : false,
        layout: 'fit',
        helpmodeid:19,
        isCNReport:true,
        title: Wtf.util.Format.ellipsis(tabTitle, Wtf.TAB_TITLE_LENGTH),
        tabTip: tabTitle,
        isCustBill: isWithOutInventory,
        extraFilters: extraFilters,
        closable: true,
        iconCls:'accountingbase creditnotereport'
    });
    return reportPanel;
}
function getDNTab(isWithOutInventory, tabId, tabTitle, extraFilters){
    var reportPanel = new Wtf.account.NoteDetailsPanel({
        id : tabId,
        border : false,
        isCNReport:false,
        isCustBill: isWithOutInventory,
        title: Wtf.util.Format.ellipsis(tabTitle, Wtf.TAB_TITLE_LENGTH),
        tabTip: tabTitle,
        extraFilters: extraFilters,
        helpmodeid:22,
        layout: 'fit',
        closable: true,
        iconCls:'accountingbase debitnotereport'
    });
    return reportPanel;
}
function getJETab(tabId, tabTitle, jeId, extraFilters){
    var reportPanel = new Wtf.account.JournalEntryDetailsPanel({
        id : tabId,
        border : false,
        helpmodeid: 24,
        layout: 'fit',
        title: Wtf.util.Format.ellipsis(tabTitle, Wtf.TAB_TITLE_LENGTH),
        tabTip: tabTitle,
        label : tabTitle,
        extraFilters: extraFilters,
        entryID:jeId,
        closable: true,
        iconCls:'accountingbase journalentryreport'
    });
    reportPanel.expandJournalEntry();
    return reportPanel;
}
function callTaxReport(){
    var panel = Wtf.getCmp("maintaxreport");
    if(panel==null){
        panel = new Wtf.TabPanel({
            title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.taxReport.taxReport"),Wtf.TAB_TITLE_LENGTH) ,
            tabTip:WtfGlobal.getLocaleText("acc.taxReport.taxReportTT"), //'You can view your Purchase and Sales Tax reports here.',
            id:'maintaxreport',
            closable:true,
            border:false,
            iconCls:'accountingbase agedrecievable',
            activeTab:0
        });
        Wtf.getCmp('as').add(panel);
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.taxreport, Wtf.Perm.taxreport.viewpt))
        	callTaxCalculation(false);
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.taxreport, Wtf.Perm.taxreport.viewst))
        	callTaxCalculation(true);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
}

function callTaxCalculation(isSales){
    var id=(isSales?'sales':'purchase')+"TaxCalculation"
    var taxCalculation=Wtf.getCmp(id);
     if(taxCalculation==null){
         taxCalculation = new Wtf.account.sampleTaxCalculation({
             id: id,
             border: false,
             isSales:isSales,
             layout: 'fit',
             iconCls: 'accountingbase agedpayable',
             title: isSales?WtfGlobal.getLocaleText("acc.taxReport.salesTax"):WtfGlobal.getLocaleText("acc.taxReport.purchaseTax"),
             tabTip:isSales?WtfGlobal.getLocaleText("acc.taxReport.salesTaxTT"):WtfGlobal.getLocaleText("acc.taxReport.purchaseTaxTT")
         });
         Wtf.getCmp('maintaxreport').add(taxCalculation);
     }else{
     }
//     Wtf.getCmp('maintaxreport').setActiveTab(taxCalculation);
     Wtf.getCmp('maintaxreport').doLayout();
}


function callsaleByItemReport(){
     var saleByItemReport=Wtf.getCmp("saleByItemReport");
     if(saleByItemReport==null){
         callSaleByItem();
         saleByItemReport = new Wtf.account.SalesByItemDetail({
             id: 'saleByItemReport',
             border: false,
             layout: 'fit',
             iconCls: 'accountingbase agedpayable',
             title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.saleByItem.detailReport"),Wtf.TAB_TITLE_LENGTH) ,
             tabTip:WtfGlobal.getLocaleText("acc.saleByItem.detailReportTT")  //'View your Sales By Item Detail Report from here.'
         });
         Wtf.getCmp('mainsalebyitem').add(saleByItemReport);
         if(!WtfGlobal.EnableDisable(Wtf.UPerm.invoice, Wtf.Perm.invoice.viewinvoice))
            saleByItemReport.on('invoice',callInvoiceList);
     }else{
     }
     Wtf.getCmp('mainsalebyitem').setActiveTab(saleByItemReport);
     Wtf.getCmp('mainsalebyitem').doLayout();
 }

 function callsaleByItemSummary(){
     var saleByItem=Wtf.getCmp("saleByItem");
     if(saleByItem==null){
         callSaleByItem()
         saleByItem = new Wtf.account.SalesByItem({
             id: 'saleByItem',
             border: false,
             layout: 'fit',
             iconCls: 'accountingbase agedpayable',
             title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.saleByItem.summaryReport"),Wtf.TAB_TITLE_LENGTH) ,
             tabTip:WtfGlobal.getLocaleText("acc.saleByItem.summaryReportTT")  //'View your Sales By Item Summary Report from here.'
         });
         Wtf.getCmp('mainsalebyitem').add(saleByItem);
     }else{
     }
     Wtf.getCmp('mainsalebyitem').setActiveTab(saleByItem);
     Wtf.getCmp('mainsalebyitem').doLayout();
 }

function callSaleByItem(){
    var panel = Wtf.getCmp("mainsalebyitem");
    if(panel==null){
        panel = new Wtf.TabPanel({
            title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.saleByItem.tabTitle"),Wtf.TAB_TITLE_LENGTH) ,
            tabTip:WtfGlobal.getLocaleText("acc.saleByItem.tabTitleTT"), //'View your Item Sales report in Summary and Details from here.',
            id:'mainsalebyitem',
            closable:true,
            border:false,
            iconCls:'accountingbase agedrecievable',
            activeTab:1
        });
        Wtf.getCmp('as').add(panel);
        callsaleByItemSummary();
        callsaleByItemReport();

    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp("mainsalebyitem").setActiveTab("saleByItem");
    Wtf.getCmp('as').doLayout();
}

function callDataSink(){
    var panel = Wtf.getCmp("dataSink");
    if(panel==null){
        panel = new Wtf.DataSink({
            id : 'dataSink',
            border : false,
            layout: 'fit',
            title:Wtf.util.Format.ellipsis('Sink Data',Wtf.TAB_TITLE_LENGTH) ,
            tabTip:'Sink Data',
            closable: true,
            iconCls:'accountingbase creditnote'
        });
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
}

function callCostCenterReport(){
    var panel = Wtf.getCmp("CostCenterReport");
    if(panel==null){
        panel = new Wtf.TabPanel({
            title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.ccReport.tabTitle"),Wtf.TAB_TITLE_LENGTH),
            tabTip:WtfGlobal.getLocaleText("acc.ccReport.Tip4"),  //'You can view the Summary, Details and Transactions associated with different Cost Center(s) in your organization from here.',
            id:'CostCenterReport',
            closable:true,
            iconCls:'accountingbase balancesheet',
            activeTab:0
        });
        Wtf.getCmp('as').add(panel);
    }
    if(!WtfGlobal.EnableDisable(Wtf.UPerm.costcenter, Wtf.Perm.costcenter.viewccs))
    	var allSummary = callAllCostCenterSummary();
    if(!WtfGlobal.EnableDisable(Wtf.UPerm.costcenter, Wtf.Perm.costcenter.viewccd))
    	var reportDetails = callCostCenterDetailsReport();
    if(!WtfGlobal.EnableDisable(Wtf.UPerm.costcenter, Wtf.Perm.costcenter.viewcct))
    	var transactionDetails = callCostCenterReportTransactionDetails();
    
    if(!WtfGlobal.EnableDisable(Wtf.UPerm.costcenter, Wtf.Perm.costcenter.viewccd)){
	    reportDetails.on("activate",function(panel){
	        panel.westPanel.setWidth(panel.getInnerWidth()/2);
	        panel.doLayout();
	        if(!panel.LoadedOnActivate){
	            panel.fetchStatement();
	            panel.LoadedOnActivate = true;
	        }
	    });
	}

    if(!WtfGlobal.EnableDisable(Wtf.UPerm.costcenter, Wtf.Perm.costcenter.viewccs))
    	Wtf.getCmp('CostCenterReport').setActiveTab(allSummary);
    Wtf.getCmp('CostCenterReport').doLayout();

    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
}
function callCostCenterDetailsReport(){
    var panel = Wtf.getCmp("CostCenterSummary");
    if(panel==null){
          panel = new Wtf.account.FinalStatement({
                id : 'CostCenterSummary',
                title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.ccReport.tab2"),Wtf.TAB_TITLE_LENGTH),
                tabTip:WtfGlobal.getLocaleText("acc.ccReport.Tip3"),  //'View Details Report of different Cost Center(s) in your organization from here.',
                topTitle:'<center><font size=4>Cost Center Details Report</font></center>',
                statementType:'CostCenter',
                border : false,
                closable: false,
                layout: 'fit',
                iconCls:'accountingbase balancesheet'
          });
        Wtf.getCmp('CostCenterReport').add(panel);
    }
    return panel;
}
function callCostCenterReportTransactionDetails(){
    var panel = Wtf.getCmp("CostCenterDetails");
    if(panel==null){
          panel = new Wtf.CostCenterDetailsTab({
                id : "CostCenterDetails",
                title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.ccReport.tab3"),Wtf.TAB_TITLE_LENGTH),
                tabTip:WtfGlobal.getLocaleText("acc.ccReport.Tip2"),  //'View Tansactions Report of different Cost Center(s) in your organization from here.',
                topTitle:'<center><font size=4>Cost Center Transactions Report</font></center>',
                border : false,
                closable: false,
                iconCls:'accountingbase balancesheet'
          });
        Wtf.getCmp('CostCenterReport').add(panel);
    }
    return panel;
}
function callAllCostCenterSummary(){
    var panel = Wtf.getCmp("allCostCenterSummary");
    if(panel==null){
          panel = new Wtf.CostCenterSummaryTab({
                id : "allCostCenterSummary",
                title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.ccReport.tab1"),Wtf.TAB_TITLE_LENGTH),
                tabTip:WtfGlobal.getLocaleText("acc.ccReport.Tip1"),  //'View Summary Report of different Cost Center(s) in your organization from here.',
                topTitle:'<center><font size=4>Cost Center Summary Report</font></center>',
                border : false,
                closable: false,
                iconCls:'accountingbase balancesheet'
          });
        Wtf.getCmp('CostCenterReport').add(panel);
    }
    return panel;
}
