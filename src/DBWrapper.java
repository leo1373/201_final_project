import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBWrapper {
	
	private static DBWrapper shared_instance = null;
	
	private static final String url = "jdbc:mysql://localhost:3306/blackjack";
	private static final String username = "root";
	private static final String password = "djx123456";
	
	private Connection conn;
	
	private DBWrapper() {
		try {
			conn = DriverManager.getConnection(url, username, password);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static DBWrapper shared() {
		if (shared_instance == null) {
			shared_instance = new DBWrapper();
		}
		return shared_instance;
	}
	
	public Player login(String username, String password) {
		try {
			String sql = "select * from Users where username =? AND password = SHA1(?)";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, username);
			ps.setString(2, password);
			
			ResultSet rs = ps.executeQuery();
			if(rs.next()) {
				return new Player(rs.getInt("id"), rs.getString("username"), rs.getDouble("tokens"));
			}
		} catch(SQLException e) {
			System.out.println(e.getMessage());
			return null;
		}
		return null;
	}
	
	public boolean addUser(String username, String password) {
		try {
			String sql = "insert into Users (username, password, tokens) values (?, SHA1(?), 0);";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, username);
			ps.setString(2, password);
			
			int row = ps.executeUpdate();
			return true;
		} catch(SQLException e) {
			System.out.println(e.getMessage());
			return false;
		}
	}
	
	public boolean updateUser(Player u) {
		
		//TODO
		String sql = "update Users SET tokens = ?";
		sql += "WHERE id = ?";
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setInt(1, u.get_user_id());
			ps.setDouble(2, u.get_money());
			
			int row = ps.executeUpdate();
			if(row == 1) 
				return true;
			else
				return false;
		} catch(SQLException e) {
			return false;
		}
	}
}
