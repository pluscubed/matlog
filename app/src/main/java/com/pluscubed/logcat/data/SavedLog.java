package com.pluscubed.logcat.data;

import java.util.List;

public class SavedLog {
	private List<String> logLines;
	private boolean truncated;
	
	public List<String> getLogLines() {
		return logLines;
	}

	public void setLogLines(List<String> logLines) {
		this.logLines = logLines;
	}

	public boolean isTruncated() {
		return truncated;
	}

	public void setTruncated(boolean truncated) {
		this.truncated = truncated;
	}
}
