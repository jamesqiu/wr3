//-------------------------------------------- wr3.clj.app.eui
/**
 * 用于 wr3.clj.app.eui/index
 */
function eui_onload() {
	// 初始化下拉选择列表
	$('#s1').combobox()
	$('#s2').combobox({multiple:true})
	// 显示右键菜单
	$('#cmenu_button').click(function(e) {
		$('#cm1').menu1('show',{
			left: e.pageX + 30,
			top: e.pageY + 30
		});
	})
	// 弹出框
	$('#message1').click(function() {
		$.messager.show({
			title: '弹出消息',
			msg: '3s后自动关闭',
			timeout: 3000, // 0 表示不自动关闭
			showType: 'show' // fade
		})
	})
	$('#progress1').click(function() {
		$.messager.progress({
			title:'请等待',
			msg:'Loading ...',
			text: '3.3秒后完成'
		})
		setTimeout(function() {
			$.messager.progress('close')			
		}, 3300)
	})
	$('#alert1').click(function() {
		$.messager.alert('标题1', '信息……')
	})
	$('#error1').click(function() {
		$.messager.alert('标题1', '信息……', 'error')		
	})
	$('#info1').click(function() {
		$.messager.alert('标题1', '信息……', 'info')				
	})
	$('#ask1').click(function() {
		$.messager.alert('标题1', '信息……', 'question')						
	})
	$('#warn1').click(function() {
		$.messager.alert('标题1', '信息……', 'warning')								
	})
	$('#confirm1').click(function(){
		$.messager.confirm('My Title', 'Are you confirm this?', function(r){
			if (r) alert('confirmed:' + r);
		});
	})
	$('#prompt1').click(function(){
		$.messager.prompt('My Title', '请输入：', function(r){
			if (r) alert('输入了:' + r);
		});		
	})
	// pager 分页条
	$('#pager1').pagination({
		total: 36,
		pagerSize: 10,
		pageNumber: 2,
		onSelectPage: function(pageNumber, pageSize) {
			$(this).pagination('loading');
			alert('当前页: '+pageNumber+', 每页条数: '+pageSize);
			$(this).pagination('loaded');
		},
		buttons: [
		          {iconCls:'icon-add', handler: function() {	alert('add'); }},
		          { iconCls:'icon-save', handler:function(){ alert('save'); } }
		]
	});
	
	// 开关panel
	$('#panel_bt').click(function() {
		$('#panel1').panel('open')
	})
	
	// progressbar演示
	$('#progress_bt').click(function() {
		var v = $('#pb1').progressbar1('getValue')
		if (v < 100) {
			$('#pb1').progressbar1('setValue', v+10)
			setTimeout(arguments.callee, 200)			
		}
	})
	
	// tabs
	$('#tt1').tabs1()
	
	// dialog 按钮事件(开关dialog1)及相应ok、cancel事件
	$('#dlg1_bt').click(function() {
		var dlg = $('#dlg1')
		var closed = dlg.dialog1('options').closed
		if (!closed) {
			dlg.dialog1("close")
		} else {
			dlg.dialog1('open')
		}
	})
	
	$('#dlg1_ok').click(function() {
		alert("-- Dialog ok --")
		$('#dlg1').dialog1('close')
	})
	
	$('#dlg1_cancel').click(function() {
		$('#dlg1').dialog1('close')
	})
	
	// 打开 window
	$('#win1_bt').click(function() {
		$('#win1').window('open')
	})
}

/**
 * wr3.clj.app.eui/layout 在onload事件时载入 参数： gmap_links: 所有左边按钮的id列表，如
 * ['gtask','gip','gcoord',..,'gclick2mark']
 */
function eui_layout() {
	
	/**
	 * 为左边Google map Demo按钮添加点击响应事件
	 * 
	 * @param gid
	 *            如：gtask, gip, gcoord, ……
	 */
	function addGmapEvent(gid) {
		$('#'+gid).click(function() {
			$('#ifrm1').attr('src', '/c/eui/gmap/'+(gid.substring(0,gid.length-3)))
		})		
	}
	
	/**
	 * 为左边Google Earth Demo按钮添加点击响应事件
	 * 
	 * @param gid
	 *            如：g3hello, g3basic, ……
	 */
	function addGearthEvent(gid) {
		$('#'+gid).click(function() {
			$('#ifrm1').attr('src', '/c/eui/gearth/'+(gid.substring(0,gid.length-3)))
		})		
	}
	
	var left_region = $('div [region="west"]')
	
	// 点击“功能演示/Google地图”菜单时转载内容并设置事件
	$('#gmap_bt').click(function() {
		// $('#layout1').layout('collapse','west')
		left_region.load('/c/eui/app-left-gmap', function() {
			$.parser.parse()
			// --- 为其下的eui-buttons增加event
			$("a[group='gmap']") .each(function(i) {
				var gid = $(this).attr('id')
				addGmapEvent(gid)
			})
			// 定位到特定经纬度位置
			$('#glocate1_bt').click(function() {
				$('#ifrm1').attr("src", "/c/eui/glocate/48.858288/2.294427") // 艾菲尔铁塔
			})
			$('#glocate2_bt').click(function() {
				$('#ifrm1').attr("src", "/c/eui/glocate/40.341370/116.026608") // 八达岭长城
			})
			$('#glocate3_bt').click(function() {
				$('#ifrm1').attr("src", "/c/eui/glocate/27.987234/86.923530") // 珠穆朗玛峰
			})
			$('#glocate4_bt').click(function() {
				$('#ifrm1').attr("src", "/c/eui/glocate/43.080699/-79.075721") // 尼亚加拉瀑布
			})
			$('#glocate5_bt').click(function() {
				$('#ifrm1').attr("src", "/c/eui/glocate/29.975611/31.130690") // 金字塔
			})			
		})
	})
	
	// 点击“功能演示/Google地*球*”菜单时转载内容并设置事件
	$('#gearth_bt').click(function() {
		// $('#layout1').layout('collapse','west')
		left_region.load('/c/eui/app-left-gearth', function() {
			$.parser.parse()
			// --- 为其下的eui-buttons增加event
			$("a[group='gearth']") .each(function(i) {
				var gid = $(this).attr('id')
				addGearthEvent(gid)
			})
		})
	})
	
	$('#foo_bt').click(function() {
		left_region.load('/c/eui/app-left-foo', function() {
			$.parser.parse()
		})
	})
}

