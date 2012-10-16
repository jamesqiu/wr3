/*------------------------------------- 以下所有以ems_ 开头的函数为“黄金集团应急应用”中的函数 */
/**
 * 用于 wr3.clj.app.ems/index
 */
function ems_onload() {
	
	// 先load右边导航条
	var right_region = $('div [region="east"]')
	right_region.load('/c/ems/app-right-main', function() {		
		$.parser.parse()
		$('a#map0').click(function(e) {
			$('#ifrm1').attr('src', '/c/ems/gmap/gorgs')
		})
		$('img.province').click(function(e) {
			var province_name = $(this).attr('province')
			$('#ifrm1').attr('src', '/c/ems/gmap/gorgs2/'+province_name)
		})
		$('a#chartf').click(function(e) {
			$('#ifrm1').attr('src', '/c/chartf/tailings')			
		})
		$('a#gtail').click(function(e) {
			$('#ifrm1').attr('src', '/c/ems/gmap/gtail')			
		})
		$('a.org').click(function(e) {
			var orgid = $(this).attr('id')
			$('#ifrm1').attr('src', '/c/ems/gmap/gone/'+orgid)
		})
	})	
	
	var menu2 = $('a.easyui-linkbutton')
	menu2.click(function(e) {
		menu2.css("color", "")
		$(this).css("color", "red")
	})
}

/**
 * 在map上标注多个location的markers，并设置点击markers的事件：弹出infoWin及切换zoom
 * 
 * @param map
 * @param locations
 *            含名称、坐标(x,y)、地址信息的多个位置
 * @param myCenter0
 *            地图中心点
 * @param myZoom0
 *            初始zoom
 * @param myZoom1
 *            点击marker后的zoom
 */
function putMarkers(map, locations, myCenter0, myZoom0, myZoom1) {
	
	var image = new google.maps.MarkerImage(wr3path+'../img/beachflag.png',
			new google.maps.Size(20, 32), // marker大小为 20 pixels wide 32
											// pixels tall.
			new google.maps.Point(0,0), // The origin for this image is 0,0.
			new google.maps.Point(0, 32)); // The anchor for this image is the
											// base of the flagpole at 0,32.
	var shadow = new google.maps.MarkerImage(wr3path+'../img/beachflag_shadow.png',
			new google.maps.Size(37, 32),
			new google.maps.Point(0,0),
			new google.maps.Point(0, 32));
	var shape = {
			coord: [1, 1, 1, 20, 18, 20, 18 , 1],
			type: 'poly'
	};

	// 在所有地点放置红旗markers
	for (var i = 0; i < locations.length; i++) {
		var beach = locations[i];
		var myLatLng = new google.maps.LatLng(beach[1], beach[2]);
		var marker = new google.maps.Marker({
			position: myLatLng,
			map: map,
			shadow: shadow,
			icon: image,
			shape: shape,
			title: ''+(i+1)+'.'+beach[0]
			// zIndex: beach[3]
		});
		// jamesqiu 增加；如下语句必须提出for循环成为单独的function才能正确生效，不能在此inline
		attachMessage( marker,
				''+(i+1)+'. '+beach[0]+'<p style="color: blue">位置：'+beach[3]+'<br/>负责人：xxx （电话：889911）<br/>其他详细内容 ...</p>', 
				myLatLng);
	}
  
	var infoWins = []

	// 设置marker的click事件弹出infowin
	function attachMessage(marker, msg, latlng) {
		var infowindow = new google.maps.InfoWindow({
			content: '<h3>'+msg+'</h3>'
		})
	    // 设置点击mark事件的动作
		google.maps.event.addListener(marker, 'click', function() {
			if (map.getZoom()==myZoom0) {
				map.setCenter(latlng)
				map.setZoom(myZoom1)				
			} else {
				map.setCenter(myCenter0)
				map.setZoom(myZoom0)
			}
			// 关闭之前打开的infowindow （没找到好的自动关闭的api）
			var len = infoWins.length
			for(var i = 0; i < len; i++) {
				if (infoWins[i]) infoWins[i].close()
				infoWins.shift()
			}
			// 打开当前位置的infowindow
			infowindow.open(marker.get('map'), marker);
			infoWins.push(infowindow)
	    });	
	}

}

