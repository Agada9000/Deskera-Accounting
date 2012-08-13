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
WtfGlobal = {
    getCookie: function(c_name){
        if (document.cookie.length > 0) {
            c_start = document.cookie.indexOf(c_name + "=");
            if (c_start != -1) {
                c_start = c_start + c_name.length + 1;
                c_end = document.cookie.indexOf(";", c_start);
                if (c_end == -1)
                    c_end = document.cookie.length;
                return unescape(document.cookie.substring(c_start, c_end));
            }
        }
        return "";
    },

    nameRenderer: function(value){
        var resultval = value.substr(0, 1);
        var patt1 = new RegExp("^[a-zA-Z]");
        if (patt1.test(resultval)) {
            return resultval.toUpperCase();
        }
        else
            return "Others";
    },

    sizeRenderer: function(value){
        var sizeinKB = value
        if (sizeinKB >= 1 && sizeinKB < 1024) {
            text = "Small";
        } else if (sizeinKB > 1024 && sizeinKB < 102400) {
            text = "Medium";
        } else if (sizeinKB > 102400 && sizeinKB < 1048576) {
            text = "Large";
        } else {
            text = "Gigantic";
        }
        return text;
    },

    dateFieldRenderer: function(value){
        var text = "";
        if (value) {
            var dt = new Date();
            if ((value.getMonth() == dt.getMonth()) && (value.getYear() == dt.getYear())) {
                if (dt.getDate() == value.getDate()) {
                    text = "Today";
                } else if (value.getDate() == (dt.getDate() - 1))
                    text = "Yesterday";
                else if (value.getDate() <= (dt.getDate() - 7) && value.getDate() > (dt.getDate() - 14))
                    text = "Last Week";
            } else if ((value.getMonth() == (dt.getMonth() - 1)) && (value.getYear() == dt.getYear()))
                text = "Last Month";
            else if ((value.getYear() == (dt.getYear() - 1)))
                text = "Last Year";
            else
                text = "Older";
        } else
            text = "None";
        return text;
    },

    permissionRenderer: function(value, rec){
        var text = value.toLowerCase();
        switch (text) {
            case "everyone":
                text = "Everyone on deskEra";
                break;
            case "connections":
                text = "All Connections";
                break;
            case "none":
                text = "Private";
                break;
            default:
                text = "Selected Connections";
                break;
        }
        return text;
    },

    replaceAll : function(txt, replace, with_this) {
        return txt.replace(new RegExp(replace, 'g'),with_this);
    },

	HTMLStripper: function(val){
        var str = Wtf.util.Format.stripTags(val);
        return str.replace(/"/g, '').trim();
    },

    ScriptStripper: function(str){
        str = Wtf.util.Format.stripScripts(str);
        if (str)
            return str.replace(/"/g, '');
        else
            return str;
    },

    URLDecode: function(str){
        str=str.replace(new RegExp('\\+','g'),' ');
        return unescape(str);
    },

    getDateFormat: function() {
        return Wtf.pref.DateFormat;
    },

    getSeperatorPos: function() {
        return Wtf.pref.seperatorpos;
    },

    getOnlyDateFormat: function() {
        var pos=WtfGlobal.getSeperatorPos();
        var fmt=WtfGlobal.getDateFormat();
        if(pos<=0)
            return "Y-m-d";
        return fmt.substring(0,pos);
    },

    getOnlyTimeFormat: function() {
        var pos=WtfGlobal.getSeperatorPos();
        var fmt=WtfGlobal.getDateFormat();
        if(pos>=fmt.length)
            return "H:i:s";
        return fmt.substring(pos);
    },

    dateRenderer: function(v) {
        if(!v) return v;
        return '<div class="datecls">'+v.format(WtfGlobal.getDateFormat())+'</div>';
    },

    onlyTimeRenderer: function(v) {
        if(!v) return v;
        return '<div class="datecls">'+v.format(WtfGlobal.getOnlyTimeFormat())+'</div>';
    },
    onlyMonthRenderer: function(v) {
        var m_names = new Array("January", "February", "March",
        "April", "May", "June", "July", "August", "September",
            "October", "November", "December");

        if(!v) return v;
        var date=new Date(v);
       return '<div class="leftdatecls">'+m_names[date.getMonth()]+"-"+date.getFullYear()+'</div>';
    },
    onlyDateRenderer: function(v) {
        if(!v) return v;
        return '<div class="datecls">'+v.format(WtfGlobal.getOnlyDateFormat())+'</div>';
    },
    onlyDateRightRenderer: function(v) {
        if(!v) return v;
        return '<div class="rightdatecls">'+v.format(WtfGlobal.getOnlyDateFormat())+'</div>';
    },
    onlyDateLeftRenderer: function(v) {
        if(!v) return v;
        return '<div class="leftdatecls">'+v.format(WtfGlobal.getOnlyDateFormat())+'</div>';
    },
    convertToGenericDate:function(value){
        if(!value) return value;
        return value.format("M d, Y h:i:s A");
    },

    getTimeZone: function() {
        return Wtf.pref.Timezone;
    },
    onlyDateDeletedRenderer: function(v,m,rec) {
        if(!v) return v;
        if(rec!=undefined&&rec.data.deleted)
            v='<del>'+v.format(WtfGlobal.getOnlyDateFormat())+'</del>';
        else
        	v = v.format(WtfGlobal.getOnlyDateFormat());
        v='<div class="datecls">'+v+'</div>';
         return v;
    },
    deletedRenderer: function(v,m,rec) {
        if(!v) return v;
        if(rec.data.deleted)
             v='<del>'+v+'</del>';
         return v;
    },
    getSelectComboRenderer:function(combo){
       return function(value) {
           var idx;
           var rec;
           var valStr="";
           if (value != undefined && value != "") {
               var valArray = value.split(",");
               for (var i=0;i < valArray.length;i++ ){
                   idx = combo.store.find(combo.valueField, valArray[i]);
                   if(idx != -1){
                       rec = combo.store.getAt(idx);
                       valStr+=rec.get(combo.displayField)+", ";
                   }
               }
               if(valStr != ""){
                   valStr=valStr.substring(0, valStr.length -2);
                   valStr="<div wtf:qtip=\""+valStr+"\">"+Wtf.util.Format.ellipsis(valStr,27)+"</div>";
                }
            }
            return valStr;
        }
    },
    currencyDeletedRenderer: function(value,isCheckCenterAlign,rec) {
        var isCenterAlign=(isCheckCenterAlign==undefined?false:isCheckCenterAlign[0]);
        var v=parseFloat(value);
        if(isNaN(v)) return value;
            v= WtfGlobal.conventInDecimal(v,WtfGlobal.getCurrencySymbol());
            if(rec.data.deleted)
                v='<del>'+v+'</del>';
             v=  '<div class="currency">'+v+'</div>';
            if(isCenterAlign)
                  v= '<div>'+v+'</div>';
         return v;
    },

     withoutRateCurrencyDeletedSymbol: function(value,m,rec) {
        var symbol=((rec==undefined||rec.data.currencysymbol==null||rec.data['currencysymbol']==undefined||rec.data['currencysymbol']=="")?WtfGlobal.getCurrencySymbol():rec.data['currencysymbol']);
        var v=parseFloat(value);
        if(isNaN(v)) return value;
        if(rec.data.deleted)
            v='<del>'+WtfGlobal.conventInDecimal(v,symbol)+'</del>';
        else
        	v=WtfGlobal.conventInDecimal(v,symbol);
          v=  '<div class="currency">'+v+'</div>';
         return v;
    },

     currencyRendererDeletedSymbol: function(value,m,rec) {
        var symbol=((rec==undefined||rec.data['currencysymbol']==null||rec.data['currencysymbol']==undefined||rec.data['currencysymbol']=="")?WtfGlobal.getCurrencySymbol():rec.data['currencysymbol']);
        var rate=((rec==undefined||rec.data['currencyrate']==undefined||rec.data['currencyrate']=="")?1:rec.data['currencyrate']);
        var oldcurrencyrate=((rec==undefined||rec.data['oldcurrencyrate']==undefined||rec.data['oldcurrencyrate']=="")?1:rec.data['oldcurrencyrate']);
        var v;
        if(rate!=0.0)
            v=(parseFloat(value)*parseFloat(rate))/parseFloat(oldcurrencyrate);
        else
            v=(parseFloat(value)/parseFloat(oldcurrencyrate));
        if(isNaN(v)) return value;
        if(rec.data.deleted)
            v='<del>'+WtfGlobal.conventInDecimal(v,symbol)+'</del>';
        else
        	v = WtfGlobal.conventInDecimal(v,symbol);
        v=  '<div class="currency">'+v+'</div>';
         return v;

    },
    onlyMonthDeletedRenderer: function(v,m,rec) {
        var m_names = new Array("January", "February", "March",
        "April", "May", "June", "July", "August", "September",
            "October", "November", "December");

        if(!v) return v;
        var date=new Date(v);
       v='<div class="leftdatecls">'+m_names[date.getMonth()]+"-"+date.getFullYear()+'</div>';
       if(rec.data.deleted)
             v='<del>'+v+'</del>';
         return v;
    },
     linkDeletedRenderer: function(v,m,rec) {
          if(rec.data.deleted)
             v="<span class='deletedlink'><del>"+v+"</del></span>";
         else
            v= "<a class='jumplink' href='#'>"+v+"</a>";
        
         return v;
    },

    renderDeletedEmailTo: function(value,p,record){
        value = "<div class='mailTo'><a href=mailto:"+value+">"+value+"</a></div>";
        if(record.data.deleted) {
             value='<del>'+value+'</del>';
        }
        return value;
    },

    renderDeletedContactToSkype: function(value,p,record){
        value = "<div class='mailTo'><a href=skype:"+value+"?call>"+value+"</a></div>";
        if(record.data.deleted) {
             value='<del>'+value+'</del>';
        }
        return value;
    },

    getCurrencyName: function() {
        return Wtf.pref.CurrencyName;
    },
    getCurrencyID: function() {
        return Wtf.pref.Currencyid;
    },

    getCurrencySymbol: function() {
        return Wtf.pref.CurrencySymbol;
    },

    getCurrencySymbolForForm: function() {
        return "<span class='currency-view'>"+WtfGlobal.getCurrencySymbol()+"</span>";
    },
    getFieldLabel:function(text){
        return "<span class='fieldlabel'>"+text+"</span>";
    },


    linkRenderer: function(value) {
        return "<a class='jumplink' href='#'>"+value+"</a>";
    },
     currencyLinkRenderer: function(text,value) {
        return text+"<a class='currencyjumplink' href='#'>"+value+"</a>";
    },
    emptyGridRenderer: function(value) {
        return "<div class='grid-link-text'>"+value+"</div>";
    },
   
    conventInDecimal: function(v,symbol) {
            v = (Math.round((v-0)*100))/100;
            v = (v == Math.floor(v)) ? v + ".00" : ((v*10 == Math.floor(v*10)) ? v + "0" : v);
            v = String(v);
            var ps = v.split('.');
            var whole = ps[0];
            var sub = ps[1] ? '.'+ ps[1] : '.00';
            var r = /(\d+)(\d{3})/;
            while (r.test(whole)) {
                whole = whole.replace(r, '$1' + ',' + '$2');
            }
            v = whole + sub;
            if(v.charAt(0) == '-') {
//                v= '-'+symbol + " " + v.substr(1);
                v= "(<label style='color:red'>"+symbol + " " + v.substr(1)+"</label>)";
            } else
                v=symbol + " " + v;            
            return v;
    },
    conventCurrencyDecimal: function(v,symbol) {
            v = (Math.round((v-0)*10000000))/10000000;
            v = (v == Math.floor(v)) ? v + ".00" : ((v*10 == Math.floor(v*10)) ? v + "0" : v);
            v = String(v);
            var ps = v.split('.');
            var whole = ps[0];
            var sub = ps[1] ? '.'+ ps[1] : '.00';
            var r = /(\d+)(\d{3})/;
            while (r.test(whole)) {
                whole = whole.replace(r, '$1' + ',' + '$2');
            }
            v = whole + sub;
            if(v.charAt(0) == '-')
                v= '-'+symbol + " " + v.substr(1);
            else
                v=symbol + " " + v;
            return v;
    },

     currencyRenderer: function(value,isCheckCenterAlign) {
        var isCenterAlign=(isCheckCenterAlign==undefined?false:isCheckCenterAlign[0]);
        var v=parseFloat(value);
        if(isNaN(v)) return value;
            v= WtfGlobal.conventInDecimal(v,WtfGlobal.getCurrencySymbol())
            if(isCenterAlign)
                 return '<div>'+v+'</div>';
        return '<div class="currency">'+v+'</div>';
    },

    withoutRateCurrencySymbol: function(value,m,rec) {
        var symbol=((rec==undefined||rec.data.currencysymbol==null||rec.data['currencysymbol']==undefined||rec.data['currencysymbol']=="")?WtfGlobal.getCurrencySymbol():rec.data['currencysymbol']);
        var v=parseFloat(value);
        if(isNaN(v)) return value;
          v= WtfGlobal.conventInDecimal(v,symbol)
        return '<div class="currency">'+v+'</div>';
    },

     currencyRendererSymbol: function(value,m,rec) {
        var symbol=((rec==undefined||rec.data['currencysymbol']==null||rec.data['currencysymbol']==undefined||rec.data['currencysymbol']=="")?WtfGlobal.getCurrencySymbol():rec.data['currencysymbol']);
        var rate=((rec==undefined||rec.data['currencyrate']==undefined||rec.data['currencyrate']=="")?1:rec.data['currencyrate']);
        var oldcurrencyrate=((rec==undefined||rec.data['oldcurrencyrate']==undefined||rec.data['oldcurrencyrate']=="")?1:rec.data['oldcurrencyrate']);
        var v;
        if(rate!=0.0)
            v=(parseFloat(value)*parseFloat(rate))/parseFloat(oldcurrencyrate);
        else
            v=(parseFloat(value)/parseFloat(oldcurrencyrate));
        if(isNaN(v)) return value;
            v= WtfGlobal.conventInDecimal(v,symbol)
        return '<div class="currency">'+v+'</div>';

    },
    currencyRendererWithoutSymbol: function(value,m,rec) {
        var rate=((rec==undefined||rec.data['currencyrate']==undefined||rec.data['currencyrate']=="")?1:rec.data['currencyrate']);
        var oldcurrencyrate=((rec==undefined||rec.data['oldcurrencyrate']==undefined||rec.data['oldcurrencyrate']=="")?1:rec.data['oldcurrencyrate']);
        var v;
        if(rate!=0.0)
            v=(parseFloat(value)*parseFloat(rate));
        else
            v=(parseFloat(value)/parseFloat(oldcurrencyrate));
        if(isNaN(v)) return value;          
        return v;
    }, 
    addCurrencySymbolOnly: function(value,symbol,isCheckCenterAlign) {
        symbol=((symbol==undefined||symbol==null||symbol=="")?WtfGlobal.getCurrencySymbol():symbol);
        symbol=((symbol.data!=undefined&&symbol.data['currencysymbol']!=null&&symbol.data['currencysymbol']!=undefined&&symbol.data['currencysymbol']!="")?symbol.data['currencysymbol']:symbol);
        var isCenterAlign=(isCheckCenterAlign==undefined?false:isCheckCenterAlign[0]);
        symbol=(symbol==undefined?WtfGlobal.getCurrencySymbol():symbol);
        var v=parseFloat(value);
        if(isNaN(v)) return value;
           v= WtfGlobal.conventInDecimal(v,symbol)
            if(isCenterAlign)
                 return '<div>'+v+'</div>';
        return '<div class="currency">'+v+'</div>';
    },

    numericRenderer:function(v){
        return '<div class="currency">'+v+'</div>';
    },
    currencySummaryRenderer: function(value) {
        return WtfGlobal.summaryRenderer(WtfGlobal.currencyRenderer(value));
    },
    currencySummaryRendererSymbol: function(value,p,r,symbol) {
        return WtfGlobal.summaryRenderer(WtfGlobal.addCurrencySymbolOnly(value,symbol));
    },
    summaryRenderer: function(value) {
        return '<div class="grid-summary-common">'+value+'</div>';
    },

    boolRenderer: function(tval,fval) {
        return function(val){ if(val) return tval; return fval};
    },

    validateEmail: function(value){
        return Wtf.ValidateMailPatt.test(value);
    },

    renderEmailTo: function(value,p,record){
        return "<div class='mailTo'><a href=mailto:"+value+">"+value+"</a></div>";
    },

    validateHTField:function(value){
      return Wtf.validateHeadTitle.test(value.trim());
    },

    renderContactToSkype: function(value,p,record){
        return "<div class='mailTo'><a href=skype:"+value+"?call>"+value+"</a></div>";
    },

    validateUserid: function(value){
        return Wtf.ValidateUserid.test(value);
    },

    validateUserName: function(value){
        return Wtf.ValidateUserName.test(value.trim());
    },

    getInstrMsg: function(msg){
        return "<span style='font-size:10px !important;color:gray !important;'>"+msg+"</span>"
    },

    EnableDisable: function(userpermcode, permcode){//alert(permcode)
        if(permcode==null){
            clog("Some Permission are undefined.\n"+userpermcode+"\n"+showCallStack());

        }
        if (userpermcode && permcode) {
            if ((userpermcode & permcode) == permcode)
                return false;
        }
        return true;
    },

//    getLocaleText:function(key, basename, def){
//    	var base=window[basename||"messages"];
//    	if(base){
//    			if(base[key])
//    				return base[key];
//    			else
//    				clog("Locale specific text not found for ["+key+"]");
//    	}else{
//    		clog("Locale specific base ("+basename+") not available");
//    	}
//    	return def||key;	
//    },
    
    getLocaleText:function(key, basename, def){
        var base=window[basename||"messages"];
        var params=[].concat(key.params||[]);
        key = key.key||key;
        if(base){
            if(base[key]){
                    params.splice(0, 0, base[key]);
                    return String.format.apply(this,params);
            }else
                    clog("Locale specific text not found for ["+key+"]");
        }else{
            clog("Locale specific base ("+basename+") not available");
        }
        return def||key;
    },
    
    loadScript: function(src, callback, scope){
        var scriptTag = document.createElement("script");
        scriptTag.type = "text/javascript";
        if(typeof callback == "function"){
        	scriptTag.onreadystatechange= function () {
        		      if (this.readyState == 'complete') 
        		    	  callback.call(scope || this || window);
        		   }
        	scriptTag.onload= callback.createDelegate(scope || this || window);        	
        }
        scriptTag.src = src;
        document.getElementsByTagName("head")[0].appendChild(scriptTag);
    },

    loadStyleSheet: function(ref){
        var styleTag = document.createElement("link");
        styleTag.setAttribute("rel", "stylesheet");
        styleTag.setAttribute("type", "text/css");
        styleTag.setAttribute("href", ref);
        document.getElementsByTagName("head")[0].appendChild(styleTag);
    },

    getJSONArray:function(grid, includeLast, idxArr){
        var indices="";
        if(idxArr)
            indices=":"+idxArr.join(":")+":";
        var store=grid.getStore();
        var arr=[];
        var fields=store.fields;
        var len=store.getCount()-1;
        if(includeLast)len++;
        for(var i=0;i<len;i++){
            if(idxArr&&indices.indexOf(":"+i+":")<0) continue;
            var rec=store.getAt(i);
            var recarr=[];
            for(var j=0;j<fields.length;j++){
                var value=rec.data[fields.get(j).name];
                switch(fields.get(j).type){
                    case "auto":if(value!=undefined){value=(value+"").trim();}value=encodeURI(value);value="\""+value+"\"";break;
                    case "date":value="'"+WtfGlobal.convertToGenericDate(value)+"'";break;
                }
                recarr.push(fields.get(j).name+":"+value);
            }
            recarr.push("modified:"+rec.dirty);
            arr.push("{"+recarr.join(",")+"}");
        }
        return "["+arr.join(',')+"]";
    },

    shortString:function(name){
        if(name.length > 20){
            return name.substr(0, 17) + '...';
        }
        return name;
    },
    scrollRec:function(grid,index) {
        var rowEl = grid.getView().getRow(index);
        var el=grid.getView().scroller;
        var a=Wtf.fly(rowEl).getOffsetsTo(el);
        el.scrollTo("top",a[1],true);
    },
    highLightAddRow:function(grid,duration,rec,color) {
        var row=grid.getStore().indexOf(rec);
        var rowEl = grid.getView().getRow(row);
        if(rowEl!=undefined) {
            var el=grid.getView().scroller;
            var a=Wtf.fly(rowEl).getOffsetsTo(el);
            el.scrollTo("top",a[1],true);
            Wtf.fly(rowEl).highlight(
            color,{
                attr: "background-color",
                duration: duration,
                endColor: "ffffff",
                easing: 'easeIn'
            });
        }
    },
    gridReloadDelay:function(){
        return 3000;
    },
    highLightRowColor:function(grid,recArr,addColor,strip,type,isrec){
        var color='FFFFFF';
        var duration = 2001;
        if(strip==1)
            color='FAFAFA';
        if(addColor){
        switch (type) {
            case 0:
                 color='FFFFCC';
                duration = 5;
                WtfGlobal.highLightAddRow(grid,duration,recArr,color);
                break;
            case 1:
                color='4E9258';
                break;
            case 3:
                color='F75D59';
                duration = 2.5;
                WtfGlobal.highLightAddRow(grid,duration,recArr,color);
                break;
            case 4:
                color='A3BAE9';
                duration = 2.5;
                WtfGlobal.highLightAddRow(grid,duration,recArr,color);
                break;
            default:
                color='F75D59'; //delete
                duration = 2000;
                break;
            }
        }
        WtfGlobal.onlyhighLightRow(grid,duration,recArr,color,isrec)
    },
    onlyhighLightRow:function(grid,duration,recArr,color,isrec) {
         var index=0;
         var rowEl;
         var store;
         var row;
         if(isrec){
             store= grid.getStore();
            row=store.indexOf(recArr);
            rowEl = grid.getView().getRow(row);
            Wtf.fly(rowEl).highlight(
            color,{
                attr: "background-color",
                duration: duration,
                endColor: "ffffff",
                easing: 'easeIn',
                stopFx:true,
                concurrent:true
            });

         }
        else{
             while(index+1<=recArr.length){
                store= grid.getStore();
                row=store.indexOf(recArr[index]);
                rowEl = grid.getView().getRow(row);
                Wtf.fly(rowEl).highlight(
                color,{
                    attr: "background-color",
                    duration: duration,
                    endColor: "ffffff",
                    easing: 'easeIn',
                    stopFx:true,
                    concurrent:true
                });
                index++;
            }
        }
    },
    loadpersonacc:function(isCustomer){
        if(isCustomer!=undefined ||isCustomer!=null)
            isCustomer?Wtf.customerAccStore.load():Wtf.vendorAccStore.load();
    },
    enableDisableBtnArr:function(btnArr,grid,singleSelectArr,multiSelectArr){
        var multi = !grid.getSelectionModel().hasSelection();
        var single = (grid.getSelectionModel().getCount()!=1);
        for(var i=0;i<multiSelectArr.length;i++){
            btnArr[multiSelectArr[i]].setDisabled(multi);
            WtfGlobal.setTip(btnArr[multiSelectArr[i]]);
        }
        for(i=0;i<singleSelectArr.length;i++){
            btnArr[singleSelectArr[i]].setDisabled(single);
            WtfGlobal.setTip(btnArr[singleSelectArr[i]]);
        }
    },

    setTip:function(btn, tipText){
            var tooltip=btn.tooltip;
            var disabled=btn.disabled;
            if(!tooltip&&btn.isAction){
                tooltip=btn.initialConfig.tooltip;
                disabled=btn.initialConfig.disabled;
            }
            if(!tooltip)
                return;

            if(!tooltip.buttonTitle)tooltip.buttonTitle=btn.getText();
            tooltip.text=(tipText?tipText:(disabled?tooltip.dtext:tooltip.etext));
            if(tooltip.text){
                if(btn.setTooltip)
                    btn.setTooltip(tooltip.text);
                else
                    btn.setText("<span wtf:qtip='"+tooltip.text+"'>"+tooltip.buttonTitle+"</span>")
            }
    },

    fetchAutoNumber:function(from, fn, scope){
        Wtf.Ajax.requestEx({
//            url:Wtf.req.account+'CompanyManager.jsp',
            url:"ACCCompanyPref/getNextAutoNumber.do",
            params:{
                mode:83,
                from:from
            }
        }, scope,function(resp){
            if(resp.success)
                fn.call(scope,resp)
            else{
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),resp.msg],resp.success*2+1);
            }
        });
    },

    formatNumber: function(v,format) {
            v = (Math.round((v-0)*100))/100;
            v = (v == Math.floor(v)) ? v + ".00" : ((v*10 == Math.floor(v*10)) ? v + "0" : v);
            v = String(v);
            var ps = v.split('.');
            var whole = ps[0];
            var sub = ps[1] ? '.'+ ps[1] : '.00';
            var r = /\d{1,3}(?=(\d{3})+(?!\d))/g;
            whole = whole.replace(r, '$&,');
            v = whole + sub;
            var temp="";
            if(v.charAt(0) == '-'){
                temp="-";
                v= v.substr(1);
            }
            var pat = /\{v\}/g;
            var val = " "+format;
            val = " "+val.replace(pat, v);
            pat = /\{c\}/g;
            val = " "+val.replace(pat, WtfGlobal.getCurrencySymbol());
            pat = /\{s\}/g;
            val = val.replace(pat, temp);

            return val;
    },

    showFormElement:function(obj){
        obj.container.up('div.x-form-item').dom.style.display='block';
    },
    hideFormElement:function(obj){
        obj.container.up('div.x-form-item').dom.style.display='none';
    },
    updateFormLabel:function(obj,newLabel){
        obj.container.up('div.x-form-item').child('label.x-form-item-label').update(newLabel);
    },
    addLabelHelp:function(HelpText){
        return "<span wtf:qtip=\""+HelpText+"\" class=\"formHelpButton\">&nbsp;&nbsp;&nbsp;&nbsp;</span>";
    },
    autoApplyHeaderQtip : function(grid){
        var m = grid.elMetrics;
        if (!grid.elMetrics) {
            m = Wtf.util.TextMetrics.createInstance(grid.container);
        }
        var cm = grid.getColumnModel();
        var vw = grid.getView();
        for(var i=0; i<cm.getColumnCount(); i++) {
            if(!cm.isHidden(i)) {
                var columnWidth = cm.getColumnWidth(i);
                var columnText = cm.getColumnHeader(i);
                var textwidth = m.getWidth(columnText);
                if(textwidth > columnWidth) {
//                    cm.setColumnTooltip(i, columnText);
                    var headerCell = vw.getHeaderCell(i).lastChild;
                    headerCell.innerHTML = "<span Wtf:qtip=\""+columnText+"\">"+headerCell.innerHTML+"<span>";
                }
            }
        }
    },
    
    getDates:function(start) {
        var d=new Date();
        var monthDateStr=d.format('M d');
        if(Wtf.account.companyAccountPref.fyfrom)
            monthDateStr=Wtf.account.companyAccountPref.fyfrom.format('M d');
        var fd=new Date(monthDateStr+', '+d.getFullYear()+' 12:00:00 AM');
        if(d<fd)
            fd=new Date(monthDateStr+', '+(d.getFullYear()-1)+' 12:00:00 AM');
        if(start)
            return fd;
        return fd.add(Date.YEAR, 1).add(Date.DAY, -1);
    }
};


