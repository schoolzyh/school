package administrator;

import Library.Book;
import Library.User;
import reader.Reader;

public class Administrator extends User {
	public static Administrator ad=null;
	public Administrator(){}
	public Administrator(String Name,String ID,String Password,char Gender,String Birthday,String Phone,String Category){
		setName(Name);
		setID(ID);
		setGender(Gender);
		setBirthday(Birthday);
		setPassword(Password);
		setPhoneNum(Phone);
		setCategory(Category);
	}

	public boolean addBook(Book book) {
		/**
		 * 调用豆瓣API，根据ISBN自动填写信息
		 * 如果查询失败，则需手动填写全部必填信息
		 * 构造一个Book对象，传给后端
		 * --- 以上均为前端的内容 ---
		 * 加到数据库中
		 */ 
		int result = database.DatabaseUpdate.insertBook(book);
		if(result == 1)			// 插入成功
			return true;
		else					// 插入失败
			return false;
	}
	
	public static String deleteBook(String bookID) {
		/**
		 * 删除单个书本
		 * 根据书的ID号来删书
		 * 如果图书处于外借状态，则不能删除
		 * 如果图书在架上，但是已被预约，则解除预约关系后删除
		 */
		Book book = database.DatabaseUpdate.findBookByBookID(bookID)[0];
		if(book == null)
			return "NOT_FOUND";
		else if(book.getLendStatus())
			return "IS_BORROWED";
		else if(book.getReservationStatus()) {
			// 构造查数据库，构造对应的reader对象，修改reservationID为null
			Reader reader = database.DatabaseUpdate.findReaderByUserID(book.getReservationReaderID())[0];
			if(reader != null) {
				database.DatabaseUpdate.setReservationBookID(reader.getID(), null);
			}
		}
		
		int result = database.DatabaseUpdate.deleteFromBook(bookID);
		if(result == 1)		// 删除成功
			return "SUCCESS";
		else				// 删除失败
			return "ERROR";
	}
	
	public String[] deleteMultipleBooks(String[] bookID) {
		/**
		 * 批量删除书本
		 * 根据传入的ID数组来删书
		 * 返回状态数组，对应每一本书的删除情况
		 */
		String[] results = new String[bookID.length];
		for(int i = 0; i < bookID.length; i++) {
			results[i] = deleteBook(bookID[i]);
		}
		return results;
	}
	
	
	public boolean addReader(Reader reader) {
		/**
		 * 添加用户 
		 * 前端填写相关信息后，检查数据合法性，构造reader对象传给后端
		 * 后端将读者信息添加到数据库中，并反馈结果
		 */
		int result = database.DatabaseUpdate.insertReader(reader);
		if(result == 1)		// 插入成功
			return true;
		else				// 插入失败
			return false;
	}
	
	public static String deleteReader(String readerID) {
		/**
		 * 删除用户
		 * 前端提供待删除的用户id
		 * 后端检查用户是否有借阅图书未归还，是否有罚金未交。如果都没有则可以删除该用户。
		 */
		// 查数据库，构造对应的reader对象
		Reader reader = database.DatabaseUpdate.findReaderByUserID(readerID)[0];
		if(reader == null)
			return "NOT_FOUND";
		else if(reader.getBorrowNum() != 0)
			return "HAS_BOOK";
		else if(reader.getFine() != 0) 
			return "HAS_FINE";
		else if(reader.getReaderStatus()) {
			// 根据reservationID构造book对象，修改其预约状态以及预约读者id
			Book book = database.DatabaseUpdate.findBookByBookID(reader.getReservationBookID())[0];
			if(book != null) {
				database.DatabaseUpdate.setBookReservationStatus(book.getID(), false);
				database.DatabaseUpdate.setReservationReaderID(book.getID(), null);
			}
		}
		
		int result = database.DatabaseUpdate.deleteFromReader(readerID);
		if(result == 1)		// 删除成功
			return "SUCCESS";
		else				// 删除失败
			return "ERROR";
	}
	
	
}