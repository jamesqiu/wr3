html * {
    margin: 0;
    /*padding: 0; SELECT NOT DISPLAYED CORRECTLY IN FIREFOX */
}

/* GENERAL */

.spinner {
    padding: 5px;
    position: absolute;
    right: 0;
}

body {
    background: #fff;
    color: #333;
    font: 12px verdana, arial, helvetica, sans-serif;
	/* jamesqiu */
    margin: 5px 15px 10px 15px;
}
a:link, a:visited, a:hover {
	/* jamesqiu */
    /* color: #666;
    font-weight: bold;
    text-decoration: none; */
}


h1 {
    color: #006dba;
    font-weight: bold; /* normal -> bold by jamesqiu */
    font-size: 16px;
    margin: .8em 0 .3em 0;
}

h2 { /* add by jamesqiu */
    color: #006dba;
    font-weight: bold;
    font-size: 14px;
    margin: .8em 0 .3em 0;
}

ul {
    padding-left: 15px;
}

input, select, textarea {
    background-color: #fcfcfc;
    border: 1px solid #ccc;
    font: 12px verdana, arial, helvetica, sans-serif;
    margin: 2px 0;
    padding: 2px 4px;
}
select {
   padding: 2px 2px 2px 0;
}
textarea {
	width: 250px;
	height: 150px;
	vertical-align: top;
}

input:focus, select:focus, textarea:focus {
    border: 1px solid #b2d1ff;
}

.body {
    float: left;
    /*margin: 0 15px 10px 15px;*/
    margin: 5 15px 10px 15px;
}

/* NAVIGATION MENU */

.nav {
    background: #fff url(../img/skin/shadow.jpg) bottom repeat-x;
    border: 1px solid #ccc;
    border-style: solid none solid none;
    margin-top: 5px;
    padding: 7px 12px;
}

.menuButton {
    font-size: 12px;
    padding: 0 5px;
}
.menuButton a {
    color: #333;
    padding: 4px 6px;
}
.menuButton a.home {
    background: url(../img/skin/house.png) center left no-repeat;
    color: #333;
    padding-left: 25px;
}
.menuButton a.list {
    background: url(../img/skin/database_table.png) center left no-repeat;
    color: #333;
    padding-left: 25px;
}
.menuButton a.create {
    background: url(../img/skin/database_add.png) center left no-repeat;
    color: #333;
    padding-left: 25px;
}

/* MESSAGES AND ERRORS */

.message {
    background: #f3f8fc url(../img/skin/information.png) 8px 50% no-repeat;
    border: 1px solid #b2d1ff;
    color: #006dba;
    margin: 10px 0 5px 0;
    padding: 5px 5px 5px 30px
}

div.errors {
    background: #fff3f3;
    border: 1px solid red;
    color: #cc0000;
    margin: 10px 0 5px 0;
    padding: 5px 0 5px 0;
}
div.errors ul {
    list-style: none;
    padding: 0;
}
div.errors li {
	background: url(../img/skin/exclamation.png) 8px 0% no-repeat;
    line-height: 16px;
    padding-left: 30px;
}

td.errors select {
    border: 1px solid red;
}
td.errors input {
    border: 1px solid red;
}

/* TABLES */

table {
    border: 1px solid #ccc;
    /* width: 100% */
}
tr {
    border: 0;
}
td, th {
    font: 12px verdana, arial, helvetica, sans-serif;
    line-height: 12px;
    padding: 5px 6px;
    /* text-align: left; */
    vertical-align: top;
}
th {
    background: #fff url(../img/skin/shadow.jpg);
	text-align: center;
    color: #666;
    font-size: 12px;
    font-weight: bold;
    line-height: 17px;
    padding: 2px 6px;
}
th a:link, th a:visited, th a:hover {
    color: #333;
    display: block;
    font-size: 12px;
    text-decoration: none;
    width: 100%;
}
th.asc a, th.desc a {
    background-position: right;
    background-repeat: no-repeat;
}
th.asc a {
    background-image: url(../img/skin/sorted_asc.gif);
}
th.desc a {
    background-image: url(../img/skin/sorted_desc.gif);
}

.odd {
    background: #f7f7f7;
}
.even {
    background: #fff;
}

/* LIST */

.list table {
    border-collapse: collapse;
}
.list th, .list td {
    border-left: 1px solid #ddd;
}
.list th:hover, .list tr:hover {
    background: #b2d1ff;
}

/* PAGINATION */

