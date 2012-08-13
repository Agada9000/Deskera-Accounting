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
Wtf.form.ExtFnComboBox=function(config){

    Wtf.form.ExtFnComboBox.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.form.ExtFnComboBox,Wtf.form.FnComboBox,{

    initComponent:function(config){
        Wtf.form.ExtFnComboBox.superclass.initComponent.call(this, config);
        var extrafield='';
        var length=this.extraFields.length;
        for (var i=0;i<length;i++)
            extrafield+='<td width="'+100/(length+1)+'%"td>{'+this.extraFields[i]+'}</td>';
        this.tpl=new Wtf.XTemplate('<tpl for="."><div class="x-combo-list-item"><table width="100%"><tr><td width="'+100/(length+1)+'%">{[this.getDots(values.level)]}{'+this.displayField+'}</td>'+extrafield+'</tr></table></div></tpl>',{
            getDots:function(val){
                var str="";
                for(var i=0;i<val;i++)
                    str+="....";
                return str;
            }
        })
    }
}); 
