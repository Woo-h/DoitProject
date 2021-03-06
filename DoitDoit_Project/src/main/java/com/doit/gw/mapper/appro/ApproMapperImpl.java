package com.doit.gw.mapper.appro;

import java.util.List;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.doit.gw.vo.appro.ApproVo;

@Repository
public class ApproMapperImpl implements IApproMapper {
	@Autowired
	private SqlSessionTemplate sqlSession;

	private final String NS = "com.doit.gw.mapper.appro.ApproMapperImpl.";

	@Override
	public ApproVo insApproval(ApproVo approVo) {
		sqlSession.insert(NS+"insApproval",approVo);
		return approVo;
	}

	@Override
	public int insApproLine(ApproVo approVo) {
		return sqlSession.insert(NS+"insApproLine",approVo);
	}

	@Override
	public int insApproDraft(ApproVo approVo) {
		return sqlSession.insert(NS+"insApproDraft",approVo);
	}

	@Override
	public List<ApproVo> selMyDocument(int emp_id) {
		return sqlSession.selectList(NS+"selMyDocument",emp_id);
	}

	@Override
	public List<Map<String, Object>> selAllDocument(int emp_id) {
		return sqlSession.selectList(NS+"selAllDocument",emp_id);
	}
	
	@Override
	public List<ApproVo> selStatusDocument(ApproVo approVo) {
		return sqlSession.selectList(NS+"selStatusDocument",approVo);
	}

	@Override
	public ApproVo selOneDocument(String appro_no) {
		return sqlSession.selectOne(NS+"selOneDocument",appro_no);
	}

	@Override
	public List<Map<String, Object>> selLineList(String appro_no) {
		return sqlSession.selectList(NS+"selLineList",appro_no);
	}

	@Override
	public int updApprovedApproLine(ApproVo approVo) {
		return sqlSession.update(NS+"updApprovedApproLine",approVo);
	}

	@Override
	public int updApprovedAppro(int appro_line_no) {
		return sqlSession.update(NS+"updApprovedAppro",appro_line_no);
	}

	@Override
	public List<ApproVo> selFinalDoc(int emp_id) {
		return sqlSession.selectList(NS+"selFinalDoc",emp_id);
	}
	@Override
	public int updReturnApproLine(ApproVo approVo) {
		return sqlSession.update(NS+"updReturnApproLine",approVo);
	}

	@Override
	public int updReturnAppro(ApproVo approVo) {
		return sqlSession.update(NS+"updReturnAppro",approVo);
	}

}
