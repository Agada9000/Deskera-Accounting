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
function openProductWin(){
    callProductWindow();
    Wtf.getCmp("productwindow").on('update',function(obj,productID){
        Wtf.getCmp("ProductReport").updateGrid(obj,productID);
    },this);
}
Wtf.account.ProductDetailsPanel=function(config){
    this.Product=null;
    this.addproductwin=config.addproduct;
    this.arrRec=[];
    this.isEdit=false;
    this.addNew=false;
    this.productID=null;
    this.productlinkid=config.productlinkid;
    this.uPermType=Wtf.UPerm.product;
    this.permType=Wtf.Perm.product;
    this.productRec = Wtf.data.Record.create ([
    {name:'productid'},
    {name:'productname'},
    {name:'desc'},
    {name:'pid'},
    {name:'vendor'},
    {name:'producttype'},
    {name:'type'},
    {name:'initialsalesprice'},
    {name:'uomid'},
    {name:'uomname'},
    {name:'parentuuid'},
    {name:'parentid'},
    {name:'parentname'},
    {name:'purchaseaccountid'},
    {name:'salesaccountid'},
    {name:'purchaseretaccountid'},
    {name:'salesretaccountid'},
    {name:'reorderquantity'},
    {name:'quantity'},
    {name:'reorderlevel'},
    {name:'leadtime'},
    {name:'purchaseprice'},
    {name:'saleprice'},
    {name: 'leaf'},
    {name: 'syncable'},
    {name: 'level'},
    {name: 'initialquantity',mapping:'initialquantity'},
    {name: 'initialprice'},
    {name: 'ccountinterval'},
    {name:'ccounttolerance'}
]);

this.productStore = new Wtf.data.Store({
//    url:Wtf.req.account+'CompanyManager.jsp',
    url:"ACCProduct/getProducts.do",
    baseParams:{mode:22},
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    },this.productRec)
});
this.productStore.on('load',this.hideMsg,this);
//Wtf.productStore.on('beforeload',function(){this.productStore.reload();},this);
    this.productStore.load();
    WtfComMsgBox(29,4,true);
    var btnArr=[], bottomArr=[] ;
    var btnArrEDSingleS=[]; // Enable/Disable button's indexes on single select
    var btnArrEDMultiS=[]; // Enable/Disable button's indexes on multi select
    var productArr=[];
    var productArrEDSingleS=[]; // Enable/Disable button's indexes on single select
    var productArrEDMultiS=[]; // Enable/Disable button's indexes on multi select
    if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.create)) {
        productArr.push(new Wtf.Action({
            text:WtfGlobal.getLocaleText("acc.productList.addNewProduct"),//'Add New Product/Service',
            tooltip:{text:WtfGlobal.getLocaleText("acc.productList.addNewProductTT")},
            iconCls:getButtonIconCls(Wtf.etype.menuadd),
            scope:this,
            handler:this.showForm.createDelegate(this,[false])
        }));
    }
    if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.edit)) {
        productArr.push(new Wtf.Action({
                text:WtfGlobal.getLocaleText("acc.productList.editProduct"),//'Edit Product/Service',
                disabled:true,
                tooltip:{text:WtfGlobal.getLocaleText("acc.productList.editProductTT"),dtext:WtfGlobal.getLocaleText("acc.productList.editProductTT")},  //,etext:" Edit selected product details."},
                scope:this,
                iconCls:getButtonIconCls(Wtf.etype.menuedit),
                handler:this.showForm.createDelegate(this,[true])
            }));
            productArrEDSingleS.push(productArr.length-1);
        }
    if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.create)) {
        productArr.push(this.prodCloneBttn = new Wtf.Action({
            text:WtfGlobal.getLocaleText("acc.productList.cloneProduct"),//'Clone Products/Service',
            disabled:true,
            tooltip:{text:WtfGlobal.getLocaleText("acc.productList.cloneProductTT"),dtext:WtfGlobal.getLocaleText("acc.productList.cloneProductTT")},  //,etext:"Clone selected product."},
            scope:this,
            iconCls:'pwnd menu-clone',
            handler:this.showForm.createDelegate(this,[true,true])
        }));
        productArrEDSingleS.push(productArr.length-1);
    }
        if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.remove)) {
            productArr.push(this.prodDelBttn = new Wtf.Action({
                text:WtfGlobal.getLocaleText("acc.productList.deleteProduct"),//'Delete Products/Service',
                disabled:true,
                tooltip:{text:WtfGlobal.getLocaleText("acc.productList.deleteProductTT"),dtext:WtfGlobal.getLocaleText("acc.productList.deleteProductTT")},  //,etext:"Delete selected product details."},
                scope:this,
                iconCls:getButtonIconCls(Wtf.etype.menudelete),
                handler:this.deleteProduct.createDelegate(this)
            }));
            productArrEDMultiS.push(productArr.length-1);
        }

        this.localSearch = new Wtf.KWLLocalSearch({
            emptyText:WtfGlobal.getLocaleText("acc.productList.searchText"),//'Search by Product Name',
            width: 150,
            searchField: "productname"
        });
        btnArr.push(this.localSearch);

    if(productArr.length>1) {
        btnArr.push({
            text:WtfGlobal.getLocaleText("acc.productList.productMenu"),//'Products And Services Menu',
            tooltip:WtfGlobal.getLocaleText("acc.productList.productMenuTT"),  //{text:"Click here to add, edit, clone or delete a product."},
            id:"manageProducts3",//FixMe: remove hardcoded helpmodeid
            iconCls:'accountingbase product',
            menu:productArr
        });
    }
    if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.viewpricelist)) {
        btnArr.push(new Wtf.Toolbar.Button({
                text:WtfGlobal.getLocaleText("acc.productList.addNewPrice"),//'Add New Price',
                id:"addPrice3",
                disabled:true,
                tooltip:WtfGlobal.getLocaleText("acc.productList.addNewPriceTT"),  //{text:"Click here to add new price (Purchase Price & Sales Price) by selecting an available product.",dtext:"Select a product to add price.", etext:"Add price to the selected product."},
                iconCls :getButtonIconCls(Wtf.etype.add),
                handler:this.showPricelist.createDelegate(this)
            })
        );
        btnArrEDSingleS.push(btnArr.length-1);
    }
    if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.viewpricelist)){

        btnArr.push(new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.productList.priceListReport"),//'Price List Report',
            disabled:true,
            tooltip:WtfGlobal.getLocaleText("acc.rem.55"),  //{text:"Select a product to view price list.",dtext:"Select a product to view price list.", etext:"View price list report for the selected product."},
            iconCls:'accountingbase pricelistbutton',
            handler:this.showPriceReport.createDelegate(this)
        }));
        btnArrEDSingleS.push(btnArr.length-1);
    }

    if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.syncproducts)) { 
        btnArr.push(new Wtf.Action({
            text:WtfGlobal.getLocaleText("acc.productList.dataSync"),//'Data Sync',
            iconCls:getButtonIconCls(Wtf.etype.sync),
            scope:this,
//            disabled:true,
            tooltip:WtfGlobal.getLocaleText("acc.productList.dataSyncTT"),  //"Data Syncing operation enables you to sync the Product List data from Accounting into CRM. Following fields from Accounting will be populated into CRM:<br>1. Product Name <br>2. Description <br>3. Category  <br>4. Vendor Name <br>5. Vendor Phone No.<br>6. Vendor Email ID <br>7. Purchase Price <br>8. Sales Price",
            handler:this.syncProducts.createDelegate(this)
        }));
    }

