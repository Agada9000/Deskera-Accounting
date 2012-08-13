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


Wtf.grid.DepCheckColumn = function(config){
    Wtf.apply(this, config);
    if(!this.id)
        this.id = Wtf.id();
    this.renderer = this.renderer.createDelegate(this);
};
Wtf.grid.DepCheckColumn.prototype ={
    fyear:0,
    byear:0,
    fdate:0,
    bdate:0,
    init : function(grid){
        this.grid = grid;
        this.grid.on('render', function(){
            var view = this.grid.getView();
            view.mainBody.on('mousedown', this.onMouseDown, this);
        }, this);
    },
    onMouseDown : function(e, t){
        if(t.className && t.className.indexOf('x-grid3-cc-'+this.id) != -1){
           e.stopEvent();
            var index = this.grid.getView().findRowIndex(t);
            var record = this.grid.store.getAt(index);
            if(record.data.frommonth<new Date()){
            var prevrecord = this.grid.store.getAt(index-1);
            if(record.data.depdetailid==""&&((index-1>=0&&prevrecord.get(this.dataIndex)==true)||index==0))
                if(record.get(this.dataIndex)==true){
                    for(var i=index;i<this.grid.store.getCount();i++){
                        var afterrecord = this.grid.store.getAt(i);
                        afterrecord.set(this.dataIndex, false);
                    }
                }
                else
                    record.set(this.dataIndex, !record.data[this.dataIndex]);
            } else
                    record.set(this.dataIndex, record.data[this.dataIndex]);
        }
    },
    renderer : function(v, p, record){
        p.css += ' x-grid3-check-col-td';
        return '<div class="x-grid3-check-col'+(v?'-on':'')+' x-grid3-cc-'+this.id+'">&#160;</div>';
    }
};

