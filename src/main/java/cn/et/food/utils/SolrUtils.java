package cn.et.food.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;

public class SolrUtils {
	//定义solr的id
	static Integer idplus = 1;
	// 定义solr地址
	static String url = "http://192.168.6.128:8080/solr/foodcore";
	// 创建solr客户端
	static HttpSolrClient client;
	static {
		client = new HttpSolrClient(url);
	}

	/**
	 * 将数据库写入solr
	 * 
	 * @throws IOException
	 * @throws SolrServerException
	 */
	public static void write(SolrInputDocument doc) throws SolrServerException, IOException {
		//加同步锁
		synchronized(idplus){				
			doc.addField("id", idplus);
			idplus++;
		}
		client.add(doc);
		client.commit();
	}

	/**
	 * 读取solr里的数据
	 * 
	 * @throws IOException
	 * @throws SolrServerException
	 */
	public static List<Map> read(String field, String value) throws SolrServerException, IOException {
		SolrQuery query = new SolrQuery(field + ":" + value);
		// 设置高亮
		query.setHighlight(true);
		query.addHighlightField(value);
		query.set("hl.fl", "foodname_ik");
		
		// 设置前后缀
		query.setHighlightSimplePre("<font color=red>");
		query.setHighlightSimplePost("</font>");
		List<Map> lm = new ArrayList<Map>();
		QueryResponse qr = client.query(query);
		// 获取高亮
		Map<String, Map<String, List<String>>> highlighting = qr.getHighlighting();
		SolrDocumentList results = qr.getResults();
		for (SolrDocument solrDocument : results) {
			//获取solr的id
			String id = solrDocument.get("id").toString();
			Map<String, List<String>> map = highlighting.get(id);
			List<String> list = map.get("foodname_ik");
			
			// 获取要高亮的值
			String str = list.get(0);
			Map m = new HashMap();
			m.put("foodid", solrDocument.get("foodid_i"));
			m.put("foodname", str);
			m.put("price", solrDocument.get("price_d"));
			m.put("img", solrDocument.get("img_s"));
			m.put("typename", solrDocument.get("typename_s"));
			lm.add(m);
		}

		return lm;
	}
	
	/**
	 * 查询到数据并分组
	 * @param field 
	 * @param value 
	 * @param typename 
	 * @return
	 * @throws IOException 
	 * @throws SolrServerException 
	 */
	public static List facet(String field,String value,String typename) throws SolrServerException, IOException{
		List<Map> list = new ArrayList();
		SolrQuery query = new SolrQuery(field+":"+value);
		//开启分组
		query.setFacet(true);
		//添加分组的范围
		query.addFacetField(typename);
		//查询
		QueryResponse qr = client.query(query);
		//获取查询结果
		List<FacetField> facetFields = qr.getFacetFields();
		//创建map集合，将分组结果加入map
		
		for (FacetField facetField : facetFields) {
			List<Count> values = facetField.getValues();
			for (Count count : values) {
				String name = count.getName();
				long num = count.getCount();
				if(num!=0){
					Map map = new HashMap();
					map.put(name, num);
					list.add(map);
				}
			}
		}
		return list;	
	}
	
	
	/**
	 * 多条件查询
	 * @param field
	 * @param value
	 * @return
	 * @throws SolrServerException
	 * @throws IOException
	 */
	public static List<Map> search(String[] field, String[] value) throws SolrServerException, IOException {
		
		SolrQuery query = new SolrQuery();
		StringBuffer sb = new StringBuffer();
		for(int i=0;i<field.length;i++){
			//添加过滤查询条件
			query.addFilterQuery(field[i]+":"+value[i]);
			if(i!=field.length-1){
				sb.append(field[i]+":"+value[i]+" && ");
			}else{
				//在末尾中不加&&
				sb.append(field[i]+":"+value[i]);
			}
		}
		
		query.setQuery(sb.toString());
		// 设置高亮
		query.setHighlight(true);
		query.addHighlightField("foodname_ik");
		query.set("hl.fl", "foodname_ik");
		
		
		// 设置前后缀
		query.setHighlightSimplePre("<font color=red>");
		query.setHighlightSimplePost("</font>");
		List<Map> lm = new ArrayList<Map>();
		QueryResponse qr = client.query(query);
		// 获取高亮
		Map<String, Map<String, List<String>>> highlighting = qr.getHighlighting();
		SolrDocumentList results = qr.getResults();
		for (SolrDocument solrDocument : results) {
			String id = solrDocument.get("id").toString();
			Map<String, List<String>> map = highlighting.get(id);
			List<String> list = map.get("foodname_ik");
			
			// 获取要高亮的值
			String str = list.get(0);
			Map m = new HashMap();
			m.put("foodid", solrDocument.get("foodid_i"));
			m.put("foodname", str);
			m.put("price", solrDocument.get("price_d"));
			m.put("img", solrDocument.get("img_s"));
			m.put("typename", solrDocument.get("typename_s"));
			lm.add(m);
		}

		return lm;
	}

}