//        btnArrEDMultiS.push(btnArr.length-1);
      //  btnArr.push("-");
    if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.inventoryreport)) {
        btnArr.push(new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.productList.inventoryReport"),//'Inventory Report',
            id:"manageInventory3",
            disabled:true,
            tooltip:WtfGlobal.getLocaleText("acc.productList.inventoryReportTT"),  //{text:"Select a product to view inventory report.",dtext:"Select a product to view inventory report.", etext:"View inventory report for the selected product."},
            iconCls:'accountingbase inventoryreport',
            handler:this.showInventoryReport.createDelegate(this)
        }));
        btnArrEDSingleS.push(btnArr.length-1);
    }
    if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.buildassembly)) {
        btnArr.push(new Wtf.Action({
            text:WtfGlobal.getLocaleText("acc.productList.buildAssembly"),//'Build Assembly',
            tooltip:WtfGlobal.getLocaleText("acc.productList.buildAssemblyTT"),  //{text:"Click here to build stock of an assembly product by using Inventory Assembly."},
            scope:this,
            iconCls:getButtonIconCls(Wtf.etype.buildassemly),
            handler:function(){
                callBuildAssemblyForm();
            }
        }));
    }
    if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.valuation)) {
        btnArr.push(new Wtf.Action({
            text:WtfGlobal.getLocaleText("acc.productList.inventoryValuation"),//'Inventory Valuation',
            tooltip:WtfGlobal.getLocaleText("acc.productList.inventoryValuationTT"),  //{text:"Click here to view Inventory Valuation Report of products available."},
            scope:this,
            iconCls:getButtonIconCls(Wtf.etype.inventoryval),
            handler:function(){
                showProductValuationTab();
            }
        }));
   }
    var cyclecountButtonArr = [];
    cyclecountButtonArr.push(new Wtf.Action({
            text:WtfGlobal.getLocaleText("acc.productList.addCycleCount"),//'Add Cycle Count Entry',
            tooltip:WtfGlobal.getLocaleText("acc.rem.61"),  //{text:"Click here to add cycle count entry."},
            iconCls:getButtonIconCls(Wtf.etype.addcyclecount),
            scope:this,
            handler:callCycleCount
        }));
     cyclecountButtonArr.push(new Wtf.Action({
            text:WtfGlobal.getLocaleText("acc.productList.cycleCountApprove"),//'Cycle Count Approve',
            tooltip:WtfGlobal.getLocaleText("acc.rem.60"),  //{text:"Click here to view Cycle count approval."},
            iconCls:getButtonIconCls(Wtf.etype.approvecyclecount),
            scope:this,
            handler:callCycleCountApproval
        }));
    cyclecountButtonArr.push(new Wtf.Action({
            text:WtfGlobal.getLocaleText("acc.productList.cycleCountWorksheet"),//'Cycle Count Worksheet',
            tooltip:WtfGlobal.getLocaleText("acc.rem.59"),  //{text:"Click here to view Cycle count worksheet."},
            iconCls:getButtonIconCls(Wtf.etype.countcyclecount),
            scope:this,
            handler:callCycleCountWorksheet
        }));
    cyclecountButtonArr.push(new Wtf.Action({
            text:WtfGlobal.getLocaleText("acc.productList.cycleCountReport"),//'Cycle Count Report',
            tooltip:WtfGlobal.getLocaleText("acc.rem.58"),  //{text:"Click here to view Cycle count report."},
            iconCls:getButtonIconCls(Wtf.etype.cyclecountreport),
            scope:this,
                handler:callCycleCountReport
        }));
   if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.cyclecount)) {
        btnArr.push({
            text:WtfGlobal.getLocaleText("acc.productList.cycleCount"),//'Cycle Count',
            tooltip:WtfGlobal.getLocaleText("acc.rem.57"),  //{text:"Click here to manage Cycle Count of products available."},
            iconCls:getButtonIconCls(Wtf.etype.cyclecount),
            menu:cyclecountButtonArr
        });
   }
   if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.reorderproducts)) {
        btnArr.push(new Wtf.Action({
            text:WtfGlobal.getLocaleText("acc.productList.reorderProducts"),//'Reorder Product(s)',
            tooltip:WtfGlobal.getLocaleText("acc.rem.56"),  //{text:"Click here to view and reorder product(s) which have reached the reorder level."},
            iconCls:getButtonIconCls(Wtf.etype.reorderreport),
            scope:this,
            handler:reorderProducts
        }));
   }
   

    this.sm = new Wtf.grid.CheckboxSelectionModel({
    	header: (Wtf.isIE7)?"":'<div class="x-grid3-hd-checker"> </div>'    // For IE 7 the all select option not available
    });
    this.grid = new Wtf.grid.HirarchicalGridPanel({
        store:this.productStore,
        sm:this.sm,
        border:false,
        hirarchyColNumber:1,
        layout:'fit',
        loadMask:true,
        viewConfig:{forceFit:true,emptyText:WtfGlobal.emptyGridRenderer("<a class='grid-link-text' href='#' onClick='javascript:openProductWin()'>"+WtfGlobal.getLocaleText("acc.nee.23")+"</a>")},
        forceFit:true,
        columns:[this.sm,{
           header:WtfGlobal.getLocaleText("acc.productList.gridProduct"),//"Product",
           dataIndex:'productname',
           pdfwidth:75
         },{
        	 header:WtfGlobal.getLocaleText("acc.productList.gridProductID"),
             dataIndex:'pid',
             align:'left',
             pdfwidth:75
         },{
            header:WtfGlobal.getLocaleText("acc.productList.gridDescription"),//"Description",
            dataIndex:'desc',
            renderer : function(val) {
                 return "<div wtf:qtip=\""+val+"\" wtf:qtitle='"+WtfGlobal.getLocaleText("acc.productList.gridDescription")+"'>"+val+"</div>";
            },
            pdfwidth:75
         },{
            hidden:true,
            dataIndex:'productid'
         },{
            header:"Unit Of Measure",
            dataIndex:'uomname',
            hidden:true
         },{
            header:WtfGlobal.getLocaleText("acc.productList.gridProductType"),//"Product Type",
            dataIndex:'type',
            pdfwidth:75
        },{
            header:WtfGlobal.getLocaleText("acc.productList.gridReorderQuantity"),//"Reorder Quantity",
            dataIndex:'reorderquantity',
            align:'right',
            renderer:this.unitRenderer,
            pdfwidth:75
        },{
            header:WtfGlobal.getLocaleText("acc.productList.gridReorderLevel"),//"Reorder Level",
            dataIndex:'reorderlevel',
            align:'right',
            renderer:this.unitRenderer,
            pdfwidth:75
        },{
            header:WtfGlobal.getLocaleText("acc.productList.gridLeadTime"),//"Lead Time(in days)",
            dataIndex:'leadtime',
            align:'right',
            renderer:this.LeadTimeRenderer,
            pdfwidth:75
        },{
            header:WtfGlobal.getLocaleText("acc.productList.gridAvailableQty"),//"Available Quantity",
            dataIndex:"quantity",
            align:'right',
            renderer:this.unitRenderer,
            pdfwidth:75
        },{
            header:WtfGlobal.getLocaleText("acc.productList.gridPurchasePrice"),//"Purchase Price",
            dataIndex:'purchaseprice',
            align:'right',
            renderer:WtfGlobal.currencyRenderer,
            pdfwidth:75
        },{
            header:WtfGlobal.getLocaleText("acc.productList.gridSalesPrice"),//"Sales Price",
            align:'right',
            dataIndex:'saleprice',
            pdfwidth:75,
            renderer:function(v,metadata,record){
            									 if(record.data['type'] == "Inventory Non-Sale"){
            										 return "N/A";
            									 }else{ 
            										  return WtfGlobal.currencyRenderer(v,false);}
            									 }
        },{
            header:WtfGlobal.getLocaleText("acc.productList.gridCycleCountInterval"),//"Cycle count Interval",
            align:'right',
            dataIndex:'ccountinterval',
            width:80,
            pdfwidth:75,
            renderer:function(v,metadata,record){
									           	 if(record.data['type'] == "Service"){
									           		 return "N/A";
											     }else{ 
											    	 	return (v+' days');}
											     }
        },{
            header:WtfGlobal.getLocaleText("acc.productList.gridCycleCountTolerance"),//"Cycle count Tolerance",
            align:'right',
            dataIndex:'ccounttolerance',
            width:80,
            pdfwidth:75,
            renderer:function(v,metadata,record){
            					 if(record.data['type'] == "Service"){
            						 return "N/A";
            					 }else{
            						 return v+' %';}
//            						 return'<div class="currency">'+v+'%</div>';}
            					 }
        }],
        tbar:btnArr,

        bbar:bottomArr
    });

    this.exportButton=new Wtf.exportButton({
        obj:this,
        tooltip:WtfGlobal.getLocaleText("acc.common.exportTT"),  //"Export Report details.",  
        params:{name:WtfGlobal.getLocaleText("acc.productList.tabTitle")},
        menuItem:{csv:true,pdf:true,rowPdf:false},
        get:915,
        label:"Product List"
    }),
    
    this.printButton=new Wtf.exportButton({
        text:WtfGlobal.getLocaleText("acc.common.print"),  //"Print",
        obj:this,
        tooltip:WtfGlobal.getLocaleText("acc.common.printTT"),  //"Print Report details.",   
        params:{name:WtfGlobal.getLocaleText("acc.productList.tabTitle")},
        menuItem:{print:true},
        get:915,
        label:"Product List"
    });
    
    bottomArr.push(this.printButton);
    bottomArr.push(this.exportButton);
	   
 		   
     btnArr.push("->");
     btnArr.push(getHelpButton(this,3));
     
    Wtf.account.ProductDetailsPanel.superclass.constructor.call(this,config);
    this.sm.on("selectionchange",function(){
        WtfGlobal.enableDisableBtnArr(btnArr, this.grid, btnArrEDSingleS, btnArrEDMultiS);
        WtfGlobal.enableDisableBtnArr(productArr, this.grid, productArrEDSingleS, productArrEDMultiS);
        this.delIA = false;
        for(var cnt=0; cnt<this.sm.selections.length; cnt++) {
            var ptype = this.sm.selections.items[cnt].data.type;
            var pqty = this.sm.selections.items[cnt].data.quantity;
            if(ptype == "Inventory Assembly" && pqty != 0) {						
            	this.delIA = true;
                break;
            }
        }
    },this);
    this.grid.on("render", function(grid) {
        WtfGlobal.autoApplyHeaderQtip(grid);
        this.localSearch.applyGrid(grid);
    },this);
}

