package com.doit.gw.ctrl.chat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.http.HttpClient.Redirect;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.http.HttpResponse;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.util.WebUtils;

import com.doit.gw.service.chat.IChatService;
import com.doit.gw.vo.chat.ChatFileVo;
import com.doit.gw.vo.chat.ChatRoomVo;
import com.doit.gw.vo.chat.ChatVo;

@Controller
public class ChatController {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private SimpMessagingTemplate template;
	
	@Autowired
	private IChatService service;

	private List<String> listMem;//mapMem?????? ???????????? ????????? ???????????? ????????? ??????
	private List<ChatVo> listChat;//mapChat?????? ????????? ??? ????????? ???????????? ????????? ??????

	private static Map<String, List<String>> mapMem;//??? ?????? ????????? ?????? ?????? ?????? 
	private static Map<String, List<ChatVo>> mapChat;
	//??? ?????? ????????? ????????? ??????(?????? ????????? ????????? ????????? ?????? ????????? ????????? ?????????) 
	
	private static List<String> listRoomAllMem;
	private static Map<String, List<String>> mapRoomAllMem;
	//?????? ???????????? ????????? ???????????? ????????? ?????? ????????? 

	public ChatController() {
		listChat = new ArrayList<ChatVo>();
		listMem = new ArrayList<String>();

		mapMem = new HashMap<String, List<String>>();
		mapChat = new HashMap<String, List<ChatVo>>();
		
		listRoomAllMem = new ArrayList<String>();
		mapRoomAllMem = new HashMap<String, List<String>>();
	}
	
	public static List<ChatVo> getListChat(String room_id) {
		List<ChatVo> sendChat = mapChat.get(room_id);
		return sendChat;
	}
	
	public static List<String> getListMem(String room_id){
		List<String> sendMem = mapMem.get(room_id);
		return sendMem;
	}

	public static Map<String, List<String>> getRoomAllMem() {
		return mapRoomAllMem;
	}

	public static void setRoomAllMem(Map<String, List<String>> roomAllMem) {
		ChatController.mapRoomAllMem = roomAllMem;
	}

	// ???????????? ???????????????
	@MessageMapping(value = "/chat/enter")
	public void enter(ChatVo cVo) {
		logger.info("@ChatController enter() : {}", cVo);

		logger.info("^^^^ ?????? ??? ?????? {} ^^^^", mapMem.get(cVo.getRoom_id()));
		if (mapMem.get(cVo.getRoom_id()) != null) {
			listMem = mapMem.get(cVo.getRoom_id());
			logger.info("?????? ?????? {}", listMem);
		} else {
			listMem = new ArrayList<String>();
			logger.info("?????? ?????? {}", listMem);
		}

		if (!listMem.contains(cVo.getEmp_id())) {
			listMem.add(cVo.getEmp_id());
			logger.info("?????? ?????? {}", listMem);
			mapMem.put(cVo.getRoom_id(), listMem);
		}

		logger.info("^^^^^^ ?????? ?????? {} ^^^^^^", mapMem);
		template.convertAndSend("/sub/chatMem/room/" + cVo.getRoom_id(), mapMem.get(cVo.getRoom_id()));
	}

	// ????????? ?????? ?????????(?????? ??????)
	@MessageMapping(value = "/chat/out")
	public void out(ChatVo cVo) {
		logger.info("@ChatController out() : {}", cVo);

		listMem = mapMem.get(cVo.getRoom_id());
		listMem.remove(cVo.getEmp_id());

		mapMem.remove(cVo.getRoom_id());
		mapMem.put(cVo.getRoom_id(), listMem);

		logger.info("^^^^^ ?????? ?????? ????????? {} ^^^^^", listMem);
		
		if(mapMem.get(cVo.getRoom_id()).size() < 1) {
			chatSave(cVo.getRoom_id());
		}
		
		template.convertAndSend("/sub/chatMem/room/" + cVo.getRoom_id(), mapMem.get(cVo.getRoom_id()));
	}

