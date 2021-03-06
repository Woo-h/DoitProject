var stomp;
var room_id;
var emp_id;
var user_name;

//우연
var susin;
var barsin;
var empArr;

// 파일 중복 업로드 방지용 맵을 선언한다.
//var map = new Map(); // Map.prototype(); 객체는 언제나 함수형태로 생성
//파일 업로드
var fd = new FormData();
// form 에 데이터 추가 시 동적으로 name 을 주기 위해 사용
var k = 0;

$(document).ready(function() {

	$(".invite-modal-body").load("./inviteJstree.do");
	
	//우연
	var empN = $("#empN").val();

	console.log("js실행");
	var sock = new SockJS("/DoitDoit_Project/stompSocket");
	console.log(sock);
	
	//방 id
	room_id = $("#room_id").val();
	// emp_id
	emp_id = $("#pr_emp_id").val();
	// emp 성함
	user_name = $("#pr_user_name").val();

	stomp = Stomp.over(sock);
	stomp.connect({}, function() {
		console.log("STOMP Connection");
		//들어 갔을 때 스크롤 밑으로 내리기
//		$('#chatLog').scrollTop($('#chatLog')[0].scrollHeight);
		
		//4. subscribe(path, callback)으로 메세지를 받을 수 있음
		// 메세지 받기
		stomp.subscribe("/sub/chat/room/" + room_id, function(chat) {
			console.log("메시지 받음",chat);
			var content = JSON.parse(chat.body);
			var clas;
			
			//누구 메세지 인지 판단
			if(content.emp_id == 0){
				clas = "issue";
			}else if(content.emp_id == emp_id){
				clas = "myMsg";
				$('#chatLog').scrollTop($('#chatLog')[0].scrollHeight);
			}else{
				clas = "anotherMsg";
			}
			console.log("받은 메시지 클래스",clas);
			//append 새로운 메시지
			var html = '';
			html += "<div class=\""+clas+"\">";
			html += 	content.chat_con;
			html += "</div>"; 
			$("#chatLog:last-child").append(html);
			
			html = "";
			if(content.chat_type == "F"){
				html += "<div class=\"files\">"
				html += content.chat_con;
				html += "</div>";
				$("#fileArea:last-child").append(html);
				if($('#fileArea').scrollTop()){
					$('#fileArea').scrollTop($('#chatLog')[0].scrollHeight);
				}
			}
			
			//scrollbar가 내려 갔을시 추가 되는 스크롤 자동으로 내려줌
			if($('#chatLog').scrollTop()){
				$('#chatLog').scrollTop($('#chatLog')[0].scrollHeight);
			};
		});
		
		//들어 왔을떄
		stomp.subscribe("/sub/chatMem/room/" + room_id, function(member){
			console.log("chatMem Enter");
			mems = member.body;
			mems = mems.replaceAll('"', '');
			mems = mems.replace('[', '');
			mems = mems.replace(']', '');
			mems = mems.split(",");
			console.log(mems);
			
			aboutChatRoom(mems);
		});
		
		roomEnter(room_id,emp_id);
		
		stomp.subscribe("/sub/chat/getOut/" + room_id, function(out){			
			var mem = JSON.parse(out.body);

			var emps = $("#members").children();
			for(var i = 0; i < emps.length; i++){
				if(emps.eq(i).attr("id") == mem.emp_id){
					$("#members").children().eq(i).remove();
				}
			}
		});
		
		stomp.subscribe("/sub/invite/room/" + room_id, function(invite){
			var mem = JSON.parse(invite.body);
			$("#members").html(mem.html);
			aboutChatRoom(mem.memList);
		});
		
		//우연
		stomp.subscribe('/sub/alarm/'+empN, function(text){
        	var hel = JSON.parse(text.body);
         
         	if(hel.type == "appr"){
	            $("#textApp").text(hel.barsin+" 님이 결재를 요청하였습니다.")
	            $("#textApp").slideDown();
	            $("#textApp").delay(3000).slideUp();
         	}else if(hel.type == "chat"){
				$("#textApp").text(hel.chat_con)
            	$("#textApp").slideDown();
            	$("#textApp").delay(6000).slideUp();
			}
      	});
      
		//우연
		stomp.subscribe('/sub/login',{});
			
		stomp.send('/pub/login', {}, JSON.stringify({ emp_id: empN }));
		
		stomp.subscribe('/sub/logout',function(){
			console.log("로그아웃")
		});
		
		//우연    
      	stomp.subscribe('/sub/approval/complet/'+empN,function(text){
         	var hel = JSON.parse(text.body);
				console.log("DAFSAFAS"+hel);
			if(hel.type == "comp"){
				console.log(hel);
	         	$("#textApp").text("결재가 완료되었습니다.")
	         	$("#textApp").slideDown();
	         	$("#textApp").delay(3000).slideUp();
			}else if(hel.type == "banryu"){
				console.log(hel);
				$("#textApp").text(hel.susin+" 님이 기안을 반려하였습니다.")
            	$("#textApp").slideDown();
            	$("#textApp").delay(3000).slideUp();
			}
    	});
	});
	
	// 채팅입력
	$("#btnSend").on("click",function(){
		chatSend();
	});
	
	//엔터키
	$("#chatCon").keydown(function(key){
		if(key.keyCode == 13){
      		chatSend();
   		}
	});
	
	//나갈때 이벤트 발생
	$(window).on("beforeunload",function(){
		roomOut(room_id, empId);
		stomp.disconnect();
	});
	
	//drag & drop
	var objDragAndDrop = $("#dragdrop");
	// dragenter : 마우스가 대상 객체의 위로 처음 진입할 때 발생함.
	$(document).on("dragenter", "#dragdrop",
			function(e) {
				//브라우저에서 기본으로 제공하는 드래그앤드롭 이벤트를 막아줘야 정상작동
				e.stopPropagation(); // 브라우저가 해당 이벤트에 대해 수행하는 기본적인 작업을 방지. 예를 들어 파일을 내려놓을 때 새탭으로 파일정보를 보여주는 이벤트를 방지 
				e.preventDefault(); // 나를 둘러싸고 있는 이벤트의 추가전파 방지
				$(this).css('border', '1px solid red');
			});
	// dragover : 드래그하면서 마우스가 대상 객체의 위에 자리 잡고 있을 때 발생함.
	$(document).on("dragover", "#dragdrop",
			function(e) {
				e.stopPropagation();
				e.preventDefault();
			});
	// drop : 	드래그가 끝나서 드래그하던 객체를 놓는 장소에 위치한 객체에서 발생함.
	$(document).on("drop", "#dragdrop", function(e) {
		console.log("drop");
		// 브라우저로 이동되는 이벤트를 방지하고 드랍 이벤트를 우선시 한다.
		e.preventDefault();
		// DROP 되는 위치에 들어온 파일 정보를 배열 형태로 받아온다.
		// drag&drop 한 모든 파일들의 정보를 가진 FileList 객체
		var files = e.originalEvent.dataTransfer.files;
		console.log("files",files);
		$(this).css('border', '1px solid black');
		// DIV에 DROP 이벤트가 발생 했을 때 다음을 호출한다.
		handleFileUpload(files);
	});
	
	//파일  크기 판단
	function handleFileUpload(files) {
		fd = new FormData();
		console.log("handle에 ",files);
		// 파일의 길이만큼 반복하며 formData에 셋팅해준다.
		var megaByte = 1024*1024;
		console.log(files.length);
		for (var i = 0; i < files.length; i++) {
			// containsValue : map에 value가 있는지 확인
			if((files[i].size/megaByte)>10){
				// 중복되는 정보 확인 위해 콘솔에 찍음
				alert(files[i].name+"은(는) \n 10메가 보다 커서 업로드가 할 수 없습니다.");
				files[i].remove;
			}else{
				console.log("append", files[i]);
				fd.append('file', files[i]);
			}
		}
		sendFileToServer(fd);
	}
	
	// 알림을 뿌려주는 아작스
	// 우연
	$.ajax({
		url:"./selAlaram.do",
		data:{emp_id:$("#empN").val()},
		type:"post",
		success:function(tag){
			console.log("성공")
			console.log(tag)            
			let html = "";
			if(tag.length>0){
				$("#bell").attr("src","./img/bellon32.png");
			}else{
				$("#bell").attr("src","./img/bell32.png");
				$("#approWindow").append("<div style='width:100%; font-size:25px; top:40%; position:absolute; text-align:center;'>알림이 없습니다.</div>")
			}
			$.each(tag,function(index,val){
				html +="<div id='alaDiv' style='border:1px solid #000; width: 100%; height:70px;' onclick='hello()'>";
				html +="	<input type='hidden' id='calId' value='"+val.cald_id+"'> ";
				html +="	<span style='font-size:13px; text-align:left;'>"+val.eboard_regdate+"</span><br> ";
				html +="	<span style='font-size:13px; text-align:center;'>"+val.eboard_title+"</span> ";
				html +="</div>";
			})
			$("#approWindow").append(html);
		},
		error:function(){
			alert("실패")
		}
	});
	
	
	
});

