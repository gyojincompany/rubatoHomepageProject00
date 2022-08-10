package com.gyojincompany.rubato;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import com.gyojincompany.rubato.dao.BoardDao;
import com.gyojincompany.rubato.dao.MemberDao;
import com.gyojincompany.rubato.dto.FBoardDto;
import com.gyojincompany.rubato.dto.FileDto;

@Controller
public class HomeController {
	
	@Autowired
	private SqlSession sqlSession;
	
	@RequestMapping(value = "/")
	public String root() {
		
		return "index";
	}
	
	@RequestMapping(value = "/index")
	public String index() {
		
		return "index";
	}
	
	@RequestMapping(value = "/board_list")
	public String board_list(HttpServletRequest request, Model model) {
		
		String searchKeyword = request.getParameter("searchKeyword");
		String searchOption = request.getParameter("searchOption");
		
		BoardDao boardDao = sqlSession.getMapper(BoardDao.class);		
		
		ArrayList<FBoardDto> fbDtos = null;
		
		if(searchKeyword == null) {
			fbDtos = boardDao.fblistDao();
		} else if(searchOption.equals("title" )) {
			fbDtos = boardDao.fbTitleSearchlist(searchKeyword);
		} else if(searchOption.equals("content")) {
			fbDtos = boardDao.fbContentSearchlist(searchKeyword);
		} else if(searchOption.equals("writer")) {
			fbDtos = boardDao.fbNameSearchlist(searchKeyword);
		}				
			
		
		int listCount = fbDtos.size();//게시판 글목록의 글 개수
		
		model.addAttribute("fblist", fbDtos);
		model.addAttribute("listCount", listCount);
		
		return "board_list";
	}
	
	@RequestMapping(value = "/board_view")
	public String board_view(HttpServletRequest request, Model model) {
		
		String fbnum = request.getParameter("fbnum");
		int fbnumint = Integer.parseInt(fbnum);
		
		BoardDao boardDao = sqlSession.getMapper(BoardDao.class);
		
		boardDao.fbhitDao(fbnum);//조회수 증가 함수 호출
		
		FBoardDto fboardDto = boardDao.fbviewDao(fbnum);
		FileDto fileDto = boardDao.fbGetFileInfoDao(fbnum);
		
		model.addAttribute("fbView", fboardDto);
		model.addAttribute("fileDto", fileDto);
		model.addAttribute("rblist", boardDao.rblistDao(fbnumint));//댓글리스트 가져와서 반환하기
		model.addAttribute("boardId", fboardDto.getFbid());//게시판 아이디 불러오기
		
		return "board_view";
	}
	
	@RequestMapping(value = "/board_write")
	public String board_write() {
		
		return "board_write";
	}
	
	@RequestMapping(value = "/member_join")
	public String member_join() {
		
		return "member_join";
	}
	
	@RequestMapping(value = "/member_joinOk", method = RequestMethod.POST)
	public String member_joinOk(HttpServletRequest request, Model model) {
		
		String memberid = request.getParameter("mid");
		String memberpw = request.getParameter("mpw");
		String membername = request.getParameter("mname");
		String memberemail = request.getParameter("memail");
		
		MemberDao memberDao = sqlSession.getMapper(MemberDao.class);
		
		memberDao.memberJoinDao(memberid, memberpw, membername, memberemail);
		
		HttpSession session = request.getSession();
		
		session.setAttribute("sessionId", memberid);
		session.setAttribute("sessionName", membername);
		
		return "redirect:index";
	}
	
	@RequestMapping(value = "/logout")
	public String logout(HttpSession session) {
		
		session.invalidate();//세션 삭제->로그아웃
		
		return "redirect:index";
	}
	
	@RequestMapping(value = "/memberLoginOk", method = RequestMethod.POST)
	public String memberLoginOk(HttpServletRequest request, Model model) {
		
		String memberid = request.getParameter("mid");
		String memberpw = request.getParameter("mpw");
		
		MemberDao memberDao = sqlSession.getMapper(MemberDao.class);
		
		int checkIdValue = memberDao.checkIdDao(memberid);
		//DB에 아이디가 존재하면 1이 반환, 없으면 0이 반환
		int checkPwValue = memberDao.checkPwDao(memberid, memberpw);
		//DB에 아이디와 비밀번호가 일치하는 계정이 존재하면 1이 반환, 없으면 0이 반환
		
		model.addAttribute("checkIdValue", checkIdValue);
		model.addAttribute("checkPwValue", checkPwValue);
		
		if(checkPwValue == 1) {
			
			HttpSession session = request.getSession();
			
			session.setAttribute("sessionId", memberid);
			
		}
		
		return "loginOk";
	}
	