/**
 * wr3.clj.app.eui/glocate
 */
function eui_glocate(x,y) {
    var latlng = new google.maps.LatLng(x,y); // 北京大致位于北纬39o28′～41o05′，东经115o24′～117o30′
    var myOptions = {
      zoom: 17, // 数值变大，地图就变大
      center: latlng,
      mapTypeId: google.maps.MapTypeId.HYBRID // ROADMAP, SATELLITE,
												// HYBRID,TERRAIN
    };
    var map = new google.maps.Map(document.getElementById("map_canvas"), myOptions);
}

var prjs = // 公司项目组名称及办公地点坐标样例数据
	[['北京总部——水资源保护项目组', 39.997383,116.333649],
	 ['北京农行——外协项目组', 39.865288,116.282258],
	 ['北京国开行——个人征信项目组', 39.922565,116.337071],
	 ['北京黄金集团——应急项目组', 39.959359,116.406219],
	 ['上海电监局——应急项目组', 31.209611,121.472075],
	 ['昆明滇投——管网项目组', 25.015294,102.687339],
	 ['湖南农信——信贷项目组', 28.242989,113.084464],
	 ['湖南电监办——应急项目组', 28.170412,112.992437]
	];

/*------------------------------------- 以下所有以eui_g 开头的函数为google map函数 */
/**
 * wr3.clj.app.eui/gtask
 */
