import java.time.LocalDateTime;

public class FileInfo {
	private String name;
	private LocalDateTime data;

	public FileInfo(String name, LocalDateTime data) {
		this.name = name;
		this.data = data;
	}

	public String getName() {
		return this.name;
	}

	public LocalDateTime getData() {
		return this.data;
	}
}