	@RequestMapping(value = "board_writeOk", method=RequestMethod.POST)
	public String board_writeOk(HttpServletRequest request, @RequestPart MultipartFile uploadFiles) throws Exception{
		
		String fbtitle = request.getParameter("fbtitle");
		String fbcontent = request.getParameter("fbcontent");
		
		BoardDao boardDao = sqlSession.getMapper(BoardDao.class);
		
		HttpSession session = request.getSession();
		
		String fbid = (String) session.getAttribute("sessionId");
		
		if(fbid == null) {
			fbid = "GUEST";
		}
		
		if(uploadFiles.isEmpty()) { //파일 첨부 여부를 판단(true or false)
			boardDao.fbwriteDao(fbid, fbtitle, fbcontent);
			//첨부된 파일이 없는 경우 제목과 내용만 db에 업로드
		} else {
			boardDao.fbwriteDao(fbid, fbtitle, fbcontent);
			ArrayList<FBoardDto> fbDtos = boardDao.fblistDao();
			
			String orifilename = uploadFiles.getOriginalFilename();//원래 파일의 이름 가져오기
			String fileextension = FilenameUtils.getExtension(orifilename).toLowerCase();//확장자 가져오기(소문자로 변환)
			String fileurl = "D:\\springboot_workspace\\rubatoHomepage00\\src\\main\\resources\\static\\uploadfiles\\";
			String filename;//변경된 파일의 이름(서버에 저장되는 파일의 이름)
			File desinationFile;//java.io의 파일관련 클래스
			
			do {
			filename = RandomStringUtils.randomAlphanumeric(32) + "." + fileextension;
			//영문대소문자와 숫자가 혼합된 랜덤 32자의 파일이름을 생성한 후 확장자 연결하여 서버에 저장될 파일의 이름 생성			
			desinationFile = new File(fileurl+filename);
			}while(desinationFile.exists());//같은 이름의 파일이 저장소에 존재하면 true 출력
			
			desinationFile.getParentFile().mkdir();
			uploadFiles.transferTo(desinationFile);
			
			int boardnum = fbDtos.get(0).getFbnum();
			//가져온 게시글 목록 중에서 가장 최근에 만들어진 글(FBoardDto)을 불러와 게시글번호(fbnum)만 추출
			
			boardDao.fbfileInsertDao(boardnum, filename, orifilename, fileurl, fileextension);
			
		}
		
		
		
		return "redirect:board_list";
	}	
	
	@RequestMapping(value = "replyOk")
	public String replyOk(HttpServletRequest request, Model model) {
		
		String boardnum = request.getParameter("boardnum");//덧글이 달릴 원 게시글의 고유번호
		String rbcontent = request.getParameter("rbcontent");//덧글의 내용
		int fbnum = Integer.parseInt(boardnum);
		
		HttpSession session = request.getSession();
		String sessionId = (String) session.getAttribute("sessionId");
		String rbid = null;
		
		if (sessionId == null) {
			rbid = "GUEST";			
		} else {
			rbid = sessionId;
		}
		
		BoardDao boardDao = sqlSession.getMapper(BoardDao.class);
		
		boardDao.rbwriteDao(fbnum, rbid, rbcontent);		
		
		model.addAttribute("fbView", boardDao.fbviewDao(boardnum));
		model.addAttribute("rblist", boardDao.rblistDao(fbnum));
		model.addAttribute("boardId", boardDao.fbviewDao(boardnum).getFbid());//게시판 아이디 불러오기
		
		return "board_view";
	}
	
	@RequestMapping(value = "/fbdelete")
	public String delete(HttpServletRequest request) {
		
		String fbnum = request.getParameter("fbnum");
		
		BoardDao boardDao = sqlSession.getMapper(BoardDao.class);
		
		boardDao.fbdeleteDao(fbnum);
		
		return "redirect:board_list";
	}
	
	
	
	
	
	
	
	
}
