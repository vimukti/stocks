
<%
	String andhra_jyothy=(String)request.getAttribute("andhra_jyothy");
	String andhra_prabha=(String)request.getAttribute("andhra_prabha");
	String deccan=(String)request.getAttribute("deccan");
	String nt=(String)request.getAttribute("nt");
	String vartha=(String)request.getAttribute("vartha");
	String mf=(String)request.getAttribute("mf");
%>
<h1>Mutual Funds</h1>
<form method="post" action="/create">
	<input type="hidden" name="type" value="mf" />
	<input type="text" name="date" value="8-Aug-2012" />
	<input type="submit" name="send" value="Create Mutual Funds"/>
</form>
<form method="post" action="/send">
	<input type="hidden" name="type" value="mf" />
	<input type="submit" name="send" value="Send Mutual Funds"/></br>
	<textarea id="mf" name="mf" rows="6" cols="40"><%= mf %></textarea>
</form>

<h1>Stocks</h1>
<form method="post" action="/create">
	<input type="hidden" name="type" value="stocks" />
	<input type="submit" name="create" value="Create Stocks"/>
</form>
<form method="post" action="/send">
	<input type="hidden" name="type" value="allStocks" />
	<input type="submit" name="send" value="Send All Stocks"/></br>
</form>
<form method="post" action="/send">
	<input type="hidden" name="type" value="stocks" />
	<input type="submit" name="send" value="Send Stocks"/></br>
	<select id="client" name="client" onchange="loadTos()">
		<option value="andhra_jyothy">andhra_jyothy</option>
		<option value="andhra_prabha">andhra_prabha</option>
		<option value="deccan">deccan</option>
		<option value="nt">nt</option>
		<option value="vartha">vartha</option>
	</select></br>
	<textarea id="to" name="to" rows="4" cols="40"><%= andhra_jyothy %></textarea>
</form>

<h1>Details</h1>
<form method="post" action="/details">
	<input type="submit" name="create" value="Show Details"/>	
</form>

<a href="/logout">Log out</a>

 <script type="text/javascript" charset="utf-8">
 	var allTos={
	 	andhra_jyothy:'<%= andhra_jyothy %>',
	 	andhra_prabha:'<%= andhra_prabha %>',
	 	deccan:'<%= deccan %>',
	 	nt:'<%= nt %>',
	 	vartha:'<%= vartha %>'
 	};
 	function loadTos(){
 		var e=document.getElementById("client");
 		var clnt = e.options[e.selectedIndex].value;
 		var tos=allTos[clnt];
 		var t=document.getElementById("to");
 		t.value=tos; 		
 	}
 	
 </script>