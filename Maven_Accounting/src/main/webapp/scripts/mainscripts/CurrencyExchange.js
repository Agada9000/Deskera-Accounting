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
function copyDefaultRates(id){
    Wtf.getCmp(id).getStore().removeAll();
    Wtf.getCmp(id).getColumnModel().setRenderer(1, function(val){
        return "<span class='defaultcurrency'>"+val+ "</span>"
        });
    Wtf.getCmp(id).getStore().proxy.conn.url = "ACCCurrency/getDefaultCurrencyExchange.do";
//    Wtf.getCmp(id).getStore().proxy.conn.url = Wtf.req.account+'CompanyManager.jsp';
    Wtf.getCmp(id).getStore().load({
        params:{
            mode:204
        }
        });   
}

Wtf.account.CurrencyExchangeWindow = function(config){
    this.currencyhistory=config.currencyhistory||false;
    this.uPermType=Wtf.UPerm.currencyexchange;
    this.permType=Wtf.Perm.currencyexchange;
    var btnArr=[];
      if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.edit)) {
        btnArr.push(this.save=new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.currency.sav"),  //'Save and Close',
            scope: this,
            hidden:this.currencyhistory,
            handler: this.saveData.createDelegate(this)
        }))
      }
      btnArr.push(this.cancel=new Wtf.Toolbar.Button({
            text: this.currencyhistory?WtfGlobal.getLocaleText("acc.common.backBtn"):WtfGlobal.getLocaleText("acc.common.cancelBtn"),
            scope: this,
            handler:this.closeWin.createDelegate(this)
        }))
    Wtf.apply(this,{
        title:WtfGlobal.getLocaleText("acc.currency.curTab"),  //"Currency Exchange Table",
        buttons: btnArr
    },config);
    Wtf.account.CurrencyExchangeWindow.superclass.constructor.call(this, config);
    this.addEvents({
        'update':true
    });
}
Wtf.extend( Wtf.account.CurrencyExchangeWindow, Wtf.Window, {
    defaultCurreny:false,
    onRender: function(config){
        Wtf.account.CurrencyExchangeWindow.superclass.onRender.call(this, config);
        this.createStore();
        this.createGrid();

        this.add({
            region: 'north',
            height:75,
            border: false,
            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
            html:getTopHtml(WtfGlobal.getLocaleText("acc.currency.title1"),WtfGlobal.getLocaleText("acc.currency.title"),"../../images/accounting_image/currency-exchange.jpg",true)
        },this.headerCalTemp=new Wtf.Panel({
            region: 'center',
            border: false,
            baseCls:'bckgroundcolor',
            layout: 'fit',
            html: "<a class='tbar-link-text' href='#' onClick='javascript: copyDefaultRates(\""+this.grid.getId()+"\")'wtf:qtip=''>"+WtfGlobal.getLocaleText("acc.currency.down")+"</a>",
            bodyStyle: 'border-bottom:1px solid #bfbfbf;padding:10px'
        }),{
            region: 'south',
            border: false,
            height:(this.currencyhistory?260:150),
            baseCls:'bckgroundcolor',
            layout: 'fit',
            items:this.grid
        });
    if(!this.currencyhistory)
        this.grid.on('cellclick',this.onCellClick, this);
    this.grid.on('validateedit',this.checkZeroValue, this);
    },
    createStore:function(){
        this.gridRec = new Wtf.data.Record.create([{
            name: 'id'
        },{
            name: 'applydate',
            type:'date'
        },{
            name: 'fromcurrency'
        },{
            name: 'tocurrency'
        },{
            name: 'exchangerate',
            type:'float'
        },{
            name: 'tocurrencyid'
        },{
            name: 'fromcurrencyid'
        },{
            name: 'companyid'
        }]);
        this.store = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:"count"
            },this.gridRec),
            url:Wtf.req.account+'CompanyManager.jsp'
        });
        
    },
    filterStore:function(store){
         this.store.filterBy(function(rec){
            if(rec.data.tocurrencyid==rec.data.fromcurrencyid)
                return false
            else
                return true
        },this)
    },

    applyTemplate:function(store){       
        this.filterStore();
        var index=store.getCount();
        if(index>0) {
            this.headerTplSummary.overwrite(this.headerCalTemp.body,{
                foreigncurrency:store.getAt(0).data['tocurrency'],
                exchangerate:store.getAt(0).data['exchangerate'],
                basecurrency:store.getAt(0).data['fromcurrency']
                });
        }else{
            this.headerTplSummary.overwrite(this.headerCalTemp.body,{
                foreigncurrency:"foreign currency",
                exchangerate:"x",
                basecurrency:WtfGlobal.getCurrencyName()
            });
        }
    },
    checkZeroValue:function(obj){
        if(obj.field=="exchangerate"){
            if(obj.value==0)
                obj.cancel=true;
        }
    },
    createGrid:function(){       
        this.gridcm= new Wtf.grid.ColumnModel([new Wtf.grid.RowNumberer(),{
            header:WtfGlobal.getLocaleText("acc.currency.cur"),  //"Currency",
            dataIndex:'tocurrency',
            hidden:this.currencyhistory,
            renderer:this.currencylink.createDelegate(this),//function(){WtfGlobal.currencyLinkRenderer(val,),
            autoWidth : true
     
        },{
            header:WtfGlobal.getLocaleText("acc.currency.exRate"),  //"Exchange Rate",
            dataIndex:'exchangerate',
            renderer:this.setRateRenderer.createDelegate(this),
            editor:this.exchangeRate=new Wtf.form.NumberField({
                allowBlank: false,
                decimalPrecision:7,
                allowNegative: false,
                minValue:0
            })
        },{
            header: this.currencyhistory?WtfGlobal.getLocaleText("acc.currency.app"):WtfGlobal.getLocaleText("acc.currency.lastapp"),  //"Applied Date":"Last Applied Date",
            dataIndex: 'applydate',
            renderer:WtfGlobal.onlyDateRenderer,
            minValue:new Date().clearTime(true),
            editor:this.currencyhistory?"":new Wtf.form.DateField({
                name:'applydate',
//                maxValue : (Wtf.account.companyAccountPref.fyfrom).clearTime(),    // Disable Dates ahead of the Financial year date 
                format:WtfGlobal.getOnlyDateFormat()
            })
        }
        ]);
        if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.edit))
        this.grid = new Wtf.grid.EditorGridPanel({
            cls:'vline-on',
            layout:'fit',
            autoScroll:true,
            height:200,
            id:(this.currencyhistory?'currencyhistory':'defaultCurrencygrid'),
            store: this.store,
            cm: this.gridcm,
            border : false,
            loadMask : true,
            viewConfig: {
                forceFit:true,
                emptyText:WtfGlobal.getLocaleText("acc.common.norec")
            }
        });
        else
         this.grid = new Wtf.grid.GridPanel({
            cls:'vline-on',
            layout:'fit',
            autoScroll:true,
            height:200,
            id:(this.currencyhistory?'currencyhistory':'defaultCurrencygrid'),
            store: this.store,
            cm: this.gridcm,
            border : false,
            loadMask : true,
            viewConfig: {
                forceFit:true,
                emptyText:WtfGlobal.getLocaleText("acc.common.norec")
            }
        });
         var downloadstr="";
        if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.edit))
        downloadstr="<a class='tbar-link-text' href='#' onClick='javascript: copyDefaultRates(\""+this.grid.getId()+"\")'wtf:qtip=''>"+WtfGlobal.getLocaleText("acc.currency.down")+"</a>"
        this.headerTplSummary=new Wtf.XTemplate(
            "<div><b>"+WtfGlobal.getLocaleText("acc.currency.homCur")+"</b> {basecurrency} </div>",
            "<div><b>"+WtfGlobal.getLocaleText("acc.currency.exrate")+"</b> "+WtfGlobal.getLocaleText("acc.currency.msg2")+"</div>",
            "<div><b>"+WtfGlobal.getLocaleText("acc.currency.example")+"</b> 1 {basecurrency} "+WtfGlobal.getLocaleText("acc.currency.hom")+" = {exchangerate} {foreigncurrency} "+WtfGlobal.getLocaleText("acc.currency.for")+" </div>",
            "<br>",
            downloadstr
        );
        this.loadStore()                
    },

    loadStore:function(){
       this.store.proxy.conn.url = "ACCCurrency/getCurrencyExchangeList.do";
       if(this.currencyhistory){
       this.store.load({
            params:{
                mode:203,
                currencyid:this.currencyid
                }
            });
        }
        else{
        this.store.proxy.conn.url = "ACCCurrency/getCurrencyExchange.do";
             this.store.load({
                params:{
                    mode:201,
                    transactiondate:WtfGlobal.convertToGenericDate(new Date())
                }
            });
        this.store.on('load',this.applyTemplate,this)
        this.gridcm.setRenderer(1, this.currencylink.createDelegate(this));
        }
             
    }, 
    currencylink:function(val){
        return WtfGlobal.currencyLinkRenderer(val,WtfGlobal.getLocaleText("acc.currency.his"));
    },
    onCellClick:function(g,i,j,e){
        e.stopEvent();
        var el=e.getTarget("a");
        if(el==null)return;
        var header=g.getColumnModel().getDataIndex(j);
        var rec=this.store.getAt(i);
        if(header=="fromcurrency"||header=="tocurrency")
            callCurrencyExchangeDetails("list"+rec.data["id"],rec.data["id"], rec.data["fromcurrency"]+' To '+rec.data["tocurrency"],true);
        Wtf.getCmp("list"+rec.data["id"]).on('update',function(){
            this.store.reload()
            },this);
    },
    closeWin:function(){
        this.fireEvent('cancel',this)
        this.close();
    },
    getUpdatedDetails:function(){
        var arr=[];
        this.store.clearFilter();
        for(var i=0;i<this.store.getCount();i++){
            var rec=this.store.getAt(i)
            if((rec.dirty||rec.data.companyid=="")){
                rec.set('applydate', (new Date(rec.data.applydate).clearTime()));
                arr.push(i);
            }
        }        

        return WtfGlobal.getJSONArray(this.grid,true,arr);
    }, 

    setRateRenderer:function(val){
       return  WtfGlobal.conventCurrencyDecimal(val,"")
    },

    saveData:function(){
        var rec=[];
        rec.mode=202;
        rec.data=this.getUpdatedDetails();
    
        if(rec.data=="[]")
        {
//            this.store.filterBy(function(rec){
//                if(rec.data.tocurrencyid==rec.data.fromcurrencyid)
//                    return false
//                else
//                    return true
//                },this)
            this.close();
            this.filterStore();
            return;
        }
        Wtf.Ajax.requestEx({
//            url:Wtf.req.account+'CompanyManager.jsp',
            url:"ACCCurrency/saveCurrencyExchange.do",
            params: rec
        },this,this.genSuccessResponse,this.genFailureResponse);
    },
  
    genSuccessResponse:function(response){
        if(response.dateexist){
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.currency.update"),WtfGlobal.getLocaleText("acc.currency.msg1"),function(btn){
                if(btn!="yes") {this.filterStore(); return; }
                var rec=[];
                rec.mode=202;
                rec.changerate=true;
                rec.data=this.getUpdatedDetails();
                if(rec.data=="[]"){
                    return;
                    this.filterStore();
                }
                Wtf.Ajax.requestEx({
                    //            url:Wtf.req.account+'CompanyManager.jsp',
                    url:"ACCCurrency/saveCurrencyExchange.do",
                    params: rec
                },this,this.genUpdateSuccessResponse,this.genFailureResponse);


            },this);
        }else{
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.info"),response.msg],response.success*2+1);
            if(response.success) this.fireEvent('update');
            this.loadStore();
            Wtf.currencyStore.load();
            this.close();
        }
    },
    genUpdateSuccessResponse:function(response){
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.info"),response.msg],response.success*2+1);
        if(response.success) this.fireEvent('update');
       this.loadStore();
        Wtf.currencyStore.load();
        this.close();
    },
    genFailureResponse:function(response){
        var msg=WtfGlobal.getLocaleText("acc.common.msg1");
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    }
});