	// ?????? ?????????
	@MessageMapping(value = "/chat/message")
	public void chatMessage(@RequestParam Map<String, String> map) throws IOException, ParseException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {		
		logger.info("@ChatController chatMessage() : {}", map);
		
		if (mapChat.get(map.get("room_id")) != null) {
			listChat = mapChat.get(map.get("room_id"));
			logger.info("?????? ?????? ?????? ?????? {}", listChat);
		} else {
			listChat = new ArrayList<ChatVo>();
			logger.info("?????? ?????? ?????? ?????? {}", listChat);
		}
		
		if(mapRoomAllMem.get(map.get("room_id")) == null) {
			List<String> memAllList = findRoomAllMem(map.get("room_id"));
			mapRoomAllMem.put(map.get("room_id"), memAllList);
		}
		
		String html = "";
		if(map.get("type").equals("T")) {
			html += "<span class=\"Name\">"+map.get("user_name")+"</span>";
			html += "<span class=\"msg\">"+ map.get("chat_con")+"</span>";

			map.put("html", html);
			map.put("type", "T");
		}else if(map.get("type").equals("O")) { // => disconnect ????????? ???
			getOut(map.get("room_id"), map.get("emp_id"));
			
			html += "<span class=\"msg\">"+map.get("user_name")+"?????? ??????????????????</span>";
			map.put("emp_id", "0");
			map.put("html", html);
			map.put("type", "O");
		}
		
		LocalDateTime now = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
		String formatedNow = now.format(formatter);
				
		ChatVo vo = new ChatVo(null, map.get("room_id"), map.get("emp_id"), map.get("html"), formatedNow.toString(), map.get("type"));
		listChat.add(vo);
		mapChat.put(map.get("room_id"), listChat);

		logger.info("????????? ?????? {}", mapChat);
		
		template.convertAndSend("/sub/chat/room/" + map.get("room_id"), vo);
		
		chatAlarm(vo);
	}
	
	private void chatAlarm(ChatVo vo) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		logger.info("@ChatController chatAlarm {}", vo);
		
		if(mapMem.get(vo.getRoom_id()) != null) {
			listMem = mapMem.get(vo.getRoom_id());
		}
		
		listRoomAllMem = mapRoomAllMem.get(vo.getRoom_id());
		
		Map<String, String> map = BeanUtils.describe(vo);
		map.put("type", "chat");
		
		logger.info("map??? ?????? {}",map);
		
