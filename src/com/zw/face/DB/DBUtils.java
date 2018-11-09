package com.zw.face.DB;

import java.sql.*;

public class DBUtils {
	private static Connection conn = null;
	private static Statement stmt = null;

	public synchronized static Statement getStatement() {
		return stmt;
	}

	static {
		String DBURL = "";
		String DBName = "";
		String DBPwd = "";
		try {
			// 注册JDBC驱动
			Class.forName("com.mysql.jdbc.Driver");
			System.out.println("连接数据库...");
			// 打开链接
			conn = DriverManager.getConnection(DBURL, DBName, DBPwd);
			stmt = conn.createStatement();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static ResultSet select(String sql) {
		try {
			System.out.println(sql);
			return stmt.executeQuery(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static int execute(String sql) {
		try {
			System.out.println(sql);
			return stmt.executeUpdate(sql);
		} catch (Exception e) {
			return -1;
		}
	}

	public static void closeConnection(Connection conn) {
		// 判断conn是否为空
		if (conn != null) {
			try {
				conn.close(); // 关闭数据库连接
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}