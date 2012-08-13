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
Wtf.account.PriceReport=function(config){
    this.Record = new Wtf.data.Record.create([{
        name: 'priceid'
    },{
        name: 'carryin',
        type:'boolean'
    },{
        name: 'price'
    },{
        name: 'applydate',
        type:'date'
    }]);

    this.Store = new Wtf.data.Store({
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        },this.Record),
//        url: Wtf.req.account+'CompanyManager.jsp',
        url: "ACCProduct/getProductPrice.do",
        baseParams:{
            mode:12,
            productid:config.productId
        }
    });
    //this.Store.load();
    WtfComMsgBox(29,4,true);

    this.gridcm= new Wtf.grid.ColumnModel([{
        header: WtfGlobal.getLocaleText("acc.rem.74"),  //"Carryin",
        dataIndex: 'carryin',
        renderer:this.CarryInRenderer
    },{
        header :WtfGlobal.getLocaleText("acc.rem.75"),  //'Price',
        dataIndex: 'price',
        align:'right',
        renderer:WtfGlobal.currencyRenderer
    },{
        header: WtfGlobal.getLocaleText("acc.rem.73"),  //"Date Modified",
        dataIndex: 'applydate',
        align:'center',
        renderer: WtfGlobal.onlyDateRenderer
    }]);
    this.grid= new Wtf.grid.GridPanel({
        stripeRows :true,
        layout:'fit',
        store: this.Store,
        cm: this.gridcm,
        border : false,
        loadMask : true,
        viewConfig: {
            forceFit:true,
            emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
        }
    });
    this.Store.on('load',this.hideMsg,this)
    Wtf.account.PriceReport.superclass.constructor.call(this,config);
}

Wtf.extend( Wtf.account.PriceReport,Wtf.Panel,{
     hideMsg: function(){
         if(this.Store.getCount()==0){
            this.grid.getView().emptyText=WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
            this.grid.getView().refresh();
         }
         Wtf.MessageBox.hide();
    },
    onRender:function(config){
        this.add(this.grid);
        Wtf.account.PriceReport.superclass.onRender.call(this,config);
        
    },

    CarryInRenderer:function(carryIn){
        var temptxt="";
        if(carryIn==false)
            temptxt=WtfGlobal.getLocaleText("acc.productList.gridSalesPrice");  //"Sale Price";
        else
            temptxt=WtfGlobal.getLocaleText("acc.productList.gridPurchasePrice");  //"Purchase Price";
        return temptxt;
    }
});