// 在map上放置一个div显示标题
function putControl(map, title) {

	var controlUI = document.createElement('DIV');
	controlUI.style.cursor = 'pointer';
  
	var controlText = document.createElement('DIV');
	controlText.style.fontFamily = '微软雅黑,Consolas';
	controlText.style.fontSize = '18px';
	controlText.style.color = '#f0a000';
	controlText.style.backgroundColor = 'white';
	controlText.style.paddingLeft = '20px';
	controlText.style.padding = '5px';
	controlText.innerHTML = '<b>'+title+'</b>';
	controlUI.appendChild(controlText);
  
	google.maps.event.addDomListener(controlUI, 'click', function() {
		alert('系统开发：Nasoft 北京汇金科技股份有限公司')	
	});
	
	var controlDiv = document.createElement('DIV');
	controlDiv.index = 1;
	controlDiv.style.padding = '5px';		
	controlDiv.appendChild(controlUI);
	// 在地图上摆放UI控件
	map.controls[google.maps.ControlPosition.TOP_LEFT].push(controlDiv);
}

/**
 * 地图显示所有黄金矿：数据来自service的
 */
function ems_gorgs() {
	
	var myZoom0 = 4
	var myZoom1 = 13
	
	// ajax获取位置数据，然后显示
	$.get(wr3path+"../c/ems/data1", function(data) {
		var mines2 = eval(data)

		var myCenter0 = new google.maps.LatLng(34.0, 110.5)
		var myOptions = {
			zoom: myZoom0,
			center: myCenter0,
			mapTypeId: google.maps.MapTypeId.ROADMAP  // ROADMAP, SATELLITE,
														// HYBRID, TERRAIN
		}
		var map = new google.maps.Map(document.getElementById("map_canvas"), myOptions);
		
		putMarkers(map, mines2, myCenter0, myZoom0, myZoom1);
		putControl(map, '共 ' + mines2.length + ' 家企业')
	});
// addLine(map, mines);
}

/**
 * 显示某省的黄金矿，数据来自service
 */
function ems_gorgs2(province_name) {
	
	var myZoom0 = 7
	var myZoom1 = 13
	
	// ajax获取位置数据，然后显示
	$.get(wr3path+"../c/ems/data2/"+province_name, function(data) {
		
		var mines2 = eval(data)

		var m = parseInt(mines2.length/2) // 取中间纬度金库
		var myCenter0 = new google.maps.LatLng(mines2[m][1], mines2[m][2])
		var myOptions = {
				zoom: myZoom0,
				center: myCenter0,
				mapTypeId: google.maps.MapTypeId.ROADMAP  // ROADMAP,
															// SATELLITE,
															// HYBRID, TERRAIN
		}
		var map = new google.maps.Map(document.getElementById("map_canvas"), myOptions);
		
		putMarkers(map, mines2, myCenter0, myZoom0, myZoom1);
		putControl(map, '中国黄金集团应急管理平台——'+province_name+'（'+mines2.length+'家企业）')
	});
// addLine(map, mines);
}


/**
 * 圆形表示数量多少
 */