function eui_gtask() {
	
	function setMarkers(map, locations) {
	  var image = new google.maps.MarkerImage(wr3path+'../img/beachflag.png',
	      new google.maps.Size(20, 32), // marker大小为 20 pixels wide 32 pixels
										// tall.
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

	  for (var i = 0; i < locations.length; i++) {
	    var beach = locations[i];
	    var myLatLng = new google.maps.LatLng(beach[1], beach[2]);
	    var marker = new google.maps.Marker({
	        position: myLatLng,
	        map: map,
	        shadow: shadow,
	        icon: image,
	        shape: shape,
	        title: beach[0]
	        // zIndex: beach[3]
	    });
		// jamesqiu 增加
	    attachMessage(marker, beach[0]); // 必须提出for循环成为单独的function才能正确生效，不能在此inline
	  }
	}
	
	// marker上添加click后出现的infowindow
	function attachMessage(marker, msg) {
	    var infowindow = new google.maps.InfoWindow({
			content: '<h3>'+msg+'</h3>'
		})
		google.maps.event.addListener(marker, 'click', function() {
			// infowindow.setPosition(e.latLng)
		  	// infowindow.open(map);
			infowindow.open(marker.get('map'), marker);
	    });	
	}

	// 在地图上绘制Polyline
	function addLine(map, locations) {
		var coords = [                        // 添加 Polyline->Polygon
	    	new google.maps.LatLng(locations[0][1], locations[0][2]),
	    	new google.maps.LatLng(locations[4][1], locations[4][2]),
	    	new google.maps.LatLng(locations[7][1], locations[7][2]),
	    	new google.maps.LatLng(locations[5][1], locations[5][2]),
	    	new google.maps.LatLng(locations[0][1], locations[0][2])
	  	];
	    var poly = new google.maps.Polygon({ // 定义 Polygon 的样式
	        path: coords,
	        strokeColor: "#FF0000",
	        strokeOpacity: 0.4,
	        strokeWeight: 3,
	        fillColor: "#FF0000",
	        fillOpacity: 0.1
	    });
		poly.setMap(map);                 // 把 Polygon 添加到地图
		google.maps.event.addListener(poly, 'click', clickResp);
	}
	
	function clickResp(event) {
		alert('区域内：[' + event.latLng.lat() + ', ' +  event.latLng.lng() + ']')
	}
	
	var myOptions = {
			zoom: 5,
			center: new google.maps.LatLng(34.0, 110.5), // 大概中国中心坐标
			mapTypeId: google.maps.MapTypeId.TERRAIN  // ROADMAP, SATELLITE,
														// HYBRID, TERRAIN
	}
	var map = new google.maps.Map(document.getElementById("map_canvas"), myOptions);
	
	setMarkers(map, prjs);
	addLine(map, prjs);
}

/**
 * wr3.clj.app.eui/gip 根据上网ip定位上网位置 todo: 可以点击弹出当前位置坐标
 */
function eui_gip() {
    
	var map;

    function initialize() {
      var myOptions = {
        zoom: 18,
        mapTypeId: google.maps.MapTypeId.HYBRID // ROADMAP
      };
      map = new google.maps.Map(document.getElementById('map_canvas'), myOptions);

      // Try HTML5 geolocation
      if(navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(function(position) {
          var pos = new google.maps.LatLng(position.coords.latitude, position.coords.longitude);

          var infowindow = new google.maps.InfoWindow({
            map: map,
            position: pos,
            content: '你目前位置在此附近.'
          });

          map.setCenter(pos);
        }, function() {
          handleNoGeolocation(true);
        });
      } else {
        // Browser doesn't support Geolocation
        handleNoGeolocation(false);
      }
    }

    function handleNoGeolocation(errorFlag) {
      if (errorFlag) {
        var content = 'Error: The Geolocation service failed.';
      } else {
        var content = 'Error: Your browser doesn\'t support geolocation.';
      }

      var options = {
        map: map,
        position: new google.maps.LatLng(60, 105),
        content: content
      };

      var infowindow = new google.maps.InfoWindow(options);
      map.setCenter(options.position);
    }

    initialize();	
}

/**
 * wr3.clj.app.eui/gcoord 显示google map位置坐标等信息, zoom_changed 时触发
 */
function eui_gcoord() {
	
    var map;
    var TILE_SIZE = 256;
    var nasoft = new google.maps.LatLng(39.997383, 116.333649); // 清华东门液晶大楼

    function bound(value, opt_min, opt_max) {
      if (opt_min != null) value = Math.max(value, opt_min);
      if (opt_max != null) value = Math.min(value, opt_max);
      return value;
    }

    function degreesToRadians(deg) {
      return deg * (Math.PI / 180);
    }

    function radiansToDegrees(rad) {
      return rad / (Math.PI / 180);
    }

    function MercatorProjection() {
      this.pixelOrigin_ = new google.maps.Point(TILE_SIZE / 2, TILE_SIZE / 2);
      this.pixelsPerLonDegree_ = TILE_SIZE / 360;
      this.pixelsPerLonRadian_ = TILE_SIZE / (2 * Math.PI);
    }

    MercatorProjection.prototype.fromLatLngToPoint = function(latLng, opt_point) {
      var me = this;
      var point = opt_point || new google.maps.Point(0, 0);
      var origin = me.pixelOrigin_;

      point.x = origin.x + latLng.lng() * me.pixelsPerLonDegree_;

      // NOTE(appleton): Truncating to 0.9999 effectively limits latitude to
      // 89.189. This is about a third of a tile past the edge of the world
      // tile.
      var siny = bound(Math.sin(degreesToRadians(latLng.lat())), -0.9999, 0.9999);
      point.y = origin.y + 0.5 * Math.log((1 + siny) / (1 - siny)) * -me.pixelsPerLonRadian_;
      return point;
    };

    MercatorProjection.prototype.fromPointToLatLng = function(point) {
      var me = this;
      var origin = me.pixelOrigin_;
      var lng = (point.x - origin.x) / me.pixelsPerLonDegree_;
      var latRadians = (point.y - origin.y) / -me.pixelsPerLonRadian_;
      var lat = radiansToDegrees(2 * Math.atan(Math.exp(latRadians)) - Math.PI / 2);
      return new google.maps.LatLng(lat, lng);
    };

    function createInfoWindowContent() {
      var numTiles = 1 << map.getZoom();
      var projection = new MercatorProjection();
      var worldCoordinate = projection.fromLatLngToPoint(nasoft);
      var pixelCoordinate = new google.maps.Point(
          worldCoordinate.x * numTiles,
          worldCoordinate.y * numTiles);
      var tileCoordinate = new google.maps.Point(
          Math.floor(pixelCoordinate.x / TILE_SIZE),
          Math.floor(pixelCoordinate.y / TILE_SIZE));

      return ['液晶大楼',
              'LatLng: ' + nasoft.lat() + ' , ' + nasoft.lng(),
              'World Coordinate: ' + worldCoordinate.x + ' , ' +
                worldCoordinate.y,
              'Pixel Coordinate: ' + Math.floor(pixelCoordinate.x) + ' , ' +
                Math.floor(pixelCoordinate.y),
              'Tile Coordinate: ' + tileCoordinate.x + ' , ' +
                tileCoordinate.y + ' at Zoom Level: ' + map.getZoom()
             ].join('<br>');
    }

    function initialize() {
      var mapOptions = {
        zoom: 18,
        center: nasoft,
        mapTypeId: google.maps.MapTypeId.ROADMAP,
        // 在左下显示比例尺
        scaleControl: true,
        scaleControlOptions: {
            position: google.maps.ControlPosition.LEFT_BOTTOM 
        }
      };

      map = new google.maps.Map(document.getElementById('map_canvas'),
          mapOptions);

      var coordInfoWindow = new google.maps.InfoWindow();
      coordInfoWindow.setContent(createInfoWindowContent());
      coordInfoWindow.setPosition(nasoft);
      coordInfoWindow.open(map);

      google.maps.event.addListener(map, 'zoom_changed', function() { // 改变焦距时触发事件
        coordInfoWindow.setContent(createInfoWindowContent());
        coordInfoWindow.open(map);
      });
    }

    initialize();	
}

/**
 * wr3.clj.app.eui/gmark 用气泡标识google map位置
 */
function eui_gmark() {

    var myOptions = {
      scaleControl: true,
      center: new google.maps.LatLng(39.997383, 116.333649), // 清华东门液晶大楼
      zoom: 15,
      mapTypeId: google.maps.MapTypeId.ROADMAP
    };

    var map = new google.maps.Map(document.getElementById('map_canvas'),
        myOptions);

    var marker = new google.maps.Marker({
      map: map,
      animation: google.maps.Animation.DROP, // 动画-出现时
      position: map.getCenter()
    });
    var infowindow = new google.maps.InfoWindow();
    infowindow.setContent('<b>清华东门液晶大楼Nasoft</b>');
    google.maps.event.addListener(marker, 'click', function() {
        infowindow.open(map, marker);
        // 动画-点击时切换
        var anim = (marker.getAnimation() != null) ? null : google.maps.Animation.BOUNCE 
        marker.setAnimation(anim);
    });
}

/**
 * wr3.clj.app.eui/gclick google map上click切换远近
 */
function eui_gclick() {
	var z0 = 4
	var z1 = 18
	var myOptions = {
		zoom: z0,
		center: new google.maps.LatLng(39.997383, 116.333649), // 清华东门液晶大楼
		mapTypeId: google.maps.MapTypeId.ROADMAP
	};

	var map = new google.maps.Map(document.getElementById('map_canvas'), myOptions);

	var marker = new google.maps.Marker({
		position: map.getCenter(),
		map: map,
		title: '切换远近角度查看液晶大厦位置'
	});

	// 点击后改变地图大小
	google.maps.event.addListener(marker, 'click', function() {
		if (map.getZoom() == z0) {
			map.setZoom(z1);
		} else {
			map.setZoom(z0);
		}
	});
}


/**
 * wr3.clj.app.eui/grandom google map上随机mark并infowin多点
 */
function eui_grandom() {

	function initialize() {
		var myOptions = {
				zoom: 5,
				center: new google.maps.LatLng(39.9136734, 116.3911514), // -25.363882,
																			// 131.044922),
				mapTypeId: google.maps.MapTypeId.ROADMAP
		};

		var map = new google.maps.Map(document.getElementById('map_canvas'), myOptions);

		// Add 5 markers to the map at random locations
		// 定一个北京市内的范围
		var southWest = new google.maps.LatLng(40.032528, 116.254776); // -31.203405,
																		// 125.244141);
		var northEast = new google.maps.LatLng(39.8131523, 116.4291229); // -25.363882,
																			// 131.044922);

		var bounds = new google.maps.LatLngBounds(southWest, northEast);
		map.fitBounds(bounds);

		var lngSpan = northEast.lng() - southWest.lng();
		var latSpan = northEast.lat() - southWest.lat();

		for (var i = 0; i < 5; i++) {
			var position = new google.maps.LatLng(
					southWest.lat() + latSpan * Math.random(),
					southWest.lng() + lngSpan * Math.random());
			var marker = new google.maps.Marker({
				position: position,
				map: map
			});

			marker.setTitle('地点' + (i + 1));
			attachSecretMessage(marker, i);
		}
	}

	// The five markers show a secret message when clicked
	// but that message is not within the marker's instance data
	function attachSecretMessage(marker, num) {
		var message = ['随机地点1', '随机地点2', '随机地点3', '随机地点4', '随机地点5'];
		var infowindow = new google.maps.InfoWindow({
			content: message[num]
		});

		google.maps.event.addListener(marker, 'click', function() {
			infowindow.open(marker.get('map'), marker);
		});
	}

	initialize();
}


/**
 * wr3.clj.app.eui/gclick2mark google map上手动点击来mark当前位置
 */
function eui_gclick2mark() {

	var map;
    var index = 0;
    var my_markers = [];
    var flag = true
	
	// 制作自定义控件
	function HomeControl(controlDiv, map) {

		  controlDiv.style.padding = '5px';

		  // Set CSS for the control border
		  var controlUI = document.createElement('DIV');
		  controlUI.style.borderStyle = 'dotted';
		  controlUI.style.borderWidth = '2px';
		  controlUI.style.cursor = 'pointer';
		  controlUI.title = '切换显示/隐藏标记层';
		  controlDiv.appendChild(controlUI);
		  
		  // Set CSS for the control interior
		  var controlText = document.createElement('DIV');
		  controlText.style.padding = '5px';
		  controlText.innerHTML = '<b>隐藏 / 显示</b>';
		  controlUI.appendChild(controlText);

		  google.maps.event.addDomListener(controlUI, 'click', function() {
			  toggleMarkers(); 				  
		  });
	}
	
    function initialize() {
        var myOptions = {
          zoom: 13,
          center: new google.maps.LatLng(39.997383, 116.333649), // 清华东门液晶大楼
																	// -25.363882,131.044922),
          mapTypeId: google.maps.MapTypeId.ROADMAP
        };
        map = new google.maps.Map(document.getElementById('map_canvas'), myOptions);

        // 在地图上摆放UI控件
        var homeControlDiv = document.createElement('DIV');
        var homeControl = new HomeControl(homeControlDiv, map);
        homeControlDiv.index = 1;
        map.controls[google.maps.ControlPosition.RIGHT_TOP].push(homeControlDiv);
        
        // 设置点击标记事件
        google.maps.event.addListener(map, 'click', function(e) {
        	placeMarker(e.latLng);
        });
    }

    function placeMarker(position) {
    	marker = new google.maps.Marker({
    		position: position,
    		map: map
    		// ,
    		// title: '标记'+(++index)
    	});
    	// map.panTo(position);
    	my_markers.push(marker)
    }
    
    
    function toggleMarkers() {
    	if (my_markers) {
    		var m = flag ? null : map;
    		for (var i=0; i<my_markers.length;i++) { // 不能用 for(i in
														// my_markers) {
    			my_markers[i].setMap(m);    			
    		}
    		flag=!flag;
    	}
    }

    alert('点击地图来标记位置')
    initialize()
}


/**
 * wr3.clj.app.eui/gnoui google map no UI
 */
function eui_gnoui() {
	 var myOptions = {
		  zoom: 18,
	      center: new google.maps.LatLng(39.997383, 116.333649), // 清华东门液晶大楼
																	// -33,
																	// 151),
	      disableDefaultUI: true,
	      mapTypeId: google.maps.MapTypeId.ROADMAP
	}
	var map = new google.maps.Map(document.getElementById("map_canvas"), myOptions);
}

/**
 * wr3.clj.app.eui/gcui google map customer UI
 */
function eui_gcui() {

	var map;
	var address0 = new google.maps.LatLng(31.209611,121.472075)
	var nasoft = new google.maps.LatLng(39.997383, 116.333649) // 清华东门液晶大楼

	// 在map上放置一个div
	function HomeControl(controlDiv, map) {

	  controlDiv.style.padding = '5px';

	  // Set CSS for the control border
	  var controlUI = document.createElement('DIV');
// controlUI.style.backgroundColor = 'white';
	  controlUI.style.borderStyle = 'dotted';
	  controlUI.style.borderWidth = '2px';
	  controlUI.style.cursor = 'pointer';
// controlUI.style.textAlign = 'center';
	  controlUI.title = '定位到指定地点';
	  controlDiv.appendChild(controlUI);
	  
	  // Set CSS for the control interior
	  var controlText = document.createElement('DIV');
// controlText.style.fontFamily = 'Arial,sans-serif';
// controlText.style.fontSize = '12px';
// controlText.style.paddingLeft = '4px';
// controlText.style.paddingRight = '4px';
	  controlText.style.padding = '5px';
	  controlText.innerHTML = '<b>北京 / 上海</b>';
	  controlUI.appendChild(controlText);

	  var ui2 = document.createElement('DIV')
	  ui2.title = '查看帮助'
	  ui2.style.borderStyle = 'solid'
	  ui2.style.textAlign = 'center'
	  var text2 = document.createElement('DIV')
	  text2.innerHTML = '关于'
	  controlDiv.appendChild(ui2)
	  ui2.appendChild(text2)
	  
	  var flag = true
	  google.maps.event.addDomListener(controlUI, 'click', function() {
		  map.setCenter(flag ? nasoft : address0)
		  flag = !flag
	  });
	  google.maps.event.addDomListener(ui2, 'click', function() {
		  alert('版本0.9 （2011-10-13）' )
	  })
	}

	function initialize() {
	  var mapDiv = document.getElementById('map_canvas');
	  var myOptions = {
	    zoom: 17,
	    center: address0,
	    mapTypeId: google.maps.MapTypeId.ROADMAP
	  }
	  map = new google.maps.Map(mapDiv, myOptions);

	  var homeControlDiv = document.createElement('DIV');
	  var homeControl = new HomeControl(homeControlDiv, map);

	  homeControlDiv.index = 1;
	  // 在地图上摆放UI控件
	  map.controls[google.maps.ControlPosition.RIGHT_TOP].push(homeControlDiv);
	}

	initialize()
}

/**
 * 圆形表示数量多少
 */
function eui_gpie() {
	
	// Create an object containing LatLng, population.
	var citymap = {};
	citymap['beijing'] = {
			center: new google.maps.LatLng(prjs[1][1], prjs[1][2]),
			persons: 25
	};
	citymap['shanghai'] = {
			center: new google.maps.LatLng(prjs[4][1], prjs[4][2]),
			persons: 10
	};
	citymap['kunming'] = {
			center: new google.maps.LatLng(prjs[5][1], prjs[5][2]),
			persons: 15
	}	
	var cityCircle;
	
	function initialize() {
		var mapOptions = {
				zoom: 4,
				center: new google.maps.LatLng(prjs[0][1], prjs[0][2]),
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
					radius: citymap[city].persons * 10000
			};
			var cityCircle = new google.maps.Circle(populationOptions);
			showPersons(cityCircle, citymap[city].persons)
		}
		
		function showPersons(circle, persons) {
			google.maps.event.addListener(circle, 'click', function () {
				alert('项目参与人数：'+persons)
			});
		}
		
	}
	
	initialize()
}
	
