Sources from svn://www.ettrema.com/milton/tags/milton-1.5.4/milton-api


/milton-api/src/main/java/com/bradmcevoy/http/HandlerHelper.java
    public boolean checkExpects( Http11ResponseHandler responseHandler, Request request, Response response ) {
        String s = request.getExpectHeader();
        if( s != null && s.length() > 0 ) {
            response.setStatus( Response.Status.SC_CONTINUE);
            return false;
        } else {
            return true;
        }
    }
became :
    public boolean checkExpects( Http11ResponseHandler responseHandler, Request request, Response response ) {
        String s = request.getExpectHeader();
        //Patched for InfoGlue and Mac OS X
        return true;

        //if( s != null && s.length() > 0 ) {
        //    response.setStatus( Response.Status.SC_CONTINUE);
        //    return false;
        //} else {
        //    return true;
        //}
    }


Remove @override annotation from interface override as it is not compatible with java 5
/milton-api/src/main/java/com/bradmcevoy/http/htt11/DeleteHandler.java
/milton-api/src/main/java/com/bradmcevoy/http/htt11/GetHandler.java
/milton-api/src/main/java/com/bradmcevoy/http/htt11/OptionsHandler.java
/milton-api/src/main/java/com/bradmcevoy/http/htt11/PostHandler.java
/milton-api/src/main/java/com/bradmcevoy/http/htt11/PutHandler.java
/milton-api/src/main/java/com/bradmcevoy/http/webdav/CopyHandler.java
/milton-api/src/main/java/com/bradmcevoy/http/webdav/LockHandler.java
/milton-api/src/main/java/com/bradmcevoy/http/webdav/MkColHandler.java
/milton-api/src/main/java/com/bradmcevoy/http/webdav/MoveHandler.java
/milton-api/src/main/java/com/bradmcevoy/http/webdav/PropFindHandler.java
/milton-api/src/main/java/com/bradmcevoy/http/webdav/UnlockHandler.java
/milton-api/src/main/java/com/bradmcevoy/http/webdav/WebDavProtocol.java