		if(listMem.size() != listRoomAllMem.size()) {
			for(int i = 0; i < listRoomAllMem.size(); i++) {
				if(!listMem.contains(listRoomAllMem.get(i))) {
					logger.info("%%%%% ?????? ?????? : {} %%%%%", listRoomAllMem.get(i));
					template.convertAndSend("/sub/alarm/" + listRoomAllMem.get(i), map);
				}
			}
		}
	}

	private List<String> findRoomAllMem(String room_id) throws ParseException  {
		logger.info("@ChatController findRoomAllMem() : {}", room_id);
		
		ChatRoomVo vo = service.selRoomMember(room_id);
		
		List<String> list = new ArrayList<String>();
		
		JSONObject json = jsonParser(vo.getRoom_mem()); 
		JSONArray jsonRoom = (JSONArray) json.get("ROOM");
		
		for (int i = 0; i < jsonRoom.size(); i++) {
			JSONObject jsonVal = (JSONObject) jsonRoom.get(i);
			list.add(String.valueOf(jsonVal.get("id")));
		}
		
		return list;
	}
	
	// ?????? ????????? ????????? ??????
	@RequestMapping(value = "/saveFile.do", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	public Map<String, String> saveFile(HttpServletRequest req, MultipartHttpServletRequest multipartRequest) throws FileNotFoundException {
		logger.info("@ChatController fileMessage {}", multipartRequest);

		LocalDate date = LocalDate.now();
		
		Map<String, String> map = null;
		String html = "";

		List<MultipartFile> file = multipartRequest.getFiles("file");

		String user_name = multipartRequest.getParameter("user_name");
		String room_id =  multipartRequest.getParameter("room_id");
		
		InputStream inputStream = null;
		OutputStream outputStream = null;
		
		for (int i = 0; i < file.size(); i++) {
			ChatFileVo cFv = new ChatFileVo();

			String fileName = file.get(i).getOriginalFilename();

			cFv.setFile_chat_originnm(fileName.substring(0, fileName.lastIndexOf(".")));
			cFv.setFile_chat_type(fileName.substring(fileName.lastIndexOf(".") + 1));
			cFv.setFile_chat_uuid(UUID.randomUUID().toString());
			cFv.setFile_uploadpath(WebUtils.getRealPath(req.getSession().getServletContext(),
					"/chatFile/" + date.getYear() +"/" + date.getMonthValue() + "/" + date.getDayOfMonth()));
			cFv.setRoom_id(room_id);
			
			try {
				inputStream = file.get(i).getInputStream();

				File storage = new File(cFv.getFile_uploadpath());
				if (!storage.exists()) {
					storage.mkdirs();
				}

				File newFile = new File(
						cFv.getFile_uploadpath() + "/" + cFv.getFile_chat_uuid() + "." + cFv.getFile_chat_type());
				if (newFile.exists()) {
					newFile.createNewFile();
				}
				
				outputStream = new FileOutputStream(newFile);
				int read = 0;
				byte[] b = new byte[(int) file.get(i).getSize()];

				while ((read = inputStream.read(b)) != -1) {
					outputStream.write(b, 0, read);
				}
				
				logger.info("?????? ?????? ");
				service.insFile(cFv);
				
				map = new HashMap<String, String>();
				
				if(cFv.getFile_chat_type().equals("png") || cFv.getFile_chat_type().equals("jpg") || cFv.getFile_chat_type().equals("jpeg") || cFv.getFile_chat_type().equals("gif") || cFv.getFile_chat_type().equals("BMP")) {
					html += "<span class=\"Name\">"+user_name+"</span>";
	           		html += "<span class=\"imageMsg\">";
	           		html +=		"<img src=\"./chatFile/" + date.getYear() +"/" 
	           						+ date.getMonthValue() + "/" + date.getDayOfMonth() + "/" 
	           						+ cFv.getFile_chat_uuid()+"."+cFv.getFile_chat_type()+"\">";
	            	html += "</span>";
	            	html += "<span class=\"saveFile\"><a href=./download.do?path="+date.getYear() +"/" + 
	            				date.getMonthValue() + "/" + date.getDayOfMonth() + "/" 
	            				+ cFv.getFile_chat_uuid()+"."+cFv.getFile_chat_type()+
	            				">??????</a> <a href=\"#\">?????? ???????????? ??????</a></span>";
				}else {
					html += "<span class=\"Name\">"+user_name+"</span>";
					html += "<span class=\"fileMsg\"><span>"+cFv.getFile_chat_originnm()+"."+cFv.getFile_chat_type()+"</span></span>";
            		html += "<span class=\"saveFile\"><a href=./download.do?path="+date.getYear() +"/" + date.getMonthValue() 
            					+ "/" + date.getDayOfMonth() + "/" + cFv.getFile_chat_uuid()+"."+cFv.getFile_chat_type()+">??????</a> "
            					+ "<a href=\"#\">?????? ???????????? ??????</a></span>";
					html += "</span>";
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		try {
			inputStream.close();
			outputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		map.put("type", "F");
		map.put("html", html);
		return map;
	}
	
	@RequestMapping( value = "/download.do", method = RequestMethod.GET )
	@ResponseBody
	public byte[] fileDonwload(String path, HttpServletRequest req, HttpServletResponse resp) throws IOException {
		logger.info("@ChatController fileDownload {}", path);
		
		String realPath = WebUtils.getRealPath(req.getSession().getServletContext(), "/chatFile");
		File file = new File(realPath + "/" + path);
		
		String uuid = path.substring(path.lastIndexOf("/")+1, path.lastIndexOf("."));
				
		logger.info("originNm : {} || file : {}", uuid, file);
		
		String originNM = service.selFileNM(uuid);
		
		byte[] bytes = FileCopyUtils.copyToByteArray(file);
		
		resp.setHeader("Content-Disposition","attachment; filename=\""+originNM+"\"");
		resp.setContentLength(bytes.length);
		resp.setContentType("application/octet-stream");
		
		return bytes;
	}
	
	//?????? ??????
	private void chatSave(String room_id) {
		logger.info("@ChatController chatSave {}",room_id);
		int cnt = 0;
		
		listChat = mapChat.get(room_id);
		
		if(listChat != null) {
			for(int i = 0; i < listChat.size(); i++) {
				Map<String, String> map = new HashMap<String, String>();
				ChatVo vo = listChat.get(i);
				
				map.put("room_id", vo.getRoom_id());
				map.put("emp_id", vo.getEmp_id());
				map.put("chat_con", vo.getChat_con());
				map.put("chat_time", vo.getChat_time());
				map.put("chat_type", vo.getChat_type());
				
				cnt += service.insChat(map);
			}
		}
		
		mapChat.remove(room_id);
		
		logger.info("?????? ????????? {}",cnt);
	}
	
	//????????? ????????? update
	@SuppressWarnings("unchecked")
	private void getOut(String room_id, String emp_id) throws ParseException  {
		ChatRoomVo vo = service.selRoomMember(room_id);
		
		mapRoomAllMem.remove(room_id);
		
		logger.info("????????? vo {}",vo.getRoom_mem());
		
		JSONObject json = jsonParser(vo.getRoom_mem());
				
		JSONArray jsonRoom = (JSONArray) json.get("ROOM");
		
		for (int i = 0; i < jsonRoom.size(); i++) {
			JSONObject jsonVal = (JSONObject) jsonRoom.get(i);
			String id = (String) jsonVal.get("id");
			
			if(id.equals(emp_id)) {
				jsonRoom.remove(i);
			}
		} 
		
		logger.info("?????? ?????? {}",jsonRoom);
		if(jsonRoom.size() == 0) {
			logger.info("????????? ?????? ?????? {}",jsonRoom);
			mapMem.remove(room_id);
			service.delChatRoom(room_id);
		}else {
			json.clear();
			json.put("ROOM", jsonRoom);
			vo.setRoom_mem(json.toString());
			
			service.updRoomMember(vo);
		}
	}
	
	public JSONObject jsonParser(String value) throws ParseException  {
		JSONParser parser = new JSONParser();
		Object obj = parser.parse(value);
		JSONObject json = (JSONObject)obj;
		
		return json;
	}
}