function ems_gtail() {
	
	var myCenter0 = new google.maps.LatLng(34.0, 110.5)
	var citymap = {};
	var cityCircle;
	
	function initialize() {
		var mapOptions = {
				zoom: 4,
				center: myCenter0,
				mapTypeId: google.maps.MapTypeId.ROADMAP
	    	};
	
		var map = new google.maps.Map(document.getElementById("map_canvas"), mapOptions);
	    
		for (var city in citymap) {
			// Construct the circle for each value in citymap. We scale
			// population by 20.
			var populationOptions = {
					strokeColor: "#FF0000",
					strokeOpacity: 0.5,
					strokeWeight: 0,
					fillColor: "#FF0000",
					fillOpacity: 0.3,
					map: map,
					center: citymap[city].center,
					radius: citymap[city].count * 50000
			};
			var cityCircle = new google.maps.Circle(populationOptions);
			showPersons(cityCircle, citymap[city].org+'\n尾矿库数量为: '+citymap[city].count)
		}
		
		function showPersons(circle, info) {
			google.maps.event.addListener(circle, 'click', function () {
				alert(info)
			});
		}
		
	}

	$.get(wr3path+"../c/ems/data3", function(data) {
		
		var mines = eval(data)
		var len = mines.length
		for (var i = 0; i < len; i++) {
			var orgid = mines[i][0]
			var org = mines[i][1]
			var position = new google.maps.LatLng(mines[i][2], mines[i][3])
			var count = mines[i][4]
			citymap[orgid] = {org: org, center: position, count: count}
		}
		initialize()
	})
	
}



/**
 * 显示一个企业或者尾矿库，infowin显示主要信息
 * 
 * @pid 企业或者尾矿库的id
 */
function ems_gone(orgid) {
	
    var map;
    var orginfo;
    var position = new google.maps.LatLng(39.997383, 116.333649); // 清华东门液晶大楼

    function createInfoWindowContent() {

      return ['' + orginfo,
              '位置坐标: ' + position.lat() + ' , ' + position.lng(),
              '缩放级别: ' + map.getZoom()
             ].join('<br>') + 
             '<br/><iframe style="border: 0px solid red" src="/c/ems/">abc</iframe>'
             ;
    }

    function initialize() {
      var mapOptions = {
        zoom: 18,
        center: position,
        mapTypeId: google.maps.MapTypeId.ROADMAP,
        // 在左下显示比例尺
        scaleControl: true,
        scaleControlOptions: {
            position: google.maps.ControlPosition.LEFT_BOTTOM 
        }
      };

      map = new google.maps.Map(document.getElementById('map_canvas'), mapOptions);

      var coordInfoWindow = new google.maps.InfoWindow();
      coordInfoWindow.setContent(createInfoWindowContent());
      coordInfoWindow.setPosition(position);
      coordInfoWindow.open(map);

      google.maps.event.addListener(map, 'zoom_changed', function() { // 改变焦距时触发事件
        coordInfoWindow.setContent(createInfoWindowContent());
        coordInfoWindow.open(map);
      });
    }

	$.get(wr3path+"../c/ems/data4/"+orgid, function(data) {
		
		var org = eval(data)[0]
		orginfo = '<h3>'+org[0]+'</h3><br/>地址：'+org[4]+'<br/>邮编：'+org[3]+' ('+org[5]+')<br/>'
	    position = new google.maps.LatLng(org[1], org[2]);
		initialize()
	})
}

/**
 * 用于 wr3.clj.app.ems/index
 */
function ems_layout2() {
	
	// 先load右边导航条
	var right_region = $('div [region="west"]')
	right_region.load('/c/ems/app-left-main2', function() {		
		$.parser.parse()
		$('a#map0').click(function(e) {
			$('#ifrm1').attr('src', '/c/ems/gmap/gorgs')
		})
		$('img.province').click(function(e) {
			var province_name = $(this).attr('province')
			$('#ifrm1').attr('src', '/c/ems/gmap/gorgs2/'+province_name)
		})
		$('a#chartf').click(function(e) {
			$('#ifrm1').attr('src', '/c/chartf/tailings')			
		})
		$('a#gtail').click(function(e) {
			$('#ifrm1').attr('src', '/c/ems/gmap/gtail')			
		})
		$('a.org').click(function(e) {
			var orgid = $(this).attr('id')
			$('#ifrm1').attr('src', '/c/ems/gmap/gone/'+orgid)
		})
	})	
	
	var menu2 = $('a.easyui-linkbutton')
	menu2.click(function(e) {
		menu2.css("color", "")
		$(this).css("color", "red")
		var id = $(this).attr("id")
		$('div [region="west"]').load("/c/ems/menu3list/"+id, function(data) {
			$.parser.parse()
		})
	})
}
