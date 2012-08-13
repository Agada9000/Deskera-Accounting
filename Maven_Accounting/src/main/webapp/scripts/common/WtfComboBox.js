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
Wtf.form.FnComboBox=function(config){
    this.initial="REC";
    Wtf.form.FnComboBox.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.form.FnComboBox,Wtf.form.ComboBox,{
    addNewDisplay:"Add New...",
    addNoneRecord: false,       // Flag to add a "none selection" record.
    noneRecordText: "None",     // None record's display text
    noneRecordValue: "",        // None recods's value (id)
    disableToolTip: "This record has been deleted.", // Style for disable record
    disableStyle: "color:gray; text-decoration:line-through;", // Style for disable record
    mode: 'local',
    triggerAction: 'all',
    typeAhead: true,
    initComponent:function(config){
        Wtf.form.FnComboBox.superclass.initComponent.call(this, config);
        this.addNewID=this.initial+this.store.id;
        this.addLastEntry(this.store);
        this.store.on('load',this.addLastEntry,this);
        this.on('beforeselect',this.callFunction, this);

        if(this.hirarchical){
            if(this.disableOnField){
                this.tpl=new Wtf.XTemplate('<tpl for="."><div class="x-combo-list-item" style="{[values.'+this.disableOnField+' == true ? "'+this.disableStyle+'" : "" ]}" Wtf:qtip="{[values.'+this.disableOnField+' == true ? "'+this.disableToolTip+'" : values.'+this.displayField+']}">{[this.getDots(values.level)]}{'+this.displayField+'}</div></tpl>',{
                    getDots:function(val){
                        var str="";
                        for(var i=0;i<val;i++)
                            str+="....";
                        return str;
                    }
                })
            } else {
                this.tpl=new Wtf.XTemplate('<tpl for="."><div class="x-combo-list-item" Wtf:qtip="{'+this.displayField+'}">{[this.getDots(values.level)]}{'+this.displayField+'}</div></tpl>',{
                    getDots:function(val){
                        var str="";
                        for(var i=0;i<val;i++)
                            str+="....";
                        return str;
                    }
                })
            }
        } else {
            if(this.disableOnField){
                this.tpl=new Wtf.XTemplate('<tpl for="."><div class="x-combo-list-item" style="{[values.'+this.disableOnField+' == true ? "'+this.disableStyle+'" : "" ]}" Wtf:qtip="{[values.'+this.disableOnField+' == true ? "'+this.disableToolTip+'" : values.'+this.displayField+']}">{'+this.displayField+'}</div></tpl>');
            } else {
                this.tpl=new Wtf.XTemplate('<tpl for="."><div class="x-combo-list-item" Wtf:qtip="{'+this.displayField+'}">{'+this.displayField+'}</div></tpl>');
            }
        }
    },

    onRender : function(ct, position){
        Wtf.form.FnComboBox.superclass.onRender.call(this, ct, position);
        if(this.addNewFn==undefined)return;
        this.anButton = this.wrap.createChild({tag: "img", src: Wtf.BLANK_IMAGE_URL, cls:"combo-addnew"});
        this.initAddNewButton();
        if(!this.width){
            this.wrap.setWidth(this.el.getWidth()+this.anButton.getWidth());
        }
    },

    initAddNewButton:function(){
        this.anButton.on("click", function(){
                if(this.disabled)return;
                if(this.isExpanded())
                    this.collapse();
                this.addNewFn();
            }, this, {preventDefault:true});
    },

    onResize : function(w, h){
        Wtf.form.FnComboBox.superclass.onResize.call(this, w, h);
        if(this.addNewFn==undefined)return;
        if(typeof w == 'number'){
            this.el.setWidth(this.adjustWidth('input', w -this.trigger.getWidth() - this.anButton.getWidth()));
        }
        this.wrap.setWidth(this.el.getWidth()+this.trigger.getWidth()+this.anButton.getWidth());
    },


    addLastEntry:function(s){
        var comboRec, rec;
        if(this.addNoneRecord){     // For no Selections, Add a record as "None"
            var nrecid=s.find(this.displayField,this.noneRecordText);
            if(nrecid==-1){
                comboRec=Wtf.data.Record.create(s.fields);
                rec=new comboRec({});
                s.insert(0,rec);
                rec.beginEdit();
                rec.set(this.valueField, this.noneRecordValue);
                rec.set(this.displayField, this.noneRecordText);
                rec.endEdit();
            }
        }
        if(this.addNewFn==undefined)return;
        var recid=s.find(this.valueField,this.addNewID);
        if(recid==-1){
            comboRec=Wtf.data.Record.create(s.fields);
            rec=new comboRec({});
            s.insert(0,rec);
            rec.beginEdit();
            rec.set(this.valueField, this.addNewID);
            rec.set(this.displayField, this.addNewDisplay);
            rec.endEdit();
        }
    },

    callFunction:function(c,r){
        if(r.data[this.valueField]==this.addNewID){
            this.collapse();
            this.addNewFn();
            return false;
        }
        if(this.disableOnField && r.data[this.disableOnField]==true){ //Don't select Disabled record.
            return false;
        }
    }
});