/*  WtfHTMLEditor: Start    */
Wtf.newHTMLEditor = function(config){
    Wtf.apply(this, config);
    this.createLinkText = 'Please enter the URL for the link:';
    this.defaultLinkValue = 'http:/'+'/';
    this.smileyel = null;
    this.SmileyArray = [" ", ":)", ":(", ";)", ":D", ";;)", ">:D<", ":-/", ":x", ":>>", ":P", ":-*", "=((", ":-O", "X(", ":>", "B-)", ":-S", "#:-S", ">:)", ":((", ":))", ":|", "/:)", "=))", "O:-)", ":-B", "=;", ":-c", ":)]", "~X("];
    this.tpl = new Wtf.Template('<div id="{curid}smiley{count}" style="float:left; height:20px; width:20px; background: #ffffff;padding-left:4px;padding-top:4px;"  ><img id="{curid}smiley{count}" src="{url}" style="height:16px; width:16px"></img></div>');
    this.tbutton = new Wtf.Toolbar.Button({
        minWidth: 30,
        disabled:true,
        enableToggle: true,
        iconCls: 'smiley'
    });
    this.eventSetFlag=false;
    this.tbutton.on("click", this.handleSmiley, this);
    this.smileyWindow = new Wtf.Window({
        width: 185,
        height: 116,
        minWidth: 200,
        plain: true,
        cls: 'replyWind',
        shadow: false,
        buttonAlign: 'center',
        draggable: false,
        header: false,
        closable  : true,
        closeAction : 'hide',
        resizable: false
    });
    this.smileyWindow.on("deactivate", this.closeSmileyWindow, this);
    Wtf.newHTMLEditor.superclass.constructor.call(this, {});
    this.on("render", this.addSmiley, this);
    this.on("activate", this.enableSmiley, this);
    this.on("hide", this.hideSmiley, this);
}

