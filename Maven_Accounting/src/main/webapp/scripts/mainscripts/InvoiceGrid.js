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
Wtf.account.ProductDetailsGrid=function(config){
    this.isCustomer=config.isCustomer;
    this.productComboStore=this.isCustomer?Wtf.productStoreSales:Wtf.productStore;
    this.currencyid=config.currencyid;
    this.productID=null;
    this.id=config.id;
    this.isOrder=config.isOrder;
    this.record=config.record;
    this.billDate=new Date();
    this.dateChange=false;
    this.pronamearr=[];
    this.fromPO=config.fromPO;
    this.readOnly=config.readOnly;
    this.copyInv=config.copyInv; 
    this.editTransaction=config.editTransaction;
    this.noteTemp=config.noteTemp;
    this.fromOrder=config.fromOrder;
    if(config.isNote!=undefined)
        this.isNote=config.isNote;
    else
        this.isNote=false;
    this.isCN=config.isCN;
    this.isQuotation=config.isQuotation;
    this.createStore();
    this.createComboEditor();
    this.createColumnModel();

    Wtf.account.ProductDetailsGrid.superclass.constructor.call(this,config);
    this.addEvents({
        'datachanged':true,
        'pricestoreload':true
    });
}
Wtf.extend(Wtf.account.ProductDetailsGrid,Wtf.grid.EditorGridPanel,{
    clicksToEdit:1,
    stripeRows :true,
    rate:1,
    symbol:null,
    layout:'fit',
    viewConfig:{forceFit:true},
    forceFit:true,
    loadMask:true,
    onRender:function(config){
         Wtf.account.ProductDetailsGrid.superclass.onRender.call(this,config);
         this.isValidEdit = true;
         this.on('render',this.addBlankRow,this);
         this.on('afteredit',this.updateRow,this);
         this.on('validateedit',this.checkRow,this);
         this.on('rowclick',this.handleRowClick,this);
         this.on('beforeedit',function(e){
             if(!this.isValidEdit){ // Fixed Bug[13888]: Overlaping text box on validation alert messages. [on TAB key navigation]
                 e.cancel= true;
                 this.isValidEdit = true;
             }
             if(e.field == "rate"){	// rate editable for product type "Service" 
           		 var beforeEditRecord=this.productComboStore.getAt(this.productComboStore.find('productid',e.record.data.productid));
            	 if(beforeEditRecord == undefined || beforeEditRecord == null){
            		 e.cancel = true;
            	 }//else{
//            		   if(beforeEditRecord.data.producttype != Wtf.producttype.service){
//	               	   e.cancel = true;
//	            	 }
//            	 }
             }
         },this);
         if(!this.isNote && !this.readOnly){
	         if(this.record == null || this.record == undefined && this.getColumnModel().getColumnById(this.id+"prtaxid").hidden == undefined && this.getColumnModel().getColumnById(this.id+"taxamount").hidden == undefined){
	        	 this.getColumnModel().setHidden(11, true);				// 21241   If statement added bcos could not use the event destroy for column model
	        	 this.getColumnModel().setHidden(12, true);							// and also could not call the createColumnModel() method from onRender
	         }
         }
     },
     createStore:function(){
         this.priceRec = Wtf.data.Record.create ([
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
            {name:'salespricedatewise'},
            {name:'purchasepricedatewise'},
            {name:'salesretaccountid'},
            {name:'reorderquantity'},
            {name:'pricedatewise'},
            {name:'quantity'},
            {name:'reorderlevel'},
            {name:'leadtime'},
            {name:'purchaseprice'},
            {name:'saleprice'},
            {name: 'leaf'},
            {name: 'type'},
            {name:'prtaxid'},
            {name:'taxamount'},
            {name:'prtaxpercent'},
            {name:'prtaxname'},
        //        {name: 'currencysymbol'},
        //        {name: 'currencyrate'},
            {name: 'level'},
            {name: 'initialquantity',mapping:'initialquantity'},
            {name: 'initialprice'},
            {name: 'producttype'}
        ]);

        this.priceStore = new Wtf.data.Store({
        //        url:Wtf.req.account+'CompanyManager.jsp',
            url:"ACCProduct/getProducts.do",
            baseParams:{mode:22
//            	loadInventory:this.isCustomer
                },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.priceRec)
        });
        
//        this.productComboStore = new Wtf.data.Store({
//            //        url:Wtf.req.account+'CompanyManager.jsp',
//                url:"ACCProduct/getProducts.do",
//                baseParams:{
//                	loadInventory:this.isCustomer
//                    },
//                reader: new Wtf.data.KwlJsonReader({
//                    root: "data"
//                },this.priceRec)
//            });
//        this.productComboStore.load();
        
        this.priceStore.on('load',this.setGridProductValues,this);
//       this.priceStore.load({params:{transactiondate:WtfGlobal.convertToGenericDate(this.billDate)}});
        this.storeRec = Wtf.data.Record.create([
            {name:'rowid'},
            {name:'productname'},
            {name:'billid'},
            {name:'billno'},
            {name:'productid'},
            {name:'desc'},
            {name:'quantity'},
            {name:'copyquantity',mapping:'quantity'},
            {name:'rate'},
            {name:'rateinbase'},
            {name:'discamount'},
            {name:'discount'},
            {name:'prdiscount'},
            {name:'prtaxid'},
            {name:'prtaxname'},
            {name:'prtaxpercent'},
            {name:'taxamount'},
            {name:'amount'},
            {name:'amountwithtax'},
            {name:'taxpercent'},
            {name:'remark'},
            {name:'transectionno'},
            {name:'remquantity'},
            {name:'remainingquantity'},
            {name:'oldcurrencyrate'},
            {name: 'currencysymbol'},
            {name: 'currencyrate'},
            {name: 'externalcurrencyrate'},
            {name:'orignalamount'},
            {name:'typeid'},
            {name:'isNewRecord'},
            {name:'producttype'}
        ]);
        var url=Wtf.req.account+((this.fromOrder||this.readOnly)?((this.isCustomer)?'CustomerManager.jsp':'VendorManager.jsp'):((this.isCN)?'CustomerManager.jsp':'VendorManager.jsp'));
        if(this.fromOrder)
           url=Wtf.req.account+(this.isCustomer?'CustomerManager.jsp':'VendorManager.jsp');
        this.store = new Wtf.data.Store({
            url:url,
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.storeRec)
        });
        this.store.on('load',this.loadPOProduct,this);
 //       chkProductPriceload();
    },  
    createComboEditor:function(){
        this.poProductRec = Wtf.data.Record.create ([
            {name:'productid'},
            {name:'quantity'},
            {name:'prtaxid'}
        ]);
        this.poProductStore = new Wtf.data.Store({
            //url:Wtf.req.account+(this.isCustomer?'CustomerManager.jsp':'VendorManager.jsp'),
            url:this.isCustomer?'ACCSalesOrderCMN/getSalesOrderRows.do':'ACCPurchaseOrderCMN/getPurchaseOrderRows.do',
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.poProductRec)
        });
