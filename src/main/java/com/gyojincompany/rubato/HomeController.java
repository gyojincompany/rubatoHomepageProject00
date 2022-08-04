package com.gyojincompany.rubato;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.gyojincompany.rubato.dao.BoardDao;
import com.gyojincompany.rubato.dao.MemberDao;
import com.gyojincompany.rubato.dto.FBoardDto;

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
		
		BoardDao boardDao = sqlSession.getMapper(BoardDao.class);
		
		boardDao.fbhitDao(fbnum);//조회수 증가 함수 호출
		
		FBoardDto fboardDto = boardDao.fbviewDao(fbnum);
		
		model.addAttribute("fbView", fboardDto);
		
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
	public String board_writeOk(HttpServletRequest request) {
		
		String fbtitle = request.getParameter("fbtitle");
		String fbcontent = request.getParameter("fbcontent");
		
		BoardDao boardDao = sqlSession.getMapper(BoardDao.class);
		
		HttpSession session = request.getSession();
		
		String fbid = (String) session.getAttribute("sessionId");
		
		if(fbid == null) {
			fbid = "GUEST";
		}
		
		boardDao.fbwriteDao(fbid, fbtitle, fbcontent);
		
		return "redirect:board_list";
	}	
	
	
	
	
	
	
	
	
	
	
	
	
}