Wtf.extend(Wtf.newHTMLEditor, Wtf.form.HtmlEditor, {
    enableSmiley:function(){
        this.tbutton.enable();
    },
    hideSmiley: function(){
        if(this.smileyWindow !== undefined && this.smileyWindow.el !== undefined)
            this.smileyWindow.hide();
    },
    addSmiley: function(editorObj){
        editorObj.getToolbar().addSeparator();
        editorObj.getToolbar().addButton(this.tbutton);

    },
    createLink : function(){
        var url = prompt(this.createLinkText, this.defaultLinkValue);
        if(url && url != 'http:/'+'/'){
            var tmpStr = url.substring(0,7);
            if(tmpStr!='http:/'+'/')
                url = 'http:/'+'/'+url;
            this.win.focus();
            var selTxt = this.doc.getSelection().trim();
            selTxt = selTxt =="" ? url : selTxt;
            if(this.SmileyArray.join().indexOf(selTxt)==-1) {
                this.insertAtCursor("<a href = '"+url+"' target='_blank'>"+selTxt+" </a>");
                this.deferFocus();
            } else {
                msgBoxShow(170,1);
            }
        }
    },
    //  FIXME: ravi: When certain smilies are used in a pattern, the resultant from this function does not conform to regex used to decode smilies in messenger.js.

    writeSmiley: function(e){
        var obj=e;
        this.insertAtCursor(this.SmileyArray[obj.target.id.substring(this.id.length + 6)]+" ");
        this.smileyWindow.hide();
        this.tbutton.toggle(false);
    },

    handleSmiley: function(buttonObj, e){
        if(this.tbutton.pressed) {
            this.smileyWindow.setPosition(e.getPageX(), e.getPageY());
            this.smileyWindow.show();
            if(!this.eventSetFlag){
                for (var i = 1; i < 29; i++) {
                    var divObj = {
                        url: '../../images/smiley' + i + '.gif',
                        count: i,
                        curid: this.id
                    };
                    this.tpl.append(this.smileyWindow.body, divObj);
                    this.smileyel = Wtf.get(this.id + "smiley" + i);
                    this.smileyel.on("click", this.writeSmiley, this);
                    this.eventSetFlag=true;
                }
            }
        } else {
            this.smileyWindow.hide();
            this.tbutton.toggle(false);
        }
    },

    closeSmileyWindow: function(smileyWindow){
        this.smileyWindow.hide();
        this.tbutton.toggle(false);
    }
});