.paginateButtons {
    background: #fff url(../img/skin/shadow.jpg) bottom repeat-x;
    border: 1px solid #ccc;
    border-top: 0;
    color: #666;
    font-size: 12px;
    overflow: hidden;
    padding: 10px 3px;
}
.paginateButtons a {
    background: #fff;
    border: 1px solid #ccc;
    border-color: #ccc #aaa #aaa #ccc;
    color: #666;
    margin: 0 3px;
    padding: 2px 6px;
}
.paginateButtons span {
    padding: 2px 3px;
}

/* DIALOG */

.dialog table {
    padding: 5px 0;
}

.prop {
    padding: 5px;
}
.prop .name {
    text-align: left;
    width: 15%;
    white-space: nowrap;
}
.prop .value {
    text-align: left;
    width: 85%;
}

/* ACTION BUTTONS */

.buttons {
    background: #fff url(../img/skin/shadow.jpg) bottom repeat-x;
    border: 1px solid #ccc;
    color: #666;
    font-size: 12px;
    margin-top: 5px;
    overflow: hidden;
    padding: 0;
}

.buttons input {
    background: #fff;
    border: 0;
    color: #333;
    cursor: pointer;
    font-size: 12px;
    font-weight: bold;
    margin-left: 3px;
    overflow: visible;
    padding: 2px 6px;
}
.buttons input.delete {
    background: transparent url(../img/skin/database_delete.png) 5px 50% no-repeat;
    padding-left: 28px;
}
.buttons input.edit {
    background: transparent url(../img/skin/database_edit.png) 5px 50% no-repeat;
    padding-left: 28px;
}
.buttons input.save {
    background: transparent url(../img/skin/database_save.png) 5px 50% no-repeat;
    padding-left: 28px;
}

/* jamesqiu */

/*------------ All Table ----------*/
.wr3table {
	border-collapse: collapse;
	width: 80%;
}
.wr3table caption {
	font-family: arial;
	font-weight: bold;
}

.numberFormat { /*-- number cell --*/
	text-align: right;
}

/*------------ FormTable ----------*/
.form table {
	border-collapse: collapse;
}

.label {
	/* meta info */
	text-align: right;
	background-color: #f8f8f8;
}

/*------------ RotateTable ----------*/
.wr3table .td0 {
	/* 1st column as head */
    background: #fff url(../img/skin/shadow.jpg);
    text-align: right;
    color: #666;
    font-size: 12px;
    font-weight: bold;
    line-height: 17px;
    padding: 2px 6px;
}
/*------------ AggregateTable ----------*/
.aggre td {
	background: #eeffff;
	color: blue;
	font-weight: bold;
}

/*------------ AggregateTable ----------*/
.groupTotal td {
	background: #ccffff;
	color: blue;
	font-weight: bold;
}
.groupSum td {
	background: #eeffff;
	color: blue;
	font-weight: bold;
}

/*------------ CrossTable ----------*/
.crossLine { /* <td class='crossLine'> */
	background:url('../img/crossLine.gif');
	width:125px;
	height:40px;
}
.dimTop { /* <div class=dimTop> */
	text-align:right;
}
.dimLeft { /* <div class=dimLeft> */
	text-align:left;
}
.dim {
	text-align:center;
	vertical-align:middle;
	font-weight: bold;
}
.sum {
	background: #eeffff;
	color: blue;
	font-weight: bold;
}

/*------------ CubeTable ----------*/
.corner {
    background: #f5f5f5 url();
	/*
	background:#f5f5f5 url('../img/crossLine.gif');
    background-position: 50% 50%;
	*/
}
.frame {
    background: #f5f5f5 url();
	border: 1px solid #999;
	text-align: center;
	vertical-align: middle;
	word-break: keep-all;
   	white-space: keep-all;
	overflow: auto;
}

/*------------ PageBar ----------*/
.pg {
	margin: 5px;
}
.pg a {
	border: 1px solid lightgray;
	padding: 2px;
	margin: 2px;
    font-weight: normal;
    text-decoration: none;
	color: blue;
}
.pg a:hover {
	background-color: lightgray;
}
a.pg-prev, a.pg-next {
	color: red;
}
.pg-info {
	font-style: italic;
	margin-left: 10px;
}

/*------------ input form ----------*/
.wr3form table caption {
	font-weight: bolder;
	background-color: #e0e0e0;
	border: 1px solid gray;
	font-size: large;
}

.wr3form table tbody td {
	vertical-align: middle;
}
