package com.netshop.dao.implement;

import java.sql.SQLException;
import java.util.ArrayList;

import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import com.netshop.commons.CommonUtils;
import com.netshop.dao.ItemsDao;
import com.netshop.jdbc.DAO;
import com.netshop.model.Category;
import com.netshop.model.CriteriaItems;
import com.netshop.model.ItemBar;
import com.netshop.model.Items;
import com.netshop.pager.Expression;
import com.netshop.pager.PageBean;
import com.netshop.pager.PageConstants;

/**
 * @ClassName: ItemsDaoImpl
 * @Description: 商品模块持久层
 * @author hdm
 * @date 创建时间：2016年3月31日 上午11:57:43 @version=1.0
 */
public class ItemsDaoImpl implements ItemsDao {
	private QueryRunner qr = new DAO();

	/**
	 * 按商品名模糊查询
	 * 
	 * @param cr
	 * @return
	 * @throws SQLException
	 */
	@Override
	public List<Items> Fuzzyquery(CriteriaItems cr) throws SQLException {
		String sql = "select * from items where item_name like ?";
		return qr.query(sql, new BeanListHandler<>(Items.class), cr.getItem_name());

	}

	/**
	 * 按Iid查询
	 * 
	 * @param Iid
	 * @return
	 * @throws SQLException
	 */
	@Override
	public Items findByIid(int Iid) throws SQLException {
		String sql = "SELECT * FROM items i,category c where c.ca_id=i.item_caid  AND i.item_id=?";
		
		Map<String, Object> map = qr.query(sql, new MapHandler(), Iid);
		// 把Map中除了ca_id以外的其他属性映射到Items对象中
		Items item = CommonUtils.toBean(map, Items.class);
		// 把Map中ca_id属性映射到Category中，即这个Category只有ca_id
		Category category = CommonUtils.toBean(map, Category.class);
		// 两者建立关系
		item.setCategory(category);
		if(map.get("ca_pid")!=null){
		Category parent=new Category();
		parent.setCa_id((String)map.get("ca_pid"));
		category.setParent(parent);
		}
		return item;
	
	}

	/**
	 * 通用的查询方法
	 * 
	 * @param exprList
	 * @param pc
	 * @return
	 * @throws SQLException
	 */
	private PageBean<Items> findByCriteria(List<Expression> exprList, int pc) throws SQLException {
		int ps = PageConstants.Item_PAGE_SIZE;// 每页记录数

		// 通过exprList来生成where子句
		StringBuilder whereSql = new StringBuilder(" where 1=1");
		List<Object> params = new ArrayList<Object>();// SQL中有问号，它是对应问号的值
		for (Expression expr : exprList) {
			/*
			 * 添加一个条件上， 1) 以and开头 2) 条件的名称 3) 条件的运算符，可以是=、!=、>、< ... is null，is
			 * null没有值 4) 如果条件不是is null，再追加问号，然后再向params中添加一与问号对应的值
			 */
			whereSql.append(" or ").append(expr.getName()).append(" ").append(expr.getOperator()).append(" ");
			// where 1=1 and item_id = ?
			if (!expr.getOperator().equals("is null")) {
				whereSql.append("?");
				params.add(expr.getValue());
			}
		}
		// 总记录数
		String sql = "select count(*) from items,category" + whereSql;
		Number number = (Number) qr.query(sql, new ScalarHandler(), params.toArray());
		int tr = number.intValue();// 得到了总记录数

		// 得到beanList，即当前页记录
		sql = "select * from items i,category c," + whereSql + " order by item_id limit ?,?";
		params.add((pc - 1) * ps);// 当前页首行记录的下标
		params.add(ps);// 一共查询几行，就是每页记录数

		List<Items> beanList = qr.query(sql, new BeanListHandler<Items>(Items.class), params.toArray());

		// 创建PageBean，设置参数
		PageBean<Items> pb = new PageBean<Items>();

		// 其中PageBean没有url，这个任务由Servlet完成
		pb.setBeanList(beanList);
		pb.setPc(pc);
		pb.setPs(ps);
		pb.setTr(tr);

		return pb;
	}

