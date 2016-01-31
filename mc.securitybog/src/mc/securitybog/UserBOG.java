package mc.securitybog;

public class UserBOG {
	
	private String playername;
	private Boolean isMod;
	private Boolean isAdmin;
	private Boolean isBanned;
	private Boolean isEmailConfirmed;
	
	public UserBOG(String _playername, 
			Boolean _isMod, 
			Boolean _isAdmin, 
			Boolean _isBanned, 
			Boolean _isEmailConfirmed) {
		this.playername = _playername;
		this.isMod = _isMod;
		this.isAdmin = _isAdmin;
		this.isBanned = _isBanned;
		this.isEmailConfirmed = _isEmailConfirmed;
	}
	
	public String getPlayername() {
		return this.playername;
	}
	
	public Boolean isMod() {
		return this.isMod;
	}
	
	public Boolean isAdmin() {
		return this.isAdmin;
	}
	
	public Boolean isBanned() {
		return this.isBanned;
	}
	
	public Boolean isEmailConfirmed() {
		return this.isEmailConfirmed;
	}
}
