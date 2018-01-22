package cn.et.food.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.et.food.dao.FoodDaoImpl;
import cn.et.food.utils.SolrUtils;

@RestController
public class FoodController {

	@Autowired
	FoodDaoImpl dao;

	/**
	 * 将查询的数据写入solr
	 * 
	 * @return
	 * @throws IOException
	 * @throws SolrServerException
	 */
	@GetMapping("/createIndex")
	public String createIndex() {
		try {
			// 获取总数据
			int allCounts = dao.getAllCounts();
			int start = 0;
			int count = 5;

			while (start < allCounts) {
				// 查到所有的数据
				List<Map<String, Object>> listMap = dao.queryFood(start, count);
				for (Map<String, Object> map : listMap) {
					SolrInputDocument document = new SolrInputDocument();
					document.addField("foodid_i", map.get("foodid"));
					document.addField("foodname_ik", map.get("foodname"));
					document.addField("price_d", map.get("price"));
					document.addField("img_s", map.get("img"));
					document.addField("typename_s", map.get("typename"));
					SolrUtils.write(document);
					start++;
				}		
			}
		} catch (Exception e) {
			e.printStackTrace();
			return "0";
		}
		return "1";
	}
	
	/**
	 * 从solr查询数据
	 * @param keyWord
	 * @return
	 * @throws SolrServerException
	 * @throws IOException
	 */
	@GetMapping("/searchFood")
	public List<Map> getFood(String keyWord) throws SolrServerException, IOException{
		return SolrUtils.read("foodname_ik", keyWord);
	}
	
	/**
	 * 分组
	 * @param keyWord
	 * @return
	 * @throws SolrServerException
	 * @throws IOException
	 */
	@GetMapping("/facetFood")
	public List<Map> facet(String keyWord) throws SolrServerException, IOException{
		List<Map> list = SolrUtils.facet("foodname_ik", keyWord, "typename_s");
		System.out.println(list);
		 return list;
	}
	
	/**
	 * 多条件查询
	 * @param keyWord
	 * @param typename
	 * @return
	 * @throws SolrServerException
	 * @throws IOException
	 */
	@GetMapping("/searchFoodMulti")
	public List<Map> search(String keyWord,String typename) throws SolrServerException, IOException{
		List<Map> list =  SolrUtils.search(new String[]{"foodname_ik","typename_s"}, new String[]{keyWord,typename});
		
		return list;
	}
	
}