/**
 * 方形区域覆盖显示
 */
function eui_grect() {
	var lat0 = prjs[0][1]
	var lng0 = prjs[0][2]
    var coachella = new google.maps.LatLng(lat0, lng0); // 液晶大楼

    var myOptions = {
      zoom: 10,
      center: coachella,
      mapTypeId: google.maps.MapTypeId.ROADMAP
    };

    var map = new google.maps.Map(document.getElementById("map_canvas"), myOptions);
        
    var rectangle = new google.maps.Rectangle();

    var bounds1 = new google.maps.LatLngBounds(
    	    new google.maps.LatLng(lat0-0.1, lng0-0.2),
    	    new google.maps.LatLng(lat0+0.1, lng0+0.2)
    );
    
    alert('请缩放以显示覆盖区域。')
    
    google.maps.event.addListener(map, 'zoom_changed', function() {
      // Get the current bounds, which reflect the bounds before the zoom.
      var rectOptions = {
        strokeColor: "#FF0000",
        strokeOpacity: 0.8,
        strokeWeight: 2,
        fillColor: "#FF0000",
        fillOpacity: 0.35,
        map: map,
        bounds: bounds1 // map.getBounds()
      };
      rectangle.setOptions(rectOptions);
    });	
}

/**
 * 图片覆盖显示
 */