//        chkproductload();
        if(this.isCustomer)
        	chkproductSalesload();
        else
        	chkproductload();
        this.productEditor=new Wtf.form.ExtFnComboBox({
            name:'productname',
            store:this.productComboStore,       //Wtf.productStore Previously, now changed bcos of addition of Inventory Non sale product type
            typeAhead: true,
            selectOnFocus:true,
            valueField:'productid',
            displayField:'productname',
            extraFields:['pid','type'],
            listWidth:400,
            //editable:false,
            scope:this,
            hirarchical:true,
           // addNewFn:this.openProductWindow.createDelegate(this),
            forceSelection:true
        });
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.product, Wtf.Perm.product.edit))
            this.productEditor.addNewFn=this.openProductWindow.createDelegate(this);

        this.productComboStore.on("load",this.loadPriceAfterProduct,this);
        this.typeStore = new Wtf.data.SimpleStore({
            fields: [{name:'typeid',type:'int'}, 'name'],
            data :[[0,'Normal'],[1,'Defective'],[2,'Return'],[3,'Defective Return'] ]
        });
        this.typeEditor = new Wtf.form.ComboBox({
            store: this.typeStore,
            name:'typeid',
            displayField:'name', 
            valueField:'typeid',
            mode: 'local',
            triggerAction: 'all',
            selectOnFocus:true
        });
        this.remark= new Wtf.form.TextField({
            name:'remark'
        });
        this.transDiscount=new Wtf.form.NumberField({
            allowBlank: false,
            allowNegative: false,
            defaultValue:0
        });
        this.taxRec = new Wtf.data.Record.create([
           {name: 'prtaxid',mapping:'taxid'},
           {name: 'prtaxname',mapping:'taxname'},
           {name: 'percent',type:'float'},
           {name: 'taxcode'},
           {name: 'accountid'},
           {name: 'accountname'},
           {name: 'applydate', type:'date'}

        ]);
        this.taxStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.taxRec),
    //        url: Wtf.req.account + 'CompanyManager.jsp',
            url : "ACCTax/getTax.do",
            baseParams:{
                mode:33
            }
        });
        if(this.readOnly)
            this.taxStore.load();
        
        this.transTax= new Wtf.form.FnComboBox({
            hiddenName:'prtaxid',
            anchor: '100%',
            store:this.taxStore,
            valueField:'prtaxid',
            forceSelection: true,
            displayField:'prtaxname',
//            addNewFn:this.addTax.createDelegate(this),
            scope:this,
            selectOnFocus:true
        });
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.tax, Wtf.Perm.tax.edit))
            this.transTax.addNewFn=this.addTax.createDelegate(this);
        this.transQuantity=new Wtf.form.NumberField({
            allowBlank: false,
            allowNegative: false,
            maxLength:10
        });
        this.editprice=new Wtf.form.NumberField({
            allowBlank: false,
            allowNegative: false,
//            minValue:0.01,
            maxLength:10
        });  
    },
    addTax:function(){
         this.stopEditing();
         var p= callTax("taxwin");
         Wtf.getCmp("taxwin").on('update', function(){this.taxStore.reload();}, this);
    },
    loadPriceAfterProduct : function(){
        if(Wtf.getCmp(this.id)){ //Load price store if component exists
            this.loadPriceStore();
        } else {
            this.productComboStore.un("load",this.loadPriceAfterProduct,this);//Remove event handler if Not exists
        }
    },
    loadPriceStore:function(val){
        this.billDate=(val==undefined?this.billDate:val);
        //if(this.editTransaction)
//         this.priceStore.on('load',this.setGridProductValues.createDelegate(this),this)
        this.priceStore.load({params:{transactiondate:WtfGlobal.convertToGenericDate(this.billDate)}});
    },
    loadPriceStoreOnly:function(val,pricestore){  //scope related issue
        this.dateChange=true;
        this.billDate=(val==undefined?this.billDate:val);        //if(this.editTransaction)
        pricestore.load({params:{transactiondate:WtfGlobal.convertToGenericDate(this.billDate)}});
    },
    openProductWindow:function(){
        this.stopEditing();
        callProductWindow(false, null, "productWin");
       // this.productStore.on('load',function(){this.productStore.})
        Wtf.getCmp("productWin").on("update",function(obj,productid){this.productID=productid;},this);
    },

    createColumnModel:function(){
        this.summary = new Wtf.ux.grid.GridSummary();
        this.rowno=(this.isNote)?new Wtf.grid.CheckboxSelectionModel():new Wtf.grid.RowNumberer();
        var columnArr = [this.rowno,{
            dataIndex:'rowid',
            hidelabel:true,
            hidden:true
        },{
            dataIndex:'billid',
            hidelabel:true,
            hidden:true
        },{
            header:WtfGlobal.getLocaleText("acc.invoice.gridProduct"),//"Product",
            width:200,
            dataIndex:this.readOnly?'productname':'productid',
            renderer:this.readOnly?"":Wtf.comboBoxRenderer(this.productEditor),
            editor:(this.isNote||this.readOnly)?"":this.productEditor
        },{
            header:this.isCN?WtfGlobal.getLocaleText("acc.invoice.gridInvNo"):WtfGlobal.getLocaleText("acc.invoice.gridVenInvNo"),//"Invoice No.":"Vendor Invoice No.",
            width:150,
            dataIndex:this.noteTemp?'transectionno':'billno',
            hidden:!this.isNote
        },{
             header:WtfGlobal.getLocaleText("acc.invoice.gridDescription"),//"Description",
             dataIndex:"desc",
             hidden:this.isNote,
             width:250,
             editor:(this.isNote||this.readOnly)?"":this.remark
         },{
             header:WtfGlobal.getLocaleText("acc.invoice.gridQty"),//"Quantity",
             dataIndex:"quantity",
             align:'right',
             width:100,
             renderer:this.storeRenderer(this.productComboStore,"productid","uomname"),
             editor:(this.isNote||this.readOnly)?"":this.transQuantity
         },{
             header:WtfGlobal.getLocaleText("acc.invoice.gridRemQty"),//"Remaining Quantity",
             dataIndex:"remainingquantity",
             align:'right',
             hidden:!this.isNote||this.noteTemp,
             width:150,
             renderer:this.storeRenderer(this.productComboStore,"productid","uomname"),
             editor:(this.isNote||this.readOnly)?"":this.transQuantity
        },{
             header:"<b> "+ WtfGlobal.getLocaleText("acc.invoice.gridEnterQty") +" </b>",//"<b>Enter Quantity</b>",
             dataIndex:"remquantity",
             align:'right',
             hidden:!this.isNote||this.noteTemp,
             width:180,
             renderer:this.storeRenderer(this.productComboStore,"productid","uomname"),
             editor:this.readOnly?"":this.transQuantity
        },{
             header:WtfGlobal.getLocaleText("acc.invoice.gridUnitPrice"),// "Unit Price", 
             dataIndex: "rate",
             align:'right',
             width:150,
             renderer:(this.isNote||this.readOnly?WtfGlobal.withoutRateCurrencySymbol:WtfGlobal.currencyRendererSymbol),
             editor:(this.isNote||this.readOnly)?"":this.editprice,
             editable:true,
             hidden: this.noteTemp
        },{
             header: WtfGlobal.getLocaleText("acc.invoice.gridDiscount"),//"Discount %",
             dataIndex:"prdiscount",
             align:'right',
             width:150,
             hidden:this.isQuotation?false:(this.isOrder || this.noteTemp),
             renderer:function(v){return'<div class="currency">'+v+'%</div>';},
             editor:this.readOnly||this.isNote?"":this.transDiscount
         },{
             header: WtfGlobal.getLocaleText("acc.invoice.proTax"),//"Tax",
             dataIndex:"prtaxid",
             id:this.id+"prtaxid",
             fixed:true,
             width:120,
             hidden:!(this.editTransaction||this.readOnly) || this.noteTemp,// || this.isOrder,
             renderer:Wtf.comboBoxRenderer(this.transTax),
             editor:this.readOnly||this.isNote?"":this.transTax  //this.transTax
        },{
             header: WtfGlobal.getLocaleText("acc.invoice.gridTaxAmount"),//"Tax Amount",
             dataIndex:"taxamount",
             id:this.id+"taxamount",
             //align:'right',
             width:150,
            hidden:!(this.editTransaction||this.readOnly)|| this.noteTemp, // || !this.isOrder,
             renderer:this.setTaxAmount.createDelegate(this) 
        },{
             header:WtfGlobal.getLocaleText("acc.invoice.Tax"),//"Tax",
             dataIndex:"taxpercent",
             align:'right',

             hidden:!this.isNote || this.noteTemp,
             width:200,
             renderer:function(v){return'<div class="currency">'+v+'%</div>';}
        },{
             header:WtfGlobal.getLocaleText("acc.invoice.gridProductTax"),//"Product Tax",
             dataIndex:"prtaxpercent",
             align:'right',
             hidden:!this.isNote || this.noteTemp,
             width:200,
             renderer:function(v){return'<div class="currency">'+v+'%</div>';}
        },{
             header:this.isNote?WtfGlobal.getLocaleText("acc.invoice.gridOriginalAmt"):WtfGlobal.getLocaleText("acc.invoice.gridInvAmt"),//"Original Amount":"Invoice Amount",
             dataIndex:"orignalamount",
             align:'right',
             width:150,
             hidden:!(this.isNote||this.readOnly) || this.noteTemp,
             renderer:(this.isNote||this.readOnly?WtfGlobal.withoutRateCurrencySymbol:WtfGlobal.currencyRendererSymbol)
        },{
             header:this.isNote?WtfGlobal.getLocaleText("acc.invoice.gridCurAmt"):WtfGlobal.getLocaleText("acc.invoice.gridAmount"),//"Current Amount ":"Amount",
             dataIndex:"amount",
             hidden:this.readOnly,
             align:'right',
             width:200,
             renderer:(this.isNote||this.readOnly?WtfGlobal.withoutRateCurrencySymbol:this.calAmount.createDelegate(this))

//    },{
//             header:"Amount (Including Tax)",
//             dataIndex:"amountwithtax",
//             align:'right',
//             hidden:!this.isNote,
//             width:200,
//             renderer:WtfGlobal.withoutRateCurrencySymbol

        },{
             header:(this.readOnly)?WtfGlobal.getLocaleText("acc.invoice.gridAmount"):"<b>"+ WtfGlobal.getLocaleText("acc.invoice.gridEnterAmt") + "</b>",//"Amount":"<b>Enter Amount</b>",
             dataIndex:this.noteTemp?'discount':'discamount',
             align:'right',
             width:200,
             hidden:!this.isNote,
             renderer:WtfGlobal.withoutRateCurrencySymbol,
             editor:this.readOnly?"":new Wtf.form.NumberField({
                allowBlank: false,
                allowNegative: false
             })
        },{ 
            header:WtfGlobal.getLocaleText("acc.invoice.gridNoteType"),//"Note Type",
            width:200,
            dataIndex:'typeid',
            hidden:(!this.isNote ||this.noteTemp),
            renderer:Wtf.comboBoxRenderer(this.typeEditor),
            editor:this.readOnly?"":this.typeEditor
        }];
        if(!this.isNote && !this.readOnly) {
            columnArr.push({
                header:WtfGlobal.getLocaleText("acc.invoice.gridAction"),//"Action",
                align:'center',
                width:40,
                renderer: this.deleteRenderer.createDelegate(this)
            });
        }
        this.cm=new Wtf.grid.ColumnModel(columnArr);
    },
    deleteRenderer:function(v,m,rec){
        return "<div class='"+getButtonIconCls(Wtf.etype.deletegridrow)+"'></div>";
    },
    handleRowClick:function(grid,rowindex,e){
        if(e.getTarget(".delete-gridrow")){
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.nee.48"), function(btn){
                if(btn!="yes") return;
                var store=grid.getStore();
                var total=store.getCount();
                store.remove(store.getAt(rowindex));
                if(rowindex==total-1){
                    this.addBlankRow();
                }
                this.fireEvent('datachanged',this);
            }, this);
        }
    },

    storeRenderer:function(store, valueField, displayField) {
        return function(value, meta, record) {
            var idx = store.find(valueField, record.data[valueField]);
            if(idx == -1)
                return value;
            var rec = store.getAt(idx);
            return value+" "+rec.data[displayField];
        }
    },

    checkRow:function(obj){
        var rec=obj.record;
        if(obj.field=="productid"){
            var index=this.productComboStore.findBy(function(rec){
                if(rec.data.productid==obj.value)
                    return true;
                else
                    return false;
            })
            var prorec=this.productComboStore.getAt(index);
            index=this.priceStore.find('productid',obj.value)
            rec=this.priceStore.getAt(index);
            if(this.store.find("productid",obj.value)>=0&&obj.ckeckProduct==undefined){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.rem.76")+" "+rec.data['productname']], 2);
                obj.cancel=true;
            }else if((this.isCustomer&&rec.data["salespricedatewise"]==0)||(!this.isCustomer&&rec.data["purchasepricedatewise"]==0)){
                 if(!WtfGlobal.EnableDisable(Wtf.UPerm.product, Wtf.Perm.product.addprice)){//permissions
                    Wtf.Msg.confirm(WtfGlobal.getLocaleText("acc.common.alert"),"Price for "+rec.data['productname']+" is not set. Do you want to set now?",
                    this.showPriceWindow.createDelegate(this,[rec, obj],true));
                }else{
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),"Price for "+rec.data['productname']+" is not set"], 2);
                }
                obj.cancel=true;
            }else if(this.isCustomer&&rec.data['quantity']<obj.record.data['quantity']&&prorec.data.producttype!=Wtf.producttype.service&&!this.isQuotation){
                this.isValidEdit = false;
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),'Maximum available quantity for '+rec.data['productname']+' is '+rec.data['quantity']], 2);
                obj.cancel=true;
            }
        }else if(this.isCustomer&&obj.field=="quantity"&&obj.record.data['productid'].length>0&&!this.isQuotation){
            prorec=this.productComboStore.getAt(this.productComboStore.find('productid',obj.record.data.productid));
            if(((!this.editTransaction||this.copyInv)&&prorec.data.quantity<obj.value&&prorec.data.producttype!=Wtf.producttype.service)||(this.editTransaction&&!this.copyInv&&obj.value>obj.record.data.copyquantity&&(obj.value-obj.record.data.copyquantity)>prorec.data.quantity&&prorec.data.producttype!=Wtf.producttype.service)){
             //  alert(this.editTransaction+"a----"+this.copyInv+"b----"+obj.value+"c----"+obj.record.data.copyquantity+"d----"+prorec.data.quantity+"e----"+obj.value)
                this.isValidEdit = false;
                var quantity=prorec.data.quantity;
                if((this.editTransaction&&!this.copyInv&&obj.value>obj.record.data.copyquantity&&(obj.value-obj.record.data.copyquantity)>prorec.data.quantity&&prorec.data.producttype!=Wtf.producttype.service)){
                    quantity=prorec.data.quantity+obj.record.data.copyquantity
                }
               WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),'Maximum available quantity for '+prorec.data.productname+' is '+quantity], 2);
                obj.cancel=true;
            }
        }
        if(this.isNote){
            if(obj.field=="typeid"&&(obj.value==0||obj.value==1))
                rec.set('remquantity',0);

            if(obj.field=="remquantity"){
                if(rec.data['typeid']==0||rec.data['typeid']==1){
                    obj.cancel=true;
                    WtfComMsgBox(["Alert",'Change the Note Type first' ], 2);
                    rec.set('remquantity',0);
                }
                else{
                    rec=this.store.getAt(this.store.find('productid',obj.record.data['productid']));
                    if(rec.data['remainingquantity']<obj.value){
                        WtfComMsgBox(["Alert",'Maximum available quantity for '+rec.data['productname']+ ' is '+rec.data.remainingquantity], 2);
                        obj.cancel=true;
                        rec.set('remquantity',0);
                    }
                }
            }
            if(obj.field=="typeid"){
                rec=this.store.getAt(this.store.find('productid',obj.record.data['productid']));
                if(rec.data['typeid']==0||rec.data['typeid']==1)
                    rec.set('remquantity',0);
            }
            if(obj.field=="discamount"){
                if(rec.data['orignalamount']<obj.value){
                    WtfComMsgBox(["Alert",'Remaining amount of '+rec.data['productname']+ ' for selected transaction  is '+WtfGlobal.getCurrencySymbol()+" "+(rec.data['amount'])], 2);
                    obj.cancel=true;
                    rec.set('discamount',0);
                }
            }
        }
    }, 
    addBlankRow:function(){
        var newrec = new this.storeRec({
            productname:"",
            productid:"",
            desc:"",
            quantity:1,
            rate:0,
            prdiscount:0,
            prtaxname:"",
            prtaxid:"",
            taxamount:0,
            amount:0,
            taxpercent:0,
            prtaxpercent:0,
            amountwithtax:0,
            remark:"",
            typeid:0,
            currencyrate:1,
            currencysymbol:this.symbol,
            oldcurrencyrate:1,
            isNewRecord:"1"
        });
        this.store.add(newrec);
    }, 
    updateRow:function(obj){
         if(obj!=null){
        	 this.productComboStore.clearFilter(); // Issue 22189
             var rec;         
             if(obj.field=="prdiscount" && obj.value >100){
                 rec=obj.record;
                        WtfComMsgBox(["Warning","Discount cannot be greater than 100 "], 2);
                        rec.set("prdiscount",0);
                  }
                  if(obj.field=="productid"){
                var index=this.priceStore.find('productid',obj.value);
                if(index>=0){
                    rec=this.priceStore.getAt(index);
                    obj.record.set("desc",rec.data["desc"]);

                    obj.record.set("quantity",1);
                    if(this.isCustomer)
                        obj.record.set("rate",rec.data["salespricedatewise"]);
                    else
                        obj.record.set("rate",rec.data["purchasepricedatewise"]);
                }
            }else if(obj.field=="quantity"){
                rec=obj.record;
                if((rec.data.isNewRecord=="" || rec.data.isNewRecord==undefined) && this.fromOrder&&!(this.editTransaction||this.copyInv)) {
                    if(obj.value!=rec.data.copyquantity) {
                        Wtf.MessageBox.confirm("Alert",this.isCustomer?"Product Quantity entered in Invoice is different from original quantity mentioned in SO. DO you want to continue?":"Product Quantity entered in Vendor Invoice is different from original quantity mentioned in PO. DO you want to continue?",function(btn){
                            if(btn!="yes") {obj.record.set(obj.field, obj.originalValue)}
                        },this)
                    }
                }
                if((obj.record.data["quantity"])==0){
                    this.store.remove(obj.record);
                }
            }else if(obj.field=="prtaxid"){
                rec=obj.record;
                var discount=rec.data.rate*rec.data.quantity*rec.data.prdiscount/100
                var val=(rec.data.quantity*rec.data.rate)-discount;
                var taxpercent=0;
               index=this.taxStore.find('prtaxid',rec.data.prtaxid);
                if(index>=0){
                    var taxrec=this.taxStore.getAt(index);
                    taxpercent=taxrec.data.percent;
                }
                var taxamount= (val*taxpercent/100);
                rec.set("taxamount",taxamount);
            }
        }
         if(!this.isNote){
        	if(obj != null || obj != undefined){ 
	         	if(obj.field=="rate")
	         		if(obj.value == 0){
//	         			Wtf.MessageBox.confirm(["Alert","Unit Price should be greater than Zero"],function(btn){
//	                         obj.record.set(obj.field, obj.originalValue);
//	                     },this);
	         			WtfComMsgBox(["Alert",'Unit Price should be greater than Zero' ], 2);
	         			obj.record.set(obj.field, obj.originalValue);
	         		}
        	}
         }
        
        this.fireEvent('datachanged',this);
        if(this.store.getCount()>0&&this.store.getAt(this.store.getCount()-1).data['productid'].length<=0)
            return;
        if(!this.isNote)
            this.addBlankRow();
    },
    calTaxAmount:function(rec){
        var discount=rec.data.rate*rec.data.quantity*rec.data.prdiscount/100
        var val=(rec.data.quantity*rec.data.rate)-discount;
        var taxpercent=0;
            var index=this.taxStore.find('prtaxid',rec.data.prtaxid);
            if(index>=0){
               var taxrec=this.taxStore.getAt(index);
                taxpercent=taxrec.data.percent;
            }
        return (val*taxpercent/100);

    },
    setTaxAmount:function(v,m,rec){
       var taxamount= this.calTaxAmount(rec);
       rec.set("taxamount",taxamount);
        if(this.isNote||this.readOnly)
             return WtfGlobal.withoutRateCurrencySymbol(taxamount,m,rec);
        return WtfGlobal.currencyRendererSymbol(taxamount,m,rec);
    },
    calAmount:function(v,m,rec){
        var discount=rec.data.rate*rec.data.quantity*rec.data.prdiscount/100
        var val=(rec.data.quantity*rec.data.rate)-discount;///rec.data.oldcurrencyrate
        var taxamount= this.calTaxAmount(rec);
        val+=taxamount;
        rec.set("amount",val);
       if(this.isNote||this.readOnly)
             return WtfGlobal.withoutRateCurrencySymbol(val,m,rec);
       return WtfGlobal.currencyRendererSymbol(val,m,rec);
    },     
    calSubtotal:function(){
        var subtotal=0;
        var count=this.store.getCount();
        for(var i=0;i<count;i++){
            var total=this.setCurrencyAmount(this.store.getAt(i).data['amount']);
            if(this.editTransaction&&!this.fromPO){
                total=total/this.store.getAt(i).data['oldcurrencyrate'];
            }
            subtotal+=total;
        }
        return subtotal;
    }, 
    addBlank:function(){
       this.setGridDiscValues();
        this.addBlankRow();
    },
    setGridDiscValues:function(){
        this.store.each(function(rec){
            if(!this.editTransaction)
                rec.set('prdiscount',0)
        },this);            
    },
    setGridProductValues:function(){
        var rate;
       this.pronamearr=[];
        this.store.each(function(record){
            var index=this.priceStore.find('productid',record.data.productid);
                if(index>=0){
                var rec=this.priceStore.getAt(index);
                    if(this.isCustomer){
                       rate=(record.data.rowid!=null?WtfGlobal.currencyRendererWithoutSymbol(rec.get('salespricedatewise'),null,record):rec.get('salespricedatewise'))
                   record.set("rate",rate);
                   if(rate==0)
                        this.pronamearr.push(rec.get('productname'))
                    }else{
                        rate=(record.data.rowid!=null?WtfGlobal.currencyRendererWithoutSymbol(rec.get('purchasepricedatewise'),null,record):rec.get('purchasepricedatewise'))
                    record.set("rate",rate);
                    if(rate==0)
                        this.pronamearr.push(rec.get('productname'))
                }
                }
                if(this.editTransaction){
                   record.set('oldcurrencyrate',record.get('currencyrate'));
                }                
                index=this.productComboStore.find('productid',record.data.productid);
                if(index>=0){
                    var prorec=this.productComboStore.getAt(index);
                    if((this.copyInv&&prorec.data.quantity<record.data.quantity&&prorec.data.producttype!=Wtf.producttype.service)){
                        WtfComMsgBox(["Alert",'Maximum available quantity for '+prorec.data.productname+' is '+prorec.data.quantity], 2);
                        record.set("quantity",0);
                    }
                }
            },this);    
            if( this.dateChange){//alert(this.pronamearr.length);//>0&&
                 this.fireEvent('pricestoreload', this.pronamearr,this);}
    }, 
    getProductDetails:function(){
        if(this.editTransaction && !this.isOrder){
            this.store.each(function(rec){//converting in home currency
                if(rec.data.rowid!=null){
                    var amount,rate;
                    if(this.record.data.externalcurrencyrate!=undefined&&this.record.data.externalcurrencyrate!=0){
                        amount=rec.get('amount')/this.record.data.externalcurrencyrate;
                        rate=rec.get('rate')/this.record.data.externalcurrencyrate;
                    }else{
                        amount=rec.get('amount')/rec.get('oldcurrencyrate');
                        rate=rec.get('rate')/rec.get('oldcurrencyrate');
                    }
                    rec.set('amount',amount);
                    rec.set('rate',rate);
                }
            },this);
        }
        var arr=[];
        this.store.each(function(rec){
            if(rec.data.rate!=0){
                arr.push(this.store.indexOf(rec));
            }
        })
        var jarray=WtfGlobal.getJSONArray(this,false,arr);
        //converting back in person currency
        this.store.each(function(rec){
            if(rec.data.rowid!=null && this.fromPO == false){
                var amount,rate;
                if(this.record.data.externalcurrencyrate!=undefined&&this.record.data.externalcurrencyrate!=0){
                    amount=rec.get('amount')*this.record.data.externalcurrencyrate;
                    rate=rec.get('rate')*this.record.data.externalcurrencyrate;
                }else{
                    amount=rec.get('amount')*rec.get('oldcurrencyrate');
                    rate=rec.get('rate')*rec.get('oldcurrencyrate');
                }
                rec.set('amount',amount);
                rec.set('rate',rate);
            }
        },this);
        return jarray;
    }, 
          
    getCMProductDetails:function(){
        var arr=[];
        var selModel=  this.getSelectionModel();
        var len=this.productComboStore.getCount();
        for(var i=0;i<len;i++){
            if(selModel.isSelected(i)){
            var rec =selModel.getSelected()
            if(rec.data.typeid==2||rec.data.typeid==3)
                if(rec.data.remquantity==0){
                    WtfComMsgBox(["Alert",'Please enter the quantity of product "'+rec.data.productname+'" you want to return' ], 2);
                    return "Error";
                }
            arr.push(i);
            }
            	// arr.push(i); moved to above line cos of issue no: 20258
        }
        return WtfGlobal.getJSONArray(this,true,arr);
    }, 
    loadPOProduct:function(){
      if(this.fromPO)
         this.store.each(function(rec){
              var taxamount= this.calTaxAmount(rec);
             rec.set("taxamount",taxamount);
             rec.set("prdiscount",0);
        },this);
      
      if(this.isCustomer && this.fromOrder && !this.isNote && !this.readOnly && !this.isOrder)
    	  this.checkSOLinkedProducts();
    },
    
    checkSOLinkedProducts:function(){
    	var msgBox = 0;
    	this.productComboStore.each(function(rec){
    		var result = this.store.find('productid',rec.data.productid);
    		
    		if(result >= 0){
    			var prorec=this.store.getAt(result);
    			if(rec.data.producttype!=Wtf.producttype.service && rec.data.quantity < prorec.data.quantity){
    				this.store.remove(this.store.getAt(result));
    				msgBox = 1;
    			}
    		}
    	},this);
    	
    	if(msgBox==1){
    		WtfComMsgBox(["Alert",'Available Qty for some Product(s) is below than the Qty mentioned in SO, so Please Edit the Qty given in SO first and then proceed.'], 2);
    		Wtf.getCmp('orderNumber2Invoice').setValue('');
    		Wtf.getCmp('linkToOrder2Invoice').setValue(false);
    	}
    },
    loadPOGridStore:function(rec){
        this.store.load({params:{bills:rec.data['billid'],mode:43,closeflag:true}});
    }, 
    showPriceWindow:function(btn,text,rec, obj){
        if(btn!="yes")return;
        callPricelistWindow(rec,"pricewindow",!this.isCustomer,this.billDate);
        this.priceStore.on('load',this.setPrevProduct.createDelegate(this,[rec,obj]), this);
        Wtf.getCmp("pricewindow").on('update',function(){this.loadPriceStore()},this);
    },

    setPrevProduct:function(rec,obj){
        obj.cancel=false;
        obj.ckeckProduct=false
        if(this.fireEvent("validateedit", obj) !== false && !obj.cancel){
            obj.record.set(obj.field, obj.value);
            delete obj.cancel;
            this.fireEvent("afteredit", obj);
        }
    },

    setCurrencyid:function(currencyid,rate,symbol,rec,store){
        this.symbol=symbol;
        this.currencyid=currencyid;
        this.rate=rate;
        for(var i=0;i<this.store.getCount();i++){
            this.store.getAt(i).set('currencysymbol',this.symbol)
            this.store.getAt(i).set('currencyrate',this.rate)           
        }
        this.getView().refresh();
    //     this.store.commitChanges();
 
     },    
    setCurrencyAmount:function(amount){
    if(this.isNote)
        return amount;
          return (amount*this.rate)
    },
    isAmountzero:function(store){
        var amount;
        var selModel=  this.getSelectionModel();
        var len=this.productComboStore.getCount();
        for(var i=0;i<len;i++){
            if(selModel.isSelected(i)){
                amount=store.getAt(i).data["discamount"];
                if(amount<=0)
                    return true;
            }
        }       
        return false;
    }
});    
