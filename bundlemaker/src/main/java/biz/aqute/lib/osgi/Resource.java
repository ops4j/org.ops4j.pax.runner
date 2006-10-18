package biz.aqute.lib.osgi;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface Resource
{

    InputStream openInputStream()
        throws IOException;

    void write( OutputStream out )
        throws IOException;
}