Wtf.extend(Wtf.account.ProductDetailsPanel,Wtf.Panel,{ 
    updateGrid: function(obj,productID){
        this.productID=productID;
        this.productStore.reload();
        this.addNew=true;
        this.productStore.on('load',this.colorRow,this)
    },
    colorRow: function(store){
        if(this.addNew){
            var recArr=[];
            recArr.push(store.getAt(store.find('productid',this.productID)));
            WtfGlobal.highLightRowColor(this.grid,recArr[0],true,0,0);
            this.addNew=false;
        }
    },
    calllinkRowColor:function(id){
        var index=this.productStore.find('productid',id );
         var rec=this.productStore.getAt(index);
         if(index>=0)
            WtfGlobal.highLightRowColor(this.grid,rec,true,0,0);
   },
   hideMsg: function(){
         Wtf.MessageBox.hide();
         Wtf.dirtyStore.product = false;
         if(this.productlinkid!=undefined)
             this.calllinkRowColor(this.productlinkid);
    },
    handleResetClick:function(){
        if(this.quickPanelSearch.getValue()){
            this.quickPanelSearch.reset();
            this.productStore.load({
                params: {
                    start:0,
                    limit:this.pP.combo.value
                }
            });
        }
    },
    unitRenderer:function(value,metadata,record){
        if(record.data['type'] == "Service"){
        	return "N/A";
        }
    	var unit=record.data['uomname'];
            value=value+" "+unit;
        return value;
    },
    LeadTimeRenderer:function(value,metadata,record){
    	if(record.data['type'] == "Service"){
        	return "N/A";
        }
    	if(value==1){
            value=value+" Day";
        }
        else{
            value=value+" Days";
        }
        return value;
    },
    showPricelist:function(){
         var rec=null;
         if(this.grid.getSelectionModel().hasSelection()==false||this.grid.getSelectionModel().getCount()>1){
             WtfComMsgBox(17,2);
             return;
         }else{
             rec=this.grid.getSelectionModel().getSelected();
             callPricelistWindow(rec,"pricewindow");
             Wtf.getCmp("pricewindow").on('update',function(){this.productStore.reload();},this);
         }
    },
    onRender: function(config){

        this.add(this.grid);
//         if(this.addproductwin){
//            callProductWindow(false,null,"productwin");
//        Wtf.getCmp("productwin").on('update',this.updateGrid,this);
//        }
        Wtf.account.ProductDetailsPanel.superclass.onRender.call(this, config);
    },
    showForm:function(isEdit,isClone){
       var recArr =[] ;
       this.isEdit=isEdit;
        if(isEdit){
            recArr = this.grid.getSelectionModel().getSelections();
            this.grid.getSelectionModel().clearSelections();
 //           WtfGlobal.highLightRowColor(this.grid,recArr,true,0,1);
        }
        var rec=isEdit?recArr[0]:null;
        var tabid=isEdit?recArr[0].data.productid:"productwin";
        if(isClone){
        	tabid=isEdit?"clone"+recArr[0].data.productid:"cloneproductwin";
        }
        var pname=isEdit?recArr[0].data.productname:null;
        callProductWindow(isEdit, rec, tabid, pname, isClone);
        Wtf.getCmp(tabid).on('update',this.updateGrid,this);
//        Wtf.getCmp(tabid).on('cancel',function(){ var num= (this.productStore.indexOf(recArr[0]))%2;WtfGlobal.highLightRowColor(this.grid,recArr,false,num,1);},this);
    },
    deleteProduct:function(){
       this.arrRec=[];
       this.arrID=[];
       this.arrRec = this.sm.getSelections();
       for(var i=0;i<this.sm.getCount();i++){
            this.arrID.push(this.arrRec[i].data['productid']);
            
            if(this.productStore.find('parentuuid', this.arrRec[i].data['productid']) != -1 ){
	            var child = this.productStore.getAt(this.productStore.find('parentuuid',this.arrRec[i].data['productid']));
	            this.arrID.push(child.data['productid']);
	            
	            while(this.productStore.find('parentuuid',child.data['productid']) != -1){
	        		child = this.productStore.getAt(this.productStore.find('parentuuid',child.data['productid']));
	        		this.arrID.push(child.data['productid']);
	        	}
            }
       }     
//        this.sm.clearSelections();
        WtfGlobal.highLightRowColor(this.grid,this.arrRec,true,0,2);
           Wtf.MessageBox.show({
       title: WtfGlobal.getLocaleText("acc.common.warning"),
       msg: WtfGlobal.getLocaleText("acc.rem.65"),  //"Are you sure you want to delete the selected product(s) and all associated sub product(s)?<div><b>Note: This data cannot be retrieved later</b></div>",
       width: 560,
       buttons: Wtf.MessageBox.OKCANCEL,
       animEl: 'upbtn',
       icon: Wtf.MessageBox.QUESTION,
       scope:this,
       fn:function(btn){
           if(btn!="ok"){
                var num= (this.productStore.indexOf(this.arrRec[0]))%2;
               WtfGlobal.highLightRowColor(this.grid,this.arrRec,false,num,2);
                 return;
            }
            else {
            	if(this.delIA){
            		this.unBuildAssemblyAfterDeletion();
            	}else{
            		this.deletionRequest(false);
            	}
            }
        }});
    },
    
    deletionRequest:function(unbuild){
    	Wtf.Ajax.requestEx({
          url:"ACCProductCMN/deleteProducts.do",
          params: {
              mode:23,
              ids:this.arrID,
              unBuild:unbuild
          }
      },this,this.genSuccessResponse,this.genFailureResponse);
    },

    unBuildAssemblyAfterDeletion:function(){
        Wtf.MessageBox.show({
            title: WtfGlobal.getLocaleText("acc.common.warning"),
            msg: WtfGlobal.getLocaleText("acc.rem.62"),  //"Would you like to unbuild and update the inventory of the sub product(s) (Bill of Materials) of selected Assembly product(s)?",
            width: 560,
            buttons: Wtf.MessageBox.YESNO,
            animEl: 'upbtn',
            icon: Wtf.MessageBox.QUESTION,
            scope:this,
            closable:false,
            fn:function(btn){
            		if(btn == "yes"){
            			this.deletionRequest(true);
            		}else if(btn == "no"){
            			this.deletionRequest(false);
            		}
            	}
            });
    },
    
    syncProducts:function(){
       var arrID=[];

        this.productStore.filterBy(function(rec){
            if(rec.data.syncable)
                return true;
                else return false
        })
       this.arrRec = this.productStore.getRange(0,this.productStore.getCount()-1);
       for(var i=0;i<this.productStore.getCount();i++)
            if(this.arrRec[i].data.syncable)
                arrID.push(this.arrRec[i].data['productid']);
        this.sm.clearSelections();
        WtfGlobal.highLightRowColor(this.grid,this.arrRec,true,0,2);
       if(this.arrRec.length==0){
           WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.rem.66")],2);
           this.productStore.clearFilter();
           return;
       }
       Wtf.MessageBox.show({
           title: WtfGlobal.getLocaleText("acc.common.confirm"),
           msg: WtfGlobal.getLocaleText("acc.rem.63"),  //"Shown product(s) will be syncronized with CRM. Are you sure you want to synchronize the product(s)?",
           width: 560,
           buttons: Wtf.MessageBox.OKCANCEL,
           animEl: 'upbtn',
           icon: Wtf.MessageBox.QUESTION,
           scope:this,
           fn:function(btn){
               if(btn!="ok"){this.productStore.clearFilter();
                    var num= (this.productStore.indexOf(this.arrRec[0]))%2;
                   WtfGlobal.highLightRowColor(this.grid,this.arrRec,false,num,2);
                     return;
                }
                else {
                        Wtf.Ajax.requestEx({
    //                        url:Wtf.req.account+'CompanyManager.jsp',
                            url:"ACCCompanySetup/sendAccProducts.do",
                            params: {
                                ids:arrID
                            }
                        },this,this.genSyncSuccessResponse,this.genSyncFailureResponse);
                }
        }});
    },
     genSyncSuccessResponse:function(response){
            for(var i=0;i<this.arrRec.length;i++){
             var ind=this.productStore.indexOf(this.arrRec[i])
             var num= ind%2;
             WtfGlobal.highLightRowColor(this.grid,this.arrRec[i],false,num,2,true);
        }
        this.productStore.clearFilter();
        if(!response.companyexist)
            this.callSubscriptionWin()
        else if(response.success){
                 WtfComMsgBox([WtfGlobal.getLocaleText("acc.productList.gridProduct"),response.msg],response.success*2+1);

//            (function(){
//            }).defer(WtfGlobal.gridReloadDelay(),this);
            }

    },
    genSyncFailureResponse:function(response){
          for(var i=0;i<this.arrRec.length;i++){
             var ind=this.productStore.indexOf(this.arrRec[i])
             var num= ind%2;
             WtfGlobal.highLightRowColor(this.grid,this.arrRec[i],false,num,2,true);
        }this.productStore.clearFilter();
        var msg=WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
   },
    showPriceReport:function(){
       var productid=this.grid.getSelectionModel().getSelected().data.productid;
       var product=this.grid.getSelectionModel().getSelected().data.productname;
       callPriceReport(productid, product);
    },
    showInventoryReport:function(){
       var productid=this.grid.getSelectionModel().getSelected().data.productid;
       var productname=this.grid.getSelectionModel().getSelected().data.productname;
       var rate= this.grid.getSelectionModel().getSelected().data.saleprice;
       var producttype=this.grid.getSelectionModel().getSelected().data.producttype;
       if(producttype==Wtf.producttype.service){
           WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.rem.64")],2);
           return;
       }
       callInventoryReport(productid, productname,rate);
    },
    endDeleteColor:function(){
       // WtfGlobal.highLightRowColor(this.grid,this.arrRec,false,0,3);
        var count=this.arrRec.length;
        for(var i=0;i<count;i++){
            WtfGlobal.highLightRowColor(this.grid,this.arrRec[i],true,0,3);
        }
    },
    genSuccessResponse:function(response){
        for(var i=0;i<this.arrRec.length;i++){
             var ind=this.productStore.indexOf(this.arrRec[i])
             var num= ind%2;
             WtfGlobal.highLightRowColor(this.grid,this.arrRec[i],false,num,2,true);
        }
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.productList.gridProduct"),response.msg],response.success*2+1);
            if(response.success){
            (function(){

                Wtf.productStore.reload();
                Wtf.productStoreSales.reload();
                this.productStore.reload();
            }).defer(WtfGlobal.gridReloadDelay(),this);
            }

    },
    genFailureResponse:function(response){
          for(var i=0;i<this.arrRec.length;i++){
             var ind=this.productStore.indexOf(this.arrRec[i])
             var num= ind%2;
             WtfGlobal.highLightRowColor(this.grid,this.arrRec[i],false,num,2,true);
        }
        var msg=  WtfGlobal.getLocaleText("acc.common.msg1");//"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
   },
   callSubscriptionWin:function(){
       var m = Wtf.DomainPatt.exec(window.location);
       m="http://apps.deskera.com/"+m[0];
       var subscribePanel = new Wtf.FormPanel({
                    width:'80%',
                    method :'POST',
                    scope: this,
                    border:false,
                    fileUpload : true,
                    waitMsgTarget: true,
                    labelWidth: 70,
                    bodyStyle: 'font-size:10px;padding:10px;',
                    layout: 'form',
                    items:[{
                        border:false,
                        html:"<div style = 'font-size:12px; width:100%;height:100%;position:relative;float:left;'>"
                                +"Data syncing operation between CRM and Accounting can't be performed as you are not currently subscribed to CRM."
                                +" In order to subscribe to CRM, click <a target='_blank' class='linkCls' href="+m+"> <b> Subscribe </b> </a> else click <b>Cancel</b>"
                                +"</div>"
                    }]
                },
                this);
                var impWin1 = new Wtf.Window({
                    resizable: false,
                    scope: this,
                    layout: 'border',
                    modal:true,
                    width: 380,
                    height: 220,
                    border : false,
                    iconCls: 'pwnd deskeralogoposition',
                    title: 'Data Syncing',
                    items: [
                            {
                                region:'north',
                                height:70,
                                border : false,
                                bodyStyle : 'background:white;',
                                html: getTopHtml("Dear "+ _fullName+",", "",null,true)
                            },{
                                region:'center',
                                layout:'fit',
                                border:false,
                                bodyStyle : 'background:white;',
                                items:[subscribePanel]
                            }
                    ],
                    buttons: [{
                        text:WtfGlobal.getLocaleText("acc.common.cancelBtn"), //'Cancel',
                        id:'canbttn1',
                        scope:this,
                        handler:function() {
                            impWin1.close();
                        }
                    }]
                },this);

                impWin1.show();}
});