function openAccRecTab(isReceivable){
    if(isReceivable)
        callCustomerDetails();
    else
        callVendorDetails();
}
function openFixedAssetTab(){
   callFixedAsset();
}
Wtf.account.COAReport = function(config){
    this.accID="";
    this.isAdd=false;
    this.recArr=[];
    this.isDepreciation=config.isDepreciation;
    this.isFixedAsset=config.isFixedAsset;
    this.costCenterId = config.costCenterId?config.costCenterId:"";
    this.isEdit=false;
    this.accid=config.accid;
    this.record=config.record;
    this.nondeleted=false;
    this.deleted=false;
    this.BalanceSummary=0; //Summary Amount In Base Currency
    this.uPermType=((config.isFixedAsset||config.isDepreciation)?Wtf.UPerm.fixedasset:Wtf.UPerm.coa);
    this.permType=((config.isFixedAsset||config.isDepreciation)?Wtf.Perm.fixedasset:Wtf.Perm.coa);
    this.typeEditor = new Wtf.form.ComboBox({
        store: Wtf.delTypeStore,
        name:'typeid',
        displayField:'name',
        id:'view'+config.helpmodeid,
        valueField:'typeid',
        mode: 'local',
        value:0,
        width:110,
        listWidth:110,
        triggerAction: 'all',
        typeAhead:true,
        selectOnFocus:true
    });
    this.createStore();
    this.createColumnModel();
    this.createGrid();
    this.store.on('load',this.hideMsg,this)
    Wtf.account.COAReport.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.account.COAReport, Wtf.Panel,{
    onRender: function(config){
        Wtf.account.COAReport.superclass.onRender.call(this, config);
        this.add(this.grid);
    },
    hideMsg:function(){
        Wtf.MessageBox.hide();
    },
    createStore:function(){
        this.coaRec = new Wtf.data.Record.create([
            {name: 'accid'},
            {name: 'accname'},
            {name: 'groupid'},
            {name: 'groupname'},
            {name: 'level'},
            {name: 'leaf'},
            {name: 'openbalance'},
            {name: 'openbalanceinbase'},
            {name: 'presentbalance'},
            {name: 'presentbalanceInBase'},
            {name: 'salvage'},
            {name: 'life'},
            {name: 'parentid'},
            {name: 'currencysymbol'},
            {name: 'currencyname'},
            {name: 'currencyid'},
            {name: 'parentname'},
            {name: 'depreciationaccount'},
            {name: 'period'},
            {name: 'frommonth', type:'date'},
            {name: 'tomonth', type:'date'},
            {name: 'firstperiodamt'},
            {name: 'posted', type:'boolean'},
            {name: 'perioddepreciation'},
            {name: 'accdepreciation'},
            {name: 'creationDate' ,type:'date'},
            {name: 'netbookvalue'},
            {name: 'depdetailid'},
            {name: 'deleted'},
            {name: 'isje'},
            {name: 'categoryid'},
            {name: 'costcenterid'},
            {name: 'costcenterName'},
            {name: 'id'}
        ]);
        this.msgLmt = 30;
        this.jReader = new Wtf.data.KwlJsonReader({
            totalProperty: 'totalCount',
            root: "data"
        }, this.coaRec);

        var baseParamsArr = {};
        baseParamsArr.mode=this.isDepreciation?4:2;
//        baseParamsArr.ignorecustomers=false;
//        baseParamsArr.ignorevendors=false;
        if(this.isFixedAsset){
          baseParamsArr.ignorecustomers=true;
          baseParamsArr.ignorevendors=true;
            baseParamsArr.group=[12];
            baseParamsArr.costCenterId = this.costCenterId;
        } else if(this.isDepreciation) {
            baseParamsArr.accid=this.accid;
        } else {
            baseParamsArr.ignore=true;
            baseParamsArr.group=[12];
        }

        this.store = new Wtf.data.Store({
            reader: this.jReader,
//            url: Wtf.req.account +'CompanyManager.jsp',
            url: this.isDepreciation?"ACCDepreciation/getAccountDepreciation.do":"ACCAccount/getAccounts.do",
            baseParams:baseParamsArr
        });
        this.store.on('beforeload',function(s,o){
            if(!o.params)o.params={};
            o.params.deleted=this.deleted;
            o.params.nondeleted=this.nondeleted;
        },this);
        this.store.load({
            params: {
                start: 0,
                limit: (this.pageLimit && this.pageLimit.combo) ? (this.pageLimit.combo.getValue() || this.msgLmt) : this.msgLmt
            }
        });
       WtfComMsgBox(29,4,true);
    },

    createColumnModel:function(){
        this.summary = new Wtf.ux.grid.GridSummary();
        this.selectionModel = new Wtf.grid.CheckboxSelectionModel({
        	singleSelect: this.isFixedAsset,
            	header: (Wtf.isIE7)?"":'<div class="x-grid3-hd-checker"> </div>',    // For IE 7 the all select option not available
        	hidden:this.isDepreciation
        });
        this.gridcm= new Wtf.grid.ColumnModel([this.selectionModel,{
            header: this.isFixedAsset?WtfGlobal.getLocaleText("acc.fixedAssetList.grid.name"):WtfGlobal.getLocaleText("acc.coa.gridAccountName"),  //"Asset Name":"Account Name",
            hidden:this.isDepreciation,
            dataIndex: 'accname',
            renderer:WtfGlobal.deletedRenderer,
            pdfwidth:150,
            summaryRenderer:function(){return '<div class="grid-summary-common">'+WtfGlobal.getLocaleText("acc.coa.total")+'</div>'}
        },{
            header: WtfGlobal.getLocaleText("acc.coa.gridType"), //"Type",
            dataIndex: 'groupname',
            hidden:this.isDepreciation||this.isFixedAsset,
            renderer:WtfGlobal.deletedRenderer,
            pdfwidth:150
        },{
            header: WtfGlobal.getLocaleText("acc.coa.gridCreationDate"), //"Creation Date",
            dataIndex: "creationDate",
             align:'center',
            hidden:this.isDepreciation,
            renderer:WtfGlobal.onlyDateDeletedRenderer,
            pdfwidth:150,
            pdfrenderer : "date"
        },{
            header :this.isFixedAsset?WtfGlobal.getLocaleText("acc.fixedAssetList.grid.value"):WtfGlobal.getLocaleText("acc.coa.gridOpeningBalance"),  //'Asset Value':'Opening Balance',
            dataIndex: this.isFixedAsset?'presentbalance':'openbalance',
            align:'right',
            hidden:this.isDepreciation,
            renderer:this.opBalRenderer,
//            summaryType:'sum',
//            summaryRenderer:this.opBalRenderer,
            pdfrenderer : "rowcurrency",
            pdfwidth:150
        },{
            header :WtfGlobal.getLocaleText("acc.coa.gridCurrency"), //'Currency',
            hidden:this.isDepreciation,
            renderer:WtfGlobal.deletedRenderer,
            dataIndex: 'currencyname'
        },{
            header :WtfGlobal.getLocaleText("acc.coa.gridOpeningBalanceType"), //'Opening Balance Type',
            dataIndex: 'openbalance',
            summaryType:'sum',
            hidden:this.isDepreciation||this.isFixedAsset,
            summaryRenderer:this.balTypeRenderer,
            pdfwidth:150,
            renderer:this.balTypeRenderer
        },{
            header: WtfGlobal.getLocaleText("acc.fixedAssetList.period"),  //"Period",
            dataIndex: 'period',
            hidden:!this.isDepreciation,
            width:30,
            align:'right',
            renderer:WtfGlobal.deletedRenderer,
            pdfwidth:150
        },{
            header: WtfGlobal.getLocaleText("acc.fixedAssetList.month"),  //"Month",
            dataIndex: 'frommonth',
            hidden:!this.isDepreciation,
            align:'center',
            renderer:WtfGlobal.onlyMonthDeletedRenderer,
            pdfwidth:150
        },{
            header :WtfGlobal.getLocaleText("acc.fixedAssetList.periodDep"),  //'Period Depreciation',
            dataIndex: 'firstperiodamt',
            hidden:!this.isDepreciation,
            align:'right',
            renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol,
            pdfwidth:150
        },{
            header :WtfGlobal.getLocaleText("acc.fixedAssetList.adjustment"),  //'Adjustment',
            dataIndex: 'perioddepreciation',
            hidden:!this.isDepreciation,
            align:'right',
            renderer:this.showAdjustment,
            pdfwidth:150
        },{
            header :WtfGlobal.getLocaleText("acc.fixedAssetList.accDep"),  //'Accumulated Depreciation',
            dataIndex: 'accdepreciation',
            hidden:!this.isDepreciation,
            align:'right',
            renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol,
            pdfwidth:150
        },{
            header :WtfGlobal.getLocaleText("acc.fixedAssetList.netBookVal"),  //'Net Book Value',
            dataIndex: 'netbookvalue',
            hidden:!this.isDepreciation,
            align:'right',
            renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol,
            pdfwidth:150
        }, this.checkColumn=new Wtf.grid.DepCheckColumn({
            header: WtfGlobal.getLocaleText("acc.fixedAssetList.postDep"),  //"Post Depreciation",
            align:'center',
            hidden:!this.isDepreciation,
            //renderer:WtfGlobal.deletedRenderer,
            dataIndex: 'isje',
            width: 80
        }),{
            header: WtfGlobal.getLocaleText("acc.fixedAssetList.grid.salvageValue"), //"Salvage Value",
            dataIndex: 'salvage',
            align:'right',
            hidden:!this.isFixedAsset,
            renderer:WtfGlobal.currencyDeletedRenderer,
            pdfwidth:150
        },{
            header: WtfGlobal.getLocaleText("acc.fixedAssetList.grid.assetLifeinYears"), //"Asset Life(in years)",
            dataIndex: 'life',
            hidden:!this.isFixedAsset,
            renderer:WtfGlobal.deletedRenderer,
            pdfwidth:150
        },{
            header :(this.isFixedAsset? WtfGlobal.getLocaleText("acc.fixedAssetList.grid.value"):WtfGlobal.getLocaleText("acc.coa.gridOpeningBalance")) +" "+ WtfGlobal.getLocaleText("acc.fixedAssetList.grid.homCur"),//'Asset Value':'Opening Balance')+" in Home Currency",
            dataIndex: this.isFixedAsset?'presentbalanceInBase':'openbalanceinbase',
            align:'right',
            hidden:this.isDepreciation,
            renderer:WtfGlobal.currencyDeletedRenderer,
            summaryRenderer: this.showBalanceSummary.createDelegate(this),
            pdfwidth:150,
            pdfrenderer : "currency"
        }]);
    },

    showBalanceSummary:function(){
        return WtfGlobal.currencySummaryRenderer(this.BalanceSummary);
    },

    showAdjustment:function(v,m,rec){
        var amt=rec.get('perioddepreciation')-rec.get('firstperiodamt');
        if(Math.abs(amt)>=0.001){
            v= WtfGlobal.currencyRenderer(amt);
        }

        v= ""
        return WtfGlobal.deletedRenderer(v,m,rec)
    },

    createGrid:function(){
        this.quickSearchTF = new Wtf.KWLTagSearch({
            emptyText:this.isFixedAsset?WtfGlobal.getLocaleText("acc.rem.8"):WtfGlobal.getLocaleText("acc.coa.accountSearchText"), //'Search by Name',
            width: 130,
            field: 'accname',
            Store:this.store
        });
         this.resetBttn=new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.common.reset"), //'Reset',
            hidden:this.isSummary,
            tooltip :WtfGlobal.getLocaleText("acc.coa.resetTT"), //'Allows you to add a new search account name by clearing existing search account names.',
            id: 'btnRec' + this.id,
            scope: this,
            iconCls :getButtonIconCls(Wtf.etype.resetbutton),
            disabled :false
        });
        this.resetBttn.on('click',this.handleResetClick,this);
        this.grid = new Wtf.grid.HirarchicalGridPanel({
            layout:'fit',
            store: this.store,
            cm: this.gridcm,
            sm : this.selectionModel,
            hirarchyColNumber:1,
            plugins:[this.summary,this.checkColumn],
            autoScroll:true,
            border : false,
            //loadMask : true,
            viewConfig: {
                forceFit:true,
                emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
            }
        });
        this.grid.on("render", function(grid) {
            WtfGlobal.autoApplyHeaderQtip(grid);
        },this);
        this.pageLimit = new Wtf.forumpPageSize({
            ftree:this.grid
        });
        this.pToolBar = new Wtf.PagingSearchToolbar({
            id: 'pgTbar' + this.id,
            pageSize: this.msgLmt,
            store: this.store,
            displayInfo: true,
            searchField: this.quickSearchTF,
//            displayMsg: 'Displaying records {0} - {1} of {2}',
            emptyMsg: WtfGlobal.getLocaleText("acc.common.norec"), //"No records to display",
            plugins: this.pageLimit
        });
        this.store.on("load", this.setPageSize, this);
        this.store.on('datachanged', function() {
            if(this.pageLimit.combo) {
                var p = this.pageLimit.combo.value;
                this.quickSearchTF.setPage(p);
            }
        }, this);
        this.grid.getStore().on('load',function(store){
            Wtf.uncheckSelAllCheckbox(this.selectionModel);
            if(store.getCount()==0){
               this.grid.getView().emptyText=WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
               this.grid.getView().refresh();
               if(this.isDepreciation){
                    if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.printdepreciation))
                        this.printDepData.disable();
                    if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.exportdepreciation))
                        this.exportDepData.disable();
               }else{
                   if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.print))
                        this.printData.disable();
                    if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.exportdata))
                        this.exportData.disable();
               }
            }else{
                if(this.isDepreciation){
                     if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.printdepreciation))
                        this.printDepData.enable();
                     if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.exportdepreciation))
                        this.exportDepData.enable();
                }else{
                    if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.print))
                        this.printData.enable();
                    if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.exportdata))
                        this.exportData.enable();
                }
            }
        },this)

        this.btnArr=[];
        this.depPostJE=new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.fixedAssetList.post"),  //'Post',
            scope: this,
            tooltip:{text:WtfGlobal.getLocaleText("acc.fixedAssetList.postTT")},  //"Post Journal Entry"},
            iconCls:getButtonIconCls(Wtf.etype.save),
            handler:this.saveDepreceation.createDelegate(this)
        });
        this.addAccount=new Wtf.Toolbar.Button({
            text:this.isFixedAsset?WtfGlobal.getLocaleText("acc.fixedAssetList.addNewFA"):WtfGlobal.getLocaleText("acc.coa.addNewAccount"),  //'Add New Fixed Asset':'Add New Account',
            id:'maintainAccounts'+(this.isFixedAsset?'':'4'),
            iconCls:getButtonIconCls(Wtf.etype.add),
            tooltip:this.isFixedAsset?WtfGlobal.getLocaleText("acc.fixedAssetList.addNewFA")+".":WtfGlobal.getLocaleText("acc.coa.addNewAccount"),  //"Add new "+(this.isFixedAsset?"Fixed Asset":"Account")+" details. You may also add a sub-"+(this.isFixedAsset?"fixed asset":"account")+" to an existing "+(this.isFixedAsset?"fixed asset":"account")+".",
            handler:this.editCOA.createDelegate(this,[false])
        });
        this.editAccount=new Wtf.Toolbar.Button({
            text: this.isFixedAsset?WtfGlobal.getLocaleText("acc.fixedAssetList.editFA"):WtfGlobal.getLocaleText("acc.coa.editAccount"),  //'Edit Fixed Asset':'Edit Account',
            scope: this,
            tooltip:{text:(this.isFixedAsset?WtfGlobal.getLocaleText("acc.rem.98"):WtfGlobal.getLocaleText("acc.rem.97")),dtext:(this.isFixedAsset?WtfGlobal.getLocaleText("acc.rem.98"):WtfGlobal.getLocaleText("acc.rem.97")), etext:(this.isFixedAsset?WtfGlobal.getLocaleText("acc.rem.100"):WtfGlobal.getLocaleText("acc.rem.99"))},
            iconCls:getButtonIconCls(Wtf.etype.edit),
            disabled:true,
            handler:this.editCOA.createDelegate(this,[true])
        });
        this.deleteAccount=new Wtf.Toolbar.Button({
            text: this.isFixedAsset?WtfGlobal.getLocaleText("acc.rem.170"):WtfGlobal.getLocaleText("acc.coa.deleteAccount"),  //'Delete Fixed Asset':'Delete Account',
            scope: this,
//            hidden:this.isFixedAsset,
            tooltip:{text:(this.isFixedAsset?WtfGlobal.getLocaleText("acc.rem.102"):WtfGlobal.getLocaleText("acc.rem.101")),dtext:(this.isFixedAsset?WtfGlobal.getLocaleText("acc.rem.102"):WtfGlobal.getLocaleText("acc.rem.101")), etext:(this.isFixedAsset?WtfGlobal.getLocaleText("acc.rem.104"):WtfGlobal.getLocaleText("acc.rem.103"))},
            disabled:true,
            iconCls:getButtonIconCls(Wtf.etype.deletebutton),
            handler:this.deleteCOA.createDelegate(this)
        });
       	
        var removeButtonArr = [];
        removeButtonArr.push(new Wtf.Action({
            text:WtfGlobal.getLocaleText("acc.fixedAssetList.sellAsset"), //'Sell Fixed Asset',
            tooltip:{text:WtfGlobal.getLocaleText("acc.rem.130")},
       		iconCls:"pwnd sellAsset",  //getButtonIconCls(Wtf.etype.menudelete),
            scope:this,
            handler: function(){callFixedAssetRemove("fixedAssetRemove",this.grid.getSelectionModel().getSelected(), false);}  //this.deleteCOA.createDelegate(this)
        }));
        removeButtonArr.push(new Wtf.Action({
            text:WtfGlobal.getLocaleText("acc.fixedAssetList.writeOffAsset"),  //'Write off Fixed Asset',
            tooltip:{text:WtfGlobal.getLocaleText("acc.rem.131")},
       		iconCls:"pwnd writeOffAsset",  //getButtonIconCls(Wtf.etype.menudelete),
            scope:this,
            handler: function(){callFixedAssetRemove("fixedAssetRemove",this.grid.getSelectionModel().getSelected(), true);}  //this.deleteCOA.createDelegate(this)
        }));
        this.RemoveAsset=new Wtf.Toolbar.Button({
       		text: WtfGlobal.getLocaleText("acc.fixedAssetList.removeAsset"),  //'Remove Fixed Asset',
       		scope: this,
       		tooltip:{text:WtfGlobal.getLocaleText("acc.rem.114"),dtext:WtfGlobal.getLocaleText("acc.rem.114"), etext:WtfGlobal.getLocaleText("acc.rem.115")},
       		disabled:true,
       		iconCls:"pwnd removeAsset",  //getButtonIconCls(Wtf.etype.deletebutton),
       		menu:removeButtonArr
       	});
        this.depreciation=new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.fixedAssetList.faDepreciation"),  //'Fixed Asset Depreciation',
            scope: this,
            hidden:!this.isFixedAsset,
            tooltip:{text:WtfGlobal.getLocaleText("acc.rem.112"),dtext:WtfGlobal.getLocaleText("acc.rem.112"), etext:WtfGlobal.getLocaleText("acc.rem.113")},
            disabled:true,
            iconCls:"accountingbase depreciation",
            handler:this.showDepreciation.createDelegate(this)
        });
        if(this.isDepreciation){
            if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.postdepreciation))
                this.btnArr.push(this.depPostJE);
            if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.exportdepreciation)) {
                this.btnArr.push(this.exportDepData=new Wtf.exportButton({
                    obj:this,
                    text:WtfGlobal.getLocaleText("acc.common.export"),
                    tooltip:WtfGlobal.getLocaleText("acc.common.exportTT"), //"Export Fixed Asset depreciation Report details",
                    menuItem:{csv:true,pdf:true,rowPdf:false},
                    extra:{accid:this.accid},
                    get:1120, //Don't Change ==> Spring changes : seperate mode for depreciation [SK]
                    label:"Fixed Asset depreciation"
                }));
            }
            if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.printdepreciation)) {
                this.btnArr.push(this.printDepData=new Wtf.exportButton({
                    text:WtfGlobal.getLocaleText("acc.common.print"),  //"Print",
                    obj:this,
                    tooltip:WtfGlobal.getLocaleText("acc.common.printTT"),  //"Print Fixed Asset depreciation Report details",
                    menuItem:{print:true},
                    extra:{accid:this.accid,name:WtfGlobal.getLocaleText("acc.fixedAssetList.faDepreciation")},
                    get:1120, //Don't Change ==> Spring changes : seperate mode for depreciation [SK]
                    label:"Fixed Asset depreciation"
                }));
            }
        }else if(this.isFixedAsset){
             this.btnArr.push(this.quickSearchTF);
            this.btnArr.push(this.resetBttn);
            if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.create))
                this.btnArr.push(this.addAccount);
            if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.edit))
                this.btnArr.push(this.editAccount);
            if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.remove))
                this.btnArr.push(this.deleteAccount);
            if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.viewdepreciation))
                this.btnArr.push(this.depreciation);
       		this.btnArr.push(this.RemoveAsset);
            if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.exportdata)){
                this.btnArr.push(this.exportData=new Wtf.exportButton({
                    obj:this,
                    text:WtfGlobal.getLocaleText("acc.common.export"),
                    tooltip:WtfGlobal.getLocaleText("acc.common.exportTT"),  //"Export "+(this.isFixedAsset?"Fixed Asset":"Accounts")+" details",
                    menuItem:{csv:true,pdf:true,rowPdf:false},
                    params:(this.isFixedAsset?{group:[12]}:{group:[12],ignore:true}),
                    get:112,
                    label:(this.isFixedAsset?"Fixed Asset":"Chart of Account")
                }));
            }
            if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.print)){
                this.btnArr.push(this.printData=new Wtf.exportButton({
                    obj:this,
                    text:WtfGlobal.getLocaleText("acc.common.print"), //"Print",
                    tooltip:WtfGlobal.getLocaleText("acc.common.printTT"),  //"Print Fixed Asset Report details",
                    menuItem:{print:true},
                    params:({group:[12],name:WtfGlobal.getLocaleText("acc.fixedAssetList.faReport")}),
                    get:112,
                    label:"Fixed Asset"
                }));
            }
             this.bbar = this.pToolBar;
        }
        else{
             this.btnArr.push(this.quickSearchTF);
             this.btnArr.push(this.resetBttn);
             if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.create))
                this.btnArr.push(this.addAccount);
            if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.edit))
                this.btnArr.push(this.editAccount);
            if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.remove))
                this.btnArr.push(this.deleteAccount);