function eui_gpic() {
	var lat0 = prjs[0][1]
	var lng0 = prjs[0][2]

	var newark = new google.maps.LatLng(lat0, lng0);
	var imageBounds = new google.maps.LatLngBounds(
			new google.maps.LatLng(lat0-0.0001, lng0-0.0004),
			new google.maps.LatLng(lat0+0.0001, lng0+0.0004));

	var myOptions = {
			zoom: 19,
			center: newark,
			mapTypeId: google.maps.MapTypeId.ROADMAP
	}

	var map = new google.maps.Map(document.getElementById("map_canvas"), myOptions);

	var pic = new google.maps.GroundOverlay(
			"/img/nasoft-logo.jpg",
			imageBounds);
	pic.setMap(map);	
}

/**
 * 
 */
function eui_glayer() {
	
	  var overlay;

	  USGSOverlay.prototype = new google.maps.OverlayView();

	  function initialize() {
	    var myLatLng = new google.maps.LatLng(62.323907, -150.109291);
	    var myOptions = {
	      zoom: 11,
	      center: myLatLng,
	      mapTypeId: google.maps.MapTypeId.SATELLITE
	    };

	    var map = new google.maps.Map(document.getElementById("map_canvas"), myOptions);

	    var swBound = new google.maps.LatLng(62.281819, -150.287132);
	    var neBound = new google.maps.LatLng(62.400471, -150.005608);
	    var bounds = new google.maps.LatLngBounds(swBound, neBound);

	    // Photograph courtesy of the U.S. Geological Survey
	    var srcImage = 'http://code.google.com/apis/maps/documentation/javascript/examples/images/talkeetna.png';
	    overlay = new USGSOverlay(bounds, srcImage, map);
	  }

	  function USGSOverlay(bounds, image, map) {

	    // Now initialize all properties.
	    this.bounds_ = bounds;
	    this.image_ = image;
	    this.map_ = map;

	    // We define a property to hold the image's div. We'll
	    // actually create this div upon receipt of the onAdd()
	    // method so we'll leave it null for now.
	    this.div_ = null;

	    // Explicitly call setMap on this overlay
	    this.setMap(map);
	  }

	  USGSOverlay.prototype.onAdd = function() {

	    // Note: an overlay's receipt of onAdd() indicates that
	    // the map's panes are now available for attaching
	    // the overlay to the map via the DOM.

	    // Create the DIV and set some basic attributes.
	    var div = document.createElement('DIV');
	    div.style.borderStyle = "none";
	    div.style.borderWidth = "0px";
	    div.style.position = "absolute";

	    // Create an IMG element and attach it to the DIV.
	    var img = document.createElement("img");
	    img.src = this.image_;
	    img.style.width = "100%";
	    img.style.height = "100%";
	    div.appendChild(img);

	    // Set the overlay's div_ property to this DIV
	    this.div_ = div;

	    // We add an overlay to a map via one of the map's panes.
	    // We'll add this overlay to the overlayImage pane.
	    var panes = this.getPanes();
	    panes.overlayImage.appendChild(div);
	  }

	  USGSOverlay.prototype.draw = function() {

	    // Size and position the overlay. We use a southwest and northeast
	    // position of the overlay to peg it to the correct position and size.
	    // We need to retrieve the projection from this overlay to do this.
	    var overlayProjection = this.getProjection();

	    // Retrieve the southwest and northeast coordinates of this overlay
	    // in latlngs and convert them to pixels coordinates.
	    // We'll use these coordinates to resize the DIV.
	    var sw = overlayProjection.fromLatLngToDivPixel(this.bounds_.getSouthWest());
	    var ne = overlayProjection.fromLatLngToDivPixel(this.bounds_.getNorthEast());

	    // Resize the image's DIV to fit the indicated dimensions.
	    var div = this.div_;
	    div.style.left = sw.x + 'px';
	    div.style.top = ne.y + 'px';
	    div.style.width = (ne.x - sw.x) + 'px';
	    div.style.height = (sw.y - ne.y) + 'px';
	  }

	  USGSOverlay.prototype.onRemove = function() {
	    this.div_.parentNode.removeChild(this.div_);
	    this.div_ = null;
	  }
	  
	  initialize();
}

