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

Wtf.account.SuggestedReorder=function(config){
    this.Product=null;
    this.addproductwin=config.addproduct;
    this.arrRec=[];
    this.isEdit=false;
    this.addNew=false;
    this.productID=null;
    this.productlinkid=config.productlinkid;
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
    {name: 'level'},
    {name: 'initialquantity',mapping:'initialquantity'},
    {name: 'initialprice'},
    {name: 'ccountinterval'},
    {name:'ccounttolerance'}
]);

this.productStore = new Wtf.data.Store({
//    url:Wtf.req.account+'CompanyManager.jsp',
    url : "ACCProduct/getSuggestedReorderProducts.do",
    baseParams:{mode:30},
    reader: new Wtf.data.KwlJsonReader({
        root: "data",
        totalProperty:"count"
    },this.productRec)
});
this.productStore.on('load',this.hideMsg,this);
this.productStore.load();
//    this.productStore.load({
//        params:{
//            start:0,
//            limit:15
//        }
//    });
    WtfComMsgBox(29,4,true);
    var btnArr=[];
    this.localSearch = new Wtf.KWLLocalSearch({
        emptyText:WtfGlobal.getLocaleText("acc.productList.searchText"),  //'Search by product',
        width: 150,
        searchField: "productname"
    });
    btnArr.push(this.localSearch);
    var productArr=[];
    this.sm = new Wtf.grid.CheckboxSelectionModel();
    this.grid = new Wtf.grid.HirarchicalGridPanel({
        store:this.productStore,
        sm:this.sm,
        border:false,
        hirarchyColNumber:1,
        layout:'fit',
        loadMask:true,
        viewConfig:{forceFit:true},
        forceFit:true,
        columns:[this.sm,{
           header:WtfGlobal.getLocaleText("acc.productList.gridProduct"),  //"Product",
           dataIndex:'productname'
         },{
            header:WtfGlobal.getLocaleText("acc.productList.gridDescription"),  //"Description",
            dataIndex:'desc',
            renderer : function(val) {
                 return "<div wtf:qtip=\""+val+"\" wtf:qtitle='"+WtfGlobal.getLocaleText("acc.product.description")+"'>"+val+"</div>";
         }
         },{
            hidden:true,
            dataIndex:'productid'
         },{
            header:WtfGlobal.getLocaleText("acc.product.uom"),  //"Unit Of Measure",
            dataIndex:'uomname',
            hidden:true
         },{
            header:WtfGlobal.getLocaleText("acc.productList.gridProductType"),  //"Product Type",
            dataIndex:'type'
        },{
            header:WtfGlobal.getLocaleText("acc.productList.gridReorderQuantity"),  //"Reorder Quantity",
            dataIndex:'reorderquantity',
            align:'right',
            renderer:this.unitRenderer
        },{
            header:WtfGlobal.getLocaleText("acc.productList.gridReorderLevel"),  //"Reorder Level",
            dataIndex:'reorderlevel',
            align:'right',
            renderer:this.unitRenderer
        },{
            header:WtfGlobal.getLocaleText("acc.productList.gridLeadTime"),  //"Lead Time(in days)",
            dataIndex:'leadtime',
            renderer:this.LeadTimeRenderer
        },{
            header:WtfGlobal.getLocaleText("acc.productList.gridAvailableQty"),  //"Available Quantity",
            dataIndex:"quantity",
            align:'right',
            renderer:this.unitRenderer
        },{
            header:WtfGlobal.getLocaleText("acc.productList.gridPurchasePrice"),  //"Purchase Price",
            dataIndex:'purchaseprice',
            align:'right',
            renderer:WtfGlobal.currencyRenderer
        },{
            header:WtfGlobal.getLocaleText("acc.productList.gridSalesPrice"),  //"Sale Price",
            align:'right',
            dataIndex:'saleprice',
            renderer:WtfGlobal.currencyRenderer
        },{
            header:WtfGlobal.getLocaleText("acc.productList.gridCycleCountInterval"),  //"Cyclecount Interval",
            align:'right',
            dataIndex:'ccountinterval',
            renderer:function(v){return (v+' days');}
        },{
            header:WtfGlobal.getLocaleText("acc.productList.gridCycleCountTolerance"),  //"Cyclecount Tolerance",
            align:'right',
            dataIndex:'ccounttolerance',
            renderer:function(v){return'<div class="currency">'+v+'%</div>';}
        }],
        tbar:btnArr
//        bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
//            pageSize: 15,
//            id: "pagingtoolbar" + this.id,
//            store: this.productStore,
////            searchField: this.quickPanelSearch,
//            displayInfo: true,
//            displayMsg: 'Displaying records {0} - {1} of {2}',
//            emptyMsg: "No results to display",
//            plugins: this.pP = new Wtf.common.pPageSize({
//            id : "pPageSize_"+this.id
//            })
//        })
    });
    this.grid.on("render", function(grid) {
        WtfGlobal.autoApplyHeaderQtip(grid);
        this.localSearch.applyGrid(grid);
    },this);

    Wtf.account.SuggestedReorder.superclass.constructor.call(this,config);
}

Wtf.extend(Wtf.account.SuggestedReorder,Wtf.Panel,{
     updateGrid: function(obj,productID){
        this.productID=productID;
        this.productStore.reload();
        this.addNew=true;
        this.productStore.on('load',this.colorRow,this)
    },
    colorRow: function(store){
        if(this.addNew &(!this.isEdit)){
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
         if(this.productlinkid!=undefined)
             this.calllinkRowColor(this.productlinkid);
    },
   
    unitRenderer:function(value,metadata,record){
        var unit=record.data['uomname'];
            value=value+" "+unit;
        return value;
    },
    LeadTimeRenderer:function(value){
        if(value==1){
            value=value+" Day";
        }
        else{
            value=value+" Days";
        }
        return value;
    },
    
    onRender: function(config){

        this.add(this.grid);
        Wtf.account.SuggestedReorder.superclass.onRender.call(this, config);
    }

});