//                this.btnArr.push(this.depreciation);
//                mosin code
//                        if(!this.isFixedAsset){
//                alert('exportReports'+(this.isFixedAsset?'':'4'));
//                this.btnArr.push(new Wtf.exportButton({
//                obj:this,
//                  id:'exportReports4',
//                tooltip:"Export Chart of Account Report details",

            if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.exportdata)){
                this.btnArr.push(this.exportData=new Wtf.exportButton({
                    obj:this,
                    text:WtfGlobal.getLocaleText("acc.common.export"),
                    tooltip:WtfGlobal.getLocaleText("acc.common.exportTT"),  //"Export "+(this.isFixedAsset?"Fixed Asset":"Accounts")+" details",
                    menuItem:{csv:true,pdf:true,rowPdf:false},
                    params:(this.isFixedAsset?{group:[12]}:{group:[12],ignore:true}),
                    get:112,
                    label:(this.isFixedAsset?"Fixed Asset":"Chart of Account")
                }));


                var extraConfig = {};
                extraConfig.url= "ACCAccount/importAccounts.do";
                var extraParams = "{\"DepreciationAccont\":\""+Wtf.account.companyAccountPref.depreciationaccount+"\"}";
                this.importBtnArray= Wtf.importMenuArray(this, "Accounts", this.store, extraParams, extraConfig);
                this.importButton= Wtf.importMenuButtonA(this.importBtnArray, this, "Accounts");
                this.btnArr.push(this.importButton);
            }
            
             if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.print)){
                this.btnArr.push(this.printData=new Wtf.exportButton({
                    obj:this,
                    text:WtfGlobal.getLocaleText("acc.common.print"),  //"Print",
                    tooltip:WtfGlobal.getLocaleText("acc.common.printTT"),  //"Print Chart of Account Report details",
                    menuItem:{print:true},
                    params:({group:[12], ignore:true, name:WtfGlobal.getLocaleText("acc.coa.tabTitle")}),
                    get:112,
                    label:"Chart of Account"
                }));
             }

            this.btnArr.push("->");
            this.btnArr.push(this.typeEditor);
            this.btnArr.push('-');
            var tooltip=WtfGlobal.getLocaleText("acc.coa.tip1");  //"Please click here to view Account Receivable/Customer account(s)";
            this.btnArr.push("<a class='tbar-link-text' href='#' onClick='javascript: openAccRecTab("+true+")'wtf:qtip='"+tooltip+"'>"+WtfGlobal.getLocaleText("acc.coa.accountReceivableLink")+"</a>");
            tooltip=WtfGlobal.getLocaleText("acc.coa.tip2");  //"Please click here to view Account Payable/Vendor account(s)";
            this.btnArr.push("<a class='tbar-link-text' href='#' onClick='javascript: openAccRecTab("+false+")'wtf:qtip='"+tooltip+"'>"+WtfGlobal.getLocaleText("acc.coa.accountPayableLink")+"</a>");
            if(!WtfGlobal.EnableDisable(Wtf.UPerm.fixedasset, Wtf.Perm.fixedasset.view)){
	            tooltip=WtfGlobal.getLocaleText("acc.coa.tip3");  //here to view Fixed Asset(s)";
	            this.btnArr.push("<a class='tbar-link-text' href='#' onClick='javascript: openFixedAssetTab()'wtf:qtip='"+tooltip+"'>"+WtfGlobal.getLocaleText("acc.coa.FixedAssetLink")+"</a>");
            }
            this.btnArr.push("  ");
            this.btnArr.push("->");
            this.btnArr.push(getHelpButton(this,4));
            this.bbar = this.pToolBar;
        }

        if(this.isFixedAsset){
            chkCostCenterload();
            if(Wtf.CostCenterStore.getCount()==0) Wtf.CostCenterStore.on("load", this.setCostCenter, this);
            this.costCenter = new Wtf.form.ComboBox({
                store: Wtf.CostCenterStore,
                name:'costCenterId',
                width:100,
                listWidth:100,
                emptyText: WtfGlobal.getLocaleText("acc.rem.9"),  //'Select a Cost Center',
                displayField:'name',
                valueField:'id',
                triggerAction: 'all',
                mode: 'local',
                typeAhead:true,
                value: this.costCenterId,
                selectOnFocus:true,
                forceSelection: true
            });

            this.costCenter.on("select", function(cmb, rec, ind){
                this.costCenterId = rec.data.id;

                var currentBaseParams = this.store.baseParams;
                currentBaseParams.costCenterId = this.costCenterId;
                this.store.baseParams=currentBaseParams;

                this.store.load({
                    params: {
                        start: 0,
                        limit: (this.pageLimit && this.pageLimit.combo) ? (this.pageLimit.combo.getValue() || this.msgLmt) : this.msgLmt
                    }
                });
            },this);
            this.btnArr.push("-",WtfGlobal.getLocaleText("acc.common.costCenter"),this.costCenter);
                         
            this.btnArr.push("->");
            this.btnArr.push(this.typeEditor);
        }
        this.tbar=this.btnArr;
        this.typeEditor.on('select',this.loadTypeStore,this);
        this.selectionModel.on("selectionchange",this.enableDisableButtons.createDelegate(this,[this.btnArr]),this);
    },
    setCostCenter: function(){
        this.costCenter.setValue(this.costCenterId);//Select Default Cost Center
        Wtf.CostCenterStore.un("load", this.setCostCenter, this);
    },
    setPageSize: function(store, rec, opt){
        var count = 0;
        for (var i = 0; i < store.getCount(); i++) {
            if (rec[i].data['level'] == 0 && (rec[i].data['parentid'] == "" || rec[i].data['parentid'] == undefined))
                count++;
        }
//        if(this.jReader.jsonData['totalCount']) {
//            this.getBottomToolbar().displayEl.update("Displaying records " + parseInt(opt.params["start"] + 1) + " - " + parseInt(count + opt.params["start"]) + " of " + this.jReader.jsonData['totalCount']);
//        }
        this.pageLimit.totalSize = this.jReader.jsonData['totalCount'];
        if(this.isFixedAsset)
        	this.BalanceSummary = this.jReader.jsonData['presentbalanceSummary'];
        else
        	this.BalanceSummary = this.jReader.jsonData['openbalanceSummary'];
        this.grid.getView().refresh();
    },