	/**
	 * 按分类查询
	 * 
	 * @param cid
	 * @return
	 * @throws SQLException
	 */
	@Override
	public List<Items> findByCategory(String cid) throws SQLException {
		String sql = "select * from category c,items i where c.ca_id=i.item_caid  and ca_id=?";
		List<Map<String, Object>> mapList = qr.query(sql, new MapListHandler(), cid);
		return toCategoryList(mapList);
	}

	private List<Items> toCategoryList(List<Map<String, Object>> mapList) {
		List<Items> categoryList = new ArrayList<Items>();// 创建一个空集合
		for (Map<String, Object> map : mapList) {// 循环遍历每个Map
			Items c = toCategory(map);
			categoryList.add(c);// 添加到集合中
		}
		return categoryList;// 返回集合
	}

	private Items toCategory(Map<String, Object> map) {

		Items item = CommonUtils.toBean(map, Items.class);
		Category category = CommonUtils.toBean(map, Category.class);
		item.setCategory(category);

		return item;
	}

	/**
	 * 添加商品
	 * 
	 * @param item
	 * @throws SQLException
	 */
	@Override
	public void add(Items item) throws SQLException {
		String sql = "insert into items(item_name,item_caid,item_gdate,item_descn,"
				+ "item_price,purprice,item_wid,item_pic,barcode,item_stock) values(?,?,?,?,?,?,?,?,?,?)";
		Object[] params = { item.getItem_name(), item.getItem_caid(), item.getItem_gdate(), item.getItem_descn(),
				item.getItem_price(), item.getPurprice(), item.getItem_wid(), item.getItem_pic() ,item.getBarcode(),item.getItem_stock()};
		qr.update(sql, params);
	}

	@Override
	public int findBookCountByCategory(String cid) throws SQLException {
		String sql = "select count(*) from items where item_caid=?";
		Number cnt = (Number)qr.query(sql, new ScalarHandler(), cid);
		return cnt == null ? 0 : cnt.intValue();
	}

	@Override
	public void edit(Items items) throws SQLException {
		String sql="update items set item_name=?,item_caid=?,item_descn=?,item_price=?,purprice=?,item_stock=? where item_id=?";
		Object[] params={items.getItem_name(),items.getItem_caid(),items.getItem_descn(),items.getItem_price(),items.getPurprice(),items.getItem_stock(),items.getItem_id()};
		qr.update(sql, params);
	
	}

	@Override
	public void delete(String id) throws SQLException {
		String sql="delete from items where item_id=?";
		qr.update(sql, id);
		
	}

	@Override
	public List<Items> findByBarcode() throws SQLException {
		String sql = "SELECT * FROM items i where i.item_name is  NULL";
		List<Map<String, Object>> mapList = qr.query(sql, new MapListHandler());
		return toCategoryList(mapList);
	}

	@Override
	public ItemBar findByItemBar(int item_id) throws SQLException {
		String sql = "SELECT item_id,barcode,item_stock FROM items  WHERE item_id=?";
		Map<String, Object> map = qr.query(sql, new MapHandler(), item_id);
		
		ItemBar itemBar = CommonUtils.toBean(map, ItemBar.class);
		return itemBar;
	}

	@Override
	public int findIdByBar(String barcode) throws SQLException {
		String sql="SELECT item_id FROM `items` WHERE barcode=?";
//		Number number = (Number) qr.query(sql, new ScalarHandler(), barcode);
		int number =  (int) qr.query(sql, new ScalarHandler(), barcode);
		return number;
	}

	@Override
	public Items findItemByBarcode(String barcode) throws SQLException {
		String sql = "select * from items where barcode=?";
		Map<String, Object> map = qr.query(sql, new MapHandler(), barcode);
		Items items=CommonUtils.toBean(map, Items.class);
		return items;
	}

	
	
}
