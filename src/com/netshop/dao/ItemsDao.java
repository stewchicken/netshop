package com.netshop.dao;

import java.sql.SQLException;
import java.util.List;

import com.netshop.model.Category;
import com.netshop.model.CriteriaItems;
import com.netshop.model.ItemBar;
import com.netshop.model.Items;
import com.netshop.pager.PageBean;

/**
 * @ClassName: ItemsDao
 * @Description:商品查询接口
 * @author hdm
 * @date 创建时间：2016年3月31日 上午11:57:43 @version=1.0
 */
public interface ItemsDao {
	// 模糊查询（不分页）
	public List<Items> Fuzzyquery(CriteriaItems cr) throws SQLException;

	// 模糊查询（分页）
	// public PageBean<Items> findByItemname(String iname, int pc) throws
	// SQLException;

	// 按Iid查询
	public Items findByIid(int Iid) throws SQLException;

	// 添加商品
	public void add(Items item) throws SQLException;

	// 按分类id查找
	public List<Items> findByCategory(String cid) throws SQLException;
	
	/**
	 * 商品的编辑
	 * @param items
	 * @throws SQLException 
	 */
	public void edit(Items items) throws SQLException;
	
	/**
	 * 查询指定分类下商品的个数
	 * @param cid
	 * @return
	 * @throws SQLException
	 */
	public int findBookCountByCategory(String cid) throws SQLException ;
	
	/**
	 * 删除商品的方法
	 * @param id
	 * @throws SQLException
	 */
	public void delete(String id) throws SQLException;
	
	/**
	 *根据空，查出入库的新商品
	 * @param barcode
	 * @return
	 * @throws SQLException 
	 */
	public List<Items> findByBarcode() throws SQLException ;
	
	
	/**
	 * 查找itembar
	 * @return
	 * @throws SQLException 
	 */
	public ItemBar findByItemBar(int item_id) throws SQLException;
	
	/**
	 * 通过条形码找这个条形码所在的id
	 * @param barcode
	 * @return
	 * @throws SQLException
	 */
	public int findIdByBar(String barcode) throws SQLException;
	
	/**
	 * 根据条形码精确查找item类
	 * @return
	 * @throws SQLException 
	 */
	public Items findItemByBarcode(String barcode) throws SQLException;

}
