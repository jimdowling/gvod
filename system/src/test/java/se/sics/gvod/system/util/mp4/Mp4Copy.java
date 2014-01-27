package se.sics.gvod.system.util.mp4;

import mp4.util.MP4StreamingModule;
import junit.framework.TestCase;


public class Mp4Copy
    extends TestCase
{
    public Mp4Copy( String testName )
    {
        super( testName );
    }
	public void testApp() {
            String[] args = {"/var/www/topgear.mp4", "/var/www/tt.mp4",
            "396880" /*396.88 seconds*/};
		try {
			MP4StreamingModule.main(args);
                        assert(true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
                        assert(false);
		}
		
	}

}