//    importCSVRecords : function(type, extraParams, extraConfig){
//        this.impWin1 = Wtf.commonFileImportWindow(this, type, extraParams, extraConfig);
//        this.impWin1.show();
//    },
//    importXLSRecords :function(obj,moduleName,store,extraParams, extraConfig){
//        this.impWin1 = Wtf.xlsCommonFileImportWindow(obj,moduleName,store,extraParams, extraConfig);
//        this.impWin1.show();
//    },
//    mappingCSVInterface:function(Header, res, impWin1, delimiterType, extraParams, extraConfig) {
//        this.filename=res.FileName;
//
//        this.mapCSV=new Wtf.csvFileMappingInterface({
//            csvheaders:Header,
//            modName:"Accounts",
//            impWin1:impWin1,
//            delimiterType:delimiterType,
//            cm:this.gridcm,
//            extraParams: extraParams,
//            extraConfig: extraConfig
//        }).show();
//        Wtf.getCmp("csvMappingInterface").on('importfn',this.importCSVfunction, this);
//    },
//    importCSVfunction:function(response, delimiterType, extraParams, extraConfig) {
//        Wtf.importMappedRecords(this, response, "Accounts", this.filename, this.store, delimiterType, extraParams, extraConfig);
//    },
    loadTypeStore:function(a,rec){
        this.deleted=false;
        this.nondeleted=false;
        var index=rec.data.typeid;
        this.deleteAccount.enable();
        if(index==1){
            this.deleted=true;
            this.deleteAccount.disable();
        }
        else if(index==2)
            this.nondeleted=true;
        this.store.load({
            params: {
                start: 0,
                limit: (this.pageLimit && this.pageLimit.combo) ? (this.pageLimit.combo.getValue() || this.msgLmt) : this.msgLmt,
                ss : this.quickSearchTF.getValue()
            }
        });
        WtfComMsgBox(29,4,true);
        this.store.on('load',this.storeloaded,this);
    },
     handleResetClick:function(){
        if(this.quickSearchTF.getValue()){
            this.quickSearchTF.reset();
            this.store.load({
            params: {
                start: 0,
                limit: (this.pageLimit && this.pageLimit.combo) ? (this.pageLimit.combo.getValue() || this.msgLmt) : this.msgLmt
            }
        });
        }
    },
    storeloaded:function(store){
        Wtf.MessageBox.hide();
        this.quickSearchTF.StorageChanged(store);
    },
    enableDisableButtons:function(btnArr){
        Wtf.uncheckSelAllCheckbox(this.selectionModel);

        var rec=this.grid.getSelectionModel().getSelected();
        if(!this.isDepreciation){
            if(rec&&((rec.data.groupid=='18'&&rec.data.accname=='Cash in hand')))
                WtfGlobal.enableDisableBtnArr(btnArr, this.grid, [3], []);
            else if(rec&&rec.data.groupid=='12'&&this.grid.getSelectionModel().isSelected(rec))
                WtfGlobal.enableDisableBtnArr(btnArr, this.grid, [3,5,6], [4]);						// neeraj
            else if(rec&&((rec.data.groupid=='10' || rec.data.groupid=='13')))
                WtfGlobal.enableDisableBtnArr(btnArr, this.grid, [], []);
            else{
                this.depreciation.disable();
                this.RemoveAsset.disable();															// neeraj
                WtfGlobal.enableDisableBtnArr(btnArr, this.grid, [3], [4]);
            }
            var arr=this.grid.getSelectionModel().getSelections();
            for(var i=0;i<arr.length;arr++){
                if(arr[i]&&arr[i].data.deleted) {
                    this.deleteAccount.disable();
                    this.editAccount.disable();
                    this.depreciation.disable();
                    this.RemoveAsset.disable();														// neeraj
                }
            }
        }
    },
