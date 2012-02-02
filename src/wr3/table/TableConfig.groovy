data {
	dataSource = "h2"
	sql1 = "select * from loan"
	sql2 = "select * from deposit"
	sql3 {
		dataSource = "mysql"
		sql = "select * from persons"
	}
}

t1 {
	type = "Form"
	data = data.sql2
}

t2 {
	type = "list"
	data = data.sql1
}