function hello(){
		$.ajax({
			url:"./delAlarm.do",
			data:{cald_id:$("#calId").val()},
			type:"post",
			success:function(text){
				console.log("성공여부",text)
				if(text==true){
					location.href='./approMain.do';
				}
			}
		});
}

//우연 시작
function sendEmp_No(empNo){
   	$("#frClick").click(function(){
 		empArr = empNo;
 		onSendApprMessage();
   });
}


function onSendApprMessage(){
  	console.log(empArr[0])
	susin = empArr[0];
	barsin = $("#imagename").val();
 	stomp.send("/pub/alarm/"+susin,{},JSON.stringify({type:"appr",barsin:barsin,susin:susin}));
}

function gyuljaeClick2(empList){
   	console.log("기안자 아이디 "+$("#gianja").val());
   	console.log(empList);
   	var json = JSON.parse(empList);
   	console.log("JSON 파싱 후 ",json.approval)
   	for(let i=0; i<json.approval.length; i++){
      	if(json.approval[i].EMP_ID==$("#empN").val()){
         	if(json.approval[i+1] !== undefined){
            	susin = json.approval[i+1].EMP_ID;
            	stomp.send("/pub/alarm/"+susin,{},JSON.stringify({type:"appr",barsin:$("#gianja").val(),susin:susin}));
     		}else{
            	susin = json.approval[i].EMP_ID;
        		stomp.send("/pub/apprMem/complet/"+$("#gianja").val(),{},JSON.stringify({type:"comp",susin:susin,barsin:$("#gianja").val()}));
     		} 
      	}
	}
}

