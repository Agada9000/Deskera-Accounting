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

Wtf.account.ExpenseInvoiceGrid=function(config){
    this.isCustomer=config.isCustomer;
    this.isCustBill=config.isCustBill;
    this.id=config.id;
    this.fromPO=false;
    this.editTransaction=config.editTransaction;
    this.readOnly=config.readOnly;
    this.isOrder=config.isOrder;
    this.noteTemp=config.noteTemp;
    this.fromOrder=config.fromOrder;
    if(config.isNote!=undefined)
        this.isNote=config.isNote;
    else
        this.isNote=false;
    this.isCN=config.isCN;
    this.createStore();
    this.createComboEditor();
    this.createColumnModel();
    Wtf.account.ExpenseInvoiceGrid.superclass.constructor.call(this,config);
    this.addEvents({
         'datachanged':true
    });
}
Wtf.extend(Wtf.account.ExpenseInvoiceGrid,Wtf.grid.EditorGridPanel,{
    clicksToEdit:1,
    stripeRows :true,
    layout:'fit',
    autoScroll:true,
    viewConfig:{forceFit:true},
    forceFit:true,
    loadMask:true,
    onRender:function(config){
         Wtf.account.ExpenseInvoiceGrid.superclass.onRender.call(this,config);
         this.on('render',this.addBlankRow,this);
         this.on('validateedit',this.checkRow,this);
         this.on('afteredit',this.updateRow,this);
         this.on('rowclick',this.handleRowClick,this);

     },
     createStore:function(){
        this.storeRec = Wtf.data.Record.create([
            {name:'rowid'},
            {name:'accountid'},
            {name:'billid'},
            {name:'billno'},
            {name:'rate'},
            {name:'discamount'},
            {name:'discount'},
            {name:'prdiscount'},
            {name:'prtaxid'},
            {name:'prtaxname'},
            {name:'prtaxpercent'},
            {name:'taxamount'},
            {name:'calamount'},
            {name:'discountamount'},
            {name: 'currencysymbol'},
            {name:'taxpercent'},
            {name: 'accountid' },
            {name: 'accountname'},
            {name:'totalamount'},
            {name:'amount'},
            {name:'transectionno'},
            {name:'orignalamount'},
            {name:'typeid'},
            {name:'isNewRecord'}
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
               this.accRec = Wtf.data.Record.create([
            {name:'accountname',mapping:'accname'},
            {name:'accountid',mapping:'accid'},
            {name:'level',type:'int'}
        ]);
        this.accountStore = new Wtf.data.Store({
            url:"ACCAccount/getAccounts.do",
//            url: Wtf.req.account+'CompanyManager.jsp',
            baseParams:{
                mode:2,
                deleted:false,
                nondeleted:true
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.accRec)
        });
        this.accountStore.load();
    },
    createComboEditor:function(){
        this.typeStore = new Wtf.data.SimpleStore({
            fields: [{name:'typeid',type:'int'}, 'name'],
            data :[[0,'Normal'],[1,'Defective']]
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
        this.editprice=new Wtf.form.NumberField({
            maxLength:10
        });
        this.Discount=new Wtf.form.NumberField({
            allowBlank: false,
            allowNegative: false,
            defaultValue:0,
            maxValue:100
        });
        this.cmbAccount=new Wtf.form.FnComboBox({
                    hiddenName:'accountid',
                    store:this.accountStore,
                    valueField:'accountid',
                    displayField:'accountname',
                    forceSelection:true,
                    hirarchical:true,
                    addNewFn:this.openCOAWindow.createDelegate(this)
                })
    }, 
    addTax:function(){
         var p= callTax("taxwin");
         Wtf.getCmp("taxwin").on('update', function(){this.taxStore.reload();}, this);
    },
    createColumnModel:function(){
        this.summary = new Wtf.ux.grid.GridSummary();
        this.rowno=(this.isNote)?new Wtf.grid.CheckboxSelectionModel():new Wtf.grid.RowNumberer();
        var columnArr =[];

        columnArr.push(this.rowno,{ 
             header:WtfGlobal.getLocaleText("acc.invoice.gridaccount"),  //"Account",
             width:200,
                dataIndex:this.readOnly?'accountname':'accountid',
                editor: this.readOnly?"":this.cmbAccount,
                renderer:this.readOnly?"":Wtf.comboBoxRenderer(this.cmbAccount)
        },{
            header:this.isCN?"Invoice No.":"Vendor Invoice No.",
            width:150,
            dataIndex:this.noteTemp?'transectionno':'billno',
            hidden:!this.isNote
        },{
            header: WtfGlobal.getLocaleText("acc.invoice.gridAmount"),  //"Amount",
            dataIndex:"rate",
            align:'right',
            width:200,
            renderer:WtfGlobal.withoutRateCurrencySymbol,
            editor:(this.isNote||this.readOnly)?"":this.editprice
        },{
            header: WtfGlobal.getLocaleText("acc.invoice.gridDiscount"),  //"Discount %",
            dataIndex:"prdiscount",
            align:'right',
            hidden:this.isOrder,
            width:200,
            renderer:function(v){return'<div class="currency">'+v+'%</div>';},
            editor:this.readOnly||this.isNote?"":this.Discount
         },{
            header:WtfGlobal.getLocaleText("acc.invoice.gridDiscountAmt"),  //"Discounted Amount",
            dataIndex:'discountamount',
            align:'right',
            width:200,
            hidden:this.isOrder,
            renderer:this.calDiscountAmount.createDelegate(this)
        },{
             header: WtfGlobal.getLocaleText("acc.invoice.Tax"),  //"Tax",
             dataIndex:"prtaxid",
             id:this.id+"prtaxid",
             //align:'right',
             width:150,
             hidden:!(this.editTransaction||this.readOnly),
             renderer:Wtf.comboBoxRenderer(this.transTax),
             editor:this.readOnly||this.isNote?"":this.transTax
        },{
             header:WtfGlobal.getLocaleText("acc.invoice.gridTaxAmount"),  // "Tax Amount",
             dataIndex:"taxamount",
             id:this.id+"taxamount",
             //align:'right',
             width:150,
             hidden:!(this.editTransaction||this.readOnly),
             renderer:this.setTaxAmount.createDelegate(this)
        },{
            header:WtfGlobal.getLocaleText("acc.invoice.gridLineAmt"),  //"Line Amount",
            align:'right',
            dataIndex:"calamount",
            width:200,
            hidden:this.isNote,
            renderer:this.calDiscAmount.createDelegate(this)
        },{
            header:WtfGlobal.getLocaleText("acc.invoice.gridOriginalAmt"),  //"Original Amount ",
            dataIndex:"orignalamount",
            align:'right',
            width:150,
            hidden:!this.isNote,
            renderer:(this.isNote||this.readOnly?WtfGlobal.withoutRateCurrencySymbol:WtfGlobal.currencyRendererSymbol)
        },{
            header:this.isNote?WtfGlobal.getLocaleText("acc.invoice.gridCurAmt"):WtfGlobal.getLocaleText("acc.invoice.gridAmount"),
            dataIndex:"amount",
            align:'right',
            hidden:!this.isNote,
            width:200,
            renderer:(this.isNote||this.readOnly?WtfGlobal.withoutRateCurrencySymbol:this.calAmount.createDelegate(this))
    },{
            header:WtfGlobal.getLocaleText("acc.invoice.Tax"),  //"Tax",
            dataIndex:"taxpercent",
            align:'right',
            hidden:!this.isNote,
            width:200,
            renderer:function(v){return'<div class="currency">'+v+'%</div>';}
        },{
             header:WtfGlobal.getLocaleText("acc.invoice.Tax"),  //"Tax",
             dataIndex:"prtaxpercent",
             align:'right',
             hidden:!this.isNote,
             width:200,
             renderer:function(v){return'<div class="currency">'+v+'%</div>';}
        },{
            header:(this.readOnly)?WtfGlobal.getLocaleText("acc.invoice.gridAmount"):"<b>"+WtfGlobal.getLocaleText("acc.invoice.gridEnterAmt")+"</b>",
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
            header:WtfGlobal.getLocaleText("acc.invoice.gridNoteType"),  //"Note Type",
            width:200,
            dataIndex:'typeid',
            hidden:(!this.isNote ||this.noteTemp),
            renderer:Wtf.comboBoxRenderer(this.typeEditor),
            editor:this.readOnly?"":this.typeEditor
        });
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.coa, Wtf.Perm.coa.create))
            this.cmbAccount.addNewFn=this.openCOAWindow.createDelegate(this)
        if(!this.isNote && !this.readOnly) {
            columnArr.push({
                header:WtfGlobal.getLocaleText("acc.invoice.gridAction"),  //"Action",
                align:'center',
                width:30,
                renderer: this.deleteRenderer.createDelegate(this)
            });
        }
        this.cm=new Wtf.grid.ColumnModel(columnArr);
    },
     checkRow:function(obj){
         if(obj!=null){
             var rec=obj.record;
             if(obj.field=="accountid"){
                var index=this.accountStore.find('accountid',obj.value)
                if(index>0){
                    rec=this.accountStore.getAt(index);
                    if(this.store.find("accountid",obj.value)>=0){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.rem.76")+" "+rec.data['accountname']], 2);
                        obj.cancel=true;
                    }
                }
             }
         }
     },
        openCOAWindow:function(){
        this.stopEditing(); 
        callCOAWindow(false, null, "coaWin");
        Wtf.getCmp("coaWin").on("update",function(){this.accountStore.reload()},this);
    },
    deleteRenderer:function(v,m,rec){
        return "<div class='"+getButtonIconCls(Wtf.etype.deletegridrow)+"'></div>";
    },
    handleRowClick:function(grid,rowindex,e){
        if(e.getTarget(".delete-gridrow")){
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.tax.msg4"), function(btn){
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

    calAmount:function(v,m,rec){        
        var val=rec.data.rate-(rec.data.rate*rec.data.prdiscount/100);
        rec.set("amount",val);
        if(this.isNote||this.readOnly)
             return WtfGlobal.withoutRateCurrencySymbol(val,m,rec);
        return WtfGlobal.currencyRendererSymbol(val,m,rec);
    },

    addBlankRow:function(){
        var newrec = new this.storeRec({
            accountid:"",
            rate:0,
            discountamount:0,
            totalamount:0,
            amount:0,
            prtaxname:"",
            prtaxid:"",
            taxamount:0,
            taxpercent:0,
            prdiscount:0,
            calamount:0,
            typeid:0,
            currencysymbol:this.symbol,
            isNewRecord:"1"
        });
        this.store.add(newrec);
    },
  addBlank:function(){
       this.setGridDiscValues();
        this.addBlankRow();
    },
      setGridDiscValues:function(){
            this.store.each(function(rec){
                   if(!this.editTransaction||this.fromPO)
                        rec.set('prdiscount',0)
                },this);
    },
    updateRow:function(obj){
        if(obj!=null){
             var rec=obj.record;
             if(obj.field=="prdiscount"){
                 rec=obj.record;
                 if(obj.value >100){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.rem.118")], 2);
                        rec.set("prdiscount",0);
                  }
                if(obj.value=="")
                     rec.set("prdiscount",0);

             }
             else if(obj.field=="prtaxid"){
                rec=obj.record;
                   // alert(obj.field)
                var discount=rec.data.rate*rec.data.prdiscount/100
                var val=rec.data.rate-discount;
                var taxpercent=0;
               var index=this.taxStore.find('prtaxid',rec.data.prtaxid);
                if(index>=0){
                    var taxrec=this.taxStore.getAt(index);
                    // alert(taxrec.data.toSource())
                    taxpercent=taxrec.data.percent;
                }
                var taxamount= (val*taxpercent/100);
                rec.set("taxamount",taxamount);
            }

        }
        this.fireEvent('datachanged',this);
        if(this.store.getCount()>0&&(this.store.getAt(this.store.getCount()-1).data['accountid'].length<=0||this.store.getAt(this.store.getCount()-1).data['rate']==0)){
            return;}
        if(!this.isNote)
            this.addBlankRow();
    },

    calDiscountAmount:function(v,m,rec){
        rec.set("discountamount",(rec.data.rate*rec.data.prdiscount/100));
        var val=(rec.data.rate*rec.data.prdiscount/100);
        return WtfGlobal.withoutRateCurrencySymbol(val,m,rec);
    },

    calDiscAmount:function(v,m,rec){
        var val=rec.data.rate-(rec.data.rate*rec.data.prdiscount/100);
        var taxamount= this.calTaxAmount(rec);
        val+=taxamount;
        rec.set("calamount",val);
        return WtfGlobal.withoutRateCurrencySymbol(val,m,rec);
    },
     calTaxAmount:function(rec){
        var discount=rec.data.rate*rec.data.prdiscount/100
        var val=rec.data.rate-discount;
        var taxpercent=0;
            var index=this.taxStore.find('prtaxid',rec.data.prtaxid);
            if(index>=0){
               var taxrec=this.taxStore.getAt(index);
              // alert(taxrec.data.toSource())
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

    loadPOGridStore:function(rec){
        this.store.load({params:{bills:rec.data.billid,mode:(this.isCustBill?53:43),closeflag:true}});
    },
    calSubtotal:function(){
        var subtotal=0;
        for(var i=0;i<this.store.getCount();i++)
            subtotal+=this.store.getAt(i).data.calamount;
        return subtotal;
    },
    setCurrencyid:function(cuerencyid,rate,symbol,record){
        this.symbol=symbol;
        this.store.each(function(rec){
            rec.set('currencysymbol',this.symbol)
        },this)
     },
    getProductDetails:function(){
          return WtfGlobal.getJSONArray(this);
    },

    getCMProductDetails:function(){
        var arr=[];
        var selModel=  this.getSelectionModel();
        var len=this.store.getCount();
        for(var i=0;i<len;i++){
            if(selModel.isSelected(i))
                arr.push(i);
            }
        return WtfGlobal.getJSONArray(this,true,arr);
    },

    isAmountzero:function(store){
        var amount;
        var selModel=  this.getSelectionModel();
        var len=this.store.getCount();
        for(var i=0;i<len;i++){
            if(selModel.isSelected(i)){
                amount=store.getAt(i).data.discamount;
                if(amount<=0)
                    return true;
            }
        }
        return false;
    }
});

