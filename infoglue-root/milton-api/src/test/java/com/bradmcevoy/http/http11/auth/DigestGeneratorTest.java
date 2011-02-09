package com.bradmcevoy.http.http11.auth;

import com.bradmcevoy.http.Request.Method;
import junit.framework.TestCase;

/**
 *
 * @author brad
 */
public class DigestGeneratorTest extends TestCase {

    DigestGenerator generator;

    String password = "Circle Of Life";

    String user = "Mufasa";

    String realm = "testrealm@host.com";

    String uri = "/dir/index.html";

    @Override
    protected void setUp() throws Exception {
        generator = new DigestGenerator();
    }



    /**
     * this test matches the example in wikipedia - http://en.wikipedia.org/wiki/Digest_access_authentication
     */
    public void testGenerateDigest() {        
        DigestResponse dr = new DigestResponse(Method.GET, user, realm, "dcd98b7102dd2f0e8b11d0f600bfb0c093", uri, "", "auth", "00000001", "0a4f113b");
        String resp = generator.generateDigest( dr, password);
        System.out.println( "server resp: " + resp );
        System.out.println( "expected: " + "6629fae49393a05397450978507c4ef1" );
        assertEquals( "6629fae49393a05397450978507c4ef1", resp);
    }

    /**
     * this matches the data in milton-test secure folder
     */
    public void testGenerateDigest2() {
        System.out.println( "testGenerateDigest2" );
        DigestResponse dr = new DigestResponse(Method.PROPFIND, user, realm, "ZWY5NTdmZDgtZjg1OC00NzhhLTg4MjctMzBlNzRmMGNjNTE4", "/webdav/secure/", "", "auth", "00000001", "7cfd3b057b80f1d9e2ff691f926c31f5");
        String resp = generator.generateDigest( dr, password);
        System.out.println( "server resp: " + resp );
        System.out.println( "expected: " + "2bd4ead0c52ff8191c2a0464a6e80fbb" );
        assertEquals( "2bd4ead0c52ff8191c2a0464a6e80fbb", resp);
        System.out.println( "----" );
    }


    public void testGenerateDigestWithEncryptedPassword() {
    }

    public void testEncodePasswordInA1Format() {
        String enc = generator.encodePasswordInA1Format( user, realm, password );
        System.out.println( "enc: " + enc );
        assertEquals( "939e7578ed9e3c518a452acee763bce9", enc);
    }

    public void testencodeMethodAndUri() {
        String actual = generator.encodeMethodAndUri( "GET", "/dir/index.html" );
        assertEquals( "39aff3a2bab6126f332b942af96d3366", actual);
    }

    public void testMD5() {
        String actual = generator.md5( "939e7578ed9e3c518a452acee763bce9","dcd98b7102dd2f0e8b11d0f600bfb0c093", "00000001", "0a4f113b", "auth", "39aff3a2bab6126f332b942af96d3366");
        assertEquals( "6629fae49393a05397450978507c4ef1", actual);
    }
}