//    PrintPriview : function(type) {
//        if(this.searchJson==null) {
//            this.searchJson = "";
//        }
//        var name="Account";
//        var fromdate="";
//        var todate="";
//        var report="crm"
//        if(this.sortInfo != undefined) {
//            var field = this.sortInfo.field;
//            var dir = this.sortInfo.direction;
//        }
//        exportWithTemplate(this,type,name,fromdate,todate,report,undefined,undefined,field,dir);
//    },
    showDepreciation:function(){
        var rec=this.grid.getSelectionModel().getSelected();
        callDepreciationReport("depwin"+rec.data["accid"],rec.data["accid"],rec)
        Wtf.getCmp("depwin"+rec.data["accid"]).on('update',this.updateGrid,this);
    },
    editCOA:function(isEdit){
        this.recArr =[] ;
        this.isEdit=isEdit;
        if(isEdit){
            this.recArr = this.grid.getSelectionModel().getSelections();
            this.grid.getSelectionModel().clearSelections();
            WtfGlobal.highLightRowColor(this.grid,this.recArr,true,0,1);
        }
        var  rec=isEdit?this.recArr[0]:null;
        if(this.isFixedAsset){
            if(rec!=null&&rec.data.posted==true){
                WtfComMsgBox(39, 2);
                var num= (this.store.indexOf(this.recArr[0]))%2;WtfGlobal.highLightRowColor(this.grid,this.recArr,false,num,true);
                return;
            }
            callFixedAssetWindow(isEdit,rec,"fixedAssetWindow");
             Wtf.getCmp("fixedAssetWindow").on('update',this.updateGrid,this);
            Wtf.getCmp("fixedAssetWindow").on('cancel',function(){ var num= (this.store.indexOf(this.recArr[0]))%2;WtfGlobal.highLightRowColor(this.grid,this.recArr,false,num,true);},this);
        }
        else{
            callCOAWindow(isEdit,rec,"coaWindow");
            Wtf.getCmp("coaWindow").on('update',this.updateGrid,this);
            Wtf.getCmp("coaWindow").on('cancel',function(){ var num= (this.store.indexOf(this.recArr[0]))%2;WtfGlobal.highLightRowColor(this.grid,this.recArr,false,num,true);},this);
        }
},
    updateGrid: function(obj,accID){
        this.accID=accID;
        this.store.reload();
        this.isAdd=true;
         if(accID!=undefined)
            this.store.on('load',this.colorRow,this)
    },
    colorRow: function(store){
        if(this.isAdd && (!this.isEdit)){
            this.recArr=[];
            if(store.find('accid',this.accID) != -1) {
                 this.recArr.push(store.getAt(store.find('accid',this.accID)));
                 WtfGlobal.highLightRowColor(this.grid,this.recArr[0],true,0,0);
            }
            this.isAdd=false;
        }
    },
   deleteCOA:function(){
        var arr=[];
        var data=[];
        this.recArr = this.grid.getSelectionModel().getSelections();
        this.grid.getSelectionModel().clearSelections();
        WtfGlobal.highLightRowColor(this.grid,this.recArr,true,0,2);
        Wtf.MessageBox.show({
            title: WtfGlobal.getLocaleText("acc.common.confirm"),  //"Confirm",
            msg: this.isFixedAsset?WtfGlobal.getLocaleText("acc.rem.209"):WtfGlobal.getLocaleText("acc.rem.10"),  //"Are you sure you want to delete the selected account(s) and all the associated sub account(s)?<div><b>Note: This data cannot be retrieved later.</b></div>",
            width: 560,
            buttons: Wtf.MessageBox.OKCANCEL,
            animEl: 'upbtn',
            icon: Wtf.MessageBox.QUESTION,
            scope:this,
            fn:function(btn){
                if(btn!="ok"){
                    for(var i=0;i<this.recArr.length;i++){
                        var ind=this.store.indexOf(this.recArr[i])
                        var num= ind%2;
                        WtfGlobal.highLightRowColor(this.grid,this.recArr[i],false,num,2,true);
                    }
                    return;
                }
                for(i=0;i<this.recArr.length;i++){
                    arr.push(this.store.indexOf(this.recArr[i]));
                }
                data= WtfGlobal.getJSONArray(this.grid,true,arr);
                Wtf.Ajax.requestEx({
                    url: "ACCAccountCMN/deleteAccount.do",
    //                url: Wtf.req.account+'CompanyManager.jsp',
                    params:{
                        data:data,
                        isFixedAsset: this.isFixedAsset,
                        mode:6
                    }
                },this,this.genSuccessResponse,this.genFailureResponse);
            }
        });
    },

    genSuccessResponse:function(response){
         for(var i=0;i<this.recArr.length;i++){
             var ind=this.store.indexOf(this.recArr[i])
             var num= ind%2;
             WtfGlobal.highLightRowColor(this.grid,this.recArr[i],false,num,2,true);
        }
        WtfComMsgBox([(this.isFixedAsset?WtfGlobal.getLocaleText("acc.fixedAssetList.tabTitle"):WtfGlobal.getLocaleText("acc.coa.tabTitle")), response.msg],response.success*2+1);    //"Fixed Asset":"Chart of Account"
        if(response.success){
            (function(){
            	Wtf.salesAccStore.reload();	
            this.store.reload();
            }).defer(WtfGlobal.gridReloadDelay(),this);
        }
    },

    genFailureResponse:function(response){
        for(var i=0;i<this.recArr.length;i++){
            var ind=this.store.indexOf(this.recArr[i])
            var num= ind%2;
            WtfGlobal.highLightRowColor(this.grid,this.recArr[i],false,num,2,true);
        }
        var msg=WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    },

    opBalRenderer:function(val,m,rec){
        return WtfGlobal.withoutRateCurrencyDeletedSymbol(Math.abs(val),m,rec);
    },

    balTypeRenderer:function(val,m,rec){
        val=(val==0?"N/A":(val>0?"Debit":"Credit"));
       return WtfGlobal.deletedRenderer(val,m,rec)
    },

    getDepreciationData : function(store){
        var arr=[];
        store.each(function(rec){
            if(rec.dirty&&rec.data.isje)
                arr.push(store.indexOf(rec));
             },this);
        return WtfGlobal.getJSONArray(this.grid,true,arr);
    },

    saveDepreceation:function(a,b){
        var detail=this.getDepreciationData(this.store);
        if(detail=="[]"){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.rem.178")],2);
            return;
        }
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.savdat"),WtfGlobal.getLocaleText("acc.rem.11"),function(btn){
            if(btn!="yes") return;
            WtfComMsgBox(27,4,true);
            var rec=[];
            rec.accountid=this.accid;
            rec.detail=detail;
            rec.mode=5;
            rec.costcenter=this.record.data.costcenterid;
            rec.memo="Fixed Asset Depreciation";
            Wtf.Ajax.requestEx({
//                url: Wtf.req.account+'CompanyManager.jsp',
                url: "ACCDepreciation/saveAccountDepreciation.do",
                params: rec
                },this,this.genDepSuccessResponse,this.genDepFailureResponse);
        },this);
    },

    genDepSuccessResponse:function(response){
        Wtf.MessageBox.hide();
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.fixedAssetList.postDep"),response.msg],response.success*2+1);
        if(response.success){
            this.store.reload();
           // this.fireEvent('update',this);
        }
    },

    genDepFailureResponse:function(response){
      Wtf.MessageBox.hide();
        var msg=WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    }
});



