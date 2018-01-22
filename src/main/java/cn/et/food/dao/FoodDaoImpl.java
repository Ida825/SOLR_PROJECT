package cn.et.food.dao;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class FoodDaoImpl {

	@Autowired
	JdbcTemplate jdbc;
	
	/**
	 * 从数据库查询数据
	 * @param start
	 * @param count
	 * @return
	 */
	public List<Map<String,Object>> queryFood(int start,int count){
		String sql = "SELECT * FROM food f INNER JOIN foodtype ft ON f.typeid=ft.typeid limit "+start+","+count;
		List<Map<String, Object>> queryForList = jdbc.queryForList(sql);
		return queryForList;
	}
	
	/**
	 * 获取数据总数
	 * @return
	 */
	public int getAllCounts(){
		String sql = "select count(*) as allCounts from food";
		return Integer.parseInt(jdbc.queryForList(sql).get(0).get("allCounts").toString());
	}
}