/**
 * 使用arcgis shp导出kml或者kmz文件
 */
function eui_gkm() {
	 var chicago = new google.maps.LatLng(prjs[0][1],prjs[0][2]);
	  var myOptions = {
	    zoom: 11,
	    center: chicago,
	    mapTypeId: google.maps.MapTypeId.ROADMAP
	  }

	  var map = new google.maps.Map(document.getElementById("map_canvas"), myOptions);

	  // var ctaLayer = new
		// google.maps.KmlLayer('http://gmaps-samples.googlecode.com/svn/trunk/ggeoxml/cta.kml');
	  // 注意：必须使用google能访问下载的公网地址，并且设置了kml、kmz的mime type
	  var ctaLayer = new google.maps.KmlLayer('http://qiujj.com:8080/province_line.kmz'); // 
	  ctaLayer.setMap(map);	
}


/**
 * 使用google fusiontable作为数据层，可做字段动态查询
 * http://www.google.com/fusiontables/DataSource?dsrcid=2054128 省会名：2054128
 * 33.84425711841772, 107.124729 省边界：2080073 33.84425711841772, 107.124729
 * River1：2054665 25.153528296822333, 98.81101213574217 chicago: 139529
 * 33.84425711841772, 107.124729
 */
function eui_gfusion() {
	
    var chicago = new google.maps.LatLng(32.153528296822333, 98.81101213574217);

    map = new google.maps.Map(document.getElementById('map_canvas'), {
      center: chicago,
      zoom: 4,
      mapTypeId: google.maps.MapTypeId.ROADMAP
    });
 
    var layer = new google.maps.FusionTablesLayer({
      query: {
    	select: 'geometry',
        from: '2080073',
        where: "name contains '西'"
      }
    });
    layer.setMap(map);	
}

/**
 * 交通状况信息 todo：可以结合eui_gip得出当前上网位置附近的交通状况；可以切换常用地点；
 */
function eui_gtraffic() {
	  var myLatlng = new google.maps.LatLng(prjs[0][1],prjs[0][2]);
	  var myOptions = {
	    zoom: 13,
	    center: myLatlng,
	    mapTypeId: google.maps.MapTypeId.ROADMAP
	  }

	  var map = new google.maps.Map(document.getElementById("map_canvas"), myOptions);

	  var trafficLayer = new google.maps.TrafficLayer();
	  trafficLayer.setMap(map);	
}

/**
 * 全世界的照片库
 */
function eui_gphoto() {

    var myOptions = {
            zoom: 18,
            center: new google.maps.LatLng(prjs[0][1], prjs[0][2]),
            mapTypeId: google.maps.MapTypeId.ROADMAP
    };

    var map = new google.maps.Map(document.getElementById('map_canvas'), myOptions);

    var panoramioLayer = new google.maps.panoramio.PanoramioLayer();
    panoramioLayer.setMap(map);

    
// var photoPanel = document.getElementById('photo-panel');
    var photoPanel = document.createElement('ul') // getElementById('photo-panel');
    photoPanel.innerHTML = '<li><strong>点击过的照片：</strong></li>'
    photoPanel.setAttribute('style', 'background-color: white')
    map.controls[google.maps.ControlPosition.RIGHT_TOP].push(photoPanel);

    google.maps.event.addListener(panoramioLayer, 'click', function(photo) {
    	var li = document.createElement('li');
    	var link = document.createElement('a');
    	link.innerHTML = photo.featureDetails.title + ': ' +
    	photo.featureDetails.author;
    	link.setAttribute('href', photo.featureDetails.url);
    	li.appendChild(link);
    	photoPanel.appendChild(li);
    	photoPanel.style.display = 'block';
    });	
}

/**
 * 航拍角度
 */
function eui_gfly() {
	var mapOptions = {
			center: new google.maps.LatLng(45.518970, -122.672899), // (36.964645,
																	// -122.01523),
			zoom: 19,
			mapTypeId: google.maps.MapTypeId.SATELLITE
	};
	map = new google.maps.Map(document.getElementById("map_canvas"), mapOptions);
	map.setTilt(45);	
	
	function rotate90() {
		var heading = map.getHeading() || 0;
		map.setHeading(heading + 90);
	}
	
    if (map.getTilt() != 0) {
        window.setInterval(rotate90, 3000);
    }
}