//===== By Category Report ========
Wtf.account.AccountListByCategory=function(config){
     Wtf.apply(this, config);
     this.isFixedAsset= config.isFixedAsset;
     this.isCategoryUpdated= false;
     this.GridRec= Wtf.data.Record.create ([
        {name: 'accid'},
        {name: 'accname'},
        {name: 'groupid'},
        {name: 'groupname'},
        {name: 'openbalance'},
        {name: 'presentbalance'},
        {name: 'salvage'},
        {name: 'life'},
        {name: 'parentid'},
        {name: 'currencysymbol'},
        {name: 'currencyname'},
        {name: 'currencyid'},
        {name: 'parentname'},
        {name: 'depreciationaccount'},
        {name: 'period'},
        {name: 'frommonth', type:'date'},
        {name: 'tomonth', type:'date'},
        {name: 'firstperiodamt'},
        {name: 'posted', type:'boolean'},
        {name: 'perioddepreciation'},
        {name: 'accdepreciation'},
        {name: 'creationDate' ,type:'date'},
        {name: 'netbookvalue'},
        {name: 'depdetailid'},
        {name: 'deleted'},
        {name: 'isje'},
        {name: 'category'},
        {name: 'categoryid'},
        {name: 'id'}
    ]);
    this.ByCategoryStore = new Wtf.data.GroupingStore({
        url:"ACCAccount/getAccountsByCategory.do",
        reader: new Wtf.data.KwlJsonReader({
            totalProperty:"count",
            root: "data"
        },this.GridRec),
        groupField:"category",
        sortInfo: {field: 'category',direction: "DESC"}
    });
    this.sm = new Wtf.grid.RowSelectionModel();
    this.grid = new Wtf.grid.GridPanel({
        store:this.ByCategoryStore,
        border:false,
        layout:'fit',
        view: new Wtf.grid.GroupingView({
            forceFit:true
        }),
        loadMask:true,
        columns:[
            {
                header:WtfGlobal.getLocaleText("acc.coa.category"),  //"Category",
                dataIndex:'category',
                hidden: true,
                fixed: true,
                renderer:WtfGlobal.deletedRenderer
            },{
                header: this.isFixedAsset?WtfGlobal.getLocaleText("acc.fixedAssetList.grid.name"):WtfGlobal.getLocaleText("acc.coa.gridAccountName"),  //"Asset Name":"Account Name",
                dataIndex: 'accname',
                renderer:WtfGlobal.deletedRenderer,
                pdfwidth:150,
                summaryRenderer:function(){return '<div class="grid-summary-common">'+WtfGlobal.getLocaleText("acc.coa.total")+'</div>'}          //Total
            },{
                header:WtfGlobal.getLocaleText("acc.coa.gridType"),  // "Type",
                dataIndex: 'groupname',
                hidden:this.isFixedAsset,
                renderer:WtfGlobal.deletedRenderer,
                pdfwidth:150
            },{
                header:WtfGlobal.getLocaleText("acc.coa.gridCreationDate"),  // "Creation Date",
                dataIndex: "creationDate",
                align:'center',
                renderer:WtfGlobal.onlyDateDeletedRenderer
            },{
                header :this.isFixedAsset?WtfGlobal.getLocaleText("acc.fixedAssetList.grid.value"):WtfGlobal.getLocaleText("acc.coa.gridOpeningBalance"),  //'Asset Value':'Opening Balance',
                dataIndex: this.isFixedAsset?'presentbalance':'openbalance',
                align:'right',
                renderer:this.opBalRenderer,
                summaryType:'sum',
                summaryRenderer:this.opBalRenderer,
                pdfrenderer : "rowcurrency",
                pdfwidth:150
            },{
                header :WtfGlobal.getLocaleText("acc.coa.gridCurrency"),  //'Currency',
                renderer:WtfGlobal.deletedRenderer,
                dataIndex: 'currencyname'
            },{
                header :WtfGlobal.getLocaleText("acc.coa.gridOpeningBalanceType"),  //'Opening Balance Type',
                dataIndex: 'openbalance',
                summaryType:'sum',
                hidden: this.isFixedAsset,
                summaryRenderer:this.balTypeRenderer,
                pdfwidth:150,
                renderer:this.balTypeRenderer
            },{
                header:WtfGlobal.getLocaleText("acc.fixedAssetList.grid.salvageValue"),  // "Salvage Value",
                dataIndex: 'salvage',
                align:'right',
                hidden:!this.isFixedAsset,
                renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol,
                pdfwidth:150
            },{
                header:WtfGlobal.getLocaleText("acc.fixedAssetList.grid.assetLifeinYears"),  // "Asset Life(in years)",
                dataIndex: 'life',
                hidden:!this.isFixedAsset,
                renderer:WtfGlobal.deletedRenderer,
                pdfwidth:150
            }],
            bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
                pageSize: 30,
                id: "pagingtoolbar" + this.id,
                store: this.ByCategoryStore,
//                searchField: this.quickPanelSearch,
                displayInfo: true,
//                displayMsg: 'Displaying records {0} - {1} of {2}',
//                emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"),  //"No results to display",
                plugins: this.pP = new Wtf.common.pPageSize({
                    id : "pPageSize_"+this.id
                })
            })
    });
    Wtf.account.AccountListByCategory.superclass.constructor.call(this,config);
}
Wtf.extend(Wtf.account.AccountListByCategory,Wtf.Panel,{
    onRender: function(config){
        if(this.isFixedAsset){
            this.ByCategoryStore.baseParams={
                group:12
            }
            this.ByCategoryStore.load({
                params:{
                    start:0,
                    limit:30
                }
            });
        } else {
            this.ByCategoryStore.load({
                params:{
                    start:0,
                    limit:30
                }
            });
        }

        this.add(this.grid);

        this.on("activate",function(){
            if(this.isCategoryUpdated){
                this.ByCategoryStore.reload();
                this.isCategoryUpdated= false;
            }
        },this);
        Wtf.account.AccountListByCategory.superclass.onRender.call(this,config);
    },
    opBalRenderer:function(val,m,rec){
        return WtfGlobal.withoutRateCurrencyDeletedSymbol(Math.abs(val),m,rec);
    },

    balTypeRenderer:function(val,m,rec){
        val=(val==0?"":(val>0?"Debit":"Credit"));
       return WtfGlobal.deletedRenderer(val,m,rec)
    }
});