// Call stack code
function showCallStack(){
var f=showCallStack,result="Call stack:\n";

while((f=f.caller)!==null){
var sFunctionName = f.toString().match(/^function (\w+)\(/)
sFunctionName = (sFunctionName) ? sFunctionName[1] : 'anonymous function';
result += sFunctionName;
result += getArguments(f.toString(), f.arguments);
result += "\n";

}
return result;
}


function getArguments(sFunction, a) {
var i = sFunction.indexOf(' ');
var ii = sFunction.indexOf('(');
var iii = sFunction.indexOf(')');
var aArgs = sFunction.substr(ii+1, iii-ii-1).split(',')
var sArgs = '';
for(var i=0; i<a.length; i++) {
var q = ('string' == typeof a[i]) ? '"' : '';
sArgs+=((i>0) ? ', ' : '')+(typeof a[i])+' '+aArgs[i]+':'+q+a[i]+q+'';
}
return '('+sArgs+')';
}


Wtf.taskDetail = Wtf.extend(Wtf.Component, {

	tplMarkup: ['<div id="fcue-360" class="fcue-outer" style="position: absolute; z-index:2000000; left: 188px; top: 12px;">'+
            '<div class="fcue-inner">'+
                '<div class="fcue-t"></div>'+
                '<div class="fcue-content">'+
                        '<a onclick="closeCue();" href="#" id="fcue-close"></a>'+
                    '<div class="ft ftnux"><p>'+
                        '</p><span id="titlehelp" style="font-weight:bold;">Welcome Help Dialog</span><p></p><span id="titledesc">sssdd</span>'+
                        '<div id="helpBttnContainerDiv"><p></p>'+
                        '<a class="cta-1 cta button1" id="nextID" onclick="goToNextCue();" href="javascript:;"><img src="../../images/next.gif" width="80" height="30"></a>'+
                        '<a class="cta-1 cta button1" style="display:none;" id="helptipsID" onclick="goToNextCue();" href="javascript:;"><img src="../../images/help-tip.jpg" width="80" height="30"></a>'+
                        '<a class="cta-1 cta button1" style="display:none;" id="closeID" onclick="closeCue();" href="javascript:;"><img src="../../images/close.jpg" width="80" height="30"></a>'+
                        '<a class="cta-1 cta button1" id="previousID" onclick="goToPrevCue();" href="javascript:;"><img src="../../images/previous.gif" width="80" height="30"></a>'+
                        '</div>'+
                    '</div>'+
                '</div>'+
            '</div>'+
            '<div class="fcue-b">'+
                '<div></div>'+
            '</div>'+
            '</div>',
        // left - top
  		'<div id="fcue-360" class="fcue-outer" style="position: absolute; z-index:2000000; left: 188px; top: 12px;">'+
            '<div class="fcue-inner">'+
                '<div class="fcue-t"></div>'+
                '<div class="fcue-content">'+
                  '<a onkeypress="" onclick="closeCue();" href="#" id="fcue-close"></a>'+
                    '<div class="ft ftnux"><p>'+
                        '</p></p><span id="titlehelp" style="font-weight:bold;">Welcome Help Dialog</span><p></p><span id="titledesc">sssdd</span>'+
                        '<div id="helpBttnContainerDiv"><p></p>'+
                        '<a class="cta-1 cta button1" id="nextID" onclick="goToNextCue();" href="javascript:;"><strong class="i"><img src="../../images/next.gif" width="80" height="30"></a>'+
                        '<a class="cta-1 cta button1"  style="display:none;" id="closeID" onclick="closeCue();" href="javascript:;"><img src="../../images/close.jpg" width="80" height="30"></a>'+
                        '<a class="cta-1 cta button1" id="previousID" onclick="goToPrevCue();" href="javascript:;"><img src="../../images/previous.gif" width="80" height="30"></a></div>'+
                    '</div>'+
                '</div>'+
            '</div>'+
            '<div class="fcue-b">'+
                '<div></div>'+
            '</div>'+
            '<div class="fcue-pnt fcue-pnt-lf-t">'+
            '</div>'+
        '</div>',
        // left - bottom
        '<div id="fcue-360" class="fcue-outer" style="position: absolute; z-index:2000000; left: 188px; top: 12px;">'+
            '<div class="fcue-inner">'+
                '<div class="fcue-t"></div>'+
                '<div class="fcue-content">'+
                 '<a onclick="closeCue();" href="#" id="fcue-close"></a>'+
                    '<div class="ft ftnux"><p>'+
                        '</p></p><span id="titlehelp" style="font-weight:bold;">Welcome Help Dialog</span><p></p><span id="titledesc">sssdd</span>'+
                        '<div id="helpBttnContainerDiv"><p></p>'+
                        '<a class="cta-1 cta button1" id="nextID" onclick="goToNextCue();" href="javascript:;"><strong class="i"><img src="../../images/next.gif" width="80" height="30"></a>'+
                        '<a class="cta-1 cta button1"  style="display:none;" id="closeID" onclick="closeCue();" href="javascript:;"><img src="../../images/close.jpg" width="80" height="30"></a>'+
                        '<a class="cta-1 cta button1" id="previousID" onclick="goToPrevCue();" href="javascript:;"><img src="../../images/previous.gif" width="80" height="30"></a></div>'+
                    '</div>'+
                '</div>'+
            '</div>'+
            '<div class="fcue-b">'+
                '<div></div>'+
            '</div>'+
            '<div class="fcue-pnt fcue-pnt-lf-b">'+
            '</div>'+
        '</div>',

        // 3 : bottom - left
        '<div id="fcue-360" class="fcue-outer" style="position: absolute; z-index:2000000; left: 188px; top: 12px;">'+
            '<div class="fcue-inner">'+
                '<div class="fcue-t"></div>'+
                '<div class="fcue-content">'+
                  '<a onclick="closeCue();" href="#" id="fcue-close"></a>'+
                    '<div class="ft ftnux"><p>'+
                        '</p></p><span id="titlehelp" style="font-weight:bold;">Welcome Help Dialog</span><p></p><span id="titledesc">sssdd</span>'+
                        '<div id="helpBttnContainerDiv"><p></p>'+
                        '<a class="cta-1 cta button1" id="nextID" onclick="goToNextCue();" href="javascript:;"><strong class="i"><img src="../../images/next.gif" width="80" height="30"></a>'+
                        '<a class="cta-1 cta button1" style="display:none;" id="closeID" onclick="closeCue();" href="javascript:;"><img src="../../images/close.jpg" width="80" height="30"></a>'+
                        '<a class="cta-1 cta button1" id="previousID" onclick="goToPrevCue();" href="javascript:;"><img src="../../images/previous.gif" width="80" height="30"></a></div>'+
                    '</div>'+
                '</div>'+
            '</div>'+
            '<div class="fcue-b">'+
                '<div></div>'+
            '</div>'+
            '<div id="pointerdiv" class="fcue-pnt fcue-pnt-bm-l">'+
            '</div>'+
        '</div>',
        // bottom - right
        '<div id="fcue-360" class="fcue-outer" style="position: absolute; z-index:2000000; left: 188px; top: 12px;">'+
            '<div class="fcue-inner">'+
                '<div class="fcue-t"></div>'+
                '<div class="fcue-content">'+
                  '<a onclick="closeCue();" href="#" id="fcue-close"></a>'+
                    '<div class="ft ftnux"><p>'+
                        '</p></p><span id="titlehelp" style="font-weight:bold;">Welcome Help Dialog</span><p></p><span id="titledesc">sssdd</span>'+
                        '<div id="helpBttnContainerDiv"><p></p>'+
                        '<a class="cta-1 cta button1" id="nextID" onclick="goToNextCue();" href="javascript:;"><strong class="i"><img src="../../images/next.gif" width="80" height="30"></a>'+
                        '<a class="cta-1 cta button1" style="display:none;" id="closeID" onclick="closeCue();" href="javascript:;"><img src="../../images/close.jpg" width="80" height="30"></a>'+
                        '<a class="cta-1 cta button1" id="previousID" onclick="goToPrevCue();" href="javascript:;"><img src="../../images/previous.gif" width="80" height="30"></a></div>'+
                    '</div>'+
                '</div>'+
            '</div>'+
            '<div class="fcue-b">'+
                '<div></div>'+
            '</div>'+
            '<div id="pointerdiv" class="fcue-pnt fcue-pnt-bm-r">'+
            '</div>'+
        '</div>',
        // top - left
        '<div id="fcue-360" class="fcue-outer" style="position: absolute; z-index:2000000; left: 188px; top: 12px;">'+
            '<div class="fcue-inner">'+
                '<div class="fcue-t"></div>'+
                '<div class="fcue-content">'+
                  '<a onclick="closeCue();" href="#" id="fcue-close"></a>'+
                    '<div class="ft ftnux"><p>'+
                        '</p></p><span id="titlehelp" style="font-weight:bold;">Welcome Help Dialog</span><p></p><span id="titledesc">sssdd</span>'+
                        '<div id="helpBttnContainerDiv"><p></p>'+
                        '<a class="cta-1 cta button1" id="nextID" onclick="goToNextCue();" href="javascript:;"><img src="../../images/next.gif" width="80" height="30"></a>'+
                        '<a class="cta-1 cta button1" style="display:none;" id="closeID" onclick="closeCue();" href="javascript:;"><img src="../../images/close.jpg" width="80" height="30"></a>'+
                        '<a class="cta-1 cta button1" id="previousID" onclick="goToPrevCue();" href="javascript:;"><img src="../../images/previous.gif" width="80" height="30"></a></div>'+                        
                        ''+
                    '</div>'+
                '</div>'+
            '</div>'+
            '<div class="fcue-b">'+
                '<div></div>'+
            '</div>'+
            '<div class="fcue-pnt fcue-pnt-t-l">'+
            '</div>'+
        '</div>',
        // top - right
        '<div id="fcue-360" class="fcue-outer" style="position: absolute; z-index:2000000; left: 188px; top: 12px;">'+
            '<div class="fcue-inner">'+
                '<div class="fcue-t"></div>'+
                '<div class="fcue-content">'+
                  '<a onclick="closeCue();" href="#" id="fcue-close"></a>'+
                    '<div class="ft ftnux"><p>'+
                        '</p></p><span id="titlehelp" style="font-weight:bold;">Welcome Help Dialog</span><p></p><span id="titledesc">sssdd</span>'+
                        '<div id="helpBttnContainerDiv"><p></p>'+
                        '<a class="cta-1 cta button1" id="nextID" onclick="goToNextCue();" href="javascript:;"><img src="../../images/next.gif" width="80" height="30"></a>'+
                        '<a class="cta-1 cta button1"  style="display:none;" id="closeID" onclick="closeCue();" href="javascript:;"><img src="../../images/close.jpg" width="80" height="30"></a>'+
                        '<a class="cta-1 cta button1"  id="previousID" onclick="goToPrevCue();" href="javascript:;"><img src="../../images/previous.gif" width="80" height="30"></a></div>'+
                        ''+
                    '</div>'+
                '</div>'+
            '</div>'+
            '<div class="fcue-b">'+
                '<div></div>'+
            '</div>'+
            '<div class="fcue-pnt fcue-pnt-t-r">'+
            '</div>'+
        '</div>'],

	startingMarkup: 'Please select a module to see details',
    id : 'helpdialog',
    helpIndex : 0,
	initComponent: function(config) {
		Wtf.taskDetail.superclass.initComponent.call(this, config);
	},

	welcomeHelp: function(flag) {
        if(document.getElementById('fcue-360-mask'))document.getElementById('fcue-360-mask').style.display="block";
        var data = _helpContent[this.helpIndex];
        var compid = data.compid;
        if(flag==undefined)
            flag=1;
        if(compid=="") {
            var len=_helpContent.length;
            this.tpl = new Wtf.Template(this.tplMarkup[0]);
            var ht = this.tpl.append(document.body,{});
            document.getElementById('titlehelp').innerHTML = data.title;
            document.getElementById('titledesc').innerHTML = data.desc;
            Wtf.get('fcue-360').setXY([500,250]);
            document.getElementById('fcue-360').style.visibility ="visible";
            if(this.helpIndex == len-2){
                document.getElementById("nextID").style.display ="none";
                document.getElementById("previousID").style.display="none";
                document.getElementById("helptipsID").style.display="inline";
                document.getElementById("closeID").style.display="inline";
            } else if(this.helpIndex==0) {
                document.getElementById("nextID").style.visibility ="visible";
                document.getElementById("previousID").style.visibility ="hidden";
            } else if(this.helpIndex == len-1){
                document.getElementById("nextID").style.display ="none";
                document.getElementById("previousID").style.display="none";
                document.getElementById("helptipsID").style.display="none";
                document.getElementById("closeID").style.display="inline";
            } else {
                document.getElementById("nextID").style.visibility ="visible";
                document.getElementById("previousID").style.visibility ="visible";
            }
        } else
            this.nextPrevious(flag);
	},

    updateToNextDetail: function() {
        this.helpIndex = this.helpIndex+1;
        this.welcomeHelp(1);
    },

    updateToPrevDetail: function() {
        this.helpIndex = this.helpIndex-1;
        this.welcomeHelp(2);
	},

    blankDetail : function() {
        this.bltpl.overwrite(this.body,"");
	},

    getTemplateIndex : function(comppos) {
        var index = 0;
        var xPos = comppos[0];
        var yPos = comppos[1];
        var flag = 0;
        var myWidth = 0, myHeight = 0;
        if( typeof( window.innerWidth ) == 'number' ) {
            //Non-IE
            myWidth = window.innerWidth;
            myHeight = window.innerHeight;
        } else if( document.documentElement && ( document.documentElement.clientWidth || document.documentElement.clientHeight ) ) {
            //IE 6+ in 'standards compliant mode'
            myWidth = document.documentElement.clientWidth;
            myHeight = document.documentElement.clientHeight;
        } else if( document.body && ( document.body.clientWidth || document.body.clientHeight ) ) {
            //IE 4 compatible
            myWidth = document.body.clientWidth;
            myHeight = document.body.clientHeight;
        }

        if(xPos<20) { // extreme left
            flag = 1;
        } else if(xPos>(myWidth-370)) { // extreme right
            flag = 2;
        }
        if(yPos<100) {
            if(flag == 1) {
                index = 1; // left top corner
            } else if(flag == 2){
                index = 6; // top right corner
            } else {
                index = 5;
            }
        } else if(yPos>(myHeight-150)) {
            if(flag == 1) {// bottom left corner
                index = 3;
            } else if(flag == 2) {// bottom right corner
                index = 4;
            } else
                index = 3; // bottom left corner
        } else if(yPos<(myHeight/2)) {
            if(flag==1)
                index = 1;
            else if(flag==2)
                index = 6;
            else index = 5;
        } else if(yPos>(myHeight/2)) {
            if(flag==1)
                index = 2;
            else
                index = 3;
        }
        return index;
    },

    nextPrevious:function(flag){
        var len=_helpContent.length;
        var data = _helpContent[this.helpIndex];

        if(Wtf.get(data['compid'])==null) {
            if(flag==1)
                this.updateToNextDetail();
            else
                this.updateToPrevDetail();
            return;
        } else if(Wtf.get(data['compid']).getXY()[0]==0 && Wtf.get(data['compid']).getXY()[1]==0){
            if(flag==1) {
                if(this.helpIndex==0)
                    this.firstCollapse=1;
                this.updateToNextDetail();
            } else
                this.updateToPrevDetail();
            return;
        } else {
            if(flag==1 && this.helpIndex == len-1) { // For the last help in the page.
                this.firstCollapse=0;
            }
        }
        var comppos = Wtf.get(data['compid']).getXY();
        if(data['modeid'] == "1") {
            var dash = Wtf.getCmp('tabdashboard').body;
            dash.dom.scrollTop = comppos[1] - 70;
        } else if(mainPanel.activeTab.EditorGrid!=null && data['compid'].indexOf('addnew')!=-1) {  // crm modules
            var store = mainPanel.activeTab.EditorStore;
            var storeLen = store.getCount();
            var rowEl = mainPanel.activeTab.EditorGrid.getView().getRow(storeLen-1);
            var gBody = mainPanel.activeTab.EditorGrid.getView().scroller;
            var a = (Wtf.fly(rowEl).getOffsetsTo(gBody)[1]) + gBody.dom.scrollTop;
            gBody.dom.scrollTop = a;
        }
        comppos = Wtf.get(data['compid']).getXY();
        var index = this.getTemplateIndex(comppos);

        this.tpl = new Wtf.Template(this.tplMarkup[index]);
        var ht = this.tpl.append(document.body,{});
        document.getElementById('titlehelp').innerHTML = data.title;
        document.getElementById('titledesc').innerHTML = data.desc;
        var helpDiv = Wtf.get('fcue-360');
        var helpSize = helpDiv.getSize();
        var pos = comppos;

        switch(index) {
            case 1: //left-top
                pos[1] -= 35;
                pos[0] += 60;
                break;
            case 2: //left-bottom
                break;
            case 3: //bottom-left
                pos[1] -= (helpSize.height);
                pos[0] -= 30
                if(pos[0]<0){
                    pos[0] = 12;
                    document.getElementById('pointerdiv').style.left = '-15px';
                }
                var topPos = helpSize.height-20;
                if(Wtf.isSafari) {
                    if(window.innerHeight-pos[1]<=180) {
                        topPos = helpSize.height-9;
                    }
                }
                document.getElementById('pointerdiv').style.top = (topPos+'px');// 22px - bottom div height
                break;
            case 4: //bottom-right
                pos[1] -= (helpSize.height);
                pos[0] -= (helpSize.width-22-32); // 22px - left div width and 32px - pointer position at inner side
                document.getElementById('pointerdiv').style.top = ((helpSize.height-20)+'px');// 22px - bottom div height
                break;
            case 5: //top - left
                pos[1] += 38;
                break;
            case 6: //top - right
                pos[1] += 35;
                pos[0] -= 310;
                break;
        }
        helpDiv.setXY(pos);
        document.getElementById('fcue-360').style.visibility ="visible";

        if(this.helpIndex == len-1) {
            if(this.firstCollapse == 0 && this.helpIndex != 0) {
                document.getElementById("nextID").style.display ="none";
                document.getElementById("previousID").style.cssFloat="left";
                document.getElementById("closeID").style.display="inline";
            } else {
                document.getElementById("nextID").style.display ="none";
                document.getElementById("previousID").style.display="none";
                document.getElementById("closeID").style.display="inline";
            }
        } else if(this.helpIndex == 0 || this.firstCollapse == 1 || this.prevCollapse == this.helpIndex) {
            document.getElementById("nextID").style.visibility ="visible";
            document.getElementById("previousID").style.visibility ="hidden";
            this.prevCollapse = this.helpIndex;
            this.firstCollapse=0;
        } else{
            document.getElementById("nextID").style.visibility ="visible";
            document.getElementById("previousID").style.visibility ="visible";
        }
    }

});
function closeCue () {
    Wtf.get('fcue-360').remove();
    if(document.getElementById('fcue-360-mask'))document.getElementById('fcue-360-mask').style.display="none";
}

function goToNextCue() {
    closeCue();
    Wtf.getCmp('helpdialog').updateToNextDetail();
}

function goToPrevCue() {
    closeCue();
    Wtf.getCmp('helpdialog').updateToPrevDetail();
}
