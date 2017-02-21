import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class URI implements Serializable {
	private static final long serialVersionUID = 1L;

	String in;
	String out;
	private static final String filename = "uri.tmp";
	public void saveURI() {

		try (ObjectOutputStream oos =
				new ObjectOutputStream(new FileOutputStream(filename))) {

			oos.writeObject(this);
			System.out.println("Done");
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
	
	public URI(String in, String out) {
		this.in = in;
		this.out = out;
	}

	static URI loadURI(){
		URI uri = null;

		try (ObjectInputStream ois
			= new ObjectInputStream(new FileInputStream(filename))) {

			uri = (URI) ois.readObject();

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return uri;

	}
}