var mines = 
	[['湖北三鑫金铜股份有限公司', 30.190000, 115.070000],
	 ['内蒙古包头鑫达黄金矿业有限责任公司', 40.690000, 109.620000],
	 ['河南金源黄金矿业有限责任公司', 34.170000, 112.080000],
	 ['中国黄金集团夹皮沟矿业有限责任公司', 42.870000, 127.500000],
	 ['陕西太白黄金矿业有限责任公司', 33.810000, 107.210000],
	 ['河北金厂峪金矿', 40.300000, 118.450000],
	 ['河南文峪金矿', 34.790000, 111.320000],
	 ['河南秦岭金矿', 34.540000, 110.890000],
	 ['黑龙江乌拉嘎金矿', 48.400000, 130.160000],
	 ['中国黄金集团二道沟金矿', 41.790000, 120.780000],
	 ['山东烟台鑫泰黄金矿业有限责任公司', 37.050000, 121.110000],
	 ['中金黄金股份有限公司中原黄金冶炼厂', 34.740000, 111.230000],
	 ['河北峪耳崖黄金矿业有限责任公司', 40.490000, 118.550000],
	 ['潼关中金黄金矿业有限责任公司', 34.480000, 110.350000],
	 ['潼关中金冶炼有限责任公司', 34.530000, 110.310000],
	 ['苏尼特金曦黄金矿业有限责任公司', 42.390000, 112.900000],
	 ['甘肃省天水李子金矿有限公司', 34.300000, 105.820000],
	 ['辽宁金凤黄金矿业有限责任公司', 40.750000, 123.670000],
	 ['黔西南金龙黄金矿业有限责任公司', 25.280000, 105.350000],
	 ['贵州金兴黄金矿业有限责任公司', 25.520000, 105.500000],
	 ['云南黄金有限责任公司', 25.040000, 102.720000],
	 ['辽宁天利金业有限责任公司', 40.770000, 124.000000],
	 ['吉林海沟黄金矿业有限责任公司', 42.650000, 128.100000],
	 ['辽宁省排山楼金矿', 42.020000, 121.670000],
	 ['辽宁新都黄金有限责任公司', 41.730000, 120.480000],
	 ['陕西略阳横现河黄金矿业有限公司', 33.340000, 106.080000],
	 ['白河县大湾银矿有限责任公司', 32.590000, 110.040000],
	 ['陕西略阳铧厂沟金矿', 33.300000, 105.800000],
	 ['商洛市恒源矿业公司', 33.730000, 110.160000],
	 ['内蒙古金予矿业有限公司', 48.670000, 116.820000],
	 ['四川通用投资有限公司阿西金矿', 33.570000, 102.960000],
	 ['四川平武矿业有限公司', 32.460000, 104.410000],
	 ['西藏中金矿业有限公司', 31.270000, 98.210000],
	 ['广西凤山天承黄金矿业有限公司', 24.560000, 106.900000],
	 ['陕西黄金公司', 34.250000, 108.960000],
	 ['辽宁黄金公司', 41.120000, 122.350000],
	 ['河北黄金公司', 37.740000, 114.970000],
	 ['广西黄金公司', 22.820000, 108.330000],
	 ['中国黄金四川公司', 30.650000, 104.070000],
	 ['嵩县金牛有限责任公司', 34.080000, 112.000000]];

/**
 * 黄金矿
 */
function eui_gmine() {
	
	var myCenter0 = new google.maps.LatLng(34.0, 110.5)
	var myZoom0 = 5
	var myZoom1 = 13
	var infoWins = []
	
	function setMarkers(map, locations) {
	  var image = new google.maps.MarkerImage(wr3path+'../img/beachflag.png',
	      new google.maps.Size(20, 32), // marker大小为 20 pixels wide 32 pixels
										// tall.
	      new google.maps.Point(0,0), // The origin for this image is 0,0.
	      new google.maps.Point(0, 32)); // The anchor for this image is the
											// base of the flagpole at 0,32.
	  var shadow = new google.maps.MarkerImage(wr3path+'../img/beachflag_shadow.png',
	      // The shadow image is larger in the horizontal dimension
	      // while the position and offset are the same as for the main image.
	      new google.maps.Size(37, 32),
	      new google.maps.Point(0,0),
	      new google.maps.Point(0, 32));
	      // Shapes define the clickable region of the icon.
	      // The type defines an HTML &lt;area&gt; element 'poly' which
	      // traces out a polygon as a series of X,Y points. The final
	      // coordinate closes the poly by connecting to the first
	      // coordinate.
	  var shape = {
	      coord: [1, 1, 1, 20, 18, 20, 18 , 1],
	      type: 'poly'
	  };

	  // 
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
		// jamesqiu 增加
	    attachMessage(marker, ''+(i+1)+'.'+beach[0], myLatLng); // 必须提出for循环成为单独的function才能正确生效，不能在此inline
	  }
	}
	
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

	function addLine(map, o) {
		var coords = [                        // 添加 Polyline->Polygon
	    	new google.maps.LatLng(o[0][1], o[0][2]),
	    	new google.maps.LatLng(o[4][1], o[4][2]),
	    	new google.maps.LatLng(o[7][1], o[7][2]),
	    	new google.maps.LatLng(o[5][1], o[5][2]),
	    	new google.maps.LatLng(o[0][1], o[0][2]),
	  	];
	    var poly = new google.maps.Polyline({ // 定义 Polygon 的样式
	        path: coords,
	        strokeColor: "#FF0000",
	        strokeOpacity: 0.4,
	        strokeWeight: 3,
	        fillColor: "#FF0000",
	        fillOpacity: 0.1
	    });
		poly.setMap(map);                 // 把 Polygon 添加到地图
		google.maps.event.addListener(poly, 'click', clickResp);
	}
	
	function clickResp(event) {
		alert('区域内：[' + event.latLng.lat() + ', ' +  event.latLng.lng() + ']')
	}
	
	var myOptions = {
			zoom: myZoom0,
			center: myCenter0,
			mapTypeId: google.maps.MapTypeId.ROADMAP  // ROADMAP, SATELLITE,
														// HYBRID, TERRAIN
	}
	var map = new google.maps.Map(document.getElementById("map_canvas"), myOptions);
	
	setMarkers(map, mines);
// addLine(map, mines);
}


/**
 * A到B的路径标示
 */
