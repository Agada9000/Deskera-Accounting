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
Wtf.account.productAssemblyGrid = function (config){
    this.bodyBorder=config.bodyBorder;
    Wtf.apply(this,config);
    Wtf.account.productAssemblyGrid.superclass.constructor.call(this);
}

Wtf.extend(Wtf.account.productAssemblyGrid,Wtf.Panel,{
    initComponent:function (){
        this.addEvents({
            'updatedcost':true,
            'updatedbuilds':true
        });

        Wtf.account.productAssemblyGrid.superclass.initComponent.call(this);
        this.maxbuilds = 0;
        this.totalcost = 0;
        this.globalProductCount = Wtf.productStore.getCount();
       this.gridRec = Wtf.data.Record.create ([
            {name:'id'},
            {name:'productid'},
            {name:'productname'},
            {name:'desc'},
            {name:'purchaseprice'},
            {name:'saleprice'},
            {name:'producttype'},
            {name:'type'},
            {name:'onhand'},
            {name:'quantity'},
            {name:'total'}
        ]);
        this.gridStore = new Wtf.data.Store({
//            url:Wtf.req.account+'CompanyManager.jsp',
            url:"ACCProduct/getAssemblyItems.do",
            baseParams:{mode:25},
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.gridRec)
        });

       this.productRec = Wtf.data.Record.create ([
            {name:'productid'},
            {name:'productname'},
            {name:'desc'},
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
            {name:'producttype'},
            {name:'type'},
            {name:'purchaseprice'},
            {name:'saleprice'},
            {name: 'leaf'},
            {name: 'level'},
            {name:'pid'}
        ]);
        this.productStore = new Wtf.data.Store({
//            url:Wtf.req.account+'CompanyManager.jsp',
            url:"ACCProduct/getProducts.do",
            baseParams:{mode:22},
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.productRec)
        });
        if(this.globalProductCount>0){
            this.cloneProductList();
        }
        chkproductload();
        Wtf.productStore.on("load", this.cloneProductList, this);
        
        this.productEditor=new Wtf.form.ExtFnComboBox({
            name:'productname',
            store:this.productStore,
            typeAhead: true,
            selectOnFocus:true,
            valueField:'productid',
            displayField:'productname',
            extraFields:['pid','type'],
            listWidth:400,
            scope:this,
            forceSelection:true
        });

        this.gridcm = new Wtf.grid.ColumnModel([
                new Wtf.grid.RowNumberer(),
                {
                    header:WtfGlobal.getLocaleText("acc.product.gridProduct"),//"Product",//
                    dataIndex:'productid',
                    renderer:Wtf.comboBoxRenderer(this.productEditor),
                    editor:this.productEditor
                },{
                    header:WtfGlobal.getLocaleText("acc.product.gridDesc"),//"Description",//
                    dataIndex:'desc',
                    renderer : function(val) {
                        return "<div wtf:qtip=\""+val+"\" wtf:qtitle="+WtfGlobal.getLocaleText("acc.product.gridDesc")+">"+val+"</div>";
                    }
                },{
                    header:WtfGlobal.getLocaleText("acc.product.gridType"),//"Type",//
                    dataIndex:'type'
                },{
                    header:WtfGlobal.getLocaleText("acc.product.gridCost"),//"Cost",//
                    dataIndex:'purchaseprice',
                    align:'right',
                    renderer: function(a,b,c){
//                        if(c.data.producttype == Wtf.producttype.service){//Service Item
////                            c.data.purchaseprice=c.data.saleprice;
////                            a=c.data.saleprice;
//                            return WtfGlobal.currencyRenderer(c.data.saleprice);
//                        }else{
                            return WtfGlobal.currencyRenderer(a);
//                        }
                   } 
                },{
                    header:WtfGlobal.getLocaleText("acc.product.gridQtyonHand"),//"Quantity On Hand",
                    dataIndex:'onhand',
                    align:'right',
                    renderer: function(a,b,c){
                        if(c.data.producttype != Wtf.producttype.service){//Service Item
                            return a;
                        }else{
                            return '';
                        }
                    }
                },{
                    header:this.rendermode=="productform"?WtfGlobal.getLocaleText("acc.product.gridQty"):WtfGlobal.getLocaleText("acc.product.gridQtyneeded"),//"Quantity" : "Quantity Needed",
                    align:'right',
                    dataIndex:'quantity',
                    editor:new Wtf.form.NumberField({ allowNegative:false, allowDecimals:false})
                },{
                    header:WtfGlobal.getLocaleText("acc.product.gridTotal"),//"Total",//
                    align:'right',
                    dataIndex:'total',
                    renderer: function(a,b,c){
                        if(c.data.quantity != "") {
//                            var price = (c.data.producttype!=Wtf.producttype.service)?c.data.purchaseprice:c.data.saleprice;//service
                            var price = c.data.purchaseprice
                            a = price * c.data.quantity;
                            c.data.total = a;
                            return WtfGlobal.currencyRenderer(a);
                        } else {
                            return "";
                        }
                    }
                },{
                    header:WtfGlobal.getLocaleText("acc.product.gridAction"),//"Action",//
                    align:'center',
                    width:30,
                    renderer: this.deleteRenderer.createDelegate(this)
                }]);


        this.itemsgrid = new Wtf.grid.EditorGridPanel({
            layout:'fit',
            region:"center",
            clicksToEdit:1,
            store: this.gridStore,
            cm:this.gridcm,
            border : false,
            loadMask : true,
            viewConfig: {
                forceFit:true,
                emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
            }
        });

        this.tplSummary=new Wtf.XTemplate("<div style='float:right;margin-right:20px'><span>"+WtfGlobal.getLocaleText("acc.product.gridTotalBOMcost")+" </span><span>"+WtfGlobal.getCurrencySymbol()+" <b>{total}</b> </span></div>");

        if(this.rendermode=="productform"){
            this.gridcm.setEditable(1,true); //Product
            this.gridcm.setEditable(6,true); //Quantity
            this.gridcm.setHidden(5,true); //Quantity on hand
        }else if(this.rendermode=="buildproduct"){
            this.gridcm.setEditable(1,false); //Product
            this.gridcm.setEditable(6,false); //Quantity
//            this.gridcm.setHidden(4,true); //Cost
            this.gridcm.setHidden(7,true); //TotalCost
            this.gridcm.setHidden(8,true); //Action
            this.tplSummary=new Wtf.XTemplate("<div style='float:right;margin-right:20px'><span>"+WtfGlobal.getLocaleText("acc.build.13")+" "+"</span><span> <b>{total}</b> </span></div>");
        }

        this.northHeaderPanel=new Wtf.Panel({
            region:"north",
            height:30,
            border:false,
            bodyStyle:"background-color:#f1f1f1;padding:8px",
            html:"<div>"+this.gridtitle+"</div>"
        });

        this.southSummaryPanel=new Wtf.Panel({
            region:"south",
            height:30,
            border:false,
            bodyStyle:"background-color:#f1f1f1;padding:8px",
            html:this.tplSummary.apply({total:0})
        });

        this.wraperPanel = new Wtf.Panel({
            layout:"border",
            border : this.bodyBorder==true?true:false,
//            height:200,
            items:[
                this.northHeaderPanel,
                this.itemsgrid,
                this.southSummaryPanel
            ]
        });

        if(this.productid){
            this.gridStore.load({
                params:{
                    productid:this.productid
                }
            });
        }

        this.gridStore.on("load",function(){
//            var parentindex = this.productStore.find('productid',this.productid);
//            if(parentindex>=0){ //Remove main assembly product from dropdown
//                var productrec=this.productStore.getAt(parentindex);
//                this.productStore.remove(productrec);
//            }
            if(this.gridStore.getCount()==0){
                this.itemsgrid.getView().emptyText=WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
                this.itemsgrid.getView().refresh();
            }
            if(this.rendermode=="productform"){
                this.addBlankRecord();
                this.updateSubtotal();
            }else if(this.rendermode=="buildproduct"){
                this.updateNoBuildFooter();
            }
        },this);

        this.itemsgrid.on('afteredit',this.updateRecord,this);
        this.itemsgrid.on('validateedit',this.validateRecord,this);
        this.itemsgrid.on('rowclick',this.handleRowClick,this);
        this.add(this.wraperPanel);
        if(this.rendermode=="productform"){
             this.addBlankRecord();
        }
    },
    addBlankRecord:function(){
        var newrec = new this.gridRec({
            productid:"",
            productname:"",
            desc:"",
            type:"",
            purchaseprice:"",
            saleprice:"",
            onhand:"",
            quantity:"",
            total:""
        });
        this.gridStore.add(newrec);
    },
    updateRecord:function(e){
        
        if(e.field=="productid"){
            if(e.row==this.gridStore.getCount()-1){
            this.addBlankRecord();
        }
            var productrec=this.productStore.getAt(this.productStore.find('productid',e.value));
            e.record.set("productname",productrec.data["productname"]);
            e.record.set("desc",productrec.data["desc"]);
            e.record.set("purchaseprice",productrec.data["purchaseprice"]);
            e.record.set("saleprice",productrec.data["saleprice"]);
            e.record.set("type",productrec.data["type"]);
            e.record.set("producttype",productrec.data["producttype"]);
            e.record.set("quantity",1);
        }
        this.updateSubtotal();
    },
    validateRecord:function(e){
        if(e.field=="productid"){
            if(this.gridStore.find("productid",e.value)>=0){
                var productrec=this.productStore.getAt(this.productStore.find('productid',e.value));
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.rem.76")+" "+productrec.data['productname']], 2);
                e.cancel=true;
            }
        }else if(e.field=="quantity"){
            if(e.value<=0){
                e.cancel=true;
            }
        }
    },
    updateSubtotal:function(){
        var subtotal=0, price = 0;
        for(var i=0;i<this.gridStore.getCount()-1;i++){
            var rec=this.gridStore.getAt(i);
//            price = (rec.data['producttype']!=Wtf.producttype.service)?rec.data['purchaseprice']:rec.data['saleprice'];//service
            price = rec.data['purchaseprice'];
            subtotal+=rec.data['quantity']*price;
        }
        this.totalcost = subtotal;
        this.fireEvent('updatedcost',this);
        this.tplSummary.overwrite(this.southSummaryPanel.body,{total:subtotal});
    },
    updateNoBuildFooter: function(){
        var maxbuilds = 0, k=0;
        for(var j=0;j<this.gridStore.getCount();j++){
            var producttype = this.gridStore.getAt(j).data['producttype'];
            if(producttype!="4efb0286-5627-102d-8de6-001cc0794cfa"){//service
                var onhand = this.gridStore.getAt(j).data['onhand'];
                onhand = (onhand<0)?0:onhand;
                var mx = onhand/this.gridStore.getAt(j).data['quantity'];
                mx = Math.floor(mx);
                if(k==0){
                    maxbuilds = mx;
                }else{
                    maxbuilds = (maxbuilds<mx)?maxbuilds:mx;
                }
                k++;
            }
        }
        this.maxbuilds = maxbuilds;
        this.fireEvent('updatedbuilds',this);
        this.tplSummary.overwrite(this.southSummaryPanel.body,{total:maxbuilds});
    },
    getAssemblyJson:function(){
        var cnt = this.gridStore.getCount()-1;
        if(this.rendermode=="buildproduct"){
            cnt = this.gridStore.getCount();
        }
        
        var jsonstring="";
        if(this.gridStore.getCount()>0){
            for(var i=0;i<cnt;i++){
                var rec = this.gridStore.getAt(i);
                jsonstring += "{product:\""+rec.data['productid']+"\","+
//                                "rate:"+(rec.data['producttype']!=Wtf.producttype.service?rec.data['purchaseprice']:rec.data['saleprice'])+","+
                                "rate:"+rec.data['purchaseprice']+","+
                                "quantity:"+rec.data['quantity']+"},";
            }
            jsonstring = jsonstring.substr(0, jsonstring.length-1);
        }
        return jsonstring;
    },
    setProduct:function(productid){
        if(productid){
            this.gridStore.load({
                params:{
                    productid:productid
                }
            });
        }
    },
    deleteRenderer:function(v,m,rec){
        var flag=false;
        var cm=this.itemsgrid.getColumnModel();
        var count = cm.getColumnCount();
        for(var i=0;i<count-1;i++){
            if(cm.getDataIndex(i).length > 0 && !cm.isHidden(i)) {
                if(rec.data[cm.getDataIndex(i)].length>0){
                    flag=true;
                    break;
                }
            }
        }
        if(flag){
            return "<div class='"+getButtonIconCls(Wtf.etype.deletegridrow)+"'></div>";
        }
        return "";
    },
    handleRowClick:function(grid,rowindex,e){
        if(e.getTarget(".delete-gridrow")){
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.setupWizard.note27"), function(btn){
                if(btn!="yes") return;
                var store=grid.getStore();
                var total=store.getCount();
                store.remove(store.getAt(rowindex));
                if(rowindex==total-1){
                    this.addBlankRecord();
                }
                if(this.rendermode=="productform"){
                    this.updateSubtotal();
                }
            }, this);
        }
    },
    cloneProductList: function(){
        if(Wtf.getCmp(this.id)){//Product Assembly Grid.
            Wtf.productStore.each(function(rec){
                if(this.productid){//Edit Case
                    if(rec.data.productid!=this.productid){
                        this.productStore.add(rec);
                    }
                } else {
                    this.productStore.add(rec);
                }
            },this);
        }
    }
});
