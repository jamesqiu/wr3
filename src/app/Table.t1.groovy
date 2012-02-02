// 公用数据部分
data {
	dataSource = "h2"
	data1 = "select * from deposit"
	data2 = "select * from loan"
	data3 {
		dataSource = "mysql"
		sql = "select * from t2"
	}
}

table1 {
	type = "Form"
	data = data.data2
	meta = [a:100, b:200]
	dd = null;
	view = null
}

table2 {
	type = "list"
	data = "select * from loan"
}

// form
form {
	type = "form"
	data = data.data1
	meta = ['月份','机构','姓名','贷款额']
	dd = [001:'机构1', 002:'机构2', 003:'机构3']
}

// list
list {
	type = "list"
	data = data.data2
	max = 10 // 每页10条
	meta = ['', '', '', '']
	dd = []
}

// rotate
rotate1 {
	type = "rotate"
	data = data.data1
	meta = []
	data = []
}

// aggregate
aggre1 {
	type = "aggregate"
	data = data.data1
	sum = true
	avg = true
}

// group
group {
	type = "group"
	data = data.data1
	group = "orgid"
	sum = true
}

// frame
frame1 {
	type = "frame"
	data = [data.data1, data.data2]
}

// frame
frametop {
	type = "frame"
	top = ['org', 'hq', 'amount', 'dq']
	data = [data.data1, data.data2]
}

// frame
frameleft {
	type = "frame"
	left = ['04', '02', '01']
	data = [data.data1, data.data2]
}

// frame
framedata {
	type = "frame"
	frame = data
	data = [data.data1, data.data2]
}

// frame
frametopleft {
	type = "frame"
	top = dataTop
	left = dataLeft
	data = [data.data1, data.data2]
}

// cross
cross {
	type = "cross"
	left = 0
	top = "name"
	data = data1
}

// cube
cube {
	type = "cube"
	layout = "month, name \\ orgid, [hq, dq]"
	data = data1
	meta = []
}

