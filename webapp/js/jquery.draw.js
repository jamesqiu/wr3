(function($){var version='1.1.0';$.fn.settings={color:"#000000",stroke:1,alpha:1};$.fn.fillRect=function(x,y,w,h,opts)
{var elm=AssignOpts($(this),opts);_mkDiv(elm,x,y,w,h);return $(this);};$.fn.drawRect=function(x,y,w,h,opts)
{var elm=AssignOpts($(this),opts);elm.drawRect(elm,x,y,w,h);return $(this);};$.fn.drawBezier=function(x,y,autoClose,opts)
{var elm=$(this);var xr=[],yr=[];for(var i=1;i<=100;i++){var p=getBezier(i*.01,x,y);xr[xr.length]=Math.round(p.x);yr[yr.length]=Math.round(p.y);}
if(autoClose){elm.drawPolygon(xr,yr,opts);}else{elm.drawPolyline(xr,yr,opts);}
return $(this);};$.fn.fillBezier=function(x,y,opts)
{var elm=$(this);var xr=[],yr=[];for(var i=1;i<=100;i++){var p=getBezier(i*.01,x,y);xr[xr.length]=Math.round(p.x);yr[yr.length]=Math.round(p.y);}
elm.fillPolygon(xr,yr,opts);return $(this);};$.fn.drawPolyline=function(x,y,opts)
{var elm=AssignOpts($(this),opts);for(var i=x.length-1;i;)
{--i;elm.drawLine(elm,x[i],y[i],x[i+1],y[i+1]);}
return $(this);};$.fn.drawPolygon=function(x,y,opts)
{var elm=AssignOpts($(this),opts);elm.drawPolyline(x,y,opts);elm.drawLine(elm,x[x.length-1],y[x.length-1],x[0],y[0]);return $(this);};$.fn.drawEllipse=function(x,y,w,h,opts)
{var elm=AssignOpts($(this),opts);elm._mkOv(elm,x,y,w,h);return $(this);};$.fn.fillEllipse=function(left,top,w,h,opts)
{var elm=AssignOpts($(this),opts);var a=w>>1,b=h>>1,wod=w&1,hod=h&1,cx=left+a,cy=top+b,x=0,y=b,oy=b,aa2=(a*a)<<1,aa4=aa2<<1,bb2=(b*b)<<1,bb4=bb2<<1,st=(aa2>>1)*(1-(b<<1))+bb2,tt=(bb2>>1)-aa2*((b<<1)-1),xl,dw,dh;if(w)while(y>0)
{if(st<0)
{st+=bb2*((x<<1)+3);tt+=bb4*(++x);}
else if(tt<0)
{st+=bb2*((x<<1)+3)-aa4*(y-1);xl=cx-x;dw=(x<<1)+wod;tt+=bb4*(++x)-aa2*(((y--)<<1)-3);dh=oy-y;_mkDiv(elm,xl,cy-oy,dw,dh);_mkDiv(elm,xl,cy+y+hod,dw,dh);oy=y;}
else
{tt-=aa2*((y<<1)-3);st-=aa4*(--y);}}
_mkDiv(elm,cx-a,cy-oy,w,(oy<<1)+hod);return $(this);};$.fn.fillArc=function(iL,iT,iW,iH,fAngA,fAngZ,opts)
{var elm=AssignOpts($(this),opts);var a=iW>>1,b=iH>>1,iOdds=(iW&1)|((iH&1)<<16),cx=iL+a,cy=iT+b,x=0,y=b,ox=x,oy=y,aa2=(a*a)<<1,aa4=aa2<<1,bb2=(b*b)<<1,bb4=bb2<<1,st=(aa2>>1)*(1-(b<<1))+bb2,tt=(bb2>>1)-aa2*((b<<1)-1),xEndA,yEndA,xEndZ,yEndZ,iSects=(1<<(Math.floor((fAngA%=360.0)/180.0)<<3))|(2<<(Math.floor((fAngZ%=360.0)/180.0)<<3))|((fAngA>=fAngZ)<<16),aBndA=new Array(b+1),aBndZ=new Array(b+1);fAngA*=Math.PI/180.0;fAngZ*=Math.PI/180.0;xEndA=cx+Math.round(a*Math.cos(fAngA));yEndA=cy+Math.round(-b*Math.sin(fAngA));_mkLinVirt(aBndA,cx,cy,xEndA,yEndA);xEndZ=cx+Math.round(a*Math.cos(fAngZ));yEndZ=cy+Math.round(-b*Math.sin(fAngZ));_mkLinVirt(aBndZ,cx,cy,xEndZ,yEndZ);while(y>0)
{if(st<0)
{st+=bb2*((x<<1)+3);tt+=bb4*(++x);}
else if(tt<0)
{st+=bb2*((x<<1)+3)-aa4*(y-1);ox=x;tt+=bb4*(++x)-aa2*(((y--)<<1)-3);_mkArcDiv(elm,ox,y,oy,cx,cy,iOdds,aBndA,aBndZ,iSects);oy=y;}
else
{tt-=aa2*((y<<1)-3);st-=aa4*(--y);if(y&&(aBndA[y]!=aBndA[y-1]||aBndZ[y]!=aBndZ[y-1]))
{_mkArcDiv(elm,x,y,oy,cx,cy,iOdds,aBndA,aBndZ,iSects);ox=x;oy=y;}}}
_mkArcDiv(elm,x,0,oy,cx,cy,iOdds,aBndA,aBndZ,iSects);if(iOdds>>16)
{if(iSects>>16)
{var xl=(yEndA<=cy||yEndZ>cy)?(cx-x):cx;_mkDiv(elm,xl,cy,x+cx-xl+(iOdds&0xffff),1);}
else if((iSects&0x01)&&yEndZ>cy)
_mkDiv(elm,cx-x,cy,x,1);}
return $(this);};$.fn.fillPolygon=function(array_x,array_y,opts)
{var elm=AssignOpts($(this),opts);var i;var y;var miny,maxy;var x1,y1;var x2,y2;var ind1,ind2;var ints;var n=array_x.length;if(!n)return;miny=array_y[0];maxy=array_y[0];for(i=1;i<n;i++)
{if(array_y[i]<miny){miny=array_y[i];}
if(array_y[i]>maxy){maxy=array_y[i];}}
for(y=miny;y<=maxy;y++)
{var polyInts=new Array();ints=0;for(i=0;i<n;i++)
{if(!i)
{ind1=n-1;ind2=0;}
else
{ind1=i-1;ind2=i;}
y1=array_y[ind1];y2=array_y[ind2];if(y1<y2)
{x1=array_x[ind1];x2=array_x[ind2];}
else if(y1>y2)
{y2=array_y[ind1];y1=array_y[ind2];x2=array_x[ind1];x1=array_x[ind2];}
else{continue;}
if((y>=y1)&&(y<y2)){polyInts[ints++]=Math.round((y-y1)*(x2-x1)/(y2-y1)+x1);}
else if((y==maxy)&&(y>y1)&&(y<=y2)){polyInts[ints++]=Math.round((y-y1)*(x2-x1)/(y2-y1)+x1);}}
polyInts.sort(function CompInt(x,y){return(x-y);});for(i=0;i<ints;i+=2){_mkDiv(elm,polyInts[i],y,polyInts[i+1]-polyInts[i]+1,1);}}
return $(this);};function AssignOpts($cont,opts)
{$cont.opts=$.extend({},$.fn.settings,opts);return SetStroke($cont);}
function SetStroke(elm)
{var x=elm.opts.stroke;if(!(x+1))
{elm.drawLine=_mkLinDott;elm._mkOv=_mkOvDott;elm.drawRect=_mkRectDott;}
else if(x-1>0)
{elm.drawLine=_mkLin2D;elm._mkOv=_mkOv2D;elm.drawRect=_mkRect;}
else
{elm.drawLine=_mkLin;elm._mkOv=_mkOv;elm.drawRect=_mkRect;}
return elm;}
function _mkLin(elm,x1,y1,x2,y2)
{if(x1>x2)
{var _x2=x2;var _y2=y2;x2=x1;y2=y1;x1=_x2;y1=_y2;}
var dx=x2-x1,dy=Math.abs(y2-y1),x=x1,y=y1,yIncr=(y1>y2)?-1:1;if(dx>=dy)
{var pr=dy<<1,pru=pr-(dx<<1),p=pr-dx,ox=x;while(dx>0)
{--dx;++x;if(p>0)
{_mkDiv(elm,ox,y,x-ox,1);y+=yIncr;p+=pru;ox=x;}
else{p+=pr;}}
_mkDiv(elm,ox,y,x2-ox+1,1);}
else
{var pr=dx<<1,pru=pr-(dy<<1),p=pr-dy,oy=y;if(y2<=y1)
{while(dy>0)
{--dy;if(p>0)
{_mkDiv(elm,x++,y,1,oy-y+1);y+=yIncr;p+=pru;oy=y;}
else
{y+=yIncr;p+=pr;}}
_mkDiv(elm,x2,y2,1,oy-y2+1);}
else
{while(dy>0)
{--dy;y+=yIncr;if(p>0)
{_mkDiv(elm,x++,oy,1,y-oy);p+=pru;oy=y;}
else{p+=pr;}}
_mkDiv(elm,x2,oy,1,y2-oy+1);}}}
function _mkLin2D(elm,x1,y1,x2,y2)
{if(x1>x2)
{var _x2=x2;var _y2=y2;x2=x1;y2=y1;x1=_x2;y1=_y2;}
var dx=x2-x1,dy=Math.abs(y2-y1),x=x1,y=y1,yIncr=(y1>y2)?-1:1;var s=elm.opts.stroke;if(dx>=dy)
{var _s;if(dx>0&&s-3>0)
{_s=(s*dx*Math.sqrt(1+dy*dy/(dx*dx))-dx-(s>>1)*dy)/dx;_s=(!(s-4)?Math.ceil(_s):Math.round(_s))+1;}
else{_s=s;}
var ad=Math.ceil(s/2);var pr=dy<<1,pru=pr-(dx<<1),p=pr-dx,ox=x;while(dx>0)
{--dx;++x;if(p>0)
{_mkDiv(elm,ox,y,x-ox+ad,_s);y+=yIncr;p+=pru;ox=x;}
else{p+=pr;}}
_mkDiv(elm,ox,y,x2-ox+ad+1,_s);}
else
{var _s;if(s-3>0)
{_s=(s*dy*Math.sqrt(1+dx*dx/(dy*dy))-(s>>1)*dx-dy)/dy;_s=(!(s-4)?Math.ceil(_s):Math.round(_s))+1;}
else{_s=s;}
var ad=Math.round(s/2);var pr=dx<<1,pru=pr-(dy<<1),p=pr-dy,oy=y;if(y2<=y1)
{++ad;while(dy>0)
{--dy;if(p>0)
{_mkDiv(elm,x++,y,_s,oy-y+ad);y+=yIncr;p+=pru;oy=y;}
else
{y+=yIncr;p+=pr;}}
_mkDiv(elm,x2,y2,_s,oy-y2+ad);}
else
{while(dy>0)
{--dy;y+=yIncr;if(p>0)
{_mkDiv(elm,x++,oy,_s,y-oy+ad);p+=pru;oy=y;}
else p+=pr;}
_mkDiv(elm,x2,oy,_s,y2-oy+ad+1);}}}
function _mkLinDott(elm,x1,y1,x2,y2)
{if(x1>x2)
{var _x2=x2;var _y2=y2;x2=x1;y2=y1;x1=_x2;y1=_y2;}
var dx=x2-x1,dy=Math.abs(y2-y1),x=x1,y=y1,yIncr=(y1>y2)?-1:1,drw=true;if(dx>=dy)
{var pr=dy<<1,pru=pr-(dx<<1),p=pr-dx;while(dx>0)
{--dx;if(drw){_mkDiv(elm,x,y,1,1);}
drw=!drw;if(p>0)
{y+=yIncr;p+=pru;}
else p+=pr;++x;}}
else
{var pr=dx<<1,pru=pr-(dy<<1),p=pr-dy;while(dy>0)
{--dy;if(drw){_mkDiv(elm,x,y,1,1);}
drw=!drw;y+=yIncr;if(p>0)
{++x;p+=pru;}
else p+=pr;}}
if(drw){_mkDiv(elm,x,y,1,1);}}
function _mkLinVirt(aLin,x1,y1,x2,y2)
{var dx=Math.abs(x2-x1),dy=Math.abs(y2-y1),x=x1,y=y1,xIncr=(x1>x2)?-1:1,yIncr=(y1>y2)?-1:1,p,i=0;if(dx>=dy)
{var pr=dy<<1,pru=pr-(dx<<1);p=pr-dx;while(dx>0)
{--dx;if(p>0)
{aLin[i++]=x;y+=yIncr;p+=pru;}
else{p+=pr;}
x+=xIncr;}}
else
{var pr=dx<<1,pru=pr-(dy<<1);p=pr-dy;while(dy>0)
{--dy;y+=yIncr;aLin[i++]=x;if(p>0)
{x+=xIncr;p+=pru;}
else{p+=pr;}}}
for(var len=aLin.length,i=len-i;i;){aLin[len-(i--)]=x;}};function _mkOv(elm,left,top,width,height)
{var a=(++width)>>1,b=(++height)>>1,wod=width&1,hod=height&1,cx=left+a,cy=top+b,x=0,y=b,ox=0,oy=b,aa2=(a*a)<<1,aa4=aa2<<1,bb2=(b*b)<<1,bb4=bb2<<1,st=(aa2>>1)*(1-(b<<1))+bb2,tt=(bb2>>1)-aa2*((b<<1)-1),w,h;while(y>0)
{if(st<0)
{st+=bb2*((x<<1)+3);tt+=bb4*(++x);}
else if(tt<0)
{st+=bb2*((x<<1)+3)-aa4*(y-1);tt+=bb4*(++x)-aa2*(((y--)<<1)-3);w=x-ox;h=oy-y;if((w&2)&&(h&2))
{_mkOvQds(elm,cx,cy,x-2,y+2,1,1,wod,hod);_mkOvQds(elm,cx,cy,x-1,y+1,1,1,wod,hod);}
else{_mkOvQds(elm,cx,cy,x-1,oy,w,h,wod,hod);}
ox=x;oy=y;}
else
{tt-=aa2*((y<<1)-3);st-=aa4*(--y);}}
w=a-ox+1;h=(oy<<1)+hod;y=cy-oy;_mkDiv(elm,cx-a,y,w,h);_mkDiv(elm,cx+ox+wod-1,y,w,h);}
function _mkOv2D(elm,left,top,width,height)
{var s=elm.opts.stroke;width+=s+1;height+=s+1;var a=width>>1,b=height>>1,wod=width&1,hod=height&1,cx=left+a,cy=top+b,x=0,y=b,aa2=(a*a)<<1,aa4=aa2<<1,bb2=(b*b)<<1,bb4=bb2<<1,st=(aa2>>1)*(1-(b<<1))+bb2,tt=(bb2>>1)-aa2*((b<<1)-1);if(s-4<0&&(!(s-2)||width-51>0&&height-51>0))
{var ox=0,oy=b,w,h,pxw;while(y>0)
{if(st<0)
{st+=bb2*((x<<1)+3);tt+=bb4*(++x);}
else if(tt<0)
{st+=bb2*((x<<1)+3)-aa4*(y-1);tt+=bb4*(++x)-aa2*(((y--)<<1)-3);w=x-ox;h=oy-y;if(w-1)
{pxw=w+1+(s&1);h=s;}
else if(h-1)
{pxw=s;h+=1+(s&1);}
else{pxw=h=s;}
_mkOvQds(elm,cx,cy,x-1,oy,pxw,h,wod,hod);ox=x;oy=y;}
else
{tt-=aa2*((y<<1)-3);st-=aa4*(--y);}}
_mkDiv(elm,cx-a,cy-oy,s,(oy<<1)+hod);_mkDiv(elm,cx+a+wod-s,cy-oy,s,(oy<<1)+hod);}
else
{var _a=(width-(s<<1))>>1,_b=(height-(s<<1))>>1,_x=0,_y=_b,_aa2=(_a*_a)<<1,_aa4=_aa2<<1,_bb2=(_b*_b)<<1,_bb4=_bb2<<1,_st=(_aa2>>1)*(1-(_b<<1))+_bb2,_tt=(_bb2>>1)-_aa2*((_b<<1)-1),pxl=new Array(),pxt=new Array(),_pxb=new Array();pxl[0]=0;pxt[0]=b;_pxb[0]=_b-1;while(y>0)
{if(st<0)
{pxl[pxl.length]=x;pxt[pxt.length]=y;st+=bb2*((x<<1)+3);tt+=bb4*(++x);}
else if(tt<0)
{pxl[pxl.length]=x;st+=bb2*((x<<1)+3)-aa4*(y-1);tt+=bb4*(++x)-aa2*(((y--)<<1)-3);pxt[pxt.length]=y;}
else
{tt-=aa2*((y<<1)-3);st-=aa4*(--y);}
if(_y>0)
{if(_st<0)
{_st+=_bb2*((_x<<1)+3);_tt+=_bb4*(++_x);_pxb[_pxb.length]=_y-1;}
else if(_tt<0)
{_st+=_bb2*((_x<<1)+3)-_aa4*(_y-1);_tt+=_bb4*(++_x)-_aa2*(((_y--)<<1)-3);_pxb[_pxb.length]=_y-1;}
else
{_tt-=_aa2*((_y<<1)-3);_st-=_aa4*(--_y);_pxb[_pxb.length-1]--;}}}
var ox=-wod,oy=b,_oy=_pxb[0],l=pxl.length,w,h;for(var i=0;i<l;i++)
{if(typeof _pxb[i]!="undefined")
{if(_pxb[i]<_oy||pxt[i]<oy)
{x=pxl[i];_mkOvQds(elm,cx,cy,x,oy,x-ox,oy-_oy,wod,hod);ox=x;oy=pxt[i];_oy=_pxb[i];}}
else
{x=pxl[i];_mkDiv(elm,cx-x,cy-oy,1,(oy<<1)+hod);_mkDiv(elm,cx+ox+wod,cy-oy,1,(oy<<1)+hod);ox=x;oy=pxt[i];}}
_mkDiv(elm,cx-a,cy-oy,1,(oy<<1)+hod);_mkDiv(elm,cx+ox+wod,cy-oy,1,(oy<<1)+hod);}}
function _mkOvDott(left,top,width,height)
{var a=(++width)>>1,b=(++height)>>1,wod=width&1,hod=height&1,hodu=hod^1,cx=left+a,cy=top+b,x=0,y=b,aa2=(a*a)<<1,aa4=aa2<<1,bb2=(b*b)<<1,bb4=bb2<<1,st=(aa2>>1)*(1-(b<<1))+bb2,tt=(bb2>>1)-aa2*((b<<1)-1),drw=true;while(y>0)
{if(st<0)
{st+=bb2*((x<<1)+3);tt+=bb4*(++x);}
else if(tt<0)
{st+=bb2*((x<<1)+3)-aa4*(y-1);tt+=bb4*(++x)-aa2*(((y--)<<1)-3);}
else
{tt-=aa2*((y<<1)-3);st-=aa4*(--y);}
if(drw&&y>=hodu){_mkOvQds(elm,cx,cy,x,y,1,1,wod,hod);}
drw=!drw;}}
function _mkOvQds(elm,cx,cy,x,y,w,h,wod,hod)
{var xl=cx-x,xr=cx+x+wod-w,yt=cy-y,yb=cy+y+hod-h;if(xr>xl+w)
{_mkDiv(elm,xr,yt,w,h);_mkDiv(elm,xr,yb,w,h);}
else{w=xr-xl+w;}
_mkDiv(elm,xl,yt,w,h);_mkDiv(elm,xl,yb,w,h);};function _mkArcDiv(elm,x,y,oy,cx,cy,iOdds,aBndA,aBndZ,iSects)
{var xrDef=cx+x+(iOdds&0xffff),y2,h=oy-y,xl,xr,w;if(!h)h=1;x=cx-x;if(iSects&0xff0000)
{y2=cy-y-h;if(iSects&0x00ff)
{if(iSects&0x02)
{xl=Math.max(x,aBndZ[y]);w=xrDef-xl;if(w>0)_mkDiv(elm,xl,y2,w,h);}
if(iSects&0x01)
{xr=Math.min(xrDef,aBndA[y]);w=xr-x;if(w>0)_mkDiv(elm,x,y2,w,h);}}
else{_mkDiv(elm,x,y2,xrDef-x,h);}
y2=cy+y+(iOdds>>16);if(iSects&0xff00)
{if(iSects&0x0100)
{xl=Math.max(x,aBndA[y]);w=xrDef-xl;if(w>0)_mkDiv(elm,xl,y2,w,h);}
if(iSects&0x0200)
{xr=Math.min(xrDef,aBndZ[y]);w=xr-x;if(w>0)_mkDiv(elm,x,y2,w,h);}}
else{_mkDiv(elm,x,y2,xrDef-x,h);}}
else
{if(iSects&0x00ff)
{if(iSects&0x02){xl=Math.max(x,aBndZ[y]);}
else{xl=x;}
if(iSects&0x01){xr=Math.min(xrDef,aBndA[y]);}
else{xr=xrDef;}
y2=cy-y-h;w=xr-xl;if(w>0){_mkDiv(elm,xl,y2,w,h);}}
if(iSects&0xff00)
{if(iSects&0x0100){xl=Math.max(x,aBndA[y]);}
else{xl=x;}
if(iSects&0x0200){xr=Math.min(xrDef,aBndZ[y]);}
else{xr=xrDef;}
y2=cy+y+(iOdds>>16);w=xr-xl;if(w>0){_mkDiv(elm,xl,y2,w,h);}}}};function _mkRect(elm,x,y,w,h)
{var s=elm.opts.stroke;_mkDiv(elm,x,y,w,s);_mkDiv(elm,x+w,y,s,h);_mkDiv(elm,x,y+h,w+s,s);_mkDiv(elm,x,y+s,s,h-s);}
function _mkRectDott(elm,x,y,w,h)
{elm.drawLine(elm,x,y,x+w,y);elm.drawLine(elm,x+w,y,x+w,y+h);elm.drawLine(elm,x,y+h,x+w,y+h);elm.drawLine(elm,x,y,x,y+h);}
function coord(x,y){if(!x)var x=0;if(!y)var y=0;return{x:x,y:y};}
function B1(t){return t*t*t}
function B2(t){return 3*t*t*(1-t)}
function B3(t){return 3*t*(1-t)*(1-t)}
function B4(t){return(1-t)*(1-t)*(1-t)}
function getBezier(percent,x,y){var pos=new coord();pos.x=x[0]*B1(percent)+x[1]*B2(percent)+x[2]*B3(percent)+x[3]*B4(percent);pos.y=y[0]*B1(percent)+y[1]*B2(percent)+y[2]*B3(percent)+y[3]*B4(percent);return pos;}
function _mkDiv(elm,x,y,w,h)
{var res=[];res[res.length]='<div style="position:absolute;';res[res.length]='left:'+x+'px;';res[res.length]='top:'+y+'px;';res[res.length]='width:'+w+'px;';res[res.length]='height:'+h+'px;';res[res.length]='clip:rect(0,'+w+'px,'+h+'px,0);';res[res.length]='padding:0px;margin:0px;';res[res.length]='background-color:'+elm.opts.color+';';res[res.length]='overflow:hidden;';res[res.length]='"><\/div>';var result=$(res.join(''));result.css("opacity",elm.opts.alpha);elm.append(result);}})(jQuery);