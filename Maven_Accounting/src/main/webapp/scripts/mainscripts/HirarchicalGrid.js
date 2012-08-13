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
Wtf.forumpPageSize = function(config){
    Wtf.apply(this, config)
    this.totalSize = null;
}
Wtf.extend(Wtf.forumpPageSize, Wtf.common.pPageSize, {
    changePageSize: function(value){
        var topicCount = 0;
        var subCount = 0;
        var pt = this.pagingToolbar;

        //this.combo.collapse();
        value = parseInt(value) || parseInt(this.combo.getValue());
        value = (value > 0) ? value : 1;

        if (value < pt.pageSize) {
            if(this.ftree!=null){
                this.ftree.getSelectionModel().selections.clear();
            }
            var store = pt.store;
            store.suspendEvents();
            for (var j = 0; j < store.getCount(); j++) {

//                if (store.getAt(j).data['ID'].match('topic')) {
                if (store.getAt(j).data['parentid'] == "" || store.getAt(j).data['parentid'] == undefined) {
                    topicCount++;
                    if ((topicCount) == (value + 1))
                        break;
                }
                subCount++;
            }
            if(topicCount!=this.totalSize)
                topicCount--;
            pt.pageSize = value;
            var ap = Math.round(pt.cursor / subCount) + 1;
            var cursor = (ap - 1) * subCount;
            store.suspendEvents();
            for (var i = 0, len = cursor - pt.cursor; i < len; i++) {
                store.remove(store.getAt(0));
            }
            while (store.getCount() > subCount) {
                store.remove(store.getAt(store.getCount() - 1));
            }
            store.resumeEvents();
            store.fireEvent('datachanged', store);
            pt.cursor = cursor;
            var d = pt.getPageData();
            pt.afterTextEl.el.innerHTML = String.format(pt.afterPageText, d.pages);
            if(store.data.length <= 0)
                ap = 1;
            pt.field.dom.value = ap;
            pt.first.setDisabled(ap == 1);
            pt.prev.setDisabled(ap == 1);
            pt.next.setDisabled(ap == d.pages);
            pt.last.setDisabled(ap == d.pages);
            pt.cursor = (ap - 1) * value;
            pt.updateInfo();
            if(store.data.length > 0)
                pt.displayEl.update("Displaying records " + parseInt(pt.cursor + 1) + " - " + parseInt(pt.cursor + parseInt(topicCount)) + " of " + this.totalSize);
            else
                pt.displayEl.update("No records to display");
        }
        else {
            this.pagingToolbar.pageSize = value;
            this.pagingToolbar.doLoad(Math.floor(this.pagingToolbar.cursor / this.pagingToolbar.pageSize) * this.pagingToolbar.pageSize);
        }
        this.updateStore();
        this.combo.collapse();
    }

});

/**************Component for Hirarchical GridPanel****************/
Wtf.grid.HirarchicalGridPanel=function(config){
    this.expandercss="x-grid3-row-expanderacc";
    this.expandedcss="x-grid3-row-expandedacc";
    this.collapsedcss="x-grid3-row-collapsedacc";
    Wtf.grid.HirarchicalGridPanel.superclass.constructor.call(this,config);
}

Wtf.extend( Wtf.grid.HirarchicalGridPanel,Wtf.grid.GridPanel,{
    stripeRows :true,
    initComponent:function(config){
        Wtf.grid.HirarchicalGridPanel.superclass.initComponent.call(this,config);
        this.getView().getRowClass=this.getRowClass.createDelegate(this);
    },

    onRender:function(config){
        this.prevRenderer=this.getColumnModel().getRenderer(this.hirarchyColNumber);
        this.getColumnModel().setRenderer(this.hirarchyColNumber,this.formatAccountName.createDelegate(this));
        this.on('rowclick',this.toggleRow,this);
        Wtf.grid.HirarchicalGridPanel.superclass.onRender.call(this,config);
    },
    
  	getRowClass: function(record){
        return this.expandedcss;
	},

    formatAccountName:function(val,m,rec,i,j,store){
        var fmtVal=(this.prevRenderer?this.prevRenderer(val,m,rec,i,j,store):val);
        if(val){
            if(rec.data['leaf']==true)
                fmtVal="<div style='margin-left:"+(rec.data['level']*20)+"px;padding-left:20px'>"+fmtVal+"</div>";
            else
                fmtVal= "<div class='"+this.expandercss+"' style='margin-left:"
				+(rec.data['level']*20)+"px;width:20px'><div style='margin-left:20px'>"+fmtVal+"</div></div>";
        }
        return fmtVal;
    },

    toggleRow:function(g,row,e){
        if(e.getTarget("."+this.expandercss)==null)return;
        if(typeof row == 'number'){
			row = this.view.getRow(row);
		}
		this[Wtf.fly(row).hasClass(this.collapsedcss) ? 'expandRow' : 'collapseRow'](row);
	},

	expandRow : function(row){
		Wtf.fly(row).replaceClass(this.collapsedcss, this.expandedcss);
		this.toggleDisplay(row.rowIndex,'');
	},

	collapseRow : function(row){
		Wtf.fly(row).replaceClass(this.expandedcss, this.collapsedcss);
		this.toggleDisplay(row.rowIndex,'none');
	},

	toggleDisplay: function(i,clname){
		var s=this.getStore();
        var plevel=s.getAt(i).data['level'];
        var ignore=false;
        var level=plevel;
        var j=i;
		while(j<s.getCount()-1){
            j++;
            if(s.getAt(j).data['level']<=plevel) break;
            if(ignore==true&&level==s.getAt(j).data['level']){
                ignore=false;
            }
            if(ignore==false)
				this.view.getRow(j).style.display=clname;
			if(ignore==false&&Wtf.fly(this.view.getRow(j)).hasClass(this.collapsedcss)){
                ignore=true;
                level=s.getAt(j).data['level'];
            }
		}
	}
});
