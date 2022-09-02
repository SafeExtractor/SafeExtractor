package safeextractor.json;

public class TimeRecord {
	Long eclipseTime;
	Long oursTime;
	int id;
	
	public TimeRecord(int id,Long eclipseTime, Long oursTime) {
		super();
		this.eclipseTime = eclipseTime;
		this.oursTime = oursTime;
		this.id = id;
	}
	/**
	 * @return the eclipseTime
	 */
	public Long getEclipseTime() {
		return eclipseTime;
	}
	/**
	 * @param eclipseTime the eclipseTime to set
	 */
	public void setEclipseTime(Long eclipseTime) {
		this.eclipseTime = eclipseTime;
	}
	/**
	 * @return the oursTime
	 */
	public Long getOursTime() {
		return oursTime;
	}
	/**
	 * @param oursTime the oursTime to set
	 */
	public void setOursTime(Long oursTime) {
		this.oursTime = oursTime;
	}
	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}
	
}