function eui_groad() {
	
	  var directionDisplay;
	  var directionsService = new google.maps.DirectionsService();
	  var map;

	  var addr1 = new google.maps.LatLng(prjs[0][1], prjs[0][2]) // 总部
	  var addr2 = new google.maps.LatLng(prjs[1][1], prjs[1][2]) // 农行软开
	  var addr3 = new google.maps.LatLng(prjs[2][1], prjs[2][2]) // 国开
	  
	  function initialize() {
	    directionsDisplay = new google.maps.DirectionsRenderer();
	    var chicago = new google.maps.LatLng(prjs[0][1], prjs[0][2]) // 41.850033,
																		// -87.6500523);
	    var myOptions = {
	      zoom:7,
	      mapTypeId: google.maps.MapTypeId.ROADMAP,
	      center: chicago
	    }
	    map = new google.maps.Map(document.getElementById("map_canvas"), myOptions);
	    directionsDisplay.setMap(map);
	  }
	  
	  // 显示路径
	  function calcRoute() {
	    var start = addr1 // 'tsinghua'//document.getElementById("start").value;
	    var end = addr2 // 'longyue'//document.getElementById("end").value;
	    var waypts = []
	    waypts.push({location: addr3, stopover: true})
	    
	    var request = {
	        origin:start, 
	        destination:end,
	        waypoints: waypts,  // 必经之路
	        travelMode: google.maps.DirectionsTravelMode.DRIVING // WALKING
																	// BICYCLING
	    };
	    directionsService.route(request, function(response, status) {
	      if (status == google.maps.DirectionsStatus.OK) {
	        directionsDisplay.setDirections(response);
	      }
	    });
	  }
	  
	  // 计算路径距离
	  function calcDistance() {
		  var service = new google.maps.DistanceMatrixService();
		  service.getDistanceMatrix({
			  origins: [addr1],
			  destinations: [addr2],
			  travelMode: google.maps.TravelMode.DRIVING,
			  unitSystem: google.maps.UnitSystem.METRIC,
			  avoidHighways: false,
			  avoidTolls: false },
			  function callback(response, status) {
				  if (status != google.maps.DistanceMatrixStatus.OK) {
			          alert('Error was: ' + status);
				  } else {
					  var origins = response.originAddresses;
			          var destinations = response.destinationAddresses;
			          var s = '';
			          for (var i = 0; i < origins.length; i++) {
			        	  var results = response.rows[i].elements;
			        	  for (var j = 0; j < results.length; j++) {
			        		  s += '起点：' + origins[i] + "\n终点：" + destinations[j]
			        		  		+ "\n" + results[j].distance.text + " in " + results[j].duration.text;
			        		  alert(s)
			        	  }
			          }
				  }
			  });
	  }	  
	  
	  initialize();
	  calcRoute();
	  calcDistance();
}

/**
 * geometry 地球几何计算
 */
function eui_ggeo() {
	
    var poly;
    var geodesicPoly;
    var marker1;
    var marker2;

    function initialize() {
      var myOptions = {
        zoom: 4,
        center: new google.maps.LatLng(34, -40.605),
        mapTypeId: google.maps.MapTypeId.ROADMAP
      };

      var map = new google.maps.Map(document.getElementById('map_canvas'),
          myOptions);

      marker1 = new google.maps.Marker({
        map: map,
        draggable: true,
        position: new google.maps.LatLng(40.71435280, -74.0059731)
      });

      marker2 = new google.maps.Marker({
        map: map,
        draggable: true,
        position: new google.maps.LatLng(48.8566140, 2.35222190)
      });

      var bounds = new google.maps.LatLngBounds(marker1.getPosition(), marker2.getPosition());
      map.fitBounds(bounds);

      google.maps.event.addListener(marker1, 'position_changed', update);
      google.maps.event.addListener(marker2, 'position_changed', update);

      var polyOptions = {
        strokeColor: '#FF0000',
        strokeOpacity: 1.0,
        strokeWeight: 3,
        map: map
      };
      poly = new google.maps.Polyline(polyOptions);

      var geodesicOptions = {
        strokeColor: '#CC0099',
        strokeOpacity: 1.0,
        strokeWeight: 3,
        geodesic: true,
        map: map
      };
      geodesicPoly = new google.maps.Polyline(geodesicOptions);

      update();
    }

    function update() {
      var path = [marker1.getPosition(), marker2.getPosition()];
      poly.setPath(path);
      geodesicPoly.setPath(path);
      var heading = google.maps.geometry.spherical.computeHeading(path[0], path[1]);
    }

    initialize();	
}

/*------------------------------------- 以下所有以eui_g3 开头的函数为google earth函数 */
/**
 * google earth 的 hello world
 */
function eui_g3hello() {
	var ge = null;
	function init() {
		google.earth.createInstance("map3d", initCallback, failureCallback);
	}
	
	function initCallback(object) {
	  ge = object;
	  ge.getWindow().setVisibility(true);
	}
	
	function failureCallback(object) {
	}
	
	init();
}

/**
 * 定位到某个坐标
 */
function eui_g3basic() {

	var ge;
	var placemark;
	
	function init() {
		google.earth.createInstance('map3d', initCB, failureCB);
	}
	  
	function initCB(instance) {
		ge = instance;
	    ge.getWindow().setVisibility(true);
	    
	    // add a navigation control
	    ge.getNavigationControl().setVisibility(ge.VISIBILITY_AUTO);
	    
	    // add some layers
	    ge.getLayerRoot().enableLayerById(ge.LAYER_BORDERS, true);
	    ge.getLayerRoot().enableLayerById(ge.LAYER_ROADS, true);

	    // create the placemark
	    placemark = ge.createPlacemark('');
	    
	    var point = ge.createPoint('');

	    var la0 = 37 // prjs[0][1]
	    var ln0 = -122 // prjs[0][2]
	    
	    point.setLatitude(la0);
	    point.setLongitude(ln0);
	    placemark.setGeometry(point);
	    
	    // add the placemark to the earth DOM
	    ge.getFeatures().appendChild(placemark);
	    
	    // look at the placemark we created
	    var la = ge.createLookAt('');
	    la.set(	la0, ln0,
	    		0, // altitude
	    		ge.ALTITUDE_RELATIVE_TO_GROUND,
	    		0, // heading
	    		75, // straight-down tilt 倾斜度
	    		500 // range (inverse of zoom) 越小越细致
	    		);
	    ge.getView().setAbstractView(la);
	  }
	  
	  function failureCB(errorCode) { }
	  
	  init();	
}