function gyuljaeBanryu(empList){
   	console.log("기안자 아이디 "+$("#gianja").val());
   	var json = JSON.parse(empList);
   	console.log("JSON 파싱 후 ",json.approval)
   	for(let i=0; i<json.approval.length; i++){
      	if(json.approval[i].EMP_ID==$("#empN").val()){
				susin = json.approval[i].EMP_ID;
        		stomp.send("/pub/apprMem/complet/"+$("#gianja").val(),{},JSON.stringify({type:"banryu",susin:susin,barsin:$("#gianja").val()}));
      	}
	}
}


function disconn(){
	if(stomp.ws.readyState === WebSocket.OPEN){
		stomp.send("/pub/logout",{},JSON.stringify({emp_id:$("#empN").val()}))	
		stomp.disconnect(function(){
		});
	}
}
//우연 끝

//채팅 컨트롤러로 보내는 영역
function chatSend(){
	if($("#chatCon").val().trim() != ""){			
		stomp.send('/pub/chat/message', {}, JSON.stringify({ room_id: room_id, chat_con: $("#chatCon").val(), emp_id: emp_id, user_name: user_name, type: "T"}));
		$("#chatCon").val("");
	}else{
		alert('채팅을 입력해 주세요');
		$("#chatCon").val("");
	}
}

//채팅방 나가기(disconnect)
function getOut(){
	console.log("getOut() 실행");	
	stomp.send('/pub/chat/message', {}, JSON.stringify({room_id: room_id, emp_id: emp_id, user_name: user_name, type: "O"}));
	stomp.send('/sub/chat/getOut/' + room_id, {}, JSON.stringify({emp_id: emp_id}));
	
	location.href="./gohome.do";
}

//채팅방 입장 시 
function roomEnter(room_id,username){
	console.log("roomEnter");
	stomp.send('/pub/chat/enter', {}, JSON.stringify({ room_id: room_id, emp_id: emp_id }));
}

//채팅방 닫기 실행 시
function roomOut(room_id, username){
   stomp.send('/pub/chat/out', {}, JSON.stringify({ room_id: room_id, emp_id: emp_id, user_name: user_name }));
}

//채팅방에 있는지 없는지 판단
function aboutChatRoom(mems){
	var emps = $("#members").children();
	for(var i = 0; i < emps.length; i++){
		if($.inArray(emps.eq(i).attr("id"), mems) > -1){
			$("#members > div").eq(i).children().eq(0).text("O");
			console.log("있음");
		}else{
			$("#members > div").eq(i).children().eq(0).text("X");
			console.log("없음");
		}
	}
}

//ajax로 파일 입력
function sendFileToServer(fd) {	
	
	fd.append("user_name",user_name);
	fd.append("room_id",room_id);
	
	$.ajax({
		type : "POST",
		data : fd,
		url : "./saveFile.do",
		enctype: "multipart/form-data",
		contentType : false, // default 값은 "application/x-www-form-urlencoded; charset=UTF-8","multipart/form-data"로 전송되도록 false 설정
		processData : false, // 일반적으로 서버에 전달되는 데이터는 query string 형태임
		cache : false, //ajax 로 통신 중 cache 가 남아서 갱신된 데이터를 받아오지 못할 경우를 대비
		success : function(map){
			stomp.send('/pub/chat/message', {}, JSON.stringify({ room_id: room_id, html: map.html ,emp_id: emp_id, user_name: user_name, type: map.type }));
			fd.remove();
		},
		error:function(){
			alert("파일업로드 실패");
		}
	});
}	