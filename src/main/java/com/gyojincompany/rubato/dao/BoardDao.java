package com.gyojincompany.rubato.dao;

import java.util.ArrayList;
import java.util.List;

import com.gyojincompany.rubato.dto.FBoardDto;

public interface BoardDao {
	
	public void fbwriteDao(String fbid, String fbtitle, String fbcontent);//자유게시판 글쓰기
	public ArrayList<FBoardDto> fblistDao();//자유게시판 글목록 가져오기
	public FBoardDto fbviewDao(String fbnum);//자유게시판 글 내용 보기 
	public void fbhitDao(String fbnum);//자유게시판 조회수 증가 함수
	public ArrayList<FBoardDto> fbTitleSearchlist(String keyword);
	//자유게시판 제목에서 검색한 결과 리스트 가져오기
	public ArrayList<FBoardDto> fbContentSearchlist(String keyword);
	//자유게시판 내용에서 검색한 결과 리스트 가져오기
	public ArrayList<FBoardDto> fbNameSearchlist(String keyword);
	//자유게시판 글쓴이로 검색한 결과 리스트 가져오기
}
