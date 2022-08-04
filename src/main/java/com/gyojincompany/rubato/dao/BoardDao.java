package com.gyojincompany.rubato.dao;

import java.util.ArrayList;
import java.util.List;

import com.gyojincompany.rubato.dto.FBoardDto;

public interface BoardDao {
	
	public void fbwriteDao(String fbid, String fbtitle, String fbcontent);//자유게시판 글쓰기
	public ArrayList<FBoardDto> fblistDao();//게시판 글목록 가져오기
}
